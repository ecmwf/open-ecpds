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

package ecmwf.ecpds.master.plugin.http.controller.monitoring.timeline;

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
import ecmwf.ecpds.master.plugin.http.controller.monitoring.Constants;
import ecmwf.ecpds.master.plugin.http.controller.monitoring.DataTransferComparator;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * The Class GetTimelineDisplayAction.
 */
public class GetTimelineDisplayAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        var date = request.getParameter("date");
        if (date == null || "".equals(date) || "All".equals(date)) {
            date = getISOFormat().format(new Date());
        }
        final ArrayList<?> params = ECMWFActionForm.getPathParameters(mapping, request);
        final var destination = DestinationHome.findByPrimaryKey(params.get(0).toString());
        final List<DataTransfer> datatransfers = new ArrayList<>(
                destination.getDataTransfersIncludingRetriesOnTransmissionDate(Constants.getDate(date)));
        Collections.sort(datatransfers, new DataTransferComparator("startDate", true));
        request.setAttribute("destination", destination);
        request.setAttribute("datatransfers", datatransfers);
        request.setAttribute("selectedDate", date);
        request.setAttribute("dateOptions", Constants.getDateOptions());
        request.setAttribute("stepWidth", Constants.TIMELINE_STEP_WIDTH);
        final var mode = request.getParameter("mode");
        if ("image".equals(mode)) {
            request.setAttribute("step", Integer.parseInt(request.getParameter("step")));
            return mapping.findForward("image");
        }
        final var productPairs = ProductStatusHome.findAllProductNameTimePairs();
        request.setAttribute("products", productPairs);
        request.setAttribute("steps", datatransfers.size() / Constants.TIMELINE_STEP_WIDTH + 1);
        // In the Timeline we don't have product or time, so take the first
        // one
        final var product = request.getSession().getAttribute(Constants.LAST_PRODUCT_SESSION_KEY);
        final var time = request.getSession().getAttribute(Constants.LAST_TIME_SESSION_KEY);
        request.setAttribute("product", product != null ? product
                : !productPairs.isEmpty() ? productPairs.iterator().next().getName() : "NONE");
        request.setAttribute("time", time != null ? time : Constants.TIMES.iterator().next());
        return mapping.findForward("chart");
    }
}
