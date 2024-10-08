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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.transport.ptcp.psocket.threads.PTCPReceiverThread;
import ecmwf.common.transport.ptcp.psocket.tools.PTCPBuffer;

/**
 * The Class PTCPInputStream.
 */
public class PTCPInputStream extends InputStream {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PTCPInputStream.class);

    /** The buffer. */
    private PTCPBuffer _buffer = null;

    /** The prws. */
    private PTCPReceiverThread[] _prws = null;

    /** The done synchro. */
    private Object _doneSynchro = null;

    /** The out. */
    private ByteArrayOutputStream _out = null;

    /** The closed. */
    private boolean _closed = false;

    /** The done. */
    private boolean _done = false;

    /** The number of streams. */
    private int _numberOfStreams = 0;

    /**
     * Default constructor Construct an empty parallel input stream with performance increase option.
     *
     * @param streams
     *            the streams
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPInputStream(final InputStream[] streams) throws IOException {
        _doneSynchro = new Object();
        _out = new ByteArrayOutputStream();
        _numberOfStreams = streams.length;
        _buffer = new PTCPBuffer(_numberOfStreams);
        _prws = new PTCPReceiverThread[_numberOfStreams];
        _log.debug("Starting " + _numberOfStreams + " stream(s)");
        for (var i = 0; i < _numberOfStreams; i++) {
            final var worker = new PTCPReceiverThread(this, streams[i], i);
            _prws[i] = worker;
            worker.execute();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Closes this input stream and releases any system resources associated with the stream.
     */
    @Override
    public void close() throws IOException {
        synchronized (_doneSynchro) {
            if (!_done) {
                _done = true;
                for (var i = 0; i < _numberOfStreams; i++) {
                    try {
                        _prws[i].close();
                    } catch (final Throwable t) {
                        _log.debug("Closing stream " + i, t);
                    }
                }
            }
        }
    }

    /**
     * Finished.
     *
     * @return true, if successful
     */
    public boolean finished() {
        synchronized (_doneSynchro) {
            return _done;
        }
    }

    /**
     * Unsign.
     *
     * @param signed
     *            the signed
     *
     * @return the int
     */
    private static int _unsign(final int signed) {
        var retVal = signed;
        if (retVal < 0) {
            retVal += 256;
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     *
     * Reads the next byte of data from the input stream. The value byte is returned as an int in the range 0 to 255. If
     * no byte is available because the end of the stream has been reached, the value -1 is returned. This method blocks
     * until input data is available, the end of thestream is detected, or an exception is thrown.
     *
     * This read only works if the input stream on the other end sends out one byte of data, otherwise IOException will
     * be thrown.
     *
     * Returns: the next byte of data, or -1 if the end of the stream is reached.
     */

    @Override
    public int read() throws IOException {
        final var r = new byte[1];
        final var len = read(r, 0, 1);
        if (len != 1) {
            _log.warn("End of stream? (len=" + len + ")");
            return -1;
        }
        return _unsign(r[0]);
    }

    /**
     * {@inheritDoc}
     *
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer. This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * If size of b is less than data size from input stream, an IOException will be thrown.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     *
     * Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read as many as
     * len bytes, but a smaller number may be read, possibly zero. The number of bytes actually read is returned as an
     * integer.
     *
     * This method blocks until input data is available, end of file is detected, or an exception is thrown.
     *
     * The parameter len must be larger than the size of data being sent over, or an IOException is thrown.
     */
    @Override
    public int read(final byte[] b, final int off, int length) throws IOException {
        if (_done) {
            throw new IOException("Closed input stream");
        }
        synchronized (_out) {
            _fillBuffer(length);
            final var buffer = _out.toByteArray();
            _out.reset();
            length = length > buffer.length ? buffer.length : length;
            System.arraycopy(buffer, 0, b, off, length);
            final var remaining = buffer.length - length;
            if (remaining > 0) {
                _out.write(buffer, length, remaining);
            }
            return length == 0 && _closed ? -1 : length;
        }
    }

    /**
     * Fill buffer.
     *
     * @param length
     *            the length
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _fillBuffer(final int length) throws IOException {
        try {
            byte[] received;
            while (!_closed && _out.size() < length) {
                if ((received = _buffer.read()) != null) {
                    _out.write(received);
                } else {
                    _closed = true;
                }
            }
        } catch (final InterruptedException e) {
            _log.warn("Filling buffer", e);
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Gets the PTCP buffer.
     *
     * @return the PTCP buffer
     */
    public PTCPBuffer getPTCPBuffer() {
        return _buffer;
    }
}
