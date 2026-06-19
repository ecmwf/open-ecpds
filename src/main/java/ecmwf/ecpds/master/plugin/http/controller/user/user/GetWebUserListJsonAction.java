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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser;
import ecmwf.web.ECMWFException;
import ecmwf.web.home.users.UserHome;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.User;

/**
 * Returns a DataTables-compatible JSON payload for the Web Users list page.
 */
public class GetWebUserListJsonAction extends PDSAction {

    private static final String USER_BASE_PATH = "/do/user/user";
    private static final String CATEGORY_BASE_PATH = "/do/user/category";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Monitor-only category names (non-destination monitor categories)
    private static final Set<String> MONITOR_CATEGORY_NAMES = Set.of("mstate", "monitoring", "transfers",
            "requirements");

    // Monitor status values used for sorting and filtering
    private static final int STATUS_NOT_MONITOR = 0;
    private static final int STATUS_MONITOR_OK = 1;
    private static final int STATUS_MONITOR_NO_DEST = 2;

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        Collection<?> users;
        try {
            users = UserHome.findAll();
        } catch (final Exception e) {
            writeError(response, draw, "Error retrieving web users: " + e.getMessage());
            return null;
        }

        final var monitorNoDestOnly = "true".equals(request.getParameter("monitorNoDestination"));

        final var root = MAPPER.createObjectNode();
        final var data = root.putArray("data");
        var total = 0;
        var filtered = 0;
        for (final Object obj : users) {
            if (!(obj instanceof final WebUser u)) {
                continue;
            }
            total++;
            // Fetch categories once and reuse for both the chip display and monitor classification.
            final var cats = getCategories(u);
            final var monitorStatus = getMonitorStatus(cats);
            if (monitorNoDestOnly && monitorStatus != STATUS_MONITOR_NO_DEST) {
                continue;
            }
            filtered++;
            final var row = data.addArray();
            row.add(buildIdLink(u.getId()));
            row.add(escapeHtml(u.getCommonName()));
            row.add(buildBadge(u.getActive()));
            row.add(buildCategoriesHtml(cats));
            row.add(buildMonitorBadge(monitorStatus));
            row.add(buildActions(u.getId()));
            // Hidden sort values: Enabled (col 2) and Monitor status (col 4)
            row.add(u.getActive() ? 1 : 0);
            row.add(monitorStatus);
        }
        root.put("draw", draw);
        root.put("recordsTotal", total);
        root.put("recordsFiltered", filtered);
        var canDelete = false;
        try {
            canDelete = user.hasAccess(USER_BASE_PATH + "/edit/delete");
        } catch (final Exception ignored) {
        }
        root.put("canDelete", canDelete);

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building web users list: " + e.getMessage());
        }
        return null;
    }

    /**
     * Collect the categories for a web user, returning an empty list on error. Called once per user so the result can
     * be reused for both display and classification.
     */
    private static Collection<Category> getCategories(final WebUser u) {
        final var cats = new ArrayList<Category>();
        try {
            for (final Object obj : u.getCategories()) {
                if (obj instanceof final Category cat) {
                    cats.add(cat);
                }
            }
        } catch (final Exception ignored) {
        }
        return cats;
    }

    /**
     * Classify a web user's monitor status based on their categories:
     * <ul>
     * <li>{@code STATUS_NOT_MONITOR} (0) — not a monitor user</li>
     * <li>{@code STATUS_MONITOR_OK} (1) — monitor user with at least one destination category</li>
     * <li>{@code STATUS_MONITOR_NO_DEST} (2) — monitor user without any destination category</li>
     * </ul>
     * A user is a monitor user when all their categories are from the monitor set {mstate, monitoring, transfers,
     * requirements} (possibly plus destination categories ending in {@code " operations"}), and they have at least one
     * monitor category.
     */
    private static int getMonitorStatus(final Collection<Category> cats) {
        var hasMonitor = false;
        var hasDestination = false;
        var hasOther = false;
        for (final Category cat : cats) {
            final var name = cat.getName();
            if (name != null && name.endsWith(" operations")) {
                hasDestination = true;
            } else if (name != null && MONITOR_CATEGORY_NAMES.contains(name)) {
                hasMonitor = true;
            } else {
                hasOther = true;
            }
        }
        if (!hasMonitor || hasOther) {
            return STATUS_NOT_MONITOR;
        }
        return hasDestination ? STATUS_MONITOR_OK : STATUS_MONITOR_NO_DEST;
    }

    private static String buildIdLink(final String id) {
        final var escaped = escapeHtml(id);
        return "<a href=\"" + USER_BASE_PATH + "/" + escaped + "\">" + escaped + "</a>";
    }

    private static String buildBadge(final boolean active) {
        if (active) {
            return "<span class=\"badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis\">"
                    + "<i class=\"bi bi-check-circle-fill me-1\"></i>Yes</span>";
        }
        return "<span class=\"badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis\">"
                + "<i class=\"bi bi-x-circle-fill me-1\"></i>No</span>";
    }

    private static String buildMonitorBadge(final int monitorStatus) {
        return switch (monitorStatus) {
        case STATUS_MONITOR_NO_DEST -> "<span class=\"badge rounded-pill border fw-normal bg-danger-subtle"
                + " text-danger-emphasis\" title=\"Monitor user with no destination category assigned\">"
                + "<i class=\"bi bi-exclamation-triangle-fill me-1\"></i>No destination</span>";
        case STATUS_MONITOR_OK -> "<span class=\"badge rounded-pill border fw-normal bg-success-subtle"
                + " text-success-emphasis\" title=\"Monitor user with at least one destination category\">"
                + "<i class=\"bi bi-check-circle-fill me-1\"></i>OK</span>";
        default -> "<span class=\"text-muted\" title=\"Not a monitor user\"><i class=\"bi bi-dash\"></i></span>";
        };
    }

    private static String buildCategoriesHtml(final Collection<Category> cats) {
        final var sb = new StringBuilder();
        for (final Category cat : cats) {
            final var id = escapeHtml(cat.getId());
            final var name = escapeHtml(cat.getName());
            final var desc = escapeHtml(cat.getDescription());
            sb.append("<span class=\"assoc-chip\"><a href=\"").append(CATEGORY_BASE_PATH).append("/").append(id)
                    .append("\" title=\"").append(desc).append("\">").append(name).append("</a></span>");
        }
        if (sb.isEmpty()) {
            return "";
        }
        return "<div class=\"d-flex flex-wrap\">" + sb + "</div>";
    }

    private static String buildActions(final String id) {
        final var escaped = escapeHtml(id);
        return "<a href=\"" + USER_BASE_PATH + "/edit/update_form/" + escaped
                + "\" title=\"Edit\"><i class=\"bi bi-pencil-square text-primary\" style=\"font-size:1rem\"></i></a>"
                + "&nbsp;<a href=\"" + USER_BASE_PATH + "/edit/delete_form/" + escaped
                + "\" title=\"Delete\"><i class=\"bi bi-trash text-danger\" style=\"font-size:1rem\"></i></a>";
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
