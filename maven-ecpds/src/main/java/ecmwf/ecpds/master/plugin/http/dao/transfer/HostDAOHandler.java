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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.ChangeLog;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.HostECUser;
import ecmwf.common.database.HostLocation;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.HostChangeLog;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.HostsSearch;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.model.users.User;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.bean.Pair;
import ecmwf.web.util.search.BooleanExpressionException;

/**
 * The Class HostDAOHandler.
 */
public class HostDAOHandler extends PDSDAOBase implements DAOHandler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(HostDAOHandler.class);

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
        final var host = new ecmwf.common.database.Host();
        host.setHostLocation(new HostLocation());
        return new HostBean(host);
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
            return new HostBean(MasterManager.getDB().getHost(key));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Problem searching by key '" + key + "'", e);
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
        try {
            if (search instanceof final HostsSearch s) {
                return convertToModelBeanCollection(MasterManager.getDB().getFilteredHosts(s.getLabel(), s.getFilter(),
                        s.getNetwork(), s.getType(), s.getSearch(), s.getDataBaseCursor()));
            }
            if (search instanceof final HostChangeLog s) {
                return convertArrayToChangeLogBeanCollection(
                        MasterManager.getDB().getHost(s.getHost().getName()).toString(false),
                        MasterManager.getDB().getChangeLogByKey("HOS_NAME", s.getHost().getName()));
            } else if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getHostArray());
            } else {
                try {
                    final var clauses = getEqualityClauseLeavesFromExpression(search);
                    if (clauses.size() == 1) {
                        if (clauses.containsKey("destination")) {
                            return convertToModelBeanCollection(
                                    MasterManager.getDB().getHostsByDestinationId(clauses.get("destination")));
                        } else if (clauses.containsKey("transferMethod")) {
                            return convertToModelBeanCollection(
                                    MasterManager.getDB().getHostsByTransferMethodId(clauses.get("transferMethod")));
                        } else {
                            throw new DAOException("Search by attribute '" + clauses + "' not supported");
                        }

                    } else if (clauses.size() == 2) {
                        if (clauses.containsKey("destination") && clauses.containsKey("priority")) {
                            final var destinationName = clauses.get("destination");
                            return convertToPairWithPriority(
                                    MasterManager.getDB().getHostsByDestinationId(destinationName), destinationName);
                        } else {
                            throw new DAOException("Search by attribute '" + clauses + "' not supported");
                        }
                    } else {
                        throw new DAOException("'find' method with query '" + search.getQuery()
                                + "' not supported! Use only 1 clause");
                    }

                } catch (final BooleanExpressionException e) {
                    throw new DAOException("Bad find expression", e);
                }
            }
        } catch (final Exception e) {
            throw new DAOException("Error searching '" + search.getKey() + "'", e);
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
        final var host = (HostBean) b;
        try {
            var ojbH = (ecmwf.common.database.Host) host.getOjbImplementation();
            final var db = MasterManager.getDB();
            final var session = Util.getECpdsSessionFromObject(context);
            // Assign the objects coming from foreign keys !!!!
            ojbH.setECUser(db.getECUser(ojbH.getECUserName()));
            ojbH.setTransferMethod(db.getTransferMethod(ojbH.getTransferMethodName()));
            ojbH.setTransferGroup(db.getTransferGroup(ojbH.getTransferGroupName()));
            try {
                ojbH = MasterManager.getMI().updateHost(session, ojbH);
            } catch (final RemoteException e) {
                throw new DAOException(e.getMessage(), e);
            }
            // Save the updated host.
            host.setOjbImplementation(ojbH);
            var updated = false;
            // Now save changes in associations with destinations.
            for (final Destination destination : host.getIncreasedPriorities()) {
                final var a = db.getAssociation(destination.getId(), host.getId());
                a.setPriority(a.getPriority() - host.getPriorityChangeStep());
                db.update(session, a);
                updated = true;
            }
            for (final Destination destination : host.getDecreasedPriorities()) {
                final var a = db.getAssociation(destination.getId(), host.getId());
                a.setPriority(a.getPriority() + host.getPriorityChangeStep());
                db.update(session, a);
                updated = true;
            }
            // Now save changes in associations with users.
            for (final EcUser ecUser : host.getAddedEcUsers()) {
                db.insert(session, new HostECUser(ecUser.getName(), ecUser.getName()), true);
            }
            for (final EcUser ecUser : host.getDeletedEcUsers()) {
                db.remove(session, db.getHostECUser(ecUser.getName(), host.getName()));
            }
            // If there is any changes then lets retart the Destinations
            if (updated) {
                updateDestinations(host, context);
            }
        } catch (final Exception e) {
            throw new DAOException("Error handling host '" + host.getId() + "'", e);
        }
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
        final var host = (HostBean) bean;
        try {
            MasterManager.getMI().removeHost(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.Host) host.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting Host '" + host.getName() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param hosts
     *            the hosts
     *
     * @return the collection
     */
    private static final Collection<Host> convertArrayToModelBeanCollection(final ecmwf.common.database.Host[] hosts) {
        final var length = hosts != null ? hosts.length : 0;
        final List<Host> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new HostBean(hosts[i]));
        }
        return results;
    }

    /**
     * Convert to model bean collection.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    private static final Collection<Host> convertToModelBeanCollection(final Collection<ecmwf.common.database.Host> c) {
        return convertArrayToModelBeanCollection(c.toArray(new ecmwf.common.database.Host[c.size()]));
    }

    /**
     * Convert to pair with priority.
     *
     * @param c
     *            the c
     * @param destinationName
     *            the destination name
     *
     * @return the collection
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    private static final Collection<Pair> convertToPairWithPriority(final Collection<ecmwf.common.database.Host> c,
            final String destinationName) throws DataBaseException, RemoteException {
        final List<Pair> results = new ArrayList<>(c.size());
        for (final ecmwf.common.database.Host host : c) {
            final Host bean = new HostBean(host);
            final var association = MasterManager.getDB().getAssociation(destinationName, bean.getName());
            if (association != null) {
                results.add(new Pair(bean, association.getPriority()));
            }
        }
        return results;
    }

    /**
     * Update destinations.
     *
     * @param host
     *            the host
     * @param context
     *            the context
     *
     * @throws DAOException
     *             the DAO exception
     */
    private static final void updateDestinations(final ecmwf.ecpds.master.plugin.http.model.transfer.Host host,
            final Object context) throws DAOException {
        // Update (to signal they need a restart) all related destinations.
        if (context instanceof final User user) {
            try {
                for (final Destination destination : host.getDestinations()) {
                    destination.save(user);
                }
            } catch (final Exception e) {
                throw new DAOException(
                        "Error signaling as 'dirty' destinations for the Transfer Module '" + host.getName() + "'", e);
            }
        } else {
            log.error("Need a {} as context. Received a {}", User.class.getName(), context.getClass().getName());
        }
    }

    /**
     * Convert array to change log bean collection.
     *
     * @param currentObject
     *            the current object
     * @param changeLogs
     *            the change logs
     *
     * @return the collection
     */
    private static final Collection<ChangeLogBean> convertArrayToChangeLogBeanCollection(final String currentObject,
            final Collection<ChangeLog> changeLogs) {
        final List<ChangeLogBean> results = new ArrayList<>(changeLogs.size());
        for (final ChangeLog changeLog : changeLogs) {
            final var bean = new ChangeLogBean(currentObject, changeLog);
            if (!bean.getDifferences().isBlank()) {
                results.add(bean);
            }
        }
        return results;
    }
}
