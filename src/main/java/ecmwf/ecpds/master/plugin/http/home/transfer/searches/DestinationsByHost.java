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

package ecmwf.ecpds.master.plugin.http.home.transfer.searches;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DestinationsByHost.
 */
public class DestinationsByHost extends ModelSearchBase {

    /** The host. */
    private final Host host;

    /**
     * Instantiates a new destinations by host.
     *
     * @param h
     *            the h
     */
    public DestinationsByHost(final Host h) {
        this.host = h;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public Host getHost() {
        return host;
    }
}
