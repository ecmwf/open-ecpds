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
 * This class encapsulates a check.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.Serializable;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.monitor.module.OpsviewProvider;
import ecmwf.common.technical.Cnf;

/**
 * The Class MonitorManager.
 */
public final class MonitorManager implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2818183025188035706L;

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MonitorManager.class);

    /** The _provider. */
    private static MonitorInterface _provider = null;

    /** The Constant GREEN. */
    public static final int GREEN = 0;

    /** The Constant YELLOW. */
    public static final int YELLOW = 1;

    /** The Constant RED. */
    public static final int RED = 2;

    /** The Constant BLUE. */
    public static final int BLUE = 3;

    /** The Constant _colors. */
    private static final String[] _colors = { "green", "yellow", "red", "blue" };

    /** The Constant _activated. */
    private static final boolean _activated = Cnf.at("Monitor", "activated", false);

    /** The Constant _delay. */
    private static final long _delay = Cnf.durationAt("Monitor", "delay", 5 * Timer.ONE_MINUTE);

    /** The _debug. */
    private static boolean _debug = Cnf.at("Monitor", "debug", false);

    /** The Constant _dontSend. */
    private static final boolean _dontSend = Cnf.at("Monitor", "dontSend", false);

    /** The _name. */
    private String _name;

    /** The _service. */
    private String _service;

    /** The _status. */
    private int _status;

    /** The _comment. */
    private String _comment;

    /**
     * Instantiates a new monitor manager.
     *
     * @param name
     *            the name
     * @param service
     *            the service
     * @param status
     *            the status
     * @param comment
     *            the comment
     *
     * @throws MonitorException
     *             the monitor exception
     */
    public MonitorManager(final String name, final String service, final int status, final String comment)
            throws MonitorException {
        _name = name;
        _service = service;
        _status = _getColor(getColor(status));
        _comment = comment;
    }

    /**
     * Instantiates a new monitor manager. No default service, it must be provided during the subscription.
     *
     * @param name
     *            the name
     * @param status
     *            the status
     * @param comment
     *            the comment
     *
     * @throws MonitorException
     *             the monitor exception
     */
    public MonitorManager(final String name, final int status, final String comment) throws MonitorException {
        this(name, null, status, comment);
    }

    /**
     * Check if is activated.
     *
     * @throws MonitorException
     *             the monitor exception
     */
    static void checkIfIsActivated() throws MonitorException {
        if (!_activated) {
            throw new MonitorException("Monitoring not activated");
        }
    }

    /**
     * Sets the provider.
     *
     * @param provider
     *            the new provider
     */
    public static synchronized void setProvider(final MonitorInterface provider) {
        _provider = provider;
    }

    /**
     * Gets the provider.
     *
     * @return the provider
     */
    public static synchronized MonitorInterface getProvider() {
        if (_provider == null) {
            _provider = new OpsviewProvider();
        }
        return _provider;
    }

    /**
     * Gets the color.
     *
     * @param status
     *            the status
     *
     * @return the color
     *
     * @throws MonitorException
     *             the monitor exception
     */
    public static String getColor(final int status) throws MonitorException {
        if (status >= 0 && status < _colors.length) {
            return _colors[status];
        }
        throw new MonitorException("Invalid color code: " + status);
    }

    /**
     * Gets the color.
     *
     * @param status
     *            the status
     *
     * @return the int
     *
     * @throws MonitorException
     *             the monitor exception
     */
    private static int _getColor(final String status) throws MonitorException {
        for (var i = 0; i < _colors.length; i++) {
            if (status.equalsIgnoreCase(_colors[i])) {
                return i;
            }
        }
        throw new MonitorException("Invalid color string: " + status);
    }

    /**
     * _status.
     *
     * @param name
     *            the name
     * @param service
     *            the service
     * @param status
     *            the status
     * @param message
     *            the message
     *
     * @throws MonitorException
     *             the monitor exception
     */
    private static synchronized void _status(final String name, final String service, final int status,
            final String message) throws MonitorException {
        checkIfIsActivated();
        // Is it debug?
        if (_debug) {
            _log.debug("Notification: " + name + "," + service + "," + status + "," + message);
        }
        if (!_dontSend) {
            try {
                getProvider().sendMessage(name, service, status, message);
            } catch (final MonitorException e) {
                throw e;
            } catch (final Exception e) {
                throw new MonitorException(e.getMessage());
            }
        }
    }

    /**
     * Update.
     *
     * @param status
     *            the status
     * @param comment
     *            the comment
     *
     * @throws MonitorException
     *             the monitor exception
     */
    public void update(final int status, final String comment) throws MonitorException {
        _status(_name, _service, _status = _getColor(getColor(status)), _comment = comment);
    }

    /**
     * Update.
     *
     * @param status
     *            the status
     *
     * @throws MonitorException
     *             the monitor exception
     */
    public void update(final int status) throws MonitorException {
        _status(_name, _service, _status = _getColor(getColor(status)), _comment);
    }

    /**
     * Update.
     *
     * @param comment
     *            the comment
     *
     * @throws MonitorException
     *             the monitor exception
     */
    public void update(final String comment) throws MonitorException {
        _status(_name, _service, _status, _comment = comment);
    }

    /**
     * Update.
     *
     * @throws MonitorException
     *             the monitor exception
     */
    public void update() throws MonitorException {
        _status(_name, _service, _status, _comment);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return _comment;
    }

    /**
     * Sets the comment.
     *
     * @param comment
     *            the new comment
     */
    public void setComment(final String comment) {
        _comment = comment;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return _status;
    }

    /**
     * Gets the color.
     *
     * @return the color
     */
    public String getColor() {
        try {
            return getColor(_status).toUpperCase();
        } catch (final MonitorException e) {
            return "INVALID_COLOR";
        }
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getColor() + (isNotEmpty(_comment) ? " (" + _comment + ")" : "");
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the new status
     *
     * @throws MonitorException
     *             the monitor exception
     */
    public void setStatus(final int status) throws MonitorException {
        _status = _getColor(getColor(status));
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(final String name) {
        _name = name;
    }

    /**
     * Gets the service.
     *
     * @return the service
     */
    public String getService() {
        return _service;
    }

    /**
     * Sets the service.
     *
     * @param service
     *            the new service
     */
    public void setService(final String service) {
        _service = service;
    }

    /**
     * Checks if is activated.
     *
     * @return true, if is activated
     */
    public static boolean isActivated() {
        return _activated;
    }

    /**
     * Checks if is debug.
     *
     * @return true, if is debug
     */
    public static boolean isDebug() {
        return _debug;
    }

    /**
     * Sets the debug.
     *
     * @param activated
     *            the new debug
     */
    public static void setDebug(final boolean activated) {
        _debug = activated;
    }

    /**
     * Gets the delay.
     *
     * @return the delay
     */
    public static long getDelay() {
        return _delay;
    }

    /**
     * Checks if is green.
     *
     * @return true, if is green
     */
    public boolean isGreen() {
        return _status == GREEN;
    }

    /**
     * Checks if is red.
     *
     * @return true, if is red
     */
    public boolean isRed() {
        return _status == RED;
    }

    /**
     * Checks if is yellow.
     *
     * @return true, if is yellow
     */
    public boolean isYellow() {
        return _status == YELLOW;
    }
}
