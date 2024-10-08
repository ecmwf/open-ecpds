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
import java.util.Objects;

/**
 * The Class ChangeLog.
 */
public class ChangeLog extends DataBaseObject {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5217175943280411688L;

    /** The chl id. */
    protected long CHL_ID;

    /** The weu id. */
    protected String WEU_ID;

    /** The chl table name. */
    protected String CHL_KEY_NAME;

    /** The chl key. */
    protected String CHL_KEY_VALUE;

    /** The chl time. */
    protected BigDecimal CHL_TIME;

    /** The chl old object. */
    protected String CHL_OLD_OBJECT;

    /** The chl new object. */
    protected String CHL_NEW_OBJECT;

    /**
     * Instantiates a new ChangeLog.
     */
    public ChangeLog() {
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return CHL_ID;
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    public void setId(final long id) {
        CHL_ID = id;
    }

    /**
     * Gets the web user id.
     *
     * @return the web user id
     */
    public String getWebUserId() {
        return WEU_ID;
    }

    /**
     * Sets the web user id.
     *
     * @param id
     *            the new web user id
     */
    public void setWebUserId(final String id) {
        WEU_ID = id;
    }

    /**
     * Gets the key name.
     *
     * @return the key name
     */
    public String getKeyName() {
        return CHL_KEY_NAME;
    }

    /**
     * Sets the key name.
     *
     * @param keyName
     *            the new key name
     */
    public void setKeyName(final String keyName) {
        CHL_KEY_NAME = keyName;
    }

    /**
     * Gets the key value.
     *
     * @return the key value
     */
    public String getKeyValue() {
        return CHL_KEY_VALUE;
    }

    /**
     * Sets the key value.
     *
     * @param keyValue
     *            the new key value
     */
    public void setKeyValue(final String keyValue) {
        CHL_KEY_VALUE = keyValue;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public java.sql.Timestamp getTime() {
        return bigDecimalToTimestamp(CHL_TIME);
    }

    /**
     * Sets the time.
     *
     * @param time
     *            the new time
     */
    public void setTime(final java.sql.Timestamp time) {
        CHL_TIME = timestampToBigDecimal(time);
    }

    /**
     * Gets the old object.
     *
     * @return the old object
     */
    public String getOldObject() {
        return CHL_OLD_OBJECT;
    }

    /**
     * Sets the old object.
     *
     * @param oldObject
     *            the new old object
     */
    public void setOldObject(final String oldObject) {
        CHL_OLD_OBJECT = oldObject;
    }

    /**
     * Gets the new object.
     *
     * @return the new object
     */
    public String getNewObject() {
        return CHL_NEW_OBJECT;
    }

    /**
     * Sets the new object.
     *
     * @param newObject
     *            the new new object
     */
    public void setNewObject(final String newObject) {
        CHL_NEW_OBJECT = newObject;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(CHL_ID);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (ChangeLog) obj;
        return Objects.equals(CHL_ID, other.CHL_ID);
    }
}
