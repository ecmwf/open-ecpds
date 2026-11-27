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

package ecmwf.ecpds.master.plugin.http.view.news;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorService;
import ecmwf.ecpds.master.plugin.http.view.general.ModelBeanSearchTag;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * NewsBeanSearchTag.java
 *
 * This class implements a JSP Tag whose main objective is to take a Collection of NewsItemBean and display them in a
 * table using some supplied parameters.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla </a>
 *
 * @version 0.1.0
 */

public class CommentsTag extends ModelBeanSearchTag {

    private static Log log = LogFactory.getLog(CommentsTag.class);

    private String name = "item";
    private String tableStyle = "comments";
    private String headerStyle = "";
    private String bodyStyle = "";
    private String descriptionStyle = "";
    private String footerStyle = "";
    private boolean allowEditing = false;
    private boolean allowReplies = false;
    private boolean showTop = false;
    private boolean showComponents = true;

    public void setName(String s) {
        this.name = s;
    }

    /**
     * @param bodyStyle
     *            The bodyStyle to set.
     */
    public void setBodyStyle(String bodyStyle) {
        this.bodyStyle = bodyStyle;
    }

    /**
     * @param descriptionStyle
     *            The descriptionStyle to set.
     */
    public void setDescriptionStyle(String descriptionStyle) {
        this.descriptionStyle = descriptionStyle;
    }

    /**
     * @param footerStyle
     *            The footerStyle to set.
     */
    public void setFooterStyle(String footerStyle) {
        this.footerStyle = footerStyle;
    }

    /**
     * @param headerStyle
     *            The headerStyle to set.
     */
    public void setHeaderStyle(String headerStyle) {
        this.headerStyle = headerStyle;
    }

    /**
     * @param tableStyle
     *            The tableStyle to set.
     */
    public void setTableStyle(String tableStyle) {
        this.tableStyle = tableStyle;
    }

    public void setAllowEditing(String allow) {
        this.allowEditing = "true".equals(allow) || "yes".equals(allow);
    }

    public void setAllowReplies(String allow) {
        this.allowReplies = "true".equals(allow) || "yes".equals(allow);
    }

    public void setShowTop(String allow) {
        this.showTop = "true".equals(allow);
    }

    public void setShowComponents(String allow) {
        this.showComponents = "true".equals(allow) || "yes".equals(allow);
    }

    public int doStartTag() throws JspTagException {

        try {

            NewsItem item = (NewsItem) TagUtils.lookupBean(pageContext, this.name);
            User u = (User) TagUtils.lookupBean(pageContext, User.SESSION_KEY);

            pageContext.getOut().println("\n<!-- Taglib generated HTML code.  -->");
            pageContext.getOut().println(getComments(pageContext, item, true, u));
            pageContext.getOut().println("<!-- END of Taglib generated HTML code -->\n");

        } catch (ModelException e) {
            log.error("Error retrieving Bean Data. ", e);
        } catch (ContentProcessorException e) {
            log.error("Error processing bean data. ", e);
        } catch (IOException e) {
            log.error("Error writing bean data. ", e);
        } catch (ClassCastException e) {
            log.error("Class class problem", e);
        }

        return SKIP_BODY;

    } // ***** PRIVATE

    // ***************************************************************************************
    // */

    public String getComments(PageContext page, NewsItem item, boolean isTop, User u)
            throws ContentProcessorException, ModelException {

        NewsChannel channel = item.getChannel();

        String out = "";
        out += "<table>";

        if (this.showTop || !isTop) {
            out += "<tr><td >";
            out += getItem(page, channel, item, !isTop, u);
            out += "</td></tr>";
        }

        ArrayList children = new ArrayList(item.getChildren());
        Collections.sort(children, new ChildrenCreationComparator());

        Iterator c = children.iterator();
        while (c.hasNext()) {
            out += "<tr><td style=\"padding-left: 18px\">";
            out += getComments(page, (NewsItem) c.next(), false, u);
            out += "</td></tr>";
        }

        out += "</table>";
        return out;

    }

    private String getItem(PageContext context, NewsChannel channel, NewsItem item, boolean showContent, User u)
            throws ContentProcessorException, NewsException {

        String out = "\n<!-- Item " + item.getId() + "-->";

        out += "<table class=\"" + this.tableStyle + "\">";
        out += "<thead><tr><td> &nbsp;" + item.getTitle()
        // +": &nbsp; <i>" + (showContent?item.getAuthor():"Comments") + "</i>"
                + " &nbsp;</td>";
        out += getToolbar(context, channel, item, u) + "</tr></thead>";

        if (showContent) {
            out += "<tbody>";

            ContentProcessingOptions options = ContentProcessorService.createProcessingOptions();
            options.setOutputContentType("text/html");
            String outDescr = ContentProcessorService.process(item, "description", options).getStringRepresentation();

            if (!"".equals(outDescr)) {
                out += "<tr><td class=\"commentBody\" colspan=\"4\">";
                out += outDescr;
                out += "</td></tr>";
            }

            String outBody = ContentProcessorService.process(item, "body", options).getStringRepresentation();

            if (this.showComponents) {
                ComponentsTag ct = new ComponentsTag();
                outBody += ct.getComponentsHTML(context, item);
            }

            if (!"".equals(outBody)) {
                out += "<tr><td class=\"commentBody\" colspan=\"4\">";
                out += outBody;
                out += "</td></tr></tbody>";
            }

            out += "<tfoot><tr><td colspan=\"4\">";

            out += "Posted by " + item.getAuthor() + " on ";
            // "<search:content name="child.creationDate"
            // dateFormatKey="date.format.medium" />" +
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm");
            out += format.format(item.getCreationDate());

            out += "</td>";
            out += "</tr></tfoot>";
        }

        out += "</table>";

        out += "<!-- End of Item " + item.getId() + "-->\n";
        return out;

    }

    private String getToolbar(PageContext context, NewsChannel channel, NewsItem item, User currentUser) {

        String out = "";
        try {

            User u = (User) TagUtils.lookupBean(context, User.SESSION_KEY);

            out += "<td width=\"1\" class=\"nowrap newsdate\" >";
            if (this.allowEditing)
                out += this.getLinkIfAuthenticated(context, u, channel, item, "/insert_form?parentId=" + item.getId(),
                        "icon.small.reply", "news.comment.add", "news.comment.add");
            out += "</td>";

            // Allow to edit the comment if the current user is the
            // one who published the comment originally
            // If the user is a moderator then allow to edit AND remove

            boolean isTheAuthor = currentUser != null && item.getAuthorUid() != null
                    && item.getAuthorUid().equals(currentUser.getUid());
            boolean isEditor = currentUser != null && ConfigService
                    .getOptionalParameter("news", "item.commentEditors", "").indexOf(currentUser.getUid()) >= 0;

            if (this.allowEditing && isTheAuthor || isEditor) {
                out += "<td width=\"1\">";
                out += this.getLinkIfAuthenticated(context, u, channel, item, "/update_form/" + item.getId(),
                        "icon.small.update", "news.comment.update", null);
                out += "</td>";
                if (isEditor) {
                    out += "<td width=\"1\">";
                    out += this.getLinkIfAuthenticated(context, u, channel, item, "/delete_form/" + item.getId(),
                            "icon.small.delete", "news.comment.delete", null);
                    out += "</td>";
                } else {
                    out += "<td width=\"1\" colspan=\"1\"></td>";
                }
            } else {
                out += "<td width=\"1\" colspan=\"3\"></td>";
            }
            return out;

        } catch (UserException e) {
            log.error("Problem drawing buttons", e);
        }

        return "";
    }

    private String getLinkIfAuthenticated(PageContext context, User u, NewsChannel channel, NewsItem item, String link,
            String iconKey, String titleKey, String textKey) throws UserException {

        String fullLink = TagUtils.getResource(context, "news.basepath", "") + "/edit/" + channel.getName() + link;
        if (u != null && u.hasAccess(context.getRequest().getServerName(), fullLink)) {
            String out = "<a href=\"" + fullLink + "\" style=\"text-decoration:none\">";
            String image = ((textKey != null) ? TagUtils.getResource(context, textKey, "") : "") + " <img src=\""
                    + TagUtils.getResource(context, iconKey, "") + "\" border=\"0\" alt=\"" + iconKey + "\" title=\""
                    + TagUtils.getResource(context, titleKey, "") + "\"/>";
            return out + image + "</a>";
        } else
            return "";
    }

    private class ChildrenCreationComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof NewsItem && o2 instanceof NewsItem) {
                return ((NewsItem) o1).getCreationDate().compareTo(((NewsItem) o2).getCreationDate());
            } else
                return 0;
        }

    }

}