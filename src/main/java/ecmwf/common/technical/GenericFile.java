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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The Class GenericFile.
 */
public abstract class GenericFile {
    /**
     * Gets the generic file.
     *
     * @param parent
     *            the parent
     * @param child
     *            the child
     *
     * @return the generic file
     */
    public static GenericFile getGenericFile(final GenericFile parent, final String child) {
        return new RegularFile(parent.getPath(), child);
    }

    /**
     * Gets the generic file.
     *
     * @param parent
     *            the parent
     * @param child
     *            the child
     *
     * @return the generic file
     */
    public static GenericFile getGenericFile(final String parent, final String child) {
        return new RegularFile(parent, child);
    }

    /**
     * Gets the generic file.
     *
     * @param path
     *            the path
     *
     * @return the generic file
     */
    public static GenericFile getGenericFile(final String path) {
        return new RegularFile(path);
    }

    /**
     * Exists.
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean exists() throws IOException;

    /**
     * Gets the path.
     *
     * @return the path
     */
    public abstract String getPath();

    /**
     * Gets the absolute path.
     *
     * @return the absolute path
     */
    public abstract String getAbsolutePath();

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public abstract String getParent();

    /**
     * Gets the name.
     *
     * @return the name
     */
    public abstract String getName();

    /**
     * Rename to.
     *
     * @param path
     *            the path
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean renameTo(String path) throws IOException;

    /**
     * Gets the file.
     *
     * @return the file
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract File getFile() throws IOException;

    /**
     * Gets the parent file.
     *
     * @return the parent file
     */
    public abstract GenericFile getParentFile();

    /**
     * Delete.
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean delete() throws IOException;

    /**
     * Delete.
     *
     * @param recursive
     *            the recursive
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean delete(final boolean recursive) throws IOException {
        if (recursive && isDirectory()) {
            for (final GenericFile file : listFiles()) {
                if (file.isDirectory()) {
                    file.delete(recursive);
                } else {
                    file.delete();
                }
            }
        }
        return delete();
    }

    /**
     * Mkdir.
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean mkdir() throws IOException;

    /**
     * Mkdirs.
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean mkdirs() throws IOException;

    /**
     * Can read.
     *
     * @return true, if successful
     */
    public abstract boolean canRead();

    /**
     * Can write.
     *
     * @return true, if successful
     */
    public abstract boolean canWrite();

    /**
     * Checks if is absolute.
     *
     * @return true, if is absolute
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean isAbsolute() throws IOException;

    /**
     * Last modified.
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long lastModified() throws IOException;

    /**
     * Sets the last modified.
     *
     * @param time
     *            the time
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean setLastModified(long time) throws IOException;

    /**
     * Sets the read only.
     *
     * @return true, if successful
     */
    public abstract boolean setReadOnly();

    /**
     * Checks if is directory.
     *
     * @return true, if is directory
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean isDirectory() throws IOException;

    /**
     * Checks if is file.
     *
     * @return true, if is file
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract boolean isFile() throws IOException;

    /**
     * Length.
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long length() throws IOException;

    /**
     * List.
     *
     * @param filter
     *            the filter
     *
     * @return the string[]
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract String[] list(GenericFileFilter filter) throws IOException;

    /**
     * List.
     *
     * @return the string[]
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract String[] list() throws IOException;

    /**
     * Gives the number of files included in the specified directory and sub-directories.
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long listCount() throws IOException;

    /**
     * Gives the total size of the files included in the specified directory and sub-directories.
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long listSize() throws IOException;

    /**
     * List files.
     *
     * @param filter
     *            the filter
     *
     * @return the generic file[]
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract GenericFile[] listFiles(GenericFileFilter filter) throws IOException;

    /**
     * List files.
     *
     * @return the generic file[]
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract GenericFile[] listFiles() throws IOException;

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Gets the output stream.
     *
     * @return the output stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract OutputStream getOutputStream() throws IOException;

    /**
     * Gets the output stream.
     *
     * @param append
     *            the append
     *
     * @return the output stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract OutputStream getOutputStream(boolean append) throws IOException;

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
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long transmitFile(OutputStream out, long offset) throws IOException;

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
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract long receiveFile(InputStream in, long size) throws IOException;

    /**
     * The Class UnexpectedFileSize.
     */
    public static class UnexpectedFileSize extends IOException {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 592271106715542861L;

        /**
         * Instantiates a new unexpected file size.
         *
         * @param message
         *            the message
         */
        UnexpectedFileSize(final String message) {
            super(message);
        }
    }

    /**
     * The Class IncorrectFileSize.
     */
    public static class IncorrectFileSize extends IOException {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -6172969978336110154L;

        /**
         * Instantiates a new incorrect file size.
         *
         * @param message
         *            the message
         */
        IncorrectFileSize(final String message) {
            super(message);
        }
    }

    /**
     * The Class IncorrectTransmittedSize.
     */
    public static class IncorrectTransmittedSize extends IOException {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -8890786604865606805L;

        /**
         * Instantiates a new incorrect transmitted size.
         *
         * @param message
         *            the message
         */
        IncorrectTransmittedSize(final String message) {
            super(message);
        }
    }
}
