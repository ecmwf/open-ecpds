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
 * @author Cristina-Iulia Bucur <cristina-iulia.bucur@ecmwf.int>, ECMWF.
 * @version 6.7.9
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SOCKET_STATISTICS;
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
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_ALLOW_EMPTY_BUCKET_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_BUCKET_LOCATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_BUCKET_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_CLIENT_EMAIL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_CLIENT_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_FTPGROUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_FTPUSER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_MK_BUCKET;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_PREFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_PRIVATE_KEY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_PRIVATE_KEY_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_PROJECT_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_PROTOCOL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_SCHEME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_SSL_VALIDATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_CHUNK_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_GCS_URL;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.http.HttpTransportOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SSLClientSocketFactory;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class GCSModule.
 */
public final class GcsModule extends TransferModule {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(GcsModule.class);

    // GCS chunk size needs to be between 256KB and 2GB, default 5MB
    // capping the max chunk size to 50MB to prevent "Out of memory..." exceptions
    private static final long MIN_CHUNK = 256L * 1024; // 256 KB
    private static final long MAX_CHUNK = 50L * 1024 * 1024; // 50 MB

    /** The status. */
    private String currentStatus = "INIT";

    /** The gcs input. */
    private InputStream gcsInput;

    /** The scheme. */
    private String scheme = "http";

    /** The prefix. */
    private String gcsprefix = "";

    /** The project id. */
    private String projectId = null;

    /** The gcs storage. */
    private Storage gcs = null;

    /** The setup. */
    private ECtransSetup currentSetup = null;

    /** The bucket name. */
    private String bucketName = null;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The socket factory. */
    private SSLClientSocketFactory socketFactory;

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
        return setup.getInteger(HOST_GCS_PORT);
    }

    /**
     * Update socket statistics.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void updateSocketStatistics() throws IOException {
        if (socketFactory != null) {
            socketFactory.updateStatistics();
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
        // user: client_id, 21-digit numeric identifier
        // password: private_key_id, 40-character alphanumeric string
        // host: an IP address
        // bucketName: optional
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

        scheme = setup.getString(HOST_GCS_SCHEME);
        bucketName = setup.getString(HOST_GCS_BUCKET_NAME);
        gcsprefix = setup.getString(HOST_GCS_PREFIX).trim();

        if (isNotEmpty(gcsprefix) && !gcsprefix.endsWith("/")) {
            gcsprefix += "/";
        }

        final var url = setup.get(HOST_GCS_URL, scheme + "://" + host + (port != 80 ? ":" + port : ""));
        _log.debug("GCS connection on {} ({})", url, user);

        setAttribute("remote.hostName", host);

        /* add socket statistics */
        final ClientSocketStatistics statistics;
        if (setup.getBoolean(HOST_ECTRANS_SOCKET_STATISTICS)) {
            statistics = new ClientSocketStatistics();
            setAttribute(statistics);
        } else {
            statistics = null;
        }
        final var socketConfig = new SocketConfig(statistics, "GCSSocketConfig");
        socketConfig.setDebug(getDebug());
        setup.getOptionalBoolean(HOST_ECTRANS_TCP_NO_DELAY).ifPresent(socketConfig::setTcpNoDelay);
        setup.getOptionalBoolean(HOST_ECTRANS_TCP_KEEP_ALIVE).ifPresent(socketConfig::setKeepAlive);
        setup.getOptionalBoolean(HOST_ECTRANS_TCP_TIME_STAMP).ifPresent(socketConfig::setTCPTimeStamp);
        setup.getOptionalBoolean(HOST_ECTRANS_TCP_QUICK_ACK).ifPresent(socketConfig::setTCPQuickAck);
        setup.getOptionalString(HOST_ECTRANS_TCP_CONGESTION_CONTROL).ifPresent(socketConfig::setTCPCongestion);
        setup.getOptionalInteger(HOST_ECTRANS_TCP_MAX_SEGMENT).ifPresent(socketConfig::setTCPMaxSegment);
        setup.getOptionalInteger(HOST_ECTRANS_TCP_WINDOW_CLAMP).ifPresent(socketConfig::setTCPWindowClamp);
        setup.getOptionalInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_TIME).ifPresent(socketConfig::setTCPKeepAliveTime);
        setup.getOptionalInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL).ifPresent(socketConfig::setTCPKeepAliveInterval);
        setup.getOptionalInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES).ifPresent(socketConfig::setTCPKeepAliveProbes);
        setup.getOptionalInteger(HOST_ECTRANS_TCP_USER_TIMEOUT).ifPresent(socketConfig::setTCPUserTimeout);
        setup.getOptionalBoolean(HOST_ECTRANS_TCP_LINGER_ENABLE).ifPresent(enable -> {
            setup.getOptionalInteger(HOST_ECTRANS_TCP_LINGER_TIME).ifPresent(time -> {
                socketConfig.setTCPLinger(enable, time);
            });
        });

        var connected = false;

        ServiceAccountCredentials credentials = null;

        projectId = setup.getString(HOST_GCS_PROJECT_ID);
        final var clientId = isNotEmpty(user) ? user : setup.getString(HOST_GCS_CLIENT_ID);
        final var privateKeyId = isNotEmpty(password) ? password : setup.getString(HOST_GCS_PRIVATE_KEY_ID);
        final var privateKey = setup.getString(HOST_GCS_PRIVATE_KEY); // RSA private key in PKCS8 format
        final var clientEmail = setup.getString(HOST_GCS_CLIENT_EMAIL);

        try {
            credentials = ServiceAccountCredentials.newBuilder().setClientId(clientId).setClientEmail(clientEmail)
                    .setPrivateKeyString(privateKey).setPrivateKeyId(privateKeyId).setProjectId(projectId).build();

            socketFactory = socketConfig.getSSLSocketFactory(setup.getString(HOST_GCS_PROTOCOL),
                    setup.getBoolean(HOST_GCS_SSL_VALIDATION));

            // Create the custom HTTP transport with the custom SSLSocketFactory
            final HttpTransport httpTransport = new NetHttpTransport.Builder().setSslSocketFactory(socketFactory)
                    .build();

            gcs = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(credentials)
                    .setTransportOptions(
                            HttpTransportOptions.newBuilder().setHttpTransportFactory(() -> httpTransport).build())
                    .build().getService();

            if (isNotEmpty(bucketName) && setup.getBoolean(HOST_GCS_MK_BUCKET)) {
                // The user has configured a Bucket Name!
                var bucketFound = false;

                if (gcs.get(bucketName) != null) {
                    bucketFound = true;
                }

                if (!bucketFound) {
                    final var region = setup.getString(HOST_GCS_BUCKET_LOCATION);
                    // gcs.create(BucketInfo.of(bucketName));
                    if (isNotEmpty(region)) {
                        gcs.create(BucketInfo.newBuilder(bucketName).setLocation(region).build());
                    } else {
                        gcs.create(BucketInfo.newBuilder(bucketName).build());
                    }

                }
            }

            connected = true;
        } catch (final Throwable t) {
            _log.error("Connection failed to {}", url, t);
            throw new IOException("Connection failed to " + url + ": " + Format.getMessage(t, "", 0));
        } finally {
            if (!connected && gcs != null) {
                try {
                    gcs.close();
                } catch (final Throwable t) {
                    // Ignore!
                }
            }
        }
    }

    /**
     * Gets the bucket name and filename (object with full "path" in bucket). Return {"bucketName", "ObjectName"},
     * similar to S3
     *
     * @param name
     *            the name
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String[] getBucketNameAndObjectName(final String name) throws IOException {
        final var token = new StringTokenizer(name == null ? "" : name.replace('\\', '/'), "/");
        final var count = token.countTokens();
        final String[] result;
        if (isNotEmpty(bucketName)) {
            // The bucket name is defined in the setup, not the path!
            if (count == 0) {
                throw new IOException("No object name specified (filename)");
            } else {
                final var fileName = token.nextToken("\0");
                result = new String[] { bucketName, gcsprefix + fileName };
            }
        } else {
            if (getSetup().getBoolean(HOST_GCS_ALLOW_EMPTY_BUCKET_NAME)) {
                result = new String[] { "", gcsprefix + token.nextToken("\0") };
            } else {
                // The name should be in the format bucketName/filename.
                if (count == 0) {
                    throw new IOException("No Bucket specified");
                } else if (count == 1) {
                    throw new IOException("No object specified (filename)");
                } else {
                    final var extractedBucketName = token.nextToken();
                    // final String blobName = token.nextToken("\0").substring(1);
                    final var fileName = token.nextToken("\0");
                    result = new String[] { extractedBucketName, gcsprefix + fileName };
                }
            }
        }
        result[1] = processKey(result[1]);
        if (getDebug()) {
            _log.debug("BucketName: {}, BlobName: {}", result[0], result[1]);
        }
        return result;
    }

    /**
     * Process key. Make sure the filename does not have a starting '/'.
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
     * Del. If file does not exist, command still successful
     *
     * @param name
     *            the name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void del(final String name) throws IOException { // name can be a folder name, prefix or object name
        _log.debug("Del file {}", name);
        setStatus("DEL");

        _log.debug("Using GCS delete");
        final var bucketNameAndObject = getBucketNameAndObjectName(name);
        try {
            if (!(gcs.delete(bucketNameAndObject[0], bucketNameAndObject[1]))) {
                _log.warn("Object {} not found for deletion", name);
            }

        } catch (final Exception e) {
            _log.debug("deleteObject", e);
            throw new IOException("Deleting object " + name + ": " + Format.getMessage(e, "", 0));
        }
    }

    /**
     * CHecks if a chunk size was set, adjusts it to be a multiple of 256KB (as per GCS requirements) and checks if this
     * size is a valid one (between 5MB and 5GB).
     *
     * @return int Size of the chunk to be used in bytes and 0 if the default GCS chunk size (5MB) should be used.
     *
     * @throws IllegalArgumentException
     *             Signals that an invalid chunk size was provided.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private int getValidatedChunkSize() throws IllegalArgumentException, IOException {
        return getSetup().getOptionalByteSize(HOST_GCS_CHUNK_SIZE).map(chunkSize -> {
            long adjustedChunk = Math.max(256 * 1024, (chunkSize.size() / (256 * 1024)) * (256 * 1024));
            if (adjustedChunk < MIN_CHUNK || adjustedChunk > MAX_CHUNK) {
                throw new IllegalArgumentException(HOST_GCS_CHUNK_SIZE.getFullName() + " Invalid GCS chunk size: "
                        + adjustedChunk + ". Must be between 256KB and 50MB, and a multiple of 256KB.");
            }
            if (adjustedChunk != chunkSize.size()) {
                _log.debug("Provided chunk size adjusted ({} -> {})", chunkSize.size(), adjustedChunk);
            }
            return (int) adjustedChunk;
        }).orElse(0); // 0 means “use default GCS chunk size”
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
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        final var bucketNameAndObject = getBucketNameAndObjectName(name);
        try {
            final var objectInfo = BlobInfo.newBuilder(BlobId.of(bucketNameAndObject[0], bucketNameAndObject[1]))
                    .build();
            // check (and use) if a different chunk size was set, GCS default 5MB
            int chunkSize = getValidatedChunkSize();
            if (chunkSize > 0) {
                _log.debug("Using GCS upload with chunk size={} bytes", chunkSize);
                gcs.createFrom(objectInfo, in, chunkSize);
            } else {
                _log.debug("Using GCS upload with default chunk size.");
                gcs.createFrom(objectInfo, in);
            }
        } catch (final IllegalArgumentException e) {
            throw new IOException("Pushing object " + name + ": " + e.getMessage());
        } catch (final Exception e) {
            _log.debug("putObject", e);
            throw new IOException("Pushing object " + name + ": " + Format.getMessage(e, "", 0));
        }
        return true;
    }

    /**
     * Put.
     *
     * @param name
     *            the name
     * @param posn
     *            the
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
        _log.warn("Fake put of: {} (posn={})", name, posn);
        setStatus("PUT");
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        _log.debug("Using GCS put");
        final var bucketNameAndObject = getBucketNameAndObjectName(name);
        try {
            final var objectInfo = BlobInfo.newBuilder(BlobId.of(bucketNameAndObject[0], bucketNameAndObject[1]))
                    .build();
            WriteChannel writer = gcs.writer(objectInfo);
            // check (and use) if a different chunk size was set, GCS default 5MB
            int chunkSize = getValidatedChunkSize();
            if (chunkSize > 0) {
                _log.debug("Using GCS upload with chunk size={} bytes", chunkSize);
                writer.setChunkSize(chunkSize);
            }
            return Channels.newOutputStream(writer);
        } catch (final IllegalArgumentException e) {
            throw new IOException("Pushing object " + name + ": " + e.getMessage());
        } catch (final Exception e) {
            _log.debug("putObject", e);
            throw new IOException("Pushing object " + name + ": " + Format.getMessage(e, "", 0));
        }
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
        _log.debug("Get file " + name);
        setStatus("GET");
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        _log.debug("Using GCS get");
        final var bucketNameAndObject = getBucketNameAndObjectName(name);
        try {
            return Channels.newInputStream(gcs.reader(bucketNameAndObject[0], bucketNameAndObject[1]));
        } catch (final Exception e) {
            _log.debug("getObject", e);
            throw new IOException("Getting object " + name + ": " + Format.getMessage(e, "", 0));
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
        _log.debug("Using GCS size");
        final var bucketNameAndObject = getBucketNameAndObjectName(name);
        try {
            // content length of the data in bytes
            return gcs.get(bucketNameAndObject[0], bucketNameAndObject[1],
                    Storage.BlobGetOption.fields(Storage.BlobField.values())).getSize();
        } catch (final Exception e) {
            _log.debug("getObject", e);
            throw new IOException("Getting object " + name + ": " + Format.getMessage(e));
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
        } else if (count == 1) {
            try {
                final var newBucketName = token.nextToken();
                final var region = currentSetup.getString(HOST_GCS_BUCKET_LOCATION);
                // gcs.create(BucketInfo.newBuilder(token.nextToken()).build());
                // gcs.create(BucketInfo.of(token.nextToken())); // alternative
                if (isNotEmpty(region)) {
                    gcs.create(BucketInfo.newBuilder(newBucketName).setLocation(region).build());
                } else {
                    gcs.create(BucketInfo.newBuilder(newBucketName).build());
                }

            } catch (final Exception e) {
                _log.debug("createBucket", e);
                throw new IOException("Creating Bucket " + directory + ": " + Format.getMessage(e, "", 0));
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
        } else if (count == 1) {
            try {
                gcs.get(token.nextToken()).delete();
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
        public void add(String line) throws IOException;
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
            } else {
                if (count == 0) {
                    // We have to display the list of Buckets!
                    listBuckets(output, null, pattern);
                } else if (count == 1) {
                    final var extractedBucketName = token.nextToken();
                    if (extractedBucketName.contains("*") || extractedBucketName.contains("?")) {
                        // the name contains wildcards so we will show the list!
                        listBuckets(output, bucketName, pattern);
                    } else {
                        // We are going to list the specified Bucket!
                        listObjects(output, extractedBucketName, null, pattern);
                    }
                } else {
                    // We have a file name specified!
                    final var extractedBucketName = token.nextToken();
                    listObjects(output, extractedBucketName, processKey(token.nextToken("\0")), pattern);
                }
            }
        } catch (final StorageException e) {
            _log.debug("list", e);
            throw new IOException("Listing " + directory + ": " + e.getCode() + " <- " + Format.getMessage(e, "", 0));

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
        _log.debug("listAsStringArray: {},{}", directory, pattern);
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
        final var buckets = gcs.list();
        WildcardFileFilter fileFilter = null;
        if (bucketName != null) {
            fileFilter = WildcardFileFilter.builder().setWildcards(bucketName).get();
        }
        for (final Bucket bucket : buckets.iterateAll()) {
            if (bucket != null) {
                final var owner = bucket.getOwner();
                final var ownerName = owner != null ? owner.toString() : "unknown";
                final var name = bucket.getName();
                if ((pattern == null || name.matches(pattern))
                        && (fileFilter == null || fileFilter.accept(new File(name)))) {
                    output.add(Format.getFtpList("drw-r--r--", getSetup().get(HOST_GCS_FTPUSER, ownerName),
                            getSetup().get(HOST_GCS_FTPGROUP, ownerName), "2048",
                            bucket.getCreateTimeOffsetDateTime() != null
                                    ? (bucket.getCreateTimeOffsetDateTime().toEpochSecond() * 1000)
                                    : new Date().getTime(),
                            name));
                }
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
        if (getDebug()) {
            _log.debug("listObjects: {},{},{}", bucketName, fileName, pattern);
        }

        final var prefix = gcsprefix + (fileName != null ? fileName : "");

        if (isNotEmpty(prefix)) { // prefix cannot be null, but it can be blank
            if (getDebug()) {
                _log.debug("Using prefix: {}", prefix);
            }
        }

        // listing current level directories and files
        final var blobs = gcs
                .list(bucketName, BlobListOption.currentDirectory(),
                        Storage.BlobListOption
                                .prefix(prefix.isBlank() ? "" : (prefix.endsWith("/") ? prefix : prefix + "/")))
                .iterateAll();

        for (final Blob blob : blobs) {
            var name = blob.getName();
            if (getDebug()) {
                _log.debug("Processing prefix+filename: {}", name);
            }
            if (isNotEmpty(prefix)) {
                // one of the entries will be an empty one; e.g. prefix = "folder_1/" => name=""
                name = name.substring(prefix.length());
                if (getDebug()) {
                    _log.debug("Name: {} ({})", name, pattern);
                }
            }

            if (pattern == null || name.matches(pattern)) {
                // blob.isDirectory() considers root folder as file when a prefix is given
                final var isDirectory = name.endsWith("/");
                final var owner = blob.getOwner();
                final var ownerName = owner != null ? owner.toString() : "unknown";

                if (!name.isBlank()) {

                    final var entry = Format.getFtpList((isDirectory ? "d" : "-") + "rw-r--r--",
                            getSetup().get(HOST_GCS_FTPUSER, ownerName), getSetup().get(HOST_GCS_FTPGROUP, ownerName),
                            String.valueOf(blob.getSize()),
                            blob.getUpdateTimeOffsetDateTime() != null
                                    ? (blob.getUpdateTimeOffsetDateTime().toEpochSecond() * 1000)
                                    : new Date().getTime(),
                            (isDirectory ? name.substring(0, name.length() - 1) : name));
                    output.add(entry);
                    if (getDebug()) {
                        _log.debug("Adding entry: {}", entry);
                    }
                }
            }
        }
    }

    /**
     * Gets the setup. Utility call to get the ECtransSetup and check if the module is not closed!
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
            StreamPlugThread.closeQuietly(gcsInput);
            if (gcs != null) {
                try {
                    gcs.close();
                } catch (final Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
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
}
