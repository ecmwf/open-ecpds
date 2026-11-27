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

package ecmwf.ecpds.master.plugin.http.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.plugin.http.ECMWFException;
import ecmwf.ecpds.master.plugin.http.ECMWFRecoverableException;
import ecmwf.ecpds.master.plugin.http.model.AccessDeniedException;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ResourceNotFoundException;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.util.bean.StringPair;

/**
 *
 * This is intended to an abstract base class for all Actions, handling exceptions and providing utility methods to get
 * user information.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 */

public abstract class ECMWFAction extends Action {

    private static Log log = LogFactory.getLog(ECMWFAction.class);
    public static final String EXCEPTION_KEY = "ecmwf.request.exception";
    public static final String PATH_PARAMETERS_KEY = "ecmwf.request.path_parameters.key";
    public static final String RETURN_AFTER_ERROR_KEY = "RETURN_AFTER_ERROR";
    private int warnIfLongerThan = 5;
    private boolean checkBasePath = false;

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {

        this.checkBasePath = "true".equals(this.getServlet().getInitParameter("ENSURE_BASEPATH"));

        Date begin = new Date();
        ActionForward forward = null;
        Principal p = request.getUserPrincipal();
        String uid = (p != null) ? p.getName() : "";

        // Keep error state
        boolean error = false;
        Exception ecmwfException = null;

        if (log.isInfoEnabled())
            log.info("[" + uid + "@" + request.getRemoteHost() + "] " + request.getPathInfo() + " => "
                    + this.getClass().getName() + ".");

        try {

            request.setAttribute(PATH_PARAMETERS_KEY,
                    processParameterEnsuringBasePath(request, mapping.getParameter(), mapping.getPath()));
            forward = safePerform(mapping, form, request, response);

        } catch (ECMWFRecoverableException e) {

            // Save data
            request.getSession().setAttribute(e.getDataKey(), e.getDataObject());
            // Pass where to go to actually "recover"
            request.setAttribute(ECMWFAction.RETURN_AFTER_ERROR_KEY,
                    new StringPair(e.getReturnLabel(), e.getReturnLink()));

            ecmwfException = e;
            error = true;

        } catch (ECMWFException e) {

            error = true;
            ecmwfException = e;

        } catch (Exception e) {

            error = true;
            ecmwfException = e;

        }

        Date end = new Date();
        checkTimeTaken(request, begin, end);

        if (error) {
            // ServletException e = new ServletException(ecmwfException);
            request.setAttribute(EXCEPTION_KEY, ecmwfException);
            try {
                if (ecmwfException instanceof ResourceNotFoundException)
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
                else if (ecmwfException instanceof AccessDeniedException)
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
                else {
                    log.error("Server Error", ecmwfException);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
                }
            } catch (IOException e) {
            }
            return null;
        } else {
            return forward;
        }

    }

    public abstract ActionForward safePerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response)
            throws IllegalAccessException, ECMWFException, ModelException, ClassCastException, ECMWFActionFormException;

    /**
     * Returns only the object that is stored in the session. A Filter is needed in this case to ensure that there is an
     * user there, otherwise we'll get a null.
     *
     * @param req
     *            Current request
     * @param enforceAuthentication
     *            Useless here, to be removed
     */

    public static User getUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute(User.SESSION_KEY);
    }

    public static String getUserCommonName(HttpServletRequest req) {
        User u = getUser(req);
        return (u == null) ? "Anonymous" : (!"null null".equals(u.getCommonName())) ? u.getCommonName() : "Anonymous";
    }

    public static String getUserUid(HttpServletRequest req) {
        User u = getUser(req);
        return (u == null) ? "" : u.getUid();
    }

    /**
     * <p>
     * struts-config.xml mappings path match only uses the part of the path with is AFTER the web.xml servlet mapping.
     * This way, if we have multiple Apache and Tomcat servlets mapping pointing to the ActionServlet, struts is not
     * able to make a difference between them and will execute the same Actions independently of the Apache/Tomcat
     * mod_jk/web.xml mappings triggered.
     * </p>
     * <p>
     * We don't want this because since our Access Control is based on URLs, we'd have to protect all possible
     * combinations of Apache+Tomcat/Struts mappings. So this method will user the "parameter" attribute of "action" to
     * ensure that the Apache+Tomcat mapping is the only one we want. This solution is not perfect because reverse
     * mappings from <html:form> tags won't work (which servlet mapping to use?) but it will do till Struts 1.2, where
     * the Modules feature should provide for this.
     * </p>
     *
     * @param request
     *            The HTTP request
     * @param parameter
     *            The Apache+Tomcat mapping WE KNOW (via "parameter") that is the only one which can access this Action.
     *            After it, comma separated, path parameters.
     * @param actionPath
     *            The path of the action mapped.
     *
     * @return A Collection will all path parameters or NULL if none.
     */

    protected Collection processParameterEnsuringBasePath(HttpServletRequest request, String parameter,
            String actionPath) throws ECMWFException {

        String basePath = parameter;
        String currentPath = request.getContextPath() + request.getServletPath();

        Collection pathParameters = null;
        if (parameter != null) {
            StringTokenizer tok = new StringTokenizer(parameter, "|");

            if (checkBasePath) {
                if (tok.hasMoreTokens())
                    basePath = tok.nextToken();
                else
                    basePath = ""; // No basePath in the parameters. Actually
                                   // NOTHING in the parameters
                if (!currentPath.equals(basePath))
                    throw new ECMWFException("The action '" + this.getClass().getName() + "' mapped via '" + actionPath
                            + "' is expecting to be accessed through the path '" + basePath + "' instead of '"
                            + currentPath + "'");
            }

            pathParameters = new ArrayList();
            while (tok.hasMoreTokens())
                pathParameters.add(tok.nextToken());

        }

        return pathParameters;

    }

    public String getResource(HttpServletRequest request, String key) {
        return getResources(request, Globals.MESSAGES_KEY).getMessage(key);
    }

    public void checkTimeTaken(HttpServletRequest request, Date begin, Date end) {

        double diff = (end.getTime() - begin.getTime()) / 1000.0;
        if (diff > warnIfLongerThan) {
            log.warn(request.getPathInfo() + " took " + diff + " seconds at the controller ("
                    + this.getClass().getName() + "). Performance must be reviewed.");
        } else if (log.isDebugEnabled()) {
            log.debug(request.getPathInfo() + " took " + diff + " seconds at the controller ("
                    + this.getClass().getName() + ").");
        }

    }

} // ECMWFAction.java
