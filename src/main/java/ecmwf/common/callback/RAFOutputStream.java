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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class RAFOutputStream.
 */
public final class RAFOutputStream extends OutputStream {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RAFInputStream.class);

    /** The _raf. */
    private final RandomAccessFile _raf;

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /**
     * Instantiates a new RAF output stream.
     *
     * @param raf
     *            the raf
     */
    public RAFOutputStream(final RandomAccessFile raf) {
        _raf = raf;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
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
     * {@inheritDoc}
     *
     * Flush.
     */
    @Override
    public void flush() throws IOException {
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        _raf.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        _raf.write(b, off, len);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final int b) throws IOException {
        _raf.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * Finalize.
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
