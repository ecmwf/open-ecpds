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

import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DataTransfersByDestinationAndTransmissionDate.
 */
public class DataTransfersByDestinationAndTransmissionDate extends ModelSearchBase {

    /** The destination. */
    private final Destination destination;

    /** The date. */
    private final Date date;

    /**
     * Instantiates a new data transfers by destination and transmission date.
     *
     * @param dest
     *            the dest
     * @param d
     *            the d
     */
    public DataTransfersByDestinationAndTransmissionDate(final Destination dest, final Date d) {
        this.destination = dest;
        this.date = d;
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
    public Destination getDestination() {
        return destination;
    }
}
