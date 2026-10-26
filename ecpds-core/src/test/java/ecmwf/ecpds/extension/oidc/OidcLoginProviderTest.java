package ecmwf.ecpds.extension.oidc;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import ecmwf.ecpds.extension.ExtensionContext;
import ecmwf.ecpds.extension.InteractiveLogins;

public final class OidcLoginProviderTest {
    public static void main(final String[] args) throws Exception {
        final var key = new RSAKeyGenerator(2048).keyID("signing-key").generate();
        try (final var provider = new Provider(key)) {
            provider.start(new ExtensionContext("master", options()));
            final var authorization = provider.authorizationUri("state", "nonce", "challenge");
            final var query = query(authorization.getRawQuery());
            check("state".equals(query.get("state")) && "nonce".equals(query.get("nonce"))
                    && "S256".equals(query.get("code_challenge_method"))
                    && "challenge".equals(query.get("code_challenge")) && "code".equals(query.get("response_type"))
                    && "openid".equals(query.get("scope")), "Authorization request must bind nonce/state and S256");
            provider.token = token(key, builder -> {
            });
            final var identity = provider.authenticate("authorization-code", "nonce", "verifier");
            check("https://identity.example".equals(identity.issuer()) && "subject-1".equals(identity.subject()),
                    "Verified issuer and stable subject must be returned");
            final var form = query(provider.form);
            check("authorization-code".equals(form.get("code")) && "verifier".equals(form.get("code_verifier"))
                    && "https://monitor.example/do/interactiveCallback".equals(form.get("redirect_uri"))
                    && "client".equals(form.get("client_id")), "Code exchange must bind client, callback and PKCE");
            reject(provider, key, builder -> builder.issuer("https://wrong.example"));
            reject(provider, key, builder -> builder.audience("wrong"));
            reject(provider, key, builder -> builder.claim("nonce", "wrong"));
            reject(provider, key, builder -> builder.expirationTime(Date.from(Instant.now().minusSeconds(120))));
            reject(provider, key, builder -> builder.issueTime(Date.from(Instant.now().plusSeconds(120))));
            reject(provider, key, builder -> builder.claim("azp", "wrong"));
            reject(provider, key, builder -> builder.audience(List.of("client", "other")));
            reject(provider, key, builder -> builder.subject(""));
            provider.token = token(key, builder -> builder.audience(List.of("client", "other")).claim("azp", "client"));
            provider.authenticate("code", "nonce", "verifier");
            final var attacker = new RSAKeyGenerator(2048).keyID("signing-key").generate();
            provider.token = token(attacker, builder -> {
            });
            fail(() -> provider.authenticate("code", "nonce", "verifier"));
            provider.token = "not-a-token";
            fail(() -> provider.authenticate("code", "nonce", "verifier"));
        }
        final var insecure = new HashMap<>(options());
        insecure.put("tokenEndpoint", "http://identity.example/token");
        fail(() -> new Provider(key).start(new ExtensionContext("master", insecure)));
        System.out.println("PASS: OIDC code exchange, PKCE parameters, signature and ID-token claims");
    }

    private static Map<String, String> options() {
        return Map.of("issuer", "https://identity.example", "authorizationEndpoint",
                "https://identity.example/authorize", "tokenEndpoint", "https://identity.example/token", "jwksUri",
                "https://identity.example/keys", "clientId", "client", "redirectUri",
                "https://monitor.example/do/interactiveCallback");
    }

    private static String token(final RSAKey key, final Consumer<JWTClaimsSet.Builder> changes) throws Exception {
        final var now = Instant.now();
        final var builder = new JWTClaimsSet.Builder().issuer("https://identity.example").subject("subject-1")
                .audience("client").issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(300)))
                .claim("nonce", "nonce");
        changes.accept(builder);
        final var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("signing-key").build(),
                builder.build());
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    private static void reject(final Provider provider, final RSAKey key, final Consumer<JWTClaimsSet.Builder> change)
            throws Exception {
        provider.token = token(key, change);
        fail(() -> provider.authenticate("code", "nonce", "verifier"));
    }

    private static Map<String, String> query(final String value) {
        final var result = new HashMap<String, String>();
        for (final var pair : value.split("&")) {
            final var split = pair.split("=", 2);
            result.put(URLDecoder.decode(split[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(split[1], StandardCharsets.UTF_8));
        }
        return result;
    }

    private static void fail(final Operation operation) throws Exception {
        try {
            operation.run();
            throw new AssertionError("Expected IOException");
        } catch (IOException expected) {
        }
    }

    private static void check(final boolean value, final String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private interface Operation {
        void run() throws Exception;
    }

    private static final class Provider extends OidcLoginProvider {
        private final RSAKey key;
        private String token;
        private String form;

        private Provider(final RSAKey key) {
            this.key = key;
        }

        @Override
        protected String request(final URI uri, final String form) throws IOException {
            if (form == null) {
                return new JWKSet(key.toPublicJWK()).toString();
            }
            this.form = form;
            return "{\"id_token\":\"" + token + "\"}";
        }
    }
}
