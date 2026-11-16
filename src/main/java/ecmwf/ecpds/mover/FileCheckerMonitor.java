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

package ecmwf.ecpds.mover;

import java.net.InetAddress;

import ecmwf.common.monitor.MonitorCallback;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.monitor.MonitorThread;
import ecmwf.common.technical.Cnf;

/**
 * The Class FileCheckerMonitor.
 */
final class FileCheckerMonitor implements MonitorCallback {
    /**
     * Start.
     */
    public void start() {
        if (MonitorManager.isActivated()) {
            try {
                MonitorThread.getInstance().subscribe("FileSystem", this);
            } catch (final MonitorException e) {
                MoverServer._log.debug(e);
            }
        }
    }

    /**
     * Stop.
     */
    public void stop() {
        if (MonitorManager.isActivated()) {
            try {
                MonitorThread.getInstance().unSubscribe("FileSystem");
            } catch (final MonitorException e) {
                MoverServer._log.debug(e);
            }
        }
    }

    /**
     * Gets the monitor manager.
     *
     * @param name
     *            the name
     *
     * @return the monitor manager
     *
     * @throws MonitorException
     *             the monitor exception
     */
    @Override
    public MonitorManager getMonitorManager(final String name) throws MonitorException {
        final var hasException = FileChecker.hasException();
        final var path = FileChecker.path;
        // The name is always FileSystem!
        try {
            return new MonitorManager(
                    Cnf.at("Login", "service", Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName())),
                    name, hasException ? MonitorManager.RED : MonitorManager.GREEN,
                    hasException ? path != null ? path : "WARNING" : "OK");
        } catch (final Throwable t) {
            throw new MonitorException(t.getMessage());
        }
    }
}
