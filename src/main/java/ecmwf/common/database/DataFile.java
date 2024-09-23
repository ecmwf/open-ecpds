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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Class DataFile.
 */
public class DataFile extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5867973171150227096L;

    /** The daf arrived time. */
    protected BigDecimal DAF_ARRIVED_TIME;

    /** The daf deleted. */
    protected boolean DAF_DELETED;

    /** The daf standby. */
    protected boolean DAF_STANDBY;

    /** The daf index. */
    protected int DAF_INDEX;

    /** The daf delete original. */
    protected boolean DAF_DELETE_ORIGINAL;

    /** The daf downloaded. */
    protected boolean DAF_DOWNLOADED;

    /** The daf get host. */
    protected String DAF_GET_HOST;

    /** The daf remote host. */
    protected String DAF_REMOTE_HOST;

    /** The daf caller. */
    protected String DAF_CALLER;

    /** The daf get time. */
    protected BigDecimal DAF_GET_TIME;

    /** The daf get duration. */
    protected long DAF_GET_DURATION;

    /** The daf get complete duration. */
    protected long DAF_GET_COMPLETE_DURATION;

    /** The daf ecauth host. */
    protected String DAF_ECAUTH_HOST;

    /** The daf ecauth user. */
    protected String DAF_ECAUTH_USER;

    /** The daf checksum. */
    protected String DAF_CHECKSUM;

    /** The daf filter name. */
    protected String DAF_FILTER_NAME;

    /** The daf filter time. */
    protected BigDecimal DAF_FILTER_TIME;

    /** The daf filter size. */
    protected long DAF_FILTER_SIZE;

    /** The daf id. */
    protected long DAF_ID;

    /** The daf meta stream. */
    protected String DAF_META_STREAM;

    /** The daf meta target. */
    protected String DAF_META_TARGET;

    /** The daf meta time. */
    protected String DAF_META_TIME;

    /** The daf meta type. */
    protected String DAF_META_TYPE;

    /** The daf original. */
    protected String DAF_ORIGINAL;

    /** The daf removed. */
    protected boolean DAF_REMOVED;

    /** The daf size. */
    protected long DAF_SIZE;

    /** The daf source. */
    protected String DAF_SOURCE;

    /** The daf time base. */
    protected BigDecimal DAF_TIME_BASE;

    /** The daf time file. */
    protected BigDecimal DAF_TIME_FILE;

    /** The daf time step. */
    protected long DAF_TIME_STEP;

    /** The mov id. */
    protected Long MOV_ID;

    /** The daf file system. */
    protected Integer DAF_FILE_SYSTEM;

    /** The daf file instance. */
    protected Integer DAF_FILE_INSTANCE;

    /** The trg name. */
    protected String TRG_NAME;

    /** The daf group by. */
    protected String DAF_GROUP_BY;

    /** The hos name for acquisition. */
    protected Integer HOS_NAME_FOR_ACQUISITION;

    /** The transfer group. */
    protected TransferGroup transferGroup;

    /** The monitoring value. */
    protected MonitoringValue monitoringValue;

    /**
     * Instantiates a new data file.
     */
    public DataFile() {
    }

    /**
     * Instantiates a new data file.
     *
     * @param id
     *            the id
     */
    public DataFile(final long id) {
        setId(id);
    }

    /**
     * Gets the host for acquisition name.
     *
     * @return the host for acquisition name
     */
    public String getHostForAcquisitionName() {
        return integerToString(HOS_NAME_FOR_ACQUISITION);
    }

    /**
     * Sets the host for acquisition name.
     *
     * @param param
     *            the new host for acquisition name
     */
    public void setHostForAcquisitionName(final String param) {
        HOS_NAME_FOR_ACQUISITION = stringToInteger(param);
    }

    /**
     * Gets the arrived time.
     *
     * @return the arrived time
     */
    public java.sql.Timestamp getArrivedTime() {
        return bigDecimalToTimestamp(DAF_ARRIVED_TIME);
    }

    /**
     * Sets the arrived time.
     *
     * @param param
     *            the new arrived time
     */
    public void setArrivedTime(final java.sql.Timestamp param) {
        DAF_ARRIVED_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the filter time.
     *
     * @return the filter time
     */
    public java.sql.Timestamp getFilterTime() {
        return bigDecimalToTimestamp(DAF_FILTER_TIME);
    }

    /**
     * Sets the filter time.
     *
     * @param param
     *            the new filter time
     */
    public void setFilterTime(final java.sql.Timestamp param) {
        DAF_FILTER_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the filter size.
     *
     * @return the filter size
     */
    public long getFilterSize() {
        return DAF_FILTER_SIZE;
    }

    /**
     * Sets the filter size.
     *
     * @param param
     *            the new filter size
     */
    public void setFilterSize(final long param) {
        DAF_FILTER_SIZE = param;
    }

    /**
     * Sets the filter size.
     *
     * @param param
     *            the new filter size
     */
    public void setFilterSize(final String param) {
        DAF_FILTER_SIZE = Long.parseLong(param);
    }

    /**
     * Gets the deleted.
     *
     * @return the deleted
     */
    public boolean getDeleted() {
        return DAF_DELETED;
    }

    /**
     * Sets the deleted.
     *
     * @param param
     *            the new deleted
     */
    public void setDeleted(final boolean param) {
        DAF_DELETED = param;
    }

    /**
     * Sets the deleted.
     *
     * @param param
     *            the new deleted
     */
    public void setDeleted(final String param) {
        DAF_DELETED = Boolean.parseBoolean(param);
    }

    /**
     * Gets the standby.
     *
     * @return the standby
     */
    public boolean getStandby() {
        return DAF_STANDBY;
    }

    /**
     * Sets the standby.
     *
     * @param param
     *            the new standby
     */
    public void setStandby(final boolean param) {
        DAF_STANDBY = param;
    }

    /**
     * Sets the standby.
     *
     * @param param
     *            the new standby
     */
    public void setStandby(final String param) {
        DAF_STANDBY = Boolean.parseBoolean(param);
    }

    /**
     * Gets the index.
     *
     * @return the index
     */
    public int getIndex() {
        return DAF_INDEX;
    }

    /**
     * Sets the index.
     *
     * @param param
     *            the new index
     */
    public void setIndex(final int param) {
        DAF_INDEX = param;
    }

    /**
     * Sets the index.
     *
     * @param param
     *            the new index
     */
    public void setIndex(final String param) {
        DAF_INDEX = Integer.parseInt(param);
    }

    /**
     * Gets the delete original.
     *
     * @return the delete original
     */
    public boolean getDeleteOriginal() {
        return DAF_DELETE_ORIGINAL;
    }

    /**
     * Sets the delete original.
     *
     * @param param
     *            the new delete original
     */
    public void setDeleteOriginal(final boolean param) {
        DAF_DELETE_ORIGINAL = param;
    }

    /**
     * Sets the delete original.
     *
     * @param param
     *            the new delete original
     */
    public void setDeleteOriginal(final String param) {
        DAF_DELETE_ORIGINAL = Boolean.parseBoolean(param);
    }

    /**
     * Gets the downloaded.
     *
     * @return the downloaded
     */
    public boolean getDownloaded() {
        return DAF_DOWNLOADED;
    }

    /**
     * Sets the downloaded.
     *
     * @param param
     *            the new downloaded
     */
    public void setDownloaded(final boolean param) {
        DAF_DOWNLOADED = param;
    }

    /**
     * Sets the downloaded.
     *
     * @param param
     *            the new downloaded
     */
    public void setDownloaded(final String param) {
        DAF_DOWNLOADED = Boolean.parseBoolean(param);
    }

    /**
     * Gets the ecauth host.
     *
     * @return the ecauth host
     */
    public String getEcauthHost() {
        return DAF_ECAUTH_HOST;
    }

    /**
     * Sets the ecauth host.
     *
     * @param param
     *            the new ecauth host
     */
    public void setEcauthHost(final String param) {
        DAF_ECAUTH_HOST = param;
    }

    /**
     * Gets the gets the host.
     *
     * @return the gets the host
     */
    public String getGetHost() {
        return DAF_GET_HOST;
    }

    /**
     * Sets the gets the host.
     *
     * @param param
     *            the new gets the host
     */
    public void setGetHost(final String param) {
        DAF_GET_HOST = param;
    }

    /**
     * Gets the remote host.
     *
     * @return the remote host
     */
    public String getRemoteHost() {
        return DAF_REMOTE_HOST;
    }

    /**
     * Sets the remote host.
     *
     * @param param
     *            the new remote host
     */
    public void setRemoteHost(final String param) {
        DAF_REMOTE_HOST = param;
    }

    /**
     * Gets the caller.
     *
     * @return the caller
     */
    public String getCaller() {
        return DAF_CALLER;
    }

    /**
     * Sets the caller.
     *
     * @param param
     *            the new caller
     */
    public void setCaller(final String param) {
        DAF_CALLER = strim(param, 2047);
    }

    /**
     * Gets the checksum.
     *
     * @return the checksum
     */
    public String getChecksum() {
        return DAF_CHECKSUM;
    }

    /**
     * Sets the checksum.
     *
     * @param param
     *            the new checksum
     */
    public void setChecksum(final String param) {
        DAF_CHECKSUM = strim(param, 33);
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    public String getFilterName() {
        return DAF_FILTER_NAME;
    }

    /**
     * Sets the filter name.
     *
     * @param param
     *            the new filter name
     */
    public void setFilterName(final String param) {
        DAF_FILTER_NAME = param;
    }

    /**
     * Gets the ecauth user.
     *
     * @return the ecauth user
     */
    public String getEcauthUser() {
        return DAF_ECAUTH_USER;
    }

    /**
     * Sets the ecauth user.
     *
     * @param param
     *            the new ecauth user
     */
    public void setEcauthUser(final String param) {
        DAF_ECAUTH_USER = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return DAF_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        DAF_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        DAF_ID = Long.parseLong(param);
    }

    /**
     * Gets the meta stream.
     *
     * @return the meta stream
     */
    public String getMetaStream() {
        return DAF_META_STREAM;
    }

    /**
     * Sets the meta stream.
     *
     * @param param
     *            the new meta stream
     */
    public void setMetaStream(final String param) {
        DAF_META_STREAM = param;
    }

    /**
     * Gets the meta target.
     *
     * @return the meta target
     */
    public String getMetaTarget() {
        return DAF_META_TARGET;
    }

    /**
     * Sets the meta target.
     *
     * @param param
     *            the new meta target
     */
    public void setMetaTarget(final String param) {
        DAF_META_TARGET = param;
    }

    /**
     * Gets the meta time.
     *
     * @return the meta time
     */
    public String getMetaTime() {
        return DAF_META_TIME;
    }

    /**
     * Sets the meta time.
     *
     * @param param
     *            the new meta time
     */
    public void setMetaTime(final String param) {
        DAF_META_TIME = param;
    }

    /**
     * Gets the meta type.
     *
     * @return the meta type
     */
    public String getMetaType() {
        return DAF_META_TYPE;
    }

    /**
     * Sets the meta type.
     *
     * @param param
     *            the new meta type
     */
    public void setMetaType(final String param) {
        DAF_META_TYPE = param;
    }

    /**
     * Gets the original.
     *
     * @return the original
     */
    public String getOriginal() {
        return DAF_ORIGINAL;
    }

    /**
     * Sets the original.
     *
     * @param param
     *            the new original
     */
    public void setOriginal(final String param) {
        DAF_ORIGINAL = param;
    }

    /**
     * Gets the removed.
     *
     * @return the removed
     */
    public boolean getRemoved() {
        return DAF_REMOVED;
    }

    /**
     * Sets the removed.
     *
     * @param param
     *            the new removed
     */
    public void setRemoved(final boolean param) {
        DAF_REMOVED = param;
    }

    /**
     * Sets the removed.
     *
     * @param param
     *            the new removed
     */
    public void setRemoved(final String param) {
        DAF_REMOVED = Boolean.parseBoolean(param);
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return DAF_SIZE;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final long param) {
        DAF_SIZE = param;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final String param) {
        DAF_SIZE = Long.parseLong(param);
    }

    /**
     * Sets the gets the duration.
     *
     * @param param
     *            the new gets the duration
     */
    public void setGetDuration(final long param) {
        DAF_GET_DURATION = param;
    }

    /**
     * Sets the gets the duration.
     *
     * @param param
     *            the new gets the duration
     */
    public void setGetDuration(final String param) {
        DAF_GET_DURATION = Long.parseLong(param);
    }

    /**
     * Gets the gets the duration.
     *
     * @return the gets the duration
     */
    public long getGetDuration() {
        return DAF_GET_DURATION;
    }

    /**
     * Sets the gets the complete duration.
     *
     * @param param
     *            the new gets the complete duration
     */
    public void setGetCompleteDuration(final long param) {
        DAF_GET_COMPLETE_DURATION = param;
    }

    /**
     * Sets the gets the complete duration.
     *
     * @param param
     *            the new gets the complete duration
     */
    public void setGetCompleteDuration(final String param) {
        DAF_GET_COMPLETE_DURATION = Long.parseLong(param);
    }

    /**
     * Gets the gets the complete duration.
     *
     * @return the gets the complete duration
     */
    public long getGetCompleteDuration() {
        return DAF_GET_COMPLETE_DURATION;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return DAF_SOURCE;
    }

    /**
     * Sets the source.
     *
     * @param param
     *            the new source
     */
    public void setSource(final String param) {
        DAF_SOURCE = param;
    }

    /**
     * Gets the time base.
     *
     * @return the time base
     */
    public java.sql.Timestamp getTimeBase() {
        return bigDecimalToTimestamp(DAF_TIME_BASE);
    }

    /**
     * Sets the time base.
     *
     * @param param
     *            the new time base
     */
    public void setTimeBase(final java.sql.Timestamp param) {
        DAF_TIME_BASE = timestampToBigDecimal(param);
    }

    /**
     * Gets the gets the time.
     *
     * @return the gets the time
     */
    public java.sql.Timestamp getGetTime() {
        return bigDecimalToTimestamp(DAF_GET_TIME);
    }

    /**
     * Sets the gets the time.
     *
     * @param param
     *            the new gets the time
     */
    public void setGetTime(final java.sql.Timestamp param) {
        DAF_GET_TIME = timestampToBigDecimal(param);
    }

    /**
     * Gets the time file.
     *
     * @return the time file
     */
    public java.sql.Timestamp getTimeFile() {
        return bigDecimalToTimestamp(DAF_TIME_FILE);
    }

    /**
     * Sets the time file.
     *
     * @param param
     *            the new time file
     */
    public void setTimeFile(final java.sql.Timestamp param) {
        DAF_TIME_FILE = timestampToBigDecimal(param);
    }

    /**
     * Gets the time step.
     *
     * @return the time step
     */
    public long getTimeStep() {
        return DAF_TIME_STEP;
    }

    /**
     * Sets the time step.
     *
     * @param param
     *            the new time step
     */
    public void setTimeStep(final long param) {
        DAF_TIME_STEP = param;
    }

    /**
     * Sets the time step.
     *
     * @param param
     *            the new time step
     */
    public void setTimeStep(final String param) {
        DAF_TIME_STEP = Long.parseLong(param);
    }

    /**
     * Gets the monitoring value id.
     *
     * @return the monitoring value id
     */
    public Long getMonitoringValueId() {
        return MOV_ID;
    }

    /**
     * Sets the monitoring value id.
     *
     * @param param
     *            the new monitoring value id
     */
    public void setMonitoringValueId(final Long param) {
        MOV_ID = param;
    }

    /**
     * Gets the file instance.
     *
     * @return the file instance
     */
    public Integer getFileInstance() {
        return DAF_FILE_INSTANCE;
    }

    /**
     * Sets the file instance.
     *
     * @param param
     *            the new file instance
     */
    public void setFileInstance(final Integer param) {
        DAF_FILE_INSTANCE = param;
    }

    /**
     * Sets the file instance.
     *
     * @param param
     *            the new file instance
     */
    public void setFileInstance(final int param) {
        DAF_FILE_INSTANCE = param;
    }

    /**
     * Gets the file system.
     *
     * @return the file system
     */
    public Integer getFileSystem() {
        return DAF_FILE_SYSTEM;
    }

    /**
     * Sets the file system.
     *
     * @param param
     *            the new file system
     */
    public void setFileSystem(final Integer param) {
        DAF_FILE_SYSTEM = param;
    }

    /**
     * Sets the file system.
     *
     * @param param
     *            the new file system
     */
    public void setFileSystem(final int param) {
        DAF_FILE_SYSTEM = param;
    }

    /**
     * Gets the group by.
     *
     * @return the group by
     */
    public String getGroupBy() {
        return DAF_GROUP_BY;
    }

    /**
     * Sets the group by.
     *
     * @param param
     *            the new group by
     */
    public void setGroupBy(final String param) {
        DAF_GROUP_BY = strim(param, 64);
    }

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    public String getTransferGroupName() {
        return TRG_NAME;
    }

    /**
     * Sets the transfer group name.
     *
     * @param param
     *            the new transfer group name
     */
    public void setTransferGroupName(final String param) {
        TRG_NAME = param;
    }

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     */
    public TransferGroup getTransferGroup() {
        return transferGroup;
    }

    /**
     * Sets the transfer group.
     *
     * @param param
     *            the new transfer group
     */
    public void setTransferGroup(final TransferGroup param) {
        transferGroup = param;
    }

    /**
     * Gets the monitoring value.
     *
     * @return the monitoring value
     */
    public MonitoringValue getMonitoringValue() {
        return monitoringValue;
    }

    /**
     * Sets the monitoring value.
     *
     * @param param
     *            the new monitoring value
     */
    public void setMonitoringValue(final MonitoringValue param) {
        monitoringValue = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(DAF_ID);
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
        final var other = (DataFile) obj;
        return DAF_ID == other.DAF_ID;
    }
}
