package ecmwf.ecpds.master.location;

import java.net.InetAddress;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ecmwf.common.database.SystemPhysicalLocationStore.Attempt;
import ecmwf.common.database.SystemPhysicalLocationStore.Snapshot;
import ecmwf.common.database.SystemPhysicalLocationStore.Status;
import ecmwf.ecpds.master.location.PhysicalLocationConfig.Discovery;
import ecmwf.ecpds.master.location.PhysicalLocationConfig.Machine;
import ecmwf.ecpds.master.location.PhysicalLocationConfig.Network;
import ecmwf.ecpds.master.location.PhysicalLocationConfig.Profile;
import ecmwf.ecpds.master.location.PhysicalLocationService.Result;

public final class PhysicalLocationDiscoveryTest {
    private static final Profile PROFILE = new Profile(false, "", "", "", "", "test-only", "");

    public static void main(String[] args) throws Exception {
        configuration();
        reconciliation();
        liveCollection();
        asynchronousInventory();
        System.out.println(
                "PASS: dynamic inventory, CIDR/DNS restrictions, deduplication, removal, persistence and bounded background work");
    }

    private static PhysicalLocationConfig config(int max, Map<String, String> systems, Map<String, Machine> machines) {
        return new PhysicalLocationConfig(true, 60_000, 86_400_000, 100, 0, 2, systems, machines, max, new Discovery(
                10_000, 161, PROFILE, List.of(Network.parse("192.0.2.0/24"), Network.parse("2001:db8::/32"))));
    }

    private static InetAddress ip(String text) {
        return com.google.common.net.InetAddresses.forString(text);
    }

    private static void configuration() throws Exception {
        final var secret = Files.createTempFile("snmp-discovery-test", ".secret");
        try {
            Files.writeString(secret, "test-only");
            final Map<String, Map<String, String>> sections = new HashMap<>();
            sections.put("PhysicalLocation", Map.of("enabled", "yes", "discovery", "yes", "discoveryProfile", "site",
                    "allowedNetworks", "192.0.2.0/24, 2001:db8::/32"));
            sections.put("PhysicalLocationProfile.site",
                    Map.of("version", "v2c", "communityPasswordFile", secret.toString()));
            final var config = PhysicalLocationConfig.load(s -> sections.getOrDefault(s, Map.of()));
            check(config.machines().isEmpty() && config.discovery() != null, "No machine sections required");
            check(config.discovery().permits(ip("192.0.2.255")), "IPv4 CIDR boundary");
            check(!config.discovery().permits(ip("192.0.3.0")), "Reject adjacent IPv4 subnet");
            check(config.discovery().permits(ip("2001:db8:ffff::1")), "IPv6 CIDR");
            check(!config.discovery().permits(ip("2001:db9::1")), "Reject adjacent IPv6 subnet");
            for (String invalid : List.of("", "0.0.0.0/0", "192.0.2.0/33", "host/24", "192.0.2.0")) {
                boolean rejected = false;
                try {
                    Network.parse(invalid);
                } catch (IllegalArgumentException e) {
                    rejected = true;
                }
                check(rejected, "Reject invalid or unrestricted CIDR: " + invalid);
            }
        } finally {
            Files.delete(secret);
        }
    }

    private static void reconciliation() throws Exception {
        final var staticMachine = PhysicalLocationConfig.machine("rack-a", ip("192.0.2.1"), 161, PROFILE);
        final var config = config(2, Map.of("master", "rack-a"), Map.of("rack-a", staticMachine));
        final AtomicReference<Map<String, String>> systems = new AtomicReference<>();
        final AtomicReference<Map<String, Machine>> machines = new AtomicReference<>();
        final var lookups = new AtomicInteger();
        try (final var discovery = new PhysicalLocationDiscovery(config, Map::of, (s, m) -> {
            systems.set(s);
            machines.set(m);
        })) {
            discovery.reconcile(Map.of("master", "outside", "mover.a", "same", "mover.share", "same", "mover.b",
                    "mixed", "mover.c", "unknown", "mover.literal", "192.0.2.2", "monitor.ui", "same",
                    "database.db:3306", "192.0.2.2"), host -> {
                        lookups.incrementAndGet();
                        return switch (host) {
                        case "same" -> new InetAddress[] { ip("192.0.2.1") };
                        case "mixed" -> new InetAddress[] { ip("192.0.2.3"), ip("198.51.100.1") };
                        default -> throw new java.net.UnknownHostException();
                        };
                    });
            check(systems.get().get("master").equals("rack-a"), "Static mapping wins");
            check(systems.get().get("mover.a").equals("rack-a") && systems.get().get("mover.share").equals("rack-a"),
                    "Shared target polled once");
            check(systems.get().get("monitor.ui").equals("rack-a")
                    && systems.get().get("database.db:3306").equals(systems.get().get("mover.literal")),
                    "Monitor and database targets automatically enrolled and deduplicated");
            check(!systems.get().containsKey("mover.b") && !systems.get().containsKey("mover.c"),
                    "Mixed DNS and unresolved names fail closed");
            check(lookups.get() == 3, "Literal bypass and duplicate DNS names resolved once per refresh");
            check(machines.get().size() == 2, "Deduplicated machine count");
            discovery.reconcile(Map.of("mover.new", "192.0.2.5"), _ -> {
                throw new AssertionError("Literal must not use DNS");
            });
            check(!systems.get().containsKey("mover.a") && systems.get().containsKey("mover.new"),
                    "Live addition and removal without restart");
            discovery.reconcile(Map.of("mover.a", "192.0.2.5", "mover.b", "192.0.2.6"), _ -> null);
            check(machines.get().size() == 2 && systems.get().size() == 2, "Target limit with static reservation");
            discovery.reconcile(Map.of("mover.a", "resolved"), _ -> new InetAddress[] { ip("198.51.100.2") });
            check(systems.get().equals(config.systems()), "DNS move outside allowed networks removes old target");
        }
    }

    private static void liveCollection() throws Exception {
        final var config = config(4, Map.of(), Map.of());
        final Map<String, Snapshot> rows = new ConcurrentHashMap<>();
        final var calls = new AtomicInteger();
        final var blocked = new CountDownLatch(1);
        final var release = new CountDownLatch(1);
        final var store = new PhysicalLocationService.Store() {
            public Snapshot find(String machine, String key) {
                return rows.get(machine);
            }

            public void save(Attempt a) {
                rows.put(a.machineId(), new Snapshot(a.machineId(), a.targetKey(), a.location(), a.status(),
                        a.attemptedAt(), a.attemptedAt()));
            }
        };
        final var provider = new PhysicalLocationService.Provider() {
            public Result query(Machine m, long timeout, int retries) throws Exception {
                calls.incrementAndGet();
                blocked.countDown();
                check(release.await(3, TimeUnit.SECONDS), "Release blocked poll");
                return new Result(Status.SUCCESS, "Rack " + m.address().getHostAddress());
            }

            public void close() {
                release.countDown();
            }
        };
        try (final var service = new PhysicalLocationService(config, provider, store, System::currentTimeMillis);
                final var discovery = new PhysicalLocationDiscovery(config, Map::of, service::replaceTargets)) {
            discovery.reconcile(Map.of("mover.a", "192.0.2.1", "mover.b", "192.0.2.1"), _ -> null);
            service.tick();
            check(blocked.await(2, TimeUnit.SECONDS), "Poll started");
            discovery.reconcile(Map.of(), _ -> null);
            check(service.metadata("mover.a").isEmpty(), "Removal hides metadata during active poll");
            discovery.reconcile(Map.of("mover.a", "192.0.2.1"), _ -> null);
            service.tick();
            check(calls.get() == 1, "Rapid re-add does not overlap polls");
            release.countDown();
            await(() -> service.activePolls() == 0);
            check(service.metadata("mover.a").get("value").equals("Rack 192.0.2.1"), "Persisted result visible");
            discovery.reconcile(Map.of("mover.a", "192.0.2.2"), _ -> null);
            check(!service.metadata("mover.a").containsKey("value"), "Changed endpoint cannot show old location");
            service.tick();
            await(() -> service.activePolls() == 0);
            check(service.metadata("mover.a").get("value").equals("Rack 192.0.2.2"), "Edited host polled");
            check(rows.size() == 2, "Old database rows retained without associating wrong endpoint");
        }
    }

    private static void asynchronousInventory() throws Exception {
        final var started = new CountDownLatch(1);
        final var release = new CountDownLatch(1);
        final var published = new CountDownLatch(1);
        final var calls = new AtomicInteger();
        try (final var discovery = new PhysicalLocationDiscovery(config(4, Map.of(), Map.of()), () -> {
            calls.incrementAndGet();
            started.countDown();
            release.await();
            return Map.of("master", "192.0.2.1");
        }, (_, _) -> published.countDown())) {
            discovery.tick();
            check(started.await(2, TimeUnit.SECONDS), "Background inventory started");
            for (int i = 0; i < 100; i++) {
                discovery.tick();
            }
            check(calls.get() == 1 && published.getCount() == 1, "No overlap or queued inventory jobs");
            release.countDown();
            check(published.await(2, TimeUnit.SECONDS), "Background reconciliation completed");
        } finally {
            release.countDown();
        }
    }

    private static void await(java.util.function.BooleanSupplier ready) throws Exception {
        final long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (!ready.getAsBoolean() && System.nanoTime() < until) {
            Thread.sleep(5);
        }
        check(ready.getAsBoolean(), "Background poll finished");
    }

    private static void check(boolean valid, String message) {
        if (!valid) {
            throw new AssertionError(message);
        }
    }
}
