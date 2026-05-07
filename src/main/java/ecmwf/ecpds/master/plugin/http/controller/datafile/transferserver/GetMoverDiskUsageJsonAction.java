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

package ecmwf.ecpds.master.plugin.http.controller.datafile.transferserver;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a JSON snapshot of per-volume disk usage for one or all DataMovers (transfer servers),
 * sourced from the in-memory per-mover cache on the Master (no DataMover RMI calls at request
 * time — data is refreshed by the background usage updater).
 *
 * URL patterns (registered in struts-config.xml):
 *   GET /do/datafile/moverdiskusage        — all DataMovers
 *   GET /do/datafile/moverdiskusage/{name} — single DataMover
 *
 * Response format:
 * <pre>
 * {
 *   "movers": {
 *     "mover1": [
 *       { "volume": 0, "used": 1234567, "total": 1099511627776, "free": 1098277060209, "pct": 0 },
 *       ...
 *     ],
 *     ...
 *   }
 * }
 * </pre>
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * Serves per-volume disk usage for individual DataMovers as JSON for dashboard visualisations.
 */
public class GetMoverDiskUsageJsonAction extends PDSAction {

    /** Shared Jackson mapper (thread-safe). */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        final var moverName = params.isEmpty() ? null : params.get(0).toString();

        Map<String, long[][]> snapshot;
        try {
            snapshot = MasterManager.getMI().getMoverVolumeUsage(moverName);
        } catch (final Exception e) {
            snapshot = Map.of();
        }

        final var root = MAPPER.createObjectNode();
        final var movers = root.putObject("movers");

        // Sort mover names for stable output
        for (final var entry : new TreeMap<>(snapshot).entrySet()) {
            final var arr = movers.putArray(entry.getKey());
            final long[][] vols = entry.getValue();
            // vols[0] = usedPerVolume, vols[1] = totalPerVolume
            for (var i = 0; i < vols[0].length; i++) {
                final var used = vols[0][i];
                final var total = vols[1][i];
                final var free = Math.max(0L, total - used);
                final var pct = total > 0 ? (int) Math.round(100.0 * used / total) : 0;
                final var vol = arr.addObject();
                vol.put("volume", i);
                vol.put("used", used);
                vol.put("total", total);
                vol.put("free", free);
                vol.put("pct", pct);
            }
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
}
