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
 * Accepts a comma-separated list of IP addresses / hostnames in the {@code ips} query parameter and returns a JSON
 * array of GeoIP lookup results. GeoIP resolution runs in the MasterServer JVM (via RMI); this action only formats the
 * results as JSON.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ecmwf.common.database.GeoIpData;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class GetGeoIpJsonAction.
 *
 * Resolves a caller-supplied list of IPs/hostnames to geographic coordinates using the MasterServer's GeoIP database
 * (via RMI). Returns a JSON array: [{ip, lat, lon, geo}, ...].
 */
public class GetGeoIpJsonAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(GetGeoIpJsonAction.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Maximum number of IPs accepted per request to prevent abuse. */
    private static final int MAX_IPS = 100;

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var ipsParam = request.getParameter("ips");
        final ArrayNode arr = MAPPER.createArrayNode();

        if (ipsParam != null && !ipsParam.isBlank()) {
            final List<String> ips = Arrays.stream(ipsParam.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                    .limit(MAX_IPS).collect(Collectors.toList());
            try {
                final List<GeoIpData> results = MasterManager.getDB().geoLocateIps(ips);
                for (final var r : results) {
                    final var node = MAPPER.createObjectNode();
                    node.put("ip", r.ip());
                    if (r.lat() != null && r.lon() != null) {
                        node.put("lat", r.lat());
                        node.put("lon", r.lon());
                    } else {
                        node.putNull("lat");
                        node.putNull("lon");
                    }
                    node.put("geo", r.geo() != null ? r.geo() : "");
                    arr.add(node);
                }
            } catch (final Exception e) {
                _log.warn("GeoIP lookup failed for {} IPs: {}", ips.size(), e.getMessage());
            }
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), arr);
        } catch (final Exception e) {
            _log.error("Failed to write GeoIP JSON response", e);
        }
        return null;
    }
}
