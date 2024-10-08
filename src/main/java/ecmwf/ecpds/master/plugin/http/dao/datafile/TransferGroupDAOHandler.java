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

import static ecmwf.common.text.Util.isNotEmpty;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ecmwf.common.database.DataBaseException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class TransferGroupDAOHandler.
 */
public class TransferGroupDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public TransferGroup create() throws DAOException {
        return new TransferGroupBean(new ecmwf.common.database.TransferGroup());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public TransferGroup findByPrimaryKey(final String key) throws DAOException {
        try {
            return new TransferGroupBean(MasterManager.getDB().getTransferGroup(key));
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
    public Collection<TransferGroup> find(final ModelSearch search) throws DAOException {
        if (!"".equals(search.getQuery())) {
            throw new DAOException("find method with query '" + search.getQuery() + "' not supported!");
        }
        final ecmwf.common.database.TransferGroup[] transfergroups;
        try {
            transfergroups = MasterManager.getDB().getTransferGroupArray();
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException(e.getMessage(), e);
        }
        final List<TransferGroup> results = new ArrayList<>(transfergroups.length);
        for (final ecmwf.common.database.TransferGroup transfergroup : transfergroups) {
            results.add(new TransferGroupBean(transfergroup));
        }
        return results;
    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        final var ts = (TransferGroupBean) b;
        try {
            // Assign the objects coming from foreign keys !!!!
            final var ojbTg = (ecmwf.common.database.TransferGroup) ts.getOjbImplementation();
            final var backupHostName = ojbTg.getHostForBackupName();
            if (isNotEmpty(backupHostName)) {
                ojbTg.setHostForBackup(MasterManager.getDB().getHost(backupHostName));
            }
            super.save(b, context); // Save ordinary fields.
        } catch (final Exception e) {
            throw new DAOException("Error saving transfer group '" + ts.getName() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var tg = (TransferGroupBean) b;
        try {
            final var mi = MasterManager.getMI();
            mi.removeTransferGroup(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.TransferGroup) tg.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting TransferGroup '" + tg.getName() + "'", e);
        }
    }
}
