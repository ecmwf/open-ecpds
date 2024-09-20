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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ecmwf.common.database.DataBaseException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class CountryDAOHandler.
 */
public class CountryDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * Creates the.
     *
     * @return the country
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public Country create() throws DAOException {
        throw new DAOException("Empty create not allowed!! Sorry.");
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the country
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public Country findByPrimaryKey(final String key) throws DAOException {
        try {
            return new CountryBean(MasterManager.getDB().getCountry(key));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Problem searching by key '" + key + "'", e);
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
    public Collection<Country> find(final ModelSearch search) throws DAOException {
        if (!"".equals(search.getQuery())) {
            throw new DAOException("find method with query '" + search.getQuery() + "' not supported!");
        }
        ecmwf.common.database.Country[] countries;
        try {
            countries = MasterManager.getDB().getCountryArray();
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException(e.getMessage(), e);
        }
        final List<Country> results = new ArrayList<>(countries.length);
        for (final ecmwf.common.database.Country element : countries) {
            results.add(new CountryBean(element));
        }
        return results;
    }
}
