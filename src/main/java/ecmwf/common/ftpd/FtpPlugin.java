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

package ecmwf.common.ftpd;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.UserSession;

import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.plugin.ServerPlugin;
import ecmwf.common.security.LoginManagement;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.common.version.Version;

/**
 * The Class FtpPlugin.
 */
public final class FtpPlugin extends ServerPlugin {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(FtpPlugin.class);

    /** The Constant _NAME. */
    private static final String _NAME = "FtpPlugin";

    /** The Constant _VERSION. */
    private static final String _VERSION = Version.getFullVersion();

    /** The _pass. */
    private PASS _pass = null;

    /**
     * Instantiates a new ftp plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     * @param socket
     *            the socket
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public FtpPlugin(final String ref, final Map<String, String> params, final Socket socket) throws IOException {
        super(ref, params, socket);
    }

    /**
     * Instantiates a new ftp plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     */
    public FtpPlugin(final String ref, final Map<String, String> params) {
        super(ref, params);
    }

    /**
     * {@inheritDoc}
     *
     * Get instance of current class.
     */
    @Override
    public ConfigurableRunnable newInstance(final String ref, final Map<String, String> params, final Socket socket)
            throws IOException {
        return new FtpPlugin(ref, params, socket);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the plugin name.
     */
    @Override
    public String getPluginName() {
        return _NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort() {
        return Integer.parseInt(Cnf.at("FtpPlugin", "port"));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the version.
     */
    @Override
    public String getVersion() {
        return _VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the info.
     */
    @Override
    public String getInfo() {
        final UserSession session;
        return _pass != null && _pass.isLogged() && (session = _pass.getUserSession()) != null ? session.getUser()
                : super.getInfo();
    }

    /**
     * {@inheritDoc}
     *
     * Caller back.
     */
    @Override
    public void callerBack(final boolean reset) {
        super.callerBack(reset);
        if (reset) {
            PASS.clearCache();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Refuse connection.
     */
    @Override
    public void refuseConnection(final Socket socket, final int connectionsCount) throws IOException {
        final var curCon = new CurrentContext(this);
        // current connection information.
        curCon.user = "";
        curCon.clientSocket = socket;
        curCon.remoteIP = getInetAddress();
        curCon.dataIP = curCon.remoteIP;
        curCon.localIP = socket.getLocalAddress();
        curCon.dataPort = socket.getPort() - 1;
        curCon.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        curCon.out = new PrintWriter(socket.getOutputStream(), true);
        curCon.respond(530,
                "The maximum number of " + getRef() + " connections have been reached (" + connectionsCount + ")");
    }

    /**
     * {@inheritDoc}
     *
     * Start connection.
     */
    @Override
    public void startConnection(final Socket socket) throws IOException {
        final var currentContext = new CurrentContext(this);
        final var startTime = System.currentTimeMillis();
        var loginRequest = 0;
        // current connection information.
        currentContext.user = "";
        currentContext.clientSocket = socket;
        currentContext.remoteIP = getInetAddress();
        currentContext.dataIP = currentContext.remoteIP;
        currentContext.localIP = socket.getLocalAddress();
        currentContext.dataPort = socket.getPort() - 1;
        currentContext.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        currentContext.out = new PrintWriter(socket.getOutputStream(), true);
        // If the plugin is not available or if the address is banned then don't
        // try the login process!
        if (!isAvailable()) {
            _log.debug("FTP service not available");
            currentContext.respond(530, "Sorry. FTP service currently unavailable");
            return;
        }
        final var hostAddress = currentContext.remoteIP.getHostAddress();
        if (!LoginManagement.check(hostAddress)) {
            _log.debug("Access denied from " + hostAddress);
            currentContext.respond(530, "Sorry. Access denied from " + hostAddress);
            return;
        }
        currentContext.respond(220, getHeader());
        try {
            while (true) // loop forever waiting for commands.
            {
                String str = null;
                try {
                    if ((str = currentContext.in.readLine()) == null) {
                        // Break on null command.
                        _log.debug("Breaking on null command");
                        break;
                    }
                } catch (final IOException e) {
                    _log.debug("waiting command", e);
                    break;
                }
                // Make a copy of a normalised command in upper case.
                final var ustr = str.trim().toUpperCase();
                _log.debug("Command received: "
                        + (ustr.startsWith("PASS ") ? "PASS " + Format.hide(str.substring(4).trim()) : str));
                // Before we go to the general command set we force a login.
                // Accept only USER, PASS, FEAT, TYPE, ALIAS and QUIT at this point.
                if (ustr.startsWith("USER ")) {
                    if (_pass != null && _pass.isLogged()) {
                        currentContext.respond(530, "Already logged in");
                    } else {
                        new USER(currentContext, str.substring(4).trim());
                    }
                    continue;
                }
                if (ustr.startsWith("QUIT") || ustr.endsWith("ABOR")) {
                    // Can always quit!
                    currentContext.respond(221, "Goodbye");
                    break;
                } else if (ustr.startsWith("FEAT")) {
                    new FEAT(currentContext, str.substring(4).trim());
                    continue;
                } else if (ustr.startsWith("PASS ")) {
                    if (_pass != null && _pass.isLogged()) {
                        currentContext.respond(530, "Already logged in");
                    } else {
                        _pass = new PASS(currentContext, str.substring(4).trim());
                        if (loginRequest++ > 2 && !_pass.isLogged()) {
                            currentContext.respond(221, "Goodbye");
                            break;
                        }
                    }
                    continue;
                } else if (ustr.startsWith("SYST")) {
                    new SYST(currentContext, str.substring(4).trim());
                    continue;
                } else if (ustr.startsWith("ALIAS")) {
                    new ALIAS(currentContext, str.substring(5).trim());
                    continue;
                } else if (ustr.startsWith("TYPE")) {
                    new TYPE(currentContext, str.substring(4).trim());
                    continue;
                }
                // Always check to see if we're logged in before processing any
                // commands.
                if (!(_pass != null && _pass.isLogged())) {
                    currentContext.respond(530, "Not logged in");
                } else {
                    // All commands except USER,PASS,SYST,QUIT,SITE and ALIAS.
                    try {
                        if (!Cnf.at("FtpPlugin", "extended", true)
                                && (ustr.startsWith("EPSV") || ustr.startsWith("EPRT"))) {
                            _log.debug("Command not found: " + str);
                            currentContext.respond(500, "'" + str + "': command not understood");
                        } else if (!Cnf.at("FtpPlugin", "rest", true) && ustr.startsWith("REST")) {
                            _log.debug("Command not found: " + str);
                            currentContext.respond(500, "'" + str + "': command not understood");
                        } else {
                            Util.exec(getClass(), currentContext,
                                    ustr.startsWith("SITE") ? (str = str.substring(4).trim()) : str,
                                    _pass.isFromCache());
                        }
                    } catch (final ClassNotFoundException e) {
                        _log.debug("Command not found: " + str);
                        currentContext.respond(500, "'" + str + "': command not understood");
                    } catch (final Exception e) {
                        _log.warn("Calling command " + str, e);
                        break;
                    }
                }
            }
            _log.info("Closing control connection (" + Format.formatDuration(startTime, System.currentTimeMillis())
                    + ")");
            currentContext.respond(221, "Service closing control connection");
        } finally {
            StreamPlugThread.closeQuietly(currentContext.dataSocket);
            if (_pass != null && _pass.isLogged()) {
                currentContext.newEvent("logout", null, false);
                _pass.logout();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("CacheConnectionCount".equals(attributeName)) {
                return PASS.getCacheConnectionCount();
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
            if ("clearCache".equals(operationName)) {
                PASS.clearCache();
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
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                "The " + getRef() + " plugin allows Member State users to submit jobs and to transfer "
                        + "files (between their own computer and ECMWF). This extended FTP "
                        + "server can also be used for access to ECMWF computing and archiving "
                        + "facilities from within shell scripts. " + super.getMBeanInfo().getDescription(),
                new MBeanAttributeInfo[] { new MBeanAttributeInfo("CacheConnectionCount", "java.lang.Integer",
                        "CacheConnectionCount: number of connections in the cache.", true, false, false) },
                new MBeanOperationInfo[] { new MBeanOperationInfo("clearCache", "clearCache(): clear the login cache.",
                        null, "void", MBeanOperationInfo.ACTION) });
    }
}
