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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility to track resource usage: open, closed, and peak concurrent count.
 */
public class ResourceTracker {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ResourceTracker.class);

    /** The Constant DEBUG_FREQUENCY. */
    private static final int DEBUG_FREQUENCY = Cnf.at("ResourceTracker", "debugFrequency", 100);

    /** The name. */
    private final String name;

    /** The open count. */
    private final AtomicInteger openCount = new AtomicInteger(0);

    /** The closed count. */
    private final AtomicInteger closedCount = new AtomicInteger(0);

    /** The peak open count. */
    private final AtomicInteger peakOpenCount = new AtomicInteger(0);

    /** The error count. */
    private final AtomicInteger errorCount = new AtomicInteger(0);

    /**
     * Instantiates a new resource tracker.
     *
     * @param clazz
     *            the clazz
     */
    public ResourceTracker(final Class<?> clazz) {
        this.name = clazz.getCanonicalName();
    }

    /**
     * Call this when a resource is opened.
     */
    public void onOpen() {
        updatePeak(openCount.incrementAndGet());
    }

    /**
     * Call this when a resource is closed.
     *
     * @param success
     *            the success
     */
    public void onClose(boolean success) {
        int open = openCount.decrementAndGet();
        if (open < 0) {
            _log.warn("Negative open count for {}: this indicates unmatched close()", name);
        }
        closedCount.incrementAndGet();
        if (!success) {
            errorCount.incrementAndGet();
            _log.warn("Resource close reported error for {}", name);
        }
        if (_log.isDebugEnabled() && closedCount.get() % DEBUG_FREQUENCY == 0) {
            _log.debug(toString());
        }
    }

    /**
     * Update peak.
     *
     * @param current
     *            the current
     */
    private void updatePeak(final int current) {
        int peak;
        do {
            peak = peakOpenCount.get();
            if (current <= peak)
                break;
        } while (!peakOpenCount.compareAndSet(peak, current));
    }

    /**
     * Gets the open count.
     *
     * @return the open count
     */
    public int getOpenCount() {
        return openCount.get();
    }

    /**
     * Gets the closed count.
     *
     * @return the closed count
     */
    public int getClosedCount() {
        return closedCount.get();
    }

    /**
     * Gets the error count.
     *
     * @return the error count
     */
    public int getErrorCount() {
        return errorCount.get();
    }

    /**
     * Gets the peak open count.
     *
     * @return the peak open count
     */
    public int getPeakOpenCount() {
        return peakOpenCount.get();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return String.format("ResourceTracker[%s]: open=%d, closed=%d, errors=%d, peak=%d", name, getOpenCount(),
                getClosedCount(), getErrorCount(), getPeakOpenCount());
    }
}
