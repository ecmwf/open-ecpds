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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.database.HostMapData;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class GetHostMapJsonAction.
 *
 * Handles AJAX requests for the host map page. Returns a GeoJSON FeatureCollection containing all hosts that match the
 * supplied filter criteria and have latitude/longitude coordinates available via GeoIP or manual entry.
 */
public class GetHostMapJsonAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(GetHostMapJsonAction.class);

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** GeoJSON cache: filter-key → full JSON string. Expires after 5 minutes. */
    private static final com.google.common.cache.Cache<String, String> GEOJSON_CACHE = com.google.common.cache.CacheBuilder
            .newBuilder().maximumSize(200).expireAfterWrite(5, java.util.concurrent.TimeUnit.MINUTES).build();

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
        final var label = param(request, "label", "All");
        final var filter = param(request, "hostFilter", "All");
        final var network = param(request, "network", "All");
        final var hostType = param(request, "hostType", "All");
        final var hostSearch = param(request, "hostSearch", "");
        final var isFullAccess = user.hasAccess(getResource(request, "transferhistory.basepath"));

        final var cacheKey = label + "|" + filter + "|" + network + "|" + hostType + "|" + hostSearch + "|"
                + (isFullAccess ? "full" : user.getId());
        var cached = GEOJSON_CACHE.getIfPresent(cacheKey);
        if (cached == null) {
            cached = buildGeoJson(label, filter, network, hostType, hostSearch, isFullAccess, user);
            GEOJSON_CACHE.put(cacheKey, cached);
        }
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(cached);
        } catch (final Exception e) {
            writeError(response, "Error writing host map GeoJSON: " + e.getMessage());
        }
        return null;
    }

    private String buildGeoJson(final String label, final String filter, final String network, final String hostType,
            final String hostSearch, final boolean isFullAccess, final User user) {
        final var root = MAPPER.createObjectNode();
        root.put("type", "FeatureCollection");
        final var features = root.putArray("features");

        try {
            final List<HostMapData> hosts;
            if (isFullAccess) {
                hosts = MasterManager.getDB().getHostsForMap(label, filter, network, hostType, hostSearch);
            } else {
                final var authorisedNames = new java.util.HashSet<>(
                        MasterManager.getDB().getAuthorisedHosts(user.getId()));
                hosts = MasterManager.getDB().getHostsForMap("All", "All", "All", "All", "").stream()
                        .filter(h -> authorisedNames.contains(h.id())).collect(java.util.stream.Collectors.toList());
            }
            for (final var h : hosts) {
                final var feature = MAPPER.createObjectNode();
                feature.put("type", "Feature");
                final var geometry = feature.putObject("geometry");
                geometry.put("type", "Point");
                final var coords = geometry.putArray("coordinates");
                coords.add(h.lon());
                coords.add(h.lat());
                final var props = feature.putObject("properties");
                props.put("id", h.id());
                props.put("nickname", h.nickname());
                props.put("hostname", h.hostname());
                props.put("type", h.type());
                props.put("active", h.active());
                props.put("geo", h.geo());
                props.put("network", h.network());
                props.put("method", h.method());
                props.put("comment", h.comment());
                props.put("url", HOST_BASE_PATH + "/" + escapeHtml(h.id()));
                features.add(feature);
            }
        } catch (final Exception e) {
            _log.warn("Error building host map GeoJSON", e);
        }

        try {
            return MAPPER.writeValueAsString(root);
        } catch (final Exception e) {
            return "{\"type\":\"FeatureCollection\",\"features\":[]}";
        }
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
