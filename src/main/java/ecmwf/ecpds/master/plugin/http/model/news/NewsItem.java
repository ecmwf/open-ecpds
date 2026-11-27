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

package ecmwf.ecpds.master.plugin.http.model.news;

import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ecmwf.ecpds.master.plugin.http.model.HierarchicalBean;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
//import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.OrderedBean;
//import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.news.components.Component;
import ecmwf.ecpds.master.plugin.http.model.news.components.ComponentException;
import ecmwf.ecpds.master.plugin.http.model.news.components.DuplicatedComponentException;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.services.content.Content;

/**
 * Represents each news item published in the web site.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public interface NewsItem extends ModelBean, Content, OrderedBean, HierarchicalBean {

    public String getName();

    public void setName(String s);

    public String getTitle();

    public void setTitle(String v);

    public String getDescription();

    public void setDescription(String v);

    public String getBody();

    public void setBody(String v);

    public Date getCreationDate();

    public void setCreationDate(Date v);

    public Date getModificationDate();

    public void setModificationDate(Date v);

    public Date getReferenceDate();

    public void setReferenceDate(Date v);

    public boolean getPublished();

    public void setPublished(boolean v);

    public boolean getInternal();

    public void setInternal(boolean v);

    public String getAuthor();

    public String getModifiedBy();

    public String getAuthorUid();

    public String getModifiedByUid();

    public void setAuthor(String v);

    public void setModifiedBy(String v);

    public void setAuthorUid(String v);

    public void setModifiedByUid(String v);

    public String getChannelId();

    public String getChannelName();

    public String getParentId();

    public boolean isComment();

    public void setChannelId(String v);

    public void setChannelName(String v);

    public void setParentId(String s);

    public NewsChannel getChannel() throws NewsException;

    public String getPresentation();

    public void setPresentation(String v);

    public String getExternalId();

    public void setExternalId(String v);

    public int getPriority();

    public void setPriority(int v);

    public void setContentType(String v);

    public String getContentType();

    // Locking support
    public void lock(User u);

    public void unlock();

    public boolean isLocked();

    public Date getLockDate();

    public String getLockUid();

    public long getCombinedSearchRelevance(Collection leaves);

    public long getSearchRelevance();

    // public long getSearchRelevance(ModelSearch search);
    public void setSearchRelevance(long v);

    public Collection getKeywords();

    public String getKeywordString();

    public void setKeywordsFromString(String s);

    public Collection getComponents() throws ComponentException;

    public List getComments() throws ModelException;

    public Map getComponentMap() throws ComponentException;

    public Component getComponentByName(String name) throws NewsException, ComponentException;

    /**
     * @return The first component or NULL if there is none.
     */
    public Component getDefaultComponent();

    public int getCommentNumber() throws ModelException;

    public NewsItem moveToChannel(NewsChannel c, Object context, boolean overwrite)
            throws NewsException, DuplicatedNewsItemException, DuplicatedComponentException;

    public NewsItem copyToChannel(NewsChannel c, Object context, boolean overwrite)
            throws NewsException, DuplicatedNewsItemException, DuplicatedComponentException;

}
