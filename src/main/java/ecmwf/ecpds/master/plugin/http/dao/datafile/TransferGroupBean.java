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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
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
     * {@inheritDoc}
     *
     * Gets the host for backup.
     */
    @Override
    public Host getHostForBackup() {
        return new HostBean(transferGroup.getHostForBackup());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host for backup name.
     */
    @Override
    public String getHostForBackupName() {
        return transferGroup.getHostForBackupName();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the host for backup name.
     */
    @Override
    public void setHostForBackupName(final String name) {
        transferGroup.setHostForBackupName(name);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return TransferGroup.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return transferGroup;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the min filtering count.
     */
    @Override
    public int getMinFilteringCount() {
        return transferGroup.getMinFilteringCount();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the min replication count.
     */
    @Override
    public int getMinReplicationCount() {
        return transferGroup.getMinReplicationCount();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the cluster weight.
     */
    @Override
    public int getClusterWeight() {
        return transferGroup.getClusterWeight();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the volume count.
     */
    @Override
    public int getVolumeCount() {
        return transferGroup.getVolumeCount();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the active.
     */
    @Override
    public boolean getActive() {
        return transferGroup.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the backup.
     */
    @Override
    public boolean getBackup() {
        return transferGroup.getBackup();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the replicate.
     */
    @Override
    public boolean getReplicate() {
        return transferGroup.getReplicate();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the filter.
     */
    @Override
    public boolean getFilter() {
        return transferGroup.getFilter();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the comment.
     */
    @Override
    public String getComment() {
        return transferGroup.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return transferGroup.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the cluster name.
     */
    @Override
    public String getClusterName() {
        return transferGroup.getClusterName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer servers.
     */
    @Override
    public Collection<TransferServer> getTransferServers() throws DataFileException {
        return TransferServerHome.findByTransferGroup(this);
    }

    /**
     * {@inheritDoc}
     *
     * Adds the transfer server.
     */
    @Override
    public synchronized void addTransferServer(final TransferServer h) {
        addedTransferServers.add(h);
    }

    /**
     * {@inheritDoc}
     *
     * Delete transfer server.
     */
    @Override
    public synchronized void deleteTransferServer(final TransferServer h) {
        deletedTransferServers.add(h);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the min filtering count.
     */
    @Override
    public void setMinFilteringCount(final int param) {
        transferGroup.setMinFilteringCount(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the volume count.
     */
    @Override
    public void setVolumeCount(final int param) {
        transferGroup.setVolumeCount(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the min replication count.
     */
    @Override
    public void setMinReplicationCount(final int param) {
        transferGroup.setMinReplicationCount(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the cluster weight.
     */
    @Override
    public void setClusterWeight(final int param) {
        transferGroup.setClusterWeight(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
     */
    @Override
    public void setActive(final boolean param) {
        transferGroup.setActive(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the replicate.
     */
    @Override
    public void setReplicate(final boolean param) {
        transferGroup.setReplicate(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the filter.
     */
    @Override
    public void setFilter(final boolean param) {
        transferGroup.setFilter(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the backup.
     */
    @Override
    public void setBackup(final boolean param) {
        transferGroup.setBackup(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the comment.
     */
    @Override
    public void setComment(final String param) {
        transferGroup.setComment(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the name.
     */
    @Override
    public void setName(final String param) {
        transferGroup.setName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the cluster name.
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
     * {@inheritDoc}
     *
     * Equals.
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
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + transferGroup + " }";
    }
}
