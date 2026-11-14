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

import java.util.concurrent.ConcurrentMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.ecpds.master.TrafficAccounting;

public final class TrafficAccountingMaintenance extends MBeanScheduler {
    private static final Logger _log = LogManager.getLogger("ecmwf.ecpds.master.MasterServer");
    private final Supplier<ECpdsBase> database;
    private final ConcurrentMap<String, TrafficAccounting> actualTraffic;
    private final BooleanSupplier ready;

    public TrafficAccountingMaintenance(final Supplier<ECpdsBase> database,
            final ConcurrentMap<String, TrafficAccounting> actualTraffic, final BooleanSupplier ready) {
        super("TrafficAccountingMaintenance");
        this.database = database;
        this.actualTraffic = actualTraffic;
        this.ready = ready;
        setDelay(Timer.ONE_HOUR);
        setJammedTimeout(5 * Timer.ONE_MINUTE);
    }

    @Override
    public int nextStep() {
        if (ready.getAsBoolean()) {
            for (final var key : actualTraffic.keySet()) {
                actualTraffic.computeIfPresent(key, (_, counter) -> {
                    if (counter.totals().values().stream().anyMatch(value -> value > 0))
                        return counter;
                    try {
                        for (final var type : TrafficAccounting.TYPES) {
                            database.get().deleteSysConfigValue("ActualTraffic", key + "." + type);
                        }
                        return null;
                    } catch (final DataBaseException e) {
                        _log.warn("Cannot prune expired traffic producer {}", key, e);
                        return counter;
                    }
                });
            }
        }
        return NEXT_STEP_DELAY;
    }
}
