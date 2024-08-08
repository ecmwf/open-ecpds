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
 * Set of TransferServers which are viewed as resilient unit.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.web.model.ModelBean;

/**
 * The Interface TransferGroup.
 */
public interface TransferGroup extends ModelBean {

    /**
     * Gets the host for backup.
     *
     * @return the host for backup
     *
     * @throws DataFileException
     *             the data file exception
     */
    Host getHostForBackup() throws DataFileException;

    /**
     * Gets the host for backup name.
     *
     * @return the host for backup name
     */
    String getHostForBackupName();

    /**
     * Sets the host for backup name.
     *
     * @param name
     *            the new host for backup name
     */
    void setHostForBackupName(String name);

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the cluster name.
     *
     * @return the cluster name
     */
    String getClusterName();

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Gets the replicate.
     *
     * @return the replicate
     */
    boolean getReplicate();

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    boolean getFilter();

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    boolean getBackup();

    /**
     * Sets the backup.
     *
     * @param b
     *            the new backup
     */
    void setBackup(boolean b);

    /**
     * Sets the volume count.
     *
     * @param param
     *            the new volume count
     */
    void setVolumeCount(int param);

    /**
     * Gets the volume count.
     *
     * @return the volume count
     */
    int getVolumeCount();

    /**
     * Gets the min filtering count.
     *
     * @return the min filtering count
     */
    int getMinFilteringCount();

    /**
     * Gets the cluster weight.
     *
     * @return the cluster weight
     */
    int getClusterWeight();

    /**
     * Gets the min replication count.
     *
     * @return the min replication count
     */
    int getMinReplicationCount();

    /**
     * Sets the name.
     *
     * @param n
     *            the new name
     */
    void setName(String n);

    /**
     * Sets the cluster name.
     *
     * @param n
     *            the new cluster name
     */
    void setClusterName(String n);

    /**
     * Sets the comment.
     *
     * @param c
     *            the new comment
     */
    void setComment(String c);

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    void setActive(boolean b);

    /**
     * Sets the replicate.
     *
     * @param b
     *            the new replicate
     */
    void setReplicate(boolean b);

    /**
     * Sets the filter.
     *
     * @param b
     *            the new filter
     */
    void setFilter(boolean b);

    /**
     * Sets the min filtering count.
     *
     * @param i
     *            the new min filtering count
     */
    void setMinFilteringCount(int i);

    /**
     * Sets the cluster weight.
     *
     * @param i
     *            the new cluster weight
     */
    void setClusterWeight(int i);

    /**
     * Sets the min replication count.
     *
     * @param i
     *            the new min replication count
     */
    void setMinReplicationCount(int i);

    /**
     * Gets the transfer servers.
     *
     * @return the transfer servers
     *
     * @throws DataFileException
     *             the data file exception
     */
    Collection<TransferServer> getTransferServers() throws DataFileException;

    /**
     * Adds the transfer server.
     *
     * @param s
     *            the s
     */
    void addTransferServer(TransferServer s);

    /**
     * Delete transfer server.
     *
     * @param s
     *            the s
     */
    void deleteTransferServer(TransferServer s);
}
