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

package ecmwf.ecpds.master.plugin.http.controller.datafile.metadata;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.datafile.MetaDataHome;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetMetaDataAction.
 */
public class GetMetaDataAction extends PDSAction {

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
        final ArrayList<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (parameters.isEmpty()) {
            request.setAttribute("attributes", MetaDataHome.findAllMetaDataNames());
            return mapping.findForward("success");
        }
        if (parameters.size() == 2) {
            if ("datafile".equals(parameters.get(0).toString())) {
                request.setAttribute("attributeName", parameters.get(1).toString());
                request.setAttribute("attributes", MetaDataHome.findByDataFileId(parameters.get(1).toString()));
                return mapping.findForward("success");
            } else if ("attribute".equals(parameters.get(0).toString())) {
                request.setAttribute("attributeName", parameters.get(1).toString());
                request.setAttribute("attributes", MetaDataHome.findByAttributeName(parameters.get(1).toString()));
                return mapping.findForward("success");
            } else {
                throw new ECMWFActionFormException("First parameter invalid.");
            }
        } else {
            throw new ECMWFActionFormException("Expecting 0 or 2 parameters.");
        }
    }
}
