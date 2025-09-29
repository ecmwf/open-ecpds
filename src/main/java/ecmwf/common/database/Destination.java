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

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Class Destination.
 */
public class Destination extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5539762981661489017L;

    /** The cou iso. */
    protected String COU_ISO;

    /** The des active. */
    protected boolean DES_ACTIVE;

    /** The des backup. */
    protected boolean DES_BACKUP;

    /** The des comment. */
    protected String DES_COMMENT;

    /** The des if target exist. */
    protected int DES_IF_TARGET_EXIST;

    /** The des keep in spool. */
    protected int DES_KEEP_IN_SPOOL;

    /** The des mail on end. */
    protected boolean DES_MAIL_ON_END;

    /** The des mail on error. */
    protected boolean DES_MAIL_ON_ERROR;

    /** The des mail on start. */
    protected boolean DES_MAIL_ON_START;

    /** The des max connections. */
    protected int DES_MAX_CONNECTIONS;

    /** The des max pending. */
    protected int DES_MAX_PENDING;

    /** The des max file size. */
    protected long DES_MAX_FILE_SIZE;

    /** The des max requeue. */
    protected int DES_MAX_REQUEUE;

    /** The des max start. */
    protected int DES_MAX_START;

    /** The des monitor. */
    protected boolean DES_MONITOR;

    /** The des force proxy. */
    protected boolean DES_FORCE_PROXY;

    /** The des name. */
    protected String DES_NAME;

    /** The des data. */
    protected String DES_DATA;

    /** The des on host failure. */
    protected int DES_ON_HOST_FAILURE;

    /** The des reset frequency. */
    protected long DES_RESET_FREQUENCY;

    /** The des retry count. */
    protected int DES_RETRY_COUNT;

    /** The des retry frequency. */
    protected int DES_RETRY_FREQUENCY;

    /** The des start frequency. */
    protected int DES_START_FREQUENCY;

    /** The des stop if dirty. */
    protected boolean DES_STOP_IF_DIRTY;

    /** The des acquisition. */
    protected boolean DES_ACQUISITION;

    /** The des max inactivity. */
    protected int DES_MAX_INACTIVITY;

    /** The des transfer rate. */
    protected long DES_TRANSFER_RATE;

    /** The des update. */
    protected BigDecimal DES_UPDATE;

    /** The des filter name. */
    protected String DES_FILTER_NAME;

    /** The des user mail. */
    protected String DES_USER_MAIL;

    /** The des user status. */
    protected String DES_USER_STATUS;

    /** The des type. */
    protected int DES_TYPE;

    /** The des groupby date. */
    protected boolean DES_GROUPBY_DATE;

    /** The des date format. */
    protected String DES_DATE_FORMAT;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The trg name. */
    protected String TRG_NAME;

    /** The scv id. */
    protected Integer SCV_ID;

    /** The sta code. */
    protected String STA_CODE;

    /** The hos name for source. */
    protected Integer HOS_NAME_FOR_SOURCE;

    /** The host for source. */
    protected Host hostForSource;

    /** The transfer group. */
    protected TransferGroup transferGroup;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The country. */
    protected Country country;

    /** The scheduler value. */
    protected SchedulerValue schedulerValue;

    /**
     * Instantiates a new destination.
     */
    public Destination() {
    }

    /**
     * Instantiates a new destination.
     *
     * @param name
     *            the name
     */
    public Destination(final String name) {
        setName(name);
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return DES_DATA;
    }

    /**
     * Sets the data.
     *
     * @param param
     *            the new data
     */
    public void setData(final String param) {
        DES_DATA = param;
    }

    /**
     * Gets the country iso.
     *
     * @return the country iso
     */
    public String getCountryIso() {
        return COU_ISO;
    }

    /**
     * Sets the country iso.
     *
     * @param param
     *            the new country iso
     */
    public void setCountryIso(final String param) {
        COU_ISO = param;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return DES_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        DES_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        DES_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    public boolean getBackup() {
        return DES_BACKUP;
    }

    /**
     * Sets the backup.
     *
     * @param param
     *            the new backup
     */
    public void setBackup(final boolean param) {
        DES_BACKUP = param;
    }

    /**
     * Sets the backup.
     *
     * @param param
     *            the new backup
     */
    public void setBackup(final String param) {
        DES_BACKUP = Boolean.parseBoolean(param);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return DES_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        DES_COMMENT = param;
    }

    /**
     * Gets the if target exist.
     *
     * @return the if target exist
     */
    public int getIfTargetExist() {
        return DES_IF_TARGET_EXIST;
    }

    /**
     * Sets the if target exist.
     *
     * @param param
     *            the new if target exist
     */
    public void setIfTargetExist(final int param) {
        DES_IF_TARGET_EXIST = param;
    }

    /**
     * Sets the if target exist.
     *
     * @param param
     *            the new if target exist
     */
    public void setIfTargetExist(final String param) {
        DES_IF_TARGET_EXIST = Integer.parseInt(param);
    }

    /**
     * Gets the keep in spool.
     *
     * @return the keep in spool
     */
    public int getKeepInSpool() {
        return DES_KEEP_IN_SPOOL;
    }

    /**
     * Sets the keep in spool.
     *
     * @param param
     *            the new keep in spool
     */
    public void setKeepInSpool(final int param) {
        DES_KEEP_IN_SPOOL = param;
    }

    /**
     * Sets the keep in spool.
     *
     * @param param
     *            the new keep in spool
     */
    public void setKeepInSpool(final String param) {
        DES_KEEP_IN_SPOOL = Integer.parseInt(param);
    }

    /**
     * Gets the mail on end.
     *
     * @return the mail on end
     */
    public boolean getMailOnEnd() {
        return DES_MAIL_ON_END;
    }

    /**
     * Sets the mail on end.
     *
     * @param param
     *            the new mail on end
     */
    public void setMailOnEnd(final boolean param) {
        DES_MAIL_ON_END = param;
    }

    /**
     * Sets the mail on end.
     *
     * @param param
     *            the new mail on end
     */
    public void setMailOnEnd(final String param) {
        DES_MAIL_ON_END = Boolean.parseBoolean(param);
    }

    /**
     * Gets the mail on error.
     *
     * @return the mail on error
     */
    public boolean getMailOnError() {
        return DES_MAIL_ON_ERROR;
    }

    /**
     * Sets the mail on error.
     *
     * @param param
     *            the new mail on error
     */
    public void setMailOnError(final boolean param) {
        DES_MAIL_ON_ERROR = param;
    }

    /**
     * Sets the mail on error.
     *
     * @param param
     *            the new mail on error
     */
    public void setMailOnError(final String param) {
        DES_MAIL_ON_ERROR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the mail on start.
     *
     * @return the mail on start
     */
    public boolean getMailOnStart() {
        return DES_MAIL_ON_START;
    }

    /**
     * Sets the mail on start.
     *
     * @param param
     *            the new mail on start
     */
    public void setMailOnStart(final boolean param) {
        DES_MAIL_ON_START = param;
    }

    /**
     * Sets the mail on start.
     *
     * @param param
     *            the new mail on start
     */
    public void setMailOnStart(final String param) {
        DES_MAIL_ON_START = Boolean.parseBoolean(param);
    }

    /**
     * Gets the max connections.
     *
     * @return the max connections
     */
    public int getMaxConnections() {
        return DES_MAX_CONNECTIONS;
    }

    /**
     * Sets the max connections.
     *
     * @param param
     *            the new max connections
     */
    public void setMaxConnections(final int param) {
        DES_MAX_CONNECTIONS = param;
    }

    /**
     * Sets the max connections.
     *
     * @param param
     *            the new max connections
     */
    public void setMaxConnections(final String param) {
        DES_MAX_CONNECTIONS = Integer.parseInt(param);
    }

    /**
     * Gets the max pending.
     *
     * @return the max pending
     */
    public int getMaxPending() {
        return DES_MAX_PENDING;
    }

    /**
     * Sets the max pending.
     *
     * @param param
     *            the new max pending
     */
    public void setMaxPending(final int param) {
        DES_MAX_PENDING = param;
    }

    /**
     * Sets the max pending.
     *
     * @param param
     *            the new max pending
     */
    public void setMaxPending(final String param) {
        DES_MAX_PENDING = Integer.parseInt(param);
    }

    /**
     * Gets the max requeue.
     *
     * @return the max requeue
     */
    public int getMaxRequeue() {
        return DES_MAX_REQUEUE;
    }

    /**
     * Sets the max requeue.
     *
     * @param param
     *            the new max requeue
     */
    public void setMaxRequeue(final int param) {
        DES_MAX_REQUEUE = param;
    }

    /**
     * Sets the max requeue.
     *
     * @param param
     *            the new max requeue
     */
    public void setMaxRequeue(final String param) {
        DES_MAX_REQUEUE = Integer.parseInt(param);
    }

    /**
     * Gets the max start.
     *
     * @return the max start
     */
    public int getMaxStart() {
        return DES_MAX_START;
    }

    /**
     * Sets the max start.
     *
     * @param param
     *            the new max start
     */
    public void setMaxStart(final int param) {
        DES_MAX_START = param;
    }

    /**
     * Sets the max start.
     *
     * @param param
     *            the new max start
     */
    public void setMaxStart(final String param) {
        DES_MAX_START = Integer.parseInt(param);
    }

    /**
     * Gets the monitor.
     *
     * @return the monitor
     */
    public boolean getMonitor() {
        return DES_MONITOR;
    }

    /**
     * Sets the monitor.
     *
     * @param param
     *            the new monitor
     */
    public void setMonitor(final boolean param) {
        DES_MONITOR = param;
    }

    /**
     * Sets the monitor.
     *
     * @param param
     *            the new monitor
     */
    public void setMonitor(final String param) {
        DES_MONITOR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the force proxy.
     *
     * @return the force proxy
     */
    public boolean getForceProxy() {
        return DES_FORCE_PROXY;
    }

    /**
     * Sets the force proxy.
     *
     * @param param
     *            the new force proxy
     */
    public void setForceProxy(final boolean param) {
        DES_FORCE_PROXY = param;
    }

    /**
     * Sets the force proxy.
     *
     * @param param
     *            the new use proxy
     */
    public void setForceProxy(final String param) {
        DES_FORCE_PROXY = Boolean.parseBoolean(param);
    }

    /**
     * Gets the group by date.
     *
     * @return the group by date
     */
    public boolean getGroupByDate() {
        return DES_GROUPBY_DATE;
    }

    /**
     * Sets the group by date.
     *
     * @param param
     *            the new group by date
     */
    public void setGroupByDate(final boolean param) {
        DES_GROUPBY_DATE = param;
    }

    /**
     * Sets the group by date.
     *
     * @param param
     *            the new group by date
     */
    public void setGroupByDate(final String param) {
        DES_GROUPBY_DATE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return DES_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        DES_NAME = param;
    }

    /**
     * Gets the on host failure.
     *
     * @return the on host failure
     */
    public int getOnHostFailure() {
        return DES_ON_HOST_FAILURE;
    }

    /**
     * Sets the on host failure.
     *
     * @param param
     *            the new on host failure
     */
    public void setOnHostFailure(final int param) {
        DES_ON_HOST_FAILURE = param;
    }

    /**
     * Sets the on host failure.
     *
     * @param param
     *            the new on host failure
     */
    public void setOnHostFailure(final String param) {
        DES_ON_HOST_FAILURE = Integer.parseInt(param);
    }

    /**
     * Gets the reset frequency.
     *
     * @return the reset frequency
     */
    public long getResetFrequency() {
        return DES_RESET_FREQUENCY;
    }

    /**
     * Sets the reset frequency.
     *
     * @param param
     *            the new reset frequency
     */
    public void setResetFrequency(final long param) {
        DES_RESET_FREQUENCY = param;
    }

    /**
     * Sets the reset frequency.
     *
     * @param param
     *            the new reset frequency
     */
    public void setResetFrequency(final String param) {
        DES_RESET_FREQUENCY = Long.parseLong(param);
    }

    /**
     * Gets the max file size.
     *
     * @return the max file size
     */
    public long getMaxFileSize() {
        return DES_MAX_FILE_SIZE;
    }

    /**
     * Sets the max file size.
     *
     * @param param
     *            the new max file size
     */
    public void setMaxFileSize(final long param) {
        DES_MAX_FILE_SIZE = param;
    }

    /**
     * Sets the max file size.
     *
     * @param param
     *            the new max file size
     */
    public void setMaxFileSize(final String param) {
        DES_MAX_FILE_SIZE = Long.parseLong(param);
    }

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    public int getRetryCount() {
        return DES_RETRY_COUNT;
    }

    /**
     * Sets the retry count.
     *
     * @param param
     *            the new retry count
     */
    public void setRetryCount(final int param) {
        DES_RETRY_COUNT = param;
    }

    /**
     * Sets the retry count.
     *
     * @param param
     *            the new retry count
     */
    public void setRetryCount(final String param) {
        DES_RETRY_COUNT = Integer.parseInt(param);
    }

    /**
     * Gets the retry frequency.
     *
     * @return the retry frequency
     */
    public int getRetryFrequency() {
        return DES_RETRY_FREQUENCY;
    }

    /**
     * Sets the retry frequency.
     *
     * @param param
     *            the new retry frequency
     */
    public void setRetryFrequency(final int param) {
        DES_RETRY_FREQUENCY = param;
    }

    /**
     * Sets the retry frequency.
     *
     * @param param
     *            the new retry frequency
     */
    public void setRetryFrequency(final String param) {
        DES_RETRY_FREQUENCY = Integer.parseInt(param);
    }

    /**
     * Gets the start frequency.
     *
     * @return the start frequency
     */
    public int getStartFrequency() {
        return DES_START_FREQUENCY;
    }

    /**
     * Sets the start frequency.
     *
     * @param param
     *            the new start frequency
     */
    public void setStartFrequency(final int param) {
        DES_START_FREQUENCY = param;
    }

    /**
     * Sets the start frequency.
     *
     * @param param
     *            the new start frequency
     */
    public void setStartFrequency(final String param) {
        DES_START_FREQUENCY = Integer.parseInt(param);
    }

    /**
     * Gets the max inactivity.
     *
     * @return the max inactivity
     */
    public int getMaxInactivity() {
        return DES_MAX_INACTIVITY;
    }

    /**
     * Sets the max inactivity.
     *
     * @param param
     *            the new max inactivity
     */
    public void setMaxInactivity(final int param) {
        DES_MAX_INACTIVITY = param;
    }

    /**
     * Sets the max inactivity.
     *
     * @param param
     *            the new max inactivity
     */
    public void setMaxInactivity(final String param) {
        DES_MAX_INACTIVITY = Integer.parseInt(param);
    }

    /**
     * Gets the stop if dirty.
     *
     * @return the stop if dirty
     */
    public boolean getStopIfDirty() {
        return DES_STOP_IF_DIRTY;
    }

    /**
     * Sets the stop if dirty.
     *
     * @param param
     *            the new stop if dirty
     */
    public void setStopIfDirty(final boolean param) {
        DES_STOP_IF_DIRTY = param;
    }

    /**
     * Gets the acquisition.
     *
     * @return the acquisition
     */
    public boolean getAcquisition() {
        return DES_ACQUISITION;
    }

    /**
     * Sets the acquisition.
     *
     * @param param
     *            the new acquisition
     */
    public void setAcquisition(final boolean param) {
        DES_ACQUISITION = param;
    }

    /**
     * Sets the stop if dirty.
     *
     * @param param
     *            the new stop if dirty
     */
    public void setStopIfDirty(final String param) {
        DES_STOP_IF_DIRTY = Boolean.parseBoolean(param);
    }

    /**
     * Gets the transfer rate.
     *
     * @return the transfer rate
     */
    public long getTransferRate() {
        return DES_TRANSFER_RATE;
    }

    /**
     * Sets the transfer rate.
     *
     * @param param
     *            the new transfer rate
     */
    public void setTransferRate(final long param) {
        DES_TRANSFER_RATE = param;
    }

    /**
     * Sets the transfer rate.
     *
     * @param param
     *            the new transfer rate
     */
    public void setTransferRate(final String param) {
        DES_TRANSFER_RATE = Long.parseLong(param);
    }

    /**
     * Gets the update.
     *
     * @return the update
     */
    public java.sql.Timestamp getUpdate() {
        return bigDecimalToTimestamp(DES_UPDATE);
    }

    /**
     * Sets the update.
     *
     * @param param
     *            the new update
     */
    public void setUpdate(final java.sql.Timestamp param) {
        DES_UPDATE = timestampToBigDecimal(param);
    }

    /**
     * Gets the user mail.
     *
     * @return the user mail
     */
    public String getUserMail() {
        return DES_USER_MAIL;
    }

    /**
     * Sets the user mail.
     *
     * @param param
     *            the new user mail
     */
    public void setUserMail(final String param) {
        DES_USER_MAIL = param;
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    public String getFilterName() {
        return DES_FILTER_NAME;
    }

    /**
     * Sets the filter name.
     *
     * @param param
     *            the new filter name
     */
    public void setFilterName(final String param) {
        DES_FILTER_NAME = param;
    }

    /**
     * Gets the user status.
     *
     * @return the user status
     */
    public String getUserStatus() {
        return DES_USER_STATUS;
    }

    /**
     * Sets the user status.
     *
     * @param param
     *            the new user status
     */
    public void setUserStatus(final String param) {
        DES_USER_STATUS = param;
    }

    /**
     * Gets the date format.
     *
     * @return the date format
     */
    public String getDateFormat() {
        return DES_DATE_FORMAT;
    }

    /**
     * Sets the date format.
     *
     * @param param
     *            the new date format
     */
    public void setDateFormat(final String param) {
        DES_DATE_FORMAT = param;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public int getType() {
        return DES_TYPE;
    }

    /**
     * Sets the type.
     *
     * @param param
     *            the new type
     */
    public void setType(final String param) {
        DES_TYPE = Integer.parseInt(param);
    }

    /**
     * Sets the type.
     *
     * @param param
     *            the new type
     */
    public void setType(final int param) {
        DES_TYPE = param;
    }

    /**
     * Gets the EC user name.
     *
     * @return the EC user name
     */
    public String getECUserName() {
        return ECU_NAME;
    }

    /**
     * Sets the EC user name.
     *
     * @param param
     *            the new EC user name
     */
    public void setECUserName(final String param) {
        ECU_NAME = param;
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
     * Gets the scheduler value id.
     *
     * @return the scheduler value id
     */
    public Integer getSchedulerValueId() {
        return SCV_ID;
    }

    /**
     * Sets the scheduler value id.
     *
     * @param param
     *            the new scheduler value id
     */
    public void setSchedulerValueId(final Integer param) {
        SCV_ID = param;
    }

    /**
     * Sets the scheduler value id.
     *
     * @param param
     *            the new scheduler value id
     */
    public void setSchedulerValueId(final int param) {
        SCV_ID = param;
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public String getStatusCode() {
        return STA_CODE;
    }

    /**
     * Sets the status code.
     *
     * @param param
     *            the new status code
     */
    public void setStatusCode(final String param) {
        STA_CODE = param;
    }

    /**
     * Gets the host for source name.
     *
     * @return the host for source name
     */
    public String getHostForSourceName() {
        return integerToString(HOS_NAME_FOR_SOURCE);
    }

    /**
     * Sets the host for source name.
     *
     * @param param
     *            the new host for source name
     */
    public void setHostForSourceName(final String param) {
        HOS_NAME_FOR_SOURCE = stringToInteger(param);
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
     * Gets the EC user.
     *
     * @return the EC user
     */
    public ECUser getECUser() {
        return ecuser;
    }

    /**
     * Sets the EC user.
     *
     * @param param
     *            the new EC user
     */
    public void setECUser(final ECUser param) {
        ecuser = param;
    }

    /**
     * Gets the country.
     *
     * @return the country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Sets the country.
     *
     * @param param
     *            the new country
     */
    public void setCountry(final Country param) {
        country = param;
    }

    /**
     * Gets the scheduler value.
     *
     * @return the scheduler value
     */
    public SchedulerValue getSchedulerValue() {
        return schedulerValue;
    }

    /**
     * Sets the scheduler value.
     *
     * @param param
     *            the new scheduler value
     */
    public void setSchedulerValue(final SchedulerValue param) {
        schedulerValue = param;
    }

    /**
     * Gets the host for source.
     *
     * @return the host for source
     */
    public Host getHostForSource() {
        if (hostForSource != null) {
            hostForSource.setUseSourcePath(true);
        }
        return hostForSource;
    }

    /**
     * Sets the host for source.
     *
     * @param param
     *            the new host for source
     */
    public void setHostForSource(final Host param) {
        hostForSource = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(DES_NAME);
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
        final var other = (Destination) obj;
        return Objects.equals(DES_NAME, other.DES_NAME);
    }
}
