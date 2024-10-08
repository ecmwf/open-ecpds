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
 * The Class DestinationECUser.
 */
public class DestinationECUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8967325365898843928L;

    /** The des name. */
    protected String DES_NAME;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The destination. */
    protected Destination destination;

    /**
     * Instantiates a new destination ec user.
     */
    public DestinationECUser() {
    }

    /**
     * Instantiates a new destination ec user.
     *
     * @param destinationName
     *            the destination name
     * @param ecuserName
     *            the ecuser name
     */
    public DestinationECUser(final String destinationName, final String ecuserName) {
        setDestinationName(destinationName);
        setECUserName(ecuserName);
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
     * Gets the EC user name.
     *
     * @return the EC user name
     */
    public String getECUserName() {
        return ECU_NAME;
    }

    /**
     * Sets the EC user name.
     *
     * @param param
     *            the new EC user name
     */
    public void setECUserName(final String param) {
        ECU_NAME = param;
    }

    /**
     * Gets the EC user.
     *
     * @return the EC user
     */
    public ECUser getECUser() {
        return ecuser;
    }

    /**
     * Sets the EC user.
     *
     * @param param
     *            the new EC user
     */
    public void setECUser(final ECUser param) {
        ecuser = param;
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
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(DES_NAME, ECU_NAME);
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
        final var other = (DestinationECUser) obj;
        return Objects.equals(DES_NAME, other.DES_NAME) && Objects.equals(ECU_NAME, other.ECU_NAME);
    }
}
