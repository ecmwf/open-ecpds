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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.LongSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Bounded, expiring latest events with conditional acknowledgement for concurrent producers. */
public final class LiveTransferEvents {
    private static final Logger LOG = LogManager.getLogger(LiveTransferEvents.class);
    private final LinkedHashMap<String, LiveTransferSample> events = new LinkedHashMap<>();
    private final LinkedHashSet<String> terminalKeys = new LinkedHashSet<>();
    private final long activeLifetime;
    private final long terminalLifetime;
    private final int capacity;
    private final LongSupplier clock;
    private long lastOverflowWarning = Long.MIN_VALUE;

    public LiveTransferEvents(long activeLifetime, long terminalLifetime, int capacity) {
        this(activeLifetime, terminalLifetime, capacity, System::currentTimeMillis);
    }

    LiveTransferEvents(long activeLifetime, long terminalLifetime, int capacity, LongSupplier clock) {
        this.activeLifetime = activeLifetime;
        this.terminalLifetime = terminalLifetime;
        this.capacity = capacity;
        this.clock = clock;
    }

    public synchronized boolean offer(LiveTransferSample sample) {
        if (sample == null || expired(sample))
            return false;
        final var key = sample.getEventKey();
        var previous = events.get(key);
        if (previous != null && expired(previous)) {
            events.remove(key);
            terminalKeys.remove(key);
            previous = null;
        }
        if (previous != null) {
            if (previous.getTimestamp() > sample.getTimestamp()
                    || previous.isTerminal()
                            && (!sample.isTerminal() || previous.getTimestamp() == sample.getTimestamp())
                    || !sample.isTerminal() && previous.getTimestamp() == sample.getTimestamp()
                            && previous.getByteSent() >= sample.getByteSent())
                return false;
        } else if (events.size() >= capacity) {
            final var oldest = terminalKeys.isEmpty() ? events.keySet().iterator().next()
                    : terminalKeys.iterator().next();
            events.remove(oldest);
            terminalKeys.remove(oldest);
            final long now = clock.getAsLong();
            if (lastOverflowWarning == Long.MIN_VALUE || now - lastOverflowWarning >= 60000) {
                LOG.warn("Live transfer event buffer reached {} entries; evicting oldest events", capacity);
                lastOverflowWarning = now;
            }
        }
        events.put(key, sample);
        if (sample.isTerminal())
            terminalKeys.add(key);
        return true;
    }

    public synchronized List<LiveTransferSample> snapshot() {
        purge();
        return List.copyOf(events.values());
    }

    public synchronized void acknowledge(List<LiveTransferSample> delivered) {
        for (final var sample : delivered) {
            final var key = sample.getEventKey();
            if (events.remove(key, sample))
                terminalKeys.remove(key);
        }
    }

    public synchronized void clear() {
        events.clear();
        terminalKeys.clear();
    }

    private boolean expired(LiveTransferSample sample) {
        return clock.getAsLong() - sample.getTimestamp() >= (sample.isTerminal() ? terminalLifetime : activeLifetime);
    }

    private void purge() {
        events.entrySet().removeIf(entry -> {
            if (!expired(entry.getValue()))
                return false;
            terminalKeys.remove(entry.getKey());
            return true;
        });
    }
}
