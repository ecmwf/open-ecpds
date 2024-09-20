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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECTRANS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import ecmwf.common.callback.RemoteInputStream;
import ecmwf.common.database.DataBaseObject;
import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferGroupHome;
import ecmwf.ecpds.master.plugin.http.home.ecuser.EcUserHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.EcTransModuleHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferMethodHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUserException;
import ecmwf.ecpds.master.plugin.http.model.transfer.ChangeLog;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMethod;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.StringPair;

/**
 * The Class HostBean.
 */
public class HostBean extends ModelBeanBase implements Host, OjbImplementedBean {

    /** The host. */
    private ecmwf.common.database.Host host;

    /** The increased priorities. */
    private final Collection<Destination> increasedPriorities = new ArrayList<>();

    /** The decreased priorities. */
    private final Collection<Destination> decreasedPriorities = new ArrayList<>();

    /** The added ec users. */
    private final Collection<EcUser> addedEcUsers = new ArrayList<>();

    /** The deleted ec users. */
    private final Collection<EcUser> deletedEcUsers = new ArrayList<>();

    /** The setup. */
    private final ECtransSetup setup;

    /** The step. */
    private int step = 0;

    /** The user. */
    private User user;

    /**
     * Instantiates a new host bean.
     *
     * @param user
     *            the user
     * @param host
     *            the host
     */
    public HostBean(final User user, final ecmwf.common.database.Host host) {
        this.setId(host.getName());
        this.user = user;
        this.host = host;
        this.setup = HOST_ECTRANS.getECtransSetup(host.getData());
        // Remove all options which are not visible
        ECtransOptions.get(ECtransGroups.HOST).stream().filter(option -> !option.isVisible()).forEach(setup::remove);
    }

    /**
     * Instantiates a new host bean.
     *
     * @param host
     *            the host
     */
    public HostBean(final ecmwf.common.database.Host host) {
        this(null, host);
    }

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return Host.class.getName();
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return host;
    }

    /**
     * Sets the ojb implementation.
     *
     * @param object
     *            the new ojb implementation
     */
    public void setOjbImplementation(final Object object) {
        host = (ecmwf.common.database.Host) object;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return host.getName();
    }

    /**
     * Gets the collection size.
     *
     * @return the collection size
     */
    @Override
    public int getCollectionSize() {
        return host.getCollectionSize();
    }

    /**
     * Gets the check time.
     *
     * @return the check time
     */
    @Override
    public Date getCheckTime() {
        return host.getHostStats().getCheckTime();
    }

    /**
     * Gets the acquisition time.
     *
     * @return the acquisition time
     */
    public Date getAcquisitionTime() {
        return host.getHostOutput().getAcquisitionTime();
    }

    /**
     * Gets the check frequency.
     *
     * @return the check frequency
     */
    @Override
    public long getCheckFrequency() {
        return host.getCheckFrequency();
    }

    /**
     * Gets the formatted check frequency.
     *
     * @return the formatted check frequency
     */
    @Override
    public String getFormattedCheckFrequency() {
        return Format.formatDuration(getCheckFrequency());
    }

    /**
     * Gets the acquisition frequency.
     *
     * @return the acquisition frequency
     */
    @Override
    public long getAcquisitionFrequency() {
        return host.getAcquisitionFrequency();
    }

    /**
     * Gets the formatted acquisition frequency.
     *
     * @return the formatted acquisition frequency
     */
    @Override
    public String getFormattedAcquisitionFrequency() {
        return Format.formatDuration(getAcquisitionFrequency());
    }

    /**
     * Gets the valid.
     *
     * @return the valid
     */
    @Override
    public boolean getValid() {
        return this.host.getHostStats().getValid();
    }

    /**
     * Gets the check.
     *
     * @return the check
     */
    @Override
    public boolean getCheck() {
        return host.getCheck();
    }

    /**
     * Gets the check filename.
     *
     * @return the check filename
     */
    @Override
    public String getCheckFilename() {
        return host.getCheckFilename();
    }

    /**
     * Gets the mail on success.
     *
     * @return the mail on success
     */
    @Override
    public boolean getMailOnSuccess() {
        return host.getMailOnSuccess();
    }

    /**
     * Gets the mail on error.
     *
     * @return the mail on error
     */
    @Override
    public boolean getMailOnError() {
        return host.getMailOnError();
    }

    /**
     * Gets the notify once.
     *
     * @return the notify once
     */
    @Override
    public boolean getNotifyOnce() {
        return host.getNotifyOnce();
    }

    /**
     * Gets the user mail.
     *
     * @return the user mail
     */
    @Override
    public String getUserMail() {
        return host.getUserMail();
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    @Override
    public boolean getActive() {
        return host.getActive();
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    @Override
    public String getComment() {
        return host.getComment();
    }

    /**
     * Gets the connections.
     *
     * @return the connections
     */
    @Override
    public int getConnections() {
        return this.host.getHostStats().getConnections();
    }

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    @Override
    public String getProperties() {
        return setup.getProperties(true);
    }

    /**
     * Gets the javascript.
     *
     * @return the javascript
     */
    @Override
    public String getJavascript() {
        return setup.getScript();
    }

    /**
     * Gets the data. This is the method which is called from the JSP to get the content of the setup.
     *
     * @return the data
     */
    @Override
    public String getData() {
        return setup.getData();
    }

    /**
     * Gets the last output.
     *
     * @return the last output
     */
    @Override
    public String getLastOutput() {
        final var hostOutput = this.host.getHostOutput();
        if (hostOutput == null) {
            return "[n/a]";
        }
        final var output = hostOutput.getOutput();
        try {
            return Format.uncompress(output);
        } catch (final Exception e) {
            return output;
        }
    }

    /**
     * Gets the formatted last output.
     *
     * @return the formatted last output
     */
    @Override
    public String getFormattedLastOutput() {
        final var lastOutput = getLastOutput();
        return lastOutput == null || lastOutput.trim().length() == 0 ? "[n/a]" : Util.getFormatted(user, lastOutput);
    }

    /**
     * Gets the ec user.
     *
     * @return the ec user
     *
     * @throws EcUserException
     *             the ec user exception
     */
    @Override
    public EcUser getEcUser() throws EcUserException {
        return EcUserHome.findByPrimaryKey(getECUserName());
    }

    /**
     * Gets the EC user name.
     *
     * @return the EC user name
     */
    @Override
    public String getECUserName() {
        return host.getECUserName();
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    @Override
    public String getHost() {
        return host != null ? host.getHost() : "None";
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    @Override
    public String getLogin() {
        return host.getLogin();
    }

    /**
     * Gets the max connections.
     *
     * @return the max connections
     */
    @Override
    public int getMaxConnections() {
        return host.getMaxConnections();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return host != null ? host.getName() : "Unassigned";
    }

    /**
     * Gets the passwd.
     *
     * @return the passwd
     */
    @Override
    public String getPasswd() {
        return host.getPasswd();
    }

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    @Override
    public int getRetryCount() {
        return host.getRetryCount();
    }

    /**
     * Gets the retry frequency.
     *
     * @return the retry frequency
     */
    @Override
    public int getRetryFrequency() {
        return host.getRetryFrequency();
    }

    /**
     * Gets the formatted retry frequency.
     *
     * @return the formatted retry frequency
     */
    @Override
    public String getFormattedRetryFrequency() {
        return Format.formatDuration(getRetryFrequency());
    }

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     *
     * @throws DataFileException
     *             the data file exception
     */
    @Override
    public TransferGroup getTransferGroup() throws DataFileException {
        return TransferGroupHome.findByPrimaryKey(getTransferGroupName());
    }

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    @Override
    public String getTransferGroupName() {
        return host.getTransferGroupName();
    }

    /**
     * Gets the transfer method.
     *
     * @return the transfer method
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public TransferMethod getTransferMethod() throws TransferException {
        return TransferMethodHome.findByPrimaryKey(getTransferMethodName());
    }

    /**
     * Gets the transfer method name.
     *
     * @return the transfer method name
     */
    @Override
    public String getTransferMethodName() {
        return host.getTransferMethodName();
    }

    /**
     * Gets the data transfers today.
     *
     * @return the data transfers today
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<DataTransferLightBean> getDataTransfersToday() throws TransferException {
        return DataTransferHome.findByHostAndDate(this, new Date());
    }

    /**
     * Gets the allowed users.
     *
     * @return the allowed users
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<EcUser> getAllowedUsers() throws TransferException {
        try {
            return EcUserHome.findByHost(this);
        } catch (final EcUserException e) {
            throw new TransferException("Problem getting allowed users for host '" + getName() + "'", e);
        }
    }

    /**
     * Sets the check frequency.
     *
     * @param i
     *            the new check frequency
     */
    @Override
    public void setCheckFrequency(final long i) {
        host.setCheckFrequency(i);
    }

    /**
     * Sets the acquisition frequency.
     *
     * @param i
     *            the new acquisition frequency
     */
    @Override
    public void setAcquisitionFrequency(final long i) {
        host.setAcquisitionFrequency(i);
    }

    /**
     * Sets the check.
     *
     * @param b
     *            the new check
     */
    @Override
    public void setCheck(final boolean b) {
        host.setCheck(b);
    }

    /**
     * Sets the check filename.
     *
     * @param s
     *            the new check filename
     */
    @Override
    public void setCheckFilename(final String s) {
        host.setCheckFilename(s);
    }

    /**
     * Sets the mail on success.
     *
     * @param b
     *            the new mail on success
     */
    @Override
    public void setMailOnSuccess(final boolean b) {
        host.setMailOnSuccess(b);
    }

    /**
     * Sets the mail on error.
     *
     * @param b
     *            the new mail on error
     */
    @Override
    public void setMailOnError(final boolean b) {
        host.setMailOnError(b);
    }

    /**
     * Sets the notify once.
     *
     * @param b
     *            the new notify once
     */
    @Override
    public void setNotifyOnce(final boolean b) {
        host.setNotifyOnce(b);
    }

    /**
     * Sets the user mail.
     *
     * @param s
     *            the new user mail
     */
    @Override
    public void setUserMail(final String s) {
        host.setUserMail(s);
    }

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    @Override
    public void setActive(final boolean b) {
        host.setActive(b);
    }

    /**
     * Sets the comment.
     *
     * @param s
     *            the new comment
     */
    @Override
    public void setComment(final String s) {
        host.setComment(s);
    }

    /**
     * Sets the data.
     *
     * @param s
     *            the new data
     */
    @Override
    public void setData(final String s) {
        host.setData(s);
    }

    /**
     * Sets the EC user name.
     *
     * @param u
     *            the new EC user name
     */
    @Override
    public void setECUserName(final String u) {
        host.setECUserName(u);
    }

    /**
     * Sets the host.
     *
     * @param s
     *            the new host
     */
    @Override
    public void setHost(final String s) {
        host.setHost(s);
    }

    /**
     * Sets the login.
     *
     * @param s
     *            the new login
     */
    @Override
    public void setLogin(final String s) {
        host.setLogin(s);
    }

    /**
     * Sets the max connections.
     *
     * @param i
     *            the new max connections
     */
    @Override
    public void setMaxConnections(final int i) {
        host.setMaxConnections(i);
    }

    /**
     * Sets the name.
     *
     * @param s
     *            the new name
     */
    @Override
    public void setName(final String s) {
        host.setName(s);
    }

    /**
     * Sets the passwd.
     *
     * @param s
     *            the new passwd
     */
    @Override
    public void setPasswd(final String s) {
        host.setPasswd(s);
    }

    /**
     * Sets the retry count.
     *
     * @param i
     *            the new retry count
     */
    @Override
    public void setRetryCount(final int i) {
        host.setRetryCount(i);
    }

    /**
     * Sets the retry frequency.
     *
     * @param i
     *            the new retry frequency
     */
    @Override
    public void setRetryFrequency(final int i) {
        host.setRetryFrequency(i);
    }

    /**
     * Sets the transfer method.
     *
     * @param m
     *            the new transfer method
     */
    @Override
    public void setTransferMethod(final TransferMethod m) {
    }

    /**
     * Sets the transfer method name.
     *
     * @param s
     *            the new transfer method name
     */
    @Override
    public void setTransferMethodName(final String s) {
        host.setTransferMethodName(s);
    }

    /**
     * Sets the transfer group.
     *
     * @param m
     *            the new transfer group
     */
    @Override
    public void setTransferGroup(final TransferGroup m) {
    }

    /**
     * Sets the transfer group name.
     *
     * @param s
     *            the new transfer group name
     */
    @Override
    public void setTransferGroupName(final String s) {
        host.setTransferGroupName(s);
    }

    /**
     * Increase priority within destination.
     *
     * @param d
     *            the d
     * @param step
     *            the step
     */
    @Override
    public void increasePriorityWithinDestination(final Destination d, final int step) {
        increasedPriorities.add(d);
        this.step = step;
    }

    /**
     * Decrease priority within destination.
     *
     * @param d
     *            the d
     * @param step
     *            the step
     */
    @Override
    public void decreasePriorityWithinDestination(final Destination d, final int step) {
        decreasedPriorities.add(d);
        this.step = step;
    }

    /**
     * Adds the allowed user.
     *
     * @param u
     *            the u
     */
    @Override
    public void addAllowedUser(final EcUser u) {
        addedEcUsers.add(u);
    }

    /**
     * Delete allowed user.
     *
     * @param u
     *            the u
     */
    @Override
    public void deleteAllowedUser(final EcUser u) {
        deletedEcUsers.add(u);
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
        return o instanceof final HostBean hostBean && equals(hostBean);
    }

    /**
     * Equals.
     *
     * @param u
     *            the u
     *
     * @return true, if successful
     */
    public boolean equals(final HostBean u) {
        return getName().equals(u.getName());
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
        return getClass().getName() + " { " + host + " }";
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    @Override
    public String getDir() {
        return host.getDir();
    }

    /**
     * Sets the dir.
     *
     * @param s
     *            the new dir
     */
    @Override
    public void setDir(final String s) {
        host.setDir(s);
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    @Override
    public long getDuration() {
        final var hostStats = host.getHostStats();
        final var duration = hostStats.getDuration();
        final var sent = hostStats.getSent();
        return sent > Cnf.at("HostBean", "minSent", sent) ? duration : Cnf.at("HostBean", "duration", duration);
    }

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     */
    @Override
    public String getFormattedDuration() {
        return Format.formatDuration(getDuration());
    }

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    @Override
    public long getSent() {
        final var curSent = host.getHostStats().getSent();
        final var minSent = Cnf.at("HostBean", "minSent", curSent);
        return curSent > minSent ? curSent : minSent;
    }

    /**
     * Gets the formatted sent.
     *
     * @return the formatted sent
     */
    @Override
    public String getFormattedSent() {
        return Format.formatSize(getSent());
    }

    /**
     * Gets the band width.
     *
     * @return the band width
     */
    @Override
    public long getBandWidth() {
        return getSent() > 0 && getDuration() > 0 ? getSent() / getDuration() : 0;
    }

    /**
     * Gets the formatted band width.
     *
     * @return the formatted band width
     */
    @Override
    public String getFormattedBandWidth() {
        return Format.formatRate(getSent(), getDuration());
    }

    /**
     * Gets the formatted band width in M bits per seconds.
     *
     * @return the formatted band width in M bits per seconds
     */
    @Override
    public double getFormattedBandWidthInMBitsPerSeconds() {
        return Format.getMBitsPerSeconds(getSent(), getDuration());
    }

    /**
     * Gets the destinations.
     *
     * @return the destinations
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<Destination> getDestinations() throws TransferException {
        return DestinationHome.findByHost(this);
    }

    /**
     * Transfer.
     *
     * @param u
     *            the u
     * @param bytes
     *            the bytes
     * @param target
     *            the target
     * @param remotePos
     *            the remote pos
     *
     * @return the long
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public long transfer(final User u, final byte[] bytes, final String target, final long remotePos)
            throws TransferException {
        try {
            return MasterManager.getMI().transfer(Util.getECpdsSessionFromObject(u), bytes, host, target, remotePos);
        } catch (final Exception e) {
            throw new TransferException("Error transfering file", e);
        }
    }

    /**
     * Gets the report.
     *
     * @param u
     *            the u
     *
     * @return the report
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getReport(final User u) throws TransferException {
        try {
            return MasterManager.getMI().getReport(Util.getECpdsSessionFromObject(u), host);
        } catch (final Exception e) {
            throw new TransferException("Error getting report", e);
        }
    }

    /**
     * Gets the report.
     *
     * @param u
     *            the u
     * @param proxy
     *            the proxy
     *
     * @return the report
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getReport(final User u, final Host proxy) throws TransferException {
        try {
            return MasterManager.getMI().getHostReport(Util.getECpdsSessionFromObject(u),
                    (ecmwf.common.database.Host) proxy.getOjbImplementation(), host);
        } catch (final Exception e) {
            throw new TransferException("Error getting report", e);
        }
    }

    /**
     * Gets the output.
     *
     * @param u
     *            the u
     *
     * @return the output
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public RemoteInputStream getOutput(final User u) throws TransferException {
        try {
            return MasterManager.getMI().getOutput(Util.getECpdsSessionFromObject(u), host);
        } catch (final Exception e) {
            throw new TransferException("Error getting output", e);
        }
    }

    /**
     * Reset transfer statistics.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void resetTransferStatistics(final User u) throws TransferException {
        try {
            MasterManager.getMI().resetTransferStatistics(Util.getECpdsSessionFromObject(u), host);
        } catch (final Exception e) {
            throw new TransferException("Problem resetting statistics for host '" + this.getName() + "'", e);
        }
    }

    /**
     * Clean data window.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public void cleanDataWindow(final User u) throws TransferException {
        try {
            MasterManager.getMI().cleanDataWindow(Util.getECpdsSessionFromObject(u), host);
        } catch (final Exception e) {
            throw new TransferException("Error cleanning data window", e);
        }
    }

    /**
     * Gets the change log list.
     *
     * @return the change log list
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<ChangeLog> getChangeLogList() throws TransferException {
        return HostHome.findChangeLogList(this);
    }

    /**
     * Gets the automatic location.
     *
     * @return the automatic location
     */
    @Override
    public boolean getAutomaticLocation() {
        return host.getAutomaticLocation();
    }

    /**
     * Sets the automatic location.
     *
     * @param b
     *            the new automatic location
     */
    @Override
    public void setAutomaticLocation(final boolean b) {
        host.setAutomaticLocation(b);
    }

    /**
     * Gets the latitude.
     *
     * @return the latitude
     */
    @Override
    public Double getLatitude() {
        return host.getHostLocation().getLatitude();
    }

    /**
     * Sets the latitude.
     *
     * @param f
     *            the new latitude
     */
    @Override
    public void setLatitude(final Double f) {
        host.getHostLocation().setLatitude(f);
    }

    /**
     * Gets the longitude.
     *
     * @return the longitude
     */
    @Override
    public Double getLongitude() {
        return host.getHostLocation().getLongitude();
    }

    /**
     * Sets the longitude.
     *
     * @param f
     *            the new longitude
     */
    @Override
    public void setLongitude(final Double f) {
        host.getHostLocation().setLongitude(f);
    }

    /**
     * Gets the nick name.
     *
     * @return the nick name
     */
    @Override
    public String getNickName() {
        return host.getNickname();
    }

    /**
     * Sets the nick name.
     *
     * @param s
     *            the new nick name
     */
    @Override
    public void setNickName(final String s) {
        host.setNickname(s);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @Override
    public String getType() {
        return host.getType();
    }

    /**
     * Sets the type.
     *
     * @param s
     *            the new type
     */
    @Override
    public void setType(final String s) {
        host.setType(s);
    }

    /**
     * Sets the acquisition frequency.
     *
     * @param s
     *            the new acquisition frequency
     */
    public void setAcquisitionFrequency(final String s) {
        host.setAcquisitionFrequency(s);
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    @Override
    public String getFilterName() {
        return host.getFilterName();
    }

    /**
     * Sets the filter name.
     *
     * @param s
     *            the new filter name
     */
    @Override
    public void setFilterName(final String s) {
        host.setFilterName(s);
    }

    /**
     * Gets the network name.
     *
     * @return the network name
     */
    @Override
    public String getNetworkName() {
        return host.getNetworkName();
    }

    /**
     * Gets the all networks.
     *
     * @return the all networks
     */
    @Override
    public List<StringPair> getAllNetworks() {
        final List<StringPair> pairs = new ArrayList<>();
        for (var i = 0; i < HostOption.networkCode.length; i++) {
            pairs.add(new StringPair(HostOption.networkCode[i],
                    HostOption.networkName.length > i ? HostOption.networkName[i] : HostOption.networkCode[i]));
        }
        return pairs;
    }

    /**
     * Gets the transfer module names.
     *
     * @return the transfer module names
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public String getTransferModuleNames() throws TransferException {
        return EcTransModuleHome.findAll().stream().map(module -> "\"" + module.getName() + "\"")
                .collect(Collectors.joining(", "));
    }

    /**
     * Sets the network name.
     *
     * @param s
     *            the new network name
     */
    @Override
    public void setNetworkName(final String s) {
        host.setNetworkName(s);
    }

    /**
     * Gets the network code.
     *
     * @return the network code
     */
    @Override
    public String getNetworkCode() {
        return host.getNetworkCode();
    }

    /**
     * Sets the network code.
     *
     * @param s
     *            the new network code
     */
    @Override
    public void setNetworkCode(final String s) {
        host.setNetworkCode(s);
    }

    /**
     * Gets the increased priorities.
     *
     * @return the increased priorities
     */
    protected Collection<Destination> getIncreasedPriorities() {
        return increasedPriorities;
    }

    /**
     * Gets the decreased priorities.
     *
     * @return the decreased priorities
     */
    protected Collection<Destination> getDecreasedPriorities() {
        return decreasedPriorities;
    }

    /**
     * Gets the priority change step.
     *
     * @return the priority change step
     */
    protected int getPriorityChangeStep() {
        return this.step;
    }

    /**
     * Gets the added ec users.
     *
     * @return the added ec users
     */
    protected Collection<EcUser> getAddedEcUsers() {
        return addedEcUsers;
    }

    /**
     * Gets the deleted ec users.
     *
     * @return the deleted ec users
     */
    protected Collection<EcUser> getDeletedEcUsers() {
        return deletedEcUsers;
    }

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    @Override
    public String getCompletions() {
        return ECtransOptions.toString(ECtransGroups.HOST);
    }
}
