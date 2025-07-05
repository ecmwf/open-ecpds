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
 * @author neilotoole@apache.org
 * @see InterruptibleRMIThreadFactory#newThread(Runnable)
 */

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Class InterruptibleMonitor.
 */
final class InterruptibleMonitor extends Thread {

	/** The rmi socket. */
	private final Socket rmiSocket;

	/** The closed. */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/** The loop. */
	private volatile boolean loop = true;

	/**
	 * Instantiates a new interruptible monitor.
	 *
	 * @param rmiSocket the rmi socket
	 */
	public InterruptibleMonitor(final Socket rmiSocket) {
		this.rmiSocket = rmiSocket;
	}

	/**
	 * Checks if is closed.
	 *
	 * @return true, if is closed
	 */
	public boolean isClosed() {
		return closed.get();
	}

	/**
	 * Sets the loop.
	 *
	 * @param loop the new loop
	 */
	public void setLoop(final boolean loop) {
		this.loop = loop;
	}

	/**
	 * Execute.
	 */
	public void execute() {
		start();
	}

	/**
	 * Run.
	 */
	@Override
	public void run() {
		while (loop && !closed.get()) {
			if (!InterruptibleRMIServerSocket.isCurrentRMIServerThreadSocketAlive(rmiSocket)) {
				closed.set(true);
			}
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException _) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}
}
