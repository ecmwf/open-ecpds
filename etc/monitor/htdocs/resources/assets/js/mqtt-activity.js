(function () {
    'use strict';
    const root = document.getElementById('mqttActivity');
    if (!root) return;
    const byId = id => document.getElementById(id);
    const states = {
        QUEUED: {label: 'Queued', color: '#6c757d'},
        PROCESSING: {label: 'Processing', color: '#0dcaf0'},
        ACCEPTED: {label: 'Broker accepted', color: '#198754'},
        PARTIAL: {label: 'Mixed results', color: '#fd7e14'},
        FAILED: {label: 'Failed', color: '#dc3545'},
        UNCONFIRMED: {label: 'Unconfirmed', color: '#b8860b'},
        SKIPPED: {label: 'Skipped', color: '#8675a9'}
    };
    let paused = false, offset = 0, pending = false, timer, timeline, distribution;
    let generation = 0, refreshAgain = false;
    let rows = [], selectedId = null, lastSnapshot = null;
    const number = value => Number(value || 0).toLocaleString();
    const utc = value => value ? new Date(value).toISOString().replace('T', ' ').slice(0, 19) : 'Not started';
    const node = (tag, text, className) => {
        const element = document.createElement(tag);
        if (text !== undefined) element.textContent = text;
        if (className) element.className = className;
        return element;
    };
    function live(label) {
        const dot = node('span', undefined, 'mqtt-dot me-2');
        dot.style.backgroundColor = label === 'Live' ? '#198754' : '#6c757d';
        byId('mqttLive').replaceChildren(dot, document.createTextNode(label));
    }
    function badge(status) {
        const state = states[status] || {label: status, color: '#6c757d'};
        const element = node('span', state.label, 'badge border fw-normal');
        element.style.color = state.color;
        return element;
    }
    function stats(counts) {
        const cards = [
            ['In flight', (counts.QUEUED || 0) + (counts.PROCESSING || 0), '#0dcaf0'],
            ['Broker accepted', counts.ACCEPTED, '#198754'],
            ['Mixed results', counts.PARTIAL, '#fd7e14'],
            ['Failed', counts.FAILED, '#dc3545'],
            ['Unconfirmed', counts.UNCONFIRMED, '#b8860b'],
            ['Skipped', counts.SKIPPED, '#8675a9']
        ];
        byId('mqttStats').replaceChildren(...cards.map(item => {
            const col = node('div', undefined, 'col');
            const card = node('div', undefined, 'mqtt-stat h-100');
            const label = node('div', undefined, 'mqtt-stat-label');
            const marker = node('i', undefined, 'bi bi-circle-fill me-1');
            marker.style.color = item[2];
            marker.setAttribute('aria-hidden', 'true');
            label.append(marker, document.createTextNode(item[0]));
            card.append(label, node('div', number(item[1]), 'mqtt-value'));
            col.append(card);
            return col;
        }));
    }
    function charts(snapshot) {
        const textColor = getComputedStyle(root).color;
        const keys = Object.keys(states);
        const minutes = Math.min(Number(byId('mqttMinutes').value) || snapshot.retentionHours * 60,
            snapshot.now / 60000);
        const width = Math.max(minutes > 60 ? 3600000 : 60000, Math.ceil(minutes / 1440) * 60000);
        const end = Math.floor(snapshot.now / width) * width;
        const start = Math.floor((snapshot.now - minutes * 60000) / width) * width;
        const times = [];
        for (let t = start; t <= end; t += width) times.push(t);
        const grouped = {};
        Object.entries(snapshot.buckets).forEach(([time, counts]) => {
            const bucket = Math.floor(Number(time) / width) * width;
            grouped[bucket] = grouped[bucket] || {};
            Object.entries(counts).forEach(([status, count]) => {
                grouped[bucket][status] = (grouped[bucket][status] || 0) + count;
            });
        });
        const data = {
            labels: times.map(t => new Date(t).toISOString().slice(minutes > 1440 ? 5 : 11, 16).replace('T', ' ')),
            datasets: keys.map(status => ({
                label: states[status].label,
                data: times.map(t => (grouped[t] || {})[status] || 0),
                backgroundColor: states[status].color,
                borderRadius: 3, maxBarThickness: 26
            }))
        };
        if (!timeline) {
            timeline = new Chart(byId('mqttTimeline'), {
                type: 'bar', data: data,
                options: {
                    responsive: true, maintainAspectRatio: false, animation: false,
                    interaction: {mode: 'index', intersect: false},
                    plugins: {legend: {display: false}},
                    scales: {
                        x: {stacked: true, grid: {display: false}, ticks: {color: textColor, maxTicksLimit: 12}},
                        y: {stacked: true, beginAtZero: true, ticks: {color: textColor, precision: 0}}
                    }
                }
            });
        } else {
            timeline.data = data;
            timeline.options.scales.x.ticks.color = textColor;
            timeline.options.scales.y.ticks.color = textColor;
            timeline.update('none');
        }
        const doughnutData = {
            labels: keys.map(k => states[k].label),
            datasets: [{data: keys.map(k => snapshot.counts[k] || 0),
                backgroundColor: keys.map(k => states[k].color), borderWidth: 0, hoverOffset: 6}]
        };
        if (!distribution) {
            distribution = new Chart(byId('mqttDistribution'), {
                type: 'doughnut', data: doughnutData,
                plugins: [{
                    id: 'mqttTotal',
                    afterDraw: chart => {
                        const area = chart.chartArea;
                        if (!area) return;
                        const context = chart.ctx;
                        const total = chart.data.datasets[0].data.reduce((sum, value) => sum + value, 0);
                        const x = (area.left + area.right) / 2, y = (area.top + area.bottom) / 2;
                        context.save();
                        context.textAlign = 'center';
                        context.fillStyle = getComputedStyle(root).color;
                        context.font = '600 27px system-ui';
                        context.fillText(number(total), x, y);
                        context.font = '11px system-ui';
                        context.fillText('Publications', x, y + 20);
                        context.restore();
                    }
                }],
                options: {
                    responsive: true, maintainAspectRatio: false, animation: false, cutout: '72%',
                    plugins: {legend: {position: 'right', labels: {color: textColor, boxWidth: 9, usePointStyle: true}}}
                }
            });
        } else {
            distribution.data = doughnutData;
            distribution.options.plugins.legend.labels.color = textColor;
            distribution.update('none');
        }
    }
    function details(entry, open) {
        selectedId = entry.id;
        const content = byId('mqttDetailContent');
        content.replaceChildren();
        content.append(badge(entry.status));
        const list = node('dl', undefined, 'row mt-3 small');
        const fields = [
            ['Publication', entry.id], ['Destination', entry.destination], ['File', entry.filename],
            ['Topic', entry.topic || 'Not resolved'], ['Queued (UTC)', utc(entry.queuedAt)],
            ['Processing started (UTC)', utc(entry.startedAt)], ['Last update (UTC)', utc(entry.updatedAt)],
            ['Processing time', entry.startedAt ? number(Math.max(0, entry.updatedAt - entry.startedAt)) + ' ms' : 'Not started'],
            ['Processing attempts', entry.attempts], ['QoS', entry.qos < 0 ? 'Not resolved' : entry.qos],
            ['Retain', entry.retain ? 'Yes' : 'No'], ['Payload size', number(entry.payloadBytes) + ' bytes'],
            ['Content type', entry.contentType || 'Not specified'], ['Reason', entry.reason || 'None']
        ];
        fields.forEach(([label, value]) => {
            list.append(node('dt', label, 'col-sm-5 text-body-secondary'), node('dd', value, 'col-sm-7 mqtt-detail-value'));
        });
        const transfer = node('dd', undefined, 'col-sm-7');
        const link = node('a', 'DataTransfer-' + entry.transferId);
        link.href = '/do/transfer/data/' + encodeURIComponent(entry.transferId);
        transfer.append(link);
        list.append(node('dt', 'Related transfer', 'col-sm-5 text-body-secondary'), transfer);
        content.append(list, node('h6', 'Per-Mover broker outcomes', 'mt-4'));
        if (!entry.brokers.length) content.append(node('p', 'No broker result recorded yet.', 'text-body-secondary small'));
        entry.brokers.forEach(broker => {
            const item = node('div', undefined, 'border rounded p-3 mb-2');
            item.append(node('strong', broker.mover, 'd-block mb-2'), badge(broker.status),
                node('div', utc(broker.completedAt) + ' UTC', 'small text-body-secondary mt-2'));
            if (broker.reason) item.append(node('div', broker.reason, 'small mt-2 mqtt-detail-value'));
            content.append(item);
        });
        content.append(node('p', 'Broker acceptance does not confirm subscriber delivery. Payload contents are not retained.', 'small text-body-secondary mt-3'));
        if (open) bootstrap.Offcanvas.getOrCreateInstance(byId('mqttDetails')).show();
    }
    function render(snapshot) {
        lastSnapshot = snapshot;
        byId('mqttLimits').textContent = ': ' + number(snapshot.maxEntries) + ' publications, '
            + number(snapshot.maxBytes) + ' bytes, ' + number(snapshot.retentionHours) + ' hours';
        rows = snapshot.rows;
        const select = byId('mqttDestination'), chosen = select.value;
        const all = node('option', 'All authorized destinations');
        all.value = '';
        select.replaceChildren(all, ...snapshot.destinations.map(name => {
            const option = node('option', name); option.value = name; return option;
        }));
        if (chosen && !snapshot.destinations.includes(chosen)) {
            const option = node('option', chosen); option.value = chosen; select.append(option);
        }
        select.value = chosen;
        stats(snapshot.counts);
        byId('mqttLastAccepted').textContent = snapshot.lastAcceptedAt ? utc(snapshot.lastAcceptedAt) : 'None recorded';
        byId('mqttOldestPending').textContent = snapshot.oldestPendingAt
            ? number(Math.max(0, Math.floor((snapshot.now - snapshot.oldestPendingAt) / 1000))) + ' s'
            : 'None recorded';
        charts(snapshot);
        byId('mqttRows').replaceChildren(...rows.map(entry => {
            const row = node('tr');
            row.append(node('td', utc(entry.updatedAt), 'text-nowrap'));
            const file = node('td');
            file.append(node('strong', entry.destination, 'd-block'), node('span', entry.filename, 'text-break'));
            row.append(file, node('td', entry.topic || 'Not resolved', 'mqtt-topic'));
            const outcome = node('td'); outcome.append(badge(entry.status)); row.append(outcome);
            row.append(node('td', number(entry.brokers.filter(b => b.status === 'ACCEPTED').length) + ' / ' + number(entry.brokers.length)));
            const action = node('td'), button = node('button', 'View', 'btn btn-sm btn-outline-secondary');
            button.type = 'button';
            button.setAttribute('aria-label', 'View publication ' + entry.id);
            button.addEventListener('click', () => details(entry, true));
            action.append(button); row.append(action);
            return row;
        }));
        if (!rows.length) {
            const row = node('tr'), cell = node('td', 'No MQTT publications match these filters. Upload a file to an event-enabled destination to see its notification here.', 'text-center text-body-secondary p-4');
            cell.colSpan = 6; row.append(cell); byId('mqttRows').append(row);
        }
        const selected = rows.find(entry => entry.id === selectedId);
        if (selected) details(selected, false);
        byId('mqttUpdated').textContent = 'Updated ' + utc(snapshot.now) + ' UTC';
        byId('mqttPage').textContent = snapshot.total ? (offset + 1) + '-' + Math.min(offset + 100, snapshot.total) + ' of ' + number(snapshot.total) : '0 publications';
        byId('mqttPrevious').disabled = offset === 0;
        byId('mqttNext').disabled = offset + 100 >= snapshot.total;
        byId('mqttPersistence').textContent = snapshot.persistenceError;
        byId('mqttPersistence').classList.toggle('d-none', !snapshot.persistenceError);
    }
    async function refresh(force) {
        clearTimeout(timer);
        if (pending) { refreshAgain = true; return; }
        if (document.hidden || (paused && !force)) return;
        pending = true;
        const requestGeneration = generation;
        const query = new URLSearchParams({
            view: 'data', minutes: byId('mqttMinutes').value, destination: byId('mqttDestination').value,
            topic: byId('mqttTopic').value, status: byId('mqttStatus').value, offset: offset
        });
        try {
            const response = await fetch('/do/monitoring/mqtt?' + query, {
                credentials: 'same-origin', headers: {'X-Requested-With': 'XMLHttpRequest'},
                signal: AbortSignal.timeout(20000)
            });
            if (!response.ok) throw new Error('HTTP ' + response.status);
            const snapshot = await response.json();
            if (requestGeneration !== generation || (paused && !force)) return;
            if (offset >= snapshot.total && offset) { offset = 0; return; }
            render(snapshot);
            byId('mqttError').classList.add('d-none');
            live(paused ? 'Paused' : 'Live');
        } catch (error) {
            if (requestGeneration !== generation || (paused && !force)) return;
            byId('mqttError').textContent = 'Activity could not be refreshed. Displayed values may be stale; check the session or Master connection.';
            byId('mqttError').classList.remove('d-none');
            live('Disconnected');
        } finally {
            pending = false;
            if (refreshAgain) {
                refreshAgain = false;
                timer = setTimeout(() => refresh(true), 0);
            } else if (!paused && !document.hidden) timer = setTimeout(() => refresh(false), 5000);
        }
    }
    byId('mqttPause').addEventListener('click', () => {
        paused = !paused;
        generation++;
        byId('mqttPause').textContent = paused ? 'Resume updates' : 'Pause updates';
        byId('mqttPause').setAttribute('aria-pressed', String(paused));
        live(paused ? 'Paused' : 'Live');
        if (paused) { clearTimeout(timer); refreshAgain = false; } else refresh();
    });
    byId('mqttFilters').addEventListener('submit', event => {
        event.preventDefault(); offset = 0;
        generation++;
        paused = false;
        byId('mqttPause').textContent = 'Pause updates';
        byId('mqttPause').setAttribute('aria-pressed', 'false');
        refresh();
    });
    byId('mqttPrevious').addEventListener('click', () => { offset = Math.max(0, offset - 100); generation++; refresh(true); });
    byId('mqttNext').addEventListener('click', () => { offset += 100; generation++; refresh(true); });
    document.addEventListener('visibilitychange', () => { if (!document.hidden) refresh(); else clearTimeout(timer); });
    new MutationObserver(() => { if (lastSnapshot) charts(lastSnapshot); })
        .observe(document.documentElement, {attributes: true, attributeFilter: ['data-bs-theme']});
    window.addEventListener('pagehide', () => {
        paused = true; clearTimeout(timer);
        if (timeline) timeline.destroy();
        if (distribution) distribution.destroy();
    });
    refresh();
}());
