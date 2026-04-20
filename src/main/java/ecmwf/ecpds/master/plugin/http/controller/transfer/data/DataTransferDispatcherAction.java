package ecmwf.ecpds.master.plugin.http.controller.transfer.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

public class DataTransferDispatcherAction extends PDSAction {

    @Override
    public ActionForward safeAuthorizedPerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, User user) throws ECMWFException {

        String json = request.getParameter("json");

        try {
            if ("list".equalsIgnoreCase(json)) {
                return new GetDataTransferListJsonAction().safeAuthorizedPerform(mapping, form, request, response,
                        user);
            }

            return new GetDataTransferAction().safeAuthorizedPerform(mapping, form, request, response, user);

        } catch (Exception e) {
            throw new ECMWFActionFormException(e.getMessage(), e);
        }
    }
}