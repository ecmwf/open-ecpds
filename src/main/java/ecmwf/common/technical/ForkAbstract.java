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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class ForkAbstract.
 */
public abstract class ForkAbstract implements Closeable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ForkAbstract.class);

    /** The Constant PERMISSION_DENIED. */
    public static final String PERMISSION_DENIED = "Permission denied";

    /** The Constant NO_SUCH_FILE_OR_DIRECTORY. */
    public static final String NO_SUCH_FILE_OR_DIRECTORY = "No such file or directory";

    /** The Constant DISC_QUOTA_EXCEEDED. */
    public static final String DISC_QUOTA_EXCEEDED = "Disc quota exceeded";

    /** The Constant UNKNOWN_ERROR. */
    public static final String UNKNOWN_ERROR = "Unknown error";

    /** The Constant ECFS_NOT_AVAILABLE. */
    public static final String ECFS_NOT_AVAILABLE = "ECFS not available (sleep)";

    /** The Constant _buffer. */
    private static final int _buffer = 4096;

    /** The _message. */
    private String _message = null;

    /** The _error. */
    private String _error = null;

    /** The _closed. */
    private boolean _closed = false;

    /** The _check. */
    private boolean _check = false;

    /** The _kill on time out. */
    private boolean _killOnTimeOut = true;

    /**
     * Sets the kill on time out.
     *
     * @param killOnTimeOut
     *            the new kill on time out
     */
    public void setKillOnTimeOut(final boolean killOnTimeOut) {
        _killOnTimeOut = killOnTimeOut;
    }

    /**
     * Converts into string.
     *
     * @param args
     *            the args
     *
     * @return the string
     */
    protected String toString(final String[] args) {
        final var result = new StringBuilder();
        int i;
        for (i = 0; i < args.length; i++) {
            if (args[i] != null) {
                result.append(i > 0 ? " " : "").append(args[i]);
            }
        }
        return result.toString();
    }

    /**
     * Check.
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean check() throws IOException {
        return check(null);
    }

    /**
     * Check.
     *
     * @param errors
     *            the errors
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean check(final String[][] errors) throws IOException {
        return check(null, errors);
    }

    /**
     * Check.
     *
     * @param expect
     *            the expect
     * @param errors
     *            the errors
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean check(final String[][] expect, final String[][] errors) throws IOException {
        final var out = new ByteArrayOutputStream();
        _check = true;
        while (!_closed && writeInto(out) != -1) {
            final var data = out.toByteArray();
            _message = data == null ? "" : new String(data);
            if (errors != null) {
                for (final String[] error : errors) {
                    if (_message.indexOf(error[0]) != -1) {
                        _error = error[1] == null ? _message : error[1];
                        kill();
                        return false;
                    }
                }
            }
            if (expect != null) {
                for (final String[] element : expect) {
                    var ok = true;
                    for (String element2 : element) {
                        if (_message.indexOf(element2) == -1) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        return true;
                    }
                }
            }
        }
        if (expect != null) {
            if (expect.length == 1 && expect[0].length == 0 && (_message == null || _message.length() == 0)) {
                return true;
            }
            final var command = getCommand();
            _error = "Unexpected return code from " + _getCommandName(command);
            _log.warn(command + ": " + (isNotEmpty(_message) ? _message : "(no-data)"));
        }
        if (_error != null) {
            kill();
            return false;
        }
        return true;
    }

    /**
     * Close.
     */
    @Override
    public abstract void close();

    /**
     * Gets the command.
     *
     * @return the command
     */
    public abstract String getCommand();

    /**
     * Gets the error.
     *
     * @return the error
     */
    public String getError() {
        return _error;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return _message == null ? "" : _message;
    }

    /**
     * Start.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void start() throws IOException;

    /**
     * Kill.
     *
     * @return true, if successful
     */
    public abstract boolean kill();

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Interrupted.
     *
     * @return the long
     */
    public long interrupted() {
        return 0;
    }

    /**
     * Restarted.
     */
    public void restarted() {
    }

    /**
     * Write into.
     *
     * @param out
     *            the out
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public int writeInto(final OutputStream out) throws IOException {
        var count = -1;
        var read = 0;
        final var data = new byte[_buffer];
        final var in = getInputStream();
        long interrupted;
        restarted();
        while (!_closed && read != -1) {
            try {
                while ((read = in.read(data, 0, _buffer)) != -1) {
                    restarted();
                    out.write(data, 0, read);
                    count = count == -1 ? read : count + read;
                    if (_check) {
                        return count;
                    }
                }
            } catch (final InterruptedIOException e) {
                if ((interrupted = interrupted()) > 0) {
                    final var command = getCommand();
                    _log.error("Command \"" + command + "\" interrupted after " + Format.formatDuration(interrupted)
                            + (_message == null ? "" : ": " + _message) + (_killOnTimeOut ? " (process killed)" : ""));
                    _error = _getCommandName(command) + " interrupted on timeout (" + Format.formatDuration(interrupted)
                            + ")";
                    _closed = true;
                    if (_killOnTimeOut) {
                        kill();
                    }
                }
            }
        }
        if (read == -1) {
            _closed = true;
        }
        return count;
    }

    /**
     * Gets the command name.
     *
     * @param command
     *            the command
     *
     * @return the string
     */
    private static String _getCommandName(String command) {
        if (command != null) {
            var index = command.indexOf(" ");
            if (index != -1) {
                command = command.substring(0, index);
                if ((index = command.lastIndexOf("/")) != -1) {
                    command = command.substring(index);
                }
            }
        }
        return command;
    }
}
