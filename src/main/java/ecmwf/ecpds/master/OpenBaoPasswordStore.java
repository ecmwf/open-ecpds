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

package ecmwf.ecpds.master;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.technical.Cnf;

/**
 * Master-side KV v2 storage. Each rotation has a separate, create-only key. The caller must persist the reference
 * before staging, and separately authorise and track which reference is active for each Host.
 */
final class OpenBaoPasswordStore implements AutoCloseable {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int PASSWORD_MAX_LENGTH = 4096;
    private static final int MAX_TOKEN_BYTES = 4096;
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final URI mountUri;
    private final TokenSource tokenSource;
    private final HttpClient client;

    @FunctionalInterface
    interface TokenSource {
        String read() throws IOException;
    }

    static OpenBaoPasswordStore configured() {
        if (!Cnf.at("OpenBao", "enabled", false)) {
            return null;
        }
        try {
            final var path = Path.of(Cnf.at("OpenBao", "tokenFile", ""));
            if (!path.isAbsolute()) {
                throw new IllegalArgumentException();
            }
            return new OpenBaoPasswordStore(URI.create(Cnf.at("OpenBao", "address", "")),
                    Cnf.at("OpenBao", "mount", "ecpds"), () -> readTokenFile(path));
        } catch (final IllegalArgumentException _) {
            throw new IllegalArgumentException("Invalid OpenBao address, mount or absolute token file path");
        }
    }

    static String readTokenFile(final Path path) throws IOException {
        try (var input = Files.newInputStream(path)) {
            final var bytes = input.readNBytes(MAX_TOKEN_BYTES + 1);
            try {
                if (bytes.length > MAX_TOKEN_BYTES) {
                    throw new IOException("OpenBao authentication unavailable");
                }
                return new String(bytes, StandardCharsets.US_ASCII).stripTrailing();
            } finally {
                Arrays.fill(bytes, (byte) 0);
            }
        }
    }

    OpenBaoPasswordStore(final URI origin, final String mount, final TokenSource tokenSource) {
        if (!"https".equalsIgnoreCase(origin.getScheme()) || origin.getHost() == null || origin.getRawUserInfo() != null
                || origin.getRawQuery() != null || origin.getRawFragment() != null
                || !(origin.getRawPath().isEmpty() || "/".equals(origin.getRawPath()))
                || !mount.matches("[A-Za-z0-9][A-Za-z0-9_-]*")) {
            throw new IllegalArgumentException("Expected an HTTPS OpenBao origin and a single KV mount name");
        }
        this.tokenSource = Objects.requireNonNull(tokenSource);
        mountUri = origin.resolve("/v1/" + mount + "/");
        client = HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NEVER)
                .proxy(ProxySelector.of(null)).build();
    }

    void stage(final UUID reference, final String password) throws IOException {
        validatePassword(password);
        final var payload = JSON.createObjectNode();
        payload.putObject("options").put("cas", 0);
        payload.putObject("data").put("password", password);
        final var reply = send("stage", "POST", "data/" + key(reference), payload, 200);
        if (!isInitialVersion(reply.path("version"))) {
            throw invalidResponse("stage");
        }
    }

    static void validatePassword(final String password) {
        if (password == null || password.isEmpty() || password.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid Host password length");
        }
    }

    String read(final UUID reference) throws IOException {
        final var reply = send("read", "GET", "data/" + key(reference) + "?version=1", null, 200);
        final var metadata = reply.path("metadata");
        final var destroyed = metadata.path("destroyed");
        final var deletionTime = metadata.path("deletion_time");
        final var password = reply.path("data").path("password");
        if (!isInitialVersion(metadata.path("version")) || !destroyed.isBoolean() || destroyed.booleanValue()
                || !deletionTime.isTextual() || !deletionTime.textValue().isEmpty() || !password.isTextual()
                || password.textValue().isEmpty() || password.textValue().length() > PASSWORD_MAX_LENGTH) {
            throw invalidResponse("read");
        }
        return password.textValue();
    }

    void destroy(final UUID reference) throws IOException {
        final var payload = JSON.createObjectNode();
        payload.putArray("versions").add(1);
        send("destroy", "PUT", "destroy/" + key(reference), payload, 204);
        final var metadata = send("verify destruction", "GET", "metadata/" + key(reference), null, 200);
        final var destroyed = metadata.path("versions").path("1").path("destroyed");
        if (!isInitialVersion(metadata.path("current_version")) || !destroyed.isBoolean()
                || !destroyed.booleanValue()) {
            throw invalidResponse("verify destruction");
        }
    }

    private static String key(final UUID reference) {
        return "host-passwords/" + Objects.requireNonNull(reference);
    }

    private static boolean isInitialVersion(final JsonNode version) {
        return version.isIntegralNumber() && version.canConvertToInt() && version.intValue() == 1;
    }

    private JsonNode send(final String operation, final String method, final String path, final JsonNode payload,
            final int expectedStatus) throws IOException {
        final String token;
        try {
            token = tokenSource.read();
        } catch (final IOException | RuntimeException _) {
            throw new IOException("OpenBao authentication unavailable");
        }
        if (token == null || token.isEmpty() || !token.chars().allMatch(c -> c >= 33 && c <= 126)) {
            throw new IOException("OpenBao authentication unavailable");
        }
        final var request = HttpRequest.newBuilder(mountUri.resolve(path)).timeout(TIMEOUT).header("X-Vault-Token",
                token);
        final var requestBytes = payload == null ? null : JSON.writeValueAsBytes(payload);
        try {
            request.method(method, requestBytes == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofByteArray(requestBytes));
            if (requestBytes != null) {
                request.header("Content-Type", "application/json");
            }
            final HttpResponse<byte[]> response;
            final var pending = client.sendAsync(request.build(),
                    HttpResponse.BodyHandlers.limiting(HttpResponse.BodyHandlers.ofByteArray(), MAX_RESPONSE_BYTES));
            try {
                response = pending.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            } catch (final InterruptedException _) {
                pending.cancel(true);
                Thread.currentThread().interrupt();
                throw new IOException("OpenBao " + operation + " interrupted");
            } catch (final TimeoutException _) {
                pending.cancel(true);
                throw new IOException("OpenBao " + operation + " timed out");
            } catch (final ExecutionException _) {
                throw new IOException("OpenBao " + operation + " request failed");
            }
            final var body = response.body();
            try {
                if (response.statusCode() != expectedStatus) {
                    throw new IOException("OpenBao " + operation + " rejected (HTTP " + response.statusCode() + ")");
                }
                if (expectedStatus == 204) {
                    return null;
                }
                try {
                    final var json = JSON.readTree(body);
                    if (json != null && json.path("data").isObject()) {
                        return json.path("data");
                    }
                } catch (final IOException _) {
                    // JSON parser diagnostics may include the response, which can contain a password.
                }
                throw invalidResponse(operation);
            } finally {
                Arrays.fill(body, (byte) 0);
            }
        } finally {
            if (requestBytes != null) {
                Arrays.fill(requestBytes, (byte) 0);
            }
        }
    }

    private static IOException invalidResponse(final String operation) {
        return new IOException("Invalid OpenBao " + operation + " response");
    }

    @Override
    public void close() {
        client.shutdownNow();
    }
}
