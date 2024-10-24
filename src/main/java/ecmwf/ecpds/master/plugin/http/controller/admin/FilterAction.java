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

package ecmwf.ecpds.master.plugin.http.controller.admin;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class FilterAction.
 */
public class FilterAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var faf = (FilterActionForm) form;
        faf.setDefaultValues(user);
        if ((faf.getDestination() == null) || (faf.getEmail() == null) || (faf.getFilter() == null)
                || (faf.getDate() == null) || (faf.getIncludeStdby() == null)) {
            return mapping.findForward("form");
        }
        try {
            final var message = MasterManager.getMI().computeFilterEfficiency(Util.getECpdsSessionFromObject(user),
                    faf.getDestination(), faf.getEmail(), faf.getFilter(),
                    Format.toTime(FilterActionForm.dateFormat, faf.getDate()), faf.getIncludeStdbyBoolean(),
                    faf.getPattern());
            request.setAttribute("message", message);
        } catch (final Exception e) {
            throw new ECMWFException(e.getMessage(), e);
        }
        return mapping.findForward("success");
    }
}
