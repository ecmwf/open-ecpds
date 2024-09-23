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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class AbstractTicket.
 */
public abstract class AbstractTicket implements Serializable, Closeable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4411284942874677369L;

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(AbstractTicket.class);

    /** The _users. */
    private final transient List<TicketUser> _users = new ArrayList<>();

    /** The _to close. */
    private final transient List<Closeable> _toClose = new ArrayList<>();

    /** The _to close. */
    private final transient Object _errorSync = new Object();

    /** The _id. */
    private long _id = -1;

    /** The _error. */
    private String _error = null;

    /** The _stamp. */
    private long _stamp = -1;

    /** The _time. */
    private long _time = -1;

    /** The _time out. */
    private long _timeOut = -1;

    /** The _completed. */
    private final AtomicBoolean _completed = new AtomicBoolean(false);

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /** The _completion time. */
    private long _completionTime = -1;

    /** The _close time. */
    private long _closeTime = -1;

    /**
     * Instantiates a new abstract ticket.
     */
    public AbstractTicket() {
        this(null);
    }

    /**
     * Instantiates a new abstract ticket.
     *
     * @param toClose
     *            the to close
     */
    public AbstractTicket(final Closeable toClose) {
        _time = System.currentTimeMillis();
        if (toClose != null) {
            toClose(toClose);
        }
    }

    /**
     * Sets the error.
     *
     * @param error
     *            the new error
     */
    public void setError(String error) {
        synchronized (_errorSync) {
            try {
                if (error == null) {
                    throw new Exception("No error message specified (null)");
                }
                if (error.length() == 0) {
                    throw new Exception("No error message specified (empty)");
                }
            } catch (final Exception e) {
                _log.warn(error, e);
                error = "no-message";
            }
            _error = error;
        }
    }

    /**
     * Sets the error.
     *
     * @param error
     *            the error
     * @param t
     *            the t
     */
    public void setError(final String error, final Throwable t) {
        synchronized (_errorSync) {
            if (_error == null) {
                // Let's build the error message!
                String message = null;
                try {
                    if (t == null) {
                        throw new Exception("No exception specified (null)");
                    }
                    message = t.getMessage();
                    if (message == null) {
                        throw new Exception("No exception specified (null-message)");
                    }
                    if (message.length() == 0) {
                        throw new Exception("No exception specified (empty-message)");
                    }
                } catch (final Exception e) {
                    if (message == null || message.length() == 0) {
                        message = "exception " + Format.getClassName(t);
                    }
                    _log.warn(error, e);
                }
                setError(error);
                if (message != null && _error != null) {
                    _error = _error + " (" + message + ")";
                }
            } else {
                // If we already have an error then we don't overwrite it!
                _log.debug("Error ignored: " + error + " (ticket already set with an error message)", t);
            }
        }
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public String getError() {
        synchronized (_errorSync) {
            return _error;
        }
    }

    /**
     * Checks if is closed.
     *
     * @return true, if is closed
     */
    public boolean isClosed() {
        return _closed.get();
    }

    /**
     * Checks for error.
     *
     * @return true, if successful
     */
    public boolean hasError() {
        synchronized (_errorSync) {
            return _error != null;
        }
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return _id;
    }

    /**
     * Gets the time out.
     *
     * @return the time out
     */
    public long getTimeOut() {
        return _timeOut;
    }

    /**
     * Sets the stamp.
     */
    public void setStamp() {
        _stamp = System.currentTimeMillis();
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public long getTime() {
        return _time;
    }

    /**
     * Gets the stamp.
     *
     * @return the stamp
     */
    long getStamp() {
        return _stamp;
    }

    /**
     * Stamped.
     *
     * @return true, if successful
     */
    boolean stamped() {
        return _stamp != -1;
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    void setId(final long id) {
        _id = id;
    }

    /**
     * Sets the time out.
     *
     * @param timeOut
     *            the new time out
     */
    public void setTimeOut(final long timeOut) {
        _timeOut = timeOut;
    }

    /**
     * Completed.
     */
    public void completed() {
        if (_completed.compareAndSet(false, true)) {
            // This ticket might not have been added to a repository and
            // therefore not have an id!
            final var id = getId();
            _log.debug("Ticket{} completed", id != -1 ? " " + id : "");
            _completionTime = System.currentTimeMillis();
            synchronized (this) {
                notifyAll();
            }
        } else {
            _log.debug("Already completed");
        }
    }

    /**
     * Checks if is completed.
     *
     * @return true, if is completed
     */
    public boolean isCompleted() {
        return _completed.get();
    }

    /**
     * Gets the completion time.
     *
     * @return the completion time
     */
    public long getCompletionTime() {
        return _completionTime;
    }

    /**
     * Gets the close time.
     *
     * @return the close time
     */
    public long getCloseTime() {
        return _closeTime;
    }

    /**
     * Adds the.
     *
     * @param ticketUser
     *            the ticket user
     */
    public void add(final TicketUser ticketUser) {
        synchronized (_users) {
            _users.add(ticketUser);
        }
    }

    /**
     * Notify users.
     */
    public void notifyUsers() {
        synchronized (_users) {
            for (final TicketUser ticketUser : _users) {
                ticketUser.notifyUser();
            }
        }
    }

    /**
     * Specify which Closeable objects should be closed when the ticket is closed.
     *
     * @param toNotify
     *            the to notify
     */
    public void toClose(final Closeable[] toNotify) {
        for (final Closeable element : toNotify) {
            toClose(element);
        }
    }

    /**
     * Specify which Closeable object should be closed when the ticket is closed.
     *
     * @param toClose
     *            the to close
     */
    public void toClose(final Closeable toClose) {
        synchronized (_toClose) {
            _toClose.add(toClose);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() {
    }

    /**
     * Expired.
     */
    public void expired() {
    }

    /**
     * Close.
     *
     * @param wait
     *            the wait
     */
    public void close(final long wait) {
        // This ticket might not have been added to a repository and therefore
        // not have an id!
        final var id = getId();
        final var sid = id != -1 ? " " + id : "";
        if (!_closed.get() && wait > 0 && !isCompleted()) {
            if (_log.isDebugEnabled()) {
                _log.debug("Wait for completion of ticket{} ({})", sid, Format.formatDuration(wait));
            }
            final var current = System.currentTimeMillis();
            while (!_closed.get() && !_completed.get() && System.currentTimeMillis() - current < wait) {
                try {
                    Thread.sleep(2);
                } catch (final InterruptedException e) {
                    _log.debug("Interrupted");
                }
            }
        }
        _log.debug("Ticket{} is ready to be closed", sid);
        if (_closed.compareAndSet(false, true)) {
            for (final Closeable toClose : _toClose) {
                if (toClose != null) {
                    _log.debug("Closing: {}", () -> Format.getClassName(toClose));
                    StreamPlugThread.closeQuietly(toClose);
                }
            }
            _toClose.clear();
            _log.debug("Closing ticket{}", sid);
            close();
            _closeTime = System.currentTimeMillis();
            _log.debug("Ticket{} closed", sid);
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public abstract String getStatus();

    /**
     * The Class CompleteTicket.
     */
    public static final class CompleteTicket implements Closeable {
        /** The _ticket. */
        final AbstractTicket _ticket;

        /**
         * Instantiates a new complete ticket.
         *
         * @param ticket
         *            the ticket
         */
        public CompleteTicket(final AbstractTicket ticket) {
            _ticket = ticket;
        }

        /**
         * Close.
         */
        @Override
        public void close() {
            _ticket.completed();
        }
    }

    /**
     * The Interface TicketUser.
     */
    public interface TicketUser {
        /**
         * Notify user.
         */
        void notifyUser();
    }
}
