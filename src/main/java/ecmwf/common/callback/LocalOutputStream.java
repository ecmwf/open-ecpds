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
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Class LocalOutputStream.
 */
public final class LocalOutputStream extends OutputStream {

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The out. */
    private final RemoteOutputStream out;

    /**
     * Instantiates a new local output stream.
     *
     * @param out
     *            the out
     */
    public LocalOutputStream(final RemoteOutputStream out) {
        this.out = out;
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized void close() throws IOException {
        if (closed.get())
            throw new IOException("Cannot close stream: stream is already closed");
        out.close();
    }

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        if (closed.get())
            throw new IOException("Cannot flush stream: stream is already closed");
        out.flush();
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        if (closed.get())
            throw new IOException("Cannot write to stream: stream is already closed");
        out.write(b);
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     * @param off
     *            the off
     * @param len
     *            the len
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (closed.get())
            throw new IOException("Cannot write to stream: stream is already closed");
        out.write(b, off, len);
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final int b) throws IOException {
        if (closed.get())
            throw new IOException("Cannot write to stream: stream is already closed");
        out.write(b);
    }
}
