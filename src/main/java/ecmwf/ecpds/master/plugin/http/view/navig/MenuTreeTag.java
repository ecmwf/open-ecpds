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

package ecmwf.ecpds.master.plugin.http.view.navig;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.navig.MenuItem;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * <p>
 * Creates an HTML menu open at current position. Creates HTML breadcrumbs. Creates HTML info about the user. Creates
 * HTML "related" menu.
 * </p>
 *
 * The output of this tag is just the HTML code for the element. Then it should be inserted into the adequate place of
 * the JSP where to be shown. Currently it is being assigned to a JavaScript variable and dynamically inserted in the
 * page.
 *
 * @author Daniel Varela
 */

public class MenuTreeTag extends TagSupport {

    private static Log log = LogFactory.getLog(MenuTreeTag.class);
    private String name = "navig.root";
    private String urlPrefix = "tree/details?id=";
    private boolean useUrlPrefixAsMainLink = false;
    private boolean showACLInfo = false;

    // ------------------------------------------------------------- Properties

    /**
     * @param name
     *            The Name of the current menu bean.
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param name
     *            Url prefix to add to links
     */
    public void setUrlPrefix(String value) {
        this.urlPrefix = value;
    }

    public void setUseUrlPrefixAsMainLink(String value) {
        this.useUrlPrefixAsMainLink = "true".equals(value);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Process the start tag.
     *
     * @exception JspException
     *                if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {

        // Retrieve the message string we are looking for
        MenuItem root = (MenuItem) TagUtils.lookupBean(pageContext, this.name);

        if (root == null) {
            JspException e = new JspException("'MenuItem with name '" + name + "' not found in request.");
            org.apache.struts.taglib.TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }
        this.showACLInfo = "true".equals(pageContext.getRequest().getParameter("showACLInfo"));

        try {
            pageContext.getOut().write(getTree(root, 0));
        } catch (IOException e) {
        }

        // Continue processing this page
        return (SKIP_BODY);

    }

    public String getTree(MenuItem here, int level) {

        try {
            String out = "<a name=\"" + here.getId() + "\">";
            out += "<table class=\"menu_tree_table\"><tr><td class=\"menu_tree_title_td\">" + this.drawBox(here, level)
                    + "</td><td class=\"menu_tree_other_td\">";
            Iterator children = here.getChildren().iterator();
            while (children.hasNext()) {
                out += getTree((MenuItem) children.next(), level + 1);
            }
            out += "</td></tr></table>\n";
            return out;
        } catch (ModelException e) {
            log.error("Problem getting menu tree", e);
            return "[Error: " + e.getMessage() + "]";
        }
    }

    private String drawBox(MenuItem here, int level) throws UserException {

        String linkHere = here.getDestinationUrl();
        String linkWithPrefix = TagUtils.parseMultiExpressionText(pageContext, this.urlPrefix)
                + URLEncoder.encode(here.getId());
        if (this.useUrlPrefixAsMainLink)
            linkHere = linkWithPrefix;

        String aclInfo = "";
        if (this.showACLInfo) {
            aclInfo = "<tr><td colspan=\"2\">R:" + here.getReadACL().getPolicies() + ", W:"
                    + here.getWriteACL().getPolicies() + "</td></tr>";
        }

        return "<table cellpadding=\"0\" cellspacing=\"0\">" + "<tr><td class=\"menu_level_" + level + "\">"
                + "<a href=\"" + linkHere + "\">" + here.getText() + "</a></td>" + "<td><a href=\"" + linkWithPrefix
                + "\"><img src=\"/assets/images/w3ec3/black_arrow.gif\" border=\"0\" align=\"right\"></a>"
                + "</td></tr>" + aclInfo + "</table>";

    }

}