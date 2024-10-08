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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;

/**
 * The Class Statistics.
 */
public class Statistics extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6609721102924722194L;

    /** The date. */
    protected BigDecimal DATE;

    /** The destination. */
    protected int DESTINATION;

    /** The size. */
    protected long SIZE;

    /**
     * Instantiates a new statistics.
     */
    public Statistics() {
    }

    /**
     * Sets the destination.
     *
     * @param param
     *            the new destination
     */
    public void setDestination(final String param) {
        DESTINATION = Integer.parseInt(param);
    }

    /**
     * Sets the destination.
     *
     * @param param
     *            the new destination
     */
    public void setDestination(final int param) {
        DESTINATION = param;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public int getDestination() {
        return DESTINATION;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return SIZE;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final long param) {
        SIZE = param;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final String param) {
        SIZE = Long.parseLong(param);
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public java.sql.Timestamp getDate() {
        return bigDecimalToTimestamp(DATE);
    }

    /**
     * Sets the date.
     *
     * @param param
     *            the new date
     */
    public void setDate(final java.sql.Timestamp param) {
        DATE = timestampToBigDecimal(param);
    }
}
