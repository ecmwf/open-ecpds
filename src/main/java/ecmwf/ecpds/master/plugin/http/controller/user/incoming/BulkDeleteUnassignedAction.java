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

package ecmwf.ecpds.master.plugin.http.controller.user.incoming;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.IncomingUserHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Bulk-deletes all Data Users that have no reachable destinations (neither directly assigned nor via any Data Policy).
 * Returns a JSON response of the form {@code {"deleted":N,"errors":M}}.
 */
public class BulkDeleteUnassignedAction extends PDSAction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        var deleted = 0;
        var errors = 0;
        try {
            for (final IncomingUser u : IncomingUserHome.findAll()) {
                if (countReachableDestinations(u) == 0) {
                    try {
                        u.delete(user);
                        deleted++;
                    } catch (final Exception e) {
                        errors++;
                    }
                }
            }
        } catch (final Exception e) {
            writeJson(response, 0, 0, e.getMessage());
            return null;
        }
        writeJson(response, deleted, errors, null);
        return null;
    }

    private static int countReachableDestinations(final IncomingUser u) {
        final var names = new HashSet<String>();
        try {
            for (final Destination dest : u.getAssociatedDestinations()) {
                names.add(dest.getName());
            }
        } catch (final Exception ignored) {
        }
        try {
            for (final IncomingPolicy policy : u.getAssociatedIncomingPolicies()) {
                try {
                    for (final Destination dest : policy.getAssociatedDestinations()) {
                        names.add(dest.getName());
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        return names.size();
    }

    private static void writeJson(final HttpServletResponse response, final int deleted, final int errors,
            final String errorMessage) {
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            final var node = MAPPER.createObjectNode();
            node.put("deleted", deleted);
            node.put("errors", errors);
            if (errorMessage != null) {
                node.put("errorMessage", errorMessage);
            }
            MAPPER.writeValue(response.getWriter(), node);
        } catch (final Exception ignored) {
        }
    }
}
