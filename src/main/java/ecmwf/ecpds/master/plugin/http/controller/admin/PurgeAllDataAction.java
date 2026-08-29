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
 * Two-step destructive action that marks ALL DataTransfers and DataFiles for
 * deletion, causing the purgeDataBase scheduler to remove DB records and the
 * ExpiredDataFileScheduler to physically delete files from data-mover disks.
 *
 * Intended exclusively for test-environment resets. Protected by:
 *   1. An "I understand" checkbox (step 1)
 *   2. Exact-phrase confirmation — user must type "PURGE ALL DATA" (step 2)
 *
 * @author Laurent Gougeon, ECMWF.
 * @version 6.7.7
 * @since 2026-08-28
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
 * The Class PurgeAllDataAction.
 */
public class PurgeAllDataAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(PurgeAllDataAction.class);

    /** Exact phrase the user must type to confirm the destructive operation. */
    public static final String CONFIRMATION_PHRASE = "PURGE ALL DATA";

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var step = request.getParameter("step");
        if ("1".equals(step)) {
            // Checkbox confirmed → show second confirmation (phrase + optional password)
            // Carry the forceDeleteDB flag so confirm2.jsp can pass it forward as a hidden field.
            request.setAttribute("forceDeleteDB",
                    Boolean.valueOf("true".equals(request.getParameter("forceDeleteDB"))));
            _populatePasswordFlag(request);
            return mapping.findForward("confirm2");
        }
        if ("2".equals(step)) {
            final var phrase = request.getParameter("phrase");
            if (!CONFIRMATION_PHRASE.equals(phrase)) {
                request.setAttribute("purgeError",
                        "Confirmation phrase did not match. No data was deleted. Please try again.");
                request.setAttribute("forceDeleteDB",
                        Boolean.valueOf("true".equals(request.getParameter("forceDeleteDB"))));
                _populatePasswordFlag(request);
                return mapping.findForward("confirm2");
            }
            // Validate server-side password if one is configured
            try {
                final var db = MasterManager.getDB();
                if (db.hasCriticalActionPassword()) {
                    final var passwordAttempt = request.getParameter("purgePassword");
                    if (!db.validateCriticalActionPassword(passwordAttempt)) {
                        request.setAttribute("purgeError",
                                "Server password incorrect. No data was deleted. Please try again.");
                        request.setAttribute("forceDeleteDB",
                                Boolean.valueOf("true".equals(request.getParameter("forceDeleteDB"))));
                        _populatePasswordFlag(request);
                        return mapping.findForward("confirm2");
                    }
                }
                // All validations passed — execute the purge
                final var triggerNow = "true".equals(request.getParameter("triggerNow"));
                final var forceDeleteDB = "true".equals(request.getParameter("forceDeleteDB"));
                final int transfers = db.markAllDataTransfersForPurge();
                final int files = db.markAllDataFilesForPurge();
                log.warn(
                        "PURGE ALL DATA triggered by user '{}' — {} transfer(s) and {} data file(s) "
                                + "marked for deletion (triggerNow={}, forceDeleteDB={})",
                        user.getId(), transfers, files, triggerNow, forceDeleteDB);
                if (triggerNow) {
                    db.triggerAllPurge();
                    log.warn("PURGE ALL DATA: purge schedulers triggered immediately by user '{}'", user.getId());
                }
                if (forceDeleteDB) {
                    db.deleteAllDataImmediatelyAsync();
                    log.warn("PURGE ALL DATA: background hard-delete of all DB records started for user '{}'",
                            user.getId());
                }
                request.setAttribute("purgedTransfers", transfers);
                request.setAttribute("purgedFiles", files);
                request.setAttribute("triggered", Boolean.valueOf(triggerNow));
                request.setAttribute("forceDeleteDB", Boolean.valueOf(forceDeleteDB));
            } catch (final Exception e) {
                log.error("PurgeAllDataAction: purge failed", e);
                request.setAttribute("purgeError", "Purge failed: " + e.getMessage());
                request.setAttribute("forceDeleteDB",
                        Boolean.valueOf("true".equals(request.getParameter("forceDeleteDB"))));
                _populatePasswordFlag(request);
                return mapping.findForward("confirm2");
            }
            return mapping.findForward("done");
        }
        // Default: show step-1 warning page
        return mapping.findForward("form");
    }

    /**
     * Sets the requiresPassword request attribute so confirm2.jsp can conditionally show the password field.
     */
    private static void _populatePasswordFlag(final HttpServletRequest request) {
        try {
            request.setAttribute("requiresPassword",
                    Boolean.valueOf(MasterManager.getDB().hasCriticalActionPassword()));
        } catch (final Exception e) {
            request.setAttribute("requiresPassword", Boolean.FALSE);
        }
    }
}
