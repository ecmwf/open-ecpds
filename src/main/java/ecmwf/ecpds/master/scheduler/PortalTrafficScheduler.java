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
import ecmwf.common.database.PortalTraffic;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.technical.Cnf;

public final class PortalTrafficScheduler extends MBeanScheduler {
    private static final Logger _log = LogManager.getLogger("ecmwf.ecpds.master.MasterServer");
    private final Supplier<ECpdsBase> database;
    private final Map<String, PortalTraffic> buffer;

    public PortalTrafficScheduler(final String name, final Supplier<ECpdsBase> database,
            final Map<String, PortalTraffic> buffer) {
        super(name);
        this.database = database;
        this.buffer = buffer;
        setDelay(Cnf.durationAt("Scheduler", "portalTrafficScheduler", Timer.ONE_MINUTE));
        setJammedTimeout(Cnf.durationAt("Scheduler", "portalTrafficSchedulerJammedTimeout", 5 * Timer.ONE_MINUTE));
    }

    @Override
    public int nextStep() {
        final var now = System.currentTimeMillis();
        final var currentMinute = (now / 60000L) * 60000L;
        final var base = database.get();
        for (final var iter = buffer.entrySet().iterator(); iter.hasNext();) {
            final var entry = iter.next();
            final var pt = entry.getValue();
            if (pt.getTime() != null && pt.getTime().getTime() < currentMinute) {
                iter.remove();
                try {
                    base.upsertPortalTraffic(pt);
                } catch (final DataBaseException e) {
                    _log.warn("PortalTrafficScheduler: failed to flush entry for user={} time={}", pt.getUser(),
                            pt.getTime(), e);
                }
            }
        }
        return NEXT_STEP_DELAY;
    }
}
