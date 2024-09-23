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

import ecmwf.common.text.Format;

/**
 * The Class TimedFilterOutputStream.
 */
public class TimedFilterOutputStream extends FilterOutputStream {

    /** The timeout millis. */
    private final long timeoutMillis;

    /** The maximum duration. */
    private final long maximumDuration;

    /** The required transfer rate. */
    private final long requiredTransferRate; // bytes per second

    /** The start time. */
    private final long startTime;

    /** The bytes written. */
    private long bytesWritten;

    /** The start step time. */
    private long startStepTime;

    /**
     * Instantiates a new timed filter output stream.
     *
     * @param out
     *            the out
     * @param timeoutMillis
     *            the timeout millis
     * @param requiredTransferRate
     *            the required transfer rate
     * @param maximumDuration
     *            the maximum duration
     */
    public TimedFilterOutputStream(final OutputStream out, final long timeoutMillis, final long requiredTransferRate,
            final long maximumDuration) {
        super(out);
        this.timeoutMillis = timeoutMillis;
        this.maximumDuration = maximumDuration;
        this.requiredTransferRate = requiredTransferRate;
        this.bytesWritten = 0;
        final var currentTime = System.currentTimeMillis();
        this.startTime = currentTime;
        this.startStepTime = currentTime;
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final int b) throws IOException {
        super.write(b);
        bytesWritten++;
        checkTransferRate();
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        super.write(b, off, len);
        bytesWritten += len;
        checkTransferRate();
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
            final var transferRate = bytesWritten / (elapsedTimeMillis / 1000);
            if (transferRate < requiredTransferRate) {
                throw new TransferTimeoutException(
                        "Transfer rate below the configured limit (" + Format.toMBits(transferRate) + " Mbits/s < "
                                + Format.toMBits(requiredTransferRate) + " Mbits/s)");
            }
            // Reset counters
            bytesWritten = 0;
            startStepTime = currentTime;
        }
    }
}
