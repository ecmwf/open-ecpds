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

package ecmwf.ecpds.master.plugin.http.controller.user.policy;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.util.bean.Pair;

/**
 * The Class IncomingPolicyActionForm.
 */
public class IncomingPolicyActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1527949956717872885L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(IncomingPolicyActionForm.class);

    /** The id. */
    private String id = "";

    /** The comment. */
    private String comment = "";

    /** The data. */
    private String data = "";

    /** The active. */
    private String active = "";

    /** The policy. */
    private IncomingPolicy policy = null;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the incoming policy.
     *
     * @return the incoming policy
     */
    public IncomingPolicy getIncomingPolicy() {
        return policy;
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
     * Sets the id.
     *
     * @param string
     *            the new id
     */
    public void setId(final String string) {
        id = string;
    }

    /**
     * Sets the policy.
     *
     * @param policy
     *            the new policy
     */
    public void setPolicy(final IncomingPolicy policy) {
        this.policy = policy;
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
     * Sets the data.
     *
     * @param s
     *            the new data
     */
    public void setData(final String s) {
        this.data = s;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return this.data;
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
     * Gets the completions.
     *
     * @return the completions
     */
    public String getCompletions() {
        return ECtransOptions.toString(ECtransGroups.USER);
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
     * Gets the destinations.
     *
     * @return All the Destinations currently associated to the IncomingPolicy
     */
    public Collection<Destination> getDestinations() {
        try {
            if (this.policy != null) {
                return this.policy.getAssociatedDestinations();
            }
        } catch (final TransferException e) {
            log.error("Problem getting Destinations", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Gets the destination options.
     *
     * @return All the Destinations currently NOT associated to the IncomingPolicy
     */
    public List<Pair> getDestinationOptions() {
        try {
            return Util.getDestinationPairList(DestinationHome.findAllNamesAndComments(), getDestinations());
        } catch (final TransferException e) {
            log.error("Problem getting Alias options", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Populate policy.
     *
     * @param p
     *            the p
     */
    protected void populatePolicy(final IncomingPolicy p) {
        p.setId(getId());
        p.setComment(getComment());
        p.setActive("on".equalsIgnoreCase(active) || "true".equalsIgnoreCase(active));
        p.setData(getData());
    }

    /**
     * Populate from policy.
     *
     * @param p
     *            the p
     */
    protected void populateFromPolicy(final IncomingPolicy p) {
        this.policy = p;
        this.setId(p.getId());
        this.setComment(p.getComment());
        this.setActive(p.getActive() ? "on" : "off");
        this.setData(p.getData());
    }
}
