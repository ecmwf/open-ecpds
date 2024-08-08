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
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
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
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.TransferHistoryByDataTransfer;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.TransferHistoryByDestinationAndDate;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferHistory;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.model.users.User;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class TransferHistoryDAOHandler.
 */
public class TransferHistoryDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * Creates the.
     *
     * @return the transfer history
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public TransferHistory create() throws DAOException {
        throw new DAOException("Empty create not allowed!! Sorry.");
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the transfer history
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public TransferHistory findByPrimaryKey(final String key) throws DAOException {
        try {
            return new TransferHistoryBean(MasterManager.getDB().getTransferHistory(Long.parseLong(key)));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Problem searching by key '" + key + "'", e);
        } catch (final NumberFormatException e) {
            throw new DAOException("Incorrect TransferHistoryId: " + key, e);
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
    public Collection<TransferHistory> find(final ModelSearch search) throws DAOException {
        try {
            if (search instanceof final TransferHistoryByDataTransfer s) {
                final var md = s.getDataTransfer();
                final var dataTransferIdString = md.getId();
                final long dataTransferId;
                try {
                    dataTransferId = Long.parseLong(dataTransferIdString);
                } catch (final NumberFormatException e) {
                    throw new DAOException("Incorrect DataTransferId: " + dataTransferIdString, e);
                }
                final var cursor = s.getDataBaseCursor();
                if (cursor != null) {
                    // We ask for the sorted data!
                    return convertArrayToModelBeanCollection(md.getUser(), MasterManager.getDB()
                            .getTransferHistoryByDataTransferId(dataTransferId, s.getAfterScheduleTime(), cursor));
                } else {
                    return convertArrayToModelBeanCollection(md.getUser(),
                            MasterManager.getDB().getTransferHistoryByDataTransferId(dataTransferId));
                }
            }
            if (search instanceof final TransferHistoryByDestinationAndDate s) {
                final var mode = s.getMode();
                if (mode == TransferHistoryByDestinationAndDate.USE_PRODUCT_DATE) {
                    return convertToModelBeanCollection(s.getUser(),
                            MasterManager.getDB().getTransferHistoryByDestinationOnProductDate(s.getDestination(),
                                    getBaseDate(s.getDate(), 0), getBaseDate(s.getDate(), 1), s.getDataBaseCursor()));
                } else if (mode == TransferHistoryByDestinationAndDate.USE_HISTORY_DATE) {
                    return convertToModelBeanCollection(s.getUser(),
                            MasterManager.getDB().getTransferHistoryByDestinationOnHistoryDate(s.getDestination(),
                                    getBaseDate(s.getDate(), 0), getBaseDate(s.getDate(), 1), s.getDataBaseCursor()));
                } else {
                    throw new DAOException("Invalid mode for TransferHistoryByDestinationAndDate: " + mode);
                }
            } else if ("".equals(search.getQuery())) {
                throw new DAOException("'find' method with empty query not supported! Specify some restrictions");
            } else {
                throw new DAOException(
                        "'find' method with query '" + search.getQuery() + "' not supported! Use simple queries");
            }
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * Save.
     *
     * @param bean
     *            the bean
     * @param context
     *            the context
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public void save(final ModelBean bean, final Object context) throws DAOException {
        throw new DAOException("Not supported!!!");
    }

    /**
     * Insert.
     *
     * @param bean
     *            the bean
     * @param context
     *            the context
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public void insert(final ModelBean bean, final Object context) throws DAOException {
        throw new DAOException("Not supported!!!");
    }

    /**
     * Delete.
     *
     * @param bean
     *            the bean
     * @param context
     *            the context
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public void delete(final ModelBean bean, final Object context) throws DAOException {
        throw new DAOException("Not supported!!!");
    }

    /**
     * Convert array to model bean collection.
     *
     * @param user
     *            the user
     * @param transferhistories
     *            the transferhistories
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<TransferHistory> convertArrayToModelBeanCollection(final User user,
            final ecmwf.common.database.TransferHistory[] transferhistories) {
        final var length = transferhistories != null ? transferhistories.length : 0;
        final List<TransferHistory> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new TransferHistoryBean(user, transferhistories[i]));
        }
        return results;
    }

    /**
     * Convert to model bean collection.
     *
     * @param user
     *            the user
     * @param collection
     *            the collection
     *
     * @return the collection
     */
    private static final Collection<TransferHistory> convertToModelBeanCollection(final User user,
            final Collection<ecmwf.common.database.TransferHistory> collection) {
        final List<TransferHistory> results = new ArrayList<>(collection.size());
        for (final ecmwf.common.database.TransferHistory history : collection) {
            results.add(new TransferHistoryBean(user, history));
        }
        return results;
    }
}
