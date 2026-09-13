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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ecmwf.common.text.Format;

/**
 * The Class LiveTransferSample.
 *
 * Lightweight, generic snapshot of an in-progress (or just-finished) data transfer, pushed periodically from a
 * DataMover to the MasterServer so that it can be broadcast to "Live ECPDS Earth" globe visualisation clients. This is
 * intentionally decoupled from ECPDS's internal transfer machinery: it only carries the information the visualisation
 * needs (who/what/where/how-fast/how-much), not full DataTransfer/Host objects.
 */
public final class LiveTransferSample implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Status: transfer is currently in progress. */
    public static final String STATUS_ACTIVE = "ACTIVE";

    /** Status: transfer completed successfully. */
    public static final String STATUS_DONE = "DONE";

    /** Status: transfer failed/was retried. */
    public static final String STATUS_FAILED = "FAILED";

    /** The data transfer id this sample relates to. */
    private final long transferId;

    /** The name of the DataMover which sampled/emitted this event. */
    private final String moverName;

    /** The destination name (e.g. an alias grouping several Hosts). */
    private final String destinationName;

    /** The target Host name (nickname) the data is being sent to. */
    private final String hostName;

    /** The ectrans module/protocol in use (e.g. ftp, sftp, http, s3, ...). */
    private final String protocol;

    /** Total size, in bytes, of the file being transferred (-1 if unknown). */
    private final long fileSize;

    /** Number of bytes sent/received so far. */
    private final long byteSent;

    /** Duration, in milliseconds, since the transfer started. */
    private final long duration;

    /** Instantaneous/average transfer rate in bits per second (-1 if not yet known). */
    private final double rateBitsPerSecond;

    /** One of {@link #STATUS_ACTIVE}, {@link #STATUS_DONE} or {@link #STATUS_FAILED}. */
    private final String status;

    /** Wall-clock time (epoch millis) at which the sample was taken. */
    private final long timestamp;

    /**
     * Instantiates a new live transfer sample.
     *
     * @param transferId
     *            the transfer id
     * @param moverName
     *            the mover name
     * @param destinationName
     *            the destination name
     * @param hostName
     *            the host name
     * @param protocol
     *            the protocol
     * @param fileSize
     *            the file size
     * @param byteSent
     *            the byte sent
     * @param duration
     *            the duration
     * @param rateBitsPerSecond
     *            the rate bits per second
     * @param status
     *            the status
     */
    @JsonCreator
    public LiveTransferSample(@JsonProperty("transferId") final long transferId,
            @JsonProperty("moverName") final String moverName,
            @JsonProperty("destinationName") final String destinationName,
            @JsonProperty("hostName") final String hostName, @JsonProperty("protocol") final String protocol,
            @JsonProperty("fileSize") final long fileSize, @JsonProperty("byteSent") final long byteSent,
            @JsonProperty("duration") final long duration,
            @JsonProperty("rateBitsPerSecond") final double rateBitsPerSecond,
            @JsonProperty("status") final String status) {
        this.transferId = transferId;
        this.moverName = moverName;
        this.destinationName = destinationName;
        this.hostName = hostName;
        this.protocol = protocol;
        this.fileSize = fileSize;
        this.byteSent = byteSent;
        this.duration = duration;
        this.rateBitsPerSecond = rateBitsPerSecond;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTransferId() {
        return transferId;
    }

    public String getMoverName() {
        return moverName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getProtocol() {
        return protocol;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getByteSent() {
        return byteSent;
    }

    public long getDuration() {
        return duration;
    }

    public double getRateBitsPerSecond() {
        return rateBitsPerSecond;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isTerminal() {
        return STATUS_DONE.equals(status) || STATUS_FAILED.equals(status);
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "LiveTransferSample [transferId=" + transferId + ", moverName=" + moverName + ", destinationName="
                + destinationName + ", hostName=" + hostName + ", protocol=" + protocol + ", fileSize="
                + Format.formatSize(fileSize) + ", byteSent=" + Format.formatSize(byteSent) + ", duration="
                + Format.formatDuration(duration) + ", status=" + status + "]";
    }
}
