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

import java.util.Collection;
import java.util.Date;

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.dao.transfer.HostBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.users.User;

/**
 * The Class TransferServerBean.
 */
public class TransferServerBean extends ModelBeanBase implements TransferServer, OjbImplementedBean {

    /** The transfer server. */
    private final ecmwf.common.database.TransferServer transferServer;

    /**
     * Instantiates a new transfer server bean.
     *
     * @param transferServer
     *            the transfer server
     */
    protected TransferServerBean(final ecmwf.common.database.TransferServer transferServer) {
        this.transferServer = transferServer;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return TransferServer.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return this.transferServer;
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
     * Gets the active.
     */
    @Override
    public boolean getActive() {
        return this.transferServer.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the check.
     */
    @Override
    public boolean getCheck() {
        return this.transferServer.getCheck();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last update.
     */
    @Override
    public long getLastUpdate() {
        return this.transferServer.getLastUpdate();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last update date.
     */
    @Override
    public Date getLastUpdateDate() {
        final var date = getLastUpdate();
        return date > 0 ? new Date(date) : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last update duration.
     */
    @Override
    public String getLastUpdateDuration() {
        final var date = getLastUpdate();
        return date > 0 ? Format.formatDuration(System.currentTimeMillis() - date) : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max inactivity.
     */
    @Override
    public int getMaxInactivity() {
        return this.transferServer.getMaxInactivity();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max transfers.
     */
    @Override
    public int getMaxTransfers() {
        return this.transferServer.getMaxTransfers();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return this.transferServer.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers today.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersToday() throws DataFileException {
        try {
            return DataTransferHome.findByTransferServerOnDate(this, new Date());
        } catch (final TransferException e) {
            throw new DataFileException("Error getting DataTransfers for TransferServer '" + this.getId() + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
     */
    @Override
    public void setActive(final boolean param) {
        transferServer.setActive(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the check.
     */
    @Override
    public void setCheck(final boolean param) {
        transferServer.setCheck(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the last update.
     */
    @Override
    public void setLastUpdate(final int param) {
        transferServer.setLastUpdate(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max inactivity.
     */
    @Override
    public void setMaxInactivity(final int param) {
        transferServer.setMaxInactivity(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max transfers.
     */
    @Override
    public void setMaxTransfers(final int param) {
        transferServer.setMaxTransfers(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the name.
     */
    @Override
    public void setName(final String param) {
        transferServer.setName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host.
     */
    @Override
    public String getHost() {
        return transferServer.getHost();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort() {
        return transferServer.getPort();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the host.
     */
    @Override
    public void setHost(final String param) {
        transferServer.setHost(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the port.
     */
    @Override
    public void setPort(final int param) {
        transferServer.setPort(param);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the load.
     */
    @Override
    public long getLoad() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer group name.
     */
    @Override
    public String getTransferGroupName() {
        return transferServer.getTransferGroupName();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer group name.
     */
    @Override
    public void setTransferGroupName(final String name) {
        transferServer.setTransferGroupName(name);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer group.
     */
    @Override
    public TransferGroup getTransferGroup() {
        return new TransferGroupBean(transferServer.getTransferGroup());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host for replication.
     */
    @Override
    public Host getHostForReplication() {
        return new HostBean(transferServer.getHostForReplication());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host for replication name.
     */
    @Override
    public String getHostForReplicationName() {
        return transferServer.getHostForReplicationName();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the host for replication name.
     */
    @Override
    public void setHostForReplicationName(final String name) {
        transferServer.setHostForReplicationName(name);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the replicate.
     */
    @Override
    public void setReplicate(final boolean param) {
        transferServer.setReplicate(param);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the replicate.
     */
    @Override
    public boolean getReplicate() {
        return transferServer.getReplicate();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the report.
     */
    @Override
    public String getReport(final User u) throws DataFileException {
        try {
            final var mi = MasterManager.getMI();
            return mi.getReport(Util.getECpdsSessionFromObject(u), this.transferServer);
        } catch (final Exception e) {
            throw new DataFileException("Error getting report", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final TransferServerBean transferServerBean && equals(transferServerBean);
    }

    /**
     * Equals.
     *
     * @param d
     *            the d
     *
     * @return true, if successful
     */
    public boolean equals(final TransferServerBean d) {
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
        return getClass().getName() + " { " + transferServer + " }";
    }
}
