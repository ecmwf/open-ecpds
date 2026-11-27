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

/*
 * Created on 17-Dec-2004
 *
 */
package ecmwf.ecpds.master.plugin.http.view.news;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.services.content.Command;
import ecmwf.ecpds.master.plugin.http.services.content.CommandBase;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorService;
import ecmwf.ecpds.master.plugin.http.util.bean.NewsItemUtil;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * Get a NewsItem by name and print a processed field
 *
 * @author sy8
 */

public class ItemPropertyCommand extends CommandBase implements Command {

    private static Log log = LogFactory.getLog(ItemPropertyCommand.class);

    private String channelKey = "channel";
    private String defaultProperty = "body";

    public String getOutput(Content container, Map parameters, String body, ContentProcessingOptions options)
            throws ContentProcessorException {

        String itemName = null;
        try {

            if (!parameters.containsKey("name"))
                return "[" + getName() + ": Needs a 'name' parameter]";
            else
                itemName = parameters.get("name").toString();

            NewsItem item = NewsItemUtil.lookupItem(itemName, container);

            String property = null;
            if (parameters.containsKey("property"))
                property = parameters.get("property").toString();
            else
                property = this.defaultProperty;

            return wrapInTable(ContentProcessorService.process(item, property, options).getStringRepresentation(),
                    options, item);

        } catch (NewsException e) {
            log.warn("Error looking for a NewsItem", e);
            return "[" + getName() + ": No item found for name '" + itemName + "':" + e.getMessage() + "]";
        } catch (Exception e) {
            throw new ContentProcessorException("Problem executing command", e);
        }

    }

    public String wrapInTable(String content, ContentProcessingOptions options, NewsItem item) {

        if (options.getExternalContext() instanceof PageContext) {
            try {
                PageContext context = (PageContext) options.getExternalContext();
                String out = "";
                out += "<table class=\"includedItemWrapper\" border=\"0\">\n<tr>";

                NewsChannel channel = item.getChannel();
                String fullLink = TagUtils.getResource(context, "news.basepath", "") + "/edit/" + channel.getName()
                        + "/update_form/" + item.getId();
                // Object o = TagUtils.lookupBean(context, User.SESSION_KEY);

                out += "<td >" + content + "</td>";

                out += "<td class=\"top\">";

                out += "<a href=\"" + TagUtils.getResource(context, "news.basepath", "") + "/get/" + channel.getName()
                        + "/" + item.getName() + "\">";
                out += "<img border=\"0\" alt=\"" + TagUtils.getResource(context, "news.goto.included", "")
                        + "\" title=\"" + TagUtils.getResource(context, "news.goto.included", "") + "\" ";
                out += " onmouseover=\"this.parentNode.parentNode.parentNode.parentNode.parentNode.className='includedItemHighlighted'\" onmouseout=\"this.parentNode.parentNode.parentNode.parentNode.parentNode.className='includedItemWrapper'\" ";
                out += " src=\"" + TagUtils.getResource(context, "icon.top.right.corner.small", "") + "\"/></a>";

                out += "</td>";

                // out += "<td class=\"top\">";
                // if (o != null && o instanceof User) {
                // User u = (User) o;
                // if (u.hasAccess(context.getRequest().getServerName(), fullLink)) {

                // out += "<a href=\"" + fullLink + "\"><div style=\"font-size:8px\"
                // onmouseover=\"this.parentNode.parentNode.parentNode.parentNode.parentNode.className='includedItemHighlightedEdit'\"
                // onmouseout=\"this.parentNode.parentNode.parentNode.parentNode.parentNode.className='includedItemWrapper'\">[edit]</div>";
                // out += "<img border=\"0\" alt=\"" + TagUtils.getResource(context, "news.goto.included.edit", "") +
                // "\" title=\"" + TagUtils.getResource(context, "news.goto.included.edit", "")+ "\" ";
                // out += " src=\"" + TagUtils.getResource(context, "icon.top.right.corner.small.red", "") + "\"/>";
                // out += "</a>";

                // }
                // }
                // out += "\n</td>";

                out += "</tr>\n</table>";
                return out;
            } catch (ModelException e) {
                return content;
            }
        } else
            return content;
    }

}