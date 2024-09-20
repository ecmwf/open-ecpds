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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;

/**
 * The Class StreamPlugThread.
 */
public final class StreamPlugThread extends ConfigurableRunnable implements Closeable {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(StreamPlugThread.class);

    /** The Constant DEFAULT_BUFF_SIZE. */
    public static final int DEFAULT_BUFF_SIZE = Cnf.at("StreamPlug", "buffSize", 65536);

    /** The _id. */
    private static int _id = 0;

    /** The Constant _idSynchro. */
    private static final Object _idSynchro = new Object();

    /** The _to close. */
    private final List<Closeable> _toClose = Collections.synchronizedList(new ArrayList<>());

    /** The _buff size. */
    private int _buffSize = -1;

    /** The _in. */
    private final InputStream _in;

    /** The _out. */
    private final OutputStream _out;

    /** The _closed. */
    private boolean _closed = false;

    /** The _count. */
    private int _count = 0;

    /** The _so timeout. */
    private int _soTimeout = -1;

    /** The _timeout. */
    private int _timeout = -1;

    /** The _message. */
    private String _message = null;

    /** The _flush. */
    private boolean _flush = false;

    /** The _read fully. */
    private boolean _readFully = false;

    /**
     * Instantiates a new stream plug thread.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     */
    public StreamPlugThread(final InputStream in, final OutputStream out) {
        _buffSize = DEFAULT_BUFF_SIZE;
        _timeout = 0;
        _soTimeout = 0;
        _in = in;
        _out = out;
    }

    /**
     * Instantiates a new stream plug thread.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     * @param timeout
     *            the timeout
     * @param soTimeout
     *            the so timeout
     */
    private StreamPlugThread(final InputStream in, final OutputStream out, final int timeout, final int soTimeout) {
        _buffSize = DEFAULT_BUFF_SIZE;
        _soTimeout = soTimeout;
        _timeout = timeout;
        _in = in;
        _out = out;
    }

    /**
     * _update.
     *
     * @param value
     *            the value
     *
     * @return the int
     */
    private static int _update(final int value) {
        synchronized (_idSynchro) {
            _id += value;
            return _id;
        }
    }

    /**
     * Sets the buff size.
     *
     * @param buffSize
     *            the new buff size
     */
    public void setBuffSize(final int buffSize) {
        _buffSize = buffSize;
    }

    /**
     * Gets the buff size.
     *
     * @return the buff size
     */
    public int getBuffSize() {
        return _buffSize;
    }

    /**
     * Sets the flush.
     *
     * @param flush
     *            the new flush
     */
    public void setFlush(final boolean flush) {
        _flush = flush;
    }

    /**
     * Gets the flush.
     *
     * @return the flush
     */
    public boolean getFlush() {
        return _flush;
    }

    /**
     * Sets the read fully.
     *
     * @param readFully
     *            the new read fully
     */
    public void setReadFully(final boolean readFully) {
        _readFully = readFully;
    }

    /**
     * Gets the read fully.
     *
     * @return the read fully
     */
    public boolean getReadFully() {
        return _readFully;
    }

    /**
     * Alive.
     *
     * @return true, if successful
     */
    public boolean alive() {
        if (_closed) {
            return false;
        }
        try {
            _out.flush();
            return _count >= 0 && _in.available() >= 0;
        } catch (final Throwable t) {
            return false;
        }
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        _closed = true;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Plug together.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     * @param toClose
     *            the to close
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void plugTogether(final Socket source, final Socket target, final Closeable... toClose)
            throws IOException {
        final var sourceTimeout = source.getSoTimeout();
        final var targetTimeout = target.getSoTimeout();
        final var timeout = sourceTimeout == 0 ? targetTimeout
                : targetTimeout == 0 ? sourceTimeout : sourceTimeout < targetTimeout ? sourceTimeout : targetTimeout;
        final var soTimeout = timeout > 0 && timeout < 1000 ? timeout : 1000;
        source.setSoTimeout(source instanceof SSLSocket ? 0 : soTimeout);
        target.setSoTimeout(target instanceof SSLSocket ? 0 : soTimeout);
        _log.debug("Plugging {}:{} to {}:{} for {}", source.getLocalAddress().getHostName(), source.getPort(),
                target.getInetAddress().getHostName(), target.getPort(), source.getInetAddress().getHostName());
        final var plugOut = new StreamPlugThread(source.getInputStream(), target.getOutputStream(), timeout,
                source.getSoTimeout());
        final var plugIn = new StreamPlugThread(target.getInputStream(), source.getOutputStream(), timeout,
                target.getSoTimeout());
        if (toClose != null) {
            plugOut.toClose(toClose);
        }
        plugOut.toClose(plugIn, source, target);
        plugIn.toClose(plugOut);
        plugOut.setPriority(Thread.MAX_PRIORITY);
        plugIn.setPriority(Thread.MAX_PRIORITY);
        plugOut.execute();
        plugIn.execute();
    }

    /**
     * Plug together.
     *
     * @param source
     *            the source
     * @param in
     *            the in
     * @param out
     *            the out
     * @param toClose
     *            the to close
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void plugTogether(final Socket source, final InputStream in, final OutputStream out,
            final Closeable toClose) throws IOException {
        final var sourceTimeout = source.getSoTimeout();
        final var timeout = sourceTimeout == 0 ? 0 : sourceTimeout;
        final var soTimeout = timeout > 0 && timeout < 1000 ? timeout : 1000;
        source.setSoTimeout(source instanceof SSLSocket ? 0 : soTimeout);
        _log.debug("Plugging {}:{} to in/out streams for {}", source.getLocalAddress().getHostName(), source.getPort(),
                source.getInetAddress().getHostName());
        final var plugOut = new StreamPlugThread(source.getInputStream(), out, timeout, source.getSoTimeout());
        final var plugIn = new StreamPlugThread(in, source.getOutputStream(), timeout, 0);
        if (toClose != null) {
            plugOut.toClose(toClose);
        }
        plugOut.toClose(plugIn, source, in, out);
        plugIn.toClose(plugOut);
        plugOut.setPriority(Thread.MAX_PRIORITY);
        plugIn.setPriority(Thread.MAX_PRIORITY);
        plugOut.execute();
        plugIn.execute();
    }

    /**
     * Read fully.
     *
     * @param in
     *            the in
     * @param b
     *            the b
     * @param off
     *            the off
     * @param len
     *            the len
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static int readFully(final InputStream in, final byte[] b, final int off, final int len) throws IOException {
        var read = 0;
        while (read < len) {
            final var count = in.read(b, off + read, len - read);
            if (count < 0) {
                break;
            }
            read += count;
        }
        return read;
    }

    /**
     * Copy.
     *
     * @param out
     *            the out
     * @param in
     *            the in
     * @param bufferSize
     *            the buffer size
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long copy(final OutputStream out, final InputStream in, final int bufferSize) throws IOException {
        return copy(out, in, bufferSize, -1);
    }

    /**
     * Copy.
     *
     * @param out
     *            the out
     * @param in
     *            the in
     * @param bufferSize
     *            the buffer size
     * @param size
     *            the size
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long copy(final OutputStream out, final InputStream in, final int bufferSize, final long size)
            throws IOException {
        final var buf = new byte[bufferSize];
        var l = 0L;
        int n;
        while ((size == -1 || size > l) && (n = in.read(buf, 0, bufferSize)) > 0) {
            out.write(buf, 0, n);
            l += n;
        }
        return l;
    }

    /**
     * Just consume the input stream, but do nothing with it.
     *
     * @param in
     *            the in
     * @param bufferSize
     *            the buffer size
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long consume(final InputStream in, final int bufferSize) throws IOException {
        final var buf = new byte[bufferSize];
        var l = 0L;
        int n;
        while ((n = in.read(buf, 0, bufferSize)) > 0) {
            l += n;
        }
        return l;
    }

    /**
     * Close quietly.
     *
     * @param process
     *            the process
     */
    public static void closeQuietly(final Process process) {
        if (process != null) {
            closeQuietly(process.getErrorStream());
            closeQuietly(process.getInputStream());
            closeQuietly(process.getOutputStream());
            try {
                process.destroy();
            } catch (final Throwable t) {
                // Ignore
            }
        }
    }

    /**
     * Close quietly.
     *
     * @param scanner
     *            the scanner
     */
    public static void closeQuietly(final Scanner scanner) {
        if (scanner != null) {
            try {
                scanner.close();
            } catch (final Throwable t) {
                // Ignore
            }
        }
    }

    /**
     * Close quietly.
     *
     * @param closeable
     *            the closeable
     */
    public static void closeQuietly(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Throwable t) {
                // Ignore
            }
        }
    }

    /**
     * Close quietly.
     *
     * @param closeable
     *            the closeable
     */
    public static void closeQuietly(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Throwable t) {
                // Ignore
            }
        }
    }

    /**
     * Close quietly.
     *
     * @param socket
     *            the socket
     */
    public static void closeQuietly(final Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (final Throwable t) {
                // Ignore
            }
        }
    }

    /**
     * Close quietly.
     *
     * @param serverSocket
     *            the server socket
     */
    public static void closeQuietly(final ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (final Throwable t) {
                // Ignore
            }
        }
    }

    /**
     * Flush the underlying output stream.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void flush() throws IOException {
        _out.flush();
    }

    /**
     * Configurable run.
     */
    @Override
    public void configurableRun() {
        _log.debug("{} stream(s) plugged (+1)", _update(1));
        try {
            final var buf = new byte[_buffSize];
            var interrupted = 0;
            while (alive()) {
                try {
                    while (!_closed && (_count = _readFully ? readFully(_in, buf, 0, _buffSize)
                            : _in.read(buf, 0, _buffSize)) > 0) {
                        interrupted = 0;
                        _out.write(buf, 0, _count);
                        if (_flush) {
                            _out.flush();
                        }
                    }
                } catch (final InterruptedIOException e) {
                    if (_timeout > 0) {
                        interrupted += _soTimeout;
                        if (interrupted > _timeout) {
                            _log.debug("Timeout occured ({}ms)", interrupted);
                            _message = "timeout";
                            break;
                        }
                    }
                } catch (final Throwable t) {
                    _message = getExceptionMessage(Format.getMessage(t));
                    _log.warn("Stream failed", t);
                    break;
                }
            }
            _closed = true;
            synchronized (_toClose) {
                for (final Closeable toClose : _toClose) {
                    if (!this.equals(toClose)) {
                        closeQuietly(toClose);
                    }
                }
            }
        } finally {
            _log.debug("{} stream(s) plugged (-1)", _update(-1));
        }
    }

    /**
     * Gets the exception message. Allow showing better message for sftp errors in the form '4: '.
     *
     * @param input
     *            the input
     *
     * @return the exception message
     */
    public static String getExceptionMessage(final String input) {
        final var matcher = Pattern.compile("^(\\d+): $").matcher(input);
        if (matcher.matches()) {
            return "Stream failed (error code: " + matcher.group(1) + ")";
        }
        return input;
    }

    /**
     * Converts into close.
     *
     * @param toClose
     *            the close
     */
    public void toClose(final Closeable... toClose) {
        for (final Closeable object : toClose) {
            toClose(object);
        }
    }

    /**
     * Converts into close.
     *
     * @param toClose
     *            the to close
     */
    public void toClose(final Closeable toClose) {
        if (toClose instanceof final StreamCloseable closeable) {
            closeable.setStreamPlugThread(this);
        }
        _toClose.add(toClose);
    }

    /**
     * The Class StreamCloseable.
     */
    public interface StreamCloseable extends Closeable {

        /**
         * Set the StreamPlugThread.
         *
         * @param plug
         *            the plug
         */
        void setStreamPlugThread(StreamPlugThread plug);
    }
}
