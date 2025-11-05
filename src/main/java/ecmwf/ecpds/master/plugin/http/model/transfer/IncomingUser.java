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

package ecmwf.ecpds.master.plugin.http.model.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * An IncomingUser, as it is going to be seen from Controller and View for the
 * web application
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Date;

import ecmwf.common.database.IncomingConnection;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

/**
 * The Interface IncomingUser.
 */
public interface IncomingUser extends ModelBean {

    /**
     * Gets the country iso.
     *
     * @return the country iso
     */
    String getCountryIso();

    /**
     * Gets the country.
     *
     * @return the country
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Country getCountry() throws TransferException;

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    String getId();

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    String getCompletions();

    /**
     * Gets the password.
     *
     * @return the password
     */
    String getPassword();

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Gets the checks if is synchronized.
     *
     * @return the checks if is synchronized
     */
    boolean getIsSynchronized();

    /**
     * Gets the last login.
     *
     * @return the last login
     */
    Date getLastLogin();

    /**
     * Gets the last login host.
     *
     * @return the last login host
     */
    String getLastLoginHost();

    /**
     * Gets the data.
     *
     * @return the data
     */
    String getData();

    /**
     * Gets the anonymous.
     *
     * @return the anonymous
     */
    boolean getAnonymous();

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    String getProperties();

    /**
     * Gets the authorized SSH keys.
     *
     * @return the authorized SSH keys
     */
    String getAuthorizedSSHKeys();

    /**
     * Sets the country iso.
     *
     * @param iso
     *            the new country iso
     */
    void setCountryIso(String iso);

    /**
     * {@inheritDoc}
     *
     * Sets the id.
     */
    @Override
    void setId(String id);

    /**
     * Sets the password.
     *
     * @param password
     *            the new password
     */
    void setPassword(String password);

    /**
     * Sets the comment.
     *
     * @param comment
     *            the new comment
     */
    void setComment(String comment);

    /**
     * Sets the active.
     *
     * @param active
     *            the new active
     */
    void setActive(boolean active);

    /**
     * Sets the checks if is synchronized.
     *
     * @param synchronised
     *            the new checks if is synchronized
     */
    void setIsSynchronized(boolean synchronised);

    /**
     * Sets the data.
     *
     * @param data
     *            the new data
     */
    void setData(String data);

    /**
     * Sets the authorized SSH keys.
     *
     * @param authorizedSSHKeys
     *            the new authorized SSH keys
     */
    void setAuthorizedSSHKeys(String authorizedSSHKeys);

    /**
     * Adds the incoming policy.
     *
     * @param p
     *            the p
     */
    void addIncomingPolicy(IncomingPolicy p);

    /**
     * Delete incoming policy.
     *
     * @param p
     *            the p
     */
    void deleteIncomingPolicy(IncomingPolicy p);

    /**
     * Adds the destination.
     *
     * @param d
     *            the d
     */
    void addDestination(Destination d);

    /**
     * Delete destination.
     *
     * @param d
     *            the d
     */
    void deleteDestination(Destination d);

    /**
     * Adds the operation.
     *
     * @param o
     *            the o
     */
    void addOperation(Operation o);

    /**
     * Delete operation.
     *
     * @param o
     *            the o
     */
    void deleteOperation(Operation o);

    /**
     * Close session.
     *
     * @param u
     *            the u
     * @param id
     *            the id
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.OperationException
     *             the operation exception
     */
    void closeSession(final User u, String id) throws OperationException;

    /**
     * Gets the incoming connections.
     *
     * @return the incoming connections
     */
    Collection<IncomingConnection> getIncomingConnections();

    /**
     * Gets the associated incoming policies.
     *
     * @return the associated incoming policies
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<IncomingPolicy> getAssociatedIncomingPolicies() throws TransferException;

    /**
     * Gets the associated destinations.
     *
     * @return the associated destinations
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Destination> getAssociatedDestinations() throws TransferException;

    /**
     * Gets the associated operations.
     *
     * @return the associated operations
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.OperationException
     *             the operation exception
     */
    Collection<Operation> getAssociatedOperations() throws OperationException;
}
