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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ecmwf.common.database.DataBaseException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.search.BooleanExpressionException;
import ecmwf.web.util.search.QueryExpressionParser;
import ecmwf.web.util.search.elements.EqualElement;

/**
 * The Class TransferServerDAOHandler.
 */
public class TransferServerDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public TransferServer create() throws DAOException {
        return new TransferServerBean(new ecmwf.common.database.TransferServer());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public TransferServer findByPrimaryKey(final String key) throws DAOException {
        try {
            return new TransferServerBean(MasterManager.getDB().getTransferServer(key));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Error getting resource with key'" + key + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<TransferServer> find(final ModelSearch search) throws DAOException {
        try {
            if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getTransferServerArray());
            }
            try {
                final var bool = QueryExpressionParser.createExpression(search.getQuery());
                final Collection<?> leaves = bool.getLeaves();
                final var clauses = new HashMap<String, String>(leaves.size());
                final Iterator<?> i = leaves.iterator();
                while (i.hasNext()) {
                    final var e = (EqualElement) i.next();
                    clauses.put(e.getName(), e.getValue());
                }
                if (leaves.size() == 1) {
                    // Single clause expressions
                    if (clauses.containsKey("dataTransfer")) {
                        final var server = DataTransferHome.findByPrimaryKey(clauses.get("dataTransfer").toString())
                                .getTransferServerName();
                        final List<TransferServer> results = new ArrayList<>(1);
                        results.add(new TransferServerBean(MasterManager.getDB().getTransferServer(server)));
                        return results;
                    } else if (clauses.containsKey("transferGroup")) {
                        return convertArrayToModelBeanCollection(
                                MasterManager.getDB().getTransferServers(clauses.get("transferGroup").toString()));
                    } else {
                        throw new DAOException("Search by attribute '" + clauses.keySet() + "' not supported");
                    }
                } else {
                    throw new DAOException(
                            "'find' method with query '" + search.getQuery() + "' not supported! Use simple queries");
                }
            } catch (final BooleanExpressionException e) {
                throw new DAOException("Bad find expression", e);
            } catch (final ClassCastException e) {
                throw new DAOException("'find' method with query '" + search.getQuery()
                        + "' not supported! Only equality (=) operators expected");
            } catch (final TransferException e) {
                throw new DAOException("Query '" + search.getQuery() + "' returned a TransferException");
            }
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        final var ts = (TransferServerBean) b;
        try {
            // Assign the objects coming from foreign keys
            final var db = MasterManager.getDB();
            final var imp = (ecmwf.common.database.TransferServer) ts.getOjbImplementation();
            final var transferGroupName = imp.getTransferGroupName();
            if (transferGroupName != null && transferGroupName.length() > 0) {
                imp.setTransferGroup(db.getTransferGroup(transferGroupName));
            }
            final var hostForReplicationName = imp.getHostForReplicationName();
            if (hostForReplicationName != null && hostForReplicationName.length() > 0) {
                imp.setHostForReplication(db.getHost(hostForReplicationName));
            }
            super.save(b, context); // Save ordinary fields.
        } catch (final Exception e) {
            throw new DAOException("Error saving transfer server '" + ts.getName() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var tm = (TransferServerBean) b;
        try {
            MasterManager.getMI().removeTransferServer(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.TransferServer) tm.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting TransferServer '" + tm.getName() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param transferservers
     *            the transferservers
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<TransferServer> convertArrayToModelBeanCollection(
            final ecmwf.common.database.TransferServer[] transferservers) {
        final var length = transferservers != null ? transferservers.length : 0;
        final List<TransferServer> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new TransferServerBean(transferservers[i]));
        }
        return results;
    }
}
