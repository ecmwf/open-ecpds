/* Network Weather administration builders: they only assist entry; the Master validates every submission. */
(function (root) {
    "use strict";
    var FIELDS = {paths: 4, groups: 2, probes: 7};
    var MODES = {TCP_CONNECT: 60, MTR_ICMP: 300, MTR_TCP: 300};
    var INTERVALS = [60, 300, 900, 1800, 3600, 10800, 21600, 43200, 86400];
    var COMMON_PORTS = [21, 22, 80, 443, 990];
    var OTHER = "\u0000other";

    function parse(kind, text) {
        return String(text || "").split(/\r\n|\r|\n/).filter(function (line) {
            return line.trim() !== "";
        }).map(function (line) {
            var fields = line.split("|").map(function (field) { return field.trim(); });
            return fields.length === FIELDS[kind] ? {fields: fields} : {fields: [line.trim()], invalid: true};
        });
    }

    function serialize(rows) {
        return rows.map(function (row) { return row.fields.join(" | "); }).join("\n");
    }

    function key(kind, fields) {
        var normalized = fields.map(function (field) { return String(field).trim(); });
        return kind === "groups" ? normalized[0] : normalized.join("\u0000");
    }

    function checkText(value, what) {
        value = String(value == null ? "" : value).trim();
        if (!value) throw new Error("Select " + what);
        if (value.length > 255 || /[|\u0000-\u001f\u007f]/.test(value))
            throw new Error("Invalid " + what + ": the character '|' and control characters are not allowed");
        return value;
    }

    function interval(seconds) {
        return seconds % 86400 === 0 ? seconds / 86400 + " day" + (seconds > 86400 ? "s" : "") :
            seconds % 3600 === 0 ? seconds / 3600 + " hour" + (seconds > 3600 ? "s" : "") :
                seconds / 60 + " minute" + (seconds > 60 ? "s" : "");
    }

    function intervals(mode) {
        return INTERVALS.filter(function (seconds) { return seconds >= (MODES[mode] || 300); });
    }

    function suggestPort(method) {
        method = String(method || "").toLowerCase();
        if (/sftp|ssh|scp/.test(method)) return 22;
        if (/ftps/.test(method)) return 990;
        if (/ftp/.test(method)) return 21;
        if (/https|s3|azure|gcs|google/.test(method)) return 443;
        if (/http/.test(method)) return 80;
        return null;
    }

    function probeFields(path, port, mode, seconds) {
        var number = Number(port);
        if (!Number.isInteger(number) || number < 1 || number > 65535) throw new Error("Enter a port between 1 and 65535");
        if (!Object.prototype.hasOwnProperty.call(MODES, mode)) throw new Error("Select a probe mode");
        seconds = Number(seconds);
        if (intervals(mode).indexOf(seconds) === -1) throw new Error("Select an interval allowed for " + mode);
        return path.concat([String(number), mode, String(seconds)]);
    }

    function add(kind, rows, fields, max) {
        if (rows.length >= max) throw new Error("At most " + max + " entries are allowed");
        var k = key(kind, fields);
        if (rows.some(function (row) { return !row.invalid && key(kind, row.fields) === k; }))
            throw new Error(kind === "groups" ? "This source already has a transfer group" : "Already in the list");
        rows.push({fields: fields});
        return rows;
    }

    function available(kind, rows, fields) {
        return !rows.some(function (row) {
            return !row.invalid && key(kind, row.fields) === key(kind, fields);
        });
    }

    function mergeCandidates(kind, text, entries, max) {
        var rows = parse(kind, text);
        var indexed = new Map(rows.filter(function (row) { return !row.invalid; })
            .map(function (row) { return [key(kind, row.fields), row]; }));
        entries.forEach(function (fields) {
            var k = key(kind, fields);
            var existing = indexed.get(k);
            if (existing) {
                if (kind === "groups" && existing.fields[1] !== fields[1])
                    throw new Error("Source " + fields[0] + " would have multiple transfer groups. "
                        + "Choose Hosts from one group or add individual paths instead.");
                return;
            }
            if (rows.length >= max) throw new Error("At most " + max + " entries are allowed");
            var row = {fields: fields};
            rows.push(row);
            indexed.set(k, row);
        });
        return serialize(rows);
    }

    function newCandidates(text, candidates) {
        var existing = new Set(parse("paths", text).filter(function (row) { return !row.invalid; })
            .map(function (row) { return key("paths", row.fields); }));
        return candidates.filter(function (host) {
            var k = key("paths", [host.source, host.id, host.target, host.method]);
            if (existing.has(k)) return false;
            existing.add(k);
            return true;
        });
    }

    var api = {parse: parse, serialize: serialize, add: add, checkText: checkText, probeFields: probeFields,
        intervals: intervals, interval: interval, suggestPort: suggestPort, available: available,
        mergeCandidates: mergeCandidates, newCandidates: newCandidates, MODES: MODES};
    root.NetworkWeatherAdmin = api;
    if (typeof document === "undefined") return;

    var choices = {sources: [], groups: [], hosts: []};
    try {
        var element = document.getElementById("nwChoices");
        if (element) choices = JSON.parse(element.textContent);
    } catch (error) {
        choices = {sources: [], groups: [], hosts: [], error: "Selector choices unavailable"};
    }
    var hostsById = new Map((choices.hosts || []).map(function (host) { return [host.id, host]; }));
    var counter = 0;

    function el(tag, className, text) {
        var node = document.createElement(tag);
        if (className) node.className = className;
        if (text != null) node.textContent = text;
        return node;
    }

    function option(value, label) {
        var node = el("option", null, label);
        node.value = value;
        return node;
    }

    function select(id, placeholder) {
        var node = el("select", "form-select form-select-sm");
        node.id = id;
        node.style.minWidth = "0";
        node.style.maxWidth = "100%";
        node.style.textOverflow = "ellipsis";
        if (placeholder) node.appendChild(option("", placeholder));
        return node;
    }

    function hostLabel(host) {
        return host.id + (host.nickname && host.nickname !== host.id ? " \u2014 " + host.nickname : "") +
            " (" + host.target + ", " + host.method + ")";
    }

    function field(label, control, width) {
        var col = el("div", width || "col-md-3");
        col.style.minWidth = "0";
        var caption = el("label", "form-label small mb-1", label);
        caption.htmlFor = control.id;
        col.append(caption, control);
        return col;
    }

    function sourcePicker(prefix) {
        var list = select(prefix + "-source", "Select a source\u2026");
        (choices.sources || []).slice().sort(function (a, b) { return a.id.localeCompare(b.id); })
            .forEach(function (source) {
                list.appendChild(option(source.id, source.id + (source.group ? " (" + source.group + ")" : "") +
                    (source.active === false ? " \u2014 inactive" : "")));
            });
        list.appendChild(option(OTHER, "Other source (e.g. Continental Mover)\u2026"));
        var other = el("input", "form-control form-control-sm mt-1");
        other.type = "text";
        other.maxLength = 128;
        other.placeholder = "Data Mover name";
        other.hidden = true;
        other.setAttribute("aria-label", "Other source name");
        list.addEventListener("change", function () {
            other.hidden = list.value !== OTHER;
            if (!other.hidden) other.focus();
        });
        var wrapper = field("Source", list);
        wrapper.appendChild(other);
        return {node: wrapper, current: function () {
            return (list.value === OTHER ? other.value : list.value).trim();
        }, value: function () {
            return checkText(list.value === OTHER ? other.value : list.value, "a source");
        }};
    }

    function hostPicker(prefix, allowed) {
        var filter = el("input", "form-control form-control-sm mb-1");
        filter.type = "search";
        filter.placeholder = "Filter Hosts (ID, name, target, method)";
        filter.setAttribute("aria-label", "Filter Hosts");
        var list = select(prefix + "-host");
        var info = el("div", "form-text");
        function fill() {
            var current = list.value;
            var words = filter.value.toLowerCase().split(/\s+/).filter(Boolean);
            var matches = (choices.hosts || []).filter(function (host) {
                var text = (host.id + " " + host.nickname + " " + host.target + " " + host.method + " " + host.group)
                    .toLowerCase();
                return allowed(host) && words.every(function (word) { return text.indexOf(word) !== -1; });
            });
            list.replaceChildren(option("", matches.length ? "Select a Host\u2026" : "No matching active Host"));
            matches.slice(0, 300).forEach(function (host) { list.appendChild(option(host.id, hostLabel(host))); });
            if (matches.slice(0, 300).some(function (host) { return host.id === current; })) list.value = current;
            info.textContent = matches.length > 300 ? "Showing 300 of " + matches.length + "; refine the filter." :
                matches.length + " active dissemination Host(s)" + (choices.truncated ? " (list truncated)" : "");
        }
        filter.addEventListener("input", fill);
        fill();
        var wrapper = field("Host", list, "col-md-5");
        wrapper.insertBefore(filter, list);
        wrapper.appendChild(info);
        return {node: wrapper, select: list, refresh: fill, value: function () {
            var host = hostsById.get(list.value);
            if (!host) throw new Error("Select a Host");
            return [checkText(host.id, "a Host"), checkText(host.target, "the Host target"),
                checkText(host.method, "the Host method")];
        }};
    }

    function builder(textarea) {
        var kind = textarea.dataset.nwBuilder;
        var max = Number(textarea.dataset.nwMax);
        if (!Number.isSafeInteger(max) || max < 1) {
            textarea.before(el("div", "alert alert-danger", "Deployment limits unavailable; reload the page before editing."));
            return;
        }
        var rows = parse(kind, textarea.value);
        var prefix = "nwb" + (++counter);
        var box = el("div", "border rounded p-2");
        var tableWrap = el("div", "table-responsive mb-2");
        tableWrap.tabIndex = 0;
        tableWrap.setAttribute("role", "region");
        var label = document.querySelector("label[for='" + textarea.id + "']");
        tableWrap.setAttribute("aria-label", (label ? label.textContent.trim() : "Scope entries") + " table");
        var table = el("table", "table table-sm align-middle mb-0");
        var head = {paths: ["Source", "Host", "Target", "Method"], groups: ["Source", "Transfer group"],
            probes: ["Source", "Host", "Target", "Method", "Port", "Mode", "Interval"]}[kind];
        var thead = el("thead"), headRow = el("tr");
        head.concat([""]).forEach(function (name) { headRow.appendChild(el("th", "small", name)); });
        thead.appendChild(headRow);
        var tbody = el("tbody");
        table.append(thead, tbody);
        var count = el("span", "small text-muted");
        var feedback = el("div", "small text-danger mt-1");
        feedback.setAttribute("role", "alert");
        var form = el("div", "row g-2 align-items-end bg-body-tertiary rounded p-2 mx-0");

        function sync() {
            textarea.value = serialize(rows);
            tbody.replaceChildren();
            rows.forEach(function (row, index) {
                var tr = el("tr");
                if (row.invalid) {
                    var td = el("td", "text-danger font-monospace small", row.fields[0]);
                    td.colSpan = head.length;
                    td.prepend(el("span", "badge text-bg-danger me-2", "invalid"));
                    tr.appendChild(td);
                } else {
                    row.fields.forEach(function (value, column) {
                        var cell = el("td", "small", column === 6 ? interval(Number(value)) : value);
                        if (column === 1 && kind !== "groups" && choices.hosts && choices.hosts.length &&
                            !hostsById.has(value)) {
                            cell.appendChild(el("span", "badge text-bg-warning ms-1", "not active"));
                        }
                        tr.appendChild(cell);
                    });
                }
                var remove = el("button", "btn btn-sm btn-outline-danger");
                remove.type = "button";
                remove.title = "Remove";
                remove.setAttribute("aria-label", "Remove entry " + (index + 1));
                remove.appendChild(el("i", "bi bi-x-lg"));
                remove.addEventListener("click", function () {
                    rows.splice(index, 1);
                    feedback.textContent = "";
                    sync();
                });
                var action = el("td", "text-end");
                action.appendChild(remove);
                tr.appendChild(action);
                tbody.appendChild(tr);
            });
            if (!rows.length) {
                var empty = el("td", "small text-muted", "No entries");
                empty.colSpan = head.length + 1;
                tbody.appendChild(el("tr")).appendChild(empty);
            }
            count.textContent = rows.length + " / " + max;
            document.dispatchEvent(new CustomEvent("nw-change", {detail: textarea.id}));
        }

        function attempt(fields) {
            try {
                add(kind, rows, fields(), max);
                feedback.textContent = "";
                sync();
            } catch (error) {
                feedback.textContent = error.message;
            }
        }

        function addButton(handler, label, width) {
            var button = el("button", "btn btn-sm btn-success w-100");
            button.type = "button";
            button.appendChild(el("i", "bi bi-plus-lg me-1"));
            button.appendChild(document.createTextNode(label || "Add"));
            button.addEventListener("click", handler);
            var col = el("div", width || "col-md-1");
            col.appendChild(button);
            return col;
        }

        var source, host, port, mode, every, suggestion, quick;
        function pathAvailable(path) {
            if (kind === "probes") {
                if (!port || !mode || !every) return true;
                return available(kind, rows, path.concat([String(Number(port.value)), mode.value,
                    String(Number(every.value))]));
            }
            return available(kind, rows, path);
        }
        if (kind === "groups") {
            source = sourcePicker(prefix);
            var group = select(prefix + "-group", "Select a transfer group\u2026");
            var groupAdd = addButton(function () {
                attempt(function () { return [source.value(), checkText(group.value, "a transfer group")]; });
            });
            var refreshGroups = function () {
                var current = group.value;
                var selectedSource = source.current();
                var used = !available("groups", rows, [selectedSource]);
                group.replaceChildren(option("", used ? "This source already has a transfer group" :
                    selectedSource ? "Select a transfer group\u2026" : "Select a source first"));
                group.disabled = !selectedSource || used;
                if (!group.disabled) {
                    (choices.groups || []).forEach(function (name) { group.appendChild(option(name, name)); });
                    if ((choices.groups || []).indexOf(current) !== -1) group.value = current;
                }
                groupAdd.querySelector("button").disabled = group.disabled || !group.value;
            };
            source.node.addEventListener("change", refreshGroups);
            source.node.addEventListener("input", refreshGroups);
            group.addEventListener("change", refreshGroups);
            document.addEventListener("nw-change", function (event) {
                if (event.detail === textarea.id) refreshGroups();
            });
            refreshGroups();
            form.append(source.node, field("Transfer group", group, "col-md-4"), groupAdd);
        } else {
            source = sourcePicker(prefix);
            host = hostPicker(prefix, function (candidate) {
                var path = [source.current(), candidate.id, candidate.target, candidate.method];
                return pathAvailable(path);
            });
            source.node.addEventListener("change", host.refresh);
            source.node.addEventListener("input", host.refresh);
            document.addEventListener("nw-change", function (event) {
                if (event.detail === textarea.id) host.refresh();
            });
            form.append(source.node, host.node);
            if (kind === "probes") {
                port = el("input", "form-control form-control-sm");
                port.type = "number";
                port.min = 1;
                port.max = 65535;
                port.id = prefix + "-port";
                port.setAttribute("list", prefix + "-ports");
                var ports = el("datalist");
                ports.id = prefix + "-ports";
                COMMON_PORTS.forEach(function (value) { ports.appendChild(option(String(value), String(value))); });
                mode = select(prefix + "-mode");
                Object.keys(MODES).forEach(function (name) { mode.appendChild(option(name, name)); });
                every = select(prefix + "-interval");
                var fillIntervals = function () {
                    var previous = Number(every.value);
                    every.replaceChildren();
                    intervals(mode.value).forEach(function (seconds) {
                        every.appendChild(option(String(seconds), interval(seconds)));
                    });
                    if (intervals(mode.value).indexOf(previous) !== -1) every.value = String(previous);
                };
                mode.addEventListener("change", fillIntervals);
                fillIntervals();
                var portField = field("Port", port, "col-md-1");
                portField.appendChild(ports);
                var hint = el("div", "form-text");
                portField.appendChild(hint);
                host.select.addEventListener("change", function () {
                    var chosen = hostsById.get(host.select.value);
                    var suggested = chosen && suggestPort(chosen.method);
                    hint.textContent = suggested ? "Usual: " + suggested : "";
                    if (suggested && !port.value) port.value = suggested;
                });
                form.append(portField, field("Mode", mode, "col-md-2"), field("Interval", every, "col-md-2"));
            }
            form.appendChild(addButton(function () {
                attempt(function () {
                    var path = [source.value()].concat(host.value());
                    return kind === "probes" ? probeFields(path, port.value, mode.value, every.value) : path;
                });
            }, "Add manually", "col-md-2"));
            if (textarea.dataset.nwSuggest) {
                suggestion = select(prefix + "-suggest", "Select an approved candidate\u2026");
                var candidateButton;
                var refreshSuggestions = function () {
                    var current = suggestion.value;
                    var approved = document.getElementById(textarea.dataset.nwSuggest);
                    suggestion.replaceChildren(option("", "Select an approved candidate\u2026"));
                    parse("paths", approved ? approved.value : "").forEach(function (row) {
                        if (!row.invalid && pathAvailable(row.fields))
                            suggestion.appendChild(option(row.fields.join("\u0000"), row.fields.join(" | ")));
                    });
                    suggestion.value = current;
                    if (candidateButton) candidateButton.disabled = !suggestion.value;
                };
                document.addEventListener("nw-change", function (event) {
                    if (event.detail === textarea.dataset.nwSuggest || event.detail === textarea.id)
                        refreshSuggestions();
                });
                refreshSuggestions();
                quick = el("div", "row g-2 align-items-end mx-0 mt-1 px-2");
                quick.append(field("Or pick an approved candidate", suggestion, "col-md-7"));
                if (kind === "probes") {
                    quick.appendChild(el("div", "col-md-3 form-text", "Uses the port, mode and interval above."));
                }
                candidateButton = addButton(function () {
                    attempt(function () {
                        var path = suggestion.value.split("\u0000");
                        return kind === "probes" ? probeFields(path, port.value, mode.value, every.value) : path;
                    });
                }, "Add candidate", "col-md-2");
                var candidateColumn = candidateButton;
                candidateButton = candidateColumn.querySelector("button");
                candidateButton.disabled = !suggestion.value;
                suggestion.addEventListener("change", function () {
                    candidateButton.disabled = !suggestion.value;
                });
                quick.appendChild(candidateColumn);
            }
            if (kind === "probes") {
                var refreshProbeChoices = function () {
                    host.refresh();
                    if (refreshSuggestions) refreshSuggestions();
                };
                [port, mode, every].forEach(function (control) {
                    control.addEventListener("input", refreshProbeChoices);
                    control.addEventListener("change", refreshProbeChoices);
                });
                host.select.addEventListener("change", refreshProbeChoices);
            }
        }

        var footer = el("div", "d-flex justify-content-between align-items-center mt-1");
        footer.append(feedback, count);
        tableWrap.appendChild(table);
        box.append(tableWrap, form);
        if (quick) box.appendChild(quick);
        box.appendChild(footer);
        textarea.hidden = true;
        textarea.after(box);
        textarea.addEventListener("nw-import", function () {
            rows = parse(kind, textarea.value);
            sync();
        });
        if (label) label.htmlFor = prefix + "-source";
        sync();
    }

    function dataTables() {
        var $ = root.jQuery;
        if (!$ || !$.fn || !$.fn.DataTable) return [];
        var lengths = [10, 25, 50, 100];
        var tables = [];
        document.querySelectorAll("table[data-nw-table]").forEach(function (node) {
            var key = node.dataset.nwTable;
            var length = 25;
            try {
                var stored = parseInt(localStorage.getItem("nwPageLen-" + key), 10);
                if (lengths.indexOf(stored) >= 0) length = stored;
            } catch (error) { /* default */ }
            var order = (node.dataset.nwOrder || "0:asc").split(":");
            var table = $(node).DataTable({
                paging: true,
                searching: true,
                info: true,
                autoWidth: false,
                order: [[parseInt(order[0], 10) || 0, order[1] === "desc" ? "desc" : "asc"]],
                pageLength: length,
                dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
                language: {
                    info: "Showing _START_-_END_ of _TOTAL_",
                    infoEmpty: "",
                    emptyTable: node.dataset.nwEmpty || "No data",
                    zeroRecords: "No matching entries found."
                }
            });
            var search = document.getElementById(key + "Search");
            if (search) search.addEventListener("input", function () { table.search(this.value).draw(); });
            var select = document.getElementById(key + "PageLen");
            if (select) {
                select.value = String(table.page.len());
                select.addEventListener("change", function () {
                    var value = +this.value;
                    try { localStorage.setItem("nwPageLen-" + key, value); } catch (error) { /* not remembered */ }
                    table.page.len(value).draw();
                });
            }
            tables.push(table);
        });
        return tables;
    }

    function start() {
        var tables = dataTables();
        if (choices.error) {
            var first = document.querySelector("[data-nw-builder]");
            if (first) {
                var warning = el("div", "alert alert-warning py-1 small", choices.error);
                first.parentNode.prepend(warning);
            }
        }
        document.querySelectorAll("textarea[data-nw-builder]").forEach(builder);
        historySearch();
        function formState() {
            var values = [];
            document.querySelectorAll("textarea[data-nw-builder]").forEach(function (textarea) {
                values.push(textarea.value);
                var box = textarea.nextElementSibling;
                if (box) box.querySelectorAll("input, select").forEach(function (control) {
                    values.push(control.value);
                });
            });
            document.querySelectorAll("input[form='nwAutomationForm'][type='checkbox']").forEach(function (control) {
                values.push(control.checked);
            });
            return JSON.stringify(values);
        }

        function historySearch() {
            var modal = document.getElementById("nwHistoryModal");
            if (!modal) return;
            var form = document.getElementById("nwHistorySearch");
            var source = sourcePicker("nw-history");
            source.node.className = "";
            document.getElementById("nwHistorySource").appendChild(source.node);
            var days = document.getElementById("nwHistoryDays");
            var protocol = document.getElementById("nwHistoryProtocol");
            (choices.protocols || []).forEach(function (name) { protocol.appendChild(option(name, name)); });
            var search = document.getElementById("nwHistorySearchButton");
            var apply = document.getElementById("nwHistoryApply");
            var feedback = document.getElementById("nwHistoryFeedback");
            var preview = document.getElementById("nwHistoryPreview");
            var mode = document.getElementById("nwHistoryMode");
            var matches = [];
            var selected = new Set();
            function invalidate() {
                matches = [];
                selected.clear();
                preview.replaceChildren();
                apply.disabled = true;
                feedback.textContent = "";
            }
            form.addEventListener("input", invalidate);
            form.addEventListener("change", invalidate);
            form.addEventListener("submit", async function (event) {
                event.preventDefault();
                if (search.disabled) return;
                invalidate();
                var controller = new AbortController();
                var timeout;
                try {
                    var period = Number(days.value);
                    if (!Number.isInteger(period) || period < 1 || period > 30)
                        throw new Error("Choose a history period from 1 to 30 days");
                    var parameters = new URLSearchParams({
                        op: "findCandidates", source: source.value(), days: String(period), protocol: protocol.value,
                        csrf: document.querySelector("#nwAutomationForm input[name='csrf']").value
                    });
                    search.disabled = true;
                    feedback.textContent = "Searching transfer history\u2026";
                    timeout = setTimeout(function () { controller.abort(); }, 10000);
                    var response = await root.fetch("/do/admin/networkweather", {
                        method: "POST", body: parameters, signal: controller.signal,
                        headers: {"Accept": "application/json"}
                    });
                    var result = await response.json();
                    if (!response.ok || result.error) throw new Error(result.error || "History search failed");
                    if (!Array.isArray(result.candidates)) throw new Error("Invalid history search response");
                    // A changed source or period must not apply results from an earlier search.
                    if (source.current() !== parameters.get("source") || Number(days.value) !== period
                        || protocol.value !== parameters.get("protocol")) {
                        throw new Error("Search settings changed. Search again to update the preview.");
                    }
                    matches = newCandidates(document.getElementById("nwApproved").value, result.candidates);
                    var alreadyApproved = result.candidates.length - matches.length;
                    selected = new Set(matches.map(function (_, index) { return index; }));
                    var filter = el("input", "form-control form-control-sm mb-2");
                    filter.type = "search";
                    filter.placeholder = "Filter matching Hosts\u2026";
                    filter.setAttribute("aria-label", "Filter matching Hosts");
                    var table = el("table", "table table-sm align-middle mb-0");
                    var head = el("tr"), all = el("input", "form-check-input");
                    all.type = "checkbox";
                    all.checked = true;
                    all.setAttribute("aria-label", "Select all filtered Hosts across pages");
                    head.appendChild(el("th")).appendChild(all);
                    ["Source", "Host", "Target / method", "Transfer group"].forEach(function (label) {
                        head.appendChild(el("th", null, label));
                    });
                    table.appendChild(el("thead")).appendChild(head);
                    var body = el("tbody");
                    var page = 0, filtered = [], summary = el("span", "small text-muted");
                    var previous = el("button", "btn btn-outline-secondary btn-sm", "Previous");
                    var next = el("button", "btn btn-outline-secondary btn-sm", "Next");
                    previous.type = next.type = "button";
                    function render() {
                        var term = filter.value.trim().toLowerCase();
                        filtered = matches.map(function (host, index) { return {host: host, index: index}; })
                            .filter(function (entry) {
                                var host = entry.host;
                                return [host.source, host.id, host.nickname, host.target, host.method, host.group]
                                    .join(" ").toLowerCase().includes(term);
                            });
                        page = Math.min(page, Math.max(0, Math.ceil(filtered.length / 50) - 1));
                        body.replaceChildren();
                        filtered.slice(page * 50, (page + 1) * 50).forEach(function (entry) {
                            var host = entry.host, index = entry.index;
                            var row = el("tr"), check = el("input", "form-check-input");
                            check.type = "checkbox";
                            check.checked = selected.has(index);
                            check.dataset.index = index;
                            check.setAttribute("aria-label", "Select Host " + host.id);
                            row.appendChild(el("td")).appendChild(check);
                            [host.source, host.id + (host.nickname ? " \u2014 " + host.nickname : ""),
                                host.target + " / " + host.method, host.group || "No group"].forEach(function (text) {
                                row.appendChild(el("td", "small", text));
                            });
                            body.appendChild(row);
                        });
                        previous.disabled = page === 0;
                        next.disabled = (page + 1) * 50 >= filtered.length;
                        summary.textContent = selected.size + " selected; " + filtered.length + " matching filter; page "
                            + (page + 1) + " / " + Math.max(1, Math.ceil(filtered.length / 50));
                        apply.disabled = selected.size === 0;
                        var selectedFiltered = filtered.filter(function (entry) { return selected.has(entry.index); }).length;
                        all.checked = filtered.length > 0 && selectedFiltered === filtered.length;
                        all.indeterminate = selectedFiltered > 0 && selectedFiltered < filtered.length;
                    }
                    table.appendChild(body);
                    var pagination = el("div", "d-flex flex-wrap align-items-center gap-2 mt-2");
                    pagination.append(summary, previous, next);
                    if (matches.length) preview.append(filter, table, pagination);
                    filter.addEventListener("input", function () { page = 0; render(); });
                    previous.addEventListener("click", function () { page--; render(); });
                    next.addEventListener("click", function () { page++; render(); });
                    all.addEventListener("change", function () {
                        filtered.forEach(function (entry) {
                            if (all.checked) selected.add(entry.index);
                            else selected.delete(entry.index);
                        });
                        render();
                    });
                    body.addEventListener("change", function (event) {
                        var index = Number(event.target.dataset.index);
                        if (event.target.checked) selected.add(index);
                        else selected.delete(index);
                        render();
                    });
                    render();
                    feedback.textContent = matches.length ? matches.length + " matching Hosts. Review before adding."
                        : alreadyApproved ? "All matching paths are already approved."
                            : "No active dissemination Hosts with a successful transfer in this period.";
                    if (alreadyApproved && matches.length)
                        feedback.textContent += " " + alreadyApproved + " already-approved or duplicate paths omitted.";
                } catch (error) {
                    feedback.textContent = error.name === "AbortError" ? "History search timed out. Try a shorter period."
                        : "Unable to search history: " + error.message;
                } finally {
                    clearTimeout(timeout);
                    search.disabled = false;
                }
            });
            apply.addEventListener("click", function () {
                try {
                    var groups = mode.value === "groups";
                    var textarea = document.getElementById(groups ? "nwSourceGroups" : "nwApproved");
                    var entries = Array.from(selected).map(function (index) {
                        var host = matches[index];
                        return groups ? [checkText(host.source, "a source"), checkText(host.group, "a transfer group")]
                            : [host.source, host.id, host.target, host.method];
                    });
                    if (!entries.length) throw new Error("Select at least one Host");
                    textarea.value = mergeCandidates(groups ? "groups" : "paths", textarea.value, entries,
                        Number(textarea.dataset.nwMax));
                    textarea.dispatchEvent(new CustomEvent("nw-import"));
                    root.bootstrap.Modal.getOrCreateInstance(modal).hide();
                    document.getElementById("nwHistoryAdded").hidden = false;
                } catch (error) {
                    feedback.textContent = error.message;
                }
            });
        }
        var initialFormState = formState();
        document.querySelectorAll("a[href='/do/admin/networkweather?refreshChoices=true']").forEach(function (link) {
            link.addEventListener("click", function (event) {
                if (!document.getElementById("nwRequestError") && formState() === initialFormState) return;
                event.preventDefault();
                root.confirmationDialog({
                    title: "Refresh choices?",
                    message: "Refreshing choices will discard your unsaved changes on this page, including Manual scope, "
                        + "Automatic selection and Probes.",
                    confirmText: "Discard changes and refresh",
                    allowHtml: false,
                    onConfirm: function () { root.location.href = link.href; }
                });
            });
        });
        var tabs = document.getElementById("nwTabs");
        if (!tabs) return;
        tabs.addEventListener("shown.bs.tab", function (event) {
            try { sessionStorage.setItem("nwAdminTab", event.target.id); } catch (error) { /* not remembered */ }
            tables.forEach(function (table) { table.columns.adjust(); });
        });
        var saved;
        try { saved = sessionStorage.getItem("nwAdminTab"); } catch (error) { saved = null; }
        var tab = saved && document.getElementById(saved);
        if (tab && tabs.contains(tab) && root.bootstrap) root.bootstrap.Tab.getOrCreateInstance(tab).show();
    }

    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", start);
    else start();
})(typeof window === "undefined" ? globalThis : window);
