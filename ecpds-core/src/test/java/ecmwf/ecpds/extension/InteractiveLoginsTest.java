package ecmwf.ecpds.extension;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

public final class InteractiveLoginsTest {
    public static void main(final String[] args) throws Exception {
        final var provider = new Provider();
        final var clock = new MutableClock();
        final var users = Map.of("alice", "{\"issuer\":\"https://identity.example\",\"subject\":\"subject-1\"}");
        final var logins = new InteractiveLogins(provider, users, clock);
        final var binding = InteractiveLogins.random();
        var attempt = logins.begin(binding, "monitor");
        final var first = attempt;
        fail(() -> logins.complete(first.state(), InteractiveLogins.random(), "monitor", "code"));
        fail(() -> logins.complete(first.state(), binding, "different-monitor", "code"));
        check(provider.calls == 0, "Invalid browser/origin binding must not reach the provider");
        check("alice".equals(logins.complete(attempt.state(), binding, "monitor", "code")),
                "Mapped identity must succeed");
        fail(() -> logins.complete(first.state(), binding, "monitor", "code"));
        check(provider.calls == 1, "Replay must not reach the provider");
        attempt = logins.begin(binding, "monitor");
        final var expired = attempt;
        clock.now = clock.now.plusSeconds(301);
        fail(() -> logins.complete(expired.state(), binding, "monitor", "code"));
        check(provider.calls == 1, "Expired attempts must not reach the provider");
        final var wrongIdentity = logins.begin(binding, "monitor");
        provider.subject = "unmapped";
        fail(() -> logins.complete(wrongIdentity.state(), binding, "monitor", "code"));
        provider.subject = "subject-1";
        fail(() -> logins.complete(wrongIdentity.state(), binding, "monitor", "code"));
        final var rejected = logins.begin(binding, "monitor");
        fail(() -> logins.complete(rejected.state(), binding, "monitor", "invalid"));
        fail(() -> logins.complete(rejected.state(), binding, "monitor", "code"));
        final var cleared = logins.begin(binding, "monitor");
        logins.clear();
        fail(() -> logins.complete(cleared.state(), binding, "monitor", "code"));
        fail(() -> new InteractiveLogins(provider, Map.of("alice", users.get("alice"), "bob", users.get("alice"))));
        final var disabled = new InteractiveLogins(null, Map.of());
        fail(() -> disabled.begin(binding, "monitor"));
        for (var i = 0; i < 1000; i++) {
            logins.begin(binding, "monitor");
        }
        fail(() -> logins.begin(binding, "monitor"));
        clock.now = clock.now.plusSeconds(301);
        logins.begin(binding, "monitor");
        check("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM".equals(
                InteractiveLogins.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")), "RFC 7636 S256 vector");
        System.out.println("PASS: mapping, browser/origin binding, expiry, replay, capacity and PKCE");
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

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-26T12:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static final class Provider implements InteractiveLoginProvider {
        private String nonce;
        private String challenge;
        private String subject = "subject-1";
        private int calls;

        @Override
        public int apiVersion() {
            return API_VERSION;
        }

        @Override
        public String displayName() {
            return "Test";
        }

        @Override
        public URI authorizationUri(final String state, final String nonce, final String challenge) {
            this.nonce = nonce;
            this.challenge = challenge;
            return URI.create("https://identity.example/authorize?state=" + state);
        }

        @Override
        public ExternalIdentity authenticate(final String code, final String nonce, final String verifier)
                throws IOException {
            calls++;
            check(this.nonce.equals(nonce), "Core must preserve nonce");
            check(challenge.equals(InteractiveLogins.challenge(verifier)), "Core must preserve the PKCE verifier");
            if (!"code".equals(code)) {
                throw new IOException("Invalid authorization code");
            }
            return new ExternalIdentity("https://identity.example", subject);
        }
    }
}
