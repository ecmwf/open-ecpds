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
 * Search parameters for retrieving the last N DataTransfers for a given host,
 * ordered by most recent first, with no date constraint.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class LastDataTransfersByHost.
 */
public class LastDataTransfersByHost extends ModelSearchBase {

    /** The host. */
    private final Host host;

    /** The limit. */
    private final int limit;

    /**
     * Instantiates a new last data transfers by host.
     *
     * @param host
     *            the host
     * @param limit
     *            maximum number of rows to return
     */
    public LastDataTransfersByHost(final Host host, final int limit) {
        this.host = host;
        this.limit = limit;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public Host getHost() {
        return host;
    }

    /**
     * Gets the limit.
     *
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }
}
