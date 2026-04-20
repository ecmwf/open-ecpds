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

package ecmwf.ecpds.master.plugin.http.controller;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class StartAction.
 */
public class StartAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        request.setAttribute("hasDataStorage",
                user.hasAccess(getResource(request, "datafile.basepath"))
                        || user.hasAccess(getResource(request, "metadata.basepath"))
                        || user.hasAccess(getResource(request, "transfergroup.basepath"))
                        || user.hasAccess(getResource(request, "transferserver.basepath"))
                        || user.hasAccess(getResource(request, "retrievalmonitoring.basepath")));

        request.setAttribute("hasTransmission",
                user.hasAccess(getResource(request, "datatransfer.basepath"))
                        || user.hasAccess(getResource(request, "destination.basepath"))
                        || user.hasAccess(getResource(request, "host.basepath"))
                        || user.hasAccess(getResource(request, "transferhistory.basepath"))
                        || user.hasAccess(getResource(request, "method.basepath"))
                        || user.hasAccess(getResource(request, "module.basepath")));

        request.setAttribute("hasAccessControl",
                user.hasAccess(getResource(request, "user.basepath"))
                        || user.hasAccess(getResource(request, "category.basepath"))
                        || user.hasAccess(getResource(request, "resource.basepath"))
                        || user.hasAccess(getResource(request, "event.basepath"))
                        || user.hasAccess(getResource(request, "incoming.basepath"))
                        || user.hasAccess(getResource(request, "policy.basepath"))
                        || user.hasAccess(getResource(request, "history.basepath")));

        request.setAttribute("hasAdmin", user.hasAccess(getResource(request, "admin.basepath")));

        request.setAttribute("title", System.getProperty("monitor.title"));
        return mapping.findForward("success");
    }
}
