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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECAUTH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_CHMOD_ON_COPY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_COPY_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_CWD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_EXEC_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_EXEC_CODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_HOST_LIST;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_IGNORE_CHECK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_IGNORE_MKDIRS_CMD_ERRORS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_KEEP_ALIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_MKDIRS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_MKDIRS_CMD_INDEX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_MKSUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PASS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_POST_MKDIRS_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PREFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PRE_MKDIRS_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PROTOCOL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PROXY_LIST;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_RESOLVE_IP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_SESSION_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_SUFFIX;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_USEMGET;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_USER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_USETMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_USE_NOOP;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.timer.Timer;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSchException;

import ecmwf.common.database.Host;
import ecmwf.common.ecaccess.ConnectionException;
import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ecauth.InteractiveSession;
import ecmwf.common.ecauth.SSHSession;
import ecmwf.common.ecauth.TelnetSession;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.RemoteProvider;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.security.RandomString;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.IPAddress;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.SessionCache;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.Synchronized;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.text.Format;
import ecmwf.common.text.Format.DuplicatedChooseScore;
import ecmwf.ecpds.mover.MoverServer.ECproxyCallback;

/**
 * The Class ECauthModule.
 */
public final class ECauthModule extends ProxyModule {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECauthModule.class);

    /** The Constant mover. */
    private static final MoverServer mover = StarterServer.getInstance(MoverServer.class);

    /** The Constant sessionCache. */
    private static final InteractiveSessionCache sessionCache = new InteractiveSessionCache();

    /** The Constant IP cache. */
    private static final Map<String, String> IPcache = new ConcurrentHashMap<>();

    /** The Constant manager. */
    private static final HostListManager manager = new HostListManager();

    /** The Constant ok token. */
    private static final String OK_TOKEN = "#[OK]#";

    /** The Constant ko token. */
    private static final String KO_TOKEN = "#[KO]#";

    /** The current status. */
    private String currentStatus = "INIT";

    /** The current directory. */
    private String currentWorkingDirectory = null;

    /** The current setup. */
    private ECtransSetup currentSetup = null;

    /** The session. */
    private InteractiveSession session = null;

    /** The keep alive. */
    private long keepAlive = 0;

    /** The key. */
    private String key = null;

    /** The usetmp. */
    private boolean usetmp = false;

    /** The mkdirs. */
    private boolean mkdirs = false;

    /** The use noop. */
    private long useNoop = 0;

    /** The success. */
    private boolean success = true;

    /** The prefix. */
    private String prefix = null;

    /** The suffix. */
    private String suffix = null;

    /** The ignore check. */
    private boolean ignoreCheck = true;

    /** The use mget. */
    private boolean usemget = false;

    /** The ignoreMkdirsCmdError. */
    private boolean ignoreMkdirsCmdError = false;

    /** The mkdirs cmd index. */
    private int mkdirsCmdIndex = 0;

    /** The pre mkdirs cmd. */
    private String preMkdirsCmd = null;

    /** The post mkdirs cmd. */
    private String postMkdirsCmd = null;

    /** The exec cmd. */
    private String execCmd = null;

    /** The exec code. */
    private int execCode = 0;

    /** The hostname. */
    private String hostname = null;

    /** The username. */
    private String username = null;

    /**
     * Gets the result.
     *
     * @param command
     *            the command
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String getResult(final String command) throws IOException {
        // Do we have a valid session?
        if (session == null) {
            throw new IOException("Module not initialized");
        }
        if (getDebug()) {
            _log.debug("Sending to {}: {}", hostname, command);
        }
        var isSuccess = false;
        try {
            session.send(command);
            final var res = session.waitfor(OK_TOKEN, KO_TOKEN);
            if (getDebug()) {
                _log.debug("Receiving from {}: {}", hostname, res);
            }
            final boolean ok;
            if (res == null || !(ok = res.endsWith(OK_TOKEN)) && !res.endsWith(KO_TOKEN)) {
                _log.warn("Received from {}: {}", hostname, res != null ? res : "(empty)");
                throw new IOException("Failed to process " + command + " on " + hostname);
            }
            final var message = res.substring(0, res.length() - (ok ? OK_TOKEN.length() : KO_TOKEN.length()));
            if (!ok) {
                throw new IOException("failed on " + hostname + " - " + message.replaceAll("[\n\r]", " ").trim());
            }
            isSuccess = true;
            return message;
        } finally {
            success = isSuccess;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort(final ECtransSetup setup) throws IOException {
        // Is it ssh/22 or telnet/23?
        return setup.get(HOST_ECAUTH_PORT,
                "ssh".equalsIgnoreCase(setup.get(HOST_ECAUTH_PROTOCOL, Cnf.at("ECauthModule", "protocol", "ssh"))) ? 22
                        : 23);
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     */
    @Override
    public void connect(final String location, final ECtransSetup setup)
            throws IOException, ConnectionException, EccmdException, JSchException {
        currentSetup = setup;
        setStatus("CONNECT");
        final var msuser = getMSUser();
        execCmd = currentSetup.getString(HOST_ECAUTH_EXEC_CMD);
        execCode = currentSetup.getInteger(HOST_ECAUTH_EXEC_CODE);
        ignoreMkdirsCmdError = currentSetup.getBoolean(HOST_ECAUTH_IGNORE_MKDIRS_CMD_ERRORS);
        mkdirsCmdIndex = currentSetup.getInteger(HOST_ECAUTH_MKDIRS_CMD_INDEX);
        preMkdirsCmd = currentSetup.getString(HOST_ECAUTH_PRE_MKDIRS_CMD);
        postMkdirsCmd = currentSetup.getString(HOST_ECAUTH_POST_MKDIRS_CMD);
        keepAlive = currentSetup.getDuration(HOST_ECAUTH_KEEP_ALIVE).toMillis();
        useNoop = currentSetup.getDuration(HOST_ECAUTH_USE_NOOP).toMillis();
        ignoreCheck = currentSetup.getBoolean(HOST_ECAUTH_IGNORE_CHECK);
        usemget = currentSetup.getBoolean(HOST_ECAUTH_USEMGET);
        usetmp = currentSetup.getBoolean(HOST_ECAUTH_USETMP);
        mkdirs = currentSetup.getBoolean(HOST_ECAUTH_MKDIRS);
        prefix = currentSetup.getString(HOST_ECAUTH_PREFIX);
        suffix = currentSetup.getString(HOST_ECAUTH_SUFFIX);
        if (currentSetup.getBoolean(HOST_ECAUTH_MKSUFFIX)) {
            suffix = "." + new RandomString(3).next();
        } else if (prefix.length() == 0 && suffix.length() == 0) {
            suffix = ".tmp";
        }
        final var ecauthUser = currentSetup.get(HOST_ECAUTH_USER, Cnf.at("ECauthModule", "user", "ecauth"));
        final var ecauthPass = currentSetup.get(HOST_ECAUTH_PASS, Cnf.at("ECauthModule", "pass", "ecauth"));
        final var ecauthProtocol = currentSetup.get(HOST_ECAUTH_PROTOCOL, Cnf.at("ECauthModule", "protocol", "ssh"));
        final var telnet = "telnet".equalsIgnoreCase(ecauthProtocol);
        username = msuser.getLogin();
        var originalHost = msuser.getHost();
        if (currentSetup.getBoolean(HOST_ECAUTH_RESOLVE_IP)) {
            // We want to try to convert the original host name into an IP
            // address and then get the dns name again. We can do that in order
            // to resolve into the primary main dns name and not an alias!
            try {
                final var hostName = InetAddress.getByName(originalHost).getHostName();
                if (!IPAddress.isIPAddress(hostName)) {
                    // This is not an IP address so we assume it is a DNS
                    // name. Let's use it and also keep it in the cache just
                    // in case we couldn't resolve it in the near future!
                    IPcache.put(originalHost, hostName);
                    originalHost = hostName;
                } else {
                    // This is an IP address so we couldn't find the DNS
                    // name. May be in the cache?
                    final var fromCache = IPcache.get(originalHost);
                    if (fromCache != null) {
                        _log.info("Resolved IP address using cache: {} ({})", originalHost, fromCache);
                        originalHost = fromCache;
                    } else {
                        _log.warn("Couldn't resolve IP address for: {} ({})", originalHost, hostName);
                    }
                }
            } catch (final Throwable t) {
                _log.warn("Getting HostName for: {}", originalHost, t);
            }
        }
        hostname = getHostName(getDebug(), getRemoteProvider(), msuser.getName(), originalHost,
                currentSetup.getString(HOST_ECAUTH_HOST_LIST));
        currentWorkingDirectory = currentSetup.get(HOST_ECAUTH_CWD, msuser.getDir());
        if (isNotEmpty(currentWorkingDirectory)) {
            currentWorkingDirectory = currentWorkingDirectory + File.separator;
            _log.debug("Directory: {}", currentWorkingDirectory);
        } else {
            currentWorkingDirectory = "";
        }
        final var port = getPort(currentSetup);
        if (hostname == null) {
            throw new IOException("No host selected");
        }
        setAttribute("remote.hostName", hostname);
        _log.debug("Host selected: {}", hostname);
        final var sessionTimeOut = setup.getOptionalDuration(HOST_ECAUTH_SESSION_TIMEOUT).orElse(Duration.ZERO);
        key = SessionCache.getKey(hostname, port, username, ecauthProtocol, sessionTimeOut);
        if (keepAlive <= 0 || (session = sessionCache.remove(key)) == null || !_isConnected(session)) {
            if (telnet) {
                // This is a telnet session!
                final var socket = new SocketConfig("ECauthModule").getSocket(hostname, port);
                if (sessionTimeOut.isPositive()) {
                    _log.debug("SoTimeOut: {}", sessionTimeOut);
                    socket.setSoTimeout((int) sessionTimeOut.toMillis());
                }
                session = new TelnetSession(socket, ecauthUser, ecauthPass, mover.getECauthToken(username).getToken(),
                        getDebug());
            } else {
                // This is a ssh session!
                session = new SSHSession(currentSetup, hostname, port, (int) sessionTimeOut.toMillis(), ecauthUser,
                        ecauthPass, mover.getECauthToken(username).getToken(), getDebug());
            }
            _log.debug("Connected to {}:{} using {} (user: {})", hostname, port, ecauthProtocol, ecauthUser);
        } else {
            _log.debug("Found cached {} connection ({})", ecauthProtocol, key);
        }
    }

    /**
     * Check if the host name name is in the format digits, letters, '.' and '-'.
     *
     * @param host
     *            the host
     * @param extra
     *            additional characters allowed (e.g ':')
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void checkHostName(final String host, final String extra) throws IOException {
        for (final char c : host.toCharArray()) {
            if (!(Character.isDigit(c) || Character.isLetter(c) || c == '.' || c == '-' || extra.indexOf(c) != -1)) {
                throw new IOException("Invalid ecauth.hostList parameter (" + host + ")");
            }
        }
    }

    /**
     * Process the hostList provided in the Host configuration.
     *
     * @param debug
     *            the debug
     * @param provider
     *            the provider
     * @param currentHostName
     *            the current host name
     * @param defaultHostName
     *            the default host name
     * @param hostList
     *            the host list
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String getHostName(final boolean debug, final RemoteProvider provider, final String currentHostName,
            final String defaultHostName, final String hostList) throws IOException {
        final List<String> result = new ArrayList<>();
        if (debug) {
            _log.debug("Getting HostName(default: {}, hostList: [{}])", defaultHostName, hostList);
        }
        if (hostList != null && !hostList.isEmpty()) {
            var selectedHostList = Format.choose(defaultHostName, hostList);
            // Did we find an entry for this Host?
            if (selectedHostList != null && !selectedHostList.isEmpty()) {
                if (debug) {
                    _log.debug("SelectedHostList: {}", selectedHostList);
                }
                if (selectedHostList.startsWith("[")) {
                    final var index = selectedHostList.indexOf("]");
                    if (index > 0) {
                        // This is the first entry and it contains a link to a remote server with a
                        // fileName to find the list of nodes available!
                        final var params = selectedHostList.substring(1, index).split(":");
                        selectedHostList = selectedHostList.substring(index + 1).trim();
                        final var hostName = params[0];
                        final var fileName = params[1];
                        final var lifeTime = Cnf.durationAt("ECauthModule", "hostListManagerLifeTime", Timer.ONE_HOUR);
                        final var frequency = Cnf.durationAt("ECauthModule", "hostListManagerFrequency",
                                params.length > 2 ? Format.getDuration(params[2]) : 5 * Timer.ONE_MINUTE);
                        // Check if the host name is valid (no port can be defined here)
                        checkHostName(hostName, "");
                        // Add the list found on the remote Host if we have a valid current host name!
                        if (isNotEmpty(currentHostName)) {
                            _log.debug("Loading host selection from: {}", currentHostName);
                            result.addAll(manager.getHostListFor(provider, currentHostName, hostName, fileName,
                                    lifeTime, frequency));
                        } else {
                            _log.warn("No valid current hostname specified");
                        }
                        // Did we get something?
                        if (debug) {
                            _log.debug("SelectedHostList: retrieved from {}:{}({} elements)", hostName, hostName,
                                    result.size());
                        }
                    }
                }
                // If no results then we get the default list provided!
                if (result.isEmpty()) {
                    result.addAll(getNodeList(selectedHostList));
                }
            } else {
                if (debug) {
                    _log.debug("No SelectedHostList found");
                }
            }
        } else if (debug) {
            _log.debug("No HostList found");
        }
        if (result.isEmpty()) {
            // Nothing found so let's return the initial host!
            if (debug) {
                _log.debug("Use default host: {}", defaultHostName);
            }
            return defaultHostName;
        }
        // Select randomly in the list of hosts available!
        final var selected = result.get(ThreadLocalRandom.current().nextInt(result.size()));
        if (debug) {
            _log.debug("Random selection over {} element(s): {}", result.size(), selected);
        }
        return selected;
    }

    /**
     * Gets the translated data channel. Allow redirecting the data channel to a proxy if required!
     *
     * @param socket
     *            the socket
     *
     * @return the translated data channel
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String getTranslatedDataChannel(final ProxySocket socket) throws IOException {
        final var dataHost = getHostName(getDebug(), getRemoteProvider(), getMSUser().getName(),
                socket.getDataHost() + ":" + socket.getDataPort(), currentSetup.getString(HOST_ECAUTH_PROXY_LIST));
        final var hostAndPort = dataHost.split(":");
        if (hostAndPort.length != 2) {
            throw new IOException("Bad format for proxy host (hostname:port): " + dataHost);
        }
        return socket.getTicket() + "|" + hostAndPort[0] + "|" + hostAndPort[1];
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public void get(final String name, final long posn, final ProxySocket socket) throws Exception {
        if (usemget) {
            if (posn != 0) {
                throw new IOException("No posn!=0 allowed with mget option");
            }
            setStatus("MGET");
            getResult(currentWorkingDirectory + name + "|" + getTranslatedDataChannel(socket) + "|mget");
        } else {
            setStatus("GET");
            getResult(currentWorkingDirectory + name + "|" + posn + "|" + getTranslatedDataChannel(socket) + "|get");
        }
        setAttribute("remote.fileName", name);
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public void put(final String name, final long posn, final long size, final ProxySocket socket) throws Exception {
        try {
            putOrCopy(null, name, posn, size, socket);
        } catch (final Exception e) {
            _log.warn("Processing put to {}", name, e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Copy.
     */
    @Override
    public void copy(final String source, final String target, final long posn, final long size) throws IOException {
        try {
            putOrCopy(source, target, posn, size, null);
        } catch (final Throwable t) {
            _log.warn("Processing copy from {} to {}", source, target, t);
            throw new IOException(t);
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
                Format.replaceAll(sb, "$uid", username);
                Format.replaceAll(sb, "$dirname", path);
                exec(sb.toString());
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
     * Put or copy.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     * @param posn
     *            the posn
     * @param size
     *            the size
     * @param socket
     *            the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void putOrCopy(final String source, final String target, final long posn, final long size,
            final ProxySocket socket) throws IOException {
        setStatus(socket == null ? "COPY" : "PUT");
        final var dir = new File(target).getParent();
        if (dir != null && mkdirs) {
            // We are requested to make the directories for the target file!
            final var token = new StringTokenizer(dir, "\\/");
            var path = dir.startsWith("\\") || dir.startsWith("/") ? File.separator : "";
            var index = 0;
            final var length = token.countTokens();
            while (token.hasMoreElements()) { // Let's go through every sub-directories!
                final var currentDir = token.nextToken();
                path += currentDir + File.separator;
                index++;
                if (isNotEmpty(preMkdirsCmd)
                        && (mkdirsCmdIndex >= index || mkdirsCmdIndex < 0 && length + mkdirsCmdIndex < index)) {
                    execMkdirsCmd("pre", preMkdirsCmd, path);
                }
                try {
                    getResult(currentWorkingDirectory + path + "|mkdir");
                } catch (final SocketTimeoutException e) {
                    throw new SocketTimeoutException("Timeout on MKDIR");
                } catch (final IOException e) {
                    // Ignored, maybe the directory already exists?
                }
                if (isNotEmpty(postMkdirsCmd)
                        && (mkdirsCmdIndex >= index || mkdirsCmdIndex < 0 && length + mkdirsCmdIndex < index)) {
                    execMkdirsCmd("post", postMkdirsCmd, path);
                }
            }
        }
        long remoteSize = -1;
        if (usetmp) {
            // We have to use a temporary file, then rename it to the target name once
            // successfully transmitted!
            if (posn > 0) {
                throw new IOException("Append/resume not compatible with " + currentSetup.getModuleName() + ".usetmp");
            }
            // Let's create the temporary name
            final var tempName = getName(target);
            // What type of put is it?
            if (socket != null) {
                // This is a push through a socket!
                var result = getResult(currentWorkingDirectory + tempName + "|" + currentWorkingDirectory + target + "|"
                        + posn + "|" + size + "|" + getTranslatedDataChannel(socket) + "|put/mv/size");
                if (!ignoreCheck) {
                    var index = result.indexOf("size=");
                    if (index != -1) {
                        result = result.substring(index + 5);
                        index = result.indexOf(")");
                        if (index != -1) {
                            try {
                                remoteSize = Long.parseLong(result.substring(0, index));
                            } catch (final Exception e) {
                            }
                        }
                    }
                }
            } else {
                // This is a remote copy, so we do the copy to the temporary name!
                final var copyCmd = currentSetup.getString(HOST_ECAUTH_COPY_CMD);
                if (isNotEmpty(copyCmd)) { // e.g. "sudo ecpds-cp $uid $source $temp $target 600"
                    try {
                        final var db = new StringBuilder(copyCmd);
                        Format.replaceAll(db, "$uid", username);
                        Format.replaceAll(db, "$source", source);
                        Format.replaceAll(db, "$temp", currentWorkingDirectory + tempName);
                        Format.replaceAll(db, "$target", currentWorkingDirectory + target);
                        exec(db.toString());
                    } catch (final SocketTimeoutException e) {
                        throw new SocketTimeoutException("Timeout on COPY");
                    }
                } else {
                    getResult(source + "|" + currentWorkingDirectory + tempName + "|copy");
                    // We rename it!
                    getResult(currentWorkingDirectory + tempName + "|" + currentWorkingDirectory + target + "|move");
                    // And set the chmod!
                    getResult(currentWorkingDirectory + target + "|" + currentSetup.getString(HOST_ECAUTH_CHMOD_ON_COPY)
                            + "|chmod");
                }
            }
        } else if (socket != null) {
            // This is a push through a socket!
            getResult(currentWorkingDirectory + target + "|" + posn + "|" + size + "|"
                    + getTranslatedDataChannel(socket) + "|put");
        } else {
            // This is a remote copy!
            if (posn > 0) {
                throw new IOException("Append/resume not compatible with ectrans.putHandler");
            }
            final var copyCmd = currentSetup.getString(HOST_ECAUTH_COPY_CMD);
            if (isNotEmpty(copyCmd)) { // command = "sudo ecpds-cp $uid $source $temp $target 600"
                try {
                    final var db = new StringBuilder(copyCmd);
                    Format.replaceAll(db, "$uid", username);
                    Format.replaceAll(db, "$source", source);
                    Format.replaceAll(db, "$temp", currentWorkingDirectory + target);
                    Format.replaceAll(db, "$target", currentWorkingDirectory + target);
                    exec(db.toString());
                } catch (final SocketTimeoutException e) {
                    throw new SocketTimeoutException("Timeout on COPY");
                }
            } else {
                getResult(source + "|" + currentWorkingDirectory + target + "|copy");
            }
        }
        // Let's check the remote size if required!
        if (!ignoreCheck) {
            if (remoteSize == -1) {
                remoteSize = Long.parseLong(getResult(currentWorkingDirectory + target + "|size"));
            }
            if (remoteSize != size + posn) {
                throw new IOException("Remote file size is " + Format.formatPercentage(remoteSize, size + posn)
                        + " of original file size (sent=" + remoteSize + "/size=" + (size + posn) + ")");
            }
        }
        if (isNotEmpty(execCmd)) {
            // Let's now execute the command as requested!
            final String command;
            try {
                final var sb = new StringBuilder(execCmd);
                Format.replaceAll(sb, "$filepath", currentWorkingDirectory + target);
                Format.replaceAll(sb, "$filename", new File(target).getName());
                command = Format.choose(sb.toString());
            } catch (final DuplicatedChooseScore e) {
                throw new IOException("Could not compute exec command (multiple choices selected)");
            } catch (final ScriptException e) {
                throw new IOException("Could not compute exec command (" + e.getMessage() + ")");
            }
            if (isNotEmpty(command)) {
                exec(command);
            }
        }
        setAttribute("remote.fileName", target);
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final String name) throws IOException {
        setStatus("DEL");
        getResult(currentWorkingDirectory + name + "|del");
    }

    /**
     * {@inheritDoc}
     *
     * Mkdir.
     */
    @Override
    public void mkdir(final String dir) throws IOException {
        setStatus("MKDIR");
        getResult(currentWorkingDirectory + dir + "|mkdir");
    }

    /**
     * {@inheritDoc}
     *
     * Rmdir.
     */
    @Override
    public void rmdir(final String dir) throws IOException {
        setStatus("RMDIR");
        getResult(currentWorkingDirectory + dir + "|rmdir");
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String name) throws IOException {
        setStatus("SIZE");
        return Long.parseLong(getResult(currentWorkingDirectory + name + "|size"));
    }

    /**
     * {@inheritDoc}
     *
     * Move.
     */
    @Override
    public void move(final String source, final String target) throws IOException {
        setStatus("MOVE");
        getResult(currentWorkingDirectory + source + "|" + target + "|move");
    }

    /**
     * {@inheritDoc}
     *
     * List as string array.
     */
    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        setStatus("LIST");
        final var token = new StringTokenizer(
                getResult(currentWorkingDirectory + (directory != null ? directory : "") + "|list"), "\r\n");
        final List<String> result = new ArrayList<>();
        var i = 0;
        while (token.hasMoreElements()) {
            final var line = token.nextToken();
            if (pattern == null || line.matches(pattern)) {
                if (getDebug()) {
                    _log.debug("List[" + i++ + "] " + line);
                }
                result.add(line);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Exec.
     *
     * @param command
     *            the command
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void exec(final String command) throws IOException {
        var result = getResult(command + "|exec");
        final int index;
        if (!result.endsWith("]") || ((index = result.lastIndexOf("[exit-code=")) == -1)) {
            throw new IOException("Unexpected response from exec command");
        }
        final int exitCode;
        var code = "";
        try {
            code = result.substring(index + 11, result.length() - 1);
            result = result.substring(0, index);
            exitCode = Integer.parseInt(code);
        } catch (final NumberFormatException e) {
            throw new IOException(
                    "Could not parse exit-code for exec command" + (code.length() > 0 ? " (" + code + ")" : ""));
        }
        if (exitCode != execCode) {
            throw new IOException("Unexpected return code for '" + command + "' (" + execCode + "!=" + exitCode + "): "
                    + result.replace('\n', ' ').trim());
        }
    }

    /**
     * {@inheritDoc}
     *
     * Removes the.
     */
    @Override
    public void remove(final boolean closedOnError) throws IOException {
        setStatus("CLOSE");
        currentSetup = null;
        if (session != null) {
            if (!closedOnError && keepAlive > 0 && success && key != null && _isConnected(session)) {
                sessionCache.put(key, session, keepAlive, useNoop);
            } else {
                close(session);
            }
        }
    }

    /**
     * _close.
     *
     * @param session
     *            the session
     */
    private static void close(final InteractiveSession session) {
        final var debug = session.getDebug();
        if (debug) {
            _log.debug("Sending '.'");
        }
        try {
            session.send(".");
        } catch (final Throwable e) {
            _log.warn("Closing session", e);
        }
        if (debug) {
            _log.debug("Disconnecting");
        }
        try {
            session.disconnect();
        } catch (final Throwable e) {
            _log.warn("Disconnecting", e);
        }
        if (debug) {
            _log.debug("Disconnected");
        }
    }

    /**
     * Checks if is connected.
     *
     * @param session
     *            the session
     *
     * @return true, if successful
     */
    private static boolean _isConnected(final InteractiveSession session) {
        return session.isConnected();
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
        if (currentSetup == null) {
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
     * The Class InteractiveSessionCache.
     */
    private static final class InteractiveSessionCache extends SessionCache<String, InteractiveSession> {

        /**
         * Disconnect.
         *
         * @param session
         *            the session
         */
        @Override
        public void disconnect(final InteractiveSession session) {
            close(session);
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
        public boolean isConnected(final InteractiveSession session) {
            return _isConnected(session);
        }

        /**
         * Update.
         *
         * @param session
         *            the session
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void update(final InteractiveSession session) throws IOException {
            isConnected(session);
        }
    }

    /**
     * The Class HostListManager.
     *
     * This class is used to manage the list of nodes retrieved from the target servers. The format of the host list
     * parameter (ecauth.hostList) is the following (e.g.):
     *
     * <li>(.=cca) ccadtn1,ccadtn2,ccadtn3,ccadtn4,ccadtn5,ccadtn6,ccadtn7,ccadtn8
     * <li>(.=lxa) lxa01,lxa02,lxa03,lxa04,lxa05,lxa06,lxa07,lxa08,lxa09,lxa10,lxa11,lxa12, lxa13,lxa14,lxa15
     * <li>(.=lxb) lxb01,lxb02,lxb03,lxb04,lxb05,lxb06,lxb07,lxb08,lxb09,lxb10,lxb11,lxb12, lxb13,lxb14,lxb15
     * <li>(.=c2a) [c2a-batch:/loadl/LOCAL/q2diss_servers:2m]
     * c2a184,c2a185,c2a187,c2a384,c2a385,c2a386,c2a387,c2a683,c2a685,c2a686, c2a687
     * <li>(.=c2b) c2b184,c2b185,c2b186,c2b187,c2b384,c2b385,c2b386,c2b387,c2b482,c2b483,
     * c2b484,c2b485,c2b486,c2b487,c2b683,c2b684,c2b685,c2b686
     * <li>(.=c1a) c1a0801,c1a0901,c1a1001,c1a1101,c1a1201,c1a1301,c1a1401
     * <li>(.=c1b) c1b0801,c1b0901,c1b1001,c1b1101,c1b1201,c1b1301,c1b1401
     *
     * You can specify a list of alternative nodes for the retrieval but on top of that, if the first node in the list
     * is in the format [hostName:fileName:frequency] then the list of alternative nodes will be retrieved from the file
     * fileName on the host hostName (and the list will be updated using the frequency specified).
     *
     * e.g. [c2a-batch:/loadl/LOCAL/q2diss_servers:2m]
     *
     * Will load the list of nodes from the q2diss_servers file on c2a-batch every 2 minutes.
     *
     * The format of the q2diss_servers file is the following:
     *
     * nodename:{on|off|unix_timestamp!duration_in_seconds}
     *
     * The timestamp (and optionally duration) can be specified to de-activate the node just for a period of time.
     *
     * If for some reason the list of nodes can not be retrieved then the nodes specified on the rest of the lines will
     * be used.
     */
    private static final class HostListManager extends ConfigurableLoopRunnable {
        /** The key is in the format: 'hostName:fileName'. */
        private final Map<String, HostEntry> hosts = new ConcurrentHashMap<>();

        /** To synchronize the loading of the file. */
        private final Synchronized loadingMutex = new Synchronized();

        /**
         * Builds the key.
         *
         * @param hostName
         *            the host name
         * @param fileName
         *            the file name
         *
         * @return the string
         */
        static String buildKey(final String hostName, final String fileName) {
            return hostName + ":" + fileName;
        }

        /**
         * The Class HostEntry. Used to keep the creation time of this Entry.
         */
        private final class HostEntry {

            /**
             * Instantiates a new host entry.
             *
             * @param host
             *            the host
             * @param fileName
             *            the file name
             * @param lifeTime
             *            the life time
             * @param frequency
             *            the frequency
             */
            HostEntry(final Host host, final String fileName, final long lifeTime, final long frequency) {
                final var currentTime = System.currentTimeMillis();
                this.host = host;
                this.fileName = fileName;
                this.lastUpdate = currentTime;
                this.lastUsed = currentTime;
                this.lifeTime = lifeTime;
                this.frequency = frequency;
                this.nodes.putAll(loadHostListFor(this));
            }

            /** The Host object used to retrieve the list. */
            private Host host;

            /** Last used. */
            private long lastUsed;

            /** Last update. */
            private long lastUpdate;

            /** Filename on the Host. */
            private final String fileName;

            /** Idle lifetime. */
            private final long lifeTime;

            /** Frequency for the updates. */
            private final long frequency;

            /** The key is in the format: 'nodeName'. */
            private final Map<String, NodeEntry> nodes = new HashMap<>();

            /**
             * Gets the key.
             *
             * @return the key
             */
            String getKey() {
                return buildKey(host.getHost(), fileName);
            }

            /**
             * Get the list of node entries.
             *
             * @return the node entries
             */
            NodeEntry[] getNodeEntries() {
                synchronized (nodes) {
                    return nodes.values().toArray(new NodeEntry[0]);
                }
            }

            /**
             * Put all entries in provided list.
             *
             * @param result
             *            the result
             */
            void putAll(final Map<String, NodeEntry> result) {
                synchronized (nodes) {
                    result.putAll(nodes);
                }
            }

            /**
             * Update the list of nodes and the last update time.
             */
            void update() {
                synchronized (nodes) {
                    nodes.clear();
                    nodes.putAll(loadHostListFor(this));
                    lastUpdate = System.currentTimeMillis();
                }
            }

            /**
             * Check if this entry should not be updated. Is it still valid?
             *
             * @return true, if it should be updated
             */
            boolean toUpdate() {
                return System.currentTimeMillis() - lastUpdate > frequency;
            }

            /**
             * Checks if is expired.
             *
             * @return true, if is expired
             */
            boolean isExpired() {
                return System.currentTimeMillis() - lastUsed > lifeTime;
            }
        }

        /**
         * The Class NodeEntry. Used to keep the timestamp and duration of the Node.
         */
        private static final class NodeEntry {

            /**
             * Instantiates a new node entry.
             *
             * @param nodeName
             *            the node name
             * @param timestamp
             *            the timestamp
             * @param duration
             *            the duration
             */
            NodeEntry(final String nodeName, final long timestamp, final long duration) {
                this.nodeName = nodeName;
                this.timestamp = timestamp;
                this.duration = duration;
            }

            /** The node name. **/
            private final String nodeName;

            /** It is off(-1), on(0) or unavailable at timestamp?. */
            private final long timestamp; //

            /** Duration of the unavailability if a timestamp is specified. */
            private final long duration;
        }

        /**
         * Instantiates a new host list manager. Configure and start the looping Thread.
         */
        HostListManager() {
            _log.debug("Starting HostListManager");
            setInheritCookie(false);
            setThreadNameAndCookie(null, null, null, null);
            setPause(Cnf.durationAt("ECauthModule", "hostListManagerDelay", 10000));
            setPriority(Thread.MIN_PRIORITY);
            execute();
        }

        /**
         * Look for the host list. For each node we find we must check its validity (according to the configuration).
         *
         * @param provider
         *            the provider
         * @param currentHostName
         *            the current host name
         * @param hostName
         *            the host name
         * @param fileName
         *            the file name
         * @param lifeTime
         *            the life time
         * @param frequency
         *            the frequency
         *
         * @return the host list for
         */
        List<String> getHostListFor(final RemoteProvider provider, final String currentHostName, final String hostName,
                final String fileName, final long lifeTime, final long frequency) {
            final List<String> result = new ArrayList<>();
            final var key = buildKey(hostName, fileName);
            final var nodeList = new StringBuilder();
            try {
                // Configure the Host!
                final var host = (Host) provider.getObject(currentHostName);
                host.setName(null); // Prevent it to be updated on the
                                    // master!
                host.setHost(hostName);
                // The host directory should be empty and the file name for
                // the loading of the host should always be absolute!
                host.setDir("");
                // Remove the hostList parameter to avoid going through this
                // when retrieving the node list!
                final var setup = HOST_ECAUTH.getECtransSetup(host.getData());
                // The node list file will never be an index file so let's make
                // sure the "usemget" parameter is set to no!
                setup.set(HOST_ECAUTH_USEMGET, false);
                setup.remove(HOST_ECAUTH_HOST_LIST);
                host.setData(setup.getData());
                HostEntry hostEntry;
                // We have to synchronize on the key otherwise we might have 2
                // concurrent transfers trying to retrieve the list at the same
                // time!
                final var mutex = loadingMutex.getMutex(key);
                synchronized (mutex.lock()) {
                    try {
                        // Do we have it in the cache?
                        hostEntry = hosts.get(key);
                        if (hostEntry != null) {
                            // If we found it in the cache then let's use it!
                            _log.debug("Host list found in cache for: {}", key);
                            // And update the Host for the next reload. The Host
                            // might have been updated since the last node list
                            // retrieval!
                            hostEntry.host = host;
                            // Let's also update the last used time to delay the
                            // expiration!
                            hostEntry.lastUsed = System.currentTimeMillis();
                        } else {
                            // Try to get the list now and add it to the cache!
                            _log.debug("Getting Host list from: {}", key);
                            hosts.put(key, hostEntry = new HostEntry(host, fileName, lifeTime, frequency));
                        }
                    } finally {
                        mutex.free();
                    }
                    // Let's go through the list of nodes
                    for (final NodeEntry entry : hostEntry.getNodeEntries()) {
                        final var timestamp = entry.timestamp;
                        if (timestamp == 0) {
                            // The node is active!
                            nodeList.append(nodeList.length() == 0 ? "" : ", ").append(entry.nodeName);
                            result.addAll(getNodeList(entry.nodeName));
                        } else if (timestamp > 0) {
                            // We are using a unix timestamp in the
                            // configuration file
                            final var current = System.currentTimeMillis() / 1000L;
                            // Check if we are not in the session!
                            if (current < timestamp && current > timestamp + entry.duration) {
                                // The node is active;
                                nodeList.append(nodeList.length() == 0 ? "" : ", ").append(entry.nodeName);
                                result.addAll(getNodeList(entry.nodeName));
                            }
                        }
                    }
                }
            } catch (final Throwable t) {
                _log.warn("Loading node list for {}", key, t);
            }
            // Return the list of nodes available!
            _log.debug("Node available list ({}): {} -> {}", key, nodeList, result);
            return result;
        }

        /**
         * Adds the node entry.
         *
         * @param nodeEntry
         *            the node entry
         * @param result
         *            the result
         */
        void addNodeEntry(final NodeEntry nodeEntry, final Map<String, NodeEntry> result) {
            for (final String nodeName : getNodeList(nodeEntry.nodeName)) {
                result.put(nodeName, nodeEntry);
            }
        }

        /**
         * Load a list of nodes from a source host name and update the cache.
         *
         * @param entry
         *            the entry
         *
         * @return the hashtable
         */
        Map<String, NodeEntry> loadHostListFor(final HostEntry entry) {
            final Map<String, NodeEntry> result = new ConcurrentHashMap<>();
            BufferedReader reader = null;
            final var key = buildKey(entry.host.getHost(), entry.fileName);
            _log.debug("Loading/updating node list for: {}", key);
            final var nodeList = new StringBuilder();
            var exception = false;
            ECproxyCallback callback = null;
            PipedOutputStream out = null;
            PipedInputStream in = null;
            try {
                out = new PipedOutputStream();
                in = new PipedInputStream(out, StreamPlugThread.DEFAULT_BUFF_SIZE);
                reader = new BufferedReader(new InputStreamReader(in));
                callback = mover.get(out, entry.host, entry.fileName, 0, null);
                try {
                    String line;
                    // Read the file in the format:
                    // nodename:{on|off|unix_timestamp!duration_in_seconds}
                    while ((line = reader.readLine()) != null) {
                        try {
                            line = line.trim();
                            // The end of the line might include a comment?
                            var index = line.indexOf("#");
                            if (index != -1) {
                                line = line.substring(0, index).trim();
                            }
                            // Skip the empty lines!
                            if (line.length() == 0) {
                                continue;
                            }
                            index = line.indexOf(":");
                            // Bad format!
                            if (index < 1) {
                                // Simple format with default to "on"
                                final var nodeName = line;
                                nodeList.append(nodeList.length() == 0 ? "" : ", ").append(nodeName).append("(on)");
                                addNodeEntry(new NodeEntry(nodeName, 0, 0), result);
                            } else {
                                // Complex format with options!
                                final var nodeName = line.substring(0, index);
                                // Let's process the:
                                // on|off|unix_timestamp!duration_in_seconds
                                line = line.substring(index + 1).toLowerCase();
                                if ("off".equals(line)) {
                                    // The node if off!
                                    nodeList.append(nodeList.length() == 0 ? "" : ", ").append(nodeName)
                                            .append("(off)");
                                    addNodeEntry(new NodeEntry(nodeName, -1, 0), result);
                                } else if ("on".equals(line)) {
                                    // The node is on!
                                    nodeList.append(nodeList.length() == 0 ? "" : ", ").append(nodeName).append("(on)");
                                    addNodeEntry(new NodeEntry(nodeName, 0, 0), result);
                                } else {
                                    // This is a timestamp!
                                    index = line.indexOf("!");
                                    if (index < 1) {
                                        // No duration specified!
                                        final var timestamp = Long.parseLong(line);
                                        nodeList.append(nodeList.length() == 0 ? "" : ", ").append(nodeName).append("(")
                                                .append(Format.formatTime(timestamp * 1000L)).append(")");
                                        addNodeEntry(new NodeEntry(nodeName, timestamp, 0), result);
                                    } else {
                                        // A duration is specified!
                                        final var timestamp = Long.parseLong(line.substring(0, index));
                                        final var duration = Long.parseLong(line.substring(index + 1));
                                        nodeList.append(nodeList.length() == 0 ? "" : ", ").append(nodeName).append("(")
                                                .append(Format.formatTime(timestamp * 1000L)).append(" => ")
                                                .append(Format.formatDuration(duration)).append(")");
                                        addNodeEntry(new NodeEntry(nodeName, timestamp, duration), result);
                                    }
                                }
                            }
                        } catch (final Throwable t) {
                            _log.warn("Loading {} ({})", key, line, t);
                            exception = true;
                        }
                    }
                } finally {
                    // Let's check if the target host didn't report any problem
                    // during the upload?
                    callback.check();
                }
            } catch (final Throwable t) {
                _log.warn("Loading {} ({} entries)", key, result.size(), t);
                exception = true;
            } finally {
                StreamPlugThread.closeQuietly(reader);
                StreamPlugThread.closeQuietly(in);
                StreamPlugThread.closeQuietly(out);
            }
            // Did we get something?
            if (exception && result.isEmpty()) {
                // There was an error and we got nothing!
                final var hostEntry = hosts.get(key);
                if (hostEntry != null) {
                    // There is already something in the cache so let's reuse it
                    _log.warn("Reusing previous list ({})", key);
                    hostEntry.putAll(result);
                }
            }
            // Return the list of nodes if required!
            _log.debug("Node list retrieved ({}): {}", key, nodeList);
            return result;
        }

        /**
         * Configurable loop run.
         */
        /*
         * Let's update the cache if some nodes have expired.
         *
         * @see ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable# configurableLoopRun()
         */
        @Override
        public void configurableLoopRun() {
            try {
                for (final HostEntry hostEntry : hosts.values().toArray(new HostEntry[0])) {
                    final var key = hostEntry.getKey();
                    if (hostEntry.isExpired()) {
                        // This Host entry is expired and should be removed from
                        // the list!
                        _log.debug("Removing expired HostEntry: {}", key);
                        hosts.remove(key);
                    } else if (hostEntry.toUpdate()) {
                        // The list of nodes in this Host entry should be
                        // updated!
                        _log.debug("Updating HostEntry: {}", key);
                        hostEntry.update();
                    }
                }
            } catch (final Throwable t) {
                _log.warn(t);
            }
        }

    }

    /**
     * Gets the node list.
     *
     * @param nodeName
     *            the node name
     *
     * @return the node list
     */
    private static List<String> getNodeList(final String nodeName) {
        // The node name might be in the form
        // "aa6-[001-2,0003-4,012]test[151-152].my.domain,bb8-[104-149,151-159].fr"
        // So we have to see if we need to expand?
        final List<String> result = new ArrayList<>();
        for (final String node : expand(nodeName).split(",")) {
            result.addAll(reduce(process(node)));
        }
        return result;
    }

    /**
     * Reduce.
     *
     * @param list
     *            the list
     *
     * @return the list
     */
    private static List<String> reduce(final List<List<String>> list) {
        while (list.size() > 1) {
            final List<String> result = new ArrayList<>();
            for (final String line1 : list.get(0)) {
                for (final String line2 : list.get(1)) {
                    result.add(line1 + line2);
                }
            }
            list.add(0, result);
            list.remove(1);
            list.remove(1);
        }
        return list.get(0);
    }

    /**
     * Checks if is numeric.
     *
     * @param str
     *            the str
     *
     * @return true, if is numeric
     */
    private static boolean isNumeric(final String str) {
        return str.matches("[+-]?\\d*(\\.\\d+)?");
    }

    /**
     * Process.
     *
     * @param node
     *            the node
     *
     * @return the list
     */
    private static List<List<String>> process(final String node) {
        final List<List<String>> list = new ArrayList<>();
        for (final String s1 : node.split("((?<=\\[)|(?=\\]))")) {
            if (s1.startsWith("]") && s1.endsWith("[")) {
                final List<String> l = new ArrayList<>();
                l.add(s1.substring(1, s1.length() - 1));
                list.add(l);
            } else if (s1.startsWith("]")) {
                final List<String> l = new ArrayList<>();
                l.add(s1.substring(1));
                list.add(l);
            } else if (s1.endsWith("[")) {
                final List<String> l = new ArrayList<>();
                l.add(s1.substring(0, s1.length() - 1));
                list.add(l);
            } else {
                final List<String> l = new ArrayList<>();
                for (final String s2 : s1.split(";")) {
                    final var s3 = s2.split("-");
                    if (s3.length == 2 && isNumeric(s3[0]) && isNumeric(s3[1])) {
                        final var start = Integer.parseInt(s3[0]);
                        final var stop = Integer.parseInt(s3[1]);
                        for (var i = start; i <= stop; i++) {
                            l.add(String.valueOf(i));
                        }
                    } else {
                        l.add(s2);
                    }
                }
                list.add(l);
            }
        }
        return list;
    }

    /**
     * Expands aa6-[001-2,0003-4,012]test[151-152].my.domain,bb8-[104-149,151-159].fr into
     * aa6-[001;002;0003;0004;012;]test[151;152;].my.domain,bb8 (...)
     *
     * @param nodeList
     *            the node list
     *
     * @return the string
     */
    private static String expand(final String nodeList) {
        final var sb = new StringBuilder();
        for (final String s1 : nodeList.split("((?<=\\[)|(?=\\]))")) {
            if (s1.startsWith("]") && s1.endsWith("[")) {
                sb.append(s1.substring(1, s1.length() - 1));
            } else if (s1.startsWith("]")) {
                sb.append(s1.substring(1));
            } else if (s1.endsWith("[")) {
                sb.append(s1.substring(0, s1.length() - 1));
            } else {
                // This is a range list!
                sb.append("[");
                for (final String s2 : s1.split(",")) {
                    final var s3 = s2.split("-");
                    if (s3.length == 2 && isNumeric(s3[0]) && isNumeric(s3[1])) {
                        final var width = s3[0].length();
                        final var start = Integer.parseInt(s3[0]);
                        final var stop = Integer.parseInt(s3[1]);
                        for (var i = start; i <= stop; i++) {
                            sb.append(Format.formatValue(i, width)).append(";");
                        }
                    } else {
                        sb.append(s2).append(";");
                    }
                }
                sb.append("]");
            }
        }
        return sb.toString();
    }
}
