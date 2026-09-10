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
 * Handles the "Product Status Messages" admin page. Lets administrators edit the two pre-filled email bodies used on
 * the product monitoring page (product.jsp) to notify recipients of a dissemination delay, or that dissemination has
 * resumed. The messages are stored as plain text in the SYS_CONFIG database table (group "ProductStatus") instead of
 * being hardcoded in the JSP, so they can be adapted to each site's needs without a code change.
 *
 * <p>
 * GET: shows the form, pre-filled with the current (customized or built-in default) messages.
 * <p>
 * POST: stores the submitted messages (an empty submission resets a message back to its built-in default).
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2026-09-10
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.controller.monitoring.ProductStatusMessages;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class ProductMessagesAction.
 */
public class ProductMessagesAction extends PDSAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        final ecmwf.ecpds.master.DataBaseInterface db;
        try {
            db = MasterManager.getDB();
        } catch (final Exception e) {
            request.setAttribute("pmError", "Unable to connect to the database: " + e.getMessage());
            setDefaults(request);
            return mapping.findForward("form");
        }

        // POST — validate and store
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            final var delayMessage = request.getParameter("delayMessage");
            final var resumedMessage = request.getParameter("resumedMessage");
            try {
                if (isBlankOrDefault(delayMessage, ProductStatusMessages.DEFAULT_DELAY_MESSAGE)) {
                    db.resetProductStatusMessage(ProductStatusMessages.DELAY_MESSAGE_NAME);
                } else {
                    db.setProductStatusMessage(ProductStatusMessages.DELAY_MESSAGE_NAME, delayMessage);
                }
                if (isBlankOrDefault(resumedMessage, ProductStatusMessages.DEFAULT_RESUMED_MESSAGE)) {
                    db.resetProductStatusMessage(ProductStatusMessages.RESUMED_MESSAGE_NAME);
                } else {
                    db.setProductStatusMessage(ProductStatusMessages.RESUMED_MESSAGE_NAME, resumedMessage);
                }
                request.setAttribute("pmSuccess", "Product Status Messages saved successfully.");
            } catch (final Exception e) {
                request.setAttribute("pmError", "Failed to store the messages: " + e.getMessage());
            }
        }

        // GET (or after POST) — show the form pre-filled with the current values
        try {
            final var delayMessage = db.getProductStatusMessage(ProductStatusMessages.DELAY_MESSAGE_NAME);
            final var resumedMessage = db.getProductStatusMessage(ProductStatusMessages.RESUMED_MESSAGE_NAME);
            request.setAttribute("delayMessage",
                    delayMessage != null ? delayMessage : ProductStatusMessages.DEFAULT_DELAY_MESSAGE);
            request.setAttribute("resumedMessage",
                    resumedMessage != null ? resumedMessage : ProductStatusMessages.DEFAULT_RESUMED_MESSAGE);
            request.setAttribute("delayMessageCustomized", delayMessage != null);
            request.setAttribute("resumedMessageCustomized", resumedMessage != null);
        } catch (final Exception e) {
            request.setAttribute("pmError", "Unable to load the current messages: " + e.getMessage());
            setDefaults(request);
        }

        return mapping.findForward("form");
    }

    /**
     * Populates the request with the built-in default messages (used when the database cannot be reached).
     */
    private static void setDefaults(final HttpServletRequest request) {
        request.setAttribute("delayMessage", ProductStatusMessages.DEFAULT_DELAY_MESSAGE);
        request.setAttribute("resumedMessage", ProductStatusMessages.DEFAULT_RESUMED_MESSAGE);
        request.setAttribute("delayMessageCustomized", Boolean.FALSE);
        request.setAttribute("resumedMessageCustomized", Boolean.FALSE);
    }

    /**
     * Returns true if the submitted text is blank, or is unchanged from the built-in default (in which case there is no
     * point storing a custom override — the row is cleared instead, so future default updates still apply).
     */
    private static boolean isBlankOrDefault(final String submitted, final String defaultText) {
        return submitted == null || submitted.isBlank() || submitted.trim().equals(defaultText.trim());
    }
}
