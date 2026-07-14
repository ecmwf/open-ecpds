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

package ecmwf.ecpds.master.plugin.http.controller.user.incoming;

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
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * Creates a new PortalSubscriber directly (bypassing the self-registration email flow). The subscriber is immediately
 * active and a generated password is returned in the JSON response so the administrator can communicate it to the user.
 */
public class InsertPortalSubscriberAction extends PDSAction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var inuId = ECMWFActionForm.getPathParameter(mapping, request, 0);
        final var email = request.getParameter("email");
        final var name = request.getParameter("name");
        final var iso = request.getParameter("iso");

        if (email == null || email.isBlank() || name == null || name.isBlank()) {
            writeJsonResult(response, false, "Email and name are required", null);
            return null;
        }

        try {
            final var session = Util.getECpdsSessionFromObject(user);
            final var password = MasterManager.getMI().insertPortalSubscriber(session, inuId, email.trim(), name.trim(),
                    iso);
            writeJsonResult(response, true, "Subscriber created successfully", password);
        } catch (final Exception e) {
            writeJsonResult(response, false, e.getMessage(), null);
        }
        return null;
    }

    private static void writeJsonResult(final HttpServletResponse response, final boolean ok, final String message,
            final String password) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            final var node = MAPPER.createObjectNode();
            node.put("ok", ok);
            node.put("message", message);
            if (password != null) {
                node.put("password", password);
            }
            MAPPER.writeValue(response.getWriter(), node);
        } catch (final Exception ignored) {
        }
    }
}
