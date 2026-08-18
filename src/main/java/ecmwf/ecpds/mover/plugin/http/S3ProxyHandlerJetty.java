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

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;

/** Jetty-specific servlet bridge for S3 requests. */
final class S3ProxyHandlerJetty extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(S3ProxyHandlerJetty.class);

    /** Request attributes. */
    public static final String ATTRIBUTE_QUERY_ENCODING = "queryEncoding";

    /** The handler. */
    private final S3ProxyHandler handler;

    S3ProxyHandlerJetty(final AuthenticationType authenticationType, final long v4MaxNonChunkedRequestSize,
            final boolean ignoreUnknownHeaders, final CrossOriginResourceSharing corsRules, final String servicePath,
            final int maximumTimeSkew) {
        handler = new S3ProxyHandler(authenticationType, v4MaxNonChunkedRequestSize, ignoreUnknownHeaders, corsRules,
                servicePath, maximumTimeSkew);
    }

    private void sendS3Exception(final HttpServletRequest request, final HttpServletResponse response,
            final S3Exception se) throws IOException {
        handler.sendSimpleErrorResponse(request, response, se.getError(), se.getMessage(), se.getElements());
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final var baseRequest = ServletContextRequest.getServletContextRequest(request);
        try (InputStream is = request.getInputStream()) {
            if (baseRequest != null) {
                request.setAttribute(ATTRIBUTE_QUERY_ENCODING, baseRequest.getQueryEncoding());
            }
            handler.doHandle(request, request, response, is);
        } catch (final IllegalArgumentException iae) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, iae.getMessage());
        } catch (final S3Exception se) {
            sendS3Exception(request, response, se);
        } catch (final UnsupportedOperationException uoe) {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, uoe.getMessage());
        } catch (final Throwable throwable) {
            logger.debug("Unknown exception:", throwable);
            throw throwable;
        }
    }
}
