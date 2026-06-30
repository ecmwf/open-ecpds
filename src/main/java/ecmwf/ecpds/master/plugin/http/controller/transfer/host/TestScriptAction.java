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
 * Executes a Directory script (JavaScript or Python) on the appropriate DataMover and returns the
 * result as JSON. This replicates the exact production execution path used during acquisition.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * The Class TestScriptAction.
 *
 * Accepts POST to /do/transfer/host/edit/testScript/{hostId} with parameters: lang - "js" or "python" script - the
 * script source code (without the $(...) wrapper)
 *
 * Executes the script on a DataMover in the host's transfer group via the MasterServer RMI interface, exactly as the
 * application does during acquisition, and returns JSON: {"output": "...", "mover": "..."} or {"error": "..."}
 */
public class TestScriptAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(TestScriptAction.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Maximum script size accepted (128 KB). */
    private static final int MAX_SCRIPT_BYTES = 131072;

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var result = MAPPER.createObjectNode();
        try {
            final var hostId = ECMWFActionForm.getPathParameter(mapping, request, 0);
            final var lang = request.getParameter("lang");
            final var script = request.getParameter("script");

            if (hostId == null || hostId.isBlank()) {
                writeError(response, result, "Missing host ID");
                return null;
            }
            if (script == null || script.isBlank()) {
                writeError(response, result, "Script is empty");
                return null;
            }
            if (script.length() > MAX_SCRIPT_BYTES) {
                writeError(response, result, "Script exceeds maximum allowed size");
                return null;
            }

            // Build the prefixed script content (as MasterServer sends to the DataMover).
            // ScriptManager.exec() auto-detects "python:" / "js:" prefix.
            final var langPrefix = "python".equalsIgnoreCase(lang) ? "python:" : "js:";
            final var prefixedScript = langPrefix + script;

            // Load the DB Host object (needed by ManagementInterface / TransferScheduler).
            final var host = MasterManager.getDB().getHost(hostId);
            if (host == null) {
                writeError(response, result, "Host not found: " + hostId);
                return null;
            }

            _log.debug("TestScript: host={} lang={}", hostId, lang);

            // Route execution through MasterServer → DataMover via RMI (same as production).
            // execDirScript returns the decoded output String directly — no stream handling needed.
            final var session = Util.getECpdsSessionFromObject(user);
            result.put("output", MasterManager.getMI().execDirScript(session, host, prefixedScript));

        } catch (final Exception e) {
            _log.warn("TestScript execution error", e);
            writeError(response, result, Format.getMessage(e));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), result);
        } catch (final Exception e) {
            _log.error("Failed to write TestScript JSON response", e);
        }
        return null;
    }

    private static void writeError(final HttpServletResponse response, final ObjectNode node, final String message) {
        node.put("error", message);
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), node);
        } catch (final Exception e) {
            _log.error("Failed to write error JSON response", e);
        }
    }
}
