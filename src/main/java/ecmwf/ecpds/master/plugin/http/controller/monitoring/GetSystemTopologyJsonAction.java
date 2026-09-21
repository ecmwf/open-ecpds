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
 * port, enabled/up state) and the Monitor instance actually serving this request (its own host, plus its own
 * locally-running plugins/ports, exactly like the Master's are reported).
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

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.plugin.ServerPlugin;
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
            topology = Map.of();
        }
        root.putPOJO("master", topology.get("master"));
        root.putPOJO("database", topology.get("database"));
        root.putPOJO("movers", topology.get("movers"));

        // This Monitor instance (the one actually serving this request): its own configured host identity, plus its
        // own locally-running plugins/ports — read directly from this JVM, no RMI call needed (unlike the Master's,
        // which comes from the Master's own JVM via the call above).
        final var monitorNode = root.putObject("monitor");
        String host;
        try {
            host = Cnf.at("Login", "hostName", InetAddress.getLocalHost().getHostName());
        } catch (final Exception e) {
            host = "monitor";
        }
        monitorNode.put("host", host);
        monitorNode.put("nickName", System.getProperty("monitor.nickName", "Monitor"));
        final var monitorPlugins = monitorNode.putArray("plugins");
        try {
            final var container = getPluginContainer();
            if (container != null) {
                for (final var info : container.getPluginInfos()) {
                    final var pluginNode = monitorPlugins.addObject();
                    pluginNode.put("ref", info.getRef());
                    pluginNode.put("name", info.getName());
                    final var plugin = container.getPlugin(info.getRef());
                    final var status = container.getPluginStatus(info.getRef());
                    final var ports = plugin instanceof final ServerPlugin serverPlugin
                            ? java.util.List.of(serverPlugin.getPort())
                            : "ON".equals(status) ? fallbackPluginPorts(info.getRef()) : java.util.List.<Integer> of();
                    if (ports.size() == 1) {
                        pluginNode.put("port", ports.get(0));
                    } else {
                        pluginNode.putNull("port");
                    }
                    final var portsArray = pluginNode.putArray("ports");
                    ports.forEach(portsArray::add);
                    pluginNode.put("status", status);
                }
            }
        } catch (final Throwable t) {
            // Best-effort: an empty plugins array is an acceptable degraded response.
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
     * Gets the listening port(s) for a network plugin that does not extend {@link ServerPlugin} (e.g. HTTP/HTTPS,
     * MQTT/MQTTS and SSH, which each manage their own embedded server rather than using the simple accept-loop
     * abstraction {@code ServerPlugin} represents), read directly from the same configuration section that plugin
     * itself uses at startup — this executes inside this same Monitor JVM, exactly like the {@code host} lookup above,
     * so it always reads this Monitor's own local configuration, never another component's. Only called by the caller
     * when {@link ecmwf.common.plugin.PluginContainer} reports that plugin's live status as {@code "ON"}, so a plugin
     * that is configured but failed to start never gets a port shown. Returns an empty list for any other/unknown
     * plugin ref.
     *
     * @param ref
     *            the plugin reference (the key used in the {@code [PluginList]} configuration section)
     *
     * @return the ports configured for that plugin, if any
     */
    private static java.util.List<Integer> fallbackPluginPorts(final String ref) {
        final java.util.List<Integer> ports = new java.util.ArrayList<>();
        switch (ref) {
        case "http" -> {
            // This Monitor's own HttpPlugin (ecmwf.ecpds.master.plugin.http.HttpPlugin) binds its HTTPS port from
            // the [MonitorPlugin] section (there is no separate plain-HTTP listener here, unlike the Data Mover's
            // HttpPlugin which uses its own [HttpPlugin] http/https keys).
            final var https = Cnf.at("MonitorPlugin", "https", -1);
            if (https > 0) {
                ports.add(https);
            }
        }
        case "mqtt" -> {
            final var mqtt = Cnf.at("MqttPlugin", "mqtt", -1);
            final var mqtts = Cnf.at("MqttPlugin", "mqtts", -1);
            if (mqtt > 0) {
                ports.add(mqtt);
            }
            if (mqtts > 0) {
                ports.add(mqtts);
            }
        }
        case "ssh" -> {
            final var port = Cnf.at("SshPlugin", "port", -1);
            if (port > 0) {
                ports.add(port);
            }
        }
        default -> {
            // No known fallback for this plugin ref.
        }
        }
        return ports;
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
