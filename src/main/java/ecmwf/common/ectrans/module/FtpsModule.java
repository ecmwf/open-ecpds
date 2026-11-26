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

package ecmwf.common.ectrans.module;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_CLOSE_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_CONNECTION_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_CONNECTION_TYPE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_CWD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_DELETE_ON_RENAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_IGNORE_CHECK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_IGNORE_DELETE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_IGNORE_MKDIRS_CMD_ERRORS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_KEEP_ALIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_LISTEN_ADDRESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_LOGIN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_MD5_EXT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_MKDIRS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_MKDIRS_CMD_INDEX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_MKSUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PASSIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PASSWORD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_POST_CONNECT_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_POST_GET_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_POST_MKDIRS_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_POST_PUT_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PREFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PRE_CLOSE_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PRE_GET_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PRE_MKDIRS_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PRE_PUT_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PROTOCOL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_READ_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_RECEIVE_BUFF_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_SEND_BUFF_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_STRICT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_SUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_USECLEANPATH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_USESUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_USETMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_USE_APPEND;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_USE_NOOP;
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.security.RandomString;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;
import ecmwf.common.technical.SessionCache;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;

/**
 * The Class FtpsModule.
 */
public final class FtpsModule extends TransferModule {
    private enum ConnectionType {
        FTP, FTPS, FTPES
    }

    private boolean rotatingCredential;
    private volatile boolean rotationClosed;
    private java.util.concurrent.CompletableFuture<Void> pendingTransfer;

    @Override
    public java.util.Optional<ecmwf.common.ectrans.PasswordRotation> passwordRotation() {
        return java.util.Optional.of(new ecmwf.common.ectrans.FileCredentialRotation<FtpsModule>("ftps",
                FtpsModule::new, java.util.Set.of(ecmwf.common.ectrans.FileCredentialRotation.STANDARD_METHOD),
                java.util.Set.of("ftps.port", "ftps.connectionType", "ftps.strict", "ftps.passive"),
                java.util.Set.of("mode")) {
            @Override
            protected void validate(final ResourceEndpoint endpoint) {
                if (!java.util.Set.of("explicit", "implicit").contains(endpoint.option("mode"))) {
                    throw new IllegalArgumentException("Expected encrypted FTP mode");
                }
            }

            @Override
            protected void connect(final FtpsModule module, final ResourceEndpoint endpoint, final String directory,
                    final String credential) throws Exception {
                module.rotatingCredential = true;
                module.rotationClosed = false;
                module.currentSetup = setup();
                module.currentStatus = "CONNECT";
                module.keepAlive = 0;
                module.usetmp = false;
                module.ignoreDelete = true;
                module.ftp = new VerifiedFTPSClient("implicit".equals(endpoint.option("mode")), endpoint.host());
                module.closeTimeout = 10000;
                module.ftp.setConnectTimeout(30000);
                module.ftp.setDefaultTimeout(60000);
                module.ftp.setDataTimeout(60000);
                module.ftp.setControlEncoding(java.nio.charset.Charset.defaultCharset().name());
                module.connectAndLogin(endpoint.host(), endpoint.port(), endpoint.login(), credential);
                module.ftp.enterLocalPassiveMode();
                require(module.ftp.setFileType(FTP.BINARY_FILE_TYPE), module.ftp);
                if (directory != null && !directory.isBlank()) {
                    require(module.ftp.changeWorkingDirectory(directory), module.ftp);
                }
            }
        });
    }

    @Override
    public boolean isSessionClosureConfirmed() {
        return rotationClosed;
    }

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(FtpsModule.class);

    /** The Constant cache. */
    private static final FtpCache cache = new FtpCache();

    /** The current status. */
    private String currentStatus = "INIT";

    /** The ftp client. */
    private FTPClient ftp;

    private ProtocolCommandListener commandListener;

    private int closeTimeout;

    /** The input. */
    private InputStream ftpsInput;

    /** The output. */
    private OutputStream ftpsOutput;

    /** The user. */
    private String user;

    /** The put name. */
    private String putName = null;

    /** The get name. */
    private String getName = null;

    /** The temporary name. */
    private String temporaryName = null;

    /** The mkdirs. */
    private boolean mkdirs = false;

    /** The mkdirs cmd index. */
    private int mkdirsCmdIndex = 0;

    /** The pre mkdirs cmd. */
    private String preMkdirsCmd = null;

    /** The post mkdirs cmd. */
    private String postMkdirsCmd = null;

    /** The ignoreMkdirsCmdError. */
    private boolean ignoreMkdirsCmdError = false;

    /** The usetmp. */
    private boolean usetmp = false;

    /** The usecleanpath. */
    private boolean usecleanpath = false;

    /** The prefix. */
    private String prefix = null;

    /** The suffix. */
    private String suffix = null;

    /** The key. */
    private String key = null;

    /** The ignore check. */
    private boolean ignoreCheck = true;

    /** The ignore delete. */
    private boolean ignoreDelete = true;

    /** The delete on rename. */
    private boolean deleteOnRename = true;

    /** The keep alive. */
    private long keepAlive = 0;

    /** The use noop. */
    private int useNoop = 0;

    /** The pre close cmd. */
    private String preCloseCmd = null;

    /** The pre get cmd. */
    private String preGetCmd = null;

    /** The pre put cmd. */
    private String prePutCmd = null;

    /** The post put cmd. */
    private String postPutCmd = null;

    /** The post get cmd. */
    private String postGetCmd = null;

    /** The md5 ext. */
    private String md5Ext = null;

    /** The checked. */
    private boolean checked = false;

    /** The transfer handled. */
    private boolean transferHandled = false;

    /** The current setup. */
    private ECtransSetup currentSetup = null;

    /** The transferError. */
    private volatile String transferError = null;

    /** The send buffer size. */
    private int sendBufferSize = -1;

    /** The receive buffer size. */
    private int receiveBufferSize = -1;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The useAppend. */
    private boolean useAppend = false;

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() {
        return currentStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort(final ECtransSetup setup) {
        return setup.getInteger(HOST_FTPS_PORT);
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     */
    @Override
    public void connect(final String location, final ECtransSetup setup) throws IOException {
        // The location is: user:password@host/dir
        currentSetup = setup;
        setStatus("CONNECT");
        String host;
        String password;
        var dir = "";
        int pos;
        if ((pos = location.lastIndexOf("@")) == -1) {
            throw new IOException("Malformed URL ('@' not found)");
        }
        host = location.substring(pos + 1);
        user = location.substring(0, pos);
        if ((pos = user.indexOf(":")) == -1) {
            throw new IOException("Malformed URL (':' not found)");
        }
        password = user.substring(pos + 1);
        user = user.substring(0, pos);
        if ((pos = host.indexOf("/")) != -1) {
            dir = host.substring(pos + 1);
            host = host.substring(0, pos);
        }
        user = setup.get(HOST_FTPS_LOGIN, user);
        password = setup.get(HOST_FTPS_PASSWORD, password);
        usecleanpath = setup.getBoolean(HOST_FTPS_USECLEANPATH);
        usetmp = setup.getBoolean(HOST_FTPS_USETMP);
        mkdirs = setup.getBoolean(HOST_FTPS_MKDIRS);
        prefix = setup.getString(HOST_FTPS_PREFIX);
        suffix = setup.getString(HOST_FTPS_SUFFIX);
        md5Ext = setup.getChecksumExtension(HOST_FTPS_MD5_EXT);
        final var postConnectCmd = setup.getString(HOST_FTPS_POST_CONNECT_CMD);
        preCloseCmd = setup.getString(HOST_FTPS_PRE_CLOSE_CMD);
        preGetCmd = setup.getString(HOST_FTPS_PRE_GET_CMD);
        prePutCmd = setup.getString(HOST_FTPS_PRE_PUT_CMD);
        postGetCmd = setup.getString(HOST_FTPS_POST_GET_CMD);
        postPutCmd = setup.getString(HOST_FTPS_POST_PUT_CMD);
        ignoreMkdirsCmdError = currentSetup.getBoolean(HOST_FTPS_IGNORE_MKDIRS_CMD_ERRORS);
        mkdirsCmdIndex = setup.getInteger(HOST_FTPS_MKDIRS_CMD_INDEX);
        preMkdirsCmd = setup.getString(HOST_FTPS_PRE_MKDIRS_CMD);
        postMkdirsCmd = setup.getString(HOST_FTPS_POST_MKDIRS_CMD);
        keepAlive = setup.getDuration(HOST_FTPS_KEEP_ALIVE).toMillis();
        if (hasTransferSession()) {
            keepAlive = 0;
        }
        useNoop = (int) setup.getDuration(HOST_FTPS_USE_NOOP).toMillis();
        ignoreCheck = setup.getBoolean(HOST_FTPS_IGNORE_CHECK);
        ignoreDelete = setup.getBoolean(HOST_FTPS_IGNORE_DELETE);
        deleteOnRename = setup.getBoolean(HOST_FTPS_DELETE_ON_RENAME);
        useAppend = setup.getBoolean(HOST_FTPS_USE_APPEND);
        if (setup.getBoolean(HOST_FTPS_MKSUFFIX)) {
            var mksuffix = new StringBuilder(".").append(new RandomString(3).next());
            if (setup.getBoolean(HOST_FTPS_USESUFFIX)) {
                mksuffix.append(suffix);
            }
            suffix = mksuffix.toString();
        } else if (prefix.isEmpty() && suffix.isEmpty()) {
            suffix = ".tmp";
        }
        dir = setup.get(HOST_FTPS_CWD, dir);
        final var port = getPort(getSetup());
        _log.debug("Open ftp connection on {} with port {} ({})", host, port, user);
        var connected = false;
        var fromCache = false;
        final var connectionTypeName = setup.getString(HOST_FTPS_CONNECTION_TYPE);
        var connectionType = ConnectionType.FTP;
        if (isNotEmpty(connectionTypeName)) {
            if ("FTP".equalsIgnoreCase(connectionTypeName)) {
                connectionType = ConnectionType.FTP;
            } else if ("FTPS".equalsIgnoreCase(connectionTypeName)) {
                connectionType = ConnectionType.FTPS;
            } else if ("FTPES".equalsIgnoreCase(connectionTypeName)) {
                connectionType = ConnectionType.FTPES;
            } else {
                throw new IOException("connectionType not recognized: " + connectionTypeName);
            }
        }
        closeTimeout = (int) setup.getDuration(HOST_FTPS_CLOSE_TIME_OUT).toMillis();
        final var connectionTimeout = (int) setup.getDuration(HOST_FTPS_CONNECTION_TIME_OUT).toMillis();
        final var readTimeout = (int) setup.getDuration(HOST_FTPS_READ_TIME_OUT).toMillis();
        final var listenAddress = setup.getString(HOST_FTPS_LISTEN_ADDRESS);
        setup.getOptionalByteSize(HOST_FTPS_SEND_BUFF_SIZE)
                .ifPresent(sendBuffSize -> this.sendBufferSize = (int) sendBuffSize.size());
        setup.getOptionalByteSize(HOST_FTPS_RECEIVE_BUFF_SIZE)
                .ifPresent(receiveBuffSize -> this.receiveBufferSize = (int) receiveBuffSize.size());
        setAttribute("remote.hostName", host);
        try {
            key = SessionCache.getKey(host, port, user, dir, connectionType, closeTimeout, connectionTimeout,
                    readTimeout, sendBufferSize, receiveBufferSize, listenAddress == null ? "default" : listenAddress,
                    setup.getBoolean(HOST_FTPS_STRICT), setup.getString(HOST_FTPS_PROTOCOL));
            if (keepAlive <= 0 || (ftp = cache.remove(key)) == null || !serverIsOpen(ftp)) {
                if (connectionType == ConnectionType.FTP) {
                    ftp = new RawFTPClient();
                } else if (!setup.getBoolean(HOST_FTPS_STRICT)) {
                    _log.debug("Disable SSL Validation");
                    final var sslcontext = SSLContext.getInstance(setup.getString(HOST_FTPS_PROTOCOL));
                    final X509TrustManager tm = new X509TrustManager() {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            if (getDebug()) {
                                _log.debug("getAcceptedIssuers");
                            }
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                                throws CertificateException {
                            if (getDebug()) {
                                _log.debug("checkClientTrusted: {}", authType);
                            }
                        }

                        @Override
                        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                                throws CertificateException {
                            if (getDebug()) {
                                _log.debug("checkServerTrusted: {}", authType);
                            }
                        }
                    };
                    sslcontext.init(null, new TrustManager[] { tm }, new SecureRandom());
                    ftp = new RawFTPSClient(connectionType == ConnectionType.FTPS, sslcontext);
                } else {
                    ftp = new RawFTPSClient(connectionType == ConnectionType.FTPS, SSLContext.getDefault());
                }
                ftp.setConnectTimeout(connectionTimeout);
                ftp.setDefaultTimeout(readTimeout);
                ftp.setDataTimeout(readTimeout);
                ftp.setControlEncoding(java.nio.charset.Charset.defaultCharset().name());
                if (sendBufferSize > 0) {
                    ftp.setSendBufferSize(sendBufferSize);
                    ftp.setSendDataSocketBufferSize(sendBufferSize);
                }
                if (receiveBufferSize > 0) {
                    ftp.setReceiveBufferSize(receiveBufferSize);
                    ftp.setReceieveDataSocketBufferSize(receiveBufferSize);
                }
                setupSession(ftp);
                connectAndLogin(host, port, user, password);
                setDataMode(ftp);
                if (isNotEmpty(listenAddress)) {
                    ftp.setActiveExternalIPAddress(listenAddress);
                    if ("0.0.0.0".equals(listenAddress) || "::".equals(listenAddress)) {
                        ftp.setReportActiveExternalIPAddress(ftp.getLocalAddress().getHostAddress());
                    }
                }
            } else {
                _log.debug("Found cached ftp connection (keepAlive: {})", keepAlive);
                ftp.setSoTimeout(readTimeout);
                setupSession(ftp);
                fromCache = true;
            }
            if (!fromCache) {
                require(ftp.setFileType(FTP.BINARY_FILE_TYPE), ftp);
                if (!dir.isEmpty()) {
                    if (mkdirs) {
                        try {
                            ftpCd(dir);
                        } catch (final IOException _) {
                            mkdirs(dir);
                            setStatus("CD");
                            ftpCd(dir);
                        }
                    } else {
                        setStatus("CD");
                        ftpCd(dir);
                    }
                    _log.debug("Working directory: {}", dir);
                }
                if (isNotEmpty(postConnectCmd)) {
                    final var tokenizer = new StringTokenizer(postConnectCmd, ";");
                    while (tokenizer.hasMoreTokens()) {
                        ftpCommand(tokenizer.nextToken());
                    }
                }
            }
            connected = true;
        } catch (final UnknownHostException _) {
            _log.error("{} is an unknown host", host);
            throw new IOException("unknown host " + host);
        } catch (final ConnectException e) {
            _log.error("Connection refused to host {}:{}", host, port, e);
            throw new IOException("connection refused to host " + host + ":" + port);
        } catch (final Exception e) {
            _log.error("Connection failed to host {}:{}", host, port, e);
            final var message = e.getMessage();
            throw new IOException(isNotEmpty(message) ? message : "connection failed to host " + host + ":" + port);
        } finally {
            if (!connected) {
                setStatus("ERROR");
                if (ftp != null) {
                    if (ftp.isConnected()) {
                        try {
                            ftp.setSoTimeout(closeTimeout);
                        } catch (final IOException ignored) {
                            // Disconnect below.
                        }
                    }
                    closeServer(ftp);
                }
            }
        }
    }

    /**
     * _setup session.
     *
     * @param ftp
     *            the ftp
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void setupSession(final FTPClient ftp) throws IOException {
        setDataMode(ftp);
        if (getDebug()) {
            _log.debug("Adding Communication Listener for Debug");
            commandListener = new ProtocolCommandListener() {
                @Override
                public void protocolCommandSent(final ProtocolCommandEvent event) {
                    if ("PASS".equalsIgnoreCase(event.getCommand()) || "ACCT".equalsIgnoreCase(event.getCommand())) {
                        _log.debug("Sent: {} ******", event.getCommand());
                    } else {
                        _log.debug("Sent: {}", event.getMessage().trim());
                    }
                }

                @Override
                public void protocolReplyReceived(final ProtocolCommandEvent event) {
                    _log.debug("Received: {}", event.getMessage().trim());
                }
            };
            ftp.addProtocolCommandListener(commandListener);
        }
    }

    private void setDataMode(final FTPClient ftp) throws IOException {
        if (getSetup().getBoolean(HOST_FTPS_PASSIVE)) {
            ftp.enterLocalPassiveMode();
        } else {
            ftp.enterLocalActiveMode();
        }
    }

    private void connectAndLogin(final String host, final int port, final String login, final String password)
            throws IOException {
        ftp.connect(host, port);
        require(FTPReply.isPositiveCompletion(ftp.getReplyCode()), ftp);
        require(ftp.login(login, password), ftp);
        if (ftp.hasFeature("UTF8") || ftp.hasFeature("UTF-8")) {
            ((RawListing) ftp).useUtf8();
            ftp.sendCommand("OPTS", "UTF8 ON");
        }
        if (ftp instanceof final FTPSClient secure) {
            secure.execPBSZ(0);
            secure.execPROT("P");
        }
    }

    private static void require(final boolean success, final FTPClient ftp) throws IOException {
        if (!success) {
            throw new IOException(ftp.getReplyString());
        }
    }

    /**
     * _server is open.
     *
     * @param ftp
     *            the ftp
     *
     * @return true, if successful
     */
    private static boolean serverIsOpen(final FTPClient ftp) {
        return ftp.isConnected();
    }

    /**
     * Close server.
     *
     * @param ftp
     *            the ftp
     */
    private static void closeServer(final FTPClient ftp) {
        try {
            ftp.logout();
        } catch (final Exception _) {
            // Close the socket even if the server does not acknowledge logout.
        } finally {
            try {
                ftp.disconnect();
            } catch (final IOException _) {
                // Ignored.
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final String name) throws IOException {
        _log.debug("Delete file {}", name);
        setStatus("DEL");
        ftpDel(name);
    }

    /**
     * Exec mkdirs cmd.
     *
     * @param ext
     *            the ext
     * @param cmd
     *            the cmd
     * @param path
     *            the path
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void execMkdirsCmd(final String ext, final String cmd, final String path) throws IOException {
        if (isNotEmpty(cmd)) {
            try {
                final var sb = new StringBuilder(cmd);
                Format.replaceAll(sb, "$uid", user);
                Format.replaceAll(sb, "$dirname", path);
                final var tokenizer = new StringTokenizer(sb.toString(), ";");
                while (tokenizer.hasMoreTokens()) {
                    ftpCommand(tokenizer.nextToken());
                }
            } catch (final SocketTimeoutException e) {
                throw new SocketTimeoutException("Timeout on " + ext + "MkdirsCmd");
            } catch (final IOException e) {
                if (!ignoreMkdirsCmdError) {
                    throw e;
                }
            }
        }
    }

    /**
     * _mkdirs.
     *
     * @param dir
     *            the dir
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void mkdirs(final String dir) throws IOException {
        _log.debug("Mkdirs {}", dir);
        setStatus("MKDIRS");
        final var token = new StringTokenizer(dir, "\\/");
        final var path = new StringBuilder(dir.startsWith("\\") || dir.startsWith("/") ? File.separator : "");
        var index = 0;
        final var length = token.countTokens();
        while (token.hasMoreElements()) {
            path.append(token.nextToken() + File.separator);
            final var currentPath = usecleanpath ? Format.getCleanPath(path.toString()) : path.toString();
            index++;
            if ((mkdirsCmdIndex > 0 && mkdirsCmdIndex >= index || mkdirsCmdIndex < 0 && length + mkdirsCmdIndex < index)
                    && isNotEmpty(preMkdirsCmd)) {
                execMkdirsCmd("pre", preMkdirsCmd, currentPath);
            }
            try {
                ftpMkdir(currentPath);
            } catch (final SocketTimeoutException _) {
                // May be we have lost the connection!
                throw new SocketTimeoutException("Ftp timeout on MKDIR");
            } catch (final IOException _) {
                // We ignore it, the directory might already exists!
            }
            if ((mkdirsCmdIndex > 0 && mkdirsCmdIndex >= index || mkdirsCmdIndex < 0 && length + mkdirsCmdIndex < index)
                    && isNotEmpty(postMkdirsCmd)) {
                execMkdirsCmd("post", postMkdirsCmd, currentPath);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Mkdir.
     */
    @Override
    public void mkdir(final String dir) throws IOException {
        _log.debug("Mkdir {}", dir);
        setStatus("MKDIR");
        ftpMkdir(dir);
    }

    /**
     * {@inheritDoc}
     *
     * Rmdir.
     */
    @Override
    public void rmdir(final String dir) throws IOException {
        _log.debug("Rmdir {}", dir);
        setStatus("RMDIR");
        ftpRmdir(dir);
    }

    /**
     * {@inheritDoc}
     *
     * Move.
     */
    @Override
    public void move(final String source, final String target) throws IOException {
        _log.debug("Move file {} to {}", source, target);
        setStatus("MOVE");
        ftpMove(source, target);
    }

    /**
     * {@inheritDoc}
     *
     * Pre put.
     */
    @Override
    public String prePut(final String name, final String tmpName, final long posn) throws IOException {
        _log.debug("PrePut file {} ({})", name, posn);
        setStatus("PREPUT");
        if (posn > 0 && usetmp) {
            throw new IOException("Append/resume not compatible with " + getSetup().getModuleName() + ".usetmp");
        }
        final var dir = new File(name).getParent();
        if (mkdirs && dir != null) {
            mkdirs(dir);
        }
        if (prePutCmd != null && !prePutCmd.isBlank()) {
            final var s = Format.replaceAll(prePutCmd, "$filename", name);
            final var tokenizer = new StringTokenizer(s, ";");
            while (tokenizer.hasMoreTokens()) {
                ftpCommand(tokenizer.nextToken());
            }
        }
        putName = name;
        temporaryName = tmpName != null ? new File(dir, tmpName).getAbsolutePath() : getName(putName);
        if (!ignoreDelete) {
            try {
                ftpDel(temporaryName);
            } catch (final IOException _) {
                // Ignored!
            }
        }
        return temporaryName;
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public OutputStream put(String name, final long posn, final long size) throws IOException {
        name = prePut(name, null, posn);
        _log.debug("Put file {} ({})", name, posn);
        setStatus("PUT");
        ftpPut(name, posn);
        transferHandled = true;
        return ftpsOutput;
    }

    /**
     * {@inheritDoc}
     *
     * Check.
     */
    @Override
    public void check(final long sent, final String checksum, final boolean error) throws IOException {
        if (error) {
            closeStreams();
        }
        awaitTransfer();
        _log.debug("Check file");
        setStatus("CHECK");
        if (putName == null && getName == null) {
            throw new IOException("A check should only occur after a put/get");
        }
        if (transferHandled && transferError != null) {
            throw new IOException(transferError);
        }
        if (error) {
            // Nothing more to do if the transfer was not successful!
            return;
        }
        var remoteName = putName != null ? temporaryName : getName;
        if (putName != null && isNotEmpty(postPutCmd)) {
            final var s = Format.replaceAll(postPutCmd, "$filename", remoteName);
            final var tokenizer = new StringTokenizer(s, ";");
            while (tokenizer.hasMoreTokens()) {
                ftpCommand(tokenizer.nextToken());
            }
        }
        if (getName != null && isNotEmpty(postGetCmd)) {
            final var s = Format.replaceAll(postGetCmd, "$filename", remoteName);
            final var tokenizer = new StringTokenizer(s, ";");
            while (tokenizer.hasMoreTokens()) {
                ftpCommand(tokenizer.nextToken());
            }
        }
        long size;
        if (!ignoreCheck && sent != (size = size(remoteName))) {
            throw new IOException("Remote file size is " + Format.formatPercentage(size, sent)
                    + " of original file size (sent=" + sent + "/size=" + size + ")");
        }
        closeStreams();
        if (putName != null && usetmp) {
            ftpMove(remoteName, putName);
            remoteName = putName;
        }
        if (putName != null && checksum != null) {
            final var md5Name = remoteName + md5Ext;
            if (!ignoreDelete) {
                try {
                    ftpDel(md5Name);
                } catch (final IOException _) {
                    // Ignored!
                }
            }
            try {
                require(ftp.storeFile(md5Name, new ByteArrayInputStream(checksum.getBytes(StandardCharsets.UTF_8))),
                        ftp);
            } catch (final Throwable t) {
                throw new IOException(Format.getMessage(t));
            }
        }
        setAttribute("remote.fileName", remoteName);
        if (!(checked = serverIsOpen(ftp))) {
            throw new IOException("Transfer aborted (connection closed)");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Pre get.
     */
    @Override
    public void preGet(final String name, final long posn) throws IOException {
        _log.debug("PreGet file {}", name);
        setStatus("PREGET");
        if (preGetCmd != null && !preGetCmd.isBlank()) {
            final var s = Format.replaceAll(preGetCmd, "$filename", name);
            final var tokenizer = new StringTokenizer(s, ";");
            while (tokenizer.hasMoreTokens()) {
                ftpCommand(tokenizer.nextToken());
            }
        }
        getName = name;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        preGet(name, posn);
        _log.debug("Get file {}", name);
        setStatus("GET");
        ftpGet(name, posn);
        transferHandled = true;
        return ftpsInput;
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String name) throws IOException {
        _log.debug("Size file {}", name);
        setStatus("SIZE");
        return ftpSize(name);
    }

    /**
     * {@inheritDoc}
     *
     * List as string array.
     */
    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        _log.debug("listAsStringArray{}{}", isEmpty(directory) ? "" : " " + directory,
                isEmpty(pattern) ? "" : " (" + pattern + ")");
        setStatus("LIST");
        final List<String> list = new ArrayList<>();
        var i = 0;
        for (final String line : ftpList(isNotEmpty(directory) ? directory : "")) {
            if (_log.isDebugEnabled() && getDebug()) {
                _log.debug("List[{}] {}", i++, line.trim());
            }
            list.add(line.trim());
        }
        // Remove first and last line!
        return list.toArray(new String[list.size()]);
    }

    /**
     * Gets the setup. Utility call to get the ECtransSetup and check if the module is not closed!
     *
     * @return the setup
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private ECtransSetup getSetup() throws IOException {
        if (closed.get()) {
            throw new IOException("Module closed");
        }
        return currentSetup;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            if (rotatingCredential || hasTransferSession()) {
                closeSessionResources(ftpsInput, ftpsOutput, this::awaitTransfer, () -> {
                    if (ftp != null && ftp.isConnected()) {
                        try {
                            ftp.setSoTimeout(closeTimeout);
                            require(ftp.logout(), ftp);
                            ftp.disconnect();
                        } catch (final Exception failure) {
                            try {
                                ftp.disconnect();
                            } catch (final IOException ignored) {
                                failure.addSuppressed(ignored);
                            }
                            throw new IOException("Unable to confirm encrypted FTP session closure");
                        }
                    }
                });
                rotationClosed = ftp == null || !ftp.isConnected();
                return;
            }
            _log.debug("Close connection");
            currentStatus = "CLOSE";
            closeStreams();
            try {
                awaitTransfer();
                if (ftp != null && serverIsOpen(ftp)) {
                    if (putName != null && !checked) {
                        try {
                            ftpDel(getName(putName));
                        } catch (final Throwable _) {
                            // Ignored!
                        }
                    }
                    if (serverIsOpen(ftp) && isNotEmpty(preCloseCmd)) {
                        final var tokenizer = new StringTokenizer(preCloseCmd, ";");
                        while (tokenizer.hasMoreTokens()) {
                            ftpCommand(tokenizer.nextToken());
                        }
                    }
                    if (checked && keepAlive > 0 && serverIsOpen(ftp) && key != null) {
                        if (commandListener != null) {
                            ftp.removeProtocolCommandListener(commandListener);
                            commandListener = null;
                        }
                        ftp.setSoTimeout(closeTimeout);
                        cache.put(key, ftp, keepAlive, useNoop);
                    } else {
                        ftp.setSoTimeout(closeTimeout);
                        closeServer(ftp);
                    }
                }
            } catch (final Throwable t) {
                closeServer(ftp);
                throw t;
            }
            _log.debug("Close completed");
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * _close streams.
     */
    private void closeStreams() {
        StreamPlugThread.closeQuietly(ftpsInput);
        StreamPlugThread.closeQuietly(ftpsOutput);
    }

    /**
     * _ftp cd.
     *
     * @param directory
     *            the directory
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void ftpCd(final String directory) throws IOException {
        try {
            require(ftp.changeWorkingDirectory(directory), ftp);
        } catch (final Throwable t) {
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * _ftp del.
     *
     * @param fileName
     *            the file name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void ftpDel(final String fileName) throws IOException {
        try {
            require(ftp.deleteFile(fileName), ftp);
        } catch (final Throwable t) {
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * _ftp rmdir.
     *
     * @param fileName
     *            the file name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void ftpRmdir(final String fileName) throws IOException {
        try {
            require(ftp.removeDirectory(fileName), ftp);
        } catch (final Throwable t) {
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * _ftp move.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void ftpMove(final String source, final String target) throws IOException {
        if (deleteOnRename) {
            try {
                ftpDel(target);
            } catch (final IOException e) {
                _log.debug("delete", e);
            }
        }
        try {
            require(ftp.rename(source, target), ftp);
        } catch (final Throwable t) {
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * _ftp mkdir.
     *
     * @param dirName
     *            the dir name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void ftpMkdir(final String dirName) throws IOException {
        try {
            require(ftp.makeDirectory(dirName), ftp);
        } catch (final Throwable t) {
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * _ftp get.
     *
     * @param fileName
     *            the file name
     * @param posn
     *            the posn
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void ftpGet(final String fileName, final long posn) throws IOException {
        if (getDebug()) {
            _log.debug("Remote name: {}", fileName);
        }
        final var in = new PipedInputStream();
        final var out = new PipedOutputStream(in);
        ftpsInput = in;
        final var completion = new java.util.concurrent.CompletableFuture<Void>();
        pendingTransfer = completion;
        new ConfigurableRunnable() {
            @Override
            public void configurableRun() {
                try {
                    ftp.setRestartOffset(posn);
                    require(ftp.retrieveFile(fileName, out), ftp);
                } catch (final Throwable t) {
                    transferError = Format.getMessage(t);
                } finally {
                    try {
                        out.close();
                    } catch (final Throwable t) {
                        if (transferError == null) {
                            transferError = Format.getMessage(t);
                        }
                    }
                    completion.complete(null);
                }
            }
        }.execute();
    }

    /**
     * _ftp list.
     *
     * @param directory
     *            the directory
     *
     * @return the list
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private List<String> ftpList(final String directory) throws IOException {
        try {
            return ((RawListing) ftp).listDirectory(directory);
        } catch (final Throwable t) {
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * _ftp put.
     *
     * @param fileName
     *            the file name
     * @param posn
     *            the posn
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void ftpPut(final String fileName, final long posn) throws IOException {
        if (getDebug()) {
            _log.debug("Remote name: {}", fileName);
        }
        final var in = new PipedInputStream();
        ftpsOutput = new PipedOutputStream(in);
        final var completion = new java.util.concurrent.CompletableFuture<Void>();
        pendingTransfer = completion;
        new ConfigurableRunnable() {
            @Override
            public void configurableRun() {
                try {
                    if (useAppend) {
                        in.skipNBytes(posn);
                        require(ftp.appendFile(fileName, in), ftp);
                    } else {
                        ftp.setRestartOffset(posn);
                        require(ftp.storeFile(fileName, in), ftp);
                    }
                } catch (final Throwable t) {
                    transferError = Format.getMessage(t);
                } finally {
                    try {
                        in.close();
                    } catch (final Throwable t) {
                        if (transferError == null) {
                            transferError = Format.getMessage(t);
                        }
                    }
                    completion.complete(null);
                }
            }
        }.execute();

    }

    private void awaitTransfer() throws IOException {
        if (pendingTransfer != null) {
            if (ftpsOutput != null) {
                ftpsOutput.close();
            }
            try {
                if (rotatingCredential || hasTransferSession()) {
                    pendingTransfer.get(65, java.util.concurrent.TimeUnit.SECONDS);
                } else {
                    pendingTransfer.get();
                }
            } catch (final InterruptedException failure) {
                Thread.currentThread().interrupt();
                throw new IOException("FTP transfer interrupted", failure);
            } catch (final Exception failure) {
                throw new IOException("FTP transfer completion unconfirmed", failure);
            }
        }
    }

    /**
     * _ftp size.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private long ftpSize(final String name) throws IOException {
        try {
            final var size = ftp.getSize(name);
            if (size == null) {
                throw new IOException(ftp.getReplyString());
            }
            return Long.parseLong(size);
        } catch (final Throwable t) {
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * _ftp command.
     *
     * @param command
     *            the command
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void ftpCommand(final String command) throws IOException {
        if (command == null || command.isEmpty()) {
            return;
        }
        try {
            final var space = command.indexOf(' ');
            final var code = space < 0 ? ftp.sendCommand(command)
                    : ftp.sendCommand(command.substring(0, space), command.substring(space + 1));
            require(FTPReply.isPositiveCompletion(code), ftp);
        } catch (final Throwable t) {
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the status
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void setStatus(final String status) throws IOException {
        _log.debug("Status set to: {}", status);
        if (closed.get()) {
            throw new IOException("Module closed");
        }
        currentStatus = status;
    }

    /**
     * Gets the name.
     *
     * @param name
     *            the name
     *
     * @return the string
     */
    private String getName(final String name) {
        final var file = new File(name);
        final var pathName = file.getParent();
        return (isNotEmpty(pathName) ? pathName + File.separator : "")
                + (usetmp ? prefix + file.getName() + suffix : file.getName());
    }

    private interface RawListing {
        List<String> listDirectory(String directory) throws IOException;

        void useUtf8() throws IOException;
    }

    private static List<String> readListing(final FTPClient client, final Socket socket, final boolean machineReadable)
            throws IOException {
        if (socket == null) {
            throw new IOException(client.getReplyString());
        }
        final var lines = new ArrayList<String>();
        final var charset = machineReadable ? StandardCharsets.UTF_8
                : java.nio.charset.Charset.forName(client.getControlEncoding());
        try (socket; var reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(socket.getInputStream(), charset))) {
            for (String line; (line = reader.readLine()) != null;) {
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
        require(client.completePendingCommand(), client);
        return lines;
    }

    private static class RawFTPClient extends FTPClient implements RawListing {
        @Override
        public void useUtf8() throws IOException {
            setControlEncoding(StandardCharsets.UTF_8.name());
            _controlInput_ = new org.apache.commons.net.io.CRLFLineReader(
                    new java.io.InputStreamReader(_socket_.getInputStream(), StandardCharsets.UTF_8));
            _controlOutput_ = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(_socket_.getOutputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public List<String> listDirectory(final String directory) throws IOException {
            final var machineReadable = hasFeature("MLSD");
            require(setFileType(FTP.ASCII_FILE_TYPE), this);
            try {
                return readListing(this,
                        _openDataConnection_(machineReadable ? "MLSD" : "LIST", directory.isEmpty() ? null : directory),
                        machineReadable);
            } finally {
                require(setFileType(FTP.BINARY_FILE_TYPE), this);
            }
        }
    }

    private static class RawFTPSClient extends FTPSClient implements RawListing {
        RawFTPSClient(final boolean implicit, final SSLContext context) {
            super(implicit, context);
        }

        @Override
        public void useUtf8() throws IOException {
            setControlEncoding(StandardCharsets.UTF_8.name());
            _controlInput_ = new org.apache.commons.net.io.CRLFLineReader(
                    new java.io.InputStreamReader(_socket_.getInputStream(), StandardCharsets.UTF_8));
            _controlOutput_ = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(_socket_.getOutputStream(), StandardCharsets.UTF_8));
        }

        @Override
        protected void execAUTH() throws IOException {
            var reply = execAUTH("TLS");
            if (reply != 234 && reply != 334) {
                reply = execAUTH("SSL");
            }
            if (reply != 234 && reply != 334) {
                throw new javax.net.ssl.SSLException(getReplyString());
            }
        }

        @Override
        public List<String> listDirectory(final String directory) throws IOException {
            final var machineReadable = hasFeature("MLSD");
            require(setFileType(FTP.ASCII_FILE_TYPE), this);
            try {
                return readListing(this,
                        _openDataConnection_(machineReadable ? "MLSD" : "LIST", directory.isEmpty() ? null : directory),
                        machineReadable);
            } finally {
                require(setFileType(FTP.BINARY_FILE_TYPE), this);
            }
        }
    }

    private static final class VerifiedFTPSClient extends RawFTPSClient {
        private final String approvedHost;

        VerifiedFTPSClient(final boolean implicit, final String host) throws java.security.NoSuchAlgorithmException {
            super(implicit, SSLContext.getDefault());
            approvedHost = host;
            setEndpointCheckingEnabled(true);
            setEnabledProtocols(new String[] { "TLSv1.3", "TLSv1.2" });
        }

        @Override
        protected Socket _openDataConnection_(final String command, final String argument) throws IOException {
            final var socket = super._openDataConnection_(command, argument);
            // Commons Net's endpoint checking covers the control socket, not the data socket.
            if (socket != null && (!(socket instanceof final javax.net.ssl.SSLSocket tls)
                    || !new org.apache.hc.client5.http.ssl.DefaultHostnameVerifier().verify(approvedHost,
                            tls.getSession()))) {
                socket.close();
                throw new javax.net.ssl.SSLHandshakeException("FTPS data channel hostname mismatch");
            }
            return socket;
        }
    }

    /**
     * The Class FtpCache.
     */
    private static class FtpCache extends SessionCache<String, FTPClient> {

        /**
         * Disconnect.
         *
         * @param ftp
         *            the ftp
         */
        @Override
        public void disconnect(final FTPClient ftp) {
            closeServer(ftp);
        }

        /**
         * Checks if is connected.
         *
         * @param ftp
         *            the ftp
         *
         * @return true, if is connected
         */
        @Override
        public boolean isConnected(final FTPClient ftp) {
            return serverIsOpen(ftp);
        }

        /**
         * Update.
         *
         * @param ftp
         *            the ftp
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void update(final FTPClient ftp) throws IOException {
            try {
                require(ftp.sendNoOp(), ftp);
            } catch (final Throwable t) {
                throw new IOException(Format.getMessage(t));
            }
        }
    }
}
