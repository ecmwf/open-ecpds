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

import java.util.Objects;

/**
 * The Class Category.
 */
public class Category extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6623313938017757527L;

    /** The cat active. */
    protected boolean CAT_ACTIVE;

    /** The cat description. */
    protected String CAT_DESCRIPTION;

    /** The cat id. */
    protected long CAT_ID;

    /** The cat name. */
    protected String CAT_NAME;

    /**
     * Instantiates a new category.
     */
    public Category() {
    }

    /**
     * Instantiates a new category.
     *
     * @param id
     *            the id
     */
    public Category(final long id) {
        setId(id);
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return CAT_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        CAT_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        CAT_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return CAT_DESCRIPTION;
    }

    /**
     * Sets the description.
     *
     * @param param
     *            the new description
     */
    public void setDescription(final String param) {
        CAT_DESCRIPTION = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return CAT_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        CAT_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        CAT_ID = Long.parseLong(param);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return CAT_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        CAT_NAME = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(CAT_ID);
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
        final var other = (Category) obj;
        return CAT_ID == other.CAT_ID;
    }
}
