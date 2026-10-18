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

package ecmwf.ecpds.master.weather;

import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** A stuck RMI callback can occupy one daemon worker, never an unbounded queue or a delivery thread. */
public final class WeatherSourceProbe implements WeatherSourceRegistry.Probe, AutoCloseable {
    private final WeatherSourceRegistry.Probe probe;
    private final long timeoutMs;
    private final ThreadPoolExecutor worker = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new SynchronousQueue<>(), task -> {
                final var thread = new Thread(task, "WeatherSourceCallback");
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });

    public WeatherSourceProbe(WeatherSourceRegistry.Probe probe) {
        this(probe, 5_000);
    }

    WeatherSourceProbe(WeatherSourceRegistry.Probe probe, long timeoutMs) {
        this.probe = java.util.Objects.requireNonNull(probe);
        if (timeoutMs <= 0 || timeoutMs > 5_000) {
            throw new IllegalArgumentException("Invalid source verification deadline");
        }
        this.timeoutMs = timeoutMs;
    }

    @Override
    public WeatherSourceIdentity identify(Object registration, UUID challenge) throws Exception {
        final var result = worker.submit(() -> probe.identify(registration, challenge));
        try {
            return result.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof Exception failure) {
                throw failure;
            }
            throw new IllegalStateException("Network Weather source callback failed", e.getCause());
        } catch (TimeoutException e) {
            // Do not cancel and replace the worker: RMI may ignore interruption.
            throw new TimeoutException("Network Weather source callback exceeded its deadline");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public void close() {
        worker.shutdownNow();
    }
}
