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

package ecmwf.ecpds.master.plugin.http.controller.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.datafile.RatesHome;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class RatesAction.
 */
public class RatesAction extends PDSAction {

    /**
     * Safe authorized perform.
     *
     * @param mapping
     *            the mapping
     * @param form
     *            the form
     * @param request
     *            the request
     * @param response
     *            the response
     * @param user
     *            the user
     *
     * @return the action forward
     *
     * @throws ECMWFException
     *             the ECMWF exception
     * @throws ClassCastException
     *             the class cast exception
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var raf = (RatesActionForm) form;
        if ((raf.getFromDate() == null) || (raf.getFromTime() == null) || (raf.getToDate() == null)
                || (raf.getToTime() == null) || (raf.getTransferServerName() == null)
                || (raf.getPerTransferServer() == null)) {
            return mapping.findForward("form");
        }
        try {
            final var dateFormat = "yyyy-MM-dd HH:mm:ss";
            final var fromDate = new Date(Format.toTime(dateFormat, raf.getFromDate() + " " + raf.getFromTime()));
            final var toDate = new Date(Format.toTime(dateFormat, raf.getToDate() + " " + raf.getToTime()));
            final var fromTime = new Date(Format.toTime(dateFormat, raf.getFromDate() + " " + raf.getFromTime()));
            final var toTime = new Date(Format.toTime(dateFormat, raf.getFromDate() + " " + raf.getToTime()));
            if (toDate.before(fromDate) || toTime.before(fromTime)) {
                throw new Exception("Please select a correct date and time range!");
            }
            final var caller = raf.getCaller();
            final var sourceHost = raf.getSourceHost();
            if (!raf.getPerTransferServerBoolean()) {
                request.setAttribute("option", "rates");
                request.setAttribute("caller", "*".equals(caller) ? "All" : caller);
                request.setAttribute("sourceHost", "*".equals(sourceHost) ? "All" : sourceHost);
                request.setAttribute("ratesList", RatesHome.findByDates(fromDate, toDate, false, caller, sourceHost));
            } else {
                final var transferServerName = raf.getTransferServerName();
                if ("All".equals(transferServerName)) {
                    request.setAttribute("option", "ratesPerTransferServer");
                    request.setAttribute("caller", "*".equals(caller) ? "All" : caller);
                    request.setAttribute("sourceHost", "*".equals(sourceHost) ? "All" : sourceHost);
                    request.setAttribute("ratesList",
                            RatesHome.findByDates(fromDate, toDate, true, caller, sourceHost));
                } else {
                    request.setAttribute("option", "ratesPerFileSystem");
                    request.setAttribute("caller", "*".equals(caller) ? "All" : caller);
                    request.setAttribute("sourceHost", "*".equals(sourceHost) ? "All" : sourceHost);
                    request.setAttribute("transferServerName", transferServerName);
                    request.setAttribute("ratesList",
                            RatesHome.findByDates(fromDate, toDate, transferServerName, caller, sourceHost));
                }
            }
        } catch (final Exception e) {
            throw new ECMWFActionFormException(e.getMessage(), e);
        }
        return mapping.findForward("success");
    }
}
