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

/** Read-only journal monitoring and shared, inexpensive administration indicators. */
public class OperationalEventJournalAction extends PDSAction {
    private static final Logger LOG = LogManager.getLogger(OperationalEventJournalAction.class);
    private static final ObjectMapper JSON = new ObjectMapper();

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
        if (!"status".equals(request.getParameter("json"))) {
            populateStatus(request, user);
            return mapping.findForward("success");
        }
        final var root = JSON.createObjectNode();
        try {
            final var status = MasterManager.getMI()
                    .getOperationalEventJournalStatus(Util.getECpdsSessionFromObject(user), true);
            root.set("journal", JSON.valueToTree(status));
            root.put("state", status.state());
            root.put("danger", status.danger());
            root.put("attention", status.attention());
        } catch (final Exception e) {
            LOG.warn("Cannot retrieve operational event journal monitoring data", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            root.put("error", "Journal status unavailable. Unable to retrieve monitoring data from the Master.");
        }
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            JSON.writeValue(response.getWriter(), root);
        } catch (final IOException e) {
            LOG.warn("Cannot write operational event journal status response", e);
            throw new ECMWFException("Cannot write operational event journal status response");
        }
        return null;
    }
}
