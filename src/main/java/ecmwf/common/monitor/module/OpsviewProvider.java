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

package ecmwf.common.monitor.module;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorInterface;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.opsview.OpsViewManager;

/**
 * The Class OpsviewProvider.
 */
public final class OpsviewProvider implements MonitorInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(OpsviewProvider.class);

    /**
     * Send message.
     *
     * @param hostname
     *            the hostname
     * @param service
     *            the service
     * @param status
     *            the status
     * @param message
     *            the message
     *
     * @throws MonitorException
     *             the monitor exception
     */
    @Override
    public void sendMessage(final String hostname, final String service, final int status, final String message)
            throws MonitorException {
        final var realHostname = hostname.startsWith(service + "/") ? hostname.substring((service + "/").length())
                : hostname;
        try {
            // In Opsview:
            // Status code '0' means that the Service Check is running successfully
            // Status code '1' means that the Service Check is in a warning state
            // Status code '2' means that the Service Check is in a critical state
            // Status code '3' means that the Service Check in an 'UNKNOWN' state.
            // Therefore, we have to convert BLUE to GREEN!
            // In Bologna, "DataMover/bodh1ecpdmv-02" should be translated into
            // "bodh1ecpdmv-02".
            OpsViewManager.detail(realHostname, service, status == MonitorManager.BLUE ? MonitorManager.GREEN : status,
                    message);
        } catch (final Throwable t) {
            _log.warn("Cannot send notification (hostname=" + realHostname + ",service=" + service + ",status=" + status
                    + ",message=" + message + ")", t);
        }
    }
}
