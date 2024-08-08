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

import static ecmwf.common.ectrans.ECtransGroups.Module.USER_PORTAL;

import java.util.ArrayList;
import java.util.Collection;

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicyException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class IncomingPolicyBean.
 */
public class IncomingPolicyBean extends ModelBeanBase implements IncomingPolicy, OjbImplementedBean {

    /** The incoming policy. */
    private final ecmwf.common.database.IncomingPolicy incomingPolicy;

    /** The added destinations. */
    private final Collection<Destination> addedDestinations = new ArrayList<>();

    /** The deleted destinations. */
    private final Collection<Destination> deletedDestinations = new ArrayList<>();

    /** The setup. */
    private final ECtransSetup setup;

    /**
     * Instantiates a new incoming policy bean.
     *
     * @param incomingPolicy
     *            the incoming policy
     */
    protected IncomingPolicyBean(final ecmwf.common.database.IncomingPolicy incomingPolicy) {
        this.incomingPolicy = incomingPolicy;
        this.setup = USER_PORTAL.getECtransSetup(incomingPolicy.getData());
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return this.incomingPolicy;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return IncomingPolicy.class.getName();
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return this.incomingPolicy.getId();
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    @Override
    public String getComment() {
        return this.incomingPolicy.getComment();
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    @Override
    public boolean getActive() {
        return this.incomingPolicy.getActive();
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    @Override
    public String getData() {
        return setup.getProperties(false);
    }

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    @Override
    public String getProperties() {
        return setup.getProperties(true);
    }

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    @Override
    public String getCompletions() {
        return ECtransOptions.toString(ECtransGroups.USER);
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    @Override
    public void setId(final String id) {
        this.incomingPolicy.setId(id);
    }

    /**
     * Sets the comment.
     *
     * @param comment
     *            the new comment
     */
    @Override
    public void setComment(final String comment) {
        this.incomingPolicy.setComment(comment);
    }

    /**
     * Sets the active.
     *
     * @param active
     *            the new active
     */
    @Override
    public void setActive(final boolean active) {
        this.incomingPolicy.setActive(active);
    }

    /**
     * Sets the data.
     *
     * @param data
     *            the new data
     */
    @Override
    public void setData(final String data) {
        this.incomingPolicy.setData(data);
    }

    /**
     * Gets the incoming policies.
     *
     * @return the incoming policies
     *
     * @throws IncomingPolicyException
     *             the incoming policy exception
     */
    public Collection<IncomingPolicy> getIncomingPolicies() throws IncomingPolicyException {
        return IncomingPolicyHome.findAll();
    }

    /**
     * Adds the destination.
     *
     * @param d
     *            the d
     */
    @Override
    public void addDestination(final Destination d) {
        addedDestinations.add(d);
    }

    /**
     * Delete destination.
     *
     * @param d
     *            the d
     */
    @Override
    public void deleteDestination(final Destination d) {
        deletedDestinations.add(d);
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
     * Gets the associated destinations.
     *
     * @return the associated destinations
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<Destination> getAssociatedDestinations() throws TransferException {
        try {
            return DestinationHome.findAssociatedToIncomingPolicy(this);
        } catch (final TransferException e) {
            throw new TransferException("Problem getting associated destinations for IncomingPolicy", e);
        }
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final IncomingPolicyBean incomingPolicyBean && equals(incomingPolicyBean);
    }

    /**
     * Equals.
     *
     * @param d
     *            the d
     *
     * @return true, if successful
     */
    public boolean equals(final IncomingPolicyBean d) {
        return getId().equals(d.getId());
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + incomingPolicy + " }";
    }
}
