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

package ecmwf.ecpds.master.plugin.http.controller.user.incoming;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.common.database.IncomingHistory;
import ecmwf.common.text.Format;

/**
 * The Class PresentationHistory.
 */
public class PresentationHistory {

    /** The history. */
    private final IncomingHistory history;

    /**
     * Instantiates a new presentation history.
     *
     * @param history
     *            the history
     */
    public PresentationHistory(final IncomingHistory history) {
        this.history = history;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return history.getUserName();
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return history.getProtocol();
    }

    /**
     * Gets the upload.
     *
     * @return the upload
     */
    public boolean getUpload() {
        return history.getUpload();
    }

    /**
     * Gets the host address.
     *
     * @return the host address
     */
    public String getHostAddress() {
        return history.getHostAddress();
    }

    /**
     * Gets the transfer server name.
     *
     * @return the transfer server name
     */
    public String getTransferServerName() {
        return history.getTransferServer();
    }

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return history.getDestination();
    }

    /**
     * Gets the data transfer id.
     *
     * @return the data transfer id
     */
    public Long getDataTransferId() {
        return history.getDataTransferId();
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return history.getFileName();
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public Date getStartTime() {
        return history.getStartTime();
    }

    /**
     * Gets the finish time.
     *
     * @return the finish time
     */
    public Date getFinishTime() {
        return new Date(history.getStartTime().getTime() + history.getDuration());
    }

    /**
     * Gets the rate.
     *
     * @return the rate
     */
    public double getRate() {
        return Format.getMBitsPerSeconds(history.getSent(), history.getDuration());
    }

    /**
     * Gets the formatted rate.
     *
     * @return the formatted rate
     */
    public String getFormattedRate() {
        return Format.formatRate(history.getSent(), history.getDuration());
    }

    /**
     * Gets the bytes.
     *
     * @return the bytes
     */
    public long getBytes() {
        return history.getSent();
    }

    /**
     * Gets the formatted bytes.
     *
     * @return the formatted bytes
     */
    public String getFormattedBytes() {
        return Format.formatSize(history.getSent());
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return history.getDuration();
    }

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     */
    public String getFormattedDuration() {
        return Format.formatDuration(history.getDuration());
    }
}
