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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;

/**
 * The Class ProxyModule.
 */
public abstract class ProxyModule extends TransferModule implements ECpdsProxy {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ProxyModule.class);

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /** The _in. */
    private InputStream _in = null;

    /** The _out. */
    private OutputStream _out = null;

    /** The _ticket. */
    private ECaccessTicket _ticket = null;

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /** The _thread. */
    private ConfigurableRunnable _thread = null;

    /**
     * Sets the output stream.
     *
     * @param out
     *            the new output stream
     */
    @Override
    public void setOutputStream(final OutputStream out) {
        _out = out;
    }

    /**
     * Sets the input stream.
     *
     * @param in
     *            the new input stream
     */
    @Override
    public void setInputStream(final InputStream in) {
        _in = in;
    }

    /**
     * Puts the.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param size
     *            the size
     * @param socket
     *            the socket
     *
     * @throws Exception
     *             the exception
     */
    public abstract void put(String name, long posn, long size, ProxySocket socket) throws Exception;

    /**
     * Put.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        _ticket = _mover.getTicketRepository().add(new ECaccessTicket(ProxyModule.this, ECaccessTicket.OUTPUT));
        final var put = new PutThread(name, posn, size);
        put.execute(true);
        _log.debug("Wait for 2mn (put)");
        final var current = System.currentTimeMillis();
        while (!_closed.get() && !_ticket.hasError() && _out == null
                && System.currentTimeMillis() - current < 2 * Timer.ONE_MINUTE) {
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                _log.debug("Interrupted");
            }
        }
        var hasError = false;
        if (_closed.get() || (hasError = _ticket.hasError()) || _out == null) {
            if (put.isAlive()) {
                _log.warn("Interrupt PutThread (closed=" + _closed.get() + ",hasError=" + hasError + ")");
                put.interrupt();
                try {
                    _log.debug("Wait for 5s");
                    put.join(5 * Timer.ONE_SECOND);
                    _log.debug("Wakeup");
                } catch (final Exception e) {
                }
            }
            throw new IOException(_closed.get() ? "Module closed" : _ticket.getError());
        }
        _thread = put;
        return _out;
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param socket
     *            the socket
     *
     * @throws Exception
     *             the exception
     */
    public abstract void get(String name, long posn, ProxySocket socket) throws Exception;

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        _ticket = _mover.getTicketRepository().add(new ECaccessTicket(ProxyModule.this, ECaccessTicket.INPUT));
        final var get = new GetThread(name, posn);
        get.execute(true);
        _log.debug("Wait for 2mn (get)");
        final var current = System.currentTimeMillis();
        while (!_closed.get() && !_ticket.hasError() && _in == null
                && System.currentTimeMillis() - current < 2 * Timer.ONE_MINUTE) {
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                _log.debug("Interrupted");
            }
        }
        var hasError = false;
        if (_closed.get() || (hasError = _ticket.hasError()) || _in == null) {
            if (get.isAlive()) {
                _log.warn("Interrupt GetThread (closed=" + _closed.get() + ",hasError=" + hasError + ")");
                get.interrupt();
                try {
                    _log.debug("Wait for 5s");
                    get.join(5 * Timer.ONE_SECOND);
                    _log.debug("Wakeup");
                } catch (final Exception e) {
                }
            }
            throw new IOException(_closed.get() ? "Module closed" : _ticket.getError());
        }
        _thread = get;
        return _in;
    }

    /**
     * Removes the.
     *
     * @param closedOnError
     *            the closed on error
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void remove(boolean closedOnError) throws IOException;

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        if (_closed.compareAndSet(false, true)) {
            if (_ticket != null) {
                _mover.getTicketRepository().remove(_ticket.getId());
            }
            remove(getClosedOnError());
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * Check.
     *
     * @param sent
     *            the sent
     * @param checksum
     *            the checksum
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void check(final long sent, final String checksum) throws IOException {
        if (_thread != null) {
            _log.debug("Waiting for ECtransGet/ECtransPut Thread to complete");
            try {
                _thread.join(5 * Timer.ONE_MINUTE);
            } catch (final Throwable t) {
            }
        }
        if (_ticket != null) {
            _mover.getTicketRepository().check(_ticket.getId(), true);
            _ticket = null;
        }
    }

    /**
     * The Class PutThread.
     */
    private final class PutThread extends ConfigurableRunnable {
        /** The _name. */
        private final String _name;

        /** The _posn. */
        private final long _posn;

        /** The _size. */
        private final long _size;

        /**
         * Instantiates a new puts the thread.
         *
         * @param name
         *            the name
         * @param posn
         *            the posn
         * @param size
         *            the size
         */
        private PutThread(final String name, final long posn, final long size) {
            _name = name;
            _posn = posn;
            _size = size;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            try {
                final var socketConfig = new SocketConfig("ECproxyPlugin");
                put(_name, _posn, _size, new ProxySocket(_ticket.getId(), socketConfig.getPublicAddress(),
                        socketConfig.getPort(), true));
            } catch (final Throwable t) {
                if (_ticket != null) {
                    _ticket.setError("PutThread", t);
                    _log.warn("PutThread: " + _ticket.getError(), t);
                } else {
                    _log.warn("PutThread", t);
                }
            }
            if (_ticket != null) {
                _ticket.completed();
            }
        }
    }

    /**
     * The Class GetThread.
     */
    private final class GetThread extends ConfigurableRunnable {
        /** The _name. */
        private final String _name;

        /** The _posn. */
        private final long _posn;

        /**
         * Instantiates a new gets the thread.
         *
         * @param name
         *            the name
         * @param posn
         *            the posn
         */
        private GetThread(final String name, final long posn) {
            _name = name;
            _posn = posn;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            try {
                final var socketConfig = new SocketConfig("ECproxyPlugin");
                get(_name, _posn, new ProxySocket(_ticket.getId(), socketConfig.getPublicAddress(),
                        socketConfig.getPort(), true));
            } catch (final Throwable t) {
                if (_ticket != null) {
                    _ticket.setError("GetThread", t);
                    _log.warn("GetThread: " + _ticket.getError(), t);
                } else {
                    _log.warn("GetThread", t);
                }
            }
            if (_ticket != null) {
                _ticket.completed();
            }
        }
    }
}
