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

import java.util.Collection;

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

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", users.size());
        root.put("recordsFiltered", users.size());

        final var data = root.putArray("data");
        for (final Object obj : users) {
            if (!(obj instanceof final WebUser u)) {
                continue;
            }
            final var row = data.addArray();
            row.add(buildIdLink(u.getId()));
            row.add(escapeHtml(u.getCommonName()));
            row.add(buildBadge(u.getActive()));
            row.add(buildCategoriesHtml(u));
            row.add(buildActions(u.getId()));
            // Hidden sort value for Enabled column (col 2)
            row.add(u.getActive() ? 1 : 0);
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building web users list: " + e.getMessage());
        }
        return null;
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

    private static String buildCategoriesHtml(final WebUser u) {
        final var sb = new StringBuilder();
        try {
            for (final Object obj : u.getCategories()) {
                if (obj instanceof final Category cat) {
                    final var id = escapeHtml(cat.getId());
                    final var name = escapeHtml(cat.getName());
                    final var desc = escapeHtml(cat.getDescription());
                    if (!sb.isEmpty()) {
                        sb.append("&nbsp;");
                    }
                    sb.append("<a href=\"").append(CATEGORY_BASE_PATH).append("/").append(id).append("\" title=\"")
                            .append(desc).append("\">").append(name).append("</a>");
                }
            }
        } catch (final Exception ignored) {
        }
        return sb.toString();
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
