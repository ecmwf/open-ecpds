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

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns, as JSON, a live status snapshot of every Dissemination Host configured for a destination: the number of
 * connections that destination currently has open on each host, plus, for the one host currently selected for new
 * dispatches, its remaining per-host retry budget before the scheduler moves on to the next host in priority order.
 * Used by the destination's Hosts table to highlight the active host and show how close it is to failing over,
 * without misrepresenting a host as "the only one in use" when {@code MAX CONNECTIONS > 1} allows several hosts to
 * be simultaneously in-flight.
 *
 * URL pattern (registered in struts-config.xml):
 *   GET /do/transfer/destination/hostsStatus/{destinationName}
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 7.4.0
 * @since 2026-09-21
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
 * Serves the per-destination Dissemination Hosts live status as JSON.
 */
public class GetDestinationHostsStatusJsonAction extends PDSAction {

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
        final var destinationName = params.size() == 1 ? params.get(0).toString() : null;

        java.util.List<java.util.Map<String, Object>> hostsStatus;
        try {
            hostsStatus = destinationName != null ? MasterManager.getMI().getDestinationHostsStatus(destinationName)
                    : java.util.List.of();
        } catch (final Exception e) {
            // Best-effort: e.g. the destination is currently off-line (no live HostProvider to query yet).
            hostsStatus = java.util.List.of();
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), hostsStatus);
        } catch (final Exception e) {
            // Response already committed or I/O error — nothing to recover
        }
        return null;
    }
}
