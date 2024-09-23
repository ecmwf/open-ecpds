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
 * Persistence handler for DataFile.
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
import ecmwf.common.database.DataFile;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.searches.DataFilesByMetaDataAndDate;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class DataFileDAOHandler.
 */
public class DataFileDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public ModelBean create() throws DAOException {
        throw new DAOException("Method not supported!");
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        try {
            return new DataFileBean(MasterManager.getDB().getDataFile(Long.parseLong(key)));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Error looking up DataFile", e);
        } catch (final NumberFormatException e) {
            throw new DAOException("Incorrect DataFileId: " + key, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<DataFileBean> find(final ModelSearch search) throws DAOException {
        try {
            if (search instanceof final DataFilesByMetaDataAndDate s) {
                final var from = getBaseDate(s.getDate(), 0);
                final var to = getBaseDate(s.getDate(), 1);
                return convertToModelBeanCollection(MasterManager.getDB().getDataFilesByMetaData(s.getName(),
                        s.getValue(), from, to, s.getDataBaseCursor()));
            }
            throw new DAOException("find method with query '" + search.getQuery() + "' not supported!");
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Query '" + search.getQuery() + "' generated and exception ", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Insert.
     */
    @Override
    public void insert(final ModelBean bean, final Object context) throws DAOException {
        throw new DAOException("Method not supported!");
    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        try {
            final var f = (DataFile) ((OjbImplementedBean) b).getOjbImplementation();
            MasterManager.getMI().updateFileMonitoringValue(Util.getECpdsSessionFromObject(context),
                    f.getMonitoringValue());
        } catch (final Exception e) {
            throw new DAOException("Problem saving object '" + b.getId() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var tm = (DataFileBean) b;
        try {
            final var mi = MasterManager.getMI();
            mi.removeDataFile(Util.getECpdsSessionFromObject(context), (DataFile) tm.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting DataFile '" + tm.getSource() + "'", e);
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
    private static final Collection<DataFileBean> convertToModelBeanCollection(final Collection<DataFile> c) {
        final List<DataFileBean> results = new ArrayList<>(c.size());
        final var i = c.iterator();
        while (i.hasNext()) {
            results.add(new DataFileBean(i.next()));
        }
        return results;
    }
}
