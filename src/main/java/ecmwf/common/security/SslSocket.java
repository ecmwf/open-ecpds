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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
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
     * {@inheritDoc}
     *
     * Bind.
     */
    @Override
    public void bind(final SocketAddress bindpoint) throws IOException {
        this.decoratee.bind(bindpoint);
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
     * Connect.
     */
    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        this.decoratee.connect(endpoint);
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     */
    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        this.decoratee.connect(endpoint, timeout);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof SslSocket && this.decoratee.equals(((SslSocket) object).decoratee);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the channel.
     */
    @Override
    public SocketChannel getChannel() {
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
     * Gets the input stream.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return this.decoratee.getInputStream();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the keep alive.
     */
    @Override
    public boolean getKeepAlive() throws SocketException {
        return this.decoratee.getKeepAlive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local address.
     */
    @Override
    public InetAddress getLocalAddress() {
        return this.decoratee.getLocalAddress();
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
     * Gets the OOB inline.
     */
    @Override
    public boolean getOOBInline() throws SocketException {
        return this.decoratee.getOOBInline();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the output stream.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new SslSocketOutputStream(this, this.decoratee.getOutputStream());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort() {
        return this.decoratee.getPort();
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
     * Gets the remote socket address.
     */
    @Override
    public SocketAddress getRemoteSocketAddress() {
        return this.decoratee.getRemoteSocketAddress();
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
     * Gets the send buffer size.
     */
    @Override
    public int getSendBufferSize() throws SocketException {
        return this.decoratee.getSendBufferSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the so linger.
     */
    @Override
    public int getSoLinger() throws SocketException {
        return this.decoratee.getSoLinger();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the so timeout.
     */
    @Override
    public int getSoTimeout() throws SocketException {
        return this.decoratee.getSoTimeout();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the tcp no delay.
     */
    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return this.decoratee.getTcpNoDelay();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the traffic class.
     */
    @Override
    public int getTrafficClass() throws SocketException {
        return this.decoratee.getTrafficClass();
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
     * Checks if is closed.
     */
    @Override
    public boolean isClosed() {
        return this.decoratee.isClosed();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is connected.
     */
    @Override
    public boolean isConnected() {
        return this.decoratee.isConnected();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is input shutdown.
     */
    @Override
    public boolean isInputShutdown() {
        return this.decoratee.isInputShutdown();
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is output shutdown.
     */
    @Override
    public boolean isOutputShutdown() {
        return this.decoratee.isOutputShutdown();
    }

    /**
     * {@inheritDoc}
     *
     * Send urgent data.
     */
    @Override
    public void sendUrgentData(final int data) throws IOException {
        this.decoratee.sendUrgentData(data);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the keep alive.
     */
    @Override
    public void setKeepAlive(final boolean on) throws SocketException {
        this.decoratee.setKeepAlive(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the OOB inline.
     */
    @Override
    public void setOOBInline(final boolean on) throws SocketException {
        this.decoratee.setOOBInline(on);
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
     * Sets the send buffer size.
     */
    @Override
    public void setSendBufferSize(final int size) throws SocketException {
        this.decoratee.setSendBufferSize(size);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the so linger.
     */
    @Override
    public void setSoLinger(final boolean on, final int linger) throws SocketException {
        this.decoratee.setSoLinger(on, linger);
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
     * Sets the tcp no delay.
     */
    @Override
    public void setTcpNoDelay(final boolean on) throws SocketException {
        this.decoratee.setTcpNoDelay(on);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the traffic class.
     */
    @Override
    public void setTrafficClass(final int tc) throws SocketException {
        this.decoratee.setTrafficClass(tc);
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown input.
     */
    @Override
    public void shutdownInput() throws IOException {
        this.decoratee.shutdownInput();
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown output.
     */
    @Override
    public void shutdownOutput() throws IOException {
        this.decoratee.shutdownOutput();
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
     * Adds the handshake completed listener.
     */
    @Override
    public void addHandshakeCompletedListener(final HandshakeCompletedListener l) {
        this.decoratee.addHandshakeCompletedListener(l);
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
     * Sets the enabled cipher suites.
     */
    @Override
    public void setEnabledCipherSuites(final String[] s) {
        this.decoratee.setEnabledCipherSuites(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the enabled protocols.
     */
    @Override
    public void setEnabledProtocols(final String[] s) {
        this.decoratee.setEnabledProtocols(s);
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
     * Removes the handshake completed listener.
     */
    @Override
    public void removeHandshakeCompletedListener(final HandshakeCompletedListener l) {
        this.decoratee.removeHandshakeCompletedListener(l);
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
     * Sets the need client auth.
     */
    @Override
    public void setNeedClientAuth(final boolean b) {
        this.decoratee.setNeedClientAuth(b);
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
     * Start handshake.
     */
    @Override
    public void startHandshake() throws IOException {
        this.decoratee.startHandshake();
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
     * Gets the want client auth.
     */
    @Override
    public boolean getWantClientAuth() {
        return this.decoratee.getWantClientAuth();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the session.
     */
    @Override
    public SSLSession getSession() {
        return this.decoratee.getSession();
    }
}
