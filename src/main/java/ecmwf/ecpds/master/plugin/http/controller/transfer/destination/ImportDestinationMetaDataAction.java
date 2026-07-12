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
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * Import XML metadata files into the destination metadata DB. GET: shows preview/import form GET with confirm=true:
 * commits import
 */
public class ImportDestinationMetaDataAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ImportDestinationMetaDataAction.class);

    /** Map from XML element/tag name to DMF_NAME. */
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
        final var attachmentsDir = Cnf.at("Server", "attachments", "/tmp/ecpds-attachments");
        final var dir = new File(attachmentsDir + File.separator + destinationName);

        final List<Map<String, Object>> preview = new ArrayList<>();
        try {
            final var db = MasterManager.getDB();
            // Build field name -> id map
            final var fieldMap = new HashMap<String, Integer>();
            for (final DestinationMetaField f : db.getDestinationMetaFields()) {
                fieldMap.put(f.getName(), f.getId());
            }

            if (dir.exists()) {
                final var xmlFiles = dir.listFiles(f -> f.getName().endsWith(".xml"));
                if (xmlFiles != null) {
                    for (final File xmlFile : xmlFiles) {
                        try {
                            final var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
                            doc.getDocumentElement().normalize();
                            extractValues(doc.getDocumentElement(), fieldMap, preview);
                        } catch (final Exception e) {
                            _log.warn("Failed to parse {}", xmlFile, e);
                            final var entry = new HashMap<String, Object>();
                            entry.put("file", xmlFile.getName());
                            entry.put("error", e.getMessage() != null ? e.getMessage() : "parse error");
                            preview.add(entry);
                        }
                    }
                }
            }

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
                    if (pos instanceof Integer) {
                        v.setPosition((Integer) pos);
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

    private void extractValues(final Element element, final Map<String, Integer> fieldMap,
            final List<Map<String, Object>> preview) {
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
                    preview.add(entry);
                }
            } else {
                // Recurse for complex types
                extractValues(child, fieldMap, preview);
            }
        }
    }
}
