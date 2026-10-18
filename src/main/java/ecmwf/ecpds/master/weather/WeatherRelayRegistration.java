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

package ecmwf.ecpds.master.weather;

import java.util.Objects;

import ecmwf.common.database.Host;
import ecmwf.ecpds.mover.MoverInterface;
import ecmwf.ecpds.master.transfer.HostOption;

/** Master-created approved route. Host control data is not exposed through status or generated toString methods. */
public final class WeatherRelayRegistration {
    private final Host host;
    private final String source;
    private final MoverInterface relay;
    private final String relayId;
    private final String hostId;
    private final String target;
    private final String data;
    private final String group;

    public WeatherRelayRegistration(String source, Host host, String relayId, MoverInterface relay) {
        WeatherCapabilities.validateSource(source);
        this.source = source;
        Objects.requireNonNull(host);
        if (!host.getActive() || !HostOption.PROXY.equals(host.getType())) {
            throw new IllegalArgumentException("Network Weather requires an active approved Proxy Host");
        }
        this.relay = Objects.requireNonNull(relay);
        this.relayId = Objects.requireNonNull(relayId);
        hostId = host.getName();
        target = host.getHost();
        data = host.getData();
        group = host.getTransferGroupName();
        this.host = new Host();
        this.host.setName(hostId);
        this.host.setHost(target);
        this.host.setData(data);
        this.host.setTransferGroupName(group);
        this.host.setActive(true);
        this.host.setType(HostOption.PROXY);
    }

    public String relayId() {
        return relayId;
    }

    public String hostId() {
        return hostId;
    }

    public MoverInterface relay() {
        return relay;
    }

    public WeatherSourceIdentity identify(java.util.UUID challenge) throws java.rmi.RemoteException {
        return relay.getWeatherSourceIdentity(host, source, challenge);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof WeatherRelayRegistration route && relay.equals(route.relay)
                && source.equals(route.source) && relayId.equals(route.relayId) && Objects.equals(hostId, route.hostId)
                && Objects.equals(target, route.target) && Objects.equals(data, route.data)
                && Objects.equals(group, route.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, relay, relayId, hostId, target, data, group);
    }
}
