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

import static ecmwf.common.ectrans.ECtransGroups.Module.USER_PORTAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.database.IncomingConnection;
import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.CountryHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingUserHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.OperationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicyException;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUserException;
import ecmwf.ecpds.master.plugin.http.model.transfer.Operation;
import ecmwf.ecpds.master.plugin.http.model.transfer.OperationException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.users.User;

/**
 * The Class IncomingUserBean.
 */
public class IncomingUserBean extends ModelBeanBase implements IncomingUser, OjbImplementedBean {

    /** The incoming user. */
    private final ecmwf.common.database.IncomingUser incomingUser;

    /** The incoming connections. */
    private final Collection<IncomingConnection> incomingConnections = new ArrayList<>();

    /** The added incoming policies. */
    private final Collection<IncomingPolicy> addedIncomingPolicies = new ArrayList<>();

    /** The deleted incoming policies. */
    private final Collection<IncomingPolicy> deletedIncomingPolicies = new ArrayList<>();

    /** The added destinations. */
    private final Collection<Destination> addedDestinations = new ArrayList<>();

    /** The deleted destinations. */
    private final Collection<Destination> deletedDestinations = new ArrayList<>();

    /** The added operations. */
    private final Collection<Operation> addedOperations = new ArrayList<>();

    /** The deleted operations. */
    private final Collection<Operation> deletedOperations = new ArrayList<>();

    /** The setup. */
    private final ECtransSetup setup;

    /**
     * Instantiates a new incoming user bean.
     *
     * @param u
     *            the u
     */
    protected IncomingUserBean(final ecmwf.common.database.IncomingUser u) {
        incomingConnections.addAll(u.getConnections());
        incomingUser = u;
        this.setup = USER_PORTAL.getECtransSetup(u.getData());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return incomingUser;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return IncomingUser.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return incomingUser.getId();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the comment.
     */
    @Override
    public String getComment() {
        return incomingUser.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the active.
     */
    @Override
    public boolean getActive() {
        return incomingUser.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the checks if is synchronized.
     */
    @Override
    public boolean getIsSynchronized() {
        return incomingUser.getSynchronized();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last login host.
     */
    @Override
    public String getLastLoginHost() {
        return incomingUser.getLastLoginHost();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last login.
     */
    @Override
    public Date getLastLogin() {
        return incomingUser.getLastLogin();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the password.
     */
    @Override
    public String getPassword() {
        return incomingUser.getPassword();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the country.
     */
    @Override
    public Country getCountry() throws TransferException {
        return CountryHome.findByPrimaryKey(getCountryIso());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the country iso.
     */
    @Override
    public String getCountryIso() {
        return incomingUser.getIso();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data.
     */
    @Override
    public String getData() {
        return setup.getProperties(false);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the properties.
     */
    @Override
    public String getProperties() {
        return setup.getProperties(true);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the authorized SSH keys.
     */
    @Override
    public String getAuthorizedSSHKeys() {
        return incomingUser.getAuthorizedSSHKeys();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the country iso.
     */
    @Override
    public void setCountryIso(final String iso) {
        incomingUser.setIso(iso);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the id.
     */
    @Override
    public void setId(final String id) {
        incomingUser.setId(id);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the password.
     */
    @Override
    public void setPassword(final String password) {
        incomingUser.setPassword(password);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the comment.
     */
    @Override
    public void setComment(final String comment) {
        incomingUser.setComment(comment);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
     */
    @Override
    public void setActive(final boolean active) {
        incomingUser.setActive(active);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the checks if is synchronized.
     */
    @Override
    public void setIsSynchronized(final boolean synchronised) {
        incomingUser.setSynchronized(synchronised);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the data.
     */
    @Override
    public void setData(final String data) {
        incomingUser.setData(data);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the authorized SSH keys.
     */
    @Override
    public void setAuthorizedSSHKeys(final String authorizedSSHKeys) {
        incomingUser.setAuthorizedSSHKeys(authorizedSSHKeys);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the associated incoming policies.
     */
    @Override
    public Collection<IncomingPolicy> getAssociatedIncomingPolicies() throws TransferException {
        try {
            return IncomingPolicyHome.findAssociatedToIncomingUser(this);
        } catch (final IncomingPolicyException e) {
            throw new TransferException("Problem getting associated policies for IncomingUser", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the associated destinations.
     */
    @Override
    public Collection<Destination> getAssociatedDestinations() throws TransferException {
        try {
            return DestinationHome.findAssociatedToIncomingUser(this);
        } catch (final TransferException e) {
            throw new TransferException("Problem getting associated destinations for IncomingUser", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the associated operations.
     */
    @Override
    public Collection<Operation> getAssociatedOperations() throws OperationException {
        try {
            return OperationHome.findAssociatedToIncomingUser(this);
        } catch (final OperationException e) {
            throw new OperationException("Problem getting associated operations for IncomingUser", e);
        }
    }

    /**
     * Gets the incoming users.
     *
     * @return the incoming users
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUserException
     *             the incoming user exception
     */
    public Collection<IncomingUser> getIncomingUsers() throws IncomingUserException {
        return IncomingUserHome.findAll();
    }

    /**
     * {@inheritDoc}
     *
     * Close session.
     */
    @Override
    public void closeSession(final User u, final String id) throws OperationException {
        try {
            MasterManager.getMI().closeIncomingConnection(Util.getECpdsSessionFromObject(u), id);
        } catch (final Exception e) {
            throw new OperationException("Problem closing session '" + id + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Adds the incoming policy.
     */
    @Override
    public void addIncomingPolicy(final IncomingPolicy p) {
        addedIncomingPolicies.add(p);
    }

    /**
     * {@inheritDoc}
     *
     * Delete incoming policy.
     */
    @Override
    public void deleteIncomingPolicy(final IncomingPolicy p) {
        deletedIncomingPolicies.add(p);
    }

    /**
     * Gets the added incoming policies.
     *
     * @return the added incoming policies
     */
    protected Collection<IncomingPolicy> getAddedIncomingPolicies() {
        return addedIncomingPolicies;
    }

    /**
     * Gets the deleted incoming policies.
     *
     * @return the deleted incoming policies
     */
    protected Collection<IncomingPolicy> getDeletedIncomingPolicies() {
        return deletedIncomingPolicies;
    }

    /**
     * {@inheritDoc}
     *
     * Adds the destination.
     */
    @Override
    public void addDestination(final Destination d) {
        addedDestinations.add(d);
    }

    /**
     * {@inheritDoc}
     *
     * Delete destination.
     */
    @Override
    public void deleteDestination(final Destination d) {
        deletedDestinations.add(d);
    }

    /**
     * {@inheritDoc}
     *
     * Adds the operation.
     */
    @Override
    public void addOperation(final Operation o) {
        addedOperations.add(o);
    }

    /**
     * {@inheritDoc}
     *
     * Delete operation.
     */
    @Override
    public void deleteOperation(final Operation o) {
        deletedOperations.add(o);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming connections.
     */
    @Override
    public Collection<IncomingConnection> getIncomingConnections() {
        return incomingConnections;
    }

    /**
     * Gets the added destinations.
     *
     * @return the added destinations
     */
    protected Collection<Destination> getAddedDestinations() {
        return addedDestinations;
    }

    /**
     * Gets the deleted destinations.
     *
     * @return the deleted destinations
     */
    protected Collection<Destination> getDeletedDestinations() {
        return deletedDestinations;
    }

    /**
     * Gets the added operations.
     *
     * @return the added operations
     */
    protected Collection<Operation> getAddedOperations() {
        return addedOperations;
    }

    /**
     * Gets the deleted operations.
     *
     * @return the deleted operations
     */
    protected Collection<Operation> getDeletedOperations() {
        return deletedOperations;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the completions.
     */
    @Override
    public String getCompletions() {
        return ECtransOptions.toString(ECtransGroups.USER);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final IncomingUserBean incomingUserBean && equals(incomingUserBean);
    }

    /**
     * Equals.
     *
     * @param d
     *            the d
     *
     * @return true, if successful
     */
    public boolean equals(final IncomingUserBean d) {
        return getId().equals(d.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + incomingUser + " }";
    }
}
