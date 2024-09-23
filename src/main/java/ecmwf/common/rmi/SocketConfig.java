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

package ecmwf.common.rmi;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.interruptible.InterruptibleRMIClientSocket;
import ecmwf.common.rmi.interruptible.InterruptibleRMIServerSocket;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class SocketConfig.
 */
public class SocketConfig {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SocketConfig.class);

    /** The Constant MIN_PORT. */
    public static final int MIN_PORT = Cnf.at("SocketConfig", "minPrivilegedPort", 500);

    /** The Constant MAX_PORT. */
    public static final int MAX_PORT = Cnf.at("SocketConfig", "maxPrivilegedPort", 1023);

    /** The current local address. */
    private static String currentLocalAddress = null;

    /** The current name. */
    private String currentName = null;

    /** The tcp congestion. */
    private String tcpCongestion = null;

    /** The so max pacing rate. */
    private Integer soMaxPacingRate = null;

    /** The tcp max segment. */
    private Integer tcpMaxSegment = null;

    /** The tcp time stamp. */
    private Boolean tcpTimeStamp = null;

    /** The tcp window clamp. */
    private Integer tcpWindowClamp = null;

    /** The tcp keep alive time. */
    private Integer tcpKeepAliveTime = null;

    /** The tcp keep alive interval. */
    private Integer tcpKeepAliveInterval = null;

    /** The tcp keep alive probes. */
    private Integer tcpKeepAliveProbes = null;

    /** The tcp linger enable. */
    private Boolean tcpLingerEnable = null;

    /** The tcp linger time. */
    private Integer tcpLingerTime = null;

    /** The tcp user timeout. */
    private Integer tcpUserTimeout = null;

    /** The tcp quick ack. */
    private Boolean tcpQuickAck = null;

    /** The current trace. */
    private boolean currentTrace = Cnf.at("SocketConfig", "trace", true);

    /** The current privileged port number. */
    private int currentPrivilegedPortNumber = 0;

    /** The current privileged local port number. */
    private boolean currentPrivilegedLocalPortNumber = Cnf.at("SocketConfig", "privilegedLocalPort", false);

    /** The current listen address. */
    private String currentListenAddress = Cnf.at("SocketConfig", "listenAddress", null);

    /** The current public address. */
    private String currentPublicAddress = Cnf.at("SocketConfig", "publicAddress", null);

    /** The current receive buffer size. */
    private int currentReceiveBufferSize = Cnf.at("SocketConfig", "receiveBufferSize", -1);

    /** The current send buffer size. */
    private int currentSendBufferSize = Cnf.at("SocketConfig", "sendBufferSize", -1);

    /** The current so time out. */
    private int currentSoTimeOut = Cnf.at("SocketConfig", "soTimeOut", 86400000);

    /** The current connect time out. */
    private int currentConnectTimeOut = Cnf.at("SocketConfig", "connectTimeOut", 15000);

    /** The current back log. */
    private int currentBackLog = Cnf.at("SocketConfig", "backLog", 100);

    /** The current keep alive. */
    private Boolean currentKeepAlive = Cnf.booleanAt("SocketConfig", "keepAlive", null);

    /** The current tcp no delay. */
    private Boolean currentTcpNoDelay = Cnf.booleanAt("SocketConfig", "tcpNoDelay", null);

    /** The current reuse address. */
    private Boolean currentReuseAddress = Cnf.booleanAt("SocketConfig", "reuseAddress", null);

    /** The current interruptible. */
    private boolean currentInterruptible = Cnf.booleanAt("SocketConfig", "interruptible", false);

    /** The current host. */
    private String currentHost = Cnf.at("SocketConfig", "host", null);

    /** The current port number. */
    private int currentPortNumber = Cnf.at("SocketConfig", "port", -1);

    /** The current server port number. */
    private int currentServerPortNumber = Cnf.at("SocketConfig", "serverPort", 0);

    /** The synchro privileged port. */
    private final Object synchroPrivilegedPort = new Object();

    /** The statistics. */
    private final ClientSocketStatistics statistics;

    /** The client socket factory. */
    private ClientSocketFactory clientSocketFactory;

    /** The debug. */
    private boolean debug = false;

    /**
     * Instantiates a new socket configuration.
     */
    protected SocketConfig() {
        statistics = null;
    }

    /**
     * Instantiates a new socket config.
     *
     * @param name
     *            the name
     */
    public SocketConfig(final String name) {
        statistics = null;
        load(name);
    }

    /**
     * Instantiates a new socket config.
     *
     * @param statistics
     *            the statistics
     * @param name
     *            the name
     */
    public SocketConfig(final ClientSocketStatistics statistics, final String name) {
        this.statistics = statistics;
        load(name);
    }

    /**
     * Instantiates a new socket config.
     *
     * @param statistics
     *            the statistics
     * @param name
     *            the name
     * @param debug
     *            the debug
     */
    public SocketConfig(final ClientSocketStatistics statistics, final String name, final boolean debug) {
        this.statistics = statistics;
        load(name);
        setDebug(debug);
    }

    /**
     * Load.
     *
     * @param name
     *            the name
     */
    protected void load(final String name) {
        if (isNotEmpty(name)) {
            currentName = name;
            if (Cnf.has(name)) { // Load configuration from the properties file
                currentListenAddress = Cnf.at(name, "listenAddress", currentListenAddress);
                currentPublicAddress = Cnf.at(name, "publicAddress", currentPublicAddress);
                currentReceiveBufferSize = Cnf.at(name, "receiveBufferSize", currentReceiveBufferSize);
                currentSendBufferSize = Cnf.at(name, "sendBufferSize", currentSendBufferSize);
                currentSoTimeOut = Cnf.at(name, "soTimeOut",
                        Cnf.at(name, "soTimeout", Cnf.at(name, "timeOut", currentSoTimeOut)));
                currentConnectTimeOut = Cnf.at(name, "connectTimeOut", currentConnectTimeOut);
                currentBackLog = Cnf.at(name, "backLog", currentBackLog);
                currentKeepAlive = Cnf.booleanAt(name, "keepAlive", currentKeepAlive);
                currentTcpNoDelay = Cnf.booleanAt(name, "tcpNoDelay", currentTcpNoDelay);
                currentReuseAddress = Cnf.booleanAt(name, "reuseAddress", currentReuseAddress);
                currentInterruptible = Cnf.at(name, "interruptible", currentInterruptible);
                currentPrivilegedLocalPortNumber = Cnf.at(name, "privilegedLocalPort",
                        currentPrivilegedLocalPortNumber);
                currentTrace = Cnf.at(name, "trace", currentTrace);
                currentHost = Cnf.at(name, "host", currentHost);
                currentPortNumber = Cnf.at(name, "port", currentPortNumber);
                currentServerPortNumber = Cnf.at(name, "serverPort", currentServerPortNumber);
            }
        }
    }

    /**
     * Gets the statistics.
     *
     * @return the statistics
     */
    protected ClientSocketStatistics getStatistics() {
        return statistics;
    }

    /**
     * With client socket factory.
     *
     * @param clientSocketFactory
     *            the client socket factory
     *
     * @return the socket config
     */
    protected SocketConfig withClientSocketFactory(final ClientSocketFactory clientSocketFactory) {
        this.clientSocketFactory = clientSocketFactory;
        return this;
    }

    /**
     * Gets the client socket factory.
     *
     * @return the client socket factory
     */
    private synchronized ClientSocketFactory getClientSocketFactory() {
        if (clientSocketFactory == null) {
            clientSocketFactory = new ClientSocketFactory(this);
        }
        return clientSocketFactory;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(final String name) {
        currentName = name;
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
     * Sets the trace.
     *
     * @param trace
     *            the new trace
     */
    public void setTrace(final boolean trace) {
        currentTrace = trace;
    }

    /**
     * Sets the listen address.
     *
     * @param listenAddress
     *            the new listen address
     */
    public void setListenAddress(final String listenAddress) {
        if (isNotEmpty(listenAddress)) {
            currentListenAddress = listenAddress;
            if (debug) {
                _log.debug("listenAddress: {}", currentListenAddress);
            }
        } else {
            currentListenAddress = null;
        }
    }

    /**
     * Sets the public address.
     *
     * @param publicAddress
     *            the new public address
     */
    public void setPublicAddress(final String publicAddress) {
        if (isNotEmpty(publicAddress)) {
            currentPublicAddress = publicAddress;
            if (debug) {
                _log.debug("publicAddress: {}", currentPublicAddress);
            }
        }
    }

    /**
     * Sets the receive buffer size.
     *
     * @param receiveBufferSize
     *            the new receive buffer size
     */
    public void setReceiveBufferSize(final int receiveBufferSize) {
        currentReceiveBufferSize = receiveBufferSize;
        if (debug) {
            _log.debug("receiveBufferSize: {}", currentReceiveBufferSize);
        }
    }

    /**
     * Sets the sends the buffer size.
     *
     * @param sendBufferSize
     *            the new sends the buffer size
     */
    public void setSendBufferSize(final int sendBufferSize) {
        currentSendBufferSize = sendBufferSize;
        if (debug) {
            _log.debug("sendBufferSize: {}", currentSendBufferSize);
        }
    }

    /**
     * Sets the so time out.
     *
     * @param timeOut
     *            the new so time out
     */
    public void setSoTimeOut(final int timeOut) {
        currentSoTimeOut = timeOut;
        if (debug) {
            _log.debug("soTimeOut: {}", currentSoTimeOut);
        }
    }

    /**
     * Sets the connect time out.
     *
     * @param timeOut
     *            the new connect time out
     */
    public void setConnectTimeOut(final int timeOut) {
        currentConnectTimeOut = timeOut > 0 ? timeOut : 0;
        if (debug) {
            _log.debug("connectTimeOut: {}", currentConnectTimeOut);
        }
    }

    /**
     * Sets the back log.
     *
     * @param backLog
     *            the new back log
     */
    public void setBackLog(final int backLog) {
        currentBackLog = backLog;
        if (debug) {
            _log.debug("backLog: {}", currentBackLog);
        }
    }

    /**
     * Sets the keep alive.
     *
     * @param keepAlive
     *            the new keep alive
     */
    public void setKeepAlive(final Boolean keepAlive) {
        currentKeepAlive = keepAlive;
        if (debug) {
            _log.debug("keepAlive: {}", currentKeepAlive);
        }

    }

    /**
     * Sets the tcp no delay.
     *
     * @param tcpNoDelay
     *            the new tcp no delay
     */
    public void setTcpNoDelay(final Boolean tcpNoDelay) {
        currentTcpNoDelay = tcpNoDelay;
        if (debug) {
            _log.debug("tcpNoDelay: {}", currentTcpNoDelay);
        }

    }

    /**
     * Sets the reuse address.
     *
     * @param reuseAddress
     *            the new reuse address
     */
    public void setReuseAddress(final Boolean reuseAddress) {
        currentReuseAddress = reuseAddress;
        if (debug) {
            _log.debug("reuseAddress: {}", currentReuseAddress);
        }

    }

    /**
     * Sets the interruptible.
     *
     * @param interruptible
     *            the new interruptible
     */
    public void setInterruptible(final boolean interruptible) {
        currentInterruptible = interruptible;
        if (debug) {
            _log.debug("interruptible: {}", currentInterruptible);
        }

    }

    /**
     * Sets the privileged local port.
     *
     * @param privilegedLocalPort
     *            the new privileged local port
     */
    public void setPrivilegedLocalPort(final boolean privilegedLocalPort) {
        currentPrivilegedLocalPortNumber = privilegedLocalPort;
        if (debug) {
            _log.debug("privilegedLocalPort: {}", currentPrivilegedLocalPortNumber);
        }

    }

    /**
     * Set a TCP option.
     *
     * @param tcpCongestion
     *            the TCP congestion algorithm name
     */
    public void setTCPCongestion(final String tcpCongestion) {
        this.tcpCongestion = tcpCongestion;
        if (debug) {
            _log.debug("tcpCongestion: {}", this.tcpCongestion);
        }
    }

    /**
     * Set a SO option.
     *
     * @param soMaxPacingRate
     *            set the maximum transmit rate in bytes per second for the socket.
     */
    public void setSOMaxPacingRate(final Integer soMaxPacingRate) {
        this.soMaxPacingRate = soMaxPacingRate;
        if (debug) {
            _log.debug("soMaxPacingRate: {}", this.soMaxPacingRate);
        }
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
        this.tcpMaxSegment = tcpMaxSegment;
        if (debug) {
            _log.debug("tcpMaxSegment: {}", this.tcpMaxSegment);
        }

    }

    /**
     * Set a TCP option.
     *
     * @param tcpTimeStamp
     *            enables or disables the use of timestamps in TCP packets
     */
    public void setTCPTimeStamp(final Boolean tcpTimeStamp) {
        this.tcpTimeStamp = tcpTimeStamp;
        if (debug) {
            _log.debug("tcpTimeStamp: {}", this.tcpTimeStamp);
        }

    }

    /**
     * Set a TCP option.
     *
     * @param tcpWindowClamp
     *            bound the size of the advertised window to this value
     */
    public void setTCPWindowClamp(final Integer tcpWindowClamp) {
        this.tcpWindowClamp = tcpWindowClamp;
        if (debug) {
            _log.debug("tcpWindowClamp: {}", this.tcpWindowClamp);
        }

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
        this.tcpKeepAliveTime = tcpKeepAliveTime;
        if (debug) {
            _log.debug("tcpKeepAlive: {}", this.tcpKeepAliveTime);
        }

    }

    /**
     * Set a TCP option.
     *
     * @param tcpKeepAliveInterval
     *            the interval between subsequential keepalive probes, regardless of what the connection has exchanged
     *            in the meantime
     */
    public void setTCPKeepAliveInterval(final Integer tcpKeepAliveInterval) {
        this.tcpKeepAliveInterval = tcpKeepAliveInterval;
        if (debug) {
            _log.debug("tcpKeepAliveInterval: {}", this.tcpKeepAliveInterval);
        }

    }

    /**
     * Set a TCP option.
     *
     * @param tcpKeepAliveProbes
     *            the number of unacknowledged probes to send before considering the connection dead and notifying the
     *            application layer
     */
    public void setTCPKeepAliveProbes(final Integer tcpKeepAliveProbes) {
        this.tcpKeepAliveProbes = tcpKeepAliveProbes;
        if (debug) {
            _log.debug("tcpKeepAliveProbes: {}", this.tcpKeepAliveProbes);
        }

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
        this.tcpLingerEnable = tcpLingerEnable;
        this.tcpLingerTime = tcpLingerTime;
        if (debug) {
            _log.debug("tcpLingerEnable: {}", this.tcpLingerEnable);
            _log.debug("tcpLingerTime: {}", this.tcpLingerTime);
        }

    }

    /**
     * Provides a way to control the timeout for unacknowledged data on a TCP connection.
     *
     * @param tcpUserTimeout
     *            maximum amount of time, in milliseconds, that transmitted data may remain unacknowledged before an
     *            error is returned
     */
    public void setTCPUserTimeout(final Integer tcpUserTimeout) {
        this.tcpUserTimeout = tcpUserTimeout;
        if (debug) {
            _log.debug("tcpUserTimeout: {}", this.tcpUserTimeout);
        }

    }

    /**
     * Controls whether the TCP stack should quickly acknowledge incoming data.
     *
     * @param tcpQuickAck
     *            when enabled, the TCP stack sends immediate acknowledgment for incoming data without waiting for the
     *            delayed acknowledgment timer.
     */
    public void setTCPQuickAck(final Boolean tcpQuickAck) {
        this.tcpQuickAck = tcpQuickAck;
        if (debug) {
            _log.debug("tcpQuickAck: {}", this.tcpQuickAck);
        }
    }

    /**
     * Sets the required socket or serverSocket options.
     *
     * @param socket
     *            the new TCP options
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setTCPOptions(final Socket socket) throws IOException {
        if (SocketOptions.isAccessible(socket)) {
            if (debug && _log.isDebugEnabled()) {
                _log.debug("Underlying socket: class={}, fd={}", socket.getClass().getName(),
                        SocketOptions.getSocketDescriptor(socket));
            }
            if (tcpCongestion != null && !tcpCongestion.isBlank()) {
                try {
                    final var result = SocketOptions.setTCPCongestion(socket, tcpCongestion);
                    if (debug) {
                        _log.debug("TCPCongestion({}) requested (result={})", tcpCongestion, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting TCPCongestion({})", tcpCongestion, t);
                }
            }
            if (soMaxPacingRate != null && soMaxPacingRate > 0) {
                try {
                    final var result = SocketOptions.setSOMaxPacingRate(socket, soMaxPacingRate);
                    if (debug) {
                        _log.debug("SOMaxPacingRate({}) requested (result={})", soMaxPacingRate, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting SOMaxPacingRate({})", soMaxPacingRate, t);
                }
            }
            if (tcpMaxSegment != null && tcpMaxSegment > 0) {
                try {
                    final var result = SocketOptions.setTCPMaxSegment(socket, tcpMaxSegment);
                    if (debug) {
                        _log.debug("TCPMaxSegment({}) requested (result={})", tcpMaxSegment, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting TCPMaxSegment({})", tcpMaxSegment, t);
                }
            }
            if (tcpTimeStamp != null) {
                try {
                    final var result = SocketOptions.setTCPTimeStamp(socket, tcpTimeStamp);
                    if (debug) {
                        _log.debug("TCPTimeStamp({}) requested (result={})", tcpTimeStamp, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("TCPTimeStamp({})", tcpTimeStamp, t);
                }
            }
            if (tcpWindowClamp != null && tcpWindowClamp > 0) {
                try {
                    final var result = SocketOptions.setTCPWindowClamp(socket, tcpWindowClamp);
                    if (debug) {
                        _log.debug("TCPWindowClamp({}) requested (result={})", tcpWindowClamp, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting TCPWindowClamp({})", tcpWindowClamp, t);
                }
            }
            if (tcpKeepAliveTime != null && tcpKeepAliveTime > 0) {
                try {
                    final var result = SocketOptions.setTCPKeepAliveTime(socket, tcpKeepAliveTime);
                    if (debug) {
                        _log.debug("TCPKeepAliveTime({}) requested (result={})", tcpKeepAliveTime, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting TCPKeepAliveTime({})", tcpKeepAliveTime, t);
                }
            }
            if (tcpKeepAliveInterval != null && tcpKeepAliveInterval > 0) {
                try {
                    final var result = SocketOptions.setTCPKeepAliveInterval(socket, tcpKeepAliveInterval);
                    if (debug) {
                        _log.debug("TCPKeepAliveInterval({}) requested (result={})", tcpKeepAliveInterval, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting TCPKeepAliveInterval({})", tcpKeepAliveInterval, t);
                }
            }
            if (tcpKeepAliveProbes != null && tcpKeepAliveProbes > 0) {
                try {
                    final var result = SocketOptions.setTCPKeepAliveProbes(socket, tcpKeepAliveProbes);
                    if (debug) {
                        _log.debug("TCPKeepAliveProbes({}) requested (result={})", tcpKeepAliveProbes, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting TCPKeepAliveProbes({})", tcpKeepAliveProbes, t);
                }
            }
            if (tcpLingerEnable != null && tcpLingerTime != null && tcpLingerTime > 0) {
                try {
                    final var result = SocketOptions.setTCPLinger(socket, tcpLingerEnable, tcpLingerTime);
                    if (debug) {
                        _log.debug("TCPLinger({},{}) requested (result={})", tcpLingerEnable, tcpLingerTime, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting TCPLinger({},{})", tcpLingerEnable, tcpLingerTime, t);
                }
            }
            if (tcpUserTimeout != null && tcpUserTimeout > 0) {
                try {
                    final var result = SocketOptions.setTCPUserTimeout(socket, tcpUserTimeout);
                    if (debug) {
                        _log.debug("TCPUserTimeout({}) requested (result={})", tcpUserTimeout, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("Setting TCPUserTimeout({})", tcpUserTimeout, t);
                }
            }
            if (tcpQuickAck != null) {
                try {
                    final var result = SocketOptions.setTCPQuickAck(socket, tcpQuickAck);
                    if (debug) {
                        _log.debug("TCPQuickAck({}) requested (result={})", tcpQuickAck, result);
                    }
                } catch (final Throwable t) {
                    _log.warn("TCPQuickAck({})", tcpQuickAck, t);
                }
            }
        }
    }

    /**
     * Sets the host.
     *
     * @param host
     *            the new host
     */
    public void setHost(final String host) {
        currentHost = host;
        if (debug) {
            _log.debug("host: {}", currentHost);
        }

    }

    /**
     * Sets the port.
     *
     * @param port
     *            the new port
     */
    public void setPort(final int port) {
        currentPortNumber = port;
        if (debug) {
            _log.debug("port: {}", currentPortNumber);
        }

    }

    /**
     * Sets the server port.
     *
     * @param serverPort
     *            the new server port
     */
    public void setServerPort(final int serverPort) {
        currentServerPortNumber = serverPort;
        if (debug) {
            _log.debug("serverPort: {}", currentServerPortNumber);
        }

    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return currentName;
    }

    /**
     * Gets the trace.
     *
     * @return the trace
     */
    public boolean getTrace() {
        return currentTrace;
    }

    /**
     * Gets the listen address.
     *
     * @return the listen address
     */
    public String getListenAddress() {
        return currentListenAddress;
    }

    /**
     * Gets the public address.
     *
     * @return the public address
     */
    public String getPublicAddress() {
        try {
            if (!isEmpty(currentPublicAddress)) {
                return currentPublicAddress;
            }
            if (isEmpty(currentListenAddress) || InetAddress.getByName(currentListenAddress).isAnyLocalAddress()) {
                final var result = getLocalAddress();
                _log.warn("Using {} as a public address{} (localhost)", result,
                        isNotEmpty(currentName) ? " for " + currentName : "");
                return result;
            } else {
                return currentListenAddress;
            }
        } catch (final UnknownHostException e) {
            final var result = "127.0.0.1";
            _log.warn("Using {} as a public address{} (loopback)", result,
                    isNotEmpty(currentName) ? " for " + currentName : "", e);
            return result;
        }
    }

    /**
     * Gets the receive buffer size.
     *
     * @return the receive buffer size
     */
    public int getReceiveBufferSize() {
        return currentReceiveBufferSize;
    }

    /**
     * Gets the sends the buffer size.
     *
     * @return the sends the buffer size
     */
    public int getSendBufferSize() {
        return currentSendBufferSize;
    }

    /**
     * Gets the SO time out.
     *
     * @return the SO time out
     */
    public int getSOTimeOut() {
        return currentSoTimeOut;
    }

    /**
     * Gets the connect time out.
     *
     * @return the connect time out
     */
    public int getConnectTimeOut() {
        return currentConnectTimeOut;
    }

    /**
     * Gets the back log.
     *
     * @return the back log
     */
    public int getBackLog() {
        return currentBackLog;
    }

    /**
     * Gets the keep alive.
     *
     * @return the keep alive
     */
    public Boolean getKeepAlive() {
        return currentKeepAlive;
    }

    /**
     * Gets the tcp no delay.
     *
     * @return the tcp no delay
     */
    public Boolean getTcpNoDelay() {
        return currentTcpNoDelay;
    }

    /**
     * Gets the reuse address.
     *
     * @return the reuse address
     */
    public Boolean getReuseAddress() {
        return currentReuseAddress;
    }

    /**
     * Gets the interruptible.
     *
     * @return the interruptible
     */
    public Boolean getInterruptible() {
        return currentInterruptible;
    }

    /**
     * Gets the privileged local port.
     *
     * @return the privileged local port
     */
    public boolean getPrivilegedLocalPort() {
        return currentPrivilegedLocalPortNumber;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return getHost(getPublicAddress());
    }

    /**
     * Gets the host.
     *
     * @param defaultHost
     *            the default host
     *
     * @return the host
     */
    public String getHost(final String defaultHost) {
        if (currentHost == null) {
            return defaultHost;
        }
        return currentHost;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort() {
        return currentPortNumber;
    }

    /**
     * Gets the server port.
     *
     * @return the server port
     */
    public int getServerPort() {
        return currentServerPortNumber;
    }

    /**
     * Gets the port.
     *
     * @param defaultPort
     *            the default port
     *
     * @return the port
     */
    public int getPort(final int defaultPort) {
        if (currentPortNumber == -1) {
            return defaultPort;
        }
        return currentPortNumber;
    }

    /**
     * Gets the socket.
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket() throws IOException {
        return getSocket(getHost(), getPort());
    }

    /**
     * Gets the socket.
     *
     * @param factory
     *            the factory
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket(final SocketFactory factory) throws IOException {
        return getSocket(factory, getHost(), getPort());
    }

    /**
     * Gets the socket.
     *
     * @param localPort
     *            the local port
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket(final int localPort) throws IOException {
        return getSocket(getHost(), getPort(), localPort);
    }

    /**
     * Gets the socket.
     *
     * @param factory
     *            the factory
     * @param localPort
     *            the local port
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket(final SocketFactory factory, final int localPort) throws IOException {
        return getSocket(factory, getHost(), getPort(), localPort);
    }

    /**
     * Gets the socket.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket(final String host, final int port) throws IOException {
        return getSocket(SocketFactory.getDefault(), host, port);
    }

    /**
     * Gets the socket.
     *
     * @param factory
     *            the factory
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket(final SocketFactory factory, final String host, final int port) throws IOException {
        if (!currentPrivilegedLocalPortNumber) {
            return getSocket(factory, host, port, 0);
        }
        synchronized (synchroPrivilegedPort) {
            var count = 0;
            while (count++ < MAX_PORT - MIN_PORT) {
                currentPrivilegedPortNumber = currentPrivilegedPortNumber < MIN_PORT ? MAX_PORT
                        : currentPrivilegedPortNumber - 1;
                try {
                    final var socket = getSocket(factory, host, port, currentPrivilegedPortNumber);
                    if (currentTrace) {
                        _log.debug("Privileged port found ({} attempt(s)): {}", count, currentPrivilegedPortNumber);
                    }
                    return socket;
                } catch (final SecurityException e) {
                    break;
                } catch (final IOException e) {
                    // Ignore and continue!
                }
            }
        }
        throw new IOException("Must run as root to bind a privileged port");
    }

    /**
     * Gets the socket.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @param localPort
     *            the local port
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket(final String host, final int port, final int localPort) throws IOException {
        return getSocket(SocketFactory.getDefault(), host, port, localPort);
    }

    /**
     * Gets the socket.
     *
     * @param factory
     *            the factory
     * @param host
     *            the host
     * @param port
     *            the port
     * @param localPort
     *            the local port
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket(final SocketFactory factory, final String host, final int port, final int localPort)
            throws IOException {
        var successful = false;
        Socket socket = null;
        try {
            try {
                socket = factory.createSocket();
                if (currentReceiveBufferSize > 0) {
                    socket.setReceiveBufferSize(currentReceiveBufferSize);
                }
                if (currentSendBufferSize > 0) {
                    socket.setSendBufferSize(currentSendBufferSize);
                }
                if (currentReuseAddress != null) {
                    socket.setReuseAddress(currentReuseAddress);
                }
                if (isNotEmpty(currentListenAddress)) {
                    socket.bind(new InetSocketAddress(InetAddress.getByName(currentListenAddress), localPort));
                } else {
                    socket.bind(new InetSocketAddress(localPort));
                }
                socket.connect(new InetSocketAddress(InetAddress.getByName(host), port), currentConnectTimeOut);
            } catch (final SocketTimeoutException e) {
                StreamPlugThread.closeQuietly(socket);
                throw e;
            } catch (final Throwable t) {
                // Unbound sockets may not be implemented (socket options ignored)!
                _log.warn("Unbound sockets implemented? connecting to {}:{} (listenAddress={},localPort={})", host,
                        port, currentListenAddress, localPort, t);
                StreamPlugThread.closeQuietly(socket);
                socket = factory.createSocket(host, port,
                        isNotEmpty(currentListenAddress) ? InetAddress.getByName(currentListenAddress) : null,
                        localPort);
            }
            socket = getConfiguredSocket(socket);
            successful = true;
            return socket;
        } finally {
            if (!successful) {
                StreamPlugThread.closeQuietly(socket);
            }
        }
    }

    /**
     * Gets the socket.
     *
     * @param serverSocket
     *            the server socket
     *
     * @return the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getSocket(final ServerSocket serverSocket) throws IOException {
        var successful = false;
        Socket socket = null;
        try {
            socket = getConfiguredSocket(serverSocket.accept());
            successful = true;
            return socket;
        } finally {
            if (!successful) {
                StreamPlugThread.closeQuietly(socket);
            }
        }
    }

    /**
     * Configure a connected plain text socket.
     *
     * @param socket
     *            the socket
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private Socket getConfiguredSocket(final Socket socket) throws IOException {
        setTCPOptions(socket);
        configureConnectedSocket(socket);
        final Socket configured;
        if (currentInterruptible) {
            if (socket instanceof SSLSocket) {
                _log.warn("SSL socket cannot be InterruptibleRMIClientSocket");
                configured = socket;
            } else {
                configured = new InterruptibleRMIClientSocket(socket);
            }
        } else {
            configured = socket;
        }
        if (statistics != null) {
            return getClientSocketFactory().getWrapper(configured);
        }
        return configured;
    }

    /**
     * Configure a connected socket. Used internally by the ClientSocketFactory and SSLClientSocketFactory.
     *
     * @param socket
     *            the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void configureConnectedSocket(final Socket socket) throws IOException {
        if (currentReceiveBufferSize > 0) {
            socket.setReceiveBufferSize(currentReceiveBufferSize);
        }
        if (currentSendBufferSize > 0) {
            socket.setSendBufferSize(currentSendBufferSize);
        }
        if (currentKeepAlive != null) {
            socket.setKeepAlive(currentKeepAlive);
        }
        if (currentTcpNoDelay != null) {
            socket.setTcpNoDelay(currentTcpNoDelay);
        }
        if (currentSoTimeOut > 0) {
            socket.setSoTimeout(currentSoTimeOut);
        }
        if (currentTrace && _log.isDebugEnabled()) {
            _log.debug("Socket configured ({}): {}", configToString(socket), socket);
        }
    }

    /**
     * Gets the server socket.
     *
     * @return the server socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public ServerSocket getServerSocket() throws IOException {
        if (!currentPrivilegedLocalPortNumber) {
            return getServerSocket(getServerPort());
        }
        for (var port = MIN_PORT; port <= MAX_PORT; port++) {
            try {
                return getServerSocket(port);
            } catch (final SecurityException e) {
                break;
            } catch (final IOException e) {
                // Ignore and continue!
            }
        }
        throw new IOException("No port available in range " + MIN_PORT + ".." + MAX_PORT);
    }

    /**
     * Gets the server socket.
     *
     * @param port
     *            the port
     *
     * @return the server socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public ServerSocket getServerSocket(final int port) throws IOException {
        return getServerSocket(ServerSocketFactory.getDefault(), port);
    }

    /**
     * Gets the server socket.
     *
     * @param factory
     *            the factory
     * @param port
     *            the port
     *
     * @return the server socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public ServerSocket getServerSocket(final ServerSocketFactory factory, final int port) throws IOException {
        var successful = false;
        ServerSocket serverSocket = null;
        try {
            try {
                serverSocket = factory.createServerSocket();
                if (currentReceiveBufferSize > 0) {
                    serverSocket.setReceiveBufferSize(currentReceiveBufferSize);
                }
                if (currentReuseAddress != null) {
                    serverSocket.setReuseAddress(currentReuseAddress);
                }
                if (isNotEmpty(currentListenAddress)) {
                    serverSocket.bind(new InetSocketAddress(InetAddress.getByName(currentListenAddress), port),
                            currentBackLog);
                } else {
                    serverSocket.bind(new InetSocketAddress(port), currentBackLog);
                }
            } catch (final Throwable t) {
                // Unbound server sockets may not be implemented (socket options ignored)!
                _log.warn("Unbound server sockets implemented? waiting on {}:{} (backLog={})", currentListenAddress,
                        port, currentBackLog, t);
                StreamPlugThread.closeQuietly(serverSocket);
                if (isNotEmpty(currentListenAddress)) {
                    serverSocket = factory.createServerSocket(port, currentBackLog,
                            InetAddress.getByName(currentListenAddress));
                } else {
                    serverSocket = factory.createServerSocket(port, currentBackLog);
                }
            }
            if (currentSoTimeOut > 0) {
                serverSocket.setSoTimeout(currentSoTimeOut);
            }
            if (currentInterruptible) {
                serverSocket = new InterruptibleRMIServerSocket(serverSocket);
            }
            if (currentTrace && _log.isDebugEnabled()) {
                _log.debug("ServerSocket created ({}): {}", configToString(serverSocket), serverSocket);
            }
            successful = true;
            return serverSocket;
        } finally {
            if (!successful) {
                StreamPlugThread.closeQuietly(serverSocket);
            }
        }
    }

    /**
     * Checks if is current rmi server thread socket alive.
     *
     * @return true, if is current rmi server thread socket alive
     */
    public static final boolean isCurrentRMIServerThreadSocketAlive() {
        return InterruptibleRMIServerSocket.isCurrentRMIServerThreadSocketAlive();
    }

    /**
     * Gets the public address.
     *
     * @param name
     *            the name
     *
     * @return the public address
     */
    public static final String getPublicAddress(final String name) {
        return new SocketConfig(name).getPublicAddress();
    }

    /**
     * Gets the local address.
     *
     * @return the local address
     */
    public static final String getLocalAddress() {
        try {
            if (currentLocalAddress == null) {
                currentLocalAddress = InetAddress.getLocalHost().getHostAddress();
            }
        } catch (final Throwable t) {
            _log.debug("Can not get localhost", t);
        }
        return currentLocalAddress;
    }

    /**
     * Config to string.
     *
     * @return the string
     */
    public String configToString() {
        final var builder = new StringBuilder();
        final var name = getName();
        if (isNotEmpty(name)) {
            builder.append("Name: ").append(name).append(", ");
        }
        builder.append("ListenAddress: ").append(getListenAddress());
        builder.append(", PublicAddress: ").append(getPublicAddress());
        builder.append(", ReuseAddress: ").append(getValue(getReuseAddress()));
        builder.append(", SoTimeOut: ").append(getSOTimeOut());
        builder.append(", ConnectTimeOut: ").append(getConnectTimeOut());
        builder.append(", ReceiveBufferSize: ").append(getValue(getReceiveBufferSize()));
        builder.append(", SendBufferSize: ").append(getValue(getSendBufferSize()));
        builder.append(", KeepAlive: ").append(getValue(getKeepAlive()));
        builder.append(", TcpNoDelay: ").append(getValue(getTcpNoDelay()));
        builder.append(", Interruptible: ").append(getInterruptible());
        builder.append(", PrivilegedLocalPort: ").append(getPrivilegedLocalPort());
        return builder.toString();
    }

    /**
     * Config to string.
     *
     * @param socket
     *            the socket
     *
     * @return the string
     *
     * @throws java.net.SocketException
     *             the socket exception
     */
    public String configToString(final Socket socket) throws SocketException {
        final var builder = new StringBuilder();
        final var name = getName();
        if (isNotEmpty(name)) {
            builder.append("Name: ").append(name).append(", ");
        }
        builder.append("ListenAddress: ").append(socket.getLocalAddress().getHostAddress());
        builder.append(", LocalPort: ").append(socket.getLocalPort());
        builder.append(", ReuseAddress: ").append(socket.getReuseAddress());
        builder.append(", SoTimeOut: ").append(socket.getSoTimeout());
        builder.append(", ConnectTimeOut: ").append(getConnectTimeOut());
        builder.append(", ReceiveBufferSize: ").append(socket.getReceiveBufferSize());
        builder.append(", SendBufferSize: ").append(socket.getSendBufferSize());
        builder.append(", KeepAlive: ").append(socket.getKeepAlive());
        builder.append(", TcpNoDelay: ").append(socket.getTcpNoDelay());
        builder.append(", Interruptible: ").append(getInterruptible());
        builder.append(", PrivilegedLocalPort: ").append(getPrivilegedLocalPort());
        return builder.toString();
    }

    /**
     * Config to string.
     *
     * @param socket
     *            the socket
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String configToString(final ServerSocket socket) throws IOException {
        final var builder = new StringBuilder();
        final var name = getName();
        if (isNotEmpty(name)) {
            builder.append("Name: ").append(name).append(", ");
        }
        builder.append("ListenAddress: ").append(socket.getInetAddress().getHostAddress());
        builder.append(", LocalPort: ").append(socket.getLocalPort());
        builder.append(", ReuseAddress: ").append(socket.getReuseAddress());
        builder.append(", SoTimeOut: ").append(socket.getSoTimeout());
        builder.append(", ConnectTimeOut: ").append(getConnectTimeOut());
        builder.append(", ReceiveBufferSize: ").append(socket.getReceiveBufferSize());
        builder.append(", Interruptible: ").append(getInterruptible());
        builder.append(", PrivilegedLocalPort: ").append(getPrivilegedLocalPort());
        builder.append(", BackLog: ").append(getBackLog());
        return builder.toString();
    }

    /**
     * Gets the value.
     *
     * @param value
     *            the value
     *
     * @return the value
     */
    private static String getValue(final Boolean value) {
        return value != null ? value.toString() : "default";
    }

    /**
     * Gets the value.
     *
     * @param value
     *            the value
     *
     * @return the value
     */
    private static String getValue(final int value) {
        return value != -1 ? "" + value : "default";
    }

    /**
     * Gets the socket factory.
     *
     * @return the socket factory
     */
    public ClientSocketFactory getSocketFactory() {
        return new ClientSocketFactory(this);
    }

    /**
     * Gets the SSL socket factory.
     *
     * @param protocol
     *            the protocol
     * @param sslValidation
     *            the ssl validation
     *
     * @return the SSL socket factory
     *
     * @throws java.security.KeyManagementException
     *             the key management exception
     * @throws java.security.NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    public SSLClientSocketFactory getSSLSocketFactory(final String protocol, final boolean sslValidation)
            throws KeyManagementException, NoSuchAlgorithmException {
        return new SSLClientSocketFactory(
                (sslValidation ? getSSLContext(protocol) : getBlindlyTrustingSSLContext(protocol)).getSocketFactory(),
                this);
    }

    /**
     * Gets the SSL context.
     *
     * @param protocol
     *            the protocol
     *
     * @return the SSL context
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws KeyManagementException
     *             the key management exception
     */
    private static SSLContext getSSLContext(final String protocol)
            throws NoSuchAlgorithmException, KeyManagementException {
        final var sslContext = SSLContext.getInstance(protocol);
        sslContext.init(null, null, null);
        return sslContext;
    }

    /**
     * Gets the blindly trusting SSL context.
     *
     * @param protocol
     *            the protocol
     *
     * @return the blindly trusting SSL context
     *
     * @throws java.security.NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws java.security.KeyManagementException
     *             the key management exception
     */
    public static SSLContext getBlindlyTrustingSSLContext(final String protocol)
            throws NoSuchAlgorithmException, KeyManagementException {
        _log.debug("Disable SSL Validation");
        final var sc = SSLContext.getInstance(protocol);
        sc.init(null, new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                // All certificates trusted
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                // All certificates trusted
            }
        } }, new java.security.SecureRandom());
        return sc;
    }
}
