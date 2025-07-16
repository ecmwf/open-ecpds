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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Allow managing a cache of ECauthToken. If an entry has expired or is not found
 * then a call to the abstract method requestECauthToken is made to create a new
 * token.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
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
     * @throws java.io.IOException
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
     * @throws java.io.IOException
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
