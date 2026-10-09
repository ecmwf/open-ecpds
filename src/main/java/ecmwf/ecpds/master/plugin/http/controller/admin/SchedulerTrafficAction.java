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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/** Read-only administration view of the Master's current background traffic workers. */
public class SchedulerTrafficAction extends PDSAction {
    private static final Logger LOG = LogManager.getLogger(SchedulerTrafficAction.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> TYPES = Set.of("Proxy", "Backup", "Replication");

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException {
        final var requested = request.getParameter("type");
        final var type = requested == null ? "Proxy" : requested;
        final var json = "list".equals(request.getParameter("json"));
        response.setHeader("Cache-Control", "no-store");
        if (!TYPES.contains(type)) {
            LOG.warn("Invalid scheduler traffic type: {}", type);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            if (json) {
                writeJson(response, MAPPER.createObjectNode().put("error", "Select Proxy, Backup or Replication."));
                return null;
            }
            throw new ECMWFException("Select Proxy, Backup or Replication.");
        }
        request.setAttribute("schedulerTrafficType", type);
        if (!json) {
            return mapping.findForward("success");
        }
        final ObjectNode root;
        try {
            final var snapshot = MasterManager.getMI().getSchedulerTraffic(type);
            root = MAPPER.valueToTree(snapshot);
            root.put("observedAt", System.currentTimeMillis());
            for (final var row : root.withArray("transfers")) {
                final var item = (ObjectNode) row;
                item.put("canViewTransfer", user.hasAccess(
                        getResource(request, "datatransfer.basepath") + "/" + row.get("transferId").asLong()));
                item.put("canViewDataFile", user
                        .hasAccess(getResource(request, "datafile.basepath") + "/" + row.get("dataFileId").asLong()));
                item.put("canViewDestination", user.hasAccess(
                        getResource(request, "destination.basepath") + "/" + row.get("destination").asText()));
            }
        } catch (final Exception e) {
            LOG.warn("Cannot retrieve {} scheduler traffic", type, e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            writeJson(response, MAPPER.createObjectNode().put("error",
                    "Unable to retrieve scheduler traffic from the Master. Refresh to retry."));
            return null;
        }
        writeJson(response, root);
        return null;
    }

    private static void writeJson(final HttpServletResponse response, final ObjectNode root) throws ECMWFException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final IOException e) {
            LOG.warn("Cannot write scheduler traffic response", e);
            throw new ECMWFException("Cannot write scheduler traffic response: " + e.getMessage());
        }
    }
}
