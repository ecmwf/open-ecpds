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

package ecmwf.ecpds.master.plugin.http.controller.datafile;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Action for the global Data Rates page (/do/datafile/datarates).
 * Loads traffic data aggregated across all destinations and places it in
 * request scope for rendering by datarates.jsp.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Loads global (all-destination) traffic data and forwards to the Data Rates tile.
 */
public class GetDataRatesAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(GetDataRatesAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        try {
            request.setAttribute("trafficList", MasterManager.getDB().getAllTraffic());
        } catch (final Exception e) {
            _log.error("Failed to load global traffic data", e);
            request.setAttribute("trafficList", Collections.emptyList());
            request.setAttribute("trafficError", e.getMessage());
        }
        return mapping.findForward("success");
    }
}
