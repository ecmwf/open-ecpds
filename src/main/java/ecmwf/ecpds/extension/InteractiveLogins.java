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
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.extension.InteractiveLoginProvider.ExternalIdentity;

/**
 * Bounded, single-use login transactions owned by the Master. No tokens or identities come from the browser.
 */
public final class InteractiveLogins {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long LIFETIME = Duration.ofMinutes(5).toMillis();
    private static final int CAPACITY = 1000;

    private final InteractiveLoginProvider provider;
    private final Map<ExternalIdentity, String> users = new HashMap<>();
    private final Map<String, Attempt> pending = new HashMap<>();
    private final Clock clock;

    public record Challenge(String state, String authorizationUri) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    private record Attempt(String binding, String origin, String nonce, String verifier, long expires) {
    }

    public InteractiveLogins(final InteractiveLoginProvider provider, final Map<String, String> mappings)
            throws IOException {
        this(provider, mappings, Clock.systemUTC());
    }

    InteractiveLogins(final InteractiveLoginProvider provider, final Map<String, String> mappings, final Clock clock)
            throws IOException {
        this.provider = provider;
        this.clock = clock;
        final var json = new ObjectMapper();
        for (final var entry : mappings.entrySet()) {
            try {
                final var identity = json.readValue(entry.getValue(), ExternalIdentity.class);
                if (entry.getKey().isBlank() || identity == null || users.put(identity, entry.getKey()) != null) {
                    throw new IOException("Invalid or duplicate interactive login identity mapping");
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid interactive login identity mapping", e);
            }
        }
    }

    public String displayName() {
        return provider == null ? null : provider.displayName();
    }

    public Challenge begin(final String binding, final String origin) throws IOException {
        if (provider == null || binding == null || !binding.matches("[A-Za-z0-9_-]{43,128}")) {
            throw new IOException("Interactive login unavailable");
        }
        final var state = random();
        final var attempt = new Attempt(binding, origin, random(), random(), clock.millis() + LIFETIME);
        synchronized (pending) {
            pending.values().removeIf(value -> value.expires() <= clock.millis());
            if (pending.size() >= CAPACITY) {
                throw new IOException("Too many pending interactive logins");
            }
            pending.put(state, attempt);
        }
        try {
            final var uri = provider.authorizationUri(state, attempt.nonce(), challenge(attempt.verifier()));
            requireHttps(uri);
            return new Challenge(state, uri.toASCIIString());
        } catch (IOException | RuntimeException e) {
            synchronized (pending) {
                pending.remove(state);
            }
            throw new IOException("Cannot start interactive login");
        }
    }

    public String complete(final String state, final String binding, final String origin, final String code)
            throws IOException {
        final Attempt attempt;
        synchronized (pending) {
            attempt = pending.get(state);
            if (attempt == null || attempt.expires() <= clock.millis()
                    || !java.util.Objects.equals(attempt.origin(), origin) || binding == null
                    || !MessageDigest.isEqual(attempt.binding().getBytes(StandardCharsets.UTF_8),
                            binding.getBytes(StandardCharsets.UTF_8))) {
                throw new IOException("Invalid or expired interactive login");
            }
            pending.remove(state);
        }
        if (code == null || code.isBlank() || code.length() > 8192) {
            throw new IOException("Invalid interactive login response");
        }
        final var identity = provider.authenticate(code, attempt.nonce(), attempt.verifier());
        final var user = users.get(identity);
        if (user == null) {
            throw new IOException("External identity is not mapped to a local account");
        }
        return user;
    }

    public void clear() {
        synchronized (pending) {
            pending.clear();
        }
    }

    public static String random() {
        final var bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String challenge(final String verifier) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void requireHttps(final URI uri) throws IOException {
        if (uri == null || !"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getFragment() != null) {
            throw new IOException("Interactive login requires an absolute HTTPS URL without user info or fragment");
        }
    }
}
