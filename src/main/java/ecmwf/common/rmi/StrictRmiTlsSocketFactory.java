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
 */

package ecmwf.common.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/** Mutual TLS for JRMP using the local JSSE key and trust stores, without legacy trust exceptions. */
public final class StrictRmiTlsSocketFactory extends SocketConfig
        implements java.rmi.server.RMIClientSocketFactory, java.rmi.server.RMIServerSocketFactory, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int HANDSHAKE_TIMEOUT = 10_000;

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        load("StrictRmiTlsSocketFactory");
        final var tls = context().getSocketFactory();
        final var tcp = new Socket();
        boolean connected = false;
        try {
            if (getListenAddress() != null && !getListenAddress().isBlank()) {
                tcp.bind(new InetSocketAddress(getListenAddress(), 0));
            }
            tcp.connect(new InetSocketAddress(host, port),
                    getConnectTimeOut() > 0 ? getConnectTimeOut() : HANDSHAKE_TIMEOUT);
            final var socket = (SSLSocket) tls.createSocket(tcp, host, port, true);
            final var parameters = socket.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            socket.setSSLParameters(parameters);
            configureSocket(socket);
            socket.setSoTimeout(HANDSHAKE_TIMEOUT);
            socket.startHandshake();
            socket.setSoTimeout(Math.max(0, getSOTimeOut()));
            connected = true;
            return socket;
        } finally {
            if (!connected) {
                tcp.close();
            }
        }
    }

    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        load("StrictRmiTlsSocketFactory");
        final var socket = (SSLServerSocket) context().getServerSocketFactory().createServerSocket();
        boolean bound = false;
        try {
            socket.setNeedClientAuth(true);
            if (getReceiveBufferSize() > 0) {
                socket.setReceiveBufferSize(getReceiveBufferSize());
            }
            if (getReuseAddress() != null) {
                socket.setReuseAddress(getReuseAddress());
            }
            final var address = getListenAddress();
            socket.bind(address == null || address.isBlank() ? new InetSocketAddress(port)
                    : new InetSocketAddress(address, port), getBackLog());
            bound = true;
            return socket;
        } finally {
            if (!bound) {
                socket.close();
            }
        }
    }

    private static SSLContext context() throws IOException {
        try {
            return SSLContext.getDefault();
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException("Unable to initialize RMI TLS", e);
        }
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof StrictRmiTlsSocketFactory;
    }

    @Override
    public int hashCode() {
        return StrictRmiTlsSocketFactory.class.hashCode();
    }
}
