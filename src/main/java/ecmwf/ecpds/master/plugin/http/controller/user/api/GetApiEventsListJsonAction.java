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

package ecmwf.ecpds.master.plugin.http.controller.user.api;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.ApiEvent;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

/**
 * Returns a DataTables-compatible JSON payload for the API Events Log page.
 */
public class GetApiEventsListJsonAction extends PDSAction {

    private static final String API_CLIENT_BASE_PATH = "/do/user/api";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var search = request.getParameter("search");
        final var clientId = request.getParameter("clientId");

        // Resolve the date filter (from request param, then from session)
        final var iso = PDSAction.getISOFormat();
        var dateStr = request.getParameter("date");
        if (dateStr == null || dateStr.isBlank()) {
            final var stored = request.getSession().getAttribute(GetApiEventsAction.DATE_FILTER_KEY);
            dateStr = stored == null ? iso.format(new java.util.Date()) : stored.toString();
        }
        java.util.Date date = null;
        try {
            date = iso.parse(dateStr);
        } catch (final Exception ignored) {
        }

        final var cursor = Util.getDataBaseCursorForDataTables(0, true, request);
        Collection<ApiEvent> events;
        String queryError = null;
        try {
            events = MasterManager.getDB().getApiEventsFiltered(clientId, date, search, cursor);
        } catch (final Exception e) {
            events = Collections.emptyList();
            queryError = e.getMessage();
        }

        final var recordsTotal = Util.getCollectionFrom(events);
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        if (queryError != null) {
            root.put("error", queryError);
        }

        final var data = root.putArray("data");
        for (final ApiEvent e : events) {
            final var row = data.addArray();
            row.add(formatDateTime(e.getDate()));
            row.add(buildClientHtml(e.getClientId()));
            row.add("<code>" + escapeHtml(e.getService()) + "</code>");
            row.add(escapeHtml(e.getHost()));
            row.add(e.getSuccess() ? "<span class=\"badge bg-success\">OK</span>"
                    : "<span class=\"badge bg-danger\">FAIL</span>");
            row.add(escapeHtml(e.getMessage()));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building API events list: " + e.getMessage());
        }
        return null;
    }

    private static String buildClientHtml(final String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return "";
        }
        final var escaped = escapeHtml(clientId);
        return "<a href=\"" + API_CLIENT_BASE_PATH + "/" + escaped + "\"><code>" + escaped + "</code></a>";
    }

    private static String formatDateTime(final java.sql.Timestamp ts) {
        return ts == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
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
        } catch (final NumberFormatException _) {
            return fallback;
        }
    }

    private static void writeError(final HttpServletResponse response, final int draw, final String message) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"draw\":" + draw + ",\"recordsTotal\":0,\"recordsFiltered\":0,\"data\":[],"
                    + "\"error\":\"" + message.replace("\"", "'") + "\"}");
        } catch (final java.io.IOException ignored) {
        }
    }

    @Override
    public boolean match(final ModelBean b, final String what) {
        return true;
    }
}
