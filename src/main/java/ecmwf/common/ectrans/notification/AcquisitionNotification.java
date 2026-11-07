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

package ecmwf.common.ectrans.notification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Protocol-neutral attribution and expected integrity, independent of the notification's retrieval URL.
 */
public record AcquisitionNotification(String profile, String messageId, String identity, String publicationTime,
        String topic, String source, String relation, Integrity integrity) {

    private static final ObjectMapper JSON = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final int MAX_JSON_BYTES = 16384;
    private static final int CHUNK_SIZE = 250;
    private static final int MAX_CHUNKS = 88;
    private static final String CHUNKS_KEY = "notify.chunks";

    public AcquisitionNotification {
        text("profile", profile, true);
        if (!profile.matches("[a-z][a-z0-9_-]{0,63}"))
            throw new IllegalArgumentException("Invalid notification profile");
        text("message ID", messageId, false);
        text("identity", identity, true);
        text("publication time", publicationTime, true);
        final var published = Instant.parse(publicationTime);
        published.toEpochMilli();
        publicationTime = published.toString();
        text("topic", topic, true);
        text("source", source, false);
        text("relation", relation, false);
    }

    private static void text(final String field, final String value, final boolean required) {
        if (value == null) {
            if (required)
                throw new IllegalArgumentException("Missing notification " + field);
        } else if (value.isBlank() || value.length() > 4096 || value.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid notification " + field);
    }

    public long timeMillis() {
        return Instant.parse(publicationTime).toEpochMilli();
    }

    /** A complete version identity; neither mirror URLs nor broker message IDs affect deduplication. */
    public String uniqueName() throws IOException {
        try {
            final var version = JSON.writeValueAsBytes(new Object[] { profile, identity, publicationTime, integrity });
            return "notification:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(version));
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException("Cannot compute notification identity", e);
        }
    }

    public String encode() throws IOException {
        final var bytes = JSON.writeValueAsBytes(this);
        if (bytes.length > MAX_JSON_BYTES)
            throw new IOException("Notification metadata exceeds 16 KiB");
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static AcquisitionNotification decode(final String encoded) throws IOException {
        if (encoded == null || encoded.length() > MAX_CHUNKS * CHUNK_SIZE)
            throw new IOException("Invalid notification metadata length");
        try {
            final var bytes = Base64.getDecoder().decode(encoded);
            if (bytes.length > MAX_JSON_BYTES)
                throw new IOException("Notification metadata exceeds 16 KiB");
            final var notification = JSON.readValue(bytes, AcquisitionNotification.class);
            if (notification == null)
                throw new IOException("Missing acquisition notification metadata");
            return notification;
        } catch (final IllegalArgumentException e) {
            throw new IOException("Invalid notification metadata", e);
        }
    }

    /** Fit the existing metadata value columns without truncating identity or changing the schema. */
    public String metadata() throws IOException {
        final var encoded = encode();
        final var count = (encoded.length() + CHUNK_SIZE - 1) / CHUNK_SIZE;
        final var result = new StringBuilder(CHUNKS_KEY).append('=').append(count);
        for (var i = 0; i < count; i++)
            result.append(",notify.").append(i).append('=').append(encoded, i * CHUNK_SIZE,
                    Math.min((i + 1) * CHUNK_SIZE, encoded.length()));
        return result.toString();
    }

    public static AcquisitionNotification fromMetadata(final Map<String, String> metadata) throws IOException {
        final var countText = metadata.get(CHUNKS_KEY);
        if (countText == null) {
            if (metadata.keySet().stream().anyMatch(key -> key.matches("notify\\.\\d+")))
                throw new IOException("Missing notification metadata chunk count");
            return null;
        }
        try {
            final var count = Integer.parseInt(countText);
            if (count < 1 || count > MAX_CHUNKS)
                throw new IOException("Invalid notification metadata chunk count");
            final var encoded = new StringBuilder();
            for (var i = 0; i < count; i++) {
                final var chunk = metadata.get("notify." + i);
                if (chunk == null || chunk.isEmpty() || chunk.length() > CHUNK_SIZE)
                    throw new IOException("Missing or invalid notification metadata chunk");
                encoded.append(chunk);
            }
            return decode(encoded.toString());
        } catch (final NumberFormatException e) {
            throw new IOException("Invalid notification metadata chunk count", e);
        }
    }

    public Map<String, String> details() {
        final var fields = new LinkedHashMap<String, String>();
        fields.put("Profile", profile);
        fields.put("Message ID", messageId);
        fields.put("Data identity", identity);
        fields.put("Published (UTC)", publicationTime);
        fields.put("Topic", topic);
        fields.put("Source", source);
        fields.put("Link relation", relation);
        fields.put("Expected integrity",
                integrity == null ? "Not advertised" : integrity.method + ": " + integrity.value);
        fields.values().removeIf(value -> value == null);
        return fields;
    }

    public record Integrity(String method, String value) {
        public Integrity {
            if (method == null || value == null)
                throw new IllegalArgumentException("Incomplete notification integrity");
            method = switch (method.toLowerCase(Locale.ROOT).replace("-", "")) {
            case "sha256" -> "SHA-256";
            case "sha384" -> "SHA-384";
            case "sha512" -> "SHA-512";
            default -> throw new IllegalArgumentException("Unsupported notification integrity method");
            };
            final var digest = Base64.getDecoder().decode(value);
            final var length = switch (method) {
            case "SHA-256" -> 32;
            case "SHA-384" -> 48;
            default -> 64;
            };
            if (digest.length != length)
                throw new IllegalArgumentException("Invalid notification integrity digest length");
            value = Base64.getEncoder().encodeToString(digest);
        }

        public MessageDigest newDigest() throws IOException {
            try {
                return MessageDigest.getInstance(method);
            } catch (final NoSuchAlgorithmException e) {
                throw new IOException("Notification integrity method unavailable", e);
            }
        }

        public void verify(final byte[] digest) throws IOException {
            if (!MessageDigest.isEqual(Base64.getDecoder().decode(value), digest))
                throw new IOException("Notification integrity mismatch (" + method + ")");
        }

        public void verifyContent(final byte[] content) throws IOException {
            verify(newDigest().digest(content));
        }
    }
}
