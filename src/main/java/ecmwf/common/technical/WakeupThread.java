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

import ecmwf.common.technical.ThreadService.ConfigurableRunnable;

/**
 * The Class WakeupThread.
 */
public abstract class WakeupThread extends ConfigurableRunnable {
    /** The _sleep. */
    private boolean _sleep = false;

    /** The _wakeup. */
    private boolean _wakeup = false;

    /** The _lock. */
    private final Object _lock = new Object();

    /** The _on hold. */
    private boolean _onHold = false;

    /**
     * Checks if is on hold.
     *
     * @return true, if is on hold
     */
    public boolean isOnHold() {
        return _onHold;
    }

    /**
     * Sets the onhold.
     *
     * @param hold
     *            the new onhold
     */
    public void setOnhold(final boolean hold) {
        _onHold = hold;
    }

    /**
     * Sets the sleep.
     *
     * @param sleep
     *            the sleep
     *
     * @return true, if successful
     */
    private boolean _setSleep(final boolean sleep) {
        return _sleep = _wakeup ? _wakeup = false : sleep;
    }

    /**
     * Checks if is sleeping.
     *
     * @return true, if is sleeping
     */
    public boolean isSleeping() {
        return _sleep;
    }

    /**
     * Wait for.
     *
     * @param delay
     *            the delay
     *
     * @return true, if successful
     */
    public boolean waitFor(final long delay) {
        boolean sleep;
        synchronized (_lock) {
            if (sleep = _setSleep(true)) {
                try {
                    _lock.wait(delay);
                } catch (final InterruptedException e) {
                } finally {
                    _setSleep(false);
                }
            }
        }
        return sleep;
    }

    /**
     * Wakeup.
     */
    public void wakeup() {
        synchronized (_lock) {
            if (_onHold) {
                return;
            }
            if (_sleep) {
                _lock.notify();
            } else {
                _wakeup = true;
            }
        }
    }
}
