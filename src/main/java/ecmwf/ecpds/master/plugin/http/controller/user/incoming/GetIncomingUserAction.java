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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.CountryHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingPolicyHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingUserHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class GetIncomingUserAction.
 */
public class GetIncomingUserAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(GetIncomingUserAction.class);

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final List<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (!parameters.isEmpty()) {
            request.setAttribute("incoming", IncomingUserHome.findByPrimaryKey(parameters.get(0).toString()));
            return mapping.findForward("success");
        }
        Collection<? extends ModelBean> users = IncomingUserHome.findAll();
        final var destinationNamesAndComment = new ArrayList<Pair>();
        destinationNamesAndComment.add(new Pair("Any Destination", ""));
        destinationNamesAndComment
                .addAll(Util.getDestinationPairList(DestinationHome.findAllNamesAndComments(), List.of()));
        request.setAttribute("destinationOptions", destinationNamesAndComment);
        final var destParam = request.getParameter("destinationNameForSearch");
        final var destinationNameForSearch = (destParam != null && !destParam.isBlank()) ? destParam
                : "Any Destination";
        // Set selectedDestination so destination_select.jsp can display the current choice.
        final var selectedDestination = destinationNamesAndComment.stream()
                .filter(p -> destinationNameForSearch.equals(p.getName())).findFirst()
                .orElse(destinationNamesAndComment.get(0));
        request.setAttribute("selectedDestination", selectedDestination);
        request.setAttribute("destinationNameForSearch", destinationNameForSearch);

        // Policy filter
        final var policyOptions = new ArrayList<String>();
        policyOptions.add("Any Policy");
        try {
            IncomingPolicyHome.findAll().stream().map(IncomingPolicy::getId).sorted().forEach(policyOptions::add);
        } catch (final Exception ignored) {
        }
        request.setAttribute("policyOptions", policyOptions);
        final var policyParam = request.getParameter("policyNameForSearch");
        final var policyNameForSearch = (policyParam != null && !policyParam.isBlank()) ? policyParam : "Any Policy";
        request.setAttribute("policyNameForSearch", policyNameForSearch);
        final var search = request.getParameter("search");
        if (search != null && !"".equals(search.trim())) {
            users = search(users, search);
        }
        request.setAttribute("users", users);
        try {
            request.setAttribute("countryOptions", CountryHome.findAll());
        } catch (final Exception e) {
            log.warn("Could not load country options for QB", e);
        }
        return mapping.findForward("success");
    }

    /**
     * {@inheritDoc}
     *
     * Match.
     */
    @Override
    public boolean match(final ModelBean b, final String what) {
        final var u = (IncomingUser) b;
        return u.getId().toLowerCase().contains(what.toLowerCase());
    }
}
