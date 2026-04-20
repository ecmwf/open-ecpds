package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

public class DestinationDispatcherAction extends PDSAction {

    private static final Logger log = LogManager.getLogger(DestinationDispatcherAction.class);

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException {

        final String json = request.getParameter("json");

        try {
            if ("list".equalsIgnoreCase(json)) {
                return new GetDestinationListJsonAction().safeAuthorizedPerform(mapping, form, request, response, user);

            } else if ("dataList".equalsIgnoreCase(json)) {
                return new GetDestinationTransferListJsonAction().safeAuthorizedPerform(mapping, form, request,
                        response, user);

            } else if ("validateList".equalsIgnoreCase(json)) {
                return new GetValidateTransferListJsonAction().safeAuthorizedPerform(mapping, form, request, response,
                        user);
            }

            // Default: normal page
            return new GetDestinationAction().safeAuthorizedPerform(mapping, form, request, response, user);

        } catch (Exception e) {
            log.warn("Error in DestinationDispatcherAction", e);
            throw e;
        }
    }
}