package ecmwf.ecpds.extension.ecaccess;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ecmwf.common.database.ECUser;
import ecmwf.ecbatch.eis.rmi.client.ECaccessInterface;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;
import ecmwf.ecbatch.eis.rmi.client.EccmdInterface;
import ecmwf.ecpds.extension.IdentityExtensions;

/**
 * Exercises the original dual-interface proxy shape, registry binding and ECUser/exception serialization.
 */
public final class RmiEccmdClientTest {
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
        final var config = Files.createTempFile("ecaccess-rmi-test-", ".properties");
        Files.writeString(config, "[RMIClientSocketFactory]\nconnectTimeOut=1000\nsoTimeOut=1000\n");
        System.setProperty("ecmwf.properties", config.toString());
        final int port;
        try (final var socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        final var calls = new AtomicInteger();
        final var remote = (Remote) Proxy.newProxyInstance(EccmdInterface.class.getClassLoader(),
                new Class<?>[] { EccmdInterface.class, ECaccessInterface.class }, (proxy, method, values) -> {
                    return switch (method.getName()) {
                    case "getECUser" -> {
                        if (values.length == 2) {
                            calls.incrementAndGet();
                            if ("outage".equals(values[1])) {
                                throw new RemoteException("simulated transport failure");
                            }
                            if (!"password".equals(values[1])) {
                                throw new EccmdException("Invalid password");
                            }
                        }
                        if ("missing".equals(values[0])) {
                            yield null;
                        }
                        final var user = new ECUser((String) values[0]);
                        user.setUid(1234);
                        user.setGid(5678);
                        user.setComment("ECCMD directory user");
                        yield user;
                    }
                    case "computePreAuth" -> {
                        if (!"123456".equals(values[1]) || !"master-root".equals(values[2])
                                || !"monitoring".equals(values[3])) {
                            throw new EccmdException("Invalid preauthentication");
                        }
                        yield "unused-preauth-result";
                    }
                    default -> throw new RemoteException("Unexpected call: " + method.getName());
                    };
                });
        final var registry = LocateRegistry.createRegistry(port);
        try {
            final var stub = UnicastRemoteObject.exportObject(remote, 0, new ecmwf.common.rmi.RMIClientSocketFactory(),
                    null);
            registry.rebind("EccmdEngine", stub);
            final var options = Map.of("class", EcaccessIdentityProvider.class.getName(), "host", "127.0.0.1", "port",
                    Integer.toString(port));
            try (final var extensions = IdentityExtensions.load("master-root", options, options)) {
                final var result = extensions.authenticateIdentity("alice", "password", "monitor");
                if (result.user().getUid() != 1234 || result.user().getGid() != 5678
                        || !"ECCMD directory user".equals(result.user().getComment())) {
                    throw new AssertionError("Original ECUser contract must survive RMI");
                }
                extensions.authenticateIdentity("alice", "123456", "monitor");
                if (!"alice".equals(extensions.lookup("alice").getName()) || extensions.lookup("missing") != null) {
                    throw new AssertionError("Directory lookup must use the original wire contract");
                }
            }
            try (final var client = new RmiEccmdClient(List.of("127.0.0.1"), port)) {
                final var before = calls.get();
                try {
                    client.password("alice", "outage");
                    throw new AssertionError("Expected remote failure");
                } catch (RemoteException expected) {
                }
                if (calls.get() != before + 1) {
                    throw new AssertionError("A failed credential RPC must not be replayed");
                }
                client.password("alice", "password");
                UnicastRemoteObject.unexportObject(remote, true);
                final var replacement = (Remote) Proxy.newProxyInstance(EccmdInterface.class.getClassLoader(),
                        new Class<?>[] { EccmdInterface.class, ECaccessInterface.class },
                        Proxy.getInvocationHandler(remote));
                try {
                    registry.rebind("EccmdEngine", UnicastRemoteObject.exportObject(replacement, 0,
                            new ecmwf.common.rmi.RMIClientSocketFactory(), null));
                    try {
                        client.lookup("alice");
                        throw new AssertionError("Stale connection must fail the current request");
                    } catch (RemoteException expected) {
                    }
                    if (!"alice".equals(client.lookup("alice").getName())) {
                        throw new AssertionError("Next request must rediscover the restarted daemon");
                    }
                } finally {
                    UnicastRemoteObject.unexportObject(replacement, true);
                }
                client.close();
                try {
                    client.lookup("alice");
                    throw new AssertionError("Closed clients must not reconnect");
                } catch (RemoteException expected) {
                }
            }
            System.out.println(
                    "PASS: ECCMD RMI proxy/DTO compatibility, SPI loading, reconnect and no credential replay");
        } finally {
            try {
                UnicastRemoteObject.unexportObject(remote, true);
            } catch (java.rmi.NoSuchObjectException ignored) {
            }
            UnicastRemoteObject.unexportObject(registry, true);
            Files.delete(config);
        }
    }
}
