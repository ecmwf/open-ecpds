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
 * Decorator for an RMI client socket to support interruptible RMI.
 * This socket registers with {@link InterruptibleRMIThread} on each RMI I/O operation.
 *
 * @author neilotoole@apache.org
 * @see InterruptibleRMIThreadFactory#newThread(Runnable)
 */

import java.net.Socket;

/**
 * The Class InterruptibleRMIClientSocket.
 */
public final class InterruptibleRMIClientSocket extends InterruptibleRMISocket {

	/**
	 * Instantiates a new interruptible RMI client socket.
	 *
	 * @param decoratee the decoratee
	 */
	public InterruptibleRMIClientSocket(final Socket decoratee) {
		super(decoratee);
	}

	/**
	 * Called by {@link InterruptibleRMISocketInputStream} and
	 * {@link InterruptibleRMISocketOutputStream} before the thread enters an RMI IO
	 * operation.
	 */
	@Override
	void ioStarting() {
		// Register this socket with the current thread if it's an
		// InterruptibleRMIThread
		if (Thread.currentThread() instanceof final InterruptibleRMIThread thread) {
			thread.registerSocketInIO(this);
		}
	}

	/**
	 * Called by {@link InterruptibleRMISocketInputStream} and
	 * {@link InterruptibleRMISocketOutputStream} after the thread exits an RMI IO
	 * operation.
	 */
	@Override
	void ioEnding() {
		if (Thread.currentThread() instanceof final InterruptibleRMIThread thread) {
			thread.unregisterSocketInIO();
		}
	}
}
