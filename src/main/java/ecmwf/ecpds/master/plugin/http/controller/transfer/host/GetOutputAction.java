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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.callback.LocalInputStream;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetOutputAction.
 */
public class GetOutputAction extends PDSAction {

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
     * @throws TransferException
     *             the transfer exception
     * @throws ECMWFActionFormException
     *             the ECMWF action form exception
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws TransferException, ECMWFActionFormException {
        final ArrayList<?> c = ECMWFActionForm.getPathParameters(mapping, request);
        final Iterator<?> i = c.iterator();
        try {
            if (c.size() != 2) {
                throw new ECMWFActionFormException("Unsupported number of parameters " + c);
            }
            final var operation = i.next().toString();
            final var hostId = i.next().toString();
            final var host = HostHome.findByPrimaryKey(hostId);
            if ("view".equalsIgnoreCase(operation)) {
                request.setAttribute("host", host);
                return mapping.findForward("success");
            } else if ("load".equalsIgnoreCase(operation)) {
                // TODO: if in progress then should display formattedLastOutput instead!
                final OutputStream out = response.getOutputStream();
                final InputStream in = new GZIPInputStream(new LocalInputStream(host.getOutput(user)));
                final var scanner = new Scanner(in);
                scanner.useDelimiter("\n");
                try {
                    while (scanner.hasNext()) {
                        out.write(Util.getFormatted(user, scanner.next() + "\n").getBytes());
                    }
                } finally {
                    scanner.close();
                    in.close();
                }
                return null;
            } else {
                throw new ECMWFActionFormException("Unsupported operation " + operation);
            }
        } catch (final Throwable t) {
            throw new ECMWFActionFormException(Format.getMessage(t));
        }
    }
}
