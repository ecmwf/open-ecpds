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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.transfer.DestinationMetaData;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDestinationMetaDataAction.
 */
public class GetDestinationMetaDataAction extends PDSAction {

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
     * @throws ECMWFActionFormException
     *             the ECMWF action form exception
     * @throws TransferException
     *             the transfer exception
     * @throws MonitoringException
     *             the monitoring exception
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException, TransferException, MonitoringException {
        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        if (params.size() == 1) {
            final var id = params.get(0).toString();
            final var destination = DestinationHome.findByPrimaryKey(id);
            request.setAttribute("destination", destination);
            final var metadata = destination.getMetaData();
            request.setAttribute("metaData", metadata);
            request.setAttribute("metaDataSize", metadata.size());
            request.setAttribute("products", ProductStatusHome.findAllProductNameTimePairs());
            request.setAttribute("currentDate", new Date());
            return mapping.findForward("success");
        }
        if (params.size() == 2) {
            final var id = params.get(0).toString();
            final var destination = DestinationHome.findByPrimaryKey(id);
            request.setAttribute("destination", destination);
            final var file = params.get(1).toString();
            final var c = destination.getMetaData();
            for (DestinationMetaData m : c) {
                if (m.getName().equals(file)) {
                    request.setAttribute("content", m);
                }
            }
            return mapping.findForward("file");
        } else {
            throw new ECMWFActionFormException("Unsupported number of parameters: " + params);
        }
    }
}
