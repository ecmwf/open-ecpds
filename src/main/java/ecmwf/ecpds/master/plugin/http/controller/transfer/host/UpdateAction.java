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

package ecmwf.ecpds.master.plugin.http.controller.transfer.host;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Update a Host.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.ecuser.EcUserHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.ecuser.EcUserException;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.ModelException;
import ecmwf.web.model.users.User;

/**
 * The Class UpdateAction.
 */
public class UpdateAction extends PDSAction {

    /** The Constant ADD_USER. */
    private static final String ADD_USER = "addEcUser";

    /** The Constant DELETE_USER. */
    private static final String DELETE_USER = "deleteEcUser";

    /**
     * Safe authorized perform.
     *
     * @param mapping
     *            the mapping
     * @param form
     *            the form
     * @param request
     *            the request
     * @param response
     *            the response
     * @param user
     *            the user
     *
     * @return the action forward
     *
     * @throws ECMWFException
     *             the ECMWF exception
     * @throws ClassCastException
     *             the class cast exception
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        if (this.isCancelled(request)) {
            return mapping.findForward("cancel");
        }
        final Collection<?> c = ECMWFActionForm.getPathParameters(mapping, request);
        final Iterator<?> i = c.iterator();
        final var haf = (HostActionForm) form;
        if (c.size() == 1) {
            // Update the host itself
            final var hostName = i.next().toString(); // Always take the
            // validated host, not
            // the one from the
            // form.
            final var h = HostHome.findByPrimaryKey(hostName);
            haf.populateHost(h);
            h.save(user);
            return mapping.findForward("success");
        } else if (c.size() == 3) {
            // Status/association changes for some individual transfers or
            // hosts
            final var h = HostHome.findByPrimaryKey(i.next().toString());
            final var subAction = i.next().toString();
            final var subActionParameter = i.next().toString();
            return executeSubAction(mapping, h, subAction, subActionParameter, user);
        } else {
            throw new ECMWFActionFormException("Unsupported number of parameters " + c);
        }
    }

    /**
     * Execute sub action.
     *
     * @param mapping
     *            the mapping
     * @param h
     *            the h
     * @param subAction
     *            the sub action
     * @param subActionParameter
     *            the sub action parameter
     * @param user
     *            the user
     *
     * @return the action forward
     *
     * @throws EcUserException
     *             the ec user exception
     * @throws ModelException
     *             the model exception
     * @throws ECMWFActionFormException
     *             the ECMWF action form exception
     */
    private static final ActionForward executeSubAction(final ActionMapping mapping, final Host h,
            final String subAction, final String subActionParameter, final User user)
            throws EcUserException, ModelException, ECMWFActionFormException {
        if (ADD_USER.equals(subAction)) {
            h.addAllowedUser(EcUserHome.findByPrimaryKey(subActionParameter));
        } else if (DELETE_USER.equals(subAction)) {
            h.deleteAllowedUser(EcUserHome.findByPrimaryKey(subActionParameter));
        } else {
            throw new ECMWFActionFormException("Invalid subaction '" + subAction + "'");
        }
        h.save(user);
        return mapping.findForward("edit");
    }
}
