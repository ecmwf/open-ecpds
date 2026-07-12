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
 * Destination metadata value.
 */

import java.sql.Timestamp;
import java.util.Objects;

/**
 * The Class DestinationMetaValue.
 */
public class DestinationMetaValue extends DataBaseObject {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The dmv id. */
    protected Long DMV_ID;

    /** The des name. */
    protected String DES_NAME;

    /** The dmf id. */
    protected Integer DMF_ID;

    /** The dmv value. */
    protected String DMV_VALUE;

    /** The dmv position. */
    protected int DMV_POSITION = 0;

    /** The dmv updated. */
    protected Timestamp DMV_UPDATED;

    /** The dmv by. */
    protected String DMV_BY;

    /** The destination meta field. */
    protected DestinationMetaField destinationMetaField;

    /**
     * Instantiates a new destination meta value.
     */
    public DestinationMetaValue() {
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return DMV_ID;
    }

    /**
     * Sets the id.
     *
     * @param v
     *            the new id
     */
    public void setId(final long v) {
        DMV_ID = v;
    }

    /**
     * Sets the id.
     *
     * @param v
     *            the new id
     */
    public void setId(final String v) {
        DMV_ID = Long.parseLong(v);
    }

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return DES_NAME;
    }

    /**
     * Sets the destination name.
     *
     * @param v
     *            the new destination name
     */
    public void setDestinationName(final String v) {
        DES_NAME = v;
    }

    /**
     * Gets the field id.
     *
     * @return the field id
     */
    public int getFieldId() {
        return DMF_ID;
    }

    /**
     * Sets the field id.
     *
     * @param v
     *            the new field id
     */
    public void setFieldId(final int v) {
        DMF_ID = v;
    }

    /**
     * Sets the field id.
     *
     * @param v
     *            the new field id
     */
    public void setFieldId(final String v) {
        DMF_ID = Integer.parseInt(v);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return DMV_VALUE;
    }

    /**
     * Sets the value.
     *
     * @param v
     *            the new value
     */
    public void setValue(final String v) {
        DMV_VALUE = v;
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public int getPosition() {
        return DMV_POSITION;
    }

    /**
     * Sets the position.
     *
     * @param v
     *            the new position
     */
    public void setPosition(final int v) {
        DMV_POSITION = v;
    }

    /**
     * Sets the position.
     *
     * @param v
     *            the new position
     */
    public void setPosition(final String v) {
        DMV_POSITION = Integer.parseInt(v);
    }

    /**
     * Gets the updated.
     *
     * @return the updated
     */
    public Timestamp getUpdated() {
        return DMV_UPDATED;
    }

    /**
     * Sets the updated.
     *
     * @param v
     *            the new updated
     */
    public void setUpdated(final Timestamp v) {
        DMV_UPDATED = v;
    }

    /**
     * Gets the by.
     *
     * @return the by
     */
    public String getBy() {
        return DMV_BY;
    }

    /**
     * Sets the by.
     *
     * @param v
     *            the new by
     */
    public void setBy(final String v) {
        DMV_BY = v;
    }

    /**
     * Gets the destination meta field.
     *
     * @return the destination meta field
     */
    public DestinationMetaField getDestinationMetaField() {
        return destinationMetaField;
    }

    /**
     * Sets the destination meta field.
     *
     * @param v
     *            the new destination meta field
     */
    public void setDestinationMetaField(final DestinationMetaField v) {
        destinationMetaField = v;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(DMV_ID);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(DMV_ID, ((DestinationMetaValue) o).DMV_ID);
    }
}
