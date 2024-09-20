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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class TransferManager.
 */
public class TransferManager implements ProgressInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TransferManager.class);

    /** The _exec. */
    private final ForkExec _exec;

    /** The _size. */
    private final long _size;

    /** The _start. */
    private long _start = 0;

    /** The _stop. */
    private long _stop = -1;

    /** The _byte sent. */
    private long _byteSent = 0;

    /** The _debug. */
    private boolean _debug = false;

    /**
     * Instantiates a new transfer manager.
     *
     * @param command
     *            the command
     * @param size
     *            the size
     */
    public TransferManager(final String command, final long size) {
        _exec = new ForkExec(command);
        _size = size;
    }

    /**
     * Start the external process and tries to get the progress in percentage from the output (ending by "XX.XX%", e.g.
     * "progress: 12.98%") to update the ProgressHandler. If an acknowledgement is provided then it tries to get it in
     * the last line.
     *
     * @param acknowledgement
     *            the acknowledgement
     * @param handler
     *            the handler
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public int waitFor(final String acknowledgement, final ProgressHandler handler) throws IOException {
        BufferedReader in = null;
        int result;
        try {
            _exec.start();
            in = new BufferedReader(new InputStreamReader(_exec.getInputStream()));
            String lastLine = null;
            String line = null;
            _start = System.currentTimeMillis();
            // Parse the output and tries to extract the progress information!
            try {
                while ((line = in.readLine()) != null) {
                    if (line.length() > 0) {
                        lastLine = line;
                        if (_getRate(line)) {
                            handler.update(this);
                        }
                    }
                }
            } finally {
                _stop = System.currentTimeMillis();
            }
            // Can we find the acknowledgment string in the last line?
            if (acknowledgement != null && (lastLine == null || lastLine.indexOf(acknowledgement) == -1)) {
                throw new IOException("acknowledgement not found"); // we didn't find what we were expecting!
            }
            _byteSent = _size;
            result = _exec.waitFor();
        } catch (Throwable t) {
            _log.warn("exec", t);
            while (t.getCause() != null) {
                t = t.getCause();
            }
            throw new IOException("Transfer aborted (" + t.getMessage() + ")");
        } finally {
            _exec.close();
            StreamPlugThread.closeQuietly(in);
        }
        return result;
    }

    /**
     * Gets the rate from the last 5 characters of the line: e.g. "12.98%"
     *
     * @param line
     *            the line
     *
     * @return true, if successful
     */
    private boolean _getRate(final String line) {
        if (_debug) {
            _log.debug("Received: " + line);
        }
        final var index = line.indexOf("%");
        if (index > 4 && line.charAt(index - 3) == '.') {
            try {
                _byteSent = (long) (Float.parseFloat(line.substring(index - 5, index)) * _size / 100f);
                return true;
            } catch (final Throwable t) {
                _log.warn("Receiving rate", t);
            }
        }
        return false;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    @Override
    public long getDuration() {
        final var stop = _stop == -1 ? System.currentTimeMillis() : _stop;
        return stop - _start;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    @Override
    public long getStartTime() {
        return _start;
    }

    /**
     * Gets the byte sent.
     *
     * @return the byte sent
     */
    @Override
    public long getByteSent() {
        return _byteSent;
    }

    /**
     * Gets the simplified rate.
     *
     * @return the simplified rate
     */
    public String getSimplifiedRate() {
        return Format.formatRate(_byteSent, getDuration());
    }

    /**
     * Close and interrupt if required.
     */
    @Override
    public void closeAndInterruptIfRequired() {
        _exec.close();
    }

    /**
     * Sets the debug.
     *
     * @param debug
     *            the new debug
     */
    public void setDebug(final boolean debug) {
        _debug = debug;
    }
}
