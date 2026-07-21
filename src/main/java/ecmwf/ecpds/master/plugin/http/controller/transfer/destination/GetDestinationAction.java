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
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.CountryHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingUserHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferServerHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class GetDestinationAction.
 */
public class GetDestinationAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(GetDestinationAction.class);

    /** The Constant DESTINATIONS_PER_COLUMN. */
    private static final int DESTINATIONS_PER_COLUMN = Cnf.at("MonitorPlugin", "destinationsPerColumn", 25);

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final ArrayList<?> pathParameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (pathParameters.isEmpty()) {
            // There are no Destination specified so we are requested to list
            // the Destinations!
            final var search = Util.getValue(request, "destinationSearch", "");
            final var sortDirection = Util.getValue(request, "sortDirection", "asc");
            final var aliases = Util.getValue(request, "aliases", "All");
            final var status = Util.getValue(request, "destinationStatus", "All Status");
            final var type = Util.getValue(request, "destinationType", "-1");
            final var filter = Util.getValue(request, "destinationFilter", "All");
            Util.getValue(request, "datausers", "any");
            Util.getValue(request, "propErrors", "");
            Util.getValue(request, "jsNonEmpty", "");
            Collection<Destination> destinationList;
            try {
                destinationList = DestinationHome.findByUser(user, search, aliases, "asc".equals(sortDirection),
                        StatusFactory.getDestinationStatusCode(status), getDestinationTypeIds(type), filter);
                request.setAttribute("getDestinationsError", "");
            } catch (final TransferException e) {
                request.setAttribute("getDestinationsError", e.getMessage());
                destinationList = new ArrayList<>(0);
            }
            request.setAttribute("statusOptions", getStatusOptions());
            request.setAttribute("filterOptions", getFilterOptions());
            request.setAttribute("typeOptions", getTypeOptions());
            request.setAttribute("destinations", destinationList);
            request.setAttribute("columns", getColumns(destinationList.size()));
            request.setAttribute("hasDestinationSearch", search != null && !search.isBlank());
            try {
                request.setAttribute("countryOptions", CountryHome.findAll());
            } catch (final Exception e) {
                log.warn("Could not load country options for QB", e);
            }
            try {
                request.setAttribute("destinationNames", DestinationHome.findAllNamesAndComments());
            } catch (final Exception e) {
                log.warn("Could not load destination names for autocomplete", e);
            }
        } else {
            // We are dealing with a Destination!
            final var daf = (DetailActionForm) form;
            final var id = pathParameters.get(0).toString();
            daf.setId(id);
            // Let's get the Destination!
            final var destination = DestinationHome.findByPrimaryKey(id);
            request.setAttribute("destination", destination);
            try {
                request.setAttribute("destPropErrors", GetDestinationListJsonAction.hasPropertyErrors(destination));
            } catch (final Exception ignored) {
            }
            // Lightweight JSON endpoint: returns the data-users count for the header badge.
            // Called asynchronously by destination_header.jsp so it works on every page
            // that embeds the header (including metadata, history, monitoring pages).
            if ("dataUsersCount".equals(request.getParameter("json"))) {
                int count = 0;
                try {
                    final var dataUsers = computeDataUsers(destination);
                    count = dataUsers.get("direct").size() + dataUsers.get("policy").size();
                } catch (final Exception ignored) {
                }
                try {
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write("{\"count\":" + count + "}");
                    response.getWriter().flush();
                } catch (final java.io.IOException ignored) {
                }
                return null;
            }
            final var mode = request.getParameter("mode");
            if ("parameters".equals(mode)) {
                // This is the all_parameters.jsp page!
                return mapping.findForward("allParams");
            }
            if ("traffic".equals(mode)) {
                // This is the traffic.jsp page!
                return mapping.findForward("traffic");
            } else if ("changelog".equals(mode)) {
                // This is the changelog.jsp page!
                return mapping.findForward("changelog");
            } else if ("aliasesfrom".equals(mode)) {
                // This is the aliases_from.jsp page!
                request.setAttribute("accessibleDestNames", _getAccessibleDestinationNames(user));
                return mapping.findForward("aliasesfrom");
            } else if ("aliasesto".equals(mode)) {
                // This is the aliases_to.jsp page!
                request.setAttribute("accessibleDestNames", _getAccessibleDestinationNames(user));
                return mapping.findForward("aliasesto");
            } else if ("aliasgraph".equals(mode)) {
                // This is the alias graph diagram page!
                final var accessible = _getAccessibleDestinationNames(user);
                try {
                    request.setAttribute("aliasGraphJson", buildAliasGraphJson(destination, accessible));
                } catch (final Exception e) {
                    log.warn("Could not build alias graph for {}", destination.getName(), e);
                    request.setAttribute("aliasGraphJson", "{}");
                }
                return mapping.findForward("aliasgraph");
            } else if ("datausers".equals(mode)) {
                // This is the data users page for this destination!
                List<IncomingUser> directUsers = new ArrayList<>();
                List<IncomingUser> policyUsers = new ArrayList<>();
                try {
                    final var dataUsers = computeDataUsers(destination);
                    directUsers = dataUsers.get("direct");
                    policyUsers = dataUsers.get("policy");
                } catch (final Exception ignored) {
                }
                request.setAttribute("directUsers", directUsers);
                request.setAttribute("policyUsers", policyUsers);
                final var allUsers = new ArrayList<>(directUsers);
                allUsers.addAll(policyUsers);
                request.setAttribute("incomingUsers", allUsers);
                return mapping.findForward("datausers");
            } else {
                // This is the main Destination page!
                final var isNotDissemination = !DestinationOption.isDissemination(destination.getType());
                // Keep the DisplayTag query string to propagate sorting and paging options.
                daf.setDisplayTagsParams(getDisplayTagsParams(request));
                daf.setIsMemberState(!user.hasAccess(getResource(request, "nonmemberstate.basepath")));
                // Put filter options in the request so that they won't have to be calculated
                // more than once for the page. If this is the acquisition then we don't need to
                // introduce the Dissemination Streams and Data Streams!
                final var disseminationStreamOptionsWithSizes = isNotDissemination ? new ArrayList<>(0)
                        : daf.getDisseminationStreamOptionsWithSizes();
                request.setAttribute("disseminationStreamOptionsWithSizes", disseminationStreamOptionsWithSizes);
                final int dataStreamOptionsWithSizesSize;
                final int dataTimeOptionsWithSizesSize;
                if (isNotDissemination) {
                    request.setAttribute("dataStreamOptionsWithSizes", new ArrayList<>(0));
                    request.setAttribute("dataTimeOptionsWithSizes", new ArrayList<>(0));
                    dataStreamOptionsWithSizesSize = 0;
                    dataTimeOptionsWithSizesSize = 0;
                } else {
                    final var dataOptions = daf.getDataOptionsWithSizes();
                    request.setAttribute("dataStreamOptionsWithSizes", dataOptions.dataStreamOptionsWithSizes);
                    request.setAttribute("dataTimeOptionsWithSizes", dataOptions.dataTimeOptionsWithSizes);
                    dataStreamOptionsWithSizesSize = dataOptions.dataStreamOptionsWithSizes.size();
                    dataTimeOptionsWithSizesSize = dataOptions.dataTimeOptionsWithSizes.size();
                }
                final var statusOptionsWithSizes = daf.getStatusOptionsWithSizes();
                request.setAttribute("statusOptionsWithSizes", statusOptionsWithSizes);
                final var dateOptions = daf.getDateOptions();
                request.setAttribute("dateOptions", dateOptions);
                // Cells to add at the right of the tables to complete a "square" table.
                final int[] sizes = { disseminationStreamOptionsWithSizes.size(), dataStreamOptionsWithSizesSize,
                        dataTimeOptionsWithSizesSize, statusOptionsWithSizes.size(), dateOptions.size() };
                Arrays.sort(sizes);
                final var biggest = sizes[sizes.length - 1];
                request.setAttribute("cellsAtTheRightForDisseminationStream",
                        biggest - disseminationStreamOptionsWithSizes.size() + 1);
                request.setAttribute("cellsAtTheRightForDataStream", biggest - dataStreamOptionsWithSizesSize + 1);
                request.setAttribute("cellsAtTheRightForDataTime", biggest - dataTimeOptionsWithSizesSize + 1);
                request.setAttribute("cellsAtTheRightForStatus", biggest - statusOptionsWithSizes.size() + 1);
                request.setAttribute("cellsAtTheRightForDate", biggest - dateOptions.size() + 1);
                request.setAttribute("fileNameSearchColspan", dateOptions.size() * 2 + 1);
                request.setAttribute("products", ProductStatusHome.findAllProductNameTimePairs());
                request.setAttribute("currentDate", new Date());
                try {
                    request.setAttribute("transferServerNames",
                            TransferServerHome.findAll().stream().map(ts -> ts.getName()).sorted().toList());
                } catch (final Exception e) {
                    request.setAttribute("transferServerNames", java.util.Collections.emptyList());
                }
            }
        }
        return mapping.findForward("success");
    }

    /**
     * Computes data users for a destination, split into direct and policy-based. Policy users are found efficiently via
     * IncomingPolicyHome.findAssociatedToDestination() + IncomingUserHome.findAssociatedToIncomingPolicy(). Direct
     * users require a full scan.
     */
    private static Map<String, List<IncomingUser>> computeDataUsers(final Destination destination) throws Exception {
        // Policy users: efficient DB-backed lookup.
        final Set<String> seen = new LinkedHashSet<>();
        final List<IncomingUser> policyUsers = new ArrayList<>();
        for (final IncomingPolicy policy : IncomingPolicyHome.findAssociatedToDestination(destination)) {
            try {
                for (final IncomingUser u : IncomingUserHome.findAssociatedToIncomingPolicy(policy)) {
                    if (seen.add(u.getId())) {
                        policyUsers.add(u);
                    }
                }
            } catch (final Exception ignored) {
            }
        }
        // Direct users: single DB query on INCOMING_ASSOCIATION for this destination.
        final List<IncomingUser> directUsers = new ArrayList<>();
        for (final IncomingUser incoming : IncomingUserHome.findDirectlyAssociatedToDestination(destination)) {
            if (seen.add(incoming.getId())) {
                directUsers.add(incoming);
            }
        }
        final Map<String, List<IncomingUser>> result = new LinkedHashMap<>();
        result.put("direct", directUsers);
        result.put("policy", policyUsers);
        return result;
    }

    /**
     * Gets the display tags params.
     *
     * @param req
     *            the req
     *
     * @return the display tags params
     */
    private static final Collection<Pair> getDisplayTagsParams(final HttpServletRequest req) {
        final List<Pair> params = new ArrayList<>(4);
        final Enumeration<?> e = req.getParameterNames();
        while (e.hasMoreElements()) {
            final var name = e.nextElement().toString();
            if (name.startsWith("d-")) {
                params.add(new Pair(name, req.getParameter(name)));
            }
        }
        return params;
    }

    /**
     * Builds alias graph JSON for Mermaid diagram rendering.
     *
     * <p>
     * Performs a BFS from the focal destination, following both outgoing alias edges ({@code getAliases()}) and
     * incoming alias edges ({@code getAliasedFrom()}) recursively until all reachable destinations are collected.
     * </p>
     *
     * @param root
     *            the focal destination
     * @param accessibleDestNames
     *            set of destination names the current user may access; {@code null} means fail-open (all accessible)
     *
     * @return JSON object string: {@code {"center":"…","nodes":[…],"edges":[…]}}
     */
    private static String buildAliasGraphJson(final Destination root, final Set<String> accessibleDestNames) {
        final var rootName = root.getName();
        final var visited = new LinkedHashSet<String>();
        final var nodes = new LinkedHashMap<String, Destination>();
        final var edgeKeys = new LinkedHashSet<String>();
        final var edgeList = new ArrayList<String[]>(); // [from, to, condition, full]
        final var queue = new ArrayDeque<Destination>();
        visited.add(rootName);
        nodes.put(rootName, root);
        queue.add(root);
        while (!queue.isEmpty()) {
            final var current = queue.poll();
            final var currentName = current.getName();
            // Outgoing: current → alias
            try {
                for (final var alias : current.getAliases()) {
                    final var aliasName = alias.getName();
                    if (edgeKeys.add(currentName + "\0" + aliasName)) {
                        final var raw = alias.getDataAlias();
                        edgeList.add(
                                new String[] { currentName, aliasName, _normalizeCondition(raw), _fullCondition(raw) });
                    }
                    if (visited.add(aliasName)) {
                        nodes.put(aliasName, alias);
                        queue.add(alias);
                    }
                }
            } catch (final TransferException e) {
                log.warn("buildAliasGraphJson: getAliases failed for {}", currentName, e);
            }
            // Incoming: from → current
            try {
                for (final var from : current.getAliasedFrom()) {
                    final var fromName = from.getName();
                    if (edgeKeys.add(fromName + "\0" + currentName)) {
                        final var raw = from.getDataAlias();
                        edgeList.add(
                                new String[] { fromName, currentName, _normalizeCondition(raw), _fullCondition(raw) });
                    }
                    if (visited.add(fromName)) {
                        nodes.put(fromName, from);
                        queue.add(from);
                    }
                }
            } catch (final TransferException e) {
                log.warn("buildAliasGraphJson: getAliasedFrom failed for {}", currentName, e);
            }
        }
        final var sb = new StringBuilder("{");
        sb.append("\"center\":").append(_jsonStr(rootName));
        sb.append(",\"nodes\":[");
        var first = true;
        for (final var entry : nodes.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            final var name = entry.getKey();
            final var dest = entry.getValue();
            final var status = dest.getFormattedStatus();
            final var statusPrefix = status.contains("-") ? status.substring(0, status.indexOf('-')) : status;
            final var isAccessible = name.equals(rootName) || accessibleDestNames == null
                    || accessibleDestNames.contains(name);
            sb.append("{\"name\":").append(_jsonStr(name));
            sb.append(",\"active\":").append(dest.getActive());
            sb.append(",\"status\":").append(_jsonStr(statusPrefix));
            sb.append(",\"accessible\":").append(isAccessible);
            sb.append(",\"comment\":").append(_jsonStr(dest.getComment()));
            sb.append("}");
        }
        sb.append("],\"edges\":[");
        first = true;
        for (final var edge : edgeList) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("{\"from\":").append(_jsonStr(edge[0]));
            sb.append(",\"to\":").append(_jsonStr(edge[1]));
            sb.append(",\"condition\":").append(_jsonStr(edge[2]));
            sb.append(",\"full\":").append(_jsonStr(edge[3]));
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * Returns the set of destination names accessible to the given user, or {@code null} when the set cannot be
     * determined (fail-open: callers treat {@code null} as "all accessible").
     *
     * @param user
     *            the authenticated user
     *
     * @return set of destination names, or {@code null} on error
     */
    private static Set<String> _getAccessibleDestinationNames(final User user) {
        try {
            final var destinations = DestinationHome.findByUser(user, "", "All", true,
                    StatusFactory.getDestinationStatusCode("All Status"), getDestinationTypeIds("-1"), "All");
            final var names = new HashSet<String>(destinations.size() * 2);
            for (final var d : destinations) {
                names.add(d.getName());
            }
            return names;
        } catch (final Exception e) {
            log.warn("Could not determine accessible destinations for user {}", user.getUid(), e);
            return null; // fail-open
        }
    }

    /**
     * Normalise an alias condition string for use as a Mermaid edge label.
     *
     * <p>
     * The condition stored per-destination is an ECtrans options string of the form
     * {@code "recursive=no;delay=0;pattern=.*"}. This method extracts the {@code pattern=} value so that the edge label
     * shows only the file-name filter, not the full options block. When the pattern is the default {@code ".*"}
     * (match-everything) the method returns {@code ".*"} so the caller can suppress the label entirely.
     * </p>
     *
     * @param raw
     *            the raw condition string from {@link Destination#getDataAlias()} (may contain HTML {@code <br>}
     *            line-breaks; may be {@code null})
     *
     * @return the extracted pattern, or {@code ".*"} when the condition is absent / matches everything
     */
    private static String _normalizeCondition(final String raw) {
        if (raw == null) {
            return ".*";
        }
        // getDataAlias() replaces newlines with <br>; restore and take only the first line
        final var s = raw.replace("<br>", "\n");
        final var firstLine = s.contains("\n") ? s.substring(0, s.indexOf('\n')).trim() : s.trim();
        if (firstLine.isEmpty()) {
            return ".*";
        }
        // Options are semicolon-separated key=value pairs (e.g. "recursive=no;delay=0;pattern=.*")
        for (final var part : firstLine.split(";")) {
            if (part.startsWith("pattern=")) {
                final var pattern = part.substring("pattern=".length()).trim();
                return pattern.isEmpty() ? ".*" : pattern;
            }
        }
        // Fallback: treat the whole first line as the condition (legacy plain-pattern format)
        return firstLine;
    }

    /**
     * Returns the full, human-readable condition string for use as a tooltip.
     *
     * <p>
     * Unlike {@link #_normalizeCondition}, this method returns the complete first-line condition (with {@code <br>
     * } sequences replaced by newlines and individual semicolon-separated options placed on separate lines), making it
     * suitable for display in a tooltip.
     * </p>
     *
     * @param raw
     *            the raw condition string from {@link Destination#getDataAlias()} (may be {@code null})
     *
     * @return the formatted full condition, or an empty string when none is set
     */
    private static String _fullCondition(final String raw) {
        if (raw == null) {
            return "";
        }
        final var s = raw.replace("<br>", "\n");
        final var firstLine = s.contains("\n") ? s.substring(0, s.indexOf('\n')).trim() : s.trim();
        if (firstLine.isEmpty()) {
            return "";
        }
        // Replace semicolons with newlines so each key=value pair appears on its own tooltip line
        return firstLine.replace(";", "\n");
    }

    /**
     *
     * @param s
     *            the string to encode; {@code null} becomes JSON {@code null}
     *
     * @return the JSON string literal
     */
    private static String _jsonStr(final String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    /**
     * Gets the columns.
     *
     * @param size
     *            the size
     *
     * @return the columns
     */
    private static final Collection<Pair> getColumns(final int size) {
        final Collection<Pair> columns = new ArrayList<>();
        var index = 0;
        while (index + DESTINATIONS_PER_COLUMN < size) {
            columns.add(new Pair(Integer.toString(index), Integer.toString(DESTINATIONS_PER_COLUMN)));
            index += DESTINATIONS_PER_COLUMN;
        }
        if (size - index >= 0 && size > 0) {
            columns.add(new Pair(Integer.toString(index), Integer.toString(size - index)));
        }
        return columns;
    }

    /**
     * Gets the destination type ids.
     *
     * @param category
     *            the category
     *
     * @return the destination type ids
     */
    static final String getDestinationTypeIds(final String category) {
        return DestinationOption.getTypeIds(category);
    }

    /**
     * Gets the status options.
     *
     * @return the status options
     */
    private static final String[] getStatusOptions() {
        final var statusNames = StatusFactory.getDestinationStatusNames();
        final var result = new String[statusNames.length + 1];
        result[0] = "All Status";
        System.arraycopy(statusNames, 0, result, 1, statusNames.length);
        return result;
    }

    /**
     * Gets the type options.
     *
     * @return the type options
     */
    private static final Collection<Pair> getTypeOptions() {
        final Collection<Pair> options = new ArrayList<>();
        for (final Map.Entry<Integer, String> entry : DestinationOption.getTypes(true)) {
            options.add(new Pair(String.valueOf(entry.getKey()), entry.getValue()));
        }
        return options;
    }

    /**
     * Gets the filter options.
     *
     * @return the filter options
     */
    public static final Collection<Pair> getFilterOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Compressions"));
        for (final String mode : StreamManager.modes) {
            options.add(new Pair(mode, mode));
        }
        return options;
    }
}
