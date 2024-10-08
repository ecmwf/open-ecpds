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

package ecmwf.ecpds.master.plugin.http.controller.user.category;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.home.users.CategoryHome;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.User;

/**
 * The Class GetCategoryAction.
 */
public class GetCategoryAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final ArrayList<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (!parameters.isEmpty()) {
            request.setAttribute("category", CategoryHome.findByPrimaryKey(parameters.get(0).toString()));
            return mapping.findForward("success");
        }
        final var search = request.getParameter("search");
        Collection<ModelBean> categories = CategoryHome.findAll();
        if (search != null && !"".equals(search.trim())) {
            categories = search(categories, search);
        }
        request.setAttribute("categories", categories);
        return mapping.findForward("success");
    }

    /**
     * {@inheritDoc}
     *
     * Match.
     */
    @Override
    public boolean match(final ModelBean b, String what) {
        final var a = (Category) b;
        what = what.toLowerCase();
        return a.getDescription().toLowerCase().contains(what) || a.getName().toLowerCase().contains(what);
    }
}
