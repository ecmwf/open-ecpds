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

package ecmwf.ecpds.mover.service;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;

/**
 * Shared HMAC-SHA256 request signing used to authenticate the internal "mover/*" and "master/*" REST control channel,
 * over which a Proxy/Continental Data Mover relays instructions to/from the Master Server through a regular Data Mover
 * ({@link RESTClient} on the calling side, {@link RESTAuthFilter} on the receiving side). This is on the same HTTPS
 * port used by end-users to download data, so without this signature it would otherwise be reachable by anyone who can
 * connect to that port.
 *
 * Configured via the {@code [Security] rccSharedSecret} option ("RCC" = REST Control Channel), distributed to every
 * Data Mover and Proxy/Continental Data Mover. This is a separate secret from {@code [Security] cliSharedSecret}, used
 * by {@link ecmwf.common.plugin.SimplePlugin} to authenticate the unrelated `ecpds` CLI ↔ Master Server channel. For
 * backward compatibility, if the secret is left unset (the default), the control channel remains
 * unsigned/unauthenticated exactly as before this was introduced - a warning is logged in that case to make the
 * operator aware.
 */
final class RESTSignature {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RESTSignature.class);

    /** The Constant SECRET. */
    private static final String SECRET = Cnf.at("Security", "rccSharedSecret", "");

    /** The Constant ENABLED. True if a shared secret is configured, so signing/verification is active. */
    static final boolean ENABLED = !SECRET.isEmpty();

    /** The Constant TIMESTAMP_HEADER. */
    static final String TIMESTAMP_HEADER = "X-ECPDS-Timestamp";

    /** The Constant SIGNATURE_HEADER. */
    static final String SIGNATURE_HEADER = "X-ECPDS-Signature";

    /** The Constant MAX_SKEW_MILLIS. Maximum allowed age (also acting as a replay window) of a signed request. */
    static final long MAX_SKEW_MILLIS = Cnf.durationAt("Security", "controlChannelMaxSkew", 5 * 60 * 1000L);

    static {
        if (!ENABLED) {
            _log.warn(
                    "[Security] rccSharedSecret is not set: the mover/master REST control channel is NOT authenticated "
                            + "(any client able to reach this Data Mover's data portal port could invoke it). Set "
                            + "[Security] rccSharedSecret (same value on every Data Mover, Proxy Host and Master Server) "
                            + "to enable HMAC request signing on this channel.");
        }
    }

    private RESTSignature() {
        // Utility class
    }

    /**
     * Computes the HMAC-SHA256 signature (Base64 encoded) covering the given request method, path (with query string,
     * if any), timestamp and body, so that any change to any of these invalidates the signature.
     *
     * @param method
     *            the HTTP method (e.g. "PUT", "GET", "DELETE")
     * @param pathAndQuery
     *            the raw request path, including the leading "/ecpds/..." context and query string if present
     * @param timestamp
     *            the epoch milliseconds at which the request was signed
     * @param body
     *            the raw request body bytes (empty array if there is no body)
     *
     * @return the base64-encoded signature
     *
     * @throws GeneralSecurityException
     *             if the HMAC could not be computed
     */
    static String sign(final String method, final String pathAndQuery, final long timestamp, final byte[] body)
            throws GeneralSecurityException {
        final var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update(method.getBytes(StandardCharsets.UTF_8));
        mac.update((byte) '\n');
        mac.update(pathAndQuery.getBytes(StandardCharsets.UTF_8));
        mac.update((byte) '\n');
        mac.update(Long.toString(timestamp).getBytes(StandardCharsets.UTF_8));
        mac.update((byte) '\n');
        mac.update(body);
        return Base64.getEncoder().encodeToString(mac.doFinal());
    }
}
