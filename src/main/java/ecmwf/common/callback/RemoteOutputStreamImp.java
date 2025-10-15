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
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Objects;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class RemoteOutputStreamImp.
 */
public final class RemoteOutputStreamImp extends RemoteManagement implements RemoteOutputStream, Closeable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7537470730187698370L;

    /** The out. */
    private final transient OutputStream out;

    /** The toClose. */
    private final transient Closeable toClose;

    /**
     * Instantiates a new remote output stream imp.
     *
     * @param out
     *            the out
     *
     * @throws RemoteException
     *             the remote exception
     */
    public RemoteOutputStreamImp(final OutputStream out) throws RemoteException {
        this(null, out);
    }

    /**
     * Instantiates a new remote output stream imp.
     *
     * @param toClose
     *            the to close
     * @param out
     *            the out
     *
     * @throws RemoteException
     *             the remote exception
     */
    public RemoteOutputStreamImp(final Closeable toClose, final OutputStream out) throws RemoteException {
        this.out = Objects.requireNonNull(out, "out must not be null");
        this.toClose = toClose;
    }

    /**
     * Alive.
     *
     * @return true, if successful
     */
    @Override
    public boolean alive() {
        try {
            out.flush();
            return true;
        } catch (final IOException _) {
            return false;
        }
    }

    /**
     * Flush.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        out.write(b);
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     * @param off
     *            the off
     * @param len
     *            the len
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        out.write(b, off, len);
    }

    /**
     * Write.
     *
     * @param b
     *            the b
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void write(final int b) throws IOException {
        out.write(b);
    }

    /**
     * Destroy.
     */
    @Override
    public void destroy() {
        StreamPlugThread.closeQuietly(toClose);
        StreamPlugThread.closeQuietly(out);
    }

    /**
     * Closes this stream and performs all associated cleanup.
     *
     * @throws IOException
     *             If an error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        try {
            StreamPlugThread.closeQuietly(toClose);
            out.close();
        } finally {
            unexport();
        }
    }
}
