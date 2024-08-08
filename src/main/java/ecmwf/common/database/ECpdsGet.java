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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.sql.SQLException;

/**
 * The Class ECpdsGet.
 */
final class ECpdsGet {
    /** The _database. */
    private final DataBase _database;

    /**
     * Instantiates a new ecpds get.
     *
     * @param database
     *            the database
     */
    ECpdsGet(final DataBase database) {
        _database = database;
    }

    /**
     * Gets the allowed ec users by host name.
     *
     * @param <T>
     *            the generic type
     * @param paramHost
     *            the param host
     * @param resultClass
     *            the result class
     *
     * @return the allowed ec users by host name
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getAllowedEcUsersByHostName(final String paramHost,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getAllowedEcUsersByHostName", resultClass,
                new String[] { "host=" + paramHost });
    }

    /**
     * Gets the bad data transfers by destination.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param resultClass
     *            the result class
     *
     * @return the bad data transfers by destination
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getBadDataTransfersByDestination(final String paramDestination,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getBadDataTransfersByDestination", resultClass,
                new String[] { "destination=" + paramDestination });
    }

    /**
     * Gets the data files by group by count.
     *
     * @param paramGroupBy
     *            the param group by
     *
     * @return the data files by group by count
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int getDataFilesByGroupByCount(final String paramGroupBy) throws SQLException, IOException {
        return _database.executeCountAsInt("ECpdsBase", "getDataFilesByGroupByCount",
                new String[] { "groupBy=" + paramGroupBy });
    }

    /**
     * Gets the bad data transfers by destination count.
     *
     * @param paramDestination
     *            the param destination
     *
     * @return the bad data transfers by destination count
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int getBadDataTransfersByDestinationCount(final String paramDestination) throws SQLException, IOException {
        return _database.executeCountAsInt("ECpdsBase", "getBadDataTransfersByDestinationCount",
                new String[] { "destination=" + paramDestination });
    }

    /**
     * Gets the existing storage directories.
     *
     * @return the existing storage directories
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getExistingStorageDirectories() throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getExistingStorageDirectories", new String[0]);
    }

    /**
     * Gets the existing storage directories per proxy host.
     *
     * @return the existing storage directories per proxy host
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getExistingStorageDirectoriesPerProxyHost() throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getExistingStorageDirectoriesPerProxyHost", new String[0]);
    }

    /**
     * Gets the traffic by destination name.
     *
     * @param paramDestination
     *            the param destination
     *
     * @return the traffic
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getTrafficByDestinationName(final String paramDestination) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getTrafficByDestinationName",
                new String[] { "destination=" + paramDestination });
    }

    /**
     * Gets the change log by key.
     *
     * @param <T>
     *            the generic type
     * @param paramKeyName
     *            the param key name
     * @param paramKeyValue
     *            the param key value
     * @param resultClass
     *            the result class
     *
     * @return the change log list
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getChangeLogByKey(final String paramKeyName, final String paramKeyValue,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getChangeLogByKey", resultClass,
                new String[] { "keyname=" + paramKeyName, "keyvalue=" + paramKeyValue });
    }

    /**
     * Gets the categories per url.
     *
     * @param <T>
     *            the generic type
     * @param paramId
     *            the param id
     * @param resultClass
     *            the result class
     *
     * @return the categories per url
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getCategoriesPerUrl(final String paramId, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getCategoriesPerUrl", resultClass,
                new String[] { "id=" + paramId });
    }

    /**
     * Gets the categories per user.
     *
     * @param <T>
     *            the generic type
     * @param paramId
     *            the param id
     * @param resultClass
     *            the result class
     *
     * @return the categories per user
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getCategoriesPerUser(final String paramId, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getCategoriesPerUser", resultClass,
                new String[] { "id=" + paramId });
    }

    /**
     * Gets the data files by meta data.
     *
     * @param paramName
     *            the param name
     * @param paramValue
     *            the param value
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramLength
     *            the param length
     *
     * @return the data files by meta data
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataFilesByMetaData(final String paramName, final String paramValue,
            final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate, final String paramSort,
            final String paramOrder, final int paramStart, final int paramLength) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataFilesByMetaData",
                new String[] { "name=" + paramName, "value=" + paramValue, "fromDate=" + paramFromDate.getTime(),
                        "toDate=" + paramToDate.getTime(), "sort=" + paramSort, "order=" + paramOrder,
                        "start=" + paramStart, "length=" + paramLength });
    }

    /**
     * Gets the data transfer count and meta data by filter.
     *
     * @param paramDestination
     *            the param destination
     * @param paramCountBy
     *            the param count by
     * @param paramTarget
     *            the param target
     * @param paramStream
     *            the param stream
     * @param paramTime
     *            the param time
     * @param paramStatus
     *            the param status
     * @param paramFileName
     *            the param file name
     * @param paramSource
     *            the param source
     * @param paramTs
     *            the param ts
     * @param paramPriority
     *            the param priority
     * @param paramChecksum
     *            the param checksum
     * @param paramGroupBy
     *            the param group by
     * @param paramIdentity
     *            the param identity
     * @param paramSize
     *            the param size
     * @param paramReplicated
     *            the param replicated
     * @param paramAsap
     *            the param asap
     * @param paramEvent
     *            the param event
     * @param paramDeleted
     *            the param deleted
     * @param paramExpired
     *            the param expired
     * @param paramProxy
     *            the param proxy
     * @param paramFrom
     *            the param from
     * @param paramTo
     *            the param to
     * @param paramPrivilegedUser
     *            the param privileged user
     * @param paramScheduledBefore
     *            the param scheduled before
     * @param requiresDataFile
     *            the requires data file
     *
     * @return the data transfer count and meta data by filter
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransferCountAndMetaDataByFilter(final String paramDestination, final String paramCountBy,
            final String paramTarget, final String paramStream, final String paramTime, final String paramStatus,
            final String paramFileName, final String paramSource, final String paramTs, final String paramPriority,
            final String paramChecksum, final String paramGroupBy, final String paramIdentity, final String paramSize,
            final String paramReplicated, final String paramAsap, final String paramEvent, final String paramDeleted,
            final String paramExpired, final String paramProxy, final java.sql.Timestamp paramFrom,
            final java.sql.Timestamp paramTo, final java.lang.String paramPrivilegedUser,
            final java.sql.Timestamp paramScheduledBefore, final boolean requiresDataFile)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransferCountAndMetaDataByFilter", new String[] {
                "destination=" + paramDestination, "countBy=" + paramCountBy, "target=" + paramTarget,
                "stream=" + paramStream, "time=" + paramTime, "status=" + paramStatus, "fileName-=" + paramFileName,
                "source-=" + paramSource, "ts-=" + paramTs, "priority-=" + paramPriority, "checksum-=" + paramChecksum,
                "groupby-=" + paramGroupBy, "identity-=" + paramIdentity, "size-=" + paramSize,
                "replicated-=" + paramReplicated, "asap-=" + paramAsap, "event-=" + paramEvent,
                "deleted-=" + paramDeleted, "expired-=" + paramExpired, "proxy-=" + paramProxy,
                "from=" + paramFrom.getTime(), "to=" + paramTo.getTime(), "privilegedUser=" + paramPrivilegedUser,
                "scheduledBefore=" + paramScheduledBefore.getTime(), "datafile=" + (requiresDataFile ? "yes" : "no") });
    }

    /**
     * Gets the data transfer count destination and metadata value by metadata name.
     *
     * @param paramName
     *            the param name
     *
     * @return the data transfer count destination and metadata value by metadata name
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransferCountDestinationAndMetadataValueByMetadataName(final String paramName)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransferCountDestinationAndMetadataValueByMetadataName",
                new String[] { "name=" + paramName });
    }

    /**
     * Gets the data transfer count not done by destination product and time on date.
     *
     * @param paramDestination
     *            the param destination
     * @param paramProduct
     *            the param product
     * @param paramTime
     *            the param time
     * @param paramLastPredicted
     *            the param last predicted
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     *
     * @return the data transfer count not done by destination product and time on date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int getDataTransferCountNotDoneByDestinationProductAndTimeOnDate(final String paramDestination,
            final String paramProduct, final String paramTime, final java.sql.Timestamp paramLastPredicted,
            final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate)
            throws SQLException, IOException {
        return _database.executeCountAsInt("ECpdsBase", "getDataTransferCountNotDoneByDestinationProductAndTimeOnDate",
                new String[] { "destination=" + paramDestination, "product=" + paramProduct, "time=" + paramTime,
                        "lastPredicted=" + paramLastPredicted.getTime(), "fromDate=" + paramFromDate.getTime(),
                        "toDate=" + paramToDate.getTime() });
    }

    /**
     * Gets the data transfer not done on date.
     *
     * @param paramCurrentDate
     *            the param current date
     *
     * @return the data transfer not done on date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransferNotDoneOnDate(final java.sql.Timestamp paramCurrentDate)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransferNotDoneOnDate",
                new String[] { "currentDate=" + paramCurrentDate.getTime() });
    }

    /**
     * Gets the data transfers by data file.
     *
     * @param <T>
     *            the generic type
     * @param paramDatafile
     *            the param datafile
     * @param paramIncludeDeleted
     *            the param include deleted
     * @param resultClass
     *            the result class
     *
     * @return the data transfers by data file
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersByDataFile(final long paramDatafile,
            final boolean paramIncludeDeleted, final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersByDataFile", resultClass, new String[] {
                "datafile=" + paramDatafile, "includeDeleted=" + (paramIncludeDeleted ? "true" : "false") });
    }

    /**
     * Gets the data transfers by destination.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param resultClass
     *            the result class
     *
     * @return the data transfers by destination
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersByDestination(final String paramDestination,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersByDestination", resultClass,
                new String[] { "destination=" + paramDestination });
    }

    /**
     * Gets the data transfers by destination and identity.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param paramIdentity
     *            the param identity
     * @param resultClass
     *            the result class
     *
     * @return the data transfers by destination and identity
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersByDestinationAndIdentity(final String paramDestination,
            final String paramIdentity, final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersByDestinationAndIdentity", resultClass,
                new String[] { "destination=" + paramDestination, "identity=" + paramIdentity });
    }

    /**
     * Gets the dates by destination and target on date.
     *
     * @param paramDestination
     *            the param destination
     * @param order
     *            the order
     *
     * @return the dates by destination and target on date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDatesByDestinationAndTargetOnDate(final String paramDestination, final int order)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDatesByDestinationAndTargetOnDate",
                new String[] { "destination=" + paramDestination, "order=" + order });
    }

    /**
     * Gets the data transfers by destination and target on date (v2).
     *
     * @param paramDestination
     *            the param destination
     * @param target
     *            the target
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param sort
     *            the sort (1=DAT_SIZE,2=DAT_TARGET,3=DAT_SCHEDULED_TIME)
     * @param order
     *            the order (Descending=2,Ascending=1)
     *
     * @return the data transfers by destination and target on date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransfersByDestinationAndTargetOnDate2(final String paramDestination, final String target,
            final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate, final int sort,
            final int order) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransfersByDestinationAndTargetOnDate2",
                new String[] { "destination=" + paramDestination, "target=" + (target != null ? target : ""),
                        "fromDate=" + paramFromDate.getTime(), "toDate=" + paramToDate.getTime(), "sort=" + sort,
                        "order=" + order });
    }

    /**
     * Gets the data transfers by destination and target on date.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param target
     *            the target
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param resultClass
     *            the result class
     *
     * @return the data transfers by destination and target on date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersByDestinationAndTargetOnDate(final String paramDestination,
            final String target, final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersByDestinationAndTargetOnDate", resultClass,
                new String[] { "destination=" + paramDestination, "target=" + (target != null ? target : ""),
                        "fromDate=" + paramFromDate.getTime(), "toDate=" + paramToDate.getTime() });
    }

    /**
     * Gets the data transfers by destination and target.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param target
     *            the target
     * @param runnable
     *            the runnable
     * @param resultClass
     *            the result class
     *
     * @return the data transfers by destination and target
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersByDestinationAndTarget(final String paramDestination,
            final String target, final boolean runnable, final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersByDestinationAndTarget", resultClass,
                new String[] { "destination=" + paramDestination, "target=" + (target != null ? target : ""),
                        "runnable=" + (runnable ? "true" : "false") });
    }

    /**
     * Gets the data transfers by destination and target2.
     *
     * @param paramDestination
     *            the param destination
     * @param target
     *            the target
     * @param runnable
     *            the runnable
     * @param sort
     *            the sort
     * @param order
     *            the order
     *
     * @return the data transfers by destination and target2
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransfersByDestinationAndTarget2(final String paramDestination, final String target,
            final boolean runnable, final int sort, final int order) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransfersByDestinationAndTarget2",
                new String[] { "destination=" + paramDestination, "target=" + (target != null ? target : ""),
                        "runnable=" + (runnable ? "true" : "false"), "sort=" + sort, "order=" + order });
    }

    /**
     * Gets the data transfers by destination and target on transmission date.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param target
     *            the target
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param resultClass
     *            the result class
     *
     * @return the data transfers by destination and target on transmission date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersByDestinationAndTargetOnTransmissionDate(
            final String paramDestination, final String target, final java.sql.Timestamp paramFromDate,
            final java.sql.Timestamp paramToDate, final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersByDestinationAndTargetOnTransmissionDate",
                resultClass,
                new String[] { "destination=" + paramDestination, "target=" + (target != null ? target : ""),
                        "fromDate=" + paramFromDate.getTime(), "toDate=" + paramToDate.getTime() });
    }

    /**
     * Gets the data transfers by destination product and time on date.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param paramStream
     *            the param stream
     * @param paramTime
     *            the param time
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param resultClass
     *            the result class
     *
     * @return the data transfers by destination product and time on date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersByDestinationProductAndTimeOnDate(
            final String paramDestination, final String paramStream, final String paramTime,
            final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersByDestinationProductAndTimeOnDate", resultClass,
                new String[] { "destination=" + paramDestination, "stream=" + paramStream, "time=" + paramTime,
                        "fromDate=" + paramFromDate.getTime(), "toDate=" + paramToDate.getTime() });
    }

    /**
     * Gets the hosts with the provided criteria.
     *
     * @param paramLabel
     *            the param label
     * @param paramFilter
     *            the param filter
     * @param paramNetwork
     *            the param network
     * @param paramType
     *            the param type
     * @param paramId
     *            the param id
     * @param paramLogin
     *            the param login
     * @param paramNickname
     *            the param nickname
     * @param paramComment
     *            the param comment
     * @param paramOptions
     *            the param options
     * @param paramDir
     *            the param dir
     * @param paramHostname
     *            the param hostname
     * @param paramEnabled
     *            the param enabled
     * @param paramMethod
     *            the param method
     * @param paramEmail
     *            the param email
     * @param paramPassword
     *            the param password
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramLength
     *            the param length
     *
     * @return the hosts
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getFilteredHosts(final String paramLabel, final String paramFilter, final String paramNetwork,
            final String paramType, final String paramId, final String paramLogin, final String paramNickname,
            final String paramComment, final String paramOptions, final String paramDir, final String paramHostname,
            final String paramEnabled, final String paramMethod, final String paramEmail, final String paramPassword,
            final String paramSort, final String paramOrder, final int paramStart, final int paramLength)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getFilteredHosts",
                new String[] { "label=" + paramLabel, "filter=" + paramFilter, "network=" + paramNetwork,
                        "type=" + paramType, "id-=" + paramId, "login-=" + paramLogin, "nickname-=" + paramNickname,
                        "comment-=" + paramComment, "options-=" + paramOptions, "dir-=" + paramDir,
                        "hostname-=" + paramHostname, "enabled-=" + paramEnabled, "method-=" + paramMethod,
                        "email-=" + paramEmail, "password-=" + paramPassword, "sort=" + paramSort,
                        "order=" + paramOrder, "start=" + paramStart, "length=" + paramLength });
    }

    /**
     * Gets the data transfers by filter.
     *
     * @param paramDestination
     *            the param destination
     * @param paramTarget
     *            the param target
     * @param paramStream
     *            the param stream
     * @param paramTime
     *            the param time
     * @param paramStatus
     *            the param status
     * @param paramPrivilegedUser
     *            the param privileged user
     * @param paramScheduledBefore
     *            the param scheduled before
     * @param paramFileName
     *            the param file name
     * @param paramSource
     *            the param source
     * @param paramTs
     *            the param ts
     * @param paramPriority
     *            the param priority
     * @param paramChecksum
     *            the param checksum
     * @param paramGroupBy
     *            the param group by
     * @param paramIdentity
     *            the param identity
     * @param paramSize
     *            the param size
     * @param paramReplicated
     *            the param replicated
     * @param paramAsap
     *            the param asap
     * @param paramEvent
     *            the param event
     * @param paramDeleted
     *            the param deleted
     * @param paramExpired
     *            the param expired
     * @param paramProxy
     *            the param proxy
     * @param paramFrom
     *            the param from
     * @param paramTo
     *            the param to
     *
     * @return the data transfers by filter
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransfersByFilter(final String paramDestination, final String paramTarget,
            final String paramStream, final String paramTime, final String paramStatus,
            final String paramPrivilegedUser, final java.sql.Timestamp paramScheduledBefore, final String paramFileName,
            final String paramSource, final String paramTs, final String paramPriority, final String paramChecksum,
            final String paramGroupBy, final String paramIdentity, final String paramSize, final String paramReplicated,
            final String paramAsap, final String paramEvent, final String paramDeleted, final String paramExpired,
            final String paramProxy, final java.sql.Timestamp paramFrom, final java.sql.Timestamp paramTo)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransfersByFilter",
                new String[] { "destination=" + paramDestination, "target=" + paramTarget, "stream=" + paramStream,
                        "time=" + paramTime, "status=" + paramStatus, "privilegedUser=" + paramPrivilegedUser,
                        "scheduledBefore=" + paramScheduledBefore.getTime(), "fileName-=" + paramFileName,
                        "source-=" + paramSource, "ts-=" + paramTs, "priority-=" + paramPriority,
                        "checksum-=" + paramChecksum, "groupby-=" + paramGroupBy, "identity-=" + paramIdentity,
                        "size-=" + paramSize, "replicated-=" + paramReplicated, "asap-=" + paramAsap,
                        "event-=" + paramEvent, "deleted-=" + paramDeleted, "expired-=" + paramExpired,
                        "proxy-=" + paramProxy, "from=" + paramFrom.getTime(), "to=" + paramTo.getTime() });
    }

    /**
     * Gets the sorted data transfers by filter.
     *
     * @param paramDestination
     *            the param destination
     * @param paramTarget
     *            the param target
     * @param paramStream
     *            the param stream
     * @param paramTime
     *            the param time
     * @param paramStatus
     *            the param status
     * @param paramPrivilegedUser
     *            the param privileged user
     * @param paramScheduledBefore
     *            the param scheduled before
     * @param paramFileName
     *            the param file name
     * @param paramSource
     *            the param source
     * @param paramTs
     *            the param ts
     * @param paramPriority
     *            the param priority
     * @param paramChecksum
     *            the param checksum
     * @param paramGroupBy
     *            the param group by
     * @param paramIdentity
     *            the param identity
     * @param paramSize
     *            the param size
     * @param paramReplicated
     *            the param replicated
     * @param paramAsap
     *            the param asap
     * @param paramEvent
     *            the param event
     * @param paramDeleted
     *            the param deleted
     * @param paramExpired
     *            the param expired
     * @param paramProxy
     *            the param proxy
     * @param paramFrom
     *            the param from
     * @param paramTo
     *            the param to
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramLength
     *            the param length
     *
     * @return the sorted data transfers by filter
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getSortedDataTransfersByFilter(final String paramDestination, final String paramTarget,
            final String paramStream, final String paramTime, final String paramStatus,
            final String paramPrivilegedUser, final java.sql.Timestamp paramScheduledBefore, final String paramFileName,
            final String paramSource, final String paramTs, final String paramPriority, final String paramChecksum,
            final String paramGroupBy, final String paramIdentity, final String paramSize, final String paramReplicated,
            final String paramAsap, final String paramEvent, final String paramDeleted, final String paramExpired,
            final String paramProxy, final java.sql.Timestamp paramFrom, final java.sql.Timestamp paramTo,
            final String paramSort, final String paramOrder, final int paramStart, final int paramLength)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getSortedDataTransfersByFilter",
                new String[] { "destination=" + paramDestination, "target=" + paramTarget, "stream=" + paramStream,
                        "time=" + paramTime, "status=" + paramStatus, "privilegedUser=" + paramPrivilegedUser,
                        "scheduledBefore=" + paramScheduledBefore.getTime(), "fileName-=" + paramFileName,
                        "source-=" + paramSource, "ts-=" + paramTs, "priority-=" + paramPriority,
                        "checksum-=" + paramChecksum, "groupby-=" + paramGroupBy, "identity-=" + paramIdentity,
                        "size-=" + paramSize, "replicated-=" + paramReplicated, "asap-=" + paramAsap,
                        "event-=" + paramEvent, "deleted-=" + paramDeleted, "expired-=" + paramExpired,
                        "proxy-=" + paramProxy, "from=" + paramFrom.getTime(), "to=" + paramTo.getTime(),
                        "sort=" + paramSort, "order=" + paramOrder, "start=" + paramStart, "length=" + paramLength });
    }

    /**
     * Gets the data transfers by host name.
     *
     * @param paramHost
     *            the param host
     * @param paramFrom
     *            the param from
     * @param paramTo
     *            the param to
     *
     * @return the data transfers by host name
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransfersByHostName(final String paramHost, final java.sql.Timestamp paramFrom,
            final java.sql.Timestamp paramTo) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransfersByHostName",
                new String[] { "host=" + paramHost, "from=" + paramFrom.getTime(), "to=" + paramTo.getTime() });
    }

    /**
     * Gets the destinations and hosts for type.
     *
     * @param type
     *            the type
     * @param paramLimit
     *            the param limit
     *
     * @return the destinations and hosts for type
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDestinationsAndHostsForType(final String type, final int paramLimit)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDestinationsAndHostsForAcquisition",
                new String[] { "type=" + type, "limit=" + paramLimit });
    }

    /**
     * Gets the sorted data transfers by status on date.
     *
     * @param paramStatus
     *            the param status
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramFileName
     *            the param file name
     * @param paramSource
     *            the param source
     * @param paramTs
     *            the param ts
     * @param paramPriority
     *            the param priority
     * @param paramChecksum
     *            the param checksum
     * @param paramGroupBy
     *            the param group by
     * @param paramIdentity
     *            the param identity
     * @param paramSize
     *            the param size
     * @param paramReplicated
     *            the param replicated
     * @param paramAsap
     *            the param asap
     * @param paramEvent
     *            the param event
     * @param paramDeleted
     *            the param deleted
     * @param paramExpired
     *            the param expired
     * @param paramProxy
     *            the param proxy
     * @param paramType
     *            the param type
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramLength
     *            the param length
     * @param requiresDataFile
     *            the requires data file
     *
     * @return the sorted data transfers by status on date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getSortedDataTransfersByStatusOnDate(final String paramStatus, final java.sql.Timestamp paramFromDate,
            final java.sql.Timestamp paramToDate, final String paramFileName, final String paramSource,
            final String paramTs, final String paramPriority, final String paramChecksum, final String paramGroupBy,
            final String paramIdentity, final String paramSize, final String paramReplicated, final String paramAsap,
            final String paramEvent, final String paramDeleted, final String paramExpired, final String paramProxy,
            final String paramType, final String paramSort, final String paramOrder, final int paramStart,
            final int paramLength, final boolean requiresDataFile) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getSortedDataTransfersByStatusOnDate",
                new String[] { "status=" + paramStatus, "fromDate=" + paramFromDate.getTime(),
                        "toDate=" + paramToDate.getTime(), "fileName-=" + paramFileName, "source-=" + paramSource,
                        "ts-=" + paramTs, "priority-=" + paramPriority, "checksum-=" + paramChecksum,
                        "groupby-=" + paramGroupBy, "identity-=" + paramIdentity, "size-=" + paramSize,
                        "replicated-=" + paramReplicated, "asap-=" + paramAsap, "event-=" + paramEvent,
                        "deleted-=" + paramDeleted, "expired-=" + paramExpired, "proxy-=" + paramProxy,
                        "sort=" + paramSort, "order=" + paramOrder, "start=" + paramStart, "length=" + paramLength,
                        "type=" + paramType, "datafile=" + (requiresDataFile ? "yes" : "no") });
    }

    /**
     * Gets the data transfers by transfer server.
     *
     * @param <T>
     *            the generic type
     * @param paramTransferServer
     *            the param transfer server
     * @param resultClass
     *            the result class
     *
     * @return the data transfers by transfer server
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersByTransferServer(final String paramTransferServer,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersByTransferServer", resultClass,
                new String[] { "transferServer=" + paramTransferServer });
    }

    /**
     * Gets the initial data transfer events.
     *
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     *
     * @return the initial data transfer events
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getInitialDataTransferEvents(final java.sql.Timestamp paramFromDate,
            final java.sql.Timestamp paramToDate) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransfersOnDate",
                new String[] { "fromDate=" + paramFromDate.getTime(), "toDate=" + paramToDate.getTime() });
    }

    /**
     * Gets the initial product status events.
     *
     * @param <T>
     *            the generic type
     * @param resultClass
     *            the result class
     *
     * @return the initial product status events
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getInitialProductStatusEvents(final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getLastUpdatedProductStatus", resultClass);
    }

    /**
     * Gets the data transfers per transfer server.
     *
     * @param paramServer
     *            the param server
     * @param paramFrom
     *            the param from
     * @param paramTo
     *            the param to
     *
     * @return the data transfers per transfer server
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransfersPerTransferServer(final String paramServer, final java.sql.Timestamp paramFrom,
            final java.sql.Timestamp paramTo) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransfersPerTransferServer",
                new String[] { "server=" + paramServer, "from=" + paramFrom.getTime(), "to=" + paramTo.getTime() });
    }

    /**
     * Gets the destination aliases.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param paramMode
     *            the param mode
     * @param resultClass
     *            the result class
     *
     * @return the destination aliases
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationAliases(final String paramDestination,
            final String paramMode, final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationAliases", resultClass,
                new String[] { "destination=" + paramDestination, "mode=" + paramMode });
    }

    /**
     * Gets the destinations for incoming user.
     *
     * @param <T>
     *            the generic type
     * @param paramId
     *            the param id
     * @param resultClass
     *            the result class
     *
     * @return the destinations for incoming user
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationsForIncomingUser(final String paramId,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationsForIncomingUser", resultClass,
                new String[] { "id=" + paramId, });
    }

    /**
     * Gets the destinations for incoming policy.
     *
     * @param <T>
     *            the generic type
     * @param paramId
     *            the param id
     * @param resultClass
     *            the result class
     *
     * @return the destinations for incoming user
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationsForIncomingPolicy(final String paramId,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationsForIncomingPolicy", resultClass,
                new String[] { "id=" + paramId, });
    }

    /**
     * Gets the destinations by user policies.
     *
     * @param <T>
     *            the generic type
     * @param paramId
     *            the param id
     * @param resultClass
     *            the result class
     *
     * @return the destinations for incoming user
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationsByUserPolicies(final String paramId,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationsByUserPolicies", resultClass,
                new String[] { "id=" + paramId, });
    }

    /**
     * Gets the incoming permissions for incoming user.
     *
     * @param <T>
     *            the generic type
     * @param paramId
     *            the param id
     * @param resultClass
     *            the result class
     *
     * @return the incoming permissions for incoming user
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getIncomingPermissionsForIncomingUser(final String paramId,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getIncomingPermissionsForIncomingUser", resultClass,
                new String[] { "id=" + paramId });
    }

    /**
     * Gets the destination ecuser.
     *
     * @param <T>
     *            the generic type
     * @param paramName
     *            the param name
     * @param resultClass
     *            the result class
     *
     * @return the destination ecuser
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationEcuser(final String paramName, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationEcuser", resultClass,
                new String[] { "name=" + paramName });
    }

    /**
     * Gets the destination incoming policies.
     *
     * @param <T>
     *            the generic type
     * @param paramName
     *            the param name
     * @param resultClass
     *            the result class
     *
     * @return the incoming policies
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationIncomingPolicy(final String paramName,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationIncomingPolicy", resultClass,
                new String[] { "name=" + paramName });
    }

    /**
     * Gets the incoming users for an incoming policy.
     *
     * @param <T>
     *            the generic type
     * @param paramName
     *            the param name
     * @param resultClass
     *            the result class
     *
     * @return the incoming users
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getIncomingUsersForIncomingPolicy(final String paramName,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getIncomingUsersForIncomingPolicy", resultClass,
                new String[] { "name=" + paramName });
    }

    /**
     * Gets the incoming policies for an incoming user.
     *
     * @param <T>
     *            the generic type
     * @param paramName
     *            the param name
     * @param resultClass
     *            the result class
     *
     * @return the incoming policies
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getIncomingPoliciesForIncomingUser(final String paramName,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getIncomingPoliciesForIncomingUser", resultClass,
                new String[] { "name=" + paramName });
    }

    /**
     * Gets the destination exts.
     *
     * @return the destination exts
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDestinationExts() throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDestinationExts");
    }

    /**
     * Gets the destination names and comments sorted by name.
     *
     * @return the destination names and comments sorted by names
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDestinationNamesAndComments() throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDestinationNamesAndComments");
    }

    /**
     * Gets the bad data transfers count.
     *
     * @return the bad data transfers count
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getBadDataTransfersCount() throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getBadDataTransfersCount");
    }

    /**
     * Gets the destination host.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param resultClass
     *            the result class
     *
     * @return the destination host
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationHost(final String paramDestination,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationHost", resultClass,
                new String[] { "destination=" + paramDestination });
    }

    /**
     * Gets the destinations.
     *
     * @param <T>
     *            the generic type
     * @param paramName
     *            the param name
     * @param resultClass
     *            the result class
     *
     * @return the destinations
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinations(final String paramName, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinations", resultClass,
                new String[] { "name=" + paramName });
    }

    /**
     * Gets the destinations.
     *
     * @param <T>
     *            the generic type
     * @param paramMonitored
     *            only for monitored destinations?
     * @param resultClass
     *            the result class
     *
     * @return the destinations
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationArray(final boolean paramMonitored,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationArray", resultClass,
                new String[] { "monitored=" + (paramMonitored ? "true" : "false") });
    }

    /**
     * Gets the destinations by country.
     *
     * @param <T>
     *            the generic type
     * @param paramName
     *            the param name
     * @param resultClass
     *            the result class
     *
     * @return the destinations by country
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationsByCountry(final String paramName,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationsByCountry", resultClass,
                new String[] { "name=" + paramName });
    }

    /**
     * Gets the destinations by user.
     *
     * @param <T>
     *            the generic type
     * @param paramUid
     *            the param uid
     * @param paramName
     *            the param name
     * @param paramComment
     *            the param comment
     * @param paramCountry
     *            the param country
     * @param paramOptions
     *            the param options
     * @param paramEnabled
     *            the param enabled
     * @param paramMonitor
     *            the param monitor
     * @param paramBackup
     *            the param backup
     * @param paramFromToAliases
     *            the from to aliases
     * @param paramAsc
     *            the param asc
     * @param paramStatus
     *            the param status
     * @param paramType
     *            the param type
     * @param paramFilter
     *            the param filter
     * @param resultClass
     *            the result class
     *
     * @return the destinations by user
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationsByUser(final String paramUid, final String paramName,
            final String paramComment, final String paramCountry, final String paramOptions, final String paramEnabled,
            final String paramMonitor, final String paramBackup, final String paramFromToAliases,
            final boolean paramAsc, final String paramStatus, final String paramType, final String paramFilter,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationsByUser", resultClass,
                new String[] { "uid=" + paramUid, "name-=" + paramName, "comment-=" + paramComment,
                        "country-=" + paramCountry, "options-=" + paramOptions, "enabled-=" + paramEnabled,
                        "monitor-=" + paramMonitor, "backup-=" + paramBackup, "fromToAliases=" + paramFromToAliases,
                        "asc=" + paramAsc, "status=" + paramStatus, "type=" + paramType, "filter=" + paramFilter });
    }

    /**
     * Gets the authorized destinations.
     *
     * @param user
     *            the user
     *
     * @return the authorized destinations
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getAuthorizedDestinations(final String user) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getAuthorisedDestinations", new String[] { "user=" + user });
    }

    /**
     * Gets the authorized hosts.
     *
     * @param user
     *            the user
     *
     * @return the authorized hosts
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getAuthorizedHosts(final String user) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getAuthorisedHosts", new String[] { "user=" + user });
    }

    /**
     * Gets the destinations by host name.
     *
     * @param <T>
     *            the generic type
     * @param paramHost
     *            the param host
     * @param resultClass
     *            the result class
     *
     * @return the destinations by host name
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDestinationsByHostName(final String paramHost,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDestinationsByHostName", resultClass,
                new String[] { "host=" + paramHost });
    }

    /**
     * Gets the EC user count.
     *
     * @return the EC user count
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int getECUserCount() throws SQLException, IOException {
        return _database.executeCountAsInt("ECpdsBase", "getECUserCount");
    }

    /**
     * Gets the EC user events.
     *
     * @param paramUser
     *            the param user
     * @param paramDate
     *            the param date
     * @param paramSearch
     *            the param search
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramEnd
     *            the param length
     * @param paramLength
     *            the param length
     *
     * @return the EC user events
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getECUserEvents(final String paramUser, final java.sql.Date paramDate, final String paramSearch,
            final String paramSort, final String paramOrder, final int paramStart, final int paramEnd,
            final int paramLength) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getECUserEvents",
                new String[] { "user=" + paramUser, "date=" + paramDate, "search=" + paramSearch, "sort=" + paramSort,
                        "order=" + paramOrder, "start=" + paramStart, "end=" + paramEnd, "length=" + paramLength });
    }

    /**
     * Gets the incoming history list.
     *
     * @param paramUser
     *            the param user
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramSearch
     *            the param search
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramEnd
     *            the param length
     * @param paramLength
     *            the param length
     *
     * @return the incoming history list
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getIncomingHistory(final String paramUser, final java.sql.Timestamp paramFromDate,
            final java.sql.Timestamp paramToDate, final String paramSearch, final String paramSort,
            final String paramOrder, final int paramStart, final int paramEnd, final int paramLength)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getIncomingHistory",
                new String[] { "user=" + paramUser, "fromDate=" + paramFromDate.getTime(),
                        "toDate=" + paramToDate.getTime(), "search=" + paramSearch, "sort=" + paramSort,
                        "order=" + paramOrder, "start=" + paramStart, "end=" + paramEnd, "length=" + paramLength });
    }

    /**
     * Gets the expired data files.
     *
     * @param paramLimit
     *            the param limit
     *
     * @return the expired data files
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getExpiredDataFiles(final int paramLimit) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getExpiredDataFiles", new String[] { "limit=" + paramLimit });
    }

    /**
     * Gets the data files to filter.
     *
     * @param paramLimit
     *            the param limit
     *
     * @return the data files to filter
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataFilesToFilter(final int paramLimit) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataFilesToFilter", new String[] { "limit=" + paramLimit });
    }

    /**
     * Gets the publications to process.
     *
     * @param paramLimit
     *            the param limit
     *
     * @return the publications to process
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getPublicationsToProcess(final int paramLimit) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getPublicationsToProcess", new String[] { "limit=" + paramLimit });
    }

    /**
     * Gets the data transfers to replicate.
     *
     * @param <T>
     *            the generic type
     * @param paramLimit
     *            the param limit
     * @param resultClass
     *            the result class
     *
     * @return the data transfers to replicate
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersToReplicate(final int paramLimit,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersToReplicate", resultClass,
                new String[] { "limit=" + paramLimit });
    }

    /**
     * Gets the data transfers to backup.
     *
     * @param <T>
     *            the generic type
     * @param paramLimit
     *            the param limit
     * @param resultClass
     *            the result class
     *
     * @return the data transfers to backup
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersToBackup(final int paramLimit, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersToBackup", resultClass,
                new String[] { "limit=" + paramLimit });
    }

    /**
     * Gets the data transfers to proxy.
     *
     * @param <T>
     *            the generic type
     * @param paramLimit
     *            the param limit
     * @param resultClass
     *            the result class
     *
     * @return the data transfers to proxy
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDataTransfersToProxy(final int paramLimit, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDataTransfersToProxy", resultClass,
                new String[] { "limit=" + paramLimit });
    }

    /**
     * Gets the acquisition data transfers to download.
     *
     * @param <T>
     *            the generic type
     * @param paramLimit
     *            the param limit
     * @param resultClass
     *            the result class
     *
     * @return the acquisition data transfers to download
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getAcquisitionDataTransfersToDownload(final int paramLimit,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getAcquisitionDataTransfersToDownload", resultClass,
                new String[] { "limit=" + paramLimit });
    }

    /**
     * Gets the dissemination data transfers to download.
     *
     * @param <T>
     *            the generic type
     * @param paramLimit
     *            the param limit
     * @param resultClass
     *            the result class
     *
     * @return the dissemination data transfers to download
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getDisseminationDataTransfersToDownload(final int paramLimit,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getDisseminationDataTransfersToDownload", resultClass,
                new String[] { "limit=" + paramLimit });
    }

    /**
     * Gets the hosts by transfer method id.
     *
     * @param <T>
     *            the generic type
     * @param paramMethod
     *            the param method
     * @param resultClass
     *            the result class
     *
     * @return the hosts by transfer method id
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getHostsByTransferMethodId(final String paramMethod,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getHostsByTransferMethodId", resultClass,
                new String[] { "method=" + paramMethod });
    }

    /**
     * Gets the hosts to check.
     *
     * @param <T>
     *            the generic type
     * @param resultClass
     *            the result class
     *
     * @return the hosts to check
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getHostsToCheck(final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getHostsToCheck", resultClass);
    }

    /**
     * Gets the interrupted transfers.
     *
     * @param <T>
     *            the generic type
     * @param resultClass
     *            the result class
     *
     * @return the interrupted transfers
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getInterruptedTransfers(final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getInterruptedTransfers", resultClass);
    }

    /**
     * Gets the interrupted transfers per destination.
     *
     * @param <T>
     *            the generic type
     * @param paramDestination
     *            the param destination
     * @param resultClass
     *            the result class
     *
     * @return the interrupted transfers per destination
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getInterruptedTransfersPerDestination(final String paramDestination,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getInterruptedTransfersPerDestination", resultClass,
                new String[] { "destination=" + paramDestination });
    }

    /**
     * Gets the meta data by attribute.
     *
     * @param paramAttribute
     *            the param attribute
     *
     * @return the meta data by attribute
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getMetaDataByAttribute(final String paramAttribute) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getMetaDataByAttribute",
                new String[] { "attribute=" + paramAttribute });
    }

    /**
     * Gets the meta data by data file.
     *
     * @param <T>
     *            the generic type
     * @param paramDatafile
     *            the param datafile
     * @param resultClass
     *            the result class
     *
     * @return the meta data by data file
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getMetaDataByDataFile(final long paramDatafile, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getMetaDataByDataFile", resultClass,
                new String[] { "datafile=" + paramDatafile });
    }

    /**
     * Gets the pending data transfers.
     *
     * @param paramDestination
     *            the param destination
     * @param paramBefore
     *            the param before
     * @param paramLimit
     *            the param limit
     *
     * @return the pending data transfers
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getPendingDataTransfers(final String paramDestination, final java.sql.Timestamp paramBefore,
            final int paramLimit) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getPendingDataTransfers", new String[] {
                "destination=" + paramDestination, "before=" + paramBefore.getTime(), "limit=" + paramLimit });
    }

    /**
     * Gets the data files by group by.
     *
     * @return the data files by group by
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataFilesByGroupBy() throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataFilesByGroupBy");
    }

    /**
     * Gets the product status.
     *
     * @param paramStream
     *            the param stream
     * @param paramTime
     *            the param time
     * @param paramType
     *            the param type
     * @param paramStep
     *            the param step
     * @param paramLimit
     *            the param limit
     *
     * @return the product status
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getProductStatus(final String paramStream, final String paramTime, final String paramType,
            final long paramStep, final int paramLimit) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getProductStatus", new String[] { "stream=" + paramStream,
                "time=" + paramTime, "type=" + paramType, "step=" + paramStep, "limit=" + paramLimit });
    }

    /**
     * Gets the scheduled data transfer.
     *
     * @param <T>
     *            the generic type
     * @param paramUniqueKey
     *            the param unique key
     * @param paramDestination
     *            the param destination
     * @param resultClass
     *            the result class
     *
     * @return the scheduled data transfer
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getScheduledDataTransfer(final String paramUniqueKey,
            final String paramDestination, final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getScheduledDataTransfer", resultClass,
                new String[] { "uniqueKey=" + paramUniqueKey, "destination=" + paramDestination });
    }

    /**
     * Gets the transfer history per data transfer.
     *
     * @param paramId
     *            the param id
     *
     * @return the transfer history per data transfer
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getTransferHistoryPerDataTransfer(final long paramId) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getTransferHistoryPerDataTransfer",
                new String[] { "id=" + paramId });
    }

    /**
     * Gets the sorted transfer history per data transfer.
     *
     * @param paramId
     *            the param id
     * @param paramAfterScheduleTime
     *            the param after schedule time
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramLength
     *            the param length
     *
     * @return the sorted transfer history per data transfer
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getSortedTransferHistoryPerDataTransfer(final long paramId, final boolean paramAfterScheduleTime,
            final String paramSort, final String paramOrder, final int paramStart, final int paramLength)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getSortedTransferHistoryPerDataTransfer",
                new String[] { "id=" + paramId, "afterScheduleTime=" + paramAfterScheduleTime, "sort=" + paramSort,
                        "order=" + paramOrder, "start=" + paramStart, "length=" + paramLength });
    }

    /**
     * Gets the sorted transfer history per destination on history date.
     *
     * @param paramDestination
     *            the param destination
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramLength
     *            the param length
     *
     * @return the sorted transfer history per destination on history date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getSortedTransferHistoryPerDestinationOnHistoryDate(final String paramDestination,
            final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate, final String paramSort,
            final String paramOrder, final int paramStart, final int paramLength) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getSortedTransferHistoryPerDestinationOnHistoryDate",
                new String[] { "destination=" + paramDestination, "fromDate=" + paramFromDate.getTime(),
                        "toDate=" + paramToDate.getTime(), "sort=" + paramSort, "order=" + paramOrder,
                        "start=" + paramStart, "length=" + paramLength });
    }

    /**
     * Gets the sorted transfer history per destination on product date.
     *
     * @param paramDestination
     *            the param destination
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramSort
     *            the param sort
     * @param paramOrder
     *            the param order
     * @param paramStart
     *            the param start
     * @param paramLength
     *            the param length
     *
     * @return the sorted transfer history per destination on product date
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getSortedTransferHistoryPerDestinationOnProductDate(final String paramDestination,
            final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate, final String paramSort,
            final String paramOrder, final int paramStart, final int paramLength) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getSortedTransferHistoryPerDestinationOnProductDate",
                new String[] { "destination=" + paramDestination, "fromDate=" + paramFromDate.getTime(),
                        "toDate=" + paramToDate.getTime(), "sort=" + paramSort, "order=" + paramOrder,
                        "start=" + paramStart, "length=" + paramLength });
    }

    /**
     * Gets the transfer methods by ectrans module name.
     *
     * @param <T>
     *            the generic type
     * @param paramModule
     *            the param module
     * @param resultClass
     *            the result class
     *
     * @return the transfer methods by ectrans module name
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getTransferMethodsByEcTransModuleName(final String paramModule,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getTransferMethodsByEcTransModuleName", resultClass,
                new String[] { "module=" + paramModule });
    }

    /**
     * Gets the transfer servers.
     *
     * @param <T>
     *            the generic type
     * @param paramGroup
     *            the param group
     * @param resultClass
     *            the result class
     *
     * @return the transfer servers
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getTransferServers(final String paramGroup, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getTransferServers", resultClass,
                new String[] { "group=" + paramGroup });
    }

    /**
     * Gets the transfer servers by data file id.
     *
     * @param <T>
     *            the generic type
     * @param paramDataFileId
     *            the param data file id
     * @param resultClass
     *            the result class
     *
     * @return the transfer servers by data file id
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getTransferServersByDataFileId(final long paramDataFileId,
            final Class<T> resultClass) throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getTransferServersByDataFileId", resultClass,
                new String[] { "dataFileId=" + paramDataFileId });
    }

    /**
     * Gets the statistics.
     *
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramGroup
     *            the param group
     * @param paramCode
     *            the param code
     * @param paramType
     *            the param type
     *
     * @return the statistics
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getStatistics(final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate,
            final String paramGroup, final String paramCode, final String paramType) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getStatistics",
                new String[] { "fromDate=" + paramFromDate.getTime(), "toDate=" + paramToDate.getTime(),
                        "group=" + paramGroup, "code=" + paramCode, "type=" + paramType });
    }

    /**
     * Gets the rates.
     *
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramCaller
     *            the param caller
     * @param paramSourceHost
     *            the param source host
     *
     * @return the rates
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getRates(final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate,
            final String paramCaller, final String paramSourceHost) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getRates", new String[] { "fromDate=" + paramFromDate.getTime(),
                "toDate=" + paramToDate.getTime(), "caller=" + paramCaller, "sourceHost=" + paramSourceHost });
    }

    /**
     * Gets the rates per transfer server.
     *
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramCaller
     *            the param caller
     * @param paramSourceHost
     *            the param source host
     *
     * @return the rates per transfer server
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getRatesPerTransferServer(final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate,
            final String paramCaller, final String paramSourceHost) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getRatesPerTransferServer",
                new String[] { "fromDate=" + paramFromDate.getTime(), "toDate=" + paramToDate.getTime(),
                        "caller=" + paramCaller, "sourceHost=" + paramSourceHost });
    }

    /**
     * Gets the rates per file system.
     *
     * @param paramFromDate
     *            the param from date
     * @param paramToDate
     *            the param to date
     * @param paramTransferServerName
     *            the param transfer server name
     * @param paramCaller
     *            the param caller
     * @param paramSourceHost
     *            the param source host
     *
     * @return the rates per file system
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getRatesPerFileSystem(final java.sql.Timestamp paramFromDate, final java.sql.Timestamp paramToDate,
            final String paramTransferServerName, final String paramCaller, final String paramSourceHost)
            throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getRatesPerFileSystem",
                new String[] { "fromDate=" + paramFromDate.getTime(), "toDate=" + paramToDate.getTime(),
                        "transferServer=" + paramTransferServerName, "caller=" + paramCaller,
                        "sourceHost=" + paramSourceHost });
    }

    /**
     * Gets the urls per category.
     *
     * @param paramId
     *            the param id
     *
     * @return the urls per category
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getUrlsPerCategory(final String paramId) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getUrlsPerCategory", new String[] { "id=" + paramId });
    }

    /**
     * Purge data base.
     *
     * @param paramCurrentTime
     *            the param current time
     * @param paramPurgeDate
     *            the param purge date
     * @param paramPurgeTime
     *            the param purge time
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int purgeDataBase(final java.sql.Timestamp paramCurrentTime, final java.sql.Date paramPurgeDate,
            final java.sql.Time paramPurgeTime) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "purgeDataBase",
                new String[] { "currentTime=" + paramCurrentTime.getTime(), "purgeDate=" + paramPurgeDate,
                        "purgeTime=" + paramPurgeTime });
    }

    /**
     * Reschedule all data transfers belonging to a specific group to the current time.
     *
     * @param paramGroupBy
     *            the param group by
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int resetDataTransferSchedulesByGroup(final String paramGroupBy) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "resetDataTransferSchedulesByGroup",
                new String[] { "groupBy=" + paramGroupBy });
    }

    /**
     * Purge data file.
     *
     * @param paramDataFileId
     *            the DataFile Id
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int purgeDataFile(final long paramDataFileId) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "purgeDataFile", new String[] { "dataFileId=" + paramDataFileId });
    }

    /**
     * Removes the destination.
     *
     * @param paramDestinationName
     *            the param destination name
     * @param remove
     *            the remove
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int removeDestination(final String paramDestinationName, final boolean remove) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "removeDestination",
                new String[] { "destinationName=" + paramDestinationName, "remove=" + (remove ? "true" : "false") });
    }

    /**
     * Removes the host.
     *
     * @param paramHostName
     *            the param host name
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int removeHost(final String paramHostName) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "removeHost", new String[] { "hostName=" + paramHostName });
    }

    /**
     * Removes the web user.
     *
     * @param paramWebUserId
     *            the param web user id
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int removeWebUser(final String paramWebUserId) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "removeWebUser", new String[] { "webUserId=" + paramWebUserId });
    }

    /**
     * Removes the category.
     *
     * @param paramCategoryId
     *            the param category id
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int removeCategory(final String paramCategoryId) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "removeCategory", new String[] { "categoryId=" + paramCategoryId });
    }

    /**
     * Removes the url.
     *
     * @param paramUrlName
     *            the param url name
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int removeUrl(final String paramUrlName) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "removeUrl", new String[] { "urlName=" + paramUrlName });
    }

    /**
     * Removes the IncomingUser.
     *
     * @param paramUserId
     *            the param user id
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int removeIncomingUser(final String paramUserId) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "removeIncomingUser", new String[] { "id=" + paramUserId });
    }

    /**
     * Removes the IncomingPolicy.
     *
     * @param paramPolicyId
     *            the param policy id
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int removeIncomingPolicy(final String paramPolicyId) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "removeIncomingPolicy", new String[] { "id=" + paramPolicyId });
    }

    /**
     * Reset product status.
     *
     * @param paramMetaStream
     *            the param meta stream
     * @param paramMetaTime
     *            the param meta time
     * @param paramTimeStep
     *            the param time step
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int resetProductStatus(final String paramMetaStream, final String paramMetaTime, final long paramTimeStep)
            throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "resetProductStatus", new String[] {
                "metaStream=" + paramMetaStream, "metaTime=" + paramMetaTime, "timeStep=" + paramTimeStep });
    }

    /**
     * Gets the data transfers by destination and meta data.
     *
     * @param paramFrom
     *            the param from
     * @param paramTo
     *            the param to
     * @param paramDestination
     *            the param destination
     * @param paramMetaStream
     *            the param meta stream
     * @param paramMetaTime
     *            the param meta time
     *
     * @return the data transfers by destination and meta data
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getDataTransfersByDestinationAndMetaData(final java.sql.Timestamp paramFrom,
            final java.sql.Timestamp paramTo, final String paramDestination, final String paramMetaStream,
            final String paramMetaTime) throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getDataTransfersByDestinationAndMetaData",
                new String[] { "from=" + paramFrom.getTime(), "to=" + paramTo.getTime(),
                        "destination=" + paramDestination, "metaStream=" + paramMetaStream,
                        "metaTime=" + paramMetaTime });
    }

    /**
     * Gets the users per category.
     *
     * @param <T>
     *            the generic type
     * @param paramId
     *            the param id
     * @param resultClass
     *            the result class
     *
     * @return the users per category
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getUsersPerCategory(final String paramId, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getUsersPerCategory", resultClass,
                new String[] { "id=" + paramId });
    }

    /**
     * Gets the user policies per user id.
     *
     * @param <T>
     *            the generic type
     * @param paramId
     *            the param id
     * @param resultClass
     *            the result class
     *
     * @return the users per category
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    <T extends DataBaseObject> DBIterator<T> getPolicyUserList(final String paramId, final Class<T> resultClass)
            throws SQLException, IOException {
        return _database.executeQuery("ECpdsBase", "getPolicyUserList", resultClass, new String[] { "id=" + paramId });
    }

    /**
     * Gets the expired data transfers.
     *
     * @return the expired data transfers
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    DBResultSet getExpiredDataTransfers() throws SQLException, IOException {
        return _database.executeSelect("ECpdsBase", "getExpiredDataTransfers");
    }

    /**
     * Removes the data transfer.
     *
     * @param paramTransferId
     *            the param transfer id
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int removeDataTransfer(final long paramTransferId) throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "removeDataTransfer",
                new String[] { "transferId=" + paramTransferId });
    }

    /**
     * Reset the data transfer queue and retry time to scheduled time.
     *
     * @param paramDestinationName
     *            the param destination name
     *
     * @return the int
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int resetDataTransferQueueAndRetryTimeToScheduledTime(final String paramDestinationName)
            throws SQLException, IOException {
        return _database.executeUpdate("ECpdsBase", "resetDataTransferQueueAndRetryTimeToScheduledTime",
                new String[] { "destinationName=" + paramDestinationName });
    }
}
