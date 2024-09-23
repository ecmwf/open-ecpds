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

package ecmwf.common.transport.ptcp.psocket;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.transport.ptcp.psocket.threads.PTCPSenderThread;
import ecmwf.common.transport.ptcp.psocket.tools.PTCPPacket;

/**
 * The Class PTCPOutputStream.
 */
public class PTCPOutputStream extends OutputStream {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PTCPOutputStream.class);

    /** The psws. */
    private PTCPSenderThread[] _psws = null;

    /** The done. */
    private boolean _done = false;

    /** The done synchro. */
    private Object _doneSynchro = null;

    /** The out. */
    private ByteArrayOutputStream _out = null;

    /** The number of streams. */
    private int _numberOfStreams = 0;

    /** The packet number. */
    private int _packetNumber = -1;

    /** The sender workers count. */
    private int _senderWorkersCount = 0;

    /** The buffer size. */
    private int _bufferSize = 0;

    /**
     * Default constructor Construct an empty parallel output stream.
     *
     * @param streams
     *            the streams
     */
    public PTCPOutputStream(final OutputStream[] streams) {
        _doneSynchro = new Object();
        _out = new ByteArrayOutputStream();
        _numberOfStreams = streams.length;
        _psws = new PTCPSenderThread[_numberOfStreams];
        _bufferSize = 65536 * _numberOfStreams;
        _log.debug("Starting " + _numberOfStreams + " stream(s) (bufferSize: " + _bufferSize + ")");
        for (var i = 0; i < _numberOfStreams; i++) {
            final var worker = new PTCPSenderThread(this, streams[i], i);
            _psws[i] = worker;
            worker.execute();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Closes this output stream and releases any system resources associated with this stream. The general contract of
     * close is that it closes the output stream. A closed stream cannot perform output operations and cannot be
     * reopened.
     */
    @Override
    public void close() throws IOException {
        _flushBuffer(true);
        synchronized (_doneSynchro) {
            if (!_done) {
                _done = true;
                final var dummyPacket = new PTCPPacket(-1, new byte[1]);
                for (var i = 0; i < _numberOfStreams; i++) {
                    try {
                        _psws[i].wakeup(dummyPacket);
                    } catch (final Throwable t) {
                        _log.debug("Waking up stream " + i, t);
                    }
                }
                for (var i = 0; i < _numberOfStreams; i++) {
                    try {
                        _psws[i].close();
                    } catch (final Throwable t) {
                        _log.debug("Closing stream " + i, t);
                    }
                }
            }
        }
    }

    /**
     * Check whether we are finished or not.
     *
     * @return true, if successful
     */
    public boolean finished() {
        synchronized (_doneSynchro) {
            return _done;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Flushes this output stream and forces any buffered output bytes to be written out. The general contract of flush
     * is that calling it is an indication that, if any bytes previously written have been buffered by the
     * implementation of the output stream, such bytes should immediately be written to their intended destination.
     */
    @Override
    public void flush() throws IOException {
        _flushBuffer(true);
        for (var i = 0; i < _numberOfStreams; i++) {
            try {
                _psws[i].flush();
            } catch (final Throwable t) {
                _log.debug("Flushing stream " + i, t);
            }
        }
    }

    /**
     * Gets the packets.
     *
     * @param data
     *            the data
     * @param off
     *            the off
     * @param len
     *            the len
     * @param connectionNumber
     *            the connection number
     * @param packetNumber
     *            the packet number
     *
     * @return the PTCP packet[]
     */
    private static PTCPPacket[] _getPackets(final byte[] data, int off, final int len, final int connectionNumber,
            int packetNumber) {
        final var packets = new PTCPPacket[connectionNumber];
        var packetSize = len / connectionNumber;
        for (var i = 0; i < connectionNumber - 1; i++) {
            final var temp = new byte[packetSize];
            System.arraycopy(data, off, temp, 0, packetSize);
            off += packetSize;
            packets[i] = new PTCPPacket(++packetNumber, temp);
        }
        packetSize = len - (connectionNumber - 1) * packetSize;
        final var temp = new byte[packetSize];
        System.arraycopy(data, off, temp, 0, packetSize);
        packets[connectionNumber - 1] = new PTCPPacket(++packetNumber, temp);
        return packets;
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (b != null && len > 0) {
            synchronized (_out) {
                _out.write(b, off, len);
                if (len > _bufferSize) {
                    _log.debug("BufferSize updated: " + _bufferSize);
                    _bufferSize = len;
                }
            }
            _flushBuffer(false);
        }
    }

    /**
     * Flush buffer.
     *
     * @param force
     *            the force
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _flushBuffer(final boolean force) throws IOException {
        synchronized (_out) {
            final var len = _out.size();
            if (force && len > 0 || len >= _bufferSize) {
                final var data = _out.toByteArray();
                _out.reset();
                final var packetCount = len / _bufferSize;
                var off = 0;
                for (var i = 0; i < packetCount - 1; i++) {
                    _write(data, off, _bufferSize);
                    off += _bufferSize;
                }
                final var remaining = len - off;
                if (force || remaining >= _bufferSize) {
                    _write(data, off, remaining);
                } else {
                    _out.write(data, off, remaining);
                }
            }
        }
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
    private void _write(final byte[] b, final int off, final int len) throws IOException {
        final var packets = _getPackets(b, off, len, _numberOfStreams, _packetNumber);
        _packetNumber = packets[_numberOfStreams - 1].getNumber();
        for (var i = 0; i < _numberOfStreams; i++) {
            _psws[i].wakeup(packets[i]);
        }
        waitSenderWorkers();
    }

    /**
     * Wake up.
     */
    public synchronized void wakeUp() {
        _senderWorkersCount++;
        // if all writers finished writing, wake this stream
        if (_senderWorkersCount == _numberOfStreams) {
            notify();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Writes the specified byte to this output stream. The general contract for write is that one byte is written to
     * the output stream. The byte to be written is the eight low-order bits of the argument b. The 24 high-order bits
     * of b are ignored.
     *
     * This single byte will travel on command stream
     */
    @Override
    public void write(final int b) throws IOException {
        write(new byte[] { (byte) b });
    }

    /**
     * {@inheritDoc}
     *
     * Writes b.length bytes from the specified byte array to this output stream. The general contract for write(b) is
     * that it should have exactly the same effect as the call write(b, 0, b.length).
     *
     * This is a parallel write.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Wait for writers to finish.
     */
    private synchronized void waitSenderWorkers() {
        while (_senderWorkersCount < _numberOfStreams) {
            try {
                wait();
            } catch (final InterruptedException e) {
            }
        }
        _senderWorkersCount = 0;
    }
}
