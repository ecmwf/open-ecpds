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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

import ecmwf.common.text.Format;

/**
 * The Class ECUser.
 */
public class ECUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 500275739755464953L;

    /** The _ecu comment. */
    @SuppressWarnings("unused")
    private String _ECU_COMMENT;

    /** The _ecu dir. */
    @SuppressWarnings("unused")
    private String _ECU_DIR;

    /** The _ecu gid. */
    @SuppressWarnings("unused")
    private long _ECU_GID;

    /** The _ecu name. */
    @SuppressWarnings("unused")
    private String _ECU_NAME;

    /** The _ecu shell. */
    @SuppressWarnings("unused")
    private String _ECU_SHELL;

    /** The _ecu uid. */
    @SuppressWarnings("unused")
    private long _ECU_UID;

    /** The _ecu comment. */
    protected String ECU_COMMENT;

    /** The _ecu dir. */
    protected String ECU_DIR;

    /** The _ecu gid. */
    protected long ECU_GID;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The ecu shell. */
    protected String ECU_SHELL;

    /** The ecu uid. */
    protected long ECU_UID;

    /**
     * Instantiates a new EC user.
     */
    public ECUser() {
    }

    /**
     * Instantiates a new EC user.
     *
     * @param name
     *            the name
     */
    public ECUser(final String name) {
        setName(name);
    }

    /**
     * This is for backward compatibility with old ECaccess Gateways.
     *
     * @param s
     *            the s
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void writeObject(final ObjectOutputStream s) throws IOException {
        _ECU_COMMENT = getComment();
        _ECU_DIR = getDir();
        _ECU_GID = getGid();
        _ECU_NAME = getName();
        _ECU_SHELL = getShell();
        _ECU_UID = getUid();
        s.defaultWriteObject();
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return ECU_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        ECU_COMMENT = Format.trimString(param, 1024);
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    public String getDir() {
        return ECU_DIR;
    }

    /**
     * Sets the dir.
     *
     * @param param
     *            the new dir
     */
    public void setDir(final String param) {
        ECU_DIR = param;
    }

    /**
     * Gets the gid.
     *
     * @return the gid
     */
    public long getGid() {
        return ECU_GID;
    }

    /**
     * Sets the gid.
     *
     * @param param
     *            the new gid
     */
    public void setGid(final long param) {
        ECU_GID = param;
    }

    /**
     * Sets the gid.
     *
     * @param param
     *            the new gid
     */
    public void setGid(final String param) {
        ECU_GID = Long.parseLong(param);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return ECU_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        ECU_NAME = param;
    }

    /**
     * Gets the shell.
     *
     * @return the shell
     */
    public String getShell() {
        return ECU_SHELL;
    }

    /**
     * Sets the shell.
     *
     * @param param
     *            the new shell
     */
    public void setShell(final String param) {
        ECU_SHELL = param;
    }

    /**
     * Gets the uid.
     *
     * @return the uid
     */
    public long getUid() {
        return ECU_UID;
    }

    /**
     * Sets the uid.
     *
     * @param param
     *            the new uid
     */
    public void setUid(final long param) {
        ECU_UID = param;
    }

    /**
     * Sets the uid.
     *
     * @param param
     *            the new uid
     */
    public void setUid(final String param) {
        ECU_UID = Long.parseLong(param);
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(ECU_UID);
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
        final var other = (ECUser) obj;
        return ECU_UID == other.ECU_UID;
    }
}
