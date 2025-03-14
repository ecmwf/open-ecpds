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
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.callback.CallBackObject;
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
import ecmwf.common.database.DestinationList;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Event;
import ecmwf.common.database.Host;
import ecmwf.common.database.HostECUser;
import ecmwf.common.database.HostLocation;
import ecmwf.common.database.HostOutput;
import ecmwf.common.database.HostStats;
import ecmwf.common.database.IncomingAssociation;
import ecmwf.common.database.IncomingHistory;
import ecmwf.common.database.IncomingPermission;
import ecmwf.common.database.IncomingPolicy;
import ecmwf.common.database.IncomingUser;
import ecmwf.common.database.MetadataAttribute;
import ecmwf.common.database.MetadataValue;
import ecmwf.common.database.Operation;
import ecmwf.common.database.PolicyAssociation;
import ecmwf.common.database.PolicyUser;
import ecmwf.common.database.ProductStatus;
import ecmwf.common.database.Rates;
import ecmwf.common.database.SQLParameterParser;
import ecmwf.common.database.SchedulerValue;
import ecmwf.common.database.Statistics;
import ecmwf.common.database.Traffic;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferHistory;
import ecmwf.common.database.TransferMethod;
import ecmwf.common.database.TransferServer;
import ecmwf.common.database.Url;
import ecmwf.common.database.WebUser;
import ecmwf.common.database.WeuCat;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.ecpds.ECpdsClient;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.DestinationOption.TypeEntry;
import ecmwf.ecpds.master.transfer.HostComparator;
import ecmwf.ecpds.master.transfer.StatusFactory;

/**
 * The Class DataBaseImpl.
 */
final class DataBaseImpl extends CallBackObject implements DataBaseInterface {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3297971691860632524L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(DataBaseImpl.class);

    /** The email pattern **/
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** The ecpds. */
    private final transient ECpdsBase ecpds;

    /** The master. */
    private final transient MasterServer master;

    /**
     * Instantiates a new data base impl.
     *
     * @param master
     *            the master
     * @param ecpds
     *            the ecpds
     *
     * @throws RemoteException
     *             the remote exception
     */
    DataBaseImpl(final MasterServer master, final ECpdsBase ecpds) throws RemoteException {
        this.master = master;
        this.ecpds = ecpds;
    }

    /**
     * {@inheritDoc}
     *
     * Insert.
     */
    @Override
    public void insert(final ECpdsSession session, final DataBaseObject object, final boolean createPk)
            throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall("insert(" + session.getWebUser().getName() + ","
                + object.getClass().getSimpleName() + "," + createPk + ")");
        SchedulerValue value = null;
        Destination destination = null;
        HostOutput hostOutput = null;
        HostLocation hostLocation = null;
        HostStats hostStats = null;
        // Anything special to do?
        if (object instanceof final Destination theDestination) {
            destination = theDestination;
            final var name = destination.getName();
            if (isEmpty(name)) {
                throw new MasterException("No name specified for the Destination");
            }
            // We have to create a scheduler value for the destination!
            value = new SchedulerValue();
            value.setHasRequeued(false);
            value.setStartCount(0);
            ecpds.insert(value, true);
            destination.setSchedulerValueId(value.getId());
            destination.setSchedulerValue(value);
            destination.setStatusCode(StatusFactory.INIT);
            try {
                AccessControl.insertAccessControl(ecpds, name);
            } catch (final Throwable e) {
                _log.warn("Could not add access control for Destination: {}", name, e);
                destination = null;
            }
        } else if (object instanceof final Host theHost) {
            final var host = theHost;
            final var nickName = host.getNickname();
            if (isEmpty(nickName)) {
                throw new MasterException("No Nickname specified for the Host");
            }
            // We have to create a host output for the host!
            ecpds.insert(hostOutput = new HostOutput(), true);
            host.setHostOutputId(hostOutput.getId());
            host.setHostOutput(hostOutput);
            // And a host location!
            ecpds.insert(hostLocation = new HostLocation(), true);
            host.setHostLocationId(hostLocation.getId());
            host.setHostLocation(hostLocation);
            // And a host stats!
            ecpds.insert(hostStats = new HostStats(), true);
            host.setHostStatsId(hostStats.getId());
            host.setHostStats(hostStats);
        } else if (object instanceof final IncomingAssociation incomingAssociation) {
            ecpds.clearIncomingUserCache(incomingAssociation.getIncomingUserId());
        } else if (object instanceof final IncomingPermission incomingPermission) {
            ecpds.clearIncomingUserCache(incomingPermission.getIncomingUserId());
        } else if (object instanceof final PolicyUser policyUser) {
            ecpds.clearIncomingUserCache(policyUser.getIncomingUserId());
        } else if (object instanceof final TransferServer transferServer) {
            ecpds.clearTransferServerCache(transferServer.getTransferGroupName());
        } else if (object instanceof final TransferGroup transferGroup) {
            ecpds.clearIncomingUserCache(transferGroup.getName());
        }
        final var action = master.startECpdsAction(session, "insert", object);
        Exception exception = null;
        var inserted = false;
        try {
            ecpds.insert(object, createPk, true);
            inserted = true;
            monitor.done();
        } catch (final DataBaseException e) {
            _log.warn(exception = e);
            throw e;
        } catch (final Exception e) {
            _log.warn(exception = e);
            throw new MasterException(e.getMessage());
        } finally {
            if (!inserted) {
                try {
                    ecpds.remove(new DataBaseObject[] { value, hostLocation, hostStats, hostOutput });
                } catch (final Throwable e) {
                }
                if (destination != null) {
                    try {
                        AccessControl.removeAccessControl(ecpds, destination.getName());
                    } catch (final Throwable e) {
                    }
                }
            } else // Force an update of the monitoring if required!
            if (destination != null && MonitorManager.isActivated()) {
                try {
                    master.getTransferScheduler().getMonitoringThread().wakeup();
                } catch (final Throwable t) {
                    _log.warn("Could not wakeup monitoring", t);
                }
            }
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Update.
     */
    @Override
    public void update(final ECpdsSession session, final DataBaseObject object)
            throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "update(" + session.getWebUser().getName() + "," + object.getClass().getSimpleName() + ")");
        // Let's try to get the original DataBaseObject for the notification to
        // allow computing the differences!
        final DataBaseObject argument;
        if (object instanceof final Destination destination) {
            argument = ecpds.getDestinationObject(destination.getName());
        } else if (object instanceof final TransferServer transferServer) {
            argument = ecpds.getTransferServerObject(transferServer.getName());
            ecpds.clearTransferServerCache(transferServer.getTransferGroupName());
        } else if (object instanceof final TransferGroup transferGroup) {
            argument = ecpds.getTransferGroupObject(transferGroup.getName());
            ecpds.clearIncomingUserCache(transferGroup.getName());
        } else if (object instanceof final WebUser webUser) {
            argument = ecpds.getWebUserObject(webUser.getId());
        } else if (object instanceof final IncomingUser incomingUser) {
            argument = ecpds.getIncomingUserObject(incomingUser.getId());
            ecpds.clearIncomingUserCache(incomingUser.getId());
        } else if (object instanceof final TransferMethod transferMethod) {
            argument = ecpds.getTransferMethodObject(transferMethod.getName());
        } else if (object instanceof final ECtransModule ectransModule) {
            argument = ecpds.getECtransModuleObject(ectransModule.getName());
        } else if (object instanceof final DataTransfer dataTransfer) {
            argument = ecpds.getDataTransferObject(dataTransfer.getId());
        } else if (object instanceof final IncomingAssociation incomingAssociation) {
            argument = null;
            ecpds.clearIncomingUserCache(incomingAssociation.getIncomingUserId());
        } else if (object instanceof final IncomingPermission incomingPermission) {
            argument = null;
            ecpds.clearIncomingUserCache(incomingPermission.getIncomingUserId());
        } else if (object instanceof final PolicyUser policyUser) {
            argument = null;
            ecpds.clearIncomingUserCache(policyUser.getIncomingUserId());
        } else {
            argument = null;
        }
        final var noArgs = argument == null;
        final var action = master.startECpdsAction(session, "update", noArgs ? object : argument);
        Exception exception = null;
        var updated = false;
        try {
            ecpds.update(object);
            updated = true;
            monitor.done();
        } catch (final DataBaseException e) {
            _log.warn(exception = e);
            throw e;
        } catch (final Exception e) {
            _log.warn(exception = e);
            throw new MasterException(e.getMessage());
        } finally {
            if (object instanceof Destination && updated && MonitorManager.isActivated()) {
                // Force an update of the monitoring just in case the monitoring
                // flag have been changed!
                try {
                    master.getTransferScheduler().getMonitoringThread().wakeup();
                } catch (final Throwable t) {
                    _log.warn("Could not wakeup monitoring", t);
                }
            }
            master.logECpdsAction(action, null, noArgs ? null : object, exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Removes the.
     */
    @Override
    public void remove(final ECpdsSession session, final DataBaseObject object)
            throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "remove(" + session.getWebUser().getName() + "," + object.getClass().getSimpleName() + ")");
        final var action = master.startECpdsAction(session, "remove", object);
        if (object instanceof final IncomingAssociation incomingAssociation) {
            ecpds.clearIncomingUserCache(incomingAssociation.getIncomingUserId());
        } else if (object instanceof final IncomingPermission incomingPermission) {
            ecpds.clearIncomingUserCache(incomingPermission.getIncomingUserId());
        } else if (object instanceof final PolicyUser policyUser) {
            ecpds.clearIncomingUserCache(policyUser.getIncomingUserId());
        } else if (object instanceof final TransferServer transferServer) {
            ecpds.clearTransferServerCache(transferServer.getTransferGroupName());
        } else if (object instanceof final TransferGroup transferGroup) {
            ecpds.clearIncomingUserCache(transferGroup.getName());
        }
        Exception exception = null;
        try {
            ecpds.remove(object);
            monitor.done();
        } catch (final DataBaseException e) {
            _log.warn(exception = e);
            throw e;
        } catch (final Exception e) {
            _log.warn(exception = e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the initial data transfer events.
     */
    @Override
    public void getInitialDataTransferEvents(final String target) throws RemoteException {
        final var monitor = new MonitorCall("getInitialDataTransferEvents()");
        master.getInitialDataTransferEvents(target);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the initial product status events.
     */
    @Override
    public void getInitialProductStatusEvents(final String target) throws RemoteException {
        final var monitor = new MonitorCall("getInitialProductStatusEvents()");
        master.getInitialProductStatusEvents(target, null, null);
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
        master.getInitialChangeHostEvents(target);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfer count not done by product and time on date.
     */
    @Override
    public int getDataTransferCountNotDoneByProductAndTimeOnDate(final String destination, final String product,
            final String time, final Date from, final Date to) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDataTransferCountNotDoneByProductAndTimeOnDate(" + destination + ","
                + product + "," + time + "," + from + "," + to + ")");
        return monitor
                .done(ecpds.getDataTransferCountNotDoneByProductAndTimeOnDate(destination, product, time, from, to));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the product status.
     */
    @Override
    public ProductStatus[] getProductStatus(final String stream, final String time, final String type, final long step,
            final int limit) throws RemoteException, DataBaseException {
        final var monitor = new MonitorCall(
                "getProductStatus(" + stream + "," + time + "," + type + "," + step + "," + limit + ")");
        return monitor.done(ecpds.getProductStatus(stream, time, type, step, limit));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer servers.
     */
    @Override
    public TransferServer[] getTransferServers(final String groupName) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferServers(" + groupName + ")");
        final var movers = ecpds.getTransferServers(groupName);
        for (final TransferServer mover : movers) {
            mover.setLastUpdate(master.lastUpdateForClientInterface(mover.getName(), "DataMover"));
        }
        return monitor.done(movers);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the statistics.
     */
    @Override
    public Statistics[] getStatistics(final Date fromDate, final Date toDate, final String groupName,
            final String status, final String type) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getStatistics(" + fromDate + "," + toDate + "," + groupName + "," + status + "," + type + ")");
        return monitor.done(ecpds.getStatistics(fromDate, toDate, groupName, status, type));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the rates.
     */
    @Override
    public Rates[] getRates(final Date fromDate, final Date toDate, final String caller, final String sourceHost)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getRates(" + fromDate + "," + toDate + "," + caller + "," + sourceHost + ")");
        return monitor.done(ecpds.getRates(fromDate, toDate, caller, sourceHost));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the rates per transfer server.
     */
    @Override
    public Rates[] getRatesPerTransferServer(final Date fromDate, final Date toDate, final String caller,
            final String sourceHost) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getRatesPerTransferServer(" + fromDate + "," + toDate + "," + caller + "," + sourceHost + ")");
        return monitor.done(ecpds.getRatesPerTransferServer(fromDate, toDate, caller, sourceHost));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the rates per file system.
     */
    @Override
    public Rates[] getRatesPerFileSystem(final Date fromDate, final Date toDate, final String transferServerName,
            final String caller, final String sourceHost) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getRatesPerFileSystem(" + fromDate + "," + toDate + ","
                + transferServerName + "," + caller + "," + sourceHost + ")");
        return monitor.done(ecpds.getRatesPerFileSystem(fromDate, toDate, transferServerName, caller, sourceHost));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination ecuser.
     */
    @Override
    public ECUser[] getDestinationEcuser(final String destinationName) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationEcuser(" + destinationName + ")");
        return monitor.done(ecpds.getDestinationEcuser(destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination incoming policies.
     */
    @Override
    public IncomingPolicy[] getDestinationIncomingPolicies(final String destinationName)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationIncomingPolicies(" + destinationName + ")");
        return monitor.done(ecpds.getDestinationIncomingPolicy(destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination aliases.
     */
    @Override
    public Destination[] getDestinationAliases(final String name, final String mode)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationAliases(" + name + "," + mode + ")");
        return monitor.done(ecpds.getDestinationAliases(name, mode));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the aliases.
     */
    @Override
    public Alias[] getAliases(final String name, final String mode) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getAliases(" + name + "," + mode + ")");
        return monitor.done(ecpds.getAliases(name, mode));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the e cuser events.
     */
    @Override
    public Collection<Event> getECuserEvents(final String userName, final Date onIsoDate, final String search,
            final DataBaseCursor cursor) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getECuserEvents(" + userName + "," + onIsoDate + ")");
        return monitor.done(ecpds.getECuserEvents(userName, onIsoDate, search, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming history.
     */
    @Override
    public Collection<IncomingHistory> getIncomingHistory(final String userName, final Date onIsoDate,
            final String search, final DataBaseCursor cursor) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getIncomingHistory(" + userName + "," + onIsoDate + "," + search + ")");
        return monitor.done(ecpds.getIncomingHistory(userName, onIsoDate, search, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by host name.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByHostName(final String name, final Date from, final Date to)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDataTransfersByHostName(" + name + "," + from + "," + to + ")");
        return monitor.done(ecpds.getDataTransfersByHostName(master.dataCache, name, from, to));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by transfer server name.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByTransferServerName(final String name, final Date from,
            final Date to) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getDataTransfersByTransferServerName(" + name + "," + from + "," + to + ")");
        return monitor.done(ecpds.getDataTransfersByTransferServerName(master.dataCache, name, from, to));
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
        final var monitor = new MonitorCall(
                "getDataTransfersByStatusCodeAndDate(" + status + "," + from + "," + to + "," + type + ")");
        return monitor.done(ecpds.getSortedDataTransfersByStatusCodeAndDate(master.dataCache, status, from, to, search,
                type, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by data file id.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByDataFileId(final long dataFileId, final boolean includeDeleted)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDataTransfersByDataFileId(" + dataFileId + "," + includeDeleted + ")");
        return monitor.done(ecpds.getDataTransfersByDataFileId(master.dataCache, dataFileId, includeDeleted));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations by country ISO.
     */
    @Override
    public Destination[] getDestinationsByCountryISO(final String isoCode) throws RemoteException {
        final var monitor = new MonitorCall("getDestinationsByCountryISO(" + isoCode + ")");
        return monitor.done(ecpds.getDestinationsByCountryISO(isoCode));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations by user.
     */
    @Override
    public Destination[] getDestinationsByUser(final String uid, final String search, final String fromToAliases,
            final boolean asc, final String status, final String type, final String filter) throws IOException {
        final String searchString;
        if (EMAIL_PATTERN.matcher(search).matches()) {
            searchString = "email=" + search;
        } else {
            searchString = search;
        }
        final var monitor = new MonitorCall("getDestinationsByUser(" + uid + "," + searchString + "," + fromToAliases
                + "," + asc + "," + status + "," + type + "," + filter + ")");
        final var options = new SQLParameterParser(searchString, "name", "comment", "country", "options", "enabled=?",
                "monitor=?", "backup=?", "email");
        final var email = options.remove("email");
        final var foundDestinations = ecpds.getDestinationsByUser(uid, options, fromToAliases, asc, status, type,
                filter);
        if (email != null && !email.isEmpty()) {
            // An email is provided, so remove all the destinations which are not associated
            // with it!
            final var destinationNames = master.getManagementInterface().getDestinationNamesForContact(email,
                    options.isCaseSensitive());
            foundDestinations.removeIf(d -> !destinationNames.contains(d.getName()));
        }
        return monitor.done(foundDestinations.toArray(new Destination[0]));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations by host name.
     */
    @Override
    public Destination[] getDestinationsByHostName(final String hostName) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationsByHostName(" + hostName + ")");
        return monitor.done(ecpds.getDestinationsByHostName(hostName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history by data transfer id.
     */
    @Override
    public TransferHistory[] getTransferHistoryByDataTransferId(final long dataTransferId)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferHistoryByDataTransferId(" + dataTransferId + ")");
        return monitor.done(ecpds.getTransferHistoryByDataTransferId(dataTransferId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history by data transfer id.
     */
    @Override
    public TransferHistory[] getTransferHistoryByDataTransferId(final long dataTransferId,
            final boolean afterScheduleTime, final DataBaseCursor cursor) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferHistory(" + dataTransferId + ")");
        return monitor.done(ecpds.getSortedTransferHistory(dataTransferId, afterScheduleTime, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the categories per user id.
     */
    @Override
    public Collection<Category> getCategoriesPerUserId(final String userId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getCategoriesPerUserId(" + userId + ")");
        return monitor.done(ecpds.getCategoriesPerUserId(userId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the urls per category id.
     */
    @Override
    public Collection<Url> getUrlsPerCategoryId(final String id) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getUrlsPerCategoryId(" + id + ")");
        return monitor.done(ecpds.getUrlsPerCategoryId(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the categories per resource id.
     */
    @Override
    public Collection<Category> getCategoriesPerResourceId(final String id) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getCategoriesPerResourceId(" + id + ")");
        return monitor.done(ecpds.getCategoriesPerResourceId(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data files by meta data.
     */
    @Override
    public Collection<DataFile> getDataFilesByMetaData(final String name, final String value, final Date from,
            final Date to, final DataBaseCursor cursor) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getDataFilesByMetaData(" + name + "," + value + "," + from + "," + to + ")");
        return monitor.done(ecpds.getDataFilesByMetaData(name, value, from, to, cursor));
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
        final var monitor = new MonitorCall("getTransferCountAndMetaDataByFilter(" + countBy + "," + destination + ","
                + target + "," + stream + "," + time + "," + status + "," + fileName + "," + from + "," + to + ","
                + privilegedUser + "," + scheduledBefore + ")");
        return monitor.done(ecpds.getTransferCountAndMetaDataByFilter(countBy, destination, target, stream, time,
                status, fileName, from, to, privilegedUser, scheduledBefore));
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
        final var monitor = new MonitorCall("getDataTransfersByFilter(" + destination + "," + target + "," + stream
                + "," + time + "," + status + "," + fileName + "," + from + "," + to + ")");
        final var transfers = ecpds.getDataTransfersByFilter(master.dataCache, destination, target, stream, time,
                status, "false", new Date(Long.MAX_VALUE), fileName, from, to, cursor);
        final var array = new ArrayList<DataTransferWithPermissions>();
        for (final DataTransfer transfer : transfers) {
            array.add(
                    new DataTransferWithPermissions(transfer, master.updateTransferStatus(transfer, StatusFactory.STOP),
                            master.updateTransferStatus(transfer, StatusFactory.WAIT),
                            master.updateTransferStatus(transfer, StatusFactory.HOLD)));
        }
        return monitor.done(array);
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
        final var monitor = new MonitorCall("getDataTransfersByFilter(" + destination + "," + target + "," + stream
                + "," + time + "," + status + "," + fileName + "," + from + "," + to + ")");
        final var transfers = ecpds.getDataTransfersByFilter(master.dataCache, destination, target, stream, time,
                status, privilegedUser, scheduledBefore, fileName, from, to);
        final var array = new ArrayList<DataTransferWithPermissions>();
        for (final DataTransfer transfer : transfers) {
            array.add(
                    new DataTransferWithPermissions(transfer, master.updateTransferStatus(transfer, StatusFactory.STOP),
                            master.updateTransferStatus(transfer, StatusFactory.WAIT),
                            master.updateTransferStatus(transfer, StatusFactory.HOLD)));
        }
        return monitor.done(array);
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
                "getFilteredHosts(" + label + "," + filter + "," + network + "," + type + "," + search + ")");
        return monitor.done(ecpds.getFilteredHosts(label, filter, network, type, search, cursor));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the hosts by destination id.
     */
    @Override
    public Collection<Host> getHostsByDestinationId(final String destId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getHostsByDestinationId(" + destId + ")");
        return monitor.done(ecpds.getHostsByDestinationId(destId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the hosts by transfer method id.
     */
    @Override
    public Collection<Host> getHostsByTransferMethodId(final String transferMethodId)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getHostsByTransferMethodId(" + transferMethodId + ")");
        return monitor.done(ecpds.getHostsByTransferMethodId(transferMethodId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer methods by ec trans module name.
     */
    @Override
    public Collection<TransferMethod> getTransferMethodsByEcTransModuleName(final String ecTransModuleName)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferMethodsByEcTransModuleName(" + ecTransModuleName + ")");
        return monitor.done(ecpds.getTransferMethodsByEcTransModuleName(ecTransModuleName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta data by data file id.
     */
    @Override
    public Collection<MetadataValue> getMetaDataByDataFileId(final long dataFileId)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getMetaDataByDataFileId(" + dataFileId + ")");
        return monitor.done(ecpds.getMetaDataByDataFileId(dataFileId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta data by attribute name.
     */
    @Override
    public Collection<MetadataValue> getMetaDataByAttributeName(final String id)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getMetaDataByAttributeName(" + id + ")");
        return monitor.done(ecpds.getMetaDataByAttributeName(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by destination and identity.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByDestinationAndIdentity(final String destination,
            final String identity) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getDataTransfersByDestinationAndIdentity(" + destination + "," + identity + ")");
        return monitor.done(ecpds.getDataTransfersByDestinationAndIdentity(master.dataCache, destination, identity));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer count with destination and metadata value by metadata name.
     */
    @Override
    public Collection<List<String>> getTransferCountWithDestinationAndMetadataValueByMetadataName(
            final String metadataName) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getTransferCountWithDestinationAndMetadataValueByMetadataName(" + metadataName + ")");
        return monitor.done(ecpds.getTransferCountWithDestinationAndMetadataValueByMetadataName(metadataName));
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
        final var monitor = new MonitorCall("getDataTransfersByDestinationProductAndTimeOnDate(" + destinationName + ","
                + product + "," + time + "," + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(ecpds.getDataTransfersByDestinationProductAndTimeOnDate(master.dataCache, destinationName,
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
        final var monitor = new MonitorCall(
                "getDataTransfersByDestinationOnDate(" + destinationName + "," + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(
                ecpds.getDataTransfersByDestinationOnDate(master.dataCache, destinationName, fromIsoDate, toIsoDate));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by destination on transmission date.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByDestinationOnTransmissionDate(final String destinationName,
            final Date fromIsoDate, final Date toIsoDate) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDataTransfersByDestinationOnTransmissionDate(" + destinationName + ","
                + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(ecpds.getDataTransfersByDestinationOnTransmissionDate(master.dataCache, destinationName,
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
        final var monitor = new MonitorCall("getBadDataTransfersByDestination(" + destinationName + ")");
        return monitor.done(ecpds.getBadDataTransfersByDestination(master.dataCache, destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bad data transfers by destination count.
     */
    @Override
    public int getBadDataTransfersByDestinationCount(final String destinationName)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getBadDataTransfersByDestinationCount(" + destinationName + ")");
        return monitor.done(ecpds.getBadDataTransfersByDestinationCount(destinationName));
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
        final var monitor = new MonitorCall("getTransferHistoryByDestinationOnProductDate(" + destinationName + ","
                + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(ecpds.getSortedTransferHistoryByDestinationOnProductDate(destinationName, fromIsoDate,
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
        final var monitor = new MonitorCall("getTransferHistoryByDestinationOnHistoryDate(" + destinationName + ","
                + fromIsoDate + "," + toIsoDate + ")");
        return monitor.done(ecpds.getSortedTransferHistoryByDestinationOnHistoryDate(destinationName, fromIsoDate,
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
        final var monitor = new MonitorCall("getAllowedEcUsersByHostName(" + hostName + ")");
        return monitor.done(ecpds.getAllowedEcUsersByHostName(hostName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the traffic by destination name.
     */
    @Override
    public Collection<Traffic> getTrafficByDestinationName(final String destinationName)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTrafficByDestinationName(" + destinationName + ")");
        final var trafficList = ecpds.getTrafficByDestinationName(destinationName);
        return monitor.done(trafficList);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the change log by key.
     */
    @Override
    public Collection<ChangeLog> getChangeLogByKey(final String keyName, final String keyValue)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getChangeLogByKey(" + keyName + "," + keyValue + ")");
        final var changeLogList = ecpds.getChangeLogByKey(keyName, keyValue);
        return monitor.done(changeLogList);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data file.
     */
    @Override
    public DataFile getDataFile(final long dataFileId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDataFile(" + dataFileId + ")");
        return monitor.done(ecpds.getDataFile(dataFileId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer group.
     */
    @Override
    public TransferGroup getTransferGroup(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferGroup(" + name + ")");
        return monitor.done(ecpds.getTransferGroup(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the metadata attribute.
     */
    @Override
    public MetadataAttribute getMetadataAttribute(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getMetadataAttribute(" + name + ")");
        return monitor.done(ecpds.getMetadataAttribute(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the metadata attribute array.
     */
    @Override
    public MetadataAttribute[] getMetadataAttributeArray() throws RemoteException {
        final var monitor = new MonitorCall("getMetadataAttributeArray()");
        return monitor.done(ecpds.getMetadataAttributeArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the cat url array.
     */
    @Override
    public CatUrl[] getCatUrlArray() throws RemoteException {
        final var monitor = new MonitorCall("getCatUrlArray()");
        return monitor.done(ecpds.getCatUrlArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer group array.
     */
    @Override
    public TransferGroup[] getTransferGroupArray() throws RemoteException {
        final var monitor = new MonitorCall("getTransferGroupArray()");
        final List<TransferGroup> groups = new ArrayList<>();
        final var defaultGroup = Cnf.at("Server", "defaultTransferGroup");
        for (final TransferGroup group : ecpds.getTransferGroupArray()) {
            if (defaultGroup != null && defaultGroup.equals(group.getName())) {
                groups.add(0, group);
            } else {
                groups.add(group);
            }
        }
        return monitor.done(groups.toArray(new TransferGroup[groups.size()]));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer server.
     */
    @Override
    public TransferServer getTransferServer(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferServer(" + name + ")");
        final var mover = ecpds.getTransferServer(name);
        mover.setLastUpdate(master.lastUpdateForClientInterface(name, "DataMover"));
        return monitor.done(mover);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer server array.
     */
    @Override
    public TransferServer[] getTransferServerArray() throws RemoteException {
        final var monitor = new MonitorCall("getTransferServerArray()");
        final var movers = ecpds.getTransferServerArray();
        for (final TransferServer mover : movers) {
            mover.setLastUpdate(master.lastUpdateForClientInterface(mover.getName(), "DataMover"));
        }
        return monitor.done(movers);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the EC user.
     */
    @Override
    public ECUser getECUser(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getECUser(" + name + ")");
        return monitor.done(ecpds.getECUser(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming policy.
     */
    @Override
    public IncomingPolicy getIncomingPolicy(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getIncomingPolicy(" + name + ")");
        return monitor.done(ecpds.getIncomingPolicy(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the operation.
     */
    @Override
    public Operation getOperation(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getOperation(" + name + ")");
        return monitor.done(ecpds.getOperation(name));
    }

    /**
     * With incoming connections.
     *
     * @param user
     *            the user
     *
     * @return the incoming user
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private IncomingUser withIncomingConnections(final IncomingUser user) throws DataBaseException {
        user.addConnections(master.getIncomingConnections().get(user.getId()));
        return user;
    }

    /**
     * With incoming connections.
     *
     * @param users
     *            the users
     *
     * @return the incoming user[]
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private IncomingUser[] withIncomingConnections(final IncomingUser[] users) throws DataBaseException {
        final var connections = master.getIncomingConnections();
        for (final IncomingUser incomingUser : users) {
            incomingUser.addConnections(connections.get(incomingUser.getId()));
        }
        return users;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming user.
     */
    @Override
    public IncomingUser getIncomingUser(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getIncomingUser(" + name + ")");
        return monitor.done(withIncomingConnections(ecpds.getIncomingUser(name)));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming user array.
     */
    @Override
    public IncomingUser[] getIncomingUserArray() throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getIncomingUserArray()");
        return monitor.done(withIncomingConnections(ecpds.getIncomingUserArray()));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the operation array.
     */
    @Override
    public Operation[] getOperationArray() throws RemoteException {
        final var monitor = new MonitorCall("getOperationArray()");
        return monitor.done(ecpds.getOperationArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming users for incoming policy.
     */
    @Override
    public IncomingUser[] getIncomingUsersForIncomingPolicy(final String policyId)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getIncomingUsersForIncomingPolicy(" + policyId + ")");
        return monitor.done(ecpds.getIncomingUsersForIncomingPolicy(policyId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming policies for incoming user.
     */
    @Override
    public IncomingPolicy[] getIncomingPoliciesForIncomingUser(final String userId)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getIncomingPoliciesForIncomingUser(" + userId + ")");
        return monitor.done(ecpds.getIncomingPoliciesForIncomingUser(userId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the operations for incoming user.
     */
    @Override
    public Operation[] getOperationsForIncomingUser(final String userId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getOperationsForIncomingUser(" + userId + ")");
        return monitor.done(ecpds.getOperationsForIncomingUser(userId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations for incoming user.
     */
    @Override
    public Destination[] getDestinationsForIncomingUser(final String userId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationsForIncomingUser(" + userId + ")");
        return monitor.done(ecpds.getDestinationsForIncomingUser(userId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations for incoming policy.
     */
    @Override
    public Destination[] getDestinationsForIncomingPolicy(final String policyId)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationsForIncomingPolicy(" + policyId + ")");
        return monitor.done(ecpds.getDestinationsForIncomingPolicy(policyId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination.
     */
    @Override
    public Destination getDestination(final String name, final boolean useCache)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestination(" + name + "," + useCache + ")");
        return monitor.done(ecpds.getDestination(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the EC user array.
     */
    @Override
    public ECUser[] getECUserArray() throws RemoteException {
        final var monitor = new MonitorCall("getECUserArray()");
        return monitor.done(ecpds.getECUserArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming policy array.
     */
    @Override
    public IncomingPolicy[] getIncomingPolicyArray() throws RemoteException {
        final var monitor = new MonitorCall("getIncomingPolicyArray()");
        return monitor.done(ecpds.getIncomingPolicyArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the country.
     */
    @Override
    public Country getCountry(final String iso) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getCountry(" + iso + ")");
        return monitor.done(ecpds.getCountry(iso));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the country array.
     */
    @Override
    public Country[] getCountryArray() throws RemoteException {
        final var monitor = new MonitorCall("getCountryArray()");
        return monitor.done(ecpds.getCountryArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfer.
     */
    @Override
    public DataTransfer getDataTransfer(final long dataTransferId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDataTransfer(" + dataTransferId + ")");
        final var transfer = master.getDataTransferFromCache(dataTransferId);
        return monitor.done(transfer != null ? transfer : ecpds.getDataTransfer(dataTransferId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination array.
     */
    @Override
    public Destination[] getDestinationArray() throws RemoteException {
        final var monitor = new MonitorCall("getDestinationArray()");
        return monitor.done(ecpds.getDestinationArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination array.
     */
    @Override
    public Destination[] getDestinationArray(final boolean monitored) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationArray()");
        return monitor.done(ecpds.getDestinationArray(monitored));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination names and comments.
     */
    @Override
    public Set<Map.Entry<String, String>> getDestinationNamesAndComments() throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationNamesAndComments()");
        return monitor.done(ecpds.getDestinationNamesAndComments());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination EC user.
     */
    @Override
    public DestinationECUser getDestinationECUser(final String destinationName, final String ecuserName)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationECUser(" + destinationName + "," + ecuserName + ")");
        return monitor.done(ecpds.getDestinationECUser(destinationName, ecuserName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the association.
     */
    @Override
    public Association getAssociation(final String destinationName, final String hostName)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getAssociation(" + destinationName + "," + hostName + ")");
        return monitor.done(ecpds.getAssociation(destinationName, hostName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the policy association.
     */
    @Override
    public PolicyAssociation getPolicyAssociation(final String destinationName, final String policyId)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getPolicyAssociation(" + destinationName + "," + policyId + ")");
        return monitor.done(ecpds.getPolicyAssociation(destinationName, policyId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the alias.
     */
    @Override
    public Alias getAlias(final String desName, final String destinationName)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getAlias(" + desName + "," + destinationName + ")");
        return monitor.done(ecpds.getAlias(desName, destinationName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ectrans module.
     */
    @Override
    public ECtransModule getECtransModule(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getECtransModule(" + name + ")");
        return monitor.done(ecpds.getECtransModule(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ectrans module array.
     */
    @Override
    public ECtransModule[] getECtransModuleArray() throws RemoteException {
        final var monitor = new MonitorCall("getECtransModuleArray()");
        return monitor.done(ecpds.getECtransModuleArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host.
     */
    @Override
    public Host getHost(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getHost(" + name + ")");
        return monitor.done(ecpds.getHostWithOutput(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host array.
     */
    @Override
    public Host[] getHostArray() throws RemoteException {
        final var monitor = new MonitorCall("getHostArray()");
        return monitor.done(ecpds.getHostArray(new HostComparator()));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer method.
     */
    @Override
    public TransferMethod getTransferMethod(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferMethod(" + name + ")");
        return monitor.done(ecpds.getTransferMethod(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host EC user.
     */
    @Override
    public HostECUser getHostECUser(final String ecuserName, final String hostName)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getHostECUser(" + ecuserName + "," + hostName + ")");
        return monitor.done(ecpds.getHostECUser(ecuserName, hostName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer history.
     */
    @Override
    public TransferHistory getTransferHistory(final long transferHistoryId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getTransferHistory(" + transferHistoryId + ")");
        return monitor.done(ecpds.getTransferHistory(transferHistoryId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer method array.
     */
    @Override
    public TransferMethod[] getTransferMethodArray() throws RemoteException {
        final var monitor = new MonitorCall("getTransferMethodArray()");
        return monitor.done(ecpds.getTransferMethodArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the category.
     */
    @Override
    public Category getCategory(final long id) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getCategory(" + id + ")");
        return monitor.done(ecpds.getCategory(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the category array.
     */
    @Override
    public Category[] getCategoryArray() throws RemoteException {
        final var monitor = new MonitorCall("getCategoryArray()");
        return monitor.done(ecpds.getCategoryArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the cat url.
     */
    @Override
    public CatUrl getCatUrl(final long categoryId, final String urlName) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getCatUrl(" + categoryId + "," + urlName + ")");
        return monitor.done(ecpds.getCatUrl(categoryId, urlName));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the url array.
     */
    @Override
    public Url[] getUrlArray() throws RemoteException {
        final var monitor = new MonitorCall("getUrlArray()");
        return monitor.done(ecpds.getUrlArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the url.
     */
    @Override
    public Url getUrl(final String name) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getUrl(" + name + ")");
        return monitor.done(ecpds.getUrl(name));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the web user.
     */
    @Override
    public WebUser getWebUser(final String id) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getWebUser(" + id + ")");
        return monitor.done(ecpds.getWebUser(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the web user array.
     */
    @Override
    public WebUser[] getWebUserArray() throws RemoteException {
        final var monitor = new MonitorCall("getWebUserArray()");
        return monitor.done(ecpds.getWebUserArray());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the weu cat.
     */
    @Override
    public WeuCat getWeuCat(final long categoryId, final String webuserId) throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getWeuCat(" + categoryId + "," + webuserId + ")");
        return monitor.done(ecpds.getWeuCat(categoryId, webuserId));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the users per category id.
     */
    @Override
    public Collection<WebUser> getUsersPerCategoryId(final String categoryId)
            throws DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getUsersPerCategoryId(" + categoryId + ")");
        return monitor.done(ecpds.getUsersPerCategoryId(categoryId));
    }

    /**
     * Allow checking if the user is allowed to use the ECPDS API service.
     *
     * @param userNameAndPassword
     *            the user name and password
     * @param service
     *            the service
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private static void checkUser(final String userNameAndPassword, final String service) throws DataBaseException {
        final var credentials = userNameAndPassword.split(":");
        if (service != null && credentials.length == 2) {
            final var permissions = Cnf.at("API", credentials[0], "").split(":");
            if (permissions.length == 2 && permissions[0].equals(credentials[1])) {
                try {
                    if (service.matches(permissions[1])) {
                        return;
                    }
                } catch (final PatternSyntaxException e) {
                    _log.warn("Pattern matching {} -> {}", service, permissions[1], e);
                }
            }
        }
        throw new DataBaseException("User not authorized");
    }

    /**
     * {@inheritDoc}
     *
     * Incoming user del. Deactivate an existing incoming user.
     */
    @Override
    public void incomingUserDel(final String user, final String id) throws DataBaseException, RemoteException {
        checkUser(user, "incomingUserDel");
        final var monitor = new MonitorCall("incomingUserDel(" + user + "," + id + ")");
        final var incomingUser = ecpds.getIncomingUserObject(id);
        if (incomingUser == null) {
            throw new DataBaseException("User " + id + " not found");
        }
        ecpds.removeIncomingUser(incomingUser);
        ecpds.clearIncomingUserCache(id);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming user add. Create or update an incoming user (this is for the Web Services team).
     */
    @Override
    public void incomingUserAdd(final String user, final String id, final String password, final String email,
            final String iso) throws DataBaseException, RemoteException {
        checkUser(user, "incomingUserAdd");
        final var monitor = new MonitorCall(
                "incomingUserAdd(" + user + "," + id + "," + password + "," + email + "," + iso + ")");
        if (iso != null) {
            // Let's check if it is a valid country!
            final var country = ecpds.getCountryObject(iso);
            if (country == null) {
                throw new DataBaseException("Country " + iso + " not found");
            }
        }
        // Let's check if it is a valid user-id?
        if (id == null || id.length() == 0) {
            throw new DataBaseException("Userid " + id + " not valid (empty)");
        }
        if (id.indexOf("-") != -1) {
            throw new DataBaseException("Userid " + id + " not valid (contains '-')");
        }
        var incomingUser = ecpds.getIncomingUserObject(id);
        if (incomingUser == null) {
            // It does not exists yet so we create it with all the default
            // values!
            incomingUser = new IncomingUser();
            // It is not active yet, only the call to add a category can
            // activate a user!
            incomingUser.setActive(false);
            incomingUser.setSynchronized(false);
            incomingUser.setComment(email);
            incomingUser.setData(null);
            incomingUser.setId(id);
            incomingUser.setLastLogin(null);
            incomingUser.setLastLoginHost(null);
            incomingUser.setPassword(password);
            incomingUser.setIso(iso);
            ecpds.insert(incomingUser, false);
            // Now we give him the standard read permissions!
            ecpds.insert(new IncomingPermission(incomingUser, ecpds.getOperation("dir")), false);
            ecpds.insert(new IncomingPermission(incomingUser, ecpds.getOperation("get")), false);
            ecpds.insert(new IncomingPermission(incomingUser, ecpds.getOperation("mtime")), false);
            ecpds.insert(new IncomingPermission(incomingUser, ecpds.getOperation("size")), false);
        } else {
            // We just update the info!
            if (password != null) {
                incomingUser.setPassword(password);
            }
            if (email != null) {
                incomingUser.setComment(email);
            }
            if (iso != null) {
                incomingUser.setIso(iso);
            }
            ecpds.update(incomingUser);
        }
        monitor.done();
    }

    /** Allow generating a temporary password. */
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Random string.
     *
     * @param len
     *            the len
     *
     * @return the string
     */
    private static String randomString(final int len) {
        final var sb = new StringBuilder(len);
        for (var i = 0; i < len; i++) {
            sb.append(AB.charAt(ThreadLocalRandom.current().nextInt(AB.length())));
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming user add 2. Create or update an incoming user (this is for the CAMS Production team).
     */
    @Override
    public String incomingUserAdd2(final String user, final String id, final String email, final String iso)
            throws DataBaseException, RemoteException {
        checkUser(user, "incomingUserAdd2");
        final var monitor = new MonitorCall("incomingUserAdd2(" + user + "," + id + "," + email + "," + iso + ")");
        // Let's check if it is a valid country!
        final var country = ecpds.getCountryObject(iso);
        if (country == null) {
            throw new DataBaseException("Country " + iso + " not found");
        }
        // Let's check if it is a valid user-id?
        if (!Format.isValidId(id, "_.")) {
            throw new DataBaseException("Userid " + id + " not valid (only letters, digits, '_' and '.' are allowed)");
        }
        // We generate a password!
        final var password = randomString(8);
        var incomingUser = ecpds.getIncomingUserObject(id);
        if (incomingUser != null) {
            // The id is already registered!
            throw incomingUser.getActive() ? new DataBaseException("Id " + id + " already exists (active)")
                    : new DataBaseException("Id " + id + " aready exists (inactive)");
        }
        // It does not exists yet so we create it!
        incomingUser = new IncomingUser();
        incomingUser.setActive(true);
        incomingUser.setSynchronized(false);
        incomingUser.setComment(email);
        incomingUser.setData(null);
        incomingUser.setId(id);
        incomingUser.setLastLogin(null);
        incomingUser.setLastLoginHost(null);
        incomingUser.setPassword(password);
        incomingUser.setIso(iso);
        ecpds.insert(incomingUser, false);
        // Now we give him the standard read permissions!
        ecpds.insert(new IncomingPermission(incomingUser, ecpds.getOperation("dir")), false);
        ecpds.insert(new IncomingPermission(incomingUser, ecpds.getOperation("get")), false);
        ecpds.insert(new IncomingPermission(incomingUser, ecpds.getOperation("mtime")), false);
        ecpds.insert(new IncomingPermission(incomingUser, ecpds.getOperation("size")), false);
        return monitor.done(password);
    }

    /**
     * {@inheritDoc}
     *
     * Incoming user list. List all active incoming users.
     */
    @Override
    public Collection<IncomingUser> incomingUserList(final String user, final String destination)
            throws DataBaseException, RemoteException {
        checkUser(user, "incomingUserList");
        final var monitor = new MonitorCall("incomingUserList(" + user + "," + destination + ")");
        final List<IncomingUser> result = new ArrayList<>();
        if (isNotEmpty(destination)) {
            // Let's check if it is a valid Destination!
            if (ecpds.getDestinationObject(destination) == null) {
                throw new DataBaseException("Destination " + destination + " not found");
            }
            // We select the incoming users by destination!
            for (final IncomingAssociation association : ecpds.getIncomingAssociationArray()) {
                if (association.getDestinationName().equals(destination)) {
                    final var incomingUser = association.getIncomingUser();
                    if (incomingUser != null && incomingUser.getActive()) {
                        result.add(incomingUser);
                    }
                }
            }
        } else {
            // We take all the incoming users!
            for (final IncomingUser incomingUser : withIncomingConnections(ecpds.getIncomingUserArray())) {
                if (incomingUser != null && incomingUser.getActive()) {
                    result.add(incomingUser);
                }
            }
        }
        return monitor.done(result);
    }

    /**
     * {@inheritDoc}
     *
     * Incoming category add.
     *
     * Activate a user if it is associated with an ECPDS category. If there is no ECPDS category then the user is
     * deactivated.
     */
    @Override
    public void incomingCategoryAdd(final String user, final String id, final List<String> categories)
            throws DataBaseException, RemoteException {
        checkUser(user, "incomingCategoryAdd");
        final var monitor = new MonitorCall("incomingCategoryAdd(" + user + "," + id + "," + categories.size() + ")");
        final var incomingUser = ecpds.getIncomingUserObject(id);
        if (incomingUser == null) {
            throw new DataBaseException("User " + id + " not found");
        }
        // Let's assume it will not be allowed to use ecpds!
        incomingUser.setActive(false);
        for (final String category : categories) {
            if (category != null && category.toUpperCase().indexOf("ECPDS") != -1) {
                incomingUser.setActive(true);
                break;
            }
        }
        // Let's update the user!
        ecpds.update(incomingUser);
        ecpds.clearIncomingUserCache(id);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming association add. Create a new incoming association.
     */
    @Override
    public void incomingAssociationAdd(final String user, final String id, final String destination)
            throws DataBaseException, RemoteException {
        checkUser(user, "incomingAssociationAdd");
        final var monitor = new MonitorCall("incomingAssociationAdd(" + user + "," + id + "," + destination + ")");
        final var incomingUser = ecpds.getIncomingUserObject(id);
        if (incomingUser == null || !incomingUser.getActive()) {
            throw new DataBaseException("User " + id + " not found/active");
        }
        final var targetDestination = ecpds.getDestinationObject(destination);
        if (targetDestination == null) {
            throw new DataBaseException("Destination " + destination + " not found");
        }
        final var association = new IncomingAssociation();
        association.setDestinationName(destination);
        association.setDestination(targetDestination);
        association.setIncomingUser(incomingUser);
        association.setIncomingUserId(id);
        ecpds.insert(association, false);
        ecpds.clearIncomingUserCache(id);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming association del. Delete an incoming association.
     */
    @Override
    public void incomingAssociationDel(final String user, final String id, final String destination)
            throws DataBaseException, RemoteException {
        checkUser(user, "incomingAssociationDel");
        final var monitor = new MonitorCall("incomingAssociationDel(" + user + "," + id + "," + destination + ")");
        final var incomingUser = ecpds.getIncomingUserObject(id);
        if (incomingUser == null || !incomingUser.getActive()) {
            throw new DataBaseException("User " + id + " not found/active");
        }
        final var targetDestination = ecpds.getDestinationObject(destination);
        if (targetDestination == null) {
            throw new DataBaseException("Destination " + destination + " not found");
        }
        final var association = new IncomingAssociation();
        association.setDestinationName(destination);
        association.setDestination(targetDestination);
        association.setIncomingUser(incomingUser);
        association.setIncomingUserId(id);
        ecpds.remove(association);
        ecpds.clearIncomingUserCache(id);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Incoming association list. List incoming associations for a user.
     */
    @Override
    public String[] incomingAssociationList(final String user, final String id)
            throws DataBaseException, RemoteException {
        checkUser(user, "incomingAssociationList");
        final var monitor = new MonitorCall("incomingAssociationList(" + user + "," + id + ")");
        final var incomingUser = ecpds.getIncomingUserObject(id);
        if (incomingUser == null || !incomingUser.getActive()) {
            throw new DataBaseException("User " + id + " not found/active");
        }
        final List<String> result = new ArrayList<>();
        for (final Destination destination : ecpds.getDestinationsForIncomingUser(id)) {
            result.add(destination.getName());
        }
        return monitor.done(result.toArray(new String[result.size()]));
    }

    /**
     * {@inheritDoc}
     *
     * Destination backup list.
     */
    @Override
    public DestinationBackup getDestinationBackup(final String user, final String id, final String iso,
            final Integer type, final String name) throws DataBaseException {
        checkUser(user, "destinationBackupList");
        final var monitor = new MonitorCall(
                "destinationBackupList(" + user + "," + id + "," + iso + "," + type + "," + name + ")");
        if (isNotEmpty(name)) {
            final var destination = ecpds.getDestinationObject(name);
            if (destination == null) {
                throw new DataBaseException("Destination " + name + " not found");
            }
        }
        if (isNotEmpty(iso)) {
            // Let's check if it is a valid country!
            final var country = ecpds.getCountryObject(iso);
            if (country == null) {
                throw new DataBaseException("Country " + iso + " not found");
            }
        }
        final var filterByWebUser = isNotEmpty(id);
        if (filterByWebUser) {
            // Let's check if it is a valid web user!
            final var webUser = ecpds.getWebUserObject(id);
            if (webUser == null) {
                throw new DataBaseException("Web User " + id + " not found");
            }
        }
        // Let's check if it is a valid type of Destination!
        if (type != null && !DestinationOption.exists(type)) {
            throw new DataBaseException("Type " + type + " not found");
        }
        final List<String> allowed = new ArrayList<>();
        if (filterByWebUser) {
            allowed.addAll(ecpds.getAuthorisedDestinations(id));
        }
        final var backup = new DestinationBackup();
        final var associationsArray = ecpds.getAssociationArray();
        final var aliasArray = ecpds.getAliasArray();
        for (final Destination destination : ecpds.getDestinationArray()) {
            final var destinationName = destination.getName();
            var selected = true;
            if (isNotEmpty(name)) {
                selected = selected && destinationName.equals(name);
            }
            if (isNotEmpty(iso)) {
                selected = selected && destination.getCountryIso().equalsIgnoreCase(iso);
            }
            if (type != null) {
                selected = selected && destination.getType() == type;
            }
            if (filterByWebUser) {
                selected = selected && allowed.contains(destinationName);
            }
            if (selected) {
                addToDestinationBackup(backup, destination, associationsArray, aliasArray);
            }
        }
        return monitor.done(backup);
    }

    /**
     * Add associations to a backup destination. Only select the associations and aliases which are linked with the
     * Destination!
     *
     * @param backup
     *            the backup
     * @param destination
     *            the destination
     * @param associationsArray
     *            the associations array
     * @param aliasArray
     *            the alias array
     */
    private static void addToDestinationBackup(final DestinationBackup backup, final Destination destination,
            final Association[] associationsArray, final Alias[] aliasArray) {
        final List<Association> associations = new ArrayList<>();
        for (final Association association : associationsArray) {
            if (destination.getName().equals(association.getDestinationName())) {
                associations.add(association);
            }
        }
        final List<Alias> aliases = new ArrayList<>();
        for (final Alias alias : aliasArray) {
            if (destination.getName().equals(alias.getDesName())) {
                aliases.add(alias);
            }
        }
        backup.add(destination, associations, aliases);
    }

    /**
     * {@inheritDoc}
     *
     * Import the provided backup.
     */
    @Override
    public int putDestinationBackup(final String user, final DestinationBackup backup, final boolean copySharedHost)
            throws DataBaseException, MasterException {
        checkUser(user, "putDestinationBackup");
        final var monitor = new MonitorCall("putDestinationBackup(" + user + ")");
        // Make sure all the required ECusers are in the database
        for (final ECUser ecuser : backup.getECUsers()) {
            if (ecpds.getECUserObject(ecuser.getName()) == null) {
                ecpds.insert(ecuser, false);
            }
        }
        // Make sure all the required transfer methods and modules are in the database
        for (final TransferMethod method : backup.getMethods()) {
            final var module = method.getECtransModule();
            if (ecpds.getECtransModuleObject(module.getName()) == null) {
                ecpds.insert(module, false);
            }
            if (ecpds.getTransferMethodObject(method.getName()) == null) {
                ecpds.insert(method, false);
            }
        }
        var destinationCount = 0;
        // Now we can create the destinations!
        for (final DestinationList list : backup.getDestinations()) {
            final var destination = list.getDestination();
            final var destinationName = destination.getName();
            if (ecpds.getDestinationObject(destinationName) == null) {
                master.importDestination(list.getDestination(), list.getAssociations().toArray(new Association[0]),
                        copySharedHost);
                destinationCount++;
            }
        }
        // And the aliases if both destinations exists ...
        for (final Alias alias : backup.getAliases()) {
            if (ecpds.getAliasObject(alias.getDesName(), alias.getDestinationName()) == null
                    && ecpds.getDestinationObject(alias.getDesName()) != null
                    && ecpds.getDestinationObject(alias.getDestinationName()) != null) {
                ecpds.insert(alias, false);
            }
        }
        return monitor.done(destinationCount);
    }

    /**
     * {@inheritDoc}
     *
     * Destination list.
     */
    @Override
    public Collection<Destination> destinationList(final String user, final String id, final String iso,
            final Integer type) throws DataBaseException, RemoteException {
        checkUser(user, "destinationList");
        final var monitor = new MonitorCall("destinationList(" + user + "," + id + "," + iso + "," + type + ")");
        if (isNotEmpty(iso)) {
            // Let's check if it is a valid country!
            final var country = ecpds.getCountryObject(iso);
            if (country == null) {
                throw new DataBaseException("Country " + iso + " not found");
            }
        }
        final var filterByWebUser = isNotEmpty(id);
        if (filterByWebUser) {
            // Let's check if it is a valid web user!
            final var webUser = ecpds.getWebUserObject(id);
            if (webUser == null) {
                throw new DataBaseException("Web User " + id + " not found");
            }
        }
        // Let's check if it is a valid type of Destination!
        if (type != null && !DestinationOption.exists(type)) {
            throw new DataBaseException("Type " + type + " not found");
        }
        final List<String> allowed = new ArrayList<>();
        if (filterByWebUser) {
            allowed.addAll(ecpds.getAuthorisedDestinations(id));
        }
        final List<Destination> result = new ArrayList<>();
        for (final Destination destination : ecpds.getDestinationArray()) {
            var selected = true;
            if (isNotEmpty(iso)) {
                selected = selected && destination.getCountryIso().equalsIgnoreCase(iso);
            }
            if (type != null) {
                selected = selected && destination.getType() == type;
            }
            if (filterByWebUser) {
                selected = selected && allowed.contains(destination.getName());
            }
            if (selected) {
                result.add(destination);
            }
        }
        return monitor.done(result);
    }

    /**
     * {@inheritDoc}
     *
     * List Destinations Countries.
     */
    @Override
    public Collection<Country> destinationCountryList(final String user) throws DataBaseException, RemoteException {
        checkUser(user, "destinationCountryList");
        final var monitor = new MonitorCall("destinationCountryList(" + user + ")");
        final List<Country> result = new ArrayList<>();
        for (final Destination destination : ecpds.getDestinationArray()) {
            final var country = destination.getCountry();
            if (!result.contains(country)) {
                result.add(country);
            }
        }
        return monitor.done(result);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination option list.
     */
    @Override
    public List<TypeEntry> getDestinationOptionList() throws RemoteException {
        final var monitor = new MonitorCall("getDestinationOptionList()");
        return monitor.done(DestinationOption.getList());
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
        checkUser(user, "datafilePut");
        final var monitor = new MonitorCall(
                "datafilePut(" + user + "," + destination + "," + metadata + "," + source + "," + uniqueName + ","
                        + target + "," + priority + "," + lifeTime + "," + standby + "," + force + ")");
        try {
            final var id = ECpdsClient.put("From the REST interface at " + remoteHost, user,
                    master.getDataBaseInterface().getDestination(destination, false), null, metadata, source,
                    uniqueName, target != null ? target : new File(source).getName(), System.currentTimeMillis(), -1,
                    false, priority != null ? priority : 99, lifeTime, at, standby != null && standby, "REST", false,
                    force != null && force, false, false, null);
            return monitor.done(id);
        } catch (final IOException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * Datafile size.
     */
    @Override
    public long datafileSize(final String user, final Long dataFileId) throws DataBaseException, RemoteException {
        checkUser(user, "datafileSize");
        final var monitor = new MonitorCall("datafileSize(" + user + "," + dataFileId + ")");
        return monitor.done(ecpds.getDataFile(dataFileId).getSize());
    }

    /**
     * {@inheritDoc}
     *
     * Datafile del.
     */
    @Override
    public void datafileDel(final String user, final Long dataFileId)
            throws MasterException, DataBaseException, RemoteException {
        checkUser(user, "datafileDel");
        final var monitor = new MonitorCall("datafileDel(" + user + "," + dataFileId + ")");
        final var file = ecpds.getDataFile(dataFileId);
        if (file.getDeleted()) {
            throw new DataBaseException("Already deleted");
        }
        master.removeDataFileAndDataTransfers(file, user, "by DataUser=" + user + " from the REST interface");
        monitor.done();
    }
}
