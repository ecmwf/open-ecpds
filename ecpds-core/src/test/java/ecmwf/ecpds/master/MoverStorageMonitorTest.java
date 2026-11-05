package ecmwf.ecpds.master;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ecmwf.common.database.StorageVolumeInfo;
import ecmwf.ecpds.mover.MoverInterface;

public final class MoverStorageMonitorTest {
    public static void main(final String[] args) throws Exception {
        final var blocked = new CountDownLatch(1);
        final var started = new CountDownLatch(1);
        final var calls = new AtomicInteger();
        final var usage = new long[][] { { 25 }, { 100 } };
        final var slow = mover((method) -> {
            if ("computeVolumeUsage".equals(method)) {
                calls.incrementAndGet();
                started.countDown();
                blocked.await();
                return usage;
            }
            return List.of(new StorageVolumeInfo(0, "/data/volume0", "/dev/sda", "/data", "fs"));
        });
        final var healthy = mover(
                (method) -> "computeVolumeUsage".equals(method) ? new long[][] { { 50 }, { 200 } } : List.of());
        try (var monitor = new MoverStorageMonitor(2)) {
            monitor.refresh("slow", 1, () -> slow);
            assert started.await(2, TimeUnit.SECONDS);
            for (var i = 0; i < 10; i++) {
                monitor.refresh("slow", 1, () -> slow);
            }
            monitor.refresh("healthy", 1, () -> healthy);
            await(() -> monitor.usage("healthy").containsKey("healthy"));
            assert monitor.usage("slow").isEmpty();
            assert calls.get() == 1 : "Overlapping calls to blocked Mover";
            assert monitor.updated("healthy").get("healthy") > 0;
            blocked.countDown();
            await(() -> monitor.details("slow").containsKey("slow"));
            usage[0][0] = 999;
            assert monitor.usage("slow").get("slow")[0][0] == 25 : "Cached arrays must be copied";
            final var broken = mover((method) -> {
                throw new java.rmi.RemoteException("test failure");
            });
            monitor.refresh("slow", 1, () -> broken);
            Thread.sleep(100);
            assert monitor.usage("slow").get("slow")[0][0] == 25 : "Failure must preserve last snapshot";
            assert monitor.details("slow").get("slow").size() == 1;
            monitor.refresh("slow", 1, () -> healthy);
            await(() -> monitor.usage("slow").get("slow")[0][0] == 50);
            final var noMetadata = mover((method) -> {
                if ("getStorageVolumeInfo".equals(method)) {
                    throw new java.rmi.RemoteException("metadata unavailable");
                }
                return new long[][] { { 75 }, { 100 } };
            });
            monitor.refresh("metadata-failure", 1, () -> noMetadata);
            await(() -> monitor.usage("metadata-failure").containsKey("metadata-failure"));
            assert monitor.details("metadata-failure").isEmpty();
        } finally {
            blocked.countDown();
        }
        final var held = new CountDownLatch(1);
        final var entered = new CountDownLatch(1);
        final var blockedMover = mover((method) -> {
            if ("computeVolumeUsage".equals(method)) {
                entered.countDown();
                held.await();
                return new long[][] { { 1 }, { 2 } };
            }
            return List.of();
        });
        try (var monitor = new MoverStorageMonitor(1)) {
            monitor.refresh("held", 1, () -> blockedMover);
            assert entered.await(2, TimeUnit.SECONDS);
            monitor.refresh("retry", 1, () -> healthy);
            assert monitor.usage("retry").isEmpty() : "Rejected work must not queue";
            held.countDown();
            await(() -> monitor.details("held").containsKey("held"));
            await(() -> {
                monitor.refresh("retry", 1, () -> healthy);
                return monitor.usage("retry").containsKey("retry");
            });
        } finally {
            held.countDown();
        }
        System.out.println(
                "PASS: nonblocking snapshots, slow-Mover isolation, single-flight bounded polling, retry, recovery and metadata failure.");
    }

    private interface Call {
        Object invoke(String method) throws Throwable;
    }

    private static MoverInterface mover(final Call call) {
        return (MoverInterface) Proxy.newProxyInstance(MoverInterface.class.getClassLoader(),
                new Class<?>[] { MoverInterface.class }, (_, method, _) -> call.invoke(method.getName()));
    }

    private static void await(final java.util.function.BooleanSupplier condition) throws Exception {
        final var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting for background poll");
            }
            Thread.sleep(10);
        }
    }
}
