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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.database.PortalSubscriber;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

public class GetPortalSubscriberListJsonAction extends PDSAction {

    private static final String BASE_PATH = "/do/user/incoming";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneOffset.UTC);

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var inuId = ECMWFActionForm.getPathParameter(mapping, request, 0);
        final var draw = parseSafeInt(request.getParameter("draw"), 1);

        var canEdit = false;
        var canDelete = false;
        try {
            canEdit = user.hasAccess(BASE_PATH + "/subscribers/" + inuId + "/edit/activate");
            canDelete = user.hasAccess(BASE_PATH + "/subscribers/" + inuId + "/edit/delete");
        } catch (final Exception ignored) {
        }

        final List<PortalSubscriber> allSubs;
        try {
            final var session = Util.getECpdsSessionFromObject(user);
            allSubs = MasterManager.getMI().getPortalSubscribersByUser(session, inuId);
        } catch (final Exception e) {
            writeError(response, draw, "Error retrieving subscribers: " + e.getMessage());
            return null;
        }

        final var subs = new ArrayList<>(allSubs);
        final var statusFilter = request.getParameter("status");
        if (statusFilter != null && !statusFilter.isBlank()) {
            subs.removeIf(s -> !matchStatus(s, statusFilter));
        }

        final var searchFilter = firstNonBlank(request.getParameter("search[value]"), request.getParameter("search"));
        if (searchFilter != null) {
            final var filter = searchFilter.toLowerCase(Locale.ROOT);
            subs.removeIf(
                    s -> !containsIgnoreCase(s.getPsbEmail(), filter) && !containsIgnoreCase(s.getPsbName(), filter));
        }

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", allSubs.size());
        root.put("recordsFiltered", subs.size());

        final var data = root.putArray("data");
        for (final var sub : subs) {
            final var row = data.addArray();
            row.add(escapeHtml(sub.getPsbEmail()));
            row.add(escapeHtml(sub.getPsbName()));
            row.add(buildCountryHtml(sub.getPsbIso()));
            row.add(buildStatusHtml(sub));
            row.add(formatTime(sub.getPsbCreatedTime()));
            row.add(buildActions(inuId, sub.getPsbId(), sub.getPsbActive(), canEdit, canDelete));
            row.add(statusSortKey(sub));
            row.add(sub.getPsbCreatedTime() != null ? sub.getPsbCreatedTime() : 0L);
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building subscriber list: " + e.getMessage());
        }
        return null;
    }

    private static boolean matchStatus(final PortalSubscriber s, final String filter) {
        return switch (filter) {
        case "active" -> s.getPsbActive();
        case "inactive" -> !s.getPsbActive() && s.getPsbVerifyToken() == null;
        case "pending" -> !s.getPsbActive() && s.getPsbVerifyToken() != null
                && !"VERIFIED".equals(s.getPsbVerifyToken());
        case "verified" -> !s.getPsbActive() && "VERIFIED".equals(s.getPsbVerifyToken());
        default -> true;
        };
    }

    private static boolean containsIgnoreCase(final String value, final String filter) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(filter);
    }

    private static String buildStatusHtml(final PortalSubscriber s) {
        if (s.getPsbActive()) {
            return "<span class=\"badge rounded-pill bg-success-subtle text-success-emphasis border border-success-subtle fw-normal\"><i class=\"bi bi-check-circle-fill me-1\"></i>Active</span>";
        }
        final var token = s.getPsbVerifyToken();
        if ("VERIFIED".equals(token)) {
            return "<span class=\"badge rounded-pill bg-warning-subtle text-warning-emphasis border border-warning-subtle fw-normal\"><i class=\"bi bi-hourglass-split me-1\"></i>Awaiting Approval</span>";
        }
        if (token != null) {
            return "<span class=\"badge rounded-pill bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle fw-normal\"><i class=\"bi bi-envelope me-1\"></i>Pending Email</span>";
        }
        return "<span class=\"badge rounded-pill bg-danger-subtle text-danger-emphasis border border-danger-subtle fw-normal\"><i class=\"bi bi-x-circle-fill me-1\"></i>Deactivated</span>";
    }

    private static int statusSortKey(final PortalSubscriber s) {
        if (s.getPsbActive()) {
            return 0;
        }
        final var token = s.getPsbVerifyToken();
        if ("VERIFIED".equals(token)) {
            return 1;
        }
        if (token != null) {
            return 2;
        }
        return 3;
    }

    private static String buildCountryHtml(final String iso) {
        if (iso == null || iso.isBlank()) {
            return "";
        }
        final var lc = iso.toLowerCase(Locale.ROOT);
        return "<span class=\"fi fi-" + escapeHtml(lc) + "\" title=\"" + escapeHtml(iso.toUpperCase(Locale.ROOT))
                + "\" style=\"font-size:1.1em;border-radius:2px;\"></span>";
    }

    private static String buildActions(final String inuId, final Long psbId, final boolean active,
            final boolean canEdit, final boolean canDelete) {
        if (psbId == null) {
            return "";
        }
        final var sb = new StringBuilder("<span class=\"d-flex gap-1 justify-content-center\">");
        if (canEdit) {
            if (active) {
                sb.append(
                        "<button type=\"button\" class=\"btn btn-xs btn-outline-warning\" title=\"Deactivate subscriber\" onclick=\"psbToggle('")
                        .append(escapeJs(inuId)).append("',").append(psbId)
                        .append(",false)\"><i class=\"bi bi-pause-circle\"></i></button>");
            } else {
                sb.append(
                        "<button type=\"button\" class=\"btn btn-xs btn-outline-success\" title=\"Activate subscriber\" onclick=\"psbToggle('")
                        .append(escapeJs(inuId)).append("',").append(psbId)
                        .append(",true)\"><i class=\"bi bi-check-circle\"></i></button>");
            }
        }
        if (canDelete) {
            sb.append(
                    "<button type=\"button\" class=\"btn btn-xs btn-outline-danger\" title=\"Delete subscriber\" onclick=\"psbDelete('")
                    .append(escapeJs(inuId)).append("',").append(psbId)
                    .append(")\"><i class=\"bi bi-trash\"></i></button>");
        }
        sb.append("</span>");
        return sb.toString();
    }

    private static String formatTime(final Long epochMs) {
        if (epochMs == null || epochMs == 0) {
            return "";
        }
        try {
            return FMT.format(Instant.ofEpochMilli(epochMs));
        } catch (final Exception e) {
            return "";
        }
    }

    private static String firstNonBlank(final String... values) {
        for (final var value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String escapeHtml(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
                "&#39;");
    }

    private static String escapeJs(final String s) {
        return escapeHtml(s).replace("\\", "\\\\").replace("\n", " ").replace("\r", " ");
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
