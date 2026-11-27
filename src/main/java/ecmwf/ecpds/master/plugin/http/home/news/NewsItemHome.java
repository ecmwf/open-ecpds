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
import java.util.StringTokenizer;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.ModelSortItem;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.model.news.NewsObjectNotFoundException;

import ecmwf.ecpds.master.plugin.http.services.persistence.DAOObjectNotFoundException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;

/**
 * Factory that allows us to get to NewsItem instances.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla </a>
 *
 * @version 0.1.0
 */

public class NewsItemHome extends ModelHomeBase {

    private static final String INTERFACE = NewsItem.class.getName();

    public static NewsItem create() throws NewsException {

        try {
            return (NewsItem) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new NewsException("Error creating object", e);
        }

    }

    public static NewsItem findByPrimaryKey(String key) throws NewsException {

        try {
            return (NewsItem) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOObjectNotFoundException e) {
            throw new NewsObjectNotFoundException(key, INTERFACE);
        } catch (DAOException e) {
            throw new NewsException("Error retrieving object", e);
        }

    }

    public static NewsItem findByExternalIdAndChannelId(String externalId, String channelId) throws NewsException {

        ModelSearch search = getDefaultSearch("external_id=\"" + externalId + "\" and channel_id=" + channelId);
        Collection results = find(search);
        if (results != null && results.size() == 1)
            return (NewsItem) results.iterator().next();
        else
            throw new NewsObjectNotFoundException("External Id: " + externalId + ", Channel Id" + channelId, INTERFACE);

    }

    public static NewsItem findByNameAndChannelName(String name, String channelName) throws NewsException {
        NewsChannel c = NewsChannelHome.findByName(channelName);
        return NewsItemHome.findByNameAndChannelId(name, c.getId());
    }

    public static NewsItem findByNameAndChannelId(String name, String channelId) throws NewsException {
        return findByNameAndChannelId(name, channelId, true);
    }

    public static NewsItem findByNameAndChannelId(String name, String channelId, boolean useCache)
            throws NewsException {

        ModelSearch search = getDefaultSearch("name=\"" + name + "\" and channel_id=" + channelId);
        search.setCacheable(useCache);
        Collection results = find(search);
        if (results != null && results.size() == 1)
            return (NewsItem) results.iterator().next();
        else
            throw new NewsObjectNotFoundException("Name '" + name + "', Channel Id '" + channelId + "'", INTERFACE);

    }

    public static NewsItem findByQualifiedName(String name, String defaultChannelId) throws NewsException {

        try {
            return findByQualifiedName(name);
        } catch (NewsException e) {
            return NewsItemHome.findByNameAndChannelId(name, defaultChannelId);
        }

    }

    public static NewsItem findByQualifiedName(String name) throws NewsException {

        if (name.indexOf(NewsChannel.CHANNEL_QUALIFIER) > 0) {
            // It is a qualified name channel::item
            StringTokenizer tok = new StringTokenizer(name, NewsChannel.CHANNEL_QUALIFIER);
            String channelName = tok.nextToken();
            String itemName = tok.nextToken();
            NewsChannel channel = NewsChannelHome.findByName(channelName);
            return NewsItemHome.findByNameAndChannelId(itemName, channel.getId());

        } else {
            throw new NewsException("A qualifed name should be 'channel" + NewsChannel.CHANNEL_QUALIFIER + "item'");
        }

    }

    public static Collection findByChannelId(String channelId) throws NewsException {
        ModelSearch search = getDefaultSearch("channel_id=" + channelId);
        search.setSizeLimit(Integer.MAX_VALUE);
        return find(search);
    }

    public static Collection findByChannelIdSortedBy(String channelId, Collection c) throws NewsException {
        ModelSearch search = getDefaultSearch("channel_id=" + channelId);
        search.setSizeLimit(Integer.MAX_VALUE);
        search.resetSortItems();
        search.addSortItems(c);
        return find(search);
    }

    public static Collection findByParent(NewsItem item) throws NewsException {
        ModelSearch search = getDefaultSearch("parent_id=" + item.getId());
        search.setSizeLimit(Integer.MAX_VALUE);
        return find(search);
    }

    public static Collection findAll() throws NewsException {
        ModelSearch search = getDefaultSearch("true");
        search.setSizeLimit(Integer.MAX_VALUE);
        search.setCacheable(false);
        return find(search);
    }

    public static Collection find(ModelSearch search) throws NewsException {

        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new NewsException("Error retrieving object set", e);
        }

    }

    public static ModelSearch getDefaultSearch(String query) {

        ModelSearch search = ModelHomeBase.getDefaultSearch(query);
        search.addSortItem(new ModelSortItem("referenceDate", ModelSortItem.DESCENDING));
        search.setScope(ModelSearch.ONELEVEL_SCOPE);
        return search;

    }

} // NewsItemHome
