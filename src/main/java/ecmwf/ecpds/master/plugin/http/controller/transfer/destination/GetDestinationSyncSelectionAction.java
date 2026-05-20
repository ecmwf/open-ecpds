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
 * Syncs the client-side selection state to the server before a basket operation. Supports two modes:
 * <ul>
 * <li><b>replace</b> (type=replace or omitted): Clears all existing selections then marks the submitted ids[] as "on".
 * Used after A/N/R which has the full selection in client memory.</li>
 * <li><b>delta</b> (type=delta): Applies incremental changes on top of the existing server selection. Marks add[] IDs
 * as "on" and del[] IDs as "off". Used after individual star-clicks where the client only knows what changed.</li>
 * </ul>
 * Called via AJAX (POST) before the form submit so that the GET form URL does not carry thousands of selectedTransfer
 * params (HTTP 431 prevention).
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDestinationSyncSelectionAction.
 */
public class GetDestinationSyncSelectionAction extends PDSAction {

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {

        final var destinationName = Util.getValue(request, "destinationName", "");
        final var type = Util.getValue(request, "type", "replace");

        try {
            final var daf = (DetailActionForm) request.getSession().getAttribute("destinationDetailActionForm");
            if (daf != null) {
                daf.setId(destinationName);
                if ("delta".equals(type)) {
                    // Apply incremental changes: add[] → "on", del[] → "off"
                    final var addIds = request.getParameterValues("add[]");
                    final var delIds = request.getParameterValues("del[]");
                    if (addIds != null) {
                        for (final var id : addIds) {
                            if (id != null && !id.isBlank()) {
                                daf.setSelectedTransfer(id, "on");
                            }
                        }
                    }
                    if (delIds != null) {
                        for (final var id : delIds) {
                            if (id != null && !id.isBlank()) {
                                daf.setSelectedTransfer(id, "off");
                            }
                        }
                    }
                } else {
                    // Replace mode: clear everything then set the provided IDs
                    final var ids = request.getParameterValues("ids[]");
                    daf.cleanSelectedTransfers();
                    if (ids != null) {
                        for (final var id : ids) {
                            if (id != null && !id.isBlank()) {
                                daf.setSelectedTransfer(id, "on");
                            }
                        }
                    }
                }
            }
        } catch (final Exception _) {
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{}");
        } catch (final Exception _) {
        }
        return null;
    }
}
