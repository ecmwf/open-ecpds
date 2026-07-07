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
            if (c.size() != 2) {
                throw new ECMWFActionFormException("Unsupported number of parameters " + c);
            }
            final var operation = i.next().toString();
            final var hostId = i.next().toString();
            final var host = HostHome.findByPrimaryKey(hostId);
            if ("view".equalsIgnoreCase(operation)) {
                request.setAttribute("host", host);
                try {
                    request.setAttribute("hostPropErrors", GetHostListJsonAction.hasPropertyErrors(host));
                } catch (final Exception ignored) {
                }
                try {
                    final var ecmName = host.getTransferMethod().getEcTransModuleName();
                    if (ecmName != null) {
                        setGuideIfPresent("/WEB-INF/jsp/pds/transfer/module/guide/" + ecmName + ".jsp", request);
                    }
                } catch (final Exception ignored) {
                }
                // For Acquisition hosts, check whether at least one associated destination is
                // eligible for the scheduler — same logic as GetHostAction.
                if (ecmwf.ecpds.master.transfer.HostOption.ACQUISITION.equals(host.getType())) {
                    try {
                        final var reasons = new java.util.ArrayList<String>();
                        var canRun = false;
                        for (final var dest : host.getDestinations()) {
                            if (!dest.getActive()) {
                                reasons.add("<strong>" + dest.getName() + "</strong> is disabled");
                            } else if (!dest.getAcquisition()) {
                                reasons.add("<strong>" + dest.getName() + "</strong> has acquisition disabled");
                            } else {
                                final var sc = dest.getStatusCode();
                                if ("STOP".equals(sc) || "INIT".equals(sc) || "FAIL".equals(sc)) {
                                    final var label = "STOP".equals(sc) ? "stopped"
                                            : "INIT".equals(sc) ? "not yet started" : "failed";
                                    reasons.add("<strong>" + dest.getName() + "</strong> is " + label);
                                } else {
                                    canRun = true;
                                    break;
                                }
                            }
                        }
                        if (!canRun && !reasons.isEmpty()) {
                            final var note = new StringBuilder();
                            if (reasons.size() == 1) {
                                note.append(reasons.get(0)).append(" &mdash; start it to allow the listing to run");
                            } else {
                                note.append("none of the ").append(reasons.size())
                                        .append(" associated destinations is eligible: ");
                                for (var k = 0; k < reasons.size(); k++) {
                                    if (k > 0)
                                        note.append(", ");
                                    note.append(reasons.get(k));
                                }
                                note.append(" &mdash; start at least one to allow the listing to run");
                            }
                            request.setAttribute("acquisitionNote", note.toString());
                        }
                    } catch (final Exception ignored) {
                    }
                }
                return mapping.findForward("success");
            } else if ("load".equalsIgnoreCase(operation)) {
                // TODO: if in progress then should display formattedLastOutput instead!
                final var out = response.getOutputStream();
                try (var in = new GZIPInputStream(new LocalInputStream(host.getOutput(user)));
                        var scanner = new Scanner(in)) {
                    scanner.useDelimiter("\n");
                    while (scanner.hasNext()) {
                        out.write(Util.getFormatted(user, scanner.next() + "\n").getBytes());
                    }
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
