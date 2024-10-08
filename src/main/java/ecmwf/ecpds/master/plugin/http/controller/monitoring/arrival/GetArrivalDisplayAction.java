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

package ecmwf.ecpds.master.plugin.http.controller.monitoring.arrival;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.controller.monitoring.Constants;
import ecmwf.ecpds.master.plugin.http.controller.monitoring.DataTransferComparator;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetArrivalDisplayAction.
 */
public class GetArrivalDisplayAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final ArrayList<?> pathParams = ECMWFActionForm.getPathParameters(mapping, request);
        if (pathParams.isEmpty()) {
            throw new ECMWFActionFormException("This action needs at least 1 parameter.");
        }
        final var destination = DestinationHome.findByPrimaryKey(pathParams.get(0).toString());
        if (pathParams.size() > 2) {
            // Getting arrival for a specific product
            final var product = pathParams.get(1).toString();
            final var time = pathParams.get(2).toString();
            var date = request.getParameter("date");
            if (date == null || "".equals(date) || "All".equals(date)) {
                date = getISOFormat().format(new Date());
            }
            request.setAttribute("destination", destination);
            request.setAttribute("product", product);
            request.getSession().setAttribute(Constants.LAST_PRODUCT_SESSION_KEY, product); // Save
                                                                                            // it
                                                                                            // for
                                                                                            // Timeline
            request.setAttribute("time", time);
            request.getSession().setAttribute(Constants.LAST_TIME_SESSION_KEY, time); // Save
                                                                                      // it
                                                                                      // for
                                                                                      // Timeline
            request.setAttribute("products", ProductStatusHome.findAllProductNameTimePairs());
            request.setAttribute("times", Constants.TIMES);
            request.setAttribute("selectedDate", date);
            request.setAttribute("dateOptions", Constants.getDateOptions());
            final Collection<DataTransfer> transfers;
            if ("All".equals(date)) {
                transfers = destination.getDataTransfersByProductAndTime(product, time);
            } else {
                transfers = destination.getDataTransfersByProductAndTimeOnDate(product, time, Constants.getDate(date));
            }
            final List<DataTransfer> datatransfers = new ArrayList<>(transfers);
            Collections.sort(datatransfers, new DataTransferComparator("scheduledDate", true));
            request.setAttribute("datatransfers", datatransfers);
            final var mode = request.getParameter("mode");
            if ("chart".equals(mode)) {
                return mapping.findForward("chart");
            } else if ("image".equals(mode)) {
                return mapping.findForward("image");
            } else {
                return mapping.findForward("table");
            }
        } else {
            // Generic arrival screen for destination. We'll redirect to the
            // first product
            return redirectToFirstProduct(mapping);
        }
    }

    /**
     * Not very happy about this (hardcoded paths et al) TODO: Substitute redirection by a destination arrival|transfer
     * page which lists all the tags, instead of forwarding to the first.
     *
     * @param mapping
     *            the mapping
     *
     * @return the action forward
     *
     * @throws MonitoringException
     *             the monitoring exception
     * @throws ECMWFActionFormException
     *             the ECMWF action form exception
     */
    private static final ActionForward redirectToFirstProduct(final ActionMapping mapping)
            throws MonitoringException, ECMWFActionFormException {
        final var pairs = ProductStatusHome.findAllProductNameTimePairs();
        if (!pairs.isEmpty()) {
            final var p = pairs.iterator().next();
            return new ActionForward("/do/" + mapping.getPath() + "/" + p.getName() + "/" + p.getValue(), true);
        }
        throw new ECMWFActionFormException("No MetaData 'tag' where to redirect");
    }
}
