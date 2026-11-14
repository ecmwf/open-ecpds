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

import javax.management.timer.Timer;
import ecmwf.common.ecaccess.MBeanScheduler;

final class WeatherAutomationScheduler extends MBeanScheduler {
    private final MasterServer _master;

    WeatherAutomationScheduler(final MasterServer _master) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super("WeatherAutomationScheduler");
        setPriority(Thread.MIN_PRIORITY);
        setDelay(60 * Timer.ONE_SECOND);
    }

    @Override
    public int nextStep() {
        _master.weatherAutomation.step(System.currentTimeMillis());
        return NEXT_STEP_DELAY;
    }
}
