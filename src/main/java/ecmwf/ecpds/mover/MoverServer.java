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

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECTRANS;
import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_PROXY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_CHECKFILTERSIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_FILTERPATTERN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_FILTER_MINIMUM_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_HOST_SELECTOR;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_FILTER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_STREAM_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_USEDNSNAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_PROXY_HTTP_MOVER_URL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_PROXY_HTTP_PROXY_URL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_PROXY_MODULO;
import static ecmwf.common.ectrans.ECtransOptions.HOST_PROXY_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_TEST_BYTES_PER_SEC;
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;
import static ecmwf.ecpds.master.DataFilePath.getDir;
import static ecmwf.ecpds.master.DataFilePath.getPath;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.timer.Timer;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;

import ecmwf.common.callback.RemoteInputStream;
import ecmwf.common.callback.RemoteInputStreamImp;
import ecmwf.common.checksum.Checksum;
import ecmwf.common.checksum.Checksum.Algorithm;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.ExistingStorageDirectory;
import ecmwf.common.database.Host;
import ecmwf.common.database.IncomingConnection;
import ecmwf.common.database.MSUser;
import ecmwf.common.database.TransferMethod;
import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.common.ecaccess.ECauthTokenManager;
import ecmwf.common.ecaccess.MBeanRepository;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ecaccess.TicketRepository;
import ecmwf.common.ectrans.AllocateInterface;
import ecmwf.common.ectrans.DefaultCallback;
import ecmwf.common.ectrans.ECtransCallback;
import ecmwf.common.ectrans.ECtransContainer;
import ecmwf.common.ectrans.ECtransDel;
import ecmwf.common.ectrans.ECtransException;
import ecmwf.common.ectrans.ECtransGet;
import ecmwf.common.ectrans.ECtransList;
import ecmwf.common.ectrans.ECtransMkdir;
import ecmwf.common.ectrans.ECtransMove;
import ecmwf.common.ectrans.ECtransProvider;
import ecmwf.common.ectrans.ECtransPut;
import ecmwf.common.ectrans.ECtransRmdir;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.ECtransSize;
import ecmwf.common.ectrans.NotificationInterface;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.monitor.MonitorCallback;
import ecmwf.common.monitor.MonitorException;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.common.monitor.MonitorThread;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.rmi.interruptible.InterruptibleInputStream;
import ecmwf.common.starter.Starter;
import ecmwf.common.technical.BoundedInputStream;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.GenericFile;
import ecmwf.common.technical.GenericFileChecker;
import ecmwf.common.technical.GenericFileFilter;
import ecmwf.common.technical.MonitoredInputStream;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;
import ecmwf.common.technical.ProgressHandler;
import ecmwf.common.technical.ProgressInterface;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.RandomInputStream;
import ecmwf.common.technical.ScriptManager;
import ecmwf.common.technical.StreamManager;
import ecmwf.common.technical.StreamManagerImp;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.Synchronized;
import ecmwf.common.technical.ThreadService;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.common.text.Format.DuplicatedChooseScore;
import ecmwf.common.text.Options;
import ecmwf.common.version.Version;
import ecmwf.ecpds.master.DataAccessInterface;
import ecmwf.ecpds.master.DownloadProgress;
import ecmwf.ecpds.master.MasterConnection;
import ecmwf.ecpds.master.MasterException;
import ecmwf.ecpds.master.MasterInterface;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferComparator;

/**
 * The Class MoverServer.
 */
public final class MoverServer extends StarterServer implements MoverInterface {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8236856567764955573L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(MoverServer.class);

    /** The Constant operational. */
    private static final transient boolean OPERATIONAL = Cnf.at("Mover", "operational", true);

    /** The _algorithm used for the hash of the files. */
    private static final Algorithm _algorithm = Algorithm.ADLER32;

    /** The ectrans. */
    private final transient ECtransContainer ectrans;

    /** The masterManager. */
    private final transient MasterManager masterManager;

    /** The transferRepository. */
    private final transient TransferRepository transferRepository;

    /** The downloadRepository. */
    private final transient DownloadRepository downloadRepository;

    /** The ticketRepository. */
    private final transient TicketRepository ticketRepository;

    /** The moverRepository. */
    private final transient MoverRepository moverRepository;

    /** The dataTransferMutexProvider. */
    private final transient Synchronized dataTransferMutexProvider = new Synchronized();

    /** The fileCheckerMonitor. */
    private final transient FileCheckerMonitor fileCheckerMonitor;

    /** The masterSynch. */
    private final transient Object masterSynch = new Object();

    /** The masterSynch. */
    private final transient ECauthTokenGenerator tokenGenerator;

    /** The errorsSync. */
    private final transient Object errorsSync = new Object();

    /** The errorsFrequency. */
    private final transient Integer errorsFrequency = Cnf.at("TestModule", "errorsFrequency", 1000);

    /** The restProvider. */
    private transient RESTProvider restProvider = null;

    /** The mqttInterface. */
    private transient MQTTInterface mqttInterface = null;

    /** The masterProxy. */
    private transient MasterProxy masterProxy = null;

    /** The purgeThread. */
    private transient PurgeThread purgeThread = null;

    /** The incomingUsersManagementThread. */
    private transient IncomingUsersManagementThread incomingUsersManagementThread = null;

    /** The errors. */
    private int errors = 0;

    /**
     * The Class ContextManagementThread.
     */
    final class IncomingUsersManagementThread extends ConfigurableLoopRunnable {
        /** The max wait time. */
        private final long maxWaitTime = Cnf.durationAt("IncomingUsersManagementThread", "maxWaitTime",
                2 * Timer.ONE_MINUTE);

        /** The old connections. */
        private final List<IncomingConnection> oldConnections = new ArrayList<>();

        /** The new connections. */
        private final List<IncomingConnection> newConnections = new ArrayList<>();

        /** The last update. */
        private long lastUpdate = System.currentTimeMillis();

        /** The last update. */
        private boolean reset = true;

        /**
         * Instantiates a new incoming users management thread.
         */
        IncomingUsersManagementThread() {
            setPause(Cnf.at("IncomingUsersManagementThread", "delay", 2000));
        }

        /**
         * Configurable loop run.
         */
        @Override
        public synchronized void configurableLoopRun() {
            try {
                if (masterManager.isConnected()) {
                    // The MoverServer is connected to the MasterServer!
                    newConnections.clear();
                    newConnections.addAll(getIncomingConnections());
                    final var currentTime = System.currentTimeMillis();
                    if (reset || currentTime - lastUpdate > maxWaitTime || !newConnections.equals(oldConnections)) {
                        _log.debug("Sending Incoming Connections to MasterServer ({})", newConnections.size());
                        getMasterInterface().updateIncomingConnectionIds(getRoot(), newConnections);
                        oldConnections.clear();
                        oldConnections.addAll(newConnections);
                        lastUpdate = currentTime;
                        reset = false;
                    }
                } else {
                    // Not connected so we initiate a reset to force the MoverServer to re-send the
                    // list of Incoming Connections!
                    reset = true;
                }
            } catch (final Throwable t) {
                _log.warn("Sending incoming connection ids", t);
            }
        }
    }

    /**
     * The Class DataFileFilter.
     */
    static final class DataFileFilter implements GenericFileFilter {
        /** The filePath. */
        private final String filePath;

        /**
         * Instantiates a new data file filter.
         *
         * @param file
         *            the file
         */
        DataFileFilter(final GenericFile file) {
            this.filePath = file.getAbsolutePath();
        }

        /**
         * Accept.
         *
         * @param dir
         *            the dir
         * @param name
         *            the name
         *
         * @return true, if successful
         */
        @Override
        public boolean accept(final GenericFile dir, final String name) {
            final var path = GenericFile.getGenericFile(dir, name).getAbsolutePath();
            return path.equals(filePath) || path.startsWith(filePath + ".");
        }
    }

    /**
     * The Class FileCheckerMonitor.
     */
    private static final class FileCheckerMonitor implements MonitorCallback {
        /**
         * Start.
         */
        public void start() {
            if (MonitorManager.isActivated()) {
                try {
                    MonitorThread.getInstance().subscribe("FileSystem", this);
                } catch (final MonitorException e) {
                    _log.debug(e);
                }
            }
        }

        /**
         * Stop.
         */
        public void stop() {
            if (MonitorManager.isActivated()) {
                try {
                    MonitorThread.getInstance().unSubscribe("FileSystem");
                } catch (final MonitorException e) {
                    _log.debug(e);
                }
            }
        }

        /**
         * Gets the monitor manager.
         *
         * @param name
         *            the name
         *
         * @return the monitor manager
         *
         * @throws MonitorException
         *             the monitor exception
         */
        @Override
        public MonitorManager getMonitorManager(final String name) throws MonitorException {
            final var hasException = FileChecker.hasException();
            final var path = FileChecker.path;
            // The name is always FileSystem!
            try {
                return new MonitorManager(
                        Cnf.at("Login", "service",
                                Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName())),
                        name, hasException ? MonitorManager.RED : MonitorManager.GREEN,
                        hasException ? path != null ? path : "WARNING" : "OK");
            } catch (final Throwable t) {
                throw new MonitorException(t.getMessage());
            }
        }
    }

    /**
     * The Class PurgeThread. This Thread is used to purge the file system (e.g. remove expired directories from the
     * repository).
     */
    private final class PurgeThread extends ConfigurableRunnable {
        /** The directoriesList. */
        final List<ExistingStorageDirectory> directoriesList = new ArrayList<>();

        /** The shouldContinue. */
        boolean shouldContinue = true;

        /** The force. */
        final boolean force;

        /**
         * Instantiates a new purge thread.
         *
         * @param directories
         *            the directories
         */
        public PurgeThread(final List<ExistingStorageDirectory> directories) {
            directoriesList.addAll(directories);
            force = false;
        }

        /**
         * Instantiates a new purge thread.
         */
        public PurgeThread() {
            force = true;
        }

        /**
         * worthChecking.
         *
         * @param dir
         *            the dir
         * @param listCount
         *            the list count
         * @param listSize
         *            the list size
         *
         * @return true, if successful
         */
        private boolean worthChecking(final String dir, final long listCount, final long listSize) {
            for (final ExistingStorageDirectory directory : directoriesList) {
                if (dir.equals(getDir(directory.getFileSystem(), directory.getArrivedTime()))) {
                    final long count = directory.getFilesCount();
                    final var size = directory.getFilesSize();
                    if (count < listCount || size < listSize) {
                        // There is a difference so we have to process the directories to check if files
                        // can be removed to recover some disk space?
                        _log.info("Spotted a difference in count/size for dir: {} (count:{}/{}, size:{}/{})", dir,
                                count, listCount, size, listSize);
                        return true;
                    }
                    // Same number of files and size so we do nothing!
                    return false;
                }
            }
            return true;
        }

        /**
         * Stop.
         */
        public void stop() {
            shouldContinue = false;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            try {
                final var repository = GenericFile.getGenericFile(getRepository());
                final var volumes = repository.listFiles();
                if (volumes == null) {
                    // The repository is not a directory?
                    _log.warn("Repository {} not a directory?", repository.getAbsolutePath());
                    return;
                }
                final List<String> alreadyProcessed = new ArrayList<>();
                for (final GenericFile dataVolume : volumes) {
                    final var realPath = dataVolume.getFile().toPath().toRealPath().toString();
                    if (alreadyProcessed.contains(realPath)) {
                        // This volume was already processed!
                        _log.debug("Skipping dataVolume {} (already processed - symbolic link?)",
                                dataVolume.getAbsolutePath());
                        continue;
                    }
                    alreadyProcessed.add(realPath);
                    final var times = dataVolume.listFiles();
                    if (times == null) {
                        // This volume is not a directory?
                        _log.debug("Skipping dataVolume {} (not a directory)", dataVolume.getAbsolutePath());
                        continue;
                    }
                    for (final GenericFile arrivedTime : times) {
                        if (force || worthChecking(dataVolume.getName() + "/" + arrivedTime.getName(),
                                arrivedTime.listCount(), arrivedTime.listSize())) {
                            // We can scan this directory!
                            final var steps = arrivedTime.listFiles();
                            var stepsDeleted = 0;
                            if (steps == null) {
                                // This is not a directory?
                                _log.debug("Skipping arrivedTime {} (not a directory)", arrivedTime.getAbsolutePath());
                                continue;
                            }
                            for (final GenericFile dataStep : steps) {
                                // Let's scan this directory to look for
                                // DataFiles!
                                final var files = dataStep.listFiles();
                                var filesDeleted = 0;
                                if (files == null) {
                                    // This is not a directory?
                                    _log.debug("Skipping dataStep {} (not a directory)", dataStep.getAbsolutePath());
                                    continue;
                                }
                                for (final GenericFile dataFile : files) {
                                    if (!shouldContinue) {
                                        // We are asked to stop the
                                        // activity!
                                        _log.debug("Stopping purge");
                                        return;
                                    }
                                    // Let's process this DataFile!
                                    final var name = dataFile.getName();
                                    final var path = dataFile.getAbsolutePath();
                                    try {
                                        if (dataFile.isFile() && name.length() > 10 && getMasterProxy()
                                                .isValidDataFile(Long.parseLong(name.substring(0, 10)))) {
                                            // This is a valid DataFile!
                                            _log.debug("DataFile {} is still valid (keep it)", path);
                                            continue;
                                        }
                                    } catch (final NumberFormatException e) {
                                        // This is not a DataFile (bad name format)!
                                        _log.warn("File {} not a DataFile (please remove it from repository)", path);
                                        continue;
                                    }
                                    // This is not a valid DataFile!
                                    final var deleted = dataFile.delete();
                                    _log.debug("Deleting file {} (not a valid DataFile): {}", path, deleted);
                                    if (deleted) {
                                        filesDeleted++;
                                    }
                                }
                                // Let's remove the dataStep directory if all
                                // the files have been deleted!
                                if (filesDeleted == files.length) {
                                    final var deleted = dataStep.delete(true);
                                    _log.info("Removed empty directory: {}: {}", dataStep.getAbsolutePath(), deleted);
                                    stepsDeleted++;
                                }
                            }
                            // Let's remove the arrivedTime directory if all
                            // the steps have been deleted!
                            if (stepsDeleted == steps.length) {
                                final var deleted = arrivedTime.delete(true);
                                _log.info("Removed empty directory: {}: {}", arrivedTime.getAbsolutePath(), deleted);
                            }
                        } else {
                            // The file exists and should not be deleted for
                            // now!
                            _log.debug("Skipping arrivedTime {} (not expired)", arrivedTime.getAbsolutePath());
                        }
                    }
                }
            } catch (final Throwable t) {
                _log.warn("configurableRun", t);
            }
        }
    }

    /**
     * The Class FileChecker.
     */
    private static final class FileChecker extends GenericFileChecker {
        /** The Constant _fs. */
        private static final Logger _fs = LogManager.getLogger("FileSystemLogs");

        /** The lastException. */
        private static boolean lastException = false;

        /** The lastExceptionTime. */
        private static long lastExceptionTime = -1;

        /** The lastExceptionUpdateTime. */
        private static long lastExceptionUpdateTime = -1;

        /** The path. */
        public static String path = null;

        /**
         * Checks for exception.
         *
         * @return true, if successful
         */
        protected static synchronized boolean hasException() {
            if (lastException && System.currentTimeMillis() - lastExceptionTime > 5 * Timer.ONE_MINUTE) {
                lastException = false;
            }
            return lastException && System.currentTimeMillis() - lastExceptionUpdateTime > 6 * Timer.ONE_MINUTE;
        }

        /**
         * Sets the exception.
         *
         * @param currentPath
         *            the current path
         */
        private static synchronized void setException(final String currentPath) {
            lastExceptionTime = System.currentTimeMillis();
            if (!lastException) {
                lastExceptionUpdateTime = System.currentTimeMillis();
                lastException = true;
            }
            path = currentPath;
        }

        /**
         * Instantiates a new file checker.
         *
         * @param file
         *            the file
         */
        FileChecker(final GenericFile file) {
            super(file);
        }

        /**
         * Catch exception.
         *
         * @param service
         *            the service
         * @param t
         *            the t
         */
        @Override
        public void catchException(final String service, final Throwable t) {
            if (t instanceof IOException && "Stream Closed".equals(t.getMessage())) {
                // This is because the stream was closed and does not indicate a
                // problem with the filesystem so we should ignore it (this can
                // happen if the transmission is interrupted).
                return;
            }
            final var currentPath = super.getGenericFile().getAbsolutePath();
            _fs.info("Executing {} on {}", service, currentPath, t);
            if (t instanceof IOException && "InputStream.read".equals(service)
                    && "Input/output error".equals(t.getMessage())) {
                _log.warn("Deleting file: {} (Input/output error)", currentPath);
                // TODO: don't delete the file before doing a better checking of
                // the file (it might be a temporary error)!
                try {
                    delete();
                } catch (final Throwable e) {
                    _log.debug(e);
                }
            }
            setException(currentPath);
        }

        /**
         * Gets the generic file.
         *
         * @return the generic file
         */
        @Override
        public GenericFile getGenericFile() {
            return new FileChecker(super.getGenericFile());
        }

        /**
         * Gets the parent file.
         *
         * @return the parent file
         */
        @Override
        public GenericFile getParentFile() {
            return new FileChecker(super.getParentFile());
        }
    }

    /**
     * The Class ECauthTokenGenerator.
     */
    private final class ECauthTokenGenerator extends ECauthTokenManager {

        /**
         * Request E cauth token.
         *
         * @param user
         *            the user
         *
         * @return the ecauth token
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public ECauthToken requestECauthToken(final String user) throws IOException {
            try {
                return getMasterProxy().getECauthToken(user);
            } catch (final Exception e) {
                _log.warn("getECauthToken", e);
                throw new IOException("Getting ECauthToken for " + user, e);
            }
        }
    }

    /**
     * Is called whenever the DataMover lost the connection with its MasterServer. This method can be called from the
     * MasterManager if it is connected through RMI or from the TransferRepository if it is connected through the REST
     * interface.
     */
    public void resetDataMover() {
        _log.warn("Reseting current DataMover");
        try {
            ectrans.close();
        } catch (final Throwable e) {
            _log.warn(e);
        }
        _log.debug("Reset transfer(s)");
        if (transferRepository != null)
            transferRepository.clear();
        _log.debug("Reset download(s)");
        if (downloadRepository != null)
            downloadRepository.clear();
        if (masterManager != null) {
            _log.debug("Close incoming connection(s)");
            try {
                closeAllIncomingConnections();
            } catch (final Throwable e) {
                _log.warn(e);
            }
        }
    }

    /**
     * Sets the REST provider.
     *
     * @param restProvider
     *            the new REST provider
     */
    public void setRESTProvider(final RESTProvider restProvider) {
        _log.debug("RESTProvider registered: {}", restProvider.getClass().getCanonicalName());
        this.restProvider = restProvider;
    }

    /**
     * Gets the REST interface.
     *
     * @param httpProxy
     *            the http proxy
     * @param httpMover
     *            the http mover
     * @param connectTimeout
     *            the connect timeout
     *
     * @return the REST interface
     *
     * @throws ecmwf.ecpds.mover.MoverException
     *             the mover exception
     */
    public RESTInterface getRESTInterface(final String httpProxy, final String httpMover, final int connectTimeout)
            throws MoverException {
        if (restProvider == null) {
            throw new MoverException("No RESTProvider available");
        }
        return restProvider.getRESTInterface(httpProxy, httpMover, connectTimeout);
    }

    /**
     * Sets the MQTT interface.
     *
     * @param mqttInterface
     *            the new MQTT interface
     */
    public void setMQTTInterface(final MQTTInterface mqttInterface) {
        _log.debug("MQTTInterface registered: {}", mqttInterface.getClass().getCanonicalName());
        this.mqttInterface = mqttInterface;
    }

    /**
     * Gets the MQTT interface.
     *
     * @return the MQTT interface
     *
     * @throws ecmwf.ecpds.mover.MoverException
     *             the mover exception
     */
    public MQTTInterface getMQTTInterface() throws MoverException {
        if (mqttInterface == null) {
            throw new MoverException("No MQTTInterface available");
        }
        return mqttInterface;
    }

    /**
     * Gets the generic file.
     *
     * @param path
     *            the path
     *
     * @return the generic file
     */
    public GenericFile getGenericFile(final String path) {
        return new FileChecker(GenericFile.getGenericFile(getRepository(), path));
    }

    /**
     * Gets the cookie.
     *
     * @param transfer
     *            the transfer
     * @param startCount
     *            the start count
     * @param action
     *            the action
     *
     * @return the string
     */
    private static String _getCookie(final DataTransfer transfer, final int startCount, final String action) {
        final var actionSpecified = isNotEmpty(action);
        return (actionSpecified ? action + "(" : "") + "DataTransfer-" + transfer.getId() + "." + startCount
                + (actionSpecified ? ")" : "");
    }

    /**
     * Gets the cookie.
     *
     * @param transfer
     *            the transfer
     * @param action
     *            the action
     *
     * @return the string
     */
    private static String _getCookie(final DataTransfer transfer, final String action) {
        return _getCookie(transfer, transfer.getStartCount(), action);
    }

    /**
     * Gets the cookie.
     *
     * @param file
     *            the file
     * @param action
     *            the action
     *
     * @return the string
     */
    private static String _getCookie(final DataFile file, final String action) {
        final var instance = file.getFileInstance();
        final var actionSpecified = isNotEmpty(action);
        return (actionSpecified ? action + "(" : "") + "DataFile-" + file.getId()
                + (instance != null ? "." + instance : "") + (actionSpecified ? ")" : "");
    }

    /**
     * Gets the data file access interface.
     *
     * @return the data file access interface
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public DataAccessInterface getDataFileAccessInterface() throws MasterException {
        try {
            return getMasterProxy().getDataFileAccessInterface();
        } catch (final Throwable t) {
            throw new MasterException(t.getMessage());
        }
    }

    /**
     * Instantiates a new mover server.
     *
     * @param starter
     *            the starter
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws javax.management.InstanceAlreadyExistsException
     *             the instance already exists exception
     * @throws javax.management.MBeanRegistrationException
     *             the MBean registration exception
     * @throws javax.management.NotCompliantMBeanException
     *             the not compliant m bean exception
     * @throws javax.management.MalformedObjectNameException
     *             the malformed object name exception
     * @throws javax.management.InstanceNotFoundException
     *             the instance not found exception
     * @throws ecmwf.common.ecaccess.ConnectionException
     *             the connection exception
     */
    public MoverServer(final Starter starter)
            throws IOException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException, InstanceNotFoundException, ConnectionException {
        super(starter);
        _log.info("MoverServer-version: {}", (Supplier<Object>) Version::getFullVersion);
        if (!OPERATIONAL) {
            _log.warn("!!!!! THIS IS NOT AN OPERATIONAL DATA-MOVER !!!!!");
        }
        tokenGenerator = new ECauthTokenGenerator();
        NativeAuthenticationProvider.setProvider(ecmwf.ecpds.mover.MoverProvider.class);
        if (Cnf.has("MasterServer")) {
            // The DataMover is connected directly to the MasterServer!
            _log.debug("Direct connection to MasterServer requested");
            masterManager = new MasterManager(Cnf.at("MasterServer", "host"),
                    Cnf.at("MasterServer", "port", (short) 6601), this);
        } else {
            masterManager = null;
            // The DataMover is connecting with a REST interface so the
            // Monitor messages must be sent as a JSON message to one of the
            // RMI data-mover available!
            MonitorManager.setProvider(
                    (name, service, status, message) -> getMasterProxy().sendMessage(name, service, status, message));
        }
        if (Cnf.at("Server", "ticketRepository", true)) {
            _log.debug("Starting TicketRepository");
            ticketRepository = new TicketRepository("TicketRepository");
            ticketRepository.setTimeOut(Timer.ONE_DAY);
            ticketRepository.start();
        } else {
            ticketRepository = null;
        }
        if (Cnf.at("Server", "transferRepository", true)) {
            _log.debug("Starting TransferRepository");
            transferRepository = new TransferRepository("TransferRepository");
            transferRepository.start();
        } else {
            transferRepository = null;
        }
        if (Cnf.at("Server", "downloadRepository", true)) {
            _log.debug("Starting DownloadRepository");
            downloadRepository = new DownloadRepository("DownloadRepository");
            downloadRepository.start();
        } else {
            downloadRepository = null;
        }
        moverRepository = new MoverRepository();
        ectrans = new ECtransContainer(new MoverProvider(moverRepository, false));
        try {
            new MBeanManager("ECaccess:service=ECTrans", ectrans);
        } catch (final Exception e) {
            _log.debug(e);
        }
        final var container = getPluginContainer();
        container.loadPlugins();
        container.startPlugins();
        fileCheckerMonitor = new FileCheckerMonitor();
        fileCheckerMonitor.start();
        if (masterManager != null) {
            // Directly connected to the MasterServer!
            masterManager.setStarted(true);
            incomingUsersManagementThread = new IncomingUsersManagementThread();
            incomingUsersManagementThread.execute();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ECproxy plugging listen address and port. This is used by the Master Server to update the
     * "TransferServer" entry in the database when the Mover subscribe. This address and port are used when the Master
     * Server send the address of the allocated Mover to the ecpds command.
     */
    @Override
    public String getECproxyAddressAndPort() {
        final var socketConfig = new SocketConfig("ECproxyPlugin");
        return socketConfig.getPublicAddress() + ":" + socketConfig.getPort();
    }

    /**
     * _check in progress.
     *
     * @param transfer
     *            the transfer
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _checkInProgress(final DataTransfer transfer) throws ECtransException, IOException {
        for (var i = 0; i < 5; i++) {
            if (transferRepository.delDataTransfer(i == 4, transfer.getId())) {
                return;
            }
            _log.warn("Try to close transfer {}", transfer.getId());
            close(transfer);
            try {
                Thread.sleep(Timer.ONE_SECOND);
            } catch (final InterruptedException e) {
            }
        }
        _log.warn("Transfer {} already in progress", transfer.getId());
        throw new ECtransException("Transfer " + transfer.getId() + " already in progress");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming connection ids.
     */
    @Override
    public String[] getIncomingConnectionIds() {
        return ecmwf.ecpds.mover.MoverProvider.getIncomingConnectionIds();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the incoming connections.
     */
    @Override
    public List<IncomingConnection> getIncomingConnections() {
        return ecmwf.ecpds.mover.MoverProvider.getIncomingConnections();
    }

    /**
     * {@inheritDoc}
     *
     * Close incoming connection.
     */
    @Override
    public boolean closeIncomingConnection(final String id) {
        _log.debug("Closing incoming connections: {}", id);
        final var result = ecmwf.ecpds.mover.MoverProvider.closeIncomingConnection(id);
        // Notify the MasterServer to clear the list!
        if (incomingUsersManagementThread != null) {
            incomingUsersManagementThread.configurableLoopRun();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * Close all incoming connections.
     */
    @Override
    public void closeAllIncomingConnections() {
        _log.debug("Closing all incoming connections");
        ecmwf.ecpds.mover.MoverProvider.closeAllIncomingConnections();
        // Notify the MasterServer to clear the list!
        if (incomingUsersManagementThread != null) {
            incomingUsersManagementThread.configurableLoopRun();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Publish a notification to the registered MQTT service (if any).
     */
    @Override
    public void publishToMQTTBroker(final String topic, final int qos, final long expiryInterval,
            final String contentType, final String clientId, final String payload, final boolean retain) {
        if (mqttInterface != null) {
            mqttInterface.publish(topic, qos, expiryInterval, contentType, clientId, payload, retain);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Remove a retained notification from the MQTT broker.
     */
    @Override
    public void removeFromMQTTBroker(final String topic) {
        if (mqttInterface != null) {
            mqttInterface.remove(topic);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Get the number of client connected to the MQTT broker.
     */
    @Override
    public int getMQTTClientsCount() {
        return mqttInterface != null ? mqttInterface.clientsCount() : 0;
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     */
    public static String getRepository() {
        return Cnf.at("ECproxyPlugin", "repository");
    }

    /**
     * Gets the filter generic file.
     *
     * @param file
     *            the file
     * @param filter
     *            the filter
     *
     * @return the generic file
     */
    private static GenericFile _getFilterGenericFile(final GenericFile file, final String filter) {
        return new FileChecker(GenericFile.getGenericFile(file.getParent(),
                file.getName() + "." + filter.toLowerCase().replace(',', '-')));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the root.
     */
    @Override
    public String getRoot() {
        return Cnf.at("Login", "root", Cnf.at("Login", "hostName"));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the service.
     */
    @Override
    public String getService() {
        if (Cnf.has("MasterServer")) {
            // The DataMover is connected directly to the MasterServer!
            return "DataMover";
        }
        // The DataMover is connecting with a REST interface!
        return "DataProxy";
    }

    /**
     * {@inheritDoc}
     *
     * Gets the password.
     */
    @Override
    public String getPassword() {
        return Cnf.at("Login", "password");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the version.
     */
    @Override
    public String getVersion() {
        return Version.getFullVersion();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(), "The ECpds MoverServer deals with data transfers",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("MonitorDebug", "java.lang.Boolean",
                                "MonitorDebug: debug move of Monitor.", true, true, false),
                        new MBeanAttributeInfo("MonitorActivated", "java.lang.Boolean",
                                "MonitorActivated: Monitor activated.", true, false, false),
                        new MBeanAttributeInfo("Connected", "java.lang.Boolean",
                                "Connected: connected to the MasterServer.", true, false, false),
                        new MBeanAttributeInfo("Root", "java.lang.String", "Root: gateway reference name.", true, false,
                                false),
                        new MBeanAttributeInfo("IncomingConnections", "java.lang.String",
                                "IncomingConnections: list of all incoming connections.", true, false, false),
                        new MBeanAttributeInfo("SynchronizedCount", "java.lang.Long",
                                "SynchronizedCount: total number of elements for all instances of Synchronized.", true,
                                false, false) },
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("purgeAllDirectories",
                                "purgeAllDirectories(): remove expired files from all directories",
                                new MBeanParameterInfo[0], "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("closeIncomingConnection",
                                "closeIncomingConnection(id): close incoming connection",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("id", "java.lang.String", "id to close") },
                                "java.lang.Boolean", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("closeAllIncomingConnections",
                                "closeAllIncomingConnections: close all incoming connections",
                                new MBeanParameterInfo[0], "java.lang.Boolean", MBeanOperationInfo.ACTION) });
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("MonitorDebug".equals(name)) {
            MonitorManager.setDebug(((Boolean) value));
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("SynchronizedCount".equals(attributeName)) {
                return Synchronized.getSize();
            }
            if ("MonitorDebug".equals(attributeName)) {
                return MonitorManager.isDebug();
            }
            if ("MonitorActivated".equals(attributeName)) {
                return MonitorManager.isActivated();
            }
            if ("Root".equals(attributeName)) {
                return getRoot();
            }
            if ("IncomingConnections".equals(attributeName)) {
                final var result = new StringBuilder();
                for (final String id : getIncomingConnectionIds()) {
                    result.append(result.length() > 0 ? " " : "").append(id);
                }
                return result.toString();
            }
            if ("Connected".equals(attributeName)) {
                return masterManager != null && masterManager.isConnected();
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * {@inheritDoc}
     *
     * Invoke.
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("purgeAllDirectories".equals(operationName) && signature.length == 0) {
                purge();
                return Boolean.TRUE;
            }
            if ("closeIncomingConnection".equals(operationName) && signature.length == 1
                    && "java.lang.String".equals(signature[0])) {
                return closeIncomingConnection((String) params[0]);
            }
            if ("closeAllIncomingConnections".equals(operationName) && signature.length == 0) {
                closeAllIncomingConnections();
                return Boolean.TRUE;
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }

    /**
     * {@inheritDoc}
     *
     * Check.
     */
    @Override
    public void check(final long ticket) throws IOException {
        ticketRepository.check(ticket, Cnf.at("Other", "ticketWaitDuration", 20 * Timer.ONE_MINUTE));
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final DataTransfer transfer, final String fileName) throws ECtransException, IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(transfer, "size"));
        try {
            _checkInProgress(transfer);
            final var host = transfer.getHost();
            final var size = new ECtransSize(fileName);
            transferRepository.put(transfer);
            try {
                ectrans.syncExec(size, _getCookie(transfer, null), host.getECUserName(),
                        host.getName() + "@" + host.getTransferMethodName(), null,
                        new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
            } finally {
                transferRepository.removeValue(transfer);
            }
            return size.getSize();
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final DataTransfer transfer, final String fileName) throws ECtransException, IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(transfer, "del"));
        try {
            _checkInProgress(transfer);
            final var host = transfer.getHost();
            transferRepository.put(transfer);
            try {
                ectrans.syncExec(new ECtransDel(fileName, true), _getCookie(transfer, null), host.getECUserName(),
                        host.getName() + "@" + host.getTransferMethodName(), null,
                        new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
            } finally {
                transferRepository.removeValue(transfer);
            }
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Filter.
     */
    @Override
    public DataFile filter(final DataFile dataFile, final boolean remove) throws IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(dataFile, "filter"));
        try {
            final var filter = StreamManagerImp.getFilters(dataFile.getFilterName(), dataFile.getSize());
            if (!StreamManagerImp.isFiltered(filter)) {
                _log.warn("DataFile {} has no valid filter specified ({})", dataFile.getId(), filter);
                return dataFile;
            }
            final GenericFile in = new FileChecker(GenericFile.getGenericFile(getRepository(), getPath(dataFile)));
            if (!in.exists() || !in.canRead()) {
                throw new FileNotFoundException("File to filter not on DataMover " + getRoot()
                        + " (not replicated yet?): " + in.getAbsolutePath());
            }
            final var out = _getFilterGenericFile(in, filter);
            final var tmp = _getFilterGenericFile(in, filter + ".tmp");
            final var outFileName = out.getAbsolutePath();
            final var tmpFileName = tmp.getAbsolutePath();
            if (tmp.delete()) {
                _log.warn("DataFile {} temporary filtered file removed: {}", dataFile.getId(), tmpFileName);
            }
            if (out.exists()) {
                _log.warn("DataFile {} already filtered: {}", dataFile.getId(), outFileName);
                dataFile.setFilterSize(out.length());
                return dataFile;
            }
            _log.info("Filtering DataFile {} with filter {}: {}", dataFile.getId(), filter, tmpFileName);
            InputStream fis = null;
            OutputStream fos = null;
            var success = false;
            final var fileSize = dataFile.getSize();
            final var cheksumAlgorithm = _algorithm.getName();
            final var checksum = dataFile.getChecksum();
            final var processChecksum = !"none".equalsIgnoreCase(checksum);
            Checksum checksumOriginal = null;
            Checksum checksumFiltered = null;
            dataFile.setChecksum(null);
            var size = 0L;
            try {
                fis = in.getInputStream();
                fos = tmp.getOutputStream();
                if (processChecksum) {
                    _log.debug("Processing {} hash on input (original)", cheksumAlgorithm);
                    try {
                        checksumOriginal = Checksum.getChecksum(_algorithm, fis);
                        fis = checksumOriginal.getInputStream();
                    } catch (final Throwable t) {
                        _log.warn("Cannot init {}", cheksumAlgorithm, t);
                    }
                    _log.debug("Processing {} hash on output (filtered)", cheksumAlgorithm);
                    try {
                        checksumFiltered = Checksum.getChecksum(_algorithm, fos);
                        fos = checksumFiltered.getOutputStream();
                    } catch (final Throwable t) {
                        _log.warn("Cannot init {}", cheksumAlgorithm, t);
                    }
                }
                fos = StreamManagerImp.getFilters(fos, filter, 0);
                size = StreamPlugThread.copy(fos, fis, StreamPlugThread.DEFAULT_BUFF_SIZE);
                fos.close();
                fis.close();
                // Check the integrity of the file
                if (fileSize != size) {
                    // Not the expected size!
                    throw new IOException("Size error (" + fileSize + " != " + size + ")");
                }
                _log.debug("Size is valid");
                final String valueOriginal;
                if (checksumOriginal != null) {
                    valueOriginal = checksumOriginal.getValue();
                    _log.debug("{}: {} -> {} (original)", cheksumAlgorithm, in.getAbsolutePath(), valueOriginal);
                    if (checksum != null) {
                        // There was already a checksum so let's check if it is
                        // the same (in the form 'checksum' or 'checksum1/filter=checksum2')?
                        if (!checksum.equals(valueOriginal) && !checksum.startsWith(valueOriginal + "/")) {
                            // Not the expected checksum!
                            throw new IOException("Checksum error (" + checksum + " != " + valueOriginal + ")");
                        }
                        _log.debug("Checksum valid");
                    } else {
                        // There was not checksum yet so we want to save it!
                        _log.debug("No initial checksum found");
                        dataFile.setChecksum(valueOriginal);
                    }
                } else {
                    valueOriginal = "";
                }
                if (checksumFiltered != null) {
                    final var valueFiltered = checksumFiltered.getValue();
                    _log.debug("{}: {} -> {} (filtered)", cheksumAlgorithm, outFileName, valueFiltered);
                    dataFile.setChecksum(valueOriginal + "/" + filter + "=" + valueFiltered);
                }
                success = true;
            } catch (final IOException e) {
                _log.warn("Filtering DataFile {}", dataFile.getId(), e);
                throw e;
            } finally {
                if (!success || !tmp.renameTo(outFileName)) {
                    if (!tmp.delete()) {
                        _log.warn("DataFile {} temporary filtered file NOT deleted: {}", dataFile.getId(), tmpFileName);
                    }
                    if (success) {
                        throw new IOException("Couldn't rename temporary file");
                    }
                    StreamPlugThread.closeQuietly(fis);
                    StreamPlugThread.closeQuietly(fos);
                } else {
                    _log.info("DataFile {} filtered: {}{}", dataFile.getId(), outFileName,
                            remove ? " (to be removed)" : "");
                    dataFile.setFilterSize(out.length());
                    if (remove && !out.delete()) {
                        _log.warn("DataFile {} filtered file NOT deleted as requested: {}", dataFile.getId(),
                                outFileName);
                    }
                }
            }
            _log.debug("Filtering completed successfully ({} byte(s))", size);
            return dataFile;
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Replicate.
     */
    @Override
    public DataFile replicate(final DataFile dataFile, final Host targetHost, final Host[] hostsForSource)
            throws ECtransException, SourceNotAvailableException, IOException {
        final var operation = HostOption.BACKUP.equals(targetHost.getType()) ? "backup"
                : HostOption.PROXY.equals(targetHost.getType()) ? "proxy" : "replicate";
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(dataFile, operation));
        try {
            final var fileName = getPath(dataFile);
            final GenericFile file = new FileChecker(GenericFile.getGenericFile(getRepository(), fileName));
            final var fileSize = dataFile.getSize();
            _log.info("File {} to {} from here to {}", fileName, operation, targetHost.getNickname());
            // Let's check if the original DataFile (e.g. uncompressed) is on the current
            // DataMover? If not we retrieve it from the source if possible!
            if (!file.canRead() || file.length() != fileSize) {
                _log.warn("File to {} not on DataMover: {}", operation, file.getAbsolutePath());
                final var ectransGet = new ECtransInputStream(hostsForSource, dataFile, 0);
                InputStream get = ectransGet;
                try {
                    if (del(dataFile)) {
                        _log.debug("Corrupted file(s) deleted for DataFile {}", dataFile.getId());
                    }
                } catch (final Throwable t) {
                    _log.warn("Couldn't delete DataFile {}", dataFile.getId(), t);
                }
                if (Cnf.at("RetrievalInputStream", "buffered", false)) {
                    _log.debug("Using BufferedInputStream for donwload");
                    get = new BufferedInputStream(get);
                }
                if (Cnf.at("RetrievalInputStream", "interruptible", false)) {
                    _log.debug("Using InterruptibleInputStream for donwload");
                    get = new InterruptibleInputStream(get);
                }
                file.receiveFile(get, fileSize);
                dataFile.setDownloaded(true);
                if (targetHost.getName().equals(ectransGet.getHost().getName())) {
                    _log.info("No file to {} (already on target)", operation);
                    return dataFile;
                }
            }
            final boolean toFilter;
            if (HostOption.PROXY.equals(targetHost.getType())) {
                // Do we have to compress to the proxy?
                final var setup = HOST_ECTRANS.getECtransSetup(targetHost.getData());
                final String filter;
                // We check the filterpattern against the original filename, this is different
                // from the dissemination host where we check against the target name. However
                // we can only check with the original file name as we don't know about the
                // target here (we might have multiple data transfers for this data file with
                // different target names)!
                if (setup.matches(HOST_ECTRANS_FILTERPATTERN, new File(dataFile.getOriginal()).getName())
                        && fileSize >= setup.getByteSize(HOST_ECTRANS_FILTER_MINIMUM_SIZE).size()) {
                    filter = StreamManagerImp.getFilters(targetHost.getFilterName(), fileSize);
                } else {
                    filter = StreamManager.NONE;
                }
                if (toFilter = StreamManagerImp.isFiltered(filter)) {
                    _log.warn("DataFile {}: force {} (will filter while transfering)", dataFile.getId(), filter);
                    setup.set(HOST_ECTRANS_INITIAL_INPUT_FILTER, filter);
                    setup.set(HOST_ECTRANS_INITIAL_INPUT_SIZE, fileSize);
                    targetHost.setData(setup.getData());
                } else {
                    _log.debug("DataFile {}: no filtering required", dataFile.getId());
                }
            } else {
                // We only compress when we send to a proxy!
                toFilter = false;
                // Is it on the target host already or not?
                try {
                    if (size(targetHost, fileName) == fileSize) {
                        return dataFile;
                    }
                } catch (final IOException e) {
                }
            }
            // Now we process the transmission synchronously!
            final var descriptor = new FileDescriptor(null, dataFile, file, fileSize, 0, -1, null, null);
            final var put = new ECtransPut(fileName, descriptor, 0, fileSize, toFilter, null);
            new ECtransContainer(new MoverProvider(new LocalRepository(targetHost)), false).syncExec(put, null,
                    targetHost.getECUserName(), targetHost.getName() + "@" + targetHost.getTransferMethodName(), null,
                    new DefaultCallback(HOST_ECTRANS.getECtransSetup(targetHost.getData())), true);
            _log.info("File {} successfully transmitted on {}", fileName, targetHost.getNickname());
            return dataFile;
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the mover report.
     */
    @Override
    public String getMoverReport(final Host proxyHost) throws IOException {
        final var setup = HOST_PROXY.getECtransSetup(proxyHost.getData());
        final var url = setup.getString(HOST_PROXY_HTTP_PROXY_URL);
        final var mover = setup.get(HOST_PROXY_HTTP_MOVER_URL, "https://" + proxyHost.getHost());
        try {
            final var rest = getRESTInterface(url, mover, (int) setup.getDuration(HOST_PROXY_TIMEOUT).toMillis());
            return rest.getMoverReport();
        } catch (final Throwable t) {
            throw new IOException("Error occurred on remote proxy (proxy=" + url + ",mover=" + mover + ")", t);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host report.
     */
    @Override
    public String getHostReport(final Host proxyHost, final Host host) throws IOException {
        final var setup = HOST_PROXY.getECtransSetup(proxyHost.getData());
        final var url = setup.getString(HOST_PROXY_HTTP_PROXY_URL);
        final var mover = setup.get(HOST_PROXY_HTTP_MOVER_URL, "https://" + proxyHost.getHost());
        try {
            final var rest = getRESTInterface(url, mover, (int) setup.getDuration(HOST_PROXY_TIMEOUT).toMillis());
            return rest.getHostReport(host);
        } catch (final Throwable t) {
            throw new IOException("Error occurred on remote proxy (proxy=" + url + ",mover=" + mover + ")", t);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the report.
     */
    @Override
    public String getReport() throws IOException {
        return _exec(Cnf.notEmptyStringAt("ReportCommand", "mover", "mover-report"));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the report.
     */
    @Override
    public String getReport(final Host host) throws IOException {
        // Load the TransferModule to get the port number!
        final int port;
        final String listenAddress;
        try {
            final var ectransModule = host.getTransferMethod().getECtransModule();
            final var provider = new MoverProvider(new ECproxyRepository(host));
            final var transferModule = provider.loadTransferModule(ectransModule);
            final var setup = new ECtransSetup(ectransModule.getName(), host.getData());
            port = transferModule.getPort(setup);
            listenAddress = setup.get("listenAddress", null);
        } catch (final Throwable t) {
            throw new IOException("Could not load the " + host.getTransferMethod().getECtransModuleName() + " module",
                    t);
        }
        // Find the command to process the Host report!
        final var c = Cnf.notEmptyStringAt("ReportCommand", "host", "host-report");
        // Let's fill the parameters with the proper values from the Host
        // object!
        final var sb = new StringBuilder(c);
        Format.replaceAll(sb, "$name", host.getName());
        Format.replaceAll(sb, "$method", host.getTransferMethodName());
        Format.replaceAll(sb, "$host", host.getHost());
        Format.replaceAll(sb, "$port", port);
        Format.replaceAll(sb, "$login", host.getLogin());
        Format.replaceAll(sb, "$passwd", host.getPasswd());
        Format.replaceAll(sb, "$mail", host.getUserMail());
        Format.replaceAll(sb, "$networkCode", host.getNetworkCode());
        Format.replaceAll(sb, "$networkName", host.getNetworkName());
        Format.replaceAll(sb, "$nickname", host.getNickname());
        Format.replaceAll(sb, "$listenAddress", listenAddress == null ? "" : listenAddress);
        // Run the report!
        return _exec(sb.toString());
    }

    /**
     * Run a command on the server and return the output (e.g. process a report for a Host from the TransferServer).
     *
     * @param c
     *            the c
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String _exec(final String c) throws IOException {
        // Find the command to process the report!
        if (c == null || !new File(c.split(" ")[0]).canExecute()) {
            throw new IOException("Command " + c + " not available");
        }
        final var result = new StringBuilder();
        _log.debug("Starting command: {}", c);
        // The process which starts the command!
        Process process = null;
        BufferedReader stdIn = null;
        BufferedReader stdErr = null;
        final int returnCode;
        try {
            process = Runtime.getRuntime().exec(c);
            stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            // Use arrays to hold references to stdIn and stdErr
            final var stdInHolder = new BufferedReader[] { stdIn };
            final var stdErrHolder = new BufferedReader[] { stdErr };
            // Thread to read stdout
            final var outputThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stdInHolder[0].readLine()) != null) {
                        synchronized (result) {
                            result.append("<font color='green'>").append(line).append("</font>").append("\r\n");
                        }
                    }
                } catch (final IOException e) {
                    _log.error("Error reading stdout", e);
                }
            });
            // Thread to read stderr
            final var errorThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stdErrHolder[0].readLine()) != null) {
                        synchronized (result) {
                            result.append("<font color='red'>").append(line).append("</font>").append("\r\n");
                        }
                    }
                } catch (final IOException e) {
                    _log.error("Error reading stderr", e);
                }
            });
            outputThread.start();
            errorThread.start();
            // Wait for both threads to finish
            outputThread.join();
            errorThread.join();
            returnCode = process.waitFor();
        } catch (final InterruptedException e) {
            _log.debug("Interrupted while waiting for process completion");
            throw new IOException("Interrupted while waiting");
        } finally {
            StreamPlugThread.closeQuietly(stdIn);
            StreamPlugThread.closeQuietly(stdErr);
            StreamPlugThread.closeQuietly(process);
        }
        _log.debug("Completed with exit code: {}", returnCode);
        // Let's check the return code from the execution!
        if (returnCode != 0) {
            // We are expecting 0 so there was an error while executing the
            // command
            throw new IOException("Unexpected return: " + returnCode);
        }
        // We can now return the result
        return result.toString();
    }

    /**
     * {@inheritDoc}
     *
     * Download.
     */
    @Override
    public DataFile download(final DataFile dataFile, final Host hostForSource)
            throws SourceNotAvailableException, IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(dataFile, "download"));
        try {
            var fileSize = dataFile.getSize();
            final var fileName = getPath(dataFile);
            final var file = GenericFile.getGenericFile(getRepository(), fileName);
            if (!OPERATIONAL) {
                // This is NOT an operational mover. First check if we should
                // not simulate an error!
                if (errorsFrequency > 0) {
                    synchronized (errorsSync) {
                        if (errors++ > errorsFrequency) {
                            errors = 0;
                            throw new IOException("Simulated error on download");
                        }
                    }
                }
                // Let's wait to simulate a transfer rate and then return the
                // DataFile to the MasterServer as if the download was
                // successful!
                final var start = System.currentTimeMillis();
                if (fileSize == -1) {
                    // This is an Acquisition file and it was a symbolic link so
                    // let's set a random file size between 10B and 10MB!
                    dataFile.setSize(fileSize = 10 * (ThreadLocalRandom.current().nextInt(1024 * 1024) + 1));
                }
                final var delay = Cnf.at("TestModule", "initialDownloadDelay", 15L)
                        + fileSize / Cnf.at("TestModule", "downloadedBytesPerSecond", 1024L * 1024L);
                if (_log.isWarnEnabled()) {
                    _log.warn("Simulating file download: {} (waiting for {} - {})", file.getAbsolutePath(),
                            Format.formatDuration(delay), Format.formatSize(fileSize));
                }
                try {
                    Thread.sleep(delay);
                } catch (final InterruptedException e) {
                }
                final var stop = System.currentTimeMillis();
                dataFile.setRemoteHost(getRoot()); // Generated from here!
                dataFile.setGetHost(getRoot());
                dataFile.setGetTime(new Timestamp(stop));
                dataFile.setGetDuration(stop - start);
                dataFile.setDownloaded(true);
                return dataFile;
            }
            // This is an operational system, let's process the download of the
            // DataFile!
            _log.debug("File to download: {}", file.getAbsolutePath());
            final var ectransIn = new ECtransInputStream(new Host[] { hostForSource }, dataFile, 0);
            InputStream get = ectransIn; // We need to have a standard input stream for buffering and more!
            try {
                if (del(dataFile)) {
                    _log.debug("Existing file(s) deleted for DataFile {}", dataFile.getId());
                }
            } catch (final Throwable t) {
                _log.warn("Couldn't delete existing DataFile {}", dataFile.getId(), t);
            }
            final var start = System.currentTimeMillis();
            if (Cnf.at("RetrievalInputStream", "buffered", false)) {
                _log.debug("Using BufferedInputStream for donwload");
                get = new BufferedInputStream(get);
            }
            if (Cnf.at("RetrievalInputStream", "interruptible", false)) {
                _log.debug("Using InterruptibleInputStream for donwload");
                get = new InterruptibleInputStream(get);
            }
            Checksum checksum = null;
            final var cheksumAlgorithm = _algorithm.getName();
            if (!"none".equalsIgnoreCase(dataFile.getChecksum())) {
                _log.debug("Processing {} hash", cheksumAlgorithm);
                try {
                    checksum = Checksum.getChecksum(_algorithm, get);
                    get = checksum.getInputStream();
                } catch (final Throwable t) {
                    _log.warn("Cannot init {}", cheksumAlgorithm, t);
                }
            }
            final var completed = new StringBuilder("0");
            final long delta;
            if ((delta = Cnf.durationAt("RetrievalInputStream", "monitored", -1)) > 0) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Using MonitoredInputStream for donwload (delta={})", Format.formatDuration(delta));
                }
                get = new MonitoredInputStream(get, delta, new ProgressHandler() {
                    @Override
                    public long getDelay() {
                        return 2 * Timer.ONE_SECOND;
                    }

                    @Override
                    public void update(final ProgressInterface monitor) {
                        final var dataFileId = dataFile.getId();
                        // If the current thread is completed then we don't need
                        // to store the download progress in the repository!
                        if (completed.charAt(0) == '1') {
                            _log.debug("Retrieval for DataFile-{} completed", dataFileId);
                            downloadRepository.removeKey(dataFile);
                            return;
                        }
                        // It is still running!
                        final var progress = downloadRepository.getValue(dataFileId);
                        if (progress != null) {
                            progress.setDuration(monitor.getDuration());
                            progress.setByteSent(monitor.getByteSent());
                        } else {
                            // We need to record it!
                            downloadRepository.put(new DownloadProgress(getRoot(), dataFile.getId(), dataFile.getSize(),
                                    monitor.getDuration(), monitor.getByteSent(), () -> {
                                        // If the receiving is completed
                                        // then no need to interrupt! This
                                        // method is called when the
                                        // Download repository is cleared
                                        // (e.g. when the Data Mover
                                        // disconnect from the Master).
                                        if (completed.charAt(0) == '0') {
                                            _log.debug("Still receiving data (interrupt)");
                                            monitor.closeAndInterruptIfRequired();
                                        } else {
                                            _log.debug("No interrupt required");
                                        }
                                    }));
                        }
                    }
                });
            }
            try {
                file.receiveFile(get, fileSize);
                completed.setCharAt(0, '1');
            } finally {
                // Let's make sure the download is removed from the download
                // repository and the monitored stream is closed properly!
                StreamPlugThread.closeQuietly(get);
                downloadRepository.removeKey(dataFile);
            }
            // The master is still waiting!
            final var stop = System.currentTimeMillis();
            // TODO: could remove the file on source if
            // dataFile.getDeleteOriginal()
            if (fileSize == -1) {
                // This is an Acquisition file and it was a symbolic link so
                // let's find out the real size of the file!
                dataFile.setSize(file.length());
            }
            dataFile.setRemoteHost(ectransIn.getRemoteHostName());
            dataFile.setGetHost(getRoot());
            dataFile.setGetTime(new Timestamp(stop));
            dataFile.setGetDuration(stop - start);
            dataFile.setDownloaded(true);
            dataFile.setChecksum(null);
            if (checksum != null) {
                // A Checksum was requested!
                final var value = checksum.getValue();
                dataFile.setChecksum(value);
                _log.debug("{}: {} -> {}", cheksumAlgorithm, fileName, value);
            }
            _log.debug("Download completed successfully");
            return dataFile;
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Purge.
     */
    @Override
    public void purge(final Host proxyHost, final List<ExistingStorageDirectory> directories) throws RemoteException {
        _log.info("Request to purge on ProxyHost-{} (found {} existing directories)", proxyHost.getName(),
                directories.size());
        final var setup = HOST_PROXY.getECtransSetup(proxyHost.getData());
        final var url = setup.getString(HOST_PROXY_HTTP_PROXY_URL);
        final var mover = setup.get(HOST_PROXY_HTTP_MOVER_URL, "https://" + proxyHost.getHost());
        try {
            final var rest = getRESTInterface(url, mover, (int) setup.getDuration(HOST_PROXY_TIMEOUT).toMillis());
            rest.purge(directories);
        } catch (final Throwable t) {
            _log.warn("Error occurred on remote proxy (proxy={},mover={})", url, mover, t);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Purge.
     */
    @Override
    public void purge(final List<ExistingStorageDirectory> directories) throws RemoteException {
        _log.info("Request to purge (found {} existing directories)", directories.size());
        synchronized (this) {
            if (purgeThread != null && purgeThread.isAlive()) {
                // Make sure we don't have 2 purge running at the same time!
                _log.debug("Previous purge still alive (kill it)");
                purgeThread.stop();
            }
            purgeThread = new PurgeThread(directories);
            purgeThread.execute();
        }
    }

    /**
     * Request from the JMX interface.
     */
    public void purge() {
        _log.info("Request to purge all directories");
        synchronized (this) {
            if (purgeThread != null && purgeThread.isAlive()) {
                // Make sure we don't have 2 purge running at the same time!
                _log.debug("Previous purge still alive (kill it)");
                purgeThread.stop();
            }
            purgeThread = new PurgeThread();
            purgeThread.execute();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public boolean del(final Host proxyHost, final DataFile dataFile) throws ECtransException, IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(dataFile, "del"));
        try {
            final var setup = HOST_PROXY.getECtransSetup(proxyHost.getData());
            final var url = setup.getString(HOST_PROXY_HTTP_PROXY_URL);
            final var mover = setup.get(HOST_PROXY_HTTP_MOVER_URL, "https://" + proxyHost.getHost());
            try {
                final var rest = getRESTInterface(url, mover, (int) setup.getDuration(HOST_PROXY_TIMEOUT).toMillis());
                rest.del(dataFile);
                return true;
            } catch (final Throwable t) {
                _log.warn("Error occurred on remote proxy (proxy={},mover={})", url, mover, t);
            }
            return false;
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public boolean del(final DataFile dataFile) throws ECtransException, IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(dataFile, "del"));
        try {
            GenericFile file = new FileChecker(GenericFile.getGenericFile(getRepository(), getPath(dataFile)));
            final var doNotExists = !file.exists();
            final var deleted = doNotExists || file.delete();
            final var instance = dataFile.getFileInstance();
            if (deleted) {
                _log.info("DataFile {}{}{}{}: {}", dataFile.getId(),
                        instance != null ? " (instance " + instance.intValue() + ")" : "",
                        doNotExists ? " do not exists" : " deleted",
                        dataFile.getArrivedTime() != null
                                ? " (" + Format.formatTime(dataFile.getArrivedTime().getTime()) + ")" : "",
                        file.getAbsolutePath());
            } else {
                _log.warn("DataFile {}{} NOT deleted ({}): {}", dataFile.getId(),
                        instance != null ? " (instance " + instance.intValue() + ")" : "",
                        file.exists() ? "exist" : "not found", file.getAbsolutePath());
            }
            final var dir = file.getParentFile();
            if (dir != null) {
                final var count = instance == null ? 0 : instance;
                for (var j = 0; j <= count; j++) {
                    final GenericFile currentFile = new FileChecker(
                            GenericFile.getGenericFile(getRepository(), getPath(dataFile, j == 0 ? null : j)));
                    final var ls = dir.list(new DataFileFilter(currentFile));
                    if (ls != null) {
                        for (final String fileName : ls) {
                            final GenericFile filter = new FileChecker(GenericFile.getGenericFile(dir, fileName));
                            if (filter.delete()) {
                                _log.info("File related to DataFile {} deleted: {}", dataFile.getId(),
                                        filter.getAbsolutePath());
                            } else {
                                _log.warn("File related to DataFile {} NOT deleted ({}): {}", dataFile.getId(),
                                        filter.exists() ? "exist" : "not found", filter.getAbsolutePath());
                            }
                        }
                    }
                }
            }
            var checkDirectory = true;
            for (var i = 0; i < 2 && checkDirectory && (file = file.getParentFile()) != null; i++) {
                final var list = file.list();
                if (list != null && list.length == 0 && (checkDirectory = file.delete())) {
                    _log.info("Directory deleted: {}", file.getAbsolutePath());
                }
            }
            return !OPERATIONAL || deleted;
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * Sets the ectrans setup.
     *
     * @param callBack
     *            the call back
     * @param dataFile
     *            the data file
     * @param filter
     *            the filter
     * @param size
     *            the size
     */
    private static void _setECtransSetup(final MoverCallback callBack, final DataFile dataFile, final String filter,
            final long size) {
        final var setup = callBack.getECtransSetup();
        setup.set(HOST_ECTRANS_INITIAL_INPUT_FILTER, filter);
        setup.set(HOST_ECTRANS_INITIAL_INPUT_SIZE, size);
        callBack.setFileSize(filter, size);
        // TODO: If the md5 sum is available then we should set it here:
        // -----------------------------------------------
        // final String md5 = dataFile.getChecksum();
        // if (md5 != null)
        // setup.set(COMMON_ECTRANS_INITIAL_INPUT_MD5, md5);
        // -----------------------------------------------
        // However we cannot use the dataFile checksum as it might not be md5!
    }

    /**
     * {@inheritDoc}
     *
     * Puts the.
     */
    @Override
    public DataTransfer put(final Host[] hostsForSource, final DataTransfer transfer, final String targetName,
            final long localPosn, final long remotePosn)
            throws ECtransException, SourceNotAvailableException, IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(transfer, "put"));
        try {
            _checkInProgress(transfer);
            final var proxyHost = transfer.getProxyHost();
            if (masterManager != null && proxyHost != null) {
                if (proxyHost.getActive()) {
                    // The DataTransfer was transmitted to a ProxyHost so we
                    // have to check if we cannot send the DataTransfer from
                    // this ProxyHost instead? We know we are not on a ProxyHost
                    // already because we have an RMI interface defined to the
                    // MasterServer!
                    final var setup = HOST_PROXY.getECtransSetup(proxyHost.getData());
                    final var url = setup.getString(HOST_PROXY_HTTP_PROXY_URL);
                    final var mover = setup.get(HOST_PROXY_HTTP_MOVER_URL, "https://" + proxyHost.getHost());
                    final int modulo = setup.getInteger(HOST_PROXY_MODULO);
                    // Did we have too many retries on the Proxy? If not then
                    // let's send the request, otherwise we try to transmit the
                    // file from the current DataMover!
                    if (transfer.getStartCount() % modulo > 0) {
                        // We are not on the ProxyHost yet so we can call the
                        // REST interface to it!
                        _log.debug("Trying on remote proxy (proxy={},mover={})", url, mover);
                        try {
                            final var rest = getRESTInterface(url, mover,
                                    (int) setup.getDuration("timeout", Duration.ofSeconds(10)).toMillis());
                            final var moverName = rest.put(transfer, targetName, localPosn, remotePosn);
                            // This is to inform the MasterServer that the
                            // DataTransfer has been launched on a ProxyHost!
                            transfer.setMoverName(moverName);
                            return transfer;
                        } catch (final Throwable t) {
                            // Error occurred on remote proxy!
                            _log.warn(
                                    "Error occurred on remote proxy (proxy={},mover={}) - will use current DataMover instead",
                                    url, mover, t);
                        }
                    } else {
                        // Too many retries on remote proxy!
                        _log.warn(
                                "Too many retries on remote proxy (proxy={},mover={}) - will use current DataMover instead",
                                url, mover);
                    }
                } else {
                    // Remote proxy not activated!
                    _log.warn("Remote proxy not active - will use current DataMover instead");
                }
            } else {
                _log.debug("Use current DataMover");
            }
            final var host = transfer.getHost();
            final var dataFile = transfer.getDataFile();
            if (!dataFile.getDownloaded()) {
                _log.warn("DataFile not yet imported: will use source hosts");
            }
            GenericFile file = new FileChecker(GenericFile.getGenericFile(getRepository(), getPath(dataFile)));
            var size = dataFile.getSize();
            final var callBack = new MoverCallback(transfer, targetName);
            final var setup = HOST_ECTRANS.getECtransSetup(host.getData());
            // Do we have to compress the file?
            final String filter;
            if (setup.matches(HOST_ECTRANS_FILTERPATTERN, targetName)
                    && size >= setup.getByteSize(HOST_ECTRANS_FILTER_MINIMUM_SIZE).size()) {
                filter = StreamManagerImp.getFilters(host.getFilterName(), size);
            } else {
                filter = StreamManager.NONE;
            }
            var toFilter = StreamManagerImp.isFiltered(filter);
            var useFilteredFile = false;
            if (toFilter) {
                // We have to compress, but maybe a compressed version of the file is available?
                final var tmp1 = _getFilterGenericFile(file, StreamManagerImp.getExtension(filter).substring(1));
                final var tmp = tmp1.exists() ? tmp1 : _getFilterGenericFile(file, filter);
                if (tmp.canRead()) {
                    // The compressed file exists!
                    toFilter = false;
                    if (setup.getBoolean(HOST_ECTRANS_CHECKFILTERSIZE) && tmp.length() >= size) {
                        _log.info("DataFile {} SMALLER than compressed file: use original file", dataFile.getId());
                    } else {
                        _log.info("DataFile {}: use {} file", dataFile.getId(), filter);
                        _setECtransSetup(callBack, dataFile, filter, size = tmp.length());
                        useFilteredFile = true;
                        file = tmp;
                    }
                } else {
                    // We will have to compress on the fly!
                    _log.warn("DataFile {}: {} file not found/readable (will filter while transfering) - {}",
                            dataFile.getId(), filter, tmp.getAbsolutePath());
                    _setECtransSetup(callBack, dataFile, filter, size);
                }
            }
            var inputFilter = StreamManager.NONE;
            if (!useFilteredFile && !file.exists()) {
                // The original Data File is not there! e.g. we could be on a remote data mover
                // and the file might only exists in one of its compressed form?
                var found = false;
                for (final String mode : StreamManager.modes) {
                    if (found) {
                        break;
                    }
                    if (!StreamManager.NONE.equals(mode)) {
                        // The file could have the normal or full extension (e.g. bz2 or lbzip2)?
                        for (final GenericFile tmp : Arrays.asList(
                                _getFilterGenericFile(file, StreamManagerImp.getExtension(mode).substring(1)),
                                _getFilterGenericFile(file, mode))) {
                            if (tmp.exists()) { // We found one source!
                                _log.info("DataFile {}: use {} file", dataFile.getId(), mode);
                                if (filter.equals(mode)) {
                                    // We found the file with the requested compression!
                                    _setECtransSetup(callBack, dataFile, filter, size = tmp.length());
                                    useFilteredFile = true;
                                    toFilter = false;
                                } else {
                                    // This is different from the requested compression, so we will have to
                                    // uncompress on the fly!
                                    _log.debug("Different from requested compression ({})", filter);
                                    inputFilter = mode;
                                }
                                file = tmp;
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            // Now let's process the transmission!
            transferRepository.put(transfer);
            var successful = false;
            try {
                final var descriptor = new FileDescriptor(hostsForSource, dataFile, file, size, localPosn, -1,
                        useFilteredFile ? filter : null, inputFilter);
                callBack.setFileDescriptor(descriptor);
                final var connectOptions = transfer.getConnectOptions();
                _log.debug("ConnectOptions for {}: {}", targetName, connectOptions);
                // If connectOptions is empty let's add "ectransAction=put" to allow the
                // transfer module to set the socket statistics if requested
                ectrans.asyncExec(new ECtransPut(targetName, descriptor, remotePosn, size, toFilter, callBack),
                        _getCookie(transfer, null), host.getECUserName(),
                        host.getName() + "@" + host.getTransferMethodName(), null,
                        new Options(connectOptions != null ? connectOptions : "ectransAction=put", ";\n"), callBack);
                transfer.setMoverName(getRoot());
                transfer.setComment("Trying from " + (masterManager != null ? "DataMover=" + getRoot() : "Proxy Host="
                        + transfer.getProxyHostName() + " (" + transfer.getProxyHost().getNickname() + ")"));
                successful = true;
                return transfer;
            } finally {
                if (!successful) {
                    _log.warn("Transfer {} not submitted (removed from repository)", transfer.getId());
                    transferRepository.removeValue(transfer);
                }
            }
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Allow getting a ProxySocket to upload a file on a target host. This method should be used only when the file is
     * to be uploaded from a different data mover than the current one.
     */
    @Override
    public ProxySocket put(final Host host, final String target, final long remotePosn, final long size)
            throws ECtransException, IOException {
        final var repository = new ECproxyRepository(host);
        final var ticket = getTicketRepository().add(new ECaccessTicket(repository, ECaccessTicket.INPUT));
        new ECtransContainer(new MoverProvider(repository), false).asyncExec(
                new ECtransPut(target, ticket, remotePosn, size), null, host.getECUserName(),
                host.getName() + "@" + host.getTransferMethodName(), null, new ECproxyCallback(ticket, host.getData()));
        final var socketConfig = new SocketConfig("ECproxyPlugin");
        return new ProxySocket(ticket.getId(), socketConfig.getPublicAddress(), socketConfig.getPort(), true);
    }

    /**
     * {@inheritDoc}
     *
     * Allow getting a ProxySocket to retrieve a file on a target host. This method should be used only when the file is
     * to be retrieved from a different data mover than the current one.
     */
    @Override
    public ProxySocket get(final Host host, final String source, final long remotePosn, final boolean removeOriginal)
            throws ECtransException, IOException {
        final var repository = new ECproxyRepository(host);
        final var ticket = getTicketRepository().add(new ECaccessTicket(repository, ECaccessTicket.OUTPUT));
        new ECtransContainer(new MoverProvider(repository), false).asyncExec(
                new ECtransGet(source, ticket, remotePosn, removeOriginal), null, host.getECUserName(),
                host.getName() + "@" + host.getTransferMethodName(), null, new ECproxyCallback(ticket, host.getData()));
        final var socketConfig = new SocketConfig("ECproxyPlugin");
        return new ProxySocket(ticket.getId(), socketConfig.getPublicAddress(), socketConfig.getPort(), true);
    }

    /**
     * Allow providing an output stream to retrieve a file. This method should only be used when it is called from the
     * current data mover. The retrieval is done asynchronously. If the retrieval fail then the provided Output Stream
     * is closed.
     *
     * @param out
     *            the output stream
     * @param host
     *            the host
     * @param source
     *            the source
     * @param remotePosn
     *            the remote posn
     * @param dataFile
     *            the data file
     *
     * @return the ecproxy callback for checking the outcome of the transmission
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public ECproxyCallback get(final OutputStream out, final Host host, final String source, final long remotePosn,
            final DataFile dataFile) throws ECtransException {
        final boolean deleteOriginal;
        if (dataFile != null) {
            deleteOriginal = dataFile.getDeleteOriginal();
        } else {
            deleteOriginal = false;
        }
        final var ecproxyCallback = new ECproxyCallback(new ECaccessTicket(ECaccessTicket.OUTPUT), host.getData(), out);
        new ECtransContainer(new MoverProvider(new StreamRepository(host, out)), false).asyncExec(
                new ECtransGet(source, null, remotePosn, deleteOriginal), null, host.getECUserName(),
                host.getName() + "@" + host.getTransferMethodName(), null, null, ecproxyCallback);
        return ecproxyCallback;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public ProxySocket get(final DataFile dataFile, final Host hostForSource, final long remotePosn)
            throws SourceNotAvailableException, IOException {
        return get(dataFile, new Host[] { hostForSource }, remotePosn, -1);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public ProxySocket get(final DataFile dataFile, final Host[] hostsForSource, final long remotePosn,
            final long length) throws SourceNotAvailableException, IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(dataFile, "get"));
        try {
            final GenericFile file = new FileChecker(GenericFile.getGenericFile(getRepository(), getPath(dataFile)));
            final var descriptor = new FileDescriptor(hostsForSource, dataFile, file, dataFile.getSize(), remotePosn,
                    length, null, null);
            final var ticket = getTicketRepository().add(new FileDescriptorTicket(descriptor));
            final var socketConfig = new SocketConfig("ECproxyPlugin");
            return new ProxySocket(ticket.getId(), socketConfig.getPublicAddress(), socketConfig.getPort(), true);
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final Host host, final String source) throws ECtransException, IOException {
        final var size = new ECtransSize(source);
        new ECtransContainer(new MoverProvider(new ECproxyRepository(host)), false).syncExec(size, null,
                host.getECUserName(), host.getName() + "@" + host.getTransferMethodName(), null,
                new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
        return size.getSize();
    }

    /**
     * {@inheritDoc}
     *
     * List as string array.
     */
    @Override
    public String[] listAsStringArray(final Host host, final String directory, final String pattern)
            throws ECtransException, IOException {
        final var list = new ECtransList(directory, pattern, true);
        new ECtransContainer(new MoverProvider(new ECproxyRepository(host)), false).syncExec(list, null,
                host.getECUserName(), host.getName() + "@" + host.getTransferMethodName(), null,
                new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
        return list.getListAsStringArray();
    }

    /**
     * {@inheritDoc}
     *
     * List as byte array.
     */
    @Override
    public RemoteInputStream listAsByteArray(final Host host, final String directory, final String pattern,
            final boolean synchronous) throws ECtransException, IOException {
        final var container = new ECtransContainer(new MoverProvider(new ECproxyRepository(host)), false);
        final InputStream in;
        if (synchronous) {
            // We are sending back the result AFTER the exec is completed. This is GZipped!
            final var list = new ECtransList(directory, pattern, false);
            container.syncExec(list, null, host.getECUserName(), host.getName() + "@" + host.getTransferMethodName(),
                    null, new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
            in = new ByteArrayInputStream(list.getListAsByteArray());
        } else {
            // We are sending back the result BEFORE the exec is completed. This is
            // not compressed!
            final var out = new PipedOutputStream();
            container.asyncExec(new ECtransList(directory, pattern, out), null, host.getECUserName(),
                    host.getName() + "@" + host.getTransferMethodName(), null,
                    new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())));
            in = new PipedInputStream(out, StreamPlugThread.DEFAULT_BUFF_SIZE);
        }
        return new RemoteInputStreamImp(in);
    }

    /**
     * {@inheritDoc}
     *
     * Execute.
     */
    @Override
    public RemoteInputStream execute(final String script) throws ScriptException, IOException {
        try {
            final var value = ScriptManager.exec(String.class, ScriptManager.JS, script);
            final var out = new ByteArrayOutputStream();
            final var gzip = new GZIPOutputStream(out, Deflater.BEST_COMPRESSION);
            gzip.write(value.getBytes());
            gzip.close();
            return new RemoteInputStreamImp(new ByteArrayInputStream(out.toByteArray()));
        } catch (final Throwable t) {
            _log.debug("Cannot execute: {}", script, t);
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final Host host, final String source) throws ECtransException, IOException {
        new ECtransContainer(new MoverProvider(new ECproxyRepository(host)), false).syncExec(new ECtransDel(source),
                null, host.getECUserName(), host.getName() + "@" + host.getTransferMethodName(), null,
                new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
    }

    /**
     * {@inheritDoc}
     *
     * Mkdir.
     */
    @Override
    public void mkdir(final Host host, final String dir) throws ECtransException, IOException {
        new ECtransContainer(new MoverProvider(new ECproxyRepository(host)), false).syncExec(new ECtransMkdir(dir),
                null, host.getECUserName(), host.getName() + "@" + host.getTransferMethodName(), null,
                new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
    }

    /**
     * {@inheritDoc}
     *
     * Rmdir.
     */
    @Override
    public void rmdir(final Host host, final String dir) throws ECtransException, IOException {
        new ECtransContainer(new MoverProvider(new ECproxyRepository(host)), false).syncExec(new ECtransRmdir(dir),
                null, host.getECUserName(), host.getName() + "@" + host.getTransferMethodName(), null,
                new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
    }

    /**
     * {@inheritDoc}
     *
     * Move.
     */
    @Override
    public void move(final Host host, final String source, final String target) throws ECtransException, IOException {
        new ECtransContainer(new MoverProvider(new ECproxyRepository(host)), false).syncExec(
                new ECtransMove(source, target), null, host.getECUserName(),
                host.getName() + "@" + host.getTransferMethodName(), null,
                new DefaultCallback(HOST_ECTRANS.getECtransSetup(host.getData())), true);
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public boolean close(final DataTransfer transfer) throws ECtransException, IOException {
        final var cookieSet = ThreadService.setCookieIfNotAlreadySet(_getCookie(transfer, "close"));
        try {
            // We first try to stop it on the current DataMover, just in case it
            // couldn't be started on the remote proxy (this is fast)!
            final var host = transfer.getHost();
            final var startCount = transfer.getStartCount();
            for (var i = startCount; i >= 1; i--) {
                final var id = _getCookie(transfer, i, null);
                if (ectrans.close(id, host.getECUserName(), host.getName() + "@" + host.getTransferMethodName(),
                        null)) {
                    _log.info("Transfer {} successfuly closed ({})", transfer.getId(), id);
                    return true;
                }
            }
            _log.debug("Transfer {} was not found on current DataMover", transfer.getId());
            // We couldn't stop it on the current DataMover so let's check if it
            // was submitted on a ProxyHost?
            final var proxyHost = transfer.getProxyHost();
            if (masterManager != null && proxyHost != null && proxyHost.getActive()) {
                // We are not on the ProxyHost yet and we have a ProxyHost
                // defined so we can call the REST interface to it!
                final var setup = HOST_PROXY.getECtransSetup(proxyHost.getData());
                final var url = setup.getString(HOST_PROXY_HTTP_PROXY_URL);
                final var mover = setup.get(HOST_PROXY_HTTP_MOVER_URL, "https://" + proxyHost.getHost());
                try {
                    final var rest = getRESTInterface(url, mover,
                            (int) setup.getDuration(HOST_PROXY_TIMEOUT).toMillis());
                    rest.close(transfer);
                    return true;
                } catch (final Throwable t) {
                    _log.warn("Error occurred on remote proxy (proxy={},mover={})", url, mover, t);
                }
            }
            _log.warn("Transfer {} not found/closed", transfer.getId());
            return false;
        } finally {
            if (cookieSet) {
                ThreadService.removeCookie();
            }
        }
    }

    /**
     * If the data mover is connected directly to its MasterServer then return a RMI interface to the Master, if the
     * data mover is connected through a delegate data mover then return a REST interface, otherwise through an
     * exception.
     *
     * @return the master proxy
     *
     * @throws ecmwf.ecpds.mover.MoverException
     *             the mover exception
     */
    public MasterProxy getMasterProxy() throws MoverException {
        synchronized (masterSynch) {
            if (masterProxy == null) {
                // First call to this method!
                if (masterManager != null) {
                    // We have a RMI interface to the MasterServer
                    _log.debug("RMI interface to the MasterServer");
                    masterProxy = new MasterRMI(this);
                } else if (restProvider != null && Cnf.has("MoverProxy")) {
                    // We have a REST interface to the MasterServer
                    _log.debug("REST interface to the MasterServer");
                    masterProxy = new MasterREST(this,
                            restProvider.getRESTInterface(Cnf.at("MoverProxy", "httpProxy", ""),
                                    Cnf.at("MoverProxy", "httpMover", ""), Cnf.at("MoverProxy", "connectTimeout", -1)));
                } else {
                    // We have no interface to the MasterServer
                    throw new MoverException("No MasterProxy available (cannot connect to MasterServer)");
                }
            }
        }
        // Return the current interface
        return masterProxy;
    }

    /**
     * Gets the master interface.
     *
     * @return the master interface
     *
     * @throws ecmwf.common.ecaccess.ConnectionException
     *             the connection exception
     */
    public MasterInterface getMasterInterface() throws ConnectionException {
        return (MasterInterface) masterManager.getConnection();
    }

    /**
     * Gets the ecauth token.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public ECauthToken getECauthToken(final String user) throws IOException {
        return tokenGenerator.getECauthToken(user);
    }

    /**
     * Gets the ticket repository.
     *
     * @return the ticket repository
     */
    public TicketRepository getTicketRepository() {
        return ticketRepository;
    }

    /**
     * Gets the download repository.
     *
     * @return the download repository
     */
    public DownloadRepository getDownloadRepository() {
        return downloadRepository;
    }

    /**
     * {@inheritDoc}
     *
     * Shutdown.
     */
    @Override
    public synchronized void shutdown() {
        if (ectrans != null) {
            try {
                ectrans.close();
            } catch (final Throwable t) {
                _log.warn("Closing ectrans container", t);
            }
        }
        if (fileCheckerMonitor != null) {
            fileCheckerMonitor.stop();
        }
        if (masterManager != null) {
            masterManager.shutdown();
        }
        if (transferRepository != null) {
            transferRepository.shutdown();
        }
        if (downloadRepository != null) {
            downloadRepository.shutdown();
        }
        if (ticketRepository != null) {
            ticketRepository.shutdown();
        }
        if (masterProxy != null) {
            masterProxy.shutdown();
        }
        super.shutdown();
    }

    /**
     * The Class MoverProvider.
     */
    private final class MoverProvider extends ECtransProvider {
        /** The _repository. */
        private final GenericRepository _repository;

        /** The _set dir. */
        private final boolean _setDir;

        /**
         * Instantiates a new mover provider.
         *
         * @param repository
         *            the repository
         */
        private MoverProvider(final GenericRepository repository) {
            this(repository, true);
        }

        /**
         * Instantiates a new mover provider.
         *
         * @param repository
         *            the repository
         * @param setDir
         *            the set dir
         */
        private MoverProvider(final GenericRepository repository, final boolean setDir) {
            _repository = repository;
            _setDir = setDir;
        }

        /**
         * Gets the root.
         *
         * @return the root
         */
        @Override
        public String getRoot() {
            return MoverServer.this.getRoot();
        }

        /**
         * Gets the allocate interface.
         *
         * @param url
         *            the url
         * @param properties
         *            the properties
         *
         * @return the allocate interface
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws MoverException
         *             the mover exception
         */
        @Override
        public AllocateInterface getAllocateInterface(final String url, final Properties properties)
                throws IOException, MoverException {
            if (restProvider == null) {
                throw new MoverException("No RESTProvider available");
            }
            return restProvider.getRESTAllocate(url, properties);
        }

        /**
         * Gets the notification interface.
         *
         * @param url
         *            the url
         * @param name
         *            the name
         * @param password
         *            the password
         *
         * @return the notification interface
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws MoverException
         *             the mover exception
         */
        @Override
        public NotificationInterface getNotificationInterface(final String url, final String name,
                final String password) throws IOException, MoverException {
            if (restProvider == null) {
                throw new MoverException("No RESTProvider available");
            }
            return restProvider.getNotificationInterface(url, name, password);
        }

        /**
         * Update MS user.
         *
         * @param msuser
         *            the msuser
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void updateMSUser(final MSUser msuser) throws IOException {
            final var host = _repository.getHost(msuser.getName());
            if (host == null) {
                // Not found in the cache?
                _log.warn("Host {} not found in cache (update canceled)", msuser.getName());
            } else {
                final var hostId = host.getName();
                if (hostId == null) {
                    // This Host was specifically tagged for not being updated!
                    _log.debug("Source Host for retrieval (no update): {}", msuser.getName());
                } else if (!hostId.equals(msuser.getName())) {
                    // The Host name does not match the MSUser name?
                    _log.warn("Host {} does not match (update canceled)", msuser.getName());
                } else {
                    // The Host should be updated!
                    _log.debug("Updating Host: {} ({})", hostId, host.getNickname());
                    host.setData(msuser.getData());
                    try {
                        getMasterProxy().updateData(host);
                    } catch (final Exception e) {
                        _log.warn("updateData", e);
                    }
                }
            }
        }

        /**
         * Gets the object.
         *
         * @param key
         *            the key
         *
         * @return the object
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public Object getObject(final Object key) throws IOException {
            final var hostName = key.toString();
            final var host = _repository.getHost(hostName);
            if (host == null) {
                throw new IOException("Host " + hostName + " not found");
            }
            return host.clone();
        }

        /**
         * Gets the MS user.
         *
         * @param ecuser
         *            the ecuser
         * @param name
         *            the name
         *
         * @return the MS user
         *
         * @throws ECtransException
         *             the ectrans exception
         */
        @Override
        public MSUser getMSUser(final String ecuser, final String name) throws ECtransException {
            final var host = _repository.getHost(name);
            if (host == null) {
                _log.debug("MSUser {} not found in the repository", name);
                return null;
            }
            final var method = host.getTransferMethod();
            final var destination = new ECtransDestination(method.getName());
            destination.setActive(method.getActive());
            destination.setComment(method.getComment());
            destination.setECtransModuleName(method.getECtransModuleName());
            destination.setECtransModule(method.getECtransModule());
            destination.setResolve(method.getResolve());
            destination.setRestrict(method.getRestrict());
            destination.setValue(method.getValue());
            var hostDir = host.getDir();
            String realDir = null;
            if (_setDir && isNotEmpty(hostDir)) {
                final var token = new StringTokenizer(hostDir, "\n");
                if (token.hasMoreTokens() && !(hostDir = token.nextToken()).trim().startsWith("(")) {
                    final var index = hostDir.indexOf("$");
                    if (index != -1) {
                        realDir = hostDir.substring(0, index);
                    } else {
                        realDir = hostDir;
                    }
                }
            }
            final var setup = HOST_ECTRANS.getECtransSetup(host.getData());
            var hostName = host.getHost();
            // Do we have a host selector to find an alternative host-name/IP?
            final var hostSelector = setup.getString(HOST_ECTRANS_HOST_SELECTOR);
            if (isNotEmpty(hostSelector)) {
                try {
                    final var sb = new StringBuilder(hostSelector);
                    Format.replaceAll(sb, "$mover", getRoot());
                    Format.replaceAll(sb, "$host", host.getHost());
                    Format.replaceAll(sb, "$network", host.getNetworkName());
                    Format.replaceAll(sb, "$group", host.getTransferGroupName());
                    hostName = Format.choose(sb.toString());
                    _log.debug("Using alternative hostname: {}", hostName);
                } catch (final DuplicatedChooseScore e) {
                    throw new ECtransException("Could not resolve host field (multiple choices selected)");
                } catch (final ScriptException e) {
                    throw new ECtransException("Could not resolve host field (" + e.getMessage() + ")");
                }
            }
            if (!setup.getBoolean(HOST_ECTRANS_USEDNSNAME) && hostName != null && hostName.indexOf("$") == -1) {
                // The host name is static (not using parameters) so we can
                // process its IP address
                final var hostLocation = host.getHostLocation();
                final var hostIp = hostLocation.getIp();
                try {
                    hostName = InetAddress.getByName(hostName).getHostAddress();
                    if (isEmpty(hostIp) || !hostIp.equals(hostName)) {
                        final var hostId = host.getName();
                        if (isNotEmpty(hostId)) {
                            // This Host should be updated on the Master!
                            _log.debug("Host-{} IP change (update): {} != {}", hostId, hostIp, hostName);
                            hostLocation.setIp(hostName);
                            try {
                                getMasterProxy().updateLocation(host);
                            } catch (final Throwable t) {
                                _log.warn("updateLocation", t);
                            }
                        }
                    }
                } catch (final UnknownHostException e) {
                    if (!isNotEmpty(hostIp)) {
                        throw new ECtransException("unknown host " + hostName);
                    }
                    _log.warn("Using cached IP address: {}", hostIp);
                    hostName = hostIp;
                }
            }
            // For the non operational system include the transfer rate for the
            // host in the ectrans data (to allow the test module to transmit at
            // the host usual rate)! For this to work, the hibernate.hbm.xml file must
            // include the hostStats in the HOST.
            if (!OPERATIONAL) {
                final var hostStats = host.getHostStats();
                if (hostStats != null && hostStats.getDuration() > 0) {
                    final var bytesPerSecond = (long) (hostStats.getSent() / ((double) hostStats.getDuration() / 1000));
                    _log.info("Setting bytesPerSecond for Host-{} to: {}", host.getName(), bytesPerSecond);
                    setup.set(HOST_TEST_BYTES_PER_SEC, ByteSize.of(bytesPerSecond));
                } else {
                    _log.debug("No transfer rate found for Host-{}", host.getName());
                }
            }
            final var msuser = new MSUser(name);
            msuser.setActive(host.getActive());
            msuser.setComment(host.getComment());
            msuser.setData(setup.getData());
            msuser.setDir(realDir);
            msuser.setECtransDestinationName(destination.getName());
            msuser.setECtransDestination(destination);
            msuser.setECUserName(host.getECUserName());
            msuser.setECUser(host.getECUser());
            msuser.setHost(hostName);
            msuser.setLogin(host.getLogin());
            msuser.setPasswd(host.getPasswd());
            return msuser;
        }

        /**
         * Gets the EC user.
         *
         * @param name
         *            the name
         *
         * @return the EC user
         */
        @Override
        public ECUser getECUser(final String name) {
            return _repository.getECUser(name);
        }

        /**
         * Gets the ectrans module.
         *
         * @param name
         *            the name
         *
         * @return the ectrans module
         */
        @Override
        public ECtransModule getECtransModule(final String name) {
            return _repository.getECtransModule(name);
        }

        /**
         * Gets the ectrans destination.
         *
         * @param name
         *            the name
         *
         * @return the ectrans destination
         */
        @Override
        public ECtransDestination getECtransDestination(final String name) {
            final var method = _repository.getTransferMethod(name);
            if (method == null) {
                return null;
            }
            final var destination = new ECtransDestination(method.getName());
            destination.setActive(method.getActive());
            destination.setComment(method.getComment());
            destination.setECtransModuleName(method.getECtransModuleName());
            destination.setECtransModule(method.getECtransModule());
            destination.setResolve(method.getResolve());
            destination.setRestrict(method.getRestrict());
            destination.setValue(method.getValue());
            return destination;
        }

        /**
         * Gets the data input stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public InputStream getDataInputStream(final Object ticket) throws IOException {
            return _repository.getDataInputStream(ticket);
        }

        /**
         * Gets the data output stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public OutputStream getDataOutputStream(final Object ticket) throws IOException {
            return _repository.getDataOutputStream(ticket);
        }

        /**
         * Gets the data input file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public File getDataInputFile(final Object ticket) throws IOException {
            return _repository.getDataInputFile(ticket);
        }

        /**
         * Gets the data output file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public File getDataOutputFile(final Object ticket) throws IOException {
            return _repository.getDataOutputFile(ticket);
        }

        /**
         * Gets the original filename.
         *
         * @param ticket
         *            the ticket
         *
         * @return the original filename
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public String getOriginalFilename(final Object ticket) throws IOException {
            return _repository.getOriginalFilename(ticket);
        }
    }

    /**
     * The Class FileDescriptor.
     */
    public final class FileDescriptor {
        /** The _hosts for source. */
        private final Host[] _hostsForSource;

        /** The _selected source host. */
        private Host _selectedSourceHost = null;

        /** The _data file. */
        private final DataFile _dataFile;

        /** The _file. */
        private final GenericFile _file;

        /** The _length. */
        private final long _size;

        /** The _posn. */
        private final long _posn;

        /** The _length. */
        private final long _length;

        /** The _filter. */
        private final String _filter;

        /** The _inputFilter. */
        private final String _inputFilter;

        /** The _local. */
        private boolean _local = true;

        /**
         * Instantiates a new file descriptor.
         *
         * @param hostsForSource
         *            the hosts for source
         * @param dataFile
         *            the data file
         * @param file
         *            the file
         * @param size
         *            the size
         * @param posn
         *            the posn
         * @param length
         *            the length
         * @param filter
         *            the filter
         * @param inputFilter
         *            the input filter (if the source file is filtered)
         *
         * @throws SourceNotAvailableException
         *             the source not available exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private FileDescriptor(final Host[] hostsForSource, final DataFile dataFile, final GenericFile file,
                final long size, final long posn, final long length, final String filter, final String inputFilter)
                throws SourceNotAvailableException, IOException {
            final var exists = file.exists();
            final var lengthOk = StreamManager.NONE.equals(inputFilter) ? exists ? file.length() == size : false : true;
            if (OPERATIONAL && (!lengthOk || !file.canRead())) {
                _log.debug("DataFile {} not found locally: {}", dataFile.getId(), file.getAbsolutePath());
                _local = false;
                if (hostsForSource == null || hostsForSource.length == 0) {
                    throw new SourceNotAvailableException("DataFile " + dataFile.getId() + " not found on " + getRoot()
                            + (exists && !lengthOk ? " (incorrect file size)" : ""));
                }
            }
            _hostsForSource = hostsForSource;
            _dataFile = dataFile;
            _file = file;
            _size = size;
            _posn = posn;
            _length = length;
            _filter = filter; // only for put and replicate (not get)
            _inputFilter = inputFilter; // only for put and replicate (not get)
        }

        /**
         * Gets the file.
         *
         * @param localOnly
         *            the local only
         *
         * @return the file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        GenericFile getFile(final boolean localOnly) throws IOException {
            if (localOnly && !_local) {
                throw new IOException("Not supported for files stored remotely");
            }
            return _file;
        }

        /**
         * Gets the input stream.
         *
         * @return the input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public InputStream getInputStream() throws IOException {
            return moverRepository.getDataInputStream(FileDescriptor.this);
        }

        /**
         * Gets the selected source host.
         *
         * @return the selected source host
         */
        Host getSelectedSourceHost() {
            return _selectedSourceHost;
        }

        /**
         * Sets the selected source host.
         *
         * @param host
         *            the new selected source host
         */
        void setSelectedSourceHost(final Host host) {
            _selectedSourceHost = host;
        }

        /**
         * Gets the hosts for source.
         *
         * @return the hosts for source
         */
        Host[] getHostsForSource() {
            return _hostsForSource;
        }

        /**
         * Gets the data file.
         *
         * @return the data file
         */
        DataFile getDataFile() {
            return _dataFile;
        }

        /**
         * Gets the posn.
         *
         * @return the posn
         */
        long getPosn() {
            return _posn;
        }

        /**
         * Gets the length.
         *
         * @return the length
         */
        long getLength() {
            return _length;
        }

        /**
         * Gets the filter.
         *
         * @return the filter
         */
        String getFilter() {
            return _filter;
        }

        /**
         * Gets the input filter.
         *
         * @return the input filter
         */
        String getInputFilter() {
            return _inputFilter;
        }

        /**
         * Gets the size.
         *
         * @return the size
         */
        long getSize() {
            return _size;
        }

        /**
         * Checks if is local.
         *
         * @return true, if is local
         */
        boolean isLocal() {
            return _local;
        }
    }

    /**
     * The Class ECproxyCallback.
     */
    public static final class ECproxyCallback extends DefaultCallback {
        /** The _ticket. */
        private final ECaccessTicket _ticket;

        /** The _closeable. */
        private final Closeable _closeable;

        /** The _remoteHostName. */
        private String _remoteHostName;

        /** The _remoteFileName. */
        private String _remoteFileName;

        /**
         * Instantiates a new ecproxy callback.
         *
         * @param ticket
         *            the ticket
         * @param data
         *            the data
         * @param closeable
         *            something to close in case of failure of the transmission
         */
        private ECproxyCallback(final ECaccessTicket ticket, final String data, final Closeable closeable) {
            super(HOST_ECTRANS.getECtransSetup(data));
            _closeable = closeable;
            _ticket = ticket;
        }

        /**
         * Instantiates a new ecproxy callback.
         *
         * @param ticket
         *            the ticket
         * @param data
         *            the data
         */
        private ECproxyCallback(final ECaccessTicket ticket, final String data) {
            this(ticket, data, null);
        }

        /**
         * Completed.
         *
         * @param module
         *            the module
         */
        @Override
        public void completed(final TransferModule module) {
            _ticket.completed();
            _remoteHostName = (String) module.getAttribute("remote.hostName");
            _remoteFileName = (String) module.getAttribute("remote.fileName");
            _log.debug("Retrieval of file {} completed from {}", _remoteFileName, _remoteHostName);
        }

        /**
         * Failed.
         *
         * @param module
         *            the module
         * @param comment
         *            the comment
         */
        @Override
        public void failed(final TransferModule module, final String comment) {
            _ticket.setError(comment);
            _ticket.completed();
            // If a close-able was provided then close it!
            StreamPlugThread.closeQuietly(_closeable);
        }

        /**
         * Check if the transmission was successful?.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public void check() throws IOException {
            final var start = System.currentTimeMillis();
            _ticket.close(30 * Timer.ONE_SECOND);
            if (_log.isDebugEnabled()) {
                _log.debug("Duration on check: {}", Format.formatDuration(start, System.currentTimeMillis()));
            }
            if (_ticket.hasError()) {
                throw new IOException(_ticket.getError());
            }
        }

        /**
         * Gets the remote host name.
         *
         * @return the remote host name
         */
        String getRemoteHostName() {
            return _remoteHostName;
        }

        /**
         * Gets the remote file name.
         *
         * @return the remote file name
         */
        String getRemoteFileName() {
            return _remoteFileName;
        }
    }

    /**
     * The Class MoverCallback.
     */
    private final class MoverCallback implements ProgressHandler, ECtransCallback {
        /** The _name. */
        private final String _name;

        /** The _file name. */
        private final String _fileName;

        /** The _transfer. */
        private final DataTransfer _transfer;

        /** The _data file. */
        private final DataFile _dataFile;

        /** The _descriptor. */
        private FileDescriptor _descriptor = null;

        /** The _monitor. */
        private ProgressInterface _monitor = null;

        /** The _update. */
        private long _update = -1;

        /** The _closed. */
        private final AtomicBoolean _closed = new AtomicBoolean(false);

        /** The _setup. */
        private final ECtransSetup _setup;

        /** The _file size. */
        private long _fileSize = -1;

        /** The _timeout. */
        private final long _timeout;

        /** The _filter. */
        private String _filter = null;

        /** The _ratio. */
        private boolean _filtered = false;

        /**
         * Instantiates a new mover callback.
         *
         * @param transfer
         *            the transfer
         * @param fileName
         *            the file name
         */
        private MoverCallback(final DataTransfer transfer, final String fileName) {
            _name = Format.formatLong(transfer.getId(), 10, true);
            _fileName = fileName;
            _transfer = transfer;
            _dataFile = transfer.getDataFile();
            _fileSize = _dataFile.getSize();
            _setup = HOST_ECTRANS.getECtransSetup(transfer.getHost().getData());
            _transfer.setFinishTime(new Timestamp(System.currentTimeMillis()));
            _transfer.setComment(null);
            _transfer.setDuration(0);
            _transfer.setSent(0);
            _transfer.setRatio(1);
            _transfer.setPutTime(null);
            _timeout = _setup.getDuration(HOST_ECTRANS_STREAM_TIMEOUT).toMillis();
        }

        /**
         * Sets the file size.
         *
         * @param filter
         *            the filter
         * @param fileSize
         *            the file size
         */
        void setFileSize(final String filter, final long fileSize) {
            _filtered = _fileSize != fileSize;
            _transfer.setRatio((double) _fileSize / (double) fileSize);
            _filter = filter;
            if (_filtered) {
                // The file has already been filtered while in the queue!
                _fileSize = fileSize;
            }
        }

        /**
         * Sets the file descriptor.
         *
         * @param descriptor
         *            the new file descriptor
         */
        void setFileDescriptor(final FileDescriptor descriptor) {
            _descriptor = descriptor;
        }

        /**
         * _update.
         */
        private void _update() {
            if (_monitor != null) {
                final var putTime = _monitor.getStartTime();
                if (_monitor instanceof final MonitoredInputStream monIn) {
                    _transfer.setDurationOnClose(monIn.getDurationOnClose());
                }
                _transfer.setDuration(_monitor.getDuration());
                _transfer.setSent(_monitor.getByteSent());
                _transfer.setPutTime(putTime > 0 ? new Timestamp(putTime) : null);
            }
        }

        /**
         * Sets the finish time.
         */
        private void _setFinishTime() {
            _transfer.setFinishTime(new Timestamp(System.currentTimeMillis() - _transfer.getFinishTime().getTime()));
        }

        /**
         * _close.
         *
         * @return true, if successful
         */
        private boolean _close() {
            if (!_closed.compareAndSet(false, true)) {
                _log.debug("Already closed");
                return false;
            }
            try {
                close(_transfer);
            } catch (final Throwable t) {
                _log.warn("Closing DataTransfer: {}", _transfer.getId(), t);
            }
            if (_monitor != null) {
                try {
                    _monitor.closeAndInterruptIfRequired();
                } finally {
                    _update();
                }
            }
            return true;
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
         * Gets the unique name.
         *
         * @return the unique name
         */
        @Override
        public String getUniqueName() {
            return _name;
        }

        /**
         * Sets the MS user.
         *
         * @param msuser
         *            the new MS user
         */
        @Override
        public void setMSUser(final MSUser msuser) {
            _transfer.setHostName(msuser.getName());
        }

        /**
         * Update.
         *
         * @param monitor
         *            the monitor
         */
        @Override
        public void update(final ProgressInterface monitor) {
            if (_monitor == null || _monitor.getByteSent() > _transfer.getSent()) {
                // First update or update in the number of bytes sent!
                _update = System.currentTimeMillis();
                _monitor = monitor;
                _update();
            } else if (!_closed.get() && _timeout > 0 && System.currentTimeMillis() - _update > _timeout) {
                failed(null, "stream timeout after " + Format.formatDuration(_timeout));
            }
        }

        /**
         * Retry.
         *
         * @param comment
         *            the comment
         */
        @Override
        public void retry(final String comment) {
            if (!_closed.get()) {
                _log.warn("Transfer {} ({}) retry{}", _transfer.getId(), _dataFile.getSource(),
                        isNotEmpty(comment) ? ": " + comment : "");
            }
        }

        /**
         * Completed.
         *
         * @param module
         *            the module
         */
        @Override
        public void completed(final TransferModule module) {
            if (!_close()) { // If it is closed then no need to process the transfer history again!
                return;
            }
            if (_transfer.getDuration() == 0) {
                // If the transfer was less than a millisecond then we set 1
                // millisecond to allow the processing of the rate!
                _log.debug("0ms duration detected for DataTransfer-{} push (forcing 1ms)", _transfer.getId());
                _transfer.setDuration(1);
            }
            final var statistics = module.getAttribute(ClientSocketStatistics.class);
            final var hostName = (String) module.getAttribute("remote.hostName");
            final var fileName = (String) module.getAttribute("remote.fileName");
            final var operation = (String) module.getAttribute("remote.operation");
            final var targetName = isNotEmpty(fileName) ? fileName : _fileName;
            final var from = masterManager != null ? "DataMover=" + getRoot() : "Proxy Host="
                    + _transfer.getProxyHostName() + " (" + _transfer.getProxyHost().getNickname() + ")";
            final String comment;
            if ("exec".equals(operation)) {
                comment = "External transfer initiated from " + from + " to " + hostName + " at " + targetName;
            } else if ("copy".equals(operation)) {
                comment = "Local copy initiated from " + from + " to " + hostName + " at " + targetName;
            } else {
                final var sourceHost = _descriptor != null ? _descriptor.getSelectedSourceHost() : null;
                final var source = sourceHost != null
                        ? " using Host=" + sourceHost.getName() + " (" + sourceHost.getNickname() + ")" : "";
                if (module.getAvailable()) {
                    // The file is available but not sent anywhere (data portal), so we have to make
                    // sure there will be no transfer rate displayed!
                    _transfer.setSent(0);
                    comment = "Available on " + from + source + " as " + targetName;
                } else { // operation == sent
                    // The file has been transmitted successfully!
                    if (module.getAttribute("compression.fileSize") instanceof final Long fileSize) {
                        // This was compression on the fly so let's compute the ratio
                        // and the number of bytes sent now that we have the data!
                        _transfer.setCompressedOnTheFly(true);
                        _transfer.setRatio((double) _fileSize / (double) fileSize);
                        _transfer.setSent(fileSize);
                        _filtered = true;
                    }
                    final var rate = Format.getMBitsPerSeconds(_transfer.getSent(), _transfer.getDuration());
                    final var ratio = (int) (100 / _transfer.getRatio());
                    final String compression;
                    if (_filter != null) {
                        final var onTheFly = _transfer.getCompressedOnTheFly() ? " on-the-fly" : "";
                        compression = " " + _filter + (_filtered ? " (" + ratio + "%" + onTheFly + ")" : "");
                    } else {
                        compression = "";
                    }
                    comment = "Sent" + compression + " from " + from + source + " to "
                            + (isNotEmpty(hostName) ? hostName + " at " : "") + targetName
                            + (rate == -1 ? "" : " (" + rate + " Mbits/s)");
                }
            }
            final var statisticsString = statistics != null && !statistics.isEmpty() ? statistics.toString() : null;
            synchronized (transferRepository) {
                _setFinishTime();
                _transfer.setComment(comment);
                _transfer.setStatistics(statisticsString);
                _transfer.setCompressed(_filter);
                _transfer.setStatusCode(StatusFactory.DONE);
                transferRepository.notifyAll();
            }
            _log.info("Transfer {} ({}) completed: {} - statistics: '{}'", _transfer.getId(), targetName, comment,
                    statisticsString != null ? statisticsString : "none");
        }

        /**
         * Failed.
         *
         * @param module
         *            the module
         * @param comment
         *            the comment
         */
        @Override
        public void failed(final TransferModule module, String comment) {
            if (!_close()) { // If it is closed then no need to process the transfer history again!
                return;
            }
            comment = isNotEmpty(comment) ? comment : null;
            var existingComment = _transfer.getComment();
            existingComment = isNotEmpty(existingComment) ? existingComment : null;
            if (comment == null && existingComment == null) {
                comment = "Transmission failed";
            } else if (comment != null && existingComment != null) {
                comment = comment + " (" + existingComment + ")";
            } else {
                if (comment == null) {
                    comment = existingComment;
                }
            }
            synchronized (transferRepository) {
                _setFinishTime();
                _transfer.setComment(comment);
                _transfer.setFailedTime(new Timestamp(System.currentTimeMillis()));
                _transfer.setStatistics(null);
                _transfer.setStatusCode(StatusFactory.RETR);
                transferRepository.notifyAll();
            }
            _log.warn("Transfer {} ({}) failed: {}", _transfer.getId(), _dataFile.getSource(), comment);
        }
    }

    /**
     * The Interface GenericRepository.
     */
    private interface GenericRepository {
        /**
         * Gets the host.
         *
         * @param name
         *            the name
         *
         * @return the host
         */
        Host getHost(String name);

        /**
         * Gets the EC user.
         *
         * @param name
         *            the name
         *
         * @return the EC user
         */
        ECUser getECUser(String name);

        /**
         * Gets the ectrans module.
         *
         * @param name
         *            the name
         *
         * @return the ectrans module
         */
        ECtransModule getECtransModule(String name);

        /**
         * Gets the transfer method.
         *
         * @param name
         *            the name
         *
         * @return the transfer method
         */
        TransferMethod getTransferMethod(String name);

        /**
         * Gets the data input stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        InputStream getDataInputStream(Object ticket) throws IOException;

        /**
         * Gets the data output stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        OutputStream getDataOutputStream(Object ticket) throws IOException;

        /**
         * Gets the data input file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        File getDataInputFile(Object ticket) throws IOException;

        /**
         * Gets the data output file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        File getDataOutputFile(Object ticket) throws IOException;

        /**
         * Gets the original file name.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        String getOriginalFilename(Object ticket) throws IOException;
    }

    /**
     * The Class MoverRepository.
     */
    private class MoverRepository implements GenericRepository {

        /**
         * Gets the host.
         *
         * @param name
         *            the name
         *
         * @return the host
         */
        @Override
        public Host getHost(final String name) {
            return transferRepository.getHost(name);
        }

        /**
         * Gets the EC user.
         *
         * @param name
         *            the name
         *
         * @return the EC user
         */
        @Override
        public ECUser getECUser(final String name) {
            return transferRepository.getECUser(name);
        }

        /**
         * Gets the ectrans module.
         *
         * @param name
         *            the name
         *
         * @return the ectrans module
         */
        @Override
        public ECtransModule getECtransModule(final String name) {
            return transferRepository.getECtransModule(name);
        }

        /**
         * Gets the transfer method.
         *
         * @param name
         *            the name
         *
         * @return the transfer method
         */
        @Override
        public TransferMethod getTransferMethod(final String name) {
            return transferRepository.getTransferMethod(name);
        }

        /**
         * Gets the data input stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public InputStream getDataInputStream(final Object ticket) throws IOException {
            final var desc = (FileDescriptor) ticket;
            final var file = desc.getFile(false);
            final var dataFile = desc.getDataFile();
            final var size = desc.getSize();
            var posn = desc.getPosn();
            var length = desc.getLength();
            var banChecksum = posn > 0 || length != -1; // No checksum possible on a range request!
            InputStream in = null;
            if (desc.isLocal() || !OPERATIONAL) {
                if (!OPERATIONAL && (file.length() != size || !file.canRead())) {
                    _log.warn("RandomInputStream for: {}", file.getAbsolutePath());
                    in = new RandomInputStream(length == -1 ? size - posn : length);
                    banChecksum = true; // No checksum should be processed (irrelevant on random data)
                    length = 0; // No length required here, the random input stream has the exact size
                    posn = 0; // Same as for the length
                } else {
                    _log.debug("Source found locally: {} ({} bytes)", file.getAbsolutePath(), size);
                    final var inputFilter = desc.getInputFilter();
                    if (banChecksum && StreamManagerImp.isFiltered(inputFilter)) {
                        // When using a range there is no filter allowed!
                        throw new IOException("Range not supported with filters");
                    }
                    // If required the skip will be done at the end of this method
                    in = StreamManagerImp.getFilters(file.getInputStream(), inputFilter, 0);
                }
            } else {
                _log.warn("Source not found locally");
                String message = null;
                Throwable throwable = null;
                try {
                    final var hostsForSource = desc.getHostsForSource();
                    if (hostsForSource != null && hostsForSource.length > 0) {
                        in = new ECtransInputStream(hostsForSource, dataFile, posn);
                        desc.setSelectedSourceHost(((ECtransInputStream) in).getHost());
                        posn = 0; // No skip required here, will be processed in the ECtransInputStream
                    }
                } catch (final SourceNotAvailableException s) {
                    throwable = s.getCause();
                    message = s.getMessage();
                } catch (final Throwable t) {
                    throwable = t;
                    _log.debug("Looking for a remote source", t);
                } finally {
                    if (in == null) {
                        _log.warn("Source not found remotely");
                        // The source is not available anywhere, locally or remotely!
                        final var fnfe = new FileNotFoundException(
                                isNotEmpty(message) ? message : "Source not available");
                        fnfe.initCause(throwable);
                        throw fnfe;
                    }
                }
            }
            // Are we supposed to skip data?
            if (posn > 0) {
                // If we skip data then obviously we can't do the checksum on
                // the fly!
                final long result;
                if ((result = in.skip(posn)) != posn) {
                    StreamPlugThread.closeQuietly(in);
                    throw new IOException("Error skipping bytes (" + result + ")");
                }
            } else if (!banChecksum) {
                // We don't skip data and we get all the bytes (no range), so we can process the
                // Checksum!
                final var checksumValue = dataFile.getChecksum();
                if (isNotEmpty(checksumValue)) {
                    // There is a Checksum defined!
                    final var filter = desc.getFilter();
                    if (isNotEmpty(filter)) {
                        // The transmission is based on a filtered file so do we
                        // have the Checksum for this filtering method?
                        final var searchString = "/" + filter + "=";
                        final var index = checksumValue.indexOf(searchString);
                        if (index != -1) {
                            final var checksumForFilter = checksumValue.substring(index + searchString.length());
                            final var fileSize = dataFile.getFilterSize();
                            if (size == fileSize) {
                                _log.debug("Checking against {} hash ({}={})", _algorithm.getName(), filter,
                                        checksumForFilter);
                                try {
                                    return Checksum.getChecksum(checksumForFilter, _algorithm, in).getInputStream();
                                } catch (final Throwable t) {
                                    _log.warn("Cannot init {}", _algorithm.getName(), t);
                                }
                            } else {
                                // Should NEVER happen but it is better to check
                                // just in case!
                                _log.warn(
                                        "Checksum control disabled for DataFile-{}: file size inconsistency (filter={},length={},size={})",
                                        dataFile.getId(), filter, size, fileSize);
                            }
                        } else {
                            // We don't have any Checksum for this filter!
                            _log.warn("Checksum control disabled for DataFile-{}: no Checksum found for {}",
                                    dataFile.getId(), filter);
                        }
                    } else {
                        // The transmission is based on a standard file (the
                        // filtering might occur but on the fly)!
                        final var fileSize = dataFile.getSize();
                        if (size == fileSize) {
                            // We could have to remove the filtered Checksum
                            // which might have been appended!
                            final var index = checksumValue.indexOf("/");
                            final var checksumForPlain = index != -1 ? checksumValue.substring(0, index)
                                    : checksumValue;
                            _log.debug("Checking against {} hash ({})", _algorithm.getName(), checksumForPlain);
                            try {
                                return Checksum.getChecksum(checksumForPlain, _algorithm, in).getInputStream();
                            } catch (final Throwable t) {
                                _log.warn("Cannot init {}", _algorithm.getName(), t);
                            }
                        } else {
                            // Should NEVER happen but it is better to check
                            // just in case!
                            _log.warn(
                                    "Checksum control disabled for DataFile-{}: file size inconsistency (length={},size={})",
                                    dataFile.getId(), size, fileSize);
                        }
                    }
                } else {
                    // We have no Checksum set on the DataFile!
                    _log.warn("No Checksum available for DataFile-{}", dataFile.getId());
                }
            }
            if (length > 0) {
                // We only send the length requested (this is a range request)!
                return new BoundedInputStream(in, length);
            }
            return in;
        }

        /**
         * Gets the data output stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public OutputStream getDataOutputStream(final Object ticket) throws IOException {
            final var desc = (FileDescriptor) ticket;
            final var file = desc.getFile(true);
            final var posn = desc.getPosn();
            if (posn > 0 && file.length() != posn) {
                throw new IOException("Incorrect posn for append (posn!=size)");
            }
            return file.getOutputStream(posn > 0);
        }

        /**
         * Gets the data input file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public File getDataInputFile(final Object ticket) throws IOException {
            final var desc = (FileDescriptor) ticket;
            return desc.getFile(true).getFile();
        }

        /**
         * Gets the data output file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public File getDataOutputFile(final Object ticket) throws IOException {
            final var desc = (FileDescriptor) ticket;
            return desc.getFile(true).getFile();
        }

        /**
         * Gets the original filename.
         *
         * @param ticket
         *            the ticket
         *
         * @return the original filename
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public String getOriginalFilename(final Object ticket) throws IOException {
            final var desc = (FileDescriptor) ticket;
            return desc.getDataFile().getOriginal();
        }
    }

    /**
     * The Class LocalRepository.
     */
    private final class LocalRepository extends MoverRepository {
        /** The _host. */
        private final Host _host;

        /**
         * Instantiates a new local repository.
         *
         * @param host
         *            the host
         */
        private LocalRepository(final Host host) {
            _host = host;
        }

        /**
         * Gets the host.
         *
         * @param name
         *            the name
         *
         * @return the host
         */
        @Override
        public Host getHost(final String name) {
            return _host;
        }

        /**
         * Gets the EC user.
         *
         * @param name
         *            the name
         *
         * @return the EC user
         */
        @Override
        public ECUser getECUser(final String name) {
            return _host.getECUser();
        }

        /**
         * Gets the ectrans module.
         *
         * @param name
         *            the name
         *
         * @return the ectrans module
         */
        @Override
        public ECtransModule getECtransModule(final String name) {
            return _host.getTransferMethod().getECtransModule();
        }

        /**
         * Gets the transfer method.
         *
         * @param name
         *            the name
         *
         * @return the transfer method
         */
        @Override
        public TransferMethod getTransferMethod(final String name) {
            return _host.getTransferMethod();
        }
    }

    /**
     * The Class ECproxyRepository.
     */
    private static final class ECproxyRepository implements GenericRepository, ECpdsProxy {
        /** The _host. */
        private final Host _host;

        /** The _in. */
        private InputStream _in = null;

        /** The _out. */
        private OutputStream _out = null;

        /**
         * Instantiates a new ecproxy repository.
         *
         * @param host
         *            the host
         */
        private ECproxyRepository(final Host host) {
            _host = host;
        }

        /**
         * Gets the host.
         *
         * @param name
         *            the name
         *
         * @return the host
         */
        @Override
        public Host getHost(final String name) {
            return _host;
        }

        /**
         * Gets the EC user.
         *
         * @param name
         *            the name
         *
         * @return the EC user
         */
        @Override
        public ECUser getECUser(final String name) {
            return _host.getECUser();
        }

        /**
         * Gets the ectrans module.
         *
         * @param name
         *            the name
         *
         * @return the ectrans module
         */
        @Override
        public ECtransModule getECtransModule(final String name) {
            return _host.getTransferMethod().getECtransModule();
        }

        /**
         * Gets the transfer method.
         *
         * @param name
         *            the name
         *
         * @return the transfer method
         */
        @Override
        public TransferMethod getTransferMethod(final String name) {
            return _host.getTransferMethod();
        }

        /**
         * Gets the data input stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public synchronized InputStream getDataInputStream(final Object ticket) throws IOException {
            if (_in == null) {
                try {
                    wait(1 * Timer.ONE_MINUTE);
                } catch (final InterruptedException e) {
                    _log.debug("Wakeup");
                }
            }
            if (_in == null) {
                throw new IOException("InputStream not set");
            }
            return _in;
        }

        /**
         * Gets the data output stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public synchronized OutputStream getDataOutputStream(final Object ticket) throws IOException {
            if (_out == null) {
                try {
                    wait(1 * Timer.ONE_MINUTE);
                } catch (final InterruptedException e) {
                    _log.debug("Wakeup");
                }
            }
            if (_out == null) {
                throw new IOException("OutputStream not set");
            }
            return _out;
        }

        /**
         * Gets the data input file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public File getDataInputFile(final Object ticket) throws IOException {
            throw new IOException("Not supported with ECproxy");
        }

        /**
         * Gets the data output file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public File getDataOutputFile(final Object ticket) throws IOException {
            throw new IOException("Not supported with ECproxy");
        }

        /**
         * Gets the original filename.
         *
         * @param ticket
         *            the ticket
         *
         * @return the original filename
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public String getOriginalFilename(final Object ticket) throws IOException {
            throw new IOException("Not supported with ECproxy");
        }

        /**
         * Sets the output stream.
         *
         * @param out
         *            the new output stream
         */
        @Override
        public synchronized void setOutputStream(final OutputStream out) {
            _out = out;
            notifyAll();
        }

        /**
         * Sets the input stream.
         *
         * @param in
         *            the new input stream
         */
        @Override
        public synchronized void setInputStream(final InputStream in) {
            _in = in;
            notifyAll();
        }
    }

    /**
     * The Class StreamRepository. Allow bypassing the ECproxy when loading files from the current data-mover.
     */
    private static final class StreamRepository implements GenericRepository {
        /** The _host. */
        private final Host _host;

        /** The _in. */
        private final InputStream _in;

        /** The _out. */
        private final OutputStream _out;

        /**
         * Instantiates a new stream repository.
         *
         * @param host
         *            the host
         * @param out
         *            the output stream
         */
        private StreamRepository(final Host host, final OutputStream out) {
            _host = host;
            _out = out;
            _in = null;
        }

        /**
         * Instantiates a new stream repository.
         *
         * @param host
         *            the host
         * @param in
         *            the input stream
         */
        private StreamRepository(final Host host, final InputStream in) {
            _host = host;
            _out = null;
            _in = in;
        }

        /**
         * Gets the host.
         *
         * @param name
         *            the name
         *
         * @return the host
         */
        @Override
        public Host getHost(final String name) {
            return _host;
        }

        /**
         * Gets the EC user.
         *
         * @param name
         *            the name
         *
         * @return the EC user
         */
        @Override
        public ECUser getECUser(final String name) {
            return _host.getECUser();
        }

        /**
         * Gets the ectrans module.
         *
         * @param name
         *            the name
         *
         * @return the ectrans module
         */
        @Override
        public ECtransModule getECtransModule(final String name) {
            return _host.getTransferMethod().getECtransModule();
        }

        /**
         * Gets the transfer method.
         *
         * @param name
         *            the name
         *
         * @return the transfer method
         */
        @Override
        public TransferMethod getTransferMethod(final String name) {
            return _host.getTransferMethod();
        }

        /**
         * Gets the data input stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public InputStream getDataInputStream(final Object ticket) throws IOException {
            return _in;
        }

        /**
         * Gets the data output stream.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public OutputStream getDataOutputStream(final Object ticket) throws IOException {
            return _out;
        }

        /**
         * Gets the data input file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data input file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public File getDataInputFile(final Object ticket) throws IOException {
            throw new IOException("Not supported with StreamRepository");
        }

        /**
         * Gets the data output file.
         *
         * @param ticket
         *            the ticket
         *
         * @return the data output file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public File getDataOutputFile(final Object ticket) throws IOException {
            throw new IOException("Not supported with StreamRepository");
        }

        /**
         * Gets the original filename.
         *
         * @param ticket
         *            the ticket
         *
         * @return the original filename
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public String getOriginalFilename(final Object ticket) throws IOException {
            throw new IOException("Not supported with StreamRepository");
        }
    }

    /**
     * The Class TransferRepository.
     */
    private final class TransferRepository extends MBeanRepository<DataTransfer> {
        /** The _list. */
        private final List<DataTransfer> _list = new ArrayList<>();

        /**
         * Instantiates a new transfer repository.
         *
         * @param name
         *            the name
         */
        private TransferRepository(final String name) {
            super(name);
            setComparator(new TransferComparator(false));
            setDelay(Cnf.durationAt("Scheduler", "transferRepository", 5 * Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "transferRepositoryJammedTimeout", 5 * Timer.ONE_MINUTE));
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
        public DataTransfer getDataTransfer(final long id) {
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
            final var comment = byteSent >= fileSize ? Format.formatRate(fileSize, duration)
                    : Format.formatPercentage(fileSize, byteSent) + " of " + Format.formatSize(fileSize);
            return transfer.getStatusCode() + "(" + comment + " on " + transfer.getDestinationName() + "/"
                    + transfer.getHostName() + ") ";
        }

        /**
         * Clear.
         */
        @Override
        public void clear() {
            super.clear();
            _list.clear();
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            if (masterManager != null && !masterManager.isConnected()) {
                return NEXT_STEP_DELAY;
            }
            for (final DataTransfer transfer : getList()) {
                if (!_list.contains(transfer)) {
                    _list.add(transfer);
                }
            }
            final var size = _list.size();
            if (size > 0) {
                final var array = _list.toArray(new DataTransfer[size]);
                synchronized (transferRepository) {
                    try {
                        getMasterProxy().updateDataTransfers(array);
                        _list.clear();
                    } catch (final Exception e) {
                        _log.warn("updateDataTransfers", e);
                        return NEXT_STEP_DELAY;
                    }
                    for (final DataTransfer transfer : array) {
                        delDataTransfer(false, transfer.getId());
                    }
                }
            }
            return NEXT_STEP_DELAY;
        }

        /**
         * Del data transfer.
         *
         * @param force
         *            the force
         * @param id
         *            the id
         *
         * @return true, if successful
         */
        public boolean delDataTransfer(final boolean force, final long id) {
            var result = true;
            try (final var mutex = dataTransferMutexProvider.getMutex(id)) {
                synchronized (mutex.lock()) {
                    final DataTransfer transfer;
                    if ((transfer = getDataTransfer(id)) != null) {
                        final var statusCode = transfer.getStatusCode();
                        if (StatusFactory.DONE.equals(statusCode) || StatusFactory.RETR.equals(statusCode)) {
                            _log.info("Remove transfer {} from data mover ({})", id, statusCode);
                            removeValue(transfer);
                        } else if (force) {
                            _log.warn("Force removal of transfer {} from data mover ({})", id, statusCode);
                            removeValue(transfer);
                        } else {
                            result = false;
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Gets the host.
         *
         * @param name
         *            the name
         *
         * @return the host
         */
        public Host getHost(final String name) {
            if (isNotEmpty(name)) {
                for (final DataTransfer transfer : getList()) {
                    if (name.equals(transfer.getHostName())) {
                        return transfer.getHost();
                    }
                }
            }
            return null;
        }

        /**
         * Gets the EC user.
         *
         * @param name
         *            the name
         *
         * @return the ECuser
         */
        public ECUser getECUser(final String name) {
            if (isNotEmpty(name)) {
                for (final DataTransfer transfer : getList()) {
                    final var host = transfer.getHost();
                    if (name.equals(host.getECUserName())) {
                        return host.getECUser();
                    }
                }
            }
            return null;
        }

        /**
         * Gets the ectrans module.
         *
         * @param name
         *            the name
         *
         * @return the ectrans module
         */
        public ECtransModule getECtransModule(final String name) {
            if (isNotEmpty(name)) {
                for (final DataTransfer transfer : getList()) {
                    final var method = transfer.getHost().getTransferMethod();
                    if (name.equals(method.getECtransModuleName())) {
                        return method.getECtransModule();
                    }
                }
            }
            return null;
        }

        /**
         * Gets the transfer method.
         *
         * @param name
         *            the name
         *
         * @return the transfer method
         */
        public TransferMethod getTransferMethod(final String name) {
            if (isNotEmpty(name)) {
                for (final DataTransfer transfer : getList()) {
                    final var host = transfer.getHost();
                    if (name.equals(host.getTransferMethodName())) {
                        return host.getTransferMethod();
                    }
                }
            }
            return null;
        }
    }

    /**
     * The Class DownloadRepository.
     */
    public final class DownloadRepository extends MBeanRepository<DownloadProgress> {
        /** The _list. */
        private final List<DownloadProgress> _list = new ArrayList<>();

        /**
         * Instantiates a new download repository.
         *
         * @param name
         *            the name
         */
        private DownloadRepository(final String name) {
            super(name);
            setDelay(Cnf.durationAt("Scheduler", "downloadRepository", 5 * Timer.ONE_SECOND));
            setJammedTimeout(Cnf.durationAt("Scheduler", "downloadRepositoryJammedTimeout", 5 * Timer.ONE_MINUTE));
        }

        /**
         * Gets the key.
         *
         * @param dataFileId
         *            the data file id
         *
         * @return the string
         */
        private String _getKey(final long dataFileId) {
            return Format.formatLong(dataFileId, 10, true);
        }

        /**
         * Gets the key.
         *
         * @param progress
         *            the progress
         *
         * @return the key
         */
        @Override
        public String getKey(final DownloadProgress progress) {
            return _getKey(progress.getDataFileId());
        }

        /**
         * Removes the key.
         *
         * @param dataFileId
         *            the data file id
         *
         * @return the download progress
         */
        public DownloadProgress removeKey(final long dataFileId) {
            return removeKey(_getKey(dataFileId));
        }

        /**
         * Get the DownloadProgress using the key.
         *
         * @param dataFileId
         *            the data file id
         *
         * @return the download progress
         */
        public DownloadProgress getValue(final long dataFileId) {
            return getValue(_getKey(dataFileId));
        }

        /**
         * Removes the key.
         *
         * @param dataFile
         *            the data file
         *
         * @return the download progress
         */
        public DownloadProgress removeKey(final DataFile dataFile) {
            return removeKey(_getKey(dataFile.getId()));
        }

        /**
         * Gets the status.
         *
         * @param progress
         *            the progress
         *
         * @return the status
         */
        @Override
        public String getStatus(final DownloadProgress progress) {
            final var fileSize = progress.getSize();
            final var byteSent = progress.getByteSent();
            return ("DataFile " + progress.getDataFileId() + " (" + Format.formatDuration(progress.getDuration()) + "/"
                    + (fileSize != -1 ? Format.formatPercentage(fileSize, byteSent) : Format.formatSize(byteSent))
                    + ")").replace(' ', '_').replace('-', '_');
        }

        /**
         * Clear.
         */
        @Override
        public void clear() {
            // Interrupt all the download Threads!
            for (final DownloadProgress progress : getList()) {
                _log.debug("Closing DownloadProgress for DataFile-{}", progress.getDataFileId());
                progress.close();
            }
            // They should clear themselves but let's make sure the list is
            // empty!
            super.clear();
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            if (masterManager != null && !masterManager.isConnected()) {
                return NEXT_STEP_DELAY;
            }
            for (final DownloadProgress progress : getList()) {
                if (!_list.contains(progress)) {
                    _list.add(progress);
                }
            }
            if (_list.isEmpty()) {
                return NEXT_STEP_DELAY;
            }
            final var array = _list.toArray(new DownloadProgress[_list.size()]);
            final DownloadProgress[] toInterrupt;
            synchronized (downloadRepository) {
                try {
                    toInterrupt = getMasterProxy().updateDownloadProgress(array);
                    _list.clear();
                } catch (final Exception e) {
                    _log.warn("updateDownloadProgress", e);
                    return NEXT_STEP_DELAY;
                }
            }
            // Let's check if we have some retrievals which should be
            // interrupted (because not found on the Master Server)?
            for (final DownloadProgress progress : toInterrupt) {
                final var localProgress = getValue(progress.getDataFileId());
                if (localProgress != null) {
                    _log.debug("Interrupting DownloadProgress - {}", localProgress);
                    localProgress.close();
                } else {
                    _log.debug("DownloadProgress not found for interruption - {}", progress);
                }
            }
            // Let's check if we have some remaining progress in the list which
            // should be removed?
            for (final DownloadProgress progress : array) {
                final var fileSize = progress.getSize();
                final var closed = progress.isClosed();
                final var completed = fileSize != -1 && progress.getByteSent() >= fileSize;
                if ((closed || completed) && removeKey(_getKey(progress.getDataFileId())) == null) {
                    _log.warn("DownloadProgress NOT removed - {}", progress);
                }
            }
            // Now we can wait for the next loop!
            return NEXT_STEP_DELAY;
        }
    }

    /**
     * The Class MasterManager.
     */
    private final class MasterManager extends MasterConnection {
        /** The _started. */
        boolean _started = false;

        /**
         * Instantiates a new master manager.
         *
         * @param host
         *            the host
         * @param port
         *            the port
         * @param access
         *            the access
         *
         * @throws ConnectionException
         *             the connection exception
         */
        private MasterManager(final String host, final short port, final MoverInterface access)
                throws ConnectionException {
            super(host, port, access);
        }

        /**
         * Sets the started.
         *
         * @param started
         *            the new started
         */
        void setStarted(final boolean started) {
            _started = started;
        }

        /**
         * Disconnected.
         */
        @Override
        public void disconnected() {
            super.disconnected();
            if (_started) {
                resetDataMover();
            }
        }
    }
}
