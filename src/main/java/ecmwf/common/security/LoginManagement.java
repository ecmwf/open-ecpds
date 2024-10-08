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

package ecmwf.common.security;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.text.Format;

/**
 * The Class LoginManagement.
 */
public final class LoginManagement {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(LoginManagement.class);

    /** The Constant _active. Do we use the login management feature? */
    private static final boolean _active = Cnf.at("LoginManagement", "active", false);

    /**
     * The Constant _timer. When a host has been sitting in the queue for this time without any attempts then it is
     * removed from the banned list (in seconds).
     */
    private static final long _timer = Cnf.at("LoginManagement", "timer", 3600) * Timer.ONE_SECOND;

    /**
     * The Constant _whitelist. This list overrides the blacklist and allow accepting all connections specified in the
     * list without checks.
     */
    private static final List<String> _whitelist = Cnf.listAt("LoginManagement", "whitelist");

    /**
     * The Constant _max. When a host has had more than this number of bad attempts in a row then we don't allow a new
     * incoming connection.
     */
    private static final int _max = Cnf.at("LoginManagement", "max", 20);

    /**
     * The Constant _delay. Minimum delay when a password is wrong (in seconds).
     */
    private static final long _delay = Cnf.at("LoginManagement", "delay", 4) * Timer.ONE_SECOND;

    /** The Constant _wait. Delay when a password is banned (in seconds)! */
    private static final long _wait = Cnf.at("LoginManagement", "wait", 300) * Timer.ONE_SECOND;

    /** The Constant _cache. The cache used to keep the failed login entries. */
    private static final Map<String, LoginEntry> _cache = new ConcurrentHashMap<>();

    /**
     * The Constant _thread. The main thread used to purge the login entries.
     */
    private static final LoginManagementThread _thread = new LoginManagementThread();

    static {
        _thread.setPriority(Thread.MIN_PRIORITY);
        _thread.execute();
    }

    /**
     * Remove all the hosts from the banned list.
     */
    public static void clear() {
        _cache.clear();
        _log.debug("Removed all entries");
    }

    /**
     * Remove the specified host from the banned list.
     *
     * @param host
     *            the host
     */
    public static void clear(final String host) {
        _cache.remove(host);
        _log.debug("Removed entry for host " + host + " (" + _cache.size() + " in queue)");
    }

    /**
     * Increment. Called when there is an error trying to login from this host (e.g. password refused or user not
     * registered). This method only record the error if the login management module is activated.
     *
     * @param host
     *            the host
     */
    public static void increment(final String host) {
        if (_active) {
            try {
                var duration = _getLoginEntry(host).increment() * Timer.ONE_SECOND;
                if (duration < _delay) {
                    duration = _delay;
                }
                _log.debug("Delay for " + host + " (" + Format.formatDuration(duration) + ")");
                Thread.sleep(duration);
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * Check. Called before to start the login process to make sure this host is not already banned.
     *
     * @param host
     *            the host
     *
     * @return true, if successful
     */
    public static boolean check(final String host) {
        final var entry = _cache.get(host);
        if (entry == null) {
            // Not banned!
            return true;
        }
        if (System.currentTimeMillis() - entry.getStart() > _timer) {
            // This host has been banned for a long time with no other try
            // so let's give it a new chance!
            clear(host);
            return true;
        } else if (entry.getAttempt() > _max) {
            // We had too many attempts so let's refuse the check!
            _log.debug("Delay for " + host + " (" + Format.formatDuration(_wait) + ") with too many attemps ("
                    + entry.getAttempt() + ")");
            try {
                Thread.sleep(_wait);
            } catch (final InterruptedException e) {
            }
            return false;
        } else {
            // The number of attempts is still low so let's allow the
            // attempt!
            return true;
        }
    }

    /**
     * Checks if the password has the right format.
     *
     * @param passcode
     *            the passcode
     *
     * @return true, if it looks like a valid passcode
     */
    public static boolean isPasscode(final String passcode) {
        if (passcode == null || !(passcode.length() == 8 || passcode.length() == 6)) {
            return false;
        }
        for (var i = 0; i < passcode.length(); i++) {
            if (!Character.isDigit(passcode.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called to check if the password looks good and if it does not: increment the banned counter and delay the answer.
     *
     * @param host
     *            the host
     * @param user
     *            the user
     * @param passcode
     *            the passcode
     *
     * @throws ecmwf.common.ecaccess.EccmdException
     *             the eccmd exception
     */
    public static void check(final String host, final String user, final String passcode) throws EccmdException {
        // Is this host white listed?
        // Special case for NX!
        if (_whitelist.contains(host) || (("nx".equals(user) || "127.0.0.1".equals(host)) && passcode == null)) {
            return;
        }
        // We don't accept root access ever nor empty or null password!
        if ("root".equalsIgnoreCase(user) || passcode == null || passcode.length() == 0) {
            increment(host);
            throw new EccmdException("Passcode rejected");
        }
        // We might have the login/password in the same string
        // (user@password)?
        final var pos = passcode.indexOf('@');
        if (!isPasscode(pos == -1 ? passcode : passcode.substring(0, pos))) {
            increment(host);
            throw new EccmdException("Passcode rejected");
        }
    }

    /**
     * Gets the login entry. If the entry exists then return it otherwise create it and return it.
     *
     * @param host
     *            the host
     *
     * @return the login entry
     */
    private static LoginEntry _getLoginEntry(final String host) {
        var entry = _cache.get(host);
        if (entry == null) {
            _cache.put(host, entry = new LoginEntry());
            _log.debug("Added entry for host " + host + " (" + _cache.size() + " in queue)");
        }
        return entry;
    }

    /**
     * The Class LoginEntry.
     */
    private static final class LoginEntry {
        /** The _start. */
        private long _start = System.currentTimeMillis();

        /** The _attempt. */
        private int _attempt = 0;

        /**
         * Gets the attempt.
         *
         * @return the attempt
         */
        int getAttempt() {
            return _attempt;
        }

        /**
         * Gets the start.
         *
         * @return the start
         */
        long getStart() {
            return _start;
        }

        /**
         * Increment.
         *
         * @return the int
         */
        int increment() {
            // Reset the date for the last update and increment the number of
            // attempts!
            _start = System.currentTimeMillis();
            _attempt = _attempt + 1;
            return _attempt;
        }
    }

    /**
     * The Class LoginManagementThread.
     */
    private static final class LoginManagementThread extends ConfigurableLoopRunnable {
        /**
         * Instantiates a new login management thread.
         */
        LoginManagementThread() {
            setPause(Cnf.at("LoginManagement", "delay", 600000));
        }

        /**
         * Configurable loop run.
         */
        @Override
        public void configurableLoopRun() {
            try {
                for (final String host : new Vector<>(_cache.keySet())) {
                    // Let's check the host so that it will be removed from the
                    // banned list if it did not have any recent attempts!
                    check(host);
                }
            } catch (final Throwable t) {
                _log.warn(t);
            }
        }
    }
}
