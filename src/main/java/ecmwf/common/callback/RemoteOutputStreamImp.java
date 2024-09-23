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

package ecmwf.common.callback;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class RemoteOutputStreamImp.
 */
public final class RemoteOutputStreamImp extends RemoteManagement implements RemoteOutputStream, Closeable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7537470730187698370L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(RemoteOutputStreamImp.class);

    /** The _closed. */
    private final transient AtomicBoolean _closed = new AtomicBoolean(false);

    /** The _out. */
    private final transient OutputStream _out;

    /** The _to close. */
    private final transient Closeable _toClose;

    /**
     * Instantiates a new remote output stream imp.
     *
     * @param out
     *            the out
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    public RemoteOutputStreamImp(final OutputStream out) throws RemoteException {
        _toClose = null;
        _out = out;
    }

    /**
     * Instantiates a new remote output stream imp.
     *
     * @param toClose
     *            the to close
     * @param out
     *            the out
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    public RemoteOutputStreamImp(final Closeable toClose, final OutputStream out) throws RemoteException {
        _toClose = toClose;
        _out = out;
    }

    /**
     * {@inheritDoc}
     *
     * Alive.
     */
    @Override
    public boolean alive() {
        if (!_closed.get()) {
            try {
                _out.flush();
                return true;
            } catch (final Throwable t) {
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (_closed.compareAndSet(false, true)) {
            StreamPlugThread.closeQuietly(_toClose);
            _out.close();
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Destroy.
     */
    @Override
    public void destroy() {
        StreamPlugThread.closeQuietly(this);
    }

    /**
     * {@inheritDoc}
     *
     * Flush.
     */
    @Override
    public void flush() throws IOException {
        _out.flush();
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        _out.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        _out.write(b, off, len);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final int b) throws IOException {
        _out.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * Finalize.
     */
    @Override
    protected void finalize() throws Throwable {
        if (_closed.compareAndSet(false, true)) {
            _log.warn("Forcing close in finalize <- {}", this.getClass().getName());
            StreamPlugThread.closeQuietly(_toClose);
            StreamPlugThread.closeQuietly(_out);
        }
        super.finalize();
    }
}
