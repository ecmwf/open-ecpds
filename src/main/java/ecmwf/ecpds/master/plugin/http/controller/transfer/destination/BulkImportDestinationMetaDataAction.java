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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.Element;

import ecmwf.common.database.DestinationMetaField;
import ecmwf.common.database.DestinationMetaValue;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * Bulk-import XML metadata files for all destinations at once.
 *
 * <p>
 * GET without {@code confirm=true}: scans every per-destination sub-directory under the attachments root, parses any
 * {@code *.xml} files found, and returns a grouped preview.
 *
 * <p>
 * GET with {@code confirm=true}: commits all parsed values to the DB.
 */
public class BulkImportDestinationMetaDataAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(BulkImportDestinationMetaDataAction.class);

    /** Mirrors the tag-to-field mapping in {@link ImportDestinationMetaDataAction}. */
    private static final Map<String, String> TAG_TO_FIELD;

    static {
        TAG_TO_FIELD = new HashMap<>();
        TAG_TO_FIELD.put("organisationWebPage", "organisationWebPage");
        TAG_TO_FIELD.put("SADNumber", "SADNumber");
        TAG_TO_FIELD.put("contractId", "contractId");
        TAG_TO_FIELD.put("generalComments", "generalComments");
        TAG_TO_FIELD.put("disseminationChartsComments", "disseminationChartsComments");
        TAG_TO_FIELD.put("agency", "agency");
        TAG_TO_FIELD.put("centreOfOrigin", "centreOfOrigin");
        TAG_TO_FIELD.put("agencyWebPage", "agencyWebPage");
        TAG_TO_FIELD.put("sadNumber", "sadNumber");
        TAG_TO_FIELD.put("dataFormat", "dataFormat");
        TAG_TO_FIELD.put("typeOfObservation", "typeOfObservation");
        TAG_TO_FIELD.put("importanceOfDataTypeForAssimilation", "importanceForAssimilation");
        TAG_TO_FIELD.put("ECFSPath", "ecfsPath");
        TAG_TO_FIELD.put("OnLineBackup", "onLineBackup");
        TAG_TO_FIELD.put("warningInfo", "warningInfo");
        TAG_TO_FIELD.put("WarningInfo", "warningInfo");
        TAG_TO_FIELD.put("comments", "comments");
    }

    /** {@inheritDoc} */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {

        final var confirm = "true".equalsIgnoreCase(request.getParameter("confirm"));
        final var attachmentsDir = Cnf.at("Server", "attachments", "/tmp/ecpds-attachments");
        final var root = new File(attachmentsDir);

        // destinationName -> list of field entry maps (same structure as single import)
        final Map<String, List<Map<String, Object>>> byDestination = new LinkedHashMap<>();
        int totalValues = 0;
        int totalErrors = 0;

        try {
            final var db = MasterManager.getDB();

            // Build field name -> id map
            final var fieldMap = new HashMap<String, Integer>();
            for (final DestinationMetaField f : db.getDestinationMetaFields()) {
                fieldMap.put(f.getName(), f.getId());
            }

            if (root.exists() && root.isDirectory()) {
                final var destDirs = root.listFiles(File::isDirectory);
                if (destDirs != null) {
                    for (final File destDir : destDirs) {
                        final var destName = destDir.getName();
                        final var xmlFiles = destDir.listFiles(f -> f.getName().endsWith(".xml"));
                        if (xmlFiles == null || xmlFiles.length == 0) {
                            continue;
                        }
                        final var entries = new ArrayList<Map<String, Object>>();
                        for (final File xmlFile : xmlFiles) {
                            try {
                                final var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                                        .parse(xmlFile);
                                doc.getDocumentElement().normalize();
                                extractValues(doc.getDocumentElement(), fieldMap, entries);
                            } catch (final Exception e) {
                                _log.warn("Failed to parse {} for destination {}", xmlFile, destName, e);
                                final var err = new HashMap<String, Object>();
                                err.put("file", xmlFile.getName());
                                err.put("error", e.getMessage() != null ? e.getMessage() : "parse error");
                                entries.add(err);
                                totalErrors++;
                            }
                        }
                        if (!entries.isEmpty()) {
                            byDestination.put(destName, entries);
                            totalValues += entries.stream().filter(e -> !e.containsKey("error")).count();
                        }
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
                        if (pos instanceof Integer) {
                            v.setPosition((Integer) pos);
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
            } else {
                request.setAttribute("byDestination", byDestination);
                request.setAttribute("totalValues", totalValues);
                request.setAttribute("totalErrors", totalErrors);
            }
        } catch (final Exception e) {
            _log.warn("BulkImportDestinationMetaDataAction", e);
            request.setAttribute("importError", e.getMessage());
        }

        return mapping.findForward("success");
    }

    private void extractValues(final Element element, final Map<String, Integer> fieldMap,
            final List<Map<String, Object>> entries) {
        final var children = element.getChildNodes();
        for (var i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element child)) {
                continue;
            }
            final var tagName = child.getLocalName() != null ? child.getLocalName() : child.getTagName();
            final var fieldName = TAG_TO_FIELD.get(tagName);
            if (fieldName != null && fieldMap.containsKey(fieldName)) {
                final var text = child.getTextContent();
                if (text != null && !text.isBlank()) {
                    final var entry = new HashMap<String, Object>();
                    entry.put("fieldName", fieldName);
                    entry.put("fieldId", fieldMap.get(fieldName));
                    entry.put("value", text.trim());
                    entry.put("position", 0);
                    entries.add(entry);
                }
            } else {
                extractValues(child, fieldMap, entries);
            }
        }
    }
}
