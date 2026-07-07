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

package ecmwf.ecpds.master.plugin.http.controller.transfer.host;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a DataTables-compatible JSON payload for the host list, enabling
 * server-side pagination without loading all rows into the page HTML.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.TimeRange;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetHostListJsonAction.
 *
 * Handles AJAX DataTables server-side requests for the host list page. Returns JSON in the standard DataTables
 * server-side protocol format.
 */
public class GetHostListJsonAction extends PDSAction {

    /** Logger. */
    private static final Logger log = LogManager.getLogger(GetHostListJsonAction.class);

    /** Base path for host detail pages. */
    private static final String HOST_BASE_PATH = "/do/transfer/host";

    /** Base path for destination detail pages. */
    private static final String DEST_BASE_PATH = "/do/transfer/destination";

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Pattern for property lines: module.optionName = value */
    private static final Pattern PROP_LINE = Pattern.compile("^\\s*([^#\\s][^\\s=]*)\\s*=");

    /** Pattern to extract a quoted value from a property line: option = "value" */
    private static final Pattern QUOTED_VALUE = Pattern.compile("\"([^\"]*)\"");

    /** Metadata about a recognized option: its Java type and allowed choices. */
    private record OptionMeta(Class<?> type, List<String> choices) {
    }

    /** Map from lowercase module.option name → OptionMeta, built once at startup. */
    private static final Map<String, OptionMeta> OPTION_MAP = buildOptionMap();

    private static Map<String, OptionMeta> buildOptionMap() {
        final var map = new HashMap<String, OptionMeta>();
        for (final var opt : ECtransOptions.get(ECtransGroups.HOST)) {
            if (opt.isVisible()) {
                map.put(opt.getParameter().toLowerCase(), new OptionMeta(opt.getClazz(), opt.getChoicesAsStrings()));
            }
        }
        return map;
    }

    /**
     * Returns true if the host's properties text contains at least one error, mirroring the validation logic in the
     * Ace-based Properties editor (fields.jsp / ecpds.js checkValueForType):
     * <ul>
     * <li>Unrecognized module.option key → "not recognized" error</li>
     * <li>Quoted value whose type doesn't match the option's declared type → type error</li>
     * <li>Quoted value not in the option's choices list (when choices are defined) → choices error</li>
     * </ul>
     */
    static boolean hasPropertyErrors(final Host host) {
        try {
            final var data = host.getData();
            if (data == null || data.isBlank()) {
                return false;
            }
            // Only look at the properties section (before the separator)
            final var sepIdx = data.indexOf(ECtransSetup.SEPARATOR);
            final var propsText = sepIdx >= 0 ? data.substring(0, sepIdx) : data;
            // Reassemble logical lines: a value spanning multiple lines (multi-line quoted string)
            // must be joined before validation so continuation lines are not mistaken for keys.
            final var sb = new StringBuilder();
            for (final var rawLine : propsText.split("\n")) {
                if (sb.length() == 0) {
                    sb.append(rawLine);
                } else {
                    // We are inside an open quoted value — append continuation
                    sb.append(' ').append(rawLine.trim());
                }
                // Count unescaped quotes to decide if the logical line is complete
                var quoteCount = 0;
                for (var i = 0; i < sb.length(); i++) {
                    if (sb.charAt(i) == '"') {
                        quoteCount++;
                    }
                }
                if (quoteCount % 2 == 0) {
                    // Balanced quotes → logical line is complete
                    if (checkLineHasError(sb.toString())) {
                        return true;
                    }
                    sb.setLength(0);
                }
                // Odd quote count → multi-line value, keep accumulating
            }
            // Handle any unterminated trailing line
            if (!sb.isEmpty() && checkLineHasError(sb.toString())) {
                return true;
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
        // Only validate keys that look like module.option (contain a dot)
        if (key.indexOf('.') < 0) {
            return false;
        }
        final var meta = OPTION_MAP.get(key.toLowerCase());
        if (meta == null) {
            // Unrecognized option name → error
            return true;
        }
        // Check value type (only when the value is quoted, matching JS editor behaviour)
        final var vm = QUOTED_VALUE.matcher(line);
        if (!vm.find()) {
            return false;
        }
        final var value = vm.group(1);
        // Choices validation (takes precedence for type errors when choices are defined)
        if (!meta.choices().isEmpty() && !meta.choices().contains(value)) {
            return true;
        }
        // Type validation
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
            // ISO-8601 duration or plain integer
            return !value.matches("-?\\d+")
                    && !value.matches("[-+]?P(?:\\d+D)?(?:T(?:\\d+H)?(?:\\d+M)?(?:\\d+(?:[.,]\\d{0,9})?S)?)?");
        }
        if (type == Period.class) {
            // ISO-8601 period or plain integer
            return !value.matches("-?\\d+") && !value.matches("[-+]?P(?:\\d+Y)?(?:\\d+M)?(?:\\d+W)?(?:\\d+D)?");
        }
        if (type == TimeRange.class) {
            // One or more HH:mm[-ss]-HH:mm[-ss] ranges separated by commas
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
        final var draw = parseSafeInt(request.getParameter("draw"), 1);
        final var label = Util.getValue(request, "label", "All");
        final var filter = Util.getValue(request, "hostFilter", "All");
        final var network = Util.getValue(request, "network", "All");
        final var hostType = Util.getValue(request, "hostType", "All");
        final var hostSearch = Util.getValue(request, "hostSearch", "");
        // propErrors=yes: filter hosts that have unrecognized options in the Properties editor.
        // dirNonEmpty=yes: filter hosts that have a non-empty Directory pattern.
        // jsNonEmpty=yes: filter hosts that have non-empty JavaScript.
        final var propErrorsFilter = "yes".equalsIgnoreCase(Util.getValue(request, "propErrors", ""));
        final var dirNonEmptyFilter = "yes".equalsIgnoreCase(Util.getValue(request, "dirNonEmpty", ""));
        final var jsNonEmptyFilter = "yes".equalsIgnoreCase(Util.getValue(request, "jsNonEmpty", ""));
        final var needsJavaFilter = propErrorsFilter || dirNonEmptyFilter || jsNonEmptyFilter;
        final var orderCol = parseSafeInt(request.getParameter("order[0][column]"), 1);
        final var orderDir = request.getParameter("order[0][dir]");
        final var sortAsc = "asc".equalsIgnoreCase(orderDir);
        final var sortByDest = (orderCol == 5);
        // For destination-count sort or Java-side filtering, load all rows and paginate in Java.
        // For other columns, delegate pagination to the DB cursor as before.
        final var cursor = (sortByDest || needsJavaFilter) ? new DataBaseCursor("0", "1", 0, Integer.MAX_VALUE)
                : Util.getDataBaseCursorForDataTables(0, true, request);
        Collection<Host> hosts;
        String queryError = null;
        boolean fullAccess;
        try {
            fullAccess = user.hasAccess(getResource(request, "transferhistory.basepath"));
        } catch (final Exception e) {
            fullAccess = false;
        }
        if (fullAccess) {
            // Full access: return all hosts matching the filter criteria
            try {
                hosts = HostHome.findByCriteria(label, filter, network, hostType, hostSearch, cursor);
            } catch (final TransferException e) {
                hosts = new ArrayList<>(0);
                queryError = e.getMessage();
            }
        } else {
            // Restricted user (monitor): only show authorised hosts
            final var hostSet = new LinkedHashSet<Host>();
            try {
                for (final String hostName : MasterManager.getDB().getAuthorisedHosts(user.getId())) {
                    try {
                        hostSet.add(HostHome.findByPrimaryKey(hostName));
                    } catch (final Exception e) {
                        log.warn("Could not load host " + hostName, e);
                    }
                }
            } catch (final Exception e) {
                queryError = e.getMessage();
            }
            hosts = hostSet;
        }
        // Pre-load destination names; for restricted users a single combined permission
        // query returns only the (host, destination) pairs the user is authorised to see,
        // avoiding two separate round trips against the same permission tables.
        Map<String, List<String>> destNames;
        try {
            if (fullAccess) {
                destNames = DestinationHome.getNamesByHost();
            } else {
                destNames = MasterManager.getDB().getAuthorisedHostsAndDestinations(user.getId());
            }
        } catch (final Exception e) {
            destNames = Collections.emptyMap();
        }
        // When sorting by destination count or applying Java-side filters: sort/filter in-memory and paginate here.
        final var destNamesRef = destNames;
        List<Host> hostList = new ArrayList<>(hosts);
        if (sortByDest) {
            final Comparator<Host> byDestCount = Comparator
                    .comparingInt(h -> destNamesRef.getOrDefault(h.getName(), Collections.emptyList()).size());
            hostList.sort(sortAsc ? byDestCount : byDestCount.reversed());
        }
        if (needsJavaFilter) {
            hostList = hostList.stream().filter(h -> {
                if (propErrorsFilter && !hasPropertyErrors(h)) {
                    return false;
                }
                if (dirNonEmptyFilter) {
                    final var dir = h.getDir();
                    if (dir == null || dir.isBlank()) {
                        return false;
                    }
                }
                if (jsNonEmptyFilter) {
                    final var data = h.getData();
                    if (data == null) {
                        return false;
                    }
                    final var sepIdx = data.indexOf(ECtransSetup.SEPARATOR);
                    final var js = sepIdx >= 0 ? data.substring(sepIdx + ECtransSetup.SEPARATOR.length()).trim() : "";
                    if (js.isEmpty()) {
                        return false;
                    }
                }
                return true;
            }).collect(java.util.stream.Collectors.toList());
        }
        final var recordsTotal = fullAccess && !needsJavaFilter ? Util.getCollectionSizeFrom(hosts) : hostList.size();
        // Paginate in Java when sorting by dest count or filtering in Java
        if (sortByDest || needsJavaFilter) {
            final int pageStart = parseSafeInt(request.getParameter("start"), 0);
            final int pageLen = Math.max(parseSafeInt(request.getParameter("length"), 25), 1);
            final int from = Math.min(pageStart, hostList.size());
            final int to = Math.min(from + pageLen, hostList.size());
            hostList = hostList.subList(from, to);
        }
        final var root = MAPPER.createObjectNode();
        root.put("draw", draw);
        root.put("recordsTotal", recordsTotal);
        root.put("recordsFiltered", recordsTotal);
        if (queryError != null) {
            root.put("queryError", queryError);
        }
        final var data = root.putArray("data");
        for (final Host host : hostList) {
            final var propErr = hasPropertyErrors(host);
            final var dirNonEmpty = host.getDir() != null && !host.getDir().isBlank();
            final var jsData = host.getData();
            final var jsSepIdx = jsData != null ? jsData.indexOf(ECtransSetup.SEPARATOR) : -1;
            final var jsContent = jsSepIdx >= 0 ? jsData.substring(jsSepIdx + ECtransSetup.SEPARATOR.length()).trim()
                    : "";
            final var jsNonEmpty = !jsContent.isEmpty();
            final var row = data.addArray();
            row.add(buildFlagHtml(host));
            row.add(buildNameHtml(host, propErr, dirNonEmpty, jsNonEmpty));
            row.add(escapeHtml(host.getHost()));
            row.add(escapeHtml(host.getTransferGroupName()));
            row.add(escapeHtml(host.getNetworkName()));
            row.add(buildDestinationsHtml(host, destNames));
        }
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, draw, "Error building host list: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // HTML column builders
    // -------------------------------------------------------------------------

    private static final String NO_LOCATION_HTML = "<i class=\"bi bi-globe2 text-muted\" title=\"No location information\" style=\"font-size:1.1em;display:block\"></i>";

    private static String buildFlagHtml(final Host host) {
        final var geo = host.getGeoIpLocation();
        if (geo == null || geo.isBlank()) {
            return NO_LOCATION_HTML;
        }
        final var parts = geo.split("/");
        var iso = "";
        for (final var part : parts) {
            final var trimmed = part.strip();
            if (trimmed.length() == 2) {
                iso = trimmed.toLowerCase();
                break;
            }
        }
        if (iso.isEmpty() && parts.length > 1) {
            iso = parts[parts.length - 1].strip().toLowerCase();
        }
        if (iso.isEmpty()) {
            return NO_LOCATION_HTML;
        }
        return "<span class=\"fi fi-" + escapeHtml(iso) + "\" title=\"" + escapeHtml(geo)
                + "\" style=\"font-size:1.1em;display:block\"></span>";
    }

    private static String buildNameHtml(final Host host, final boolean propErr, final boolean dirNonEmpty,
            final boolean jsNonEmpty) {
        final var sb = new StringBuilder();
        final var active = host.getActive();
        final var nickName = escapeHtml(host.getNickName());
        final var name = escapeHtml(host.getName());
        final var href = HOST_BASE_PATH + "/" + escapeHtml(host.getName());

        sb.append("<span style=\"white-space:nowrap\">");
        if (!active) {
            sb.append(
                    "<i class=\"bi bi-slash-circle-fill text-danger me-1\" title=\"Disabled\" style=\"font-size:0.78rem;\"></i>");
        }
        if (!active) {
            sb.append("<a href=\"").append(href).append(
                    "\" class=\"fw-semibold dest-list-link\" style=\"text-decoration:line-through;color:var(--bs-secondary-color)\">")
                    .append(nickName).append("</a>");
        } else {
            sb.append("<a href=\"").append(href).append("\" class=\"fw-semibold text-decoration-none dest-list-link\">")
                    .append(nickName).append("</a>");
        }
        if (!name.equals(nickName)) {
            sb.append("<code class=\"dest-page-id ms-1\" style=\"font-size:0.75rem;\" title=\"Host identifier\">")
                    .append(name).append("</code>");
        }
        // Type badge
        final var type = host.getType();
        if ("Dissemination".equals(type)) {
            sb.append(
                    "<span class=\"ms-1 text-primary\" style=\"font-size:0.85rem;\" title=\"Dissemination\"><i class=\"bi bi-send-fill\"></i></span>");
        } else if ("Acquisition".equals(type)) {
            sb.append(
                    "<span class=\"ms-1 text-success\" style=\"font-size:0.85rem;\" title=\"Acquisition\"><i class=\"bi bi-cloud-download-fill\"></i></span>");
        } else if ("Source".equals(type)) {
            sb.append(
                    "<span class=\"ms-1 text-secondary\" style=\"font-size:0.85rem;\" title=\"Source\"><i class=\"bi bi-database-fill\"></i></span>");
        } else if ("Replication".equals(type)) {
            sb.append(
                    "<span class=\"ms-1 text-secondary\" style=\"font-size:0.85rem;\" title=\"Replication\"><i class=\"bi bi-copy\"></i></span>");
        } else if ("Backup".equals(type)) {
            sb.append(
                    "<span class=\"ms-1 text-secondary\" style=\"font-size:0.85rem;\" title=\"Backup\"><i class=\"bi bi-archive-fill\"></i></span>");
        } else if ("Proxy".equals(type)) {
            sb.append(
                    "<span class=\"ms-1 text-secondary\" style=\"font-size:0.85rem;\" title=\"Proxy\"><i class=\"bi bi-arrow-left-right\"></i></span>");
        } else if (type != null && !type.isBlank()) {
            sb.append("<span class=\"badge bg-secondary ms-1\" style=\"font-size:0.7rem;\">").append(escapeHtml(type))
                    .append("</span>");
        }
        // Transfer method badge — method name is on the host object, no DB call needed
        final var method = host.getTransferMethodName();
        if (method != null && !method.isBlank()) {
            sb.append(
                    "<span class=\"badge bg-info text-dark ms-1\" style=\"font-size:0.7rem;\"><i class=\"bi bi-hdd-network me-1\"></i>")
                    .append(escapeHtml(method)).append("</span>");
        }
        // Editor warning badges
        if (propErr) {
            sb.append(
                    "<span class=\"ms-1 text-warning\" style=\"font-size:0.85rem;\" title=\"Properties editor has unrecognized options\"><i class=\"bi bi-exclamation-triangle-fill\"></i></span>");
        }
        if (dirNonEmpty) {
            sb.append(
                    "<span class=\"ms-1 text-info\" style=\"font-size:0.85rem;\" title=\"Directory pattern is set\"><i class=\"bi bi-folder-fill\"></i></span>");
        }
        if (jsNonEmpty) {
            sb.append(
                    "<span class=\"ms-1 text-secondary\" style=\"font-size:0.85rem;\" title=\"JavaScript is configured\"><i class=\"bi bi-braces\"></i></span>");
        }
        sb.append("</span>");
        // Comment below
        final var comment = host.getComment();
        if (comment != null && !comment.isBlank()) {
            sb.append("<div class=\"text-muted\" style=\"font-size:0.78rem;line-height:1.3;margin-top:1px;\">")
                    .append(escapeHtml(comment)).append("</div>");
        }
        return sb.toString();
    }

    private static String buildDestinationsHtml(final Host host, final Map<String, List<String>> destNames) {
        final var names = destNames.getOrDefault(host.getName(), Collections.emptyList());
        if (names.isEmpty()) {
            return "<span class=\"badge bg-body-tertiary text-muted border fst-italic\">none</span>";
        }
        if (names.size() == 1) {
            final var name = escapeHtml(names.get(0));
            return "<a href=\"" + DEST_BASE_PATH + "/" + name
                    + "\" class=\"badge bg-body-tertiary text-secondary border text-decoration-none\">" + name + "</a>";
        }
        // Multiple destinations — Bootstrap dropdown
        final var sb = new StringBuilder();
        sb.append("<div class=\"dropdown\">").append(
                "<button class=\"badge bg-body-tertiary text-secondary border text-decoration-none dropdown-toggle\" ")
                .append("style=\"cursor:pointer;\" ")
                .append("data-bs-toggle=\"dropdown\" data-bs-auto-close=\"true\" data-bs-boundary=\"viewport\">")
                .append(names.size()).append(" destinations").append("</button>")
                .append("<ul class=\"dropdown-menu\" style=\"max-height:300px;overflow-y:auto;\">");
        for (final var name : names) {
            final var eName = escapeHtml(name);
            sb.append("<li><a class=\"dropdown-item\" href=\"").append(DEST_BASE_PATH).append("/").append(eName)
                    .append("\">").append(eName).append("</a></li>");
        }
        sb.append("</ul></div>");
        return sb.toString();
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
