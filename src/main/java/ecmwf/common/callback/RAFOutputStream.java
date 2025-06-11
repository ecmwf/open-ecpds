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
import java.io.RandomAccessFile;

import ecmwf.common.technical.CleanableSupport;

/**
 * The Class RAFOutputStream.
 */
public final class RAFOutputStream extends OutputStream {

    /** Cleaner support for resource cleanup. */
    private final CleanableSupport cleaner;

    /** The raf. */
    private final RandomAccessFile raf;

    /**
     * Instantiates a new RAF output stream.
     *
     * @param raf
     *            the raf
     */
    public RAFOutputStream(final RandomAccessFile raf) {
        this.raf = raf;
        // Setup GC cleanup hook
        this.cleaner = new CleanableSupport(this, this::cleanup);
    }

    /**
     * Closes this stream and performs all associated cleanup.
     *
     * @throws IOException
     *             If an error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        cleaner.close();
    }

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        // No implementation required.
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
        raf.write(b);
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
        raf.write(b, off, len);
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
        raf.write(b);
    }

    /**
     * Cleans up resources and terminates the process if necessary.
     *
     * @throws IOException
     *             If an error occurs during cleanup.
     */
    private void cleanup() throws IOException {
        raf.close();
    }
}
