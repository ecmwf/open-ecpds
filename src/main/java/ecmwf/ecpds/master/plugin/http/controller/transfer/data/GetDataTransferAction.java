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
 * ECMWF Product Data Store (ECPDS) Project
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.dao.transfer.StatusBean;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferServerHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.TransferMethodHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
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
/**
 * The Class GetDataTransferAction.
 */
public class GetDataTransferAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(GetDataTransferAction.class);

    /** The Constant DAYS_BACK. */
    private static final int DAYS_BACK = 7;

    @Override
    public ActionForward safeAuthorizedPerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, User user) throws ECMWFException {
        String json = request.getParameter("json");
        try {
            if ("list".equalsIgnoreCase(json)) {
                new GetDataTransferListJsonAction().safeAuthorizedPerform(mapping, form, request, response, user);
                return null;
            }

            return authorizedPerform(mapping, form, request, response, user);

        } catch (Exception e) {
            throw new ECMWFActionFormException(e.getMessage(), e);
        }
    }

    /**
     * Safe authorized perform.
     */
    public ActionForward authorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final ArrayList<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (parameters.isEmpty()) {
            // There are no DataTransfer specified so we are requested to list the
            // DataTransfers! Just set up the filter options — the actual data is
            // fetched via DataTables AJAX (GetDataTransferListJsonAction).
            var date = Util.getValue(request, "date", getISOFormat().format(new Date()));
            // Guard against a stale non-ISO value (e.g. "All") left in the session by
            // another page. Reset to today and fix the session so the error doesn't recur.
            try {
                getISOFormat().parse(date);
            } catch (final ParseException ignored) {
                date = getISOFormat().format(new Date());
                request.getSession().setAttribute("date", date);
            }
            final var status = Util.getValue(request, "transferStatus", Status.EXEC);
            final var search = Util.getValue(request, "transferSearch", "");
            Util.getValue(request, "transferType", ""); // read/persist in session for form state
            try {
                request.setAttribute("transferTypeOptions", getTypeOptions());
            } catch (final Exception e) {
                throw new ECMWFActionFormException("Bad date", e);
            }
            request.setAttribute("selectedDate", date);
            request.setAttribute("dateOptions", getDateOptions(DAYS_BACK, false));
            request.setAttribute("transferStatusOptions", getStatusOptions());
            request.setAttribute("currentTransferStatus", new StatusBean(status));
            request.setAttribute("hasFileNameSearch", search != null && !search.isBlank());
            try {
                request.setAttribute("transferServerOptions", TransferServerHome.findAll());
            } catch (final Exception e) {
                log.warn("Could not load transfer servers for autocomplete", e);
            }
            try {
                request.setAttribute("transferMethodOptions", TransferMethodHome.findAll());
            } catch (final Exception e) {
                log.warn("Could not load transfer methods for autocomplete", e);
            }
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
            try {
                request.setAttribute("destination", DestinationHome.findByPrimaryKey(transfer.getDestinationName()));
            } catch (final Exception e) {
                log.warn("Could not load destination '{}' for transfer {}", transfer.getDestinationName(),
                        transfer.getId(), e);
            }
            try {
                final var stats = MasterManager.getDB()
                        .getTransferStatisticsByDataTransferId(Long.parseLong(transfer.getId()));
                request.setAttribute("transferStatistics", stats);
                request.setAttribute("transferStatisticsGroups", groupStatsByAttempt(stats));
            } catch (final Exception e) {
                log.warn("Could not load transfer statistics for {}", transfer.getId(), e);
            }
        }
        return mapping.findForward("success");
    }

    /**
     * Groups a list of TransferStatistics by their recorded requeueHistory value. Each distinct value represents one
     * sending attempt, so grouping is exact with no time-gap heuristics. Results are ordered by ascending
     * requeueHistory (i.e. attempt 1 first). Records pre-dating this field (requeueHistory == 0) all fall into the
     * first group, which is the correct backward-compatible behaviour for transfers that were never requeued.
     */
    private static List<List<ecmwf.common.database.TransferStatistics>> groupStatsByAttempt(
            final List<ecmwf.common.database.TransferStatistics> stats) {
        final var groups = new java.util.LinkedHashMap<Integer, List<ecmwf.common.database.TransferStatistics>>();
        if (stats != null) {
            for (final var ts : stats) {
                groups.computeIfAbsent(ts.getRequeueHistory(), _ -> new ArrayList<>()).add(ts);
            }
        }
        return new ArrayList<>(groups.values());
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
