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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.rmi.interruptible.InterruptibleRMIServerSocket.isCurrentRMIServerThreadSocketAlive;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;

/**
 * The Class InterruptibleMonitor.
 */
final class InterruptibleMonitor extends ConfigurableLoopRunnable {
    /** The _handler. */
    private final Socket _rmiSocket;

    /** The _closed. */
    private final AtomicBoolean _closed = new AtomicBoolean(false);

    /**
     * Instantiates a new progress thread.
     *
     * @param rmiSocket
     *            the rmi socket
     */
    InterruptibleMonitor(final Socket rmiSocket) {
        setPause(2 * Timer.ONE_SECOND);
        _rmiSocket = rmiSocket;
    }

    /**
     * Checks if is closed.
     *
     * @return true, if is closed
     */
    public boolean isClosed() {
        return _closed.get();
    }

    /**
     * Configurable loop run.
     */
    @Override
    public void configurableLoopRun() {
        if (!isCurrentRMIServerThreadSocketAlive(_rmiSocket)) {
            _closed.set(true);
        }
    }
}