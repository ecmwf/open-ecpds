package ecmwf.ecpds.master.plugin.http.controller.datafile.datafile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.model.users.User;
import ecmwf.web.ECMWFException;

/**
 * Dispatcher for DataFile module. Routes /datafile/datafile/* requests to the correct view.
 */
public class DataFileDispatcherAction extends PDSAction {

    private static final Logger log = LogManager.getLogger(DataFileDispatcherAction.class);

    @Override
    public ActionForward safeAuthorizedPerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, User user) throws ECMWFException {

        // Struts wildcard parameter {1}
        final String action = request.getParameter(mapping.getParameter());

        if (action == null) {
            // Default behavior: go to main page
            return mapping.findForward("success");
        }

        try {
            switch (action.toLowerCase()) {

            case "list":
                return mapping.findForward("list");

            case "detail":
            case "view":
            case "datafile":
            default:
                return mapping.findForward("success");
            }

        } catch (Exception e) {
            log.warn("DataFileDispatcher error for action=" + action, e);
            throw new ECMWFException("Invalid DataFile action: " + action, e);
        }
    }
}