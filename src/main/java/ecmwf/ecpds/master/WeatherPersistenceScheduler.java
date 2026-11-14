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

import java.util.Set;
import javax.management.timer.Timer;
import ecmwf.common.ecaccess.MBeanScheduler;

final class WeatherPersistenceScheduler extends MBeanScheduler {
    private final MasterServer _master;

    WeatherPersistenceScheduler(final MasterServer _master) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super("WeatherPersistenceScheduler");
        setPriority(Thread.MIN_PRIORITY);
        setDelay(Timer.ONE_SECOND);
        setJammedTimeout(5 * Timer.ONE_MINUTE);
    }

    @Override
    public int nextStep() {
        _master.weatherAdministration.persistence().step();
        _master.weatherAdministration.refreshAvailability();
        final var state = _master.weatherAdministration.status();
        _master.weatherAdministration.probes().step(state.collector().enabled(), state.collector().masterEpoch(),
                Set.copyOf(state.paths()), System.currentTimeMillis());
        return NEXT_STEP_DELAY;
    }
}
