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

package ecmwf.ecpds.master.plugin.http.controller.login;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Strategy which implements authentication via the specific system designed for
 * ECPDS. This system uses NIS uids and passwords for the validation, which is
 * done via the MasterServer and a remote service.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.web.home.users.UserHome;
import ecmwf.web.model.users.UnrecoverableUserException;
import ecmwf.web.model.users.User;
import ecmwf.web.model.users.UserException;
import ecmwf.web.services.users.UserAuthStrategy;

/**
 * The Class EcPdsUserAuthStrategy.
 */
public class EcPdsUserAuthStrategy implements UserAuthStrategy {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(EcPdsUserAuthStrategy.class);

    /** The Constant USER_PARAMETER. */
    public static final String USER_PARAMETER = "user";

    /** The Constant PASS_PARAMETER. */
    public static final String PASS_PARAMETER = "password";

    /** The Constant USER_REQUEST_KEY. */
    public static final String USER_REQUEST_KEY = "ECPDS-NAME";

    /** The Constant CERT_REQUEST_KEY. */
    public static final String CERT_REQUEST_KEY = "ECPDS-CERT";

    /**
     * Not relevant here.
     *
     * @return the cookie name
     */

    @Override
    public String getCookieName() {
        return "";
    }

    /**
     * Gets the user.
     *
     * @param credential
     *            the credential
     *
     * @return the user
     *
     * @throws UserException
     *             the user exception
     * @throws UnrecoverableUserException
     *             the unrecoverable user exception
     */
    @Override
    public User getUser(final Object credential) throws UserException, UnrecoverableUserException {
        return credential instanceof final HttpServletRequest httpRequest ? getUser(httpRequest) : null;
    }

    /**
     * Return an user object obtained via the credentials contained in this request.
     *
     * @param request
     *            the request
     *
     * @return the user
     *
     * @throws UserException
     *             the user exception
     */

    private User getUser(final HttpServletRequest request) throws UserException {
        var user = request.getParameter(USER_PARAMETER);
        var password = request.getParameter(PASS_PARAMETER);
        if (user == null || "".equals(user) || password == null || "".equals(password)) {
            final var userO = request.getAttribute(USER_REQUEST_KEY);
            final var certO = request.getAttribute(CERT_REQUEST_KEY);
            if (userO == null || certO == null) {
                throw new UserException("We need an user parameter or a certificate");
            }
            user = userO.toString();
            password = certO.toString();
            log.debug("Trying to get user with certificate for '" + user + "'");
        } else {
            log.debug("Trying to get user with password for '" + user + "'");
        }
        return getUser(user, password, request.getRemoteHost(), request.getHeader("User-Agent"), null);
    }

    /**
     * Will probably NOT be called. ECPDS doesn't know about public users.
     *
     * @param request
     *            the request
     *
     * @return the public user
     *
     * @throws UserException
     *             the user exception
     */
    public User getPublicUser(final HttpServletRequest request) throws UserException {
        throw new UserException("Public user not supported");
    }

    /**
     * No such a thing here as a Fallback public user here....
     *
     * @return the fallback public user
     */
    @Override
    public User getFallbackPublicUser() {
        return null;
    }

    /**
     * Gets the user.
     *
     * @param uid
     *            the uid
     * @param password
     *            the password
     * @param host
     *            the host
     * @param agent
     *            the agent
     * @param comment
     *            the comment
     *
     * @return the user
     *
     * @throws UserException
     *             the user exception
     */
    private User getUser(final String uid, final String password, final String host, final String agent,
            final String comment) throws UserException {
        // Authenticate user
        if (uid == null || password == null || "".equals(uid) || "".equals(password)) {
            throw new UserException("Fill both 'User name' and 'Password' fields.");
        }
        try {
            return UserHome.findByUidAndPass(uid, password, host, agent, comment);
        } catch (final UserException e) {
            log.warn("Login failed for user '" + uid + "'", e);
            throw new UserException("Problem getting user", e);
        }
    }

    /**
     * Gets the credential.
     *
     * @param request
     *            the request
     *
     * @return the credential
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public Object getCredential(final HttpServletRequest request) throws UserException {
        return null;
    }

    /**
     * Gets the public user.
     *
     * @param credential
     *            the credential
     *
     * @return the public user
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public User getPublicUser(final Object credential) throws UserException {
        return null;
    }
}
