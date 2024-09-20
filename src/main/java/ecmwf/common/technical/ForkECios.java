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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.text.Format;

/**
 * The Class ForkECios.
 */
public class ForkECios extends ForkAbstract {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ForkECios.class);

    /** The Constant _socketConfig. */
    private static final SocketConfig _socketConfig = new SocketConfig("ECios");

    /** The _args. */
    private final String[] _args;

    /** The _uid. */
    private final String _uid;

    /** The _dir. */
    private final String _dir;

    /** The _synchro on kill. */
    private final Object _synchroOnKill = new Object();

    /** The _start time. */
    private long _startTime = System.currentTimeMillis();

    /** The _command. */
    private String _command = null;

    /** The _socket. */
    private Socket _socket = null;

    /** The _so time out. */
    private int _soTimeOut = -1;

    /** The _time out. */
    private int _timeOut = -1;

    /** The _pid. */
    private long _pid = -1;

    /** The _interrupted. */
    private long _interrupted = -1;

    /**
     * Instantiates a new fork e cios.
     *
     * @param args
     *            the args
     */
    public ForkECios(final String[] args) {
        _args = args;
        _uid = "root";
        _dir = "";
    }

    /**
     * Instantiates a new fork e cios.
     *
     * @param uid
     *            the uid
     * @param args
     *            the args
     */
    public ForkECios(final String uid, final String[] args) {
        _args = args;
        _uid = uid;
        _dir = "";
    }

    /**
     * Instantiates a new fork e cios.
     *
     * @param uid
     *            the uid
     * @param dir
     *            the dir
     * @param args
     *            the args
     */
    public ForkECios(final String uid, final String dir, final String[] args) {
        _args = args;
        _uid = uid;
        _dir = dir;
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        StreamPlugThread.closeQuietly(_socket);
    }

    /**
     * Gets the socket config.
     *
     * @return the socket config
     */
    public static SocketConfig getSocketConfig() {
        return _socketConfig;
    }

    /**
     * Start.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void start() throws IOException {
        start(0);
    }

    /**
     * Start.
     *
     * @param timeOut
     *            the time out
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void start(final int timeOut) throws IOException {
        _startTime = System.currentTimeMillis();
        _command = toString(_args);
        final var request = _command + "|-c|" + Cnf.at("Command", "sh") + "|" + _dir + "|" + _uid;
        _log.debug(_uid + " start \"" + _command + "\"");
        IOException exception = null;
        var succesfful = false;
        var commandWriten = false;
        try {
            for (var i = 0; i < 5; i++) {
                try {
                    commandWriten = false;
                    _socket = _socketConfig.getSocket();
                    _socket.setSoTimeout(Cnf.at("ECios", "startSoTimeout", 15000));
                    final var in = _socket.getInputStream();
                    final var out = _socket.getOutputStream();
                    out.write(("E0000000000" + Format.formatLong(request.length(), 4, true) + request).getBytes());
                    out.flush();
                    commandWriten = true;
                    if ((char) in.read() != '1') {
                        close();
                        exception = new IOException("Exec request not allowed");
                        break;
                    }
                    final var buf = new byte[10];
                    if (StreamPlugThread.readFully(in, buf, 0, 10) != 10) {
                        throw new IOException("Unexpected pid");
                    }
                    _pid = Long.parseLong(new String(buf));
                    _soTimeOut = timeOut > 0 && timeOut < 1000 ? timeOut : 1000;
                    _timeOut = timeOut < 0 ? 0 : timeOut;
                    _socket.setSoTimeout(_soTimeOut);
                    break;
                } catch (final SocketTimeoutException e) {
                    close();
                    exception = e;
                } catch (final IOException e) {
                    close();
                    if (!commandWriten && i < 5) {
                        _log.debug("Retry connection to ecios (" + i + ")", e);
                        try {
                            Thread.sleep(Timer.ONE_SECOND);
                        } catch (final InterruptedException ignored) {
                        }
                    } else {
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                _log.debug("Connection refused to ecios: " + _socketConfig.getHost() + ":" + _socketConfig.getPort(),
                        exception);
                throw exception;
            }
            succesfful = true;
        } finally {
            _log.debug(_uid + " exec[pid=" + _pid + "] " + (succesfful ? "" : "NOT ") + "started \"" + _command
                    + "\" (duration:" + Format.formatDuration(_startTime, System.currentTimeMillis()) + ",timeout:"
                    + Format.formatDuration(_timeOut) + ")");
        }
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
    @Override
    public boolean check(final String[][] expect, final String[][] errors) throws IOException {
        final var succesfful = super.check(expect, errors);
        _log.debug(_uid + " exec[pid=" + _pid + "] " + (succesfful ? "" : "NOT ") + "completed \"" + _command
                + "\" (duration:" + Format.formatDuration(_startTime, System.currentTimeMillis()) + ",timeout:"
                + Format.formatDuration(_timeOut) + ")");
        return succesfful;
    }

    /**
     * Kill.
     *
     * @param pid
     *            the pid
     *
     * @return true, if successful
     */
    public static boolean kill(final long pid) {
        if (pid <= 0) {
            _log.warn("Invalid pid: " + pid);
            return false;
        }
        Socket socket = null;
        try {
            socket = _socketConfig.getSocket();
            socket.setSoTimeout(Cnf.at("ECios", "killSoTimeout", 15000));
            final var request = String.valueOf(pid);
            socket.getOutputStream()
                    .write(("K0000000000" + Format.formatLong(request.length(), 4, true) + request).getBytes());
            socket.getOutputStream().flush();
            if ((char) socket.getInputStream().read() != '0') {
                _log.warn("kill request refused (" + pid + ")");
                return false;
            }
            return true;
        } catch (final Throwable t) {
            _log.warn("kill request failed (" + pid + ")", t);
            return false;
        } finally {
            if (socket != null) {
                StreamPlugThread.closeQuietly(socket);
            }
        }
    }

    /**
     * Kill.
     *
     * @return true, if successful
     */
    @Override
    public boolean kill() {
        synchronized (_synchroOnKill) {
            if (_pid <= 0) {
                return false;
            }
            var successful = false;
            try {
                kill(_pid);
                successful = true;
            } catch (final Throwable t) {
            } finally {
                _pid = -1;
                close();
                _log.debug(_uid + " kill[pid=" + _pid + "] " + (successful ? "" : "NOT ") + "successful \"" + _command
                        + "\" (timeout: " + Format.formatDuration(_timeOut) + ")");
            }
            return successful;
        }
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (_socket == null) {
            throw new IOException("Connection not initialized");
        }
        return _socket.getInputStream();
    }

    /**
     * Interrupted.
     *
     * @return the long
     */
    @Override
    public long interrupted() {
        return _timeOut > 0 && (_interrupted += _soTimeOut) > _timeOut ? _interrupted : 0;
    }

    /**
     * Restarted.
     */
    @Override
    public void restarted() {
        _interrupted = 0;
    }

    /**
     * Gets the command.
     *
     * @return the command
     */
    @Override
    public String getCommand() {
        return _args[0];
    }

    /**
     * Gets the command with args.
     *
     * @return the command with args
     */
    public String getCommandWithArgs() {
        return _command;
    }
}
