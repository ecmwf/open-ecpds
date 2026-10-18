(function (root) {
    "use strict";

    function locations(samples, origin, proxies) {
        var points = new Map();
        function add(lat, lon) {
            if (!Number.isFinite(lat) || !Number.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180) return;
            var key = lat.toFixed(3) + "," + lon.toFixed(3);
            points.set(key, {key: key, lat: lat, lon: lon});
        }
        var active = samples.filter(function (sample) { return sample.status === "ACTIVE"; });
        if (active.length) {
            active.forEach(function (sample) {
                add(sample.hostLat, sample.hostLon);
                if (sample.isProxyHost) add(sample.moverLat, sample.moverLon);
                else if (origin) add(origin.lat, origin.lon);
            });
        } else {
            if (origin) add(origin.lat, origin.lon);
            proxies.forEach(function (proxy) { add(proxy.lat, proxy.lon); });
        }
        return Array.from(points.values());
    }

    function create(options) {
        var running = false, timer = null, generation = 0, previous = null;
        var schedule = options.schedule || setTimeout;
        var clear = options.clear || clearTimeout;
        var random = options.random || Math.random;
        function stop() {
            var wasRunning = running;
            running = false;
            generation++;
            if (timer !== null) clear(timer);
            timer = null;
            if (wasRunning) options.cancel();
            options.changed(false);
        }
        function next() {
            timer = null;
            if (!running) return;
            var points = options.locations();
            if (!options.enabled() || points.length < 2) {
                stop();
                return;
            }
            var available = points.filter(function (point) { return point.key !== previous; });
            var point = available[Math.floor(random() * available.length)];
            previous = point.key;
            var current = generation;
            options.fly(point, function () {
                if (running && generation === current) timer = schedule(next, 2000);
            }, function () {
                if (running && generation === current) stop();
            });
        }
        return {
            start: function () {
                if (running || !options.enabled() || options.locations().length < 2) return;
                running = true;
                generation++;
                previous = null;
                options.changed(true);
                next();
            },
            stop: stop,
            running: function () { return running; }
        };
    }

    root.GlobeTour = {locations: locations, create: create};
})(typeof window === "undefined" ? globalThis : window);
