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
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return destination;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return Destination.class.getName();
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return getId();
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
     * Gets the data.
     *
     * @return the data
     */
    @Override
    public String getData() {
        return setup.getData();
    }

    /**
     * Gets the data alias.
     *
     * @return the data alias
     */
    @Override
    public String getDataAlias() {
        return dataAlias.replace("\n", "<br>");
    }

    /**
     * Sets the data.
     *
     * @param data
     *            the new data
     */
    @Override
    public void setData(final String data) {
        setup.setData(data);
        destination.setData(getData());
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    @Override
    public boolean getActive() {
        return destination.getActive();
    }

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    @Override
    public boolean getBackup() {
        return destination.getBackup();
    }

    /**
     * Gets the acquisition.
     *
     * @return the acquisition
     */
    @Override
    public boolean getAcquisition() {
        return destination.getAcquisition();
    }

    /**
     * Gets the ec user.
     *
     * @return the ec user
     *
     * @throws TransferException
     *             the transfer exception
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
        return destination.getTransferGroupName();
    }

    /**
     * Sets the transfer group.
     *
     * @param m
     *            the new transfer group
     */
    @Override
    public void setTransferGroup(final TransferGroup m) {
        // We don't set the transfer group from the interface!
    }

    /**
     * Sets the transfer group name.
     *
     * @param s
     *            the new transfer group name
     */
    @Override
    public void setTransferGroupName(final String s) {
        destination.setTransferGroupName(s);
    }

    /**
     * Gets the associated ec users.
     *
     * @return the associated ec users
     *
     * @throws TransferException
     *             the transfer exception
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
     * Gets the associated incoming policies.
     *
     * @return the associated incoming policies
     *
     * @throws TransferException
     *             the transfer exception
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
     * Gets the ec user name.
     *
     * @return the ec user name
     */
    @Override
    public String getEcUserName() {
        return destination.getECUserName();
    }

    /**
     * Gets the country.
     *
     * @return the country
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Country getCountry() throws TransferException {
        return CountryHome.findByPrimaryKey(getCountryIso());
    }

    /**
     * Gets the country iso.
     *
     * @return the country iso
     */
    @Override
    public String getCountryIso() {
        return destination.getCountryIso();
    }

    /**
     * Gets the host for source.
     *
     * @return the host for source
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Host getHostForSource() throws TransferException {
        final var hostForSourceName = getHostForSourceName();
        return hostForSourceName != null ? HostHome.findByPrimaryKey(hostForSourceName) : null;
    }

    /**
     * Gets the host for source name.
     *
     * @return the host for source name
     */
    @Override
    public String getHostForSourceName() {
        return destination.getHostForSourceName();
    }

    /**
     * Gets the if target exist.
     *
     * @return the if target exist
     */
    @Override
    public int getIfTargetExist() {
        return destination.getIfTargetExist();
    }

    /**
     * Gets the on host failure.
     *
     * @return the on host failure
     */
    @Override
    public int getOnHostFailure() {
        return destination.getOnHostFailure();
    }

    /**
     * Gets the keep in spool.
     *
     * @return the keep in spool
     */
    @Override
    public int getKeepInSpool() {
        return destination.getKeepInSpool();
    }

    /**
     * Gets the if target exist text.
     *
     * @return the if target exist text
     */
    @Override
    public String getIfTargetExistText() {
        return DestinationOption.ifTargetExist.get(destination.getIfTargetExist());
    }

    /**
     * Gets the on host failure text.
     *
     * @return the on host failure text
     */
    @Override
    public String getOnHostFailureText() {
        return DestinationOption.onHostFailure.get(destination.getOnHostFailure());
    }

    /**
     * Gets the keep in spool text.
     *
     * @return the keep in spool text
     */
    @Override
    public String getKeepInSpoolText() {
        return DestinationOption.deleteFromSpool.get(destination.getKeepInSpool());
    }

    /**
     * Gets the mail on end.
     *
     * @return the mail on end
     */
    @Override
    public boolean getMailOnEnd() {
        return destination.getMailOnEnd();
    }

    /**
     * Gets the mail on error.
     *
     * @return the mail on error
     */
    @Override
    public boolean getMailOnError() {
        return destination.getMailOnError();
    }

    /**
     * Gets the mail on start.
     *
     * @return the mail on start
     */
    @Override
    public boolean getMailOnStart() {
        return destination.getMailOnStart();
    }

    /**
     * Gets the user mail.
     *
     * @return the user mail
     */
    @Override
    public String getUserMail() {
        return destination.getUserMail();
    }

    /**
     * Gets the max start.
     *
     * @return the max start
     */
    @Override
    public int getMaxStart() {
        return destination.getMaxStart();
    }

    /**
     * Gets the start frequency.
     *
     * @return the start frequency
     */
    @Override
    public int getStartFrequency() {
        return destination.getStartFrequency();
    }

    /**
     * Gets the formatted start frequency.
     *
     * @return the formatted start frequency
     */
    @Override
    public String getFormattedStartFrequency() {
        return Format.formatDuration(getStartFrequency());
    }

    /**
     * Gets the max inactivity.
     *
     * @return the max inactivity
     */
    @Override
    public int getMaxInactivity() {
        return destination.getMaxInactivity();
    }

    /**
     * Gets the formatted max inactivity.
     *
     * @return the formatted max inactivity
     */
    @Override
    public String getFormattedMaxInactivity() {
        return Format.formatDuration(getMaxInactivity());
    }

    /**
     * Sets the max inactivity.
     *
     * @param i
     *            the new max inactivity
     */
    @Override
    public void setMaxInactivity(final int i) {
        destination.setMaxInactivity(i);
    }

    /**
     * Gets the reset frequency.
     *
     * @return the reset frequency
     */
    @Override
    public long getResetFrequency() {
        return destination.getResetFrequency();
    }

    /**
     * Sets the max file size.
     *
     * @param i
     *            the new max file size
     */
    @Override
    public void setMaxFileSize(final long i) {
        destination.setMaxFileSize(i);
    }

    /**
     * Gets the formatted reset frequency.
     *
     * @return the formatted reset frequency
     */
    @Override
    public String getFormattedResetFrequency() {
        return Format.formatDuration(getResetFrequency());
    }

    /**
     * Sets the max start.
     *
     * @param i
     *            the new max start
     */
    @Override
    public void setMaxStart(final int i) {
        destination.setMaxStart(i);
    }

    /**
     * Sets the max requeue.
     *
     * @param i
     *            the new max requeue
     */
    @Override
    public void setMaxRequeue(final int i) {
        destination.setMaxRequeue(i);
    }

    /**
     * Sets the start frequency.
     *
     * @param i
     *            the new start frequency
     */
    @Override
    public void setStartFrequency(final int i) {
        destination.setStartFrequency(i);
    }

    /**
     * Sets the reset frequency.
     *
     * @param i
     *            the new reset frequency
     */
    @Override
    public void setResetFrequency(final long i) {
        destination.setResetFrequency(i);
    }

    /**
     * Gets the group by date.
     *
     * @return the group by date
     */
    @Override
    public boolean getGroupByDate() {
        return destination.getGroupByDate();
    }

    /**
     * Sets the group by date.
     *
     * @param b
     *            the new group by date
     */
    @Override
    public void setGroupByDate(final boolean b) {
        destination.setGroupByDate(b);
    }

    /**
     * Gets the date format.
     *
     * @return the date format
     */
    @Override
    public String getDateFormat() {
        return destination.getDateFormat();
    }

    /**
     * Sets the date format.
     *
     * @param format
     *            the new date format
     */
    @Override
    public void setDateFormat(final String format) {
        destination.setDateFormat(format);
    }

    /**
     * Gets the data transfers on date.
     *
     * @param d
     *            the d
     *
     * @return the data transfers on date
     *
     * @throws TransferException
     *             the transfer exception
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
     * @throws TransferException
     *             the transfer exception
     */
    public Collection<DataTransfer> getDataTransfersOnTransmissionDate(final Date d) throws TransferException {
        return DataTransferHome.findByDestinationAndTransmissionDate(this, d);
    }

    /**
     * Gets the data transfers including retries on transmission date.
     *
     * @param d
     *            the d
     *
     * @return the data transfers including retries on transmission date
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<DataTransfer> getDataTransfersIncludingRetriesOnTransmissionDate(final Date d)
            throws TransferException {
        return getDataTransfersIncludingRetries(getDataTransfersOnTransmissionDate(d));
    }

    /**
     * Gets the data transfers by product and time.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @return the data transfers by product and time
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByProductAndTime(final String product, final String time)
            throws TransferException {
        return DataTransferHome.findByDestinationProductAndTime(this, product, time);
    }

    /**
     * Gets the data transfers by product and time on date.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     * @param d
     *            the d
     *
     * @return the data transfers by product and time on date
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<DataTransfer> getDataTransfersByProductAndTimeOnDate(final String product, final String time,
            final Date d) throws TransferException {
        return DataTransferHome.findByDestinationProductAndTimeOnDate(this, product, time, d);
    }

    /**
     * Gets the hosts and priorities.
     *
     * @return the hosts and priorities
     *
     * @throws TransferException
     *             the transfer exception
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
     * Gets the dissemination hosts and priorities.
     *
     * @return the dissemination hosts and priorities
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<Pair> getDisseminationHostsAndPriorities() throws TransferException {
        return getAssociatedHostsAndPriorities(HostOption.DISSEMINATION);
    }

    /**
     * Gets the acquisition hosts and priorities.
     *
     * @return the acquisition hosts and priorities
     *
     * @throws TransferException
     *             the transfer exception
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
     * @throws TransferException
     *             the transfer exception
     */
    public Collection<Pair> getProxyHostsAndPriorities() throws TransferException {
        return getAssociatedHostsAndPriorities(HostOption.PROXY);
    }

    /**
     * Gets the aliases.
     *
     * @return the aliases
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<Destination> getAliases() throws TransferException {
        return DestinationHome.findAliases(this);
    }

    /**
     * Gets the alias list.
     *
     * @return the alias list
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<Alias> getAliasList() throws TransferException {
        return DestinationHome.findAliasList(this);
    }

    /**
     * Gets the traffic list.
     *
     * @return the traffic list
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<Traffic> getTrafficList() throws TransferException {
        return DestinationHome.findTrafficList(this);
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
        return DestinationHome.findChangeLogList(this);
    }

    /**
     * Gets the aliased from.
     *
     * @return the aliased from
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<Destination> getAliasedFrom() throws TransferException {
        return DestinationHome.findAliasedFrom(this);
    }

    /**
     * Gets the meta data.
     *
     * @return the meta data
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<DestinationMetaData> getMetaData() throws TransferException {
        return DestinationMetaDataHome.findByDestination(this);
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    @Override
    public void setActive(final boolean param) {
        destination.setActive(param);
    }

    /**
     * Sets the backup.
     *
     * @param param
     *            the new backup
     */
    @Override
    public void setBackup(final boolean param) {
        destination.setBackup(param);
    }

    /**
     * Sets the acquisition.
     *
     * @param param
     *            the new acquisition
     */
    @Override
    public void setAcquisition(final boolean param) {
        destination.setAcquisition(param);
    }

    /**
     * Sets the country iso.
     *
     * @param param
     *            the new country iso
     */
    @Override
    public void setCountryIso(final String param) {
        destination.setCountryIso(param);
    }

    /**
     * Sets the host for source name.
     *
     * @param name
     *            the new host for source name
     */
    @Override
    public void setHostForSourceName(final String name) {
        destination.setHostForSourceName(name);
    }

    /**
     * Sets the ec user name.
     *
     * @param param
     *            the new ec user name
     */
    @Override
    public void setEcUserName(final String param) {
        destination.setECUserName(param);
    }

    /**
     * Sets the if target exist.
     *
     * @param param
     *            the new if target exist
     */
    @Override
    public void setIfTargetExist(final int param) {
        destination.setIfTargetExist(param);
    }

    /**
     * Sets the on host failure.
     *
     * @param i
     *            the new on host failure
     */
    @Override
    public void setOnHostFailure(final int i) {
        destination.setOnHostFailure(i);
    }

    /**
     * Sets the keep in spool.
     *
     * @param i
     *            the new keep in spool
     */
    @Override
    public void setKeepInSpool(final int i) {
        destination.setKeepInSpool(i);
    }

    /**
     * Sets the mail on end.
     *
     * @param param
     *            the new mail on end
     */
    @Override
    public void setMailOnEnd(final boolean param) {
        destination.setMailOnEnd(param);
    }

    /**
     * Sets the mail on error.
     *
     * @param param
     *            the new mail on error
     */
    @Override
    public void setMailOnError(final boolean param) {
        destination.setMailOnError(param);
    }

    /**
     * Sets the mail on start.
     *
     * @param param
     *            the new mail on start
     */
    @Override
    public void setMailOnStart(final boolean param) {
        destination.setMailOnStart(param);
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    @Override
    public void setName(final String param) {
        destination.setName(param);
    }

    /**
     * Sets the user mail.
     *
     * @param param
     *            the new user mail
     */
    @Override
    public void setUserMail(final String param) {
        destination.setUserMail(param);
    }

    /**
     * Adds the associated ec user.
     *
     * @param u
     *            the u
     */
    @Override
    public synchronized void addAssociatedEcUser(final EcUser u) {
        addedAssociatedEcUsers.add(u);
    }

    /**
     * Delete associated ec user.
     *
     * @param u
     *            the u
     */
    @Override
    public synchronized void deleteAssociatedEcUser(final EcUser u) {
        deletedAssociatedEcUsers.add(u);
    }

    /**
     * Adds the host.
     *
     * @param h
     *            the h
     */
    @Override
    public synchronized void addHost(final Host h) {
        addedHosts.add(h);
    }

    /**
     * Adds the incoming policy.
     *
     * @param p
     *            the p
     */
    @Override
    public synchronized void addIncomingPolicy(final IncomingPolicy p) {
        addedIncomingPolicies.add(p);
    }

    /**
     * Delete host.
     *
     * @param h
     *            the h
     */
    @Override
    public synchronized void deleteHost(final Host h) {
        deletedHosts.add(h);
    }

    /**
     * Delete incoming policy.
     *
     * @param p
     *            the p
     */
    @Override
    public synchronized void deleteIncomingPolicy(final IncomingPolicy p) {
        deletedIncomingPolicies.add(p);
    }

    /**
     * Adds the alias.
     *
     * @param d
     *            the d
     */
    @Override
    public synchronized void addAlias(final Destination d) {
        addedAliases.add(d);
    }

    /**
     * Delete alias.
     *
     * @param d
     *            the d
     */
    @Override
    public synchronized void deleteAlias(final Destination d) {
        deletedAliases.add(d);
    }

    /**
     * Delete metadata file.
     *
     * @param fileName
     *            the file name
     */
    @Override
    public void deleteMetadataFile(final String fileName) {
        deletedMetadataFiles.add(fileName);
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    @Override
    public String getFilterName() {
        return destination.getFilterName();
    }

    /**
     * Sets the filter name.
     *
     * @param param
     *            the new filter name
     */
    @Override
    public void setFilterName(final String param) {
        destination.setFilterName(param);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    @Override
    public String getComment() {
        return destination.getComment();
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    @Override
    public void setComment(final String param) {
        destination.setComment(param);
    }

    /**
     * Checks if is dirty.
     *
     * @return true, if is dirty
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
     * Gets the max connections.
     *
     * @return the max connections
     */
    @Override
    public int getMaxConnections() {
        return destination.getMaxConnections();
    }

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    @Override
    public int getRetryCount() {
        return destination.getRetryCount();
    }

    /**
     * Gets the retry frequency.
     *
     * @return the retry frequency
     */
    @Override
    public int getRetryFrequency() {
        return destination.getRetryFrequency();
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
     * Gets the formatted status.
     *
     * @return the formatted status
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
     * Gets the last transfer.
     *
     * @return the last transfer
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
     * Gets the last error.
     *
     * @return the last error
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
     * Gets the queue size.
     *
     * @return the queue size
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
     * Restart.
     *
     * @param graceful
     *            the graceful
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
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
     * Put on hold.
     *
     * @param graceful
     *            the graceful
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
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
     * Clean.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
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
     * Clean expired.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
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
     * Sets the max connections.
     *
     * @param i
     *            the new max connections
     */
    @Override
    public void setMaxConnections(final int i) {
        destination.setMaxConnections(i);
    }

    /**
     * Sets the retry count.
     *
     * @param i
     *            the new retry count
     */
    @Override
    public void setRetryCount(final int i) {
        destination.setRetryCount(i);
    }

    /**
     * Sets the retry frequency.
     *
     * @param i
     *            the new retry frequency
     */
    @Override
    public void setRetryFrequency(final int i) {
        destination.setRetryFrequency(i);
    }

    /**
     * Sets the type.
     *
     * @param i
     *            the new type
     */
    @Override
    public void setType(final int i) {
        destination.setType(i);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @Override
    public int getType() {
        return destination.getType();
    }

    /**
     * Gets the type text.
     *
     * @return the type text
     */
    @Override
    public String getTypeText() {
        return DestinationOption.getLabel(destination.getType());
    }

    /**
     * Gets the status map for products.
     *
     * @return the status map for products
     */
    @Override
    public Map<String, DestinationProductStatus> getStatusMapForProducts() {
        return statusMap;
    }

    /**
     * Gets the monitoring status.
     *
     * @return the monitoring status
     *
     * @throws TransferException
     *             the transfer exception
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
     * Gets the band width.
     *
     * @return the band width
     *
     * @throws TransferException
     *             the transfer exception
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
     * Gets the max requeue.
     *
     * @return the max requeue
     */
    @Override
    public int getMaxRequeue() {
        return destination.getMaxRequeue();
    }

    /**
     * Gets the bad data transfers.
     *
     * @return the bad data transfers
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Collection<DataTransfer> getBadDataTransfers() throws TransferException {
        return DataTransferHome.findBadByDestination(this);
    }

    /**
     * Gets the bad data transfers size.
     *
     * @return the bad data transfers size
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public int getBadDataTransfersSize() throws TransferException {
        return DataTransferHome.findBadCountByDestination(this);
    }

    /**
     * Gets the stop if dirty.
     *
     * @return the stop if dirty
     */
    @Override
    public boolean getStopIfDirty() {
        return destination.getStopIfDirty();
    }

    /**
     * Sets the stop if dirty.
     *
     * @param b
     *            the new stop if dirty
     */
    @Override
    public void setStopIfDirty(final boolean b) {
        destination.setStopIfDirty(b);
    }

    /**
     * Gets the max pending.
     *
     * @return the max pending
     */
    @Override
    public int getMaxPending() {
        return destination.getMaxPending();
    }

    /**
     * Gets the max file size.
     *
     * @return the max file size
     */
    @Override
    public long getMaxFileSize() {
        return destination.getMaxFileSize();
    }

    /**
     * Sets the max pending.
     *
     * @param param
     *            the new max pending
     */
    @Override
    public void setMaxPending(final int param) {
        destination.setMaxPending(param);
    }

    /**
     * Gets the show in monitors.
     *
     * @return the show in monitors
     */
    @Override
    public boolean getShowInMonitors() {
        return destination.getMonitor();
    }

    /**
     * Sets the show in monitors.
     *
     * @param b
     *            the new show in monitors
     */
    @Override
    public void setShowInMonitors(final boolean b) {
        destination.setMonitor(b);
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     *
     * @throws TransferException
     *             the transfer exception
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
     * Gets the transfer content.
     *
     * @param t
     *            the t
     * @param u
     *            the u
     *
     * @return the transfer content
     *
     * @throws TransferException
     *             the transfer exception
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
            throw new TransferException("Problem with destination '" + name + "' getting download for transfer " + t,
                    e);
        }
    }

    /**
     * Gets the all types.
     *
     * @return the all types
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
     * Gets the completions.
     *
     * @return the completions
     *
     * @throws TransferException
     *             the transfer exception
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
     * Equals.
     *
     * @param o
     *            the o
     *
     * @return true, if successful
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
        return getClass().getName() + " { " + destination + " }";
    }
}
