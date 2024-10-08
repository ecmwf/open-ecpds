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

package ecmwf.common.rmi.interruptible;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class InterruptibleOutputStream.
 */
public final class InterruptibleOutputStream extends FilterOutputStream {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(InterruptibleOutputStream.class);

    /** The _monitor. */
    private final InterruptibleMonitor _monitor;

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /**
     * Instantiates a new interruptible input stream.
     *
     * @param out
     *            the out
     */
    public InterruptibleOutputStream(final OutputStream out) {
        super(out);
        _monitor = new InterruptibleMonitor(InterruptibleRMIServerSocket.getCurrentRMIServerThreadSocket());
        _monitor.execute();
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final int b) throws IOException {
        if (_monitor.isClosed()) {
            throw new IOException("RMI client disconnected");
        }
        out.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        if (_monitor.isClosed()) {
            throw new IOException("RMI client disconnected");
        }
        out.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (_monitor.isClosed()) {
            throw new IOException("RMI client disconnected");
        }
        out.write(b, off, len);
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (_closed.compareAndSet(false, true)) {
            try {
                out.close();
            } finally {
                _log.debug("Interrupting progress");
                _monitor.setLoop(false);
                _monitor.interrupt();
                try {
                    _monitor.join(5 * Timer.ONE_MINUTE);
                } catch (final Throwable t) {
                    _log.warn("Waiting for monitor to join", t);
                }
            }
        } else {
            _log.debug("Already closed");
        }
    }
}
