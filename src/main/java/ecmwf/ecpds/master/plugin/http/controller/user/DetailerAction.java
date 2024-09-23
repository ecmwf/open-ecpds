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

package ecmwf.ecpds.master.plugin.http.controller.user;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.home.users.ResourceHome;
import ecmwf.web.home.users.UserHome;
import ecmwf.web.model.users.User;

/**
 * The Class DetailerAction.
 */
public class DetailerAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        String url = null;
        if (((url = request.getParameter("page")) == null) && ((url = request.getHeader("referer")) == null)) {
            throw new ECMWFActionFormException(
                    "Expected URL information in 'page' parameter or 'referer' header: (Headers:"
                            + request.getHeaderNames() + ")");
        }
        if (url.startsWith("http://")) {
            url = url.substring(url.indexOf('/', 8));
        } else if (url.startsWith("https://")) {
            url = url.substring(url.indexOf('/', 9));
        }
        final var resource = ResourceHome.findByURI(url);
        final Collection<?> categories = resource.getCategories();
        final Collection<User> users = new ArrayList<>();
        final Collection<User> usersNo = new ArrayList<>();
        final Iterator<User> j = UserHome.findAll().iterator();
        while (j.hasNext()) {
            final var u = j.next();
            if (u.hasAccess(resource)) {
                users.add(u);
            } else {
                usersNo.add(u);
            }
        }
        request.setAttribute("resource", resource);
        request.setAttribute("categories", categories);
        request.setAttribute("users", users);
        request.setAttribute("usersNo", usersNo);
        request.setAttribute("user", user);
        return mapping.findForward("success");
    }
}
