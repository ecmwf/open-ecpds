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
import ecmwf.common.database.ECtransModule;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.model.transfer.EcTransModule;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class EcTransModuleDAOHandler.
 */
public class EcTransModuleDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * Creates the.
     *
     * @return the ec trans module
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public EcTransModule create() throws DAOException {
        return new EcTransModuleBean(new ECtransModule());
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the ec trans module
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public EcTransModule findByPrimaryKey(final String key) throws DAOException {
        try {
            return new EcTransModuleBean(MasterManager.getDB().getECtransModule(key));
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
    public Collection<EcTransModule> find(final ModelSearch search) throws DAOException {
        if (!"".equals(search.getQuery())) {
            throw new DAOException("find method with query '" + search.getQuery() + "' not supported!");
        }
        ECtransModule[] ectransmodules;
        try {
            ectransmodules = MasterManager.getDB().getECtransModuleArray();
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException(e.getMessage(), e);
        }
        final List<EcTransModule> results = new ArrayList<>(ectransmodules.length);
        for (final ECtransModule ectransmodule : ectransmodules) {
            results.add(new EcTransModuleBean(ectransmodule));
        }
        return results;
    }

    /**
     * Delete.
     *
     * @param b
     *            the b
     * @param context
     *            the context
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var ec = (EcTransModuleBean) b;
        try {
            final var mi = MasterManager.getMI();
            mi.removeECtransModule(Util.getECpdsSessionFromObject(context), (ECtransModule) ec.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting EcTransModule '" + ec.getName() + "'", e);
        }
    }
}
