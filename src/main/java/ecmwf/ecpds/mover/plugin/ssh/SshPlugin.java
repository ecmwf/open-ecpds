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

package ecmwf.ecpds.mover.plugin.ssh;

import java.io.Closeable;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.9
 * @since 2024-11-23
 */

import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.client.config.keys.ClientIdentity;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.keyboard.DefaultKeyboardInteractiveAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.core.CoreModuleProperties;

import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.plugin.PluginThread;
import ecmwf.common.security.LoginManagement;
import ecmwf.common.ssh.AuthenticationInfo;
import ecmwf.common.ssh.MinaFileSystemAccessor;
import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class SshPlugin.
 */
public final class SshPlugin extends PluginThread {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SshPlugin.class);

    /** The mover. */
    private static final MoverServer mover = getCaller(MoverServer.class);

    /** The Constant _NAME. */
    private static final String NAME = SshPlugin.class.getSimpleName();

    /** The Constant _VERSION. */
    private static final String VERSION = Version.getFullVersion();

    /** The Constant listenAddress. */
    private final String listenAddress = Cnf.at(NAME, "listenAddress", "0.0.0.0");

    /** The Constant keyPairsDir. */
    private final String keyPairsDir = Cnf.at(NAME, "keyPairsDir", "ssh/keys");

    /** The Constant port. */
    private final int port = Cnf.at(NAME, "port", 22);

    /** The server. */
    private SshServer server;

    /**
     * Instantiates a new ssh2 plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     *
     * @throws SshException
     *             the ssh exception
     */
    public SshPlugin(final String ref, final Map<String, String> params) throws SshException {
        super(ref, params);
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
     * Gets the version.
     *
     * @return the version
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * Start.
     *
     * @return true, if successful
     */
    @Override
    public synchronized boolean start() {
        if (server == null) {
            _log.info("Starting SSHD");
            try {
                _log.info("Listen on {}:{}", listenAddress, port);
                server = SshServer.setUpDefaultServer();
                server.setHost(listenAddress);
                server.setPort(port);
                final var maxConcurrentSessions = Cnf.at(NAME, "maxConcurrentSessions", 0);
                if (maxConcurrentSessions > 0)
                    server.getProperties().put(CoreModuleProperties.MAX_CONCURRENT_SESSIONS.getName(),
                            maxConcurrentSessions);
                server.getProperties().put(CoreModuleProperties.SERVER_IDENTIFICATION.getName(),
                        Cnf.at("SshPlugin", "softwareVersion", "ECPDS_SSHD"));
                CoreModuleProperties.WELCOME_BANNER.set(server, Cnf.fileContentAt("SshPlugin", "banner", ""));
                server.setKeyPairProvider(
                        ClientIdentity.loadDefaultKeyPairProvider(Path.of(keyPairsDir), false, true, null));
                server.setPublickeyAuthenticator(this::verifyKey);
                server.setPasswordAuthenticator(this::verifyPassword);
                server.setKeyboardInteractiveAuthenticator(new DefaultKeyboardInteractiveAuthenticator());
                server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder()
                        .withFileSystemAccessor(new MinaFileSystemAccessor()).build()));
                // server.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE,
                // Duration.ofSeconds(30));
                // No interactive shell
                server.setShellFactory(null);
                // Reject all command execution (e.g., "ssh user@host ls")
                server.setCommandFactory((_, _) -> {
                    throw new UnsupportedOperationException("Command execution is not allowed. SFTP only.");
                });
                // By default forwarding is disabled, but we explicitly enforce it
                server.setForwardingFilter(null);
                // Only allow SFTP subsystem
                server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder()
                        .withFileSystemAccessor(new MinaFileSystemAccessor()).build()));
                server.start(); // Start SSHD
            } catch (final Exception e) {
                _log.error("Starting the plugin", e);
                server = null;
            }
        }
        return server != null;
    }

    private boolean verifyKey(final String username, final PublicKey key, final ServerSession session) {
        final var info = getAuthenticationInfo(username, null, session);
        if (info != null) {
            _log.debug("Certificate logon for {} ({})", username, session.getClientVersion());
            try {
                for (final var e : AuthorizedKeyEntry.readAuthorizedKeys(
                        new StringReader(info.profile().getIncomingUser().getAuthorizedSSHKeys()), true)) {
                    if (KeyUtils.compareKeys(key, e.resolvePublicKey(session, e.getLoginOptions(), null))) {
                        return true;
                    }
                }
            } catch (IOException | GeneralSecurityException e) {
                _log.warn("Error parsing public key", e);
            }
        }
        return false;
    }

    private boolean verifyPassword(final String username, final String password, final ServerSession session) {
        final var info = getAuthenticationInfo(username, password, session);
        if (info != null) {
            _log.debug("Password logon for {} ({})", username, session.getClientVersion());
            return true;
        }
        return false;
    }

    private static AuthenticationInfo getAuthenticationInfo(final String username, final String password,
            final ServerSession session) {
        if (session.getClientAddress() instanceof InetSocketAddress socketAddress) {
            final var remoteIP = socketAddress.getHostName();
            final var from = "Using sftp on DataMover=" + mover.getRoot() + " from " + username + "@" + remoteIP;
            if (!LoginManagement.check(remoteIP)) {
                // If the address is banned then don't try the login process!
                _log.warn("{}: banned!", from);
            } else {
                try {
                    var info = new AuthenticationInfo(remoteIP, username,
                            mover.getMasterProxy().getIncomingProfile(username, password, from),
                            NativeAuthenticationProvider.getInstance().getUserSession(remoteIP, username, password,
                                    "sftp",
                                    (Closeable) () -> session.disconnect(SshConstants.SSH2_DISCONNECT_BY_APPLICATION,
                                            "Close requested")));
                    session.setAttribute(AuthenticationInfo.AUTHENTICATION_INFO, info);
                    // Check ECtransSetup: info.profile().getECtransSetup()
                    CoreModuleProperties.IDLE_TIMEOUT.set(session, Duration.ofMinutes(10));
                    session.getProperties().put(CoreModuleProperties.AUTH_TIMEOUT.getName(),
                            Duration.ofSeconds(30).toMillis());
                    session.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE,
                            Duration.ofSeconds(30));
                    // End of ECtransOptions
                    _log.info("Authenticated {}@{} (client: {}, session: {})", username, remoteIP,
                            session.getClientVersion(), Long.toHexString(session.getIoSession().getId()));
                    session.addSessionListener(new SessionListener() {
                        @Override
                        public void sessionClosed(Session s) {
                            _log.info("Session closed for {}@{}", username, remoteIP);
                        }

                        @Override
                        public void sessionException(Session s, Throwable t) {
                            _log.warn("Session exception for {}@{}", username, remoteIP, t);
                        }
                    });
                    _log.debug("Algorithms for {}@{}: clientVersion={}", username, remoteIP,
                            session.getClientVersion());
                    if (password != null)
                        LoginManagement.clear(remoteIP);
                    return info;
                } catch (final Throwable t) {
                    LoginManagement.increment(remoteIP);
                    _log.warn(from, t);
                }
            }
        }
        return null;
    }

    /**
     * Stop.
     */
    @Override
    public synchronized void stop() {
        if (server != null) {
            _log.info("Stopping SSHd");
            try {
                server.stop();
            } catch (IOException e) {
                _log.warn("Stopping the plugin", e);
            }
        }
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
            if ("ListenAddress".equals(attributeName)) {
                return listenAddress;
            }
            if ("Port".equals(attributeName)) {
                return port;
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(), super.getMBeanInfo().getDescription(),
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("ListenAddress", "java.lang.String",
                                "ListenAddress: the address to listen to.", true, false, false),
                        new MBeanAttributeInfo("Port", "java.lang.Integer", "Port: the plugin port number.", true,
                                false, false) },
                new MBeanOperationInfo[0]);
    }
}
