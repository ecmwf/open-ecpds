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
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

public class DeleteApiClientAction extends PDSAction {

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final List<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (parameters.isEmpty()) {
            throw new ECMWFException("Missing client ID parameter");
        }
        final var clientId = parameters.get(0).toString();
        try {
            final var db = MasterManager.getDB();
            final var client = db.getApiClient(clientId);
            db.remove(Util.getECpdsSessionFromObject(user), client);
        } catch (final Exception e) {
            throw new ECMWFException("Error deleting API client: " + e.getMessage(), e);
        }
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        try {
            response.sendRedirect(request.getContextPath() + "/do/user/api");
        } catch (final Exception ignored) {
        }
        return null;
    }

    @Override
    public boolean match(final ModelBean b, final String what) {
        return true;
    }
}
