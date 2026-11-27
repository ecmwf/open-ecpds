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

package ecmwf.ecpds.master.plugin.http.controller.transfer.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.ECMWFException;
import ecmwf.ecpds.master.plugin.http.controller.ECMWFActionFormException;
import ecmwf.ecpds.master.plugin.http.model.users.User;

public class DataTransferDispatcherAction extends PDSAction {

    @Override
    public ActionForward safeAuthorizedPerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, User user) throws ECMWFException {

        String json = request.getParameter("json");

        try {
            if ("list".equalsIgnoreCase(json)) {
                return withServlet(new GetDataTransferListJsonAction()).safeAuthorizedPerform(mapping, form, request,
                        response, user);
            }

            return withServlet(new GetDataTransferAction()).safeAuthorizedPerform(mapping, form, request, response,
                    user);

        } catch (Exception e) {
            throw new ECMWFActionFormException(e.getMessage(), e);
        }
    }

    /**
     * Propagates this dispatcher's {@link org.apache.struts.action.ActionServlet} to a directly-instantiated
     * sub-action, since Struts only calls {@code setServlet} on actions it creates itself via the action mapping. Not
     * doing so leaves {@code action.getServlet()} {@code null}, which makes any call relying on it (e.g.
     * {@link ecmwf.ecpds.master.plugin.http.controller.ECMWFAction#getResource}) throw a {@link NullPointerException}.
     *
     * @param action
     *            the newly created sub-action
     *
     * @return the same action, for chaining
     */
    private <T extends org.apache.struts.action.Action> T withServlet(final T action) {
        action.setServlet(getServlet());
        return action;
    }
}