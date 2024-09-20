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
 * The Class PipedInputStream. I have imported this class from the JDK to remove
 * the check of the readSide and writeSide threads as this was breaking the
 * usage of the pipe-streams by multiple threads.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;

/**
 * The Class PipedInputStream.
 */
public final class PipedInputStream extends InputStream {

    /** The closed by writer. */
    boolean closedByWriter = false;

    /** The closed by reader. */
    volatile boolean closedByReader = false;

    /** The connected. */
    boolean connected = false;

    /**
     * The read side.
     *
     * REMIND: identification of the read and write sides needs to be more sophisticated. Either using thread groups
     * (but what about pipes within a thread?) or using finalization (but it may be a long time until the next GC).
     */
    Thread readSide;

    /** The write side. */
    Thread writeSide;

    /** The Constant DEFAULT_PIPE_SIZE. */
    private static final int DEFAULT_PIPE_SIZE = 1024;

    /**
     * The default size of the pipe's circular input buffer.
     *
     * This used to be a constant before the pipe size was allowed to change. This field will continue to be maintained
     * for backward compatibility.
     *
     * @since JDK1.1
     */
    protected static final int PIPE_SIZE = DEFAULT_PIPE_SIZE;

    /**
     * The circular buffer into which incoming data is placed.
     *
     * @since JDK1.1
     */
    protected byte[] buffer;

    /**
     * The index of the position in the circular buffer at which the next byte of data will be stored when received from
     * the connected piped output stream. <code>in&lt;0</code> implies the buffer is empty, <code>in==out</code> implies
     * the buffer is full
     *
     * @since JDK1.1
     */
    protected int in = -1;

    /**
     * The index of the position in the circular buffer at which the next byte of data will be read by this piped input
     * stream.
     *
     * @since JDK1.1
     */
    protected int out = 0;

    /**
     * Creates a <code>PipedInputStream</code> so that it is connected to the piped output stream <code>src</code>. Data
     * bytes written to <code>src</code> will then be available as input from this stream.
     *
     * @param src
     *            the stream to connect to.
     *
     * @exception IOException
     *                if an I/O error occurs.
     */
    public PipedInputStream(final PipedOutputStream src) throws IOException {
        this(src, DEFAULT_PIPE_SIZE);
    }

    /**
     * Creates a <code>PipedInputStream</code> so that it is connected to the piped output stream <code>src</code> and
     * uses the specified pipe size for the pipe's buffer. Data bytes written to <code>src</code> will then be available
     * as input from this stream.
     *
     * @param src
     *            the stream to connect to.
     * @param pipeSize
     *            the size of the pipe's buffer.
     *
     * @since 1.6
     *
     * @exception IOException
     *                if an I/O error occurs.
     * @exception IllegalArgumentException
     *                if <code>pipeSize <= 0</code>.
     */
    public PipedInputStream(final PipedOutputStream src, final int pipeSize) throws IOException {
        initPipe(pipeSize);
        connect(src);
    }

    /**
     * Creates a <code>PipedInputStream</code> so that it is not yet {@linkplain #connect(java.io.PipedOutputStream)
     * connected}. It must be {@linkplain java.io.PipedOutputStream#connect(java.io.PipedInputStream) connected} to a
     * <code>PipedOutputStream</code> before being used.
     */
    public PipedInputStream() {
        initPipe(DEFAULT_PIPE_SIZE);
    }

    /**
     * Creates a <code>PipedInputStream</code> so that it is not yet {@linkplain #connect(java.io.PipedOutputStream)
     * connected} and uses the specified pipe size for the pipe's buffer. It must be
     * {@linkplain java.io.PipedOutputStream#connect(java.io.PipedInputStream) connected} to a
     * <code>PipedOutputStream</code> before being used.
     *
     * @param pipeSize
     *            the size of the pipe's buffer.
     *
     * @since 1.6
     *
     * @exception IllegalArgumentException
     *                if <code>pipeSize <= 0</code>.
     */
    public PipedInputStream(final int pipeSize) {
        initPipe(pipeSize);
    }

    /**
     * Inits the pipe.
     *
     * @param pipeSize
     *            the pipe size
     */
    private void initPipe(final int pipeSize) {
        if (pipeSize <= 0) {
            throw new IllegalArgumentException("Pipe Size <= 0");
        }
        buffer = new byte[pipeSize];
    }

    /**
     * Causes this piped input stream to be connected to the piped output stream <code>src</code>. If this object is
     * already connected to some other piped output stream, an <code>IOException</code> is thrown.
     * <p>
     * If <code>src</code> is an unconnected piped output stream and <code>snk</code> is an unconnected piped input
     * stream, they may be connected by either the call:
     * <p>
     *
     * <pre>
     * <code>snk.connect(src)</code>
     * </pre>
     * <p>
     * or the call:
     * <p>
     *
     * <pre>
     * <code>src.connect(snk)</code>
     * </pre>
     * <p>
     * The two calls have the same effect.
     *
     * @param src
     *            The piped output stream to connect to.
     *
     * @exception IOException
     *                if an I/O error occurs.
     */
    public void connect(final PipedOutputStream src) throws IOException {
        src.connect(this);
    }

    /**
     * Receives a byte of data. This method will block if no input is available.
     *
     * @param b
     *            the byte being received
     *
     * @since JDK1.1
     *
     * @exception IOException
     *                If the pipe is <a href=#BROKEN> <code>broken</code></a>,
     *                {@link #connect(java.io.PipedOutputStream) unconnected}, closed, or if an I/O error occurs.
     */
    protected synchronized void receive(final int b) throws IOException {
        checkStateForReceive();
        // writeSide = Thread.currentThread();
        if (in == out) {
            awaitSpace();
        }
        if (in < 0) {
            in = 0;
            out = 0;
        }
        buffer[in++] = (byte) (b & 0xFF);
        if (in >= buffer.length) {
            in = 0;
        }
    }

    /**
     * Receives data into an array of bytes. This method will block until some input is available.
     *
     * @param b
     *            the buffer into which the data is received
     * @param off
     *            the start offset of the data
     * @param len
     *            the maximum number of bytes received
     *
     * @exception IOException
     *                If the pipe is <a href=#BROKEN> broken</a>, {@link #connect(java.io.PipedOutputStream)
     *                unconnected}, closed,or if an I/O error occurs.
     */
    synchronized void receive(final byte b[], int off, final int len) throws IOException {
        checkStateForReceive();
        // writeSide = Thread.currentThread();
        var bytesToTransfer = len;
        while (bytesToTransfer > 0) {
            if (in == out) {
                awaitSpace();
            }
            var nextTransferAmount = 0;
            if (out < in) {
                nextTransferAmount = buffer.length - in;
            } else if (in < out) {
                if (in == -1) {
                    in = out = 0;
                    nextTransferAmount = buffer.length - in;
                } else {
                    nextTransferAmount = out - in;
                }
            }
            if (nextTransferAmount > bytesToTransfer) {
                nextTransferAmount = bytesToTransfer;
            }
            assert nextTransferAmount > 0;
            System.arraycopy(b, off, buffer, in, nextTransferAmount);
            bytesToTransfer -= nextTransferAmount;
            off += nextTransferAmount;
            in += nextTransferAmount;
            if (in >= buffer.length) {
                in = 0;
            }
        }
    }

    /**
     * Check state for receive.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void checkStateForReceive() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        }
        if (closedByWriter || closedByReader) {
            throw new IOException("Pipe closed");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("Read end dead: " + readSide.getName());
        }
    }

    /**
     * Await space.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void awaitSpace() throws IOException {
        while (in == out) {
            checkStateForReceive();

            /* full: kick any waiting readers */
            notifyAll();
            try {
                wait(10);
            } catch (final InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
    }

    /**
     * Notifies all waiting threads that the last byte of data has been received.
     */
    synchronized void receivedLast() {
        closedByWriter = true;
        notifyAll();
    }

    /**
     * Reads the next byte of data from this piped input stream. The value byte is returned as an <code>int</code> in
     * the range <code>0</code> to <code>255</code>. This method blocks until input data is available, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
     *
     * @exception IOException
     *                if the pipe is {@link #connect(java.io.PipedOutputStream) unconnected}, <a href=#BROKEN>
     *                <code>broken</code></a>, closed, or if an I/O error occurs.
     */
    @Override
    public synchronized int read() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        }
        if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive() && !closedByWriter && in < 0) {
            throw new IOException("Write end dead: " + writeSide.getName());
        }

        // readSide = Thread.currentThread();
        var trials = 2;
        while (in < 0) {
            if (closedByWriter) {
                /* closed by writer, return EOF */
                return -1;
            }
            if (writeSide != null && !writeSide.isAlive() && --trials < 0) {
                throw new IOException("Pipe broken");
            }
            /* might be a writer waiting */
            notifyAll();
            try {
                wait(10);
            } catch (final InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        final var ret = buffer[out++] & 0xFF;
        if (out >= buffer.length) {
            out = 0;
        }
        if (in == out) {
            /* now empty */
            in = -1;
        }

        return ret;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this piped input stream into an array of bytes. Less than
     * <code>len</code> bytes will be read if the end of the data stream is reached or if <code>len</code> exceeds the
     * pipe's buffer size. If <code>len </code> is zero, then no bytes are read and 0 is returned; otherwise, the method
     * blocks until at least 1 byte of input is available, end of the stream has been detected, or an exception is
     * thrown.
     *
     * @param b
     *            the buffer into which the data is read.
     * @param off
     *            the start offset in the destination array <code>b</code>
     * @param len
     *            the maximum number of bytes read.
     *
     * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the
     *         end of the stream has been reached.
     *
     * @exception IOException
     *                if the pipe is <a href=#BROKEN> <code>broken</code></a>,
     *                {@link #connect(java.io.PipedOutputStream) unconnected}, closed, or if an I/O error occurs.
     * @exception NullPointerException
     *                If <code>b</code> is <code>null</code>.
     * @exception IndexOutOfBoundsException
     *                If <code>off</code> is negative, <code>len</code> is negative, or <code>len</code> is greater than
     *                <code>b.length - off</code>
     */
    @Override
    public synchronized int read(final byte b[], final int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        /* possibly wait on the first character */
        final var c = read();
        if (c < 0) {
            return -1;
        }
        b[off] = (byte) c;
        var rlen = 1;
        while (in >= 0 && len > 1) {

            int available;

            if (in > out) {
                available = Math.min(buffer.length - out, in - out);
            } else {
                available = buffer.length - out;
            }

            // A byte is read beforehand outside the loop
            if (available > len - 1) {
                available = len - 1;
            }
            System.arraycopy(buffer, out, b, off + rlen, available);
            out += available;
            rlen += available;
            len -= available;

            if (out >= buffer.length) {
                out = 0;
            }
            if (in == out) {
                /* now empty */
                in = -1;
            }
        }
        return rlen;
    }

    /**
     * Returns the number of bytes that can be read from this input stream without blocking.
     *
     * @return the number of bytes that can be read from this input stream without blocking, or {@code 0} if this input
     *         stream has been closed by invoking its {@link #close()} method, or if the pipe is
     *         {@link #connect(java.io.PipedOutputStream) unconnected}, or <a href=#BROKEN> <code>broken</code></a>.
     *
     * @since JDK1.0.2
     *
     * @exception IOException
     *                if an I/O error occurs.
     */
    @Override
    public synchronized int available() throws IOException {
        if (in < 0) {
            return 0;
        }
        if (in == out) {
            return buffer.length;
        } else if (in > out) {
            return in - out;
        } else {
            return in + buffer.length - out;
        }
    }

    /**
     * Closes this piped input stream and releases any system resources associated with the stream.
     *
     * @exception IOException
     *                if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        closedByReader = true;
        synchronized (this) {
            in = -1;
        }
    }
}
