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

import ecmwf.common.database.DestinationMetaValue;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * Import XML metadata files into the destination metadata DB.
 *
 * <p>
 * File scanning and XML parsing are delegated to the MasterServer (via
 * {@code MasterManager.getDB().scanMetadataAttachments(destinationName)}) so they always run on the machine that owns
 * the attachment directories.
 *
 * <p>
 * GET: shows preview/import form. GET with {@code confirm=true}: commits import.
 */
public class ImportDestinationMetaDataAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ImportDestinationMetaDataAction.class);

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException, ecmwf.ecpds.master.plugin.http.model.transfer.TransferException {
        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        if (params.isEmpty()) {
            throw new ECMWFActionFormException("Missing destination parameter");
        }
        final var destinationName = params.get(0).toString();
        final var destination = DestinationHome.findByPrimaryKey(destinationName);
        request.setAttribute("destination", destination);

        final var confirm = "true".equalsIgnoreCase(request.getParameter("confirm"));

        try {
            final var db = MasterManager.getDB();

            // Scanning happens on the MasterServer side — only for this destination
            final var scanned = db.scanMetadataAttachments(destinationName);
            final List<Map<String, Object>> preview = scanned.getOrDefault(destinationName, List.of());

            if (confirm && !preview.isEmpty()) {
                final var values = new ArrayList<DestinationMetaValue>();
                for (final var entry : preview) {
                    if (entry.containsKey("error")) {
                        continue;
                    }
                    final var v = new DestinationMetaValue();
                    v.setFieldId((Integer) entry.get("fieldId"));
                    v.setValue((String) entry.get("value"));
                    final var pos = entry.get("position");
                    if (pos instanceof Integer p) {
                        v.setPosition(p);
                    }
                    v.setBy(user.getName());
                    values.add(v);
                }
                db.setDestinationMetaValues(destinationName, values);
                request.setAttribute("importDone", true);
                request.setAttribute("importCount", values.size());
            } else {
                request.setAttribute("importPreview", preview);
            }
        } catch (final Exception e) {
            _log.warn("ImportDestinationMetaDataAction", e);
            request.setAttribute("importError", e.getMessage());
        }
        return mapping.findForward("success");
    }
}
