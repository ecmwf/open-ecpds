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
 * Handles AJAX POST submissions from the feedback offcanvas.
 * Returns a JSON response: {"ok":true} or {"ok":false,"error":"..."}.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.database.Feedback;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class SubmitFeedbackAction.
 */
public class SubmitFeedbackAction extends PDSAction {

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
            final var ratingStr = request.getParameter("rating");
            final var anonymous = "true".equals(request.getParameter("anonymous"));
            final var rating = ratingStr != null ? Integer.parseInt(ratingStr.trim()) : 0;
            if (rating < 1 || rating > 5) {
                writeJson(response, "{\"ok\":false,\"error\":\"Invalid rating\"}");
                return null;
            }
            final var fb = new Feedback();
            fb.setTime(new BigDecimal(System.currentTimeMillis()));
            fb.setRating(rating);
            fb.setComment(trim(request.getParameter("comment")));
            fb.setUsage(trim(request.getParameter("usage")));
            fb.setComponent(trim(request.getParameter("component")));
            fb.setOneThing(trim(request.getParameter("oneThing")));
            final var recommendStr = request.getParameter("recommend");
            if ("yes".equals(recommendStr)) {
                fb.setRecommend(Boolean.TRUE);
            } else if ("no".equals(recommendStr)) {
                fb.setRecommend(Boolean.FALSE);
            }
            fb.setQuoteOk("true".equals(request.getParameter("quoteOk")));
            if (!anonymous) {
                fb.setWebUserId(user.getUid());
                // Only store contact email if not anonymous
                fb.setContact(trim(request.getParameter("contact")));
            }
            final var ok = MasterManager.getDB().tryInsertFeedback(fb);
            writeJson(response, ok ? "{\"ok\":true}" : "{\"ok\":false,\"error\":\"Failed to save\"}");
        } catch (final NumberFormatException e) {
            writeJson(response, "{\"ok\":false,\"error\":\"Invalid input\"}");
        } catch (final Exception e) {
            writeJson(response, "{\"ok\":false,\"error\":\"Server error\"}");
        }
        return null;
    }

    private static String trim(final String s) {
        if (s == null)
            return null;
        final var t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static void writeJson(final HttpServletResponse response, final String json) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(json);
            response.getWriter().flush();
        } catch (final java.io.IOException ignored) {
        }
    }
}
