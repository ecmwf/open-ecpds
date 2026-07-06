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

import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.TimeRange;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingUserHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Returns a DataTables-compatible JSON payload for the Data Users list page.
 */
public class GetIncomingUserListJsonAction extends PDSAction {

    private static final String INCOMING_BASE_PATH = "/do/user/incoming";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Pattern for property lines: module.optionName = value */
    private static final Pattern PROP_LINE = Pattern.compile("^\\s*([^#\\s][^\\s=]*)\\s*=");

    /** Pattern to extract a quoted value from a property line: option = "value" */
    private static final Pattern QUOTED_VALUE = Pattern.compile("\"([^\"]*)\"");

    /** Metadata about a recognized option: its Java type and allowed choices. */
    private record OptionMeta(Class<?> type, List<String> choices) {
    }

    /** Map from lowercase module.option name → OptionMeta, built once at startup from the USER group. */
    private static final Map<String, OptionMeta> OPTION_MAP = buildOptionMap();

    private static Map<String, OptionMeta> buildOptionMap() {
        final var map = new HashMap<String, OptionMeta>();
        for (final var opt : ECtransOptions.get(ECtransGroups.USER)) {
            if (opt.isVisible()) {
                map.put(opt.getParameter().toLowerCase(), new OptionMeta(opt.getClazz(), opt.getChoicesAsStrings()));
            }
        }
        return map;
    }

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        // Lightweight existence check used by the insert form to validate login uniqueness.
        if ("checkId".equals(request.getParameter("json"))) {
            final var id = request.getParameter("id");
            boolean exists = false;
            if (id != null && !id.isBlank()) {
                try {
                    IncomingUserHome.findByPrimaryKey(id);
                    exists = true;
                } catch (final Exception ignored) {
                }
            }
            try {
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"exists\":" + exists + "}");
                response.getWriter().flush();
            } catch (final java.io.IOException ignored) {
            }
            return null;
        }
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

        final var policyFilter = request.getParameter("policyNameForSearch");
        if (policyFilter != null && !policyFilter.isBlank() && !"Any Policy".equals(policyFilter)) {
            users = filterByPolicy(users, policyFilter);
        }

        final var unassignedOnly = "true".equals(request.getParameter("unassigned"));
        if (unassignedOnly) {
            users = filterUnassigned(users);
        }

        // QB filters
        final var enabledFilter = request.getParameter("enabled"); // "yes" | "no"
        final var anonymousFilter = request.getParameter("anonymous"); // "yes" | "no"
        final var totpFilter = request.getParameter("totp"); // "yes" | "no"
        final var propErrorsFilter = request.getParameter("propErrors"); // "yes"
        final var countryFilter = request.getParameter("country"); // ISO code e.g. "fr"
        final var commentFilter = request.getParameter("comment"); // wildcard e.g. "*test*"
        if ((enabledFilter != null && !enabledFilter.isBlank())
                || (anonymousFilter != null && !anonymousFilter.isBlank())
                || (totpFilter != null && !totpFilter.isBlank()) || "yes".equals(propErrorsFilter)
                || (countryFilter != null && !countryFilter.isBlank())
                || (commentFilter != null && !commentFilter.isBlank())) {
            users = filterByQB(users, enabledFilter, anonymousFilter, totpFilter, "yes".equals(propErrorsFilter),
                    countryFilter, commentFilter);
        }

        // Check once whether the current user can manage (edit/update) incoming users
        var canEdit = false;
        var canDelete = false;
        try {
            canEdit = user.hasAccess(INCOMING_BASE_PATH + "/edit/update");
            canDelete = user.hasAccess(INCOMING_BASE_PATH + "/edit/delete");
        } catch (final Exception ignored) {
        }

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", users.size());
        root.put("recordsFiltered", users.size());
        root.put("canDelete", canDelete);

        final var data = root.putArray("data");
        for (final IncomingUser u : users) {
            final var row = data.addArray();
            final var connCount = u.getIncomingConnections().size();
            final var propErrors = hasPropertyErrors(u);
            row.add(buildIdLink(u.getId(), propErrors));
            row.add(escapeHtml(u.getComment()));
            row.add(buildCountryHtml(u));
            row.add(buildBadge(u.getActive()));
            row.add(buildBadge(u.getIsSynchronized()));
            row.add(buildAnonymousHtml(u.getAnonymous()));
            row.add(connCount);
            row.add(buildActions(u.getId(), connCount, canEdit));
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

    private static String buildIdLink(final String id, final boolean propErrors) {
        final var escaped = escapeHtml(id);
        final var warning = propErrors
                ? " <span title=\"Properties editor has errors\" style=\"color:#dc3545;font-size:0.85em\">&#9888;</span>"
                : "";
        return "<a href=\"" + INCOMING_BASE_PATH + "/" + escaped + "\">" + escaped + "</a>" + warning;
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

    private static int countReachableDestinations(final IncomingUser u) {
        final var names = new java.util.HashSet<String>();
        try {
            for (final Destination dest : u.getAssociatedDestinations()) {
                names.add(dest.getName());
            }
        } catch (final Exception ignored) {
        }
        try {
            for (final IncomingPolicy policy : u.getAssociatedIncomingPolicies()) {
                try {
                    for (final Destination dest : policy.getAssociatedDestinations()) {
                        names.add(dest.getName());
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        return names.size();
    }

    /**
     * Returns true if the user's properties text contains at least one error, mirroring the validation logic in the
     * Ace-based Properties editor (fields.jsp / ecpds.js checkValueForType).
     */
    private static boolean hasPropertyErrors(final IncomingUser u) {
        try {
            final var data = u.getData();
            if (data == null || data.isBlank()) {
                return false;
            }
            for (final var line : data.split("\n")) {
                if (checkLineHasError(line)) {
                    return true;
                }
            }
        } catch (final Exception ignored) {
        }
        return false;
    }

    private static boolean checkLineHasError(final String line) {
        final var m = PROP_LINE.matcher(line);
        if (!m.find()) {
            return false;
        }
        final var key = m.group(1);
        if (key.indexOf('.') < 0) {
            return false;
        }
        final var meta = OPTION_MAP.get(key.toLowerCase());
        if (meta == null) {
            return true; // Unrecognized option → error
        }
        final var vm = QUOTED_VALUE.matcher(line);
        if (!vm.find()) {
            return false;
        }
        final var value = vm.group(1);
        if (!meta.choices().isEmpty() && !meta.choices().contains(value)) {
            return true;
        }
        return isTypeError(meta.type(), value);
    }

    private static boolean isTypeError(final Class<?> type, final String value) {
        if (type == Boolean.class) {
            return !List.of("yes", "no", "true", "false").contains(value.toLowerCase());
        }
        if (type == Integer.class) {
            return !"max-integer".equals(value) && !value.matches("-?\\d+");
        }
        if (type == Long.class) {
            return !"max-long".equals(value) && !value.matches("-?\\d+");
        }
        if (type == Double.class) {
            return !"max-double".equals(value) && !value.matches("-?\\d+(\\.\\d+)?");
        }
        if (type == ByteSize.class) {
            return !"max-size".equals(value) && !value.matches("(?i)\\d+(b|kb|mb|gb|pb|tb|eb)?");
        }
        if (type == Duration.class) {
            return !value.matches("-?\\d+")
                    && !value.matches("[-+]?P(?:\\d+D)?(?:T(?:\\d+H)?(?:\\d+M)?(?:\\d+(?:[.,]\\d{0,9})?S)?)?");
        }
        if (type == Period.class) {
            return !value.matches("-?\\d+") && !value.matches("[-+]?P(?:\\d+Y)?(?:\\d+M)?(?:\\d+W)?(?:\\d+D)?");
        }
        if (type == TimeRange.class) {
            final var timeRe = "(?:[01]\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d(?:\\.\\d{1,9})?)?";
            final var rangeRe = timeRe + "-" + timeRe;
            return !value.matches(rangeRe + "(?:," + rangeRe + ")*");
        }
        return false;
    }

    private static Collection<IncomingUser> filterByQB(final Collection<IncomingUser> users, final String enabledFilter,
            final String anonymousFilter, final String totpFilter, final boolean propErrors, final String countryFilter,
            final String commentFilter) {
        final var filtered = new ArrayList<IncomingUser>();
        for (final IncomingUser u : users) {
            if (enabledFilter != null && !enabledFilter.isBlank()) {
                final boolean want = "yes".equalsIgnoreCase(enabledFilter);
                if (u.getActive() != want) {
                    continue;
                }
            }
            if (anonymousFilter != null && !anonymousFilter.isBlank()) {
                final boolean want = "yes".equalsIgnoreCase(anonymousFilter);
                if (u.getAnonymous() != want) {
                    continue;
                }
            }
            if (totpFilter != null && !totpFilter.isBlank()) {
                final boolean want = "yes".equalsIgnoreCase(totpFilter);
                if (u.getIsSynchronized() != want) {
                    continue;
                }
            }
            if (propErrors && !hasPropertyErrors(u)) {
                continue;
            }
            if (countryFilter != null && !countryFilter.isBlank()) {
                String iso = "";
                try {
                    final var country = u.getCountry();
                    iso = country != null ? country.getIso() : "";
                } catch (final Exception ignored) {
                }
                if (!countryFilter.equalsIgnoreCase(iso)) {
                    continue;
                }
            }
            if (commentFilter != null && !commentFilter.isBlank()) {
                final var comment = u.getComment();
                if (!matchesGlob(comment != null ? comment : "", commentFilter, true)) {
                    continue;
                }
            }
            filtered.add(u);
        }
        return filtered;
    }

    /** Case-insensitive glob match supporting * and ? wildcards. */
    private static boolean matchesGlob(final String text, final String pattern, final boolean caseInsensitive) {
        final var t = caseInsensitive ? text.toLowerCase() : text;
        final var p = caseInsensitive ? pattern.toLowerCase() : pattern;
        return matchGlob(t, p, 0, 0);
    }

    private static boolean matchGlob(final String t, final String p, int ti, int pi) {
        while (pi < p.length()) {
            final char pc = p.charAt(pi);
            if (pc == '*') {
                while (pi < p.length() && p.charAt(pi) == '*')
                    pi++;
                if (pi == p.length())
                    return true;
                for (int i = ti; i <= t.length(); i++) {
                    if (matchGlob(t, p, i, pi))
                        return true;
                }
                return false;
            } else if (pc == '?') {
                if (ti >= t.length())
                    return false;
                ti++;
                pi++;
            } else {
                if (ti >= t.length() || t.charAt(ti) != pc)
                    return false;
                ti++;
                pi++;
            }
        }
        return ti == t.length();
    }

    private static Collection<IncomingUser> filterUnassigned(final Collection<IncomingUser> users) {
        final var filtered = new ArrayList<IncomingUser>();
        for (final IncomingUser u : users) {
            if (countReachableDestinations(u) == 0) {
                filtered.add(u);
            }
        }
        return filtered;
    }

    private static String buildActions(final String id, final int connectionCount, final boolean canEdit) {
        final var escaped = escapeHtml(id);
        final var sb = new StringBuilder();
        sb.append("<a href=\"").append(INCOMING_BASE_PATH).append("/edit/update_form/").append(escaped).append(
                "\" title=\"Edit\"><i class=\"bi bi-pencil-square text-primary\" style=\"font-size:1rem\"></i></a>")
                .append("&nbsp;<a href=\"").append(INCOMING_BASE_PATH).append("/edit/delete_form/").append(escaped)
                .append("\" title=\"Delete\"><i class=\"bi bi-trash text-danger\" style=\"font-size:1rem\"></i></a>");
        if (canEdit && connectionCount > 0) {
            final var closeUrl = INCOMING_BASE_PATH + "/edit/update/" + escaped + "/closeAllSessions/all";
            sb.append("&nbsp;<a href=\"javascript:confirmCloseAll('").append(closeUrl).append("',")
                    .append(connectionCount).append(")\" title=\"Close all sessions (").append(connectionCount)
                    .append(")\"><i class=\"bi bi-plug-fill text-warning\" style=\"font-size:1rem\"></i></a>");
        }
        return sb.toString();
    }

    private static Collection<IncomingUser> filterByDestination(final Collection<IncomingUser> users,
            final String destinationName) {
        final var filtered = new ArrayList<IncomingUser>();
        for (final IncomingUser u : users) {
            try {
                // Direct link: INCOMING_USER -> DESTINATION
                for (final Destination dest : u.getAssociatedDestinations()) {
                    if (destinationName.equals(dest.getName())) {
                        filtered.add(u);
                        break;
                    }
                }
                if (filtered.contains(u)) {
                    continue;
                }
                // Indirect link: INCOMING_USER -> POLICY -> DESTINATION
                outer: for (final IncomingPolicy policy : u.getAssociatedIncomingPolicies()) {
                    for (final Destination dest : policy.getAssociatedDestinations()) {
                        if (destinationName.equals(dest.getName())) {
                            filtered.add(u);
                            break outer;
                        }
                    }
                }
            } catch (final Exception ignored) {
            }
        }
        return filtered;
    }

    private static Collection<IncomingUser> filterByPolicy(final Collection<IncomingUser> users,
            final String policyId) {
        final var filtered = new ArrayList<IncomingUser>();
        for (final IncomingUser u : users) {
            try {
                for (final IncomingPolicy policy : u.getAssociatedIncomingPolicies()) {
                    if (policyId.equals(policy.getId())) {
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
