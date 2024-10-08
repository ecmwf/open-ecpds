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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.cache.CacheException;
import ecmwf.web.services.cache.CacheService;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class DestinationProductStatusHome.
 */
public class DestinationProductStatusHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DestinationProductStatusHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = DestinationProductStatus.class.getName();

    /**
     * Find.
     *
     * @param destinationName
     *            the destination name
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @return the destination product status
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException
     *             the monitoring exception
     */
    public static final DestinationProductStatus find(final String destinationName, final String product,
            final String time) throws MonitoringException {
        try {
            return (DestinationProductStatus) DAOService.findByPrimaryKey(INTERFACE,
                    destinationName + "@" + product + "@" + time, true);
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
     * @throws ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException
     *             the monitoring exception
     */
    public static final Collection<DestinationProductStatus> findAll() throws MonitoringException {
        try {
            final var search = getDefaultSearch("All");
            search.setCacheable(false);
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new MonitoringException("Error retrieving objecs", e);
        }
    }

    /**
     * Find from memory.
     *
     * @return the map
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException
     *             the monitoring exception
     */
    public static final Map<String, DestinationProductStatus> findFromMemory() throws MonitoringException {
        try {
            // This is a bit hackish, but is the best we can do.....
            return CacheService.getHandler(INTERFACE).getAsMap();
        } catch (final CacheException e) {
            log.error("Error retrieving Map from cache", e);
            throw new MonitoringException("Error retrieving Map from cache", e);
        }
    }

    /**
     * Clean product.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException
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
}
