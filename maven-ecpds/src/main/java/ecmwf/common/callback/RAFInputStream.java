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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class RAFInputStream.
 */
public final class RAFInputStream extends InputStream {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RAFInputStream.class);

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /** The _raf. */
    private final RandomAccessFile _raf;

    /**
     * Instantiates a new RAF input stream.
     *
     * @param raf
     *            the raf
     */
    public RAFInputStream(final RandomAccessFile raf) {
        _raf = raf;
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
        return 0;
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        if (_closed.compareAndSet(false, true)) {
            _raf.close();
        } else {
            _log.debug("Already closed");
        }
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
        return _raf.read();
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
        return _raf.read(b);
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
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return _raf.read(b, off, len);
    }

    /**
     * Reset.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void reset() throws IOException {
        _raf.seek(0);
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
        _raf.seek(n);
        return n;
    }

    /**
     * Finalize.
     *
     * @throws Throwable
     *             the throwable
     */
    @Override
    protected void finalize() throws Throwable {
        if (_closed.compareAndSet(false, true)) {
            _log.warn("Forcing close in finalize <- {}", this.getClass().getName());
            StreamPlugThread.closeQuietly(_raf);
        }
        super.finalize();
    }
}
