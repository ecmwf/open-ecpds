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

package ecmwf.ecpds.master.plugin.http.dao.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.home.datafile.searches.RatesByDates;
import ecmwf.ecpds.master.plugin.http.model.datafile.Rates;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class RatesDAOHandler.
 */
public class RatesDAOHandler extends PDSDAOBase implements DAOHandler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(RatesDAOHandler.class);

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public Rates create() throws DAOException {
        throw new DAOException("Create rates not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public Rates findByPrimaryKey(final String key) throws DAOException {
        throw new DAOException("findByPrimaryKey rates not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<Rates> find(final ModelSearch search) throws DAOException {
        try {
            if (!(search instanceof final RatesByDates ratesByDates)) {
                throw new DAOException("find rates not implemented");
            }
            final var transferServerName = ratesByDates.getTransferServerName();
            if (transferServerName != null) {
                return convertToModelBeanCollection(MasterManager.getDB().getRatesPerFileSystem(
                        ratesByDates.getFromDate(), ratesByDates.getToDate(), transferServerName,
                        Format.unix2sqlWildcards(ratesByDates.getCaller()),
                        Format.unix2sqlWildcards(ratesByDates.getSourceHost())));
            } else if (ratesByDates.getPerTransferServer()) {
                return convertToModelBeanCollection(
                        MasterManager.getDB().getRatesPerTransferServer(ratesByDates.getFromDate(),
                                ratesByDates.getToDate(), Format.unix2sqlWildcards(ratesByDates.getCaller()),
                                Format.unix2sqlWildcards(ratesByDates.getSourceHost())));
            } else {
                return convertToModelBeanCollection(MasterManager.getDB().getRates(ratesByDates.getFromDate(),
                        ratesByDates.getToDate(), Format.unix2sqlWildcards(ratesByDates.getCaller()),
                        Format.unix2sqlWildcards(ratesByDates.getSourceHost())));
            }
        } catch (final Exception e) {
            log.warn("DataBase problem", e);
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * Convert to model bean collection.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    private static final Collection<Rates> convertToModelBeanCollection(final ecmwf.common.database.Rates[] c) {
        final List<Rates> results = new ArrayList<>(c.length);
        for (final ecmwf.common.database.Rates rates : c) {
            results.add(new RatesBean(rates));
        }
        return results;
    }
}
