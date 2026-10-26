package ecmwf.ecpds.extension.ecaccess;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ecmwf.common.database.ECUser;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;
import ecmwf.ecpds.extension.AuthenticationProvider.Result;
import ecmwf.ecpds.extension.ExtensionContext;

public final class EcaccessIdentityProviderTest {
    public static void main(final String[] args) throws Exception {
        try {
            run();
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
        // LoginManagement owns a process-wide maintenance thread.
        System.exit(0);
    }

    private static void run() throws Exception {
        final var client = new Client();
        final var provider = new EcaccessIdentityProvider(client, "master-root");
        check(provider.authenticateIdentity("alice", "password", "monitor").user() == client.user,
                "Password authentication must return fresh user data for core persistence");
        check(client.events.equals(List.of("password")), "A password must not trigger preauthentication");
        for (final var credential : List.of("123456", "12345678")) {
            client.events.clear();
            check(provider.authenticateIdentity("alice", credential, "monitor").result() == Result.ACCEPTED,
                    "ActivID six/eight digit passcodes must be accepted after preauthentication");
            check(client.events.equals(List.of("password", "preauth:master-root", "lookup")),
                    "Passcode must use Master root and fetch user only after successful preauthentication");
        }
        client.events.clear();
        check(provider.authenticate("alice", "wrong", "monitor") == Result.REJECTED, "Invalid password must reject");
        check(client.events.equals(List.of("password")), "A non-passcode must not trigger preauthentication");
        client.events.clear();
        check(provider.authenticate("alice", "000000", "monitor") == Result.REJECTED, "Invalid passcode must reject");
        check(client.events.equals(List.of("password", "preauth:master-root")),
                "Rejected preauth must not fetch a user");
        client.events.clear();
        check(provider.authenticate("alice", "999999", "monitor") == Result.REJECTED,
                "Null lookup after preauth must reject");
        client.events.clear();
        client.outage = true;
        fail(() -> provider.authenticate("alice", "123456", "monitor"));
        check(client.events.equals(List.of("password")), "An outage must not replay credentials through preauth");
        client.outage = false;
        client.absent = false;
        check(provider.lookup("alice") == client.user, "Directory import must return the ECUser");
        check(provider.lookup("missing") == null, "Absent ECUser must remain absent");
        fail(() -> provider.lookup("denied"));
        client.user.setName("someone-else");
        fail(() -> provider.authenticate("alice", "password", "monitor"));
        fail(() -> provider.lookup("alice"));
        client.events.clear();
        check(provider.authenticate("alice", "", "monitor") == Result.REJECTED, "Empty credentials must reject");
        check(client.events.isEmpty(), "Empty credentials must not reach ECCMD");
        provider.close();
        check(client.closed, "Provider shutdown must close its client");
        for (final var options : List.of(Map.<String, String> of(), Map.of("host", "host", "port", "0"),
                Map.of("host", "host", "port", "65536"), Map.of("host", "host", "port", "wrong"),
                Map.of("host", "host,"))) {
            fail(() -> new EcaccessIdentityProvider().start(new ExtensionContext("master", options)));
        }
        System.out.println("PASS: ECCMD password/passcode ordering, import, outages, identity checks and lifecycle");
    }

    private static void check(final boolean value, final String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void fail(final Operation operation) throws Exception {
        try {
            operation.run();
            throw new AssertionError("Expected IOException");
        } catch (IOException expected) {
        }
    }

    private interface Operation {
        void run() throws Exception;
    }

    private static final class Client implements EccmdClient {
        private final ECUser user = new ECUser("alice");
        private final List<String> events = new ArrayList<>();
        private boolean outage;
        private boolean absent;
        private boolean closed;

        @Override
        public ECUser password(final String user, final String credential) throws EccmdException, RemoteException {
            events.add("password");
            if (outage) {
                throw new RemoteException("private diagnostic must not escape");
            }
            if ("password".equals(credential)) {
                return this.user;
            }
            throw new EccmdException("Invalid password");
        }

        @Override
        public void preauthenticate(final String user, final String credential, final String root)
                throws EccmdException {
            events.add("preauth:" + root);
            if ("000000".equals(credential)) {
                throw new EccmdException("Invalid passcode");
            }
            absent = "999999".equals(credential);
        }

        @Override
        public ECUser lookup(final String user) throws EccmdException {
            events.add("lookup");
            if ("denied".equals(user)) {
                throw new EccmdException("Directory unavailable");
            }
            return absent || "missing".equals(user) ? null : this.user;
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
