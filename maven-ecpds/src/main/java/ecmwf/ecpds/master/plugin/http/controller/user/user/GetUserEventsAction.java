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

package ecmwf.ecpds.master.plugin.http.controller.user.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.database.Event;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.home.users.UserHome;
import ecmwf.web.model.users.User;

/**
 * The Class GetUserEventsAction.
 */
public class GetUserEventsAction extends PDSAction {

    /** The Constant DATE_FILTER_KEY. */
    public static final String DATE_FILTER_KEY = "ecpds_user_dateNow";

    /** The Constant DAYS_BACK. */
    private static final int DAYS_BACK = 7;

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
        final ArrayList<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        final var iso = getISOFormat();
        // Get the date filter as a parameter or get it from the stored
        // value in the session.
        var date = request.getParameter("date");
        if (date == null) {
            final var dateO = request.getSession().getAttribute(DATE_FILTER_KEY);
            if (dateO == null) {
                date = iso.format(new Date());
            } else {
                date = dateO.toString();
            }
        } else {
            parameters.remove("date");
        }
        // Date filtering options
        request.getSession().setAttribute(DATE_FILTER_KEY, date);
        request.setAttribute("selectedDate", date);
        request.setAttribute("dateOptions", getDateOptions(DAYS_BACK, false));
        // Initialize the cursor for the database search
        final var cursor = Util.getDataBaseCursor("event", 25, 0, true, request);
        final String userName;
        if (parameters.size() > 0) {
            request.setAttribute("user", UserHome.findByPrimaryKey(userName = parameters.get(0).toString()));
        } else {
            userName = null;
        }
        // Now get the history list
        final Collection<Event> history;
        try {
            history = MasterManager.getDB().getECuserEvents(userName, iso.parse(date), request.getParameter("search"),
                    cursor);
        } catch (final Exception e) {
            throw new ECMWFActionFormException("Error getting history", e);
        }
        request.setAttribute("events", convertCollectionIntoPresentationEvent(history));
        request.setAttribute("eventsSize", Util.getCollectionFrom(history));
        return mapping.findForward("success");
    }

    /**
     * Convert collection into presentation event.
     *
     * @param c
     *            the c
     *
     * @return the collection
     */
    private static final Collection<PresentationEvent> convertCollectionIntoPresentationEvent(
            final Collection<Event> c) {
        final Collection<PresentationEvent> out = new ArrayList<>(c.size());
        for (final Event event : c) {
            out.add(new PresentationEvent(event));
        }
        return out;
    }
}
