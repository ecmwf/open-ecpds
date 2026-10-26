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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.extension.InteractiveLogins;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.users.UserBean;
import ecmwf.web.controller.ECMWFAction;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * Thin browser boundary: protocol validation and local account authorization stay on the Master.
 */
public final class InteractiveLoginAction extends ECMWFAction {
    private static final Logger LOG = LogManager.getLogger(InteractiveLoginAction.class);
    private static final String START_KEY = InteractiveLoginAction.class.getName() + ".start";
    private static final String PENDING_KEY = InteractiveLoginAction.class.getName() + ".pending";

    private final ecmwf.ecpds.master.ManagementInterface management;

    public InteractiveLoginAction() {
        this(null);
    }

    InteractiveLoginAction(final ecmwf.ecpds.master.ManagementInterface management) {
        this.management = management;
    }

    private ecmwf.ecpds.master.ManagementInterface management() throws ecmwf.ecpds.master.MasterException {
        return management == null ? MasterManager.getMI() : management;
    }

    private record Pending(String state, String binding) implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
    }

    static void attributes(final HttpServletRequest request, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        try {
            final var label = MasterManager.getMI().getInteractiveLoginName();
            if (label != null && !label.isBlank()) {
                final var session = request.getSession();
                synchronized (session) {
                    if (session.getAttribute(START_KEY) == null) {
                        session.setAttribute(START_KEY, InteractiveLogins.random());
                    }
                    request.setAttribute("interactiveLoginToken", session.getAttribute(START_KEY));
                }
                request.setAttribute("interactiveLoginName", label);
            }
        } catch (Exception e) {
            LOG.debug("Interactive login is unavailable");
        }
    }

    static void clear(final HttpServletRequest request) {
        final var session = request.getSession(false);
        if (session != null) {
            synchronized (session) {
                session.removeAttribute(START_KEY);
                session.removeAttribute(PENDING_KEY);
            }
        }
    }

    @Override
    public ActionForward safePerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        try {
            final var session = request.getSession(false);
            if (session == null || !request.isSecure()) {
                throw new IOException("Interactive login requires an HTTPS browser session");
            }
            if ("/interactiveLogin".equals(mapping.getPath())) {
                if (!"POST".equals(request.getMethod())) {
                    throw new IOException("Interactive login requires POST");
                }
                synchronized (session) {
                    final var expected = (String) session.getAttribute(START_KEY);
                    final var received = single(request, "loginToken");
                    if (expected == null || received == null
                            || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                                    received.getBytes(StandardCharsets.UTF_8))) {
                        throw new IOException("Invalid login initiation");
                    }
                    session.removeAttribute(START_KEY);
                    session.removeAttribute(PENDING_KEY);
                    request.changeSessionId();
                    final var binding = InteractiveLogins.random();
                    final var challenge = management().beginInteractiveLogin(binding);
                    InteractiveLogins.requireHttps(java.net.URI.create(challenge.authorizationUri()));
                    session.setAttribute(PENDING_KEY, new Pending(challenge.state(), binding));
                    response.sendRedirect(challenge.authorizationUri());
                }
                return null;
            }
            if (!"GET".equals(request.getMethod())) {
                throw new IOException("Interactive callback requires GET");
            }
            final Pending pending;
            synchronized (session) {
                pending = (Pending) session.getAttribute(PENDING_KEY);
                session.removeAttribute(PENDING_KEY);
            }
            if (pending == null || !pending.state().equals(single(request, "state"))
                    || request.getParameterMap().containsKey("error")) {
                throw new IOException("Invalid interactive callback");
            }
            final var authenticated = management().completeInteractiveLogin(pending.state(), pending.binding(),
                    single(request, "code"), request.getRemoteHost(), request.getHeader("User-Agent"));
            session.invalidate();
            final var replacement = request.getSession(true);
            replacement.setAttribute(User.SESSION_KEY, UserBean.fromInteractiveSession(authenticated));
            response.sendRedirect(request.getContextPath() + "/do/start");
            return null;
        } catch (Exception e) {
            LOG.warn("Interactive login failed");
            request.getSession().setAttribute(Globals.ERROR_KEY,
                    ECMWFActionForm.newErrors("errors.interactiveLoginFailed", ""));
            return new ActionForward("/do/login?auto=false", true);
        }
    }

    private static String single(final HttpServletRequest request, final String name) throws IOException {
        final var values = request.getParameterValues(name);
        if (values == null || values.length != 1 || values[0].length() > 8192) {
            throw new IOException("Invalid login response parameter");
        }
        return values[0];
    }
}
