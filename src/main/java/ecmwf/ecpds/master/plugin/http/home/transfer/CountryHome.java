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

package ecmwf.ecpds.master.plugin.http.home.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class CountryHome.
 */
public class CountryHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(CountryHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = Country.class.getName();

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the country
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Country findByPrimaryKey(final String key) throws TransferException {
        try {
            return (Country) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new TransferException("Error retrieving object by key", e);
        }
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Country> findAll() throws TransferException {
        return find(getDefaultSearch(""));
    }

    /**
     * Find.
     *
     * @param search
     *            the search
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Country> find(final ModelSearch search) throws TransferException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new TransferException("Error retrieving objects", e);
        }
    }
}
