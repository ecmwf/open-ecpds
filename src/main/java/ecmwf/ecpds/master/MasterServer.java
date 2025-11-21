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

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_ECTRANS;
import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_MQTT;
import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_SCHEDULER;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ACQUISITION;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECTRANS;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_PROXY;
import static ecmwf.common.ectrans.ECtransGroups.Module.USER_PORTAL;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_CLIENT_ID;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_CONTENT_TYPE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_EXPIRY_INTERVAL;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_PAYLOAD;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_QOS;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_RETAIN;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_TOPIC;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_FORCE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_FORCE_STOP;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_MASTER_TO_NOTIFY_ON_DONE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_RESET_QUEUE_ON_CHANGE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_STANDBY;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_ECTRANS_FILTER_MINIMUM_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_ECTRANS_FILTERPATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_ACTION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DATEDELTA;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DATEFORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DATEPATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DATESOURCE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DEBUG;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DEFAULT_DATE_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_DELETEORIGINAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_EVENT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_FILEAGE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_FILESIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_GROUPBY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_INTERRUPT_SLOW;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIFETIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIST_MAX_THREADS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIST_MAX_WAITING;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIST_PARALLEL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_LIST_SYNCHRONOUS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_MAXIMUM_DURATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_METADATA;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_NORETRIEVAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_ONLY_VALID_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_PAYLOAD_EXTENSION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_PRIORITY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_RECENT_DATE_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REGEX_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REGEX_PATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REMOVE_PARAMETERS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUEON;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUEONSAMESIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUEONUPDATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_REQUEUE_ON_FAILURE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SERVER_LANGUAGE_CODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SERVER_TIME_ZONE_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SHORT_MONTH_NAMES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_STANDBY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_SYSTEM_KEY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_TARGET;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_TRANSFERGROUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_UNIQUE_BY_NAME_AND_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_UNIQUE_BY_TARGET_ONLY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_USE_SYMLINK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_USE_TARGET_AS_UNIQUE_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ACQUISITION_WILDCARD_FILTER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_FILTERPATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_LASTUPDATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_FILTER_MINIMUM_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_ANONYMOUS;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_GEOBLOCLING;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_MAX_CONNECTIONS;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_UPDATE_LAST_LOGIN_INFORMATION;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_USE_PASSCODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_PROXY_USE_DESTINATION_FILTER;
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;
import static ecmwf.common.text.Util.nullToNone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.timer.Timer;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Value;

import com.maxmind.geoip2.exception.AddressNotFoundException;

import ecmwf.common.callback.RemoteInputStreamImp;
import ecmwf.common.database.Alias;
import ecmwf.common.database.Association;
import ecmwf.common.database.ChangeLog;
import ecmwf.common.database.DBIterator;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataBaseObject;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.DestinationECUser;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.ECpdsBase.DataTransferCache;
import ecmwf.common.database.ExistingStorageDirectory;
import ecmwf.common.database.Host;
import ecmwf.common.database.HostLocation;
import ecmwf.common.database.HostOutput;
import ecmwf.common.database.HostStats;
import ecmwf.common.database.IncomingConnection;
import ecmwf.common.database.IncomingUser;
import ecmwf.common.database.Publication;
import ecmwf.common.database.SchedulerValue;
import ecmwf.common.database.TransferGroup;
import ecmwf.common.database.TransferHistory;
import ecmwf.common.database.TransferServer;
import ecmwf.common.database.UploadHistory;
import ecmwf.common.database.WebUser;
import ecmwf.common.ecaccess.AbstractTicket;
import ecmwf.common.ecaccess.ClientInterface;
import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.common.ecaccess.ECaccessProvider;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.common.ecaccess.ECauthTokenGenerator;
import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.HandlerInterface;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.ecaccess.MailMBean;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.StorageRepository;
import ecmwf.common.ecaccess.TicketRepository;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ftp.FtpParser;
import ecmwf.common.ftp.FtpParser.FileEntry;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.mbean.MBeanService;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.plugin.EventComparator;
import ecmwf.common.plugin.PluginContainer;
import ecmwf.common.plugin.PluginEvent;
import ecmwf.common.security.LoginManagement;
import ecmwf.common.security.TOTP;
import ecmwf.common.security.Tools;
import ecmwf.common.starter.Starter;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ExecutorManager;
import ecmwf.common.technical.ExecutorRunnable;
import ecmwf.common.technical.GeoIP2Helper;
import ecmwf.common.technical.MonitoredOutputStream;
import ecmwf.common.technical.ScriptManager;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.Synchronized;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.common.text.Options;
import ecmwf.common.version.Version;
import ecmwf.ecpds.master.plugin.ecpds.ECpdsClient;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferComparator;
import ecmwf.ecpds.master.transfer.TransferManagement;
import ecmwf.ecpds.master.transfer.TransferScheduler;
import ecmwf.ecpds.master.transfer.TransferScheduler.AcquisitionResult;
import ecmwf.ecpds.master.transfer.TransferScheduler.BackupResult;
import ecmwf.ecpds.master.transfer.TransferScheduler.DestinationThread;
import ecmwf.ecpds.master.transfer.TransferScheduler.MonitoringThread;
import ecmwf.ecpds.master.transfer.TransferScheduler.PurgeResult;
import ecmwf.ecpds.mover.MoverInterface;

/**
 * The Class MasterServer.
 */
public final class MasterServer extends ECaccessProvider
        implements HandlerInterface, MasterInterface, ECaccessInterface {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3373001425401551480L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(MasterServer.class);

    /** The Constant host outputs. */
    private static final transient String HOST_OUTPUTS = Cnf.at("Server", "hostoutputs") + File.separator;

    /** The Constant _splunk. */
    private static final Logger _splunk = LogManager.getLogger("SplunkLogs");

    /** The Constant _events. */
    private static final Logger _events = LogManager.getLogger("EventLogs");

    /** The transfer scheduler. */
    private final transient TransferScheduler theTransferScheduler;

    /** The transfer repository. */
    private final transient TransferRepository theTransferRepository;

    /** The history repository. */
    private final transient HistoryRepository theHistoryRepository;

    /** The event repository. */
    private final transient EventRepository theEventRepository;

    /** The proxy host repository. */
    private final transient ProxyHostRepository theProxyHostRepository;

    /** The notification repository. */
    private final transient NotificationRepository theNotificationRepository;

    /** The ticket repository. */
    private final transient TicketRepository theTicketRepository;

    /** The mail repository. */
    private final transient MailMBean theMailRepository;

    /** The host check scheduler. */
    private final transient HostCheckScheduler theHostCheckScheduler;

    /** The replicate scheduler. */
    private final transient ReplicateScheduler theReplicateScheduler;

    /** The backup scheduler. */
    private final transient BackupScheduler theBackupScheduler;

    /** The proxy scheduler. */
    private final transient ProxyScheduler theProxyScheduler;

    /** The event scheduler. */
    private final transient EventScheduler theEventScheduler;

    /** The acquisition scheduler. */
    private final transient AcquisitionScheduler theAcquisitionScheduler;

    /** The purge scheduler. */
    private final transient PurgeScheduler thePurgeScheduler;

    /** The diss download scheduler. */
    private final transient DissDownloadScheduler theDissDownloadScheduler;

    /** The acq download scheduler. */
    private final transient AcqDownloadScheduler theAcqDownloadScheduler;

    /** The filter scheduler. */
    private final transient FilterScheduler theFilterScheduler;

    /** The data transfer check. */
    private final transient DataTransferCheck theDataTransferCheck;

    /** The host mutex provider. */
    private final transient Synchronized hostMutexProvider = new Synchronized();

    /** The host stats mutex provider. */
    private final transient Synchronized hostStatsMutexProvider = new Synchronized();

    /** The host location mutex provider. */
    private final transient Synchronized hostLocationMutexProvider = new Synchronized();

    /** The host output mutex provider. */
    private final transient Synchronized hostOutputMutexProvider = new Synchronized();

    /** The current transfers. */
    private final transient Map<String, ProgressInterface> currentTransfers = new ConcurrentHashMap<>();

    /** The current dataFiles. */
    private final transient Map<Long, ProgressInterface> currentDataFiles = new ConcurrentHashMap<>();

    /** The incoming connection ids. */
    private final transient Map<String, List<IncomingConnection>> incomingConnectionIds = new ConcurrentHashMap<>();

    /** The management. */
    private final transient ManagementImpl management;

    /** The database access. */
    private final transient DataBaseImpl databaseAccess;

    /** The attachment access. */
    private final transient AttachmentAccessImpl attachmentAccess;

    /** The data access. */
    private final transient DataFileAccessImpl dataAccess;

    /** The transfer server management. */
    private final transient TransferServerManagement transferServerManagement;

    /** The event script content. */
    private final StringBuffer eventScriptContent = new StringBuffer(
            Cnf.fileContentAt("Scheduler", "eventScriptFile", ""));

    /** The notify local container. */
    private final transient boolean notifyLocalContainer = Cnf.at("Server", "notifyLocalContainer", true);

    /**
     * The containers to notify. By default the ECpdsMonitor running on this server.
     */
    private final transient String containersToNotify = Cnf.at("Server", "containersToNotify",
            "ECpdsMonitor/" + getRoot());

    /** The allowed password on. By default the DNS or IP address of this server. */
    private final transient String allowedPasswordOn = Cnf.at("Server", "allowPasswordOn",
            "(" + getRoot() + "|" + Format.getHostAddress(getRoot()) + ")");

    /** The local container. */
    private final transient PluginContainer localContainer;

    /** The schedule clone id. */
    private transient Integer scheduleCloneId = null;

    /** The send mail notifications for transfers. */
    private transient boolean sendMailNotificationsForTransfers = Cnf.at("Server", "sendMailNotificationsForTransfers",
            true);

    /** The mail subject tag. */
    private final transient String mailSubjectTag = Cnf.at("Server", "mailSubjectTag", getRoot());

    /** The MQTT token used in the connect parameters. */
    private static final String MQTT_TOKEN = ";notification=mqtt";

    /**
     * Instantiates a new master server.
     *
     * @param starter
     *            the starter
     *
     * @throws SQLException
     *             the SQL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws MBeanRegistrationException
     *             the MBean registration exception
     * @throws NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     * @throws InstanceNotFoundException
     *             the instance not found exception
     * @throws ConnectionException
     *             the connection exception
     * @throws DataBaseException
     *             the data base exception
     */
    public MasterServer(final Starter starter)
            throws SQLException, IOException, IllegalAccessException, InstantiationException, ClassNotFoundException,
            InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, InstanceNotFoundException, ConnectionException, DataBaseException {
        super(new ECpdsBase(), starter);
        _log.info("MasterServer-version: {}", Version.getFullVersion());
        NativeAuthenticationProvider.setProvider(MasterProvider.class);
        DestinationOption.getList(); // Make sure the list of options is loaded!
        transferServerManagement = new TransferServerManagement(this);
        if (Cnf.at("Server", "transferScheduler", true)) {
            _log.debug("Starting TransferScheduler");
            theTransferScheduler = new TransferScheduler("TransferScheduler");
            theTransferScheduler.start();
        } else {
            theTransferScheduler = null;
        }
        if (Cnf.at("Server", "ticketRepository", true)) {
            _log.debug("Starting TicketRepository");
            theTicketRepository = new TicketRepository("TicketRepository");
            theTicketRepository.start();
        } else {
            theTicketRepository = null;
        }
        if (Cnf.at("Server", "transferRepository", true)) {
            _log.debug("Starting TransferRepository");
            theTransferRepository = new TransferRepository("TransferRepository");
            theTransferRepository.start();
        } else {
            theTransferRepository = null;
        }
        if (Cnf.at("Server", "proxyHostRepository", true)) {
            _log.debug("Starting ProxyHostRepository");
            theProxyHostRepository = new ProxyHostRepository("ProxyHostRepository");
            theProxyHostRepository.start();
        } else {
            theProxyHostRepository = null;
        }
        if (Cnf.at("Server", "historyRepository", true)) {
            _log.debug("Starting HistoryRepository");
            theHistoryRepository = new HistoryRepository("HistoryRepository");
            theHistoryRepository.start();
        } else {
            theHistoryRepository = null;
        }
        if (Cnf.at("Server", "eventRepository", true)) {
            _log.debug("Starting EventRepository");
            theEventRepository = new EventRepository("EventRepository");
            theEventRepository.start();
            if (!eventScriptContent.isEmpty() && Cnf.at("Server", "eventScheduler", true)) {
                _log.debug("Starting EventScheduler");
                theEventScheduler = new EventScheduler("EventScheduler");
                theEventScheduler.start();
            } else {
                theEventScheduler = null;
            }
        } else {
            theEventRepository = null;
            theEventScheduler = null;
        }
        if (Cnf.at("Server", "mailRepository", true)) {
            _log.debug("Starting MailRepository");
            theMailRepository = new MailMBean("MailRepository");
            theMailRepository.setPriority(Thread.MIN_PRIORITY);
            theMailRepository.start();
        } else {
            theMailRepository = null;
        }
        if (Cnf.at("Server", "notificationRepository", true)) {
            _log.debug("Starting NotificationRepository");
            theNotificationRepository = new NotificationRepository("NotificationRepository");
            theNotificationRepository.start();
        } else {
            theNotificationRepository = null;
        }
        if (Cnf.at("Server", "hostCheckScheduler", true)) {
            _log.debug("Starting HostCheckScheduler");
            theHostCheckScheduler = new HostCheckScheduler("HostCheckScheduler");
            theHostCheckScheduler.setPriority(Thread.MIN_PRIORITY);
            theHostCheckScheduler.start();
        } else {
            theHostCheckScheduler = null;
        }
        if (Cnf.at("Server", "dataTransferCheck", true)) {
            _log.debug("Starting DataTransferCheck");
            theDataTransferCheck = new DataTransferCheck("DataTransferCheck");
            theDataTransferCheck.start();
        } else {
            theDataTransferCheck = null;
        }
        management = new ManagementImpl(this);
        databaseAccess = new DataBaseImpl(this, getECpdsBase());
        attachmentAccess = new AttachmentAccessImpl(this);
        dataAccess = new DataFileAccessImpl(this);
        localContainer = getPluginContainer();
        localContainer.loadPlugins();
        try {
            new MBeanManager("ECaccess:service=ECUser", new ECUserMBean());
        } catch (final Exception e) {
            _log.warn("Starting MBeanManager", e);
        }
        if (theTransferScheduler != null) {
            while (!theTransferScheduler.isInitialized()) {
                _log.info("Wait for TransferScheduler to initialize");
                try {
                    Thread.sleep(4 * Timer.ONE_SECOND);
                } catch (final InterruptedException e) {
                }
            }
        }
        if (Cnf.at("Server", "replicateScheduler", true)) {
            _log.debug("Starting ReplicateScheduler");
            theReplicateScheduler = new ReplicateScheduler("ReplicateScheduler");
            theReplicateScheduler.start();
        } else {
            theReplicateScheduler = null;
        }
        if (Cnf.at("Server", "backupScheduler", true)) {
            _log.debug("Starting BackupScheduler");
            theBackupScheduler = new BackupScheduler("BackupScheduler");
            theBackupScheduler.start();
        } else {
            theBackupScheduler = null;
        }
        if (Cnf.at("Server", "proxyScheduler", true)) {
            _log.debug("Starting ProxyScheduler");
            theProxyScheduler = new ProxyScheduler("ProxyScheduler");
            theProxyScheduler.start();
        } else {
            theProxyScheduler = null;
        }
        if (Cnf.at("Server", "acquisitionScheduler", true)) {
            _log.debug("Starting AcquisitionScheduler");
            theAcquisitionScheduler = new AcquisitionScheduler("AcquisitionScheduler");
            theAcquisitionScheduler.start();
        } else {
            theAcquisitionScheduler = null;
        }
        if (Cnf.at("Server", "purgeScheduler", true)) {
            _log.debug("Starting PurgeScheduler");
            thePurgeScheduler = new PurgeScheduler("PurgeScheduler");
            thePurgeScheduler.start();
        } else {
            thePurgeScheduler = null;
        }
        if (Cnf.at("Server", "acqDownloadScheduler", true)) {
            _log.debug("Starting AcqDownloadScheduler");
            theAcqDownloadScheduler = new AcqDownloadScheduler("AcqDownloadScheduler");
            theAcqDownloadScheduler.start();
        } else {
            theAcqDownloadScheduler = null;
        }
        if (Cnf.at("Server", "dissDownloadScheduler", true)) {
            _log.debug("Starting DissDownloadScheduler");
            theDissDownloadScheduler = new DissDownloadScheduler("DissDownloadScheduler");
            theDissDownloadScheduler.start();
        } else {
            theDissDownloadScheduler = null;
        }
        if (Cnf.at("Server", "filterScheduler", true)) {
            _log.debug("Starting FilterScheduler");
            theFilterScheduler = new FilterScheduler("FilterScheduler");
            theFilterScheduler.start();
        } else {
            theFilterScheduler = null;
        }
        _log.debug("Monitors to notify: {}", containersToNotify);
        _log.debug("Passwords allowed from: {}", allowedPasswordOn);
        localContainer.startPlugins();
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    @Override
    public String getVersion() {
        return Version.getFullVersion();
    }

    /**
     * Gets the management interface.
     *
     * @return the management interface
     */
    @Override
    public ManagementInterface getManagementInterface() {
        return management;
    }

    /**
     * Gets the data base interface.
     *
     * @return the data base interface
     */
    @Override
    public DataBaseInterface getDataBaseInterface() {
        return databaseAccess;
    }

    /**
     * Gets the attachment access interface.
     *
     * @return the attachment access interface
     */
    @Override
    public DataAccessInterface getAttachmentAccessInterface() {
        return attachmentAccess;
    }

    /**
     * Gets the data file access interface.
     *
     * @return the data file access interface
     */
    @Override
    public DataAccessInterface getDataFileAccessInterface() {
        return dataAccess;
    }

    /**
     * Gets the event script content.
     *
     * @return the event script content
     */
    public String getEventScriptContent() {
        return eventScriptContent.toString();
    }

    /**
     * Gets the destination.
     *
     * @param name
     *            the name
     *
     * @return the destination
     */
    @Override
    public Destination getDestination(final String name) {
        return getDataBase().getDestinationObject(name);
    }

    /**
     * Checks if is valid data file.
     *
     * This method is called by the cleaning scheduler on the DataMovers to check if a DataFile still exists and is
     * valid? Otherwise the DataFile is removed from the DataMover storage system. In case of error, the data file is
     * considered as valid to avoid its deletion.
     *
     * @param isProxy
     *            the is proxy
     * @param dataFileId
     *            the data file id
     *
     * @return true, if is valid data file
     */
    @Override
    public boolean isValidDataFile(final boolean isProxy, final long dataFileId) {
        try {
            // Should it be deleted already (after expiration)?
            return getECpdsBase().isValidDataFile(isProxy, dataFileId);
        } catch (final Throwable t) {
            // If we don't know let's keep it. For example we might have a
            // problem with the DataBase connection!
            _log.warn("No info for DataFile {}", dataFileId, t);
            return true;
        }
    }

    /**
     * The data cache. Allow getting latest update of the DataTransfer when calling method from The data cache!
     */
    public final transient DataTransferCache dataCache = transfer -> {
        final var fromCache = getDataTransferFromCache(transfer.getId());
        return fromCache != null ? fromCache : transfer;
    };

    /**
     * Gets the incoming user hash.
     *
     * @param incomingUser
     *            the incoming user
     *
     * @return the incoming user hash
     */
    @Override
    public String getIncomingUserHash(final String incomingUser) {
        try {
            return _getIncomingUserHash(getECpdsBase().getIncomingUser(incomingUser));
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Return a hash of the incoming user in the form user:password.
     *
     * @param user
     *            the user
     *
     * @return the string
     */
    private static String _getIncomingUserHash(final IncomingUser user) {
        return Format.getHash((user.getId() + ":" + user.getPassword()).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the s 3 authorization signature.
     *
     * @param incomingUser
     *            the incoming user
     * @param prefix
     *            the prefix
     * @param data
     *            the data
     * @param algorithm
     *            the algorithm
     *
     * @return the s 3 authorization signature
     */
    @Override
    public byte[] getS3AuthorizationSignature(final String incomingUser, final String prefix, final String data,
            final String algorithm) {
        try {
            final var user = getECpdsBase().getIncomingUser(incomingUser);
            final var mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec((prefix + user.getPassword()).getBytes(StandardCharsets.UTF_8), algorithm));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (final Throwable t) {
            _log.warn("getS3AuthorizationSignature", t);
            return null;
        }
    }

    /**
     * Gets the incoming profile.
     *
     * @param incomingUser
     *            the incoming user
     * @param incomingPassword
     *            the incoming password
     * @param from
     *            the from
     *
     * @return the incoming profile
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public IncomingProfile getIncomingProfile(final String incomingUser, final String incomingPassword,
            final String from) throws RemoteException {
        try {
            // Is it a valid user?
            final var base = getECpdsBase();
            final var user = base.getIncomingUserObject(incomingUser);
            if (user == null) {
                if (_splunk.isInfoEnabled())
                    _splunk.info("DEA;UserId={};Message=Not found;Context={}", incomingUser, from);
                throw new MasterException("Login failed");
            }
            // Let's get the data from the user and the data policies!
            final var setup = USER_PORTAL.getECtransSetup(base.getDataFromUserPolicies(user));
            final var geoblocking = setup.getStringList(USER_PORTAL_GEOBLOCLING);
            var blocked = false;
            if (!geoblocking.isEmpty()) {
                blocked = true;
                if (isNotEmpty(from)) {
                    final var ipAddress = from.split("@")[1];
                    // Local host is obviously not in the database!
                    if (!"127.0.0.1".equals(ipAddress) && !"::1".equals(ipAddress)) {
                        try {
                            final var response = GeoIP2Helper.getCityResponse(ipAddress);
                            final var continent = response.getContinent().getName();
                            final var country = response.getCountry().getIsoCode();
                            final var city = response.getCity().getName();
                            blocked = geoblocking.stream().anyMatch(str -> str.equalsIgnoreCase(continent)
                                    || str.equalsIgnoreCase(country) || str.equalsIgnoreCase(city));
                            if (blocked) {
                                _log.warn("Geolocation restriction for incoming user {} ({}->{}->{})", incomingUser,
                                        continent, country, city);
                            }
                        } catch (final AddressNotFoundException e) {
                            _log.debug("No location found for {}", ipAddress);
                        } catch (final Throwable t) {
                            _log.warn("Getting location for {}", ipAddress, t);
                        }
                    }
                }
            }
            if (blocked) {
                if (_splunk.isInfoEnabled())
                    _splunk.info("DEA;UserId={};Message=Geolocation restriction;Context={}", incomingUser, from);
                throw new MasterException("Login failed");
            }
            if (!user.getActive()) {
                if (_splunk.isInfoEnabled())
                    _splunk.info("DEA;UserId={};Message=Disabled;Context={}", incomingUser, from);
                throw new MasterException("Login failed");
            }
            if (setup.getBoolean(USER_PORTAL_ANONYMOUS)) {
                // This is an anonymous user so no authentication required!
            } else if (incomingPassword != null) {
                if (user.getSynchronized()) {
                    // User/password authentication through TOTP!
                    boolean authenticated;
                    try {
                        authenticated = TOTP.authenticate(incomingUser, incomingPassword,
                                setup.getBoolean(USER_PORTAL_USE_PASSCODE));
                    } catch (IOException | URISyntaxException e) {
                        authenticated = false;
                    }
                    if (!authenticated) {
                        if (_splunk.isInfoEnabled())
                            _splunk.info("DEA;UserId={};Message=TOTP authentication failed;Context={}", incomingUser,
                                    from);
                        throw new MasterException("Login failed");
                    }
                } else {
                    // User/password authentication against the database
                    final var localPassword = user.getPassword();
                    if (localPassword != null && !localPassword.equals(incomingPassword)
                            && !_getIncomingUserHash(user).equals(incomingPassword)) {
                        if (_splunk.isInfoEnabled())
                            _splunk.info("DEA;UserId={};Message=Password authentication failed;Context={}",
                                    incomingUser, from);
                        throw new MasterException("Login failed");
                    }
                    if (localPassword == null) {
                        // There was no password set for this user!
                        if (_splunk.isInfoEnabled())
                            _splunk.info("DEA;UserId={};Message=Password not set;Context={}", incomingUser, from);
                        _log.debug("Password not set for IncomingUser {}", incomingUser);
                        throw new MasterException("Login failed");
                    }
                }
            }
            // Let's check if the maximum number of connections have not been
            // reached for this user!
            final var count = _getIncomingConnectionCountFor(incomingUser);
            if (count >= setup.getInteger(USER_PORTAL_MAX_CONNECTIONS)) {
                final var message = "Maximum number of connections exceeded (" + count + ")";
                if (_splunk.isInfoEnabled())
                    _splunk.info("DEA;UserId={};Message={};Context={}", incomingUser, message, from);
                _log.warn("{} for IncomingUser {}", message, incomingUser);
                throw new MasterException(message);
            }
            // Look for the Destinations accessible to this user!
            final List<Destination> destinations = new ArrayList<>();
            Collections.addAll(destinations, base.getDestinationsForIncomingUser(incomingUser));
            for (final Destination destination : base.getDestinationsByUserPolicies(incomingUser)) {
                if (!destinations.contains(destination)) {
                    destinations.add(destination);
                }
            }
            if (destinations.isEmpty()) {
                if (_splunk.isInfoEnabled())
                    _splunk.info("DEA;UserId={};Message=No associated Destinations;Context={}", incomingUser, from);
                throw new MasterException("Login failed");
            }
            // Look for the Permissions associated to this user!
            final var permissions = base.getIncomingPermissionsForIncomingUser(incomingUser);
            if (permissions.isEmpty()) {
                if (_splunk.isInfoEnabled())
                    _splunk.info("DEA;UserId={};Message=No associated Permissions;Context={}", incomingUser, from);
                throw new MasterException("Login failed");
            }
            // Let's update with the last login informations!
            user.setLastLogin(new Timestamp(System.currentTimeMillis()));
            user.setLastLoginHost(from);
            if (setup.getBoolean(USER_PORTAL_UPDATE_LAST_LOGIN_INFORMATION)) { // Do we persist?
                base.update(user);
            }
            // Let's pass the data from data policies to the mover!
            user.setData(setup.getData());
            return new IncomingProfile(user, permissions, destinations);
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Gets the transfer scheduler.
     *
     * @return the transfer scheduler
     *
     * @throws MasterException
     *             the master exception
     */
    public TransferScheduler getTransferScheduler() throws MasterException {
        if (theTransferScheduler == null) {
            throw new MasterException("TransferScheduler not started");
        }
        return theTransferScheduler;
    }

    /**
     * Check if a lock has not yet been set on the specific key. If the lock is not set then lock on behalf of the
     * object parameter otherwise return the object which has been used to lock the key. This method is dedicated to the
     * ECpdsPlugin which use it to avoid having parallel requests for the same transfer request.
     *
     * @param key
     *            the unique key
     * @param plugin
     *            the plugin
     *
     * @return the ecpds plugin interface
     */
    public ProgressInterface lockTransfer(final String key, final ProgressInterface plugin) {
        final ProgressInterface found;
        synchronized (currentTransfers) {
            if ((found = currentTransfers.get(key)) == null) {
                currentTransfers.put(key, plugin);
            }
        }
        return found;
    }

    /**
     * Lock a data file. This method is used by the ECpdsPlugin to record which DataFiles are in the process of being
     * retrieved without using a Download Scheduler.
     *
     * @param key
     *            the key
     */
    public void lockDataFile(final String key) {
        final var progress = currentTransfers.get(key);
        if (progress != null) {
            // The plugin is already registered.
            currentDataFiles.put(progress.getDataFileId(), progress);
        } else {
            // This shouldn't happen!
            _log.warn("Couldn't lock DataFile for key={}", key);
        }
    }

    /**
     * Unlock the data transfer and associated data file for the specified unique key.
     *
     * @param key
     *            the unique key of the data transfer
     */
    public void unlockTransfer(final String key) {
        final var progress = currentTransfers.remove(key);
        if (progress != null) {
            currentDataFiles.remove(progress.getDataFileId());
        }
        if (currentTransfers.size() == 0) {
            // Just to make sure we don't have left over!
            _log.debug("DataTransfer lock queue size=0, DataFile lock queue size={}", currentDataFiles.size());
        }
    }

    /**
     * Check if the data transfer for the specified unique key is locked. This method is used by the DownloadScheduler
     * to avoid downloading a file which is still processed by the ECpdsPlugin.
     *
     * @param key
     *            the unique key of the data transfer
     *
     * @return true, if successful
     */
    public boolean transferIsLocked(final String key) {
        return currentTransfers.containsKey(key);
    }

    /**
     * Gets the download scheduler.
     *
     * @param acquisition
     *            the acquisition
     *
     * @return the download scheduler
     *
     * @throws MasterException
     *             the master exception
     */
    public DownloadScheduler getDownloadScheduler(final boolean acquisition) throws MasterException {
        final var downloadScheduler = acquisition ? theAcqDownloadScheduler : theDissDownloadScheduler;
        if (downloadScheduler == null) {
            throw new MasterException("DownloadScheduler not started");
        }
        return downloadScheduler;
    }

    /**
     * Gets the file in the host dir.
     *
     * @param host
     *            the host
     * @param temporary
     *            the temporary
     *
     * @return the file
     */
    private static File _getHostOutputFile(final Host host, final boolean temporary) {
        final var setup = HOST_ACQUISITION.getECtransSetup(host.getData());
        final var fileName = new StringBuilder(HOST_OUTPUTS)
                .append(Format.formatValue(Integer.parseInt(host.getName()), 15));
        if (setup.getBoolean(HOST_ACQUISITION_LIST_SYNCHRONOUS)) {
            // The file if GZipped!
            fileName.append(temporary ? ".tmp" : "").append(".gz");
        } else {
            // This is a plain-text file
            fileName.append(".txt");
        }
        final var file = new File(fileName.toString());
        if (temporary) {
            final var parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
        }
        return file;
    }

    /**
     * Gets the transfer servers.
     *
     * @param file
     *            the file
     *
     * @return the transfer server[]
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private TransferServer[] _getTransferServers(final DataFile file) throws DataBaseException {
        return getECpdsBase().getTransferServersByDataFileId(file.getId());
    }

    /**
     * Gets the data transfers.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the data transfers
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public DataTransfer[] getDataTransfers(final long dataFileId) throws DataBaseException {
        return getECpdsBase().getDataTransfersByDataFileId(dataCache, dataFileId).toArray(new DataTransfer[0]);
    }

    /**
     * Gets the associations.
     *
     * @param associations
     *            the associations
     * @param destinationName
     *            the destination name
     *
     * @return the map
     */
    private static Map<String, Association> _getAssociations(final Association[] associations,
            final String destinationName) {
        final Map<String, Association> result = new ConcurrentHashMap<>();
        for (final Association association : associations) {
            if (destinationName.equals(association.getDestinationName())) {
                result.put(association.getHostName(), association);
            }
        }
        return result;
    }

    /**
     * Gets the aliases.
     *
     * @param aliases
     *            the aliases
     * @param destinationName
     *            the destination name
     *
     * @return the map
     */
    private static Map<String, Alias> _getAliases(final Alias[] aliases, final String destinationName) {
        final Map<String, Alias> result = new ConcurrentHashMap<>();
        for (final Alias alias : aliases) {
            if (destinationName.equals(alias.getDesName())) {
                result.put(alias.getDestinationName(), alias);
            }
        }
        return result;
    }

    /**
     * Gets the destination ecusers.
     *
     * @param ecusers
     *            the ecusers
     * @param destinationName
     *            the destination name
     *
     * @return the hashtable
     */
    private static Map<String, DestinationECUser> _getDestinationECUsers(final DestinationECUser[] ecusers,
            final String destinationName) {
        final Map<String, DestinationECUser> result = new ConcurrentHashMap<>();
        for (final DestinationECUser destinationECUser : ecusers) {
            if (destinationName.equals(destinationECUser.getDestinationName())) {
                result.put(destinationECUser.getECUserName(), destinationECUser);
            }
        }
        return result;
    }

    /**
     * Gets the destination scheduler cache.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination scheduler cache
     *
     * @throws MonitorException
     *             the monitor exception
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    public DestinationSchedulerCache getDestinationSchedulerCache(final String destinationName)
            throws MonitorException, MasterException, DataBaseException {
        return getDestinationSchedulerCache(destinationName, null);
    }

    /**
     * Gets the destination scheduler cache.
     *
     * @param destinationName
     *            the Destination name
     * @param statusCode
     *            the Status Code
     *
     * @return the destination scheduler cache
     *
     * @throws MonitorException
     *             the monitor exception
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    public DestinationSchedulerCache getDestinationSchedulerCache(final String destinationName, final String statusCode)
            throws MonitorException, MasterException, DataBaseException {
        final var scheduler = getTransferScheduler();
        MonitoringThread monitoringThread;
        DestinationThread destinationThread;
        try {
            monitoringThread = scheduler.getMonitoringThread();
        } catch (final MasterException e) {
            monitoringThread = null;
        }
        try {
            destinationThread = scheduler.getDestinationThread(destinationName);
        } catch (final MasterException e) {
            destinationThread = null;
        }
        final var schedulerCache = new DestinationSchedulerCache();
        schedulerCache.setDestinationName(destinationName);
        schedulerCache.setPendingDataTransfersCount(scheduler.getPendingDataTransfersCount(destinationName));
        schedulerCache.setDestinationStatus(scheduler.getDestinationStatus(destinationName, statusCode));
        schedulerCache.setLastFailedTransfer(scheduler.getDestinationLastTransfer(destinationName, false));
        schedulerCache.setLastTransfer(scheduler.getDestinationLastTransfer(destinationName, true));
        if (monitoringThread != null) {
            schedulerCache.setMonitorManager(monitoringThread.getMonitorManager(destinationName));
        }
        if (destinationThread != null) {
            schedulerCache.setDestinationStep(destinationThread.getDestinationStep());
            schedulerCache.setDestinationSize(destinationThread.getSize());
            schedulerCache.setDestinationStartDate(destinationThread.getStartDate());
        }
        return schedulerCache;
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
     */
    public Map<String, DestinationCache> getDestinationCaches()
            throws MonitorException, MasterException, DataBaseException {
        final var base = getECpdsBase();
        final var associations = base.getAssociationArray();
        final var ecusers = base.getDestinationECUserArray();
        final var aliases = base.getAliasArray();
        final var badDataTransfersCount = base.getBadDataTransfersCount();
        final var result = new HashMap<String, DestinationCache>();
        for (final Destination destination : base.getDestinationArray()) {
            final var destinationName = destination.getName();
            try {
                final var cache = new DestinationCache();
                cache.setDestination(destination);
                cache.setAssociations(_getAssociations(associations, destinationName));
                cache.setAliases(_getAliases(aliases, destinationName));
                cache.setDestinationECUsers(_getDestinationECUsers(ecusers, destinationName));
                cache.setBadDataTransfersCount(badDataTransfersCount.get(destinationName));
                cache.setDestinationSchedulerCache(
                        getDestinationSchedulerCache(destinationName, destination.getStatusCode()));
                result.put(destinationName, cache);
            } catch (final Throwable t) {
                _log.warn("Destination {} not added to DestinationCache", destinationName, t);
            }
        }
        return result;
    }

    /**
     * Gets the destination cache. This is called when a host or a destination is duplicated, so the calculation of the
     * number of bad data transfers is not relevant (0 if it is a new destination and same as before in the cache if it
     * is a new host).
     *
     * @param destinationName
     *            the destination name
     *
     * @return the destination cache
     *
     * @throws MonitorException
     *             the monitor exception
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    public DestinationCache getDestinationCache(final String destinationName)
            throws MonitorException, MasterException, DataBaseException {
        final var base = getECpdsBase();
        final var destination = base.getDestination(destinationName);
        final var associations = base.getAssociationArray();
        final var ecusers = base.getDestinationECUserArray();
        final var aliases = base.getAliasArray();
        final var cache = new DestinationCache();
        cache.setDestination(destination);
        cache.setAssociations(_getAssociations(associations, destinationName));
        cache.setAliases(_getAliases(aliases, destinationName));
        cache.setDestinationECUsers(_getDestinationECUsers(ecusers, destinationName));
        cache.setBadDataTransfersCount(0);
        cache.setDestinationSchedulerCache(getDestinationSchedulerCache(destinationName, destination.getStatusCode()));
        return cache;
    }

    /**
     * Gets the ecpds base.
     *
     * @return the ecpds base
     */
    public ECpdsBase getECpdsBase() {
        return getDataBase(ECpdsBase.class);
    }

    /**
     * Gets the ticket repository.
     *
     * @return the ticket repository
     */
    public TicketRepository getTicketRepository() {
        return theTicketRepository;
    }

    /**
     * Purge data base.
     *
     * @param milliseconds
     *            the milliseconds
     */
    @Override
    public void purgeDataBase(final long milliseconds) {
        final var base = getECpdsBase();
        // DataBase maintenance (table optimization and generation of
        // statistics)!
        base.purgeDataBase(milliseconds);
        // Remove all the expired DataTransfers from the DataBase (delete
        // TransferHistory, DataTransfer elements and remove the links to the
        // UploadHistory). If there are more than the specified number of
        // DataTransfer with the same Identity then the rest of the
        // DataTransfers are deleted. If the life time after expiration is more
        // than the one specified then the rest of the DataTransfers are deleted
        // as well. By default we keep all the files entries 210 days after
        // expiration and only 7 instances of the same file!
        final var removed = base.purgeExpiredDataTransfers(Cnf.at("DataBase", "maxIdentityCount", 7),
                Cnf.at("DataBase", "maxTransferLife", 210));
        _log.info("Removed {} DataTransfer(s) entries", removed);
        // Let's now purge all the active DataMovers!
        _purgeDataMovers();
        // And all ProxyHosts!
        _purgeProxyHosts();
    }

    /**
     * Go through every ProxyHost and check for the DataFiles which should be deleted.
     */
    private void _purgeProxyHosts() {
        _log.debug("Starting ProxyHosts purge");
        try (var it = getECpdsBase().getExistingStorageDirectoriesPerProxyHost()) {
            final List<ExistingStorageDirectory> directories = new ArrayList<>();
            String proxyHostName = null;
            while (it.hasNext()) {
                final var directory = it.next();
                // Do we process a new ProxyHostName?
                if (proxyHostName == null || !proxyHostName.equals(directory.getProxyHostName())) {
                    // Do we have any directories to process?
                    _processStorageDirectoriesOnProxyHosts(directories);
                    directories.clear();
                    // Let's add the current one and start with the new
                    // ProxyHostName!
                    proxyHostName = directory.getProxyHostName();
                }
                // Add the new directory to the list for the current
                // ProxyHost!
                directories.add(directory);
            }
            // Do we have directories left to process?
            _processStorageDirectoriesOnProxyHosts(directories);
        } catch (final Throwable t) {
            _log.warn("purgeProxyHosts", t);
        }
    }

    /**
     * _process storage directories on proxy hosts.
     *
     * @param directories
     *            the directories
     */
    private void _processStorageDirectoriesOnProxyHosts(final List<ExistingStorageDirectory> directories) {
        // Do we have anything to process?
        if (!directories.isEmpty()) {
            final var base = getECpdsBase();
            final var proxyHostName = directories.get(0).getProxyHostName();
            try {
                final var proxyHost = base.getHost(proxyHostName);
                TransferScheduler.purge(base.getTransferServers(proxyHost.getTransferGroupName()), proxyHost,
                        directories);
            } catch (final Throwable t) {
                _log.warn("Purging ProxyHost {}", proxyHostName, t);
            }
        }
    }

    /**
     * Go through every DataMover and check for the DataFiles which should be deleted.
     */
    private void _purgeDataMovers() {
        _log.debug("Starting DataMovers purge");
        try (var it = getECpdsBase().getExistingStorageDirectories()) {
            final List<ExistingStorageDirectory> directories = new ArrayList<>();
            String transferGroupName = null;
            while (it.hasNext()) {
                final var directory = it.next();
                // Do we process a new TransferGroupName?
                if (transferGroupName == null || !transferGroupName.equals(directory.getTransferGroupName())) {
                    // Do we have any directories to process?
                    _processStorageDirectoriesOnDataMovers(directories);
                    directories.clear();
                    // Let's add the current one and start with the new
                    // TransferGroupName!
                    transferGroupName = directory.getTransferGroupName();
                }
                // Add the new directory to the list for the current
                // TransferGroup!
                directories.add(directory);
            }
            // Do we have directories left to process?
            _processStorageDirectoriesOnDataMovers(directories);
        } catch (final Throwable t) {
            _log.warn("purgeDataMovers", t);
        }
    }

    /**
     * Publish MQTT message to all transfers servers from all transfer groups.
     *
     * @param topic
     *            the topic
     * @param qos
     *            the qos
     * @param expiryInterval
     *            the expiry interval
     * @param contentType
     *            the content type
     * @param clientId
     *            the client id
     * @param payload
     *            the payload
     * @param retain
     *            the retain
     */
    public void publishToMQTTBroker(final String topic, final int qos, final long expiryInterval,
            final String contentType, final String clientId, final String payload, final boolean retain) {
        // Publish to all transfer servers from all transfer groups!
        for (final TransferServer server : getECpdsBase().getTransferServerArray()) {
            final var moverName = server.getName();
            final var mover = getDataMoverInterface(moverName);
            if (mover != null) {
                try {
                    _log.debug(
                            "Publish MQTT message on DataMover {} with topic {} (qos={},expiryInterval={},contentType={},clientId={},retain={})",
                            moverName, topic, qos, expiryInterval, contentType, clientId, retain);
                    mover.publishToMQTTBroker(topic, qos, expiryInterval, contentType, clientId, payload, retain);
                } catch (final Throwable t) {
                    _log.warn("Publishing MQTT message on DataMover {}", moverName, t);
                }
            }
        }
    }

    /**
     * Remove MQTT retain message from all transfers servers from all transfer groups.
     *
     * @param topic
     *            the topic
     */
    public void removeFromMQTTBroker(final String topic) {
        // Remove from all transfer servers from all transfer groups!
        for (final TransferServer server : getECpdsBase().getTransferServerArray()) {
            final var moverName = server.getName();
            final var mover = getDataMoverInterface(moverName);
            if (mover != null) {
                try {
                    _log.debug("Remove MQTT retain message on DataMover {} with topic {}", moverName, topic);
                    mover.removeFromMQTTBroker(topic);
                } catch (final Throwable t) {
                    _log.warn("Removing MQTT retain message on DataMover {}", moverName, t);
                }
            }
        }
    }

    /**
     * Get the number of clients connected to all the MQTT brokers.
     *
     * @return the MQTT clients count
     */
    public int getMQTTClientsCount() {
        // Remove from all transfer servers from all transfer groups!
        var count = 0;
        for (final TransferServer server : getECpdsBase().getTransferServerArray()) {
            final var moverName = server.getName();
            final var mover = getDataMoverInterface(moverName);
            if (mover != null) {
                try {
                    final var countFromMover = mover.getMQTTClientsCount();
                    if (countFromMover > 0) {
                        count += countFromMover;
                    }
                } catch (final Throwable t) {
                    _log.warn("Getting MQTT clients count on DataMover {}", moverName, t);
                }
            }
        }
        return count;
    }

    /**
     * _process storage directories on data movers.
     *
     * @param directories
     *            the directories
     */
    private void _processStorageDirectoriesOnDataMovers(final List<ExistingStorageDirectory> directories) {
        // Do we have anything to process?
        if (!directories.isEmpty()) {
            final var base = getECpdsBase();
            final var transferGroupName = directories.get(0).getTransferGroupName();
            try {
                // For this group go through all its data movers!
                for (final TransferServer server : base.getTransferServers(transferGroupName)) {
                    final var moverName = server.getName();
                    final var mover = getDataMoverInterface(moverName);
                    if (mover != null) {
                        try {
                            // Purge on the data mover!
                            _log.debug("Starting purge activity on DataMover {} with {} directories to process",
                                    moverName, directories.size());
                            mover.purge(directories);
                        } catch (final Throwable t) {
                            _log.warn("Starting purge on DataMover {}", moverName, t);
                        }
                    }
                }
            } catch (final Throwable t) {
                _log.warn("Purging TransferGroup {}", transferGroupName, t);
            }
        }
    }

    /**
     * Delete data file.
     *
     * @param file
     *            the file
     *
     * @return true, if successful
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public boolean deleteDataFile(final DataFile file) throws DataBaseException {
        // For the EcpdsPlugin!
        return TransferScheduler.purge(_getTransferServers(file), file).complete;
    }

    /**
     * Purge data file.
     *
     * @param file
     *            the file
     * @param byAndFrom
     *            the by and from
     *
     * @return the purge result
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public PurgeResult purgeDataFile(final DataFile file, final String byAndFrom) throws DataBaseException {
        // Let's do the purge!
        final var start = System.currentTimeMillis();
        final var result = TransferScheduler.purge(_getTransferServers(file), file);
        if (result.complete) {
            _log.debug("DataFile-{} successfully deleted", file.getId());
            final var base = getECpdsBase();
            // Get all the DataTransfers related to this DataFile!
            final var relatedTransfers = base.getDataTransfersByDataFileId(dataCache, file.getId(), true);
            // Add a new history for every DataTransfer related to
            // this DataFile to inform about the purge!
            final var duration = Format.formatDuration(System.currentTimeMillis() - start);
            for (final DataTransfer transfer : relatedTransfers) {
                if (isNotEmpty(byAndFrom)) {
                    transfer.setComment("Purge completed in " + duration
                            + (!result.transferServers.isEmpty() ? " on " + Format.toList(result.transferServers) : "")
                            + " " + byAndFrom);
                    addTransferHistory(transfer);
                }
                // If this DataTransfer was transmitted to a ProxyHost then
                // we have to delete the DataFile on the ProxyHost as well!
                final var proxyHost = transfer.getProxyHost();
                if (proxyHost != null && proxyHost.getActive()) {
                    try {
                        final var purge = TransferScheduler
                                .purge(getECpdsBase().getTransferServers(file.getTransferGroupName()), proxyHost, file);
                        _log.debug("Deletion of DataFile-{} on ProxyHost-{}:{}", file.getId(), proxyHost.getName(),
                                purge.complete);
                    } catch (final Throwable t) {
                        _log.warn("Deletion of DataFile-{} on ProxyHost-{}: false", file.getId(), proxyHost.getName(),
                                t);
                    }
                }
            }
            // Now let's tag the DataFile as deleted!
            base.purgeDataFile(file.getId());
            // Make sure this file is not in the cache anymore as it was deleted from the
            // database!
            base.clearCache(DataFile.class, List.of(file.getId()));
        }
        return result;
    }

    /**
     * The Class FilterEfficiency.
     */
    private static final class FilterEfficiency {

        /** The destination name. */
        String destinationName = null;

        /** The pattern name. */
        String pattern = null;

        /** The date. */
        long date = -1;

        /** The count. */
        int totalNumberOfFiles = 0;

        /** The filter count. */
        int totalNumberOfProcessedFiles = 0;

        /** The size. */
        long totalSize = 0;

        /** The filter size. */
        long totalProcessedSize = 0;

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return destinationName + "|" + totalNumberOfFiles + "|" + totalSize + "|" + totalNumberOfProcessedFiles
                    + "|" + totalProcessedSize + "|"
                    + Format.formatPercentage(totalNumberOfFiles, totalNumberOfProcessedFiles) + "|"
                    + Format.formatPercentage(totalSize, totalSize - totalProcessedSize);
        }

        /**
         * Get the content specifically formated for the email.
         *
         * @return the email content
         */
        String getEmailContent() {
            return "Destination Name: " + destinationName + "\nDate: " + Format.formatTime(date) + "\nFile Pattern: "
                    + (isNotEmpty(pattern) ? pattern : "(.*)") + "\nTotal Number of Eligible Files: "
                    + totalNumberOfFiles + "\nTotal Size of Eligible Files: " + Format.formatSize(totalSize)
                    + "\nNumber of Eligible Files Processed: " + totalNumberOfProcessedFiles
                    + "\nTotal Size After Processing: " + Format.formatSize(totalProcessedSize) + "\n"
                    + Format.formatPercentage(totalNumberOfFiles, totalNumberOfProcessedFiles)
                    + " of Eligible Files Processed\nData Reduced by "
                    + Format.formatPercentage(totalSize, totalSize - totalProcessedSize);
        }
    }

    /**
     * Compute the filter efficiency. The result is sent to the specified email address.
     *
     * @param destinationName
     *            the destination name
     * @param email
     *            the email address
     * @param filter
     *            the filter
     * @param date
     *            the date
     * @param includeStdby
     *            specify if should include Stdby files or not?
     * @param pattern
     *            specify if should include Stdby files or not?
     *
     * @return the filter efficiency
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public String computeFilterEfficiency(final String destinationName, final String email, final String filter,
            final long date, final boolean includeStdby, final String pattern) throws DataBaseException {
        // Just make sure the Destination exists!
        getECpdsBase().getDestination(destinationName);
        final ConfigurableRunnable thread = new ConfigurableRunnable() {
            @Override
            public void configurableRun() {
                final var to = date + Timer.ONE_DAY;
                final var efficiency = new FilterEfficiency();
                efficiency.destinationName = destinationName;
                efficiency.pattern = pattern;
                efficiency.date = date;
                try (var it = getECpdsBase().getDataTransfersByDestinationAndTargetOnDateIterator(destinationName, null,
                        new Date(date), new Date(to))) {
                    _log.info("Processing Destination {} from {} to {}", () -> destinationName,
                            () -> Format.formatTime(date), () -> Format.formatTime(to));
                    var count = 0;
                    while (it.hasNext()) {
                        final var dataTransfer = it.next();
                        if (isNotEmpty(pattern)) {
                            final var target = dataTransfer.getTarget();
                            boolean selected;
                            try {
                                selected = target.matches(pattern);
                            } catch (final PatternSyntaxException e) {
                                _log.debug("Pattern matching {} -> {}", target, pattern, e);
                                selected = false;
                            }
                            if (!selected) {
                                _log.debug("NOT Processing DataTransfer-{}", dataTransfer.getId());
                                continue;
                            }
                        }
                        _log.debug("Processing DataTransfer-{} (count={})", dataTransfer.getId(), ++count);
                        var dataFile = dataTransfer.getDataFile();
                        if (!dataFile.getDownloaded() || dataFile.getDeleted()
                                || !includeStdby && StatusFactory.HOLD.equals(dataTransfer.getStatusCode())) {
                            continue;
                        }
                        final var currentFilter = dataFile.getFilterName();
                        if (!filter.equalsIgnoreCase(currentFilter) || dataFile.getFilterSize() == -1) {
                            dataFile.setFilterName(filter);
                            dataFile = TransferScheduler.filter(_getTransferServers(dataFile), dataFile, true).dataFile;
                        }
                        efficiency.totalNumberOfFiles++;
                        efficiency.totalSize += dataFile.getSize();
                        if (dataFile.getFilterSize() == -1) {
                            efficiency.totalProcessedSize += dataFile.getSize();
                            _log.warn("Could not get filter size for DataFile {} in Destination {}", dataFile.getId(),
                                    destinationName);
                        } else if (dataFile.getSize() <= dataFile.getFilterSize()) {
                            efficiency.totalProcessedSize += dataFile.getSize();
                            if (_log.isWarnEnabled()) {
                                _log.warn("Original DataFile {} smaller than Filtered DataFile ({}<={})",
                                        dataFile.getId(), Format.formatSize(dataFile.getSize()),
                                        Format.formatSize(dataFile.getFilterSize()));
                            }
                        } else {
                            if (_log.isInfoEnabled()) {
                                _log.info("DataFile {} larger than Filtered DataFile ({}>{})", dataFile.getId(),
                                        Format.formatSize(dataFile.getSize()),
                                        Format.formatSize(dataFile.getFilterSize()));
                            }
                            efficiency.totalProcessedSize += dataFile.getFilterSize();
                            efficiency.totalNumberOfProcessedFiles++;
                        }
                    }
                } catch (final Throwable t) {
                    // If an error occurred then we send an email to the
                    // specified address and we stop the computation!
                    sendECpdsMessage(email,
                            "Error occurred while computing efficiency of " + filter + " on " + destinationName,
                            Format.getMessage(t));
                    return;
                }
                // Record the result in the logs!
                _log.info(() -> efficiency.toString());
                // Send the outcome at the specified email address!
                sendECpdsMessage(email, "Efficiency of " + filter + " on " + destinationName,
                        efficiency.getEmailContent());
            }
        };
        thread.execute();
        // Message for the user!
        return "Task started on " + getRoot() + " to compute efficiency of " + filter + " on " + destinationName
                + ". Once completed, the outcome will be sent to " + email + ".";
    }

    /**
     * Gets the mover report.
     *
     * @param proxyHost
     *            the proxy host
     *
     * @return the mover report
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getMoverReport(final Host proxyHost) throws DataBaseException, IOException {
        if (!HostOption.PROXY.equals(proxyHost.getType())) {
            throw new IOException(
                    "Report only available through a ProxyHost: " + proxyHost.getName() + " (not a ProxyHost)");
        }
        return TransferScheduler.getMoverReport(proxyHost);
    }

    /**
     * Gets the host report.
     *
     * @param proxyHost
     *            the proxy host
     * @param host
     *            the host
     *
     * @return the host report
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getHostReport(final Host proxyHost, final Host host) throws DataBaseException, IOException {
        if (!HostOption.PROXY.equals(proxyHost.getType())) {
            throw new IOException(
                    "Report only available through a ProxyHost: " + proxyHost.getName() + " (not a ProxyHost)");
        }
        final var hostName = host.getHost();
        if (hostName == null || hostName.isEmpty() || hostName.indexOf("$") != -1) {
            throw new IOException("Report not available for: " + hostName + " (name only resolved at runtime)");
        }
        return TransferScheduler.getHostReport(proxyHost, host);
    }

    /**
     * Clean the Data Window of the selected Host.
     *
     * @param host
     *            the host
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void cleanDataWindow(final Host host) throws DataBaseException, IOException {
        HostCleaning.cleanHost(getECpdsBase(), host);
    }

    /**
     * Reset the host stats of the selected Host.
     *
     * @param host
     *            the host
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public void resetHostStats(final Host host) throws DataBaseException {
        final var hostId = host.getName();
        try (final var mutex = hostStatsMutexProvider.getMutex(hostId)) {
            synchronized (mutex.lock()) {
                try {
                    getDataBase().update(new HostStats(host.getHostStatsId()));
                } catch (final Throwable e) {
                    _log.warn("Resetting HostStats for Host-{}", hostId, e);
                }
            }
        }
    }

    /**
     * Get a report for the specified Host.
     *
     * @param host
     *            the host
     *
     * @return the report
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getReport(final Host host) throws DataBaseException, IOException {
        final var hostName = host.getHost();
        if (hostName == null || hostName.isEmpty() || hostName.indexOf("$") != -1) {
            throw new IOException("Report not available for: " + hostName + " (name only resolved at runtime)");
        }
        Throwable throwable = null;
        for (final TransferServer current : transferServerManagement.getActiveTransferServers("MasterServer", null,
                host.getTransferGroup(), null)) {
            final var moverName = current.getName();
            final MoverInterface mover;
            if ((mover = getDataMoverInterface(moverName)) == null) {
                _log.warn("DataMover {} NOT available for report", moverName);
                continue;
            }
            try {
                _log.debug("Getting report for {} on {}", host.getName(), moverName);
                return mover.getReport(host);
            } catch (final Throwable t) {
                throwable = t;
                continue;
            }
        }
        throw new IOException(throwable != null ? "Failed on all " + host.getTransferGroupName() + " DataMover(s)"
                : "No " + host.getTransferGroupName() + " DataMover available", throwable);
    }

    /**
     * Get the output for the specified Host. This is used by the monitoring interface.
     *
     * @param host
     *            the host
     *
     * @return the output
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public RemoteInputStreamImp getOutput(final Host host) throws IOException {
        final var path = Paths.get(_getHostOutputFile(host, false).getAbsolutePath());
        final var file = path.toFile();
        if (!file.exists() || !file.canRead() || (file.length() <= 0)) {
            // There is no such file yet!
            final var out = new ByteArrayOutputStream();
            final var gzip = new GZIPOutputStream(out, Deflater.NO_COMPRESSION);
            gzip.write("[n/a]".getBytes());
            gzip.close();
            return new RemoteInputStreamImp(new ByteArrayInputStream(out.toByteArray()));
        }
        // The file exits, is readable and is not empty so let's send its content!
        if (path.toFile().getName().endsWith(".gz")) {
            // This is a compressed file, so nothing to do
            return new RemoteInputStreamImp(Files.newInputStream(path));
        } else {
            // We have to compress the file content
            final var os = new ByteArrayOutputStream();
            try (final var gos = new GZIPOutputStream(os); final var in = Files.newInputStream(path)) {
                in.transferTo(gos);
            }
            return new RemoteInputStreamImp(new ByteArrayInputStream(os.toByteArray()));
        }
    }

    /**
     * Get a report for the specified TransferServer.
     *
     * @param server
     *            the server
     *
     * @return the report
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getReport(final TransferServer server) throws IOException {
        final var moverName = server.getName();
        final MoverInterface mover;
        if ((mover = getDataMoverInterface(moverName)) != null) {
            _log.debug("Getting report for {}", moverName);
            return mover.getReport();
        }
        throw new IOException("DataMover " + moverName + " not available");
    }

    /**
     * Update incoming connection ids.
     *
     * @param serverName
     *            the server name
     * @param incomingConnections
     *            the incoming connections
     */
    @Override
    public void updateIncomingConnectionIds(final String serverName,
            final List<IncomingConnection> incomingConnections) {
        incomingConnectionIds.put(serverName, incomingConnections);
    }

    /**
     * Get all incoming connections from all data movers per user.
     *
     * @return the incoming connections per user
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public Map<String, ArrayList<IncomingConnection>> getIncomingConnections() throws DataBaseException {
        final var result = new HashMap<String, ArrayList<IncomingConnection>>();
        for (final TransferGroup group : getECpdsBase().getTransferGroupArray()) {
            for (final TransferServer server : getECpdsBase().getTransferServers(group.getName())) {
                final var serverName = server.getName();
                // Is it still connected?
                if (getDataMoverInterface(serverName) != null) {
                    for (final IncomingConnection incomingConnection : incomingConnectionIds.get(serverName)) {
                        final var login = incomingConnection.getLogin();
                        var current = result.get(login);
                        if (current == null) {
                            result.put(login, current = new ArrayList<>());
                        }
                        current.add(incomingConnection);
                    }
                } else {
                    // Just to make sure we don't keep some old data!
                    incomingConnectionIds.remove(serverName);
                }
            }
        }
        return result;
    }

    /**
     * Formats a 2D long matrix representing used and total space per volume into a human-readable string.
     * <p>
     * The matrix is expected to have exactly two rows:
     * <ul>
     * <li>Row 0: used space per volume (in bytes)</li>
     * <li>Row 1: total/max space per volume (in bytes)</li>
     * </ul>
     * Each value is converted into an approximate size string (e.g., "432.7GB").
     *
     * @param name
     *            the name of the entity (e.g., server or group) being formatted
     * @param matrix
     *            a 2D long array with [usedPerVolume[], maxCapacityPerVolume[]]
     *
     * @return a human-readable string representing the used and max capacity per volume
     */
    private static String formatLongMatrix(final String name, final long[][] matrix) {
        final var sb = new StringBuilder();
        sb.append(name).append(" [\n");
        for (var i = 0; i < matrix.length; i++) {
            sb.append("  ").append(i == 0 ? "used" : " max").append(" [");
            var rowValues = Arrays.stream(matrix[i]).mapToObj(v -> ByteSize.of(v).toApproximateSize())
                    .collect(Collectors.joining(", "));
            sb.append(rowValues).append("]");
            if (i < matrix.length - 1)
                sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    /**
     * Retrieves information about the data volumes for all transfer servers.
     * <p>
     * Iterates over all transfer groups and their associated transfer servers, computing the used and total capacity
     * for each volume. The results are formatted using {@link #formatLongMatrix(String, long[][])} and concatenated
     * into a single string. Only servers that are currently connected are included.
     *
     * @return a human-readable string containing the volume usage information for all active transfer servers
     *
     * @throws RemoteException
     *             if there is an error communicating with any remote transfer server
     */
    private String getDataVolumeInformations() throws RemoteException {
        final var result = new StringBuilder();
        for (final TransferGroup group : getECpdsBase().getTransferGroupArray()) {
            for (final TransferServer server : getECpdsBase().getTransferServers(group.getName())) {
                final var serverName = server.getName();
                final var mover = getDataMoverInterface(serverName);
                // Is it still connected?
                if (mover != null)
                    result.append(formatLongMatrix(serverName,
                            mover.computeVolumeUsage(server.getTransferGroup().getVolumeCount())));
            }
        }
        return result.toString();
    }

    /**
     * Computes the aggregated volume usage across all data movers belonging to the specified {@link TransferGroup}.
     * Each data mover reports usage for a fixed number of metrics per volume (currently two), and the results are
     * summed volume-by-volume.
     * <p>
     * If a mover is unreachable or returns invalid data (null, incorrect dimensions, or incomplete metric rows), its
     * contribution is ignored and a warning is logged.
     *
     * @param group
     *            the transfer group for which usage statistics should be aggregated. Must not be null and must have a
     *            defined volume count.
     *
     * @return a {@code long[][]} array containing aggregated usage metrics across all connected and valid data movers,
     *         with one row per volume and two metrics per volume. Never {@code null}.
     *
     * @throws RemoteException
     *             if the underlying remote communication with movers fails.
     */
    public long[][] computeVolumeUsage(final TransferGroup group) throws RemoteException {
        final var volumeCount = group.getVolumeCount();
        final var metricCount = 2; // fixed
        final var volumeUsageTotal = new long[volumeCount][metricCount];
        var initialized = false;
        for (final TransferServer server : getECpdsBase().getTransferServers(group.getName())) {
            final var serverName = server.getName();
            final var mover = getDataMoverInterface(serverName);
            if (mover == null) {
                continue;
            }
            final var volumeUsage = mover.computeVolumeUsage(volumeCount);
            // validate dimensions
            if (volumeUsage == null || volumeUsage.length != volumeCount
                    || Arrays.stream(volumeUsage).anyMatch(row -> row == null || row.length != metricCount)) {
                _log.warn("Invalid volumeUsage returned by mover {}", serverName);
                continue;
            }
            if (!initialized) {
                for (var i = 0; i < volumeCount; i++) {
                    System.arraycopy(volumeUsage[i], 0, volumeUsageTotal[i], 0, metricCount);
                }
                initialized = true;
                continue;
            }
            for (var i = 0; i < volumeCount; i++) {
                final var src = volumeUsage[i];
                final var dest = volumeUsageTotal[i];
                dest[0] += src[0];
                dest[1] += src[1];
            }
        }
        return volumeUsageTotal;
    }

    /**
     * Get all incoming connections from all data movers.
     *
     * @return the incoming connection ids
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public String getIncomingConnectionIds() {
        final var result = new StringBuilder();
        for (final TransferGroup group : getECpdsBase().getTransferGroupArray()) {
            for (final TransferServer server : getECpdsBase().getTransferServers(group.getName())) {
                final var serverName = server.getName();
                // Is it still connected?
                if (getDataMoverInterface(serverName) != null) {
                    for (final IncomingConnection incomingConnection : incomingConnectionIds.get(serverName)) {
                        result.append(!result.isEmpty() ? "\n" : "").append(incomingConnection.getId()).append("=")
                                .append(incomingConnection.getProtocol()).append(":")
                                .append(incomingConnection.getLogin()).append("@")
                                .append(incomingConnection.getRemoteIpAddress());
                    }
                } else {
                    // Just to make sure we don't keep some old data!
                    incomingConnectionIds.remove(serverName);
                }
            }
        }
        return result.toString();
    }

    /**
     * Get the number of connections for the specified user-id.
     *
     * @param uid
     *            the uid
     *
     * @return the incoming connection count for
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private int _getIncomingConnectionCountFor(final String uid) throws DataBaseException {
        var count = 0;
        for (final TransferGroup group : getECpdsBase().getTransferGroupArray()) {
            for (final TransferServer server : getECpdsBase().getTransferServers(group.getName())) {
                final var serverName = server.getName();
                // Is it still connected?
                if (getDataMoverInterface(serverName) != null) {
                    final var incomingConnections = incomingConnectionIds.get(serverName);
                    if (isNotEmpty(incomingConnections)) {
                        for (final IncomingConnection incomingConnection : incomingConnections) {
                            if (uid.equals(incomingConnection.getLogin())) {
                                count++;
                            }
                        }
                    }
                } else {
                    // Just to make sure we don't keep some old data!
                    incomingConnectionIds.remove(serverName);
                }
            }
        }
        return count;
    }

    /**
     * Close all incoming connections from all data movers.
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public void closeAllIncomingConnections() throws DataBaseException {
        var numberProcessed = 0;
        for (final TransferGroup group : getECpdsBase().getTransferGroupArray()) {
            for (final TransferServer server : getECpdsBase().getTransferServers(group.getName())) {
                final var serverName = server.getName();
                final var mover = getDataMoverInterface(serverName);
                if (mover != null) {
                    _log.debug("Closing all incoming connections for {}", serverName);
                    try {
                        mover.closeAllIncomingConnections();
                        numberProcessed++;
                    } catch (final Throwable t) {
                        _log.warn("Closing all incoming connections from {}", serverName, t);
                    }
                } else {
                    _log.warn("Data Mover {} not found/running?", serverName);
                }
            }
        }
        _log.debug("Closing every incoming connection for {} Data Mover(s)", numberProcessed);
    }

    /**
     * Close an incoming connection from its identifier.
     *
     * @param id
     *            the id in the format dataMoverName_id
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean closeIncomingConnection(final String id) throws IOException {
        final var index = id.indexOf("_");
        if (index == -1) {
            _log.warn("Bad format for connection identifier: {}", id);
            return false;
        }
        final var moverName = id.substring(0, index);
        final var mover = getDataMoverInterface(moverName);
        if (mover != null) {
            return mover.closeIncomingConnection(id);
        }
        _log.warn("Mover not found/connected: {}", moverName);
        return false;
    }

    /**
     * Close all incoming connections on a data mover.
     *
     * @param transferServerName
     *            the transfer server name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void closeAllIncomingConnections(final String transferServerName) throws IOException {
        final var mover = getDataMoverInterface(transferServerName);
        if (mover != null) {
            mover.closeAllIncomingConnections();
        }
    }

    /**
     * Update transfer status.
     *
     * @param transfer
     *            the transfer
     * @param code
     *            the code
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     */
    public boolean updateTransferStatus(final DataTransfer transfer, final String code) throws MasterException {
        return updateTransferStatus(getDataTransfer(transfer), code, false, null, null, true, false, true);
    }

    /**
     * Update transfer status.
     *
     * @param id
     *            the id
     * @param code
     *            the code
     * @param commit
     *            the commit
     * @param username
     *            the username
     * @param byAndFrom
     *            the comment
     * @param synchronous
     *            the synchronous
     * @param reset
     *            the reset
     * @param addHistory
     *            the add history
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     */
    public boolean updateTransferStatus(final long id, final String code, final boolean commit, final String username,
            final String byAndFrom, final boolean synchronous, final boolean reset, final boolean addHistory)
            throws MasterException {
        final var transfer = getDataTransfer(id);
        if (transfer == null) {
            throw new MasterException("DataTransfer not found: " + id);
        }
        return updateTransferStatus(transfer, code, commit, username, byAndFrom, synchronous, reset, addHistory);
    }

    /**
     * Update transfer status.
     *
     * @param transfer
     *            the transfer
     * @param code
     *            the code
     * @param commit
     *            the commit
     * @param username
     *            the username
     * @param byAndFrom
     *            the comment
     * @param synchronous
     *            the synchronous
     * @param reset
     *            the reset
     * @param addHistory
     *            the add history
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     */
    public boolean updateTransferStatus(final DataTransfer transfer, String code, final boolean commit,
            final String username, final String byAndFrom, final boolean synchronous, final boolean reset,
            final boolean addHistory) throws MasterException {
        final var file = transfer.getDataFile();
        final var expiry = transfer.getExpiryTime();
        if ((transfer.getDeleted() || file != null && file.getDeleted()
                || expiry != null && expiry.before(new Date(System.currentTimeMillis())))
                && !StatusFactory.STOP.equals(code)) {
            // Only allow if it is to be stopped!
            return false;
        }
        final var orig = transfer.getStatusCode();
        code = code.toUpperCase();
        if (orig.equals(code)
                || (StatusFactory.EXEC.equals(orig) || StatusFactory.RETR.equals(orig)
                        || StatusFactory.SCHE.equals(orig) || StatusFactory.FETC.equals(orig))
                        && StatusFactory.WAIT.equals(code)
                || StatusFactory.DONE.equals(orig) && StatusFactory.STOP.equals(code)) {
            return false;
        }
        if (!StatusFactory.HOLD.equals(code) && !StatusFactory.INIT.equals(code) && !StatusFactory.SCHE.equals(code)
                && !StatusFactory.FETC.equals(code) && !StatusFactory.STOP.equals(code)
                && !StatusFactory.WAIT.equals(code)) {
            _log.warn("DataTransfer status update refused (uid={},id={},orig={},code={})", username, transfer.getId(),
                    orig, code);
            return false;
        }
        if (commit) {
            final boolean stopped;
            code = StatusFactory.WAIT.equals(code) ? StatusFactory.RETR : code;
            if (StatusFactory.EXEC.equals(orig) && getDataTransferFromCache(transfer.getId()) != null) {
                final var ticket = new PendingUpdateTicket(username, code, byAndFrom);
                theTicketRepository.add(ticket, transfer.getId());
                if (!(stopped = _closeDataTransfer(transfer))) {
                    _log.warn("DataTransfer {} could not be stopped on DataMover", transfer.getId());
                }
                if (addHistory && !StatusFactory.STOP.equals(code)) {
                    addTransferHistory(transfer, StatusFactory.STOP, byAndFrom);
                }
                for (var i = 0; synchronous && i < 5; i++) {
                    try {
                        if (ticket.isCompleted()) {
                            break;
                        }
                        Thread.sleep(Timer.ONE_SECOND);
                    } catch (final InterruptedException e) {
                    }
                }
                if (synchronous && !ticket.isCompleted()) {
                    _log.warn("Ticket {} not completed yet ({})", ticket.getId(), code);
                }
                if (!ticket.isCompleted() && !stopped) {
                    // Let's force an update of the DataTransfer because we
                    // couldn't stop it on the DataMover!
                    _log.warn("Force STOP of DataTransfer");
                    checkPendingTicket(transfer, false);
                    theTransferScheduler.notifyCompletion(transfer);
                    if (addHistory)
                        addTransferHistory(transfer, StatusFactory.STOP, byAndFrom);
                }
            } else {
                transfer.setStatusCode(code);
                transfer.setUserStatus(username);
                transfer.setComment(byAndFrom);
                transfer.setDuration(0);
                transfer.setSent(0);
                transfer.setPutTime(null);
                stopped = false;
                if (StatusFactory.RETR.equals(code) && reset) {
                    transfer.setRequeueCount(0);
                    transfer.setStartCount(0);
                }
                if (addHistory && !StatusFactory.STOP.equals(code)) {
                    addTransferHistory(transfer);
                }
                try {
                    getDataBase().update(transfer);
                } catch (final DataBaseException e) {
                    _log.warn("Updating DataTransfer {}", transfer.getId(), e);
                    throw new MasterException(e.getMessage());
                }
                if (StatusFactory.RETR.equals(code) && theTransferScheduler != null) {
                    theTransferScheduler.notifyRequeue(transfer);
                    _log.debug("Requeue notification sent to TransferScheduler");
                }
            }
            _log.info("DataTransfer status updated (uid={},id={},orig={},code={}){}", username, transfer.getId(), orig,
                    code, stopped ? " (stopped)" : "");
        }
        return true;
    }

    /**
     * _update transfer status.
     *
     * @param transfer
     *            the transfer
     * @param code
     *            the code
     * @param comment
     *            the comment
     * @param dataBaseUpdate
     *            the data base update
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private void _updateTransferStatus(final DataTransfer transfer, final String code, final String comment,
            final boolean dataBaseUpdate) throws DataBaseException {
        transfer.setStatusCode(code);
        transfer.setComment(comment);
        transfer.setDuration(0);
        transfer.setSent(0);
        transfer.setPutTime(null);
        if (!StatusFactory.STOP.equals(code) && !StatusFactory.FAIL.equals(code)) {
            addTransferHistory(transfer);
        }
        if (dataBaseUpdate) {
            try {
                getDataBase().update(transfer);
            } catch (final DataBaseException e) {
                _log.warn("Updating DataTransfer {}", transfer.getId(), e);
                throw e;
            }
        }
    }

    /**
     * Reload destination.
     *
     * @param transfer
     *            the transfer
     *
     * @return true, if successful
     *
     * @throws MasterException
     *             the master exception
     */
    public boolean reloadDestination(final DataTransfer transfer) throws MasterException {
        var result = true;
        if (transfer.getRetryTime().getTime() > new Timestamp(System.currentTimeMillis() + 10 * Timer.ONE_MINUTE)
                .getTime()) {
            return result;
        }
        if (theTransferScheduler == null) {
            throw new MasterException("Scheduler not started");
        }
        try {
            theTransferScheduler.getDestinationThread(transfer.getDestinationName()).reset();
        } catch (final MasterException e) {
            result = false;
        }
        return result;
    }

    /**
     * Get the list of active TransferServers.
     *
     * @param caller
     *            the caller
     * @param original
     *            the original
     * @param group
     *            the group
     * @param fileSystem
     *            the file system
     *
     * @return the active transfer servers
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public List<TransferServer> getActiveTransferServers(final String caller, final TransferServer original,
            final TransferGroup group, final Integer fileSystem) throws DataBaseException {
        return transferServerManagement.getActiveTransferServers(caller, original, group, fileSystem);
    }

    /**
     * Transfer.
     *
     * @param bytes
     *            the bytes
     * @param server
     *            the server
     * @param host
     *            the host
     * @param target
     *            the target
     * @param remotePosn
     *            the remote posn
     * @param size
     *            the size
     *
     * @return the long
     *
     * @throws MasterException
     *             the master exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public long transfer(final byte[] bytes, final TransferServer server, final Host host, final String target,
            final long remotePosn, final long size) throws MasterException, IOException {
        final var proxy = TransferScheduler.put(server, host, target, remotePosn, size);
        MonitoredOutputStream out = null;
        var success = false;
        Socket socket = null;
        try {
            socket = proxy.getDataSocket();
            out = new MonitoredOutputStream(socket.getOutputStream());
            final var plug = new StreamPlugThread(new ByteArrayInputStream(bytes), out);
            plug.configurableRun();
            out.close();
            socket.close();
            plug.close();
            success = true;
        } finally {
            if (!success) {
                StreamPlugThread.closeQuietly(out);
                StreamPlugThread.closeQuietly(socket);
            }
        }
        final var byteSent = out.getByteSent();
        if (_log.isDebugEnabled() && byteSent > 0) {
            _log.debug("Transfer rate: {}", out.getSimplifiedRate());
        }
        TransferScheduler.check(server, proxy.getTicket());
        return byteSent;
    }

    /**
     * Gets the client interface.
     *
     * @param name
     *            the name
     *
     * @return the client interface
     */
    public MoverInterface getDataMoverInterface(final String name) {
        return getClientInterface(name, "DataMover", MoverInterface.class);
    }

    /**
     * _close data transfer.
     *
     * @param transfer
     *            the transfer
     *
     * @return true, if successful
     */
    private boolean _closeDataTransfer(final DataTransfer transfer) {
        var result = false;
        final var serverName = transfer.getTransferServerName();
        _log.debug("Close DataTransfer {} on DataMover {}", transfer.getId(), serverName);
        try {
            final var mover = getDataMoverInterface(serverName);
            if (mover != null) {
                result = mover.close(transfer);
            } else {
                _log.warn("DataTransfer {} not closed (DataMover {} not available)", transfer.getId(), serverName);
            }
        } catch (final Throwable t) {
            _log.warn("Could not close DataTransfer {} on DataMover {}", transfer.getId(), serverName, t);
        }
        return result;
    }

    /**
     * Reset transfer server.
     *
     * @param root
     *            the root
     * @param comment
     *            the comment
     */
    public void resetTransferServer(final String root, final String comment) {
        if (theTransferRepository == null) {
            return;
        }
        _log.debug("Reset {}: {}", root, comment);
        final List<String> affectedDestinations = new ArrayList<>();
        // Find all the affected DataTransfers, stop them and notify the
        // Transfer Scheduler!
        for (final DataTransfer transfer : theTransferRepository.getList()) {
            if ((root.equals(transfer.getMoverName()) || root.equals(transfer.getTransferServerName()))
                    && !StatusFactory.INTR.equals(transfer.getStatusCode())) {
                final var destination = transfer.getDestinationName();
                _log.debug("Reset DataTransfer {} for Destination {} ({})", transfer.getId(), destination, comment);
                transfer.setStatusCode(StatusFactory.INTR);
                transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
                transfer.setComment(comment);
                transfer.setFinishTime(null);
                transfer.setSent(0);
                transfer.setDuration(0);
                transfer.setPutTime(null);
                updateDataTransfer(transfer);
                if (!affectedDestinations.contains(destination)) {
                    affectedDestinations.add(destination);
                }
            }
        }
        // Restart all the affected Destinations to avoid them getting stuck
        // with pending connections to/from the Transfer Servers!
        for (final String destination : affectedDestinations) {
            try {
                theTransferScheduler.restartDestination(null, comment, destination, false, false);
            } catch (final Throwable t) {
                _log.warn("Restarting destination: {}", destination, t);
            }
        }
    }

    /**
     * Reset destination.
     *
     * @param destination
     *            the destination
     * @param comment
     *            the comment
     */
    public void resetDestination(final Destination destination, final String comment) {
        if (theTransferRepository == null) {
            return;
        }
        _log.debug("Reset Destination {}", destination.getName());
        final var name = destination.getName();
        for (final DataTransfer transfer : theTransferRepository.getList()) {
            if (name.equals(transfer.getDestinationName()) && StatusFactory.EXEC.equals(transfer.getStatusCode())) {
                theTransferRepository.removeValue(transfer);
                _closeDataTransfer(transfer);
                transfer.setStatusCode(StatusFactory.INTR);
                transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
                transfer.setComment(comment);
                transfer.setFinishTime(null);
                transfer.setSent(0);
                transfer.setDuration(0);
                transfer.setPutTime(null);
                getDataBase().tryUpdate(transfer);
                addTransferHistory(transfer);
            }
        }
    }

    /**
     * Called when a ProxyHost sends an update to the Master.
     *
     * @param name
     *            the name
     *
     * @return the long
     */
    @Override
    public long proxyHostIsAlive(final String name) {
        if (theProxyHostRepository != null) {
            // Let's add this ProxyHost to the repository. If it is there
            // already it will be simply updated!
            if (!theProxyHostRepository.containsKey(name)) {
                _log.debug("ProxyHost was not registered yet: {}", name);
            }
            theProxyHostRepository.put(new ProxyHost(name));
        }
        // Return the start time to allow the ProxyHost to check if the
        // MasterServer was restarted. If it was the case then it would have to
        // reset all its transmissions!
        return getStartDate().getTime();
    }

    /**
     * Adds the root.
     *
     * @param access
     *            the access
     * @param host
     *            the host
     * @param root
     *            the root
     * @param service
     *            the service
     */
    @Override
    public void addRoot(final ClientInterface access, final String host, final String root, final String service) {
        if ("DataMover".equals(service) || "DataProxy".equals(service)) {
            resetTransferServer(root, service + " " + root + " restarted");
        }
        if ("DataMover".equals(service)) {
            final var base = getDataBase();
            final var server = base.getTransferServerObject(root);
            if (server != null) {
                final var mover = MoverInterface.class.cast(access);
                try {
                    final var address = mover.getECproxyAddressAndPort();
                    _log.debug("Checking ECproxy for TransferServer " + root + ": " + address + " (host=" + host + ")");
                    final var index = address.lastIndexOf(":");
                    final var listen = address.substring(0, index);
                    // Let's try to do the best guess for the IP address used to connect to the
                    // ECproxyPlugin on the DataMover!
                    final var newhost = "0.0.0.0".equals(listen) || "::".equals(listen)
                            ? "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host)
                                    ? root : host
                            : listen;
                    final var newport = address.substring(index + 1);
                    if (!newhost.equals(server.getHost()) || !newport.equals(Integer.toString(server.getPort()))) {
                        _log.debug("Change detected in ECproxy address: " + server.getHost() + ":" + server.getPort()
                                + " -> " + newhost + ":" + newport);
                        if (Cnf.at("Master", "updateECproxyAddressAndPort", false)) {
                            _log.debug("Processing update");
                            base.tryUpdate(server);
                        }
                    }
                } catch (final Throwable t) {
                    _log.warn("Updating TransferServer " + root, t);
                }
            } else {
                _log.warn("TransferServer " + root + " not registered in the database");
            }
        }
        super.addRoot(access, host, root, service);
    }

    /**
     * Removes the expired.
     *
     * @param root
     *            the root
     * @param service
     *            the service
     */
    @Override
    public void removeExpired(final String root, final String service) {
        super.removeExpired(root, service);
        if ("DataMover".equals(service) || "DataProxy".equals(service)) {
            resetTransferServer(root, service + " " + root + " interrupted");
        }
    }

    /**
     * Import EC user.
     *
     * @param uid
     *            the uid
     *
     * @return the EC user
     *
     * @throws EccmdException
     *             the eccmd exception
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public ECUser importECUser(final String uid) throws EccmdException, RemoteException {
        return null;
    }

    /**
     * Handle E cuser update.
     *
     * This method is called when an ECUser change is detected (when the database has just been updated with a new
     * version from NIS).
     *
     * @param ecuser
     *            the ecuser
     */
    @Override
    public void handleECuserUpdate(final ECUser ecuser) {
        final var name = ecuser.getName();
        _log.debug("Update detected for ecuser: " + name);
        var webUser = getDataBase().getWebUserObject(name);
        if (webUser == null) {
            // The Webuser does not exists!
            _log.debug("Creating Webuser: " + name);
            webUser = new WebUser(name);
            webUser.setName(ecuser.getComment());
            webUser.setActive(true);
            try {
                getDataBase().insert(webUser, false);
            } catch (final DataBaseException e) {
                _log.warn("Error inserting Webuser: " + name, e);
            }
        } else {
            // Update the id and name fields only!
            _log.debug("Updating Webuser: " + name);
            webUser.setId(name);
            webUser.setName(ecuser.getComment());
            getDataBase().tryUpdate(webUser);
        }
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(), "The ECpds MasterServer deals with data transfers",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("Connected", "java.lang.Boolean",
                                "Connected: connected to the ECcmdServer.", true, false, false),
                        new MBeanAttributeInfo("sendMailForTransfers", "java.lang.Boolean",
                                "sendMailForTransfers: send mails for each event on a data transfer.", true, true,
                                false),
                        new MBeanAttributeInfo("Trace", "java.lang.Boolean",
                                "Trace: show remote calls from monitoring interface in logs.", true, true, false),
                        new MBeanAttributeInfo("SynchronizedCount", "java.lang.Long",
                                "SynchronizedCount: total number of elements for all instances of Synchronized.", true,
                                false, false) },
                new MBeanOperationInfo[] { new MBeanOperationInfo("computeFilterEfficiency",
                        "computeFilterEfficiency(destination,email,filter,date,includeStdby): check the efficiency of filtering",
                        new MBeanParameterInfo[] {
                                new MBeanParameterInfo("destination", "java.lang.String", "target destination"),
                                new MBeanParameterInfo("email", "java.lang.String", "email to receive the results"),
                                new MBeanParameterInfo("filter", "java.lang.String", "filter to be used"),
                                new MBeanParameterInfo("date", "java.lang.String", "in the form yyyy-MM-dd"),
                                new MBeanParameterInfo("includeStdby", "java.lang.Boolean",
                                        "should we include standby files?"),
                                new MBeanParameterInfo("pattern", "java.lang.String", "pattern for file selection") },
                        "java.lang.String", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("exportDestinationsAndHosts",
                                "exportDestinationsAndHosts(dir,type): export destinations, hosts and association to backup files in dir",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("dir", "java.lang.String",
                                                "dir where to export backup files"),
                                        new MBeanParameterInfo("type", "java.lang.Integer",
                                                "type of Destinations to export (-1 == all)") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("cleanHost",
                                "cleanHost(hostId): remove unused parameters in data window for selected Host",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("hostId", "java.lang.String", "Host identifier") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("cleanHosts",
                                "cleanHost(hostType): remove unused parameters in data window for Host of the selected Type",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("hostType", "java.lang.String",
                                        "Host type (e.g. Dissemination, Acquisition ...)") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("cleanHosts",
                                "cleanHosts(): remove unused parameters in data window for each Host",
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("importDestinationsAndHosts",
                                "importDestinationsAndHosts(dir): import destinations, hosts and association from backup files in dir",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("dir", "java.lang.String",
                                        "dir where to import backup files") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("purgeProxyHosts",
                                "purgeProxyHosts: purge the storage filesystem on every ProxyHost",
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("purgeDataMovers",
                                "purgeDataMovers: purge the storage filesystem on every DataMover",
                                new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("restartAllDestinations",
                                "restartAllDestinations(graceful): restart all the destinations",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("graceful", "java.lang.Boolean", "graceful restart") },
                                "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("updateHostLocations",
                                "updateHostLocations(): update the latitude/longitude for the hosts configured for automatic location",
                                new MBeanParameterInfo[0], "java.lang.Integer", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("cleanAccessControl",
                                "cleanAccessControl(): remove the access control for the destinations which have already been removed",
                                new MBeanParameterInfo[0], "java.lang.Integer", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("holdAllDestinations",
                                "holdAllDestinations(graceful): stop all the destinations",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("graceful", "java.lang.Boolean", "graceful restart") },
                                "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("getTargetName", "getTargetName(dataTransferId): get the target name",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("dataTransferId", "java.lang.Long",
                                        "id of the data transfer") },
                                "java.lang.String", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("updateTransferStatus",
                                "updateTransferStatus(transferId,status,commit): update the status of the data transfer",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("transferId", "java.lang.Integer",
                                                "the transfer identifier"),
                                        new MBeanParameterInfo("status", "java.lang.String", "the status code"),
                                        new MBeanParameterInfo("commit", "java.lang.Boolean", "commit the change"),
                                        new MBeanParameterInfo("user", "java.lang.String", "the user name") },
                                "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("mqttPublish",
                                "mqttPublish(topic,qos,expiryInterval,contentType,clientId,payload,retain): publish event to all transfer servers from all transfer groups",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("topic", "java.lang.String", "the topic"),
                                        new MBeanParameterInfo("qos", "java.lang.Integer", "the qos"),
                                        new MBeanParameterInfo("expiryInterval", "java.lang.Long",
                                                "the expiry interval"),
                                        new MBeanParameterInfo("contentType", "java.lang.String", "the content type"),
                                        new MBeanParameterInfo("clientId", "java.lang.String", "the client id"),
                                        new MBeanParameterInfo("payload", "java.lang.String", "the payload"),
                                        new MBeanParameterInfo("retain", "java.lang.Boolean", "the retain flag") },
                                "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("getMqttClientsCount",
                                "getMqttClientsCount(): get total number of mqtt clients across all mqtt brokers",
                                new MBeanParameterInfo[0], "java.lang.Integer", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("mqttRemoveTopic",
                                "mqttRemoveTopic(topic): remove retain message from all mqtt brokers",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("topic", "java.lang.String",
                                        "topic associated to the retain message") },
                                "java.lang.Integer", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("getIncomingConnectionIds",
                                "getIncomingConnectionIds(): get all incoming connections on all data movers",
                                new MBeanParameterInfo[0], "java.lang.String", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("getDataVolumeInformations",
                                "getDataVolumeInformations(): get data volume informations from all data movers",
                                new MBeanParameterInfo[0], "java.lang.String", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("getContacts", "getContacts(): get contact details for each destination",
                                new MBeanParameterInfo[0], "java.lang.String", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("closeAllIncomingConnections",
                                "closeAllIncomingConnections(): close all incoming connections on all data movers",
                                new MBeanParameterInfo[0], "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("closeAllIncomingConnections",
                                "closeAllIncomingConnections(dataMoverName): close all incoming connections for the specified data mover",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("dataMoverName", "java.lang.String",
                                        "data mover to close all the incoming connections") },
                                "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("insertAccessControl",
                                "insertAccessControl(destinationName): recover/create permissions for the specified destination",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("destinationName", "java.lang.String",
                                        "destination name to recover the permissions") },
                                "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("closeIncomingConnection",
                                "closeIncomingConnection(id): close specified incoming connection",
                                new MBeanParameterInfo[] { new MBeanParameterInfo("id", "java.lang.String",
                                        "id to close in the form dataMoverName_identifier as shown in getIncomingConnectionIds") },
                                "java.lang.Boolean", MBeanOperationInfo.ACTION) });
    }

    /**
     * Gets the attribute.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute
     *
     * @throws AttributeNotFoundException
     *             the attribute not found exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("SynchronizedCount".equals(attributeName)) {
                return Synchronized.getSize();
            }
            if ("sendMailForTransfers".equals(attributeName)) {
                return sendMailNotificationsForTransfers;
            }
            if ("Trace".equals(attributeName)) {
                return MonitorCall.getTrace();
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return true, if successful
     *
     * @throws InvalidAttributeValueException
     *             the invalid attribute value exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("sendMailForTransfers".equals(name)) {
            sendMailNotificationsForTransfers = (Boolean) value;
            return true;
        }
        if ("Trace".equals(name)) {
            MonitorCall.setTrace((Boolean) value);
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * Invoke.
     *
     * @param operationName
     *            the operation name
     * @param params
     *            the params
     * @param signature
     *            the signature
     *
     * @return the object
     *
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("computeFilterEfficiency".equals(operationName) && signature.length == 5
                    && "java.lang.String".equals(signature[0]) && "java.lang.String".equals(signature[1])
                    && "java.lang.String".equals(signature[2]) && "java.lang.String".equals(signature[3])
                    && "java.lang.Boolean".equals(signature[4]) && "java.lang.String".equals(signature[5])) {
                return computeFilterEfficiency((String) params[0], (String) params[1], (String) params[2],
                        Format.toTime("yyyy-MM-dd", (String) params[3]), (Boolean) params[4], (String) params[5]);
            }
            if ("cleanHost".equals(operationName) && signature.length == 1 && "java.lang.String".equals(signature[0])) {
                final var base = getECpdsBase();
                final var host = base.getHost((String) params[0]);
                HostCleaning.cleanHost(base, host);
                return true;
            }
            if ("cleanHosts".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                final var base = getECpdsBase();
                return HostCleaning.cleanHosts(base, (String) params[0]) + " Host(s) processed";
            }
            if ("cleanHosts".equals(operationName) && signature.length == 0) {
                return HostCleaning.cleanHosts(getECpdsBase()) + " Host(s) processed";
            }
            if ("updateHostLocations".equals(operationName) && signature.length == 0) {
                return updateHostLocations();
            }
            if ("purgeDataMovers".equals(operationName) && signature.length == 0) {
                _purgeDataMovers();
                return true;
            }
            if ("purgeProxyHosts".equals(operationName) && signature.length == 0) {
                _purgeProxyHosts();
                return true;
            }
            if ("cleanAccessControl".equals(operationName) && signature.length == 0) {
                return AccessControl.cleanAccessControl(getECpdsBase());
            }
            if ("getTargetName".equals(operationName) && signature.length == 1
                    && "java.lang.Long".equals(signature[0])) {
                final var transfer = getDataBase().getDataTransfer((Long) params[0]);
                return TransferManagement.getTargetName(transfer, transfer.getHost().getDir());
            }
            if ("updateTransferStatus".equals(operationName) && signature.length == 4
                    && "java.lang.Integer".equals(signature[0]) && "java.lang.String".equals(signature[1])
                    && "java.lang.Boolean".equals(signature[2]) && "java.lang.String".equals(signature[3])) {
                return updateTransferStatus(((Integer) params[0]).intValue(), (String) params[1], ((Boolean) params[2]),
                        (String) params[3], "From the JMX interface", true, true, true);
            }
            if ("mqttPublish".equals(operationName) && signature.length == 7 && "java.lang.String".equals(signature[0])
                    && "java.lang.Integer".equals(signature[1]) && "java.lang.Long".equals(signature[2])
                    && "java.lang.String".equals(signature[3]) && "java.lang.String".equals(signature[4])
                    && "java.lang.String".equals(signature[5]) && "java.lang.Boolean".equals(signature[6])) {
                publishToMQTTBroker((String) params[0], (Integer) params[1], (Long) params[2], (String) params[3],
                        (String) params[4], (String) params[5], ((Boolean) params[6]));
                return true;
            }
            if ("mqttRemoveTopic".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                removeFromMQTTBroker((String) params[0]);
                return true;
            }
            if ("getMqttClientsCount".equals(operationName) && signature.length == 0) {
                return getMQTTClientsCount();
            }
            if ("exportDestinationsAndHosts".equals(operationName) && signature.length == 2
                    && "java.lang.String".equals(signature[0]) && "java.lang.Integer".equals(signature[1])) {
                _exportDestinationsAndHosts((String) params[0], (Integer) params[1]);
                return true;
            }
            if ("importDestinationsAndHosts".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                _importDestinationsAndHosts((String) params[0]);
                return true;
            }
            if ("restartAllDestinations".equals(operationName) && signature.length == 1
                    && "java.lang.Boolean".equals(signature[0])) {
                getTransferScheduler().restartAllDestinations(null, ((Boolean) params[0]));
                return true;
            }
            if ("holdAllDestinations".equals(operationName) && signature.length == 1
                    && "java.lang.Boolean".equals(signature[0])) {
                getTransferScheduler().holdAllDestinations(null, StatusFactory.STOP, ((Boolean) params[0]));
                return true;
            }
            if ("closeIncomingConnection".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                return closeIncomingConnection((String) params[0]);
            }
            if ("closeAllIncomingConnections".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                closeAllIncomingConnections((String) params[0]);
                return true;
            }
            if ("closeAllIncomingConnections".equals(operationName) && signature.length == 0) {
                closeAllIncomingConnections();
                return true;
            }
            if ("getIncomingConnectionIds".equals(operationName) && signature.length == 0) {
                return getIncomingConnectionIds();
            }
            if ("getDataVolumeInformations".equals(operationName) && signature.length == 0) {
                return getDataVolumeInformations();
            }
            if ("getContacts".equals(operationName) && signature.length == 0) {
                final var result = new StringBuilder();
                final var contacts = management.getContacts(true);
                for (final String destination : contacts.keySet()) {
                    result.append("Destination " + destination + ": " + contacts.get(destination) + "\n");
                }
                return result.isEmpty() ? "No contact found" : result.toString();
            }
            if ("insertAccessControl".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                AccessControl.insertAccessControl(getDataBase(ECpdsBase.class), (String) params[0]);
                return true;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }

    /**
     * Update data transfers.
     *
     * @param transfers
     *            the transfers
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void updateDataTransfers(final DataTransfer[] transfers) throws RemoteException {
        try {
            for (DataTransfer transfer : transfers) {
                final var id = transfer.getId();
                if (transfer.getDestination() == null && getDataTransferFromCache(id) != null) {
                    // This update comes from a ProxyHost and it is being processed
                    // on the Master, so we only take the relevant information (the
                    // full Destination object is not set in the JSON request)!
                    _log.debug("Received update from Proxy Host-{}: {} (found in cache)", transfer.getProxyHostName(),
                            transfer);
                    // We have to create a clone otherwise the TransferRepository
                    // will detect the status change and if the file is DONE then it
                    // will be removed from the repository too soon!
                    final var found = getDataTransfer(id);
                    if (found != null && found.clone() instanceof final DataTransfer local) {
                        try {
                            local.setFinishTime(transfer.getFinishTime());
                            local.setFailedTime(transfer.getFailedTime());
                            local.setPutTime(transfer.getPutTime());
                            local.setSent(transfer.getSent());
                            local.setDuration(transfer.getDuration());
                            local.setDurationOnClose(transfer.getDurationOnClose());
                            local.setComment(transfer.getComment());
                            local.setStatusCode(transfer.getStatusCode());
                            local.setRatio(transfer.getRatio());
                            local.setCompressed(transfer.getCompressed());
                            local.setCompressedOnTheFly(transfer.getCompressedOnTheFly());
                            local.setStatistics(transfer.getStatistics());
                            local.setProxyName(transfer.getProxyHostName());
                            final var hostName = transfer.getHostName();
                            if (hostName != null) {
                                local.setHost(getECpdsBase().getHost(hostName));
                                local.setHostName(hostName);
                            }
                            // We want now to update the updated local object!
                            _log.debug("Local DataTransfer-{} updated", id);
                            transfer = local;
                        } catch (final Throwable t) {
                            _log.warn("Couldn't update DataTransfer-{} from remote DataMover", id, t);
                            continue;
                        }
                    } else {
                        _log.warn("Couldn't find/clone DataTransfer-{}", id);
                    }
                }
                // Let's update the DataTransfer!
                updateDataTransfer(transfer);
            }
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Update download progress.
     *
     * @param progress
     *            the progress
     *
     * @return the download progress[]
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public DownloadProgress[] updateDownloadProgress(final DownloadProgress[] progress) throws RemoteException {
        try {
            final List<DownloadProgress> toInterrupt = new ArrayList<>();
            for (final DownloadProgress aProgress : progress) {
                final var dataFileId = aProgress.getDataFileId();
                final var root = aProgress.getRoot();
                final var progressInterface = getProgressInterface(dataFileId);
                if (progressInterface != null) {
                    // We found it!
                    progressInterface.update(root, aProgress.getByteSent());
                } else {
                    // Not found so it should be interrupted on the Mover (e.g. the
                    // retrieval has been interrupted because it was too slow)!
                    _log.warn("DownloadProgress not found/ignored - " + aProgress.toString());
                    toInterrupt.add(aProgress);
                }
            }
            // Return all the DownloadProgress not found so that the Mover can
            // interrupt them!
            return toInterrupt.toArray(new DownloadProgress[toInterrupt.size()]);
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Check pending ticket.
     *
     * @param transfer
     *            the transfer
     * @param updateRepository
     *            the update repository
     */
    public void checkPendingTicket(final DataTransfer transfer, final boolean updateRepository) {
        final var ticket = (PendingUpdateTicket) theTicketRepository.remove(transfer.getId());
        if (ticket != null) {
            ticket.completed();
            transfer.setStatusCode(ticket.getStatusCode());
            transfer.setUserStatus(ticket.getUserName());
            transfer.setComment(ticket.getByAndFrom());
            transfer.setDuration(0);
            transfer.setSent(0);
            transfer.setPutTime(null);
            final var code = ticket.getStatusCode();
            if (!StatusFactory.STOP.equals(code) && !StatusFactory.FAIL.equals(code) && !StatusFactory.INIT.equals(code)
                    && !StatusFactory.SCHE.equals(code) && !StatusFactory.FETC.equals(code)) {
                addTransferHistory(transfer);
            }
            if (updateRepository) {
                addDataTransfer(transfer);
            }
        }
    }

    /**
     * Update data transfer.
     *
     * @param transfer
     *            the transfer
     */
    public void updateDataTransfer(final DataTransfer transfer) {
        final var id = transfer.getId();
        if (getDataTransferFromCache(id) == null) {
            _log.debug("DataTransfer-" + id + " not in cache (ignored): " + transfer);
            return;
        }
        final var local = getDataTransfer(id);
        if (local != null) {
            transfer.setPriority(local.getPriority());
        }
        final var code = transfer.getStatusCode();
        final var done = StatusFactory.DONE.equals(code);
        final var stop = StatusFactory.STOP.equals(code) || StatusFactory.FAIL.equals(code);
        final var retr = StatusFactory.RETR.equals(code) || StatusFactory.INTR.equals(code);
        if (done || stop || retr) {
            final var retryTime = transfer.getRetryTime();
            final var finishTime = transfer.getFinishTime();
            final var time = new Timestamp(retryTime != null && finishTime != null
                    ? retryTime.getTime() + finishTime.getTime() : System.currentTimeMillis());
            transfer.setFinishTime(time);
            if (done && transfer.getFirstFinishTime() == null) {
                transfer.setFirstFinishTime(time);
            }
            checkPendingTicket(transfer, false);
            theTransferScheduler.notifyCompletion(transfer);
            if (done && transfer.getDuration() == 0) {
                _log.debug("0ms duration detected for DataTransfer-{} push (forcing 1ms)", id);
                transfer.setDuration(1);
            }
            if (done) {
                transfer.setNotify(true);
                transfer.setRecordUPH(true);
            }
            try {
                final var destination = getDataBase().getDestination(transfer.getDestinationName());
                final var firstRetry = transfer.getRequeueCount() == 0 && transfer.getFailedTime() == null;
                if (done && destination.getMailOnEnd()
                        || (stop || retr && !firstRetry) && destination.getMailOnError()) {
                    sendECpdsMessage(transfer);
                }
                if (done && new DestinationOption(destination).deleteFromSpoolOnSuccess()) {
                    addTransferHistory(transfer);
                    transfer.setDeleted(true);
                    addTransferHistory(transfer, null, code, "Deleted on successful data transmission", false);
                }
            } catch (final DataBaseException e) {
                _log.debug(e);
            }
        }
        addDataTransfer(transfer);
    }

    /**
     * Adds the data transfer.
     *
     * @param transfer
     *            the transfer
     */
    public void addDataTransfer(final DataTransfer transfer) {
        theTransferRepository.put(transfer);
    }

    /**
     * Removes the data transfer.
     *
     * @param transfer
     *            the transfer
     */
    public void removeDataTransfer(final DataTransfer transfer) {
        theTransferRepository.removeValue(transfer);
    }

    /**
     * Removes the data file and data transfers.
     *
     * @param file
     *            the file
     * @param username
     *            the username
     * @param byAndFrom
     *            the comment
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    public void removeDataFileAndDataTransfers(final DataFile file, final String username, final String byAndFrom)
            throws MasterException, DataBaseException {
        for (final DataTransfer transfer : getDataTransfers(file.getId())) {
            // Let's make sure the file is not in EXEC mode!
            updateTransferStatus(transfer.getId(), StatusFactory.STOP, true, username, "Deleted " + byAndFrom, true,
                    false, true);
            // Remove it from the Transfer Repository!
            if (theTransferRepository != null) {
                removeDataTransfer(transfer);
            }
            // Make sure it is stopped
            transfer.setStatusCode(StatusFactory.STOP);
            transfer.setDeleted(true);
            getECpdsBase().update(transfer);
        }
        purgeDataFile(file, byAndFrom);
    }

    /**
     * Adds the transfer history.
     *
     * @param transfer
     *            the transfer
     */
    public void addTransferHistory(final DataTransfer transfer) {
        addTransferHistory(transfer, transfer.getStatusCode(), transfer.getComment());
    }

    /**
     * Adds the transfer history.
     *
     * @param transfer
     *            the transfer
     * @param code
     *            the code
     * @param comment
     *            the comment
     */
    public void addTransferHistory(final DataTransfer transfer, final String code, final String comment) {
        addTransferHistory(transfer, null, code, comment, false);
    }

    /**
     * Adds the transfer history.
     *
     * @param transfer
     *            the transfer
     * @param source
     *            source host
     * @param code
     *            the code
     * @param comment
     *            the comment
     * @param error
     *            is it an error?
     */
    public void addTransferHistory(final DataTransfer transfer, final Host source, final String code,
            final String comment, final boolean error) {
        if (theHistoryRepository == null) {
            return;
        }
        final var history = new TransferHistory();
        history.setDataTransfer(transfer);
        history.setDataTransferId(transfer.getId());
        history.setDestinationName(transfer.getDestinationName());
        history.setError(error || StatusFactory.STOP.equals(code) || StatusFactory.FAIL.equals(code)
                || StatusFactory.INTR.equals(code));
        // If it is retrieval then we should have a source host and if it is
        // dissemination then we should have a dissemination host. Otherwise we don't
        // display any host.
        final var host = source != null ? source : transfer.getHost();
        if (host != null) {
            history.setHost(host);
            history.setHostName(host.getName());
        }
        history.setSent(transfer.getSent());
        history.setStatusCode(code);
        history.setTime(new Timestamp(System.currentTimeMillis()));
        if (StatusFactory.EXEC.equals(code) && host != null && (comment == null || comment.isEmpty())) {
            final var method = host.getTransferMethod();
            final var groupName = transfer.getTransferServer().getTransferGroupName();
            final var originalTransferServer = transfer.getOriginalTransferServer();
            final var originalName = originalTransferServer != null ? originalTransferServer.getTransferGroupName()
                    : null;
            history.setComment("Trying TransferMethod=" + method.getName() + " (" + method.getECtransModuleName()
                    + ") from TransferGroup=" + groupName + (originalName != null && !groupName.equals(originalName)
                            ? " (data on TransferGroup=" + originalName + ")" : ""));
        } else {
            history.setComment(comment);
        }
        final var message = history.getComment();
        if (isNotEmpty(message)) {
            _log.debug("Add TransferHistory " + transfer.getId() + "=" + code + ": " + message);
            theHistoryRepository.put(history);
        } else {
            _log.warn("TransferHistory " + transfer.getId() + "=" + code + " not added (no message specified)");
        }
        if (transfer.getNotify()) {
            // Is global notification enabled ?
            // Transfer is completed or file is available for download, so we might want to
            // trigger an external event!
            if ((theEventRepository != null) && transfer.getEvent()) {
                if (theEventRepository.getThreadsSize() > 0) {
                    theEventRepository.put(transfer);
                } else {
                    try {
                        theEventRepository.update(transfer);
                    } catch (final Exception e) {
                        _log.warn("Firing event", e);
                    }
                }
            }
            // No need to notify again!
            transfer.setNotify(false);
        }
    }

    /**
     * Gets the progress interface either from the download schedulers or the ecpds plugin (depending on how the data
     * file is retrieved).
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the progress interface
     */
    public ProgressInterface getProgressInterface(final long dataFileId) {
        // Is it on the AcquisitionScheduler?
        var progress = theAcqDownloadScheduler != null ? theAcqDownloadScheduler.getProgressInterface(dataFileId)
                : null;
        if (progress == null) {
            // Is it on the DisseminationScheduler?
            progress = theDissDownloadScheduler != null ? theDissDownloadScheduler.getProgressInterface(dataFileId)
                    : null;
        }
        if (progress == null) {
            // Is it on the ECpdsPlugin?
            progress = currentDataFiles.get(dataFileId);
        }
        return progress;
    }

    /**
     * Gets the number of bytes retrieved. If the thread is not found on both download schedulers then -1 is returned.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the retrieved
     */
    public long getRetrieved(final long dataFileId) {
        final var progress = getProgressInterface(dataFileId);
        if (progress != null) {
            return progress.getByteSent();
        }
        return 0;
    }

    /**
     * Gets the transfer server name.
     *
     * @param dataFileId
     *            the data file id
     *
     * @return the transfer server name
     */
    public String getTransferServerName(final long dataFileId) {
        final var progress = getProgressInterface(dataFileId);
        if (progress != null) {
            return progress.getRoot();
        }
        return null;
    }

    /**
     * Gets the data transfer from cache.
     *
     * @param id
     *            the id
     *
     * @return the data transfer from cache
     */
    public DataTransfer getDataTransferFromCache(final long id) {
        return theTransferRepository != null ? theTransferRepository.getDataTransfer(id) : null;
    }

    /**
     * Gets the status.
     *
     * @param destinationName
     *            the Destination name
     *
     * @return the status
     */
    public String getStatus(final String destinationName) {
        return theTransferRepository != null ? theTransferRepository.getStatus(destinationName) : null;
    }

    /**
     * Gets the data transfers.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the data transfers
     */
    public DataTransfer[] getDataTransfers(final String destinationName) {
        return theTransferRepository != null ? theTransferRepository.getDataTransfers(destinationName)
                : new DataTransfer[0];
    }

    /**
     * Gets the data transfer.
     *
     * @param id
     *            the id
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer(final long id) {
        final var transfer = getDataTransferFromCache(id);
        if (transfer == null) {
            return getDataBase().getDataTransferObject(id);
        }
        return transfer;
    }

    /**
     * Gets the data transfer.
     *
     * @param transfer
     *            the transfer
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer(final DataTransfer transfer) {
        final var result = getDataTransferFromCache(transfer.getId());
        return result != null ? result : transfer;
    }

    /**
     * Sends the ecpds message.
     *
     * @param transfer
     *            the transfer
     */
    public void sendECpdsMessage(final DataTransfer transfer) {
        if (sendMailNotificationsForTransfers) {
            final var destination = transfer.getDestination();
            final var comment = transfer.getComment();
            final var file = transfer.getDataFile();
            final var ecuser = destination.getECUser();
            final var mailto = destination.getUserMail();
            sendECpdsMessage(mailto != null ? mailto : ecuser.getName(),
                    "request: " + file.getId() + " " + transfer.getStatusCode() + " (" + destination.getName() + ")",
                    "Message concerning ECpds request: " + file.getId() + "\nRequest status: "
                            + transfer.getStatusCode() + (isNotEmpty(comment) ? "\nComment: " + comment : "")
                            + "\nRequest owner: " + ecuser.getName() + "\nTarget destination: " + destination.getName()
                            + "\nOriginal file: " + file.getOriginal() + "\nFile size: " + file.getSize()
                            + "\nTarget file: " + transfer.getTarget() + "\nMail sent at: "
                            + Format.formatCurrentTime());
        } else {
            _log.warn("Mail notification not sent for DataTransfer {} (email notification for transfer disabled)",
                    transfer.getId());
        }
    }

    /**
     * Sends the ecpds message.
     *
     * @param to
     *            the to
     * @param cc
     *            the cc
     * @param subject
     *            the subject
     * @param content
     *            the content
     * @param attachmentName
     *            the attachment name
     * @param attachmentContent
     *            the attachment content
     */
    public void sendECpdsMessage(final String to, final String cc, final String subject, final String content,
            final String attachmentName, final String attachmentContent) {
        if (theMailRepository != null) {
            theMailRepository.sendMail(null, to, cc, "[" + mailSubjectTag + "] " + subject, content, attachmentName,
                    attachmentContent);
        } else {
            _log.warn("Mail not sent: to={}, cc={}, subject={}, attachment={}\n{} (email service disabled)", to, cc,
                    subject, attachmentName, content);
        }
    }

    /**
     * Sends the ecpds message.
     *
     * @param to
     *            the to
     * @param subject
     *            the subject
     * @param content
     *            the content
     */
    public void sendECpdsMessage(final String to, final String subject, final String content) {
        sendECpdsMessage(to, null, subject, content, null, null);
    }

    /**
     * Checks if is available.
     *
     * @return the long
     */
    @Override
    public long isAvailable() {
        return System.currentTimeMillis();
    }

    /**
     * This method is called to interrupt the acquisition thread activity for the specified host.
     *
     * @param host
     *            the host
     *
     * @return the host
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public void interruptAquisitionFor(final Host host) throws DataBaseException {
        if (HostOption.ACQUISITION.equals(host.getType())) {
            final var hostName = host.getName();
            try (final var mutex = hostMutexProvider.getMutex(hostName)) {
                synchronized (mutex.lock()) {
                    try {
                        // If there is an acquisition thread running for this Host then let's kill it!
                        if (theAcquisitionScheduler != null) {
                            if (theAcquisitionScheduler.interruptAcquisitionFor(host)) {
                                _log.debug("Acquisition Thread interrupted for Host-{}", host.getName());
                            } else {
                                _log.debug("Acquisition Thread NOT interrupted for Host-{}", host.getName());
                            }
                        }
                        // Reset the host output so that the thread will be restarted as soon as
                        // possible!
                        _log.debug("Reset Host output for Host-{}", host.getName());
                        getDataBase().update(new HostOutput(host.getHostOutputId()));
                    } catch (final Throwable t) {
                        _log.warn("Interrupting Acquisition Host-{}", hostName, t);
                    }
                }
            }
        }
    }

    /**
     * This method is called from the Web monitoring.
     *
     * @param host
     *            the host
     *
     * @return the host
     *
     * @throws DataBaseException
     *             the data base exception
     */
    Host updateHost(final Host host) throws DataBaseException {
        final var hostName = host.getName();
        try (final var mutex = hostMutexProvider.getMutex(hostName)) {
            synchronized (mutex.lock()) {
                try {
                    // Update the data time-stamp for the updateData method!
                    final var setup = HOST_ECTRANS.getECtransSetup(host.getData());
                    setup.set(HOST_ECTRANS_LASTUPDATE, System.currentTimeMillis());
                    host.setData(setup.getData());
                    // Deal with the Acquisition
                    interruptAquisitionFor(host);
                    _log.debug("Data update requested for Host-{} (monitoring)", hostName);
                    getDataBase().update(host);
                    if (theTransferScheduler != null) {
                        // The configuration might have been changed by the user, so
                        // the update must be propagated to the transfer scheduler!
                        theTransferScheduler.updateHost(host);
                    }
                    // The location might have changed?
                    updateLocation(host);
                } catch (final Throwable t) {
                    _log.warn("Updating Host-{}", hostName, t);
                }
            }
        }
        return host;
    }

    /**
     * This method is only updating the statistics for the Host. This is called once a file has been uploaded either
     * from the history repository or the host check scheduler.
     *
     * @param host
     *            the host
     * @param connections
     *            the connections
     * @param sent
     *            the sent
     * @param duration
     *            the duration
     * @param valid
     *            the valid
     * @param checkTime
     *            the check time
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private void _updateHostStats(final Host host, final int connections, final long sent, final long duration,
            final boolean valid, final Timestamp checkTime) throws DataBaseException {
        final var hostStats = host.getHostStats();
        final var hostStatsId = hostStats.getId();
        try (final var mutex = hostStatsMutexProvider.getMutex(hostStatsId)) {
            synchronized (mutex.lock()) {
                try {
                    hostStats.setConnections(hostStats.getConnections() + connections);
                    hostStats.setSent(hostStats.getSent() + sent);
                    hostStats.setDuration(hostStats.getDuration() + duration);
                    hostStats.setValid(valid);
                    hostStats.setCheckTime(checkTime);
                    getDataBase().update(hostStats);
                } catch (final Throwable e) {
                    _log.warn("Updating HostStats-" + hostStatsId, e);
                }
            }
        }
    }

    /**
     * This method is only updating the output for the Host. This is called from the acquisition scheduler.
     *
     * @param host
     *            the host
     * @param output
     *            the output
     */
    private void _updateHostOutput(final Host host, final String output) {
        final var hostId = host.getName();
        try (final var mutex = hostOutputMutexProvider.getMutex(hostId)) {
            synchronized (mutex.lock()) {
                try {
                    final var hostOutput = new HostOutput(host.getHostOutputId());
                    hostOutput.setOutput(output);
                    hostOutput.setAcquisitionTime(new Timestamp(System.currentTimeMillis()));
                    getDataBase().update(hostOutput);
                } catch (final Throwable t) {
                    _log.warn("Updating output for Host-" + hostId, t);
                }
            }
        }
    }

    /**
     * Update data.
     *
     * This method is used from the MoverProvider on the MoverServer (updateMSUser) to update the Host. The method will
     * make sure the Host was not updated by a newer version. If a newer version exists then this update will not be
     * done. The only field updated in this method is the DATA.
     *
     * @param host
     *            the host
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void updateData(final Host host) throws RemoteException {
        try {
            updateData(host.getName(), host.getData());
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Update data.
     *
     * This method is used from the MoverProvider on the MoverServer (updateMSUser) to update the Host. The method will
     * make sure the Host was not updated by a newer version. If a newer version exists then this update will not be
     * done. The only field updated in this method is the DATA.
     *
     * @param hostId
     *            the host id
     * @param data
     *            the data
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void updateData(final String hostId, final String data) throws RemoteException {
        final var base = getDataBase();
        try (final var mutex = hostMutexProvider.getMutex(hostId)) {
            synchronized (mutex.lock()) {
                try {
                    final var initialHost = base.getHost(hostId);
                    final var initialData = initialHost.getData();
                    // Check if the Host was updated since the host object was initially retrieved
                    // from the database!
                    if (Objects.equals(HOST_ECTRANS.getECtransSetup(initialData).getLong(HOST_ECTRANS_LASTUPDATE),
                            HOST_ECTRANS.getECtransSetup(data).getLong(HOST_ECTRANS_LASTUPDATE))) {
                        // The timestamp is the same so we can safely update the data!
                        initialHost.setData(data);
                        _log.debug("Data update requested for Host-" + hostId);
                        base.update(initialHost);
                    } else {
                        _log.debug("Out of sync for data update request on Host-" + hostId);
                    }
                } catch (final Throwable t) {
                    _log.warn("Updating data for Host-" + hostId, t);
                }
            }
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Update location.
     *
     * This method is used from the MoverProvider on the MoverServer (getMSUser) to update the Host location. This is
     * triggered when the MoverServer has detected a change in the IP address. It is also called from the Web monitoring
     * and JMX interface.
     *
     * @param host
     *            the host
     */
    @Override
    public void updateLocation(final Host host) {
        final var hostLocation = host.getHostLocation();
        final var hostId = host.getName();
        try (final var mutex = hostLocationMutexProvider.getMutex(hostId)) {
            synchronized (mutex.lock()) {
                try {
                    // Is it configured for automatic location discovery?
                    if (host.getAutomaticLocation()) {
                        // Update geolocation. If the IP is not defined then use the hostName to find
                        // the IP.
                        final var dnsName = host.getHost();
                        var hostIp = hostLocation.getIp();
                        try {
                            final var latestHostIp = InetAddress.getByName(dnsName).getHostAddress();
                            if (isEmpty(hostIp) || !hostIp.equals(latestHostIp)) {
                                hostIp = latestHostIp;
                                // No IP address defined, or IP updated!
                                hostLocation.setIp(hostIp);
                                // Local host is obviously not in the database!
                                if (!"127.0.0.1".equals(hostIp) && !"::1".equals(hostIp)) {
                                    final var start = System.currentTimeMillis();
                                    final var location = GeoIP2Helper.getCityResponse(hostIp).getLocation();
                                    if (location == null) {
                                        _log.warn("Could not get geolocation for Host-{}: {}", hostId, hostIp);
                                    } else {
                                        final var latitude = location.getLatitude();
                                        final var longitude = location.getLongitude();
                                        if (_log.isDebugEnabled()) {
                                            _log.debug(
                                                    "New location found for Host-{} ({}): latitude={}, longitude={} ({})",
                                                    hostId, hostIp, latitude, longitude,
                                                    Format.formatDuration(System.currentTimeMillis() - start));
                                        }
                                        hostLocation.setLatitude(latitude);
                                        hostLocation.setLongitude(longitude);
                                        getDataBase().update(hostLocation);
                                    }
                                }
                            }
                        } catch (final AddressNotFoundException e) {
                            _log.warn("Could not get geolocation for Host-{}: {}", hostId, e.getMessage());
                        } catch (final Throwable t) {
                            _log.warn("Could not get geolocation for Host-{}: {}", hostId, hostIp, t);
                        }
                    }
                } catch (final Throwable t) {
                    _log.warn("Updating location for Host-{}", hostId, t);
                }
            }
        }
    }

    /**
     * This method is updating the locations for every Host in the database (called from the JMX interface).
     *
     * @return the int
     */
    public int updateHostLocations() {
        var update = 0;
        for (final Host host : getDataBase().getHostArray()) {
            updateLocation(host);
            update++;
        }
        return update;
    }

    /**
     * Gets the ecauth token.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public ECauthToken getECauthToken(final String user) throws RemoteException {
        try {
            return ECauthTokenGenerator.getInstance().getECauthToken(user);
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Gets the e tag.
     *
     * @param dataTransferId
     *            the data transfer id
     *
     * @return the e tag
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public String getETag(final long dataTransferId) throws RemoteException {
        try {
            return TransferManagement.getETag(getDataTransfer(dataTransferId).getDataFile());
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Gets the ecpds session.
     *
     * @param user
     *            the user
     * @param password
     *            the password
     * @param root
     *            the root
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
    ECpdsSession getECpdsSession(final String user, final String password, final String root, final String host,
            final String agent, final String comment) throws MasterException, DataBaseException, RemoteException {
        final var restricted = Cnf.stringListAt("Restricted", user);
        if (restricted.length > 0) {
            var allowed = false;
            final var dns = Format.getHostName(root);
            for (var i = 0; !allowed && i < restricted.length; i++) {
                allowed = dns.matches(restricted[i]);
            }
            if (!allowed) {
                _log.warn("User " + user + " not allowed to connect from " + dns);
                throw new MasterException("User " + user + " not allowed to connect from " + dns);
            }
        }
        final var base = getDataBase();
        final var webUser = getWebUser(user, password, root);
        final var ecuser = base.getECUser(user);
        final var activity = Cnf.at("Server", "anonymousUser", "anonymous").equals(user) ? null
                : newActivity(ecuser, "MonitorPlugin", host, agent, "login", comment, false);
        webUser.setLastLogin(new Timestamp(System.currentTimeMillis()));
        webUser.setLastLoginHost(host);
        base.update(webUser);
        return new ECpdsSession(webUser, root, activity);
    }

    /**
     * Write a Serial DataBaseObject to the disk!.
     *
     * @param object
     *            the object
     * @param file
     *            the file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void _writeObject(final DataBaseObject object, final File file) throws IOException {
        final var fos = new FileOutputStream(file);
        final var oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
    }

    /**
     * Read a Serial DataBaseObject from the disk!.
     *
     * @param file
     *            the file
     *
     * @return the object
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    private static Object _readObject(final File file) throws IOException, ClassNotFoundException {
        final var fis = new FileInputStream(file);
        final var ois = new ObjectInputStream(fis);
        final var object = (DataBaseObject) ois.readObject();
        ois.close();
        return object;
    }

    /**
     * Import Destinations, Hosts and ECUsers, and Transfer Methods and ECtrans Modules from the disk!.
     *
     * @param dir
     *            the dir
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _importDestinationsAndHosts(final String dir) throws IOException {
        final var destinations = new HashMap<String, ArrayList<Association>>();
        final var base = getDataBase();
        try (final var paths = Files.walk(Paths.get(dir))) {
            paths.forEach(path -> {
                if (!Files.isDirectory(path)) {
                    final var fileName = path.toFile().getName();
                    if (fileName.matches("(.*)_Association_\\d+")) { // The Associations includes all the Objects
                        try {
                            final var association = (Association) _readObject(path.toFile());
                            final var destinationName = association.getDestinationName();
                            if (base.getDestinationObject(destinationName) == null) {
                                var associations = destinations.get(destinationName);
                                if (associations == null) {
                                    destinations.put(destinationName, associations = new ArrayList<>());
                                }
                                associations.add(association);
                            }
                        } catch (ClassNotFoundException | IOException e) {
                            _log.warn("Processing association: {}", fileName, e);
                        }
                    }
                }
            });
        }
        // Now let's go through the list and import the Destinations!
        for (final ArrayList<Association> associations : destinations.values()) {
            importDestination(associations.get(0).getDestination(), associations.toArray(new Association[0]), false);
        }
    }

    /**
     * Export destinations, hosts, associations, ecusers, transfer methods, ectrans modules to the disk!.
     *
     * @param dir
     *            the dir
     * @param type
     *            the type
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _exportDestinationsAndHosts(final String dir, final Integer type) throws IOException {
        final var directory = new File(dir);
        final var base = getDataBase();
        final var associations = base.getAssociationArray();
        for (final Destination destination : base.getDestinationArray()) {
            if (type == null || type == -1 || type == destination.getType()) {
                // Dump Destination
                _log.debug("Dumping Destination: " + destination);
                _writeObject(destination, new File(directory, destination.getName() + "_Destination"));
                for (final Association association : associations) {
                    if (association.getDestinationName().equals(destination.getName())) {
                        final var host = association.getHost();
                        // Dump host
                        _log.debug("Dumping Host: " + host);
                        _writeObject(host, new File(directory, destination.getName() + "_Host_" + host.getName()));
                        // Dump association
                        _log.debug("Dumping Association: " + association);
                        _writeObject(association,
                                new File(directory, destination.getName() + "_Association_" + host.getName()));
                    }
                }
            }
        }
    }

    /**
     * Check if the specified Host is a shared Host? Meaning, used in more than one association. If the call is remote
     * and a shared Host is found then the name of the target Host is updated with the name of the local host.
     *
     * @param associationsList
     *            the associations list
     * @param host
     *            the host
     * @param remoteRequest
     *            the remote request
     *
     * @return true if is a shared host
     */
    private static boolean _isSharedHost(final Association[] associationsList, final Host host,
            final boolean remoteRequest) {
        final var nickName = host.getNickname();
        final var hostId = host.getName();
        var count = 0;
        for (final Association association : associationsList) {
            if (remoteRequest) {
                // This request comes from an external master server so we have
                // to check on the nick-name instead of the name which cannot be
                // synchronized!
                if (association.getHost().getNickname().equals(nickName)) {
                    host.setName(association.getHostName());
                    return true;
                }
            } else if (association.getHostName().equals(hostId) && ++count > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copy a Host within the same Destination.
     *
     * @param destinationName
     *            the destination name
     * @param hostName
     *            the host name
     *
     * @return the host
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public Host copyHost(final String destinationName, final String hostName) throws DataBaseException {
        final var base = getDataBase(ECpdsBase.class);
        // Does the Association exists?
        final var association = base.getAssociation(destinationName, hostName);
        // Let's create the new Host!
        final var host = (Host) base.getHost(hostName).clone();
        host.setName(null);
        // Remove the acquisition output and the timestamp from
        // the user editing!
        final var setup = HOST_ECTRANS.getECtransSetup(host.getData());
        setup.remove(HOST_ECTRANS_LASTUPDATE);
        host.setData(setup.getData());
        final List<DataBaseObject> toRemove = new ArrayList<>();
        try {
            // We have to create a host output for the host!
            final var hostOutput = new HostOutput();
            base.insert(hostOutput, true);
            toRemove.add(hostOutput);
            host.setHostOutputId(hostOutput.getId());
            host.setHostOutput(hostOutput);
            // And a host location!
            final var hostLocation = new HostLocation();
            base.insert(hostLocation, true);
            toRemove.add(hostLocation);
            host.setHostLocationId(hostLocation.getId());
            host.setHostLocation(hostLocation);
            // And a host stats!
            final var hostStats = new HostStats();
            base.insert(hostStats, true);
            toRemove.add(hostStats);
            host.setHostStatsId(hostStats.getId());
            host.setHostStats(hostStats);
            base.insert(host, true);
            toRemove.add(host);
            // And now the new Association!
            final var newAssociation = new Association(destinationName, host.getName());
            newAssociation.setPriority(association.getPriority());
            base.insert(newAssociation, true);
            toRemove.add(newAssociation);
            return host;
        } catch (final DataBaseException e) {
            Collections.reverse(toRemove);
            base.remove(toRemove.toArray(new DataBaseObject[toRemove.size()]));
            throw e;
        }
    }

    /**
     * Copy a destination.
     *
     * @param fromDestinationName
     *            the from destination name
     * @param toDestinationName
     *            the target destination name
     * @param comment
     *            the comment
     * @param copySharedHost
     *            the copy shared host
     *
     * @return the destination
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    public Destination copyDestination(final String fromDestinationName, final String toDestinationName,
            final String comment, final boolean copySharedHost) throws MasterException, DataBaseException {
        final var base = getDataBase(ECpdsBase.class);
        // Does the source Destination exists?
        final var fromDestination = base.getDestinationObject(fromDestinationName);
        if (fromDestination == null) {
            throw new MasterException("Can't process further (original Destination not found)");
        }
        // We only select the associations which are linked with the source
        // Destination!
        final List<Association> associations = new ArrayList<>();
        for (final Association assoc : base.getAssociationArray()) {
            if (fromDestinationName.equals(assoc.getDestinationName())) {
                associations.add(assoc);
            }
        }
        // Now we forward it to the create method.
        return _createDestination(fromDestination, associations.toArray(new Association[associations.size()]),
                toDestinationName, comment, copySharedHost, false);
    }

    /**
     * Import destination.
     *
     * @param fromDestination
     *            the from destination
     * @param linkedAssociations
     *            the linked associations
     * @param copySharedHost
     *            the copy shared host
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public void importDestination(final Destination fromDestination, final Association[] linkedAssociations,
            final boolean copySharedHost) throws RemoteException {
        try {
            // Let's create a new Destination with the same name, comment and hosts!
            _createDestination(fromDestination, linkedAssociations, fromDestination.getName(),
                    fromDestination.getComment(), copySharedHost, true);
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Export destination.
     *
     * @param targetMaster
     *            the target master in the form host:<port>
     * @param fromDestination
     *            the from destination
     * @param copySharedHost
     *            the copy shared host
     *
     * @throws RemoteException
     *             the remote exception
     * @throws DataBaseException
     *             the data base exception
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws NotBoundException
     *             the not bound exception
     */
    public void exportDestination(final String targetMaster, final String fromDestination, final boolean copySharedHost)
            throws RemoteException, DataBaseException, MalformedURLException, NotBoundException {
        final var base = getDataBase(ECpdsBase.class);
        final var destination = base.getDestination(fromDestination);
        // We only select the associations which are linked with the source
        // Destination!
        final List<Association> associations = new ArrayList<>();
        for (final Association assoc : base.getAssociationArray()) {
            if (fromDestination.equals(assoc.getDestinationName())) {
                associations.add(assoc);
            }
        }
        // RMI call to the remote importDestination of the target Master!
        final var master = (MasterInterface) Naming.lookup("//" + targetMaster + "/MasterServer");
        master.importDestination(destination, associations.toArray(new Association[associations.size()]),
                copySharedHost);
    }

    /**
     * Create a destination.
     *
     * @param fromDestination
     *            the from destination
     * @param linkedAssociations
     *            the linked associations list
     * @param toDestinationName
     *            the target destination name
     * @param comment
     *            the comment
     * @param copySharedHost
     *            the copy shared host
     * @param importRequest
     *            the remote request
     *
     * @return the destination
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    private Destination _createDestination(final Destination fromDestination, final Association[] linkedAssociations,
            final String toDestinationName, final String comment, final boolean copySharedHost,
            final boolean importRequest) throws MasterException, DataBaseException {
        final var fromDestinationName = fromDestination.getName();
        _log.debug("Create Destination: {} -> {} (copySharedHost={})", fromDestinationName, toDestinationName,
                copySharedHost);
        if (getDestination(toDestinationName) != null) {
            throw new MasterException("Destination already exists: " + toDestinationName);
        }
        final var newDes = (Destination) fromDestination.clone();
        newDes.setName(toDestinationName);
        newDes.setComment(comment);
        newDes.setUserStatus(null);
        final var value = new SchedulerValue();
        value.setHasRequeued(false);
        value.setStartCount(0);
        final List<DataBaseObject> toRemove = new ArrayList<>();
        final var base = getDataBase(ECpdsBase.class);
        try {
            base.insert(value, true);
            toRemove.add(value);
            newDes.setSchedulerValueId(value.getId());
            newDes.setSchedulerValue(value);
            newDes.setStatusCode(StatusFactory.INIT);
            // Setting TransferGroup for Destination!
            var transferGroup = _getTransferGroup(base, newDes.getTransferGroupName());
            if (transferGroup != null) {
                newDes.setTransferGroupName(transferGroup.getName());
                newDes.setTransferGroup(transferGroup);
            } else {
                newDes.setTransferGroupName(null);
                newDes.setTransferGroup(null);
            }
            if (base.getHostObject(newDes.getHostForSourceName()) == null) {
                newDes.setHostForSourceName(null);
                newDes.setHostForSource(null);
            }
            if (base.getECUserObject(newDes.getECUserName()) == null) {
                base.insert(newDes.getECUser(), false);
            }
            _log.debug("Adding Destination {}: {}", newDes.getName(), newDes);
            base.insert(newDes, false);
            toRemove.add(newDes);
            var i = 0;
            final var localAssociations = base.getAssociationArray();
            for (final Association assoc : linkedAssociations) {
                var host = assoc.getHost();
                // If the Host is part of more than one Association then
                // this is a shared Host (use this one in the Association).
                if (!_isSharedHost(localAssociations, host, importRequest) || copySharedHost) {
                    // We have to create a host output for the host!
                    final var hostOutput = new HostOutput();
                    base.insert(hostOutput, true);
                    toRemove.add(hostOutput);
                    host.setHostOutputId(hostOutput.getId());
                    host.setHostOutput(hostOutput);
                    // And a host location!
                    final var hostLocation = new HostLocation();
                    base.insert(hostLocation, true);
                    toRemove.add(hostLocation);
                    host.setHostLocationId(hostLocation.getId());
                    host.setHostLocation(hostLocation);
                    // And a host stats!
                    final var hostStats = new HostStats();
                    base.insert(hostStats, true);
                    toRemove.add(hostStats);
                    host.setHostStatsId(hostStats.getId());
                    host.setHostStats(hostStats);
                    // We create a new host!
                    host = (Host) host.clone();
                    host.setName(null);
                    host.setNickname(host.getNickname().replaceAll(fromDestinationName, toDestinationName));
                    host.setComment(host.getComment().replaceAll(fromDestinationName, toDestinationName));
                    // Setting TransferGroup for host!
                    transferGroup = _getTransferGroup(base, host.getTransferGroupName());
                    if (transferGroup != null) {
                        host.setTransferGroupName(transferGroup.getName());
                        host.setTransferGroup(transferGroup);
                    } else {
                        host.setTransferGroupName(null);
                        host.setTransferGroup(null);
                    }
                    if (base.getECUserObject(host.getECUserName()) == null) {
                        base.insert(host.getECUser(), false);
                    }
                    if (base.getTransferMethodObject(host.getTransferMethodName()) == null) {
                        final var method = host.getTransferMethod();
                        if (base.getECtransModuleObject(method.getECtransModuleName()) == null) {
                            base.insert(method.getECtransModule(), false);
                        }
                        base.insert(method, false);
                    }
                    _log.debug("Adding Host/Association {}: {}", host.getNickname(), host);
                    // Remove the acquisition output and the timestamp from
                    // the user editing!
                    final var setup = HOST_ECTRANS.getECtransSetup(host.getData());
                    setup.remove(HOST_ECTRANS_LASTUPDATE);
                    host.setData(setup.getData());
                    base.insert(host, true);
                    toRemove.add(host);
                } else {
                    // We are using a shared Host (if the call was remote then
                    // the name (HOS_NAME) has been updated with the name found
                    // in the local database (with the same nick-name).
                    _log.debug("Using shared Host {}: {}", host.getNickname(), host);
                }
                // Let's create the association if it is missing!
                if (base.getAssociationObject(toDestinationName, host.getName()) == null) {
                    final var a = new Association(toDestinationName, host.getName());
                    a.setDestination(newDes);
                    a.setPriority(assoc.getPriority());
                    base.insert(a, false);
                    toRemove.add(a);
                }
                i++;
            }
            try {
                AccessControl.insertAccessControl(base, toDestinationName);
            } catch (final Throwable e) {
                _log.warn("Could not add access control for Destination: {}", toDestinationName, e);
            }
            _log.warn("Done for {} ({} Host(s))", toDestinationName, i);
            return newDes;
        } catch (final Throwable t) {
            if (newDes != null) {
                try {
                    AccessControl.removeAccessControl(base, newDes.getName());
                } catch (final Throwable e) {
                }
                Collections.reverse(toRemove);
                base.remove(toRemove.toArray(new DataBaseObject[toRemove.size()]));
            }
            _log.warn("Create Destination", t);
            throw new MasterException("Couldn't create Destination");
        }
    }

    /**
     * Gets the transfer group.
     *
     * @param base
     *            the base
     * @param transferGroupName
     *            the transfer group name
     *
     * @return the transfer group
     */
    private static TransferGroup _getTransferGroup(final ECpdsBase base, String transferGroupName) {
        if (transferGroupName == null) {
            // Not name defined
            return null;
        }
        // Do we need to translate group names ?
        for (final String replaceEntry : Cnf.listAt("Server", "groupNameTranslationList")) {
            final var replace = replaceEntry.split("=");
            if (replace.length == 2) {
                transferGroupName = transferGroupName.replace(replace[0], replace[1]);
            }
        }
        // Does it exists?
        final var transferGroup = base.getTransferGroupObject(transferGroupName);
        if (transferGroup != null) {
            _log.debug("Updating transfer group name to: " + transferGroupName);
            return transferGroup;
        } else {
            // Transfer group not found so let's take the first one in the list!
            _log.warn("Transfer group not found: " + transferGroupName);
            final var transferGroups = base.getTransferGroupArray();
            if (transferGroups.length > 0) {
                return transferGroups[0];
            } else {
                // No transfer group defined?
                return null;
            }
        }
    }

    /**
     * Update data transfer status to another Master Server.
     *
     * @param remoteMaster
     *            the remote master
     * @param standby
     *            the standby flag
     * @param destination
     *            the destination name
     * @param target
     *            the target name
     * @param uniqueKey
     *            the unique key
     * @param status
     *            the status
     *
     * @return true, if successful
     *
     * @throws RemoteException
     *             the remote exception
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws NotBoundException
     *             the not bound exception
     */
    public boolean updateRemoteTransferStatus(final String remoteMaster, final boolean standby,
            final String destination, final String target, final String uniqueKey, final String status)
            throws RemoteException, MalformedURLException, NotBoundException {
        // RMI call to the remote updateDataTransferStatus of the target Master!
        final var master = (MasterInterface) Naming.lookup("//" + remoteMaster + "/MasterServer");
        return master.updateLocalTransferStatus(getRoot(), standby, destination, target, uniqueKey, status);
    }

    /**
     * Update local transfer status.
     *
     * @param remoteMaster
     *            the remote master
     * @param standby
     *            the standby
     * @param destination
     *            the destination
     * @param target
     *            the target
     * @param uniqueKey
     *            the unique key
     * @param status
     *            the status
     *
     * @return true, if successful
     *
     * @throws RemoteException
     *             the remote exception
     */
    @Override
    public boolean updateLocalTransferStatus(final String remoteMaster, final boolean standby, final String destination,
            final String target, final String uniqueKey, final String status) throws RemoteException {
        try {
            var result = false;
            for (final DataTransfer transfer : getECpdsBase().getScheduledDataTransfer(uniqueKey, destination)) {
                // We don't stop Aliased data transfers, hence the check on the Destination!
                if (transfer.getDestinationName().equals(destination)) {
                    result = updateTransferStatus(transfer.getId(), status, true, null,
                            "Update from Master: " + remoteMaster, true, false, true);
                    if (result) {
                        reloadDestination(transfer);
                    }
                    break;
                }
            }
            return result;
        } catch (final Throwable t) {
            throw Format.getRemoteException("MasterServer=" + getRoot(), t);
        }
    }

    /**
     * Start ecpds action.
     *
     * @param ecpds
     *            the ecpds
     * @param action
     *            the action
     *
     * @return the ecpds action
     *
     * @throws MasterException
     *             the master exception
     */
    ECpdsAction startECpdsAction(final ECpdsSession ecpds, final String action) throws MasterException {
        return startECpdsAction(ecpds, action, null);
    }

    /**
     * Start ecpds action.
     *
     * @param ecpds
     *            the ecpds
     * @param action
     *            the action
     * @param argument
     *            the argument
     *
     * @return the ecpds action
     *
     * @throws MasterException
     *             the master exception
     */
    ECpdsAction startECpdsAction(final ECpdsSession ecpds, final String action, final DataBaseObject argument)
            throws MasterException {
        if (isEmpty(action)) {
            throw new MasterException("Invalid action");
        }
        if (ecpds == null) {
            throw new MasterException("ECpdsSession required");
        }
        return new ECpdsAction(ecpds, action, argument);
    }

    /**
     * Gets the object link.
     *
     * @param object
     *            the object
     *
     * @return the string
     */
    private static String _getObjectLink(final Object object) {
        final var ref = Cnf.at("Server", "monitoring", "");
        return switch (object) {
        case final DataFile dataFile -> ref + "/do/datafile/datafile/" + dataFile.getId();
        case final DataTransfer dataTransfer -> ref + "/do/transfer/data/" + dataTransfer.getId();
        case final Destination destination -> ref + "/do/transfer/destination/" + destination.getName();
        case final Host host -> ref + "/do/transfer/host/" + host.getName();
        case null, default -> null;
        };
    }

    /**
     * Log ecpds action.
     *
     * @param action
     *            the action
     * @param comment
     *            the comment
     * @param exception
     *            the exception
     */
    void logECpdsAction(final ECpdsAction action, final String comment, final Exception exception) {
        logECpdsAction(action, comment, null, exception);
    }

    /**
     * Log ecpds action.
     *
     * @param action
     *            the action
     * @param comment
     *            the comment
     * @param object
     *            the object
     * @param exception
     *            the exception
     */
    void logECpdsAction(final ECpdsAction action, final String comment, final DataBaseObject object,
            final Exception exception) {
        try {
            if (Cnf.at("Server", "anonymousUser", "anonymous").equals(action.getWebUserId())) {
                // We don't want to log any anonymous activity. This is internal to ECPDS only.
                return;
            }
            final var argument = action.getArgument();
            final var operation = new StringBuilder(action.getAction());
            final var keyValues = new StringBuilder();
            final String link;
            if (argument != null) {
                // Let's build the link URL and the operation!
                operation.append(Format.getClassName(argument));
                link = _getObjectLink(argument);
                for (final Object value : getDataBase().getPrimaryKeyValues(argument)) {
                    keyValues.append(keyValues.length() > 1 ? "," : "").append(value);
                }
                if (keyValues.length() > 0) {
                    keyValues.insert(0, "(");
                    keyValues.append(")");
                }
            } else {
                // No argument specified!
                link = null;
            }
            final var emails = new StringBuilder(Cnf.at("Server", "email", ""));
            var isDataTransfer = false;
            var isDestination = false;
            var isHost = false;
            // Do we have to send a notification to the Destination owner?
            if (argument != null) {
                // For the Destinations, DataTransfers, Associations and Aliases let's get the
                // Destinations involved and build the list of user-mail to notify!
                final List<Destination> destinations = new ArrayList<>();
                if (argument instanceof final Destination destination) {
                    destinations.add(destination);
                    isDestination = true;
                }
                if (argument instanceof final Host host) {
                    Collections.addAll(destinations, getECpdsBase().getDestinationsByHostName(host.getName()));
                    isHost = true;
                }
                if (argument instanceof final DataTransfer dataTransfer) {
                    destinations.add(dataTransfer.getDestination());
                    isDataTransfer = true;
                }
                if (argument instanceof final Association association) {
                    destinations.add(association.getDestination());
                }
                if (argument instanceof final Alias alias) {
                    destinations.add(alias.getDestination());
                }
                // Let's build the list of email addresses!
                for (final Destination destination : destinations) {
                    if (destination != null && destination.getMailOnUpdate()) {
                        final var email = destination.getUserMail();
                        if (isNotEmpty(email) && emails.indexOf(email) == -1) {
                            _log.debug("Adding email address: " + email);
                            emails.append(";").append(email);
                        }
                    }
                }
            }
            final var session = action.getECpdsSession();
            final var webUser = session.getWebUser();
            final var activity = session.getActivity();
            final var success = exception == null;
            final var statusMessage = success ? "SUCCESSFUL" : "NOT-SUCCESSFUL";
            // Record a new event in the DataBase!
            newEvent(activity, operation.toString(),
                    operation.append(keyValues).toString() + ": " + (isNotEmpty(comment) ? comment : statusMessage)
                            + (!success ? " <- " + Format.getMessage(exception) : ""),
                    !success);
            // Should we record the change?
            if (!(Cnf.at("Server", "dontRecordTransferActions", true) && isDataTransfer
                    || Arrays.stream(Cnf.stringListAt("Server", "dontRecordActions", "output", "report"))
                            .anyMatch(action.getAction()::equals))) {
                final var content = new StringBuilder("Message concerning ECpds action: ").append(operation);
                if (isNotEmpty(comment)) {
                    content.append("\nAction comment: ").append(comment);
                }
                content.append("\nExecution status: ").append(statusMessage);
                if (link != null) {
                    content.append("\nReference: ").append(link);
                }
                content.append("\nMaster server: ").append(getRoot()).append("\nOwner identifier: ")
                        .append(webUser.getId()).append("\nOwner name: ").append(webUser.getName());
                content.append("\nOwner gateway: ")
                        .append(session.getRoot() == null ? "local" : Format.getHostName(session.getRoot()));
                if (activity != null) {
                    content.append("\nPlugin name: ").append(activity.getPlugin()).append("\nOwner agent: ")
                            .append(activity.getAgent());
                }
                if (webUser.getLastLoginHost() != null && webUser.getLastLogin() != null) {
                    content.append("\nLogin from: ").append(Format.getHostName(webUser.getLastLoginHost()))
                            .append("\nLogin time: ").append(Format.formatTime(webUser.getLastLogin().getTime()));
                }
                content.append("\nMail sent at: ").append(Format.formatCurrentTime());
                if (argument != null) {
                    content.append("\n\nOriginal Content\n===============================\n")
                            .append(DataBaseObject.toString(argument)).append("\n===============================");
                }
                if (object != null) {
                    content.append("\n\nTarget Content\n===============================\n")
                            .append(DataBaseObject.toString(object)).append("\n===============================");
                }
                if (success && argument != null && object != null && (isDestination || isHost)) {
                    final var changeLog = new ChangeLog();
                    changeLog.setWebUserId(webUser.getId());
                    changeLog.setTime(new Timestamp(System.currentTimeMillis()));
                    changeLog.setOldObject(argument.toString(false));
                    changeLog.setNewObject(object.toString(false));
                    if (argument instanceof final Destination destination) {
                        changeLog.setKeyName("DES_NAME");
                        changeLog.setKeyValue(destination.getName());
                    }
                    if (argument instanceof final Host host) {
                        changeLog.setKeyName("HOS_NAME");
                        changeLog.setKeyValue(host.getName());
                    }
                    getDataBase().tryInsert(changeLog, true);
                }
                if (argument != null && object != null && argument.getClass().equals(object.getClass())) {
                    // If one of the Object is null or they are not from the same
                    // Class then we can't do any comparison!
                    final var compare = argument.compare(object, true);
                    if (isNotEmpty(compare)) {
                        content.append("\n\nDifference(s)\n===============================\n").append(compare)
                                .append("\n===============================");
                    }
                }
                if (!success) {
                    content.append("\n\nError message:\n===============================\n")
                            .append(Format.getMessage(exception)).append("\n===============================");
                }
                // Send a notification email!
                sendECpdsMessage(emails.toString(), new StringBuilder(operation).append(" initiated by ")
                        .append(webUser.getId()).append(": ").append(statusMessage).toString(), content.toString());
            }
        } catch (final Exception e) {
            _log.warn(e);
        }
    }

    /**
     * Close ecpds session.
     *
     * @param session
     *            the session
     * @param expired
     *            the expired
     */
    void closeECpdsSession(final ECpdsSession session, final boolean expired) {
        newEvent(session.getActivity(), "login", expired ? "Session expired" : null, false);
    }

    /**
     * Allow getting a Web User. The credentials are checked before returning the Web User.
     *
     * @param user
     *            the user
     * @param credentials
     *            the credentials
     * @param root
     *            the root
     *
     * @return the web user
     *
     * @throws MasterException
     *             the master exception
     */
    public WebUser getWebUser(final String user, final String credentials, final String root) throws MasterException {
        if (!Cnf.at("Server", "anonymousUser", "anonymous").equals(user)) { // No need to log anonymous requests!
            _log.debug("Authentication request for " + user + " from " + root);
        }
        // Are the credentials valid?
        if (isEmpty(credentials)) {
            throw new MasterException("Empty password not allowed");
        }
        // Is it a valid ECUser ?
        final var ecuser = getECUser(user, true);
        if (ecuser == null) {
            throw new MasterException("User not found (ECUser)");
        }
        // Is it a valid Web User?
        final var webUser = getDataBase().getWebUserObject(user);
        if (webUser == null) {
            throw new MasterException("User not found (WebUser)");
        }
        if (!webUser.getActive()) {
            throw new MasterException("User disabled");
        }
        // Is it the anonymous user or a user with a static password?
        if (Cnf.at("Roots", user, "").equals(credentials) || Cnf.at("Server", "anonymousUser", "anonymous").equals(user)
                && Cnf.at("Server", "anonymousPass", "anonymous").equals(credentials)) {
            _log.debug("Static authentication successful for " + user + " (Cnf)");
            return webUser;
        }
        final var isPasscode = LoginManagement.isPasscode(credentials);
        if (!isPasscode) {
            // Is it a Certificate authentication or a password?
            try {
                final var certificate = Tools.fromPEM(credentials);
                final var userId = certificate.getSubjectX500Principal().getName();
                _log.debug("Certificate authentication successful for " + userId);
                return webUser;
            } catch (final CertificateExpiredException e) {
                _log.error("Certificate authentication failed: expired", e);
                throw new MasterException("Certificate expired");
            } catch (final CertificateNotYetValidException e) {
                _log.error("Certificate authentication failed: not yet valid", e);
                throw new MasterException("Certificate not yet valid");
            } catch (final CertificateException e) {
                // Can only be a password, so let's continue if we are allowed from this place?
                if (!root.matches(allowedPasswordOn)) {
                    _log.error("Password authentication failed for " + user + " (password refused from " + root + ")");
                    throw new MasterException("Authentication failed");
                }
            } catch (final Exception e) {
                _log.error("Certificate authentication failed: " + credentials, e);
                throw new MasterException("Authentication failed");
            }
        }
        // Let's do a TOTP or local authentication!
        if (TOTP.ACTIVE) {
            boolean authenticated;
            try {
                authenticated = TOTP.authenticate(user, credentials, isPasscode);
            } catch (IOException | URISyntaxException e) {
                authenticated = false;
            }
            final var mode = isPasscode ? "passcode" : "password";
            if (!authenticated) {
                _log.error("TOTP authentication failed for " + user + " (" + mode + ")");
                throw new MasterException("Authentication failed");
            }
            _log.debug("TOTP authentication successful for " + user + " (" + mode + ")");
        } else if (!credentials.equals(webUser.getPassword())) {
            _log.error("Static authentication failed for " + user);
            throw new MasterException("Authentication failed");
        } else {
            _log.debug("Static authentication successful for " + user + " (WebUser)");
        }
        return webUser;
    }

    /**
     * Creates the web user.
     *
     * @param webUser
     *            the web user
     *
     * @return the web user
     *
     * @throws MasterException
     *             the master exception
     * @throws DataBaseException
     *             the data base exception
     */
    public void saveWebUser(final WebUser webUser) throws MasterException, DataBaseException {
        final var base = getDataBase();
        final var user = webUser.getId();
        // Is it the anonymous user?
        if (Cnf.at("Server", "anonymousUser", "anonymous".equals(user))) {
            throw new MasterException("User " + user + " is reserved");
        }
        // Do we have an existing entry with the same name?
        if (base.getWebUserObject(user) != null) {
            // This is an update
            base.update(webUser);
        } else {
            // Do we have an existing ECuser?
            var ecuser = getECUser(user, true);
            if (ecuser == null) {
                // The ECuser does not exists so let's create a new one!
                ecuser = new ECUser(webUser.getId());
                ecuser.setUid(-1);
                ecuser.setGid(-1);
                ecuser.setDir("");
                ecuser.setShell("");
                ecuser.setComment(webUser.getName());
                base.insert(ecuser, false);
            }
            // Now let's create the web user
            base.insert(webUser, false);
        }
    }

    /**
     * Shutdown.
     */
    @Override
    public void shutdown() {
        if (theTransferScheduler != null) {
            theTransferScheduler.shutdown(getGraceful());
        }
        if (theNotificationRepository != null) {
            theNotificationRepository.shutdown();
        }
        if (theTicketRepository != null) {
            theTicketRepository.shutdown();
        }
        if (theTransferRepository != null) {
            theTransferRepository.shutdown();
        }
        if (theMailRepository != null) {
            theMailRepository.shutdown(getGraceful());
        }
        if (theTransferRepository != null) {
            theTransferRepository.shutdown();
        }
        if (theHistoryRepository != null) {
            theHistoryRepository.shutdown();
        }
        if (theProxyHostRepository != null) {
            theProxyHostRepository.shutdown();
        }
        if (theHostCheckScheduler != null) {
            theHostCheckScheduler.shutdown();
        }
        if (theFilterScheduler != null) {
            theFilterScheduler.shutdown();
        }
        if (theDissDownloadScheduler != null) {
            theDissDownloadScheduler.shutdown();
        }
        if (theAcqDownloadScheduler != null) {
            theAcqDownloadScheduler.shutdown();
        }
        if (theReplicateScheduler != null) {
            theReplicateScheduler.shutdown();
        }
        if (theBackupScheduler != null) {
            theBackupScheduler.shutdown();
        }
        if (theProxyScheduler != null) {
            theProxyScheduler.shutdown();
        }
        if (scheduleCloneId != null) {
            try {
                getMBeanCenter().removeNotifications(scheduleCloneId);
            } catch (final InstanceNotFoundException e) {
                _log.warn("Removing notifications", e);
            }
        }
        super.shutdown();
    }

    /**
     * Check if the DataTransfer associated with this event is worth being sent to the monitoring interface.
     *
     * @param event
     *            the event
     *
     * @return true, if successful
     */
    private static boolean _isValidDataTransferEvent(final DataTransferEvent event) {
        final var transfer = event.getDataTransfer();
        // If the DataTransfer is in STDBY then don't send it!
        if (StatusFactory.HOLD.equals(transfer.getStatusCode())) {
            return false;
        }
        // Is there an identity to the DataTransfer?
        if (transfer.getIdentity() == null) {
            _log.warn("Cannot handle DataTransfer with no identity: " + transfer);
            return false;
        }
        final var dataFile = transfer.getDataFile();
        // Is there any DataFile defined?
        if (dataFile == null) {
            _log.warn("Cannot handle DataTransfer with no DataFile specified: " + transfer);
            return false;
        }
        // Only send the notification if the Destination is monitored and is a
        // dissemination destination!
        final var destination = transfer.getDestination();
        if (destination != null
                && (!destination.getMonitor() || !DestinationOption.isDissemination(destination.getType()))) {
            return false;
        }
        return dataFile.getMetaType() != null && dataFile.getMetaStream() != null && dataFile.getTimeStep() != -1
                && dataFile.getMetaTime() != null && dataFile.getMonitoringValue() != null
                && transfer.getMonitoringValue() != null;
    }

    /**
     * Handle.
     *
     * @param event
     *            the event
     */
    @Override
    public void handle(final PluginEvent<?> event) {
        if (event instanceof final DataTransferEvent dataTransferEvent
                && !_isValidDataTransferEvent(dataTransferEvent)) {
            return;
        }
        if (theNotificationRepository != null) {
            theNotificationRepository.put(event); // Send it asynchronously!
        }
    }

    /**
     * Handle.
     *
     * @param events
     *            the events
     */
    @Override
    public void handle(final PluginEvent<?>[] events) {
        // If the NotificationRepository is not activated then we don't do
        // notifications at all!
        if (theNotificationRepository != null) {
            // Send it synchronously (we don't use the repository for the array
            // of events)!
            try {
                updatePluginEvents(events);
            } catch (final Throwable t) {
                _log.warn("Handling events", t);
            }
        }
    }

    /**
     * Update plugin events.
     *
     * Process the events in the list only if the specified limit is reached. This allow triggering events in bulk
     * rather than one after the other. Once the events are processed the list is emptied.
     *
     * @param events
     *            the events
     * @param limit
     *            the limit
     *
     * @throws Exception
     *             the exception
     */
    private void _updatePluginEvents(final List<PluginEvent<?>> events, final int limit) throws Exception {
        if (events.size() > limit) {
            updatePluginEvents(events.toArray(new PluginEvent[0]));
            events.clear();
        }
    }

    /**
     * Submit the initial data transfer events to the handler specified by its target name.
     *
     * @param target
     *            the target
     *
     * @return the initial data transfer events
     */
    public void getInitialDataTransferEvents(final String target) {
        final var current = System.currentTimeMillis();
        var i = 0;
        try (var it = getECpdsBase().getInitialDataTransferEventsIterator(new Date(current - 30 * Timer.ONE_DAY),
                new Date(current))) {
            final List<PluginEvent<?>> events = new ArrayList<>();
            while (it.hasNext()) {
                final var event = new DataTransferEvent(it.next());
                event.setSource("MasterServer.getInitialDataTransferEvents");
                event.setTarget(target);
                events.add(event);
                _updatePluginEvents(events, 100);
                i++;
            }
            _updatePluginEvents(events, 0);
        } catch (final Throwable e) {
            _log.warn("DataTransferEvents cache not initialized for " + target, e);
        }
        _log.debug("DataTransferEvent(s) notified with " + i + " element(s)");
    }

    /**
     * Submit the initial product status events to the handler specified by its target name and optionally stream and/or
     * time.
     *
     * @param target
     *            the target
     * @param stream
     *            the stream
     * @param time
     *            the time
     *
     * @return the initial product status events
     */
    public void getInitialProductStatusEvents(final String target, final String stream, final String time) {
        var i = 0;
        try (var it = getECpdsBase().getInitialProductStatusEventsIterator()) {
            final List<PluginEvent<?>> events = new ArrayList<>();
            while (it.hasNext()) {
                final var ps = it.next();
                if ((stream == null || stream.equals(ps.getStream())) && (time == null || time.equals(ps.getTime()))) {
                    final var event = new ProductStatusEvent(ps);
                    event.setTarget(target);
                    events.add(event);
                    _updatePluginEvents(events, 100);
                    i++;
                }
            }
            _updatePluginEvents(events, 0);
        } catch (final Throwable e) {
            _log.warn("ProductStatusEvents cache not initialized for " + target, e);
        }
        _log.debug("ProductStatusEvent(s) notified with " + i + " element(s)");
    }

    /**
     * Submit the initial change host events to the handler specified by its target name.
     *
     * @param target
     *            the target
     *
     * @return the initial change host events
     */
    public void getInitialChangeHostEvents(final String target) {
        var i = 0;
        try {
            final List<PluginEvent<?>> events = new ArrayList<>();
            for (final Destination destination : theTransferScheduler.getDestinations()) {
                final var event = new ChangeHostEvent(destination);
                event.setTarget(target);
                events.add(event);
                _updatePluginEvents(events, 100);
                i++;
            }
            _updatePluginEvents(events, 0);
        } catch (final Throwable e) {
            _log.warn("ChangeHostEvent cache not initialized for " + target, e);
        }
        _log.debug("ChangeHostEvent(s) notified with " + i + " element(s)");
    }

    /**
     * Gets the root.
     *
     * @return the root
     */
    @Override
    public String getRoot() {
        return Cnf.at("Login", "root", Cnf.at("Login", "hostName"));
    }

    /**
     * Gets the service.
     *
     * @return the service
     */
    @Override
    public String getService() {
        return "MasterServer";
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return Cnf.at("Login", "password");
    }

    /**
     * The Class ECUserMBean.
     */
    private final class ECUserMBean implements MBeanService {

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("ECUsers".equals(attributeName)) {
                    return getDataBase(ECpdsBase.class).getECUserCount();
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            throw new AttributeNotFoundException(
                    "Cannot find " + attributeName + " attribute in " + this.getClass().getName());
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo(this.getClass().getName(),
                    "The uid authenticate the local Member State user. This MBean provides "
                            + "operations to monitor such information.",
                    new MBeanAttributeInfo[] { new MBeanAttributeInfo("ECUsers", "java.lang.Integer",
                            "ECUsers: number of ECUsers in the database.", true, false, false) },
                    new MBeanConstructorInfo[0],
                    new MBeanOperationInfo[] {
                            new MBeanOperationInfo("importRegisteredUser",
                                    "importRegisteredUser(ecuser): import a ECMWF registered user in the database",
                                    new MBeanParameterInfo[] { new MBeanParameterInfo("ecuser", "java.lang.String",
                                            "ECMWF user identifier") },
                                    "java.lang.Boolean", MBeanOperationInfo.ACTION),
                            new MBeanOperationInfo("getWebUser",
                                    "getWebUser(ecuser,password,root): import a Web user in the database",
                                    new MBeanParameterInfo[] {
                                            new MBeanParameterInfo("ecuser", "java.lang.String",
                                                    "ECMWF user identifier"),
                                            new MBeanParameterInfo("password", "java.lang.String",
                                                    "Associated password"),
                                            new MBeanParameterInfo("root", "java.lang.String", "Associated root") },
                                    "ecmwf.common.database.WebUser", MBeanOperationInfo.ACTION) },
                    new MBeanNotificationInfo[0]);
        }

        /**
         * Invoke.
         *
         * @param operationName
         *            the operation name
         * @param params
         *            the params
         * @param signature
         *            the signature
         *
         * @return the object
         *
         * @throws NoSuchMethodException
         *             the no such method exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object invoke(final String operationName, final Object[] params, final String[] signature)
                throws NoSuchMethodException, MBeanException {
            try {
                if ("importRegisteredUser".equals(operationName) && signature.length == 1
                        && "java.lang.String".equals(signature[0])) {
                    return isRegistredUser((String) params[0]);
                }
                if ("getWebUser".equals(operationName) && signature.length == 3
                        && "java.lang.String".equals(signature[0]) && "java.lang.String".equals(signature[1])
                        && "java.lang.String".equals(signature[2])) {
                    return getWebUser((String) params[0], (String) params[1], (String) params[2]);
                }
            } catch (final Exception e) {
                _log.warn("Invoking the {} MBean method", operationName, e);
                throw new MBeanException(e);
            }
            throw new NoSuchMethodException(operationName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            return false;
        }
    }

    /**
     * The Class TransferRepository.
     */
    private final class TransferRepository extends StorageRepository<DataTransfer> {

        /**
         * Instantiates a new transfer repository.
         *
         * @param name
         *            the name
         */
        private TransferRepository(final String name) {
            super(name, Cnf.at("StorageRepository", "transferSize", 5),
                    Cnf.at("StorageRepository", "transferDelay", 250));
            setComparator(new TransferComparator(false));
        }

        /**
         * Gets the key.
         *
         * @param transfer
         *            the transfer
         *
         * @return the key
         */
        @Override
        public String getKey(final DataTransfer transfer) {
            return Format.formatLong(transfer.getId(), 10, true);
        }

        /**
         * Gets the data transfer.
         *
         * @param id
         *            the id
         *
         * @return the data transfer
         */
        private DataTransfer getDataTransfer(final long id) {
            return getValue(Format.formatLong(id, 10, true));
        }

        /**
         * Gets the status.
         *
         * @param transfer
         *            the transfer
         *
         * @return the status
         */
        @Override
        public String getStatus(final DataTransfer transfer) {
            final var fileSize = transfer.getDataFile().getSize();
            final var duration = transfer.getDuration();
            final var byteSent = transfer.getSent();
            final var moverName = transfer.getMoverName();
            return transfer.getStatusCode() + "("
                    + (byteSent >= fileSize ? Format.formatRate(fileSize, duration)
                            : Format.formatPercentage(fileSize, byteSent) + " of " + Format.formatSize(fileSize))
                    + " for " + transfer.getDestinationName() + (isNotEmpty(moverName) ? " on " + moverName : "") + "/"
                    + transfer.getHostName() + ")";
        }

        /**
         * Gets the status.
         *
         * @param destinationName
         *            the Destination name
         *
         * @return the status
         */
        public String getStatus(final String destinationName) {
            final var result = new StringBuilder();
            for (final DataTransfer transfer : getList()) {
                if (transfer.getDestinationName().equals(destinationName)) {
                    result.append(result.isEmpty() ? "" : " ").append(getStatus(transfer).replace(' ', '_'));
                }
            }
            return result.toString();
        }

        /**
         * Gets the data transfers.
         *
         * @param destinationName
         *            the destination name
         *
         * @return the data transfers
         */
        public DataTransfer[] getDataTransfers(final String destinationName) {
            final List<DataTransfer> array = new ArrayList<>();
            for (final DataTransfer transfer : getList()) {
                if (destinationName.equals(transfer.getDestinationName())) {
                    array.add(transfer);
                }
            }
            return array.toArray(new DataTransfer[array.size()]);
        }

        /**
         * Expired.
         *
         * @param transfer
         *            the transfer
         *
         * @return true, if successful
         */
        @Override
        public boolean expired(final DataTransfer transfer) {
            final var code = transfer.getStatusCode();
            return transfer.getDeleted() || !StatusFactory.EXEC.equals(code) && !StatusFactory.WAIT.equals(code);
        }

        /**
         * Update.
         *
         * @param transfer
         *            the transfer
         *
         * @throws Exception
         *             the exception
         */
        @Override
        public void update(final DataTransfer transfer) throws Exception {
            final var size = transfer.getDataFile().getSize();
            final var filterSize = transfer.getDataFile().getFilterSize();
            final var sent = transfer.getSent();
            final var code = transfer.getStatusCode();
            final var comment = transfer.getComment();
            if (StatusFactory.INIT.equals(code) || StatusFactory.SCHE.equals(code) || StatusFactory.FETC.equals(code)) {
                return;
            }
            _log.debug("Update DataTransfer-{}={} ({})", transfer.getId(), code, StatusFactory.DONE.equals(code)
                    ? Format.formatRate(sent, transfer.getDuration()) : isNotEmpty(comment) ? comment : "no-comment");
            if (size != sent && filterSize != sent && StatusFactory.DONE.equals(code)) {
                _log.warn("Incompatible sent/size for DataTransfer-{} (sent={},size={},filterSize={})",
                        transfer.getId(), sent, size, filterSize);
            }
            final var event = new DataTransferEvent(transfer);
            event.setSource("MasterServer.TransferRepository.update");
            handle(event);
            getDataBase().tryUpdate(transfer);
            if (!transfer.getDeleted() && !StatusFactory.RETR.equals(code)) {
                addTransferHistory(transfer);
            }
        }
    }

    /**
     * The Class HistoryRepository.
     */
    private final class HistoryRepository extends StorageRepository<TransferHistory> {

        /**
         * Instantiates a new history repository.
         *
         * @param name
         *            the name
         */
        private HistoryRepository(final String name) {
            super(name, Cnf.at("StorageRepository", "historySize", 5),
                    Cnf.at("StorageRepository", "historyDelay", 30 * Timer.ONE_SECOND));
            setMaxAuthorisedSize(Cnf.at("StorageRepository", "historyMaxAuthorisedSize", 0));
        }

        /**
         * Gets the status.
         *
         * @param history
         *            the history
         *
         * @return the status
         */
        @Override
        public String getStatus(final TransferHistory history) {
            final var comment = history.getComment();
            return history.getStatusCode() + (comment != null ? "(" + comment + ") " : " ");
        }

        /**
         * Update.
         *
         * @param history
         *            the history
         *
         * @throws Exception
         *             the exception
         */
        @Override
        public void update(final TransferHistory history) throws Exception {
            final var transfer = history.getDataTransfer();
            final var host = history.getHost();
            final var code = transfer.getStatusCode();
            final var sent = transfer.getSent();
            final var completed = StatusFactory.DONE.equals(code);
            if ((completed || StatusFactory.STOP.equals(code) || StatusFactory.FAIL.equals(code)
                    || StatusFactory.RETR.equals(code) || StatusFactory.INTR.equals(code)) && transfer.getSent() > 0
                    && host != null) {
                _updateHostStats(host, 1, sent, transfer.getDuration(), completed,
                        new Timestamp(System.currentTimeMillis()));
            }
            final var file = transfer.getDataFile();
            final var base = getDataBase();
            if (completed && host != null) {
                // And now let's insert a new UploadHistory!
                try {
                    final var upload = new UploadHistory();
                    upload.setDataTransfer(transfer);
                    upload.setDataTransferId(transfer.getId());
                    upload.setDestination(transfer.getDestinationName());
                    upload.setFileName(transfer.getTarget());
                    upload.setFileSize(file.getSize());
                    upload.setMetaStream(file.getMetaStream());
                    upload.setMetaType(file.getMetaType());
                    upload.setMetaTime(file.getMetaTime());
                    upload.setPriority(transfer.getPriority());
                    upload.setQueueTime(transfer.getQueueTime());
                    upload.setRequeueCount(transfer.getRequeueHistory());
                    upload.setScheduledTime(transfer.getScheduledTime());
                    upload.setStartTime(transfer.getStartTime());
                    upload.setPutTime(transfer.getPutTime());
                    upload.setDuration(transfer.getDuration());
                    upload.setSent(sent);
                    upload.setStatusCode(code);
                    upload.setRetrievalTime(file.getGetTime());
                    upload.setTimeBase(file.getTimeBase());
                    upload.setTimeStep(file.getTimeStep());
                    upload.setFinishTime(transfer.getFinishTime());
                    upload.setTransferModule(host.getTransferMethod().getECtransModuleName());
                    upload.setTransferServer(transfer.getTransferServerName());
                    upload.setNetworkCode(host.getNetworkCode());
                    upload.setHostAddress(host.getHost());
                    base.insert(upload, true);
                    final var socketStatistics = nullToNone(transfer.getStatistics());
                    if (transfer.getRecordUPH() && _splunk.isInfoEnabled()) {
                        // For the accounting!
                        final var destination = transfer.getDestination();
                        final var proxyHost = transfer.getProxyHost();
                        final var retryTime = transfer.getRetryTime();
                        final var startTime = transfer.getStartTime();
                        _splunk.info(
                                "UPH;{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{}",
                                "Monitored=" + destination.getMonitor(), "DataTransferId=" + upload.getDataTransferId(),
                                "DestinationName=" + destination.getName(),
                                "DestinationType=" + DestinationOption.getLabel(destination.getType()),
                                "FileName=" + upload.getFileName(), "FileSize=" + upload.getFileSize(),
                                "Identity=" + transfer.getIdentity(),
                                "MetaStream=" + nullToNone(upload.getMetaStream()),
                                "MetaType=" + nullToNone(upload.getMetaType()), "MetaTime=" + upload.getMetaTime(),
                                "Priority=" + upload.getPriority(), "QueueTime=" + upload.getQueueTime(),
                                "RequeueCount=" + upload.getRequeueCount(),
                                "ScheduledTime=" + upload.getScheduledTime(),
                                "StartTime=" + (retryTime != null ? retryTime : startTime),
                                "PutTime=" + upload.getPutTime(), "Duration=" + upload.getDuration(),
                                "DurationOnClose=" + transfer.getDurationOnClose(), "BytesSent=" + upload.getSent(),
                                "StatusCode=" + upload.getStatusCode(),
                                "RetrievalTime=" + nullToNone(upload.getRetrievalTime()),
                                "TimeBase=" + upload.getTimeBase(), "TimeStep=" + upload.getTimeStep(),
                                "FinishTime=" + upload.getFinishTime(),
                                "TransferProtocol=" + upload.getTransferModule(),
                                "TransferServer=" + upload.getTransferServer(),
                                "NetworkCode=" + HostOption.getLabel(upload.getNetworkCode()),
                                "HostAddress=" + upload.getHostAddress(), "PreSchedule=" + transfer.getAsap(),
                                "ProxyHost=" + nullToNone(proxyHost != null ? proxyHost.getHost() : null),
                                "Proxied=" + (transfer.getProxyName() != null), "SocketStatistics=" + socketStatistics,
                                "Compressed=" + nullToNone(transfer.getCompressed()),
                                "CompressedOnTheFly=" + transfer.getCompressedOnTheFly());
                    }
                    _log.debug("UploadHistory created for DataTransfer {} (statistics: {})", transfer.getId(),
                            socketStatistics);
                } catch (final Throwable t) {
                    _log.error("Creating UploadHistory", t);
                }
            }
            // Let's insert a new transfer history!
            try {
                base.insert(history, true);
            } catch (final Exception e) {
                _log.error("History NOT recorded: {}", history, e);
            }
            // Let's see if we should notify a remote Master?
            if (completed) {
                final var setup = DESTINATION_SCHEDULER.getECtransSetup(transfer.getDestination().getData());
                final var masterToNotifyOnDone = setup.getString(DESTINATION_SCHEDULER_MASTER_TO_NOTIFY_ON_DONE);
                if (masterToNotifyOnDone != null) {
                    _log.error("Notifying remote Master: " + masterToNotifyOnDone);
                    try {
                        updateRemoteTransferStatus(masterToNotifyOnDone, file.getStandby(),
                                transfer.getDestinationName(), transfer.getTarget(), transfer.getUniqueKey(),
                                StatusFactory.STOP);
                    } catch (final Exception e) {
                        _log.error("Remote Master NOT notified: " + masterToNotifyOnDone, e);
                    }
                }
            }
        }
    }

    /**
     * The Class NotificationRepository.
     */
    private final class EventRepository extends StorageRepository<DataTransfer> {

        /**
         * Instantiates a new event repository.
         *
         * @param name
         *            the name
         */
        private EventRepository(final String name) {
            super(name, Cnf.at("StorageRepository", "eventSize", 5),
                    Cnf.at("StorageRepository", "eventDelay", 30 * Timer.ONE_SECOND));
            setMaxAuthorisedSize(Cnf.at("StorageRepository", "eventMaxAuthorisedSize", 0));
        }

        /**
         * Gets the status.
         *
         * @param transfer
         *            the transfer
         *
         * @return the status
         */
        @Override
        public String getStatus(final DataTransfer transfer) {
            return transfer.getStatusCode();
        }

        /**
         * Update.
         *
         * @param transfer
         *            the transfer
         *
         * @throws Exception
         *             the exception
         */
        @Override
        public void update(final DataTransfer transfer) throws Exception {
            final var code = transfer.getStatusCode();
            final var selectedConnectOptions = new StringBuilder();
            final var currentConnectOptions = transfer.getConnectOptions();
            final var standby = StatusFactory.HOLD.equals(code);
            if (standby && isEmpty(currentConnectOptions)) {
                // We have to build the connect options as the file was not transmitted and
                // there was no connect options configured!
                selectedConnectOptions.append(TransferManagement.getConnectOptions(transfer, transfer.getTarget(),
                        transfer.getMoverName(), new ArrayList<>()));
            } else {
                // We use the connect options configured during the push!
                selectedConnectOptions.append(currentConnectOptions);
            }
            if (!selectedConnectOptions.isEmpty()) {
                selectedConnectOptions.append(";standby=" + standby);
                // Insert into the Publication table, to be processed by the event scheduler for
                // the global notification system!
                insertPublication(transfer, selectedConnectOptions);
                final var destination = transfer.getDestination();
                if (DESTINATION_MQTT.getECtransSetup(destination.getData())
                        .getBoolean(ECtransOptions.DESTINATION_MQTT_PUBLISH)) {
                    // Insert into the Publication table, to be processed by the event scheduler for
                    // the MQTT notification system!
                    insertPublication(transfer, selectedConnectOptions.append(MQTT_TOKEN));
                }
            } else {
                _log.error("Publication cancelled (no options found)");
            }
        }

        /**
         * Insert publication.
         *
         * @param transfer
         *            the transfer
         * @param selectedConnectOptions
         *            the selected connect options
         */
        // Utility method to insert a new publication entry in the database
        private void insertPublication(final DataTransfer transfer, final StringBuilder selectedConnectOptions) {
            final var publication = new Publication();
            publication.setDataTransferId(transfer.getId());
            publication.setOptions(selectedConnectOptions.toString());
            publication.setScheduledTime(transfer.getScheduledTime());
            publication.setProcessedTime(null);
            publication.setDone(false);
            try {
                getDataBase().insert(publication, true);
            } catch (final Throwable t) {
                _log.error("Publication NOT recorded: {}", publication, t);
            }
        }
    }

    /**
     * The Class EventScheduler.
     */
    public final class EventScheduler extends MBeanScheduler {

        /** The _time out event thread. */
        private long _timeOutEventThread = Cnf.durationAt("Scheduler", "timeOutEventThread", -1);

        /** The _max event threads. */
        private int _maxEventThreads = Cnf.at("Scheduler", "maxEventThreads", 200);

        /** The _event threads. */
        private final Map<Long, EventThread> _eventThreads = new ConcurrentHashMap<>();

        /** The _to remove. */
        private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

        /** The _current key. */
        private Long _currentKey = null;

        /**
         * Instantiates a new event scheduler.
         *
         * @param name
         *            the name
         */
        public EventScheduler(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "eventScheduler", 5 * Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "eventSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "eventSchedulerTimeRanges"));
        }

        /**
         * Gets the event threads count.
         *
         * @return the event threads count
         */
        public int getEventThreadsCount() {
            return _eventThreads.size() - _toRemove.size();
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return "Publication=" + (_currentKey == null ? "[none]" : _currentKey) + ",eventThreads="
                    + _eventThreads.size();
        }

        /**
         * Gets the key.
         *
         * @param publication
         *            the publication
         *
         * @return the string
         */
        private long _getKey(final Publication publication) {
            return publication.getId();
        }

        /**
         * Gets the thread list.
         *
         * @return the thread list
         */
        public String getThreadList() {
            final var status = new StringBuilder();
            final var currentTime = System.currentTimeMillis();
            for (final EventThread thread : _eventThreads.values()) {
                final Publication publication;
                final long key;
                if (thread != null && !_toRemove.contains(key = _getKey(publication = thread.publication))) {
                    status.append(("Publication " + key + " for DataTransfer " + publication.getDataTransferId() + " ("
                            + Format.formatDuration(thread.time, currentTime) + ")").replace(' ', '_').replace('-',
                                    '_'))
                            .append(" ");
                }
            }
            return status.toString().trim();
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "The EventScheduler is used to manage the event threads.",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("ThreadSize", "int", "ThreadSize: number of event thread(s).", true,
                                    false, false),
                            new MBeanAttributeInfo("MaximumEventThreads", "int",
                                    "MaximumEventThreads: maximum number of parallel events.", true, true, false),
                            new MBeanAttributeInfo("TimeOutEventThreads", "long",
                                    "TimeOutEventThreads: timeout for each event thread (-1 to deactivate).", true,
                                    true, false),
                            new MBeanAttributeInfo("ThreadList", "int", "ThreadList: list of event thread(s).", true,
                                    false, false) },
                    new MBeanOperationInfo[] { new MBeanOperationInfo("reloadEventScript",
                            "reloadEventScript(): reload event script content", null, "void",
                            MBeanOperationInfo.ACTION) });
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("ThreadSize".equals(attributeName)) {
                    return _eventThreads.size();
                }
                if ("ThreadList".equals(attributeName)) {
                    return getThreadList();
                }
                if ("MaximumEventThreads".equals(attributeName)) {
                    return _maxEventThreads;
                }
                if ("TimeOutEventThreads".equals(attributeName)) {
                    return _timeOutEventThread;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("MaximumEventThreads".equals(name)) {
                _maxEventThreads = (Integer) value;
                return true;
            }
            if ("TimeOutEventThreads".equals(name)) {
                _timeOutEventThread = (Long) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Invoke.
         *
         * @param operationName
         *            the operation name
         * @param params
         *            the params
         * @param signature
         *            the signature
         *
         * @return the object
         *
         * @throws NoSuchMethodException
         *             the no such method exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object invoke(final String operationName, final Object[] params, final String[] signature)
                throws NoSuchMethodException, MBeanException {
            try {
                if ("reloadEventScript".equals(operationName) && signature.length == 0) {
                    eventScriptContent.replace(0, eventScriptContent.length(),
                            Cnf.fileContentAt("Scheduler", "eventScriptFile", ""));
                    return true;
                }
            } catch (final Exception e) {
                _log.warn("Invoking the {} MBean method", operationName, e);
                throw new MBeanException(e);
            }
            throw new NoSuchMethodException(operationName);
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final var start = System.currentTimeMillis();
            var processed = 0;
            try (var it = getECpdsBase().getPublicationIterator(2 * _maxEventThreads)) {
                while (isRunning() && it.hasNext()) {
                    try {
                        final var publication = it.next();
                        if (publication.getDone()) {
                            _log.warn("Publication-{} already processed", publication.getId());
                            continue;
                        }
                        if (getEventThreadsCount() >= _maxEventThreads) {
                            break;
                        }
                        _currentKey = _getKey(publication);
                        synchronized (_eventThreads) {
                            if (!_eventThreads.containsKey(_currentKey)) {
                                final var thread = new EventThread(publication);
                                thread.setThreadNameAndCookie(null, null, "Publication-" + _currentKey, null);
                                thread.execute(true);
                                _eventThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    } catch (final Throwable t) {
                        _log.warn("nextStep", t);
                    }
                }
                synchronized (_toRemove) {
                    for (final long key : _toRemove) {
                        _eventThreads.remove(key);
                    }
                    _toRemove.clear();
                }
                // Check the expired EventThreads
                if (_timeOutEventThread > 0) {
                    final var currentTime = System.currentTimeMillis();
                    for (final EventThread thread : _eventThreads.values()) {
                        if (thread != null && System.currentTimeMillis() - thread.time > _timeOutEventThread) {
                            if (_log.isWarnEnabled()) {
                                _log.warn("EventThread Publication-{} expired({})", _getKey(thread.publication),
                                        Format.formatDuration(thread.time, currentTime));
                            }
                            thread.interrupt();
                        }
                    }
                }
                final var duration = System.currentTimeMillis() - start;
                if (_log.isDebugEnabled() && duration > 60000) {
                    _log.debug("Step completed in {}: {} Publication(s) processed for notification",
                            Format.formatDuration(duration), processed);
                }
            } catch (final Throwable t) {
                _log.warn("Error in scheduler", t);
            }
            // If there are still files to be processed then there is no reason
            // to wait!
            return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
        }

        /**
         * The Class EventThread.
         */
        private final class EventThread extends ConfigurableRunnable {

            /** The time. */
            private final long time = System.currentTimeMillis();

            /** The publication. */
            private Publication publication = null;

            /**
             * Instantiates a new event thread.
             *
             * @param publication
             *            the publication
             */
            private EventThread(final Publication publication) {
                this.publication = publication;
            }

            public void configurableRun() {
                final var key = _getKey(publication);
                try {
                    publication.setProcessedTime(new Timestamp(System.currentTimeMillis()));
                    final var selectedConnectOptions = publication.getOptions();
                    final var dataTransferId = publication.getDataTransferId();
                    _events.info("Starting processing for Publication-{} (DataTransfer-{}): {}", publication.getId(),
                            dataTransferId, selectedConnectOptions);
                    final var transfer = getECpdsBase().getDataTransferObject(dataTransferId);
                    final long remainingMilliseconds;
                    try {
                        if (transfer != null && (remainingMilliseconds = transfer.getExpiryTime().getTime()
                                - System.currentTimeMillis()) > 0) { // We found it and it is not expired!
                            if (selectedConnectOptions.endsWith(MQTT_TOKEN)) { // This is an MQTT request
                                final var destination = transfer.getDestination();
                                final var setup = DESTINATION_MQTT.getECtransSetup(replace(
                                        selectedConnectOptions.substring(0,
                                                selectedConnectOptions.length() - MQTT_TOKEN.length()),
                                        destination.getData()));
                                ScriptManager.exec(setup.getScriptLanguage(), Map.of(), setup.getScriptContent(),
                                        value -> {
                                            final var topic = getMQTTTopic(destination, setup, transfer, value);
                                            if (topic != null && !topic.isBlank()) {
                                                final var payload = setup.getString(DESTINATION_MQTT_PAYLOAD, value);
                                                if (payload != null && !payload.isBlank()) {
                                                    _log.debug("Payload detected for DataTransfer-{}", dataTransferId);
                                                    publishToMQTTBroker(topic,
                                                            setup.getInteger(DESTINATION_MQTT_QOS, value),
                                                            setup.getOptionalDuration(DESTINATION_MQTT_EXPIRY_INTERVAL,
                                                                    value)
                                                                    .orElse(Duration.ofMillis(remainingMilliseconds))
                                                                    .toMillis(),
                                                            setup.getString(DESTINATION_MQTT_CONTENT_TYPE, value),
                                                            setup.getString(DESTINATION_MQTT_CLIENT_ID, value), payload,
                                                            setup.getBoolean(DESTINATION_MQTT_RETAIN, value));
                                                } else {
                                                    _log.warn(
                                                            "No payload for DataTransfer-{}; ignoring & publication set to done -> {}",
                                                            dataTransferId, value);
                                                }
                                            } else {
                                                _log.warn(
                                                        "No topic for DataTransfer-{}; ignoring & publication set to done",
                                                        dataTransferId);
                                            }
                                            return null;
                                        });
                            } else { // This is an event.js request (e.g. Aviso)
                                ScriptManager.exec(ScriptManager.JS,
                                        replace(selectedConnectOptions, eventScriptContent.toString()));
                            }
                        } else {
                            _log.warn("DataTransfer-{} not found or expired; ignoring & publication set to done",
                                    dataTransferId);
                        }
                        publication.setDone(true);
                    } catch (final ScriptException e) {
                        _events.error("Event invocation failed ({})", e.getMessage(), e);
                    } finally {
                        getDataBase().tryUpdate(publication);
                    }
                } catch (final Throwable t) {
                    _events.warn("Running EventThread {}", key, t);
                } finally {
                    _toRemove.add(key);
                    _events.info("Publication-{}: {}", publication.getId(), publication.getDone());
                }
            }

            /**
             * Build the MQTT topic. It is either defined in the destination setup, or by default it is using the
             * destination name. If the topic ends with a '/' (the default when no topic is specified), the target name
             * is appended.
             *
             * @param destination
             *            the destination
             * @param setup
             *            the setup
             * @param transfer
             *            the transfer
             * @param value
             *            the value
             *
             * @return the MQTT topic
             */
            private static String getMQTTTopic(final Destination destination, final ECtransSetup setup,
                    final DataTransfer transfer, final Value value) {
                final var topic = setup.getOptionalString(DESTINATION_MQTT_TOPIC, value)
                        .orElse(destination.getName() + "/");
                return (topic.endsWith("/") ? topic + getTarget(destination, transfer) : topic).replaceAll("/+", "/");
            }

            /**
             * Gets the target.
             *
             * @param destination
             *            the destination
             * @param transfer
             *            the transfer
             *
             * @return the target
             */
            private static String getTarget(final Destination destination, final DataTransfer transfer) {
                final var target = transfer.getTarget();
                if (destination.getGroupByDate()) {
                    // We want to have the target in the form "date/filename"
                    return DestinationOption.formatDate(destination, transfer.getDataFile().getTimeBase().getTime())
                            + "/" + new File(target).getName();
                }
                // We want the target including all sub-directories if any
                return target;
            }

            /**
             * Replace.
             *
             * @param options
             *            the options
             * @param body
             *            the body
             *
             * @return the string
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private static String replace(final String options, final String body) throws IOException {
                final var sb = new Options(options, ";\n").replace(new StringBuilder(body));
                int index;
                while ((index = sb.indexOf("$metadata[")) != -1) {
                    final var last = sb.indexOf("]", index + 10);
                    if (last == -1) {
                        throw new IOException("Malformed metadata in notification configuration: \"" + sb + "\"");
                    }
                    if (_events.isWarnEnabled()) {
                        _events.warn("Metadata \"{}\" not found", sb.substring(index, last + 1));
                    }
                    sb.delete(index, last + 1);
                }
                return sb.toString();
            }
        }
    }

    /**
     * The Class ProxyHost used to keep information about remote ProxyHost in the ProxyHost repository.
     */
    private static final class ProxyHost {

        /** The _name. */
        final String _name;

        /** The _last update. */
        final long _lastUpdate;

        /**
         * Instantiates a new proxy host.
         *
         * @param name
         *            the name
         */
        ProxyHost(final String name) {
            _name = name;
            _lastUpdate = System.currentTimeMillis();
        }
    }

    /**
     * The Class ProxyHostRepository. Check if ProxyHost notifications are on time. If a ProxyHost is expired then all
     * the current transfers occurring on this Host are cancelled.
     */
    private final class ProxyHostRepository extends StorageRepository<ProxyHost> {

        /**
         * Instantiates a new proxy host repository.
         *
         * @param name
         *            the name
         */
        private ProxyHostRepository(final String name) {
            super(name, Cnf.at("StorageRepository", "proxyHostSize", 2),
                    Cnf.at("StorageRepository", "proxyHostDelay", 15 * Timer.ONE_SECOND));
        }

        /**
         * Gets the key.
         *
         * @param proxyHost
         *            the proxy host
         *
         * @return the key
         */
        /*
         * The key is the name of the ProxyHost.
         *
         * @see ecmwf.common.ecaccess.MBeanRepository#getKey(java.lang.Object)
         */
        @Override
        public String getKey(final ProxyHost proxyHost) {
            return proxyHost._name;
        }

        /**
         * Gets the status.
         *
         * @param proxyHost
         *            the proxy host
         *
         * @return the status
         */
        /*
         * For the display on the JMX interface.
         *
         * @see ecmwf.common.ecaccess.MBeanRepository#getStatus(java.lang.Object)
         */
        @Override
        public String getStatus(final ProxyHost proxyHost) {
            return proxyHost._name + " (" + Format.formatDuration(proxyHost._lastUpdate, System.currentTimeMillis())
                    + ")";
        }

        /**
         * Expired.
         *
         * @param proxyHost
         *            the proxy host
         *
         * @return true, if successful
         */
        /*
         * If we had no news from the ProxyHost for the past 2 minutes then we should consider it as expired. The
         * ProxyHost will do the same if it can not send any alive message to the MasterServer.
         *
         * @see ecmwf.common.ecaccess.StorageRepository#expired(java.lang.Object)
         */
        @Override
        public boolean expired(final ProxyHost proxyHost) {
            return System.currentTimeMillis() - proxyHost._lastUpdate > 2 * Timer.ONE_MINUTE;
        }

        /**
         * Update.
         *
         * @param proxyHost
         *            the proxy host
         *
         * @throws Exception
         *             the exception
         */
        /*
         * The ProxyHost is expired and it is now removed from the list.
         *
         * @see ecmwf.common.ecaccess.StorageRepository#update(java.lang.Object)
         */
        @Override
        public void update(final ProxyHost proxyHost) throws Exception {
            resetTransferServer(proxyHost._name, "ProxyHost " + proxyHost._name + " interrupted");
        }
    }

    /**
     * _select events.
     *
     * @param events
     *            the events
     * @param root
     *            the root
     *
     * @return the plugin event[]
     */
    private static PluginEvent<?>[] _selectEvents(final PluginEvent<?>[] events, final String root) {
        final List<PluginEvent<?>> array = new ArrayList<>();
        for (final PluginEvent<?> event : events) {
            final var target = event.getTarget();
            if (target == null || target.equals(root)) {
                array.add(event);
            }
        }
        return array.toArray(new PluginEvent[array.size()]);
    }

    /**
     * Update plugin events.
     *
     * @param events
     *            the events
     *
     * @throws Exception
     *             the exception
     */
    public void updatePluginEvents(final PluginEvent<?>[] events) throws Exception {
        if (notifyLocalContainer && localContainer != null) {
            localContainer.notify(events);
        }
        for (final String root : getClientRoots()) {
            if (root.matches(containersToNotify)) {
                final var client = getClientInterface(root, ClientInterface.class);
                if (client instanceof final HandlerInterface monitoring) {
                    final var selectedEvents = _selectEvents(events, root);
                    if (selectedEvents.length > 0) {
                        final var processed = selectedEvents.length + "/" + events.length;
                        try {
                            final var start = System.currentTimeMillis();
                            monitoring.handle(selectedEvents);
                            _log.debug(processed + " event(s) sent to " + root + " in "
                                    + Format.formatDuration(start, System.currentTimeMillis()));
                        } catch (final Throwable t) {
                            _log.error(processed + " event(s) lost", t);
                        }
                    }
                } else {
                    _log.warn("Container " + root + " not valid for notifications (" + client + ")");
                }
            }
        }
    }

    /**
     * The Class NotificationRepository.
     */
    private final class NotificationRepository extends StorageRepository<PluginEvent<?>> {

        /**
         * Instantiates a new notification repository.
         *
         * @param name
         *            the name
         */
        private NotificationRepository(final String name) {
            super(name, Cnf.at("StorageRepository", "notificationSize", 5),
                    Cnf.at("StorageRepository", "notificationDelay", Timer.ONE_SECOND));
            setComparator(new EventComparator());
        }

        /**
         * Update.
         *
         * @param object
         *            the object
         *
         * @throws Exception
         *             the exception
         */
        @Override
        public void update(final PluginEvent<?> object) throws Exception {
            updatePluginEvents(new PluginEvent<?>[] { object });
        }
    }

    /**
     * The Class PendingUpdateTicket.
     */
    private final class PendingUpdateTicket extends AbstractTicket {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1741226363537549612L;

        /** The _status. */
        private final String _status;

        /** The _user name. */
        private final String _userName;

        /** The _comment. */
        private final String _byAndFrom;

        /**
         * Instantiates a new pending update ticket.
         *
         * @param userName
         *            the user name
         * @param status
         *            the status
         * @param byAndFrom
         *            the comment
         */
        private PendingUpdateTicket(final String userName, final String status, final String byAndFrom) {
            _userName = userName;
            _status = status;
            _byAndFrom = byAndFrom;
        }

        /**
         * Expired.
         */
        @Override
        public void expired() {
            try {
                _updateTransferStatus(getDataTransfer(getId()), _status, _byAndFrom, true);
            } catch (final Exception e) {
                _log.debug(e);
            }
        }

        /**
         * Gets the status.
         *
         * @return the status
         */
        @Override
        public String getStatus() {
            return _status;
        }

        /**
         * Gets the status code.
         *
         * @return the status code
         */
        public String getStatusCode() {
            return _status;
        }

        /**
         * Gets the user name.
         *
         * @return the user name
         */
        public String getUserName() {
            return _userName;
        }

        /**
         * Gets the comment.
         *
         * @return the comment
         */
        public String getByAndFrom() {
            return _byAndFrom;
        }
    }

    /**
     * The Class HostCheckScheduler.
     */
    private final class HostCheckScheduler extends MBeanScheduler {

        /** The _check file name. */
        private String _checkFileName = null;

        /** The _test phrase. */
        private byte[] _testPhrase = null;

        /** The _length. */
        private long _length = 0;

        /**
         * Instantiates a new host check scheduler.
         *
         * @param name
         *            the name
         */
        private HostCheckScheduler(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "hostCheckScheduler", 5 * Timer.ONE_MINUTE));
            setJammedTimeout(Cnf.durationAt("Scheduler", "hostCheckSchedulerJammedTimeout", 30 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "hostCheckSchedulerTimeRanges"));
            _checkFileName = Cnf.at("Server", "checkFileName", "ecpds.tst");
            _setTestPhrase(Cnf.at("Server", "testPhrase", "Test transfer file"));
        }

        /**
         * Sets the test phrase.
         *
         * @param testPhrase
         *            the test phrase
         */
        private void _setTestPhrase(final String testPhrase) {
            _testPhrase = testPhrase.getBytes();
            _length = testPhrase.length();
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final var base = getECpdsBase();
            for (final Host host : base.getHostsToCheck()) {
                try {
                    check(host, false, false);
                } catch (final MasterException | DataBaseException e) {
                    _log.warn(e);
                }
                waitFor(5 * Timer.ONE_SECOND);
            }
            return NEXT_STEP_DELAY;
        }

        /**
         * Check.
         *
         * @param host
         *            the host
         * @param force
         *            the force
         * @param notify
         *            the notify
         *
         * @return true, if successful
         *
         * @throws DataBaseException
         *             the data base exception
         * @throws MasterException
         *             the master exception
         */
        public boolean check(final Host host, final boolean force, final boolean notify)
                throws DataBaseException, MasterException {
            final var currentTime = System.currentTimeMillis();
            final var group = host.getTransferGroup();
            final var hostStats = host.getHostStats();
            if (!group.getActive()) {
                throw new MasterException("TransferGroup " + group.getName() + " not active");
            }
            TransferServer server = null;
            for (final TransferServer theServer : transferServerManagement
                    .getActiveTransferServers("HostCheckScheduler", null, group, null)) {
                if (theServer.getCheck()) {
                    server = theServer;
                    break;
                }
            }
            if (server == null) {
                throw new MasterException(
                        "No TransferServer(s) available for checking in TransferGroup " + group.getName());
            }
            final var valid = hostStats.getValid();
            Timestamp checkTime = null;
            var sent = 0L;
            if (!force && (checkTime = hostStats.getCheckTime()) != null
                    && currentTime - checkTime.getTime() < host.getCheckFrequency()) {
                return true;
            }
            _log.debug("Check host " + host.getName());
            var checkFileName = host.getCheckFilename();
            checkFileName = isNotEmpty(checkFileName) ? checkFileName : _checkFileName;
            String message = null;
            try {
                sent = transfer(_testPhrase, server, host, checkFileName, 0, _testPhrase.length);
                hostStats.setValid(true);
            } catch (final MasterException e) {
                message = e.getMessage();
                if (message != null && ("Transfer server " + server.getName() + " not subscribed").equals(message)) {
                    throw e;
                }
                _log.warn("Host " + host.getName() + " warning", e);
                hostStats.setValid(false);
            } catch (final Exception e) {
                message = e.getMessage();
                _log.warn("Host " + host.getName() + " warning", e);
                hostStats.setValid(false);
            }
            _log.debug("Host " + host.getName() + " " + (hostStats.getValid() ? "validated" : "not validated") + " ("
                    + checkFileName + ")");
            _updateHostStats(host, 0, 0, 0, hostStats.getValid(), new Timestamp(currentTime));
            if (notify || !hostStats.getValid() && host.getMailOnError() && (valid || !host.getNotifyOnce())
                    || hostStats.getValid() && host.getMailOnSuccess() && (!valid || !host.getNotifyOnce())) {
                var mail = host.getUserMail();
                var data = host.getData();
                if (data != null && data.endsWith("\n")) {
                    data = data.substring(0, data.length() - 1);
                }
                mail = isNotEmpty(mail) ? mail : host.getECUserName();
                sendECpdsMessage(mail,
                        "Host check " + " for " + host.getHost() + " ("
                                + (hostStats.getValid() ? "SUCCESSFUL" : "NOT-SUCCESSFUL") + ")",
                        "Host check " + (hostStats.getValid() ? "successful" : "failure") + ": " + host.getName()
                                + "\nHost name: " + host.getHost() + "\nHost comment: " + host.getComment()
                                + "\nTransfer server: " + server.getName() + "\nTransfer group: " + group.getName()
                                + "\nTransfer method: " + host.getTransferMethodName() + "\nLogin name: "
                                + host.getLogin() + (isNotEmpty(host.getDir()) ? "\nDirectory: " + host.getDir() : "")
                                + "\nFile name: " + checkFileName + "\nByte(s) sent: " + sent + "/" + _length
                                + (!hostStats.getValid() && isNotEmpty(message) ? "\nError message: " + message : "")
                                + "\nCheck time: " + Format.formatTime(currentTime)
                                + (isNotEmpty(data) ? "\n\nData:\n===============================\n" + data
                                        + "\n===============================\n" : ""));
            }
            return hostStats.getValid();
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "This MBean provides operations to monitor and manage " + "the HostCheckScheduler",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("TestPhrase", "java.lang.String", "TestPhrase: test phrase.", true,
                                    true, false),
                            new MBeanAttributeInfo("DefaultCheckFileName", "java.lang.String",
                                    "DefaultCheckFileName: default check filename.", true, true, false) },
                    new MBeanOperationInfo[] { new MBeanOperationInfo("check",
                            "check(hostname): check if a host is available",
                            new MBeanParameterInfo[] {
                                    new MBeanParameterInfo("hostname", "java.lang.String", "Name of the host to check"),
                                    new MBeanParameterInfo("force", "java.lang.Boolean",
                                            "Check even if within its validity time period."),
                                    new MBeanParameterInfo("notify", "java.lang.Boolean",
                                            "Activate mail notification") },
                            "boolean", MBeanOperationInfo.ACTION) });
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("TestPhrase".equals(attributeName)) {
                    return new String(_testPhrase);
                }
                if ("DefaultCheckFileName".equals(attributeName)) {
                    return _checkFileName;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("TestPhrase".equals(name)) {
                _setTestPhrase(String.valueOf(value));
                return true;
            }
            if ("DefaultCheckFileName".equals(name)) {
                _checkFileName = String.valueOf(value);
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Invoke.
         *
         * @param operationName
         *            the operation name
         * @param params
         *            the params
         * @param signature
         *            the signature
         *
         * @return the object
         *
         * @throws NoSuchMethodException
         *             the no such method exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object invoke(final String operationName, final Object[] params, final String[] signature)
                throws NoSuchMethodException, MBeanException {
            try {
                if ("check".equals(operationName) && signature.length == 3 && "java.lang.String".equals(signature[0])
                        && "java.lang.Boolean".equals(signature[1]) && "java.lang.Boolean".equals(signature[2])) {
                    final var base = getECpdsBase();
                    return check(base.getHost((String) params[0]), ((Boolean) params[1]), ((Boolean) params[2]));
                }
            } catch (final Exception e) {
                _log.warn("Invoking the {} MBean method", operationName, e);
                throw new MBeanException(e);
            }
            return super.invoke(operationName, params, signature);
        }
    }

    /**
     * The Class DataTransferCheck.
     */
    private final class DataTransferCheck extends MBeanScheduler {

        /**
         * Instantiates a new data transfer check.
         *
         * @param name
         *            the name
         */
        private DataTransferCheck(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "dataTransferCheck", 5 * Timer.ONE_MINUTE));
            setJammedTimeout(Cnf.durationAt("Scheduler", "dataTransferCheckJammedTimeout", 10 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "dataTransferCheckTimeRanges"));
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final List<DataTransferEvent> transferEvents = new ArrayList<>();
            try {
                for (final DataTransfer transfer : getECpdsBase().getDataTransferNotDoneOnDate()) {
                    final var event = new DataTransferEvent(transfer);
                    if (_isValidDataTransferEvent(event)) {
                        event.setSource("DataTransferCheck");
                        transferEvents.add(event);
                    }
                }
                handle(transferEvents.toArray(new DataTransferEvent[transferEvents.size()]));
            } catch (final DataBaseException e) {
                _log.warn("nextStep", e);
            }
            if (!transferEvents.isEmpty()) {
                _log.debug("Number of DataTransfer(s) handled: " + transferEvents.size());
            }
            return NEXT_STEP_DELAY;
        }
    }

    /**
     * The Class FilterScheduler.
     */
    public final class FilterScheduler extends MBeanScheduler {

        /** The _time out filter thread. */
        private long _timeOutFilterThread = Cnf.durationAt("Scheduler", "timeOutFilterThread", -1);

        /** The _max filter threads. */
        private int _maxFilterThreads = Cnf.at("Scheduler", "maxFilterThreads", 15);

        /** The _filter threads. */
        private final Map<Long, FilterThread> _filterThreads = new ConcurrentHashMap<>();

        /** The _to remove. */
        private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

        /** The _current key. */
        private Long _currentKey = null;

        /**
         * Instantiates a new filter scheduler.
         *
         * @param name
         *            the name
         */
        public FilterScheduler(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "filterScheduler", 5 * Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "filterSchedulerJammedTimeout", 5 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "filterSchedulerTimeRanges"));
        }

        /**
         * Gets the filter threads count.
         *
         * @return the filter threads count
         */
        public int getFilterThreadsCount() {
            return _filterThreads.size() - _toRemove.size();
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",filterThreads="
                    + _filterThreads.size();
        }

        /**
         * Gets the key.
         *
         * @param file
         *            the file
         *
         * @return the string
         */
        private long _getKey(final DataFile file) {
            return file.getId();
        }

        /**
         * Gets the thread list.
         *
         * @return the thread list
         */
        public String getThreadList() {
            final var status = new StringBuilder();
            final var currentTime = System.currentTimeMillis();
            for (final FilterThread thread : _filterThreads.values()) {
                final DataFile file;
                final long key;
                if (thread != null && !_toRemove.contains(key = _getKey(file = thread._file))) {
                    status.append(("DataFile " + key + "=" + file.getFilterName() + " ("
                            + Format.formatDuration(thread._time, currentTime) + ")").replace(' ', '_').replace('-',
                                    '_'))
                            .append(" ");
                }
            }
            return status.toString().trim();
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "The FilterScheduler is used to manage the filter threads.",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("ThreadSize", "int", "ThreadSize: number of filter thread(s).", true,
                                    false, false),
                            new MBeanAttributeInfo("MaximumFilterThreads", "int",
                                    "MaximumFilterThreads: maximum number of parallel filters.", true, true, false),
                            new MBeanAttributeInfo("TimeOutFilterThreads", "long",
                                    "TimeOutFilterThreads: timeout for each filter thread (-1 to deactivate).", true,
                                    true, false),
                            new MBeanAttributeInfo("ThreadList", "int", "ThreadList: list of filter thread(s).", true,
                                    false, false) },
                    new MBeanOperationInfo[0]);
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("ThreadSize".equals(attributeName)) {
                    return _filterThreads.size();
                }
                if ("ThreadList".equals(attributeName)) {
                    return getThreadList();
                }
                if ("MaximumFilterThreads".equals(attributeName)) {
                    return _maxFilterThreads;
                }
                if ("TimeOutFilterThreads".equals(attributeName)) {
                    return _timeOutFilterThread;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("MaximumFilterThreads".equals(name)) {
                _maxFilterThreads = (Integer) value;
                return true;
            }
            if ("TimeOutFilterThreads".equals(name)) {
                _timeOutFilterThread = (Long) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final var start = System.currentTimeMillis();
            var processed = 0;
            try (var it = getECpdsBase().getDataFilesToFilterIterator(2 * _maxFilterThreads)) {
                while (isRunning() && it.hasNext()) {
                    if (getFilterThreadsCount() >= _maxFilterThreads) {
                        break;
                    }
                    try {
                        final var file = it.next();
                        if (file == null) {
                            continue;
                        }
                        synchronized (_filterThreads) {
                            if (!_filterThreads.containsKey(_currentKey = _getKey(file))) {
                                final var thread = new FilterThread(file);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + file.getId(), null);
                                thread.execute();
                                _filterThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    } catch (final Throwable t) {
                        _log.warn("nextStep", t);
                    }
                }
                synchronized (_toRemove) {
                    for (final long key : _toRemove) {
                        _filterThreads.remove(key);
                    }
                    _toRemove.clear();
                }
                // Check the expired FilterThreads
                if (_timeOutFilterThread > 0) {
                    final var currentTime = System.currentTimeMillis();
                    for (final FilterThread thread : _filterThreads.values()) {
                        if (thread != null && System.currentTimeMillis() - thread._time > _timeOutFilterThread) {
                            _log.warn("EventThread Publication-{} expired({})", _getKey(thread._file),
                                    Format.formatDuration(thread._time, currentTime));
                            thread.interrupt();
                        }
                    }
                }
                final var duration = System.currentTimeMillis() - start;
                if (_log.isDebugEnabled() && duration > 60000) {
                    _log.debug("Step completed in {}: {} DataFile(s) processed for filtering",
                            Format.formatDuration(duration), processed);
                }
            } catch (final Throwable t) {
                _log.warn("Error in scheduler", t);
            }
            // If there are still files to be processed then there is no reason
            // to wait!
            return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
        }

        /**
         * The Class FilterThread.
         */
        private final class FilterThread extends ConfigurableRunnable {

            /** The _time. */
            private final long _time = System.currentTimeMillis();

            /** The _file. */
            private DataFile _file;

            /**
             * Instantiates a new filter thread.
             *
             * @param file
             *            the file
             */
            private FilterThread(final DataFile file) {
                _file = file;
            }

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                var complete = false;
                final var filter = _file.getFilterName();
                final var key = _getKey(_file);
                try {
                    final var start = System.currentTimeMillis();
                    final var base = getDataBase(ECpdsBase.class);
                    final var fr = TransferScheduler.filter(_getTransferServers(_file), _file, false);
                    _file = base.getDataFile(_file.getId());
                    _file.setFilterTime(new Timestamp(System.currentTimeMillis()));
                    _file.setFilterName(fr.complete ? filter : null);
                    _file.setFilterSize(fr.complete ? fr.dataFile.getFilterSize() : -1);
                    final var checksum = fr.dataFile.getChecksum();
                    if (checksum != null) {
                        _log.debug("Setting Checksum to: " + checksum);
                        _file.setChecksum(checksum);
                    }
                    base.update(_file);
                    if (complete = fr.complete && !fr.transferServers.isEmpty()) {
                        // Add a new history for every DataTransfer related to
                        // this DataFile to inform about the pre-compression!
                        final var current = System.currentTimeMillis();
                        for (final DataTransfer transfer : base.getDataTransfersByDataFileId(dataCache,
                                _file.getId())) {
                            transfer.setComment(
                                    "Preprocessed with " + filter + " in " + Format.formatDuration(current - start)
                                            + " on " + Format.toList(fr.transferServers) + " ("
                                            + Format.formatSize(_file.getFilterSize()) + ")");
                            addTransferHistory(transfer);
                        }
                    }
                } catch (final Throwable t) {
                    _log.warn("Running FilterThread " + key, t);
                    complete = false;
                } finally {
                    _toRemove.add(key);
                    _log.info("DataFile " + key + " filtered: " + complete + " (" + filter + ")");
                }
            }
        }
    }

    /**
     * The Class ReplicateScheduler.
     */
    public final class ReplicateScheduler extends MBeanScheduler {

        /** The _time out replicate thread. */
        private long _timeOutReplicateThread = Cnf.durationAt("Scheduler", "timeOutReplicateThread", -1);

        /** The _max replicate threads. */
        private int _maxReplicateThreads = Cnf.at("Scheduler", "maxReplicateThreads", 200);

        /** The _max replicate threads per mover. */
        private int _maxReplicateThreadsPerMover = Cnf.at("Scheduler", "maxReplicateThreadsPerMover", 10);

        /** The _replicate threads. */
        private final Map<Long, ReplicateThread> _replicateThreads = new ConcurrentHashMap<>();

        /** The _to remove. */
        private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

        /** The _current key. */
        private Long _currentKey = null;

        /**
         * Instantiates a new replicate scheduler.
         *
         * @param name
         *            the name
         */
        public ReplicateScheduler(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "replicateScheduler", 5 * Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "replicateSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "replicateSchedulerTimeRanges"));
        }

        /**
         * Gets the replicate threads count.
         *
         * @return the replicate threads count
         */
        public int getReplicateThreadsCount() {
            return _replicateThreads.size() - _toRemove.size();
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",replicateThreads="
                    + _replicateThreads.size();
        }

        /**
         * Gets the key.
         *
         * @param transfer
         *            the transfer
         *
         * @return the string
         */
        private long _getKey(final DataTransfer transfer) {
            return transfer.getDataFileId();
        }

        /**
         * Gets the thread list.
         *
         * @return the thread list
         */
        public String getThreadList() {
            final var status = new StringBuilder();
            final var currentTime = System.currentTimeMillis();
            for (final ReplicateThread thread : _replicateThreads.values()) {
                final DataTransfer transfer;
                final long key;
                if (thread != null && !_toRemove.contains(key = _getKey(transfer = thread._transfer))) {
                    status.append((transfer.getDestinationName() + " DataFile " + key + " on " + thread.getSourceMover()
                            + " (" + Format.formatDuration(thread._time, currentTime) + ")").replace(' ', '_')
                                    .replace('-', '_'))
                            .append(" ");
                }
            }
            return status.toString().trim();
        }

        /**
         * Return the number of threads with the specified sourceMover.
         *
         * @param sourceMover
         *            the source mover
         *
         * @return the thread count
         */
        public int getThreadCount(final String sourceMover) {
            var count = 0;
            for (final ReplicateThread thread : _replicateThreads.values()) {
                if (thread != null && !_toRemove.contains(_getKey(thread._transfer))
                        && sourceMover.equals(thread.getSourceMover())) {
                    count++;
                }
            }
            return count;
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "The ReplicateScheduler is used to manage the replication threads.",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("ThreadSize", "int", "ThreadSize: number of replicate thread(s).",
                                    true, false, false),
                            new MBeanAttributeInfo("MaximumReplicateThreads", "int",
                                    "MaximumReplicateThreads: maximum number of parallel replications.", true, true,
                                    false),
                            new MBeanAttributeInfo("MaximumReplicateThreadsPerMover", "int",
                                    "MaximumReplicateThreads: maximum number of parallel replications per data mover.",
                                    true, true, false),
                            new MBeanAttributeInfo("TimeOutReplicateThreads", "long",
                                    "TimeOutReplicateThreads: timeout for each replication thread (-1 to deactivate).",
                                    true, true, false),
                            new MBeanAttributeInfo("ThreadList", "int", "ThreadList: list of replicate thread(s).",
                                    true, false, false) },
                    new MBeanOperationInfo[0]);
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("ThreadSize".equals(attributeName)) {
                    return _replicateThreads.size();
                }
                if ("ThreadList".equals(attributeName)) {
                    return getThreadList();
                }
                if ("MaximumReplicateThreads".equals(attributeName)) {
                    return _maxReplicateThreads;
                }
                if ("MaximumReplicateThreadsPerMover".equals(attributeName)) {
                    return _maxReplicateThreadsPerMover;
                }
                if ("TimeOutReplicateThreads".equals(attributeName)) {
                    return _timeOutReplicateThread;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("MaximumReplicateThreads".equals(name)) {
                _maxReplicateThreads = (Integer) value;
                return true;
            }
            if ("MaximumReplicateThreadsPerMover".equals(name)) {
                _maxReplicateThreadsPerMover = (Integer) value;
                return true;
            }
            if ("TimeOutReplicateThreads".equals(name)) {
                _timeOutReplicateThread = (Long) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final var start = System.currentTimeMillis();
            var processed = 0;
            try (var it = getECpdsBase().getDataTransfersToReplicateIterator(2 * _maxReplicateThreads)) {
                while (isRunning() && it.hasNext()) {
                    try {
                        final var transfer = it.next();
                        if (transfer.getReplicated()) {
                            _log.warn("DataTransfer " + transfer.getId() + " already replicated");
                            continue;
                        }
                        final var fromCache = getDataTransferFromCache(transfer.getId());
                        if (fromCache != null) {
                            final var code = fromCache.getStatusCode();
                            if (!StatusFactory.WAIT.equals(code) && !StatusFactory.RETR.equals(code)
                                    && !StatusFactory.HOLD.equals(code)) {
                                _log.debug("Delay Replication for DataTransfer " + transfer.getId() + ": " + code
                                        + " (in cache)");
                                continue;
                            }
                        }
                        if (getReplicateThreadsCount() >= _maxReplicateThreads) {
                            break;
                        }
                        final var sourceMover = TransferScheduler.getTransferServerUsedForRetrieval(transfer);
                        if (getThreadCount(sourceMover) >= _maxReplicateThreadsPerMover) {
                            continue;
                        }
                        synchronized (_replicateThreads) {
                            if (!_replicateThreads.containsKey(_currentKey = _getKey(transfer))) {
                                final var thread = new ReplicateThread(sourceMover, transfer);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + _currentKey, null);
                                thread.execute(true);
                                _replicateThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    } catch (final Throwable t) {
                        _log.warn("nextStep", t);
                    }
                }
                synchronized (_toRemove) {
                    for (final long key : _toRemove) {
                        _replicateThreads.remove(key);
                    }
                    _toRemove.clear();
                }
                // Check the expired ReplicateThreads
                if (_timeOutReplicateThread > 0) {
                    final var currentTime = System.currentTimeMillis();
                    for (final ReplicateThread thread : _replicateThreads.values()) {
                        if (thread != null && System.currentTimeMillis() - thread._time > _timeOutReplicateThread) {
                            _log.warn("ReplicateThread DataFile-" + _getKey(thread._transfer) + " expired ("
                                    + Format.formatDuration(thread._time, currentTime) + ")");
                            try {
                                thread.interrupt();
                            } catch (final Throwable t) {
                                _log.warn("Interrupting ReplicateThread", t);
                            }
                        }
                    }
                }
                final var duration = System.currentTimeMillis() - start;
                if (_log.isDebugEnabled() && duration > 60000) {
                    _log.debug("Step completed in {}: {} DataFile(s) processed for replication",
                            Format.formatDuration(duration), processed);
                }
            } catch (final Throwable t) {
                _log.warn("Error in scheduler", t);
            }
            // If there are still files to be processed then there is no reason
            // to wait!
            return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
        }

        /**
         * The Class ReplicateThread.
         */
        private final class ReplicateThread extends ConfigurableRunnable {

            /** The _time. */
            private final long _time = System.currentTimeMillis();

            /** The _source mover. */
            private final String _sourceMover;

            /** The _transfer. */
            private DataTransfer _transfer = null;

            /**
             * Instantiates a new replicate thread.
             *
             * @param sourceMover
             *            the source mover
             * @param transfer
             *            the transfer
             */
            private ReplicateThread(final String sourceMover, final DataTransfer transfer) {
                _sourceMover = sourceMover;
                _transfer = transfer;
            }

            /**
             * Return the data mover used for the retrieval.
             *
             * @return the source mover
             */
            public String getSourceMover() {
                return _sourceMover;
            }

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                var complete = false;
                final var key = _getKey(_transfer);
                try {
                    final var start = System.currentTimeMillis();
                    _log.info("Starting replication for DataTransfer " + _transfer.getId());
                    final var list = _getTransferServers(_transfer.getDataFile());
                    final var rr = TransferScheduler.replicate(_sourceMover, list, _transfer);
                    final var duration = System.currentTimeMillis() - start;
                    final var base = getDataBase();
                    _transfer = base.getDataTransfer(_transfer.getId());
                    _transfer.setReplicateTime(new Timestamp(System.currentTimeMillis()));
                    _transfer.setReplicated(rr.complete);
                    final var file = _transfer.getDataFile();
                    if (!file.getDownloaded()) {
                        final var downloaded = rr.dataFile.getDownloaded();
                        _log.debug("DataFile " + file.getId() + ", DataTransfer " + _transfer.getId() + " downloaded: "
                                + downloaded + " (" + _transfer.getStatusCode() + ")");
                        file.setDownloaded(downloaded);
                    }
                    base.update(_transfer);
                    final var target = Format.toList(rr.transferServers);
                    if (complete = rr.complete) {
                        // Add a new history to inform about the replication!
                        if (rr.transferServers.isEmpty()) {
                            _transfer.setComment("No replication required (only one copy stored)");
                        } else {
                            _transfer.setComment("Replication completed in " + Format.formatDuration(duration)
                                    + " from DataMover=" + _sourceMover + " to " + target);
                        }
                        addTransferHistory(_transfer);
                    }
                    final var destination = _transfer.getDestination();
                    final var dataFile = _transfer.getDataFile();
                    if (_splunk.isInfoEnabled() && !(complete && target.isEmpty()))
                        _splunk.info("CPY;{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{}",
                                "Monitored=" + destination.getMonitor(), "DataTransferId=" + _transfer.getId(),
                                "DestinationName=" + destination.getName(),
                                "DestinationType=" + DestinationOption.getLabel(destination.getType()),
                                "FileName=" + _transfer.getTarget(), "FileSize=" + dataFile.getSize(),
                                "ScheduledTime=" + _transfer.getScheduledTime(), "StartTime=" + new Timestamp(start),
                                "MetaStream=" + nullToNone(dataFile.getMetaStream()),
                                "MetaType=" + nullToNone(dataFile.getMetaType()), "MetaTime=" + dataFile.getMetaTime(),
                                "TimeBase=" + dataFile.getTimeBase(), "TimeStep=" + dataFile.getTimeStep(),
                                "Duration=" + duration, "CountryCode=" + destination.getCountryIso(),
                                "Target=" + (isEmpty(target) ? "-" : target.replaceAll("DataMover=", "")),
                                "TransferServer=" + _sourceMover, "Caller=" + nullToNone(dataFile.getCaller()),
                                "ExpiryTime=" + _transfer.getExpiryTime(), "FileSystem=" + dataFile.getFileSystem(),
                                "Status=" + complete, "Message=" + nullToNone(rr.message), "Action=replicate");
                } catch (final Throwable t) {
                    _log.warn("Running ReplicateThread " + key, t);
                } finally {
                    _toRemove.add(key);
                    _log.info("DataTransfer " + _transfer.getId() + " replicated: " + complete);
                }
            }
        }
    }

    /**
     * The Class PurgeScheduler.
     */
    public final class PurgeScheduler extends MBeanScheduler {

        /** The _time out purge thread. */
        private long _timeOutPurgeThread = Cnf.durationAt("Scheduler", "timeOutPurgeThread", -1);

        /** The _max purge threads. */
        private int _maxPurgeThreads = Cnf.at("Scheduler", "maxPurgeThreads", 50);

        /** The _purge threads. */
        private final Map<Long, PurgeThread> _purgeThreads = new ConcurrentHashMap<>();

        /** The _to remove. */
        private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

        /** The _current key. */
        private Long _currentKey = null;

        /**
         * Instantiates a new purge scheduler.
         *
         * @param name
         *            the name
         */
        public PurgeScheduler(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "purgeScheduler", 5 * Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "purgeSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "purgeSchedulerTimeRanges"));
        }

        /**
         * Gets the purge threads count.
         *
         * @return the purge threads count
         */
        public int getPurgeThreadsCount() {
            return _purgeThreads.size() - _toRemove.size();
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",purgeThreads="
                    + _purgeThreads.size();
        }

        /**
         * Gets the key.
         *
         * @param file
         *            the file
         *
         * @return the string
         */
        private long _getKey(final DataFile file) {
            return file.getId();
        }

        /**
         * Gets the thread list.
         *
         * @return the thread list
         */
        public String getThreadList() {
            final var status = new StringBuilder();
            final var currentTime = System.currentTimeMillis();
            for (final PurgeThread thread : _purgeThreads.values()) {
                final long key;
                if (thread != null && !_toRemove.contains(key = _getKey(thread._dataFile))) {
                    status.append(("DataFile " + key + " (" + Format.formatDuration(thread._time, currentTime) + ")")
                            .replace(' ', '_').replace('-', '_')).append(" ");
                }
            }
            return status.toString().trim();
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "The PurgeScheduler is used to manage the purge threads.",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("ThreadSize", "int", "ThreadSize: number of purge thread(s).", true,
                                    false, false),
                            new MBeanAttributeInfo("MaximumPurgeThreads", "int",
                                    "MaximumPurgeThreads: maximum number of parallel purges.", true, true, false),
                            new MBeanAttributeInfo("TimeOutPurgeThreads", "long",
                                    "TimeOutPurgeThreads: timeout for each purge thread (-1 to deactivate).", true,
                                    true, false),
                            new MBeanAttributeInfo("ThreadList", "int", "ThreadList: list of purge thread(s).", true,
                                    false, false) },
                    new MBeanOperationInfo[0]);
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("ThreadSize".equals(attributeName)) {
                    return _purgeThreads.size();
                }
                if ("ThreadList".equals(attributeName)) {
                    return getThreadList();
                }
                if ("MaximumPurgeThreads".equals(attributeName)) {
                    return _maxPurgeThreads;
                }
                if ("TimeOutPurgeThreads".equals(attributeName)) {
                    return _timeOutPurgeThread;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("MaximumPurgeThreads".equals(name)) {
                _maxPurgeThreads = (Integer) value;
                return true;
            }
            if ("TimeOutPurgeThreads".equals(name)) {
                _timeOutPurgeThread = (Long) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final var start = System.currentTimeMillis();
            var processed = 0;
            try (var it = getECpdsBase().getExpiredDataFilesIterator(2 * _maxPurgeThreads)) {
                while (isRunning() && it.hasNext()) {
                    try {
                        final var dataFile = it.next();
                        if (dataFile.getDeleted() && dataFile.getRemoved()) {
                            _log.warn("DataFile " + dataFile.getId() + " already purged");
                            continue;
                        }
                        if (getPurgeThreadsCount() >= _maxPurgeThreads) {
                            break;
                        }
                        synchronized (_purgeThreads) {
                            if (!_purgeThreads.containsKey(_currentKey = _getKey(dataFile))) {
                                final var thread = new PurgeThread(dataFile);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + _currentKey, null);
                                thread.execute(true);
                                _purgeThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    } catch (final Throwable t) {
                        _log.warn("nextStep", t);
                    }
                }
                synchronized (_toRemove) {
                    for (final long key : _toRemove) {
                        _purgeThreads.remove(key);
                    }
                    _toRemove.clear();
                }
                // Check the expired PurgeThreads
                if (_timeOutPurgeThread > 0) {
                    final var currentTime = System.currentTimeMillis();
                    for (final PurgeThread thread : _purgeThreads.values()) {
                        if (thread != null && System.currentTimeMillis() - thread._time > _timeOutPurgeThread) {
                            _log.warn("PurgeThread " + _getKey(thread._dataFile) + " expired ("
                                    + Format.formatDuration(thread._time, currentTime) + ")");
                            try {
                                thread.interrupt();
                            } catch (final Throwable t) {
                                _log.warn("Interrupting PurgeThread", t);
                            }
                        }
                    }
                }
                final var duration = System.currentTimeMillis() - start;
                if (_log.isDebugEnabled() && duration > 60000) {
                    _log.debug("Step completed in {}: {} DataFile(s) processed for purge",
                            Format.formatDuration(duration), processed);
                }
            } catch (final Throwable t) {
                _log.warn("Error in scheduler", t);
            }
            // If there are still files to be processed then there is no reason
            // to wait!
            return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
        }

        /**
         * The Class PurgeThread.
         */
        private final class PurgeThread extends ConfigurableRunnable {

            /** The _time. */
            private final long _time = System.currentTimeMillis();

            /** The _data file. */
            private final DataFile _dataFile;

            /**
             * Instantiates a new purge thread.
             *
             * @param dataFile
             *            the data file
             */
            private PurgeThread(final DataFile dataFile) {
                _dataFile = dataFile;
            }

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                final var start = System.currentTimeMillis();
                final var key = _getKey(_dataFile);
                var complete = false;
                try {
                    _log.info("Starting purge for DataFile " + _dataFile.getId());
                    // Purge on all the active DataMovers for the TransferGroup
                    // of the DataFile!
                    complete = purgeDataFile(_dataFile, null).complete;
                } catch (final Throwable t) {
                    _log.warn("Running PurgeThread " + key, t);
                } finally {
                    _toRemove.add(key);
                    _log.info("DataFile " + _dataFile.getId() + " purged: " + complete + " ("
                            + Format.formatDuration(System.currentTimeMillis() - start) + ")");
                }
            }
        }
    }

    /**
     * The Class BackupScheduler.
     */
    public final class BackupScheduler extends MBeanScheduler {

        /** The _time out backup thread. */
        private final long _timeOutBackupThread = Cnf.durationAt("Scheduler", "timeOutBackupThread", -1);

        /** The _max backup threads. */
        private final int _maxBackupThreads = Cnf.at("Scheduler", "maxBackupThreads", 15);

        /** The _backup threads. */
        private final Map<Long, BackupThread> _backupThreads = new ConcurrentHashMap<>();

        /** The _to remove. */
        private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

        /** The _current key. */
        private Long _currentKey = null;

        /**
         * Instantiates a new backup scheduler.
         *
         * @param name
         *            the name
         */
        public BackupScheduler(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "backupScheduler", 5 * Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "backupSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "backupSchedulerTimeRanges"));
        }

        /**
         * Gets the backup threads count.
         *
         * @return the backup threads count
         */
        public int getBackupThreadsCount() {
            return _backupThreads.size() - _toRemove.size();
        }

        /**
         * Return the number of threads with the specified host for source name.
         *
         * @param hostForBackup
         *            the host for backup
         *
         * @return the thread count
         */
        public int getThreadCount(final Host hostForBackup) {
            var count = 0;
            for (final BackupThread thread : _backupThreads.values()) {
                if (thread != null && !_toRemove.contains(_getKey(thread._transfer))
                        && hostForBackup.getName().equals(thread.getHostForBackup().getName())) {
                    count++;
                }
            }
            return count;
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",backupThreads="
                    + _backupThreads.size();
        }

        /**
         * Gets the key.
         *
         * @param transfer
         *            the transfer
         *
         * @return the string
         */
        private long _getKey(final DataTransfer transfer) {
            return transfer.getDataFileId();
        }

        /**
         * Gets the thread list.
         *
         * @return the thread list
         */
        public String getThreadList() {
            final var status = new StringBuilder();
            final var currentTime = System.currentTimeMillis();
            for (final BackupThread thread : _backupThreads.values()) {
                final DataTransfer transfer;
                final long key;
                if (thread != null && !_toRemove.contains(key = _getKey(transfer = thread._transfer))) {
                    status.append((transfer.getDestinationName() + " DataFile " + key + " ("
                            + Format.formatDuration(thread._time, currentTime) + ")").replace(' ', '_').replace('-',
                                    '_')
                            + " ");
                }
            }
            return status.toString().trim();
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "The BackupScheduler is used to manage the backup threads.",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("ThreadSize", "int", "ThreadSize: number of backup thread(s).", true,
                                    false, false),
                            new MBeanAttributeInfo("ThreadList", "int", "ThreadList: list of backup thread(s).", true,
                                    false, false) },
                    new MBeanOperationInfo[0]);
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("ThreadSize".equals(attributeName)) {
                    return _backupThreads.size();
                }
                if ("ThreadList".equals(attributeName)) {
                    return getThreadList();
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final var start = System.currentTimeMillis();
            var processed = 0;
            try (var it = getECpdsBase().getDataTransfersToBackupIterator(2 * _maxBackupThreads)) {
                while (isRunning() && it.hasNext()) {
                    try {
                        final var transfer = it.next();
                        if (transfer.getBackupHostName() != null) {
                            _log.warn("DataTransfer " + transfer.getId() + " already backup");
                            continue;
                        }
                        final var fromCache = getDataTransferFromCache(transfer.getId());
                        if (fromCache != null) {
                            final var code = fromCache.getStatusCode();
                            if (!StatusFactory.WAIT.equals(code) && !StatusFactory.RETR.equals(code)
                                    && !StatusFactory.HOLD.equals(code) && !StatusFactory.DONE.equals(code)) {
                                _log.debug("Delay Backup for DataTransfer " + transfer.getId() + ": " + code
                                        + " (in cache)");
                                continue;
                            }
                        }
                        if (getBackupThreadsCount() >= _maxBackupThreads) {
                            break;
                        }
                        final var hostForBackup = transfer.getDataFile().getTransferGroup().getHostForBackup();
                        if (getThreadCount(hostForBackup) >= hostForBackup.getMaxConnections()) {
                            continue;
                        }
                        synchronized (_backupThreads) {
                            if (!_backupThreads.containsKey(_currentKey = _getKey(transfer))) {
                                final var thread = new BackupThread(hostForBackup, transfer);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + _currentKey, null);
                                thread.execute(true);
                                _backupThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    } catch (final Throwable t) {
                        _log.warn("nextStep", t);
                    }
                }
                synchronized (_toRemove) {
                    for (final long key : _toRemove) {
                        _backupThreads.remove(key);
                    }
                    _toRemove.clear();
                }
                // Check the expired BackupThreads
                if (_timeOutBackupThread > 0) {
                    final var currentTime = System.currentTimeMillis();
                    for (final BackupThread thread : _backupThreads.values()) {
                        if (thread != null && System.currentTimeMillis() - thread._time > _timeOutBackupThread) {
                            _log.warn("BackupThread " + _getKey(thread._transfer) + " expired ("
                                    + Format.formatDuration(thread._time, currentTime) + ")");
                            try {
                                thread.interrupt();
                            } catch (final Throwable t) {
                                _log.warn("Interrupting BackupThread", t);
                            }
                        }
                    }
                }
                final var duration = System.currentTimeMillis() - start;
                if (_log.isDebugEnabled() && duration > 60000) {
                    _log.debug("Step completed in {}: {} DataFile(s) processed for backup",
                            Format.formatDuration(duration), processed);
                }
            } catch (final Throwable t) {
                _log.warn("Error in scheduler", t);
            }
            // If there are still files to be processed then there is no reason
            // to wait!
            return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
        }

        /**
         * The Class BackupThread.
         */
        private final class BackupThread extends ConfigurableRunnable {

            /** The _time. */
            private final long _time = System.currentTimeMillis();

            /** The _host for backup. */
            private final Host _hostForBackup;

            /** The _transfer. */
            private DataTransfer _transfer = null;

            /**
             * Instantiates a new backup thread.
             *
             * @param hostForBackup
             *            the host for backup
             * @param transfer
             *            the transfer
             */
            private BackupThread(final Host hostForBackup, final DataTransfer transfer) {
                _transfer = transfer;
                _hostForBackup = hostForBackup;
            }

            /**
             * Gets the host for backup.
             *
             * @return the host for backup
             */
            public Host getHostForBackup() {
                return _hostForBackup;
            }

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                final var start = System.currentTimeMillis();
                var complete = false;
                final var key = _getKey(_transfer);
                try {
                    _log.info("Starting backup for DataTransfer " + _transfer.getId());
                    final var list = _getTransferServers(_transfer.getDataFile());
                    final var rr = TransferScheduler.backup(_hostForBackup, list, _transfer);
                    final var base = getDataBase(ECpdsBase.class);
                    _transfer = base.getDataTransfer(_transfer.getId());
                    _transfer.setBackupTime(new Timestamp(System.currentTimeMillis()));
                    if (rr.hostForBackup != null) {
                        _transfer.setBackupHostName(rr.hostForBackup.getName());
                        _transfer.setBackupHost(rr.hostForBackup);
                    } else {
                        // It failed to complete!
                        _transfer.setBackupHostName(null);
                        _transfer.setBackupHost(null);
                    }
                    base.update(_transfer);
                    complete = rr.complete;
                    if (complete) {
                        // Also update all the DataTransfers with the same
                        // DataFile (this happen with Destinations with
                        // aliases)!
                        for (final DataTransfer transfer : base.getDataTransfersByDataFileId(dataCache,
                                _transfer.getDataFileId())) {
                            transfer.setBackupTime(_transfer.getBackupTime());
                            transfer.setBackupHostName(_transfer.getBackupHostName());
                            transfer.setBackupHost(_transfer.getBackupHost());
                            base.update(transfer);
                            // Add a new history to inform about the backup!
                            transfer.setComment(
                                    "Backup completed in " + Format.formatDuration(System.currentTimeMillis() - start)
                                            + " from " + rr.transferServer + " to Host=" + rr.hostForBackup.getName()
                                            + " (" + rr.hostForBackup.getNickname() + ")");
                            addTransferHistory(transfer, transfer.getBackupHost(), transfer.getStatusCode(),
                                    transfer.getComment(), false);
                        }
                    } else if (isNotEmpty(rr.message)) {
                        // Add a new history to inform about the failure!
                        for (final DataTransfer transfer : base.getDataTransfersByDataFileId(dataCache,
                                _transfer.getDataFileId())) {
                            addTransferHistory(transfer, _transfer.getBackupHost(), StatusFactory.STOP, rr.message,
                                    true);
                        }
                    }
                } catch (final Throwable t) {
                    _log.warn("Running BackupThread " + key, t);
                } finally {
                    _toRemove.add(key);
                    _log.info("DataTransfer " + _transfer.getId() + " backup: " + complete);
                }
            }
        }
    }

    /**
     * The Class ProxyScheduler.
     */
    public final class ProxyScheduler extends MBeanScheduler {

        /** The _time out proxy thread. */
        private final long _timeOutProxyThread = Cnf.durationAt("Scheduler", "timeOutProxyThread", -1);

        /** The _max proxy threads. */
        private int _maxProxyThreads = Cnf.at("Scheduler", "maxProxyThreads", 15);

        /** The _debug. */
        public boolean _debug = Cnf.at("Scheduler", "debug", false);

        /** The _proxy threads. */
        private final Map<Long, ProxyThread> _proxyThreads = new ConcurrentHashMap<>();

        /** The _to remove. */
        private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

        /** The _current key. */
        private Long _currentKey = null;

        /**
         * Instantiates a new proxy scheduler.
         *
         * @param name
         *            the name
         */
        public ProxyScheduler(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "proxyScheduler", 5 * Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "proxySchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "proxySchedulerTimeRanges"));
        }

        /**
         * Gets the proxy threads count.
         *
         * @return the proxy threads count
         */
        public int getProxyThreadsCount() {
            return _proxyThreads.size() - _toRemove.size();
        }

        /**
         * Return the number of threads with the specified host for source name.
         *
         * @param hostForBackup
         *            the host for backup
         *
         * @return the thread count
         */
        public int getThreadCount(final Host hostForBackup) {
            var count = 0;
            for (final ProxyThread thread : _proxyThreads.values()) {
                final Host currentHostForProxy;
                if (thread != null && (currentHostForProxy = thread.getHostForProxy()) != null
                        && !_toRemove.contains(_getKey(thread._transfer))
                        && hostForBackup.getName().equals(currentHostForProxy.getName())) {
                    count++;
                }
            }
            return count;
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",proxyThreads="
                    + _proxyThreads.size();
        }

        /**
         * Gets the key.
         *
         * @param transfer
         *            the transfer
         *
         * @return the string
         */
        private long _getKey(final DataTransfer transfer) {
            return transfer.getDataFileId();
        }

        /**
         * Gets the thread list.
         *
         * @return the thread list
         */
        public String getThreadList() {
            final var status = new StringBuilder();
            final var currentTime = System.currentTimeMillis();
            for (final ProxyThread thread : _proxyThreads.values()) {
                final DataTransfer transfer;
                final long key;
                if (thread != null && !_toRemove.contains(key = _getKey(transfer = thread._transfer))) {
                    status.append((transfer.getDestinationName() + " DataFile " + key + " ("
                            + Format.formatDuration(thread._time, currentTime) + ")").replace(' ', '_').replace('-',
                                    '_'))
                            .append(" ");
                }
            }
            return status.toString().trim();
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "The ProxyScheduler is used to manage the proxy threads.",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("Debug", "boolean", "Debug: display more debug logs.", true, true,
                                    false),
                            new MBeanAttributeInfo("MaximumProxyThreads", "int",
                                    "MaximumProxyThreads: maximum number of parallel transfers.", true, true, false),
                            new MBeanAttributeInfo("ThreadSize", "int", "ThreadSize: number of proxy thread(s).", true,
                                    false, false),
                            new MBeanAttributeInfo("ThreadList", "int", "ThreadList: list of proxy thread(s).", true,
                                    false, false) },
                    new MBeanOperationInfo[0]);
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("Debug".equals(attributeName)) {
                    return _debug;
                }
                if ("MaximumProxyThreads".equals(attributeName)) {
                    return _maxProxyThreads;
                }
                if ("ThreadSize".equals(attributeName)) {
                    return _proxyThreads.size();
                }
                if ("ThreadList".equals(attributeName)) {
                    return getThreadList();
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("Debug".equals(name)) {
                _debug = (Boolean) value;
                return true;
            }
            if ("MaximumProxyThreads".equals(name)) {
                _maxProxyThreads = (Integer) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Gets the hosts by destination id and type.
         *
         * @param destId
         *            the dest id
         * @param type
         *            the type
         *
         * @return the hosts by destination id and type
         *
         * @throws DataBaseException
         *             the data base exception
         */
        private Collection<Host> getHostsByDestinationIdAndType(final String destId, final String type)
                throws DataBaseException {
            final List<Host> result = new ArrayList<>();
            for (final Host host : getECpdsBase().getHostsByDestinationId(destId)) {
                if (host.getType().equals(type)) {
                    result.add(host);
                }
            }
            return result;
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final var start = System.currentTimeMillis();
            var processed = 0;
            try (var it = getECpdsBase().getDataTransfersToProxyIterator(2 * _maxProxyThreads)) {
                while (isRunning() && it.hasNext()) {
                    try {
                        final var transfer = it.next();
                        if (transfer.getProxyHostName() != null) {
                            _log.warn("DataTransfer " + transfer.getId() + " already proxy");
                            continue;
                        }
                        final var fromCache = getDataTransferFromCache(transfer.getId());
                        if (fromCache != null) {
                            final var code = fromCache.getStatusCode();
                            if (!StatusFactory.WAIT.equals(code) && !StatusFactory.RETR.equals(code)
                                    && !StatusFactory.HOLD.equals(code) && !StatusFactory.DONE.equals(code)) {
                                _log.debug("Delay Proxy for DataTransfer " + transfer.getId() + ": " + code
                                        + " (in cache)");
                                continue;
                            }
                        }
                        final var threadsCount = getProxyThreadsCount();
                        if (threadsCount >= _maxProxyThreads) {
                            if (_debug) {
                                _log.debug("No ProxyThread available (current=" + threadsCount + ",max="
                                        + _maxProxyThreads + ")");
                            }
                            break;
                        }
                        final var hostsForProxy = getHostsByDestinationIdAndType(transfer.getDestinationName(),
                                HostOption.PROXY);
                        // Do we have at least one ProxyHost defined?
                        if (hostsForProxy.isEmpty()) {
                            if (_debug) {
                                _log.debug("No ProxyHost associated with DataTransfer-" + transfer.getId());
                            }
                            continue;
                        }
                        synchronized (_proxyThreads) {
                            if (!_proxyThreads.containsKey(_currentKey = _getKey(transfer))) {
                                final var thread = new ProxyThread(hostsForProxy, transfer);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + _currentKey, null);
                                thread.execute(true);
                                _proxyThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    } catch (final Throwable t) {
                        _log.warn("nextStep", t);
                    }
                }
                synchronized (_toRemove) {
                    for (final long key : _toRemove) {
                        _proxyThreads.remove(key);
                    }
                    _toRemove.clear();
                }
                // Check the expired ProxyThreads
                if (_timeOutProxyThread > 0) {
                    final var currentTime = System.currentTimeMillis();
                    for (final ProxyThread thread : _proxyThreads.values()) {
                        if (thread != null && System.currentTimeMillis() - thread._time > _timeOutProxyThread) {
                            _log.warn("ProxyThread " + _getKey(thread._transfer) + " expired ("
                                    + Format.formatDuration(thread._time, currentTime) + ")");
                            try {
                                thread.interrupt();
                            } catch (final Throwable t) {
                                _log.warn("Interrupting ProxyThread", t);
                            }
                        }
                    }
                }
                final var duration = System.currentTimeMillis() - start;
                if (_log.isDebugEnabled() && duration > 60000) {
                    _log.debug("Step completed in {}: {} DataFile(s) processed for proxy",
                            Format.formatDuration(duration), processed);
                }
            } catch (final Throwable t) {
                _log.warn("Error in scheduler", t);
            }
            // If there are still files to be processed then there is no reason
            // to wait!
            return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
        }

        /**
         * The Class ProxyThread.
         */
        private final class ProxyThread extends ConfigurableRunnable {

            /** The _time. */
            private final long _time = System.currentTimeMillis();

            /** The _hosts for proxy. */
            private final Collection<Host> _hostsForProxy;

            /** The _host for proxy. */
            private Host _hostForProxy = null;

            /** The _transfer. */
            private DataTransfer _transfer = null;

            /**
             * Instantiates a new proxy thread.
             *
             * @param hostForProxy
             *            the host for proxy
             * @param transfer
             *            the transfer
             */
            private ProxyThread(final Collection<Host> hostForProxy, final DataTransfer transfer) {
                _transfer = transfer;
                _hostsForProxy = hostForProxy;
            }

            /**
             * Gets the host for proxy.
             *
             * @return the host for proxy
             */
            public Host getHostForProxy() {
                return _hostForProxy;
            }

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                final var start = System.currentTimeMillis();
                var complete = false;
                final var key = _getKey(_transfer);
                try {
                    _log.info("Starting proxy for DataTransfer " + _transfer.getId());
                    final var list = _getTransferServers(_transfer.getDataFile());
                    final var destination = _transfer.getDestination();
                    BackupResult rr = null;
                    // TODO: find a better way of checking the maximum number of
                    // proxy hosts - there is a synchronisation issue here!
                    for (final Host hostForProxy : _hostsForProxy) {
                        final var threadCound = getThreadCount(hostForProxy);
                        final var maxConnections = hostForProxy.getMaxConnections();
                        if (threadCound >= maxConnections) {
                            if (_debug) {
                                _log.debug("No session available for ProxyHost " + hostForProxy.getNickname()
                                        + " (count=" + threadCound + ",max=" + maxConnections + ")");
                            }
                            continue;
                        }
                        // Are we requested to use the filter options defined at the destination level?
                        final var proxySetup = HOST_PROXY.getECtransSetup(hostForProxy.getData());
                        if (proxySetup.getBoolean(HOST_PROXY_USE_DESTINATION_FILTER)) {
                            _log.debug("Using Destination options for replicating DataTransfer-{} to ProxyHost {}",
                                    _transfer.getId(), hostForProxy.getNickname());
                            hostForProxy.setFilterName(destination.getFilterName());
                            final var setup = DESTINATION_ECTRANS.getECtransSetup(destination.getData());
                            setup.getOptionalString(DESTINATION_ECTRANS_FILTERPATTERN).ifPresent(
                                    filterPattern -> proxySetup.set(HOST_ECTRANS_FILTERPATTERN, filterPattern));
                            setup.getOptionalByteSize(DESTINATION_ECTRANS_FILTER_MINIMUM_SIZE)
                                    .ifPresent(byteSize -> proxySetup.set(HOST_ECTRANS_FILTER_MINIMUM_SIZE, byteSize));
                            hostForProxy.setData(proxySetup.getData());
                        }
                        if ((rr = TransferScheduler.backup(_hostForProxy = hostForProxy, list, _transfer)).complete) {
                            break;
                        }
                    }
                    // Did we find any HostForProxy available for the
                    // transmission? (good or bad)
                    final var duration = System.currentTimeMillis() - start;
                    if (rr == null) {
                        // Maybe next time!
                        if (_debug) {
                            _log.debug("No ProxyHost found for DataTransfer-" + _transfer.getId());
                        }
                        return;
                    }
                    final var base = getDataBase();
                    // Make sure we have the latest transfer update!
                    final var fromCache = getDataTransferFromCache(_transfer.getId());
                    _transfer = fromCache != null ? fromCache : base.getDataTransfer(_transfer.getId());
                    _transfer.setProxyTime(new Timestamp(System.currentTimeMillis()));
                    if (rr.hostForBackup != null) {
                        // Do we have a host for backup set?
                        _transfer.setProxyHostName(rr.hostForBackup.getName());
                        _transfer.setProxyHost(rr.hostForBackup);
                    }
                    if (fromCache == null) { // Not updated in the scheduler so we have to do it now
                        base.update(_transfer);
                    }
                    complete = rr.complete;
                    if (complete) {
                        // Add a new history to inform about the successful
                        // transmissions to the Proxy!
                        _transfer.setComment("Replication completed in " + Format.formatDuration(duration) + " from "
                                + rr.transferServer + " to Proxy Host=" + rr.hostForBackup.getName() + " ("
                                + rr.hostForBackup.getNickname() + ")");
                        addTransferHistory(_transfer);
                    } else if (isNotEmpty(rr.message)) {
                        // Add a new history to inform about the failure!
                        addTransferHistory(_transfer, StatusFactory.STOP, rr.message);
                    }
                    final var dataFile = _transfer.getDataFile();
                    if (_splunk.isInfoEnabled())
                        _splunk.info("CPY;{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{}",
                                "Monitored=" + destination.getMonitor(), "DataTransferId=" + _transfer.getId(),
                                "DestinationName=" + destination.getName(),
                                "DestinationType=" + DestinationOption.getLabel(destination.getType()),
                                "FileName=" + _transfer.getTarget(), "FileSize=" + dataFile.getSize(),
                                "ScheduledTime=" + _transfer.getScheduledTime(), "StartTime=" + new Timestamp(start),
                                "MetaStream=" + nullToNone(dataFile.getMetaStream()),
                                "MetaType=" + nullToNone(dataFile.getMetaType()), "MetaTime=" + dataFile.getMetaTime(),
                                "TimeBase=" + dataFile.getTimeBase(), "TimeStep=" + dataFile.getTimeStep(),
                                "Duration=" + duration, "CountryCode=" + destination.getCountryIso(),
                                "Target=" + (rr.hostForBackup == null ? "-" : rr.hostForBackup.getNickname()),
                                "TransferServer=" + (isEmpty(rr.transferServer) ? "-"
                                        : rr.transferServer.replaceAll("DataMover=", "")),
                                "Caller=" + nullToNone(dataFile.getCaller()), "ExpiryTime=" + _transfer.getExpiryTime(),
                                "FileSystem=" + dataFile.getFileSystem(), "Status=" + complete,
                                "Message=" + nullToNone(rr.message), "Action=proxy");
                } catch (final Throwable t) {
                    _log.warn("Running ProxyThread " + key, t);
                } finally {
                    _toRemove.add(key);
                    _log.info("DataTransfer " + _transfer.getId() + " proxy: " + complete + " (" + _transfer + ")");
                }
            }
        }
    }

    /**
     * The Class AcquisitionScheduler.
     */
    public final class AcquisitionScheduler extends MBeanScheduler {

        /** The _max acquisition threads. */
        private int _maxAcquisitionThreads = Cnf.at("Scheduler", "maxAcquisitionThreads", 100);

        /** The _maximum duration. */
        private long _maximumDuration = Cnf.durationAt("Scheduler", "maximumDurationAcquisitionThread",
                10 * Timer.ONE_MINUTE);

        /** The _interrupt slow. */
        public boolean _interruptSlow = Cnf.at("Scheduler", "interruptSlowAcquisitionThread", true);

        /** The _acquisition threads. */
        private final Map<String, AcquisitionThread> _acquisitionThreads = new ConcurrentHashMap<>();

        /** The _to remove. */
        private final List<String> _toRemove = Collections.synchronizedList(new ArrayList<>());

        /** The _current key. */
        private String _currentKey = "[none]";

        /** The _pause. */
        private boolean _pause = false;

        /**
         * Instantiates a new acquisition scheduler.
         *
         * @param name
         *            the name
         */
        public AcquisitionScheduler(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "acquisitionScheduler", Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "acquisitionSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "acquisitionSchedulerTimeRanges"));
        }

        /**
         * Gets the download threads count.
         *
         * @return the download threads count
         */
        public int getDownloadThreadsCount() {
            return _acquisitionThreads.size() - _toRemove.size();
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return "lastHost=" + _currentKey + ",acquisitionThreads=" + _acquisitionThreads.size();
        }

        /**
         * Gets the key.
         *
         * @param host
         *            the host
         *
         * @return the string
         */
        private String _getKey(final Host host) {
            return host.getName();
        }

        /**
         * Gets the thread list.
         *
         * @return the thread list
         */
        public String getThreadList() {
            final var status = new StringBuilder();
            final var currentTime = System.currentTimeMillis();
            for (final AcquisitionThread thread : _acquisitionThreads.values()) {
                final String key;
                if (thread != null && !_toRemove.contains(key = _getKey(thread._host))) {
                    status.append(
                            (thread._desName + " Host " + key + " (" + Format.formatDuration(thread._time, currentTime)
                                    + ")").replace(' ', '_').replace('-', '_'))
                            .append(" ");
                }
            }
            return status.toString().trim();
        }

        /**
         * Interrupt acquisition for.
         *
         * @param host
         *            the host
         *
         * @return true, if successful
         */
        public boolean interruptAcquisitionFor(final Host host) {
            final var thread = _acquisitionThreads.get(_getKey(host));
            if (thread != null && thread.isAlive()) {
                _log.debug("Interrupting AcquisitionThread for Host-{}", host.getName());
                try {
                    thread._running = false;
                    thread._time = System.currentTimeMillis();
                    try {
                        thread.join(10 * Timer.ONE_SECOND);
                    } catch (final Exception e) {
                        // The wait timed out? Interrupted?
                        if (thread.isAlive())
                            thread.interrupt();
                    }
                    return true;
                } catch (final Throwable t) {
                    _log.warn("Interrupting AcquisitionThread", t);
                }
            }
            return false;
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                    "The AcquisitionScheduler is used to manage the acquisition threads.",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("ThreadSize", "int", "ThreadSize: number of download thread(s).",
                                    true, false, false),
                            new MBeanAttributeInfo("ThreadList", "int", "ThreadList: list of download thread(s).", true,
                                    false, false),
                            new MBeanAttributeInfo("MaximumAcquisitionThreads", "int",
                                    "MaximumAcquisitionThreads: maximum number of parallel acquisitions.", true, true,
                                    false),
                            new MBeanAttributeInfo("MaximumDuration", "long",
                                    "MaximumDuration: maximum duration for an acquisition in milliseconds.", true, true,
                                    false),
                            new MBeanAttributeInfo("InterruptSlow", "boolean",
                                    "InterruptSlow: automaticaly interrupt slow acquisitions.", true, true, false) },
                    new MBeanOperationInfo[0]);
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("ThreadSize".equals(attributeName)) {
                    return _acquisitionThreads.size();
                }
                if ("ThreadList".equals(attributeName)) {
                    return getThreadList();
                }
                if ("MaximumAcquisitionThreads".equals(attributeName)) {
                    return _maxAcquisitionThreads;
                }
                if ("MaximumDuration".equals(attributeName)) {
                    return _maximumDuration;
                }
                if ("InterruptSlow".equals(attributeName)) {
                    return _interruptSlow;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("MaximumAcquisitionThreads".equals(name)) {
                _maxAcquisitionThreads = (Integer) value;
                return true;
            }
            if ("MaximumDuration".equals(name)) {
                _maximumDuration = (Long) value;
                return true;
            }
            if ("InterruptSlow".equals(name)) {
                _interruptSlow = (Boolean) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Sets the pause.
         *
         * @param pause
         *            the new pause
         */
        public void setPause(final boolean pause) {
            _pause = pause;
        }

        /**
         * Gets the pause.
         *
         * @return the pause
         */
        public boolean getPause() {
            return _pause;
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            var processed = 0;
            try {
                final var start = System.currentTimeMillis();
                final var destinations = getECpdsBase().getDestinationsAndHostsForType(HostOption.ACQUISITION,
                        2 * _maxAcquisitionThreads);
                for (final String desName : destinations.keySet()) {
                    for (final Host host : destinations.get(desName).toArray(new Host[0])) {
                        if (_pause || !isRunning() || getDownloadThreadsCount() >= _maxAcquisitionThreads) {
                            break;
                        }
                        synchronized (_acquisitionThreads) {
                            if (!_acquisitionThreads.containsKey(_currentKey = _getKey(host))) {
                                final var thread = new AcquisitionThread(desName, host);
                                thread.setThreadNameAndCookie(null, desName, "Host-" + host.getName(), null);
                                thread.execute(true);
                                _acquisitionThreads.put(_currentKey, thread);
                                processed++;
                            }
                        }
                    }
                }
                synchronized (_toRemove) {
                    for (final String key : _toRemove) {
                        final var thread = _acquisitionThreads.remove(key);
                        _log.debug("AcquisitionThread " + key + " removed (" + (thread != null) + ")");
                    }
                    _toRemove.clear();
                }
                // Check the expired DownloadThreads
                if (_interruptSlow) {
                    for (final AcquisitionThread thread : _acquisitionThreads.values()) {
                        if (thread != null && thread._localInterruptSlow) {
                            final var currentTime = System.currentTimeMillis();
                            final var duration = currentTime - thread._time;
                            final var maximumDuration = thread._localMaximumDuration;
                            if (duration > maximumDuration) {
                                _log.debug("Interrupting slow acquisition: " + thread._host.getName() + " ("
                                        + Format.formatDuration(duration) + ")");
                                try {
                                    thread.interrupt();
                                    thread._time = System.currentTimeMillis();
                                } catch (final Throwable t) {
                                    _log.warn("Interrupting AcquisitionThread", t);
                                }
                            }
                        }
                    }
                }
                final var duration = System.currentTimeMillis() - start;
                if (_log.isDebugEnabled() && duration > 60000) {
                    _log.debug("Step completed in {}: {} Host(s) processed for acquisition",
                            Format.formatDuration(duration), processed);
                }
            } catch (final Throwable t) {
                _log.warn("Error in scheduler", t);
            }
            // If there are still Destinations to be processed then there is no
            // reason to wait!
            return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
        }

        /**
         * The Class AcquisitionThread.
         */
        private final class AcquisitionThread extends ConfigurableRunnable implements StatusUpdate {

            /** The Constant LINE_SEPARATOR. */
            private static final String LINE_SEPARATOR = "------------------------------------------------------------------------";

            /** The _des name. */
            private final String _desName;

            /** The _gzip. */
            private final OutputStream _tmpOut;

            /** The _out. */
            private final File _fileOutputTmp;

            /** The _out. */
            private final File _fileOutputDone;

            /** The _out. */
            private final FileOutputStream _out;

            /** The _options list. */
            private final Map<String, String> _optionsList = new ConcurrentHashMap<>();

            /** The _time. */
            private long _time = System.currentTimeMillis();

            /** The _localMaximumDuration. */
            private long _localMaximumDuration = _maximumDuration;

            /** The _localInterruptSlow. */
            private boolean _localInterruptSlow = _interruptSlow;

            /** The _host. */
            private Host _host = null;

            /** The _debug. */
            private boolean _debug = false;

            /** The _setup. */
            private ECtransSetup _setup = null;

            /** The _size. */
            private int _size = 0;

            /** The summary. */
            final StringBuilder _summary = new StringBuilder();

            /** The _running. */
            boolean _running = true;

            /**
             * Instantiates a new acquisition thread.
             *
             * @param desName
             *            the des name
             * @param host
             *            the host
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private AcquisitionThread(final String desName, final Host host) throws IOException {
                _fileOutputTmp = _getHostOutputFile(host, true);
                _fileOutputDone = _getHostOutputFile(host, false);
                _out = new FileOutputStream(_fileOutputTmp);
                if (_fileOutputTmp.getName().endsWith(".gz")) {
                    _tmpOut = new GZIPOutputStream(_out, Deflater.BEST_COMPRESSION);
                } else {
                    _tmpOut = _out;
                }
                _desName = desName;
                _host = host;
            }

            /**
             * _compare.
             *
             * @param op
             *            the op
             * @param localValue
             *            the local value
             * @param remoteValue
             *            the remote value
             *
             * @return true, if successful
             */
            private boolean _compare(final String op, final long localValue, final long remoteValue) {
                if ("==".equals(op)) {
                    return remoteValue == localValue;
                }
                if (">=".equals(op)) {
                    return remoteValue >= localValue;
                }
                if ("<=".equals(op)) {
                    return remoteValue <= localValue;
                }
                if (">>".equals(op)) {
                    return remoteValue > localValue;
                }
                if ("<<".equals(op)) {
                    return remoteValue < localValue;
                }
                if ("!=".equals(op)) {
                    return remoteValue != localValue;
                }
                // Operation no recognized
                return false;
            }

            /**
             * _check size.
             *
             * @param remoteSize
             *            the remote size
             *
             * @return true, if successful
             *
             * @throws ScriptException
             *             the script exception
             */
            private boolean _checkSize(final long remoteSize) throws ScriptException {
                var value = _optionsList.get(HOST_ACQUISITION_FILESIZE.getName());
                if (value == null && (value = _setup.getString(HOST_ACQUISITION_FILESIZE)) != null) {
                    if (value.indexOf("$size") != -1) {
                        // This is a string to evaluate!
                        return ScriptManager.exec(Boolean.class, ScriptManager.JS,
                                Format.replaceAll(value, "$size", remoteSize));
                    }
                    final var option = _getValue(value);
                    if (option != null && "".equals(option.name)) {
                        value = option.op2 + option.value;
                    }
                }
                return value == null || value.length() < 3
                        || _compare(value.substring(0, 2), ByteSize.parse(value.substring(2)).size(), remoteSize);
            }

            /**
             * _check age.
             *
             * @param remoteAge
             *            the remote age
             *
             * @return true, if successful
             *
             * @throws ScriptException
             *             the script exception
             */
            private boolean _checkAge(final long remoteAge) throws ScriptException {
                var value = _optionsList.get(HOST_ACQUISITION_FILEAGE.getName());
                if (value == null && (value = _setup.getString(HOST_ACQUISITION_FILEAGE)) != null) {
                    if (value.indexOf("$age") != -1) {
                        // This is a string to evaluate!
                        return ScriptManager.exec(Boolean.class, ScriptManager.JS,
                                Format.replaceAll(value, "$age", remoteAge));
                    }
                    final var option = _getValue(value);
                    if (option != null && "".equals(option.name)) {
                        value = option.op2 + option.value;
                    }
                }
                return value == null || value.length() < 3
                        || _compare(value.substring(0, 2), Format.getDuration(value.substring(2)), remoteAge);
            }

            /**
             * Gets the boolean.
             *
             * @param option
             *            the option
             *
             * @return true, if successful
             */
            private boolean _getBoolean(final ECtransOptions option) {
                try {
                    final var result = _getString(null, true, option.getName(), option.getDefaultString());
                    return "true".equalsIgnoreCase(result) || "yes".equalsIgnoreCase(result);
                } catch (final Throwable t) {
                    return option.getDefaultBoolean();
                }
            }

            /**
             * Gets the int.
             *
             * @param option
             *            the option
             *
             * @return the int
             */
            private int _getInt(final ECtransOptions option) {
                try {
                    return Integer.parseInt(_getString(null, true, option.getName(), option.getDefaultString()));
                } catch (final Throwable t) {
                    return option.getDefaultInteger();
                }
            }

            /**
             * By default get the options from the line and the global host configuration only.
             *
             * @param option
             *            the option
             * @param defaultValue
             *            the default value
             *
             * @return the string
             */
            private String _getString(final ECtransOptions option, final String defaultValue) {
                return _getString(null, true, option.getName(), defaultValue);
            }

            /**
             * Gets the string.
             *
             * @param option
             *            the option
             *
             * @return the string
             */
            private String _getString(final ECtransOptions option) {
                return _getString(null, true, option.getName(), option.getDefaultString());
            }

            /**
             * Gets the string.
             *
             * @param params
             *            the params
             * @param useLineOption
             *            the use line option
             * @param option
             *            the option
             * @param defaultValue
             *            the default value
             *
             * @return the string
             */
            private String _getString(final Map<String, String> params, final boolean useLineOption,
                    final String option, final String defaultValue) {
                // First check the option in the parameter itself e.g.
                // $date[dateformat=YYYY,...]
                var value = params != null ? params.get(option) : null;
                // If there is no option found then search the options shared
                // with the line e.g. [dateformat=YYYY,...]/mydir/{test(.*).tmp}
                // unless asked not to do so!
                value = value == null ? useLineOption ? _optionsList.get(option) : null : value;
                if (value != null && value.length() > 2) {
                    // We found the option let's remove the operator
                    return value.substring(2);
                }
                // Not found let's search the host configuration
                return _setup.get(option, defaultValue);
            }

            /**
             * Gets the string.
             *
             * @param params
             *            the params
             * @param useLineOption
             *            the use line option
             * @param option
             *            the option
             *
             * @return the string
             */
            private String _getString(final Map<String, String> params, final boolean useLineOption,
                    final ECtransOptions option) {
                return _getString(params, useLineOption, option.getName(), option.getDefaultString());
            }

            /**
             * Display the data according to the options.
             *
             * @param params
             *            the params
             * @param useOptions
             *            the use options
             *
             * @return the string
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private String _getFormatedDate(final Map<String, String> params, final boolean useOptions)
                    throws IOException {
                // How should we display the date?
                final var format = _getString(params, useOptions, HOST_ACQUISITION_DATEFORMAT);
                // Do we have a delta to apply to the date?
                final var delta = _getString(params, useOptions, HOST_ACQUISITION_DATEDELTA);
                // If we want to parse the date from a source what pattern
                // should we use?
                final var pattern = _getString(params, useOptions, HOST_ACQUISITION_DATEPATTERN.getName(), format);
                // Do we have a source to parse the date from?
                final var source = _getString(params, useOptions, HOST_ACQUISITION_DATESOURCE);
                final long date;
                if (isNotEmpty(source)) {
                    // Let's parse the date from the source according to the
                    // provided pattern!
                    final var simpleFormat = new SimpleDateFormat(pattern);
                    try {
                        date = simpleFormat.parse(source).getTime();
                    } catch (final Throwable t) {
                        throw new IOException("parsing date in " + source);
                    }
                } else {
                    // There is no source to parse the date from so let's use
                    // the current time!
                    date = System.currentTimeMillis();
                }
                // Return the formated date according to the delta and format
                // found
                return Format.formatTime(format, date + Format.getDuration(delta));
            }

            /**
             * Check if there are no parameters configured after the $date or $dirdate option .e.g
             * $date[dateformat=YYYY].
             *
             * @param paramName
             *            the param name
             * @param string
             *            the string
             * @param useOptions
             *            the use options
             *
             * @return the string
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private String _replaceDate(final String paramName, String string, final boolean useOptions)
                    throws IOException {
                final var result = new StringBuilder();
                int index;
                while ((index = string.toLowerCase().indexOf(paramName)) != -1) {
                    Map<String, String> params = null;
                    result.append(string.substring(0, index));
                    string = string.substring(index + paramName.length());
                    if (string.startsWith("[") && (index = string.indexOf("]")) != -1) {
                        params = _getParams(string.substring(1, index), false);
                        string = string.substring(index + 1);
                    }
                    string = _getFormatedDate(params, useOptions) + string;
                }
                return result.append(string).toString();
            }

            /**
             * The Class Option.
             */
            private final class Option {

                /** The name. */
                String name;

                /** The value. */
                String value;

                /** The op1. */
                String op1; // Original operation (might be 1 character only)

                /** The op2. */
                String op2; // Updated operation (always 2 characters)
            }

            /**
             * Gets the value.
             *
             * @param option
             *            the option
             *
             * @return the option
             */
            private Option _getValue(String option) {
                final var result = new Option();
                // If the option is in the form name="value" then let's remove
                // the '="' and '"' at the end!
                final var pos1 = option.indexOf("=\"");
                if (pos1 > 0 && option.endsWith("\"") && pos1 + 2 < option.length()) {
                    option = option.substring(0, pos1 + 1) + option.substring(pos1 + 2, option.length() - 1);
                }
                // Now we know we have an option in the format name=value,
                // name<=value,name>=value ...
                for (String op : new String[] { "==", ">=", "<=", "!=", "=", "<", ">" }) {
                    final var pos2 = option.indexOf(op);
                    if (pos2 >= 0) {
                        // We might have an empty name!
                        result.name = option.substring(0, pos2);
                        var value = option.substring(pos2 + op.length());
                        // If '>', '<' or '=' then set '>>',
                        // '<<' or '==' to encode the operation
                        // on 2 characters
                        result.op1 = op;
                        if (op.length() == 1) {
                            op += op;
                        }
                        result.op2 = op;
                        // If the value is between quotes then let's remove
                        // them!
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                            value = value.substring(1, value.length() - 1);
                        }
                        result.value = value;
                        return result;
                    }
                }
                // Parsing error?
                return null;
            }

            /**
             * Update status.
             *
             * @param messages
             *            the messages
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             * @throws DataBaseException
             *             Signals that a DataBase exception has occurred.
             */
            @Override
            public void info(final String... messages) throws IOException, DataBaseException {
                final var sb = new StringBuilder();
                for (final String message : messages) {
                    sb.append(sb.isEmpty() ? "" : "\n").append(getCurrentTime()).append(message);
                }
                _update(sb);
            }

            /**
             * Update status.
             *
             * @param messages
             *            the messages
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             * @throws DataBaseException
             *             Signals that a DataBase exception has occurred.
             */
            @Override
            public void warn(final String... messages) throws IOException, DataBaseException {
                final var sb = new StringBuilder();
                for (final String message : messages) {
                    sb.append(sb.isEmpty() ? "" : "\n").append("err:").append(getCurrentTime()).append(message);
                }
                _update(sb);
            }

            /**
             * Update status.
             *
             * @param progress
             *            the progress
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private void _update(final StringBuilder progress) throws IOException {
                _updateHostOutput(_host,
                        Format.compressBase64(_summary.append(_summary.isEmpty() ? "" : "\n").append(progress)));
            }

            /**
             * Adds the.
             *
             * @param value
             *            the value
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            public void _add(final String value) throws IOException {
                synchronized (_tmpOut) {
                    try {
                        _tmpOut.write(("\n" + value).getBytes());
                    } catch (final IOException e) {
                        // Ignore!
                    }
                }
                if (_debug) {
                    _log.debug(value);
                }
            }

            /**
             * Adds the.
             *
             * @param name
             *            the name
             * @param value
             *            the value
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private void _add(final String name, final String value) throws IOException {
                _add(name + ": " + value);
            }

            /**
             * Look for string inside delimiters.
             *
             * @param delimiter1
             *            the delimiter1
             * @param delimiter2
             *            the delimiter2
             * @param string
             *            the string
             *
             * @return the int
             */
            private int _getNext(final char delimiter1, final char delimiter2, final String string) {
                if (string.charAt(0) == delimiter1) {
                    var index = 0;
                    var in = 0;
                    for (final char c : string.toCharArray()) {
                        index++;
                        if (c == delimiter1) {
                            in++;
                        }
                        if (c == delimiter2) {
                            in--;
                        }
                        if (in == 0) {
                            return index;
                        }
                    }
                }
                return -1;
            }

            /**
             * Parse options in the form filesize=">1234";fileage>=24h,dateformat=yyyyMMdd;datedelta="-1d"
             * ;metadata="dir=$date[dateformat=yyyy]/test".
             *
             * @param options
             *            the options
             * @param log
             *            the log
             *
             * @return the hashtable
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private Map<String, String> _getParams(final String options, final boolean log) throws IOException {
                final Map<String, String> params = new ConcurrentHashMap<>();
                final var current = new StringBuilder();
                Character delimiter = null;
                var in = 0;
                for (final char c : options.toCharArray()) {
                    if (delimiter != null && c == delimiter) {
                        delimiter = null;
                        continue;
                    }
                    if (delimiter == null) {
                        if (c == '"' || c == '\'') {
                            delimiter = c;
                            continue;
                        } else if (in == 0 && (c == ';' || c == ',')) {
                            _addParam(params, current.toString(), log);
                            current.setLength(0);
                            continue;
                        } else if (c == '[') {
                            in++;
                        } else if (c == ']') {
                            in--;
                        }
                    }
                    current.append(c);
                }
                if (current.length() > 0) {
                    _addParam(params, current.toString(), log);
                }
                return params;
            }

            /**
             * Process the option and add it to the list.
             *
             * @param params
             *            the params
             * @param string
             *            the string
             * @param log
             *            the log
             *
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private void _addParam(final Map<String, String> params, final String string, final boolean log)
                    throws IOException {
                final var option = _getValue(string);
                if (option != null) {
                    params.put(option.name, option.op2 + option.value);
                    if (log) {
                        _add("Opt" + (++_size), option.name + option.op1 + option.value);
                    }
                } else {
                    _log.debug("Error parsing option: " + string);
                    if (log) {
                        _add("err:Opt" + (++_size) + ": " + string + " (parsing error)");
                    }
                }
            }

            /**
             * Parses an acquisition file entry.
             *
             * @param base
             *            the base
             * @param ar
             *            the ar
             * @param copy
             *            the copy
             * @param namePattern
             *            the name pattern
             * @param currentPath
             *            the current path
             * @param destination
             *            the destination
             * @param entry
             *            the entry
             * @param body
             *            the body
             * @param message
             *            the message
             *
             * @throws ScriptException
             *             the script exception
             * @throws IOException
             *             Signals that an I/O exception has occurred.
             */
            private void _parseEntry(final ECpdsBase base, final AcquisitionResult ar, final Host copy,
                    final String namePattern, final String currentPath, final Destination destination,
                    final FileEntry entry, final String body, final String message)
                    throws ScriptException, IOException {
                String outcome;
                if (entry.exception == null) {
                    // File entry found with no error
                    final var symlink = FtpParser.SYMBOLIC_LINK_TYPE == entry.type;
                    final var useSymlink = _getBoolean(HOST_ACQUISITION_USE_SYMLINK);
                    if (!useSymlink && symlink) {
                        // We are not allowed to use the symbolic links!
                        outcome = "not-selected: symbolic link (useSymlink=no)";
                    } else if (!symlink && FtpParser.FILE_TYPE != entry.type) {
                        // Don't select directories or files with an unknown
                        // type
                        outcome = "not-selected: directory or unknown type";
                    } else {
                        final var onlyValidTime = _getBoolean(HOST_ACQUISITION_ONLY_VALID_TIME);
                        if (onlyValidTime && entry.time == -1) {
                            outcome = "not-selected: date/time could not be parsed";
                        } else {
                            // It is a valid data/time or we didn't check!
                            try {
                                if ("".equals(namePattern) || entry.name.matches(namePattern)) {
                                    // It does match the required pattern!
                                    outcome = _processValidEntry(base, ar, copy, currentPath, destination, entry,
                                            symlink, body, message);
                                } else {
                                    outcome = "not-selected: wrong pattern";
                                }
                            } catch (final PatternSyntaxException e) {
                                _log.warn("Pattern matching {} -> {}", entry.name, namePattern, e);
                                outcome = "not-selected: syntax error in regex pattern";
                            }
                        }
                    }
                } else {
                    // Error parsing the file entry
                    outcome = "error: " + Format.getMessage(entry.exception);
                }
                // Display the current line
                _add((outcome.startsWith("selected") ? "log:" : "err:") + entry.line + " (" + outcome + ")");
            }

            /**
             * A match has been found with a file entry, so let's process it.
             *
             * @param base
             *            the base
             * @param ar
             *            the ar
             * @param copy
             *            the copy
             * @param currentPath
             *            the current path
             * @param destination
             *            the destination
             * @param entry
             *            the entry
             * @param symlink
             *            the symlink
             * @param body
             *            the body
             * @param message
             *            the message
             *
             * @return the string
             *
             * @throws ScriptException
             *             the script exception
             */
            String _processValidEntry(final ECpdsBase base, final AcquisitionResult ar, final Host copy,
                    final String currentPath, final Destination destination, final FileEntry entry,
                    final boolean symlink, final String body, final String message) throws ScriptException {
                // Check if this is an absolute or relative entry name
                final var name = Format.cleanTextContent(entry.name);
                final var lower = name.toLowerCase();
                final String initialTarget;
                final String original;
                var windows = false;
                var url = false;
                if ((url = lower.startsWith("http://") || lower.startsWith("https://")) // URL
                        || lower.startsWith("/") // UNIX
                        || (windows = lower.startsWith("\\"))) // Windows
                {
                    // This is an absolute entry name (forget currentPath)
                    original = name;
                    if (url) {
                        final var urlName = new File(name).getName();
                        if (_getBoolean(HOST_ACQUISITION_REMOVE_PARAMETERS)) {
                            final var index = urlName.indexOf("?");
                            // If we detect a ? then let's remove the parameters!
                            initialTarget = index != -1 ? urlName.substring(0, index) : urlName;
                        } else {
                            initialTarget = urlName;
                        }
                    } else {
                        // Is it UNIX or Windows?
                        final var index = name.lastIndexOf(windows ? "\\" : "/");
                        // If we detect a file separator then let's remove the
                        // parent directory!
                        initialTarget = index != -1 ? name.substring(index + 1) : name;
                    }
                } else {
                    // This is a relative entry name
                    initialTarget = name;
                    original = currentPath + initialTarget;
                }
                // Which target should be used effectively?
                var effectiveTarget = _getString(HOST_ACQUISITION_TARGET);
                final var filename = new File(initialTarget).getName();
                try {
                    effectiveTarget = Format.replaceAllExt(effectiveTarget, "$destination", destination.getName());
                    effectiveTarget = Format.replaceAllExt(effectiveTarget, "$name", filename);
                    effectiveTarget = Format.replaceAllExt(effectiveTarget, "$target", initialTarget);
                    effectiveTarget = Format.replaceAllExt(effectiveTarget, "$original", original);
                    effectiveTarget = Format.replaceAllExt(effectiveTarget, "$link", entry.link);
                    effectiveTarget = _replaceDate("$dirdate", effectiveTarget, true);
                    effectiveTarget = _replaceDate("$date", effectiveTarget, false);
                    effectiveTarget = Format.replaceAll(effectiveTarget, "$timestamp", entry.time / 1000L * 1000L);
                } catch (final IOException _) {
                }
                // Let's check the size and age options. We obviously only check
                // the time if it is valid!
                if ((symlink || _checkSize(entry.size))
                        && (entry.time == -1 || _checkAge(System.currentTimeMillis() - entry.time))) {
                    // Match the size or age option. What should we do with this
                    // selection?
                    final var action = _getString(HOST_ACQUISITION_ACTION).toLowerCase();
                    if ("delete".equals(action)) {
                        // This is a delete request!
                        try {
                            TransferScheduler.del(ar.server, copy, original);
                            // The file has been selected and deleted
                            // successfully
                            return "selected: deleted";
                        } catch (final Throwable t) {
                            // Error when trying to delete the data file
                            return "not-selected: delete error (" + Format.getMessage(t) + ")";
                        }
                    } else {
                        // If we have a data file with the same name but a
                        // different file time do we consider it as a single
                        // file or as different files? If we don't know the time
                        // then we just use the original name.
                        final var uniqueName = (_getBoolean(HOST_ACQUISITION_UNIQUE_BY_TARGET_ONLY) ? ""
                                : _getBoolean(HOST_ACQUISITION_USE_TARGET_AS_UNIQUE_NAME) ? initialTarget : original)
                                + (entry.time == -1 || !_getBoolean(HOST_ACQUISITION_UNIQUE_BY_NAME_AND_TIME) ? ""
                                        : "." + entry.time);
                        if ("queue".equals(action)) {
                            // This is a queue request. Check if the file is not
                            // already in the database?
                            final var standby = _getBoolean(HOST_ACQUISITION_STANDBY);
                            final var transfers = base.getScheduledDataTransfer(
                                    TransferManagement.getUniqueKey(standby, _desName, effectiveTarget, uniqueName),
                                    _desName);
                            final var found = transfers.length > 0;
                            var force = false;
                            if (found) {
                                // The datafile is already in the database so
                                // let's find the time and size and check if it
                                // should be requeued or not?
                                final var dataFile = transfers[0].getDataFile();
                                // Let's be compatible with the old parameters
                                // if the new parameter requeueon is not found!
                                final var requeueonupdate = _getBoolean(HOST_ACQUISITION_REQUEUEONUPDATE);
                                final var requeueonsamesize = _getBoolean(HOST_ACQUISITION_REQUEUEONSAMESIZE);
                                var requeueon = _getString(HOST_ACQUISITION_REQUEUEON, requeueonupdate
                                        ? requeueonsamesize ? "$time2 > $time1" : "$time2 > $time1 && $size2 != $size1"
                                        : "false");
                                try {
                                    requeueon = Format.replaceAll(requeueon, "$time1",
                                            dataFile.getTimeFile().getTime());
                                    requeueon = Format.replaceAll(requeueon, "$size1", dataFile.getSize());
                                    requeueon = Format.replaceAll(requeueon, "$time2", entry.time / 1000L * 1000L);
                                    requeueon = Format.replaceAll(requeueon, "$size2",
                                            entry.size >= 0 ? entry.size : -1);
                                    requeueon = Format.replaceAllExt(requeueon, "$destination", destination.getName());
                                    requeueon = Format.replaceAllExt(requeueon, "$target", initialTarget);
                                    requeueon = Format.replaceAllExt(requeueon, "$original", original);
                                    // Should we requeue it?
                                    force = ScriptManager.exec(Boolean.class, ScriptManager.JS, requeueon);
                                } catch (final Throwable t) {
                                    return "not-selected: requeueon error (" + Format.getMessage(t) + ")";
                                }
                            }
                            var failedOnly = false;
                            if (found && !force) {
                                for (final DataTransfer transfer : transfers) {
                                    final var status = transfer.getStatusCode();
                                    if (StatusFactory.FAIL.equals(status)) {
                                        // The file has not been retrieved so
                                        // let's re-queue it
                                        _log.debug("Requeuing DataFile: " + transfer.getDataFileId()
                                                + " (retrieval didn't succeed last time)");
                                        failedOnly = true;
                                        force = true;
                                        break;
                                    }
                                }
                            }
                            if (found && !force) {
                                // The file has already been recorded in the
                                // database
                                return "not-selected: exists DatafileId=" + transfers[0].getDataFileId();
                            } else {
                                // The file has not been found in the
                                // database or should be re-queued. Let's
                                // schedule it for retrieval
                                var metadata = _getString(HOST_ACQUISITION_METADATA);
                                try {
                                    metadata = Format.replaceAllExt(metadata, "$destination", destination.getName());
                                    metadata = Format.replaceAllExt(metadata, "$name", filename);
                                    metadata = Format.replaceAllExt(metadata, "$target", initialTarget);
                                    metadata = Format.replaceAllExt(metadata, "$original", original);
                                    metadata = Format.replaceAllExt(metadata, "$link", entry.link);
                                    metadata = _replaceDate("$dirdate", metadata, true);
                                    metadata = _replaceDate("$date", metadata, false);
                                    metadata = Format.replaceAll(metadata, "$timestamp", entry.time / 1000L * 1000L);
                                    final long dataFileId;
                                    final long notificationId;
                                    if (!isEmpty(message)) {
                                        // Let's push the notification message with the correct extension!
                                        final var payLoadName = effectiveTarget
                                                + _getString(HOST_ACQUISITION_PAYLOAD_EXTENSION);
                                        notificationId = ECpdsClient.put(
                                                "Notification from Acquisition Host=" + _host.getName() + " ("
                                                        + _host.getNickname() + ") on DataMover=" + ar.server.getName()
                                                        + " for Destination=" + _desName,
                                                _host.getLogin(), destination, _host.getName(), metadata, payLoadName,
                                                payLoadName, payLoadName, entry.time, -1, false,
                                                _getInt(HOST_ACQUISITION_PRIORITY),
                                                _getString(HOST_ACQUISITION_LIFETIME), null, standby, force, failedOnly,
                                                false, _getString(HOST_ACQUISITION_TRANSFERGROUP),
                                                Base64.getDecoder().decode(message));
                                    } else {
                                        notificationId = -1;
                                    }
                                    if (isEmpty(body)) {
                                        // Retrieval is going to be scheduled
                                        dataFileId = ECpdsClient.put(
                                                "Scheduled from Acquisition Host=" + _host.getName() + " ("
                                                        + _host.getNickname() + ") on DataMover=" + ar.server.getName()
                                                        + " for Destination=" + _desName,
                                                _host.getLogin(), destination, _host.getName(), metadata, original,
                                                uniqueName, effectiveTarget, entry.time,
                                                entry.size >= 0 ? entry.size : -1, _getBoolean(HOST_ACQUISITION_EVENT),
                                                _getInt(HOST_ACQUISITION_PRIORITY),
                                                _getString(HOST_ACQUISITION_LIFETIME), null, standby,
                                                _getString(HOST_ACQUISITION_GROUPBY,
                                                        "ACQ_" + _desName + "_" + _host.getName()),
                                                _getBoolean(HOST_ACQUISITION_NORETRIEVAL), force, failedOnly,
                                                _getBoolean(HOST_ACQUISITION_DELETEORIGINAL),
                                                _getString(HOST_ACQUISITION_TRANSFERGROUP));
                                        // The file has been selected and registered in the database for later retrieval
                                        return "selected: " + (force ? "re-" : "") + "scheduled with DatafileId="
                                                + dataFileId + (notificationId >= 0
                                                        ? " - notification DatafileId=" + notificationId : "");
                                    } else {
                                        // Sending the body
                                        dataFileId = ECpdsClient.put(
                                                "Received from Acquisition Host=" + _host.getName() + " ("
                                                        + _host.getNickname() + ") on DataMover=" + ar.server.getName()
                                                        + " for Destination=" + _desName,
                                                _host.getLogin(), destination, _host.getName(), metadata, original,
                                                uniqueName, effectiveTarget, entry.time,
                                                entry.size >= 0 ? entry.size : -1, _getBoolean(HOST_ACQUISITION_EVENT),
                                                _getInt(HOST_ACQUISITION_PRIORITY),
                                                _getString(HOST_ACQUISITION_LIFETIME), null, standby, force, failedOnly,
                                                _getBoolean(HOST_ACQUISITION_DELETEORIGINAL),
                                                _getString(HOST_ACQUISITION_TRANSFERGROUP),
                                                Base64.getDecoder().decode(body));
                                        // The file has been registered in the database and sent to the mover
                                        return "selected: " + (force ? "re-" : "") + "received with DatafileId="
                                                + dataFileId + (notificationId >= 0
                                                        ? " - notification DatafileId=" + notificationId : "");
                                    }
                                } catch (final Throwable t) {
                                    // Error when trying to register the
                                    // data file
                                    final var error = Format.getMessage(t);
                                    if (error.indexOf("DataFile already exist") != -1) {
                                        // already exists and submitted between
                                        // the check and the insert!
                                        return "not-selected:" + error.substring(8);
                                    } else {
                                        _log.warn("Registration error", t);
                                        return "not-selected: registration error (" + error + ")";
                                    }
                                }
                            }
                        } else {
                            // Unknown action requested!
                            return "not-selected: unknown action requested (" + action + ")";
                        }
                    }
                }
                if (symlink) {
                    return "not-selected: wrong age on symbolic link (time=" + Format.formatTime(entry.time) + ")";
                } else {
                    return "not-selected: wrong size/age (time=" + Format.formatTime(entry.time) + ",size=" + entry.size
                            + ")";
                }
            }

            /**
             * The Class ListThread.
             */
            // Listing meant to be used in multiple instances in parallel!
            final class ListThread extends ExecutorRunnable {

                /** The base. */
                final ECpdsBase _base;

                /** The ar. */
                final AcquisitionResult _ar;

                /** The copy. */
                final Host _copy;

                /** The name pattern. */
                final String _namePattern;

                /** The current path. */
                final String _currentPath;

                /** The destination. */
                final Destination _destination;

                /** The entry. */
                final FileEntry _entry;

                /** The body. */
                final String _body;

                /** The message. */
                final String _message;

                /**
                 * Instantiates a new list thread.
                 *
                 * @param manager
                 *            the manager
                 * @param base
                 *            the base
                 * @param ar
                 *            the ar
                 * @param copy
                 *            the copy
                 * @param namePattern
                 *            the name pattern
                 * @param currentPath
                 *            the current path
                 * @param destination
                 *            the destination
                 * @param entry
                 *            the entry
                 * @param body
                 *            the body
                 * @param message
                 *            the message
                 */
                ListThread(final ExecutorManager<ListThread> manager, final ECpdsBase base, final AcquisitionResult ar,
                        final Host copy, final String namePattern, final String currentPath,
                        final Destination destination, final FileEntry entry, final String body, final String message) {
                    super(manager);
                    _base = base;
                    _ar = ar;
                    _copy = copy;
                    _namePattern = namePattern;
                    _currentPath = currentPath;
                    _destination = destination;
                    _entry = entry;
                    _body = body;
                    _message = message;
                }

                /**
                 * Process.
                 *
                 * @throws ScriptException
                 *             the script exception
                 * @throws IOException
                 *             Signals that an I/O exception has occurred.
                 */
                @Override
                public void process() throws ScriptException, IOException {
                    _parseEntry(_base, _ar, _copy, _namePattern, _currentPath, _destination, _entry, _body, _message);
                }
            }

            /**
             * Gets the script.
             *
             * @param host
             *            the host
             *
             * @return the script
             */
            // Provide some parameters
            private String getScript(final Host host) {
                final var sb = new StringBuilder(host.getDir());
                final var method = host.getTransferMethod();
                final var module = method.getECtransModule();
                Format.replaceAll(sb, "$host[name]", host.getName());
                Format.replaceAll(sb, "$host[comment]", host.getComment());
                Format.replaceAll(sb, "$host[host]", host.getHost());
                Format.replaceAll(sb, "$host[login]", host.getLogin());
                Format.replaceAll(sb, "$host[passwd]", host.getPasswd());
                Format.replaceAll(sb, "$host[userMail]", host.getUserMail());
                Format.replaceAll(sb, "$host[networkCode]", host.getNetworkCode());
                Format.replaceAll(sb, "$host[networkName]", host.getNetworkName());
                Format.replaceAll(sb, "$host[nickname]", host.getNickname());
                Format.replaceAll(sb, "$transferMethod[name]", method.getName());
                Format.replaceAll(sb, "$transferMethod[comment]", method.getComment());
                Format.replaceAll(sb, "$transferMethod[value]", method.getValue());
                Format.replaceAll(sb, "$ectransModule[name]", module.getName());
                return sb.toString().trim();
            }

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                var completed = false;
                var interrupted = false;
                final var key = _getKey(_host);
                _log.debug("Starting AcquisitionThread " + key);
                Scanner scanner = null;
                try {
                    final var base = getDataBase(ECpdsBase.class);
                    final var destination = base.getDestination(_desName);
                    if (StatusFactory.STOP.equals(destination.getStatusCode())) {
                        _log.debug("AcquisitionThread " + key + " interrupted");
                        _running = false;
                    }
                    _host = base.getHost(_host.getName());
                    _setup = HOST_ACQUISITION.getECtransSetup(_host.getData());
                    _localMaximumDuration = _setup
                            .get(HOST_ACQUISITION_MAXIMUM_DURATION, Duration.ofMillis(_localMaximumDuration))
                            .toMillis();
                    _localInterruptSlow = _setup.getOptionalBoolean(HOST_ACQUISITION_INTERRUPT_SLOW)
                            .orElse(_localInterruptSlow);
                    final var script = getScript(_host);
                    final BufferedReader reader;
                    if (script.startsWith("$(") && script.endsWith(")")) {
                        // This is a script so we just evaluate it on one of the allocated data-mover
                        // and return the result string!
                        final var er = TransferScheduler.execution(this, _desName, _host,
                                script.substring(2, script.length() - 1));
                        if (!er.complete) {
                            return;
                        }
                        // We update the string with the result of the code
                        // and we continue the processing!
                        reader = new BufferedReader(new InputStreamReader(er.in));
                    } else {
                        reader = new BufferedReader(new StringReader(script));
                    }
                    _debug = _getBoolean(HOST_ACQUISITION_DEBUG);
                    final var start = System.currentTimeMillis();
                    final var startDate = Format.formatTime(start);
                    _add("Date", startDate + " (start=" + start + " ago)");
                    info("Acquisition started (start=" + start + " ago)");
                    var listingCount = 0;
                    String currentPath;
                    // Build a summary to display the progress of the processing!
                    final var progress = new StringBuilder();
                    ExecutorManager<ListThread> manager = null;
                    // Parse the list of directories in the form
                    // "[filesize=1234;fileage>=24h;dateformat=yyyyMMdd;datedelta=-1d]/home/test/acq-$date/{filename-regex}"
                    while (_running && !(interrupted = interrupted()) && (currentPath = reader.readLine()) != null) {
                        currentPath = currentPath.trim();
                        if (currentPath.isEmpty()) {
                            continue;
                        }
                        _optionsList.clear(); // remove previous values
                        _size = 0; // reset the option count!
                        listingCount++;
                        int pos;
                        if ((pos = _getNext('[', ']', currentPath)) != -1) {
                            final var options = currentPath.substring(1, pos - 1);
                            currentPath = currentPath.substring(pos);
                            _optionsList.putAll(_getParams(options, true));
                        }
                        // Now the current path is in the form
                        // "/home/systems/syi/acq-$date/{filename-regex}"
                        var namePattern = "";
                        if (currentPath.endsWith("}") && (pos = currentPath.indexOf("{")) != -1) {
                            namePattern = currentPath.substring(pos + 1, currentPath.length() - 1);
                            currentPath = currentPath.substring(0, pos);
                        }
                        // Now the current path should be in the form "/home/systems/syi/acq-$date/".
                        // Replace the $date parameter according to the options (format and delta)!
                        currentPath = _replaceDate("$date", currentPath, true);
                        namePattern = _replaceDate("$date", namePattern, true); // {...}
                        var regexPattern = _getString(HOST_ACQUISITION_REGEX_PATTERN);
                        regexPattern = _replaceDate("$date", regexPattern, true);
                        regexPattern = Format.replaceAll(regexPattern, "$namePattern", namePattern);
                        var wildcardFilter = _getString(HOST_ACQUISITION_WILDCARD_FILTER);
                        wildcardFilter = _replaceDate("$date", wildcardFilter, true);
                        final var path = currentPath + wildcardFilter;
                        final var formattedPath = path + (regexPattern.length() > 0 ? " (" + regexPattern + ")" : "");
                        _add("Path", formattedPath);
                        _add("Pattern", namePattern);
                        info("Processing " + formattedPath);
                        // Configure the host for the data mover
                        final var copy = (Host) _host.clone();
                        copy.setDir("");
                        progress.setLength(0);
                        final var ar = TransferScheduler.acquisition(this, _desName, copy, path,
                                regexPattern.length() > 0 ? regexPattern : null,
                                _setup.getBoolean(HOST_ACQUISITION_LIST_SYNCHRONOUS));
                        if (_running && ar.complete) {
                            info("Starting parsing of the listing.", StatusUpdate.IN_PROGRESS);
                            _add(LINE_SEPARATOR);
                            scanner = new Scanner(ar.in);
                            scanner.useDelimiter("\n");
                            var filesCount = 0;
                            // Create the ListManager if required!
                            manager = _setup.getBoolean(HOST_ACQUISITION_LIST_PARALLEL)
                                    ? new ExecutorManager<>(_setup.getInteger(HOST_ACQUISITION_LIST_MAX_WAITING),
                                            _setup.getInteger(HOST_ACQUISITION_LIST_MAX_THREADS))
                                    : null;
                            // Get the list of files from the remote site
                            while (_running && scanner.hasNext()) {
                                // We don't process the list of files at once as it might be huge and would
                                // consume a lot of memory!
                                final var entry = scanner.next();
                                if (entry.startsWith("err:")) {
                                    // This was an error so we just report it!
                                    _add(entry);
                                } else {
                                    // If we have a body and message content then let's extract them!
                                    final var pattern = Pattern.compile("^(.*)\\s\\[(.+)]$");
                                    final var matcher = pattern.matcher(entry);
                                    final String newLine;
                                    final String body;
                                    final String message;
                                    if (matcher.matches()) {
                                        newLine = matcher.group(1); // text before " ["
                                        final var groupContent = matcher.group(2); // inside the brackets
                                        final var bodyAndMessage = groupContent.split(";", 2);
                                        if (bodyAndMessage.length == 2) {
                                            body = bodyAndMessage[0].isEmpty() ? null : bodyAndMessage[0];
                                            message = bodyAndMessage[1];
                                        } else {
                                            body = null;
                                            message = groupContent;
                                        }
                                    } else {
                                        newLine = entry;
                                        body = null;
                                        message = null;
                                    }
                                    final var entries = FtpParser.parseDir(_getString(HOST_ACQUISITION_REGEX_FORMAT),
                                            _getString(HOST_ACQUISITION_SYSTEM_KEY).toUpperCase(),
                                            _getString(HOST_ACQUISITION_DEFAULT_DATE_FORMAT),
                                            _getString(HOST_ACQUISITION_RECENT_DATE_FORMAT),
                                            _getString(HOST_ACQUISITION_SERVER_LANGUAGE_CODE).toLowerCase(),
                                            _getString(HOST_ACQUISITION_SHORT_MONTH_NAMES),
                                            _getString(HOST_ACQUISITION_SERVER_TIME_ZONE_ID), new String[] { newLine });
                                    // Did we find anything?
                                    if (entries.length == 0) {
                                        continue;
                                    }
                                    if (manager != null) {
                                        // Do we have to start the ListManager?
                                        if (filesCount == 0) {
                                            manager.start();
                                        }
                                        // Let's start the listing!
                                        manager.put(new ListThread(manager, base, ar, copy, namePattern, currentPath,
                                                destination, entries[0], body, message));
                                    } else {
                                        _parseEntry(base, ar, copy, namePattern, currentPath, destination, entries[0],
                                                body, message);
                                    }
                                    filesCount++;
                                }
                            }
                            if (manager != null) {
                                // We don't want to take more jobs!
                                manager.stopRun();
                                // And now we wait for all the Threads to complete!
                                try {
                                    manager.join();
                                } catch (final InterruptedException e) {
                                }
                            }
                            // If we have no entries then we print a message!
                            if (filesCount == 0) {
                                _add("err:(empty)");
                            }
                            _add(LINE_SEPARATOR);
                            _add("List", progress.append("Found ").append(filesCount)
                                    .append(" file(s) using DataMover=").append(ar.server.getName()).toString());
                            // Let's update the progress on the fly for each new entry!
                            _update(progress.insert(0, getCurrentTime()));
                        } else {
                            // Couldn't connect to the remote host to list the files
                            _add(LINE_SEPARATOR);
                            _add("err:" + progress
                                    .append(ar.message).append(ar.server != null
                                            ? " using DataMover=" + ar.server.getName() : " (no DataMover available)")
                                    .toString());
                            _add(LINE_SEPARATOR);
                            // Let's update the progress on the fly for each new entry!
                            _update(progress.insert(0, getCurrentTime()).insert(0, "err:"));
                        }
                    }
                    final var fullDuration = Format.formatDuration(System.currentTimeMillis() - start);
                    if (!interrupted) {
                        // Completed, let's give some information about the overall process!
                        final String message;
                        if (listingCount == 0) {
                            message = "No directory to process, please review the configuration";
                            _log.warn(message);
                            warn(message);
                            _add("err:" + message);
                        } else {
                            message = "Completed " + listingCount + " listing(s) in " + fullDuration;
                            _log.info(message);
                            info(message);
                            _add(message);
                        }
                        completed = true;
                    } else {
                        // The activity was interrupted!
                        final var message = "Acquisition interrupted after " + fullDuration;
                        _log.warn(message);
                        warn(message);
                        _add("err:" + message);
                        if (manager != null)
                            manager.stopRun();
                    }
                    _tmpOut.close();
                    _out.close();
                    if (!_fileOutputDone.getAbsolutePath().equals(_fileOutputTmp.getAbsolutePath())) {
                        // Tmp and done file names are different, so we need to move tmp to done!
                        _fileOutputDone.delete();
                        _fileOutputTmp.renameTo(_fileOutputDone);
                    }
                } catch (final Throwable t) {
                    _log.warn("Running AcquisitionThread " + key, t);
                    try {
                        warn(Format.getMessage(t));
                    } catch (final Throwable ignored) {
                    }
                } finally {
                    StreamPlugThread.closeQuietly(scanner);
                    StreamPlugThread.closeQuietly(_tmpOut);
                    StreamPlugThread.closeQuietly(_out);
                    _toRemove.add(key);
                    _log.info("Host " + key + " acquisition: " + completed + (interrupted ? " (interrupted)" : ""));
                }
            }
        }

        /**
         * Gets the current time.
         *
         * @return the current time
         */
        private String getCurrentTime() {
            return "time=" + System.currentTimeMillis() + " ";
        }
    }

    /**
     * The Class DissDownloadScheduler.
     */
    public final class DissDownloadScheduler extends DownloadScheduler {

        /** The _cache ratio. */
        public int _cacheRatio = Cnf.at("Scheduler", "cacheRatio", 40);

        /**
         * Instantiates a new diss download scheduler.
         *
         * @param name
         *            the name
         */
        public DissDownloadScheduler(final String name) {
            super(name,
                    "The DissDownloadScheduler is implementing the retrieval mechanism for the Dissemination files.");
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(), null,
                    new MBeanAttributeInfo[] { new MBeanAttributeInfo("CacheRatio", "int",
                            "CacheRatio: maximum number of DataTransfers retrieved from the Data-base (ratio*maximumDownloadThreads).",
                            true, true, false) },
                    new MBeanOperationInfo[] {});
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("CacheRatio".equals(attributeName)) {
                    return _cacheRatio;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("CacheRatio".equals(name)) {
                _cacheRatio = (Integer) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Gets the data transfers to download iterator.
         *
         * @return the data transfers to download iterator
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public DBIterator<DataTransfer> getDataTransfersToDownloadIterator() throws IOException, SQLException {
            return getECpdsBase()
                    .getDisseminationDataTransfersToDownloadIterator(_cacheRatio * getMaxDownloadThreads());
        }
    }

    /**
     * The Class AcqDownloadScheduler.
     */
    public final class AcqDownloadScheduler extends DownloadScheduler {

        /** The _cache ratio. */
        public int _maxPresetPerDestination = Cnf.at("Scheduler", "maxPresetPerDestination", 60);

        /**
         * Instantiates a new acq download scheduler.
         *
         * @param name
         *            the name
         */
        public AcqDownloadScheduler(final String name) {
            super(name, "The AcqDownloadScheduler is implementing the retrieval mechanism for the Acquisition files.");
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(), null,
                    new MBeanAttributeInfo[] { new MBeanAttributeInfo("MaxPresetPerDestination", "int",
                            "MaxPresetPerDestination: maximum number of DataTransfers retrieved from the Data-base per Destination.",
                            true, true, false) },
                    new MBeanOperationInfo[] {});
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("MaxPresetPerDestination".equals(attributeName)) {
                    return _maxPresetPerDestination;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("MaxPresetPerDestination".equals(name)) {
                _maxPresetPerDestination = (Integer) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Gets the data transfers to download iterator.
         *
         * @return the data transfers to download iterator
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws SQLException
         *             the SQL exception
         */
        @Override
        public DBIterator<DataTransfer> getDataTransfersToDownloadIterator() throws IOException, SQLException {
            return getECpdsBase().getAcquisitionDataTransfersToDownloadIterator(_maxPresetPerDestination);
        }
    }

    /**
     * The Class DownloadScheduler.
     */
    public abstract class DownloadScheduler extends MBeanScheduler {

        /** The _max download threads. */
        private int _maxDownloadThreads = Cnf.at("Scheduler", "maxDownloadThreads", 60);

        /** The _maximum duration. */
        private long _maximumDuration = Cnf.durationAt("Scheduler", "maximumDurationDownloadThread",
                10 * Timer.ONE_MINUTE);

        /** The _minimum duration. */
        public long _minimumDuration = Cnf.durationAt("Scheduler", "minimumDurationDownloadThread",
                5 * Timer.ONE_MINUTE);

        /** The _minimum rate. */
        public long _minimumRate = Cnf.at("Scheduler", "minimumRateDownloadThread", 2359296);

        /** The _debug. */
        public boolean _debug = Cnf.at("Scheduler", "debug", false);

        /** The _processChecksum. */
        public boolean _processChecksum = Cnf.at("Scheduler", "processChecksum", true);

        /** The _download threads. */
        final private Map<Long, DownloadThread> _downloadThreads = new ConcurrentHashMap<>();

        /** The _to remove. */
        private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

        /** The _current key. */
        private Long _currentKey = null;

        /** The _pause. */
        private boolean _pause = false;

        /** The _description. */
        private final String _description;

        /**
         * Instantiates a new download scheduler.
         *
         * @param name
         *            the name
         * @param description
         *            the description
         */
        public DownloadScheduler(final String name, final String description) {
            super(name);
            _description = description;
            setDelay(Cnf.durationAt("Scheduler", "downloadScheduler", Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "downloadSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
            setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "downloadSchedulerTimeRanges"));
        }

        /**
         * Gets the data transfers to download iterator.
         *
         * @return the data transfers to download iterator
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws SQLException
         *             the SQL exception
         */
        public abstract DBIterator<DataTransfer> getDataTransfersToDownloadIterator() throws IOException, SQLException;

        /**
         * Sets the max download threads.
         *
         * @param maxDownloadThreads
         *            the new max download threads
         */
        public void setMaxDownloadThreads(final int maxDownloadThreads) {
            _maxDownloadThreads = maxDownloadThreads;
        }

        /**
         * Sets the time out download thread.
         *
         * @param timeOutDownloadThread
         *            the new time out download thread
         */
        public void setTimeOutDownloadThread(final long timeOutDownloadThread) {
            _maximumDuration = timeOutDownloadThread;
        }

        /**
         * Gets the max download threads.
         *
         * @return the max download threads
         */
        public int getMaxDownloadThreads() {
            return _maxDownloadThreads;
        }

        /**
         * Gets the time out download thread.
         *
         * @return the time out download thread
         */
        public long getTimeOutDownloadThread() {
            return _maximumDuration;
        }

        /**
         * Gets the download threads count.
         *
         * @return the download threads count
         */
        public int getDownloadThreadsCount() {
            return _downloadThreads.size() - _toRemove.size();
        }

        /**
         * Gets the activity.
         *
         * @return the activity
         */
        @Override
        public String getActivity() {
            return "lastDataFile=" + (_currentKey == null ? "[none]" : _currentKey) + ",downloadThreads="
                    + _downloadThreads.size();
        }

        /**
         * Gets the key.
         *
         * @param transfer
         *            the transfer
         *
         * @return the string
         */
        private long _getKey(final DataTransfer transfer) {
            return transfer.getDataFileId();
        }

        /**
         * Gets the thread list.
         *
         * @return the thread list
         */
        public String getThreadList() {
            final var status = new StringBuilder();
            final var currentTime = System.currentTimeMillis();
            for (final DownloadThread thread : _downloadThreads.values()) {
                final DataTransfer transfer;
                final long key;
                if (thread != null && !_toRemove.contains(key = _getKey(transfer = thread._transfer))) {
                    final var size = transfer.getDataFile().getSize();
                    status.append((transfer.getDestinationName() + " DataFile " + key + " ("
                            + Format.formatDuration(thread._time, currentTime) + "/"
                            + (size != -1 ? Format.formatPercentage(size, thread.getByteSent())
                                    : Format.formatSize(thread.getByteSent()))
                            + (isNotEmpty(thread._root) ? " on " + thread._root : "") + ")").replace(' ', '_')
                                    .replace('-', '_'))
                            .append(" ");
                }
            }
            return status.toString().trim();
        }

        /**
         * Gets the total number of DataTransfers related to the DataFiles being retrieved.
         *
         * @return the count
         */
        public int getDataTransfersCount() {
            var count = 0;
            for (final DownloadThread thread : _downloadThreads.values()) {
                if (thread != null) {
                    count += thread._dataTransfersCount;
                }
            }
            return count;
        }

        /**
         * Gets the progress interface.
         *
         * @param dataFileId
         *            the data file id
         *
         * @return the progress interface
         */
        public ProgressInterface getProgressInterface(final long dataFileId) {
            final var thread = _downloadThreads.get(dataFileId);
            if (thread != null && thread.isAlive()) {
                // We found it!
                return thread;
            }
            // The retrieval thread was not found or completed!
            return null;
        }

        /**
         * Interrupt all downloads.
         *
         * @param destinationName
         *            the destination name
         * @param message
         *            the message
         */
        public void interruptAllDownload(final String destinationName, final String message) {
            for (final DownloadThread thread : _downloadThreads.values()) {
                if (thread != null && thread.getDestinationName().equals(destinationName)) {
                    interruptDownload(thread._transfer, message);
                }
            }
        }

        /**
         * Interrupt download.
         *
         * @param transfer
         *            the transfer
         * @param message
         *            the message
         *
         * @return true, if successful
         */
        public boolean interruptDownload(final DataTransfer transfer, final String message) {
            return interruptDownload(_getKey(transfer), message);
        }

        /**
         * Interrupt download.
         *
         * @param key
         *            the key
         * @param message
         *            the message
         *
         * @return true, if successful
         */
        public boolean interruptDownload(final long key, final String message) {
            final var thread = _downloadThreads.get(key);
            if (thread != null && thread.isAlive()) {
                try {
                    thread._message = message;
                    thread._time = System.currentTimeMillis();
                    thread.interrupt();
                    return true;
                } catch (final Throwable t) {
                    _log.warn("Interrupting DownloadThread", t);
                }
            }
            return false;
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return MBeanManager.addMBeanInfo(super.getMBeanInfo(), _description, new MBeanAttributeInfo[] {
                    new MBeanAttributeInfo("Debug", "boolean", "Debug: display more debug logs.", true, true, false),
                    new MBeanAttributeInfo("ProcessChecksum", "boolean",
                            "ProcessChecksum: generate checksum for downloaded files.", true, true, false),
                    new MBeanAttributeInfo("ThreadSize", "int", "ThreadSize: number of download thread(s).", true,
                            false, false),
                    new MBeanAttributeInfo("ThreadList", "java.lang.String", "ThreadList: list of download thread(s).",
                            true, false, false),
                    new MBeanAttributeInfo("DataTransfersCount", "int",
                            "DataTransfersCount: number of DataTransfer(s) related to the DataFile(s) retrieved.", true,
                            false, false),
                    new MBeanAttributeInfo("MaximumDownloadThreads", "int",
                            "MaximumDownloadThreads: maximum number of parallel transfers.", true, true, false),
                    new MBeanAttributeInfo("DefaultMaximumDuration", "long",
                            "DefaultMaximumDuration: maximum duration for a transfer in milliseconds.", true, true,
                            false),
                    new MBeanAttributeInfo("DefaultMinimumDuration", "long",
                            "DefaultMinimumDuration: minimum duration for a transfer in milliseconds.", true, true,
                            false),
                    new MBeanAttributeInfo("DefaultMinimumRate", "long",
                            "DefaultMinimumRate: minimum rate for a transfer in bytes/s.", true, true, false) },
                    new MBeanOperationInfo[] { new MBeanOperationInfo("closeIncomingConnection",
                            "interruptDownload(id): interrupt download for the specified DataFile",
                            new MBeanParameterInfo[] {
                                    new MBeanParameterInfo("id", "java.lang.Integer", "DataFile id") },
                            "java.lang.Boolean", MBeanOperationInfo.ACTION) });
        }

        /**
         * Invoke.
         *
         * @param operationName
         *            the operation name
         * @param params
         *            the params
         * @param signature
         *            the signature
         *
         * @return the object
         *
         * @throws NoSuchMethodException
         *             the no such method exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object invoke(final String operationName, final Object[] params, final String[] signature)
                throws NoSuchMethodException, MBeanException {
            try {
                if ("closeIncomingConnection".equals(operationName) && signature.length == 1
                        && "java.lang.Integer".equals(signature[0])) {
                    return interruptDownload((Integer) params[0], "Manual interruption from JMX interface");
                }
            } catch (final Exception e) {
                _log.warn("Invoking the {} MBean method", operationName, e);
                throw new MBeanException(e);
            }
            return super.invoke(operationName, params, signature);
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("Debug".equals(attributeName)) {
                    return _debug;
                }
                if ("ProcessChecksum".equals(attributeName)) {
                    return _processChecksum;
                }
                if ("DataTransfersCount".equals(attributeName)) {
                    return getDataTransfersCount();
                }
                if ("ThreadSize".equals(attributeName)) {
                    return _downloadThreads.size();
                }
                if ("ThreadList".equals(attributeName)) {
                    return getThreadList();
                }
                if ("MaximumDownloadThreads".equals(attributeName)) {
                    return _maxDownloadThreads;
                }
                if ("DefaultMaximumDuration".equals(attributeName)) {
                    return _maximumDuration;
                }
                if ("DefaultMinimumDuration".equals(attributeName)) {
                    return _minimumDuration;
                }
                if ("DefaultMinimumRate".equals(attributeName)) {
                    return _minimumRate;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            return super.getAttribute(attributeName);
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            if ("MaximumDownloadThreads".equals(name)) {
                _maxDownloadThreads = (Integer) value;
                return true;
            }
            if ("DefaultMaximumDuration".equals(name)) {
                _maximumDuration = (Long) value;
                return true;
            }
            if ("DefaultMinimumDuration".equals(name)) {
                _minimumDuration = (Long) value;
                return true;
            }
            if ("DefaultMinimumRate".equals(name)) {
                _minimumRate = (Long) value;
                return true;
            }
            if ("Debug".equals(name)) {
                _debug = (Boolean) value;
                return true;
            }
            if ("ProcessChecksum".equals(name)) {
                _processChecksum = (Boolean) value;
                return true;
            }
            return super.setAttribute(name, value);
        }

        /**
         * Sets the pause.
         *
         * @param pause
         *            the new pause
         */
        public void setPause(final boolean pause) {
            _pause = pause;
        }

        /**
         * Gets the pause.
         *
         * @return the pause
         */
        public boolean getPause() {
            return _pause;
        }

        /**
         * Checks if is acquisition.
         *
         * @param hostsPerDestination
         *            the hosts per destination
         * @param transfer
         *            the transfer
         *
         * @return true, if successful
         *
         * @throws DataBaseException
         *             the data base exception
         */
        private boolean _isAcquisition(final HashMap<String, Collection<Host>> hostsPerDestination,
                final DataTransfer transfer) throws DataBaseException {
            final var destination = transfer.getDestination();
            if (destination.getAcquisition()) {
                // Was this DataTransfer submitted by the Acquisition process?
                final var hostName = transfer.getDataFile().getHostForAcquisitionName();
                if (hostName != null) {
                    // Is it a DataTransfer Alias or was it submitted through
                    // its original Destination?
                    final var destinationName = destination.getName();
                    var hosts = hostsPerDestination.get(destinationName);
                    if (hosts == null) {
                        hostsPerDestination.put(destinationName,
                                hosts = getECpdsBase().getHostsByDestinationId(destinationName));
                    }
                    for (final Host host : hosts) {
                        if (HostOption.ACQUISITION.equals(host.getType()) && hostName.equals(host.getName())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            final var start = System.currentTimeMillis();
            var processed = 0;
            try (var it = getDataTransfersToDownloadIterator()) {
                final var hostsPerDestination = new HashMap<String, Collection<Host>>();
                while (!_pause && isRunning() && it.hasNext()) {
                    try {
                        // Let's go through every selected DataTransfer from the database
                        final var transfer = it.next();
                        final var destination = transfer.getDestination();
                        final var isAcquisition = _isAcquisition(hostsPerDestination, transfer);
                        // May be the DataTransfer was already retrieved?
                        if (transfer.getDataFile().getDownloaded()) {
                            _log.debug("DataTransfer " + transfer.getId() + " already downloaded");
                            continue;
                        }
                        // Check if this DataTransfer is not currently processed on the MasterServer? If
                        // it is the case then we should delay the retrieval!
                        final var fromCache = getDataTransferFromCache(transfer.getId());
                        if (fromCache != null) {
                            final var code = fromCache.getStatusCode();
                            if (!StatusFactory.WAIT.equals(code) && !StatusFactory.RETR.equals(code)
                                    && !StatusFactory.HOLD.equals(code)) {
                                if (_debug) {
                                    _log.debug("Delay Download for DataTransfer " + transfer.getId() + ": " + code
                                            + " (in cache)");
                                }
                                continue;
                            }
                        }
                        // If we exceed the maximum number of parallel
                        // retrievals then we stop there!
                        if (getDownloadThreadsCount() >= _maxDownloadThreads) {
                            if (_debug) {
                                _log.debug(
                                        "Maximum number of parallel retrieval reached (" + _maxDownloadThreads + ")");
                            }
                            break;
                        }
                        // If this is the acquisition then we want to check the number of parallel
                        // connections just in case there would be a limit on the Destination?
                        var goToNext = false;
                        if (isAcquisition) {
                            // This is the acquisition!
                            final var limit = destination.getMaxConnections();
                            if (limit >= 0) {
                                // We definitely have a limit!
                                final var name = destination.getName();
                                var count = 0;
                                for (final DownloadThread thread : _downloadThreads.values()) {
                                    if (name.equals(thread._transfer.getDestinationName()) && ++count >= limit) {
                                        // We have already the maximum number of retrievals for this Destination, so
                                        // let's delay the retrieval!
                                        if (_debug) {
                                            _log.debug("Delay Download for DataTransfer " + transfer.getId()
                                                    + " (max connections is " + count + " for " + name + ")");
                                        }
                                        goToNext = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            // This is not an Acquisition Destination!
                            final var dataFile = transfer.getDataFile();
                            // This file was queued by an acquisition
                            // Destination and is therefore an Alias. The
                            // DataFile will be retrieved with the original
                            // DataTransfer, not this one. This will allow
                            // to implement the limit above. However if the
                            // file was already retrieved on the data mover
                            // then we can process it!
                            if ((dataFile.getHostForAcquisitionName() != null) && !dataFile.getDownloaded()) {
                                if (_debug) {
                                    _log.debug("Delay Download for DataTransfer " + transfer.getId()
                                            + " (Acquisition Destination must retrieve the file first)");
                                }
                                continue;
                            }
                        }
                        if (goToNext) {
                            continue;
                        }
                        // Is it processed in the ECpdsPlugin?
                        if (transferIsLocked(transfer.getUniqueKey())) {
                            if (_debug) {
                                _log.debug("DataTransfer " + transfer.getId() + " is locked");
                            }
                            continue;
                        }
                        // We now have to check if this transfer is not already
                        // processed by this scheduler!
                        synchronized (_downloadThreads) {
                            if (!_downloadThreads.containsKey(_currentKey = _getKey(transfer))) {
                                final var thread = new DownloadThread(transfer);
                                thread.setThreadNameAndCookie(null, null, "DataFile-" + transfer.getDataFileId(), null);
                                thread.execute(true);
                                _downloadThreads.put(_currentKey, thread);
                                processed++;
                            } else if (_debug) {
                                _log.debug("Download already in progress for DataTransfer " + transfer.getId());
                            }
                        }
                    } catch (final Throwable t) {
                        _log.warn("nextStep", t);
                    }
                }
                // We now check if some DownloadThread have not completed and
                // have requested to be removed from the cache?
                synchronized (_toRemove) {
                    for (final long key : _toRemove) {
                        final var thread = _downloadThreads.remove(key);
                        _log.debug("DownloadThread " + key + " removed (" + (thread != null) + ")");
                    }
                    _toRemove.clear();
                }
                // If it took more than 1m then log it!
                final var duration = System.currentTimeMillis() - start;
                if (_log.isDebugEnabled() && duration > 60000) {
                    _log.debug("Step completed in {}: {} DataFile(s) processed for download",
                            Format.formatDuration(duration), processed);
                }
            } catch (final Throwable t) {
                _log.warn("Error in scheduler", t);
            }
            // If there are still files to be processed then there is no reason
            // to wait!
            return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
        }

        /**
         * The Class DownloadThread.
         */
        private final class DownloadThread extends ConfigurableRunnable implements ProgressInterface {
            /** The _time. */
            private long _time = System.currentTimeMillis();

            /** The _transfer. */
            private DataTransfer _transfer = null;

            /** The source Host which is used for the retrieval. */
            private Host _source = null;

            /**
             * Used to indicate the interruption of the thread when the transmission takes too long.
             */
            private String _message = null;

            /** The _transfer server name. */
            private String _root = null;

            /** The _byte sent. */
            private long _byteSent = 0;

            /** The number of data transfers related to this DataFile. */
            private int _dataTransfersCount = 1;

            /**
             * Instantiates a new download thread.
             *
             * @param transfer
             *            the transfer
             */
            private DownloadThread(final DataTransfer transfer) {
                _transfer = transfer;
            }

            /**
             * Gets the data file id.
             *
             * @return the data file id
             */
            @Override
            public long getDataFileId() {
                return _transfer.getDataFileId();
            }

            /**
             * Gets the destination name.
             *
             * @return the destination name
             */
            /*
             * Allow getting the Destination for this retrieval.
             */
            public String getDestinationName() {
                return _transfer.getDestinationName();
            }

            /**
             * Update.
             *
             * @param root
             *            the root
             * @param byteSent
             *            the byte sent
             */
            @Override
            public void update(final String root, final long byteSent) {
                // If the dataFile has been fully downloaded then we set the
                // bytes sent value to the size of the DataFile.
                final var dataFile = _transfer.getDataFile();
                final var size = dataFile.getSize();
                _byteSent = dataFile.getDownloaded() && size != -1 ? size : byteSent;
                _root = root;
            }

            /**
             * Gets the byte sent.
             *
             * @return the byte sent
             */
            @Override
            public long getByteSent() {
                // If the dataFile has been fully downloaded then we always
                // return the size, otherwise we return the last bytes sent
                // value that we have.
                final var dataFile = _transfer.getDataFile();
                final var size = dataFile.getSize();
                return dataFile.getDownloaded() && size != -1 ? size : _byteSent;
            }

            /**
             * Gets the root.
             *
             * @return the root
             */
            @Override
            public String getRoot() {
                return _root;
            }

            /**
             * Configurable run.
             */
            @Override
            public void configurableRun() {
                var complete = false;
                final var key = _getKey(_transfer);
                final List<DataTransfer> relatedTransfers = new ArrayList<>();
                final var base = getDataBase(ECpdsBase.class);
                final var originalStatus = _transfer.getStatusCode();
                _log.debug("Starting DownloadThread {}", key);
                try {
                    final var start = System.currentTimeMillis();
                    final var dataFile = _transfer.getDataFile();
                    final var dataFileId = dataFile.getId();
                    final var hostForSourceName = dataFile.getHostForAcquisitionName();
                    final var acquisition = isNotEmpty(hostForSourceName);
                    if (acquisition) {
                        _log.debug("Registered by the Acquisition Thread");
                        _source = base.getHost(hostForSourceName);
                    } else {
                        _source = _transfer.getDestination().getHostForSource();
                    }
                    // Get all the DataTransfers related to this DataFile!
                    relatedTransfers.addAll(base.getDataTransfersByDataFileId(dataCache, dataFileId));
                    _dataTransfersCount = relatedTransfers.size();
                    // Add a new history for every DataTransfer related to this
                    // DataFile to inform about the start of the retrieval and
                    // update the status to FETC!
                    final var comment = " using Host=" + _source.getName() + " (" + _source.getNickname()
                            + ") from Destination=" + _transfer.getDestinationName();
                    for (final DataTransfer transfer : relatedTransfers) {
                        transfer.setComment("Starting retrieval" + comment);
                        final var status = StatusFactory.FETC;
                        transfer.setStatusCode(status);
                        // Make sure that when we update the common dataFile it
                        // is propagated to every transfer!
                        transfer.setDataFile(dataFile);
                        base.update(transfer);
                        addTransferHistory(transfer, _source, transfer.getStatusCode(), transfer.getComment(), false);
                    }
                    // Shall we generate a checksum?
                    dataFile.setChecksum(_processChecksum ? null : "none");
                    final var dr = TransferScheduler.download(_transfer, _source, relatedTransfers);
                    final var completed = System.currentTimeMillis();
                    _transfer = base.getDataTransfer(_transfer.getId());
                    final var server = dr.server;
                    if (server != null && dr.complete) {
                        // Do we have a checksum?
                        final var checksum = dr.dataFile.getChecksum();
                        // The DataFile was successfully retrieved from the
                        // remote site!
                        final var remoteHost = dr.dataFile.getRemoteHost();
                        dataFile.setRemoteHost(remoteHost);
                        dataFile.setGetHost(dr.dataFile.getGetHost());
                        dataFile.setGetTime(dr.dataFile.getGetTime());
                        dataFile.setGetCompleteDuration(completed - start);
                        dataFile.setGetDuration(dr.dataFile.getGetDuration());
                        dataFile.setSize(dr.dataFile.getSize());
                        dataFile.setChecksum(checksum);
                        dataFile.setDownloaded(true);
                        base.update(dataFile);
                        for (final DataTransfer transfer : relatedTransfers) {
                            // First let's add a new history with the transfer rate!
                            final var index = dataFile.getIndex();
                            transfer.setComment("Retrieved " + (index > 0 ? index : 1) + " file(s)"
                                    + (isNotEmpty(remoteHost) ? " from " + remoteHost : "") + " on DataMover="
                                    + dataFile.getGetHost() + " ("
                                    + Format.getMBitsPerSeconds(dataFile.getSize(), dataFile.getGetDuration())
                                    + " Mbits/s)");
                            addTransferHistory(transfer, _source, transfer.getStatusCode(), transfer.getComment(),
                                    false);
                            // Do we have to store it in Splunk?
                            if (_splunk.isInfoEnabled()) {
                                final var destination = transfer.getDestination();
                                final var ecauthHost = dataFile.getEcauthHost();
                                final var ecauthUser = dataFile.getEcauthUser();
                                final var login = _source.getLogin();
                                final var host = _source.getHost();
                                _splunk.info(
                                        "RET;{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{}",
                                        "Monitored=" + destination.getMonitor(), "DataTransferId=" + transfer.getId(),
                                        "DestinationName=" + destination.getName(),
                                        "DestinationType=" + DestinationOption.getLabel(destination.getType()),
                                        "FileName=" + dataFile.getOriginal(), "FileSize=" + dataFile.getSize(),
                                        "Identity=" + transfer.getIdentity(),
                                        "ScheduledTime=" + transfer.getScheduledTime(),
                                        "StartTime=" + dataFile.getGetTime(),
                                        "MetaStream=" + nullToNone(dataFile.getMetaStream()),
                                        "MetaType=" + nullToNone(dataFile.getMetaType()),
                                        "MetaTime=" + dataFile.getMetaTime(), "TimeBase=" + dataFile.getTimeBase(),
                                        "TimeStep=" + dataFile.getTimeStep(),
                                        "Duration=" + dataFile.getGetCompleteDuration(), "HostId=" + _source.getName(),
                                        "HostLogin=" + (isNotEmpty(login) ? login : "-"),
                                        "HostAddress=" + (isNotEmpty(host) ? host : "-"),
                                        "TransferProtocol=" + _source.getTransferMethod().getECtransModuleName(),
                                        "MoverName=" + server.getName(),
                                        "UserId=" + (isNotEmpty(ecauthUser) ? ecauthUser : "-"),
                                        "RequestAddress=" + (isNotEmpty(ecauthHost) ? ecauthHost : "-"),
                                        "DataOnlyDuration=" + dataFile.getGetDuration(),
                                        "StandBy=" + dataFile.getStandby(), "RemoteHost=" + nullToNone(remoteHost));
                            }
                            // If this is the acquisition then the Status of the
                            // DataTransfer should be set to WAIT otherwise it
                            // would be set to FAIL by the transfer scheduler.
                            // Cause we don't allow retries on retrieval for the
                            // acquisition!
                            final var target = transfer.getTarget();
                            final var destination = transfer.getDestination();
                            final var setup = DESTINATION_SCHEDULER.getECtransSetup(destination.getData());
                            final var forceOptions = setup.getOptions(DESTINATION_SCHEDULER_FORCE, target, null);
                            final boolean standby;
                            if ("never".equalsIgnoreCase(setup.getString(DESTINATION_SCHEDULER_STANDBY))
                                    || "never".equalsIgnoreCase(forceOptions.get("standby", ""))
                                            && forceOptions.matches("pattern", target, ".*")
                                            && !forceOptions.matches("ignore", target)) {
                                _log.debug("WAIT forced by scheduler option in Destination");
                                standby = false;
                            } else if (setup.getBoolean(DESTINATION_SCHEDULER_STANDBY)
                                    || forceOptions.get("standby", false)
                                            && forceOptions.matches("pattern", target, ".*")
                                            && !forceOptions.matches("ignore", target)) {
                                _log.debug("Standby forced by scheduler option in Destination");
                                standby = true;
                            } else {
                                standby = dataFile.getStandby();
                            }
                            final var currentStatus = standby ? StatusFactory.HOLD
                                    : acquisition || dataFile.getFileInstance() == null ? StatusFactory.WAIT
                                            : StatusFactory.RETR;
                            transfer.setStatusCode(currentStatus);
                            // If this is the experimental ECPDS then all the transfers to a stop
                            // destination will be stopped!
                            final var forceStop = !Cnf.at("ECpdsPlugin", "failOnDestinationNotFound", true)
                                    && setup.getBoolean(DESTINATION_SCHEDULER_FORCE_STOP)
                                    && StatusFactory.STOP.equals(destination.getStatusCode());
                            if (forceStop) {
                                transfer.setStatusCode(StatusFactory.STOP);
                                transfer.setComment("Not scheduled (destination is stopped)");
                            } else if (StatusFactory.HOLD.equals(currentStatus)) {
                                transfer.setComment("Not scheduled yet (manual queuing is required)");
                                transfer.setNotify(true);
                            } else {
                                transfer.setComment("Scheduled for "
                                        + (transfer.getAsap() ? "as soon as possible" : "no sooner than " + Format
                                                .formatTime("MMM dd HH:mm:ss", transfer.getScheduledTime().getTime())));
                            }
                            transfer.setTransferServer(server);
                            transfer.setTransferServerName(server.getName());
                            transfer.setOriginalTransferServer(server);
                            transfer.setOriginalTransferServerName(server.getName());
                            transfer.setReplicateTime(null);
                            transfer.setReplicated(false);
                            transfer.setReplicateCount(0);
                            transfer.setSize(dr.dataFile.getSize());
                            transfer.setDataFile(dataFile);
                            transfer.setDataFileId(dataFileId);
                            if (StatusFactory.RETR.equals(currentStatus)) {
                                // The update does not write an history if the
                                // status is RETR.
                                addTransferHistory(transfer);
                            }
                            theTransferRepository.update(transfer);
                            if (setup.getBoolean(DESTINATION_SCHEDULER_RESET_QUEUE_ON_CHANGE)) {
                                reloadDestination(transfer);
                            }
                        }
                        _log.info("DataFile " + dataFileId + " retrieved successfully from " + dataFile.getGetHost()
                                + ": " + Format.formatRate(dataFile.getSize(), dataFile.getGetDuration())
                                + (checksum != null ? " (checksum: " + checksum + ")" : ""));
                    } else {
                        // The retrieval process failed!
                        if (_message == null) {
                            // There is not initial message so let's see if we
                            // had an error from the download process?
                            if (dr.message == null) {
                                // No messages from anywhere?
                                _message = "Retrieval failed on all data movers";
                            } else {
                                // Let's display the error we had during the
                                // download!
                                _message = dr.message;
                            }
                        }
                        _log.warn("DataFile " + dataFileId + ": " + _message);
                        // Let's reset the checksum!
                        dataFile.setChecksum(null);
                        // We have to process every related DataTransfer to be
                        // sure the DataFile will not be retried before the
                        // time has expired with another Destination (when we
                        // have aliases)!
                        final var currentTime = new Timestamp(System.currentTimeMillis());
                        for (final DataTransfer transfer : relatedTransfers) {
                            transfer.setReplicateTime(currentTime);
                            transfer.setReplicated(false);
                            transfer.setReplicateCount(transfer.getReplicateCount() + 1);
                            transfer.setComment(_message);
                            addTransferHistory(transfer, _source, StatusFactory.STOP, _message, true);
                            final String currentStatus;
                            if (acquisition) {
                                // If this is the acquisition then the Status of
                                // the DataTransfer should be set to FAIL
                                // otherwise it would be retried (the FAIL
                                // status will be set in the Transfer
                                // Scheduler)!
                                if (HOST_ACQUISITION.getECtransSetup(_source.getData())
                                        .getBoolean(HOST_ACQUISITION_REQUEUE_ON_FAILURE)) {
                                    currentStatus = StatusFactory.SCHE;
                                } else {
                                    currentStatus = StatusFactory.RETR;
                                }
                            } else if (Cnf.at("Other", "dontRetryDownloads", false)) {
                                // We don't want to retry the download of the
                                // files which are not on the super computer any
                                // more (this is for the Dissemination only)!
                                currentStatus = StatusFactory.FAIL;
                            } else {
                                // Let's put it back to SCHE so that the
                                // retrieval will be restarted later on!
                                currentStatus = StatusFactory.SCHE;
                            }
                            transfer.setStatusCode(currentStatus);
                            // No need to add history, it will be done in the
                            // transfer scheduler!
                            base.update(transfer);
                        }
                    }
                    complete = dr.complete;
                } catch (final Throwable t) {
                    _log.warn("Running DownloadThread " + key, t);
                    // If there was a problem then we have to make sure the
                    // DataTransfer is rescheduled for retrieval and to add the
                    // proper error message in the history!
                    final var currentTime = new Timestamp(System.currentTimeMillis());
                    final var message = "Retrieval failed - " + Format.getMessage(t);
                    for (final DataTransfer transfer : relatedTransfers) {
                        transfer.setReplicateTime(currentTime);
                        transfer.setReplicated(false);
                        transfer.setReplicateCount(transfer.getReplicateCount() + 1);
                        transfer.setComment(message);
                        addTransferHistory(transfer, _source, StatusFactory.STOP, message, true);
                        transfer.setStatusCode(originalStatus);
                        // If we can't update the DataBase then we give up :-(
                        base.tryUpdate(transfer);
                    }
                } finally {
                    _toRemove.add(key);
                    _log.info("DataFile " + key + " downloaded: " + complete);
                }
            }
        }

    }
}
