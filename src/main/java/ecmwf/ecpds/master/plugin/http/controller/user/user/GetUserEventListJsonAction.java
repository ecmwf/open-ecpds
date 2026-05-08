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

package ecmwf.ecpds.master.plugin.http.controller.user.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.database.Event;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Returns a DataTables-compatible JSON payload for the Web User Events page.
 */
public class GetUserEventListJsonAction extends PDSAction {

    private static final String EVENT_BASE_PATH = "/do/user/event";
    private static final String DATATRANSFER_BASE_PATH = "/do/transfer/data";
    private static final String DATAFILE_BASE_PATH = "/do/datafile/datafile";
    private static final String DESTINATION_BASE_PATH = "/do/transfer/destination";
    private static final String HOST_BASE_PATH = "/do/transfer/host";
    private static final String TRANSFERSERVER_BASE_PATH = "/do/datafile/transferserver";
    private static final String TRANSFERGROUP_BASE_PATH = "/do/datafile/transfergroup";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var iso = getISOFormat();
        var date = request.getParameter("date");
        if (date == null || date.isBlank()) {
            final var dateO = request.getSession().getAttribute(GetUserEventsAction.DATE_FILTER_KEY);
            date = dateO == null ? iso.format(new Date()) : dateO.toString();
        }
        request.getSession().setAttribute(GetUserEventsAction.DATE_FILTER_KEY, date);

        final var cursor = Util.getDataBaseCursorForDataTables(0, true, request);
        Collection<Event> history;
        String queryError = null;
        try {
            history = MasterManager.getDB().getECuserEvents(null, iso.parse(date), request.getParameter("search"),
                    cursor);
        } catch (final Exception e) {
            history = new ArrayList<>(0);
            queryError = e.getMessage();
        }

        final var recordsTotal = Util.getCollectionFrom(history);
        final var events = convertCollectionIntoPresentationEvent(history);
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        if (queryError != null) {
            root.put("error", queryError);
        }

        final var data = root.putArray("data");
        for (final PresentationEvent event : events) {
            final var type = event.getType();
            final var row = data.addArray();
            row.add(formatDateTime(event.getDate(), event.getTime()));
            row.add(buildUserHtml(event));
            row.add(escapeHtml(event.getAction()));
            row.add(escapeHtml(event.getComment()));
            row.add(escapeHtml(event.getName()));
            row.add(escapeHtml(event.getFileName()));
            row.add(buildLinkHtml(type, event.getLinkId()));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building user event list: " + e.getMessage());
        }
        return null;
    }

    private static Collection<PresentationEvent> convertCollectionIntoPresentationEvent(
            final Collection<Event> events) {
        final Collection<PresentationEvent> out = new ArrayList<>(events.size());
        for (final Event event : events) {
            out.add(new PresentationEvent(event));
        }
        return out;
    }

    private static String buildUserHtml(final PresentationEvent event) {
        if (event.getActivity() == null || event.getActivity().getECUser() == null) {
            return "";
        }
        final var name = escapeHtml(event.getActivity().getECUser().getName());
        final var host = escapeHtml(event.getActivity().getHost());
        final var title = host.isEmpty() ? "" : " title=\"Web User logged in from " + host + "\"";
        return "<a href=\"" + EVENT_BASE_PATH + "/" + name + "\"" + title + ">" + name + "</a>";
    }

    private static String buildLinkHtml(final String type, final String linkId) {
        if (type == null || type.isBlank() || "(none)".equals(type)) {
            return "";
        }
        if ("lost".equals(type)) {
            return "<i class=\"bi bi-dash text-muted\" title=\"No related object\"></i>";
        }
        final var basePath = getBasePath(type);
        if (basePath == null || linkId == null || linkId.isBlank()) {
            return "";
        }
        return "<a href=\"" + basePath + "/" + escapeHtml(linkId)
                + "\" title=\"View related object\"><i class=\"bi bi-arrow-right-square\"></i></a>";
    }

    private static String getBasePath(final String type) {
        if ("datatransfer".equals(type)) {
            return DATATRANSFER_BASE_PATH;
        }
        if ("datafile".equals(type)) {
            return DATAFILE_BASE_PATH;
        }
        if ("destination".equals(type)) {
            return DESTINATION_BASE_PATH;
        }
        if ("host".equals(type)) {
            return HOST_BASE_PATH;
        }
        if ("transferserver".equals(type)) {
            return TRANSFERSERVER_BASE_PATH;
        }
        if ("transfergroup".equals(type)) {
            return TRANSFERGROUP_BASE_PATH;
        }
        return null;
    }

    private static String formatDateTime(final java.util.Date date, final java.util.Date time) {
        if (date == null && time == null) {
            return "";
        }
        if (date == null) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
        }
        if (time == null) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        }
        final var calendar = Calendar.getInstance();
        calendar.setTime(date);
        final var timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, 0);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
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
