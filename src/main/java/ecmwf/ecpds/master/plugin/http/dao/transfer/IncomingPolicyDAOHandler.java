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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.PolicyAssociation;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByDestination;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class IncomingPolicyDAOHandler.
 */
public class IncomingPolicyDAOHandler extends PDSDAOBase implements DAOHandler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(IncomingPolicyDAOHandler.class);

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public ModelBean create() throws DAOException {
        return new IncomingPolicyBean(new ecmwf.common.database.IncomingPolicy());
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        try {
            return new IncomingPolicyBean(MasterManager.getDB().getIncomingPolicy(key));
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
    public Collection<IncomingPolicy> find(final ModelSearch search) throws DAOException {
        try {
            if (search instanceof final SearchByDestination s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getDestinationIncomingPolicies(s.getDestinationName()));
            }
            if (search instanceof final SearchByIncomingUser s) {
                return convertArrayToModelBeanCollection(
                        MasterManager.getDB().getIncomingPoliciesForIncomingUser(s.getIncomingUserId()));
            } else if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getIncomingPolicyArray());
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
        final var res = (IncomingPolicyBean) b;
        try {
            final var session = Util.getECpdsSessionFromObject(context);
            super.save(b, context); // Save ordinary fields.
            final var db = MasterManager.getDB();
            Iterator<?> i = null;
            Collection<?> c = null;
            if ((c = res.getAddedDestinations()) != null) {
                i = c.iterator();
                while (i.hasNext()) {
                    db.insert(session,
                            new PolicyAssociation(
                                    ((ecmwf.ecpds.master.plugin.http.model.transfer.Destination) i.next()).getName(),
                                    res.getId()),
                            true);
                }
                log.debug("Added Destinations " + res.getAddedDestinations());
            }
            if ((c = res.getDeletedDestinations()) != null) {
                i = c.iterator();
                while (i.hasNext()) {
                    db.remove(session,
                            new PolicyAssociation(
                                    ((ecmwf.ecpds.master.plugin.http.model.transfer.Destination) i.next()).getName(),
                                    res.getId()));
                }
                log.debug("Deleted Destinations " + res.getDeletedDestinations());
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
        final var p = (IncomingPolicyBean) b;
        try {
            final var mi = MasterManager.getMI();
            mi.removeIncomingPolicy(Util.getECpdsSessionFromObject(context),
                    (ecmwf.common.database.IncomingPolicy) p.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting IncomingPolicy '" + p.getId() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param incomingPolicy
     *            the incoming policy
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<IncomingPolicy> convertArrayToModelBeanCollection(
            final ecmwf.common.database.IncomingPolicy[] incomingPolicy) {
        final var length = incomingPolicy != null ? incomingPolicy.length : 0;
        final List<IncomingPolicy> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new IncomingPolicyBean(incomingPolicy[i]));
        }
        return results;
    }
}
