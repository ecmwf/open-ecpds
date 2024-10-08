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

package ecmwf.ecpds.master.plugin.http.controller.admin;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class DeleteUnsuccessfulTransfersAction.
 */
public class DeleteUnsuccessfulTransfersAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DeleteUnsuccessfulTransfersAction.class);

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final List<DataTransfer> unsuccessful = new ArrayList<>();
        for (final Destination d : DestinationHome.findAll()) {
            unsuccessful.addAll(d.getBadDataTransfers());
        }
        if ("true".equals(request.getParameter("delete"))) {
            var requeuedFilesCount = 0;
            for (final DataTransfer transfer : unsuccessful) {
                try {
                    transfer.delete(user);
                    requeuedFilesCount++;
                } catch (final Exception e) {
                    log.warn("Problem trying to delete Data Transfer '" + transfer.getId() + "'", e);
                }
            }
            request.setAttribute("action", "Deleted");
            request.setAttribute("requeuedSize", requeuedFilesCount);
            return mapping.findForward("list");
        }
        request.setAttribute("transfers", unsuccessful);
        return mapping.findForward("list");
    }
}
