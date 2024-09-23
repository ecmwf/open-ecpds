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
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMethod;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.search.BooleanExpressionException;

/**
 * The Class TransferMethodDAOHandler.
 */
public class TransferMethodDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public TransferMethod create() throws DAOException {
        return new TransferMethodBean(new ecmwf.common.database.TransferMethod());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public TransferMethod findByPrimaryKey(final String key) throws DAOException {
        try {
            return new TransferMethodBean(MasterManager.getDB().getTransferMethod(key));
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
    public Collection<TransferMethod> find(final ModelSearch search) throws DAOException {
        try {
            if ("".equals(search.getQuery())) {
                final var transfermethods = MasterManager.getDB().getTransferMethodArray();
                final List<TransferMethod> results = new ArrayList<>(transfermethods.length);
                for (final ecmwf.common.database.TransferMethod transfermethod : transfermethods) {
                    results.add(new TransferMethodBean(transfermethod));
                }
                return results;
            }
            final var clauses = getEqualityClauseLeavesFromExpression(search);
            if (clauses.size() == 1) {
                if (clauses.containsKey("ecTransModule")) {
                    return convertToModelBeanCollection(
                            MasterManager.getDB().getTransferMethodsByEcTransModuleName(clauses.get("ecTransModule")));
                } else {
                    throw new DAOException("find method with clauses '" + clauses + "' not supported!");
                }
            } else {
                throw new DAOException("find method with query '" + search.getQuery() + "' not supported!");
            }
        } catch (final BooleanExpressionException e) {
            throw new DAOException("Bad search expression", e);
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        final var tm = (TransferMethodBean) b;
        try {
            final var ojbTm = (ecmwf.common.database.TransferMethod) tm.getOjbImplementation();
            ojbTm.setECtransModule(MasterManager.getDB().getECtransModule(ojbTm.getECtransModuleName()));
            super.save(b, context); // Save ordinary fields.
        } catch (final Exception e) {
            throw new DAOException(
                    "Error signaling as 'dirty' destinations for the transfer method '" + tm.getName() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var tm = (TransferMethodBean) b;
        try {
            MasterManager.getMI().removeTransferMethod(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.TransferMethod) tm.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting TransferMethod '" + tm.getName() + "'", e);
        }
    }

    /**
     * Convert to model bean collection.
     *
     * @param collection
     *            the collection
     *
     * @return the collection
     */
    private static final Collection<TransferMethod> convertToModelBeanCollection(
            final Collection<ecmwf.common.database.TransferMethod> collection) {
        final List<TransferMethod> results = new ArrayList<>(collection.size());
        for (final ecmwf.common.database.TransferMethod method : collection) {
            results.add(new TransferMethodBean(method));
        }
        return results;
    }
}
