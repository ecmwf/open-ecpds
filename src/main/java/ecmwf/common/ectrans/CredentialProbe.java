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
 */

package ecmwf.common.ectrans;

import java.io.IOException;

/** Opt-in transfer module capability for testing replacement credentials. */
public interface CredentialProbe<E extends CredentialProbe.Endpoint, C> {
    interface Endpoint {
        /** Stable, protocol-specific fingerprint of the approved endpoint, excluding credentials. */
        String fingerprint();
    }

    enum Result {
        PASSED, CONNECTION_OR_AUTH_FAILED, WRITE_FAILED, CLEANUP_FAILED
    }

    E parseEndpoint(String approvedConfiguration);

    /** Tests an operator-approved endpoint without modifying the module's live transfer state. */
    Result probe(E endpoint, C credential) throws IOException;
}
