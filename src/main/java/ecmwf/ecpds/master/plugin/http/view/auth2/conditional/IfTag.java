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

package ecmwf.ecpds.master.plugin.http.view.auth2.conditional;

/**
 * This class implements a "if" authorization tag. The tag has a "realms" attribute, mandatory, which is a
 * comma-separated list of the possible authentication domain which would fulfill the authorization request.
 * This tag will only evaluate the truthness of the filter, a value that will be used by its "children tags",
 * "then" and "else", implemented by <code>ThenTag</code> and <code>ElseTag</code>.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.9.0
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.users.ResourceHome;
import ecmwf.ecpds.master.plugin.http.model.users.Category;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.util.Util;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

public class IfTag extends BodyTagSupport {

    private static Log log = LogFactory.getLog(IfTag.class);

    private Collection policies = null;
    private Collection paths = null;
    private URL url = null;

    private String basePath = "";
    private boolean enabled = false; // Go/NoGo
    private boolean realUser = true;
    private String service = null;

    /**
     * The preferred method for showing or not showing content is checking if a user can access a given path/s. If the
     * user can access <i>all</i> of the supplied paths, the filter is fulfilled.
     *
     * @param paths
     *            The comma-separated list of paths that will be evaluated, in the current server, with an AND
     *            operation.
     */

    public void setPaths(String paths) {

        this.paths = new Vector();

        if ("".equals(paths)) {
            // In this case we'll be relying on the basePathKey only.
            this.paths.add("");
        } else {
            this.paths = Util.listStringToCollection(paths, ",");
        }
        log.debug("IfTag checking for paths: " + this.paths);

    }

    /**
     * An alternative way of filtering content is to directly specify the "policies" that can access to the content. If
     * the user has <i>one</i> of the policies, he will be able to access. Since this is more implementation dependent
     * that the path-based filter, its use is not as recommended as the other one.
     *
     * @param policies
     *            The comma-separated list of policies that will be matched with an OR operation.
     */

    public void setPolicies(String policies) {
        this.policies = Util.listStringToCollection(policies, ",");
    }

    /**
     * And yet another way. Pass an URL that will be parsed with a java.net.URL class for a host and a path. This
     * enables checking of non-local permissions.
     *
     * @param policies
     *            The url to check
     */

    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            log.error("Bad url passed: " + url, e);
        }
    }

    public void setBasePathKey(String key) throws JspException {

        String resolvedKey = TagUtils.parseMultiExpressionText(pageContext, key);
        String message = TagUtils.getResource(pageContext, resolvedKey, "");
        if (message == "")
            log.warn("IfTag: '" + resolvedKey + "' (from '" + key + "') entry not found (or empty) in resources file.");
        else
            this.basePath = message;

    }

    public void setService(String service) {
        this.service = service;
    }

    public void setRealUser(String service) {
        this.realUser = "true".equals(service);
    }

    /**
     * Hook method called by the taglib container at the start of the tag. We respond EVAL_BODY_TAG, meaning that we
     * want the body of the tag to be evaluated (the "then" and "else" tags probably are there).
     *
     * @return What to do after... EVAL_BODY_TAG
     */

    public int doStartTag() {
        // log.debug(" IfTag startTag() !!!");
        this.enabled = this.evaluateFilter();
        return EVAL_BODY_BUFFERED;

    }

    /**
     * Hook method called by the taglib container at the end of the tag body, and after all of it has been evaluated. We
     * first write out the body content, and then call SKIP_BODY to avoid further body evaluations (as would be the
     * case, for example, of a loop tag).
     *
     * @return What to do after... SKIP_BODY
     */

    public int doAfterBody() throws JspTagException {

        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
            return SKIP_BODY;
        } catch (IOException ex) {
            throw new JspTagException(ex.toString());
        }

    }

    /**
     * This method, intended to be used by children tags <code>ThenTag</code> and <code>ElseTag</code>, provides them
     * with the info if the user belonged or not to the policies and hence show or hide their bodies.
     *
     * @return Is the user enabled to see this?
     *
     */

    protected boolean userEnabled() {
        return this.enabled;
    }

    // Private *******************************************************************************************

    /**
     * Evaluate a filter constructed from the data set by the Tag properties. The data source is current user.
     *
     * @return The result of the evaluation.
     */

    private boolean evaluateFilter() {

        try {

            if (this.url != null)
                return this.evaluateUrl();
            if (this.paths != null)
                return this.evaluatePaths();
            else if (this.policies != null)
                return this.evaluateCategories();
            else if (this.realUser)
                return this.evaluateLoggedUser();
            else {
                log.error(
                        "IfTag: An IfTag should have a 'urls', 'paths' or a 'policies' attribute. We are evaluating as 'false' due to their non presence.");
                return false;
            }

        } catch (UserException e) {
            log.error("Problem trying to get user or resource information", e);
            return false;
        }

    }

    /**
     * TODO: Try to concentrate of the "user grabbing" into one method.
     *
     * @return Can the user see our paths.. ?
     *
     * @throws UserException
     */

    private boolean evaluatePaths() throws UserException {

        User user = (User) pageContext.getSession().getAttribute(User.SESSION_KEY);
        if (user == null) {
            log.error("There is no user in session with key '" + User.SESSION_KEY + "'.");
            return false;
        }

        Iterator i = this.paths.iterator();
        String path = null;
        while (i.hasNext()) {
            path = TagUtils.parseMultiExpressionText(pageContext, (String) i.next());
            log.debug("Checking access to path '" + basePath + path + "'");
            if (!user.hasAccess(pageContext.getRequest().getServerName(), basePath + path))
                return false;
        }

        pageContext.setAttribute("auth.if.validated.path", basePath + path);
        return true;

    }

    private boolean evaluateCategories() throws UserException {

        User user = (User) pageContext.getSession().getAttribute(User.SESSION_KEY);
        if (user == null) {
            log.error("There is no user in session with key '" + User.SESSION_KEY + "'.");
            return false;
        }

        // return this.policies.contains(user.getCategory().getName());
        return this.matchPolicies(user.getPolicies());

    }

    private boolean matchPolicies(Collection c) {
        Iterator i = c.iterator();
        while (i.hasNext()) {
            if (this.policies.contains(i.next().toString()))
                return true;
        }
        return false;
    }

    private boolean evaluateLoggedUser() throws UserException {

        User user = (User) pageContext.getSession().getAttribute(User.SESSION_KEY);
        if (user == null) {
            log.error("There is no user in session with key '" + User.SESSION_KEY + "'.");
            return false;
        }
        try {
            String publidId = ConfigService.getParameter("users", "public_user.id");
            return !publidId.equals(user.getUid());
        } catch (Exception e) {
            log.error("Problem checking for a non anonymous user", e);
            return false;
        }

    }

    private boolean evaluateUrl() throws UserException {

        User user = (User) pageContext.getSession().getAttribute(User.SESSION_KEY);
        return user.canRead(ResourceHome.findByURI(this.url.toString()));

    }

}
