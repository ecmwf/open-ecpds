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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.CloseableIterator;
import ecmwf.common.technical.ResourceTracker;
import ecmwf.common.text.Format;

/**
 * The Class DBIterator.
 *
 * @param <E>
 *            the element type
 */
public class DBIterator<E extends DataBaseObject> implements Iterator<E>, CloseableIterator<E> {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DBIterator.class);

    /** The Constant TRACKER. */
    private static final ResourceTracker TRACKER = new ResourceTracker(DBIterator.class);

    /** The underlying iterator. */
    private final CloseableIterator<E> iterator;

    /** The broker. */
    private final Broker broker;

    /** The start time. */
    private final long startTime = System.currentTimeMillis();

    /** The closed flag. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Instantiates a new DB iterator.
     */
    DBIterator() {
        this.iterator = CloseableIterator.empty();
        this.broker = null;
    }

    /**
     * Instantiates a new DB iterator.
     *
     * @param broker
     *            the broker
     * @param target
     *            the target
     */
    DBIterator(final Broker broker, final Class<E> target) {
        this.iterator = broker.getIterator(target);
        this.broker = broker;
        TRACKER.onOpen();
    }

    /**
     * Instantiates a new DB iterator.
     *
     * @param broker
     *            the broker
     * @param target
     *            the target
     * @param sql
     *            the sql
     */
    DBIterator(final Broker broker, final Class<E> target, final String sql) {
        this.iterator = broker.getIterator(target, sql);
        this.broker = broker;
        TRACKER.onOpen();
    }

    /**
     * Checks for next.
     *
     * @return true, if successful
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Next.
     *
     * @return the e
     */
    @Override
    public E next() {
        try {
            return iterator.next();
        } catch (final Throwable t) {
            _log.warn("DBIterator error: {}", iterator, t);
            throw t;
        }
    }

    /**
     * This method is closing the underlying database connection. This allow releasing the underlying resources.
     *
     * @see java.util.Iterator#close()
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true) && broker != null) {
            try {
                try {
                    iterator.close();
                } catch (Exception e) {
                    _log.warn("Failed to close iterator", e);
                }
                try {
                    broker.release();
                } catch (Exception e) {
                    _log.warn("Failed to release broker", e);
                }
            } finally {
                TRACKER.onClose();
                final var elapsed = System.currentTimeMillis() - startTime;
                if (_log.isDebugEnabled() && TRACKER.getClosedCount() % 100 == 0) {
                    _log.debug("Closed after {}: {}", Format.formatDuration(elapsed), TRACKER);
                }
            }
        }
    }
}
