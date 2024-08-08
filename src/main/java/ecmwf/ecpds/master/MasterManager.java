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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Alias;
import ecmwf.common.database.Association;
import ecmwf.common.database.CatUrl;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.common.ecaccess.HandlerInterface;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.text.Format;

/**
 * The Class MasterManager.
 */
public final class MasterManager {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MasterManager.class);

    /** The Constant USE_CACHE. */
    private static final boolean USE_CACHE = Cnf.booleanAt("MasterManager", "useCache", false);

    /** The Constant DESTINATIONS_MAP. */
    private static final Map<String, DestinationCache> DESTINATIONS_MAP = new ConcurrentHashMap<>();

    /** The Constant ECUSERS_MAP. */
    private static final Map<String, ECUser> ECUSERS_MAP = new ConcurrentHashMap<>();

    /** The Constant CATURLS_LIST. */
    private static final ArrayList<CatUrl> CATURLS_LIST = new ArrayList<>();

    /** The cacheManagementThread. */
    private static CacheManagementThread cacheManagementThread;

    /** The connection. */
    private static MasterConnection connection = null;

    /** The root. */
    private static String root = null;

    /** The ready. */
    private static boolean ready = false;

    /**
     * Checks if is ready.
     *
     * @return true, if is ready
     */
    public static boolean isReady() {
        return !USE_CACHE || ready;
    }

    /**
     * Inits the.
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
     */
    private static synchronized void init() throws MasterException, RemoteException {
        if (Cnf.has("ECpds") && connection == null) {
            if (USE_CACHE && cacheManagementThread == null) {
                cacheManagementThread = new CacheManagementThread();
                cacheManagementThread.execute();
            }
            final var instance = StarterServer.getInstance();
            final var handler = instance instanceof HandlerInterface;
            try {
                connection = new MasterConnection(Cnf.at("ECpds", "host"), Cnf.at("ECpds", "port", (short) 6601),
                        handler ? (HandlerInterface) instance : null);
                if (handler) {
                    final var handlerInterface = (HandlerInterface) instance;
                    root = handlerInterface.getService() + "/" + handlerInterface.getRoot();
                    _log.info("HandlerInterface detected ({})", root);
                }
                try {
                    MasterManager.class.wait(2 * Timer.ONE_SECOND);
                } catch (final InterruptedException e) {
                    // Ignored!
                }
            } catch (final ConnectionException e) {
                connection = null;
                _log.warn("Initializing MasterConnection", e);
                throw new MasterException("MasterManager not initialized");
            }
        }
    }

    /**
     * Gets the ecaccess interface.
     *
     * @return the ecaccess interface
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
     */
    private static ECaccessInterface getECaccessInterface() throws MasterException, RemoteException {
        if (!Cnf.has("ECpds")) {
            return StarterServer.getInstance(ECaccessInterface.class);
        }
        if (connection == null) {
            init();
        }
        try {
            return (ECaccessInterface) connection.getConnection();
        } catch (final ConnectionException e) {
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Gets the db.
     *
     * @return the db
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public static DataBaseInterface getDB() throws DataBaseException {
        try {
            final var db = getECaccessInterface().getDataBaseInterface();
            return USE_CACHE ? new DataBaseProxy(db) : db;
        } catch (MasterException | RemoteException e) {
            throw new DataBaseException(e.getMessage(), e);
        }
    }

    /**
     * Gets the mi.
     *
     * @return the mi
     *
     * @throws MasterException
     *             the master exception
     */
    public static ManagementInterface getMI() throws MasterException {
        try {
            final var mi = getECaccessInterface().getManagementInterface();
            return USE_CACHE ? new ManagementProxy(mi) : mi;
        } catch (final RemoteException e) {
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Gets the ai.
     *
     * @return the ai
     *
     * @throws MasterException
     *             the master exception
     */
    public static DataAccessInterface getAI() throws MasterException {
        try {
            final var ai = getECaccessInterface().getAttachmentAccessInterface();
            return USE_CACHE ? new AttachmentAccessProxy(ai) : ai;
        } catch (final RemoteException e) {
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Gets the di.
     *
     * @return the di
     *
     * @throws MasterException
     *             the master exception
     */
    public static DataAccessInterface getDI() throws MasterException {
        try {
            final var di = getECaccessInterface().getDataFileAccessInterface();
            return USE_CACHE ? new AttachmentAccessProxy(di) : di;
        } catch (final RemoteException e) {
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Gets the monitoring caches.
     *
     * @return the monitoring caches
     */
    public static Collection<DestinationCache> getDestinationCaches() {
        return DESTINATIONS_MAP.values();
    }

    /**
     * Gets the monitoring cache.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the monitoring cache
     *
     * @throws MasterException
     *             the master exception
     */
    protected static DestinationCache getMonitoringCache(final String destinationName) throws MasterException {
        final var cache = DESTINATIONS_MAP.get(destinationName);
        if (cache != null) {
            return cache;
        }
        throw new MasterException("Destination " + destinationName + " not in cache");
    }

    /**
     * Gets the EC user.
     *
     * @param ecuserName
     *            the ecuser name
     *
     * @return the EC user
     */
    protected static ECUser getECUser(final String ecuserName) {
        return ECUSERS_MAP.get(ecuserName);
    }

    /**
     * Update cache.
     *
     * @param destinationSchedulerCache
     *            the destination scheduler cache
     */
    protected static void updateCache(final DestinationSchedulerCache destinationSchedulerCache) {
        final var cache = DESTINATIONS_MAP.get(destinationSchedulerCache.getDestinationName());
        if (cache != null) {
            cache.setDestinationSchedulerCache(destinationSchedulerCache);
        }
    }

    /**
     * Update cache.
     *
     * @param alias
     *            the alias
     */
    protected static void updateCache(final Alias alias) {
        final var cache = DESTINATIONS_MAP.get(alias.getDesName());
        if (cache != null) {
            cache.getAliases().put(alias.getDestinationName(), alias);
        }
    }

    /**
     * Update cache.
     *
     * @param host
     *            the host
     */
    protected static void updateCache(final Host host) {
        final var hostId = host.getName();
        synchronized (DESTINATIONS_MAP) {
            for (final DestinationCache cache : DESTINATIONS_MAP.values()) {
                final var associations = cache.getAssociations();
                if (associations.containsKey(hostId)) {
                    associations.get(hostId).setHost(host);
                }
            }
        }
    }

    /**
     * Update cache.
     *
     * @param destination
     *            the destination
     */
    protected static void updateCache(final Destination destination) {
        final var cache = DESTINATIONS_MAP.get(destination.getName());
        if (cache != null) {
            cache.setDestination(destination);
        }
    }

    /**
     * Update cache.
     *
     * @param association
     *            the association
     */
    protected static void updateCache(final Association association) {
        final var cache = DESTINATIONS_MAP.get(association.getDestinationName());
        if (cache != null) {
            cache.getAssociations().put(association.getHostName(), association);
        }
    }

    /**
     * Update cache.
     *
     * @param object
     *            the object
     */
    protected static void updateCache(final Object object) {
        _log.debug("updateCache: {}:{}", object.getClass().getSimpleName(), object);
        if (object instanceof final Alias alias) {
            updateCache(alias);
        } else if (object instanceof final Association association) {
            updateCache(association);
        } else if (object instanceof final Destination destination) {
            updateCache(destination);
        } else if (object instanceof final Host host) {
            updateCache(host);
        }
    }

    /**
     * Removes the from cache.
     *
     * @param alias
     *            the alias
     */
    protected static void removeFromCache(final Alias alias) {
        final var cache = DESTINATIONS_MAP.get(alias.getDesName());
        if (cache != null) {
            cache.getAliases().remove(alias.getDestinationName());
        }
    }

    /**
     * Removes the from cache.
     *
     * @param host
     *            the host
     */
    protected static void removeFromCache(final Host host) {
        final var hostId = host.getName();
        synchronized (DESTINATIONS_MAP) {
            for (final DestinationCache cache : DESTINATIONS_MAP.values()) {
                cache.getAssociations().remove(hostId);
            }
        }
    }

    /**
     * Removes the from cache.
     *
     * @param destination
     *            the destination
     */
    protected static void removeFromCache(final Destination destination) {
        DESTINATIONS_MAP.remove(destination.getName());
    }

    /**
     * Removes the from cache.
     *
     * @param association
     *            the association
     */
    protected static void removeFromCache(final Association association) {
        final var cache = DESTINATIONS_MAP.get(association.getDestinationName());
        if (cache != null) {
            cache.getAssociations().remove(association.getHostName());
        }
    }

    /**
     * Removes the from cache.
     *
     * @param object
     *            the object
     */
    protected static void removeFromCache(final Object object) {
        _log.debug("removeFromCache: {}:{}", object.getClass().getSimpleName(), object);
        if (object instanceof final Alias alias) {
            removeFromCache(alias);
        } else if (object instanceof final Association association) {
            removeFromCache(association);
        } else if (object instanceof final Destination destination) {
            removeFromCache(destination);
        } else if (object instanceof final Host host) {
            removeFromCache(host);
        }
    }

    /**
     * Insert in cache.
     *
     * @param alias
     *            the alias
     */
    protected static void insertInCache(final Alias alias) {
        final var cache = DESTINATIONS_MAP.get(alias.getDesName());
        if (cache != null) {
            cache.getAliases().put(alias.getDestinationName(), alias);
        }
    }

    /**
     * Insert in cache.
     *
     * @param association
     *            the association
     */
    protected static void insertInCache(final Association association) {
        _log.debug("Insert Association: {}", association);
        final var cache = DESTINATIONS_MAP.get(association.getDestinationName());
        if (cache != null) {
            cache.getAssociations().put(association.getHostName(), association);
        }
    }

    /**
     * Insert in cache.
     *
     * @param destinationCache
     *            the destination cache
     */
    protected static void insertInCache(final DestinationCache destinationCache) {
        _log.debug("Insert DestinationCache: {}", destinationCache);
        final var destinationName = destinationCache.getDestinationName();
        synchronized (DESTINATIONS_MAP) {
            final var localCache = DESTINATIONS_MAP.get(destinationName);
            if (localCache != null) {
                if (destinationCache.getCreationTime() <= localCache.getCreationTime()) {
                    // Nothing to do as the cache provided is older than the one we already have!
                    return;
                }
                destinationCache.setBadDataTransfersCount(localCache.getBadDataTransfersCount());
            }
            DESTINATIONS_MAP.put(destinationName, destinationCache);
        }
    }

    /**
     * Insert in cache.
     *
     * @param object
     *            the object
     */
    protected static void insertInCache(final Object object) {
        _log.debug("insertInCache: {}:{}", object.getClass().getSimpleName(), object);
        if (object instanceof final Alias alias) {
            insertInCache(alias);
        } else if (object instanceof final Association association) {
            insertInCache(association);
            // } else if (object instanceof Destination || object instanceof Host) {
            // _resetCaches(object);
        }
    }

    /**
     * Gets the cat urls.
     *
     * @return the cat urls
     */
    @SuppressWarnings("unchecked")
    protected static List<CatUrl> getCatUrls() {
        return (List<CatUrl>) CATURLS_LIST.clone();
    }

    /**
     * Reset caches.
     */
    protected static synchronized void resetCaches() {
        try {
            final var ecaccess = getECaccessInterface();
            final var management = ecaccess.getManagementInterface();
            final var destinationCaches = management.getDestinationCaches();
            final Map<String, DestinationCache> updatedCaches = new HashMap<>();
            var start = System.currentTimeMillis();
            synchronized (DESTINATIONS_MAP) {
                for (final DestinationCache destinationCache : destinationCaches.values()) {
                    final var destinationName = destinationCache.getDestinationName();
                    final var originalCache = DESTINATIONS_MAP.get(destinationName);
                    if (originalCache != null && originalCache.getCreationTime() > destinationCache.getCreationTime()) {
                        _log.debug("Keep current destination cache for Destination {}", destinationName);
                        updatedCaches.put(destinationName, originalCache);
                    }
                }
                DESTINATIONS_MAP.clear();
                DESTINATIONS_MAP.putAll(destinationCaches);
                DESTINATIONS_MAP.putAll(updatedCaches);
            }
            if (_log.isInfoEnabled()) {
                _log.info("DestinationCache(s) updated with {} element(s) in {}", destinationCaches.size(),
                        Format.formatDuration(start, System.currentTimeMillis()));
            }
            final var database = ecaccess.getDataBaseInterface();
            final var categories = database.getCatUrlArray();
            start = System.currentTimeMillis();
            synchronized (CATURLS_LIST) {
                CATURLS_LIST.clear();
                CATURLS_LIST.addAll(Arrays.asList(categories));
            }
            if (_log.isInfoEnabled()) {
                _log.info("CatUrl(s) updated with {} element(s) in {}", categories.length,
                        Format.formatDuration(start, System.currentTimeMillis()));
            }
            final var ecusers = new HashMap<String, ECUser>();
            for (final DestinationCache destinationCache : destinationCaches.values()) {
                final var ecuser = destinationCache.getDestination().getECUser();
                if (ecuser != null) {
                    ecusers.put(ecuser.getName(), ecuser);
                }
            }
            start = System.currentTimeMillis();
            synchronized (ECUSERS_MAP) {
                ECUSERS_MAP.clear();
                ECUSERS_MAP.putAll(ecusers);
            }
            if (_log.isInfoEnabled()) {
                _log.info("ECUser(s) updated with {} element(s) in {}", ecusers.size(),
                        Format.formatDuration(start, System.currentTimeMillis()));
            }
            if (!ready) {
                _log.info("MasterManager is now ready");
                ready = true;
            }
        } catch (final Throwable t) {
            _log.warn(t);
        }
    }

    /**
     * Gets the root.
     *
     * @return the root
     */
    public static String getRoot() {
        return root;
    }

    /**
     * The Class CacheManagementThread.
     */
    private static final class CacheManagementThread extends ConfigurableLoopRunnable {
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
            resetCaches();
        }
    }
}
