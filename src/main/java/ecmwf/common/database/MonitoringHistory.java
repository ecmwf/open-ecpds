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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * The Class MonitoringHistory.
 */
public class MonitoringHistory extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2855145235386463041L;

    /** The moh key. */
    protected String MOH_KEY;

    /** The moh time. */
    protected BigDecimal MOH_TIME;

    /** The moh metadata. */
    protected String MOH_METADATA;

    /** The moh object. */
    protected byte[] MOH_OBJECT;

    /**
     * Instantiates a new monitoring history.
     */
    public MonitoringHistory() {
    }

    /**
     * Instantiates a new monitoring history.
     *
     * @param time
     *            the time
     * @param key
     *            the key
     */
    public MonitoringHistory(final Timestamp time, final String key) {
        MOH_KEY = key;
        MOH_TIME = timestampToBigDecimal(time);
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return MOH_KEY;
    }

    /**
     * Sets the key.
     *
     * @param param
     *            the new key
     */
    public void setKey(final String param) {
        MOH_KEY = param;
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public String getMetadata() {
        return MOH_METADATA;
    }

    /**
     * Sets the metadata.
     *
     * @param param
     *            the new metadata
     */
    public void setMetadata(final String param) {
        MOH_METADATA = param;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public java.sql.Timestamp getTime() {
        return bigDecimalToTimestamp(MOH_TIME);
    }

    /**
     * Sets the time.
     *
     * @param param
     *            the new time
     */
    public void setTime(final java.sql.Timestamp param) {
        MOH_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the object.
     *
     * @return the object
     */
    public byte[] getObject() {
        return MOH_OBJECT;
    }

    /**
     * Sets the object.
     *
     * @param param
     *            the new object
     */
    public void setObject(final byte[] param) {
        MOH_OBJECT = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(MOH_KEY, MOH_TIME);
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
        final var other = (MonitoringHistory) obj;
        return Objects.equals(MOH_KEY, other.MOH_KEY) && Objects.equals(MOH_TIME, other.MOH_TIME);
    }
}
