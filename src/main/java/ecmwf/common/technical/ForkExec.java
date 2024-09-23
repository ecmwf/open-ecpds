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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class ForkExec.
 */
public final class ForkExec extends ForkAbstract {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ForkExec.class);

    /** The _args. */
    private final String[] _args;

    /** The _command. */
    private String _command = null;

    /** The _process. */
    private Process _process = null;

    /**
     * Instantiates a new fork exec.
     *
     * @param args
     *            the args
     */
    public ForkExec(final String[] args) {
        _args = args;
    }

    /**
     * Instantiates a new fork exec.
     *
     * @param args
     *            the args
     */
    public ForkExec(final String args) {
        final var tokenizer = new StringTokenizer(args, " ");
        final List<String> list = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        _args = list.toArray(new String[list.size()]);
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() {
        StreamPlugThread.closeQuietly(_process);
    }

    /**
     * {@inheritDoc}
     *
     * Start.
     */
    @Override
    public void start() throws IOException {
        _command = toString(_args);
        final var request = _command;
        var successful = false;
        try {
            _process = Runtime.getRuntime().exec(request);
            successful = true;
        } finally {
            _log.debug("Exec " + (successful ? "" : "NOT ") + "successful \"" + _command + "\"");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Kill.
     */
    @Override
    public boolean kill() {
        close();
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the input stream.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return _process.getInputStream();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the command.
     */
    @Override
    public String getCommand() {
        return _args[0];
    }

    /**
     * Wait for.
     *
     * @return the int
     *
     * @throws java.lang.InterruptedException
     *             the interrupted exception
     */
    public int waitFor() throws InterruptedException {
        return _process != null ? _process.waitFor() : -1;
    }
}
