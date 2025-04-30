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

package ecmwf.common.ftp;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.transport.ptcp.psocket.PTCPServerSocket;
import ecmwf.common.transport.ptcp.psocket.PTCPSocket;

/**
 * The Class FtpClient.
 */
public final class FtpClient {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(FtpClient.class);

    /** The _keep alive thread. */
    private static KeepControlConnectionAliveThread _keepAliveThread = null;

    /** The Constant FTP_PORT. */
    public static final int FTP_PORT = 21;

    /** The Constant FTP_SUCCESS. */
    private static final int FTP_SUCCESS = 1;

    /** The Constant FTP_TRY_AGAIN. */
    private static final int FTP_TRY_AGAIN = 2;

    /** The Constant FTP_ERROR. */
    private static final int FTP_ERROR = 3;

    /** The Constant _lock. */
    private static final Object _lock = new Object();

    /** The tcp no delay. */
    private Boolean _tcpNoDelay = null;

    /** The tcp keep alive. */
    private Boolean _tcpKeepAlive = null;

    /** The tcp congestion. */
    private String _tcpCongestion = null;

    /** The so max pacing rate. */
    private Integer _soMaxPacingRate = null;

    /** The tcp max segment. */
    private Integer _tcpMaxSegment = null;

    /** The tcp time stamp. */
    private Boolean _tcpTimeStamp = null;

    /** The tcp window clamp. */
    private Integer _tcpWindowClamp = null;

    /** The tcp keep alive time. */
    private Integer _tcpKeepAliveTime = null;

    /** The tcp keep alive interval. */
    private Integer _tcpKeepAliveInterval = null;

    /** The tcp keep alive probes. */
    private Integer _tcpKeepAliveProbes = null;

    /** The tcp linger enable. */
    private Boolean _tcpLingerEnable = null;

    /** The tcp linger time. */
    private Integer _tcpLingerTime = null;

    /** The tcp user timeout. */
    private Integer _tcpUserTimeout = null;

    /** The tcp quick ack. */
    private Boolean _tcpQuickAck = null;

    /** The _debug. */
    private boolean _debug = false;

    /** The _data socket. */
    private DataSocket _dataSocket = null;

    /** The _reply pending. */
    private boolean _replyPending = false;

    /** The _logged. */
    private boolean _logged = false;

    /** The _last command. */
    private String _lastCommand = null;

    /** The _noop command. */
    private String _noopCommand = null;

    /** The _last reply code. */
    private int _lastReplyCode = -1;

    /** The _server response. */
    private List<String> _serverResponse = new ArrayList<>(1);

    /** The _server socket. */
    private Socket _serverSocket = null;

    /** The _server output. */
    private PrintWriter _serverOutput = null;

    /** The _server input. */
    private InputStream _serverInput = null;

    /** The _output filters. */
    private boolean _outputFilters = false;

    /** The _passive. */
    private boolean _passive = true;

    /** The _extended. */
    private boolean _extended = false;

    /** The _packet. */
    private boolean _packet = false;

    /** The _shared passive. */
    private boolean _sharedPassive = false;

    /** The _data alive. */
    private boolean _dataAlive = false;

    /** The _low port. */
    private boolean _lowPort = false;

    /** The _comm time out. */
    private int _commTimeOut = 60000;

    /** The _data time out. */
    private int _dataTimeOut = 60000;

    /** The _port time out. */
    private int _portTimeOut = 60000;

    /** The _listen address. */
    private String _listenAddress = null;

    /** The _send buffer size. */
    private int _sendBufferSize = -1;

    /** The _receive buffer size. */
    private int _receiveBufferSize = -1;

    /** The _mkdirs. */
    private boolean _mkdirs = false;

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /** The _host. */
    private String _host = null;

    /** The _port. */
    private int _port = -1;

    /** The _user. */
    private String _user = null;

    /** The _password. */
    private String _password = null;

    /** The _password. */
    private ClientSocketStatistics _statistics = null;

    /**
     * Instantiates a new ftp client.
     */
    public FtpClient() {
        // All options will be set using the setters before calling one of the connect
        // methods.
    }

    /**
     * Connect.
     *
     * @param host
     *            the host
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void connect(final String host) throws IOException {
        final var hostAndPort = host.split(":");
        _openServer(hostAndPort[0], hostAndPort.length == 2 ? Integer.parseInt(hostAndPort[1]) : FTP_PORT);
    }

    /**
     * Connect.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void connect(final String host, final int port) throws IOException {
        _openServer(host, port);
    }

    /**
     * Retry login.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void retryLogin() throws IOException {
        if (_host == null || _port == -1 || _user == null) {
            throw new IOException("Did not login successfully before");
        }
        _openServer(_host, _port);
        login(_user, _password);
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "Noop: " + _noopCommand + ", Passive: " + _passive + ", Extended: " + _extended + ", SharedPassive: "
                + _sharedPassive + ", DataAlive: " + _dataAlive + ", LowPort: " + _lowPort + ", CommTimeOut: "
                + _commTimeOut + ", DataTimeOut: " + _dataTimeOut + ", PortTimeOut: " + _portTimeOut
                + ", ListenAddress: " + _listenAddress + ", SendBufferSize: " + _sendBufferSize
                + ", ReceiveBufferSize: " + _receiveBufferSize;
    }

    /**
     * Set a client socket statistics to gather socket statistics (ss output).
     *
     * @param statistics
     *            the client socket statistics
     */
    public void setClientSocketStatistics(final ClientSocketStatistics statistics) {
        _statistics = statistics;
    }

    /**
     * Set a TCP option.
     *
     * @param tcpCongestion
     *            the TCP congestion algorithm name
     */
    public void setTCPCongestion(final String tcpCongestion) {
        this._tcpCongestion = tcpCongestion;
    }

    /**
     * Set a SO option.
     *
     * @param soMaxPacingRate
     *            set the maximum transmit rate in bytes per second for the socket.
     */
    private void setSOMaxPacingRate(final Integer soMaxPacingRate) {
        this._soMaxPacingRate = soMaxPacingRate;
    }

    /**
     * Set a SO option.
     *
     * @param soMaxPacingRate
     *            set the maximum transmit rate in bytes per second for the socket.
     */
    public void setSOMaxPacingRate(final ByteSize soMaxPacingRate) {
        final var pacingRateInBytes = soMaxPacingRate.size();
        if (pacingRateInBytes > 0 && pacingRateInBytes < Integer.MAX_VALUE) {
            setSOMaxPacingRate((int) pacingRateInBytes);
        }
    }

    /**
     * Set a TCP option.
     *
     * @param tcpMaxSegment
     *            maximum amount of data that can be sent in a single TCP segment.
     */
    public void setTCPMaxSegment(final Integer tcpMaxSegment) {
        this._tcpMaxSegment = tcpMaxSegment;
    }

    /**
     * Set a TCP option.
     *
     * @param tcpTimeStamp
     *            enables or disables the use of timestamps in TCP packets
     */
    public void setTCPTimeStamp(final Boolean tcpTimeStamp) {
        this._tcpTimeStamp = tcpTimeStamp;

    }

    /**
     * Set a TCP option.
     *
     * @param tcpWindowClamp
     *            bound the size of the advertised window to this value
     */
    public void setTCPWindowClamp(final Integer tcpWindowClamp) {
        this._tcpWindowClamp = tcpWindowClamp;
    }

    /**
     * Set a TCP option.
     *
     * @param tcpKeepAliveTime
     *            the interval between the last data packet sent (simple ACKs are not considered data) and the first
     *            keepalive probe; after the connection is marked to need keepalive, this counter is not used any
     *            further
     */
    public void setTCPKeepAliveTime(final Integer tcpKeepAliveTime) {
        this._tcpKeepAliveTime = tcpKeepAliveTime;
    }

    /**
     * Set a TCP option.
     *
     * @param tcpKeepAliveInterval
     *            the interval between subsequential keepalive probes, regardless of what the connection has exchanged
     *            in the meantime
     */
    public void setTCPKeepAliveInterval(final Integer tcpKeepAliveInterval) {
        this._tcpKeepAliveInterval = tcpKeepAliveInterval;
    }

    /**
     * Set a TCP option.
     *
     * @param tcpKeepAliveProbes
     *            the number of unacknowledged probes to send before considering the connection dead and notifying the
     *            application layer
     */
    public void setTCPKeepAliveProbes(final Integer tcpKeepAliveProbes) {
        this._tcpKeepAliveProbes = tcpKeepAliveProbes;
    }

    /**
     * Controls the behavior of a socket when it is closed and there is unsent data. It specifies whether the socket
     * should linger for a specified amount of time before closing. The TCP_LINGER option is often used to ensure that
     * all data is sent before the socket is closed.
     *
     * @param tcpLingerEnable
     *            the tcp linger enable
     * @param tcpLingerTime
     *            the amount of time, in seconds, the socket should linger before closing
     */
    public void setTCPLinger(final Boolean tcpLingerEnable, final Integer tcpLingerTime) {
        this._tcpLingerEnable = tcpLingerEnable;
        this._tcpLingerTime = tcpLingerTime;
    }

    /**
     * Provides a way to control the timeout for unacknowledged data on a TCP connection.
     *
     * @param tcpUserTimeout
     *            maximum amount of time, in milliseconds, that transmitted data may remain unacknowledged before an
     *            error is returned
     */
    public void setTCPUserTimeout(final Integer tcpUserTimeout) {
        this._tcpUserTimeout = tcpUserTimeout;
    }

    /**
     * Controls whether the TCP stack should quickly acknowledge incoming data.
     *
     * @param tcpQuickAck
     *            when enabled, the TCP stack sends immediate acknowledgment for incoming data without waiting for the
     *            delayed acknowledgment timer.
     */
    public void setTCPQuickAck(final Boolean tcpQuickAck) {
        this._tcpQuickAck = tcpQuickAck;
    }

    /**
     * Controls whether the TCP no delay option should be set.
     *
     * @param tcpNoDelay
     *            tcp no delay.
     */
    public void setTCPNoDelay(final Boolean tcpNoDelay) {
        this._tcpNoDelay = tcpNoDelay;
    }

    /**
     * Controls whether the TCP keep alive option should be set.
     *
     * @param tcpKeepAlive
     *            the new TCP keep alive
     */
    public void setTCPKeepAlive(final Boolean tcpKeepAlive) {
        this._tcpKeepAlive = tcpKeepAlive;
    }

    /**
     * Apply all the provided TCP options to the underlying socket configuration.
     *
     * @param socketConfig
     *            the socket config
     */
    private void applyTCPOptions(final SocketConfig socketConfig) {
        if (_tcpNoDelay != null) {
            socketConfig.setTcpNoDelay(_tcpNoDelay);
        }
        if (_tcpKeepAlive != null) {
            socketConfig.setKeepAlive(_tcpKeepAlive);
        }
        if (_tcpCongestion != null && !_tcpCongestion.isBlank()) {
            socketConfig.setTCPCongestion(_tcpCongestion);
        }
        if (_soMaxPacingRate != null && _soMaxPacingRate > 0) {
            socketConfig.setSOMaxPacingRate(_soMaxPacingRate);
        }
        if (_tcpMaxSegment != null && _tcpMaxSegment > 0) {
            socketConfig.setTCPMaxSegment(_tcpMaxSegment);
        }
        if (_tcpTimeStamp != null) {
            socketConfig.setTCPTimeStamp(_tcpTimeStamp);
        }
        if (_tcpWindowClamp != null && _tcpWindowClamp > 0) {
            socketConfig.setTCPWindowClamp(_tcpWindowClamp);
        }
        if (_tcpKeepAliveTime != null && _tcpKeepAliveTime > 0) {
            socketConfig.setTCPKeepAliveTime(_tcpKeepAliveTime);
        }
        if (_tcpKeepAliveInterval != null && _tcpKeepAliveInterval > 0) {
            socketConfig.setTCPKeepAliveInterval(_tcpKeepAliveInterval);
        }
        if (_tcpKeepAliveProbes != null && _tcpKeepAliveProbes > 0) {
            socketConfig.setTCPKeepAliveProbes(_tcpKeepAliveProbes);
        }
        if (_tcpLingerEnable != null && _tcpLingerTime != null && _tcpLingerTime > 0) {
            socketConfig.setTCPLinger(_tcpLingerEnable, _tcpLingerTime);
        }
        if (_tcpUserTimeout != null && _tcpUserTimeout > 0) {
            socketConfig.setTCPUserTimeout(_tcpUserTimeout);
        }
        if (_tcpQuickAck != null) {
            socketConfig.setTCPQuickAck(_tcpQuickAck);
        }
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return _host;
    }

    /**
     * Sets the comm time out.
     *
     * @param commTimeOut
     *            the new comm time out
     */
    public void setCommTimeOut(final int commTimeOut) {
        if (_debug) {
            _log.debug("set commTimeOut: {}", commTimeOut);
        }
        _commTimeOut = commTimeOut;
    }

    /**
     * Sets the listen address.
     *
     * @param listenAddress
     *            the new listen address
     */
    public void setListenAddress(final String listenAddress) {
        if (_debug) {
            _log.debug("set listenAddress: {}", listenAddress);
        }
        _listenAddress = listenAddress;
    }

    /**
     * Sets the sends the buffer size.
     *
     * @param sendBufferSize
     *            the new sends the buffer size
     */
    public void setSendBufferSize(final int sendBufferSize) {
        if (_debug) {
            _log.debug("set sendBufferSize: {}", sendBufferSize);
        }
        _sendBufferSize = sendBufferSize;
    }

    /**
     * Sets the receive buffer size.
     *
     * @param receiveBufferSize
     *            the new receive buffer size
     */
    public void setReceiveBufferSize(final int receiveBufferSize) {
        if (_debug) {
            _log.debug("set receiveBufferSize: {}", receiveBufferSize);
        }
        _receiveBufferSize = receiveBufferSize;
    }

    /**
     * Sets the mkdirs.
     *
     * @param mkdirs
     *            the new mkdirs
     */
    public void setMkdirs(final boolean mkdirs) {
        if (_debug) {
            _log.debug("set mkdirs: {}", mkdirs);
        }
        _mkdirs = mkdirs;
    }

    /**
     * Sets the data time out.
     *
     * @param dataTimeOut
     *            the new data time out
     */
    public void setDataTimeOut(final int dataTimeOut) {
        if (_debug) {
            _log.debug("set dataTimeOut: {}", dataTimeOut);
        }
        _dataTimeOut = dataTimeOut;
    }

    /**
     * Sets the port time out.
     *
     * @param portTimeOut
     *            the new port time out
     */
    public void setPortTimeOut(final int portTimeOut) {
        if (_debug) {
            _log.debug("set portTimeOut: {}", portTimeOut);
        }
        _portTimeOut = portTimeOut;
    }

    /**
     * Sets the passive.
     *
     * @param passive
     *            the new passive
     */
    public void setPassive(final boolean passive) {
        if (_debug) {
            _log.debug("set passive: {}", passive);
        }
        _passive = passive;
    }

    /**
     * Sets the extended.
     *
     * @param extended
     *            the new extended
     */
    public void setExtended(final boolean extended) {
        if (_debug) {
            _log.debug("set extended: {}", extended);
        }
        _extended = extended;
    }

    /**
     * Sets the shared passive.
     *
     * @param sharedPassive
     *            the new shared passive
     */
    public void setSharedPassive(final boolean sharedPassive) {
        if (_debug) {
            _log.debug("set sharedPassive: {}", sharedPassive);
        }
        _sharedPassive = sharedPassive;
    }

    /**
     * Sets the packet.
     *
     * @param packet
     *            the new packet
     */
    public void setPacket(final boolean packet) {
        if (_debug) {
            _log.debug("set packet: {}", packet);
        }
        _packet = packet;
    }

    /**
     * Sets the data alive.
     *
     * @param dataAlive
     *            the new data alive
     */
    public void setDataAlive(final boolean dataAlive) {
        if (_debug) {
            _log.debug("set dataAlive: {}", dataAlive);
        }
        _dataAlive = dataAlive;
    }

    /**
     * Sets the low port.
     *
     * @param lowPort
     *            the new low port
     */
    public void setLowPort(final boolean lowPort) {
        if (_debug) {
            _log.debug("set lowPort: {}", lowPort);
        }
        _lowPort = lowPort;
    }

    /**
     * Sets the noop.
     *
     * @param noop
     *            the new noop
     */
    public void setNoop(final String noop) {
        if (_debug) {
            _log.debug("set noop: {}", noop);
        }
        _noopCommand = noop;
    }

    /**
     * Sets the debug.
     *
     * @param debug
     *            the new debug
     */
    public void setDebug(final boolean debug) {
        if (debug) {
            _log.debug("set debug: {}", debug);
        }
        _debug = debug;
    }

    /**
     * Sets the input filters.
     *
     * @param filters
     *            the new input filters
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setInputFilters(final String filters) throws IOException {
        if (_debug) {
            _log.debug("set InputFilters: {}", filters);
        }
        dissCommandCheck("OPTS RCOMP " + filters);
        _outputFilters = true;
    }

    /**
     * Sets the output filters.
     *
     * @param filters
     *            the new output filters
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setOutputFilters(final String filters) throws IOException {
        if (_debug) {
            _log.debug("set OutputFilters: {}", filters);
        }
        dissCommandCheck("OPTS SCOMP " + filters);
    }

    /**
     * Delegate checksum.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void delegateChecksum() throws IOException {
        if (_debug) {
            _log.debug("delegateChecksum");
        }
        dissCommandCheck("OPTS MD5SUM");
    }

    /**
     * Dump checksum.
     *
     * @param checksum
     *            the checksum
     * @param fileName
     *            the file name
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void dumpChecksum(final String checksum, final String fileName) throws IOException {
        if (_debug) {
            _log.debug("dumpChecksum");
        }
        if (checksum != null) {
            dissCommandCheck("OPTS MD5SET " + checksum);
        }
        dissCommandCheck("OPTS MD5DUMP " + fileName);
    }

    /**
     * Command is open.
     *
     * @return true, if successful
     */
    public boolean commandIsOpen() {
        return _socketIsOpen(_serverSocket);
    }

    /**
     * _socket is open.
     *
     * @param s
     *            the s
     *
     * @return true, if successful
     */
    private static boolean _socketIsOpen(final Socket s) {
        return s != null && !(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown());
    }

    /**
     * _read server response.
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private int _readServerResponse() throws IOException {
        final var replyBuf = new StringBuilder(32);
        var c = 10;
        var continuingCode = -1;
        var code = -1;
        String response;
        if (_debug) {
            _log.debug("Reading server response");
        }
        try {
            while (commandIsOpen() && c != -1) {
                while ((c = _serverInput.read()) != -1) {
                    if ((c == '\r') && ((c = _serverInput.read()) != '\n')) {
                        replyBuf.append('\r');
                    }
                    replyBuf.append((char) c);
                    if (c == '\n') {
                        break;
                    }
                }
                response = replyBuf.toString();
                replyBuf.setLength(0);
                if (_debug) {
                    var message = response;
                    if (message.endsWith("\n")) {
                        message = message.substring(0, message.length() - 1);
                    }
                    _log.debug("< {}", message);
                }
                if (response.length() < 3) {
                    continue;
                }
                try {
                    code = Integer.parseInt(response.substring(0, 3));
                } catch (final NumberFormatException e) {
                    code = -1;
                }
                _serverResponse.add(response);
                if (continuingCode != -1) {
                    if (code != continuingCode || response.length() >= 4 && response.charAt(3) == '-') {
                        continue;
                    } else {
                        continuingCode = -1;
                        break;
                    }
                }
                if (response.length() >= 4 && response.charAt(3) == '-') {
                    continuingCode = code;
                    continue;
                } else {
                    break;
                }
            }
        } catch (final IOException e) {
            close(false);
            _log.warn("Reading server response", e);
            throw e;
        } catch (final Exception e) {
            close(false);
            _log.warn("Reading server response", e);
            throw new IOException(e.getMessage());
        }
        if (_debug) {
            _log.debug("Server response: {}", code);
        }
        return _lastReplyCode = code;
    }

    /**
     * Gets the response string.
     *
     * @return the response string
     */
    public String getResponseString() {
        final var string = new StringBuilder();
        for (final String element : _serverResponse) {
            final String res;
            if ((res = element.replace('\n', ' ').replace('\r', ' ').trim()).length() > 3) {
                try {
                    if (Integer.parseInt(res.substring(0, 3)) >= 400) {
                        string.setLength(0);
                        string.append(res);
                        break;
                    }
                } catch (final NumberFormatException e) {
                }
            }
            string.append(res);
        }
        _serverResponse = new ArrayList<>(1);
        return string.toString();
    }

    /**
     * Gets the response string no reset.
     *
     * @return the response string no reset
     */
    public String getResponseStringNoReset() {
        final var string = new StringBuilder();
        for (final String element : _serverResponse) {
            string.append(element).append(" ");
        }
        return string.toString().trim();
    }

    /**
     * Close.
     *
     * @param gracefully
     *            the gracefully
     */
    public void close(final boolean gracefully) {
        if (_closed.compareAndSet(false, true)) {
            if (_dataSocket != null) {
                StreamPlugThread.closeQuietly(_dataSocket);
                _dataSocket = null;
            }
            if (_logged && gracefully && commandIsOpen()) {
                try {
                    _issueCommand("QUIT");
                } catch (final Throwable t) {
                    // Ignore
                } finally {
                    _logged = false;
                }
            }
            if (_serverSocket != null) {
                StreamPlugThread.closeQuietly(_serverSocket);
                _serverSocket = null;
            }
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * _issue command.
     *
     * @param cmd
     *            the cmd
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private int _issueCommand(final String cmd) throws IOException {
        if (!commandIsOpen()) {
            throw new FtpLoginException("Not connected to host for '" + cmd + "'");
        }
        _lastCommand = cmd;
        int reply;
        if (_debug) {
            _log.debug("> {}", cmd != null && cmd.startsWith("PASS ") ? "PASS ********" : cmd);
        }
        if (_replyPending) {
            if (_debug) {
                _log.debug("Reply pending");
            }
            final int code;
            if ((code = _readReply()) == FTP_ERROR) {
                _log.debug("Error reading pending reply");
            } else {
                _log.debug("Reply code: {}", code);
            }
        }
        _replyPending = false;
        do {
            _serverOutput.print(cmd + "\r\n");
            _serverOutput.flush();
            reply = _readReply();
        } while (reply == FTP_TRY_AGAIN);
        return reply;
    }

    /**
     * Issue command check.
     *
     * @param cmd
     *            the cmd
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void issueCommandCheck(final String cmd) throws IOException {
        issueCommandCheck(cmd, false);
    }

    /**
     * Issue command check.
     *
     * @param cmd
     *            the cmd
     * @param isError
     *            the is error
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void issueCommandCheck(final String cmd, final boolean isError) throws IOException {
        final var code = _issueCommand(cmd);
        if (isError ? code == FTP_ERROR : code != FTP_SUCCESS) {
            throw new FtpProtocolException(cmd + " failed (" + code + ")");
        }
    }

    /**
     * Diss command check.
     *
     * @param cmd
     *            the cmd
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String dissCommandCheck(final String cmd) throws IOException {
        if (_issueCommand(cmd) == FTP_ERROR) {
            throw new FtpProtocolException("Not a DissFTP server? (" + cmd + ")");
        }
        return getResponseString();
    }

    /**
     * _read reply.
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private int _readReply() throws IOException {
        _lastReplyCode = _readServerResponse();
        switch (_lastReplyCode / 100) {
        case 1:
            _replyPending = true;
            return FTP_SUCCESS;
        case 2: // This case is for future purposes. If not properly used, it
            // might cause an infinite loop.
            // Don't add any code here , unless you know what you are doing.
        case 3:
            return FTP_SUCCESS;
        case 5:
            if (_lastReplyCode == 530) {
                if (!_logged) {
                    throw new FtpLoginException("Not logged in");
                }
                return FTP_ERROR;
            }
            if (_lastReplyCode == 550) {
                if (_lastCommand.startsWith("PASS")) {
                    throw new FtpLoginException("Wrong password");
                }
                var message = getResponseStringNoReset();
                if (message.isEmpty()) {
                    message = "No reply from server (FTP code: 550)";
                }
                throw new IOException(message);
            }
        }
        return FTP_ERROR;
    }

    /**
     * Sends the start data server.
     *
     * @param id
     *            the id
     * @param out
     *            the out
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void _sendStartDataServer(final String id, final OutputStream out) throws IOException {
        final var header = new ByteArrayOutputStream(4 + id.getBytes().length);
        final var stream = new DataOutputStream(header);
        stream.writeInt(id.getBytes().length);
        stream.writeBytes(id);
        stream.flush();
        _log.debug("Send start data server header ({} bytes)", stream.size());
        out.write(header.toByteArray());
        out.flush();
    }

    /**
     * Gets the data socket.
     *
     * @param cmd
     *            the cmd
     * @param posn
     *            the posn
     * @param streamsCount
     *            the streams count
     *
     * @return the data socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private DataSocket _getDataSocket(final String cmd, final long posn, final int streamsCount) throws IOException {
        if (posn > 0) {
            issueCommandCheck("REST " + posn, true);
        }
        if (_dataSocket != null && _dataSocket.isOpen()) {
            if (_debug) {
                _log.debug("Use existing data socket: {}", _dataSocket);
            }
            issueCommandCheck(cmd, true);
        } else {
            if (_dataSocket != null) {
                try {
                    _dataSocket.close();
                } catch (final Throwable t) {
                    // Ignore
                }
            }
            _dataSocket = _openDataSocket(cmd, streamsCount);
        }
        return _dataSocket;
    }

    /**
     * _open data socket.
     *
     * @param cmd
     *            the cmd
     * @param streamsCount
     *            the streams count
     *
     * @return the data socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("resource")
    private DataSocket _openDataSocket(final String cmd, final int streamsCount) throws IOException {
        if (!commandIsOpen()) {
            throw new IOException("Control channel is closed");
        }
        Socket dataSocket;
        final var localAddress = _serverSocket.getLocalAddress();
        final var ptcp = streamsCount > 0;
        try { // PASV mode
            if (ptcp) {
                dissCommandCheck("OPTS PTCP " + streamsCount);
            }
            if (_sharedPassive) {
                dissCommandCheck("OPTS PSHARED");
            }
            final var passiveCmd = _extended ? "EPSV" : "PASV";
            if (!_passive || _issueCommand(passiveCmd) == FTP_ERROR) {
                throw new FtpProtocolException(passiveCmd);
            }
            var reply = getResponseStringNoReset();
            reply = reply.substring(reply.indexOf("(") + 1, reply.indexOf(")"));
            var ipaddress = _serverSocket.getInetAddress().getHostAddress();
            var port = 0;
            if (_extended) {
                // EPSV (Extended Passive)
                try {
                    port = Integer.parseInt(reply.substring(3, reply.length() - 1));
                } catch (final Throwable t) {
                    _log.warn("Invalid PortNumber for EPSV: {}", reply);
                    throw new FtpProtocolException("EPSV");
                }
            } else {
                // PSV (Passive)
                final var st = new StringTokenizer(reply, ",");
                if (st.countTokens() != 6) {
                    _log.warn("Invalid IPAddress for PASV: {}", reply);
                    throw new FtpProtocolException("PASV");
                }
                final var nums = new String[6];
                for (var i = 0; i < 6; i++) {
                    nums[i] = st.nextToken();
                }
                ipaddress = nums[0] + "." + nums[1] + "." + nums[2] + "." + nums[3];
                port = 0;
                try {
                    final var firstbits = Integer.parseInt(nums[4]) << 8;
                    final var lastbits = Integer.parseInt(nums[5]);
                    port = firstbits + lastbits;
                } catch (final Throwable b) {
                    _log.warn("Invalid PortNumber for PASV: {},{}", nums[4], nums[5]);
                    throw new FtpProtocolException("PASV");
                }
            }
            if (port <= 0) {
                _log.warn("Invalid PortNumber for {}: {}", passiveCmd, port);
                throw new FtpProtocolException(passiveCmd);
            }
            if (ptcp) {
                if (_debug) {
                    _log.debug("Use PTCPSocket data socket (passive)");
                }
                dataSocket = new PTCPSocket(ipaddress, port, streamsCount);
                if (_sendBufferSize != -1) {
                    dataSocket.setSendBufferSize(_sendBufferSize);
                }
                if (_receiveBufferSize != -1) {
                    dataSocket.setReceiveBufferSize(_receiveBufferSize);
                }
            } else {
                if (_debug) {
                    _log.debug("Use normal data socket (passive)");
                }
                final var socketConfig = new SocketConfig(_statistics, "FtpPASV", _debug);
                applyTCPOptions(socketConfig);
                socketConfig.setListenAddress(localAddress.getHostAddress());
                if (_sendBufferSize != -1) {
                    socketConfig.setSendBufferSize(_sendBufferSize);
                }
                if (_receiveBufferSize != -1) {
                    socketConfig.setReceiveBufferSize(_receiveBufferSize);
                }
                dataSocket = socketConfig.getSocket(ipaddress, port);
            }
            if (_sharedPassive) {
                if (_debug) {
                    _log.debug("Use shared passive data socket");
                }
                final var address = _serverSocket.getLocalAddress().getHostAddress();
                final var key = address + ":" + _serverSocket.getLocalPort();
                dataSocket.setSoTimeout(_dataTimeOut); // Avoid being stuck
                final var out = dataSocket.getOutputStream();
                if (!ptcp) {
                    out.write(PTCPServerSocket.getNormalSocketHeader());
                }
                _sendStartDataServer(key, out);
            }
            issueCommandCheck(cmd, true);
        } catch (final FtpProtocolException fpe) {
            // PASV/EPSV was not supported. Resort to PORT/EPRT!
            if (ptcp) {
                if (_debug) {
                    _log.debug("Use PTCPServerSocket data socket (port)");
                }
                final var pportSocket = new PTCPServerSocket(0, 1, localAddress);
                try {
                    issueCommandCheck(_getPortCommand(localAddress, pportSocket.getLocalPort()), true);
                    issueCommandCheck(cmd, true);
                    pportSocket.setSoTimeout(_portTimeOut);
                    dataSocket = pportSocket.accept();
                } finally {
                    if (pportSocket != null) {
                        try {
                            pportSocket.close(true);
                        } catch (final Throwable t) {
                            // Ignore
                        }
                    }
                }
            } else {
                if (_debug) {
                    _log.debug("Use ServerSocket data socket (port)");
                }
                final var socketConfig = new SocketConfig(_statistics, "FtpPORT", _debug);
                socketConfig.setBackLog(1);
                socketConfig.setPrivilegedLocalPort(_lowPort);
                applyTCPOptions(socketConfig);
                socketConfig.setListenAddress(localAddress.getHostAddress());
                if (_sendBufferSize != -1) {
                    socketConfig.setSendBufferSize(_sendBufferSize);
                }
                if (_receiveBufferSize != -1) {
                    socketConfig.setReceiveBufferSize(_receiveBufferSize);
                }
                final var portSocket = socketConfig.getServerSocket();
                try {
                    issueCommandCheck(_getPortCommand(localAddress, portSocket.getLocalPort()), true);
                    issueCommandCheck(cmd, true);
                    portSocket.setSoTimeout(_portTimeOut);
                    // The data socket is closed in the close method of the
                    // DataSocket object!
                    dataSocket = socketConfig.getSocket(portSocket);
                } finally {
                    if (portSocket != null) {
                        try {
                            portSocket.close();
                        } catch (final Throwable t) {
                            // Ignore
                        }
                    }
                }
            }
        }
        dataSocket.setSoTimeout(_dataTimeOut);
        return new DataSocket(dataSocket, _dataAlive);
    }

    /**
     * Gets the port command.
     *
     * @param localAddress
     *            the local address
     * @param localPort
     *            the local port
     *
     * @return the string
     *
     * @throws FtpProtocolException
     *             the ftp protocol exception
     */
    private String _getPortCommand(final InetAddress localAddress, final int localPort) throws FtpProtocolException {
        String portCmd = null;
        if (_extended) {
            if (localAddress instanceof Inet6Address) {
                portCmd = "EPRT |2|" + localAddress.getHostAddress() + "|" + localPort + "|";
            } else {
                portCmd = "EPRT |1|" + localAddress.getHostAddress() + "|" + localPort + "|";
            }
        } else {
            if (localAddress instanceof Inet6Address) {
                throw new FtpProtocolException("PORT command not support with IPv6");
            }
            portCmd = "PORT ";
            final var srcAddr = localAddress.getAddress();
            for (final byte element : srcAddr) {
                portCmd += (element & 0xFF) + ",";
            }
            portCmd += (localPort >>> 8 & 0xff) + "," + (localPort & 0xff);
        }
        return portCmd;
    }

    /**
     * _open server.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _openServer(final String host, final int port) throws IOException {
        final var socketConfig = new SocketConfig("FtpOPEN");
        socketConfig.setDebug(_debug);
        applyTCPOptions(socketConfig);
        if (_listenAddress != null) {
            socketConfig.setListenAddress(_listenAddress);
        }
        _openServer(socketConfig.getSocket(host, port));
        _host = host;
        _port = port;
    }

    /**
     * Login.
     *
     * @param user
     *            the user
     * @param password
     *            the password
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void login(final String user, final String password) throws IOException {
        if (_issueCommand("USER " + user) == FTP_ERROR) {
            throw new FtpLoginException("Invalid user name");
        }
        _log.debug("LastReplyCode: {}", _lastReplyCode);
        if (_lastReplyCode != 230 && password != null && _issueCommand("PASS " + password) == FTP_ERROR) {
            throw new FtpLoginException("Login failed");
        }
        if (_dataAlive) {
            dissCommandCheck("OPTS DATAALIVE");
        }
        if (_mkdirs) {
            dissCommandCheck("OPTS MKDIRS");
        }
        _logged = true;
        _user = user;
        _password = password;
    }

    /**
     * Gets the.
     *
     * @param filename
     *            the filename
     * @param posn
     *            the posn
     * @param streamsCount
     *            the streams count
     *
     * @return the input stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public InputStream get(final String filename, final long posn, final int streamsCount) throws IOException {
        final var dataSocket = _getDataSocket("RETR " + _format(filename), posn, streamsCount);
        final var stream = dataSocket.getInputStream();
        return _dataAlive ? stream : new FilterInputStream(stream) {
            @Override
            public void close() throws IOException {
                dataSocket.close();
            }
        };
    }

    /**
     * Puts the.
     *
     * @param filename
     *            the filename
     * @param posn
     *            the posn
     * @param size
     *            the size
     * @param append
     *            the append
     * @param streamsCount
     *            the streams count
     *
     * @return the output stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public OutputStream put(final String filename, final long posn, final long size, final boolean append,
            final int streamsCount) throws IOException {
        final var dataSocket = _getDataSocket((append ? "APPE" : "STOR") + " " + _format(filename), posn, streamsCount);
        final var stream = dataSocket.getOutputStream(_outputFilters || _packet ? -1 : size);
        return _dataAlive ? stream : new FilterOutputStream(stream) {
            @Override
            public void write(final int b) throws IOException {
                out.write(b);
            }

            @Override
            public void write(final byte[] b) throws IOException {
                out.write(b);
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                out.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                dataSocket.close();
            }
        };
    }

    /**
     * Check pending reply.
     *
     * @return the int
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public int checkPendingReply() throws IOException {
        var code = -1;
        try {
            if (_replyPending) {
                if (_debug) {
                    _log.debug("Reply pending");
                }
                if ((code = _readReply()) == FTP_ERROR) {
                    var message = getResponseStringNoReset();
                    if (message.isEmpty()) {
                        message = "No reply from server (FTP code: " + code + ")";
                    }
                    throw new FtpProtocolException(message);
                }
                if (_debug) {
                    _log.debug("Reply code: {}", code);
                }
            }
        } finally {
            _replyPending = false;
        }
        return code;
    }

    /**
     * _list.
     *
     * @param command
     *            the command
     * @param directory
     *            the directory
     *
     * @return the buffered reader
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private BufferedReader _list(String command, final String directory) throws IOException {
        command += isNotEmpty(directory) ? " " + _format(directory) : "";
        final Reader reader;
        if (!_dataAlive) {
            try (var dataSocket = _openDataSocket(command, 0)) {
                final var list = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                final var result = new StringBuilder();
                String line;
                while ((line = list.readLine()) != null) {
                    result.append(line).append("\n");
                }
                reader = new StringReader(result.toString());
            }
        } else {
            getResponseString();
            issueCommandCheck(command);
            final var result = new StringBuilder();
            for (final String line : _serverResponse) {
                if (line != null && !line.startsWith("200-LIST") && !line.startsWith("200 LIST")) {
                    result.append(line).append("\n");
                }
            }
            _serverResponse = new ArrayList<>(1);
            reader = new StringReader(result.toString());
        }
        return new BufferedReader(reader);
    }

    /**
     * Nlist.
     *
     * @param remoteDirectory
     *            the remote directory
     *
     * @return the buffered reader
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public BufferedReader nlist(final String remoteDirectory) throws IOException {
        return _list("NLST", remoteDirectory);
    }

    /**
     * List.
     *
     * @param remoteDirectory
     *            the remote directory
     *
     * @return the buffered reader
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public BufferedReader list(final String remoteDirectory) throws IOException {
        return _list("LIST", remoteDirectory);
    }

    /**
     * Cd.
     *
     * @param remoteDirectory
     *            the remote directory
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void cd(final String remoteDirectory) throws IOException {
        issueCommandCheck("CWD " + _format(remoteDirectory));
    }

    /**
     * Rename.
     *
     * @param oldFile
     *            the old file
     * @param newFile
     *            the new file
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void rename(final String oldFile, final String newFile) throws IOException {
        issueCommandCheck("RNFR " + _format(oldFile));
        issueCommandCheck("RNTO " + _format(newFile));
    }

    /**
     * Site.
     *
     * @param params
     *            the params
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void site(final String params) throws IOException {
        issueCommandCheck("SITE " + params);
    }

    /**
     * Binary.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void binary() throws IOException {
        issueCommandCheck("TYPE I");
    }

    /**
     * Ascii.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void ascii() throws IOException {
        issueCommandCheck("TYPE A");
    }

    /**
     * Abort.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void abort() throws IOException {
        issueCommandCheck("ABOR");
    }

    /**
     * Cdup.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void cdup() throws IOException {
        issueCommandCheck("CDUP");
    }

    /**
     * Mkdir.
     *
     * @param s
     *            the s
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void mkdir(final String s) throws IOException {
        issueCommandCheck("MKD " + _format(s));
    }

    /**
     * Rmdir.
     *
     * @param s
     *            the s
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void rmdir(final String s) throws IOException {
        issueCommandCheck("RMD " + _format(s));
    }

    /**
     * Delete.
     *
     * @param s
     *            the s
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void delete(final String s) throws IOException {
        issueCommandCheck("DELE " + _format(s));
    }

    /**
     * Pwd.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void pwd() throws IOException {
        issueCommandCheck("PWD");
    }

    /**
     * Syst.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void syst() throws IOException {
        issueCommandCheck("SYST");
    }

    /**
     * Noop.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void noop() throws IOException {
        issueCommandCheck(_noopCommand != null ? _noopCommand : "NOOP");
    }

    /**
     * Checks if is alive.
     *
     * @return true, if is alive
     */
    public boolean isAlive() {
        try {
            noop();
            return true;
        } catch (final Throwable t) {
            return false;
        }
    }

    /**
     * Size.
     *
     * @param s
     *            the s
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void size(final String s) throws IOException {
        issueCommandCheck("SIZE " + _format(s));
    }

    /**
     * Mdtm.
     *
     * @param s
     *            the s
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void mdtm(final String s) throws IOException {
        issueCommandCheck("MDTM " + _format(s));
    }

    /**
     * Empty.
     *
     * @param s
     *            the s
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void empty(final String s) throws IOException {
        dissCommandCheck("OPTS EMPTY " + _format(s));
    }

    /**
     * _open server.
     *
     * @param socket
     *            the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _openServer(final Socket socket) throws IOException {
        if (_serverSocket != null) {
            close(false);
        }
        _serverSocket = socket;
        _serverSocket.setSoTimeout(_commTimeOut);
        _serverOutput = new PrintWriter(new BufferedOutputStream(_serverSocket.getOutputStream()), true);
        _serverInput = new BufferedInputStream(_serverSocket.getInputStream());
        final var code = _readReply();
        if (code == FTP_ERROR) {
            var message = getResponseStringNoReset();
            if (message.isEmpty()) {
                message = "Connection refused (FTP code: " + code + ")";
            }
            throw new FtpConnectException(message);
        }
    }

    /**
     * Keep control connection alive.
     *
     * @param keepAlive
     *            the keep alive
     */
    public void keepControlConnectionAlive(final boolean keepAlive) {
        // Is the keep alive thread already initialized?
        synchronized (_lock) {
            if (_keepAliveThread == null) {
                _keepAliveThread = new KeepControlConnectionAliveThread();
                _keepAliveThread.execute();
            }
        }
        if (keepAlive) {
            _keepAliveThread.add(this);
        } else {
            _keepAliveThread.remove(this);
        }
    }

    /**
     * _format.
     *
     * @param o
     *            the o
     *
     * @return the string
     */
    private static String _format(final Object o) {
        return o != null ? String.valueOf(o) : "";
    }

    /**
     * The Class KeepControlConnectionAliveThread.
     */
    private static final class KeepControlConnectionAliveThread extends ConfigurableLoopRunnable {
        /** The clients. */
        private final Map<Integer, FtpClient> clients = new ConcurrentHashMap<>();

        /**
         * Adds the.
         *
         * @param client
         *            the client
         */
        synchronized void add(final FtpClient client) {
            clients.put(client.hashCode(), client);
            _log.debug("ControlConnectionAliveCacheSize(+): {}", clients.size());
        }

        /**
         * Removes the.
         *
         * @param client
         *            the client
         */
        synchronized void remove(final FtpClient client) {
            clients.remove(client.hashCode());
            _log.debug("ControlConnectionAliveCacheSize(-): {}", clients.size());
        }

        /**
         * Configurable loop run.
         */
        @Override
        public void configurableLoopRun() {
            try {
                for (final Integer key : clients.keySet().toArray(new Integer[0])) {
                    final var client = clients.get(key);
                    if (client != null) {
                        try {
                            if (client.commandIsOpen()) {
                                client._serverOutput.append('\0');
                                client._serverOutput.flush();
                            } else {
                                remove(client);
                            }
                        } catch (final Throwable t) {
                            remove(client);
                            _log.warn(t);
                        }
                    }
                }
            } catch (final Throwable t) {
                _log.warn(t);
            }
            try {
                Thread.sleep(30 * Timer.ONE_SECOND);
            } catch (final InterruptedException e) {
                // Ignore
            }
        }
    }
}
