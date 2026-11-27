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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import ecmwf.ecpds.master.plugin.http.model.users.Resource;

/**
 * @author sy8
 *
 *         To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates.
 *         To enable and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */
public class FileContent implements Content {

    // private String content = null;
    private String contentType = "text/html";
    private String accessPath = "#";
    private Resource resource = null;
    private File file;

    public FileContent(File file, Resource resource, String contentType) {
        this.accessPath = file.getAbsolutePath();
        this.contentType = contentType;
        this.file = file;
        this.resource = resource;
    }

    public String getName() {
        return this.file.getName();
    }

    public String getTitle() {
        return "File at " + this.getName();
    }

    public String getStringRepresentation() {
        return "File stored at '" + this.accessPath + "' responding for resource '" + this.resource + "'";
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getAccessPath() {
        return this.accessPath;
    }

    public Resource getResource() {
        return this.resource;
    }

    public String getBasePathKey() {
        return "";
    }

    public Collection getLinks() {
        return new Vector();
    }

    public InputStream getInputStream() throws ContentProcessorException {
        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
            throw new ContentProcessorException("FileContent: Error getting InputStream", e);
        }
    }

    /**
     * @see ecmwf.ecpds.master.plugin.http.services.content.Content#getLinkByName(String)
     */
    public Content getLinkByName(String name) throws ContentProcessorException {
        throw new ContentProcessorException("No links for this FileContent at all.");
    }

    public String getWikiWordPath() throws ContentProcessorException {
        throw new ContentProcessorException("No WikiWords in FileContent");
    }

    public Date getModificationDate() {
        return new Date(this.file.lastModified());
    }

}
