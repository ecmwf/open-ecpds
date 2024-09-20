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
 * An {@code InputStream} wrapper that provides up to a maximum number of
 * bytes from the underlying stream.  Does not support mark/reset, even
 * when the wrapped stream does, and does not perform any buffering.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;

/**
 * The Class BoundedInputStream.
 */
public class BoundedInputStream extends InputStream {

    /** This stream's underlying @{code InputStream}. */
    private final InputStream data;

    /** The maximum number of bytes still available from this stream. */
    private long bytesRemaining;

    /**
     * Initializes a new {@code BoundedInputStream} with the specified underlying stream and byte limit.
     *
     * @param data
     *            the @{code InputStream} serving as the source of this one's data
     * @param maxBytes
     *            the maximum number of bytes this stream will deliver before signaling end-of-data
     */
    public BoundedInputStream(final InputStream data, final long maxBytes) {
        this.data = data;
        bytesRemaining = Math.max(maxBytes, 0);
    }

    /**
     * Available.
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int available() throws IOException {
        return (int) Math.min(data.available(), bytesRemaining);
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        data.close();
    }

    /**
     * Mark.
     *
     * @param limit
     *            the limit
     */
    @Override
    public synchronized void mark(final int limit) {
        // does nothing
    }

    /**
     * Mark supported.
     *
     * @return true, if successful
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Read.
     *
     * @param buf
     *            the buf
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
    @Override
    public int read(final byte[] buf, final int off, final int len) throws IOException {
        if (bytesRemaining > 0) {
            final var nRead = data.read(buf, off, (int) Math.min(len, bytesRemaining));

            bytesRemaining -= nRead;

            return nRead;
        }
        return -1;
    }

    /**
     * Read.
     *
     * @param buf
     *            the buf
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int read(final byte[] buf) throws IOException {
        return this.read(buf, 0, buf.length);
    }

    /**
     * Reset.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("reset() not supported");
    }

    /**
     * Skip.
     *
     * @param n
     *            the n
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long skip(final long n) throws IOException {
        final var skipped = data.skip(Math.min(n, bytesRemaining));

        bytesRemaining -= skipped;

        return skipped;
    }

    /**
     * Read.
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int read() throws IOException {
        if (bytesRemaining <= 0) {
            return -1;
        }
        final var c = data.read();

        if (c >= 0) {
            bytesRemaining -= 1;
        }

        return c;
    }
}