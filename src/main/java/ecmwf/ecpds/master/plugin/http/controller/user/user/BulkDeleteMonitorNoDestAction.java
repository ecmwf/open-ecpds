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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser;
import ecmwf.web.ECMWFException;
import ecmwf.web.home.users.UserHome;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.User;

/**
 * Bulk-deletes all Web Users that are classified as Monitor users but have no destination category assigned (i.e. their
 * categories are exclusively from the monitor set with no {@code * operations} category). Returns a JSON response of
 * the form {@code {"deleted":N,"errors":M}}.
 */
public class BulkDeleteMonitorNoDestAction extends PDSAction {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> MONITOR_CATEGORY_NAMES = Set.of("mstate", "monitoring", "transfers",
            "requirements");
    private static final int STATUS_MONITOR_NO_DEST = 2;

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        var deleted = 0;
        var errors = 0;
        try {
            for (final Object obj : UserHome.findAll()) {
                if (!(obj instanceof final WebUser u)) {
                    continue;
                }
                if (getMonitorStatus(getCategories(u)) == STATUS_MONITOR_NO_DEST) {
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

    private static java.util.Collection<Category> getCategories(final WebUser u) {
        final var cats = new ArrayList<Category>();
        try {
            for (final Object obj : u.getCategories()) {
                if (obj instanceof final Category cat) {
                    cats.add(cat);
                }
            }
        } catch (final Exception ignored) {
        }
        return cats;
    }

    private static int getMonitorStatus(final java.util.Collection<Category> cats) {
        var hasMonitor = false;
        var hasDestination = false;
        var hasOther = false;
        for (final Category cat : cats) {
            final var name = cat.getName();
            if (name != null && name.endsWith(" operations")) {
                hasDestination = true;
            } else if (name != null && MONITOR_CATEGORY_NAMES.contains(name)) {
                hasMonitor = true;
            } else {
                hasOther = true;
            }
        }
        if (!hasMonitor || hasOther) {
            return 0;
        }
        return hasDestination ? 1 : STATUS_MONITOR_NO_DEST;
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
