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

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.technical.Cnf;

public final class PortalBytesScheduler extends MBeanScheduler {
    private static final Logger _log = LogManager.getLogger("ecmwf.ecpds.master.MasterServer");
    private final Supplier<ECpdsBase> database;
    private final ConcurrentMap<String, long[]> portalBytes;
    private final ConcurrentMap<String, long[]> destinationBytes;
    private final long retentionMs;

    private int flushCount = 0;

    public PortalBytesScheduler(final String name, final Supplier<ECpdsBase> database,
            final ConcurrentMap<String, long[]> portalBytes, final ConcurrentMap<String, long[]> destinationBytes,
            final long retentionMs) {
        super(name);
        this.database = database;
        this.portalBytes = portalBytes;
        this.destinationBytes = destinationBytes;
        this.retentionMs = retentionMs;
        setDelay(Cnf.durationAt("Scheduler", "portalBytesScheduler", 30 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "portalBytesSchedulerJammedTimeout", 5 * Timer.ONE_MINUTE));
    }

    @Override
    public int nextStep() {
        final var base = database.get();
        if (base == null)
            return NEXT_STEP_DELAY;
        for (final var iter = portalBytes.entrySet().iterator(); iter.hasNext();) {
            final var entry = iter.next();
            final var key = entry.getKey();
            final var separator = key.lastIndexOf('|');
            if (separator <= 0 || separator >= key.length() - 1) {
                _log.warn("PortalBytesScheduler: invalid buffer key={}", key);
                continue;
            }
            final var snapshot = entry.getValue().clone();
            if (snapshot[0] <= 0 && snapshot[1] <= 0) {
                iter.remove();
                continue;
            }
            final var userId = key.substring(0, separator);
            try {
                final var minute = new Timestamp(Long.parseLong(key.substring(separator + 1)));
                base.upsertPortalBytesSnapshot(minute, userId, snapshot[0], snapshot[1]);
                portalBytes.compute(key, (_, current) -> {
                    if (current == null)
                        return null;
                    current[0] -= snapshot[0];
                    current[1] -= snapshot[1];
                    return current[0] <= 0 && current[1] <= 0 ? null : current;
                });
            } catch (final Exception e) {
                _log.warn("PortalBytesScheduler: failed to flush entry for user={} minute={}", userId,
                        key.substring(separator + 1), e);
            }
        }
        for (final var iter = destinationBytes.entrySet().iterator(); iter.hasNext();) {
            final var entry = iter.next();
            final var key = entry.getKey();
            final var separator = key.lastIndexOf('|');
            if (separator <= 0 || separator >= key.length() - 1) {
                _log.warn("PortalBytesScheduler: invalid destination buffer key={}", key);
                continue;
            }
            final var snapshot = entry.getValue().clone();
            if (snapshot[0] <= 0 && snapshot[1] <= 0) {
                iter.remove();
                continue;
            }
            final var destinationName = key.substring(0, separator);
            try {
                final var minute = Timestamp.valueOf(key.substring(separator + 1).replace('T', ' ') + ":00");
                base.upsertDestinationBytesSnapshot(minute, destinationName, snapshot[0], snapshot[1]);
                destinationBytes.compute(key, (_, current) -> {
                    if (current == null)
                        return null;
                    current[0] -= snapshot[0];
                    current[1] -= snapshot[1];
                    return current[0] <= 0 && current[1] <= 0 ? null : current;
                });
            } catch (final Exception e) {
                _log.warn("PortalBytesScheduler: failed to flush entry for destination={} minute={}", destinationName,
                        key.substring(separator + 1), e);
            }
        }
        if (++flushCount % 10 == 0) {
            final var retentionHours = (int) (retentionMs / Timer.ONE_HOUR);
            try {
                base.deleteOldPortalBytesSnapshots(retentionHours);
                base.deleteOldDestinationBytesSnapshots(retentionHours);
            } catch (final DataBaseException e) {
                _log.warn("PortalBytesScheduler: failed to delete old bytes snapshots", e);
            }
        }
        return NEXT_STEP_DELAY;
    }
}
