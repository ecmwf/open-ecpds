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
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return TransferServer.class.getName();
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return this.transferServer;
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
     * Gets the active.
     *
     * @return the active
     */
    @Override
    public boolean getActive() {
        return this.transferServer.getActive();
    }

    /**
     * Gets the check.
     *
     * @return the check
     */
    @Override
    public boolean getCheck() {
        return this.transferServer.getCheck();
    }

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    @Override
    public long getLastUpdate() {
        return this.transferServer.getLastUpdate();
    }

    /**
     * Gets the last update date.
     *
     * @return the last update date
     */
    @Override
    public Date getLastUpdateDate() {
        final var date = getLastUpdate();
        return date > 0 ? new Date(date) : null;
    }

    /**
     * Gets the last update duration.
     *
     * @return the last update duration
     */
    @Override
    public String getLastUpdateDuration() {
        final var date = getLastUpdate();
        return date > 0 ? Format.formatDuration(System.currentTimeMillis() - date) : null;
    }

    /**
     * Gets the max inactivity.
     *
     * @return the max inactivity
     */
    @Override
    public int getMaxInactivity() {
        return this.transferServer.getMaxInactivity();
    }

    /**
     * Gets the max transfers.
     *
     * @return the max transfers
     */
    @Override
    public int getMaxTransfers() {
        return this.transferServer.getMaxTransfers();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.transferServer.getName();
    }

    /**
     * Gets the data transfers today.
     *
     * @return the data transfers today
     *
     * @throws DataFileException
     *             the data file exception
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
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    @Override
    public void setActive(final boolean param) {
        transferServer.setActive(param);
    }

    /**
     * Sets the check.
     *
     * @param param
     *            the new check
     */
    @Override
    public void setCheck(final boolean param) {
        transferServer.setCheck(param);
    }

    /**
     * Sets the last update.
     *
     * @param param
     *            the new last update
     */
    @Override
    public void setLastUpdate(final int param) {
        transferServer.setLastUpdate(param);
    }

    /**
     * Sets the max inactivity.
     *
     * @param param
     *            the new max inactivity
     */
    @Override
    public void setMaxInactivity(final int param) {
        transferServer.setMaxInactivity(param);
    }

    /**
     * Sets the max transfers.
     *
     * @param param
     *            the new max transfers
     */
    @Override
    public void setMaxTransfers(final int param) {
        transferServer.setMaxTransfers(param);
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    @Override
    public void setName(final String param) {
        transferServer.setName(param);
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    @Override
    public String getHost() {
        return transferServer.getHost();
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    @Override
    public int getPort() {
        return transferServer.getPort();
    }

    /**
     * Sets the host.
     *
     * @param param
     *            the new host
     */
    @Override
    public void setHost(final String param) {
        transferServer.setHost(param);
    }

    /**
     * Sets the port.
     *
     * @param param
     *            the new port
     */
    @Override
    public void setPort(final int param) {
        transferServer.setPort(param);
    }

    /**
     * Gets the load.
     *
     * @return the load
     */
    @Override
    public long getLoad() {
        return 0;
    }

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    @Override
    public String getTransferGroupName() {
        return transferServer.getTransferGroupName();
    }

    /**
     * Sets the transfer group name.
     *
     * @param name
     *            the new transfer group name
     */
    @Override
    public void setTransferGroupName(final String name) {
        transferServer.setTransferGroupName(name);
    }

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     */
    @Override
    public TransferGroup getTransferGroup() {
        return new TransferGroupBean(transferServer.getTransferGroup());
    }

    /**
     * Gets the host for replication.
     *
     * @return the host for replication
     */
    @Override
    public Host getHostForReplication() {
        return new HostBean(transferServer.getHostForReplication());
    }

    /**
     * Gets the host for replication name.
     *
     * @return the host for replication name
     */
    @Override
    public String getHostForReplicationName() {
        return transferServer.getHostForReplicationName();
    }

    /**
     * Sets the host for replication name.
     *
     * @param name
     *            the new host for replication name
     */
    @Override
    public void setHostForReplicationName(final String name) {
        transferServer.setHostForReplicationName(name);
    }

    /**
     * Sets the replicate.
     *
     * @param param
     *            the new replicate
     */
    @Override
    public void setReplicate(final boolean param) {
        transferServer.setReplicate(param);
    }

    /**
     * Gets the replicate.
     *
     * @return the replicate
     */
    @Override
    public boolean getReplicate() {
        return transferServer.getReplicate();
    }

    /**
     * Gets the report.
     *
     * @param u
     *            the u
     *
     * @return the report
     *
     * @throws DataFileException
     *             the data file exception
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
     * Equals.
     *
     * @param o
     *            the o
     *
     * @return true, if successful
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
        return getClass().getName() + " { " + transferServer + " }";
    }
}
