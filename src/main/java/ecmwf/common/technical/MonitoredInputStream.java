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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class MonitoredInputStream.
 */
public class MonitoredInputStream extends FilterInputStream implements StreamMonitorInterface, ProgressInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MonitoredInputStream.class);

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The progress. */
    private final ProgressThread progress;

    /** The delta. */
    private final long delta;

    /** The byteCount. */
    private long byteCount = 0;

    /** The deltaTime. */
    private long deltaTime = System.currentTimeMillis();

    /** The startAt. */
    private long startAt = -1;

    /** The stopAt. */
    private long stopAt = -1;

    /** The closedAt. */
    private long closedAt = -1;

    /**
     * Instantiates a new monitored input stream.
     *
     * @param in
     *            the in
     */
    public MonitoredInputStream(final InputStream in) {
        this(in, -1, null);
    }

    /**
     * Instantiates a new monitored input stream. If delta is set to -1 then no debug is displayed in the logs.
     *
     * @param in
     *            the in
     * @param delta
     *            the delta
     * @param handler
     *            the handler
     */
    public MonitoredInputStream(final InputStream in, final long delta, final ProgressHandler handler) {
        super(in);
        this.delta = delta;
        if (handler != null) {
            progress = new ProgressThread(handler, this);
            progress.execute();
        } else {
            progress = null;
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
        if (closed.compareAndSet(false, true)) {
            final var currentTime = System.currentTimeMillis();
            closedAt = currentTime;
            try {
                in.close();
            } finally {
                if (stopAt == -1) {
                    stopAt = currentTime;
                }
                if (progress != null) {
                    _log.debug("Interrupting progress");
                    progress.setLoop(false);
                    progress.interrupt();
                    try {
                        progress.join(300_000L);
                    } catch (final Throwable t) {
                        _log.warn("Waiting for progress to join", t);
                    }
                }
                if (startAt != -1) {
                    _log.debug("{} byte(s) transferred - {}", byteCount, getRate());
                }
            }
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
        if (startAt == -1) {
            startAt = System.currentTimeMillis();
        }
        final var r = in.read();
        if (r >= 0) {
            byteCount++;
            if (delta > 0 && System.currentTimeMillis() - deltaTime > delta) {
                _log.debug("{} byte(s) transferred so far", byteCount);
                deltaTime = System.currentTimeMillis();
            }
        } else {
            stopAt = System.currentTimeMillis();
        }
        return r;
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
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (startAt == -1) {
            startAt = System.currentTimeMillis();
        }
        final var r = in.read(b, off, len);
        if (r > 0) {
            byteCount += r;
            if (delta > 0 && System.currentTimeMillis() - deltaTime > delta) {
                _log.debug("{} byte(s) transferred so far", byteCount);
                deltaTime = System.currentTimeMillis();
            }
        } else if (r == -1) {
            stopAt = System.currentTimeMillis();
        }
        return r;
    }

    /**
     * Sets the byte sent.
     *
     * @param byteCount
     *            the new byte sent
     */
    public void setByteSent(final long byteCount) {
        this.byteCount = byteCount;
    }

    /**
     * Gets the byte sent.
     *
     * @return the byte sent
     */
    @Override
    public long getByteSent() {
        return byteCount;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    @Override
    public long getStartTime() {
        return startAt;
    }

    /**
     * Gets the rate.
     *
     * @return the rate
     */
    @Override
    public String getRate() {
        if (startAt == -1) {
            return "0 bits transferred";
        }
        return Format.getMBitsPerSeconds(byteCount, (stopAt == -1 ? System.currentTimeMillis() : stopAt) - startAt)
                + " Mbits/s";
    }

    /**
     * Gets the rate on close.
     *
     * @return the rate on close
     */
    public String getRateOnClose() {
        if (startAt == -1) {
            return "0 bits transferred";
        }
        return Format.getMBitsPerSeconds(byteCount, (closedAt == -1 ? System.currentTimeMillis() : closedAt) - startAt)
                + " Mbits/s";
    }

    /**
     * Gets the simplified rate.
     *
     * @return the simplified rate
     */
    @Override
    public String getSimplifiedRate() {
        if (startAt == -1) {
            return "0 bytes transferred";
        }
        return Format.formatRate(byteCount, (stopAt == -1 ? System.currentTimeMillis() : stopAt) - startAt);
    }

    /**
     * Gets the simplified rate on close.
     *
     * @return the simplified rate on close
     */
    public String getSimplifiedRateOnClose() {
        if (startAt == -1) {
            return "0 bytes transferred";
        }
        return Format.formatRate(byteCount, (closedAt == -1 ? System.currentTimeMillis() : closedAt) - startAt);
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    @Override
    public long getDuration() {
        final var stopTime = stopAt == -1 ? System.currentTimeMillis() : stopAt;
        return startAt != -1 ? stopTime - startAt : 0;
    }

    /**
     * Gets the duration on close.
     *
     * @return the duration on close
     */
    public long getDurationOnClose() {
        final var closedTime = closedAt == -1 ? System.currentTimeMillis() : closedAt;
        return startAt != -1 ? closedTime - startAt : 0;
    }

    /**
     * Close and interrupt if required.
     */
    @Override
    public void closeAndInterruptIfRequired() {
        StreamPlugThread.closeQuietly(this);
    }
}
