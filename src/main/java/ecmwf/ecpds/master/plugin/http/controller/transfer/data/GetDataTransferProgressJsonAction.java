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

package ecmwf.ecpds.master.plugin.http.controller.transfer.data;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Returns a lightweight JSON snapshot of a single data transfer's live progress
 * metrics (progress %, duration, rate, sent bytes, status code). Used by the
 * data transfer detail page to poll for updates while the transfer is active.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * The Class GetDataTransferProgressJsonAction.
 *
 * Handles AJAX polling requests for the data transfer detail page. Returns a compact JSON object with the current
 * progress, duration, transfer rate, bytes sent, and status code of a single data transfer.
 */
public class GetDataTransferProgressJsonAction extends PDSAction {

    /** Shared Jackson mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        final ArrayList<?> params = ECMWFActionForm.getPathParameters(mapping, request);
        if (params.isEmpty()) {
            writeError(response, "Transfer ID required");
            return null;
        }

        final var id = params.get(0).toString();
        DataTransfer transfer;
        try {
            transfer = DataTransferHome.findByPrimaryKey(id);
        } catch (final Exception e) {
            writeError(response, "Transfer not found: " + id);
            return null;
        }

        try {
            final var root = MAPPER.createObjectNode();
            final var statusCode = transfer.getStatusCode();
            root.put("statusCode", statusCode);
            root.put("transferring", StatusFactory.EXEC.equals(statusCode) || StatusFactory.FETC.equals(statusCode)
                    || StatusFactory.INIT.equals(statusCode));

            int progress = 0;
            try {
                progress = transfer.getProgress();
            } catch (final Exception ignored) {
            }
            root.put("progress", progress);

            long sent = 0;
            try {
                sent = transfer.getSent();
            } catch (final Exception ignored) {
            }
            root.put("sent", sent);

            String formattedSent = "";
            try {
                formattedSent = transfer.getFormattedSent();
            } catch (final Exception ignored) {
            }
            root.put("formattedSent", formattedSent != null ? formattedSent : "");

            String formattedDuration = "";
            try {
                formattedDuration = transfer.getFormattedDuration();
            } catch (final Exception ignored) {
            }
            root.put("formattedDuration", formattedDuration != null ? formattedDuration : "");

            long transferRate = 0;
            try {
                transferRate = transfer.getTransferRate();
            } catch (final Exception ignored) {
            }
            root.put("transferRate", transferRate);

            String formattedTransferRate = "";
            try {
                formattedTransferRate = transfer.getFormattedTransferRate();
            } catch (final Exception ignored) {
            }
            root.put("formattedTransferRate", formattedTransferRate != null ? formattedTransferRate : "");

            double rateMbits = 0;
            try {
                rateMbits = transfer.getFormattedTransferRateInMBitsPerSeconds();
            } catch (final Exception ignored) {
            }
            root.put("formattedTransferRateInMBitsPerSeconds", rateMbits);

            String formattedStatus = "";
            String detailedStatus = "";
            try {
                formattedStatus = transfer.getFormattedStatus();
                detailedStatus = transfer.getDetailedStatus();
            } catch (final Exception ignored) {
            }
            root.put("formattedStatus", formattedStatus != null ? formattedStatus : "");
            root.put("detailedStatus", detailedStatus != null ? detailedStatus : "");

            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception e) {
            writeError(response, "Error building progress response: " + e.getMessage());
        }
        return null;
    }

    /**
     * Writes a JSON error response.
     */
    private static void writeError(final HttpServletResponse response, final String message) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            final ObjectNode err = MAPPER.createObjectNode();
            err.put("error", message);
            MAPPER.writeValue(response.getWriter(), err);
        } catch (final Exception ignored) {
        }
    }
}
