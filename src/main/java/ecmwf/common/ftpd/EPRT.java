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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.net.InetAddress;
import java.util.StringTokenizer;

/**
 * The Class EPRT.
 */
final class EPRT {
    /**
     * Instantiates a new eprt.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public EPRT(final CurrentContext currentContext, final String parameter) {
        final var st = new StringTokenizer(parameter, " |");
        if (st.countTokens() != 3) {
            currentContext.respond(501, "EPRT parse error");
            return;
        }
        try {
            Integer.parseInt(st.nextToken());
        } catch (final Throwable e) {
            currentContext.respond(500, "Bad protocol", e);
        }
        try {
            currentContext.dataIP = InetAddress.getByName(st.nextToken());
        } catch (final Throwable e) {
            currentContext.respond(500, "Bad IP address", e);
        }
        try {
            currentContext.dataPort = Integer.parseInt(st.nextToken());
        } catch (final Throwable e) {
            currentContext.respond(500, "Bad Port number", e);

            return;
        }
        currentContext.respond(200, "EPRT command successful. Consider using EPSV.");
    }
}
