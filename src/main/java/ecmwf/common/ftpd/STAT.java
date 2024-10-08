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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

/**
 * The Class STAT.
 */
final class STAT {
    /**
     * Instantiates a new stat.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public STAT(final CurrentContext currentContext, final String parameter) {
        currentContext.respond("211- FTP Server\n");
        currentContext.respond("211- Connected to " + currentContext.remoteIP);
        currentContext.respond("211- Connected from " + currentContext.remoteSite);
        currentContext.respond("211- Logged in as " + currentContext.user);
        if (isNotEmpty(currentContext.clientName)) {
            currentContext.respond("211- Logged in with " + currentContext.clientName);
        }
        currentContext.respond("211 End of status.");
    }
}
