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
 * Syncs the client-side selection state to the server before a basket operation. Supports three modes:
 * <ul>
 * <li><b>replace</b> (type=replace or omitted): Clears all existing selections then marks the submitted IDs
 * (comma-separated in the "ids" field) as "on". Used after A/N/R which has the full selection in client memory.</li>
 * <li><b>delta</b> (type=delta): Applies incremental changes on top of the existing server selection. Marks IDs in the
 * "add" field as "on" and IDs in the "del" field (both comma-separated) as "off". Used after individual star-clicks
 * where the client only knows what changed.</li>
 * <li><b>basketAction</b> (type=basketAction): Syncs only the pages visited in the basket view. Sets IDs in the "on"
 * field as "on" and IDs in the "off" field as "off"; unvisited basket items remain unchanged in the session. Used
 * before a basket bulk action (requeue/stop/delete/priority) to avoid HTTP 414 from URL-encoded params.</li>
 * </ul>
 * The request body is JSON (Content-Type: application/json) so that Jetty's maxFormContentSize limit does not apply —
 * the routing parameter json=syncSelection is passed as a URL query string parameter instead. Called via AJAX before
 * the form submit so that the GET form URL does not carry thousands of selectedTransfer params (HTTP 431 prevention).
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDestinationSyncSelectionAction.
 */
public class GetDestinationSyncSelectionAction extends PDSAction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {

        try {
            JsonNode root;
            try (var reader = request.getReader()) {
                root = MAPPER.readTree(reader.lines().collect(Collectors.joining()));
            } catch (final IOException e) {
                root = MAPPER.createObjectNode();
            }

            final var destinationName = root.path("destinationName").asText("").trim();
            final var type = root.path("type").asText("replace");

            final var daf = (DetailActionForm) request.getSession().getAttribute("destinationDetailActionForm");
            if (daf != null && !destinationName.isEmpty()) {
                daf.setId(destinationName);
                if ("delta".equals(type)) {
                    // Apply incremental changes: add → "on", del → "off" (both comma-separated)
                    applyIds(daf, root.path("add").asText(""), "on");
                    applyIds(daf, root.path("del").asText(""), "off");
                } else if ("basketAction".equals(type)) {
                    // Sync basket action state without clearing unvisited items.
                    // on/off contain only IDs from pages the user visited; unvisited items
                    // keep their existing "on" state in the session (they were "on" when added to basket).
                    applyIds(daf, root.path("on").asText(""), "on");
                    applyIds(daf, root.path("off").asText(""), "off");
                } else {
                    // Replace mode: clear everything then set the provided IDs.
                    // JSON body bypasses Jetty's maxFormContentSize limit so any selection size works.
                    daf.cleanSelectedTransfers();
                    applyIds(daf, root.path("ids").asText(""), "on");
                }
            }
        } catch (final Exception _) {
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{}");
        } catch (final Exception _) {
        }
        return null;
    }

    private static void applyIds(final DetailActionForm daf, final String csv, final String value) {
        if (csv == null || csv.isBlank()) {
            return;
        }
        for (final var id : csv.split(",")) {
            final var trimmed = id.trim();
            if (!trimmed.isEmpty()) {
                daf.setSelectedTransfer(trimmed, value);
            }
        }
    }
}
