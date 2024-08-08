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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;
import com.google.common.net.PercentEscaper;

import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class AwsSignature.
 */
final class AwsSignature {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(AwsSignature.class);

    /** The Constant AWS_URL_PARAMETER_ESCAPER. */
    private static final PercentEscaper AWS_URL_PARAMETER_ESCAPER = new PercentEscaper("-_.~", false);

    /** The Constant SIGNED_SUBRESOURCES. */
    private static final Set<String> SIGNED_SUBRESOURCES = ImmutableSet.of("acl", "delete", "lifecycle", "location",
            "logging", "notification", "partNumber", "policy", "requestPayment", "response-cache-control",
            "response-content-disposition", "response-content-encoding", "response-content-language",
            "response-content-type", "response-expires", "torrent", "uploadId", "uploads", "versionId", "versioning",
            "versions", "website");

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /**
     * Instantiates a new aws signature.
     */
    private AwsSignature() {
    }

    /**
     * Create Amazon V2 signature. Reference: http://docs.aws.amazon.com/general/latest/gr/signature-version-2.html
     *
     * @param request
     *            the request
     * @param uri
     *            the uri
     * @param identity
     *            the identity
     * @param queryAuth
     *            the query auth
     * @param bothDateHeader
     *            the both date header
     *
     * @return the string
     */
    static String createAuthorizationSignature(final HttpServletRequest request, final String uri,
            final String identity, final boolean queryAuth, final boolean bothDateHeader) {
        // sort Amazon headers
        final SortedSetMultimap<String, String> canonicalizedHeaders = TreeMultimap.create();
        for (String headerName : Collections.list(request.getHeaderNames())) {
            final Collection<String> headerValues = Collections.list(request.getHeaders(headerName));
            headerName = headerName.toLowerCase();
            if (!headerName.startsWith("x-amz-")
                    || bothDateHeader && AwsHttpHeaders.DATE.equalsIgnoreCase(headerName)) {
                continue;
            }
            if (headerValues.isEmpty()) {
                canonicalizedHeaders.put(headerName, "");
            }
            for (final String headerValue : headerValues) {
                canonicalizedHeaders.put(headerName, Strings.nullToEmpty(headerValue));
            }
        }

        // Build string to sign
        final var builder = new StringBuilder().append(request.getMethod()).append('\n')
                .append(Strings.nullToEmpty(request.getHeader(HttpHeaders.CONTENT_MD5))).append('\n')
                .append(Strings.nullToEmpty(request.getHeader(HttpHeaders.CONTENT_TYPE))).append('\n');
        final var expires = request.getParameter("Expires");
        if (queryAuth) {
            // If expires is not nil, then it is query string sign
            // If expires is nil, maybe also query string sign
            // So should check other accessid param, presign to judge.
            // not the expires
            builder.append(Strings.nullToEmpty(expires));
        } else if (!bothDateHeader) {
            if (canonicalizedHeaders.containsKey(AwsHttpHeaders.DATE)) {
                builder.append("");
            } else {
                builder.append(request.getHeader(HttpHeaders.DATE));
            }
        } else {
            if (!canonicalizedHeaders.containsKey(AwsHttpHeaders.DATE)) {
                builder.append(request.getHeader(AwsHttpHeaders.DATE));
            } else {
                // panic
            }
        }

        builder.append('\n');
        for (final Map.Entry<String, String> entry : canonicalizedHeaders.entries()) {
            builder.append(entry.getKey()).append(':').append(entry.getValue()).append('\n');
        }
        builder.append(uri);

        var separator = '?';
        final List<String> subresources = Collections.list(request.getParameterNames());
        Collections.sort(subresources);
        for (final String subresource : subresources) {
            if (SIGNED_SUBRESOURCES.contains(subresource)) {
                builder.append(separator).append(subresource);

                final var value = request.getParameter(subresource);
                if (!"".equals(value)) {
                    builder.append('=').append(value);
                }
                separator = '&';
            }
        }

        final var stringToSign = builder.toString();
        logger.trace("stringToSign: {}", stringToSign);

        // Sign string
        try {
            return BaseEncoding.base64().encode(getS3AuthorizationSignature(identity, "", stringToSign, "HmacSHA1"));
        } catch (RemoteException | ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the s 3 authorization signature.
     *
     * @param incomingUser
     *            the incoming user
     * @param prefix
     *            the prefix
     * @param data
     *            the data
     * @param algorithm
     *            the algorithm
     *
     * @return the s 3 authorization signature
     *
     * @throws RemoteException
     *             the remote exception
     * @throws ConnectionException
     *             the connection exception
     */
    private static byte[] getS3AuthorizationSignature(final String incomingUser, final String prefix, final String data,
            final String algorithm) throws RemoteException, ConnectionException {
        return _mover.getMasterInterface().getS3AuthorizationSignature(incomingUser, prefix, data, algorithm);
    }

    /**
     * Sign message.
     *
     * @param data
     *            the data
     * @param key
     *            the key
     * @param algorithm
     *            the algorithm
     *
     * @return the byte[]
     *
     * @throws InvalidKeyException
     *             the invalid key exception
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    private static byte[] signMessage(final byte[] data, final byte[] key, final String algorithm)
            throws InvalidKeyException, NoSuchAlgorithmException {
        final var mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data);
    }

    /**
     * Gets the message digest.
     *
     * @param payload
     *            the payload
     * @param algorithm
     *            the algorithm
     *
     * @return the message digest
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    private static String getMessageDigest(final byte[] payload, final String algorithm)
            throws NoSuchAlgorithmException {
        final var md = MessageDigest.getInstance(algorithm);
        final var hash = md.digest(payload);
        return BaseEncoding.base16().lowerCase().encode(hash);
    }

    /**
     * Extract signed headers.
     *
     * @param authorization
     *            the authorization
     *
     * @return the list
     */
    private static List<String> extractSignedHeaders(final String authorization) {
        final var index = authorization.indexOf("SignedHeaders=");
        if (index < 0) {
            return null;
        }
        final var endSigned = authorization.indexOf(',', index);
        if (endSigned < 0) {
            return null;
        }
        final var startHeaders = authorization.indexOf('=', index);
        return Splitter.on(';').splitToList(authorization.substring(startHeaders + 1, endSigned));
    }

    /**
     * Builds the canonical headers.
     *
     * @param request
     *            the request
     * @param signedHeaders
     *            the signed headers
     *
     * @return the string
     */
    private static String buildCanonicalHeaders(final HttpServletRequest request, final List<String> signedHeaders) {
        final List<String> headers = new ArrayList<>();
        for (final String header : signedHeaders) {
            headers.add(header.toLowerCase());
        }
        Collections.sort(headers);
        final List<String> headersWithValues = new ArrayList<>();
        for (final String header : headers) {
            final List<String> values = new ArrayList<>();
            final var headerWithValue = new StringBuilder();
            headerWithValue.append(header);
            headerWithValue.append(":");
            for (String value : Collections.list(request.getHeaders(header))) {
                value = value.trim();
                if (!value.startsWith("\"")) {
                    value = value.replaceAll("\\s+", " ");
                }
                values.add(value);
            }
            headerWithValue.append(Joiner.on(",").join(values));
            headersWithValues.add(headerWithValue.toString());
        }

        return Joiner.on("\n").join(headersWithValues);
    }

    /**
     * Builds the canonical query string.
     *
     * @param request
     *            the request
     *
     * @return the string
     *
     * @throws UnsupportedEncodingException
     *             the unsupported encoding exception
     */
    private static String buildCanonicalQueryString(final HttpServletRequest request)
            throws UnsupportedEncodingException {
        // The parameters are required to be sorted
        final List<String> parameters = Collections.list(request.getParameterNames());
        Collections.sort(parameters);
        final List<String> queryParameters = new ArrayList<>();

        for (final String key : parameters) {
            if ("X-Amz-Signature".equals(key)) {
                continue;
            }
            // re-encode keys and values in AWS normalized form
            final var value = request.getParameter(key);
            queryParameters.add(AWS_URL_PARAMETER_ESCAPER.escape(key) + "=" + AWS_URL_PARAMETER_ESCAPER.escape(value));
        }
        return Joiner.on("&").join(queryParameters);
    }

    /**
     * Creates the canonical request.
     *
     * @param request
     *            the request
     * @param uri
     *            the uri
     * @param payload
     *            the payload
     * @param hashAlgorithm
     *            the hash algorithm
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    private static String createCanonicalRequest(final HttpServletRequest request, final String uri,
            final byte[] payload, final String hashAlgorithm) throws IOException, NoSuchAlgorithmException {
        final var authorizationHeader = request.getHeader("Authorization");
        var xAmzContentSha256 = request.getHeader(AwsHttpHeaders.CONTENT_SHA256);
        if (xAmzContentSha256 == null) {
            xAmzContentSha256 = request.getParameter("X-Amz-SignedHeaders");
        }
        final String digest;
        if (authorizationHeader == null) {
            digest = "UNSIGNED-PAYLOAD";
        } else if ("STREAMING-AWS4-HMAC-SHA256-PAYLOAD".equals(xAmzContentSha256)) {
            digest = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD";
        } else if ("UNSIGNED-PAYLOAD".equals(xAmzContentSha256)) {
            digest = "UNSIGNED-PAYLOAD";
        } else {
            digest = getMessageDigest(payload, hashAlgorithm);
        }
        final List<String> signedHeaders;
        if (authorizationHeader != null) {
            signedHeaders = extractSignedHeaders(authorizationHeader);
        } else {
            signedHeaders = Splitter.on(';').splitToList(request.getParameter("X-Amz-SignedHeaders"));
        }

        /*
         * CORS Preflight
         *
         * The signature is based on the canonical request, which includes the HTTP Method. For presigned URLs, the
         * method must be replaced for OPTIONS request to match
         */
        var method = request.getMethod();
        if ("OPTIONS".equals(method)) {
            final var corsMethod = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
            if (corsMethod != null) {
                method = corsMethod;
            }
        }

        final var canonicalRequest = Joiner.on("\n").join(method, uri, buildCanonicalQueryString(request),
                buildCanonicalHeaders(request, signedHeaders) + "\n", Joiner.on(';').join(signedHeaders), digest);

        return getMessageDigest(canonicalRequest.getBytes(StandardCharsets.UTF_8), hashAlgorithm);
    }

    /**
     * Create v4 signature. Reference: http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html
     *
     * @param request
     *            the request
     * @param authHeader
     *            the auth header
     * @param payload
     *            the payload
     * @param uri
     *            the uri
     * @param identity
     *            the identity
     *
     * @return the string
     *
     * @throws InvalidKeyException
     *             the invalid key exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws S3Exception
     *             the s 3 exception
     * @throws ConnectionException
     *             the connection exception
     */
    static String createAuthorizationSignatureV4(final HttpServletRequest request,
            final S3AuthorizationHeader authHeader, final byte[] payload, final String uri, final String identity)
            throws InvalidKeyException, IOException, NoSuchAlgorithmException, S3Exception, ConnectionException {
        final var canonicalRequest = createCanonicalRequest(request, uri, payload, authHeader.hashAlgorithm);
        final var algorithm = authHeader.hmacAlgorithm;
        final var dateKey = getS3AuthorizationSignature(identity, "AWS4", authHeader.date, algorithm);
        final var dateRegionKey = signMessage(authHeader.region.getBytes(StandardCharsets.UTF_8), dateKey, algorithm);
        final var dateRegionServiceKey = signMessage(authHeader.service.getBytes(StandardCharsets.UTF_8), dateRegionKey,
                algorithm);
        final var signingKey = signMessage("aws4_request".getBytes(StandardCharsets.UTF_8), dateRegionServiceKey,
                algorithm);
        var date = request.getHeader(AwsHttpHeaders.DATE);
        if (date == null) {
            date = request.getParameter("X-Amz-Date");
        }
        final var signatureString = "AWS4-HMAC-SHA256\n" + date + "\n" + authHeader.date + "/" + authHeader.region
                + "/s3/aws4_request\n" + canonicalRequest;
        final var signature = signMessage(signatureString.getBytes(StandardCharsets.UTF_8), signingKey, algorithm);
        return BaseEncoding.base16().lowerCase().encode(signature);
    }
}
