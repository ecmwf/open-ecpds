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
 * Handles the "Origin Location" admin page. Lets administrators configure the geographic location used to place the
 * MasterServer's own "origin" marker on the Live Earth globe (globe.jsp), either automatically (GeoIP lookup against
 * the MasterServer's own hostname/IP address, the default) or manually (an explicit latitude/longitude, stored in the
 * SYS_CONFIG database table, group "Master").
 *
 * <p>
 * GET: shows the form, pre-filled with the current mode and coordinates (the automatically-resolved ones when in
 * automatic mode, so switching to manual starts from a sensible value).
 * <p>
 * POST: stores the submitted mode/coordinates.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2026-09-13
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * The Class OriginLocationAction.
 */
public class OriginLocationAction extends PDSAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {

        final ecmwf.ecpds.master.DataBaseInterface db;
        try {
            db = MasterManager.getDB();
        } catch (final Exception e) {
            request.setAttribute("olError", "Unable to connect to the database: " + e.getMessage());
            loadCurrentState(null, request);
            return mapping.findForward("form");
        }

        // POST — validate and store
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            final var automatic = "on".equalsIgnoreCase(request.getParameter("automaticLocation"))
                    || "true".equalsIgnoreCase(request.getParameter("automaticLocation"));
            if (automatic) {
                try {
                    db.clearOriginLocationOverride();
                    request.setAttribute("olSuccess", "Origin Location reverted to automatic (GeoIP) resolution.");
                } catch (final Exception e) {
                    request.setAttribute("olError", "Failed to store the setting: " + e.getMessage());
                }
            } else {
                final var latitudeText = request.getParameter("latitude");
                final var longitudeText = request.getParameter("longitude");
                final Double latitude = parseCoordinate(latitudeText);
                final Double longitude = parseCoordinate(longitudeText);
                if (latitude == null || longitude == null) {
                    request.setAttribute("olError", "Please provide a valid latitude and longitude.");
                } else if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                    request.setAttribute("olError",
                            "Latitude must be between -90 and 90, and longitude between -180 and 180.");
                } else {
                    try {
                        db.setOriginLocationOverride(latitude, longitude);
                        request.setAttribute("olSuccess", "Origin Location saved successfully.");
                    } catch (final Exception e) {
                        request.setAttribute("olError", "Failed to store the setting: " + e.getMessage());
                    }
                }
            }
        }

        // GET (or after POST) — show the form pre-filled with the current values
        loadCurrentState(db, request);
        return mapping.findForward("form");
    }

    /**
     * Populates the request with the current mode/coordinates, and the automatically-resolved location/hostname (for
     * display, and to pre-fill the manual fields with a sensible starting point).
     */
    private static void loadCurrentState(final ecmwf.ecpds.master.DataBaseInterface db,
            final HttpServletRequest request) {
        var automatic = true;
        double[] override = null;
        if (db != null) {
            try {
                automatic = db.isOriginLocationAutomatic();
                override = db.getOriginLocationOverride();
            } catch (final Exception e) {
                // Keep defaults (automatic, no override) — already reflected in the initialized locals above.
            }
        }
        request.setAttribute("automaticLocation", automatic);
        request.setAttribute("overrideLatitude", override != null ? override[0] : null);
        request.setAttribute("overrideLongitude", override != null ? override[1] : null);

        String resolvedHost = null;
        double[] resolvedLocation = null;
        try {
            resolvedHost = MasterManager.getMI().getLiveTransferOriginHost();
            resolvedLocation = MasterManager.getMI().getAutomaticLiveTransferOrigin();
        } catch (final Exception e) {
            // Leave resolvedHost/resolvedLocation as null — the JSP shows "unresolved" in that case.
        }
        request.setAttribute("resolvedHost", resolvedHost);
        request.setAttribute("resolvedLatitude", resolvedLocation != null ? resolvedLocation[0] : null);
        request.setAttribute("resolvedLongitude", resolvedLocation != null ? resolvedLocation[1] : null);
    }

    /**
     * Parses a submitted coordinate text field, returning {@code null} if it is blank or not a valid number (rather
     * than throwing) so the caller can report a single, friendly validation message.
     */
    private static Double parseCoordinate(final String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(text.trim());
        } catch (final NumberFormatException e) {
            return null;
        }
    }
}
