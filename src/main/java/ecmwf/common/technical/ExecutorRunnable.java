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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class ExecutorRunnable.
 */
public abstract class ExecutorRunnable implements Runnable {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ExecutorRunnable.class);

    /** The manager. */
    private final ExecutorManager<? extends ExecutorRunnable> manager;

    /**
     * Instantiates a new executor runnable.
     *
     * @param manager
     *            the manager
     */
    protected ExecutorRunnable(final ExecutorManager<? extends ExecutorRunnable> manager) {
        this.manager = manager;
    }

    /**
     * Process.
     *
     * @throws Exception
     *             the exception
     */
    public abstract void process() throws Exception;

    /**
     * Run.
     */
    @Override
    public void run() {
        final var count = manager.getThreadCount();
        try {
            process();
        } catch (final Throwable t) {
            _log.warn("Error processing", t);
        } finally {
            // We have one Thread less!
            count.decrementAndGet();
        }
    }
}
