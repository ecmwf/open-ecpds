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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Class IncomingUser.
 */
public class IncomingUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3032023062243081251L;

    /** The inu id. */
    protected String INU_ID;

    /** The inu password. */
    protected String INU_PASSWORD;

    /** The inu comment. */
    protected String INU_COMMENT;

    /** The inu active. */
    protected boolean INU_ACTIVE;

    /** The inu last login. */
    protected BigDecimal INU_LAST_LOGIN;

    /** The inu last login host. */
    protected String INU_LAST_LOGIN_HOST;

    /** The inu data. */
    protected String INU_DATA;

    /** The inu authorized keys. */
    protected String INU_AUTHORIZED_KEYS;

    /** The cou iso. */
    protected String COU_ISO;

    /** The inu synchronized. */
    protected boolean INU_SYNCHRONIZED;

    /** The incoming list of connections. */
    protected List<IncomingConnection> connectionsList = new ArrayList<>();

    /**
     * Instantiates a new incoming user.
     */
    public IncomingUser() {
    }

    /**
     * Gets the iso.
     *
     * @return the iso
     */
    public String getIso() {
        return COU_ISO;
    }

    /**
     * Sets the iso.
     *
     * @param param
     *            the new iso
     */
    public void setIso(final String param) {
        COU_ISO = param;
    }

    /**
     * Instantiates a new incoming user.
     *
     * @param id
     *            the id
     */
    public IncomingUser(final String id) {
        setId(id);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return INU_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        INU_ID = param;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return INU_PASSWORD;
    }

    /**
     * Sets the password.
     *
     * @param param
     *            the new password
     */
    public void setPassword(final String param) {
        INU_PASSWORD = param;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return INU_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        INU_COMMENT = param;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return INU_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        INU_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        INU_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the synchronized.
     *
     * @return the synchronized
     */
    public boolean getSynchronized() {
        return INU_SYNCHRONIZED;
    }

    /**
     * Sets the synchronized.
     *
     * @param param
     *            the new synchronized
     */
    public void setSynchronized(final boolean param) {
        INU_SYNCHRONIZED = param;
    }

    /**
     * Sets the synchronized.
     *
     * @param param
     *            the new synchronized
     */
    public void setSynchronized(final String param) {
        INU_SYNCHRONIZED = Boolean.parseBoolean(param);
    }

    /**
     * Gets the last login.
     *
     * @return the last login
     */
    public java.sql.Timestamp getLastLogin() {
        return bigDecimalToTimestamp(INU_LAST_LOGIN);
    }

    /**
     * Sets the last login.
     *
     * @param param
     *            the new last login
     */
    public void setLastLogin(final java.sql.Timestamp param) {
        INU_LAST_LOGIN = timestampToBigDecimal(param);
    }

    /**
     * Gets the last login host.
     *
     * @return the last login host
     */
    public String getLastLoginHost() {
        return INU_LAST_LOGIN_HOST;
    }

    /**
     * Sets the last login host.
     *
     * @param param
     *            the new last login host
     */
    public void setLastLoginHost(final String param) {
        INU_LAST_LOGIN_HOST = param;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return INU_DATA;
    }

    /**
     * Sets the data.
     *
     * @param param
     *            the new data
     */
    public void setData(final String param) {
        INU_DATA = param;
    }

    /**
     * Gets the authorized SSH keys.
     *
     * @return the authorized SSH keys
     */
    public String getAuthorizedSSHKeys() {
        return INU_AUTHORIZED_KEYS;
    }

    /**
     * Sets the authorized SSH keys.
     *
     * @param param
     *            the new authorized SSH keys
     */
    public void setAuthorizedSSHKeys(final String param) {
        INU_AUTHORIZED_KEYS = param;
    }

    /**
     * Add the new connections to the list.
     *
     * @param param
     *            the new connections
     */
    public void addConnections(final ArrayList<IncomingConnection> param) {
        if (param != null) {
            connectionsList.addAll(param);
        }
    }

    /**
     * The list of connections.
     *
     * @return the list of connections
     */
    public List<IncomingConnection> getConnections() {
        return connectionsList;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(INU_ID);
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
        final var other = (IncomingUser) obj;
        return Objects.equals(INU_ID, other.INU_ID);
    }
}
