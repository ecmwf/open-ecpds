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
 * Persistence handler for the PDS categories.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ecmwf.common.database.CatUrl;
import ecmwf.common.database.Category;
import ecmwf.common.database.DataBaseException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.model.users.Resource;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.search.BooleanExpressionException;
import ecmwf.web.util.search.QueryExpressionParser;
import ecmwf.web.util.search.elements.EqualElement;

/**
 * The Class CategoryDAOHandler.
 */
public class CategoryDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public ModelBean create() throws DAOException {
        return new CategoryBean(new Category());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        try {
            return new CategoryBean(MasterManager.getDB().getCategory(Long.parseLong(key)));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Error getting category with key '" + key + "'", e);
        } catch (final NumberFormatException e) {
            throw new DAOException("The key '" + key + "' is not a Long", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<CategoryBean> find(final ModelSearch search) throws DAOException {
        try {
            if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getCategoryArray());
            }
            try {
                // Maybe a BooleanExpression2SQL translator should be
                // developed later on.....
                final var bool = QueryExpressionParser.createExpression(search.getQuery());
                final Collection<?> leaves = bool.getLeaves();
                final var clauses = new HashMap<String, String>(leaves.size());
                final Iterator<?> i = leaves.iterator();
                while (i.hasNext()) {
                    final var e = (EqualElement) i.next();
                    clauses.put(e.getName(), e.getValue());
                }
                if (leaves.size() == 1) {
                    // Single clause expressions
                    if (clauses.containsKey("user")) {
                        return convertToModelBeanCollection(
                                MasterManager.getDB().getCategoriesPerUserId(clauses.get("user").toString()));
                    } else if (clauses.containsKey("resource")) {
                        return convertToModelBeanCollection(MasterManager.getDB().getCategoriesPerResourceId(
                                clauses.get("resource").toString().replace(ResourceBean.USE_INSTEAD_OF_SLASHES, '/')));
                    } else {
                        throw new DAOException("Search by attribute '" + clauses.keySet() + "' not supported");
                    }
                } else {
                    throw new DAOException(
                            "'find' method with query '" + search.getQuery() + "' not supported! Use simple queries");
                }
            } catch (final BooleanExpressionException e) {
                throw new DAOException("Bad find expression: " + search.getQuery(), e);
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
        super.save(b, context); // Save ordinary fields.
        final var cat = (CategoryBean) b;
        // Now save associations.
        try {
            final var db = MasterManager.getDB();
            final var session = Util.getECpdsSessionFromObject(context);
            for (final Resource resource : cat.getAddedResources()) {
                db.insert(session, new CatUrl(Long.parseLong(cat.getId()), resource.getPath()), true);
            }
            for (final Resource resource : cat.getAddedResources()) {
                db.remove(session, db.getCatUrl(cat.getIntId(), resource.getPath()));
            }
        } catch (final Exception e) {
            throw new DAOException("Error handling associations for Category '" + cat.getName() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var tm = (CategoryBean) b;
        try {
            final var mi = MasterManager.getMI();
            mi.removeCategory(Util.getECpdsSessionFromObject(context), (Category) tm.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting WebUser '" + tm.getName() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param cats
     *            the cats
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<CategoryBean> convertArrayToModelBeanCollection(final Category[] cats) {
        final var length = cats != null ? cats.length : 0;
        final List<CategoryBean> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new CategoryBean(cats[i]));
        }
        return results;
    }

    /**
     * Convert to model bean collection.
     *
     * @param cats
     *            the cats
     *
     * @return the collection
     */
    private static final Collection<CategoryBean> convertToModelBeanCollection(final Collection<Category> cats) {
        final List<CategoryBean> l = new ArrayList<>(cats.size());
        final var i = cats.iterator();
        while (i.hasNext()) {
            l.add(new CategoryBean(i.next()));
        }
        return l;
    }
}
