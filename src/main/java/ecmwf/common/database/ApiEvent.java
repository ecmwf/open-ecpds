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
import java.util.Objects;

public class ApiEvent extends DataBaseObject {
    private static final long serialVersionUID = 1L;

    protected Long APE_ID;
    protected String APU_ID;
    protected String APE_SERVICE;
    protected String APE_HOST;
    protected BigDecimal APE_DATE;
    protected boolean APE_SUCCESS;
    protected String APE_MESSAGE;

    public ApiEvent() {
    }

    public Long getEventId() {
        return APE_ID;
    }

    public void setEventId(final Long v) {
        APE_ID = v;
    }

    public String getClientId() {
        return APU_ID;
    }

    public void setClientId(final String v) {
        APU_ID = v;
    }

    public String getService() {
        return APE_SERVICE;
    }

    public void setService(final String v) {
        APE_SERVICE = v;
    }

    public String getHost() {
        return APE_HOST;
    }

    public void setHost(final String v) {
        APE_HOST = v;
    }

    public java.sql.Timestamp getDate() {
        return bigDecimalToTimestamp(APE_DATE);
    }

    public void setDate(final java.sql.Timestamp v) {
        APE_DATE = timestampToBigDecimal(v);
    }

    public boolean getSuccess() {
        return APE_SUCCESS;
    }

    public void setSuccess(final boolean v) {
        APE_SUCCESS = v;
    }

    public String getMessage() {
        return APE_MESSAGE;
    }

    public void setMessage(final String v) {
        APE_MESSAGE = v;
    }

    @Override
    public int hashCode() {
        return Objects.hash(APE_ID);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(APE_ID, ((ApiEvent) obj).APE_ID);
    }
}
