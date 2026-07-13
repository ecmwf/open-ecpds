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
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * Bulk-import XML metadata files for all destinations at once.
 *
 * <p>
 * File scanning and XML parsing are delegated to the MasterServer (via
 * {@code MasterManager.getDB().scanMetadataAttachments(null)}) so they always run on the machine that owns the
 * attachment directories — regardless of whether the web application and the MasterServer run on different hosts.
 *
 * <p>
 * GET without {@code confirm=true}: returns a grouped preview of what would be imported. GET with {@code confirm=true}:
 * commits all parsed values to the DB.
 */
public class BulkImportDestinationMetaDataAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(BulkImportDestinationMetaDataAction.class);

    /** {@inheritDoc} */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {

        final var confirm = "true".equalsIgnoreCase(request.getParameter("confirm"));

        try {
            final var db = MasterManager.getDB();

            // Scanning happens on the MasterServer side — returns dest → entries
            final var byDestination = db.scanMetadataAttachments(null);

            var totalValues = 0;
            var totalErrors = 0;
            for (final var entries : byDestination.values()) {
                for (final var e : entries) {
                    if (e.containsKey("error")) {
                        totalErrors++;
                    } else {
                        totalValues++;
                    }
                }
            }

            if (confirm && !byDestination.isEmpty()) {
                var importedDests = 0;
                var importedValues = 0;
                for (final var entry : byDestination.entrySet()) {
                    final var destName = entry.getKey();
                    final var values = new ArrayList<DestinationMetaValue>();
                    for (final var row : entry.getValue()) {
                        if (row.containsKey("error")) {
                            continue;
                        }
                        final var v = new DestinationMetaValue();
                        v.setFieldId((Integer) row.get("fieldId"));
                        v.setValue((String) row.get("value"));
                        final var pos = row.get("position");
                        if (pos instanceof Integer p) {
                            v.setPosition(p);
                        }
                        v.setBy(user.getName());
                        values.add(v);
                    }
                    if (!values.isEmpty()) {
                        db.setDestinationMetaValues(destName, values);
                        importedDests++;
                        importedValues += values.size();
                    }
                }
                request.setAttribute("importDone", true);
                request.setAttribute("importedDests", importedDests);
                request.setAttribute("importedValues", importedValues);
                // Invalidate the contacts cache so email searches reflect the new data immediately
                try {
                    MasterManager.getMI().refreshContactsCache();
                } catch (final Exception ex) {
                    _log.warn("BulkImportDestinationMetaDataAction: could not refresh contacts cache", ex);
                }
            } else {
                // Build a lightweight summary list for the preview (no raw values sent to JSP)
                final var destSummary = new ArrayList<java.util.Map<String, Object>>();
                for (final var entry : byDestination.entrySet()) {
                    var valCount = 0;
                    var errCount = 0;
                    for (final var e : entry.getValue()) {
                        if (e.containsKey("error")) {
                            errCount++;
                        } else {
                            valCount++;
                        }
                    }
                    final var row = new java.util.HashMap<String, Object>();
                    row.put("name", entry.getKey());
                    row.put("values", valCount);
                    row.put("errors", errCount);
                    destSummary.add(row);
                }
                destSummary.sort(java.util.Comparator.comparing(r -> (String) r.get("name")));
                request.setAttribute("destSummary", destSummary);
                request.setAttribute("totalValues", totalValues);
                request.setAttribute("totalErrors", totalErrors);
                request.setAttribute("totalDests", byDestination.size());
            }
        } catch (final Exception e) {
            _log.warn("BulkImportDestinationMetaDataAction", e);
            request.setAttribute("importError", e.getMessage());
        }

        return mapping.findForward("success");
    }
}
