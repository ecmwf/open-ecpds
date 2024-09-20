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

package ecmwf.common.ftpd;

import static ecmwf.common.text.Util.isNotEmpty;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.UserSession;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService;

/**
 * The Class PASS.
 */
final class PASS {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PASS.class);

    /** The Constant _cache. */
    private static final Map<String, String[]> _cache = new ConcurrentHashMap<>(); // {(remote.user.password,(count,token))...}

    /** The Constant _synchro. */
    private static final Map<String, Object> _synchro = new ConcurrentHashMap<>(); // {(user.password,synch)...}

    /** The _logged in. */
    private boolean _loggedIn = false;

    /** The _is password. */
    private boolean _isPassword = true;

    /** The _from cache. */
    private boolean _fromCache = false;

    /** The _key. */
    private String _key = null;

    /** The _token. */
    private String _token = null;

    /** The _session. */
    private UserSession _session = null;

    /**
     * Instantiates a new pass.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    PASS(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, PASS.class, parameter)) == null) {
            return;
        }
        _loggedIn = false; // assume they won't be able to log on.
        if (currentContext.user == null) {
            currentContext.respond(530, "Not logged in");
            return;
        }
        final String user;
        String domain = null;
        int pos;
        if ((pos = currentContext.user.indexOf("@")) > 0 || (pos = currentContext.user.indexOf("-")) > 0) {
            user = currentContext.user.substring(0, pos);
            domain = currentContext.user.substring(pos + 1);
            if (domain.length() == 0 || ".".equals(domain)) {
                domain = null;
            }
        } else {
            user = currentContext.user;
        }
        currentContext.remoteSite = currentContext.remoteIP.getHostName();
        final String restrict;
        if ((restrict = Cnf.at("FtpPlugin", "restrict." + user)) != null
                && !currentContext.remoteIP.getHostAddress().startsWith(restrict)) {
            _log.info(
                    "User " + user + " NOT logged in from " + currentContext.remoteSite + " (restricted access only)");
            currentContext.respond(530, "Restricted access only");
            return;
        }
        final var authMessage = _isAuthenticated(currentContext, user, parameter);
        if (!(authMessage.isEmpty()
                && DOMAIN.setDomain(currentContext, domain != null ? domain : _session.getDefaultDomain()))) {
            _log.info("User " + user + " NOT logged in from " + currentContext.remoteSite + " (login failed)");
            if (authMessage.isEmpty()) {
                currentContext.respond(530, "Invalid domain name");
            } else if (authMessage.startsWith("Maximum number of connections exceeded")) {
                currentContext.respond(421, authMessage);
            } else {
                currentContext.respond(530, authMessage);
            }
            _removeUser();
            currentContext.user = null;
            currentContext.authName = null; // This has not been authorised, kill it!
            return;
        }
        // Logged in.
        ThreadService.setCookie(currentContext.user);
        try {
            currentContext.clientSocket.setSoTimeout(7200000);
        } catch (final IOException e) {
            _log.warn(e);
        }
        final var msg = "User " + currentContext.user + " logged in from " + currentContext.remoteSite;
        _log.info(msg + (!_isPassword ? " (token)" : ""));
        currentContext.newEvent("login", !_isPassword ? "token" : null, false);
        if (_isPassword) {
            final var welcome = _session.getWelcome();
            if (isNotEmpty(welcome)) {
                Util.display(currentContext, welcome, 230, msg);
            } else {
                Util.display(currentContext,
                        new File(currentContext.browser ? Cnf.at("FtpPlugin", "browser", "browser.ftp")
                                : Cnf.at("FtpPlugin", "welcome", "welcome.ftp")),
                        230, msg);
            }
        } else {
            currentContext.respond(230, msg);
        }
        currentContext.dataSocket = new DataSocket();
        _loggedIn = true;
    }

    /**
     * Checks if is logged.
     *
     * @return true, if is logged
     */
    boolean isLogged() {
        return _loggedIn;
    }

    /**
     * Gets the user session.
     *
     * @return the user session
     */
    UserSession getUserSession() {
        return _session;
    }

    /**
     * Checks if is in cache.
     *
     * @param remote
     *            the remote
     * @param user
     *            the user
     * @param password
     *            the password
     *
     * @return the string
     */
    private String _isInCache(final String remote, final String user, final String password) {
        final String[] value;
        _key = remote + '.' + user + '.' + password;
        if ((value = _cache.get(_key)) != null) {
            _log.debug("Token found in cache " + value[1]);
            return value[1];
        }
        return null;
    }

    /**
     * Checks if is authenticated.
     *
     * @param currentContext
     *            the current context
     * @param user
     *            the user
     * @param password
     *            the password
     *
     * @return true, if successful
     */
    private String _isAuthenticated(final CurrentContext currentContext, final String user, String password) {
        final var syncroKey = user + "." + password;
        try {
            final var provider = NativeAuthenticationProvider.getInstance();
            Object inProgress;
            final String token;
            final var supportTicket = provider.supportTickets();
            // If the provider support the ticket then let's check if it is a
            // ticket or not? Otherwise it can only be a password!
            if (_isPassword = !supportTicket || provider.isPassword(user, password)) {
                synchronized (_synchro) {
                    if ((inProgress = _synchro.get(syncroKey)) == null) {
                        _synchro.put(syncroKey, inProgress = new Object());
                    }
                }
            } else {
                inProgress = new Object();
            }
            synchronized (inProgress) {
                if (supportTicket && _isPassword
                        && (token = _isInCache(currentContext.remoteSite, user, password)) != null) {
                    // It is a password and we already have a token for this
                    // password so let's use it to get a session!
                    password = "WWW" + token;
                    _fromCache = true;
                }
                final var notAToken = _isPassword && !_fromCache;
                try {
                    final var address = currentContext.remoteIP.getHostAddress();
                    _session = notAToken ? provider.getUserSession(address, user, password, "ftp", (Closeable) () -> {
                        currentContext.logout();
                        logout();
                    }) : provider.getUserSession(address, password, "ftp", (Closeable) this::logout);
                } catch (final EccmdException e) {
                    _log.warn("Not logged in", e);
                    return "Not logged in";
                }
                if (_session != null) {
                    currentContext.user = _session.getUser();
                    currentContext.session = _session;
                    // If we support the ticket and it is a password then we
                    // store it in the cache for the next time!
                    if (supportTicket && notAToken) {
                        _addUser(_session.getToken());
                    }
                    return "";
                }
            }
        } catch (final Exception e) {
            final var message = e.getMessage();
            if (message != null && message.startsWith("Maximum number of connections exceeded")) {
                return message;
            }
            _log.warn("Authenticating user " + user, e);
        } finally {
            synchronized (_synchro) {
                _synchro.remove(syncroKey);
            }
        }
        return "Not logged in";
    }

    /**
     * Adds the user.
     *
     * @param token
     *            the token
     */
    private void _addUser(final String token) {
        final String[] value;
        var count = 1;
        _token = token;
        if ((value = _cache.get(_key)) != null) {
            count = Integer.parseInt(value[0]) + 1;
        } else {
            _log.debug("Caching token " + _token);
        }
        _cache.put(_key, new String[] { String.valueOf(count), token });
    }

    /**
     * Checks if is from cache.
     *
     * @return true, if is from cache
     */
    boolean isFromCache() {
        return _fromCache;
    }

    /**
     * Logout.
     */
    void logout() {
        _session.close(_isPassword && _removeUser());
    }

    /**
     * Removes the user.
     *
     * @return true, if successful
     */
    private boolean _removeUser() {
        var count = 0;
        final String[] value;
        if (_key == null || _token == null) {
            return true;
        }
        if ((value = _cache.get(_key)) != null) {
            count = Integer.parseInt(value[0]) - 1;
        }
        if (count < 1) {
            _cache.remove(_key);
            _log.debug("Uncaching token {}", _token);
            return true;
        }
        if (value != null) {
            value[0] = String.valueOf(count);
            _cache.put(_key, value);
        }
        return false;
    }

    /**
     * Clear cache.
     */
    static void clearCache() {
        _cache.clear();
    }

    /**
     * Gets the cache connection count.
     *
     * @return the cache connection count
     */
    static int getCacheConnectionCount() {
        return _cache.size();
    }
}
