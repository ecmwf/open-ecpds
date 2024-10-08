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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStepStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetSummaryDisplayAction.
 */
public class GetSummaryDisplayAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var c = DestinationHome.findAll();
        final List<Destination> destinations = new ArrayList<>(c);
        Collections.sort(destinations, new DestinationComparator("name", true));
        request.setAttribute("destinations", destinations);
        if (!destinations.isEmpty()) {
            request.setAttribute("firstDestination", destinations.iterator().next());
        }
        final ArrayList<?> params = ECMWFActionForm.getPathParameters(mapping, request);
        final var ses = (MonitoringSessionActionForm) form;
        if (params.isEmpty()) {
            throw new ECMWFActionFormException("Unsupported Feature. Please contact the development team.");
        }
        if (params.size() == 2) {
            // Summary by product (tag). Take as tag name whatever they pass us.
            // If it is invalid then the page will be empty :-(. It is too
            // expensive to validate.
            final var product = params.get(0).toString();
            final var time = params.get(1).toString();
            final var stepStatii = ProductStepStatusHome.findAll(product, time);
            putDataForHeader(request, ses, product, time, stepStatii.size(), false);
            request.setAttribute("productStepStatii", stepStatii);
        } else if (params.size() == 4) {
            final var product = params.get(0).toString();
            final var time = params.get(1).toString();
            var step = 0L;
            try {
                step = Long.parseLong(params.get(2).toString());
            } catch (final NumberFormatException e) {
                throw new ECMWFActionFormException("'step' has to be a number", e);
            }
            final var type = params.get(3).toString();
            final var history = ProductStepStatusHome.findHistory(product, time, step, type, -1);
            putDataForHeader(request, ses, product, time, history.size(), true);
            request.setAttribute("productStepStatii", history);
            request.setAttribute("step", step);
            request.setAttribute("type", type);
        } else {
            throw new ECMWFActionFormException("Expected 0 or 2 parameters.");
        }
        request.setAttribute("updated", new Date());
        return mapping.findForward("success");
    }

    /**
     * Put data for header.
     *
     * @param request
     *            the request
     * @param ses
     *            the ses
     * @param product
     *            the product
     * @param time
     *            the time
     * @param stepStatiiSize
     *            the step statii size
     * @param onecolumn
     *            the onecolumn
     *
     * @throws MonitoringException
     *             the monitoring exception
     * @throws TransferException
     *             the transfer exception
     */
    private static final void putDataForHeader(final HttpServletRequest request, final MonitoringSessionActionForm ses,
            final String product, final String time, final int stepStatiiSize, final boolean onecolumn)
            throws MonitoringException, TransferException {
        final List<ProductStatus> products = new ArrayList<>(ProductStatusHome.findFromMemory().values());
        Collections.sort(products, new ProductStatusComparator());
        request.setAttribute("productStatus", ProductStatusHome.findByProduct(product, time));
        request.setAttribute("productName", product);
        request.setAttribute("productNameAndTime", time + "-" + product);
        request.setAttribute("products", products);
        request.setAttribute("reqData", new MonitoringRequest(request, ses));
        if (onecolumn) {
            request.setAttribute("stepsPerColumn", stepStatiiSize);
        } else {
            request.setAttribute("stepsPerColumn", stepStatiiSize / 2 + 1);
        }
        request.setAttribute("nearestToScheduleIndex", MonitoringRequest.getNearestToScheduleIndex(products));
    }
}
