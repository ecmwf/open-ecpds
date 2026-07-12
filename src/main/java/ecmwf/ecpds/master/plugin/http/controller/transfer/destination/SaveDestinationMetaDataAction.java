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

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.DestinationMetaValue;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * Saves destination metadata values via JSON POST. Returns JSON: {"success":true} or {"success":false,"error":"..."}
 */
public class SaveDestinationMetaDataAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SaveDestinationMetaDataAction.class);

    /** The Constant _mapper. */
    private static final ObjectMapper _mapper = new ObjectMapper();

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            @SuppressWarnings("unchecked")
            final var body = _mapper.readValue(request.getInputStream(), Map.class);
            final var destinationName = (String) body.get("destination");
            if (destinationName == null || destinationName.isBlank()) {
                throw new IllegalArgumentException("Missing destination");
            }
            @SuppressWarnings("unchecked")
            final var rawValues = (List<Map<String, Object>>) body.get("values");
            final var values = new ArrayList<DestinationMetaValue>();
            if (rawValues != null) {
                final var by = user.getName();
                for (final var m : rawValues) {
                    final var v = new DestinationMetaValue();
                    v.setFieldId(Integer.parseInt(String.valueOf(m.get("DMF_ID"))));
                    final var val = m.get("DMV_VALUE");
                    v.setValue(val != null ? String.valueOf(val) : null);
                    final var pos = m.get("DMV_POSITION");
                    if (pos != null) {
                        v.setPosition(Integer.parseInt(String.valueOf(pos)));
                    }
                    v.setBy(by);
                    values.add(v);
                }
            }
            MasterManager.getDB().setDestinationMetaValues(destinationName, values);
            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("SaveDestinationMetaDataAction", e);
            try {
                final var msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "error";
                response.getWriter().write("{\"success\":false,\"error\":\"" + msg + "\"}");
            } catch (final Exception ignored) {
            }
        }
        return null; // already wrote response
    }
}
