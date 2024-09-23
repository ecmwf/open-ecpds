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

package ecmwf.common.technical;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class IPAddress.
 *
 * @author root
 */
public final class IPAddress {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(IPAddress.class);

    /** The VALI d_ ip v4_ pattern. */
    private static Pattern VALID_IPV4_PATTERN = null;

    /** The VALI d_ ip v6_ pattern. */
    private static Pattern VALID_IPV6_PATTERN = null;

    /** The Constant ipv4Pattern. */
    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";

    /** The Constant ipv6Pattern. */
    private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    static {
        try {
            VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
            VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
        } catch (final PatternSyntaxException e) {
            _log.error("Unable to compile pattern", e);
        }
    }

    /**
     * Checks if it is a valid IP address (IPV4 or IPV6)?.
     *
     * @param IPAddress
     *            the IP address
     *
     * @return true, if is a valid IP address
     */
    public static boolean isIPAddress(final String IPAddress) {
        final var m1 = VALID_IPV4_PATTERN.matcher(IPAddress);
        if (m1.matches()) {
            return true;
        }
        final var m2 = VALID_IPV6_PATTERN.matcher(IPAddress);
        return m2.matches();
    }
}
