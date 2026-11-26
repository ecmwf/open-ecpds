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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Common bounded write/cleanup probe for modules with an isolated, encrypted credential connection. */
public abstract class FileCredentialRotation<M extends TransferModule> implements PasswordRotation {
    public static final String STANDARD_METHOD = "$msuser[login]:$msuser[passwd]@$msuser[host]/$msuser[dir]$target";
    private static final ObjectMapper JSON = new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private final String namespace;
    private final Supplier<M> factory;
    private final Set<String> methods;
    private final Set<String> hostOptions;
    private final Set<String> endpointOptions;

    protected FileCredentialRotation(final String namespace, final Supplier<M> factory, final Set<String> methods,
            final Set<String> hostOptions, final Set<String> endpointOptions) {
        this.namespace = namespace;
        this.factory = factory;
        this.methods = Set.copyOf(methods);
        this.hostOptions = Set.copyOf(hostOptions);
        this.endpointOptions = Set.copyOf(endpointOptions);
    }

    public record ResourceEndpoint(String host, int port, String login, String probeDirectory,
            Map<String, String> options) implements PasswordRotation.Endpoint {
        public ResourceEndpoint {
            if (host == null || !host.matches("[A-Za-z0-9.:-]{1,253}") || port < 1 || port > 65535 || login == null
                    || login.isBlank() || login.length() > 255 || login.indexOf(':') >= 0 || probeDirectory == null
                    || !probeDirectory.matches("(?:/[A-Za-z0-9_.-]+)+")
                    || probeDirectory.matches(".*/\\.{1,2}(?:/.*)?")) {
                throw new IllegalArgumentException("Invalid approved credential endpoint");
            }
            options = Map.copyOf(options);
        }

        public String option(final String name) {
            final var value = options.get(name);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Missing approved endpoint option: " + name);
            }
            return value;
        }

        public URI origin() {
            try {
                return new URI("https", null, host, port, "/", null, null);
            } catch (final Exception failure) {
                throw new IllegalArgumentException("Invalid approved TLS origin");
            }
        }

        public boolean matchesOrigin(final String scheme, final String actualHost, final int actualPort) {
            return "https".equalsIgnoreCase(scheme) && actualHost != null
                    && host.equalsIgnoreCase(actualHost.replace("[", "").replace("]", ""))
                    && port == (actualPort < 0 ? 443 : actualPort);
        }

        @Override
        public String fingerprint() {
            try {
                return HexFormat.of()
                        .formatHex(MessageDigest.getInstance("SHA-256")
                                .digest(JSON.writeValueAsBytes(new TreeMap<>(Map.of("host", host, "port", port, "login",
                                        login, "probeDirectory", probeDirectory, "options", new TreeMap<>(options))))));
            } catch (final Exception failure) {
                throw new IllegalStateException("Cannot fingerprint approved endpoint");
            }
        }
    }

    @Override
    public final ResourceEndpoint parseEndpoint(final String configuration) {
        try {
            final var object = object(configuration);
            final var options = new HashMap<String, String>();
            final var fields = object.fieldNames();
            while (fields.hasNext()) {
                final var name = fields.next();
                if (!Set.of("host", "port", "login", "probeDirectory").contains(name)) {
                    if (!endpointOptions.contains(name) || !object.get(name).isTextual()) {
                        throw new IllegalArgumentException();
                    }
                    options.put(name, object.get(name).textValue());
                }
            }
            if (!object.path("port").isIntegralNumber() || !object.path("port").canConvertToInt()) {
                throw new IllegalArgumentException();
            }
            final var endpoint = new ResourceEndpoint(text(object, "host"), object.get("port").intValue(),
                    text(object, "login"), text(object, "probeDirectory"), options);
            validate(endpoint);
            return endpoint;
        } catch (final Exception failure) {
            throw new IllegalArgumentException("Invalid approved credential endpoint");
        }
    }

    protected void validate(final ResourceEndpoint endpoint) {
    }

    @Override
    public final String snapshotHostData(final String hostData) {
        try {
            return "# Host configuration SHA256: " + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((hostData == null ? "" : hostData).getBytes(StandardCharsets.UTF_8)));
        } catch (final Exception failure) {
            throw new IllegalStateException("Cannot fingerprint Host configuration");
        }
    }

    @Override
    public final boolean supports(final String method, final String hostData, final int port) {
        if (!methods.contains(method) || port < 1 || port > 65535) {
            return false;
        }
        if (hostData == null || hostData.isBlank()) {
            return true;
        }
        final var configuration = new ECtransSetup(namespace, hostData);
        if (!configuration.getScript().isBlank()) {
            return false;
        }
        return hostOptions.containsAll(configuration.getConfiguredParameterNames());
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void connect(final TransferModule module, final PasswordRotation.Endpoint endpoint,
            final String directory, final ECtransSetup ignored, final String credential) throws IOException {
        if (!(endpoint instanceof ResourceEndpoint approved) || credential == null || credential.isEmpty()
                || credential.length() > 4096) {
            throw new IOException("Invalid vault-backed credential");
        }
        try {
            module.setDebug(false);
            connect((M) module, approved, directory, credential);
        } catch (final Exception failure) {
            throw new IOException("Encrypted credential connection failed");
        }
    }

    protected abstract void connect(M module, ResourceEndpoint endpoint, String directory, String credential)
            throws Exception;

    @Override
    public final Result probe(final PasswordRotation.Endpoint endpoint, final String credential) {
        if (!(endpoint instanceof ResourceEndpoint approved) || credential == null || credential.isEmpty()
                || credential.length() > 4096) {
            return Result.CONNECTION_OR_AUTH_FAILED;
        }
        final var module = factory.get();
        final var path = approved.probeDirectory() + "/ecpds-probe-" + UUID.randomUUID() + UUID.randomUUID();
        final var bytes = "OpenECPDS credential probe\n".getBytes(StandardCharsets.US_ASCII);
        var result = Result.CONNECTION_OR_AUTH_FAILED;
        boolean attempted = false;
        try {
            connect(module, approved, "/", credential);
            result = Result.WRITE_FAILED;
            attempted = true;
            if (!module.put(new ByteArrayInputStream(bytes), path, 0, bytes.length)) {
                try (final var output = module.put(path, 0, bytes.length)) {
                    output.write(bytes);
                }
            }
            module.check(bytes.length, null, false);
            if (module.size(path) == bytes.length) {
                result = Result.PASSED;
            }
        } catch (final Exception failure) {
            // Return only bounded outcomes; provider exceptions may contain credentials.
        } finally {
            if (attempted) {
                try {
                    module.del(path);
                } catch (final Exception failure) {
                    result = Result.CLEANUP_FAILED;
                }
            }
            try {
                module.close();
                if (!module.isSessionClosureConfirmed()) {
                    result = Result.CLEANUP_FAILED;
                }
            } catch (final Exception failure) {
                result = Result.CLEANUP_FAILED;
            }
        }
        return result;
    }

    protected final ECtransSetup setup() {
        return new ECtransSetup(namespace);
    }

    protected static JsonNode object(final String credential) throws IOException {
        if (credential == null || credential.length() > 4096) {
            throw new IOException("Invalid credential document");
        }
        final var object = JSON.readTree(credential);
        if (object == null || !object.isObject()) {
            throw new IOException("Expected a credential document");
        }
        return object;
    }

    protected static String text(final JsonNode object, final String name) throws IOException {
        final var value = object.get(name);
        if (value == null || !value.isTextual() || value.textValue().isBlank()) {
            throw new IOException("Missing credential field: " + name);
        }
        return value.textValue();
    }

}
