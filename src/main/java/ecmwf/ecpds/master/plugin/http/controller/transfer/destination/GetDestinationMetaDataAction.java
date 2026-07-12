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
import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.transfer.DestinationMetaData;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDestinationMetaDataAction.
 *
 * @author root
 */
public class GetDestinationMetaDataAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException, TransferException, MonitoringException {
        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        if (params.size() == 1) {
            final var id = params.get(0).toString();
            final var destination = DestinationHome.findByPrimaryKey(id);
            request.setAttribute("destination", destination);
            try {
                request.setAttribute("destPropErrors", GetDestinationListJsonAction.hasPropertyErrors(destination));
            } catch (final Exception ignored) {
            }
            // Load metadata fields (filter by destination DES_TYPE via junction table) and existing values
            try {
                final var db = MasterManager.getDB();
                final var allFields = db.getDestinationMetaFields();
                final var typeMap = db.getDestinationMetaFieldTypeMap();
                final int desType = destination.getType();
                final var fields = new ArrayList<ecmwf.common.database.DestinationMetaField>();
                for (final var f : allFields) {
                    final var allowed = typeMap.get(f.getId());
                    // No rows in junction table → applies to all types
                    if (allowed == null || allowed.isEmpty() || allowed.contains(desType)) {
                        fields.add(f);
                    }
                }
                request.setAttribute("metaFields", fields);
                request.setAttribute("metaValues", db.getDestinationMetaValuesByDestination(id));
            } catch (final Exception e) {
                request.setAttribute("metaFields", Collections.emptyList());
                request.setAttribute("metaValues", Collections.emptyList());
            }
            request.setAttribute("products", ProductStatusHome.findAllProductNameTimePairs());
            request.setAttribute("currentDate", new Date());
            return mapping.findForward("success");
        }
        if (params.size() == 2) {
            final var id = params.get(0).toString();
            final var destination = DestinationHome.findByPrimaryKey(id);
            request.setAttribute("destination", destination);
            try {
                request.setAttribute("destPropErrors", GetDestinationListJsonAction.hasPropertyErrors(destination));
            } catch (final Exception ignored) {
            }
            final var file = params.get(1).toString();
            final var c = destination.getMetaData();
            for (DestinationMetaData m : c) {
                if (m.getName().equals(file)) {
                    request.setAttribute("content", m);
                }
            }
            return mapping.findForward("file");
        } else {
            throw new ECMWFActionFormException("Unsupported number of parameters: " + params);
        }
    }
}
