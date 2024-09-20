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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.security.RandomString;
import ecmwf.common.text.Format;

/**
 * The Class RegularFile.
 */
public final class RegularFile extends GenericFile {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RegularFile.class);

    /** The Constant FD_SYNC. */
    private static final boolean FD_SYNC = Cnf.at("IO", "fdSync", true);

    /** The Constant CHANNEL_FORCE. */
    private static final boolean CHANNEL_FORCE = Cnf.at("IO", "channelForce", false);

    /** The Constant CHANNEL_FORCE_METADATA. */
    private static final boolean CHANNEL_FORCE_METADATA = Cnf.at("IO", "channelForceMetadata", false);

    /** The underlyingFile. */
    private final File underlyingFile;

    /**
     * Instantiates a new regular file.
     *
     * @param path
     *            the path
     */
    RegularFile(final String path) {
        underlyingFile = new File(path);
    }

    /**
     * Instantiates a new regular file.
     *
     * @param parent
     *            the parent
     * @param name
     *            the name
     */
    RegularFile(final String parent, final String name) {
        underlyingFile = new File(parent, name);
    }

    /**
     * Rename to.
     *
     * @param path
     *            the path
     *
     * @return true, if successful
     */
    @Override
    public boolean renameTo(final String path) {
        return underlyingFile.renameTo(new File(path));
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    @Override
    public String getParent() {
        return underlyingFile.getParent();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return underlyingFile.getName();
    }

    /**
     * Checks if is directory.
     *
     * @return true, if is directory
     */
    @Override
    public boolean isDirectory() {
        return underlyingFile.isDirectory();
    }

    /**
     * Checks if is file.
     *
     * @return true, if is file
     */
    @Override
    public boolean isFile() {
        return underlyingFile.isFile();
    }

    /**
     * Exists.
     *
     * @return true, if successful
     */
    @Override
    public boolean exists() {
        return underlyingFile.exists();
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    @Override
    public String getPath() {
        return underlyingFile.getPath();
    }

    /**
     * Gets the absolute path.
     *
     * @return the absolute path
     */
    @Override
    public String getAbsolutePath() {
        return underlyingFile.getAbsolutePath();
    }

    /**
     * Gets the parent file.
     *
     * @return the parent file
     */
    @Override
    public GenericFile getParentFile() {
        final var parent = underlyingFile.getParentFile();
        if (parent != null) {
            return new RegularFile(parent.getAbsolutePath());
        }
        return null;
    }

    /**
     * Delete.
     *
     * @return true, if successful
     */
    @Override
    public boolean delete() {
        return underlyingFile.delete();
    }

    /**
     * Can read.
     *
     * @return true, if successful
     */
    @Override
    public boolean canRead() {
        return underlyingFile.canRead();
    }

    /**
     * Can write.
     *
     * @return true, if successful
     */
    @Override
    public boolean canWrite() {
        return underlyingFile.canWrite();
    }

    /**
     * Length.
     *
     * @return the long
     */
    @Override
    public long length() {
        return underlyingFile.length();
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
        return underlyingFile.lastModified();
    }

    /**
     * Sets the last modified.
     *
     * @param time
     *            the time
     *
     * @return true, if successful
     */
    @Override
    public boolean setLastModified(final long time) {
        return underlyingFile.setLastModified(time);
    }

    /**
     * Sets the read only.
     *
     * @return true, if successful
     */
    @Override
    public boolean setReadOnly() {
        return underlyingFile.setReadOnly();
    }

    /**
     * Mkdir.
     *
     * @return true, if successful
     */
    @Override
    public boolean mkdir() {
        return underlyingFile.mkdir();
    }

    /**
     * Mkdirs.
     *
     * @return true, if successful
     */
    @Override
    public boolean mkdirs() {
        return underlyingFile.mkdirs();
    }

    /**
     * List.
     *
     * @param filter
     *            the filter
     *
     * @return the string[]
     */
    @Override
    public String[] list(final GenericFileFilter filter) {
        final class LocalFilenameFilter implements FilenameFilter {
            final GenericFileFilter dirFilter;

            LocalFilenameFilter(final GenericFileFilter filter) {
                dirFilter = filter;
            }

            @Override
            public boolean accept(final File dir, final String name) {
                return dirFilter.accept(new RegularFile(dir.getAbsolutePath()), name);
            }
        }
        return underlyingFile.list(new LocalFilenameFilter(filter));
    }

    /**
     * List files.
     *
     * @param filter
     *            the filter
     *
     * @return the generic file[]
     */
    @Override
    public GenericFile[] listFiles(final GenericFileFilter filter) {
        final var files = list(filter);
        if (files == null) {
            return null;
        }
        final List<GenericFile> list = new ArrayList<>(files.length);
        for (final String file : files) {
            list.add(new RegularFile(getPath(), file));
        }
        return list.toArray(new GenericFile[list.size()]);
    }

    /**
     * Checks if is absolute.
     *
     * @return true, if is absolute
     */
    @Override
    public boolean isAbsolute() {
        return underlyingFile.isAbsolute();
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
        return underlyingFile;
    }

    /**
     * List.
     *
     * @return the string[]
     */
    @Override
    public String[] list() {
        return underlyingFile.list();
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
        try (final Stream<Path> paths = Files.walk(Paths.get(getAbsolutePath()))) {
            return paths.parallel().filter(p -> p.toFile().isFile()).count();
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
        try (Stream<Path> paths = Files.walk(Paths.get(getAbsolutePath()))) {
            return paths.filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
        }
    }

    /**
     * List files.
     *
     * @return the generic file[]
     */
    @Override
    public GenericFile[] listFiles() {
        final var files = list();
        if (files == null) {
            return null;
        }
        final List<GenericFile> list = new ArrayList<>(files.length);
        for (final String file : files) {
            list.add(new RegularFile(getPath(), file));
        }
        return list.toArray(new GenericFile[list.size()]);
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(underlyingFile);
    }

    /**
     * Gets the output stream.
     *
     * @return the output stream
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(underlyingFile);
    }

    /**
     * Gets the output stream.
     *
     * @param append
     *            the append
     *
     * @return the output stream
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public OutputStream getOutputStream(final boolean append) throws FileNotFoundException {
        return new FileOutputStream(underlyingFile, append);
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
     * @throws FileNotFoundException
     *             the file not found exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long transmitFile(final OutputStream out, final long offset) throws IOException {
        var closeCompleted = false;
        FileChannel inChannel = null;
        WritableByteChannel outChannel = null;
        FileInputStream file = null;
        try {
            file = new FileInputStream(underlyingFile);
            inChannel = file.getChannel();
            outChannel = Channels.newChannel(out);
            final var start = System.currentTimeMillis();
            final var expectedBytesCount = underlyingFile.length() - offset;
            final var transmittedBytesCount = inChannel.transferTo(offset, expectedBytesCount, outChannel);
            outChannel.close();
            inChannel.close();
            file.close();
            closeCompleted = true;
            final var duration = System.currentTimeMillis() - start;
            _log.info("File " + underlyingFile + " transmitted: " + Format.formatRate(transmittedBytesCount, duration));
            if (transmittedBytesCount != expectedBytesCount) {
                throw new IncorrectTransmittedSize("Incorrect transmitted size: " + transmittedBytesCount
                        + " byte(s) (instead of " + expectedBytesCount + " expected byte(s))");
            }
            return duration;
        } finally {
            if (!closeCompleted) {
                StreamPlugThread.closeQuietly(outChannel);
                StreamPlugThread.closeQuietly(inChannel);
                StreamPlugThread.closeQuietly(file);
            }
        }
    }

    /**
     * Receive file.
     *
     * @param in
     *            the in
     * @param expectedBytesCount
     *            the expected bytes count
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long receiveFile(final InputStream in, final long expectedBytesCount) throws IOException {
        var closeCompleted = false;
        var fileAvailable = false;
        ReadableByteChannel inChannel = null;
        FileChannel outChannel = null;
        FileOutputStream outFile = null;
        File tmpFile = null;
        try {
            inChannel = Channels.newChannel(in);
            final var parent = underlyingFile.getParentFile();
            if ((parent != null && !parent.exists() && !parent.mkdirs()) && !parent.exists()) {
                throw new IOException("Couldn't mkdirs: " + parent.getAbsolutePath());
            }
            final var suffix = "." + new RandomString(3).next();
            tmpFile = new File(parent, underlyingFile.getName() + suffix);
            if (expectedBytesCount >= 0) {
                _log.info("Waiting for " + expectedBytesCount + " byte(s) for file " + tmpFile.getAbsolutePath());
            } else {
                _log.info("Waiting byte(s) for file " + tmpFile.getAbsolutePath());
            }
            if (tmpFile.exists() && !tmpFile.delete()) {
                _log.warn("Couldn't delete existing temporary file: " + tmpFile.getAbsolutePath());
            }
            for (var i = 0; i < 3; i++) {
                try {
                    outFile = new FileOutputStream(tmpFile);
                    outChannel = outFile.getChannel();
                    if (CHANNEL_FORCE) {
                        outChannel.force(CHANNEL_FORCE_METADATA);
                    }
                    break;
                } catch (final FileNotFoundException e1) {
                    StreamPlugThread.closeQuietly(outFile);
                    StreamPlugThread.closeQuietly(outChannel);
                    if (i == 2) {
                        _log.warn("Could not create file", e1);
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException e2) {
                        }
                    }
                }
            }
            if (outChannel == null) {
                throw new IOException("Could not create file (no space left on device?)");
            }
            final var start = System.currentTimeMillis();
            final var receivedBytesCount = outChannel.transferFrom(inChannel, 0,
                    expectedBytesCount >= 0 ? expectedBytesCount : Long.MAX_VALUE);
            _log.debug("Transfer duration: " + Format.formatDuration(start, System.currentTimeMillis()));
            final var fdstart = System.currentTimeMillis();
            if (outFile != null && FD_SYNC) {
                outFile.getFD().sync();
                _log.debug("Sync: " + Format.formatDuration(fdstart, System.currentTimeMillis()));
            }
            outChannel.close();
            inChannel.close();
            if (outFile != null)
                outFile.close();
            closeCompleted = true;
            final var duration = System.currentTimeMillis() - start;
            _log.info("File " + underlyingFile + " downloaded: " + Format.formatRate(receivedBytesCount, duration));
            final var length = tmpFile.length();
            if (receivedBytesCount != length) {
                throw new IncorrectFileSize("Incorrect file size: " + length + " byte(s) (instead of "
                        + receivedBytesCount + " received byte(s))");
            }
            if (expectedBytesCount >= 0 && receivedBytesCount != expectedBytesCount) {
                throw new UnexpectedFileSize("Unexpected file size: " + length + " byte(s) (instead of "
                        + expectedBytesCount + " expected byte(s))");
            } else {
                if (underlyingFile.exists() && !underlyingFile.delete()) {
                    _log.warn("Couldn't delete existing file: " + underlyingFile.getAbsolutePath());
                }
                if (!tmpFile.renameTo(underlyingFile)) {
                    throw new IOException("Rename operation unsuccessful for " + tmpFile.getAbsolutePath() + " ("
                            + underlyingFile.getAbsolutePath() + ")");
                }
            }
            fileAvailable = true;
            return duration;
        } finally {
            if (!closeCompleted) {
                StreamPlugThread.closeQuietly(outChannel);
                StreamPlugThread.closeQuietly(inChannel);
                StreamPlugThread.closeQuietly(outFile);
            }
            if (!fileAvailable && (tmpFile != null && tmpFile.exists() && !tmpFile.delete())) {
                _log.warn("Couldn't delete temporary file: " + underlyingFile.getAbsolutePath());
            }
        }
    }
}
