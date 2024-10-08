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

import static ecmwf.common.text.Util.isNotEmpty;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Activity;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.UserSession;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class CurrentContext.
 */
public final class CurrentContext {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(CurrentContext.class);

    /** The Constant ATYPE. */
    public static final char ATYPE = 'A'; // Ascii transfer type.

    /** The Constant ITYPE. */
    public static final char ITYPE = 'I'; // Image trasfer type (Binary).

    /** The Constant AMODE. */
    private static final String AMODE = "ASCII mode";

    /** The Constant IMODE. */
    private static final String IMODE = "Binary mode";

    /** The user. */
    public String user; // person or machine name

    /** The auth name. */
    public String authName; // login name as it arrived

    /** The remote site. */
    public String remoteSite; // remote site name

    /** The remote ip. */
    public InetAddress remoteIP = null;

    /** The local ip. */
    public InetAddress localIP = null; // Internet connection info

    /** The out. */
    public PrintWriter out = null;

    /** The in. */
    public BufferedReader in = null;

    /** The client socket. */
    public Socket clientSocket; // incoming client socket

    /** The data socket. */
    public DataSocket dataSocket = null; // data socket (active or PASV)

    /** The data port. */
    public int dataPort; // current data port

    /** The data ip. */
    public InetAddress dataIP = null; // Current data IP address

    /** The accounts. */
    public final Vector<String> accounts = new Vector<>(); // Account
                                                           // names
                                                           // (if used)
                                                           // from ACCT
                                                           // command

    /**
     * Logout.
     */
    public void logout() {
        StreamPlugThread.closeQuietly(clientSocket);
    }

    /**
     * The transfer type.
     */
    public char transferType = ATYPE;

    /** The activity. */
    public Activity activity = null;

    /** The aliases. */
    public final Map<String, Map<String, String>> aliases = new ConcurrentHashMap<>();

    /** The browser. */
    public boolean browser = false;

    /** The client name. */
    public String clientName = "";

    /** The buffer. */
    public int buffer = Cnf.at("Other", "buffer", 65536);

    /** The session. */
    public UserSession session = null;

    /** The domain name. */
    public String domainName = null;

    /** The domain user. */
    public String domainUser = null;

    /** The domain value. */
    public String domainValue = null;

    /** The parameters list. */
    public final Map<String, Object> parametersList = new ConcurrentHashMap<>();

    /** The passive mode. */
    public boolean passiveMode = Cnf.at("FtpPlugin", "passive", true);

    /** The path. */
    public final Map<String, String> path = new ConcurrentHashMap<>(); // ECMWF
                                                                       // specific
                                                                       // parameters

    /** The plugin. */
    public FtpPlugin plugin = null;

    /** The rename file. */
    public String renameFile = null;

    /** The rest. */
    public long rest = 0;

    /** The umask. */
    public int umask = 640;

    /**
     * Instantiates a new current context.
     *
     * @param plugin
     *            the plugin
     */
    CurrentContext(final FtpPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Respond.
     *
     * @param message
     *            the message
     */
    void respond(final String message) {
        out.write(message);
        out.write(Util.CRLF);
        out.flush();
    }

    /**
     * Transfer text.
     *
     * @return the string
     */
    public String transferText() {
        return transferType == ATYPE ? AMODE : IMODE;
    }

    /**
     * Transfer eol.
     *
     * @return the string
     */
    public String transferEOL() {
        return transferType == ATYPE ? Util.CRLF : Util.LF;
    }

    /**
     * Contains aliases.
     *
     * @return true, if successful
     */
    public boolean containsAliases() {
        return aliases.containsKey(domainName);
    }

    /**
     * Gets the alias.
     *
     * @param source
     *            the source
     *
     * @return the alias
     */
    public String getAlias(final String source) {
        if (domainName != null && aliases.containsKey(domainName)) {
            return aliases.get(domainName).get(source);
        }
        return null;
    }

    /**
     * Gets the aliases.
     *
     * @return the aliases
     */
    public Map<String, String> getAliases() {
        if (domainName != null && aliases.containsKey(domainName)) {
            return aliases.get(domainName);
        }
        return null;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        var current = "*".equals(domainName) ? "/" : path.get(domainName + '.' + domainUser);
        if (current == null && !"*".equals(domainName)) {
            current = "/";
            path.put(domainName + '.' + domainUser, current);
        }
        return current;
    }

    /**
     * New event.
     *
     * @param action
     *            the action
     * @param comment
     *            the comment
     * @param error
     *            the error
     */
    public synchronized void newEvent(final String action, final String comment, final boolean error) {
        try {
            if (activity == null) {
                NativeAuthenticationProvider.getInstance().newActivity(user, plugin.getRef(), remoteIP.getHostName(),
                        null, action, comment, error);
            } else {
                NativeAuthenticationProvider.getInstance().newEvent(activity, action, comment, error);
            }
        } catch (final Exception e) {
            _log.debug("newEvent", e);
        }
    }

    /**
     * Puts the alias.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     */
    public void putAlias(final String source, final String target) {
        final Map<String, String> current;
        // Can only putAlias if the domain name is set.
        if (domainName == null) {
            return;
        }
        if (aliases.containsKey(domainName)) {
            current = aliases.get(domainName);
        } else {
            current = new ConcurrentHashMap<>();
            aliases.put(domainName, current);
        }
        current.put(source, target);
    }

    /**
     * Removes the alias.
     *
     * @param source
     *            the source
     */
    public void removeAlias(final String source) {
        if (domainName != null && aliases.containsKey(domainName)) {
            aliases.get(domainName).remove(source);
        }
    }

    /**
     * Respond.
     *
     * @param code
     *            the code
     * @param message
     *            the message
     */
    public void respond(final int code, final String message) {
        _log.debug("Response sent: " + code + " " + message);
        respond(code + " " + message);
    }

    /**
     * Respond.
     *
     * @param code
     *            the code
     * @param message
     *            the message
     * @param exception
     *            the exception
     */
    public void respond(final int code, final String message, final Throwable exception) {
        if (exception != null) {
            final var content = exception.getMessage();
            if (isNotEmpty(content)) {
                final var errorMessage = code + " " + message + " (" + content + ")";
                _log.warn("Response sent: " + errorMessage, exception);
                respond(errorMessage);
                return;
            }
        }
        final var errorMessage = code + " " + message;
        _log.warn("Response sent: " + errorMessage, exception);
        respond(errorMessage);
    }

    /**
     * Respond.
     *
     * @param code
     *            the code
     * @param exception
     *            the exception
     */
    public void respond(final int code, final Throwable exception) {
        final var errorMessage = exception.getMessage();
        _log.warn("Response sent: " + code + " " + errorMessage, exception);
        respond(code + " " + errorMessage);
    }
}
