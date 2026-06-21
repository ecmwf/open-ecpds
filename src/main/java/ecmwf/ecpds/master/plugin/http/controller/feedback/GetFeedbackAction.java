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

package ecmwf.ecpds.master.plugin.http.controller.feedback;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Serves the admin feedback review page and handles delete / mark-reviewed actions.
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

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * The Class GetFeedbackAction.
 */
public class GetFeedbackAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        try {
            final var parameters = ECMWFActionForm.getPathParameters(mapping, request);
            if (!parameters.isEmpty()) {
                final var action = parameters.get(0).toString();
                final var idStr = parameters.size() > 1 ? parameters.get(1).toString() : null;
                if ("deleteReviewed".equals(action)) {
                    MasterManager.getDB().removeAllReviewedFeedback();
                } else if ("deleteAll".equals(action)) {
                    MasterManager.getDB().removeAllFeedback();
                } else if (idStr != null) {
                    final var id = Long.parseLong(idStr);
                    final var list = MasterManager.getDB().getFeedbackList();
                    final var opt = list.stream().filter(f -> f.getId() == id).findFirst();
                    if (opt.isPresent()) {
                        if ("delete".equals(action)) {
                            MasterManager.getDB().removeFeedback(opt.get());
                        } else if ("review".equals(action)) {
                            final var fb = opt.get();
                            fb.setReviewed(true);
                            MasterManager.getDB().markFeedbackReviewed(fb);
                        }
                    }
                }
                return mapping.findForward("success");
            }
            request.setAttribute("feedbackList", MasterManager.getDB().getFeedbackList());
        } catch (final Exception e) {
            request.setAttribute("feedbackList", java.util.Collections.emptyList());
        }
        return mapping.findForward("success");
    }
}
