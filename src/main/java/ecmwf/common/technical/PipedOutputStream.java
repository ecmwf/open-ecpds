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
 * The Class PipedOutputStream. I have imported this class from the JDK to
 * remove the check of the readSide and writeSide threads as this was breaking
 * the usage of the pipe-streams by multiple threads!
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class PipedOutputStream.
 */
public final class PipedOutputStream extends OutputStream {

    /**
     * The sink.
     *
     * REMIND: identification of the read and write sides needs to be more sophisticated. Either using thread groups
     * (but what about pipes within a thread?) or using finalization (but it may be a long time until the next GC).
     */
    private PipedInputStream sink;

    /**
     * Creates a piped output stream connected to the specified piped input stream. Data bytes written to this stream
     * will then be available as input from <code>snk</code>.
     *
     * @param snk
     *            The piped input stream to connect to.
     *
     * @exception IOException
     *                if an I/O error occurs.
     *
     * @throws java.io.IOException
     *             if any.
     */
    public PipedOutputStream(final PipedInputStream snk) throws IOException {
        connect(snk);
    }

    /**
     * Creates a piped output stream that is not yet connected to a piped input stream. It must be connected to a piped
     * input stream, either by the receiver or the sender, before being used.
     *
     * @see java.io.PipedInputStream#connect(java.io.PipedOutputStream)
     * @see java.io.PipedOutputStream#connect(java.io.PipedInputStream)
     */
    public PipedOutputStream() {
    }

    /**
     * Connects this piped output stream to a receiver. If this object is already connected to some other piped input
     * stream, an <code>IOException</code> is thrown.
     * <p>
     * If <code>snk</code> is an unconnected piped input stream and <code>src</code> is an unconnected piped output
     * stream, they may be connected by either the call: <blockquote>
     *
     * <pre>
     * src.connect(snk)
     * </pre>
     *
     * </blockquote> or the call: <blockquote>
     *
     * <pre>
     * snk.connect(src)
     * </pre>
     *
     * </blockquote> The two calls have the same effect.
     *
     * @param snk
     *            the piped input stream to connect to.
     *
     * @exception IOException
     *                if an I/O error occurs.
     *
     * @throws java.io.IOException
     *             if any.
     */
    public synchronized void connect(final PipedInputStream snk) throws IOException {
        if (snk == null) {
            throw new NullPointerException();
        }
        if (sink != null || snk.connected) {
            throw new IOException("Already connected");
        }
        sink = snk;
        snk.in = -1;
        snk.out = 0;
        snk.connected = true;
    }

    /**
     * {@inheritDoc}
     *
     * Writes the specified <code>byte</code> to the piped output stream.
     * <p>
     * Implements the <code>write</code> method of <code>OutputStream</code>.
     *
     * @exception IOException
     *                if the pipe is <a href=#BROKEN> broken</a>, {@link #connect(java.io.PipedInputStream)
     *                unconnected}, closed, or if an I/O error occurs.
     */
    @Override
    public void write(final int b) throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        }
        sink.receive(b);
    }

    /**
     * {@inheritDoc}
     *
     * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code> to this piped
     * output stream. This method blocks until all the bytes are written to the output stream.
     *
     * @exception IOException
     *                if the pipe is <a href=#BROKEN> broken</a>, {@link #connect(java.io.PipedInputStream)
     *                unconnected}, closed, or if an I/O error occurs.
     */
    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        }
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        sink.receive(b, off, len);
    }

    /**
     * {@inheritDoc}
     *
     * Flushes this output stream and forces any buffered output bytes to be written out. This will notify any readers
     * that bytes are waiting in the pipe.
     *
     * @exception IOException
     *                if an I/O error occurs.
     */
    @Override
    public synchronized void flush() throws IOException {
        if (sink != null) {
            synchronized (sink) {
                sink.notifyAll();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Closes this piped output stream and releases any system resources associated with this stream. This stream may no
     * longer be used for writing bytes.
     *
     * @exception IOException
     *                if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        if (sink != null) {
            sink.receivedLast();
        }
    }
}
