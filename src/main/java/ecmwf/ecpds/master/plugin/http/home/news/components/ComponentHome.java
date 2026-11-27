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

package ecmwf.ecpds.master.plugin.http.home.news.components;

/**
 * @author sy8
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

import java.util.Collection;
import java.util.StringTokenizer;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.home.news.NewsItemHome;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.model.news.components.Component;
import ecmwf.ecpds.master.plugin.http.model.news.components.ComponentException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;

public class ComponentHome extends ModelHomeBase {

    private static final String INTERFACE = Component.class.getName();

    public static Component create() throws ComponentException {

        try {
            return (Component) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new ComponentException("Error creating object set: ", e);
        }

    }

    public static Component findByNameAndItemId(String name, String itemId) throws ComponentException {

        ModelSearch search = getDefaultSearch("name='" + name + "' and item_id=" + itemId);
        Collection results = find(search);
        if (results != null && results.size() == 1)
            return (Component) results.iterator().next();
        else
            throw new ComponentException("External Id search DIDN'T return ONE element but " + results);

    }

    public static Component findByQualifiedName(String name) throws ComponentException {

        StringTokenizer t = new StringTokenizer(name, NewsChannel.CHANNEL_QUALIFIER);
        if (t.countTokens() == 3) {
            String channelName = t.nextToken();
            String itemName = t.nextToken();
            String componentName = t.nextToken();

            try {
                NewsItem item = NewsItemHome.findByNameAndChannelName(itemName, channelName);
                return item.getComponentByName(componentName);
            } catch (NewsException e) {
                throw new ComponentException(
                        "Problem finding the Item to get a Component from. Qualified name '" + name + "'", e);
            }

        } else
            throw new ComponentException(
                    "Expecting 3 name components divided by '" + NewsChannel.CHANNEL_QUALIFIER + "'. Got " + name);

    }

    public static Collection findByNewsItem(NewsItem item) throws ComponentException {
        return findByNewsItemId(item.getId());
    }

    public static Collection findByNewsItemId(String id) throws ComponentException {

        ModelSearch search = getDefaultSearch("item_id='" + id + "'");
        return find(search);

    }

    public static Collection findAll() throws ComponentException {

        ModelSearch search = getDefaultSearch("true");
        search.setCacheable(false);
        search.setSizeLimit(Integer.MAX_VALUE);
        return find(search);
    }

    public static Component findByPrimaryKey(String key) throws ComponentException {
        try {
            return (Component) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOException e) {
            throw new ComponentException("Error retrieving object set: ", e);
        }
    }

    public static Collection find(ModelSearch search) throws ComponentException {

        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new ComponentException("Error retrieving object set: ", e);
        }

    }

    public static Collection findByItemIdSortedBy(String itemId, Collection c) throws ComponentException {
        ModelSearch search = getDefaultSearch("item_id=" + itemId);
        search.setSizeLimit(Integer.MAX_VALUE);
        search.resetSortItems();
        search.addSortItems(c);
        return find(search);
    }

}
