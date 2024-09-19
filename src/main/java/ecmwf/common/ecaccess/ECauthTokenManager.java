/*
 * ECaccess Project - ECauthTokenManager.java
 *
 * Class: ecmwf.ecbatch.eis.rmi.client.ECauthTokenManager
 * Using JDK: 1.8.0_60
 *
 * Copyright (c) 2000-2016 Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 */

package ecmwf.common.ecaccess;

/**
 * The Class ECauthTokenManager. Allow managing a cache of ECauthToken. If an
 * entry has expired or is not found then a call to the abstract method
 * requestECauthToken is made to create a new token.
 *
 * @author <a href="mailto:syi@ecmwf.int">Laurent Gougeon</a>
 * @version 4.2.0
 */

import java.io.IOException;
import java.util.Hashtable;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;

/**
 * The Class ECauthTokenManager.
 */
public abstract class ECauthTokenManager {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECauthTokenManager.class);

    /** The _use ecauth cache. */
    private static boolean _useEcauthCache = Cnf.at("Other", "ecauthTokenCache", true);

    /** The _ecauth tokens cache. */
    private final Hashtable<String, ECauthToken> _ecauthTokensCache = new Hashtable<>();

    /** The _management thread. */
    private CacheManagementThread _managementThread = null;

    /**
     * Request ecauth token.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract ECauthToken requestECauthToken(String user) throws IOException;

    /**
     * Gets the ecauth token.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ECauthToken getECauthToken(final String user) throws IOException {
        var token = _useEcauthCache ? _ecauthTokensCache.get(user) : null;
        if (token == null || System.currentTimeMillis() - token.getTime() > Timer.ONE_MINUTE) {
            token = requestECauthToken(user);
            if (_useEcauthCache) {
                synchronized (_ecauthTokensCache) {
                    _ecauthTokensCache.put(user, token);
                    if (_managementThread == null) {
                        (_managementThread = new CacheManagementThread()).execute();
                    }
                }
            }
        } else {
            _log.debug("Using ECauthToken from cache for user: " + user);
        }
        return token;
    }

    /**
     * The Class CacheManagementThread.
     */
    private class CacheManagementThread extends ConfigurableLoopRunnable {
        /**
         * Instantiates a new cache management thread.
         */
        CacheManagementThread() {
            setPause(30 * Timer.ONE_SECOND);
        }

        /**
         * Configurable loop run.
         */
        @Override
        public void configurableLoopRun() {
            try {
                @SuppressWarnings("unchecked")
                final var clone = (Hashtable<String, ECauthToken>) _ecauthTokensCache.clone();
                for (final String userId : clone.keySet().toArray(new String[0])) {
                    final var token = _ecauthTokensCache.get(userId);
                    if (token != null && System.currentTimeMillis() - token.getTime() > Timer.ONE_MINUTE) {
                        _ecauthTokensCache.remove(userId);
                    }
                }
            } catch (final Throwable t) {
                _log.debug("Processing", t);
            }
        }
    }
}
