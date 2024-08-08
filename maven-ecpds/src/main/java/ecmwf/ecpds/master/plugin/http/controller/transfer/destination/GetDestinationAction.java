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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.dao.transfer.DataTransferLightBean;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class GetDestinationAction.
 */
public class GetDestinationAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(GetDestinationAction.class);

    /** The Constant DESTINATIONS_PER_COLUMN. */
    private static final int DESTINATIONS_PER_COLUMN = Cnf.at("MonitorPlugin", "destinationsPerColumn", 25);

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
     * @throws ECMWFException
     *             the ECMWF exception
     * @throws ClassCastException
     *             the class cast exception
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final ArrayList<?> pathParameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (pathParameters.isEmpty()) {
            // There are no Destination specified so we are requested to list
            // the Destinations!
            final var search = Util.getValue(request, "destinationSearch", "");
            final var sortDirection = Util.getValue(request, "sortDirection", "asc");
            final var aliases = Util.getValue(request, "aliases", "All");
            final var status = Util.getValue(request, "destinationStatus", "All Status");
            final var type = Util.getValue(request, "destinationType", "-1");
            final var filter = Util.getValue(request, "destinationFilter", "All");
            Collection<Destination> destinationList;
            try {
                destinationList = DestinationHome.findByUser(user, search, aliases, "asc".equals(sortDirection),
                        StatusFactory.getDestinationStatusCode(status), getDestinationTypeIds(type), filter);
                request.setAttribute("getDestinationsError", "");
            } catch (final TransferException e) {
                request.setAttribute("getDestinationsError", e.getMessage());
                destinationList = new ArrayList<>(0);
            }
            request.setAttribute("statusOptions", getStatusOptions());
            request.setAttribute("filterOptions", getFilterOptions());
            request.setAttribute("typeOptions", getTypeOptions());
            request.setAttribute("destinations", destinationList);
            request.setAttribute("columns", getColumns(destinationList.size()));
            request.setAttribute("hasDestinationSearch", search != null && !search.isBlank());
        } else {
            // We are dealing with a Destination!
            final var daf = (DetailActionForm) form;
            final var id = pathParameters.get(0).toString();
            daf.setId(id);
            // Let's get the Destination!
            final var destination = DestinationHome.findByPrimaryKey(id);
            request.setAttribute("destination", destination);
            final var mode = request.getParameter("mode");
            if ("parameters".equals(mode)) {
                // This is the all_parameters.jsp page!
                return mapping.findForward("allParams");
            }
            if ("traffic".equals(mode)) {
                // This is the traffic.jsp page!
                return mapping.findForward("traffic");
            } else if ("changelog".equals(mode)) {
                // This is the changelog.jsp page!
                return mapping.findForward("changelog");
            } else if ("aliasesfrom".equals(mode)) {
                // This is the aliases_from.jsp page!
                return mapping.findForward("aliasesfrom");
            } else if ("aliasesto".equals(mode)) {
                // This is the aliases_to.jsp page!
                return mapping.findForward("aliasesto");
            } else {
                // This is the main Destination page!
                final var isNotDissemination = !DestinationOption.isDissemination(destination.getType());
                // Keep the DisplayTag query string to propagate sorting and paging options.
                daf.setDisplayTagsParams(getDisplayTagsParams(request));
                daf.setIsMemberState(!user.hasAccess(getResource(request, "nonmemberstate.basepath")));
                // Put filter options in the request so that they won't have to be calculated
                // more than once for the page. If this is the acquisition then we don't need to
                // introduce the Dissemination Streams and Data Streams!
                final var disseminationStreamOptionsWithSizes = isNotDissemination ? new ArrayList<>(0)
                        : daf.getDisseminationStreamOptionsWithSizes();
                request.setAttribute("disseminationStreamOptionsWithSizes", disseminationStreamOptionsWithSizes);
                final int dataStreamOptionsWithSizesSize;
                final int dataTimeOptionsWithSizesSize;
                if (isNotDissemination) {
                    request.setAttribute("dataStreamOptionsWithSizes", new ArrayList<>(0));
                    request.setAttribute("dataTimeOptionsWithSizes", new ArrayList<>(0));
                    dataStreamOptionsWithSizesSize = 0;
                    dataTimeOptionsWithSizesSize = 0;
                } else {
                    final var dataOptions = daf.getDataOptionsWithSizes();
                    request.setAttribute("dataStreamOptionsWithSizes", dataOptions.dataStreamOptionsWithSizes);
                    request.setAttribute("dataTimeOptionsWithSizes", dataOptions.dataTimeOptionsWithSizes);
                    dataStreamOptionsWithSizesSize = dataOptions.dataStreamOptionsWithSizes.size();
                    dataTimeOptionsWithSizesSize = dataOptions.dataTimeOptionsWithSizes.size();
                }
                final var statusOptionsWithSizes = daf.getStatusOptionsWithSizes();
                request.setAttribute("statusOptionsWithSizes", statusOptionsWithSizes);
                final var dateOptions = daf.getDateOptions();
                request.setAttribute("dateOptions", dateOptions);
                // Cells to add at the right of the tables to complete a "square" table.
                final int[] sizes = { disseminationStreamOptionsWithSizes.size(), dataStreamOptionsWithSizesSize,
                        dataTimeOptionsWithSizesSize, statusOptionsWithSizes.size(), dateOptions.size() };
                Arrays.sort(sizes);
                final var biggest = sizes[sizes.length - 1];
                request.setAttribute("cellsAtTheRightForDisseminationStream",
                        biggest - disseminationStreamOptionsWithSizes.size() + 1);
                request.setAttribute("cellsAtTheRightForDataStream", biggest - dataStreamOptionsWithSizesSize + 1);
                request.setAttribute("cellsAtTheRightForDataTime", biggest - dataTimeOptionsWithSizesSize + 1);
                request.setAttribute("cellsAtTheRightForStatus", biggest - statusOptionsWithSizes.size() + 1);
                request.setAttribute("cellsAtTheRightForDate", biggest - dateOptions.size() + 1);
                request.setAttribute("fileNameSearchColspan", dateOptions.size() * 2 + 1);
                // Check if the user has access to the full list of DataTransfers (if it doesn't
                // have any privilege then it should not see the file which have not passed
                // their schedule time.
                var hasAccess = true;
                try {
                    hasAccess = user.hasAccess(getResource(request, "datatransfer.basepath"));
                } catch (final Exception e) {
                    log.error(
                            "Problem checking access for user '{}' to this destination data transfers. Returning all without filtering.",
                            user);
                }
                // Initialize the cursor for the database search
                final var cursor = Util.getDataBaseCursor("transfer", 25, 2, true, request);
                Collection<DataTransferLightBean> transfers;
                try {
                    transfers = daf.getDataTransfers(hasAccess, cursor);
                    request.setAttribute("getTransfersError", "");
                } catch (final TransferException e) {
                    request.setAttribute("getTransfersError", e.getMessage());
                    transfers = new ArrayList<>(0);
                }
                request.setAttribute("filteredTransfers", transfers);
                request.setAttribute("dataTransfersSize", Util.getCollectionSizeFrom(transfers));
                request.setAttribute("products", ProductStatusHome.findAllProductNameTimePairs());
                request.setAttribute("currentDate", new Date());
                final var fileNameSearch = daf.getFileNameSearch();
                request.setAttribute("hasFileNameSearch", fileNameSearch != null && !fileNameSearch.isBlank());
            }
        }
        return mapping.findForward("success");
    }

    /**
     * Gets the display tags params.
     *
     * @param req
     *            the req
     *
     * @return the display tags params
     */
    private static final Collection<Pair> getDisplayTagsParams(final HttpServletRequest req) {
        final List<Pair> params = new ArrayList<>(4);
        final Enumeration<?> e = req.getParameterNames();
        while (e.hasMoreElements()) {
            final var name = e.nextElement().toString();
            if (name.startsWith("d-")) {
                params.add(new Pair(name, req.getParameter(name)));
            }
        }
        return params;
    }

    /**
     * Gets the columns.
     *
     * @param size
     *            the size
     *
     * @return the columns
     */
    private static final Collection<Pair> getColumns(final int size) {
        final Collection<Pair> columns = new ArrayList<>();
        var index = 0;
        while (index + DESTINATIONS_PER_COLUMN < size) {
            columns.add(new Pair(Integer.toString(index), Integer.toString(DESTINATIONS_PER_COLUMN)));
            index += DESTINATIONS_PER_COLUMN;
        }
        if (size - index >= 0 && size > 0) {
            columns.add(new Pair(Integer.toString(index), Integer.toString(size - index)));
        }
        return columns;
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
    private static final String[] getStatusOptions() {
        final var statusNames = StatusFactory.getDestinationStatusNames();
        final var result = new String[statusNames.length + 1];
        result[0] = "All Status";
        System.arraycopy(statusNames, 0, result, 1, statusNames.length);
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

    /**
     * Gets the filter options.
     *
     * @return the filter options
     */
    public static final Collection<Pair> getFilterOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Compressions"));
        for (final String mode : StreamManager.modes) {
            options.add(new Pair(mode, mode));
        }
        return options;
    }
}
