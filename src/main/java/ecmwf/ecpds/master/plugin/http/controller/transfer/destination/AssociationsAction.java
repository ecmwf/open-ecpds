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

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Change associations between the destination and another objects which have a
 * N-M relationship with it (ie, no foreign key).
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
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class AssociationsAction.
 */
public class AssociationsAction extends PDSAction {

    /** The Constant ADD_POLICY. */
    private static final String ADD_POLICY = "addPolicy";

    /** The Constant DELETE_POLICY. */
    private static final String DELETE_POLICY = "deletePolicy";

    /** The Constant ADD_HOST. */
    private static final String ADD_HOST = "addHost";

    /** The Constant DELETE_HOST. */
    private static final String DELETE_HOST = "deleteHost";

    /** The Constant ADD_ALIAS. */
    private static final String ADD_ALIAS = "addAlias";

    /** The Constant DELETE_ALIAS. */
    private static final String DELETE_ALIAS = "deleteAlias";

    /** The Constant INCREASE_HOST_PRIORITY. */
    private static final String INCREASE_HOST_PRIORITY = "increaseHostPriority";

    /** The Constant DECREASE_HOST_PRIORITY. */
    private static final String DECREASE_HOST_PRIORITY = "decreaseHostPriority";

    /** The Constant ADD_USER. */
    private static final String ADD_USER = "addEcUser";

    /** The Constant DELETE_USER. */
    private static final String DELETE_USER = "deleteEcUser";

    /** The Constant DELETE_METADATAFILE. */
    private static final String DELETE_METADATAFILE = "deleteMetadataFile";

    /** The Constant HOST_PRIORITY_STEP. */
    private static final int HOST_PRIORITY_STEP = 1;

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
        if (c.size() == 3) {
            // Status/association changes for some individual transfers or
            // hosts
            final var daf = (DetailActionForm) form;
            final Iterator<?> i = c.iterator();
            final var destination = DestinationHome.findByPrimaryKey(i.next().toString());
            final var subAction = i.next().toString();
            final var subActionParameter = i.next().toString();
            request.setAttribute("destination", destination);
            return addDisplayTagParams(
                    executeSubAction(request, mapping, destination, subAction, subActionParameter, user),
                    daf.getDisplayTagsParamCollection());
        } else {
            // There must be a problem with struts-conf.xml. We never should
            // have other than 1, 2 or 3 parameters to match this Action.
            throw new ECMWFActionFormException("Expected 3 path parameters. Got " + c.size());
        }
    }

    /**
     * Execute an action on some specific destination.
     *
     * @param request
     *            the request
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
     * @return Where to forward after this.
     *
     * @throws ECMWFException
     *             the ECMWF exception
     */
    private ActionForward executeSubAction(final HttpServletRequest request, final ActionMapping mapping,
            final Destination d, final String subAction, final String subActionParameter, final User u)
            throws ECMWFException {
        if (ADD_HOST.equals(subAction)) {
            d.addHost(HostHome.findByPrimaryKey(subActionParameter));
            d.save(u);
            return mapping.findForward("edit");
        }
        if (DELETE_HOST.equals(subAction)) {
            d.deleteHost(HostHome.findByPrimaryKey(subActionParameter));
            d.save(u);
            return mapping.findForward("edit");
        } else if (ADD_POLICY.equals(subAction)) {
            d.addIncomingPolicy(IncomingPolicyHome.findByPrimaryKey(subActionParameter));
            d.save(u);
            return mapping.findForward("edit");
        } else if (DELETE_POLICY.equals(subAction)) {
            d.deleteIncomingPolicy(IncomingPolicyHome.findByPrimaryKey(subActionParameter));
            d.save(u);
            return mapping.findForward("edit");
        } else if (INCREASE_HOST_PRIORITY.equals(subAction)) {
            final var h = HostHome.findByPrimaryKey(subActionParameter);
            h.increasePriorityWithinDestination(d, HOST_PRIORITY_STEP);
            h.save(u);
            return mapping.findForward("edit");
        } else if (DECREASE_HOST_PRIORITY.equals(subAction)) {
            final var h = HostHome.findByPrimaryKey(subActionParameter);
            h.decreasePriorityWithinDestination(d, HOST_PRIORITY_STEP);
            h.save(u);
            return mapping.findForward("edit");

        } else if (ADD_USER.equals(subAction)) {
            d.addAssociatedEcUser(EcUserHome.findByPrimaryKey(subActionParameter));
            d.save(u);
            return mapping.findForward("edit");
        } else if (DELETE_USER.equals(subAction)) {
            d.deleteAssociatedEcUser(EcUserHome.findByPrimaryKey(subActionParameter));
            d.save(u);
            return mapping.findForward("edit");
        } else if (ADD_ALIAS.equals(subAction)) {
            d.addAlias(DestinationHome.findByPrimaryKey(subActionParameter));
            d.save(u);
            return mapping.findForward("edit");
        } else if (DELETE_ALIAS.equals(subAction)) {
            d.deleteAlias(DestinationHome.findByPrimaryKey(subActionParameter));
            d.save(u);
            return mapping.findForward("edit");
        } else if (DELETE_METADATAFILE.equals(subAction)) {
            d.deleteMetadataFile(subActionParameter);
            d.save(u);
            return mapping.findForward("edit");
        } else {
            throw new ECMWFException(
                    "The subAction '" + subAction + "' is not defined for class " + this.getClass().getName());
        }

    }

    /**
     * Adds the display tag params.
     *
     * @param fw
     *            the fw
     * @param params
     *            the params
     *
     * @return the action forward
     */
    private static final ActionForward addDisplayTagParams(final ActionForward fw, final Collection<Pair> params) {
        if (params == null || fw.getPath().startsWith(".pds.")) {
            return fw;
        }
        final var out = new StringBuilder();
        out.append(fw.getPath()).append("?");
        for (Pair p : params) {
            out.append(p.getName() + "=" + p.getValue() + "&");
        }
        return new ActionForward(out.toString(), fw.getRedirect());
    }
}
