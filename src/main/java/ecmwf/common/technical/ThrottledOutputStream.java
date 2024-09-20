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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class ThrottledOutputStream.
 */
public class ThrottledOutputStream extends OutputStream {

    /** The Constant SLEEP_DURATION_MS. */
    private static final long SLEEP_DURATION_MS = 30;

    /** The out. */
    private final OutputStream _out;

    /** The max bytes per second. */
    private final long _maxBytesPerSecond;

    /** The start time. */
    private final long _startTime = System.nanoTime();

    /** The bytes write. */
    private long _bytesWrite = 0;

    /** The total sleep time. */
    private long _totalSleepTime = 0;

    /**
     * Instantiates a new throttled output stream.
     *
     * @param outputStream
     *            the output stream
     */
    public ThrottledOutputStream(final OutputStream outputStream) {
        this(outputStream, Long.MAX_VALUE);
    }

    /**
     * Instantiates a new throttled output stream.
     *
     * @param outputStream
     *            the output stream
     * @param maxBytesPerSecond
     *            the max bytes per second
     */
    public ThrottledOutputStream(final OutputStream outputStream, final long maxBytesPerSecond) {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream shouldn't be null");
        }

        if (maxBytesPerSecond <= 0) {
            throw new IllegalArgumentException("maxBytesPerSecond should be greater than zero");
        }

        this._out = outputStream;
        this._maxBytesPerSecond = maxBytesPerSecond;
    }

    /**
     * Write.
     *
     * @param arg0
     *            the arg 0
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final int arg0) throws IOException {
        throttle();
        _out.write(arg0);
        _bytesWrite++;
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
        if (len < _maxBytesPerSecond) {
            throttle();
            _bytesWrite = _bytesWrite + len;
            _out.write(b, off, len);
            return;
        }

        long currentOffSet = off;
        long remainingBytesToWrite = len;

        do {
            throttle();
            remainingBytesToWrite = remainingBytesToWrite - _maxBytesPerSecond;
            _bytesWrite = _bytesWrite + _maxBytesPerSecond;
            _out.write(b, (int) currentOffSet, (int) _maxBytesPerSecond);
            currentOffSet = currentOffSet + _maxBytesPerSecond;
        } while (remainingBytesToWrite > _maxBytesPerSecond);

        throttle();
        _bytesWrite = _bytesWrite + remainingBytesToWrite;
        _out.write(b, (int) currentOffSet, (int) remainingBytesToWrite);
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
        this.write(b, 0, b.length);
    }

    /**
     * Throttle.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void throttle() throws IOException {
        while (getBytesPerSec() > _maxBytesPerSecond) {
            try {
                Thread.sleep(SLEEP_DURATION_MS);
                _totalSleepTime += SLEEP_DURATION_MS;
            } catch (final InterruptedException e) {
                throw new IOException("Thread interrupted", e);
            }
        }
    }

    /**
     * Return the number of bytes read per second.
     *
     * @return the bytes per sec
     */
    public long getBytesPerSec() {
        final var elapsed = (System.nanoTime() - _startTime) / 1000000000;
        if (elapsed == 0) {
            return _bytesWrite;
        }
        return _bytesWrite / elapsed;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ThrottledOutputStream{" + "bytesWrite=" + _bytesWrite + ", maxBytesPerSecond=" + _maxBytesPerSecond
                + ", bytesPerSec=" + getBytesPerSec() + ", totalSleepTimeInSeconds=" + _totalSleepTime / 1000 + '}';
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        _out.close();
    }
}