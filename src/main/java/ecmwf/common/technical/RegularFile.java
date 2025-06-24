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
            TimeUnit.SECONDS.toSeconds(Cnf.at("IO", "byteBufferPoolMaxIdleSec", 60)));

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
        return deleteIfExists(underlyingFile);
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
    public String[] list(final GenericFileFilter filter) throws IOException {
        final var dirPath = underlyingFile.toPath();
        final List<String> result = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(dirPath, entry -> {
            final var fileName = entry.getFileName();
            return fileName != null && filter.accept(new RegularFile(dirPath.toString()), fileName.toString());
        })) {
            for (final Path entry : stream) {
                result.add(entry.getFileName().toString());
            }
        }
        return result.toArray(String[]::new);
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
    public GenericFile[] listFiles(final GenericFileFilter filter) throws IOException {
        final var files = list(filter);
        final var list = new ArrayList<>(files.length);
        final var parent = getPath();
        for (final String file : files) {
            list.add(new RegularFile(parent, file));
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
    public String[] list() throws IOException {
        final var dirPath = underlyingFile.toPath();
        final List<String> result = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(dirPath)) {
            for (final Path entry : stream) {
                result.add(entry.getFileName().toString());
            }
        }
        return result.toArray(String[]::new);
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
        try (final var paths = Files.walk(Paths.get(getAbsolutePath()))) {
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
        try (var paths = Files.walk(Paths.get(getAbsolutePath()))) {
            return paths.filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
        }
    }

    /**
     * List files.
     *
     * @return the generic file[]
     */
    @Override
    public GenericFile[] listFiles() throws IOException {
        final var files = list();
        final var list = new ArrayList<>(files.length);
        final var parent = getPath();
        for (final String file : files) {
            list.add(new RegularFile(parent, file));
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
        final var count = underlyingFile.length() - offset;
        final var buffer = getByteBuffer(count);
        try (final var inFile = new FileInputStream(underlyingFile); final var inChannel = inFile.getChannel();
                final var outChannel = Channels.newChannel(out)) {
            var position = offset;
            final var start = System.currentTimeMillis();
            while (position < count) {
                buffer.clear(); // Prepare buffer for writing
                final var bytesRead = inChannel.read(buffer, position);
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
        try (final var inChannel = Channels.newChannel(in)) {
            final var parent = underlyingFile.getParentFile();
            if ((parent != null && !parent.exists() && !parent.mkdirs()) && !parent.exists()) {
                throw new IOException("Couldn't mkdirs: " + parent.getAbsolutePath());
            }
            final var suffix = "." + new RandomString(3).next();
            final var tmpFile = new File(parent, underlyingFile.getName() + suffix);
            if (expectedBytesCount >= 0) {
                _log.info("Waiting for {} byte(s) for file {}", expectedBytesCount, tmpFile.getAbsolutePath());
            } else {
                _log.info("Waiting byte(s) for file {}", tmpFile.getAbsolutePath());
            }
            deleteIfExists(tmpFile);
            final var buffer = getByteBuffer(expectedBytesCount);
            try (final var outFile = new FileOutputStream(tmpFile); final var outChannel = createFileChannel(outFile)) {
                var position = 0L;
                final var count = expectedBytesCount >= 0 ? expectedBytesCount : Long.MAX_VALUE;
                final var start = System.currentTimeMillis();
                while (position < count) {
                    buffer.clear(); // Prepare buffer for writing
                    final var bytesRead = inChannel.read(buffer);
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
            final var deleted = Files.deleteIfExists(path);
            if (deleted && _log.isDebugEnabled()) {
                _log.debug("Deleted file: {}", path);
            } else if (!deleted) {
                _log.warn("File did not exist: {}", path);
            }
            return deleted;
        } catch (final NoSuchFileException e) {
            _log.warn("File not found during deletion: {}", path);
        } catch (final DirectoryNotEmptyException e) {
            _log.warn("Directory not empty: {}", path);
        } catch (final IOException e) {
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
        final var bufferSize = (int) Math.min(BYTE_BUFFER_SIZE_IN_BYTES, length < 0 ? Long.MAX_VALUE : length);
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
            } catch (final FileNotFoundException e1) {
                _log.warn("Could not create file", e1);
                if (i == 2) {
                    throw new IOException("Could not create file after 3 attempts", e1);
                }
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e2) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Thread interrupted during retry", e2);
                }
            }
        throw new IOException("Could not create file (no space left on device?)");
    }
}
