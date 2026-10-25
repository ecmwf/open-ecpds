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
package ecmwf.ecpds.master.plugin.http.controller.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.OperationalEventJournalStatus;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/** Operational event journal monitoring and runtime controls. */
public class OperationalEventJournalAction extends PDSAction {
    private static final Logger LOG = LogManager.getLogger(OperationalEventJournalAction.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String TOKEN = OperationalEventJournalAction.class.getName() + ".csrf";

    @Override
    protected boolean isAuthorized(final User user, final String currentPath, final HttpServletRequest request)
            throws ECMWFException {
        return super.isAuthorized(user, currentPath, request) && user.hasAccess("/do/admin/");
    }

    public static OperationalEventJournalStatus populateStatus(final HttpServletRequest request, final User user) {
        if (Boolean.TRUE.equals(request.getAttribute("journalStatusLoaded"))) {
            return (OperationalEventJournalStatus) request.getAttribute("journalStatus");
        }
        request.setAttribute("journalStatusLoaded", true);
        try {
            final var status = MasterManager.getMI()
                    .getOperationalEventJournalStatus(Util.getECpdsSessionFromObject(user), false);
            request.setAttribute("journalStatus", status);
            request.setAttribute("journalEnabled", status.enabled());
            request.setAttribute("journalDanger", status.danger());
            request.setAttribute("journalAttention", status.attention());
            request.setAttribute("journalHadPersistenceFailures", status.persistenceFailures() > 0);
            return status;
        } catch (final Exception e) {
            LOG.warn("Cannot retrieve operational event journal runtime status", e);
            request.setAttribute("journalUnavailable", true);
            return null;
        }
    }

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException {
        response.setHeader("Cache-Control", "no-store");
        final var httpSession = request.getSession();
        final String token;
        synchronized (httpSession) {
            if (!(httpSession.getAttribute(TOKEN) instanceof String)) {
                httpSession.setAttribute(TOKEN, UUID.randomUUID().toString());
            }
            token = (String) httpSession.getAttribute(TOKEN);
        }
        request.setAttribute("journalCsrf", token);

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            final var submitted = request.getParameter("csrf");
            if (submitted == null || submitted.length() > 128 || !MessageDigest
                    .isEqual(token.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8))) {
                return error(response, HttpServletResponse.SC_FORBIDDEN,
                        "The form expired or is invalid; reload the page.");
            }
            try {
                final var session = Util.getECpdsSessionFromObject(user);
                final var operation = request.getParameter("op");
                final var state = request.getParameter("state");
                if (state == null) {
                    throw new IllegalArgumentException("Unknown journal control state");
                }
                final var paused = switch (state) {
                case "paused" -> true;
                case "resumed" -> false;
                default -> throw new IllegalArgumentException("Unknown journal control state");
                };
                if ("collection".equals(operation)) {
                    MasterManager.getMI().setOperationalEventCollectionPaused(session, paused);
                } else if ("delivery".equals(operation)) {
                    MasterManager.getMI().setOperationalEventDeliveryPaused(session, paused);
                } else {
                    throw new IllegalArgumentException("Unknown journal control");
                }
                return status(response, session, true);
            } catch (final IllegalArgumentException e) {
                LOG.warn("Invalid operational event journal control request", e);
                return error(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } catch (final Exception e) {
                LOG.warn("Operational event journal control failed", e);
                return error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Unable to update operational event journal controls.");
            }
        }

        if (!"status".equals(request.getParameter("json"))) {
            populateStatus(request, user);
            return mapping.findForward("success");
        }
        try {
            return status(response, Util.getECpdsSessionFromObject(user), false);
        } catch (final Exception e) {
            LOG.warn("Cannot retrieve operational event journal monitoring data", e);
            return error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Journal status unavailable. Unable to retrieve monitoring data from the Master.");
        }
    }

    private ActionForward status(final HttpServletResponse response, final ecmwf.ecpds.master.ECpdsSession session,
            final boolean changed) throws Exception {
        final var root = JSON.createObjectNode();
        final var status = MasterManager.getMI().getOperationalEventJournalStatus(session, true);
        root.set("journal", JSON.valueToTree(status));
        root.put("state", status.state());
        root.put("danger", status.danger());
        root.put("attention", status.attention());
        root.put("changed", changed);
        writeJson(response, root);
        return null;
    }

    private ActionForward error(final HttpServletResponse response, final int status, final String message)
            throws ECMWFException {
        response.setStatus(status);
        final var root = JSON.createObjectNode();
        root.put("error", message);
        try {
            writeJson(response, root);
        } catch (final Exception e) {
            LOG.warn("Cannot write operational event journal error response", e);
            throw new ECMWFException("Cannot write operational event journal response");
        }
        return null;
    }

    private static void writeJson(final HttpServletResponse response,
            final com.fasterxml.jackson.databind.JsonNode root) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        JSON.writeValue(response.getWriter(), root);
    }
}
