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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class StorageMetadata.
 */
public class StorageMetadata {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(StorageMetadata.class);

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /**
     * The Enum Type.
     */
    public enum Type {

        /** A container. */
        CONTAINER,

        /** An object in the object store. */
        BLOB,
        /**
         * Represents "special" blobs that have content-type set to application/directory.
         */
        FOLDER,
        /**
         * A partial path; used when the delimiter is set and represents all objects that start with the same name up to
         * the delimiter character (e.g. foo-bar and foo-baz, with delimiter set to "-" will be returned as "foo-").
         */
        RELATIVE_PATH;
    }

    /** The element. */
    private final FileListElement _element;

    /** The name. */
    private final String _name;

    /** The type. */
    private final Type _type;

    /** The truncated. */
    private boolean _truncated = false;

    /**
     * Instantiates a new storage metadata.
     *
     * @param container
     *            the container
     * @param element
     *            the element
     */
    StorageMetadata(final boolean container, final FileListElement element) {
        _element = element;
        _name = element.getPath() + (!container && element.isDirectory() ? "/" : "");
        _type = container ? Type.CONTAINER : element.isDirectory() ? Type.FOLDER : Type.BLOB;
    }

    /**
     * Gets the creation date.
     *
     * @return the creation date
     */
    public Date getCreationDate() {
        return new Date(_element.getTime());
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType() {
        return _type;
    }

    /**
     * Gets the last modified.
     *
     * @return the last modified
     */
    public Date getLastModified() {
        return new Date(_element.getTime());
    }

    /**
     * Gets the e tag.
     *
     * @return the e tag
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getETag() throws IOException {
        if (_type != Type.BLOB) {
            throw new IOException("ETags only available for Blobs");
        }
        return _getETag(_element.getComment().trim());
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public String getSize() {
        return _element.isDirectory() ? "-" : _element.getSize();
    }

    /**
     * Gets the tier.
     *
     * @return the tier
     */
    public String getTier() {
        return "Standard";
    }

    /**
     * Sets the truncated.
     *
     * @param truncated
     *            the new truncated
     */
    public void setTruncated(final boolean truncated) {
        _truncated = truncated;
    }

    /**
     * Checks if is truncated.
     *
     * @return true, if is truncated
     */
    public boolean isTruncated() {
        return _truncated;
    }

    /**
     * Gets the E tag.
     *
     * @param dataTransferId
     *            the data transfer id
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static final String _getETag(final String dataTransferId) throws IOException {
        try {
            return _mover.getMasterInterface().getETag(Long.parseLong(dataTransferId));
        } catch (final Throwable t) {
            logger.warn("Cannot process ETag", t);
            throw new IOException("Cannot process ETag");
        }
    }
}
