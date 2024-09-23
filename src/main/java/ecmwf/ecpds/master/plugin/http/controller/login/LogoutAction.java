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

package ecmwf.ecpds.master.plugin.http.controller.login;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Log out.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFAction;
import ecmwf.web.model.users.User;

/**
 * The Class LogoutAction.
 */
public class LogoutAction extends ECMWFAction {

    /**
     * {@inheritDoc}
     *
     * Safe perform.
     */
    @Override
    public ActionForward safePerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response)
            throws ECMWFException, ClassCastException {
        request.setAttribute("title", System.getProperty("monitor.title"));
        // Wipe the user out from the session
        request.getSession().setAttribute(User.SESSION_KEY, null);
        // Delete the RETURN_AFTER_LOGIN, to avoid returning to a place you
        // can't go (which would re-trigger authentication !!
        request.getSession().setAttribute(PDSAction.RETURN_AFTER_LOGIN_KEY, null);
        request.getSession().setAttribute(Globals.ERROR_KEY, null);
        return mapping.findForward("success");
    }
}
