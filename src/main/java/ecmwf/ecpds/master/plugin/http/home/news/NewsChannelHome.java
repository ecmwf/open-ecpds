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

package ecmwf.ecpds.master.plugin.http.home.news;

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsObjectNotFoundException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOObjectNotFoundException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;

/**
 * NewsChannels Home interface.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public class NewsChannelHome extends ModelHomeBase {

    private static final String INTERFACE = NewsChannel.class.getName();

    public static NewsChannel create() throws NewsException {
        try {
            return (NewsChannel) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new NewsException("Error creating object: ", e);
        }
    }

    public static NewsChannel findTopOfHierarchy() throws NewsException {
        return findByPrimaryKey(NewsChannel.TOP_ID);
    }

    public static NewsChannel findByPrimaryKey(String key) throws NewsException {
        try {
            return (NewsChannel) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOObjectNotFoundException e) {
            throw new NewsObjectNotFoundException(key, INTERFACE);
        } catch (DAOException e) {
            throw new NewsException("Error retrieving object", e);
        }
    }

    public static NewsChannel findByName(String name) throws NewsException {
        Collection c = find(getDefaultSearch("name='" + name + "'"));
        if (c.size() < 1)
            throw new NewsObjectNotFoundException(name, INTERFACE);
        else
            return (NewsChannel) c.iterator().next();
    }

    public static Collection findAll() throws NewsException {
        ModelSearch search = getDefaultSearch("true");
        search.setCacheable(false);
        search.setSizeLimit(Integer.MAX_VALUE);
        return find(search);
    }

    public static Collection find(ModelSearch search) throws NewsException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new NewsException("Error retrieving object set: ", e);
        }
    }

    public static Collection findBelow(NewsChannel channel, ModelSearch search) throws ModelException {

        // TODO: Replace this semantics to the use of ModelSearch.setBase(channel.getName()) and ModelSearch.setScope()

        // Iterator i = channel.getChildren().iterator();
        // String channelQuery = "";
        // while (i.hasNext()) {
        // channelQuery += "parent_id="+((NewsChannel)i.next()).getId();
        // if (i.hasNext())
        // channelQuery += " or ";
        // }
        // if (!"".equals(channelQuery)) {
        search.addAndQuery("(parent_id=" + channel.getId() + ")");
        // }
        return find(search);
    }

}
