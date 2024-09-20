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

package ecmwf.ecpds.master.plugin.http.controller.datafile.transferserver;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.home.datafile.TransferGroupHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.web.controller.ECMWFActionForm;

/**
 * The Class TransferServerActionForm.
 */
public class TransferServerActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7945785284879269199L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(TransferServerActionForm.class);

    /** The id. */
    private String id = "";

    /** The name. */
    private String name = "";

    /** The transfer group name. */
    private String transferGroupName = "";

    /** The host for replication name. */
    private String hostForReplicationName = "";

    /** The max transfers. */
    private String maxTransfers = "";

    /** The max inactivity. */
    private String maxInactivity = "";

    /** The last update. */
    private String lastUpdate = "";

    /** The active. */
    private String active = "";

    /** The replicate. */
    private String replicate = "";

    /** The check. */
    private String check = "";

    /** The host. */
    private String host = "";

    /** The port. */
    private String port = "";

    /**
     * Gets the active.
     *
     * @return the active
     */
    public String getActive() {
        return active;
    }

    /**
     * Gets the replicate.
     *
     * @return the replicate
     */
    public String getReplicate() {
        return replicate;
    }

    /**
     * Gets the check.
     *
     * @return the check
     */
    public String getCheck() {
        return check;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    public String getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Gets the max inactivity.
     *
     * @return the max inactivity
     */
    public String getMaxInactivity() {
        return maxInactivity;
    }

    /**
     * Gets the max transfers.
     *
     * @return the max transfers
     */
    public String getMaxTransfers() {
        return maxTransfers;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the active.
     *
     * @param string
     *            the new active
     */
    public void setActive(final String string) {
        active = string;
    }

    /**
     * Sets the replicate.
     *
     * @param string
     *            the new replicate
     */
    public void setReplicate(final String string) {
        replicate = string;
    }

    /**
     * Sets the check.
     *
     * @param string
     *            the new check
     */
    public void setCheck(final String string) {
        check = string;
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
     * Sets the last update.
     *
     * @param string
     *            the new last update
     */
    public void setLastUpdate(final String string) {
        lastUpdate = string;
    }

    /**
     * Sets the max inactivity.
     *
     * @param string
     *            the new max inactivity
     */
    public void setMaxInactivity(final String string) {
        maxInactivity = string;
    }

    /**
     * Sets the max transfers.
     *
     * @param string
     *            the new max transfers
     */
    public void setMaxTransfers(final String string) {
        maxTransfers = string;
    }

    /**
     * Sets the name.
     *
     * @param string
     *            the new name
     */
    public void setName(final String string) {
        name = string;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the host.
     *
     * @param string
     *            the new host
     */
    public void setHost(final String string) {
        host = string;
    }

    /**
     * Sets the port.
     *
     * @param string
     *            the new port
     */
    public void setPort(final String string) {
        port = string;
    }

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    public String getTransferGroupName() {
        return transferGroupName;
    }

    /**
     * Sets the transfer group name.
     *
     * @param string
     *            the new transfer group name
     */
    public void setTransferGroupName(final String string) {
        transferGroupName = string;
    }

    /**
     * Gets the host for replication name.
     *
     * @return the host for replication name
     */
    public String getHostForReplicationName() {
        return hostForReplicationName;
    }

    /**
     * Sets the host for replication name.
     *
     * @param string
     *            the new host for replication name
     */
    public void setHostForReplicationName(final String string) {
        hostForReplicationName = string;
    }

    /**
     * Gets the transfer group options.
     *
     * @return the transfer group options
     */
    public Collection<TransferGroup> getTransferGroupOptions() {
        try {
            return TransferGroupHome.findAll();
        } catch (final DataFileException e) {
            log.error("Error getting Transfer Groups", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the host for replication options.
     *
     * @return the host for replication options
     */
    public Collection<Host> getHostForReplicationOptions() {
        final List<Host> replicationHosts = new ArrayList<>();
        try {
            final var list = HostHome.findAll();
            for (final Host host : list) {
                if (HostOption.REPLICATION.equals(host.getType())) {
                    replicationHosts.add(host);
                }
            }
        } catch (final TransferException e) {
            log.error("Error getting Hosts", e);
        }
        return replicationHosts;
    }

    /**
     * Reset.
     *
     * @param mapping
     *            the mapping
     * @param request
     *            the request
     */
    @Override
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        this.replicate = "off";
        this.active = "off";
        this.check = "off";
    }

    // *********************************

    /**
     * Reset fields.
     */
    protected void resetFields() {
        id = "";
        name = "";
        transferGroupName = "";
        hostForReplicationName = "";
        maxTransfers = "";
        maxInactivity = "";
        lastUpdate = "";
        active = "";
        replicate = "";
        host = "";
        port = "";

    }

    /**
     * Populate transfer server.
     *
     * @param h
     *            the h
     */
    protected void populateTransferServer(final TransferServer h) {
        h.setName(this.getName());
        // h.setMaxTransfers(convertToInt(this.getMaxTransfers()));
        // h.setMaxInactivity(convertToInt(this.getMaxInactivity()));
        // h.setLastUpdate(convertToInt(this.getLastUpdate()));
        h.setActive(convertToBoolean(this.getActive()));
        h.setHost(this.getHost());
        h.setPort(convertToInt(this.getPort()));
        h.setTransferGroupName(this.transferGroupName);
        h.setHostForReplicationName(this.hostForReplicationName);
        h.setCheck(convertToBoolean(this.getCheck()));
        h.setReplicate(convertToBoolean(this.getReplicate()));
    }

    /**
     * Populate from transfer server.
     *
     * @param h
     *            the h
     */
    protected void populateFromTransferServer(final TransferServer h) {
        id = h.getId();
        this.setName(h.getName());
        // this.setMaxTransfers(Integer.toString(h.getMaxTransfers()));
        // this.setMaxInactivity(Integer.toString(h.getMaxInactivity()));
        this.setLastUpdate(Long.toString(h.getLastUpdate()));
        this.setActive(h.getActive() ? "on" : "off");
        this.setHost(h.getHost());
        this.setPort(Integer.toString(h.getPort()));
        this.setTransferGroupName(h.getTransferGroupName());
        this.setHostForReplicationName(h.getHostForReplicationName());
        this.setCheck(h.getCheck() ? "on" : "off");
        this.setReplicate(h.getReplicate() ? "on" : "off");
    }
}
