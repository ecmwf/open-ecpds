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

package ecmwf.common.transport.ptcp.psocket.threads;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.transport.ptcp.psocket.PTCPOutputStream;
import ecmwf.common.transport.ptcp.psocket.tools.PTCPPacket;

/**
 * The Class PTCPSenderThread.
 */
public class PTCPSenderThread extends ConfigurableRunnable {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PTCPSenderThread.class);

    /** The io exception. */
    private IOException _ioException = null;

    /** The dos. */
    private DataOutputStream _dos = null;

    /** The parent. */
    private PTCPOutputStream _parent = null;

    /** The packet. */
    private PTCPPacket _packet = null;

    /** The id. */
    private int _id = -1;

    /**
     * Instantiates a new PTCP sender thread.
     *
     * @param parent
     *            the parent
     * @param os
     *            the os
     * @param id
     *            the id
     */
    public PTCPSenderThread(final PTCPOutputStream parent, final OutputStream os, final int id) {
        _parent = parent;
        _id = id;
        _dos = new DataOutputStream(os);
        _log.debug("Stream " + _id + " has fired");
    }

    /**
     * Close.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void close() throws IOException {
        _log.debug("Closing stream " + _id);
        _dos.close();
    }

    /**
     * Flush.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void flush() throws IOException {
        _dos.flush();
    }

    /**
     * {@inheritDoc}
     *
     * Configurable run.
     */
    @Override
    public void configurableRun() {
        try {
            while (!_parent.finished()) {
                send();
                _parent.wakeUp();
            }
        } catch (final IOException e) {
            _ioException = e;
            _parent.wakeUp();
        } catch (final Throwable t) {
            _log.warn("Failed", t);
            _ioException = new IOException(t.getMessage());
            _parent.wakeUp();
        }
    }

    /**
     * Send.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public synchronized void send() throws IOException {
        var sendIt = true;
        while (_packet == null) {
            try {
                wait();
            } catch (final InterruptedException e) {
                _log.warn("Stream " + _id + " interrupted", e);
                sendIt = false;
            }
        }
        if (!_parent.finished() && sendIt) {
            var success = false;
            try {
                _dos.writeInt(_packet.getNumber());
                _dos.writeInt(_packet.getSize());
                _dos.write(_packet.getData(), 0, _packet.getSize());
                _dos.flush();
                success = true;
            } finally {
                if (!success) {
                    _log.warn("Couldn't send: " + _packet);
                }
            }
        }
        _packet = null;
    }

    /**
     * Wakeup.
     *
     * @param packet
     *            the packet
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public synchronized void wakeup(final PTCPPacket packet) throws IOException {
        if (_ioException != null) {
            throw _ioException;
        }
        _packet = packet;
        if (packet != null) {
            notify(); // wake up a writer that is waiting to write
        } else {
            _log.warn("Stream " + _id + " received a null packet to send");
        }
    }
}
