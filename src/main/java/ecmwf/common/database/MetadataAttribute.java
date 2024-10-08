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
 * The Class MetadataAttribute.
 */
public class MetadataAttribute extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5335274473734503963L;

    /** The mea name. */
    protected String MEA_NAME;

    /**
     * Instantiates a new metadata attribute.
     */
    public MetadataAttribute() {
    }

    /**
     * Instantiates a new metadata attribute.
     *
     * @param name
     *            the name
     */
    public MetadataAttribute(final String name) {
        setName(name);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return MEA_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        MEA_NAME = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(MEA_NAME);
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
        final var other = (MetadataAttribute) obj;
        return Objects.equals(MEA_NAME, other.MEA_NAME);
    }
}
