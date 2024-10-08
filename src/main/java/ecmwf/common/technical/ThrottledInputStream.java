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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;

/**
 * The Class ThrottledInputStream.
 */
public class ThrottledInputStream extends InputStream {

    /** The Constant SLEEP_DURATION_MS. */
    private static final long SLEEP_DURATION_MS = 30;

    /** The in. */
    private final InputStream _in;

    /** The max bytes per sec. */
    private final long _maxBytesPerSec;

    /** The start time. */
    private final long _startTime = System.nanoTime();

    /** The bytes read. */
    private long _bytesRead = 0;

    /** The total sleep time. */
    private long _totalSleepTime = 0;

    /**
     * Instantiates a new throttled input stream.
     *
     * @param inputStream
     *            the input stream
     */
    public ThrottledInputStream(final InputStream inputStream) {
        this(inputStream, Long.MAX_VALUE);
    }

    /**
     * Instantiates a new throttled input stream.
     *
     * @param inputStream
     *            the input stream
     * @param maxBytesPerSec
     *            the max bytes per sec
     */
    public ThrottledInputStream(final InputStream inputStream, final long maxBytesPerSec) {
        if (maxBytesPerSec < 0) {
            throw new IllegalArgumentException("maxBytesPerSec shouldn't be negative");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream shouldn't be null");
        }

        this._in = inputStream;
        this._maxBytesPerSec = maxBytesPerSec;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        _in.close();
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read() throws IOException {
        throttle();
        final var data = _in.read();
        if (data != -1) {
            _bytesRead++;
        }
        return data;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        throttle();
        final var readLen = _in.read(b);
        if (readLen != -1) {
            _bytesRead += readLen;
        }
        return readLen;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        throttle();
        final var readLen = _in.read(b, off, len);
        if (readLen != -1) {
            _bytesRead += readLen;
        }
        return readLen;
    }

    /**
     * Throttle.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void throttle() throws IOException {
        while (getBytesPerSec() > _maxBytesPerSec) {
            try {
                Thread.sleep(SLEEP_DURATION_MS);
                _totalSleepTime += SLEEP_DURATION_MS;
            } catch (final InterruptedException e) {
                throw new IOException("Thread interrupted", e);
            }
        }
    }

    /**
     * Gets the total bytes read.
     *
     * @return the total bytes read
     */
    public long getTotalBytesRead() {
        return _bytesRead;
    }

    /**
     * Return the number of bytes read per second.
     *
     * @return the bytes per sec
     */
    public long getBytesPerSec() {
        final var elapsed = (System.nanoTime() - _startTime) / 1000000000;
        if (elapsed == 0) {
            return _bytesRead;
        }
        return _bytesRead / elapsed;
    }

    /**
     * Gets the total sleep time.
     *
     * @return the total sleep time
     */
    public long getTotalSleepTime() {
        return _totalSleepTime;
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "ThrottledInputStream{" + "bytesRead=" + _bytesRead + ", maxBytesPerSec=" + _maxBytesPerSec
                + ", bytesPerSec=" + getBytesPerSec() + ", totalSleepTimeInSeconds=" + _totalSleepTime / 1000 + '}';
    }
}
