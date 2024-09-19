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

package ecmwf.ecpds.mover.plugin.http;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.UserSession;
import ecmwf.common.text.Format;

/**
 * The Class BlobStore.
 */
public class BlobStore implements Closeable {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(BlobStore.class);

    /** The remote address. */
    final String _remoteAddress;

    /** The session. */
    final UserSession _session;

    /**
     * Instantiates a new blob store.
     *
     * @param remoteAddress
     *            the remote address
     * @param session
     *            the session
     */
    BlobStore(final String remoteAddress, final UserSession session) {
        _remoteAddress = remoteAddress;
        _session = session;
    }

    /**
     * Container exists.
     *
     * @param containerName
     *            the container name
     *
     * @return true, if successful
     */
    public boolean containerExists(final String containerName) {
        logger.debug("containerExists: " + containerName);
        try {
            final var path = _getFilename(containerName);
            return path.getKey() && _session.getFileListElement(path.getValue()).isDirectory();
        } catch (final Throwable t) {
            logger.warn("containerExists", t);
        }
        return false;
    }

    /**
     * Gets the container access.
     *
     * @param containerName
     *            the container name
     *
     * @return the container access
     */
    public ContainerAccess getContainerAccess(final String containerName) {
        logger.debug("getContainerAccess: " + containerName);
        return ContainerAccess.PRIVATE;
    }

    /**
     * List.
     *
     * @return the list
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    public List<StorageMetadata> list() throws S3Exception {
        logger.debug("list");
        return list("", new ListContainerOptions(), "", 1000);
    }

    /**
     * List.
     *
     * @param containerName
     *            the container name
     * @param options
     *            the options
     * @param prefix
     *            the prefix
     * @param maxKeys
     *            the max keys
     *
     * @return the list
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    public List<StorageMetadata> list(final String containerName, final ListContainerOptions options,
            final String prefix, final int maxKeys) throws S3Exception {
        final var path = _getFilename(containerName + "/" + prefix);
        final List<StorageMetadata> result = new ArrayList<>();
        logger.debug("list: bucket=" + containerName + ", maxKeys=" + maxKeys + ": " + path);
        try {
            for (final FileListElement element : _session.getFileList(path.getValue())) {
                if (options.isRecursive() && element.isDirectory()) {
                    // This is a new directory and the request is recursive so let's call this
                    // method recursively, unless we have already reached the maximum number of keys
                    final var newMaxKeys = maxKeys - result.size();
                    if (newMaxKeys > 0) {
                        for (final StorageMetadata metadata : list(containerName, options,
                                prefix + "/" + element.getName(), newMaxKeys)) {
                            if (_isTruncated(result, newMaxKeys, metadata)) {
                                break;
                            }
                        }
                    }
                } else if (_isTruncated(result, maxKeys, new StorageMetadata("".equals(containerName), element))) {
                    break;
                }
            }
        } catch (final EccmdException t) {
            final var message = t.getMessage();
            if (message != null && message.startsWith("Destination not found")) {
                throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
            }
            logger.warn("list", t);
        } catch (final Throwable t) {
            logger.warn("list", t);
        }
        return result;
    }

    /**
     * Checks if is truncated.
     *
     * Add the metadata to the list and check if the listing should be stopped there (meaning we have reached the limit
     * specified by the max keys parameter).
     *
     * @param result
     *            the result
     * @param maxKeys
     *            the max keys
     * @param metadata
     *            the metadata
     *
     * @return true, if successful
     */
    private static boolean _isTruncated(final List<StorageMetadata> result, final int maxKeys,
            final StorageMetadata metadata) {
        logger.debug("add" + metadata.getType() + ": " + metadata.getName());
        result.add(metadata);
        final var truncated = maxKeys > 0 && result.size() >= maxKeys;
        metadata.setTruncated(truncated);
        return truncated;
    }

    /**
     * Removes the blobs.
     *
     * @param containerName
     *            the container name
     * @param blobNames
     *            the blob names
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    public void removeBlobs(final String containerName, final Collection<String> blobNames) throws S3Exception {
        logger.debug("removeBlobs: " + containerName + ", " + blobNames);
        for (final String blobName : blobNames) {
            final var path = _getFilename(containerName + "/" + blobName);
            logger.debug("removeBlobs: bucket=" + containerName + ": " + path);
            try {
                _session.deleteFile(path.getValue(), true);
            } catch (final Throwable t) {
                logger.warn("removeBlobs", t);
            }
        }
    }

    /**
     * Blob metadata.
     *
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     *
     * @return the blob metadata
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    public BlobMetadata blobMetadata(final String containerName, final String blobName) throws S3Exception {
        logger.debug("blobMetadata: " + containerName + ", " + blobName);
        final var path = _getFilename(containerName + "/" + blobName);
        logger.debug("blobMetadata: bucket=" + containerName + ": " + path);
        try {
            return new BlobMetadata(_session.getFileListElement(path.getValue()), path.getValue());
        } catch (final Throwable t) {
            logger.warn("blobMetadata", t);
        }
        throw new S3Exception(S3ErrorCode.NO_SUCH_KEY);
    }

    /**
     * Gets the blob.
     *
     * @param containerName
     *            the container name
     * @param blobName
     *            the blob name
     * @param options
     *            the options
     *
     * @return the blob
     *
     * @throws S3Exception
     *             the s 3 exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Blob getBlob(final String containerName, final String blobName, final GetOptions options)
            throws S3Exception, IOException {
        logger.debug("getBlob: " + containerName + ", " + blobName + ", " + options);
        return new Blob(_remoteAddress, _session, blobMetadata(containerName, blobName), options);
    }

    /**
     * Gets the filename.
     *
     * Return a boolean to indicate if this is a container (destination) and the normalized internal path in ECPDS.
     *
     * @param filename
     *            the filename
     *
     * @return the map. entry
     *
     * @throws S3Exception
     *             the s 3 exception
     */
    private final Map.Entry<Boolean, String> _getFilename(final String filename) throws S3Exception {
        try {
            final var path = Format.normalizePath(filename);
            return new AbstractMap.SimpleEntry<>(path.startsWith("/") && path.split("/").length == 2,
                    "[" + _session.getUser() + "]DATA:" + path);
        } catch (final FileNotFoundException e) {
            throw new S3Exception(S3ErrorCode.NO_SUCH_BUCKET);
        }
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException {
        _session.close(false);
    }
}
