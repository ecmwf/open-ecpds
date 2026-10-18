package ecmwf.ecpds.master.plugin.http.controller.monitoring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

public final class GetWeatherAction extends PDSAction {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public ActionForward safeAuthorizedPerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, User user) throws ECMWFException {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var snapshot = MasterManager.getMI().getWeatherViewer(Util.getECpdsSessionFromObject(user),
                    !"true".equals(request.getParameter("stateOnly")));
            JSON.writeValue(response.getWriter(), snapshot);
        } catch (Exception e) {
            org.apache.logging.log4j.LogManager.getLogger(GetWeatherAction.class).warn("Weather viewer unavailable", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        return null;
    }
}
