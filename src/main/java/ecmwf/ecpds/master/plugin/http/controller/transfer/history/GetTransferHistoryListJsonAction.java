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

package ecmwf.ecpds.master.plugin.http.controller.transfer.history;

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

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferHistoryHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferHistory;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Returns a DataTables-compatible JSON payload for the Transfer History page.
 */
public class GetTransferHistoryListJsonAction extends PDSAction {

    private static final String TRANSFER_HISTORY_BASE_PATH = "/do/transfer/history";
    private static final String HOST_BASE_PATH = "/do/transfer/host";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var currentDate = getISOFormat().format(new Date());
        final var destinationName = Util.getValue(request, "destinationName", "");
        var date = Util.getValue(request, "date", currentDate);
        if ("All".equals(date)) {
            date = currentDate;
            request.getSession().setAttribute("date", date);
        }
        final var mode = request.getParameter("mode");
        final var cursor = Util.getDataBaseCursorForDataTables(1, true, request);

        Collection<TransferHistory> historyItems;
        String queryError = null;
        try {
            final var historyDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            historyItems = "productDate".equals(mode)
                    ? TransferHistoryHome.findByDestinationNameAndProductDate(user, destinationName, historyDate,
                            cursor)
                    : TransferHistoryHome.findByDestinationNameAndHistoryDate(user, destinationName, historyDate,
                            cursor);
        } catch (final Exception e) {
            historyItems = new ArrayList<>(0);
            queryError = e.getMessage();
        }

        final var recordsTotal = Util.getCollectionSizeFrom(historyItems);
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        if (queryError != null) {
            root.put("error", queryError);
        }

        final var data = root.putArray("data");
        for (final TransferHistory history : historyItems) {
            history.setUser(user);
            final var row = data.addArray();
            row.add(buildErrorHtml(history));
            row.add(buildEventTimeHtml(history));
            row.add(buildStatusHtml(history));
            row.add(buildHostHtml(history));
            row.add(buildCommentHtml(history));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building transfer history list: " + e.getMessage());
        }
        return null;
    }

    private static String buildErrorHtml(final TransferHistory history) {
        if (history.getError()) {
            return "<i class=\"bi bi-x-circle-fill text-danger\" title=\"Error\"></i>";
        }
        return "<i class=\"bi bi-check-circle-fill text-success\" title=\"OK\"></i>";
    }

    private static String buildStatusHtml(final TransferHistory history) {
        final var code = history.getStatus();
        final var label = escapeHtml(history.getFormattedStatus());
        final String cls;
        if (code == null) {
            cls = "badge bg-secondary";
        } else {
            switch (code) {
            case "DONE":
                cls = "badge bg-success";
                break;
            case "EXEC":
            case "FETC":
                cls = "badge bg-primary";
                break;
            case "INIT":
                cls = "badge bg-info text-dark";
                break;
            case "RETR":
            case "STOP":
            case "INTR":
                cls = "badge bg-warning text-dark";
                break;
            case "FAIL":
                cls = "badge bg-danger";
                break;
            default:
                cls = "badge bg-secondary";
                break;
            }
        }
        return "<span class=\"" + cls + "\">" + label + "</span>";
    }

    private static String buildEventTimeHtml(final TransferHistory history) {
        return "<a href=\"" + TRANSFER_HISTORY_BASE_PATH + "/" + escapeHtml(history.getId()) + "\">"
                + formatDateTime(history.getDate()) + "</a>";
    }

    private static String buildHostHtml(final TransferHistory history) {
        final var hostName = history.getHostName();
        if (hostName == null || hostName.isBlank()) {
            return "<i class=\"bi bi-dash text-muted\" title=\"Not transferred to remote host\"></i>";
        }
        return "<a href=\"" + HOST_BASE_PATH + "/" + escapeHtml(hostName) + "\">"
                + escapeHtml(history.getHostNickName()) + "</a>";
    }

    private static String buildCommentHtml(final TransferHistory history) {
        final var comment = history.getFormattedComment();
        return comment == null ? "" : comment;
    }

    private static String formatDateTime(final Date date) {
        return date == null ? "" : new SimpleDateFormat("HH:mm:ss").format(date);
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
