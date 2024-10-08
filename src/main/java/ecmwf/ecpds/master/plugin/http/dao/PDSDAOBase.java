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

package ecmwf.ecpds.master.plugin.http.dao;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Since all the save, insert and delete methods are similar I implemented them here.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.util.bean.Pair;
import ecmwf.web.util.search.BooleanExpressionException;
import ecmwf.web.util.search.QueryExpressionParser;
import ecmwf.web.util.search.elements.EqualElement;

/**
 * The Class PDSDAOBase.
 */
public class PDSDAOBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(PDSDAOBase.class);

    /**
     * Insert.
     *
     * @param bean
     *            the bean
     *
     * @throws ecmwf.web.services.persistence.DAOException
     *             the DAO exception
     */
    public void insert(final ModelBean bean) throws DAOException {
        throw new DAOException("Not supported!");
    }

    /**
     * Save.
     *
     * @param bean
     *            the bean
     *
     * @throws ecmwf.web.services.persistence.DAOException
     *             the DAO exception
     */
    public void save(final ModelBean bean) throws DAOException {
        throw new DAOException("Not supported!");
    }

    /**
     * Delete.
     *
     * @param bean
     *            the bean
     *
     * @throws ecmwf.web.services.persistence.DAOException
     *             the DAO exception
     */
    public void delete(final ModelBean bean) throws DAOException {
        throw new DAOException("Not supported!");
    }

    /**
     * Save.
     *
     * @param bean
     *            the bean
     * @param context
     *            the context
     *
     * @throws ecmwf.web.services.persistence.DAOException
     *             the DAO exception
     */
    public void save(final ModelBean bean, final Object context) throws DAOException {
        try {
            MasterManager.getDB().update(Util.getECpdsSessionFromObject(context),
                    ((OjbImplementedBean) bean).getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem saving object '" + bean.getId() + "'", e);
        }
    }

    /**
     * Insert.
     *
     * @param bean
     *            the bean
     * @param context
     *            the context
     *
     * @throws ecmwf.web.services.persistence.DAOException
     *             the DAO exception
     */
    public void insert(final ModelBean bean, final Object context) throws DAOException {
        try {
            MasterManager.getDB().insert(Util.getECpdsSessionFromObject(context),
                    ((OjbImplementedBean) bean).getOjbImplementation(), true);
        } catch (final Exception e) {
            throw new DAOException("Problem inserting object '" + bean.getId() + "'", e);
        }
    }

    /**
     * Delete.
     *
     * @param bean
     *            the bean
     * @param context
     *            the context
     *
     * @throws ecmwf.web.services.persistence.DAOException
     *             the DAO exception
     */
    public void delete(final ModelBean bean, final Object context) throws DAOException {
        try {
            MasterManager.getDB().remove(Util.getECpdsSessionFromObject(context),
                    ((OjbImplementedBean) bean).getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting object '" + bean.getId() + "'", e);
        }
    }

    /**
     * Gets the equality clause leaves from expression.
     *
     * @param search
     *            the search
     *
     * @return the equality clause leaves from expression
     *
     * @throws ecmwf.web.util.search.BooleanExpressionException
     *             the boolean expression exception
     * @throws java.lang.ClassCastException
     *             the class cast exception
     */
    public static final Map<String, String> getEqualityClauseLeavesFromExpression(final ModelSearch search)
            throws BooleanExpressionException, ClassCastException {
        final var bool = QueryExpressionParser.createExpression(search.getQuery());
        final Collection<EqualElement> leaves = bool.getLeaves();
        final var clauses = new HashMap<String, String>(leaves.size());
        for (final EqualElement e : leaves) {
            clauses.put(e.getName(), e.getValue());
        }
        return clauses;
    }

    /**
     * Get a collection of collections and convert it to collection of pairs, easier to handle.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    public static final Collection<Pair> convertCollectionsToObjectPairs(final Collection<?> c) {
        final List<Pair> results = new ArrayList<>(c.size());
        for (final Object element : c) {
            if (element instanceof final Collection<?> col) {
                if (col.size() == 2) {
                    final Iterator<?> j = col.iterator();
                    results.add(new Pair(j.next(), j.next()));
                } else if (col.size() == 3) {
                    final Iterator<?> j = col.iterator();
                    results.add(new Pair(new Pair(j.next(), j.next()), j.next()));
                } else {
                    log.error("Unsupported Collection to ObjectPair conversion for a Collection of size " + c.size());
                }
            }

        }
        return results;
    }

    /**
     * Gets the base date.
     *
     * @param d
     *            the d
     * @param offsetDays
     *            the offset days
     *
     * @return the base date
     */
    public static final Date getBaseDate(final Date d, final int offsetDays) {
        final var c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DATE, offsetDays);
        return c.getTime();
    }
}
