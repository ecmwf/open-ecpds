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
 * Destination metadata field definition.
 */

import java.util.Objects;

/**
 * The Class DestinationMetaField.
 */
public class DestinationMetaField extends DataBaseObject {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The dmf id. */
    protected Integer DMF_ID;

    /** The dmf name. */
    protected String DMF_NAME;

    /** The dmf label. */
    protected String DMF_LABEL;

    /** The dmf type. */
    protected String DMF_TYPE;

    /** The dmf category. */
    protected String DMF_CATEGORY;

    /** The dmf tooltip. */
    protected String DMF_TOOLTIP;

    /** The dmf max occurs. */
    protected int DMF_MAX_OCCURS = 1;

    /** The dmf position. */
    protected int DMF_POSITION = 0;

    /** The dmf active. */
    protected boolean DMF_ACTIVE = true;

    /**
     * Instantiates a new destination meta field.
     */
    public DestinationMetaField() {
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return DMF_ID;
    }

    /**
     * Sets the id.
     *
     * @param v
     *            the new id
     */
    public void setId(final int v) {
        DMF_ID = v;
    }

    /**
     * Sets the id.
     *
     * @param v
     *            the new id
     */
    public void setId(final String v) {
        DMF_ID = Integer.parseInt(v);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return DMF_NAME;
    }

    /**
     * Sets the name.
     *
     * @param v
     *            the new name
     */
    public void setName(final String v) {
        DMF_NAME = v;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return DMF_LABEL;
    }

    /**
     * Sets the label.
     *
     * @param v
     *            the new label
     */
    public void setLabel(final String v) {
        DMF_LABEL = v;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return DMF_TYPE;
    }

    /**
     * Sets the type.
     *
     * @param v
     *            the new type
     */
    public void setType(final String v) {
        DMF_TYPE = v;
    }

    /**
     * Gets the category.
     *
     * @return the category
     */
    public String getCategory() {
        return DMF_CATEGORY;
    }

    /**
     * Sets the category.
     *
     * @param v
     *            the new category
     */
    public void setCategory(final String v) {
        DMF_CATEGORY = v;
    }

    /**
     * Gets the tooltip.
     *
     * @return the tooltip
     */
    public String getTooltip() {
        return DMF_TOOLTIP;
    }

    /**
     * Sets the tooltip.
     *
     * @param v
     *            the new tooltip
     */
    public void setTooltip(final String v) {
        DMF_TOOLTIP = v;
    }

    /**
     * Gets the max occurs.
     *
     * @return the max occurs
     */
    public int getMaxOccurs() {
        return DMF_MAX_OCCURS;
    }

    /**
     * Sets the max occurs.
     *
     * @param v
     *            the new max occurs
     */
    public void setMaxOccurs(final int v) {
        DMF_MAX_OCCURS = v;
    }

    /**
     * Sets the max occurs.
     *
     * @param v
     *            the new max occurs
     */
    public void setMaxOccurs(final String v) {
        DMF_MAX_OCCURS = Integer.parseInt(v);
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public int getPosition() {
        return DMF_POSITION;
    }

    /**
     * Sets the position.
     *
     * @param v
     *            the new position
     */
    public void setPosition(final int v) {
        DMF_POSITION = v;
    }

    /**
     * Sets the position.
     *
     * @param v
     *            the new position
     */
    public void setPosition(final String v) {
        DMF_POSITION = Integer.parseInt(v);
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return DMF_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param v
     *            the new active
     */
    public void setActive(final boolean v) {
        DMF_ACTIVE = v;
    }

    /**
     * Sets the active.
     *
     * @param v
     *            the new active
     */
    public void setActive(final String v) {
        DMF_ACTIVE = "1".equals(v) || "true".equalsIgnoreCase(v);
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(DMF_ID);
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
        return Objects.equals(DMF_ID, ((DestinationMetaField) o).DMF_ID);
    }
}
