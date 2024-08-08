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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.IOException;

import ecmwf.common.database.Activity;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.Event;
import ecmwf.ecbatch.eis.rmi.client.ECauthToken;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;

/**
 * The Class NativeAuthenticationProvider.
 */
public abstract class NativeAuthenticationProvider {
    /** The _cls. */
    private static Class<?> _cls = null;

    /** The _instance. */
    private static NativeAuthenticationProvider _instance = null;

    /**
     * New activity.
     *
     * @param user
     *            the user
     * @param plugin
     *            the plugin
     * @param host
     *            the host
     * @param agent
     *            the agent
     * @param action
     *            the action
     * @param comment
     *            the comment
     * @param error
     *            the error
     *
     * @return the activity
     */
    public Activity newActivity(final String user, final String plugin, final String host, final String agent,
            final String action, final String comment, final boolean error) {
        if (StarterServer.getInstance() instanceof final ECaccessServer ecaccessServer) {
            return ecaccessServer.newActivity(user, plugin, host, agent, action, comment, error);
        }
        return null;
    }

    /**
     * New event.
     *
     * @param activity
     *            the activity
     * @param action
     *            the action
     * @param comment
     *            the comment
     * @param error
     *            the error
     *
     * @return the event
     */
    public Event newEvent(final Activity activity, final String action, final String comment, final boolean error) {
        if (StarterServer.getInstance() instanceof final ECaccessServer ecaccessServer) {
            return ecaccessServer.newEvent(activity, action, comment, error);
        }
        return null;
    }

    /**
     * Gets the ECuser.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     */
    public ECUser getECUser(final String name) {
        if (StarterServer.getInstance() instanceof final ECaccessServer ecaccessServer) {
            return ecaccessServer.getECUser(name, true);
        }
        return null;
    }

    /**
     * Gets the root.
     *
     * @return the root
     *
     * @throws Exception
     *             the exception
     */
    public abstract String getRoot() throws Exception;

    /**
     * Connected.
     *
     * @return true, if successful
     */
    public boolean connected() {
        return true;
    }

    /**
     * Gets the new certificate.
     *
     * @param user
     *            the user
     * @param passcode
     *            the passcode
     * @param profile
     *            the profile
     * @param remoteHost
     *            the remote host
     *
     * @return the new certificate
     *
     * @throws Exception
     *             the exception
     */
    public byte[] getNewCertificate(final String user, final String passcode, final String profile,
            final String remoteHost) throws Exception {
        throw new EccmdException("Not implemented (only available for Remote Gateways)");
    }

    /**
     * Gets the new token.
     *
     * @param certificate
     *            the certificate
     * @param profile
     *            the profile
     * @param remoteHost
     *            the remote host
     *
     * @return the new token
     *
     * @throws Exception
     *             the exception
     */
    public String[] getNewToken(final byte[] certificate, final String profile, final String remoteHost)
            throws Exception {
        throw new EccmdException("Not implemented (only available for Remote Gateways)");
    }

    /**
     * Gets the new token.
     *
     * @param user
     *            the user
     * @param passcode
     *            the passcode
     * @param profile
     *            the profile
     * @param remoteHost
     *            the remote host
     *
     * @return the new token
     *
     * @throws Exception
     *             the exception
     */
    public String[] getNewToken(final String user, final String passcode, final String profile, final String remoteHost)
            throws Exception {
        throw new EccmdException("Not implemented (only available for Remote Gateways)");
    }

    /**
     * Checks if it is a registred user.
     *
     * @param user
     *            the user
     *
     * @return true, if is registred user
     *
     * @throws Exception
     *             the exception
     */
    public abstract boolean isRegistredUser(String user) throws Exception;

    /**
     * Allow differentiating tickets and passwords. Depending of the result the session will be created with one
     * getUserSession or the other.
     *
     * @param user
     *            the user
     * @param passwordOrTicket
     *            the password or ticket
     *
     * @return true, if it is a password
     */
    public boolean isPassword(final String user, final String passwordOrTicket) {
        return true;
    }

    /**
     * If tickets are supported then the isPassword method will be called to differentiate the passwords and the
     * tickets.
     *
     * @return true, if it does support tickets.
     */
    public boolean supportTickets() {
        return true;
    }

    /**
     * Called if we have a ticket.
     *
     * @param host
     *            the host
     * @param ticket
     *            the ticket
     * @param profile
     *            the profile
     * @param closeable
     *            the closeable
     *
     * @return the user session
     *
     * @throws Exception
     *             the exception
     */
    public abstract UserSession getUserSession(String host, String ticket, String profile, Closeable closeable)
            throws Exception;

    /**
     * Called if we have a password.
     *
     * @param host
     *            the host
     * @param user
     *            the user
     * @param password
     *            the password
     * @param profile
     *            the profile
     * @param closeable
     *            the closeable
     *
     * @return the user session
     *
     * @throws Exception
     *             the exception
     */
    public abstract UserSession getUserSession(String host, String user, String password, String profile,
            Closeable closeable) throws Exception;

    /**
     * Gets the user session.
     *
     * @param host
     *            the host
     * @param user
     *            the user
     * @param token
     *            the token
     * @param profile
     *            the profile
     * @param closeable
     *            the closeable
     *
     * @return the user session
     *
     * @throws Exception
     *             the exception
     */
    public abstract UserSession getUserSession(String host, String user, ECauthToken token, String profile,
            Closeable closeable) throws Exception;

    /**
     * Sets the provider.
     *
     * @param cls
     *            the new provider
     */
    public static void setProvider(final Class<?> cls) {
        _cls = cls;
    }

    /**
     * Gets the single instance of NativeAuthenticationProvider.
     *
     * @return single instance of NativeAuthenticationProvider
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static NativeAuthenticationProvider getInstance() throws IOException {
        if (_instance == null) {
            try {
                if (_cls == null) {
                    throw new IOException("There is no authentication provider configured");
                }

                _instance = (NativeAuthenticationProvider) _cls.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                throw new IOException("The authentication provider failed to initialize: " + e.getMessage());
            }
        }
        return _instance;
    }
}
