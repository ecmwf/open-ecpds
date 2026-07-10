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

package ecmwf.common.ectrans.module;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SOCKET_STATISTICS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SO_MAX_PACING_RATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_CONGESTION_CONTROL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_LINGER_ENABLE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_LINGER_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_MAX_SEGMENT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_NO_DELAY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_QUICK_ACK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_TIME_STAMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_USER_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_WINDOW_CLAMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_ACCELERATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_ALLOW_EMPTY_BUCKET_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_BUCKET_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_CROSS_REGION_ACCESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_DISABLE_CHUNKED_ENCODING;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_DUALSTACK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_DURATION_SECONDS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_ENABLE_PATH_STYLE_ACCESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_EXTERNAL_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_FTPGROUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_FTPUSER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_LISTEN_ADDRESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_MK_BUCKET;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_MULTIPART_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_NUM_UPLOAD_THREADS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_PART_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_QUEUE_CAPACITY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_PREFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_PROTOCOL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_RECURSIVE_LEVEL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_REGION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_REQUEST_CHECKSUM_CALCULATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_RESPONSE_CHECKSUM_VALIDATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_ROLE_ARN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_ROLE_SESSION_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_SCHEME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_SINGLEPART_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_SSL_VALIDATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_STRICT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_URL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_USE_BYTE_ARRAY_INPUT_STREAM;
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.http.ExecutableHttpRequest;
import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpResponse;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.utils.IoUtils;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class AmazonS3Module.
 */
public final class AmazonS3Module extends TransferModule {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(AmazonS3Module.class);

    /** The status. */
    private String currentStatus = "INIT";

    /** The s3input. */
    private InputStream s3input;

    /** The prefix. */
    private String s3prefix = "";

    /** The s3. */
    private Session s3 = null;

    /** The setup. */
    private ECtransSetup currentSetup = null;

    /** The bucket name. */
    private String bucketName = null;

    /** The partSize in MB (minimum part size for S3 multipart is 5 MB except for the last part). */
    private int partSize = 10;

    /** The number of threads used to upload parts in parallel during multipart uploads. */
    private int numUploadThreads = 2;

    /**
     * Maximum number of part buffers that may be queued waiting for an upload thread. Bounds peak memory use to
     * approximately (numUploadThreads + queueCapacity) × partSize MB.
     */
    private int queueCapacity = 4;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Gets the status.
     *
     * @return the status
     */
    @Override
    public String getStatus() {
        return currentStatus;
    }

    /**
     * Gets the port.
     *
     * @param setup
     *            the setup
     *
     * @return the port
     */
    @Override
    public int getPort(final ECtransSetup setup) {
        return setup.getInteger(HOST_S3_PORT);
    }

    /**
     * Update socket statistics.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void updateSocketStatistics() throws IOException {
        if (s3 != null) {
            s3.updateStatistics();
        }
    }

    /**
     * Connect.
     *
     * @param location
     *            the location
     * @param setup
     *            the setup
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void connect(final String location, final ECtransSetup setup) throws IOException {
        // The location is: user:password@host/bucketName
        currentSetup = setup;
        setStatus("CONNECT");
        int pos;
        if ((pos = location.lastIndexOf("@")) == -1) {
            throw new IOException("Malformed URL ('@' not found)");
        }
        var host = location.substring(pos + 1);
        var user = location.substring(0, pos);
        if ((pos = user.indexOf(":")) == -1) {
            throw new IOException("Malformed URL (':' not found)");
        }
        final var password = user.substring(pos + 1);
        user = user.substring(0, pos);
        if ((pos = host.indexOf("/")) != -1) {
            bucketName = host.substring(pos + 1);
            host = host.substring(0, pos);
        }
        final var port = getPort(getSetup());
        final var scheme = setup.getString(HOST_S3_SCHEME);
        bucketName = setup.getString(HOST_S3_BUCKET_NAME);
        s3prefix = setup.getString(HOST_S3_PREFIX).trim();
        partSize = setup.getInteger(HOST_S3_PART_SIZE);
        numUploadThreads = Math.max(1, setup.getInteger(HOST_S3_NUM_UPLOAD_THREADS));
        queueCapacity = Math.max(1, setup.getInteger(HOST_S3_QUEUE_CAPACITY));
        if (isNotEmpty(s3prefix) && !s3prefix.endsWith("/")) {
            s3prefix += "/";
        }
        final var region = setup.getString(HOST_S3_REGION);
        // Resolve the S3 endpoint URL:
        // 1. Explicit s3.url in config — always used as-is.
        // 2. Destination host looks like a native AWS endpoint (*.amazonaws.com) — pass empty
        // so the SDK constructs the correct regional endpoint from s3.region alone.
        // 3. Destination host is an IP address (happens when ectrans.usednsname=no resolves the
        // hostname before passing it here) AND s3.region is explicitly set — also pass empty,
        // since an IP cannot be used as a virtual-hosted S3 endpoint and s3.region is authoritative.
        // 4. Any other hostname (MinIO, Ceph, custom S3-compatible service) — auto-use as override.
        final var resolvedHost = host; // capture final copy (host is reassigned above)
        final var defaultUrl = scheme + "://" + resolvedHost
                + (port != ("https".equals(scheme) ? 443 : 80) ? ":" + port : "");
        final boolean isAwsHost = resolvedHost.endsWith(".amazonaws.com") || resolvedHost.equals("amazonaws.com");
        final boolean isIpAddress = resolvedHost.matches("\\d{1,3}(\\.\\d{1,3}){3}") || resolvedHost.contains(":");
        final var url = setup.getOptionalString(HOST_S3_URL)
                .orElseGet(() -> isAwsHost || (isIpAddress && isNotEmpty(region)) ? "" : defaultUrl);
        _log.debug("AmazonS3 connection on {} ({})", isNotEmpty(url) ? url : "default-regional-endpoint", user);
        var connected = false;
        setAttribute("remote.hostName", host);
        final ClientSocketStatistics statistics;
        if (setup.getBoolean(HOST_ECTRANS_SOCKET_STATISTICS) && getAttribute("connectOptions") != null) {
            _log.debug("Activating Socket Statistics");
            statistics = new ClientSocketStatistics();
            setAttribute(statistics);
        } else {
            statistics = null;
        }
        // Build a SocketConfig to capture and apply all TCP options at connect time.
        // All options (NO_DELAY, QUICK_ACK, congestion, keepalive tuning, etc.) are applied
        // via TcpTunedSslSocketFactory.prepareSocket() once the TCP connection is established.
        final var socketConfig = new SocketConfig(statistics, "S3SocketConfig", getDebug());
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_NO_DELAY, socketConfig::setTcpNoDelay);
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE, socketConfig::setKeepAlive);
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_TIME_STAMP, socketConfig::setTCPTimeStamp);
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_QUICK_ACK, socketConfig::setTCPQuickAck);
        setup.setStringIfPresent(HOST_ECTRANS_TCP_CONGESTION_CONTROL, socketConfig::setTCPCongestion);
        setup.setByteSizeIfPresent(HOST_ECTRANS_SO_MAX_PACING_RATE, socketConfig::setSOMaxPacingRate);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_MAX_SEGMENT, socketConfig::setTCPMaxSegment);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_WINDOW_CLAMP, socketConfig::setTCPWindowClamp);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_TIME, socketConfig::setTCPKeepAliveTime);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL, socketConfig::setTCPKeepAliveInterval);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES, socketConfig::setTCPKeepAliveProbes);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_USER_TIMEOUT, socketConfig::setTCPUserTimeout);
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_LINGER_ENABLE, enable -> setup
                .setIntegerIfPresent(HOST_ECTRANS_TCP_LINGER_TIME, time -> socketConfig.setTCPLinger(enable, time)));
        try {
            s3 = Session.getSession(user, password, setup.getString(HOST_S3_LISTEN_ADDRESS), socketConfig,
                    setup.getString(HOST_S3_PROTOCOL), setup.getBoolean(HOST_S3_SSL_VALIDATION),
                    setup.getBoolean(HOST_S3_STRICT), setup.getBoolean(HOST_S3_ACCELERATION),
                    setup.getBoolean(HOST_S3_DUALSTACK), setup.getBoolean(HOST_S3_DISABLE_CHUNKED_ENCODING),
                    setup.getBoolean(HOST_S3_ENABLE_PATH_STYLE_ACCESS), setup.getBoolean(HOST_S3_CROSS_REGION_ACCESS),
                    bucketName, url, isNotEmpty(region) ? region : "", setup.getBoolean(HOST_S3_MK_BUCKET),
                    setup.getString(HOST_S3_ROLE_ARN), setup.getString(HOST_S3_ROLE_SESSION_NAME),
                    setup.getInteger(HOST_S3_DURATION_SECONDS), setup.getString(HOST_S3_EXTERNAL_ID),
                    setup.getString(HOST_S3_REQUEST_CHECKSUM_CALCULATION),
                    setup.getString(HOST_S3_RESPONSE_CHECKSUM_VALIDATION), getDebug());
            connected = true;
        } catch (final S3Exception e) {
            _log.error("Connection failed to {}", url, e);
            throw new IOException("Connection failed to " + url + ": " + formatS3Exception(e));
        } catch (final Throwable t) {
            _log.error("Connection failed to {}", url, t);
            throw new IOException("Connection failed to " + url + ": " + Format.getMessage(t, "", 0));
        } finally {
            if (!connected) {
                setStatus("ERROR");
            }
        }
    }

    /**
     * Gets the bucket name and key. Return {"bucketName", "key"}!
     *
     * @param name
     *            the name
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String[] getBucketNameAndKey(final String name) throws IOException {
        final var token = new StringTokenizer(name == null ? "" : name.replace('\\', '/'), "/");
        final var count = token.countTokens();
        final String[] result;
        if (isNotEmpty(bucketName)) {
            // The Bucket name is defined in the setup, not the path!
            if (count == 0) {
                throw new IOException("No Key specified (filename)");
            }
            final var fileName = token.nextToken("\0");
            result = new String[] { bucketName, s3prefix + fileName };
        } else if (getSetup().getBoolean(HOST_S3_ALLOW_EMPTY_BUCKET_NAME)) {
            result = new String[] { "", s3prefix + token.nextToken("\0") };
        } else {
            // The name should be in the format bucketName/filename.
            if (count == 0) {
                throw new IOException("No Bucket specified");
            }
            if (count == 1) {
                throw new IOException("No Key specified (filename)");
            } else {
                final var extractedBucketName = token.nextToken();
                final var fileName = token.nextToken("\0");
                result = new String[] { extractedBucketName, s3prefix + fileName };
            }
        }
        result[1] = processKey(result[1]);
        if (getDebug()) {
            _log.debug("BucketName: {}, Key: {}", result[0], result[1]);
        }
        return result;
    }

    /**
     * Process key. Make sure the key does not have a starting '/'.
     *
     * @param key
     *            the key
     *
     * @return the string
     */
    private static String processKey(final String key) {
        return key.startsWith("/") ? key.substring(1) : key;
    }

    /**
     * Del.
     *
     * @param name
     *            the name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void del(final String name) throws IOException {
        _log.debug("Del file {}", name);
        setStatus("DEL");
        final var bnk = getBucketNameAndKey(name);
        try {
            s3.getS3Client().deleteObject(DeleteObjectRequest.builder().bucket(bnk[0]).key(bnk[1]).build());
        } catch (final S3Exception e) {
            _log.debug("deleteObject", e);
            throw new IOException("Deleting object " + name + ": " + formatS3Exception(e));
        } catch (final Exception e) {
            _log.debug("deleteObject", e);
            throw new IOException("Deleting object " + name + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * Put.
     *
     * @param in
     *            the in
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public boolean put(final InputStream in, final String name, final long posn, final long size) throws IOException {
        _log.debug("Put file {} ({})", name, posn);
        setStatus("PUT");
        final var bnk = getBucketNameAndKey(name);
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        if (size < 0 || size >= getSetup().getByteSize(HOST_S3_MULTIPART_SIZE).size()) {
            // The size of the file is not known beforehand or we are instructed to perform a
            // multi-part transfer — use the streaming multipart upload.
            _log.debug("Using MultipartUploadOutputStream");
            final var mpu = new MultipartUploadOutputStream(bnk[0], bnk[1], partSize);
            var completed = false;
            try {
                StreamPlugThread.copy(mpu, in, StreamPlugThread.DEFAULT_BUFF_SIZE);
                mpu.close();
                _log.debug("Multi-part transfer completed");
                completed = true;
            } catch (final S3Exception e) {
                _log.debug("putObject S3Exception", e);
                throw new IOException("Pushing Object " + name + ": " + formatS3Exception(e));
            } catch (final Throwable t) {
                _log.debug("putObject unexpected error", t);
                throw new IOException("Pushing Object " + name + ": " + Format.getMessage(t, "", 0));
            } finally {
                if (!completed) {
                    mpu.abort();
                }
            }
        } else {
            // We know the file size so we can use a single-part PUT.
            try {
                final RequestBody requestBody;
                if (getSetup().getBoolean(HOST_S3_USE_BYTE_ARRAY_INPUT_STREAM)
                        && size < getSetup().getLong(HOST_S3_SINGLEPART_SIZE)) {
                    requestBody = RequestBody.fromBytes(IoUtils.toByteArray(in));
                } else {
                    requestBody = RequestBody.fromInputStream(in, size);
                }
                s3.getS3Client().putObject(
                        PutObjectRequest.builder().bucket(bnk[0]).key(bnk[1]).contentLength(size).build(), requestBody);
            } catch (final S3Exception e) {
                _log.debug("putObject", e);
                throw new IOException("Pushing object " + name + ": " + formatS3Exception(e));
            } catch (final Exception e) {
                _log.debug("putObject", e);
                throw new IOException("Pushing object " + name + ": " + Format.getMessage(e, "", 0));
            }
        }
        return true;
    }

    /**
     * Put.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        _log.debug("Put file {} ({})", name, posn);
        setStatus("PUT");
        final var bnk = getBucketNameAndKey(name);
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        _log.debug("Using MultipartUploadOutputStream");
        return new MultipartUploadOutputStream(bnk[0], bnk[1], partSize);
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        _log.debug("Get file {}", name);
        setStatus("GET");
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        final var bnk = getBucketNameAndKey(name);
        try {
            s3input = s3.getS3Client().getObject(GetObjectRequest.builder().bucket(bnk[0]).key(bnk[1]).build());
            return s3input;
        } catch (final S3Exception e) {
            _log.debug("getObject", e);
            throw new IOException("Getting Object " + name + ": " + formatS3Exception(e));
        } catch (final Exception e) {
            _log.debug("getObject", e);
            throw new IOException("Getting Object " + name + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * Size.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long size(final String name) throws IOException {
        _log.debug("Size {}", name);
        setStatus("SIZE");
        final var bnk = getBucketNameAndKey(name);
        try {
            // Use HeadObject — avoids downloading the object body just to get its size.
            return s3.getS3Client().headObject(HeadObjectRequest.builder().bucket(bnk[0]).key(bnk[1]).build())
                    .contentLength();
        } catch (final S3Exception e) {
            _log.debug("headObject", e);
            throw new IOException("Getting size of " + name + ": " + formatS3Exception(e));
        } catch (final Exception e) {
            _log.debug("headObject", e);
            throw new IOException("Getting size of " + name + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * Mkdir.
     *
     * @param directory
     *            the directory
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void mkdir(final String directory) throws IOException {
        _log.debug("Mkdir {}", directory);
        setStatus("MKDIR");
        if (isNotEmpty(bucketName)) {
            throw new IOException(
                    "Mkdir not allowed when parameter " + getSetup().getModuleName() + ".bucketName is set");
        }
        final var token = new StringTokenizer(directory == null ? "" : directory.replace('\\', '/'), "/");
        final var count = token.countTokens();
        if (count == 0) {
            throw new IOException("Invalid directory specified: empty");
        }
        if (count == 1) {
            final var bucketName = token.nextToken();
            try {
                s3.getS3Client().createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            } catch (final S3Exception e) {
                _log.debug("createBucket", e);
                throw new IOException("Creating Bucket " + bucketName + ": " + formatS3Exception(e));
            } catch (final Exception e) {
                _log.debug("createBucket", e);
                throw new IOException("Creating Bucket " + bucketName + ": " + Format.getMessage(e, "", 0));
            }
        } else {
            throw new IOException("Subdirectories not allowed in Bucket");
        }
    }

    /**
     * Rmdir.
     *
     * @param directory
     *            the directory
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void rmdir(final String directory) throws IOException {
        _log.debug("Rmdir {}", directory);
        setStatus("RMDIR");
        if (isNotEmpty(bucketName)) {
            throw new IOException(
                    "Rmdir not allowed when parameter " + getSetup().getModuleName() + ".bucketName is set");
        }
        final var token = new StringTokenizer(directory == null ? "" : directory.replace('\\', '/'), "/");
        final var count = token.countTokens();
        if (count == 0) {
            throw new IOException("Invalid directory specified: empty");
        }
        if (count == 1) {
            final var bucketName = token.nextToken();
            try {
                s3.getS3Client().deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
            } catch (final S3Exception e) {
                _log.debug("deleteBucket", e);
                throw new IOException("Deleting Bucket " + bucketName + ": " + formatS3Exception(e));
            } catch (final Exception e) {
                _log.debug("deleteBucket", e);
                throw new IOException("Deleting Bucket " + bucketName + ": " + Format.getMessage(e, "", 0));
            }
        } else {
            throw new IOException("Subdirectories not allowed in Bucket");
        }
    }

    /**
     * The Interface ListOutput.
     */
    private interface ListOutput {

        /**
         * Adds the.
         *
         * @param line
         *            the line
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void add(String line) throws IOException;
    }

    /**
     * List.
     *
     * @param output
     *            the output
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void list(final ListOutput output, final String directory, final String pattern) throws IOException {
        if (getDebug()) {
            _log.debug("list: {},{}", directory, pattern);
        }
        final var token = new StringTokenizer(directory == null ? "" : directory.replace('\\', '/'), "/");
        final var count = token.countTokens();
        try {
            if (isNotEmpty(bucketName)) {
                // We have a Bucket defined in the setup!
                if (count == 0) {
                    // We are going to list the specified Bucket!
                    listObjects(output, bucketName, null, pattern);
                } else {
                    // We have a file name specified!
                    listObjects(output, bucketName, processKey(token.nextToken("\0")), pattern);
                }
            } else if (count == 0) {
                // We have to display the list of Buckets!
                listBuckets(output, null, pattern);
            } else if (count == 1) {
                final var extractedBucketName = token.nextToken();
                if (extractedBucketName.contains("*") || extractedBucketName.contains("?")) {
                    // the name contains wildcards so we will show the list!
                    listBuckets(output, extractedBucketName, pattern);
                } else {
                    // We are going to list the specified Bucket!
                    listObjects(output, bucketName, null, pattern);
                }
            } else {
                // We have a file name specified!
                final var extractedBucketName = token.nextToken();
                listObjects(output, extractedBucketName, processKey(token.nextToken("\0")), pattern);
            }
        } catch (final S3Exception e) {
            _log.debug("list", e);
            throw new IOException("Listing " + directory + ": " + formatS3Exception(e));
        } catch (final Exception e) {
            _log.debug("list", e);
            throw new IOException("Listing " + directory + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * List as string array.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        _log.debug("listAsStringArray{}{}", isEmpty(directory) ? "" : " " + directory,
                isEmpty(pattern) ? "" : " (" + pattern + ")");
        setStatus("LIST");
        final List<String> list = new ArrayList<>();
        list(list::add, directory, pattern);
        return list.toArray(new String[list.size()]);
    }

    /**
     * List as byte array.
     *
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     *
     * @return the byte[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public byte[] listAsByteArray(final String directory, final String pattern) throws IOException {
        _log.debug("listAsByteArray: {},{}", directory, pattern);
        setStatus("LIST");
        final var out = new ByteArrayOutputStream();
        final var gzip = new GZIPOutputStream(out, Deflater.BEST_COMPRESSION);
        list(line -> gzip.write(line.concat("\n").getBytes()), directory, pattern);
        gzip.close();
        return out.toByteArray();
    }

    /**
     * List buckets.
     *
     * @param output
     *            the output
     * @param bucketName
     *            the bucket name
     * @param pattern
     *            the pattern
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void listBuckets(final ListOutput output, final String bucketName, final String pattern)
            throws IOException {
        if (getDebug()) {
            _log.debug("listBuckets: {},{}", bucketName, pattern);
        }
        final var result = s3.getS3Client().listBuckets();
        // In v2, the owner is account-level (same for all buckets) and returned on the response.
        final var responseOwner = result.owner();
        final var defaultOwnerName = (responseOwner != null && responseOwner.displayName() != null)
                ? responseOwner.displayName() : "unknown";
        for (final Bucket bucket : result.buckets()) {
            final var name = bucket.name();
            final var matchesPattern = (pattern == null || name.matches(pattern));
            var matchesWildcard = true;
            if (bucketName != null) {
                final var filter = WildcardFileFilter.builder().setWildcards(bucketName).get();
                matchesWildcard = filter.accept(new java.io.File(name));
            }
            if (matchesPattern && matchesWildcard) {
                final var creationTime = bucket.creationDate() != null ? bucket.creationDate().toEpochMilli()
                        : System.currentTimeMillis();
                output.add(Format.getFtpList("drw-r--r--", getSetup().get(HOST_S3_FTPUSER, defaultOwnerName),
                        getSetup().get(HOST_S3_FTPGROUP, defaultOwnerName), "2048", creationTime, name));
            }
        }
    }

    /**
     * List the objects in the specified bucket!
     *
     * @param output
     *            the output
     * @param bucketName
     *            the bucket name
     * @param fileName
     *            the file name
     * @param pattern
     *            the pattern
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void listObjects(final ListOutput output, final String bucketName, final String fileName,
            final String pattern) throws IOException {
        listObjects(output, bucketName, "", fileName, pattern, getSetup().get(HOST_S3_RECURSIVE_LEVEL, 0));
    }

    /**
     * List the objects in the specified bucket!
     *
     * @param output
     *            the output
     * @param bucketName
     *            the bucket name
     * @param rootName
     *            the root name
     * @param fileName
     *            the file name
     * @param pattern
     *            the pattern
     * @param level
     *            the level
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void listObjects(final ListOutput output, final String bucketName, final String rootName,
            final String fileName, final String pattern, final int level) throws IOException {
        if (getDebug()) {
            _log.debug("listObjects: {},{},{}", bucketName, fileName, pattern);
        }
        final var prefix = s3prefix + (fileName != null ? fileName : "");
        final var reqBuilder = ListObjectsV2Request.builder().fetchOwner(true);
        if (isNotEmpty(bucketName)) {
            reqBuilder.bucket(bucketName);
        }
        if (isNotEmpty(prefix)) {
            reqBuilder.prefix(prefix);
            if (getDebug()) {
                _log.debug("Using prefix: {}", prefix);
            }
        }
        // Paginate through all results using the continuation token.
        String continuationToken = null;
        do {
            if (continuationToken != null) {
                reqBuilder.continuationToken(continuationToken);
            }
            final var response = s3.getS3Client().listObjectsV2(reqBuilder.build());
            for (final S3Object objectSummary : response.contents()) {
                var name = objectSummary.key();
                if (getDebug()) {
                    _log.debug("Processing key: {}", name);
                }
                if (isNotEmpty(prefix)) {
                    name = name.substring(prefix.length());
                    if (getDebug()) {
                        _log.debug("Name: {} ({})", name, pattern);
                    }
                }
                if (pattern == null || name.matches(pattern)) {
                    final var isDirectory = name.endsWith("/");
                    if (level > 0 && isDirectory) {
                        listObjects(output, bucketName, rootName + name, fileName + name, pattern, level - 1);
                    } else {
                        final var displayName = (!rootName.isBlank() ? rootName : "") + name;
                        if (pattern == null || displayName.matches(pattern)) {
                            final var owner = objectSummary.owner();
                            final var ownerName = (owner != null && owner.displayName() != null) ? owner.displayName()
                                    : "unknown";
                            final var lastModified = objectSummary.lastModified();
                            final var entry = Format.getFtpList((isDirectory ? "d" : "-") + "rw-r--r--",
                                    getSetup().get(HOST_S3_FTPUSER, ownerName),
                                    getSetup().get(HOST_S3_FTPGROUP, ownerName), String.valueOf(objectSummary.size()),
                                    lastModified != null ? lastModified.toEpochMilli() : System.currentTimeMillis(),
                                    displayName);
                            output.add(entry);
                            if (getDebug()) {
                                _log.debug("Adding entry: {}", entry);
                            }
                        }
                    }
                }
            }
            continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;
        } while (continuationToken != null);
    }

    /**
     * Gets the setup. Utility method to get the ECtransSetup and check if the module is not closed!
     *
     * @return the setup
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private ECtransSetup getSetup() throws IOException {
        if (closed.get()) {
            throw new IOException("Module closed");
        }
        return currentSetup;
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            _log.debug("Close connection");
            currentStatus = "CLOSE";
            StreamPlugThread.closeQuietly(s3input);
            if (s3 != null)
                s3.close();
            _log.debug("Close completed");
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the status
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void setStatus(final String status) throws IOException {
        _log.debug("Status set to: {}", status);
        if (closed.get()) {
            throw new IOException("Module closed");
        }
        currentStatus = status;
    }

    /**
     * An {@link SSLConnectionSocketFactory} subclass that applies all {@link SocketConfig} TCP options (NO_DELAY,
     * QUICK_ACK, congestion control, keepalive tuning, etc.) to the underlying TCP socket inside
     * {@link #prepareSocket(SSLSocket)}, which is called after TCP connection is established but before the SSL
     * handshake begins. This is the same hook used by the SDK's own {@code SdkTlsSocketFactory}. When
     * {@link ClientSocketStatistics} is present in the config, each connected socket is tracked so that
     * {@link #updateStatistics()} can snapshot OS-level TCP statistics on demand.
     */
    private static final class TcpTunedSslSocketFactory extends SSLConnectionSocketFactory {

        private final SocketConfig tcpConfig;
        private final ClientSocketStatistics statistics;
        private final Queue<SocketEntry> tracked = new ConcurrentLinkedQueue<>();

        /** Strict-mode constructor — uses the default hostname verifier. */
        TcpTunedSslSocketFactory(final SSLContext ctx, final SocketConfig cfg) {
            super(ctx);
            tcpConfig = cfg;
            statistics = cfg.getStatistics();
        }

        /** Non-strict constructor — allows a custom (e.g. no-op) hostname verifier. */
        TcpTunedSslSocketFactory(final SSLContext ctx, final HostnameVerifier hv, final SocketConfig cfg) {
            super(ctx, hv);
            tcpConfig = cfg;
            statistics = cfg.getStatistics();
        }

        @Override
        protected void prepareSocket(final SSLSocket socket) throws IOException {
            super.prepareSocket(socket);
            final var tcp = getUnderlyingSocket(socket);
            tcpConfig.configureSocket(tcp);
            if (statistics != null) {
                tracked.add(new SocketEntry(tcp, System.currentTimeMillis()));
            }
        }

        /**
         * Snapshots OS-level TCP statistics for all currently live sockets. Closed or disconnected sockets are pruned
         * from the tracking queue.
         *
         * @throws IOException
         *             if an error occurs collecting statistics for a socket
         */
        void updateStatistics() throws IOException {
            if (statistics == null) {
                return;
            }
            tracked.removeIf(e -> e.socket().isClosed() || !e.socket().isConnected());
            for (final var entry : tracked) {
                statistics.add(entry.socket(), entry.startTime());
            }
        }

        /**
         * Unwrap the SSL socket to get the underlying TCP socket so that native TCP options (congestion algorithm,
         * quick-ack, etc.) can be applied via file-descriptor access. Falls back to the SSL socket itself if the JVM
         * internals are not accessible.
         */
        private static Socket getUnderlyingSocket(final SSLSocket sslSocket) {
            try {
                final var selfField = Class.forName("sun.security.ssl.BaseSSLSocketImpl").getDeclaredField("self");
                selfField.setAccessible(true);
                if (selfField.get(sslSocket) instanceof final Socket socket) {
                    return socket;
                }
            } catch (final Exception e) {
                // Reflection not available — fall through to the SSL socket itself
            }
            return sslSocket;
        }
    }

    /**
     * A {@link PlainConnectionSocketFactory} subclass that applies all {@link SocketConfig} TCP options and tracks each
     * connected plain (HTTP) socket for statistics collection, mirroring what {@link TcpTunedSslSocketFactory} does for
     * HTTPS connections.
     */
    private static final class TcpTunedPlainSocketFactory extends PlainConnectionSocketFactory {

        private final SocketConfig tcpConfig;
        private final ClientSocketStatistics statistics;
        private final Queue<SocketEntry> tracked = new ConcurrentLinkedQueue<>();

        TcpTunedPlainSocketFactory(final SocketConfig cfg) {
            tcpConfig = cfg;
            statistics = cfg.getStatistics();
        }

        @Override
        public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
                final java.net.InetSocketAddress remoteAddress, final java.net.InetSocketAddress localAddress,
                final HttpContext context) throws IOException {
            final var connected = super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress,
                    context);
            tcpConfig.configureSocket(connected);
            if (statistics != null) {
                tracked.add(new SocketEntry(connected, System.currentTimeMillis()));
            }
            return connected;
        }

        void updateStatistics() throws IOException {
            if (statistics == null) {
                return;
            }
            tracked.removeIf(e -> e.socket().isClosed() || !e.socket().isConnected());
            for (final var entry : tracked) {
                statistics.add(entry.socket(), entry.startTime());
            }
        }
    }

    /**
     * A thin {@link SdkHttpClient} adapter backed by a pre-built Apache {@link CloseableHttpClient} with custom
     * TCP-tuned socket factories registered for both "http" and "https" schemes. This replaces the
     * {@code ApacheHttpClient.builder().socketFactory(...)} approach, which only hooks into HTTPS connections, so that
     * plain-HTTP S3 endpoints also contribute socket statistics.
     *
     * <p>
     * Headers managed by Apache via the request entity (Content-Length, Transfer-Encoding) are excluded from the
     * forwarded headers to avoid duplicates; Content-Length is read from the SDK request and applied to the entity
     * directly.
     */
    private static final class TrackingApacheSdkHttpClient implements SdkHttpClient {

        private final CloseableHttpClient httpClient;

        TrackingApacheSdkHttpClient(final CloseableHttpClient client) {
            httpClient = client;
        }

        @Override
        public ExecutableHttpRequest prepareRequest(final HttpExecuteRequest sdkRequest) {
            final var request = sdkRequest.httpRequest();
            final var uri = request.getUri();
            final var body = sdkRequest.contentStreamProvider().orElse(null);
            // Build Apache request based on method
            final HttpUriRequest apacheRequest;
            switch (request.method().name().toUpperCase(java.util.Locale.ROOT)) {
            case "GET" -> apacheRequest = new HttpGet(uri);
            case "HEAD" -> apacheRequest = new HttpHead(uri);
            case "DELETE" -> apacheRequest = new HttpDelete(uri);
            case "PUT" -> {
                final var put = new HttpPut(uri);
                if (body != null) {
                    final var contentLength = request.firstMatchingHeader("Content-Length").map(Long::parseLong)
                            .orElse(-1L);
                    put.setEntity(new InputStreamEntity(body.newStream(), contentLength));
                }
                apacheRequest = put;
            }
            case "POST" -> {
                final var post = new HttpPost(uri);
                if (body != null) {
                    final var contentLength = request.firstMatchingHeader("Content-Length").map(Long::parseLong)
                            .orElse(-1L);
                    post.setEntity(new InputStreamEntity(body.newStream(), contentLength));
                }
                apacheRequest = post;
            }
            default -> throw new UnsupportedOperationException("Unsupported HTTP method: " + request.method());
            }
            // Forward headers; skip entity-managed headers to avoid duplicates
            request.headers().forEach((name, values) -> {
                if (!"Content-Length".equalsIgnoreCase(name) && !"Transfer-Encoding".equalsIgnoreCase(name)) {
                    values.forEach(v -> apacheRequest.addHeader(name, v));
                }
            });
            return new ExecutableHttpRequest() {
                @Override
                public HttpExecuteResponse call() throws IOException {
                    final var apacheResponse = httpClient.execute(apacheRequest);
                    final var statusLine = apacheResponse.getStatusLine();
                    final var sdkResponseBuilder = SdkHttpResponse.builder().statusCode(statusLine.getStatusCode())
                            .statusText(statusLine.getReasonPhrase());
                    for (final var header : apacheResponse.getAllHeaders()) {
                        sdkResponseBuilder.appendHeader(header.getName(), header.getValue());
                    }
                    final var entity = apacheResponse.getEntity();
                    final AbortableInputStream bodyStream = entity != null
                            ? AbortableInputStream.create(entity.getContent(), () -> {
                                try {
                                    apacheResponse.close();
                                } catch (final IOException ignored) {
                                }
                            }) : null;
                    return HttpExecuteResponse.builder().response(sdkResponseBuilder.build()).responseBody(bodyStream)
                            .build();
                }

                @Override
                public void abort() {
                    apacheRequest.abort();
                }
            };
        }

        @Override
        public String clientName() {
            return "TrackingApacheSdkHttpClient";
        }

        @Override
        public void close() {
            try {
                httpClient.close();
            } catch (final IOException ignored) {
            }
        }
    }

    /** Shared socket-tracking entry used by both TCP-tuned socket factories. */
    private record SocketEntry(Socket socket, long startTime) {
    }

    /**
     * The Class Session.
     */
    private static class Session {

        /** The S3 client. */
        private final S3Client s3Client;

        /** Tracks live HTTPS sockets for statistics collection. */
        private final TcpTunedSslSocketFactory sslSocketFactory;

        /** Tracks live plain-HTTP sockets for statistics collection. */
        private final TcpTunedPlainSocketFactory plainSocketFactory;

        /** The custom HTTP client — must be closed alongside the S3 client. */
        private final TrackingApacheSdkHttpClient httpClient;

        /**
         * Instantiates a new Session.
         *
         * @param client
         *            the S3 client
         * @param sslFactory
         *            the SSL socket factory
         * @param plainFactory
         *            the plain socket factory
         * @param trackingClient
         *            the custom HTTP client backing the S3 client
         */
        Session(final S3Client client, final TcpTunedSslSocketFactory sslFactory,
                final TcpTunedPlainSocketFactory plainFactory, final TrackingApacheSdkHttpClient trackingClient) {
            s3Client = client;
            sslSocketFactory = sslFactory;
            plainSocketFactory = plainFactory;
            httpClient = trackingClient;
        }

        /**
         * Discovers the actual AWS region for a bucket by calling GetBucketLocation with a temporary cross-region
         * enabled client. This avoids stream-replay errors that occur when a PUT/upload request hits a 301 redirect.
         *
         * @param creds
         *            the credentials provider
         * @param bucketName
         *            the bucket name to look up
         *
         * @return the actual region for the bucket (e.g. "eu-west-1"), or null if discovery fails
         */
        private static String discoverBucketRegion(final AwsCredentialsProvider creds, final String bucketName) {
            // Use ApacheHttpClient explicitly — the SDK default falls back to apache5 which
            // is not on the classpath.
            try (final var discoveryClient = S3Client.builder().credentialsProvider(creds).region(Region.US_EAST_1)
                    .crossRegionAccessEnabled(true).httpClientBuilder(ApacheHttpClient.builder()).build()) {
                final var location = discoveryClient
                        .getBucketLocation(GetBucketLocationRequest.builder().bucket(bucketName).build())
                        .locationConstraintAsString();
                // Empty string means us-east-1; "EU" is the legacy alias for eu-west-1
                if (location == null || location.isEmpty()) {
                    return Region.US_EAST_1.id();
                }
                return "EU".equals(location) ? "eu-west-1" : location;
            } catch (final Exception e) {
                _log.warn("Could not discover region for bucket '{}': {}", bucketName, e.getMessage());
                return null;
            }
        }

        /**
         * Gets the Session.
         *
         * @param user
         *            the access key id
         * @param password
         *            the secret access key
         * @param listenAddress
         *            the local address to bind outgoing connections to (optional)
         * @param tcpConfig
         *            the tcp config (SSL context + socket options applied via TcpTunedSslSocketFactory)
         * @param protocol
         *            the SSL protocol (e.g. TLS)
         * @param sslValidation
         *            the ssl validation
         * @param strict
         *            the strict hostname verification
         * @param acceleration
         *            the acceleration
         * @param dualstack
         *            the dualstack
         * @param disableChunkedEncoding
         *            the disable chunked encoding
         * @param enablePathStyleAccess
         *            the enable path style access
         * @param crossRegionAccess
         *            when true, automatically follow cross-region redirects to the correct bucket region
         * @param bucketName
         *            the bucket name
         * @param url
         *            the url
         * @param region
         *            the region
         * @param mkBucket
         *            the mk bucket
         * @param roleArn
         *            the role arn
         * @param roleSessionName
         *            the role session name
         * @param durationSeconds
         *            the duration seconds
         * @param externalId
         *            the external id
         * @param requestChecksumCalculation
         *            the request checksum calculation (WHEN_SUPPORTED or WHEN_REQUIRED, empty to use SDK default)
         * @param responseChecksumValidation
         *            the response checksum validation (WHEN_SUPPORTED or WHEN_REQUIRED, empty to use SDK default)
         * @param debug
         *            the debug
         *
         * @return the session
         *
         * @throws NoSuchAlgorithmException
         *             the no such algorithm exception
         * @throws KeyManagementException
         *             the key management exception
         */
        static Session getSession(final String user, final String password, final String listenAddress,
                final SocketConfig tcpConfig, final String protocol, final boolean sslValidation, final boolean strict,
                final boolean acceleration, final boolean dualstack, final boolean disableChunkedEncoding,
                final boolean enablePathStyleAccess, final boolean crossRegionAccess, final String bucketName,
                final String url, final String region, final boolean mkBucket, final String roleArn,
                final String roleSessionName, final int durationSeconds, final String externalId,
                final String requestChecksumCalculation, final String responseChecksumValidation, final boolean debug)
                throws NoSuchAlgorithmException, KeyManagementException {
            // Build socket factories for both schemes.
            // TcpTunedSslSocketFactory hooks into prepareSocket() to apply TCP options + track HTTPS sockets.
            // TcpTunedPlainSocketFactory hooks into connectSocket() to apply TCP options + track HTTP sockets.
            final var sslContext = sslValidation ? getSslContext(protocol)
                    : SocketConfig.getBlindlyTrustingSSLContext(protocol);
            final var sslSocketFactory = strict ? new TcpTunedSslSocketFactory(sslContext, tcpConfig)
                    : new TcpTunedSslSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE, tcpConfig);
            final var plainSocketFactory = new TcpTunedPlainSocketFactory(tcpConfig);
            // Build a unified Apache HTTP client with custom factories for both "http" and "https".
            // This replaces ApacheHttpClient.builder().socketFactory() which only hooks into HTTPS,
            // so that plain-HTTP S3 endpoints also produce socket statistics.
            final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                    .<ConnectionSocketFactory> create().register("http", plainSocketFactory)
                    .register("https", sslSocketFactory).build();
            final var connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            connectionManager.setMaxTotal(50);
            connectionManager.setDefaultMaxPerRoute(50);
            final var hcBuilder = HttpClients.custom().setConnectionManager(connectionManager);
            if (isNotEmpty(listenAddress)) {
                try {
                    hcBuilder.setDefaultRequestConfig(
                            RequestConfig.custom().setLocalAddress(InetAddress.getByName(listenAddress)).build());
                } catch (final UnknownHostException e) {
                    _log.warn("Cannot set listen address: {}", listenAddress, e);
                }
            }
            final var trackingHttpClient = new TrackingApacheSdkHttpClient(hcBuilder.build());
            // S3-specific service configuration.
            final var s3Config = S3Configuration.builder().accelerateModeEnabled(acceleration)
                    .pathStyleAccessEnabled(enablePathStyleAccess).chunkedEncodingEnabled(!disableChunkedEncoding)
                    .build();
            // Build the S3 client.
            // STS uses the global us-east-1 endpoint — region only matters for service-specific STS endpoints.
            final var stsRegion = isNotEmpty(region) ? region : Region.US_EAST_1.id();
            final var creds = loadCredentials(user, password, stsRegion, sslContext, roleArn, roleSessionName,
                    durationSeconds, externalId);
            final var clientBuilder = S3Client.builder().credentialsProvider(creds).serviceConfiguration(s3Config)
                    .dualstackEnabled(dualstack).httpClient(trackingHttpClient);
            if (isNotEmpty(requestChecksumCalculation)) {
                clientBuilder
                        .requestChecksumCalculation(RequestChecksumCalculation.fromValue(requestChecksumCalculation));
            }
            if (isNotEmpty(responseChecksumValidation)) {
                clientBuilder
                        .responseChecksumValidation(ResponseChecksumValidation.fromValue(responseChecksumValidation));
            }
            // Resolve the effective region: explicit config wins; otherwise auto-discover from bucket.
            var resolvedRegion = isNotEmpty(region) ? region : null;
            if (resolvedRegion == null && isNotEmpty(bucketName)) {
                resolvedRegion = discoverBucketRegion(creds, bucketName);
                if (resolvedRegion != null) {
                    _log.info("Auto-discovered region '{}' for bucket '{}'", resolvedRegion, bucketName);
                }
            }
            if (resolvedRegion == null) {
                resolvedRegion = Region.US_EAST_1.id();
                _log.warn("Could not determine region for bucket '{}'; falling back to '{}'", bucketName,
                        resolvedRegion);
            }
            if (crossRegionAccess) {
                clientBuilder.region(Region.of(resolvedRegion)).crossRegionAccessEnabled(true);
                _log.debug("Cross-region access enabled: using region '{}' for bucket '{}'", resolvedRegion,
                        bucketName);
            } else if (acceleration || isEmpty(url)) {
                clientBuilder.region(Region.of(resolvedRegion));
            } else {
                clientBuilder.endpointOverride(URI.create(url)).region(Region.of(resolvedRegion));
                _log.debug("EndPoint: {} - region: {}", url, resolvedRegion);
            }
            if (debug) {
                clientBuilder.overrideConfiguration(c -> c.addExecutionInterceptor(new ExecutionInterceptor() {
                    @Override
                    public void beforeTransmission(final Context.BeforeTransmission context,
                            final ExecutionAttributes attributes) {
                        context.httpRequest().headers().forEach((k, v) -> _log.debug("Request Header: {}={}", k, v));
                    }

                    @Override
                    public void afterExecution(final Context.AfterExecution context,
                            final ExecutionAttributes attributes) {
                        context.httpResponse().headers().forEach((k, v) -> _log.debug("Response Header: {}={}", k, v));
                    }
                }));
            }
            var connected = false;
            S3Client s3 = null;
            try {
                s3 = clientBuilder.build();
                if (isNotEmpty(bucketName) && mkBucket) {
                    var bucketFound = false;
                    for (final var bucket : s3.listBuckets().buckets()) {
                        if (bucketName.equals(bucket.name())) {
                            bucketFound = true;
                            break;
                        }
                    }
                    if (!bucketFound) {
                        s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    }
                }
                connected = true;
            } finally {
                if (!connected && s3 != null) {
                    try {
                        s3.close();
                    } catch (final Throwable t) {
                        // Ignore
                    }
                    trackingHttpClient.close();
                }
            }
            return new Session(s3, sslSocketFactory, plainSocketFactory, trackingHttpClient);
        }

        /**
         * Gets the S3 client.
         *
         * @return the S3 client
         */
        S3Client getS3Client() {
            return s3Client;
        }

        /**
         * Closes the S3 client and the backing Apache HTTP client. When using a pre-built {@link SdkHttpClient} via
         * {@code .httpClient()}, the SDK does not close it on {@code S3Client.close()}, so we do it explicitly.
         */
        void close() {
            s3Client.close();
            if (httpClient != null) {
                httpClient.close();
            }
        }

        /**
         * Snapshots OS-level TCP statistics for all live sockets tracked by both the SSL and plain socket factories.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void updateStatistics() throws IOException {
            sslSocketFactory.updateStatistics();
            plainSocketFactory.updateStatistics();
        }

        /**
         * Builds a validated SSL context for the given protocol.
         *
         * @param protocol
         *            the protocol
         *
         * @return the SSL context
         *
         * @throws NoSuchAlgorithmException
         *             the no such algorithm exception
         * @throws KeyManagementException
         *             the key management exception
         */
        private static SSLContext getSslContext(final String protocol)
                throws NoSuchAlgorithmException, KeyManagementException {
            final var ctx = SSLContext.getInstance(protocol);
            ctx.init(null, null, null);
            return ctx;
        }

        /**
         * Load credentials. Returns a static provider wrapping real AWS keys, anonymous credentials, or temporary STS
         * credentials obtained by assuming a role.
         *
         * @param accessKeyId
         *            the access key id
         * @param secretAccessKey
         *            the secret access key
         * @param region
         *            the region
         * @param sslContext
         *            the ssl context used for the STS client
         * @param roleArn
         *            the role arn
         * @param roleSessionName
         *            the role session name
         * @param durationSeconds
         *            the duration seconds
         * @param externalId
         *            the external id
         *
         * @return the aws credentials provider
         */
        private static AwsCredentialsProvider loadCredentials(final String accessKeyId, final String secretAccessKey,
                final String region, final SSLContext sslContext, final String roleArn, final String roleSessionName,
                final int durationSeconds, final String externalId) {
            // Base credentials: real key-pair or anonymous.
            final var baseProvider = (accessKeyId != null && !accessKeyId.isBlank())
                    ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))
                    : AnonymousCredentialsProvider.create();
            // If no role is specified, return the base provider directly.
            if (isEmpty(roleArn) || isEmpty(roleSessionName)) {
                return baseProvider;
            }
            // Assume the specified role via STS.
            try (final var stsClient = StsClient.builder().credentialsProvider(baseProvider).region(Region.of(region))
                    .httpClientBuilder(
                            ApacheHttpClient.builder().socketFactory(new SSLConnectionSocketFactory(sslContext)))
                    .build()) {
                final var assumeReqBuilder = AssumeRoleRequest.builder().roleArn(roleArn)
                        .roleSessionName(roleSessionName).durationSeconds(durationSeconds);
                if (externalId != null && !externalId.isBlank()) {
                    assumeReqBuilder.externalId(externalId);
                }
                final var stsCreds = stsClient.assumeRole(assumeReqBuilder.build()).credentials();
                return StaticCredentialsProvider.create(AwsSessionCredentials.create(stsCreds.accessKeyId(),
                        stsCreds.secretAccessKey(), stsCreds.sessionToken()));
            }
        }
    }

    /**
     * An OutputStream that accumulates data into fixed-size parts and uploads them concurrently via the S3 multipart
     * upload API. Parts are submitted to a thread pool as they fill, allowing the next part to be buffered from the
     * source while prior parts are in-flight to S3 (pipelining).
     *
     * <p>
     * Memory is bounded and deterministic: a pool of {@code (numUploadThreads + queueCapacity)} byte arrays of
     * {@code partSize} MB each is allocated once at construction time. The write thread takes a buffer from the pool,
     * fills it, and hands it <em>directly</em> to the upload task — no {@code Arrays.copyOf} is performed. When the
     * upload completes the buffer is returned to the pool. If the pool is empty the write thread blocks (back-pressure)
     * instead of allocating new heap. Peak memory = {@code (numUploadThreads + queueCapacity) × partSize} MB, allocated
     * once, reused indefinitely.
     *
     * <p>
     * The upload is completed when {@link #close()} is called, which waits for all in-flight parts before issuing the
     * final {@code CompleteMultipartUpload}. On error, {@link #abort()} cancels pending work and aborts the in-progress
     * upload on S3.
     */
    private final class MultipartUploadOutputStream extends FilterOutputStream {

        /** The bucket. */
        private final String bucket;

        /** The key. */
        private final String key;

        /** The part size bytes. */
        private final int partSizeBytes;

        /** The upload id. */
        private final String uploadId;

        /** Executor for concurrent part uploads. */
        private final ExecutorService uploadExecutor;

        /**
         * Pool of pre-allocated part buffers. The write thread acquires a buffer (blocking when the pool is empty),
         * fills it, submits it to the upload task, and the upload task returns it when done. This eliminates per-part
         * heap allocations and GC pressure while providing the same back-pressure as the semaphore approach.
         */
        private final ArrayBlockingQueue<byte[]> bufferPool;

        /** Futures for in-flight part uploads, in submission order. */
        private final List<Future<CompletedPart>> pendingParts = new ArrayList<>();

        /** The current write buffer (taken from the pool; replaced after each part submission). */
        private byte[] partBuffer;

        /** The buffer position. */
        private int bufferPos = 0;

        /** The part number (1-based), incremented synchronously before each async submission. */
        private int partNumber = 1;

        /** Whether close() has already been called. */
        private boolean uploadClosed = false;

        /**
         * Initiates a new multipart upload for the given bucket and key.
         *
         * @param bucket
         *            the bucket
         * @param key
         *            the key
         * @param partSizeMB
         *            the part size in megabytes (must be ≥ 5 MB for all parts except the last)
         *
         * @throws IOException
         *             if the multipart upload cannot be initiated
         */
        MultipartUploadOutputStream(final String bucket, final String key, final int partSizeMB) throws IOException {
            super(OutputStream.nullOutputStream());
            this.bucket = bucket;
            this.key = key;
            this.partSizeBytes = partSizeMB * 1024 * 1024;
            // Pre-allocate the buffer pool. Total buffers = numUploadThreads (active uploads) +
            // queueCapacity (queued) + 1 (current write buffer). Each buffer is partSizeBytes.
            // This is the only heap allocation for part data for the lifetime of the upload.
            final var poolSize = numUploadThreads + queueCapacity;
            this.bufferPool = new ArrayBlockingQueue<>(poolSize);
            for (var i = 0; i < poolSize; i++) {
                bufferPool.add(new byte[this.partSizeBytes]);
            }
            // Take the first write buffer from the pool
            this.partBuffer = bufferPool.poll();
            this.uploadExecutor = new ThreadPoolExecutor(numUploadThreads, numUploadThreads, 0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(queueCapacity), new ThreadPoolExecutor.AbortPolicy());
            try {
                uploadId = s3.getS3Client()
                        .createMultipartUpload(CreateMultipartUploadRequest.builder().bucket(bucket).key(key).build())
                        .uploadId();
            } catch (final S3Exception e) {
                uploadExecutor.shutdownNow();
                throw new IOException("Initiating multipart upload for " + key + ": " + formatS3Exception(e));
            } catch (final Exception e) {
                uploadExecutor.shutdownNow();
                throw new IOException("Initiating multipart upload for " + key + ": " + Format.getMessage(e, "", 0));
            }
        }

        @Override
        public void write(final int b) throws IOException {
            partBuffer[bufferPos++] = (byte) b;
            if (bufferPos == partSizeBytes) {
                submitCurrentPart();
            }
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            var remaining = len;
            var offset = off;
            while (remaining > 0) {
                final var space = partSizeBytes - bufferPos;
                final var toWrite = Math.min(remaining, space);
                System.arraycopy(b, offset, partBuffer, bufferPos, toWrite);
                bufferPos += toWrite;
                offset += toWrite;
                remaining -= toWrite;
                if (bufferPos == partSizeBytes) {
                    submitCurrentPart();
                }
            }
        }

        /**
         * Hands the current buffer directly to an async upload task (zero-copy), then acquires a fresh buffer from the
         * pool — blocking if all buffers are in use (back-pressure). Peak heap usage is bounded to
         * {@code (numUploadThreads + queueCapacity) × partSize} MB, allocated once at construction.
         */
        private void submitCurrentPart() throws IOException {
            if (bufferPos == 0) {
                return;
            }
            // Hand off the current buffer directly — no copy needed.
            final var data = partBuffer;
            final var dataLen = bufferPos;
            final int currentPartNumber = partNumber++;
            bufferPos = 0;
            // Acquire the next write buffer from the pool BEFORE submitting, so the executor
            // queue never holds more tasks than there are pool buffers (prevents RejectedExecution).
            try {
                partBuffer = bufferPool.take();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(
                        "Interrupted waiting for buffer pool slot for part " + currentPartNumber + " of " + key, e);
            }
            // Submit upload; the upload task returns its buffer to the pool when done.
            try {
                pendingParts.add(uploadExecutor.submit(() -> {
                    try {
                        // Full parts hand off the buffer directly (zero-copy).
                        // Partial final parts need a trim — only one copy per upload, not per part.
                        final var body = dataLen == data.length ? data : Arrays.copyOf(data, dataLen);
                        final var response = s3.getS3Client().uploadPart(
                                UploadPartRequest.builder().bucket(bucket).key(key).uploadId(uploadId)
                                        .partNumber(currentPartNumber).contentLength((long) dataLen).build(),
                                RequestBody.fromBytes(body));
                        return CompletedPart.builder().partNumber(currentPartNumber).eTag(response.eTag()).build();
                    } finally {
                        bufferPool.offer(data);
                    }
                }));
            } catch (final RejectedExecutionException e) {
                bufferPool.offer(data);
                throw new IOException(
                        "Upload executor rejected part " + currentPartNumber + " for " + key + ": executor shut down",
                        e);
            }
        }

        /**
         * Completes the multipart upload. Submits the final (partial) part, waits for all in-flight parts to finish,
         * then issues CompleteMultipartUpload. On any error, aborts the upload before rethrowing.
         */
        @Override
        public void close() throws IOException {
            if (uploadClosed) {
                return;
            }
            uploadClosed = true;
            try {
                // Submit the last (possibly partial) part, then stop accepting new tasks
                submitCurrentPart();
                uploadExecutor.shutdown();
                // Collect completed parts in submission (part-number) order
                final List<CompletedPart> completedParts = new ArrayList<>(pendingParts.size());
                for (final var future : pendingParts) {
                    try {
                        completedParts.add(future.get());
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted waiting for part upload for " + key, e);
                    } catch (final ExecutionException e) {
                        final var cause = e.getCause();
                        if (cause instanceof final S3Exception s3e) {
                            throw new IOException("Uploading part for " + key + ": " + formatS3Exception(s3e));
                        }
                        throw new IOException("Uploading part for " + key + ": " + Format.getMessage(cause, "", 0));
                    }
                }
                s3.getS3Client().completeMultipartUpload(CompleteMultipartUploadRequest.builder().bucket(bucket)
                        .key(key).uploadId(uploadId)
                        .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build()).build());
            } catch (final IOException e) {
                abort();
                throw e;
            } catch (final S3Exception e) {
                abort();
                throw new IOException("Completing multipart upload for " + key + ": " + formatS3Exception(e));
            } catch (final Exception e) {
                abort();
                throw new IOException("Completing multipart upload for " + key + ": " + Format.getMessage(e, "", 0));
            }
        }

        /**
         * Cancels pending part uploads and aborts the in-progress multipart upload on S3.
         */
        void abort() {
            uploadExecutor.shutdownNow();
            for (final var future : pendingParts) {
                future.cancel(true);
            }
            try {
                s3.getS3Client().abortMultipartUpload(
                        AbortMultipartUploadRequest.builder().bucket(bucket).key(key).uploadId(uploadId).build());
            } catch (final Exception e) {
                _log.warn("Failed to abort multipart upload for {}/{}: {}", bucket, key, Format.getMessage(e, "", 0));
            }
        }
    }

    /**
     * Formats a S3Exception into a single-line string with the key AWS diagnostic fields.
     *
     * @param e
     *            the S3Exception
     *
     * @return formatted string
     */
    public static String formatS3Exception(final S3Exception e) {
        final var details = e.awsErrorDetails();
        final var sb = new StringBuilder();
        sb.append("HTTPStatus=").append(e.statusCode()).append(", ");
        sb.append("AWSCode=").append(details != null ? details.errorCode() : "unknown").append(", ");
        sb.append("Message=").append(details != null ? details.errorMessage() : e.getMessage()).append(", ");
        sb.append("RequestId=").append(e.requestId());
        final var cause = e.getCause();
        if (cause != null) {
            sb.append(", Cause=").append(cause.getClass().getSimpleName()).append(":").append(cause.getMessage());
        }
        return sb.toString();
    }
}
