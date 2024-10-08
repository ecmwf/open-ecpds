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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.concurrent.TimeoutException;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.WakeupThread;
import ecmwf.common.text.Format;

/**
 * The Class StorageThread.
 *
 * @param <O>
 *            the generic type
 */
class StorageThread<O> extends WakeupThread {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(StorageThread.class);

    /** The _repository. */
    private final StorageRepository<O> _repository;

    /** The _run. */
    private boolean _run = true;

    /** The _duration. */
    private long _duration = 0;

    /** The _updated. */
    private long _updated = 0;

    /** The _not updated. */
    private long _notUpdated = 0;

    /**
     * Instantiates a new storage thread.
     *
     * @param repository
     *            the repository
     */
    StorageThread(final StorageRepository<O> repository) {
        _repository = repository;
    }

    /**
     * Gets the updated.
     *
     * @return the updated
     */
    long getUpdated() {
        return _updated;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    long getDuration() {
        return _duration;
    }

    /**
     * Shutdown.
     */
    void shutdown() {
        _run = false;
        wakeup();
        try {
            join(Timer.ONE_MINUTE);
        } catch (final TimeoutException e) {
            interrupt();
        } catch (final Exception e) {
        }
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    String getStatus() {
        return _updated + "/" + (_updated + _notUpdated)
                + (_updated > 0 ? "(" + Format.formatDuration(_duration / _updated) + ")" : "");
    }

    /**
     * {@inheritDoc}
     *
     * Configurable run.
     */
    @Override
    public void configurableRun() {
        while (_run || !_run && !_repository.isEmpty()) {
            if (_repository.isEmpty()
                    && waitFor(Cnf.at("StorageRepository", "storageThreadDelay", 15 * Timer.ONE_SECOND))) {
                continue;
            }
            O object = null;
            while ((object = _repository.getNextElement()) != null) {
                try {
                    final var start = System.currentTimeMillis();
                    _repository.update(object);
                    _duration += System.currentTimeMillis() - start;
                    object = null;
                    _updated++;
                } catch (final Throwable t) {
                    _notUpdated++;
                    _log.error("Updating (" + _notUpdated + ")", t);
                    if (_run) {
                        _repository.put(object);
                    }
                }
            }
        }
    }
}
