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

import ecmwf.ecpds.master.plugin.http.home.monitoring.CleanProductSearch;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.web.dao.ReadOnlyDAOHandler;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.cache.CacheService;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class DestinationProductStatusDAOHandler.
 */
public class DestinationProductStatusDAOHandler extends ReadOnlyDAOHandler implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public ModelBean create() throws DAOException {
        throw new DAOException("Method not supported");
    }

    /**
     * {@inheritDoc}
     *
     * This method should only be called when the CacheService entry is empty, like with every other Bean. The
     * difference here is that the Cache will be filled externally by MonitoringStatusCalculatorTask, so the bean
     * provided here is only intended to stand as a "provisional" value.
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        final var token = new StringTokenizer(key, "@");
        if (token.countTokens() > 2) {
            final var destination = token.nextToken();
            final var product = token.nextToken();
            final var time = token.nextToken();
            return new DestinationProductStatusBean(destination, product, time, false);
        }
        throw new DAOException("Key expected as 'destination@product@time'. Got '" + key + "'  instead.");
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<?> find(final ModelSearch search) throws DAOException {
        if (search instanceof final CleanProductSearch cleanProductSearch) {
            cleanDestinations(cleanProductSearch.getProduct(), cleanProductSearch.getTime());
            return new ArrayList<>(0);
        }
        if (!"All".equals(search.getQuery())) {
            throw new DAOException("Search not supported: " + search);
        }
        try {
            final List<Object> results = new ArrayList<>();
            final Iterator<?> i = CacheService.getValuesIterator(DestinationProductStatus.class.getName());
            while (i.hasNext()) {
                results.add(i.next());
            }
            return results;
        } catch (final Exception e) {
            throw new DAOException("Problem getting all ProductStatus", e);
        }
    }

    /**
     * Clean destinations.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @throws DAOException
     *             the DAO exception
     */
    private static final void cleanDestinations(final String product, final String time) throws DAOException {
        try {
            final var klass = DestinationProductStatus.class.getName();
            final Iterator<?> i = CacheService.getKeysIterator(klass);
            while (i.hasNext()) {
                final var pss = (DestinationProductStatus) CacheService.get(klass, i.next().toString());
                if (product.equals(pss.getProduct()) && time.equals(pss.getTime())) {
                    pss.reset();
                }
            }
        } catch (final Exception e) {
            throw new DAOException("Problem cleaning DestinationProductStatus", e);
        }
    }
}
