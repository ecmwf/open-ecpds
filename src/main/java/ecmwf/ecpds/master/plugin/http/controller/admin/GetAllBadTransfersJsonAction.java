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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the admin requeue page,
 * showing bad transfers across ALL destinations with server-side pagination.
 * Only the requested page of records is fetched from the database — no full
 * table scan into Java memory.
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

import ecmwf.common.database.DataBaseCursor;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.model.users.User;

/**
 * Handles AJAX DataTables server-side requests for the admin requeue page. Returns JSON in the standard DataTables
 * server-side protocol. Only the requested page of records is fetched from the database.
 */
public class GetAllBadTransfersJsonAction extends PDSAction {

    /** Base paths for detail pages. */
    private static final String TRANSFER_BASE_PATH = "/do/transfer/data";
    private static final String DESTINATION_BASE_PATH = "/do/transfer/destination";

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** {@inheritDoc} */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws TransferException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);

        // Map DataTables column index to SQL sort index:
        // 0=Destination, 1=Host, 2=Target, 3=Status, 4=%(n/a), 5=B/s(n/a), 6=Priority, 7=Comment
        final var orderColParam = request.getParameter("order[0][column]");
        final var orderDirParam = request.getParameter("order[0][dir]");
        int sqlSort = 0;
        try {
            sqlSort = switch (Integer.parseInt(orderColParam)) {
            case 1 -> 1; // host
            case 2 -> 2; // target
            case 3 -> 3; // status
            case 6 -> 4; // priority
            case 7 -> 5; // comment
            default -> 0; // destination (default)
            };
        } catch (final Throwable _) {
            // ignore
        }
        final var order = "asc".equalsIgnoreCase(orderDirParam) ? "1" : "2";
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
        final var rawSearch = request.getParameter("search[value]");
        final var search = rawSearch != null ? rawSearch.replace("'", "").replace("\\", "").trim() : "";
        final var cursor = new DataBaseCursor(String.valueOf(sqlSort), order, start, start + length, search);

        Collection<DataTransfer> transfers;
        try {
            transfers = DataTransferHome.findSortedBad(cursor);
        } catch (final Exception e) {
            transfers = new ArrayList<>(0);
        }
        final var recordsTotal = Util.getCollectionSizeFrom(transfers);
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        final var data = root.putArray("data");
        for (final DataTransfer transfer : transfers) {
            final var row = data.addArray();
            row.add(buildDestinationHtml(transfer));
            row.add(buildHostHtml(transfer));
            row.add(buildTargetHtml(transfer));
            row.add(buildStatusHtml(transfer));
            row.add(buildProgressHtml(transfer));
            row.add(buildRateHtml(transfer));
            row.add(String.valueOf(transfer.getPriority()));
            row.add(escapeHtml(transfer.getComment()));
        }
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building bad transfers list: " + e.getMessage());
        }
        return null;
    }

    private static String buildDestinationHtml(final DataTransfer transfer) {
        final var name = escapeHtml(transfer.getDestinationName());
        if (name.isEmpty()) {
            return "<span class=\"text-muted fst-italic\">unknown</span>";
        }
        return "<a href=\"" + DESTINATION_BASE_PATH + "/" + name + "\" class=\"text-decoration-none\">" + name + "</a>";
    }

    private static String buildHostHtml(final DataTransfer transfer) {
        final var nickName = escapeHtml(transfer.getHostNickName());
        if (nickName.isEmpty()) {
            return "<span class=\"text-muted fst-italic\">unknown</span>";
        }
        return nickName;
    }

    private static String buildTargetHtml(final DataTransfer transfer) {
        final var id = transfer.getId();
        final var target = escapeHtml(transfer.getTarget());
        return "<a href=\"" + TRANSFER_BASE_PATH + "/" + id + "\" class=\"text-decoration-none\">" + target + "</a>";
    }

    private static String buildStatusHtml(final DataTransfer transfer) {
        String statusText;
        try {
            statusText = transfer.getDetailedStatus();
        } catch (final Exception e) {
            statusText = transfer.getStatusCode();
        }
        if (statusText == null) {
            statusText = "";
        }
        final var escaped = escapeHtml(statusText);
        // Extract base status (before any "-username" suffix) for colour selection and display, same convention
        // as the Destination transfer list (see GetDestinationTransferListJsonAction.buildStatusHtml).
        final var base = statusText.contains("-") ? statusText.substring(0, statusText.indexOf('-')).trim()
                : statusText.trim();
        final var baseEscaped = escapeHtml(base);
        if (transfer.getExpired() && transfer.getDeleted()) {
            final var expiry = transfer.getExpiryDate();
            final var expStr = expiry != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expiry)
                    : "";
            return "<span class=\"badge bg-danger\" title=\"Data Transfer expired on " + escapeHtml(expStr) + "\">"
                    + baseEscaped + "</span>";
        }
        if (transfer.getDeleted()) {
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
        final String tooltip = hasUid ? escapeHtml(base + " \u00b7 " + uidRelation(transfer.getStatusCode()) + uid)
                : escaped;
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

    private static String buildProgressHtml(final DataTransfer transfer) {
        try {
            final var sent = transfer.getSent();
            final var size = transfer.getSize();
            if (size <= 0) {
                return "0";
            }
            return String.valueOf(Math.min((int) (sent * 100L / size), 100));
        } catch (final Exception e) {
            return "0";
        }
    }

    private static String buildRateHtml(final DataTransfer transfer) {
        try {
            return escapeHtml(Format.formatRate(transfer.getSent(), transfer.getDuration()));
        } catch (final Exception e) {
            return "0";
        }
    }

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
