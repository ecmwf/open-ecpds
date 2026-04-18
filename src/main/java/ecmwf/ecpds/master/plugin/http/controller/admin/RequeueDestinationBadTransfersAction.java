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

package ecmwf.ecpds.master.plugin.http.controller.admin;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Requeues all outstanding (bad) transfers for a single destination without
 * loading the full set into Java memory. Transfers are processed in batches;
 * after each batch is requeued its status changes so the next iteration always
 * fetches the new "first page" of remaining bad transfers.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * The Class RequeueDestinationBadTransfersAction.
 */
public class RequeueDestinationBadTransfersAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(RequeueDestinationBadTransfersAction.class);

    /** Number of transfers to process per database fetch. */
    private static final int BATCH_SIZE = 100;

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var destinationName = ECMWFActionForm.getPathParameter(mapping, request, 0);
        final var destination = DestinationHome.findByPrimaryKey(destinationName);
        if ("true".equals(request.getParameter("restart"))) {
            final var rawSearch = request.getParameter("search");
            final var search = rawSearch != null ? rawSearch.replace("'", "").replace("\\", "").trim() : "";
            // Process in batches. After requeue, status changes so we always fetch from
            // offset 0 — the next batch of still-bad transfers becomes the new first page.
            final var cursor = new DataBaseCursor("0", "1", 0, BATCH_SIZE, search);
            Collection<DataTransfer> batch;
            var count = 0;
            do {
                batch = DataTransferHome.findSortedBadByDestination(destination, cursor);
                var processed = 0;
                for (final DataTransfer transfer : batch) {
                    try {
                        transfer.requeue(user);
                        count++;
                        processed++;
                    } catch (final Exception e) {
                        log.warn("Problem requeuing Data Transfer '" + transfer.getId() + "'", e);
                    }
                }
                if (processed == 0) {
                    break; // No progress made — avoid infinite loop
                }
            } while (!batch.isEmpty());
            log.info("Requeued {} outstanding transfer(s) for destination '{}'", count, destinationName);
        }
        // Redirect back to the monitoring page — the AJAX table will reflect the new state
        try {
            response.sendRedirect(request.getContextPath() + "/do/monitoring/unsuccessful/" + destinationName);
        } catch (final Exception e) {
            log.warn("Redirect failed after requeue for destination '{}'", destinationName, e);
        }
        return null;
    }
}
