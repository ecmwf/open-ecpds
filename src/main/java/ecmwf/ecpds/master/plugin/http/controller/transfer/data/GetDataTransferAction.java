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

package ecmwf.ecpds.master.plugin.http.controller.transfer.data;

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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.dao.transfer.StatusBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferHistoryHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class GetDataTransferAction.
 */
public class GetDataTransferAction extends PDSAction {

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
        final ArrayList<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        try {
            if (parameters.isEmpty()) {
                // There are no DataTransfer specified so we are requested to list the
                // DataTransfers!
                final var date = Util.getValue(request, "date", getISOFormat().format(new Date()));
                final var status = Util.getValue(request, "transferStatus", Status.EXEC);
                final var search = Util.getValue(request, "transferSearch", "");
                final var type = Util.getValue(request, "transferType", "");
                // Initialize the cursor for the database search
                final var cursor = Util.getDataBaseCursor("transfer", 25, 2, true, request);
                // Now get the transfers
                Collection<DataTransfer> transfers;
                try {
                    transfers = DataTransferHome.findByStatusIdAndDate(status, getISOFormat().parse(date), search,
                            getDestinationTypeIds(type), cursor);
                    request.setAttribute("getTransfersError", "");
                } catch (final TransferException e) {
                    request.setAttribute("getTransfersError", e.getMessage());
                    transfers = new ArrayList<>(0);
                }
                try {
                    request.setAttribute("transferTypeOptions", getTypeOptions());
                } catch (final Exception e) {
                    throw new ECMWFActionFormException("Bad date", e);
                }
                request.setAttribute("selectedDate", date);
                request.setAttribute("dateOptions", getDateOptions(DAYS_BACK, false));
                request.setAttribute("transferList", transfers);
                request.setAttribute("transferListSize", Util.getCollectionSizeFrom(transfers));
                request.setAttribute("transferStatusOptions", getStatusOptions());
                request.setAttribute("currentTransferStatus", new StatusBean(status));
                request.setAttribute("hasFileNameSearch", search != null && !search.isBlank());
            } else {
                final var transfer = DataTransferHome.findByPrimaryKey(parameters.get(0).toString());
                // To allow setting the links in the comments!
                transfer.setUser(user);
                final var dataTransfersBasePath = getResource(request, "datatransfer.basepath");
                final var canSeeTransferList = user.hasAccess(dataTransfersBasePath);
                // Only "privileged users" can ScheduleNow and only before the
                // scheduled time
                final var now = new Date();
                final var statusCode = transfer.getStatusCode();
                if ((Status.WAIT.equals(statusCode) || Status.RETR.equals(statusCode)) && canSeeTransferList
                        && (now.before(transfer.getScheduledTime()) || now.before(transfer.getQueueTime()))) {
                    request.setAttribute("showScheduleNow", "YES");
                }
                // Either "priveleged users" or everybody after scheduled time
                // can see size
                if (canSeeTransferList || now.after(transfer.getScheduledTime())) {
                    request.setAttribute("showFileSize", "YES");
                }
                request.setAttribute("datatransfer", transfer);
                request.setAttribute("datafile", transfer.getDataFile());
                // Initialize the cursor for the database search
                final var cursor = Util.getDataBaseCursor("history", 15, 1, true, request);
                // Can the user see the transfer history details?
                final var transferHistoryBasePath = getResource(request, "transferhistory.basepath");
                final var canSeeHistoryDetail = user.hasAccess(transferHistoryBasePath);
                final var historyItems = TransferHistoryHome.findByDataTransfer(transfer, !canSeeHistoryDetail, cursor);
                request.setAttribute("historyItems", historyItems);
                request.setAttribute("historyItemsSize", Util.getCollectionSizeFrom(historyItems));
            }
        } catch (final ParseException e) {
            throw new ECMWFActionFormException("Bad date", e);
        }
        return mapping.findForward("success");
    }

    /**
     * Gets the destination type ids.
     *
     * @param category
     *            the category
     *
     * @return the destination type ids
     */
    private static final String getDestinationTypeIds(final String category) {
        return DestinationOption.getTypeIds(category);
    }

    /**
     * Gets the status options.
     *
     * @return the status options
     */
    private static final Collection<Status> getStatusOptions() {
        final List<Status> result = new ArrayList<>();
        for (final String code : StatusFactory.getDataTransferStatusCodes()) {
            result.add(new StatusBean(code));
        }
        return result;
    }

    /**
     * Gets the type options.
     *
     * @return the type options
     */
    private static final Collection<Pair> getTypeOptions() {
        final Collection<Pair> options = new ArrayList<>();
        for (final Map.Entry<Integer, String> entry : DestinationOption.getTypes(true)) {
            options.add(new Pair(String.valueOf(entry.getKey()), entry.getValue()));
        }
        return options;
    }
}
