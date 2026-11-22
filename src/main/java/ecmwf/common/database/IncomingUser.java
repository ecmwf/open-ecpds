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
    protected transient String INU_PASSWORD;
    protected transient String INU_PASSWORD_HASH;
    protected transient String INU_S3_SECRET;

    private String newPassword;
    private String newS3Secret;
    private boolean disableS3Secret;
    private boolean passwordSet;
    private String s3CredentialStatus = "Not set";

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

    /** The inu portal service. */
    protected String INU_PORTAL_SERVICE = "standard-login";

    /** The inu verify token. */

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
        return newPassword;
    }

    /**
     * Sets the password.
     *
     * @param param
     *            the new password
     */
    public void setPassword(final String param) {
        newPassword = param;
    }

    public String legacyPassword() {
        return INU_PASSWORD;
    }

    public String passwordHash() {
        return INU_PASSWORD_HASH;
    }

    public String encryptedS3Secret() {
        return INU_S3_SECRET;
    }

    public void setNewS3Secret(final String secret) {
        newS3Secret = secret;
    }

    String newS3Secret() {
        return newS3Secret;
    }

    public void setDisableS3Secret(final boolean disable) {
        disableS3Secret = disable;
    }

    boolean disableS3Secret() {
        return disableS3Secret;
    }

    public boolean hasPassword() {
        return passwordSet || INU_PASSWORD_HASH != null || INU_PASSWORD != null && !INU_PASSWORD.isEmpty();
    }

    public String s3CredentialStatus() {
        return s3CredentialStatus;
    }

    public IncomingUser withoutCredentials() {
        final var copy = (IncomingUser) clone();
        copy.passwordSet = hasPassword();
        if ("".equals(INU_S3_SECRET)) {
            copy.s3CredentialStatus = "Disabled";
        } else if (INU_S3_SECRET != null) {
            copy.s3CredentialStatus = "Separate secret set";
        } else if (INU_PASSWORD != null && !INU_PASSWORD.isEmpty()) {
            copy.s3CredentialStatus = "Legacy secret retained";
        }
        copy.INU_PASSWORD = null;
        copy.INU_PASSWORD_HASH = null;
        copy.INU_S3_SECRET = null;
        copy.newPassword = null;
        copy.newS3Secret = null;
        copy.disableS3Secret = false;
        return copy;
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
     * Gets the portal service.
     *
     * @return the portal service (e.g. "standard-login", "open-access", "self-service")
     */
    public String getPortalService() {
        return INU_PORTAL_SERVICE != null ? INU_PORTAL_SERVICE : "standard-login";
    }

    /**
     * Sets the portal service.
     *
     * @param param
     *            the new portal service
     */
    public void setPortalService(final String param) {
        INU_PORTAL_SERVICE = param;
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
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(INU_ID);
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
        final var other = (IncomingUser) obj;
        return Objects.equals(INU_ID, other.INU_ID);
    }
}
