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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.SessionCache;
import ecmwf.common.text.DateUtil;

/**
 * The Class ECpdsBase.
 */
public final class ECpdsBase extends DataBase {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECpdsBase.class);

    /** The Constant CACHE_TIMEOUT. */
    private static final long CACHE_TIMEOUT = Cnf.durationAt("ECpdsBase", "cacheTimeout", Timer.ONE_MINUTE);

    /** The Constant CACHE_PAUSE. */
    private static final long CACHE_PAUSE = Cnf.durationAt("ECpdsBase", "cachePause", 5 * Timer.ONE_SECOND);

    /** The operationsCache. */
    private final SessionCache<String, List<Operation>> operationsCache = new SessionCache<>("operations", CACHE_PAUSE);

    /** The destinationsCache. */
    private final SessionCache<String, List<Destination>> destinationsCache = new SessionCache<>("destinations",
            CACHE_PAUSE);

    /** The policyUserCache. */
    private final SessionCache<String, List<PolicyUser>> policyUserCache = new SessionCache<>("policyUsers",
            CACHE_PAUSE);

    /** The transferServersCache. */
    private final SessionCache<String, List<TransferServer>> transferServersCache = new SessionCache<>(
            "transferServers", CACHE_PAUSE);

    /** The incomingPermissionCache. */
    private final SessionCache<String, List<IncomingPermission>> incomingPermissionCache = new SessionCache<>(
            "incomingPermissions", CACHE_PAUSE);

    /** The ecpds. */
    private final ECpdsGet ecpds = new ECpdsGet(this);

    /**
     * Allow clearing the cache for an IncomingUser.
     *
     * @param userId
     *            user identifier
     */
    public void clearIncomingUserCache(final String userId) {
        operationsCache.delete(userId);
        destinationsCache.delete("BUP$" + userId);
        destinationsCache.delete("FIU$" + userId);
        policyUserCache.delete(userId);
        incomingPermissionCache.delete(userId);
    }

    /**
     * Allow clearing the cache for an TransferGroup.
     *
     * @param groupName
     *            the group name
     */
    public void clearTransferServerCache(final String groupName) {
        transferServersCache.delete(groupName);
    }

    /**
     * Gets the transfer servers.
     *
     * @param groupName
     *            the group name
     *
     * @return the transfer servers
     */
    public TransferServer[] getTransferServers(final String groupName) {
        final List<TransferServer> list;
        final var mutex = transferServersCache.getMutex(groupName);
        synchronized (mutex.lock()) {
            try {
                list = transferServersCache.computeIfAbsent(groupName, k -> {
                    DBIterator<TransferServer> it = null;
                    final List<TransferServer> defaultList = new ArrayList<>();
                    try {
                        it = ecpds.getTransferServers(k, TransferServer.class);
                        while (it.hasNext()) {
                            defaultList.add(it.next());
                        }
                    } catch (SQLException | IOException e) {
                        _log.warn("getTransferServers", e);
                    } finally {
                        if (it != null) {
                            it.remove();
                        }
                    }
                    transferServersCache.put(k, defaultList, CACHE_TIMEOUT);
                    return defaultList;
                });
            } finally {
                mutex.free();
            }
        }
        logSqlRequest("getTransferServers", list.size());
        return list.toArray(new TransferServer[list.size()]);
    }

    /**
     * Gets the authorised destinations.
     *
     * @param user
     *            the user
     *
     * @return the authorised destinations
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public List<String> getAuthorisedDestinations(final String user) throws DataBaseException {
        final List<String> destinationList = new ArrayList<>();
        try (var rs = ecpds.getAuthorizedDestinations(user)) {
            while (rs.next()) {
                destinationList.add(rs.getString("DES_NAME"));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getAuthorisedDestinations", e);
            throw new DataBaseException("getAuthorisedDestinations", e);
        }
        logSqlRequest("getAuthorisedDestinations", destinationList.size());
        return destinationList;
    }

    /**
     * Gets the authorised hosts.
     *
     * @param user
     *            the user
     *
     * @return the authorised hosts
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public List<String> getAuthorisedHosts(final String user) throws DataBaseException {
        final List<String> hostList = new ArrayList<>();
        try (var rs = ecpds.getAuthorizedHosts(user)) {
            while (rs.next()) {
                hostList.add(rs.getString("HOS_NAME"));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getAuthorisedHosts", e);
            throw new DataBaseException("getAuthorisedHosts", e);
        }
        logSqlRequest("getAuthorisedHosts", hostList.size());
        return hostList;
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Statistics[] getStatistics(final Date fromDate, final Date toDate, final String groupName,
            final String status, final String type) throws DataBaseException {
        final List<Statistics> list = new ArrayList<>();
        try (var rs = ecpds.getStatistics(new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()), groupName,
                status, type)) {
            while (rs.next()) {
                final var stat = new Statistics();
                stat.setDate(rs.getTimestamp("DATE"));
                stat.setDestination(rs.getInt("DESTINATION"));
                stat.setSize(rs.getLong("SIZE"));
                list.add(stat);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getStatistics", e);
            throw new DataBaseException("getStatistics", e);
        }
        logSqlRequest("getStatistics", list.size());
        return list.toArray(new Statistics[list.size()]);
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Rates[] getRates(final Date fromDate, final Date toDate, final String caller, final String sourceHost)
            throws DataBaseException {
        final List<Rates> list = new ArrayList<>();
        try (var rs = ecpds.getRates(new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()), caller,
                sourceHost)) {
            while (rs.next()) {
                final var rates = new Rates();
                rates.setDate(rs.getString("DATE"));
                rates.setTransferGroupName(rs.getString("TRG_NAME"));
                rates.setCount(rs.getLong("COUNT"));
                rates.setSize(rs.getLong("SIZE"));
                rates.setGetDuration(rs.getLong("DURATION"));
                list.add(rates);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getRates", e);
            throw new DataBaseException("getRates", e);
        }
        logSqlRequest("getRates", list.size());
        return list.toArray(new Rates[list.size()]);
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Rates[] getRatesPerTransferServer(final Date fromDate, final Date toDate, final String caller,
            final String sourceHost) throws DataBaseException {
        final List<Rates> list = new ArrayList<>();
        try (var rs = ecpds.getRatesPerTransferServer(new Timestamp(fromDate.getTime()),
                new Timestamp(toDate.getTime()), caller, sourceHost)) {
            while (rs.next()) {
                final var rates = new Rates();
                rates.setDate(rs.getString("DATE"));
                rates.setTransferGroupName(rs.getString("TRG_NAME"));
                rates.setGetHost(rs.getString("DAF_GET_HOST"));
                rates.setCount(rs.getLong("COUNT"));
                rates.setSize(rs.getLong("SIZE"));
                rates.setGetDuration(rs.getLong("DURATION"));
                list.add(rates);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getRatesPerTransferServer", e);
            throw new DataBaseException("getRatesPerTransferServer", e);
        }
        logSqlRequest("getRatesPerTransferServer", list.size());
        return list.toArray(new Rates[list.size()]);
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Rates[] getRatesPerFileSystem(final Date fromDate, final Date toDate, final String transferServerName,
            final String caller, final String sourceHost) throws DataBaseException {
        final List<Rates> list = new ArrayList<>();
        try (var rs = ecpds.getRatesPerFileSystem(new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()),
                transferServerName, caller, sourceHost)) {
            while (rs.next()) {
                final var rates = new Rates();
                rates.setDate(rs.getString("DATE"));
                rates.setTransferGroupName(rs.getString("TRG_NAME"));
                rates.setGetHost(rs.getString("DAF_GET_HOST"));
                rates.setFileSystem(rs.getInteger("DAF_FILE_SYSTEM"));
                rates.setCount(rs.getLong("COUNT"));
                rates.setSize(rs.getLong("SIZE"));
                rates.setGetDuration(rs.getLong("DURATION"));
                list.add(rates);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getRatesPerFileSystem", e);
            throw new DataBaseException("getRatesPerFileSystem", e);
        }
        logSqlRequest("getRatesPerFileSystem", list.size());
        return list.toArray(new Rates[list.size()]);
    }

    /**
     * Gets the transfer servers by data file id.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the transfer servers by data file id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferServer[] getTransferServersByDataFileId(final long dataFileId) throws DataBaseException {
        final List<TransferServer> list = new ArrayList<>();
        DBIterator<TransferServer> it = null;
        try {
            it = ecpds.getTransferServersByDataFileId(dataFileId, TransferServer.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getTransferServersByDataFileId", e);
            throw new DataBaseException("getTransferServersByDataFileId", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getTransferServersByDataFileId", list.size());
        return list.toArray(new TransferServer[list.size()]);
    }

    /**
     * Gets the EC user count.
     *
     * @return the EC user count
     */
    public int getECUserCount() {
        try {
            return ecpds.getECUserCount();
        } catch (SQLException | IOException e) {
            _log.warn("getECUserCount", e);
        }
        return -1;
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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Map<String, List<Host>> getDestinationsAndHostsForType(final String type, final int paramLimit)
            throws DataBaseException {
        final var result = new HashMap<String, List<Host>>();
        DBResultSet rs = null;
        List<Host> array = new ArrayList<>();
        String current = null;
        try {
            rs = ecpds.getDestinationsAndHostsForType(type, paramLimit);
            while (rs.next()) {
                final var desName = rs.getString("DES_NAME");
                if (current == null || !current.equals(desName)) {
                    result.put(current = desName, array = new ArrayList<>());
                }
                final var host = new Host();
                host.setType(rs.getString("HOS_TYPE"));
                host.setData(rs.getString("HOS_DATA"));
                host.setDir(rs.getString("HOS_DIR"));
                host.setHost(rs.getString("HOS_HOST"));
                host.setLogin(rs.getString("HOS_LOGIN"));
                host.setName(rs.getString("HOS_NAME"));
                host.setNetworkCode(rs.getString("HOS_NETWORK_CODE"));
                host.setNetworkName(rs.getString("HOS_NETWORK_NAME"));
                host.setNickname(rs.getString("HOS_NICKNAME"));
                host.setPasswd(rs.getString("HOS_PASSWD"));
                host.setTransferGroupName(rs.getString("TRG_NAME"));
                array.add(host);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationsAndHostsForType", e);
            throw new DataBaseException("getDestinationsAndHostsForType", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        logSqlRequest("getDestinationsAndHostsForType", result.size());
        return result;
    }

    /**
     * Gets the destination host.
     *
     * @param dest
     *            the dest
     * @param type
     *            the type
     *
     * @return the destination host
     */
    public Host[] getDestinationHost(final Destination dest, final String type) {
        return getDestinationHost(dest.getName(), type);
    }

    /**
     * Gets the destination host.
     *
     * @param dest
     *            the dest
     * @param type
     *            the type
     *
     * @return the destination host
     */
    public Host[] getDestinationHost(final String dest, final String type) {
        final List<Host> list = new ArrayList<>();
        DBIterator<Host> it = null;
        try {
            it = ecpds.getDestinationHost(dest, Host.class);
            while (it.hasNext()) {
                final var host = it.next();
                if (host.getActive() && (type == null || type.equals(host.getType()))) {
                    list.add(host);
                }
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationHost", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationHost", list.size());
        return list.toArray(new Host[list.size()]);
    }

    /**
     * Gets the destination ecuser.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination ecuser
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECUser[] getDestinationEcuser(final String destinationName) throws DataBaseException {
        final List<ECUser> list = new ArrayList<>();
        DBIterator<ECUser> it = null;
        try {
            it = ecpds.getDestinationEcuser(destinationName, ECUser.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationEcuser", e);
            throw new DataBaseException("getDestinationEcuser", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationEcuser", list.size());
        return list.toArray(new ECUser[list.size()]);
    }

    /**
     * Gets the destination incoming policies.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination incoming policies
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public IncomingPolicy[] getDestinationIncomingPolicy(final String destinationName) throws DataBaseException {
        final List<IncomingPolicy> list = new ArrayList<>();
        DBIterator<IncomingPolicy> it = null;
        try {
            it = ecpds.getDestinationIncomingPolicy(destinationName, IncomingPolicy.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationIncomingPolicy", e);
            throw new DataBaseException("getDestinationIncomingPolicy", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationIncomingPolicy", list.size());
        return list.toArray(new IncomingPolicy[list.size()]);
    }

    /**
     * Gets the incoming users for an incoming policy.
     *
     * @param policyId
     *            the policy id
     *
     * @return the incoming users
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public IncomingUser[] getIncomingUsersForIncomingPolicy(final String policyId) throws DataBaseException {
        final List<IncomingUser> list = new ArrayList<>();
        DBIterator<IncomingUser> it = null;
        try {
            it = ecpds.getIncomingUsersForIncomingPolicy(policyId, IncomingUser.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getIncomingUsersForIncomingPolicy", e);
            throw new DataBaseException("getIncomingUsersForIncomingPolicy", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getIncomingUsersForIncomingPolicy", list.size());
        return list.toArray(new IncomingUser[list.size()]);
    }

    /**
     * Gets the incoming policies for an incoming user.
     *
     * @param userId
     *            the user id
     *
     * @return the incoming policies
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public IncomingPolicy[] getIncomingPoliciesForIncomingUser(final String userId) throws DataBaseException {
        final List<IncomingPolicy> list = new ArrayList<>();
        DBIterator<IncomingPolicy> it = null;
        try {
            it = ecpds.getIncomingPoliciesForIncomingUser(userId, IncomingPolicy.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getIncomingPoliciesForIncomingUser", e);
            throw new DataBaseException("getIncomingPoliciesForIncomingUser", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getIncomingPoliciesForIncomingUser", list.size());
        return list.toArray(new IncomingPolicy[list.size()]);
    }

    /**
     * Gets the operations for an incoming user.
     *
     * @param userId
     *            the user id
     *
     * @return the operations
     */
    public Operation[] getOperationsForIncomingUser(final String userId) {
        final List<Operation> list;
        final var mutex = operationsCache.getMutex(userId);
        synchronized (mutex.lock()) {
            try {
                list = operationsCache.computeIfAbsent(userId, k -> {
                    DBIterator<IncomingPermission> it = null;
                    final List<Operation> defaultList = new ArrayList<>();
                    try {
                        it = ecpds.getIncomingPermissionsForIncomingUser(k, IncomingPermission.class);
                        while (it.hasNext()) {
                            defaultList.add(it.next().getOperation());
                        }
                    } catch (SQLException | IOException e) {
                        _log.warn("getOperationsForIncomingUser", e);
                    } finally {
                        if (it != null) {
                            it.remove();
                        }
                    }
                    operationsCache.put(k, defaultList, CACHE_TIMEOUT);
                    return defaultList;
                });
            } finally {
                mutex.free();
            }
        }
        logSqlRequest("getOperationsForIncomingUser", list.size());
        return list.toArray(new Operation[list.size()]);
    }

    /**
     * Gets the destinations for an incoming policy.
     *
     * @param policyId
     *            the policy id
     *
     * @return the destinations
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Destination[] getDestinationsForIncomingPolicy(final String policyId) throws DataBaseException {
        final List<Destination> list = new ArrayList<>();
        DBIterator<Destination> it = null;
        try {
            it = ecpds.getDestinationsForIncomingPolicy(policyId, Destination.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationsForIncomingPolicy", e);
            throw new DataBaseException("getDestinationsForIncomingPolicy", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationsForIncomingPolicy", list.size());
        return list.toArray(new Destination[list.size()]);
    }

    /**
     * Gets the destinations.
     *
     * @param name
     *            the name
     *
     * @return the destinations
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Destination[] getDestinations(final String name) throws DataBaseException {
        final List<Destination> list = new ArrayList<>();
        DBIterator<Destination> it = null;
        try {
            it = ecpds.getDestinations(name, Destination.class);
            while (it.hasNext()) {
                final var destination = it.next();
                if (name != null && name.equals(destination.getName())) {
                    // Let's put the initial Destination at the top of the list!
                    list.add(0, destination);
                } else {
                    list.add(destination);
                }
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinations", e);
            throw new DataBaseException("getDestinations", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinations", list.size());
        return list.toArray(new Destination[list.size()]);
    }

    /**
     * Gets the destinations.
     *
     * @param monitored
     *            only monitored destinations?
     *
     * @return the destinations
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Destination[] getDestinationArray(final boolean monitored) throws DataBaseException {
        final List<Destination> list = new ArrayList<>();
        DBIterator<Destination> it = null;
        try {
            it = ecpds.getDestinationArray(monitored, Destination.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationArray", e);
            throw new DataBaseException("getDestinationArray", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationArray", list.size());
        return list.toArray(new Destination[list.size()]);
    }

    /**
     * Gets the destinations for incoming user.
     *
     * @param userId
     *            the uid
     *
     * @return the destinations for incoming user
     */
    public Destination[] getDestinationsForIncomingUser(final String userId) {
        final var key = "FIU$" + userId;
        final List<Destination> list;
        final var mutex = destinationsCache.getMutex(key);
        synchronized (mutex.lock()) {
            try {
                list = destinationsCache.computeIfAbsent(key, k -> {
                    DBIterator<Destination> it = null;
                    final List<Destination> defaultList = new ArrayList<>();
                    try {
                        it = ecpds.getDestinationsForIncomingUser(userId, Destination.class);
                        while (it.hasNext()) {
                            defaultList.add(it.next());
                        }
                    } catch (SQLException | IOException e) {
                        _log.warn("getDestinationsForIncomingUser", e);
                    } finally {
                        if (it != null) {
                            it.remove();
                        }
                    }
                    destinationsCache.put(k, defaultList, CACHE_TIMEOUT);
                    return defaultList;
                });
            } finally {
                mutex.free();
            }
        }
        logSqlRequest("getDestinationsForIncomingUser", list.size());
        return list.toArray(new Destination[list.size()]);
    }

    /**
     * Gets the destinations by user policies.
     *
     * @param userId
     *            the user id
     *
     * @return the destinations by user policies
     */
    public Destination[] getDestinationsByUserPolicies(final String userId) {
        final var key = "BUP$" + userId;
        final List<Destination> list;
        final var mutex = destinationsCache.getMutex(key);
        synchronized (mutex.lock()) {
            try {
                list = destinationsCache.computeIfAbsent(key, k -> {
                    DBIterator<Destination> it = null;
                    final List<Destination> defaultList = new ArrayList<>();
                    try {
                        it = ecpds.getDestinationsByUserPolicies(userId, Destination.class);
                        while (it.hasNext()) {
                            defaultList.add(it.next());
                        }
                    } catch (SQLException | IOException e) {
                        _log.warn("getDestinationsByUserPolicies", e);
                    } finally {
                        if (it != null) {
                            it.remove();
                        }
                    }
                    destinationsCache.put(k, defaultList, CACHE_TIMEOUT);
                    return defaultList;
                });
            } finally {
                mutex.free();
            }
        }
        logSqlRequest("getDestinationsByUserPolicies", list.size());
        return list.toArray(new Destination[list.size()]);
    }

    /**
     * Gets the incoming permissions for incoming user.
     *
     * @param userId
     *            the user id
     *
     * @return the incoming permissions for incoming user
     */
    public List<IncomingPermission> getIncomingPermissionsForIncomingUser(final String userId) {
        final List<IncomingPermission> list;
        final var mutex = incomingPermissionCache.getMutex(userId);
        synchronized (mutex.lock()) {
            try {
                list = incomingPermissionCache.computeIfAbsent(userId, k -> {
                    DBIterator<IncomingPermission> it = null;
                    final List<IncomingPermission> defaultList = new ArrayList<>();
                    try {
                        it = ecpds.getIncomingPermissionsForIncomingUser(k, IncomingPermission.class);
                        while (it.hasNext()) {
                            defaultList.add(it.next());
                        }
                    } catch (SQLException | IOException e) {
                        _log.warn("getIncomingPermissionsForIncomingUser", e);
                    } finally {
                        if (it != null) {
                            it.remove();
                        }
                    }
                    incomingPermissionCache.put(k, defaultList, CACHE_TIMEOUT);
                    return defaultList;
                });
            } finally {
                mutex.free();
            }
        }
        logSqlRequest("getIncomingPermissionsForIncomingUser", list.size());
        return list;
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Destination[] getDestinationAliases(final String name, final String mode) throws DataBaseException {
        final List<Destination> list = new ArrayList<>();
        DBIterator<Destination> it = null;
        try {
            it = ecpds.getDestinationAliases(name, mode, Destination.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationAliases", e);
            throw new DataBaseException("getDestinationAliases", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationAliases", list.size());
        return list.toArray(new Destination[list.size()]);
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Alias[] getAliases(final String name, final String mode) throws DataBaseException {
        final List<Alias> list = new ArrayList<>();
        DBIterator<Alias> it = null;
        try {
            it = ecpds.getDestinationAliases(name, mode, Alias.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getAliases", e);
            throw new DataBaseException("getAliases", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getAliases", list.size());
        return list.toArray(new Alias[list.size()]);
    }

    /**
     * Purge data base.
     *
     * @param milliseconds
     *            the milliseconds
     *
     * @return the int
     */
    public int purgeDataBase(final long milliseconds) {
        final var purgeTime = System.currentTimeMillis() - milliseconds;
        try {
            return ecpds.purgeDataBase(new Timestamp(System.currentTimeMillis()), new java.sql.Date(purgeTime),
                    new java.sql.Time(purgeTime));
        } catch (SQLException | IOException e) {
            _log.warn("purgeDataBase", e);
        }
        return -1;
    }

    /**
     * Reschedule all data transfers belonging to a specific group to the current time.
     *
     * @param groupBy
     *            the group by
     *
     * @return the int
     */
    public int resetDataTransferSchedulesByGroup(final String groupBy) {
        try {
            return ecpds.resetDataTransferSchedulesByGroup(groupBy);
        } catch (SQLException | IOException e) {
            _log.warn("resetDataTransferSchedulesByGroup", e);
        }
        return -1;
    }

    /**
     * Purge data files.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the int
     */
    public int purgeDataFile(final long dataFileId) {
        try {
            return ecpds.purgeDataFile(dataFileId);
        } catch (SQLException | IOException e) {
            _log.warn("purgeDataFile", e);
        }
        return -1;
    }

    /**
     * Gets the bad data transfers count.
     *
     * @return the bad data transfers count
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Map<String, Integer> getBadDataTransfersCount() throws DataBaseException {
        final var result = new HashMap<String, Integer>();
        try (var rs = ecpds.getBadDataTransfersCount()) {
            while (rs.next()) {
                result.put(rs.getString("DES_NAME"), rs.getInteger("COUNT"));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getBadDataTransfersCount", e);
            throw new DataBaseException("getBadDataTransfersCount", e);
        }
        logSqlRequest("getBadDataTransfersCount", result.size());
        return result;
    }

    /**
     * Allow getting a value cached by the caller.
     *
     * @author Laurent_Gougeon
     *
     */
    public interface DataTransferCache {
        /**
         * Try getting the DataTransfer from the cache. If it is not in the cache then the original DataTransfer should
         * be returned.
         *
         * @param transfer
         *            the data transfer
         *
         * @return the data transfer
         */
        DataTransfer getFromCache(DataTransfer transfer);
    }

    /**
     * Gets the data transfers by host name.
     *
     * @param cache
     *            the data transfer cache
     * @param hostName
     *            the host name
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the data transfers by host name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransfersByHostName(final DataTransferCache cache, final String hostName,
            final Date from, final Date to) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        try (var rs = ecpds.getDataTransfersByHostName(hostName, new Timestamp(from.getTime()),
                new Timestamp(to.getTime()))) {
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var file = new DataFile();
                file.setId(rs.getLong("DAF_ID"));
                file.setSize(rs.getLong("DAT_SIZE"));
                file.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                final var transfer = new DataTransfer();
                transfer.setId(rs.getLong("DAT_ID"));
                transfer.setDataFileId(file.getId());
                transfer.setDataFile(file);
                transfer.setSize(rs.getLong("DAT_SIZE"));
                transfer.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                transfer.setDestinationName(rs.getString("DES_NAME"));
                transfer.setTransferServerName(rs.getString("TRS_NAME"));
                transfer.setTarget(rs.getString("DAT_TARGET"));
                transfer.setStatusCode(rs.getString("STA_CODE"));
                transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                transfer.setSent(rs.getLong("DAT_SENT"));
                transfer.setDuration(rs.getLong("DAT_DURATION"));
                transfer.setPriority(rs.getInt("DAT_PRIORITY"));
                transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                _setHost(this, transfer, rs.getString("HOS_NAME"), hosts);
                array.add(cache.getFromCache(transfer));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDataTransfersByHostName", e);
            throw new DataBaseException("getDataTransfersByHostName", e);
        }
        logSqlRequest("getDataTransfersByHostName", array.size());
        return array;
    }

    /**
     * Gets the data transfers by destination and meta data.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     * @param destinationName
     *            the destination name
     * @param metaStream
     *            the meta stream
     * @param metaTime
     *            the meta time
     *
     * @return the data transfers by destination and meta data
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Map<Long, DataTransfer> getDataTransfersByDestinationAndMetaData(final Date from, final Date to,
            final String destinationName, final String metaStream, final String metaTime) throws DataBaseException {
        final var hashtable = new HashMap<Long, DataTransfer>();
        try (var rs = ecpds.getDataTransfersByDestinationAndMetaData(new Timestamp(from.getTime()),
                new Timestamp(to.getTime()), destinationName, metaStream, metaTime)) {
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var file = new DataFile();
                file.setId(rs.getLong("DAF_ID"));
                file.setSize(rs.getLong("DAF_SIZE"));
                file.setTimeStep(rs.getLong("DAF_TIME_STEP"));
                file.setMetaStream(rs.getString("DAF_META_STREAM"));
                file.setMetaTarget(rs.getString("DAF_META_TARGET"));
                file.setMetaTime(rs.getString("DAF_META_TIME"));
                file.setMetaType(rs.getString("DAF_META_TYPE"));
                file.setArrivedTime(rs.getTimestamp("DAF_ARRIVED_TIME"));
                file.setTimeBase(rs.getTimestamp("DAF_TIME_BASE"));
                final var transfer = new DataTransfer();
                transfer.setId(rs.getLong("DAT_ID"));
                transfer.setDataFileId(file.getId());
                transfer.setDataFile(file);
                transfer.setDestinationName(rs.getString("DES_NAME"));
                transfer.setTransferServerName(rs.getString("TRS_NAME"));
                transfer.setTarget(rs.getString("DAT_TARGET"));
                transfer.setStatusCode(rs.getString("STA_CODE"));
                transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                transfer.setSent(rs.getLong("DAT_SENT"));
                transfer.setDuration(rs.getLong("DAT_DURATION"));
                transfer.setPriority(rs.getInt("DAT_PRIORITY"));
                transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                transfer.setFinishTime(rs.getTimestamp("DAT_FINISH_TIME"));
                transfer.setFirstFinishTime(rs.getTimestamp("DAT_FIRST_FINISH_TIME"));
                transfer.setIdentity(rs.getString("DAT_IDENTITY"));
                final Long dafMoveId = rs.getLong("DAF_MOV_ID");
                if (dafMoveId != null) {
                    final var dafMov = getMonitoringValue(dafMoveId);
                    file.setMonitoringValueId(dafMov.getId());
                    file.setMonitoringValue(dafMov);
                }
                final Long datMoveId = rs.getLong("DAT_MOV_ID");
                if (datMoveId != null) {
                    final var datMov = getMonitoringValue(datMoveId);
                    transfer.setMonitoringValueId(datMov.getId());
                    transfer.setMonitoringValue(datMov);
                }
                _setHost(this, transfer, rs.getString("HOS_NAME"), hosts);
                hashtable.put(transfer.getId(), transfer);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDataTransfersByDestinationAndMetaData", e);
            throw new DataBaseException("getDataTransfersByDestinationAndMetaData", e);
        }
        logSqlRequest("getDataTransfersByDestinationAndMetaData", hashtable.size());
        return hashtable;
    }

    /** The Constant _SET_HOS_NAME. */
    private static final int _SET_HOS_NAME = 0;

    /** The Constant _SET_HOS_NAME_BACKUP. */
    private static final int _SET_HOS_NAME_BACKUP = 1;

    /** The Constant _SET_HOS_NAME_PROXY. */
    private static final int _SET_HOS_NAME_PROXY = 2;

    /**
     * Sets the host.
     *
     * @param db
     *            the db
     * @param object
     *            the object
     * @param hostName
     *            the host name
     * @param hosts
     *            the hosts
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private static void _setHost(final DataBase db, final Object object, final String hostName,
            final HashMap<String, Host> hosts) throws DataBaseException {
        _setHost(db, object, hostName, hosts, _SET_HOS_NAME);
    }

    /**
     * Sets the host.
     *
     * @param db
     *            the db
     * @param object
     *            the object
     * @param hostName
     *            the host name
     * @param hosts
     *            the hosts
     * @param option
     *            the option
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private static void _setHost(final DataBase db, final Object object, final String hostName,
            final HashMap<String, Host> hosts, final int option) throws DataBaseException {
        Host host = null;
        if (hostName != null && (host = hosts.get(hostName)) == null && (host = db.getHost(hostName)) != null) {
            hosts.put(hostName, host);
        }
        if (object instanceof final TransferHistory history) {
            history.setHostName(hostName);
            history.setHost(host);
        } else if (object instanceof final DataTransfer transfer) {
            switch (option) {
            case _SET_HOS_NAME:
                transfer.setHostName(hostName);
                transfer.setHost(host);
                break;
            case _SET_HOS_NAME_BACKUP:
                transfer.setBackupHostName(hostName);
                transfer.setBackupHost(host);
                break;
            case _SET_HOS_NAME_PROXY:
                transfer.setProxyHostName(hostName);
                transfer.setProxyHost(host);
                break;
            default:
                break;
            }
        }
    }

    /**
     * Gets the data transfer not done on date.
     *
     * @return the data transfer not done on date
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransferNotDoneOnDate() throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        try (var rs = ecpds.getDataTransferNotDoneOnDate(new Timestamp(System.currentTimeMillis()))) {
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var file = new DataFile();
                file.setId(rs.getLong("DAF_ID"));
                file.setSize(rs.getLong("DAF_SIZE"));
                file.setTimeStep(rs.getLong("DAF_TIME_STEP"));
                file.setMetaStream(rs.getString("DAF_META_STREAM"));
                file.setMetaTarget(rs.getString("DAF_META_TARGET"));
                file.setMetaTime(rs.getString("DAF_META_TIME"));
                file.setMetaType(rs.getString("DAF_META_TYPE"));
                file.setArrivedTime(rs.getTimestamp("DAF_ARRIVED_TIME"));
                file.setTimeBase(rs.getTimestamp("DAF_TIME_BASE"));
                final var transfer = new DataTransfer();
                transfer.setId(rs.getLong("DAT_ID"));
                transfer.setIdentity(rs.getString("DAT_IDENTITY"));
                transfer.setDataFileId(file.getId());
                transfer.setDataFile(file);
                transfer.setDestinationName(rs.getString("DES_NAME"));
                transfer.setTransferServerName(rs.getString("TRS_NAME"));
                transfer.setTarget(rs.getString("DAT_TARGET"));
                transfer.setStatusCode(rs.getString("STA_CODE"));
                transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                transfer.setSent(rs.getLong("DAT_SENT"));
                transfer.setDuration(rs.getLong("DAT_DURATION"));
                transfer.setPriority(rs.getInt("DAT_PRIORITY"));
                transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                transfer.setFinishTime(rs.getTimestamp("DAT_FINISH_TIME"));
                transfer.setFirstFinishTime(rs.getTimestamp("DAT_FIRST_FINISH_TIME"));
                final Long dafMoveId = rs.getLong("DAF_MOV_ID");
                if (dafMoveId != null) {
                    final var dafMov = getMonitoringValue(dafMoveId);
                    file.setMonitoringValueId(dafMov.getId());
                    file.setMonitoringValue(dafMov);
                }
                final Long datMoveId = rs.getLong("DAT_MOV_ID");
                if (datMoveId != null) {
                    final var datMov = getMonitoringValue(datMoveId);
                    transfer.setMonitoringValueId(datMov.getId());
                    transfer.setMonitoringValue(datMov);
                }
                _setHost(this, transfer, rs.getString("HOS_NAME"), hosts);
                array.add(transfer);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDataTransferNotDoneOnDate", e);
            throw new DataBaseException("getDataTransferNotDoneOnDate", e);
        }
        logSqlRequest("getDataTransferNotDoneOnDate", array.size());
        return array;
    }

    /**
     * Gets the data transfers by transfer server name.
     *
     * @param cache
     *            the data transfer cache
     * @param name
     *            the name
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the data transfers by transfer server name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransfersByTransferServerName(final DataTransferCache cache,
            final String name, final Date from, final Date to) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        try (var rs = ecpds.getDataTransfersPerTransferServer(name, new Timestamp(from.getTime()),
                new Timestamp(to.getTime()))) {
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var file = new DataFile();
                file.setId(rs.getLong("DAF_ID"));
                file.setSize(rs.getLong("DAT_SIZE"));
                file.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                final var transfer = new DataTransfer();
                transfer.setId(rs.getLong("DAT_ID"));
                transfer.setDataFileId(file.getId());
                transfer.setDataFile(file);
                transfer.setSize(rs.getLong("DAT_SIZE"));
                transfer.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                transfer.setDestinationName(rs.getString("DES_NAME"));
                transfer.setTransferServerName(rs.getString("TRS_NAME"));
                transfer.setTarget(rs.getString("DAT_TARGET"));
                transfer.setStatusCode(rs.getString("STA_CODE"));
                transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                transfer.setSent(rs.getLong("DAT_SENT"));
                transfer.setDuration(rs.getLong("DAT_DURATION"));
                transfer.setPriority(rs.getInt("DAT_PRIORITY"));
                transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                _setHost(this, transfer, rs.getString("HOS_NAME"), hosts);
                array.add(cache.getFromCache(transfer));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDataTransfersByTransferServerName", e);
            throw new DataBaseException("getDataTransfersByTransferServerName", e);
        }
        logSqlRequest("getDataTransfersByTransferServerName", array.size());
        return array;
    }

    /**
     * Gets the sql like formatted string.
     *
     * @param search
     *            the search
     *
     * @return the string
     */
    private static String getSQLLikeFormattedString(final String search) {
        if (search == null || "".equals(search.trim())) {
            return "%";
        }
        final var result = new StringBuilder();
        for (final char c : search.toCharArray()) {
            switch (c) {
            case '%':
                result.append("\\%");
                break;
            case '_':
                result.append("\\_");
                break;
            case '*':
                result.append('%');
                break;
            case '?':
                result.append('_');
                break;
            default:
                result.append(c);
                break;
            }
        }
        return result.toString();
    }

    /**
     * Gets the initial data transfer events.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the initial data transfer events
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Iterator<DataTransfer> getInitialDataTransferEventsIterator(final Date from, final Date to)
            throws DataBaseException, SQLException, IOException {
        return new Iterator<>() {
            final HashMap<String, Host> hosts = new HashMap<>();
            final DBResultSet rs = ecpds.getInitialDataTransferEvents(new Timestamp(from.getTime()),
                    new Timestamp(to.getTime()));

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (final SQLException e) {
                    return false;
                }
            }

            @Override
            public DataTransfer next() {
                DataTransfer transfer = null;
                try {
                    final var identity = rs.getString("DAT_IDENTITY");
                    if (identity != null) {
                        final var file = new DataFile();
                        file.setId(rs.getLong("DAF_ID"));
                        file.setSize(rs.getLong("DAF_SIZE"));
                        file.setTimeStep(rs.getLong("DAF_TIME_STEP"));
                        file.setMetaStream(rs.getString("DAF_META_STREAM"));
                        file.setMetaTarget(rs.getString("DAF_META_TARGET"));
                        file.setMetaTime(rs.getString("DAF_META_TIME"));
                        file.setMetaType(rs.getString("DAF_META_TYPE"));
                        file.setArrivedTime(rs.getTimestamp("DAF_ARRIVED_TIME"));
                        file.setTimeBase(rs.getTimestamp("DAF_TIME_BASE"));
                        transfer = new DataTransfer();
                        transfer.setIdentity(identity);
                        transfer.setId(rs.getLong("DAT_ID"));
                        transfer.setDataFileId(file.getId());
                        transfer.setDataFile(file);
                        transfer.setDestinationName(rs.getString("DES_NAME"));
                        transfer.setTransferServerName(rs.getString("TRS_NAME"));
                        transfer.setTarget(rs.getString("DAT_TARGET"));
                        transfer.setStatusCode(rs.getString("STA_CODE"));
                        transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                        transfer.setSent(rs.getLong("DAT_SENT"));
                        transfer.setDuration(rs.getLong("DAT_DURATION"));
                        transfer.setPriority(rs.getInt("DAT_PRIORITY"));
                        transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                        transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                        transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                        transfer.setFinishTime(rs.getTimestamp("DAT_FINISH_TIME"));
                        transfer.setFirstFinishTime(rs.getTimestamp("DAT_FIRST_FINISH_TIME"));
                        final Long dafMoveId = rs.getLong("DAF_MOV_ID");
                        if (dafMoveId != null) {
                            final var dafMov = getMonitoringValue(dafMoveId);
                            file.setMonitoringValueId(dafMov.getId());
                            file.setMonitoringValue(dafMov);
                        }
                        final Long datMoveId = rs.getLong("DAT_MOV_ID");
                        if (datMoveId != null) {
                            final var datMov = getMonitoringValue(datMoveId);
                            transfer.setMonitoringValueId(datMov.getId());
                            transfer.setMonitoringValue(datMov);
                        }
                        _setHost(ECpdsBase.this, transfer, rs.getString("HOS_NAME"), hosts);
                    }
                } catch (final Throwable e) {
                    _log.warn("getInitialDataTransferEventsIterator", e);
                }
                return transfer;
            }

            @Override
            public void remove() {
                if (rs != null) {
                    rs.close();
                }
            }
        };
    }

    /**
     * Gets the initial data transfer events.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     *
     * @return the initial data transfer events
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getInitialDataTransferEvents(final Date from, final Date to)
            throws DataBaseException {
        final var hashtable = new HashMap<String, DataTransfer>();
        try (var rs = ecpds.getInitialDataTransferEvents(new Timestamp(from.getTime()), new Timestamp(to.getTime()))) {
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var identity = rs.getString("DAT_IDENTITY");
                if (identity != null && !hashtable.containsKey(identity)) {
                    final var file = new DataFile();
                    file.setId(rs.getLong("DAF_ID"));
                    file.setSize(rs.getLong("DAF_SIZE"));
                    file.setTimeStep(rs.getLong("DAF_TIME_STEP"));
                    file.setMetaStream(rs.getString("DAF_META_STREAM"));
                    file.setMetaTarget(rs.getString("DAF_META_TARGET"));
                    file.setMetaTime(rs.getString("DAF_META_TIME"));
                    file.setMetaType(rs.getString("DAF_META_TYPE"));
                    file.setArrivedTime(rs.getTimestamp("DAF_ARRIVED_TIME"));
                    file.setTimeBase(rs.getTimestamp("DAF_TIME_BASE"));
                    final var transfer = new DataTransfer();
                    transfer.setIdentity(identity);
                    transfer.setId(rs.getLong("DAT_ID"));
                    transfer.setDataFileId(file.getId());
                    transfer.setDataFile(file);
                    transfer.setDestinationName(rs.getString("DES_NAME"));
                    transfer.setTransferServerName(rs.getString("TRS_NAME"));
                    transfer.setTarget(rs.getString("DAT_TARGET"));
                    transfer.setStatusCode(rs.getString("STA_CODE"));
                    transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                    transfer.setSent(rs.getLong("DAT_SENT"));
                    transfer.setDuration(rs.getLong("DAT_DURATION"));
                    transfer.setPriority(rs.getInt("DAT_PRIORITY"));
                    transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                    transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                    transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                    transfer.setFinishTime(rs.getTimestamp("DAT_FINISH_TIME"));
                    transfer.setFirstFinishTime(rs.getTimestamp("DAT_FIRST_FINISH_TIME"));
                    final Long dafMoveId = rs.getLong("DAF_MOV_ID");
                    if (dafMoveId != null) {
                        final var dafMov = getMonitoringValue(dafMoveId);
                        file.setMonitoringValueId(dafMov.getId());
                        file.setMonitoringValue(dafMov);
                    }
                    final Long datMoveId = rs.getLong("DAT_MOV_ID");
                    if (datMoveId != null) {
                        final var datMov = getMonitoringValue(datMoveId);
                        transfer.setMonitoringValueId(datMov.getId());
                        transfer.setMonitoringValue(datMov);
                    }
                    _setHost(this, transfer, rs.getString("HOS_NAME"), hosts);
                    hashtable.put(identity, transfer);
                }
            }
        } catch (SQLException | IOException e) {
            _log.warn("getInitialDataTransferEvents", e);
            throw new DataBaseException("getInitialDataTransferEvents", e);
        }
        logSqlRequest("getInitialDataTransferEvents", hashtable.size());
        return hashtable.values();
    }

    /**
     * Gets the sorted data transfers by status code and date. This is used on the transfer page!
     *
     * @param cache
     *            the data transfer cache
     * @param status
     *            the status
     * @param from
     *            the from
     * @param to
     *            the to
     * @param fileName
     *            the search
     * @param type
     *            the type
     * @param cursor
     *            the cursor
     *
     * @return the sorted data transfers by status code and date
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getSortedDataTransfersByStatusCodeAndDate(final DataTransferCache cache,
            final String status, final Date from, final Date to, final String fileName, final String type,
            final DataBaseCursor cursor) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        DataTransfer initialTransfer = null; // The first DataTransfer will contain the collection size (total)!
        DBResultSet rs = null;
        try {
            final var options = new SQLParameterParser(fileName, "target", "source", "ts=d", "priority=d", "checksum",
                    "groupby", "identity", "size=b", "replicated=?", "asap=?", "event=?", "deleted=?", "expired=?",
                    "proxy=?");
            rs = ecpds.getSortedDataTransfersByStatusOnDate(status, new Timestamp(from.getTime()),
                    new Timestamp(to.getTime()), options.get(0, "DAT_TARGET"), options.get(1, "DAF_ORIGINAL"),
                    options.get(2, "DAT_TIME_STEP"), options.get(3, "DAT_PRIORITY"), options.get(4, "DAF_CHECKSUM"),
                    options.get(5, "DAF_GROUP_BY"), options.get(6, "DAT_IDENTITY"), options.get(7, "DAT_SIZE"),
                    options.get(8, "DAT_REPLICATED"), options.get(9, "DAT_ASAP"), options.get(10, "DAT_EVENT"),
                    options.get(11, "DAT_DELETED"), options.get(12, "DAT_EXPIRY_TIME < UNIX_TIMESTAMP() * 1000"),
                    options.get(13, "HOS_NAME_PROXY is not null"), type, cursor.getSort(), cursor.getOrder(),
                    cursor.getStart(), cursor.getLength(), options.has(1) || options.has(4) || options.has(5));
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var file = new DataFile();
                file.setId(rs.getLong("DAF_ID"));
                file.setSize(rs.getLong("DAT_SIZE"));
                file.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                final var transfer = new DataTransfer();
                transfer.setId(rs.getLong("DAT_ID"));
                transfer.setDataFileId(file.getId());
                transfer.setDataFile(file);
                transfer.setSize(rs.getLong("DAT_SIZE"));
                transfer.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                transfer.setDestinationName(rs.getString("DES_NAME"));
                transfer.setTransferServerName(rs.getString("TRS_NAME"));
                transfer.setTarget(rs.getString("DAT_TARGET"));
                transfer.setStatusCode(rs.getString("STA_CODE"));
                transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                transfer.setSent(rs.getLong("DAT_SENT"));
                transfer.setDuration(rs.getLong("DAT_DURATION"));
                transfer.setDeleted(rs.getBoolean("DAT_DELETED"));
                transfer.setPriority(rs.getInt("DAT_PRIORITY"));
                transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                transfer.setStartTime(rs.getTimestamp("DAT_START_TIME"));
                transfer.setFinishTime(rs.getTimestamp("DAT_FINISH_TIME"));
                transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                transfer.setFailedTime(rs.getTimestamp("DAT_FAILED_TIME"));
                _setHost(this, transfer, rs.getString("HOS_NAME"), hosts);
                final var used = cache.getFromCache(transfer);
                if (initialTransfer == null) {
                    initialTransfer = used;
                }
                array.add(used);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getSortedDataTransfersByStatusCodeAndDate", e);
            throw new DataBaseException("getSortedDataTransfersByStatusCodeAndDate", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first
        // DataTransfer (if we have any)!
        if (initialTransfer != null && rs != null) {
            initialTransfer.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getSortedDataTransfersByStatusCodeAndDate(" + cursor + ")", array.size());
        return array;
    }

    /**
     * Gets the sorted destination names and comments.
     *
     * @return the sorted destination names and comments
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Set<Map.Entry<String, String>> getDestinationNamesAndComments() throws DataBaseException {
        final Set<Map.Entry<String, String>> list = new LinkedHashSet<>();
        try (var rs = ecpds.getDestinationNamesAndComments()) {
            while (rs.next()) {
                list.add(new AbstractMap.SimpleEntry<>(rs.getString("DES_NAME"), rs.getString("DES_COMMENT")));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationNamesAndComments", e);
        }
        logSqlRequest("getDestinationNamesAndComments", list.size());
        return list;
    }

    /**
     * Gets the destination exts.
     *
     * @return the destination exts
     */
    public DestinationExt[] getDestinationExts() {
        final List<DestinationExt> list = new ArrayList<>();
        try (var rs = ecpds.getDestinationExts()) {
            while (rs.next()) {
                final var destination = new DestinationExt(rs.getString("DES_NAME"));
                destination.pendingTransfersCount = rs.getInt("PENDING_COUNT");
                destination.setResetFrequency(rs.getLong("DES_RESET_FREQUENCY"));
                destination.setStopIfDirty(rs.getBoolean("DES_STOP_IF_DIRTY"));
                destination.setUpdate(rs.getTimestamp("DES_UPDATE"));
                destination.minQueueTime = rs.getTimestamp("MIN_QUEUE_TIME");
                destination.setStatusCode(rs.getString("STA_CODE"));
                final var value = new SchedulerValue(rs.getInt("SCV_ID"));
                value.setHostName(rs.getString("HOS_NAME"));
                value.setStartCount(rs.getInt("SCV_START_COUNT"));
                value.setResetTime(rs.getTimestamp("SCV_RESET_TIME"));
                destination.setSchedulerValueId(value.getId());
                destination.setSchedulerValue(value);
                list.add(destination);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationExts", e);
        }
        logSqlRequest("getDestinationExts", list.size());
        return list.toArray(new DestinationExt[list.size()]);
    }

    /**
     * Gets the pending data transfers.
     *
     * @param destination
     *            the destination
     * @param limit
     *            the limit
     *
     * @return the pending data transfers
     */
    public List<DataTransfer> getPendingDataTransfers(final Destination destination, final int limit) {
        final List<DataTransfer> list = new ArrayList<>();
        try (var rs = ecpds.getPendingDataTransfers(destination.getName(), new Timestamp(System.currentTimeMillis()),
                limit)) {
            while (rs.next()) {
                final var transfer = new DataTransfer(rs.getLong("DAT_ID"));
                transfer.setStatusCode(rs.getString("STA_CODE"));
                transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                list.add(transfer);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getPendingDataTransfers", e);
        }
        logSqlRequest("getPendingDataTransfers", list.size());
        return list;
    }

    /**
     * Gets the data files by group by.
     *
     * @return the data files by group by
     */
    @SuppressWarnings("null")
    public Map<String, List<Long>> getDataFilesByGroupBy() {
        final var files = new HashMap<String, List<Long>>();
        String groupBy = null;
        List<Long> vector = null;
        try (var rs = ecpds.getDataFilesByGroupBy()) {
            while (rs.next()) {
                final var current = rs.getString("DAF_GROUP_BY");
                if (!current.equals(groupBy)) {
                    files.put(groupBy = current, vector = new ArrayList<>());
                }
                vector.add(rs.getLong("DAF_ID"));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDataFilesByGroupBy", e);
        }
        logSqlRequest("getDataFilesByGroupBy", files.size());
        return files;
    }

    /**
     * Gets the data files by group by count.
     *
     * @param groupBy
     *            the group by
     *
     * @return the data files by group by count
     */
    public long getDataFilesByGroupByCount(final String groupBy) {
        try {
            return ecpds.getDataFilesByGroupByCount(groupBy);
        } catch (SQLException | IOException e) {
            _log.warn("getDataFilesByGroupByCount", e);
        }
        return -1;
    }

    /**
     * Gets the data transfers by data file id.
     *
     * @param cache
     *            the data transfer cache
     * @param dataFileId
     *            the dataFile id
     *
     * @return the data transfers by data file id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public List<DataTransfer> getDataTransfersByDataFileId(final DataTransferCache cache, final long dataFileId)
            throws DataBaseException {
        return getDataTransfersByDataFileId(cache, dataFileId, false);
    }

    /**
     * Gets the data transfers by data file id.
     *
     * @param cache
     *            the data transfer cache
     * @param dataFileId
     *            the id
     * @param includeDeleted
     *            the include deleted
     *
     * @return the data transfers by data file id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public List<DataTransfer> getDataTransfersByDataFileId(final DataTransferCache cache, final long dataFileId,
            final boolean includeDeleted) throws DataBaseException {
        final List<DataTransfer> vector = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByDataFile(dataFileId, includeDeleted, DataTransfer.class);
            while (it.hasNext()) {
                vector.add(cache.getFromCache(it.next()));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDataTransfersByDataFileId", e);
            throw new DataBaseException("getDataTransfersByDataFileId", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDataTransfersByDataFileId", vector.size());
        return vector;
    }

    /**
     * Gets the destinations by country iso.
     *
     * @param isoCode
     *            the iso code
     *
     * @return the destinations by country iso
     */
    public Destination[] getDestinationsByCountryISO(final String isoCode) {
        final List<Destination> list = new ArrayList<>();
        DBIterator<Destination> it = null;
        try {
            it = ecpds.getDestinationsByCountry(isoCode, Destination.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationsByCountryISO", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationsByCountryISO", list.size());
        return list.toArray(new Destination[list.size()]);
    }

    /**
     * Gets the destinations by user.
     *
     * @param uid
     *            the uid
     * @param options
     *            the options
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
     */
    public List<Destination> getDestinationsByUser(final String uid, final SQLParameterParser options,
            final String fromToAliases, final boolean asc, final String status, final String type,
            final String filter) {
        final List<Destination> list = new ArrayList<>();
        DBIterator<Destination> it = null;
        try {
            it = ecpds.getDestinationsByUser(uid, options.get(0, "DES_NAME"), options.get(1, "DES_COMMENT"),
                    options.get(2, "COUNTRY.COU_ISO", "COUNTRY.COU_NAME"), options.get(3, "DES_DATA"),
                    options.get(4, "DES_ACTIVE"), options.get(5, "DES_MONITOR"), options.get(6, "DES_BACKUP"),
                    fromToAliases, asc, status, type, filter, Destination.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationsByUser", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationsByUser", list.size());
        return list;
    }

    /**
     * Gets the destinations by host name.
     *
     * @param hostName
     *            the host name
     *
     * @return the destinations by host name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Destination[] getDestinationsByHostName(final String hostName) throws DataBaseException {
        final List<Destination> list = new ArrayList<>();
        DBIterator<Destination> it = null;
        try {
            it = ecpds.getDestinationsByHostName(hostName, Destination.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDestinationsByHostName", e);
            throw new DataBaseException("getDestinationsByHostName", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDestinationsByHostName", list.size());
        return list.toArray(new Destination[list.size()]);
    }

    /**
     * Gets the transfer history.
     *
     * @param dataTransferId
     *            the data transfer id
     *
     * @return the transfer history
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferHistory[] getTransferHistoryByDataTransferId(final long dataTransferId) throws DataBaseException {
        final List<TransferHistory> list = new ArrayList<>();
        try (var rs = ecpds.getTransferHistoryPerDataTransfer(dataTransferId)) {
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var history = new TransferHistory();
                history.setId(rs.getLong("TRH_ID"));
                history.setDestinationName(rs.getString("DES_NAME"));
                history.setComment(rs.getString("TRH_COMMENT"));
                history.setDataTransferId(rs.getLong("DAT_ID"));
                history.setError(rs.getBoolean("TRH_ERROR"));
                history.setSent(rs.getLong("TRH_SENT"));
                history.setTime(rs.getTimestamp("TRH_TIME"));
                history.setStatusCode(rs.getString("STA_CODE"));
                _setHost(this, history, rs.getString("HOS_NAME"), hosts);
                list.add(history);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getTransferHistoryByDataTransferId", e);
            throw new DataBaseException("getTransferHistoryByDataTransferId", e);
        }
        logSqlRequest("getTransferHistoryByDataTransferId", list.size());
        return list.toArray(new TransferHistory[list.size()]);
    }

    /**
     * Gets the sorted transfer history.
     *
     * @param dataTransferId
     *            the data transfer id
     * @param afterScheduleTime
     *            the after schedule time
     * @param cursor
     *            the cursor
     *
     * @return the sorted transfer history
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    @SuppressWarnings("null")
    public TransferHistory[] getSortedTransferHistory(final long dataTransferId, final boolean afterScheduleTime,
            final DataBaseCursor cursor) throws DataBaseException {
        final List<TransferHistory> list = new ArrayList<>();
        TransferHistory initialHistory = null; // In the first TransferHistory we set the collection size (total)!
        DBResultSet rs = null;
        try {
            rs = ecpds.getSortedTransferHistoryPerDataTransfer(dataTransferId, afterScheduleTime, cursor.getSort(),
                    cursor.getOrder(), cursor.getStart(), cursor.getLength());
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var history = new TransferHistory();
                if (initialHistory == null) {
                    initialHistory = history;
                }
                history.setId(rs.getLong("TRH_ID"));
                history.setDestinationName(rs.getString("DES_NAME"));
                history.setComment(rs.getString("TRH_COMMENT"));
                history.setDataTransferId(rs.getLong("DAT_ID"));
                history.setError(rs.getBoolean("TRH_ERROR"));
                history.setSent(rs.getLong("TRH_SENT"));
                history.setTime(rs.getTimestamp("TRH_TIME"));
                history.setStatusCode(rs.getString("STA_CODE"));
                _setHost(this, history, rs.getString("HOS_NAME"), hosts);
                list.add(history);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getSortedTransferHistory", e);
            throw new DataBaseException("getSortedTransferHistory", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first
        // TransferHistory (if we have any)!
        if (initialHistory != null) {
            initialHistory.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getSortedTransferHistory(" + cursor + ")", list.size());
        return list.toArray(new TransferHistory[list.size()]);
    }

    /**
     * Gets the scheduled data transfer.
     *
     * @param uniqueKey
     *            the unique key
     * @param destinationName
     *            the destination name
     *
     * @return the scheduled data transfer
     */
    public DataTransfer[] getScheduledDataTransfer(final String uniqueKey, final String destinationName) {
        final List<DataTransfer> list = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getScheduledDataTransfer(uniqueKey, destinationName, DataTransfer.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getScheduledDataTransfer", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getScheduledDataTransfer", list.size());
        return list.toArray(new DataTransfer[list.size()]);
    }

    /**
     * Gets the existing storage directories.
     *
     * @return the existing storage directories
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<ExistingStorageDirectory> getExistingStorageDirectories() throws IOException, SQLException {
        return new Iterator<>() {
            final DBResultSet rs = ecpds.getExistingStorageDirectories();

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (final SQLException e) {
                    return false;
                }
            }

            @Override
            public ExistingStorageDirectory next() {
                ExistingStorageDirectory file = null;
                try {
                    file = new ExistingStorageDirectory();
                    file.setArrivedTime(rs.getDate("DAF_ARRIVED_DATE").getTime());
                    file.setTransferGroupName(rs.getString("TRG_NAME"));
                    file.setFileSystem(rs.getInt("DAF_FILE_SYSTEM"));
                    file.setFilesCount(rs.getInt("FILES_COUNT"));
                    file.setFilesSize(rs.getLong("FILES_SIZE"));
                } catch (final SQLException e) {
                    _log.warn("getExistingStorageDirectories", e);
                }
                return file;
            }

            @Override
            public void remove() {
                if (rs != null) {
                    rs.close();
                }
            }
        };
    }

    /**
     * Gets the existing storage directories per proxy host.
     *
     * @return the existing storage directories per proxy host
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<ExistingStorageDirectory> getExistingStorageDirectoriesPerProxyHost()
            throws IOException, SQLException {
        return new Iterator<>() {
            final DBResultSet rs = ecpds.getExistingStorageDirectoriesPerProxyHost();

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (final SQLException e) {
                    return false;
                }
            }

            @Override
            public ExistingStorageDirectory next() {
                ExistingStorageDirectory file = null;
                try {
                    file = new ExistingStorageDirectory();
                    file.setArrivedTime(rs.getDate("DAF_ARRIVED_DATE").getTime());
                    file.setProxyHostName(rs.getString("HOS_NAME_PROXY"));
                    file.setFileSystem(rs.getInt("DAF_FILE_SYSTEM"));
                } catch (final SQLException e) {
                    _log.warn("getExistingStorageDirectoriesPerProxyHost", e);
                }
                return file;
            }

            @Override
            public void remove() {
                if (rs != null) {
                    rs.close();
                }
            }
        };
    }

    /**
     * Gets the expired data files iterator. Avoid getting a large array of DataFiles!
     *
     * @param limit
     *            the limit
     *
     * @return the expired data files iterator
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<DataFile> getExpiredDataFilesIterator(final int limit) throws IOException, SQLException {
        return new Iterator<>() {
            final DBResultSet rs = ecpds.getExpiredDataFiles(limit);

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (final SQLException e) {
                    return false;
                }
            }

            @Override
            public DataFile next() {
                DataFile file = null;
                try {
                    file = new DataFile();
                    file.setId(rs.getLong("DAF_ID"));
                    file.setTransferGroupName(rs.getString("TRG_NAME"));
                    file.setTransferGroup(new TransferGroup(rs.getString("TRG_NAME")));
                    final Long monitoringValueId = rs.getLong("MOV_ID");
                    if (monitoringValueId != null) {
                        file.setMonitoringValueId(monitoringValueId);
                        file.setMonitoringValue(new MonitoringValue(monitoringValueId));
                    }
                    file.setArrivedTime(rs.getTimestamp("DAF_ARRIVED_TIME"));
                    file.setOriginal(rs.getString("DAF_ORIGINAL"));
                    file.setSource(rs.getString("DAF_SOURCE"));
                    file.setSize(rs.getLong("DAF_SIZE"));
                    file.setMetaStream(rs.getString("DAF_META_STREAM"));
                    file.setMetaTarget(rs.getString("DAF_META_TARGET"));
                    file.setMetaTime(rs.getString("DAF_META_TIME"));
                    file.setMetaType(rs.getString("DAF_META_TYPE"));
                    file.setDeleteOriginal(rs.getBoolean("DAF_DELETE_ORIGINAL"));
                    file.setTimeStep(rs.getLong("DAF_TIME_STEP"));
                    file.setTimeBase(rs.getTimestamp("DAF_TIME_BASE"));
                    file.setTimeFile(rs.getTimestamp("DAF_TIME_FILE"));
                    file.setDeleted(rs.getBoolean("DAF_DELETED"));
                    file.setRemoved(rs.getBoolean("DAF_REMOVED"));
                    file.setFileSystem(rs.getInteger("DAF_FILE_SYSTEM"));
                    file.setFileInstance(rs.getInteger("DAF_FILE_INSTANCE"));
                } catch (final SQLException e) {
                    _log.warn("getExpiredDataFilesIterator", e);
                }
                return file;
            }

            @Override
            public void remove() {
                if (rs != null) {
                    rs.close();
                }
            }
        };
    }

    /**
     * Gets the data files to filter iterator. Avoid getting a large array of DataFiles!
     *
     * @param limit
     *            the limit
     *
     * @return the data files to filter iterator
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<DataFile> getDataFilesToFilterIterator(final int limit) throws IOException, SQLException {
        return new Iterator<>() {
            final DBResultSet rs = ecpds.getDataFilesToFilter(limit);

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (final SQLException e) {
                    return false;
                }
            }

            @Override
            public DataFile next() {
                DataFile file = null;
                try {
                    file = new DataFile();
                    file.setId(rs.getLong("DAF_ID"));
                    file.setTransferGroupName(rs.getString("TRG_NAME"));
                    final var group = new TransferGroup(rs.getString("TRG_NAME"));
                    group.setMinFilteringCount(rs.getInteger("TRG_MIN_FILTERING_COUNT"));
                    group.setMinReplicationCount(rs.getInteger("TRG_MIN_REPLICATION_COUNT"));
                    file.setTransferGroup(group);
                    file.setArrivedTime(rs.getTimestamp("DAF_ARRIVED_TIME"));
                    file.setOriginal(rs.getString("DAF_ORIGINAL"));
                    file.setTimeStep(rs.getLong("DAF_TIME_STEP"));
                    file.setFilterTime(rs.getTimestamp("DAF_FILTER_TIME"));
                    file.setFilterName(rs.getString("DES_FILTER_NAME"));
                    file.setChecksum(rs.getString("DAF_CHECKSUM"));
                    file.setFileSystem(rs.getInteger("DAF_FILE_SYSTEM"));
                    file.setFileInstance(rs.getInteger("DAF_FILE_INSTANCE"));
                    file.setSize(rs.getLong("DAF_SIZE"));
                } catch (final SQLException e) {
                    _log.warn("getDataFilesToFilterIterator", e);
                }
                return file;
            }

            @Override
            public void remove() {
                if (rs != null) {
                    rs.close();
                }
            }
        };
    }

    /**
     * Gets the publications to process iterator. Avoid getting a large array of Publications!
     *
     * @param limit
     *            the limit
     *
     * @return the publications to process iterator
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<Publication> getPublicationIterator(final int limit) throws IOException, SQLException {
        return new Iterator<>() {
            final DBResultSet rs = ecpds.getPublicationsToProcess(limit);

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (final SQLException e) {
                    return false;
                }
            }

            @Override
            public Publication next() {
                Publication publication = null;
                try {
                    publication = new Publication();
                    publication.setId(rs.getLong("PUB_ID"));
                    publication.setDataTransferId(rs.getLong("DAT_ID"));
                    publication.setScheduledTime(rs.getTimestamp("PUB_SCHEDULED_TIME"));
                    publication.setProcessedTime(rs.getTimestamp("PUB_PROCESSED_TIME"));
                    publication.setDone(rs.getBoolean("PUB_DONE"));
                    publication.setOptions(rs.getString("PUB_OPTIONS"));
                } catch (final SQLException e) {
                    _log.warn("getPublicationIterator", e);
                }
                return publication;
            }

            @Override
            public void remove() {
                if (rs != null) {
                    rs.close();
                }
            }
        };
    }

    /**
     * Gets the data transfers to replicate iterator. Get an iterator to avoid having a large array of DataTransfers!
     *
     * @param limit
     *            the limit
     *
     * @return the data transfers to replicate iterator
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<DataTransfer> getDataTransfersToReplicateIterator(final int limit)
            throws IOException, SQLException {
        return ecpds.getDataTransfersToReplicate(limit, DataTransfer.class);
    }

    /**
     * Gets the data transfers to backup iterator. Avoid getting a large array of DataTransfers!
     *
     * @param limit
     *            the limit
     *
     * @return the data transfers to backup iterator
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<DataTransfer> getDataTransfersToBackupIterator(final int limit) throws IOException, SQLException {
        return ecpds.getDataTransfersToBackup(limit, DataTransfer.class);
    }

    /**
     * Gets the data transfers to proxy iterator. Avoid getting a large array of DataTransfers!
     *
     * @param limit
     *            the limit
     *
     * @return the data transfers to proxy iterator
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<DataTransfer> getDataTransfersToProxyIterator(final int limit) throws IOException, SQLException {
        return ecpds.getDataTransfersToProxy(limit, DataTransfer.class);
    }

    /**
     * Gets the acquisition data transfers to download iterator. Avoid caching the DataTransfers in an array!
     *
     * @param limit
     *            the limit
     *
     * @return the acquisition data transfers to download iterator
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Iterator<DataTransfer> getAcquisitionDataTransfersToDownloadIterator(final int limit)
            throws SQLException, IOException {
        return ecpds.getAcquisitionDataTransfersToDownload(limit, DataTransfer.class);
    }

    /**
     * Gets the dissemination data transfers to download iterator. Avoid caching the DataTransfers in an array!
     *
     * @param limit
     *            the limit
     *
     * @return the dissemination data transfers to download iterator
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Iterator<DataTransfer> getDisseminationDataTransfersToDownloadIterator(final int limit)
            throws SQLException, IOException {
        return ecpds.getDisseminationDataTransfersToDownload(limit, DataTransfer.class);
    }

    /**
     * Purge expired data transfers.
     *
     * @param maxIdentityCount
     *            the max identity count
     * @param maxTransferLife
     *            the max transfer life
     *
     * @return the long
     */
    public long purgeExpiredDataTransfers(final int maxIdentityCount, final int maxTransferLife) {
        DBResultSet rs = null;
        String currentIdentity = null;
        var entries = 0L;
        var removed = 0L;
        try {
            rs = ecpds.getExpiredDataTransfers();
            var count = 0;
            while (rs.next()) {
                var transferRemoved = false;
                entries++;
                final var newIdentity = rs.getString("DAT_IDENTITY");
                if (currentIdentity == null || !currentIdentity.equals(newIdentity)) {
                    currentIdentity = newIdentity;
                    count = 1;
                } else {
                    count++;
                    if (count > maxIdentityCount) {
                        ecpds.removeDataTransfer(rs.getLong("DAT_ID"));
                        transferRemoved = true;
                        removed++;
                    }
                }
                if (!transferRemoved) {
                    final var expiryTime = rs.getTimestamp("DAT_EXPIRY_TIME");
                    if (expiryTime != null
                            && System.currentTimeMillis() - expiryTime.getTime() > Timer.ONE_DAY * maxTransferLife) {
                        ecpds.removeDataTransfer(rs.getLong("DAT_ID"));
                        removed++;
                    }
                }
            }
        } catch (SQLException | IOException e) {
            _log.warn("purgeExpiredDataTransfers", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        logSqlRequest("purgeExpiredDataTransfers", entries);
        return removed;
    }

    /**
     * Gets the categories per user id.
     *
     * @param userId
     *            the user id
     *
     * @return the categories per user id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<Category> getCategoriesPerUserId(final String userId) throws DataBaseException {
        final List<Category> array = new ArrayList<>();
        DBIterator<Category> it = null;
        try {
            it = ecpds.getCategoriesPerUser(userId, Category.class);
            while (it.hasNext()) {
                array.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getCategoriesPerUserId", e);
            throw new DataBaseException("getCategoriesPerUserId", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getCategoriesPerUserId", array.size());
        return array;
    }

    /**
     * Gets the incoming user object with the data field populated with the data from the associated data policies. Not
     * meant to be saved to the database by the caller!
     *
     * @param user
     *            the user
     *
     * @return the incoming user object
     */
    public String getDataFromUserPolicies(final IncomingUser user) {
        final var sb = new StringBuilder();
        // Do we have some data policies associated with this user?
        for (final PolicyUser policyUser : getPolicyUserList(user.getId())) {
            final var policyData = policyUser.getIncomingPolicy().getData();
            if (policyData != null && policyData.length() > 0) {
                sb.append(policyData).append("\n");
            }
        }
        final var userData = user.getData();
        if (userData != null && userData.length() > 0) {
            sb.append(userData);
        }
        return sb.toString();
    }

    /**
     * Gets the user policies per user id.
     *
     * @param userId
     *            the user id
     *
     * @return the user policies per user id
     */
    public List<PolicyUser> getPolicyUserList(final String userId) {
        final List<PolicyUser> list;
        final var mutex = policyUserCache.getMutex(userId);
        synchronized (mutex.lock()) {
            try {
                list = policyUserCache.computeIfAbsent(userId, k -> {
                    DBIterator<PolicyUser> it = null;
                    final List<PolicyUser> defaultList = new ArrayList<>();
                    try {
                        it = ecpds.getPolicyUserList(k, PolicyUser.class);
                        while (it.hasNext()) {
                            defaultList.add(it.next());
                        }
                    } catch (SQLException | IOException e) {
                        _log.warn("getPolicyUserList", e);
                    } finally {
                        if (it != null) {
                            it.remove();
                        }
                    }
                    policyUserCache.put(k, defaultList, CACHE_TIMEOUT);
                    return defaultList;
                });
            } finally {
                mutex.free();
            }
        }
        logSqlRequest("getPolicyUserList", list.size());
        return list;
    }

    /**
     * Gets the users per category id.
     *
     * @param categoryId
     *            the category id
     *
     * @return the users per category id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<WebUser> getUsersPerCategoryId(final String categoryId) throws DataBaseException {
        final List<WebUser> array = new ArrayList<>();
        DBIterator<WebUser> it = null;
        try {
            it = ecpds.getUsersPerCategory(categoryId, WebUser.class);
            while (it.hasNext()) {
                array.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getUsersPerCategoryId", e);
            throw new DataBaseException("getUsersPerCategoryId", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getUsersPerCategoryId", array.size());
        return array;
    }

    /**
     * Gets the urls per category id.
     *
     * @param id
     *            the id
     *
     * @return the urls per category id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<Url> getUrlsPerCategoryId(final String id) throws DataBaseException {
        final List<Url> array = new ArrayList<>();
        try (var rs = ecpds.getUrlsPerCategory(id)) {
            while (rs.next()) {
                array.add(new Url(rs.getString("URL_NAME")));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getUrlsPerCategoryId", e);
            throw new DataBaseException("getUrlsPerCategoryId", e);
        }
        logSqlRequest("getUrlsPerCategoryId", array.size());
        return array;
    }

    /**
     * Gets the categories per resource id.
     *
     * @param id
     *            the id
     *
     * @return the categories per resource id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<Category> getCategoriesPerResourceId(final String id) throws DataBaseException {
        final List<Category> array = new ArrayList<>();
        DBIterator<Category> it = null;
        try {
            it = ecpds.getCategoriesPerUrl(id, Category.class);
            while (it.hasNext()) {
                array.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getCategoriesPerResourceId", e);
            throw new DataBaseException("getCategoriesPerResourceId", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getCategoriesPerResourceId", array.size());
        return array;
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    @SuppressWarnings("null")
    public Collection<DataFile> getDataFilesByMetaData(final String name, final String value, final Date from,
            final Date to, final DataBaseCursor cursor) throws DataBaseException {
        final List<DataFile> array = new ArrayList<>();
        DataFile initialFile = null; // The first DataFile will contain the collection size (total)!
        DBResultSet rs = null;
        try {
            rs = ecpds.getDataFilesByMetaData(name, value, new Timestamp(from.getTime()), new Timestamp(to.getTime()),
                    cursor.getSort(), cursor.getOrder(), cursor.getStart(), cursor.getLength());
            while (rs.next()) {
                final var file = new DataFile();
                file.setId(rs.getLong("DAF_ID"));
                file.setSize(rs.getLong("DAF_SIZE"));
                file.setTimeStep(rs.getLong("DAF_TIME_STEP"));
                file.setTimeBase(rs.getTimestamp("DAF_TIME_BASE"));
                file.setOriginal(rs.getString("DAF_ORIGINAL"));
                if (initialFile == null) {
                    initialFile = file;
                }
                array.add(file);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDataFilesByMetaData", e);
            throw new DataBaseException("getDataFilesByMetaData", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first
        // DataTransfer (if we have any)!
        if (initialFile != null) {
            initialFile.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getDataFilesByMetaData(" + cursor + ")", array.size());
        return array;
    }

    /**
     * Gets the transfer count and meta data by filter. This is used on the destination page to show the number of
     * transfers per stream, target of status!
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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<List<String>> getTransferCountAndMetaDataByFilter(final String countBy, final String destination,
            final String target, final String stream, final String time, final String status, final String fileName,
            final Date from, final Date to, final String privilegedUser, final Date scheduledBefore)
            throws DataBaseException {
        final List<List<String>> results = new ArrayList<>();
        DBResultSet rs = null;
        try {
            final var options = new SQLParameterParser(fileName, "target", "source", "ts=d", "priority=d", "checksum",
                    "groupby", "identity", "size=b", "replicated=?", "asap=?", "event=?", "deleted=?", "expired=?",
                    "proxy=?");
            rs = ecpds.getDataTransferCountAndMetaDataByFilter(destination, countBy, target, stream, time, status,
                    options.get(0, "DAT_TARGET"), options.get(1, "DAF_ORIGINAL"), options.get(2, "DAT_TIME_STEP"),
                    options.get(3, "DAT_PRIORITY"), options.get(4, "DAF_CHECKSUM"), options.get(5, "DAF_GROUP_BY"),
                    options.get(6, "DAT_IDENTITY"), options.get(7, "DAT_SIZE"), options.get(8, "DAT_REPLICATED"),
                    options.get(9, "DAT_ASAP"), options.get(10, "DAT_EVENT"), options.get(11, "DAT_DELETED"),
                    options.get(12, "DAT_EXPIRY_TIME < UNIX_TIMESTAMP() * 1000"),
                    options.get(13, "HOS_NAME_PROXY is not null"), new Timestamp(from.getTime()),
                    new Timestamp(to.getTime()), privilegedUser, new Timestamp(scheduledBefore.getTime()),
                    options.has(1) || options.has(4) || options.has(5));
            var total = 0L;
            var size = 0L;
            final var addSize = "stream2".equals(countBy) || "target2".equals(countBy) || "status2".equals(countBy);
            while (rs.next()) {
                final List<String> p = new ArrayList<>(2);
                p.add(rs.getString("name") != null ? rs.getString("name") : "None");
                if (addSize) {
                    p.add(rs.getString("count") + ";" + rs.getLong("size"));
                    total += rs.getInt("count");
                    size += rs.getLong("size");
                } else {
                    p.add(rs.getString("count"));
                    total += rs.getInt("count");
                }
                results.add(p);
            }
            final List<String> p = new ArrayList<>(2);
            p.add("All");
            if (addSize) {
                p.add(total + ";" + size);
            } else {
                p.add(Long.toString(total));
            }
            results.add(0, p);
        } catch (IOException | SQLException e) {
            _log.warn("getTransferCountAndMetaDataByFilter", e);
            throw new DataBaseException("getTransferCountAndMetaDataByFilter", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        logSqlRequest("getTransferCountAndMetaDataByFilter", results.size());
        return results;
    }

    /**
     * Gets the sorted data transfers by filter. This is used on the destination page, to filter the transfers!
     *
     * @param cache
     *            the data transfer cache
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
     * @return the sorted data transfers by filter
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransfersByFilter(final DataTransferCache cache, final String destination,
            final String target, final String stream, final String time, final String status,
            final String privilegedUser, final Date scheduledBefore, final String fileName, final Date from,
            final Date to, final DataBaseCursor cursor) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        DataTransfer initialTransfer = null; // The first DataTransfer we have the collection size (total)!
        DBResultSet rs = null;
        try {
            final var options = new SQLParameterParser(fileName, "target", "source", "ts=d", "priority=d", "checksum",
                    "groupby", "identity", "size=b", "replicated=?", "asap=?", "event=?", "deleted=?", "expired=?",
                    "proxy=?");
            rs = ecpds.getSortedDataTransfersByFilter(destination, target, stream, time, status, privilegedUser,
                    new Timestamp(scheduledBefore.getTime()), options.get(0, "DAT_TARGET"),
                    options.get(1, "DAF_ORIGINAL"), options.get(2, "DAT_TIME_STEP"), options.get(3, "DAT_PRIORITY"),
                    options.get(4, "DAF_CHECKSUM"), options.get(5, "DAF_GROUP_BY"), options.get(6, "DAT_IDENTITY"),
                    options.get(7, "DAT_SIZE"), options.get(8, "DAT_REPLICATED"), options.get(9, "DAT_ASAP"),
                    options.get(10, "DAT_EVENT"), options.get(11, "DAT_DELETED"),
                    options.get(12, "DAT_EXPIRY_TIME < UNIX_TIMESTAMP() * 1000"),
                    options.get(13, "HOS_NAME_PROXY is not null"), new Timestamp(from.getTime()),
                    new Timestamp(to.getTime()), cursor.getSort(), cursor.getOrder(), cursor.getStart(),
                    cursor.getLength());
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var file = new DataFile();
                file.setOriginal(rs.getString("DAF_ORIGINAL"));
                file.setId(rs.getLong("DAF_ID"));
                file.setSize(rs.getString("DAT_SIZE"));
                file.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                final var transfer = new DataTransfer();
                transfer.setId(rs.getLong("DAT_ID"));
                transfer.setDataFileId(file.getId());
                transfer.setDataFile(file);
                transfer.setSize(rs.getString("DAT_SIZE"));
                transfer.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                transfer.setDestinationName(rs.getString("DES_NAME"));
                transfer.setTransferServerName(rs.getString("TRS_NAME"));
                transfer.setTarget(rs.getString("DAT_TARGET"));
                transfer.setStatusCode(rs.getString("STA_CODE"));
                transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                transfer.setSent(rs.getString("DAT_SENT"));
                transfer.setDuration(rs.getString("DAT_DURATION"));
                transfer.setPriority(rs.getString("DAT_PRIORITY"));
                transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                transfer.setStartTime(rs.getTimestamp("DAT_START_TIME"));
                transfer.setFinishTime(rs.getTimestamp("DAT_FINISH_TIME"));
                transfer.setFailedTime(rs.getTimestamp("DAT_FAILED_TIME"));
                transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                transfer.setDeleted(rs.getBoolean("DAT_DELETED"));
                transfer.setReplicated(rs.getBoolean("DAT_REPLICATED"));
                transfer.setExpiryTime(rs.getTimestamp("DAT_EXPIRY_TIME"));
                _setHost(this, transfer, rs.getString("HOS_NAME_BACKUP"), hosts, _SET_HOS_NAME_BACKUP);
                _setHost(this, transfer, rs.getString("HOS_NAME_PROXY"), hosts, _SET_HOS_NAME_PROXY);
                _setHost(this, transfer, rs.getString("HOS_NAME"), hosts);
                final var used = cache.getFromCache(transfer);
                if (initialTransfer == null) {
                    initialTransfer = used;
                }
                array.add(used);
            }
        } catch (IOException | SQLException e) {
            _log.warn("getSortedDataTransfersByFilter", e);
            throw new DataBaseException("getSortedDataTransfersByFilter", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first
        // DataTransfer (if we have any)!
        if (initialTransfer != null && rs != null) {
            initialTransfer.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getSortedDataTransfersByFilter(" + cursor + ")", array.size());
        return array;
    }

    /**
     * Gets the data transfers by filter. This is used on the destination page, to filter the transfers!
     *
     * @param cache
     *            the data transfer cache
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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransfersByFilter(final DataTransferCache cache, final String destination,
            final String target, final String stream, final String time, final String status,
            final String privilegedUser, final Date scheduledBefore, final String fileName, final Date from,
            final Date to) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        DBResultSet rs = null;
        try {
            final var options = new SQLParameterParser(fileName, "target", "source", "ts=d", "priority=d", "checksum",
                    "groupby", "identity", "size=b", "replicated=?", "asap=?", "event=?", "deleted=?", "expired=?",
                    "proxy=?");
            rs = ecpds.getDataTransfersByFilter(destination, target, stream, time, status, privilegedUser,
                    new Timestamp(scheduledBefore.getTime()), options.get(0, "DAT_TARGET"),
                    options.get(1, "DAF_ORIGINAL"), options.get(2, "DAT_TIME_STEP"), options.get(3, "DAT_PRIORITY"),
                    options.get(4, "DAF_CHECKSUM"), options.get(5, "DAF_GROUP_BY"), options.get(6, "DAT_IDENTITY"),
                    options.get(7, "DAT_SIZE"), options.get(8, "DAT_REPLICATED"), options.get(9, "DAT_ASAP"),
                    options.get(10, "DAT_EVENT"), options.get(11, "DAT_DELETED"),
                    options.get(12, "DAT_EXPIRY_TIME < UNIX_TIMESTAMP() * 1000"),
                    options.get(13, "HOS_NAME_PROXY is not null"), new Timestamp(from.getTime()),
                    new Timestamp(to.getTime()));
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var file = new DataFile();
                file.setOriginal(rs.getString("DAF_ORIGINAL"));
                file.setId(rs.getLong("DAF_ID"));
                file.setSize(rs.getString("DAT_SIZE"));
                file.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                final var transfer = new DataTransfer();
                transfer.setId(rs.getLong("DAT_ID"));
                transfer.setDataFileId(file.getId());
                transfer.setDataFile(file);
                transfer.setSize(rs.getString("DAT_SIZE"));
                transfer.setTimeStep(rs.getLong("DAT_TIME_STEP"));
                transfer.setDestinationName(rs.getString("DES_NAME"));
                transfer.setTransferServerName(rs.getString("TRS_NAME"));
                transfer.setTarget(rs.getString("DAT_TARGET"));
                transfer.setStatusCode(rs.getString("STA_CODE"));
                transfer.setUserStatus(rs.getString("DAT_USER_STATUS"));
                transfer.setSent(rs.getString("DAT_SENT"));
                transfer.setDuration(rs.getString("DAT_DURATION"));
                transfer.setPriority(rs.getString("DAT_PRIORITY"));
                transfer.setRetryTime(rs.getTimestamp("DAT_RETRY_TIME"));
                transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                transfer.setStartTime(rs.getTimestamp("DAT_START_TIME"));
                transfer.setFinishTime(rs.getTimestamp("DAT_FINISH_TIME"));
                transfer.setFailedTime(rs.getTimestamp("DAT_FAILED_TIME"));
                transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                transfer.setDeleted(rs.getBoolean("DAT_DELETED"));
                transfer.setReplicated(rs.getBoolean("DAT_REPLICATED"));
                transfer.setExpiryTime(rs.getTimestamp("DAT_EXPIRY_TIME"));
                _setHost(this, transfer, rs.getString("HOS_NAME_BACKUP"), hosts, _SET_HOS_NAME_BACKUP);
                _setHost(this, transfer, rs.getString("HOS_NAME_PROXY"), hosts, _SET_HOS_NAME_PROXY);
                _setHost(this, transfer, rs.getString("HOS_NAME"), hosts);
                array.add(cache.getFromCache(transfer));
            }
        } catch (IOException | SQLException e) {
            _log.warn("getDataTransfersByFilter", e);
            throw new DataBaseException("getDataTransfersByFilter", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        logSqlRequest("getDataTransfersByFilter", array.size());
        return array;
    }

    /**
     * Gets the hosts with the provided criteria.
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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    @SuppressWarnings("null")
    public Collection<Host> getFilteredHosts(final String label, final String filter, final String network,
            final String type, final String search, final DataBaseCursor cursor) throws DataBaseException {
        final List<Host> array = new ArrayList<>();
        Host initialHost = null; // In the first Host we set the collection size (total)!
        DBResultSet rs = null;
        try {
            final var options = new SQLParameterParser(search, "nickname", "id=d", "login", "comment", "options", "dir",
                    "hostname", "enabled=?", "method", "email", "password");
            rs = ecpds.getFilteredHosts(label, filter, network, type, options.get(1, "HOS_NAME"),
                    options.get(2, "HOS_LOGIN"), options.get(0, "HOS_NICKNAME"), options.get(3, "HOS_COMMENT"),
                    options.get(4, "HOS_DATA"), options.get(5, "HOS_DIR"), options.get(6, "HOS_HOST"),
                    options.get(7, "HOS_ACTIVE"), options.get(8, "TME_NAME"), options.get(9, "HOS_USER_MAIL"),
                    options.get(10, "HOS_PASSWD"), cursor.getSort(), cursor.getOrder(), cursor.getStart(),
                    cursor.getLength());
            while (rs.next()) {
                final var host = new Host();
                if (initialHost == null) {
                    initialHost = host;
                }
                host.setHost(rs.getString("HOS_HOST"));
                host.setName(rs.getString("HOS_NAME"));
                host.setType(rs.getString("HOS_TYPE"));
                host.setActive(rs.getBoolean("HOS_ACTIVE"));
                host.setTransferGroupName(rs.getString("TRG_NAME"));
                host.setTransferMethodName(rs.getString("TME_NAME"));
                host.setNetworkName(rs.getString("HOS_NETWORK_NAME"));
                host.setNickname(rs.getString("HOS_NICKNAME"));
                array.add(host);
            }
        } catch (IOException | SQLException e) {
            _log.warn("getFilteredHosts", e);
            throw new DataBaseException("getFilteredHosts", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first
        // TransferHistory (if we have any)!
        if (initialHost != null) {
            initialHost.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getFilteredHosts(" + cursor + ")", array.size());
        return array;
    }

    /**
     * Gets the hosts by destination id.
     *
     * @param destId
     *            the dest id
     *
     * @return the hosts by destination id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<Host> getHostsByDestinationId(final String destId) throws DataBaseException {
        final List<Host> array = new ArrayList<>();
        DBIterator<Host> it = null;
        try {
            it = ecpds.getDestinationHost(destId, Host.class);
            while (it.hasNext()) {
                array.add(it.next());
            }
        } catch (IOException | SQLException e) {
            _log.warn("getHostsByDestinationId", e);
            throw new DataBaseException("getHostsByDestinationId", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getHostsByDestinationId", array.size());
        return array;
    }

    /**
     * Gets the hosts by transfer method id.
     *
     * @param transferMethodId
     *            the transfer method id
     *
     * @return the hosts by transfer method id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<Host> getHostsByTransferMethodId(final String transferMethodId) throws DataBaseException {
        final List<Host> array = new ArrayList<>();
        DBIterator<Host> it = null;
        try {
            it = ecpds.getHostsByTransferMethodId(transferMethodId, Host.class);
            while (it.hasNext()) {
                array.add(it.next());
            }
        } catch (IOException | SQLException e) {
            _log.warn("getHostsByTransferMethodId", e);
            throw new DataBaseException("getHostsByTransferMethodId", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getHostsByTransferMethodId", array.size());
        return array;
    }

    /**
     * Gets the hosts to check.
     *
     * @return the hosts to check
     */
    public Host[] getHostsToCheck() {
        final List<Host> list = new ArrayList<>();
        DBIterator<Host> it = null;
        try {
            it = ecpds.getHostsToCheck(Host.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (IOException | SQLException e) {
            _log.warn("getHostsToCheck", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getHostsToCheck", list.size());
        return list.toArray(new Host[list.size()]);
    }

    /**
     * Gets the transfer methods by ec trans module name.
     *
     * @param ecTransModuleName
     *            the ec trans module name
     *
     * @return the transfer methods by ec trans module name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<TransferMethod> getTransferMethodsByEcTransModuleName(final String ecTransModuleName)
            throws DataBaseException {
        final List<TransferMethod> array = new ArrayList<>();
        DBIterator<TransferMethod> it = null;
        try {
            it = ecpds.getTransferMethodsByEcTransModuleName(ecTransModuleName, TransferMethod.class);
            while (it.hasNext()) {
                array.add(it.next());
            }
        } catch (IOException | SQLException e) {
            _log.warn("getTransferMethodsByEcTransModuleName", e);
            throw new DataBaseException("getTransferMethodsByEcTransModuleName", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getTransferMethodsByEcTransModuleName", array.size());
        return array;
    }

    /**
     * Gets the meta data by data file id.
     *
     * @param dataFileId
     *            the dataFile id
     *
     * @return the meta data by data file id
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<MetadataValue> getMetaDataByDataFileId(final long dataFileId) throws DataBaseException {
        final List<MetadataValue> array = new ArrayList<>();
        DBIterator<MetadataValue> it = null;
        try {
            it = ecpds.getMetaDataByDataFile(dataFileId, MetadataValue.class);
            while (it.hasNext()) {
                array.add(it.next());
            }
            Collections.sort(array, (o1, o2) -> Integer.compare(o2.getMetadataAttributeName().length(),
                    o1.getMetadataAttributeName().length()));
        } catch (IOException | SQLException e) {
            _log.warn("getMetaDataByDataFileId", e);
            throw new DataBaseException("getMetaDataByDataFileId", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getMetaDataByDataFileId", array.size());
        return array;
    }

    /**
     * Gets the meta data by attribute name.
     *
     * @param id
     *            the id
     *
     * @return the meta data by attribute name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<MetadataValue> getMetaDataByAttributeName(final String id) throws DataBaseException {
        final List<MetadataValue> array = new ArrayList<>();
        try (var rs = ecpds.getMetaDataByAttribute(id)) {
            while (rs.next()) {
                final var val = new MetadataValue();
                val.setValue(rs.getString("MEV_VALUE"));
                val.setMetadataAttributeName(id);
                array.add(val);
            }
        } catch (IOException | SQLException e) {
            _log.warn("getMetaDataByAttributeName", e);
            throw new DataBaseException("getMetaDataByAttributeName", e);
        }
        logSqlRequest("getMetaDataByAttributeName", array.size());
        return array;
    }

    /**
     * Gets the data transfers by destination.
     *
     * @param destinationName
     *            the Destination name
     *
     * @return the data transfers by destination
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Iterator<DataTransfer> getDataTransfersByDestination(final String destinationName) throws DataBaseException {
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByDestination(destinationName, DataTransfer.class);
        } catch (IOException | SQLException e) {
            _log.warn("getDataTransfersByDestination", e);
            throw new DataBaseException("getDataTransfersByDestination", e);
        }
        return it;
    }

    /**
     * Reset the re-queued transfers per destination.
     *
     * @param destination
     *            the destination
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int resetRequeuedTransfersPerDestination(final Destination destination) throws DataBaseException {
        try {
            return ecpds.resetDataTransferQueueAndRetryTimeToScheduledTime(destination.getName());
        } catch (IOException | SQLException e) {
            _log.warn("resetRequeuedTransfersPerDestination", e);
            throw new DataBaseException("resetRequeuedTransfersPerDestination", e);
        }
    }

    /**
     * Gets the data transfers by transfer server.
     *
     * @param server
     *            the server
     *
     * @return the data transfers by transfer server
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Iterator<DataTransfer> getDataTransfersByTransferServer(final TransferServer server)
            throws DataBaseException {
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByTransferServer(server.getName(), DataTransfer.class);
        } catch (IOException | SQLException e) {
            _log.warn("getDataTransfersByTransferServer", e);
            throw new DataBaseException("getDataTransfersByTransferServer", e);
        }
        return it;
    }

    /**
     * Gets the data transfers by destination and identity.
     *
     * @param cache
     *            the data transfer cache
     * @param destination
     *            the destination
     * @param identity
     *            the identity
     *
     * @return the data transfers by destination and identity
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransfersByDestinationAndIdentity(final DataTransferCache cache,
            final String destination, final String identity) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByDestinationAndIdentity(destination, identity, DataTransfer.class);
            while (it.hasNext()) {
                array.add(cache.getFromCache(it.next()));
            }
        } catch (IOException | SQLException e) {
            _log.warn("getDataTransfersByDestinationAndIdentity", e);
            throw new DataBaseException("getDataTransfersByDestinationAndIdentity", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDataTransfersByDestinationAndIdentity", array.size());
        return array;
    }

    /**
     * Gets the transfer count with destination and metadata value by metadata name.
     *
     * @param metadataName
     *            the metadata name
     *
     * @return the transfer count with destination and metadata value by metadata name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<List<String>> getTransferCountWithDestinationAndMetadataValueByMetadataName(
            final String metadataName) throws DataBaseException {
        final List<List<String>> results = new ArrayList<>();
        try (var rs = ecpds.getDataTransferCountDestinationAndMetadataValueByMetadataName(metadataName)) {
            while (rs.next()) {
                final List<String> p = new ArrayList<>(3);
                p.add(rs.getString("destination"));
                p.add(rs.getString("value"));
                p.add(rs.getString("count"));
                results.add(p);
            }
        } catch (IOException | SQLException e) {
            _log.warn("getTransferCountWithDestinationAndMetadataValueByMetadataName");
            throw new DataBaseException("getTransferCountWithDestinationAndMetadataValueByMetadataName", e);
        }
        logSqlRequest("getTransferCountWithDestinationAndMetadataValueByMetadataName", results.size());
        return results;
    }

    /**
     * Gets the data transfers by destination product and time on date.
     *
     * @param cache
     *            the data transfer cache
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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransfersByDestinationProductAndTimeOnDate(final DataTransferCache cache,
            final String destinationName, final String product, final String time, final Date fromIsoDate,
            final Date toIsoDate) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByDestinationProductAndTimeOnDate(destinationName, product, time,
                    new Timestamp(fromIsoDate.getTime()), new Timestamp(toIsoDate.getTime()), DataTransfer.class);
            while (it.hasNext()) {
                array.add(cache.getFromCache(it.next()));
            }
        } catch (IOException | SQLException e) {
            _log.warn("getDataTransfersByDestinationProductAndTimeOnDate", e);
            throw new DataBaseException("getDataTransfersByDestinationProductAndTimeOnDate", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDataTransfersByDestinationProductAndTimeOnDate", array.size());
        return array;
    }

    /**
     * Gets the data transfers by destination on date.
     *
     * @param cache
     *            the data transfer cache
     * @param destinationName
     *            the destination name
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     *
     * @return the data transfers by destination on date
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransfersByDestinationOnDate(final DataTransferCache cache,
            final String destinationName, final Date fromIsoDate, final Date toIsoDate) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByDestinationAndTargetOnDate(destinationName, null,
                    new Timestamp(fromIsoDate.getTime()), new Timestamp(toIsoDate.getTime()), DataTransfer.class);
            while (it.hasNext()) {
                array.add(cache.getFromCache(it.next()));
            }
        } catch (IOException | SQLException e) {
            _log.warn("getDataTransfersByDestinationOnDate", e);
            throw new DataBaseException("getDataTransfersByDestinationOnDate", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDataTransfersByDestinationOnDate", array.size());
        return array;
    }

    /**
     * Gets the data transfers by destination on date iterator.
     *
     * @param destinationName
     *            the destination name
     * @param target
     *            the target
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     *
     * @return the data transfers by destination on date iterator
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Iterator<DataTransfer> getDataTransfersByDestinationAndTargetOnDateIterator(final String destinationName,
            final String target, final Date fromIsoDate, final Date toIsoDate) throws DataBaseException {
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByDestinationAndTargetOnDate(destinationName, target,
                    new Timestamp(fromIsoDate.getTime()), new Timestamp(toIsoDate.getTime()), DataTransfer.class);
        } catch (IOException | SQLException e) {
            _log.warn("getDataTransfersByDestinationAndTargetOnDateIterator", e);
            throw new DataBaseException("getDataTransfersByDestinationAndTargetOnDateIterator", e);
        }
        return it;
    }

    /**
     * Gets the data transfers by destination on date iterator (v2).
     *
     * @param destinationName
     *            the destination name
     * @param target
     *            the target
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     * @param sort
     *            the sort
     * @param order
     *            the order
     *
     * @return the data transfers by destination on date iterator
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<DataTransfer> getDataTransfersByDestinationAndTargetOnDateIterator2(final String destinationName,
            final String target, final Date fromIsoDate, final Date toIsoDate, final int sort, final int order)
            throws IOException, SQLException {
        return new Iterator<>() {
            final DBResultSet rs = ecpds.getDataTransfersByDestinationAndTargetOnDate2(destinationName, target,
                    new Timestamp(fromIsoDate.getTime()), new Timestamp(toIsoDate.getTime()), sort, order);

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (final SQLException e) {
                    return false;
                }
            }

            @Override
            public DataTransfer next() {
                DataTransfer transfer = null;
                try {
                    transfer = new DataTransfer();
                    transfer.setId(rs.getLong("DAT_ID"));
                    transfer.setSize(rs.getString("DAT_SIZE"));
                    transfer.setTarget(rs.getString("DAT_TARGET"));
                    transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                    transfer.setQueueTime(rs.getTimestamp("DAT_QUEUE_TIME"));
                } catch (final SQLException e) {
                    _log.warn("getDataTransfersByDestinationAndTargetOnDateIterator2", e);
                }
                return transfer;
            }

            @Override
            public void remove() {
                if (rs != null) {
                    rs.close();
                }
            }
        };
    }

    /**
     * Gets the data transfers by destination on transmission date.
     *
     * @param cache
     *            the data transfer cache
     * @param destinationName
     *            the destination name
     * @param fromIsoDate
     *            the from iso date
     * @param toIsoDate
     *            the to iso date
     *
     * @return the data transfers by destination on transmission date
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getDataTransfersByDestinationOnTransmissionDate(final DataTransferCache cache,
            final String destinationName, final Date fromIsoDate, final Date toIsoDate) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByDestinationAndTargetOnTransmissionDate(destinationName, null,
                    new Timestamp(fromIsoDate.getTime()), new Timestamp(toIsoDate.getTime()), DataTransfer.class);
            while (it.hasNext()) {
                array.add(cache.getFromCache(it.next()));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDataTransfersByDestinationOnTransmissionDate", e);
            throw new DataBaseException("getDataTransfersByDestinationOnTransmissionDate", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getDataTransfersByDestinationOnTransmissionDate", array.size());
        return array;
    }

    /**
     * Gets the dates by destination and target on date.
     *
     * @param destinationName
     *            the destination name
     * @param order
     *            the order
     *
     * @return the dates by destination and target on date
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Long[] getDatesByDestinationAndTargetOnDate(final String destinationName, final int order)
            throws DataBaseException {
        final List<Long> list = new ArrayList<>();
        try (var rs = ecpds.getDatesByDestinationAndTargetOnDate(destinationName, order)) {
            while (rs.next()) {
                list.add(rs.getLong("DAF_TIME_BASE"));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getDatesByDestinationAndTargetOnDate", e);
            throw new DataBaseException("getDatesByDestinationAndTargetOnDate", e);
        }
        logSqlRequest("getDatesByDestinationAndTargetOnDate", list.size());
        return list.toArray(new Long[list.size()]);
    }

    /**
     * Gets the data transfers by destination and target iterator.
     *
     * @param destinationName
     *            the destination name
     * @param target
     *            the target
     * @param runnable
     *            the runnable
     *
     * @return the data transfers by destination and target iterator
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Iterator<DataTransfer> getDataTransfersByDestinationAndTargetIterator(final String destinationName,
            final String target, final boolean runnable) throws DataBaseException {
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getDataTransfersByDestinationAndTarget(destinationName, target, runnable, DataTransfer.class);
        } catch (SQLException | IOException e) {
            _log.warn("getDataTransfersByDestinationAndTargetIterator", e);
            throw new DataBaseException("getDataTransfersByDestinationAndTargetIterator", e);
        }
        return it;
    }

    /**
     * Gets the data transfers by destination and target iterator (v2).
     *
     * @param destinationName
     *            the destination name
     * @param target
     *            the target
     * @param runnable
     *            the runnable
     * @param sort
     *            the sort
     * @param order
     *            the order
     *
     * @return the data transfers by destination and target iterator
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public Iterator<DataTransfer> getDataTransfersByDestinationAndTargetIterator2(final String destinationName,
            final String target, final boolean runnable, final int sort, final int order)
            throws IOException, SQLException {
        return new Iterator<>() {
            final DBResultSet rs = ecpds.getDataTransfersByDestinationAndTarget2(destinationName, target, runnable,
                    sort, order);

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (final SQLException e) {
                    return false;
                }
            }

            @Override
            public DataTransfer next() {
                DataTransfer transfer = null;
                try {
                    transfer = new DataTransfer();
                    transfer.setId(rs.getLong("DAT_ID"));
                    transfer.setSize(rs.getString("DAT_SIZE"));
                    transfer.setTarget(rs.getString("DAT_TARGET"));
                    transfer.setScheduledTime(rs.getTimestamp("DAT_SCHEDULED_TIME"));
                } catch (final SQLException e) {
                    _log.warn("getDataTransfersByDestinationAndTargetIterator2", e);
                }
                return transfer;
            }

            @Override
            public void remove() {
                if (rs != null) {
                    rs.close();
                }
            }
        };
    }

    /**
     * Gets the bad data transfers by destination.
     *
     * @param cache
     *            the data transfer cache
     * @param destinationName
     *            the destination name
     *
     * @return the bad data transfers by destination
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<DataTransfer> getBadDataTransfersByDestination(final DataTransferCache cache,
            final String destinationName) throws DataBaseException {
        final List<DataTransfer> array = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getBadDataTransfersByDestination(destinationName, DataTransfer.class);
            while (it.hasNext()) {
                array.add(cache.getFromCache(it.next()));
            }
        } catch (SQLException | IOException e) {
            _log.warn("getBadDataTransfersByDestination", e);
            throw new DataBaseException("getBadDataTransfersByDestination", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getBadDataTransfersByDestination", array.size());
        return array;
    }

    /**
     * Gets the bad data transfers by destination count.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the bad data transfers by destination count
     */
    public int getBadDataTransfersByDestinationCount(final String destinationName) {
        try {
            return ecpds.getBadDataTransfersByDestinationCount(destinationName);
        } catch (SQLException | IOException e) {
            _log.warn("getBadDataTransfersByDestinationCount", e);
        }
        return -1;
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int getDataTransferCountNotDoneByProductAndTimeOnDate(final String destination, final String product,
            final String time, final Date from, final Date to) throws DataBaseException {
        try {
            return ecpds.getDataTransferCountNotDoneByDestinationProductAndTimeOnDate(destination, product, time,
                    new Timestamp(System.currentTimeMillis()), new Timestamp(from.getTime()),
                    new Timestamp(to.getTime()));
        } catch (SQLException | IOException e) {
            _log.warn("getDataTransferCountNotDoneByProductAndTimeOnDate", e);
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Gets the sorted transfer history by destination on product date.
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
     * @return the sorted transfer history by destination on product date
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    @SuppressWarnings("null")
    public Collection<TransferHistory> getSortedTransferHistoryByDestinationOnProductDate(final String destinationName,
            final Date fromIsoDate, final Date toIsoDate, final DataBaseCursor cursor) throws DataBaseException {
        final List<TransferHistory> array = new ArrayList<>();
        TransferHistory initialHistory = null; // In the first TransferHistory we set the collection size (total)!
        DBResultSet rs = null;
        try {
            rs = ecpds.getSortedTransferHistoryPerDestinationOnProductDate(destinationName,
                    new Timestamp(fromIsoDate.getTime()), new Timestamp(toIsoDate.getTime()), cursor.getSort(),
                    cursor.getOrder(), cursor.getStart(), cursor.getLength());
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var history = new TransferHistory();
                if (initialHistory == null) {
                    initialHistory = history;
                }
                history.setId(rs.getLong("TRH_ID"));
                history.setDestinationName(rs.getString("DES_NAME"));
                history.setComment(rs.getString("TRH_COMMENT"));
                history.setDataTransferId(rs.getLong("DAT_ID"));
                history.setError(rs.getBoolean("TRH_ERROR"));
                history.setSent(rs.getLong("TRH_SENT"));
                history.setTime(rs.getTimestamp("TRH_TIME"));
                history.setStatusCode(rs.getString("STA_CODE"));
                _setHost(this, history, rs.getString("HOS_NAME"), hosts);
                array.add(history);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getSortedTransferHistoryByDestinationOnProductDate", e);
            throw new DataBaseException("getSortedTransferHistoryByDestinationOnProductDate", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first
        // TransferHistory (if we have any)!
        if (initialHistory != null) {
            initialHistory.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getSortedTransferHistoryByDestinationOnProductDate(" + cursor + ")", array.size());
        return array;
    }

    /**
     * Gets the sorted transfer history by destination on history date.
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
     * @return the sorted transfer history by destination on history date
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    @SuppressWarnings("null")
    public Collection<TransferHistory> getSortedTransferHistoryByDestinationOnHistoryDate(final String destinationName,
            final Date fromIsoDate, final Date toIsoDate, final DataBaseCursor cursor) throws DataBaseException {
        final List<TransferHistory> array = new ArrayList<>();
        TransferHistory initialHistory = null; // In the first TransferHistory we set the collection size (total)!
        DBResultSet rs = null;
        try {
            rs = ecpds.getSortedTransferHistoryPerDestinationOnHistoryDate(destinationName,
                    new Timestamp(fromIsoDate.getTime()), new Timestamp(toIsoDate.getTime()), cursor.getSort(),
                    cursor.getOrder(), cursor.getStart(), cursor.getLength());
            final var hosts = new HashMap<String, Host>();
            while (rs.next()) {
                final var history = new TransferHistory();
                if (initialHistory == null) {
                    initialHistory = history;
                }
                history.setId(rs.getLong("TRH_ID"));
                history.setDestinationName(rs.getString("DES_NAME"));
                history.setComment(rs.getString("TRH_COMMENT"));
                history.setDataTransferId(rs.getLong("DAT_ID"));
                history.setError(rs.getBoolean("TRH_ERROR"));
                history.setSent(rs.getLong("TRH_SENT"));
                history.setTime(rs.getTimestamp("TRH_TIME"));
                history.setStatusCode(rs.getString("STA_CODE"));
                _setHost(this, history, rs.getString("HOS_NAME"), hosts);
                array.add(history);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getSortedTransferHistoryByDestinationOnHistoryDate", e);
            throw new DataBaseException("getSortedTransferHistoryByDestinationOnHistoryDate", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first
        // TransferHistory (if we have any)!
        if (initialHistory != null) {
            initialHistory.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getSortedTransferHistoryByDestinationOnHistoryDate(" + cursor + ")", array.size());
        return array;
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<Event> getECuserEvents(final String userName, final Date onIsoDate, final String search,
            final DataBaseCursor cursor) throws DataBaseException {
        final var date = new java.sql.Date(onIsoDate.getTime());
        Event initialEvent = null; // In the first Event we set the collection size (total)!
        final List<Event> array = new ArrayList<>();
        DBResultSet rs = null;
        try {
            rs = ecpds.getECUserEvents(getSQLLikeFormattedString(userName), date, getSQLLikeFormattedString(search),
                    cursor.getSort(), cursor.getOrder(), cursor.getStart(), cursor.getEnd(), cursor.getLength());
            while (rs.next()) {
                final var event = new Event();
                if (initialEvent == null) {
                    initialEvent = event;
                }
                event.setDate(date);
                event.setTime(rs.getTime("EVE_TIME"));
                event.setAction(rs.getString("EVE_ACTION"));
                event.setComment(rs.getString("EVE_COMMENT"));
                final var activity = new Activity();
                activity.setPlugin(rs.getString("ACT_PLUGIN"));
                activity.setHost(rs.getString("ACT_HOST"));
                activity.setAgent(rs.getString("ACT_AGENT"));
                activity.setECUser(new ECUser(rs.getString("ECU_NAME")));
                event.setActivity(activity);
                array.add(event);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getECuserEvents", e);
            throw new DataBaseException("getECuserEvents", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first Event (if we have
        // any)!
        if (initialEvent != null && rs != null) {
            initialEvent.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getECuserEvents(" + cursor + ")", array.size());
        return array;

    }

    /**
     * Gets the incoming history list.
     *
     * @param incomingUserName
     *            the incoming user name
     * @param onIsoDate
     *            the on iso date
     * @param search
     *            the search
     * @param cursor
     *            the cursor
     *
     * @return the incoming history list
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<IncomingHistory> getIncomingHistory(final String incomingUserName, final Date onIsoDate,
            final String search, final DataBaseCursor cursor) throws DataBaseException {
        IncomingHistory initialHistory = null; // In the first IncomingHistory we set the collection size (total)!
        final List<IncomingHistory> array = new ArrayList<>();
        DBResultSet rs = null;
        try {
            rs = ecpds.getIncomingHistory(getSQLLikeFormattedString(incomingUserName),
                    new Timestamp(DateUtil.getStartOfDay(onIsoDate).getTime()),
                    new Timestamp(DateUtil.getEndOfDay(onIsoDate).getTime()), getSQLLikeFormattedString(search),
                    cursor.getSort(), cursor.getOrder(), cursor.getStart(), cursor.getEnd(), cursor.getLength());
            while (rs.next()) {
                final var event = new IncomingHistory();
                if (initialHistory == null) {
                    initialHistory = event;
                }
                event.setUserName(rs.getString("INH_USER_NAME"));
                event.setStartTime(rs.getTimestamp("INH_START_TIME"));
                event.setDataTransferId(rs.getLong("DAT_ID"));
                event.setDestination(rs.getString("INH_DESTINATION"));
                event.setFileName(rs.getString("INH_FILE_NAME"));
                event.setFileSize(rs.getLong("INH_FILE_SIZE"));
                event.setDuration(rs.getLong("INH_DURATION"));
                event.setSent(rs.getLong("INH_SENT"));
                event.setProtocol(rs.getString("INH_PROTOCOL"));
                event.setTransferServer(rs.getString("INH_TRANSFER_SERVER"));
                event.setHostAddress(rs.getString("INH_HOST_ADDRESS"));
                event.setUpload(rs.getBoolean("INH_UPLOAD"));
                array.add(event);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getIncomingHistory", e);
            throw new DataBaseException("getIncomingHistory", e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // Let's set the full size of the Collection in the first
        // IncomingHistory (if we have any)!
        if (initialHistory != null && rs != null) {
            initialHistory.setCollectionSize(rs.getFoundRows(cursor));
        }
        logSqlRequest("getIncomingHistory(" + cursor + ")", array.size());
        return array;
    }

    /**
     * Gets the allowed ec users by host name.
     *
     * @param hostName
     *            the host name
     *
     * @return the allowed ec users by host name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Collection<ECUser> getAllowedEcUsersByHostName(final String hostName) throws DataBaseException {
        final List<ECUser> array = new ArrayList<>();
        DBIterator<ECUser> it = null;
        try {
            it = ecpds.getAllowedEcUsersByHostName(hostName, ECUser.class);
            while (it.hasNext()) {
                array.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getAllowedEcUsersByHostName", e);
            throw new DataBaseException("getAllowedEcUsersByHostName", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getAllowedEcUsersByHostName", array.size());
        return array;
    }

    /**
     * Gets the traffic by destination name.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the traffic
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public List<Traffic> getTrafficByDestinationName(final String destinationName) throws DataBaseException {
        final List<Traffic> array = new ArrayList<>();
        try (var rs = ecpds.getTrafficByDestinationName(destinationName)) {
            while (rs.next()) {
                final var traffic = new Traffic();
                traffic.setDate(rs.getString("DATE"));
                traffic.setBytes(rs.getLong("BYTES"));
                traffic.setDuration(rs.getLong("DURATION"));
                traffic.setFiles(rs.getInt("FILES"));
                array.add(traffic);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getTrafficByDestinationName", e);
            throw new DataBaseException("getTrafficByDestinationName", e);
        }
        logSqlRequest("getTrafficByDestinationName", array.size());
        return array;
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public List<ChangeLog> getChangeLogByKey(final String keyName, final String keyValue) throws DataBaseException {
        final List<ChangeLog> list = new ArrayList<>();
        DBIterator<ChangeLog> it = null;
        try {
            it = ecpds.getChangeLogByKey(keyName, keyValue, ChangeLog.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getChangeLogByKey", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getChangeLogByKey", list.size());
        return list;
    }

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
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ProductStatus[] getProductStatus(final String stream, final String time, final String type, final long step,
            final int limit) throws DataBaseException {
        final List<ProductStatus> list = new ArrayList<>();
        try (var rs = ecpds.getProductStatus(stream, time, type, step, limit)) {
            while (rs.next()) {
                final var product = new ProductStatus();
                product.setId(rs.getLong("PRS_ID"));
                product.setStream(rs.getString("PRS_STREAM"));
                product.setStep(rs.getInt("PRS_STEP"));
                product.setType(rs.getString("PRS_TYPE"));
                product.setTime(rs.getString("PRS_TIME"));
                product.setBuffer(rs.getLong("PRS_BUFFER"));
                product.setScheduleTime(rs.getTimestamp("PRS_SCHEDULE_TIME"));
                product.setLastUpdate(rs.getTimestamp("PRS_LAST_UPDATE"));
                product.setTimeBase(rs.getTimestamp("PRS_TIME_BASE"));
                product.setStatusCode(rs.getString("STA_CODE"));
                list.add(product);
            }
        } catch (SQLException | IOException e) {
            _log.warn("getProductStatus", e);
        }
        logSqlRequest("getProductStatus", list.size());
        return list.toArray(new ProductStatus[list.size()]);
    }

    /**
     * Gets the initial product status events.
     *
     * @return the initial product status events iterator
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Iterator<ProductStatus> getInitialProductStatusEventsIterator()
            throws DataBaseException, SQLException, IOException {
        return ecpds.getInitialProductStatusEvents(ProductStatus.class);
    }

    /**
     * Gets the initial product status events.
     *
     * @return the initial product status events
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ProductStatus[] getInitialProductStatusEvents() throws DataBaseException {
        final List<ProductStatus> list = new ArrayList<>();
        DBIterator<ProductStatus> it = null;
        try {
            it = ecpds.getInitialProductStatusEvents(ProductStatus.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getInitialProductStatusEvents", e);
            throw new DataBaseException("getInitialProductStatusEvents", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getInitialProductStatusEvents", list.size());
        return list.toArray(new ProductStatus[list.size()]);
    }

    /**
     * Gets the interrupted transfers.
     *
     * @return the interrupted transfers
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public DataTransfer[] getInterruptedTransfers() throws DataBaseException {
        final List<DataTransfer> list = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getInterruptedTransfers(DataTransfer.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getInterruptedTransfers", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getInterruptedTransfers", list.size());
        return list.toArray(new DataTransfer[list.size()]);
    }

    /**
     * Gets the interrupted transfers per destination.
     *
     * @param destination
     *            the destination
     *
     * @return the interrupted transfers per destination
     */
    public DataTransfer[] getInterruptedTransfersPerDestination(final Destination destination) {
        final List<DataTransfer> list = new ArrayList<>();
        DBIterator<DataTransfer> it = null;
        try {
            it = ecpds.getInterruptedTransfersPerDestination(destination.getName(), DataTransfer.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
        } catch (SQLException | IOException e) {
            _log.warn("getInterruptedTransfersPerDestination", e);
        } finally {
            if (it != null) {
                it.remove();
            }
        }
        logSqlRequest("getInterruptedTransfersPerDestination", list.size());
        return list.toArray(new DataTransfer[list.size()]);
    }

    /**
     * Reset product status.
     *
     * @param metaStream
     *            the meta stream
     * @param metaTime
     *            the meta time
     * @param timeStep
     *            the time step
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int resetProductStatus(final String metaStream, final String metaTime, final long timeStep)
            throws DataBaseException {
        try {
            return ecpds.resetProductStatus(metaStream, metaTime, timeStep);
        } catch (SQLException | IOException e) {
            _log.warn("resetProductStatus", e);
            throw new DataBaseException("resetProductStatus", e);
        }
    }

    /**
     * Removes the destination.
     *
     * @param destinationName
     *            the Destination name
     * @param remove
     *            the remove
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int removeDestination(final String destinationName, final boolean remove) throws DataBaseException {
        try {
            return ecpds.removeDestination(destinationName, remove);
        } catch (SQLException | IOException e) {
            _log.warn("removeDestination", e);
            throw new DataBaseException("removeDestination", e);
        }
    }

    /**
     * Removes the host.
     *
     * @param host
     *            the host
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int removeHost(final Host host) throws DataBaseException {
        try {
            return ecpds.removeHost(host.getName());
        } catch (SQLException | IOException e) {
            _log.warn("removeHost", e);
            throw new DataBaseException("removeHost", e);
        }
    }

    /**
     * Removes the web user.
     *
     * @param user
     *            the web user
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int removeWebUser(final WebUser user) throws DataBaseException {
        try {
            return ecpds.removeWebUser(user.getId());
        } catch (SQLException | IOException e) {
            _log.warn("removeWebUser", e);
            throw new DataBaseException("removeWebUser", e);
        }
    }

    /**
     * Removes the category.
     *
     * @param category
     *            the category
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int removeCategory(final Category category) throws DataBaseException {
        try {
            return ecpds.removeCategory(String.valueOf(category.getId()));
        } catch (SQLException | IOException e) {
            _log.warn("removeCategory", e);
            throw new DataBaseException("removeCategory", e);
        }
    }

    /**
     * Removes the url.
     *
     * @param url
     *            the url
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int removeUrl(final Url url) throws DataBaseException {
        try {
            return ecpds.removeUrl(url.getName());
        } catch (SQLException | IOException e) {
            _log.warn("removeUrl", e);
            throw new DataBaseException("removeUrl", e);
        }
    }

    /**
     * Removes the IncomingUser.
     *
     * @param user
     *            the incoming user
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int removeIncomingUser(final IncomingUser user) throws DataBaseException {
        try {
            return ecpds.removeIncomingUser(user.getId());
        } catch (SQLException | IOException e) {
            _log.warn("removeIncomingUser", e);
            throw new DataBaseException("removeIncomingUser", e);
        }
    }

    /**
     * Removes the IncomingPolicy.
     *
     * @param policy
     *            the incoming policy
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public int removeIncomingPolicy(final IncomingPolicy policy) throws DataBaseException {
        try {
            return ecpds.removeIncomingPolicy(policy.getId());
        } catch (SQLException | IOException e) {
            _log.warn("removeIncomingPolicy", e);
            throw new DataBaseException("removeIncomingPolicy", e);
        }
    }
}
