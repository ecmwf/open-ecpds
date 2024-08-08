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
import ecmwf.ecpds.master.plugin.http.dao.transfer.DataTransferLightBean;
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
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.NotDoneTransferCount;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;
import ecmwf.web.util.bean.Pair;

/**
 * The Class DataTransferHome.
 */
public class DataTransferHome extends ModelHomeBase {

    /** The Constant ALL. */
    public static final String ALL = "All";

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DataTransferHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = DataTransfer.class.getName();

    /**
     * Creates the.
     *
     * @return the data transfer
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final DataTransfer create() throws TransferException {
        try {
            return (DataTransfer) DAOService.create(INTERFACE);
        } catch (final DAOException e) {
            log.error("Error creating object", e);
            throw new TransferException("Error creating object", e);
        }
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the data transfer
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final DataTransfer findByPrimaryKey(final String key) throws TransferException {
        try {
            return (DataTransfer) DAOService.findByPrimaryKey(INTERFACE, key, false);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new TransferException("Error retrieving object by key", e);
        }
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findAll() throws TransferException {
        return find(getDefaultSearch(""));
    }

    /**
     * Find by destination and date.
     *
     * @param d
     *            the d
     * @param date
     *            the date
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findByDestinationAndDate(final Destination d, final Date date)
            throws TransferException {
        return find(new DataTransfersByDestination(d, date));
    }

    /**
     * Find by destination and transmission date.
     *
     * @param d
     *            the d
     * @param date
     *            the date
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findByDestinationAndTransmissionDate(final Destination d,
            final Date date) throws TransferException {
        return find(new DataTransfersByDestinationAndTransmissionDate(d, date));
    }

    /**
     * Find by destination product and time.
     *
     * @param d
     *            the d
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findByDestinationProductAndTime(final Destination d,
            final String product, final String time) throws TransferException {
        return find(new DataTransfersByDestinationNameProductAndTime(d.getName(), product, time));
    }

    /**
     * Find by destination product and time on date.
     *
     * @param d
     *            the d
     * @param product
     *            the product
     * @param time
     *            the time
     * @param date
     *            the date
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findByDestinationProductAndTimeOnDate(final Destination d,
            final String product, final String time, final Date date) throws TransferException {
        return find(new DataTransfersByDestinationNameProductAndTime(d.getName(), product, time, date));
    }

    /**
     * Find by destination name product and time on date.
     *
     * @param dname
     *            the dname
     * @param product
     *            the product
     * @param time
     *            the time
     * @param date
     *            the date
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findByDestinationNameProductAndTimeOnDate(final String dname,
            final String product, final String time, final Date date) throws TransferException {
        return find(new DataTransfersByDestinationNameProductAndTime(dname, product, time, date));
    }

    /**
     * Gets the not done transfer count.
     *
     * @param dname
     *            the dname
     * @param product
     *            the product
     * @param time
     *            the time
     * @param date
     *            the date
     *
     * @return the not done transfer count
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final int getNotDoneTransferCount(final String dname, final String product, final String time,
            final Date date) throws TransferException {
        final Collection<Integer> c = find(new NotDoneTransferCount(dname, product, time, date));
        if (c.size() == 1) {
            return c.iterator().next();
        }
        throw new TransferException("Expected a Collection with one Integer element. Got " + c);
    }

    /**
     * Find by host and date.
     *
     * @param h
     *            the h
     * @param date
     *            the date
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransferLightBean> findByHostAndDate(final Host h, final Date date)
            throws TransferException {
        return find(new DataTransfersByHost(h, date));
    }

    /**
     * Find by data file.
     *
     * @param d
     *            the d
     * @param includeDeleted
     *            the include deleted
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findByDataFile(final DataFile d, final boolean includeDeleted)
            throws TransferException {
        return find(new DataTransfersByDataFile(d, includeDeleted));
    }

    /**
     * Find by transfer server on date.
     *
     * @param s
     *            the s
     * @param date
     *            the date
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findByTransferServerOnDate(final TransferServer s, final Date date)
            throws TransferException {
        return find(new DataTransfersByTransferServer(s, date));
    }

    /**
     * Find by status id and date.
     *
     * @param id
     *            the id
     * @param d
     *            the d
     * @param search
     *            the search
     * @param type
     *            the type
     * @param cursor
     *            the cursor
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findByStatusIdAndDate(final String id, final Date d,
            final String search, final String type, final DataBaseCursor cursor) throws TransferException {
        return find(new DataTransfersByStatusCode(id, d, search, type, cursor));
    }

    /**
     * Get DataTransfers for a destination which match a given file name. The idea is to see older data transfers for a
     * given data file. Since the data files are created every day, there is nothing such as "the same file", so the
     * transfers need to be identified by the file name, which could be the same, though there is NO WARRANTY of that.
     * The problem could happen after a rename of the files.
     *
     * @param destination
     *            The destination to where the transfers belong.
     * @param identity
     *            the identity
     *
     * @return A Collection of DataTransfers
     *
     * @throws TransferException
     *             If anything fails while searching.
     */
    public static final Collection<DataTransfer> findByDestinationAndIdentity(final String destination,
            final String identity) throws TransferException {
        return find(new DataTransfersByDestinationNameAndIdentity(destination, identity));
    }

    /**
     * Find by filter.
     *
     * @param destinationName
     *            the destination name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param hasAccess
     *            the has access
     * @param fileName
     *            the file name
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
    public static final Collection<DataTransferLightBean> findByFilter(final String destinationName,
            final String disseminationStream, final String dataStream, final String dataTime, final String status,
            final boolean hasAccess, final String fileName, final Date date, final DataBaseCursor cursor)
            throws TransferException {
        return find(new DataTransfersByFilter(destinationName, disseminationStream, dataStream, dataTime, status,
                hasAccess ? null : new Date(), fileName, date, cursor));
    }

    /**
     * Find by filter.
     *
     * @param destinationName
     *            the destination name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param hasAccess
     *            the has access
     * @param fileName
     *            the file name
     * @param date
     *            the date
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransferLightBean> findByFilter(final String destinationName,
            final String disseminationStream, final String dataStream, final String dataTime, final String status,
            final boolean hasAccess, final String fileName, final Date date) throws TransferException {
        return find(new DataTransfersByFilter(destinationName, disseminationStream, dataStream, dataTime, status,
                hasAccess ? null : new Date(), fileName, date));
    }

    /**
     * Gets the count by filter.
     *
     * @param countOfWhat
     *            the count of what
     * @param destinationName
     *            the destination name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param fileNameSearch
     *            the file name search
     * @param date
     *            the date
     *
     * @return the count by filter
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Pair> getCountByFilter(final String countOfWhat, final String destinationName,
            final String disseminationStream, final String dataStream, final String dataTime, final String status,
            final String fileNameSearch, final Date date) throws TransferException {
        return find(new DataTransfersCountByFilter(countOfWhat, destinationName, disseminationStream, dataStream,
                dataTime, status, fileNameSearch, date));
    }

    /**
     * Gets the count by filter for non privileged users.
     *
     * @param countOfWhat
     *            the count of what
     * @param destinationName
     *            the destination name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param fileNameSearch
     *            the file name search
     * @param date
     *            the date
     *
     * @return the count by filter for non privileged users
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Pair> getCountByFilterForNonPrivilegedUsers(final String countOfWhat,
            final String destinationName, final String disseminationStream, final String dataStream,
            final String dataTime, final String status, final String fileNameSearch, final Date date)
            throws TransferException {
        return find(new DataTransfersCountByFilter(countOfWhat, destinationName, disseminationStream, dataStream,
                dataTime, status, fileNameSearch, date, new Date()));
    }

    /**
     * Gets the count by filter.
     *
     * @param countOfWhat
     *            the count of what
     * @param destinationName
     *            the destination name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param fileNameSearch
     *            the file name search
     *
     * @return the count by filter
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Pair> getCountByFilter(final String countOfWhat, final String destinationName,
            final String disseminationStream, final String dataStream, final String dataTime, final String status,
            final String fileNameSearch) throws TransferException {
        return find(new DataTransfersCountByFilter(countOfWhat, destinationName, disseminationStream, dataStream,
                dataTime, status, fileNameSearch));
    }

    /**
     * Gets the count by filter for non privileged users.
     *
     * @param countOfWhat
     *            the count of what
     * @param destinationName
     *            the destination name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param fileNameSearch
     *            the file name search
     *
     * @return the count by filter for non privileged users
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Pair> getCountByFilterForNonPrivilegedUsers(final String countOfWhat,
            final String destinationName, final String disseminationStream, final String dataStream,
            final String dataTime, final String status, final String fileNameSearch) throws TransferException {
        final var filter = new DataTransfersCountByFilter(countOfWhat, destinationName, disseminationStream, dataStream,
                dataTime, status, fileNameSearch);
        filter.setScheduledBefore(new Date());
        return find(filter);
    }

    /**
     * Find bad by destination.
     *
     * @param d
     *            the d
     *
     * @return Collection of transfers (any day) which are deemed as problematic for this destination.
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<DataTransfer> findBadByDestination(final Destination d) throws TransferException {
        return find(new BadDataTransfersByDestination(d));
    }

    /**
     * Find bad count by destination.
     *
     * @param d
     *            the d
     *
     * @return the int
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final int findBadCountByDestination(final Destination d) throws TransferException {
        final Collection<Integer> c = find(new BadDataTransferCountByDestination(d));
        if (c.size() == 1) {
            return c.iterator().next();
        }
        throw new TransferException("Expected a Collection with one Integer element. Got " + c);
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
    public static final Collection find(final ModelSearch search) throws TransferException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new TransferException("Error retrieving objects", e);
        }
    }

    /**
     * Gets the default search.
     *
     * @param s
     *            the s
     *
     * @return the default search
     */
    public static final ModelSearch getDefaultSearch(final String s) {
        final var search = ModelHomeBase.getDefaultSearch(s);
        search.setCacheable(false);
        return search;
    }
}
