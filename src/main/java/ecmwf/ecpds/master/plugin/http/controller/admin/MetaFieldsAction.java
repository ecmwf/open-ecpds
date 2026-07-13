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
 */
package ecmwf.ecpds.master.plugin.http.controller.admin;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.DestinationMetaField;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * Admin action for managing destination metadata field definitions.
 *
 * GET /admin/metafields → list all fields (HTML page) POST /admin/metafields/save → save (insert or update) a field
 * (JSON response) POST /admin/metafields/toggle → toggle active flag (JSON response)
 */
public class MetaFieldsAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(MetaFieldsAction.class);
    private static final ObjectMapper _mapper = new ObjectMapper();

    /** Valid field types */
    private static final java.util.Set<String> VALID_TYPES = java.util.Set.of("text", "textarea", "url", "email",
            "phone", "password", "contact", "mail-group", "switchboard");

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {

        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        final var action = params.isEmpty() ? "" : params.get(0).toString();

        if ("save".equals(action)) {
            return handleSave(request, response, user);
        }
        if ("toggle".equals(action)) {
            return handleToggle(request, response);
        }
        if ("delete".equals(action)) {
            return handleDelete(request, response);
        }
        if ("deleteUnassigned".equals(action)) {
            return handleDeleteUnassigned(request, response);
        }

        // Default: list page
        try {
            final var db = MasterManager.getDB();
            final var fields = new ArrayList<>(db.getAllDestinationMetaFields());
            final var typeMap = db.getDestinationMetaFieldTypeMap();
            final var usedIds = db.getUsedMetaFieldIds();
            final var destTypes = ecmwf.ecpds.master.transfer.DestinationOption.getList();
            request.setAttribute("metaFields", fields);
            request.setAttribute("usedFieldIds", usedIds);
            // Serialize to JSON for safe JS embedding
            request.setAttribute("fieldTypeMapJson", _mapper.writeValueAsString(typeMap));
            request.setAttribute("usedFieldIdsJson", _mapper.writeValueAsString(usedIds));
            final var typesForJs = new java.util.ArrayList<java.util.Map<String, Object>>();
            for (final var t : destTypes) {
                final var m = new java.util.LinkedHashMap<String, Object>();
                m.put("id", t.getId());
                m.put("label", t.getLabel());
                typesForJs.add(m);
            }
            request.setAttribute("destTypesJson", _mapper.writeValueAsString(typesForJs));
        } catch (final Exception e) {
            _log.warn("MetaFieldsAction: failed to load fields", e);
            request.setAttribute("metaFields", java.util.Collections.emptyList());
            request.setAttribute("fieldTypeMapJson", "{}");
            request.setAttribute("usedFieldIdsJson", "[]");
            request.setAttribute("destTypesJson", "[]");
            request.setAttribute("loadError", e.getMessage());
        }
        return mapping.findForward("success");
    }

    private ActionForward handleSave(final HttpServletRequest request, final HttpServletResponse response,
            final User user) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var body = _mapper.readValue(request.getInputStream(), Map.class);

            final var field = new DestinationMetaField();

            // ID: 0 = insert, >0 = update
            final var idVal = body.get("DMF_ID");
            if (idVal != null) {
                final int id = Integer.parseInt(String.valueOf(idVal));
                if (id > 0)
                    field.setId(id);
            }

            final var name = trimOrNull(body.get("DMF_NAME"));
            if (name == null || name.isBlank())
                throw new IllegalArgumentException("Field name is required");
            if (name.length() > 64)
                throw new IllegalArgumentException("Field name too long (max 64)");
            if (!name.matches("[a-zA-Z0-9_\\-]+"))
                throw new IllegalArgumentException(
                        "Name must contain only letters, digits, underscores or hyphens (no spaces)");
            field.setName(name);

            final var label = trimOrNull(body.get("DMF_LABEL"));
            if (label == null || label.isBlank())
                throw new IllegalArgumentException("Label is required");
            if (label.length() > 128)
                throw new IllegalArgumentException("Label too long (max 128)");
            field.setLabel(label);

            final var type = trimOrNull(body.get("DMF_TYPE"));
            if (type == null || !VALID_TYPES.contains(type))
                throw new IllegalArgumentException("Invalid type: " + type);
            field.setType(type);

            final var category = trimOrNull(body.get("DMF_CATEGORY"));
            field.setCategory(category != null && !category.isBlank() ? category : "General");

            final var tooltip = trimOrNull(body.get("DMF_TOOLTIP"));
            field.setTooltip(tooltip);

            final var maxOccurs = body.get("DMF_MAX_OCCURS");
            field.setMaxOccurs(maxOccurs != null ? Integer.parseInt(String.valueOf(maxOccurs)) : 1);

            final var position = body.get("DMF_POSITION");
            field.setPosition(position != null ? Integer.parseInt(String.valueOf(position)) : 0);

            final var active = body.get("DMF_ACTIVE");
            field.setActive(active == null || Boolean.parseBoolean(String.valueOf(active))
                    || "1".equals(String.valueOf(active)));

            final var db = MasterManager.getDB();
            db.saveDestinationMetaField(field);

            // For inserts, DMF_ID may not be reflected back through the proxy (RMI by-value).
            // Resolve the actual ID by looking up by name if needed.
            int savedId;
            try {
                savedId = field.getId();
            } catch (final NullPointerException ignored) {
                savedId = 0;
            }
            if (savedId == 0) {
                for (final var f : db.getAllDestinationMetaFields()) {
                    if (field.getName().equals(f.getName())) {
                        savedId = f.getId();
                        break;
                    }
                }
            }

            // Save type restrictions (DES_TYPEs). Empty list = all types.
            @SuppressWarnings("unchecked")
            final var rawTypes = (java.util.List<Object>) body.get("DES_TYPES");
            final var types = new java.util.HashSet<Integer>();
            if (rawTypes != null) {
                for (final var t : rawTypes) {
                    types.add(Integer.parseInt(String.valueOf(t)));
                }
            }
            if (savedId > 0) {
                db.setDestinationMetaFieldTypes(savedId, types);
            }

            response.getWriter().write("{\"success\":true,\"id\":" + savedId + "}");
        } catch (final Exception e) {
            _log.warn("MetaFieldsAction.handleSave", e);
            final var msg = friendlyDbError(e.getMessage());
            writeError(response, msg);
        }
        return null;
    }

    private ActionForward handleToggle(final HttpServletRequest request, final HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var body = _mapper.readValue(request.getInputStream(), Map.class);
            final int id = Integer.parseInt(String.valueOf(body.get("DMF_ID")));
            final boolean active = Boolean.parseBoolean(String.valueOf(body.get("DMF_ACTIVE")));

            // Load existing (including inactive), flip active, save
            final var fields = MasterManager.getDB().getAllDestinationMetaFields();
            DestinationMetaField target = null;
            for (final var f : fields) {
                if (f.getId() == id) {
                    target = f;
                    break;
                }
            }
            if (target == null)
                throw new IllegalArgumentException("Field not found: " + id);
            target.setActive(active);
            MasterManager.getDB().saveDestinationMetaField(target);
            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("MetaFieldsAction.handleToggle", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    private ActionForward handleDelete(final HttpServletRequest request, final HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var body = _mapper.readValue(request.getInputStream(), Map.class);
            final int id = Integer.parseInt(String.valueOf(body.get("DMF_ID")));
            final var db = MasterManager.getDB();
            // Load and delete — CASCADE in DB removes DESTINATION_META_FIELD_TYPE and VALUES rows
            final var fields = db.getAllDestinationMetaFields();
            DestinationMetaField target = null;
            for (final var f : fields) {
                if (f.getId() == id) {
                    target = f;
                    break;
                }
            }
            if (target == null)
                throw new IllegalArgumentException("Field not found: " + id);
            db.deleteDestinationMetaField(target);
            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("MetaFieldsAction.handleDelete", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    private ActionForward handleDeleteUnassigned(final HttpServletRequest request, final HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var db = MasterManager.getDB();
            final var usedIds = db.getUsedMetaFieldIds();
            final var fields = db.getAllDestinationMetaFields();
            int deleted = 0;
            int errors = 0;
            for (final var f : fields) {
                if (!usedIds.contains(f.getId())) {
                    try {
                        db.deleteDestinationMetaField(f);
                        deleted++;
                    } catch (final Exception e) {
                        _log.warn("handleDeleteUnassigned: failed to delete field {}", f.getId(), e);
                        errors++;
                    }
                }
            }
            response.getWriter().write("{\"success\":true,\"deleted\":" + deleted + ",\"errors\":" + errors + "}");
        } catch (final Exception e) {
            _log.warn("MetaFieldsAction.handleDeleteUnassigned", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    private static String trimOrNull(final Object v) {
        return v != null ? String.valueOf(v).trim() : null;
    }

    private static String friendlyDbError(final String msg) {
        if (msg == null)
            return "Unknown error";
        if (msg.contains("Duplicate entry") && msg.contains("DMF_NAME_UQ"))
            return "A field with that name already exists. Please choose a unique name.";
        if (msg.contains("Duplicate entry"))
            return "Duplicate entry — a record with those values already exists.";
        if (msg.contains("cannot be null") || msg.contains("Column") && msg.contains("cannot be null"))
            return "A required field is missing.";
        // Strip raw JDBC noise: keep only up to the first '[' or newline
        final int bracket = msg.indexOf("[(conn=");
        if (bracket > 0)
            return msg.substring(0, bracket).trim();
        return msg;
    }

    private static void writeError(final HttpServletResponse response, final String msg) {
        try {
            final var safe = (msg != null ? msg : "error").replace("\"", "'");
            response.getWriter().write("{\"success\":false,\"error\":\"" + safe + "\"}");
        } catch (final Exception ignored) {
        }
    }
}
