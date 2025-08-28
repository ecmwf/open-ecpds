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
 * ECMWF Product Data Store (OpenECPDS) Project
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
     * {@inheritDoc}
     *
     * Sets the user.
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user.
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return Host.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
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
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return host.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the collection size.
     */
    @Override
    public int getCollectionSize() {
        return host.getCollectionSize();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the check time.
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
     * {@inheritDoc}
     *
     * Gets the check frequency.
     */
    @Override
    public long getCheckFrequency() {
        return host.getCheckFrequency();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted check frequency.
     */
    @Override
    public String getFormattedCheckFrequency() {
        return Format.formatDuration(getCheckFrequency());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the acquisition frequency.
     */
    @Override
    public long getAcquisitionFrequency() {
        return host.getAcquisitionFrequency();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted acquisition frequency.
     */
    @Override
    public String getFormattedAcquisitionFrequency() {
        return Format.formatDuration(getAcquisitionFrequency());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the valid.
     */
    @Override
    public boolean getValid() {
        return this.host.getHostStats().getValid();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the check.
     */
    @Override
    public boolean getCheck() {
        return host.getCheck();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the check filename.
     */
    @Override
    public String getCheckFilename() {
        return host.getCheckFilename();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the mail on success.
     */
    @Override
    public boolean getMailOnSuccess() {
        return host.getMailOnSuccess();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the mail on error.
     */
    @Override
    public boolean getMailOnError() {
        return host.getMailOnError();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the notify once.
     */
    @Override
    public boolean getNotifyOnce() {
        return host.getNotifyOnce();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user mail.
     */
    @Override
    public String getUserMail() {
        return host.getUserMail();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the active.
     */
    @Override
    public boolean getActive() {
        return host.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the comment.
     */
    @Override
    public String getComment() {
        return host.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the connections.
     */
    @Override
    public int getConnections() {
        return this.host.getHostStats().getConnections();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the properties.
     */
    @Override
    public String getProperties() {
        return setup.getProperties(true);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the javascript.
     */
    @Override
    public String getJavascript() {
        return setup.getScript();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data. This is the method which is called from the JSP to get the content of the setup.
     */
    @Override
    public String getData() {
        return setup.getData();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last output.
     */
    @Override
    public String getLastOutput() {
        final var hostOutput = this.host.getHostOutput();
        if (hostOutput == null) {
            return "[n/a]";
        }
        final var output = hostOutput.getOutput();
        try {
            return Format.uncompressBase64(output);
        } catch (final Exception _) {
            return output;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted last output.
     */
    @Override
    public String getFormattedLastOutput() {
        final var lastOutput = getLastOutput();
        return lastOutput == null || lastOutput.trim().isEmpty() ? "[n/a]" : Util.getFormatted(user, lastOutput);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ec user.
     */
    @Override
    public EcUser getEcUser() throws EcUserException {
        return EcUserHome.findByPrimaryKey(getECUserName());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the EC user name.
     */
    @Override
    public String getECUserName() {
        return host.getECUserName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host.
     */
    @Override
    public String getHost() {
        return host != null ? host.getHost() : "None";
    }

    /**
     * {@inheritDoc}
     *
     * Gets the login.
     */
    @Override
    public String getLogin() {
        return host.getLogin();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max connections.
     */
    @Override
    public int getMaxConnections() {
        return host.getMaxConnections();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return host != null ? host.getName() : "Unassigned";
    }

    /**
     * {@inheritDoc}
     *
     * Gets the passwd.
     */
    @Override
    public String getPasswd() {
        return host.getPasswd();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the retry count.
     */
    @Override
    public int getRetryCount() {
        return host.getRetryCount();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the retry frequency.
     */
    @Override
    public int getRetryFrequency() {
        return host.getRetryFrequency();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted retry frequency.
     */
    @Override
    public String getFormattedRetryFrequency() {
        return Format.formatDuration(getRetryFrequency());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer group.
     */
    @Override
    public TransferGroup getTransferGroup() throws DataFileException {
        return TransferGroupHome.findByPrimaryKey(getTransferGroupName());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer group name.
     */
    @Override
    public String getTransferGroupName() {
        return host.getTransferGroupName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer method.
     */
    @Override
    public TransferMethod getTransferMethod() throws TransferException {
        return TransferMethodHome.findByPrimaryKey(getTransferMethodName());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer method name.
     */
    @Override
    public String getTransferMethodName() {
        return host.getTransferMethodName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers today.
     */
    @Override
    public Collection<DataTransferLightBean> getDataTransfersToday() throws TransferException {
        return DataTransferHome.findByHostAndDate(this, new Date());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the allowed users.
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
     * {@inheritDoc}
     *
     * Sets the check frequency.
     */
    @Override
    public void setCheckFrequency(final long i) {
        host.setCheckFrequency(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the acquisition frequency.
     */
    @Override
    public void setAcquisitionFrequency(final long i) {
        host.setAcquisitionFrequency(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the check.
     */
    @Override
    public void setCheck(final boolean b) {
        host.setCheck(b);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the check filename.
     */
    @Override
    public void setCheckFilename(final String s) {
        host.setCheckFilename(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the mail on success.
     */
    @Override
    public void setMailOnSuccess(final boolean b) {
        host.setMailOnSuccess(b);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the mail on error.
     */
    @Override
    public void setMailOnError(final boolean b) {
        host.setMailOnError(b);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the notify once.
     */
    @Override
    public void setNotifyOnce(final boolean b) {
        host.setNotifyOnce(b);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the user mail.
     */
    @Override
    public void setUserMail(final String s) {
        host.setUserMail(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
     */
    @Override
    public void setActive(final boolean b) {
        host.setActive(b);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the comment.
     */
    @Override
    public void setComment(final String s) {
        host.setComment(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the data.
     */
    @Override
    public void setData(final String s) {
        host.setData(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the EC user name.
     */
    @Override
    public void setECUserName(final String u) {
        host.setECUserName(u);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the host.
     */
    @Override
    public void setHost(final String s) {
        host.setHost(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the login.
     */
    @Override
    public void setLogin(final String s) {
        host.setLogin(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max connections.
     */
    @Override
    public void setMaxConnections(final int i) {
        host.setMaxConnections(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the name.
     */
    @Override
    public void setName(final String s) {
        host.setName(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the passwd.
     */
    @Override
    public void setPasswd(final String s) {
        host.setPasswd(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the retry count.
     */
    @Override
    public void setRetryCount(final int i) {
        host.setRetryCount(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the retry frequency.
     */
    @Override
    public void setRetryFrequency(final int i) {
        host.setRetryFrequency(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer method.
     */
    @Override
    public void setTransferMethod(final TransferMethod m) {
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer method name.
     */
    @Override
    public void setTransferMethodName(final String s) {
        host.setTransferMethodName(s);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer group.
     */
    @Override
    public void setTransferGroup(final TransferGroup m) {
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer group name.
     */
    @Override
    public void setTransferGroupName(final String s) {
        host.setTransferGroupName(s);
    }

    /**
     * {@inheritDoc}
     *
     * Increase priority within destination.
     */
    @Override
    public void increasePriorityWithinDestination(final Destination d, final int step) {
        increasedPriorities.add(d);
        this.step = step;
    }

    /**
     * {@inheritDoc}
     *
     * Decrease priority within destination.
     */
    @Override
    public void decreasePriorityWithinDestination(final Destination d, final int step) {
        decreasedPriorities.add(d);
        this.step = step;
    }

    /**
     * {@inheritDoc}
     *
     * Adds the allowed user.
     */
    @Override
    public void addAllowedUser(final EcUser u) {
        addedEcUsers.add(u);
    }

    /**
     * {@inheritDoc}
     *
     * Delete allowed user.
     */
    @Override
    public void deleteAllowedUser(final EcUser u) {
        deletedEcUsers.add(u);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
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
        return getClass().getName() + " { " + host + " }";
    }

    /**
     * {@inheritDoc}
     *
     * Gets the dir.
     */
    @Override
    public String getDir() {
        return host.getDir();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the dir.
     */
    @Override
    public void setDir(final String s) {
        host.setDir(s);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the duration.
     */
    @Override
    public long getDuration() {
        final var hostStats = host.getHostStats();
        final var duration = hostStats.getDuration();
        final var sent = hostStats.getSent();
        return sent > Cnf.at("HostBean", "minSent", sent) ? duration : Cnf.at("HostBean", "duration", duration);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted duration.
     */
    @Override
    public String getFormattedDuration() {
        return Format.formatDuration(getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the sent.
     */
    @Override
    public long getSent() {
        final var curSent = host.getHostStats().getSent();
        final var minSent = Cnf.at("HostBean", "minSent", curSent);
        return curSent > minSent ? curSent : minSent;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted sent.
     */
    @Override
    public String getFormattedSent() {
        return Format.formatSize(getSent());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the band width.
     */
    @Override
    public long getBandWidth() {
        return getSent() > 0 && getDuration() > 0 ? getSent() / getDuration() : 0;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted band width.
     */
    @Override
    public String getFormattedBandWidth() {
        return Format.formatRate(getSent(), getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted band width in M bits per seconds.
     */
    @Override
    public double getFormattedBandWidthInMBitsPerSeconds() {
        return Format.getMBitsPerSeconds(getSent(), getDuration());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destinations.
     */
    @Override
    public Collection<Destination> getDestinations() throws TransferException {
        return DestinationHome.findByHost(this);
    }

    /**
     * {@inheritDoc}
     *
     * Transfer.
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
     * {@inheritDoc}
     *
     * Gets the report.
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
     * {@inheritDoc}
     *
     * Gets the report.
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
     * {@inheritDoc}
     *
     * Gets the output.
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
     * {@inheritDoc}
     *
     * Reset transfer statistics.
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
     * {@inheritDoc}
     *
     * Clean data window.
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
     * {@inheritDoc}
     *
     * Gets the change log list.
     */
    @Override
    public Collection<ChangeLog> getChangeLogList() throws TransferException {
        return HostHome.findChangeLogList(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the automatic location.
     */
    @Override
    public boolean getAutomaticLocation() {
        return host.getAutomaticLocation();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the automatic location.
     */
    @Override
    public void setAutomaticLocation(final boolean b) {
        host.setAutomaticLocation(b);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the latitude.
     */
    @Override
    public Double getLatitude() {
        return host.getHostLocation().getLatitude();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the latitude.
     */
    @Override
    public void setLatitude(final Double f) {
        host.getHostLocation().setLatitude(f);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the longitude.
     */
    @Override
    public Double getLongitude() {
        return host.getHostLocation().getLongitude();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the longitude.
     */
    @Override
    public void setLongitude(final Double f) {
        host.getHostLocation().setLongitude(f);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the nick name.
     */
    @Override
    public String getNickName() {
        return host.getNickname();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the nick name.
     */
    @Override
    public void setNickName(final String s) {
        host.setNickname(s);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the type.
     */
    @Override
    public String getType() {
        return host.getType();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the type.
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
     * {@inheritDoc}
     *
     * Gets the filter name.
     */
    @Override
    public String getFilterName() {
        return host.getFilterName();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the filter name.
     */
    @Override
    public void setFilterName(final String s) {
        host.setFilterName(s);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the network name.
     */
    @Override
    public String getNetworkName() {
        return host.getNetworkName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the all networks.
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
     * {@inheritDoc}
     *
     * Gets the transfer module names.
     */
    @Override
    public String getTransferModuleNames() throws TransferException {
        return EcTransModuleHome.findAll().stream().map(module -> "\"" + module.getName() + "\"")
                .collect(Collectors.joining(", "));
    }

    /**
     * {@inheritDoc}
     *
     * Sets the network name.
     */
    @Override
    public void setNetworkName(final String s) {
        host.setNetworkName(s);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the network code.
     */
    @Override
    public String getNetworkCode() {
        return host.getNetworkCode();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the network code.
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
     * {@inheritDoc}
     *
     * Gets the completions.
     */
    @Override
    public String getCompletions() {
        return ECtransOptions.toString(ECtransGroups.HOST);
    }
}
