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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 7.4.0
 * @since 2026-09-30
 */

import java.io.Serializable;

/**
 * One enabled Proxy-type Host, for the "Live ECPDS Earth" globe's Continental Mover marker - sent regardless of whether
 * its Continental Mover is currently connected, so every configured one gets a marker (dimmed when not connected, see
 * globe.jsp's upsertMover()), not only the ones currently relaying traffic.
 *
 * @param name
 *            the Proxy Host's own name
 * @param address
 *            the Proxy Host's own address ({@code HOS_HOST}), used to resolve its geolocation
 * @param connected
 *            whether its Continental Mover is currently connected to the MasterServer (see
 *            {@link ManagementInterface#getActiveProxyHostNames()}), resolved via that Host's {@code proxy.root} option
 *            (see {@code ECtransOptions#HOST_PROXY_ROOT})
 */
public record ProxyHostStatus(String name, String address, boolean connected) implements Serializable {

    private static final long serialVersionUID = 1L;
}
