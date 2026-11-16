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

package ecmwf.ecpds.mover;

import java.util.Map;

import javax.management.timer.Timer;

import ecmwf.common.technical.Cnf;

final class TrafficAccountingScheduler extends ecmwf.common.ecaccess.MBeanScheduler {
    private final MoverServer _mover;

    private Map<String, String> acknowledged = Map.of();

    TrafficAccountingScheduler(final MoverServer _mover) {
        this._mover = _mover;

        super("TrafficAccountingScheduler");
        final var delay = Cnf.durationAt("Scheduler", "trafficAccounting", 10 * Timer.ONE_SECOND);
        if (delay < Timer.ONE_SECOND)
            throw new IllegalArgumentException("trafficAccounting must be at least 1 second");
        setDelay(delay);
        setJammedTimeout(Cnf.durationAt("Scheduler", "trafficAccountingJammedTimeout", 5 * Timer.ONE_MINUTE));
    }

    @Override
    public int nextStep() {
        try {
            final var snapshot = _mover.trafficAccounting.checkpoint(_mover.trafficAccountingPath);
            if (_mover.masterManager != null && !_mover.masterManager.isConnected()) {
                return NEXT_STEP_DELAY;
            }
            if (_mover.masterManager == null && (_mover.restProvider == null || !Cnf.has("MoverProxy"))) {
                return NEXT_STEP_DELAY;
            }
            final var changes = ecmwf.ecpds.master.TrafficAccounting.changedBuckets(snapshot, acknowledged);
            if (!changes.isEmpty()) {
                _mover.getMasterProxy().updateTrafficAccounting(
                        _mover.getRoot() + ":" + _mover.trafficAccounting.getSourceId(), changes);
                acknowledged = snapshot;
            }
        } catch (final Exception e) {
            MoverServer._log.warn("Traffic accounting checkpoint/delivery failed; will retry", e);
        }
        return NEXT_STEP_DELAY;
    }
}
