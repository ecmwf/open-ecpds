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
 * Returns a DataTables-compatible JSON payload for the destination transfer list,
 * enabling server-side pagination without loading all rows into the page HTML.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDestinationTransferListJsonAction.
 *
 * Handles AJAX DataTables server-side requests for the destination transfer table. Returns JSON in the standard
 * DataTables server-side protocol format. Only the requested page is fetched from the database via SQL_CALC_FOUND_ROWS
 * + LIMIT, making it safe for tables with hundreds of thousands of rows.
 */
public class GetDestinationTransferListJsonAction extends PDSAction {

    /** Base paths for linked detail pages. */
    private static final String HOST_BASE_PATH = "/do/transfer/host";
    private static final String DATATRANSFER_BASE_PATH = "/do/transfer/data";

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Maps DataTables column index (0-based) to the DB sort column. SQL sort values defined in
     * getSortedDataTransfersByFilter.sql: 0=failed_time, 1=host, 2=sched_time, 3=start_time, 4=finish_time, 5=target,
     * 6=ts, 7=% (non-sortable — live value from TransferScheduler, not DB), 8=mbits, 9=size, 10=status, 11=priority.
     * Columns 12 (Actions) and 13 (Select) are non-sortable, defaulting to column 2 (scheduled time).
     */
    private static final int[] SORT_COLS = { 0, 1, 2, 3, 4, 5, 6, 2, 8, 11, 9, 10, 2, 2 };

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var destinationName = Util.getValue(request, "destinationName", "");
        final var disseminationStream = Util.getValue(request, "disseminationStream", "");
        final var dataStream = Util.getValue(request, "dataStream", "");
        final var dataTime = Util.getValue(request, "dataTime", "");
        final var status = Util.getValue(request, "status", "");
        final var fileNameSearch = Util.getValue(request, "fileNameSearch", "");
        final var dateParam = Util.getValue(request, "date", "");

        // Parse date: only set if defined and not "All"; otherwise, leave null (fetch all dates)
        final var iso = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        if (!dateParam.isEmpty() && !"All".equalsIgnoreCase(dateParam)) {
            try {
                date = iso.parse(dateParam);
            } catch (final ParseException _) {
            }
        }
        // If date is null ("All" or not defined), leave as null to fetch all dates

        // Compute permissions server-side
        boolean hasAccess = true;
        try {
            hasAccess = user.hasAccess(getResource(request, "datatransfer.basepath"));
        } catch (final Exception _) {
        }
        boolean memberState = false;
        try {
            memberState = !user.hasAccess(getResource(request, "nonmemberstate.basepath"));
        } catch (final Exception _) {
        }
        // Column visibility is already gated by the JSP auth tag (ecpdsCanHandleQueue).
        // Action security is enforced by the individual action handlers on submit.

        // Build DataBaseCursor from DataTables params
        var start = 0;
        var length = 25;
        try {
            start = Integer.parseInt(request.getParameter("start"));
        } catch (final Throwable _) {
        }
        try {
            length = Integer.parseInt(request.getParameter("length"));
            if (length < 1) {
                length = 25;
            }
        } catch (final Throwable _) {
        }
        var colIdx = parseSafeInt(request.getParameter("order[0][column]"), 2);
        if (colIdx < 0 || colIdx >= SORT_COLS.length) {
            colIdx = 2;
        }
        final var dbSortCol = SORT_COLS[colIdx];
        final var dir = request.getParameter("order[0][dir]");
        // DB order: "1" = ascending, "2" = descending
        final var dbOrder = "asc".equalsIgnoreCase(dir) ? "1" : "2";
        final var cursor = new DataBaseCursor(String.valueOf(dbSortCol), dbOrder, start, start + length);

        Collection<DataTransfer> transfers;
        String queryError = null;
        try {
            transfers = (Collection<DataTransfer>) (Collection<?>) DataTransferHome.findByFilter(destinationName,
                    disseminationStream, dataStream, dataTime, status, hasAccess, fileNameSearch, date, cursor);
        } catch (final Exception e) {
            transfers = new ArrayList<>(0);
            queryError = e.getMessage();
        }

        final var recordsTotal = Util.getCollectionSizeFrom(transfers);
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        if (queryError != null) {
            root.put("error", queryError);
        }
        // Read which transfers are already selected in the server-side session
        final var selectedIdsNode = root.putArray("selectedIds");
        DetailActionForm daf = null;
        try {
            daf = (DetailActionForm) request.getSession().getAttribute("destinationDetailActionForm");
            if (daf != null) {
                daf.setId(destinationName); // ensure cache targets the right destination
            }
        } catch (final Exception _) {
        }
        root.put("totalSelected", daf != null ? daf.getSelectedTransfersCount() : 0);
        // Check once whether this destination has at least one active dissemination host.
        // If not, the per-row Requeue action is disabled to prevent NoHosts failures.
        var hasActiveDissHosts = true;
        try {
            hasActiveDissHosts = DestinationHome.findByPrimaryKey(destinationName).getHasActiveDisseminationHosts();
        } catch (final Exception _) {
            // fail open: if we cannot determine host availability, leave requeue enabled
        }
        final var data = root.putArray("data");
        for (final DataTransfer dt : transfers) {
            final var id = dt.getId();
            if (daf != null && "on".equals(daf.getSelectedTransfer(id))) {
                selectedIdsNode.add(id);
            }
            final var row = data.addArray();
            row.add(buildErrHtml(dt));
            row.add(buildHostHtml(dt));
            row.add(buildScheduledTimeHtml(dt));
            row.add(buildStartTimeHtml(dt));
            row.add(buildFinishTimeHtml(dt));
            row.add(buildTargetHtml(dt));
            row.add(buildTimeStepHtml(dt));
            row.add(buildProgressHtml(dt));
            row.add(buildRateHtml(dt));
            row.add(buildSizeHtml(dt));
            row.add(buildStatusHtml(dt, memberState));
            row.add(String.valueOf(dt.getPriority()));
            row.add(buildActionsHtml(dt, hasActiveDissHosts));
            row.add(buildSelectHtml(dt));
        }
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building transfer list: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // HTML column builders
    // -------------------------------------------------------------------------

    private static String buildErrHtml(final DataTransfer dt) {
        if (dt.getFailedTime() != null) {
            return "<i class=\"bi bi-x-circle-fill text-danger\" title=\"Transfer failed\"></i>";
        }
        return "<i class=\"bi bi-check-circle-fill text-success\" title=\"No errors\"></i>";
    }

    private static String buildHostHtml(final DataTransfer dt) {
        final var nickName = dt.getHostNickName();
        if (nickName == null || nickName.isBlank()) {
            return "<i class=\"bi bi-dash text-muted\" title=\"Not transferred to remote host\"></i>";
        }
        final var hostName = escapeHtml(dt.getHostName());
        final var serverName = dt.getTransferServerName();
        final var titleText = serverName != null && !serverName.isBlank()
                ? escapeHtml(nickName) + " (via " + escapeHtml(serverName) + ")" : escapeHtml(nickName);
        return "<a href=\"" + HOST_BASE_PATH + "/" + hostName + "\" title=\"" + titleText
                + "\" class=\"text-decoration-none\">" + escapeHtml(nickName) + "</a>";
    }

    private static String buildScheduledTimeHtml(final DataTransfer dt) {
        final var t = dt.getScheduledTime();
        return t != null ? Format.formatTime("MMM dd HH:mm:ss", t.getTime()) : "";
    }

    private static String buildStartTimeHtml(final DataTransfer dt) {
        final var t = dt.getStartTime();
        return t != null ? Format.formatTime("MMM dd HH:mm:ss", t.getTime())
                : "<i class=\"bi bi-dash text-muted\" title=\"Not started\"></i>";
    }

    private static String buildFinishTimeHtml(final DataTransfer dt) {
        final var t = dt.getRealFinishTime();
        return t != null ? Format.formatTime("MMM dd HH:mm:ss", t.getTime())
                : "<i class=\"bi bi-dash text-muted\" title=\"Not finished\"></i>";
    }

    private static String buildTargetHtml(final DataTransfer dt) {
        final var id = escapeHtml(dt.getId());
        final var target = escapeHtml(dt.getTarget());
        final var size = escapeHtml(dt.getFormattedSize());
        final var isRed = dt.getExpired() || dt.getDeleted();
        final var cls = isRed ? "text-danger" : "text-decoration-none";
        return "<a href=\"" + DATATRANSFER_BASE_PATH + "/" + id + "\" class=\"" + cls + "\" title=\"Size: " + size
                + "\">" + target + "</a>";
    }

    private static String buildTimeStepHtml(final DataTransfer dt) {
        try {
            return String.valueOf(dt.getDataFile().getTimeStep());
        } catch (final Exception _) {
            return "";
        }
    }

    private static String buildProgressHtml(final DataTransfer dt) {
        try {
            return String.valueOf(dt.getProgress());
        } catch (final Exception _) {
            return "";
        }
    }

    private static String buildRateHtml(final DataTransfer dt) {
        try {
            if (dt.getTransferRate() == 0) {
                return "<i class=\"bi bi-dash text-muted\" title=\"Not applicable\"></i>";
            }
            final var mbit = dt.getFormattedTransferRateInMBitsPerSeconds();
            final var rate = escapeHtml(dt.getFormattedTransferRate());
            return "<span title=\"Rate: " + rate + "\">" + String.format("%.3f", mbit) + "</span>";
        } catch (final Exception _) {
            return "";
        }
    }

    private static String buildSizeHtml(final DataTransfer dt) {
        try {
            return escapeHtml(Format.formatShortSize(dt.getSize()));
        } catch (final Exception _) {
            return "";
        }
    }

    private static String buildStatusHtml(final DataTransfer dt, final boolean memberState) {
        String statusText;
        try {
            statusText = memberState ? dt.getMemberStateDetailedStatus() : dt.getDetailedStatus();
        } catch (final Exception _) {
            statusText = dt.getStatusCode();
        }
        if (statusText == null) {
            statusText = "";
        }
        final var escaped = escapeHtml(statusText);
        // Extract base status (before any "-username" suffix) for colour selection and display
        final var base = statusText.contains("-") ? statusText.substring(0, statusText.indexOf('-')).trim()
                : statusText.trim();
        final var baseEscaped = escapeHtml(base);
        if (dt.getExpired() && dt.getDeleted()) {
            final var expiry = dt.getExpiryDate();
            final var expStr = expiry != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiry)
                    : "";
            return "<span class=\"badge bg-danger\" title=\"Data Transfer expired on " + escapeHtml(expStr) + "\">"
                    + baseEscaped + "</span>";
        }
        if (dt.getDeleted()) {
            return "<span class=\"badge bg-danger\" title=\"Data Transfer deleted\">" + baseEscaped + "</span>";
        }
        final String cls;
        switch (base) {
        case "Done":
            cls = "badge bg-success";
            break;
        case "Transferring":
        case "Fetching":
        case "Arriving":
            cls = "badge bg-primary";
            break;
        case "Queued":
        case "Preset":
        case "StandBy":
        case "ReQueued":
            cls = "badge bg-warning text-dark";
            break;
        case "Failed":
            cls = "badge bg-danger";
            break;
        case "Stopped":
        case "Interrupted":
        default:
            cls = "badge bg-secondary";
            break;
        }
        // Show only base status in badge; full text (including any "-username" suffix) in tooltip
        final boolean hasUid = statusText.contains("-");
        final String uid = hasUid ? statusText.substring(statusText.indexOf('-') + 1).trim() : null;
        final String tooltip = hasUid ? escapeHtml(base + " \u00b7 " + uidRelation(dt.getStatusCode()) + uid) : escaped;
        final String userIcon = hasUid ? " <i class=\"bi bi-person-fill\" style=\"font-size:0.75em;\"></i>" : "";
        final String style = hasUid ? " style=\"display:inline-flex;align-items:center;gap:3px;\"" : "";
        return "<span class=\"" + cls + "\"" + style + " title=\"" + tooltip + "\">" + baseEscaped + userIcon
                + "</span>";
    }

    /**
     * Returns the appropriate preposition for the uid tooltip depending on whether the status represents a direct user
     * action ("by") or an outcome triggered by a prior user action ("initiated by").
     */
    private static String uidRelation(final String statusCode) {
        if (statusCode == null) {
            return "by ";
        }
        switch (statusCode) {
        case "RETR": // ReQueued
        case "HOLD": // Standby
        case "STOP": // Stopped
        case "SCHE": // Scheduled
            return "by ";
        default: // EXEC, DONE, FAIL, WAIT, INTR, FETC, INIT …
            return "initiated by ";
        }
    }

    private static String buildActionsHtml(final DataTransfer dt, final boolean hasActiveDissHosts) {
        final var id = escapeHtml(dt.getId());
        if (dt.getDeleted()) {
            return "<span class=\"text-muted fst-italic\" title=\"Data Transfer deleted\">[deleted]</span>";
        }
        if (dt.getExpired()) {
            final var expiry = dt.getExpiryDate();
            final var expStr = expiry != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiry)
                    : "";
            return "<span class=\"text-muted fst-italic\" title=\"Data Transfer expired on " + escapeHtml(expStr)
                    + "\">[expired]</span>";
        }
        final var sb = new StringBuilder();
        sb.append("<span class=\"d-flex gap-1 align-items-center\">");
        if (dt.getCanBeDownloaded()) {
            sb.append("<a href=\"javascript:transferChange('download','").append(id).append("')\" title=\"Download ")
                    .append(escapeHtml(dt.getTarget())).append("\">").append("<i class=\"bi bi-download\"></i></a>");
        }
        if (dt.getCanBeRequeued()) {
            if (hasActiveDissHosts) {
                sb.append("<a href=\"javascript:transferChange('requeue','").append(id)
                        .append("')\" title=\"Requeue\">").append("<i class=\"bi bi-arrow-repeat\"></i></a>");
            } else {
                sb.append(
                        "<span class=\"text-muted\" data-bs-toggle=\"tooltip\" title=\"Requeue unavailable: no active dissemination hosts\">")
                        .append("<i class=\"bi bi-arrow-repeat\"></i></span>");
            }
        }
        if (dt.getCanBeStopped()) {
            sb.append("<a href=\"javascript:transferChange('stop','").append(id).append("')\" title=\"Stop\">")
                    .append("<i class=\"bi bi-stop-circle text-danger\"></i></a>");
        }
        final var priority = dt.getPriority();
        if (priority <= 0) {
            sb.append("<span class=\"text-muted\" title=\"Already at highest priority (0)\">")
                    .append("<i class=\"bi bi-arrow-up\"></i></span>");
        } else {
            sb.append("<a href=\"javascript:transferChange('increaseTransferPriority','").append(id)
                    .append("')\" title=\"Increase priority\">").append("<i class=\"bi bi-arrow-up\"></i></a>");
        }
        if (priority >= 99) {
            sb.append("<span class=\"text-muted\" title=\"Already at lowest priority (99)\">")
                    .append("<i class=\"bi bi-arrow-down\"></i></span>");
        } else {
            sb.append("<a href=\"javascript:transferChange('decreaseTransferPriority','").append(id)
                    .append("')\" title=\"Decrease priority\">").append("<i class=\"bi bi-arrow-down\"></i></a>");
        }
        sb.append("</span>");
        return sb.toString();
    }

    private static String buildSelectHtml(final DataTransfer dt) {
        if (dt.getExpired()) {
            return "";
        }
        final var id = escapeHtml(dt.getId());
        return "<span class=\"star-select\" data-transfer-id=\"" + id + "\"" + " onclick=\"select(this,'" + id
                + "')\" style=\"cursor:pointer\" title=\"Select transfer\">"
                + "<i class=\"bi bi-star fs-6\"></i></span>";
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
