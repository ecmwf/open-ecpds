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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.ProxySocket;

/**
 * The Class UserSession.
 */
public abstract class UserSession {
    /** The _user. */
    private final String _user;

    /** The _token. */
    private final String _token;

    /**
     * Instantiates a new user session.
     *
     * @param user
     *            the user
     * @param token
     *            the token
     */
    public UserSession(final String user, final String token) {
        _user = user;
        _token = token;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public String getUser() {
        return _user;
    }

    /**
     * Gets the token.
     *
     * @return the token
     */
    public String getToken() {
        return _token;
    }

    /**
     * Gets the default group.
     *
     * @return the default group
     */
    public String getDefaultGroup() {
        return "ecaccess";
    }

    /**
     * Gets the default domain.
     *
     * @return the default domain
     */
    public String getDefaultDomain() {
        return null;
    }

    /**
     * Gets the welcome.
     *
     * @return the welcome
     */
    public String getWelcome() {
        return null;
    }

    /**
     * Gets the setup.
     *
     * @return the setup
     */
    public ECtransSetup getECtransSetup() {
        return null;
    }

    /**
     * Chmod.
     *
     * @param mode
     *            the mode
     * @param path
     *            the path
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void chmod(final int mode, final String path) throws EccmdException, IOException {
        throw new EccmdException("chmod: operation not supported");
    }

    /**
     * Gets the file size.
     *
     * @param source
     *            the source
     *
     * @return the file size
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long getFileSize(String source) throws EccmdException, IOException;

    /**
     * Delete file.
     *
     * @param source
     *            the source
     * @param force
     *            the force
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void deleteFile(final String source, final boolean force) throws EccmdException, IOException {
        throw new EccmdException("del: operation not supported");
    }

    /**
     * Gets the file list.
     *
     * @param path
     *            the path
     *
     * @return the file list
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract FileListElement[] getFileList(String path) throws EccmdException, IOException;

    /**
     * Gets the file list.
     *
     * @param path
     *            the path
     * @param options
     *            the options
     *
     * @return the file list
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract FileListElement[] getFileList(String path, String options) throws EccmdException, IOException;

    /**
     * Gets the file list element.
     *
     * @param path
     *            the path
     *
     * @return the file list element
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract FileListElement getFileListElement(String path) throws EccmdException, IOException;

    /**
     * Gets the file last modified.
     *
     * @param source
     *            the source
     *
     * @return the file last modified
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long getFileLastModified(String source) throws EccmdException, IOException;

    /**
     * Mkdir.
     *
     * @param dir
     *            the dir
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void mkdir(final String dir) throws EccmdException, IOException {
        throw new EccmdException("mkdir: operation not supported");
    }

    /**
     * Rmdir.
     *
     * @param dir
     *            the dir
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void rmdir(final String dir) throws EccmdException, IOException {
        throw new EccmdException("rmdir: operation not supported");
    }

    /**
     * Move file.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void moveFile(final String source, final String target) throws EccmdException, IOException {
        throw new EccmdException("mv: operation not supported");
    }

    /**
     * Copy file.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     * @param erase
     *            the erase
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void copyFile(final String source, final String target, final boolean erase)
            throws EccmdException, IOException {
        throw new EccmdException("cp: operation not supported");
    }

    /**
     * Gets the temp file.
     *
     * @return the temp file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EccmdException
     *             the eccmd exception
     */
    public String getTempFile() throws IOException, EccmdException {
        throw new EccmdException("mktmp: operation not supported");
    }

    /**
     * Gets the proxy socket input.
     *
     * @param source
     *            the source
     * @param offset
     *            the offset
     *
     * @return the proxy socket input
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract ProxySocket getProxySocketInput(String source, long offset) throws EccmdException, IOException;

    /**
     * Gets the proxy socket input.
     *
     * @param source
     *            the source
     * @param offset
     *            the offset
     * @param length
     *            the length
     *
     * @return the proxy socket input
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract ProxySocket getProxySocketInput(String source, long offset, long length)
            throws EccmdException, IOException;

    /**
     * Gets the proxy socket output.
     *
     * @param target
     *            the target
     * @param offset
     *            the offset
     * @param umask
     *            the umask
     *
     * @return the proxy socket output
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract ProxySocket getProxySocketOutput(String target, long offset, int umask)
            throws EccmdException, IOException;

    /**
     * Check.
     *
     * @param proxy
     *            the proxy
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EccmdException
     *             the eccmd exception
     */
    public abstract void check(ProxySocket proxy) throws IOException, EccmdException;

    /**
     * Close.
     *
     * @param remove
     *            the remove
     */
    public void close(final boolean remove) {
    }
}
