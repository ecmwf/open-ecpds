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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.security.SSLSocketFactory;
import ecmwf.common.text.Format;

/**
 * The Class ProxySocket.
 */
public final class ProxySocket implements Serializable, Closeable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7289886343909451972L;

    /** The Constant _proxys. */
    private static final Hashtable<String, PSElement> _proxys = new Hashtable<>();

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ProxySocket.class);

    /** The Constant _factory. */
    private static final SocketFactory _factory = _getSocketFactory();

    /** The _data host. */
    private final String _dataHost;

    /** The _data port. */
    private final int _dataPort;

    /** The _ticket. */
    private final long _ticket;

    /** The _source. */
    private final String _source;

    /** The _target. */
    private final String _target;

    /** The _maxBytesPerSec. */
    private long _maxBytesPerSec = 0;

    /** The _objects. */
    private final Hashtable<String, Object> _objects = new Hashtable<>();

    /** The _is direct. */
    private final boolean _isDirect;

    /** The _socket config. */
    private transient SocketConfig _socketConfig = null;

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "DataHost: " + _dataHost + ":" + _dataPort + " Ticket: " + _ticket + " Source: " + _source + " Target: "
                + _target + " Direct: " + _isDirect;
    }

    /**
     * Instantiates a new proxy socket.
     *
     * @param source
     *            the source
     * @param ticket
     *            the ticket
     */
    public ProxySocket(final String source, final long ticket) {
        _source = source;
        _dataHost = null;
        _dataPort = 0;
        _target = null;
        _ticket = ticket;
        _isDirect = false;
        _log.debug("ProxySocket created (" + toString() + ")");
    }

    /**
     * Gets the socket factory.
     *
     * @return the socket factory
     */
    private static SocketFactory _getSocketFactory() {
        final var localName = SocketConfig.getPublicAddress("ProxySocket");
        final var definedName = Cnf.at("ProxySocket", "host", localName);
        var plainText = false;
        try {
            final var defined = InetAddress.getByName(definedName);
            final var definedAddress = defined.getHostAddress();
            final var local = InetAddress.getByName(localName);
            final var localAddress = local.getHostAddress();
            plainText = Cnf.at("ProxySocket", "plainText", localAddress.equals(definedAddress));
            _log.debug("definedAddress: " + definedAddress + ", localAddress: " + localAddress + ", plainText: "
                    + plainText);
        } catch (final Throwable t) {
            if (_log != null) {
                _log.warn("Force secure connection", t);
            }
        }
        if (plainText) {
            return SocketConfig.DEFAULT_FACTORY;
        }
        return SSLSocketFactory.getSSLSocketFactory();
    }

    /**
     * Instantiates a new proxy socket.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public ProxySocket(final String source, final String target) throws IOException {
        this(new SocketConfig("ProxySocket"), source, target);
    }

    /**
     * Instantiates a new proxy socket.
     *
     * @param socketConfig
     *            the socket config
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public ProxySocket(final SocketConfig socketConfig, final String source, final String target) throws IOException {
        final var host = Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress());
        final var port = Cnf.at("ProxySocket", "port", 646);
        final var socket = _getCustomizedSocket(socketConfig, host, port);
        _dataPort = 0;
        _dataHost = null;
        _isDirect = false;
        _source = source;
        _target = target;
        final var name = getName();
        final var ps = new PrintStream(socket.getOutputStream());
        ps.print("C" + Format.formatLong(name.length(), 4, true) + name);
        ps.flush();
        final var in = socket.getInputStream();
        if ((char) in.read() != '0') {
            socket.close();
            final var message = "Ticket at " + host + ":" + port + " not created";
            _log.warn(message);
            throw new IOException(message);
        }
        final var buf = new byte[10];
        if (StreamPlugThread.readFully(in, buf, 0, 10) != 10) {
            throw new IOException("Unexpected ticket");
        }
        _ticket = Long.parseLong(new String(buf));
        _proxys.put(name + ":" + _ticket, new PSElement(this, socket));
        _log.debug("ProxySocket created (" + toString() + ")");
    }

    /**
     * Instantiates a new proxy socket.
     *
     * @param ticket
     *            the ticket
     * @param dataHost
     *            the data host
     * @param dataPort
     *            the data port
     * @param isDirect
     *            the is direct
     */
    public ProxySocket(final long ticket, final String dataHost, final int dataPort, final boolean isDirect) {
        _source = null;
        _target = null;
        _ticket = ticket;
        _dataHost = dataHost;
        _dataPort = dataPort;
        _isDirect = isDirect;
        _log.debug("ProxySocket created (" + toString() + ")");
    }

    /**
     * Sets the maximum bytes per seconds.
     *
     * @param maxBytesPerSec
     *            the new max bytes per sec
     */
    public void setMaxBytesPerSec(final long maxBytesPerSec) {
        _maxBytesPerSec = maxBytesPerSec;
    }

    /**
     * Gets the socket config.
     *
     * @return the socket config
     */
    public synchronized SocketConfig getSocketConfig() {
        if (_socketConfig == null) {
            _socketConfig = new SocketConfig("ProxySocket");
        }
        return _socketConfig;
    }

    /**
     * Sets the socket config.
     *
     * @param socketConfig
     *            the new socket config
     */
    public synchronized void setSocketConfig(final SocketConfig socketConfig) {
        _socketConfig = socketConfig;
    }

    /**
     * Gets the data socket.
     *
     * @return the data socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getDataSocket() throws IOException {
        return getDataSocket(getSocketConfig());
    }

    /**
     * Gets the data socket.
     *
     * @param socketConfig
     *            the socket config
     *
     * @return the data socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Socket getDataSocket(final SocketConfig socketConfig) throws IOException {
        return _getDataSocket(socketConfig, _ticket, _dataHost, _dataPort, _isDirect);
    }

    /**
     * Gets the data socket.
     *
     * @param socketConfig
     *            the socket config
     * @param ticket
     *            the ticket
     * @param dataHost
     *            the data host
     * @param dataPort
     *            the data port
     * @param isDirect
     *            the is direct
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static Socket _getDataSocket(final SocketConfig socketConfig, final long ticket, final String dataHost,
            final int dataPort, final boolean isDirect) throws IOException {
        _log.debug("getDataSocket (DataHost: " + dataHost + ":" + dataPort + " Ticket: " + ticket + " Direct: "
                + isDirect + ")");
        final var socket = _getCustomizedSocket(socketConfig, dataHost, dataPort);
        final var soTimeOut = socket.getSoTimeout();
        socket.setSoTimeout(180000);
        final var ps = new PrintStream(socket.getOutputStream());
        final var in = socket.getInputStream();
        if (!isDirect) {
            ps.print("P" + Format.formatLong(ticket, 10, true));
            ps.flush();
            final var result = (char) in.read();
            if (result != '0') {
                StreamPlugThread.closeQuietly(socket);
                final var message = result == (char) -1 ? "Connection to data channel closed"
                        : "Data ticket " + ticket + "@" + dataHost + ":" + dataPort + " not found (" + result + ")";
                _log.warn(message);
                throw new IOException(message);
            }
        } else {
            ps.print("T" + Format.formatLong(ticket, 10, true));
            ps.flush();
            final var result = (char) in.read();
            if (result != '0' && result != '1') {
                StreamPlugThread.closeQuietly(socket);
                final var message = result == (char) -1
                        ? "Connection to data channel closed (" + dataHost + ":" + dataPort + ")"
                        : "Data ticket " + ticket + "@" + dataHost + ":" + dataPort + " not found (" + result + ")";
                _log.warn(message);
                throw new IOException(message);
            }
        }
        socket.setSoTimeout(soTimeOut);
        return socket;
    }

    /**
     * Gets the customized socket.
     *
     * @param socketConfig
     *            the socket config
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static Socket _getCustomizedSocket(final SocketConfig socketConfig, final String host, final int port)
            throws IOException {
        if (host == null) {
            throw new IOException("No dataHost to access in ProxySocket");
        }
        final var socket = socketConfig.getSocket(_factory, host, port);
        if (socket instanceof SSLSocket) {
            _log.debug("SSL connection detected");
            final var cipherSuites = Cnf.listAt("ProxySocket", "cipherSuites");
            if (cipherSuites.size() > 0) {
                // SSL connection.
                ((SSLSocket) socket).setEnabledCipherSuites(cipherSuites.toArray(new String[cipherSuites.size()]));
                _log.debug("Enabled cipher suites: " + Cnf.at("ProxySocket", "cipherSuites"));
            }
        }
        return socket;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() {
        final var name = getName() + ":" + _ticket;
        final var element = _proxys.remove(name);
        if (element != null) {
            try {
                element.getSocket().close();
            } catch (final IOException e) {
                _log.debug(e);
            }
        }
    }

    /**
     * Gets the data input stream.
     *
     * @param socketConfig
     *            the socket config
     * @param ticket
     *            the ticket
     *
     * @return the data input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static InputStream _getDataInputStream(final SocketConfig socketConfig, final long ticket)
            throws IOException {
        return _getDataSocket(socketConfig, ticket,
                Cnf.at("ProxySocket", "dataHost", Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress())),
                Cnf.at("ProxySocket", "dataPort", Cnf.at("ProxySocket", "port", 646)),
                Cnf.at("ProxySocket", "dataHost") != null).getInputStream();
    }

    /**
     * Gets the data input stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static InputStream getDataInputStream(final long ticket) throws IOException {
        return _getDataInputStream(new SocketConfig("ProxySocket"), ticket);
    }

    /**
     * Gets the data input stream.
     *
     * @param socketConfig
     *            the socket config
     *
     * @return the data input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private InputStream _getDataInputStream(final SocketConfig socketConfig) throws IOException {
        return _getDataSocket(socketConfig, _ticket,
                _isDirect ? _dataHost : Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress()),
                _isDirect ? _dataPort : Cnf.at("ProxySocket", "port", 646), _isDirect).getInputStream();
    }

    /**
     * Gets the data input stream.
     *
     * @return the data input stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public InputStream getDataInputStream() throws IOException {
        final var in = _getDataInputStream(getSocketConfig());
        // Check if the input stream should be throttled or not?
        return _maxBytesPerSec <= 0 ? in : new ThrottledInputStream(in, _maxBytesPerSec);
    }

    /**
     * Gets the data output stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static OutputStream getDataOutputStream(final long ticket) throws IOException {
        return _getDataOutputStream(new SocketConfig("ProxySocket"), ticket);
    }

    /**
     * Gets the data output stream.
     *
     * @param socketConfig
     *            the socket config
     * @param ticket
     *            the ticket
     *
     * @return the data output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static OutputStream _getDataOutputStream(final SocketConfig socketConfig, final long ticket)
            throws IOException {
        return _getDataSocket(socketConfig, ticket,
                Cnf.at("ProxySocket", "dataHost", Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress())),
                Cnf.at("ProxySocket", "dataPort", Cnf.at("ProxySocket", "port", 646)),
                Cnf.at("ProxySocket", "dataHost") != null).getOutputStream();
    }

    /**
     * Gets the data output stream.
     *
     * @return the data output stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public OutputStream getDataOutputStream() throws IOException {
        final var out = _getDataOutputStream(getSocketConfig());
        // Check if the input stream should be throttled or not?
        return _maxBytesPerSec <= 0 ? out : new ThrottledOutputStream(out, _maxBytesPerSec);
    }

    /**
     * Gets the data output stream.
     *
     * @param socketConfig
     *            the socket config
     *
     * @return the data output stream
     *
     * @throws UnknownHostException
     *             the unknown host exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private OutputStream _getDataOutputStream(final SocketConfig socketConfig)
            throws UnknownHostException, IOException {
        return _getDataSocket(socketConfig, _ticket,
                _isDirect ? _dataHost : Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress()),
                _isDirect ? _dataPort : Cnf.at("ProxySocket", "port", 646), _isDirect).getOutputStream();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return _source + (_target == null ? "" : ":" + _target);
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
        final var element = _proxys.get(getName() + ":" + _ticket);
        if (element != null) {
            return element.getSocket();
        }
        throw new IOException("proxy socket not found");
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return _source;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return _target;
    }

    /**
     * Gets the ticket.
     *
     * @return the ticket
     */
    public long getTicket() {
        return _ticket;
    }

    /**
     * Open.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void open() throws IOException {
        open(getSocketConfig());
    }

    /**
     * Open.
     *
     * @param socketConfig
     *            the socket config
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void open(final SocketConfig socketConfig) throws IOException {
        _log.debug("ProxySocket open (" + toString() + ")");
        final var host = Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress());
        final var port = Cnf.at("ProxySocket", "port", 646);
        final var socket = _getCustomizedSocket(socketConfig, host, port);
        final var ps = new PrintStream(socket.getOutputStream());
        final var in = socket.getInputStream();
        final var name = getName();
        ps.print("O" + Format.formatLong(_ticket, 10, true) + Format.formatLong(name.length(), 4, true) + name);
        ps.flush();
        final char result;
        if ((result = (char) in.read()) != '0') {
            StreamPlugThread.closeQuietly(socket);
            final var message = result == (char) -1 ? "Connection to data channel closed"
                    : "Data ticket " + _ticket + "@" + host + ":" + port + " not found (" + result + ")";
            _log.warn(message);
            throw new IOException(message);
        }
        _proxys.put(name + ":" + _ticket, new PSElement(this, socket));
    }

    /**
     * Tunnel.
     *
     * @param aHost
     *            the a host
     * @param aPort
     *            the a port
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void tunnel(final String aHost, final int aPort) throws IOException {
        tunnel(getSocketConfig(), aHost, aPort);
    }

    /**
     * Tunnel.
     *
     * @param socketConfig
     *            the socket config
     * @param aHost
     *            the a host
     * @param aPort
     *            the a port
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void tunnel(final SocketConfig socketConfig, final String aHost, final int aPort) throws IOException {
        _log.debug("ProxySocket tunnel (" + toString() + ")");
        final var host = Cnf.at("ProxySocket", "host", socketConfig.getPublicAddress());
        final var port = Cnf.at("ProxySocket", "port", 646);
        final var socket = _getCustomizedSocket(socketConfig, host, port);
        final var ps = new PrintStream(socket.getOutputStream());
        final var in = socket.getInputStream();
        final var name = getName();
        final var srvInfo = aHost + ":" + aPort;
        ps.print("F" + Format.formatLong(_ticket, 10, true) + Format.formatLong(name.length(), 4, true) + name
                + Format.formatLong(srvInfo.length(), 4, true) + srvInfo);
        ps.flush();
        final char result;
        if ((result = (char) in.read()) != '0') {
            StreamPlugThread.closeQuietly(socket);
            final var message = result == (char) -1 ? "Connection to data channel closed"
                    : "Data ticket " + _ticket + "@" + host + ":" + port + " not found (" + result + ")";
            _log.warn(message);
            throw new IOException(message);
        }
        _proxys.put(name + ":" + _ticket, new PSElement(this, socket));
    }

    /**
     * Gets the data host.
     *
     * @return the data host
     */
    public String getDataHost() {
        return _dataHost;
    }

    /**
     * Gets the data port.
     *
     * @return the data port
     */
    public int getDataPort() {
        return _dataPort;
    }

    /**
     * Adds the object.
     *
     * @param key
     *            the key
     * @param object
     *            the object
     */
    public void addObject(final String key, final Object object) {
        if (object == null) {
            _objects.remove(key);
        } else {
            _objects.put(key, object);
        }
    }

    /**
     * Gets the object.
     *
     * @param key
     *            the key
     *
     * @return the object
     */
    public Object getObject(final String key) {
        return _objects.get(key);
    }

    /**
     * The Class PSElement.
     */
    static final class PSElement {
        /** The _proxy. */
        private final ProxySocket _proxy;

        /** The _socket. */
        private final Socket _socket;

        /**
         * Instantiates a new PS element.
         *
         * @param proxy
         *            the proxy
         * @param socket
         *            the socket
         */
        public PSElement(final ProxySocket proxy, final Socket socket) {
            _proxy = proxy;
            _socket = socket;
        }

        /**
         * Gets the proxy.
         *
         * @return the proxy
         */
        ProxySocket getProxy() {
            return _proxy;
        }

        /**
         * Gets the socket.
         *
         * @return the socket
         */
        Socket getSocket() {
            return _socket;
        }
    }
}
