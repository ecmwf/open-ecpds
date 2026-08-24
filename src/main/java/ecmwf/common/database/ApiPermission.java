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

import java.util.Objects;

public class ApiPermission extends DataBaseObject {
    private static final long serialVersionUID = 1L;

    protected String APU_ID;
    protected String APP_PATTERN;

    public ApiPermission() {
    }

    public ApiPermission(final String clientId, final String pattern) {
        APU_ID = clientId;
        APP_PATTERN = pattern;
    }

    public String getClientId() {
        return APU_ID;
    }

    public void setClientId(final String v) {
        APU_ID = v;
    }

    public String getPattern() {
        return APP_PATTERN;
    }

    public void setPattern(final String v) {
        APP_PATTERN = v;
    }

    @Override
    public int hashCode() {
        return Objects.hash(APU_ID, APP_PATTERN);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ApiPermission o = (ApiPermission) obj;
        return Objects.equals(APU_ID, o.APU_ID) && Objects.equals(APP_PATTERN, o.APP_PATTERN);
    }
}
