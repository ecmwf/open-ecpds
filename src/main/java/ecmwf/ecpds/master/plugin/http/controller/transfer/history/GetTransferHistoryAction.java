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

package ecmwf.ecpds.master.plugin.http.controller.transfer.history;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferHistoryHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferHistory;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class GetTransferHistoryAction.
 */
public class GetTransferHistoryAction extends PDSAction {

    /** The Constant DAYS_BACK. */
    private static final int DAYS_BACK = 7;

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final ArrayList<?> pathParameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (pathParameters.isEmpty()) {
            final var destinationNamesAndComment = DestinationHome.findAllNamesAndComments();
            final var destinationName = Util.getValue(request, "destinationName",
                    () -> getFirstPairName(destinationNamesAndComment));
            if ("".equals(destinationName)) {
                throw new ECMWFActionFormException("No Transfer History available (no Destinations found)");
            }
            final var currentDestination = DestinationHome.findByPrimaryKey(destinationName);
            final var currentDate = getISOFormat().format(new Date());
            var date = Util.getValue(request, "date", currentDate);
            if ("All".equals(date)) {
                date = currentDate;
            }
            final var mode = request.getParameter("mode");
            // Initialize the cursor for the database search
            final var cursor = Util.getDataBaseCursor("history", 25, 1, true, request);
            final Collection<TransferHistory> historyItems;
            try {
                historyItems = "productDate".equals(mode)
                        ? TransferHistoryHome.findByDestinationNameAndProductDate(user, destinationName,
                                getISOFormat().parse(date), cursor)
                        : TransferHistoryHome.findByDestinationNameAndHistoryDate(user, destinationName,
                                getISOFormat().parse(date), cursor);
            } catch (final ParseException e) {
                throw new ECMWFActionFormException("Error parsing date", e);
            }
            // And now save the options and values.
            request.setAttribute("historyItems", historyItems);
            request.setAttribute("historyItemsSize", Util.getCollectionSizeFrom(historyItems));
            request.setAttribute("destination", currentDestination);
            request.setAttribute("destinationOptions", destinationNamesAndComment);
            request.setAttribute("selectedDestination", currentDestination);
            request.setAttribute("selectedDate", date);
            request.setAttribute("dateOptions", getDateOptions(DAYS_BACK, false));
            request.setAttribute("currentDate", new Date());
        } else {
            final var history = TransferHistoryHome.findByPrimaryKey(pathParameters.get(0).toString());
            // To allow the display of the links in the comment!
            history.setUser(user);
            request.setAttribute("item", history);
        }
        return mapping.findForward("success");
    }

    /**
     * Gets the first pair name.
     *
     * @param destinationCollection
     *            the destination collection
     *
     * @return the first pair name
     */
    private static String getFirstPairName(final Collection<Pair> destinationCollection) {
        try {
            return !destinationCollection.isEmpty() ? (String) destinationCollection.iterator().next().getName() : "";
        } catch (final Throwable t) {
            return "";
        }
    }
}
