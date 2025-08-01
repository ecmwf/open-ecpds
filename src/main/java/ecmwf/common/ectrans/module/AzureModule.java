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

import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_BLOCK_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_CONTAINER_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_FTPGROUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_FTPUSER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_IGNORE_CHECK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_IGNORE_DELETE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_MK_CONTAINER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_MULTIPART_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_NUM_BUFFERS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_OVERWRITE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_SAS_SUBSCRIPTION_KEY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_SAS_URL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_SCHEME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_URL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_USER_ASSIGNED_CLIENT_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_USE_MD5;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.FluxUtil;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.checksum.Checksum;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.Synchronized;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import reactor.core.publisher.Mono;

/**
 * The Class AzureModule.
 */
public final class AzureModule extends TransferModule {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(AzureModule.class);

    /** The current status. */
    private String currentStatus = "INIT";

    /** The azureInput. */
    private InputStream azureInput;

    /** The blob name. */
    private String containerName = null;

    /** The blob service. */
    private BlobServiceClientCache service = null;

    /** The current setup. */
    private ECtransSetup currentSetup = null;

    /** Remote name for checking the size at the end of the transmission *. */
    private String remoteName = null;

    /** Task for putting the file *. */
    private ConfigurableRunnable runnable = null;

    /** Checksum if required *. */
    private Checksum md5Checksum = null;

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
        return setup.getInteger(HOST_AZURE_PORT);
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
     * @throws URISyntaxException
     *             the URI syntax exception
     */
    @Override
    public void connect(final String location, final ECtransSetup setup) throws IOException, URISyntaxException {
        // The location is: user:password@host/containerName
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
            containerName = host.substring(pos + 1);
            host = host.substring(0, pos);
        }
        final var port = getPort(getSetup());
        final var scheme = getSetup().getString(HOST_AZURE_SCHEME);
        containerName = getSetup().getString(HOST_AZURE_CONTAINER_NAME);
        final var url = getSetup().get(HOST_AZURE_URL, scheme + "://" + host + (port != 80 ? ":" + port : ""));
        _log.debug("Azure connection on {} ({})", url, user);
        var connected = false;
        final var theUrl = new URI(url).toURL();
        setAttribute("remote.hostName", theUrl.getHost());
        try {
            service = BlobServiceClientCache.getBlobServiceClientCache(getSetup().getString(HOST_AZURE_SAS_URL),
                    getSetup().getString(HOST_AZURE_SAS_SUBSCRIPTION_KEY), user, password,
                    getSetup().getString(HOST_AZURE_USER_ASSIGNED_CLIENT_ID), url, containerName,
                    getSetup().getBoolean(HOST_AZURE_MK_CONTAINER));
            connected = true;
        } catch (final Throwable e) {
            _log.error("Connection failed to {}", url, e);
            final var message = e.getMessage();
            throw new IOException(isNotEmpty(message) ? message : "connection failed to " + url);
        } finally {
            if (!connected) {
                setStatus("ERROR");
            }
        }
    }

    /**
     * Gets the blob name and key. Return {"containerName", "blobName"}.
     *
     * @param name
     *            the name
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String[] getContainerNameAndBloblName(final String name) throws IOException {
        final var token = getPathTokenizer(name);
        final var count = token.countTokens();
        final String[] result;
        if (isNotEmpty(containerName)) {
            // The Container name is defined in the setup, not the path!
            if (count == 0) {
                throw new IOException("No Blob name specified (filename)");
            }
            final var fileName = token.nextToken("\0").substring(1);
            result = new String[] { containerName, fileName };
        } else // The name should be in the format containerName/blobName. We will ignore the
        // prefix in this case.
        if (count == 0) {
            throw new IOException("No Container specified");
        } else if (count == 1) {
            throw new IOException("No Blob specified (filename)");
        } else {
            final var container = token.nextToken();
            final var blob = token.nextToken("\0").substring(1);
            result = new String[] { container, blob };
        }
        _log.debug("ContainerName: {}, BlobName: {}", result[0], result[1]);
        return result;
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
        final var cnb = getContainerNameAndBloblName(name);
        try {
            service.getBlobContainerClient(cnb[0]).getBlobClient(cnb[1]).delete();
        } catch (final Exception e) {
            _log.debug("deleteBlob", e);
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Gets the MD 5 input stream. Utility to initialize the md5 object for a given input stream if required from the
     * configuration.
     *
     * @param in
     *            the in
     *
     * @return the MD 5 input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private InputStream getMD5InputStream(final InputStream in) throws IOException {
        if (!getSetup().getBoolean(HOST_AZURE_USE_MD5)) {
            return in;
        }
        try {
            return (md5Checksum = Checksum.getChecksum(Checksum.Algorithm.MD5, in)).getInputStream();
        } catch (final NoSuchAlgorithmException e) {
            throw new IOException("MD5 algorithm not found");
        }
    }

    /**
     * Gets the mark supported input stream. This make sure the input stream support mark and reset features.
     *
     * @param in
     *            the in
     *
     * @return the mark supported input stream
     */
    private static InputStream getMarkSupportedInputStream(final InputStream in) {
        if (in.markSupported()) {
            return in;
        }
        _log.debug("Setting up BufferedInputStream to support mark and reset");
        return new BufferedInputStream(in);
    }

    /**
     * Exists mono. Helper to avoid a null pointer.
     *
     * @param exists
     *            the exists
     *
     * @return true, if successful
     */
    private static boolean existsMono(final Mono<Boolean> exists) {
        if (exists != null) {
            final var block = exists.block();
            return block != null && block;
        }
        return false;
    }

    /**
     * Exists boolean. Helper to avoid a null pointer.
     *
     * @param exists
     *            the exists
     *
     * @return true, if successful
     */
    private static boolean existsBoolean(final Boolean exists) {
        if (exists != null) {
            return exists;
        }
        return false;
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
        final var cnb = getContainerNameAndBloblName(name);
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        remoteName = name;
        if (size < 0 || size >= getSetup().getByteSize(HOST_AZURE_MULTIPART_SIZE).size()) {
            // The size of the file is not know beforehand or we are instructed to perform a
            // multi-part transfer!
            _log.debug("Using async service with parallel transfers");
            try {
                final var containerClient = service.buildAsyncClient().getBlobContainerAsyncClient(cnb[0]);
                // If the container does not exists do we have to create it ?
                if (getSetup().getBoolean(HOST_AZURE_MK_CONTAINER) && !existsMono(containerClient.exists())) {
                    if (getDebug()) {
                        _log.debug("Creating container");
                    }
                    containerClient.create().block();
                }
                final var blobClient = containerClient.getBlobAsyncClient(cnb[1]);
                // If a blob with the same name already exists do we have to delete it ?
                if (!getSetup().getBoolean(HOST_AZURE_IGNORE_DELETE) && existsMono(blobClient.exists())) {
                    if (getDebug()) {
                        _log.debug("Deleting original blob");
                    }
                    blobClient.delete().block();
                }
                final var dataFlux = FluxUtil.toFluxByteBuffer(getMarkSupportedInputStream(getMD5InputStream(in)));
                BlobRequestConditions requestConditions = null;
                if (!getSetup().getBoolean(HOST_AZURE_OVERWRITE)) {
                    // Only upload if the blob does NOT already exist
                    requestConditions = new BlobRequestConditions().setIfNoneMatch("*");
                }
                final var parallelOptions = new ParallelTransferOptions()
                        .setBlockSizeLong(getSetup().getByteSize(HOST_AZURE_BLOCK_SIZE).size()) // block size in bytes
                        .setMaxConcurrency(getSetup().getInteger(HOST_AZURE_NUM_BUFFERS)); // number of parallel buffers
                final var uploadOptions = new BlobParallelUploadOptions(dataFlux)
                        .setParallelTransferOptions(parallelOptions).setRequestConditions(requestConditions);
                // then call upload with this options object
                blobClient.uploadWithResponse(uploadOptions).block();
            } catch (final Exception e) {
                _log.debug("upload", e);
                throw new IOException(e.getMessage());
            }
        } else {
            // Standard synchronous upload
            _log.debug("Using standard service");
            try {
                final var containerClient = service.getBlobContainerClient(cnb[0]);
                // If the container does not exists do we have to create it ?
                if (getSetup().getBoolean(HOST_AZURE_MK_CONTAINER) && !containerClient.exists()) {
                    if (getDebug()) {
                        _log.debug("Creating container");
                    }
                    containerClient.create();
                }
                final var blobClient = containerClient.getBlobClient(cnb[1]).getBlockBlobClient();
                // If a blob with the same name already exists do we have to delete it ?
                if (!getSetup().getBoolean(HOST_AZURE_IGNORE_DELETE) && existsBoolean(blobClient.exists())) {
                    if (getDebug()) {
                        _log.debug("Deleting original blob");
                    }
                    blobClient.delete();
                }
                blobClient.upload(getMarkSupportedInputStream(getMD5InputStream(in)), size,
                        getSetup().getBoolean(HOST_AZURE_OVERWRITE));
            } catch (final Exception e) {
                _log.debug("upload", e);
                throw new IOException(e.getMessage());
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
        final var cnb = getContainerNameAndBloblName(name);
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        remoteName = name;
        _log.debug("Using async service with parallel transfers");
        try {
            final var containerClient = service.buildAsyncClient().getBlobContainerAsyncClient(cnb[0]);
            // If the container does not exists do we have to create it ?
            if (getSetup().getBoolean(HOST_AZURE_MK_CONTAINER) && !existsMono(containerClient.exists())) {
                if (getDebug()) {
                    _log.debug("Creating container");
                }
                containerClient.create().block();
            }
            final var blobClient = containerClient.getBlobAsyncClient(cnb[1]);
            // If a blob with the same name already exists do we have to delete it ?
            if (!getSetup().getBoolean(HOST_AZURE_IGNORE_DELETE) && existsMono(blobClient.exists())) {
                if (getDebug()) {
                    _log.debug("Deleting original blob");
                }
                blobClient.delete().block();
            }
            final var output = new PipedOutputStream();
            final var input = getMD5InputStream(new PipedInputStream(output, StreamPlugThread.DEFAULT_BUFF_SIZE));
            runnable = new ConfigurableRunnable() {
                @Override
                public void configurableRun() {
                    _log.debug("Starting upload");
                    try {
                        final var dataFlux = FluxUtil.toFluxByteBuffer(getMarkSupportedInputStream(input));
                        BlobRequestConditions requestConditions = null;
                        if (!getSetup().getBoolean(HOST_AZURE_OVERWRITE)) {
                            // Only upload if the blob does NOT already exist
                            requestConditions = new BlobRequestConditions().setIfNoneMatch("*");
                        }
                        final var parallelOptions = new ParallelTransferOptions()
                                .setBlockSizeLong(getSetup().getByteSize(HOST_AZURE_BLOCK_SIZE).size())
                                .setMaxConcurrency(getSetup().getInteger(HOST_AZURE_NUM_BUFFERS));
                        final var uploadOptions = new BlobParallelUploadOptions(dataFlux)
                                .setParallelTransferOptions(parallelOptions).setRequestConditions(requestConditions);
                        // then call upload with this options object
                        blobClient.uploadWithResponse(uploadOptions).block();
                        _log.debug("File uploaded");
                    } catch (final Exception e) {
                        _log.debug("File NOT uploaded", e);
                    } finally {
                        StreamPlugThread.closeQuietly(input);
                    }
                }
            };
            runnable.execute();
            return output;
        } catch (final Exception e) {
            _log.debug("upload", e);
            throw new IOException(e.getMessage());
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
        _log.debug("Get file {} ({})", name, posn);
        setStatus("GET");
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        final var cnb = getContainerNameAndBloblName(name);
        try {
            // Open input stream directly from the blob client
            return service.getBlobContainerClient(cnb[0]).getBlobClient(cnb[1]).openInputStream();
        } catch (final Exception e) {
            _log.debug("download", e);
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Check.
     *
     * @param sent
     *            the sent
     * @param checksum
     *            the checksum
     * @param error
     *            the error
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void check(final long sent, final String checksum, final boolean error) throws IOException {
        _log.debug("Check file ({},{})", sent, checksum);
        setStatus("CHECK");
        if (remoteName == null) {
            throw new IOException("A check should only occur after a put/get");
        }
        if (runnable != null) {
            // Wait for the task to complete!
            _log.debug("Waiting for upload task to complete");
            try {
                runnable.join();
            } catch (TimeoutException | ExecutionException | InterruptedException e) {
            }
            _log.debug("Upload task completed");
        }
        if (!error) {
            if (!getSetup().getBoolean(HOST_AZURE_IGNORE_CHECK)) {
                final var properties = getProperties(remoteName);
                final var size = properties.getBlobSize();
                if (sent != size) {
                    throw new IOException("Remote file size is " + Format.formatPercentage(size, sent)
                            + " of original file size (sent=" + sent + "/size=" + size + ")");
                }
                final var md5 = properties.getContentMd5();
                if (md5 != null) {
                    final var md5Hex = getValue(md5);
                    if (isNotEmpty(checksum) && !md5Hex.equals(checksum)) {
                        throw new IOException("Remote file md5 is " + md5Hex + ", original file md5 is " + checksum);
                    } else if (md5Checksum != null && !md5Hex.equals(md5Checksum.getValue())) {
                        throw new IOException(
                                "Remote file md5 is " + md5Hex + ", original file md5 is " + md5Checksum.getValue());
                    } else {
                        _log.debug("Remote file md5: {}{}", md5Hex, md5Checksum != null ? " (checked)" : "");
                    }
                } else {
                    _log.debug("No remote md5 available");
                }
            }
            setAttribute("remote.fileName", remoteName);
        }
    }

    /**
     * Gets the value.
     *
     * @param digest
     *            the digest
     *
     * @return the value
     */
    public String getValue(final byte[] digest) {
        return printHexBinary(digest).toLowerCase();
    }

    /** The Constant hexCode. */
    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    /**
     * Prints the hex binary.
     *
     * @param data
     *            the data
     *
     * @return the string
     */
    private String printHexBinary(final byte[] data) {
        final var r = new StringBuilder(data.length * 2);
        for (final byte b : data) {
            r.append(hexCode[b >> 4 & 0xF]);
            r.append(hexCode[b & 0xF]);
        }
        return r.toString();
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
        return getProperties(name).getBlobSize();
    }

    /**
     * Gets the properties for a blob (used to get the size and md5).
     *
     * @param name
     *            the name
     *
     * @return the properties
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private BlobProperties getProperties(final String name) throws IOException {
        _log.debug("Properties {}", name);
        final var cnb = getContainerNameAndBloblName(name);
        try {
            return service.getBlobContainerClient(cnb[0]).getBlobClient(cnb[1]).getProperties();
        } catch (final Exception e) {
            _log.debug("getBlobProperties", e);
            throw new IOException(e.getMessage());
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
        if (isNotEmpty(containerName)) {
            throw new IOException(
                    "Mkdir not allowed when parameter " + getSetup().getModuleName() + ".containerName is set");
        }
        final var token = getPathTokenizer(directory);
        final var count = token.countTokens();
        if (count == 0) {
            throw new IOException("Invalid directory specified: empty");
        }
        if (count == 1) {
            try {
                service.getBlobContainerClient(token.nextToken()).create();
            } catch (final Exception e) {
                _log.debug("createContainer", e);
                throw new IOException(e.getMessage());
            }
        } else {
            throw new IOException("Subdirectories not allowed in Container");
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
        if (isNotEmpty(containerName)) {
            throw new IOException(
                    "Rmdir not allowed when parameter " + getSetup().getModuleName() + ".containerName is set");
        }
        final var token = getPathTokenizer(directory);
        final var count = token.countTokens();
        if (count == 0) {
            throw new IOException("Invalid directory specified: empty");
        }
        if (count == 1) {
            try {
                service.getBlobContainerClient(token.nextToken()).delete();
            } catch (final Exception e) {
                _log.debug("deleteContainer", e);
                throw new IOException(e.getMessage());
            }
        } else {
            throw new IOException("Subdirectories not allowed in Blob");
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
        _log.debug("list: {},{}", directory == null ? "(null)" : directory, pattern == null ? "(null)" : pattern);
        final var token = getPathTokenizer(directory);
        final var count = token.countTokens();
        try {
            if (isNotEmpty(containerName)) {
                // We have a Container defined in the setup!
                if (count == 0) {
                    // We are going to list the specified Container!
                    listObjects(output, containerName, null, pattern);
                } else {
                    // We have a Blob name specified!
                    listObjects(output, containerName, token.nextToken("\0").substring(1), pattern);
                }
            } else if (count == 0) {
                // We have to display the list of Containers!
                listContainers(output, null, pattern);
            } else if (count == 1) {
                final var container = token.nextToken();
                if (container.contains("*") || container.contains("?")) {
                    // the name contains wildcards so we will show the list!
                    listContainers(output, container, pattern);
                } else {
                    // We are going to list the specified Container!
                    listObjects(output, container, null, pattern);
                }
            } else {
                // We have a Blob name specified!
                final var container = token.nextToken();
                listObjects(output, container, token.nextToken("\0").substring(1), pattern);
            }
        } catch (final Exception e) {
            _log.debug("list", e);
            throw new IOException(e.getMessage());
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
        _log.debug("List {}{}", directory, pattern != null ? " (" + pattern + ")" : "");
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
     * List blobs.
     *
     * @param output
     *            the output
     * @param containerName
     *            the container name
     * @param pattern
     *            the pattern
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void listContainers(final ListOutput output, final String containerName, final String pattern)
            throws IOException {
        _log.debug("listContainers: {},{}", containerName == null ? "(null)" : containerName,
                pattern == null ? "(null)" : pattern);
        final var client = service.buildClient();
        final var owner = client.getAccountName();
        final var ownerName = owner != null ? owner : "unknown";
        final List<String> list = new ArrayList<>();
        // Prepare wildcard matcher if needed
        Predicate<String> containerMatcher = _ -> true;
        if (containerName != null && !containerName.isBlank()) {
            // Glob-style matcher (e.g., *data*, logs?, etc.)
            final var matcher = FileSystems.getDefault().getPathMatcher("glob:" + containerName);
            containerMatcher = name -> matcher.matches(Path.of(name));
        }
        for (final BlobContainerItem containerItem : client.listBlobContainers()) {
            final var name = containerItem.getName();
            _log.debug("ContainerItemName: {}", name);
            if (list.contains(name)) {
                break; // End of the listing
            }
            list.add(name);
            final var matchesPattern = pattern == null || name.matches(pattern);
            final var matchesWildcard = containerMatcher.test(name);
            if (matchesPattern && matchesWildcard) {
                output.add(Format.getFtpList("drw-r--r--", getSetup().get(HOST_AZURE_FTPUSER, ownerName),
                        getSetup().get(HOST_AZURE_FTPGROUP, ownerName), "2048",
                        containerItem.getProperties().getLastModified().toEpochSecond() * 1000, name));
            }
        }
    }

    /**
     * List objects in the specified container.
     *
     * @param output
     *            the output
     * @param containerName
     *            the container name
     * @param fileName
     *            the file name
     * @param pattern
     *            the pattern
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void listObjects(final ListOutput output, final String containerName, final String fileName,
            final String pattern) throws IOException {
        _log.debug("listObjects: {},{},{}", containerName == null ? "(null)" : containerName,
                fileName == null ? "(null)" : fileName, pattern == null ? "(null)" : pattern);
        final var storageClient = service.buildClient();
        final var containerClient = storageClient.getBlobContainerClient(containerName);
        final var owner = storageClient.getAccountName();
        final var ownerName = owner != null ? owner : "unknown";
        final List<String> list = new ArrayList<>();
        // Prepare wildcard matcher if needed
        Predicate<String> fileNameMatcher = _ -> true;
        if (fileName != null && !fileName.isBlank()) {
            final var matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileName);
            fileNameMatcher = name -> matcher.matches(Path.of(name));
        }
        for (final BlobItem blobItem : containerClient.listBlobs()) {
            final var name = blobItem.getName();
            if (list.contains(name)) {
                // End of the listing!
                break;
            }
            list.add(name);
            final var properties = blobItem.getProperties();
            if (properties != null && (pattern == null || name.matches(pattern)) && fileNameMatcher.test(name)) {
                final var entry = Format.getFtpList("-rw-r--r--", getSetup().get(HOST_AZURE_FTPUSER, ownerName),
                        getSetup().get(HOST_AZURE_FTPGROUP, ownerName), String.valueOf(properties.getContentLength()),
                        properties.getLastModified().toEpochSecond() * 1000, name);
                output.add(entry);

                if (getDebug()) {
                    _log.debug("Adding entry: {}", entry);
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
            StreamPlugThread.closeQuietly(azureInput);
            if (service != null) {
                service.shutdown();
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

    /**
     * Gets the path tokenizer.
     *
     * @param path
     *            the path
     *
     * @return the path tokenizer
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    private static StringTokenizer getPathTokenizer(final String path) throws FileNotFoundException {
        final String normalizedPath;
        if (isNotEmpty(path)) {
            normalizedPath = Format.normalizePath(path);
        } else {
            normalizedPath = "";
        }
        _log.debug("NormalizedPath: {}", normalizedPath);
        return new StringTokenizer(normalizedPath, "/");
    }

    /**
     * Gets the sas token.
     *
     * @param sasUrl
     *            the sas url
     * @param sasSubscriptionKey
     *            the sas subscription key
     *
     * @return the sas token
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String getSasToken(final String sasUrl, final String sasSubscriptionKey) throws IOException {
        final var message = new StringBuilder();
        try (final var client = HttpClient.newHttpClient()) {
            final var request = HttpRequest.newBuilder().uri(URI.create(sasUrl))
                    .header("Ocp-Apim-Subscription-Key", sasSubscriptionKey).GET().build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse the JSON response and extract the SAS token from the 'token' key
                return new ObjectMapper().readTree(response.body()).get("token").asText();
            } else {
                message.append("Failed to get SAS token, Status Code: ").append(response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            message.append("Failed to get SAS token: ").append(e.getMessage());
            _log.warn("Failed to get SAS token", e);
        }
        throw new IOException(message.toString());
    }

    /**
     * The Class BlobServiceClientCache.
     */
    private static final class BlobServiceClientCache {

        /** The Constant instances. */
        private static final Map<String, BlobServiceClientCache> instances = new ConcurrentHashMap<>();

        /** The Constant mutexProvider. */
        private static final Synchronized mutexProvider = new Synchronized();

        /** The count number. */
        private final AtomicInteger countNumber = new AtomicInteger();

        /** The blol builder. */
        private final BlobServiceClientBuilder blolBuilder;

        /** The unique key. */
        private final String uniqueKey;

        /**
         * Gets the blob service client cache.
         *
         * @param sasUrl
         *            the sas url
         * @param sasSubscriptionKey
         *            the sas subscription key
         * @param accountName
         *            the account name
         * @param accountKey
         *            the account key
         * @param url
         *            the url
         * @param containerName
         *            the container name
         * @param mkContainer
         *            the mk container
         *
         * @return the blob service client cache
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        static BlobServiceClientCache getBlobServiceClientCache(final String sasUrl, final String sasSubscriptionKey,
                final String accountName, final String accountKey, final String userAssignedClientId, final String url,
                final String containerName, final boolean mkContainer) throws IOException {
            final var key = new StringBuilder(sasUrl).append(sasSubscriptionKey).append(accountName).append(accountKey)
                    .append(url).append(containerName).append(mkContainer).toString();
            try (final var mutex = mutexProvider.getMutex(key)) {
                synchronized (mutex.lock()) {
                    var cacheIten = instances.get(key);
                    if (cacheIten == null) {
                        final var builder = new BlobServiceClientBuilder()
                                .httpClient(new NettyAsyncHttpClientBuilder().build()).endpoint(url);
                        if (isNotEmpty(accountName) && isNotEmpty(accountKey)) {
                            // Using storage shared key credentials
                            _log.debug("Using storage shared key credentials");
                            builder.credential(new StorageSharedKeyCredential(accountName, accountKey));
                        } else if (isNotEmpty(sasUrl) && isNotEmpty(sasSubscriptionKey)) {
                            // Using SAS token
                            _log.debug("Using SAS token");
                            builder.sasToken(getSasToken(sasUrl, sasSubscriptionKey));
                        } else if (isNotEmpty(userAssignedClientId)) {
                            // Using user-assigned managed identity
                            _log.debug("Using user-assigned managed identity");
                            final var managedIdentityCredential = new ManagedIdentityCredentialBuilder()
                                    .clientId(userAssignedClientId).build();
                            builder.credential(managedIdentityCredential);
                        }
                        cacheIten = new BlobServiceClientCache(key, builder);
                        instances.put(key, cacheIten);
                        if (mkContainer && isNotEmpty(containerName)) {
                            final var containerClient = cacheIten.getBlobContainerClient(containerName);
                            if (!containerClient.exists()) {
                                _log.debug("Creating Container {}", containerName);
                                containerClient.create();
                            }
                        }
                    }
                    cacheIten.lock();
                    return cacheIten;
                }
            }
        }

        /**
         * Instantiates a new blob service client cache.
         *
         * @param key
         *            the key
         * @param builder
         *            the builder
         */
        BlobServiceClientCache(final String key, final BlobServiceClientBuilder builder) {
            uniqueKey = key;
            blolBuilder = builder;
            _log.debug("Cache {}", key);
        }

        /**
         * Gets the blob container client.
         *
         * @param containerName
         *            the container name
         *
         * @return the blob container client
         */
        BlobContainerClient getBlobContainerClient(final String containerName) {
            return buildClient().getBlobContainerClient(containerName);
        }

        /**
         * Builds the client.
         *
         * @return the blob service client
         */
        BlobServiceClient buildClient() {
            return blolBuilder.buildClient();
        }

        /**
         * Builds the async client.
         *
         * @return the blob service async client
         */
        BlobServiceAsyncClient buildAsyncClient() {
            return blolBuilder.buildAsyncClient();
        }

        /**
         * Lock.
         */
        void lock() {
            final var count = countNumber.addAndGet(1);
            _log.debug("Lock {}: {}", uniqueKey, count);
        }

        /**
         * Shutdown.
         */
        void shutdown() {
            var close = false;
            try (final var mutex = mutexProvider.getMutex(uniqueKey)) {
                synchronized (mutex.lock()) {
                    final var count = countNumber.addAndGet(-1);
                    if (close = count <= 0) {
                        instances.remove(uniqueKey);
                    }
                    _log.debug("Free {}:{}", uniqueKey, count);
                }
            }
            if (close) {
                _log.debug("Shutdown {}", uniqueKey);
            }
        }
    }
}
