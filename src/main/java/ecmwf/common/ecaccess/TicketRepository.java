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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.security.Tools;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class TicketRepository.
 */
public class TicketRepository extends StorageRepository<AbstractTicket> {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TicketRepository.class);

    /** The _timeout. */
    private long _timeout = 5 * Timer.ONE_HOUR;

    /** The _timeout after completion. */
    private long _timeoutAfterCompletion = 3 * Timer.ONE_MINUTE;

    /**
     * Instantiates a new ticket repository.
     *
     * @param name
     *            the name
     */
    public TicketRepository(final String name) {
        super(name, Cnf.at("StorageRepository", "ticketSize", 0),
                Cnf.at("StorageRepository", "ticketDelay", Timer.ONE_MINUTE));
        setTimeOut(Cnf.at("StorageRepository", "ticketTimeOut", _timeout));
        setTimeOutAfterCompletion(Cnf.at("StorageRepository", "ticketTimeOutAfterCompletion", _timeoutAfterCompletion));
    }

    /**
     * Adds the new ticket.
     *
     * @param <E>
     *            the element type
     * @param ticket
     *            the ticket
     *
     * @return the e
     */
    public <E extends AbstractTicket> E add(final E ticket) {
        return add(ticket, Tools.getUniqueLongId());
    }

    /**
     * Adds the new ticket.
     *
     * @param <E>
     *            the element type
     * @param ticket
     *            the ticket
     * @param id
     *            the id
     *
     * @return the e
     */
    public <E extends AbstractTicket> E add(final E ticket, final long id) {
        ticket.setId(id);
        put(ticket);
        _log.debug("Ticket added: " + ticket.getId() + " (" + ticket.getClass().getSimpleName() + ")");
        return ticket;
    }

    /**
     * Gets the time out.
     *
     * @return the time out
     */
    public long getTimeOut() {
        return _timeout;
    }

    /**
     * Sets the time out.
     *
     * @param timeout
     *            the new time out
     */
    public void setTimeOut(final long timeout) {
        _timeout = timeout;
    }

    /**
     * Gets the time out after completion.
     *
     * @return the time out after completion
     */
    public long getTimeOutAfterCompletion() {
        return _timeoutAfterCompletion;
    }

    /**
     * Sets the time out after completion.
     *
     * @param timeoutAfterCompletion
     *            the new time out after completion
     */
    public void setTimeOutAfterCompletion(final long timeoutAfterCompletion) {
        _timeoutAfterCompletion = timeoutAfterCompletion;
    }

    /**
     * Gets the.
     *
     * @param id
     *            the id
     *
     * @return the abstract ticket
     */
    public AbstractTicket get(final long id) {
        final var ticket = getValue(Format.formatLong(id, 10, true));
        if (ticket == null) {
            _log.debug("Ticket not found: " + id + " (get)");
        }
        return ticket;
    }

    /**
     * Gets the.
     *
     * @param <T>
     *            the generic type
     * @param id
     *            the id
     * @param clazz
     *            the clazz
     *
     * @return the abstract ticket
     */
    public <T extends AbstractTicket> T get(final long id, final Class<T> clazz) {
        return clazz.cast(get(id));
    }

    /**
     * Check.
     *
     * @param id
     *            the id
     * @param wait
     *            the wait
     *
     * @return the abstract ticket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public AbstractTicket check(final long id, final long wait) throws IOException {
        final var ticket = removeKey(Format.formatLong(id, 10, true));
        if (ticket != null) {
            _check(ticket, wait);
        } else {
            _log.debug("Ticket already closed/not found: " + id);
        }
        return ticket;
    }

    /**
     * Check.
     *
     * @param id
     *            the id
     * @param wait
     *            the wait
     *
     * @return the abstract ticket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public AbstractTicket check(final long id, final boolean wait) throws IOException {
        return check(id, wait ? Long.MAX_VALUE : 0);
    }

    /**
     * _check.
     *
     * @param ticket
     *            the ticket
     * @param wait
     *            the wait
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _check(final AbstractTicket ticket, final long wait) throws IOException {
        _log.debug("Ticket checked: " + ticket.getId());
        ticket.close(wait);
        if (ticket.hasError()) {
            _log.warn("Ticket " + ticket.getId() + " error: " + ticket.getError());
            throw new IOException(ticket.getError());
        }
    }

    /**
     * Removes the.
     *
     * @param id
     *            the id
     *
     * @return the abstract ticket
     */
    public AbstractTicket remove(final long id) {
        final var ticket = removeKey(Format.formatLong(id, 10, true));
        if (ticket != null) {
            _log.debug("Ticket removed: " + id);
            ticket.notifyUsers();
            ticket.close(0);
        }
        return ticket;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the key.
     */
    @Override
    public String getKey(final AbstractTicket ticket) {
        return Format.formatLong(ticket.getId(), 10, true);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus(final AbstractTicket ticket) {
        return ticket.getStatus();
    }

    /**
     * {@inheritDoc}
     *
     * Expired.
     */
    @Override
    public boolean expired(final AbstractTicket ticket) {
        var timeOut = ticket.getTimeOut();
        timeOut = timeOut >= 0 ? timeOut : _timeout;
        var length = timeOut;
        final var expired = timeOut > 0
                && (ticket.stamped() && (length = System.currentTimeMillis() - ticket.getStamp()) > timeOut
                        || !ticket.stamped() && (length = System.currentTimeMillis() - ticket.getTime()) > timeOut)
                || (ticket.isCompleted() || ticket.hasError())
                        && (length = System.currentTimeMillis() - ticket.getCompletionTime()) > _timeoutAfterCompletion;
        if (expired) {
            _log.debug("Ticket " + ticket.getId() + " expired after " + Format.formatDuration(length) + " ("
                    + ticket.getStatus() + ")");
            ticket.expired();
        }
        return expired;
    }

    /**
     * {@inheritDoc}
     *
     * Update.
     */
    @Override
    public void update(final AbstractTicket ticket) {
        try {
            _check(ticket, 0);
        } catch (final Throwable t) {
            _log.error("Ticket update: " + ticket.getId(), t);
        }
    }
}
