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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.transport.ptcp.psocket.PTCPInputStream;
import ecmwf.common.transport.ptcp.psocket.tools.PTCPBuffer;

/**
 * The Class PTCPReceiverThread.
 */
public class PTCPReceiverThread extends ConfigurableRunnable {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PTCPReceiverThread.class);

    /** The din. */
    private DataInputStream _din = null;

    /** The parent. */
    private PTCPInputStream _parent = null;

    /** The buffer. */
    private PTCPBuffer _buffer = null;

    /** The id. */
    private int _id = -1;

    /** The done. */
    private boolean _done = false;

    /**
     * Instantiates a new PTCP receiver thread.
     *
     * @param parent
     *            the parent
     * @param is
     *            the is
     * @param id
     *            the id
     */
    public PTCPReceiverThread(final PTCPInputStream parent, final InputStream is, final int id) {
        _parent = parent;
        _buffer = parent.getPTCPBuffer();
        _id = id;
        _din = new DataInputStream(is);
        _log.debug("Stream " + _id + " has fired");
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void close() throws IOException {
        _log.debug("Closing stream " + _id);
        _done = true;
        _din.close();
    }

    /**
     * Read.
     */
    public void read() {
        var number = -1;
        var size = 0;
        byte[] data = null;
        try {
            if ((number = _din.readInt()) != -1) {
                size = _din.readInt();
                data = new byte[size];
                _din.readFully(data, 0, size);
                _buffer.write(number, data);
            } else {
                _log.warn("Stream " + _id + " error (packetNumber=-1)");
                _done = true;
            }
        } catch (final SocketTimeoutException e) {
            // Will check if the parent is finished
        } catch (final EOFException e) {
            _done = true;
        } catch (final Throwable t) {
            if (!_done) {
                _log.warn("Stream " + _id + " error", t);
                _done = true;
            }
        }
    }

    /**
     * Configurable run.
     */
    @Override
    public synchronized void configurableRun() {
        while (!_parent.finished() && !_done) {
            read();
        }
        _buffer.setExit(_id);
        StreamPlugThread.closeQuietly(_din);
        _log.debug("Stream " + _id + " is exiting");
    }
}
