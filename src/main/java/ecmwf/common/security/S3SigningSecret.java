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

package ecmwf.common.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public final class S3SigningSecret {
    private static final String PURPOSE = "incoming-user-s3-signing";

    private S3SigningSecret() {
    }

    public static String encrypt(final String user, final String secret) throws IOException, GeneralSecurityException {
        if (!LocalPassword.isSet(secret) || secret.length() > 128) {
            throw new IllegalArgumentException("S3 secret must be between 1 and 128 characters");
        }
        return DatabaseEncryption.encrypt(PURPOSE, user, secret.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decrypt(final String user, final String stored) throws IOException, GeneralSecurityException {
        if (stored != null && stored.startsWith("dbenc:")) {
            return DatabaseEncryption.decrypt(PURPOSE, user, stored);
        }
        return DatabaseEncryption.decryptLegacyS3(user, stored);
    }
}
