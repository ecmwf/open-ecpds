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

package ecmwf.common.ftp;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.transport.ptcp.psocket.PTCPSocket;

/**
 * The Class DataSocket.
 */
public final class DataSocket implements Closeable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DataSocket.class);

    /** The socketClosed. */
    private final AtomicBoolean socketClosed = new AtomicBoolean(false);

    /** The pis. */
    private PersistentInputStream pis = null;

    /** The pos. */
    private PersistentOutputStream pos = null;

    /** The _data alive. */
    private final boolean dataAlive;

    /** The _socket. */
    private final Socket socket;

    /**
     * Instantiates a new data socket.
     *
     * @param socket
     *            the socket
     * @param dataAlive
     *            the data alive
     */
    public DataSocket(final Socket socket, final boolean dataAlive) {
        this.dataAlive = dataAlive;
        this.socket = socket;
        _log.debug("Manage: {}", this);
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public synchronized InputStream getInputStream() throws IOException {
        if (pis != null && !pis.isClosed()) {
            StreamPlugThread.closeQuietly(pis);
        }
        var in = socket.getInputStream();
        if (dataAlive) {
            final var dis = new DataInputStream(in);
            final var size = dis.readLong();
            if (size >= 0) {
                _log.debug("Create RawInputStream ({} byte(s))", size);
                pis = new RawInputStream(in, size);
            } else {
                _log.debug("Create PacketInputStream");
                pis = new PacketInputStream(in);
            }
            in = pis;
        }
        return in;
    }

    /**
     * Gets the output stream.
     *
     * @param size
     *            the size
     *
     * @return the output stream
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public synchronized OutputStream getOutputStream(final long size) throws IOException {
        if (pos != null && !pos.isClosed()) {
            StreamPlugThread.closeQuietly(pos);
        }
        var out = socket.getOutputStream();
        if (dataAlive) {
            final var dos = new DataOutputStream(out);
            dos.writeLong(size);
            dos.flush();
            if (size >= 0) {
                _log.debug("Create RawOutputStream ({} byte(s))", size);
                pos = new RawOutputStream(out, size);
            } else {
                _log.debug("Create PacketOutputStream");
                pos = new PacketOutputStream(out);
            }
            out = pos;
        }
        return out;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (socketClosed.compareAndSet(false, true)) {
            socket.close();
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * Close.
     *
     * @param force
     *            the force
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void close(final boolean force) throws IOException {
        if (force || !dataAlive) {
            _log.debug("{}lose: {}", force ? "Force c" : "C", this);
            close();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Finalize.
     */
    @Override
    protected void finalize() throws Throwable {
        if (socketClosed.compareAndSet(false, true)) {
            _log.warn("Forcing close in finalize <- {}", this.getClass().getName());
            StreamPlugThread.closeQuietly(socket);
        }
        super.finalize();
    }

    /**
     * Checks if is open.
     *
     * @return true, if is open
     */
    public boolean isOpen() {
        return socketIsOpen(socket);
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return socket.toString() + (socket instanceof PTCPSocket ? " (PTCP)" : "");
    }

    /**
     * _socket is open.
     *
     * @param s
     *            the s
     *
     * @return true, if successful
     */
    private static boolean socketIsOpen(final Socket s) {
        return s != null && !(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown());
    }

    /**
     * _unsign.
     *
     * @param signed
     *            the signed
     *
     * @return the int
     */
    private static int unsign(final int signed) {
        var retVal = signed;
        if (retVal < 0) {
            retVal += 256;
        }
        return retVal;
    }

    /**
     * The Class PersistentInputStream.
     */
    abstract static class PersistentInputStream extends FilterInputStream {
        /**
         * Instantiates a new persistent input stream.
         *
         * @param in
         *            the in
         */
        PersistentInputStream(final InputStream in) {
            super(in);
        }

        /**
         * Checks if is closed.
         *
         * @return true, if is closed
         */
        public abstract boolean isClosed();
    }

    /**
     * The Class PersistentOutputStream.
     */
    abstract static class PersistentOutputStream extends FilterOutputStream {
        /**
         * Instantiates a new persistent output stream.
         *
         * @param out
         *            the out
         */
        PersistentOutputStream(final OutputStream out) {
            super(out);
        }

        /**
         * Checks if is closed.
         *
         * @return true, if is closed
         */
        public abstract boolean isClosed();
    }

    /**
     * The Class RawInputStream.
     */
    private static final class RawInputStream extends PersistentInputStream {
        /** The size. */
        private long size = -1;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /**
         * Instantiates a new raw input stream.
         *
         * @param in
         *            the in
         * @param size
         *            the size
         */
        public RawInputStream(final InputStream in, final long size) {
            super(in);
            this.size = size;
        }

        /**
         * Read.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read() throws IOException {
            if (closed.get()) {
                throw new IOException("Closed RawInputStream");
            }
            if (size <= 0) {
                closed.set(true);
                return -1;
            }
            final var r = in.read();
            size--;
            return unsign(r);
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         * @param off
         *            the off
         * @param len
         *            the len
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b, final int off, int len) throws IOException {
            if (closed.get()) {
                throw new IOException("Closed RawInputStream");
            }
            if (size <= 0) {
                closed.set(true);
                return -1;
            }
            if (len > size) {
                len = (int) size;
            }
            final var r = in.read(b, off, len);
            if (r > 0) {
                size -= r;
            }
            return r;
        }

        /**
         * Close.
         */
        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                _log.debug("Close RawInputStream");
                if (size > 0) {
                    _log.warn("{} byte(s) remaining", size);
                }
            } else {
                _log.debug("Already closed");
            }
        }

        /**
         * Checks if is closed.
         *
         * @return true, if is closed
         */
        @Override
        public boolean isClosed() {
            return closed.get() || size <= 0;
        }
    }

    /**
     * The Class RawOutputStream.
     */
    private static final class RawOutputStream extends PersistentOutputStream {
        /** The size. */
        private long size = -1;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /**
         * Instantiates a new raw output stream.
         *
         * @param out
         *            the out
         * @param size
         *            the size
         */
        public RawOutputStream(final OutputStream out, final long size) {
            super(out);
            this.size = size;
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
            if (closed.get()) {
                throw new IOException("Closed RawOutputStream");
            }
            if (size <= 0) {
                closed.set(true);
                throw new IOException("End Of RawOutputStream");
            }
            out.write(b);
            size--;
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
            write(b, 0, b.length);
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
        public void write(final byte[] b, final int off, int len) throws IOException {
            if (closed.get()) {
                throw new IOException("Closed RawOutputStream");
            }
            if (size <= 0) {
                closed.set(true);
                throw new IOException("End Of RawOutputStream");
            }
            if (len > size) {
                len = (int) size;
            }
            out.write(b, off, len);
            size -= len;
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                _log.debug("Close RawOutputStream");
                if (size > 0) {
                    _log.warn("{} byte(s) remaining", size);
                }
                out.flush();
            } else {
                _log.debug("Already closed");
            }
        }

        /**
         * Checks if is closed.
         *
         * @return true, if is closed
         */
        @Override
        public boolean isClosed() {
            return closed.get() || size <= 0;
        }
    }

    /**
     * The Class PacketInputStream.
     */
    private static final class PacketInputStream extends PersistentInputStream {
        /** The packet. */
        private final ByteArrayOutputStream packet;

        /** The dis. */
        private final DataInputStream dis;

        /** The _closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The eof. */
        private boolean eof = false;

        /** The packet count. */
        private long packetCount = 0;

        /** The packet size. */
        private long packetSize = 0;

        /**
         * Instantiates a new packet input stream.
         *
         * @param in
         *            the in
         */
        public PacketInputStream(final InputStream in) {
            super(in);
            packet = new ByteArrayOutputStream();
            dis = new DataInputStream(in);
        }

        /**
         * _receive header.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private int receiveHeader() throws IOException {
            final var size = dis.readInt();
            if (size <= 0) {
                _log.debug("Received final header ({} packet(s) received accounting for {} bytes)", packetCount,
                        packetSize);
                eof = true;
            }
            return size;
        }

        /**
         * _receive packets.
         *
         * @param length
         *            the length
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void receivePackets(final int length) throws IOException {
            if (!eof && length > packet.size()) {
                final var size = receiveHeader();
                if (size > 0) {
                    final var received = new byte[size];
                    dis.readFully(received);
                    packet.write(received);
                    packetSize += size;
                    packetCount++;
                }
            }
        }

        /**
         * Read.
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read() throws IOException {
            final var r = new byte[1];
            final var len = read(r, 0, 1);
            if (len != 1) {
                _log.warn("End of stream? (len={})", len);
                return -1;
            }
            return unsign(r[0]);
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public int read(final byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        /**
         * Read.
         *
         * @param b
         *            the b
         * @param off
         *            the off
         * @param length
         *            the length
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public synchronized int read(final byte[] b, final int off, int length) throws IOException {
            if (closed.get()) {
                throw new IOException("Closed PacketInputStream");
            }
            receivePackets(length);
            final var buffer = packet.toByteArray();
            packet.reset();
            length = length > buffer.length ? buffer.length : length;
            System.arraycopy(buffer, 0, b, off, length);
            final var remaining = buffer.length - length;
            if (remaining > 0) {
                packet.write(buffer, length, remaining);
                packet.flush();
            }
            return length == 0 && eof ? -1 : length;
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                _log.debug("Close PacketInputStream");
                if (!eof) {
                    final var size = receiveHeader();
                    if (size >= 0) {
                        _log.warn("Was expecting a final header ({})", size);
                    }
                }
            } else {
                _log.debug("Already closed");
            }
        }

        /**
         * Checks if is closed.
         *
         * @return true, if is closed
         */
        @Override
        public boolean isClosed() {
            return closed.get();
        }
    }

    /**
     * The Class PacketOutputStream.
     */
    private static final class PacketOutputStream extends PersistentOutputStream {
        /** The packet. */
        private final ByteArrayOutputStream packet;

        /** The dos. */
        private final DataOutputStream dos;

        /** The _closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The done. */
        private boolean done = true;

        /** The packet count. */
        private long packetCount = 0;

        /** The packet size. */
        private long packetSize = 0;

        /**
         * Instantiates a new packet output stream.
         *
         * @param out
         *            the out
         */
        public PacketOutputStream(final OutputStream out) {
            super(out);
            packet = new ByteArrayOutputStream();
            dos = new DataOutputStream(out);
        }

        /**
         * Sends the packet.
         *
         * @param close
         *            the close
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void sendPacket(final boolean close) throws IOException {
            final var size = packet.size();
            if (done && (close && size > 0 || size >= 65536)) {
                try {
                    done = false;
                    dos.writeInt(size);
                    dos.write(packet.toByteArray());
                    packetSize += size;
                    packetCount++;
                    done = true;
                } finally {
                    packet.reset();
                }
            }
            if (done && close) {
                _log.debug("Sending final header ({} packet(s) sent accounting for {} bytes)", packetCount, packetSize);
                dos.writeInt(0);
                dos.flush();
            }
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
            if (closed.get()) {
                throw new IOException("Closed PacketOutputStream");
            }
            synchronized (packet) {
                packet.write(b);
                sendPacket(false);
            }
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
            write(b, 0, b.length);
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
            if (closed.get()) {
                throw new IOException("Closed PacketOutputStream");
            }
            synchronized (packet) {
                packet.write(b, off, len);
                sendPacket(false);
            }
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                _log.debug("Close PacketOutputStream");
                synchronized (packet) {
                    sendPacket(true);
                }
            } else {
                _log.debug("Already closed");
            }
        }

        /**
         * Checks if is closed.
         *
         * @return true, if is closed
         */
        @Override
        public boolean isClosed() {
            return closed.get();
        }
    }
}
