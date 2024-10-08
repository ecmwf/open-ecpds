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
 * The Class IncomingAssociation.
 */
public class IncomingAssociation extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3998249920680519313L;

    /** The inu id. */
    protected String INU_ID;

    /** The des name. */
    protected String DES_NAME;

    /** The incoming user. */
    protected IncomingUser incomingUser;

    /** The destination. */
    protected Destination destination;

    /**
     * Instantiates a new incoming association.
     */
    public IncomingAssociation() {
    }

    /**
     * Instantiates a new incoming association.
     *
     * @param incomingUserId
     *            the incoming user id
     * @param destinationName
     *            the destination name
     */
    public IncomingAssociation(final String incomingUserId, final String destinationName) {
        setIncomingUserId(incomingUserId);
        setDestinationName(destinationName);
    }

    /**
     * Gets the incoming user id.
     *
     * @return the incoming user id
     */
    public String getIncomingUserId() {
        return INU_ID;
    }

    /**
     * Sets the incoming user id.
     *
     * @param param
     *            the new incoming user id
     */
    public void setIncomingUserId(final String param) {
        INU_ID = param;
    }

    /**
     * Gets the incoming user.
     *
     * @return the incoming user
     */
    public IncomingUser getIncomingUser() {
        return incomingUser;
    }

    /**
     * Sets the incoming user.
     *
     * @param param
     *            the new incoming user
     */
    public void setIncomingUser(final IncomingUser param) {
        incomingUser = param;
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
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(DES_NAME, INU_ID);
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
        final var other = (IncomingAssociation) obj;
        return Objects.equals(DES_NAME, other.DES_NAME) && Objects.equals(INU_ID, other.INU_ID);
    }
}
