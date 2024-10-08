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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Update data transfers or the the status within the destination, but not the
 * destination itself or its associations with other objects
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.timer.Timer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.technical.ExecutorRepository;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class OperationsAction.
 */
public class OperationsAction extends PDSAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(OperationsAction.class);

    /** The Constant VALIDATE. */
    private static final String VALIDATE = "validate";

    /** The Constant SELECT_FILTERED. */
    private static final String SELECT_FILTERED = "selectFiltered";

    /** The Constant DEACTIVATE_HOST. */
    private static final String DEACTIVATE_HOST = "deactivateHost";

    /** The Constant ACTIVATE_HOST. */
    private static final String ACTIVATE_HOST = "activateHost";

    /** The Constant INCREASE_HOST_PRIORITY. */
    private static final String INCREASE_HOST_PRIORITY = "increaseHostPriority";

    /** The Constant DECREASE_HOST_PRIORITY. */
    private static final String DECREASE_HOST_PRIORITY = "decreaseHostPriority";

    /** The Constant DUPLICATE_HOST. */
    private static final String DUPLICATE_HOST = "duplicateHost";

    /** The Constant GRACEFUL_RESTART. */
    private static final String GRACEFUL_RESTART = "gracefulRestart";

    /** The Constant IMMEDIATE_RESTART. */
    private static final String IMMEDIATE_RESTART = "immediateRestart";

    /** The Constant IMMEDIATE_PUT_ON_HOLD. */
    private static final String IMMEDIATE_PUT_ON_HOLD = "immediatePutOnHold";

    /** The Constant GRACEFUL_PUT_ON_HOLD. */
    private static final String GRACEFUL_PUT_ON_HOLD = "gracefulPutOnHold";

    /** The Constant CLEAN_DESTINATION. */
    private static final String CLEAN_DESTINATION = "cleanDestination";

    /** The Constant CLEAN_EXPIRED_DESTINATION. */
    private static final String CLEAN_EXPIRED_DESTINATION = "cleanExpiredDestination";

    /** The Constant REQUEUE. */
    private static final String REQUEUE = "requeue";

    /** The Constant INCREASE_TRANSFER_PRIORITY. */
    private static final String INCREASE_TRANSFER_PRIORITY = "increaseTransferPriority";

    /** The Constant DECREASE_TRANSFER_PRIORITY. */
    private static final String DECREASE_TRANSFER_PRIORITY = "decreaseTransferPriority";

    /** The Constant SET_TRANSFER_PRIORITY. */
    private static final String SET_TRANSFER_PRIORITY = "setTransferPriority";

    /** The Constant DOWNLOAD. */
    private static final String DOWNLOAD = "download";

    /** The Constant INTERRUPT. */
    private static final String INTERRUPT = "interrupt";

    /** The Constant EXTEND_LIFETIME. */
    private static final String EXTEND_LIFETIME = "extendLifetime";

    /** The Constant SCHEDULE_NOW. */
    private static final String SCHEDULE_NOW = "scheduleNow";

    /** The Constant STOP. */
    private static final String STOP = "stop";

    /** The Constant CANCEL. */
    private static final String CANCEL = "cancel";

    /** The Constant CLEAN. */
    private static final String CLEAN = "clean";

    /** The Constant TRANSFER_PRIORITY_STEP. */
    private static final int TRANSFER_PRIORITY_STEP = 1;

    /** The Constant HOST_PRIORITY_STEP. */
    private static final int HOST_PRIORITY_STEP = 1;

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
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
            // Status/association changes for some individual transfer or
            // host
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
        if (VALIDATE.equals(subAction)) {
            // Just changed to the screen without performing an action
            return mapping.findForward("validate");
            // DATA TRANSFER THINGIES
        }
        if (EXTEND_LIFETIME.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.setExpiryDate(new Date(transfer.getExpiryDate().getTime() + Timer.ONE_DAY));
            transfer.save(u);
        } else if (INCREASE_TRANSFER_PRIORITY.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.setPriority(transfer.getPriority() - TRANSFER_PRIORITY_STEP, u);
            transfer.save(u);
        } else if (DECREASE_TRANSFER_PRIORITY.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.setPriority(transfer.getPriority() + TRANSFER_PRIORITY_STEP, u);
            transfer.save(u);
        } else if (REQUEUE.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.requeue(u);
        } else if (STOP.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.stop(u);
        } else if (DOWNLOAD.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            request.setAttribute("content", d.getTransferContent(transfer, u));
            return mapping.findForward("download");
        } else if (SCHEDULE_NOW.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.scheduleNow(u);
            // HOST THINGIES
        } else if (INTERRUPT.equals(subAction)) {
            final var transfer = getValidatedDataTransfer(subActionParameter, d, u);
            transfer.interruptRetrieval(u);
        } else if (DEACTIVATE_HOST.equals(subAction)) {
            final var h = HostHome.findByPrimaryKey(subActionParameter);
            h.setActive(false);
            h.save(u);
            return mapping.findForward("detail");
        } else if (ACTIVATE_HOST.equals(subAction)) {
            final var h = HostHome.findByPrimaryKey(subActionParameter);
            h.setActive(true);
            h.save(u);
            return mapping.findForward("detail");
        } else if (DUPLICATE_HOST.equals(subAction)) {
            final var h = HostHome.findByPrimaryKey(subActionParameter);
            try {
                final var mi = MasterManager.getMI();
                final var session = Util.getECpdsSessionFromObject(u);
                mi.copyHost(session, d.getName(), h.getName());
            } catch (final Throwable t) {
                log.warn(t);
                final var e = new ECMWFActionFormException(Format.getMessage(t));
                e.initCause(t);
                throw e;
            }
            return mapping.findForward("detail");
        } else if (INCREASE_HOST_PRIORITY.equals(subAction)) {
            final var h = HostHome.findByPrimaryKey(subActionParameter);
            h.increasePriorityWithinDestination(d, HOST_PRIORITY_STEP);
            h.save(u);
            return mapping.findForward("detail");
        } else if (DECREASE_HOST_PRIORITY.equals(subAction)) {
            final var h = HostHome.findByPrimaryKey(subActionParameter);
            h.decreasePriorityWithinDestination(d, HOST_PRIORITY_STEP);
            h.save(u);
            return mapping.findForward("detail");
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
        if (VALIDATE.equals(subAction)) {
            // Just changed to the screen without performing an action
            forward = mapping.findForward("validate");
        } else if (SELECT_FILTERED.equals(subAction)) {
            var hasAccess = true;
            try {
                hasAccess = u.hasAccess(getResource(request, "datatransfer.basepath"));
            } catch (final Exception e) {
                log.error("Problem checking access for user '" + u
                        + "' to this destination data transfers. Returning all without filtering.");
            }
            form.addToSelections(form.getDataTransfers(hasAccess));
            forward = mapping.findForward("validate");
        } else if (EXTEND_LIFETIME.equals(subAction)) {
            new ExecutorRepository<>(exceptions, processedCount, form.getActionTransfers()) {
                @Override
                public void exec(final DataTransfer transfer) throws Exception {
                    transfer.setExpiryDate(new Date(transfer.getExpiryDate().getTime() + Timer.ONE_DAY), u);
                }
            };
            forward = mapping.findForward("validate.redir");
        } else if (INCREASE_TRANSFER_PRIORITY.equals(subAction)) {
            new ExecutorRepository<>(exceptions, processedCount, form.getActionTransfers()) {
                @Override
                public void exec(final DataTransfer transfer) throws Exception {
                    transfer.setPriority(transfer.getPriority() - TRANSFER_PRIORITY_STEP, u);
                }
            };
            forward = mapping.findForward("validate.redir");
        } else if (DECREASE_TRANSFER_PRIORITY.equals(subAction)) {
            new ExecutorRepository<>(exceptions, processedCount, form.getActionTransfers()) {
                @Override
                public void exec(final DataTransfer transfer) throws Exception {
                    transfer.setPriority(transfer.getPriority() + TRANSFER_PRIORITY_STEP, u);
                }
            };
            forward = mapping.findForward("validate.redir");
        } else if (SET_TRANSFER_PRIORITY.equals(subAction)) {
            new ExecutorRepository<>(exceptions, processedCount, form.getActionTransfers()) {
                @Override
                public void exec(final DataTransfer transfer) throws Exception {
                    transfer.setPriority(Integer.parseInt(form.getNewPriority()), u);
                }
            };
            if (!exceptions.isEmpty()) {
                form.setNewPriority("");
            }
            forward = mapping.findForward("validate.redir");
        } else if (REQUEUE.equals(subAction)) {
            new ExecutorRepository<>(exceptions, processedCount, form.getActionTransfers()) {
                @Override
                public void exec(final DataTransfer transfer) throws Exception {
                    transfer.requeue(u);
                }
            };
            if (exceptions.isEmpty()) {
                form.cleanActionTransfers();
                form.cleanSelectedTransfers();
                forward = mapping.findForward("detail");
            } else {
                forward = mapping.findForward("validate.redir");
            }
        } else if (STOP.equals(subAction)) {
            new ExecutorRepository<>(exceptions, processedCount, form.getActionTransfers()) {
                @Override
                public void exec(final DataTransfer transfer) throws Exception {
                    transfer.stop(u);
                }
            };
            forward = mapping.findForward("validate.redir");
        } else if (CANCEL.equals(subAction)) {
            forward = mapping.findForward("detail");
        } else if (CLEAN.equals(subAction)) {
            form.cleanActionTransfers();
            form.cleanSelectedTransfers();
            forward = mapping.findForward("detail");
        } else if (IMMEDIATE_RESTART.equals(subAction)) {
            try {
                d.restart(false, u);
                forward = mapping.findForward("detail");
            } catch (final TransferException e) {
                throw new ECMWFActionFormException("Problem executing action '" + subAction + "'", e);
            }
        } else if (CLEAN_DESTINATION.equals(subAction)) {
            try {
                d.clean(u);
                forward = mapping.findForward("detail");
            } catch (final TransferException e) {
                throw new ECMWFActionFormException("Problem executing action '" + subAction + "'", e);
            }
        } else if (CLEAN_EXPIRED_DESTINATION.equals(subAction)) {
            try {
                d.cleanExpired(u);
                forward = mapping.findForward("detail");
            } catch (final TransferException e) {
                throw new ECMWFActionFormException("Problem executing action '" + subAction + "'", e);
            }
        } else if (GRACEFUL_RESTART.equals(subAction)) {
            try {
                d.restart(true, u);
                forward = mapping.findForward("detail");
            } catch (final TransferException e) {
                throw new ECMWFActionFormException("Problem executing action '" + subAction + "'", e);
            }
        } else if (IMMEDIATE_PUT_ON_HOLD.equals(subAction)) {
            try {
                d.putOnHold(false, u);
                forward = mapping.findForward("detail");
            } catch (final TransferException e) {
                throw new ECMWFActionFormException("Problem executing action '" + subAction + "'", e);
            }
        } else if (GRACEFUL_PUT_ON_HOLD.equals(subAction)) {
            try {
                d.putOnHold(true, u);
                forward = mapping.findForward("detail");
            } catch (final TransferException e) {
                throw new ECMWFActionFormException("Problem executing action '" + subAction + "'", e);
            }
        } else {
            throw new ECMWFActionFormException(
                    "The subAction '" + subAction + "' is not defined for class " + this.getClass().getName());
        }
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
        for (final Pair p : params) {
            out.append(p.getName()).append("=").append(p.getValue()).append("&");
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
        log.warn("Data Transfer '" + id + "' doesn't belong to Destination '" + d.getName() + "', but to Destination '"
                + transfer.getDestinationName() + "'. Possible cheating attempt, playing with the URLS?. User is '"
                + u.getId() + "'");
        throw new TransferException("Data Transfer '" + id + "' doesn't belong to Destination '" + d.getName()
                + "'. Cannot perform operation.");
    }
}
