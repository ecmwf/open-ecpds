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
 * Returns a JSON snapshot of in-flight download counts per data mover and volume, sourced from
 * the live {@code TransferScheduler._metricsPerDataMovers} map via RMI.
 *
 * URL: GET /do/datafile/moverdownloads/data
 *
 * Response format:
 * <pre>
 * {
 *   "downloads": {
 *     "group-04.bodh1ecpdmv-04": [3, 0, 1, 0, ...],
 *     "group-04.bodh2ecpdmv-04": [0, 0, 0, 0, ...]
 *   }
 * }
 * </pre>
 *
 * The array index corresponds to the volume (filesystem) index; the value is the current number
 * of in-flight downloads on that volume for that data mover.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Serves per-mover per-volume in-flight download counts as JSON for the Download Activity matrix.
 */
public class GetMoverDownloadsJsonAction extends PDSAction {

    /** Shared Jackson mapper (thread-safe). */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        Map<String, int[]> metrics;
        try {
            metrics = MasterManager.getMI().getDownloadMetrics();
        } catch (final Exception e) {
            metrics = Map.of();
        }

        final var root = MAPPER.createObjectNode();
        final var downloads = root.putObject("downloads");

        for (final var entry : metrics.entrySet()) {
            final var arr = downloads.putArray(entry.getKey());
            for (final int count : entry.getValue()) {
                arr.add(count);
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
