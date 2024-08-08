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

package ecmwf.ecpds.master.plugin.http.home.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.TransferHistoryByDataTransfer;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.TransferHistoryByDestinationAndDate;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferHistory;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.model.users.User;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class TransferHistoryHome.
 */
public class TransferHistoryHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(TransferHistoryHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = TransferHistory.class.getName();

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the transfer history
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final TransferHistory findByPrimaryKey(final String key) throws TransferException {
        try {
            return (TransferHistory) DAOService.findByPrimaryKey(INTERFACE, key, false);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new TransferException("Error retrieving object by key", e);
        }
    }

    /**
     * Find by destination name and product date.
     *
     * @param user
     *            the user
     * @param dest
     *            the dest
     * @param date
     *            the date
     * @param cursor
     *            the cursor
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<TransferHistory> findByDestinationNameAndProductDate(final User user,
            final String dest, final Date date, final DataBaseCursor cursor) throws TransferException {
        return find(new TransferHistoryByDestinationAndDate(user, dest, date,
                TransferHistoryByDestinationAndDate.USE_PRODUCT_DATE, cursor));
    }

    /**
     * Find by destination name and history date.
     *
     * @param user
     *            the user
     * @param dest
     *            the dest
     * @param date
     *            the date
     * @param cursor
     *            the cursor
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<TransferHistory> findByDestinationNameAndHistoryDate(final User user,
            final String dest, final Date date, final DataBaseCursor cursor) throws TransferException {
        return find(new TransferHistoryByDestinationAndDate(user, dest, date,
                TransferHistoryByDestinationAndDate.USE_HISTORY_DATE, cursor));
    }

    /**
     * Find by data transfer.
     *
     * @param data
     *            the data
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<TransferHistory> findByDataTransfer(final DataTransfer data)
            throws TransferException {
        return find(new TransferHistoryByDataTransfer(data));
    }

    /**
     * Find by data transfer.
     *
     * @param data
     *            the data
     * @param afterScheduleTime
     *            the after schedule time
     * @param cursor
     *            the cursor
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<TransferHistory> findByDataTransfer(final DataTransfer data,
            final boolean afterScheduleTime, final DataBaseCursor cursor) throws TransferException {
        return find(new TransferHistoryByDataTransfer(data, afterScheduleTime, cursor));
    }

    /**
     * Find.
     *
     * @param search
     *            the search
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<TransferHistory> find(final ModelSearch search) throws TransferException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new TransferException("Error retrieving objects", e);
        }
    }
}
