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
 * The Class Association.
 */
public class Association extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2099416592695979989L;

    /** The aso priority. */
    protected int ASO_PRIORITY;

    /** The des name. */
    protected String DES_NAME;

    /** The hos name. */
    protected int HOS_NAME;

    /** The host. */
    protected Host host;

    /** The destination. */
    protected Destination destination;

    /**
     * Instantiates a new association.
     */
    public Association() {
    }

    /**
     * Instantiates a new association.
     *
     * @param destinationName
     *            the destination name
     * @param hostName
     *            the host name
     */
    public Association(final String destinationName, final String hostName) {
        setDestinationName(destinationName);
        setHostName(hostName);
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return ASO_PRIORITY;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final int param) {
        ASO_PRIORITY = param;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final String param) {
        ASO_PRIORITY = Integer.parseInt(param);
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
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostName() {
        return integerToString(HOS_NAME);
    }

    /**
     * Sets the host name.
     *
     * @param param
     *            the new host name
     */
    public void setHostName(final String param) {
        HOS_NAME = stringToInteger(param);
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public Host getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param param
     *            the new host
     */
    public void setHost(final Host param) {
        host = param;
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
        return Objects.hash(DES_NAME, HOS_NAME);
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
        final var other = (Association) obj;
        return Objects.equals(DES_NAME, other.DES_NAME) && HOS_NAME == other.HOS_NAME;
    }
}
