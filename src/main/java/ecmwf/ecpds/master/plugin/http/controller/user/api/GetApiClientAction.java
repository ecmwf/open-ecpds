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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.ecuser.ApiClientHome;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

public class GetApiClientAction extends PDSAction {

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        if ("checkId".equals(request.getParameter("json"))) {
            final var id = request.getParameter("id");
            boolean exists = false;
            if (id != null && !id.isBlank()) {
                try {
                    exists = ApiClientHome.findByPrimaryKey(id) != null;
                } catch (final Exception ignored) {
                }
            }
            try {
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"exists\":" + exists + "}");
                response.getWriter().flush();
            } catch (final java.io.IOException ignored) {
            }
            return null;
        }
        final List<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (!parameters.isEmpty()) {
            final var client = ApiClientHome.findByPrimaryKey(parameters.get(0).toString());
            request.setAttribute("apiClient", client);
            try {
                request.setAttribute("permissions", MasterManager.getDB().getApiPermissionsForClient(client.getId()));
            } catch (final Exception e) {
            }
            return mapping.findForward("success");
        }
        request.setAttribute("apiClients", List.of());
        return mapping.findForward("success");
    }

    @Override
    public boolean match(final ModelBean b, final String what) {
        return true;
    }
}
