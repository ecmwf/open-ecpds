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

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the destination list,
 * enabling server-side pagination without loading all rows into the page HTML.
 * Since the underlying DB query does not support cursor-based pagination,
 * this action fetches all matching destinations and slices the result in memory.
 * Pass length=-1 to retrieve all rows (used by the split-view toggle).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingUserHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.TimeRange;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDestinationListJsonAction.
 *
 * Handles AJAX DataTables server-side requests for the destination list page.
 */
public class GetDestinationListJsonAction extends PDSAction {

    /** Base path for destination detail pages. */
    private static final String DEST_BASE_PATH = "/do/transfer/destination";

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Pattern for property lines: module.optionName = value */
    private static final Pattern PROP_LINE = Pattern.compile("^\\s*([^#\\s][^\\s=]*)\\s*=");

    /** Pattern to extract a quoted value from a property line. */
    private static final Pattern QUOTED_VALUE = Pattern.compile("\"([^\"]*)\"");

    /** Metadata about a recognized option: its Java type and allowed choices. */
    private record OptionMeta(Class<?> type, List<String> choices) {
    }

    /** Map from lowercase module.option name → OptionMeta for the DESTINATION group. */
    private static final Map<String, OptionMeta> OPTION_MAP = buildOptionMap();

    private static Map<String, OptionMeta> buildOptionMap() {
        final var map = new HashMap<String, OptionMeta>();
        for (final var opt : ECtransOptions.get(ECtransGroups.DESTINATION)) {
            if (opt.isVisible()) {
                map.put(opt.getParameter().toLowerCase(), new OptionMeta(opt.getClazz(), opt.getChoicesAsStrings()));
            }
        }
        return map;
    }

    static boolean hasPropertyErrors(final Destination dest) {
        try {
            final var data = dest.getData();
            if (data == null || data.isBlank()) {
                return false;
            }
            final var sepIdx = data.indexOf(ECtransSetup.SEPARATOR);
            final var propsText = sepIdx >= 0 ? data.substring(0, sepIdx) : data;
            final var sb = new StringBuilder();
            for (final var rawLine : propsText.split("\n")) {
                if (sb.length() == 0) {
                    sb.append(rawLine);
                } else {
                    sb.append(' ').append(rawLine.trim());
                }
                var quoteCount = 0;
                for (var i = 0; i < sb.length(); i++) {
                    if (sb.charAt(i) == '"')
                        quoteCount++;
                }
                if (quoteCount % 2 == 0) {
                    if (checkLineHasError(sb.toString()))
                        return true;
                    sb.setLength(0);
                }
            }
            if (!sb.isEmpty() && checkLineHasError(sb.toString()))
                return true;
        } catch (final Exception ignored) {
        }
        return false;
    }

    private static boolean hasJavascript(final Destination dest) {
        try {
            final var data = dest.getData();
            if (data == null) {
                return false;
            }
            final var sepIdx = data.indexOf(ECtransSetup.SEPARATOR);
            return sepIdx >= 0 && !data.substring(sepIdx + ECtransSetup.SEPARATOR.length()).trim().isEmpty();
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
        final var dotIdx = key.indexOf('.');
        if (dotIdx < 0) {
            return false;
        }
        // alias.<destName> keys are dynamic (one per associated destination) — the option name
        // is an arbitrary destination name, not a static parameter. Accept any alias.* key
        // and only flag a value error if the quoted value is completely absent.
        final var modulePrefix = key.substring(0, dotIdx).toLowerCase();
        if ("alias".equals(modulePrefix)) {
            return false;
        }
        final var meta = OPTION_MAP.get(key.toLowerCase());
        if (meta == null) {
            return true;
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

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws TransferException, ECMWFActionFormException {
        // Lightweight existence check used by the insert form to validate name uniqueness.
        if ("checkId".equals(request.getParameter("json"))) {
            final var id = request.getParameter("id");
            boolean exists = false;
            if (id != null && !id.isBlank()) {
                try {
                    DestinationHome.findByPrimaryKey(id);
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
        final var start = parseSafeInt(request.getParameter("start"), 0);
        final var lengthParam = parseSafeInt(request.getParameter("length"), 25);

        final var search = Util.getValue(request, "destinationSearch", "");
        final var sortDirection = Util.getValue(request, "sortDirection", "asc");
        final var aliases = Util.getValue(request, "aliases", "All");
        final var status = Util.getValue(request, "destinationStatus", "All Status");
        final var type = Util.getValue(request, "destinationType", "-1");
        final var filter = Util.getValue(request, "destinationFilter", "All");
        final var dataUsersFilter = Util.getValue(request, "datausers", "any"); // "any" | "yes" | "no"
        final var propErrorsFilter = "yes".equalsIgnoreCase(Util.getValue(request, "propErrors", ""));
        final var jsNonEmptyFilter = "yes".equalsIgnoreCase(Util.getValue(request, "jsNonEmpty", ""));

        // DataTables server-side sort params take priority over legacy sortDirection
        final var orderColParam = request.getParameter("order[0][column]");
        final var orderDirParam = request.getParameter("order[0][dir]");
        final int orderCol = orderColParam != null ? parseSafeInt(orderColParam, 1) : 1;
        final boolean ascending = orderDirParam != null ? "asc".equals(orderDirParam) : "asc".equals(sortDirection);

        final boolean filterByDataUsers = "yes".equals(dataUsersFilter) || "no".equals(dataUsersFilter);
        final boolean needsJavaFilter = filterByDataUsers || propErrorsFilter || jsNonEmptyFilter;

        Collection<Destination> page;
        int recordsTotal = 0;
        int recordsFiltered;
        String queryError = null;
        Set<String> proxyDestNames;
        try {
            if (needsJavaFilter) {
                // Fetch all matching destinations (ignoring pagination), then post-filter and paginate in memory.
                final var all = DestinationHome.findByUser(user, search, aliases, orderCol, ascending, 0, -1,
                        StatusFactory.getDestinationStatusCode(status),
                        GetDestinationAction.getDestinationTypeIds(type), filter);
                recordsTotal = DestinationHome.countByUser(user, search, aliases,
                        StatusFactory.getDestinationStatusCode(status),
                        GetDestinationAction.getDestinationTypeIds(type), filter);
                final var destsWithUsers = filterByDataUsers ? buildDestinationsWithDataUsers()
                        : java.util.Collections.<String> emptySet();
                final boolean wantWith = "yes".equals(dataUsersFilter);
                final List<Destination> filtered = new ArrayList<>();
                for (final Destination d : all) {
                    if (filterByDataUsers && wantWith != destsWithUsers.contains(d.getName())) {
                        continue;
                    }
                    if (propErrorsFilter && !hasPropertyErrors(d)) {
                        continue;
                    }
                    if (jsNonEmptyFilter && !hasJavascript(d)) {
                        continue;
                    }
                    filtered.add(d);
                }
                recordsFiltered = filtered.size();
                final int end = Math.min(start + (lengthParam < 0 ? filtered.size() : lengthParam), filtered.size());
                page = start < filtered.size() ? filtered.subList(start, end) : new ArrayList<>(0);
            } else {
                page = DestinationHome.findByUser(user, search, aliases, orderCol, ascending, start, lengthParam,
                        StatusFactory.getDestinationStatusCode(status),
                        GetDestinationAction.getDestinationTypeIds(type), filter);
                recordsTotal = DestinationHome.countByUser(user, search, aliases,
                        StatusFactory.getDestinationStatusCode(status),
                        GetDestinationAction.getDestinationTypeIds(type), filter);
                recordsFiltered = recordsTotal;
            }
            proxyDestNames = DestinationHome.findNamesWithProxyHosts();
        } catch (final TransferException e) {
            page = new ArrayList<>(0);
            proxyDestNames = java.util.Collections.emptySet();
            recordsTotal = 0;
            recordsFiltered = 0;
            queryError = e.getMessage();
        }

        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsFiltered);
        if (queryError != null) {
            root.put("queryError", queryError);
        }
        final var data = root.putArray("data");
        for (final Destination d : page) {
            final var propErr = hasPropertyErrors(d);
            final var jsNonEmpty = hasJavascript(d);
            final var row = data.addArray();
            row.add(buildFlagHtml(d));
            row.add(buildNameHtml(d, proxyDestNames, propErr, jsNonEmpty));
            row.add(escapeHtml(d.getId()));
            row.add(buildStatusHtml(d));
            row.add(buildAliasesHtml(d));
            row.add(buildCategoryHtml(d));
            row.add(buildCompressionHtml(d));
            row.add(buildEnabledHtml(d));
            row.add(buildAcquisitionHtml(d));
            row.add(buildShowInMonitorsHtml(d));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building destination list: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // HTML column builders
    // -------------------------------------------------------------------------

    private static String buildFlagHtml(final Destination d) {
        final var iso = d.getCountryIso();
        if (iso == null || iso.isBlank() || iso.length() != 2) {
            return "";
        }
        final var liso = iso.toLowerCase();
        String countryName = "";
        try {
            final var c = d.getCountry();
            if (c != null && c.getName() != null) {
                countryName = escapeHtml(c.getName());
            }
        } catch (final Exception ignored) {
        }
        if ("ex".equals(liso)) {
            return "<i class=\"bi bi-globe\" title=\"" + countryName
                    + "\" style=\"font-size:1.1em;display:block\"></i>";
        }
        return "<span class=\"fi fi-" + liso + "\" title=\"" + countryName + "\" data-iso=\"" + liso + "\" data-name=\""
                + countryName + "\" style=\"font-size:1.1em;display:block\"></span>";
    }

    private static String buildNameHtml(final Destination d, final Set<String> proxyDestNames, final boolean propErr,
            final boolean jsNonEmpty) {
        final var sb = new StringBuilder();
        final var active = d.getActive();
        final var id = escapeHtml(d.getId());
        final var href = DEST_BASE_PATH + "/" + id;

        sb.append("<span style=\"white-space:nowrap\">");
        if (!active) {
            sb.append(
                    "<i class=\"bi bi-slash-circle-fill text-danger me-1\" title=\"Disabled\" style=\"font-size:0.78rem;\"></i>");
        }
        if (!active) {
            sb.append("<a href=\"").append(href).append(
                    "\" class=\"fw-semibold dest-list-link\" style=\"text-decoration:line-through;color:var(--bs-secondary-color)\">")
                    .append(id).append("</a>");
        } else {
            sb.append("<a href=\"").append(href).append("\" class=\"fw-semibold text-decoration-none dest-list-link\">")
                    .append(id).append("</a>");
        }
        // Type badge
        final var typeText = d.getTypeText();
        if (typeText != null && !typeText.isBlank()) {
            switch (typeText) {
            case "Gold":
                sb.append(
                        "<span class=\"dest-page-type dest-type-gold ms-1\"><i class=\"bi bi-trophy-fill\"></i> Gold</span>");
                break;
            case "Silver":
                sb.append(
                        "<span class=\"dest-page-type dest-type-silver ms-1\"><i class=\"bi bi-award-fill\"></i> Silver</span>");
                break;
            case "Bronze":
                sb.append(
                        "<span class=\"dest-page-type dest-type-bronze ms-1\"><i class=\"bi bi-award\"></i> Bronze</span>");
                break;
            case "Basic":
                sb.append(
                        "<span class=\"dest-page-type dest-type-basic ms-1\"><i class=\"bi bi-patch-check\"></i> Basic</span>");
                break;
            default:
                sb.append("<span class=\"dest-page-type ms-1\">").append(escapeHtml(typeText)).append("</span>");
            }
        }
        // Monitor visibility
        if (!d.getShowInMonitors()) {
            sb.append(
                    "<i class=\"bi bi-eye-slash text-muted ms-1\" title=\"Not shown in Monitor Display\" style=\"font-size:0.78rem;\"></i>");
        }
        // Compression icon
        final var filterName = d.getFilterName();
        if (filterName != null && !filterName.isBlank() && !"none".equals(filterName)) {
            sb.append(buildCompressionIcon(filterName));
        }
        // Proxy icon
        if (proxyDestNames.contains(d.getId())) {
            sb.append(
                    "<i class=\"bi bi-hdd-network text-secondary ms-1\" title=\"Uses a Proxy Host\" style=\"font-size:0.78rem;\"></i>");
        }
        // Editor warning badges
        if (propErr) {
            sb.append(
                    "<span class=\"ms-1 text-warning\" style=\"font-size:0.85rem;\" title=\"Properties editor has errors\"><i class=\"bi bi-exclamation-triangle-fill\"></i></span>");
        }
        if (jsNonEmpty) {
            sb.append(
                    "<span class=\"ms-1 text-secondary\" style=\"font-size:0.85rem;\" title=\"JavaScript is configured\"><i class=\"bi bi-braces\"></i></span>");
        }
        sb.append("</span>");
        // Comment
        final var comment = d.getComment();
        if (comment != null && !comment.isBlank()) {
            sb.append("<div class=\"text-muted\" style=\"font-size:0.78rem;line-height:1.3;margin-top:1px;\">")
                    .append(escapeHtml(comment)).append("</div>");
        }
        return sb.toString();
    }

    private static String buildStatusHtml(final Destination d) {
        final var status = d.getFormattedStatus();
        if (status == null) {
            return "";
        }
        final var base = status.contains("-") ? status.substring(0, status.indexOf('-')) : status;
        final String cls;
        switch (base.trim()) {
        case "Running":
            cls = "badge bg-success";
            break;
        case "Waiting":
        case "Retrying":
        case "Interrupted":
            cls = "badge bg-warning text-dark";
            break;
        case "Restarting":
        case "Resending":
            cls = "badge bg-info text-dark";
            break;
        case "Idle":
            cls = "badge bg-secondary";
            break;
        default:
            cls = "badge bg-danger";
        }
        return "<span class=\"" + cls + "\" title=\"" + escapeHtml(status) + "\">" + escapeHtml(status) + "</span>";
    }

    private static String buildAliasesHtml(final Destination d) {
        Collection<Destination> aliases;
        try {
            aliases = d.getAliases();
        } catch (final Exception e) {
            return "";
        }
        if (aliases == null || aliases.isEmpty()) {
            return "<span class=\"badge bg-body-tertiary text-muted border fst-italic\">none</span>";
        }
        if (aliases.size() == 1) {
            final var aid = escapeHtml(aliases.iterator().next().getId());
            return "<a href=\"" + DEST_BASE_PATH + "/" + aid
                    + "\" class=\"badge bg-body-tertiary text-secondary border text-decoration-none\" title=\"" + aid
                    + " is an alias for " + escapeHtml(d.getId()) + "\">" + aid + "</a>";
        }
        // Multiple aliases — Bootstrap dropdown
        final var sb = new StringBuilder();
        sb.append("<div class=\"dropdown\">").append(
                "<button class=\"badge bg-body-tertiary text-secondary border text-decoration-none dropdown-toggle\" ")
                .append("style=\"cursor:pointer;\" ")
                .append("data-bs-toggle=\"dropdown\" data-bs-auto-close=\"true\" data-bs-boundary=\"viewport\">")
                .append(aliases.size()).append(" aliases").append("</button>")
                .append("<ul class=\"dropdown-menu\" style=\"max-height:300px;overflow-y:auto;\">");
        for (final Destination alias : aliases) {
            final var aid = escapeHtml(alias.getId());
            sb.append("<li><a class=\"dropdown-item\" href=\"").append(DEST_BASE_PATH).append("/").append(aid)
                    .append("\" title=\"").append(aid).append(" is an alias for ").append(escapeHtml(d.getId()))
                    .append("\">").append(aid).append("</a></li>");
        }
        sb.append("</ul></div>");
        return sb.toString();
    }

    private static String buildCategoryHtml(final Destination d) {
        final var typeText = d.getTypeText();
        if (typeText == null || typeText.isBlank()) {
            return "";
        }
        return switch (typeText) {
        case "Gold" -> "<span class=\"dest-page-type dest-type-gold\"><i class=\"bi bi-trophy-fill\"></i> Gold</span>";
        case "Silver" -> "<span class=\"dest-page-type dest-type-silver\"><i class=\"bi bi-award-fill\"></i> Silver</span>";
        case "Bronze" -> "<span class=\"dest-page-type dest-type-bronze\"><i class=\"bi bi-award\"></i> Bronze</span>";
        case "Basic" -> "<span class=\"dest-page-type dest-type-basic\"><i class=\"bi bi-patch-check\"></i> Basic</span>";
        default -> "<span class=\"dest-page-type\">" + escapeHtml(typeText) + "</span>";
        };
    }

    private static String buildCompressionHtml(final Destination d) {
        final var f = d.getFilterName();
        if (f == null || f.isBlank() || "none".equals(f)) {
            return "<span class=\"badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic\">None</span>";
        }
        return buildCompressionIcon(f) + " <small class=\"text-muted\">" + escapeHtml(f) + "</small>";
    }

    private static String buildEnabledHtml(final Destination d) {
        return d.getActive()
                ? "<i class=\"bi bi-check-circle-fill text-success\" title=\"Enabled\" style=\"font-size:0.9rem;\"></i>"
                : "<i class=\"bi bi-x-circle-fill text-danger\" title=\"Disabled\" style=\"font-size:0.9rem;\"></i>";
    }

    private static String buildAcquisitionHtml(final Destination d) {
        return d.getAcquisition()
                ? "<i class=\"bi bi-check-circle-fill text-success\" title=\"Acquisition\" style=\"font-size:0.9rem;\"></i>"
                : "<i class=\"bi bi-dash text-muted\" style=\"font-size:0.9rem;\"></i>";
    }

    private static String buildShowInMonitorsHtml(final Destination d) {
        return d.getShowInMonitors()
                ? "<i class=\"bi bi-check-circle-fill text-success\" title=\"Shown in monitors\" style=\"font-size:0.9rem;\"></i>"
                : "<i class=\"bi bi-dash text-muted\" style=\"font-size:0.9rem;\"></i>";
    }

    private static String buildCompressionIcon(final String name) {
        final String icon;
        switch (name) {
        case "zip":
            icon = "bi-file-zip";
            break;
        case "gzip":
            icon = "bi-file-earmark-zip";
            break;
        case "lzma":
            icon = "bi-box-seam";
            break;
        case "bzip2a":
            icon = "bi-archive";
            break;
        case "lbzip2":
            icon = "bi-cpu";
            break;
        case "lz4":
            icon = "bi-lightning";
            break;
        case "snappy":
            icon = "bi-lightning-charge";
            break;
        case "zstd":
            icon = "bi-stack";
            break;
        default:
            icon = "bi-file-zip";
        }
        return "<i class=\"bi " + icon + " text-muted\" title=\"" + escapeHtml(name)
                + "\" style=\"font-size:0.85rem\"></i>";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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

    /**
     * Builds the set of destination names that have at least one associated data user (directly or via a Data Policy).
     * Uses efficient DB-backed lookups where possible.
     */
    private static Set<String> buildDestinationsWithDataUsers() {
        final Set<String> names = new LinkedHashSet<>();
        // Policy-based (efficient): policy → destinations, policy → users
        try {
            for (final var policy : IncomingPolicyHome.findAll()) {
                try {
                    if (!IncomingUserHome.findAssociatedToIncomingPolicy(policy).isEmpty()) {
                        for (final var dest : policy.getAssociatedDestinations()) {
                            names.add(dest.getName());
                        }
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        // Direct associations (full user scan — no DB index from destination→user)
        try {
            for (final var user : IncomingUserHome.findAll()) {
                try {
                    for (final var dest : user.getAssociatedDestinations()) {
                        names.add(dest.getName());
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        return names;
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
