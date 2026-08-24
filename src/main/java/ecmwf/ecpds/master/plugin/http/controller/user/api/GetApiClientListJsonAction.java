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
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.ecuser.ApiClientHome;
import ecmwf.ecpds.master.plugin.http.model.ecuser.ApiClient;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

/**
 * Returns a DataTables-compatible JSON payload for the API Clients list page.
 */
public class GetApiClientListJsonAction extends PDSAction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        final var draw = parseSafeInt(request.getParameter("draw"), 1);

        Collection<ApiClient> clients;
        try {
            clients = ApiClientHome.findAll();
        } catch (final Exception e) {
            writeError(response, draw, "Error retrieving API clients: " + e.getMessage());
            return null;
        }

        // Filter: search against id and comment (case-insensitive contains)
        final var search = request.getParameter("search");
        if (search != null && !search.isBlank()) {
            final var q = search.trim().toLowerCase();
            final var filtered = new ArrayList<ApiClient>();
            for (final ApiClient c : clients) {
                final var id = c.getId() != null ? c.getId().toLowerCase() : "";
                final var comment = c.getComment() != null ? c.getComment().toLowerCase() : "";
                if (id.contains(q) || comment.contains(q)) {
                    filtered.add(c);
                }
            }
            clients = filtered;
        }

        // Filter: no-permissions-only
        final var noPermsOnly = "true".equals(request.getParameter("noPerms"));
        if (noPermsOnly) {
            final var filtered = new ArrayList<ApiClient>();
            for (final ApiClient c : clients) {
                if (c.getPermissions() == null || c.getPermissions().isEmpty()) {
                    filtered.add(c);
                }
            }
            clients = filtered;
        }

        final int total = clients.size();

        // Pagination
        final int start = parseSafeInt(request.getParameter("start"), 0);
        final int length = parseSafeInt(request.getParameter("length"), 25);
        final var page = new ArrayList<ApiClient>(clients).subList(Math.min(start, total),
                Math.min(start + length, total));

        final var canEdit = safeHasAccess(user, "/do/user/api/x/edit/update");
        final var canDelete = safeHasAccess(user, "/do/user/api/x/edit/delete");

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", total);
        root.put("recordsFiltered", total);
        root.put("canEdit", canEdit);
        root.put("canDelete", canDelete);

        final var data = root.putArray("data");
        final var df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (final ApiClient c : page) {
            final var row = data.addObject();
            row.put("id", c.getId());
            row.put("comment", c.getComment() != null ? c.getComment() : "");
            row.put("active", c.getActive());
            row.put("created", c.getCreated() != null ? df.format(c.getCreated()) : "");
            row.put("lastUsed", c.getLastUsed() != null ? df.format(c.getLastUsed()) : "");
            row.put("lastUsedHost", c.getLastUsedHost() != null ? c.getLastUsedHost() : "");
            row.put("noPerms", c.getPermissions() == null || c.getPermissions().isEmpty());
            row.put("countryHtml", buildCountryHtml(c));
            row.put("countryIso", c.getCountryIso() != null ? c.getCountryIso() : "");
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(MAPPER.writeValueAsString(root));
            response.getWriter().flush();
        } catch (final Exception e) {
            writeError(response, draw, "Error writing response: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean match(final ModelBean b, final String what) {
        return true;
    }

    // --- helpers ---

    private static int parseSafeInt(final String s, final int def) {
        if (s == null)
            return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (final NumberFormatException e) {
            return def;
        }
    }

    private static boolean safeHasAccess(final User user, final String path) {
        try {
            return user.hasAccess(path);
        } catch (final Exception e) {
            return false;
        }
    }

    private static String buildCountryHtml(final ApiClient c) {
        try {
            final var country = c.getCountry();
            if (country == null)
                return "";
            final var iso = country.getIso();
            final var name = escapeHtml(country.getName());
            final var sb = new StringBuilder("<span class=\"d-inline-flex align-items-center gap-1\">");
            if ("ex".equals(iso)) {
                sb.append("<i class=\"bi bi-globe\" title=\"").append(name).append("\" style=\"font-size:1.1em\"></i>");
            } else if (iso != null && iso.length() == 2) {
                sb.append("<span class=\"fi fi-").append(iso.toLowerCase()).append("\" title=\"").append(name)
                        .append("\" style=\"font-size:1.1em;border-radius:2px\"></span>");
            }
            sb.append("<span>").append(name).append("</span></span>");
            return sb.toString();
        } catch (final Exception e) {
            return "";
        }
    }

    private static String escapeHtml(final String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
                "&#39;");
    }

    private static void writeError(final HttpServletResponse response, final int draw, final String msg) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"draw\":" + draw + ",\"recordsTotal\":0,\"recordsFiltered\":0,"
                    + "\"data\":[],\"error\":" + MAPPER.writeValueAsString(msg) + "}");
            response.getWriter().flush();
        } catch (final Exception ignored) {
        }
    }
}
