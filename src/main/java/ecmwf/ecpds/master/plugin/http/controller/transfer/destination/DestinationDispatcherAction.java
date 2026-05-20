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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class DestinationDispatcherAction.
 */
public class DestinationDispatcherAction extends PDSAction {

    private static final Logger log = LogManager.getLogger(DestinationDispatcherAction.class);

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException {

        final String json = request.getParameter("json");

        try {
            if ("list".equalsIgnoreCase(json)) {
                return new GetDestinationListJsonAction().safeAuthorizedPerform(mapping, form, request, response, user);

            } else if ("dataList".equalsIgnoreCase(json)) {
                return new GetDestinationTransferListJsonAction().safeAuthorizedPerform(mapping, form, request,
                        response, user);

            } else if ("validateList".equalsIgnoreCase(json)) {
                return new GetValidateTransferListJsonAction().safeAuthorizedPerform(mapping, form, request, response,
                        user);

            } else if ("idList".equalsIgnoreCase(json)) {
                return new GetDestinationTransferIdListAction().safeAuthorizedPerform(mapping, form, request, response,
                        user);

            } else if ("basketIdList".equalsIgnoreCase(json)) {
                return new GetBasketIdListAction().safeAuthorizedPerform(mapping, form, request, response, user);

            } else if ("syncSelection".equalsIgnoreCase(json)) {
                return new GetDestinationSyncSelectionAction().safeAuthorizedPerform(mapping, form, request, response,
                        user);
            }

            // Default: normal page
            return new GetDestinationAction().safeAuthorizedPerform(mapping, form, request, response, user);

        } catch (Exception e) {
            log.warn("Error in DestinationDispatcherAction", e);
            throw e;
        }
    }
}