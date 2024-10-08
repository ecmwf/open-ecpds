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

import java.io.Serializable;
import java.util.List;

/**
 * The Class DestinationList.
 */
public class DestinationList implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The destination. */
    protected Destination destination;

    /** The associations. */
    protected List<Association> associations;

    /**
     * Instantiates a new destination list.
     */
    public DestinationList() {
    }

    /**
     * Instantiates a new destination list.
     *
     * @param destination
     *            the destination
     * @param associations
     *            the associations
     */
    public DestinationList(final Destination destination, final List<Association> associations) {
        this.destination = destination;
        this.associations = associations;
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
     * Gets the associations.
     *
     * @return the associations
     */
    public List<Association> getAssociations() {
        return associations;
    }

    /**
     * Sets the destination.
     *
     * @param destination
     *            the new destination
     */
    public void setDestination(final Destination destination) {
        this.destination = destination;
    }

    /**
     * Sets the associations.
     *
     * @param associations
     *            the new associations
     */
    public void setAssociations(final List<Association> associations) {
        this.associations = associations;
    }
}
