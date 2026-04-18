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
 * Returns a GeoJSON FeatureCollection of all hosts that have coordinates set,
 * enabling the map page to display host locations on the fly without static KML files.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class GetHostMapJsonAction.
 *
 * Handles AJAX requests for the host map page. Returns a GeoJSON FeatureCollection containing all hosts that match the
 * supplied filter criteria and have latitude/longitude coordinates available via GeoIP or manual entry.
 */
public class GetHostMapJsonAction extends PDSAction {

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Base path for host detail pages. */
    private static final String HOST_BASE_PATH = "/do/transfer/host";

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        // Accept the same filter params as the host list endpoint so the map
        // respects the full server-side query (wildcards, field tokens, etc.).
        final var label = param(request, "label", "All");
        final var filter = param(request, "hostFilter", "All");
        final var network = param(request, "network", "All");
        final var hostType = param(request, "hostType", "All");
        final var hostSearch = param(request, "hostSearch", "");

        // Use an unlimited cursor: sort by name asc, return everything.
        final var cursor = new DataBaseCursor("0", "asc", 0, Integer.MAX_VALUE);

        Collection<Host> hosts;
        try {
            hosts = HostHome.findByCriteria(label, filter, network, hostType, hostSearch, cursor);
        } catch (final TransferException e) {
            hosts = new ArrayList<>(0);
        }

        final var root = MAPPER.createObjectNode();
        root.put("type", "FeatureCollection");
        final var features = root.putArray("features");

        for (final Host host : hosts) {
            Double lat, lon;
            try {
                // GeoIP resolution only runs on the Master; call getHost() via RMI so the
                // Master performs the lookup and returns a fully-populated Host with lat/lon.
                final var dbHost = MasterManager.getDB().getHost(host.getName());
                final var loc = dbHost.getHostLocation();
                if (loc == null) {
                    continue;
                }
                lat = loc.getLatitude();
                lon = loc.getLongitude();
            } catch (final Exception e) {
                continue;
            }
            if (lat == null || lon == null || (lat == 0.0 && lon == 0.0)) {
                continue;
            }

            final var feature = MAPPER.createObjectNode();
            feature.put("type", "Feature");

            final var geometry = feature.putObject("geometry");
            geometry.put("type", "Point");
            final var coords = geometry.putArray("coordinates");
            coords.add(lon);
            coords.add(lat);

            final var props = feature.putObject("properties");
            props.put("id", safeStr(host.getName()));
            props.put("nickname", safeStr(host.getNickName()));
            props.put("hostname", safeStr(host.getHost()));
            props.put("type", safeStr(host.getType()));
            props.put("active", host.getActive());
            props.put("geo", safeStr(host.getGeoIpLocation()));
            props.put("network", safeStr(host.getNetworkName()));
            props.put("method", safeStr(host.getTransferMethodName()));
            props.put("comment", safeStr(host.getComment()));
            props.put("url", HOST_BASE_PATH + "/" + escapeHtml(host.getName()));

            features.add(feature);
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, "Error building host map GeoJSON: " + e.getMessage());
        }
        return null;
    }

    private static String safeStr(final String s) {
        return s != null ? s : "";
    }

    private static String param(final HttpServletRequest request, final String name, final String defaultValue) {
        final var v = request.getParameter(name);
        return (v != null && !v.isBlank()) ? v : defaultValue;
    }

    private static String escapeHtml(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
                "&#39;");
    }

    private static void writeError(final HttpServletResponse response, final String message) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            final ObjectNode err = MAPPER.createObjectNode();
            err.put("type", "FeatureCollection");
            err.putArray("features");
            err.put("error", message);
            MAPPER.writeValue(response.getWriter(), err);
        } catch (final Exception ignored) {
        }
    }
}
