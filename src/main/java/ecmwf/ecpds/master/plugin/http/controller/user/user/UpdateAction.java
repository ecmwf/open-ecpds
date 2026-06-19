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
import java.util.Set;

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
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.User;

/**
 * The Class UpdateAction.
 */
public class UpdateAction extends PDSAction {

    /** The Constant ADD_CATEGORY. */
    private static final String ADD_CATEGORY = "addCategory";

    /** The Constant ADD_CATEGORIES. */
    private static final String ADD_CATEGORIES = "addCategories";

    /** The Constant DELETE_CATEGORY. */
    private static final String DELETE_CATEGORY = "deleteCategory";

    /** The Constant DELETE_ALL_CATEGORIES. */
    private static final String DELETE_ALL_CATEGORIES = "deleteAllCategories";

    /** The Constant SET_ADMIN_CATEGORIES. */
    private static final String SET_ADMIN_CATEGORIES = "setAdminCategories";

    /** The Constant SET_MONITOR_CATEGORIES. */
    private static final String SET_MONITOR_CATEGORIES = "setMonitorCategories";

    /** Web categories assigned to the Admin preset. */
    private static final Set<String> ADMIN_CATEGORIES = Set.of("operator", "administrator", "mstate", "monitoring",
            "admin", "datafile", "transfer info", "transfer admin", "transfers", "operations");

    /** Web categories assigned to the Monitor preset. */
    private static final Set<String> MONITOR_CATEGORIES = Set.of("mstate", "monitoring", "transfers");

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
        } else if (ADD_CATEGORIES.equals(subAction)) {
            for (final var id : subActionParameter.split(",")) {
                final var trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    webUser.addCategory(CategoryHome.findByPrimaryKey(trimmed));
                }
            }
            webUser.save(u);
            return mapping.findForward("edit");
        } else if (DELETE_CATEGORY.equals(subAction)) {
            webUser.deleteCategory(CategoryHome.findByPrimaryKey(subActionParameter));
            webUser.save(u);
            return mapping.findForward("edit");
        } else if (DELETE_ALL_CATEGORIES.equals(subAction)) {
            for (final var cat : new java.util.ArrayList<>(webUser.getCategories())) {
                webUser.deleteCategory((Category) cat);
            }
            webUser.save(u);
            return mapping.findForward("edit");
        } else if (SET_ADMIN_CATEGORIES.equals(subAction) || SET_MONITOR_CATEGORIES.equals(subAction)) {
            final var targetNames = SET_ADMIN_CATEGORIES.equals(subAction) ? ADMIN_CATEGORIES : MONITOR_CATEGORIES;
            final java.util.Set<String> currentNames = new java.util.HashSet<>();
            for (final var cat : webUser.getCategories()) {
                currentNames.add(((Category) cat).getName());
            }
            // Remove categories not in the target preset
            for (final var cat : new java.util.ArrayList<>(webUser.getCategories())) {
                if (!targetNames.contains(((Category) cat).getName())) {
                    webUser.deleteCategory((Category) cat);
                }
            }
            // Add missing categories from the preset
            for (final var cat : CategoryHome.findAll()) {
                if (targetNames.contains(((Category) cat).getName())
                        && !currentNames.contains(((Category) cat).getName())) {
                    webUser.addCategory((Category) cat);
                }
            }
            webUser.save(u);
            return mapping.findForward("edit");
        } else {
            throw new ECMWFException(
                    "The subAction '" + subAction + "' is not defined for class " + UpdateAction.class.getName());
        }
    }
}
