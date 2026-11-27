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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
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

public class ChannelTreeTag extends TagSupport {

    private static Log log = LogFactory.getLog(ChannelTreeTag.class);

    private String topName = "top";
    private String openName = "open";
    private String tableStyle = "channelTree";
    private String headerStyle = "newsLabel";
    private String bodyStyle = "";
    private String descriptionStyle = "";
    private String footerStyle = "newsAdditionalLabel";

    private String basePathKey = null;
    private String link = null;

    private boolean allowEditing = true;
    private boolean hideForNonEditors = true;
    private int maxLevels = 100;

    private String upIconKey = "icon.small.arrow.up";
    private String unrollIconKey = "icon.plus";
    private String rollIconKey = "icon.minus";
    private String downIconKey = "icon.small.arrow.down";

    private Map context;

    public void setTopName(String s) {
        this.topName = s;
    }

    public void setOpenName(String s) {
        this.openName = s;
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
        this.allowEditing = "true".equals(allow);
    }

    public void setHideForNonEditors(String hide) {
        this.hideForNonEditors = "true".equals(hide);
    }

    public void setMaxLevels(String levels) {
        try {
            this.maxLevels = Integer.parseInt(levels);
        } catch (NumberFormatException e) {
            log.warn("Bad integer", e);
        }
    }

    public void setLink(String s) {
        this.link = s;
    }

    public void setBasePathKey(String s) {
        this.basePathKey = s;
    }

    public int doStartTag() throws JspTagException {

        try {

            NewsChannel top = (NewsChannel) TagUtils.lookupBean(pageContext, this.topName);
            Collection open = (Collection) TagUtils.lookupBean(pageContext, this.openName);

            this.context = new HashMap(3);
            this.context.put("channel", TagUtils.lookupBean(pageContext, "channel"));
            this.context.put("item", TagUtils.lookupBean(pageContext, "item"));

            pageContext.getOut().println("\n<!-- Taglib generated HTML code.  -->");
            User u = (User) TagUtils.lookupBean(pageContext, User.SESSION_KEY);
            pageContext.getOut().println(getChannels(pageContext, u, top, open, 0));
            pageContext.getOut().println("<!-- END of Taglib generated HTML code -->\n");

        } catch (ModelException e) {
            log.error("Error retrieving Bean Data. ", e);
        } catch (ContentProcessorException e) {
            log.error("Error processing bean data. ", e);
        } catch (IOException e) {
            log.error("Error writing bean data. ", e);
        } catch (ClassCastException e) {
            log.error("Expecting a NewsChannel and a Collection ", e);
        }

        return SKIP_BODY;

    } // ***** PRIVATE

    // ***************************************************************************************
    // */

    public String getChannels(PageContext page, User u, NewsChannel channel, Collection open, int level)
            throws ContentProcessorException, ModelException {

        String out = "";
        if (level < this.maxLevels) {

            out += "<table class=\"" + this.tableStyle + "\">";
            out += "<tr><td >";
            out += getChannel(page, u, channel, open.contains(channel.getId()), level);
            out += "</td></tr>";

            if (open.contains(channel.getId())) {
                Iterator c = channel.getChildren().iterator();
                while (c.hasNext()) {
                    out += "<tr><td style=\"padding-left: 22px\">";
                    out += getChannels(page, u, (NewsChannel) c.next(), open, level + 1);
                    out += "</td></tr>";
                }
            }
            out += "</table>";
        }
        return out;

    }

    private String getChannel(PageContext context, User u, NewsChannel channel, boolean unrolled, int level)
            throws ContentProcessorException, NewsException {

        if (canSee(u, channel)) {

            this.context.put("to", channel);
            String out = "\n<!-- Channel " + channel.getId() + "-->";

            out += "<table class=\"" + this.tableStyle + "\">";
            out += "<tr><td align=\"right\">" + getToolbar(context, channel, unrolled, level) + "</td>";
            out += "<td class=\"" + this.bodyStyle + "\">";

            if (link == null)
                out += "<a class=\"menuitem\" href=\"" + channel.getBasePathKey() + channel.getAccessPath() + "\">";
            else
                out += "<a class=\"menuitem\" title=\""
                        + TagUtils.getResource(pageContext, "newsChannels.tree.move", "") + "\" href=\""
                        + ((this.basePathKey != null) ? TagUtils.getResource(pageContext, this.basePathKey, "") : "")
                        + TagUtils.parseMultiExpressionText(this.context, link) + "\">";

            out += channel.getName();
            out += "</a>: ";
            out += channel.getTitle();
            out += "</td>";
            out += "</tr>";
            out += "</table>";

            out += "<!-- End of Channel " + channel.getId() + "-->\n";
            return out;

        } else {
            return "";
        }

    }

    /**
     * Get a tool bar with several buttons:
     */
    private String getToolbar(PageContext context, NewsChannel channel, boolean unrolled, int level) {

        String out = "", href = "", image = "";
        out += "<table><tr>";

        // IF not the TOP channel but we are the first level, button to go UP in
        // the hierarchy
        if (level == 0 && !channel.getIsRoot()) {
            out += "<td>";
            href = "<a href=\"?topId=" + channel.getParentId() + "&open=" + channel.getParentId() + "\">";
            image = "<img src=\"" + TagUtils.getResource(context, upIconKey, "") + "\" border=\"0\" alt=\""
                    + TagUtils.getResource(context, "newsChannels.tree.setParentAsTop", "") + "\" title=\""
                    + TagUtils.getResource(context, "newsChannels.tree.setParentAsTop", "") + "\"/>";
            out += href + image + "</a>";
            out += "</td>";
        }

        // UNROLL and ROLL this channel
        String icon = unrolled ? rollIconKey : unrollIconKey;
        String parameter = unrolled ? "close" : "open";
        out += "<td>";
        href = "<a href=\"?" + parameter + "=" + channel.getId() + "\">";
        image = "<img src=\"" + TagUtils.getResource(context, icon, "") + "\" border=\"0\" alt=\""
                + TagUtils.getResource(context, "newsChannels.tree." + parameter, "") + "\" title=\""
                + TagUtils.getResource(context, "newsChannels.tree." + parameter, "") + "\"/>";
        out += href + image + "</a>";
        out += "</td>";

        // Set this as TOP and UNROLL
        if (level > 0) {
            out += "<td>";
            href = "<a href=\"?topId=" + channel.getId() + "&open=" + channel.getId() + "\">";
            image = "<img src=\"" + TagUtils.getResource(context, downIconKey, "") + "\" border=\"0\" alt=\""
                    + TagUtils.getResource(context, "newsChannels.tree.setAsTop", "") + "\" title=\""
                    + TagUtils.getResource(context, "newsChannels.tree.setAsTop", "") + "\"/>";
            out += href + image + "</a>";
            out += "</td>";
        }

        // Go to this channel homepage
        out += "<td>";
        href = "<a href=\"" + TagUtils.getResource(context, "news.basepath", "") + "/get/" + channel.getName() + "\">";
        image = "<img src=\"" + TagUtils.getResource(context, "icon.text", "") + "\" border=\"0\" alt=\""
                + TagUtils.getResource(context, "newsChannels.home", "") + "\" title=\""
                + TagUtils.getResource(context, "newsChannels.home", "") + "\"/>";
        out += href + image + "</a>";
        out += "</td>";

        if (this.allowEditing) {

            try {

                User u = (User) TagUtils.lookupBean(context, User.SESSION_KEY);
                out += "<td>";
                out += this.getLinkIfAuthenticated(context, u, channel, "/update_form?id=" + channel.getId(),
                        "icon.small.update", "newsChannels.tree.update");
                out += "</td><td>";
                out += this.getLinkIfAuthenticated(context, u, channel, "/delete_form?id=" + channel.getId(),
                        "icon.small.delete", "newsChannels.tree.delete");
                out += "</td>";

            } catch (UserException e) {
                log.error("Problem drawing buttons", e);
            }
        }
        out += "</tr></table>";
        return out;

    }

    private String getLinkIfAuthenticated(PageContext context, User u, NewsChannel channel, String link, String iconKey,
            String titleKey) throws UserException {

        String fullLink = TagUtils.getResource(context, "news.basepath", "") + "/channels/edit/" + link;
        if (u != null && u.hasAccess(context.getRequest().getServerName(), fullLink)) {
            String out = "<a href=\"" + fullLink + "\">";
            String image = "<img src=\"" + TagUtils.getResource(context, iconKey, "") + "\" border=\"0\" alt=\""
                    + iconKey + "\" title=\"" + TagUtils.getResource(context, titleKey, "") + "\"/>";
            return out + image + "</a>";
        } else
            return "";
    }

    private static boolean canSee(User u, NewsChannel c) {
        return true;
    }

}