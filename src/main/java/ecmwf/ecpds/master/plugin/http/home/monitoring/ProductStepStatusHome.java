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

package ecmwf.ecpds.master.plugin.http.home.monitoring;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStepStatus;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class ProductStepStatusHome.
 */
public class ProductStepStatusHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ProductStepStatusHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = ProductStepStatus.class.getName();

    /**
     * Find by product and step.
     *
     * These two methods results work WITH the cache.
     *
     * This key is decrypted in "ProductStepStatusDAOHandler".
     *
     * @param product
     *            the product
     * @param time
     *            the time
     * @param buffer
     *            the buffer
     * @param step
     *            the step
     * @param type
     *            the type
     *
     * @return the product step status
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    public static final ProductStepStatus findByProductAndStep(final String product, final String time,
            final long buffer, final long step, final String type) throws MonitoringException {
        try {
            return (ProductStepStatus) DAOService.findByPrimaryKey(INTERFACE,
                    product + "@" + time + "@" + buffer + "@" + step + "@" + type, true);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new MonitoringException("Error retrieving object by key", e);
        }

    }

    // From here they are uncached (see getDefaultSearch())

    /**
     * Clean product.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    public static final void cleanProduct(final String product, final String time) throws MonitoringException {
        try {
            final ModelSearch search = new CleanProductSearch(product, time);
            DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new MonitoringException("Error retrieving object by key", e);
        }
    }

    /**
     * Find all.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @return the collection
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    public static final Collection<ProductStepStatus> findAll(final String product, final String time)
            throws MonitoringException {
        try {
            return filterNotPresent(DAOService.find(INTERFACE, new GetProductStepStatus(product, time)));
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new MonitoringException("Error retrieving object by key", e);
        }
    }

    /**
     * Filter not present.
     *
     * @param l
     *            the l
     *
     * @return the collection
     */
    private static final Collection<ProductStepStatus> filterNotPresent(final Collection<ProductStepStatus> l) {
        final List<ProductStepStatus> result = new ArrayList<>(l.size());
        for (ProductStepStatus p : l) {
            if (p.isPresent()) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Find history.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     * @param step
     *            the step
     * @param type
     *            the type
     * @param limit
     *            the limit
     *
     * @return the collection
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    public static final Collection<ProductStepStatus> findHistory(final String product, final String time,
            final long step, final String type, final int limit) throws MonitoringException {
        try {
            final var search = new GetProductStepStatusHistory(product, time, step, type);
            if (limit > 0) {
                search.setLimit(limit);
            }
            return DAOService.find(INTERFACE, new GetProductStepStatusHistory(product, time, step, type));
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new MonitoringException("Error retrieving object by key", e);
        }
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    public static final Collection<ProductStepStatus> findAll() throws MonitoringException {
        try {
            return DAOService.find(INTERFACE, getDefaultSearch("all"));
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new MonitoringException("Error retrieving object by key", e);
        }
    }

    /**
     * Gets the default search.
     *
     * @param query
     *            the query
     *
     * @return the default search
     */
    public static final ModelSearch getDefaultSearch(final String query) {
        final var search = getDefaultSearch();
        search.setQuery(query);
        search.setCacheable(false);
        return search;
    }
}
