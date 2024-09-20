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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public class AuthorizedECUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6697021026226176263L;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The msu name. */
    protected String MSU_NAME;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The msuser. */
    protected MSUser msuser;

    /**
     * Instantiates a new authorized ec user.
     */
    public AuthorizedECUser() {
    }

    /**
     * Instantiates a new authorized ec user.
     *
     * @param ecuserName
     *            the ecuser name
     * @param msuserName
     *            the msuser name
     */
    public AuthorizedECUser(final String ecuserName, final String msuserName) {
        setECUserName(ecuserName);
        setMSUserName(msuserName);
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
     * Gets the MS user name.
     *
     * @return the MS user name
     */
    public String getMSUserName() {
        return MSU_NAME;
    }

    /**
     * Sets the MS user name.
     *
     * @param param
     *            the new MS user name
     */
    public void setMSUserName(final String param) {
        MSU_NAME = param;
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
     * Gets the MS user.
     *
     * @return the MS user
     */
    public MSUser getMSUser() {
        return msuser;
    }

    /**
     * Sets the MS user.
     *
     * @param param
     *            the new MS user
     */
    public void setMSUser(final MSUser param) {
        msuser = param;
    }
}
