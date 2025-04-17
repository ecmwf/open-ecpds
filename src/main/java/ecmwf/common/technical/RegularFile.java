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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    private static final boolean CHANNEL_FORCE = Cnf.at("IO", "channelForce", true);

    /** The Constant BYTE_BUFFER_POOL. */
    private static final boolean BYTE_BUFFER_POOL = Cnf.at("IO", "byteBufferPool", true);

    /** The Constant BYTE_BUFFER_USE. */
    private static final boolean BYTE_BUFFER_USE = Cnf.at("IO", "byteBufferUse", true);

    /** The Constant BYTE_BUFFER_DIRECT. */
    private static final boolean BYTE_BUFFER_DIRECT = Cnf.at("IO", "byteBufferDirect", true);

    /** The Constant BYTE_BUFFER_SIZE_IN_BYTES. */
    private static final int BYTE_BUFFER_SIZE_IN_BYTES = 1024 * 1024 * Cnf.at("IO", "byteBufferSizeInMb", 1);

    /** The Constant CHANNEL_FORCE_METADATA. */
    private static final boolean CHANNEL_FORCE_METADATA = Cnf.at("IO", "channelForceMetadata", true);

    /** The Constant pool. */
    private static DirectByteBufferPool pool = new DirectByteBufferPool(BYTE_BUFFER_SIZE_IN_BYTES,
            TimeUnit.SECONDS.toSeconds(Cnf.at("IO", "byteBufferPoolMaxIdleSec", 30)));

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
     * {@inheritDoc}
     *
     * Rename to.
     */
    @Override
    public boolean renameTo(final String path) {
        return underlyingFile.renameTo(new File(path));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the parent.
     */
    @Override
    public String getParent() {
        return underlyingFile.getParent();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return underlyingFile.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is directory.
     */
    @Override
    public boolean isDirectory() {
        return underlyingFile.isDirectory();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is file.
     */
    @Override
    public boolean isFile() {
        return underlyingFile.isFile();
    }

    /**
     * {@inheritDoc}
     *
     * Exists.
     */
    @Override
    public boolean exists() {
        return underlyingFile.exists();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the path.
     */
    @Override
    public String getPath() {
        return underlyingFile.getPath();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the absolute path.
     */
    @Override
    public String getAbsolutePath() {
        return underlyingFile.getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the parent file.
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
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public boolean delete() {
        return deleteIfExists(underlyingFile);
    }

    /**
     * {@inheritDoc}
     *
     * Can read.
     */
    @Override
    public boolean canRead() {
        return underlyingFile.canRead();
    }

    /**
     * {@inheritDoc}
     *
     * Can write.
     */
    @Override
    public boolean canWrite() {
        return underlyingFile.canWrite();
    }

    /**
     * {@inheritDoc}
     *
     * Length.
     */
    @Override
    public long length() {
        return underlyingFile.length();
    }

    /**
     * {@inheritDoc}
     *
     * Last modified.
     */
    @Override
    public long lastModified() throws IOException {
        return underlyingFile.lastModified();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the last modified.
     */
    @Override
    public boolean setLastModified(final long time) {
        return underlyingFile.setLastModified(time);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the read only.
     */
    @Override
    public boolean setReadOnly() {
        return underlyingFile.setReadOnly();
    }

    /**
     * {@inheritDoc}
     *
     * Mkdir.
     */
    @Override
    public boolean mkdir() {
        return underlyingFile.mkdir();
    }

    /**
     * {@inheritDoc}
     *
     * Mkdirs.
     */
    @Override
    public boolean mkdirs() {
        return underlyingFile.mkdirs();
    }

    /**
     * {@inheritDoc}
     *
     * List.
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
     * {@inheritDoc}
     *
     * List files.
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
     * {@inheritDoc}
     *
     * Checks if is absolute.
     */
    @Override
    public boolean isAbsolute() {
        return underlyingFile.isAbsolute();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the file.
     */
    @Override
    public File getFile() throws IOException {
        return underlyingFile;
    }

    /**
     * {@inheritDoc}
     *
     * List.
     */
    @Override
    public String[] list() {
        return underlyingFile.list();
    }

    /**
     * {@inheritDoc}
     *
     * List count.
     */
    @Override
    public long listCount() throws IOException {
        try (final Stream<Path> paths = Files.walk(Paths.get(getAbsolutePath()))) {
            return paths.parallel().filter(p -> p.toFile().isFile()).count();
        }
    }

    /**
     * {@inheritDoc}
     *
     * List size.
     */
    @Override
    public long listSize() throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(getAbsolutePath()))) {
            return paths.filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
        }
    }

    /**
     * {@inheritDoc}
     *
     * List files.
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
     * {@inheritDoc}
     *
     * Gets the input stream.
     */
    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(underlyingFile);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the output stream.
     */
    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(underlyingFile);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the output stream.
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
        return BYTE_BUFFER_USE ? transmitFileWithByteBuffer(out, offset) : transmitFileStandard(out, offset);
    }

    /**
     * Transmit file standard.
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
     */
    private long transmitFileStandard(final OutputStream out, final long offset) throws IOException {
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
            if (_log.isInfoEnabled())
                _log.info("File {} transmitted: {}", underlyingFile,
                        Format.formatRate(transmittedBytesCount, duration));
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
     * Transmit file with byte buffer.
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
     */
    private long transmitFileWithByteBuffer(final OutputStream out, final long offset) throws IOException {
        final long count = underlyingFile.length() - offset;
        final ByteBuffer buffer = getByteBuffer(count);
        try (final FileInputStream inFile = new FileInputStream(underlyingFile);
                final FileChannel inChannel = inFile.getChannel();
                final WritableByteChannel outChannel = Channels.newChannel(out)) {
            long position = offset;
            final var start = System.currentTimeMillis();
            while (position < count) {
                buffer.clear(); // Prepare buffer for writing
                final int bytesRead = inChannel.read(buffer, position);
                if (bytesRead == -1)
                    break; // End of file
                buffer.flip(); // Prepare buffer for reading
                while (buffer.hasRemaining())
                    position += outChannel.write(buffer);
            }
            final var duration = System.currentTimeMillis() - start;
            if (_log.isInfoEnabled())
                _log.info("File {} transmitted: {}", underlyingFile, Format.formatRate(position - offset, duration));
            if (position - offset != count)
                throw new IncorrectTransmittedSize("Incorrect transmitted size: " + (position - offset)
                        + " byte(s) (instead of " + count + " expected byte(s))");
            return duration;
        } finally {
            // Important: release buffer to the pool
            release(buffer);
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
        return BYTE_BUFFER_USE ? receiveFileWithByteBuffer(in, expectedBytesCount)
                : receiveFileStandard(in, expectedBytesCount);
    }

    /**
     * Receive file standard.
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
    private long receiveFileStandard(final InputStream in, final long expectedBytesCount) throws IOException {
        var closeCompleted = false;
        var fileAvailable = false;
        var synchronised = false;
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
                _log.info("Waiting for {} byte(s) for file {}", expectedBytesCount, tmpFile.getAbsolutePath());
            } else {
                _log.info("Waiting byte(s) for file {}", tmpFile.getAbsolutePath());
            }
            deleteIfExists(tmpFile);
            for (var i = 0; i < 3; i++) {
                try {
                    outFile = new FileOutputStream(tmpFile);
                    outChannel = outFile.getChannel();
                    if (CHANNEL_FORCE) {
                        outChannel.force(CHANNEL_FORCE_METADATA);
                        synchronised = true;
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
                            Thread.currentThread().interrupt();
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
            if (_log.isInfoEnabled())
                _log.debug("Transfer duration: {}", Format.formatDuration(start, System.currentTimeMillis()));
            final var fdstart = System.currentTimeMillis();
            if (!synchronised && outFile != null && FD_SYNC) {
                outFile.getFD().sync();
                if (_log.isInfoEnabled())
                    _log.debug("Sync: {}", Format.formatDuration(fdstart, System.currentTimeMillis()));
            }
            outChannel.close();
            inChannel.close();
            if (outFile != null)
                outFile.close();
            closeCompleted = true;
            final var duration = System.currentTimeMillis() - start;
            if (_log.isInfoEnabled())
                _log.info("File {} downloaded: {}", underlyingFile, Format.formatRate(receivedBytesCount, duration));
            final var length = tmpFile.length();
            if (receivedBytesCount != length) {
                throw new IncorrectFileSize("Incorrect file size: " + length + " byte(s) (instead of "
                        + receivedBytesCount + " received byte(s))");
            }
            if (expectedBytesCount >= 0 && receivedBytesCount != expectedBytesCount) {
                throw new UnexpectedFileSize("Unexpected file size: " + length + " byte(s) (instead of "
                        + expectedBytesCount + " expected byte(s))");
            } else {
                deleteIfExists(underlyingFile);
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
            if (!fileAvailable)
                deleteIfExists(tmpFile);
        }
    }

    /**
     * Receive file with byte buffer.
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
    private long receiveFileWithByteBuffer(final InputStream in, final long expectedBytesCount) throws IOException {
        var fileAvailable = false;
        try (final ReadableByteChannel inChannel = Channels.newChannel(in)) {
            final var parent = underlyingFile.getParentFile();
            if ((parent != null && !parent.exists() && !parent.mkdirs()) && !parent.exists()) {
                throw new IOException("Couldn't mkdirs: " + parent.getAbsolutePath());
            }
            final var suffix = "." + new RandomString(3).next();
            final File tmpFile = new File(parent, underlyingFile.getName() + suffix);
            if (expectedBytesCount >= 0) {
                _log.info("Waiting for {} byte(s) for file {}", expectedBytesCount, tmpFile.getAbsolutePath());
            } else {
                _log.info("Waiting byte(s) for file {}", tmpFile.getAbsolutePath());
            }
            deleteIfExists(tmpFile);
            final ByteBuffer buffer = getByteBuffer(expectedBytesCount);
            try (final FileOutputStream outFile = new FileOutputStream(tmpFile);
                    final FileChannel outChannel = createFileChannel(outFile)) {
                long position = 0;
                long count = expectedBytesCount >= 0 ? expectedBytesCount : Long.MAX_VALUE;
                final var start = System.currentTimeMillis();
                while (position < count) {
                    buffer.clear(); // Prepare buffer for writing
                    int bytesRead = inChannel.read(buffer);
                    if (bytesRead == -1)
                        break; // End of stream
                    buffer.flip(); // Prepare buffer for reading
                    while (buffer.hasRemaining())
                        position += outChannel.write(buffer);
                }
                if (_log.isDebugEnabled())
                    _log.debug("Transfer duration: {}", Format.formatDuration(start, System.currentTimeMillis()));
                if (FD_SYNC) {
                    final var fdstart = System.currentTimeMillis();
                    outFile.getFD().sync();
                    if (_log.isDebugEnabled())
                        _log.debug("Sync: {}", Format.formatDuration(fdstart, System.currentTimeMillis()));
                }
                final var duration = System.currentTimeMillis() - start;
                if (_log.isInfoEnabled())
                    _log.info("File {} downloaded: {}", underlyingFile, Format.formatRate(position, duration));
                final var length = tmpFile.length();
                if (position != length)
                    throw new IncorrectFileSize("Incorrect file size: " + length + " byte(s) (instead of " + position
                            + " received byte(s))");
                if (expectedBytesCount >= 0 && position != expectedBytesCount) {
                    throw new UnexpectedFileSize("Unexpected file size: " + length + " byte(s) (instead of "
                            + expectedBytesCount + " expected byte(s))");
                } else {
                    deleteIfExists(underlyingFile);
                    if (!tmpFile.renameTo(underlyingFile))
                        throw new IOException("Rename operation unsuccessful for " + tmpFile.getAbsolutePath() + " ("
                                + underlyingFile.getAbsolutePath() + ")");
                }
                fileAvailable = true;
                return duration;
            } finally {
                // Return buffer to pool even in case of exception
                release(buffer);
                // Clean up temp file if download failed
                if (!fileAvailable)
                    deleteIfExists(tmpFile);
            }
        }
    }

    /**
     * Attempts to delete a file using NIO, with detailed logging on failure.
     *
     * @param file
     *            the file to delete
     *
     * @return true if the file was successfully deleted or did not exist, false otherwise
     */
    public static boolean deleteIfExists(final File file) {
        if (file == null)
            return false;
        final var path = file.toPath();
        try {
            var deleted = Files.deleteIfExists(path);
            if (deleted && _log.isDebugEnabled()) {
                _log.debug("Deleted file: {}", path);
            } else if (!deleted) {
                _log.warn("File did not exist: {}", path);
            }
            return deleted;
        } catch (NoSuchFileException e) {
            _log.warn("File not found during deletion: {}", path);
        } catch (DirectoryNotEmptyException e) {
            _log.warn("Directory not empty: {}", path);
        } catch (IOException e) {
            _log.warn("I/O error while deleting file {}: {}", path, e.toString());
        }
        return false;
    }

    /**
     * Gets the byte buffer.
     *
     * @param length
     *            the length
     *
     * @return the byte buffer
     */
    private static ByteBuffer getByteBuffer(final long length) {
        // Only use the pool if the file is large enough and the pool is enabled
        if (BYTE_BUFFER_POOL && length >= BYTE_BUFFER_SIZE_IN_BYTES)
            return pool.acquire(); // Uses a pooled buffer
        // For small files or when pool is disabled, allocate a new buffer
        final int bufferSize = (int) Math.min(BYTE_BUFFER_SIZE_IN_BYTES, length < 0 ? 0 : length);
        return BYTE_BUFFER_DIRECT ? ByteBuffer.allocateDirect(bufferSize) : ByteBuffer.allocate(bufferSize);
    }

    /**
     * Release.
     *
     * @param buffer
     *            the buffer
     */
    private static void release(final ByteBuffer buffer) {
        if (BYTE_BUFFER_POOL)
            pool.release(buffer);
    }

    /**
     * Creates the file channel.
     *
     * @param outFile
     *            the out file
     *
     * @return the file channel
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static FileChannel createFileChannel(final FileOutputStream outFile) throws IOException {
        for (var i = 0; i < 3; i++)
            try {
                final var outChannel = outFile.getChannel();
                if (CHANNEL_FORCE)
                    outChannel.force(CHANNEL_FORCE_METADATA);
                return outChannel;
            } catch (FileNotFoundException e1) {
                _log.warn("Could not create file", e1);
                if (i == 2) {
                    throw new IOException("Could not create file after 3 attempts", e1);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Thread interrupted during retry", e2);
                }
            }
        throw new IOException("Could not create file (no space left on device?)");
    }
}
