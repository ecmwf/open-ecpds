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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_INCOMING;
import static ecmwf.common.ectrans.ECtransGroups.Module.USER_PORTAL;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Destination;
import ecmwf.common.database.IncomingConnection;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ecaccess.UserSession;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.SessionCache;
import ecmwf.ecpds.master.IncomingProfile;
import ecmwf.ecpds.master.MasterException;

/**
 * The Class MoverProvider.
 */
public final class MoverProvider extends NativeAuthenticationProvider {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MoverProvider.class);

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /** The Constant _dataSpaces. */
    private static final List<UserDataSpace> _dataSpaces = Collections.synchronizedList(new ArrayList<>());

    /** The Constant _fileElementCache. */
    private static final FileElementCache _fileElementCache = new FileElementCache();

    /** The Constant _fileListElementCache. */
    private static final FileListElementCache _fileListElementCache = new FileListElementCache();

    /** The Constant _cacheTimeout. */
    private static final long _cacheTimeout = Cnf.at("MoverProvider", "cacheTimeout", 15 * Timer.ONE_MINUTE);

    /**
     * Adds the.
     *
     * @param dataSpace
     *            the data space
     */
    private static void _add(final UserDataSpace dataSpace) {
        _log.debug("Adding " + dataSpace.getId() + "=" + dataSpace.toString());
        _dataSpaces.add(dataSpace);
    }

    /**
     * Removes the.
     *
     * @param dataSpace
     *            the data space
     */
    private static void _remove(final UserDataSpace dataSpace) {
        final var removed = _dataSpaces.remove(dataSpace);
        _log.debug("Removing " + dataSpace.getId() + "=" + dataSpace.toString() + " (success=" + removed + ")");
    }

    /**
     * Close all incoming connections.
     */
    public static void closeAllIncomingConnections() {
        synchronized (_dataSpaces) {
            for (final UserDataSpace dataSpace : _dataSpaces) {
                final var currentId = dataSpace.getId();
                final var closeable = dataSpace._closeable;
                _log.debug("Closing " + currentId + "=" + dataSpace.toString() + " (closing " + closeable + ")");
                try {
                    _forceCloseOfDataSpace(dataSpace, closeable);
                } catch (final Throwable t) {
                    _log.warn("Could not close session " + currentId, t);
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
        if (id != null && id.length() > 0) {
            synchronized (_dataSpaces) {
                for (final UserDataSpace dataSpace : _dataSpaces) {
                    if (id.equals(dataSpace.getId())) {
                        final var closeable = dataSpace._closeable;
                        _log.debug("Closing " + id + "=" + dataSpace.toString() + " (closing " + closeable + ")");
                        try {
                            _forceCloseOfDataSpace(dataSpace, closeable);
                            return true;
                        } catch (final Throwable t) {
                            _log.warn("Could not close session " + id, t);
                        }
                        // No need to continue as we found it already!
                        return false;
                    }
                }
            }
        }
        _log.warn("Could not find session " + id);
        return false;
    }

    /**
     * Force close of the data space.
     *
     * @param dataSpace
     *            the data space
     * @param closeable
     *            the closeable
     */
    public static void _forceCloseOfDataSpace(final UserDataSpace dataSpace, final Closeable closeable) {
        if (closeable != null) {
            // The closing of the Closeable Object should cause the closing of the
            // UserDataSpace and the removing from the list.
            try {
                closeable.close();
                _log.debug("Incoming session closed for " + dataSpace.getUser());
            } catch (final Throwable t) {
                // Close failed so let's force the closure of the dataspace.
                _log.warn("Close failed, removing session", t);
                _remove(dataSpace);
            }
        } else {
            // Nothing to be closed so we should remove it from the list immediately! This
            // should not happen.
            _log.warn("Nothing to close, removing session");
            _remove(dataSpace);
        }
    }

    /**
     * Gets the incoming connection ids.
     *
     * @return the incoming connection ids
     */
    public static String[] getIncomingConnectionIds() {
        final List<String> result = new ArrayList<>();
        synchronized (_dataSpaces) {
            for (final UserDataSpace dataSpace : _dataSpaces) {
                result.add(dataSpace.getId() + "=" + dataSpace.toString());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Gets the incoming connections.
     *
     * @return the incoming connections
     */
    public static List<IncomingConnection> getIncomingConnections() {
        final List<IncomingConnection> result = new ArrayList<>();
        synchronized (_dataSpaces) {
            for (final UserDataSpace dataSpace : _dataSpaces) {
                result.add(dataSpace.getIncomingConnection());
            }
        }
        return result;
    }

    /**
     * The Class FileElementCache.
     */
    private static final class FileElementCache extends SessionCache<String, FileElement> {
        /**
         * Instantiates a new file element cache.
         */
        public FileElementCache() {
            setDebug(Cnf.at("MoverProvider", "debug", false));
        }

        /**
         * Disconnect.
         *
         * @param session
         *            the session
         */
        @Override
        public void disconnect(final FileElement session) {
        }

        /**
         * Checks if is connected.
         *
         * @param session
         *            the session
         *
         * @return true, if is connected
         */
        @Override
        public boolean isConnected(final FileElement session) {
            return true;
        }
    }

    /**
     * The Class FileListElementCache.
     */
    private static final class FileListElementCache extends SessionCache<String, FileListElement> {
        /**
         * Instantiates a new file list element cache.
         */
        public FileListElementCache() {
            setDebug(Cnf.at("MoverProvider", "debug", false));
        }

        /**
         * Disconnect.
         *
         * @param session
         *            the session
         */
        @Override
        public void disconnect(final FileListElement session) {
        }

        /**
         * Checks if is connected.
         *
         * @param session
         *            the session
         *
         * @return true, if is connected
         */
        @Override
        public boolean isConnected(final FileListElement session) {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is registred user.
     */
    @Override
    public boolean isRegistredUser(final String user) throws Exception {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user session.
     */
    @Override
    public UserSession getUserSession(final String host, final String user, final String password, final String profile,
            final Closeable closeable) throws Exception {
        final var from = "Using " + profile + " on DataMover=" + _mover.getRoot() + " from " + user + "@" + host;
        return new UserDataSpace(_mover.getMasterProxy().getIncomingProfile(user, password, from), from, profile, host,
                closeable);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user session.
     */
    @Override
    public UserSession getUserSession(final String host, final String user, final ECauthToken token,
            final String profile, final Closeable closeable) throws Exception {
        throw new Exception("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user session.
     */
    @Override
    public UserSession getUserSession(final String host, final String ticket, final String profile,
            final Closeable closeable) throws Exception {
        throw new Exception("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Support tickets.
     */
    @Override
    public boolean supportTickets() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is password.
     */
    @Override
    public boolean isPassword(final String user, final String password) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the root.
     */
    @Override
    public String getRoot() throws RemoteException {
        return _mover.getRoot();
    }

    /**
     * The Class FtpURL.
     */
    private static final class FtpURL {
        /** The Constant TYPE_UNKNOWN. */
        static final int TYPE_UNKNOWN = -1;

        /** The Constant TYPE_BY_DATA. */
        static final int TYPE_BY_DATA = 4;

        /** The _domain. */
        private final String _domain;

        /** The _tokenizer. */
        private final StringTokenizer _tokenizer;

        /**
         * Instantiates a new ftp url.
         *
         * @param defaultDestination
         *            the default destination
         * @param url
         *            the url
         */
        FtpURL(final String defaultDestination, final String url) {
            final var user = url.indexOf("]");
            final var domain = url.indexOf(":");
            _domain = url.substring(user + 1, domain);
            _tokenizer = new StringTokenizer(
                    (defaultDestination != null ? defaultDestination : "") + url.substring(domain + 1), "/");
        }

        /**
         * Gets the domain.
         *
         * @return the domain
         */
        String getDomain() {
            return _domain;
        }

        /**
         * Count tokens.
         *
         * @return the int
         */
        int countTokens() {
            return _tokenizer.countTokens();
        }

        /**
         * Next element.
         *
         * @return the string
         */
        String nextElement() {
            return _tokenizer.nextToken();
        }

        /**
         * Next elements.
         *
         * @return the string
         */
        String nextElements() {
            final var result = new StringBuilder();
            while (_tokenizer.hasMoreElements()) {
                result.append(result.length() == 0 ? "" : "/").append(nextElement());
            }
            return result.toString();
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        int getType() {
            if ("data".equalsIgnoreCase(_domain)) {
                return TYPE_BY_DATA;
            }
            return TYPE_UNKNOWN;
        }
    }

    /**
     * The Class UserDataSpace.
     */
    public final class UserDataSpace extends UserSession {
        /** The _from. */
        private final String _from;

        /** The _protocol. */
        private final String _protocol;

        /** The _host. */
        private final String _host;

        /** The _closeable. */
        private final Closeable _closeable;

        /** The _profile. */
        private final IncomingProfile _profile;

        /** The _default destination. */
        private final String _defaultDestination;

        /** The _welcome. */
        private final int _sort;

        /** The _welcome. */
        private final int _order;

        /** The _welcome. */
        private final long _start = System.currentTimeMillis();

        /** The _welcome. */
        private final String _id;

        /** The setup. */
        private final ECtransSetup _setup;

        /**
         * Instantiates a new user data space.
         *
         * @param profile
         *            the profile
         * @param from
         *            the from
         * @param protocol
         *            the protocol
         * @param host
         *            the host
         * @param closeable
         *            the closeable
         *
         * @throws Exception
         *             the exception
         */
        UserDataSpace(final IncomingProfile profile, final String from, final String protocol, final String host,
                final Closeable closeable) throws Exception {
            super(profile.getIncomingUser().getId(), profile.getIncomingUser().getId());
            _id = _mover.getRoot() + "_" + this.hashCode();
            _profile = profile;
            _from = from;
            _protocol = protocol;
            _host = host;
            _closeable = closeable;
            _setup = USER_PORTAL.getECtransSetup(_profile.getIncomingUser().getData());
            if (Arrays.asList(_setup.getList("excludeProtocols")).contains(protocol)) {
                throw new EccmdException("Protocol " + protocol + " not allowed for " + getUser());
            }
            _defaultDestination = _setup.getString(ECtransOptions.USER_PORTAL_DESTINATION);
            final var sort = _setup.getString(ECtransOptions.USER_PORTAL_SORT);
            _sort = "size".equals(sort) ? 1 : "target".equals(sort) ? 2 : "time".equals(sort) ? 3 : -1;
            final var order = _setup.getString(ECtransOptions.USER_PORTAL_ORDER);
            _order = "asc".equals(order) ? 1 : "desc".equals(order) ? 2 : -1;
            _add(this);
        }

        /**
         * Close.
         *
         * @param remove
         *            the remove
         */
        @Override
        public void close(final boolean remove) {
            _log.debug("Closing DataSpace");
            _remove(this);
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {
            return _id;
        }

        /**
         * Gets the string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return _protocol + ":" + getUser() + "@" + _host;
        }

        /**
         * Gets the incoming connection.
         *
         * @return the incoming connection
         */
        public IncomingConnection getIncomingConnection() {
            final var connection = new IncomingConnection();
            connection.setId(_id);
            connection.setLogin(getUser());
            connection.setDataMoverName(_mover.getRoot());
            connection.setProtocol(_protocol);
            connection.setRemoteIpAddress(_host);
            connection.setStartTime(_start);
            return connection;
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
         * Gets the default domain.
         *
         * @return the default domain
         */
        @Override
        public String getDefaultDomain() {
            return _setup.getString(ECtransOptions.USER_PORTAL_DOMAIN);
        }

        /**
         * Gets the welcome.
         *
         * @return the welcome
         */
        @Override
        public String getWelcome() {
            return _setup.getString(ECtransOptions.USER_PORTAL_WELCOME);
        }

        /**
         * Gets the ectrans setup.
         *
         * @return the ectrans setup
         */
        @Override
        public ECtransSetup getECtransSetup() {
            return _setup;
        }

        /**
         * Gets the destinations.
         *
         * @param options
         *            the options
         *
         * @return the vector
         */
        private List<FileListElement> _getDestinations(final String options) {
            final List<FileListElement> vector = new ArrayList<>();
            for (final Destination destination : _profile.getDestinations()) {
                if (destination.getActive()) {
                    final var setup = DESTINATION_INCOMING.getECtransSetup(destination.getData());
                    final var destinationName = setup.get(ECtransOptions.DESTINATION_INCOMING_ROOT_DIR,
                            destination.getName());
                    final var update = destination.getUpdate();
                    final var element = new FileListElement();
                    element.setComment(destination.getComment());
                    element.setName(destinationName);
                    element.setPath(destinationName);
                    element.setTime(update != null ? update.getTime() : _mover.getStartDate().getTime());
                    if (options != null && options.indexOf("n") != -1) {
                        element.setUser("101");
                        element.setGroup("1001");
                    } else {
                        element.setUser(getUser());
                        element.setGroup(destination.getCountryIso());
                    }
                    element.setRight("drwxr-x---");
                    element.setSize("2048");
                    vector.add(element);
                }
            }
            // sort: 1=DAT_SIZE, 2=DAT_TARGET, 3=DAT_SCHEDULED_TIME
            // order: descending=2, ascending=1
            vector.sort((d1, d2) -> {
                return switch (_sort) {
                case 3:
                    yield _order == 1 ? Long.compare(d1.getTime(), d2.getTime())
                            : Long.compare(d2.getTime(), d1.getTime());
                default: // No size available for a destination!
                    yield _order == 1 ? d1.getName().compareTo(d2.getName()) : d2.getName().compareTo(d1.getName());
                };
            });
            return vector;
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
        private FileElement _getFileElement(final String path, final boolean fileOnly)
                throws EccmdException, IOException {
            var fileElement = _fileElementCache.retrieve(path);
            if (fileElement != null) {
                return fileElement;
            }
            final var url = new FtpURL(_defaultDestination, path);
            final var element = _getFileListElement(url, path);
            if (fileOnly && element.isDirectory()) {
                throw new EccmdException("Invalid request for a directory");
            }
            if (element.getName() == null) {
                _log.debug("FileNotFoundException: " + path);
                throw new FileNotFoundException(path);
            }
            switch (url.getType()) {
            case FtpURL.TYPE_BY_DATA:
                final var tokenizer = new StringTokenizer(element.getName(), "/");
                if (tokenizer.countTokens() < 1) {
                    throw new EccmdException("Invalid source/target name");
                }
                final var rootdir = tokenizer.nextToken();
                final var dir = new StringBuilder();
                while (tokenizer.hasMoreElements()) {
                    dir.append(dir.length() == 0 ? "" : "/").append(tokenizer.nextToken());
                }
                fileElement = _getDataElement(rootdir, dir.toString(), "");
                ((DataFileElement) fileElement)._element = element;
                _fileElementCache.put(path, fileElement, _cacheTimeout);
                return fileElement;
            default:
                throw new EccmdException("Not a registred domain: " + url.getDomain());
            }
        }

        /**
         * Gets the data element.
         *
         * @param rootdir
         *            the rootdir
         * @param path
         *            the path
         * @param options
         *            the options
         *
         * @return the data file element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private DataFileElement _getDataElement(final String rootdir, final String path, final String options)
                throws EccmdException, IOException {
            return new DataFileElement(_checkDestination(rootdir), path.startsWith("/") ? path.substring(1) : path);
        }

        /**
         * Gets the destination root dir. Return the name to display as a rootdir for this Destination. The default
         * rootdir is the name of the Destination.
         *
         * @param destination
         *            the destination
         *
         * @return the string
         */
        private String _getDestinationRootDir(final Destination destination) {
            return DESTINATION_INCOMING.getECtransSetup(destination.getData())
                    .get(ECtransOptions.DESTINATION_INCOMING_ROOT_DIR, destination.getName());
        }

        /**
         * _check destination.
         *
         * @param rootdir
         *            the rootdir
         *
         * @return the destination
         *
         * @throws EccmdException
         *             the eccmd exception
         */
        private Destination _checkDestination(final String rootdir) throws EccmdException {
            for (final Destination destination : _profile.getDestinations()) {
                if (destination.getActive() && _getDestinationRootDir(destination).equals(rootdir)) {
                    return destination;
                }
            }
            throw new EccmdException("Destination not found for " + rootdir);
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
        private FileListElement _getFileListElement(final FtpURL url, final String path)
                throws EccmdException, IOException {
            var fileListElement = _fileListElementCache.retrieve(path);
            if (fileListElement != null) {
                return fileListElement;
            }
            fileListElement = new FileListElement();
            fileListElement.setRight("drwxr-x---");
            String rootdir = null;
            var cache = true;
            switch (url.getType()) {
            case FtpURL.TYPE_BY_DATA:
                switch (url.countTokens()) {
                case 0:
                    break;
                case 1:
                    _checkDestination(rootdir = url.nextElement());
                    break;
                default:
                    _checkDestination(rootdir = url.nextElement());
                    final var dfe = _getDataElement(rootdir, url.nextElements(), "");
                    cache = dfe.set(fileListElement) != null;
                }
                break;
            default:
                throw new EccmdException("Not a registred domain: " + url.getDomain());
            }
            if (cache) {
                _fileListElementCache.put(path, fileListElement, _cacheTimeout);
            }
            return fileListElement;
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
            final List<FileListElement> list = new ArrayList<>();
            _log.debug("getFileList(" + path + ")");
            _profile.checkPermission("dir", path);
            final var url = new FtpURL(_defaultDestination, path);
            switch (url.getType()) {
            case FtpURL.TYPE_BY_DATA:
                switch (url.countTokens()) {
                case 0:
                    list.addAll(_getDestinations(options));
                    break;
                default:
                    final var rootdir = url.nextElement();
                    final var elements = _getDataElement(rootdir, url.nextElements(), options).listCurrent();
                    final var tokenizer = new StringTokenizer(path, "/");
                    if (tokenizer.countTokens() >= 3) {
                        // Add the entry to the cache!
                        final var domain = tokenizer.nextToken();
                        tokenizer.nextToken(); // Destination name
                        final var dir = tokenizer.nextToken("\0");
                        final var key = domain + "/" + rootdir + dir;
                        for (final FileListElement element : elements) {
                            if (!element.isDirectory()) {
                                final var copy = (FileListElement) element.clone();
                                final var name = key + "/" + copy.getName();
                                copy.setName(rootdir + "/" + dir + "/" + copy.getName());
                                _fileListElementCache.put(name, copy, _cacheTimeout);
                            }
                        }
                    }
                    return elements;
                }
                break;
            default:
                throw new EccmdException("Not a registred domain: " + url.getDomain());
            }
            return list.toArray(new FileListElement[list.size()]);
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
            return getFileList(path, "");
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
            return _getFileListElement(new FtpURL(_defaultDestination, path), path);
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
            _log.debug("getFileLastModified(" + source + ")");
            _profile.checkPermission("mtime", source);
            return _getFileElement(source, true).getFileLastModified();
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
            _log.debug("getFileSize(" + source + ")");
            _profile.checkPermission("size", source);
            final var element = _getFileListElement(new FtpURL(_defaultDestination, source), source);
            if (element.isDirectory()) {
                return 4096;
            }
            return _getFileElement(source, true).size();
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
            _log.debug("getProxySocketInput(" + source + "," + offset + ")");
            _profile.checkPermission("get", source);
            final var result = _getFileElement(source, true).getProxySocketInput(offset);
            result.setSocketConfig(new SocketConfig("ECproxyPlugin"));
            return result;
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
            _log.debug("getProxySocketInput(" + source + "," + offset + ")");
            _profile.checkPermission("get", source);
            final var result = _getFileElement(source, true).getProxySocketInput(offset, length);
            result.setSocketConfig(new SocketConfig("ECproxyPlugin"));
            return result;
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
            _log.debug("getProxySocketOutput(" + target + "," + offset + "," + umask + ")");
            _profile.checkPermission("put", target);
            final var index = target.lastIndexOf("/");
            if (index > 0) {
                // Let's check if the path already exists? If it does not then we have to check
                // if the user is allowed to do an mkdir, as it would be implicitly created!
                final var dir = target.substring(0, index);
                if (!_getFileListElement(new FtpURL(_defaultDestination, dir), dir).isDirectory()) {
                    _profile.checkPermission("mkdir", dir);
                }
            }
            final var result = _getFileElement(target, true).getProxySocketOutput(offset, umask);
            result.setSocketConfig(new SocketConfig("ECproxyPlugin"));
            return result;
        }

        /**
         * Chmod.
         *
         * @param mode
         *            the mode
         * @param path
         *            the path
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void chmod(final int mode, final String path) throws EccmdException, IOException {
            // Let's ignore this request as it does not make sense in the Data Store!
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
            _log.debug("deleteFile(" + source + ")");
            _profile.checkPermission("delete", source);
            final var element = _getFileElement(source, false);
            _fileListElementCache.remove(source);
            _fileElementCache.remove(source);
            element.delete(force);
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
            _log.debug("mkdir(" + dir + ")");
            _profile.checkPermission("mkdir", dir);
            _getFileElement(dir, false).mkdir();
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
            _log.debug("rmdir(" + dir + ")");
            _profile.checkPermission("rmdir", dir);
            final var element = _getFileElement(dir, false);
            _fileListElementCache.remove(dir);
            _fileElementCache.remove(dir);
            element.rmdir();
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
            _log.debug("moveFile(" + source + "," + target + ")");
            _profile.checkPermission("rename", source);
            _profile.checkPermission("rename", target);
            final var sourceElement = _getFileElement(source, true);
            if (!(sourceElement instanceof final DataFileElement fileElement)) {
                throw new EccmdException("Source file not a plain file");
            }
            final var url = new FtpURL(_defaultDestination, target);
            switch (url.getType()) {
            case FtpURL.TYPE_BY_DATA:
                // This is a file in the data domain!
                if (url.countTokens() < 1) {
                    // The target is not a proper file!
                    throw new EccmdException("Target file not a plain file");
                }
                // We have the destination!
                final var destination = url.nextElement();
                final var currentDestination = fileElement._destination.getName();
                // Is it the same destination as the source?
                if (currentDestination.equals(destination)) {
                    _fileListElementCache.remove(source);
                    _fileElementCache.remove(source);
                    sourceElement.move(url.nextElements());
                } else {
                    // We don't move files between different destinations!
                    throw new EccmdException("Different source and target destinations (" + destination + "!="
                            + currentDestination + ")");
                }
                break;
            default:
                // What domain is this?
                throw new EccmdException("Not a registred domain: " + url.getDomain());
            }
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
            _log.debug("check(" + proxy.getTicket() + ")");
            final var key = FileElement.class.getName();
            final var fileElement = (FileElement) proxy.getObject(key);
            proxy.addObject(key, null);
            fileElement.check(proxy);
        }

        /**
         * The Class DataFileElement.
         */
        private final class DataFileElement extends FileElement {
            /** The _destination. */
            private final Destination _destination;

            /** The _element. */
            private FileListElement _element = null;

            /** The _path. */
            private final String _path;

            /** The _sort. */
            private final int _sort;

            /** The _order. */
            private final int _order;

            /**
             * Instantiates a new data file element.
             *
             * @param destination
             *            the destination
             * @param path
             *            the path
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            DataFileElement(final Destination destination, final String path) throws EccmdException, IOException {
                _destination = destination;
                _path = path;
                final var ecuser = _destination.getECUser();
                if (ecuser != null) {
                    ecuser.setComment(_from);
                }
                final var setup = DESTINATION_INCOMING.getECtransSetup(_destination.getData());
                final var sort = setup.getString(ECtransOptions.DESTINATION_INCOMING_SORT);
                _sort = "size".equals(sort) ? 1
                        : "target".equals(sort) ? 2 : "time".equals(sort) ? 3 : UserDataSpace.this._sort;
                final var order = setup.getString(ECtransOptions.DESTINATION_INCOMING_ORDER);
                _order = "asc".equals(order) ? 1 : "desc".equals(order) ? 2 : UserDataSpace.this._order;
                // If the grouping is by date we need to check if the date is valid and exists?
                if (destination.getGroupByDate() && path.length() > 0) {
                    _list(new StringTokenizer(path, "/").nextToken());
                }
            }

            /**
             * Sets the.
             *
             * @param element
             *            the element
             *
             * @return the file list element
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public FileListElement set(final FileListElement element) throws EccmdException, IOException {
                final var fle = super.set(_element = element);
                // If the name is given by its DataTransferId then lets replace it by its real
                // name returned from the Master Server
                element.setName(_getDestinationRootDir(_destination) + "/"
                        + (_path.startsWith("DataTransferId=") ? fle.getName() : _path));
                return fle;
            }

            /**
             * Gets the path.
             *
             * @return the path
             */
            @Override
            public String getPath() {
                return _path;
            }

            /**
             * Gets the data transfer id or path.
             *
             * @return the data transfer id or path
             */
            public String getDataTransferIdOrPath() {
                return _element != null && !_element.isDirectory() && _element.getComment() != null
                        ? "DataTransferId=" + _element.getComment() : _path;
            }

            /**
             * To string.
             *
             * @return the string
             */
            @Override
            public String toString() {
                return "[" + _destination.getName() + "]" + _path;
            }

            /**
             * Gets the.
             *
             * @return the file list element
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public FileListElement get() throws EccmdException, IOException {
                try {
                    return _mover.getDataFileAccessInterface().get(_destination.getName(), _path);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
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
                    return _mover.getDataFileAccessInterface().getFileLastModified(_destination.getName(),
                            getDataTransferIdOrPath());
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
                    return _mover.getDataFileAccessInterface().size(_destination.getName(), getDataTransferIdOrPath());
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
                    proxy = _mover.getDataFileAccessInterface().getProxySocketInput(_destination.getName(),
                            getDataTransferIdOrPath(), offset);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(FileElement.class.getName(), this);
                proxy.addObject(IncomingProfile.class.getName(), _profile);
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
                    proxy = _mover.getDataFileAccessInterface().getProxySocketInput(_destination.getName(),
                            getDataTransferIdOrPath(), offset, length);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(FileElement.class.getName(), this);
                proxy.addObject(IncomingProfile.class.getName(), _profile);
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
                    proxy = _mover.getDataFileAccessInterface().getProxySocketOutput(_destination.getName(), getPath(),
                            offset, umask);
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
                proxy.addObject(FileElement.class.getName(), this);
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
                    _mover.getDataFileAccessInterface().delete(_destination.getName(), getDataTransferIdOrPath(),
                            force);
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
                    _mover.getDataFileAccessInterface().mkdir(_destination.getName(), getPath());
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
                    _mover.getDataFileAccessInterface().rmdir(_destination.getName(), getPath());
                } catch (final MasterException e) {
                    throw new EccmdException(e.getMessage());
                }
            }

            /**
             * List current.
             *
             * @return the file list element[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public FileListElement[] listCurrent() throws EccmdException, IOException {
                return _list(getPath());
            }

            /**
             * List parent.
             *
             * @return the file list element[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            @Override
            public FileListElement[] listParent() throws EccmdException, IOException {
                return _list(new File(getPath()).getParent());
            }

            /**
             * List.
             *
             * @param path
             *            the path
             *
             * @return the file list element[]
             *
             * @throws EccmdException
             *             the eccmd exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private FileListElement[] _list(final String path) throws EccmdException, IOException {
                try {
                    return _mover.getDataFileAccessInterface().list(_destination.getName(), path, _sort, _order);
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
                try {
                    _mover.getDataFileAccessInterface().move(_destination.getName(), getDataTransferIdOrPath(), target);
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
                    _mover.getDataFileAccessInterface().check(proxy);
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
         * @return the file list element[]
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract FileListElement[] listParent() throws EccmdException, IOException;

        /**
         * List current.
         *
         * @return the file list element[]
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract FileListElement[] listCurrent() throws EccmdException, IOException;

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
         * Gets the.
         *
         * @return the file list element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public abstract FileListElement get() throws EccmdException, IOException;

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
         * @return the file list element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public FileListElement set(final FileListElement element) throws EccmdException, IOException {
            var directory = false;
            final var fle = get();
            if (fle != null && !(directory = fle.getRight().startsWith("d"))) {
                element.setComment(fle.getComment());
                element.setSize(fle.getSize());
                element.setTime(fle.getTime());
            }
            if (!directory) {
                element.setRight("-rwxr-x---");
            }
            return fle;
        }
    }
}
