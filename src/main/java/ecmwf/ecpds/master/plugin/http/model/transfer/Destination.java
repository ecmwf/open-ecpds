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

package ecmwf.ecpds.master.plugin.http.model.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * A Destination, as it is going to be seen from Controller and View for the web
 * application
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationStatus;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;
import ecmwf.web.services.content.Content;
import ecmwf.web.util.bean.Pair;
import ecmwf.web.util.bean.StringPair;

/**
 * The Interface Destination.
 */
public interface Destination extends ModelBean {

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Gets the backup.
     *
     * @return the backup
     */
    boolean getBackup();

    /**
     * Gets the acquisition.
     *
     * @return the acquisition
     */
    boolean getAcquisition();

    /**
     * Gets the completions.
     *
     * @return the completions
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    String getCompletions() throws TransferException;

    /**
     * Gets the ec user.
     *
     * @return the ec user
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    EcUser getEcUser() throws TransferException;

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    TransferGroup getTransferGroup() throws DataFileException;

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    String getTransferGroupName();

    /**
     * Gets the data.
     *
     * @return the data
     */
    String getData();

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    String getProperties();

    /**
     * Gets the javascript.
     *
     * @return the javascript
     */
    String getJavascript();

    /**
     * Gets the data alias.
     *
     * @return the data alias
     */
    String getDataAlias();

    /**
     * Sets the data.
     *
     * @param string
     *            the new data
     */
    void setData(String string);

    /**
     * Gets the country.
     *
     * @return the country
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Country getCountry() throws TransferException;

    /**
     * Gets the ec user name.
     *
     * @return the ec user name
     */
    String getEcUserName();

    /**
     * Gets the country iso.
     *
     * @return the country iso
     */
    String getCountryIso();

    /**
     * Gets the host for source.
     *
     * @return the host for source
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Host getHostForSource() throws TransferException;

    /**
     * Gets the host for source name.
     *
     * @return the host for source name
     */
    String getHostForSourceName();

    /**
     * Gets the if target exist.
     *
     * @return the if target exist
     */
    int getIfTargetExist();

    /**
     * Gets the on host failure.
     *
     * @return the on host failure
     */
    int getOnHostFailure();

    /**
     * Gets the keep in spool.
     *
     * @return the keep in spool
     */
    int getKeepInSpool();

    /**
     * Gets the max start.
     *
     * @return the max start
     */
    int getMaxStart();

    /**
     * Gets the start frequency.
     *
     * @return the start frequency
     */
    int getStartFrequency();

    /**
     * Gets the formatted start frequency.
     *
     * @return the formatted start frequency
     */
    String getFormattedStartFrequency();

    /**
     * Gets the max requeue.
     *
     * @return the max requeue
     */
    int getMaxRequeue();

    /**
     * Gets the max pending.
     *
     * @return the max pending
     */
    int getMaxPending();

    /**
     * Gets the max file size.
     *
     * @return the max file size
     */
    long getMaxFileSize();

    /**
     * Gets the start time.
     *
     * @return the start time
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Date getStartTime() throws TransferException;

    /**
     * Gets the reset frequency.
     *
     * @return the reset frequency
     */
    long getResetFrequency();

    /**
     * Gets the max inactivity.
     *
     * @return the max inactivity
     */
    int getMaxInactivity();

    /**
     * Sets the max inactivity.
     *
     * @param i
     *            the new max inactivity
     */
    void setMaxInactivity(int i);

    /**
     * Gets the formatted max inactivity.
     *
     * @return the formatted max inactivity
     */
    String getFormattedMaxInactivity();

    /**
     * Gets the formatted reset frequency.
     *
     * @return the formatted reset frequency
     */
    String getFormattedResetFrequency();

    /**
     * Gets the if target exist text.
     *
     * @return the if target exist text
     */
    String getIfTargetExistText();

    /**
     * Gets the on host failure text.
     *
     * @return the on host failure text
     */
    String getOnHostFailureText();

    /**
     * Gets the keep in spool text.
     *
     * @return the keep in spool text
     */
    String getKeepInSpoolText();

    /**
     * Gets the mail on update.
     *
     * @return the mail on update
     */
    boolean getMailOnUpdate();

    /**
     * Gets the mail on end.
     *
     * @return the mail on end
     */
    boolean getMailOnEnd();

    /**
     * Gets the mail on error.
     *
     * @return the mail on error
     */
    boolean getMailOnError();

    /**
     * Gets the mail on start.
     *
     * @return the mail on start
     */
    boolean getMailOnStart();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the user mail.
     *
     * @return the user mail
     */
    String getUserMail();

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    String getFilterName();

    /**
     * Gets the group by date.
     *
     * @return the group by date
     */
    boolean getGroupByDate();

    /**
     * Sets the group by date.
     *
     * @param b
     *            the new group by date
     */
    void setGroupByDate(boolean b);

    /**
     * Gets the date format.
     *
     * @return the date format
     */
    String getDateFormat();

    /**
     * Sets the date format.
     *
     * @param format
     *            the new date format
     */
    void setDateFormat(String format);

    /**
     * Sets the transfer group.
     *
     * @param m
     *            the new transfer group
     */
    void setTransferGroup(TransferGroup m);

    /**
     * Sets the transfer group name.
     *
     * @param s
     *            the new transfer group name
     */
    void setTransferGroupName(String s);

    /**
     * Checks if is dirty.
     *
     * @return True if some changes to data related to the destination need a restart to be applied.
     */
    boolean isDirty();

    /**
     * Gets the stop if dirty.
     *
     * @return the stop if dirty
     */
    boolean getStopIfDirty();

    /**
     * Sets the stop if dirty.
     *
     * @param b
     *            the new stop if dirty
     */
    void setStopIfDirty(boolean b);

    /**
     * Gets the associated ec users.
     *
     * @return the associated ec users
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<EcUser> getAssociatedEcUsers() throws TransferException;

    /**
     * Gets the associated incoming policies.
     *
     * @return the associated incoming policies
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<IncomingPolicy> getAssociatedIncomingPolicies() throws TransferException;

    /**
     * Adds the associated ec user.
     *
     * @param u
     *            the u
     */
    void addAssociatedEcUser(EcUser u);

    /**
     * Delete associated ec user.
     *
     * @param u
     *            the u
     */
    void deleteAssociatedEcUser(EcUser u);

    /**
     * Gets the data transfers on date.
     *
     * @param d
     *            the d
     *
     * @return the data transfers on date
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<DataTransfer> getDataTransfersOnDate(Date d) throws TransferException;

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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<DataTransfer> getDataTransfersByProductAndTime(String product, String time) throws TransferException;

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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<DataTransfer> getDataTransfersByProductAndTimeOnDate(String product, String time, Date d)
            throws TransferException;

    /**
     * Gets the data transfers including retries on transmission date.
     *
     * @param d
     *            the d
     *
     * @return the data transfers including retries on transmission date
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<DataTransfer> getDataTransfersIncludingRetriesOnTransmissionDate(Date d) throws TransferException;

    /**
     * Gets the bad data transfers.
     *
     * @return the bad data transfers
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<DataTransfer> getBadDataTransfers() throws TransferException;

    /**
     * Gets the bad data transfers size.
     *
     * @return the bad data transfers size
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    int getBadDataTransfersSize() throws TransferException;

    /**
     * Gets the hosts and priorities.
     *
     * @return the hosts and priorities
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Pair> getHostsAndPriorities() throws TransferException;

    /**
     * Gets the dissemination hosts and priorities.
     *
     * @return the dissemination hosts and priorities
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Pair> getDisseminationHostsAndPriorities() throws TransferException;

    /**
     * Gets the acquisition hosts and priorities.
     *
     * @return the acquisition hosts and priorities
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Pair> getAcquisitionHostsAndPriorities() throws TransferException;

    /**
     * The Destinations that are aliases of this one.
     *
     * @return the aliases
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Destination> getAliases() throws TransferException;

    /**
     * Gets the alias list.
     *
     * @return the alias list
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Alias> getAliasList() throws TransferException;

    /**
     * Gets the traffic list.
     *
     * @return the traffic list
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Traffic> getTrafficList() throws TransferException;

    /**
     * Gets the change log list.
     *
     * @return the change log list
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<ChangeLog> getChangeLogList() throws TransferException;

    /**
     * This destination is an alias for this ones.
     *
     * @return the aliased from
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Destination> getAliasedFrom() throws TransferException;

    /**
     * Gets the meta data.
     *
     * @return the meta data
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<DestinationMetaData> getMetaData() throws TransferException;

    /**
     * Gets the max connections.
     *
     * @return the max connections
     */
    int getMaxConnections();

    /**
     * Gets the retry frequency.
     *
     * @return the retry frequency
     */
    int getRetryFrequency();

    /**
     * Gets the formatted retry frequency.
     *
     * @return the formatted retry frequency
     */
    String getFormattedRetryFrequency();

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    int getRetryCount();

    /**
     * Gets the band width.
     *
     * @return the band width
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    long getBandWidth() throws TransferException;

    /**
     * Gets the status map for products.
     *
     * @return the status map for products
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Map<String, DestinationProductStatus> getStatusMapForProducts() throws TransferException;

    /**
     * Gets the monitoring status.
     *
     * @return the monitoring status
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    DestinationStatus getMonitoringStatus() throws TransferException;

    /**
     * Gets the formatted status.
     *
     * @return the formatted status
     */
    String getFormattedStatus();

    /**
     * Gets the last transfer.
     *
     * @return the last transfer
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    DataTransfer getLastTransfer() throws TransferException;

    /**
     * Gets the last error.
     *
     * @return the last error
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    DataTransfer getLastError() throws TransferException;

    /**
     * Gets the queue size.
     *
     * @return the queue size
     */
    int getQueueSize();

    /**
     * Clean.
     *
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void clean(User u) throws TransferException;

    /**
     * Clean expired.
     *
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void cleanExpired(User u) throws TransferException;

    /**
     * Restart.
     *
     * @param graceful
     *            the graceful
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void restart(boolean graceful, User u) throws TransferException;

    /**
     * Put on hold.
     *
     * @param graceful
     *            the graceful
     * @param u
     *            the u
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    void putOnHold(boolean graceful, User u) throws TransferException;

    /**
     * Adds the host.
     *
     * @param h
     *            the h
     */
    void addHost(Host h);

    /**
     * Adds the incoming policy.
     *
     * @param h
     *            the h
     */
    void addIncomingPolicy(IncomingPolicy h);

    /**
     * Delete host.
     *
     * @param p
     *            the p
     */
    void deleteHost(Host p);

    /**
     * Delete incoming policy.
     *
     * @param p
     *            the p
     */
    void deleteIncomingPolicy(IncomingPolicy p);

    /**
     * Adds the alias.
     *
     * @param d
     *            the d
     */
    void addAlias(Destination d);

    /**
     * Delete alias.
     *
     * @param d
     *            the d
     */
    void deleteAlias(Destination d);

    /**
     * Delete metadata file.
     *
     * @param fileName
     *            the file name
     */
    void deleteMetadataFile(String fileName);

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    void setActive(boolean b);

    /**
     * Sets the backup.
     *
     * @param b
     *            the new backup
     */
    void setBackup(boolean b);

    /**
     * Sets the acquisition.
     *
     * @param b
     *            the new acquisition
     */
    void setAcquisition(boolean b);

    /**
     * Sets the ec user name.
     *
     * @param name
     *            the new ec user name
     */
    void setEcUserName(String name);

    /**
     * Sets the country iso.
     *
     * @param iso
     *            the new country iso
     */
    void setCountryIso(String iso);

    /**
     * Sets the host for source name.
     *
     * @param name
     *            the new host for source name
     */
    void setHostForSourceName(String name);

    /**
     * Sets the if target exist.
     *
     * @param i
     *            the new if target exist
     */
    void setIfTargetExist(int i);

    /**
     * Sets the on host failure.
     *
     * @param i
     *            the new on host failure
     */
    void setOnHostFailure(int i);

    /**
     * Sets the keep in spool.
     *
     * @param i
     *            the new keep in spool
     */
    void setKeepInSpool(int i);

    /**
     * Sets the mail on update.
     *
     * @param b
     *            the new mail on update
     */
    void setMailOnUpdate(boolean b);

    /**
     * Sets the mail on end.
     *
     * @param b
     *            the new mail on end
     */
    void setMailOnEnd(boolean b);

    /**
     * Sets the mail on error.
     *
     * @param b
     *            the new mail on error
     */
    void setMailOnError(boolean b);

    /**
     * Sets the mail on start.
     *
     * @param b
     *            the new mail on start
     */
    void setMailOnStart(boolean b);

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    void setName(String name);

    /**
     * Sets the user mail.
     *
     * @param mail
     *            the new user mail
     */
    void setUserMail(String mail);

    /**
     * Sets the comment.
     *
     * @param comment
     *            the new comment
     */
    void setComment(String comment);

    /**
     * Sets the filter name.
     *
     * @param filterName
     *            the new filter name
     */
    void setFilterName(String filterName);

    /**
     * Sets the max connections.
     *
     * @param i
     *            the new max connections
     */
    void setMaxConnections(int i);

    /**
     * Sets the retry frequency.
     *
     * @param i
     *            the new retry frequency
     */
    void setRetryFrequency(int i);

    /**
     * Sets the retry count.
     *
     * @param i
     *            the new retry count
     */
    void setRetryCount(int i);

    /**
     * Sets the max pending.
     *
     * @param i
     *            the new max pending
     */
    void setMaxPending(int i);

    /**
     * Sets the max file size.
     *
     * @param i
     *            the new max file size
     */
    void setMaxFileSize(long i);

    /**
     * Sets the max start.
     *
     * @param i
     *            the new max start
     */
    void setMaxStart(int i);

    /**
     * Sets the max requeue.
     *
     * @param i
     *            the new max requeue
     */
    void setMaxRequeue(int i);

    /**
     * Sets the start frequency.
     *
     * @param i
     *            the new start frequency
     */
    void setStartFrequency(int i);

    /**
     * Sets the reset frequency.
     *
     * @param l
     *            the new reset frequency
     */
    void setResetFrequency(long l);

    /**
     * Gets the show in monitors.
     *
     * @return the show in monitors
     */
    boolean getShowInMonitors();

    /**
     * Sets the show in monitors.
     *
     * @param b
     *            the new show in monitors
     */
    void setShowInMonitors(boolean b);

    /**
     * Gets the type.
     *
     * @return the type
     */
    int getType();

    /**
     * Sets the type.
     *
     * @param i
     *            the new type
     */
    void setType(int i);

    /**
     * Gets the type text.
     *
     * @return the type text
     */
    String getTypeText();

    /**
     * Gets the all types.
     *
     * @return the all types
     */
    List<StringPair> getAllTypes();

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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Content getTransferContent(DataTransfer t, User u) throws TransferException;
}
