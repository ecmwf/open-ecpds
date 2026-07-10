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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * The user tries to Log In. Validate credentials and put the user into the
 * session.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFAction;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;
import ecmwf.web.model.users.UserException;
import ecmwf.web.services.users.UserAuthStrategy;

/**
 * The Class LoginAction.
 */
public class LoginAction extends ECMWFAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(LoginAction.class);

    /**
     * {@inheritDoc}
     *
     * Safe perform.
     */
    @Override
    public ActionForward safePerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response)
            throws ECMWFException, ClassCastException {
        request.setAttribute("title", System.getProperty("monitor.title"));
        request.setAttribute("loginAnimatedBackground", Cnf.at("MonitorPlugin", "loginAnimatedBackground", false));
        setAuthModeAttributes(request);
        final UserAuthStrategy strategy = new EcPdsUserAuthStrategy();
        try {
            final var auto = request.getParameter("auto");
            if ("false".equals(auto)) {
                return mapping.findForward("failure");
            }
            final var user = strategy.getUser(request);
            request.getSession().setAttribute(User.SESSION_KEY, user);
            log.debug("Login success for user '" + user.getUid() + "'");
            final var after = request.getSession().getAttribute(PDSAction.RETURN_AFTER_LOGIN_KEY);
            if (after != null) {
                return new ActionForward(after.toString(), true);
            } else {
                return mapping.findForward("success");
            }
        } catch (final UserException e) {
            if (request.getParameter(EcPdsUserAuthStrategy.USER_PARAMETER) != null
                    || request.getParameter(EcPdsUserAuthStrategy.PASS_PARAMETER) != null
                    || request.getAttribute(EcPdsUserAuthStrategy.USER_REQUEST_KEY) != null) {
                final var showPassword = Boolean.TRUE.equals(request.getAttribute("showPassword"));
                final var showOtp = Boolean.TRUE.equals(request.getAttribute("showOtp"));
                final var errorKey = showPassword && !showOtp ? "errors.loginFailedPasswordOnly"
                        : !showPassword && showOtp ? "errors.loginFailedOtpOnly" : "errors.loginFailed";
                request.getSession().setAttribute(Globals.ERROR_KEY, ECMWFActionForm.newErrors(errorKey, ""));
                return new ActionForward("/do/login", true);
            }
            return mapping.findForward("failure");
        }
    }

    /**
     * Reads the {@code authMode} config key from the {@code [MonitorPlugin]} section and sets two boolean request
     * attributes — {@code showPassword} and {@code showOtp} — that the login JSP uses to conditionally render the
     * credential field. Accepted values (comma-separated, case-insensitive): {@code password}, {@code otp}. Defaults to
     * both when the key is absent or empty.
     *
     * @param request
     *            the current HTTP request
     */
    static void setAuthModeAttributes(final HttpServletRequest request) {
        final var mode = Cnf.at("MonitorPlugin", "authMode", "password,otp").toLowerCase();
        final var tokens = mode.split(",");
        var showPassword = false;
        var showOtp = false;
        for (final var token : tokens) {
            final var t = token.trim();
            if ("password".equals(t)) {
                showPassword = true;
            } else if ("otp".equals(t)) {
                showOtp = true;
            }
        }
        // Default to both if config is missing or unrecognised
        if (!showPassword && !showOtp) {
            showPassword = true;
            showOtp = true;
        }
        request.setAttribute("showPassword", showPassword);
        request.setAttribute("showOtp", showOtp);
    }
}
