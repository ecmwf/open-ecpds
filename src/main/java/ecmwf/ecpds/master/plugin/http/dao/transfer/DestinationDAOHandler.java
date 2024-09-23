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
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_ALIAS;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ecmwf.common.database.Alias;
import ecmwf.common.database.Association;
import ecmwf.common.database.ChangeLog;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DestinationECUser;
import ecmwf.common.database.PolicyAssociation;
import ecmwf.common.database.Traffic;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationAliasedFrom;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationAliases;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationChangeLog;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationTraffic;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationsByCountry;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationsByHost;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationsByUser;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingPolicy;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingUser;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.bean.Pair;

/**
 * The Class DestinationDAOHandler.
 */
public class DestinationDAOHandler extends PDSDAOBase implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public Destination create() throws DAOException {
        return new DestinationBean(new ecmwf.common.database.Destination());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public Destination findByPrimaryKey(final String key) throws DAOException {
        return findByPrimaryKey(key, true);
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     * @param useCache
     *            the use cache
     *
     * @return the destination
     *
     * @throws ecmwf.web.services.persistence.DAOException
     *             the DAO exception
     */
    public Destination findByPrimaryKey(final String key, final boolean useCache) throws DAOException {
        try {
            return new DestinationBean(MasterManager.getDB().getDestination(key, useCache));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Problem searching by key '" + key + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<?> find(final ModelSearch search) throws DAOException {
        try {
            if (search instanceof final DestinationsByHost s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getDestinationsByHostName(s.getHost().getName()));
            }
            if (search instanceof final SearchByIncomingUser s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getDestinationsForIncomingUser(s.getIncomingUserId()));
            } else if (search instanceof final SearchByIncomingPolicy s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getDestinationsForIncomingPolicy(s.getIncomingPolicyId()));
            } else if (search instanceof final DestinationsByCountry s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getDestinationsByCountryISO(s.getCountry().getIso()));
            } else if (search instanceof final DestinationsByUser s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getDestinationsByUser(s.getUser().getId(), s.getSearch(),
                                s.getFromToAliases(), s.getAsc(), s.getStatus(), s.getType(), s.getFilter()));
            } else if (search instanceof final DestinationAliases s) {
                if (s.isAliasList()) {
                    return convertArrayToModelBeanCollection(
                            MasterManager.getDB().getAliases(s.getDestination().getName(), "aliases"));
                } else {
                    return convertArrayToModelBeanCollection(s.getDestination(), false,
                            MasterManager.getDB().getDestinationAliases(s.getDestination().getName(), "aliases"));
                }
            } else if (search instanceof final DestinationTraffic s) {
                return convertArrayToTrafficBeanCollection(
                        MasterManager.getDB().getTrafficByDestinationName(s.getDestination().getName()));
            } else if (search instanceof final DestinationChangeLog s) {
                return convertArrayToChangeLogBeanCollection(
                        MasterManager.getDB().getDestination(s.getDestination().getName(), true).toString(false),
                        MasterManager.getDB().getChangeLogByKey("DES_NAME", s.getDestination().getName()));
            } else if (search instanceof final DestinationAliasedFrom s) {
                return convertArrayToModelBeanCollection(s.getDestination(), true,
                        MasterManager.getDB().getDestinationAliases(s.getDestination().getName(), "aliasedFrom"));
            } else if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getDestinationArray());
            } else if ("NamesAndComments".equals(search.getQuery())) {
                return convertArrayToPairCollection(MasterManager.getDB().getDestinationNamesAndComments());
            } else if ("ShowingInMonitor".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getDestinationArray(true));
            } else {
                throw new DAOException("'find' method with query '" + search.getQuery() + "' not supported!");
            }
        } catch (DataBaseException | IOException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Insert.
     */
    @Override
    public void insert(final ModelBean b, final Object context) throws DAOException {
        final var d = (DestinationBean) b;
        final var ojbD = (ecmwf.common.database.Destination) d.getOjbImplementation();
        ojbD.setStatusCode(StatusFactory.WAIT);
        super.insert(b, context);
    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        final var bean = (DestinationBean) b;
        try {
            final var session = Util.getECpdsSessionFromObject(context);
            final var db = MasterManager.getDB();
            // Assign the objects coming from foreign keys !!!!
            final var imp = (ecmwf.common.database.Destination) bean.getOjbImplementation();
            imp.setECUser(db.getECUser(imp.getECUserName()));
            imp.setCountry(db.getCountry(imp.getCountryIso()));
            imp.setHostForSource(db.getHost(imp.getHostForSourceName()));
            // Deal with the ECtransSetup of the acquisition!
            final var setup = DESTINATION_ALIAS.getECtransSetup(bean.getData());
            // Now save the associations!
            for (final EcUser ecUser : bean.getAddedAssociatedEcUsers()) {
                db.insert(session, new DestinationECUser(bean.getName(), ecUser.getId()), true);
            }
            for (final Host host : bean.getAddedHosts()) {
                db.insert(session, new Association(bean.getName(), host.getId()), true);
            }
            for (final IncomingPolicy policy : bean.getAddedIncomingPolicies()) {
                db.insert(session, new PolicyAssociation(bean.getName(), policy.getId()), true);
            }
            for (final Destination destination : bean.getAddedAliases()) {
                db.insert(session, new Alias(bean.getName(), destination.getName()), true);
            }
            for (final EcUser ecUser : bean.getDeletedAssociatedEcUsers()) {
                db.remove(session, db.getDestinationECUser(bean.getName(), ecUser.getId()));
            }
            for (final Host host : bean.getDeletedHosts()) {
                db.remove(session, db.getAssociation(bean.getName(), host.getId()));
            }
            for (final IncomingPolicy policy : bean.getDeletedPolicyAssociations()) {
                db.remove(session, db.getPolicyAssociation(bean.getName(), policy.getId()));
            }
            for (final Destination destination : bean.getDeletedAliases()) {
                final var destinationName = destination.getName();
                db.remove(session, db.getAlias(bean.getName(), destinationName));
                setup.remove(destinationName);
            }
            for (final String file : bean.getDeletedMetadataFiles()) {
                try {
                    MasterManager.getAI().delete(bean.getName(), file, true);
                } catch (final Exception e) {
                    throw new DAOException("Problem deleting MetadataFile for Destination '" + bean.getName() + "'", e);
                }
            }
            // Let's remove the parameters concerning the Aliases which does not
            // exists any more and add the one who are missing!
            final var aliases = bean.getAliasList();
            for (final ecmwf.ecpds.master.plugin.http.model.transfer.Alias alias : aliases) {
                final var destinationName = alias.getDestinationName();
                if (setup.get(destinationName, null) == null) { // The parameters are missing?
                    setup.set(destinationName, "recursive=no;delay=0;pattern=.*");
                }
            }
            // Now deal with the saving of the data!
            bean.setData(setup.getData());
            // Update the last modified date!
            bean.setUpdatedNow();
            // Save the ordinary fields!
            super.save(b, context);
        } catch (final Exception e) {
            throw new DAOException("Error handling associations for Destination '" + bean.getName() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var destination = (DestinationBean) b;
        try {
            MasterManager.getMI().removeDestination(Util.getECpdsSessionFromObject(context),
                    ((ecmwf.common.database.Destination) destination.getOjbImplementation()).getName(), false, true);
        } catch (final Exception e) {
            throw new DAOException("Problem deleting Destination '" + destination.getName() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param originalDestination
     *            the original destination
     * @param aliasFrom
     *            the alias from
     * @param destinations
     *            the destinations
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<DestinationBean> convertArrayToModelBeanCollection(
            final Destination originalDestination, final boolean aliasFrom,
            final ecmwf.common.database.Destination[] destinations) {
        final var length = destinations != null ? destinations.length : 0;
        final List<DestinationBean> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new DestinationBean(originalDestination, aliasFrom, destinations[i]));
        }
        return results;
    }

    /**
     * Convert array to pair collection.
     *
     * @param destinations
     *            the destinations
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<Pair> convertArrayToPairCollection(
            final Set<Map.Entry<String, String>> destinations) {
        final var length = destinations != null ? destinations.size() : 0;
        final List<Pair> results = new ArrayList<>(length);
        if (length > 0) {
            for (final Map.Entry<String, String> destination : destinations) {
                results.add(new Pair(destination.getKey(), destination.getValue()));
            }
        }
        return results;
    }

    /**
     * Convert array to model bean collection.
     *
     * @param destinations
     *            the destinations
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<DestinationBean> convertArrayToModelBeanCollection(
            final ecmwf.common.database.Destination[] destinations) {
        final var length = destinations != null ? destinations.length : 0;
        final List<DestinationBean> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new DestinationBean(destinations[i]));
        }
        return results;
    }

    /**
     * Convert array to model bean collection.
     *
     * @param aliases
     *            the aliases
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<AliasBean> convertArrayToModelBeanCollection(final Alias[] aliases) {
        final var length = aliases != null ? aliases.length : 0;
        final List<AliasBean> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new AliasBean(aliases[i]));
        }
        return results;
    }

    /**
     * Convert array to traffic bean collection.
     *
     * @param traffics
     *            the traffics
     *
     * @return the collection
     */
    private static final Collection<TrafficBean> convertArrayToTrafficBeanCollection(
            final Collection<Traffic> traffics) {
        final List<TrafficBean> results = new ArrayList<>(traffics.size());
        for (final Traffic traffic : traffics) {
            results.add(new TrafficBean(traffic));
        }
        return results;
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
