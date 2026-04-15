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

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the destination list,
 * enabling server-side pagination without loading all rows into the page HTML.
 * Since the underlying DB query does not support cursor-based pagination,
 * this action fetches all matching destinations and slices the result in memory.
 * Pass length=-1 to retrieve all rows (used by the split-view toggle).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDestinationListJsonAction.
 *
 * Handles AJAX DataTables server-side requests for the destination list page.
 */
public class GetDestinationListJsonAction extends PDSAction {

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
        final var start = parseSafeInt(request.getParameter("start"), 0);
        final var lengthParam = parseSafeInt(request.getParameter("length"), 25);
        final var allRows = lengthParam == -1;
        final var length = allRows ? Integer.MAX_VALUE : Math.max(1, lengthParam);

        final var search = Util.getValue(request, "destinationSearch", "");
        final var sortDirection = Util.getValue(request, "sortDirection", "asc");
        final var aliases = Util.getValue(request, "aliases", "All");
        final var status = Util.getValue(request, "destinationStatus", "All Status");
        final var type = Util.getValue(request, "destinationType", "-1");
        final var filter = Util.getValue(request, "destinationFilter", "All");

        // DataTables server-side sort params take priority over legacy sortDirection
        final var orderColParam = request.getParameter("order[0][column]");
        final var orderDirParam = request.getParameter("order[0][dir]");
        final int orderCol = orderColParam != null ? parseSafeInt(orderColParam, 1) : 1;
        final boolean ascending = orderDirParam != null ? "asc".equals(orderDirParam) : "asc".equals(sortDirection);

        Collection<Destination> allDestinations;
        try {
            // DB-level sort only applies to the name column (1); other columns sort in-memory
            allDestinations = DestinationHome.findByUser(user, search, aliases, orderCol != 1 || ascending,
                    StatusFactory.getDestinationStatusCode(status), GetDestinationAction.getDestinationTypeIds(type),
                    filter);
        } catch (final TransferException e) {
            allDestinations = new ArrayList<>(0);
        }

        // In-memory sort for Status (col 2) and Aliases (col 3)
        if (orderCol == 2 || orderCol == 3) {
            final var list = allDestinations instanceof List ? (List<Destination>) allDestinations
                    : new ArrayList<>(allDestinations);
            if (orderCol == 2) {
                list.sort((a, b) -> {
                    final var va = a.getFormattedStatus() != null ? a.getFormattedStatus() : "";
                    final var vb = b.getFormattedStatus() != null ? b.getFormattedStatus() : "";
                    return ascending ? va.compareTo(vb) : vb.compareTo(va);
                });
            } else {
                list.sort((a, b) -> {
                    final int ca = safeAliasCount(a);
                    final int cb = safeAliasCount(b);
                    return ascending ? Integer.compare(ca, cb) : Integer.compare(cb, ca);
                });
            }
            allDestinations = list;
        }

        final var recordsTotal = allDestinations.size();
        final List<Destination> page;
        if (allRows) {
            page = allDestinations instanceof List ? (List<Destination>) allDestinations
                    : new ArrayList<>(allDestinations);
        } else {
            final var list = allDestinations instanceof List ? (List<Destination>) allDestinations
                    : new ArrayList<>(allDestinations);
            final var from = Math.min(start, list.size());
            final var to = Math.min(start + length, list.size());
            page = list.subList(from, to);
        }

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        final var data = root.putArray("data");
        for (final Destination d : page) {
            final var row = data.addArray();
            row.add(buildFlagHtml(d));
            row.add(buildNameHtml(d));
            row.add(buildStatusHtml(d));
            row.add(buildAliasesHtml(d));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building destination list: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // HTML column builders
    // -------------------------------------------------------------------------

    private static String buildFlagHtml(final Destination d) {
        final var iso = d.getCountryIso();
        if (iso == null || iso.isBlank()) {
            return "";
        }
        final var liso = iso.toLowerCase();
        String countryName = "";
        try {
            final var c = d.getCountry();
            if (c != null && c.getName() != null) {
                countryName = escapeHtml(c.getName());
            }
        } catch (final Exception ignored) {
        }
        return "<span class=\"fi fi-" + liso + "\" title=\"" + countryName + "\" data-iso=\"" + liso + "\" data-name=\""
                + countryName + "\" style=\"font-size:1.1em;display:block\"></span>";
    }

    private static String buildNameHtml(final Destination d) {
        final var sb = new StringBuilder();
        final var active = d.getActive();
        final var id = escapeHtml(d.getId());
        final var href = DEST_BASE_PATH + "/" + id;

        sb.append("<span style=\"white-space:nowrap\">");
        if (!active) {
            sb.append(
                    "<i class=\"bi bi-slash-circle-fill text-danger me-1\" title=\"Disabled\" style=\"font-size:0.78rem;\"></i>");
        }
        if (!active) {
            sb.append("<a href=\"").append(href).append(
                    "\" class=\"fw-semibold dest-list-link\" style=\"text-decoration:line-through;color:var(--bs-secondary-color)\">")
                    .append(id).append("</a>");
        } else {
            sb.append("<a href=\"").append(href).append("\" class=\"fw-semibold text-decoration-none dest-list-link\">")
                    .append(id).append("</a>");
        }
        // Type badge
        final var typeText = d.getTypeText();
        if (typeText != null && !typeText.isBlank()) {
            switch (typeText) {
            case "Gold":
                sb.append(
                        "<span class=\"dest-page-type dest-type-gold ms-1\"><i class=\"bi bi-trophy-fill\"></i> Gold</span>");
                break;
            case "Silver":
                sb.append(
                        "<span class=\"dest-page-type dest-type-silver ms-1\"><i class=\"bi bi-award-fill\"></i> Silver</span>");
                break;
            case "Bronze":
                sb.append(
                        "<span class=\"dest-page-type dest-type-bronze ms-1\"><i class=\"bi bi-award\"></i> Bronze</span>");
                break;
            case "Basic":
                sb.append(
                        "<span class=\"dest-page-type dest-type-basic ms-1\"><i class=\"bi bi-patch-check\"></i> Basic</span>");
                break;
            default:
                sb.append("<span class=\"dest-page-type ms-1\">").append(escapeHtml(typeText)).append("</span>");
            }
        }
        // Monitor visibility
        if (!d.getShowInMonitors()) {
            sb.append(
                    "<i class=\"bi bi-eye-slash text-muted ms-1\" title=\"Not shown in Monitor Display\" style=\"font-size:0.78rem;\"></i>");
        }
        // Compression icon
        final var filterName = d.getFilterName();
        if (filterName != null && !filterName.isBlank() && !"none".equals(filterName)) {
            sb.append(buildCompressionIcon(filterName));
        }
        sb.append("</span>");
        // Comment
        final var comment = d.getComment();
        if (comment != null && !comment.isBlank()) {
            sb.append("<div class=\"text-muted\" style=\"font-size:0.78rem;line-height:1.3;margin-top:1px;\">")
                    .append(escapeHtml(comment)).append("</div>");
        }
        return sb.toString();
    }

    private static String buildStatusHtml(final Destination d) {
        final var status = d.getFormattedStatus();
        if (status == null) {
            return "";
        }
        final var base = status.contains("-") ? status.substring(0, status.indexOf('-')) : status;
        final String cls;
        switch (base.trim()) {
        case "Running":
            cls = "badge bg-success";
            break;
        case "Waiting":
        case "Retrying":
        case "Interrupted":
            cls = "badge bg-warning text-dark";
            break;
        case "Restarting":
        case "Resending":
            cls = "badge bg-info text-dark";
            break;
        case "Idle":
            cls = "badge bg-secondary";
            break;
        default:
            cls = "badge bg-danger";
        }
        return "<span class=\"" + cls + "\" title=\"" + escapeHtml(status) + "\">" + escapeHtml(status) + "</span>";
    }

    private static String buildAliasesHtml(final Destination d) {
        Collection<Destination> aliases;
        try {
            aliases = d.getAliases();
        } catch (final Exception e) {
            return "";
        }
        if (aliases == null || aliases.isEmpty()) {
            return "<span class=\"text-muted fst-italic\" style=\"font-size:0.8rem;\">none</span>";
        }
        final var count = aliases.size();
        if (count >= 3) {
            return "<span class=\"badge bg-light text-secondary border\">" + count + " aliases</span>";
        }
        final var sb = new StringBuilder();
        for (final Destination alias : aliases) {
            final var aid = escapeHtml(alias.getId());
            sb.append("<a href=\"").append(DEST_BASE_PATH).append("/").append(aid)
                    .append("\" class=\"badge bg-light text-secondary border text-decoration-none me-1\" title=\"")
                    .append(aid).append(" is an alias for ").append(escapeHtml(d.getId())).append("\">").append(aid)
                    .append("</a>");
        }
        return sb.toString();
    }

    private static String buildCompressionIcon(final String name) {
        final String icon;
        switch (name) {
        case "zip":
            icon = "bi-file-zip";
            break;
        case "gzip":
            icon = "bi-file-earmark-zip";
            break;
        case "lzma":
            icon = "bi-box-seam";
            break;
        case "bzip2a":
            icon = "bi-archive";
            break;
        case "lbzip2":
            icon = "bi-cpu";
            break;
        case "lz4":
            icon = "bi-lightning";
            break;
        case "snappy":
            icon = "bi-lightning-charge";
            break;
        case "zstd":
            icon = "bi-stack";
            break;
        default:
            icon = "bi-file-zip";
        }
        return "<i class=\"bi " + icon + " text-muted\" title=\"" + escapeHtml(name)
                + "\" style=\"font-size:0.85rem\"></i>";
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

    private static int safeAliasCount(final Destination d) {
        try {
            final var a = d.getAliases();
            return a != null ? a.size() : 0;
        } catch (final Exception e) {
            return 0;
        }
    }
}
