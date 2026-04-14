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

package ecmwf.ecpds.master.plugin.http.controller.transfer.host;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the host list, enabling
 * server-side pagination without loading all rows into the page HTML.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetHostListJsonAction.
 *
 * Handles AJAX DataTables server-side requests for the host list page. Returns JSON in the standard DataTables
 * server-side protocol format.
 */
public class GetHostListJsonAction extends PDSAction {

    /** Base path for host detail pages. */
    private static final String HOST_BASE_PATH = "/do/transfer/host";

    /** Base path for destination detail pages. */
    private static final String DEST_BASE_PATH = "/do/transfer/destination";

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws TransferException, ECMWFActionFormException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var label = Util.getValue(request, "label", "All");
        final var filter = Util.getValue(request, "hostFilter", "All");
        final var network = Util.getValue(request, "network", "All");
        final var hostType = Util.getValue(request, "hostType", "All");
        final var hostSearch = Util.getValue(request, "hostSearch", "");
        final var cursor = Util.getDataBaseCursorForDataTables(0, true, request);
        Collection<Host> hosts;
        try {
            hosts = HostHome.findByCriteria(label, filter, network, hostType, hostSearch, cursor);
        } catch (final TransferException e) {
            hosts = new ArrayList<>(0);
        }
        final var recordsTotal = Util.getCollectionSizeFrom(hosts);
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        final var data = root.putArray("data");
        for (final Host host : hosts) {
            final var row = data.addArray();
            row.add(buildFlagHtml(host));
            row.add(buildNameHtml(host));
            row.add(escapeHtml(host.getHost()));
            row.add(escapeHtml(host.getTransferGroupName()));
            row.add(escapeHtml(host.getNetworkName()));
            row.add(buildDestinationsHtml(host));
        }
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building host list: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // HTML column builders
    // -------------------------------------------------------------------------

    private static String buildFlagHtml(final Host host) {
        final var geo = host.getGeoIpLocation();
        if (geo == null || geo.isBlank()) {
            return "";
        }
        final var parts = geo.split("/");
        var iso = "";
        for (final var part : parts) {
            final var trimmed = part.strip();
            if (trimmed.length() == 2) {
                iso = trimmed.toLowerCase();
                break;
            }
        }
        if (iso.isEmpty() && parts.length > 1) {
            iso = parts[parts.length - 1].strip().toLowerCase();
        }
        if (iso.isEmpty()) {
            return "";
        }
        return "<span class=\"fi fi-" + escapeHtml(iso) + "\" title=\"" + escapeHtml(geo)
                + "\" style=\"font-size:1.1em;display:block\"></span>";
    }

    private static String buildNameHtml(final Host host) {
        final var sb = new StringBuilder();
        final var active = host.getActive();
        final var nickName = escapeHtml(host.getNickName());
        final var name = escapeHtml(host.getName());
        final var href = HOST_BASE_PATH + "/" + escapeHtml(host.getName());

        sb.append("<span style=\"white-space:nowrap\">");
        if (!active) {
            sb.append(
                    "<i class=\"bi bi-slash-circle-fill text-danger me-1\" title=\"Disabled\" style=\"font-size:0.78rem;\"></i>");
        }
        if (!active) {
            sb.append("<a href=\"").append(href).append(
                    "\" class=\"fw-semibold dest-list-link\" style=\"text-decoration:line-through;color:var(--bs-secondary-color)\">")
                    .append(nickName).append("</a>");
        } else {
            sb.append("<a href=\"").append(href).append("\" class=\"fw-semibold text-decoration-none dest-list-link\">")
                    .append(nickName).append("</a>");
        }
        if (!name.equals(nickName)) {
            sb.append("<code class=\"dest-page-id ms-1\" style=\"font-size:0.75rem;\" title=\"Host identifier\">")
                    .append(name).append("</code>");
        }
        // Type badge
        final var type = host.getType();
        if ("Dissemination".equals(type)) {
            sb.append(
                    "<span class=\"badge bg-secondary ms-1\" style=\"font-size:0.7rem;\" title=\"Dissemination\"><i class=\"bi bi-send-fill\"></i></span>");
        } else if ("Acquisition".equals(type)) {
            sb.append(
                    "<span class=\"badge bg-secondary ms-1\" style=\"font-size:0.7rem;\" title=\"Acquisition\"><i class=\"bi bi-cloud-download-fill\"></i></span>");
        } else if (type != null && !type.isBlank()) {
            sb.append("<span class=\"badge bg-secondary ms-1\" style=\"font-size:0.7rem;\">").append(escapeHtml(type))
                    .append("</span>");
        }
        // Transfer method badge
        final var method = host.getTransferMethodName();
        if (method != null && !method.isBlank()) {
            String methodTitle = "";
            try {
                final var tm = host.getTransferMethod();
                if (tm != null && tm.getComment() != null) {
                    methodTitle = escapeHtml(tm.getComment());
                }
            } catch (final Exception ignored) {
            }
            sb.append("<span class=\"badge bg-info text-dark ms-1\" style=\"font-size:0.7rem;\" title=\"")
                    .append(methodTitle).append("\"><i class=\"bi bi-hdd-network me-1\"></i>")
                    .append(escapeHtml(method)).append("</span>");
        }
        sb.append("</span>");
        // Comment below
        final var comment = host.getComment();
        if (comment != null && !comment.isBlank()) {
            sb.append("<div class=\"text-muted\" style=\"font-size:0.78rem;line-height:1.3;margin-top:1px;\">")
                    .append(escapeHtml(comment)).append("</div>");
        }
        return sb.toString();
    }

    private static String buildDestinationsHtml(final Host host) {
        Collection<Destination> destinations;
        try {
            destinations = host.getDestinations();
        } catch (final Exception e) {
            return "";
        }
        if (destinations == null || destinations.isEmpty()) {
            return "<span class=\"text-muted fst-italic\">none</span>";
        }
        final var count = destinations.size();
        if (count > 3) {
            return "<span class=\"badge bg-light text-secondary border\">" + count + " destinations</span>";
        }
        final var sb = new StringBuilder();
        for (final Destination d : destinations) {
            sb.append("<a href=\"").append(DEST_BASE_PATH).append("/").append(escapeHtml(d.getName()))
                    .append("\" class=\"badge bg-light text-secondary border text-decoration-none me-1\">")
                    .append(escapeHtml(d.getName())).append("</a>");
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String escapeHtml(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
                "&#39;");
    }

    private static int parseSafeInt(final String s, final int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (final Throwable _) {
            return fallback;
        }
    }

    private static void writeError(final HttpServletResponse response, final int draw, final String message) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            final ObjectNode err = MAPPER.createObjectNode();
            err.put("draw", draw);
            err.put("recordsTotal", 0);
            err.put("recordsFiltered", 0);
            err.putArray("data");
            err.put("error", message);
            MAPPER.writeValue(response.getWriter(), err);
        } catch (final Exception ignored) {
        }
    }
}
