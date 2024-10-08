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

package ecmwf.ecpds.master.plugin.http.controller.user.incoming;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.CountryHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.OperationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicyException;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.Operation;
import ecmwf.ecpds.master.plugin.http.model.transfer.OperationException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.util.bean.Pair;

/**
 * The Class IncomingUserActionForm.
 */
public class IncomingUserActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1527949956717872885L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(IncomingUserActionForm.class);

    /** The id. */
    private String id = "";

    /** The iso. */
    private String iso = "";

    /** The password. */
    private String password = "";

    /** The comment. */
    private String comment = "";

    /** The user data. */
    private String userData = "";

    /** The authorized SSH keys. */
    private String authorizedSSHKeys = "";

    /** The active. */
    private String active = "";

    /** The synchronised. */
    private String synchronised = "";

    /** The user. */
    private IncomingUser user = null;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the country iso.
     *
     * @return the country iso
     */
    public String getCountryIso() {
        return iso;
    }

    /**
     * Gets the incoming user.
     *
     * @return the incoming user
     */
    public IncomingUser getIncomingUser() {
        return user;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public String getActive() {
        return active;
    }

    /**
     * Gets the synchronized.
     *
     * @return the synchronized
     */
    public String getSynchronized() {
        return synchronised;
    }

    /**
     * Gets the checks if is synchronized.
     *
     * @return the checks if is synchronized
     */
    public String getIsSynchronized() {
        return synchronised;
    }

    /**
     * Sets the id.
     *
     * @param string
     *            the new id
     */
    public void setId(final String string) {
        id = string;
    }

    /**
     * Sets the password.
     *
     * @param string
     *            the new password
     */
    public void setPassword(final String string) {
        password = string;
    }

    /**
     * Sets the country iso.
     *
     * @param string
     *            the new country iso
     */
    public void setCountryIso(final String string) {
        iso = string;
    }

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    public void setUser(final IncomingUser user) {
        this.user = user;
    }

    /**
     * Sets the active.
     *
     * @param s
     *            the new active
     */
    public void setActive(final String s) {
        this.active = s;
    }

    /**
     * Sets the synchronized.
     *
     * @param s
     *            the new synchronized
     */
    public void setSynchronized(final String s) {
        this.synchronised = s;
    }

    /**
     * Sets the checks if is synchronized.
     *
     * @param s
     *            the new checks if is synchronized
     */
    public void setIsSynchronized(final String s) {
        this.synchronised = s;
    }

    /**
     * Sets the user data.
     *
     * @param s
     *            the new user data
     */
    public void setUserData(final String s) {
        this.userData = s;
    }

    /**
     * Sets the authorized SSH keys.
     *
     * @param s
     *            the new authorized SSH keys
     */
    public void setAuthorizedSSHKeys(final String s) {
        this.authorizedSSHKeys = s;
    }

    /**
     * Gets the user data.
     *
     * @return the user data
     */
    public String getUserData() {
        return this.userData;
    }

    /**
     * Gets the user properties.
     *
     * @return the user properties
     */
    public String getUserProperties() {
        return this.user.getProperties();
    }

    /**
     * Gets the authorized SSH keys.
     *
     * @return the authorized SSH keys
     */
    public String getAuthorizedSSHKeys() {
        return this.authorizedSSHKeys;
    }

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    public String getCompletions() {
        return ECtransOptions.toString(ECtransGroups.USER);
    }

    /**
     * Sets the comment.
     *
     * @param s
     *            the new comment
     */
    public void setComment(final String s) {
        this.comment = s;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Gets the incoming policies.
     *
     * @return All the IncomingPolicies currently associated to the User
     */
    public Collection<IncomingPolicy> getIncomingPolicies() {
        try {
            if (this.user != null) {
                return this.user.getAssociatedIncomingPolicies();
            }
        } catch (final TransferException e) {
            log.error("Problem getting Policies", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Gets the incoming policy options.
     *
     * @return All the IncomingPolicies currently NOT associated to the User
     */
    public Collection<IncomingPolicy> getIncomingPolicyOptions() {
        try {
            final var all = IncomingPolicyHome.findAll();
            all.removeAll(getIncomingPolicies());
            return all;
        } catch (final IncomingPolicyException e) {
            log.error("Problem getting IncomingPolicies options", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the destinations.
     *
     * @return All the Destinations currently associated to the User
     */
    public Collection<Destination> getDestinations() {
        try {
            if (this.user != null) {
                return this.user.getAssociatedDestinations();
            }
        } catch (final TransferException e) {
            log.error("Problem getting Destinations", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Gets the destination options.
     *
     * @return All the Destinations currently NOT associated to the User
     */
    public Collection<Pair> getDestinationOptions() {
        try {
            return Util.getDestinationPairList(DestinationHome.findAllNamesAndComments(), getDestinations());
        } catch (final TransferException e) {
            log.error("Problem getting Alias options", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the operations.
     *
     * @return All the Operations currently associated to the User
     */
    public Collection<Operation> getOperations() {
        try {
            if (this.user != null) {
                return this.user.getAssociatedOperations();
            }
        } catch (final OperationException e) {
            log.error("Problem getting Operations", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Gets the operation options.
     *
     * @return All the Operations currently NOT associated to the User
     */
    public Collection<Operation> getOperationOptions() {
        try {
            final var all = OperationHome.findAll();
            all.removeAll(getOperations());
            return all;
        } catch (final OperationException e) {
            log.error("Problem getting Operations options", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the country options.
     *
     * @return the country options
     */
    public Collection<Country> getCountryOptions() {
        try {
            return CountryHome.findAll();
        } catch (final TransferException e) {
            log.error("Problem getting Countries", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Populate user.
     *
     * @param u
     *            the u
     */
    protected void populateUser(final IncomingUser u) {
        u.setCountryIso(getCountryIso());
        u.setId(getId());
        u.setPassword(getPassword());
        u.setComment(getComment());
        u.setActive("on".equalsIgnoreCase(active) || "true".equalsIgnoreCase(active));
        u.setIsSynchronized("on".equalsIgnoreCase(synchronised) || "true".equalsIgnoreCase(synchronised));
        u.setData(getUserData());
        u.setAuthorizedSSHKeys(getAuthorizedSSHKeys());
    }

    /**
     * Populate from user.
     *
     * @param u
     *            the u
     */
    protected void populateFromUser(final IncomingUser u) {
        this.user = u;
        this.setCountryIso(u.getCountryIso());
        this.setId(u.getId());
        this.setPassword(u.getPassword());
        this.setComment(u.getComment());
        this.setActive(u.getActive() ? "on" : "off");
        this.setSynchronized(u.getIsSynchronized() ? "on" : "off");
        this.setUserData(u.getData());
        this.setAuthorizedSSHKeys(u.getAuthorizedSSHKeys());
    }
}
