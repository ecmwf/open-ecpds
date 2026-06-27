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
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.technical.StreamManager;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferGroupHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferMethodHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class GetHostAction.
 */
public class GetHostAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(GetHostAction.class);

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (parameters.isEmpty()) {
            // Check if the user has full access (operator/admin) or restricted access (monitor)
            final var fullAccess = user.hasAccess(getResource(request, "transferhistory.basepath"));
            request.setAttribute("hostFullAccess", fullAccess);
            Collection<Host> hosts;
            if (fullAccess) {
                final var label = Util.getValue(request, "label", "All");
                final var filter = Util.getValue(request, "hostFilter", "All");
                final var network = Util.getValue(request, "network", "All");
                final var hostType = Util.getValue(request, "hostType", "All");
                final var hostSearch = Util.getValue(request, "hostSearch", "");
                final var cursor = Util.getDataBaseCursor("host", 25, 0, true, request);
                try {
                    hosts = HostHome.findByCriteria(label, filter, network, hostType, hostSearch, cursor);
                    request.setAttribute("getHostsError", "");
                } catch (final TransferException e) {
                    request.setAttribute("getHostsError", e.getMessage());
                    hosts = new ArrayList<>(0);
                }
                request.setAttribute("typeOptions", getTypeOptions());
                request.setAttribute("networkOptions", getNetworkOptions());
                request.setAttribute("labelOptions", getLabelOptions());
                request.setAttribute("filterOptions", getFilterOptions());
                request.setAttribute("hasHostSearch", hostSearch != null && !hostSearch.isBlank());
                try {
                    request.setAttribute("transferMethodOptions", TransferMethodHome.findAll());
                } catch (final Exception e) {
                    log.warn("Could not load transfer methods for autocomplete", e);
                }
                try {
                    final var nickNames = new TreeSet<String>();
                    final var hostNames = new TreeSet<String>();
                    for (final Host h : HostHome.findAll()) {
                        nickNames.add(h.getNickName());
                        if (h.getHost() != null && !h.getHost().isBlank()) {
                            hostNames.add(h.getHost());
                        }
                    }
                    request.setAttribute("hostNickNames", nickNames);
                    request.setAttribute("hostHostNames", hostNames);
                } catch (final Exception e) {
                    log.warn("Could not load host names for autocomplete", e);
                }
            } else {
                // Restricted user: collect only authorised hosts from DB
                final var hostSet = new LinkedHashSet<Host>();
                try {
                    for (final String hostName : MasterManager.getDB().getAuthorisedHosts(user.getId())) {
                        try {
                            hostSet.add(HostHome.findByPrimaryKey(hostName));
                        } catch (final Exception e) {
                            log.warn("Could not load host " + hostName, e);
                        }
                    }
                    request.setAttribute("getHostsError", "");
                } catch (final Exception e) {
                    request.setAttribute("getHostsError", e.getMessage());
                }
                hosts = hostSet;
            }
            request.setAttribute("hostsSize", hosts.size());
            request.setAttribute("hosts", hosts);
            return mapping.findForward("success");
        }
        final var host = HostHome.findByPrimaryKey(parameters.get(0).toString());
        // Let's pass the user for the getLastOutput links!
        host.setUser(user);
        request.setAttribute("host", host);
        // Lightweight JSON endpoint: returns whether an acquisition thread is running for this host.
        // Called asynchronously by data.jsp to control the Run Now / polling behaviour.
        if ("acquisitionRunning".equals(request.getParameter("json"))) {
            final var running = host.isAcquisitionRunning(user);
            try {
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"running\":" + running + "}");
                response.getWriter().flush();
            } catch (final java.io.IOException ignored) {
            }
            return null;
        }
        // Trigger acquisition immediately by resetting the acquisition time so the scheduler picks it up.
        if ("triggerAcquisition".equals(request.getParameter("json"))) {
            final var force = "true".equals(request.getParameter("force"));
            final var alreadyRunning = host.isAcquisitionRunning(user);
            if (force && alreadyRunning) {
                // Interrupt the running listing, then schedule an immediate re-run
                host.interruptAcquisition(user);
            }
            if (!alreadyRunning || force) {
                // Trigger acquisition immediately
                host.triggerAcquisition(user);
            }
            final boolean triggered = !alreadyRunning || force;
            try {
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"triggered\":" + triggered + ",\"running\":" + (alreadyRunning && !force)
                        + ",\"interrupted\":" + (force && alreadyRunning) + "}");
                response.getWriter().flush();
            } catch (final java.io.IOException ignored) {
            }
            return null;
        }
        final var mode = request.getParameter("mode");
        // Resolve the module documentation guide key for the host's ECtrans module.
        try {
            final var ecmName = host.getTransferMethod().getEcTransModuleName();
            if (ecmName != null) {
                request.setAttribute("moduleGuide", "/WEB-INF/jsp/pds/transfer/module/guide/" + ecmName + ".jsp");
            }
        } catch (final Exception ignored) {
        }
        // For Acquisition hosts, check whether at least one associated destination is
        // eligible for the scheduler to pick up this host. If none qualifies, expose
        // a human-readable note so the progress panel can explain why Run Now won't work.
        // The scheduler selects a host once per eligible destination, so a single running
        // destination is enough — the note is suppressed as soon as one eligible one is found.
        if (HostOption.ACQUISITION.equals(host.getType())) {
            try {
                final var reasons = new java.util.ArrayList<String>();
                var canRun = false;
                for (final var dest : host.getDestinations()) {
                    if (!dest.getActive()) {
                        reasons.add("<strong>" + dest.getName() + "</strong> is disabled");
                    } else if (!dest.getAcquisition()) {
                        reasons.add("<strong>" + dest.getName() + "</strong> has acquisition disabled");
                    } else {
                        final var sc = dest.getStatusCode();
                        if ("STOP".equals(sc) || "INIT".equals(sc) || "FAIL".equals(sc)) {
                            final var label = "STOP".equals(sc) ? "stopped"
                                    : "INIT".equals(sc) ? "not yet started" : "failed";
                            reasons.add("<strong>" + dest.getName() + "</strong> is " + label);
                        } else {
                            canRun = true;
                            break;
                        }
                    }
                }
                if (!canRun && !reasons.isEmpty()) {
                    final var note = new StringBuilder();
                    if (reasons.size() == 1) {
                        note.append(reasons.get(0)).append(" &mdash; start it to allow the listing to run");
                    } else {
                        note.append("none of the ").append(reasons.size())
                                .append(" associated destinations is eligible: ");
                        for (var i = 0; i < reasons.size(); i++) {
                            if (i > 0)
                                note.append(", ");
                            note.append(reasons.get(i));
                        }
                        note.append(" &mdash; start at least one to allow the listing to run");
                    }
                    request.setAttribute("acquisitionNote", note.toString());
                }
            } catch (final Exception ignored) {
            }
        }
        if ("changelog".equals(mode)) {
            // This is the changelog.jsp page!
            return mapping.findForward("changelog");
        } else {
            // This is the main Host page!
            return mapping.findForward("success");
        }
    }

    /**
     * Gets the type options.
     *
     * @return the type options
     */
    private static final Collection<Pair> getTypeOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Types"));
        for (final String element : HostOption.type) {
            options.add(new Pair(element, element));
        }
        return options;
    }

    /**
     * Gets the label options.
     *
     * @return the label options
     */
    public static final Collection<Pair> getLabelOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Labels"));
        for (var i = 0; i < HostOption.networkCode.length; i++) {
            options.add(new Pair(HostOption.networkCode[i], HostOption.networkName[i]));
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

    /**
     * Gets the network options.
     *
     * @return the network options
     */
    private static final Collection<Pair> getNetworkOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Groups"));
        try {
            for (final TransferGroup group : TransferGroupHome.findAll()) {
                options.add(new Pair(group.getName(), group.getName()));
            }
        } catch (final DataFileException e) {
            log.error("Problem getting TransferGroups", e);
        }
        return options;
    }
}
