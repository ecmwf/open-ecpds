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
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingUserHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Returns a DataTables-compatible JSON payload for the Data Users list page.
 */
public class GetIncomingUserListJsonAction extends PDSAction {

    private static final String INCOMING_BASE_PATH = "/do/user/incoming";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        Collection<IncomingUser> users;
        try {
            users = IncomingUserHome.findAll();
        } catch (final Exception e) {
            writeError(response, draw, "Error retrieving data users: " + e.getMessage());
            return null;
        }

        final var destFilter = request.getParameter("destinationNameForSearch");
        if (destFilter != null && !destFilter.isBlank() && !"Any Destination".equals(destFilter)) {
            users = filterByDestination(users, destFilter);
        }

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", users.size());
        root.put("recordsFiltered", users.size());

        final var data = root.putArray("data");
        for (final IncomingUser u : users) {
            final var row = data.addArray();
            row.add(buildIdLink(u.getId()));
            row.add(escapeHtml(u.getComment()));
            row.add(buildCountryHtml(u));
            row.add(buildBadge(u.getActive()));
            row.add(buildBadge(u.getIsSynchronized()));
            row.add(buildAnonymousHtml(u.getAnonymous()));
            row.add(u.getIncomingConnections().size());
            row.add(buildActions(u.getId()));
            // Hidden sort values for boolean columns (cols 3, 4, 5)
            row.add(u.getActive() ? 1 : 0);
            row.add(u.getIsSynchronized() ? 1 : 0);
            row.add(u.getAnonymous() ? 1 : 0);
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building data users list: " + e.getMessage());
        }
        return null;
    }

    private static String buildIdLink(final String id) {
        final var escaped = escapeHtml(id);
        return "<a href=\"" + INCOMING_BASE_PATH + "/" + escaped + "\">" + escaped + "</a>";
    }

    private static String buildCountryHtml(final IncomingUser u) {
        try {
            final var country = u.getCountry();
            if (country == null) {
                return "";
            }
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

    private static String buildBadge(final boolean active) {
        if (active) {
            return "<span class=\"badge rounded-pill border fw-normal bg-success-subtle text-success-emphasis\">"
                    + "<i class=\"bi bi-check-circle-fill me-1\"></i>Yes</span>";
        }
        return "<span class=\"badge rounded-pill border fw-normal bg-secondary-subtle text-secondary-emphasis\">"
                + "<i class=\"bi bi-x-circle-fill me-1\"></i>No</span>";
    }

    private static String buildAnonymousHtml(final boolean anonymous) {
        if (anonymous) {
            return "<i class=\"bi bi-exclamation-circle-fill text-warning\" title=\"Yes\"></i>";
        }
        return "<i class=\"bi bi-dash text-muted\" title=\"No\"></i>";
    }

    private static String buildActions(final String id) {
        final var escaped = escapeHtml(id);
        return "<a href=\"" + INCOMING_BASE_PATH + "/edit/update_form/" + escaped
                + "\" title=\"Edit\"><i class=\"bi bi-pencil-square text-primary\" style=\"font-size:1rem\"></i></a>"
                + "&nbsp;<a href=\"" + INCOMING_BASE_PATH + "/edit/delete_form/" + escaped
                + "\" title=\"Delete\"><i class=\"bi bi-trash text-danger\" style=\"font-size:1rem\"></i></a>";
    }

    private static Collection<IncomingUser> filterByDestination(final Collection<IncomingUser> users,
            final String destinationName) {
        final var filtered = new ArrayList<IncomingUser>();
        for (final IncomingUser u : users) {
            try {
                for (final Destination dest : u.getAssociatedDestinations()) {
                    if (destinationName.equals(dest.getName())) {
                        filtered.add(u);
                        break;
                    }
                }
            } catch (final Exception ignored) {
            }
        }
        return filtered;
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
