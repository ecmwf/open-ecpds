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

package ecmwf.ecpds.master.scheduler;

import java.util.Map;
import java.util.function.Supplier;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.technical.Cnf;

public final class MoverAvailabilityScheduler extends MBeanScheduler {
    private static final Logger _log = LogManager.getLogger("ecmwf.ecpds.master.MasterServer");
    private final Supplier<ECpdsBase> database;
    private final Map<String, Boolean> heartbeats;

    private static final long RETENTION_MS = 365 * 24 * 3600_000L; // 1 year

    private int flushCount = 0;

    public MoverAvailabilityScheduler(final String name, final Supplier<ECpdsBase> database,
            final Map<String, Boolean> heartbeats) {
        super(name);
        this.database = database;
        this.heartbeats = heartbeats;
        setDelay(Cnf.durationAt("Scheduler", "moverAvailabilityScheduler", Timer.ONE_MINUTE));
        setJammedTimeout(Cnf.durationAt("Scheduler", "moverAvailabilitySchedulerJammedTimeout", 5 * Timer.ONE_MINUTE));
    }

    @Override
    public int nextStep() {
        final var base = database.get();
        if (base == null)
            return NEXT_STEP_DELAY;
        final var now = System.currentTimeMillis();
        final var currentMinute = (now / 60_000L) * 60_000L;
        final var prevMinute = currentMinute - 60_000L;
        // Flush completed minute buckets and remember which movers were recorded as "up".
        // We must track this BEFORE clearing the buffer so the "down" pass does not overwrite them.
        final var upMovers = new java.util.HashSet<String>();
        for (final var iter = heartbeats.entrySet().iterator(); iter.hasNext();) {
            final var entry = iter.next();
            final var key = entry.getKey();
            final var sep = key.lastIndexOf('|');
            if (sep <= 0)
                continue;
            final long bucketMinute;
            try {
                bucketMinute = Long.parseLong(key.substring(sep + 1));
            } catch (final NumberFormatException ignored) {
                iter.remove();
                continue;
            }
            if (bucketMinute < currentMinute) {
                iter.remove();
                final var moverName = key.substring(0, sep);
                try {
                    base.upsertMoverAvailabilitySnapshot(new java.sql.Timestamp(bucketMinute), moverName, true);
                    // Track movers recorded as "up" for prevMinute so we do not overwrite them below
                    if (bucketMinute == prevMinute) {
                        upMovers.add(moverName);
                    }
                } catch (final DataBaseException e) {
                    _log.warn("MoverAvailabilityScheduler: failed to record up for mover={}", moverName, e);
                }
            }
        }
        // Record "down" only for movers that had no heartbeat in the previous minute
        // and were not already written as "up" by the flush pass above.
        try {
            for (final var mover : database.get().getTransferServerArray()) {
                final var moverName = mover.getName();
                if (!upMovers.contains(moverName)) {
                    base.upsertMoverAvailabilitySnapshot(new java.sql.Timestamp(prevMinute), moverName, false);
                }
            }
        } catch (final Exception e) {
            _log.warn("MoverAvailabilityScheduler: failed to record down movers", e);
        }
        // Periodic retention cleanup (~every 24 hours)
        if (++flushCount % (24 * 60) == 0) {
            final var retentionHours = (int) (RETENTION_MS / Timer.ONE_HOUR);
            try {
                base.deleteOldMoverAvailabilitySnapshots(retentionHours);
            } catch (final DataBaseException e) {
                _log.warn("MoverAvailabilityScheduler: failed to delete old availability snapshots", e);
            }
        }
        return NEXT_STEP_DELAY;
    }
}
