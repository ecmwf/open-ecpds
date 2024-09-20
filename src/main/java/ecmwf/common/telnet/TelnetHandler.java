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

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class TelnetHandler.
 */
public final class TelnetHandler {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TelnetHandler.class);

    /** The Constant NUL. */
    public static final char NUL = 0;

    /** The Constant LF. */
    public static final char LF = 10;

    /** The Constant CR. */
    public static final char CR = 13;

    /** The Constant BEL. */
    public static final char BEL = 7;

    /** The Constant BS. */
    public static final char BS = 8;

    /** The Constant HT. */
    public static final char HT = 9;

    /** The Constant FF. */
    public static final char FF = 12;

    /** The Constant BRK. */
    public static final char BRK = 243; // Break - Indicates that the "break" or
                                        // "attention" key was hi.

    /** The Constant IP. */
    public static final char IP = 244; // Suspend - Interrupt or abort the
                                       // process to which the NVT is
                                       // connected.

    /** The Constant AO. */
    public static final char AO = 245; // Abort output - Allows the current
                                       // process to run to completion but does
                                       // not

    // send its output to the user.
    /** The Constant AYT. */
    public static final char AYT = 246; // Are you there - Send back to the NVT
                                        // some visible evidence that the AYT
                                        // was

    // received.
    /** The Constant EC. */
    public static final char EC = 247; // Erase character - The receiver should
                                       // delete the last preceding undeleted

    // character from the data stream.
    /** The Constant EL. */
    public static final char EL = 248; // Erase line - Delete characters from
                                       // the data stream back to but not
                                       // including

    // the previous CRLF.
    /** The Constant NOP. */
    public static final char NOP = 241; // No operation

    /** The Constant GA. */
    public static final char GA = 249; // Go ahead - Under certain circumstances
                                       // used to tell the other end that it
                                       // can

    // transmit.
    /** The Constant DM. */
    public static final char DM = 242; // Data mark - Indicates the position of
                                       // a Synch event within the data stream.

    // This should always be accompanied by a TCP urgent notification.
    /** The Constant IAC. */
    public static final char IAC = 255; // Interpret as a command

    /** The Constant SE. */
    public static final char SE = 240; // End of subnegotiation parameters

    /** The Constant SB. */
    public static final char SB = 250; // Subnegotiation - Subnegotiation of the
                                       // indicated option follows.

    /** The Constant WILL. */
    public static final char WILL = 251; // Will - Indicates the desire to begin
                                         // performing, or confirmation that
                                         // you are

    // now performing, the indicated option.
    /** The Constant WONT. */
    public static final char WONT = 252; // Won't - Indicates the refusal to
                                         // perform, or continue performing,
                                         // the

    // indicated option.
    /** The Constant DO. */
    public static final char DO = 253; // Do - Indicates the request that the
                                       // other party perform, or confirmation
                                       // that

    // you are expecting the other party to perform, the indicated option.
    /** The Constant DONT. */
    public static final char DONT = 254; // Don't - Indicates the demand that
                                         // the other party stop performing,
                                         // or

    // confirmation that you are no longer expecting the other party to perform,
    // the
    // indicated option.
    /**
     * The Constant SUPPRESS_GO_AHEAD.
     */
    public static final char SUPPRESS_GO_AHEAD = 3; // RFC858

    /** The Constant STATUS. */
    public static final char STATUS = 5; // RFC859

    /** The Constant ECHO. */
    public static final char ECHO = 1; // RFC857

    /** The Constant TIMING_MARK. */
    public static final char TIMING_MARK = 6; // RFC860

    /** The Constant TERMINAL_TYPE. */
    public static final char TERMINAL_TYPE = 24; // RFC1091

    /** The Constant WINDOW_SIZE. */
    public static final char WINDOW_SIZE = 31; // RFC1073

    /** The Constant TERMINAL_SPEED. */
    public static final char TERMINAL_SPEED = 32; // RFC1079

    /** The Constant REMOTE_FLOW_CONTROL. */
    public static final char REMOTE_FLOW_CONTROL = 33; // RFC1372

    /** The Constant LINEMODE. */
    public static final char LINEMODE = 34; // RFC1184

    /** The Constant ENVIRONMENT. */
    public static final char ENVIRONMENT = 36; // RFC1408

    /** The Constant NEW_ENVIRON. */
    public static final char NEW_ENVIRON = 39; // RFC1408

    /** The Constant ECHO_DIGIT_ONLY. */
    public static final String ECHO_DIGIT_ONLY = "0123456789";

    /** The Constant ECHO_LETTER_ONLY. */
    public static final String ECHO_LETTER_ONLY = "abcdefghijklmnopqrstuvwxyz";

    /** The Constant ECHO_LETTER_AND_DIGIT_ONLY. */
    public static final String ECHO_LETTER_AND_DIGIT_ONLY = ECHO_DIGIT_ONLY.concat(ECHO_LETTER_ONLY);

    /** The Constant ECHO_ALL. */
    public static final String ECHO_ALL = null;

    /** The Constant SEND. */
    public static final char SEND = 1;

    /** The socket. */
    private Socket socket;

    /** The out. */
    private final BufferedOutputStream out;

    /** The in. */
    private final BufferedInputStream in;

    /** The kludge. */
    private final boolean kludge = true;

    /** The kludge cr. */
    private boolean kludgeCR = false;

    /** The kludge lf. */
    private boolean kludgeLF = false;

    /** The terminal. */
    private String terminal = "UNKNOWN";

    /** The window size. */
    private Dimension windowSize = null;

    /**
     * Instantiates a new telnet handler.
     *
     * @param socket
     *            the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public TelnetHandler(final Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedInputStream(socket.getInputStream());
        this.out = new BufferedOutputStream(socket.getOutputStream());
    }

    /**
     * Instantiates a new telnet handler.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public TelnetHandler(final InputStream in, final OutputStream out) throws IOException {
        this.in = new BufferedInputStream(in);
        this.out = new BufferedOutputStream(out);
    }

    /**
     * Close.
     */
    public synchronized void close() {
        StreamPlugThread.closeQuietly(socket);
        socket = null;
    }

    /**
     * Next char.
     *
     * @return the char
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public char nextChar() throws IOException {
        while (true) {
            final var c = in.read();
            switch (c) {
            case -1:
                throw new IOException();
            case BRK:
                break;
            case IP:
                // throw new TelnetInterruptException();
                break;
            case AO:
                // throw new TelnetAbortOutputException();
                break;
            case AYT:
                // are you there?
                break;
            case NOP:
                break;
            case GA:
                break;
            case DM:
                break;
            case IAC:
                if (!interpretAsCommand()) {
                    return '\0';
                }
                break;
            case EC:
                break;
            case EL:
                break;
            default:
                return (char) c;
            }
        }
    }

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void flush() throws IOException {
        if (kludge) {
            // Reset end-of-line detection
            kludgeCR = false;
            kludgeLF = false;
        }
        out.flush();
    }

    /**
     * Sends the.
     *
     * @param c
     *            the c
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void send(final char c) throws IOException {
        out.write(c);
        out.flush();
    }

    /**
     * Prints the.
     *
     * @param c
     *            the c
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void print(final char c) throws IOException {
        out.write(c);
        if (kludge) {
            // Kludge mode
            if (c == CR) {
                kludgeCR = true;
            } else if (c == LF) {
                kludgeLF = true;
            }
            if (kludgeCR && kludgeLF) {
                // End of line, so flush
                flush();
            }
        } else {
            // Character-at-a-time mode
            flush();
        }
    }

    /**
     * Println.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void println() throws IOException {
        print(CR);
        print(LF);
    }

    /**
     * Prints the.
     *
     * @param s
     *            the s
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void print(final String s) throws IOException {
        if (s == null) {
            return;
        }
        for (var i = 0; i < s.length(); i++) {
            print(s.charAt(i));
        }
    }

    /**
     * Println.
     *
     * @param s
     *            the s
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void println(final String s) throws IOException {
        print(s);
        println();
    }

    /**
     * Sends the option.
     *
     * @param option
     *            the option
     * @param state
     *            the state
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void sendOption(final char option, final boolean state) throws IOException {
        send(IAC);
        send(state ? WILL : WONT);
        send(option);
    }

    /**
     * Sends the option request.
     *
     * @param option
     *            the option
     * @param state
     *            the state
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void sendOptionRequest(final char option, final boolean state) throws IOException {
        send(IAC);
        send(state ? DO : DONT);
        send(option);
    }

    /**
     * String.
     *
     * @param c
     *            the c
     *
     * @return the string
     */
    private static String string(final int c) {
        return switch (c) {
        case BRK -> "BRK";
        case IP -> "IP";
        case AO -> "AO";
        case AYT -> "AYT";
        case EC -> "EC";
        case EL -> "EL";
        case NOP -> "NOP";
        case GA -> "GA";
        case DM -> "DM";
        case IAC -> "IAC";
        case SB -> "SB";
        case SE -> "SE";
        case WILL -> "WILL";
        case WONT -> "WONT";
        case DO -> "DO";
        case DONT -> "DONT";
        case SUPPRESS_GO_AHEAD -> "SUPPRESS_GO_AHEAD";
        case STATUS -> "STATUS";
        case ECHO -> "ECHO";
        case TIMING_MARK -> "TIMING_MARK";
        case TERMINAL_TYPE -> "TERMINAL_TYPE";
        case WINDOW_SIZE -> "WINDOW_SIZE";
        case TERMINAL_SPEED -> "TERMINAL_SPEED";
        case REMOTE_FLOW_CONTROL -> "REMOTE_FLOW_CONTROL";
        case LINEMODE -> "LINEMODE";
        case ENVIRONMENT -> "ENVIRONMENT";
        case NEW_ENVIRON -> "NEW_ENVIRON";
        default -> String.valueOf(c);
        };
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public InputStream getInputStream() throws IOException {
        return in;
    }

    /**
     * Gets the output stream.
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public OutputStream getOutputStream() throws IOException {
        return out;
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
     * Gets the terminal.
     *
     * @return the terminal
     */
    public String getTerminal() {
        return terminal;
    }

    /**
     * Sets the terminal.
     *
     * @param terminal
     *            the new terminal
     */
    public void setTerminal(final String terminal) {
        this.terminal = terminal;
    }

    /**
     * Gets the window size.
     *
     * @return the window size
     */
    public Dimension getWindowSize() {
        return windowSize;
    }

    /**
     * Sets the window size.
     *
     * @param windowSize
     *            the new window size
     */
    public void setWindowSize(final Dimension windowSize) {
        this.windowSize = windowSize;
    }

    /**
     * Interpret as command.
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private boolean interpretAsCommand() throws IOException {
        final var operation = in.read();
        final var option = operation != SE ? in.read() : -1;
        switch (operation) {
        case SB:
            // Subnegotiaton
            var subnegotiating = true;
            final var request = in.read() == 1;
            if (request) {
                // Send value to client
                _log.debug("Request for: " + string(option) + " (ignored)");
            } else {
                // Get value from client
                var value = "";
                var length = 0;
                while (subnegotiating) {
                    value += nextChar();
                    length++;
                }
                switch (option) {
                case TERMINAL_TYPE:
                    setTerminal(value.substring(0, length > 0 ? length - 1 : length));
                    break;
                default:
                    // Unsupported option
                    _log.debug("Value for: " + string(option) + "=" + value + " (ignored)");
                }
            }
            break;
        case SE:
            return subnegotiating = false;
        case WILL:
            switch (option) {
            case TERMINAL_TYPE:
                subNegotiation(TERMINAL_TYPE, SEND);
                break;
            default:
                _log.debug("Request to send: " + string(option) + " (ignored)");
            }
            break;
        case WONT:
            break;
        case DO:
        case DONT:
            switch (option) {
            case SUPPRESS_GO_AHEAD:
            case ECHO:
                sendOption((char) option, true);
                break;
            default:
                // Unsupported option
                sendOption((char) option, false);
                _log.debug("Refusal to send: " + string(option));
            }
            break;
        }
        return true;
    }

    /**
     * Read line.
     *
     * @param size
     *            the size
     * @param set
     *            the set
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String readLine(final int size, final String set) throws IOException {
        return readLine(-1, size, set);
    }

    /**
     * Read line.
     *
     * @param echo
     *            the echo
     * @param size
     *            the size
     * @param set
     *            the set
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String readLine(final int echo, final int size, final String set) throws IOException {
        final var line = new StringBuilder("");
        var cr = false;
        boolean validType;
        boolean display;
        var cursor = 0;
        char c;
        while (true) {
            display = true;
            c = nextChar();
            validType = set == null || set.indexOf(Character.toLowerCase(c)) != -1
                    || set.indexOf(Character.toUpperCase(c)) != -1;
            switch (c) {
            case 3:
                throw new IOException("CTRL-C");
            case 4:
                throw new IOException("CTRL-D");
            case CR:
                cr = true;
                break;
            case LF:
                break;
            case 0:
                break;
            case 127:
                c = BS;
            case BS:
                if (cursor > 0) {
                    cursor--;
                    line.deleteCharAt(cursor);
                } else {
                    c = 0;
                }
                break;
            default:
                if (validType && (size < 0 || cursor < size)) {
                    line.insert(cursor++, c);
                    if (echo > 0) {
                        c = (char) echo;
                    }
                } else {
                    display = false;
                }
                break;
            }
            if (cr) {
                break;
            }
            if (display && c != LF && c != 0) {
                send(c);
                if (c == BS) {
                    send(' ');
                    send(BS);
                }
            }
        }
        return line.toString();
    }

    /**
     * Read string.
     *
     * @param message
     *            the message
     * @param size
     *            the size
     * @param set
     *            the set
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String readString(final String message, final int size, final String set) throws IOException {
        return readString(message, size, set, null, 0);
    }

    /**
     * Read string.
     *
     * @param message
     *            the message
     * @param size
     *            the size
     * @param set
     *            the set
     * @param values
     *            the values
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String readString(final String message, final int size, final String set, final String[] values)
            throws IOException {
        return readString(message, size, set, values, 0);
    }

    /**
     * Read string.
     *
     * @param message
     *            the message
     * @param size
     *            the size
     * @param set
     *            the set
     * @param values
     *            the values
     * @param defaultPos
     *            the default pos
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("null")
    public String readString(final String message, final int size, final String set, final String[] values,
            final int defaultPos) throws IOException {
        final var hasValues = values != null && values.length > 0;
        if (hasValues) {
            final var list = new StringBuilder();
            for (var i = 0; values.length > 1 && i < values.length; i++) {
                list.append(list.length() == 0 ? " (" : ", ").append(values[i])
                        .append(i + 1 == values.length ? ")" : "");
            }
            var defaultValue = values[defaultPos];
            defaultValue = defaultValue.length() > size ? defaultValue.substring(0, size) : defaultValue;
            print(message + list.toString() + " [" + defaultValue + "]: ");
        } else {
            print(message + ": ");
        }
        flush();
        final var result = readLine(size, set);
        println();
        flush();
        return result.length() == 0 && hasValues ? values[0] : result;
    }

    /**
     * Gets the password.
     *
     * @param size
     *            the size
     * @param set
     *            the set
     * @param message
     *            the message
     * @param help
     *            the help
     *
     * @return the password
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getPassword(final int size, final String set, final String message, final File help)
            throws IOException {
        return getPassword(size, set, message, null, help);
    }

    /**
     * Gets the password.
     *
     * @param size
     *            the size
     * @param set
     *            the set
     * @param message
     *            the message
     * @param verify
     *            the verify
     * @param help
     *            the help
     *
     * @return the password
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getPassword(final int size, final String set, final String message, final String verify,
            final File help) throws IOException {
        String password;
        var retry = false;
        while (true) {
            print((retry ? "Please try again" : message) + ":");
            retry = false;
            flush();
            password = readLine('X', size, set);
            println();
            flush();
            if (password == null || password.length() == 0) {
                if (help.canRead()) {
                    display(help, false);
                }
            } else if (verify != null) {
                print(verify + ":");
                flush();
                final var next = readLine('X', size, set);
                println();
                flush();
                if (next != null && next.equals(password)) {
                    break;
                }
                println("Do not match.");
                retry = true;
            } else {
                break;
            }
        }
        println();
        flush();
        return password;
    }

    /**
     * Read char.
     *
     * @param message
     *            the message
     * @param values
     *            the values
     *
     * @return the char
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public char readChar(final String message, final String[] values) throws IOException {
        return readString(message, 1, values, 0).charAt(0);
    }

    /**
     * Read string.
     *
     * @param message
     *            the message
     * @param size
     *            the size
     * @param values
     *            the values
     * @param defaultValue
     *            the default value
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String readString(final String message, final int size, final String[] values, final int defaultValue)
            throws IOException {
        final var set = new StringBuilder();
        var count = 0;
        for (final String value2 : values) {
            final var value = size < 0 ? value2 : value2.substring(0, size);
            final var length = value.length();
            if (length > 0) {
                set.append(value);
                if (length > count) {
                    count = length;
                }
            }
        }
        return readString(message, size < 0 ? count : size, set.toString(), values, defaultValue);
    }

    /**
     * Sub negotiation.
     *
     * @param option
     *            the option
     * @param param
     *            the param
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void subNegotiation(final char option, final char param) throws IOException {
        subNegotiation(option, new char[] { param });
    }

    /**
     * Sub negotiation.
     *
     * @param option
     *            the option
     * @param params
     *            the params
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void subNegotiation(final char option, final char[] params) throws IOException {
        send(IAC);
        send(SB);
        send(option);
        for (final char param : params) {
            send(param);
        }
        send(IAC);
        send(SE);
    }

    /**
     * Display.
     *
     * @param file
     *            the file
     * @param wait
     *            the wait
     */
    public void display(final File file, final boolean wait) {
        if (file.canRead()) {
            try {
                final var fis = new FileInputStream(file);
                final var dis = new BufferedReader(new InputStreamReader(fis));
                String line;
                try {
                    while ((line = dis.readLine()) != null) {
                        println(line);
                    }
                } catch (final Exception e1) {
                    _log.warn("Not displayed", e1);
                } finally {
                    dis.close();
                }
                if (wait) {
                    readLine(0, TelnetHandler.ECHO_ALL);
                }
            } catch (final Exception e2) {
                _log.warn("Not displayed", e2);
            }
        } else {
            _log.warn("Not displayed (file " + file.getAbsolutePath() + " not found/readable)");
        }
    }
}
