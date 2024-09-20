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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * ECPDS user persistence implementation.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.WeuCat;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.MasterException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser;
import ecmwf.web.home.users.UserByUidAndPass;
import ecmwf.web.home.users.UserHome;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.User;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.search.BooleanExpressionException;
import ecmwf.web.util.search.QueryExpressionParser;
import ecmwf.web.util.search.elements.EqualElement;

/**
 * The Class UserDAOHandler.
 */
public class UserDAOHandler extends PDSDAOBase implements DAOHandler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(UserDAOHandler.class);

    /**
     * Creates the.
     *
     * @return the web user
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public WebUser create() throws DAOException {
        return new UserBean(new ecmwf.common.database.WebUser());
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the web user
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public WebUser findByPrimaryKey(final String key) throws DAOException {
        try {
            return new UserBean(MasterManager.getDB().getWebUser(key));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Problem getting Web User " + key, e);
        }
    }

    /**
     * Find.
     *
     * @param search
     *            the search
     *
     * @return the collection
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public Collection<?> find(final ModelSearch search) throws DAOException {
        if (search instanceof final UserByUidAndPass s) {
            // Creating a "LOGIN" user
            try {
                final var session = MasterManager.getMI().getECpdsSession(s.getUid(), s.getPass(), s.getHost(),
                        s.getAgent(), s.getComment());
                final User user = new UserBean(session.getWebUser());
                user.setCredentials(session);
                final List<User> userHolder = new ArrayList<>(1);
                userHolder.add(user);
                return userHolder;
            } catch (MasterException | DataBaseException e) {
                throw new DAOException("Problem getting Web User " + s.getUid(), e);
            } catch (final RemoteException e) {
                throw new DAOException("Problem contacting authentication server for Web User " + s.getUid(), e);
            }
        }
        if (UserHome.ALL.equals(search.getQuery())) {
            try {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getWebUserArray());
            } catch (DataBaseException | RemoteException e) {
                throw new DAOException(e.getMessage(), e);
            }
        } else {
            try {
                final var bool = QueryExpressionParser.createExpression(search.getQuery());
                final Collection<?> leaves = bool.getLeaves();
                final HashMap<String, String> clauses = HashMap.newHashMap(leaves.size());
                final Iterator<?> i = leaves.iterator();
                while (i.hasNext()) {
                    final var e = (EqualElement) i.next();
                    clauses.put(e.getName(), e.getValue());
                }
                if (leaves.size() == 1) {
                    // Single clause expressions
                    if (clauses.containsKey("category")) {
                        final var categoryId = clauses.get("category");
                        try {
                            return convertCollectionToModelBeanCollection(
                                    MasterManager.getDB().getUsersPerCategoryId(categoryId));
                        } catch (DataBaseException | RemoteException e) {
                            throw new DAOException(e.getMessage(), e);
                        }
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
        }
    }

    /**
     * Save.
     *
     * @param b
     *            the b
     * @param context
     *            the context
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        final var user = (UserBean) b;
        final var id = user.getId();
        // The anonymous user is protected!
        if (Cnf.at("Server", "anonymousUser", "anonymous".equals(id))) {
            throw new DAOException("User " + id + " is protected");
        }
        try {
            final var session = Util.getECpdsSessionFromObject(context);
            MasterManager.getMI().saveWebUser(session,
                    (ecmwf.common.database.WebUser) ((OjbImplementedBean) b).getOjbImplementation());
            final var db = MasterManager.getDB();
            Collection<Category> c;
            if ((c = user.getAddedCategories()) != null) {
                for (final Category category : c) {
                    db.insert(session, new WeuCat(Long.parseLong(category.getId()), id), true);
                }
                log.debug("Added Categories " + user.getAddedCategories());
            }
            if ((c = user.getDeletedCategories()) != null) {
                for (final Category category : c) {
                    db.remove(session, db.getWeuCat(Long.parseLong(category.getId()), id));
                }
                log.debug("Deleted Categories " + user.getDeletedCategories());
            }
        } catch (final Exception e) {
            throw new DAOException("Problem updating associations for Web User " + id, e);
        }
    }

    /**
     * Insert.
     *
     * @param b
     *            the b
     * @param context
     *            the context
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public void insert(final ModelBean b, final Object context) throws DAOException {
        final var user = (UserBean) b;
        final var id = user.getId();
        // The anonymous user is protected!
        if (Cnf.at("Server", "anonymousUser", "anonymous".equals(id))) {
            throw new DAOException("User " + id + " is protected");
        }
        try {
            MasterManager.getMI().saveWebUser(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.WebUser) ((OjbImplementedBean) b).getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem creating Web User " + id, e);
        }
    }

    /**
     * Delete.
     *
     * @param b
     *            the b
     * @param context
     *            the context
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var user = (UserBean) b;
        final var id = user.getId();
        // The anonymous user is protected!
        if (Cnf.at("Server", "anonymousUser", "anonymous".equals(id))) {
            throw new DAOException("User " + id + " is protected");
        }
        try {
            MasterManager.getMI().removeWebUser(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.WebUser) user.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting Web User " + user.getId(), e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param users
     *            the users
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<WebUser> convertArrayToModelBeanCollection(
            final ecmwf.common.database.WebUser[] users) {
        final var length = users != null ? users.length : 0;
        final List<WebUser> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new UserBean(users[i]));
        }
        return results;
    }

    /**
     * Convert collection to model bean collection.
     *
     * @param users
     *            the users
     *
     * @return the collection
     */
    private static final Collection<WebUser> convertCollectionToModelBeanCollection(
            final Collection<ecmwf.common.database.WebUser> users) {
        final List<WebUser> results = new ArrayList<>(users.size());
        for (final ecmwf.common.database.WebUser user : users) {
            results.add(new UserBean(user));
        }
        return results;
    }
}
