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
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public SslServerSocket(final SSLServerSocket decoratee) throws IOException {
        this.decoratee = decoratee;
    }

    /**
     * {@inheritDoc}
     *
     * Accept.
     */
    @Override
    public Socket accept() throws IOException {
        return new SslSocket((SSLSocket) this.decoratee.accept());
    }

    /**
     * {@inheritDoc}
     *
     * Bind.
     */
    @Override
    public void bind(final SocketAddress endpoint, final int backlog) throws IOException {
        this.decoratee.bind(endpoint, backlog);
    }

    /**
     * {@inheritDoc}
     *
     * Bind.
     */
    @Override
    public void bind(final SocketAddress endpoint) throws IOException {
        this.decoratee.bind(endpoint);
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        this.decoratee.close();
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof SslServerSocket && this.decoratee.equals(((SslServerSocket) object).decoratee);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the channel.
     */
    @Override
    public ServerSocketChannel getChannel() {
        return this.decoratee.getChannel();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the inet address.
     */
    @Override
    public InetAddress getInetAddress() {
        return this.decoratee.getInetAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local port.
     */
    @Override
    public int getLocalPort() {
        return this.decoratee.getLocalPort();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local socket address.
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        return this.decoratee.getLocalSocketAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the receive buffer size.
     */
    @Override
    public int getReceiveBufferSize() throws SocketException {
        return this.decoratee.getReceiveBufferSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the reuse address.
     */
    @Override
    public boolean getReuseAddress() throws SocketException {
        return this.decoratee.getReuseAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the so timeout.
     */
    @Override
    public int getSoTimeout() throws IOException {
        return this.decoratee.getSoTimeout();
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return this.decoratee.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is bound.
     */
    @Override
    public boolean isBound() {
        return this.decoratee.isBound();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the want client auth.
     */
    @Override
    public boolean getWantClientAuth() {
        return this.decoratee.getWantClientAuth();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the need client auth.
     */
    @Override
    public boolean getNeedClientAuth() {
        return this.decoratee.getNeedClientAuth();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the use client mode.
     */
    @Override
    public boolean getUseClientMode() {
        return this.decoratee.getUseClientMode();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the enable session creation.
     */
    @Override
    public boolean getEnableSessionCreation() {
        return this.decoratee.getEnableSessionCreation();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is closed.
     */
    @Override
    public boolean isClosed() {
        return this.decoratee.isClosed();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the performance preferences.
     */
    @Override
    public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
        this.decoratee.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the receive buffer size.
     */
    @Override
    public void setReceiveBufferSize(final int size) throws SocketException {
        this.decoratee.setReceiveBufferSize(size);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the reuse address.
     */
    @Override
    public void setReuseAddress(final boolean on) throws SocketException {
        this.decoratee.setReuseAddress(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the so timeout.
     */
    @Override
    public void setSoTimeout(final int timeout) throws SocketException {
        this.decoratee.setSoTimeout(timeout);
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return this.decoratee.toString();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the enabled cipher suites.
     */
    @Override
    public void setEnabledCipherSuites(final String[] s) {
        this.decoratee.setEnabledCipherSuites(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the want client auth.
     */
    @Override
    public void setWantClientAuth(final boolean b) {
        this.decoratee.setWantClientAuth(b);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the need client auth.
     */
    @Override
    public void setNeedClientAuth(final boolean b) {
        this.decoratee.setNeedClientAuth(b);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the enabled protocols.
     */
    @Override
    public void setEnabledProtocols(final String[] p) {
        this.decoratee.setEnabledProtocols(p);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the use client mode.
     */
    @Override
    public void setUseClientMode(final boolean b) {
        this.decoratee.setUseClientMode(b);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the enable session creation.
     */
    @Override
    public void setEnableSessionCreation(final boolean b) {
        this.decoratee.setEnableSessionCreation(b);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the supported cipher suites.
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return this.decoratee.getSupportedCipherSuites();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the enabled cipher suites.
     */
    @Override
    public String[] getEnabledCipherSuites() {
        return this.decoratee.getEnabledCipherSuites();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the supported protocols.
     */
    @Override
    public String[] getSupportedProtocols() {
        return this.decoratee.getSupportedProtocols();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the enabled protocols.
     */
    @Override
    public String[] getEnabledProtocols() {
        return this.decoratee.getEnabledProtocols();
    }
}
