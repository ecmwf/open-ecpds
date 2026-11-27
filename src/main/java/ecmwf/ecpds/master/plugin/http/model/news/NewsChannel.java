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

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.model.HierarchicalBean;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.OrderedBean;
import ecmwf.ecpds.master.plugin.http.services.content.Content;

/**
 * The client-accessible interface for a NewsChannel.
 *
 * Created: Thu Apr 25 17:23:18 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public interface NewsChannel extends ModelBean, Content, HierarchicalBean, OrderedBean {

    public static final String CHANNEL_QUALIFIER = "::";
    public static final String TOP_ID = "0";

    public String getParentId();

    public void setParentId(String id);

    public void setRoot();

    public Collection getNewsItems() throws NewsException;

    public Collection getNewsItemNames() throws NewsException;

    public String getName();

    public void setName(String v);

    public String getTitle();

    public void setTitle(String v);

    public String getDescription();

    public void setDescription(String v);

    public String getContentType();

    public void setContentType(String s);

    public NewsChannelPresentation getDefaultPresentation() throws NewsException;

    public NewsChannelPresentation getPresentation(String name) throws NewsException;

    public void setPresentations(Collection c);

    public Collection getPresentations();

    public Collection getPresentationIds();

    public Collection getAvailableCommands();

    public long getSearchRelevance();

    public long getSearchRelevance(ModelSearch search);

    public void setSearchRelevance(long v);

} // NewsChannel
