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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.DataBaseCursor;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class HostsSearch.
 */
public class HostsSearch extends ModelSearchBase {

    /** The label. */
    private final String label;

    /** The filter. */
    private final String filter;

    /** The network. */
    private final String network;

    /** The type. */
    private final String type;

    /** The search. */
    private final String search;

    /** The cursor. */
    private final DataBaseCursor cursor;

    /**
     * Instantiates a new hosts search.
     *
     * @param label
     *            the label
     * @param filter
     *            the filter
     * @param network
     *            the network
     * @param type
     *            the type
     * @param search
     *            the search
     * @param cursor
     *            the cursor
     */
    public HostsSearch(final String label, final String filter, final String network, final String type,
            final String search, final DataBaseCursor cursor) {
        this.label = label;
        this.filter = filter;
        this.network = network;
        this.type = type;
        this.search = search;
        this.cursor = cursor;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Gets the network.
     *
     * @return the network
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the search.
     *
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * Gets the data base cursor.
     *
     * @return the data base cursor
     */
    public DataBaseCursor getDataBaseCursor() {
        return this.cursor;
    }
}
