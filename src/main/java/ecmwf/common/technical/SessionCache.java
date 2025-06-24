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
 * This Java class implements a cache mechanism with session-like features for
 * objects of type S, indexed by keys of type K.
 *
 * The cache is implemented as a Map object with the keys of type K and values
 * of type Queue<CacheElement>. The Queue contains instances of a nested
 * CacheElement class, which contains a session object of type S and metadata
 * associated with it.
 *
 * The SessionCache class provides methods to add, retrieve, delete and update
 * cached session objects. The cached objects can be single entry caching (e.g.
 * with database objects) or session like caching (multiple connections/host).
 *
 * The class also provides methods to check if a session is still connected, to
 * disconnect a session and to get the number of sessions that match a given
 * criterion. Finally, the class includes a CacheManagementThread that manages
 * the expiration of sessions in the cache.
 *
 * @param <K> the key type
 * @param <S> the generic type
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.text.Format;

/**
 * The Class SessionCache.
 *
 * @param <K>
 *            the key type
 * @param <S>
 *            the generic type
 */
public class SessionCache<K, S> {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SessionCache.class);

    /** The Constant DEBUG_FREQUENCY. */
    private static final int DEBUG_FREQUENCY = Cnf.at("SessionCache", "debugFrequency", 1000000);

    /** The Constant LOG_ACTIVITY. */
    private static final boolean LOG_ACTIVITY = Cnf.at("SessionCache", "activityLogger", true);

    /** The Constant MINIMUM_PAUSE. */
    private static final long MINIMUM_PAUSE = Cnf.at("SessionCache", "minimumPause", 15 * Timer.ONE_SECOND);

    /** The debug. */
    private boolean debug = Cnf.at("SessionCache", "debug", false);

    /** The cache activities. */
    private final CacheActivity cacheActivity;

    /** The management thread. */
    private final CacheManagementThread managementThread;

    /** The cache. */
    private final Map<K, Queue<CacheElement>> cache = new ConcurrentHashMap<>();

    /** Map of key -> lock. */
    private final ConcurrentHashMap<K, ReentrantLock> locks = new ConcurrentHashMap<>();

    /** The maxQueueSize. */
    private final int maxQueueSize;

    /** The name. */
    private final String name;

    /**
     * Instantiates a new session cache. Default constructor for session like caching (multiple connections/host).
     */
    public SessionCache() {
        this.name = this.getClass().getSimpleName();
        this.cacheActivity = LOG_ACTIVITY ? new CacheActivity(name) : null;
        this.maxQueueSize = Integer.MAX_VALUE;
        this.managementThread = new CacheManagementThread(MINIMUM_PAUSE);
    }

    /**
     * Instantiates a new session cache. Default constructor for session like caching (multiple connections/host).
     *
     * @param name
     *            the name
     */
    public SessionCache(final String name) {
        this.name = name;
        this.cacheActivity = LOG_ACTIVITY ? new CacheActivity(name) : null;
        this.maxQueueSize = Integer.MAX_VALUE;
        this.managementThread = new CacheManagementThread(MINIMUM_PAUSE);
    }

    /**
     * Instantiates a new session cache. Constructor for single entry caching (e.g. with database objects).
     *
     * @param name
     *            the name
     * @param pause
     *            the pause
     */
    public SessionCache(final String name, final long pause) {
        this.name = name;
        this.cacheActivity = LOG_ACTIVITY ? new CacheActivity(name) : null;
        this.maxQueueSize = 1;
        this.managementThread = new CacheManagementThread(pause > MINIMUM_PAUSE ? pause : MINIMUM_PAUSE);
    }

    /**
     * Disconnect called when a session is removed.
     *
     * @param session
     *            the session
     */
    public void disconnect(final S session) {
        // Called when the session is removed from the cache
    }

    /**
     * Checks if a session is still connected.
     *
     * @param session
     *            the session
     *
     * @return true, if is connected
     */
    public boolean isConnected(final S session) {
        // If this is a connection then the implementation can check if it is still
        // alive!
        return true;
    }

    /**
     * If a session is set up to receive frequent updates, this method will be invoked to ensure that the session
     * remains active. For example, in an FTP session, this might involve sending a NOOP command.
     *
     * @param session
     *            the session
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void update(final S session) throws IOException {
        // If this is a connection then the implementation can do something to keep it
        // alive!
    }

    /**
     * Sets the debug mode.
     *
     * @param debug
     *            the new debug mode
     */
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    /**
     * This utility enables the creation of a key from a list of Objects. It can be utilized to construct a distinctive
     * key while registering a session.
     *
     * @param parameters
     *            the parameters
     *
     * @return the key
     */
    public static String getKey(final Object... parameters) {
        final var key = new StringBuilder();
        for (var i = 0; parameters != null && i < parameters.length; i++) {
            key.append(":").append(parameters[i]);
        }
        return key.toString();
    }

    /**
     * Removes one session object recorded with the specified key or return null.
     *
     * @param key
     *            the key
     *
     * @return the s
     */
    public S remove(final K key) {
        return remove(key, null);
    }

    /**
     * This method removes one session object associated with the given key. If the specified key does not have any
     * value mapped to it, the method returns the default value provided.
     *
     * @param key
     *            the key
     * @param defaultValue
     *            the default value returned if the key does not exists in the cache
     *
     * @return the s
     */
    public S remove(final K key, final S defaultValue) {
        final var list = cache.get(key);
        if (list == null) {
            return defaultValue;
        }
        synchronized (list) {
            final var element = list.poll();
            if (element == null || !isConnected(element.session)) {
                return defaultValue;
            }
            element.removed = true;
            if (LOG_ACTIVITY && _log.isDebugEnabled()) {
                cacheActivity.update(true);
            }
            return element.session;
        }
    }

    /**
     * This method retrieves a ReentrantLock for the specified key.
     *
     * @param key
     *            the key
     *
     * @return the ReentrantLock
     */
    public ReentrantLock getLock(final K key) {
        return locks.computeIfAbsent(key, _ -> new ReentrantLock());
    }

    /**
     * This method remove the ReentrantLock for the specified key.
     *
     * @param key
     *            the key
     */
    public void cleanupLock(final K key) {
        final var lock = locks.get(key);
        if (lock != null && !lock.isLocked()) {
            // Remove the lock only if no one is holding it
            locks.remove(key, lock);
        }
    }

    /**
     * This method retrieves a session from the cache that is registered with the specified key. If there is no session
     * registered with this key, the method returns null. It's important to note that the session is not removed from
     * the cache.
     *
     * @param key
     *            the key
     *
     * @return the s
     */
    public S retrieve(final K key) {
        return retrieve(key, null);
    }

    /**
     * This method fetches a session from the cache associated with the given key. If no session is found with the
     * specified key, the default value is returned. It's important to note that the session is not deleted from the
     * cache.
     *
     * @param key
     *            the key
     * @param defaultValue
     *            the default value returned if the key does not exists in the cache
     *
     * @return the s
     */
    public S retrieve(final K key, final S defaultValue) {
        final var list = cache.get(key);
        if (list == null) {
            return defaultValue;
        }
        synchronized (list) {
            final var element = list.peek();
            if (element == null || !isConnected(element.session)) {
                return defaultValue;
            }
            element.lastUpdate = System.currentTimeMillis();
            if (LOG_ACTIVITY && _log.isDebugEnabled()) {
                cacheActivity.update(true);
            }
            return element.session;
        }
    }

    /**
     * This method fetches a session from the cache associated with the given key. If no session is found with the
     * specified key, the default value is returned. It's important to note that the session is not deleted from the
     * cache.
     *
     * @param key
     *            the key
     * @param defaultValue
     *            the function to call in order to get the default value if the key does not exists in the cache
     *
     * @return the s
     */
    public S computeIfAbsent(final K key, final Function<K, S> defaultValue) {
        final var list = cache.get(key);
        final var element = list != null ? list.peek() : null;
        final S session;
        if ((element == null) || !isConnected(session = element.session)) {
            return defaultValue.apply(key);
        }
        element.lastUpdate = System.currentTimeMillis();
        if (LOG_ACTIVITY && _log.isDebugEnabled()) {
            cacheActivity.update(true);
        }
        return session;
    }

    /**
     * Remove all sessions registered in the cache with the specified key.
     *
     * @param key
     *            the key
     *
     * @return true, if the key exists and was removed
     */
    public boolean delete(final K key) {
        final var removed = cache.remove(key) != null;
        if (debug && removed) {
            _log.debug("Cache: {} -> Deleted key {}", name, key);
        }
        return removed;
    }

    /**
     * Checks if the given session S exists for the given key K in the cache. The comparator is used to compare the
     * session object with the sessions stored in the cache with the given key.
     *
     * @param key
     *            the key
     * @param session
     *            the session
     * @param comparator
     *            the comparator
     *
     * @return true, if successful
     */
    public boolean exists(final K key, final S session, final Comparator<S> comparator) {
        final var list = cache.get(key);
        if (list == null) {
            return false;
        }
        synchronized (list) {
            for (final CacheElement element : list) {
                if (comparator.compare(session, element.session) == 0 && !element.removed && !element.expired()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the cache contains the given key.
     *
     * @param key
     *            the key
     *
     * @return true, if successful
     */
    public boolean exists(final K key) {
        return cache.containsKey(key);
    }

    /**
     * Put the session in the cache, recorded with the given key and a timeout, used by the management thread to expire
     * the session.
     *
     * @param key
     *            the key
     * @param session
     *            the session
     * @param timeout
     *            the timeout
     *
     * @return true, if successful
     */
    public boolean put(final K key, final S session, final long timeout) {
        return put(key, session, timeout, -1);
    }

    /**
     * Put the session in the cache, recorded with the given key and a timeout, used by the management thread to expire
     * the session. If the update frequency is positive it is used by the management thread to regularly update the
     * session.
     *
     * @param key
     *            the key
     * @param session
     *            the session
     * @param timeout
     *            the timeout
     * @param updateFrequency
     *            the update frequency
     *
     * @return true, if successfully added to the cache
     */
    public boolean put(final K key, final S session, final long timeout, final long updateFrequency) {
        if (timeout > 0 && key != null && session != null) {
            final var added = cache.computeIfAbsent(key, _ -> new LinkedBlockingQueue<CacheElement>(maxQueueSize))
                    .offer(new CacheElement(session, timeout, updateFrequency));
            if (added) {
                if (debug) {
                    _log.debug("Cache: {} -> Added key {}", name, key);
                }
                if (LOG_ACTIVITY && _log.isDebugEnabled()) {
                    cacheActivity.update(false);
                }
                // Let's make sure the management thread is running!
                if (!managementThread.isStarted()) {
                    managementThread.setInheritCookie(false);
                    managementThread.execute();
                }
            }
            return added;
        }
        disconnect(session);
        return false;
    }

    /**
     * The Class CacheElement.
     */
    private final class CacheElement {
        /** The session. */
        private final S session;

        /** The time out. */
        private final long timeOut;

        /** The update frequency. */
        private final long updateFrequency;

        /** The start time. */
        private final long startTime;

        /** The last update. */
        private long lastUpdate = -1;

        /** The removed. */
        private boolean removed = false;

        /**
         * Instantiates a new cache element.
         *
         * @param session
         *            the session
         * @param timeOut
         *            the time out
         * @param updateFrequency
         *            the update frequency
         */
        CacheElement(final S session, final long timeOut, final long updateFrequency) {
            this.session = session;
            this.timeOut = timeOut;
            this.updateFrequency = updateFrequency;
            this.startTime = System.currentTimeMillis();
            this.lastUpdate = startTime;
        }

        /**
         * Check if the element is expired.
         *
         * @return true, if expired
         */
        boolean expired() {
            var expired = System.currentTimeMillis() - startTime > timeOut || !isConnected(session);
            if (!expired && updateFrequency > 0) {
                try {
                    if (System.currentTimeMillis() - lastUpdate > updateFrequency) {
                        update(session);
                        lastUpdate = System.currentTimeMillis();
                    }
                } catch (final Throwable t) {
                    _log.warn("Cache: {} -> Session not updated", name, t);
                    expired = true;
                }
            }
            return expired;
        }
    }

    /**
     * The Class CacheManagementThread.
     */
    private final class CacheManagementThread extends ConfigurableLoopRunnable {

        /**
         * Instantiates a new cache management thread.
         *
         * @param pause
         *            the pause
         */
        CacheManagementThread(final long pause) {
            setPause(pause);
        }

        /**
         * Disconnect expired sessions.
         *
         * @param keys
         *            the keys
         */
        private void disconnectExpiredSessions(final List<K> keys) {
            keys.forEach(key -> {
                final var list = cache.get(key);
                if (list != null) {
                    list.removeIf(element -> {
                        if (element.removed || !element.expired()) {
                            return element.removed;
                        }
                        if (debug) {
                            _log.debug("Cache: {} -> Disconnect expired session for {} ({}): {} left", name, key,
                                    Format.getClassName(element.session), list.size() - 1);
                        }
                        disconnect(element.session);
                        return true;
                    });
                }
            });
        }

        /**
         * Removes the empty entries.
         *
         * @param keys
         *            the keys
         */
        private void removeEmptyEntries(final List<K> keys) {
            keys.forEach(key -> {
                final var list = cache.get(key);
                if (list != null && list.isEmpty()) {
                    delete(key);
                }
            });
        }

        /**
         * Iterate through the sessions stored in the cache, disconnect any sessions that have expired, and subsequently
         * remove any empty lists of sessions.
         *
         * @see ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable# configurableLoopRun()
         */
        @Override
        public void configurableLoopRun() {
            try {
                final List<K> keys = new ArrayList<>(cache.keySet());
                disconnectExpiredSessions(keys);
                removeEmptyEntries(keys);
            } catch (final Throwable t) {
                _log.warn("Cache: {} -> Processing", name, t);
            }
        }
    }

    /**
     * This class is tracking the activity of a sesion cache.
     *
     * The class has several fields, including a DecimalFormat object called "format" (used to format numbers with two
     * decimal points), a "startup" long that stores the system time when the object was created, and two AtomicLong
     * objects called "reads" and "writes" that are used to track the number of read and write operations performed
     * against the cache.
     *
     * The class has a method called "getAvgPerSec" that takes an AtomicLong object called "count" and returns a string
     * representation of the average number of operations per second for that count, based on the time elapsed since the
     * object was created.
     *
     * The main method in the class is "update", which takes a boolean argument called "read" that indicates whether a
     * read or write operation has been performed. The method updates the appropriate AtomicLong object and, if the
     * number of operations is a multiple of a constant called "DEBUG_FREQUENCY", it logs a message to a logger called
     * "_log" indicating the number of reads and writes, along with their respective average operations per second.
     */
    private static final class CacheActivity {

        /** The Constant format. */
        static final DecimalFormat format = new DecimalFormat("0.00");

        /** The startup. */
        final long startup = System.currentTimeMillis();

        /** The reads. */
        final AtomicLong reads = new AtomicLong(0);

        /** The writes. */
        final AtomicLong writes = new AtomicLong(0);

        /** The cache name. */
        final String cacheName;

        /**
         * Instantiates a new cache activity.
         *
         * @param cacheName
         *            the cache name
         */
        CacheActivity(final String cacheName) {
            this.cacheName = cacheName;
        }

        /**
         * Gets the avg per sec.
         *
         * @param count
         *            the count
         *
         * @return the avg per sec
         */
        String getAvgPerSec(final AtomicLong count) {
            return format.format(count.get() / ((System.currentTimeMillis() - startup) / 1000d));
        }

        /**
         * Update.
         *
         * @param read
         *            the read
         */
        void update(final boolean read) {
            if ((read ? reads : writes).updateAndGet(c -> c == Long.MAX_VALUE ? 1 : c + 1) % DEBUG_FREQUENCY == 0) {
                _log.debug("Cache {}: Reads: {} ({}/sec avg) Writes: {} ({}/sec avg)", cacheName, reads,
                        getAvgPerSec(reads), writes, getAvgPerSec(writes));
            }
        }
    }
}
