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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/**
 * The Class SslServerSocket.
 */
public class SslServerSocket extends SSLServerSocket {
    /** The decoratee. */
    private final SSLServerSocket decoratee;

    /**
     * Instantiates a new ssl server socket.
     *
     * @param decoratee
     *            the decoratee
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public SslServerSocket(final SSLServerSocket decoratee) throws IOException {
        this.decoratee = decoratee;
    }

    /**
     * Accept.
     *
     * @return the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Socket accept() throws IOException {
        return new SslSocket((SSLSocket) this.decoratee.accept());
    }

    /**
     * Bind.
     *
     * @param endpoint
     *            the endpoint
     * @param backlog
     *            the backlog
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void bind(final SocketAddress endpoint, final int backlog) throws IOException {
        this.decoratee.bind(endpoint, backlog);
    }

    /**
     * Bind.
     *
     * @param endpoint
     *            the endpoint
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void bind(final SocketAddress endpoint) throws IOException {
        this.decoratee.bind(endpoint);
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
     * Equals.
     *
     * @param object
     *            the object
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof SslServerSocket && this.decoratee.equals(((SslServerSocket) object).decoratee);
    }

    /**
     * Gets the channel.
     *
     * @return the channel
     */
    @Override
    public ServerSocketChannel getChannel() {
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
     * Gets the so timeout.
     *
     * @return the so timeout
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int getSoTimeout() throws IOException {
        return this.decoratee.getSoTimeout();
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
     * Gets the want client auth.
     *
     * @return the want client auth
     */
    @Override
    public boolean getWantClientAuth() {
        return this.decoratee.getWantClientAuth();
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
     * Checks if is closed.
     *
     * @return true, if is closed
     */
    @Override
    public boolean isClosed() {
        return this.decoratee.isClosed();
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
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return this.decoratee.toString();
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
     * Sets the enabled protocols.
     *
     * @param p
     *            the new enabled protocols
     */
    @Override
    public void setEnabledProtocols(final String[] p) {
        this.decoratee.setEnabledProtocols(p);
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
}
