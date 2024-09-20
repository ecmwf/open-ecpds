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
 * The Class HostECUser.
 */
public class HostECUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5380527049792490854L;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The hos name. */
    protected int HOS_NAME;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The host. */
    protected Host host;

    /**
     * Instantiates a new host ec user.
     */
    public HostECUser() {
    }

    /**
     * Instantiates a new host ec user.
     *
     * @param ecuserName
     *            the ecuser name
     * @param hostName
     *            the host name
     */
    public HostECUser(final String ecuserName, final String hostName) {
        setECUserName(ecuserName);
        setHostName(hostName);
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
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(ECU_NAME, HOS_NAME);
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
        final var other = (HostECUser) obj;
        return Objects.equals(ECU_NAME, other.ECU_NAME) && HOS_NAME == other.HOS_NAME;
    }
}
