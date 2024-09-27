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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.IncomingAssociation;
import ecmwf.common.database.IncomingPermission;
import ecmwf.common.database.PolicyUser;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.Operation;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class IncomingUserDAOHandler.
 */
public class IncomingUserDAOHandler extends PDSDAOBase implements DAOHandler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(IncomingUserDAOHandler.class);

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public IncomingUser create() throws DAOException {
        return new IncomingUserBean(new ecmwf.common.database.IncomingUser());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public IncomingUser findByPrimaryKey(final String key) throws DAOException {
        try {
            return new IncomingUserBean(MasterManager.getDB().getIncomingUser(key));
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
    public Collection<IncomingUser> find(final ModelSearch search) throws DAOException {
        try {
            if (search instanceof final SearchByIncomingPolicy s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getIncomingUsersForIncomingPolicy(s.getIncomingPolicyId()));
            }
            if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getIncomingUserArray());
            } else {
                throw new DAOException("Unsupported find expression or search class: " + search.getKey());
            }
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }

    }

    /**
     * {@inheritDoc}
     *
     * Save.
     */
    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        final var res = (IncomingUserBean) b;
        try {
            final var session = Util.getECpdsSessionFromObject(context);
            super.save(b, context); // Save ordinary fields.
            final var db = MasterManager.getDB();
            Collection<IncomingPolicy> policyList;
            if ((policyList = res.getAddedIncomingPolicies()) != null) {
                for (final IncomingPolicy policy : policyList) {
                    db.insert(session, new PolicyUser(res.getId(), policy.getId()), true);
                }
                log.debug("Added Categories " + res.getAddedIncomingPolicies());
            }
            if ((policyList = res.getDeletedIncomingPolicies()) != null) {
                for (final IncomingPolicy policy : policyList) {
                    db.remove(session, new PolicyUser(res.getId(), policy.getId()));
                }
                log.debug("Deleted Categories " + res.getDeletedIncomingPolicies());
            }
            Collection<Destination> ddestinationList;
            if ((ddestinationList = res.getAddedDestinations()) != null) {
                for (final Destination destination : ddestinationList) {
                    db.insert(session, new IncomingAssociation(res.getId(), destination.getName()), true);
                }
                log.debug("Added Destinations " + res.getAddedDestinations());
            }
            if ((ddestinationList = res.getDeletedDestinations()) != null) {
                for (final Destination destination : ddestinationList) {
                    db.remove(session, new IncomingAssociation(res.getId(), destination.getName()));
                }
                log.debug("Deleted Destinations " + res.getDeletedDestinations());
            }
            Collection<Operation> operationList;
            if ((operationList = res.getAddedOperations()) != null) {
                for (final Operation operation : operationList) {
                    db.insert(session, new IncomingPermission(res.getId(), operation.getName()), true);
                }
                log.debug("Added Operations " + res.getAddedOperations());
            }
            if ((operationList = res.getDeletedOperations()) != null) {
                for (final Operation operation : operationList) {
                    db.remove(session, new IncomingPermission(res.getId(), operation.getName()));
                }
                log.debug("Deleted Operations " + res.getDeletedOperations());
            }
        } catch (final Exception e) {
            throw new DAOException("Error saving IncomingUser '" + res.getId() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var u = (IncomingUserBean) b;
        try {
            MasterManager.getMI().removeIncomingUser(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.IncomingUser) u.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting IncomingUser '" + u.getId() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param incomingUser
     *            the incoming user
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<IncomingUser> convertArrayToModelBeanCollection(
            final ecmwf.common.database.IncomingUser[] incomingUser) {
        final var length = incomingUser != null ? incomingUser.length : 0;
        final List<IncomingUser> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new IncomingUserBean(incomingUser[i]));
        }
        return results;
    }
}
