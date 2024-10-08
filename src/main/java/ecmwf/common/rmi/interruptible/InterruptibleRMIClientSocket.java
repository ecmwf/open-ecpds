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

package ecmwf.common.rmi.interruptible;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Decorator for an RMI client socket to support interruptible RMI.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.net.Socket;

/**
 * The Class InterruptibleRMIClientSocket.
 */
public final class InterruptibleRMIClientSocket extends InterruptibleRMISocket {

    /**
     * Create a decorator for the given socket.
     *
     * @param decoratee
     *            the decoratee
     */
    public InterruptibleRMIClientSocket(final Socket decoratee) {
        super(decoratee);
    }

    /**
     * Called by {@link InterruptibleRMISocketInputStream} and {@link InterruptibleRMISocketOutputStream} before the
     * thread enters an RMI IO operation.
     */
    @Override
    void ioStarting() {
        if (Thread.currentThread() instanceof final InterruptibleRMIThread interruptibleRMIThread) {
            interruptibleRMIThread.registerSocketInIO(this);
        }
    }

    /**
     * Called by {@link InterruptibleRMISocketInputStream} and {@link InterruptibleRMISocketOutputStream} after the
     * thread exits an RMI IO operation.
     */
    @Override
    void ioEnding() {
        if (Thread.currentThread() instanceof final InterruptibleRMIThread interruptibleRMIThread) {
            interruptibleRMIThread.unregisterSocketInIO();
        }
    }
}
