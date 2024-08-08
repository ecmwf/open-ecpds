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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;

/**
 * The Class TransferHistory.
 */
public class TransferHistory extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -536725907129766950L;

    /** The dat id. */
    protected long DAT_ID;

    /** The hos name. */
    protected Integer HOS_NAME;

    /** The sta code. */
    protected String STA_CODE;

    /** The trh comment. */
    protected String TRH_COMMENT;

    /** The des name. */
    protected String DES_NAME;

    /** The trh error. */
    protected boolean TRH_ERROR;

    /** The trh id. */
    protected long TRH_ID;

    /** The trh sent. */
    protected long TRH_SENT;

    /** The trh time. */
    protected BigDecimal TRH_TIME;

    /** The data transfer. */
    protected DataTransfer dataTransfer;

    /** The host. */
    protected Host host;

    /**
     * Instantiates a new transfer history.
     */
    public TransferHistory() {
    }

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return DES_NAME;
    }

    /**
     * Sets the destination name.
     *
     * @param param
     *            the new destination name
     */
    public void setDestinationName(final String param) {
        DES_NAME = param;
    }

    /**
     * Instantiates a new transfer history.
     *
     * @param id
     *            the id
     */
    public TransferHistory(final long id) {
        setId(id);
    }

    /**
     * Gets the data transfer id.
     *
     * @return the data transfer id
     */
    public long getDataTransferId() {
        return DAT_ID;
    }

    /**
     * Sets the data transfer id.
     *
     * @param param
     *            the new data transfer id
     */
    public void setDataTransferId(final long param) {
        DAT_ID = param;
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostName() {
        return integerToString(HOS_NAME);
    }

    /**
     * Sets the host name.
     *
     * @param param
     *            the new host name
     */
    public void setHostName(final String param) {
        HOS_NAME = stringToInteger(param);
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public String getStatusCode() {
        return STA_CODE;
    }

    /**
     * Sets the status code.
     *
     * @param param
     *            the new status code
     */
    public void setStatusCode(final String param) {
        STA_CODE = param;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return TRH_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        TRH_COMMENT = strim(param, 255);
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public boolean getError() {
        return TRH_ERROR;
    }

    /**
     * Sets the error.
     *
     * @param param
     *            the new error
     */
    public void setError(final boolean param) {
        TRH_ERROR = param;
    }

    /**
     * Sets the error.
     *
     * @param param
     *            the new error
     */
    public void setError(final String param) {
        TRH_ERROR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return TRH_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        TRH_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        TRH_ID = Long.parseLong(param);
    }

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    public long getSent() {
        return TRH_SENT;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final long param) {
        TRH_SENT = param;
    }

    /**
     * Sets the sent.
     *
     * @param param
     *            the new sent
     */
    public void setSent(final String param) {
        TRH_SENT = Long.parseLong(param);
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public java.sql.Timestamp getTime() {
        return bigDecimalToTimestamp(TRH_TIME);
    }

    /**
     * Sets the time.
     *
     * @param param
     *            the new time
     */
    public void setTime(final java.sql.Timestamp param) {
        TRH_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the data transfer.
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer() {
        return dataTransfer;
    }

    /**
     * Sets the data transfer.
     *
     * @param param
     *            the new data transfer
     */
    public void setDataTransfer(final DataTransfer param) {
        dataTransfer = param;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public Host getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param param
     *            the new host
     */
    public void setHost(final Host param) {
        host = param;
    }
}
