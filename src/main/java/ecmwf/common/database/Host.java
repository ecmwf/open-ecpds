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

import java.util.Objects;

/**
 * The Class Host.
 */
public class Host extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6269434296900284206L;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The hos active. */
    protected boolean HOS_ACTIVE;

    /** The hos check. */
    protected boolean HOS_CHECK;

    /** The hos type. */
    protected String HOS_TYPE;

    /** The hos check filename. */
    protected String HOS_CHECK_FILENAME;

    /** The hos check frequency. */
    protected long HOS_CHECK_FREQUENCY;

    /** The hos acquisition frequency. */
    protected long HOS_ACQUISITION_FREQUENCY;

    /** The hos comment. */
    protected String HOS_COMMENT;

    /** The hos data. */
    protected String HOS_DATA;

    /** The hos dir. */
    protected String HOS_DIR;

    /** The hos host. */
    protected String HOS_HOST;

    /** The hos login. */
    protected String HOS_LOGIN;

    /** The hos mail on error. */
    protected boolean HOS_MAIL_ON_ERROR;

    /** The hos mail on success. */
    protected boolean HOS_MAIL_ON_SUCCESS;

    /** The hos max connections. */
    protected int HOS_MAX_CONNECTIONS;

    /** The hos name. */
    protected Integer HOS_NAME;

    /** The hos network code. */
    protected String HOS_NETWORK_CODE;

    /** The hos network name. */
    protected String HOS_NETWORK_NAME;

    /** The hos nickname. */
    protected String HOS_NICKNAME;

    /** The hos notify once. */
    protected boolean HOS_NOTIFY_ONCE;

    /** The hos passwd. */
    protected String HOS_PASSWD;

    /** The hos retry count. */
    protected int HOS_RETRY_COUNT;

    /** The hos retry frequency. */
    protected int HOS_RETRY_FREQUENCY;

    /** The hos user mail. */
    protected String HOS_USER_MAIL;

    /** The tme name. */
    protected String TME_NAME;

    /** The trg name. */
    protected String TRG_NAME;

    /** The hlo id. */
    protected Integer HLO_ID;

    /** The hou id. */
    protected Integer HOU_ID;

    /** The hst id. */
    protected Integer HST_ID;

    /** The hos filter name. */
    protected String HOS_FILTER_NAME;

    /** The hos automatic location. */
    protected boolean HOS_AUTOMATIC_LOCATION;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The transfer method. */
    protected TransferMethod transferMethod;

    /** The transfer group. */
    protected TransferGroup transferGroup;

    /** The host stats. */
    protected HostStats hostStats;

    /** The host location. */
    protected HostLocation hostLocation;

    /** The host output. Not populated as not defined in hibernate.hbm.xml file */
    protected HostOutput hostOutput;

    /** The use source path. Not mapped into the database */
    private boolean useSourcePath = false;

    /**
     * Instantiates a new host.
     */
    public Host() {
    }

    /**
     * Instantiates a new host.
     *
     * @param name
     *            the name
     */
    public Host(final String name) {
        setName(name);
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
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return HOS_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        HOS_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        HOS_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the check.
     *
     * @return the check
     */
    public boolean getCheck() {
        return HOS_CHECK;
    }

    /**
     * Sets the check.
     *
     * @param param
     *            the new check
     */
    public void setCheck(final boolean param) {
        HOS_CHECK = param;
    }

    /**
     * Sets the check.
     *
     * @param param
     *            the new check
     */
    public void setCheck(final String param) {
        HOS_CHECK = Boolean.parseBoolean(param);
    }

    /**
     * Gets the check filename.
     *
     * @return the check filename
     */
    public String getCheckFilename() {
        return HOS_CHECK_FILENAME;
    }

    /**
     * Sets the check filename.
     *
     * @param param
     *            the new check filename
     */
    public void setCheckFilename(final String param) {
        HOS_CHECK_FILENAME = param;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return HOS_TYPE;
    }

    /**
     * Sets the type.
     *
     * @param param
     *            the new type
     */
    public void setType(final String param) {
        HOS_TYPE = param;
    }

    /**
     * Gets the check frequency.
     *
     * @return the check frequency
     */
    public long getCheckFrequency() {
        return HOS_CHECK_FREQUENCY;
    }

    /**
     * Sets the check frequency.
     *
     * @param param
     *            the new check frequency
     */
    public void setCheckFrequency(final long param) {
        HOS_CHECK_FREQUENCY = param;
    }

    /**
     * Sets the check frequency.
     *
     * @param param
     *            the new check frequency
     */
    public void setCheckFrequency(final String param) {
        HOS_CHECK_FREQUENCY = Long.parseLong(param);
    }

    /**
     * Gets the acquisition frequency.
     *
     * @return the acquisition frequency
     */
    public long getAcquisitionFrequency() {
        return HOS_ACQUISITION_FREQUENCY;
    }

    /**
     * Sets the acquisition frequency.
     *
     * @param param
     *            the new acquisition frequency
     */
    public void setAcquisitionFrequency(final long param) {
        HOS_ACQUISITION_FREQUENCY = param;
    }

    /**
     * Sets the acquisition frequency.
     *
     * @param param
     *            the new acquisition frequency
     */
    public void setAcquisitionFrequency(final String param) {
        HOS_ACQUISITION_FREQUENCY = Long.parseLong(param);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return HOS_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        HOS_COMMENT = param;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return HOS_DATA;
    }

    /**
     * Sets the data.
     *
     * @param param
     *            the new data
     */
    public void setData(final String param) {
        HOS_DATA = param;
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    public String getDir() {
        return HOS_DIR;
    }

    /**
     * Sets the dir.
     *
     * @param param
     *            the new dir
     */
    public void setDir(final String param) {
        HOS_DIR = param;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return HOS_HOST != null ? HOS_HOST.trim() : HOS_HOST;
    }

    /**
     * Sets the host.
     *
     * @param param
     *            the new host
     */
    public void setHost(String param) {
        if (param != null) {
            param = param.trim();
        }
        HOS_HOST = param;
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    public String getLogin() {
        return HOS_LOGIN;
    }

    /**
     * Sets the login.
     *
     * @param param
     *            the new login
     */
    public void setLogin(final String param) {
        HOS_LOGIN = param;
    }

    /**
     * Gets the mail on error.
     *
     * @return the mail on error
     */
    public boolean getMailOnError() {
        return HOS_MAIL_ON_ERROR;
    }

    /**
     * Sets the mail on error.
     *
     * @param param
     *            the new mail on error
     */
    public void setMailOnError(final boolean param) {
        HOS_MAIL_ON_ERROR = param;
    }

    /**
     * Sets the mail on error.
     *
     * @param param
     *            the new mail on error
     */
    public void setMailOnError(final String param) {
        HOS_MAIL_ON_ERROR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the mail on success.
     *
     * @return the mail on success
     */
    public boolean getMailOnSuccess() {
        return HOS_MAIL_ON_SUCCESS;
    }

    /**
     * Sets the mail on success.
     *
     * @param param
     *            the new mail on success
     */
    public void setMailOnSuccess(final boolean param) {
        HOS_MAIL_ON_SUCCESS = param;
    }

    /**
     * Sets the mail on success.
     *
     * @param param
     *            the new mail on success
     */
    public void setMailOnSuccess(final String param) {
        HOS_MAIL_ON_SUCCESS = Boolean.parseBoolean(param);
    }

    /**
     * Gets the max connections.
     *
     * @return the max connections
     */
    public int getMaxConnections() {
        return HOS_MAX_CONNECTIONS;
    }

    /**
     * Sets the max connections.
     *
     * @param param
     *            the new max connections
     */
    public void setMaxConnections(final int param) {
        HOS_MAX_CONNECTIONS = param;
    }

    /**
     * Sets the max connections.
     *
     * @param param
     *            the new max connections
     */
    public void setMaxConnections(final String param) {
        HOS_MAX_CONNECTIONS = Integer.parseInt(param);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return integerToString(HOS_NAME);
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        HOS_NAME = stringToInteger(param);
    }

    /**
     * Gets the network code.
     *
     * @return the network code
     */
    public String getNetworkCode() {
        return HOS_NETWORK_CODE;
    }

    /**
     * Sets the network code.
     *
     * @param param
     *            the new network code
     */
    public void setNetworkCode(final String param) {
        HOS_NETWORK_CODE = param;
    }

    /**
     * Gets the network name.
     *
     * @return the network name
     */
    public String getNetworkName() {
        return HOS_NETWORK_NAME;
    }

    /**
     * Sets the network name.
     *
     * @param param
     *            the new network name
     */
    public void setNetworkName(final String param) {
        HOS_NETWORK_NAME = param;
    }

    /**
     * Gets the nickname.
     *
     * @return the nickname
     */
    public String getNickname() {
        return HOS_NICKNAME;
    }

    /**
     * Sets the nickname.
     *
     * @param param
     *            the new nickname
     */
    public void setNickname(final String param) {
        HOS_NICKNAME = param;
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
     * Gets the notify once.
     *
     * @return the notify once
     */
    public boolean getNotifyOnce() {
        return HOS_NOTIFY_ONCE;
    }

    /**
     * Sets the notify once.
     *
     * @param param
     *            the new notify once
     */
    public void setNotifyOnce(final boolean param) {
        HOS_NOTIFY_ONCE = param;
    }

    /**
     * Sets the notify once.
     *
     * @param param
     *            the new notify once
     */
    public void setNotifyOnce(final String param) {
        HOS_NOTIFY_ONCE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the passwd.
     *
     * @return the passwd
     */
    public String getPasswd() {
        return HOS_PASSWD;
    }

    /**
     * Sets the passwd.
     *
     * @param param
     *            the new passwd
     */
    public void setPasswd(final String param) {
        HOS_PASSWD = param;
    }

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    public int getRetryCount() {
        return HOS_RETRY_COUNT;
    }

    /**
     * Sets the retry count.
     *
     * @param param
     *            the new retry count
     */
    public void setRetryCount(final int param) {
        HOS_RETRY_COUNT = param;
    }

    /**
     * Sets the retry count.
     *
     * @param param
     *            the new retry count
     */
    public void setRetryCount(final String param) {
        HOS_RETRY_COUNT = Integer.parseInt(param);
    }

    /**
     * Gets the retry frequency.
     *
     * @return the retry frequency
     */
    public int getRetryFrequency() {
        return HOS_RETRY_FREQUENCY;
    }

    /**
     * Sets the retry frequency.
     *
     * @param param
     *            the new retry frequency
     */
    public void setRetryFrequency(final int param) {
        HOS_RETRY_FREQUENCY = param;
    }

    /**
     * Sets the retry frequency.
     *
     * @param param
     *            the new retry frequency
     */
    public void setRetryFrequency(final String param) {
        HOS_RETRY_FREQUENCY = Integer.parseInt(param);
    }

    /**
     * Gets the user mail.
     *
     * @return the user mail
     */
    public String getUserMail() {
        return HOS_USER_MAIL;
    }

    /**
     * Sets the user mail.
     *
     * @param param
     *            the new user mail
     */
    public void setUserMail(final String param) {
        HOS_USER_MAIL = param;
    }

    /**
     * Gets the transfer method name.
     *
     * @return the transfer method name
     */
    public String getTransferMethodName() {
        return TME_NAME;
    }

    /**
     * Sets the transfer method name.
     *
     * @param param
     *            the new transfer method name
     */
    public void setTransferMethodName(final String param) {
        TME_NAME = param;
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
     * Gets the transfer method.
     *
     * @return the transfer method
     */
    public TransferMethod getTransferMethod() {
        return transferMethod;
    }

    /**
     * Sets the transfer method.
     *
     * @param param
     *            the new transfer method
     */
    public void setTransferMethod(final TransferMethod param) {
        transferMethod = param;
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
     * Gets the host stats id.
     *
     * @return the host stats id
     */
    public Integer getHostStatsId() {
        return HST_ID;
    }

    /**
     * Sets the host stats id.
     *
     * @param param
     *            the new host stats id
     */
    public void setHostStatsId(final int param) {
        HST_ID = param;
    }

    /**
     * Gets the host stats.
     *
     * @return the host stats
     */
    public HostStats getHostStats() {
        return hostStats;
    }

    /**
     * Sets the host stats.
     *
     * @param param
     *            the new host stats
     */
    public void setHostStats(final HostStats param) {
        hostStats = param;
    }

    /**
     * Gets the host location id.
     *
     * @return the host location id
     */
    public Integer getHostLocationId() {
        return HLO_ID;
    }

    /**
     * Sets the host location id.
     *
     * @param param
     *            the new host location id
     */
    public void setHostLocationId(final int param) {
        HLO_ID = param;
    }

    /**
     * Gets the host location.
     *
     * @return the host location
     */
    public HostLocation getHostLocation() {
        return hostLocation;
    }

    /**
     * Sets the host location.
     *
     * @param param
     *            the new host location
     */
    public void setHostLocation(final HostLocation param) {
        hostLocation = param;
    }

    /**
     * Gets the host output id.
     *
     * @return the host output id
     */
    public Integer getHostOutputId() {
        return HOU_ID;
    }

    /**
     * Sets the host output id.
     *
     * @param param
     *            the new host output id
     */
    public void setHostOutputId(final int param) {
        HOU_ID = param;
    }

    /**
     * Gets the host output.
     *
     * @return the host location
     */
    public HostOutput getHostOutput() {
        return hostOutput;
    }

    /**
     * Sets the host output.
     *
     * @param param
     *            the new host output
     */
    public void setHostOutput(final HostOutput param) {
        hostOutput = param;
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    public String getFilterName() {
        return HOS_FILTER_NAME;
    }

    /**
     * Sets the filter name.
     *
     * @param param
     *            the new filter name
     */
    public void setFilterName(final String param) {
        HOS_FILTER_NAME = param;
    }

    /**
     * Gets the automatic location.
     *
     * @return the automatic location
     */
    public boolean getAutomaticLocation() {
        return HOS_AUTOMATIC_LOCATION;
    }

    /**
     * Sets the automatic location.
     *
     * @param param
     *            the new automatic location
     */
    public void setAutomaticLocation(final boolean param) {
        HOS_AUTOMATIC_LOCATION = param;
    }

    /**
     * Sets the automatic location.
     *
     * @param param
     *            the new automatic location
     */
    public void setAutomaticLocation(final String param) {
        HOS_AUTOMATIC_LOCATION = Boolean.parseBoolean(param);
    }

    /**
     * Gets the use source path.
     *
     * @return the use source path
     */
    public boolean getUseSourcePath() {
        return useSourcePath;
    }

    /**
     * Sets the use source path.
     *
     * @param param
     *            the new use source path
     */
    public void setUseSourcePath(final boolean param) {
        useSourcePath = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(HOS_NAME);
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
        final var other = (Host) obj;
        return Objects.equals(HOS_NAME, other.HOS_NAME);
    }
}
