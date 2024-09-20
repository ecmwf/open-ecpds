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

package ecmwf.ecpds.master.plugin.http.controller.datafile.transfergroup;

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

import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.web.controller.ECMWFActionForm;

/**
 * The Class TransferGroupActionForm.
 */
public class TransferGroupActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 843631750353607452L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(TransferGroupActionForm.class);

    /** The id. */
    private String id = "";

    /** The name. */
    private String name = "";

    /** The cluster name. */
    private String clusterName = "";

    /** The comment. */
    private String comment = "";

    /** The active. */
    private String active = "";

    /** The replicate. */
    private String replicate = "";

    /** The filter. */
    private String filter = "";

    /** The backup. */
    private String backup = "";

    /** The min filtering count. */
    private String minFilteringCount = "";

    /** The cluster weight. */
    private String clusterWeight = "";

    /** The min replication count. */
    private String minReplicationCount = "";

    /** The volume count. */
    private String volumeCount = "";

    /** The host for backup name. */
    private String hostForBackupName = "";

    /** The transfer group. */
    private TransferGroup transferGroup = null;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
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
     * Gets the cluster name.
     *
     * @return the cluster name
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
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
     * Gets the replicate.
     *
     * @return the replicate
     */
    public String getReplicate() {
        return replicate;
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    public String getBackup() {
        return backup;
    }

    /**
     * Gets the min filtering count.
     *
     * @return the min filtering count
     */
    public String getMinFilteringCount() {
        return minFilteringCount;
    }

    /**
     * Gets the cluster weight.
     *
     * @return the cluster weight
     */
    public String getClusterWeight() {
        return clusterWeight;
    }

    /**
     * Gets the min replication count.
     *
     * @return the min replication count
     */
    public String getMinReplicationCount() {
        return minReplicationCount;
    }

    /**
     * Gets the volume count.
     *
     * @return the volume count
     */
    public String getVolumeCount() {
        return volumeCount;
    }

    /**
     * Sets the volume count.
     *
     * @param string
     *            the new volume count
     */
    public void setVolumeCount(final String string) {
        volumeCount = string;
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
     * Sets the cluster name.
     *
     * @param string
     *            the new cluster name
     */
    public void setClusterName(final String string) {
        clusterName = string;
    }

    /**
     * Sets the comment.
     *
     * @param string
     *            the new comment
     */
    public void setComment(final String string) {
        comment = string;
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
     * Sets the filter.
     *
     * @param string
     *            the new filter
     */
    public void setFilter(final String string) {
        filter = string;
    }

    /**
     * Sets the backup.
     *
     * @param string
     *            the new backup
     */
    public void setBackup(final String string) {
        backup = string;
    }

    /**
     * Sets the min filtering count.
     *
     * @param string
     *            the new min filtering count
     */
    public void setMinFilteringCount(final String string) {
        minFilteringCount = string;
    }

    /**
     * Sets the cluster weight.
     *
     * @param string
     *            the new cluster weight
     */
    public void setClusterWeight(final String string) {
        clusterWeight = string;
    }

    /**
     * Sets the min replication count.
     *
     * @param string
     *            the new min replication count
     */
    public void setMinReplicationCount(final String string) {
        minReplicationCount = string;
    }

    /**
     * Gets the host for backup name.
     *
     * @return the host for backup name
     */
    public String getHostForBackupName() {
        return hostForBackupName;
    }

    /**
     * Sets the host for backup name.
     *
     * @param string
     *            the new host for backup name
     */
    public void setHostForBackupName(final String string) {
        hostForBackupName = string;
    }

    /**
     * Gets the transfer servers.
     *
     * @return the transfer servers
     */
    public Collection<TransferServer> getTransferServers() {
        try {
            if (this.transferGroup != null) {
                return transferGroup.getTransferServers();
            }
        } catch (final DataFileException e) {
            log.error("Problem getting TransferServers", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Reset.
     *
     * @param map
     *            the map
     * @param req
     *            the req
     */
    @Override
    public void reset(final ActionMapping map, final HttpServletRequest req) {
        resetFields();
    }

    /**
     * Gets the host for backup options.
     *
     * @return the host for backup options
     */
    public Collection<Host> getHostForBackupOptions() {
        final List<Host> backupHosts = new ArrayList<>();
        try {
            for (final Host host : HostHome.findAll()) {
                if (HostOption.BACKUP.equals(host.getType())) {
                    backupHosts.add(host);
                }
            }
        } catch (final TransferException e) {
            log.error("Error getting Hosts", e);
        }
        return backupHosts;
    }

    /**
     * Blank the ActionForm in order to be able to insert a new element.
     */

    protected void resetFields() {
        this.id = "";
        this.name = "";
        this.clusterName = "";
        this.comment = "";
        this.active = "off";
        this.replicate = "off";
        this.filter = "off";
        this.backup = "off";
        this.clusterWeight = "0";
        this.minFilteringCount = "0";
        this.minReplicationCount = "0";
        this.hostForBackupName = "";
    }

    /**
     * Populate transfer group.
     *
     * @param h
     *            the h
     */
    protected void populateTransferGroup(final TransferGroup h) {
        h.setName(this.getName());
        h.setClusterName(this.getClusterName());
        h.setComment(this.getComment());
        h.setActive(convertToBoolean(this.getActive()));
        h.setReplicate(convertToBoolean(this.getReplicate()));
        h.setFilter(convertToBoolean(this.getFilter()));
        h.setBackup(convertToBoolean(this.getBackup()));
        h.setHostForBackupName(this.getHostForBackupName());
        h.setMinFilteringCount(convertToInt(this.getMinFilteringCount()));
        h.setClusterWeight(convertToInt(this.getClusterWeight()));
        h.setMinReplicationCount(convertToInt(this.getMinReplicationCount()));
        h.setVolumeCount(convertToInt(this.getVolumeCount()));
    }

    /**
     * Populate from transfer group.
     *
     * @param h
     *            the h
     */
    protected void populateFromTransferGroup(final TransferGroup h) {
        id = h.getId();
        this.setName(h.getName());
        this.setClusterName(h.getClusterName());
        this.setComment(h.getComment());
        this.setActive(h.getActive() ? "on" : "off");
        this.setReplicate(h.getReplicate() ? "on" : "off");
        this.setFilter(h.getFilter() ? "on" : "off");
        this.setBackup(h.getBackup() ? "on" : "off");
        this.setHostForBackupName(h.getHostForBackupName());
        this.setMinFilteringCount(Integer.toString(h.getMinFilteringCount()));
        this.setClusterWeight(Integer.toString(h.getClusterWeight()));
        this.setMinReplicationCount(Integer.toString(h.getMinReplicationCount()));
        this.setVolumeCount(Integer.toString(h.getVolumeCount()));
        this.transferGroup = h;
    }
}
