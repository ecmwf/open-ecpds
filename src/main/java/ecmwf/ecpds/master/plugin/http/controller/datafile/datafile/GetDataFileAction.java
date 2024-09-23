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

package ecmwf.ecpds.master.plugin.http.controller.datafile.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.DataFileHome;
import ecmwf.ecpds.master.plugin.http.home.datafile.MetaDataHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.MetaData;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetDataFileAction.
 */
public class GetDataFileAction extends PDSAction {

    /** The Constant DAYS_BACK. */
    private static final int DAYS_BACK = 7;

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final ArrayList<?> parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (parameters.isEmpty()) {
            final var date = Util.getValue(request, "date", getISOFormat().format(new Date()));
            final var metadataNames = getMetaDataNames();
            final var originalMetadataName = (String) request.getSession().getAttribute("metaDataName");
            final var metaDataName = Util.getValue(request, "metaDataName", () -> {
                final var metaData = getFirstMetadata(metadataNames);
                return metaData != null ? metaData.getName() : "";
            });
            final var metadataValues = getMetaDataValues(metaDataName);
            final String metaDataValue;
            if (!metaDataName.equals(originalMetadataName)) {
                final var metaData = getFirstMetadata(metadataValues);
                metaDataValue = metaData != null ? metaData.getValue() : "";
            } else {
                metaDataValue = Util.getValue(request, "metaDataValue", () -> {
                    final var metaData = getFirstMetadata(metadataValues);
                    return metaData != null ? metaData.getValue() : "";
                });
            }
            // Initialize the cursor for the database search
            final var cursor = Util.getDataBaseCursor("datafile", 25, 2, true, request);
            final Collection<DataFile> datafiles;
            try {
                datafiles = DataFileHome.findByMetaDataAndDate(metaDataName, metaDataValue, getISOFormat().parse(date),
                        cursor);
            } catch (final ParseException e) {
                throw new ECMWFActionFormException("Error parsing date", e);
            }
            // And now save the filter data stream and pass the filter options.
            request.setAttribute("selectedDate", date);
            request.setAttribute("dateOptions", getDateOptions(DAYS_BACK, false));
            request.setAttribute("datafileList", datafiles);
            request.setAttribute("datafileListSize", Util.getCollectionSizeFrom(datafiles));
            request.setAttribute("selectedMetaDataName", metaDataName);
            request.setAttribute("selectedMetaDataValue", metaDataValue);
            request.setAttribute("metaDataNameOptions", metadataNames);
            request.setAttribute("metaDataValueOptions", metadataValues);
        } else {
            final var df = DataFileHome.findByPrimaryKey(parameters.get(0).toString());
            // To allow setting the links in the comments!
            df.setUser(user);
            request.setAttribute("datafile", df);
        }
        return mapping.findForward("success");
    }

    /**
     * Gets the meta data names.
     *
     * @return the meta data names
     */
    private static Collection<MetaData> getMetaDataNames() {
        try {
            return MetaDataHome.findAllMetaDataNames();
        } catch (final Throwable t) {
        }
        return new ArrayList<>();
    }

    /**
     * Gets the meta data values.
     *
     * @param metaDataName
     *            the meta data name
     *
     * @return the meta data values
     */
    private static Collection<MetaData> getMetaDataValues(final String metaDataName) {
        if (!"".equals(metaDataName)) {
            try {
                return MetaDataHome.findByAttributeName(metaDataName);
            } catch (final Throwable t) {
            }
        }
        return new ArrayList<>();
    }

    /**
     * Gets the first metadata.
     *
     * @param metadataCollection
     *            the metadata collection
     *
     * @return the first metadata
     */
    private static MetaData getFirstMetadata(final Collection<MetaData> metadataCollection) {
        try {
            if (!metadataCollection.isEmpty()) {
                return metadataCollection.iterator().next();
            }
        } catch (final Throwable t) {
        }
        return null;
    }
}
