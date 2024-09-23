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

package ecmwf.ecpds.mover.plugin.ecproxy;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.ecpds.master.DataFilePath.getDataFileId;

import java.io.FilterInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.AbstractTicket;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.plugin.SimplePlugin;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.GenericFile;
import ecmwf.common.technical.MonitoredInputStream;
import ecmwf.common.technical.ProgressHandler;
import ecmwf.common.technical.ProgressInterface;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThreadService;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.common.version.Version;
import ecmwf.ecpds.master.DownloadProgress;
import ecmwf.ecpds.mover.ECaccessTicket;
import ecmwf.ecpds.mover.FileDescriptorTicket;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class ECproxyPlugin.
 */
public final class ECproxyPlugin extends SimplePlugin {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECproxyPlugin.class);

    /** The Constant _NAME. */
    private static final String _NAME = "ECproxyPlugin";

    /** The Constant _VERSION. */
    private static final String _VERSION = Version.getFullVersion();

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /** The _target. */
    private GenericFile _target = null;

    /** The _data file id. */
    private long _dataFileId = -1;

    /** The _size. */
    private long _size = -1;

    /** The _duration. */
    private long _duration = -1;

    /** The _start. */
    private long _start = -1;

    /**
     * Instantiates a new e cproxy plugin.
     *
     * @param name
     *            the name
     * @param params
     *            the params
     * @param socket
     *            the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public ECproxyPlugin(final String name, final Map<String, String> params, final Socket socket) throws IOException {
        super(name, params, socket);
        setLoop(true);
    }

    /**
     * Instantiates a new e cproxy plugin.
     *
     * @param name
     *            the name
     * @param params
     *            the params
     */
    public ECproxyPlugin(final String name, final Map<String, String> params) {
        super(name, params);
    }

    /**
     * {@inheritDoc}
     *
     * Get instance of current class.
     */
    @Override
    public ConfigurableRunnable newInstance(final String ref, final Map<String, String> params, final Socket socket)
            throws IOException {
        return new ECproxyPlugin(ref, params, socket);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the plugin name.
     */
    @Override
    public String getPluginName() {
        return _NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort() {
        return Integer.parseInt(Cnf.at("ECproxyPlugin", "port"));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the version.
     */
    @Override
    public String getVersion() {
        return _VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * Customize socket.
     */
    @Override
    public void customizeSocket(final Socket socket) throws IOException {
        final var sendBufferSize = Cnf.at("ECproxyPlugin", "sendBufferSize", -1);
        if (sendBufferSize > 0) {
            socket.setSendBufferSize(sendBufferSize);
        }
        final var receiveBufferSize = Cnf.at("ECproxyPlugin", "receiveBufferSize", -1);
        if (receiveBufferSize > 0) {
            socket.setReceiveBufferSize(receiveBufferSize);
        }
        socket.setKeepAlive(Cnf.at("ECproxyPlugin", "keepAlive", socket.getKeepAlive()));
        socket.setTcpNoDelay(Cnf.at("ECproxyPlugin", "tcpNoDelay", socket.getTcpNoDelay()));
        _log.debug("Socket created (sendBufferSize: " + socket.getSendBufferSize() + ", receiveBufferSize: "
                + socket.getReceiveBufferSize() + ", keepAlive: " + socket.getKeepAlive() + ", tcpNoDelay: "
                + socket.getTcpNoDelay() + "): " + socket);
    }

    /**
     * Version req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void versionReq(final String[] parameters) throws ParameterException {
    }

    /**
     * Ecpds req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void ecpdsReq(final String[] parameters) throws ParameterException {
    }

    /**
     * Host req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void hostReq(final String[] parameters) throws ParameterException {
    }

    /**
     * Target req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParameterException
     *             the parameter exception
     */
    public void targetReq(final String[] parameters) throws IOException, ParameterException {
        final var targetName = getParameter(parameters);
        _target = _mover.getGenericFile(targetName);
        _dataFileId = getDataFileId(targetName);
        send("CONNECT 1");
    }

    /**
     * Source req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParameterException
     *             the parameter exception
     */
    public void sourceReq(final String[] parameters) throws IOException, ParameterException {
        send("PUT LOCAL");
    }

    /**
     * Size req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParameterException
     *             the parameter exception
     */
    public void sizeReq(final String[] parameters) throws IOException, ParameterException {
        _size = Long.parseLong(getParameter(parameters));
        putReq();
        send("STAT +Transfer successful (" + _mover.getRoot() + "|" + _start + "|" + _duration + ")");
        send("BYE");
        if (_size == -1) {
            // Connection from the ECpdsClient (not the ecpds command)!
            setLoop(false);
        }
    }

    /**
     * Bye req.
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void byeReq() throws ParameterException {
        setLoop(false);
    }

    /**
     * Puts the req.
     */
    public void putReq() {
        if (_target == null) {
            _log.error("Missing parameters");
            error("Missing parameters");
        } else {
            final var fakeWrite = getOpts("ecproxy.fakeWrite", false);
            var in = getInputStream();
            try {
                _start = System.currentTimeMillis();
                if (fakeWrite) {
                    _log.warn("Faking write (file not created), size: " + Format.formatSize(_size));
                    final var buff = new byte[StreamPlugThread.DEFAULT_BUFF_SIZE];
                    long realSize = 0, readSize = 0, leftOver = _size;
                    while ((readSize = in.read(buff, 0,
                            leftOver == -1 || leftOver > buff.length ? buff.length : (int) leftOver)) != -1) {
                        leftOver -= readSize;
                        realSize += readSize;
                        if (_size != -1 && realSize >= _size) {
                            break;
                        }
                    }
                    if (_size != -1 && _size != realSize) {
                        _log.warn("File size not as expected: " + realSize + " bits (instead of " + _size + " bits )");
                        throw new IOException("Incorrect file size (delta=" + (_size - realSize) + ")");
                    }
                    _duration = System.currentTimeMillis() - _start;
                } else {
                    // TODO: if the size of the expected data is -1 then it
                    // means the data comes from the ECpdsClient and not the
                    // ecpds command line. In this case the end of the data is
                    // recognised by the shutdownOutput of the Socket but this
                    // can cause problems on some platforms. I should rather use
                    // an end-of-data packet!
                    in = new FilterInputStream(in) {
                        @Override
                        public void close() throws IOException {
                            _log.debug("The underlying InputStream will be closed by the Plugin container");
                        }
                    };
                    // If required let's monitor the download of the file
                    // through a monitored stream and the download repository as
                    // for the download method!
                    final var downloadRepository = _mover.getDownloadRepository();
                    final var currentThread = Thread.currentThread();
                    final var completed = new StringBuilder("0");
                    long delta = -1;
                    if (_dataFileId != -1 && (delta = Cnf.durationAt("RetrievalInputStream", "monitored", -1)) > 0) {
                        _log.debug(
                                "Using MonitoredInputStream for donwload (delta=" + Format.formatDuration(delta) + ")");
                        in = new MonitoredInputStream(in, delta, new ProgressHandler() {
                            @Override
                            public long getDelay() {
                                return 2 * Timer.ONE_SECOND;
                            }

                            @Override
                            public void update(final ProgressInterface monitor) {
                                // If the current thread is completed then we
                                // don't need to store the download progress in
                                // the repository.
                                if (completed.charAt(0) == '1' || !currentThread.isAlive()) {
                                    _log.debug("Current retrieval expired for DataFile-" + _dataFileId);
                                    downloadRepository.removeKey(_dataFileId);
                                    return;
                                }
                                // It is still running!
                                final var progress = downloadRepository.getValue(_dataFileId);
                                if (progress != null) {
                                    // Yes! we can update it.
                                    progress.setDuration(monitor.getDuration());
                                    progress.setByteSent(monitor.getByteSent());
                                } else // Do we need to record it?
                                if (_size == -1 || monitor.getByteSent() < _size) {
                                    _log.debug("Creating new DownloadProgress for DataFile-" + _dataFileId);
                                    downloadRepository.put(new DownloadProgress(_mover.getRoot(), _dataFileId, _size,
                                            monitor.getDuration(), monitor.getByteSent(), () -> {
                                                // If the receiving is completed then no need to interrupt! This
                                                // method is called when the Download repository is cleared
                                                // (e.g. when the Data Mover disconnect from the Master).
                                                if (currentThread.isAlive() && completed.charAt(0) == '0') {
                                                    _log.debug("Still receiving data (interrupt)");
                                                    currentThread.interrupt();
                                                } else {
                                                    _log.debug("No interrupt required");
                                                }
                                            }));
                                }
                            }
                        });
                    }
                    try {
                        _duration = _target.receiveFile(in, _size);
                        completed.setCharAt(0, '1');
                    } finally {
                        // Let's make sure the download is removed from the
                        // download repository and the monitored input stream is
                        // properly closed!
                        if (delta > 0) {
                            StreamPlugThread.closeQuietly(in);
                            downloadRepository.removeKey(_dataFileId);
                        }
                    }
                    _log.debug("File received");
                }
            } catch (final Throwable t) {
                _log.error("Process aborted", t);
                final var error = t.getMessage();
                error(error == null ? "aborted by server" : error);
                setLoop(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Parses the command.
     */
    @Override
    public String parseCommand(final String command) {
        if (command.length() == 11 && command.startsWith("T")) {
            try {
                return "ticket " + Long.parseLong(command.substring(1));
            } catch (final NumberFormatException e) {
            }
        }
        return super.parseCommand(command);
    }

    /**
     * _setup so timeout.
     *
     * @param socket
     *            the socket
     */
    private static void _setupSOTimeout(final Socket socket) {
        try {
            socket.setSoTimeout(Cnf.at(_NAME, "SocketSoTimeout", 60000));
        } catch (final Throwable t) {
            _log.debug("SoTimeout error", t);
        }
    }

    /**
     * Ticket req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws java.lang.Exception
     *             the exception
     */
    public void ticketReq(final String[] parameters) throws Exception {
        final var ticket = Long.parseLong(getParameter(parameters));
        _log.debug("Ticket received: " + ticket);
        final var genericTicket = _mover.getTicketRepository().get(ticket);
        if (genericTicket != null) {
            if (genericTicket instanceof ECaccessTicket) {
                ThreadService.setCookieIfNotAlreadySet("ECaccessTicket-" + ticket);
                final var ectransTicket = (ECaccessTicket) genericTicket;
                final var proxy = ectransTicket.getECpdsProxy();
                final var socket = getSocket();
                _setupSOTimeout(socket);
                ectransTicket.toClose(socket);
                try {
                    final var in = getInputStream();
                    final var out = getOutputStream();
                    if (ectransTicket.getMode() == ECaccessTicket.OUTPUT) {
                        _log.debug("Output for ECaccessTicket");
                        out.write("0".getBytes());
                        out.flush();
                        // socket.shutdownInput();
                        proxy.setOutputStream(out);
                    } else {
                        _log.debug("Input for ECaccessTicket");
                        out.write("1".getBytes());
                        out.flush();
                        // socket.shutdownOutput();
                        proxy.setInputStream(in);
                    }
                    setCloseOnExit(false);
                    setLoop(false);
                } catch (final IOException e) {
                    final var message = "Processing ticket " + ticket;
                    StreamPlugThread.closeQuietly(socket);
                    ectransTicket.setError(message, e);
                    ectransTicket.completed();
                    _log.warn(message, e);
                }
            } else if (genericTicket instanceof FileDescriptorTicket) {
                ThreadService.setCookieIfNotAlreadySet("FileDescriptorTicket-" + ticket);
                _log.debug("Input for FileDescriptorTicket");
                final var descTicket = (FileDescriptorTicket) genericTicket;
                final var desc = descTicket.getFileDescriptor();
                final var socket = getSocket();
                _setupSOTimeout(socket);
                descTicket.toClose(socket);
                try {
                    final var out = getOutputStream();
                    out.write("0".getBytes());
                    out.flush();
                    // socket.shutdownInput();
                    final var in = desc.getInputStream();
                    @SuppressWarnings("resource")
                    final var plug = new StreamPlugThread(in, out);
                    plug.toClose(in);
                    plug.toClose(out);
                    plug.toClose(new AbstractTicket.CompleteTicket(descTicket));
                    setCloseOnExit(false);
                    setLoop(false);
                    plug.execute();
                } catch (final IOException e) {
                    final var message = "Processing ticket " + ticket;
                    StreamPlugThread.closeQuietly(socket);
                    descTicket.setError(message, e);
                    descTicket.completed();
                    _log.warn(message, e);
                }
            } else {
                _log.error("Ticket not valid (" + ticket + ")");
            }
        } else {
            _log.error("Descriptor not found (" + ticket + ")");
        }
    }
}
