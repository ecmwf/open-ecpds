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
 * The Class Permission.
 */
public class Permission extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8539175370263451104L;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The ope name. */
    protected String OPE_NAME;

    /** The per validity. */
    protected int PER_VALIDITY;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The operation. */
    protected Operation operation;

    /**
     * Instantiates a new permission.
     */
    public Permission() {
    }

    /**
     * Instantiates a new permission.
     *
     * @param ecuserName
     *            the ecuser name
     * @param operationName
     *            the operation name
     */
    public Permission(final String ecuserName, final String operationName) {
        setECUserName(ecuserName);
        setOperationName(operationName);
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
     * Gets the validity.
     *
     * @return the validity
     */
    public int getValidity() {
        return PER_VALIDITY;
    }

    /**
     * Sets the validity.
     *
     * @param param
     *            the new validity
     */
    public void setValidity(final int param) {
        PER_VALIDITY = param;
    }

    /**
     * Sets the validity.
     *
     * @param param
     *            the new validity
     */
    public void setValidity(final String param) {
        PER_VALIDITY = Integer.parseInt(param);
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
        return Objects.hash(ECU_NAME, OPE_NAME);
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
        final var other = (Permission) obj;
        return Objects.equals(ECU_NAME, other.ECU_NAME) && Objects.equals(OPE_NAME, other.OPE_NAME);
    }
}
