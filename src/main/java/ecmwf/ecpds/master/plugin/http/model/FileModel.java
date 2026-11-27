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

package ecmwf.ecpds.master.plugin.http.model;

import java.io.InputStream;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
//import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

/**
 * FileModel.java
 *
 * <p>
 * Class representing a file managed by any application.
 * </p>
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public interface FileModel extends ModelBean, Content {

    public void setAccessPath(String v);

    public void setName(String v);

    public void setContentType(String v);

    public String getIcon();

    public void setIcon(String v);

    public String getPhysicalPath();

    public void setPhysicalPath(String path);

    public void setLastModified(Date d);

    /**
     * These methods are using during downloads an uploads to get information on the file about to be saved.
     */
    public long getSize();

    public void setSize(long v);

    /**
     * Gets the size of the file ONCE DOWNLOADED.
     */
    public long getPhysicalFileSize() throws ModelException;

    public String getFormattedPhysicalFileSize() throws ModelException;

    public void setPhysicalFileSize(long l);

    public InputStream getFileInputStream() throws ContentProcessorException;

    public void setFileInputStream(InputStream v);

    /**
     * Copy all fields, with the exception of the streams
     */
    public FileModel copy();

} // FileModel
