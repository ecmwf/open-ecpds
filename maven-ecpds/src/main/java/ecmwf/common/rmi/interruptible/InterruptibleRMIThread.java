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
 * The Class InterruptibleRMIThread.
 */
public final class InterruptibleRMIThread extends Thread {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(InterruptibleRMIThread.class);

    /** The rmi socket. */
    private Socket rmiSocket;

    /**
     * Instantiates a new interruptible RMI thread.
     *
     * @param target
     *            the target
     *
     * @see Thread#Thread(java.lang.String)
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
     *
     * @see Thread#Thread(java.lang.String)
     */
    public InterruptibleRMIThread(final ThreadGroup group, final Runnable runnable, final String name,
            final long stackSize) {
        super(group, runnable, name, stackSize);
    }

    /**
     * Register the supplied socket as currently being in an IO operation.
     *
     * @param socket
     *            the socket
     */
    synchronized void registerSocketInIO(final InterruptibleRMIClientSocket socket) {
        rmiSocket = socket;
    }

    /**
     * Unregister the socket currently registered as being in an IO operation.
     */
    synchronized void unregisterSocketInIO() {
        rmiSocket = null;
    }

    /**
     * Interrupt this thread, even if it is in a blocking RMI IO operation. This method first invokes the superclass's
     * #interrupt method. Then, if an RMI socket has been registered as being in IO for this thread, a special marker
     * (like an EOF) is written to the socket (to help the server side shutdown its threads), and then the RMI socket is
     * directly closed. Afterwards, this thread's interrupt status will have been set.
     *
     * @see Thread#interrupt()
     */
    @Override
    public synchronized void interrupt() {
        try {
            super.interrupt();
        } finally {
            if (rmiSocket != null) {
                _log.debug("Interrupting RMI Thread in IO operation ({})", rmiSocket);
                try {
                    final var out = rmiSocket.getOutputStream();
                    out.write(InterruptibleRMISocket.SHUTDOWN_SOCKET);
                    out.flush();
                    out.close();
                    rmiSocket.close();
                } catch (final IOException e) {
                    _log.warn("Closing Socket connection", e);
                } finally {
                    rmiSocket = null;
                }
            } else {
                _log.debug("RMI Thread NOT in IO operation");
            }
        }
    }
}
