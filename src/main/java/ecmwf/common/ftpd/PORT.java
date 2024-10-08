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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * The Class PORT.
 */
final class PORT {
    /**
     * Instantiates a new port.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public PORT(final CurrentContext currentContext, final String parameter) {
        final var st = new StringTokenizer(parameter, " ,");
        if (st.countTokens() != 6) {
            currentContext.respond(501, "PORT parse error");
            return;
        }
        final var sb = new StringBuilder();
        sb.append(st.nextToken()).append('.');
        sb.append(st.nextToken()).append('.');
        sb.append(st.nextToken()).append('.');
        sb.append(st.nextToken());
        try {
            currentContext.dataIP = InetAddress.getByName(sb.toString());
        } catch (final UnknownHostException e) {
            currentContext.respond(500, "Bad IP address", e);
        }
        try {
            final var dp1 = Integer.parseInt(st.nextToken());
            final var dp2 = Integer.parseInt(st.nextToken());
            currentContext.dataPort = (dp1 << 8) + dp2;
        } catch (final NumberFormatException e) {
            currentContext.respond(500, "Bad Port number", e);
            return;
        }
        currentContext.respond(200, "PORT command successful. Consider using PASV.");
    }
}
