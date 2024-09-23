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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class DBIterator.
 *
 * @param <E>
 *            the element type
 */
class DBIterator<E extends DataBaseObject> implements Iterator<E> {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DBIterator.class);

    /** The data base. */
    private final DataBase dataBase;

    /** The underlying iterator. */
    private final Iterator<E> iterator;

    /** The start time. */
    private final long startTime = System.currentTimeMillis();

    /** The successful flag. */
    private boolean successful = true;

    /** The broker. */
    private Broker broker = null;

    /**
     * Instantiates a new DB iterator.
     */
    DBIterator() {
        iterator = new ArrayList<E>().iterator();
        dataBase = null;
        broker = null;
    }

    /**
     * Instantiates a new DB iterator.
     *
     * @param dataBase
     *            the data base
     * @param broker
     *            the broker
     * @param iterator
     *            the iterator
     * @param info
     *            the info
     */
    DBIterator(final DataBase dataBase, final Broker broker, final Iterator<E> iterator, final String info) {
        this.iterator = iterator;
        this.dataBase = dataBase;
        this.broker = broker;
        if (!iterator.hasNext()) {
            remove();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Checks for next.
     */
    @Override
    public boolean hasNext() {
        var completed = false;
        try {
            final var result = iterator.hasNext();
            completed = true;
            return result;
        } finally {
            if (!completed) {
                successful = false;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Next.
     */
    @Override
    public E next() {
        var completed = false;
        try {
            final var result = iterator.next();
            completed = true;
            return result;
        } catch (final Throwable t) {
            _log.warn("DBIterator error: {}", iterator, t);
            throw t;
        } finally {
            if (!completed) {
                successful = false;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * This method has a different behavior, as instead of removing the latest element it is closing the underlying
     * database connection. This allow releasing the underlying resources even when using a standard iterator interface.
     *
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
        var done = false;
        synchronized (this) {
            if (broker != null && dataBase != null) {
                broker.release(successful);
                broker = null;
                done = true;
            }
        }
        if (done) {
            final var elapsed = System.currentTimeMillis() - startTime;
            if (_log.isDebugEnabled() && elapsed > 10000L) {
                _log.debug("Closing DBIterator after: {}", Format.formatDuration(elapsed));
            }
        }
    }
}
