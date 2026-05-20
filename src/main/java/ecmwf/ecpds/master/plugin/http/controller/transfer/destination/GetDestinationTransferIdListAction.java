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
 * Returns a lightweight JSON array of all transfer IDs matching the current filter — used by the A/N/R bulk-selection
 * buttons to select across all pages without re-rendering the full table.
 *
 * Response: {"ids": ["123", "456", ...]}
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDestinationTransferIdListAction.
 */
public class GetDestinationTransferIdListAction extends PDSAction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {

        final var destinationName = Util.getValue(request, "destinationName", "");
        final var disseminationStream = Util.getValue(request, "disseminationStream", "");
        final var dataStream = Util.getValue(request, "dataStream", "");
        final var dataTime = Util.getValue(request, "dataTime", "");
        final var status = Util.getValue(request, "status", "");
        final var fileNameSearch = Util.getValue(request, "fileNameSearch", "");
        final var dateParam = Util.getValue(request, "date", "");

        final var iso = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        if (!dateParam.isEmpty() && !"All".equalsIgnoreCase(dateParam)) {
            try {
                date = iso.parse(dateParam);
            } catch (final ParseException _) {
            }
        }

        boolean hasAccess = true;
        try {
            hasAccess = user.hasAccess(getResource(request, "datatransfer.basepath"));
        } catch (final Exception _) {
        }

        final var root = MAPPER.createObjectNode();
        final var ids = root.putArray("ids");

        try {
            final Collection<DataTransfer> transfers = (Collection<DataTransfer>) (Collection<?>) DataTransferHome
                    .findByFilter(destinationName, disseminationStream, dataStream, dataTime, status, hasAccess,
                            fileNameSearch, date);
            for (final DataTransfer dt : transfers) {
                if (!dt.getExpired()) {
                    ids.add(dt.getId());
                }
            }
        } catch (final Exception _) {
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            MAPPER.writeValue(response.getWriter(), root);
        } catch (final Exception _) {
        }
        return null;
    }
}
