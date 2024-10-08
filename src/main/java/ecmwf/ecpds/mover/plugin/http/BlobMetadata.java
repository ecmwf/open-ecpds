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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class BlobMetadata.
 */
public class BlobMetadata {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(BlobMetadata.class);

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /** The element. */
    private final FileListElement _element;

    /** The content metadata. */
    private final ContentMetadata _contentMetadata;

    /** The user metadata. */
    private final Map<String, String> _userMetadata;

    /** The etag. */
    private final String _etag;

    /** The path. */
    private final String _path;

    /**
     * Instantiates a new blob metadata.
     *
     * @param element
     *            the element
     * @param path
     *            the path
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    BlobMetadata(final FileListElement element, final String path) throws IOException {
        _userMetadata = new HashMap<>();
        _contentMetadata = new ContentMetadata(element);
        _element = element;
        _etag = _getETag(_element.getComment().trim());
        _path = path;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return _path;
    }

    /**
     * Gets the e tag.
     *
     * @return the e tag
     */
    public String getETag() {
        return _etag;
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
     * Gets the content metadata.
     *
     * @return the content metadata
     */
    public ContentMetadata getContentMetadata() {
        return _contentMetadata;
    }

    /**
     * Gets the user metadata.
     *
     * @return the user metadata
     */
    public Map<String, String> getUserMetadata() {
        return _userMetadata;
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
