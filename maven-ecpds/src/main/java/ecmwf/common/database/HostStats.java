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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Class HostStats.
 */
public class HostStats extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6269434296900284206L;

    /** The hst id. */
    protected int HST_ID;

    /** The connections. */
    protected int HST_CONNECTIONS;

    /** The duration. */
    protected long HST_DURATION;

    /** The sent. */
    protected long HST_SENT;

    /** The valid. */
    protected boolean HST_VALID;

    /** The check time. */
    protected BigDecimal HST_CHECK_TIME;

    /**
     * Instantiates a new host stats.
     */
    public HostStats() {
        setConnections(0);
        setDuration(0);
        setSent(0);
        setValid(false);
        setCheckTime(null);
    }

    /**
     * Instantiates a new host stats.
     *
     * @param id
     *            the id
     */
    public HostStats(final int id) {
        setId(id);
    }

    /**
     * Gets the check time.
     *
     * @return the check time
     */
    public java.sql.Timestamp getCheckTime() {
        return bigDecimalToTimestamp(HST_CHECK_TIME);
    }

    /**
     * Sets the check time.
     *
     * @param param
     *            the new check time
     */
    public void setCheckTime(final java.sql.Timestamp param) {
        HST_CHECK_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the connections.
     *
     * @return the connections
     */
    public int getConnections() {
        return HST_CONNECTIONS;
    }

    /**
     * Sets the connections.
     *
     * @param param
     *            the new connections
     */
    public void setConnections(final int param) {
        HST_CONNECTIONS = param;
    }

    /**
     * Sets the connections.
     *
     * @param param
     *            the new connections
     */
    public void setConnections(final String param) {
        HST_CONNECTIONS = Integer.parseInt(param);
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return HST_DURATION;
    }

    /**
     * Sets the duration.
     *
     * @param param
     *            the new duration
     */
    public void setDuration(final long param) {
        HST_DURATION = param;
    }

    /**
     * Sets the duration.
     *
     * @param param
     *            the new duration
     */
    public void setDuration(final String param) {
        HST_DURATION = Long.parseLong(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return HST_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        HST_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        HST_ID = Integer.parseInt(param);
    }

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    public long getSent() {
        return HST_SENT;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final long param) {
        HST_SENT = param;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final String param) {
        HST_SENT = Long.parseLong(param);
    }

    /**
     * Gets the valid.
     *
     * @return the valid
     */
    public boolean getValid() {
        return HST_VALID;
    }

    /**
     * Sets the valid.
     *
     * @param param
     *            the new valid
     */
    public void setValid(final boolean param) {
        HST_VALID = param;
    }

    /**
     * Sets the valid.
     *
     * @param param
     *            the new valid
     */
    public void setValid(final String param) {
        HST_VALID = Boolean.parseBoolean(param);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(HST_ID);
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (HostStats) obj;
        return HST_ID == other.HST_ID;
    }
}
