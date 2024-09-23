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
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
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
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.Operation;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class OperationDAOHandler.
 */
public class OperationDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public Operation create() throws DAOException {
        return new OperationBean(new ecmwf.common.database.Operation());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public Operation findByPrimaryKey(final String key) throws DAOException {
        try {
            return new OperationBean(MasterManager.getDB().getOperation(key));
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
    public Collection<Operation> find(final ModelSearch search) throws DAOException {
        try {
            if (search instanceof final SearchByIncomingUser s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getOperationsForIncomingUser(s.getIncomingUserId()));
            }
            if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getOperationArray());
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
     * @param incomingPolicy
     *            the incoming policy
     *
     * @return the collection
     */
    private static final Collection<Operation> convertArrayToModelBeanCollection(
            final ecmwf.common.database.Operation[] incomingPolicy) {
        final var length = incomingPolicy != null ? incomingPolicy.length : 0;
        final List<Operation> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new OperationBean(incomingPolicy[i]));
        }
        return results;
    }
}
