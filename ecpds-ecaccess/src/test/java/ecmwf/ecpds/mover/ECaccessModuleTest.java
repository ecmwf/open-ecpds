package ecmwf.ecpds.mover;

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_GATEWAY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_LASTUSED;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECACCESS_LOADBALANCING;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.MSUser;
import ecmwf.common.ectrans.ECtransException;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.RMIClientSocketFactory;
import ecmwf.common.technical.ProxySocket;
import ecmwf.ecbatch.eis.rmi.client.DataAccess;
import ecmwf.ecbatch.eis.rmi.client.ECaccessInterface;
import ecmwf.ecbatch.eis.rmi.client.EccmdInterface;
import ecmwf.ecpds.extension.RemoteServices;
import ecmwf.ecpds.extension.ecaccess.EcaccessGatewayAccess;
import ecmwf.ecpds.extension.ecaccess.EcaccessGatewayProvider;
import ecmwf.ecpds.master.MasterInterface;

/** Real RMI control hops and ProxySocket byte callbacks, without production services. */
public final class ECaccessModuleTest {
    private static final byte[] PAYLOAD = "resumed transfer payload".getBytes(StandardCharsets.UTF_8);

    public static void main(final String[] args) throws Exception {
        final var config = Files.createTempFile("ecaccess-transfer-", ".properties");
        Files.writeString(config, """
                [RMIClientSocketFactory]
                connectTimeOut=2000
                soTimeOut=2000
                [CallBack]
                port=0
                [ProxySocket]
                plainText=yes
                host=127.0.0.1
                """);
        System.setProperty("ecmwf.properties", config.toString());
        final var events = Collections.synchronizedList(new ArrayList<String>());
        final var payload = new AtomicReference<byte[]>();
        final var failRemove = new AtomicBoolean();
        final var gateway = (Remote) Proxy.newProxyInstance(DataAccess.class.getClassLoader(),
                new Class<?>[] { DataAccess.class }, (proxy, method, values) -> {
                    final var target = (String) values[0];
                    events.add(method.getName() + ":" + target);
                    if ("available".equals(method.getName())) {
                        return !"unavailable".equals(target);
                    }
                    check("alice".equals(((ECUser) values[1]).getName()), "ECUser must survive both RMI hops");
                    final var association = (MSUser) values[2];
                    check("association".equals(association.getName())
                            && association.getECtransDestinationName() == null
                            && association.getECtransDestination() == null, "Association must be detached");
                    final var destination = (ECtransDestination) values[3];
                    check("genericFtp".equals(destination.getName()) && !destination.getActive(),
                            "Destination must preserve legacy gateway handling");
                    check(("transfer-cookie@" + target).equals(values[4]), "Cookie must include selected gateway");
                    return switch (method.getName()) {
                    case "put" -> {
                        check((long) values[7] == 5 && (long) values[8] == PAYLOAD.length + 5,
                                "Upload offset and total size must be preserved");
                        check(association.getData().contains("initialInputFilter")
                                && association.getData().contains("initialInputMd5"),
                                "Delegated filter and checksum metadata must reach the gateway");
                        try (final var in = ((ProxySocket) values[6]).getDataInputStream()) {
                            payload.set(in.readAllBytes());
                        }
                        yield null;
                    }
                    case "get" -> {
                        check((long) values[7] == 5, "Download offset must be preserved");
                        try (final var out = ((ProxySocket) values[6]).getDataOutputStream()) {
                            out.write(PAYLOAD);
                        }
                        yield null;
                    }
                    case "size" -> 42L;
                    case "list" -> new String[] { "one.txt", "two.bin" };
                    case "del" -> throw new ECtransException("simulated gateway failure");
                    case "remove" -> {
                        if (failRemove.get()) {
                            throw new ECtransException("simulated cleanup failure");
                        }
                        yield true;
                    }
                    default -> null;
                    };
                });
        final var gatewayStub = UnicastRemoteObject.exportObject(gateway, 0, new RMIClientSocketFactory(), null);
        final int port;
        try (final var probe = new ServerSocket(0)) {
            port = probe.getLocalPort();
        }
        final var registry = LocateRegistry.createRegistry(port);
        final var eccmd = (Remote) Proxy.newProxyInstance(EccmdInterface.class.getClassLoader(),
                new Class<?>[] { EccmdInterface.class, ECaccessInterface.class }, (proxy, method, values) -> {
                    check("getDataAccess".equals(method.getName()), "Transfer discovery must not authenticate");
                    events.add("discover:" + values[0]);
                    return "missing".equals(values[0]) ? null : gatewayStub;
                });
        Remote master = null;
        try (final var extensions = RemoteServices.load("master-root", Map.of("ecaccess", "EcaccessTransfers"),
                section -> Map.of("class", EcaccessGatewayProvider.class.getName(), "host", "127.0.0.1", "port",
                        Integer.toString(port)))) {
            registry.rebind("EccmdEngine", UnicastRemoteObject.exportObject(eccmd, 0,
                    new RMIClientSocketFactory(), null));
            master = (Remote) Proxy.newProxyInstance(MasterInterface.class.getClassLoader(),
                    new Class<?>[] { MasterInterface.class },
                    (proxy, method, values) -> {
                        try {
                            return extensions.open((String) values[0], (String) values[1]);
                        } catch (IOException e) {
                            throw new RemoteException("Remote extension service is unavailable");
                        }
                    });
            final var masterStub = (MasterInterface) UnicastRemoteObject.exportObject(master, 0,
                    new RMIClientSocketFactory(), null);
            final var relays = new ArrayList<EcaccessGatewayAccess>();
            final ECaccessModule.Connector connector = target -> {
                final var access = (EcaccessGatewayAccess) masterStub.getExtensionService("ecaccess", target);
                if (access != null) {
                    relays.add(access);
                }
                return access;
            };
            final var setup = new ECtransSetup("ecaccess");
            setup.set(HOST_ECACCESS_GATEWAY, "missing,unavailable,good");
            final var module = configured(connector);
            module.connect("unused", setup);
            check(events.equals(List.of("discover:missing", "discover:unavailable", "available:unavailable",
                    "discover:good", "available:good")), "Unavailable gateways must fail over in order");
            fails(() -> relays.getFirst().available());
            check("good".equals(setup.getString(HOST_ECACCESS_LASTUSED)), "Selected gateway must be remembered");
            module.setInputFilter("gzip");
            module.setInputSize(PAYLOAD.length + 5);
            module.setInputMd5("checksum-from-source");
            transfer(module, true);
            check(Arrays.equals(PAYLOAD, payload.get()), "Upload bytes must reach the gateway");
            transfer(module, false);
            check(module.size("file") == 42, "Gateway size must be returned");
            check(Arrays.equals(module.listAsStringArray("/", ".*\\.txt"), new String[] { "one.txt" }),
                    "Listing must preserve filename filtering");
            fails(() -> module.listAsStringArray("/", "["));
            module.mkdir("dir");
            module.rmdir("dir");
            module.move("old", "new");
            check(events.containsAll(List.of("mkdir:good", "rmdir:good", "move:good")),
                    "Directory and move operations must reach the gateway");
            fails(() -> module.del("file"));
            check(Collections.frequency(events, "del:good") == 1, "Failed mutations must never be replayed");
            module.check(PAYLOAD.length, "checksum-from-source", false);
            check("file".equals(module.getAttribute("remote.fileName")), "Remote filename must remain available");
            module.close();
            module.close();
            check(Collections.frequency(events, "remove:good") == 1, "Close must remove the cookie exactly once");
            fails(() -> relays.getLast().available());
            fails(() -> module.size("file"));

            events.clear();
            setup.set(HOST_ECACCESS_GATEWAY, "good,next");
            setup.set(HOST_ECACCESS_LOADBALANCING, true);
            try (final var balanced = configured(connector)) {
                balanced.connect("unused", setup);
                check("next".equals(balanced.getAttribute("remote.hostName")),
                        "Load balancing must start after the last successful gateway");
            }
            final var withoutCookie = configured(connector);
            final var setCookie = TransferModule.class.getDeclaredMethod("setCookie", String.class);
            setCookie.setAccessible(true);
            setCookie.invoke(withoutCookie, (Object) null);
            withoutCookie.connect("unused", setup);
            withoutCookie.close();
            fails(() -> relays.getLast().available());

            final var failedCleanup = configured(connector);
            failedCleanup.connect("unused", setup);
            failRemove.set(true);
            failedCleanup.close();
            fails(() -> relays.getLast().available());
            final var leased = (EcaccessGatewayAccess) masterStub.getExtensionService("ecaccess", "good");
            extensions.close();
            fails(leased::available);
            fails(() -> masterStub.getExtensionService("ecaccess", "good"));
            System.out.println("PASS: ECCMD discovery, Master RMI relay, byte callbacks, resume, metadata, failover,"
                    + " load balancing, no replay and export cleanup");
        } finally {
            if (master != null) {
                UnicastRemoteObject.unexportObject(master, true);
            }
            UnicastRemoteObject.unexportObject(eccmd, true);
            UnicastRemoteObject.unexportObject(gateway, true);
            UnicastRemoteObject.unexportObject(registry, true);
            Files.delete(config);
        }
    }

    private static ECaccessModule configured(final ECaccessModule.Connector connector) throws Exception {
        final var module = new ECaccessModule(connector);
        final var association = new MSUser("association");
        association.setECtransDestinationName("local-destination");
        association.setECtransDestination(new ECtransDestination("local-destination"));
        // Initialize the context normally supplied by ECtrans.
        for (final var entry : Map.of("setECUser", new ECUser("alice"), "setMSUser", association,
                "setCookie", "transfer-cookie").entrySet()) {
            final var setter = TransferModule.class.getDeclaredMethod(entry.getKey(), entry.getValue().getClass());
            setter.setAccessible(true);
            setter.invoke(module, entry.getValue());
        }
        return module;
    }

    private static void transfer(final ECaccessModule module, final boolean upload) throws Exception {
        try (final var listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                final var executor = Executors.newSingleThreadExecutor()) {
            listener.setSoTimeout(3000);
            final var callback = executor.submit(() -> {
                try (final var socket = listener.accept()) {
                    socket.setSoTimeout(3000);
                    final var in = socket.getInputStream();
                    check("T0000000007".equals(new String(in.readNBytes(11), StandardCharsets.US_ASCII)),
                            "Gateway must connect using the original proxy ticket protocol");
                    final var out = socket.getOutputStream();
                    out.write('0');
                    out.flush();
                    if (upload) {
                        out.write(PAYLOAD);
                        return PAYLOAD;
                    }
                    return in.readAllBytes();
                }
            });
            final var ticket = new ProxySocket(7, "127.0.0.1", listener.getLocalPort(), true);
            if (upload) {
                module.put("file", 5, PAYLOAD.length + 5, ticket);
            } else {
                module.get("file", 5, ticket);
            }
            check(Arrays.equals(PAYLOAD, callback.get(4, TimeUnit.SECONDS)),
                    "Download bytes must reach the Mover callback");
        }
    }

    private static void check(final boolean condition, final String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void fails(final Operation operation) throws Exception {
        try {
            operation.run();
            throw new AssertionError("Expected IOException");
        } catch (IOException expected) {
        }
    }

    private interface Operation {
        void run() throws Exception;
    }
}
