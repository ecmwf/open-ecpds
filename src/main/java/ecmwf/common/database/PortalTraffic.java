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

import java.sql.Timestamp;

/**
 * ECMWF Product Data Store (OpenECPDS) Project. Represents one minute-bucket of Data Portal traffic for a specific user
 * (or all users when PTR_USER is empty).
 */
public final class PortalTraffic extends DataBaseObject {

    private static final long serialVersionUID = 1L;

    protected String PTR_USER = "";
    protected Timestamp PTR_TIME;
    protected int PTR_CONNECTIONS = 0;
    protected long PTR_BYTES_IN = 0;
    protected long PTR_BYTES_OUT = 0;
    protected long PTR_DURATION_IN = 0;
    protected long PTR_DURATION_OUT = 0;

    public String getUser() {
        return PTR_USER;
    }

    public void setUser(final String v) {
        PTR_USER = v;
    }

    public Timestamp getTime() {
        return PTR_TIME;
    }

    public void setTime(final Timestamp v) {
        PTR_TIME = v;
    }

    public int getConnections() {
        return PTR_CONNECTIONS;
    }

    public void setConnections(final int v) {
        PTR_CONNECTIONS = v;
    }

    public long getBytesIn() {
        return PTR_BYTES_IN;
    }

    public void setBytesIn(final long v) {
        PTR_BYTES_IN = v;
    }

    public long getBytesOut() {
        return PTR_BYTES_OUT;
    }

    public void setBytesOut(final long v) {
        PTR_BYTES_OUT = v;
    }

    public long getDurationIn() {
        return PTR_DURATION_IN;
    }

    public void setDurationIn(final long v) {
        PTR_DURATION_IN = v;
    }

    public long getDurationOut() {
        return PTR_DURATION_OUT;
    }

    public void setDurationOut(final long v) {
        PTR_DURATION_OUT = v;
    }

    /** Thread-safe in-place accumulation used by the in-memory buffer. */
    public synchronized void accumulate(final int connections, final long bytesIn, final long bytesOut,
            final long durationIn, final long durationOut) {
        PTR_CONNECTIONS += connections;
        PTR_BYTES_IN += bytesIn;
        PTR_BYTES_OUT += bytesOut;
        PTR_DURATION_IN += durationIn;
        PTR_DURATION_OUT += durationOut;
    }
}
