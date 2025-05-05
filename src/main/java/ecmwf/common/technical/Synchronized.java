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
 * This is a class that provides a synchronized mechanism for locking objects
 * based on their keys. It provides methods for acquiring and releasing locks on
 * objects based on their keys.
 *
 * The class maintains a map of instances, where each instance represents a
 * collection of keys and their corresponding locks. The getInstance method is
 * used to get an instance of the Synchronized class based on the given class
 * clazz. If an instance already exists for the class, it returns the existing
 * instance, otherwise, it creates a new instance and returns it.
 *
 * The lock method is used to acquire a lock on an object specified by a key. It
 * first synchronizes on the elements map and then gets the ObjectElement
 * associated with the key. If the ObjectElement does not exist for the key, a
 * new ObjectElement is created and added to the map, and the ObjectElement is
 * returned. If the ObjectElement already exists for the key, its subscription
 * count is incremented and it is returned.
 *
 * The free method is used to release a lock on an object specified by a key. It
 * first synchronizes on the elements map and gets the ObjectElement associated
 * with the key. If the ObjectElement exists for the key and its subscription
 * count is decremented to zero, it is removed from the map and true is
 * returned, otherwise false is returned.
 *
 * The getSize method is used to get the total number of elements for all
 * instances of the Synchronized class.
 *
 * The getMutex method is used to get a Mutex object associated with a key. The
 * Mutex object can be used to synchronize threads accessing the same key. *
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class Synchronized.
 */
public final class Synchronized {
    /** The map of instances. */
    private static final Map<String, Synchronized> instances = new ConcurrentHashMap<>();

    /** The elements. */
    private final Map<String, ObjectElement> elements = new ConcurrentHashMap<>();

    /**
     * Gets the single instance of Synchronized.
     *
     * @param clazz
     *            the clazz
     *
     * @return single instance of Synchronized
     */
    public static Synchronized getInstance(final Class<?> clazz) {
        return instances.computeIfAbsent(clazz.getName(), _ -> new Synchronized());
    }

    /**
     * Gets the total number of elements for all instances.
     *
     * @return total number of elements for all instances
     */
    public static long getSize() {
        var size = 0L;
        for (final Synchronized sync : instances.values()) {
            size += sync.elements.size();
        }
        return size;
    }

    /**
     * Gets the Mutex for the specified key.
     *
     * @param key
     *            the key
     *
     * @return the mutex
     */
    public Mutex getMutex(final Object key) {
        return new Mutex(this, key);
    }

    /**
     * Retrieve an object to synchronize for the given key.
     *
     * @param key
     *            the key
     *
     * @return the object
     */
    public Object lock(final Object key) {
        synchronized (elements) {
            final var string = key.toString();
            var element = elements.get(string);
            if (element == null) {
                elements.put(string, element = new ObjectElement());
            } else {
                element.subscribe();
            }
            return element;
        }
    }

    /**
     * Free the object related to the given key.
     *
     * @param key
     *            the key
     *
     * @return true, if successful
     */
    public boolean free(final Object key) {
        synchronized (elements) {
            final var string = key.toString();
            final var element = elements.get(string);
            if (element != null && element.unsubscribe()) {
                return elements.remove(string) != null;
            }
        }
        return false;
    }

    /**
     * The Class ObjectElement.
     */
    private static final class ObjectElement {
        /** The count. */
        private int count = 1;

        /**
         * Subscribe.
         */
        void subscribe() {
            count += 1;
        }

        /**
         * Unsubscribe.
         *
         * @return true, if successful
         */
        boolean unsubscribe() {
            return (count -= 1) <= 0;
        }
    }
}
