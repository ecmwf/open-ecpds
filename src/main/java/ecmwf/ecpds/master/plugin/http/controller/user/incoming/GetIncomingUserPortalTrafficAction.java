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

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/** Action for per-user portal traffic page (/do/user/incoming/{id}/portaltraffic). */
public class GetIncomingUserPortalTrafficAction extends PDSAction {
    private static final Logger _log = LogManager.getLogger(GetIncomingUserPortalTrafficAction.class);

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        final var id = params.isEmpty() ? request.getParameter("id") : params.get(0).toString();
        try {
            request.setAttribute("portalTrafficList",
                    id != null ? MasterManager.getDB().getPortalTrafficByUser(id) : Collections.emptyList());
            request.setAttribute("portalTrafficUser", id);
        } catch (final Exception e) {
            _log.error("Failed to load portal traffic data for user {}", id, e);
            request.setAttribute("portalTrafficList", Collections.emptyList());
            request.setAttribute("portalTrafficError", e.getMessage());
        }
        return mapping.findForward("success");
    }
}
