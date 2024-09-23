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

package ecmwf.ecpds.master.plugin.http.controller.transfer.host;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.technical.StreamManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferGroupHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class GetHostAction.
 */
public class GetHostAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(GetHostAction.class);

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var parameters = ECMWFActionForm.getPathParameters(mapping, request);
        if (parameters.isEmpty()) {
            final var label = Util.getValue(request, "label", "All");
            final var filter = Util.getValue(request, "hostFilter", "All");
            final var network = Util.getValue(request, "network", "All");
            final var hostType = Util.getValue(request, "hostType", "All");
            final var hostSearch = Util.getValue(request, "hostSearch", "");
            // Initialize the cursor for the database search
            final var cursor = Util.getDataBaseCursor("host", 25, 0, true, request);
            Collection<Host> hosts;
            try {
                hosts = HostHome.findByCriteria(label, filter, network, hostType, hostSearch, cursor);
                request.setAttribute("getHostsError", "");
            } catch (final TransferException e) {
                request.setAttribute("getHostsError", e.getMessage());
                hosts = new ArrayList<>(0);
            }
            request.setAttribute("typeOptions", getTypeOptions());
            request.setAttribute("networkOptions", getNetworkOptions());
            request.setAttribute("labelOptions", getLabelOptions());
            request.setAttribute("filterOptions", getFilterOptions());
            request.setAttribute("hostsSize", Util.getCollectionSizeFrom(hosts));
            request.setAttribute("hosts", hosts);
            request.setAttribute("hasHostSearch", hostSearch != null && !hostSearch.isBlank());
            return mapping.findForward("success");
        }
        final var host = HostHome.findByPrimaryKey(parameters.get(0).toString());
        // Let's pass the user for the getLastOutput links!
        host.setUser(user);
        request.setAttribute("host", host);
        final var mode = request.getParameter("mode");
        if ("changelog".equals(mode)) {
            // This is the changelog.jsp page!
            return mapping.findForward("changelog");
        } else {
            // This is the main Host page!
            return mapping.findForward("success");
        }
    }

    /**
     * Gets the type options.
     *
     * @return the type options
     */
    private static final Collection<Pair> getTypeOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Types"));
        for (final String element : HostOption.type) {
            options.add(new Pair(element, element));
        }
        return options;
    }

    /**
     * Gets the label options.
     *
     * @return the label options
     */
    public static final Collection<Pair> getLabelOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Labels"));
        for (var i = 0; i < HostOption.networkCode.length; i++) {
            options.add(new Pair(HostOption.networkCode[i], HostOption.networkName[i]));
        }
        return options;
    }

    /**
     * Gets the filter options.
     *
     * @return the filter options
     */
    public static final Collection<Pair> getFilterOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Compressions"));
        for (final String mode : StreamManager.modes) {
            options.add(new Pair(mode, mode));
        }
        return options;
    }

    /**
     * Gets the network options.
     *
     * @return the network options
     */
    private static final Collection<Pair> getNetworkOptions() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Networks"));
        try {
            for (final TransferGroup group : TransferGroupHome.findAll()) {
                options.add(new Pair(group.getName(), group.getName()));
            }
        } catch (final DataFileException e) {
            log.error("Problem getting TransferGroups", e);
        }
        return options;
    }
}
