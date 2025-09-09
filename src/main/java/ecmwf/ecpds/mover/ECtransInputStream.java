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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECTRANS;
import static ecmwf.common.text.Util.isNotEmpty;
import static ecmwf.ecpds.master.DataFilePath.getPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataFile;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.CleanableSupport;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.mover.MoverServer.ECproxyCallback;

/**
 * The Class ECtransInputStream.
 */
final class ECtransInputStream extends InputStream implements AutoCloseable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECtransInputStream.class);

    /** The Constant mover. */
    private static final MoverServer MOVER = StarterServer.getInstance(MoverServer.class);

    /** The Constant ECAUTH_USER. */
    private static final String ECAUTH_USER = "$dataFile[ecauthUser]";

    /** The Constant ECAUTH_HOST. */
    private static final String ECAUTH_HOST = "$dataFile[ecauthHost]";

    /** Cleaner support for resource cleanup. */
    private final CleanableSupport cleaner;

    /** The host for source. */
    private Host hostForSource = null;

    /** The in. */
    private SourceInputStream in = null;

    /**
     * Instantiates a new ectrans input stream.
     *
     * @param hostsForSource
     *            the hosts for source
     * @param dataFile
     *            the data file
     * @param posn
     *            the posn
     *
     * @throws SourceNotAvailableException
     *             the source not available exception
     */
    public ECtransInputStream(final Host[] hostsForSource, final DataFile dataFile, final long posn)
            throws SourceNotAvailableException {
        // Setup GC cleanup hook
        this.cleaner = new CleanableSupport(this, this::cleanup);
        final var fileSize = dataFile.getSize();
        final var hostsList = new StringBuilder();
        Throwable throwable = null;
        for (var i = 0; i < hostsForSource.length; i++) {
            hostForSource = hostsForSource[i];
            var login = hostForSource.getLogin();
            var host = hostForSource.getHost();
            // Is it a generic host for source?
            if ((dataFile.getEcauthUser() == null || dataFile.getEcauthHost() == null)
                    && (login.indexOf(ECAUTH_USER) != -1 || host.indexOf(ECAUTH_HOST) != -1)) {
                _log.debug("Discarding generic source host {} (file was pushed through the data portal)",
                        hostForSource.getNickname());
                continue;
            }
            _log.debug("Trying getting source from {}", hostForSource.getNickname());
            if (isNotEmpty(login)) {
                login = Cnf.getValue(login);
                hostForSource.setLogin(Format.replaceAll(login, ECAUTH_USER, dataFile.getEcauthUser()));
                _log.debug("Using login: {}", hostForSource.getLogin());
            }
            if (isNotEmpty(host)) {
                host = Cnf.getValue(host);
                hostForSource.setHost(Format.replaceAll(host, ECAUTH_HOST, dataFile.getEcauthHost()));
                _log.debug("Using host: {}", hostForSource.getHost());
            }
            final var copy = (Host) hostForSource.clone();
            final var acquisition = HostOption.ACQUISITION.equals(copy.getType());
            final var backup = HostOption.BACKUP.equals(copy.getType());
            final String source;
            if (!backup && (copy.getUseSourcePath() || acquisition)) {
                // This is either a source host or a host for acquisition so we
                // have to set the source with the full original path
                source = dataFile.getSource();
                copy.setDir("");
            } else {
                // This is a backup host on a data mover host so we have to find
                // the correct path on the mover
                source = getPath(dataFile);
            }
            hostsList.append(hostsList.isEmpty() ? "" : ", ").append("Host=").append(copy.getName()).append(" (")
                    .append(copy.getNickname()).append(")");
            final var module = copy.getTransferMethod().getECtransModuleName();
            final var index = dataFile.getIndex();
            try {
                if (index > 0 && !backup && copy.getUseSourcePath()) {
                    // This is an index file
                    if (HOST_ECTRANS.getECtransSetup(copy.getData()).getBoolean(ECtransOptions.HOST_ECTRANS_USEMGET)) {
                        copy.setName(null); // Prevent it to be updated on the
                                            // master!
                        // This is a source host and the file is an index but
                        // the list of files will be processed by the transfer
                        // module directly!
                        final var setup = new ECtransSetup(module, copy.getData());
                        setup.set(ECtransOptions.HOST_ECTRANS_USEMGET, true);
                        copy.setData(setup.getData());
                        final var out = new PipedOutputStream();
                        this.in = new SimpleInputStream(MOVER.get(out, copy, source, posn, dataFile),
                                new PipedInputStream(out, StreamPlugThread.DEFAULT_BUFF_SIZE), copy, source);
                        _log.info("Files in {} will be retrieved from {} (index managed by transfer module {})", source,
                                copy.getNickname(), module);
                        return;
                    }
                    // This is a source host and the file is an index so we
                    // have to retrieve the list of files first!
                    try {
                        final var fileNames = loadFileListFor(copy, source);
                        final var size = fileNames.size();
                        if (size != index) {
                            throw new IOException("Wrong number of files in index (" + size + "!=" + index + ")");
                        }
                        _log.info("Index file {} contains {} name(s)", source, index);
                        this.in = new MultipleInputStream(copy, fileNames, posn, dataFile);
                        _log.info("Files in {} will be retrieved from {}", source, copy.getNickname());
                        return;
                    } catch (final Throwable t) {
                        if (i == hostsForSource.length - 1) {
                            throw new SourceNotAvailableException("Could not load index file on " + hostsList.toString()
                                    + " from DataMover=" + MOVER.getRoot(), t);
                        } else {
                            _log.warn("Could not load index file ({}) on {}", source, hostsList, t);
                        }
                    }
                } else {
                    // This is not an index so we try to get the file directly!
                    final var remoteFileSize = MOVER.size(copy, source);
                    final var fifo = remoteFileSize == 0 && fileSize == -1;
                    final var link = remoteFileSize >= 0 && fileSize == -1;
                    if (fifo || link || remoteFileSize == -1 && acquisition && i == hostsForSource.length - 1
                            || remoteFileSize == fileSize) {
                        if (fifo) {
                            _log.debug("Fifo detected on the remote host (removing {}.hostList)", module);
                            // We have to make sure there is no
                            // ecauth.hostList defined in the Host as with the
                            // fifo we have to connect to the original node (the
                            // fifo are not shared among nodes)!
                            final var setup = new ECtransSetup(module, copy.getData());
                            setup.remove(ECtransOptions.HOST_ECAUTH_HOST_LIST);
                            copy.setData(setup.getData());
                        }
                        final var out = new PipedOutputStream();
                        this.in = new SimpleInputStream(MOVER.get(out, copy, source, posn, dataFile),
                                new PipedInputStream(out, StreamPlugThread.DEFAULT_BUFF_SIZE), copy, source);
                        _log.info("File {} will be retrieved from {}", source, copy.getNickname());
                        return;
                    }
                    if (i == hostsForSource.length - 1) {
                        throw new SourceNotAvailableException("Incorrect size (" + remoteFileSize + " bytes) on "
                                + hostsList.toString() + " from DataMover=" + MOVER.getRoot());
                    } else {
                        _log.warn("Incorrect size ({} bytes) on {}", remoteFileSize, hostsList);
                    }
                }
            } catch (final SourceNotAvailableException t) {
                throwable = t;
                throw t;
            } catch (final Throwable t) {
                throwable = t;
                _log.warn("File {} not retrieved from {}", source, copy.getNickname(), t);
            }
        }

        final var authHost = dataFile.getEcauthHost();
        final var snae = new SourceNotAvailableException(
                "Not retrieved " + (hostsList.length() > 0 ? "using " + hostsList + " on DataMover=" + MOVER.getRoot()
                        : "(hosts list empty)") + (isNotEmpty(authHost) ? " with source " + authHost : ""));
        snae.initCause(throwable);
        throw snae;
    }

    /**
     * Load the list of fileNames for the given index.
     *
     * @param host
     *            the host
     * @param indexFileName
     *            the index file name
     *
     * @return the vector
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static List<String> loadFileListFor(final Host host, final String indexFileName) throws IOException {
        final List<String> result = new ArrayList<>();
        ECproxyCallback callback = null;
        BufferedReader reader = null;
        PipedOutputStream out = null;
        PipedInputStream in = null;
        try {
            out = new PipedOutputStream();
            in = new PipedInputStream(out, StreamPlugThread.DEFAULT_BUFF_SIZE);
            reader = new BufferedReader(new InputStreamReader(in));
            callback = MOVER.get(out, host, indexFileName, 0, null);
            try {
                String line;
                // Read the file in the format:
                // filename
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // The end of the line might include a comment?
                    final var index = line.indexOf("#");
                    if (index != -1) {
                        line = line.substring(0, index).trim();
                    }
                    // Skip the empty lines!
                    if (line.isEmpty()) {
                        continue;
                    }
                    // We have a new file!
                    _log.debug("For index {} -> {}", indexFileName, line);
                    result.add(line);
                }
            } finally {
                // Let's check if the target host didn't report any problem
                // during the upload?
                callback.check();
            }
        } catch (final Throwable t) {
            throw new IOException("Loading index file (" + result.size() + " entries)", t);
        } finally {
            StreamPlugThread.closeQuietly(reader);
            StreamPlugThread.closeQuietly(in);
            StreamPlugThread.closeQuietly(out);
        }
        return result;
    }

    /**
     * Gets the remote host name.
     *
     * @return the remote host name
     */
    String getRemoteHostName() {
        return in.getRemoteHostName();
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public Host getHost() {
        return hostForSource;
    }

    /**
     * Available.
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int available() throws IOException {
        if (cleaner.isCleaned()) {
            throw new IOException("Stream closed");
        }
        return in.available();
    }

    /**
     * Read.
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int read() throws IOException {
        if (cleaner.isCleaned()) {
            throw new IOException("Stream closed");
        }
        return in.read();
    }

    /**
     * Read.
     *
     * @param b
     *            the b
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        if (cleaner.isCleaned()) {
            throw new IOException("Stream closed");
        }
        return in.read(b);
    }

    /**
     * Read.
     *
     * @param b
     *            the b
     * @param off
     *            the off
     * @param len
     *            the len
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (cleaner.isCleaned()) {
            throw new IOException("Stream closed");
        }
        return in.read(b, off, len);
    }

    /**
     * Cleans up resources and terminates the process if necessary.
     *
     * @throws IOException
     *             If an error occurs during cleanup.
     */
    private void cleanup() throws IOException {
        if (in != null)
            in.close();
    }

    /**
     * Closes this stream and performs all associated cleanup.
     *
     * @throws IOException
     *             If an error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        cleaner.close();
    }

    /**
     * The Class SourceInputStream. A class to allow getting the name of the source file. This interface is implemented
     * by the various input streams.
     */
    private abstract static class SourceInputStream extends InputStream {
        /** The _source. */
        private String source;

        /**
         * Sets the source.
         *
         * @param source
         *            the new source
         */
        void setSource(final String source) {
            this.source = source;
        }

        /**
         * Gets the source.
         *
         * @return the source
         */
        String getSource() {
            return source;
        }

        /**
         * Gets the remote host name.
         *
         * @return the remote host name
         */
        String getRemoteHostName() {
            return null;
        }
    }

    /**
     * The Class SimpleInputStream. This stream is used when only one file is to be retrieved (no index).
     */
    private static final class SimpleInputStream extends SourceInputStream {
        /** The host. */
        private final Host host;

        /** The sourceIn. */
        private final InputStream sourceIn;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The callback. */
        private final ECproxyCallback callback;

        /**
         * Instantiates a new simple input stream.
         *
         * @param callback
         *            the callback
         * @param in
         *            the in
         * @param host
         *            the host
         * @param source
         *            the source
         */
        SimpleInputStream(final ECproxyCallback callback, final InputStream in, final Host host, final String source) {
            setSource(source);
            this.callback = callback;
            this.sourceIn = in;
            this.host = host;
        }

        /**
         * Gets the remote host name.
         *
         * @return the remote host name
         */
        @Override
        String getRemoteHostName() {
            return callback.getRemoteHostName();
        }

        /**
         * Available.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int available() throws IOException {
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return sourceIn.available();
        }

        /**
         * Read.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read() throws IOException {
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return sourceIn.read();
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b) throws IOException {
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return sourceIn.read(b);
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         * @param off
         *            the off
         * @param len
         *            the len
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return sourceIn.read(b, off, len);
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (!closed.compareAndSet(false, true)) {
                // This stream was already closed!
                _log.debug("Already closed");
                return;
            }
            try {
                sourceIn.close();
            } finally {
                try {
                    callback.check();
                } catch (final Throwable t) {
                    final var e = new IOException("Could not get source from Host=" + host.getName() + " ("
                            + host.getNickname() + ") : " + t.getMessage());
                    e.initCause(t);
                    throw e;
                }
            }
        }
    }

    /**
     * The Class CachedInputStream. This class is used for each new input stream added to the queue of the multiple
     * input stream class to read data in advance and populate a cache.
     */
    private static final class CachedInputStream extends SourceInputStream {
        /** The exception. */
        private IOException exception;

        /** The input. */
        private final InputStream input;

        /** The pipeOutput. */
        private final PipedOutputStream pipeOutput;

        /** The pipeInput. */
        private final PipedInputStream pipeInput;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /**
         * Instantiates a new cached input stream.
         *
         * @param in
         *            the in
         * @param bufferSize
         *            the buffer size
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        CachedInputStream(final SimpleInputStream in, final int bufferSize) throws IOException {
            setSource(in.getSource());
            this.input = in;
            this.pipeInput = new PipedInputStream(bufferSize);
            this.pipeOutput = new PipedOutputStream(pipeInput);
            new CopyManager().execute();
        }

        /**
         * Available.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int available() throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return pipeInput.available();
        }

        /**
         * Read.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read() throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return pipeInput.read();
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b) throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return pipeInput.read(b);
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         * @param off
         *            the off
         * @param len
         *            the len
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return pipeInput.read(b, off, len);
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (!closed.compareAndSet(false, true)) {
                // This stream was already closed!
                _log.debug("Already closed");
                return;
            }
            if (exception != null) {
                StreamPlugThread.closeQuietly(pipeInput);
                StreamPlugThread.closeQuietly(pipeOutput);
                throw exception;
            }
            try {
                pipeInput.close();
            } finally {
                pipeOutput.close();
            }
        }

        /**
         * The Class CopyManager.
         */
        private final class CopyManager extends ConfigurableRunnable {

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                var success = false;
                try {
                    StreamPlugThread.copy(pipeOutput, input, StreamPlugThread.DEFAULT_BUFF_SIZE);
                    pipeOutput.close();
                    input.close();
                    success = true;
                } catch (final Throwable t) {
                    exception = t instanceof final IOException ioException ? ioException : new IOException(t);
                } finally {
                    if (!success) {
                        StreamPlugThread.closeQuietly(pipeOutput);
                        StreamPlugThread.closeQuietly(input);
                    }
                }
            }
        }
    }

    /**
     * The Class MultipleInputStream. This Class is used when the source is an index file (multiple files will be
     * retrieved).
     */
    private static final class MultipleInputStream extends SourceInputStream {
        /** The inputs. */
        private final ArrayBlockingQueue<SourceInputStream> inputs;

        /** The exception. */
        private IOException exception;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The host. */
        private final Host host;

        /** The dataFile. */
        private final DataFile dataFile; //

        /** The list of data files to retrieve. */
        private final List<String> fileNames;

        /** The pipe output. */
        private final PipedOutputStream pipeOutput;

        /** The aggregated input stream to read data from. */
        private final PipedInputStream pipeInput;

        /** The nick name. */
        private final String nickName;

        /**
         * The thread which read from the various input streams and populate the main input stream.
         */
        private final TransferManager manager;

        /** The thread which create the list of input streams. */
        private final ConnectionsPool pool;

        /** The _retry count. */
        private final int retryCount;

        /** The _retry frequency. */
        private final int retryFrequency;

        /** The _use cache. */
        private final boolean useCache;

        /** The _cache size. */
        private final int cacheSize;

        /** The _cache size. */
        private final int total;

        /**
         * Instantiates a new multiple input stream.
         *
         * @param host
         *            the host
         * @param fileNames
         *            the file names
         * @param posn
         *            the posn
         * @param dataFile
         *            the data file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        MultipleInputStream(final Host host, final List<String> fileNames, final long posn, final DataFile dataFile)
                throws IOException {
            final var options = HOST_ECTRANS.getECtransSetup(host.getData())
                    .getOptions(ECtransOptions.HOST_ECTRANS_MULTIPLE_INPUT_STREAM);
            this.retryCount = options.get("retryCount", 1);
            this.retryFrequency = options.get("retryFrequency", 1000);
            this.useCache = options.get("useCache", false);
            this.cacheSize = options.get("cacheSize", 10 * StreamPlugThread.DEFAULT_BUFF_SIZE);
            this.inputs = new ArrayBlockingQueue<>(options.get("queueSize", 3), true);
            this.dataFile = dataFile;
            this.pipeOutput = new PipedOutputStream();
            this.pipeInput = new PipedInputStream(pipeOutput, StreamPlugThread.DEFAULT_BUFF_SIZE);
            this.host = host;
            this.fileNames = fileNames;
            this.nickName = host.getNickname();
            this.total = fileNames.size();
            (this.pool = new ConnectionsPool()).execute();
            (this.manager = new TransferManager()).execute();
            if (posn > 0) {
                // We need to skip the first part of the file (could be improved for better
                // performances)
                pipeInput.skip(posn);
            }
        }

        /**
         * The Class ConnectionsPool. The purpose of this Thread is to populate the inputs queue with all the input
         * streams required to retrieve the chunks of the file.
         */
        private final class ConnectionsPool extends ConfigurableRunnable {

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                var success = false;
                var index = 0;
                try {
                    // Let's go through each file in the index file and create
                    // the input stream for the retrieval!
                    for (final String source : fileNames) {
                        // New file!
                        SimpleInputStream in;
                        var start = System.currentTimeMillis();
                        var retry = 0;
                        index++;
                        while (true) {
                            // Let's check if the TransferManager did not set an
                            // error?
                            if (exception != null) {
                                _log.warn("TransferManager failed", exception);
                                return;
                            }
                            // If we are closed then let's end the Thread!
                            if (closed.get()) {
                                _log.warn("ConnectionsPool ends (stream is closed)");
                                return;
                            }
                            // Now we try to create the simple input stream!
                            _log.info("Create stream for file {} {} on {}", index, source, nickName);
                            try {
                                final var pout = new PipedOutputStream();
                                final var pin = new PipedInputStream(pout, StreamPlugThread.DEFAULT_BUFF_SIZE);
                                // If this is the last filename then we have to provide the dataFile to the
                                // SimpleInputStream to allow the ECtransGet to provide a notification if
                                // required!
                                in = new SimpleInputStream(
                                        MOVER.get(pout, host, source, 0, index == fileNames.size() ? dataFile : null),
                                        pin, host, source);
                                break;
                            } catch (final Throwable t) {
                                if (retry++ >= retryCount) {
                                    // We have done all the retry requested
                                    // without success!
                                    _log.warn("Every retry failed");
                                    throw t;
                                }
                                // Let's wait for some time and then retry!
                                _log.warn("Retry in {}", Format.formatDuration(retryFrequency), t);
                                try {
                                    Thread.sleep(retryFrequency);
                                } catch (final InterruptedException e) {
                                    // We just continue!
                                }
                            }
                        }
                        if (_log.isInfoEnabled())
                            _log.info("Add {}stream for file {} {} on {} to the queue (wait: {})",
                                    (useCache ? "cached " : ""), index, source, nickName,
                                    Format.formatDuration(start, System.currentTimeMillis()));
                        // Push it into the queue!
                        start = System.currentTimeMillis();
                        inputs.put(useCache ? new CachedInputStream(in, cacheSize) : in);
                        if (_log.isDebugEnabled())
                            _log.debug("Queued (wait: {}, size: {})",
                                    Format.formatDuration(start, System.currentTimeMillis()), inputs.size());
                    }
                    // We have opened all the streams available!
                    success = true;
                } catch (final Throwable t) {
                    _log.warn("Could not create all input streams using {}", nickName, t);
                    if (exception == null) {
                        // No previous exception was thrown in the other Thread
                        // so we have to set the exception!
                        exception = t instanceof final IOException ioException ? ioException : new IOException(t);
                        // To be sure the other Thread which is waiting for the
                        // data on the input stream will be notified of the
                        // error!
                        manager.interrupt();
                    } else {
                        // There was already an exception thrown in the
                        // TransferManager Thread!
                        _log.debug("Exception ignored (use original exception)");
                    }
                } finally {
                    if (success) {
                        _log.info("{} input stream(s) created using {}", index, nickName);
                    } else {
                        _log.warn("Only {}/{} input stream(s) created using {}", index, total, nickName);
                    }
                }
            }
        }

        /**
         * The Class TransferManager. The purpose of this Thread is to process all the input streams created by the
         * connections pool Thread.
         */
        private final class TransferManager extends ConfigurableRunnable {

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                var success = false;
                var count = 0;
                var length = 0L;
                try {
                    final var start = System.currentTimeMillis();
                    // Let's go through each input stream to retrieve the full
                    // content of the target file!
                    for (var index = 1; index <= total; index += 1) {
                        // Let's check if the ConnectionsPool did not set an
                        // error?
                        if (exception != null) {
                            _log.debug("ConnectionsPool failed", exception);
                            return;
                        }
                        // If we are closed then let's end the Thread!
                        if (closed.get()) {
                            _log.warn("TransferManager ends (stream is closed)");
                            return;
                        }
                        // Let's take a new stream from the queue!
                        final var taketime = System.currentTimeMillis();
                        final var in = inputs.take();
                        if (_log.isInfoEnabled())
                            _log.info("Retrieve file {} {} from {} (wait: {})", index, in.getSource(), nickName,
                                    Format.formatDuration(taketime, System.currentTimeMillis()));
                        var completed = false;
                        try {
                            // Now we read all the data from this file!
                            length += StreamPlugThread.copy(pipeOutput, in, StreamPlugThread.DEFAULT_BUFF_SIZE);
                            in.close();
                            completed = true;
                            count++;
                        } finally {
                            if (!completed) {
                                // Either the copy or the close have failed, but
                                // by using the quiet close we make sure that we
                                // keep the initial exception!
                                StreamPlugThread.closeQuietly(in);
                            }
                        }
                        if (_log.isInfoEnabled())
                            _log.info("File {} {} retrieved from {} in {}", index, in.getSource(), nickName,
                                    Format.formatDuration(start, System.currentTimeMillis()));
                    }
                    // Make sure the read will not wait unnecessarily!
                    pipeOutput.close();
                    success = true;
                    if (dataFile.getDeleteOriginal()) {
                        // We are requested to delete all the source files!
                        for (final String source : fileNames) {
                            try {
                                MOVER.del(host, source);
                            } catch (final Throwable t) {
                                // We don't want to fail the all process if the
                                // deletion fail!
                                _log.warn("Could not delete file {} from {}", source, nickName, t);
                            }
                        }
                    }
                } catch (final Throwable t) {
                    _log.warn("Could not read all files using {}", nickName, t);
                    if (exception == null) {
                        // This exception was thrown in this Thread so we have
                        // to set the exception!
                        exception = t instanceof final IOException ioException ? ioException : new IOException(t);
                        // To be sure the other Thread which is waiting to add
                        // more input streams will be notified of the error!
                        pool.interrupt();
                    } else {
                        // There was already an exception thrown in the
                        // ConnectionsPool Thread!
                        _log.debug("Exception ignored (use original exception)");
                    }
                } finally {
                    if (success) {
                        _log.info("{} file(s) retrieved from {} ({} bytes)", total, nickName, length);
                    } else {
                        _log.warn("Only {}/{} file(s) retrieved from {} ({} bytes)", count, total, nickName, length);
                        if (closed.compareAndSet(false, true)) {
                            // Force the Thread using this input stream to fail!
                            StreamPlugThread.closeQuietly(pipeOutput);
                            StreamPlugThread.closeQuietly(pipeInput);
                        } else {
                            _log.debug("Already closed");
                        }
                        // We have to make sure there are no input streams
                        // left open in the queue?
                        SourceInputStream in;
                        var index = 0;
                        while ((in = inputs.poll()) != null) {
                            StreamPlugThread.closeQuietly(in);
                            index++;
                        }
                        // How many streams did we close?
                        _log.debug("Closed {} stream(s)", index);
                    }
                }
            }
        }

        /**
         * Available.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int available() throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return pipeInput.available();
        }

        /**
         * Read.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read() throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return pipeInput.read();
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b) throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return pipeInput.read(b);
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         * @param off
         *            the off
         * @param len
         *            the len
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (exception != null) {
                throw exception;
            }
            if (closed.get()) {
                throw new IOException("Stream closed");
            }
            return pipeInput.read(b, off, len);
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (!closed.compareAndSet(false, true)) {
                // This stream was already closed!
                _log.debug("Already closed");
                return;
            }
            if (exception != null) {
                StreamPlugThread.closeQuietly(pipeOutput);
                StreamPlugThread.closeQuietly(pipeInput);
                throw exception;
            }
            try {
                pipeOutput.close();
            } finally {
                pipeInput.close();
            }
        }
    }
}
