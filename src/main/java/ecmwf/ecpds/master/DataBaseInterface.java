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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ecmwf.common.database.Alias;
import ecmwf.common.database.Association;
import ecmwf.common.database.CatUrl;
import ecmwf.common.database.Category;
import ecmwf.common.database.ChangeLog;
import ecmwf.common.database.Country;
import ecmwf.common.database.DataBaseCursor;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataBaseObject;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.DestinationBackup;
import ecmwf.common.database.DestinationECUser;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Event;
import ecmwf.common.database.Host;
import ecmwf.common.database.HostECUser;
import ecmwf.common.database.IncomingHistory;
import ecmwf.common.database.IncomingPolicy;
import ecmwf.common.database.IncomingUser;
import ecmwf.common.database.MetadataAttribute;
import ecmwf.common.database.MetadataValue;
import ecmwf.common.database.Operation;
import ecmwf.common.database.PolicyAssociation;
import ecmwf.common.database.ProductStatus;
import ecmwf.common.database.Rates;
import ecmwf.common.database.Statistics;
import ecmwf.common.database.Traffic;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferHistory;
import ecmwf.common.database.TransferMethod;
import ecmwf.common.database.TransferServer;
import ecmwf.common.database.Url;
import ecmwf.common.database.WebUser;
import ecmwf.common.database.WeuCat;
import ecmwf.ecpds.master.transfer.DestinationOption.TypeEntry;

/**
 * The Interface DataBaseInterface.
 */
public interface DataBaseInterface extends Remote {
    /**
     * Insert.
     *
     * @param session
     *            the session
     * @param object
     *            the object
     * @param createPk
     *            the create pk
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void insert(ECpdsSession session, DataBaseObject object, boolean createPk)
            throws MasterException, DataBaseException, RemoteException;

    /**
     * Update.
     *
     * @param session
     *            the session
     * @param object
     *            the object
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void update(ECpdsSession session, DataBaseObject object) throws MasterException, DataBaseException, RemoteException;

    /**
     * Removes the.
     *
     * @param session
     *            the session
     * @param object
     *            the object
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void remove(ECpdsSession session, DataBaseObject object) throws MasterException, DataBaseException, RemoteException;

    /**
     * Gets the initial data transfer events.
     *
     * @param target
     *            the target
     *
     * @return the initial data transfer events
     *
     * @throws RemoteException
     *             the remote exception
     */
    void getInitialDataTransferEvents(String target) throws RemoteException;

    /**
     * Gets the initial change host events.
     *
     * @param target
     *            the target
     *
     * @return the initial change host events
     *
     * @throws RemoteException
     *             the remote exception
     */
    void getInitialChangeHostEvents(String target) throws RemoteException;

    /**
     * Gets the initial product status events.
     *
     * @param target
     *            the target
     *
     * @return the initial product status events
     *
     * @throws RemoteException
     *             the remote exception
     */
    void getInitialProductStatusEvents(String target) throws RemoteException;

    /**
     * Gets the transfer servers.
     *
     * @param groupName
     *            the group name
     *
     * @return the transfer servers
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    TransferServer[] getTransferServers(String groupName) throws DataBaseException, RemoteException;

    /**
     * Gets the destination ecuser.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination ecuser
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    ECUser[] getDestinationEcuser(String destinationName) throws DataBaseException, RemoteException;

    /**
     * Gets the destination incoming policies.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination incoming policies
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    IncomingPolicy[] getDestinationIncomingPolicies(String destinationName) throws DataBaseException, RemoteException;

    /**
     * Gets the incoming users for an incoming policy.
     *
     * @param policyId
     *            the policy id
     *
     * @return the incoming users
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    IncomingUser[] getIncomingUsersForIncomingPolicy(String policyId) throws DataBaseException, RemoteException;

    /**
     * Gets the incoming policies for an incoming user.
     *
     * @param userId
     *            the incoming user id
     *
     * @return the incoming policies
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    IncomingPolicy[] getIncomingPoliciesForIncomingUser(String userId) throws DataBaseException, RemoteException;

    /**
     * Gets operations for an incoming user.
     *
     * @param userId
     *            the incoming user id
     *
     * @return the operations
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Operation[] getOperationsForIncomingUser(String userId) throws DataBaseException, RemoteException;

    /**
     * Gets the destinations for an incoming user.
     *
     * @param userId
     *            the incoming user id
     *
     * @return the destinations
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Destination[] getDestinationsForIncomingUser(String userId) throws DataBaseException, RemoteException;

    /**
     * Gets the destinations for an incoming policy.
     *
     * @param policyId
     *            the incoming policy id
     *
     * @return the destinations
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Destination[] getDestinationsForIncomingPolicy(String policyId) throws DataBaseException, RemoteException;

    /**
     * Gets the statistics.
     *
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @param groupName
     *            the group name
     * @param status
     *            the status
     * @param type
     *            the type
     *
     * @return the statistics
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Statistics[] getStatistics(Date fromDate, Date toDate, String groupName, String status, String type)
            throws DataBaseException, RemoteException;

    /**
     * Gets the rates.
     *
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @param caller
     *            the caller
     * @param sourceHost
     *            the source host
     *
     * @return the rates
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Rates[] getRates(Date fromDate, Date toDate, String caller, String sourceHost)
            throws DataBaseException, RemoteException;

    /**
     * Gets the rates per transfer server.
     *
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @param caller
     *            the caller
     * @param sourceHost
     *            the source host
     *
     * @return the rates per transfer server
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Rates[] getRatesPerTransferServer(Date fromDate, Date toDate, String caller, String sourceHost)
            throws DataBaseException, RemoteException;

    /**
     * Gets the rates per file system.
     *
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @param transferServerName
     *            the transfer server name
     * @param caller
     *            the caller
     * @param sourceHost
     *            the source host
     *
     * @return the rates per file system
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Rates[] getRatesPerFileSystem(Date fromDate, Date toDate, String transferServerName, String caller,
            String sourceHost) throws DataBaseException, RemoteException;

    /**
     * Gets the data transfer count not done by product and time on date.
     *
     * @param destination
     *            the destination
     * @param product
     *            the product
     * @param time
     *            the time
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the data transfer count not done by product and time on date
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    int getDataTransferCountNotDoneByProductAndTimeOnDate(String destination, String product, String time, Date from,
            Date to) throws DataBaseException, RemoteException;

    /**
     * Gets the destination aliases.
     *
     * @param name
     *            the name
     * @param mode
     *            the mode
     *
     * @return the destination aliases
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Destination[] getDestinationAliases(String name, String mode) throws DataBaseException, RemoteException;

    /**
     * Gets the aliases.
     *
     * @param name
     *            the name
     * @param mode
     *            the mode
     *
     * @return the aliases
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Alias[] getAliases(String name, String mode) throws DataBaseException, RemoteException;

    /**
     * Gets the ecuser events.
     *
     * @param userName
     *            the user name
     * @param onIsoDate
     *            the on iso date
     * @param search
     *            the search
     * @param cursor
     *            the cursor
     *
     * @return the ecuser events
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Event> getECuserEvents(String userName, Date onIsoDate, String search, DataBaseCursor cursor)
            throws DataBaseException, RemoteException;

    /**
     * Gets the incoming history list.
     *
     * @param incomingUserId
     *            the incoming user id
     * @param onIsoDate
     *            the on iso date
     * @param search
     *            the search
     * @param cursor
     *            the cursor
     *
     * @return the incoming history list
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<IncomingHistory> getIncomingHistory(String incomingUserId, Date onIsoDate, String search,
            DataBaseCursor cursor) throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by host name.
     *
     * @param name
     *            the name
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the data transfers by host name
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getDataTransfersByHostName(String name, Date from, Date to)
            throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by transfer server name.
     *
     * @param name
     *            the name
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the data transfers by transfer server name
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getDataTransfersByTransferServerName(String name, Date from, Date to)
            throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by status code and date.
     *
     * @param status
     *            the status
     * @param from
     *            the from
     * @param to
     *            the to
     * @param search
     *            the search
     * @param type
     *            the type
     * @param cursor
     *            the cursor
     *
     * @return the data transfers by status code and date
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getDataTransfersByStatusCodeAndDate(String status, Date from, Date to, String search,
            String type, DataBaseCursor cursor) throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by data file id.
     *
     * @param dataFileId
     *            the dataFile id
     * @param includeDeleted
     *            the include deleted
     *
     * @return the data transfers by data file id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getDataTransfersByDataFileId(long dataFileId, boolean includeDeleted)
            throws DataBaseException, RemoteException;

    /**
     * Gets the destinations by country iso.
     *
     * @param isoCode
     *            the iso code
     *
     * @return the destinations by country iso
     *
     * @throws RemoteException
     *             the remote exception
     */
    Destination[] getDestinationsByCountryISO(String isoCode) throws RemoteException;

    /**
     * Gets the destinations by user.
     *
     * @param uid
     *            the uid
     * @param search
     *            the search
     * @param fromToAliases
     *            the from to aliases
     * @param asc
     *            the asc
     * @param status
     *            the status
     * @param type
     *            the type
     * @param filter
     *            the filter
     *
     * @return the destinations by user
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    Destination[] getDestinationsByUser(String uid, String search, String fromToAliases, boolean asc, String status,
            String type, String filter) throws IOException;

    /**
     * Gets the destinations by host name.
     *
     * @param hostName
     *            the host name
     *
     * @return the destinations by host name
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Destination[] getDestinationsByHostName(String hostName) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer history.
     *
     * @param dataTransferId
     *            the dataTransfer Id
     *
     * @return the list of transfer history associated to this dataTransfer
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    TransferHistory[] getTransferHistoryByDataTransferId(long dataTransferId) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer history.
     *
     * @param dataTransferId
     *            the dataTransfer id
     * @param afterScheduleTime
     *            the after schedule time
     * @param cursor
     *            the cursor
     *
     * @return the transfer history
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    TransferHistory[] getTransferHistoryByDataTransferId(long dataTransferId, boolean afterScheduleTime,
            DataBaseCursor cursor) throws DataBaseException, RemoteException;

    /**
     * Gets the categories per user id.
     *
     * @param userId
     *            the user id
     *
     * @return the categories per user id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Category> getCategoriesPerUserId(String userId) throws DataBaseException, RemoteException;

    /**
     * Gets the urls per category id.
     *
     * @param id
     *            the id
     *
     * @return the urls per category id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Url> getUrlsPerCategoryId(String id) throws DataBaseException, RemoteException;

    /**
     * Gets the categories per resource id.
     *
     * @param id
     *            the id
     *
     * @return the categories per resource id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Category> getCategoriesPerResourceId(String id) throws DataBaseException, RemoteException;

    /**
     * Gets the data files by meta data.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param from
     *            the from
     * @param to
     *            the to
     * @param cursor
     *            the cursor
     *
     * @return the data files by meta data
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataFile> getDataFilesByMetaData(String name, String value, Date from, Date to, DataBaseCursor cursor)
            throws DataBaseException, RemoteException;

    /**
     * Gets the transfer count and meta data by filter.
     *
     * @param countBy
     *            the count by
     * @param destination
     *            the destination
     * @param target
     *            the target
     * @param stream
     *            the stream
     * @param time
     *            the time
     * @param status
     *            the status
     * @param fileName
     *            the file name
     * @param from
     *            the from
     * @param to
     *            the to
     * @param privilegedUser
     *            the privileged user
     * @param scheduledBefore
     *            the scheduled before
     *
     * @return the transfer count and meta data by filter
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<List<String>> getTransferCountAndMetaDataByFilter(String countBy, String destination, String target,
            String stream, String time, String status, String fileName, Date from, Date to, String privilegedUser,
            Date scheduledBefore) throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by filter.
     *
     * @param destination
     *            the destination
     * @param target
     *            the target
     * @param stream
     *            the stream
     * @param time
     *            the time
     * @param status
     *            the status
     * @param privilegedUser
     *            the privileged user
     * @param scheduledBefore
     *            the scheduled before
     * @param fileName
     *            the file name
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the data transfers by filter
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransferWithPermissions> getDataTransfersByFilter(String destination, String target, String stream,
            String time, String status, String privilegedUser, Date scheduledBefore, String fileName, Date from,
            Date to) throws MasterException, DataBaseException, RemoteException;

    /**
     * Gets the data transfers by filter.
     *
     * @param destination
     *            the destination
     * @param target
     *            the target
     * @param stream
     *            the stream
     * @param time
     *            the time
     * @param status
     *            the status
     * @param privilegedUser
     *            the privileged user
     * @param scheduledBefore
     *            the scheduled before
     * @param fileName
     *            the file name
     * @param from
     *            the from
     * @param to
     *            the to
     * @param cursor
     *            the cursor
     *
     * @return the data transfers by filter
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransferWithPermissions> getDataTransfersByFilter(String destination, String target, String stream,
            String time, String status, String privilegedUser, Date scheduledBefore, String fileName, Date from,
            Date to, DataBaseCursor cursor) throws MasterException, DataBaseException, RemoteException;

    /**
     * Gets the hosts.
     *
     * @param label
     *            the label
     * @param filter
     *            the filter
     * @param network
     *            the network
     * @param type
     *            the type
     * @param search
     *            the search
     * @param cursor
     *            the cursor
     *
     * @return the hosts
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Host> getFilteredHosts(String label, String filter, String network, String type, String search,
            DataBaseCursor cursor) throws DataBaseException, RemoteException;

    /**
     * Gets the hosts by destination id.
     *
     * @param destId
     *            the dest id
     *
     * @return the hosts by destination id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Host> getHostsByDestinationId(String destId) throws DataBaseException, RemoteException;

    /**
     * Gets the hosts by transfer method id.
     *
     * @param transferMethodId
     *            the transfer method id
     *
     * @return the hosts by transfer method id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Host> getHostsByTransferMethodId(String transferMethodId) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer methods by ec trans module name.
     *
     * @param ecTransModuleName
     *            the ec trans module name
     *
     * @return the transfer methods by ectrans module name
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<TransferMethod> getTransferMethodsByEcTransModuleName(String ecTransModuleName)
            throws DataBaseException, RemoteException;

    /**
     * Gets the meta data by data file id.
     *
     * @param dataFileId
     *            the dataFile id
     *
     * @return the meta data by data file id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<MetadataValue> getMetaDataByDataFileId(long dataFileId) throws DataBaseException, RemoteException;

    /**
     * Gets the meta data by attribute name.
     *
     * @param id
     *            the id
     *
     * @return the meta data by attribute name
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<MetadataValue> getMetaDataByAttributeName(String id) throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by destination and identity.
     *
     * @param destination
     *            the destination
     * @param identity
     *            the identity
     *
     * @return the data transfers by destination and identity
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getDataTransfersByDestinationAndIdentity(String destination, String identity)
            throws DataBaseException, RemoteException;

    /**
     * Gets the transfer count with destination and metadata value by metadata name.
     *
     * @param metadataName
     *            the metadata name
     *
     * @return the transfer count with destination and metadata value by metadata name
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<List<String>> getTransferCountWithDestinationAndMetadataValueByMetadataName(String metadataName)
            throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by destination product and time on date.
     *
     * @param destinationName
     *            the destination name
     * @param product
     *            the product
     * @param time
     *            the time
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     *
     * @return the data transfers by destination product and time on date
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getDataTransfersByDestinationProductAndTimeOnDate(String destinationName, String product,
            String time, Date fromIsoDate, Date toIsoDate) throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by destination on date.
     *
     * @param destinationName
     *            the destination name
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     *
     * @return the data transfers by destination on date
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getDataTransfersByDestinationOnDate(String destinationName, Date fromIsoDate,
            Date toIsoDate) throws DataBaseException, RemoteException;

    /**
     * Gets the data transfers by destination on transmission date.
     *
     * @param destinationName
     *            the destination name
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     *
     * @return the data transfers by destination on transmission date
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getDataTransfersByDestinationOnTransmissionDate(String destinationName, Date fromIsoDate,
            Date toIsoDate) throws DataBaseException, RemoteException;

    /**
     * Gets the bad data transfers by destination.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the bad data transfers by destination
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<DataTransfer> getBadDataTransfersByDestination(String destinationName)
            throws DataBaseException, RemoteException;

    /**
     * Gets the bad data transfers by destination count.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the bad data transfers by destination count
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    int getBadDataTransfersByDestinationCount(String destinationName) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer history by destination on product date.
     *
     * @param destinationName
     *            the destination name
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     * @param cursor
     *            the cursor
     *
     * @return the transfer history by destination on product date
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<TransferHistory> getTransferHistoryByDestinationOnProductDate(String destinationName, Date fromIsoDate,
            Date toIsoDate, DataBaseCursor cursor) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer history by destination on history date.
     *
     * @param destinationName
     *            the destination name
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     * @param cursor
     *            the cursor
     *
     * @return the transfer history by destination on history date
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<TransferHistory> getTransferHistoryByDestinationOnHistoryDate(String destinationName, Date fromIsoDate,
            Date toIsoDate, DataBaseCursor cursor) throws DataBaseException, RemoteException;

    /**
     * Gets the allowed ec users by host name.
     *
     * @param hostName
     *            the host name
     *
     * @return the allowed ec users by host name
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<ECUser> getAllowedEcUsersByHostName(String hostName) throws DataBaseException, RemoteException;

    /**
     * Gets the traffic by destination name.
     *
     * @param destinationName
     *            the destinationname
     *
     * @return the traffic
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Traffic> getTrafficByDestinationName(final String destinationName)
            throws DataBaseException, RemoteException;

    /**
     * Gets the change log by key.
     *
     * @param keyName
     *            the key name
     * @param keyValue
     *            the key value
     *
     * @return the change log
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<ChangeLog> getChangeLogByKey(final String keyName, final String keyValue)
            throws DataBaseException, RemoteException;

    /**
     * Gets the data file.
     *
     * @param dataFileId
     *            the dataFile id
     *
     * @return the data file
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    DataFile getDataFile(long dataFileId) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer group.
     *
     * @param name
     *            the name
     *
     * @return the transfer group
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    TransferGroup getTransferGroup(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the metadata attribute.
     *
     * @param name
     *            the name
     *
     * @return the metadata attribute
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    MetadataAttribute getMetadataAttribute(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the metadata attribute array.
     *
     * @return the metadata attribute array
     *
     * @throws RemoteException
     *             the remote exception
     */
    MetadataAttribute[] getMetadataAttributeArray() throws RemoteException;

    /**
     * Gets the cat url array.
     *
     * @return the cat url array
     *
     * @throws RemoteException
     *             the remote exception
     */
    CatUrl[] getCatUrlArray() throws RemoteException;

    /**
     * Gets the transfer group array.
     *
     * @return the transfer group array
     *
     * @throws RemoteException
     *             the remote exception
     */
    TransferGroup[] getTransferGroupArray() throws RemoteException;

    /**
     * Gets the product status.
     *
     * @param stream
     *            the stream
     * @param time
     *            the time
     * @param type
     *            the type
     * @param step
     *            the step
     * @param limit
     *            the limit
     *
     * @return the product status
     *
     * @throws RemoteException
     *             the remote exception
     * @throws DataBaseException
     *             the data base exception
     */
    ProductStatus[] getProductStatus(String stream, String time, String type, long step, int limit)
            throws RemoteException, DataBaseException;

    /**
     * Gets the transfer server.
     *
     * @param name
     *            the name
     *
     * @return the transfer server
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    TransferServer getTransferServer(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer server array.
     *
     * @return the transfer server array
     *
     * @throws RemoteException
     *             the remote exception
     */
    TransferServer[] getTransferServerArray() throws RemoteException;

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    ECUser getECUser(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the incoming policy.
     *
     * @param name
     *            the name
     *
     * @return the incoming policy
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    IncomingPolicy getIncomingPolicy(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the operation.
     *
     * @param name
     *            the name
     *
     * @return the operation
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Operation getOperation(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the incoming user.
     *
     * @param name
     *            the name
     *
     * @return the incoming user
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    IncomingUser getIncomingUser(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the incoming user array.
     *
     * @return the incoming user array
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    IncomingUser[] getIncomingUserArray() throws DataBaseException, RemoteException;

    /**
     * Gets the destination.
     *
     * @param name
     *            the name
     * @param useCache
     *            use the cache?
     *
     * @return the destination
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Destination getDestination(String name, boolean useCache) throws DataBaseException, RemoteException;

    /**
     * Gets the EC user array.
     *
     * @return the EC user array
     *
     * @throws RemoteException
     *             the remote exception
     */
    ECUser[] getECUserArray() throws RemoteException;

    /**
     * Gets the incoming policy array.
     *
     * @return the incoming policy array
     *
     * @throws RemoteException
     *             the remote exception
     */
    IncomingPolicy[] getIncomingPolicyArray() throws RemoteException;

    /**
     * Gets the operation array.
     *
     * @return the operation array
     *
     * @throws RemoteException
     *             the remote exception
     */
    Operation[] getOperationArray() throws RemoteException;

    /**
     * Gets the country.
     *
     * @param iso
     *            the iso
     *
     * @return the country
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Country getCountry(String iso) throws DataBaseException, RemoteException;

    /**
     * Gets the country array.
     *
     * @return the country array
     *
     * @throws RemoteException
     *             the remote exception
     */
    Country[] getCountryArray() throws RemoteException;

    /**
     * Gets the data transfer.
     *
     * @param dataTransferId
     *            the dataTransfer id
     *
     * @return the data transfer
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    DataTransfer getDataTransfer(long dataTransferId) throws DataBaseException, RemoteException;

    /**
     * Gets the destination array.
     *
     * @return the destination array
     *
     * @throws RemoteException
     *             the remote exception
     */
    Destination[] getDestinationArray() throws RemoteException;

    /**
     * Gets the destination array.
     *
     * @param monitored
     *            is it only for monitored destinations
     *
     * @return the destination array
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Destination[] getDestinationArray(boolean monitored) throws DataBaseException, RemoteException;

    /**
     * Gets the destination names and comments sorted by names.
     *
     * @return the destination names and comments
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Set<Map.Entry<String, String>> getDestinationNamesAndComments() throws DataBaseException, RemoteException;

    /**
     * Gets the destination ec user.
     *
     * @param destinationName
     *            the destination name
     * @param ecuserName
     *            the ecuser name
     *
     * @return the destination ec user
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    DestinationECUser getDestinationECUser(String destinationName, String ecuserName)
            throws DataBaseException, RemoteException;

    /**
     * Gets the association.
     *
     * @param destinationName
     *            the destination name
     * @param hostName
     *            the host name
     *
     * @return the association
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Association getAssociation(String destinationName, String hostName) throws DataBaseException, RemoteException;

    /**
     * Gets the policy association.
     *
     * @param destinationName
     *            the destination name
     * @param policyId
     *            the policy id
     *
     * @return the association
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    PolicyAssociation getPolicyAssociation(String destinationName, String policyId)
            throws DataBaseException, RemoteException;

    /**
     * Gets the alias.
     *
     * @param desName
     *            the des name
     * @param destinationName
     *            the destination name
     *
     * @return the alias
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Alias getAlias(String desName, String destinationName) throws DataBaseException, RemoteException;

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    ECtransModule getECtransModule(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the ectrans module array.
     *
     * @return the ectrans module array
     *
     * @throws RemoteException
     *             the remote exception
     */
    ECtransModule[] getECtransModuleArray() throws RemoteException;

    /**
     * Gets the host.
     *
     * @param name
     *            the name
     *
     * @return the host
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Host getHost(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the host array.
     *
     * @return the host array
     *
     * @throws RemoteException
     *             the remote exception
     */
    Host[] getHostArray() throws RemoteException;

    /**
     * Gets the transfer method.
     *
     * @param name
     *            the name
     *
     * @return the transfer method
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    TransferMethod getTransferMethod(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the host ec user.
     *
     * @param ecuserName
     *            the ecuser name
     * @param hostName
     *            the host name
     *
     * @return the host ec user
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    HostECUser getHostECUser(String ecuserName, String hostName) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer history.
     *
     * @param transferHistoryId
     *            the TransferHistory id
     *
     * @return the transfer history
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    TransferHistory getTransferHistory(long transferHistoryId) throws DataBaseException, RemoteException;

    /**
     * Gets the transfer method array.
     *
     * @return the transfer method array
     *
     * @throws RemoteException
     *             the remote exception
     */
    TransferMethod[] getTransferMethodArray() throws RemoteException;

    /**
     * Gets the category.
     *
     * @param id
     *            the id
     *
     * @return the category
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Category getCategory(long id) throws DataBaseException, RemoteException;

    /**
     * Gets the category array.
     *
     * @return the category array
     *
     * @throws RemoteException
     *             the remote exception
     */
    Category[] getCategoryArray() throws RemoteException;

    /**
     * Gets the cat url.
     *
     * @param categoryId
     *            the category id
     * @param urlName
     *            the url name
     *
     * @return the cat url
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    CatUrl getCatUrl(long categoryId, String urlName) throws DataBaseException, RemoteException;

    /**
     * Gets the url array.
     *
     * @return the url array
     *
     * @throws RemoteException
     *             the remote exception
     */
    Url[] getUrlArray() throws RemoteException;

    /**
     * Gets the url.
     *
     * @param name
     *            the name
     *
     * @return the url
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Url getUrl(String name) throws DataBaseException, RemoteException;

    /**
     * Gets the web user.
     *
     * @param id
     *            the id
     *
     * @return the web user
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    WebUser getWebUser(String id) throws DataBaseException, RemoteException;

    /**
     * Gets the web user array.
     *
     * @return the web user array
     *
     * @throws RemoteException
     *             the remote exception
     */
    WebUser[] getWebUserArray() throws RemoteException;

    /**
     * Gets the weu cat.
     *
     * @param categoryId
     *            the category id
     * @param webuserId
     *            the webuser id
     *
     * @return the weu cat
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    WeuCat getWeuCat(long categoryId, String webuserId) throws DataBaseException, RemoteException;

    /**
     * Gets the users per category id.
     *
     * @param categoryId
     *            the category id
     *
     * @return the users per category id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<WebUser> getUsersPerCategoryId(String categoryId) throws DataBaseException, RemoteException;

    /**
     * Incoming user del.
     *
     * @param user
     *            the user
     * @param id
     *            the id
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void incomingUserDel(String user, String id) throws DataBaseException, RemoteException;

    /**
     * Incoming user add.
     *
     * @param user
     *            the user
     * @param id
     *            the id
     * @param password
     *            the password
     * @param email
     *            the email
     * @param iso
     *            the iso
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void incomingUserAdd(String user, String id, String password, String email, String iso)
            throws DataBaseException, RemoteException;

    /**
     * Incoming user add2.
     *
     * @param user
     *            the user
     * @param id
     *            the id
     * @param email
     *            the email
     * @param iso
     *            the iso
     *
     * @return the string
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    String incomingUserAdd2(String user, String id, String email, String iso) throws DataBaseException, RemoteException;

    /**
     * Incoming user list.
     *
     * @param user
     *            the user
     * @param destination
     *            the destination
     *
     * @return the collection
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<IncomingUser> incomingUserList(String user, String destination)
            throws DataBaseException, RemoteException;

    /**
     * Incoming association add.
     *
     * @param user
     *            the user
     * @param id
     *            the id
     * @param destination
     *            the destination
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void incomingAssociationAdd(String user, String id, String destination) throws DataBaseException, RemoteException;

    /**
     * Incoming category add.
     *
     * @param user
     *            the user
     * @param id
     *            the id
     * @param categories
     *            the categories
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void incomingCategoryAdd(String user, String id, List<String> categories) throws DataBaseException, RemoteException;

    /**
     * Incoming association del.
     *
     * @param user
     *            the user
     * @param id
     *            the id
     * @param destination
     *            the destination
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void incomingAssociationDel(String user, String id, String destination) throws DataBaseException, RemoteException;

    /**
     * Incoming association list.
     *
     * @param user
     *            the user
     * @param id
     *            the id
     *
     * @return the string[]
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    String[] incomingAssociationList(String user, String id) throws DataBaseException, RemoteException;

    /**
     * Destination list.
     *
     * @param user
     *            the id
     * @param id
     *            the id
     * @param iso
     *            the iso
     * @param type
     *            the type
     *
     * @return the collection
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Destination> destinationList(String user, String id, String iso, Integer type)
            throws DataBaseException, RemoteException;

    /**
     * Get the backup for the selected destination(s).
     *
     * @param user
     *            the id
     * @param id
     *            the id
     * @param iso
     *            the iso
     * @param type
     *            the type
     * @param name
     *            the name
     *
     * @return the collection
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    DestinationBackup getDestinationBackup(String user, String id, String iso, Integer type, String name)
            throws DataBaseException, RemoteException;

    /**
     * Import the provided backup.
     *
     * @param user
     *            the user
     * @param backup
     *            the backup
     * @param copySharedHost
     *            copy the shared host
     *
     * @return number of destinations created
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
     */
    int putDestinationBackup(String user, DestinationBackup backup, boolean copySharedHost)
            throws DataBaseException, MasterException, RemoteException;

    /**
     * Get the DestinationOption list.
     *
     * @return the destination option list
     *
     * @throws RemoteException
     *             the remote exception
     */
    List<TypeEntry> getDestinationOptionList() throws RemoteException;

    /**
     * Destination country list.
     *
     * @param user
     *            the user
     *
     * @return the collection
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    Collection<Country> destinationCountryList(String user) throws DataBaseException, RemoteException;

    /**
     * Datafile put.
     *
     * @param user
     *            the user
     * @param remoteHost
     *            the remote host
     * @param destination
     *            the destination
     * @param metadata
     *            the metadata
     * @param source
     *            the source
     * @param uniqueName
     *            the unique name
     * @param target
     *            the target
     * @param priority
     *            the priority
     * @param lifeTime
     *            the life time
     * @param at
     *            the at
     * @param standby
     *            the standby
     * @param force
     *            the force
     *
     * @return the long
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    long datafilePut(String user, String remoteHost, String destination, String metadata, String source,
            String uniqueName, String target, Integer priority, String lifeTime, String at, Boolean standby,
            Boolean force) throws DataBaseException, RemoteException;

    /**
     * Datafile size.
     *
     * @param user
     *            the user
     * @param dataFileId
     *            the dataFile id
     *
     * @return the long
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    long datafileSize(String user, Long dataFileId) throws DataBaseException, RemoteException;

    /**
     * Datafile del.
     *
     * @param user
     *            the user
     * @param dataFileId
     *            the dataFile id
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    void datafileDel(String user, Long dataFileId) throws MasterException, DataBaseException, RemoteException;
}
