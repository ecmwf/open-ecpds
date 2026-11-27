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
 */

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.model.users.User;

/** Retains only a short-lived PKCE code across the redirect; a Monitor password is never persisted. */
public final class HostPasswordMfaCallbackAction extends PDSAction {
    @Override
    protected boolean isAuthorized(final User user, final String path, final HttpServletRequest request) {
        return true;
    }

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ecmwf.ecpds.master.plugin.http.ECMWFException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        final var session = request.getSession(false);
        final var states = request.getParameterValues("state");
        final var codes = request.getParameterValues("code");
        if (!request.isSecure() || !"GET".equals(request.getMethod()) || session == null || states == null
                || states.length != 1 || states[0].length() > 128 || codes == null || codes.length != 1
                || codes[0].isBlank() || codes[0].length() > 8192 || request.getParameterMap().containsKey("error")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        synchronized (session) {
            if (!(session.getAttribute(
                    PendingHostPasswordAction.ACTIVATION) instanceof PendingHostPasswordAction.PendingActivation pending)
                    || pending.expires() <= System.currentTimeMillis() || pending.code() != null
                    || pending.challenge().authorizationUri() == null || !pending.challenge().state().equals(states[0])
                    || !user.hasAccess("/do/transfer/destination/operations/" + pending.destination() + "/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
            session.setAttribute(PendingHostPasswordAction.ACTIVATION,
                    new PendingHostPasswordAction.PendingActivation(pending.destination(), pending.host(),
                            pending.reference(), pending.challenge(), codes[0], pending.expires()));
            final var destination = URLEncoder.encode(pending.destination(), StandardCharsets.UTF_8).replace("+",
                    "%20");
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.setHeader("Location", request.getContextPath() + "/do/transfer/destination/" + destination
                    + "#password-rotation-" + pending.host());
        }
        return null;
    }
}
