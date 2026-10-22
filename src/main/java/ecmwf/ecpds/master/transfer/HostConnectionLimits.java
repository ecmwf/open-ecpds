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

package ecmwf.ecpds.master.transfer;

import java.util.HashMap;
import java.util.Map;

import ecmwf.common.database.Host;
import ecmwf.common.ectrans.ECtransGroups.Module;
import ecmwf.common.ectrans.ECtransOptions;

/**
 * Master-local reservations for scheduled acquisition and dissemination transfers.
 */
public final class HostConnectionLimits {
    private final Map<String, Map<Lease, String>> reservations = new HashMap<>();

    public synchronized Map<String, Object> status(final Host host, final String destination) {
        final var active = reservations.get(host.getName());
        final Map<String, Object> status = new HashMap<>();
        status.put("hostName", host.getName());
        status.put("type", "acquisition");
        status.put("activeConnections",
                active == null ? 0 : active.values().stream().filter(destination::equals).count());
        status.put("totalConnections", active == null ? 0 : active.size());
        status.put("maxConnections", host.getMaxConnections());
        status.put("sharedConnectionLimit", Module.HOST_ECTRANS.getECtransSetup(host.getData())
                .getBoolean(ECtransOptions.HOST_ECTRANS_SHARED_CONNECTION_LIMIT));
        return status;
    }

    public synchronized Lease acquire(final Host host, final String destination) {
        final var active = reservations.get(host.getName());
        final var shared = Module.HOST_ECTRANS.getECtransSetup(host.getData())
                .getBoolean(ECtransOptions.HOST_ECTRANS_SHARED_CONNECTION_LIMIT);
        final var count = active == null ? 0
                : shared ? active.size() : active.values().stream().filter(destination::equals).count();
        if (host.getMaxConnections() >= 0 && count >= host.getMaxConnections()) {
            return null;
        }
        final var lease = new Lease(host.getName());
        reservations.computeIfAbsent(host.getName(), ignored -> new HashMap<>()).put(lease, destination);
        return lease;
    }

    public final class Lease implements AutoCloseable {
        private final String host;

        private Lease(final String host) {
            this.host = host;
        }

        @Override
        public void close() {
            synchronized (HostConnectionLimits.this) {
                final var active = reservations.get(host);
                if (active != null) {
                    active.remove(this);
                    if (active.isEmpty()) {
                        reservations.remove(host);
                    }
                }
            }
        }
    }
}
