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
 * In applying this License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/** Read-only, destination-authorized MQTT publication history. */
public final class MQTTActivityAction extends PDSAction {
    private static final Logger LOG = LogManager.getLogger(MQTTActivityAction.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected boolean isAuthorized(final User user, final String currentPath, final HttpServletRequest request)
            throws ECMWFException {
        return user.hasAccess(getResource(request, "monitoring.basepath") + "/");
    }

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user) {
        response.setHeader("Cache-Control", "no-store");
        if (!"data".equals(request.getParameter("view"))) {
            return mapping.findForward("success");
        }
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var minutes = Math.max(1, Math.min(1440, number(request, "minutes", 60)));
            final var result = MasterManager.getMI().getMQTTActivity(Util.getECpdsSessionFromObject(user),
                    System.currentTimeMillis() - minutes * 60000L, text(request, "destination"), text(request, "topic"),
                    text(request, "status"), number(request, "offset", 0));
            JSON.writeValue(response.getWriter(), result);
        } catch (final Exception e) {
            LOG.warn("Retrieving MQTT activity", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        return null;
    }

    private static int number(final HttpServletRequest request, final String name, final int fallback) {
        try {
            return Integer.parseInt(request.getParameter(name));
        } catch (final RuntimeException e) {
            return fallback;
        }
    }

    private static String text(final HttpServletRequest request, final String name) {
        final var value = request.getParameter(name);
        return value == null ? "" : value.substring(0, Math.min(1024, value.length()));
    }
}
