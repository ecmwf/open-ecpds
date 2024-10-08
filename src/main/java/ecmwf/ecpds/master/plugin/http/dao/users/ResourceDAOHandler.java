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

package ecmwf.ecpds.master.plugin.http.dao.users;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Persistence handler for PDS resources.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.CatUrl;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.Url;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebResource;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.model.users.Category;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.search.BooleanExpressionException;

/**
 * The Class ResourceDAOHandler.
 */
public class ResourceDAOHandler extends PDSDAOBase implements DAOHandler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ResourceDAOHandler.class);

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public WebResource create() throws DAOException {
        return new ResourceBean(new Url());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public WebResource findByPrimaryKey(final String key) throws DAOException {
        return findAuthoritative(
                (key.startsWith("ecpds:") ? key.substring(6) : key).replace(ResourceBean.USE_INSTEAD_OF_SLASHES, '/'));
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<WebResource> find(final ModelSearch search) throws DAOException {
        try {
            if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getUrlArray());
            }
            try {
                final var clauses = getEqualityClauseLeavesFromExpression(search);
                if (clauses.size() == 1) {
                    // Single clause expressions
                    if (clauses.containsKey("category")) {
                        return convertToModelBeanCollection(
                                MasterManager.getDB().getUrlsPerCategoryId(clauses.get("category")));
                    } else {
                        throw new DAOException("Search by attribute '" + clauses.keySet() + "' not supported");
                    }
                } else if (clauses.size() == 2) {
                    if (clauses.containsKey("server") && "ecpds".equals(clauses.get("server"))
                            && clauses.containsKey("location")) {
                        final List<WebResource> results = new ArrayList<>(1);
                        try {
                            results.add(new ResourceBean(MasterManager.getDB().getUrl(clauses.get("location"))));
                        } catch (final DataBaseException e) {
                            // Not found. Since this is a "find" method, we
                            // don't propagate the error, just return the
                            // empty collection;
                        }
                        return results;
                    } else {
                        throw new DAOException("Search by attribute '" + clauses.keySet() + "' not supported");
                    }
                } else {
                    throw new DAOException(
                            "'find' method with query '" + search.getQuery() + "' not supported! Use simple queries");
                }
            } catch (final BooleanExpressionException e) {
                throw new DAOException("Bad find expression", e);
            } catch (final ClassCastException e) {
                throw new DAOException("'find' method with query '" + search.getQuery()
                        + "' not supported! Only equality (=) operators expected");
            }
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        final var res = (ResourceBean) b;
        try {
            final var session = Util.getECpdsSessionFromObject(context);
            super.save(b, context); // Save ordinary fields.
            final var db = MasterManager.getDB();
            for (final Category category : res.getAddedCategories()) {
                db.insert(session, new CatUrl(Long.parseLong(category.getId()), res.getPath()), true);
            }
            for (final Category category : res.getDeletedCategories()) {
                db.remove(session, db.getCatUrl(Long.parseLong(category.getId()), res.getPath()));
            }
        } catch (final Exception e) {
            throw new DAOException("Error saving Resource '" + res.getPath() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var tm = (ResourceBean) b;
        try {
            MasterManager.getMI().removeUrl(Util.getECpdsSessionFromObject(context), (Url) tm.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting Resource '" + tm.getId() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param urls
     *            the urls
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<WebResource> convertArrayToModelBeanCollection(final Url[] urls) {
        final var length = urls != null ? urls.length : 0;
        final List<WebResource> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new ResourceBean(urls[i]));
        }
        return results;
    }

    /**
     * Convert to model bean collection.
     *
     * @param urls
     *            the urls
     *
     * @return the collection
     */
    private static final Collection<WebResource> convertToModelBeanCollection(final Collection<Url> urls) {
        final List<WebResource> list = new ArrayList<>(urls.size());
        for (final Url url : urls) {
            list.add(new ResourceBean(url));
        }
        return list;
    }

    /**
     * Find authoritative.
     *
     * @param path
     *            the path
     *
     * @return the web resource
     *
     * @throws ecmwf.web.services.persistence.DAOException
     *             the DAO exception
     */
    public static final WebResource findAuthoritative(final String path) throws DAOException {
        final CatUrl[] catUrls;
        try {
            // Let's get all the categories per url at once!
            catUrls = MasterManager.getDB().getCatUrlArray();
        } catch (final Throwable t) {
            throw new DAOException(t.getMessage(), t);
        }
        var tmpPath = path.indexOf("?") > 0 ? path.substring(0, path.indexOf("?")) : path;
        while (tmpPath.length() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("findAuthoritative(" + path + "): Trying path " + tmpPath);
            }
            try {
                return new ResourceBean(getUrl(catUrls, tmpPath));
            } catch (final DataBaseException e) {
                final var pos = tmpPath.substring(0, tmpPath.length() - 1).lastIndexOf('/');
                if (pos < 0) {
                    throw new DAOException(
                            "Didn't find an authoritative path for '" + path + "'. Last tried was '" + tmpPath + "'.");
                }
                tmpPath = tmpPath.substring(0, pos + 1);
            }
        }
        throw new DAOException("No resource found for path in findAuthoritative(" + path + ").");
    }

    /**
     * Gets the url.
     *
     * @param catUrls
     *            the cat urls
     * @param name
     *            the name
     *
     * @return the url
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static final Url getUrl(final CatUrl[] catUrls, final String name) throws DataBaseException {
        for (final CatUrl catUrl : catUrls) {
            final var url = catUrl.getUrl();
            if (name.equals(url.getName())) {
                return url;
            }
        }
        throw new DataBaseException("Url " + name + " not found");
    }
}
