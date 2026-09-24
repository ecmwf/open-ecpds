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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.SystemMessage;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Admin action for managing system-wide, time-bounded warning/maintenance messages, stored in the SYSTEM_MESSAGE
 * database table. Each message has a start and end time; it is automatically shown as a banner on the Monitor UI
 * landing page ({@code /do/start}) and on the Data Portal for as long as the current time falls within that window, and
 * automatically stops being shown once it expires &mdash; no manual cleanup is required for display purposes, though
 * expired entries remain listed here (for history/audit) until explicitly deleted.
 *
 * GET /admin/systemmessages &rarr; list all messages (HTML page) POST /admin/systemmessages/save &rarr; add or update a
 * message (JSON response) POST /admin/systemmessages/delete/{id} &rarr; remove a message (JSON response)
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2026-09-24
 */
public class SystemMessagesAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SystemMessagesAction.class);

    /** The Constant _mapper. */
    private static final ObjectMapper _mapper = new ObjectMapper();

    /** Maximum accepted length for a message text. */
    private static final int MAX_MESSAGE_LENGTH = 2000;

    /** Accepted severity levels. */
    private static final java.util.Set<String> VALID_LEVELS = java.util.Set.of(SystemMessage.LEVEL_INFO,
            SystemMessage.LEVEL_WARNING, SystemMessage.LEVEL_DANGER);

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        final var action = params.isEmpty() ? "" : params.get(0).toString();

        if ("save".equals(action)) {
            return handleSave(request, response, user);
        }
        if ("delete".equals(action)) {
            return handleDelete(request, response);
        }

        // Default: list page
        try {
            final var db = MasterManager.getDB();
            final var messages = new ArrayList<>(db.getSystemMessages());
            messages.sort(Comparator.comparingLong(SystemMessage::getCreatedAt).reversed());
            final var now = System.currentTimeMillis();
            request.setAttribute("systemMessages", messages);
            request.setAttribute("systemMessagesNow", now);
        } catch (final Exception e) {
            _log.warn("SystemMessagesAction: failed to load system messages", e);
            request.setAttribute("systemMessages", java.util.Collections.emptyList());
            request.setAttribute("smError", "Unable to load system messages: " + e.getMessage());
        }

        return mapping.findForward("success");
    }

    /**
     * Handle save (add or update).
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param user
     *            the authenticated admin user
     *
     * @return the action forward
     */
    private ActionForward handleSave(final HttpServletRequest request, final HttpServletResponse response,
            final User user) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var body = _mapper.readValue(request.getInputStream(), Map.class);

            final var message = body.get("message") != null ? String.valueOf(body.get("message")).trim() : "";
            if (message.isBlank())
                throw new IllegalArgumentException("Message text is required");
            if (message.length() > MAX_MESSAGE_LENGTH)
                throw new IllegalArgumentException("Message text too long (max " + MAX_MESSAGE_LENGTH + ")");

            var level = body.get("level") != null ? String.valueOf(body.get("level")).trim()
                    : SystemMessage.LEVEL_WARNING;
            if (!VALID_LEVELS.contains(level))
                throw new IllegalArgumentException("Invalid level: " + level);

            final var startTime = toLong(body.get("startTime"), "Start time");
            final var endTime = toLong(body.get("endTime"), "End time");
            if (endTime <= startTime)
                throw new IllegalArgumentException("End time must be after start time");

            final var idRaw = body.get("id");
            final var id = idRaw != null ? Long.parseLong(String.valueOf(idRaw)) : 0L;

            final var db = MasterManager.getDB();
            final var entity = new SystemMessage();
            // Only set the id when editing an existing message; leave it unset (null) for new
            // messages so Hibernate's "increment" generator correctly treats it as transient.
            if (id > 0) {
                entity.setId(id);
            }
            entity.setMessage(message);
            entity.setLevel(level);
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setCreatedBy(user.getName());
            entity.setCreatedAt(id > 0 ? findCreatedAt(db, id) : System.currentTimeMillis());
            db.saveSystemMessage(entity);

            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("SystemMessagesAction.handleSave", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    /**
     * Handle delete.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the action forward
     */
    private ActionForward handleDelete(final HttpServletRequest request, final HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var body = _mapper.readValue(request.getInputStream(), Map.class);
            final var idRaw = body.get("id");
            if (idRaw == null)
                throw new IllegalArgumentException("Message id is required");
            final var id = Long.parseLong(String.valueOf(idRaw));
            MasterManager.getDB().deleteSystemMessage(id);
            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("SystemMessagesAction.handleDelete", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    /**
     * Preserves the original creation timestamp when editing an existing message.
     *
     * @param db
     *            the database
     * @param id
     *            the message id
     *
     * @return the original creation time, or now if not found
     */
    private static long findCreatedAt(final ecmwf.ecpds.master.DataBaseInterface db, final long id) {
        try {
            for (final var m : db.getSystemMessages()) {
                if (m.getId() == id) {
                    return m.getCreatedAt();
                }
            }
        } catch (final Exception ignored) {
        }
        return System.currentTimeMillis();
    }

    /**
     * Parses a required epoch-millis long field from the JSON body.
     *
     * @param v
     *            the raw value
     * @param label
     *            the field label, for error messages
     *
     * @return the parsed value
     */
    private static long toLong(final Object v, final String label) {
        if (v == null)
            throw new IllegalArgumentException(label + " is required");
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(label + " is invalid");
        }
    }

    /**
     * Write error.
     *
     * @param response
     *            the response
     * @param msg
     *            the msg
     */
    private static void writeError(final HttpServletResponse response, final String msg) {
        try {
            final var safe = (msg != null ? msg : "error").replace("\"", "'");
            response.getWriter().write("{\"success\":false,\"error\":\"" + safe + "\"}");
        } catch (final Exception ignored) {
        }
    }
}
