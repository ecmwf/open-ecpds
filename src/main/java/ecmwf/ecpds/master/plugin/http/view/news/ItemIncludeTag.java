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

import javax.servlet.ServletException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.news.NewsChannelHome;
import ecmwf.ecpds.master.plugin.http.home.news.NewsItemHome;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;
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

public class ItemIncludeTag extends TagSupport {

    private static Log log = LogFactory.getLog(ItemIncludeTag.class);

    private String name = "";
    private String channelName = "";
    private boolean allowEditing = true;

    public void setName(String s) {
        this.name = s;
    }

    public void setChannel(String s) {
        this.channelName = s;
    }

    public int doStartTag() throws JspTagException {

        try {

            NewsChannel channel = null;
            if (!"".equals(channelName)) {
                channel = NewsChannelHome.findByName(channelName);
            } else {
                channel = (NewsChannel) TagUtils.lookupBean(pageContext, "channel");
            }
            NewsItem item = NewsItemHome.findByNameAndChannelId(name, channel.getId());

            pageContext.getOut().println("\n<!-- Taglib generated HTML code.  -->");
            pageContext.include(item.getBasePathKey() + item.getAccessPath());
            pageContext.getOut().println("<!-- END of Taglib generated HTML code -->\n");

        } catch (NewsException e) {
            log.error("Channel or Item not found. ", e);
        } catch (IOException e) {
            log.error("Error writing bean data. ", e);
        } catch (ClassCastException e) {
            log.error("Expecting a NewsItem ", e);
        } catch (ServletException e) {
            log.error("Error including content ", e);
        }

        return SKIP_BODY;

    }

    private String getToolbar(PageContext context, NewsChannel channel, NewsItem item) {

        String out = "";
        try {

            Object o = TagUtils.lookupBean(context, User.SESSION_KEY);

            if (o != null) {

                User u = (User) o;

                out += "\n<table><tr><td>";
                out += this.getLinkIfAuthenticated(context, u, channel, item, "/insert_form?parentId=" + item.getId(),
                        "icon.small.insert", "news.comment.add");
                out += "</td>";

                if (this.allowEditing) {
                    out += "<td>";
                    out += this.getLinkIfAuthenticated(context, u, channel, item, "/update_form/" + item.getId(),
                            "icon.small.update", "news.comment.update");
                    out += "</td><td>";
                    out += this.getLinkIfAuthenticated(context, u, channel, item, "/delete_form/" + item.getId(),
                            "icon.small.delete", "news.comment.delete");
                    out += "</td>";
                }
                out += "</tr></table>";

            }

            return out;

        } catch (UserException e) {
            log.error("Problem drawing buttons", e);
        }

        return "";
    }

    private String getLinkIfAuthenticated(PageContext context, User u, NewsChannel channel, NewsItem item, String link,
            String iconKey, String titleKey) throws UserException {

        String fullLink = TagUtils.getResource(context, "news.basepath", "") + "/edit/" + channel.getName() + link;
        if (u != null && u.hasAccess(context.getRequest().getServerName(), fullLink)) {
            String out = "<a href=\"" + fullLink + "\">";
            String image = "<img src=\"" + TagUtils.getResource(context, iconKey, "") + "\" border=\"0\" alt=\""
                    + iconKey + "\" title=\"" + TagUtils.getResource(context, titleKey, "") + "\"/>";
            return out + image + "</a>";
        } else
            return "";
    }

}