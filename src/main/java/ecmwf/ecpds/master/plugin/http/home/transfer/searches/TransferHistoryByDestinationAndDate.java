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
 * Search object to get TransferHistory for a given destination and date. The
 * date can be the Product Date or the History Date (the date where the history
 * item happened) depending on the search mode.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.web.dao.ModelSearchBase;
import ecmwf.web.model.users.User;

/**
 * The Class TransferHistoryByDestinationAndDate.
 */
public class TransferHistoryByDestinationAndDate extends ModelSearchBase {

    /** The Constant USE_PRODUCT_DATE. */
    public static final int USE_PRODUCT_DATE = 0;

    /** The Constant USE_HISTORY_DATE. */
    public static final int USE_HISTORY_DATE = 1;

    /** The destination. */
    private final String destination;

    /** The date. */
    private final Date date;

    /** The mode. */
    private final int mode;

    /** The user. */
    private final User user;

    /** The cursor. */
    private final DataBaseCursor cursor;

    /**
     * Instantiates a new transfer history by destination and date.
     *
     * @param user
     *            the user
     * @param destination
     *            the destination
     * @param date
     *            the date
     * @param mode
     *            the mode
     * @param cursor
     *            the cursor
     */
    public TransferHistoryByDestinationAndDate(final User user, final String destination, final Date date,
            final int mode, final DataBaseCursor cursor) {
        this.destination = destination;
        this.date = date;
        this.user = user;
        this.mode = mode;
        this.cursor = cursor;
    }

    /**
     * Gets the data base cursor.
     *
     * @return the data base cursor
     */
    public DataBaseCursor getDataBaseCursor() {
        return cursor;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public int getMode() {
        return this.mode;
    }
}
