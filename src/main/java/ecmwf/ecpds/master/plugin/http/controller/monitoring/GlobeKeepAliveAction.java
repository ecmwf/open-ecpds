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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 7.4.0
 * @since 2026-09-14
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class GlobeKeepAliveAction.
 *
 * Tiny authenticated no-op endpoint polled periodically (via {@code fetch}) by the "Live ECPDS Earth" globe page (see
 * {@code globe.jsp}) while it is open. The globe page itself never issues any other HTTP request after the initial page
 * load - all live data flows over the separate {@code /ws/globe} WebSocket - so, unlike every other page in the
 * application, simply leaving it open does not reset the HttpSession's inactivity timer, and the user's login
 * eventually expires from under them even while actively watching the globe. Hitting this endpoint (which goes through
 * the same Struts/ACL authentication chain as any other action, and therefore does touch the session) fixes that
 * without requiring a full page reload, which would otherwise reset the globe/Cesium view.
 */
public class GlobeKeepAliveAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return null;
    }
}
