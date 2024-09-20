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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;

/**
 * The Class ProgressThread.
 */
final class ProgressThread extends ConfigurableLoopRunnable {
    /** The _handler. */
    private final ProgressHandler _handler;

    /** The _progress. */
    private final ProgressInterface _progress;

    /**
     * Instantiates a new progress thread.
     *
     * @param handler
     *            the handler
     * @param progress
     *            the progress
     */
    ProgressThread(final ProgressHandler handler, final ProgressInterface progress) {
        setPause(handler.getDelay());
        _handler = handler;
        _progress = progress;
    }

    /**
     * Configurable loop run.
     */
    @Override
    public void configurableLoopRun() {
        _handler.update(_progress);
    }

    /**
     * Configurable loop end.
     */
    @Override
    public void configurableLoopEnd() {
        _handler.update(_progress);
    }
}
