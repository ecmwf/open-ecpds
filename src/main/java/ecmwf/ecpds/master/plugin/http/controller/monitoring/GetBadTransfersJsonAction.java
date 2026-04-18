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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the outstanding/unsuccessful
 * transfers list, enabling server-side pagination without loading all rows into
 * Java memory.
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
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * Handles AJAX DataTables server-side requests for the outstanding transfers page. Returns JSON in the standard
 * DataTables server-side protocol format. Only the requested page of records is fetched from the database.
 */
public class GetBadTransfersJsonAction extends PDSAction {

    /** Base path for transfer detail pages. */
    private static final String TRANSFER_BASE_PATH = "/do/transfer/data";

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws TransferException, ECMWFActionFormException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var destinationName = ECMWFActionForm.getPathParameter(mapping, request, 0);

        // Map DataTables column index to SQL sort index:
        // 0=Host(HOS_NAME), 1=Target(DAT_TARGET), 2=Status(STA_CODE),
        // 3=% (not sortable), 4=B/s (not sortable), 5=Priority, 6=Comment
        final var orderColParam = request.getParameter("order[0][column]");
        final var orderDirParam = request.getParameter("order[0][dir]");
        int sqlSort = 0;
        try {
            sqlSort = switch (Integer.parseInt(orderColParam)) {
            case 1 -> 1; // target
            case 2 -> 2; // status code
            case 5 -> 3; // priority
            case 6 -> 4; // comment
            default -> 0; // host (default)
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
        // Sanitize search term: strip single quotes to prevent SQL injection
        final var rawSearch = request.getParameter("search[value]");
        final var search = rawSearch != null ? rawSearch.replace("'", "").replace("\\", "").trim() : "";
        final var cursor = new DataBaseCursor(String.valueOf(sqlSort), order, start, start + length, search);

        Collection<DataTransfer> transfers;
        try {
            final var destination = DestinationHome.findByPrimaryKey(destinationName);
            transfers = DataTransferHome.findSortedBadByDestination(destination, cursor);
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

    // -------------------------------------------------------------------------
    // HTML column builders
    // -------------------------------------------------------------------------

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
        try {
            return escapeHtml(transfer.getFormattedStatus());
        } catch (final Exception e) {
            return escapeHtml(transfer.getStatusCode());
        }
    }

    private static String buildProgressHtml(final DataTransfer transfer) {
        try {
            final var sent = transfer.getSent();
            final var size = transfer.getSize();
            if (size <= 0) {
                return "0";
            }
            final var pct = (int) (sent * 100L / size);
            return String.valueOf(Math.min(pct, 100));
        } catch (final Exception e) {
            return "0";
        }
    }

    private static String buildRateHtml(final DataTransfer transfer) {
        try {
            final var sent = transfer.getSent();
            final var duration = transfer.getDuration();
            return escapeHtml(Format.formatRate(sent, duration));
        } catch (final Exception e) {
            return "0";
        }
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
