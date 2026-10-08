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

package ecmwf.ecpds.master;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import ecmwf.common.technical.AccountingStreams;

/** Standalone regression checks, runnable with the core classes and lib/* on the classpath. */
public final class TrafficAccountingCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void invalid(TrafficAccounting counter, Map<String, String> state) {
        var before = counter.snapshot();
        try {
            counter.merge(state);
            throw new AssertionError("Accepted invalid snapshot");
        } catch (IllegalArgumentException expected) {
            check(before.equals(counter.snapshot()), "Invalid snapshot mutated counters");
        }
    }

    public static void main(String[] args) throws Exception {
        var producer = new TrafficAccounting();
        for (var type : TrafficAccounting.TYPES) producer.add(type, 100);
        var master = new TrafficAccounting();
        master.merge(producer.snapshot());
        master.merge(producer.snapshot());
        check(master.totals().values().stream().allMatch(value -> value == 100), "Duplicate delivery");
        var older = producer.snapshot();
        producer.add("Proxy", 20);
        master.merge(producer.snapshot());
        master.merge(older);
        check(master.totals().get("Proxy") == 120, "Out-of-order delivery");
        check(TrafficAccounting.changedBuckets(older, older).isEmpty(), "Unchanged payload");
        var changed = TrafficAccounting.changedBuckets(producer.snapshot(), older);
        check(changed.size() == 1 && changed.containsKey("Proxy"), "Per-type changed payload");

        long minute = System.currentTimeMillis() / 60000;
        invalid(master, Map.of("Unknown", minute + ":1"));
        invalid(master, Map.of("Backup", minute + ":-1"));
        invalid(master, Map.of("Backup", "-1:1"));
        invalid(master, Map.of("Backup", (minute + 1) + ":1"));
        invalid(master, Map.of("Backup", "x".repeat(60001)));
        invalid(master, Map.of("Backup", minute + ":999," + minute + ":not-a-number"));
        var expiry = new TrafficAccounting();
        expiry.merge(Map.of("Source", (minute - 1440) + ":5," + (minute - 1439) + ":7"));
        check(expiry.totals().get("Source") == 7, "Minute expiry");
        var full = new StringBuilder();
        for (int age = 0; age < 1440; age++) {
            if (age > 0) full.append(',');
            full.append(minute - age).append(":1000000000");
        }
        var fullState = new java.util.LinkedHashMap<String, String>();
        for (var type : TrafficAccounting.TYPES) fullState.put(type, full.toString());
        var fullCounter = new TrafficAccounting();
        fullCounter.merge(fullState);
        check(fullCounter.totals().values().stream().allMatch(value -> value == 1440000000000L),
                "Full window/type separation");
        check(fullCounter.snapshot().values().stream().allMatch(value -> value.length() < 60000),
                "Snapshot exceeds database column");
        var secondProducer = new TrafficAccounting();
        secondProducer.merge(fullState);
        check(fullCounter.totals().get("Backup") + secondProducer.totals().get("Backup") == 2880000000000L,
                "Independent producer summation");
        long snapshotStart = System.nanoTime();
        for (int i = 0; i < 100; i++) fullCounter.snapshot();
        System.out.printf("Full six-type snapshot: %.3f ms; full replay: %d bytes%n",
                (System.nanoTime() - snapshotStart) / 1e8,
                fullCounter.snapshot().values().stream().mapToInt(String::length).sum());
        var before = fullCounter.snapshot();
        fullCounter.add("Dissemination", 1);
        check(TrafficAccounting.changedBuckets(fullCounter.snapshot(), before).get("Dissemination").length() < 50,
                "Steady-state transport sends full history");

        var directory = Files.createTempDirectory("traffic-accounting-check-");
        var file = directory.resolve("state.json");
        try {
            var checkpoint = producer.checkpoint(file);
            producer.add("Proxy", 50);
            check(checkpoint.equals(producer.checkpoint(file)) == false, "Checkpoint advances");
            var recovered = new TrafficAccounting();
            recovered.load(file);
            check(producer.getSourceId().equals(recovered.getSourceId()), "Persistent producer identity");
            check(producer.snapshot().equals(recovered.snapshot()), "Restart recovery");

            // Transport must use the checkpoint, never later uncheckpointed increments.
            var durable = recovered.checkpoint(file);
            recovered.add("Proxy", 999);
            var receiver = new TrafficAccounting();
            receiver.merge(durable);
            var afterCrash = new TrafficAccounting();
            afterCrash.load(file);
            check(receiver.snapshot().equals(afterCrash.snapshot()), "Crash rollback ahead of Master");
            afterCrash.add("Proxy", 10);
            receiver.merge(TrafficAccounting.changedBuckets(afterCrash.checkpoint(file), durable));
            check(receiver.totals().get("Proxy") == 180, "New increments after restart");
            var lostAck = afterCrash.checkpoint(file);
            receiver.merge(TrafficAccounting.changedBuckets(lostAck, durable));
            receiver.merge(TrafficAccounting.changedBuckets(lostAck, durable));
            check(receiver.totals().get("Proxy") == 180, "Retry after lost acknowledgement");
            check(!new TrafficAccounting().getSourceId().equals(afterCrash.getSourceId()), "New epoch after file loss");

            var blocker = directory.resolve("not-a-directory");
            Files.writeString(blocker, "blocked");
            try {
                producer.add("Backup", 1);
                producer.checkpoint(blocker.resolve("state.json"));
                throw new AssertionError("Checkpoint failure hidden");
            } catch (IOException expected) {
                check(producer.totals().get("Backup") == 101, "Disk failure loses memory");
            } finally {
                Files.delete(blocker);
            }
            fullCounter.checkpoint(file);
            var fullRecovery = new TrafficAccounting();
            fullRecovery.load(file);
            check(fullRecovery.snapshot().equals(fullCounter.snapshot()), "Full-window checkpoint recovery");
            check(Files.size(file) < 400000, "Checkpoint exceeds recovery size limit");
            Files.writeString(file, "{\"__source\":\"not-a-uuid\"}");
            try {
                new TrafficAccounting().load(file);
                throw new AssertionError("Corrupt checkpoint silently reset");
            } catch (IOException expected) {
            }
        } finally {
            Files.deleteIfExists(file.resolveSibling("state.json.tmp"));
            Files.deleteIfExists(file);
            Files.delete(directory);
        }

        var observed = new AtomicLong();
        var buffer = new byte[1024];
        try (var input = AccountingStreams.input(new ByteArrayInputStream(buffer), observed::addAndGet)) {
            check(input.read() == 0, "Single-byte read");
            check(input.readNBytes(buffer, 0, 100) == 100, "Bulk read");
            check(input.skip(10) == 10, "Skip");
            input.transferTo(OutputStream.nullOutputStream());
            check(input.read() == -1, "EOF");
        }
        check(observed.get() == 1014, "Input bytes counted twice or skipped bytes counted");
        observed.set(0);
        var sink = new ByteArrayOutputStream();
        try (var output = AccountingStreams.output(sink, observed::addAndGet)) {
            output.write(1);
            output.write(buffer);
            output.write(buffer, 0, 100);
        }
        check(observed.get() == sink.size() && sink.size() == 1125, "Output bulk double count");
        observed.set(0);
        sink.reset();
        try (var compressed = new GZIPOutputStream(AccountingStreams.output(sink, observed::addAndGet))) {
            compressed.write(new byte[10000]);
        }
        check(observed.get() == sink.size() && observed.get() < 10000, "Filtered-output boundary");
        observed.set(0);
        var failure = AccountingStreams.output(new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw new IOException("Simulated failure");
            }
        }, observed::addAndGet);
        try {
            failure.write(buffer);
            throw new AssertionError("Stream failure hidden");
        } catch (IOException expected) {
            check(observed.get() == 0, "Failed write invented bytes");
        }
        observed.set(0);
        var attempts = new java.util.concurrent.atomic.AtomicInteger();
        var partial = AccountingStreams.output(new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                if (attempts.incrementAndGet() > 100) throw new IOException("Attempt failed");
            }
        }, observed::addAndGet);
        try {
            for (int i = 0; i < 200; i++) partial.write(0);
            throw new AssertionError("Partial failure hidden");
        } catch (IOException expected) {
            check(observed.get() == 100, "Partial attempt lost");
        }
        try (var retry = AccountingStreams.output(OutputStream.nullOutputStream(), observed::addAndGet)) {
            retry.write(new byte[200]);
        }
        check(observed.get() == 300, "Retry attempt deduplicated as completed file bytes");

        var concurrent = new TrafficAccounting();
        long start = System.nanoTime();
        try (var threads = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<Future<?>>();
            for (int thread = 0; thread < 8; thread++) {
                futures.add(threads.submit(() -> {
                    for (int i = 0; i < 100000; i++) concurrent.add("Dissemination", 65536);
                }));
            }
            for (var future : futures) future.get();
        }
        check(concurrent.totals().get("Dissemination") == 800000L * 65536, "Concurrent increment loss");
        System.out.printf("Checks passed; 800,000 contended increments: %.1f ms%n",
                (System.nanoTime() - start) / 1e6);
    }
}
