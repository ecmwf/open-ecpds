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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.DataBaseInterface;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.controller.monitoring.MonitoringStatusColors;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Configures the shared status colors used throughout the Monitor UI.
 */
public class MonitoringColorsAction extends PDSAction {

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final DataBaseInterface db;
        try {
            db = MasterManager.getDB();
        } catch (final Exception e) {
            request.setAttribute("mcError", "Unable to connect to the database: " + e.getMessage());
            loadDefaults(request);
            return mapping.findForward("form");
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            try {
                saveColors(db, request);
                request.setAttribute("mcSuccess",
                        "on".equals(request.getParameter("resetDefaults"))
                                ? "Monitoring status colors restored to their built-in defaults."
                                : "Monitoring status colors saved successfully.");
            } catch (final Exception e) {
                request.setAttribute("mcError", "Failed to store the colors: " + e.getMessage());
            } finally {
                MonitoringStatusColors.invalidateCache();
            }
        }

        try {
            request.setAttribute("statusColors",
                    MonitoringStatusColors.colors(db.getMonitorConfigGroup(MonitoringStatusColors.CONFIG_GROUP)));
            request.setAttribute("colorSettings", MonitoringStatusColors.settings());
            request.setAttribute("colorGroups", MonitoringStatusColors.groups());
        } catch (final Exception e) {
            request.setAttribute("mcError", "Unable to load the current colors: " + e.getMessage());
            loadDefaults(request);
        }
        return mapping.findForward("form");
    }

    private static void saveColors(final DataBaseInterface db, final HttpServletRequest request) throws Exception {
        final var settings = MonitoringStatusColors.settings();
        final var reset = "on".equals(request.getParameter("resetDefaults"));
        final Map<String, String> submitted = new LinkedHashMap<>();
        if (!reset) {
            for (final var setting : settings) {
                submitted.put(setting.getKey(),
                        MonitoringStatusColors.normalizeColor(request.getParameter(setting.getKey())));
            }
        }
        for (final var setting : settings) {
            if (reset || setting.getDefaultColor().equalsIgnoreCase(submitted.get(setting.getKey()))) {
                db.deleteMonitorConfigValue(MonitoringStatusColors.CONFIG_GROUP, setting.getKey());
            } else {
                db.setMonitorConfigValue(MonitoringStatusColors.CONFIG_GROUP, setting.getKey(),
                        submitted.get(setting.getKey()));
            }
        }
    }

    private static void loadDefaults(final HttpServletRequest request) {
        request.setAttribute("statusColors", MonitoringStatusColors.colors(Map.of()));
        request.setAttribute("colorSettings", MonitoringStatusColors.settings());
        request.setAttribute("colorGroups", MonitoringStatusColors.groups());
    }
}
