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

package ecmwf.ecpds.master.plugin.http.controller.user.user;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
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
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.home.users.CategoryHome;
import ecmwf.web.home.users.UserHome;
import ecmwf.web.model.users.User;

/**
 * The Class UpdateAction.
 */
public class UpdateAction extends PDSAction {

    /** The Constant ADD_CATEGORY. */
    private static final String ADD_CATEGORY = "addCategory";

    /** The Constant DELETE_CATEGORY. */
    private static final String DELETE_CATEGORY = "deleteCategory";

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        if (this.isCancelled(request)) {
            return mapping.findForward("cancel");
        }
        final Collection<?> c = ECMWFActionForm.getPathParameters(mapping, request);
        final var daf = (UserActionForm) form;
        if (c.size() == 1) {
            final var d = UserHome.findByPrimaryKey(c.iterator().next().toString());
            daf.populateUser(d);
            d.save(user);
            return mapping.findForward("success");
        } else if (c.size() == 3) {
            final Iterator<?> i = c.iterator();
            final var d = UserHome.findByPrimaryKey(i.next().toString());
            final var subAction = i.next().toString();
            final var subActionParameter = i.next().toString();
            return executeSubAction(mapping, d, subAction, subActionParameter, user);
        } else {
            // There must be a problem with struts-conf.xml. We never should
            // have other than 1 or 3 parameters to match this Action.
            throw new ECMWFActionFormException("Expected 1 or 3 path parameters. Got " + c.size());
        }
    }

    /**
     * Execute sub action.
     *
     * @param mapping
     *            the mapping
     * @param d
     *            the d
     * @param subAction
     *            the sub action
     * @param subActionParameter
     *            the sub action parameter
     * @param u
     *            the u
     *
     * @return the action forward
     *
     * @throws ECMWFException
     *             the ECMWF exception
     */
    private static final ActionForward executeSubAction(final ActionMapping mapping, final User d,
            final String subAction, final String subActionParameter, final User u) throws ECMWFException {
        if (!(d instanceof final WebUser webUser)) {
            throw new ECMWFActionFormException(
                    "An User implementation '" + d.getClass().getName() + "' can NOT be edited");
        }
        if (ADD_CATEGORY.equals(subAction)) {
            webUser.addCategory(CategoryHome.findByPrimaryKey(subActionParameter));
            webUser.save(u);
            return mapping.findForward("edit");
        } else if (DELETE_CATEGORY.equals(subAction)) {
            webUser.deleteCategory(CategoryHome.findByPrimaryKey(subActionParameter));
            webUser.save(u);
            return mapping.findForward("edit");
        } else {
            throw new ECMWFException(
                    "The subAction '" + subAction + "' is not defined for class " + UpdateAction.class.getName());
        }
    }
}
