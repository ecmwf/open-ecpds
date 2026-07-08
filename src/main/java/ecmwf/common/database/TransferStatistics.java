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

import java.math.BigDecimal;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * Represents a single TCP connection recorded during a data transfer. One DataTransfer may have multiple entries (e.g.
 * parallel S3 connections). Key fields are stored as typed columns for querying; the full raw ss(8) output is kept in
 * TST_RAW for reference.
 */
public final class TransferStatistics extends DataBaseObject {

    private static final long serialVersionUID = 1L;

    protected Long TST_ID;
    protected BigDecimal DAT_ID;
    protected BigDecimal TST_START_TIME;
    protected BigDecimal TST_END_TIME;
    protected String TST_LOCAL_ADDRESS;
    protected String TST_REMOTE_ADDRESS;
    protected Double TST_RTT_MS;
    protected Long TST_BYTES_SENT;
    protected Long TST_BYTES_RECEIVED;
    protected Long TST_PACING_RATE_BPS;
    protected Long TST_DELIVERY_RATE_BPS;
    protected Integer TST_CWND;
    protected Integer TST_SEGS_OUT;
    protected Integer TST_SEGS_IN;
    protected String TST_RAW;
    protected int TST_REQUEUE_HISTORY;

    public long getId() {
        return TST_ID != null ? TST_ID : 0;
    }

    public void setId(final long v) {
        TST_ID = v;
    }

    public long getDataTransferId() {
        return DAT_ID != null ? DAT_ID.longValue() : 0;
    }

    public void setDataTransferId(final long v) {
        DAT_ID = BigDecimal.valueOf(v);
    }

    public long getStartTime() {
        return TST_START_TIME != null ? TST_START_TIME.longValue() : 0;
    }

    public void setStartTime(final long v) {
        TST_START_TIME = BigDecimal.valueOf(v);
    }

    public long getEndTime() {
        return TST_END_TIME != null ? TST_END_TIME.longValue() : 0;
    }

    public void setEndTime(final long v) {
        TST_END_TIME = BigDecimal.valueOf(v);
    }

    public long getDurationMs() {
        return getEndTime() - getStartTime();
    }

    public String getLocalAddress() {
        return TST_LOCAL_ADDRESS;
    }

    public void setLocalAddress(final String v) {
        TST_LOCAL_ADDRESS = v;
    }

    public String getRemoteAddress() {
        return TST_REMOTE_ADDRESS;
    }

    public void setRemoteAddress(final String v) {
        TST_REMOTE_ADDRESS = v;
    }

    public Double getRttMs() {
        return TST_RTT_MS;
    }

    public void setRttMs(final Double v) {
        TST_RTT_MS = v;
    }

    public Long getBytesSent() {
        return TST_BYTES_SENT;
    }

    public void setBytesSent(final Long v) {
        TST_BYTES_SENT = v;
    }

    public Long getBytesReceived() {
        return TST_BYTES_RECEIVED;
    }

    public void setBytesReceived(final Long v) {
        TST_BYTES_RECEIVED = v;
    }

    public Long getPacingRateBps() {
        return TST_PACING_RATE_BPS;
    }

    public void setPacingRateBps(final Long v) {
        TST_PACING_RATE_BPS = v;
    }

    public Long getDeliveryRateBps() {
        return TST_DELIVERY_RATE_BPS;
    }

    public void setDeliveryRateBps(final Long v) {
        TST_DELIVERY_RATE_BPS = v;
    }

    public Integer getCwnd() {
        return TST_CWND;
    }

    public void setCwnd(final Integer v) {
        TST_CWND = v;
    }

    public Integer getSegsOut() {
        return TST_SEGS_OUT;
    }

    public void setSegsOut(final Integer v) {
        TST_SEGS_OUT = v;
    }

    public Integer getSegsIn() {
        return TST_SEGS_IN;
    }

    public void setSegsIn(final Integer v) {
        TST_SEGS_IN = v;
    }

    public String getRaw() {
        return TST_RAW;
    }

    public void setRaw(final String v) {
        TST_RAW = v;
    }

    public int getRequeueHistory() {
        return TST_REQUEUE_HISTORY;
    }

    public void setRequeueHistory(final int v) {
        TST_REQUEUE_HISTORY = v;
    }
}
