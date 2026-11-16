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

final class WeatherNegotiationScheduler extends ecmwf.common.ecaccess.MBeanScheduler {
    private final MoverServer _mover;

    final ecmwf.ecpds.master.weather.WeatherSourceSigner signer = new ecmwf.ecpds.master.weather.WeatherSourceSigner(
            _mover.getRoot());
    volatile boolean closed;
    final ecmwf.ecpds.master.weather.WeatherNegotiation negotiation = new ecmwf.ecpds.master.weather.WeatherNegotiation(
            _mover.getRoot());
    final ecmwf.ecpds.master.weather.WeatherOutbox outbox = new ecmwf.ecpds.master.weather.WeatherOutbox();
    final ecmwf.ecpds.master.weather.WeatherCapture capture = new ecmwf.ecpds.master.weather.WeatherCapture(
            _mover.getRoot(), negotiation, outbox, signer.epoch());
    final ecmwf.ecpds.master.weather.WeatherProbeRunner probes = new ecmwf.ecpds.master.weather.WeatherProbeRunner(
            _mover.getRoot(), signer.epoch());
    private ecmwf.ecpds.master.weather.WeatherSelection lastSelection;
    private int failures;
    private volatile String state;

    WeatherNegotiationScheduler(final MoverServer _mover) {
        // Field initializers need the same early owner binding as the former inner class.
        this._mover = _mover;
        super("WeatherNegotiationScheduler");
        setPriority(Thread.MIN_PRIORITY);
        setDelay(30 * Timer.ONE_SECOND);
        setJammedTimeout(5 * Timer.ONE_MINUTE);
    }

    @Override
    public String getActivity() {
        final var lease = negotiation.current();
        return (state == null ? "Not negotiated" : state)
                + (lease == null ? "; no valid control lease"
                        : "; ingress=" + lease.capabilities().observationIngress() + "; collection="
                                + lease.capabilities().selection().enabled())
                + "; capture=" + capture.status() + "; probes=" + probes.status();
    }

    @Override
    public int nextStep() {
        try {
            final long sentAtMs = System.currentTimeMillis();
            final long sentAtNanos = System.nanoTime();
            final var previous = lastSelection;
            final var capabilities = _mover.getMasterProxy().getWeatherCapabilities(_mover.getRoot());
            negotiation.update(capabilities, sentAtMs, sentAtNanos);
            if (previous == null || !capabilities.selection().enabled()
                    || !previous.masterEpoch().equals(capabilities.selection().masterEpoch())
                    || previous.revision() != capabilities.selection().revision()) {
                outbox.clear();
            }
            lastSelection = capabilities.selection();
            if (!capabilities.selection().enabled()) {
                probes.cancel();
            }
            final var lease = negotiation.current();
            probes.reconcile(lease);
            if (lease != null && capabilities.selection().enabled()) {
                for (final var job : capabilities.probeJobs()) {
                    probes.offer(job, lease);
                }
                final var results = probes.results();
                if (!results.isEmpty()) {
                    final var report = signer.sign(new ecmwf.ecpds.master.weather.WeatherBatch(1,
                            java.util.UUID.randomUUID(), capabilities.selection().masterEpoch(), _mover.getRoot(),
                            java.util.List.of(), "", 0, results));
                    final var receipt = _mover.getMasterProxy().deliverWeatherObservations(report);
                    receipt.validateFor(report);
                    probes.acknowledge(receipt.probeReceipts());
                }
            }
            ecmwf.ecpds.master.weather.WeatherDelivery.deliver(negotiation, outbox, signer,
                    batch -> _mover.getMasterProxy().deliverWeatherObservations(batch));
            failures = 0;
            setDelay((capabilities.selection().enabled() ? 10 : 30) * Timer.ONE_SECOND);
            if (!"AVAILABLE".equals(state)) {
                MoverServer._log.info("Network Weather control negotiated; observation ingress={}, collection={}",
                        capabilities.observationIngress(), capabilities.selection().enabled());
            }
            state = "AVAILABLE";
        } catch (final ecmwf.ecpds.master.weather.WeatherUnsupportedException e) {
            negotiation.invalidate();
            probes.cancel();
            outbox.clear();
            setDelay(5 * Timer.ONE_MINUTE);
            if (!"UNSUPPORTED".equals(state)) {
                MoverServer._log.warn("Network Weather negotiation unsupported; will retry in five minutes", e);
            }
            state = "UNSUPPORTED";
        } catch (final Exception e) {
            negotiation.invalidate();
            probes.cancel();
            failures = Math.min(failures + 1, 4);
            setDelay(Math.min(30 * Timer.ONE_SECOND * (1L << failures), 5 * Timer.ONE_MINUTE));
            MoverServer._log.warn("Network Weather negotiation failed; cached permission invalidated, will retry", e);
            state = "FAILED";
        }
        return NEXT_STEP_DELAY;
    }
}
