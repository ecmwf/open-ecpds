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
 * The Class IncomingPermission.
 */
public class IncomingPermission extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8539175370263451104L;

    /** The inu id. */
    protected String INU_ID;

    /** The ope name. */
    protected String OPE_NAME;

    /** The incoming user. */
    protected IncomingUser incomingUser;

    /** The operation. */
    protected Operation operation;

    /**
     * Instantiates a new incoming permission.
     *
     * @param incomingUser
     *            the incoming user
     * @param operation
     *            the operation
     */
    public IncomingPermission(final IncomingUser incomingUser, final Operation operation) {
        setIncomingUser(incomingUser);
        setIncomingUserId(incomingUser.getId());
        setOperation(operation);
        setOperationName(operation.getName());
    }

    /**
     * Instantiates a new incoming permission.
     *
     * @param incomingUserId
     *            the incoming user id
     * @param operationName
     *            the operation name
     */
    public IncomingPermission(final String incomingUserId, final String operationName) {
        setIncomingUserId(incomingUserId);
        setOperationName(operationName);
    }

    /**
     * Instantiates a new incoming permission.
     */
    public IncomingPermission() {
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
     * Gets the operation name.
     *
     * @return the operation name
     */
    public String getOperationName() {
        return OPE_NAME;
    }

    /**
     * Sets the operation name.
     *
     * @param param
     *            the new operation name
     */
    public void setOperationName(final String param) {
        OPE_NAME = param;
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
     * Gets the operation.
     *
     * @return the operation
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Sets the operation.
     *
     * @param param
     *            the new operation
     */
    public void setOperation(final Operation param) {
        operation = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(INU_ID, OPE_NAME);
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
        final var other = (IncomingPermission) obj;
        return Objects.equals(INU_ID, other.INU_ID) && Objects.equals(OPE_NAME, other.OPE_NAME);
    }
}
