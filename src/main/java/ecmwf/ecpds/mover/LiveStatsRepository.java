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

import javax.management.timer.Timer;

import ecmwf.common.ecaccess.MBeanRepository;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.LiveTransferSample;

/**
 * Batches live globe samples per transfer; this viewer-driven stream is independent of Network Weather.
 */
final class LiveStatsRepository extends MBeanRepository<LiveTransferSample> {
    private final MoverServer _mover;

    /** Pending samples, coalesced by transfer id. */
    private final ecmwf.ecpds.master.LiveTransferEvents _pending = new ecmwf.ecpds.master.LiveTransferEvents(60_000,
            60_000, 20_000);

    /** Cached result of the last {@link MasterProxy#isLiveTransferMonitoringEnabled()} poll. */
    private volatile boolean _masterWantsLiveStats = false;

    /** Last time {@link MasterProxy#isLiveTransferMonitoringEnabled()} was polled. */
    private volatile long _lastEnabledCheck = -1;
    private long _lastDeliveryWarning = -1;

    private void warnDeliveryFailure(final String message, final Exception error) {
        final var now = System.currentTimeMillis();
        if (_lastDeliveryWarning < 0 || now - _lastDeliveryWarning >= Timer.ONE_MINUTE) {
            MoverServer._log.warn(message, error);
            _lastDeliveryWarning = now;
        } else {
            MoverServer._log.debug(message, error);
        }
    }

    /**
     * Instantiates a new live stats repository.
     *
     * @param name
     *            the name
     */
    LiveStatsRepository(final MoverServer _mover, final String name) {
        this._mover = _mover;

        super(name);
        setDelay(Cnf.durationAt("Scheduler", "liveStatsRepository", 2 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "liveStatsRepositoryJammedTimeout", 5 * Timer.ONE_MINUTE));
    }

    /**
     * Whether the MasterServer currently wants live transfer statistics. Cached and refreshed at most every
     * {@link #getDelay()} to keep the check cheap when called from the hot path (e.g. {@code update()} on every active
     * transfer).
     *
     * @return true, if enabled
     */
    boolean isMasterEnabled() {
        return _masterWantsLiveStats;
    }

    /**
     * Offer a fresh sample, replacing any not-yet-pushed sample for the same transfer.
     *
     * @param sample
     *            the sample
     */
    void offer(final LiveTransferSample sample) {
        if (sample != null) {
            _pending.offer(sample);
        }
    }

    /**
     * Gets the key.
     *
     * @param sample
     *            the sample
     *
     * @return the key
     */
    @Override
    public String getKey(final LiveTransferSample sample) {
        return sample.getEventKey();
    }

    /**
     * Gets the status.
     *
     * @param sample
     *            the sample
     *
     * @return the status
     */
    @Override
    public String getStatus(final LiveTransferSample sample) {
        return sample.getStatus() + "(" + Format.formatSize(sample.getByteSent()) + " on " + sample.getDestinationName()
                + "/" + sample.getHostName() + ")";
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        if (_mover.masterManager != null && !_mover.masterManager.isConnected()) {
            return NEXT_STEP_DELAY;
        }
        final var now = System.currentTimeMillis();
        if (now - _lastEnabledCheck >= getDelay()) {
            _lastEnabledCheck = now;
            try {
                _masterWantsLiveStats = _mover.getMasterProxy().isLiveTransferMonitoringEnabled();
            } catch (final Exception e) {
                warnDeliveryFailure("Cannot refresh globe interest; retaining recent events for retry", e);
                return NEXT_STEP_DELAY;
            }
        }
        if (!_masterWantsLiveStats) {
            // Nobody is watching, so let's not bother pushing anything (and drop what accumulated meanwhile)!
            _pending.clear();
            return NEXT_STEP_DELAY;
        }
        final var batch = _pending.snapshot();
        if (batch.isEmpty())
            return NEXT_STEP_DELAY;
        final var array = batch.toArray(new LiveTransferSample[0]);
        try {
            _mover.getMasterProxy().updateLiveTransferStatistics(array);
            _pending.acknowledge(batch);
        } catch (final Exception e) {
            warnDeliveryFailure("Live transfer event delivery failed; retaining recent events for retry", e);
        }
        return NEXT_STEP_DELAY;
    }
}
