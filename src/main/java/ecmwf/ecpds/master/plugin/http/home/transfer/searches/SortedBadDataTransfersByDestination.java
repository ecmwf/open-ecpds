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
 * Search for bad data transfers by destination with cursor-based server-side
 * pagination (used by the DataTables AJAX endpoint).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class SortedBadDataTransfersByDestination.
 */
public class SortedBadDataTransfersByDestination extends ModelSearchBase {

    /** The destination. */
    private final Destination destination;

    /** The cursor. */
    private final DataBaseCursor cursor;

    /**
     * Instantiates a new sorted bad data transfers by destination.
     *
     * @param dest
     *            the dest
     * @param cursor
     *            the data base cursor
     */
    public SortedBadDataTransfersByDestination(final Destination dest, final DataBaseCursor cursor) {
        this.destination = dest;
        this.cursor = cursor;
        this.setCacheable(false);
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Gets the cursor.
     *
     * @return the cursor
     */
    public DataBaseCursor getCursor() {
        return cursor;
    }
}
