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
 * Returns a JSON array of recent DataTransfers for a given host, suitable for populating the "pick a
 * DataTransfer" modal used by the script/Directory test pre-flight.
 *
 * Transfers are looked up via the Destination(s) associated with the host (not via the DataTransfer's
 * own HOS_NAME column): HOS_NAME is only populated once the scheduler actually dispatches a transfer to
 * a specific host, so a HOLD ("StandBy") or WAIT ("Queued") transfer that has not been picked up yet
 * would otherwise never show up here even though it genuinely belongs to a Destination this host
 * serves - which is exactly the case (files already waiting at/for the destination) this picker exists
 * for.
 *
 * URL pattern (registered in struts-config.xml):
 *   GET /do/transfer/host/edit/recentTransfers/{hostId}
 *
 * Response format:
 * <pre>
 * [
 *   {
 *     "id": "12345",
 *     "target": "myfile.grib",
 *     "destination": "myDest",
 *     "statusCode": "DONE",
 *     "dataFileName": "original_name.grib"
 *   },
 *   ...
 * ]
 * </pre>
 *
 * Returns an empty array if no transfers found, or on any error.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.transfer.DataTransferLightBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * Serves recent DataTransfers for a host as JSON for the script-test pre-flight picker.
 */
public class GetRecentTransfersAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(GetRecentTransfersAction.class);

    /** Maximum number of transfers returned. */
    private static final int MAX_RESULTS = 50;

    /** Shared Jackson mapper (thread-safe). */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var arr = MAPPER.createArrayNode();
        try {
            final var hostId = ECMWFActionForm.getPathParameter(mapping, request, 0);
            if (hostId == null || hostId.isBlank()) {
                writeJson(response, arr);
                return null;
            }
            final var host = HostHome.findByPrimaryKey(hostId);
            Collection<Destination> destinations;
            try {
                destinations = DestinationHome.findByHost(host);
            } catch (final Exception e) {
                _log.debug("GetRecentTransfers: findByHost failed for host {}", hostId, e);
                destinations = null;
            }

            final List<DataTransferLightBean> merged = new ArrayList<>();
            if (destinations != null) {
                final var cursor = new DataBaseCursor("2", "2", 0, MAX_RESULTS);
                for (final var destination : destinations) {
                    try {
                        merged.addAll(DataTransferHome.findByFilter(destination.getName(), DataTransferHome.ALL,
                                DataTransferHome.ALL, DataTransferHome.ALL, DataTransferHome.ALL, true, null, null,
                                cursor));
                    } catch (final Exception e) {
                        _log.debug("GetRecentTransfers: findByFilter failed for destination {}", destination.getName(),
                                e);
                    }
                }
            }
            if (merged.isEmpty()) {
                // Host has no associated Destination (or none found) - fall back to the host's own
                // transfer history so the picker still shows something for e.g. Acquisition hosts.
                merged.addAll(DataTransferHome.findLastByHost(host, MAX_RESULTS));
            } else {
                merged.sort(Comparator.comparing(DataTransferLightBean::getScheduledTime,
                        Comparator.nullsLast(Comparator.reverseOrder())));
            }

            for (final var transfer : merged.stream().limit(MAX_RESULTS).toList()) {
                final var node = arr.addObject();
                node.put("id", String.valueOf(transfer.getId()));
                node.put("target", nullSafe(transfer.getTarget()));
                node.put("destination", nullSafe(transfer.getDestinationName()));
                node.put("statusCode", nullSafe(transfer.getStatusCode()));
                try {
                    final var df = transfer.getDataFile();
                    node.put("dataFileName", df != null ? nullSafe(df.getOriginal()) : "");
                } catch (final Exception ignored) {
                    node.put("dataFileName", "");
                }
            }
        } catch (final Exception e) {
            _log.debug("GetRecentTransfers error", e);
        }
        writeJson(response, arr);
        return null;
    }

    private static String nullSafe(final Object v) {
        return v != null ? v.toString() : "";
    }

    private static void writeJson(final HttpServletResponse response,
            final com.fasterxml.jackson.databind.node.ArrayNode arr) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), arr);
        } catch (final Exception e) {
            _log.error("Failed to write GetRecentTransfers JSON response", e);
        }
    }
}
