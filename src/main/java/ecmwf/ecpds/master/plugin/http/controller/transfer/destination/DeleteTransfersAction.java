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

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.technical.ExecutorRepository;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class DeleteTransfersAction.
 */
public class DeleteTransfersAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DeleteTransfersAction.class);

    /** The Constant DELETE_TRANSFER. */
    private static final String DELETE_TRANSFER = "deleteTransfer";

    /** The Constant DELETE_TRANSFER_FORM. */
    private static final String DELETE_TRANSFER_FORM = "deleteTransferForm";

    /** The Constant DELETE. */
    private static final String DELETE = "delete";

    /** The Constant SCHEDULE_NOW. */
    private static final String SCHEDULE_NOW = "scheduleNow";

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
        if (this.isCancelled(request)) {
            return mapping.findForward("cancel");
        }
        final Collection<?> c = ECMWFActionForm.getPathParameters(mapping, request);
        if (c.size() == 2) {
            // Status change for the set of selected transfers
            final var daf = (DetailActionForm) form;
            final Iterator<?> i = c.iterator();
            final var destination = DestinationHome.findByPrimaryKey(i.next().toString());
            final var subAction = i.next().toString();
            daf.setIsMemberState(!user.hasAccess(getResource(request, "nonmemberstate.basepath")));
            request.setAttribute("destination", destination);
            return executeSubActionOnSelectedDataTransfers(request, mapping, destination, subAction, daf, user);
        } else if (c.size() == 3) {
            // Status/association changes for some individual transfers or
            // hosts
            final var daf = (DetailActionForm) form;
            final Iterator<?> i = c.iterator();
            final var destination = DestinationHome.findByPrimaryKey(i.next().toString());
            final var subAction = i.next().toString();
            final var subActionParameter = i.next().toString();
            daf.setIsMemberState(!user.hasAccess(getResource(request, "nonmemberstate.basepath")));
            request.setAttribute("destination", destination);
            return addDisplayTagParams(executeSubActionOnDataTransferOrHost(request, mapping, destination, subAction,
                    subActionParameter, user), daf.getDisplayTagsParamCollection());
        } else {
            // There must be a problem with struts-conf.xml. We never should
            // have other than 1, 2 or 3 parameters to match this Action.
            throw new ECMWFActionFormException("Expected 2 or 3 path parameters. Got " + c.size());
        }
    }

    /**
     * Execute an action on some specific data transfer.
     *
     * @param request
     *            the request
     * @param mapping
     *            the mapping
     * @param d
     *            the d
     * @param subAction
     *            the sub action
     * @param subActionParameter
     *            the sub action parameter
     * @param u
     *            the u
     *
     * @return Where to forward after this.
     *
     * @throws ECMWFException
     *             the ECMWF exception
     */
    private ActionForward executeSubActionOnDataTransferOrHost(final HttpServletRequest request,
            final ActionMapping mapping, final Destination d, final String subAction, final String subActionParameter,
            final User u) throws ECMWFException {
        if (DELETE_TRANSFER_FORM.equals(subAction)) {
            request.setAttribute("datatransfer", getValidatedDataTransfer(subActionParameter, d, u));
            return mapping.findForward("delete_transfer_form");
        }
        if (DELETE_TRANSFER.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.delete(u);
        } else if (SCHEDULE_NOW.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.scheduleNow(u);
        } else {
            throw new ECMWFException(
                    "The subAction '" + subAction + "' is not defined for class " + this.getClass().getName());
        }
        final var from = request.getParameter("from");
        if ("selection".equals(from)) {
            // This was called from the selection screen
            return mapping.findForward("validate.redir");
        }
        // This was called from the detail screen
        return mapping.findForward("detail");
    }

    /**
     * Execute an action on all the selected data transfers.
     *
     * @param request
     *            the request
     * @param mapping
     *            the mapping
     * @param d
     *            the d
     * @param subAction
     *            the sub action
     * @param form
     *            the form
     * @param u
     *            the u
     *
     * @return the action forward
     *
     * @throws ECMWFActionFormException
     *             the ECMWF action form exception
     */
    private ActionForward executeSubActionOnSelectedDataTransfers(final HttpServletRequest request,
            final ActionMapping mapping, final Destination d, final String subAction, final DetailActionForm form,
            final User u) throws ECMWFActionFormException {
        final List<Exception> exceptions = Collections.synchronizedList(new ArrayList<Exception>());
        final var processedCount = new AtomicLong(0);
        final var startTime = System.currentTimeMillis();
        final ActionForward forward;
        if (!DELETE.equals(subAction)) {
            throw new ECMWFActionFormException(
                    "The subAction '" + subAction + "' is not defined for class " + this.getClass().getName());
        }
        new ExecutorRepository<>(exceptions, processedCount, form.getActionTransfers()) {
            @Override
            public void exec(final DataTransfer transfer) throws Exception {
                form.deleteFromSelections(transfer);
                transfer.delete(u);
            }
        };
        form.cleanActionTransfers();
        forward = mapping.findForward("validate.redir");
        if (processedCount.get() > 0) {
            form.setMessage("Action '" + subAction + "' performed on " + processedCount.get() + " Data Transfer(s) in "
                    + Format.formatDuration(startTime, System.currentTimeMillis()));
        }
        if (!exceptions.isEmpty()) {
            form.setMessagesFromExceptions(exceptions);
        }
        return forward;
    }

    /**
     * Adds the display tag params.
     *
     * @param fw
     *            the fw
     * @param params
     *            the params
     *
     * @return the action forward
     */
    private static final ActionForward addDisplayTagParams(final ActionForward fw, final Collection<Pair> params) {
        if (params == null || fw.getPath().startsWith(".pds.")) {
            return fw;
        }
        final var out = new StringBuilder();
        for (Pair p : params) {
            out.append(p.getName() + "=" + p.getValue() + "&");
        }
        return new ActionForward(fw.getPath() + "?" + out.toString(), fw.getRedirect());
    }

    /**
     * Gets the validated data transfer.
     *
     * @param id
     *            the id
     * @param d
     *            the d
     * @param u
     *            the u
     *
     * @return the validated data transfer
     *
     * @throws TransferException
     *             the transfer exception
     */
    private static final DataTransfer getValidatedDataTransfer(final String id, final Destination d, final User u)
            throws TransferException {
        final var transfer = DataTransferHome.findByPrimaryKey(id);
        if (transfer.getDestinationName().equals(d.getName())) {
            return transfer;
        }
        log.warn("Data transfer '" + id + "' doesn't belong to destination '" + d.getName() + "', but to destination '"
                + transfer.getDestinationName() + "'. Possible cheating attempt, playing with the URLS?. User is '"
                + u.getId() + "'");
        throw new TransferException("Data transfer '" + id + "' doesn't belong to destination '" + d.getName()
                + "'. Cannot perform operation.");
    }
}
