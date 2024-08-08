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

import java.util.Objects;

/**
 * The Class Alias.
 */
public class Alias extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1411124279599936414L;

    /** The ali des name. */
    protected String ALI_DES_NAME;

    /** The des name. */
    protected String DES_NAME;

    /** The destination. */
    protected Destination destination;

    /**
     * Instantiates a new alias.
     */
    public Alias() {
    }

    /**
     * Instantiates a new alias.
     *
     * @param desName
     *            the des name
     * @param destinationName
     *            the destination name
     */
    public Alias(final String desName, final String destinationName) {
        setDesName(desName);
        setDestinationName(destinationName);
    }

    /**
     * Gets the des name.
     *
     * @return the des name
     */
    public String getDesName() {
        return ALI_DES_NAME;
    }

    /**
     * Sets the des name.
     *
     * @param param
     *            the new des name
     */
    public void setDesName(final String param) {
        ALI_DES_NAME = param;
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
     * @param param
     *            the new destination name
     */
    public void setDestinationName(final String param) {
        DES_NAME = param;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Sets the destination.
     *
     * @param param
     *            the new destination
     */
    public void setDestination(final Destination param) {
        destination = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(ALI_DES_NAME, DES_NAME);
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
        final var other = (Alias) obj;
        return Objects.equals(ALI_DES_NAME, other.ALI_DES_NAME) && Objects.equals(DES_NAME, other.DES_NAME);
    }
}
