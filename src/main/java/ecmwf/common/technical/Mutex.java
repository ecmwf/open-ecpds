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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * The Mutex class represents a mutual exclusion lock for a specific key. It has a reference to a Synchronized object,
 * which is used to manage locks on a per-key basis. The lock() method of the Mutex object acquires the lock for the
 * associated key by calling the lock() method of the underlying Synchronized object. The free() method of the Mutex
 * object releases the lock for the associated key by calling the free() method of the underlying Synchronized object.
 * This class provides a way to synchronize access to a shared resource on a per-key basis, which can help prevent data
 * corruption and other synchronization-related problems. *
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public final class Mutex {
    /** The sync. */
    private final Synchronized sync;

    /** The key. */
    private final Object key;

    /**
     * Instantiates a new mutex.
     *
     * @param sync
     *            the sync
     * @param key
     *            the key
     */
    Mutex(final Synchronized sync, final Object key) {
        this.sync = sync;
        this.key = key;
    }

    /**
     * Lock.
     *
     * @return the object
     */
    public Object lock() {
        return sync.lock(key);
    }

    /**
     * Free.
     *
     * @return true, if successful
     */
    public boolean free() {
        return sync.free(key);
    }
}
