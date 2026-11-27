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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.users.Resource;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

/**
 * @author sy8
 */
public class DefaultContent implements Content {
    private String content = null;
    private String contentType = "text/html";
    private String accessPath = "#";

    public DefaultContent(String content) {
        this.content = content;
    }

    public DefaultContent(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public String getName() {
        return "DefaultContent";
    }

    public String getTitle() {
        return getName();
    }

    public String getStringRepresentation() {
        return this.content;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getAccessPath() {
        return this.accessPath;
    }

    public Resource getResource() throws ContentProcessorException {
        throw new ContentProcessorException("DefaultContent doesn't support getResource()");
    }

    public String getBasePathKey() {
        return "";
    }

    public Collection getLinks() {
        return new ArrayList(0);
    }

    public InputStream getInputStream() throws ContentProcessorException {
        return new ByteArrayInputStream(this.content.getBytes());
    }

    public Date getModificationDate() {
        return new Date();
    }

    public Content getLinkByName(String name) throws ContentProcessorException {
        throw new ContentProcessorException("No links for this Content at all.");
    }

    public String getWikiWordPath() throws ContentProcessorException {
        throw new ContentProcessorException("No WikiWords supported for this Content at all.");
    }
}
