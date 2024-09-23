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

package ecmwf.common.telnet;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Imported/modified from external API: "TelnetProtocolHandler.java,v 2.14
 * 2001/10/07 20:17:43 marcus Exp $";
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class TelnetWrapper.
 */
public final class TelnetWrapper extends TelnetClientHandler {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TelnetWrapper.class);

    /** The in. */
    protected InputStream in = null;

    /** The out. */
    protected OutputStream out = null;

    /** The socket. */
    protected Socket socket = null;

    /** The host. */
    protected String host = null;

    /** The port. */
    protected int port = 23;

    /** The prompt. */
    private String prompt = null;

    /** The record ios. */
    private boolean recordIos = false;

    /** The read ios. */
    private boolean readIos = false;

    /** The ios. */
    private final ByteArrayOutputStream ios = new ByteArrayOutputStream();

    /** The terminal. */
    protected String terminal = null;

    /** The window size. */
    protected TelnetDimension windowSize = new TelnetDimension(80, 24);

    /**
     * Instantiates a new telnet wrapper.
     */
    public TelnetWrapper() {
    }

    /**
     * Record ios.
     *
     * @param recordIos
     *            the record ios
     */
    public void recordIos(final boolean recordIos) {
        this.recordIos = recordIos;
    }

    /**
     * Gets the ios log.
     *
     * @return the ios log
     */
    public String getIosLog() {
        return ios.toString();
    }

    /**
     * Write ios.
     *
     * @param read
     *            the read
     * @param b
     *            the b
     * @param length
     *            the length
     */
    private void writeIos(final boolean read, final byte[] b, final int length) {
        if (recordIos && length > 0) {
            synchronized (ios) {
                try {
                    if (read != readIos || ios.size() == 0) {
                        ios.write((read ? "\nRead<=" : "\nWrite=>").getBytes());
                    }
                    readIos = read;
                    ios.write(b, 0, length);
                    ios.flush();
                } catch (final IOException e) {
                }
            }
        }
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
        final var socketConfig = new SocketConfig("TelnetWrapper");
        connect(socketConfig.getSocket(host, port));
    }

    /**
     * Connect.
     *
     * @param socket
     *            the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void connect(final Socket socket) throws IOException {
        if (socket.isClosed() || !socket.isConnected() || !socket.isBound()) {
            throw new IOException("Socket not connected");
        }
        this.socket = socket;
        connect(socket.getInputStream(), socket.getOutputStream());
    }

    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Connect.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void connect(final InputStream in, final OutputStream out) throws IOException {
        this.in = in;
        this.out = out;
    }

    /**
     * Disconnect.
     */
    public synchronized void disconnect() {
        if (socket != null) {
            _log.debug("Closing socket connection");
            StreamPlugThread.closeQuietly(socket);
            socket = null;
        } else {
            _log.debug("Closing in/out streams");
            StreamPlugThread.closeQuietly(in);
            StreamPlugThread.closeQuietly(out);
            in = null;
            out = null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Notify end of record.
     */
    @Override
    public void notifyEndOfRecord() {
    }

    /**
     * Login.
     *
     * @param user
     *            the user
     * @param pwd
     *            the pwd
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void login(final String user, final String pwd) throws IOException {
        waitfor("login:"); // throw output away
        send(user);
        waitfor("Password:"); // throw output away
        send(pwd);
    }

    /**
     * Sets the prompt.
     *
     * @param prompt
     *            the new prompt
     */
    public void setPrompt(final String prompt) {
        this.prompt = prompt;
    }

    /**
     * Sends the.
     *
     * @param cmd
     *            the cmd
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String send(final String cmd) throws IOException {
        write((cmd + "\n").getBytes());
        if (isNotEmpty(prompt)) {
            return waitfor(prompt);
        }
        return null;
    }

    /**
     * Waitfor.
     *
     * @param searchElements
     *            the search elements
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String waitfor(final String... searchElements) throws IOException {
        final List<ScriptHandler> list = new ArrayList<>();
        for (final String element : searchElements) {
            if (isNotEmpty(element)) {
                final var handler = new ScriptHandler();
                handler.setup(element);
                list.add(handler);
            }
        }
        if (list.size() > 0) {
            final var b = new byte[1024];
            var n = 0;
            final var ret = new StringBuilder();
            while (n >= 0) {
                var ok = false;
                try {
                    n = read(b);
                    ok = true;
                } finally {
                    if (!ok) {
                        _log.debug("Waitfor buffer: " + ret.toString());
                    }
                }
                if (n > 0) {
                    ret.append(new String(b, 0, n));
                    for (final ScriptHandler handler : list) {
                        if (handler.match(b, n)) {
                            return ret.toString();
                        }
                    }
                }
            }
        }
        _log.debug("Search pattern not found");
        return null;
    }

    /**
     * Read.
     *
     * @param b
     *            the b
     *
     * @return the int
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public int read(final byte[] b) throws IOException {
        var n = 0;
        try {
            n = negotiate(b);
            if (n > 0) {
                return n;
            }
            while (n <= 0) {
                do {
                    n = negotiate(b);
                    if (n > 0) {
                        return n;
                    }
                } while (n == 0);
                n = in.read(b);
                if (n < 0) {
                    return n;
                }
                inputfeed(b, n);
                n = negotiate(b);
            }
            return n;
        } finally {
            writeIos(true, b, n);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        try {
            out.write(b);
            out.flush();
        } finally {
            writeIos(false, b, b.length);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the terminal type.
     */
    @Override
    public String getTerminalType() {
        return terminal;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the window size.
     */
    @Override
    public TelnetDimension getWindowSize() {
        return windowSize;
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
     * {@inheritDoc}
     *
     * Sets the local echo.
     */
    @Override
    public void setLocalEcho(final boolean echo) {
    }

    /**
     * Sets the terminal type.
     *
     * @param terminal
     *            the new terminal type
     */
    public void setTerminalType(final String terminal) {
        this.terminal = terminal;
    }

    /**
     * Sets the window size.
     *
     * @param X
     *            the x
     * @param Y
     *            the y
     */
    public void setWindowSize(final int X, final int Y) {
        this.windowSize = new TelnetDimension(X, Y);
    }
}
