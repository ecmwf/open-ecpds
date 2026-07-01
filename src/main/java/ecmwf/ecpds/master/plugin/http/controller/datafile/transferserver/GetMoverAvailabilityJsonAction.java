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
 * Returns a JSON time-series of per-minute availability for a single DataMover, sourced from the
 * MOVER_AVAILABILITY_SNAPSHOT database table populated by the background MoverAvailabilityScheduler.
 *
 * URL pattern (registered in struts-config.xml):
 *   GET /do/datafile/moveravailability/{name}?hours={n}
 *
 * Response format:
 * <pre>
 * {
 *   "mover":  "bodh2ecpdmv-24",
 *   "hours":  168,
 *   "uptime": 99.3,
 *   "data": [
 *     [1751234400000, 1],
 *     [1751234460000, 0],
 *     ...
 *   ]
 * }
 * </pre>
 * Each element of {@code data} is {@code [minuteEpochMs, available]} where available is 1 (up) or 0 (down).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

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
 * Serves per-minute DataMover availability time-series as JSON for dashboard visualisations.
 */
public class GetMoverAvailabilityJsonAction extends PDSAction {

    /** Default retention window exposed to the chart (7 days). */
    private static final int DEFAULT_HOURS = 168;

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

        int hours = DEFAULT_HOURS;
        try {
            final var hoursParam = request.getParameter("hours");
            if (hoursParam != null && !hoursParam.isBlank()) {
                hours = Math.min(Math.max(1, Integer.parseInt(hoursParam)), 8760);
            }
        } catch (final NumberFormatException ignored) {
            // use default
        }

        final var root = MAPPER.createObjectNode();
        root.put("mover", moverName != null ? moverName : "");
        root.put("hours", hours);

        final var dataArr = root.putArray("data");
        long upCount = 0;
        long totalCount = 0;
        if (moverName != null && !moverName.isBlank()) {
            try {
                final var snapshots = MasterManager.getMI().getMoverAvailability(moverName, hours);
                for (final long[] entry : snapshots) {
                    final var point = dataArr.addArray();
                    point.add(entry[0]);
                    point.add(entry[1]);
                    if (entry[1] == 1)
                        upCount++;
                    totalCount++;
                }
            } catch (final Exception e) {
                // leave data empty
            }
        }
        root.put("uptime", totalCount > 0 ? Math.round(1000.0 * upCount / totalCount) / 10.0 : -1.0);

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
