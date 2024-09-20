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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.MSUser;

/**
 * The Class TransferModule.
 */
public abstract class TransferModule implements Closeable {
    /** The _env. */
    private final Map<Object, Object> env = new ConcurrentHashMap<>();

    /** The msuser. */
    private MSUser msuser = null;

    /** The ecuser. */
    private ECUser ecuser = null;

    /** The module. */
    private ECtransModule module = null;

    /** The destination. */
    private ECtransDestination destination = null;

    /** The provider. */
    private RemoteProvider provider = null;

    /** The cookie. */
    private String cookie = null;

    /** The password. */
    private String password = null;

    /** The debug. */
    private boolean debug = false;

    /** The closed on error. */
    private boolean closedOnError = false;

    /** The available (not sent). */
    private boolean available = false;

    /**
     * Sets the remote provider.
     *
     * @param provider
     *            the remote provider
     */
    void setRemoteProvider(final RemoteProvider provider) {
        this.provider = provider;
    }

    /**
     * Sets the MS user.
     *
     * @param msuser
     *            the new MS user
     */
    void setMSUser(final MSUser msuser) {
        this.msuser = msuser;
    }

    /**
     * Sets the ECUser.
     *
     * @param ecuser
     *            the new ECUser
     */
    void setECUser(final ECUser ecuser) {
        this.ecuser = ecuser;
    }

    /**
     * Sets the ectrans module.
     *
     * @param module
     *            the new ectrans module
     */
    void setECtransModule(final ECtransModule module) {
        this.module = module;
    }

    /**
     * Sets the ectrans destination.
     *
     * @param destination
     *            the new ectrans destination
     */
    void setECtransDestination(final ECtransDestination destination) {
        this.destination = destination;
    }

    /**
     * Sets the cookie.
     *
     * @param cookie
     *            the new cookie
     */
    void setCookie(final String cookie) {
        this.cookie = cookie;
    }

    /**
     * Sets the clear password.
     *
     * @param password
     *            the new clear password
     */
    void setClearPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the debug.
     *
     * @param debug
     *            the new debug
     */
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    /**
     * Sets the closed on error.
     *
     * @param closedOnError
     *            the new closed on error
     */
    public void setClosedOnError(final boolean closedOnError) {
        this.closedOnError = closedOnError;
    }

    /**
     * Sets the available.
     *
     * @param available
     *            the available
     */
    public void setAvailable(final boolean available) {
        this.available = available;
    }

    /**
     * Gets the remote provider.
     *
     * @return the remote provider
     */
    public RemoteProvider getRemoteProvider() {
        return provider;
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
     * Gets the ECUser.
     *
     * @return the ECUser
     */
    public ECUser getECUser() {
        return ecuser;
    }

    /**
     * Gets the ectrans module.
     *
     * @return the ectrans module
     */
    public ECtransModule getECtransModule() {
        return module;
    }

    /**
     * Gets the ectrans destination.
     *
     * @return the ectrans destination
     */
    public ECtransDestination getECtransDestination() {
        return destination;
    }

    /**
     * Gets the cookie.
     *
     * @return the cookie
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * Gets the clear password.
     *
     * @return the clear password
     */
    public String getClearPassword() {
        return password;
    }

    /**
     * Gets the debug.
     *
     * @return the debug
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Gets the closed on error.
     *
     * @return the closed on error
     */
    public boolean getClosedOnError() {
        return closedOnError;
    }

    /**
     * Gets the available.
     *
     * @return the available
     */
    public boolean getAvailable() {
        return available;
    }

    /**
     * Connect.
     *
     * @param location
     *            the location
     * @param setup
     *            the setup
     *
     * @throws Exception
     *             the exception
     */
    public abstract void connect(String location, ECtransSetup setup) throws Exception;

    /**
     * Gets the port from the setup.
     *
     * @param setup
     *            the setup
     *
     * @return the port
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public int getPort(final ECtransSetup setup) throws IOException {
        throw new IOException("Port not available");
    }

    /**
     * Delete by name.
     *
     * @param name
     *            the name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void del(String name) throws IOException;

    /**
     * Make directory by name.
     *
     * @param dir
     *            the dir
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void mkdir(final String dir) throws IOException {
        throw new IOException("Mkdir not implemented");
    }

    /**
     * Remove directory by name.
     *
     * @param dir
     *            the dir
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void rmdir(final String dir) throws IOException {
        throw new IOException("Rmdir not implemented");
    }

    /**
     * Move source to target.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void move(final String source, final String target) throws IOException {
        throw new IOException("Move not implemented");
    }

    /**
     * Pre-get. When using an external module to transfer files this method is called first.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void preGet(final String name, final long posn) throws IOException {
        throw new IOException("PreGet not implemented");
    }

    /**
     * Gets the input stream to read the file given by name starting at posn.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract InputStream get(String name, long posn) throws IOException;

    /**
     * Gets the content of the file given by name written to the provided output stream, starting at posn. By default,
     * doing nothing and returning false.
     *
     * @param out
     *            the out
     * @param name
     *            the name
     * @param posn
     *            the posn
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean get(final OutputStream out, final String name, final long posn) throws IOException {
        return false;
    }

    /**
     * Pre-put. When using an external module to transfer files this method is called first to get the target filename!
     *
     * @param name
     *            the name
     * @param tmpName
     *            the tmp name
     * @param posn
     *            the posn
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String prePut(final String name, final String tmpName, final long posn) throws IOException {
        throw new IOException("PrePut not implemented");
    }

    /**
     * Sets the input filter (for compression on the fly).
     *
     * @param filter
     *            the new input filter
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setInputFilter(final String filter) throws IOException {
        throw new IOException("Input filter not supported");
    }

    /**
     * Sets the input size. By default, is ignored.
     *
     * @param size
     *            the new input size
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setInputSize(final long size) throws IOException {
    }

    /**
     * Sets the input md5. By default, is ignored.
     *
     * @param md5
     *            the new input md5
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setInputMd5(final String md5) throws IOException {
    }

    /**
     * Sets the output filter (for compression on the fly).
     *
     * @param filter
     *            the new output filter
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setOutputFilter(final String filter) throws IOException {
        throw new IOException("Output filter not supported");
    }

    /**
     * Instruct the transfer module that the checksum is delegated.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void delegateChecksum() throws IOException {
        throw new IOException("Checksum not supported on filtred stream");
    }

    /**
     * Provide an output stream to write the content of the file given by its name starting at position posn.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract OutputStream put(String name, long posn, long size) throws IOException;

    /**
     * Write the content of the file given by its name, starting at position posn and writing to the provided input
     * stream. By default, doing nothing and returning false.
     *
     * @param in
     *            the in
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean put(final InputStream in, final String name, final long posn, final long size) throws IOException {
        return false;
    }

    /**
     * Force an update of the statistics from the underlying socket.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void updateSocketStatistics() throws IOException {
    }

    /**
     * Copy the source file to the target file, starting at position posn and writing size bytes.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void copy(final String source, final String target, final long posn, final long size) throws IOException {
        throw new IOException("Copy not implemented");
    }

    /**
     * Check if the last put was successful comparing the number of bytes sent and the checksum provided to the one
     * calculated. By default, doing nothing.
     *
     * @param sent
     *            the sent
     * @param checksum
     *            the checksum
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void check(final long sent, final String checksum) throws IOException {
    }

    /**
     * Give the size of the file given by its name.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long size(String name) throws IOException;

    /**
     * List as a string array.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        throw new IOException("List not implemented");
    }

    /**
     * Default implementation to provide the list as a GZIPed byte array. The implementation should give a better
     * implementation as this one is relying on the non optimal listAsStringArray method by default.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     *
     * @return the bytes
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public byte[] listAsByteArray(final String directory, final String pattern) throws IOException {
        final var out = new ByteArrayOutputStream();
        try (final var gzip = new GZIPOutputStream(out, Deflater.BEST_COMPRESSION)) {
            list(directory, pattern, gzip);
        }
        return out.toByteArray();
    }

    /**
     * Default implementation to write the list to the provided output stream. The implementation should give a better
     * implementation as this one is relying on the non optimal listAsStringArray method by default.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param out
     *            the out
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void list(final String directory, final String pattern, final OutputStream out) throws IOException {
        for (final String line : listAsStringArray(directory, pattern)) {
            out.write(line.concat("\n").getBytes());
            out.flush();
        }
    }

    /**
     * Sets the attribute given by its key and value.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void setAttribute(final Object key, final Object value) {
        env.put(key, value);
    }

    /**
     * Gets the attribute given by its key.
     *
     * @param key
     *            the key
     *
     * @return the attribute
     */
    public Object getAttribute(final Object key) {
        return env.get(key);
    }

    /**
     * Sets the attribute given by its key and value.
     *
     * @param <T>
     *            the generic type
     * @param value
     *            the value
     */
    public <T> void setAttribute(final T value) {
        env.put(value.getClass().getName(), value);
    }

    /**
     * Gets the attribute given by its class.
     *
     * @param <T>
     *            the generic type
     * @param key
     *            the class
     *
     * @return the attribute value
     */
    public <T> T getAttribute(final Class<T> key) {
        final var value = env.get(key.getName());
        return value != null && key.isInstance(value) ? key.cast(value) : null;
    }

    /**
     * Check if the attribute exists by its key.
     *
     * @param key
     *            the key
     *
     * @return true if exists
     */
    public boolean containsAttribute(final Object key) {
        return env.containsKey(key);
    }

    /**
     * Gets the current status of the transfer module.
     *
     * @return the status
     */
    public String getStatus() {
        return toString();
    }
}
