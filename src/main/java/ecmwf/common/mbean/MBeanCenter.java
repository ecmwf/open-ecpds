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

package ecmwf.common.mbean;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class MBeanCenter.
 */
public final class MBeanCenter implements NotificationListener {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MBeanCenter.class);

    /** The _instance. */
    private static MBeanCenter instance;

    /** The _m beans. */
    private final Map<String, ObjectName> mBeans;

    /** The _m bean server. */
    private final MBeanServer mBeanServer;

    /** The _timer. */
    private Timer timer = null;

    /** The _timer url. */
    private String timerURL = null;

    /**
     * Instantiates a new m bean center.
     *
     * @param mBeanServer
     *            the MBean server
     */
    private MBeanCenter(final MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
        this.mBeans = new ConcurrentHashMap<>();
    }

    /**
     * Creates the MBean center.
     *
     * @param mBeanServer
     *            the MBean server
     *
     * @return the MBean center
     */
    public static synchronized MBeanCenter createMBeanCenter(final MBeanServer mBeanServer) {
        if (instance == null) {
            instance = new MBeanCenter(mBeanServer);
        }
        return instance;
    }

    /**
     * Gets the MBean center.
     *
     * @return the MBean center
     *
     * @throws InstanceNotFoundException
     *             the instance not found exception
     */
    public static MBeanCenter getMBeanCenter() throws InstanceNotFoundException {
        if (instance == null) {
            throw new InstanceNotFoundException("MBeanCenter not initialized");
        }
        return instance;
    }

    /**
     * Register m bean.
     *
     * @param object
     *            the object
     * @param URL
     *            the url
     *
     * @throws NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws MBeanRegistrationException
     *             the MBean registration exception
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    public void registerMBean(final Object object, final String URL) throws NotCompliantMBeanException,
            InstanceAlreadyExistsException, MBeanRegistrationException, MalformedObjectNameException {
        final var objectName = new ObjectName(URL);
        mBeans.put(URL, objectName);
        mBeanServer.registerMBean(object, objectName);
    }

    /**
     * Register m bean timer.
     *
     * @param URL
     *            the url
     *
     * @throws NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws MBeanRegistrationException
     *             the MBean registration exception
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    public synchronized void registerMBeanTimer(final String URL) throws NotCompliantMBeanException,
            InstanceAlreadyExistsException, MBeanRegistrationException, MalformedObjectNameException {
        if (timer != null) {
            throw new InstanceAlreadyExistsException("Timer already started");
        }
        timer = new Timer();
        timerURL = URL;
        registerMBean(timer, timerURL);
        timer.addNotificationListener(this, null, null);
        timer.start();
    }

    /**
     * Unregister m bean.
     *
     * @param URL
     *            the url
     *
     * @throws MBeanRegistrationException
     *             the MBean registration exception
     * @throws InstanceNotFoundException
     *             the instance not found exception
     */
    public void unregisterMBean(final String URL) throws MBeanRegistrationException, InstanceNotFoundException {
        mBeanServer.unregisterMBean(mBeans.remove(URL));
        if (timerURL != null && URL.equals(timerURL)) {
            stopTimer();
        }
    }

    /**
     * Unregister m beans.
     */
    public synchronized void unregisterMBeans() {
        for (final ObjectName objectName : mBeans.values()) {
            try {
                mBeanServer.unregisterMBean(objectName);
            } catch (final Exception e) {
                _log.debug(e);
            }
        }
        mBeans.clear();
        stopTimer();
    }

    /**
     * Gets the mbean server.
     *
     * @return the MBean server
     */
    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    /**
     * _stop timer.
     */
    private synchronized void stopTimer() {
        if (timer != null) {
            timer.removeAllNotifications();
            timer.stop();
            timerURL = null;
            timer = null;
        }
    }

    /**
     * Schedule notifications.
     *
     * @param listener
     *            the listener
     * @param date
     *            the date
     * @param period
     *            the period
     * @param message
     *            the message
     *
     * @return the integer
     *
     * @throws InstanceNotFoundException
     *             the instance not found exception
     */
    public Integer scheduleNotifications(final MBeanListener listener, final Date date, final long period,
            final String message) throws InstanceNotFoundException {
        if (timer == null) {
            throw new InstanceNotFoundException("Timer not started");
        }
        if (period <= 0) {
            _log.debug("Schedule call of {} on {} disabled", message, Format.getClassName(listener));
            return null;
        }
        var start = new Date().getTime();
        if (date == null || (start = date.getTime()) < System.currentTimeMillis()) {
            start += period;
        }
        final var startDate = new Date(start);
        _log.debug("Start date: {}", () -> Format.formatTime(startDate.getTime()));
        final var id = timer.addNotification(Format.getClassName(listener), message, listener, startDate, period, 0,
                true);
        if (_log.isDebugEnabled()) {
            _log.debug("Schedule call of {} on {} each {} (next start: {})",
                    Format.formatTime(timer.getDate(id).getTime()), message, Format.getClassName(listener),
                    Format.formatDuration(0, period));
        }
        return id;
    }

    /**
     * Removes the notifications.
     *
     * @param id
     *            the id
     *
     * @throws InstanceNotFoundException
     *             the instance not found exception
     */
    public void removeNotifications(final Integer id) throws InstanceNotFoundException {
        if (timer == null) {
            throw new InstanceNotFoundException("Timer not started");
        }
        _log.debug("Unschedule call: {}", id);
        timer.removeNotification(id);
    }

    /**
     * Handle notification.
     *
     * @param notification
     *            the notification
     * @param handback
     *            the handback
     */
    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        try {
            if (notification.getUserData() instanceof final MBeanListener listener) {
                listener.handleNotification(notification.getMessage());
            }
        } catch (final Exception e) {
            _log.warn("handleNotification", e);
        }
    }
}
