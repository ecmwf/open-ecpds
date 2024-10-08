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

package ecmwf.ecpds.master.plugin.http.home.datafile.searches;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DataFilesByMetaDataAndDate.
 */
public class DataFilesByMetaDataAndDate extends ModelSearchBase {

    /** The name. */
    private final String name;

    /** The value. */
    private final String value;

    /** The date. */
    private final Date date;

    /** The cursor. */
    private final DataBaseCursor cursor;

    /**
     * Instantiates a new data files by meta data and date.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param date
     *            the date
     * @param cursor
     *            the cursor
     */
    public DataFilesByMetaDataAndDate(final String name, final String value, final Date date,
            final DataBaseCursor cursor) {
        this.name = name;
        this.value = value;
        this.date = date;
        this.cursor = cursor;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        return this.date;
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
