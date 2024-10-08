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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.FileListElement;
import ecmwf.ecpds.master.MasterException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.model.transfer.DestinationMetaData;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.users.Resource;
import ecmwf.web.services.content.Content;
import ecmwf.web.services.content.ContentProcessorException;

/**
 * The Class DestinationMetaDataBean.
 */
public class DestinationMetaDataBean extends ModelBeanBase implements DestinationMetaData {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DestinationMetaDataBean.class);

    /** The Constant NOT_SUPPORTED. */
    private static final String NOT_SUPPORTED = "Not Supported";

    /** The Constant DEFAULT_CONTENT_TYPE. */
    protected static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /** The destination name or type. */
    private final String destinationNameOrType;

    /** The file. */
    private final FileListElement file;

    /** The content type. */
    private final String contentType;

    /**
     * Instantiates a new destination meta data bean.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param file
     *            the file
     * @param contentType
     *            the content type
     */
    protected DestinationMetaDataBean(final String destinationNameOrType, final FileListElement file,
            final String contentType) {
        this.destinationNameOrType = destinationNameOrType;
        this.file = file;
        this.contentType = contentType;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return DestinationMetaData.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the modification date.
     */
    @Override
    public Date getModificationDate() {
        return new Date(file.getTime());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last modification date.
     */
    @Override
    public Date getLastModificationDate() {
        return getModificationDate();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return file.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the title.
     */
    @Override
    public String getTitle() {
        return getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the input stream.
     */
    @Override
    public InputStream getInputStream() throws ContentProcessorException {
        try {
            return MasterManager.getAI().getProxySocketInput(destinationNameOrType, file.getName(), 0).getDataSocket()
                    .getInputStream();
        } catch (MasterException | IOException e) {
            throw new ContentProcessorException("Problem getting input for file '" + file + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the access path.
     */
    @Override
    public String getAccessPath() {
        return "/do/transfer/destination/metadata/" + destinationNameOrType + "/" + file.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the base path key.
     */
    @Override
    public String getBasePathKey() {
        return "";
    }

    /**
     * {@inheritDoc}
     *
     * Gets the content type.
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the link by name.
     */
    @Override
    public Content getLinkByName(final String name) throws ContentProcessorException {
        throw new ContentProcessorException(NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the links.
     */
    @Override
    public Collection<?> getLinks() throws ContentProcessorException {
        throw new ContentProcessorException(NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the resource.
     */
    @Override
    public Resource getResource() throws ContentProcessorException {
        throw new ContentProcessorException(NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the string representation.
     */
    @Override
    public String getStringRepresentation() {
        return getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the wiki word path.
     */
    @Override
    public String getWikiWordPath() throws ContentProcessorException {
        return "#";
    }

    /**
     * Gets the string content.
     *
     * @return the string content
     */
    public String getStringContent() {
        final var out = new StringBuilder();
        try {
            final var is = getInputStream();
            final var buffer = new byte[1024];
            var read = 0;
            while ((read = is.read(buffer)) > 0) {
                out.append(new String(buffer, 0, read));
            }
            is.close();
            return out.toString();
        } catch (final Exception e) {
            log.error("Problem getting string representation", e);
            return e.getMessage();
        }
    }
}
