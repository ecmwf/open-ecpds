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

package ecmwf.ecpds.master.plugin.http.dao.ecuser;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
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
import ecmwf.common.database.ECUser;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.home.ecuser.searches.SearchByDestination;
import ecmwf.ecpds.master.plugin.http.home.ecuser.searches.SearchByHost;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class EcUserDAOHandler.
 */
public class EcUserDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public ModelBean create() throws DAOException {
        return new EcUserBean(new ecmwf.common.database.ECUser());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        try {
            return new EcUserBean(MasterManager.getDB().getECUser(key));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Problem searching by key '" + key + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<EcUser> find(final ModelSearch search) throws DAOException {
        try {
            if (search instanceof final SearchByHost searchByHost) {
                return convertToModelBeanCollection(
                        MasterManager.getDB().getAllowedEcUsersByHostName(searchByHost.getHostName()));
            }
            if (search instanceof final SearchByDestination searchByDestination) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getDestinationEcuser(searchByDestination.getDestinationName()));
            } else if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getECUserArray());
            } else {
                throw new DAOException("Unsupported find expression or search class: " + search.getKey());
            }
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param ecusers
     *            the ecusers
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<EcUser> convertArrayToModelBeanCollection(final ECUser[] ecusers) {
        final var length = ecusers != null ? ecusers.length : 0;
        final List<EcUser> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new EcUserBean(ecusers[i]));
        }
        return results;
    }

    /**
     * Convert to model bean collection.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    private static final Collection<EcUser> convertToModelBeanCollection(final Collection<ECUser> c) {
        final List<EcUser> results = new ArrayList<>(c.size());
        final var i = c.iterator();
        while (i.hasNext()) {
            results.add(new EcUserBean(i.next()));
        }
        return results;
    }
}
