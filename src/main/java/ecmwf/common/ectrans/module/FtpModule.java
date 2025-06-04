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

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SOCKET_STATISTICS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SO_MAX_PACING_RATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_CONGESTION_CONTROL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_LINGER_ENABLE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_LINGER_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_MAX_SEGMENT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_NO_DELAY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_QUICK_ACK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_TIME_STAMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_USER_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_WINDOW_CLAMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_COMM_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_CWD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_DATA_ALIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_DATA_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_DELETE_ON_RENAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_EXTENDED;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_FTPGROUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_FTPUSER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_IGNORE_CHECK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_IGNORE_DELETE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_IGNORE_MKDIRS_CMD_ERRORS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_KEEP_ALIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_KEEP_CONTROL_CONNECTION_ALIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_LIKE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_LISTEN_ADDRESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_LOGIN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_LOW_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_MD5_EXT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_MKDIRS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_MKDIRS_CMD_INDEX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_MKSUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_NOPASSWORD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PARALLEL_STREAMS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PASSIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PASSWORD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PORT_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_POST_CONNECT_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_POST_GET_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_POST_MKDIRS_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_POST_PUT_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PREFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PRE_CLOSE_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PRE_GET_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PRE_MKDIRS_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PRE_PUT_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_RECEIVE_BUFF_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_RETRY_AFTER_TIMEOUT_ON_CHECK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_SEND_BUFF_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_SET_NOOP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_SUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_USECLEANPATH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_USENLIST;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_USESUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_USETMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_USE_APPEND;
import static ecmwf.common.ectrans.ECtransOptions.HOST_FTP_USE_NOOP;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.ftp.FtpClient;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.security.RandomString;
import ecmwf.common.technical.SessionCache;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class FtpModule.
 */
public final class FtpModule extends TransferModule {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(FtpModule.class);

    /** The Constant cache. */
    private static final FtpCache cache = new FtpCache();

    /** The current status. */
    private String currentStatus = "INIT";

    /** The ftp. */
    private FtpClient ftp;

    /** The in. */
    private InputStream ftpInput;

    /** The out. */
    private OutputStream ftpOutput;

    /** The buff. */
    private BufferedReader buff;

    /** The put name. */
    private String putName = null;

    /** The get name. */
    private String getName = null;

    /** The temporary name. */
    private String temporaryName = null;

    /** The usetmp. */
    private boolean usetmp = false;

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

    /** The post get cmd. */
    private String postGetCmd = null;

    /** The pre put cmd. */
    private String prePutCmd = null;

    /** The post put cmd. */
    private String postPutCmd = null;

    /** The mkdirs. */
    private boolean mkdirs = false;

    /** The mkdirs cmd index. */
    private int mkdirsCmdIndex = 0;

    /** The pre mkdirs cmd. */
    private String preMkdirsCmd = null;

    /** The post mkdirs cmd. */
    private String postMkdirsCmd = null;

    /** The md5 ext. */
    private String md5Ext = null;

    /** The md5sum. */
    private boolean md5sum = false;

    /** The checked. */
    private boolean checked = false;

    /** The retry after timeout on check. */
    private boolean retryAfterTimeoutOnCheck = false;

    /** The keep control connection alive. */
    private boolean keepControlConnectionAlive = false;

    /** The transfer handled. */
    private boolean transferHandled = false;

    /** The usenlist. */
    private boolean usenlist = false;

    /** The usecleanpath. */
    private boolean usecleanpath = false;

    /** The parallel streams. */
    private int parallelStreams = 0;

    /** The dir. */
    private String dir = null;

    /** The setup. */
    private ECtransSetup currentSetup = null;

    /** The user. */
    private String user = null;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The ignoreMkdirsCmdError. */
    private boolean ignoreMkdirsCmdError = false;

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
        return setup.getInteger(HOST_FTP_PORT);
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
        dir = "";
        int pos;
        if ((pos = location.lastIndexOf("@")) == -1) {
            throw new IOException("Malformed URL ('@' not found)");
        }
        host = location.substring(pos + 1);
        user = location.substring(0, pos);
        if ((pos = user.lastIndexOf(":")) == -1) {
            throw new IOException("Malformed URL (':' not found)");
        }
        var password = user.substring(pos + 1);
        user = user.substring(0, pos);
        if ((pos = host.indexOf("/")) != -1) {
            dir = host.substring(pos + 1);
            host = host.substring(0, pos);
        }
        user = setup.get(HOST_FTP_LOGIN, user);
        password = setup.get(HOST_FTP_PASSWORD, password);
        usecleanpath = setup.getBoolean(HOST_FTP_USECLEANPATH);
        usenlist = setup.getBoolean(HOST_FTP_USENLIST);
        usetmp = setup.getBoolean(HOST_FTP_USETMP);
        mkdirs = setup.getBoolean(HOST_FTP_MKDIRS);
        prefix = setup.getString(HOST_FTP_PREFIX);
        suffix = setup.getString(HOST_FTP_SUFFIX);
        md5Ext = setup.getString(HOST_FTP_MD5_EXT);
        final var postConnectCmd = setup.getString(HOST_FTP_POST_CONNECT_CMD);
        preCloseCmd = setup.getString(HOST_FTP_PRE_CLOSE_CMD);
        preGetCmd = setup.getString(HOST_FTP_PRE_GET_CMD);
        prePutCmd = setup.getString(HOST_FTP_PRE_PUT_CMD);
        postGetCmd = setup.getString(HOST_FTP_POST_GET_CMD);
        postPutCmd = setup.getString(HOST_FTP_POST_PUT_CMD);
        ignoreMkdirsCmdError = currentSetup.getBoolean(HOST_FTP_IGNORE_MKDIRS_CMD_ERRORS);
        mkdirsCmdIndex = setup.getInteger(HOST_FTP_MKDIRS_CMD_INDEX);
        preMkdirsCmd = setup.getString(HOST_FTP_PRE_MKDIRS_CMD);
        postMkdirsCmd = setup.getString(HOST_FTP_POST_MKDIRS_CMD);
        keepAlive = setup.getDuration(HOST_FTP_KEEP_ALIVE).toMillis();
        useNoop = (int) setup.getDuration(HOST_FTP_USE_NOOP).toMillis();
        ignoreCheck = setup.getBoolean(HOST_FTP_IGNORE_CHECK);
        ignoreDelete = setup.getBoolean(HOST_FTP_IGNORE_DELETE);
        parallelStreams = setup.getInteger(HOST_FTP_PARALLEL_STREAMS);
        deleteOnRename = setup.getBoolean(HOST_FTP_DELETE_ON_RENAME);
        retryAfterTimeoutOnCheck = setup.getBoolean(HOST_FTP_RETRY_AFTER_TIMEOUT_ON_CHECK);
        keepControlConnectionAlive = setup.getBoolean(HOST_FTP_KEEP_CONTROL_CONNECTION_ALIVE);
        useAppend = setup.getBoolean(HOST_FTP_USE_APPEND);
        if (setup.getBoolean(HOST_FTP_MKSUFFIX)) {
            var mksuffix = new StringBuilder(".").append(new RandomString(3).next());
            if (setup.getBoolean(HOST_FTP_USESUFFIX)) {
                mksuffix.append(suffix);
            }
            suffix = mksuffix.toString();
        } else if (prefix.isEmpty() && suffix.isEmpty()) {
            suffix = ".tmp";
        }
        dir = setup.get(HOST_FTP_CWD, dir);
        final var port = getPort(getSetup());
        _log.debug("Open ftp connection on {} with port {} ({})", host, port, user);
        var connected = false;
        var fromCache = false;
        final var client = new FtpClient();
        client.setDebug(getDebug());
        if (setup.getBoolean(HOST_ECTRANS_SOCKET_STATISTICS) && getAttribute("connectOptions") != null) {
            _log.debug("Activating Socket Statistics");
            final var statistics = new ClientSocketStatistics();
            setAttribute(statistics);
            client.setClientSocketStatistics(statistics);
        }
        setupSession(client);
        setAttribute("remote.hostName", host);
        try {
            key = SessionCache.getKey(host, port, user, dir, "hash=" + client.toString().hashCode());
            if (keepAlive <= 0 || (ftp = cache.remove(key)) == null || !ftp.commandIsOpen()) {
                ftp = client;
                ftp.connect(host, port);
                ftp.login(user, getSetup().getBoolean(HOST_FTP_NOPASSWORD) ? null : password);
            } else {
                _log.debug("Found cached ftp connection ({})", key);
                fromCache = true;
            }
            if (!fromCache) {
                ftpBinary();
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
        } catch (final UnknownHostException e) {
            _log.error("{} is an unknown host", e.getMessage());
            throw new IOException("unknown host " + host);
        } catch (final ConnectException e) {
            _log.error("Connection refused to host {}:{}", host, port, e);
            throw new IOException("connection refused to host " + host + ":" + port);
        } catch (final Exception e) {
            _log.error("Connection failed to ftp host {}:{}", host, port, e);
            final var message = e.getMessage();
            throw new IOException(isNotEmpty(message) ? message : "connection failed to host " + host + ":" + port);
        } finally {
            if (!connected) {
                setStatus("ERROR");
                if (ftp != null) {
                    ftp.close(false);
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
    private void setupSession(final FtpClient ftp) throws IOException {
        getSetup().getOptionalBoolean(HOST_ECTRANS_TCP_NO_DELAY).ifPresent(ftp::setTCPNoDelay);
        getSetup().getOptionalBoolean(HOST_ECTRANS_TCP_KEEP_ALIVE).ifPresent(ftp::setTCPKeepAlive);
        getSetup().getOptionalBoolean(HOST_ECTRANS_TCP_TIME_STAMP).ifPresent(ftp::setTCPTimeStamp);
        getSetup().getOptionalBoolean(HOST_ECTRANS_TCP_QUICK_ACK).ifPresent(ftp::setTCPQuickAck);
        getSetup().getOptionalByteSize(HOST_ECTRANS_SO_MAX_PACING_RATE).ifPresent(ftp::setSOMaxPacingRate);
        ftp.setTCPLinger(getSetup().getOptionalBoolean(HOST_ECTRANS_TCP_LINGER_ENABLE).orElse(null),
                getSetup().getInteger(HOST_ECTRANS_TCP_LINGER_TIME));
        ftp.setTCPCongestion(getSetup().getString(HOST_ECTRANS_TCP_CONGESTION_CONTROL));
        ftp.setTCPMaxSegment(getSetup().getInteger(HOST_ECTRANS_TCP_MAX_SEGMENT));
        ftp.setTCPWindowClamp(getSetup().getInteger(HOST_ECTRANS_TCP_WINDOW_CLAMP));
        ftp.setTCPKeepAliveTime(getSetup().getInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_TIME));
        ftp.setTCPKeepAliveInterval(getSetup().getInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL));
        ftp.setTCPKeepAliveProbes(getSetup().getInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES));
        ftp.setTCPUserTimeout(getSetup().getInteger(HOST_ECTRANS_TCP_USER_TIMEOUT));
        final var shared = "shared".equalsIgnoreCase(getSetup().getString(HOST_FTP_PASSIVE));
        ftp.setPassive(shared || getSetup().getBoolean(HOST_FTP_PASSIVE));
        ftp.setExtended(getSetup().getBoolean(HOST_FTP_EXTENDED));
        ftp.setSharedPassive(shared);
        ftp.setMkdirs("remote".equalsIgnoreCase(getSetup().getString(HOST_FTP_MKDIRS)));
        ftp.setDataAlive(getSetup().getBoolean(HOST_FTP_DATA_ALIVE));
        ftp.setLowPort(getSetup().getBoolean(HOST_FTP_LOW_PORT));
        ftp.setCommTimeOut((int) getSetup().getDuration(HOST_FTP_COMM_TIME_OUT).toMillis());
        ftp.setDataTimeOut((int) getSetup().getDuration(HOST_FTP_DATA_TIME_OUT).toMillis());
        ftp.setPortTimeOut((int) getSetup().getDuration(HOST_FTP_PORT_TIME_OUT).toMillis());
        ftp.setListenAddress(getSetup().getString(HOST_FTP_LISTEN_ADDRESS));
        getSetup().getOptionalByteSize(HOST_FTP_SEND_BUFF_SIZE)
                .ifPresent(sendBuffSize -> ftp.setSendBufferSize((int) sendBuffSize.size()));
        getSetup().getOptionalByteSize(HOST_FTP_RECEIVE_BUFF_SIZE)
                .ifPresent(receiveBuffSize -> ftp.setSendBufferSize((int) receiveBuffSize.size()));
        ftp.setNoop(getSetup().getString(HOST_FTP_SET_NOOP));
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
            } catch (final SocketTimeoutException _) {
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
                ftp.getResponseString();
                ftp.mkdir(currentPath);
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
        checked = false;
        if (posn > 0 && usetmp) {
            throw new IOException("Append/resume not compatible with " + getSetup().getModuleName() + ".usetmp");
        }
        final var directory = new File(name).getParent();
        if (mkdirs && directory != null) {
            mkdirs(directory);
        }
        if (prePutCmd != null && !prePutCmd.isBlank()) {
            final var s = Format.replaceAll(prePutCmd, "$filename", name);
            final var tokenizer = new StringTokenizer(s, ";");
            while (tokenizer.hasMoreTokens()) {
                ftpCommand(tokenizer.nextToken());
            }
        }
        putName = name;
        temporaryName = tmpName != null ? new File(directory, tmpName).getAbsolutePath() : getName(putName);
        if (!ignoreDelete) {
            try {
                ftpDel(temporaryName);
            } catch (final IOException _) {
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
        ftpPut(name, posn, size);
        transferHandled = true;
        if (keepControlConnectionAlive) {
            ftp.keepControlConnectionAlive(true);
        }
        return ftpOutput;
    }

    /**
     * {@inheritDoc}
     *
     * Check.
     */
    @Override
    public void check(final long sent, final String checksum) throws IOException {
        _log.debug("Check file");
        setStatus("CHECK");
        if (keepControlConnectionAlive) {
            ftp.keepControlConnectionAlive(false);
        }
        if (putName == null && getName == null) {
            throw new IOException("A check should only occur after a put/get");
        }
        if (transferHandled) {
            try {
                final var code = ftp.checkPendingReply();
                _log.debug("Reply code: {}", code);
            } catch (final SocketTimeoutException e) {
                if (!retryAfterTimeoutOnCheck) {
                    throw e;
                }
                _log.debug("Retry to login after timeout", e);
                ftp.retryLogin();
                if (isNotEmpty(dir)) {
                    ftpCd(dir);
                }
            }
        }
        if (keepControlConnectionAlive) {
            // Try a NOOP to reset the control connection!
            try {
                ftp.noop();
            } catch (final Throwable _) {
            }
        }
        var remoteName = putName != null ? temporaryName : getName;
        if (putName != null && postPutCmd != null) {
            final var s = Format.replaceAll(postPutCmd, "$filename", remoteName);
            final var tokenizer = new StringTokenizer(s, ";");
            while (tokenizer.hasMoreTokens()) {
                ftpCommand(tokenizer.nextToken());
            }
        }
        if (getName != null && postGetCmd != null) {
            final var s = Format.replaceAll(postGetCmd, "$filename", remoteName);
            final var tokenizer = new StringTokenizer(s, ";");
            while (tokenizer.hasMoreTokens()) {
                ftpCommand(tokenizer.nextToken());
            }
        }
        final long size;
        if (!ignoreCheck && sent != (size = size(remoteName))) {
            throw new IOException("Remote file size is " + Format.formatPercentage(size, sent)
                    + " of original file size (sent=" + sent + "/size=" + size + ")");
        }
        closeStreams();
        if (putName != null && usetmp) {
            ftpMove(remoteName, putName);
            remoteName = putName;
        }
        if (putName != null && (checksum != null || md5sum)) {
            if (!ignoreDelete) {
                try {
                    ftpDel(remoteName + md5Ext);
                } catch (final IOException _) {
                }
            }
            if (checksum != null && !md5sum) {
                OutputStream output = null;
                var success = false;
                try {
                    ftp.getResponseString();
                    final var bytes = checksum.getBytes();
                    output = ftp.put(remoteName + md5Ext, 0, bytes.length, false, 0);
                    ftp.getResponseString();
                    _log.debug("Write checksum: {}", checksum);
                    output.write(bytes);
                    output.close();
                    success = true;
                } finally {
                    if (!success) {
                        StreamPlugThread.closeQuietly(output);
                    }
                }
            } else {
                md5sum = false;
                ftp.dumpChecksum(checksum, remoteName + md5Ext);
            }
        }
        setAttribute("remote.fileName", remoteName);
        if (!(checked = ftp.commandIsOpen())) {
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
        checked = false;
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
        if (keepControlConnectionAlive) {
            ftp.keepControlConnectionAlive(true);
        }
        return ftpInput;
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
        checked = false;
        final var result = ftpSize(name);
        final int index;
        if ((index = result.indexOf("213 ")) != -1) {
            try {
                final var size = Long.parseLong(result.substring(index + 4).trim());
                checked = true;
                return size;
            } catch (final Exception e) {
                _log.error("Can't get size for {}", name, e);
            }
        }
        throw new IOException(result);
    }

    /**
     * Modification time.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public long modificationTime(final String name) throws IOException {
        _log.debug("Modification Time {}", name);
        setStatus("MDTM");
        checked = false;
        final var result = ftpMDTM(name);
        final int index;
        if ((index = result.indexOf("213 ")) != -1) {
            try {
                final var time = Long.parseLong(result.substring(index + 4).trim());
                checked = true;
                return time;
            } catch (final Exception e) {
                _log.error("Can't get MDTM for {}", name, e);
            }
        }
        throw new IOException(result);
    }

    /**
     * {@inheritDoc}
     *
     * List as string array.
     */
    @Override
    public String[] listAsStringArray(String directory, final String pattern) throws IOException {
        _log.debug("List{}{}", directory != null ? " " + directory : "", pattern != null ? " (" + pattern + ")" : "");
        if (directory == null) {
            directory = "";
        }
        setStatus("LIST");
        checked = false;
        ftpList(directory);
        final List<String> list = new ArrayList<>();
        String line;
        var i = 0;
        while ((line = buff.readLine()) != null) {
            try {
                if (pattern == null || line.matches(pattern)) {
                    if (getDebug()) {
                        _log.debug("List[{}] {}", i++, line);
                    }
                    list.add(line);
                }
            } catch (final Exception _) {
            }
        }
        // If required transform to an ftp like listing!
        if (usenlist && getSetup().getBoolean(HOST_FTP_LIKE)) {
            final var index = directory.lastIndexOf("/");
            if (index != -1) {
                // Let's remove the pattern at the end!
                directory = directory.substring(0, index + 1);
            } else {
                // There is no path defined so we can remove everything as it is
                // only a pattern!
                directory = "";
            }
            final var ownerUser = isNotEmpty(user) ? user : "nouser";
            final var ownerGroup = isNotEmpty(user) ? user : "nogroup";
            final List<String> copy = new ArrayList<>();
            i = 0;
            for (final String name : list) {
                if (getSetup() == null) {
                    // We are not connected anymore!
                    break;
                }
                try {
                    // Is the directory included?
                    final var remoteName = name.startsWith("/") ? name : directory + name;
                    final var element = Format.getFtpList("-rw-r--r--", getSetup().get(HOST_FTP_FTPUSER, ownerUser),
                            getSetup().get(HOST_FTP_FTPGROUP, ownerGroup), String.valueOf(size(remoteName)),
                            modificationTime(remoteName), new File(name).getName());
                    if (getDebug()) {
                        _log.debug("Adding ftp[{}]: {} => {}", i++, name, element);
                    }
                    copy.add(element);
                } catch (final Throwable t) {
                    _log.warn("Error getting details for: {}", name, t);
                }
            }
            _log.debug("Selected as ftp: {} line(s)", copy.size());
            list.clear();
            list.addAll(copy);
        }
        checked = true;
        return list.toArray(new String[list.size()]);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the input filter.
     */
    @Override
    public void setInputFilter(final String filters) throws IOException {
        try {
            ftp.getResponseString();
            ftp.setInputFilters(filters);
            ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on setInputFilters");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Sets the output filter.
     */
    @Override
    public void setOutputFilter(final String filters) throws IOException {
        try {
            ftp.getResponseString();
            ftp.setOutputFilters(filters);
            ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on setOutputFilters");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Sets the input md 5.
     */
    @Override
    public void setInputMd5(final String md5) throws IOException {
        _log.debug("SetInputMd5: {}", md5);
        md5sum = true;
    }

    /**
     * {@inheritDoc}
     *
     * Delegate checksum.
     */
    @Override
    public void delegateChecksum() throws IOException {
        try {
            ftp.getResponseString();
            ftp.delegateChecksum();
            ftp.getResponseString();
            md5sum = true;
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on setOutputFilters");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            _log.debug("Close connection");
            currentStatus = "CLOSE";
            closeStreams();
            try {
                if (ftp != null && ftp.commandIsOpen()) {
                    if (putName != null && !checked) {
                        try {
                            ftpDel(getName(putName));
                        } catch (final IOException _) {
                        }
                    }
                    if (ftp.commandIsOpen() && isNotEmpty(preCloseCmd)) {
                        final var tokenizer = new StringTokenizer(preCloseCmd, ";");
                        while (tokenizer.hasMoreTokens()) {
                            ftpCommand(tokenizer.nextToken());
                        }
                    }
                    if (checked && keepAlive > 0 && key != null && ftp.commandIsOpen()) {
                        cache.put(key, ftp, keepAlive, useNoop);
                    } else {
                        ftp.close(true);
                    }
                }
            } catch (final IOException e) {
                ftp.close(false);
                throw e;
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
        StreamPlugThread.closeQuietly(ftpInput);
        StreamPlugThread.closeQuietly(ftpOutput);
    }

    /**
     * _ftp binary.
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpBinary() throws IOException {
        try {
            ftp.getResponseString();
            ftp.binary();
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on BINARY");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * _ftp cd.
     *
     * @param directory
     *            the directory
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpCd(final String directory) throws IOException {
        try {
            ftp.getResponseString();
            ftp.cd(directory);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on CD");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * _ftp del.
     *
     * @param fileName
     *            the file name
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpDel(final String fileName) throws IOException {
        try {
            ftp.getResponseString();
            ftp.delete(fileName);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on DEL");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * _ftp rmdir.
     *
     * @param fileName
     *            the file name
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpRmdir(final String fileName) throws IOException {
        try {
            ftp.getResponseString();
            ftp.rmdir(fileName);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on RMDIR");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
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
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpMove(final String source, final String target) throws IOException {
        if (deleteOnRename) {
            try {
                ftpDel(target);
            } catch (final SocketTimeoutException e) {
                // This is a timeout we might have lost the connection!
                throw e;
            } catch (final IOException e) {
                // Let's ignore it, the file might not exists!
                _log.debug("delete", e);
            }
        }
        try {
            ftp.getResponseString();
            ftp.rename(source, target);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException e) {
            throw new SocketTimeoutException("Ftp timeout on RNFR");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * _ftp mkdir.
     *
     * @param dirName
     *            the dir name
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpMkdir(final String dirName) throws IOException {
        try {
            ftp.getResponseString();
            ftp.mkdir(dirName);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on MKDIR");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
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
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpGet(final String fileName, final long posn) throws IOException {
        try {
            ftp.getResponseString();
            ftpInput = ftp.get(fileName, posn, parallelStreams);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on GET");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * _ftp list.
     *
     * @param directory
     *            the directory
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpList(final String directory) throws IOException {
        try {
            ftp.getResponseString();
            buff = usenlist ? ftp.nlist(directory) : ftp.list(directory);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on " + (usenlist ? "NLIST" : "LIST"));
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * _ftp put.
     *
     * @param fileName
     *            the file name
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpPut(final String fileName, final long posn, final long size) throws IOException {
        try {
            ftp.getResponseString();
            ftpOutput = ftp.put(fileName, posn, size, useAppend, parallelStreams);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on STOR");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * _ftp size.
     *
     * @param name
     *            the name
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpSize(final String name) throws IOException {
        try {
            ftp.getResponseString();
            ftp.size(name);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on SIZE");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
    }

    /**
     * _ftp mdtm.
     *
     * @param name
     *            the name
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String ftpMDTM(final String name) throws IOException {
        try {
            ftp.getResponseString();
            ftp.mdtm(name);
            return ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on MDTM");
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
        }
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
            ftp.getResponseString();
            ftp.issueCommandCheck(command);
            ftp.getResponseString();
        } catch (final SocketTimeoutException _) {
            throw new SocketTimeoutException("Ftp timeout on " + command);
        } catch (final IOException e) {
            throw new IOException(getExceptionMessage(e));
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

    /**
     * Gets the exception message.
     *
     * @param t
     *            the t
     *
     * @return the string
     */
    private String getExceptionMessage(final Throwable t) {
        var response = ftp.getResponseString();
        if (response == null || response.isEmpty()) {
            response = "Ftp error";
        }
        var message = t.getMessage();
        final int length;
        if (message == null || (length = message.length()) <= 0) {
            return response;
        }
        if (message.endsWith("\n")) {
            message = message.substring(0, length - 1);
        }
        return message.equals(response) ? response : response + ": " + message;
    }

    /**
     * The Class FtpCache.
     */
    private static final class FtpCache extends SessionCache<String, FtpClient> {

        /**
         * Disconnect.
         *
         * @param ftp
         *            the ftp
         */
        @Override
        public void disconnect(final FtpClient ftp) {
            ftp.close(true);
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
        public boolean isConnected(final FtpClient ftp) {
            return ftp.commandIsOpen();
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
        public void update(final FtpClient ftp) throws IOException {
            ftp.noop();
        }
    }
}
