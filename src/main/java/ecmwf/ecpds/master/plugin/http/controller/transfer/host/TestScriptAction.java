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
 * Before executing, resolves all placeholder families supported by TransferManagement:
 * - Always: $host[...], $transferMethod[...], $ectransModule[...] (via MasterServer.execDirScript)
 * - When transferId is supplied: $dataFile[...], $dataTransfer[...], $destination[...],
 *   $country[...], $transferGroup[...], $transferServer[...], $moverName
 * - When valuesJson is supplied: arbitrary key→value substitutions from the JSON map
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.MasterServer;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.transfer.StatusFactory;
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

            // Pre-process transfer-specific placeholders before sending to the DataMover.
            // MasterServer.execDirScript() handles $host/$transferMethod/$ectransModule itself.
            final var transferId = request.getParameter("transferId");
            final var valuesJson = request.getParameter("valuesJson");

            var scriptToRun = prefixedScript;
            if (transferId != null && !transferId.isBlank()) {
                scriptToRun = resolveTransferPlaceholders(prefixedScript, transferId);
            } else if (valuesJson != null && !valuesJson.isBlank()) {
                scriptToRun = resolveManualValues(prefixedScript, valuesJson);
            }

            // Route execution through MasterServer → DataMover via RMI (same as production).
            final var session = Util.getECpdsSessionFromObject(user);
            // Resolve acquisition-format lines in the output: strip [options] prefix,
            // resolve $date/$dirdate, strip {regex} suffix — ready for the Preview mechanism.
            result.put("output",
                    MasterServer.resolveAcqOutput(MasterManager.getMI().execDirScript(session, host, scriptToRun)));

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

    /**
     * Resolves all transfer-specific placeholders by loading the DataTransfer with the given ID and substituting
     * $dataFile[...], $dataTransfer[...], $destination[...], $country[...], $transferGroup[...], $transferServer[...],
     * and $moverName — exactly matching the production substitutions performed by TransferManagement.getTargetName().
     */
    private static String resolveTransferPlaceholders(final String script, final String transferId) {
        try {
            final DataTransfer transfer = DataTransferHome.findByPrimaryKey(transferId);
            final var target = new File(nullSafe(transfer.getTarget()));
            final var status = StatusFactory.getDataTransferStatusName(false, transfer.getStatusCode());
            final var sb = new StringBuilder(script);

            // $moverName — use transfer server name as proxy (actual mover is allocated at runtime)
            try {
                final var server = transfer.getTransferServer();
                Format.replaceAll(sb, "$moverName", server.getName());
                Format.replaceAll(sb, "$transferServer[name]", server.getName());
                Format.replaceAll(sb, "$transferServer[host]", server.getHost());
                Format.replaceAll(sb, "$transferServer[port]", server.getPort());
                try {
                    final var group = server.getTransferGroup();
                    Format.replaceAll(sb, "$transferGroup[name]", group.getName());
                    Format.replaceAll(sb, "$transferGroup[comment]", group.getComment());
                } catch (final Exception ignored) {
                }
            } catch (final Exception ignored) {
                Format.replaceAll(sb, "$moverName", "");
            }

            try {
                final var dest = transfer.getDestination();
                Format.replaceAll(sb, "$destination[name]", dest.getName());
                Format.replaceAll(sb, "$destination[comment]", dest.getComment());
                Format.replaceAll(sb, "$destination[userMail]", dest.getUserMail());
                try {
                    final var country = dest.getCountry();
                    Format.replaceAll(sb, "$country[name]", country.getName());
                    Format.replaceAll(sb, "$country[iso]", country.getIso());
                } catch (final Exception ignored) {
                }
            } catch (final Exception ignored) {
            }

            try {
                final var file = transfer.getDataFile();
                Format.replaceAll(sb, "$dataFile[timeStep]", file.getTimeStep());
                Format.replaceAll(sb, "$dataFile[arrivedTime]", toString(file.getArrivedTime()));
                Format.replaceAll(sb, "$dataFile[id]", Long.toString(transfer.getDataFileId()));
                Format.replaceAll(sb, "$dataFile[original]", file.getOriginal());
                Format.replaceAll(sb, "$dataFile[source]", file.getSource());
                Format.replaceAll(sb, "$dataFile[formatSize]", Format.formatSize(file.getSize()));
                Format.replaceAll(sb, "$dataFile[size]", file.getSize());
                Format.replaceAll(sb, "$dataFile[timeBase]", toString(file.getProductTime()));
                Format.replaceAll(sb, "$dataFile[timeFile]", toString(file.getProductGenerationTime()));
                Format.replaceAll(sb, "$dataFile[metaTime]", file.getMetaTime());
                Format.replaceAll(sb, "$dataFile[metaStream]", file.getMetaStream());
                Format.replaceAll(sb, "$dataFile[checksum]", nullSafe(file.getChecksum()));
            } catch (final Exception ignored) {
            }

            Format.replaceAll(sb, "$dataTransfer[target]", nullSafe(transfer.getTarget()));
            Format.replaceAll(sb, "$dataTransfer[id]", transferId);
            Format.replaceAll(sb, "$dataTransfer[comment]", nullSafe(transfer.getComment()));
            Format.replaceAll(sb, "$dataTransfer[identity]", nullSafe(transfer.getIdentity()));
            Format.replaceAll(sb, "$dataTransfer[priority]", transfer.getPriority());
            Format.replaceAll(sb, "$dataTransfer[scheduled]", toString(transfer.getScheduledTime()));
            Format.replaceAll(sb, "$dataTransfer[statusCode]", nullSafe(status));
            Format.replaceAll(sb, "$dataTransfer[name]", target.getName());
            Format.replaceAll(sb, "$dataTransfer[path]", nullSafe(target.getPath()));
            Format.replaceAll(sb, "$dataTransfer[parent]", nullSafe(target.getParent()));
            Format.replaceAll(sb, "$dataTransfer[asap]", transfer.getAsap());
            return sb.toString();
        } catch (final Exception e) {
            _log.warn("Could not resolve transfer placeholders for transferId={}: {}", transferId, e.getMessage());
            return script;
        }
    }

    /**
     * Resolves manually supplied placeholder values from a JSON map. Expects JSON like: {"$dataFile[original]":
     * "myfile.grib", "$dataTransfer[target]": "out.grib"}
     */
    private static String resolveManualValues(final String script, final String valuesJson) {
        try {
            final Map<String, String> values = MAPPER.readValue(valuesJson, new TypeReference<Map<String, String>>() {
            });
            final var sb = new StringBuilder(script);
            for (final var entry : values.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    Format.replaceAll(sb, entry.getKey(), entry.getValue());
                }
            }
            return sb.toString();
        } catch (final Exception e) {
            _log.warn("Could not apply manual placeholder values: {}", e.getMessage());
            return script;
        }
    }

    private static String nullSafe(final Object v) {
        return v != null ? v.toString() : "";
    }

    private static String toString(final java.util.Date d) {
        return d != null ? d.toString() : "";
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
