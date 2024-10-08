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
                request.setAttribute(Globals.ERROR_KEY, ECMWFActionForm.newErrors("errors.loginFailed", ""));
            }
            return mapping.findForward("failure");
        }
    }
}
