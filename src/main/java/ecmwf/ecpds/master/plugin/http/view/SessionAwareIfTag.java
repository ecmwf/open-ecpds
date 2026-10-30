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

package ecmwf.ecpds.master.plugin.http.view;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.web.model.users.User;
import ecmwf.web.view.taglibs.auth2.conditional.IfTag;

/**
 * Keeps missing-session authorization conditions false without reporting expected unauthenticated rendering as errors.
 */
public class SessionAwareIfTag extends IfTag {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogManager.getLogger(SessionAwareIfTag.class);
    private static final String LOGGED = SessionAwareIfTag.class.getName() + ".logged";

    private boolean missingUser;

    @Override
    public int doStartTag() {
        final var request = (HttpServletRequest) pageContext.getRequest();
        final var session = request.getSession(false);
        missingUser = session == null || session.getAttribute(User.SESSION_KEY) == null;
        if (!missingUser) {
            return super.doStartTag();
        }
        if (LOG.isDebugEnabled() && request.getAttribute(LOGGED) == null) {
            request.setAttribute(LOGGED, Boolean.TRUE);
            final var error = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            final var forward = request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
            final var include = request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
            LOG.debug(
                    "No authenticated session user while rendering an authorization condition; "
                            + "condition evaluated as false. request={}, current={}, rendering={}, dispatcher={}",
                    path(error != null ? error : forward != null ? forward : request.getRequestURI()),
                    path(request.getRequestURI()), path(include != null ? include : request.getServletPath()),
                    request.getDispatcherType());
        }
        // Keep then/else body processing, but do not invoke the legacy missing-user error branch.
        return EVAL_BODY_BUFFERED;
    }

    @Override
    protected boolean userEnabled() {
        return !missingUser && super.userEnabled();
    }

    private static String path(final Object value) {
        if (value == null) {
            return "-";
        }
        return value.toString().split("[?#;]", 2)[0].replaceAll("[\\p{Cntrl}]", "_");
    }
}
