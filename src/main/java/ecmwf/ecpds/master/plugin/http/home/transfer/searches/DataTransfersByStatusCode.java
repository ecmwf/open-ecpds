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
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DataTransfersByStatusCode.
 */
public class DataTransfersByStatusCode extends ModelSearchBase {

    /** The status code. */
    private final String statusCode;

    /** The search. */
    private final String search;

    /** The date. */
    private final Date date;

    /** The cursor. */
    private final DataBaseCursor cursor;

    /** The type. */
    private final String type;

    /**
     * Instantiates a new data transfers by status code.
     *
     * @param s
     *            the s
     * @param d
     *            the d
     * @param search
     *            the search
     * @param type
     *            the type
     * @param cursor
     *            the cursor
     */
    public DataTransfersByStatusCode(final String s, final Date d, final String search, final String type,
            final DataBaseCursor cursor) {
        this.statusCode = s;
        this.date = d;
        this.cursor = cursor;
        this.type = type;
        if (search != null && !"".equals(search.trim())) {
            this.search = search;
        } else {
            this.search = null;
        }
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
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
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
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public String getStatusCode() {
        return statusCode;
    }
}
