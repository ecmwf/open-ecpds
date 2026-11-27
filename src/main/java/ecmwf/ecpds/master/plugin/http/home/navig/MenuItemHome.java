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

package ecmwf.ecpds.master.plugin.http.home.navig;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.navig.MenuException;
import ecmwf.ecpds.master.plugin.http.model.navig.MenuItem;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;

/**
 * Factory that allows us to get to MenuItem instances.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla </a>
 *
 * @version 0.1.0
 */

public class MenuItemHome extends ModelHomeBase {

    private static Log log = LogFactory.getLog(MenuItemHome.class);
    private static final String INTERFACE = MenuItem.class.getName();

    public static MenuItem create() throws MenuException {

        try {
            return (MenuItem) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new MenuException("Error creating object: ", e);
        }

    }

    public static MenuItem findByPrimaryKey(String key) throws MenuException {

        try {
            return (MenuItem) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOException e) {
            throw new MenuException("Object not found or Error retrieving object with key '" + key + "'", e);
        }

    }

    public static MenuItem findByResourcePath(String path) throws MenuException {

        try {
            ModelSearch search = getDefaultSearch("path=\"" + path + "\"");
            Collection c = DAOService.find(INTERFACE, search);
            if (c != null && c.size() == 1)
                return (MenuItem) c.iterator().next();
            else
                throw new MenuException("No MenuItem for Resource with path '" + path + "'.");
        } catch (DAOException e) {
            throw new MenuException("Error looking for MenuItem for a Resource: ", e);
        }

    }

    /**
     * Returns the MenuItem which probably governs this path. If a full URL is supplied, it will ignore the host bit and
     * just use the path applied to the current host Menu Directory.
     *
     * @param path
     *            The path of the resource or its URL
     *
     * @return The authoritive <code>MenuItem</code>
     */

    public static MenuItem findAuthoritativeByResourceUrl(String url) throws MenuException {

        String path = null;
        int pos = 0;
        if ((url.startsWith("http://") || url.startsWith("https://")) && (pos = url.indexOf("/", 8)) > 0)
            path = url.substring(pos, url.length());
        else
            path = url;

        String tmpPath = path.replaceAll("//", "/");

        while (tmpPath.length() > 0) {

            try {

                if (log.isDebugEnabled())
                    log.debug("MenuItem.findAuthoritative(" + path + "): Trying path " + tmpPath);
                ModelSearch search = MenuItemHome.getDefaultSearch("path='" + tmpPath + "'");
                Collection c = DAOService.find(INTERFACE, search);

                if (c.size() == 1) {
                    MenuItem item = (MenuItem) c.iterator().next();
                    String host = item.getHost();
                    if (host == null || "".equals(host) || "local".equals(host))
                        return item;

                } else if (c.size() > 1) {

                    if (log.isDebugEnabled())
                        log.debug("Expecting one entry for path '" + tmpPath + "'. Found " + c.size()
                                + ". Will return first one belonging to this host");
                    Iterator i = c.iterator();
                    while (i.hasNext()) {
                        MenuItem item = (MenuItem) i.next();
                        String host = item.getHost();
                        if (host == null || "".equals(host) || "local".equals(host)) {
                            if (log.isDebugEnabled())
                                log.debug("Chosen '" + item.getId() + "'");
                            return item;
                        }
                    }

                }

            } catch (DAOException e) {
                log.warn("Problem getting MenuItem for path '" + tmpPath + "'", e);
            }

            // If not keep on taking out chunks of the path

            if (tmpPath.endsWith("/")) {
                // Lets try without the trailing slash.
                tmpPath = tmpPath.substring(0, tmpPath.length() - 1);
            } else if (tmpPath.lastIndexOf("?") > 0) {
                // Lets try without the query bit.
                tmpPath = tmpPath.substring(0, tmpPath.lastIndexOf("?"));
            } else {
                // Lets try removing one path component.
                pos = tmpPath.substring(0, tmpPath.length() - 1).lastIndexOf('/');
                if (pos < 0)
                    throw new MenuException("MenuItemHome: The path '" + tmpPath + "' seems to lack some slashes (/).");
                else
                    tmpPath = tmpPath.substring(0, pos + 1);
            }

        }

        throw new MenuException("No resource found for path in findAuthoritativeByResourcePath(" + path + ").");

    }

    public static Collection find(ModelSearch search) throws MenuException {

        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new MenuException("Error retrieving object set: ", e);
        }

    }

    public static Collection findAll() throws MenuException {
        return find(new AllMenuItemsSearch());
    }

    public static ModelSearch getDefaultSearch(String query) {

        ModelSearch search = ModelHomeBase.getDefaultSearch(query);
        // search.addSortItem(new ModelSortItem("referenceDate",
        // ModelSortItem.DESCENDING));
        search.setScope(ModelSearch.SUBTREE_SCOPE);
        return search;

    }

    /*
     *
     * public static void main(String args[]) throws ModelException, ConfigException {
     * ConfigService.init("/var/tmp/sy8/devel/workspace/dynamicweb/webapp/", "WEB-INF/config");
     * equalizeMenuItemChildOrders(MenuItemHome.findByPrimaryKey(ConfigService.getParameter("directory",
     * "dn.website.top"))); }
     *
     * private static void equalizeMenuItemChildOrders(MenuItem parent) throws ModelException {
     *
     * Iterator c = parent.getChildren().iterator(); int counter = 1; while (c.hasNext()) { MenuItem item = (MenuItem)
     * c.next(); item.setOrder(counter); System.out.println("Saving ITEM "+item.getId() + " order " + counter);
     * equalizeMenuItemChildOrders(item); if (item.getHost() == null) { ((Resource) item).setHost("local");
     * System.out.println(item.getId() + " setting host!!!! "); } if (item.getDescription() == null ||
     * "".equals(item.getDescription())) { item.setDescription(item.getText()); System.out.println(item.getId() +
     * " setting description!!!! "); }
     *
     * item.save(); equalizeRelatedMenuItems(item);
     *
     * counter++; }
     *
     * }
     *
     * private static void equalizeRelatedMenuItems(MenuItem parent) throws ModelException {
     *
     * Collection c = parent.getLocalRelated(); int counter = c.size(); Iterator i = c.iterator(); while (i.hasNext()) {
     * RelatedMenuItem related = (RelatedMenuItem) i.next(); related.setOrder(counter);
     * System.out.println("Saving RELATED '" + related.getId() + "' with order " + counter); try { related.save(); }
     * catch (Throwable e) { System.out.println("Problem saving RelatedMenu:" + e.getMessage());
     *
     * System.out.println("Deleting RelatedMenu:" + related.getId()); //related.delete(); } counter--; }
     *
     * }
     */

} // MenuItemHome
