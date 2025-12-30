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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.management.timer.Timer;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ecmwf.common.callback.CallBackObject;
import ecmwf.common.callback.RemoteInputStream;
import ecmwf.common.database.Association;
import ecmwf.common.database.Category;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.IncomingAssociation;
import ecmwf.common.database.IncomingPolicy;
import ecmwf.common.database.IncomingUser;
import ecmwf.common.database.MonitoringValue;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferMethod;
import ecmwf.common.database.TransferServer;
import ecmwf.common.database.Url;
import ecmwf.common.database.WebUser;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.plugin.PluginEvent;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ExecutorManager;
import ecmwf.common.technical.ExecutorRunnable;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferServerProvider;

/**
 * The Class ManagementImpl.
 */
final class ManagementImpl extends CallBackObject implements ManagementInterface {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 580759094520010493L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(ManagementImpl.class);

    /** The master. */
    private final transient MasterServer master;

    /** The base. */
    private final transient ECpdsBase base;

    /** The destination caches. */
    private final transient Map<String, DestinationCache> destinationCaches = new ConcurrentHashMap<>();

    /** The destination caches last update. */
    private transient long destinationCachesLastUpdate = -1;

    /** The contacts cache. */
    private final transient Map<String, String> contactsCache = new ConcurrentHashMap<>();

    /** The ATTACHMENTS_DIR. */
    private static final transient String ATTACHMENTS_DIR = Cnf.at("Server", "attachments", "/tmp/ecpds-attachments")
            + File.separator;

    /** The Constant METADATA_RELOAD. */
    private static final transient long METADATA_RELOAD = Cnf.durationAt("MetaData", "reload", 24 * Timer.ONE_HOUR);

    /** The Constant METADATA_FILENAME. */
    private static final transient String METADATA_FILENAME = Cnf.at("MetaData", "fileName", ".*(.xml)");

    /** The Constant METADATA_TAGS. */
    private static final transient String[] METADATA_TAGS = Cnf.stringListAt("MetaData", "tags", "mailGroup:email",
            "ContactInformations:email");

    /** The Constant METADATA_MISSING_CONTACTS. */
    private static final transient String METADATA_MISSING_CONTACTS = Cnf.at("MetaData", "missingContactsMail", "");

    /** The contacts cache last update. */
    private transient long contactsCacheLastUpdate = -1;

    /**
     * Instantiates a new management impl.
     *
     * @param master
     *            the master
     *
     * @throws RemoteException
     *             the remote exception
     */
    ManagementImpl(final MasterServer master) throws RemoteException {
        this.master = master;
        this.base = master.getECpdsBase();
    }

    /**
     * Gets the contacts.
     *
     * @return the contacts
     *
     * @throws MasterException
     *             the master exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Map<String, String> getContacts() throws MasterException, IOException {
        return getContacts(false);
    }

    /**
     * Gets the destination names for contact.
     *
     * Get the destination names which follow the provided rules for the email addresses in their contacts!
     *
     * @param rules
     *            the rules
     * @param caseSensitive
     *            the case sensitive
     *
     * @return the destination names for contact
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public List<String> getDestinationNamesForContact(final List<Map.Entry<String, String>> rules,
            final boolean caseSensitive) throws IOException {
        final var names = new ArrayList<String>();
        for (final Map.Entry<String, String> rule : rules) {
            for (final Map.Entry<String, String> entries : loadContacts(".*:email").entrySet()) {
                for (final String value : entries.getValue().split(",")) {
                    if (matchesRule(rule, value, caseSensitive)) {
                        names.add(entries.getKey());
                    }
                }
            }
        }
        return names;
    }

    /**
     * Matches rule.
     *
     * @param operator
     *            the operator
     * @param email
     *            the email
     * @param caseSensitive
     *            the case sensitive
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static boolean matchesRule(final Map.Entry<String, String> operator, final String email,
            final boolean caseSensitive) throws IOException {
        if ("=".equals(operator.getKey())) {
            final var value = caseSensitive ? operator.getValue() : operator.getValue().toLowerCase();
            final var mail = caseSensitive ? email : email.toLowerCase();
            return matchesPattern(value, mail);
        }
        // Operator not recognized or allowed!
        throw new IOException("Only operator '=' is allowed for rule 'email'");
    }

    /**
     * Matches pattern.
     *
     * @param pattern
     *            the pattern
     * @param email
     *            the email
     *
     * @return true, if successful
     */
    private static boolean matchesPattern(final String pattern, final String email) {
        return Pattern.compile(pattern.replace(".", "\\.").replace("_", ".").replace("%", ".*")).matcher(email)
                .matches();
    }

    /**
     * Collect emails.
     *
     * @param contactXml
     *            the contact xml
     * @param tagNames
     *            the tag names
     *
     * @return the list
     *
     * @throws ParserConfigurationException
     *             the parser configuration exception
     * @throws SAXException
     *             the SAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static List<String> collectEmails(final File contactXml, final String... tagNames)
            throws ParserConfigurationException, SAXException, IOException {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        final var builder = factory.newDocumentBuilder();
        final var doc = builder.parse(contactXml);
        doc.getDocumentElement().normalize();
        return collectEmails(doc.getDocumentElement(), tagNames);
    }

    /**
     * Collect emails.
     *
     * @param node
     *            the node
     * @param tagNames
     *            the tag names
     *
     * @return the list
     */
    private static List<String> collectEmails(final Node node, final String... tagNames) {
        final List<String> emailList = new ArrayList<>();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (matches(node, tagNames)) {
                final var mailList = Format.trimString(node.getTextContent().trim(), "");
                if (!mailList.isBlank()) {
                    final var tokenizer = new StringTokenizer(mailList, "\"\\|/:;, ");
                    while (tokenizer.hasMoreTokens()) {
                        final var email = tokenizer.nextToken();
                        if (Format.isValidEmail(email)) {
                            _log.debug("Found email for {} -> {}: {}", node.getParentNode().getNodeName(),
                                    node.getNodeName(), email);
                            emailList.add(email);
                        }
                    }
                }
            }
            // Recursively call this method for child nodes
            final var childNodes = node.getChildNodes();
            for (var i = 0; i < childNodes.getLength(); i++) {
                emailList.addAll(collectEmails(childNodes.item(i), tagNames));
            }
        }
        return emailList;
    }

    /**
     * Matches.
     *
     * @param node
     *            the node
     * @param tagNames
     *            the tag names
     *
     * @return true, if successful
     */
    private static boolean matches(final Node node, final String... tagNames) {
        for (final String tagName : tagNames) {
            final var tag = tagName.split(":");
            if (tag.length == 2 && node.getParentNode().getNodeName().matches(tag[0])
                    && node.getNodeName().matches(tag[1])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Load contacts.
     *
     * @param tagNames
     *            the tag names
     *
     * @return the hash map
     */
    private HashMap<String, String> loadContacts(final String... tagNames) {
        final var contacts = new HashMap<String, String>();
        var dirs = new File(ATTACHMENTS_DIR).listFiles();
        if (dirs == null) {
            dirs = new File[] {};
        } else {
            Arrays.sort(dirs);
        }
        for (final File dir : dirs) {
            final var destinationName = dir.getName();
            if (master.getDestination(destinationName) == null) { // Destination does not exists, ignore it!
                _log.debug("Destination {} not found", destinationName);
            } else {
                final var files = new File(dir.getAbsolutePath()).listFiles();
                for (final File contactXml : files == null ? new File[] {} : files) {
                    if (contactXml.getName().matches(METADATA_FILENAME) && contactXml.exists()) {
                        try {
                            _log.debug("Processing file: {}", contactXml.getAbsolutePath());
                            final var emails = collectEmails(contactXml, tagNames);
                            if (!emails.isEmpty()) {
                                contacts.put(destinationName, emails.stream().collect(Collectors.joining(",")));
                            }
                        } catch (final Throwable t) {
                            _log.warn("Reading {}", contactXml.getAbsolutePath(), t);
                        }
                    }
                }
            }
        }
        _log.debug("Contact(s) updated for {} Destination(s)", contacts.size());
        return contacts;
    }

    /**
     * Gets the contacts.
     *
     * Retrieve the contacts. If reload is true or the cache is expired then get it from the configuration files.
     * Otherwise the information is read from the cache!
     *
     * @param reload
     *            the reload
     *
     * @return the contacts
     */
    public Map<String, String> getContacts(final boolean reload) {
        final var monitor = new MonitorCall("getContacts()");
        synchronized (contactsCache) {
            if (reload || contactsCacheLastUpdate == -1
                    || System.currentTimeMillis() - contactsCacheLastUpdate > METADATA_RELOAD) {
                contactsCacheLastUpdate = System.currentTimeMillis();
                contactsCache.clear();
                contactsCache.putAll(loadContacts(METADATA_TAGS));
                if (METADATA_MISSING_CONTACTS.length() > 0) {
                    // Let's build the list of missing contacts per type of Destination for the
                    // sending of the email!
                    final Map<Integer, StringBuilder> perType = new HashMap<>();
                    for (final Destination d : master.getDataBase().getDestinationArray()) {
                        final var name = d.getName();
                        if (d.getActive() && d.getMonitor() && !contactsCache.containsKey(name)) {
                            _log.debug("Missing contact for Destination {}", name);
                            final var missingContacts = perType.computeIfAbsent(d.getType(), _ -> new StringBuilder());
                            missingContacts.append("\n").append("Destination ").append(name);
                            // Only append the comment if it is not empty!
                            final var comment = d.getComment();
                            if (isNotEmpty(comment)) {
                                missingContacts.append(" (").append(comment.trim()).append(")");
                            }

                        }
                    }
                    // Now let's build the full list organized per Destination type!
                    final var missingContacts = new StringBuilder();
                    for (final Integer type : perType.keySet()) {
                        missingContacts.append("\n\n").append(DestinationOption.getLabel(type)).append("\n")
                                .append(perType.get(type));
                    }
                    // Did we found anything to report?
                    if (missingContacts.length() > 0) {
                        master.sendECpdsMessage(METADATA_MISSING_CONTACTS, "Missing contacts",
                                "Contacts are missing for the following Destination(s):\n\n----------"
                                        + missingContacts.toString() + "\n----------\n");
                    }
                }
            }
        }
        return monitor.done(contactsCache);
    }

    /**
     * Gets the destination caches.
     *
     * @return the destination caches
     *
     * @throws MonitorException
     *             the monitor exception
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public Map<String, DestinationCache> getDestinationCaches()
            throws MonitorException, MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall("getDestinationCaches()");
        synchronized (destinationCaches) {
            if (destinationCaches.isEmpty()
                    || System.currentTimeMillis() - destinationCachesLastUpdate > 10 * Timer.ONE_SECOND) {
                destinationCaches.clear();
                destinationCaches.putAll(master.getDestinationCaches());
                destinationCachesLastUpdate = System.currentTimeMillis();
            }
        }
        return monitor.done(destinationCaches);
    }

    /**
     * Gets the ecpds session.
     *
     * @param user
     *            the user
     * @param password
     *            the password
     * @param host
     *            the host
     * @param agent
     *            the agent
     * @param comment
     *            the comment
     *
     * @return the ecpds session
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public ECpdsSession getECpdsSession(final String user, final String password, final String host, final String agent,
            final String comment) throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "getECpdsSession(" + user + "," + password + "," + host + "," + agent + "," + comment + ")");
        String root;
        try {
            root = getClientHost();
        } catch (final ServerNotActiveException e) {
            root = null;
        }
        return monitor.done(master.getECpdsSession(user, password, root, host, agent, comment));
    }

    /**
     * Save web user.
     *
     * @param session
     *            the session
     * @param webUser
     *            the web user
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void saveWebUser(final ECpdsSession session, final WebUser webUser)
            throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "saveWebUser(" + session.getWebUser().getName() + "," + webUser.getId() + ")");
        final var action = master.startECpdsAction(session, "saveWebUser");
        Exception exception = null;
        try {
            master.saveWebUser(webUser);
            monitor.done();
        } catch (final MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Saving WebUser(" + webUser.getId() + ")", webUser, exception);
        }
    }

    /**
     * Copy host.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param hostName
     *            the host name
     *
     * @return the destination cache
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public DestinationCache copyHost(final ECpdsSession session, final String destinationName, final String hostName)
            throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall(
                "copyHost(" + session.getWebUser().getName() + "," + destinationName + "," + hostName + ")");
        final var action = master.startECpdsAction(session, "duplicate", base.getHost(hostName));
        Host host = null;
        Exception exception = null;
        try {
            host = master.copyHost(destinationName, hostName);
            return monitor.done(master.getDestinationCache(destinationName));
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Duplicating Host in Destination(" + destinationName + ")", host, exception);
        }
    }

    /**
     * Copy destination.
     *
     * @param session
     *            the session
     * @param fromDestination
     *            the from destination
     * @param toDestination
     *            the to destination
     * @param label
     *            the label
     * @param copySharedHost
     *            the copy shared host
     *
     * @return the destination cache
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public DestinationCache copyDestination(final ECpdsSession session, final String fromDestination,
            final String toDestination, final String label, final boolean copySharedHost)
            throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall("copyDestination(" + session.getWebUser().getName() + "," + fromDestination
                + "," + toDestination + "," + copySharedHost + ")");
        final var action = master.startECpdsAction(session, "duplicate", base.getDestination(fromDestination));
        Destination destination = null;
        Exception exception = null;
        try {
            destination = master.copyDestination(fromDestination, toDestination, label, copySharedHost);
            destinationCaches.clear();
            // Force an update of the monitoring!
            if (MonitorManager.isActivated()) {
                try {
                    master.getTransferScheduler().getMonitoringThread().wakeup();
                } catch (final Throwable t) {
                    _log.warn("Could not wakeup monitoring", t);
                }
            }
            return monitor.done(master.getDestinationCache(toDestination));
        } catch (final MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Duplicating into Destination(" + toDestination + ") with label '" + label
                    + "'" + (copySharedHost ? " (copy-shared-host)" : ""), destination, exception);
        }
    }

    /**
     * Export destination.
     *
     * @param session
     *            the session
     * @param targetMaster
     *            the target master
     * @param fromDestination
     *            the from destination
     * @param copySharedHost
     *            the copy shared host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void exportDestination(final ECpdsSession session, final String targetMaster, final String fromDestination,
            final boolean copySharedHost) throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall("exportDestination(" + session.getWebUser().getName() + "," + targetMaster
                + "," + fromDestination + "," + copySharedHost + ")");
        final var action = master.startECpdsAction(session, "export", base.getDestination(fromDestination));
        Exception exception = null;
        try {
            master.exportDestination(targetMaster, fromDestination, copySharedHost);
            monitor.done();
        } catch (final RemoteException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action,
                    "Exporting Destination to " + targetMaster + (copySharedHost ? " (copy-shared-host)" : ""),
                    exception);
        }
    }

    /**
     * Close E cpds session.
     *
     * @param session
     *            the session
     * @param expired
     *            the expired
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void closeECpdsSession(final ECpdsSession session, final boolean expired) throws RemoteException {
        final var monitor = new MonitorCall(
                "closeECpdsSession(" + session.getWebUser().getName() + "," + expired + ")");
        master.closeECpdsSession(session, expired);
        monitor.done();
    }

    /**
     * Close incoming connection.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void closeIncomingConnection(final ECpdsSession session, final String id) throws IOException {
        final var monitor = new MonitorCall(
                "closeIncomingConnection(" + session.getWebUser().getName() + "," + id + ")");
        master.closeIncomingConnection(id);
        monitor.done();
    }

    /**
     * Restart destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param graceful
     *            the graceful
     *
     * @return the destination scheduler cache
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws MasterException
     *             the master exception
     */
    @Override
    public DestinationSchedulerCache restartDestination(final ECpdsSession session, final String destinationName,
            final boolean graceful) throws DataBaseException, MasterException {
        final var monitor = new MonitorCall(
                "restartDestination(" + session.getWebUser().getName() + "," + destinationName + "," + graceful + ")");
        final var action = master.startECpdsAction(session, "restart", base.getDestination(destinationName));
        final var webUser = session.getWebUser();
        Exception exception = null;
        try {
            master.getTransferScheduler().restartDestination(action.getWebUserId(), action.getWebUserId(),
                    destinationName, graceful, true);
            if (!graceful) {
                // We might have some retrieval going on for this Destination? If it is graceful
                // we let them complete otherwise we have to interrupt the retrievals!
                final var message = "Retrieval interrupted by WebUser=" + webUser.getId() + " (" + webUser.getName()
                        + ") following immediate stop of Destination=" + destinationName;
                try {
                    master.getDownloadScheduler(false).interruptAllDownload(destinationName, message);
                } catch (final MasterException e) {
                    // The download scheduler is not started!
                }
                try {
                    master.getDownloadScheduler(true).interruptAllDownload(destinationName, message);
                } catch (final MasterException e) {
                    // The acquisition scheduler is not started!
                }
                // We might have some listing going on for this Destination?
                for (var host : master.getECpdsBase().getDestinationHost(destinationName, HostOption.ACQUISITION))
                    master.updateHost(host);
            }
            return monitor.done(master.getDestinationSchedulerCache(destinationName));
        } catch (final MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, (graceful ? "Graceful" : "Immediate") + " restart", exception);
        }
    }

    /**
     * Restart all destinations.
     *
     * @param session
     *            the session
     * @param graceful
     *            the graceful
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public void restartAllDestinations(final ECpdsSession session, final boolean graceful) throws MasterException {
        final var monitor = new MonitorCall(
                "restartAllDestinations(" + session.getWebUser().getName() + "," + graceful + ")");
        final var action = master.startECpdsAction(session, "restartAllDestinations");
        Exception exception = null;
        try {
            master.getTransferScheduler().restartAllDestinations(action.getWebUserId(), graceful);
            monitor.done();
        } catch (final MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, (graceful ? "Graceful" : "Immediat") + " restart", exception);
        }
    }

    /**
     * Shutdown.
     *
     * @param session
     *            the session
     * @param graceful
     *            the graceful
     * @param restart
     *            the restart
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public void shutdown(final ECpdsSession session, final boolean graceful, final boolean restart)
            throws MasterException {
        final var monitor = new MonitorCall(
                "shutdown(" + session.getWebUser().getName() + "," + graceful + "," + restart + ")");
        final var action = master.startECpdsAction(session, "shutdown");
        Exception exception = null;
        try {
            master.shutdown(graceful, restart);
            monitor.done();
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action,
                    (graceful ? "Graceful" : "Immediat") + " shutdown" + (restart ? " and restart" : ""), exception);
        }
    }

    /**
     * Gets the retrieved.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the retrieved
     *
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public long getRetrieved(final long dataFileId) throws DataBaseException {
        final var monitor = new MonitorCall("getRetrieved(" + dataFileId + ")");
        return monitor.done(master.getRetrieved(dataFileId));
    }

    /**
     * Gets the transfer server name.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the transfer server name
     *
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public String getTransferServerName(final long dataFileId) throws DataBaseException {
        final var monitor = new MonitorCall("getTransferServerName(" + dataFileId + ")");
        return monitor.done(master.getTransferServerName(dataFileId));
    }

    /**
     * Gets the destination status.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination status
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public String getDestinationStatus(final String destinationName) throws MasterException, DataBaseException {
        final var monitor = new MonitorCall("getDestinationStatus(" + destinationName + ")");
        return monitor.done(master.getTransferScheduler().getDestinationStatus(destinationName));
    }

    /**
     * Gets the monitor manager.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the monitor manager
     *
     * @throws MonitorException
     *             the monitor exception
     * @throws MasterException
     *             the master exception
     */
    @Override
    public MonitorManager getMonitorManager(final String destinationName) throws MonitorException, MasterException {
        final var monitor = new MonitorCall("getMonitorManager(" + destinationName + ")");
        return monitor.done(master.getTransferScheduler().getMonitoringThread().getMonitorManager(destinationName));
    }

    /**
     * Gets the destination size.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination size
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public int getDestinationSize(final String destinationName) throws MasterException {
        final var monitor = new MonitorCall("getDestinationSize(" + destinationName + ")");
        return monitor.done(master.getTransferScheduler().getDestinationThread(destinationName).getSize());
    }

    /**
     * Gets the destination start date.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination start date
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public Date getDestinationStartDate(final String destinationName) throws MasterException {
        final var monitor = new MonitorCall("getDestinationStartDate(" + destinationName + ")");
        return monitor.done(master.getTransferScheduler().getDestinationThread(destinationName).getStartDate());
    }

    /**
     * Gets the pending data transfers count.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the pending data transfers count
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public int getPendingDataTransfersCount(final String destinationName) throws MasterException {
        final var monitor = new MonitorCall("getPendingDataTransfersCount(" + destinationName + ")");
        return monitor.done(master.getTransferScheduler().getPendingDataTransfersCount(destinationName));
    }

    /**
     * Gets the destination last transfer.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination last transfer
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public DataTransfer getDestinationLastTransfer(final String destinationName) throws MasterException {
        final var monitor = new MonitorCall("getDestinationLastTransfer(" + destinationName + ")");
        return monitor.done(master.getTransferScheduler().getDestinationLastTransfer(destinationName, true));
    }

    /**
     * Gets the destination last failed transfer.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination last failed transfer
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public DataTransfer getDestinationLastFailedTransfer(final String destinationName) throws MasterException {
        final var monitor = new MonitorCall("getDestinationLastFailedTransfer(" + destinationName + ")");
        final Timestamp failedTime;
        final var transfer = master.getTransferScheduler().getDestinationLastTransfer(destinationName, false);
        return monitor.done(transfer != null && (failedTime = transfer.getFailedTime()) != null
                && System.currentTimeMillis() - failedTime.getTime() < Timer.ONE_DAY ? transfer : null);
    }

    /**
     * Hold destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param graceful
     *            the graceful
     *
     * @return the destination scheduler cache
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws MasterException
     *             the master exception
     */
    @Override
    public DestinationSchedulerCache holdDestination(final ECpdsSession session, final String destinationName,
            final boolean graceful) throws DataBaseException, MasterException {
        final var monitor = new MonitorCall(
                "holdDestination(" + session.getWebUser().getName() + "," + destinationName + "," + graceful + ")");
        final var action = master.startECpdsAction(session, "stop", base.getDestination(destinationName));
        final var webUser = session.getWebUser();
        Exception exception = null;
        try {
            master.getTransferScheduler().holdDestination(action.getWebUserId(), destinationName, StatusFactory.STOP,
                    graceful);
            if (!graceful) {
                // We might have some retrieval going on for this Destination? If it is graceful
                // we let them complete otherwise we have to interrupt the retrievals!
                final var message = "Retrieval interrupted by WebUser=" + webUser.getId() + " (" + webUser.getName()
                        + ") following immediate stop of Destination=" + destinationName;
                try {
                    master.getDownloadScheduler(false).interruptAllDownload(destinationName, message);
                } catch (final MasterException e) {
                    // The download scheduler is not started!
                }
                try {
                    master.getDownloadScheduler(true).interruptAllDownload(destinationName, message);
                } catch (final MasterException e) {
                    // The acquisition scheduler is not started!
                }
                // We might have some listing going on for this Destination?
                for (var host : master.getECpdsBase().getDestinationHost(destinationName, HostOption.ACQUISITION))
                    master.updateHost(host);
            }
            return monitor.done(master.getDestinationSchedulerCache(destinationName));
        } catch (final MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, (graceful ? "Graceful" : "Immediate") + " stop", exception);
        }
    }

    /**
     * Hold all destinations.
     *
     * @param session
     *            the session
     * @param graceful
     *            the graceful
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public void holdAllDestinations(final ECpdsSession session, final boolean graceful) throws MasterException {
        final var monitor = new MonitorCall(
                "holdAllDestinations(" + session.getWebUser().getName() + "," + graceful + ")");
        final var action = master.startECpdsAction(session, "stopAllDestinations");
        Exception exception = null;
        try {
            master.getTransferScheduler().holdAllDestinations(action.getWebUserId(), StatusFactory.STOP, graceful);
            monitor.done();
        } catch (final MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, (graceful ? "Graceful" : "Immediate") + " stop", exception);
        }
    }

    /**
     * _host count.
     *
     * @param associationsList
     *            the associations list
     * @param hostName
     *            the host name
     *
     * @return the int
     */
    private static int hostCount(final Association[] associationsList, final String hostName) {
        var count = 0;
        for (final Association association : associationsList) {
            if (association.getHostName().equals(hostName)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Removes the destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeDestination(final ECpdsSession session, final String destinationName)
            throws MasterException, DataBaseException {
        removeDestination(session, destinationName, false, true);
    }

    /**
     * The Class CleanThread. Listing meant to be used in multiple instances in parallel!
     */
    final class CleanThread extends ExecutorRunnable {

        /** The action. */
        final ECpdsAction action;

        /** The transfer. */
        final DataTransfer transfer;

        /**
         * Instantiates a new clean thread.
         *
         * @param manager
         *            the manager
         * @param action
         *            the action
         * @param transfer
         *            the transfer
         */
        CleanThread(final ExecutorManager<CleanThread> manager, final ECpdsAction action, final DataTransfer transfer) {
            super(manager);
            this.action = action;
            this.transfer = transfer;
        }

        /**
         * Process.
         *
         * @throws DataBaseException
         *             the data base exception
         * @throws MasterException
         *             the master exception
         */
        @Override
        public void process() throws DataBaseException, MasterException {
            cleanDataTransfer(action, transfer, false);
        }
    }

    /**
     * Removes the destination.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param cleanOnly
     *            the clean only
     * @param removeAll
     *            the remove all
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeDestination(final ECpdsSession session, final String destinationName, final boolean cleanOnly,
            final boolean removeAll) throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeDestination(" + session.getWebUser().getName() + "," + destinationName + "," + cleanOnly + ")");
        final var action = master.startECpdsAction(session, cleanOnly ? "clean" : "remove",
                base.getDestination(destinationName));
        Exception exception = null;
        try {
            try {
                // Make sure the TransferScheduler is stopping immediately to
                // use the Destination!
                master.getTransferScheduler().holdDestination(action.getWebUserId(), destinationName,
                        StatusFactory.STOP, false);
            } catch (final MasterException e) {
                _log.warn("holding Destination", e);
            }
            try {
                // Stop all acquisition activity as well
                for (final Host host : base.getHostsByDestinationId(destinationName))
                    master.interruptAquisitionFor(host);
            } catch (final Throwable t) {
                _log.warn("interrupting Acquisition", t);
            }
            final var manager = new ExecutorManager<CleanThread>(100, 50);
            try (var it = base.getDataTransfersByDestination(destinationName)) {
                if (it.hasNext()) {
                    manager.start();
                }
                // Delete every single DataTransfer/DataFile for this Destination!
                while (it.hasNext()) {
                    final var transfer = it.next();
                    final var expiry = transfer.getExpiryTime();
                    final var code = transfer.getStatusCode();
                    if (removeAll || expiry != null && expiry.before(new Date(System.currentTimeMillis()))
                            || StatusFactory.STOP.equals(code) || StatusFactory.FAIL.equals(code)) {
                        manager.put(new CleanThread(manager, action, transfer));
                    }
                }
                // We don't want to take more jobs!
                manager.stopRun();
                // And now we wait for all the Threads to complete!
                try {
                    manager.join();
                } catch (final InterruptedException e) {
                    _log.warn("Interrupted", e);
                }
            }
            if (!cleanOnly) {
                // Remove all the Hosts which are not shared!
                final var associationsList = base.getAssociationArray();
                for (final Association assoc : associationsList) {
                    if (destinationName.equals(assoc.getDestinationName())) {
                        final var host = assoc.getHost();
                        // If the Host is part of more than one Association then
                        // this is a shared Host (don't remove it).
                        if (hostCount(associationsList, host.getName()) <= 1) {
                            removeHost(session, host);
                        }
                    }
                }
                // Remove all the incoming permissions!
                final var permissionList = base.getIncomingAssociationArray();
                for (final IncomingAssociation assoc : permissionList) {
                    if (destinationName.equals(assoc.getDestinationName())) {
                        base.remove(assoc);
                    }
                }
                // Remove the access control!
                try {
                    AccessControl.removeAccessControl(base, destinationName);
                } catch (final Throwable t) {
                    _log.warn("Could not remove access control for {}", destinationName, t);
                }
                // Remove the DataTransfers and the Destination from the DataBase
                base.removeDestination(destinationName, true);
                destinationCaches.clear();
                base.clearCache(); // We need to make sure there are no leftover
                // Force an update of the monitoring!
                if (MonitorManager.isActivated()) {
                    try {
                        final var monitoring = master.getTransferScheduler().getMonitoringThread();
                        monitoring.unSubscribe(destinationName);
                        monitoring.wakeup();
                    } catch (final Throwable t) {
                        _log.warn("Could not cancel monitoring for {}", destinationName, t);
                    }
                }
                // Remove the metadata files and directory
                deleteDirectory(ATTACHMENTS_DIR + destinationName);
            } else {
                // Remove all the DataTransfers from the DataBase
                base.removeDestination(destinationName, false);
            }
            monitor.done();
        } catch (DataBaseException | MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Delete directory. Delete a non-empty directory.
     *
     * @param directoryName
     *            the directory name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void deleteDirectory(final String directoryName) throws IOException {
        final var path = Paths.get(directoryName);
        if (path.toFile().exists()) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                // delete directories
                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    Files.delete(dir);
                    _log.debug("Directory deleted: {}", dir);
                    return FileVisitResult.CONTINUE;
                }

                // delete files
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    _log.debug("File deleted: {}", file);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * _clean data transfer.
     *
     * @param action
     *            the action
     * @param transfer
     *            the transfer
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    private void cleanDataTransfer(final ECpdsAction action, final DataTransfer transfer, final boolean addHistory)
            throws MasterException, DataBaseException {
        final var webUser = action.getECpdsSession().getWebUser();
        final var byAndFrom = "By WebUser=" + webUser.getId() + " (" + webUser.getName() + ") from the web interface";
        master.updateTransferStatus(transfer.getId(), StatusFactory.STOP, true, action.getWebUserId(), byAndFrom, true,
                false, addHistory);
        transfer.setDeleted(true);
        base.update(transfer);
        if (master.getDataTransfers(transfer.getDataFileId()).length == 0) {
            master.purgeDataFile(transfer.getDataFile(), addHistory ? byAndFrom : null);
        }
    }

    /**
     * Removes the host.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeHost(final ECpdsSession session, final Host host) throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeHost(" + session.getWebUser().getName() + "," + host.getName() + ")");
        final var action = master.startECpdsAction(session, "remove", host);
        Exception exception = null;
        try {
            synchronized (host) {
                host.setActive(false);
                base.update(host);
            }
            base.removeHost(host);
            monitor.done();
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the incoming user.
     *
     * @param session
     *            the session
     * @param user
     *            the user
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeIncomingUser(final ECpdsSession session, final IncomingUser user)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeIncomingUser(" + session.getWebUser().getName() + "," + user.getId() + ")");
        final var action = master.startECpdsAction(session, "remove", user);
        Exception exception = null;
        try {
            synchronized (user) {
                user.setActive(false);
                base.update(user);
            }
            base.removeIncomingUser(user);
            monitor.done();
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the incoming policy.
     *
     * @param session
     *            the session
     * @param policy
     *            the policy
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeIncomingPolicy(final ECpdsSession session, final IncomingPolicy policy)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeIncomingPolicy(" + session.getWebUser().getName() + "," + policy.getId() + ")");
        final var action = master.startECpdsAction(session, "remove", policy);
        Exception exception = null;
        try {
            synchronized (policy) {
                policy.setActive(false);
                base.update(policy);
            }
            base.removeIncomingPolicy(policy);
            monitor.done();
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the transfer method.
     *
     * @param session
     *            the session
     * @param method
     *            the method
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeTransferMethod(final ECpdsSession session, final TransferMethod method)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeTransferMethod(" + session.getWebUser().getName() + "," + method.getName() + ")");
        final var action = master.startECpdsAction(session, "remove", method);
        Exception exception = null;
        try {
            method.setActive(false);
            master.getDataBase().update(method);
            for (final Host host : base.getHostsByTransferMethodId(method.getName()).toArray(new Host[0])) {
                removeHost(session, host);
            }
            base.remove(method);
            monitor.done();
        } catch (MasterException | DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the E ctrans module.
     *
     * @param session
     *            the session
     * @param module
     *            the module
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeECtransModule(final ECpdsSession session, final ECtransModule module)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeECtransModule(" + session.getWebUser().getName() + "," + module.getName() + ")");
        final var action = master.startECpdsAction(session, "remove", module);
        Exception exception = null;
        try {
            module.setActive(false);
            master.getDataBase().update(module);
            for (final TransferMethod method : base.getTransferMethodsByEcTransModuleName(module.getName())
                    .toArray(new TransferMethod[0])) {
                removeTransferMethod(session, method);
            }
            base.remove(module);
            monitor.done();
        } catch (MasterException | DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the transfer group.
     *
     * @param session
     *            the session
     * @param group
     *            the group
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeTransferGroup(final ECpdsSession session, final TransferGroup group)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeTransferGroup(" + session.getWebUser().getName() + "," + group.getName() + ")");
        final var action = master.startECpdsAction(session, "remove", group);
        Exception exception = null;
        try {
            group.setActive(false);
            master.getDataBase().update(group);
            for (final TransferServer server : base.getTransferServerArray()) {
                if (server.getTransferGroupName().equals(group.getName())) {
                    removeTransferServer(session, server);
                }
            }
            base.remove(group);
            monitor.done();
        } catch (MasterException | DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the transfer server.
     *
     * @param session
     *            the session
     * @param server
     *            the server
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeTransferServer(final ECpdsSession session, final TransferServer server)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeTransferServer(" + session.getWebUser().getName() + "," + server.getName() + ")");
        final var action = master.startECpdsAction(session, "remove", server);
        Exception exception = null;
        try {
            server.setActive(false);
            master.getDataBase().update(server);
            final var serverName = server.getName();
            final var mover = master.getDataMoverInterface(serverName);
            if (mover != null) {
                try {
                    mover.shutdown(false, false);
                } catch (final IOException e) {
                    _log.warn(e);
                }
                while (master.existsClientInterface(serverName, "DataMover")) {
                    try {
                        Thread.sleep(2 * Timer.ONE_SECOND);
                    } catch (final InterruptedException e) {
                        _log.debug("Interrupted");
                    }
                }
            } else {
                master.resetTransferServer(serverName, "DataMover " + serverName + " removed");
            }
            try (var it = base.getDataTransfersByTransferServer(server)) {
                while (it.hasNext()) {
                    cleanDataTransfer(action, it.next(), true);
                }
            }
            base.remove(server);
            monitor.done();
        } catch (MasterException | DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Shutdown transfer server.
     *
     * @param session
     *            the session
     * @param server
     *            the server
     * @param graceful
     *            the graceful
     * @param restart
     *            the restart
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public void shutdownTransferServer(final ECpdsSession session, final TransferServer server, final boolean graceful,
            final boolean restart) throws MasterException {
        final var monitor = new MonitorCall("shutdownTransferServer(" + session.getWebUser().getName() + ","
                + server.getName() + "," + graceful + "," + restart + ")");
        final var action = master.startECpdsAction(session, "shutdown", server);
        Exception exception = null;
        try {
            final var serverName = server.getName();
            final var mover = master.getDataMoverInterface(serverName);
            if (mover == null) {
                throw new MasterException("DataMover " + serverName + " not available");
            }
            mover.shutdown(graceful, restart);
            monitor.done();
        } catch (final MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action,
                    (graceful ? "Graceful" : "Immediat") + " shutdown" + (restart ? " and restart" : ""), exception);
        }
    }

    /**
     * Removes the data transfer.
     *
     * @param session
     *            the session
     * @param transfer
     *            the transfer
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeDataTransfer(final ECpdsSession session, final DataTransfer transfer)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeDataTransfer(" + session.getWebUser().getName() + "," + transfer.getId() + ")");
        final var action = master.startECpdsAction(session, "clean", transfer);
        Exception exception = null;
        try {
            cleanDataTransfer(action, transfer, true);
            monitor.done();
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Interrupt data transfer retrieval.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     *
     * @return true, if successful
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws MasterException
     *             the master exception
     */
    @Override
    public boolean interruptDataTransferRetrieval(final ECpdsSession session, final long id)
            throws DataBaseException, MasterException {
        final var monitor = new MonitorCall(
                "interruptDataTransferRetrieval(" + session.getWebUser().getName() + "," + id + ")");
        final var transfer = base.getDataTransfer(id);
        final var action = master.startECpdsAction(session, "interrupt", transfer);
        final var webUser = session.getWebUser();
        Exception exception = null;
        try {
            final var message = "Retrieval interrupted by WebUser=" + webUser.getId() + " (" + webUser.getName() + ")";
            return monitor.done(master.getDownloadScheduler(false).interruptDownload(transfer, message)
                    || master.getDownloadScheduler(true).interruptDownload(transfer, message));
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the data file.
     *
     * @param session
     *            the session
     * @param file
     *            the file
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeDataFile(final ECpdsSession session, final DataFile file)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeDataFile(" + session.getWebUser().getName() + "," + file.getId() + ")");
        final var action = master.startECpdsAction(session, "remove", file);
        final var webUser = session.getWebUser();
        Exception exception = null;
        try {
            master.removeDataFileAndDataTransfers(file, action.getWebUserId(),
                    "by WebUser=" + webUser.getId() + " (" + webUser.getName() + ") from the web interface");
            monitor.done();
        } catch (MasterException | DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the web user.
     *
     * @param session
     *            the session
     * @param user
     *            the user
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeWebUser(final ECpdsSession session, final WebUser user)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeWebUser(" + session.getWebUser().getName() + "," + user.getName() + ")");
        final var action = master.startECpdsAction(session, "remove", user);
        Exception exception = null;
        try {
            synchronized (user) {
                user.setActive(false);
                base.update(user);
            }
            base.removeWebUser(user);
            monitor.done();
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the category.
     *
     * @param session
     *            the session
     * @param category
     *            the category
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeCategory(final ECpdsSession session, final Category category)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "removeCategory(" + session.getWebUser().getName() + "," + category.getId() + ")");
        final var action = master.startECpdsAction(session, "remove", category);
        Exception exception = null;
        try {
            base.removeCategory(category);
            monitor.done();
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Removes the url.
     *
     * @param session
     *            the session
     * @param url
     *            the url
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void removeUrl(final ECpdsSession session, final Url url) throws MasterException, DataBaseException {
        final var monitor = new MonitorCall("removeUrl(" + session.getWebUser().getName() + "," + url.getName() + ")");
        final var action = master.startECpdsAction(session, "remove", url);
        Exception exception = null;
        try {
            base.removeUrl(url);
            monitor.done();
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Update transfer priority.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     * @param priority
     *            the priority
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void updateTransferPriority(final ECpdsSession session, final long id, final int priority)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "updateTransferPriority(" + session.getWebUser().getName() + "," + id + "," + priority + ")");
        final var transfer = master.getDataTransfer(id);
        if (transfer == null) {
            throw new MasterException("DataTransfer not found: " + id);
        }
        final var action = master.startECpdsAction(session, "update", transfer);
        Exception exception = null;
        try {
            if (priority < 0 || priority > 99) {
                throw new MasterException("Priority out of range (0..99)");
            }
            _log.debug("Setting priority to {} for DataTransfer-{}", priority, id);
            transfer.setPriority(priority);
            master.getDataBase().update(transfer);
            master.reloadDestination(transfer);
            monitor.done();
        } catch (MasterException | DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Setting priority to " + priority, exception);
        }
    }

    /**
     * Update expiry time.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     * @param timestamp
     *            the timestamp
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void updateExpiryTime(final ECpdsSession session, final long id, final Timestamp timestamp)
            throws MasterException, DataBaseException, RemoteException {
        final var monitor = new MonitorCall("updateExpiryTime(" + session.getWebUser().getName() + "," + id + ","
                + Format.formatTime(timestamp) + ")");
        final var transfer = master.getDataTransfer(id);
        if (transfer == null) {
            throw new MasterException("DataTransfer not found: " + id);
        }
        final var action = master.startECpdsAction(session, "update", transfer);
        final var requestedTime = Format.formatTime(timestamp);
        Exception exception = null;
        try {
            final var current = transfer.getExpiryTime();
            if (timestamp.before(current)) {
                throw new MasterException("Expiry time already later than " + requestedTime);
            }
            _log.debug("Setting expiry time to {} for DataTransfer {}", requestedTime, id);
            transfer.setExpiryTime(timestamp);
            master.getDataBase().update(transfer);
            master.reloadDestination(transfer);
            monitor.done();
        } catch (MasterException | DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Setting expiry time to " + requestedTime, exception);
        }
    }

    /**
     * Update transfer monitoring value.
     *
     * @param session
     *            the session
     * @param value
     *            the value
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void updateTransferMonitoringValue(final ECpdsSession session, final MonitoringValue value)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "updateTransferMonitoringValue(" + session.getWebUser().getName() + "," + value.getId() + ")");
        try {
            base.update(value);
            monitor.done();
        } catch (final DataBaseException e) {
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            _log.warn(e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Update file monitoring value.
     *
     * @param session
     *            the session
     * @param value
     *            the value
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void updateFileMonitoringValue(final ECpdsSession session, final MonitoringValue value)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "updateFileMonitoringValue(" + session.getWebUser().getName() + "," + value.getId() + ")");
        try {
            base.update(value);
            monitor.done();
        } catch (final DataBaseException e) {
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            _log.warn(e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Reset transfer schedule date.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public void resetTransferScheduleDate(final ECpdsSession session, final long id)
            throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "resetTransferScheduleDate(" + session.getWebUser().getName() + "," + id + ")");
        final var transfer = master.getDataTransfer(id);
        if (transfer == null) {
            throw new MasterException("DataTransfer not found: " + id);
        }
        final var action = master.startECpdsAction(session, "update", transfer);
        Exception exception = null;
        final var now = System.currentTimeMillis();
        try {
            if (_log.isDebugEnabled()) {
                _log.debug("Updating schedule date to {} for DataTransfer {}", Format.formatTime(now), id);
            }
            transfer.setQueueTime(new Timestamp(now));
            transfer.setRetryTime(new Timestamp(now));
            transfer.setComment("Manualy scheduled for no sooner than " + Format.formatTime("MMM dd HH:mm:ss", now));
            master.addTransferHistory(transfer);
            master.getDataBase().update(transfer);
            master.reloadDestination(transfer);
            monitor.done();
        } catch (MasterException | DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Setting schedule date to " + Format.formatTime(now), exception);
        }
    }

    /**
     * Update host.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @return the host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    @Override
    public Host updateHost(final ECpdsSession session, final Host host) throws MasterException, DataBaseException {
        final var monitor = new MonitorCall(
                "updateHost(" + session.getWebUser().getName() + "," + host.getName() + ")");
        final var action = master.startECpdsAction(session, "update", base.getHost(host.getName()));
        Exception exception = null;
        try {
            return monitor.done(master.updateHost(host));
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, host, exception);
        }
    }

    /**
     * Transfer.
     *
     * @param session
     *            the session
     * @param bytes
     *            the bytes
     * @param host
     *            the host
     * @param target
     *            the target
     * @param remotePosn
     *            the remote posn
     *
     * @return the long
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long transfer(final ECpdsSession session, final byte[] bytes, final Host host, final String target,
            final long remotePosn) throws MasterException, DataBaseException, IOException {
        final var monitor = new MonitorCall("transfer(" + session.getWebUser().getName() + ",bytes[]," + host.getName()
                + "," + target + "," + remotePosn + ")");
        final var action = master.startECpdsAction(session, "upload", host);
        Exception exception = null;
        try {
            final var group = host.getTransferGroup();
            final var servers = TransferServerProvider.getTransferServers("ManagementImpl", group);
            if (servers.isEmpty()) {
                throw new MasterException("No TransferServer(s) available for TransferGroup " + group.getName());
            }
            return monitor.done(master.transfer(bytes, servers.get(0), host, target, remotePosn, -1));
        } catch (DataBaseException | MasterException | IOException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Uploading " + Format.formatSize(bytes.length) + " to " + target, exception);
        }
    }

    /**
     * Gets the report.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @return the report
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String getReport(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, IOException {
        final var monitor = new MonitorCall("getReport(" + session.getWebUser().getName() + "," + host.getName() + ")");
        final var action = master.startECpdsAction(session, "report", host);
        Exception exception = null;
        try {
            return monitor.done(master.getReport(host));
        } catch (DataBaseException | IOException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Gets the output.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @return the output
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public RemoteInputStream getOutput(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, IOException {
        final var monitor = new MonitorCall("getOutput(" + session.getWebUser().getName() + "," + host.getName() + ")");
        final var action = master.startECpdsAction(session, "output", host);
        Exception exception = null;
        try {
            return monitor.done(master.getOutput(host));
        } catch (final IOException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Gets the host report.
     *
     * @param session
     *            the session
     * @param proxy
     *            the proxy
     * @param host
     *            the host
     *
     * @return the host report
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String getHostReport(final ECpdsSession session, final Host proxy, final Host host)
            throws MasterException, DataBaseException, IOException {
        final var monitor = new MonitorCall(
                "getHostReport(" + session.getWebUser().getName() + "," + proxy.getName() + "," + host.getName() + ")");
        final var action = master.startECpdsAction(session, "report", host);
        Exception exception = null;
        try {
            return monitor.done(master.getHostReport(proxy, host));
        } catch (DataBaseException | IOException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Getting trace through ProxyHost(" + proxy.getName() + ")", exception);
        }
    }

    /**
     * Clean data window.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void cleanDataWindow(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, IOException {
        final var monitor = new MonitorCall(
                "cleanDataWindow(" + session.getWebUser().getName() + "," + host.getName() + ")");
        final var action = master.startECpdsAction(session, "clean", (Host) host.clone());
        Exception exception = null;
        try {
            master.cleanDataWindow(host);
            monitor.done();
        } catch (DataBaseException | IOException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, host, exception);
        }
    }

    /**
     * Reset transfer statistics.
     *
     * @param session
     *            the session
     * @param host
     *            the host
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void resetTransferStatistics(final ECpdsSession session, final Host host)
            throws MasterException, DataBaseException, IOException {
        final var monitor = new MonitorCall(
                "resetHostStats(" + session.getWebUser().getName() + "," + host.getName() + ")");
        final var action = master.startECpdsAction(session, "reset", (Host) host.clone());
        Exception exception = null;
        try {
            master.resetHostStats(host);
            monitor.done();
        } catch (final DataBaseException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, host, exception);
        }
    }

    /**
     * Gets the mover report.
     *
     * @param session
     *            the session
     * @param proxy
     *            the proxy
     *
     * @return the mover report
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String getMoverReport(final ECpdsSession session, final Host proxy)
            throws MasterException, DataBaseException, IOException {
        final var monitor = new MonitorCall(
                "getMoverReport(" + session.getWebUser().getName() + "," + proxy.getName() + ")");
        final var action = master.startECpdsAction(session, "report", proxy);
        Exception exception = null;
        try {
            return monitor.done(master.getMoverReport(proxy));
        } catch (DataBaseException | IOException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Gets the report.
     *
     * @param session
     *            the session
     * @param server
     *            the server
     *
     * @return the report
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String getReport(final ECpdsSession session, final TransferServer server)
            throws MasterException, DataBaseException, IOException {
        final var monitor = new MonitorCall(
                "getReport(" + session.getWebUser().getName() + "," + server.getName() + ")");
        final var action = master.startECpdsAction(session, "report", server);
        Exception exception = null;
        try {
            return monitor.done(master.getReport(server));
        } catch (final IOException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, null, exception);
        }
    }

    /**
     * Transfer status update allowed.
     *
     * @param id
     *            the id
     * @param code
     *            the code
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public boolean transferStatusUpdateAllowed(final long id, final String code) throws MasterException {
        final var monitor = new MonitorCall("transferStatusUpdateAllowed(" + id + "," + code + ")");
        return monitor.done(master.updateTransferStatus(id, code, false, null, null, true, false, true));
    }

    /**
     * Update transfer status.
     *
     * @param session
     *            the session
     * @param id
     *            the id
     * @param code
     *            the code
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     */
    @Override
    public boolean updateTransferStatus(final ECpdsSession session, final long id, final String code)
            throws MasterException {
        final var monitor = new MonitorCall(
                "updateTransferStatus(" + session.getWebUser().getName() + "," + id + "," + code + ")");
        final var transfer = master.getDataTransfer(id);
        if (transfer == null) {
            throw new MasterException("DataTransfer not found: " + id);
        }
        final var action = master.startECpdsAction(session, "update", transfer);
        final var webUser = action.getECpdsSession().getWebUser();
        Exception exception = null;
        var result = false;
        try {
            result = master.updateTransferStatus(id, code, true, action.getWebUserId(),
                    "By WebUser=" + webUser.getId() + " (" + webUser.getName() + ") from the web interface", true, true,
                    true);
            if (result) {
                master.reloadDestination(transfer);
            }
            return monitor.done(result);
        } catch (final MasterException e) {
            exception = e;
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action, "Setting status to " + StatusFactory.getDataTransferStatusName(true, code),
                    exception != null ? exception
                            : result ? null : new MasterException("DataTransfer status update not allowed"));
        }
    }

    /**
     * Resend data transfer events.
     *
     * @param root
     *            the root
     * @param dataTransferEventRequests
     *            the data transfer event requests
     */
    @Override
    public void resendDataTransferEvents(final String root,
            final DataTransferEventRequest[] dataTransferEventRequests) {
        final var monitor = new MonitorCall("resendDataTransferEvents(" + root + ",DataTransferEventRequest["
                + dataTransferEventRequests.length + "])");
        var eventsCount = 0;
        for (final DataTransferEventRequest request : dataTransferEventRequests) {
            try {
                final var destination = base.getDestination(request.getDestinationName());
                if (!destination.getMonitor()) {
                    continue;
                }
                final var destinationName = destination.getName();
                final var metaStream = request.getMetaStream();
                final var metaTime = request.getMetaTime();
                final var transfers = base.getDataTransfersByDestinationAndMetaData(request.getFrom(), request.getTo(),
                        destinationName, metaStream, metaTime);
                final var fromCache = master.getDataTransfers(destinationName);
                for (final DataTransfer transfer : fromCache) {
                    transfers.put(transfer.getId(), transfer);
                }
                final var reset = new ResetDestinationProductEvent(destinationName, metaStream, metaTime);
                reset.setTarget(root);
                final var array = transfers.values().toArray(new DataTransfer[0]);
                final var length = array.length;
                final var events = new PluginEvent<?>[length + 1];
                events[0] = reset;
                for (var k = 0; k < length; k++) {
                    events[1 + k] = new DataTransferEvent(root, array[k]);
                    events[1 + k].setSource("ManagementImpl.resendDataTransferEvents");
                }
                master.handle(events);
                eventsCount += length;
            } catch (final Exception e) {
                _log.warn("Resending DataTransfer(s)", e);
            }
        }
        monitor.done(eventsCount);
        if (dataTransferEventRequests.length > 0) {
            _log.debug("{} request(s) submitted: {} DataTransferEvent(s) sent", dataTransferEventRequests.length,
                    eventsCount);
        }
    }

    /**
     * Exec.
     *
     * @param session
     *            the session
     * @param environment
     *            the environment
     * @param request
     *            the request
     * @param service
     *            the service
     *
     * @return the byte[]
     *
     * @throws MasterException
     *             the master exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public byte[] exec(final ECpdsSession session, final Map<String, String> environment, final byte[] request,
            final String service) throws MasterException, IOException {
        final var monitor = new MonitorCall("exec(" + session.getWebUser().getName() + ",Map(" + environment.size()
                + "),byte[" + request.length + "]," + service + ")");
        var host = Cnf.at("Services", service);
        var port = -1;
        final int index;
        if (host != null && (index = host.indexOf(":")) != -1) {
            try {
                port = Integer.parseInt(host.substring(index + 1));
                host = host.substring(0, index);
            } catch (final Exception e) {
                _log.warn(e);
            }
        }
        if (host == null || port == -1) {
            throw new MasterException("Service not available");
        }
        final var socketConfig = new SocketConfig("ManagementEXEC");
        final var socket = socketConfig.getSocket(host, port);
        final var out = socket.getOutputStream();
        out.write((environment.size() + "\n").getBytes());
        out.flush();
        for (final Entry<String, String> entry : environment.entrySet()) {
            out.write((entry.getKey() + "\n").getBytes());
            out.write((entry.getValue() + "\n").getBytes());
            out.flush();
        }
        out.write(request);
        out.flush();
        final var result = new ByteArrayOutputStream();
        final var buffer = new byte[1024];
        final var in = socket.getInputStream();
        int read;
        while ((read = StreamPlugThread.readFully(in, buffer, 0, 1024)) > 0) {
            result.write(buffer, 0, read);
        }
        socket.close();
        return monitor.done(result.toByteArray());
    }

    /**
     * Gets the destination scheduler cache.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination scheduler cache
     *
     * @throws MasterException
     *             the master exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public DestinationSchedulerCache getDestinationSchedulerCache(final String destinationName)
            throws MasterException, RemoteException {
        final var monitor = new MonitorCall("getDestinationSchedulerCache(" + destinationName + ")");
        try {
            final var cache = master.getDestinationSchedulerCache(destinationName);
            cache.setDataTransfersInCache(master.getDataTransfers(destinationName));
            return monitor.done(cache);
        } catch (final MasterException e) {
            _log.warn(e);
            throw e;
        } catch (final Exception e) {
            _log.warn(e);
            throw new MasterException(e.getMessage());
        }
    }

    /**
     * Compute filter efficiency.
     *
     * @param session
     *            the session
     * @param destinationName
     *            the destination name
     * @param email
     *            the email
     * @param filter
     *            the filter
     * @param date
     *            the date
     * @param includeStdby
     *            the include stdby
     * @param pattern
     *            the pattern
     *
     * @return the string
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws MasterException
     *             the master exception
     */
    @Override
    public String computeFilterEfficiency(final ECpdsSession session, final String destinationName, final String email,
            final String filter, final long date, final boolean includeStdby, final String pattern)
            throws DataBaseException, MasterException {
        final var monitor = new MonitorCall(
                "computeFilterEfficiency(" + session.getWebUser().getName() + "," + destinationName + "," + email + ","
                        + filter + "," + Format.formatTime(date) + "," + includeStdby + "," + pattern + ")");
        final var action = master.startECpdsAction(session, "compress", base.getDestination(destinationName));
        String message = null;
        Exception exception = null;
        try {
            return monitor.done(message = master.computeFilterEfficiency(destinationName, email, filter, date,
                    includeStdby, pattern));
        } catch (final Exception e) {
            exception = e;
            _log.warn(e);
            throw new MasterException(e.getMessage());
        } finally {
            master.logECpdsAction(action,
                    "Computing Filter Efficiency using " + filter + " on " + Format.formatTime(date)
                            + " (notification sent to " + email + ")" + (isNotEmpty(message) ? ": " + message : ""),
                    exception);
        }
    }
}
