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

package ecmwf.common.plugin;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class SimplePlugin.
 */
public abstract class SimplePlugin extends ServerPlugin {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SimplePlugin.class);

    /** The _in. */
    private InputStream _in = null;

    /** The _out. */
    private OutputStream _out = null;

    /** The _loop. */
    private boolean _loop = false;

    /** The _separators. */
    private String _separators = "\n";

    /** The _opts. */
    private final Map<String, String> _opts = new ConcurrentHashMap<>();

    /**
     * Instantiates a new simple plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     * @param socket
     *            the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public SimplePlugin(final String ref, final Map<String, String> params, final Socket socket) throws IOException {
        super(ref, params, socket);
    }

    /**
     * Instantiates a new simple plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     */
    public SimplePlugin(final String ref, final Map<String, String> params) {
        super(ref, params);
    }

    /**
     * Sets the separators.
     *
     * @param separators
     *            the new separators
     */
    protected void setSeparators(final String separators) {
        _separators = separators;
    }

    /**
     * Sets the loop.
     *
     * @param loop
     *            the new loop
     */
    protected void setLoop(final boolean loop) {
        _loop = loop;
    }

    /**
     * Refuse connection.
     *
     * @param socket
     *            the socket
     * @param connectionsCount
     *            the connections count
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void refuseConnection(final Socket socket, final int connectionsCount) throws IOException {
    }

    /**
     * Start connection.
     *
     * @param socket
     *            the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void startConnection(final Socket socket) throws IOException {
        final var start = System.currentTimeMillis();
        try {
            _in = socket.getInputStream();
            _out = socket.getOutputStream();
        } catch (final IOException e) {
            _log.debug("Can't open input/output streams", e);
            return;
        }
        String command = null;
        String message = null;
        do {
            try {
                command = readLine();
            } catch (final IOException e) {
                break;
            }
            _log.debug("Command received is \"" + command + "\"");
            final int ind;
            if ((ind = command.indexOf(' ')) != -1) {
                message = command.substring(ind + 1);
                command = command.substring(0, ind);
            } else {
                message = null;
            }
            try {
                invoke(command.toLowerCase(), message == null || message.trim().length() == 0 ? null
                        : Format.getParameters(message, _separators));
            } catch (final NoSuchMethodException e) {
                _log.error("No such command available");
                break;
            } catch (final Throwable t) {
                _log.error("Breaking on command exception", t instanceof InvocationTargetException
                        ? ((InvocationTargetException) t).getTargetException() : t);
                break;
            }
        } while (_loop);
        _log.debug("Control connection completed in " + Format.formatDuration(start, System.currentTimeMillis()));
    }

    /**
     * Invoke.
     *
     * @param command
     *            the command
     * @param parameters
     *            the parameters
     *
     * @throws Exception
     *             the exception
     */
    public void invoke(final String command, final String[] parameters) throws Exception {
        final var method = this.getClass().getMethod(command + "Req",
                parameters == null ? null : new Class[] { String[].class });
        method.invoke(this, parameters == null ? null : new Object[] { parameters });
    }

    /**
     * Release connection.
     *
     * @param socket
     *            the socket
     * @param close
     *            the close
     */
    @Override
    public void releaseConnection(final Socket socket, final boolean close) {
        if (close) {
            StreamPlugThread.closeQuietly(_in);
            StreamPlugThread.closeQuietly(_out);
        } else {
            _log.debug("Close handled by plugin");
        }
        super.releaseConnection(socket, close);
    }

    /**
     * Parses the command.
     *
     * @param command
     *            the command
     *
     * @return the string
     */
    public String parseCommand(final String command) {
        return null;
    }

    /**
     * Read line.
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected String readLine() throws IOException {
        try {
            final var buffer = new StringBuilder(128);
            int i;
            while ((i = _in.read()) != -1) {
                final var c = (char) i;
                if ((c == '\n') || (c == '\r')) {
                    break;
                }
                buffer.append(c);
                final String command;
                if ((command = parseCommand(buffer.toString())) != null) {
                    return command;
                }
            }
            final var result = buffer.toString();
            if (i == -1 && result.length() == 0) {
                throw new IOException("Connection closed");
            }
            return result.trim();
        } catch (final InterruptedIOException e) {
            _log.error("Read timeout");
            throw e;
        } catch (final IOException e) {
            _log.error("Read error", e);
            throw e;
        }
    }

    /**
     * Opts req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void optsReq(final String[] parameters) throws ParameterException {
        final var opts = getParameter(parameters);
        final var token = new StringTokenizer(opts, ";");
        while (token.hasMoreElements()) {
            final var parameter = token.nextToken().trim();
            final var index = parameter.indexOf("=");
            if (index > 0) {
                _opts.put(parameter.substring(0, index), parameter.substring(index + 1));
            } else {
                _opts.put(parameter, "yes");
            }
        }
    }

    /**
     * Gets the opts.
     *
     * @param name
     *            the name
     *
     * @return the opts
     */
    public String getOpts(final String name) {
        return _opts.get(name);
    }

    /**
     * Gets the opts.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the opts
     */
    public boolean getOpts(final String name, final boolean defaultValue) {
        final var value = getOpts(name);
        return value == null ? defaultValue : "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    /**
     * Prints the.
     *
     * @param message
     *            the message
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void print(final String message) throws IOException {
        _out.write(message.getBytes());
        _out.flush();
    }

    /**
     * Println.
     *
     * @param message
     *            the message
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void println(final String message) throws IOException {
        print(message + "\n");
    }

    /**
     * Error.
     *
     * @param message
     *            the message
     */
    protected void error(final String message) {
        try {
            println("-" + message);
        } catch (final IOException e) {
            _log.debug("Could not send error message", e);
        }
    }

    /**
     * Sends the.
     *
     * @param message
     *            the message
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void send(final String message) throws IOException {
        println("+" + message);
    }

    /**
     * Wait.
     *
     * @param message
     *            the message
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void wait(final String message) throws IOException {
        println("*" + message);
        final var ack = readLine();
        if (ack == null || !"ACK".equals(ack)) {
            throw new IOException("Not ACK from client");
        }
    }

    /**
     * Sends the.
     *
     * @param message
     *            the message
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void send(final long message) throws IOException {
        send(String.valueOf(message));
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     */
    protected InputStream getInputStream() {
        return _in;
    }

    /**
     * Gets the output stream.
     *
     * @return the output stream
     */
    protected OutputStream getOutputStream() {
        return _out;
    }

    /**
     * Gets the parameter.
     *
     * @param parameters
     *            the parameters
     *
     * @return the parameter
     *
     * @throws ParameterException
     *             the parameter exception
     */
    protected static String getParameter(final String[] parameters) throws ParameterException {
        if (parameters.length != 1) {
            throw new ParameterException("Bad request");
        }
        return parameters[0];
    }

    /**
     * Gets the parameter.
     *
     * @param param
     *            the param
     *
     * @return the parameter
     */
    protected static String[] getParameter(final String param) {
        return new String[] { param };
    }

    /**
     * The Class ParameterException.
     */
    public static final class ParameterException extends Exception {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -3984330301081669466L;

        /**
         * Instantiates a new parameter exception.
         *
         * @param message
         *            the message
         */
        public ParameterException(final String message) {
            super(message);
        }
    }
}
