/* Weather display never owns collection or diagnostic execution. */
(function (root) {
    "use strict";
    root.createNetworkWeather = function (options) {
        var preferred = false, storageError = false;
        var busy = false, closed = false, enabled = false, paths = [], controller;
        var interval = options.interval || 15000;
        var deadline = options.deadline || 8000;
        try { preferred = options.storage.getItem("globeNetworkWeather") === "true"; }
        catch (error) { storageError = true; }
        function showToggle(message) {
            var on = enabled && preferred;
            options.toggle.hidden = !enabled;
            options.toggle.classList.toggle("active", on);
            options.toggle.setAttribute("aria-pressed", String(on));
            options.toggle.title = "Network Weather overlay " + (on ? "on" : "off") + " - " + message;
        }
        showToggle("waiting for collection state");
        function display(message) {
            showToggle(message);
            options.render(enabled && preferred ? paths : [], message +
                (storageError ? "; browser cannot save the overlay preference" : ""));
        }
        async function refresh() {
            if (busy || closed) return;
            busy = true;
            controller = new AbortController();
            var timeout;
            var requestedDetails = preferred;
            try {
                var data = await Promise.race([
                    options.fetch("/do/monitoring/weather" + (requestedDetails ? "" : "?stateOnly=true"),
                        {cache: "no-store", signal: controller.signal})
                        .then(function (response) {
                            if (!response.ok) throw new Error("Weather unavailable");
                            return response.json();
                        }),
                    new Promise(function (resolve, reject) {
                        timeout = setTimeout(function () {
                            controller.abort();
                            reject(new Error("Weather request timed out"));
                        }, deadline);
                    })
                ]);
                if (closed) return;
                if (typeof data.enabled !== "boolean" || !Array.isArray(data.paths) || data.paths.length > 50)
                    throw new Error("Invalid weather response");
                enabled = data.enabled;
                paths = enabled ? data.paths : [];
                display(!enabled ? "Collection disabled or suspended by administrator" : preferred ?
                    "Recent five-minute delivery evidence; not a diagnosis of network faults" : "Weather overlay off");
            } catch (error) {
                if (!closed) {
                    enabled = false;
                    paths = [];
                    display("Weather unavailable; no health conclusion");
                }
            } finally {
                clearTimeout(timeout);
                busy = false;
                if (!closed && enabled && preferred && !requestedDetails) refresh();
            }
        }
        options.toggle.addEventListener("click", function () {
            if (!enabled) return;
            preferred = !preferred;
            try { options.storage.setItem("globeNetworkWeather", String(preferred)); }
            catch (error) { storageError = true; }
            display(preferred ? "Recent five-minute delivery evidence; not a diagnosis of network faults" :
                "Weather overlay off");
            refresh();
        });
        var timer = setInterval(refresh, interval);
        refresh();
        return {
            refresh: refresh,
            close: function () {
                closed = true;
                clearInterval(timer);
                if (controller) controller.abort();
                enabled = false;
                paths = [];
                display("Weather overlay off");
            }
        };
    };
})(typeof window === "undefined" ? globalThis : window);
