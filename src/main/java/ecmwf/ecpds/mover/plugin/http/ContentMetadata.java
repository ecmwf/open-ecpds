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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.IOException;
import java.util.Date;

import ecmwf.common.ecaccess.FileListElement;

/**
 * The Class ContentMetadata.
 */
public class ContentMetadata {

    /** The element. */
    private final FileListElement _element;

    /**
     * Instantiates a new content metadata.
     *
     * @param element
     *            the element
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ContentMetadata(final FileListElement element) throws IOException {
        _element = element;
    }

    /**
     * Gets the cache control.
     *
     * @return the cache control
     */
    public String getCacheControl() {
        return "no-cache";
    }

    /**
     * Gets the content encoding.
     *
     * @return the content encoding
     */
    public String getContentEncoding() {
        return "none";
    }

    /**
     * Gets the content language.
     *
     * @return the content language
     */
    public String getContentLanguage() {
        return "en";
    }

    /**
     * Gets the content disposition.
     *
     * @return the content disposition
     */
    public String getContentDisposition() {
        return "attachment; filename = \"" + new File(_element.getName()).getName() + "\"";
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return "application/octet-stream";
    }

    /**
     * Gets the expires.
     *
     * @return the expires
     */
    public Date getExpires() {
        return null;
    }

    /**
     * Gets the content length.
     *
     * @return the content length
     */
    public String getContentLength() {
        return _element.getSize();
    }
}
