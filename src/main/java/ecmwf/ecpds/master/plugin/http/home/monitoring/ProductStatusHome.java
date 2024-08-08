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
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.services.cache.CacheException;
import ecmwf.web.services.cache.CacheService;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;
import ecmwf.web.util.bean.Pair;

/**
 * The Class ProductStatusHome.
 */
public class ProductStatusHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ProductStatusHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = ProductStatus.class.getName();

    /**
     * Find by product. This key is decrypted in "ProductStatusDAOHandler".
     *
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @return the product status
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    public static final ProductStatus findByProduct(final String product, final String time)
            throws MonitoringException {
        try {
            return (ProductStatus) DAOService.findByPrimaryKey(INTERFACE, product + "@" + time, true);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new MonitoringException("Error retrieving object by key", e);
        }
    }

    /**
     * Find all product name time pairs.
     *
     * @return the collection
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    public static final Collection<Pair> findAllProductNameTimePairs() throws MonitoringException {
        try {
            final var search = getDefaultSearch("NameTimePair");
            search.setCacheable(false);
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new MonitoringException("Error retrieving objects", e);
        }
    }

    /**
     * Find from memory.
     *
     * @return the map
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    public static final Map<String, ProductStatus> findFromMemory() throws MonitoringException {
        try {
            return CacheService.getHandler(INTERFACE).getAsMap();
        } catch (final CacheException e) {
            log.error("Error retrieving Map from cache", e);
            throw new MonitoringException("Error retrieving Map from cache", e);
        }
    }
}
