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

package ecmwf.ecpds.master.plugin.http.services.content;

/**
 * Defines representable, processable and accessible content.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela</a>
 * @version 1.0
 */

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.users.Resource;

public interface Content {

    public String getName();

    public String getTitle();

    /**
     * @return A quick (and maybe wrong) default representation of the content. I should be ok for text/plain type
     *         content.
     */
    public String getStringRepresentation();

    /**
     * @return The content type ("text/plain", "image/gif", "text/html", etc) of the content.
     */
    public String getContentType();

    /**
     * @return If the content is addressable through an application (ie NewsItem), the resource key to where the app is
     *         mounted.
     */
    public String getBasePathKey();

    /**
     * @return The path used to access the content (HTTP or file, but protocol is not included) TODO: Make this a bit
     *         clearer (include protocol, URL-like??)
     */
    public String getAccessPath();

    /**
     * @return The path to access a WikiWord which appears at this content level. It will depend in the way This content
     *         gets accessed
     */

    public String getWikiWordPath() throws ContentProcessorException;

    /**
     * @return The web wide resource for this content.
     */
    public Resource getResource() throws ContentProcessorException;

    /**
     * @return Access to the Content <code>InputStream</code>.
     *
     * @throws ContentProcessorException
     *             Some content objects can't provide an InputStream (ie news item)
     */
    public InputStream getInputStream() throws ContentProcessorException;

    /**
     * @return The collection of other contents dependent on this (ie, Components for a NewsItem).
     *
     * @throws ContentProcessorException
     *             If they cant be found.
     */
    public Collection getLinks() throws ContentProcessorException;

    /**
     * @param name
     *            The name of the <code>Content</code>
     *
     * @return A <code>Content</code> dependent on this which is keyed by the supplied name.
     *
     * @throws ContentProcessorException
     *             No content keyed by that name.
     */
    public Content getLinkByName(String name) throws ContentProcessorException;

    /**
     * @return When was last changed ;-)
     */
    public Date getModificationDate();

}
