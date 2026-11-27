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

package ecmwf.ecpds.master.plugin.http.model.news.components;

/**
 * @author sy8
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.FileModel;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.OrderedBean;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.services.content.Content;

public interface Component extends ModelBean, Content, OrderedBean {

    public String getName();

    public void setName(String v);

    public String getTitle();

    public void setTitle(String v);

    public String getDescription();

    public void setDescription(String v);

    public String getPresentation();

    public void setPresentation(String v);

    public Date getCreationDate();

    public String getCreationDateAsString();

    public void setCreationDate(Date v);

    public Date getModificationDate();

    public String getModificationDateAsString();

    public void setModificationDate(Date v);

    public String getAuthor();

    public String getAuthorUid();

    public String getModifiedBy();

    public String getModifiedByUid();

    public void setAuthor(String s);

    public void setAuthorUid(String s);

    public void setModifiedBy(String s);

    public void setModifiedByUid(String s);

    public boolean getPublished();

    public void setPublished(boolean v);

    public String getLocale();

    public void setLocale(String v);

    public int getSortOrder();

    public void setSortOrder(int v);

    public String getItemId();

    public void setItemId(String v);

    public String getChannelName() throws ComponentException;

    public void setChannelName(String v);

    public FileModel getFile();

    public void setFile(FileModel b);

    public boolean isImage();

    public Component moveToItem(NewsItem i, Object context, boolean overwrite)
            throws ComponentException, DuplicatedComponentException;

    public Component copyToItem(NewsItem i, Object context, boolean overwrite)
            throws ComponentException, DuplicatedComponentException;

    // public String getContent();

}
