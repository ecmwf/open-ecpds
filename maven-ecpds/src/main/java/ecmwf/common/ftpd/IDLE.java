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

package ecmwf.common.ftpd;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.net.SocketException;

/**
 * The Class IDLE.
 */
final class IDLE {
    /**
     * Instantiates a new idle.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public IDLE(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, IDLE.class, parameter)) == null) {
            return;
        }
        try {
            final var idleTime = Integer.parseInt(parameter);
            if (idleTime > 0) // Don't allow infinite or negative timeouts.
            {
                try {
                    currentContext.clientSocket.setSoTimeout(idleTime);
                    currentContext.respond(200, "IDLE set to " + idleTime);
                } catch (final SocketException e) {
                    currentContext.respond(500, "Set SO timeout error", e);
                }
            } else {
                currentContext.respond(501, "IDLE parse error");
            }
        } catch (final NumberFormatException e) {
            currentContext.respond(501, "IDLE parse error");
        }
    }
}
