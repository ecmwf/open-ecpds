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

import javax.management.timer.Timer;

/**
 * The Class DestinationCache.
 */
public final class DestinationCache extends DestinationDataBaseCache {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -9122391317666032666L;

    /** The _destination scheduler cache. */
    private DestinationSchedulerCache _destinationSchedulerCache = null;

    /** The creation time . */
    private final long _creationTime = System.currentTimeMillis();

    /** The _last destination scheduler cache update. */
    private long _lastDestinationSchedulerCacheUpdate = System.currentTimeMillis();

    /**
     * Gets the destination scheduler cache.
     *
     * @return the destination scheduler cache
     */
    public DestinationSchedulerCache getDestinationSchedulerCache() {
        return _destinationSchedulerCache;
    }

    /**
     * Gets the creation time.
     *
     * @return time
     */
    public long getCreationTime() {
        return _creationTime;
    }

    /**
     * Checks if is destination scheduler cache expired.
     *
     * @return true, if is destination scheduler cache expired
     */
    public boolean isDestinationSchedulerCacheExpired() {
        return System.currentTimeMillis() - _lastDestinationSchedulerCacheUpdate > 5 * Timer.ONE_SECOND;
    }

    /**
     * Sets the destination scheduler cache.
     *
     * @param destinationSchedulerCache
     *            the new destination scheduler cache
     */
    public void setDestinationSchedulerCache(final DestinationSchedulerCache destinationSchedulerCache) {
        _lastDestinationSchedulerCacheUpdate = System.currentTimeMillis();
        _destinationSchedulerCache = destinationSchedulerCache;
    }
}
