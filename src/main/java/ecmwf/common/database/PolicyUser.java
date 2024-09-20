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
 * The Class PolicyUser.
 */
public class PolicyUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1798750795300979693L;

    /** The incoming user id. */
    protected String INU_ID;

    /** The incoming policy id. */
    protected String INP_ID;

    /** The incoming policy. */
    protected IncomingPolicy incomingPolicy;

    /** The user. */
    protected IncomingUser incomingUser;

    /**
     * Instantiates a new policy user.
     */
    public PolicyUser() {
    }

    /**
     * Instantiates a new policy user.
     *
     * @param incomingUserId
     *            the incoming user id
     * @param incomingPolicyId
     *            the incoming policy id
     */
    public PolicyUser(final String incomingUserId, final String incomingPolicyId) {
        setIncomingUserId(incomingUserId);
        setIncomingPolicyiD(incomingPolicyId);
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
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(INP_ID, INU_ID);
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
        final var other = (PolicyUser) obj;
        return Objects.equals(INP_ID, other.INP_ID) && Objects.equals(INU_ID, other.INU_ID);
    }
}
