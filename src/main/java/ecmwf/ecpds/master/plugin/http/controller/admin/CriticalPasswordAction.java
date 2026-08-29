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
 * Handles the "Critical Action Password" admin page. The password is stored as a SHA-256 hex digest in the SYS_CONFIG
 * database table (group="Master", name="criticalActionPasswordHash") and is completely separate from the user's normal
 * administrator password.
 *
 * <p>
 * GET: shows the form (initial setup when no password exists, or renewal when one already exists).
 * <p>
 * POST: validates current password (renewal only), checks that new password matches confirmation, hashes the new
 * password, and stores the hash.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2026-08-29
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class CriticalPasswordAction.
 */
public class CriticalPasswordAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(CriticalPasswordAction.class);

    /** {@inheritDoc} */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        final ecmwf.ecpds.master.DataBaseInterface db;
        try {
            db = MasterManager.getDB();
        } catch (final Exception e) {
            request.setAttribute("capError", "Unable to connect to the database: " + e.getMessage());
            request.setAttribute("hasPassword", Boolean.FALSE);
            return mapping.findForward("form");
        }

        final boolean hasPassword;
        try {
            hasPassword = db.hasCriticalActionPassword();
        } catch (final Exception e) {
            request.setAttribute("capError", "Unable to connect to the database: " + e.getMessage());
            return mapping.findForward("form");
        }

        request.setAttribute("hasPassword", hasPassword);

        // GET — show form
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return mapping.findForward("form");
        }

        // POST — validate and store
        final var newPassword = request.getParameter("newPassword");
        final var confirmPassword = request.getParameter("confirmPassword");
        final var currentPassword = request.getParameter("currentPassword");

        // Validate new password length
        if (newPassword == null || newPassword.length() < 12) {
            request.setAttribute("capError", "New password must be at least 12 characters.");
            return mapping.findForward("form");
        }

        // Validate confirmation matches
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("capError", "New password and confirmation do not match.");
            return mapping.findForward("form");
        }

        // If renewing, verify the current password
        if (hasPassword) {
            try {
                if (!db.validateCriticalActionPassword(currentPassword)) {
                    request.setAttribute("capError", "Current password is incorrect.");
                    return mapping.findForward("form");
                }
            } catch (final Exception e) {
                request.setAttribute("capError", "Validation failed: " + e.getMessage());
                return mapping.findForward("form");
            }
        }

        // Hash and store the new password
        final var hash = _sha256Hex(newPassword);
        if (hash.isEmpty()) {
            request.setAttribute("capError", "Internal error: could not hash the password.");
            return mapping.findForward("form");
        }
        try {
            db.setCriticalActionPasswordHash(hash);
            log.warn("Critical Action Password {} by user '{}'", hasPassword ? "renewed" : "set for the first time",
                    user.getUid());
            request.setAttribute("capSuccess", hasPassword ? "Critical Action Password renewed successfully."
                    : "Critical Action Password set successfully.");
        } catch (final Exception e) {
            log.error("setCriticalActionPasswordHash failed", e);
            request.setAttribute("capError", "Failed to store the password: " + e.getMessage());
            return mapping.findForward("form");
        }

        request.setAttribute("hasPassword", true);
        return mapping.findForward("form");
    }

    /**
     * Computes the SHA-256 hex digest of the given plaintext string.
     */
    private static String _sha256Hex(final String text) {
        try {
            final var digest = java.security.MessageDigest.getInstance("SHA-256");
            final var bytes = digest.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            final var sb = new StringBuilder(bytes.length * 2);
            for (final byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final Exception e) {
            log.warn("_sha256Hex failed", e);
            return "";
        }
    }
}
