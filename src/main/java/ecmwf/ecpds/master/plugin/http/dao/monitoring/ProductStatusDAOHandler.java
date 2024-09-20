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
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;
import ecmwf.web.dao.ReadOnlyDAOHandler;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.cache.CacheService;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.bean.Pair;

/**
 * The Class ProductStatusDAOHandler.
 */
public class ProductStatusDAOHandler extends ReadOnlyDAOHandler implements DAOHandler {

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
     * This key is defined in "ProductStatusHome".
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
        final var tok = new StringTokenizer(key, "@");
        if (tok.countTokens() > 1) {
            final var product = tok.nextToken();
            final var time = tok.nextToken();
            return new ProductStatusBean(product, time, 0, false);
        }
        throw new DAOException("Key expected as 'product@time'. Got '" + key + "'  instead.");
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
        if ("NameTimePair".equals(search.getQuery())) {
            try {
                final List<Pair> results = new ArrayList<>();
                final var klass = ProductStatus.class.getName();
                final Iterator<ProductStatus> i = CacheService.getValuesIterator(klass);
                while (i.hasNext()) {
                    final var ps = i.next();
                    results.add(new Pair(ps.getProduct(), ps.getTime()));
                }
                return results;
            } catch (final Exception e) {
                throw new DAOException("Problem getting keys", e);
            }
        }
        if ("All".equals(search.getQuery())) {
            try {
                final List<ProductStatus> results = new ArrayList<>();
                final var klass = ProductStatus.class.getName();
                final Iterator<ProductStatus> i = CacheService.getValuesIterator(klass);
                while (i.hasNext()) {
                    results.add(i.next());
                }
                return results;
            } catch (final Exception e) {
                throw new DAOException("Problem getting all ProductStatus", e);
            }
        } else if ("AllCalculated".equals(search.getQuery())) {
            try {
                final List<ProductStatus> results = new ArrayList<>();
                final var klass = ProductStatus.class.getName();
                final Iterator<ProductStatus> i = CacheService.getValuesIterator(klass);
                while (i.hasNext()) {
                    final var status = i.next();
                    if (status.isCalculated()) {
                        results.add(status);
                    }
                }
                return results;
            } catch (final Exception e) {
                throw new DAOException("Problem getting all ProductStatus", e);
            }
        } else {
            throw new DAOException("Unsupported query. Key: " + search.getKey());
        }
    }
}
