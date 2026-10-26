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

package ecmwf.ecpds.extension.oidc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import ecmwf.ecpds.extension.ExtensionContext;
import ecmwf.ecpds.extension.InteractiveLoginProvider;
import ecmwf.ecpds.extension.InteractiveLogins;

/**
 * OIDC authorization-code login with S256 PKCE and RS256 ID tokens from a pinned issuer/JWKS endpoint.
 */
public class OidcLoginProvider implements InteractiveLoginProvider {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int RESPONSE_LIMIT = 524288;
    private String issuer;
    private String clientId;
    private String secret;
    private String clientAuthentication;
    private String label;
    private URI authorizationEndpoint;
    private URI tokenEndpoint;
    private URI redirectUri;
    private DefaultJWTProcessor<SecurityContext> processor;
    private AutoCloseable keys;

    @Override
    public int apiVersion() {
        return API_VERSION;
    }

    @Override
    public void start(final ExtensionContext context) throws IOException {
        issuer = required(context, "issuer");
        endpoint(issuer);
        authorizationEndpoint = endpoint(required(context, "authorizationEndpoint"));
        tokenEndpoint = endpoint(required(context, "tokenEndpoint"));
        redirectUri = endpoint(required(context, "redirectUri"));
        final var jwks = endpoint(required(context, "jwksUri"));
        clientId = required(context, "clientId");
        secret = context.option("clientSecret");
        clientAuthentication = context.option("clientAuthentication");
        if (clientAuthentication == null) {
            clientAuthentication = secret == null || secret.isBlank() ? "none" : "basic";
        }
        if (!Set.of("none", "basic", "post").contains(clientAuthentication)
                || !"none".equals(clientAuthentication) && (secret == null || secret.isBlank())) {
            throw new IOException("Invalid OIDC client authentication configuration");
        }
        label = context.option("displayName");
        if (label == null || label.isBlank()) {
            label = "Sign in with OpenID Connect";
        }
        final var source = JWKSourceBuilder
                .<SecurityContext> create(jwks.toURL(),
                        url -> new Resource(request(URI.create(url.toString()), null), "application/json"))
                .refreshAheadCache(false).build();
        if (source instanceof AutoCloseable closeable) {
            keys = closeable;
        }
        processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, source));
        final var verifier = new DefaultJWTClaimsVerifier<SecurityContext>(clientId,
                new JWTClaimsSet.Builder().issuer(issuer).build(), Set.of("iss", "sub", "aud", "exp", "iat", "nonce"));
        verifier.setMaxClockSkew(60);
        processor.setJWTClaimsSetVerifier(verifier);
    }

    @Override
    public String displayName() {
        return label;
    }

    @Override
    public URI authorizationUri(final String state, final String nonce, final String codeChallenge) {
        return URI.create(authorizationEndpoint + "?response_type=code&scope=openid&client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri.toString()) + "&state=" + encode(state) + "&nonce="
                + encode(nonce) + "&code_challenge_method=S256&code_challenge=" + encode(codeChallenge)
                + "&response_mode=query");
    }

    @Override
    public ExternalIdentity authenticate(final String code, final String nonce, final String codeVerifier)
            throws IOException {
        try {
            var form = "grant_type=authorization_code&code=" + encode(code) + "&redirect_uri="
                    + encode(redirectUri.toString()) + "&client_id=" + encode(clientId) + "&code_verifier="
                    + encode(codeVerifier);
            if ("post".equals(clientAuthentication)) {
                form += "&client_secret=" + encode(secret);
            }
            final var response = JSON.readTree(request(tokenEndpoint, form));
            if (response == null || response.has("error") || !response.path("id_token").isTextual()) {
                throw new IOException("OIDC token exchange failed");
            }
            return validate(response.path("id_token").textValue(), nonce);
        } catch (IOException | RuntimeException e) {
            // Provider responses and transport exceptions may contain codes, tokens or credentials.
            throw new IOException("OIDC authentication failed");
        }
    }

    private ExternalIdentity validate(final String token, final String nonce) throws IOException {
        try {
            final var claims = processor.process(token, null);
            final var azp = claims.getStringClaim("azp");
            final var issued = claims.getIssueTime();
            if (!nonce.equals(claims.getStringClaim("nonce")) || claims.getSubject().isBlank()
                    || claims.getSubject().length() > 255 || issued.toInstant().isAfter(Instant.now().plusSeconds(60))
                    || !claims.getExpirationTime().after(issued)
                    || (claims.getAudience().size() > 1 || azp != null) && !clientId.equals(azp)) {
                throw new IOException("Invalid OIDC identity claims");
            }
            return new ExternalIdentity(claims.getIssuer(), claims.getSubject());
        } catch (com.nimbusds.jose.JOSEException | com.nimbusds.jose.proc.BadJOSEException | ParseException
                | RuntimeException e) {
            throw new IOException("Invalid OIDC ID token");
        }
    }

    /**
     * Fixed HTTPS destinations, no redirects, bounded response size and socket waits.
     */
    protected String request(final URI uri, final String form) throws IOException {
        final var connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/json");
        try {
            if (form != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                if ("basic".equals(clientAuthentication)) {
                    connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                            (encode(clientId) + ":" + encode(secret)).getBytes(StandardCharsets.UTF_8)));
                }
                final var bytes = form.getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(bytes.length);
                try (final var out = connection.getOutputStream()) {
                    out.write(bytes);
                }
            }
            if (connection.getResponseCode() != 200) {
                throw new IOException("OIDC endpoint request failed");
            }
            try (final var in = connection.getInputStream()) {
                final var bytes = in.readNBytes(RESPONSE_LIMIT + 1);
                if (bytes.length > RESPONSE_LIMIT) {
                    throw new IOException("OIDC response too large");
                }
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String required(final ExtensionContext context, final String name) throws IOException {
        final var value = context.option(name);
        if (value == null || value.isBlank()) {
            throw new IOException("Missing OIDC option: " + name);
        }
        return value;
    }

    private static URI endpoint(final String value) throws IOException {
        try {
            final var uri = URI.create(value);
            InteractiveLogins.requireHttps(uri);
            if (uri.getQuery() != null) {
                throw new IOException("OIDC endpoints must not contain query parameters");
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid OIDC endpoint");
        }
    }

    @Override
    public void close() throws IOException {
        if (keys != null) {
            try {
                keys.close();
            } catch (Exception e) {
                throw new IOException("Cannot close OIDC keys");
            }
        }
    }
}
