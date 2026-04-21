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
        try {
            String format = request.getParameter("format");
            boolean isDataRequest = "data".equalsIgnoreCase(format);
            ArrayList<?> c = ECMWFActionForm.getPathParameters(mapping, request);
            Iterator<?> i = c.iterator();

            // =========================================================
            // 1. DATA MODE (replacement for GetReportDataAction)
            // =========================================================
            if (isDataRequest) {
                final String report;
                if (c.size() == 1) {
                    String hostId = i.next().toString();
                    var host = HostHome.findByPrimaryKey(hostId);
                    report = host.getReport(user);
                } else if (c.size() == 2) {
                    String hostId = i.next().toString();
                    String proxyId = i.next().toString();
                    var host = HostHome.findByPrimaryKey(hostId);
                    var proxy = HostHome.findByPrimaryKey(proxyId);
                    report = host.getReport(user, proxy);
                } else {
                    throw new ECMWFActionFormException("Unsupported number of parameters " + c);
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(report != null ? report : "");
                response.getWriter().flush();
                return null;
            }

            // =========================================================
            // 2. HTML/UI MODE
            // =========================================================
            if (c.size() == 1) {
                String hostId = i.next().toString();
                var host = HostHome.findByPrimaryKey(hostId);
                request.setAttribute("host", host);
                request.setAttribute("reportDataUrl", "/do/transfer/host/edit/getReport/" + hostId + "?format=data");
                return mapping.findForward("success");
            }

            if (c.size() == 2) {
                String hostId = i.next().toString();
                String proxyId = i.next().toString();
                var host = HostHome.findByPrimaryKey(hostId);
                var proxy = HostHome.findByPrimaryKey(proxyId);
                request.setAttribute("host", host);
                request.setAttribute("proxy", proxy);
                request.setAttribute("reportDataUrl",
                        "/do/transfer/host/edit/getReport/" + hostId + "/" + proxyId + "?format=data");
                return mapping.findForward("success");
            }

            throw new ECMWFActionFormException("Unsupported number of parameters " + c);

        } catch (Throwable t) {
            throw new ECMWFActionFormException(Format.getMessage(t));
        }
    }
}