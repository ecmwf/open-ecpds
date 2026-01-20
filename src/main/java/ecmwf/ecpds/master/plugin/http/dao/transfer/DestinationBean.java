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

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_ALIAS;
import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_ECTRANS;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.MonitoredInputStream;
import ecmwf.common.technical.ProxyEvent;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferGroupHome;
import ecmwf.ecpds.master.plugin.http.home.ecuser.EcUserHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.CountryHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationMetaDataHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferHistoryHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUserException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.transfer.Alias;
import ecmwf.ecpds.master.plugin.http.model.transfer.ChangeLog;
import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.DestinationMetaData;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicyException;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.plugin.http.model.transfer.Traffic;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.ModelException;
import ecmwf.web.model.users.User;
import ecmwf.web.services.content.Content;
import ecmwf.web.services.content.DefaultContent;
import ecmwf.web.util.bean.Pair;
import ecmwf.web.util.bean.StringPair;

/**
 * The Class DestinationBean.
 */
public class DestinationBean extends ModelBeanBase implements Destination, OjbImplementedBean {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DestinationBean.class);

    /** The Constant MAXIMUM_FILES_IN_TIMELINE. */
    private static final int MAXIMUM_FILES_IN_TIMELINE = 5000;

    /** The added associated ec users. */
    private final Collection<EcUser> addedAssociatedEcUsers = new ArrayList<>();

    /** The deleted associated ec users. */
    private final Collection<EcUser> deletedAssociatedEcUsers = new ArrayList<>();

    /** The added hosts. */
    private final Collection<Host> addedHosts = new ArrayList<>();

    /** The deleted hosts. */
    private final Collection<Host> deletedHosts = new ArrayList<>();

    /** The added incoming policies. */
    private final Collection<IncomingPolicy> addedIncomingPolicies = new ArrayList<>();

    /** The deleted incoming policies. */
    private final Collection<IncomingPolicy> deletedIncomingPolicies = new ArrayList<>();

    /** The added aliases. */
    private final Collection<Destination> addedAliases = new ArrayList<>();

    /** The deleted aliases. */
    private final Collection<Destination> deletedAliases = new ArrayList<>();

    /** The deleted metadata files. */
    private final Collection<String> deletedMetadataFiles = new ArrayList<>();

    /** The destination. */
    private final ecmwf.common.database.Destination destination;

    /** The setup. */
    private final ECtransSetup setup;

    /** The status map. */
    private final StatusMap statusMap;

    /** The data alias. */
    private final String dataAlias;

    /** The name. */
    private final String name;

    /**
     * Instantiates a new destination bean.
     *
     * @param originalDestination
     *            the original destination
     * @param aliasFrom
     *            the alias from
     * @param destination
     *            the destination
     */
    protected DestinationBean(final Destination originalDestination, final boolean aliasFrom,
            final ecmwf.common.database.Destination destination) {
        setId(name = destination.getName());
        this.destination = destination;
        this.setup = DESTINATION_ECTRANS.getECtransSetup(destination.getData());
        statusMap = new StatusMap(name);
        dataAlias = (aliasFrom ? DESTINATION_ALIAS.getECtransSetup(getData()).get(originalDestination.getName(), ".*")
                : DESTINATION_ALIAS.getECtransSetup(originalDestination.getData()).get(name, ".*")).trim();
    }

    /**
     * Instantiates a new destination bean.
     *
     * @param destination
     *            the destination
     */
    protected DestinationBean(final ecmwf.common.database.Destination destination) {
        setId(name = destination.getName());
        this.destination = destination;
        this.setup = DESTINATION_ECTRANS.getECtransSetup(destination.getData());
        statusMap = new StatusMap(name);
        dataAlias = null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return destination;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return Destination.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return getId();
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
     * Gets the data.
     */
    @Override
    public String getData() {
        return setup.getData();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data alias.
     */
    @Override
    public String getDataAlias() {
        return dataAlias.replace("\n", "<br>");
    }

    /**
     * {@inheritDoc}
     *
     * Sets the data.
     */
    @Override
    public void setData(final String data) {
        setup.setData(data);
        destination.setData(getData());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the active.
     */
    @Override
    public boolean getActive() {
        return destination.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the backup.
     */
    @Override
    public boolean getBackup() {
        return destination.getBackup();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the acquisition.
     */
    @Override
    public boolean getAcquisition() {
        return destination.getAcquisition();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ec user.
     */
    @Override
    public EcUser getEcUser() throws TransferException {
        try {
            return EcUserHome.findByPrimaryKey(getEcUserName());
        } catch (final EcUserException e) {
            throw new TransferException("Problem getting an user ('" + getEcUserName() + "') for Destination", e);
        }
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
        return destination.getTransferGroupName();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer group.
     */
    @Override
    public void setTransferGroup(final TransferGroup m) {
        // We don't set the transfer group from the interface!
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer group name.
     */
    @Override
    public void setTransferGroupName(final String s) {
        destination.setTransferGroupName(s);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the associated ec users.
     */
    @Override
    public Collection<EcUser> getAssociatedEcUsers() throws TransferException {
        try {
            return EcUserHome.findAssociatedToDestination(this);
        } catch (final EcUserException e) {
            throw new TransferException("Problem getting associated users for Destination", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the associated incoming policies.
     */
    @Override
    public Collection<IncomingPolicy> getAssociatedIncomingPolicies() throws TransferException {
        try {
            return IncomingPolicyHome.findAssociatedToDestination(this);
        } catch (final IncomingPolicyException e) {
            throw new TransferException("Problem getting associated policies for Destination", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ec user name.
     */
    @Override
    public String getEcUserName() {
        return destination.getECUserName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the country.
     */
    @Override
    public Country getCountry() throws TransferException {
        return CountryHome.findByPrimaryKey(getCountryIso());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the country iso.
     */
    @Override
    public String getCountryIso() {
        return destination.getCountryIso();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host for source.
     */
    @Override
    public Host getHostForSource() throws TransferException {
        final var hostForSourceName = getHostForSourceName();
        return hostForSourceName != null ? HostHome.findByPrimaryKey(hostForSourceName) : null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host for source name.
     */
    @Override
    public String getHostForSourceName() {
        return destination.getHostForSourceName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the if target exist.
     */
    @Override
    public int getIfTargetExist() {
        return destination.getIfTargetExist();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the on host failure.
     */
    @Override
    public int getOnHostFailure() {
        return destination.getOnHostFailure();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the keep in spool.
     */
    @Override
    public int getKeepInSpool() {
        return destination.getKeepInSpool();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the if target exist text.
     */
    @Override
    public String getIfTargetExistText() {
        return DestinationOption.ifTargetExist.get(destination.getIfTargetExist());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the on host failure text.
     */
    @Override
    public String getOnHostFailureText() {
        return DestinationOption.onHostFailure.get(destination.getOnHostFailure());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the keep in spool text.
     */
    @Override
    public String getKeepInSpoolText() {
        return DestinationOption.deleteFromSpool.get(destination.getKeepInSpool());
    }

    /**
     * Gets the mail on update.
     *
     * @return the mail on update
     */
    @Override
    public boolean getMailOnUpdate() {
        return destination.getMailOnUpdate();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the mail on end.
     */
    @Override
    public boolean getMailOnEnd() {
        return destination.getMailOnEnd();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the mail on error.
     */
    @Override
    public boolean getMailOnError() {
        return destination.getMailOnError();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the mail on start.
     */
    @Override
    public boolean getMailOnStart() {
        return destination.getMailOnStart();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user mail.
     */
    @Override
    public String getUserMail() {
        return destination.getUserMail();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max start.
     */
    @Override
    public int getMaxStart() {
        return destination.getMaxStart();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the start frequency.
     */
    @Override
    public int getStartFrequency() {
        return destination.getStartFrequency();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted start frequency.
     */
    @Override
    public String getFormattedStartFrequency() {
        return Format.formatDuration(getStartFrequency());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max inactivity.
     */
    @Override
    public int getMaxInactivity() {
        return destination.getMaxInactivity();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted max inactivity.
     */
    @Override
    public String getFormattedMaxInactivity() {
        return Format.formatDuration(getMaxInactivity());
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max inactivity.
     */
    @Override
    public void setMaxInactivity(final int i) {
        destination.setMaxInactivity(i);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the reset frequency.
     */
    @Override
    public long getResetFrequency() {
        return destination.getResetFrequency();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max file size.
     */
    @Override
    public void setMaxFileSize(final long i) {
        destination.setMaxFileSize(i);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the formatted reset frequency.
     */
    @Override
    public String getFormattedResetFrequency() {
        return Format.formatDuration(getResetFrequency());
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max start.
     */
    @Override
    public void setMaxStart(final int i) {
        destination.setMaxStart(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max requeue.
     */
    @Override
    public void setMaxRequeue(final int i) {
        destination.setMaxRequeue(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the start frequency.
     */
    @Override
    public void setStartFrequency(final int i) {
        destination.setStartFrequency(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the reset frequency.
     */
    @Override
    public void setResetFrequency(final long i) {
        destination.setResetFrequency(i);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the group by date.
     */
    @Override
    public boolean getGroupByDate() {
        return destination.getGroupByDate();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the group by date.
     */
    @Override
    public void setGroupByDate(final boolean b) {
        destination.setGroupByDate(b);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the date format.
     */
    @Override
    public String getDateFormat() {
        return destination.getDateFormat();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the date format.
     */
    @Override
    public void setDateFormat(final String format) {
        destination.setDateFormat(format);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers on date.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersOnDate(final Date d) throws TransferException {
        return DataTransferHome.findByDestinationAndDate(this, d);
    }

    /**
     * Gets the data transfers on transmission date.
     *
     * @param d
     *            the d
     *
     * @return the data transfers on transmission date
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    public Collection<DataTransfer> getDataTransfersOnTransmissionDate(final Date d) throws TransferException {
        return DataTransferHome.findByDestinationAndTransmissionDate(this, d);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers including retries on transmission date.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersIncludingRetriesOnTransmissionDate(final Date d)
            throws TransferException {
        return getDataTransfersIncludingRetries(getDataTransfersOnTransmissionDate(d));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by product and time.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByProductAndTime(final String product, final String time)
            throws TransferException {
        return DataTransferHome.findByDestinationProductAndTime(this, product, time);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the data transfers by product and time on date.
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByProductAndTimeOnDate(final String product, final String time,
            final Date d) throws TransferException {
        return DataTransferHome.findByDestinationProductAndTimeOnDate(this, product, time, d);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the hosts and priorities.
     */
    @Override
    public Collection<Pair> getHostsAndPriorities() throws TransferException {
        return HostHome.findWithPriorityByDestination(this);
    }

    /**
     * Gets the associated hosts and priorities.
     *
     * @param type
     *            the type
     *
     * @return the associated hosts and priorities
     *
     * @throws TransferException
     *             the transfer exception
     */
    private Collection<Pair> getAssociatedHostsAndPriorities(final String type) throws TransferException {
        final Collection<Pair> selectedAssignedPairs = new ArrayList<>();
        for (final Pair pair : getHostsAndPriorities()) {
            if (type.equals(((Host) pair.getName()).getType())) {
                selectedAssignedPairs.add(pair);
            }
        }
        return selectedAssignedPairs;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the dissemination hosts and priorities.
     */
    @Override
    public Collection<Pair> getDisseminationHostsAndPriorities() throws TransferException {
        return getAssociatedHostsAndPriorities(HostOption.DISSEMINATION);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the acquisition hosts and priorities.
     */
    @Override
    public Collection<Pair> getAcquisitionHostsAndPriorities() throws TransferException {
        return getAssociatedHostsAndPriorities(HostOption.ACQUISITION);
    }

    /**
     * Gets the proxy hosts and priorities.
     *
     * @return the proxy hosts and priorities
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    public Collection<Pair> getProxyHostsAndPriorities() throws TransferException {
        return getAssociatedHostsAndPriorities(HostOption.PROXY);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the aliases.
     */
    @Override
    public Collection<Destination> getAliases() throws TransferException {
        return DestinationHome.findAliases(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the alias list.
     */
    @Override
    public Collection<Alias> getAliasList() throws TransferException {
        return DestinationHome.findAliasList(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the traffic list.
     */
    @Override
    public Collection<Traffic> getTrafficList() throws TransferException {
        return DestinationHome.findTrafficList(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the change log list.
     */
    @Override
    public Collection<ChangeLog> getChangeLogList() throws TransferException {
        return DestinationHome.findChangeLogList(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the aliased from.
     */
    @Override
    public Collection<Destination> getAliasedFrom() throws TransferException {
        return DestinationHome.findAliasedFrom(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the meta data.
     */
    @Override
    public Collection<DestinationMetaData> getMetaData() throws TransferException {
        return DestinationMetaDataHome.findByDestination(this);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
     */
    @Override
    public void setActive(final boolean param) {
        destination.setActive(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the backup.
     */
    @Override
    public void setBackup(final boolean param) {
        destination.setBackup(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the acquisition.
     */
    @Override
    public void setAcquisition(final boolean param) {
        destination.setAcquisition(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the country iso.
     */
    @Override
    public void setCountryIso(final String param) {
        destination.setCountryIso(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the host for source name.
     */
    @Override
    public void setHostForSourceName(final String name) {
        destination.setHostForSourceName(name);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the ec user name.
     */
    @Override
    public void setEcUserName(final String param) {
        destination.setECUserName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the if target exist.
     */
    @Override
    public void setIfTargetExist(final int param) {
        destination.setIfTargetExist(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the on host failure.
     */
    @Override
    public void setOnHostFailure(final int i) {
        destination.setOnHostFailure(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the keep in spool.
     */
    @Override
    public void setKeepInSpool(final int i) {
        destination.setKeepInSpool(i);
    }

    /**
     * Sets the mail on update.
     *
     * @param param
     *            the new mail on update
     */
    @Override
    public void setMailOnUpdate(final boolean param) {
        destination.setMailOnUpdate(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the mail on end.
     */
    @Override
    public void setMailOnEnd(final boolean param) {
        destination.setMailOnEnd(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the mail on error.
     */
    @Override
    public void setMailOnError(final boolean param) {
        destination.setMailOnError(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the mail on start.
     */
    @Override
    public void setMailOnStart(final boolean param) {
        destination.setMailOnStart(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the name.
     */
    @Override
    public void setName(final String param) {
        destination.setName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the user mail.
     */
    @Override
    public void setUserMail(final String param) {
        destination.setUserMail(param);
    }

    /**
     * {@inheritDoc}
     *
     * Adds the associated ec user.
     */
    @Override
    public synchronized void addAssociatedEcUser(final EcUser u) {
        addedAssociatedEcUsers.add(u);
    }

    /**
     * {@inheritDoc}
     *
     * Delete associated ec user.
     */
    @Override
    public synchronized void deleteAssociatedEcUser(final EcUser u) {
        deletedAssociatedEcUsers.add(u);
    }

    /**
     * {@inheritDoc}
     *
     * Adds the host.
     */
    @Override
    public synchronized void addHost(final Host h) {
        addedHosts.add(h);
    }

    /**
     * {@inheritDoc}
     *
     * Adds the incoming policy.
     */
    @Override
    public synchronized void addIncomingPolicy(final IncomingPolicy p) {
        addedIncomingPolicies.add(p);
    }

    /**
     * {@inheritDoc}
     *
     * Delete host.
     */
    @Override
    public synchronized void deleteHost(final Host h) {
        deletedHosts.add(h);
    }

    /**
     * {@inheritDoc}
     *
     * Delete incoming policy.
     */
    @Override
    public synchronized void deleteIncomingPolicy(final IncomingPolicy p) {
        deletedIncomingPolicies.add(p);
    }

    /**
     * {@inheritDoc}
     *
     * Adds the alias.
     */
    @Override
    public synchronized void addAlias(final Destination d) {
        addedAliases.add(d);
    }

    /**
     * {@inheritDoc}
     *
     * Delete alias.
     */
    @Override
    public synchronized void deleteAlias(final Destination d) {
        deletedAliases.add(d);
    }

    /**
     * {@inheritDoc}
     *
     * Delete metadata file.
     */
    @Override
    public void deleteMetadataFile(final String fileName) {
        deletedMetadataFiles.add(fileName);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the filter name.
     */
    @Override
    public String getFilterName() {
        return destination.getFilterName();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the filter name.
     */
    @Override
    public void setFilterName(final String param) {
        destination.setFilterName(param);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the comment.
     */
    @Override
    public String getComment() {
        return destination.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the comment.
     */
    @Override
    public void setComment(final String param) {
        destination.setComment(param);
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is dirty.
     */
    @Override
    public boolean isDirty() {
        final Date lastChange = destination.getUpdate();
        if (lastChange != null) {
            try {
                return lastChange.after(MasterManager.getMI().getDestinationStartDate(name));
            } catch (final Exception e) {
                final var message = e.getMessage();
                if (message == null || message.indexOf("is off line") == -1) {
                    log.warn("Exception getting last update for destination '{}' from MasterServer", name, e);
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max connections.
     */
    @Override
    public int getMaxConnections() {
        return destination.getMaxConnections();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the retry count.
     */
    @Override
    public int getRetryCount() {
        return destination.getRetryCount();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the retry frequency.
     */
    @Override
    public int getRetryFrequency() {
        return destination.getRetryFrequency();
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
     * Gets the formatted status.
     */
    @Override
    public String getFormattedStatus() {
        try {
            return StatusFactory.getDestinationStatusName(destination,
                    MasterManager.getMI().getDestinationStatus(name));
        } catch (final Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last transfer.
     */
    @Override
    public DataTransfer getLastTransfer() {
        try {
            final var d = MasterManager.getMI().getDestinationLastTransfer(name);
            return d != null && d.getFinishTime() != null ? new DataTransferBaseBean(d) : null;
        } catch (final Exception e) {
            log.error("No last transfer for destination '{}'", name, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last error.
     */
    @Override
    public DataTransfer getLastError() {
        try {
            final var d = MasterManager.getMI().getDestinationLastFailedTransfer(name);
            return d != null ? new DataTransferBaseBean(d) : null;
        } catch (final Exception e) {
            log.error("No last error for destination '{}'", name, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the queue size.
     */
    @Override
    public int getQueueSize() {
        try {
            return MasterManager.getMI().getDestinationSize(name);
        } catch (final Exception e) {
            log.error("Problem getting queue size", e);
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Restart.
     */
    @Override
    public void restart(final boolean graceful, final User u) throws TransferException {
        try {
            MasterManager.getMI().restartDestination(Util.getECpdsSessionFromObject(u), name, graceful);
        } catch (final Exception e) {
            throw new TransferException("Problem restarting destination '" + name + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Put on hold.
     */
    @Override
    public void putOnHold(final boolean graceful, final User u) throws TransferException {
        try {
            MasterManager.getMI().holdDestination(Util.getECpdsSessionFromObject(u), name, graceful);
        } catch (final Exception e) {
            throw new TransferException("Problem putting destination '" + name + "' on Hold", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Clean.
     */
    @Override
    public void clean(final User u) throws TransferException {
        try {
            MasterManager.getMI().removeDestination(Util.getECpdsSessionFromObject(u), name, true, true);
        } catch (final Exception e) {
            throw new TransferException("Problem cleaning destination '" + name + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Clean expired.
     */
    @Override
    public void cleanExpired(final User u) throws TransferException {
        try {
            MasterManager.getMI().removeDestination(Util.getECpdsSessionFromObject(u), name, true, false);
        } catch (final Exception e) {
            throw new TransferException("Problem cleaning destination '" + name + "'", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max connections.
     */
    @Override
    public void setMaxConnections(final int i) {
        destination.setMaxConnections(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the retry count.
     */
    @Override
    public void setRetryCount(final int i) {
        destination.setRetryCount(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the retry frequency.
     */
    @Override
    public void setRetryFrequency(final int i) {
        destination.setRetryFrequency(i);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the type.
     */
    @Override
    public void setType(final int i) {
        destination.setType(i);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the type.
     */
    @Override
    public int getType() {
        return destination.getType();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the type text.
     */
    @Override
    public String getTypeText() {
        return DestinationOption.getLabel(destination.getType());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status map for products.
     */
    @Override
    public Map<String, DestinationProductStatus> getStatusMapForProducts() {
        return statusMap;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the monitoring status.
     */
    @Override
    public DestinationStatus getMonitoringStatus() throws TransferException {
        try {
            return DestinationStatusHome.findByName(name);
        } catch (final MonitoringException e) {
            throw new TransferException("Error getting Status for destination " + name, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the band width.
     */
    @Override
    public long getBandWidth() throws TransferException {
        final var transferRate = destination.getTransferRate();
        if (transferRate > 0) {
            return transferRate;
        }
        try {
            var transferred = 0L;
            var time = 0L;
            for (final Host host : HostHome.findByDestination(this)) {
                transferred += host.getSent();
                time += host.getDuration();
            }
            return time != 0 ? transferred / time : 0;
        } catch (final TransferException e) {
            throw new TransferException("Error calculating destination bandwidth", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max requeue.
     */
    @Override
    public int getMaxRequeue() {
        return destination.getMaxRequeue();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bad data transfers.
     */
    @Override
    public Collection<DataTransfer> getBadDataTransfers() throws TransferException {
        return DataTransferHome.findBadByDestination(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bad data transfers size.
     */
    @Override
    public int getBadDataTransfersSize() throws TransferException {
        return DataTransferHome.findBadCountByDestination(this);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the stop if dirty.
     */
    @Override
    public boolean getStopIfDirty() {
        return destination.getStopIfDirty();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the stop if dirty.
     */
    @Override
    public void setStopIfDirty(final boolean b) {
        destination.setStopIfDirty(b);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max pending.
     */
    @Override
    public int getMaxPending() {
        return destination.getMaxPending();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the max file size.
     */
    @Override
    public long getMaxFileSize() {
        return destination.getMaxFileSize();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the max pending.
     */
    @Override
    public void setMaxPending(final int param) {
        destination.setMaxPending(param);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the show in monitors.
     */
    @Override
    public boolean getShowInMonitors() {
        return destination.getMonitor();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the show in monitors.
     */
    @Override
    public void setShowInMonitors(final boolean b) {
        destination.setMonitor(b);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the start time.
     */
    @Override
    public Date getStartTime() throws TransferException {
        try {
            return MasterManager.getMI().getDestinationStartDate(name);
        } catch (final Exception e) {
            throw new TransferException("Problem with destination '" + name + "' getting Start Time", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer content.
     */
    @Override
    public Content getTransferContent(final DataTransfer t, final User u) throws TransferException {
        if (!t.getDestinationName().equals(name)) {
            throw new TransferException(
                    "Data transfer " + t + " can only be downloaded through its correct destination ("
                            + t.getDestinationName() + "), not " + name);
        }
        try {
            final var di = MasterManager.getDI();
            final var proxy = di.getProxySocketInput(name, "DataTransferId=" + t.getId(), 0);
            return new DataTransferContent(t, new MonitoredInputStream(proxy.getDataInputStream()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    try {
                        // Make sure we run the check method to record the download in the transfer
                        // history and update the transfer status to DONE!
                        final var session = Util.getECpdsSessionFromObject(u);
                        final var event = new ProxyEvent(proxy);
                        event.setDuration(getDuration());
                        event.setProtocol("http");
                        event.setLocalHost(Cnf.at("Login", "hostName"));
                        event.setRemoteHost(session.getWebUser().getLastLoginHost());
                        event.setUserType(ProxyEvent.UserType.WEB_USER);
                        event.setUserName(session.getWebUser().getId());
                        event.setStartTime(getStartTime());
                        event.setSent(getByteSent());
                        di.check(proxy);
                    } catch (MasterException | ModelException e) {
                        throw new IOException(e);
                    }
                }
            });
        } catch (final Exception e) {
			log.warn("Problem getting download for transfer {} from destination '{}'", t.getId(), name, e);
			throw new TransferException(
					"Problem with destination '" + name + "' getting download for transfer " + t.getId(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the all types.
     */
    @Override
    public List<StringPair> getAllTypes() {
        final List<StringPair> pairs = new ArrayList<>();
        for (final Map.Entry<Integer, String> entry : DestinationOption.getTypes(false)) {
            pairs.add(new StringPair(String.valueOf(entry.getKey()), entry.getValue()));
        }
        return pairs;
    }

    /**
     * Sets the update.
     *
     * @param d
     *            the new update
     */
    protected void setUpdate(final Date d) {
        destination.setUpdate(new Timestamp(d.getTime()));
    }

    /**
     * Gets the added associated ec users.
     *
     * @return the added associated ec users
     */
    protected Collection<EcUser> getAddedAssociatedEcUsers() {
        return addedAssociatedEcUsers;
    }

    /**
     * Gets the added hosts.
     *
     * @return the added hosts
     */
    protected Collection<Host> getAddedHosts() {
        return addedHosts;
    }

    /**
     * Gets the added incoming policies.
     *
     * @return the added incoming policies
     */
    protected Collection<IncomingPolicy> getAddedIncomingPolicies() {
        return addedIncomingPolicies;
    }

    /**
     * Gets the deleted policy associations.
     *
     * @return the deleted policy associations
     */
    protected Collection<IncomingPolicy> getDeletedPolicyAssociations() {
        return deletedIncomingPolicies;
    }

    /**
     * Gets the deleted associated ec users.
     *
     * @return the deleted associated ec users
     */
    protected Collection<EcUser> getDeletedAssociatedEcUsers() {
        return deletedAssociatedEcUsers;
    }

    /**
     * Gets the deleted hosts.
     *
     * @return the deleted hosts
     */
    protected Collection<Host> getDeletedHosts() {
        return deletedHosts;
    }

    /**
     * Sets the updated now.
     */
    protected void setUpdatedNow() {
        destination.setUpdate(new Timestamp(new Date().getTime()));
    }

    /**
     * Gets the added aliases.
     *
     * @return the added aliases
     */
    protected Collection<Destination> getAddedAliases() {
        return addedAliases;
    }

    /**
     * Gets the deleted aliases.
     *
     * @return the deleted aliases
     */
    protected Collection<Destination> getDeletedAliases() {
        return deletedAliases;
    }

    /**
     * Gets the deleted metadata files.
     *
     * @return the deleted metadata files
     */
    protected Collection<String> getDeletedMetadataFiles() {
        return deletedMetadataFiles;
    }

    /**
     * Returns the supplied collection of transfers, but converting those which have been restarted.
     *
     * @param baseDataTransfers
     *            the base data transfers
     *
     * @return the data transfers including retries
     *
     * @throws TransferException
     *             the transfer exception
     */
    private Collection<DataTransfer> getDataTransfersIncludingRetries(final Collection<DataTransfer> baseDataTransfers)
            throws TransferException {
        final Collection<DataTransfer> result = new ArrayList<>(baseDataTransfers.size());
        final var i = baseDataTransfers.iterator();
        // Same limit as in getDataTransfersByDestinationAndTargetOnTransmissionDate to
        // make sure we don't run out of memory!
        while (i.hasNext() && result.size() <= MAXIMUM_FILES_IN_TIMELINE) {
            final var dt = i.next();
            if (dt.getStartCount() > 1) {
                // This transfer was restarted. This means that we will show more than one entry
                // here.
                final var history = TransferHistoryHome.findByDataTransfer(dt);
                final var j = history.iterator();
                DataTransfer dataTransfer = null;
                while (j.hasNext() && result.size() <= MAXIMUM_FILES_IN_TIMELINE) {
                    final var item = j.next();
                    if (Status.EXEC.equals(item.getStatus())) {
                        dataTransfer = dt.getCopy();
                        dataTransfer.setStartTime(item.getDate());
                        dataTransfer.setStartCount(0);
                    } else if (dataTransfer != null
                            && (Status.STOP.equals(item.getStatus()) || Status.DONE.equals(item.getStatus()))) {
                        dataTransfer.setFinishTime(item.getDate());
                        dataTransfer.setStatus(item.getStatus());
                        dataTransfer.setRetry(true);
                        result.add(dataTransfer);
                        dataTransfer = null;
                    }
                }
            } else {
                result.add(dt);
            }
        }
        if (i.hasNext()) {
            log.warn("Some files won't be displayed on the TimeLine (> " + MAXIMUM_FILES_IN_TIMELINE + " files)");
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the completions.
     */
    @Override
    public String getCompletions() throws TransferException {
        final var sb = new StringBuilder();
        return sb
                .append(ECtransOptions.toString(ECtransOptions.DESTINATION_ALIAS_PATTERN,
                        getAliases().stream().map(Destination::getName).toList()))
                .append(sb.length() > 0 ? ",\n" : "").append(ECtransOptions.toString(ECtransGroups.DESTINATION))
                .toString();
    }

    /**
     * The Class DataTransferContent.
     */
    private static final class DataTransferContent extends DefaultContent {

        /** The t. */
        private final DataTransfer t;

        /** The s. */
        private final InputStream s;

        /**
         * Instantiates a new data transfer content.
         *
         * @param t
         *            the t
         * @param s
         *            the s
         */
        public DataTransferContent(final DataTransfer t, final InputStream s) {
            super("", "application/octet-stream");
            this.t = t;
            this.s = s;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        @Override
        public String getName() {
            return t.getTarget();
        }

        /**
         * Gets the input stream.
         *
         * @return the input stream
         */
        @Override
        public InputStream getInputStream() {
            return s;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final DestinationBean destinationBean && equals(destinationBean);
    }

    /**
     * Equals.
     *
     * @param u
     *            the u
     *
     * @return true, if successful
     */
    public boolean equals(final DestinationBean u) {
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
        return getClass().getName() + " { " + destination + " }";
    }
}
