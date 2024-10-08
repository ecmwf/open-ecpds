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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;

/**
 * The Class WaitingThread.
 */
public abstract class WaitingThread extends ConfigurableRunnable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(WaitingThread.class);

    /** The _exception. */
    private Throwable _exception = null;

    /** The _time out expired. */
    private boolean _timeOutExpired = false;

    /**
     * Action.
     *
     * @throws java.lang.Exception
     *             the exception
     */
    public abstract void action() throws Exception;

    /**
     * Exec.
     *
     * @param timeout
     *            the timeout
     * @param asynchronous
     *            the asynchronous
     *
     * @return the long
     */
    public long exec(final long timeout, final boolean asynchronous) {
        if (asynchronous) {
            final var runnable = new AsynchronousThread(timeout);
            runnable.execute();
            return 0;
        }
        final var name = Format.getClassName(this);
        final var current = System.currentTimeMillis();
        _log.debug("Start activity " + name + " (timeout=" + timeout + ")");
        if (timeout > 0) {
            execute();
            try {
                if (join(timeout) && isAlive()) {
                    throw new TimeoutException("Timeout expired (" + Format.formatDuration(timeout) + ")");
                }
            } catch (final ExecutionException e) {
                _log.debug("Activity " + name + " failed)");
                _exception = e.getCause();
            } catch (final TimeoutException e) {
                interrupt();
                _exception = e;
                _log.debug("Activity " + name + " interrupted (timeout=" + timeout + ")");
                _timeOutExpired = true;
            } catch (final InterruptedException e) {
                _exception = e;
                _log.debug("Activity " + name + " interrupted)");
                _timeOutExpired = true;
            }
        } else {
            configurableRun();
        }
        final var duration = System.currentTimeMillis() - current;
        final var message = _exception != null ? _exception.getMessage() : null;
        _log.debug("Activity " + name + " "
                + (_exception == null ? "completed" : "failed" + (isNotEmpty(message) ? " (" + message + ")" : ""))
                + " (duration=" + duration + ",timeout=" + timeout + ")");
        return duration;
    }

    /**
     * {@inheritDoc}
     *
     * Configurable run.
     */
    @Override
    public void configurableRun() {
        try {
            action();
        } catch (final Throwable t) {
            _exception = t;
        }
    }

    /**
     * Completed.
     *
     * @throws java.lang.Throwable
     *             the throwable
     */
    public void completed() throws Throwable {
        if (_exception != null) {
            throw _exception;
        }
    }

    /**
     * Time out expired.
     *
     * @return true, if successful
     */
    public boolean timeOutExpired() {
        return _timeOutExpired;
    }

    /**
     * The Class AsynchronousThread.
     */
    public class AsynchronousThread extends ConfigurableRunnable {
        /** The _timeout. */
        private long _timeout = 0;

        /**
         * Instantiates a new asynchronous thread.
         *
         * @param timeout
         *            the timeout
         */
        AsynchronousThread(final long timeout) {
            _timeout = timeout;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            try {
                exec(_timeout, false);
                completed();
            } catch (final Throwable t) {
                _log.warn("Activity failed", t);
            }
        }
    }
}
