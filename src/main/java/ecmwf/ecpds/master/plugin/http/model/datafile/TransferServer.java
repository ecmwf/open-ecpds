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

package ecmwf.ecpds.master.plugin.http.model.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Date;

import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

/**
 * The Interface TransferServer.
 */
public interface TransferServer extends ModelBean {

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the max transfers.
     *
     * @return the max transfers
     */
    int getMaxTransfers();

    /**
     * Gets the max inactivity.
     *
     * @return the max inactivity
     */
    int getMaxInactivity();

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    long getLastUpdate();

    /**
     * Gets the last update date.
     *
     * @return the last update date
     */
    Date getLastUpdateDate();

    /**
     * Gets the last update duration.
     *
     * @return the last update duration
     */
    String getLastUpdateDuration();

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Gets the check.
     *
     * @return the check
     */
    boolean getCheck();

    /**
     * Gets the host.
     *
     * @return the host
     */
    String getHost();

    /**
     * Gets the port.
     *
     * @return the port
     */
    int getPort();

    /**
     * Gets the load.
     *
     * @return the load
     */
    long getLoad();

    /**
     * Gets the data transfers today.
     *
     * @return the data transfers today
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    Collection<DataTransfer> getDataTransfersToday() throws DataFileException;

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    TransferGroup getTransferGroup() throws DataFileException;

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    String getTransferGroupName();

    /**
     * Gets the host for replication.
     *
     * @return the host for replication
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    Host getHostForReplication() throws DataFileException;

    /**
     * Gets the host for replication name.
     *
     * @return the host for replication name
     */
    String getHostForReplicationName();

    /**
     * Gets the replicate.
     *
     * @return the replicate
     */
    boolean getReplicate();

    /**
     * Sets the name.
     *
     * @param s
     *            the new name
     */
    void setName(String s);

    /**
     * Sets the max transfers.
     *
     * @param i
     *            the new max transfers
     */
    void setMaxTransfers(int i);

    /**
     * Sets the max inactivity.
     *
     * @param i
     *            the new max inactivity
     */
    void setMaxInactivity(int i);

    /**
     * Sets the last update.
     *
     * @param i
     *            the new last update
     */
    void setLastUpdate(int i);

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    void setActive(boolean b);

    /**
     * Sets the check.
     *
     * @param b
     *            the new check
     */
    void setCheck(boolean b);

    /**
     * Sets the host.
     *
     * @param s
     *            the new host
     */
    void setHost(String s);

    /**
     * Sets the port.
     *
     * @param i
     *            the new port
     */
    void setPort(int i);

    /**
     * Sets the transfer group name.
     *
     * @param name
     *            the new transfer group name
     */
    void setTransferGroupName(String name);

    /**
     * Sets the host for replication name.
     *
     * @param name
     *            the new host for replication name
     */
    void setHostForReplicationName(String name);

    /**
     * Sets the replicate.
     *
     * @param b
     *            the new replicate
     */
    void setReplicate(boolean b);

    /**
     * Gets the report.
     *
     * @param u
     *            the u
     *
     * @return the report
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    String getReport(User u) throws DataFileException;
}
