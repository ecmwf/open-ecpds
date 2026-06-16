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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import javax.xml.parsers.DocumentBuilderFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import com.google.common.net.PercentEscaper;

import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.common.technical.StreamPlugThread;

/**
 * HTTP server-independent handler for S3 requests.
 *
 * @author root
 */
public class S3ProxyHandler {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(S3ProxyHandler.class);

    /** The Constant AWS_XMLNS. */
    private static final String AWS_XMLNS = "http://s3.amazonaws.com/doc/2006-03-01/";

    /** The Constant USER_METADATA_PREFIX. */
    private static final String USER_METADATA_PREFIX = "x-amz-meta-";

    /** The Constant FAKE_OWNER_ID. */
    private static final String FAKE_OWNER_ID = "75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a";

    /** The Constant FAKE_OWNER_DISPLAY_NAME. */
    private static final String FAKE_OWNER_DISPLAY_NAME = "CustomersName@amazon.com";

    /** The Constant FAKE_REQUEST_ID. */
    private static final String FAKE_REQUEST_ID = "4442587FB7D0A2F9";

    /** The Constant VALID_BUCKET_FIRST_CHAR. */
    private static final CharMatcher VALID_BUCKET_FIRST_CHAR = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('A', 'Z')).or(CharMatcher.inRange('0', '9'));

    /** The Constant VALID_BUCKET. */
    private static final CharMatcher VALID_BUCKET = VALID_BUCKET_FIRST_CHAR.or(CharMatcher.is('.'))
            .or(CharMatcher.is('_')).or(CharMatcher.is('-'));

    /** The Constant UNSUPPORTED_PARAMETERS. */
    private static final Set<String> UNSUPPORTED_PARAMETERS = Set.of("accelerate", "analytics", "cors", "inventory",
            "lifecycle", "logging", "metrics", "notification", "replication", "requestPayment", "restore", "tagging",
            "torrent", "versions", "website");
    /** All supported x-amz- headers, except for x-amz-meta- user metadata. */
    private static final Set<String> SUPPORTED_X_AMZ_HEADERS = Set.of(AwsHttpHeaders.ACL, AwsHttpHeaders.CONTENT_SHA256,
            AwsHttpHeaders.COPY_SOURCE, AwsHttpHeaders.COPY_SOURCE_IF_MATCH,
            AwsHttpHeaders.COPY_SOURCE_IF_MODIFIED_SINCE, AwsHttpHeaders.COPY_SOURCE_IF_NONE_MATCH,
            AwsHttpHeaders.COPY_SOURCE_IF_UNMODIFIED_SINCE, AwsHttpHeaders.COPY_SOURCE_RANGE, AwsHttpHeaders.DATE,
            AwsHttpHeaders.DECODED_CONTENT_LENGTH, AwsHttpHeaders.METADATA_DIRECTIVE, AwsHttpHeaders.STORAGE_CLASS,
            // SDK v2 checksum / integrity headers — accept but do not verify (we rely on ECPDS checksums)
            AwsHttpHeaders.CHECKSUM_ALGORITHM, AwsHttpHeaders.CHECKSUM_CRC32, AwsHttpHeaders.CHECKSUM_CRC32C,
            AwsHttpHeaders.CHECKSUM_SHA1, AwsHttpHeaders.CHECKSUM_SHA256, AwsHttpHeaders.TRAILER, AwsHttpHeaders.TE,
            AwsHttpHeaders.EXPECTED_BUCKET_OWNER);

    /** The Constant XML_CONTENT_TYPE. */
    private static final String XML_CONTENT_TYPE = "application/xml";

    /** The Constant UTF_8. */
    private static final String UTF_8 = "UTF-8";
    /** URLEncoder escapes / which we do not want. */
    private static final Escaper urlEscaper = new PercentEscaper("*-./_", /* plusForSpace= */ false);

    /**
     * In-progress multipart upload state. Key = uploadId. Parts are stored as local temp files so that the final
     * CompleteMultipartUpload can stream them in order to ECPDS in a single getProxySocketOutput call.
     */
    private static final ConcurrentHashMap<String, MultipartUpload> _multipartUploads = new ConcurrentHashMap<>();

    /** Tracks a single in-progress multipart upload. */
    private static final class MultipartUpload {
        final String containerName;
        final String blobName;
        final Date initiated;
        final Path tempDir;
        /** partNumber (1-based) → {tempFile, etag(MD5 hex)} */
        final ConcurrentHashMap<Integer, PartInfo> parts = new ConcurrentHashMap<>();

        MultipartUpload(final String containerName, final String blobName, final Path tempDir) {
            this.containerName = containerName;
            this.blobName = blobName;
            this.initiated = new Date();
            this.tempDir = tempDir;
        }
    }

    /** Metadata for a single uploaded part. */
    private static final class PartInfo {
        final Path file;
        final String etag;
        final long size;
        final Date lastModified;

        PartInfo(final Path file, final String etag, final long size) {
            this.file = file;
            this.etag = etag;
            this.size = size;
            this.lastModified = new Date();
        }
    }

    /** The authentication type. */
    private final AuthenticationType authenticationType;

    /** The v 4 max non chunked request size. */
    private final long v4MaxNonChunkedRequestSize;

    /** The ignore unknown headers. */
    private final boolean ignoreUnknownHeaders;

    /** The cors rules. */
    private final CrossOriginResourceSharing corsRules;

    /** The service path. */
    private final String servicePath;

    /** The maximum time skew. */
    private final int maximumTimeSkew;

    /** The xml output factory. */
    private final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    /** The blob store locator. */
    private final BlobStoreLocator blobStoreLocator;

    /**
     * Instantiates a new s 3 proxy handler.
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
    public S3ProxyHandler(final AuthenticationType authenticationType, final long v4MaxNonChunkedRequestSize,
            final boolean ignoreUnknownHeaders, final CrossOriginResourceSharing corsRules, final String servicePath,
            final int maximumTimeSkew) {
        this.corsRules = corsRules;
        this.blobStoreLocator = new BlobStoreLocator();
        this.authenticationType = authenticationType;
        this.v4MaxNonChunkedRequestSize = v4MaxNonChunkedRequestSize;
        this.ignoreUnknownHeaders = ignoreUnknownHeaders;
        xmlOutputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);
        this.servicePath = Strings.nullToEmpty(servicePath);
        this.maximumTimeSkew = maximumTimeSkew;
    }

    /**
     * Checks if is valid container.
     *
     * @param containerName
     *            the container name
     *
     * @return true, if is valid container
     */
    private static boolean isValidContainer(final String containerName) {
        return !(containerName == null || containerName.length() < 3 || containerName.length() > 255
                || containerName.startsWith(".") || containerName.endsWith(".") || validateIpAddress(containerName)
                || !VALID_BUCKET_FIRST_CHAR.matches(containerName.charAt(0))
                || !VALID_BUCKET.matchesAllOf(containerName));
    }

    /**
     * Do handle.
     *
     * @param baseRequest
     *            the base request
     * @param request
     *            the request
     * @param response
     *            the response
     * @param is
     *            the is
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws ecmwf.ecpds.mover.plugin.http.S3Exception
     *             the s 3 exception
     */
    public final void doHandle(final HttpServletRequest baseRequest, final HttpServletRequest request,
            final HttpServletResponse response, InputStream is) throws IOException, S3Exception {
        logger.debug("request: {}", request);

        final var method = request.getMethod();
        var uri = request.getRequestURI();
        final var originalUri = uri;

        if (!servicePath.isEmpty() && uri.length() >= servicePath.length()) {
            uri = uri.substring(servicePath.length());
        }
        if (uri.isEmpty()) {
            uri = "/";
        }

        // Determine early if this is an unsigned anonymous request so auth checks can be skipped.
        // The container name (= bucket = username) is extracted from the URI before any auth parsing.
        final var earlyPath = uri.split("/", 3);
        final var earlyContainer = earlyPath.length > 1 && !earlyPath[1].isEmpty()
                ? URLDecoder.decode(earlyPath[1], UTF_8) : "";
        final var anonymousS3 = !earlyContainer.isEmpty() && request.getHeader(HttpHeaders.AUTHORIZATION) == null
                && request.getParameter("X-Amz-Algorithm") == null && request.getParameter("AWSAccessKeyId") == null
                && blobStoreLocator.isAnonymousUser(earlyContainer);

        response.addHeader(AwsHttpHeaders.REQUEST_ID, FAKE_REQUEST_ID);

        var hasDateHeader = false;
        var hasXAmzDateHeader = false;
        for (final String headerName : Collections.list(request.getHeaderNames())) {
            if (logger.isDebugEnabled()) {
                for (final String headerValue : Collections.list(request.getHeaders(headerName))) {
                    logger.debug("header: {}: {}", headerName, Strings.nullToEmpty(headerValue));
                }
            }
            if (HttpHeaders.DATE.equalsIgnoreCase(headerName)) {
                hasDateHeader = true;
            } else if (AwsHttpHeaders.DATE.equalsIgnoreCase(headerName)
                    && !Strings.isNullOrEmpty(request.getHeader(AwsHttpHeaders.DATE))) {
                hasXAmzDateHeader = true;
            }
        }
        final var haveBothDateHeader = hasDateHeader && hasXAmzDateHeader;

        // when access information is not provided in request header,
        // treat it as anonymous, return all public accessible information
        if (!anonymousS3
                && ("GET".equals(method) || "HEAD".equals(method) || "POST".equals(method) || "OPTIONS".equals(method))
                && request.getHeader(HttpHeaders.AUTHORIZATION) == null &&
                // v2 or /v4
                request.getParameter("X-Amz-Algorithm") == null && // v4 query
                request.getParameter("AWSAccessKeyId") == null) // v2 query
        {
            throw new S3Exception(S3ErrorCode.ACCESS_DENIED, "AWS authentication required");
        }

        // should according the AWSAccessKeyId= Signature or auth header nil
        if (!anonymousS3 && !hasDateHeader && !hasXAmzDateHeader && request.getParameter("X-Amz-Date") == null
                && request.getParameter("Expires") == null) {
            throw new S3Exception(S3ErrorCode.ACCESS_DENIED,
                    "AWS authentication requires a valid Date or x-amz-date header");
        }

        String requestIdentity = null;
        var headerAuthorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        S3AuthorizationHeader authHeader = null;
        var presignedUrl = false;

        if (!anonymousS3) {
            if (headerAuthorization == null) {
                final var algorithm = request.getParameter("X-Amz-Algorithm");
                if (algorithm == null) { // v2 query
                    final var identity = request.getParameter("AWSAccessKeyId");
                    final var signature = request.getParameter("Signature");
                    if (identity == null || signature == null) {
                        throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
                    }
                    headerAuthorization = "AWS " + identity + ":" + signature;
                } else if ("AWS4-HMAC-SHA256".equals(algorithm)) { // v4 query
                    final var credential = request.getParameter("X-Amz-Credential");
                    final var signedHeaders = request.getParameter("X-Amz-SignedHeaders");
                    final var signature = request.getParameter("X-Amz-Signature");
                    if (credential == null || signedHeaders == null || signature == null) {
                        throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
                    }
                    headerAuthorization = "AWS4-HMAC-SHA256" + " Credential=" + credential + ", requestSignedHeaders="
                            + signedHeaders + ", Signature=" + signature;
                } else {
                    throw new IllegalArgumentException("unknown algorithm: " + algorithm);
                }
                presignedUrl = true;
            }

            try {
                authHeader = new S3AuthorizationHeader(headerAuthorization);
                // whether v2 or v4 (normal header and query)
            } catch (final IllegalArgumentException iae) {
                throw new S3Exception(S3ErrorCode.INVALID_ARGUMENT, iae);
            }
            requestIdentity = authHeader.identity;
        } else {
            requestIdentity = earlyContainer;
        }

        var dateSkew = 0L; // date for timeskew check

        if (!anonymousS3) {
            // v2 GET /s3proxy-1080747708/foo?AWSAccessKeyId=local-identity&Expires=
            // 1510322602&Signature=UTyfHY1b1Wgr5BFEn9dpPlWdtFE%3D)
            // have no date
            var haveDate = true;

            final AuthenticationType finalAuthType;
            if (authHeader.authenticationType == AuthenticationType.AWS_V2
                    && (authenticationType == AuthenticationType.AWS_V2
                            || authenticationType == AuthenticationType.AWS_V2_OR_V4)) {
                finalAuthType = AuthenticationType.AWS_V2;
            } else if (authHeader.authenticationType == AuthenticationType.AWS_V4
                    && (authenticationType == AuthenticationType.AWS_V4
                            || authenticationType == AuthenticationType.AWS_V2_OR_V4)) {
                finalAuthType = AuthenticationType.AWS_V4;
            } else {
                throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
            }

            if (hasXAmzDateHeader) { // format diff between v2 and v4
                if (finalAuthType == AuthenticationType.AWS_V2) {
                    dateSkew = request.getDateHeader(AwsHttpHeaders.DATE);
                    dateSkew /= 1000;
                    // case sensitive?
                } else if (finalAuthType == AuthenticationType.AWS_V4) {
                    dateSkew = parseIso8601(request.getHeader(AwsHttpHeaders.DATE));
                }
            } else if (request.getParameter("X-Amz-Date") != null) { // v4 query
                final var dateString = request.getParameter("X-Amz-Date");
                dateSkew = parseIso8601(dateString);
            } else if (hasDateHeader) {
                try {
                    dateSkew = request.getDateHeader(HttpHeaders.DATE);
                } catch (final IllegalArgumentException iae) {
                    throw new S3Exception(S3ErrorCode.ACCESS_DENIED, iae);
                }
                dateSkew /= 1000;
            } else {
                haveDate = false;
            }
            if (haveDate) {
                isTimeSkewed(dateSkew);
            }
        }

        final var path = uri.split("/", 3);
        for (var i = 0; i < path.length; i++) {
            path[i] = URLDecoder.decode(path[i], UTF_8);
        }

        final Map.Entry<String, BlobStore> provider;
        BlobStore blobStore = null;
        try {
            if (anonymousS3) {
                // Anonymous (unsigned) request — bucket name is the user identity;
                // MasterServer skips password verification for USER_PORTAL_ANONYMOUS users.
                provider = blobStoreLocator.locateAnonymousBlobStore(request, response, earlyContainer);
                if (provider == null) {
                    throw new S3Exception(S3ErrorCode.INVALID_ACCESS_KEY_ID);
                }
            } else {
                if (requestIdentity == null) {
                    throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
                }
                var expiresString = request.getParameter("Expires");
                if (expiresString != null) { // v2 query
                    final var expires = Long.parseLong(expiresString);
                    final var nowSeconds = System.currentTimeMillis() / 1000;
                    if (nowSeconds >= expires) {
                        throw new S3Exception(S3ErrorCode.ACCESS_DENIED, "Request has expired");
                    }
                    if (expires - nowSeconds > TimeUnit.DAYS.toSeconds(365)) {
                        throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
                    }
                }

                final var dateString = request.getParameter("X-Amz-Date");
                // from para v4 query
                expiresString = request.getParameter("X-Amz-Expires");
                if (dateString != null && expiresString != null) { // v4 query
                    final var date = parseIso8601(dateString);
                    final var expires = Long.parseLong(expiresString);
                    final var nowSeconds = System.currentTimeMillis() / 1000;
                    if (nowSeconds >= date + expires) {
                        throw new S3Exception(S3ErrorCode.ACCESS_DENIED, "Request has expired");
                    }
                    if (expires > TimeUnit.DAYS.toSeconds(7)) {
                        throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
                    }
                }

                // The aim ?
                switch (authHeader.authenticationType) {
                case AWS_V2:
                    switch (authenticationType) {
                    case AWS_V2, AWS_V2_OR_V4:
                        break;
                    default:
                        throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
                    }
                    break;
                case AWS_V4:
                    switch (authenticationType) {
                    case AWS_V4, AWS_V2_OR_V4:
                        break;
                    default:
                        throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled type: " + authHeader.authenticationType);
                }

                provider = blobStoreLocator.locateBlobStore(request, response, requestIdentity,
                        path.length > 1 ? path[1] : null, path.length > 2 ? path[2] : null);
                if (provider == null) {
                    throw new S3Exception(S3ErrorCode.INVALID_ACCESS_KEY_ID);
                }

                final String expectedSignature;
                if (authHeader.hmacAlgorithm == null) {
                    // V2. WWhen presigned url is generated, it doesn't consider the service path
                    final var uriForSigning = presignedUrl ? uri : originalUri;
                    expectedSignature = AwsSignature.createAuthorizationSignature(request, uriForSigning,
                            provider.getKey(), presignedUrl, haveBothDateHeader);
                } else {
                    final var contentSha256 = request.getHeader(AwsHttpHeaders.CONTENT_SHA256);
                    try {
                        final byte[] payload;
                        if (request.getParameter("X-Amz-Algorithm") != null) {
                            payload = new byte[0];
                        } else if ("STREAMING-AWS4-HMAC-SHA256-PAYLOAD".equals(contentSha256)) {
                            payload = new byte[0];
                            is = new ChunkedInputStream(is);
                        } else if ("UNSIGNED-PAYLOAD".equals(contentSha256)) {
                            payload = new byte[0];
                        } else {
                            // buffer the entire stream to calculate digest
                            // why input stream read contentlength of header?
                            payload = ByteStreams.toByteArray(ByteStreams.limit(is, v4MaxNonChunkedRequestSize + 1));
                            if (payload.length == v4MaxNonChunkedRequestSize + 1) {
                                throw new S3Exception(S3ErrorCode.MAX_MESSAGE_LENGTH_EXCEEDED);
                            }

                            // maybe we should check this when signing,
                            // a lot of dup code with aws sign code.
                            final var md = MessageDigest.getInstance(authHeader.hashAlgorithm);
                            final var hash = md.digest(payload);
                            if (!contentSha256.equals(BaseEncoding.base16().lowerCase().encode(hash))) {
                                throw new S3Exception(S3ErrorCode.X_AMZ_CONTENT_S_H_A_256_MISMATCH);
                            }
                            is = new ByteArrayInputStream(payload);
                        }

                        // When presigned url is generated, it doesn't consider the service path
                        final var uriForSigning = presignedUrl ? uri : originalUri;
                        // v4 signature
                        expectedSignature = AwsSignature.createAuthorizationSignatureV4(baseRequest, authHeader,
                                payload, uriForSigning, provider.getKey());
                    } catch (InvalidKeyException | NoSuchAlgorithmException | ConnectionException e) {
                        throw new S3Exception(S3ErrorCode.INVALID_ARGUMENT, e);
                    }
                }

                if (!constantTimeEquals(expectedSignature, authHeader.signature)) {
                    throw new S3Exception(S3ErrorCode.SIGNATURE_DOES_NOT_MATCH);
                }
            } // end authenticated block

            for (final String parameter : Collections.list(request.getParameterNames())) {
                if (UNSUPPORTED_PARAMETERS.contains(parameter)) {
                    logger.error("Unknown parameters {} with URI {}", parameter, request.getRequestURI());
                    throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
                }
            }

            // emit NotImplemented for unknown x-amz- headers
            for (final String headerName : Collections.list(request.getHeaderNames())) {
                if (ignoreUnknownHeaders || !headerName.startsWith("x-amz-")
                        || headerName.startsWith(USER_METADATA_PREFIX)) {
                    continue;
                }
                if (!SUPPORTED_X_AMZ_HEADERS.contains(headerName.toLowerCase())) {
                    logger.error("Unknown header {} with URI {}", headerName, request.getRequestURI());
                    throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
                }
            }

            // Validate container name
            if (!"/".equals(uri) && !isValidContainer(path[1])) {
                if ("PUT".equals(method) && (path.length <= 2 || path[2].isEmpty())
                        && !"".equals(request.getParameter("acl"))) {
                    throw new S3Exception(S3ErrorCode.INVALID_BUCKET_NAME);
                }
                throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
            }

            // Lets get the BlobStore
            try {
                blobStore = provider.getValue();
            } catch (final RuntimeException e) {
                if (e.getCause() instanceof S3Exception s3e) {
                    throw s3e;
                }
                throw e;
            }
            if (blobStore == null) {
                throw new S3Exception(S3ErrorCode.INVALID_ACCESS_KEY_ID);
            }

            final var uploadId = request.getParameter("uploadId");
            switch (method) {
            case "DELETE":
                if (path.length <= 2 || path[2].isEmpty()) {
                    handleContainerDelete(response, blobStore, path[1]);
                } else if (uploadId != null) {
                    handleAbortMultipartUpload(request, response, blobStore, path[1], path[2], uploadId);
                } else {
                    handleBlobRemove(response, blobStore, path[1], path[2]);
                }
                return;
            case "GET":
                if ("/".equals(uri)) {
                    handleContainerList(response, blobStore);
                } else if (path.length <= 2 || path[2].isEmpty()) {
                    if ("".equals(request.getParameter("acl"))) {
                        handleGetContainerAcl(response, blobStore, path[1]);
                        return;
                    }
                    if ("".equals(request.getParameter("location"))) {
                        handleContainerLocation(response);
                        return;
                    } else if ("".equals(request.getParameter("policy"))) {
                        handleBucketPolicy(blobStore, path[1]);
                        return;
                    } else if ("".equals(request.getParameter("uploads"))) {
                        handleListMultipartUploads(request, response, blobStore, path[1]);
                        return;
                    } else if ("".equals(request.getParameter("versioning"))) {
                        handleGetBucketVersioning(response);
                        return;
                    }
                    handleBlobList(request, response, blobStore, path[1]);
                } else {
                    if ("".equals(request.getParameter("acl"))) {
                        handleGetBlobAcl(response, blobStore, path[1], path[2]);
                        return;
                    }
                    if (uploadId != null) {
                        handleListParts(request, response, blobStore, path[1], path[2], uploadId);
                        return;
                    }
                    handleGetBlob(request, response, blobStore, path[1], path[2]);
                }
                return;
            case "HEAD":
                if (path.length <= 2 || path[2].isEmpty()) {
                    handleContainerExists(blobStore, path[1]);
                } else {
                    handleBlobMetadata(request, response, blobStore, path[1], path[2]);
                }
                return;
            case "POST":
                if ("".equals(request.getParameter("delete"))) {
                    handleMultiBlobRemove(response, is, blobStore, path[1]);
                    return;
                }
                if ("".equals(request.getParameter("uploads"))) {
                    handleInitiateMultipartUpload(request, response, blobStore, path[1], path[2]);
                    return;
                } else if (uploadId != null && request.getParameter("partNumber") == null) {
                    handleCompleteMultipartUpload(request, response, is, blobStore, path[1], path[2], uploadId);
                    return;
                }
                break;
            case "PUT":
                if (path.length <= 2 || path[2].isEmpty()) {
                    if ("".equals(request.getParameter("acl"))) {
                        handleSetContainerAcl(request, response, is, blobStore, path[1]);
                        return;
                    }
                    handleContainerCreate(request, response, is, blobStore, path[1]);
                } else if (uploadId != null) {
                    if (request.getHeader(AwsHttpHeaders.COPY_SOURCE) != null) {
                        handleCopyPart(request, response, blobStore, path[1], path[2], uploadId);
                    } else {
                        handleUploadPart(request, response, is, blobStore, path[1], path[2], uploadId);
                    }
                } else if (request.getHeader(AwsHttpHeaders.COPY_SOURCE) != null) {
                    handleCopyBlob(request, response, is, blobStore, path[1], path[2]);
                } else {
                    if ("".equals(request.getParameter("acl"))) {
                        handleSetBlobAcl(request, response, is, blobStore, path[1], path[2]);
                        return;
                    }
                    handlePutBlob(request, response, is, blobStore, path[1], path[2]);
                }
                return;
            case "OPTIONS":
                handleOptionsBlob(request, response, blobStore, path[1]);
                return;
            default:
                break;
            }
            logger.error("Unknown method {} with URI {}", method, request.getRequestURI());
            throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
        } finally {
            if (blobStore != null) {
                blobStore.close();
            }
        }
    }

    /**
     * Handle get container acl.
     *
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleGetContainerAcl(final HttpServletResponse response, final BlobStore blobStore,
            final String containerName) throws IOException, S3Exception {
        if (!blobStore.containerExists(containerName)) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }

        final var access = blobStore.getContainerAccess(containerName);

        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("AccessControlPolicy");
            xml.writeDefaultNamespace(AWS_XMLNS);

            writeOwnerStanza(xml);

            xml.writeStartElement("AccessControlList");

            xml.writeStartElement("Grant");

            xml.writeStartElement("Grantee");
            xml.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xml.writeAttribute("xsi:type", "CanonicalUser");

            writeSimpleElement(xml, "ID", FAKE_OWNER_ID);
            writeSimpleElement(xml, "DisplayName", FAKE_OWNER_DISPLAY_NAME);

            xml.writeEndElement();

            writeSimpleElement(xml, "Permission", "FULL_CONTROL");

            xml.writeEndElement();

            if (access == ContainerAccess.PUBLIC_READ) {
                xml.writeStartElement("Grant");

                xml.writeStartElement("Grantee");
                xml.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                xml.writeAttribute("xsi:type", "Group");

                writeSimpleElement(xml, "URI", "http://acs.amazonaws.com/groups/global/AllUsers");

                xml.writeEndElement();

                writeSimpleElement(xml, "Permission", "READ");

                xml.writeEndElement();
            }

            xml.writeEndElement();

            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle set container acl.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param is
     *            the is
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws S3Exception
     *             the s 3 exception
     */
    private static void handleSetContainerAcl(final HttpServletRequest request, final HttpServletResponse response,
            final InputStream is, final BlobStore blobStore, final String containerName)
            throws IOException, S3Exception {
        throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Handle get blob acl.
     *
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void handleGetBlobAcl(final HttpServletResponse response, final BlobStore blobStore,
            final String containerName, final String blobName) throws IOException {
        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("AccessControlPolicy");
            xml.writeDefaultNamespace(AWS_XMLNS);

            writeOwnerStanza(xml);

            xml.writeStartElement("AccessControlList");

            xml.writeStartElement("Grant");

            xml.writeStartElement("Grantee");
            xml.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xml.writeAttribute("xsi:type", "CanonicalUser");

            writeSimpleElement(xml, "ID", FAKE_OWNER_ID);
            writeSimpleElement(xml, "DisplayName", FAKE_OWNER_DISPLAY_NAME);

            xml.writeEndElement();

            writeSimpleElement(xml, "Permission", "FULL_CONTROL");

            xml.writeEndElement();

            xml.writeEndElement();

            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle set blob acl.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param is
     *            the is
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private static void handleSetBlobAcl(final HttpServletRequest request, final HttpServletResponse response,
            final InputStream is, final BlobStore blobStore, final String containerName, final String blobName)
            throws S3Exception {
        throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Handle container list.
     *
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     *
     * @throws S3Exception
     *             the s 3 exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void handleContainerList(final HttpServletResponse response, final BlobStore blobStore)
            throws S3Exception, IOException {
        final var buckets = blobStore.list();

        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("ListAllMyBucketsResult");
            xml.writeDefaultNamespace(AWS_XMLNS);

            writeOwnerStanza(xml);

            xml.writeStartElement("Buckets");
            for (final StorageMetadata metadata : buckets) {
                xml.writeStartElement("Bucket");

                final var name = metadata.getName();
                writeSimpleElement(xml, "Name", name);

                var creationDate = metadata.getCreationDate();
                if (creationDate == null) {
                    // Some providers, e.g., Swift, do not provide container
                    // creation date. Emit a bogus one to satisfy clients like
                    // s3cmd which require one.
                    creationDate = new Date(0);
                }
                writeSimpleElement(xml, "CreationDate", formatDate(creationDate));

                xml.writeEndElement();
            }
            xml.writeEndElement();

            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle container location.
     *
     * @param response
     *            the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void handleContainerLocation(final HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("LocationConstraint");
            xml.writeDefaultNamespace(AWS_XMLNS);
            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle bucket policy.
     *
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private static void handleBucketPolicy(final BlobStore blobStore, final String containerName) throws S3Exception {
        if (!blobStore.containerExists(containerName)) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }
        throw new S3Exception(S3ErrorCode.NO_SUCH_POLICY);
    }

    /**
     * Handle list multipart uploads.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param container
     *            the container
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleListMultipartUploads(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String container) throws IOException, S3Exception {
        if (!blobStore.containerExists(container)) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }
        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("ListMultipartUploadsResult");
            xml.writeDefaultNamespace(AWS_XMLNS);
            writeSimpleElement(xml, "Bucket", container);
            writeSimpleElement(xml, "MaxUploads", "1000");
            writeSimpleElement(xml, "IsTruncated", "false");
            for (final var entry : _multipartUploads.entrySet()) {
                final var mpu = entry.getValue();
                if (!container.equals(mpu.containerName)) {
                    continue;
                }
                xml.writeStartElement("Upload");
                writeSimpleElement(xml, "Key", mpu.blobName);
                writeSimpleElement(xml, "UploadId", entry.getKey());
                writeSimpleElement(xml, "Initiated", formatDate(mpu.initiated));
                xml.writeStartElement("Initiator");
                writeSimpleElement(xml, "ID", FAKE_OWNER_ID);
                writeSimpleElement(xml, "DisplayName", FAKE_OWNER_DISPLAY_NAME);
                xml.writeEndElement();
                writeOwnerStanza(xml);
                writeSimpleElement(xml, "StorageClass", "STANDARD");
                xml.writeEndElement();
            }
            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle get bucket versioning.
     *
     * @param response
     *            the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void handleGetBucketVersioning(final HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("VersioningConfiguration");
            writeSimpleElement(xml, "Status", "Disabled");
            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle container exists.
     *
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private static void handleContainerExists(final BlobStore blobStore, final String containerName)
            throws S3Exception {
        if (!blobStore.containerExists(containerName)) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }
    }

    /**
     * Handle container create.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param is
     *            the is
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private static void handleContainerCreate(final HttpServletRequest request, final HttpServletResponse response,
            final InputStream is, final BlobStore blobStore, final String containerName) throws S3Exception {
        throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Handle container delete.
     *
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private static void handleContainerDelete(final HttpServletResponse response, final BlobStore blobStore,
            final String containerName) throws S3Exception {
        throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Handle blob list.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleBlobList(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String containerName) throws IOException, S3Exception {
        final var options = new ListContainerOptions();
        final var encodingType = request.getParameter("encoding-type");
        final var delimiter = request.getParameter("delimiter");
        if (delimiter != null) {
            options.delimiter(delimiter);
        } else {
            options.recursive();
        }
        final var prefix = request.getParameter("prefix");
        if (prefix != null && !prefix.isEmpty()) {
            options.prefix(prefix);
        }

        var isListV2 = false;
        final String marker;
        final var listType = request.getParameter("list-type");
        final var continuationToken = request.getParameter("continuation-token");
        final var startAfter = request.getParameter("start-after");
        if (listType == null) {
            marker = request.getParameter("marker");
        } else if ("2".equals(listType)) {
            isListV2 = true;
            if (continuationToken != null && startAfter != null) {
                throw new S3Exception(S3ErrorCode.INVALID_ARGUMENT);
            }
            if (continuationToken != null) {
                marker = continuationToken;
            } else {
                marker = startAfter;
            }
        } else {
            throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
        }

        final var fetchOwner = !isListV2 || "true".equals(request.getParameter("fetch-owner"));

        var maxKeys = 1000;
        final var maxKeysString = request.getParameter("max-keys");
        if (maxKeysString != null) {
            try {
                maxKeys = Integer.parseInt(maxKeysString);
            } catch (final NumberFormatException nfe) {
                throw new S3Exception(S3ErrorCode.INVALID_ARGUMENT, nfe);
            }
            if (maxKeys > 1000) {
                maxKeys = 1000;
            }
        }
        options.maxResults(maxKeys);

        final var set = blobStore.list(containerName, options, options.getPrefix(), options.getMaxKeys());

        addCorsResponseHeader(request, response);

        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("ListBucketResult");
            xml.writeDefaultNamespace(AWS_XMLNS);

            writeSimpleElement(xml, "Name", containerName);

            if (prefix == null) {
                xml.writeEmptyElement("Prefix");
            } else {
                writeSimpleElement(xml, "Prefix", encodeBlob(encodingType, prefix));
            }

            if (isListV2) {
                writeSimpleElement(xml, "KeyCount", String.valueOf(set.size()));
            }
            writeSimpleElement(xml, "MaxKeys", String.valueOf(maxKeys));

            if (!isListV2) {
                if (marker == null) {
                    xml.writeEmptyElement("Marker");
                } else {
                    writeSimpleElement(xml, "Marker", encodeBlob(encodingType, marker));
                }
            } else {
                if (continuationToken == null) {
                    xml.writeEmptyElement("ContinuationToken");
                } else {
                    writeSimpleElement(xml, "ContinuationToken", encodeBlob(encodingType, continuationToken));
                }
                if (startAfter == null) {
                    xml.writeEmptyElement("StartAfter");
                } else {
                    writeSimpleElement(xml, "StartAfter", encodeBlob(encodingType, startAfter));
                }
            }

            if (delimiter != null && !delimiter.isEmpty()) {
                writeSimpleElement(xml, "Delimiter", encodeBlob(encodingType, delimiter));
            }

            if (encodingType != null && "url".equals(encodingType)) {
                writeSimpleElement(xml, "EncodingType", encodingType);
            }

            final Set<String> commonPrefixes = new TreeSet<>();
            StorageMetadata lastMetadata = null;
            for (final StorageMetadata metadata : set) {
                lastMetadata = metadata;
                switch (metadata.getType()) {
                case FOLDER, RELATIVE_PATH:
                    final var name = metadata.getName();
                    commonPrefixes.add(name);
                    continue;
                default:
                    break;
                }

                xml.writeStartElement("Contents");

                writeSimpleElement(xml, "Key", encodeBlob(encodingType, metadata.getName()));

                final var lastModified = metadata.getLastModified();
                if (lastModified != null) {
                    writeSimpleElement(xml, "LastModified", formatDate(lastModified));
                }

                final var eTag = metadata.getETag();
                if (eTag != null) {
                    writeSimpleElement(xml, "ETag", maybeQuoteETag(eTag));
                }

                writeSimpleElement(xml, "Size", metadata.getSize());
                writeSimpleElement(xml, "StorageClass", metadata.getTier());

                if (fetchOwner) {
                    writeOwnerStanza(xml);
                }

                xml.writeEndElement();
            }

            writeSimpleElement(xml, "IsTruncated",
                    lastMetadata != null && lastMetadata.isTruncated() ? "true" : "false");

            for (final String commonPrefix : commonPrefixes) {
                xml.writeStartElement("CommonPrefixes");

                writeSimpleElement(xml, "Prefix", encodeBlob(encodingType, commonPrefix));

                xml.writeEndElement();
            }

            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle blob remove.
     *
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private static void handleBlobRemove(final HttpServletResponse response, final BlobStore blobStore,
            final String containerName, final String blobName) throws S3Exception {
        throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Handle multi blob remove.
     *
     * @param response
     *            the response
     * @param is
     *            the is
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleMultiBlobRemove(final HttpServletResponse response, final InputStream is,
            final BlobStore blobStore, final String containerName) throws IOException, S3Exception {
        final var dmor = new ObjectMapper().readValue(is, DeleteMultipleObjectsRequest.class);
        if (dmor.objects == null) {
            throw new S3Exception(S3ErrorCode.MALFORMED_X_M_L);
        }

        final Collection<String> blobNames = new ArrayList<>();
        for (final DeleteMultipleObjectsRequest.S3Object s3Object : dmor.objects) {
            blobNames.add(s3Object.key);
        }

        blobStore.removeBlobs(containerName, blobNames);

        response.setCharacterEncoding(UTF_8);
        try (Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("DeleteResult");
            xml.writeDefaultNamespace(AWS_XMLNS);

            if (!dmor.quiet) {
                for (final String blobName : blobNames) {
                    xml.writeStartElement("Deleted");

                    writeSimpleElement(xml, "Key", blobName);

                    xml.writeEndElement();
                }
            }

            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle blob metadata.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private static void handleBlobMetadata(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String containerName, final String blobName) throws S3Exception {
        final var metadata = blobStore.blobMetadata(containerName, blobName);
        if (metadata == null) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_KEY);
        }

        // BlobStore.blobMetadata does not support GetOptions so we emulate
        // conditional requests.
        final var ifMatch = request.getHeader(HttpHeaders.IF_MATCH);
        final var ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        final var ifModifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        final var ifUnmodifiedSince = request.getDateHeader(HttpHeaders.IF_UNMODIFIED_SINCE);

        var eTag = metadata.getETag();
        if (eTag != null) {
            eTag = maybeQuoteETag(eTag);
            if (ifMatch != null && !ifMatch.equals(eTag)) {
                throw new S3Exception(S3ErrorCode.PRECONDITION_FAILED);
            }
            if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        final var lastModified = metadata.getLastModified();
        if (lastModified != null) {
            if (ifModifiedSince != -1 && lastModified.compareTo(new Date(ifModifiedSince)) <= 0) {
                throw new S3Exception(S3ErrorCode.PRECONDITION_FAILED);
            }
            if (ifUnmodifiedSince != -1 && lastModified.compareTo(new Date(ifUnmodifiedSince)) >= 0) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        addMetadataToResponse(request, response, metadata);
    }

    /**
     * Handle options blob.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleOptionsBlob(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String containerName) throws S3Exception {
        if (!blobStore.containerExists(containerName)) {
            // Don't leak internal information, although authenticated
            throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
        }

        final var corsOrigin = request.getHeader(HttpHeaders.ORIGIN);
        if (Strings.isNullOrEmpty(corsOrigin)) {
            throw new S3Exception(S3ErrorCode.INVALID_CORS_ORIGIN);
        }
        if (!corsRules.isOriginAllowed(corsOrigin)) {
            throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
        }

        final var corsMethod = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        if (!corsRules.isMethodAllowed(corsMethod)) {
            throw new S3Exception(S3ErrorCode.INVALID_CORS_METHOD);
        }

        final var corsHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        if (!Strings.isNullOrEmpty(corsHeaders)) {
            if (!corsRules.isEveryHeaderAllowed(corsHeaders)) {
                throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
            }
            response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, corsHeaders);
        }

        response.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, corsRules.getAllowedOrigin(corsOrigin));
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, corsRules.getAllowedMethods());

        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Handle get blob.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleGetBlob(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String containerName, final String blobName)
            throws IOException, S3Exception {
        var status = HttpServletResponse.SC_OK;
        final var options = new GetOptions();

        final var ifMatch = request.getHeader(HttpHeaders.IF_MATCH);
        if (ifMatch != null) {
            options.ifETagMatches(ifMatch);
        }

        final var ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch != null) {
            options.ifETagDoesntMatch(ifNoneMatch);
        }

        final var ifModifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        if (ifModifiedSince != -1) {
            options.ifModifiedSince(new Date(ifModifiedSince));
        }

        final var ifUnmodifiedSince = request.getDateHeader(HttpHeaders.IF_UNMODIFIED_SINCE);
        if (ifUnmodifiedSince != -1) {
            options.ifUnmodifiedSince(new Date(ifUnmodifiedSince));
        }

        var range = request.getHeader(HttpHeaders.RANGE);
        if (range != null && range.startsWith("bytes=") &&
        // ignore multiple ranges
                range.indexOf(',') == -1) {
            range = range.substring("bytes=".length());
            final var ranges = range.split("-", 2);
            if (ranges[0].isEmpty()) {
                options.tail(Long.parseLong(ranges[1]));
            } else if (ranges[1].isEmpty()) {
                options.startAt(Long.parseLong(ranges[0]));
            } else {
                options.range(Long.parseLong(ranges[0]), Long.parseLong(ranges[1]));
            }
            status = HttpServletResponse.SC_PARTIAL_CONTENT;
        }

        final var blob = blobStore.getBlob(containerName, blobName, options);
        if (blob == null) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_KEY);
        }

        addCorsResponseHeader(request, response);
        addMetadataToResponse(request, response, blob.getMetadata());

        // Do we need to add the content range header?
        if (status == HttpServletResponse.SC_PARTIAL_CONTENT) {
            response.addHeader(HttpHeaders.CONTENT_RANGE, blob.getContentRange());
            response.addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(blob.getContentLength()));
            response.addHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        }

        response.setStatus(status);

        final var in = blob.openStream();
        try {
            StreamPlugThread.copy(response.getOutputStream(), in, StreamPlugThread.DEFAULT_BUFF_SIZE);
        } finally {
            StreamPlugThread.closeQuietly(in);
        }
    }

    /**
     * Handle copy blob.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param is
     *            the is
     * @param blobStore
     *            the blob store
     * @param destContainerName
     *            the dest container name
     * @param destBlobName
     *            the dest blob name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleCopyBlob(final HttpServletRequest request, final HttpServletResponse response,
            final InputStream is, final BlobStore blobStore, final String destContainerName, final String destBlobName)
            throws IOException, S3Exception {
        // x-amz-copy-source is /srcBucket/srcKey or srcBucket/srcKey
        var copySource = request.getHeader(AwsHttpHeaders.COPY_SOURCE);
        if (copySource == null) {
            throw new S3Exception(S3ErrorCode.INVALID_ARGUMENT);
        }
        copySource = URLDecoder.decode(copySource, UTF_8);
        if (copySource.startsWith("/")) {
            copySource = copySource.substring(1);
        }
        final var slash = copySource.indexOf('/');
        if (slash < 0) {
            throw new S3Exception(S3ErrorCode.INVALID_ARGUMENT);
        }
        final var srcContainer = copySource.substring(0, slash);
        final var srcBlob = copySource.substring(slash + 1);

        if (!blobStore.containerExists(srcContainer)) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }
        if (!blobStore.containerExists(destContainerName)) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }

        // Open source and stream directly into the destination
        final var srcBlob_ = blobStore.getBlob(srcContainer, srcBlob, new GetOptions());
        if (srcBlob_ == null) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_KEY);
        }
        final var in = srcBlob_.openStream();
        try {
            final var eTag = blobStore.putBlob(destContainerName, destBlobName, in);
            addCorsResponseHeader(request, response);
            response.setCharacterEncoding(UTF_8);
            try (final Writer writer = response.getWriter()) {
                response.setContentType(XML_CONTENT_TYPE);
                final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
                xml.writeStartDocument();
                xml.writeStartElement("CopyObjectResult");
                xml.writeDefaultNamespace(AWS_XMLNS);
                writeSimpleElement(xml, "LastModified", formatDate(new Date()));
                if (eTag != null) {
                    writeSimpleElement(xml, "ETag", maybeQuoteETag(eTag));
                }
                xml.writeEndElement();
                xml.flush();
            } catch (final XMLStreamException xse) {
                throw new IOException(xse);
            }
        } finally {
            StreamPlugThread.closeQuietly(in);
        }
    }

    /**
     * Handle put blob.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param is
     *            the is
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handlePutBlob(final HttpServletRequest request, final HttpServletResponse response,
            final InputStream is, final BlobStore blobStore, final String containerName, final String blobName)
            throws IOException, S3Exception {
        if (!blobStore.containerExists(containerName)) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }
        // For streaming uploads (STREAMING-AWS4-HMAC-SHA256-PAYLOAD), SDK v2 sets
        // Content-Length to the chunked-envelope size and puts the real object size in
        // x-amz-decoded-content-length. Pass it through so ECPDS can size correctly.
        final var decodedLength = request.getHeader(AwsHttpHeaders.DECODED_CONTENT_LENGTH);
        if (decodedLength != null) {
            response.setHeader(AwsHttpHeaders.DECODED_CONTENT_LENGTH, decodedLength);
        }
        final var eTag = blobStore.putBlob(containerName, blobName, is);
        addCorsResponseHeader(request, response);
        if (eTag != null) {
            response.addHeader(HttpHeaders.ETAG, maybeQuoteETag(eTag));
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Handle initiate multipart upload.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleInitiateMultipartUpload(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String containerName, final String blobName)
            throws IOException, S3Exception {
        if (!blobStore.containerExists(containerName)) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }
        final var uploadId = UUID.randomUUID().toString();
        final var tempDir = Files.createTempDirectory("s3mpu-" + uploadId);
        _multipartUploads.put(uploadId, new MultipartUpload(containerName, blobName, tempDir));
        logger.debug("InitiateMultipartUpload: {}/{} uploadId={}", containerName, blobName, uploadId);
        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("InitiateMultipartUploadResult");
            xml.writeDefaultNamespace(AWS_XMLNS);
            writeSimpleElement(xml, "Bucket", containerName);
            writeSimpleElement(xml, "Key", blobName);
            writeSimpleElement(xml, "UploadId", uploadId);
            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle complete multipart upload.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param is
     *            the is
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     * @param uploadId
     *            the upload id
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleCompleteMultipartUpload(final HttpServletRequest request, final HttpServletResponse response,
            final InputStream is, final BlobStore blobStore, final String containerName, final String blobName,
            final String uploadId) throws IOException, S3Exception {
        final var mpu = _multipartUploads.get(uploadId);
        if (mpu == null) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_UPLOAD);
        }
        // Parse the <CompleteMultipartUpload> body for ordered part list
        final List<PartEntry> parts = new ArrayList<>();
        try {
            final var dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            final var doc = dbf.newDocumentBuilder().parse(is);
            final var partNodes = doc.getElementsByTagName("Part");
            for (var i = 0; i < partNodes.getLength(); i++) {
                final var part = partNodes.item(i);
                var partNumber = 0;
                String etag = null;
                final var children = part.getChildNodes();
                for (var j = 0; j < children.getLength(); j++) {
                    final var child = children.item(j);
                    if ("PartNumber".equals(child.getNodeName())) {
                        partNumber = Integer.parseInt(child.getTextContent().trim());
                    } else if ("ETag".equals(child.getNodeName())) {
                        etag = child.getTextContent().trim();
                    }
                }
                if (partNumber > 0) {
                    parts.add(new PartEntry(partNumber, etag));
                }
            }
        } catch (final Exception e) {
            throw new S3Exception(S3ErrorCode.MALFORMED_X_M_L);
        }
        if (parts.isEmpty()) {
            throw new S3Exception(S3ErrorCode.MALFORMED_X_M_L);
        }
        parts.sort(Comparator.comparingInt((PartEntry p) -> p.partNumber));

        // Validate all parts are present
        for (final var p : parts) {
            if (!mpu.parts.containsKey(p.partNumber)) {
                throw new S3Exception(S3ErrorCode.INVALID_PART);
            }
        }

        // Stream all parts in order as a single SequenceInputStream into putBlob
        final var partStreams = new ArrayList<InputStream>();
        try {
            for (final var p : parts) {
                partStreams.add(Files.newInputStream(mpu.parts.get(p.partNumber).file));
            }
            final InputStream combined = new java.io.SequenceInputStream(Collections.enumeration(partStreams));
            final var eTag = blobStore.putBlob(containerName, blobName, combined);
            _multipartUploads.remove(uploadId);
            _deleteMultipartTempDir(mpu);
            logger.debug("CompleteMultipartUpload: {}/{} uploadId={} eTag={}", containerName, blobName, uploadId, eTag);
            addCorsResponseHeader(request, response);
            response.setCharacterEncoding(UTF_8);
            try (final Writer writer = response.getWriter()) {
                response.setContentType(XML_CONTENT_TYPE);
                final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
                xml.writeStartDocument();
                xml.writeStartElement("CompleteMultipartUploadResult");
                xml.writeDefaultNamespace(AWS_XMLNS);
                writeSimpleElement(xml, "Location", "/" + containerName + "/" + blobName);
                writeSimpleElement(xml, "Bucket", containerName);
                writeSimpleElement(xml, "Key", blobName);
                if (eTag != null) {
                    writeSimpleElement(xml, "ETag", maybeQuoteETag(eTag));
                }
                xml.writeEndElement();
                xml.flush();
            } catch (final XMLStreamException xse) {
                throw new IOException(xse);
            }
        } finally {
            for (final var s : partStreams) {
                StreamPlugThread.closeQuietly(s);
            }
        }
    }

    /**
     * Handle abort multipart upload.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     * @param uploadId
     *            the upload id
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleAbortMultipartUpload(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String containerName, final String blobName, final String uploadId)
            throws S3Exception {
        final var mpu = _multipartUploads.remove(uploadId);
        if (mpu == null) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_UPLOAD);
        }
        _deleteMultipartTempDir(mpu);
        logger.debug("AbortMultipartUpload: {}/{} uploadId={}", containerName, blobName, uploadId);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Handle list parts.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     * @param uploadId
     *            the upload id
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleListParts(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String containerName, final String blobName, final String uploadId)
            throws IOException, S3Exception {
        final var mpu = _multipartUploads.get(uploadId);
        if (mpu == null) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_UPLOAD);
        }
        final var maxParts = 1000;
        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("ListPartsResult");
            xml.writeDefaultNamespace(AWS_XMLNS);
            writeSimpleElement(xml, "Bucket", containerName);
            writeSimpleElement(xml, "Key", blobName);
            writeSimpleElement(xml, "UploadId", uploadId);
            writeSimpleElement(xml, "MaxParts", String.valueOf(maxParts));
            writeSimpleElement(xml, "IsTruncated", "false");
            writeOwnerStanza(xml);
            final var sorted = new ArrayList<>(mpu.parts.entrySet());
            sorted.sort(Comparator.comparingInt(Map.Entry::getKey));
            for (final var entry : sorted) {
                final var info = entry.getValue();
                xml.writeStartElement("Part");
                writeSimpleElement(xml, "PartNumber", String.valueOf(entry.getKey()));
                writeSimpleElement(xml, "LastModified", formatDate(info.lastModified));
                writeSimpleElement(xml, "ETag", maybeQuoteETag(info.etag));
                writeSimpleElement(xml, "Size", String.valueOf(info.size));
                xml.writeEndElement();
            }
            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Handle copy part.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     * @param uploadId
     *            the upload id
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleCopyPart(final HttpServletRequest request, final HttpServletResponse response,
            final BlobStore blobStore, final String containerName, final String blobName, final String uploadId)
            throws S3Exception {
        throw new S3Exception(S3ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Handle upload part.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param is
     *            the is
     * @param blobStore
     *            the blob store
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     * @param uploadId
     *            the upload id
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void handleUploadPart(final HttpServletRequest request, final HttpServletResponse response,
            final InputStream is, final BlobStore blobStore, final String containerName, final String blobName,
            final String uploadId) throws IOException, S3Exception {
        final var mpu = _multipartUploads.get(uploadId);
        if (mpu == null) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_UPLOAD);
        }
        final var partNumberStr = request.getParameter("partNumber");
        final int partNumber;
        try {
            partNumber = Integer.parseInt(partNumberStr);
        } catch (final NumberFormatException e) {
            throw new S3Exception(S3ErrorCode.INVALID_ARGUMENT);
        }
        if (partNumber < 1 || partNumber > 10000) {
            throw new S3Exception(S3ErrorCode.INVALID_ARGUMENT);
        }
        // Write part to a temp file, computing MD5 for the part ETag
        final var partFile = mpu.tempDir.resolve(String.valueOf(partNumber));
        final MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        long size;
        try (final var dis = new DigestInputStream(is, md5); final var out = Files.newOutputStream(partFile)) {
            size = StreamPlugThread.copy(out, dis, StreamPlugThread.DEFAULT_BUFF_SIZE);
        }
        final var etag = BaseEncoding.base16().lowerCase().encode(md5.digest());
        mpu.parts.put(partNumber, new PartInfo(partFile, etag, size));
        logger.debug("UploadPart: {}/{} uploadId={} part={} size={} etag={}", containerName, blobName, uploadId,
                partNumber, size, etag);
        response.addHeader(HttpHeaders.ETAG, maybeQuoteETag(etag));
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Adds the metadata to response.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param metadata
     *            the metadata
     */
    private static void addMetadataToResponse(final HttpServletRequest request, final HttpServletResponse response,
            final BlobMetadata metadata) {
        final var contentMetadata = metadata.getContentMetadata();
        addResponseHeaderWithOverride(request, response, HttpHeaders.CACHE_CONTROL, "response-cache-control",
                contentMetadata.getCacheControl());
        addResponseHeaderWithOverride(request, response, HttpHeaders.CONTENT_ENCODING, "response-content-encoding",
                contentMetadata.getContentEncoding());
        addResponseHeaderWithOverride(request, response, HttpHeaders.CONTENT_LANGUAGE, "response-content-language",
                contentMetadata.getContentLanguage());
        addResponseHeaderWithOverride(request, response, HttpHeaders.CONTENT_DISPOSITION,
                "response-content-disposition", contentMetadata.getContentDisposition());
        response.addHeader(HttpHeaders.CONTENT_LENGTH, contentMetadata.getContentLength());
        final var overrideContentType = request.getParameter("response-content-type");
        response.setContentType(overrideContentType != null ? overrideContentType : contentMetadata.getContentType());
        final var eTag = metadata.getETag();
        if (eTag != null) {
            response.addHeader(HttpHeaders.ETAG, maybeQuoteETag(eTag));
        }
        final var overrideExpires = request.getParameter("response-expires");
        if (overrideExpires != null) {
            response.addHeader(HttpHeaders.EXPIRES, overrideExpires);
        } else {
            final var expires = contentMetadata.getExpires();
            if (expires != null) {
                response.addDateHeader(HttpHeaders.EXPIRES, expires.getTime());
            }
        }
        response.addDateHeader(HttpHeaders.LAST_MODIFIED, metadata.getLastModified().getTime());
        for (final Map.Entry<String, String> entry : metadata.getUserMetadata().entrySet()) {
            response.addHeader(USER_METADATA_PREFIX + entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds the response header with override.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param headerName
     *            the header name
     * @param overrideHeaderName
     *            the override header name
     * @param value
     *            the value
     */
    private static void addResponseHeaderWithOverride(final HttpServletRequest request,
            final HttpServletResponse response, final String headerName, final String overrideHeaderName,
            final String value) {
        var override = request.getParameter(overrideHeaderName);

        // NPE in if value is null
        override = override != null ? override : value;

        if (override != null) {
            response.addHeader(headerName, override);
        }
    }

    /**
     * Parse ISO 8601 timestamp into seconds since 1970.
     *
     * @param date
     *            the date
     *
     * @return the long
     */
    private static long parseIso8601(final String date) {
        final var formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return formatter.parse(date).getTime() / 1000;
        } catch (final ParseException pe) {
            throw new IllegalArgumentException(pe);
        }
    }

    /**
     * Checks if is time skewed.
     *
     * @param date
     *            the date
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private void isTimeSkewed(final long date) throws S3Exception {
        if (date < 0) {
            throw new S3Exception(S3ErrorCode.ACCESS_DENIED);
        }
        final var now = System.currentTimeMillis() / 1000;
        if (now + maximumTimeSkew < date || now - maximumTimeSkew > date) {
            logger.debug("time skewed {} {}", date, now);
            throw new S3Exception(S3ErrorCode.REQUEST_TIME_TOO_SKEWED);
        }
    }

    /**
     * Format date.
     *
     * Cannot call BlobStore.getContext().utils().date().iso8601DateFormatsince it has unwanted millisecond precision.
     *
     * @param date
     *            the date
     *
     * @return the string
     */
    private static String formatDate(final Date date) {
        final var formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }

    /**
     * Send simple error response.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param code
     *            the code
     * @param message
     *            the message
     * @param elements
     *            the elements
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    protected final void sendSimpleErrorResponse(final HttpServletRequest request, final HttpServletResponse response,
            final S3ErrorCode code, final String message, final Map<String, String> elements) throws IOException {
        logger.debug("sendSimpleErrorResponse: {} {}", code, elements);

        if (response.isCommitted()) {
            // Another handler already opened and closed the writer.
            return;
        }

        response.setStatus(code.getHttpStatusCode());

        if ("HEAD".equals(request.getMethod())) {
            // The HEAD method is identical to GET except that the server MUST
            // NOT return a message-body in the response.
            return;
        }

        response.setCharacterEncoding(UTF_8);
        try (final Writer writer = response.getWriter()) {
            response.setContentType(XML_CONTENT_TYPE);
            final var xml = xmlOutputFactory.createXMLStreamWriter(writer);
            xml.writeStartDocument();
            xml.writeStartElement("Error");

            writeSimpleElement(xml, "Code", code.getErrorCode());
            writeSimpleElement(xml, "Message", message);

            for (final Map.Entry<String, String> entry : elements.entrySet()) {
                writeSimpleElement(xml, entry.getKey(), entry.getValue());
            }

            writeSimpleElement(xml, "RequestId", FAKE_REQUEST_ID);

            xml.writeEndElement();
            xml.flush();
        } catch (final XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Adds the cors response header.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    private void addCorsResponseHeader(final HttpServletRequest request, final HttpServletResponse response) {
        final var corsOrigin = request.getHeader(HttpHeaders.ORIGIN);
        if (!Strings.isNullOrEmpty(corsOrigin) && corsRules.isOriginAllowed(corsOrigin)) {
            response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, corsRules.getAllowedOrigin(corsOrigin));
            response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, corsRules.getAllowedMethods());
        }
    }

    /**
     * Write owner stanza.
     *
     * @param xml
     *            the xml
     *
     * @throws XMLStreamException
     *             the XML stream exception
     */
    private static void writeOwnerStanza(final XMLStreamWriter xml) throws XMLStreamException {
        xml.writeStartElement("Owner");

        writeSimpleElement(xml, "ID", FAKE_OWNER_ID);
        writeSimpleElement(xml, "DisplayName", FAKE_OWNER_DISPLAY_NAME);

        xml.writeEndElement();
    }

    /**
     * Write simple element.
     *
     * @param xml
     *            the xml
     * @param elementName
     *            the element name
     * @param characters
     *            the characters
     *
     * @throws XMLStreamException
     *             the XML stream exception
     */
    private static void writeSimpleElement(final XMLStreamWriter xml, final String elementName, final String characters)
            throws XMLStreamException {
        xml.writeStartElement(elementName);
        xml.writeCharacters(characters);
        xml.writeEndElement();
    }

    /**
     * Maybe quote E tag.
     *
     * @param eTag
     *            the e tag
     *
     * @return the string
     */
    private static String maybeQuoteETag(final String eTag) {
        if (!eTag.startsWith("\"") && !eTag.endsWith("\"")) {
            return "\"" + eTag + "\"";
        }
        return eTag;
    }

    /**
     * Encode blob.
     *
     * Encode blob name if client requests it. This allows for characters which XML 1.0 cannot represent.
     *
     * @param encodingType
     *            the encoding type
     * @param blobName
     *            the blob name
     *
     * @return the string
     */
    private static String encodeBlob(final String encodingType, final String blobName) {
        if (encodingType != null && "url".equals(encodingType)) {
            return urlEscaper.escape(blobName);
        }
        return blobName;
    }

    /**
     * Validate ip address.
     *
     * @param string
     *            the string
     *
     * @return true, if successful
     */
    private static boolean validateIpAddress(final String string) {
        final var parts = Splitter.on('.').splitToList(string);
        if (parts.size() != 4) {
            return false;
        }
        for (final String part : parts) {
            try {
                final var num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (final NumberFormatException nfe) {
                return false;
            }
        }
        return true;
    }

    /**
     * Constant time equals.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     *
     * @return true, if successful
     */
    private static boolean constantTimeEquals(final String x, final String y) {
        return MessageDigest.isEqual(x.getBytes(StandardCharsets.UTF_8), y.getBytes(StandardCharsets.UTF_8));
    }

    /** Deletes all temp files and the temp directory for a multipart upload. */
    private static void _deleteMultipartTempDir(final MultipartUpload mpu) {
        try {
            for (final var info : mpu.parts.values()) {
                Files.deleteIfExists(info.file);
            }
            Files.deleteIfExists(mpu.tempDir);
        } catch (final IOException e) {
            logger.warn("Failed to delete multipart temp dir: {}", mpu.tempDir, e);
        }
    }

    /** Simple holder for a parsed part entry from a CompleteMultipartUpload body. */
    private static final class PartEntry {
        final int partNumber;
        final String etag;

        PartEntry(final int partNumber, final String etag) {
            this.partNumber = partNumber;
            this.etag = etag;
        }
    }
}
