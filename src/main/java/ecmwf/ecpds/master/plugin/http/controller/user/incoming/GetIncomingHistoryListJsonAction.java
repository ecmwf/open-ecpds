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

package ecmwf.ecpds.master.plugin.http.controller.user.incoming;

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

import ecmwf.common.database.IncomingHistory;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Returns a DataTables-compatible JSON payload for the Incoming Data Event Log page.
 */
public class GetIncomingHistoryListJsonAction extends PDSAction {

    private static final String INCOMING_BASE_PATH = "/do/user/incoming";
    private static final String DESTINATION_BASE_PATH = "/do/transfer/destination";
    private static final String DATATRANSFER_BASE_PATH = "/do/transfer/data";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var iso = getISOFormat();
        var date = request.getParameter("date");
        if (date == null || date.isBlank()) {
            final var dateO = request.getSession().getAttribute(GetIncomingHistoryAction.DATE_FILTER_KEY);
            date = dateO == null ? iso.format(new Date()) : dateO.toString();
        }
        request.getSession().setAttribute(GetIncomingHistoryAction.DATE_FILTER_KEY, date);

        final var cursor = Util.getDataBaseCursorForDataTables(5, true, request);
        Collection<IncomingHistory> history;
        String queryError = null;
        try {
            history = MasterManager.getDB().getIncomingHistory(null, iso.parse(date), request.getParameter("search"),
                    cursor);
        } catch (final Exception e) {
            history = new ArrayList<>(0);
            queryError = e.getMessage();
        }

        final var recordsTotal = Util.getCollectionFrom(history);
        final var events = convertCollectionIntoPresentationHistory(history);
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        if (queryError != null) {
            root.put("error", queryError);
        }

        final var data = root.putArray("data");
        for (final PresentationHistory event : events) {
            final var row = data.addArray();
            row.add(buildIncomingUserHtml(event));
            row.add(buildDestinationHtml(event));
            row.add(escapeHtml(event.getTransferServerName()));
            row.add(escapeHtml(event.getProtocol()));
            row.add(buildFileNameHtml(event));
            row.add(formatDateTime(event.getStartTime()));
            row.add(formatDateTime(event.getFinishTime()));
            row.add(buildRateHtml(event));
            row.add(event.getUpload() ? "upload" : "download");
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building incoming history list: " + e.getMessage());
        }
        return null;
    }

    private static Collection<PresentationHistory> convertCollectionIntoPresentationHistory(
            final Collection<IncomingHistory> historyItems) {
        final Collection<PresentationHistory> out = new ArrayList<>(historyItems.size());
        for (final IncomingHistory history : historyItems) {
            out.add(new PresentationHistory(history));
        }
        return out;
    }

    private static String buildIncomingUserHtml(final PresentationHistory event) {
        final var userName = escapeHtml(event.getUserName());
        return userName.isEmpty() ? "" : "<a href=\"" + INCOMING_BASE_PATH + "/" + userName + "\">" + userName + "</a>";
    }

    private static String buildDestinationHtml(final PresentationHistory event) {
        final var destinationName = escapeHtml(event.getDestinationName());
        return destinationName.isEmpty() ? ""
                : "<a href=\"" + DESTINATION_BASE_PATH + "/" + destinationName + "\">" + destinationName + "</a>";
    }

    private static String buildFileNameHtml(final PresentationHistory event) {
        final var fileName = escapeHtml(event.getFileName());
        final var formattedBytes = escapeHtml(event.getFormattedBytes());
        final var dataTransferId = event.getDataTransferId();
        if (dataTransferId != null && dataTransferId.longValue() > 0L) {
            return "<a href=\"" + DATATRANSFER_BASE_PATH + "/" + dataTransferId + "\" title=\"Size: " + formattedBytes
                    + "\">" + fileName + "</a>";
        }
        return "<span class=\"text-danger\" title=\"Size: " + formattedBytes + "\">" + fileName + "</span>";
    }

    private static String buildRateHtml(final PresentationHistory event) {
        final var rate = event.getRate();
        if (rate == 0) {
            return "<span class=\"text-muted\">[n/a]</span>";
        }
        return "<span title=\"Rate: " + escapeHtml(event.getFormattedRate()) + "\">" + rate + "</span>";
    }

    private static String formatDateTime(final Date date) {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
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
