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

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Form bean for Destination. Used to Insert/Update/Delete destinations.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamManager;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferGroupHome;
import ecmwf.ecpds.master.plugin.http.home.ecuser.EcUserHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.CountryHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUserException;
import ecmwf.ecpds.master.plugin.http.model.transfer.Alias;
import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.DestinationMetaData;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicyException;
import ecmwf.ecpds.master.plugin.http.model.transfer.Traffic;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class DestinationActionForm.
 */
public class DestinationActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6716935024591378992L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DestinationActionForm.class);

    /** The host list. */
    private Collection<Host> hostList;

    /** The hosts and priorities. */
    private Collection<Pair> hostsAndPriorities;

    /** The ecuser list. */
    private Collection<EcUser> ecuserList;

    /** The names and comments. */
    private Collection<Pair> namesAndComments;

    /** The id. */
    private String id = "";

    /** The name. */
    private String name = "";

    /** The master. */
    private String master = "";

    /** The comment. */
    private String comment = "";

    /** The on host failure. */
    private String onHostFailure = "";

    /** The keep in spool. */
    private String keepInSpool = "";

    /** The if target exist. */
    private String ifTargetExist = "";

    /** The user mail. */
    private String userMail = "";

    /** The filter name. */
    private String filterName = "";

    /** The properties. */
    private String properties = "";

    /** The javascript. */
    private String javascript = "";

    /** The max connections. */
    private String maxConnections = Cnf.at("DestinationActionForm", "maxConnections", "1");

    /** The retry count. */
    private String retryCount = Cnf.at("DestinationActionForm", "retryCount", "-1");

    /** The retry frequency. */
    private String retryFrequency = Cnf.at("DestinationActionForm", "retryFrequency", "30000");

    /** The reset frequency. */
    private String resetFrequency = Cnf.at("DestinationActionForm", "resetFrequency", "1200000");

    /** The max file size. */
    private String maxFileSize = Cnf.at("DestinationActionForm", "maxFileSize", "-1");

    /** The max inactivity. */
    private String maxInactivity = Cnf.at("DestinationActionForm", "maxInactivity", "0");

    /** The start count. */
    private String startCount = Cnf.at("DestinationActionForm", "startCount", "0");

    /** The start frequency. */
    private String startFrequency = Cnf.at("DestinationActionForm", "startFrequency", "600000");

    /** The max start. */
    private String maxStart = Cnf.at("DestinationActionForm", "maxStart", "2");

    /** The max requeue. */
    private String maxRequeue = Cnf.at("DestinationActionForm", "maxRequeue", "-1");

    /** The max pending. */
    private String maxPending = Cnf.at("DestinationActionForm", "maxPending", "8000");

    /** The mail on start. */
    private String mailOnStart = "off";

    /** The mail on end. */
    private String mailOnEnd = "off";

    /** The mail on error. */
    private String mailOnError = "off";

    /** The active. */
    private String active = "off";

    /** The backup. */
    private String backup = "off";

    /** The acquisition. */
    private String acquisition = "off";

    /** The stop if dirty. */
    private String stopIfDirty = "off";

    /** The show in monitors. */
    private String showInMonitors = "off";

    /** The group by date. */
    private String groupByDate = "off";

    /** The date format. */
    private String dateFormat = Cnf.at("DestinationActionForm", "dateFormat", "yyyyMMdd");

    /** The ec user name. */
    private String ecUserName = "";

    /** The transfer group. */
    private String transferGroup = "";

    /** The country iso. */
    private String countryIso = "";

    /** The type. */
    private String type = "0";

    /** The host for source name. */
    private String hostForSourceName = Cnf.at("DestinationActionForm", "hostForSourceName", "");

    /** The action requested. */
    private String actionRequested = "create";

    /** The source destination. */
    private String sourceDestination = "";

    /** The from destination. */
    private String fromDestination = "";

    /** The to destination. */
    private String toDestination = "";

    /** The copy source shared host. */
    private String copySourceSharedHost = "off";

    /** The copy shared host. */
    private String copySharedHost = "off";

    /** The label. */
    private String label = "";

    /** The destination. */
    private Destination destination;

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     */
    public String getTransferGroup() {
        return transferGroup;
    }

    /**
     * Sets the transfer group.
     *
     * @param tg
     *            the new transfer group
     */
    public void setTransferGroup(final String tg) {
        transferGroup = tg;
    }

    /**
     * Gets the action requested.
     *
     * @return the action requested
     */
    public String getActionRequested() {
        return actionRequested;
    }

    /**
     * Gets the from destination.
     *
     * @return the from destination
     */
    public String getFromDestination() {
        return fromDestination;
    }

    /**
     * Gets the source destination.
     *
     * @return the source destination
     */
    public String getSourceDestination() {
        return sourceDestination;
    }

    /**
     * Gets the to destination.
     *
     * @return the to destination
     */
    public String getToDestination() {
        return toDestination;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the action requested.
     *
     * @param param
     *            the new action requested
     */
    public void setActionRequested(final String param) {
        actionRequested = param;
    }

    /**
     * Sets the from destination.
     *
     * @param param
     *            the new from destination
     */
    public void setFromDestination(final String param) {
        fromDestination = param;
    }

    /**
     * Sets the source destination.
     *
     * @param param
     *            the new source destination
     */
    public void setSourceDestination(final String param) {
        sourceDestination = param;
    }

    /**
     * Sets the to destination.
     *
     * @param param
     *            the new to destination
     */
    public void setToDestination(final String param) {
        toDestination = param;
    }

    /**
     * Sets the label.
     *
     * @param param
     *            the new label
     */
    public void setLabel(final String param) {
        label = param;
    }

    /**
     * Sets the default values.
     *
     * @param u
     *            the new default values
     */
    public void setDefaultValues(final User u) {
        if (isNotEmpty(ecUserName = u.getId())) {
            userMail = ecUserName + "@ecmwf.int";
        }
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public String getActive() {
        return active;
    }

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    public String getBackup() {
        return backup;
    }

    /**
     * Gets the acquisition.
     *
     * @return the acquisition
     */
    public String getAcquisition() {
        return acquisition;
    }

    /**
     * Gets the stop if dirty.
     *
     * @return the stop if dirty
     */
    public String getStopIfDirty() {
        return stopIfDirty;
    }

    /**
     * Gets the country iso.
     *
     * @return the country iso
     */
    public String getCountryIso() {
        return countryIso;
    }

    /**
     * Gets the host for source name.
     *
     * @return the host for source name
     */
    public String getHostForSourceName() {
        return hostForSourceName;
    }

    /**
     * Gets the ec user name.
     *
     * @return the ec user name
     */
    public String getEcUserName() {
        return ecUserName;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the if target exist.
     *
     * @return the if target exist
     */
    public String getIfTargetExist() {
        return ifTargetExist;
    }

    /**
     * Gets the on host failure.
     *
     * @return the on host failure
     */
    public String getOnHostFailure() {
        return onHostFailure;
    }

    /**
     * Gets the keep in spool.
     *
     * @return the keep in spool
     */
    public String getKeepInSpool() {
        return keepInSpool;
    }

    /**
     * Gets the mail on end.
     *
     * @return the mail on end
     */
    public String getMailOnEnd() {
        return mailOnEnd;
    }

    /**
     * Gets the mail on error.
     *
     * @return the mail on error
     */
    public String getMailOnError() {
        return mailOnError;
    }

    /**
     * Gets the mail on start.
     *
     * @return the mail on start
     */
    public String getMailOnStart() {
        return mailOnStart;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the master.
     *
     * @return the master
     */
    public String getMaster() {
        return master;
    }

    /**
     * Gets the user mail.
     *
     * @return the user mail
     */
    public String getUserMail() {
        return userMail;
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Gets the properties.
     *
     * @param data
     *            the data
     *
     * @return the properties
     */
    private String getProperties(final String data) {
        final var pos = data.indexOf(ECtransSetup.SEPARATOR);
        return (pos >= 0 ? data.substring(0, pos) : data).trim();
    }

    /**
     * Gets the javascript.
     *
     * @return the javascript
     */
    public String getJavascript() {
        return javascript;
    }

    /**
     * Gets the javascript.
     *
     * @param data
     *            the data
     *
     * @return the javascript
     */
    private String getJavascript(final String data) {
        final var pos = data.indexOf(ECtransSetup.SEPARATOR);
        return pos >= 0 ? data.substring(pos + ECtransSetup.SEPARATOR.length()).trim() : "";
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the max start.
     *
     * @return the max start
     */
    public String getMaxStart() {
        return maxStart;
    }

    /**
     * Gets the max requeue.
     *
     * @return the max requeue
     */
    public String getMaxRequeue() {
        return maxRequeue;
    }

    /**
     * Gets the start frequency.
     *
     * @return the start frequency
     */
    public String getStartFrequency() {
        return startFrequency;
    }

    /**
     * Gets the start count.
     *
     * @return the start count
     */
    public String getStartCount() {
        return startCount;
    }

    /**
     * Gets the reset frequency.
     *
     * @return the reset frequency
     */
    public String getResetFrequency() {
        return resetFrequency;
    }

    /**
     * Gets the max inactivity.
     *
     * @return the max inactivity
     */
    public String getMaxInactivity() {
        return maxInactivity;
    }

    /**
     * Sets the active.
     *
     * @param string
     *            the new active
     */
    public void setActive(final String string) {
        active = string;
    }

    /**
     * Sets the backup.
     *
     * @param string
     *            the new backup
     */
    public void setBackup(final String string) {
        backup = string;
    }

    /**
     * Sets the acquisition.
     *
     * @param string
     *            the new acquisition
     */
    public void setAcquisition(final String string) {
        acquisition = string;
    }

    /**
     * Sets the stop if dirty.
     *
     * @param s
     *            the new stop if dirty
     */
    public void setStopIfDirty(final String s) {
        stopIfDirty = s;
    }

    /**
     * Sets the country iso.
     *
     * @param string
     *            the new country iso
     */
    public void setCountryIso(final String string) {
        countryIso = string;
    }

    /**
     * Sets the host for source name.
     *
     * @param string
     *            the new host for source name
     */
    public void setHostForSourceName(final String string) {
        hostForSourceName = string;
    }

    /**
     * Sets the ec user name.
     *
     * @param string
     *            the new ec user name
     */
    public void setEcUserName(final String string) {
        ecUserName = string;
    }

    /**
     * Sets the id.
     *
     * @param string
     *            the new id
     */
    public void setId(final String string) {
        id = string;
    }

    /**
     * Sets the if target exist.
     *
     * @param string
     *            the new if target exist
     */
    public void setIfTargetExist(final String string) {
        ifTargetExist = string;
    }

    /**
     * Sets the keep in spool.
     *
     * @param string
     *            the new keep in spool
     */
    public void setKeepInSpool(final String string) {
        keepInSpool = string;
    }

    /**
     * Sets the on host failure.
     *
     * @param string
     *            the new on host failure
     */
    public void setOnHostFailure(final String string) {
        onHostFailure = string;
    }

    /**
     * Sets the mail on end.
     *
     * @param string
     *            the new mail on end
     */
    public void setMailOnEnd(final String string) {
        mailOnEnd = string;
    }

    /**
     * Sets the mail on error.
     *
     * @param string
     *            the new mail on error
     */
    public void setMailOnError(final String string) {
        mailOnError = string;
    }

    /**
     * Sets the mail on start.
     *
     * @param string
     *            the new mail on start
     */
    public void setMailOnStart(final String string) {
        mailOnStart = string;
    }

    /**
     * Sets the name.
     *
     * @param string
     *            the new name
     */
    public void setName(final String string) {
        name = string;
    }

    /**
     * Sets the master.
     *
     * @param string
     *            the new master
     */
    public void setMaster(final String string) {
        master = string;
    }

    /**
     * Sets the user mail.
     *
     * @param string
     *            the new user mail
     */
    public void setUserMail(final String string) {
        userMail = string;
    }

    /**
     * Sets the filter name.
     *
     * @param string
     *            the new filter name
     */
    public void setFilterName(final String string) {
        filterName = string;
    }

    /**
     * Sets the properties.
     *
     * @param string
     *            the new properties
     */
    public void setProperties(final String string) {
        properties = string;
    }

    /**
     * Sets the javascript.
     *
     * @param string
     *            the new javascript
     */
    public void setJavascript(final String string) {
        javascript = string;
    }

    /**
     * Sets the comment.
     *
     * @param string
     *            the new comment
     */
    public void setComment(final String string) {
        comment = string;
    }

    /**
     * Sets the max start.
     *
     * @param string
     *            the new max start
     */
    public void setMaxStart(final String string) {
        maxStart = string;
    }

    /**
     * Sets the max pending.
     *
     * @param string
     *            the new max pending
     */
    public void setMaxPending(final String string) {
        maxPending = string;
    }

    /**
     * Sets the max file size.
     *
     * @param string
     *            the new max file size
     */
    public void setMaxFileSize(final String string) {
        maxFileSize = string;
    }

    /**
     * Sets the max requeue.
     *
     * @param string
     *            the new max requeue
     */
    public void setMaxRequeue(final String string) {
        maxRequeue = string;
    }

    /**
     * Sets the start frequency.
     *
     * @param string
     *            the new start frequency
     */
    public void setStartFrequency(final String string) {
        startFrequency = string;
    }

    /**
     * Sets the start count.
     *
     * @param string
     *            the new start count
     */
    public void setStartCount(final String string) {
        startCount = string;
    }

    /**
     * Sets the reset frequency.
     *
     * @param string
     *            the new reset frequency
     */
    public void setResetFrequency(final String string) {
        resetFrequency = string;
    }

    /**
     * Sets the max inactivity.
     *
     * @param string
     *            the new max inactivity
     */
    public void setMaxInactivity(final String string) {
        maxInactivity = string;
    }

    /**
     * Gets the master options.
     *
     * @return the master options
     */
    public Collection<Pair> getMasterOptions() {
        final Collection<Pair> options = new ArrayList<>();
        for (final String masterURL : Cnf.listAt("DestinationActionForm", "masterURLs")) {
            final var elements = masterURL.split("=");
            options.add(new Pair(elements[0], elements.length > 1 ? elements[1] : elements[0]));
        }
        return options;
    }

    /**
     * Gets the filter name options.
     *
     * @return the filter name options
     */
    public Collection<Pair> getFilterNameOptions() {
        final Collection<Pair> options = new ArrayList<>();
        for (final String mode : StreamManager.modes) {
            options.add(new Pair(mode, mode));
        }
        return options;
    }

    /**
     * Gets the ec user options.
     *
     * @return the ec user options
     */
    public synchronized Collection<EcUser> getEcUserOptions() {
        if (ecuserList == null) {
            try {
                ecuserList = EcUserHome.findAll();
            } catch (final EcUserException e) {
                log.error("Problem getting ECusers", e);
            }
        }
        return ecuserList != null ? ecuserList : new ArrayList<>();
    }

    /**
     * Gets the from destination options.
     *
     * @return the from destination options
     */
    public Collection<Pair> getFromDestinationOptions() {
        if (namesAndComments == null) {
            try {
                namesAndComments = DestinationHome.findAllNamesAndComments();
            } catch (final TransferException e) {
                log.error("Problem getting Destinations", e);
            }
        }
        return namesAndComments != null ? namesAndComments : new ArrayList<>();
    }

    /**
     * Gets the type options.
     *
     * @return the type options
     */
    public Collection<Pair> getTypeOptions() {
        final Collection<Pair> options = new ArrayList<>();
        for (final Map.Entry<Integer, String> entry : DestinationOption.getTypes(false)) {
            options.add(new Pair(String.valueOf(entry.getKey()), entry.getValue()));
        }
        return options;
    }

    /**
     * Gets the associated hosts and priorities.
     *
     * @param type
     *            the type
     *
     * @return All the hosts currently associated to the Destination
     */
    private Collection<Pair> _getAssociatedHostsAndPriorities(final String type) {
        final var allHostsForType = _getHostOptions(type);
        final Collection<Pair> selectedAssignedPairs = new ArrayList<>();
        for (final Pair pair : getHostsAndPriorities()) {
            final var host = (Host) pair.getName();
            if (allHostsForType.contains(host)) {
                selectedAssignedPairs.add(pair);
            }
        }
        return selectedAssignedPairs;
    }

    /**
     * Gets the dissemination hosts and priorities.
     *
     * @return the dissemination hosts and priorities
     */
    public Collection<Pair> getDisseminationHostsAndPriorities() {
        return _getAssociatedHostsAndPriorities(HostOption.DISSEMINATION);
    }

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    public String getCompletions() {
        final var sb = new StringBuilder();
        return sb
                .append(ECtransOptions.toString(ECtransOptions.DESTINATION_ALIAS_PATTERN,
                        getAliases().stream().map(Destination::getName).toList()))
                .append(sb.length() > 0 ? ",\n" : "").append(ECtransOptions.toString(ECtransGroups.DESTINATION))
                .toString();
    }

    /**
     * Gets the acquisition hosts and priorities.
     *
     * @return the acquisition hosts and priorities
     */
    public Collection<Pair> getAcquisitionHostsAndPriorities() {
        return _getAssociatedHostsAndPriorities(HostOption.ACQUISITION);
    }

    /**
     * Gets the proxy hosts and priorities.
     *
     * @return the proxy hosts and priorities
     */
    public Collection<Pair> getProxyHostsAndPriorities() {
        return _getAssociatedHostsAndPriorities(HostOption.PROXY);
    }

    /**
     * Gets the hosts and priorities.
     *
     * @return the hosts and priorities
     */
    public synchronized Collection<Pair> getHostsAndPriorities() {
        if (hostsAndPriorities == null) {
            try {
                hostsAndPriorities = destination.getHostsAndPriorities();
            } catch (final TransferException e) {
                log.error("Problem getting Hosts and priorities", e);
            }
        }
        return hostsAndPriorities != null ? hostsAndPriorities : new ArrayList<>();
    }

    /**
     * Gets the not associated host options.
     *
     * @param type
     *            the type
     *
     * @return All the hosts NOT currently associated to the Destination
     */
    private Collection<Host> _getNotAssociatedHostOptions(final String type) {
        final var allHostsForType = _getHostOptions(type);
        final var allAssignedPairs = getHostsAndPriorities();
        final Collection<Host> assigned = new ArrayList<>(allAssignedPairs.size());
        for (final Pair pair : allAssignedPairs) {
            assigned.add((Host) pair.getName());
        }
        allHostsForType.removeAll(assigned);
        return allHostsForType;
    }

    /**
     * Gets the dissemination host options.
     *
     * @return the dissemination host options
     */
    public Collection<Host> getDisseminationHostOptions() {
        return _getNotAssociatedHostOptions(HostOption.DISSEMINATION);
    }

    /**
     * Gets the acquisition host options.
     *
     * @return the acquisition host options
     */
    public Collection<Host> getAcquisitionHostOptions() {
        return _getNotAssociatedHostOptions(HostOption.ACQUISITION);
    }

    /**
     * Gets the proxy host options.
     *
     * @return the proxy host options
     */
    public Collection<Host> getProxyHostOptions() {
        return _getNotAssociatedHostOptions(HostOption.PROXY);
    }

    /**
     * Gets the associated ec users.
     *
     * @return All the ECUSERS currently associated to the Destination
     */
    public Collection<EcUser> getAssociatedEcUsers() {
        try {
            return destination.getAssociatedEcUsers();
        } catch (final TransferException e) {
            log.error("Problem getting EcUsers", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the associated incoming policies.
     *
     * @return All the IncomingPolicies currently associated to the Destination
     */
    public Collection<IncomingPolicy> getAssociatedIncomingPolicies() {
        try {
            return destination.getAssociatedIncomingPolicies();
        } catch (final TransferException e) {
            log.error("Problem getting IncomingPolicies", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the aliases.
     *
     * @return All the DESTINATIONS currently ALIAS for the Destination
     */
    public Collection<Destination> getAliases() {
        try {
            return destination.getAliases();
        } catch (final TransferException e) {
            log.error("Problem getting Aliases", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the alias list.
     *
     * @return the alias list
     */
    public Collection<Alias> getAliasList() {
        try {
            return destination.getAliasList();
        } catch (final TransferException e) {
            log.error("Problem getting AliasList", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the traffic list.
     *
     * @return the traffic list
     */
    public Collection<Traffic> getTrafficList() {
        try {
            return destination.getTrafficList();
        } catch (final TransferException e) {
            log.error("Problem getting Traffic", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the associated ec user options.
     *
     * @return All the ECUSERS NOT currently associated to the Destination
     */
    public Collection<EcUser> getAssociatedEcUserOptions() {
        try {
            final var all = EcUserHome.findAll();
            all.removeAll(getAssociatedEcUsers());
            return all;
        } catch (final EcUserException e) {
            log.error("Problem getting EcUser options", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the associated incoming policies options.
     *
     * @return All the IncomingPolicies NOT currently associated to the Destination
     */
    public Collection<IncomingPolicy> getAssociatedIncomingPoliciesOptions() {
        try {
            final var all = IncomingPolicyHome.findAll();
            all.removeAll(getAssociatedIncomingPolicies());
            return all;
        } catch (final IncomingPolicyException e) {
            log.error("Problem getting IncomingPolicy options", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the metadata files.
     *
     * @return the metadata files
     */
    public Collection<DestinationMetaData> getMetadataFiles() {
        try {
            return destination.getMetaData();
        } catch (final TransferException e) {
            log.error("Problem getting Alias options", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the alias options.
     *
     * @return All the destinations not currently aliased for this Destination
     */
    public Collection<Pair> getAliasOptions() {
        try {
            final var toRemove = getAliases();
            toRemove.add(destination);
            return Util.getDestinationPairList(DestinationHome.findAllNamesAndComments(), toRemove);
        } catch (final TransferException e) {
            log.error("Problem getting Alias options", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the country options.
     *
     * @return the country options
     */
    public Collection<Country> getCountryOptions() {
        try {
            return CountryHome.findAll();
        } catch (final TransferException e) {
            log.error("Problem getting Countries", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the host options.
     *
     * @param type
     *            the type
     *
     * @return the collection
     */
    private synchronized Collection<Host> _getHostOptions(final String type) {
        final List<Host> hosts = new ArrayList<>();
        try {
            if (hostList == null) {
                hostList = HostHome.findAll();
            }
            for (final Host host : hostList) {
                if (type.equals(host.getType())) {
                    hosts.add(host);
                }
            }
        } catch (final TransferException e) {
            log.error("Error getting Hosts options", e);
        }
        return hosts;
    }

    /**
     * Gets the host for source options.
     *
     * @return the host for source options
     */
    public Collection<Host> getHostForSourceOptions() {
        return _getHostOptions(HostOption.SOURCE);
    }

    /**
     * Gets the host for acquisition options.
     *
     * @return the host for acquisition options
     */
    public Collection<Host> getHostForAcquisitionOptions() {
        return _getHostOptions(HostOption.ACQUISITION);
    }

    /**
     * Gets the host for proxy options.
     *
     * @return the host for proxy options
     */
    public Collection<Host> getHostForProxyOptions() {
        return _getHostOptions(HostOption.PROXY);
    }

    /**
     * Gets the on host failure options.
     *
     * @return the on host failure options
     */
    public Collection<Pair> getOnHostFailureOptions() {
        try {
            final var opts = DestinationOption.onHostFailure;
            final List<Pair> options = new ArrayList<>(opts.size());
            for (var i = 0; i < opts.size(); i++) {
                options.add(new Pair(opts.get(i), Integer.toString(i)));
            }
            return options;
        } catch (final Exception e) {
            log.error("Problem getting OnHostFailureOptions", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the keep in spool options.
     *
     * @return the keep in spool options
     */
    public Collection<Pair> getKeepInSpoolOptions() {
        try {
            final var opts = DestinationOption.deleteFromSpool;
            final List<Pair> options = new ArrayList<>(opts.size());
            for (var i = 0; i < opts.size(); i++) {
                options.add(new Pair(opts.get(i), String.valueOf(i)));
            }
            return options;
        } catch (final Exception e) {
            log.error("Problem getting KeepInSpoolOptions", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the if target exist options.
     *
     * @return the if target exist options
     */
    public Collection<Pair> getIfTargetExistOptions() {
        try {
            final var opts = DestinationOption.ifTargetExist;
            final List<Pair> options = new ArrayList<>(opts.size());
            for (var i = 0; i < opts.size(); i++) {
                options.add(new Pair(opts.get(i), Integer.toString(i)));
            }
            return options;
        } catch (final Exception e) {
            log.error("Problem getting IfTargetExistOptions", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the max connections.
     *
     * @return the max connections
     */
    public String getMaxConnections() {
        return maxConnections;
    }

    /**
     * Gets the max pending.
     *
     * @return the max pending
     */
    public String getMaxPending() {
        return maxPending;
    }

    /**
     * Gets the max file size.
     *
     * @return the max file size
     */
    public String getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    public String getRetryCount() {
        return retryCount;
    }

    /**
     * Gets the retry frequency.
     *
     * @return the retry frequency
     */
    public String getRetryFrequency() {
        return retryFrequency;
    }

    /**
     * Sets the max connections.
     *
     * @param string
     *            the new max connections
     */
    public void setMaxConnections(final String string) {
        maxConnections = string;
    }

    /**
     * Sets the retry count.
     *
     * @param string
     *            the new retry count
     */
    public void setRetryCount(final String string) {
        retryCount = string;
    }

    /**
     * Sets the retry frequency.
     *
     * @param string
     *            the new retry frequency
     */
    public void setRetryFrequency(final String string) {
        retryFrequency = string;
    }

    /**
     * Sets the show in monitors.
     *
     * @param s
     *            the new show in monitors
     */
    public void setShowInMonitors(final String s) {
        showInMonitors = s;
    }

    /**
     * Gets the show in monitors.
     *
     * @return the show in monitors
     */
    public String getShowInMonitors() {
        return showInMonitors;
    }

    /**
     * Gets the group by date.
     *
     * @return the group by date
     */
    public String getGroupByDate() {
        return groupByDate;
    }

    /**
     * Sets the group by date.
     *
     * @param s
     *            the new group by date
     */
    public void setGroupByDate(final String s) {
        groupByDate = s;
    }

    /**
     * Gets the date format.
     *
     * @return the date format
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the date format.
     *
     * @param format
     *            the new date format
     */
    public void setDateFormat(final String format) {
        dateFormat = format;
    }

    /**
     * Sets the copy shared host.
     *
     * @param s
     *            the new copy shared host
     */
    public void setCopySharedHost(final String s) {
        copySharedHost = s;
    }

    /**
     * Gets the copy shared host.
     *
     * @return the copy shared host
     */
    public String getCopySharedHost() {
        return copySharedHost;
    }

    /**
     * Sets the copy source shared host.
     *
     * @param s
     *            the new copy source shared host
     */
    public void setCopySourceSharedHost(final String s) {
        copySourceSharedHost = s;
    }

    /**
     * Gets the copy source shared host.
     *
     * @return the copy source shared host
     */
    public String getCopySourceSharedHost() {
        return copySourceSharedHost;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param t
     *            the new type
     */
    public void setType(final String t) {
        type = t;
    }

    /**
     * Gets the transfer group options.
     *
     * @return the transfer group options
     */
    public Collection<TransferGroup> getTransferGroupOptions() {
        try {
            return TransferGroupHome.findAll();
        } catch (final DataFileException e) {
            log.error("Problem getting TransferGroups", e);
            return new ArrayList<>();
        }
    }

    /**
     * Populate destination.
     *
     * @param destination
     *            the destination
     */
    protected void populateDestination(final Destination destination) {
        destination.setId(id);
        destination.setName(name);
        destination.setOnHostFailure(convertToInt(onHostFailure));
        destination.setIfTargetExist(convertToInt(ifTargetExist));
        destination.setKeepInSpool(convertToInt(keepInSpool));
        destination.setTransferGroupName(transferGroup);
        destination.setMaxConnections(convertToInt(maxConnections));
        destination.setRetryCount(convertToInt(retryCount));
        destination.setRetryFrequency(convertToInt(retryFrequency));
        destination.setMaxPending(convertToInt(maxPending));
        destination.setFilterName(filterName);
        destination.setUserMail(userMail);
        destination.setMailOnStart(convertToBoolean(mailOnStart));
        destination.setMailOnEnd(convertToBoolean(mailOnEnd));
        destination.setMailOnError(convertToBoolean(mailOnError));
        destination.setActive(convertToBoolean(active));
        destination.setBackup(convertToBoolean(backup));
        destination.setAcquisition(convertToBoolean(acquisition));
        destination.setStopIfDirty(convertToBoolean(stopIfDirty));
        destination.setShowInMonitors(convertToBoolean(showInMonitors));
        destination.setGroupByDate(convertToBoolean(groupByDate));
        destination.setData(properties + "\n" + ECtransSetup.SEPARATOR + javascript);
        destination.setDateFormat(dateFormat);
        destination.setEcUserName(ecUserName);
        destination.setCountryIso(countryIso);
        destination.setHostForSourceName(hostForSourceName);
        destination.setComment(comment);
        destination.setMaxStart(convertToInt(maxStart));
        destination.setMaxRequeue(convertToInt(maxRequeue));
        destination.setStartFrequency(convertToInt(startFrequency));
        destination.setMaxInactivity(convertToInt(maxInactivity));
        destination.setMaxFileSize(convertToLong(maxFileSize));
        destination.setResetFrequency(convertToLong(resetFrequency));
        destination.setType(convertToInt(type));
    }

    /**
     * Populate from destination.
     *
     * @param destination
     *            the destination
     */
    protected void populateFromDestination(final Destination destination) {
        this.destination = destination;
        setId(destination.getId());
        setName(destination.getName());
        setKeepInSpool(Integer.toString(destination.getKeepInSpool()));
        setOnHostFailure(Integer.toString(destination.getOnHostFailure()));
        setIfTargetExist(Integer.toString(destination.getIfTargetExist()));
        setMaxConnections(Integer.toString(destination.getMaxConnections()));
        setRetryCount(Integer.toString(destination.getRetryCount()));
        setRetryFrequency(Integer.toString(destination.getRetryFrequency()));
        setMaxPending(Integer.toString(destination.getMaxPending()));
        setFilterName(destination.getFilterName());
        setUserMail(destination.getUserMail());
        setMailOnStart(destination.getMailOnStart() ? "on" : "off");
        setMailOnEnd(destination.getMailOnEnd() ? "on" : "off");
        setMailOnError(destination.getMailOnError() ? "on" : "off");
        setActive(destination.getActive() ? "on" : "off");
        setBackup(destination.getBackup() ? "on" : "off");
        setAcquisition(destination.getAcquisition() ? "on" : "off");
        setStopIfDirty(destination.getStopIfDirty() ? "on" : "off");
        setShowInMonitors(destination.getShowInMonitors() ? "on" : "off");
        setGroupByDate(destination.getGroupByDate() ? "on" : "off");
        setTransferGroup(destination.getTransferGroupName());
        setProperties(getProperties(destination.getData()));
        setJavascript(getJavascript(destination.getData()));
        setDateFormat(destination.getDateFormat());
        setEcUserName(destination.getEcUserName());
        setCountryIso(destination.getCountryIso());
        setHostForSourceName(destination.getHostForSourceName());
        setComment(destination.getComment());
        setMaxStart(Integer.toString(destination.getMaxStart()));
        setMaxRequeue(Integer.toString(destination.getMaxRequeue()));
        setStartFrequency(Long.toString(destination.getStartFrequency()));
        setResetFrequency(Long.toString(destination.getResetFrequency()));
        setMaxFileSize(Long.toString(destination.getMaxFileSize()));
        setMaxInactivity(Integer.toString(destination.getMaxInactivity()));
        setType(Integer.toString(destination.getType()));
    }
}
