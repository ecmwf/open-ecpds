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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class NullOutputStream.
 */
public class NullOutputStream extends OutputStream {
    /** The _bytes sent. */
    private long _bytesSent = 0;

    /** The _bytes per second. */
    private long _bytesPerSecond = 1024 * 1024;

    /** The _closed. */
    private boolean _closed = false;

    /**
     * Instantiates a new null output stream.
     */
    public NullOutputStream() {
    }

    /**
     * Instantiates a new null output stream.
     *
     * @param bytesPerSecond
     *            the bytes per second
     */
    public NullOutputStream(final long bytesPerSecond) {
        _bytesPerSecond = bytesPerSecond;
    }

    /**
     * Write.
     *
     * @param i
     *            the i
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final int i) throws IOException {
        if (_closed) {
            throw new IOException("Closed");
        }
        if (_bytesSent++ > _bytesPerSecond) {
            _bytesSent = 0;
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException e) {
            }
        }
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
    public void write(final byte b[]) throws IOException {
        write(b, 0, b.length);
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
    public void write(final byte b[], final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (var i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        _closed = true;
    }
}
