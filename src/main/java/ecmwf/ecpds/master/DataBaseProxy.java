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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isEmpty;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import ecmwf.ecpds.master.transfer.DestinationComparator;
import ecmwf.ecpds.master.transfer.DestinationOption.TypeEntry;

/**
 * The Class DataBaseProxy.
 */
final class DataBaseProxy implements DataBaseInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DataBaseProxy.class);

    /** The Constant DATA_TRANSFER_CACHE. */
    private static final DataTransferCache DATA_TRANSFER_CACHE = new DataTransferCache();

    /** The data base interface. */
    private final DataBaseInterface dataBaseInterface;

    /**
     * Instantiates a new data base proxy.
     *
     * @param dataBaseInterface
     *            the data base interface
     */
    protected DataBaseProxy(final DataBaseInterface dataBaseInterface) {
        this.dataBaseInterface = dataBaseInterface;
    }

    /**
     * {@inheritDoc}
     *
     * Insert.
     */
    @Override
    public void insert(final ECpdsSession session, final DataBaseObject object, final boolean createPk)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || object == null) {
            throw new DataBaseException("Invalid parameter(s) for insert");
        }
        final var monitor = new MonitorCall("insert(" + session.getWebUser().getName() + ","
                + object.getClass().getSimpleName() + "," + createPk + ")");
        dataBaseInterface.insert(session, object, createPk);
        MasterManager.insertInCache(object);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Update.
     */
    @Override
    public void update(final ECpdsSession session, final DataBaseObject object)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || object == null) {
            throw new DataBaseException("Invalid parameter(s) for update");
        }
        final var monitor = new MonitorCall(
                "update(" + session.getWebUser().getName() + "," + object.getClass().getSimpleName() + ")");
        dataBaseInterface.update(session, object);
        MasterManager.updateCache(object);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the.
     */
    @Override
    public void remove(final ECpdsSession session, final DataBaseObject object)
            throws MasterException, DataBaseException, RemoteException {
        if (session == null || object == null) {
            throw new DataBaseException("Invalid parameter(s) for remove");
        }
        final var monitor = new MonitorCall(
                "remove(" + session.getWebUser().getName() + "," + object.getClass().getSimpleName() + ")");
        dataBaseInterface.remove(session, object);
        MasterManager.removeFromCache(object);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the initial data transfer events.
     */
    @Override
    public void getInitialDataTransferEvents(final String target) throws RemoteException {
        final var monitor = new MonitorCall("getInitialDataTransferEvents()");
        dataBaseInterface.getInitialDataTransferEvents(target);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the initial change host events.
     */
    @Override
    public void getInitialChangeHostEvents(final String target) throws RemoteException {
        final var monitor = new MonitorCall("getInitialChangeHostEvents()");
        dataBaseInterface.getInitialChangeHostEvents(target);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the statistics.
     */
    @Override
    public Statistics[] getStatistics(final Date fromDate, final Date toDate, final String groupName,
            final String status, final String type) throws DataBaseException, RemoteException {
        if (fromDate == null || toDate == null || isEmpty(groupName) || isEmpty(status) || isEmpty(type)) {
            throw new DataBaseException("Invalid parameter(s) for getStatistics");
        }
        final var monitor = new MonitorCall(
                "getStatistics(" + fromDate + "," + toDate + "," + groupName + "," + status + "," + type + ")");
        return monitor.done(dataBaseInterface.getStatistics(fromDate, toDate, groupName, status, type));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the rates.
     */
    @Override
    public Rates[] getRates(final Date fromDate, final Date toDate, final String caller, final String sourceHost)
            throws DataBaseException, RemoteException {
        if (fromDate == null || toDate == null) {
            throw new DataBaseException("Invalid parameter(s) for getRates");
        }
        final var monitor = new MonitorCall(
                "getRates(" + fromDate + "," + toDate + "," + caller + "," + sourceHost + ")");
        return monitor.done(dataBaseInterface.getRates(fromDate, toDate, caller, sourceHost));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the rates per transfer server.
     */
    @Override
    public Rates[] getRatesPerTransferServer(final Date fromDate, final Date toDate, final String caller,
            final String sourceHost) throws DataBaseException, RemoteException {
        if (fromDate == null || toDate == null) {
            throw new DataBaseException("Invalid parameter(s) for getRatesPerTransferServer");
        }
        final var monitor = new MonitorCall(
                "getRatesPerTransferServer(" + fromDate + "," + toDate + "," + caller + "," + sourceHost + ")");
        return monitor.done(dataBaseInterface.getRatesPerTransferServer(fromDate, toDate, caller, sourceHost));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the rates per file system.
     */
    @Override
    public Rates[] getRatesPerFileSystem(final Date fromDate, final Date toDate, final String transferServerName,
            final String caller, final String sourceHost) throws DataBaseException, RemoteException {
        if (fromDate == null || toDate == null) {
            throw new DataBaseException("Invalid parameter(s) for getRatesPerFileSystem");
        }
        final var monitor = new MonitorCall("getRatesPerFileSystem(" + fromDate + "," + toDate + ","
                + transferServerName + "," + caller + "," + sourceHost + ")");
        return monitor.done(
                dataBaseInterface.getRatesPerFileSystem(fromDate, toDate, transferServerName, caller, sourceHost));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the initial product status events.
     */
    @Override
    public void getInitialProductStatusEvents(final String target) throws RemoteException {
        final var monitor = new MonitorCall("getInitialProductStatusEvents()");
        dataBaseInterface.getInitialProductStatusEvents(target);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer servers.
     */
    @Override
    public TransferServer[] getTransferServers(final String groupName) throws DataBaseException, RemoteException {
        if (isEmpty(groupName)) {
            throw new DataBaseException("Invalid parameter(s) for getTransferServers");
        }
        final var monitor = new MonitorCall("getTransferServers(" + groupName + ")");
        return monitor.done(dataBaseInterface.getTransferServers(groupName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination ecuser.
     */
    @Override
    public ECUser[] getDestinationEcuser(final String destinationName) throws DataBaseException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new DataBaseException("Invalid parameter(s) for getDestinationEcuser");
        }
        final var monitor = new MonitorCall("getDestinationEcuser(" + destinationName + ")");
        return monitor.done(dataBaseInterface.getDestinationEcuser(destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination incoming policies.
     */
    @Override
    public IncomingPolicy[] getDestinationIncomingPolicies(final String destinationName)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new DataBaseException("Invalid parameter(s) for getDestinationIncomingPolicies");
        }
        final var monitor = new MonitorCall("getDestinationIncomingPolicies(" + destinationName + ")");
        return monitor.done(dataBaseInterface.getDestinationIncomingPolicies(destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming users for incoming policy.
     */
    @Override
    public IncomingUser[] getIncomingUsersForIncomingPolicy(final String policyId)
            throws DataBaseException, RemoteException {
        if (isEmpty(policyId)) {
            throw new DataBaseException("Invalid parameter(s) for getIncomingUsersForIncomingPolicy");
        }
        final var monitor = new MonitorCall("getIncomingUsersForIncomingPolicy(" + policyId + ")");
        return monitor.done(dataBaseInterface.getIncomingUsersForIncomingPolicy(policyId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming policies for incoming user.
     */
    @Override
    public IncomingPolicy[] getIncomingPoliciesForIncomingUser(final String userId)
            throws DataBaseException, RemoteException {
        if (isEmpty(userId)) {
            throw new DataBaseException("Invalid parameter(s) for getIncomingPoliciesForIncomingUser");
        }
        final var monitor = new MonitorCall("getIncomingPoliciesForIncomingUser(" + userId + ")");
        return monitor.done(dataBaseInterface.getIncomingPoliciesForIncomingUser(userId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the operations for incoming user.
     */
    @Override
    public Operation[] getOperationsForIncomingUser(final String userId) throws DataBaseException, RemoteException {
        if (isEmpty(userId)) {
            throw new DataBaseException("Invalid parameter(s) for getOperationsForIncomingUser");
        }
        final var monitor = new MonitorCall("getOperationsForIncomingUser(" + userId + ")");
        return monitor.done(dataBaseInterface.getOperationsForIncomingUser(userId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations for incoming user.
     */
    @Override
    public Destination[] getDestinationsForIncomingUser(final String userId) throws DataBaseException, RemoteException {
        if (isEmpty(userId)) {
            throw new DataBaseException("Invalid parameter(s) for getDestinationsForIncomingUser");
        }
        final var monitor = new MonitorCall("getDestinationsForIncomingUser(" + userId + ")");
        return monitor.done(dataBaseInterface.getDestinationsForIncomingUser(userId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations for incoming policy.
     */
    @Override
    public Destination[] getDestinationsForIncomingPolicy(final String policyId)
            throws DataBaseException, RemoteException {
        if (isEmpty(policyId)) {
            throw new DataBaseException("Invalid parameter(s) for getDestinationsForIncomingPolicy");
        }
        final var monitor = new MonitorCall("getDestinationsForIncomingPolicy(" + policyId + ")");
        return monitor.done(dataBaseInterface.getDestinationsForIncomingPolicy(policyId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfer count not done by product and time on date.
     */
    @Override
    public int getDataTransferCountNotDoneByProductAndTimeOnDate(final String destination, final String product,
            final String time, final Date from, final Date to) throws DataBaseException, RemoteException {
        if (isEmpty(destination) || isEmpty(product) || isEmpty(time) || from == null || to == null) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransferCountNotDoneByProductAndTimeOnDate");
        }
        final var monitor = new MonitorCall("getDataTransferCountNotDoneByProductAndTimeOnDate(" + destination + ","
                + product + "," + time + "," + from + "," + to + ")");
        return monitor.done(dataBaseInterface.getDataTransferCountNotDoneByProductAndTimeOnDate(destination, product,
                time, from, to));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination aliases.
     */
    @Override
    public Destination[] getDestinationAliases(final String name, final String mode)
            throws DataBaseException, RemoteException {
        if (isEmpty(name) || isEmpty(mode)) {
            throw new DataBaseException("Invalid parameter(s) for getDestinationAliases");
        }
        final var monitor = new MonitorCall("getDestinationAliases(" + name + "," + mode + ")");
        return monitor.done(dataBaseInterface.getDestinationAliases(name, mode));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the aliases.
     */
    @Override
    public Alias[] getAliases(final String name, final String mode) throws DataBaseException, RemoteException {
        if (isEmpty(name) || isEmpty(mode)) {
            throw new DataBaseException("Invalid parameter(s) for getAliases");
        }
        final var monitor = new MonitorCall("getAliases(" + name + "," + mode + ")");
        return monitor.done(dataBaseInterface.getAliases(name, mode));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the e cuser events.
     */
    @Override
    public Collection<Event> getECuserEvents(final String userName, final Date onIsoDate, final String search,
            final DataBaseCursor cursor) throws DataBaseException, RemoteException {
        if (onIsoDate == null) {
            throw new DataBaseException("Invalid parameter(s) for getECuserEvents");
        }
        final var monitor = new MonitorCall("getECuserEvents(" + userName + "," + onIsoDate + ")");
        return monitor.done(dataBaseInterface.getECuserEvents(userName, onIsoDate, search, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming history.
     */
    @Override
    public Collection<IncomingHistory> getIncomingHistory(final String userName, final Date onIsoDate,
            final String search, final DataBaseCursor cursor) throws DataBaseException, RemoteException {
        if (onIsoDate == null) {
            throw new DataBaseException("Invalid parameter(s) for getIncomingHistory");
        }
        final var monitor = new MonitorCall("getIncomingHistory(" + userName + "," + onIsoDate + ")");
        return monitor.done(dataBaseInterface.getIncomingHistory(userName, onIsoDate, search, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by host name.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByHostName(final String name, final Date from, final Date to)
            throws DataBaseException, RemoteException {
        if (isEmpty(name) || from == null || to == null) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfersByHostName");
        }
        final var monitor = new MonitorCall("getDataTransfersByHostName(" + name + "," + from + "," + to + ")");
        return monitor.done(dataBaseInterface.getDataTransfersByHostName(name, from, to));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by transfer server name.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByTransferServerName(final String name, final Date from,
            final Date to) throws DataBaseException, RemoteException {
        if (isEmpty(name) || from == null || to == null) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfersByTransferServerName");
        }
        final var monitor = new MonitorCall(
                "getDataTransfersByTransferServerName(" + name + "," + from + "," + to + ")");
        return monitor.done(dataBaseInterface.getDataTransfersByTransferServerName(name, from, to));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by status code and date.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByStatusCodeAndDate(final String status, final Date from,
            final Date to, final String search, final String type, final DataBaseCursor cursor)
            throws DataBaseException, RemoteException {
        if (isEmpty(status) || from == null || to == null) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfersByStatusCodeAndDate");
        }
        final var monitor = new MonitorCall(
                "getDataTransfersByStatusCodeAndDate(" + status + "," + from + "," + to + "," + type + ")");
        return monitor
                .done(dataBaseInterface.getDataTransfersByStatusCodeAndDate(status, from, to, search, type, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by data file id.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByDataFileId(final long dataFileId, final boolean includeDeleted)
            throws DataBaseException, RemoteException {
        if (dataFileId < 0) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfersByDataFileId");
        }
        final var monitor = new MonitorCall("getDataTransfersByDataFileId(" + dataFileId + "," + includeDeleted + ")");
        return monitor.done(dataBaseInterface.getDataTransfersByDataFileId(dataFileId, includeDeleted));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations by country ISO.
     */
    @Override
    public Destination[] getDestinationsByCountryISO(final String isoCode) throws RemoteException {
        final var monitor = new MonitorCall("getDestinationsByCountryISO(" + isoCode + ")");
        if (isEmpty(isoCode)) {
            return monitor.done(new Destination[0]);
        }
        try {
            final List<Destination> list = new ArrayList<>();
            for (final DestinationCache cache : MasterManager.getDestinationCaches()) {
                final var destination = cache.getDestination();
                if (isoCode.equals(destination.getCountryIso())) {
                    list.add(destination);
                }
            }
            if (!list.isEmpty()) {
                return monitor.done(DestinationComparator.getDestinationArray(list));
            }
        } catch (final Exception e) {
            _log.debug("getDestinationsByCountryISO", e);
        }
        return monitor.done(dataBaseInterface.getDestinationsByCountryISO(isoCode));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations by user.
     */
    @Override
    public Destination[] getDestinationsByUser(final String uid, final String search, final String fromToAliases,
            final boolean asc, final String status, final String type, final String filter) throws IOException {
        if (isEmpty(uid)) {
            return new Destination[0];
        }
        final var monitor = new MonitorCall("getDestinationsByUser(" + uid + "," + search + "," + asc + "," + status
                + "," + type + "," + filter + ")");
        return monitor
                .done(dataBaseInterface.getDestinationsByUser(uid, search, fromToAliases, asc, status, type, filter));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations by host name.
     */
    @Override
    public Destination[] getDestinationsByHostName(final String hostName) throws DataBaseException, RemoteException {
        if (isEmpty(hostName)) {
            throw new DataBaseException("Invalid parameter(s) for getDestinationsByHostName");
        }
        final var monitor = new MonitorCall("getDestinationsByHostName(" + hostName + ")");
        try {
            final List<Destination> list = new ArrayList<>();
            for (final DestinationCache cache : MasterManager.getDestinationCaches()) {
                if (cache.getAssociations().containsKey(hostName)) {
                    list.add(cache.getDestination());
                }
            }
            if (!list.isEmpty()) {
                return monitor.done(DestinationComparator.getDestinationArray(list));
            }
        } catch (final Exception e) {
            _log.debug("getDestinationsByHostName", e);
        }
        return monitor.done(dataBaseInterface.getDestinationsByHostName(hostName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history by data transfer id.
     */
    @Override
    public TransferHistory[] getTransferHistoryByDataTransferId(final long dataTransferId)
            throws DataBaseException, RemoteException {
        if (dataTransferId < 0) {
            throw new DataBaseException("Invalid parameter(s) for getTransferHistoryByDataTransferId");
        }
        final var monitor = new MonitorCall("getTransferHistoryByDataTransferId(" + dataTransferId + ")");
        return monitor.done(dataBaseInterface.getTransferHistoryByDataTransferId(dataTransferId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history by data transfer id.
     */
    @Override
    public TransferHistory[] getTransferHistoryByDataTransferId(final long dataTransferId,
            final boolean afterScheduleTime, final DataBaseCursor cursor) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferHistoryByDataTransferId(" + dataTransferId + ")");
        return monitor
                .done(dataBaseInterface.getTransferHistoryByDataTransferId(dataTransferId, afterScheduleTime, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the categories per user id.
     */
    @Override
    public Collection<Category> getCategoriesPerUserId(final String userId) throws DataBaseException, RemoteException {
        if (isEmpty(userId)) {
            throw new DataBaseException("Invalid parameter(s) for getCategoriesPerUserId");
        }
        final var monitor = new MonitorCall("getCategoriesPerUserId(" + userId + ")");
        return monitor.done(dataBaseInterface.getCategoriesPerUserId(userId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the urls per category id.
     */
    @Override
    public Collection<Url> getUrlsPerCategoryId(final String id) throws DataBaseException, RemoteException {
        if (isEmpty(id)) {
            throw new DataBaseException("Invalid parameter(s) for getUrlsPerCategoryId");
        }
        final var monitor = new MonitorCall("getUrlsPerCategoryId(" + id + ")");
        try {
            final List<Url> results = new ArrayList<>();
            final var categoryId = Long.parseLong(id);
            for (final CatUrl catUrl : MasterManager.getCatUrls()) {
                if (catUrl.getCategoryId() == categoryId) {
                    results.add(catUrl.getUrl());
                }
            }
            if (!results.isEmpty()) {
                return monitor.done(results);
            }
        } catch (final Exception e) {
            _log.debug("getUrlsPerCategoryId", e);
        }
        return monitor.done(dataBaseInterface.getUrlsPerCategoryId(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the categories per resource id.
     */
    @Override
    public Collection<Category> getCategoriesPerResourceId(final String id) throws DataBaseException, RemoteException {
        if (isEmpty(id)) {
            throw new DataBaseException("Invalid parameter(s) for getCategoriesPerResourceId");
        }
        final var monitor = new MonitorCall("getCategoriesPerResourceId(" + id + ")");
        try {
            final List<Category> results = new ArrayList<>();
            for (final CatUrl catUrl : MasterManager.getCatUrls()) {
                if (catUrl.getUrlName().equals(id)) {
                    results.add(catUrl.getCategory());
                }
            }
            if (!results.isEmpty()) {
                return monitor.done(results);
            }
        } catch (final Exception e) {
            _log.debug("getCategoriesPerResourceId", e);
        }
        return monitor.done(dataBaseInterface.getCategoriesPerResourceId(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data files by meta data.
     */
    @Override
    public Collection<DataFile> getDataFilesByMetaData(final String name, final String value, final Date from,
            final Date to, final DataBaseCursor cursor) throws DataBaseException, RemoteException {
        if (name == null || value == null || from == null || to == null) {
            throw new DataBaseException("Invalid parameter(s) for getDataFilesByMetaData");
        }
        final var monitor = new MonitorCall(
                "getDataFilesByMetaData(" + name + "," + value + "," + from + "," + to + ")");
        return monitor.done(dataBaseInterface.getDataFilesByMetaData(name, value, from, to, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer count and meta data by filter.
     */
    @Override
    public Collection<List<String>> getTransferCountAndMetaDataByFilter(final String countBy, final String destination,
            final String target, final String stream, final String time, final String status, final String fileName,
            final Date from, final Date to, final String privilegedUser, final Date scheduledBefore)
            throws DataBaseException, RemoteException {
        if (!("status".equals(countBy) || "status2".equals(countBy))
                && !("target".equals(countBy) || "target2".equals(countBy))
                && !("stream".equals(countBy) || "stream2".equals(countBy)) || isEmpty(destination) || isEmpty(target)
                || isEmpty(status) || isEmpty(stream) || isEmpty(time) || from == null || to == null
                || !"false".equals(privilegedUser) && !"true".equals(privilegedUser) || scheduledBefore == null) {
            throw new DataBaseException("Invalid parameter(s) for getTransferCountAndMetaDataByFilter");
        }
        final var monitor = new MonitorCall("getTransferCountAndMetaDataByFilter(" + countBy + "," + destination + ","
                + target + "," + stream + "," + time + "," + status + "," + fileName + "," + from + "," + to + ","
                + privilegedUser + "," + scheduledBefore + ")");
        return monitor.done(dataBaseInterface.getTransferCountAndMetaDataByFilter(countBy, destination, target, stream,
                time, status, fileName, from, to, privilegedUser, scheduledBefore));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by filter.
     */
    @Override
    public Collection<DataTransferWithPermissions> getDataTransfersByFilter(final String destination,
            final String target, final String stream, final String time, final String status,
            final String privilegedUser, final Date scheduledBefore, final String fileName, final Date from,
            final Date to, final DataBaseCursor cursor) throws MasterException, DataBaseException, RemoteException {
        if (isEmpty(destination) || isEmpty(target) || isEmpty(stream) || isEmpty(time) || isEmpty(status)
                || fileName == null || from == null || to == null) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfersByFilter");
        }
        final var monitor = new MonitorCall("getDataTransfersByFilter(" + destination + "," + target + "," + stream
                + "," + time + "," + status + "," + fileName + "," + from + "," + to + ")");
        return monitor.done(dataBaseInterface.getDataTransfersByFilter(destination, target, stream, time, status,
                privilegedUser, scheduledBefore, fileName, from, to, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by filter.
     */
    @Override
    public Collection<DataTransferWithPermissions> getDataTransfersByFilter(final String destination,
            final String target, final String stream, final String time, final String status,
            final String privilegedUser, final Date scheduledBefore, final String fileName, final Date from,
            final Date to) throws MasterException, DataBaseException, RemoteException {
        if (isEmpty(destination) || isEmpty(target) || isEmpty(stream) || isEmpty(status) || fileName == null
                || from == null || to == null) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfersByFilter");
        }
        final var monitor = new MonitorCall("getDataTransfersByFilter(" + destination + "," + target + "," + stream
                + "," + time + "," + status + "," + fileName + "," + from + "," + to + ")");
        return monitor.done(dataBaseInterface.getDataTransfersByFilter(destination, target, stream, time, status,
                privilegedUser, scheduledBefore, fileName, from, to));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the filtered hosts.
     */
    @Override
    public Collection<Host> getFilteredHosts(final String label, final String filter, final String network,
            final String type, final String search, final DataBaseCursor cursor)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getHosts(" + label + "," + filter + "," + network + "," + type + "," + search + ")");
        return monitor.done(dataBaseInterface.getFilteredHosts(label, filter, network, type, search, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the hosts by destination id.
     */
    @Override
    public Collection<Host> getHostsByDestinationId(final String destId) throws DataBaseException, RemoteException {
        if (isEmpty(destId)) {
            throw new DataBaseException("Invalid parameter(s) for getHostsByDestinationId");
        }
        final var monitor = new MonitorCall("getHostsByDestinationId(" + destId + ")");
        return monitor.done(dataBaseInterface.getHostsByDestinationId(destId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the hosts by transfer method id.
     */
    @Override
    public Collection<Host> getHostsByTransferMethodId(final String transferMethodId)
            throws DataBaseException, RemoteException {
        if (isEmpty(transferMethodId)) {
            throw new DataBaseException("Invalid parameter(s) for getHostsByTransferMethodId");
        }
        final var monitor = new MonitorCall("getHostsByTransferMethodId(" + transferMethodId + ")");
        return monitor.done(dataBaseInterface.getHostsByTransferMethodId(transferMethodId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer methods by ec trans module name.
     */
    @Override
    public Collection<TransferMethod> getTransferMethodsByEcTransModuleName(final String ecTransModuleName)
            throws DataBaseException, RemoteException {
        if (isEmpty(ecTransModuleName)) {
            throw new DataBaseException("Invalid parameter(s) for getTransferMethodsByEcTransModuleName");
        }
        final var monitor = new MonitorCall("getTransferMethodsByEcTransModuleName(" + ecTransModuleName + ")");
        return monitor.done(dataBaseInterface.getTransferMethodsByEcTransModuleName(ecTransModuleName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta data by data file id.
     */
    @Override
    public Collection<MetadataValue> getMetaDataByDataFileId(final long dataFileId)
            throws DataBaseException, RemoteException {
        if (dataFileId < 0) {
            throw new DataBaseException("Invalid parameter(s) for getMetaDataByDataFileId");
        }
        final var monitor = new MonitorCall("getMetaDataByDataFileId(" + dataFileId + ")");
        return monitor.done(dataBaseInterface.getMetaDataByDataFileId(dataFileId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta data by attribute name.
     */
    @Override
    public Collection<MetadataValue> getMetaDataByAttributeName(final String id)
            throws DataBaseException, RemoteException {
        if (isEmpty(id)) {
            throw new DataBaseException("Invalid parameter(s) for getMetaDataByAttributeName");
        }
        final var monitor = new MonitorCall("getMetaDataByAttributeName(" + id + ")");
        return monitor.done(dataBaseInterface.getMetaDataByAttributeName(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by destination and identity.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByDestinationAndIdentity(final String destination,
            final String identity) throws DataBaseException, RemoteException {
        if (isEmpty(destination) || isEmpty(identity)) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfersByDestinationAndIdentity");
        }
        final var monitor = new MonitorCall(
                "getDataTransfersByDestinationAndIdentity(" + destination + "," + identity + ")");
        return monitor.done(dataBaseInterface.getDataTransfersByDestinationAndIdentity(destination, identity));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer count with destination and metadata value by metadata name.
     */
    @Override
    public Collection<List<String>> getTransferCountWithDestinationAndMetadataValueByMetadataName(
            final String metadataName) throws DataBaseException, RemoteException {
        if (isEmpty(metadataName)) {
            throw new DataBaseException(
                    "Invalid parameter(s) for getTransferCountWithDestinationAndMetadataValueByMetadataName");
        }
        final var monitor = new MonitorCall(
                "getTransferCountWithDestinationAndMetadataValueByMetadataName(" + metadataName + ")");
        return monitor
                .done(dataBaseInterface.getTransferCountWithDestinationAndMetadataValueByMetadataName(metadataName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by destination product and time on date.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByDestinationProductAndTimeOnDate(final String destinationName,
            final String product, final String time, final Date fromIsoDate, final Date toIsoDate)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName) || isEmpty(product) || isEmpty(time) || fromIsoDate == null || toIsoDate == null) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfersByDestinationProductAndTimeOnDate");
        }
        final var monitor = new MonitorCall("getDataTransfersByDestinationProductAndTimeOnDate(" + destinationName + ","
                + product + "," + time + "," + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(dataBaseInterface.getDataTransfersByDestinationProductAndTimeOnDate(destinationName,
                product, time, fromIsoDate, toIsoDate));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by destination on date.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByDestinationOnDate(final String destinationName,
            final Date fromIsoDate, final Date toIsoDate) throws DataBaseException, RemoteException {
        if (isEmpty(destinationName) || fromIsoDate == null || toIsoDate == null) {
            throw new DataBaseException("Invalid parameter(s) specified for getDataTransfersByDestinationOnDate");
        }
        final var monitor = new MonitorCall(
                "getDataTransfersByDestinationOnDate(" + destinationName + "," + fromIsoDate + "," + toIsoDate + ")");
        return monitor
                .done(dataBaseInterface.getDataTransfersByDestinationOnDate(destinationName, fromIsoDate, toIsoDate));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by destination on transmission date.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByDestinationOnTransmissionDate(final String destinationName,
            final Date fromIsoDate, final Date toIsoDate) throws DataBaseException, RemoteException {
        if (isEmpty(destinationName) || fromIsoDate == null || toIsoDate == null) {
            throw new DataBaseException(
                    "Invalid parameter(s) specified for getDataTransfersByDestinationOnTransmissionDate");
        }
        final var monitor = new MonitorCall("getDataTransfersByDestinationOnTransmissionDate(" + destinationName + ","
                + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(dataBaseInterface.getDataTransfersByDestinationOnTransmissionDate(destinationName,
                fromIsoDate, toIsoDate));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bad data transfers by destination.
     */
    @Override
    public Collection<DataTransfer> getBadDataTransfersByDestination(final String destinationName)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new DataBaseException("Invalid parameter(s) for getBadDataTransfersByDestination");
        }
        final var monitor = new MonitorCall("getBadDataTransfersByDestination(" + destinationName + ")");
        return monitor.done(dataBaseInterface.getBadDataTransfersByDestination(destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bad data transfers by destination count.
     */
    @Override
    public int getBadDataTransfersByDestinationCount(final String destinationName)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new DataBaseException("Invalid parameter(s) for getBadDataTransfersByDestinationCount");
        }
        final var monitor = new MonitorCall("getBadDataTransfersByDestinationCount(" + destinationName + ")");
        try {
            return monitor.done(MasterManager.getMonitoringCache(destinationName).getBadDataTransfersCount());
        } catch (final Exception e) {
            return monitor.done(dataBaseInterface.getBadDataTransfersByDestinationCount(destinationName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history by destination on product date.
     */
    @Override
    public Collection<TransferHistory> getTransferHistoryByDestinationOnProductDate(final String destinationName,
            final Date fromIsoDate, final Date toIsoDate, final DataBaseCursor cursor)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName) || fromIsoDate == null || toIsoDate == null) {
            throw new DataBaseException("Invalid parameter(s) for getTransferHistoryByDestinationOnProductDate");
        }
        final var monitor = new MonitorCall("getTransferHistoryByDestinationOnProductDate(" + destinationName + ","
                + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(dataBaseInterface.getTransferHistoryByDestinationOnProductDate(destinationName, fromIsoDate,
                toIsoDate, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history by destination on history date.
     */
    @Override
    public Collection<TransferHistory> getTransferHistoryByDestinationOnHistoryDate(final String destinationName,
            final Date fromIsoDate, final Date toIsoDate, final DataBaseCursor cursor)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName) || fromIsoDate == null || toIsoDate == null) {
            throw new DataBaseException("Invalid parameter(s) for getTransferHistoryByDestinationOnHistoryDate");
        }
        final var monitor = new MonitorCall("getTransferHistoryByDestinationOnHistoryDate(" + destinationName + ","
                + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(dataBaseInterface.getTransferHistoryByDestinationOnHistoryDate(destinationName, fromIsoDate,
                toIsoDate, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the allowed ec users by host name.
     */
    @Override
    public Collection<ECUser> getAllowedEcUsersByHostName(final String hostName)
            throws DataBaseException, RemoteException {
        if (isEmpty(hostName)) {
            throw new DataBaseException("Invalid parameter(s) for getAllowedEcUsersByHostName");
        }
        final var monitor = new MonitorCall("getAllowedEcUsersByHostName(" + hostName + ")");
        return monitor.done(dataBaseInterface.getAllowedEcUsersByHostName(hostName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the traffic by destination name.
     */
    @Override
    public Collection<Traffic> getTrafficByDestinationName(final String destinationName)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName)) {
            throw new DataBaseException("Invalid parameter(s) for getTrafficByDestinationName");
        }
        final var monitor = new MonitorCall("getTrafficByDestinationName(" + destinationName + ")");
        return monitor.done(dataBaseInterface.getTrafficByDestinationName(destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the change log by key.
     */
    @Override
    public Collection<ChangeLog> getChangeLogByKey(final String keyName, final String keyValue)
            throws DataBaseException, RemoteException {
        if (isEmpty(keyName) || isEmpty(keyValue)) {
            throw new DataBaseException("Invalid parameter(s) for getChangeLogByKey");
        }
        final var monitor = new MonitorCall("getChangeLogByKey(" + keyName + "," + keyValue + ")");
        return monitor.done(dataBaseInterface.getChangeLogByKey(keyName, keyValue));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data file.
     */
    @Override
    public DataFile getDataFile(final long dataFileId) throws DataBaseException, RemoteException {
        if (dataFileId < 0) {
            throw new DataBaseException("Invalid parameter(s) for getDataFile");
        }
        final var monitor = new MonitorCall("getDataFile(" + dataFileId + ")");
        return monitor.done(dataBaseInterface.getDataFile(dataFileId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer group.
     */
    @Override
    public TransferGroup getTransferGroup(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getTransferGroup");
        }
        final var monitor = new MonitorCall("getTransferGroup(" + name + ")");
        return monitor.done(dataBaseInterface.getTransferGroup(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the metadata attribute.
     */
    @Override
    public MetadataAttribute getMetadataAttribute(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getMetadataAttribute");
        }
        final var monitor = new MonitorCall("getMetadataAttribute(" + name + ")");
        return monitor.done(dataBaseInterface.getMetadataAttribute(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the metadata attribute array.
     */
    @Override
    public MetadataAttribute[] getMetadataAttributeArray() throws RemoteException {
        final var monitor = new MonitorCall("getMetadataAttributeArray()");
        return monitor.done(dataBaseInterface.getMetadataAttributeArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the cat url array.
     */
    @Override
    public CatUrl[] getCatUrlArray() throws RemoteException {
        final var monitor = new MonitorCall("getCatUrlArray()");
        final var vector = MasterManager.getCatUrls();
        final var size = vector.size();
        if (size > 0) {
            return monitor.done(vector.toArray(new CatUrl[vector.size()]));
        }
        return monitor.done(dataBaseInterface.getCatUrlArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer group array.
     */
    @Override
    public TransferGroup[] getTransferGroupArray() throws RemoteException {
        final var monitor = new MonitorCall("getTransferGroupArray()");
        return monitor.done(dataBaseInterface.getTransferGroupArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the product status.
     */
    @Override
    public ProductStatus[] getProductStatus(final String stream, final String time, final String type, final long step,
            final int limit) throws RemoteException, DataBaseException {
        if (isEmpty(stream) || isEmpty(time)) {
            throw new DataBaseException("Invalid parameter(s) for getProductStatus");
        }
        final var monitor = new MonitorCall(
                "getProductStatus(" + stream + "," + time + "," + type + "," + step + "," + limit + ")");
        return monitor.done(dataBaseInterface.getProductStatus(stream, time, type, step, limit));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer server.
     */
    @Override
    public TransferServer getTransferServer(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getTransferServer");
        }
        final var monitor = new MonitorCall("getTransferServer(" + name + ")");
        return monitor.done(dataBaseInterface.getTransferServer(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer server array.
     */
    @Override
    public TransferServer[] getTransferServerArray() throws RemoteException {
        final var monitor = new MonitorCall("getTransferServerArray()");
        return monitor.done(dataBaseInterface.getTransferServerArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the EC user.
     */
    @Override
    public ECUser getECUser(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getECUser");
        }
        final var monitor = new MonitorCall("getECUser(" + name + ")");
        var ecuser = MasterManager.getECUser(name);
        if (ecuser == null) {
            ecuser = dataBaseInterface.getECUser(name);
        }
        return monitor.done(ecuser);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming policy.
     */
    @Override
    public IncomingPolicy getIncomingPolicy(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getIncomingPolicy");
        }
        final var monitor = new MonitorCall("getIncomingPolicy(" + name + ")");
        return monitor.done(dataBaseInterface.getIncomingPolicy(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the operation.
     */
    @Override
    public Operation getOperation(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getOperation");
        }
        final var monitor = new MonitorCall("getOperation(" + name + ")");
        return monitor.done(dataBaseInterface.getOperation(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming user.
     */
    @Override
    public IncomingUser getIncomingUser(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getIncomingUser");
        }
        final var monitor = new MonitorCall("getIncomingUser(" + name + ")");
        return monitor.done(dataBaseInterface.getIncomingUser(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming user array.
     */
    @Override
    public IncomingUser[] getIncomingUserArray() throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getIncomingUserArray()");
        return monitor.done(dataBaseInterface.getIncomingUserArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the operation array.
     */
    @Override
    public Operation[] getOperationArray() throws RemoteException {
        final var monitor = new MonitorCall("getOperationArray()");
        return monitor.done(dataBaseInterface.getOperationArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination.
     */
    @Override
    public Destination getDestination(final String name, final boolean useCache)
            throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getDestination");
        }
        final var monitor = new MonitorCall("getDestination(" + name + ")");
        if (useCache) {
            try {
                final var cache = MasterManager.getMonitoringCache(name);
                if (cache != null) {
                    // We found it!
                    if (cache.isDestinationSchedulerCacheExpired()) {
                        // It is expired so let's updated it!
                        cache.setDestinationSchedulerCache(MasterManager.getMI().getDestinationSchedulerCache(name));
                    }
                    return monitor.done(cache.getDestination());
                }
            } catch (final Exception e) {
                // Ignore
            }
        }
        return monitor.done(dataBaseInterface.getDestination(name, useCache));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the EC user array.
     */
    @Override
    public ECUser[] getECUserArray() throws RemoteException {
        final var monitor = new MonitorCall("getECUserArray()");
        return monitor.done(dataBaseInterface.getECUserArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming policy array.
     */
    @Override
    public IncomingPolicy[] getIncomingPolicyArray() throws RemoteException {
        final var monitor = new MonitorCall("getIncomingPolicyArray()");
        return monitor.done(dataBaseInterface.getIncomingPolicyArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the country.
     */
    @Override
    public Country getCountry(final String iso) throws DataBaseException, RemoteException {
        if (isEmpty(iso)) {
            throw new DataBaseException("Invalid parameter(s) for getCountry");
        }
        final var monitor = new MonitorCall("getCountry(" + iso + ")");
        try {
            for (final DestinationCache cache : MasterManager.getDestinationCaches()) {
                final var country = cache.getDestination().getCountry();
                if (iso.equals(country.getIso())) {
                    return monitor.done(country);
                }
            }
        } catch (final Exception e) {
            _log.debug("getCountry", e);
        }
        return monitor.done(dataBaseInterface.getCountry(iso));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the country array.
     */
    @Override
    public Country[] getCountryArray() throws RemoteException {
        final var monitor = new MonitorCall("getCountryArray()");
        return monitor.done(dataBaseInterface.getCountryArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfer.
     */
    @Override
    public DataTransfer getDataTransfer(final long dataTransferId) throws DataBaseException, RemoteException {
        if (dataTransferId < 0) {
            throw new DataBaseException("Invalid parameter(s) for getDataTransfer");
        }
        final var monitor = new MonitorCall("getDataTransfer(" + dataTransferId + ")");
        var result = DATA_TRANSFER_CACHE.getDataTransfer(dataTransferId);
        if (result == null) {
            result = dataBaseInterface.getDataTransfer(dataTransferId);
            DATA_TRANSFER_CACHE.setDataTransfer(result);
        }
        return monitor.done(result);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination array.
     */
    @Override
    public Destination[] getDestinationArray() throws RemoteException {
        final var monitor = new MonitorCall("getDestinationArray()");
        try {
            final List<Destination> list = new ArrayList<>();
            for (final DestinationCache cache : MasterManager.getDestinationCaches()) {
                list.add(cache.getDestination());
            }
            if (!list.isEmpty()) {
                return monitor.done(DestinationComparator.getDestinationArray(list));
            }
        } catch (final Exception e) {
            _log.debug("getDestinationArray", e);
        }
        return monitor.done(dataBaseInterface.getDestinationArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination array.
     */
    @Override
    public Destination[] getDestinationArray(final boolean monitored) throws RemoteException {
        final var monitor = new MonitorCall("getDestinationArray()");
        try {
            final List<Destination> list = new ArrayList<>();
            for (final DestinationCache cache : MasterManager.getDestinationCaches()) {
                final var destination = cache.getDestination();
                if (destination.getMonitor()) {
                    list.add(destination);
                }
            }
            if (!list.isEmpty()) {
                return monitor.done(DestinationComparator.getDestinationArray(list));
            }
        } catch (final Exception e) {
            _log.debug("getDestinationArray", e);
        }
        return monitor.done(dataBaseInterface.getDestinationArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination names and comments.
     */
    @Override
    public Set<Map.Entry<String, String>> getDestinationNamesAndComments() throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationNamesAndComments()");
        try {
            final Set<Map.Entry<String, String>> list = new HashSet<>();
            for (final DestinationCache cache : MasterManager.getDestinationCaches()) {
                final var destination = cache.getDestination();
                list.add(new AbstractMap.SimpleEntry<>(destination.getName(), destination.getComment()));
            }
            if (!list.isEmpty()) {
                return monitor.done(list.stream().sorted(Comparator.comparing(Map.Entry<String, String>::getKey))
                        .collect(Collectors.toSet()));
            }
        } catch (final Exception e) {
            _log.debug("getDestinationNamesAndComments", e);
        }
        return monitor.done(dataBaseInterface.getDestinationNamesAndComments());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination EC user.
     */
    @Override
    public DestinationECUser getDestinationECUser(final String destinationName, final String ecuserName)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName) || isEmpty(ecuserName)) {
            throw new DataBaseException("Invalid parameter(s) for getDestinationECUser");
        }
        final var monitor = new MonitorCall("getDestinationECUser(" + destinationName + "," + ecuserName + ")");
        try {
            return monitor
                    .done(MasterManager.getMonitoringCache(destinationName).getDestinationECUsers().get(ecuserName));
        } catch (final Exception e) {
            return monitor.done(dataBaseInterface.getDestinationECUser(destinationName, ecuserName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the association.
     */
    @Override
    public Association getAssociation(final String destinationName, final String hostName)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName) || isEmpty(hostName)) {
            throw new DataBaseException("Invalid parameter(s) for getAssociation");
        }
        final var monitor = new MonitorCall("getAssociation(" + destinationName + "," + hostName + ")");
        try {
            return monitor.done(MasterManager.getMonitoringCache(destinationName).getAssociations().get(hostName));
        } catch (final Exception e) {
            return monitor.done(dataBaseInterface.getAssociation(destinationName, hostName));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the policy association.
     */
    @Override
    public PolicyAssociation getPolicyAssociation(final String destinationName, final String policyId)
            throws DataBaseException, RemoteException {
        if (isEmpty(destinationName) || isEmpty(policyId)) {
            throw new DataBaseException("Invalid parameter(s) for getPolicyAssociation");
        }
        final var monitor = new MonitorCall("getPolicyAssociation(" + destinationName + "," + policyId + ")");
        return monitor.done(dataBaseInterface.getPolicyAssociation(destinationName, policyId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the alias.
     */
    @Override
    public Alias getAlias(final String desName, final String destinationName)
            throws DataBaseException, RemoteException {
        if (isEmpty(desName) || isEmpty(destinationName)) {
            throw new DataBaseException("Invalid parameter(s) for getAlias");
        }
        final var monitor = new MonitorCall("getAlias(" + desName + "," + destinationName + ")");
        return monitor.done(dataBaseInterface.getAlias(desName, destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ectrans module.
     */
    @Override
    public ECtransModule getECtransModule(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getECtransModule");
        }
        final var monitor = new MonitorCall("getECtransModule(" + name + ")");
        return monitor.done(dataBaseInterface.getECtransModule(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ectrans module array.
     */
    @Override
    public ECtransModule[] getECtransModuleArray() throws RemoteException {
        final var monitor = new MonitorCall("getECtransModuleArray()");
        return monitor.done(dataBaseInterface.getECtransModuleArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host.
     */
    @Override
    public Host getHost(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getHost");
        }
        final var monitor = new MonitorCall("getHost(" + name + ")");
        return monitor.done(dataBaseInterface.getHost(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host array.
     */
    @Override
    public Host[] getHostArray() throws RemoteException {
        final var monitor = new MonitorCall("getHostArray()");
        return monitor.done(dataBaseInterface.getHostArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer method.
     */
    @Override
    public TransferMethod getTransferMethod(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getTransferMethod");
        }
        final var monitor = new MonitorCall("getTransferMethod(" + name + ")");
        return monitor.done(dataBaseInterface.getTransferMethod(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host EC user.
     */
    @Override
    public HostECUser getHostECUser(final String ecuserName, final String hostName)
            throws DataBaseException, RemoteException {
        if (isEmpty(ecuserName) || isEmpty(hostName)) {
            throw new DataBaseException("Invalid parameter(s) for getHostECUser");
        }
        final var monitor = new MonitorCall("getHostECUser(" + ecuserName + "," + hostName + ")");
        return monitor.done(dataBaseInterface.getHostECUser(ecuserName, hostName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history.
     */
    @Override
    public TransferHistory getTransferHistory(final long transferHistoryId) throws DataBaseException, RemoteException {
        if (transferHistoryId < 0) {
            throw new DataBaseException("Invalid parameter(s) for getTransferHistory");
        }
        final var monitor = new MonitorCall("getTransferHistory(" + transferHistoryId + ")");
        return monitor.done(dataBaseInterface.getTransferHistory(transferHistoryId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer method array.
     */
    @Override
    public TransferMethod[] getTransferMethodArray() throws RemoteException {
        final var monitor = new MonitorCall("getTransferMethodArray()");
        return monitor.done(dataBaseInterface.getTransferMethodArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the category.
     */
    @Override
    public Category getCategory(final long id) throws DataBaseException, RemoteException {
        if (id < 0) {
            throw new DataBaseException("Invalid parameter(s) for getCategory");
        }
        final var monitor = new MonitorCall("getCategory(" + id + ")");
        try {
            for (final CatUrl catUrl : MasterManager.getCatUrls()) {
                final var category = catUrl.getCategory();
                if (category.getId() == id) {
                    return monitor.done(category);
                }
            }
        } catch (final Exception e) {
            _log.debug("getCategory", e);
        }
        return monitor.done(dataBaseInterface.getCategory(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the category array.
     */
    @Override
    public Category[] getCategoryArray() throws RemoteException {
        final var monitor = new MonitorCall("getCategoryArray()");
        return monitor.done(dataBaseInterface.getCategoryArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the cat url.
     */
    @Override
    public CatUrl getCatUrl(final long categoryId, final String urlName) throws DataBaseException, RemoteException {
        if (categoryId < 0 || isEmpty(urlName)) {
            throw new DataBaseException("Invalid parameter(s) for getCatUrl");
        }
        final var monitor = new MonitorCall("getCatUrl(" + categoryId + "," + urlName + ")");
        try {
            for (final CatUrl catUrl : MasterManager.getCatUrls()) {
                if (catUrl.getCategoryId() == categoryId && catUrl.getUrlName().equals(urlName)) {
                    return monitor.done(catUrl);
                }
            }
        } catch (final Exception e) {
            _log.debug("getCatUrl", e);
        }
        return monitor.done(dataBaseInterface.getCatUrl(categoryId, urlName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the url array.
     */
    @Override
    public Url[] getUrlArray() throws RemoteException {
        final var monitor = new MonitorCall("getUrlArray()");
        return monitor.done(dataBaseInterface.getUrlArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the url.
     */
    @Override
    public Url getUrl(final String name) throws DataBaseException, RemoteException {
        if (isEmpty(name)) {
            throw new DataBaseException("Invalid parameter(s) for getUrl");
        }
        final var monitor = new MonitorCall("getUrl(" + name + ")");
        final var catUrls = MasterManager.getCatUrls();
        if (!catUrls.isEmpty()) {
            for (final CatUrl catUrl : catUrls) {
                final var url = catUrl.getUrl();
                if (name.equals(url.getName())) {
                    return monitor.done(url);
                }
            }
            // We could not found it in the cache so we give up as it should be
            // in the cache!
            throw new DataBaseException("Url " + name + " not found");

        }
        // The cache is empty so we try to get it from the master!
        return monitor.done(dataBaseInterface.getUrl(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the web user.
     */
    @Override
    public WebUser getWebUser(final String id) throws DataBaseException, RemoteException {
        if (isEmpty(id)) {
            throw new DataBaseException("Invalid parameter(s) for getWebUser");
        }
        final var monitor = new MonitorCall("getWebUser(" + id + ")");
        return monitor.done(dataBaseInterface.getWebUser(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the web user array.
     */
    @Override
    public WebUser[] getWebUserArray() throws RemoteException {
        final var monitor = new MonitorCall("getWebUserArray()");
        return monitor.done(dataBaseInterface.getWebUserArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the weu cat.
     */
    @Override
    public WeuCat getWeuCat(final long categoryId, final String webuserId) throws DataBaseException, RemoteException {
        if (isEmpty(webuserId)) {
            throw new DataBaseException("Invalid parameter(s) for getWeuCat");
        }
        final var monitor = new MonitorCall("getWeuCat(" + categoryId + "," + webuserId + ")");
        return monitor.done(dataBaseInterface.getWeuCat(categoryId, webuserId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the users per category id.
     */
    @Override
    public Collection<WebUser> getUsersPerCategoryId(final String categoryId)
            throws DataBaseException, RemoteException {
        if (isEmpty(categoryId)) {
            throw new DataBaseException("Invalid parameter(s) for getUsersPerCategoryId");
        }
        final var monitor = new MonitorCall("getUsersPerCategoryId(" + categoryId + ")");
        return monitor.done(dataBaseInterface.getUsersPerCategoryId(categoryId));
    }

    /**
     * {@inheritDoc}
     *
     * Incoming user del.
     */
    @Override
    public void incomingUserDel(final String user, final String id) throws DataBaseException, RemoteException {
        if (isEmpty(user) || isEmpty(id)) {
            throw new DataBaseException("Invalid parameter(s) for incomingUserDel");
        }
        final var monitor = new MonitorCall("incomingUserDel(" + user + "," + id + ")");
        dataBaseInterface.incomingUserDel(user, id);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming user add.
     */
    @Override
    public void incomingUserAdd(final String user, final String id, final String password, final String email,
            final String iso) throws DataBaseException, RemoteException {
        if (isEmpty(user) || isEmpty(id)) {
            throw new DataBaseException("Invalid parameter(s) for incomingUserAdd");
        }
        final var monitor = new MonitorCall(
                "incomingUserAdd(" + user + "," + id + "," + password + "," + email + "," + iso + ")");
        dataBaseInterface.incomingUserAdd(user, id, password, email, iso);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming association add.
     */
    @Override
    public void incomingAssociationAdd(final String user, final String id, final String destination)
            throws DataBaseException, RemoteException {
        if (isEmpty(user) || isEmpty(id) || isEmpty(destination)) {
            throw new DataBaseException("Invalid parameter(s) for incomingAssociationAdd");
        }
        final var monitor = new MonitorCall("incomingAssociationAdd(" + user + "," + id + "," + destination + ")");
        dataBaseInterface.incomingAssociationAdd(user, id, destination);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming category add.
     */
    @Override
    public void incomingCategoryAdd(final String user, final String id, final List<String> categories)
            throws DataBaseException, RemoteException {
        if (isEmpty(user) || isEmpty(id) || isEmpty(categories)) {
            throw new DataBaseException("Invalid parameter(s) for incomingCategoryAdd");
        }
        final var monitor = new MonitorCall("incomingCategoryAdd(" + user + "," + id + "," + categories.size() + ")");
        dataBaseInterface.incomingCategoryAdd(user, id, categories);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming user add 2.
     */
    @Override
    public String incomingUserAdd2(final String user, final String id, final String email, final String iso)
            throws DataBaseException, RemoteException {
        if (isEmpty(user) || isEmpty(id) || isEmpty(email) || isEmpty(iso)) {
            throw new DataBaseException("Invalid parameter(s) for incomingUserAdd2");
        }
        final var monitor = new MonitorCall("incomingUserAdd2(" + user + "," + id + "," + email + "," + iso + ")");
        return monitor.done(dataBaseInterface.incomingUserAdd2(user, id, email, iso));
    }

    /**
     * {@inheritDoc}
     *
     * Incoming user list.
     */
    @Override
    public Collection<IncomingUser> incomingUserList(final String user, final String destination)
            throws DataBaseException, RemoteException {
        if (isEmpty(user)) {
            throw new DataBaseException("Invalid parameter(s) for incomingUserList");
        }
        final var monitor = new MonitorCall("incomingUserList(" + user + "," + destination + ")");
        return monitor.done(dataBaseInterface.incomingUserList(user, destination));
    }

    /**
     * {@inheritDoc}
     *
     * Incoming association del.
     */
    @Override
    public void incomingAssociationDel(final String user, final String id, final String destination)
            throws DataBaseException, RemoteException {
        if (isEmpty(user) || isEmpty(id) || isEmpty(destination)) {
            throw new DataBaseException("Invalid parameter(s) for incomingAssociationDel");
        }
        final var monitor = new MonitorCall("incomingAssociationDel(" + user + "," + id + "," + destination + ")");
        dataBaseInterface.incomingAssociationDel(user, id, destination);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming association list.
     */
    @Override
    public String[] incomingAssociationList(final String user, final String id)
            throws DataBaseException, RemoteException {
        if (isEmpty(user) || isEmpty(id)) {
            throw new DataBaseException("Invalid parameter(s) for incomingAssociationList");
        }
        final var monitor = new MonitorCall("incomingAssociationList(" + user + "," + id + ")");
        return monitor.done(dataBaseInterface.incomingAssociationList(user, id));
    }

    /**
     * {@inheritDoc}
     *
     * Destination list.
     */
    @Override
    public Collection<Destination> destinationList(final String user, final String id, final String iso,
            final Integer type) throws DataBaseException, RemoteException {
        if (isEmpty(user) || iso != null && iso.isEmpty() || id != null && id.isEmpty()) {
            throw new DataBaseException("Invalid parameter(s) for destinationList");
        }
        final var monitor = new MonitorCall("destinationList(" + user + "," + id + "," + iso + "," + type + ")");
        return monitor.done(dataBaseInterface.destinationList(user, id, iso, type));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination backup.
     */
    @Override
    public DestinationBackup getDestinationBackup(final String user, final String id, final String iso,
            final Integer type, final String name) throws DataBaseException, RemoteException {
        if (isEmpty(user) || iso != null && iso.isEmpty() || id != null && id.isEmpty()) {
            throw new DataBaseException("Invalid parameter(s) for getDestinationBackup");
        }
        final var monitor = new MonitorCall(
                "destinationBackupList(" + user + "," + id + "," + iso + "," + type + "," + name + ")");
        return monitor.done(dataBaseInterface.getDestinationBackup(user, id, iso, type, name));
    }

    /**
     * {@inheritDoc}
     *
     * Put destination backup.
     */
    @Override
    public int putDestinationBackup(final String user, final DestinationBackup backup, final boolean copySharedHost)
            throws DataBaseException, MasterException, RemoteException {
        if (isEmpty(user) || backup == null) {
            throw new DataBaseException("Invalid parameter(s) for putDestinationBackup");
        }
        final var monitor = new MonitorCall("putDestinationBackup(" + user + ")");
        return monitor.done(dataBaseInterface.putDestinationBackup(user, backup, copySharedHost));
    }

    /**
     * {@inheritDoc}
     *
     * Destination country list.
     */
    @Override
    public Collection<Country> destinationCountryList(final String user) throws DataBaseException, RemoteException {
        if (isEmpty(user)) {
            throw new DataBaseException("Invalid parameter(s) for destinationCountryList");
        }
        final var monitor = new MonitorCall("destinationCountryList(" + user + ")");
        return monitor.done(dataBaseInterface.destinationCountryList(user));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination option list.
     */
    @Override
    public List<TypeEntry> getDestinationOptionList() throws RemoteException {
        final var monitor = new MonitorCall("getDestinationOptionList()");
        return monitor.done(dataBaseInterface.getDestinationOptionList());
    }

    /**
     * {@inheritDoc}
     *
     * Datafile put.
     */
    @Override
    public long datafilePut(final String user, final String remoteHost, final String destination, final String metadata,
            final String source, final String uniqueName, final String target, final Integer priority,
            final String lifeTime, final String at, final Boolean standby, final Boolean force)
            throws DataBaseException, RemoteException {
        if (isEmpty(user) || destination != null && destination.isEmpty() || source != null && source.isEmpty()) {
            throw new DataBaseException("Invalid parameter(s) for datafilePut");
        }
        final var monitor = new MonitorCall(
                "datafilePut(" + user + "," + destination + "," + metadata + "," + source + "," + uniqueName + ","
                        + target + "," + priority + "," + lifeTime + "," + standby + "," + force + ")");
        return monitor.done(dataBaseInterface.datafilePut(user, remoteHost, destination, metadata, source, uniqueName,
                target, priority, lifeTime, at, standby, force));
    }

    /**
     * {@inheritDoc}
     *
     * Datafile size.
     */
    @Override
    public long datafileSize(final String user, final Long dataFileId) throws DataBaseException, RemoteException {
        if (isEmpty(user) || dataFileId != null) {
            throw new DataBaseException("Invalid parameter(s) for datafileSize");
        }
        final var monitor = new MonitorCall("datafileSize(" + user + "," + dataFileId + ")");
        return monitor.done(dataBaseInterface.datafileSize(user, dataFileId));
    }

    /**
     * {@inheritDoc}
     *
     * Datafile del.
     */
    @Override
    public void datafileDel(final String user, final Long dataFileId)
            throws MasterException, DataBaseException, RemoteException {
        if (isEmpty(user) || dataFileId != null) {
            throw new DataBaseException("Invalid parameter(s) for datafileDel");
        }
        final var monitor = new MonitorCall("datafileDel(" + user + "," + dataFileId + ")");
        dataBaseInterface.datafileDel(user, dataFileId);
        monitor.done();
    }

    /**
     * The Class DataTransferCache.
     */
    static final class DataTransferCache {
        /** The transfer. */
        DataTransfer transfer = null;

        /** The last update. */
        long lastUpdate = -1;

        /**
         * Gets the data transfer.
         *
         * @param dataTransferId
         *            the dataTransfer id
         *
         * @return the data transfer
         */
        synchronized DataTransfer getDataTransfer(final long dataTransferId) {
            if (transfer != null && dataTransferId == transfer.getId()
                    && System.currentTimeMillis() - lastUpdate < 2 * Timer.ONE_SECOND) {
                return (DataTransfer) transfer.clone();
            }
            return null;
        }

        /**
         * Sets the data transfer.
         *
         * @param dataTransfer
         *            the new data transfer
         */
        synchronized void setDataTransfer(final DataTransfer dataTransfer) {
            transfer = dataTransfer;
            lastUpdate = System.currentTimeMillis();
        }
    }
}
