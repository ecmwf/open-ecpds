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

package ecmwf.ecpds.master.plugin.http.dao.monitoring;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.ProductStatus;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.StringAsLongComparator;
import ecmwf.ecpds.master.plugin.http.home.monitoring.CleanProductSearch;
import ecmwf.ecpds.master.plugin.http.home.monitoring.GetProductStepStatus;
import ecmwf.ecpds.master.plugin.http.home.monitoring.GetProductStepStatusHistory;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStepStatus;
import ecmwf.web.dao.ReadOnlyDAOHandler;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.cache.CacheException;
import ecmwf.web.services.cache.CacheService;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class ProductStepStatusDAOHandler.
 */
public class ProductStepStatusDAOHandler extends ReadOnlyDAOHandler implements DAOHandler {

    /**
     * Creates the.
     *
     * @return the model bean
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public ModelBean create() throws DAOException {
        throw new DAOException("Method not supported");
    }

    /**
     * This method should only be called when the CacheService entry is empty, like with every other Bean. The
     * difference here is that the Cache will be filled externally by MonitoringStatusCalculatorTask, so the bean
     * provided here is only intended to stand as a "provisional" value.
     *
     * This key is defined in "ProductStepStatusHome".
     *
     * @param key
     *            the key
     *
     * @return the model bean
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        try {
            final var tok = new StringTokenizer(key, "@");
            if (tok.countTokens() > 3) {
                final var product = tok.nextToken();
                final var time = tok.nextToken();
                final var buffer = tok.nextToken();
                final var step = tok.nextToken();
                final var type = tok.nextToken();
                return new ProductStepStatusBean(product, time, Long.parseLong(buffer), Long.parseLong(step), type,
                        false);
            }
            throw new DAOException("Key expected as 'product@time@buffer@step@type'. Invalid key '" + key + "'");
        } catch (final NumberFormatException e) {
            throw new DAOException("Bad Number for step (key: " + key + ")", e);
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
        try {
            if (search instanceof final CleanProductSearch cps) {
                cleanSteps(cps.getProduct(), cps.getTime());
                return new ArrayList<>(0);
            }
            if (search instanceof final GetProductStepStatusHistory s) {
                final var ps = MasterManager.getDB().getProductStatus(s.getProduct(), s.getTime(), s.getType(),
                        s.getStep(), s.getLimit());
                return convertArrayToBeanCollection(ps);
            } else if (search instanceof final GetProductStepStatus s) {
                try {
                    final Iterator<?> keys = CacheService.getKeysIterator(ProductStepStatus.class.getName());
                    final List<ProductStepStatus> result = new ArrayList<>();
                    final var product = s.getProduct();
                    final var time = s.getTime();
                    while (keys.hasNext()) {
                        final var key = keys.next().toString();
                        if (key.startsWith(product + "@" + time + "@")) {
                            result.add((ProductStepStatus) CacheService.get(ProductStepStatus.class.getName(), key));
                        }
                    }
                    Collections.sort(result, new ProductStepStatusComparator());
                    return result;
                } catch (final CacheException e) {
                    throw new DAOException("Problem getting keys", e);
                }
            } else if ("names".equals(search.getQuery())) {
                try {
                    final Iterator<?> keys = CacheService.getKeysIterator(ProductStepStatus.class.getName());
                    final var result = new HashMap<String, String>();
                    while (keys.hasNext()) {
                        result.put(getStep(keys.next().toString()), "");
                    }
                    final List<String> sorted = new ArrayList<>(result.keySet());
                    Collections.sort(sorted, new StringAsLongComparator());
                    return sorted;
                } catch (final CacheException e) {
                    throw new DAOException("Problem getting keys", e);
                }
            } else if ("all".equals(search.getQuery())) {
                try {
                    final Iterator<?> values = CacheService.getValuesIterator(ProductStepStatus.class.getName());
                    final Collection<Object> result = new ArrayList<>();
                    while (values.hasNext()) {
                        result.add(values.next());
                    }
                    return result;
                } catch (final CacheException e) {
                    throw new DAOException("Problem getting keys", e);
                }
            } else {
                throw new DAOException("Unsupported query. Key: " + search.getKey());
            }
        } catch (final DataBaseException e) {
            throw new DAOException("Problem getting ProductStepStatus from Database", e);
        } catch (final RemoteException e) {
            throw new DAOException("Remote Problem getting ProductStepStatus", e);
        }
    }

    /**
     * Clean steps.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @throws DAOException
     *             the DAO exception
     */
    private static final void cleanSteps(final String product, final String time) throws DAOException {
        try {
            final var klass = ProductStepStatus.class.getName();
            final Iterator<?> i = CacheService.getKeysIterator(klass);
            while (i.hasNext()) {
                final var key = i.next().toString();
                final var pss = (ProductStepStatus) CacheService.get(klass, key);
                if (product.equals(pss.getProduct()) && time.equals(pss.getTime())) {
                    // Completely DELETE this entry, the same step might not be available for next
                    // iteration pss.reset();
                    CacheService.remove(klass, key);
                }
            }
        } catch (final Exception e) {
            throw new DAOException("Problem cleaning ProductStatus", e);
        }
    }

    /**
     * Gets the step.
     *
     * @param key
     *            the key
     *
     * @return the step
     */
    private static final String getStep(final String key) {
        final var tok = new StringTokenizer(key, "@");
        if (tok.countTokens() > 2) {
            tok.nextToken();
            tok.nextToken();
            return tok.nextToken();
        }
        return key;
    }

    /**
     * Convert array to bean collection.
     *
     * @param ps
     *            the ps
     *
     * @return the collection
     */
    private static final Collection<ProductStepStatusBean> convertArrayToBeanCollection(final ProductStatus[] ps) {
        final List<ProductStepStatusBean> l = new ArrayList<>(ps.length);
        for (final ProductStatus p : ps) {
            final var pssb = new ProductStepStatusBean(p.getStream(), p.getTime(), p.getBuffer(), p.getStep(),
                    p.getType(), true);
            pssb.setLastUpdate(p.getLastUpdate());
            pssb.setProductTime(p.getTimeBase());
            pssb.setScheduledTime(p.getScheduleTime());
            pssb.setGenerationStatusFromCode(p.getStatusCode());
            l.add(pssb);
        }
        return l;
    }
}
