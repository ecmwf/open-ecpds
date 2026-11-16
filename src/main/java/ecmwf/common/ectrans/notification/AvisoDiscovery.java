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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.text.Format;

/** Bounded inventory plus Aviso replay; only the receiving Master acknowledges the resulting queue entries. */
public final class AvisoDiscovery {
    public static final String CHECKPOINT = "aviso-checkpoint:";
    public static final String WARNING = "aviso-warning:";
    private static final ObjectMapper JSON = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final int MAX_RESPONSE = 32 * 1024 * 1024;
    private static final int MAX_FRAME = 1024 * 1024;

    @FunctionalInterface
    public interface Transport {
        Response request(URI uri, String body) throws IOException;
    }

    @FunctionalInterface
    public interface Output {
        void add(String line) throws IOException;
    }

    public record Response(int status, InputStream input, Closeable connection) implements Closeable {
        @Override
        public void close() throws IOException {
            connection.close();
        }
    }

    public record Config(URI base, String destination, String source, String directory, long after, long inventoryAt,
            long reconcileMillis, int maxEvents, int maxFiles, int maxDirectories, String pattern) {
        public Config {
            if (base == null || !base.isAbsolute() || base.getHost() == null || base.getUserInfo() != null
                    || base.getQuery() != null || base.getFragment() != null || !base.getPath().endsWith("/")
                    || !("https".equals(base.getScheme()) || "http".equals(base.getScheme())) || destination == null
                    || destination.isBlank() || destination.length() > 32 || source == null || source.isBlank()
                    || after < -1 || inventoryAt < 0 || reconcileMillis <= 0 || maxEvents <= 0 || maxFiles <= 0
                    || maxDirectories <= 0) {
                throw new IllegalArgumentException("Invalid Aviso acquisition configuration");
            }
            directory = directory == null ? "" : directory.replaceAll("^/+|/+$", "");
            if (!directory.isEmpty())
                path(directory);
            if (pattern != null && !pattern.isEmpty())
                Pattern.compile(pattern);
        }
    }

    private final Config config;
    private final Transport transport;
    private final Output output;
    private long cursor;
    private long inventoryAt;

    public AvisoDiscovery(final Config config, final Transport transport, final Output output) {
        this.config = config;
        this.transport = transport;
        this.output = output;
        cursor = config.after();
        inventoryAt = config.inventoryAt();
    }

    public void discover() throws IOException {
        final var high = sequence(
                json(config.base().resolve("api/v1/cursor/" + encode(config.destination())), null).path("sequence"));
        if (cursor > high)
            throw new IOException(
                    "Aviso source sequence moved backwards; use a new avisoSourceId after a source reset");
        if (cursor == -1 || System.currentTimeMillis() - inventoryAt >= config.reconcileMillis())
            inventory(high);
        if (!replay()) {
            output.add(WARNING + "Aviso history expired; reconciling the current inventory (not historical versions)");
            final var watermark = sequence(
                    json(config.base().resolve("api/v1/cursor/" + encode(config.destination())), null)
                            .path("sequence"));
            if (watermark < cursor)
                throw new IOException("Aviso source sequence moved backwards during reconciliation");
            inventory(watermark);
            if (!replay())
                throw new IOException("Aviso history expired during inventory; increase source history retention");
        }
        output.add(CHECKPOINT + cursor + ":" + inventoryAt);
    }

    private void inventory(final long watermark) throws IOException {
        final var started = System.currentTimeMillis();
        final var directories = new ArrayDeque<String>();
        final var visited = new HashSet<String>();
        directories.add("");
        var files = 0;
        while (!directories.isEmpty()) {
            final var directory = directories.remove();
            if (!visited.add(directory) || visited.size() > config.maxDirectories())
                throw new IOException("Aviso inventory directory limit exceeded");
            final var entries = json(data("list", directory), null);
            if (!entries.isArray())
                throw new IOException("Aviso inventory is not a JSON array");
            for (final var entry : entries) {
                final var name = text(entry.path("name"));
                if (name.contains("/") || name.contains("\\"))
                    throw new IOException("Invalid inventory basename");
                final var target = directory + name;
                path(target);
                if (!entry.path("directory").isBoolean())
                    throw new IOException("Invalid inventory entry type");
                if (entry.path("directory").booleanValue()) {
                    if (directories.size() + visited.size() >= config.maxDirectories())
                        throw new IOException("Aviso inventory directory limit exceeded");
                    directories.add(target + "/");
                } else {
                    if (!config.destination().equals(text(entry.path("destination"))))
                        throw new IOException("Inventory root does not match avisoDestination");
                    if (++files > config.maxFiles())
                        throw new IOException("Aviso inventory file limit exceeded");
                    file(text(entry.path("target")), target, text(entry.path("etag")), sequence(entry.path("size")),
                            text(entry.path("time")), "inventory");
                }
            }
        }
        cursor = Math.max(cursor, watermark);
        inventoryAt = started;
    }

    /** False means the HTTP 410 response must be recovered by inventory, not by jumping the cursor. */
    private boolean replay() throws IOException {
        if (cursor == Long.MAX_VALUE)
            throw new IOException("Aviso sequence exhausted");
        final var request = JSON.createObjectNode();
        request.putObject("identifier").put("destination", config.destination());
        request.put("event_type", "file_available");
        request.put("from_id", Long.toString(cursor + 1));
        try (final var response = transport.request(config.base().resolve("api/v1/replay"), request.toString())) {
            if (response.status() == 410)
                return false;
            requireOk(response);
            final var reader = new BufferedReader(new InputStreamReader(response.input(), StandardCharsets.UTF_8));
            final var data = new StringBuilder();
            var event = "";
            var received = 0;
            String line;
            while ((line = limitedLine(reader)) != null) {
                if (line.isEmpty()) {
                    if (!data.isEmpty()) {
                        final var frame = JSON.readTree(data.toString());
                        if ("replay".equals(event)) {
                            final var id = text(frame.path("id"));
                            if (!id.startsWith("file_available@"))
                                throw new IOException("Invalid Aviso event ID");
                            final long next;
                            try {
                                next = Long.parseLong(id.substring("file_available@".length()));
                            } catch (final NumberFormatException e) {
                                throw new IOException("Invalid Aviso event sequence", e);
                            }
                            if (next <= cursor)
                                throw new IOException("Non-increasing Aviso event sequence");
                            final var identifier = frame.path("data").path("identifier");
                            if (!config.destination().equals(text(identifier.path("destination"))))
                                throw new IOException("Unexpected Aviso destination");
                            final var payload = frame.path("data").path("payload");
                            final var target = text(identifier.path("target"));
                            file(target, payload.path("portalPath").asText(target), text(payload.path("etag")),
                                    sequence(payload.path("size")), text(frame.path("time")), id);
                            cursor = next;
                            if (++received >= config.maxEvents())
                                return true;
                        } else if ("replay-control".equals(event)
                                && "replay_completed".equals(frame.path("type").asText())) {
                            final var last = sequence(frame.path("last_sequence"));
                            if (last < cursor)
                                throw new IOException("Aviso replay cursor moved backwards");
                            cursor = last;
                            return true;
                        } else if ("error".equals(event) || "connection-closing".equals(event)) {
                            throw new IOException("Aviso replay did not complete: " + frame);
                        } else if (!"replay-control".equals(event) && !"heartbeat".equals(event)) {
                            throw new IOException("Unexpected Aviso SSE event: " + event);
                        }
                    }
                    event = "";
                    data.setLength(0);
                } else if (line.startsWith("event:")) {
                    event = line.substring(6).trim();
                } else if (line.startsWith("data:")) {
                    if (data.length() + line.length() > MAX_FRAME)
                        throw new IOException("Aviso SSE frame exceeds size limit");
                    if (!data.isEmpty())
                        data.append('\n');
                    data.append(line.substring(5).stripLeading());
                }
            }
            throw new IOException("Aviso replay ended without a completion frame");
        }
    }

    private void file(String target, String portalPath, final String etag, final long size, final String time,
            final String messageId) throws IOException {
        target = target.replaceAll("^/+", "");
        portalPath = portalPath.replaceAll("^/+", "");
        path(target);
        path(portalPath);
        try {
            if (!UUID.fromString(etag).toString().equals(etag))
                throw new IllegalArgumentException("Non-canonical ETag");
            final var instant = Instant.parse(time);
            if (config.pattern() != null && !config.pattern().isEmpty() && !target.matches(config.pattern()))
                return;
            final var notification = new AcquisitionNotification("aviso", messageId,
                    JSON.writeValueAsString(new String[] { config.source(), config.destination(), target, etag }),
                    instant.toString(), config.destination(), config.source(), "canonical", null);
            final var url = data("file", portalPath) + "?etag=" + etag;
            // Both the URL and the output path survive FTP listing parsing, including spaces in nested targets.
            final var line = Format.getFtpList("lr--r--r--", "aviso", "aviso", Long.toString(size),
                    instant.toEpochMilli(), url + " -> " + target);
            output.add(AcquisitionListing.format(line, null, null, notification));
        } catch (final IllegalArgumentException e) {
            throw new IOException("Invalid versioned Aviso file metadata", e);
        }
    }

    private URI data(final String operation, final String target) {
        final var name = (config.directory().isEmpty() ? "" : config.directory() + "/") + target;
        return config.base().resolve("../data/" + operation + "/" + encodePath(name));
    }

    private JsonNode json(final URI uri, final String body) throws IOException {
        try (final var response = transport.request(uri, body)) {
            requireOk(response);
            final var bytes = response.input().readNBytes(MAX_RESPONSE + 1);
            if (bytes.length > MAX_RESPONSE)
                throw new IOException("Aviso JSON response exceeds 32 MiB");
            final var node = JSON.readTree(bytes);
            if (node == null)
                throw new IOException("Empty Aviso JSON response");
            return node;
        }
    }

    private static void requireOk(final Response response) throws IOException {
        if (response.status() != 200 || response.input() == null)
            throw new IOException("Aviso HTTP request failed: " + response.status());
    }

    private static long sequence(final JsonNode node) throws IOException {
        if (!node.isIntegralNumber() || !node.canConvertToLong() || node.longValue() < 0)
            throw new IOException("Invalid Aviso sequence or size");
        return node.longValue();
    }

    private static String text(final JsonNode node) throws IOException {
        if (!node.isTextual() || node.textValue().isBlank())
            throw new IOException("Missing Aviso text field");
        return node.textValue();
    }

    private static void path(final String path) {
        if (path.isBlank() || !path.equals(path.strip()) || path.startsWith("/") || path.endsWith("/")
                || path.contains("\\") || path.contains(" -> ") || path.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid Aviso relative path");
        for (final var segment : path.split("/", -1))
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment))
                throw new IllegalArgumentException("Invalid Aviso path segment");
    }

    private static String encode(final String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String encodePath(final String path) {
        return String.join("/", java.util.Arrays.stream(path.split("/", -1)).map(AvisoDiscovery::encode).toList());
    }

    private static String limitedLine(final BufferedReader reader) throws IOException {
        final var line = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '\n')
                return line.toString();
            if (c != '\r')
                line.append((char) c);
            if (line.length() > MAX_FRAME)
                throw new IOException("Aviso SSE line exceeds size limit");
        }
        return line.isEmpty() ? null : line.toString();
    }
}
