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

package ecmwf.ecpds.master.plugin.http.dao.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import ecmwf.common.database.DataBaseObject;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.transfer.HostBean;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferServerHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class TransferGroupBean.
 */
public class TransferGroupBean extends ModelBeanBase implements TransferGroup, OjbImplementedBean {

    /** The transfer group. */
    private final ecmwf.common.database.TransferGroup transferGroup;

    /** The added transfer servers. */
    private final Collection<TransferServer> addedTransferServers = new ArrayList<>();

    /** The deleted transfer servers. */
    private final Collection<TransferServer> deletedTransferServers = new ArrayList<>();

    /**
     * Instantiates a new transfer group bean.
     *
     * @param transferGroup
     *            the transfer group
     */
    protected TransferGroupBean(final ecmwf.common.database.TransferGroup transferGroup) {
        this.transferGroup = transferGroup;
    }

    /**
     * Gets the host for backup.
     *
     * @return the host for backup
     */
    @Override
    public Host getHostForBackup() {
        return new HostBean(transferGroup.getHostForBackup());
    }

    /**
     * Gets the host for backup name.
     *
     * @return the host for backup name
     */
    @Override
    public String getHostForBackupName() {
        return transferGroup.getHostForBackupName();
    }

    /**
     * Sets the host for backup name.
     *
     * @param name
     *            the new host for backup name
     */
    @Override
    public void setHostForBackupName(final String name) {
        transferGroup.setHostForBackupName(name);
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return TransferGroup.class.getName();
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return transferGroup;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return getName();
    }

    /**
     * Gets the min filtering count.
     *
     * @return the min filtering count
     */
    @Override
    public int getMinFilteringCount() {
        return transferGroup.getMinFilteringCount();
    }

    /**
     * Gets the min replication count.
     *
     * @return the min replication count
     */
    @Override
    public int getMinReplicationCount() {
        return transferGroup.getMinReplicationCount();
    }

    /**
     * Gets the cluster weight.
     *
     * @return the cluster weight
     */
    @Override
    public int getClusterWeight() {
        return transferGroup.getClusterWeight();
    }

    /**
     * Gets the volume count.
     *
     * @return the volume count
     */
    @Override
    public int getVolumeCount() {
        return transferGroup.getVolumeCount();
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    @Override
    public boolean getActive() {
        return transferGroup.getActive();
    }

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    @Override
    public boolean getBackup() {
        return transferGroup.getBackup();
    }

    /**
     * Gets the replicate.
     *
     * @return the replicate
     */
    @Override
    public boolean getReplicate() {
        return transferGroup.getReplicate();
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    @Override
    public boolean getFilter() {
        return transferGroup.getFilter();
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    @Override
    public String getComment() {
        return transferGroup.getComment();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return transferGroup.getName();
    }

    /**
     * Gets the cluster name.
     *
     * @return the cluster name
     */
    @Override
    public String getClusterName() {
        return transferGroup.getClusterName();
    }

    /**
     * Gets the transfer servers.
     *
     * @return the transfer servers
     *
     * @throws DataFileException
     *             the data file exception
     */
    @Override
    public Collection<TransferServer> getTransferServers() throws DataFileException {
        return TransferServerHome.findByTransferGroup(this);
    }

    /**
     * Adds the transfer server.
     *
     * @param h
     *            the h
     */
    @Override
    public synchronized void addTransferServer(final TransferServer h) {
        addedTransferServers.add(h);
    }

    /**
     * Delete transfer server.
     *
     * @param h
     *            the h
     */
    @Override
    public synchronized void deleteTransferServer(final TransferServer h) {
        deletedTransferServers.add(h);
    }

    /**
     * Sets the min filtering count.
     *
     * @param param
     *            the new min filtering count
     */
    @Override
    public void setMinFilteringCount(final int param) {
        transferGroup.setMinFilteringCount(param);
    }

    /**
     * Sets the volume count.
     *
     * @param param
     *            the new volume count
     */
    @Override
    public void setVolumeCount(final int param) {
        transferGroup.setVolumeCount(param);
    }

    /**
     * Sets the min replication count.
     *
     * @param param
     *            the new min replication count
     */
    @Override
    public void setMinReplicationCount(final int param) {
        transferGroup.setMinReplicationCount(param);
    }

    /**
     * Sets the cluster weight.
     *
     * @param param
     *            the new cluster weight
     */
    @Override
    public void setClusterWeight(final int param) {
        transferGroup.setClusterWeight(param);
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    @Override
    public void setActive(final boolean param) {
        transferGroup.setActive(param);
    }

    /**
     * Sets the replicate.
     *
     * @param param
     *            the new replicate
     */
    @Override
    public void setReplicate(final boolean param) {
        transferGroup.setReplicate(param);
    }

    /**
     * Sets the filter.
     *
     * @param param
     *            the new filter
     */
    @Override
    public void setFilter(final boolean param) {
        transferGroup.setFilter(param);
    }

    /**
     * Sets the backup.
     *
     * @param param
     *            the new backup
     */
    @Override
    public void setBackup(final boolean param) {
        transferGroup.setBackup(param);
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    @Override
    public void setComment(final String param) {
        transferGroup.setComment(param);
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    @Override
    public void setName(final String param) {
        transferGroup.setName(param);
    }

    /**
     * Sets the cluster name.
     *
     * @param param
     *            the new cluster name
     */
    @Override
    public void setClusterName(final String param) {
        transferGroup.setClusterName(param);
    }

    /**
     * Gets the added transfer servers.
     *
     * @return the added transfer servers
     */
    protected Collection<TransferServer> getAddedTransferServers() {
        return this.addedTransferServers;
    }

    /**
     * Gets the deleted transfer servers.
     *
     * @return the deleted transfer servers
     */
    protected Collection<TransferServer> getDeletedTransferServers() {
        return this.deletedTransferServers;
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
        return o instanceof final TransferGroupBean transferGroupBean && equals(transferGroupBean);
    }

    /**
     * Equals.
     *
     * @param d
     *            the d
     *
     * @return true, if successful
     */
    public boolean equals(final TransferGroupBean d) {
        return getName().equals(d.getName());
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + transferGroup + " }";
    }
}
