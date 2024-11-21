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
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_DISABLE_CHUNKED_ENCODING;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_DUALSTACK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_ENABLE_MARK_AND_RESET;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_ENABLE_PATH_STYLE_ACCESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_FORCE_GLOBAL_BUCKET_ACCESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_FTPGROUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_FTPUSER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_LISTEN_ADDRESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_MK_BUCKET;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_MULTIPART_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_NUM_UPLOAD_THREADS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_PART_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_PREFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_PROTOCOL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_QUEUE_CAPACITY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_RECURSIVE_LEVEL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_S3_REGION;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Request;
import com.amazonaws.RequestClientOptions;
import com.amazonaws.Response;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.SkipMd5CheckStrategy;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.thirdparty.apache.http.Header;
import com.amazonaws.thirdparty.apache.http.conn.ssl.NoopHostnameVerifier;
import com.amazonaws.thirdparty.apache.http.conn.ssl.SSLConnectionSocketFactory;
import com.amazonaws.util.IOUtils;

import alex.mojaki.s3upload.StreamTransferManager;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SSLClientSocketFactory;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.Cnf;
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

    /** The scheme. */
    private String scheme = "http";

    /** The prefix. */
    private String s3prefix = "";

    /** The s3. */
    private Session s3 = null;

    /** The setup. */
    private ECtransSetup currentSetup = null;

    /** The bucket name. */
    private String bucketName = null;

    /** The numUploadThreads. */
    private int numUploadThreads = 2;

    /** The queueCapacity. */
    private int queueCapacity = 2;

    /** The partSize. */
    private int partSize = 10;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Allow skipping the MD5 check for GET and PUT.
     **/
    static {
        if (Cnf.at("AmazonS3", "skipMd5CheckStrategy", false)) {
            System.setProperty(SkipMd5CheckStrategy.DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY, "true");
            System.setProperty(SkipMd5CheckStrategy.DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY, "true");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() {
        return currentStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort(final ECtransSetup setup) {
        return setup.getInteger(HOST_S3_PORT);
    }

    /**
     * {@inheritDoc}
     *
     * Update socket statistics.
     */
    @Override
    public void updateSocketStatistics() throws IOException {
        if (s3 != null) {
            s3.updateStatistics();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
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
        scheme = setup.getString(HOST_S3_SCHEME);
        bucketName = setup.getString(HOST_S3_BUCKET_NAME);
        s3prefix = setup.getString(HOST_S3_PREFIX).trim();
        numUploadThreads = setup.getInteger(HOST_S3_NUM_UPLOAD_THREADS);
        queueCapacity = setup.getInteger(HOST_S3_QUEUE_CAPACITY);
        partSize = setup.getInteger(HOST_S3_PART_SIZE);
        if (isNotEmpty(s3prefix) && !s3prefix.endsWith("/")) {
            s3prefix += "/";
        }
        final var region = setup.getString(HOST_S3_REGION);
        final var url = setup.get(HOST_S3_URL, scheme + "://" + host + (port != 80 ? ":" + port : ""));
        _log.debug("AmazonS3 connection on {} ({})", url, user);
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
                    setup.getBoolean(HOST_S3_DUALSTACK), setup.getBoolean(HOST_S3_FORCE_GLOBAL_BUCKET_ACCESS),
                    setup.getBoolean(HOST_S3_DISABLE_CHUNKED_ENCODING),
                    setup.getBoolean(HOST_S3_ENABLE_PATH_STYLE_ACCESS), bucketName, url,
                    isNotEmpty(region) ? region : Regions.DEFAULT_REGION.getName(), setup.getBoolean(HOST_S3_MK_BUCKET),
                    setup.getString(HOST_S3_ROLE_ARN), setup.getString(HOST_S3_ROLE_SESSION_NAME), getDebug());
            connected = true;
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
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final String name) throws IOException {
        _log.debug("Del file {}", name);
        setStatus("DEL");
        final var bnk = getBucketNameAndKey(name);
        try {
            s3.getAmazonS3().deleteObject(bnk[0], bnk[1]);
        } catch (final Exception e) {
            _log.debug("deleteObject", e);
            throw new IOException("Deleting object " + name + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Put.
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
            // The size of the file is not know beforehand or we are instructed to perform a
            // multi-part transfer!
            _log.debug("Using StreamTransferManager");
            final var manager = new StreamTransferManager(bnk[0], bnk[1], s3.getAmazonS3()).numStreams(1)
                    .numUploadThreads(numUploadThreads).queueCapacity(queueCapacity).partSize(partSize);
            try {
                final OutputStream out = manager.getMultiPartOutputStreams().get(0);
                StreamPlugThread.copy(out, in, StreamPlugThread.DEFAULT_BUFF_SIZE);
                out.close();
                manager.complete();
            } catch (final Throwable t) {
                manager.abort();
            }
        } else {
            // We know the file size so we can use the default mechanism!
            try {
                final var metadata = new ObjectMetadata();
                metadata.setContentLength(size);
                final InputStream input;
                input = getSetup().getBoolean(HOST_S3_USE_BYTE_ARRAY_INPUT_STREAM)
                        && size < getSetup().getLong(HOST_S3_SINGLEPART_SIZE)
                                ? new ByteArrayInputStream(IOUtils.toByteArray(in)) : in;
                final var request = new PutObjectRequest(bnk[0], bnk[1], input, metadata);
                if (getSetup().getBoolean(HOST_S3_ENABLE_MARK_AND_RESET)) {
                    request.getRequestClientOptions().setReadLimit(RequestClientOptions.DEFAULT_STREAM_BUFFER_SIZE);
                }
                s3.getAmazonS3().putObject(request);
            } catch (final Exception e) {
                _log.debug("putObject", e);
                throw new IOException("Pushing object " + name + ": " + Format.getMessage(e, "", 0));
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        _log.debug("Put file {} ({})", name, posn);
        setStatus("PUT");
        final var bnk = getBucketNameAndKey(name);
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        _log.debug("Using StreamTransferManager");
        final var manager = new StreamTransferManager(bnk[0], bnk[1], s3.getAmazonS3()).numStreams(1)
                .numUploadThreads(numUploadThreads).queueCapacity(queueCapacity).partSize(partSize);
        // Let's implement the various write methods for better performances!
        return new FilterOutputStream(manager.getMultiPartOutputStreams().get(0)) {
            @Override
            public void write(final int b) throws IOException {
                out.write(b);
            }

            @Override
            public void write(final byte[] b) throws IOException {
                out.write(b);
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                out.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                var complete = false;
                try {
                    super.close();
                    complete = true;
                } finally {
                    if (complete) {
                        manager.complete();
                    } else {
                        manager.abort();
                    }
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
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
            final var s3Object = s3.getAmazonS3().getObject(new GetObjectRequest(bnk[0], bnk[1]));
            s3input = new FilterInputStream(s3Object.getObjectContent()) {
                @Override
                public void close() throws IOException {
                    s3Object.close();
                }
            };
            return s3input;
        } catch (final Exception e) {
            _log.debug("getObject", e);
            throw new IOException("Getting object " + name + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String name) throws IOException {
        _log.debug("Size {}", name);
        setStatus("SIZE");
        final var bnk = getBucketNameAndKey(name);
        try {
            final var s3Object = s3.getAmazonS3().getObject(new GetObjectRequest(bnk[0], bnk[1]));
            try (s3Object) {
                return s3Object.getObjectMetadata().getContentLength();
            }
        } catch (final Exception e) {
            _log.debug("getObject", e);
            throw new IOException("Getting object " + name + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Mkdir.
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
            try {
                s3.getAmazonS3().createBucket(token.nextToken());
            } catch (final Exception e) {
                _log.debug("createBucket", e);
                throw new IOException("Creating Bucket " + directory + ": " + Format.getMessage(e, "", 0));
            }
        } else {
            throw new IOException("Subdirectories not allowed in Bucket");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Rmdir.
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
            try {
                s3.getAmazonS3().deleteBucket(token.nextToken());
            } catch (final Exception e) {
                _log.debug("deleteBucket", e);
                throw new IOException("Deleting Bucket " + directory + ": " + Format.getMessage(e, "", 0));
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
        } catch (final AmazonS3Exception e) {
            _log.debug("list", e);
            throw new IOException(
                    "Listing " + directory + ": " + e.getErrorResponseXml() + " <- " + Format.getMessage(e, "", 0));
        } catch (final Exception e) {
            _log.debug("list", e);
            throw new IOException("Listing " + directory + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * {@inheritDoc}
     *
     * List as string array.
     */
    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        _log.debug("listAsStringArray: {},{}", directory, pattern);
        setStatus("LIST");
        final List<String> list = new ArrayList<>();
        list(list::add, directory, pattern);
        return list.toArray(new String[list.size()]);
    }

    /**
     * {@inheritDoc}
     *
     * List as byte array.
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
        for (final Bucket bucket : s3.getAmazonS3().listBuckets()) {
            final var owner = bucket.getOwner();
            final var ownerName = owner != null ? owner.getDisplayName() : "unknown";
            final var name = bucket.getName();
            if ((pattern == null || name.matches(pattern))
                    && (bucketName == null || new WildcardFileFilter(bucketName).accept(new File(name)))) {
                output.add(Format.getFtpList("drw-r--r--", getSetup().get(HOST_S3_FTPUSER, ownerName),
                        getSetup().get(HOST_S3_FTPGROUP, ownerName), "2048", bucket.getCreationDate().getTime(), name));
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
        final var request = new ListObjectsRequest();
        if (isNotEmpty(bucketName)) {
            request.withBucketName(bucketName);
        }
        final var prefix = s3prefix + (fileName != null ? fileName : "");
        if (isNotEmpty(prefix)) {
            request.withPrefix(prefix);
            if (getDebug()) {
                _log.debug("Using prefix: {}", prefix);
            }
        }
        var objectListing = s3.getAmazonS3().listObjects(request);
        final List<S3ObjectSummary> keyList = new ArrayList<>(objectListing.getObjectSummaries());
        while (objectListing.isTruncated()) {
            objectListing = s3.getAmazonS3().listNextBatchOfObjects(objectListing);
            keyList.addAll(objectListing.getObjectSummaries());
        }
        for (final S3ObjectSummary objectSummary : keyList) {
            var name = objectSummary.getKey();
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
                if (level > 0 && isDirectory) { // This is a directory and we are required to go one level up!
                    listObjects(output, bucketName, rootName + name, fileName + name, pattern, level - 1);
                } else { // This is a file!
                    final var displayName = (!rootName.isBlank() ? rootName : "") + name;
                    if (pattern == null || displayName.matches(pattern)) {
                        final var owner = objectSummary.getOwner();
                        final var ownerName = owner != null ? owner.getDisplayName() : "unknown";
                        final var date = objectSummary.getLastModified();
                        final var entry = Format.getFtpList((isDirectory ? "d" : "-") + "rw-r--r--",
                                getSetup().get(HOST_S3_FTPUSER, ownerName), getSetup().get(HOST_S3_FTPGROUP, ownerName),
                                String.valueOf(objectSummary.getSize()),
                                date != null ? date.getTime() : new Date().getTime(), displayName);
                        output.add(entry);
                        if (getDebug()) {
                            _log.debug("Adding entry: {}", entry);
                        }
                    }
                }
            }
        }
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
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            _log.debug("Close connection");
            currentStatus = "CLOSE";
            StreamPlugThread.closeQuietly(s3input);
            if (s3 != null)
                s3.getAmazonS3().shutdown();
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
     * The Class Session.
     */
    private static class Session {

        /** The amazon S 3. */
        private final AmazonS3 amazonS3;

        /** The socket factory. */
        final SSLClientSocketFactory socketFactory;

        /**
         * Instantiates a new session.
         *
         * @param s3
         *            the s 3
         * @param clientSocketFactory
         *            the client socket factory
         */
        Session(final AmazonS3 s3, final SSLClientSocketFactory clientSocketFactory) {
            amazonS3 = s3;
            socketFactory = clientSocketFactory;
        }

        /**
         * Gets the Session.
         *
         * @param user
         *            the user
         * @param password
         *            the password
         * @param listenAddress
         *            the listen address
         * @param tcpConfig
         *            the tcp config
         * @param protocol
         *            the protocol
         * @param sslValidation
         *            the ssl validation
         * @param strict
         *            the strict
         * @param acceleration
         *            the acceleration
         * @param dualstack
         *            the dualstack
         * @param forceGlobalBucketAccess
         *            the force global bucket access
         * @param disableChunkedEncoding
         *            the disable chunked encoding
         * @param enablePathStyleAccess
         *            the enable path style access
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
                final boolean acceleration, final boolean dualstack, final boolean forceGlobalBucketAccess,
                final boolean disableChunkedEncoding, final boolean enablePathStyleAccess, final String bucketName,
                final String url, final String region, final boolean mkBucket, final String roleArn,
                final String roleSessionName, final boolean debug)
                throws NoSuchAlgorithmException, KeyManagementException {
            final var builder = AmazonS3ClientBuilder.standard();
            final var clientConfiguration = new ClientConfiguration();
            if (isNotEmpty(listenAddress)) {
                try {
                    clientConfiguration.setLocalAddress(InetAddress.getByName(listenAddress));
                } catch (final UnknownHostException ignored) {
                    _log.warn("Cannot set listen address: {}", listenAddress, ignored);
                }
            }
            final SSLClientSocketFactory clientSocketFactory;
            if (acceleration) {
                // Acceleration is not working with the tweaked SSL socket factory!
                clientSocketFactory = null;
            } else {
                clientSocketFactory = tcpConfig.getSSLSocketFactory(protocol, sslValidation);
                clientConfiguration.getApacheHttpClientConfig().withSslSocketFactory(new SSLConnectionSocketFactory(
                        clientSocketFactory, !strict ? NoopHostnameVerifier.INSTANCE : null));
            }
            builder.withCredentials(
                    loadCredentials(user, password, region, clientConfiguration, roleArn, roleSessionName))
                    .withClientConfiguration(clientConfiguration)
                    .withForceGlobalBucketAccessEnabled(forceGlobalBucketAccess).withAccelerateModeEnabled(acceleration)
                    .withDualstackEnabled(dualstack);
            if (acceleration || isEmpty(url)) {
                builder.withRegion(region);
            } else {
                builder.withEndpointConfiguration(new EndpointConfiguration(url, region));
            }
            if (disableChunkedEncoding) {
                builder.disableChunkedEncoding();
            }
            if (enablePathStyleAccess) {
                builder.enablePathStyleAccess();
            }
            if (debug) {
                builder.withRequestHandlers(new RequestHandler2() {
                    @Override
                    public void afterResponse(final Request<?> request, final Response<?> response) {
                        final var headers2 = response.getHttpResponse().getHttpRequest().getAllHeaders();
                        for (final Header header : headers2) {
                            _log.debug("Request Headers: {}:{}", header.getName(), header.getValue());
                        }
                        final var header1 = response.getHttpResponse().getAllHeaders();
                        for (final Map.Entry<String, List<String>> entry : header1.entrySet()) {
                            final var sb = new StringBuilder();
                            for (final String value : entry.getValue()) {
                                sb.append((sb.length() > 0 ? "," : "") + value);
                            }
                            _log.debug("Response Headers: {}:{}", entry.getKey(), sb);
                        }
                    }

                    @Override
                    public void beforeRequest(final Request<?> request) {
                        final var parameters = request.getParameters();
                        for (final Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                            final var sb = new StringBuilder();
                            for (final String key : entry.getValue()) {
                                sb.append((sb.length() > 0 ? "," : "") + key);
                            }
                            _log.debug("Request Parameters: {}:{}", entry.getKey(), sb);
                        }
                        final var headers = request.getHeaders();
                        for (final Map.Entry<String, String> entry : headers.entrySet()) {
                            _log.debug("Request Headers: {},{}", entry.getKey(), entry.getValue());
                        }
                    }
                });
            }
            var connected = false;
            AmazonS3 s3 = null;
            try {
                s3 = builder.build();
                if (isNotEmpty(bucketName) && mkBucket) {
                    // The user has configured a Bucket Name!
                    var bucketFound = false;
                    for (final Bucket bucket : s3.listBuckets()) {
                        if (bucketName.equals(bucket.getName())) {
                            bucketFound = true;
                            break;
                        }
                    }
                    if (!bucketFound) {
                        s3.createBucket(bucketName);
                    }
                }
                connected = true;
            } finally {
                if (!connected && s3 != null) {
                    try {
                        s3.shutdown();
                    } catch (final Throwable t) {
                        // Ignore!
                    }
                }
            }
            return new Session(s3, clientSocketFactory);
        }

        /**
         * Gets the amazon S 3.
         *
         * @return the amazon S 3
         */
        AmazonS3 getAmazonS3() {
            return amazonS3;
        }

        /**
         * Update statistics.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void updateStatistics() throws IOException {
            if (socketFactory != null)
                socketFactory.updateStatistics();
        }

        /**
         * Load credentials.
         *
         * @param accessKeyId
         *            the access key id
         * @param secretAccessKey
         *            the secret access key
         * @param region
         *            the region
         * @param clientConfiguration
         *            the client configuration
         * @param roleArn
         *            the role arn
         * @param roleSessionName
         *            the role session name
         *
         * @return the AWS credentials provider
         */
        private static AWSCredentialsProvider loadCredentials(final String accessKeyId, final String secretAccessKey,
                final String region, final ClientConfiguration clientConfiguration, final String roleArn,
                final String roleSessionName) {
            final var provider = new AWSStaticCredentialsProvider(accessKeyId != null && !accessKeyId.isBlank()
                    ? new BasicAWSCredentials(accessKeyId, secretAccessKey) : new AnonymousAWSCredentials());
            if (isNotEmpty(roleArn) && isNotEmpty(roleSessionName)) {
                final var builder = AWSSecurityTokenServiceAsyncClientBuilder.standard();
                final AWSSecurityTokenService stsClient = builder.withCredentials(provider)
                        .withClientConfiguration(clientConfiguration).withRegion(region).build();
                final var assumeRoleRequest = new AssumeRoleRequest().withDurationSeconds(3600).withRoleArn(roleArn)
                        .withRoleSessionName(roleSessionName);
                final var assumeRoleResult = stsClient.assumeRole(assumeRoleRequest);
                final var creds = assumeRoleResult.getCredentials();
                return new AWSStaticCredentialsProvider(new BasicSessionCredentials(creds.getAccessKeyId(),
                        creds.getSecretAccessKey(), creds.getSessionToken()));
            }
            return provider;
        }
    }
}
