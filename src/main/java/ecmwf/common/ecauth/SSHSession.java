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

package ecmwf.common.ecauth;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_CIPHER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_COMPRESSION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_CONNECT_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_FINGER_PRINT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_LISTEN_ADDRESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PASSPHRASE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PRIVATE_KEY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PRIVATE_KEY_FILE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_SERVER_ALIVE_COUNT_MAX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_SERVER_ALIVE_INTERVAL;

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.telnet.ScriptHandler;
import ecmwf.common.text.Format;

/**
 * The Class SSHSession.
 */
public final class SSHSession implements InteractiveSession {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SSHSession.class);

    /** The channel. */
    private final Channel channel;

    /** The session. */
    private final Session session;

    /** The in. */
    private final DataInputStream in;

    /** The out. */
    private final OutputStream out;

    /** The hostname. */
    private final String hostname;

    /** The debug. */
    private final boolean debug;

    /**
     * Gets the debug.
     *
     * @return the debug
     */
    @Override
    public boolean getDebug() {
        return debug;
    }

    /**
     * Send.
     *
     * @param cmd
     *            the cmd
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void send(final String cmd) throws IOException {
        try {
            // Let's clear the input stream first
            final var available = in.available();
            if (available > 0) {
                in.readFully(new byte[available]);
            }
            // Now we push the command
            final var bytes = (cmd + "\n").getBytes();
            out.write(bytes);
            out.flush();
            // And we consume the echo!
            in.readFully(bytes);
        } catch (final IOException e) {
            // If this is the end message then we don't need to propagate
            // the exception!
            if (!".".equals(cmd)) {
                final var i = new IOException("Lost connection to " + hostname);
                i.initCause(e);
                throw i;
            }
        }
    }

    /**
     * Waitfor.
     *
     * @param searchElements
     *            the search elements
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String waitfor(final String... searchElements) throws IOException {
        final List<ScriptHandler> handlers = new ArrayList<>();
        for (final String element : searchElements) {
            if (isNotEmpty(element)) {
                final var handler = new ScriptHandler();
                handler.setup(element);
                handlers.add(handler);
            }
        }
        final var ret = new StringBuilder();
        if (!handlers.isEmpty()) {
            final var b = new byte[1];
            var n = 0;
            while (n >= 0) {
                var ok = false;
                try {
                    n = in.read(b);
                    ok = true;
                } finally {
                    if (!ok && _log.isWarnEnabled()) {
                        _log.warn("Waitfor buffer: {} (search={})", ret, Arrays.toString(searchElements));
                    }
                }
                if (n > 0) {
                    ret.append(new String(b, 0, n));
                    for (final ScriptHandler handler : handlers) {
                        if (handler.match(b, n)) {
                            return ret.toString().trim();
                        }
                    }
                }
            }
        }
        if (debug && _log.isDebugEnabled()) {
            _log.debug("Pattern not found: {} (search={})", ret, Arrays.toString(searchElements));
        }
        return null;
    }

    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    /**
     * Disconnect.
     */
    @Override
    public void disconnect() {
        _log.debug("Disconnecting session (and channels)");
        try {
            session.disconnect();
        } finally {
            StreamPlugThread.closeQuietly(out);
            StreamPlugThread.closeQuietly(in);
        }
    }

    /**
     * Instantiates a new SSH session.
     *
     * @param setup
     *            the setup
     * @param hostname
     *            the hostname
     * @param port
     *            the port
     * @param sessionTimeOut
     *            the session time out
     * @param login
     *            the login
     * @param password
     *            the password
     * @param token
     *            the token
     * @param debug
     *            the debug
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JSchException
     *             the jsch exception
     */
    public SSHSession(final ECtransSetup setup, final String hostname, final int port, final int sessionTimeOut,
            final String login, final String password, final byte[] token, final boolean debug)
            throws IOException, JSchException {
        this.debug = debug;
        this.hostname = hostname;
        var connected = false;
        try {
            final var jsch = new JSch();
            final var privateKeyFile = setup.getString(HOST_ECAUTH_PRIVATE_KEY_FILE);
            final var privateKey = setup.getString(HOST_ECAUTH_PRIVATE_KEY).trim();
            if (isNotEmpty(privateKeyFile)) {
                final var file = new File(privateKeyFile);
                final var fileName = file.getCanonicalPath();
                if (!file.canRead()) {
                    throw new IOException("Can not find/read private key: " + fileName);
                }
                _log.debug("Loading certificate from file");
                jsch.addIdentity(fileName);
            } else if (isNotEmpty(privateKey)) {
                _log.debug("Using private key");
                jsch.addIdentity(setup.getModuleName(), privateKey.getBytes(), null, null);
            }
            final var fingerPrint = setup.getString(HOST_ECAUTH_FINGER_PRINT);
            final var passPhrase = setup.getString(HOST_ECAUTH_PASSPHRASE);
            final var listenAddress = setup.getString(HOST_ECAUTH_LISTEN_ADDRESS);
            final var connectTimeOut = (int) setup.getDuration(HOST_ECAUTH_CONNECT_TIME_OUT).toMillis();
            session = jsch.getSession(login, hostname, port);
            final var config = new Hashtable<String, String>();
            // e.g. "zlib,none"
            final var cipher = setup.getString(HOST_ECAUTH_CIPHER);
            final var compression = setup.getString(HOST_ECAUTH_COMPRESSION);
            config.put("compression.s2c", compression);
            config.put("compression.c2s", compression);
            _log.debug("Using compression: {}", compression);
            if (!"none".equals(cipher)) {
                // e.g. "aes128-cbc,3des-cbc,blowfish-cbc"
                config.put("cipher.s2c", cipher);
                config.put("cipher.c2s", cipher);
                _log.debug("Using cipher: {}", cipher);
            }
            final var serverAliveInterval = (int) setup.getOptionalDuration(HOST_ECAUTH_SERVER_ALIVE_INTERVAL)
                    .orElse(Duration.ZERO).toMillis();
            final int serverAliveCountMax = setup.getOptionalInteger(HOST_ECAUTH_SERVER_ALIVE_COUNT_MAX).orElse(0);
            if (_log.isDebugEnabled()) {
                _log.debug("SessionTimeOut: {}", Format.formatDuration(sessionTimeOut));
                _log.debug("ConnectTimeOut: {}", Format.formatDuration(connectTimeOut));
                _log.debug("ServerAliveInterval: {}", Format.formatDuration(serverAliveInterval));
                _log.debug("ServerAliveCountMax: {}", serverAliveCountMax);
            }
            session.setConfig(config);
            session.setUserInfo(new JSftpUserInfo(password, passPhrase, fingerPrint));
            final var socketFactory = new JschSocketFactory(listenAddress, connectTimeOut, sessionTimeOut);
            session.setSocketFactory(socketFactory);
            if (sessionTimeOut > 0) {
                session.setTimeout(sessionTimeOut);
            }
            if (serverAliveCountMax > 0) {
                session.setServerAliveCountMax(serverAliveCountMax);
            }
            if (serverAliveInterval > 0) {
                session.setServerAliveInterval(serverAliveInterval);
            }
            session.connect(connectTimeOut);
            final var socket = socketFactory.getSocket();
            _log.debug("RemoteServer: {} (from: {}:{}, to: {}:{})", session.getServerVersion(),
                    socket.getLocalAddress().getHostAddress(), socket.getLocalPort(), hostname, port);
            channel = session.openChannel("shell");
            final var pip = new PipedInputStream();
            channel.setInputStream(pip);
            out = new PipedOutputStream(pip);
            final var pop = new PipedOutputStream();
            channel.setOutputStream(pop);
            in = new DataInputStream(new PipedInputStream(pop));
            channel.connect(connectTimeOut);
            _log.debug("Connected(shell): {}", channel.isConnected());
            login(token);
            connected = true;
        } finally {
            if (!connected) {
                disconnect();
            }
        }
    }

    /**
     * Login.
     *
     * @param token
     *            the token
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void login(final byte[] token) throws IOException {
        String res = null;
        try {
            res = waitfor("token:");
            if (res == null || res.indexOf("token:") == -1) {
                throw new IOException("No ecauth prompt with " + hostname + " (token)");
            }
            send(new String(token));
            res = waitfor("env:", "Refused");
            if (res == null || res.indexOf("env:") == -1) {
                if (debug) {
                    _log.debug("Res: {}", res);
                }
                throw new IOException("Not authenticated with " + hostname + " (token)");
            }
            final var env = new HashMap<String, String>();
            env.put("ECAUTH_SERVICE", "data");
            for (final Map.Entry<String, String> entry : env.entrySet()) {
                send(entry.getKey() + "=" + entry.getValue());
                waitfor("env:");
            }
            send(".");
            res = waitfor("Accepted");
            if (res == null || res.indexOf("Accepted") == -1) {
                throw new IOException("Not authenticated with " + hostname);
            }
        } catch (final IOException ioe) {
            disconnect();
            _log.warn("Not connected with {}", hostname, ioe);
            throw ioe;
        } catch (final Throwable t) {
            disconnect();
            _log.warn("Not connected with {}", hostname, t);
            throw new IOException("Not connected with " + hostname);
        }
    }

    /**
     * The Class JSftpUserInfo.
     */
    private static final class JSftpUserInfo implements UserInfo, UIKeyboardInteractive {
        /** The passwd. */
        private final String passwd;

        /** The pass phrase. */
        private final String passPhrase;

        /** The fingerprint. */
        private final String fingerprint;

        /**
         * Instantiates a new jsftp user info.
         *
         * @param passwd
         *            the passwd
         * @param passPhrase
         *            the pass phrase
         * @param fingerprint
         *            the fingerprint
         */
        private JSftpUserInfo(final String passwd, final String passPhrase, final String fingerprint) {
            this.fingerprint = fingerprint;
            this.passPhrase = passPhrase;
            this.passwd = passwd;
        }

        /**
         * Prompt keyboard interactive.
         *
         * @param destination
         *            the destination
         * @param name
         *            the name
         * @param instruction
         *            the instruction
         * @param prompt
         *            the prompt
         * @param echo
         *            the echo
         *
         * @return the string[]
         */
        @Override
        public String[] promptKeyboardInteractive(final String destination, final String name, final String instruction,
                final String[] prompt, final boolean[] echo) {
            if (prompt == null) {
                return new String[0];
            }
            final var response = new String[prompt.length];
            for (var i = 0; i < prompt.length; i++) {
                _log.debug("Keyboard interactive prompt: {}", prompt[i]);
                response[i] = passwd;
            }
            return response;
        }

        /**
         * Gets the password.
         *
         * @return the password
         */
        @Override
        public String getPassword() {
            return passwd;
        }

        /**
         * Prompt yes no.
         *
         * @param str
         *            the str
         *
         * @return true, if successful
         */
        @Override
        public boolean promptYesNo(final String str) {
            final var result = fingerprint == null || fingerprint.trim().length() == 0 || str == null
                    || str.indexOf("fingerprint is " + fingerprint + ".") >= 0;
            _log.debug("PromptYesNo: {} ({})", str, result ? "yes" : "no");
            return result;
        }

        /**
         * Gets the passphrase.
         *
         * @return the passphrase
         */
        @Override
        public String getPassphrase() {
            return passPhrase;
        }

        /**
         * Prompt passphrase.
         *
         * @param message
         *            the message
         *
         * @return true, if successful
         */
        @Override
        public boolean promptPassphrase(final String message) {
            _log.debug("PromptPassphrase: {}", message);
            return passPhrase != null;
        }

        /**
         * Prompt password.
         *
         * @param message
         *            the message
         *
         * @return true, if successful
         */
        @Override
        public boolean promptPassword(final String message) {
            _log.debug("PromptPassword: {}", message);
            return passwd != null;
        }

        /**
         * Show message.
         *
         * @param message
         *            the message
         */
        @Override
        public void showMessage(final String message) {
            _log.debug("ShowMessage: {}", message);
        }
    }

    /**
     * A factory for creating JschSocket objects.
     */
    private static final class JschSocketFactory implements SocketFactory {
        /** The listen address. */
        private final String listenAddress;

        /** The connect time out. */
        private final int connectTimeOut;

        /** The so time out. */
        private final int soTimeOut;

        /** The socket. */
        private Socket socket;

        /**
         * Instantiates a new jsch socket factory.
         *
         * @param listenAddress
         *            the listen address
         * @param connectTimeOut
         *            the connect time out
         * @param soTimeOut
         *            the so time out
         */
        private JschSocketFactory(final String listenAddress, final int connectTimeOut, final int soTimeOut) {
            this.listenAddress = listenAddress;
            this.connectTimeOut = connectTimeOut;
            this.soTimeOut = soTimeOut;
        }

        /**
         * Creates a new JschSocket object.
         *
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
        @Override
        public Socket createSocket(final String host, final int port) throws IOException {
            final var socketConfig = new SocketConfig("JSftp");
            if (listenAddress != null) {
                socketConfig.setListenAddress(listenAddress);
            }
            if (soTimeOut > 0) {
                socketConfig.setSoTimeOut(soTimeOut);
            }
            if (connectTimeOut > 0) {
                socketConfig.setConnectTimeOut(connectTimeOut);
            }
            socket = socketConfig.getSocket(host, port);
            return socket;
        }

        /**
         * Gets the socket.
         *
         * @return the socket
         */
        public Socket getSocket() {
            return socket;
        }

        /**
         * Gets the input stream.
         *
         * @param socket
         *            the socket
         *
         * @return the input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public InputStream getInputStream(final Socket socket) throws IOException {
            return socket.getInputStream();
        }

        /**
         * Gets the output stream.
         *
         * @param socket
         *            the socket
         *
         * @return the output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public OutputStream getOutputStream(final Socket socket) throws IOException {
            return socket.getOutputStream();
        }
    }
}
