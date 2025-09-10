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

package ecmwf.common.callback;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Class LocalInputStream.
 */
public final class LocalInputStream extends InputStream {

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The in. */
    private final RemoteInputStream in;

    /**
     * Instantiates a new local input stream.
     *
     * @param in
     *            the in
     */
    public LocalInputStream(final RemoteInputStream in) {
        this.in = in;
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
        if (closed.get())
            throw new IOException("Cannot close stream: stream is already closed");
        return in.available();
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true))
            in.close();
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
        if (closed.get())
            throw new IOException("Cannot read from stream: stream is already closed");
        return in.read();
    }

    /**
     * Read.
     *
     * @param b
     *            the b
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Read.
     *
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
    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        if (closed.get())
            throw new IOException("Cannot read from stream: stream is already closed");
        final var data = in.read(len);
        if ((len = data.getLen()) > 0) {
            System.arraycopy(data.getBytes(), 0, b, off, len);
        }
        return len;
    }

    /**
     * Reset.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void reset() throws IOException {
        if (closed.get())
            throw new IOException("Cannot reset stream: stream is already closed");
        in.reset();
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
        if (closed.get())
            throw new IOException("Cannot skip stream: stream is already closed");
        return in.skip(n);
    }
}
