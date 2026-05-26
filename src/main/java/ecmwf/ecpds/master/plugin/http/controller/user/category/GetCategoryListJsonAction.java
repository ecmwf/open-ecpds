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

package ecmwf.ecpds.master.plugin.http.controller.user.category;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the Web Categories list,
 * enabling AJAX loading so the page renders immediately instead of waiting for
 * all category/resource data to be fetched server-side.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.home.users.CategoryHome;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.Resource;
import ecmwf.web.model.users.User;

/**
 * The Class GetCategoryListJsonAction.
 */
public class GetCategoryListJsonAction extends PDSAction {

    private static final String CATEGORY_BASE_PATH = "/do/user/category";
    private static final String RESOURCE_BASE_PATH = "/do/user/resource";
    private static final int MAX_RESOURCES_SHOWN = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        Collection<?> categories;
        try {
            categories = CategoryHome.findAll();
        } catch (final Exception e) {
            writeError(response, draw, "Error retrieving categories: " + e.getMessage());
            return null;
        }

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", categories.size());
        root.put("recordsFiltered", categories.size());

        final var data = root.putArray("data");
        for (final Object obj : categories) {
            if (!(obj instanceof final Category cat)) {
                continue;
            }
            final var row = data.addArray();
            row.add(buildNameLink(cat));
            row.add(escapeHtml(cat.getDescription()));
            row.add(buildResourcesHtml(cat));
            row.add(buildActions(cat));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building categories list: " + e.getMessage());
        }
        return null;
    }

    private static String buildNameLink(final Category cat) {
        final var id = escapeHtml(cat.getId());
        final var name = escapeHtml(cat.getName());
        return "<a href=\"" + CATEGORY_BASE_PATH + "/" + id + "\">" + name + "</a>";
    }

    private static String buildResourcesHtml(final Category cat) {
        Collection<?> resources;
        try {
            resources = cat.getAccessibleResources();
        } catch (final Exception e) {
            return "<span class=\"text-muted\">—</span>";
        }
        if (resources == null || resources.isEmpty()) {
            return "<span class=\"text-muted\">—</span>";
        }
        final var sb = new StringBuilder();
        int count = 0;
        for (final Object obj : resources) {
            if (count >= MAX_RESOURCES_SHOWN) {
                sb.append("&nbsp;<span class=\"text-muted\">…</span>");
                break;
            }
            if (obj instanceof final Resource r) {
                final var id = escapeHtml(r.getId());
                final var path = escapeHtml(r.getPath());
                sb.append("<a href=\"").append(RESOURCE_BASE_PATH).append("/").append(id).append("\">").append(path)
                        .append("</a>&nbsp;");
                count++;
            }
        }
        return sb.toString();
    }

    private static String buildActions(final Category cat) {
        final var id = escapeHtml(cat.getId());
        return "<a href=\"" + CATEGORY_BASE_PATH + "/edit/update_form/" + id
                + "\" title=\"Edit\"><i class=\"bi bi-pencil-square text-primary\"></i></a>" + "&nbsp;<a href=\""
                + CATEGORY_BASE_PATH + "/edit/delete_form/" + id
                + "\" title=\"Delete\"><i class=\"bi bi-trash text-danger\"></i></a>";
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
