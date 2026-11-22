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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/** Mutual TLS for JRMP using the local JSSE key and trust stores, without legacy trust exceptions. */
public final class StrictRmiTlsSocketFactory extends SocketConfig
        implements java.rmi.server.RMIClientSocketFactory, java.rmi.server.RMIServerSocketFactory, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int HANDSHAKE_TIMEOUT = 10_000;
    private static volatile SSLContext dedicatedContext;

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
        final var keyStore = System.getProperty("ecpds.rmi.tls.keyStore", "");
        final var trustStore = System.getProperty("ecpds.rmi.tls.trustStore", "");
        final var passwordFile = System.getProperty("ecpds.rmi.tls.passwordFile", "");
        if (!keyStore.isEmpty() || !trustStore.isEmpty() || !passwordFile.isEmpty()) {
            if (keyStore.isEmpty() || trustStore.isEmpty() || passwordFile.isEmpty()) {
                throw new IOException("Incomplete dedicated RMI TLS configuration");
            }
            if (dedicatedContext == null) {
                synchronized (StrictRmiTlsSocketFactory.class) {
                    if (dedicatedContext == null) {
                        dedicatedContext = createContext(keyStore, trustStore, passwordFile);
                    }
                }
            }
            return dedicatedContext;
        }
        try {
            return SSLContext.getDefault();
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException("Unable to initialize RMI TLS", e);
        }
    }

    private static SSLContext createContext(final String keyPath, final String trustPath, final String passwordPath)
            throws IOException {
        final Path keyFile;
        final Path trustFile;
        final Path passwordFile;
        try {
            keyFile = Path.of(keyPath);
            trustFile = Path.of(trustPath);
            passwordFile = Path.of(passwordPath);
        } catch (final InvalidPathException e) {
            throw new IOException("Invalid dedicated RMI TLS path", e);
        }
        if (!keyFile.isAbsolute() || !trustFile.isAbsolute() || !passwordFile.isAbsolute()) {
            throw new IOException("Dedicated RMI TLS paths must be absolute");
        }
        final byte[] passwordBytes;
        try (var input = Files.newInputStream(passwordFile)) {
            passwordBytes = input.readNBytes(4097);
        }
        if (passwordBytes.length == 0 || passwordBytes.length > 4096) {
            Arrays.fill(passwordBytes, (byte) 0);
            throw new IOException("Invalid RMI TLS password file");
        }
        final var password = new String(passwordBytes, StandardCharsets.US_ASCII).stripTrailing().toCharArray();
        Arrays.fill(passwordBytes, (byte) 0);
        try {
            if (password.length == 0) {
                throw new IOException("Invalid RMI TLS password file");
            }
            final var keys = KeyStore.getInstance("PKCS12");
            try (var input = Files.newInputStream(keyFile)) {
                keys.load(input, password);
            }
            final var trust = KeyStore.getInstance("PKCS12");
            try (var input = Files.newInputStream(trustFile)) {
                trust.load(input, password);
            }
            final var keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagers.init(keys, password);
            final var trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagers.init(trust);
            final var context = SSLContext.getInstance("TLS");
            context.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), null);
            return context;
        } catch (final GeneralSecurityException | IllegalArgumentException e) {
            throw new IOException("Unable to initialize dedicated RMI TLS", e);
        } finally {
            Arrays.fill(password, '\0');
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
