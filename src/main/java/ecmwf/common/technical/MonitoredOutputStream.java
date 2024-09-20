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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class MonitoredOutputStream.
 */
public class MonitoredOutputStream extends FilterOutputStream implements StreamMonitorInterface, ProgressInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MonitoredOutputStream.class);

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The progress. */
    private final ProgressThread progress;

    /** The byteCount. */
    private long byteCount = 0;

    /** The startAt. */
    private long startAt = -1;

    /** The stopAt. */
    private long stopAt = -1;

    /**
     * Instantiates a new monitored output stream.
     *
     * @param out
     *            the out
     */
    public MonitoredOutputStream(final OutputStream out) {
        this(out, null);
    }

    /**
     * Instantiates a new monitored output stream.
     *
     * @param out
     *            the out
     * @param handler
     *            the handler
     */
    public MonitoredOutputStream(final OutputStream out, final ProgressHandler handler) {
        super(out);
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
            try {
                out.close();
            } finally {
                stopAt = System.currentTimeMillis();
                if (progress != null) {
                    progress.setLoop(false);
                    progress.interrupt();
                    try {
                        progress.join(5 * Timer.ONE_MINUTE);
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
        if (startAt == -1) {
            startAt = System.currentTimeMillis();
        }
        out.write(b);
        byteCount++;
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
        if (startAt == -1) {
            startAt = System.currentTimeMillis();
        }
        out.write(b, off, len);
        byteCount += len;
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
        if (startAt == -1) {
            startAt = System.currentTimeMillis();
        }
        out.write(b);
        byteCount++;
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
     * Gets the rate.
     *
     * @return the rate
     */
    @Override
    public String getRate() {
        if (startAt == -1) {
            return "0 bits transfered";
        }
        return Format.getMBitsPerSeconds(byteCount, (stopAt == -1 ? System.currentTimeMillis() : stopAt) - startAt)
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
            return "0 bytes transfered";
        }
        return Format.formatRate(byteCount, (stopAt == -1 ? System.currentTimeMillis() : stopAt) - startAt);
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
     * Gets the duration.
     *
     * @return the duration
     */
    @Override
    public long getDuration() {
        return startAt != -1 ? (stopAt == -1 ? System.currentTimeMillis() : stopAt) - startAt : 0;
    }

    /**
     * Close and interrupt if required.
     */
    @Override
    public void closeAndInterruptIfRequired() {
        StreamPlugThread.closeQuietly(this);
    }
}
