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
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public class TransferECUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8786338171671060155L;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The tme name. */
    protected String TME_NAME;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The transfer method. */
    protected TransferMethod transferMethod;

    /**
     * Instantiates a new transfer ec user.
     */
    public TransferECUser() {
    }

    /**
     * Instantiates a new transfer ec user.
     *
     * @param ecuserName
     *            the ecuser name
     * @param transfermethodName
     *            the transfermethod name
     */
    public TransferECUser(final String ecuserName, final String transfermethodName) {
        setECUserName(ecuserName);
        setTransferMethodName(transfermethodName);
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
     * Gets the transfer method name.
     *
     * @return the transfer method name
     */
    public String getTransferMethodName() {
        return TME_NAME;
    }

    /**
     * Sets the transfer method name.
     *
     * @param param
     *            the new transfer method name
     */
    public void setTransferMethodName(final String param) {
        TME_NAME = param;
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
     * Gets the transfer method.
     *
     * @return the transfer method
     */
    public TransferMethod getTransferMethod() {
        return transferMethod;
    }

    /**
     * Sets the transfer method.
     *
     * @param param
     *            the new transfer method
     */
    public void setTransferMethod(final TransferMethod param) {
        transferMethod = param;
    }
}
