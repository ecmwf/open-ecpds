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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.interruptible.InterruptibleOutputStream;
import ecmwf.common.rmi.interruptible.InterruptibleRMIServerSocket;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class ECtransList.
 */
public final class ECtransList extends ECtransAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECtransList.class);

    /** The types available for listing. */
    private enum TYPE {

        /** The string array. */
        STRING_ARRAY,
        /** The gzipped byte array. */
        GZIPPED_BYTE_ARRAY,
        /** The byte output. */
        BYTE_OUTPUT
    }

    /** The directory. */
    private final String directory;

    /** The pattern. */
    private final String pattern;

    /** The version. */
    private final TYPE type;

    /** The list as string array. */
    private String[] listAsStringArray = null;

    /** The list as byte array. */
    private byte[] listAsByteArray = null;

    /** The output stream. */
    private final OutputStream out;

    /**
     * RMI socket captured at construction time (while still in the RMI dispatch thread). For async exec the listing
     * runs in a different thread where getCurrentRMIServerThreadSocket() returns null, so we must save it here.
     */
    private final Socket rmiSocket;

    /**
     * Instantiates a new ectrans list.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param out
     *            the out
     * @param type
     *            the type
     */
    private ECtransList(final String directory, final String pattern, final OutputStream out, final TYPE type) {
        this.directory = directory;
        this.pattern = pattern;
        this.out = out;
        this.type = type;
        // Capture the RMI server socket from the calling (RMI dispatch) thread now, before any async handoff
        this.rmiSocket = InterruptibleRMIServerSocket.getCurrentRMIServerThreadSocket();
    }

    /**
     * Instantiates a new ectrans list.
     *
     * @param directory
     *            the directory
     */
    public ECtransList(final String directory) {
        this(directory, null, null, TYPE.STRING_ARRAY);
    }

    /**
     * Instantiates a new ectrans list.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param asStringArray
     *            the asStringArray
     */
    public ECtransList(final String directory, final String pattern, final boolean asStringArray) {
        this(directory, pattern, null, asStringArray ? TYPE.STRING_ARRAY : TYPE.GZIPPED_BYTE_ARRAY);
    }

    /**
     * Instantiates a new ectrans list.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param out
     *            the out
     */
    public ECtransList(final String directory, final String pattern, final OutputStream out) {
        this(directory, pattern, out, TYPE.BYTE_OUTPUT);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    protected String getName() {
        return "list";
    }

    /**
     * {@inheritDoc}
     *
     * Exec.
     */
    @Override
    protected void exec(final TransferModule module, final boolean interruptible) throws Exception {
        // Resolve the best available RMI socket: prefer the one captured at construction
        // (reliable for async exec where the current thread is no longer the RMI dispatch
        // thread), fall back to the current thread's socket (for sync exec).
        final var socket = rmiSocket != null ? rmiSocket
                : InterruptibleRMIServerSocket.getCurrentRMIServerThreadSocket();
        switch (type) {
        case STRING_ARRAY:
            if (interruptible && socket != null) {
                // Run a watcher that interrupts this thread when the RMI client disconnects,
                // so that the blocking listAsStringArray() call can be unblocked.
                final var callerThread = Thread.currentThread();
                final var watcher = startConnectionWatcher(socket, callerThread);
                try {
                    listAsStringArray = module.listAsStringArray(directory, pattern);
                } finally {
                    watcher.interrupt();
                }
            } else {
                listAsStringArray = module.listAsStringArray(directory, pattern);
            }
            break;
        case GZIPPED_BYTE_ARRAY:
            if (interruptible && socket != null) {
                final var callerThread = Thread.currentThread();
                final var watcher = startConnectionWatcher(socket, callerThread);
                try {
                    listAsByteArray = module.listAsByteArray(directory, pattern);
                } finally {
                    watcher.interrupt();
                }
            } else {
                listAsByteArray = module.listAsByteArray(directory, pattern);
            }
            break;
        case BYTE_OUTPUT:
            // Wrap the output stream so every write checks whether the RMI client is still
            // connected. When the client disconnects, the InterruptibleOutputStream throws
            // IOException on the next write, aborting the in-progress listing.
            final var effectiveOut = (interruptible && socket != null) ? new InterruptibleOutputStream(out, socket)
                    : out;
            try {
                module.list(directory, pattern, effectiveOut);
            } catch (final Exception e) {
                try {
                    effectiveOut.write(("err:" + Format.getMessage(e)).getBytes());
                    effectiveOut.flush();
                } catch (final IOException ignored) {
                    // Connection already lost — can't report the error downstream, ignore
                    _log.debug("Could not write listing error to output (connection lost)", ignored);
                }
                throw e;
            } finally {
                StreamPlugThread.closeQuietly(effectiveOut);
            }
            break;
        }
    }

    /**
     * Start a virtual watcher thread that polls the given RMI socket liveness every 2 seconds and interrupts the caller
     * thread as soon as the client disconnects.
     *
     * @param socket
     *            the RMI server-side socket to monitor
     * @param callerThread
     *            the thread to interrupt when the connection is lost
     *
     * @return the watcher thread (already started); caller must interrupt it when done
     */
    private static Thread startConnectionWatcher(final Socket socket, final Thread callerThread) {
        return Thread.ofVirtual().name("ectrans-list-watcher").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (!InterruptibleRMIServerSocket.isCurrentRMIServerThreadSocketAlive(socket)) {
                    _log.debug("RMI client disconnected — interrupting listing thread");
                    callerThread.interrupt();
                    return;
                }
                try {
                    Thread.sleep(2000);
                } catch (final InterruptedException e) {
                    return;
                }
            }
        });
    }

    /**
     * Gets the list as a string array.
     *
     * @return the list
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public String[] getListAsStringArray() throws ECtransException {
        if (type != TYPE.STRING_ARRAY || listAsStringArray == null) {
            // The list was not set so either the exec failed or was not called!
            throw new ECtransException("List failed");
        }
        return listAsStringArray;
    }

    /**
     * Gets the list as a GZIPed byte array.
     *
     * @return the list
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public byte[] getListAsByteArray() throws ECtransException {
        if (type != TYPE.GZIPPED_BYTE_ARRAY || listAsByteArray == null) {
            // The list was not set so either the exec failed or was not called!
            throw new ECtransException("List failed");
        }
        return listAsByteArray;
    }
}
