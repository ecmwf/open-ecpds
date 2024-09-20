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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The Class GenericFileChecker.
 */
public abstract class GenericFileChecker extends GenericFile {
    /** The _file. */
    private GenericFile _file = null;

    /**
     * Instantiates a new generic file checker.
     *
     * @param file
     *            the file
     */
    public GenericFileChecker(final GenericFile file) {
        _file = file;
    }

    /**
     * Catch exception.
     *
     * @param service
     *            the service
     * @param t
     *            the t
     */
    public abstract void catchException(String service, Throwable t);

    /**
     * Gets the generic file.
     *
     * @return the generic file
     */
    public GenericFile getGenericFile() {
        return _file;
    }

    /**
     * Gets the parent file.
     *
     * @return the parent file
     */
    @Override
    public GenericFile getParentFile() {
        return _file.getParentFile();
    }

    /**
     * Exists.
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean exists() throws IOException {
        try {
            return _file.exists();
        } catch (final IOException e) {
            catchException("exists", e);
            throw e;
        }
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    @Override
    public String getPath() {
        return _file.getPath();
    }

    /**
     * Gets the absolute path.
     *
     * @return the absolute path
     */
    @Override
    public String getAbsolutePath() {
        return _file.getAbsolutePath();
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    @Override
    public String getParent() {
        return _file.getParent();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return _file.getName();
    }

    /**
     * Rename to.
     *
     * @param path
     *            the path
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean renameTo(final String path) throws IOException {
        try {
            return _file.renameTo(path);
        } catch (final IOException e) {
            catchException("renameTo", e);
            throw e;
        }
    }

    /**
     * Gets the file.
     *
     * @return the file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public File getFile() throws IOException {
        try {
            return _file.getFile();
        } catch (final IOException e) {
            catchException("getFile", e);
            throw e;
        }
    }

    /**
     * Delete.
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean delete() throws IOException {
        try {
            return _file.delete();
        } catch (final IOException e) {
            catchException("delete", e);
            throw e;
        }
    }

    /**
     * Mkdir.
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean mkdir() throws IOException {
        try {
            return _file.mkdir();
        } catch (final IOException e) {
            catchException("mkdir", e);
            throw e;
        }
    }

    /**
     * Mkdirs.
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean mkdirs() throws IOException {
        try {
            return _file.mkdirs();
        } catch (final IOException e) {
            catchException("mkdirs", e);
            throw e;
        }
    }

    /**
     * Can read.
     *
     * @return true, if successful
     */
    @Override
    public boolean canRead() {
        return _file.canRead();
    }

    /**
     * Can write.
     *
     * @return true, if successful
     */
    @Override
    public boolean canWrite() {
        return _file.canWrite();
    }

    /**
     * Checks if is absolute.
     *
     * @return true, if is absolute
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean isAbsolute() throws IOException {
        try {
            return _file.isAbsolute();
        } catch (final IOException e) {
            catchException("isAbsolute", e);
            throw e;
        }
    }

    /**
     * Last modified.
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long lastModified() throws IOException {
        try {
            return _file.lastModified();
        } catch (final IOException e) {
            catchException("lastModified", e);
            throw e;
        }
    }

    /**
     * Sets the last modified.
     *
     * @param time
     *            the time
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean setLastModified(final long time) throws IOException {
        try {
            return _file.setLastModified(time);
        } catch (final IOException e) {
            catchException("setLastModified", e);
            throw e;
        }
    }

    /**
     * Sets the read only.
     *
     * @return true, if successful
     */
    @Override
    public boolean setReadOnly() {
        return _file.setReadOnly();
    }

    /**
     * Checks if is directory.
     *
     * @return true, if is directory
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean isDirectory() throws IOException {
        try {
            return _file.isDirectory();
        } catch (final IOException e) {
            catchException("isDirectory", e);
            throw e;
        }
    }

    /**
     * Checks if is file.
     *
     * @return true, if is file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean isFile() throws IOException {
        try {
            return _file.isFile();
        } catch (final IOException e) {
            catchException("isFile", e);
            throw e;
        }
    }

    /**
     * Length.
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long length() throws IOException {
        try {
            return _file.length();
        } catch (final IOException e) {
            catchException("length", e);
            throw e;
        }
    }

    /**
     * List.
     *
     * @param filter
     *            the filter
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String[] list(final GenericFileFilter filter) throws IOException {
        try {
            return _file.list(filter);
        } catch (final IOException e) {
            catchException("list", e);
            throw e;
        }
    }

    /**
     * List files.
     *
     * @param filter
     *            the filter
     *
     * @return the generic file[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public GenericFile[] listFiles(final GenericFileFilter filter) throws IOException {
        try {
            return _file.listFiles(filter);
        } catch (final IOException e) {
            catchException("list", e);
            throw e;
        }
    }

    /**
     * List.
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String[] list() throws IOException {
        try {
            return _file.list();
        } catch (final IOException e) {
            catchException("list", e);
            throw e;
        }
    }

    /**
     * List count.
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long listCount() throws IOException {
        try {
            return _file.listCount();
        } catch (final IOException e) {
            catchException("listCount", e);
            throw e;
        }
    }

    /**
     * List size.
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long listSize() throws IOException {
        try {
            return _file.listSize();
        } catch (final IOException e) {
            catchException("listSize", e);
            throw e;
        }
    }

    /**
     * List files.
     *
     * @return the generic file[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public GenericFile[] listFiles() throws IOException {
        try {
            return _file.listFiles();
        } catch (final IOException e) {
            catchException("list", e);
            throw e;
        }
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public InputStream getInputStream() throws IOException, FileNotFoundException {
        try {
            return new InputStreamChecker(_file.getInputStream()) {
                @Override
                public void catchException(final String service, final Throwable t) {
                    GenericFileChecker.this.catchException(service, t);
                }
            };
        } catch (final IOException e) {
            catchException("getInputStream", e);
            throw e;
        }
    }

    /**
     * Gets the output stream.
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public OutputStream getOutputStream() throws IOException, FileNotFoundException {
        try {
            return new OutputStreamChecker(_file.getOutputStream()) {
                @Override
                public void catchException(final String service, final Throwable t) {
                    GenericFileChecker.this.catchException(service, t);
                }
            };
        } catch (final IOException e) {
            catchException("getOutputStream", e);
            throw e;
        }
    }

    /**
     * Gets the output stream.
     *
     * @param append
     *            the append
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public OutputStream getOutputStream(final boolean append) throws IOException, FileNotFoundException {
        try {
            return new OutputStreamChecker(_file.getOutputStream(append)) {
                @Override
                public void catchException(final String service, final Throwable t) {
                    GenericFileChecker.this.catchException(service, t);
                }
            };
        } catch (final IOException e) {
            catchException("getOutputStream", e);
            throw e;
        }
    }

    /**
     * Transmit file.
     *
     * @param out
     *            the out
     * @param offset
     *            the offset
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public long transmitFile(final OutputStream out, final long offset) throws IOException, FileNotFoundException {
        try {
            return _file.transmitFile(out, offset);
        } catch (final IOException e) {
            catchException("transmitFile", e);
            throw e;
        }
    }

    /**
     * Receive file.
     *
     * @param in
     *            the in
     * @param size
     *            the size
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public long receiveFile(final InputStream in, final long size) throws IOException, FileNotFoundException {
        try {
            return _file.receiveFile(in, size);
        } catch (final UnexpectedFileSize e) {
            // Don't report as this is not a filesytem issue.
            throw e;
        } catch (final FileNotFoundException | IncorrectFileSize e) {
            catchException("receiveFile", e);
            throw e;
        } catch (final IOException e) {
            final var message = e.getMessage();
            // If could not get source from host don't report as this is not a
            // filesystem issue.
            if (message == null || !message.startsWith("Could not get source from")) {
                catchException("receiveFile", e);
            }
            throw e;
        }
    }
}