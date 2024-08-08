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

package ecmwf.common.security;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * The Class SslSocket.
 */
public class SslSocket extends SSLSocket {
    /** The decoratee. */
    protected final SSLSocket decoratee;

    /**
     * Instantiates a new ssl socket.
     *
     * @param decoratee
     *            the decoratee
     */
    SslSocket(final SSLSocket decoratee) {
        this.decoratee = decoratee;
    }

    /**
     * Bind.
     *
     * @param bindpoint
     *            the bindpoint
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void bind(final SocketAddress bindpoint) throws IOException {
        this.decoratee.bind(bindpoint);
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        this.decoratee.close();
    }

    /**
     * Connect.
     *
     * @param endpoint
     *            the endpoint
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        this.decoratee.connect(endpoint);
    }

    /**
     * Connect.
     *
     * @param endpoint
     *            the endpoint
     * @param timeout
     *            the timeout
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        this.decoratee.connect(endpoint, timeout);
    }

    /**
     * Equals.
     *
     * @param object
     *            the object
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof SslSocket && this.decoratee.equals(((SslSocket) object).decoratee);
    }

    /**
     * Gets the channel.
     *
     * @return the channel
     */
    @Override
    public SocketChannel getChannel() {
        return this.decoratee.getChannel();
    }

    /**
     * Gets the inet address.
     *
     * @return the inet address
     */
    @Override
    public InetAddress getInetAddress() {
        return this.decoratee.getInetAddress();
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return this.decoratee.getInputStream();
    }

    /**
     * Gets the keep alive.
     *
     * @return the keep alive
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public boolean getKeepAlive() throws SocketException {
        return this.decoratee.getKeepAlive();
    }

    /**
     * Gets the local address.
     *
     * @return the local address
     */
    @Override
    public InetAddress getLocalAddress() {
        return this.decoratee.getLocalAddress();
    }

    /**
     * Gets the local port.
     *
     * @return the local port
     */
    @Override
    public int getLocalPort() {
        return this.decoratee.getLocalPort();
    }

    /**
     * Gets the local socket address.
     *
     * @return the local socket address
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        return this.decoratee.getLocalSocketAddress();
    }

    /**
     * Gets the OOB inline.
     *
     * @return the OOB inline
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public boolean getOOBInline() throws SocketException {
        return this.decoratee.getOOBInline();
    }

    /**
     * Gets the output stream.
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new SslSocketOutputStream(this, this.decoratee.getOutputStream());
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    @Override
    public int getPort() {
        return this.decoratee.getPort();
    }

    /**
     * Gets the receive buffer size.
     *
     * @return the receive buffer size
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public int getReceiveBufferSize() throws SocketException {
        return this.decoratee.getReceiveBufferSize();
    }

    /**
     * Gets the remote socket address.
     *
     * @return the remote socket address
     */
    @Override
    public SocketAddress getRemoteSocketAddress() {
        return this.decoratee.getRemoteSocketAddress();
    }

    /**
     * Gets the reuse address.
     *
     * @return the reuse address
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public boolean getReuseAddress() throws SocketException {
        return this.decoratee.getReuseAddress();
    }

    /**
     * Gets the send buffer size.
     *
     * @return the send buffer size
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public int getSendBufferSize() throws SocketException {
        return this.decoratee.getSendBufferSize();
    }

    /**
     * Gets the so linger.
     *
     * @return the so linger
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public int getSoLinger() throws SocketException {
        return this.decoratee.getSoLinger();
    }

    /**
     * Gets the so timeout.
     *
     * @return the so timeout
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public int getSoTimeout() throws SocketException {
        return this.decoratee.getSoTimeout();
    }

    /**
     * Gets the tcp no delay.
     *
     * @return the tcp no delay
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return this.decoratee.getTcpNoDelay();
    }

    /**
     * Gets the traffic class.
     *
     * @return the traffic class
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public int getTrafficClass() throws SocketException {
        return this.decoratee.getTrafficClass();
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return this.decoratee.hashCode();
    }

    /**
     * Checks if is bound.
     *
     * @return true, if is bound
     */
    @Override
    public boolean isBound() {
        return this.decoratee.isBound();
    }

    /**
     * Checks if is closed.
     *
     * @return true, if is closed
     */
    @Override
    public boolean isClosed() {
        return this.decoratee.isClosed();
    }

    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    @Override
    public boolean isConnected() {
        return this.decoratee.isConnected();
    }

    /**
     * Checks if is input shutdown.
     *
     * @return true, if is input shutdown
     */
    @Override
    public boolean isInputShutdown() {
        return this.decoratee.isInputShutdown();
    }

    /**
     * Checks if is output shutdown.
     *
     * @return true, if is output shutdown
     */
    @Override
    public boolean isOutputShutdown() {
        return this.decoratee.isOutputShutdown();
    }

    /**
     * Send urgent data.
     *
     * @param data
     *            the data
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void sendUrgentData(final int data) throws IOException {
        this.decoratee.sendUrgentData(data);
    }

    /**
     * Sets the keep alive.
     *
     * @param on
     *            the new keep alive
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setKeepAlive(final boolean on) throws SocketException {
        this.decoratee.setKeepAlive(on);
    }

    /**
     * Sets the OOB inline.
     *
     * @param on
     *            the new OOB inline
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setOOBInline(final boolean on) throws SocketException {
        this.decoratee.setOOBInline(on);
    }

    /**
     * Sets the performance preferences.
     *
     * @param connectionTime
     *            the connection time
     * @param latency
     *            the latency
     * @param bandwidth
     *            the bandwidth
     */
    @Override
    public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
        this.decoratee.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    /**
     * Sets the receive buffer size.
     *
     * @param size
     *            the new receive buffer size
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setReceiveBufferSize(final int size) throws SocketException {
        this.decoratee.setReceiveBufferSize(size);
    }

    /**
     * Sets the reuse address.
     *
     * @param on
     *            the new reuse address
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setReuseAddress(final boolean on) throws SocketException {
        this.decoratee.setReuseAddress(on);
    }

    /**
     * Sets the send buffer size.
     *
     * @param size
     *            the new send buffer size
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setSendBufferSize(final int size) throws SocketException {
        this.decoratee.setSendBufferSize(size);
    }

    /**
     * Sets the so linger.
     *
     * @param on
     *            the on
     * @param linger
     *            the linger
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setSoLinger(final boolean on, final int linger) throws SocketException {
        this.decoratee.setSoLinger(on, linger);
    }

    /**
     * Sets the so timeout.
     *
     * @param timeout
     *            the new so timeout
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setSoTimeout(final int timeout) throws SocketException {
        this.decoratee.setSoTimeout(timeout);
    }

    /**
     * Sets the tcp no delay.
     *
     * @param on
     *            the new tcp no delay
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setTcpNoDelay(final boolean on) throws SocketException {
        this.decoratee.setTcpNoDelay(on);
    }

    /**
     * Sets the traffic class.
     *
     * @param tc
     *            the new traffic class
     *
     * @throws SocketException
     *             the socket exception
     */
    @Override
    public void setTrafficClass(final int tc) throws SocketException {
        this.decoratee.setTrafficClass(tc);
    }

    /**
     * Shutdown input.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void shutdownInput() throws IOException {
        this.decoratee.shutdownInput();
    }

    /**
     * Shutdown output.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void shutdownOutput() throws IOException {
        this.decoratee.shutdownOutput();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return this.decoratee.toString();
    }

    /**
     * Adds the handshake completed listener.
     *
     * @param l
     *            the l
     */
    @Override
    public void addHandshakeCompletedListener(final HandshakeCompletedListener l) {
        this.decoratee.addHandshakeCompletedListener(l);
    }

    /**
     * Gets the supported protocols.
     *
     * @return the supported protocols
     */
    @Override
    public String[] getSupportedProtocols() {
        return this.decoratee.getSupportedProtocols();
    }

    /**
     * Gets the enabled protocols.
     *
     * @return the enabled protocols
     */
    @Override
    public String[] getEnabledProtocols() {
        return this.decoratee.getEnabledProtocols();
    }

    /**
     * Gets the supported cipher suites.
     *
     * @return the supported cipher suites
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return this.decoratee.getSupportedCipherSuites();
    }

    /**
     * Gets the enabled cipher suites.
     *
     * @return the enabled cipher suites
     */
    @Override
    public String[] getEnabledCipherSuites() {
        return this.decoratee.getEnabledCipherSuites();
    }

    /**
     * Sets the enabled cipher suites.
     *
     * @param s
     *            the new enabled cipher suites
     */
    @Override
    public void setEnabledCipherSuites(final String[] s) {
        this.decoratee.setEnabledCipherSuites(s);
    }

    /**
     * Sets the enabled protocols.
     *
     * @param s
     *            the new enabled protocols
     */
    @Override
    public void setEnabledProtocols(final String[] s) {
        this.decoratee.setEnabledProtocols(s);
    }

    /**
     * Sets the use client mode.
     *
     * @param b
     *            the new use client mode
     */
    @Override
    public void setUseClientMode(final boolean b) {
        this.decoratee.setUseClientMode(b);
    }

    /**
     * Gets the use client mode.
     *
     * @return the use client mode
     */
    @Override
    public boolean getUseClientMode() {
        return this.decoratee.getUseClientMode();
    }

    /**
     * Gets the enable session creation.
     *
     * @return the enable session creation
     */
    @Override
    public boolean getEnableSessionCreation() {
        return this.decoratee.getEnableSessionCreation();
    }

    /**
     * Removes the handshake completed listener.
     *
     * @param l
     *            the l
     */
    @Override
    public void removeHandshakeCompletedListener(final HandshakeCompletedListener l) {
        this.decoratee.removeHandshakeCompletedListener(l);
    }

    /**
     * Sets the enable session creation.
     *
     * @param b
     *            the new enable session creation
     */
    @Override
    public void setEnableSessionCreation(final boolean b) {
        this.decoratee.setEnableSessionCreation(b);
    }

    /**
     * Sets the need client auth.
     *
     * @param b
     *            the new need client auth
     */
    @Override
    public void setNeedClientAuth(final boolean b) {
        this.decoratee.setNeedClientAuth(b);
    }

    /**
     * Sets the want client auth.
     *
     * @param b
     *            the new want client auth
     */
    @Override
    public void setWantClientAuth(final boolean b) {
        this.decoratee.setWantClientAuth(b);
    }

    /**
     * Start handshake.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void startHandshake() throws IOException {
        this.decoratee.startHandshake();
    }

    /**
     * Gets the need client auth.
     *
     * @return the need client auth
     */
    @Override
    public boolean getNeedClientAuth() {
        return this.decoratee.getNeedClientAuth();
    }

    /**
     * Gets the want client auth.
     *
     * @return the want client auth
     */
    @Override
    public boolean getWantClientAuth() {
        return this.decoratee.getWantClientAuth();
    }

    /**
     * Gets the session.
     *
     * @return the session
     */
    @Override
    public SSLSession getSession() {
        return this.decoratee.getSession();
    }
}
