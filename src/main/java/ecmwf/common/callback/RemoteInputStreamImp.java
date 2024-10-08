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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class RemoteInputStreamImp.
 */
public final class RemoteInputStreamImp extends RemoteManagement implements RemoteInputStream, Closeable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4473817650326462267L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(RemoteInputStreamImp.class);

    /** The _closed. */
    private final transient AtomicBoolean _closed = new AtomicBoolean(false);

    /** The _in. */
    private final transient InputStream _in;

    /** The _i. */
    private transient int _i = 0;

    /**
     * Instantiates a new remote input stream imp.
     *
     * @param in
     *            the in
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    public RemoteInputStreamImp(final InputStream in) throws RemoteException {
        _in = in;
    }

    /**
     * {@inheritDoc}
     *
     * Alive.
     */
    @Override
    public boolean alive() {
        try {
            return !_closed.get() && _i >= 0 && available() >= 0;
        } catch (final Throwable t) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Available.
     */
    @Override
    public int available() throws IOException {
        return _in.available();
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (_closed.compareAndSet(false, true)) {
            _in.close();
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
     * Mark.
     */
    @Override
    public void mark(final int readlimit) throws RemoteException {
        _in.mark(readlimit);
    }

    /**
     * {@inheritDoc}
     *
     * Mark supported.
     */
    @Override
    public boolean markSupported() throws RemoteException {
        return _in.markSupported();
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read() throws IOException {
        final var c = _i = _in.read();
        if (_i == -1) {
            close();
        }
        return c;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public ByteStream read(final int len) throws IOException {
        final var holder = new byte[len];
        final var bs = new ByteStream(holder, _i = _in.read(holder, 0, len));
        if (_i == -1) {
            close();
        }
        return bs;
    }

    /**
     * {@inheritDoc}
     *
     * Reset.
     */
    @Override
    public void reset() throws IOException {
        _in.reset();
    }

    /**
     * {@inheritDoc}
     *
     * Skip.
     */
    @Override
    public long skip(final long n) throws IOException {
        return _in.skip(n);
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
            StreamPlugThread.closeQuietly(_in);
        }
        super.finalize();
    }
}
