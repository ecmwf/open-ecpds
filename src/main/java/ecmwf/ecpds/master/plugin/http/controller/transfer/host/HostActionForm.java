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

package ecmwf.ecpds.master.plugin.http.controller.transfer.host;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Form bean for Host
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamManager;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferGroupHome;
import ecmwf.ecpds.master.plugin.http.home.ecuser.EcUserHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.EcTransModuleHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferMethodHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUser;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUserException;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMethod;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class HostActionForm.
 */
public class HostActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6339376929448972074L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(HostActionForm.class);

    /** The id. */
    private String id = "";

    /** The name. */
    private String name = "";

    /** The nick name. */
    private String nickName = "";

    /** The automatic location. */
    private String automaticLocation = "";

    /** The latitude. */
    private String latitude = "";

    /** The longitude. */
    private String longitude = "";

    /** The type. */
    private String type = "";

    /** The filter name. */
    private String filterName = "";

    /** The login. */
    private String login = "";

    /** The passwd. */
    private String passwd = "";

    /** The network name. */
    private String networkName = "";

    /** The network code. */
    private String networkCode = "";

    /** The check filename. */
    private String checkFilename = "";

    /** The user mail. */
    private String userMail = "";

    /** The max connections. */
    private String maxConnections = Cnf.at("HostActionForm", "maxConnections", "1");

    /** The connections. */
    private String connections = Cnf.at("HostActionForm", "connections", "0");

    /** The retry count. */
    private String retryCount = Cnf.at("HostActionForm", "retryCount", "15");

    /** The retry frequency. */
    private String retryFrequency = Cnf.at("HostActionForm", "retryFrequency", "15000");

    /** The comment. */
    private String comment = "";

    /** The host. */
    private String host = "";

    /** The dir. */
    private String dir = "";

    /** The properties. */
    private String properties = "";

    /** The javascript. */
    private String javascript = "";

    /** The active. */
    private String active = "off";

    /** The check frequency. */
    private String checkFrequency = "600000";

    /** The acquisition frequency. */
    private String acquisitionFrequency = "600000";

    /** The valid. */
    private String valid = "off";

    /** The check. */
    private String check = "off";

    /** The mail on success. */
    private String mailOnSuccess = "off";

    /** The mail on error. */
    private String mailOnError = "off";

    /** The notify once. */
    private String notifyOnce = "off";

    /** The transfer group. */
    private String transferGroup = "";

    /** The transfer method. */
    private String transferMethod = "";

    /** The owner. */
    private String owner = "";

    /**
     * Sets the default values.
     *
     * @param u
     *            the new default values
     */
    public void setDefaultValues(final User u) {
        automaticLocation = Cnf.at("HostActionForm", "automaticLocation", "on");
        maxConnections = Cnf.at("HostActionForm", "maxConnections", "1");
        connections = Cnf.at("HostActionForm", "connections", "0");
        retryCount = Cnf.at("HostActionForm", "retryCount", "15");
        retryFrequency = Cnf.at("HostActionForm", "retryFrequency", "15000");
        properties = Cnf.valuesOf("ECtrans");
        active = Cnf.at("HostActionForm", "active", "on");
        acquisitionFrequency = Cnf.at("HostActionForm", "acquisitionFrequency", "600000");
        checkFrequency = Cnf.at("HostActionForm", "checkFrequency", "600000");
        check = Cnf.at("HostActionForm", "check", "off");
        mailOnSuccess = Cnf.at("HostActionForm", "mailOnSuccess", "off");
        mailOnError = Cnf.at("HostActionForm", "mailOnError", "off");
        notifyOnce = Cnf.at("HostActionForm", "notifyOnce", "on");
        owner = u.getId();
        if (isNotEmpty(owner)) {
            userMail = owner + "@ecmwf.int";
        }
    }

    /**
     * Gets the check frequency.
     *
     * @return the check frequency
     */
    public String getCheckFrequency() {
        return checkFrequency;
    }

    /**
     * Gets the acquisition frequency.
     *
     * @return the acquisition frequency
     */
    public String getAcquisitionFrequency() {
        return acquisitionFrequency;
    }

    /**
     * Gets the valid.
     *
     * @return the valid
     */
    public String getValid() {
        return valid;
    }

    /**
     * Gets the check.
     *
     * @return the check
     */
    public String getCheck() {
        return check;
    }

    /**
     * Gets the check filename.
     *
     * @return the check filename
     */
    public String getCheckFilename() {
        return checkFilename;
    }

    /**
     * Gets the mail on success.
     *
     * @return the mail on success
     */
    public String getMailOnSuccess() {
        return mailOnSuccess;
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
     * Gets the notify once.
     *
     * @return the notify once
     */
    public String getNotifyOnce() {
        return notifyOnce;
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
     * Gets the active.
     *
     * @return the active
     */
    public String getActive() {
        return active;
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
     * Gets the connections.
     *
     * @return the connections
     */
    public String getConnections() {
        return connections;
    }

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    public String getCompletions() {
        return ECtransOptions.toString(ECtransGroups.HOST);
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
        if (pos >= 0) {
            return data.substring(0, pos).trim();
        }
        return data.trim();
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
        if (pos >= 0) {
            return data.substring(pos + ECtransSetup.SEPARATOR.length()).trim();
        }
        return "";
    }

    /**
     * Gets the transfer module names.
     *
     * @return the transfer module names
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    public String getTransferModuleNames() throws TransferException {
        return EcTransModuleHome.findAll().stream().map(module -> "\"" + module.getName() + "\"")
                .collect(Collectors.joining(", "));
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    public String getDir() {
        return dir;
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
     * Gets the login.
     *
     * @return the login
     */
    public String getLogin() {
        return login;
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
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the passwd.
     *
     * @return the passwd
     */
    public String getPasswd() {
        return passwd;
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
     * Sets the check frequency.
     *
     * @param string
     *            the new check frequency
     */
    public void setCheckFrequency(final String string) {
        checkFrequency = string;
    }

    /**
     * Sets the acquisition frequency.
     *
     * @param string
     *            the new acquisition frequency
     */
    public void setAcquisitionFrequency(final String string) {
        acquisitionFrequency = string;
    }

    /**
     * Sets the valid.
     *
     * @param string
     *            the new valid
     */
    public void setValid(final String string) {
        valid = string;
    }

    /**
     * Sets the check.
     *
     * @param string
     *            the new check
     */
    public void setCheck(final String string) {
        check = string;
    }

    /**
     * Sets the check filename.
     *
     * @param string
     *            the new check filename
     */
    public void setCheckFilename(final String string) {
        checkFilename = string;
    }

    /**
     * Sets the mail on success.
     *
     * @param string
     *            the new mail on success
     */
    public void setMailOnSuccess(final String string) {
        mailOnSuccess = string;
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
     * Sets the notify once.
     *
     * @param string
     *            the new notify once
     */
    public void setNotifyOnce(final String string) {
        notifyOnce = string;
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
     * Sets the active.
     *
     * @param string
     *            the new active
     */
    public void setActive(final String string) {
        active = string;
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
     * Sets the connections.
     *
     * @param string
     *            the new connections
     */
    public void setConnections(final String string) {
        connections = string;
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
     * Sets the host.
     *
     * @param string
     *            the new host
     */
    public void setHost(final String string) {
        host = string;
    }

    /**
     * Sets the dir.
     *
     * @param string
     *            the new dir
     */
    public void setDir(final String string) {
        dir = string;
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
     * Sets the login.
     *
     * @param string
     *            the new login
     */
    public void setLogin(final String string) {
        login = string;
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
     * Sets the name.
     *
     * @param string
     *            the new name
     */
    public void setName(final String string) {
        name = string;
    }

    /**
     * Sets the passwd.
     *
     * @param string
     *            the new passwd
     */
    public void setPasswd(final String string) {
        passwd = string;
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
     * Gets the network code.
     *
     * @return the network code
     */
    public String getNetworkCode() {
        return networkCode;
    }

    /**
     * Sets the network code.
     *
     * @param networkCode
     *            the new network code
     */
    public void setNetworkCode(final String networkCode) {
        this.networkCode = networkCode;
        for (var i = 0; i < HostOption.networkCode.length; i++) {
            if (HostOption.networkCode[i].equals(networkCode)) {
                this.setNetworkName(HostOption.networkName[i]);
            }
        }
    }

    /**
     * Gets the network name.
     *
     * @return the network name
     */
    public String getNetworkName() {
        return networkName;
    }

    /**
     * Sets the network name.
     *
     * @param networkName
     *            the new network name
     */
    public void setNetworkName(final String networkName) {
        this.networkName = networkName;
    }

    /**
     * Gets the nick name.
     *
     * @return the nick name
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * Sets the nick name.
     *
     * @param nickName
     *            the new nick name
     */
    public void setNickName(final String nickName) {
        this.nickName = nickName;
    }

    /**
     * Sets the automatic location.
     *
     * @param automaticLocation
     *            the new automatic location
     */
    public void setAutomaticLocation(final String automaticLocation) {
        this.automaticLocation = automaticLocation;
    }

    /**
     * Gets the latitude.
     *
     * @return the latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude.
     *
     * @param latitude
     *            the new latitude
     */
    public void setLatitude(final String latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude.
     *
     * @return the longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude.
     *
     * @param longitude
     *            the new longitude
     */
    public void setLongitude(final String longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets the automatic location.
     *
     * @return the automatic location
     */
    public String getAutomaticLocation() {
        return automaticLocation;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        if (type.isBlank()) {
            type = HostOption.type[0];
        }
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(final String type) {
        this.type = type;
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
     * Sets the filter name.
     *
     * @param filterName
     *            the new filter name
     */
    public void setFilterName(final String filterName) {
        this.filterName = filterName;
    }

    /**
     * Gets the transfer method options.
     *
     * @return the transfer method options
     */
    public Collection<TransferMethod> getTransferMethodOptions() {
        try {
            return TransferMethodHome.findAll();
        } catch (final TransferException e) {
            log.error("Problem getting TransferMethods", e);
            return new ArrayList<>();
        }
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
     * Gets the network options.
     *
     * @return the network options
     */
    public Collection<Pair> getNetworkOptions() {
        final Collection<Pair> options = new ArrayList<>();
        for (var i = 0; i < HostOption.networkCode.length; i++) {
            options.add(new Pair(HostOption.networkCode[i], HostOption.networkName[i]));
        }
        return options;
    }

    /**
     * Gets the type options.
     *
     * @return the type options
     */
    public Collection<Pair> getTypeOptions() {
        final Collection<Pair> options = new ArrayList<>();
        for (final String element : HostOption.type) {
            options.add(new Pair(element, element));
        }
        return options;
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
     * Gets the allowed users.
     *
     * @return the allowed users
     */
    public Collection<EcUser> getAllowedUsers() {
        try {
            return EcUserHome.findByHostName(this.id);
        } catch (final EcUserException e) {
            log.error("Problem getting EcUsers for host '" + this.id + "'", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Gets the allowed user options.
     *
     * @return the allowed user options
     */
    public Collection<EcUser> getAllowedUserOptions() {
        try {
            final var all = EcUserHome.findAll();
            all.removeAll(getAllowedUsers());
            return all;
        } catch (final EcUserException e) {
            log.error("Problem getting EcUser options for host '" + this.id + "'", e);
            return new ArrayList<>(0);
        }
    }

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
     * @param transferGroup
     *            the new transfer group
     */
    public void setTransferGroup(final String transferGroup) {
        this.transferGroup = transferGroup;
    }

    /**
     * Gets the transfer method.
     *
     * @return the transfer method
     */
    public String getTransferMethod() {
        if (transferMethod.isBlank()) {
            final var methods = getTransferMethodOptions().iterator();
            if (methods.hasNext()) {
                transferMethod = methods.next().getName();
            }
        }
        return transferMethod;
    }

    /**
     * Gets the transfer method value.
     *
     * @return the transfer method value
     */
    public String getTransferMethodValue() {
        final var method = getTransferMethod();
        if (!method.isBlank()) {
            for (TransferMethod currentMethod : getTransferMethodOptions()) {
                if (method.equals(currentMethod.getName())) {
                    return currentMethod.getValue();
                }
            }
        }
        return "";
    }

    /**
     * Sets the transfer method.
     *
     * @param string
     *            the new transfer method
     */
    public void setTransferMethod(final String string) {
        transferMethod = string;
    }

    /**
     * Gets the owner options.
     *
     * @return the owner options
     */
    public Collection<EcUser> getOwnerOptions() {
        try {
            return EcUserHome.findAll();
        } catch (final EcUserException e) {
            log.error("Problem getting EcUser", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the owner.
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner.
     *
     * @param string
     *            the new owner
     */
    public void setOwner(final String string) {
        owner = string;
    }

    /**
     * Populate host.
     *
     * @param h
     *            the h
     */
    protected void populateHost(final Host h) {
        h.setLogin(login);
        h.setPasswd(passwd);
        h.setMaxConnections(convertToInt(maxConnections));
        h.setRetryCount(convertToInt(retryCount));
        h.setRetryFrequency(convertToInt(retryFrequency));
        h.setComment(comment);
        h.setHost(host);
        h.setDir(dir);
        h.setData(properties + "\n" + ECtransSetup.SEPARATOR + javascript);
        h.setECUserName(owner);
        h.setTransferMethodName(transferMethod);
        h.setTransferGroupName(transferGroup);
        h.setActive(convertToBoolean(active));
        h.setCheckFrequency(convertToLong(checkFrequency));
        h.setAcquisitionFrequency(convertToLong(acquisitionFrequency));
        h.setCheck(convertToBoolean(check));
        h.setCheckFilename(checkFilename);
        h.setMailOnSuccess(convertToBoolean(mailOnSuccess));
        h.setMailOnError(convertToBoolean(mailOnError));
        h.setNotifyOnce(convertToBoolean(notifyOnce));
        h.setUserMail(userMail);
        h.setNetworkCode(this.getNetworkCode());
        h.setNetworkName(this.getNetworkName());
        h.setNickName(this.getNickName().trim());
        h.setAutomaticLocation(convertToBoolean(automaticLocation));
        h.setLatitude(convertToDouble(this.getLatitude()));
        h.setLongitude(convertToDouble(this.getLongitude()));
        h.setType(this.getType());
        h.setFilterName(this.getFilterName());
    }

    /**
     * Convert to double.
     *
     * @param value
     *            the value
     *
     * @return the double
     */
    public double convertToDouble(final String value) {
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException e) {
            this.addError("errors.badNumber", e.getMessage(), value);
            return 0;
        }
    }

    /**
     * Convert from double.
     *
     * @param value
     *            the value
     *
     * @return the string
     */
    public static final String convertFromDouble(final Double value) {
        if (value != null) {
            return Double.toString(value);
        }
        return Double.toString(0);
    }

    /**
     * Populate from host.
     *
     * @param h
     *            the h
     */
    protected void populateFromHost(final Host h) {
        this.setId(h.getId());
        this.setName(h.getName());
        this.setLogin(h.getLogin());
        this.setPasswd(h.getPasswd());
        this.setDir(h.getDir());
        this.setMaxConnections(Integer.toString(h.getMaxConnections()));
        this.setConnections(Integer.toString(h.getConnections()));
        this.setRetryCount(Integer.toString(h.getRetryCount()));
        this.setRetryFrequency(Integer.toString(h.getRetryFrequency()));
        this.setComment(h.getComment());
        this.setHost(h.getHost());
        this.setProperties(getProperties(h.getData()));
        this.setJavascript(getJavascript(h.getData()));
        this.setActive(h.getActive() ? "on" : "off");
        this.setTransferMethod(h.getTransferMethodName());
        this.setTransferGroup(h.getTransferGroupName());
        this.setOwner(h.getECUserName());
        this.setCheckFrequency(Long.toString(h.getCheckFrequency()));
        this.setAcquisitionFrequency(Long.toString(h.getAcquisitionFrequency()));
        this.setValid(h.getValid() ? "on" : "off");
        this.setCheck(h.getCheck() ? "on" : "off");
        this.setCheckFilename(h.getCheckFilename());
        this.setMailOnSuccess(h.getMailOnSuccess() ? "on" : "off");
        this.setMailOnError(h.getMailOnError() ? "on" : "off");
        this.setNotifyOnce(h.getNotifyOnce() ? "on" : "off");
        this.setUserMail(h.getUserMail());
        this.setNickName(h.getNickName());
        this.setAutomaticLocation(h.getAutomaticLocation() ? "on" : "off");
        this.setLongitude(convertFromDouble(h.getLongitude()));
        this.setLatitude(convertFromDouble(h.getLatitude()));
        this.setType(h.getType());
        this.setFilterName(h.getFilterName());
        this.setNetworkCode(h.getNetworkCode());
        this.setNetworkName(h.getNetworkName());
    }
}
