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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.ecuser.ApiClientHome;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

/**
 * Loads an existing API client into the action form for editing.
 */
public class GetApiClientUpdateFormAction extends PDSAction {

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var clientId = ECMWFActionForm.getPathParameter(mapping, request, 0);
        try {
            final var client = ApiClientHome.findByPrimaryKey(clientId);
            final var actionForm = (ApiClientActionForm) form;
            actionForm.setId(client.getId());
            actionForm.setComment(client.getComment() != null ? client.getComment() : "");
            actionForm.setActive(String.valueOf(client.getActive()));
            actionForm.setCountryIso(client.getCountryIso() != null ? client.getCountryIso() : "");
            request.setAttribute("updateMode", Boolean.TRUE);
        } catch (final Exception e) {
            throw new ECMWFException("Error loading API client '" + clientId + "': " + e.getMessage(), e);
        }
        return mapping.findForward("success");
    }

    @Override
    public boolean match(final ModelBean b, final String what) {
        return true;
    }
}
