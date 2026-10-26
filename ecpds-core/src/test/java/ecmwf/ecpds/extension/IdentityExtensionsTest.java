package ecmwf.ecpds.extension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ecmwf.common.database.ECUser;

public final class IdentityExtensionsTest {
    private static final List<String> EVENTS = new ArrayList<>();

    public static void main(final String[] args) throws Exception {
        try (final var extensions = IdentityExtensions.load("master", Map.of(), Map.of())) {
            check(!extensions.hasAuthenticationProvider(), "Unconfigured authentication must stay disabled");
            check(extensions.lookup("alice") == null, "Unconfigured directory must not import users");
        }
        try (final var extensions = IdentityExtensions.load("master", options(TestAuthentication.class),
                options(TestDirectory.class))) {
            check(extensions.hasAuthenticationProvider(), "Configured authentication must be active");
            check(extensions.authenticate("alice", "valid", "monitor") == AuthenticationProvider.Result.ACCEPTED,
                    "Valid credentials must be accepted");
            check(extensions.authenticate("alice", "invalid", "monitor") == AuthenticationProvider.Result.REJECTED,
                    "Invalid credentials must be rejected");
            check(extensions.authenticate("alice", "unsupported",
                    "monitor") == AuthenticationProvider.Result.UNSUPPORTED,
                    "Unsupported credentials must remain distinct from rejection");
            expectFailure(() -> extensions.authenticate("alice", "outage", "monitor"));
            expectFailure(() -> extensions.authenticate("alice", "null", "monitor"));
            check("alice".equals(extensions.lookup("alice").getName()), "Directory entry must be returned");
            check(extensions.lookup("absent") == null, "Missing directory entry must remain absent");
            expectFailure(() -> extensions.lookup("mismatched"));
            expectFailure(() -> extensions.lookup("outage"));
        }
        check(EVENTS.equals(List.of("auth.start", "directory.start", "directory.close", "auth.close")),
                "Providers must start once and close in reverse order");
        EVENTS.clear();
        expectFailure(() -> IdentityExtensions.load("master", options(TestAuthentication.class),
                options(FailingDirectory.class)));
        check(EVENTS.equals(List.of("auth.start", "failing.start", "failing.close", "auth.close")),
                "Failed startup must release the failed provider and previously started providers");
        EVENTS.clear();
        expectFailure(() -> IdentityExtensions.load("master", options(IncompatibleAuthentication.class), Map.of()));
        check(EVENTS.equals(List.of("auth.close")), "Incompatible providers must not start");
        expectFailure(() -> IdentityExtensions.load("master", options(String.class), Map.of()));
        expectFailure(() -> IdentityExtensions.load("master", Map.of("class", "missing.IdentityProvider"), Map.of()));
        System.out.println("PASS: explicit loading, identity results, directory validation and lifecycle cleanup");
    }

    private static Map<String, String> options(final Class<?> type) {
        return Map.of("class", type.getName(), "endpoint", "test");
    }

    private static void expectFailure(final Operation operation) throws Exception {
        try {
            operation.run();
            throw new AssertionError("Expected IOException");
        } catch (final IOException expected) {
        }
    }

    private static void check(final boolean valid, final String message) {
        if (!valid) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface Operation {
        void run() throws Exception;
    }

    public static class TestAuthentication implements AuthenticationProvider {
        @Override
        public int apiVersion() {
            return API_VERSION;
        }

        @Override
        public void start(final ExtensionContext context) {
            check("master".equals(context.root()) && "test".equals(context.option("endpoint")),
                    "Provider must receive its own startup configuration");
            EVENTS.add("auth.start");
        }

        @Override
        public Result authenticate(final String user, final String credentials, final String origin)
                throws IOException {
            return switch (credentials) {
            case "valid" -> Result.ACCEPTED;
            case "unsupported" -> Result.UNSUPPORTED;
            case "null" -> null;
            case "outage" -> throw new IOException("Provider unavailable");
            default -> Result.REJECTED;
            };
        }

        @Override
        public void close() {
            EVENTS.add("auth.close");
        }
    }

    public static class TestDirectory implements UserDirectoryProvider {
        @Override
        public int apiVersion() {
            return API_VERSION;
        }

        @Override
        public void start(final ExtensionContext context) {
            EVENTS.add("directory.start");
        }

        @Override
        public ECUser lookup(final String user) throws IOException {
            return switch (user) {
            case "absent" -> null;
            case "outage" -> throw new IOException("Directory unavailable");
            case "mismatched" -> new ECUser("someone-else");
            default -> new ECUser(user);
            };
        }

        @Override
        public void close() {
            EVENTS.add("directory.close");
        }
    }

    public static final class FailingDirectory extends TestDirectory {
        @Override
        public void start(final ExtensionContext context) {
            EVENTS.add("failing.start");
            throw new IllegalStateException("Startup failed");
        }

        @Override
        public void close() {
            EVENTS.add("failing.close");
        }
    }

    public static final class IncompatibleAuthentication extends TestAuthentication {
        @Override
        public int apiVersion() {
            return API_VERSION + 1;
        }
    }
}
