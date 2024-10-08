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

package ecmwf.ecpds.master.plugin.http.home.datafile;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.home.datafile.searches.RatesByDates;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.Rates;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class RatesHome.
 */
public class RatesHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(RatesHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = Rates.class.getName();

    /**
     * Creates the.
     *
     * @return the rates
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Rates create() throws DataFileException {
        try {
            return (Rates) DAOService.create(INTERFACE);
        } catch (final DAOException e) {
            log.error("Error creating object", e);
            throw new DataFileException("Error creating object", e);
        }
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the rates
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Rates findByPrimaryKey(final String key) throws DataFileException {
        try {
            return (Rates) DAOService.findByPrimaryKey(INTERFACE, key);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new DataFileException("Error retrieving object by key", e);
        }

    }

    /**
     * Find by dates.
     *
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @param perTransferServer
     *            the per transfer server
     * @param caller
     *            the caller
     * @param sourceHost
     *            the source host
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<Rates> findByDates(final Date fromDate, final Date toDate,
            final boolean perTransferServer, final String caller, final String sourceHost) throws DataFileException {
        return find(new RatesByDates(fromDate, toDate, perTransferServer, caller, sourceHost));
    }

    /**
     * Find by dates.
     *
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @param transferServerName
     *            the transfer server name
     * @param caller
     *            the caller
     * @param sourceHost
     *            the source host
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<Rates> findByDates(final Date fromDate, final Date toDate,
            final String transferServerName, final String caller, final String sourceHost) throws DataFileException {
        return find(new RatesByDates(fromDate, toDate, transferServerName, caller, sourceHost));
    }

    /**
     * Find.
     *
     * @param search
     *            the search
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<Rates> find(final ModelSearch search) throws DataFileException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new DataFileException("Error retrieving objects", e);
        }
    }
}
