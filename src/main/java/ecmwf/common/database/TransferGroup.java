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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class TransferGroup.
 */
public class TransferGroup extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1123825978934573742L;

    /** The trg active. */
    protected boolean TRG_ACTIVE;

    /** The trg comment. */
    protected String TRG_COMMENT;

    /** The trg name. */
    protected String TRG_NAME;

    /** The trg cluster name. */
    protected String TRG_CLUSTER_NAME;

    /** The trg cluster weight. */
    protected Integer TRG_CLUSTER_WEIGHT;

    /** The trg replicate. */
    protected boolean TRG_REPLICATE;

    /** The trg filter. */
    protected boolean TRG_FILTER;

    /** The trg backup. */
    protected boolean TRG_BACKUP;

    /** The trg min replication count. */
    protected int TRG_MIN_REPLICATION_COUNT;

    /** The trg min filtering count. */
    protected int TRG_MIN_FILTERING_COUNT;

    /** The trg volume count. */
    protected int TRG_VOLUME_COUNT;

    /** The hos name for backup. */
    protected Integer HOS_NAME_FOR_BACKUP;

    /** The host for backup. */
    protected Host hostForBackup;

    /**
     * Instantiates a new transfer group.
     */
    public TransferGroup() {
    }

    /**
     * Instantiates a new transfer group.
     *
     * @param name
     *            the name
     */
    public TransferGroup(final String name) {
        setName(name);
    }

    /**
     * Gets the min replication count.
     *
     * @return the min replication count
     */
    public int getMinReplicationCount() {
        return TRG_MIN_REPLICATION_COUNT;
    }

    /**
     * Sets the min replication count.
     *
     * @param param
     *            the new min replication count
     */
    public void setMinReplicationCount(final int param) {
        TRG_MIN_REPLICATION_COUNT = param;
    }

    /**
     * Gets the min filtering count.
     *
     * @return the min filtering count
     */
    public int getMinFilteringCount() {
        return TRG_MIN_FILTERING_COUNT;
    }

    /**
     * Sets the min filtering count.
     *
     * @param param
     *            the new min filtering count
     */
    public void setMinFilteringCount(final int param) {
        TRG_MIN_FILTERING_COUNT = param;
    }

    /**
     * Gets the volume count.
     *
     * @return the volume count
     */
    public int getVolumeCount() {
        return TRG_VOLUME_COUNT;
    }

    /**
     * Sets the volume count.
     *
     * @param param
     *            the new volume count
     */
    public void setVolumeCount(final int param) {
        TRG_VOLUME_COUNT = param;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return TRG_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        TRG_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        TRG_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the replicate.
     *
     * @return the replicate
     */
    public boolean getReplicate() {
        return TRG_REPLICATE;
    }

    /**
     * Sets the replicate.
     *
     * @param param
     *            the new replicate
     */
    public void setReplicate(final boolean param) {
        TRG_REPLICATE = param;
    }

    /**
     * Sets the replicate.
     *
     * @param param
     *            the new replicate
     */
    public void setReplicate(final String param) {
        TRG_REPLICATE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    public boolean getBackup() {
        return TRG_BACKUP;
    }

    /**
     * Sets the backup.
     *
     * @param param
     *            the new backup
     */
    public void setBackup(final boolean param) {
        TRG_BACKUP = param;
    }

    /**
     * Sets the backup.
     *
     * @param param
     *            the new backup
     */
    public void setBackup(final String param) {
        TRG_BACKUP = Boolean.parseBoolean(param);
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public boolean getFilter() {
        return TRG_FILTER;
    }

    /**
     * Sets the filter.
     *
     * @param param
     *            the new filter
     */
    public void setFilter(final boolean param) {
        TRG_FILTER = param;
    }

    /**
     * Sets the filter.
     *
     * @param param
     *            the new filter
     */
    public void setFilter(final String param) {
        TRG_FILTER = Boolean.parseBoolean(param);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return TRG_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        TRG_COMMENT = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return TRG_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        TRG_NAME = param;
    }

    /**
     * Gets the cluster name.
     *
     * @return the cluster name
     */
    public String getClusterName() {
        return stringToString(TRG_CLUSTER_NAME, "");
    }

    /**
     * Sets the cluster name.
     *
     * @param param
     *            the new cluster name
     */
    public void setClusterName(final String param) {
        TRG_CLUSTER_NAME = param;
    }

    /**
     * Gets the host for backup name.
     *
     * @return the host for backup name
     */
    public String getHostForBackupName() {
        return integerToString(HOS_NAME_FOR_BACKUP);
    }

    /**
     * Sets the host for backup name.
     *
     * @param param
     *            the new host for backup name
     */
    public void setHostForBackupName(final String param) {
        HOS_NAME_FOR_BACKUP = stringToInteger(param);
    }

    /**
     * Gets the cluster weight.
     *
     * @return the cluster weight
     */
    public Integer getClusterWeight() {
        return integerToInt(TRG_CLUSTER_WEIGHT, 0);
    }

    /**
     * Sets the cluster weight.
     *
     * @param param
     *            the new cluster weight
     */
    public void setClusterWeight(final Integer param) {
        TRG_CLUSTER_WEIGHT = param;
    }

    /**
     * Gets the host for backup.
     *
     * @return the host for backup
     */
    public Host getHostForBackup() {
        return hostForBackup;
    }

    /**
     * Sets the host for backup.
     *
     * @param param
     *            the new host for backup
     */
    public void setHostForBackup(final Host param) {
        hostForBackup = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(TRG_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (TransferGroup) obj;
        return Objects.equals(TRG_NAME, other.TRG_NAME);
    }
}
