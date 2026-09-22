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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a JSON snapshot of the OpenECPDS system topology for the "System Topology" diagram: the Master Server
 * (host and locally-running plugins/ports), the database (host and reachability), every known DataMover (host,
 * port, enabled/up state) and every Monitor instance currently connected to the Master (there can be more than one
 * for HA/scale-out setups), each with its own host plus its own locally-running plugins/ports, exactly like the
 * Master's are reported.
 *
 * URL pattern (registered in struts-config.xml):
 *   GET /do/monitoring/topology/data
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 7.4.0
 * @since 2026-09-21
 */

import java.net.InetAddress;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * Serves the system topology (Master/DataMovers/Database/this Monitor) as JSON for the topology diagram.
 */
public class GetSystemTopologyJsonAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(GetSystemTopologyJsonAction.class);

    /** Shared Jackson mapper (thread-safe). */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        ECMWFActionForm.getPathParameters(mapping, request); // no path parameters expected, kept for consistency

        final var root = MAPPER.createObjectNode();

        Map<String, Object> topology;
        try {
            topology = MasterManager.getMI().getSystemTopology();
        } catch (final Exception e) {
            // Should now be rare - ManagementImpl#getSystemTopology() itself defensively catches/times-out every
            // per-Mover/per-Monitor probe internally so a single unreachable component cannot reach this point.
            // Logged (rather than silently swallowed as before) so any future occurrence - e.g. the Master itself
            // being unreachable, or an entirely unexpected error - leaves a clear trace instead of just showing
            // "No topology data available yet." with nothing to diagnose from.
            _log.warn("Failed to retrieve system topology from the Master", e);
            topology = Map.of();
        }
        root.putPOJO("master", topology.get("master"));
        root.putPOJO("database", topology.get("database"));
        root.putPOJO("movers", topology.get("movers"));

        // Every ECpds Monitor instance currently connected to the Master (there can be more than one for HA/scale-out
        // setups), each with its own host/plugins/ports read live over its own MonitorInterface RMI connection - see
        // ManagementImpl#getSystemTopology(). Falls back below to this instance's own local info only if the Master
        // could not report any connected monitor (e.g. this instance is not yet registered, or the call above failed).
        @SuppressWarnings("unchecked")
        final var monitorsFromMaster = (java.util.List<Map<String, Object>>) topology.get("monitors");
        final var monitorsNode = root.putArray("monitors");
        if (monitorsFromMaster != null && !monitorsFromMaster.isEmpty()) {
            monitorsFromMaster.forEach(monitorsNode::addPOJO);
        } else {
            monitorsNode.addPOJO(buildLocalMonitorInfo());
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            // Response already committed or I/O error — nothing to recover
        }
        return null;
    }

    /**
     * Builds this Monitor instance's own host identity plus its own locally-running plugins/ports, read directly from
     * this JVM (no RMI call needed). Used only as a fallback when the Master could not report any connected Monitor
     * instance (e.g. this instance is not registered as an {@code ECpdsMonitor} client, or the RMI call failed), so the
     * topology diagram always shows at least this one.
     *
     * @return a map with {@code host}, {@code name}, {@code nickName} and {@code plugins} keys
     */
    private Map<String, Object> buildLocalMonitorInfo() {
        final Map<String, Object> monitorInfo = new java.util.HashMap<>();
        String host;
        try {
            host = Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName());
        } catch (final Exception e) {
            host = "monitor";
        }
        monitorInfo.put("host", host);
        monitorInfo.put("name", host);
        monitorInfo.put("nickName", System.getProperty("monitor.nickName", "Monitor"));
        monitorInfo.put("rmiConnected", false);
        final java.util.List<Map<String, Object>> pluginInfos = new java.util.ArrayList<>();
        try {
            final var container = getPluginContainer();
            if (container != null) {
                for (final var info : container.getPluginInfos()) {
                    final Map<String, Object> pluginInfo = new java.util.HashMap<>();
                    pluginInfo.put("ref", info.getRef());
                    pluginInfo.put("name", info.getName());
                    final var plugin = container.getPlugin(info.getRef());
                    final var status = container.getPluginStatus(info.getRef());
                    final var ports = plugin != null && "ON".equals(status) ? plugin.getListeningPorts()
                            : java.util.List.<Integer> of();
                    pluginInfo.put("port", ports.size() == 1 ? ports.get(0) : null);
                    pluginInfo.put("ports", ports);
                    pluginInfo.put("status", status);
                    pluginInfos.add(pluginInfo);
                }
            }
        } catch (final Throwable t) {
            // Best-effort: an empty plugins array is an acceptable degraded response.
        }
        monitorInfo.put("plugins", pluginInfos);
        return monitorInfo;
    }

    /**
     * Gets the plugin container this Monitor instance is running in, or {@code null} if unavailable (e.g. the webapp is
     * not hosted inside an {@code ecmwf.common.ecaccess.StarterServer}-based process for some reason). The {@code http}
     * plugin (this very servlet) runs as one of the plugins loaded by the Monitor's own {@code StarterServer}, which
     * registers itself as the plugin "caller" at startup — so any code running inside this JVM can look it back up this
     * way, with no RMI call needed.
     *
     * @return the plugin container, or {@code null}
     */
    private static ecmwf.common.plugin.PluginContainer getPluginContainer() {
        final var starter = ecmwf.common.plugin.PluginThread.getCaller(ecmwf.common.ecaccess.StarterServer.class);
        return starter != null ? starter.getPluginContainer() : null;
    }
}
