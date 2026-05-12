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

        Collection<Destination> page;
        int recordsTotal = 0;
        String queryError = null;
        try {
            page = DestinationHome.findByUser(user, search, aliases, orderCol, ascending, start, lengthParam,
                    StatusFactory.getDestinationStatusCode(status), GetDestinationAction.getDestinationTypeIds(type),
                    filter);
            recordsTotal = DestinationHome.countByUser(user, search, aliases,
                    StatusFactory.getDestinationStatusCode(status), GetDestinationAction.getDestinationTypeIds(type),
                    filter);
        } catch (final TransferException e) {
            page = new ArrayList<>(0);
            queryError = e.getMessage();
        }

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        if (queryError != null) {
            root.put("queryError", queryError);
        }
        final var data = root.putArray("data");
        for (final Destination d : page) {
            final var row = data.addArray();
            row.add(buildFlagHtml(d));
            row.add(buildNameHtml(d));
            row.add(escapeHtml(d.getId()));
            row.add(buildStatusHtml(d));
            row.add(buildAliasesHtml(d));
            row.add(buildCategoryHtml(d));
            row.add(buildCompressionHtml(d));
            row.add(buildEnabledHtml(d));
            row.add(buildAcquisitionHtml(d));
            row.add(buildShowInMonitorsHtml(d));
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
            return "<span class=\"badge bg-body-tertiary text-muted border fst-italic\">none</span>";
        }
        if (aliases.size() == 1) {
            final var aid = escapeHtml(aliases.iterator().next().getId());
            return "<a href=\"" + DEST_BASE_PATH + "/" + aid
                    + "\" class=\"badge bg-body-tertiary text-secondary border text-decoration-none\" title=\"" + aid
                    + " is an alias for " + escapeHtml(d.getId()) + "\">" + aid + "</a>";
        }
        // Multiple aliases — Bootstrap dropdown
        final var sb = new StringBuilder();
        sb.append("<div class=\"dropdown\">").append(
                "<button class=\"badge bg-body-tertiary text-secondary border text-decoration-none dropdown-toggle\" ")
                .append("style=\"cursor:pointer;\" ")
                .append("data-bs-toggle=\"dropdown\" data-bs-auto-close=\"true\" data-bs-boundary=\"viewport\">")
                .append(aliases.size()).append(" aliases").append("</button>")
                .append("<ul class=\"dropdown-menu\" style=\"max-height:300px;overflow-y:auto;\">");
        for (final Destination alias : aliases) {
            final var aid = escapeHtml(alias.getId());
            sb.append("<li><a class=\"dropdown-item\" href=\"").append(DEST_BASE_PATH).append("/").append(aid)
                    .append("\" title=\"").append(aid).append(" is an alias for ").append(escapeHtml(d.getId()))
                    .append("\">").append(aid).append("</a></li>");
        }
        sb.append("</ul></div>");
        return sb.toString();
    }

    private static String buildCategoryHtml(final Destination d) {
        final var typeText = d.getTypeText();
        if (typeText == null || typeText.isBlank()) {
            return "";
        }
        return switch (typeText) {
        case "Gold" -> "<span class=\"dest-page-type dest-type-gold\"><i class=\"bi bi-trophy-fill\"></i> Gold</span>";
        case "Silver" -> "<span class=\"dest-page-type dest-type-silver\"><i class=\"bi bi-award-fill\"></i> Silver</span>";
        case "Bronze" -> "<span class=\"dest-page-type dest-type-bronze\"><i class=\"bi bi-award\"></i> Bronze</span>";
        case "Basic" -> "<span class=\"dest-page-type dest-type-basic\"><i class=\"bi bi-patch-check\"></i> Basic</span>";
        default -> "<span class=\"dest-page-type\">" + escapeHtml(typeText) + "</span>";
        };
    }

    private static String buildCompressionHtml(final Destination d) {
        final var f = d.getFilterName();
        if (f == null || f.isBlank() || "none".equals(f)) {
            return "";
        }
        return buildCompressionIcon(f) + " <small class=\"text-muted\">" + escapeHtml(f) + "</small>";
    }

    private static String buildEnabledHtml(final Destination d) {
        return d.getActive()
                ? "<i class=\"bi bi-check-circle-fill text-success\" title=\"Enabled\" style=\"font-size:0.9rem;\"></i>"
                : "<i class=\"bi bi-x-circle-fill text-danger\" title=\"Disabled\" style=\"font-size:0.9rem;\"></i>";
    }

    private static String buildAcquisitionHtml(final Destination d) {
        return d.getAcquisition()
                ? "<i class=\"bi bi-check-circle-fill text-success\" title=\"Acquisition\" style=\"font-size:0.9rem;\"></i>"
                : "<i class=\"bi bi-dash text-muted\" style=\"font-size:0.9rem;\"></i>";
    }

    private static String buildShowInMonitorsHtml(final Destination d) {
        return d.getShowInMonitors()
                ? "<i class=\"bi bi-check-circle-fill text-success\" title=\"Shown in monitors\" style=\"font-size:0.9rem;\"></i>"
                : "<i class=\"bi bi-dash text-muted\" style=\"font-size:0.9rem;\"></i>";
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
}
