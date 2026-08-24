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

package ecmwf.ecpds.master.plugin.http.controller.user.api;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

public class GetApiEventsAction extends PDSAction {

    public static final String DATE_FILTER_KEY = "ecpds_apiEvent_dateNow";
    private static final int DAYS_BACK = 7;

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var iso = getISOFormat();
        var date = request.getParameter("date");
        if (date == null) {
            final var stored = request.getSession().getAttribute(DATE_FILTER_KEY);
            date = stored == null ? iso.format(new Date()) : stored.toString();
        }
        request.getSession().setAttribute(DATE_FILTER_KEY, date);
        request.setAttribute("selectedDate", date);
        request.setAttribute("dateOptions", getDateOptions(DAYS_BACK, false));
        // clientFilter pre-fills the DataTable client ID filter
        final var clientFilter = request.getParameter("clientId");
        if (clientFilter != null && !clientFilter.isBlank()) {
            request.setAttribute("clientFilter", clientFilter);
        }
        return mapping.findForward("success");
    }

    @Override
    public boolean match(final ModelBean b, final String what) {
        return true;
    }
}
