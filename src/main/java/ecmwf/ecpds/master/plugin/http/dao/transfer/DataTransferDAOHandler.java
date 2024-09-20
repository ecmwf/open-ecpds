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
 * Persistence handler for DataTransfers.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ecmwf.common.database.DataBaseException;
import ecmwf.ecpds.master.DataTransferWithPermissions;
import ecmwf.ecpds.master.MasterException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.BadDataTransferCountByDestination;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.BadDataTransfersByDestination;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByDataFile;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByDestination;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByDestinationAndTransmissionDate;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByDestinationNameAndIdentity;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByDestinationNameProductAndTime;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByFilter;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByHost;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByStatusCode;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersByTransferServer;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersCountByFilter;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DataTransfersCountByMetaData;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.NotDoneTransferCount;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class DataTransferDAOHandler.
 */
public class DataTransferDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * Creates the.
     *
     * @return the model bean
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public ModelBean create() throws DAOException {
        return new DataTransferBaseBean(new ecmwf.common.database.DataTransfer());
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the model bean
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        try {
            return new DataTransferHeavyBean(MasterManager.getDB().getDataTransfer(Long.parseLong(key)));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem searching by key '" + key + "'", e);
        } catch (final NumberFormatException e) {
            throw new DAOException("Incorrect DataTransferId: " + key, e);
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
    public Collection<?> find(final ModelSearch search) throws DAOException {
        Collection<ecmwf.common.database.DataTransfer> beans = null;
        Collection<List<String>> pairs = null;
        try {
            if (search instanceof final DataTransfersByHost s) {
                return convertToLightBeanCollection(MasterManager.getDB().getDataTransfersByHostName(
                        s.getHost().getName(), getBaseDate(s.getDate(), 0), getBaseDate(s.getDate(), 1)));
            }
            if (search instanceof final DataTransfersByDestination s) {
                beans = MasterManager.getDB().getDataTransfersByDestinationOnDate(s.getDestination().getName(),
                        getBaseDate(s.getDate(), 0), getBaseDate(s.getDate(), 1));
            } else if (search instanceof final DataTransfersByDestinationAndTransmissionDate s) {
                beans = MasterManager.getDB().getDataTransfersByDestinationOnTransmissionDate(
                        s.getDestination().getName(), getBaseDate(s.getDate(), 0), getBaseDate(s.getDate(), 1));
            } else if (search instanceof final DataTransfersByTransferServer s) {
                beans = MasterManager.getDB().getDataTransfersByTransferServerName(s.getTransferServer().getName(),
                        getBaseDate(s.getDate(), 0), getBaseDate(s.getDate(), 1));
            } else if (search instanceof final DataTransfersByDestinationNameAndIdentity s) {
                beans = MasterManager.getDB().getDataTransfersByDestinationAndIdentity(s.getDestinationName(),
                        s.getIdentity());
            } else if (search instanceof final DataTransfersByStatusCode s) {
                // We are asking for sorting and ordering (paging)!
                beans = MasterManager.getDB().getDataTransfersByStatusCodeAndDate(s.getStatusCode(),
                        getBaseDate(s.getDate(), 0), getBaseDate(s.getDate(), 1), s.getSearch(), s.getType(),
                        s.getDataBaseCursor());
                return convertToLightBeanCollection(beans);
            } else if (search instanceof final DataTransfersByDestinationNameProductAndTime s) {
                if (s.hasDate()) {
                    beans = MasterManager.getDB().getDataTransfersByDestinationProductAndTimeOnDate(
                            s.getDestinationName(), s.getProduct(), s.getTime(), getBaseDate(s.getDate(), 0),
                            getBaseDate(s.getDate(), 1));
                } else {
                    throw new DAOException("DataTransfersByDestinationNameProductAndTime needs a Date");
                }
                return convertToHeavyBeanCollection(beans);
            } else if (search instanceof final DataTransfersByFilter s) {
                final var fileSearch = s.getFileNameSearch();
                final var from = s.hasDate() ? getBaseDate(s.getDate(), 0) : new Date(0);
                final var to = s.hasDate() ? getBaseDate(s.getDate(), 1) : new Date(Long.MAX_VALUE);
                final var scheduledBefore = s.hasScheduledBefore() ? s.getScheduledBefore() : new Date(Long.MAX_VALUE);
                final var privilegedUser = s.hasScheduledBefore() ? "false" : "true";
                final var cursor = s.getDataBaseCursor();
                final Collection<DataTransferWithPermissions> transfersWithPermissions;
                if (cursor != null && isNotEmpty(cursor.getSort())) {
                    // We are asking for sorting and ordering (paging)!
                    transfersWithPermissions = MasterManager.getDB().getDataTransfersByFilter(s.getDestinationName(),
                            s.getDisseminationStream(), s.getDataStream(), s.getDataTime(), s.getStatusCode(),
                            privilegedUser, scheduledBefore, fileSearch, from, to, cursor);
                } else {
                    // We are asking for everything!
                    transfersWithPermissions = MasterManager.getDB().getDataTransfersByFilter(s.getDestinationName(),
                            s.getDisseminationStream(), s.getDataStream(), s.getDataTime(), s.getStatusCode(),
                            privilegedUser, scheduledBefore, fileSearch, from, to);
                }
                return convertToLightBeanCollectionWithOperationsInfo(transfersWithPermissions);
            } else if (search instanceof final DataTransfersByDataFile s) {
                final var dataFileIdString = s.getDataFile().getId();
                final long dataFileId;
                try {
                    dataFileId = Long.parseLong(dataFileIdString);
                } catch (final NumberFormatException e) {
                    throw new DAOException("Incorrect DataFileId: " + dataFileIdString);
                }
                beans = MasterManager.getDB().getDataTransfersByDataFileId(dataFileId, s.getIncludeDeleted());
            } else if (search instanceof final DataTransfersCountByFilter s) {
                final var fileSearch = s.getFileNameSearch();
                final var from = s.hasDate() ? getBaseDate(s.getDate(), 0) : new Date(0);
                final var to = s.hasDate() ? getBaseDate(s.getDate(), 1) : new Date(Long.MAX_VALUE);
                final var scheduledBefore = s.hasScheduledBefore() ? s.getScheduledBefore() : new Date(Long.MAX_VALUE);
                final var privilegedUser = s.hasScheduledBefore() ? "false" : "true";
                pairs = MasterManager.getDB().getTransferCountAndMetaDataByFilter(s.getNameToGet(),
                        s.getDestinationName(), s.getDisseminationStream(), s.getDataStream(), s.getDataTime(),
                        s.getStatusCode(), fileSearch, from, to, privilegedUser, scheduledBefore);
            } else if (search instanceof final DataTransfersCountByMetaData s) {
                pairs = MasterManager.getDB()
                        .getTransferCountWithDestinationAndMetadataValueByMetadataName(s.getMetaDataName());
            } else if (search instanceof final BadDataTransfersByDestination s) {
                beans = MasterManager.getDB().getBadDataTransfersByDestination(s.getDestination().getName());
            } else if (search instanceof final BadDataTransferCountByDestination s) {
                final var i = MasterManager.getDB().getBadDataTransfersByDestinationCount(s.getDestination().getName());
                final List<Integer> l = new ArrayList<>(1);
                l.add(i);
                return l;
            } else if (search instanceof final NotDoneTransferCount s) {
                final var i = MasterManager.getDB().getDataTransferCountNotDoneByProductAndTimeOnDate(
                        s.getDestinationName(), s.getProduct(), s.getTime(), getBaseDate(s.getDate(), 0),
                        getBaseDate(s.getDate(), 1));
                final List<Integer> l = new ArrayList<>(1);
                l.add(i);
                return l;
            } else {
                throw new DAOException("Query '" + search.getQuery() + "' through class '" + search.getClass().getName()
                        + "' unsupported");
            }
            if (beans != null) {
                return convertToBeanCollection(beans);
            }
            return convertCollectionsToObjectPairs(pairs);
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        } catch (final MasterException e) {
            throw new DAOException("MasterException with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * Save.
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
    public void save(final ModelBean b, final Object context) throws DAOException {
        try {
            final var transfer = (ecmwf.common.database.DataTransfer) ((OjbImplementedBean) b).getOjbImplementation();
            MasterManager.getMI().updateTransferMonitoringValue(Util.getECpdsSessionFromObject(context),
                    transfer.getMonitoringValue());
        } catch (final Exception e) {
            throw new DAOException("Problem saving object '" + b.getId() + "'", e);
        }
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
        final var tm = (DataTransferBaseBean) b;
        try {
            final var mi = MasterManager.getMI();
            mi.removeDataTransfer(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.DataTransfer) tm.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting DataTransfer '" + tm.getId() + "'", e);
        }
    }

    /**
     * Convert to bean collection.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    private static final Collection<DataTransferBaseBean> convertToBeanCollection(
            final Collection<ecmwf.common.database.DataTransfer> c) {
        final List<DataTransferBaseBean> results = new ArrayList<>(c.size());
        for (final ecmwf.common.database.DataTransfer transfer : c) {
            results.add(new DataTransferBaseBean(transfer));
        }
        return results;
    }

    /**
     * Convert to light bean collection.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    private static final Collection<DataTransferLightBean> convertToLightBeanCollection(
            final Collection<ecmwf.common.database.DataTransfer> c) {
        final List<DataTransferLightBean> results = new ArrayList<>(c.size());
        for (final ecmwf.common.database.DataTransfer transfer : c) {
            results.add(new DataTransferLightBean(transfer));
        }
        return results;
    }

    /**
     * Convert to light bean collection with operations info.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    private static final Collection<DataTransferLightBean> convertToLightBeanCollectionWithOperationsInfo(
            final Collection<DataTransferWithPermissions> c) {
        final List<DataTransferLightBean> results = new ArrayList<>(c.size());
        for (final DataTransferWithPermissions transfer : c) {
            final var b = new DataTransferLightBean(transfer.getDataTransfer());
            b.setCanBePutOnHold(transfer.getHold());
            b.setCanBeRequeued(transfer.getWait());
            b.setCanBeStopped(transfer.getStop());
            results.add(b);
        }
        return results;
    }

    /**
     * Convert to heavy bean collection.
     *
     * @param collection
     *            the collection
     *
     * @return the collection
     */
    private static final Collection<DataTransferHeavyBean> convertToHeavyBeanCollection(
            final Collection<ecmwf.common.database.DataTransfer> collection) {
        final List<DataTransferHeavyBean> results = new ArrayList<>(collection.size());
        for (final ecmwf.common.database.DataTransfer transfer : collection) {
            results.add(new DataTransferHeavyBean(transfer));
        }
        return results;
    }
}
