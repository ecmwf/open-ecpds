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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.ECMWFException;
import ecmwf.ecpds.master.plugin.http.model.users.User;

public final class GetWeatherAction extends PDSAction {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, User user) throws ECMWFException {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var snapshot = MasterManager.getMI().getWeatherViewer(Util.getECpdsSessionFromObject(user),
                    !"true".equals(request.getParameter("stateOnly")));
            JSON.writeValue(response.getWriter(), snapshot);
        } catch (Exception e) {
            org.apache.logging.log4j.LogManager.getLogger(GetWeatherAction.class).warn("Weather viewer unavailable", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        return null;
    }
}
