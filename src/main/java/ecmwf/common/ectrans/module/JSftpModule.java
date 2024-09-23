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
 * ECMWF Product Data Store (OpenPDS) Project
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
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_ALLOCATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_BULK_REQUEST_NUMBER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_CHMOD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_CIPHER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_CLIENT_VERSION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_COMMIT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_COMPRESSION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_CONNECT_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_CWD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_EXEC_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_EXEC_CODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_FINGER_PRINT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_IGNORE_CHECK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_IGNORE_MKDIRS_CMD_ERRORS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_KEX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_LISTEN_ADDRESS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_LIST_MAX_DIRS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_LIST_MAX_THREADS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_LIST_MAX_WAITING;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_LIST_RECURSIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_LOGIN;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_MAC;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_MD5_EXT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_MKDIRS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_MKDIRS_CMD_INDEX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_MKSUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_OPTIONS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PASSWORD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PASS_PHRASE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_POST_MKDIRS_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PREFERRED_AUTHENTICATIONS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PREFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PRE_MKDIRS_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PRIVATE_KEY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PRIVATE_KEY_FILE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PROPERTIES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_SERVER_ALIVE_COUNT_MAX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_SERVER_ALIVE_INTERVAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_SERVER_HOST_KEY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_SESSION_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_SUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_USECLEANPATH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_USETMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_USE_WRITE_FLUSH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_WMO_LIKE_FORMAT;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.SocketFactory;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import ecmwf.common.ectrans.AllocateInterface;
import ecmwf.common.ectrans.ECtransConstants;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.ClientSocketFactory;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.security.RandomString;
import ecmwf.common.technical.ExecutorManager;
import ecmwf.common.technical.ExecutorRunnable;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;
import ecmwf.common.text.Format.DuplicatedChooseScore;
import ecmwf.common.text.Options;

/**
 * The Class JSftpModule.
 */
public class JSftpModule extends TransferModule {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(JSftpModule.class);

    /** The currentStatus. */
    private String currentStatus = "INIT";

    /** The session. */
    private Session session = null;

    /** The sftp. */
    private ChannelSftp sftp = null;

    /** The sftpOutput. */
    private OutputStream sftpOutput = null;

    /** The put name. */
    private String putName = null;

    /** The get name. */
    private String getName = null;

    /** The temporary name. */
    private String temporaryName = null;

    /** The mkdirs. */
    private boolean mkdirs = false;

    /** The usetmp. */
    private boolean usetmp = false;

    /** The prefix. */
    private String prefix = null;

    /** The suffix. */
    private String suffix = null;

    /** The md5 ext. */
    private String md5Ext = null;

    /** The chmod. */
    private String chmod = null;

    /** The exec cmd. */
    private String execCmd = null;

    /** The execution code. */
    private int executionCode = 0;

    /** The ignore check. */
    private boolean ignoreCheck = false;

    /** The wmo like format. */
    private boolean wmoLikeFormat = false;

    /** The checked. */
    private boolean checked = false;

    /** The user. */
    private String user = null;

    /** The current setup. */
    private ECtransSetup currentSetup = null;

    /** The allocate manager. */
    private AllocateInterface allocateManager = null;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The ignoreMkdirsCmdError. */
    private boolean ignoreMkdirsCmdError = false;

    /** The mkdirs cmd index. */
    private int mkdirsCmdIndex = 0;

    /** The pre mkdirs cmd. */
    private String preMkdirsCmd = null;

    /** The post mkdirs cmd. */
    private String postMkdirsCmd = null;

    /** The usecleanpath. */
    private boolean usecleanpath = false;

    /** The socket factory. */
    private JschSocketFactory socketFactory = null;

    static {
        // Allow ssh-rsa by default for our legacy customers!
        JSch.setConfig("server_host_key", JSch.getConfig("server_host_key") + ",ssh-rsa");
        JSch.setConfig("PubkeyAcceptedAlgorithms", JSch.getConfig("PubkeyAcceptedAlgorithms") + ",ssh-rsa");
    }

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
        return setup.getInteger(HOST_SFTP_PORT);
    }

    /**
     * {@inheritDoc}
     *
     * Update socket statistics.
     */
    @Override
    public void updateSocketStatistics() throws IOException {
        if (socketFactory != null) {
            socketFactory.updateStatistics();
        }
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
        String dir = null;
        int pos;
        if ((pos = location.lastIndexOf("@")) == -1) {
            throw new IOException("Malformed URL ('@' not found)");
        }
        host = location.substring(pos + 1);
        user = location.substring(0, pos);
        if ((pos = user.indexOf(":")) == -1) {
            throw new IOException("Malformed URL (':' not found)");
        }
        final var password = setup.get(HOST_SFTP_PASSWORD, user.substring(pos + 1));
        user = setup.get(HOST_SFTP_LOGIN, user.substring(0, pos));
        if ((pos = host.indexOf("/")) != -1) {
            dir = host.substring(pos + 1);
            host = host.substring(0, pos);
        }
        // Do we have an allocate request? If this is the case then we have to
        // send the request and adjust the host and directory accordingly!
        final var allocate = setup.getOptions(HOST_SFTP_ALLOCATE);
        if (!allocate.isEmpty()) {
            // Do we have the connect options available?
            final var connectOptions = (Options) getAttribute("connectOptions");
            if (isNotEmpty(connectOptions)) {
                allocate.inject(connectOptions);
                allocate.inject("$hostname", host);
            }
            // The format of the allocate request is the following:
            // sftp3.allocate="url=http://localhost:5555/allocate?dataset=fields&file=$filename&length=$filesize;req=json.pathspecs[0]"
            final var url = allocate.get("url", null);
            final var req = allocate.get("req", null);
            if (isNotEmpty(url) && isNotEmpty(req)) {
                _log.debug("Allocate requested ({})", url);
                final String get;
                try {
                    allocateManager = getRemoteProvider().getAllocateInterface(url,
                            setup.getOptions(HOST_SFTP_PROPERTIES).getProperties());
                    get = allocateManager.get(req);
                } catch (final Throwable t) {
                    throw new IOException("Processing allocate", t);
                }
                final var tokenizer = new StringTokenizer(get, ":");
                if (tokenizer.countTokens() != 2) {
                    throw new IOException("Processing allocate (received: " + get + ")");
                }
                _log.debug("Adjusting host and dir ({})", get);
                host = tokenizer.nextToken();
                dir = new File(tokenizer.nextToken()).getParent();
            }
        }
        if (isNotEmpty(dir)) {
            dir = dir + File.separator;
        }
        dir = setup.get(HOST_SFTP_CWD, dir);
        usecleanpath = setup.getBoolean(HOST_SFTP_USECLEANPATH);
        usetmp = setup.getBoolean(HOST_SFTP_USETMP);
        mkdirs = setup.getBoolean(HOST_SFTP_MKDIRS);
        prefix = setup.getString(HOST_SFTP_PREFIX);
        suffix = setup.getString(HOST_SFTP_SUFFIX);
        md5Ext = setup.getString(HOST_SFTP_MD5_EXT);
        chmod = setup.getString(HOST_SFTP_CHMOD);
        ignoreCheck = setup.getBoolean(HOST_SFTP_IGNORE_CHECK);
        wmoLikeFormat = setup.getBoolean(HOST_SFTP_WMO_LIKE_FORMAT);
        executionCode = setup.getInteger(HOST_SFTP_EXEC_CODE);
        execCmd = setup.getString(HOST_SFTP_EXEC_CMD);
        ignoreMkdirsCmdError = setup.getBoolean(HOST_SFTP_IGNORE_MKDIRS_CMD_ERRORS);
        mkdirsCmdIndex = setup.getInteger(HOST_SFTP_MKDIRS_CMD_INDEX);
        preMkdirsCmd = setup.getString(HOST_SFTP_PRE_MKDIRS_CMD);
        postMkdirsCmd = setup.getString(HOST_SFTP_POST_MKDIRS_CMD);
        final var clientVersion = setup.getString(HOST_SFTP_CLIENT_VERSION);
        final var kex = setup.getString(HOST_SFTP_KEX);
        final var serverHostKey = setup.getString(HOST_SFTP_SERVER_HOST_KEY);
        final var cipher = setup.getString(HOST_SFTP_CIPHER);
        final var mac = setup.getString(HOST_SFTP_MAC);
        final var compression = setup.getString(HOST_SFTP_COMPRESSION);
        final var fingerPrint = setup.getString(HOST_SFTP_FINGER_PRINT);
        final var passPhrase = setup.getString(HOST_SFTP_PASS_PHRASE);
        final var privateKeyFile = setup.getString(HOST_SFTP_PRIVATE_KEY_FILE);
        final var privateKey = setup.getString(HOST_SFTP_PRIVATE_KEY).trim();
        final var listenAddress = setup.getString(HOST_SFTP_LISTEN_ADDRESS);
        final var serverAliveInterval = (int) setup.getOptionalDuration(HOST_SFTP_SERVER_ALIVE_INTERVAL)
                .orElse(Duration.ZERO).toMillis();
        final int serverAliveCountMax = setup.getOptionalInteger(HOST_SFTP_SERVER_ALIVE_COUNT_MAX).orElse(0);
        final var sessionTimeOut = (int) setup.getDuration(HOST_SFTP_SESSION_TIME_OUT).toMillis();
        final var connectTimeOut = (int) setup.getDuration(HOST_SFTP_CONNECT_TIME_OUT).toMillis();
        final var config = new Properties();
        final var preferredAuthentications = setup.getString(HOST_SFTP_PREFERRED_AUTHENTICATIONS);
        if (!ECtransConstants.DEFAULT.equals(preferredAuthentications)) {
            // e.g. publickey,keyboard-interactive,password
            config.put("PreferredAuthentications", preferredAuthentications);
        }
        // e.g. "zlib,none"
        config.put("compression.s2c", compression);
        config.put("compression.c2s", compression);
        _log.debug("Using compression: {}", compression);
        if (!ECtransConstants.DEFAULT.equals(kex)) {
            // e.g. "diffie-hellman-group-exchange-sha1,
            // diffie-hellman-group1-sha1,
            // diffie-hellman-group14-sha1,
            // diffie-hellman-group-exchange-sha256,
            // ecdh-sha2-nistp256,
            // ecdh-sha2-nistp384,
            // ecdh-sha2-nistp521"
            config.put("kex", kex);
            _log.debug("Using kex: {}", kex);
        }
        if (!ECtransConstants.DEFAULT.equals(serverHostKey)) {
            // e.g.
            // ssh-rsa,ssh-dss,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521
            config.put("server_host_key", serverHostKey);
            _log.debug("Using server_host_key: {}", serverHostKey);
        }
        if (!ECtransConstants.NONE.equals(cipher)) {
            // e.g. "aes128-cbc,3des-cbc,blowfish-cbc"
            config.put("cipher.s2c", cipher);
            config.put("cipher.c2s", cipher);
            _log.debug("Using cipher: {}", cipher);
        }
        if (!ECtransConstants.NONE.equals(mac)) {
            // e.g. "hmac-md5,hmac-md5-96,hmac-sha1,hmac-sha1-96"
            config.put("mac.s2c", mac);
            config.put("mac.c2s", mac);
            _log.debug("Using MACs: {}", mac);
        }
        // Allow setting any option. These options will overwrite all the
        // previous options configured!
        final var options = setup.getString(HOST_SFTP_OPTIONS).trim();
        if (!ECtransConstants.DEFAULT.equals(options)) {
            final var reader = new BufferedReader(new StringReader(options));
            String line;
            while ((line = reader.readLine()) != null) {
                final var index = line.indexOf("=");
                if (index > 0) {
                    final var name = line.substring(0, index);
                    final var value = line.substring(index + 1);
                    _log.debug("Adding option {}={}", name, value);
                    config.put(name, value);
                }
            }
            reader.close();
        }
        if (setup.getBoolean(HOST_SFTP_MKSUFFIX)) {
            suffix = "." + new RandomString(3).next();
        } else if (prefix.length() == 0 && suffix.length() == 0) {
            suffix = ".tmp";
        }
        final var port = getPort(getSetup());
        setAttribute("remote.hostName", host);
        _log.debug("Open sftp connection on {}:{} ({})", host, port, user);
        var connected = false;
        try {
            final var jsch = new JSch();
            if (getDebug()) {
                jsch.setInstanceLogger(new JSftpLogger());
            }
            if (isNotEmpty(privateKeyFile)) {
                final var file = new File(privateKeyFile);
                final var fileName = file.getCanonicalPath();
                if (!file.canRead()) {
                    throw new IOException("Can not find/read private key: " + fileName);
                }
                _log.debug("Loading certificate from file");
                jsch.addIdentity(fileName, passPhrase);
            } else if (isNotEmpty(privateKey)) {
                _log.debug("Using private key");
                jsch.addIdentity(setup.getModuleName(), privateKey.getBytes(), null,
                        passPhrase != null ? passPhrase.getBytes() : null);
            }
            session = jsch.getSession(user, host, port);
            if (getDebug()) {
                session.setLogger(new JSftpLogger());
            }
            if (!ECtransConstants.DEFAULT.equals(clientVersion)) {
                session.setClientVersion(clientVersion);
            }
            session.setConfig(config);
            session.setUserInfo(new JSftpUserInfo(password, passPhrase, fingerPrint));
            if (serverAliveCountMax > 0) {
                session.setServerAliveCountMax(serverAliveCountMax);
            }
            if (serverAliveInterval > 0) {
                session.setServerAliveInterval(serverAliveInterval);
            }
            if (sessionTimeOut > 0) {
                session.setTimeout(sessionTimeOut);
            }
            final ClientSocketStatistics statistics;
            if (setup.getBoolean(HOST_ECTRANS_SOCKET_STATISTICS) && getAttribute("connectOptions") != null) {
                _log.debug("Activating Socket Statistics");
                statistics = new ClientSocketStatistics();
                setAttribute(statistics);
            } else {
                statistics = null;
            }
            final var socketConfig = new SocketConfig(statistics, "JSftp", getDebug());
            if (isNotEmpty(listenAddress)) {
                socketConfig.setListenAddress(listenAddress);
            }
            if (sessionTimeOut != -1) {
                socketConfig.setSoTimeOut(sessionTimeOut);
            }
            if (connectTimeOut != -1) {
                socketConfig.setConnectTimeOut(connectTimeOut);
            }
            setup.setBooleanIfPresent(HOST_ECTRANS_TCP_NO_DELAY, socketConfig::setTcpNoDelay);
            setup.setBooleanIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE, socketConfig::setKeepAlive);
            setup.setBooleanIfPresent(HOST_ECTRANS_TCP_TIME_STAMP, socketConfig::setTCPTimeStamp);
            setup.setBooleanIfPresent(HOST_ECTRANS_TCP_QUICK_ACK, socketConfig::setTCPQuickAck);
            setup.setStringIfPresent(HOST_ECTRANS_TCP_CONGESTION_CONTROL, socketConfig::setTCPCongestion);
            setup.setByteSizeIfPresent(HOST_ECTRANS_SO_MAX_PACING_RATE, socketConfig::setSOMaxPacingRate);
            setup.setIntegerIfPresent(HOST_ECTRANS_TCP_MAX_SEGMENT, socketConfig::setTCPMaxSegment);
            setup.setIntegerIfPresent(HOST_ECTRANS_TCP_WINDOW_CLAMP, socketConfig::setTCPWindowClamp);
            setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_TIME, socketConfig::setTCPKeepAliveTime);
            setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL, socketConfig::setTCPKeepAliveInterval);
            setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES, socketConfig::setTCPKeepAliveProbes);
            setup.setIntegerIfPresent(HOST_ECTRANS_TCP_USER_TIMEOUT, socketConfig::setTCPUserTimeout);
            setup.setBooleanIfPresent(HOST_ECTRANS_TCP_LINGER_ENABLE,
                    enable -> setup.setIntegerIfPresent(HOST_ECTRANS_TCP_LINGER_TIME,
                            time -> socketConfig.setTCPLinger(enable, time)));
            socketFactory = new JschSocketFactory(socketConfig);
            session.setSocketFactory(socketFactory);
            session.connect(connectTimeOut);
            _log.debug("RemoteServer: {}", session.getServerVersion());
            sftp = (ChannelSftp) session.openChannel("sftp");
            try {
                sftp.setBulkRequests(setup.getInteger(HOST_SFTP_BULK_REQUEST_NUMBER));
            } catch (final JSchException e) {
                // Ignored (parameter <= 0)
            }
            sftp.setUseWriteFlushWorkaround(setup.getBoolean(HOST_SFTP_USE_WRITE_FLUSH));
            sftp.connect();
            if (isNotEmpty(dir)) {
                if (mkdirs) {
                    try {
                        sftp.cd(dir);
                    } catch (final SftpException e) {
                        setStatus("MKDIRS");
                        mkdirs(dir);
                        setStatus("CD");
                        sftp.cd(dir);
                    }
                } else {
                    setStatus("CD");
                    sftp.cd(dir);
                }
                _log.debug("Working directory: {}", dir);
            }
            connected = true;
        } catch (final Throwable t) {
            _log.error("Can't connect to {}:{} (user={})", host, port, user, t);
            throwIOException(t, "connect to " + host + ":" + port);
        } finally {
            if (!connected) {
                setStatus("ERROR");
                close();
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
        try {
            sftp.rm(name);
        } catch (final SftpException e) {
            _log.debug("rm", e);
            throwIOException(e, "rm " + name);
        }
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
                    exec(tokenizer.nextToken());
                }
            } catch (final Throwable t) {
                if (!ignoreMkdirsCmdError) {
                    throwIOException(t, "Executing " + ext + "MkdirsCmd");
                }
            }
        }
    }

    /**
     * Mkdirs.
     *
     * @param dir
     *            the dir
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void mkdirs(final String dir) throws IOException {
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
                sftp.mkdir(currentPath);
            } catch (final SftpException e) {
                // Ignored
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
        try {
            sftp.mkdir(dir);
        } catch (final SftpException e) {
            _log.debug("mkdir", e);
            throwIOException(e, "mkdir " + dir);
        }
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
        try {
            sftp.rmdir(dir);
        } catch (final SftpException e) {
            _log.debug("rmdir", e);
            throwIOException(e, "rmdir " + dir);
        }
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
        try {
            return sftp.lstat(name).getSize();
        } catch (final SftpException e) {
            _log.debug("lstat", e);
            throwIOException(e, "lstat " + name);
        }
        return -1;
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
        try {
            sftp.rename(source, target);
        } catch (final SftpException e) {
            _log.debug("rename", e);
            throwIOException(e, "rename " + source + " to " + target);
        }
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
            throw new IOException("Append not compatible with " + getSetup().getModuleName() + ".usetmp");
        }
        final var dir = new File(name).getParent();
        if (mkdirs && dir != null) {
            try {
                mkdirs(dir);
            } catch (final IOException e) {
            }
        }
        putName = wmoLikeFormat ? Format.toWMOFormat(name) : name;
        temporaryName = tmpName != null ? new File(dir, tmpName).getAbsolutePath() : getName(putName);
        if (posn > 0) {
            long fileSize = -1;
            try {
                fileSize = sftp.lstat(name).getSize();
            } catch (final SftpException e) {
                _log.debug("lstat", e);
                throwIOException(e, "lstat " + name);
            }
            if (posn != fileSize) {
                throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
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
        if (posn > 0 && posn != size) {
            throw new IOException("Only append supported (posn!=size)");
        }
        try {
            sftpOutput = sftp.put(name, posn > 0 ? ChannelSftp.APPEND : ChannelSftp.OVERWRITE);
        } catch (final SftpException e) {
            _log.debug("put", e);
            throwIOException(e, "put " + name);
        }
        return sftpOutput;
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public boolean put(final InputStream in, String name, final long posn, final long size) throws IOException {
        name = prePut(name, null, posn);
        _log.debug("Put file {} ({})", name, posn);
        setStatus("PUT");
        if (posn > 0 && posn != size) {
            throw new IOException("Only append supported (posn!=size)");
        }
        try {
            sftp.put(in, name, posn > 0 ? ChannelSftp.APPEND : ChannelSftp.OVERWRITE);
        } catch (final SftpException e) {
            _log.debug("put", e);
            throwIOException(e, "put " + name);
        }
        return true;
    }

    /**
     * Tries to find the correct error message for the transfer history.
     *
     * @param t
     *            the t
     * @param op
     *            the op
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static final void throwIOException(final Throwable t, final String op) throws IOException {
        final var message = "Failed to " + op + " (";
        if (t instanceof final SftpException sftpException) {
            final var sb = new StringBuilder(message);
            switch (sftpException.id) {
            case ChannelSftp.SSH_FX_EOF:
                sb.append("end of file");
                break;
            case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                sb.append("no such file");
                break;
            case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                sb.append("permission denied");
                break;
            case ChannelSftp.SSH_FX_FAILURE:
                sb.append("failure");
                break;
            case ChannelSftp.SSH_FX_BAD_MESSAGE:
                sb.append("wrong packet or protocol incompatibility");
                break;
            case ChannelSftp.SSH_FX_NO_CONNECTION:
                sb.append("no connection");
                break;
            case ChannelSftp.SSH_FX_CONNECTION_LOST:
                sb.append("connection lost");
                break;
            case ChannelSftp.SSH_FX_OP_UNSUPPORTED:
                sb.append("not supported by server");
                break;
            default:
                sb.append("unknown error");
                break;
            }
            throw new IOException(sb.append(": ").append(sftpException.getMessage()).append(")").toString());
        }
        throw new IOException(message + Format.getMessage(t) + ")");
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
        if (putName == null && getName == null) {
            throw new IOException("A check should only occur after a put/get");
        }
        var remoteName = putName != null ? temporaryName : getName;
        final long size;
        if (!ignoreCheck && sent != (size = size(remoteName))) {
            throw new IOException("Remote file size is " + Format.formatPercentage(size, sent)
                    + " of expected file size (expected-size=" + sent + "/real-size=" + size + ")");
        }
        StreamPlugThread.closeQuietly(sftpOutput);
        if (putName != null && usetmp) {
            _log.debug("Deleting {}", putName);
            try {
                sftp.rm(putName);
            } catch (final Exception ignored) {
            }
            _log.debug("Renaming from {} to {}", remoteName, putName);
            try {
                sftp.rename(remoteName, putName);
                remoteName = putName;
            } catch (final Throwable t) {
                _log.debug("rename", t);
                throwIOException(t, "rename " + remoteName + " to " + putName);
            }
        }
        if (putName != null && isNotEmpty(chmod)) {
            try {
                sftp.chmod(Integer.parseInt(chmod, 8), remoteName);
            } catch (final Throwable t) {
                _log.debug("chmod", t);
                throwIOException(t, "chmod " + remoteName);
            }
        }
        if (putName != null && checksum != null) {
            OutputStream output = null;
            var success = false;
            try {
                output = sftp.put(remoteName + md5Ext, ChannelSftp.OVERWRITE);
                output.write(checksum.getBytes());
                output.close();
                success = true;
            } catch (final Throwable t) {
                _log.debug("put", t);
                throwIOException(t, "put " + remoteName + md5Ext);
            } finally {
                if (!success) {
                    StreamPlugThread.closeQuietly(output);
                }
            }
            if (isNotEmpty(chmod)) {
                try {
                    sftp.chmod(Integer.parseInt(chmod, 8), remoteName + md5Ext);
                } catch (final Throwable t) {
                    _log.debug("chmod", t);
                    throwIOException(t, "chmod " + remoteName + md5Ext);
                }
            }
        }
        if (isNotEmpty(execCmd)) {
            String s;
            try {
                s = Format.replaceAll(execCmd, "$filename", remoteName);
                s = Format.choose(s);
            } catch (final DuplicatedChooseScore e) {
                throw new IOException("Could not compute exec command (multiple choices selected)");
            } catch (final ScriptException e) {
                throw new IOException("Could not compute exec command (" + e.getMessage() + ")");
            }
            if (isNotEmpty(s)) {
                try {
                    final var execCode = exec(s);
                    if (execCode != executionCode) {
                        throw new IOException(
                                "Unexpected return code for exec (" + executionCode + "!=" + execCode + ")");
                    }
                } catch (final Throwable t) {
                    _log.debug("exec", t);
                    throwIOException(t, "exec '" + s + "'");
                }
            }
        }
        setAttribute("remote.fileName", remoteName);
        if (!(checked = sftp.isConnected())) {
            throw new IOException("Transfer aborted (connection closed)");
        }
        // Do we have an allocate manager. If it is the case then we have to
        // send a commit?
        if (allocateManager != null) {
            // The format of the parameter is the following:
            // sftp3.commit="url=http://localhost:5555/commit;req=200"
            final var commit = getSetup().getOptions(HOST_SFTP_COMMIT);
            final var url = commit.get("url", null);
            final var req = commit.get("req", 200);
            if (isNotEmpty(url)) {
                _log.debug("Commit requested ({})", url);
                final int status;
                try {
                    status = allocateManager.commit(url);
                } catch (final Throwable t) {
                    throw new IOException("Processing commit", t);
                }
                if (status != req) {
                    throw new IOException("Processing commit (status: " + status + ")");
                }
            }
        }
    }

    /**
     * Exec.
     *
     * @param command
     *            the command
     *
     * @return the int
     *
     * @throws com.jcraft.jsch.JSchException
     *             the j sch exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public int exec(final String command) throws JSchException, IOException {
        final var err = new ByteArrayOutputStream();
        final var channel = session.openChannel("exec");
        var result = -1;
        try {
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(err);
            final var input = channel.getInputStream();
            channel.connect();
            final var tmp = new byte[1024];
            while (true) {
                while (input.available() > 0) {
                    final var i = input.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    if (_log.isDebugEnabled() && getDebug()) {
                        _log.debug(new String(tmp, 0, i));
                    }
                }
                if (channel.isClosed()) {
                    result = channel.getExitStatus();
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (final Exception e) {
                    // Ignored
                }
            }
        } finally {
            channel.disconnect();
        }
        if (getDebug()) {
            err.flush();
            if (_log.isDebugEnabled() && err.size() > 0) {
                _log.debug("Error: {}", new String(err.toByteArray()));
            }
        }
        return result;
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
        getName = name;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        throw new IOException("Please use OutputStream method");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public boolean get(final OutputStream out, final String name, final long posn) throws IOException {
        preGet(name, posn);
        _log.debug("Get file {} (posn={})", name, posn);
        setStatus("GET");
        try {
            if (posn > 0) {
                sftp.get(name, out, null, ChannelSftp.RESUME, posn);
            } else {
                sftp.get(name, out);
            }
        } catch (final SftpException e) {
            _log.debug("get", e);
            throwIOException(e, "get " + name);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * List as string array.
     */
    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        _log.debug("List{}{}", directory.length() > 0 ? " " + directory : "",
                pattern != null ? " (" + pattern + ")" : "");
        setStatus("LIST");
        final List<String> resultList = Collections.synchronizedList(new ArrayList<>());
        final ExecutorManager<ListThread> manager = getSetup().getBoolean(HOST_SFTP_LIST_RECURSIVE)
                ? new ExecutorManager<>(getSetup().getInteger(HOST_SFTP_LIST_MAX_WAITING),
                        getSetup().getInteger(HOST_SFTP_LIST_MAX_THREADS))
                : null;
        // Let's start the listing!
        list(manager, resultList, directory, directory, pattern, 0);
        if (manager != null) {
            // We don't want to take more jobs!
            manager.stopRun();
            // And now we wait for all the Threads to complete!
            try {
                manager.join();
            } catch (final InterruptedException e) {
                _log.warn("Interrupted", e);
            }
        }
        if (getDebug()) {
            var i = 0;
            for (final String result : resultList) {
                _log.debug("Line[{}]: {}", i++, result);
            }
            _log.debug("Total: {}", resultList.size());
        }
        Collections.sort(resultList, new ListComparator());
        return resultList.toArray(new String[resultList.size()]);
    }

    /**
     * Ls. Utility class to get the list as an ArrayList.
     *
     * @param sftp
     *            the sftp
     * @param path
     *            the path
     *
     * @return the list
     *
     * @throws SftpException
     *             the sftp exception
     */
    private static List<LsEntry> ls(final ChannelSftp sftp, final String path) throws SftpException {
        final var list = new ArrayList<LsEntry>();
        sftp.ls(path == null || path.isEmpty() ? "." : path, entry -> {
            list.add(entry);
            return 0;
        });
        return list;
    }

    /**
     * List.
     *
     * @param manager
     *            the manager
     * @param resultList
     *            the result list
     * @param rootDirectory
     *            the root directory
     * @param directory
     *            the directory
     * @param pattern
     *            the pattern
     * @param level
     *            the level
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void list(final ExecutorManager<ListThread> manager, final List<String> resultList,
            final String rootDirectory, final String directory, final String pattern, final int level)
            throws IOException {
        _log.debug("List{}", directory != null ? " " + directory : "");
        setStatus("LIST");
        final List<LsEntry> orig;
        ChannelSftp currentSftp = null;
        try {
            currentSftp = (ChannelSftp) session.openChannel("sftp");
            currentSftp.connect();
            orig = ls(currentSftp, directory != null && !directory.isEmpty() ? directory : ".");
        } catch (SftpException | JSchException e) {
            _log.debug("ls", e);
            throw new IOException(e.getMessage());
        } finally {
            if (currentSftp != null)
                currentSftp.exit();
        }
        var i = 0;
        for (final LsEntry entry : orig) {
            if (getDebug()) {
                _log.debug("Processing {}", entry.getLongname());
            }
            final var attrs = entry.getAttrs();
            final var fileName = entry.getFilename();
            if (fileName == null || attrs == null) {
                continue;
            }
            try {
                if (manager != null && attrs.isDir() && !".".equals(fileName) && !"..".equals(fileName)) {
                    // Let's parse this directory!
                    if (level <= getSetup().getInteger(HOST_SFTP_LIST_MAX_DIRS)) {
                        // Do we have to start the ListManager?
                        if (level == 0 && !manager.isAlive()) {
                            manager.start();
                        }
                        // Process new directory!
                        manager.put(new ListThread(manager, resultList, rootDirectory, getFullName(directory, fileName),
                                pattern, level + 1));
                    } else {
                        _log.warn("Discarding {} (max-directory): {}", fileName, level);
                    }
                } else // This is a new file!
                if (pattern == null || fileName.matches(pattern)) {
                    final var fullName = getFullName(directory, fileName);
                    final var currentPrefix = rootDirectory
                            + (rootDirectory.length() > 0 && !rootDirectory.endsWith("/") ? "/" : "");
                    final var line = Format.getFtpList(attrs.getPermissionsString(), String.valueOf(attrs.getUId()),
                            String.valueOf(attrs.getGId()), String.valueOf(attrs.getSize()), attrs.getMTime() * 1000L,
                            fullName.startsWith(currentPrefix) ? fullName.substring(currentPrefix.length()) : fullName);
                    if (getDebug()) {
                        _log.debug("List[{}] {}", i++, line);
                    }
                    resultList.add(line);
                }
            } catch (final Exception e) {
                _log.debug("Processing {} listing", directory, e);
            }
        }
    }

    /**
     * Gets the full name.
     *
     * @param directory
     *            the directory
     * @param name
     *            the name
     *
     * @return the full name
     */
    private static final String getFullName(final String directory, final String name) {
        return directory + (directory.length() > 0 && !directory.endsWith("/") && !name.startsWith("/") ? "/" : "")
                + name;
    }

    /**
     * The Class ListThread. Listing meant to be used in multiple instances in parallel!
     */
    final class ListThread extends ExecutorRunnable {

        /** The manager. */
        final ExecutorManager<ListThread> manager;

        /** The result list. */
        final List<String> resultList;

        /** The root directory. */
        final String rootDirectory;

        /** The current directory. */
        final String currentDirectory;

        /** The level. */
        final int level;

        /** The pattern. */
        final String pattern;

        /**
         * Instantiates a new list thread.
         *
         * @param manager
         *            the manager
         * @param resultList
         *            the result list
         * @param rootDirectory
         *            the root directory
         * @param directory
         *            the directory
         * @param pattern
         *            the pattern
         * @param level
         *            the level
         */
        ListThread(final ExecutorManager<ListThread> manager, final List<String> resultList, final String rootDirectory,
                final String directory, final String pattern, final int level) {
            super(manager);
            this.manager = manager;
            this.resultList = resultList;
            this.rootDirectory = rootDirectory;
            this.currentDirectory = directory;
            this.pattern = pattern;
            this.level = level;
        }

        /**
         * Process.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void process() throws IOException {
            list(manager, resultList, rootDirectory, currentDirectory, pattern, level);
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
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            _log.debug("Close connection");
            currentStatus = "CLOSE";
            StreamPlugThread.closeQuietly(sftpOutput);
            if (sftp != null && sftp.isConnected()) {
                if (putName != null && !checked) {
                    try {
                        // Was commented!
                        sftp.rm(getName(putName));
                    } catch (final Exception e) {
                        _log.debug("rm", e);
                    }
                }
                try {
                    sftp.disconnect();
                } catch (final Exception e) {
                    _log.debug("disconnect", e);
                } finally {
                    sftp = null;
                }
            }
            if (session != null && session.isConnected()) {
                try {
                    session.disconnect();
                } catch (final Exception e) {
                    _log.debug("disconnect", e);
                } finally {
                    session = null;
                }
            }
            _log.debug("Close completed");
        } else {
            _log.debug("Already closed");
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
     * A factory for creating JschSocket objects.
     */
    private static final class JschSocketFactory implements SocketFactory {

        /** The socket factory. */
        private final ClientSocketFactory socketFactory;

        /**
         * Instantiates a new jsch socket factory.
         *
         * @param socketConfig
         *            the socket config
         */
        private JschSocketFactory(final SocketConfig socketConfig) {
            socketFactory = socketConfig.getSocketFactory();
        }

        /**
         * Update statistics.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public void updateStatistics() throws IOException {
            socketFactory.updateStatistics();
        }

        /**
         * Creates a new JschSocket object.
         *
         * @param host
         *            the host
         * @param port
         *            the port
         *
         * @return the socket
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public Socket createSocket(final String host, final int port) throws IOException {
            return socketFactory.createSocket(host, port);
        }

        /**
         * Gets the input stream.
         *
         * @param socket
         *            the socket
         *
         * @return the input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public InputStream getInputStream(final Socket socket) throws IOException {
            return socket.getInputStream();
        }

        /**
         * Gets the output stream.
         *
         * @param socket
         *            the socket
         *
         * @return the output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public OutputStream getOutputStream(final Socket socket) throws IOException {
            return socket.getOutputStream();
        }
    }

    /**
     * The Class ListComparator.
     */
    private static final class ListComparator implements Comparator<String> {

        /**
         * Compare.
         *
         * @param o1
         *            the o 1
         * @param o2
         *            the o 2
         *
         * @return the int
         */
        @Override
        public int compare(final String o1, final String o2) {
            return o1.substring(56).compareTo(o2.substring(56));
        }
    }

    /**
     * The Class JSftpUserInfo.
     */
    private static final class JSftpUserInfo implements UserInfo, UIKeyboardInteractive {
        /** The passwd. */
        private final String passwd;

        /** The pass phrase. */
        private final String passPhrase;

        /** The fingerprint. */
        private final String fingerprint;

        /**
         * Instantiates a new j sftp user info.
         *
         * @param passwd
         *            the passwd
         * @param passPhrase
         *            the pass phrase
         * @param fingerprint
         *            the fingerprint
         */
        private JSftpUserInfo(final String passwd, final String passPhrase, final String fingerprint) {
            this.fingerprint = fingerprint;
            this.passPhrase = passPhrase;
            this.passwd = passwd;
        }

        /**
         * Prompt keyboard interactive.
         *
         * @param destination
         *            the destination
         * @param name
         *            the name
         * @param instruction
         *            the instruction
         * @param prompt
         *            the prompt
         * @param echo
         *            the echo
         *
         * @return the string[]
         */
        @Override
        public String[] promptKeyboardInteractive(final String destination, final String name, final String instruction,
                final String[] prompt, final boolean[] echo) {
            if (prompt == null) {
                return new String[0];
            }
            final var response = new String[prompt.length];
            for (var i = 0; i < prompt.length; i++) {
                _log.debug("Keyboard interactive prompt: {}", prompt[i]);
                response[i] = passwd;
            }
            return response;
        }

        /**
         * Gets the password.
         *
         * @return the password
         */
        @Override
        public String getPassword() {
            return passwd;
        }

        /**
         * Prompt yes no.
         *
         * @param str
         *            the str
         *
         * @return true, if successful
         */
        @Override
        public boolean promptYesNo(final String str) {
            final var result = fingerprint == null || fingerprint.trim().length() == 0 || str == null
                    || str.indexOf("fingerprint is " + fingerprint + ".") >= 0;
            _log.debug("PromptYesNo: {} ({})", str, result ? "yes" : "no");
            return result;
        }

        /**
         * Gets the passphrase.
         *
         * @return the passphrase
         */
        @Override
        public String getPassphrase() {
            return passPhrase;
        }

        /**
         * Prompt passphrase.
         *
         * @param message
         *            the message
         *
         * @return true, if successful
         */
        @Override
        public boolean promptPassphrase(final String message) {
            _log.debug("PromptPassphrase: {}", message);
            return passPhrase != null;
        }

        /**
         * Prompt password.
         *
         * @param message
         *            the message
         *
         * @return true, if successful
         */
        @Override
        public boolean promptPassword(final String message) {
            _log.debug("PromptPassword: {}", message);
            return passwd != null;
        }

        /**
         * Show message.
         *
         * @param message
         *            the message
         */
        @Override
        public void showMessage(final String message) {
            _log.debug("ShowMessage: {}", message);
        }
    }

    /**
     * The Class ProgressMonitor.
     */
    public static final class ProgressMonitor implements SftpProgressMonitor {
        /** The _count. */
        private long count = 0;

        /** The _ended. */
        private boolean ended = false;

        /**
         * Inits the.
         *
         * @param op
         *            the op
         * @param src
         *            the src
         * @param dest
         *            the dest
         * @param max
         *            the max
         */
        @Override
        public void init(final int op, final String src, final String dest, final long max) {
            _log.debug("Op: {}, src: {}, dest: {}, max: {}", op, src, dest, max);
        }

        /**
         * Count.
         *
         * @param count
         *            the count
         *
         * @return true, if successful
         */
        @Override
        public boolean count(final long count) {
            this.count = count;
            return true;
        }

        /**
         * End.
         */
        @Override
        public void end() {
            _log.debug("Count: {}", count);
            ended = true;
        }

        /**
         * Checks if is ended.
         *
         * @return true, if is ended
         */
        public boolean isEnded() {
            return ended;
        }
    }

    /**
     * The Class JSftpLogger.
     */
    public static final class JSftpLogger implements com.jcraft.jsch.Logger {

        /**
         * Checks if is enabled.
         *
         * @param level
         *            the level
         *
         * @return true, if is enabled
         */
        @Override
        public boolean isEnabled(final int level) {
            return true;
        }

        /**
         * Log.
         *
         * @param level
         *            the level
         * @param message
         *            the message
         */
        @Override
        public void log(final int level, final String message) {
            switch (level) {
            case DEBUG:
                _log.debug(message);
                break;
            case INFO:
                _log.info(message);
                break;
            case WARN:
                _log.warn(message);
                break;
            case ERROR:
                _log.error(message);
                break;
            case FATAL:
                _log.fatal(message);
                break;
            default:
                break;
            }
        }
    }
}
