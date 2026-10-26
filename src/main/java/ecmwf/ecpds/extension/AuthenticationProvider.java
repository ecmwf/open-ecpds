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

package ecmwf.ecpds.extension;

import java.io.IOException;

import ecmwf.common.database.ECUser;

/**
 * Credential validation for Master WebUsers, not authorization or Data Portal authentication. Implementations must
 * support concurrent calls, bound network waits and never log credentials.
 */
public interface AuthenticationProvider extends Extension {
    enum Result {
        ACCEPTED, REJECTED, UNSUPPORTED
    }

    /**
     * Validate the requested user without remapping its identity. An outage must throw IOException. Neither rejection
     * nor an unsupported request permits fallback to another credential store.
     */
    Result authenticate(String user, String credentials, String origin) throws IOException;

    /**
     * Optionally return fresh directory data with a successful credential check, without a second authentication. The
     * core validates the identity and persists updates; this does not grant WebUser access.
     */
    default Authentication authenticateIdentity(final String user, final String credentials, final String origin)
            throws IOException {
        final var result = authenticate(user, credentials, origin);
        if (result == null) {
            throw new IOException("Authentication extension returned no result");
        }
        return new Authentication(result, null);
    }

    record Authentication(Result result, ECUser user) {
        public Authentication {
            if (result == null || user != null && result != Result.ACCEPTED) {
                throw new IllegalArgumentException("Invalid authentication result");
            }
        }
    }
}
