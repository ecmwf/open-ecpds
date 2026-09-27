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
 * Exports the destination metadata fields flagged "Include in Notes" ({@link
 * ecmwf.common.database.DestinationMetaValue#getIncludeInNotes()}) as an Opsview note, via
 * {@link MonitorManager#addNotes}. Triggered by the "Export Notes" button on the Destination Metadata page
 * (only shown when {@link MonitorManager#isActivated()} and the viewer can edit metadata - both re-checked here,
 * server-side). Returns JSON: {@code {"success":true}} or {@code {"success":false,"error":"..."}}, same contract as
 * {@link SaveDestinationMetaDataAction}.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2026-09-27
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.DestinationMetaField;
import ecmwf.common.database.DestinationMetaValue;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class ExportDestinationMetaNotesAction.
 */
public class ExportDestinationMetaNotesAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ExportDestinationMetaNotesAction.class);

    /** The Constant _mapper. */
    private static final ObjectMapper _mapper = new ObjectMapper();

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            if (!MonitorManager.isActivated()) {
                throw new IllegalStateException("Monitoring is not activated");
            }
            if (!GetDestinationMetaDataAction.canEditMeta(user)) {
                throw new IllegalStateException("Not authorized to edit destination metadata");
            }
            final var body = _mapper.readValue(request.getInputStream(), Map.class);
            final var destinationName = (String) body.get("destination");
            if (destinationName == null || destinationName.isBlank()) {
                throw new IllegalArgumentException("Missing destination");
            }
            final var noteBody = buildNoteBody(destinationName);
            MonitorManager.addNotes(destinationName, noteBody);
            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("ExportDestinationMetaNotesAction", e);
            try {
                final var msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "error";
                response.getWriter().write("{\"success\":false,\"error\":\"" + msg + "\"}");
            } catch (final Exception ignored) {
            }
        }
        return null; // already wrote response
    }

    /**
     * Builds the Opsview note body: one line per field flagged "Include in Notes" (with at least one non-blank value),
     * ordered the same way fields appear on the metadata page ({@link DestinationMetaField#getPosition()}). Password
     * fields are always excluded, even if somehow flagged, since their plaintext value must never leave this system.
     *
     * @param destinationName
     *            the destination name
     *
     * @return the note body (possibly empty, if nothing is flagged/non-blank)
     *
     * @throws Exception
     *             if the metadata fields/values cannot be loaded
     */
    private static String buildNoteBody(final String destinationName) throws Exception {
        final var db = MasterManager.getDB();
        final Map<Integer, DestinationMetaField> fieldsById = new HashMap<>();
        for (final DestinationMetaField f : db.getDestinationMetaFields()) {
            fieldsById.put(f.getId(), f);
        }
        final Map<Integer, List<String>> valuesByField = new LinkedHashMap<>();
        for (final DestinationMetaValue v : db.getDestinationMetaValuesByDestination(destinationName)) {
            if (!v.getIncludeInNotes() || v.getValue() == null || v.getValue().isBlank()) {
                continue;
            }
            final var field = fieldsById.get(v.getFieldId());
            if (field == null || "password".equals(field.getType())) {
                continue; // never export a password field, even if somehow flagged
            }
            valuesByField.computeIfAbsent(v.getFieldId(), k -> new ArrayList<>())
                    .add(formatValue(field.getType(), v.getValue()));
        }
        final var orderedFieldIds = new ArrayList<>(valuesByField.keySet());
        orderedFieldIds
                .sort((a, b) -> Integer.compare(fieldsById.get(a).getPosition(), fieldsById.get(b).getPosition()));
        final var sb = new StringBuilder();
        for (final var fieldId : orderedFieldIds) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(fieldsById.get(fieldId).getLabel()).append(": ")
                    .append(String.join(", ", valuesByField.get(fieldId)));
        }
        return sb.toString();
    }

    /**
     * Formats a stored metadata value for inclusion in an Opsview note, rendering the composite JSON types
     * ({@code contact}/{@code mail-group}/{@code switchboard}) as human-readable text instead of raw JSON - mirrors the
     * read-only rendering already done client-side in {@code metadata.jsp}'s {@code dmfRenderInput()}. Any other type
     * is used as-is.
     *
     * @param type
     *            the field type
     * @param rawValue
     *            the stored value
     *
     * @return the formatted value
     */
    private static String formatValue(final String type, final String rawValue) {
        if ("contact".equals(type) || "mail-group".equals(type) || "switchboard".equals(type)) {
            try {
                final var node = _mapper.readTree(rawValue);
                final List<String> parts = new ArrayList<>();
                addIfPresent(parts, node, "name");
                addIfPresent(parts, node, "email");
                addIfPresent(parts, node, "phone");
                addIfPresent(parts, node, "fax");
                if (!parts.isEmpty()) {
                    return String.join(", ", parts);
                }
            } catch (final Exception e) {
                // Not valid JSON (or empty) - fall through to the raw value.
            }
        }
        return rawValue;
    }

    /**
     * Appends {@code node.<key>} to {@code parts} if present and non-blank.
     */
    private static void addIfPresent(final List<String> parts, final JsonNode node, final String key) {
        final var value = node.path(key).asText(null);
        if (value != null && !value.isBlank()) {
            parts.add(value);
        }
    }
}
