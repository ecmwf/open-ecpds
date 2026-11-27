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

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.navig.MenuItemHome;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.navig.MenuItem;
import ecmwf.ecpds.master.plugin.http.model.users.Resource;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;
import ecmwf.ecpds.master.plugin.http.util.Util;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * Show a link only if the current user HAS permissions to see it. If the <code>imageKey</code> parameter is supplied,
 * show the icon add icon referred by that resource key.
 *
 */

public class AuthorizedLinkTag extends BodyTagSupport {

    private static Log log = LogFactory.getLog(AuthorizedLinkTag.class);
    private String href = ""; // The href of the link or a expression
                              // resolving to it.
    private String linkTitle = null; // The TITLE for the link

    private String target = null;
    private String imageKey = null; // The key to a possible icon or a
                                    // expression resolving to it.
    private String imageAltKey = null; // The key to an ALT text for the image.
    private String imageTitleKey = null; // The key to a TITLE text for the
                                         // image.
    private String basePathKey = null; // The base path to apply to the link or
                                       // a expression resolving to it.
    private String beanName = null; // The name of the bean to resolve attribute
                                    // names.
    private String style = null; // The CSS style to be used
    private String alternativeText = null; // Show this if the user can not see
                                           // the link;
    private ArrayList wrappingTags = null; // Wrap the conditional content with
                                           // these tags
    private ArrayList wrappingTagsClasses = null; // Styles to use in the
                                                  // wrapping tags;
    private ListIterator wrappingTagsIterator = null; // Wrap content with
                                                      // these tags, if
                                                      // authorized.
    private boolean checkMenuWriteAndAddMenuId = false; // If this is set, check ALSO if the
    // current MenuItem is Writable
    private boolean ignoreAccessControl = false; // Sometimes it can
                                                 // interesting to use just
                                                 // the link
    // building features, because checking auth would be overkill.

    // private int maxLinkTextCharacters = Integer.MIN_VALUE;

    private boolean authorized = false;

    public void setStyleClass(String value) {
        this.style = value;
    }

    public void setHref(String value) {
        this.href = value;
    }

    public void setImageKey(String value) {
        this.imageKey = value;
    }

    public void setImageAltKey(String value) {
        this.imageAltKey = value;
    }

    public void setImageTitleKey(String value) {
        this.imageTitleKey = value;
    }

    public void setBasePathKey(String value) {
        this.basePathKey = value;
    }

    public void setLinkTitle(String value) {
        this.linkTitle = value;
    }

    public void setName(String value) {
        this.beanName = value;
    }

    public void setIgnoreAccessControl(String value) {
        this.ignoreAccessControl = "true".equals(value);
    }

    public void setCheckMenuWriteAndAddMenuId(String value) {
        this.checkMenuWriteAndAddMenuId = "true".equals(value);
    }

    public void setNoACL(String value) {
        this.setIgnoreAccessControl(value);
    }

    public void setAlternativeText(String text) {
        this.alternativeText = text;
    }

    public void setWrappingTags(String s) {
        this.wrappingTags = Util.listStringToArrayList(s);
    }

    public void setWrappingTagsClasses(String s) {
        this.wrappingTagsClasses = Util.listStringToArrayList(s);
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /*
     * // Not so easy to access the BODY of the tag from within // TODO: Think a bit about this public void
     * setMaxLinkTextCharacters(String s) { try { this.maxLinkTextCharacters = Integer.parseInt(s); } catch
     * (NumberFormatException e) { log.warn("Bad value for 'maxLinkTextCharacters': ",e); } }
     */

    public int doStartTag() throws JspTagException {

        User user = null;
        try {

            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            String host = request.getServerName();
            user = (User) TagUtils.lookupBean(pageContext, User.SESSION_KEY);

            if (user != null || this.ignoreAccessControl) {

                this.href = this.calculateHref(this.basePathKey, this.href, this.beanName);

                this.authorized = this.isAuthorized(user, host, this.href);

                if (this.authorized) {
                    pageContext.getOut().print(getLinkOpening(this.href));
                    pageContext.getOut().print(getLinkBody());
                    return EVAL_BODY_BUFFERED;
                } else {
                    if (log.isDebugEnabled())
                        log.debug("User '" + user.getUid() + "' won't see href '" + this.href + "'.");
                    if (this.alternativeText != null)
                        pageContext.getOut()
                                .print(TagUtils.parseMultiExpressionText(pageContext, this.alternativeText));
                    return SKIP_BODY;
                }

            } else {
                log.warn("No user found. Will not display '" + this.href + "'.");
            }

        } catch (UserException e) {
            log.error("Error getting user information", e);
        } catch (IOException e) {
            log.error("Writing page content", e);
        } catch (ClassCastException e) {
            log.error("Got an " + user.getClass().getName() + " when expecting " + User.class.getName(), e);
        } catch (Exception e) {
            log.error("Error getting user information", e);
        }

        return this.SKIP_BODY;

    }

    public int doAfterBody() {

        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException e) {
            log.error("AuthorizedLinkTag: (IOException) Writing page content", e);
        }
        return SKIP_BODY;

    }

    public int doEndTag() {

        try {
            if (this.authorized)
                pageContext.getOut().print(getLinkClosing());
        } catch (IOException e) {
            log.error("AuthorizedLinkTag: (IOException) Writing page content", e);
        }
        return EVAL_PAGE;

    }

    /*
     * PRIVATE, DON'T LOOK HERE ;-) ***********************************************
     */

    private String calculateHref(String basePathKey, String href, String beanName) {
        return TagUtils.getLink(pageContext, href,
                (basePathKey != null) ? TagUtils.parseMultiExpressionText(pageContext, basePathKey) : null);
    }

    private String getLinkOpening(String href) {

        String styleBit = (this.style != null) ? " class=\"" + this.style + "\"" : "";
        return getWrappingTags() + "<a " + styleBit + getLinkTarget() + " href=\"" + href + "\" " + getLinkTitle()
                + ">";
    }

    private String getLinkClosing() {

        return "</a>" + getWrappingTagsClosings();
    }

    private String getLinkBody() {
        if (this.imageKey != null && !this.imageKey.equals(""))
            return getImage();
        else
            return "";
    }

    /**
     * The link to the image is got by parsing the string received as an expression. The result is used as a key to a
     * resource.
     *
     * @return The real src of the image.
     */

    private String getImage() {
        return "<img src=\""
                + TagUtils.getResource(pageContext, TagUtils.parseMultiExpressionText(pageContext, this.imageKey), "")
                + "\" border=\"0\" " + getImageAlt() + " " + getImageTitle() + "/>";
    }

    private String getImageAlt() {
        if (this.imageAltKey == null)
            return "";
        else
            return "alt=\"" + TagUtils.getResource(pageContext,
                    TagUtils.parseMultiExpressionText(pageContext, this.imageAltKey),
                    "Alt '" + this.imageAltKey + "' not Found") + "\"";
    }

    private String getImageTitle() {
        if (this.imageTitleKey == null)
            return "";
        else
            return "title=\"" + TagUtils.getResource(pageContext,
                    TagUtils.parseMultiExpressionText(pageContext, this.imageTitleKey),
                    "Title '" + this.imageTitleKey + "' not Found") + "\"";
    }

    private String getLinkTarget() {
        if (this.target == null)
            return "";
        else
            return " target=\"" + this.target + "\"";
    }

    private String getLinkTitle() {
        if (this.linkTitle == null)
            return "";
        else
            return " title=\"" + TagUtils.parseMultiExpressionText(pageContext, this.linkTitle) + "\" ";
    }

    private String getWrappingTags() {

        String out = "";
        if (this.wrappingTags != null) {
            this.wrappingTagsIterator = this.wrappingTags.listIterator();
            int pos = 0;
            while (this.wrappingTagsIterator.hasNext()) {
                out += "<"
                        + ((this.wrappingTagsClasses != null && this.wrappingTagsClasses.size() > pos)
                                ? this.wrappingTagsClasses.get(pos) : "")
                        + (this.wrappingTagsIterator.next().toString()) + ">";
                pos++;
            }
        }
        return out;

    }

    private String getWrappingTagsClosings() {
        String out = "";
        if (this.wrappingTags != null) {
            while (this.wrappingTagsIterator.hasPrevious())
                out += "</" + (this.wrappingTagsIterator.previous().toString()) + ">";
        }
        return out;
    }

    private boolean isAuthorized(User user, String host, String href) throws ModelException {

        int pos = -1;
        if ((pos = href.indexOf("?")) > 0)
            href = href.substring(0, pos);

        if (user != null && log.isDebugEnabled() && !this.ignoreAccessControl)
            log.debug(
                    "Checking permission for user '" + user.getUid() + "' to host '" + host + "' href '" + href + "'");

        if (!this.checkMenuWriteAndAddMenuId)
            return this.ignoreAccessControl || user.hasAccess(host, href);
        else {
            // Find the current menu for this page
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            String url = null;
            Object urlO = request.getAttribute(Resource.ORIGINAL_URL_REQUEST_KEY);
            if (urlO != null)
                url = urlO.toString();
            else
                url = request.getRequestURL().toString();

            MenuItem menu = MenuItemHome.findAuthoritativeByResourceUrl(url);
            if (this.ignoreAccessControl || (user.hasAccess(host, href) && user.canWrite(menu))) {
                this.href += "?id=" + menu.getId();
                return true;
            } else {
                return false;
            }

        }

    }

}
