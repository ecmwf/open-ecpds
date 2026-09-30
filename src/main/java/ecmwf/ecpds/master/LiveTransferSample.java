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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ecmwf.common.text.Format;

/**
 * The Class LiveTransferSample.
 *
 * Lightweight, generic snapshot of an in-progress (or just-finished) data transfer, pushed periodically from a
 * DataMover to the MasterServer so that it can be broadcast to "Live ECPDS Earth" globe visualisation clients. This is
 * intentionally decoupled from ECPDS's internal transfer machinery: it only carries the information the visualisation
 * needs (who/what/where/how-fast/how-much), not full DataTransfer/Host objects.
 * <p>
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} is required here: {@link #isTerminal()} is a derived getter (no
 * backing field, not a constructor parameter), but Jackson's default bean introspection still serialises it as a
 * "terminal" JSON property on the way out. A Continental/Proxy Data Mover pushing samples over its REST relay (see
 * {@code RESTClient#updateLiveTransferStatistics}) round-trips through actual JSON, unlike a regular, RMI-connected
 * Data Mover (native Java serialization) - without this annotation, the MasterServer's {@code @JsonCreator} constructor
 * (which has no "terminal" parameter) rejects that field as unrecognised and the whole batch is dropped, silently
 * losing every sample a Continental Mover reports.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LiveTransferSample implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Status: transfer is currently in progress. */
    public static final String STATUS_ACTIVE = "ACTIVE";

    /** Status: transfer completed successfully. */
    public static final String STATUS_DONE = "DONE";

    /** Status: transfer failed/was retried. */
    public static final String STATUS_FAILED = "FAILED";

    /** Direction: data pushed out to a Dissemination Host. */
    public static final String DIRECTION_DISSEMINATION = "DISSEMINATION";

    /** Direction: data pulled in from an Acquisition Host. */
    public static final String DIRECTION_ACQUISITION = "ACQUISITION";

    /** The data transfer id this sample relates to. */
    private final long transferId;

    /** The name of the DataMover which sampled/emitted this event. */
    private final String moverName;

    /** The destination name (e.g. an alias grouping several Hosts). */
    private final String destinationName;

    /** The target Host name (nickname) the data is being sent to. */
    private final String hostName;

    /**
     * Display name (nickname) of the target Host, for human-readable labelling on the globe UI (falls back to
     * {@link #hostName} when not set).
     */
    private final String hostNickname;

    /**
     * The actual network address (hostname or IP) the target Host connects to, used to resolve its geolocation - the
     * Host's database name/id ({@link #hostName}) is an internal identifier and is never itself resolvable via GeoIP.
     */
    private final String hostAddress;

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

    /**
     * One of {@link #DIRECTION_DISSEMINATION} (data pushed out to a Host) or {@link #DIRECTION_ACQUISITION} (data
     * pulled in from a Host); defaults to {@link #DIRECTION_DISSEMINATION} when not explicitly set (e.g. samples pushed
     * by older DataMovers or code paths not yet aware of Acquisition sampling), so the globe UI can keep treating an
     * absent value the same way it always has.
     */
    private final String direction;

    /**
     * The target/source Host's actual type ({@code ecmwf.ecpds.master.transfer.HostOption} - one of "Dissemination",
     * "Acquisition", "Replication", "Source", "Backup" or "Proxy"), so the globe UI can filter/group by the real Host
     * type rather than only by {@link #direction} - several of these types share the same direction (e.g. Replication/
     * Backup/Proxy are all {@link #DIRECTION_DISSEMINATION} pushes, same as an ordinary Dissemination Host) but are
     * still meaningfully distinct to filter on individually. May be {@code null} for samples from a code path that
     * predates this field (older DataMovers) or where the Host's type genuinely couldn't be determined - the globe UI
     * falls back to {@link #direction} in that case.
     */
    private final String hostType;

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
     * @param hostNickname
     *            the host nickname (display name, falls back to hostName when blank)
     * @param hostAddress
     *            the host's actual network address (hostname or IP), used for GeoIP resolution
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
     * @param direction
     *            one of {@link #DIRECTION_DISSEMINATION} or {@link #DIRECTION_ACQUISITION}; defaults to
     *            {@link #DIRECTION_DISSEMINATION} if {@code null}
     * @param hostType
     *            the target/source Host's actual type (see {@link #hostType}); may be {@code null}
     */
    @JsonCreator
    public LiveTransferSample(@JsonProperty("transferId") final long transferId,
            @JsonProperty("moverName") final String moverName,
            @JsonProperty("destinationName") final String destinationName,
            @JsonProperty("hostName") final String hostName, @JsonProperty("hostNickname") final String hostNickname,
            @JsonProperty("hostAddress") final String hostAddress, @JsonProperty("protocol") final String protocol,
            @JsonProperty("fileSize") final long fileSize, @JsonProperty("byteSent") final long byteSent,
            @JsonProperty("duration") final long duration,
            @JsonProperty("rateBitsPerSecond") final double rateBitsPerSecond,
            @JsonProperty("status") final String status, @JsonProperty("direction") final String direction,
            @JsonProperty("hostType") final String hostType) {
        this.transferId = transferId;
        this.moverName = moverName;
        this.destinationName = destinationName;
        this.hostName = hostName;
        this.hostNickname = hostNickname;
        this.hostAddress = hostAddress;
        this.protocol = protocol;
        this.fileSize = fileSize;
        this.byteSent = byteSent;
        this.duration = duration;
        this.rateBitsPerSecond = rateBitsPerSecond;
        this.status = status;
        this.direction = direction != null ? direction : DIRECTION_DISSEMINATION;
        this.hostType = hostType;
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

    public String getHostNickname() {
        return hostNickname;
    }

    public String getHostAddress() {
        return hostAddress;
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

    public String getDirection() {
        return direction;
    }

    public String getHostType() {
        return hostType;
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
                + destinationName + ", hostName=" + hostName + ", hostNickname=" + hostNickname + ", hostAddress="
                + hostAddress + ", protocol=" + protocol + ", fileSize=" + Format.formatSize(fileSize) + ", byteSent="
                + Format.formatSize(byteSent) + ", duration=" + Format.formatDuration(duration) + ", status=" + status
                + ", direction=" + direction + ", hostType=" + hostType + "]";
    }
}
