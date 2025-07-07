/**
 * Copyright 2005 Neil O'Toole - neilotoole@apache.org Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package ecmwf.common.rmi.interruptible;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Tailored Neil O'Toole's original version to specific requirements.
 *
 * Extends Thread to provide support for interrupting the thread while the
 * thread is in a blocking RMI IO operation. Typically, threads that are in
 * blocking IO operations can't be interrupted. This implementation provides a
 * mechanism for an RMI client socket (created by
 * {@link org.neilja.net.interruptiblermi.InterruptibleRMISocketFactory#createSocket(String, int)}
 * ) to register when it enters blocking IO. When {@link #interrupt()} is called
 * on this thread, the associated RMI socket is shutdown and closed, thus
 * terminating the blocking IO operation. Use
 * {@link org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory#newThread(Runnable)}
 * to create instances of this thread class.
 *
 * @author neilotoole@apache.org
 * @see InterruptibleRMIThreadFactory#newThread(Runnable)
 */

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An RMI thread that can be forcefully interrupted via socket closure.
 */

public final class InterruptibleRMIThread extends Thread {

    /** The Constant LOG. */
    private static final Logger LOG = LogManager.getLogger(InterruptibleRMIThread.class);

    /** The rmi socket. */
    private volatile Socket rmiSocket;

    /**
     * Instantiates a new interruptible RMI thread.
     *
     * @param target
     *            the target
     */
    public InterruptibleRMIThread(final Runnable target) {
        super(target);
    }

    /**
     * Instantiates a new interruptible RMI thread.
     *
     * @param group
     *            the group
     * @param runnable
     *            the runnable
     * @param name
     *            the name
     * @param stackSize
     *            the stack size
     */
    public InterruptibleRMIThread(final ThreadGroup group, final Runnable runnable, final String name,
            final long stackSize) {
        super(group, runnable, name, stackSize);
    }

    /**
     * Register socket in IO.
     *
     * @param socket
     *            the socket
     */
    synchronized void registerSocketInIO(final InterruptibleRMIClientSocket socket) {
        this.rmiSocket = socket;
    }

    /**
     * Unregister socket in IO.
     */
    synchronized void unregisterSocketInIO() {
        this.rmiSocket = null;
    }

    /**
     * Interrupt.
     */
    @Override
    public void interrupt() {
        Socket socketToClose;

        // Take local copy to avoid holding lock during I/O
        synchronized (this) {
            socketToClose = this.rmiSocket;
            this.rmiSocket = null;
        }

        // First, call standard interrupt
        super.interrupt();

        if (socketToClose != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Interrupting RMI Thread in IO operation ({})", socketToClose);
            }
            try {
                socketToClose.shutdownInput(); // optionally signal clean shutdown
                socketToClose.shutdownOutput();
            } catch (final IOException _) {
                // Ignore, likely already shut down
            }
            try {
                socketToClose.close(); // This is the actual interrupt trigger
            } catch (final IOException e) {
                LOG.warn("Error closing RMI socket during interrupt", e);
            }
        } else {
            LOG.debug("RMI Thread NOT in IO operation");
        }
    }
}
