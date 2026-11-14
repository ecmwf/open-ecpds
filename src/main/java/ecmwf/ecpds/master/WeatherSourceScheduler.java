/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.master;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.timer.Timer;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.mover.MoverInterface;

final class WeatherSourceScheduler extends MBeanScheduler {
    private final MasterServer _master;

    private final ecmwf.ecpds.master.weather.WeatherSourceProbe probe = new ecmwf.ecpds.master.weather.WeatherSourceProbe(
            (callback, challenge) -> {
                if (callback instanceof ecmwf.ecpds.master.weather.WeatherRelayRegistration route) {
                    return route.identify(challenge);
                }
                return ((MoverInterface) callback).getWeatherSourceIdentity(challenge);
            });
    private volatile Map<String, ecmwf.ecpds.master.weather.WeatherRelayRegistration> routes = Map.of();
    private long refreshed;
    private Set<String> lastSources = Set.of();

    Object registration(String source) {
        final var direct = _master.getClientInterface("DataMover/" + source, MoverInterface.class);
        if (direct != null) {
            return direct;
        }
        final var route = routes.get(source);
        if (route == null || !route.hostId().equals(Cnf.at("NetworkWeatherRelay", source, ""))
                || System.nanoTime() - refreshed < 0 || System.nanoTime() - refreshed >= 60_000_000_000L
                || !route.relay()
                        .equals(_master.getClientInterface("DataMover/" + route.relayId(), MoverInterface.class))) {
            return null;
        }
        return route;
    }

    private void refreshRoutes(Set<String> selected) throws Exception {
        final long now = System.nanoTime();
        if (selected.equals(lastSources) && now - refreshed >= 0 && now - refreshed < 30_000_000_000L) {
            return;
        }
        // Expire the previous topology before SQL: failure must not keep an old approval alive.
        routes = Map.of();
        refreshed = now;
        lastSources = Set.copyOf(selected);
        final Map<String, ecmwf.ecpds.master.weather.WeatherRelayRegistration> next = new HashMap<>();
        for (final var source : selected) {
            final var hostId = Cnf.at("NetworkWeatherRelay", source, "");
            if (hostId.isEmpty() || _master.getClientInterface("DataMover/" + source, MoverInterface.class) != null) {
                continue;
            }
            try {
                final var host = _master.getECpdsBase().getHost(hostId);
                if (host == null || !host.getActive() || !HostOption.PROXY.equals(host.getType())) {
                    throw new IOException("Network Weather relay mapping requires an active Proxy Host");
                }
                for (final var server : _master.getECpdsBase().getTransferServers(host.getTransferGroupName())) {
                    final var callback = _master.getClientInterface("DataMover/" + server.getName(),
                            MoverInterface.class);
                    if (callback != null) {
                        next.put(source, new ecmwf.ecpds.master.weather.WeatherRelayRegistration(source, host,
                                server.getName(), callback));
                        break;
                    }
                }
            } catch (final Exception e) {
                MasterServer._log.warn("Network Weather approved route unavailable for source {}", source, e);
            }
        }
        routes = Map.copyOf(next);
    }

    WeatherSourceScheduler(final MasterServer _master) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super("WeatherSourceScheduler");
        setPriority(Thread.MIN_PRIORITY);
        setDelay(Math.max(1, Math.min(2 * Timer.ONE_SECOND,
                40_000 / ecmwf.ecpds.master.weather.WeatherLimits.configured().maxSources())));
        setJammedTimeout(5 * Timer.ONE_MINUTE);
    }

    @Override
    public int nextStep() {
        try {
            final var selected = _master.weatherAdministration.selectedSources();
            refreshRoutes(selected);
            final Map<String, Object> callbacks = new HashMap<>();
            for (final var source : selected) {
                final var callback = registration(source);
                if (callback != null) {
                    callbacks.put(source, callback);
                }
            }
            _master.weatherAdministration.sources().poll(selected, callbacks, probe);
        } catch (final Exception e) {
            MasterServer._log.warn("Network Weather source verification failed; that source's evidence is not admitted",
                    e);
        }
        return NEXT_STEP_DELAY;
    }

    @Override
    public void shutdown() {
        probe.close();
        super.shutdown();
    }
}
