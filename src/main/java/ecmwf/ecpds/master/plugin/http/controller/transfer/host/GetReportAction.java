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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetReportAction.
 */
public class GetReportAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws TransferException, ECMWFActionFormException {
        final ArrayList<?> c = ECMWFActionForm.getPathParameters(mapping, request);
        final Iterator<?> i = c.iterator();
        try {
            if (c.size() == 1) {
                final var hostId = i.next().toString();
                final var host = HostHome.findByPrimaryKey(hostId);
                request.setAttribute("message", host.getReport(user));
                request.setAttribute("host", host);
                return mapping.findForward("success");
            }
            if (c.size() == 2) {
                final var hostId = i.next().toString();
                final var proxyId = i.next().toString();
                final var host = HostHome.findByPrimaryKey(hostId);
                final var proxy = HostHome.findByPrimaryKey(proxyId);
                request.setAttribute("message", host.getReport(user, proxy));
                request.setAttribute("host", host);
                request.setAttribute("proxy", proxy);
                return mapping.findForward("success");
            } else {
                throw new ECMWFActionFormException("Unsupported number of parameters " + c);
            }
        } catch (final Throwable t) {
            throw new ECMWFActionFormException(Format.getMessage(t));
        }
    }
}
