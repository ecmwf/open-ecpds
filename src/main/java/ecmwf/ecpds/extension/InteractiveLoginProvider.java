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
import java.net.URI;

/**
 * Browser login protocol adapter. The core owns state, PKCE secrets and local account mapping. Implementations must be
 * thread-safe and must not return an identity until protocol validation succeeds.
 */
public interface InteractiveLoginProvider extends Extension {
    String displayName();

    URI authorizationUri(String state, String nonce, String codeChallenge) throws IOException;

    ExternalIdentity authenticate(String code, String nonce, String codeVerifier) throws IOException;

    record ExternalIdentity(String issuer, String subject) {
        public ExternalIdentity {
            if (issuer == null || issuer.isBlank() || subject == null || subject.isBlank()) {
                throw new IllegalArgumentException("External identity requires issuer and subject");
            }
        }
    }
}
