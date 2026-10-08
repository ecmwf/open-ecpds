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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Standalone regression checks using the core classes and lib/* classpath. */
public final class LiveTransferEventsCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static LiveTransferSample sample(long id, String host, String attempt, long timestamp, String status) {
        return new LiveTransferSample(id, "mover", "destination", host, host, "127.0.0.1", "ftp",
                100, 50, 100, 4000, status, LiveTransferSample.DIRECTION_DISSEMINATION, "Dissemination",
                timestamp, attempt);
    }

    public static void main(String[] args) throws Exception {
        var clock = new AtomicLong(100000);
        var pending = new LiveTransferEvents(60000, 60000, 20000, clock::get);
        var active = sample(1, "host", "attempt-1", clock.get(), LiveTransferSample.STATUS_ACTIVE);
        pending.offer(active);
        check(!pending.offer(active), "Duplicate active accepted");
        var batch = pending.snapshot();
        clock.incrementAndGet();
        var completed = sample(1, "host", "attempt-1", clock.get(), LiveTransferSample.STATUS_DONE);
        pending.offer(completed);
        pending.acknowledge(batch);
        check(pending.snapshot().equals(List.of(completed)), "Acknowledgement discarded concurrent terminal");
        check(!pending.offer(active), "Older active overwrote terminal");
        check(!pending.offer(completed), "Duplicate terminal refreshed state");
        check(pending.snapshot().equals(pending.snapshot()), "Failed delivery removed pending batch");

        // A newer progress update survives acknowledging the version that was actually sent.
        var nextAttempt = sample(1, "host", "attempt-2", clock.get(), LiveTransferSample.STATUS_ACTIVE);
        pending.offer(nextAttempt);
        batch = pending.snapshot();
        var newer = sample(1, "host", "attempt-2", clock.incrementAndGet(), LiveTransferSample.STATUS_ACTIVE);
        pending.offer(newer);
        pending.acknowledge(batch);
        check(pending.snapshot().equals(List.of(newer)), "Acknowledgement discarded concurrent active update");
        pending.acknowledge(pending.snapshot());
        check(pending.snapshot().isEmpty(), "Successful delivery not cleared");

        var events = new LiveTransferEvents(60000, 60000, 20000, clock::get);
        events.offer(completed);
        events.offer(nextAttempt);
        events.offer(sample(1, "proxy", "attempt-1", clock.get(), LiveTransferSample.STATUS_DONE));
        check(events.snapshot().size() == 3, "Legs/attempts overwrite each other");
        var lateActive = sample(1, "host", "attempt-1", clock.addAndGet(20000), LiveTransferSample.STATUS_ACTIVE);
        check(!events.offer(lateActive), "Terminal tombstone failed after visualization expiry");
        check(events.snapshot().stream().filter(event -> !event.isTerminal()).count() == 1,
                "Terminal counted as active");
        clock.addAndGet(60000);
        check(events.snapshot().isEmpty(), "Old events did not expire");
        check(!events.offer(completed), "Retry revived expired event");

        var limited = new LiveTransferEvents(60000, 60000, 2, clock::get);
        limited.offer(sample(1, "host", "a", clock.get(), LiveTransferSample.STATUS_ACTIVE));
        limited.offer(sample(2, "host", "b", clock.get(), LiveTransferSample.STATUS_DONE));
        limited.offer(sample(3, "host", "c", clock.get(), LiveTransferSample.STATUS_ACTIVE));
        check(limited.snapshot().size() == 2 && limited.snapshot().stream().noneMatch(event -> event.getTransferId() == 2),
                "Capacity not enforced/terminal not evicted first");
        limited.offer(sample(4, "host", "d", clock.get(), LiveTransferSample.STATUS_ACTIVE));
        check(limited.snapshot().stream().noneMatch(event -> event.getTransferId() == 1), "Oldest active not evicted");
        limited.clear();
        check(limited.snapshot().isEmpty(), "No-viewer clear failed");
        limited.offer(sample(1, "host", "terminal-to-expire", clock.get(), LiveTransferSample.STATUS_DONE));
        clock.addAndGet(60000);
        limited.offer(sample(1, "host", "terminal-to-expire", clock.get(), LiveTransferSample.STATUS_ACTIVE));
        check(limited.snapshot().size() == 1 && !limited.snapshot().get(0).isTerminal(),
                "Expired tombstone blocks new legacy movement");

        var json = new ObjectMapper();
        var wire = json.writeValueAsString(completed);
        var relayed = json.readValue(wire, LiveTransferSample.class);
        relayed = json.readValue(json.writeValueAsString(relayed), LiveTransferSample.class);
        check(relayed.getTimestamp() == completed.getTimestamp(), "REST relay refreshed timestamp");
        check(relayed.getEventKey().equals(completed.getEventKey()), "REST relay changed event identity");
        check(!wire.contains("eventKey"), "Derived key serialized needlessly");
        var oldJson = json.readTree(wire);
        ((com.fasterxml.jackson.databind.node.ObjectNode) oldJson).remove(List.of("timestamp", "attemptId"));
        var legacy = json.treeToValue(oldJson, LiveTransferSample.class);
        check(legacy.getAttemptId() == null && legacy.getTimestamp() > 0, "Legacy JSON not supported");
        var serialized = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(serialized)) { out.writeObject(completed); }
        try (var in = new ObjectInputStream(new ByteArrayInputStream(serialized.toByteArray()))) {
            var rmi = (LiveTransferSample) in.readObject();
            check(rmi.getEventKey().equals(completed.getEventKey()) && rmi.getTimestamp() == completed.getTimestamp(),
                    "RMI identity/timestamp changed");
        }

        var registry = LiveTransferRegistry.getInstance();
        long now = System.currentTimeMillis();
        var shortTransfer = sample(123456789, "host", "short", now, LiveTransferSample.STATUS_DONE);
        var running = sample(123456789, "host", "retry", now, LiveTransferSample.STATUS_ACTIVE);
        registry.update(new LiveTransferSample[] { shortTransfer, shortTransfer, running });
        check(registry.getVisualizationSamples().size() == 2, "Short terminal lost / retry overwrote attempt");
        check(registry.getActiveTransfers().equals(List.of(running)), "Active-only snapshot contains terminal");
        check(registry.getVisualizationSamples().size() == 2, "First Monitor consumed second Monitor's event");
        var oldDone = sample(2, "host", "expired-display", now - 16000, LiveTransferSample.STATUS_FAILED);
        registry.update(new LiveTransferSample[] { oldDone });
        check(!registry.getVisualizationSamples().contains(oldDone), "Expired terminal visible");
        registry.update(new LiveTransferSample[] {
                sample(2, "host", "expired-display", now, LiveTransferSample.STATUS_ACTIVE) });
        check(registry.getActiveTransfers().size() == 1, "Delayed active revived finished event");

        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            events.offer(sample(i % 10000, "host", "perf", clock.get(), LiveTransferSample.STATUS_ACTIVE));
        }
        check(events.snapshot().size() == 10000, "Coalescing grows without bound");
        for (int i = 10000; i < 30000; i++) {
            events.offer(sample(i, "host", "overflow", clock.get(), LiveTransferSample.STATUS_DONE));
        }
        check(events.snapshot().size() == 20000, "Overflow capacity not bounded");
        System.out.printf("Event checks passed; 120,000 offers including bounded overflow: %.1f ms%n",
                (System.nanoTime() - start) / 1e6);
    }
}
