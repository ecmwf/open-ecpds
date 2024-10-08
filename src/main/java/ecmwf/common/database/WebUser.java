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

import java.math.BigDecimal;
import java.util.Objects;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public class WebUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3220826851489855134L;

    /** The weu active. */
    protected boolean WEU_ACTIVE;

    /** The weu environment. */
    protected String WEU_ENVIRONMENT;

    /** The weu id. */
    protected String WEU_ID;

    /** The weu last login. */
    protected BigDecimal WEU_LAST_LOGIN;

    /** The weu last login host. */
    protected String WEU_LAST_LOGIN_HOST;

    /** The weu name. */
    protected String WEU_NAME;

    /** The weu name. */
    protected String WEU_PASSWORD;

    /**
     * Instantiates a new web user.
     */
    public WebUser() {
    }

    /**
     * Instantiates a new web user.
     *
     * @param id
     *            the id
     */
    public WebUser(final String id) {
        setId(id);
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return WEU_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        WEU_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        WEU_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the environment.
     *
     * @return the environment
     */
    public String getEnvironment() {
        return WEU_ENVIRONMENT;
    }

    /**
     * Sets the environment.
     *
     * @param param
     *            the new environment
     */
    public void setEnvironment(final String param) {
        WEU_ENVIRONMENT = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return WEU_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        WEU_ID = param;
    }

    /**
     * Gets the last login.
     *
     * @return the last login
     */
    public java.sql.Timestamp getLastLogin() {
        return bigDecimalToTimestamp(WEU_LAST_LOGIN);
    }

    /**
     * Sets the last login.
     *
     * @param param
     *            the new last login
     */
    public void setLastLogin(final java.sql.Timestamp param) {
        WEU_LAST_LOGIN = timestampToBigDecimal(param);
    }

    /**
     * Gets the last login host.
     *
     * @return the last login host
     */
    public String getLastLoginHost() {
        return WEU_LAST_LOGIN_HOST;
    }

    /**
     * Sets the last login host.
     *
     * @param param
     *            the new last login host
     */
    public void setLastLoginHost(final String param) {
        WEU_LAST_LOGIN_HOST = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return WEU_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        WEU_NAME = param;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return WEU_PASSWORD;
    }

    /**
     * Sets the password.
     *
     * @param param
     *            the new password
     */
    public void setPassword(final String param) {
        WEU_PASSWORD = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(WEU_ID);
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
        final var other = (WebUser) obj;
        return Objects.equals(WEU_ID, other.WEU_ID);
    }
}
