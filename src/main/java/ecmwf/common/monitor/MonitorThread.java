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

package ecmwf.common.monitor;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * The Class MonitorThread. This thread is constantly checking internal
 * components which requires monitoring and send notifications to the monitoring
 * application using the selected provider.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;

/**
 * The Class MonitorThread.
 */
public final class MonitorThread extends ConfigurableLoopRunnable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MonitorThread.class);

    /** The _this. */
    private static MonitorThread _this;

    /**
     * The Class MonitorEntry.
     */
    private static final class MonitorEntry {
        /** The callback. */
        private final MonitorCallback callback;

        /** The service. */
        private final String service;

        /**
         * Instantiates a new monitor entry.
         *
         * @param callback
         *            the callback
         * @param type
         *            the type
         */
        MonitorEntry(final MonitorCallback callback, final String type) {
            this.callback = callback;
            this.service = type;
        }
    }

    /** The _callbacks. */
    private final Map<String, MonitorEntry> _callbacks = new ConcurrentHashMap<>();

    /**
     * Instantiates a new monitor thread.
     */
    private MonitorThread() {
        setPause(MonitorManager.getDelay());
    }

    /**
     * Gets the single instance of MonitorThread.
     *
     * @return single instance of MonitorThread
     *
     * @throws ecmwf.common.monitor.MonitorException
     *             the monitor exception
     */
    public static synchronized MonitorThread getInstance() throws MonitorException {
        MonitorManager.checkIfIsActivated();
        if (_this == null) {
            _this = new MonitorThread();
            _this.execute();
        }
        return _this;
    }

    /**
     * Subscribe.
     *
     * @param name
     *            the name
     * @param callback
     *            the callback
     */
    public void subscribe(final String name, final MonitorCallback callback) {
        subscribe(name, null, callback);
    }

    /**
     * Subscribe.
     *
     * @param name
     *            the name
     * @param service
     *            the service
     * @param callback
     *            the callback
     */
    public void subscribe(final String name, final String service, final MonitorCallback callback) {
        if (MonitorManager.isDebug()) {
            _log.debug("Subscribing " + name + (service != null ? " (" + service + ")" : ""));
        }
        _callbacks.put(name, new MonitorEntry(callback, service));
    }

    /**
     * Un-subscribe.
     *
     * @param name
     *            the name
     */
    public void unSubscribe(final String name) {
        if (MonitorManager.isDebug()) {
            _log.debug("UnSubscribing " + name);
        }
        _callbacks.remove(name);
    }

    /**
     * Un-subscribe all.
     *
     * @param clazz
     *            the clazz
     */
    public void unSubscribeAll(final Class<?> clazz) {
        if (MonitorManager.isDebug()) {
            _log.debug("UnSubscribingAll");
        }
        final List<String> toRemove = new ArrayList<>();
        for (final String key : _callbacks.keySet()) {
            if (clazz.isInstance(_callbacks.get(key))) {
                toRemove.add(key);
            }
        }
        for (final String key : toRemove) {
            _callbacks.remove(key);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Configurable loop run.
     */
    @Override
    public void configurableLoopRun() {
        try {
            for (final String key : new ArrayList<>(_callbacks.keySet())) {
                final MonitorEntry entry;
                try {
                    if ((entry = _callbacks.get(key)) != null) {
                        final var manager = entry.callback.getMonitorManager(key);
                        if (manager.getService() == null) {
                            // There is no service defined in the
                            // MonitorManager so we should find one defined
                            // during subscription!
                            if (entry.service != null) {
                                // We set the one defined during the
                                // subscription!
                                manager.setService(entry.service);
                            } else {
                                // We can't send the notification as we have
                                // no service defined!
                                _log.warn("No service defined for: " + key);
                            }
                        }
                        // Let's update the event!
                        manager.update();
                    } else {
                        _log.warn("Callback not found: " + key);
                    }
                } catch (final Throwable t) {
                    _log.warn("Sending status for " + key, t);
                }
            }
        } catch (final Throwable t) {
            _log.warn("Sending status", t);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Configurable loop end.
     */
    @Override
    public void configurableLoopEnd() {
        _callbacks.clear();
    }
}
