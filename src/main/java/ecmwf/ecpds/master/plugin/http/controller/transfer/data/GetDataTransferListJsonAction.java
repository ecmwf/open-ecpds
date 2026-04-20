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

package ecmwf.ecpds.master.plugin.http.controller.transfer.data;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the data transfer list,
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
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDataTransferListJsonAction.
 *
 * Handles AJAX DataTables server-side requests for the data transfer list page. Returns JSON in the standard DataTables
 * server-side protocol format. Only the requested page is fetched from the database via SQL_CALC_FOUND_ROWS + LIMIT,
 * making it safe for tables with hundreds of thousands of rows.
 */
public class GetDataTransferListJsonAction extends PDSAction {

    /** Base paths for linked detail pages. */
    private static final String DESTINATION_BASE_PATH = "/do/transfer/destination";
    private static final String HOST_BASE_PATH = "/do/transfer/host";
    private static final String DATATRANSFER_BASE_PATH = "/do/transfer/data";

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Maps DataTables column index (0-based) to the DB sort column. SQL sort values defined in
     * getSortedDataTransfersByStatusOnDate.sql: 0=DES_NAME, 1=HOS_NAME, 2=DAT_SCHEDULED_TIME, 3=DAT_TARGET,
     * 4=DAT_SENT/DAT_SIZE (%), 5=DAT_SENT/DAT_DURATION (rate), 6=DAT_PRIORITY
     */
    private static final int[] SORT_COLS = { 0, 1, 2, 3, 4, 5, 6 };

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
        final var status = Util.getValue(request, "transferStatus", "EXEC");
        final var search = Util.getValue(request, "transferSearch", "");
        final var type = Util.getValue(request, "transferType", "");

        // Parse date, fall back to today if absent or unparseable
        final var iso = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = iso.parse(Util.getValue(request, "date", ""));
        } catch (final ParseException e) {
            date = new Date();
        }

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
            transfers = DataTransferHome.findByStatusIdAndDate(status, date, search, DestinationOption.getTypeIds(type),
                    cursor);
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
        final var data = root.putArray("data");
        for (final DataTransfer dt : transfers) {
            final var row = data.addArray();
            row.add(buildDestinationHtml(dt));
            row.add(buildHostHtml(dt));
            row.add(buildScheduledTimeHtml(dt));
            row.add(buildTargetHtml(dt));
            row.add(buildProgressHtml(dt));
            row.add(buildRateHtml(dt));
            row.add(String.valueOf(dt.getPriority()));
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

    private static String buildDestinationHtml(final DataTransfer dt) {
        final var name = escapeHtml(dt.getDestinationName());
        return "<a href=\"" + DESTINATION_BASE_PATH + "/" + name + "\" class=\"text-decoration-none\">" + name + "</a>";
    }

    private static String buildHostHtml(final DataTransfer dt) {
        final var nickName = dt.getHostNickName();
        if (nickName == null || nickName.isBlank()) {
            return "<i class=\"bi bi-x-circle text-warning\" title=\"Not transferred to remote host\"></i>";
        }
        final var hostName = escapeHtml(dt.getHostName());
        final var serverName = dt.getTransferServerName();
        final var title = serverName != null && !serverName.isBlank()
                ? " title=\"Transmitted through " + escapeHtml(serverName) + "\"" : "";
        return "<a href=\"" + HOST_BASE_PATH + "/" + hostName + "\"" + title + " class=\"text-decoration-none\">"
                + escapeHtml(nickName) + "</a>";
    }

    private static String buildScheduledTimeHtml(final DataTransfer dt) {
        final var t = dt.getScheduledTime();
        return t != null ? Format.formatTime(t.getTime()) : "";
    }

    private static String buildTargetHtml(final DataTransfer dt) {
        final var id = escapeHtml(dt.getId());
        final var target = escapeHtml(dt.getTarget());
        final var size = escapeHtml(dt.getFormattedSize());
        final var cls = dt.getDeleted() ? "text-danger" : "text-decoration-none";
        return "<a href=\"" + DATATRANSFER_BASE_PATH + "/" + id + "\" class=\"" + cls + "\" title=\"Size: " + size
                + "\">" + target + "</a>";
    }

    private static String buildProgressHtml(final DataTransfer dt) {
        try {
            return String.valueOf(dt.getProgress());
        } catch (final Exception e) {
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
        } catch (final Exception e) {
            return "";
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
