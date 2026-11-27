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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Semaphore;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public final class LocalPassword {
    private static final String PREFIX = "$argon2id$v=19$m=19456,t=2,p=1$";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Semaphore WORKERS = new Semaphore(4);
    private static final Base64.Encoder ENCODER = Base64.getEncoder().withoutPadding();

    private LocalPassword() {
    }

    public static boolean isSet(final String value) {
        return value != null && !value.isEmpty();
    }

    public static String hash(final String password) {
        if (!isSet(password) || password.length() > 4096) {
            throw new IllegalArgumentException("Password must be between 1 and 4096 characters");
        }

        final var salt = new byte[16];
        RANDOM.nextBytes(salt);
        return PREFIX + ENCODER.encodeToString(salt) + "$" + ENCODER.encodeToString(derive(password, salt));
    }

    public static String generate() {
        final var bytes = new byte[18];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static boolean matches(final String hash, final String legacy, final String submitted) {
        if (!isSet(submitted) || submitted.length() > 4096) {
            return false;
        }
        if (hash == null) {
            return isSet(legacy) && equal(legacy, submitted);
        }
        if (!hash.startsWith(PREFIX) || hash.length() != PREFIX.length() + 66
                || hash.charAt(PREFIX.length() + 22) != '$') {
            return false;
        }
        try {
            final var decoder = Base64.getDecoder();
            final var salt = decoder.decode(hash.substring(PREFIX.length(), PREFIX.length() + 22));
            final var expected = decoder.decode(hash.substring(PREFIX.length() + 23));
            return MessageDigest.isEqual(expected, derive(submitted, salt));
        } catch (final IllegalArgumentException _) {
            return false;
        }
    }

    public static boolean equal(final String first, final String second) {
        return first != null && second != null && MessageDigest.isEqual(first.getBytes(StandardCharsets.UTF_8),
                second.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] derive(final String password, final byte[] salt) {
        WORKERS.acquireUninterruptibly();
        final var characters = password.toCharArray();
        try {
            final var parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id).withMemoryAsKB(19456)
                    .withIterations(2).withParallelism(1).withSalt(salt).build();
            final var generator = new Argon2BytesGenerator();
            generator.init(parameters);
            final var hash = new byte[32];
            generator.generateBytes(characters, hash);
            return hash;
        } finally {
            Arrays.fill(characters, '\0');
            WORKERS.release();
        }
    }
}
