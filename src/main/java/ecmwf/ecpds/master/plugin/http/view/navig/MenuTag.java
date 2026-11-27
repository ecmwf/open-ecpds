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
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.navig.MenuItemHome;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.navig.MenuException;
import ecmwf.ecpds.master.plugin.http.model.navig.MenuItem;
import ecmwf.ecpds.master.plugin.http.model.users.Resource;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * <p>
 * Creates an HTML menu open at current position.
 * </p>
 *
 *
 * @author Daniel Varela
 */

public class MenuTag extends TagSupport {

    private static Log log = LogFactory.getLog(MenuTag.class);
    private String cssClass = null;

    // ------------------------------------------------------------- Properties

    public void setCssClass(String value) {
        this.cssClass = value;
    }

    public int doStartTag() throws JspException {

        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        Object urlO = req.getAttribute(Resource.ORIGINAL_URL_REQUEST_KEY);

        String url = "/";
        if (urlO != null)
            url = urlO.toString();

        MenuItem here = null;

        try {
            try {

                here = MenuItemHome.findAuthoritativeByResourceUrl(url);
            } catch (MenuException e) {
                here = MenuItemHome.findByPrimaryKey(ConfigService.getParameter("directory", "dn.website.top"));
            }
            try {
                pageContext.getOut().write(getMenu(here));
            } catch (IOException e) {
                // This is not happening.
            }
        } catch (Exception e) {
            log.error("Problem getting menu for url '" + url + "'", e);
        }

        // Continue processing this page
        return SKIP_BODY;

    }

    public String getMenu(MenuItem here) {

        try {
            return drawChildren(here.getChildren());

        } catch (ModelException e) {
            log.error("Problem getting menu", e);
            return "[Error: " + e.getMessage() + "]";
        }

    }

    private String drawChildren(Collection items) throws ModelException {

        String out = "<ul" + (cssClass != null ? " class=\"" + cssClass + "\"" : "") + ">";
        Iterator children = items.iterator();
        while (children.hasNext()) {
            MenuItem child = (MenuItem) children.next();
            if (!child.getHideInMainMenu()) {
                out += "<li>";
                out += "<a href=\"" + child.getDestinationUrl() + "\">" + child.getText() + "</a>";
                if (child.isExternal())
                    out += " <img src=\"" + TagUtils.getResource(pageContext, "icon.link", "") + "\">";
                out += "</li>";
            }
        }
        return out + "</ul>\n";
    }

}