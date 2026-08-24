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

package ecmwf.ecpds.master.plugin.http.controller.user.api;

import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ecmwf.common.database.ApiClient;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

public class InsertApiClientAction extends PDSAction {

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        if (isCancelled(request)) {
            return mapping.findForward("cancel");
        }
        final var actionForm = (ApiClientActionForm) form;
        final var clientId = actionForm.getId().trim();
        final var comment = actionForm.getComment().trim();
        final var active = Boolean.parseBoolean(actionForm.getActive());

        // Server-side validation
        if (clientId.isEmpty() || !clientId.matches("[\\w\\-]{1,64}")) {
            final var errors = new ActionErrors();
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("errors.detail", clientId.isEmpty() ? "Client ID is required."
                            : "Client ID must be 1–64 characters (letters, digits, hyphens, underscores)."));
            saveErrors(request, errors);
            return mapping.findForward("input");
        }

        final var secret = ApiClient.generateSecret();
        final var secretHash = ApiClient.sha256Hex(secret);

        final var client = new ecmwf.common.database.ApiClient();
        client.setId(clientId);
        client.setSecretHash(secretHash);
        client.setComment(comment);
        client.setActive(active);
        client.setCreated(new Timestamp(System.currentTimeMillis()));
        final var countryIso = actionForm.getCountryIso();
        if (countryIso != null && !countryIso.isBlank()) {
            client.setCountryIso(countryIso.trim());
        }

        try {
            final var db = MasterManager.getDB();
            final var session = Util.getECpdsSessionFromObject(user);
            db.insert(session, client, true);
        } catch (final Exception e) {
            throw new ECMWFException("Error inserting API client: " + e.getMessage(), e);
        }

        // PRG: store secret in session so it survives the redirect, then redirect
        // to the detail page. detail.jsp will read and remove it from session.
        request.getSession().setAttribute("pendingApiSecret", secret);
        request.getSession().setAttribute("pendingApiClientId", clientId);
        try {
            response.sendRedirect(request.getContextPath() + "/do/user/api/" + clientId);
        } catch (final java.io.IOException e) {
            throw new ECMWFException("Error redirecting after API client creation", e);
        }
        return null;
    }

    @Override
    public boolean match(final ModelBean b, final String what) {
        return true;
    }
}
