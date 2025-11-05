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

package ecmwf.ecpds.master.plugin.ecpds;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_SCHEDULER;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_ASAP;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_DATE_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_DELAY;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_FORCE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_FORCE_STOP;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_LIFETIME;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_NO_RETRIEVAL;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_STANDBY;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_TRANSFERGROUP;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_VERSION;
import static ecmwf.common.text.Util.isNotEmpty;
import static ecmwf.common.text.Util.nullToNone;
import static ecmwf.ecpds.master.DataFilePath.getPath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.IncomingHistory;
import ecmwf.common.database.MetadataAttribute;
import ecmwf.common.database.MetadataValue;
import ecmwf.common.database.MonitoringValue;
import ecmwf.common.database.ProductStatus;
import ecmwf.common.database.TransferServer;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.plugin.SimplePlugin;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.GenericFile;
import ecmwf.common.technical.Synchronized;
import ecmwf.common.technical.ThreadService;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.common.version.Version;
import ecmwf.ecpds.master.AttachmentAccessTicket;
import ecmwf.ecpds.master.DataTransferEvent;
import ecmwf.ecpds.master.MasterServer;
import ecmwf.ecpds.master.MasterServer.DownloadScheduler;
import ecmwf.ecpds.master.MoverAccessTicket;
import ecmwf.ecpds.master.ProductStatusEvent;
import ecmwf.ecpds.master.ProgressInterface;
import ecmwf.ecpds.master.ResetProductEvent;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsCompleted;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsExpected;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsPut;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsSelect;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsStarted;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsWaitForGroup;
import ecmwf.ecpds.master.transfer.AliasesParser;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferManagement;
import ecmwf.ecpds.master.transfer.TransferServerProvider;

/**
 * The Class ECpdsPlugin.
 */
public final class ECpdsPlugin extends SimplePlugin implements ProgressInterface {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECpdsPlugin.class);

    /** The Constant _splunk. */
    private static final Logger _splunk = LogManager.getLogger("SplunkLogs");

    /** The Constant NAME. */
    private static final String NAME = "ECpdsPlugin";

    /** The DEFAULT_DATE_FORMAT. */
    private static final String DEFAULT_DATE_FORMAT = "MMM dd HH:mm:ss";

    /** The PRODUCT_DATE_FORMAT. */
    private static final String PRODUCT_DATE_FORMAT = "yyyyMMdd";

    /** The Constant LOG_REQUESTS. */
    private static final boolean LOG_REQUESTS = Cnf.at("RequestParser", "logRequests", false);

    /** The Constant FORCE_STANDBY. */
    private static final boolean FORCE_STANDBY = Cnf.at("ECpdsPlugin", "forceStandby", false);

    /** The Constant WAIT_FOR_RELOAD_INTERVAL. */
    private static final long WAIT_FOR_RELOAD_INTERVAL = Cnf.durationAt("ECpdsPlugin", "waitForReloadInterval",
            5 * Timer.ONE_SECOND);

    /** The Constant WAIT_FOR_REFRESH_INTERVAL. */
    private static final long WAIT_FOR_REFRESH_INTERVAL = Cnf.durationAt("ECpdsPlugin", "waitForRefreshInterval",
            15 * Timer.ONE_SECOND);

    /** The Constant WAIT_FOR_INITIAL_WAIT. */
    private static final long WAIT_FOR_INITIAL_WAIT = Cnf.durationAt("ECpdsPlugin", "waitForInitialWait",
            25 * Timer.ONE_SECOND);

    /** The Constant MAX_SOURCE_NAME_LENGTH. */
    private static final int MAX_SOURCE_NAME_LENGTH = Cnf.at("ECpdsPlugin", "maxSourceNameLength", 255);

    /** The Constant DEFAULT_APPLICATION. */
    private static final String DEFAULT_APPLICATION = Cnf.at("ECpdsPlugin", "defaultApplication", "pgen");

    /** The Constant resetDataTransferSchedulesByGroup. */
    private static boolean resetDataTransferSchedulesByGroup = Cnf.at("ECpdsPlugin",
            "resetDataTransferSchedulesByGroup", false);

    /** The Constant MASTER. */
    private static final MasterServer MASTER = StarterServer.getInstance(MasterServer.class);

    /** The Constant DATABASE. */
    private static final ECpdsBase DATABASE = MASTER.getECpdsBase();

    /** The Constant groupThread. */
    private static final DataFilesByGroupThread groupThread = new DataFilesByGroupThread();

    /** The Constant metadataAttributeMutexProvider. */
    private static final Synchronized metadataAttributeMutexProvider = new Synchronized();

    /** The Constant productStatusMutexProvider. */
    private static final Synchronized productStatusMutexProvider = new Synchronized();

    /** The metaDataList. */
    private final Map<String, String> metaDataList = new HashMap<>();

    /** The currentTransfer. */
    private DataTransfer currentTransfer = null;

    /** The datafileToDelete. */
    private DataFile datafileToDelete = null;

    /** The transfersList. */
    private DataTransfer[] transfersList = null;

    /** The errorMessage. */
    private String errorMessage = null;

    /** The userName. */
    private String userName = null;

    /** The remoteIp. */
    private String remoteIp = null;

    /** The caller. */
    private String caller = null;

    /** The message. */
    private String message = null;

    /** The selectedDestination. */
    private String selectedDestination = null;

    /** The source. */
    private String source = null;

    /** The currentTarget. */
    private String currentTarget = null;

    /** The identity. */
    private String identity = null;

    /** The original. */
    private String original = null;

    /** The hostForAcquisition. */
    private String hostForAcquisition = null;

    /** The requestId. */
    private long requestId = -1;

    /** The dataFileId. */
    private long dataFileId = -1;

    /** The currentRoot. */
    private String currentRoot = null;

    /** The currentByteSent. */
    private long currentByteSent = 0;

    /** The currentSize. */
    private long currentSize = -1;

    /** The currentPriority. */
    private int currentPriority = 99;

    /** The lifeTime. */
    private long lifeTime = 2 * Timer.ONE_DAY;

    /** The lifeTimeString. */
    private String lifeTimeString = "2d";

    /** The currentDelay. */
    private long currentDelay = 0;

    /** The at. */
    private long at = -1;

    /** The atString. */
    private String atString = null;

    /** The timeCritical. */
    private boolean timeCritical = false;

    /** The standBy. */
    private boolean standBy = false;

    /** The currentIndex. */
    private int currentIndex = 0;

    /** The groupBy. */
    private String groupBy = null;

    /** The remove. */
    private boolean remove = false;

    /** The reQueue. */
    private boolean reQueue = false;

    /** The force. */
    private boolean force = false;

    /** The noRetrieval. */
    private boolean noRetrieval = false;

    /** The failedOnly. */
    private boolean failedOnly = false;

    /** The dateFormat. */
    private String dateFormat = "yyyyMMddHHmmss";

    /** The transferGroup. */
    private String transferGroup = Cnf.at("ECpdsPlugin", "transferGroup");

    /** The checkCluster. */
    private final boolean checkCluster = Cnf.at("ECpdsPlugin", "checkCluster", true);

    /** The purge. */
    private boolean purge = false;

    /** The acquisition. */
    private boolean acquisition = false;

    /** The timeStep. */
    private long timeStep = -1;

    /** The currentBuffer. */
    private long currentBuffer = 0;

    /** The metaData. */
    private String metaData = null;

    /** The metaStream. */
    private String metaStream = null;

    /** The metaTime. */
    private String metaTime = null;

    /** The metaTarget. */
    private String metaTarget = null;

    /** The metaType. */
    private String metaType = null;

    /** The uniqueName. */
    private String uniqueName = null;

    /** The domain. */
    private String domain = "";

    /** The from. */
    private String from = null;

    /** The currentProductDate. */
    private long currentProductDate = -1;

    /** The timeFile. */
    private long timeFile = -1;

    /** The timeFileString. */
    private String timeFileString = null;

    /** The _new data file. */
    private boolean newDataFile = false;

    /** The key. */
    private String key = null;

    /** The start. */
    private final long start = System.currentTimeMillis();

    /** The currentStreams. */
    private int currentStreams = -1;

    /** The currentTimeout. */
    private long currentTimeout = -1;

    /** The version. */
    private String version = null;

    /** The currentAsap. */
    private boolean currentAsap = false;

    /** The currentEvent. */
    private boolean currentEvent = false;

    /** The processMetadata. */
    private boolean processMetadata = true;

    /** The recordUploadHistory. */
    private boolean recordUploadHistory = false;

    /** The provider. */
    private TransferServerProvider provider = null;

    /** The putReq. */
    private ECpdsPut putReq = null;

    /**
     * Make sure the RequestParser is initialized if required!
     */
    static {
        groupThread.setPriority(Thread.MIN_PRIORITY);
        groupThread.execute();
        final var dir = Cnf.at("RequestParser", "parserDir", null);
        if (dir != null && Cnf.at("RequestParser", "useParser", false)) {
            new ECpdsParser(Cnf.at("RequestParser", "lastTimeStamp", -1L), Cnf.at("RequestParser", "ratio", 1L), dir,
                    Cnf.at("RequestParser", "clean", true));
        }
    }

    /**
     * Instantiates a new ecpds plugin.
     *
     * @param name
     *            the name
     * @param params
     *            the params
     * @param socket
     *            the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ECpdsPlugin(final String name, final Map<String, String> params, final Socket socket) throws IOException {
        super(name, params, socket);
        setLoop(true);
    }

    /**
     * Instantiates a new ecpds plugin.
     *
     * @param name
     *            the name
     * @param params
     *            the params
     */
    public ECpdsPlugin(final String name, final Map<String, String> params) {
        super(name, params);
    }

    /**
     * Stop and send an error.
     *
     * @param message
     *            the message
     */
    private void stopAndError(final String message) {
        _log.error("Forward error message to client: {}", message);
        setLoop(false);
        error(message);
    }

    /**
     * Get instance of current class.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     * @param socket
     *            the socket
     *
     * @return the configurable runnable
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public ConfigurableRunnable newInstance(final String ref, final Map<String, String> params, final Socket socket)
            throws IOException {
        return new ECpdsPlugin(ref, params, socket);
    }

    /**
     * Gets the plugin name.
     *
     * @return the plugin name
     */
    @Override
    public String getPluginName() {
        return NAME;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    @Override
    public int getPort() {
        return Integer.parseInt(Cnf.at("ECpdsPlugin", "port"));
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
     * Gets the info.
     *
     * @return the info
     */
    @Override
    public String getInfo() {
        return userName;
    }

    /**
     * Gets the data file id.
     *
     * @return the data file id
     */
    @Override
    public long getDataFileId() {
        return dataFileId;
    }

    /**
     * Gets the root.
     *
     * @return the root
     */
    @Override
    public String getRoot() {
        return currentRoot;
    }

    /**
     * Gets the byte sent.
     *
     * @return the byte sent
     */
    @Override
    public long getByteSent() {
        return currentByteSent;
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
        currentRoot = root;
        currentByteSent = byteSent;
    }

    /**
     * Parses the command.
     *
     * @param command
     *            the command
     *
     * @return the string
     */
    @Override
    public String parseCommand(final String command) {
        if (command.length() == 11 && command.startsWith("T")) {
            try {
                return "ticket " + Long.parseLong(command.substring(1));
            } catch (final NumberFormatException e) {
                // Ignored
            }
        }
        return super.parseCommand(command);
    }

    /**
     * The Class DataFilesByGroupThread.
     */
    private static final class DataFilesByGroupThread extends ConfigurableLoopRunnable {
        /**
         * Instantiates a new data files by group thread.
         */
        DataFilesByGroupThread() {
            setPause(WAIT_FOR_RELOAD_INTERVAL);
        }

        /** The dataFilesByGroupByList. */
        private final Map<String, List<Long>> dataFilesByGroupByList = new ConcurrentHashMap<>();

        /**
         * Gets the data file ids by group by count.
         *
         * @param groupBy
         *            the group by
         *
         * @return the data file ids by group by count
         */
        List<Long> getDataFileIdsByGroupByCount(final String groupBy) {
            final var result = dataFilesByGroupByList.get(groupBy);
            if (result == null) {
                return new ArrayList<>();
            }
            return result;
        }

        /**
         * Configurable loop run.
         */
        @Override
        public void configurableLoopRun() {
            try {
                final var latest = DATABASE.getDataFilesByGroupBy();
                synchronized (dataFilesByGroupByList) {
                    dataFilesByGroupByList.clear();
                    dataFilesByGroupByList.putAll(latest);
                }
            } catch (final Throwable t) {
                _log.warn("Loadind DataFiles by groups", t);
            }
        }
    }

    /**
     * _setup so timeout.
     *
     * @param socket
     *            the socket
     */
    private static void setupSOTimeout(final Socket socket) {
        try {
            socket.setSoTimeout(Cnf.at(NAME, "SocketSoTimeout", 60000));
        } catch (final Throwable t) {
            _log.debug("SoTimeout error", t);
        }
    }

    /**
     * Ticket req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void ticketReq(final String[] parameters) throws ParameterException {
        final var ticket = Long.parseLong(getParameter(parameters));
        _log.debug("Ticket received: {}", ticket);
        final var genericTicket = MASTER.getTicketRepository().get(ticket);
        if (genericTicket != null) {
            if (genericTicket instanceof final AttachmentAccessTicket attachmentTicket) {
                final var name = "AttachmentAccessTicket-" + ticket;
                _log.debug("Processing {}", name);
                ThreadService.setCookieIfNotAlreadySet(name);
                final var file = GenericFile.getGenericFile(attachmentTicket.getFile().getAbsolutePath());
                // Socket socket = getSocket();
                try {
                    final var in = getInputStream();
                    final var out = getOutputStream();
                    out.write("0".getBytes());
                    out.flush();
                    if (attachmentTicket.getMode() == AttachmentAccessTicket.OUTPUT) {
                        _log.debug("OutputTicket");
                        // socket.shutdownOutput();
                        file.receiveFile(in, -1);
                    } else {
                        _log.debug("InputTicket");
                        // socket.shutdownInput();
                        file.transmitFile(out, attachmentTicket.getOffset());
                    }
                    attachmentTicket.completed();
                    setLoop(false);
                } catch (final Throwable t) {
                    final var exceptionMessage = "Processing ticket " + ticket;
                    attachmentTicket.setError(exceptionMessage, t);
                    attachmentTicket.completed();
                    _log.warn(exceptionMessage, t);
                }
            } else if (genericTicket instanceof final MoverAccessTicket moverTicket) {
                final var name = "MoverAccessTicket-" + ticket;
                _log.debug("Processing {}", name);
                ThreadService.setCookieIfNotAlreadySet(name);
                final var socket = getSocket();
                setupSOTimeout(socket);
                moverTicket.toClose(socket);
                try {
                    final var in = getInputStream();
                    final var out = getOutputStream();
                    out.write("0".getBytes());
                    out.flush();
                    // socket.shutdownOutput();
                    final var datafileId = ECpdsClient.put("From data portal", moverTicket.getDestination(),
                            moverTicket.getTarget(), in, moverTicket.getTimeFile(), moverTicket.getTimeBase());
                    moverTicket.setDataFileId(datafileId);
                    moverTicket.completed();
                    setLoop(false);
                } catch (final IOException e) {
                    final var exceptionMessage = "Processing ticket " + ticket;
                    moverTicket.setError(exceptionMessage, e);
                    moverTicket.completed();
                    _log.warn(exceptionMessage, e);
                }
            } else {
                _log.error("Ticket not valid ({})", ticket);
            }
        } else {
            _log.error("Descriptor not found ({})", ticket);
        }
    }

    /**
     * Version req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void versionReq(final String[] parameters) throws ParameterException {
        version = getParameter(parameters);
        // If this is NOT the acquisition then we have to process the metadata to
        // retrieve the metatime, metatarget and metastream which are used to sort the
        // file on the destination display.
        processMetadata = !version.endsWith(" (acquisition)");
        // Only a direct push from the ecpds/mspds command should trigger an incoming
        // history (and Splunk record), not the acquisition or data portal push which
        // are using the ECpdsClient.
        recordUploadHistory = processMetadata && !version.endsWith(" (other)");
    }

    /**
     * User req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParameterException
     *             the parameter exception
     */
    public void userReq(final String[] parameters) throws IOException, ParameterException {
        final var port = getSocket().getPort();
        if (port > 1023) {
            _log.warn("Not a privileged IP port: {}", port);
            if (Cnf.at("ECpdsPlugin", "checkPort", true)) {
                stopAndError("Not a privileged IP port (connection refused by server)");
                return;
            }
        }
        userName = getParameter(parameters);
        remoteIp = getRemoteHost();
        if (from == null) {
            // Can we extract from the version?
            final var m = Pattern.compile("cmd=(.*?),").matcher(version);
            from = "From the " + (m.find() ? m.group(1) : "ecpds") + " command at " + remoteIp;
        }
        if (processMetadata) {
            // This is NOT the acquisition so we need the target, stream and time to be
            // defined in the metadata. Let's set some default values.
            metaDataList.put("target", "None");
            metaDataList.put("stream", "None");
            metaDataList.put("time", "00");
        }
        if ("-".equals(userName)) { // If coming from data portal then no user is specified!
            send("MESSAGE welcome from " + remoteIp);
        } else {
            ThreadService.setCookie(userName);
            send("MESSAGE welcome " + userName + "@" + remoteIp);
        }
    }

    /**
     * Remoteip req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void remoteipReq(final String[] parameters) throws ParameterException {
        // When the remote IP should be set to a different host than the calling
        // one! e.g. when a proxy is used.
        remoteIp = getParameter(parameters);
    }

    /**
     * From req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void fromReq(final String[] parameters) throws ParameterException {
        from = getParameter(parameters);
    }

    /**
     * Destination req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void destinationReq(final String[] parameters) throws ParameterException {
        selectedDestination = getParameter(parameters);
    }

    /**
     * Size req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void sizeReq(final String[] parameters) throws ParameterException {
        final var size = getParameter(parameters);
        try {
            if ((currentSize = Long.parseLong(size)) >= 0) {
                return;
            }
        } catch (final NumberFormatException e) {
            // Ignored
        }
        stopAndError("Invalid source file size (" + size + ")");
    }

    /**
     * Format req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void formatReq(final String[] parameters) throws ParameterException {
        dateFormat = getParameter(parameters);
    }

    /**
     * Group req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void groupReq(final String[] parameters) throws ParameterException {
        transferGroup = getParameter(parameters);
    }

    /**
     * Purge req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void purgeReq(final String[] parameters) throws ParameterException {
        purge = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * ASAP req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void asapReq(final String[] parameters) throws ParameterException {
        currentAsap = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * EVENT req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void eventReq(final String[] parameters) throws ParameterException {
        currentEvent = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Timefile req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void timefileReq(final String[] parameters) throws ParameterException {
        timeFileString = getParameter(parameters);
        try {
            if ((timeFile = Long.parseLong(timeFileString) * 1000L) >= 0) {
                return;
            }
        } catch (final NumberFormatException e) {
            // Ignored
        }
        stopAndError("Invalid timefile (" + timeFileString + ")");
    }

    /**
     * Priority req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void priorityReq(final String[] parameters) throws ParameterException {
        final var priorityString = getParameter(parameters);
        try {
            currentPriority = Integer.parseInt(priorityString);
            if (currentPriority < 0 || currentPriority > 99) {
                stopAndError("Invalid value specified for the -priority option (range is 0..99)");
            }
        } catch (final NumberFormatException e) {
            stopAndError("Invalid format used for the -priority option (" + priorityString + ")");
        }
    }

    /**
     * Source req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void sourceReq(final String[] parameters) throws ParameterException {
        source = getParameter(parameters);
        checkParameterSize("source", source, MAX_SOURCE_NAME_LENGTH);
    }

    /**
     * Original req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void originalReq(final String[] parameters) throws ParameterException {
        original = getParameter(parameters);
        checkParameterSize("original", original, MAX_SOURCE_NAME_LENGTH);
    }

    /**
     * Hostforacquisition req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void hostforacquisitionReq(final String[] parameters) throws ParameterException {
        hostForAcquisition = getParameter(parameters);
        checkParameterSize("hostforacquisition", hostForAcquisition, 32);
    }

    /**
     * Target req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void targetReq(final String[] parameters) throws ParameterException {
        final var target = getParameter(parameters);
        try {
            final var normalized = Format.normalizePath(target, true);
            currentTarget = normalized.substring(normalized.startsWith("/") || normalized.startsWith("\\") ? 1 : 0);
            if (currentTarget.endsWith("/")) { // The target is a directory!
                currentTarget += new File(source == null ? original : source).getName();
            }
        } catch (final FileNotFoundException _) {
            stopAndError("Invalid format for the -target option (" + target + ")");
        }
        checkParameterSize("target", currentTarget, MAX_SOURCE_NAME_LENGTH);
    }

    /**
     * Check parameter size.
     *
     * Utility method to check the size of a parameter (e.g. identity, original, source and target).
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @param size
     *            the size
     */
    private void checkParameterSize(final String key, final String value, final int size) {
        if (value != null && value.length() > size) {
            stopAndError("Invalid value specified for the -" + key + " option (max length is " + size + ")");
        }
    }

    /**
     * Identity req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void identityReq(final String[] parameters) throws ParameterException {
        identity = getParameter(parameters);
        checkParameterSize("identity", identity, MAX_SOURCE_NAME_LENGTH);
    }

    /**
     * Caller req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void callerReq(final String[] parameters) throws ParameterException {
        caller = getParameter(parameters);
    }

    /**
     * Lifetime req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void lifetimeReq(final String[] parameters) throws ParameterException {
        lifeTimeString = getParameter(parameters);
        try {
            lifeTime = Format.getDuration(lifeTimeString);
            if (lifeTime <= 0) {
                stopAndError("Invalid value specified for the -lifetime option (should be > 0)");
            }
        } catch (final NumberFormatException e) {
            stopAndError("Invalid duration format for the -lifetime option (" + lifeTimeString + ")");
        }
    }

    /**
     * Delay req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void delayReq(final String[] parameters) throws ParameterException {
        final var delayString = getParameter(parameters);
        try {
            currentDelay = Format.getDuration(delayString);
            if (currentDelay <= 0) {
                stopAndError("Invalid value specified for the -delay option (should be > 0)");
            }
        } catch (final NumberFormatException e) {
            stopAndError("Invalid duration format for the -delay option (" + delayString + ")");
        }
    }

    /**
     * Buffer req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void bufferReq(final String[] parameters) throws ParameterException {
        final var bufferString = getParameter(parameters);
        try {
            currentBuffer = Format.getDuration(bufferString);
            if (currentBuffer < 0) {
                stopAndError("Invalid value specified for the -buffer option (should be >= 0)");
            }
        } catch (final NumberFormatException e) {
            stopAndError("Invalid duration format for the -buffer option (" + bufferString + ")");
        }
    }

    /**
     * At req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void atReq(final String[] parameters) throws ParameterException {
        atString = getParameter(parameters);
        at = Format.toTime(dateFormat, atString);
        if (at == -1) {
            stopAndError("Invalid date format for the -at option (" + dateFormat + ")");
        }
    }

    /**
     * _check meta data.
     *
     * @param what
     *            the what
     * @param name
     *            the name
     * @param value
     *            the value
     * @param length
     *            the length
     *
     * @return true, if successful
     */
    private boolean checkMetaData(final String what, final String name, final String value, final long length) {
        final var result = value.length() <= length;
        if (!result) {
            stopAndError("Invalid " + what + " for metadata " + name + " (max size is " + length + ")");
        }
        return result;
    }

    /**
     * Metadata req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void metadataReq(final String[] parameters) throws ParameterException {
        metaData = getParameter(parameters);
        if (metaData.startsWith("\"") && metaData.endsWith("\"") && metaData.length() > 1) {
            metaData = metaData.substring(1, metaData.length() - 1);
        }
        // Let's go through all the key-value pairs and extract the metadata which are
        // relevant to the datafile.
        String previousName = null;
        String application = null;
        String metaDate = null;
        for (final String option : metaData.trim().split(",")) {
            final var index = option.indexOf("=");
            if (index > 0) {
                final var name = option.substring(0, index).trim();
                final var value = option.substring(index + 1).trim();
                previousName = name;
                metaDataList.put(name, value);
                if ("step".equals(name)) {
                    if (processMetadata) { // This is dissemination!
                        metaDataList.remove("step");
                    }
                    try {
                        timeStep = Long.parseLong(value);
                        _log.debug("TimeStep: {}", timeStep);
                    } catch (final NumberFormatException e) {
                        if (processMetadata) {
                            stopAndError("Invalid number format for metadata step (" + value + ")");
                            return;
                        } // This is acquisition so we can ignore!
                    }
                } else if ("application".equals(name)) {
                    application = value;
                } else if ("date".equals(name)) {
                    if (processMetadata) { // This is dissemination!
                        metaDataList.remove("date");
                    }
                    metaDate = value;
                } else if ("domain".equals(name)) {
                    domain = value;
                } else if ("type".equals(name)) {
                    if (!checkMetaData("value", "type", value, 25)) {
                        return;
                    }
                    metaType = value;
                } else if ("time".equals(name)) {
                    if (!checkMetaData("value", "time", value, 16)) {
                        return;
                    }
                    metaTime = value;
                } else if ("target".equals(name)) {
                    if (!checkMetaData("value", "target", value, 25)) {
                        return;
                    }
                    metaTarget = value;
                } else if ("stream".equals(name)) {
                    if (!checkMetaData("value", "stream", value, 25)) {
                        return;
                    }
                    metaStream = value;
                } else {
                    if (!checkMetaData("name", name, name, 25) || !checkMetaData("value", name, value, 255)) {
                        return;
                    }
                }
            } else // No equal found in the token so we should concatenate the
            // value to the previous parameter, except if the parameter is
            // one of the recognized parameter!
            if (isNotEmpty(previousName) && !("application".equals(previousName) || "step".equals(previousName)
                    || "date".equals(previousName) || "time".equals(previousName) || "domain".equals(previousName)
                    || "type".equals(previousName) || "target".equals(previousName) || "stream".equals(previousName))) {
                final var previousValue = metaDataList.get(previousName);
                if (isNotEmpty(previousValue)) {
                    metaDataList.put(previousName, previousValue + "," + option);
                }
            }
        }
        if (isNotEmpty(metaStream) && isNotEmpty(application) && !DEFAULT_APPLICATION.equalsIgnoreCase(application)) {
            _log.debug("This is NOT a {} product: {}", DEFAULT_APPLICATION, application);
            final var stream = metaStream + "-" + application;
            if (!checkMetaData("value", "stream", stream, 25)) {
                return;
            }
            metaStream = stream;
        }
        // Let's extract the product date!
        if (metaDate != null) {
            final String productDate;
            if (metaDate.length() == 8) { // yyyyMMdd
                if (metaTime != null && metaTime.length() == 2) { // HH
                    productDate = metaDate + metaTime;
                } else {
                    productDate = metaDate + "00"; // fallback
                }
            } else if (metaDate.length() == 10) { // yyyyMMddHH already
                productDate = metaDate.substring(0, 10); // ensure only yyyyMMddHH
            } else {
                _log.warn("Unexpected date length: '{}', expected 8 or 10 chars", metaDate);
                productDate = null;
            }
            if (productDate != null) {
                currentProductDate = Format.toTime("yyyyMMddHH", productDate);
                if (processMetadata && currentProductDate != -1) { // This is dissemination!
                    metaDataList.put("time", Format.formatTime("HH", currentProductDate));
                }
            }
        }
    }

    /**
     * Reqid req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void reqidReq(final String[] parameters) throws ParameterException {
        final var reqId = getParameter(parameters);
        try {
            requestId = Long.parseLong(reqId);
            if (requestId < 0) {
                stopAndError("Invalid value specified for the -reqid option (<0)");
            }
        } catch (final NumberFormatException e) {
            stopAndError("Invalid format used for the -reqid option (" + reqId + ")");
        }

    }

    /**
     * Groupby req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void groupbyReq(final String[] parameters) throws ParameterException {
        groupBy = getParameter(parameters);
        checkParameterSize("groupby", groupBy, 64);
    }

    /**
     * Requeue req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void requeueReq(final String[] parameters) throws ParameterException {
        reQueue = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Force req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void forceReq(final String[] parameters) throws ParameterException {
        force = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Acquisition req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void acquisitionReq(final String[] parameters) throws ParameterException {
        acquisition = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Failedonly req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void failedonlyReq(final String[] parameters) throws ParameterException {
        failedOnly = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Noretrieval req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void noretrievalReq(final String[] parameters) throws ParameterException {
        noRetrieval = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Dontsend req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void dontsendReq(final String[] parameters) throws ParameterException {
        standBy = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Standby req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void standbyReq(final String[] parameters) throws ParameterException {
        standBy = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Index req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void indexReq(final String[] parameters) throws ParameterException {
        final var index = getParameter(parameters);
        try {
            currentIndex = Integer.parseInt(index);
            if (currentIndex <= 0) {
                stopAndError("Invalid value specified for index (should be >0)");
            }
        } catch (final NumberFormatException e) {
            stopAndError("Invalid format used for index (" + index + ")");
        }
    }

    /**
     * Timecritical req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void timecriticalReq(final String[] parameters) throws ParameterException {
        timeCritical = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Uniquename req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void uniquenameReq(final String[] parameters) throws ParameterException {
        uniqueName = getParameter(parameters);
    }

    /**
     * Removes the req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void removeReq(final String[] parameters) throws ParameterException {
        remove = Boolean.parseBoolean(getParameter(parameters));
    }

    /**
     * Host req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void hostReq(final String[] parameters) throws ParameterException {
        var host = getParameter(parameters).trim();
        if (host.startsWith("+")) {
            final var dataFile = currentTransfer.getDataFile();
            final var token = new StringTokenizer(host, "(|)");
            if (token.countTokens() == 4) {
                token.nextToken();
                dataFile.setRemoteHost(getRemoteHost());
                dataFile.setGetHost(token.nextToken());
                dataFile.setGetTime(new Timestamp(Long.parseLong(token.nextToken())));
                dataFile.setGetDuration(token.nextToken());
            }
        } else {
            if (host.startsWith("-")) {
                host = host.substring(1);
            }
            if (provider != null) {
                errorMessage = "Error during transmission to group " + provider.getTransferGroup().getName()
                        + (host.length() > 0 ? " (" + host + ")" : "");
            } else {
                errorMessage = "Error during transmission" + (host.length() > 0 ? " (" + host + ")" : "");
            }
        }
    }

    /**
     * Release connection.
     *
     * @param socket
     *            the socket
     * @param close
     *            the close
     */
    @Override
    public void releaseConnection(final Socket socket, final boolean close) {
        try {
            if (groupBy == null && newDataFile && currentTransfer != null && currentTransfer.getDataFile() != null) {
                _log.warn("Removing DataFile and corresponding DataTransfer(s)");
                try {
                    MASTER.removeDataFileAndDataTransfers(currentTransfer.getDataFile(), userName,
                            "from the ecpds command-line");
                } catch (final Exception e) {
                    _log.warn("Removing DataFile and DataTransfer(s)", e);
                }
            }
        } finally {
            if (key != null) {
                MASTER.unlockTransfer(key);
            }
        }
        super.releaseConnection(socket, close);
    }

    /**
     * Expected req.
     */
    public void expectedReq() {
        if (LOG_REQUESTS) {
            final var req = new ECpdsExpected();
            req.setUSER(userName);
            req.setVERSION(version);
            req.setMETADATA(metaData);
            req.setAT(atString);
            req.setFORMAT(dateFormat);
            req.store();
        }
        notification(StatusFactory.INIT);
    }

    /**
     * Started req.
     */
    public void startedReq() {
        if (LOG_REQUESTS) {
            final var req = new ECpdsStarted();
            req.setUSER(userName);
            req.setVERSION(version);
            req.setMETADATA(metaData);
            req.store();
        }
        notification(StatusFactory.EXEC);
    }

    /**
     * Completed req.
     */
    public void completedReq() {
        if (LOG_REQUESTS) {
            final var req = new ECpdsCompleted();
            req.setUSER(userName);
            req.setVERSION(version);
            req.setMETADATA(metaData);
            req.store();
        }
        notification(StatusFactory.DONE);
    }

    /**
     * _reset all.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void resetAll() throws IOException {
        final var products = DATABASE.getProductStatusArray();
        for (final ProductStatus status : products) {
            final var stream = status.getStream();
            final var time = status.getTime();
            try {
                final var affectedRows = DATABASE.resetProductStatus(stream, time, -1);
                _log.debug("{} ProductStatus updated for {}-{}", affectedRows, stream, time);
            } catch (final DataBaseException e) {
                _log.warn(e);
            }
            MASTER.handle(new ResetProductEvent(status.getStream(), status.getTime()));
        }
        send("MESSAGE " + products.length + " product(s) status reseted");
    }

    /**
     * Reset req.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void resetReq() throws IOException {
        setLoop(false);
        if (metaStream == null && metaTime == null) {
            resetAll();
            return;
        }
        if (metaStream == null || metaTime == null) {
            stopAndError("Missing metadata value(s) (stream and/or time)");
            return;
        }
        try {
            DATABASE.resetProductStatus(domain + metaStream, metaTime, timeStep);
        } catch (final DataBaseException e) {
            _log.warn(e);
        }
        MASTER.handle(new ResetProductEvent(domain + metaStream, metaTime));
        MASTER.getInitialProductStatusEvents(null, domain + metaStream, metaTime); // Update the monitoring display
        _log.debug("Notification reveived: reset product {}-{}{}-{}", metaTime, domain, metaStream, timeStep);
        send("MESSAGE Product status reseted");
    }

    /**
     * _notification.
     *
     * @param status
     *            the status
     */
    private void notification(final String status) {
        setLoop(false);
        if (metaStream == null || metaTime == null || timeStep == -1) {
            stopAndError("Missing metadata value(s) (stream, time and/or step)");
            return;
        }
        try (final var mutex = productStatusMutexProvider
                .getMutex(domain + metaStream + metaTime + timeStep + (metaType != null ? metaType : ""))) {
            synchronized (mutex.lock()) {
                try {
                    final var products = DATABASE.getProductStatus(domain + metaStream, metaTime, metaType, timeStep,
                            1);
                    final var length = products.length;
                    if (length == 0 && at == -1) {
                        final var notFoundMessage = "Product status not found (-at option required)";
                        if (Cnf.at("ECpdsPlugin", "ignoreMissingScheduleTime", true)) {
                            send("MESSAGE " + notFoundMessage);
                        } else {
                            stopAndError(notFoundMessage);
                        }
                        _log.warn(notFoundMessage);
                        return;
                    }
                    if (length > 1) {
                        _log.warn("Multiple entries for same product: {}", products[0]);
                    }
                    if (StatusFactory.INIT.equals(status) && length > 0) {
                        final var product = products[0];
                        if (product.getScheduleTime().getTime() == at) {
                            final var alreadyExpectedMessage = "Product status already expected (notification ignored)";
                            _log.warn("{}: {}", alreadyExpectedMessage, product);
                            send("MESSAGE " + alreadyExpectedMessage);
                            return;
                        }
                        if (_log.isDebugEnabled()) {
                            _log.debug("Product status updated ({}!={})",
                                    Format.formatTime(product.getScheduleTime().getTime()), Format.formatTime(at));
                        }
                    }
                    /* Addition for repeated products */
                    if (StatusFactory.EXEC.equals(status) && length > 0) {
                        final var product = products[0];
                        if (!StatusFactory.INIT.equals(product.getStatusCode())) {
                            final var notExpectedMessage = "Product status not expected (notification ignored)";
                            _log.warn("{}: {}", notExpectedMessage, product);
                            send("MESSAGE " + notExpectedMessage);
                            return;
                        }
                    }
                    if (StatusFactory.DONE.equals(status) && length > 0) {
                        final var product = products[0];
                        if (StatusFactory.DONE.equals(product.getStatusCode())) {
                            final var alreadyCompletedMessage = "Product status already completed (notification ignored)";
                            _log.warn("{}: {}", alreadyCompletedMessage, product);
                            send("MESSAGE " + alreadyCompletedMessage);
                            return;
                        }
                    }
                    /* End of addition */
                    final var product = new ProductStatus();
                    product.setStream(domain + metaStream);
                    product.setStep(timeStep);
                    product.setTime(metaTime);
                    product.setBuffer(currentBuffer);
                    product.setStatusCode(status);
                    product.setTimeBase(new Timestamp(getProductDate()));
                    product.setUserStatus(userName);
                    product.setComment(from);
                    product.setType(metaType);
                    var currentUpdate = System.currentTimeMillis();
                    // Fix just in case of inconsistencies in the Timeline (e.g. bad
                    // timing in logs).
                    if (length > 0) {
                        final var lastUpdate = products[0].getLastUpdate().getTime();
                        if (lastUpdate >= currentUpdate) {
                            currentUpdate = lastUpdate + 1;
                            if (_log.isWarnEnabled()) {
                                _log.warn("Last update resynchronized to {}", Format.formatTime(currentUpdate));
                            }
                        }
                    }
                    product.setLastUpdate(new Timestamp(currentUpdate));
                    product.setScheduleTime(at == -1 ? products[0].getScheduleTime() : new Timestamp(at));
                    DATABASE.insert(product, true);
                    if (_splunk.isInfoEnabled()) {
                        // For the accounting!
                        _splunk.info("PRS;{};{};{};{};{};{};{}", "StatusCode=" + product.getStatusCode(),
                                "DataStream=" + product.getStream(), "TimeStep=" + product.getStep(),
                                "TimeBase=" + product.getTimeBase(), "Type=" + product.getType(),
                                "ScheduleTime=" + product.getScheduleTime(), "LastUpdate=" + product.getLastUpdate());
                    }
                    MASTER.handle(new ProductStatusEvent(product));
                    _log.debug("Notification received: {}", product);
                    send("MESSAGE Product status " + (length > 0 ? "updated to " : "created as ")
                            + StatusFactory.getProductStatusName(status) + " (ProductId=" + product.getId() + ")");
                } catch (final Throwable t) {
                    _log.error("Process aborted", t);
                    final var error = t.getMessage();
                    error(error == null ? "aborted by server" : error);
                }
            }
        }
    }

    /**
     * Gets the product date.
     *
     * If the product date was not set then let's use the timeFile if we have it or then let's use the current time!
     *
     * @return the product date
     */
    private long getProductDate() {
        if (currentProductDate != -1) {
            return currentProductDate;
        }
        final var currentTimeFile = timeFile != -1 ? timeFile : System.currentTimeMillis();
        return Format.toTime(PRODUCT_DATE_FORMAT, Format.formatTime(PRODUCT_DATE_FORMAT, currentTimeFile));
    }

    /**
     * _try to delete data file.
     *
     * @param dataFile
     *            the data file
     */
    private void tryToDeleteDataFile(final DataFile dataFile) {
        final var id = "original DataFile " + dataFile.getId();
        var deleted = false;
        try {
            deleted = MASTER.deleteDataFile(dataFile);
        } catch (final Throwable t) {
            _log.warn("Deleting {}", id, t);
        } finally {
            if (!deleted) {
                _log.warn("Could NOT fully delete {}", id);
            } else {
                _log.warn("The {} was deleted", id);
            }
        }
    }

    /**
     * Bye req.
     */
    public void byeReq() {
        setLoop(false);
        final var duration = System.currentTimeMillis() - start;
        if (putReq != null) {
            // We have a put request pending to be recorded. Let's update the
            // size and store it!
            putReq.setSIZE(currentSize);
            putReq.store();
        }
        String currentStatus;
        var notDownloaded = false;
        if (groupBy != null) {
            if (!noRetrieval) {
                // We are in the groupBy mode (retrieval) files will be
                // downloaded through the download scheduler
                currentStatus = StatusFactory.SCHE;
            } else {
                // The file will not be downloaded (source host will be used for
                // the dissemination)
                currentStatus = standBy ? StatusFactory.HOLD : StatusFactory.WAIT;
                notDownloaded = true;
            }
        } else // We are in the standard mode (push)
        if (standBy) {
            currentStatus = StatusFactory.HOLD;
        } else {
            currentStatus = newDataFile ? StatusFactory.WAIT : StatusFactory.RETR;
        }
        final var dataFile = currentTransfer.getDataFile();
        if (errorMessage != null) {
            error(errorMessage);
            dataFile.setDeleted(true);
            dataFile.setRemoved(true);
            for (final DataTransfer transfer : transfersList) {
                transfer.setDeleted(true);
                transfer.setComment(errorMessage);
            }
            currentStatus = StatusFactory.FAIL;
        } else if (recordUploadHistory) {
            // We have to create an history for the standard push!
            final var command = version.indexOf("cmd=mspds") != -1 ? "mspds" : "ecpds";
            final var uploadMessage = (groupBy == null ? "Uploaded" : "Scheduled") + " by " + userName + " from "
                    + getRemoteHost() + " using " + command + " command " + version + " ("
                    + Format.getMBitsPerSeconds(currentSize, duration) + " Mbits/s)";
            for (final DataTransfer transfer : transfersList) {
                if (groupBy == null)
                    MASTER.addTransferHistory(transfer, null, currentStatus, uploadMessage, false);
                // Create a new IncomingHistory!
                try {
                    final var history = new IncomingHistory();
                    history.setDataTransfer(transfer);
                    history.setDataTransferId(transfer.getId());
                    history.setDestination(transfer.getDestinationName());
                    history.setFileName(transfer.getTarget());
                    history.setFileSize(currentSize);
                    history.setScheduledTime(transfer.getScheduledTime());
                    history.setStartTime(new Timestamp(start));
                    history.setMetaStream(dataFile.getMetaStream());
                    history.setMetaType(dataFile.getMetaType());
                    history.setMetaTime(dataFile.getMetaTime());
                    history.setTimeBase(dataFile.getTimeBase());
                    history.setTimeStep(dataFile.getTimeStep());
                    history.setDuration(duration);
                    history.setUserName(userName);
                    history.setSent(currentSize);
                    history.setProtocol(command);
                    history.setTransferServer(MASTER.getRoot());
                    history.setHostAddress(getRemoteHost());
                    history.setUpload(groupBy == null);
                    if (groupBy == null)
                        DATABASE.insert(history, true);
                    if (_splunk.isInfoEnabled()) {
                        // For the accounting!
                        final var destination = transfer.getDestination();
                        final var ecuser = DATABASE.getECUserObject(userName);
                        _splunk.info("INH;{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{}",
                                "Monitored=" + destination.getMonitor(),
                                "DataTransferId=" + history.getDataTransferId(),
                                "DestinationName=" + destination.getName(),
                                "DestinationType=" + DestinationOption.getLabel(destination.getType()),
                                "FileName=" + history.getFileName(), "FileSize=" + history.getFileSize(),
                                "ScheduledTime=" + history.getScheduledTime(), "StartTime=" + history.getStartTime(),
                                "MetaStream=" + nullToNone(history.getMetaStream()),
                                "MetaType=" + nullToNone(history.getMetaType()), "MetaTime=" + history.getMetaTime(),
                                "TimeBase=" + history.getTimeBase(), "TimeStep=" + history.getTimeStep(),
                                "Duration=" + history.getDuration(), "UserId=" + userName,
                                "CountryCode=" + destination.getCountryIso(),
                                "UserDescription=" + (ecuser != null ? ecuser.getComment() : ""),
                                "BytesSent=" + history.getSent(), "TransferProtocol=" + history.getProtocol(),
                                "TransferServer=" + history.getTransferServer(),
                                "Caller=" + nullToNone(dataFile.getCaller()), "ExpiryTime=" + transfer.getExpiryTime(),
                                "FileSystem=" + dataFile.getFileSystem(), "HostAddress=" + history.getHostAddress(),
                                "Action=" + (groupBy == null ? "upload" : "scheduled"));
                    }
                    _log.debug("IncomingHistory created for DataTransfer {}", transfer.getId());
                } catch (final Throwable t) {
                    _log.error("Creating IncomingHistory", t);
                }
            }
        }
        try {
            if (groupBy == null) {
                dataFile.setGetCompleteDuration(duration);
            }
            dataFile.setSize(currentSize);
            DATABASE.update(dataFile);
            for (final DataTransfer transfer : transfersList) {
                var thisStatus = currentStatus;
                var queued = StatusFactory.WAIT.equals(thisStatus) || StatusFactory.RETR.equals(thisStatus);
                final var setup = DESTINATION_SCHEDULER.getECtransSetup(transfer.getDestination().getData());
                final var forceOptions = setup.getOptions(DESTINATION_SCHEDULER_FORCE, currentTarget, null);
                if (queued) {
                    // "never" will do with false as we look for yes or true!
                    if (setup.getBoolean(DESTINATION_SCHEDULER_STANDBY) || forceOptions.get("standby", false)
                            && forceOptions.matches("pattern", currentTarget, ".*")
                            && !forceOptions.matches("ignore", currentTarget)) {
                        _log.debug("Standby forced by scheduler option in Destination");
                        thisStatus = StatusFactory.HOLD;
                    }
                } else // If in standby mode, are we required to [re]queue it?
                if (StatusFactory.HOLD.equals(thisStatus)
                        && ("never".equalsIgnoreCase(setup.getString(DESTINATION_SCHEDULER_STANDBY))
                                || "never".equalsIgnoreCase(forceOptions.get("standby", ""))
                                        && forceOptions.matches("pattern", currentTarget, ".*")
                                        && !forceOptions.matches("ignore", currentTarget))) {
                    _log.debug("WAIT forced by scheduler option in Destination");
                    thisStatus = newDataFile ? StatusFactory.WAIT : StatusFactory.RETR;
                    queued = true;
                }
                if (groupBy == null && currentAsap) {
                    // The schedule time will not be set in the waitfor method so it has to be done
                    // here!
                    _log.debug("Setting queue/retry time to current time (asap)");
                    final var currentTime = new Timestamp(System.currentTimeMillis());
                    transfer.setQueueTime(currentTime);
                    transfer.setRetryTime(currentTime);
                }
                transfer.setDataFileId(dataFile.getId());
                transfer.setDataFile(dataFile);
                transfer.setSize(currentSize);
                // If this is the experimental ECPDS then all the transfers to a stop
                // destination will be stopped!
                final var forceStop = !Cnf.at("ECpdsPlugin", "failOnDestinationNotFound", true)
                        && setup.getBoolean(DESTINATION_SCHEDULER_FORCE_STOP)
                        && StatusFactory.STOP.equals(transfer.getDestination().getStatusCode());
                if (forceStop) {
                    transfer.setStatusCode(StatusFactory.STOP);
                    transfer.setComment("Not scheduled (destination is stopped)");
                } else {
                    transfer.setStatusCode(thisStatus);
                    if (StatusFactory.SCHE.equals(thisStatus)) {
                        transfer.setComment("Not scheduled yet (queuing required for " + groupBy + ")");
                    } else if (StatusFactory.HOLD.equals(thisStatus)) {
                        transfer.setComment("Not scheduled yet (manual queuing is required)");
                        transfer.setNotify(true);
                    } else if (queued) {
                        transfer.setComment("Scheduled for "
                                + (transfer.getAsap() ? "as soon as possible" : "no sooner than " + Format
                                        .formatTime(DEFAULT_DATE_FORMAT, transfer.getScheduledTime().getTime())));
                    }
                }
                if (groupBy == null && provider != null) {
                    for (final TransferServer server : provider.getTransferServers()) {
                        if (server.getName().equals(dataFile.getGetHost())) {
                            final var serverName = server.getName();
                            transfer.setTransferServerName(serverName);
                            transfer.setTransferServer(server);
                            if (transfer.getOriginalTransferServer() == null) {
                                transfer.setOriginalTransferServerName(serverName);
                                transfer.setOriginalTransferServer(server);
                            }
                            break;
                        }
                    }
                }
                _log.debug("DataFile {}, DataTransfer {} downloaded: {} ({})", dataFile.getId(), transfer.getId(),
                        dataFile.getDownloaded(), transfer.getStatusCode());
                DATABASE.update(transfer);
                if (notDownloaded && !forceStop) {
                    MASTER.addTransferHistory(transfer, thisStatus,
                            "File not retrieved (source host will be used instead)");
                }
                final var ticket = MASTER.getTicketRepository().get(transfer.getId());
                if (ticket != null && StatusFactory.INIT.equals(ticket.getStatus())) {
                    MASTER.addTransferHistory(transfer, StatusFactory.INIT,
                            "Awaiting transfer interruption of previous instance");
                } else {
                    MASTER.addTransferHistory(transfer);
                }
                final var event = new DataTransferEvent(transfer);
                event.setSource("ECpdsPlugin.byeReq");
                MASTER.handle(event);
                if (queued) {
                    MASTER.reloadDestination(transfer);
                }
            }
            if (datafileToDelete != null) {
                tryToDeleteDataFile(datafileToDelete);
            }
            send(message);
            newDataFile = false;
        } catch (final Throwable t) {
            _log.error("Process aborted", t);
            final var error = t.getMessage();
            error(error == null ? "aborted by server" : error);
        }
    }

    /**
     * Puts the req.
     */
    public void putReq() {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        // If we are on the test system then force the standby mode, except for
        // the acquisition!
        if (FORCE_STANDBY && (from == null || from != null && !from.startsWith("From Acquisition Host"))) {
            _log.debug("Forcing StandBy mode");
            standBy = true;
        }
        if (LOG_REQUESTS) {
            // We create the put request but we don't store it yet as the size
            // might not be know yet (e.g. with incoming ftp/sftp data file the
            // size is only known afterwards - just before the bye method is
            // invoked)!
            putReq = new ECpdsPut();
            putReq.setUSER(userName);
            putReq.setVERSION(version);
            putReq.setAT(atString);
            putReq.setDESTINATION(selectedDestination);
            putReq.setFAILEDONLY(failedOnly);
            putReq.setFORCE(force);
            putReq.setFROM(from);
            putReq.setCALLER(caller);
            putReq.setINDEX(currentIndex);
            putReq.setGROUPBY(groupBy);
            putReq.setHOSTFORACQUISITION(hostForAcquisition);
            putReq.setIDENTITY(identity);
            putReq.setLIFETIME(lifeTimeString);
            putReq.setMETADATA(metaData);
            putReq.setNORETRIEVAL(noRetrieval);
            putReq.setORIGINAL(original);
            putReq.setPRIORITY(currentPriority);
            putReq.setSOURCE(source);
            putReq.setSTANDBY(standBy);
            putReq.setTARGET(currentTarget);
            putReq.setTIMEFILE(timeFileString);
            putReq.setUNIQUENAME(uniqueName);
            putReq.setFORMAT(dateFormat);
            putReq.setASAP(currentAsap);
            putReq.setEVENT(currentEvent);
        }
        try {
            if (selectedDestination == null || selectedDestination.isBlank()) {
                stopAndError("Destination not specified");
                return;
            }
            // Get the current Destination!
            final var currentDestination = DATABASE.getDestinationObject(selectedDestination);
            if (currentDestination == null) {
                // The Destination is not found in the database!
                final var error = "Destination " + selectedDestination + " not found";
                if (groupBy != null && !Cnf.at("ECpdsPlugin", "failOnDestinationNotFound", true)) {
                    // We are on the test system in the retrieval mode, just
                    // report the error but don't fail!
                    _log.error("Ignore request for Destination {} (not found)", selectedDestination);
                    send("MESSAGE " + error + " (metadata ignored)");
                } else {
                    // Fail on Destination not found!
                    stopAndError(error);
                }
                return;
            }
            final var setup = DESTINATION_SCHEDULER.getECtransSetup(currentDestination.getData());
            // Let's look at the scheduler.force options!
            final var forceOptions = setup.getOptions(DESTINATION_SCHEDULER_FORCE, currentTarget, null);
            {
                // Check if the uniqueName (version option of the ecpds/mspds command) is not
                // forced in the Destination?
                final var defaultUniqueName = setup.getString(DESTINATION_SCHEDULER_VERSION);
                final var newUniqueName = forceOptions.matches("pattern", currentTarget, ".*")
                        && !forceOptions.matches("ignore", currentTarget)
                                ? forceOptions.get(DESTINATION_SCHEDULER_VERSION.getName(), defaultUniqueName)
                                : defaultUniqueName;
                if (newUniqueName != null) {
                    final var format = setup.getString(DESTINATION_SCHEDULER_DATE_FORMAT);
                    final var sb = new StringBuilder(newUniqueName);
                    Format.replaceAll(sb, "$date", Format.formatTime(format, System.currentTimeMillis()));
                    Format.replaceAll(sb, "$timestamp", System.currentTimeMillis());
                    Format.replaceAll(sb, "$destination", selectedDestination);
                    Format.replaceAll(sb, "$target", currentTarget);
                    Format.replaceAll(sb, "$original", original);
                    Format.replaceAll(sb, "$timefile", timeFile);
                    uniqueName = sb.toString();
                    _log.debug("Version forced by scheduler option in {} ({})", selectedDestination, uniqueName);
                }
            }
            key = TransferManagement.getUniqueKey(standBy, selectedDestination, currentTarget, uniqueName);
            final Object object = MASTER.lockTransfer(key, this);
            if (object != null) {
                if (object instanceof final ECpdsPlugin plugin) {
                    // We found that this transfer request is already processed in a
                    // different instance of the ECpdsPlugin!
                    stopAndError("Already processed by " + plugin.getInfo() + " (" + plugin.version + ") from "
                            + plugin.getRemoteHost() + ":" + plugin.getSocket().getPort() + " (uniqueKey=" + key
                            + ",alive=" + plugin.isAlive() + ")");
                    return;
                }
                // This transfer request is locked by something else than the
                // ECpdsPlugin!
                stopAndError("Already processed by " + Format.getClassName(object) + " (uniqueKey=" + key + ")");
                return;
            }
            if (requestId == -1 && (selectedDestination == null || original == null)) {
                stopAndError("Missing parameters (reqid or destination and source)");
                return;
            }
            currentProductDate = getProductDate();
            if (_log.isDebugEnabled()) {
                _log.debug("Product date: {}", Format.formatTime(currentProductDate));
            }
            final var date = System.currentTimeMillis();
            if (currentTarget == null) {
                currentTarget = source == null ? original : source;
            }
            at = at == -1 ? date : at;
            var startAtWithDelay = at + currentDelay;
            var expiry = startAtWithDelay + lifeTime;
            if (expiry < date) {
                stopAndError("The request has already expired (please review -at and/or -delay options)");
                return;
            }
            if (identity == null) {
                identity = currentTarget;
            }
            if (requestId != -1) {
                transfersList = MASTER.getDataTransfers(requestId);
            } else {
                transfersList = DATABASE.getScheduledDataTransfer(key, selectedDestination);
                final List<Long> dataFileIds = new ArrayList<>();
                if (transfersList.length > 0) {
                    // Check that there is a single DataFile for all
                    // the DataTransfer(s)
                    final var dataFile = transfersList[0].getDataFile();
                    final var originalId = dataFile.getId();
                    final var dataFilesList = new StringBuilder("DataFileId=").append(originalId);
                    dataFileIds.add(originalId);
                    for (var i = 1; i < transfersList.length; i++) {
                        final var transfer = transfersList[i];
                        final var currentId = transfer.getDataFileId();
                        if (!dataFileIds.contains(currentId)) {
                            dataFilesList.append(", DataFileId=").append(currentId);
                            dataFileIds.add(currentId);
                        }
                    }
                    // If we have more than one DataFileId then we don't know
                    // which one to use and the user must select one!
                    if (dataFileIds.size() > 1) {
                        stopAndError(dataFileIds.size() + " DataFiles found with " + transfersList.length
                                + " DataTransfers (" + dataFilesList
                                + "), please use the '-reqid' option to select one! (uniqueKey=" + key + ")");
                        return;
                    }
                }
            }
            final DataFile dataFile;
            if (transfersList.length == 0) {
                // No DataTransfer(s) found!
                if (purge || reQueue) {
                    // Can't purge or re-queue when no DataTransfer(s) are found!
                    stopAndError("No corresponding DataTransfer(s) to " + (purge ? "purge" : "requeue"));
                    return;
                }
                // Create a new DataFile and associated
                // DataTransfer(s)!
                _log.debug("Create a new DataFile and corresponding DataTransfer(s)");
                currentTransfer = new DataTransfer();
                dataFile = new DataFile();
                newDataFile = true;
            } else {
                // DataTransfer(s) found!
                // If requested then keep only the fail transfers
                if (failedOnly) {
                    _log.debug("Only requeue DataTransfer(s) with the FAIL status");
                    final List<DataTransfer> transfers = new ArrayList<>();
                    for (final DataTransfer element : transfersList) {
                        if (StatusFactory.FAIL.equals(element.getStatusCode())) {
                            transfers.add(element);
                        }
                    }
                    transfersList = transfers.toArray(new DataTransfer[transfers.size()]);
                }
                currentTransfer = transfersList[0];
                dataFile = currentTransfer.getDataFile();
                _log.debug("DataFile found: {} (key={})", dataFile.getId(), key);
                if (!purge && !reQueue && !force) {
                    // Should be a reQueue, force or purge!
                    if (groupBy == null) {
                        stopAndError("Duplicate DataFile found with " + transfersList.length
                                + " DataTransfer(s) (DataFileId=" + dataFile.getId()
                                + "), please use the '-requeue/force/purge' option!");
                    } else {
                        // When the groupBy mode is used it should not fail when
                        // the metadata are sent another time.
                        _log.debug("The DataFile and related DataTransfer(s) are already there (groupby mode)");
                        send("MESSAGE DataFile already exist with " + transfersList.length
                                + " DataTransfer(s) (DataFileId=" + dataFile.getId() + ")");
                        setLoop(false);
                    }
                    return;
                }
                final var targetStatus = groupBy == null ? StatusFactory.INIT : StatusFactory.SCHE;
                _log.debug("Set the DataTransfer(s) to {}", targetStatus);
                for (final DataTransfer transfer : transfersList) {
                    transfer.setComment(from + ("-".equals(userName) ? "" : " (" + userName + ")"));
                    MASTER.updateTransferStatus(transfer.getId(), targetStatus, true, userName, from, false, false,
                            true);
                    if (purge) {
                        transfer.setDeleted(true);
                        DATABASE.update(transfer);
                    }
                }
                if (purge) {
                    // It is a purge. Stop the DataTransfer(s) and
                    // remove the DataFile and related DataTransfer(s)!
                    _log.debug("Purge the DataFile and related DataTransfer(s)");
                    MASTER.removeDataFileAndDataTransfers(dataFile, userName, "from the ecpds command-line");
                    send("MESSAGE Purge completed with " + transfersList.length
                            + " DataTransfer(s) deleted (DataFileId=" + dataFile.getId() + ")");
                    setLoop(false);
                    return;
                }
                // Is re-queue or force so delete the original DataFile!
                datafileToDelete = (DataFile) dataFile.clone();
                // This will be another instance of the file!
                final var fileInstance = dataFile.getFileInstance();
                final var instance = fileInstance == null ? 1 : fileInstance + 1;
                dataFile.setFileInstance(instance);
                _log.debug("Overwrite DataFile {} (FileInstance:{})", dataFile.getId(), instance);
            }
            // Update the new or existing DataFile with the new values!
            dataFile.setArrivedTime(new Timestamp(date));
            dataFile.setHostForAcquisitionName(hostForAcquisition);
            dataFile.setSize(currentSize);
            dataFile.setDeleteOriginal(remove);
            dataFile.setStandby(standBy);
            dataFile.setIndex(currentIndex);
            dataFile.setOriginal(original);
            dataFile.setSource(source);
            dataFile.setTimeStep(timeStep);
            dataFile.setMetaTime(metaTime != null ? metaTime : "00");
            dataFile.setMetaType(metaType);
            dataFile.setMetaStream(metaStream != null ? domain + metaStream : "None");
            dataFile.setMetaTarget(metaTarget != null ? metaTarget : "None");
            dataFile.setTimeBase(new Timestamp(currentProductDate));
            dataFile.setDownloaded(groupBy == null || noRetrieval);
            dataFile.setGroupBy(groupBy);
            dataFile.setEcauthHost("-".equals(remoteIp) ? null : remoteIp);
            dataFile.setEcauthUser("-".equals(userName) ? null : userName);
            dataFile.setCaller(caller);
            dataFile.setFilterName(null);
            dataFile.setFilterTime(null);
            dataFile.setFilterSize(-1);
            dataFile.setChecksum(null);
            dataFile.setDeleted(false);
            dataFile.setRemoved(false);
            dataFile.setTimeFile(new Timestamp(timeFile));
            {
                // Check if the lifetime is not forced in the Destination?
                final var defaultDuration = setup.getDuration(DESTINATION_SCHEDULER_LIFETIME);
                final var newDuration = forceOptions.matches("pattern", currentTarget, ".*")
                        && !forceOptions.matches("ignore", currentTarget)
                                ? forceOptions.getDuration(DESTINATION_SCHEDULER_LIFETIME.getName(), defaultDuration)
                                : defaultDuration;
                if (newDuration != null && newDuration.isPositive()) {
                    try {
                        final var newLifeTime = newDuration.toMillis();
                        final var newExpiry = startAtWithDelay + newLifeTime;
                        if (newExpiry < date) {
                            _log.warn("The request has already expired (please review -at, -delay"
                                    + " and/or lifetime options in {})", selectedDestination);
                        } else {
                            _log.debug("Lifetime forced by scheduler option in {} ({})", selectedDestination,
                                    newDuration);
                            lifeTimeString = newDuration.toString();
                            lifeTime = newLifeTime;
                            expiry = newExpiry;
                        }
                    } catch (final NumberFormatException e) {
                        _log.warn("Invalid duration format for the lifetime option in {} ({})", selectedDestination,
                                newDuration);
                    }
                }
            }
            {
                // Check if the delay is not forced in the Destination?
                final var defaultDuration = setup.getDuration(DESTINATION_SCHEDULER_DELAY);
                final var newDuration = forceOptions.matches("pattern", currentTarget, ".*")
                        && !forceOptions.matches("ignore", currentTarget)
                                ? forceOptions.getDuration(DESTINATION_SCHEDULER_DELAY.getName(), defaultDuration)
                                : defaultDuration;
                if (newDuration != null && newDuration.isPositive()) {
                    final var newDelay = newDuration.toMillis();
                    _log.debug("Delay forced by scheduler option in {} ({})", selectedDestination, newDuration);
                    currentDelay += newDelay;
                    startAtWithDelay += newDelay;
                    expiry += newDelay;
                }
            }
            {
                // Check if the noRetrieval is not forced in the Destination?
                final var defaultBoolean = setup.getBooleanObject(DESTINATION_SCHEDULER_NO_RETRIEVAL);
                final var newBoolean = forceOptions.matches("pattern", currentTarget, ".*")
                        && !forceOptions.matches("ignore", currentTarget)
                                ? forceOptions.getBoolean(DESTINATION_SCHEDULER_NO_RETRIEVAL.getName(), defaultBoolean)
                                : defaultBoolean;
                // We only set the boolean is a value is forced!
                if (newBoolean != null && noRetrieval != newBoolean) {
                    _log.debug("NoRetrieval forced by scheduler option in {} ({})", selectedDestination, newBoolean);
                    noRetrieval = newBoolean;
                    dataFile.setDownloaded(groupBy == null || noRetrieval);
                }
            }
            {
                // Check if the ASAP is not forced in the Destination?
                final var defaultBoolean = setup.getBooleanObject(DESTINATION_SCHEDULER_ASAP);
                final var newBoolean = forceOptions.matches("pattern", currentTarget, ".*")
                        && !forceOptions.matches("ignore", currentTarget)
                                ? forceOptions.getBoolean(DESTINATION_SCHEDULER_ASAP.getName(), defaultBoolean)
                                : defaultBoolean;
                if (newBoolean != null && currentAsap != newBoolean) {
                    _log.debug("Asap forced by scheduler option in {} ({})", selectedDestination, newBoolean);
                    currentAsap = newBoolean;
                }
            }
            if (newDataFile) {
                // Check if the transfer group is not forced in the Destination?
                final var defaultTransferGroupName = setup.getString(DESTINATION_SCHEDULER_TRANSFERGROUP);
                final var newTransferGroupName = forceOptions.matches("pattern", currentTarget, ".*")
                        && !forceOptions.matches("ignore", currentTarget)
                                ? forceOptions.get(DESTINATION_SCHEDULER_TRANSFERGROUP.getName(),
                                        defaultTransferGroupName)
                                : defaultTransferGroupName;
                if (newTransferGroupName != null) {
                    _log.debug("TransferGroup forced by scheduler option in {} ({})", selectedDestination,
                            newTransferGroupName);
                    transferGroup = newTransferGroupName;
                }
                // Get the list containing the current Destination and all the
                // Aliases that we should use (e.g. according to the pattern of
                // the target filename)!
                final var aliases = new AliasesParser(currentDestination, currentTarget, lifeTimeString, currentDelay,
                        currentPriority, currentAsap, currentEvent);
                // Check for each Destination that we have not reached the
                // limit and that we are authorized!
                for (final Destination destination : aliases.getDestinations()) {
                    final var name = destination.getName();
                    // Is it a time critical user?
                    if (timeCritical) {
                        // Is it a time critical Destination?
                        if (!DestinationOption.isTimeCritical(destination.getType())) {
                            // The Destination is not a time critical one!
                            stopAndError("Access denied to Destination " + name + " (not a Time-Critical Destination)");
                            return;
                        }
                        var found = false;
                        for (final ECUser user : MASTER.getDataBaseInterface().getDestinationEcuser(name)) {
                            if (userName.equals(user.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            // The Destination is not authorized for this
                            // time critical user!
                            stopAndError("Access denied to Destination " + name + " (not an authorised user)");
                            return;
                        }
                    }
                    // Have we exceeded the maximum number of pending files?
                    final var maxPending = destination.getMaxPending();
                    final int currentPending;
                    if (maxPending > 0 && (currentPending = MASTER.getManagementInterface()
                            .getPendingDataTransfersCount(name)) > maxPending) {
                        stopAndError("Destination " + name + " have " + currentPending
                                + " pending DataTransfer(s) (limit is " + maxPending + ")");
                        return;
                    }
                    // Have we exceeded the maximum size for the file?
                    final var maxFileSize = destination.getMaxFileSize();
                    if (maxFileSize > 0 && currentSize > maxFileSize) {
                        stopAndError("File exceeds maximum allowed size: " + currentSize + " bytes (limit is "
                                + maxFileSize + " bytes)");
                        return;
                    }
                }
                // Initialize the new DataTransfer(s) and DataFile!
                _log.debug("Create new DataTransfer(s) and associated DataFile");
                provider = new TransferServerProvider("ECpdsPlugin", checkCluster, null, transferGroup,
                        selectedDestination, null);
                final var group = provider.getTransferGroup();
                dataFile.setTransferGroup(group);
                dataFile.setTransferGroupName(group.getName());
                dataFile.setFileSystem(provider.getFileSystem());
                dataFile.setFileInstance(null);
                DATABASE.insert(dataFile, true);
                currentTransfer.setStatusCode(StatusFactory.INIT);
                currentTransfer.setDataFile(dataFile);
                currentTransfer.setDataFileId(dataFile.getId());
                currentTransfer.setBackupHostName(null);
                currentTransfer.setProxyHostName(null);
                currentTransfer.setBackupHost(null);
                currentTransfer.setProxyHost(null);
                currentTransfer.setBackupTime(null);
                currentTransfer.setReplicated(noRetrieval);
                currentTransfer.setReplicateTime(null);
                currentTransfer.setReplicateCount(0);
                currentTransfer.setDeleted(false);
                currentTransfer.setStartCount(0);
                currentTransfer.setRequeueCount(0);
                currentTransfer.setRequeueHistory(0);
                currentTransfer.setStartTime(null);
                currentTransfer.setFinishTime(null);
                currentTransfer.setFirstFinishTime(null);
                currentTransfer.setUniqueKey(key);
                currentTransfer.setPriority(currentPriority);
                currentTransfer.setTarget(currentTarget);
                currentTransfer.setIdentity(identity);
                currentTransfer.setComment(from + ("-".equals(userName) ? "" : " (" + userName + ")"));
                currentTransfer.setSize(currentSize);
                currentTransfer.setEvent(currentEvent);
                currentTransfer.setTimeStep(timeStep);
                currentTransfer.setTimeBase(new Timestamp(currentProductDate));
                final List<DataTransfer> transfers = new ArrayList<>();
                for (final Destination destination : aliases.getDestinations()) {
                    // We only process the statistics for the dissemination destinations which are
                    // monitored and not in standby mode!
                    final var requireMonitoringValue = !standBy
                            && DestinationOption.isDissemination(destination.getType()) && destination.getMonitor();
                    if (requireMonitoringValue && dataFile.getMonitoringValue() == null) {
                        try {
                            final var dafMonitoring = new MonitoringValue();
                            DATABASE.insert(dafMonitoring, true);
                            dataFile.setMonitoringValue(dafMonitoring);
                            dataFile.setMonitoringValueId(dafMonitoring.getId());
                        } catch (final Throwable t) {
                            _log.warn("Creating monitoring value for Destination {}", destination.getName(), t);
                        }
                    }
                    final var destinationName = destination.getName();
                    final var comment = new StringBuilder().append(currentTransfer.getComment());
                    var target = currentTarget;
                    var newExpiry = expiry;
                    var priority = currentPriority;
                    var delay = 0L;
                    var asap = currentAsap;
                    var event = currentEvent;
                    if (!selectedDestination.equals(destinationName)) {
                        // This is not the main Destination so let's use the
                        // updated parameters for this Alias!
                        final var options = aliases.getAliasOptions(destinationName);
                        if (options != null) {
                            target = options.getTarget();
                            final var newLifeTime = Format.getDuration(options.getLifeTime());
                            if (newLifeTime > 0) {
                                if (startAtWithDelay + newLifeTime < date) {
                                    _log.warn(
                                            "The request would already expire (please review -at, -delay and/or alias option for {} in {})",
                                            destinationName, selectedDestination);
                                } else {
                                    _log.debug("Lifetime forced by alias option for {} in {}", destinationName,
                                            selectedDestination);
                                    newExpiry = startAtWithDelay + newLifeTime;
                                }
                            }
                            priority = options.getPriority();
                            delay = options.getDelay();
                            asap = options.getAsap();
                            event = options.getEvent();
                            final var viaMessage = new StringBuilder();
                            var size = options.getVia().size();
                            for (final String desName : options.getVia()) {
                                size--;
                                if (size == 0) {
                                    viaMessage.append(" to ");
                                } else {
                                    viaMessage.append(viaMessage.isEmpty() ? " via " : ", Destination=" + desName);
                                }
                            }
                            comment.append(" aliased from Destination=").append(selectedDestination)
                                    .append(viaMessage.length() > 4 ? viaMessage.toString() : "");
                        } else {
                            _log.warn("No rules for {}?", destinationName);
                        }
                    }
                    final var transfer = (DataTransfer) currentTransfer.clone();
                    if (requireMonitoringValue) {
                        try {
                            final var datMonitoring = new MonitoringValue();
                            DATABASE.insert(datMonitoring, true);
                            transfer.setMonitoringValue(datMonitoring);
                            transfer.setMonitoringValueId(datMonitoring.getId());
                        } catch (final Throwable t) {
                            _log.warn("Creating monitoring value for Destination {}", destination.getName(), t);
                        }
                    }
                    transfer.setComment(comment.toString());
                    transfer.setPriority(priority);
                    transfer.setTarget(target);
                    transfer.setAsap(asap);
                    transfer.setEvent(event);
                    transfer.setScheduledTime(new Timestamp(startAtWithDelay + delay));
                    transfer.setQueueTime(new Timestamp(startAtWithDelay + delay));
                    transfer.setRetryTime(new Timestamp(startAtWithDelay + delay));
                    transfer.setExpiryTime(new Timestamp(newExpiry + delay));
                    transfer.setDestination(destination);
                    transfer.setDestinationName(destinationName);
                    DATABASE.insert(transfer, true);
                    transfers.add(transfer);
                    MASTER.addTransferHistory(transfer);
                }
                transfersList = transfers.toArray(new DataTransfer[transfers.size()]);
            } else {
                // This is re-queue or force, so overwrite the existing DataTransfer(s)!
                transferGroup = dataFile.getTransferGroupName();
                for (final DataTransfer transfer : transfersList) {
                    if (groupBy == null && provider == null) {
                        provider = new TransferServerProvider("ECpdsPlugin", false, dataFile.getFileSystem(),
                                transferGroup, selectedDestination, null);
                    }
                    transfer.setTransferServer(null);
                    transfer.setTransferServerName(null);
                    transfer.setOriginalTransferServer(null);
                    transfer.setOriginalTransferServerName(null);
                    transfer.setScheduledTime(new Timestamp(startAtWithDelay));
                    transfer.setQueueTime(new Timestamp(startAtWithDelay));
                    transfer.setRetryTime(new Timestamp(startAtWithDelay));
                    transfer.setExpiryTime(new Timestamp(expiry));
                    transfer.setUniqueKey(key);
                    transfer.setPriority(currentPriority);
                    transfer.setTarget(currentTarget);
                    transfer.setAsap(currentAsap);
                    transfer.setEvent(currentEvent);
                    transfer.setStartCount(0);
                    transfer.setRequeueCount(0);
                    transfer.setDuration(0);
                    transfer.setSent(0);
                    transfer.setPutTime(null);
                    transfer.setBackupHostName(null);
                    transfer.setProxyHostName(null);
                    transfer.setBackupHost(null);
                    transfer.setProxyHost(null);
                    transfer.setBackupTime(null);
                    transfer.setReplicated(noRetrieval);
                    transfer.setReplicateTime(null);
                    transfer.setReplicateCount(0);
                }
            }
            // Store the MetaData for the DataFile (new or existing one). The
            // "DatafileId" is sent in the message with the exact case (lower
            // case f) to allow the ecpds client to parse it easily. When the
            // "dataFileId" is returned (upper case f) this mean there was a
            // problem (e.g. dataFile already exists)!
            storeMetaData(dataFile);
            if (groupBy != null) {
                _log.debug("Retrieval mode (file downloaded later)");
                message = "MESSAGE " + transfersList.length + " DataTransfer(s) initialized for group " + groupBy + ": "
                        + (standBy ? "standby mode" : Format.formatTime(DEFAULT_DATE_FORMAT, startAtWithDelay))
                        + " (DatafileId=" + dataFile.getId() + ")";
                byeReq();
            } else {
                _log.debug("Push mode (file uploaded through client)");
                send("TARGET " + getPath(dataFile));
                send("ECPROXY " + getECproxyServers(provider.getTransferServers()));
                // The DataFileId is used to track the DownloadProgress on the
                // DataMover!
                dataFileId = dataFile.getId();
                MASTER.lockDataFile(key);
                message = "MESSAGE " + transfersList.length + " DataTransfer(s) "
                        + (standBy ? "in standby"
                                : "will start at: " + Format.formatTime(DEFAULT_DATE_FORMAT, startAtWithDelay))
                        + " (DatafileId=" + dataFileId + ")" + (!newDataFile ? " (updated)" : "");
                send("Please continue");
            }
        } catch (final Throwable t) {
            _log.warn("put", t);
            final var error = t.getMessage();
            stopAndError(error == null ? "aborted by server" : error);
        }
    }

    /**
     * Select req.
     */
    public void selectReq() {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        if (currentSize == -1) {
            _log.error("Missing parameters");
            error("Missing parameters");
        } else {
            String select = null;
            try {
                select = receiveData(getInputStream(), currentSize);
                // The data is returned compressed to lower the memory usage so
                // we have to uncompress it!
                final var in = new ByteArrayInputStream(DATABASE.select(select));
                final var gzip = new GZIPInputStream(in);
                final var array = new byte[2048];
                int read;
                while ((read = gzip.read(array)) != -1) {
                    print(new String(array, 0, read, StandardCharsets.ISO_8859_1));
                }
            } catch (final Exception e) {
                _log.error("Process aborted", e);
                final var error = e.getMessage();
                error(error == null ? "aborted by server" : error);
            } finally {
                if (LOG_REQUESTS) {
                    final var req = new ECpdsSelect();
                    req.setUSER(userName);
                    req.setVERSION(version);
                    req.setSELECT(select);
                    req.store();
                }
            }
        }
        setLoop(false);
    }

    /**
     * Update req.
     */
    public void updateReq() {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        if (currentSize == -1) {
            _log.error("Missing parameters");
            stopAndError("Missing parameters");
        } else {
            try {
                println(DATABASE.update(receiveData(getInputStream(), currentSize)));
            } catch (final Exception e) {
                _log.error("Process aborted", e);
                final var error = e.getMessage();
                error(error == null ? "aborted by server" : error);
            }
        }
        setLoop(false);
    }

    /**
     * Noop req.
     */
    public void noopReq() {
        // Nothing to do
    }

    /**
     * Streams req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void streamsReq(final String[] parameters) throws ParameterException {
        final var streams = getParameter(parameters);
        try {
            if ((currentStreams = Integer.parseInt(streams)) > 0) {
                return;
            }
        } catch (final NumberFormatException e) {
            // Ignored
        }
        stopAndError("Invalid streams (" + streams + ")");
    }

    /**
     * Timeout req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void timeoutReq(final String[] parameters) throws ParameterException {
        final var timeout = getParameter(parameters);
        try {
            if ((currentTimeout = Format.getDuration(timeout)) >= 0) {
                return;
            }
        } catch (final NumberFormatException e) {
            // Ignored
        }
        stopAndError("Invalid timeout (" + timeout + ")");
    }

    /**
     * Destinationstart req.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void destinationstartReq() throws IOException {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        try {
            MASTER.getTransferScheduler().restartDestination(userName, userName, selectedDestination, true, true);
        } catch (final Throwable t) {
            stopAndError(t.getMessage());
            return;
        }
        send("MESSAGE Destination started");
        setLoop(false);
    }

    /**
     * Destinationstop req.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void destinationstopReq() throws IOException {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        try {
            MASTER.getTransferScheduler().holdDestination(userName, selectedDestination, StatusFactory.STOP, true);
        } catch (final Throwable t) {
            stopAndError(t.getMessage());
            return;
        }
        send("MESSAGE Destination stopped");
        setLoop(false);
    }

    /**
     * Schedulerstart req.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void schedulerstartReq() throws IOException {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        try {
            MASTER.getDownloadScheduler(acquisition).setPause(false);
        } catch (final Throwable t) {
            stopAndError(t.getMessage());
            return;
        }
        send("MESSAGE Scheduler started");
        setLoop(false);
    }

    /**
     * Schedulerstop req.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void schedulerstopReq() throws IOException {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        try {
            MASTER.getDownloadScheduler(acquisition).setPause(true);
        } catch (final Throwable t) {
            stopAndError(t.getMessage());
            return;
        }
        send("MESSAGE Scheduler stopped");
        setLoop(false);
    }

    /**
     * Schedulercheck req.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void schedulercheckReq() throws IOException {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        final DownloadScheduler downloadScheduler;
        try {
            downloadScheduler = MASTER.getDownloadScheduler(acquisition);
            if (currentStreams > 0) {
                downloadScheduler.setMaxDownloadThreads(currentStreams);
            }
            if (currentTimeout >= 0) {
                downloadScheduler.setTimeOutDownloadThread(currentTimeout);
            }
        } catch (final Throwable t) {
            stopAndError(t.getMessage());
            return;
        }
        send("MESSAGE Scheduler " + (downloadScheduler.getPause() ? "stopped" : "started") + " (streams="
                + downloadScheduler.getMaxDownloadThreads() + ",timeout=" + downloadScheduler.getTimeOutDownloadThread()
                + ")");
        setLoop(false);
    }

    /**
     * Waitforgroup req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws DataBaseException
     *             the data base exception
     */
    public void waitforgroupReq(final String[] parameters) throws ParameterException, IOException, DataBaseException {
        if (userName == null || remoteIp == null) {
            stopAndError("Please login first");
            return;
        }
        final var group = getParameter(parameters);
        List<Long> dataFileIds;
        final var currentTime = System.currentTimeMillis();
        var lastUpdate = currentTime;
        var found = false;
        int count;
        if (LOG_REQUESTS) {
            final var req = new ECpdsWaitForGroup();
            req.setUSER(userName);
            req.setVERSION(version);
            req.setWAITFORGROUP(group);
            req.store();
        }
        // We loop till we have no files waiting to be retrieved for this group
        // or we never found any files for this group and we have started to
        // loop less than the initial wait allowed!
        while ((count = (dataFileIds = groupThread.getDataFileIdsByGroupByCount(group)).size()) > 0
                || !found && System.currentTimeMillis() - currentTime < WAIT_FOR_INITIAL_WAIT) {
            try {
                Thread.sleep(Timer.ONE_SECOND);
            } catch (final InterruptedException e) {
            }
            if (count > 0) {
                // We have found some files waiting in the queue!
                found = true;
                send(Format.formatTime(System.currentTimeMillis()) + ": Still " + count
                        + " DataFile(s) to process for group " + group);
                if (count < 10 && System.currentTimeMillis() - lastUpdate > WAIT_FOR_REFRESH_INTERVAL) {
                    // Less than 10 files in the queue so let's show the details
                    // to the remote side!
                    lastUpdate = System.currentTimeMillis();
                    for (final Long currentDataFileId : dataFileIds) {
                        final var dataFile = DATABASE.getDataFile(currentDataFileId);
                        if (!dataFile.getDownloaded()) {
                            final var ecauthUser = dataFile.getEcauthUser();
                            final var ecauthHost = dataFile.getEcauthHost();
                            final var header = new StringBuilder(" >> DataFile ").append(currentDataFileId)
                                    .append(": ");
                            if (ecauthUser != null && ecauthHost != null) {
                                header.append(ecauthUser).append("@").append(ecauthHost).append("->");
                            }
                            header.append(dataFile.getOriginal()).append(" (")
                                    .append(Format.formatSize(dataFile.getSize())).append(") - ");
                            // Let's see if we can find some progress
                            // information about the retrieval?
                            final var progress = MASTER.getProgressInterface(dataFile.getId());
                            if (progress != null) {
                                // Let's give some information related to the
                                // retrieval!
                                final var size = dataFile.getSize();
                                final var root = progress.getRoot();
                                header.append(size != -1 ? Format.formatPercentage(size, progress.getByteSent())
                                        : Format.formatSize(progress.getByteSent()))
                                        .append(isNotEmpty(root) ? " retrieved on " + root : "");
                            } else {
                                // No information found so the retrieval is not
                                // happening yet!
                                header.append("retrieval not started yet");
                            }
                            send(header.toString());
                        }
                    }
                }
            }
        }
        // Did we found something ever?
        if (!found) {
            // We had nothing, the files might have been retrieved before we had
            // a chance to find them in the queue?
            long existing;
            for (var i = 0; (existing = DATABASE.getDataFilesByGroupByCount(group)) <= 0 && i < 10; i++) {
                _log.debug("Group {} not found yet (attempt={})", group, i + 1);
                try {
                    Thread.sleep(Timer.ONE_SECOND);
                } catch (final InterruptedException e) {
                }
            }
            if (existing <= 0) {
                // No we didn't find any match for this group!
                stopAndError("Group " + group + " not found (" + existing + ")");
                return;
            }
            // Yes there were some files retrieved for this group!
            if (resetDataTransferSchedulesByGroup) {
                final var currentStart = System.currentTimeMillis();
                final var rowsAffected = DATABASE.resetDataTransferSchedulesByGroup(group);
                if (_log.isDebugEnabled()) {
                    _log.debug("resetDataTransferSchedulesByGroup: {} ({}) - {} DataFile(s) - {} row(s) affected",
                            group, Format.formatDuration(currentStart, System.currentTimeMillis()), existing,
                            rowsAffected);
                }
            }
            send("Group " + group + " completed (" + existing + " DataFile(s))");
        } else {
            // All the files have been retrieved successfully!
            if (resetDataTransferSchedulesByGroup) {
                final var currentStart = System.currentTimeMillis();
                final var rowsAffected = DATABASE.resetDataTransferSchedulesByGroup(group);
                if (_log.isDebugEnabled()) {
                    _log.debug("resetDataTransferSchedulesByGroup: {} ({}) - {} row(s) affected", group,
                            Format.formatDuration(currentStart, System.currentTimeMillis()), rowsAffected);
                }
            }
            send("Group " + group + " completed");
        }
        send("QUIT");
        setLoop(false);
    }

    /**
     * Error req.
     *
     * @param parameters
     *            the parameters
     *
     * @throws ParameterException
     *             the parameter exception
     */
    public void errorReq(final String[] parameters) throws ParameterException {
        if (_log.isErrorEnabled()) {
            _log.error(getParameter(parameters));
        }
    }

    /**
     * Quit req.
     */
    public void quitReq() {
        setLoop(false);
    }

    /**
     * _store meta data.
     *
     * @param dataFile
     *            the data file
     *
     * @throws DataBaseException
     *             the data base exception
     */
    private void storeMetaData(final DataFile dataFile) throws DataBaseException {
        if (metaDataList.size() == 0) {
            // Nothing to process!
            return;
        }
        final var values = DATABASE.getMetaDataByDataFileId(dataFile.getId()).toArray(new MetadataValue[0]);
        for (final String name : metaDataList.keySet()) {
            final var attribute = new MetadataAttribute(name);
            try (final var mutex = metadataAttributeMutexProvider.getMutex(name)) {
                synchronized (mutex.lock()) {
                    if (DATABASE.getMetadataAttributeObject(name) == null) {
                        DATABASE.insert(attribute, false);
                    }
                }
            }
            // Check if the metadata exists (insert || update)
            var found = false;
            for (final MetadataValue value : values) {
                if (name.equals(value.getMetadataAttributeName())) {
                    value.setValue(metaDataList.get(name));
                    DATABASE.update(value);
                    found = true;
                    break;
                }
            }
            if (!found) {
                final var value = new MetadataValue();
                value.setValue(metaDataList.get(name));
                value.setDataFile(dataFile);
                value.setDataFileId(dataFile.getId());
                value.setMetadataAttribute(attribute);
                value.setMetadataAttributeName(name);
                DATABASE.insert(value, true);
            }
        }
    }

    /**
     * _receive data.
     *
     * @param in
     *            the in
     * @param size
     *            the size
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String receiveData(final InputStream in, long size) throws IOException {
        final var data = new StringBuilder();
        final var buffer = Cnf.at("Other", "buffer", 65536);
        final var buf = new byte[buffer];
        int read;
        while (size > 0 && (read = in.read(buf, 0, size < buffer ? (int) size : buffer)) != -1) {
            data.append(new String(buf, 0, read));
            size -= read;
        }
        if (size != 0) {
            throw new IOException("Data not complete: " + size);
        }
        return data.toString();
    }

    /**
     * Gets the ecproxy servers.
     *
     * @param ecproxyList
     *            the ecproxy list
     *
     * @return the string
     */
    private static String getECproxyServers(final List<TransferServer> ecproxyList) {
        final var list = new StringBuilder();
        for (final TransferServer mover : ecproxyList) {
            list.append(list.length() > 0 ? "|" : "").append(mover.getHost()).append(":").append(mover.getPort());
        }
        return list.toString();
    }
}
