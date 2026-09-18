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
                return withServlet(new GetDataTransferListJsonAction()).safeAuthorizedPerform(mapping, form, request,
                        response, user);
            }

            return withServlet(new GetDataTransferAction()).safeAuthorizedPerform(mapping, form, request, response,
                    user);

        } catch (Exception e) {
            throw new ECMWFActionFormException(e.getMessage(), e);
        }
    }

    /**
     * Propagates this dispatcher's {@link org.apache.struts.action.ActionServlet} to a directly-instantiated
     * sub-action, since Struts only calls {@code setServlet} on actions it creates itself via the action mapping. Not
     * doing so leaves {@code action.getServlet()} {@code null}, which makes any call relying on it (e.g.
     * {@link ecmwf.web.controller.ECMWFAction#getResource}) throw a {@link NullPointerException}.
     *
     * @param action
     *            the newly created sub-action
     *
     * @return the same action, for chaining
     */
    private <T extends org.apache.struts.action.Action> T withServlet(final T action) {
        action.setServlet(getServlet());
        return action;
    }
}