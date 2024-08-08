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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import java.util.Date;
import java.util.List;

import ecmwf.common.callback.RemoteInputStream;
import ecmwf.ecpds.master.plugin.http.dao.transfer.DataTransferLightBean;
import ecmwf.ecpds.master.plugin.http.model.CollectionSizeBean;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUserException;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.StringPair;

/**
 * The Interface Host.
 */
public interface Host extends ModelBean, CollectionSizeBean {

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    void setUser(User user);

    /**
     * Gets the user.
     *
     * @return the user
     */
    User getUser();

    /**
     * Gets the nick name.
     *
     * @return the nick name
     */
    String getNickName();

    /**
     * Sets the nick name.
     *
     * @param n
     *            the new nick name
     */
    void setNickName(String n);

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    String getCompletions();

    /**
     * Gets the automatic location.
     *
     * @return the automatic location
     */
    boolean getAutomaticLocation();

    /**
     * Sets the automatic location.
     *
     * @param b
     *            the new automatic location
     */
    void setAutomaticLocation(boolean b);

    /**
     * Gets the latitude.
     *
     * @return the latitude
     */
    Double getLatitude();

    /**
     * Sets the latitude.
     *
     * @param f
     *            the new latitude
     */
    void setLatitude(Double f);

    /**
     * Gets the longitude.
     *
     * @return the longitude
     */
    Double getLongitude();

    /**
     * Sets the longitude.
     *
     * @param f
     *            the new longitude
     */
    void setLongitude(Double f);

    /**
     * Gets the type.
     *
     * @return the type
     */
    String getType();

    /**
     * Sets the type.
     *
     * @param n
     *            the new type
     */
    void setType(String n);

    /**
     * Gets the acquisition frequency.
     *
     * @return the acquisition frequency
     */
    long getAcquisitionFrequency();

    /**
     * Gets the formatted acquisition frequency.
     *
     * @return the formatted acquisition frequency
     */
    String getFormattedAcquisitionFrequency();

    /**
     * Sets the acquisition frequency.
     *
     * @param i
     *            the new acquisition frequency
     */
    void setAcquisitionFrequency(long i);

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    String getFilterName();

    /**
     * Sets the filter name.
     *
     * @param n
     *            the new filter name
     */
    void setFilterName(String n);

    /**
     * Gets the network name.
     *
     * @return the network name
     */
    String getNetworkName();

    /**
     * Sets the network name.
     *
     * @param s
     *            the new network name
     */
    void setNetworkName(String s);

    /**
     * Gets the all networks.
     *
     * @return the all networks
     */
    List<StringPair> getAllNetworks();

    /**
     * Gets the transfer module names.
     *
     * @return the transfer module names
     *
     * @throws TransferException
     *             the transfer exception
     */
    String getTransferModuleNames() throws TransferException;

    /**
     * Gets the network code.
     *
     * @return the network code
     */
    String getNetworkCode();

    /**
     * Sets the network code.
     *
     * @param s
     *            the new network code
     */
    void setNetworkCode(String s);

    /**
     * Gets the check time.
     *
     * @return the check time
     */
    Date getCheckTime();

    /**
     * Gets the check frequency.
     *
     * @return the check frequency
     */
    long getCheckFrequency();

    /**
     * Gets the formatted check frequency.
     *
     * @return the formatted check frequency
     */
    String getFormattedCheckFrequency();

    /**
     * Sets the check frequency.
     *
     * @param i
     *            the new check frequency
     */
    void setCheckFrequency(long i);

    /**
     * Gets the valid.
     *
     * @return the valid
     */
    boolean getValid();

    /**
     * Gets the check.
     *
     * @return the check
     */
    boolean getCheck();

    /**
     * Sets the check.
     *
     * @param b
     *            the new check
     */
    void setCheck(boolean b);

    /**
     * Gets the check filename.
     *
     * @return the check filename
     */
    String getCheckFilename();

    /**
     * Sets the check filename.
     *
     * @param s
     *            the new check filename
     */
    void setCheckFilename(String s);

    /**
     * Gets the mail on success.
     *
     * @return the mail on success
     */
    boolean getMailOnSuccess();

    /**
     * Sets the mail on success.
     *
     * @param b
     *            the new mail on success
     */
    void setMailOnSuccess(boolean b);

    /**
     * Gets the mail on error.
     *
     * @return the mail on error
     */
    boolean getMailOnError();

    /**
     * Sets the mail on error.
     *
     * @param b
     *            the new mail on error
     */
    void setMailOnError(boolean b);

    /**
     * Gets the notify once.
     *
     * @return the notify once
     */
    boolean getNotifyOnce();

    /**
     * Sets the notify once.
     *
     * @param b
     *            the new notify once
     */
    void setNotifyOnce(boolean b);

    /**
     * Gets the user mail.
     *
     * @return the user mail
     */
    String getUserMail();

    /**
     * Sets the user mail.
     *
     * @param s
     *            the new user mail
     */
    void setUserMail(String s);

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    void setActive(boolean b);

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Sets the comment.
     *
     * @param c
     *            the new comment
     */
    void setComment(String c);

    /**
     * Gets the connections.
     *
     * @return the connections
     */
    int getConnections();

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
     * Sets the data.
     *
     * @param s
     *            the new data
     */
    void setData(String s);

    /**
     * Gets the ec user.
     *
     * @return the ec user
     *
     * @throws EcUserException
     *             the ec user exception
     */
    EcUser getEcUser() throws EcUserException;

    /**
     * Gets the last output.
     *
     * @return the last output
     */
    String getLastOutput();

    /**
     * Gets the formatted last output.
     *
     * @return the formatted last output
     */
    String getFormattedLastOutput();

    /**
     * Gets the EC user name.
     *
     * @return the EC user name
     */
    String getECUserName();

    /**
     * Sets the EC user name.
     *
     * @param u
     *            the new EC user name
     */
    void setECUserName(String u);

    /**
     * Gets the allowed users.
     *
     * @return the allowed users
     *
     * @throws TransferException
     *             the transfer exception
     */
    Collection<EcUser> getAllowedUsers() throws TransferException;

    /**
     * Adds the allowed user.
     *
     * @param u
     *            the u
     */
    void addAllowedUser(EcUser u);

    /**
     * Delete allowed user.
     *
     * @param u
     *            the u
     */
    void deleteAllowedUser(EcUser u);

    /**
     * Gets the host.
     *
     * @return the host
     */
    String getHost();

    /**
     * Sets the host.
     *
     * @param s
     *            the new host
     */
    void setHost(String s);

    /**
     * Gets the login.
     *
     * @return the login
     */
    String getLogin();

    /**
     * Sets the login.
     *
     * @param s
     *            the new login
     */
    void setLogin(String s);

    /**
     * Gets the max connections.
     *
     * @return the max connections
     */
    int getMaxConnections();

    /**
     * Sets the max connections.
     *
     * @param i
     *            the new max connections
     */
    void setMaxConnections(int i);

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the name.
     *
     * @param s
     *            the new name
     */
    void setName(String s);

    /**
     * Gets the passwd.
     *
     * @return the passwd
     */
    String getPasswd();

    /**
     * Sets the passwd.
     *
     * @param s
     *            the new passwd
     */
    void setPasswd(String s);

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    int getRetryCount();

    /**
     * Sets the retry count.
     *
     * @param i
     *            the new retry count
     */
    void setRetryCount(int i);

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
     * Sets the retry frequency.
     *
     * @param i
     *            the new retry frequency
     */
    void setRetryFrequency(int i);

    /**
     * Gets the transfer method.
     *
     * @return the transfer method
     *
     * @throws TransferException
     *             the transfer exception
     */
    TransferMethod getTransferMethod() throws TransferException;

    /**
     * Gets the change log list.
     *
     * @return the change log list
     *
     * @throws TransferException
     *             the transfer exception
     */
    Collection<ChangeLog> getChangeLogList() throws TransferException;

    /**
     * Sets the transfer method.
     *
     * @param m
     *            the new transfer method
     */
    void setTransferMethod(TransferMethod m);

    /**
     * Gets the transfer method name.
     *
     * @return the transfer method name
     */
    String getTransferMethodName();

    /**
     * Sets the transfer method name.
     *
     * @param s
     *            the new transfer method name
     */
    void setTransferMethodName(String s);

    /**
     * Gets the transfer group.
     *
     * @return the transfer group
     *
     * @throws DataFileException
     *             the data file exception
     */
    TransferGroup getTransferGroup() throws DataFileException;

    /**
     * Sets the transfer group.
     *
     * @param m
     *            the new transfer group
     */
    void setTransferGroup(TransferGroup m);

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    String getTransferGroupName();

    /**
     * Sets the transfer group name.
     *
     * @param s
     *            the new transfer group name
     */
    void setTransferGroupName(String s);

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    String getDir();

    /**
     * Sets the dir.
     *
     * @param dir
     *            the new dir
     */
    void setDir(String dir);

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    long getSent();

    /**
     * Gets the formatted sent.
     *
     * @return the formatted sent
     */
    String getFormattedSent();

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    long getDuration();

    /**
     * Gets the formatted duration.
     *
     * @return the formatted duration
     */
    String getFormattedDuration();

    /**
     * Gets the band width.
     *
     * @return the band width
     */
    long getBandWidth();

    /**
     * Gets the formatted band width.
     *
     * @return the formatted band width
     */
    String getFormattedBandWidth();

    /**
     * Gets the formatted band width in M bits per seconds.
     *
     * @return the formatted band width in M bits per seconds
     */
    double getFormattedBandWidthInMBitsPerSeconds();

    /**
     * Increase priority within destination.
     *
     * @param d
     *            the d
     * @param step
     *            the step
     */
    void increasePriorityWithinDestination(Destination d, int step);

    /**
     * Decrease priority within destination.
     *
     * @param d
     *            the d
     * @param step
     *            the step
     */
    void decreasePriorityWithinDestination(Destination d, int step);

    /**
     * Gets the data transfers today.
     *
     * @return the data transfers today
     *
     * @throws TransferException
     *             the transfer exception
     */
    Collection<DataTransferLightBean> getDataTransfersToday() throws TransferException;

    /**
     * Gets the destinations.
     *
     * @return the destinations
     *
     * @throws TransferException
     *             the transfer exception
     */
    Collection<Destination> getDestinations() throws TransferException;

    /**
     * Reset transfer statistics.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    void resetTransferStatistics(User u) throws TransferException;

    /**
     * Clean data window.
     *
     * @param u
     *            the u
     *
     * @throws TransferException
     *             the transfer exception
     */
    void cleanDataWindow(User u) throws TransferException;

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
    String getReport(User u) throws TransferException;

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
    String getReport(User u, Host proxy) throws TransferException;

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
    RemoteInputStream getOutput(final User u) throws TransferException;

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
    long transfer(User u, byte[] bytes, String target, long remotePos) throws TransferException;

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    Object getOjbImplementation();
}
