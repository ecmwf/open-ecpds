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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Verifies the HMAC-SHA256 signature ({@link RESTSignature}) that {@link RESTClient} attaches to every "mover/*" and
 * "master/*" control-channel request. This channel is served by the very same {@code RESTServer} JAX-RS resource - and
 * therefore the same HTTPS port and servlet context ("/ecpds") - as the end-user facing data portal paths ("home/",
 * "file/", "data/...", "register", ...), which have their own, separate, authentication and are deliberately left
 * untouched by this filter (only paths starting with "mover/" or "master/" are checked).
 *
 * If {@code [Security] sharedSecret} is not configured ({@link RESTSignature#ENABLED} is {@code false}), this filter is
 * a no-op and the control channel remains reachable without authentication, exactly as it was before this filter was
 * introduced - this preserves backward compatibility for existing deployments, but is strongly discouraged: a startup
 * warning is logged by {@link RESTSignature} in that case.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public final class RESTAuthFilter implements ContainerRequestFilter {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RESTAuthFilter.class);

    /**
     * {@inheritDoc}
     *
     * Filter.
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {
        final var path = requestContext.getUriInfo().getPath(); // e.g. "mover/del" - relative to the "/ecpds" context
        if (!(path.startsWith("mover/") || path.startsWith("master/"))) {
            return; // Not a control-channel endpoint: end-user facing paths have their own authentication.
        }
        if (!RESTSignature.ENABLED) {
            return; // No shared secret configured: preserve pre-existing (unauthenticated) behaviour.
        }
        final var timestampHeader = requestContext.getHeaderString(RESTSignature.TIMESTAMP_HEADER);
        final var signatureHeader = requestContext.getHeaderString(RESTSignature.SIGNATURE_HEADER);
        if (timestampHeader == null || signatureHeader == null) {
            reject(requestContext, "Missing signature headers");
            return;
        }
        final long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader);
        } catch (final NumberFormatException e) {
            reject(requestContext, "Invalid timestamp header");
            return;
        }
        if (Math.abs(System.currentTimeMillis() - timestamp) > RESTSignature.MAX_SKEW_MILLIS) {
            reject(requestContext, "Timestamp outside of the allowed window");
            return;
        }
        final var body = readAndResetBody(requestContext);
        final var requestUri = requestContext.getUriInfo().getRequestUri();
        final var pathAndQuery = requestUri.getRawPath()
                + (requestUri.getRawQuery() != null ? "?" + requestUri.getRawQuery() : "");
        try {
            final var expected = RESTSignature.sign(requestContext.getMethod(), pathAndQuery, timestamp, body);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8))) {
                reject(requestContext, "Signature mismatch");
            }
        } catch (final Exception e) {
            _log.error("Error verifying control-channel request signature", e);
            reject(requestContext, "Internal error verifying signature");
        }
    }

    /**
     * Fully reads the (typically small, JSON) request entity so it can be included in the signature check, then
     * restores it as the request's entity stream so the JAX-RS body reader can still deserialize it downstream.
     *
     * @param requestContext
     *            the request context
     *
     * @return the raw request body bytes (empty array if there is no body)
     */
    private static byte[] readAndResetBody(final ContainerRequestContext requestContext) {
        try {
            final var in = requestContext.getEntityStream();
            final var bytes = in == null ? new byte[0] : in.readAllBytes();
            requestContext.setEntityStream(new ByteArrayInputStream(bytes));
            return bytes;
        } catch (final Exception e) {
            _log.warn("Could not read control-channel request body for signature verification", e);
            return new byte[0];
        }
    }

    /**
     * Aborts the request with a 401 response and logs the reason.
     *
     * @param requestContext
     *            the request context
     * @param reason
     *            the reason for the rejection
     */
    private static void reject(final ContainerRequestContext requestContext, final String reason) {
        _log.warn("Rejecting control-channel request {} {}: {}", requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(), reason);
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(reason).build());
    }
}
