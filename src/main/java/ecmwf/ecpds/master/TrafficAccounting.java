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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Absolute, per-source minute counters. Replaying a snapshot merges maxima, never adds the same bytes twice. Transfer
 * threads only touch in-memory counters.
 */
public final class TrafficAccounting {
    public static final List<String> TYPES = List.of("Dissemination", "Acquisition", "Replication", "Source", "Backup",
            "Proxy");
    private static final int MINUTES = 1440;
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Map<String, long[]> bytes = new LinkedHashMap<>();
    private final long[] minutes = new long[MINUTES];
    private long generation;
    private long savedGeneration = -1;
    private String sourceId = java.util.UUID.randomUUID().toString();
    private final Object checkpointLock = new Object();
    private Map<String, String> lastCheckpoint = Map.of();

    public String getSourceId() {
        return sourceId;
    }

    public TrafficAccounting() {
        java.util.Arrays.fill(minutes, -1);
        for (var type : TYPES)
            bytes.put(type, new long[MINUTES]);
    }

    public synchronized void add(String type, long count) {
        if (count < 0)
            throw new IllegalArgumentException("Negative traffic bytes");
        if (count == 0)
            return;
        var bucket = bytes.get(type);
        if (bucket == null)
            throw new IllegalArgumentException("Unknown traffic type: " + type);
        long minute = System.currentTimeMillis() / 60000;
        int index = (int) (minute % MINUTES);
        if (minutes[index] != minute) {
            minutes[index] = minute;
            for (var values : bytes.values())
                values[index] = 0;
        }
        bucket[index] = Math.addExact(bucket[index], count);
        generation++;
    }

    public synchronized Map<String, String> snapshot() {
        var result = new LinkedHashMap<String, String>();
        long now = System.currentTimeMillis() / 60000;
        for (var entry : bytes.entrySet()) {
            var text = new StringBuilder();
            for (int i = 0; i < MINUTES; i++) {
                if (minutes[i] > now - MINUTES && minutes[i] <= now && entry.getValue()[i] > 0) {
                    if (text.length() > 0)
                        text.append(',');
                    text.append(minutes[i]).append(':').append(entry.getValue()[i]);
                }
            }
            result.put(entry.getKey(), text.toString());
        }
        return result;
    }

    public synchronized void merge(Map<String, String> snapshot) {
        // Parse the whole snapshot before changing counters.
        if (snapshot == null || snapshot.size() > TYPES.size())
            throw new IllegalArgumentException("Invalid traffic snapshot");
        var parsed = new LinkedHashMap<String, Map<Long, Long>>();
        long now = System.currentTimeMillis() / 60000;
        for (var entry : snapshot.entrySet()) {
            if (!bytes.containsKey(entry.getKey()))
                throw new IllegalArgumentException("Unknown traffic type: " + entry.getKey());
            if (entry.getValue() == null || entry.getValue().length() > 60000)
                throw new IllegalArgumentException("Oversized or missing traffic buckets");
            var values = new LinkedHashMap<Long, Long>();
            if (!entry.getValue().isBlank()) {
                for (var item : entry.getValue().split(",")) {
                    var fields = item.split(":");
                    if (fields.length != 2)
                        throw new IllegalArgumentException("Invalid traffic bucket");
                    long minute = Long.parseLong(fields[0]), count = Long.parseLong(fields[1]);
                    if (count < 0 || minute < 0 || minute > now)
                        throw new IllegalArgumentException("Invalid traffic bucket value");
                    if (minute > now - MINUTES)
                        values.merge(minute, count, Math::max);
                }
            }
            parsed.put(entry.getKey(), values);
        }
        for (var entry : parsed.entrySet()) {
            for (var value : entry.getValue().entrySet()) {
                int index = (int) (value.getKey() % MINUTES);
                if (minutes[index] != value.getKey()) {
                    minutes[index] = value.getKey();
                    for (var bucket : bytes.values())
                        bucket[index] = 0;
                }
                bytes.get(entry.getKey())[index] = Math.max(bytes.get(entry.getKey())[index], value.getValue());
            }
        }
        generation++;
    }

    public synchronized Map<String, Long> totals() {
        var result = new LinkedHashMap<String, Long>();
        long now = System.currentTimeMillis() / 60000;
        for (var entry : bytes.entrySet()) {
            long total = 0;
            for (int i = 0; i < MINUTES; i++) {
                if (minutes[i] > now - MINUTES && minutes[i] <= now)
                    total = Math.addExact(total, entry.getValue()[i]);
            }
            result.put(entry.getKey(), total);
        }
        return result;
    }

    public void load(Path path) throws IOException {
        if (!Files.notExists(path)) {
            if (Files.size(path) > 400000)
                throw new IOException("Oversized accounting checkpoint");
            var state = JSON.readValue(Files.readString(path), new TypeReference<Map<String, String>>() {
            });
            var id = state.remove("__source");
            if (id == null)
                throw new IOException("Accounting source identity missing");
            try {
                java.util.UUID.fromString(id);
                merge(state);
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid accounting checkpoint", e);
            }
            sourceId = id;
            lastCheckpoint = snapshot();
        }
    }

    public Map<String, String> checkpoint(Path path) throws IOException {
        synchronized (checkpointLock) {
            final Map<String, String> snapshot;
            final long version;
            synchronized (this) {
                if (generation == savedGeneration)
                    return lastCheckpoint;
                snapshot = snapshot();
                version = generation;
            }
            Files.createDirectories(path.toAbsolutePath().getParent());
            var fileState = new LinkedHashMap<>(snapshot);
            fileState.put("__source", sourceId);
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temporary, JSON.writeValueAsString(fileState));
            try (var channel = java.nio.channels.FileChannel.open(temporary, java.nio.file.StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            synchronized (this) {
                savedGeneration = version;
                lastCheckpoint = Map.copyOf(snapshot);
                return lastCheckpoint;
            }
        }
    }

    public static Map<String, String> changedBuckets(Map<String, String> current, Map<String, String> previous) {
        var result = new LinkedHashMap<String, String>();
        for (var entry : current.entrySet()) {
            var old = new java.util.HashSet<String>(List.of(previous.getOrDefault(entry.getKey(), "").split(",")));
            var changed = new StringBuilder();
            for (var bucket : entry.getValue().split(",")) {
                if (!bucket.isEmpty() && !old.contains(bucket)) {
                    if (changed.length() > 0)
                        changed.append(',');
                    changed.append(bucket);
                }
            }
            if (changed.length() > 0)
                result.put(entry.getKey(), changed.toString());
        }
        return result;
    }
}
