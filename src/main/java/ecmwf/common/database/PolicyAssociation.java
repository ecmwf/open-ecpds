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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class PolicyAssociation.
 */
public class PolicyAssociation extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1798750795300979693L;

    /** The des name. */
    protected String DES_NAME;

    /** The inp id. */
    protected String INP_ID;

    /** The incoming policy. */
    protected IncomingPolicy incomingPolicy;

    /** The destination. */
    protected Destination destination;

    /**
     * Instantiates a new policy association.
     */
    public PolicyAssociation() {
    }

    /**
     * Instantiates a new policy association.
     *
     * @param destinationName
     *            the destination name
     * @param incomingPolicyId
     *            the incoming policy id
     */
    public PolicyAssociation(final String destinationName, final String incomingPolicyId) {
        setDestinationName(destinationName);
        setIncomingPolicyiD(incomingPolicyId);
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
     * Gets the incoming policy id.
     *
     * @return the incoming policy id
     */
    public String getIncomingPolicyId() {
        return INP_ID;
    }

    /**
     * Sets the incoming policy id.
     *
     * @param param
     *            the incoming policy id
     */
    public void setIncomingPolicyiD(final String param) {
        INP_ID = param;
    }

    /**
     * Gets the incoming policy.
     *
     * @return the incoming policy
     */
    public IncomingPolicy getIncomingPolicy() {
        return incomingPolicy;
    }

    /**
     * Sets the incoming policy.
     *
     * @param param
     *            the new incoming policy
     */
    public void setIncomingPolicy(final IncomingPolicy param) {
        incomingPolicy = param;
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
        return Objects.hash(DES_NAME, INP_ID);
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
        final var other = (PolicyAssociation) obj;
        return Objects.equals(DES_NAME, other.DES_NAME) && Objects.equals(INP_ID, other.INP_ID);
    }
}
