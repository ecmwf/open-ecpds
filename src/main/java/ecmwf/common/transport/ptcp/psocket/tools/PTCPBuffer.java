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

package ecmwf.common.transport.ptcp.psocket.tools;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class PTCPBuffer.
 */
public class PTCPBuffer {

    /** The Constant TIME_WAIT. */
    private static final int TIME_WAIT = 5;

    /** The buffer. */
    private final Map<String, Object> _buffer;

    /** The buffer size. */
    private final int _bufferSize;

    /** The number of streams. */
    private final int _numberOfStreams;

    /** The exit array. */
    private final boolean _exitArray[];

    /** The exit. */
    private boolean _exit = false;

    /** The in index. */
    private int _inIndex = 0;

    /** The out index. */
    private int _outIndex = 0;

    /**
     * Instantiates a new PTCP buffer.
     *
     * @param numberOfStreams
     *            the number of streams
     */
    public PTCPBuffer(final int numberOfStreams) {
        _numberOfStreams = numberOfStreams;
        _bufferSize = numberOfStreams;
        _exitArray = new boolean[numberOfStreams];
        for (var i = 0; i < numberOfStreams; i++) {
            _exitArray[i] = false;
        }
        _buffer = new ConcurrentHashMap<>();
    }

    /**
     * Sets the exit.
     *
     * @param id
     *            the new exit
     */
    public synchronized void setExit(final int id) {
        _exitArray[id] = true;
        _exit = _exitArray[0];
        for (var i = 1; i < _numberOfStreams; i++) {
            _exit = _exit && _exitArray[i];
        }
    }

    /**
     * Write.
     *
     * @param index
     *            the index
     * @param o
     *            the o
     *
     * @throws java.lang.InterruptedException
     *             the interrupted exception
     */
    public synchronized void write(final int index, final Object o) throws InterruptedException {
        while (index != _inIndex || _buffer.size() == _bufferSize) {
            wait(TIME_WAIT);
            if (_exit) {
                break;
            }
        }
        _buffer.put(String.valueOf(index), o);
        _inIndex++;
        notifyAll();
    }

    /**
     * Read.
     *
     * @return the byte[]
     *
     * @throws java.lang.InterruptedException
     *             the interrupted exception
     */
    public synchronized byte[] read() throws InterruptedException {
        final var limit = _outIndex + _bufferSize;
        while (_buffer.size() < _bufferSize && !_exit) {
            wait(TIME_WAIT);
        }
        var size = 0;
        for (var i = _outIndex; i < limit; i++) {
            final var packet = (byte[]) _buffer.get(String.valueOf(i));
            if (packet != null) {
                size += packet.length;
            }
        }
        final var data = new byte[size];
        var start = 0;
        final var end = _outIndex;
        for (var i = end; i < limit; i++) {
            final var packet = (byte[]) _buffer.get(String.valueOf(i));
            if (packet != null) {
                System.arraycopy(packet, 0, data, start, packet.length);
                start += packet.length;
            }
            _buffer.remove(String.valueOf(i));
            _outIndex++;
        }
        notifyAll();
        if (!_exit) {
            return data;
        }
        if (data.length > 0) {
            return data;
        } else {
            return null;
        }
    }
}
