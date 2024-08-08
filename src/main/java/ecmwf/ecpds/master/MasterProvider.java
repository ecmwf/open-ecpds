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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ACQUISITION;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_MASTER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DEFAULT_DATE_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_RECENT_DATE_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REGEX_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SERVER_LANGUAGE_CODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SERVER_TIME_ZONE_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SHORT_MONTH_NAMES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SYSTEM_KEY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_MASTER_HOME_DIR;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Country;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferServer;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ecaccess.UserSession;
import ecmwf.common.ftp.FtpParser;
import ecmwf.common.ftp.FtpParser.FileEntry;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.text.Format;
import ecmwf.ecbatch.eis.rmi.client.ECauthToken;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;
import ecmwf.ecbatch.eis.rmi.client.FileListElement;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.TransferScheduler;

/**
 * The Class MasterProvider.
 */
public final class MasterProvider extends NativeAuthenticationProvider {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MasterProvider.class);

    /** The Constant master. */
    private static final MasterServer master = StarterServer.getInstance(MasterServer.class);

    /** The Constant base. */
    private static final ECpdsBase base = master.getECpdsBase();

    /** The Constant dataSpaces. */
    private static final List<UserDataSpace> dataSpaces = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adds the.
     *
     * @param dataSpace
     *            the data space
     */
    private static void add(final UserDataSpace dataSpace) {
        dataSpaces.add(dataSpace);
    }

    /**
     * Removes the.
     *
     * @param dataSpace
     *            the data space
     */
    private static void remove(final UserDataSpace dataSpace) {
        dataSpaces.remove(dataSpace);
    }

    /**
     * Close all incoming connections.
     */
    public static void closeAllIncomingConnections() {
        synchronized (dataSpaces) {
            for (final UserDataSpace dataSpace : dataSpaces) {
                final var closeable = dataSpace.closeable;
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (final Throwable t) {
                        // Ignored!
                    }
                }
            }
        }
    }

    /**
     * Close incoming connection.
     *
     * @param id
     *            the id
     *
     * @return true, if successful
     */
    public static boolean closeIncomingConnection(final String id) {
        synchronized (dataSpaces) {
            for (final UserDataSpace dataSpace : dataSpaces) {
                if (id != null && id.equals(dataSpace.getId())) {
                    final var closeable = dataSpace.closeable;
                    try {
                        if (closeable != null) {
                            closeable.close();
                        }
                        return true;
                    } catch (final Throwable t) {
                        // Ignored!
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets the incoming connection ids.
     *
     * @return the incoming connection ids
     */
    public static String[] getIncomingConnectionIds() {
        final List<String> result = new ArrayList<>();
        synchronized (dataSpaces) {
            for (final UserDataSpace dataSpace : dataSpaces) {
                result.add(dataSpace.getId());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Converts into array.
     *
     * @param results
     *            the results
     *
     * @return the array
     */
    private static List<FileListElement> toArray(final String[] results) {
        final List<FileListElement> array = new ArrayList<>();
        if (results != null) {
            for (final String result : results) {
                final var file = new FileListElement();
                file.setName(result);
                array.add(file);
            }
        }
        return array;
    }

    /**
     * Gets the interactive dir.
     *
     * @param host
     *            the host
     *
     * @return the string
     */
    private static String getInteractiveDir(final Host host) {
        final var br = new BufferedReader(new StringReader(host.getDir()));
        String path;
        try {
            path = br.readLine();
        } catch (final IOException e) {
            _log.warn("Error getting dir on Host-{}", host.getName(), e);
            path = "";
        }
        path = isNotEmpty(path) ? path : "";
        int pos;
        if (HostOption.ACQUISITION.equals(host.getType())) {
            // Acquisition hosts!
            final var homeDir = HOST_MASTER.getECtransSetup(host.getData()).getString(HOST_MASTER_HOME_DIR);
            if (homeDir == null) {
                // Remove the options!
                if (path.startsWith("[") && (pos = path.indexOf("]")) != -1) {
                    path = path.substring(pos + 1);
                }
                // Remove the pattern!
                if (path.endsWith("}") && (pos = path.lastIndexOf("{")) != -1) {
                    path = path.substring(0, pos);
                }
                // Stop the path before the date parameter if it is set!
                if ((pos = path.indexOf("$date")) != -1) {
                    path = path.substring(0, pos);
                }
            } else {
                // Let's use the defined path!
                path = homeDir;
            }
        } else if (HostOption.DISSEMINATION.equals(host.getType())) {
            // Dissemination hosts, remove selector!
            if (path.startsWith("(") && (pos = path.indexOf(")")) != -1) {
                path = path.substring(pos + 1);
                if (path.startsWith(" ")) {
                    path = path.substring(1);
                }
            }
            // Stop the path before any parameter
            if ((pos = path.indexOf("$")) != -1) {
                path = path.substring(0, pos);
            }
        }
        // Replication and source hosts (use the first line)
        pos = path.lastIndexOf("/");
        if (pos != -1) {
            path = path.substring(0, pos);
        }
        if (path.length() > 0) {
            path = path + "/";
        }
        return path;
    }

    /**
     * Checks if is registred user.
     *
     * @param user
     *            the user
     *
     * @return true, if is registred user
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public boolean isRegistredUser(final String user) throws Exception {
        return master.isRegistredUser(user);
    }

    /**
     * Gets the user session.
     *
     * @param host
     *            the host
     * @param user
     *            the user
     * @param password
     *            the password
     * @param profile
     *            the profile
     * @param closeable
     *            the closeable
     *
     * @return the user session
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public UserSession getUserSession(final String host, final String user, final String password, final String profile,
            final Closeable closeable) throws Exception {
        final var root = master.getRoot();
        return new UserDataSpace(master.getWebUser(user, password, root).getId(),
                "Using " + profile + " on " + root + " from " + user + "@" + host, profile, host, closeable);
    }

    /**
     * Gets the user session.
     *
     * @param host
     *            the host
     * @param user
     *            the user
     * @param token
     *            the token
     * @param profile
     *            the profile
     * @param closeable
     *            the closeable
     *
     * @return the user session
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public UserSession getUserSession(final String host, final String user, final ECauthToken token,
            final String profile, final Closeable closeable) throws Exception {
        throw new IOException("Not implemented");
    }

    /**
     * Gets the user session.
     *
     * @param host
     *            the host
     * @param ticket
     *            the ticket
     * @param profile
     *            the profile
     * @param closeable
     *            the closeable
     *
     * @return the user session
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public UserSession getUserSession(final String host, final String ticket, final String profile,
            final Closeable closeable) throws Exception {
        throw new IOException("Not implemented");
    }

    /**
     * Support tickets.
     *
     * @return true, if successful
     */
    @Override
    public boolean supportTickets() {
        return false;
    }

    /**
     * Checks if is password.
     *
     * @param user
     *            the user
     * @param password
     *            the password
     *
     * @return true, if is password
     */
    @Override
    public boolean isPassword(final String user, final String password) {
        return true;
    }

    /**
     * Gets the root.
     *
     * @return the root
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public String getRoot() throws RemoteException {
        return master.getRoot();
    }

    /**
     * The Class FtpURL.
     */
    private static final class FtpURL {
        /** The Constant TYPE_UNKNOWN. */
        static final int TYPE_UNKNOWN = -1;

        /** The Constant TYPE_BY_HOSTS. */
        static final int TYPE_BY_HOSTS = 0;

        /** The Constant TYPE_BY_DESTINATIONS. */
        static final int TYPE_BY_DESTINATIONS = 1;

        /** The Constant TYPE_BY_COUNTRIES. */
        static final int TYPE_BY_COUNTRIES = 2;

        /** The Constant TYPE_BY_ATTACHMENTS. */
        static final int TYPE_BY_ATTACHMENTS = 3;

        /** The Constant TYPE_BY_DATA. */
        static final int TYPE_BY_DATA = 4;

        /** The Constant TYPE_BY_TYPES. */
        static final int TYPE_BY_TYPES = 5;

        /** The _domain. */
        private final String domain;

        /** The _path. */
        private final String path;

        /** The _tokenizer. */
        private final StringTokenizer tokenizer;

        /**
         * Instantiates a new ftp url.
         *
         * @param url
         *            the url
         */
        FtpURL(final String url) {
            final var userIndex = url.indexOf("]");
            final var domainIndex = url.indexOf(":");
            this.domain = url.substring(userIndex + 1, domainIndex);
            this.path = url.substring(domainIndex + 1);
            this.tokenizer = new StringTokenizer(path, "/");
        }

        /**
         * Gets the domain.
         *
         * @return the domain
         */
        String getDomain() {
            return domain;
        }

        /**
         * Count tokens.
         *
         * @return the int
         */
        int countTokens() {
            return tokenizer.countTokens();
        }

        /**
         * Next element.
         *
         * @return the string
         */
        String nextElement() {
            return tokenizer.nextToken();
        }

        /**
         * Next elements.
         *
         * @return the string
         */
        String nextElements() {
            final var result = new StringBuilder();
            while (tokenizer.hasMoreElements()) {
                result.append(result.length() == 0 ? "" : "/").append(nextElement());
            }
            return result.toString();
        }

        /**
         * Next element.
         *
         * @param index
         *            the index
         *
         * @return the string
         */
        String nextElement(final int index) {
            for (var i = 1; i < index; i++) {
                nextElement();
            }
            return nextElement();
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        int getType() {
            if ("destinations".equalsIgnoreCase(domain)) {
                return TYPE_BY_DESTINATIONS;
            }
            if ("hosts".equalsIgnoreCase(domain)) {
                return TYPE_BY_HOSTS;
            } else if ("countries".equalsIgnoreCase(domain)) {
                return TYPE_BY_COUNTRIES;
            } else if ("attachments".equalsIgnoreCase(domain)) {
                return TYPE_BY_ATTACHMENTS;
            } else if ("data".equalsIgnoreCase(domain)) {
                return TYPE_BY_DATA;
            } else if ("types".equalsIgnoreCase(domain)) {
                return TYPE_BY_TYPES;
            } else {
                return TYPE_UNKNOWN;
            }
        }
    }

    /**
     * The Class UserDataSpace.
     */
    public final class UserDataSpace extends UserSession {
        /** The from. */
        private final String from;

        /** The protocol. */
        private final String protocol;

        /** The host. */
        private final String host;

        /** The closeable. */
        private final Closeable closeable;

        /**
         * Instantiates a new user data space.
         *
         * @param user
         *            the user
         * @param from
         *            the from
         * @param protocol
         *            the protocol
         * @param host
         *            the host
         * @param closeable
         *            the closeable
         */
        UserDataSpace(final String user, final String from, final String protocol, final String host,
                final Closeable closeable) {
            super(user, user);
            this.from = from;
            this.protocol = protocol;
            this.host = host;
            this.closeable = closeable;
            add(this);
        }

        /**
         * Close.
         *
         * @param remove
         *            the remove
         */
        @Override
        public void close(final boolean remove) {
            remove(this);
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {
            return protocol + ":" + getUser() + "@" + host;
        }

        /**
         * Gets the default group.
         *
         * @return the default group
         */
        @Override
        public String getDefaultGroup() {
            return "ecpds";
        }

        /**
         * _check destination.
         *
         * @param destination
         *            the destination
         *
         * @return the string
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private String checkDestination(final String destination) throws EccmdException {
            final List<String> authorised = new ArrayList<>();
            final Destination fromDb;
            try {
                authorised.addAll(base.getAuthorisedDestinations(getUser()));
                fromDb = base.getDestinationObject(destination);
            } catch (final DataBaseException e) {
                throw new EccmdException(Format.getMessage(e));
            }
            if (fromDb == null) {
                throw new EccmdException("Destination " + destination + " not found");
            }
            if (!authorised.contains(destination)) {
                _log.debug("Permission denied for {}: allowed={}", destination, authorised);
                throw new EccmdException("Permission denied for user " + getUser());
            }
            if (!fromDb.getActive()) {
                throw new EccmdException("Destination " + destination + " not active");
            }
            return "Current destination: " + fromDb.getName() + " (" + fromDb.getCountryIso() + ")\n Max connections: "
                    + fromDb.getMaxConnections() + "\n Retry count    : " + fromDb.getRetryCount()
                    + "\n Retry frequency: " + Format.formatDuration(fromDb.getRetryFrequency())
                    + "\n Max start      : " + fromDb.getMaxStart() + "\n Start frequency: "
                    + Format.formatDuration(fromDb.getStartFrequency()) + "\n User mail      : " + fromDb.getUserMail()
                    + "\n Comment        : " + fromDb.getComment();
        }

        /**
         * _check destination.
         *
         * @param type
         *            the type
         *
         * @return the string
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private String checkType(final String type) throws EccmdException {
            final var entry = DestinationOption.getTypeEntry(type);
            if (entry == null) {
                throw new EccmdException(type + " not found");
            }
            var authorised = false;
            try {
                for (final String destination : base.getAuthorisedDestinations(getUser())) {
                    final var current = base.getDestinationObject(destination);
                    if (current != null && current.getType() == entry.getId()) {
                        authorised = true;
                        break;
                    }
                }
            } catch (final DataBaseException e) {
                throw new EccmdException(Format.getMessage(e));
            }
            if (!authorised) {
                _log.debug("Permission denied for {}", type);
                throw new EccmdException("Permission denied for user " + getUser());
            }
            return "Current type: " + entry.getLabel() + " (id=" + entry.getId() + ")";
        }

        /**
         * _check host.
         *
         * @param host
         *            the host
         *
         * @return the string
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private String checkHost(final String host) throws EccmdException {
            Host fromDb;
            try {
                fromDb = master.getDataBase().getHostObject(host);
            } catch (final Exception e) {
                fromDb = null;
            }
            if (fromDb == null) {
                throw new EccmdException("Host " + host + " not found");
            }
            if (!fromDb.getActive()) {
                throw new EccmdException("Host " + host + " not active");
            }
            return "Current host: " + fromDb.getNickname() + " (" + fromDb.getHost() + ")\n Login          : "
                    + fromDb.getLogin() + "\n Password       : " + fromDb.getPasswd() + "\n Directory      : "
                    + getInteractiveDir(fromDb) + "\n Host Type      : " + fromDb.getType() + "\n Transfer group : "
                    + fromDb.getTransferGroupName() + "\n Transfer method: " + fromDb.getTransferMethodName()
                    + "\n Max connections: " + fromDb.getMaxConnections() + "\n Retry count    : "
                    + fromDb.getRetryCount() + "\n Retry frequency: "
                    + Format.formatDuration(fromDb.getRetryFrequency()) + "\n Comment        : " + fromDb.getComment();
        }

        /**
         * _check transfer server.
         *
         * @param transferServer
         *            the transfer server
         *
         * @return the string
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private String checkTransferServer(final String transferServer) throws EccmdException {
            final var fromDb = master.getDataBase().getTransferServerObject(transferServer);
            if (fromDb == null) {
                throw new EccmdException("TransferServer " + transferServer + " not found");
            }
            if (!fromDb.getActive()) {
                throw new EccmdException("TransferServer " + transferServer + " not active");
            }
            return "Current transfer server: " + fromDb.getName() + " (" + fromDb.getHost() + ":" + fromDb.getPort()
                    + ")\n Replication   : " + fromDb.getHostForReplicationName();
        }

        /**
         * _check country.
         *
         * @param country
         *            the country
         *
         * @return the string
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private String checkCountry(final String country) throws EccmdException {
            if (master.getDataBase().getCountryObject(country) == null) {
                throw new EccmdException("Country " + country + " not found");
            }
            return null;
        }

        /**
         * Gets the destinations.
         *
         * @return the array
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private List<FileListElement> getDestinations() throws EccmdException {
            final List<FileListElement> array = new ArrayList<>();
            final List<String> authorised;
            final Destination[] destinations;
            try {
                authorised = base.getAuthorisedDestinations(getUser());
                destinations = base.getDestinationArray();
            } catch (final DataBaseException e) {
                throw new EccmdException(Format.getMessage(e));
            }
            for (final Destination destination : destinations) {
                if (destination.getActive() && authorised.contains(destination.getName())) {
                    final var update = destination.getUpdate();
                    final var element = new FileListElement();
                    element.setComment(destination.getComment());
                    element.setGroup(destination.getCountryIso());
                    element.setName(destination.getName());
                    element.setTime(update != null ? update.getTime() : master.getStartDate().getTime());
                    element.setUser(destination.getECUserName());
                    element.setRight("drwxr-x---");
                    element.setSize("2048");
                    array.add(element);
                }
            }
            return array;
        }

        /**
         * Gets the types.
         *
         * @return the array
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private List<FileListElement> getTypes() throws EccmdException {
            final List<FileListElement> array = new ArrayList<>();
            final List<String> authorised;
            final Destination[] destinations;
            try {
                authorised = base.getAuthorisedDestinations(getUser());
                destinations = base.getDestinationArray();
            } catch (final DataBaseException e) {
                throw new EccmdException(Format.getMessage(e));
            }
            final List<String> existingLabel = new ArrayList<>();
            for (final Destination destination : destinations) {
                if (authorised.contains(destination.getName())) {
                    final Integer type = destination.getType();
                    final var label = DestinationOption.getLabel(type);
                    if (!existingLabel.contains(label)) {
                        existingLabel.add(label);
                        final var element = new FileListElement();
                        element.setComment(label + " (id=" + type + ")");
                        element.setGroup(DestinationOption.TYPE_USER_AND_GROUP);
                        element.setUser(DestinationOption.TYPE_USER_AND_GROUP);
                        element.setName(label);
                        element.setTime(master.getStartDate().getTime());
                        element.setRight("drwxr-x---");
                        element.setSize("2048");
                        array.add(element);
                    }
                }
            }
            return array;
        }

        /**
         * Gets the hosts.
         *
         * @param mover
         *            the mover
         *
         * @return the array
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private List<FileListElement> getHosts(final String mover) throws EccmdException {
            final List<FileListElement> array = new ArrayList<>();
            final List<String> authorised;
            final String transferGroupName;
            try {
                transferGroupName = base.getTransferServer(mover).getTransferGroupName();
                authorised = base.getAuthorisedHosts(getUser());
            } catch (final DataBaseException e) {
                throw new EccmdException(Format.getMessage(e));
            }
            for (final Host host : base.getHostArray()) {
                if (host.getActive() && host.getTransferGroupName().equals(transferGroupName)
                        && authorised.contains(host.getName())) {
                    final var element = new FileListElement();
                    element.setComment(host.getComment());
                    element.setGroup(host.getType().toLowerCase());
                    element.setName(host.getName());
                    element.setTime(master.getStartDate().getTime());
                    element.setUser(host.getECUserName());
                    element.setRight("drwxr-x---");
                    element.setSize("2048");
                    array.add(element);
                }
            }
            return array;
        }

        /**
         * Gets the hosts.
         *
         * @param destination
         *            the destination
         * @param mover
         *            the mover
         *
         * @return the array
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private List<FileListElement> getHosts(final String destination, final String mover) throws EccmdException {
            final List<FileListElement> array = new ArrayList<>();
            final TransferGroup transferGroup;
            final Iterator<Host> hosts;
            try {
                hosts = base.getHostsByDestinationId(destination).iterator();
                transferGroup = base.getTransferServer(mover).getTransferGroup();
            } catch (final DataBaseException e) {
                throw new EccmdException(Format.getMessage(e));
            }
            while (hosts != null && hosts.hasNext()) {
                final var host = hosts.next();
                if (host.getActive() && host.getTransferGroupName().equals(transferGroup.getName())) {
                    final var element = new FileListElement();
                    element.setComment(transferGroup.getComment());
                    element.setGroup(host.getType().toLowerCase());
                    element.setName(host.getName());
                    element.setTime(master.getStartDate().getTime());
                    element.setUser(host.getECUserName());
                    element.setRight("drwxr-x---");
                    element.setSize("2048");
                    array.add(element);
                }
            }
            return array;
        }

        /**
         * Gets the transfer servers.
         *
         * @param destination
         *            the destination
         *
         * @return the array
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private List<FileListElement> getTransferServers(final String destination) throws EccmdException {
            final List<FileListElement> array = new ArrayList<>();
            final List<String> groupSelected = new ArrayList<>();
            final List<TransferServer> servers = new ArrayList<>();
            try {
                for (final Host host : base.getDestinationHost(destination, null)) {
                    final var group = host.getTransferGroup();
                    final var groupName = group.getName();
                    if (!groupSelected.contains(groupName)) {
                        groupSelected.add(groupName);
                        servers.addAll(Arrays.stream(base.getTransferServers(groupName)).toList());
                    }
                }
            } catch (final Exception e) {
                throw new EccmdException(Format.getMessage(e));
            }
            for (final TransferServer server : servers) {
                final var serverName = server.getName();
                if (server.getActive() && master.existsClientInterface(serverName, "DataMover")) {
                    final var element = new FileListElement();
                    element.setComment(server.getHost());
                    element.setGroup(server.getTransferGroupName().toLowerCase());
                    element.setName(serverName);
                    element.setTime(master.getStartDate().getTime());
                    element.setUser(getUser());
                    element.setRight("drwxr-x---");
                    element.setSize("2048");
                    array.add(element);
                }
            }
            return array;
        }

        /**
         * Gets the transfer servers.
         *
         * @return the array
         */
        private List<FileListElement> getTransferServers() {
            final List<FileListElement> array = new ArrayList<>();
            for (final TransferServer server : master.getDataBase().getTransferServerArray()) {
                if (server.getActive()) {
                    final var element = new FileListElement();
                    element.setComment(server.getHost());
                    element.setGroup(server.getTransferGroupName().toLowerCase());
                    element.setName(server.getName());
                    element.setTime(master.getStartDate().getTime());
                    element.setUser(getUser());
                    element.setRight("drwxr-x---");
                    element.setSize("2048");
                    array.add(element);
                }
            }
            return array;
        }

        /**
         * Gets the countries.
         *
         * @return the array
         */
        private List<FileListElement> getCountries() {
            final List<FileListElement> array = new ArrayList<>();
            for (final Country country : base.getCountryArray()) {
                final var element = new FileListElement();
                element.setComment(country.getName());
                element.setGroup(getUser());
                element.setName(country.getIso());
                element.setTime(master.getStartDate().getTime());
                element.setUser(getUser());
                element.setRight("drwxr-x---");
                element.setSize("2048");
                array.add(element);
            }
            return array;
        }

        /**
         * Gets the destinations.
         *
         * @param country
         *            the country
         *
         * @return the array
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private List<FileListElement> getDestinations(final String country) throws EccmdException {
            final List<FileListElement> array = new ArrayList<>();
            final List<String> authorised;
            final Destination[] destinations;
            try {
                authorised = base.getAuthorisedDestinations(getUser());
                destinations = base.getDestinationsByCountryISO(country);
            } catch (final DataBaseException e) {
                throw new EccmdException(Format.getMessage(e));
            }
            for (final Destination destination : destinations) {
                if (destination.getActive() && authorised.contains(destination.getName())) {
                    final var update = destination.getUpdate();
                    final var element = new FileListElement();
                    element.setComment(destination.getComment());
                    element.setGroup(destination.getCountryIso());
                    element.setName(destination.getName());
                    element.setTime(update != null ? update.getTime() : master.getStartDate().getTime());
                    element.setUser(destination.getECUserName());
                    element.setRight("drwxr-x---");
                    element.setSize("2048");
                    array.add(element);
                }
            }
            return array;
        }

        /**
         * Gets the file element.
         *
         * @param path
         *            the path
         * @param fileOnly
         *            the file only
         *
         * @return the file element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private FileElement getFileElement(final String path, final boolean fileOnly)
                throws EccmdException, IOException {
            final var url = new FtpURL(path);
            final var element = getFileListElement(url, path);
            if (fileOnly && element.isDirectory()) {
                throw new EccmdException("Invalid request for a directory");
            }
            if (element.getName() == null) {
                throw new FileNotFoundException(path);
            }
            StringTokenizer tokenizer = null;
            final var dir = new StringBuilder();
            switch (url.getType()) {
            case FtpURL.TYPE_BY_DESTINATIONS, FtpURL.TYPE_BY_COUNTRIES, FtpURL.TYPE_BY_HOSTS:
                tokenizer = new StringTokenizer(element.getName(), "/");
                if (tokenizer.countTokens() < 2) {
                    throw new EccmdException("Invalid source/target name");
                }
                final var server = tokenizer.nextToken();
                final var host = tokenizer.nextToken();
                while (tokenizer.hasMoreElements()) {
                    dir.append(dir.length() == 0 ? "" : "/").append(tokenizer.nextToken());
                }
                return getMoverElement(server, host, dir.toString());
            case FtpURL.TYPE_BY_ATTACHMENTS, FtpURL.TYPE_BY_TYPES:
                tokenizer = new StringTokenizer(element.getName(), "/");
                if (tokenizer.countTokens() < 1) {
                    throw new EccmdException("Invalid source/target name");
                }
                final var destinationNameOrType = tokenizer.nextToken();
                while (tokenizer.hasMoreElements()) {
                    dir.append(dir.length() == 0 ? "" : "/").append(tokenizer.nextToken());
                }
                return getAttachmentElement(destinationNameOrType, dir.toString());
            case FtpURL.TYPE_BY_DATA:
                tokenizer = new StringTokenizer(element.getName(), "/");
                if (tokenizer.countTokens() < 1) {
                    throw new EccmdException("Invalid source/target name");
                }
                final var destinationName = tokenizer.nextToken();
                while (tokenizer.hasMoreElements()) {
                    dir.append(dir.length() == 0 ? "" : "/").append(tokenizer.nextToken());
                }
                return getDataElement(destinationName, dir.toString());
            default:
                throw new EccmdException("Not a registred domain: " + url.getDomain());
            }
        }

        /**
         * Gets the data element.
         *
         * @param destinationNane
         *            the destination nane
         * @param path
         *            the path
         *
         * @return the data file element
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private DataFileElement getDataElement(final String destinationNane, final String path) throws EccmdException {
            final Destination destination;
            try {
                destination = master.getDataBase().getDestination(destinationNane);
            } catch (final DataBaseException e) {
                throw new EccmdException(e.getMessage());
            }
            return new DataFileElement(destination, path.startsWith("/") ? path.substring(1) : path);
        }

        /**
         * Gets the attachment element.
         *
         * @param destinationNaneOrType
         *            the destination nane
         * @param path
         *            the path
         *
         * @return the attachment element
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private AttachmentElement getAttachmentElement(final String destinationNaneOrType, final String path)
                throws EccmdException {
            final var destination = base.getDestinationObject(destinationNaneOrType);
            if (destination != null) {
                return new AttachmentElement(destination.getName(), path.startsWith("/") ? path.substring(1) : path);
            }
            final var entry = DestinationOption.getTypeEntry(destinationNaneOrType);
            if (entry != null) {
                return new AttachmentElement(entry.getLabel(), path.startsWith("/") ? path.substring(1) : path);
            } else {
                throw new EccmdException(destinationNaneOrType + " not found");
            }
        }

        /**
         * Gets the mover element.
         *
         * @param serverName
         *            the server name
         * @param hostName
         *            the host name
         * @param path
         *            the path
         *
         * @return the mover element
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private MoverElement getMoverElement(final String serverName, final String hostName, final String path)
                throws EccmdException {
            final TransferServer server;
            final Host host;
            try {
                server = base.getTransferServer(serverName);
                host = base.getHost(hostName);
            } catch (final DataBaseException e) {
                throw new EccmdException(e.getMessage());
            }
            if (!host.getActive()) {
                throw new EccmdException("Host " + host.getName() + " not active");
            }
            return new MoverElement(server, host, path.startsWith("/") ? path.substring(1) : path);
        }

        /**
         * Gets the file list element.
         *
         * @param url
         *            the url
         * @param path
         *            the path
         *
         * @return the file list element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private FileListElement getFileListElement(final FtpURL url, final String path)
                throws EccmdException, IOException {
            _log.debug("getFileListElement({})", path);
            final var element = new FileListElement();
            element.setRight("drwxr-x---");
            String comment = null;
            switch (url.getType()) {
            case FtpURL.TYPE_BY_DESTINATIONS:
                switch (url.countTokens()) {
                case 0:
                    break;
                case 1:
                    comment = checkDestination(url.nextElement());
                    break;
                case 2:
                    checkDestination(url.nextElement());
                    comment = checkTransferServer(url.nextElement());
                    break;
                case 3:
                    checkDestination(url.nextElement());
                    checkTransferServer(url.nextElement());
                    comment = checkHost(url.nextElement());
                    break;
                default:
                    final String server;
                    final String host;
                    checkDestination(url.nextElement());
                    checkTransferServer(server = url.nextElement());
                    checkHost(host = url.nextElement());
                    getMoverElement(server, host, url.nextElements()).set(element);
                }
                break;
            case FtpURL.TYPE_BY_COUNTRIES:
                switch (url.countTokens()) {
                case 0:
                    break;
                case 1:
                    comment = checkCountry(url.nextElement());
                    break;
                case 2:
                    checkCountry(url.nextElement());
                    comment = checkDestination(url.nextElement());
                    break;
                case 3:
                    checkCountry(url.nextElement());
                    checkDestination(url.nextElement());
                    comment = checkTransferServer(url.nextElement());
                    break;
                case 4:
                    checkCountry(url.nextElement());
                    checkDestination(url.nextElement());
                    checkTransferServer(url.nextElement());
                    comment = checkHost(url.nextElement());
                    break;
                default:
                    checkCountry(url.nextElement());
                    checkDestination(url.nextElement());
                    final var server = url.nextElement();
                    checkTransferServer(server);
                    final var host = url.nextElement();
                    checkHost(host);
                    getMoverElement(server, host, url.nextElements()).set(element);
                }
                break;
            case FtpURL.TYPE_BY_HOSTS:
                switch (url.countTokens()) {
                case 0:
                    break;
                case 1:
                    comment = checkTransferServer(url.nextElement());
                    break;
                case 2:
                    checkTransferServer(url.nextElement());
                    comment = checkHost(url.nextElement());
                    break;
                default:
                    final var server = url.nextElement();
                    checkTransferServer(server);
                    final var host = url.nextElement();
                    checkHost(host);
                    getMoverElement(server, host, url.nextElements()).set(element);
                }
                break;
            case FtpURL.TYPE_BY_ATTACHMENTS:
                switch (url.countTokens()) {
                case 0:
                    break;
                case 1:
                    checkDestination(url.nextElement());
                    break;
                default:
                    final var destinationName = url.nextElement();
                    checkDestination(destinationName);
                    getAttachmentElement(destinationName, url.nextElements()).set(element);
                }
                break;
            case FtpURL.TYPE_BY_TYPES:
                switch (url.countTokens()) {
                case 0:
                    break;
                case 1:
                    checkType(url.nextElement());
                    break;
                default:
                    final var type = url.nextElement();
                    checkType(type);
                    getAttachmentElement(type, url.nextElements()).set(element);
                }
                break;
            case FtpURL.TYPE_BY_DATA:
                switch (url.countTokens()) {
                case 0:
                    break;
                case 1:
                    checkDestination(url.nextElement());
                    break;
                default:
                    final var destination = url.nextElement();
                    checkDestination(destination);
                    getDataElement(destination, url.nextElements()).set(element);
                }
                break;
            default:
                throw new EccmdException("Not a registred domain: " + url.getDomain());
            }
            if (comment != null) {
                element.setComment(comment);
            }
            return element;
        }

        /**
         * Gets the file list.
         *
         * @param path
         *            the path
         * @param options
         *            the options
         *
         * @return the file list
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public FileListElement[] getFileList(final String path, final String options)
                throws EccmdException, IOException {
            _log.warn("Options not implemented for getFileList");
            return getFileList(path);
        }

        /**
         * Gets the file list.
         *
         * @param path
         *            the path
         *
         * @return the file list
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public FileListElement[] getFileList(final String path) throws EccmdException, IOException {
            List<FileListElement> array;
            _log.debug("getFileList({})", path);
            final var url = new FtpURL(path);
            switch (url.getType()) {
            case FtpURL.TYPE_BY_DESTINATIONS:
                array = switch (url.countTokens()) {
                case 0 -> getDestinations();
                case 1 -> getTransferServers(url.nextElement());
                case 2 -> {
                    final var destination = url.nextElement();
                    yield getHosts(destination, url.nextElement());
                }
                default -> {
                    final var serverName = url.nextElement(2);
                    final var hostName = url.nextElement();
                    yield toArray(getMoverElement(serverName, hostName, url.nextElements()).listCurrent());
                }
                };
                break;
            case FtpURL.TYPE_BY_COUNTRIES:
                array = switch (url.countTokens()) {
                case 0 -> getCountries();
                case 1 -> getDestinations(url.nextElement());
                case 2 -> getTransferServers(url.nextElement(2));
                case 3 -> {
                    final var destination = url.nextElement(2);
                    yield getHosts(destination, url.nextElement());
                }
                default -> {
                    final var serverName = url.nextElement(3);
                    final var hostName = url.nextElement();
                    yield toArray(getMoverElement(serverName, hostName, url.nextElements()).listCurrent());
                }
                };
                break;
            case FtpURL.TYPE_BY_HOSTS:
                array = switch (url.countTokens()) {
                case 0 -> getTransferServers();
                case 1 -> getHosts(url.nextElement());
                default -> {
                    final var serverName = url.nextElement();
                    final var hostName = url.nextElement();
                    yield toArray(getMoverElement(serverName, hostName, url.nextElements()).listCurrent());
                }
                };
                break;
            case FtpURL.TYPE_BY_ATTACHMENTS:
                if (url.countTokens() == 0) {
                    array = getDestinations();
                } else {
                    final var destinationName = url.nextElement();
                    array = toArray(getAttachmentElement(destinationName, url.nextElements()).listCurrent());
                }
                break;
            case FtpURL.TYPE_BY_TYPES:
                if (url.countTokens() == 0) {
                    array = getTypes();
                } else {
                    final var type = url.nextElement();
                    array = toArray(getAttachmentElement(type, url.nextElements()).listCurrent());
                }
                break;
            case FtpURL.TYPE_BY_DATA:
                if (url.countTokens() == 0) {
                    array = getDestinations();
                } else {
                    final var destinationName = url.nextElement();
                    array = toArray(getDataElement(destinationName, url.nextElements()).listCurrent());
                }
                break;
            default:
                throw new EccmdException("Not a registred domain: " + url.getDomain());
            }
            return array.toArray(new FileListElement[array.size()]);
        }

        /**
         * Gets the file list element.
         *
         * @param path
         *            the path
         *
         * @return the file list element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public FileListElement getFileListElement(final String path) throws EccmdException, IOException {
            return getFileListElement(new FtpURL(path), path);
        }

        /**
         * Gets the file last modified.
         *
         * @param source
         *            the source
         *
         * @return the file last modified
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public long getFileLastModified(final String source) throws EccmdException, IOException {
            _log.debug("getFileLastModified({})", source);
            return master.getStartDate().getTime();
        }

        /**
         * Gets the file size.
         *
         * @param source
         *            the source
         *
         * @return the file size
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public long getFileSize(final String source) throws EccmdException, IOException {
            _log.debug("getFileSize({})", source);
            final var element = getFileListElement(new FtpURL(source), source);
            if (element.isDirectory()) {
                return 4096;
            }
            return getFileElement(source, true).size();
        }

        /**
         * Gets the proxy socket input.
         *
         * @param source
         *            the source
         * @param offset
         *            the offset
         *
         * @return the proxy socket input
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public ProxySocket getProxySocketInput(final String source, final long offset)
                throws EccmdException, IOException {
            _log.debug("getProxySocketInput({},{})", source, offset);
            return getFileElement(source, true).getProxySocketInput(offset);
        }

        /**
         * Gets the proxy socket input.
         *
         * @param source
         *            the source
         * @param offset
         *            the offset
         * @param length
         *            the length
         *
         * @return the proxy socket input
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public ProxySocket getProxySocketInput(final String source, final long offset, final long length)
                throws EccmdException, IOException {
            _log.debug("getProxySocketInput({},{})", source, offset);
            return getFileElement(source, true).getProxySocketInput(offset, length);
        }

        /**
         * Gets the proxy socket output.
         *
         * @param target
         *            the target
         * @param offset
         *            the offset
         * @param umask
         *            the umask
         *
         * @return the proxy socket output
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public ProxySocket getProxySocketOutput(final String target, final long offset, final int umask)
                throws EccmdException, IOException {
            _log.debug("getProxySocketOutput({},{},{})", target, offset, umask);
            return getFileElement(target, true).getProxySocketOutput(offset, umask);
        }

        /**
         * Delete file.
         *
         * @param source
         *            the source
         * @param force
         *            the force
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void deleteFile(final String source, final boolean force) throws EccmdException, IOException {
            _log.debug("deleteFile({})", source);
            getFileElement(source, false).delete(force);
        }

        /**
         * Mkdir.
         *
         * @param dir
         *            the dir
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void mkdir(final String dir) throws EccmdException, IOException {
            _log.debug("mkdir({})", dir);
            getFileElement(dir, false).mkdir();
        }

        /**
         * Rmdir.
         *
         * @param dir
         *            the dir
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void rmdir(final String dir) throws EccmdException, IOException {
            _log.debug("rmdir({})", dir);
            getFileElement(dir, false).rmdir();
        }

        /**
         * Move file.
         *
         * @param source
         *            the source
         * @param target
         *            the target
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void moveFile(final String source, final String target) throws EccmdException, IOException {
            _log.debug("moveFile({},{})", source, target);
            getFileElement(source, false).move(target);
        }

        /**
         * Check.
         *
         * @param proxy
         *            the proxy
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws EccmdException
         *             the eccmd exception
         */
        @Override
        public void check(final ProxySocket proxy) throws IOException, EccmdException {
            _log.debug("check({})", proxy.getTicket());
            ((FileElement) proxy.getObject(MasterProvider.class.getName())).check(proxy);
        }

        /**
         * The Class DataFileElement.
         */
        private final class DataFileElement extends FileElement {
            /** The currentDestination. */
            private final Destination currentDestination;

            /** The currentPath. */
            private final String currentPath;

            /**
             * Instantiates a new data file element.
             *
             * @param destination
             *            the destination
             * @param path
             *            the path
             */
            DataFileElement(final Destination destination, final String path) {
                currentDestination = destination;
                currentPath = path;
                final var ecuser = currentDestination.getECUser();
                if (ecuser != null) {
                    _log.debug(from);
                    ecuser.setComment(from);
                }
            }

            /**
             * Sets the.
             *
             * @param element
             *            the element
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void set(final FileListElement element) throws EccmdException, IOException {
                super.set(element);
                element.setName(currentDestination.getName() + "/" + currentPath);
            }

            /**
             * Gets the destination.
             *
             * @return the destination
             */
            Destination getDestination() {
                return currentDestination;
            }

            /**
             * Equals.
             *
             * @param element
             *            the element
             *
             * @return true, if successful
             */
            boolean sameAs(final FileElement element) {
                return element instanceof final DataFileElement dataFileElement
                        && currentDestination.getName().equals(dataFileElement.getDestination().getName());
            }

            /**
             * Gets the path.
             *
             * @return the path
             */
            @Override
            public String getPath() {
                return currentPath;
            }

            /**
             * To string.
             *
             * @return the string
             */
            @Override
            public String toString() {
                return "[" + currentDestination.getName() + "]" + currentPath;
            }

            /**
             * Gets the file last modified.
             *
             * @return the file last modified
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public long getFileLastModified() throws EccmdException, IOException {
                try {
                    return master.getDataFileAccessInterface().getFileLastModified(currentDestination.getName(),
                            getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Size.
             *
             * @return the long
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public long size() throws EccmdException, IOException {
                try {
                    return master.getDataFileAccessInterface().size(currentDestination.getName(), getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Gets the proxy socket input.
             *
             * @param offset
             *            the offset
             *
             * @return the proxy socket input
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketInput(final long offset) throws EccmdException, IOException {
                final ProxySocket proxy;
                try {
                    proxy = master.getDataFileAccessInterface().getProxySocketInput(currentDestination.getName(),
                            getPath(), offset);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Gets the proxy socket input.
             *
             * @param offset
             *            the offset
             * @param length
             *            the length
             *
             * @return the proxy socket input
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketInput(final long offset, final long length)
                    throws EccmdException, IOException {
                final ProxySocket proxy;
                try {
                    proxy = master.getDataFileAccessInterface().getProxySocketInput(currentDestination.getName(),
                            getPath(), offset, length);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Gets the proxy socket output.
             *
             * @param offset
             *            the offset
             * @param umask
             *            the umask
             *
             * @return the proxy socket output
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketOutput(final long offset, final int umask)
                    throws EccmdException, IOException {
                ProxySocket proxy;
                try {
                    proxy = master.getDataFileAccessInterface().getProxySocketOutput(currentDestination.getName(),
                            getPath(), offset, umask);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Delete.
             *
             * @param force
             *            the force
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void delete(final boolean force) throws EccmdException, IOException {
                try {
                    master.getDataFileAccessInterface().delete(currentDestination.getName(), getPath(), force);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Mkdir.
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void mkdir() throws EccmdException, IOException {
                try {
                    master.getDataFileAccessInterface().mkdir(currentDestination.getName(), getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Rmdir.
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void rmdir() throws EccmdException, IOException {
                try {
                    master.getDataFileAccessInterface().rmdir(currentDestination.getName(), getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * _list.
             *
             * @param files
             *            the files
             *
             * @return the string[]
             */
            private String[] list(final FileListElement[] files) {
                if (files == null) {
                    return new String[0];
                }
                final var result = new String[files.length];
                for (var i = 0; i < result.length; i++) {
                    result[i] = Format.getFtpList(files[i].getRight(), currentDestination.getECUserName(),
                            currentDestination.getCountryIso(), files[i].getSize(), files[i].getTime(),
                            files[i].getName());
                }
                return result;
            }

            /**
             * List current.
             *
             * @return the string[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public String[] listCurrent() throws EccmdException, IOException {
                try {
                    return list(master.getDataFileAccessInterface().list(currentDestination.getName(), getPath()));
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * List parent.
             *
             * @return the string[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public String[] listParent() throws EccmdException, IOException {
                try {
                    return list(master.getDataFileAccessInterface().list(currentDestination.getName(),
                            new File(getPath()).getParent()));
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Move.
             *
             * @param target
             *            the target
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void move(final String target) throws EccmdException, IOException {
                final var targetElement = getFileElement(target, false);
                if (!sameAs(targetElement)) {
                    throw new EccmdException("Move across domains/destinations not supported");
                }
                try {
                    master.getDataFileAccessInterface().move(currentDestination.getName(), getPath(),
                            targetElement.getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Check.
             *
             * @param proxy
             *            the proxy
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void check(final ProxySocket proxy) throws EccmdException, IOException {
                try {
                    master.getDataFileAccessInterface().check(proxy);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }
        }

        /**
         * The Class AttachmentElement.
         */
        private final class AttachmentElement extends FileElement {
            /** The currentDestinationNameOrType. */
            private final String currentDestinationNameOrType;

            /** The currentDestination. */
            private final Destination currentDestination;

            /** The currentPath. */
            private final String currentPath;

            /**
             * Instantiates a new attachment element.
             *
             * @param destinationNameOrType
             *            the destination name or type
             * @param path
             *            the path
             */
            AttachmentElement(final String destinationNameOrType, final String path) {
                currentDestinationNameOrType = destinationNameOrType;
                currentDestination = base.getDestinationObject(destinationNameOrType);
                currentPath = path;
            }

            /**
             * Sets the.
             *
             * @param element
             *            the element
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void set(final FileListElement element) throws EccmdException, IOException {
                super.set(element);
                element.setName(currentDestinationNameOrType + "/" + currentPath);
            }

            /**
             * Gets the destination.
             *
             * @return the destination
             */
            private String getDestinationNameOrType() {
                return currentDestinationNameOrType;
            }

            /**
             * Equals.
             *
             * @param element
             *            the element
             *
             * @return true, if successful
             */
            private boolean sameAs(final FileElement element) {
                return element instanceof final AttachmentElement attachmentElement
                        && currentDestinationNameOrType.equals(attachmentElement.getDestinationNameOrType());
            }

            /**
             * Gets the path.
             *
             * @return the path
             */
            @Override
            public String getPath() {
                return currentPath;
            }

            /**
             * To string.
             *
             * @return the string
             */
            @Override
            public String toString() {
                return "[" + currentDestinationNameOrType + "]" + currentPath;
            }

            /**
             * Gets the file last modified.
             *
             * @return the file last modified
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public long getFileLastModified() throws EccmdException, IOException {
                try {
                    return master.getAttachmentAccessInterface().getFileLastModified(currentDestinationNameOrType,
                            getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Size.
             *
             * @return the long
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public long size() throws EccmdException, IOException {
                try {
                    return master.getAttachmentAccessInterface().size(currentDestinationNameOrType, getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Gets the proxy socket input.
             *
             * @param offset
             *            the offset
             *
             * @return the proxy socket input
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketInput(final long offset) throws EccmdException, IOException {
                final ProxySocket proxy;
                try {
                    proxy = master.getAttachmentAccessInterface().getProxySocketInput(currentDestinationNameOrType,
                            getPath(), offset);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Gets the proxy socket input.
             *
             * @param offset
             *            the offset
             * @param length
             *            the length
             *
             * @return the proxy socket input
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketInput(final long offset, final long length)
                    throws EccmdException, IOException {
                final ProxySocket proxy;
                try {
                    proxy = master.getAttachmentAccessInterface().getProxySocketInput(currentDestinationNameOrType,
                            getPath(), offset, length);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Gets the proxy socket output.
             *
             * @param offset
             *            the offset
             * @param umask
             *            the umask
             *
             * @return the proxy socket output
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketOutput(final long offset, final int umask)
                    throws EccmdException, IOException {
                final ProxySocket proxy;
                try {
                    proxy = master.getAttachmentAccessInterface().getProxySocketOutput(currentDestinationNameOrType,
                            getPath(), offset, umask);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Delete.
             *
             * @param force
             *            the force
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void delete(final boolean force) throws EccmdException, IOException {
                try {
                    master.getAttachmentAccessInterface().delete(currentDestinationNameOrType, getPath(), force);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Mkdir.
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void mkdir() throws EccmdException, IOException {
                try {
                    master.getAttachmentAccessInterface().mkdir(currentDestinationNameOrType, getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Rmdir.
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void rmdir() throws EccmdException, IOException {
                try {
                    master.getAttachmentAccessInterface().rmdir(currentDestinationNameOrType, getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * list.
             *
             * @param files
             *            the files
             *
             * @return the string[]
             */
            private String[] list(final FileListElement[] files) {
                if (files == null) {
                    return new String[0];
                }
                final var result = new String[files.length];
                for (var i = 0; i < result.length; i++) {
                    result[i] = Format.getFtpList(files[i].getRight(),
                            currentDestination != null ? currentDestination.getECUserName()
                                    : DestinationOption.TYPE_USER_AND_GROUP,
                            currentDestination != null ? currentDestination.getCountryIso()
                                    : DestinationOption.TYPE_USER_AND_GROUP,
                            files[i].getSize(), files[i].getTime(), files[i].getName());
                }
                return result;
            }

            /**
             * List current.
             *
             * @return the string[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public String[] listCurrent() throws EccmdException, IOException {
                try {
                    return list(master.getAttachmentAccessInterface().list(currentDestinationNameOrType, getPath()));
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * List parent.
             *
             * @return the string[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public String[] listParent() throws EccmdException, IOException {
                try {
                    return list(master.getAttachmentAccessInterface().list(currentDestinationNameOrType,
                            new File(getPath()).getParent()));
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Move.
             *
             * @param target
             *            the target
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void move(final String target) throws EccmdException, IOException {
                final var targetElement = getFileElement(target, false);
                if (!sameAs(targetElement)) {
                    throw new EccmdException("Move across domains/destinations not supported");
                }
                try {
                    master.getAttachmentAccessInterface().move(currentDestinationNameOrType, getPath(),
                            targetElement.getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Check.
             *
             * @param proxy
             *            the proxy
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void check(final ProxySocket proxy) throws EccmdException, IOException {
                try {
                    master.getAttachmentAccessInterface().check(proxy);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }
        }

        /**
         * The Class MoverElement.
         */
        private final class MoverElement extends FileElement {
            /** The theServer. */
            final TransferServer theServer;

            /** The theHost. */
            final Host theHost;

            /** The thePath. */
            final String thePath;

            /**
             * Instantiates a new mover element.
             *
             * @param server
             *            the server
             * @param host
             *            the host
             * @param path
             *            the path
             */
            MoverElement(final TransferServer server, final Host host, final String path) {
                theServer = server;
                theHost = (Host) host.clone();
                theHost.setDir(getInteractiveDir(host));
                thePath = path;
            }

            /**
             * Sets the.
             *
             * @param element
             *            the element
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void set(final FileListElement element) throws EccmdException, IOException {
                super.set(element);
                element.setName(theServer.getName() + "/" + theHost.getName() + "/" + thePath);
            }

            /**
             * Gets the transfer server.
             *
             * @return the transfer server
             */
            TransferServer getTransferServer() {
                return theServer;
            }

            /**
             * Equals.
             *
             * @param element
             *            the element
             *
             * @return true, if successful
             */
            boolean sameAs(final FileElement element) {
                return element instanceof final MoverElement mover
                        && theServer.getName().equals(mover.getTransferServer().getName())
                        && theHost.getName().equals(mover.getHost().getName());
            }

            /**
             * Gets the host.
             *
             * @return the host
             */
            Host getHost() {
                return theHost;
            }

            /**
             * Gets the path.
             *
             * @return the path
             */
            @Override
            public String getPath() {
                return thePath;
            }

            /**
             * To string.
             *
             * @return the string
             */
            @Override
            public String toString() {
                return "[" + theServer.getName() + "][" + theHost.getName() + "]" + thePath;
            }

            /**
             * Gets the file last modified.
             *
             * @return the file last modified
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public long getFileLastModified() throws IOException {
                return master.getStartDate().getTime();
            }

            /**
             * Size.
             *
             * @return the long
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public long size() throws EccmdException, IOException {
                try {
                    return TransferScheduler.size(getTransferServer(), getHost(), getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Gets the proxy socket input.
             *
             * @param offset
             *            the offset
             *
             * @return the proxy socket input
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketInput(final long offset) throws EccmdException, IOException {
                final ProxySocket proxy;
                try {
                    proxy = TransferScheduler.get(getTransferServer(), getHost(), getPath(), offset, -1, false);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Gets the proxy socket input.
             *
             * @param offset
             *            the offset
             * @param length
             *            the length
             *
             * @return the proxy socket input
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketInput(final long offset, final long length)
                    throws EccmdException, IOException {
                final ProxySocket proxy;
                try {
                    proxy = TransferScheduler.get(getTransferServer(), getHost(), getPath(), offset, length, false);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Gets the proxy socket output.
             *
             * @param offset
             *            the offset
             * @param umask
             *            the umask
             *
             * @return the proxy socket output
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public ProxySocket getProxySocketOutput(final long offset, final int umask)
                    throws EccmdException, IOException {
                final ProxySocket proxy;
                try {
                    proxy = TransferScheduler.put(getTransferServer(), getHost(), getPath(), offset, -1);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(MasterProvider.class.getName(), this);
                return proxy;
            }

            /**
             * Delete.
             *
             * @param force
             *            the force
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void delete(final boolean force) throws EccmdException, IOException {
                try {
                    TransferScheduler.del(getTransferServer(), getHost(), getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Mkdir.
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void mkdir() throws EccmdException, IOException {
                try {
                    TransferScheduler.mkdir(getTransferServer(), getHost(), getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Rmdir.
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void rmdir() throws EccmdException, IOException {
                try {
                    TransferScheduler.rmdir(getTransferServer(), getHost(), getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * List current.
             *
             * @return the string[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public String[] listCurrent() throws EccmdException, IOException {
                try {
                    // Get the listing from the remote Host
                    final var files = TransferScheduler.list(getTransferServer(), getHost(), getPath(), null);
                    final List<String> result = new ArrayList<>();
                    // Use the configuration from the Host to try to analyse the
                    // listing output
                    final var setup = HOST_ACQUISITION.getECtransSetup(getHost().getData());
                    // For every entry convert it in the usual ftp format!
                    for (final FileEntry entry : FtpParser.parseDir(setup.getString(HOST_ACQUISITION_REGEX_FORMAT),
                            setup.getString(HOST_ACQUISITION_SYSTEM_KEY).toUpperCase(),
                            setup.getString(HOST_ACQUISITION_DEFAULT_DATE_FORMAT),
                            setup.getString(HOST_ACQUISITION_RECENT_DATE_FORMAT),
                            setup.getString(HOST_ACQUISITION_SERVER_LANGUAGE_CODE).toLowerCase(),
                            setup.getString(HOST_ACQUISITION_SHORT_MONTH_NAMES),
                            setup.getString(HOST_ACQUISITION_SERVER_TIME_ZONE_ID), files)) {
                        if (entry.permissions == null || entry.exception != null) {
                            // The list can not be parsed so let's send the
                            // original listing without modification!
                            _log.debug("Could not parse list (use original)", entry.exception);
                            return files;
                        }
                        result.add(Format.getFtpList(entry.permissions, isNotEmpty(entry.user) ? entry.user : "none",
                                isNotEmpty(entry.group) ? entry.group : "none", String.valueOf(entry.size), entry.time,
                                entry.name));
                    }
                    return result.toArray(new String[result.size()]);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * List parent.
             *
             * @return the string[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public String[] listParent() throws EccmdException, IOException {
                try {
                    return TransferScheduler.list(getTransferServer(), getHost(), new File(getPath()).getParent(),
                            null);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Move.
             *
             * @param target
             *            the target
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void move(final String target) throws EccmdException, IOException {
                final var targetElement = getFileElement(target, false);
                if (!sameAs(targetElement)) {
                    throw new EccmdException("Move across domains/hosts not supported");
                }
                try {
                    TransferScheduler.move(getTransferServer(), getHost(), getPath(), targetElement.getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * Check.
             *
             * @param proxy
             *            the proxy
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public void check(final ProxySocket proxy) throws EccmdException, IOException {
                try {
                    TransferScheduler.check(getTransferServer(), proxy.getTicket());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }
        }
    }

    /**
     * The Class FileElement.
     */
    private abstract static class FileElement {
        /**
         * Gets the path.
         *
         * @return the path
         */
        public abstract String getPath();

        /**
         * List parent.
         *
         * @return the string[]
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract String[] listParent() throws EccmdException, IOException;

        /**
         * List current.
         *
         * @return the string[]
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract String[] listCurrent() throws EccmdException, IOException;

        /**
         * Move.
         *
         * @param target
         *            the target
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract void move(String target) throws EccmdException, IOException;

        /**
         * Rmdir.
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract void rmdir() throws EccmdException, IOException;

        /**
         * Mkdir.
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract void mkdir() throws EccmdException, IOException;

        /**
         * Delete.
         *
         * @param force
         *            the force
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract void delete(boolean force) throws EccmdException, IOException;

        /**
         * Gets the proxy socket input.
         *
         * @param offset
         *            the offset
         *
         * @return the proxy socket input
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract ProxySocket getProxySocketInput(long offset) throws EccmdException, IOException;

        /**
         * Gets the proxy socket input.
         *
         * @param offset
         *            the offset
         * @param length
         *            the length
         *
         * @return the proxy socket input
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract ProxySocket getProxySocketInput(long offset, long length) throws EccmdException, IOException;

        /**
         * Gets the file last modified.
         *
         * @return the file last modified
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract long getFileLastModified() throws EccmdException, IOException;

        /**
         * Gets the proxy socket output.
         *
         * @param offset
         *            the offset
         * @param umask
         *            the umask
         *
         * @return the proxy socket output
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract ProxySocket getProxySocketOutput(long offset, int umask) throws EccmdException, IOException;

        /**
         * Check.
         *
         * @param proxy
         *            the proxy
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract void check(ProxySocket proxy) throws EccmdException, IOException;

        /**
         * Size.
         *
         * @return the long
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract long size() throws EccmdException, IOException;

        /**
         * Sets the.
         *
         * @param element
         *            the element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public void set(final FileListElement element) throws EccmdException, IOException {
            final var list = listParent();
            var directory = false;
            if (list != null) {
                final var file = new File(getPath());
                for (final String line : list) {
                    if (line.endsWith(" " + file.getName()) || line.endsWith("\t" + file.getName())) {
                        directory = line.startsWith("d");
                        break;
                    }
                }
            }
            if (!directory) {
                element.setRight("-rwxr-x---");
            }
        }
    }
}
