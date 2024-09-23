/*
 * Copyright 2014-2020 Andrew Gaul <andrew@gaul.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ecmwf.ecpds.mover.plugin.http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/** Jetty-specific handler for S3 requests. */
final class S3ProxyHandlerJetty extends AbstractHandler {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(S3ProxyHandlerJetty.class);

    /** The handler. */
    private final S3ProxyHandler handler;

    /**
     * Instantiates a new s 3 proxy handler jetty.
     *
     * @param authenticationType
     *            the authentication type
     * @param v4MaxNonChunkedRequestSize
     *            the v 4 max non chunked request size
     * @param ignoreUnknownHeaders
     *            the ignore unknown headers
     * @param corsRules
     *            the cors rules
     * @param servicePath
     *            the service path
     * @param maximumTimeSkew
     *            the maximum time skew
     */
    S3ProxyHandlerJetty(final AuthenticationType authenticationType, final long v4MaxNonChunkedRequestSize,
            final boolean ignoreUnknownHeaders, final CrossOriginResourceSharing corsRules, final String servicePath,
            final int maximumTimeSkew) {
        handler = new S3ProxyHandler(authenticationType, v4MaxNonChunkedRequestSize, ignoreUnknownHeaders, corsRules,
                servicePath, maximumTimeSkew);
    }

    /**
     * Send S 3 exception.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param se
     *            the se
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void sendS3Exception(final HttpServletRequest request, final HttpServletResponse response,
            final S3Exception se) throws IOException {
        handler.sendSimpleErrorResponse(request, response, se.getError(), se.getMessage(), se.getElements());
    }

    /** Request attributes. */
    public static final String ATTRIBUTE_QUERY_ENCODING = "queryEncoding";

    /**
     * {@inheritDoc}
     *
     * Handle.
     */
    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        try (InputStream is = request.getInputStream()) {
            baseRequest.setAttribute(ATTRIBUTE_QUERY_ENCODING, baseRequest.getQueryEncoding());
            handler.doHandle(baseRequest, request, response, is);
            baseRequest.setHandled(true);
        } catch (final IllegalArgumentException iae) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, iae.getMessage());
            baseRequest.setHandled(true);
            return;
        } catch (final S3Exception se) {
            sendS3Exception(request, response, se);
            baseRequest.setHandled(true);
            return;
        } catch (final UnsupportedOperationException uoe) {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, uoe.getMessage());
            baseRequest.setHandled(true);
            return;
        } catch (final Throwable throwable) {
            logger.debug("Unknown exception:", throwable);
            throw throwable;
        }
    }
}
