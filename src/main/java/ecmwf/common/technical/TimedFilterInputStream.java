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

import ecmwf.common.text.Format;

/**
 * The Class TimedFilterInputStream.
 */
public class TimedFilterInputStream extends FilterInputStream {

    /** The timeout millis. */
    private final long timeoutMillis;

    /** The maximum duration. */
    private final long maximumDuration;

    /** The required transfer rate. */
    private final long requiredTransferRate; // bytes per second

    /** The start time. */
    private final long startTime;

    /** The bytes read. */
    private long bytesRead;

    /** The start step time. */
    private long startStepTime;

    /**
     * Instantiates a new timed filter input stream.
     *
     * @param in
     *            the in
     * @param timeoutMillis
     *            the timeout millis
     * @param requiredTransferRate
     *            the required transfer rate
     * @param maximumDuration
     *            the maximum duration
     */
    public TimedFilterInputStream(final InputStream in, final long timeoutMillis, final long requiredTransferRate,
            final long maximumDuration) {
        super(in);
        this.timeoutMillis = timeoutMillis;
        this.maximumDuration = maximumDuration;
        this.requiredTransferRate = requiredTransferRate;
        this.bytesRead = 0;
        final var currentTime = System.currentTimeMillis();
        this.startTime = currentTime;
        this.startStepTime = currentTime;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read() throws IOException {
        final var read = super.read();
        if (read != -1) {
            bytesRead++;
            checkTransferRate();
        }
        return read;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final var read = super.read(b, off, len);
        if (read != -1) {
            bytesRead += read;
            checkTransferRate();
        }
        return read;
    }

    /**
     * Check transfer rate.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void checkTransferRate() throws IOException {
        final var currentTime = System.currentTimeMillis();
        // Check transfer duration when the specified duration is over
        final var transferDuration = currentTime - startTime;
        if (transferDuration > maximumDuration) {
            throw new TransferTimeoutException("Transfer duration over the configured limit ("
                    + Format.formatDuration(transferDuration) + " > " + Format.formatDuration(maximumDuration) + ")");
        }
        // Check transfer rate when the specified time is over
        final var elapsedTimeMillis = currentTime - startStepTime;
        if (elapsedTimeMillis > timeoutMillis) {
            final var transferRate = bytesRead / (elapsedTimeMillis / 1000);
            if (transferRate < requiredTransferRate) {
                throw new TransferTimeoutException(
                        "Transfer rate below the configured limit (" + Format.toMBits(transferRate) + " Mbits/s < "
                                + Format.toMBits(requiredTransferRate) + " Mbits/s)");
            }
            // Reset counters
            bytesRead = 0;
            startStepTime = currentTime;
        }
    }
}
