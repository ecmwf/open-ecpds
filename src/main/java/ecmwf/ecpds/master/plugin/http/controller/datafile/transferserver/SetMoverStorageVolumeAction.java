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
 * In applying this license, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization.
 */

package ecmwf.ecpds.master.plugin.http.controller.datafile.transferserver;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/** Admin-only endpoint for disabling or re-enabling a Data Mover storage volume. */
public final class SetMoverStorageVolumeAction extends PDSAction {
    private static final String TOKEN = SetMoverStorageVolumeAction.class.getName() + ".csrf";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected boolean isAuthorized(final User user, final String path, final HttpServletRequest request)
            throws ECMWFException {
        return super.isAuthorized(user, path, request) && user.hasAccess("/do/admin/");
    }

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            final var session = request.getSession();
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                synchronized (session) {
                    if (!(session.getAttribute(TOKEN) instanceof String)) {
                        session.setAttribute(TOKEN, UUID.randomUUID().toString());
                    }
                }
                MAPPER.writeValue(response.getWriter(), Map.of("csrf", session.getAttribute(TOKEN)));
                return null;
            }
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                MAPPER.writeValue(response.getWriter(), Map.of("error", "Method not allowed"));
                return null;
            }
            final var expected = (String) session.getAttribute(TOKEN);
            final var supplied = request.getParameter("csrf");
            if (expected == null || supplied == null || !MessageDigest
                    .isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                MAPPER.writeValue(response.getWriter(), Map.of("error", "Invalid or expired request token"));
                return null;
            }
            final var mover = request.getParameter("mover");
            final var volume = Integer.parseInt(request.getParameter("volume"));
            final var disabled = switch (request.getParameter("disabled")) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Invalid disabled state");
            };
            MasterManager.getMI().setStorageVolumeDisabled(Util.getECpdsSessionFromObject(user), mover, volume,
                    disabled);
            MAPPER.writeValue(response.getWriter(), Map.of("ok", true));
        } catch (final Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                MAPPER.writeValue(response.getWriter(),
                        Map.of("error", e.getMessage() == null ? "Operation failed" : e.getMessage()));
            } catch (final Exception writeFailure) {
                e.addSuppressed(writeFailure);
                throw new ECMWFException("Cannot report storage volume operation failure", e);
            }
        }
        return null;
    }
}
