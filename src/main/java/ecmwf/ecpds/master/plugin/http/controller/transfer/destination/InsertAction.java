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
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class InsertAction.
 */
public class InsertAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(InsertAction.class);

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
        if (this.isCancelled(request)) {
            return mapping.findForward("cancel");
        }
        final var daf = (DestinationActionForm) form;
        final var d = DestinationHome.create();
        final var action = daf.getActionRequested();
        try {
            if ("copy".equalsIgnoreCase(action)) {
                // Check if the Destination Name is a valid name?
                final var name = daf.getToDestination();
                if (!Format.isValidId(name, "-_")) {
                    throw new Exception(
                            "Please choose a valid non-empty Destination name (only letters, digits, '_' and '-' are allowed): \""
                                    + name + "\"");
                }
                // Create from an existing Destination/Host
                final var mi = MasterManager.getMI();
                final var session = Util.getECpdsSessionFromObject(user);
                mi.copyDestination(session, daf.getFromDestination(), name, daf.getLabel(),
                        "on".equalsIgnoreCase(daf.getCopySharedHost())
                                || "true".equalsIgnoreCase(daf.getCopySharedHost()));
            } else if ("export".equalsIgnoreCase(action)) {
                // Export from an existing Destination/Host
                final var mi = MasterManager.getMI();
                final var session = Util.getECpdsSessionFromObject(user);
                mi.exportDestination(session, daf.getMaster(), daf.getSourceDestination(),
                        "on".equalsIgnoreCase(daf.getCopySourceSharedHost())
                                || "true".equalsIgnoreCase(daf.getCopySourceSharedHost()));
            } else {
                // Check if the Destination Name is a valid name?
                final var name = daf.getName();
                if (!Format.isValidId(name, "-_")) {
                    throw new Exception(
                            "Please choose a valid non-empty Destination name (only letters, digits, '_' and '-' are allowed): \""
                                    + name + "\"");
                }
                // Create from scratch
                daf.populateDestination(d);
                d.insert(user);
            }
        } catch (final Throwable t) {
            log.warn(t);
            final var e = new ECMWFActionFormException(Format.getMessage(t));
            e.initCause(t);
            throw e;
        }
        return mapping.findForward("success");
    }
}
