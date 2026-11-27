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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.news.NewsChannelHome;
import ecmwf.ecpds.master.plugin.http.home.news.NewsItemHome;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.services.content.Command;
import ecmwf.ecpds.master.plugin.http.services.content.CommandBase;
import ecmwf.ecpds.master.plugin.http.services.content.CommandParameter;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.util.Util;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * @author sy8
 *
 *         Command displaying a list or table of items.
 */
public class ItemsCommand extends CommandBase implements Command {

    private static Log log = LogFactory.getLog(ItemsCommand.class);

    private String containerStyle = "itemList";
    private String innerContainerStyle = "itemInnerList";
    private String itemStyle = null;
    private String tdEven = "even";
    private String tdOdd = "odd";

    public Collection getParameters() {
        return Arrays.asList(new Object[] {
                new CommandParameter("channel", "String",
                        "Name of the channel to list. It is mandatory to have one of 'channel' or 'query'", false),
                new CommandParameter("query", "Search Expression",
                        "Search expression to apply globally. See documentation to check available fields", false),
                new CommandParameter("limit", "Number", "Maximum number of items to display", false),
                new CommandParameter("order", "String [ASC|DESC]?, ....",
                        "A list of comma separated fields, with ASC or DESC to specify ascending or descending sort by the field",
                        false),
                new CommandParameter("recursive", "Boolean",
                        "Search on the channel specified (default) or also in subchannels", false),
                new CommandParameter("fields", "List", "Fields to show on the listing (default: title)", false),
                new CommandParameter("fieldTitles", "List", "Titles for the fields", false),
                new CommandParameter("fieldStyles", "List", "Styles to apply to every individual field", false) });
    }

    public void setContainerStyle(String s) {
        this.containerStyle = s;
    }

    public void setTableStyle(String s) {
        this.containerStyle = s;
        this.innerContainerStyle = s;
    }

    public void setListStyle(String s) {
        this.containerStyle = s;
    }

    public void setinnerContainerStyle(String s) {
        this.innerContainerStyle = s;
    }

    public void setItemStyle(String s) {
        this.itemStyle = s;
    }

    public String getOutput(Content container, Map params, String body, ContentProcessingOptions options)
            throws ContentProcessorException {
        ArrayList fields = null, fieldTitles = null, fieldStyles = null, fieldTitleStyles = null;
        if (params.containsKey("fields"))
            fields = Util.listStringToArrayList(params.get("fields").toString());
        if (params.containsKey("fieldTitles"))
            fieldTitles = Util.listStringToArrayList(params.get("fieldTitles").toString());
        if (params.containsKey("fieldStyles"))
            fieldStyles = Util.listStringToArrayList(params.get("fieldStyles").toString());
        if (params.containsKey("fieldTitleStyles"))
            fieldTitleStyles = Util.listStringToArrayList(params.get("fieldTitleStyles").toString());

        String containerStyle = params.containsKey("containerStyle") ? params.get("containerStyle").toString()
                : this.containerStyle;
        String innerContainerStyle = params.containsKey("innerContainerStyle")
                ? params.get("innerContainerStyle").toString() : this.innerContainerStyle;

        return getHTML(getItems(container, params), params, options, fields, fieldTitles, fieldStyles, fieldTitleStyles,
                containerStyle, innerContainerStyle);
    }

    private Collection getItems(Content container, Map parameters) throws ContentProcessorException {

        try {
            Collection items = null;
            if (parameters.containsKey("channel")) {
                NewsChannel c = NewsChannelHome.findByName(parameters.get("channel").toString());
                items = c.getNewsItems();
            } else if (parameters.containsKey("query")) {
                ModelSearch search = NewsItemHome.getDefaultSearch(parameters.get("query").toString());

                if (parameters.containsKey("limit")) {
                    search.setSizeLimit(Integer.parseInt(parameters.get("limit").toString()));
                }

                if (parameters.containsKey("offset")) {
                    search.setOffset(Integer.parseInt(parameters.get("offset").toString()));
                }

                if (parameters.containsKey("order")) {
                    Collection order = Util.listStringToCollection(parameters.get("order").toString());
                    search.setSortItemsFromStrings(order);
                }

                if (parameters.containsKey("recursive")) {
                    if (parameters.get("recursive").equals("true"))
                        search.setScope(ModelSearch.SUBTREE_SCOPE);
                    else
                        search.setScope(ModelSearch.ONELEVEL_SCOPE);
                }

                search.setPartialMatches(
                        parameters.containsKey("partialMatches") && parameters.get("partialMatches").equals("true"));
                items = NewsItemHome.find(search);

            } else if (container instanceof NewsChannel) {
                items = ((NewsChannel) container).getNewsItems();
            } else
                throw new ContentProcessorException("Needs a 'channel' or 'query' parameter.");

            return items;

        } catch (NewsException e) {
            throw new ContentProcessorException("Error getting items", e);
        }

    }

    private String getHTML(Collection items, Map params, ContentProcessingOptions options, ArrayList fields,
            ArrayList fieldTitles, ArrayList fieldStyles, ArrayList fieldTitleStyles, String containerStyle,
            String innerContainerStyle) {
        if (params.containsKey("mode") && params.get("mode").equals("table")) {
            return getTable(items, params, options, fields, fieldTitles, fieldStyles, fieldTitleStyles, containerStyle,
                    innerContainerStyle);
        } else {
            return getList(items, params, options, fields, fieldTitles, fieldStyles, fieldTitleStyles, containerStyle,
                    innerContainerStyle);
        }
    }

    private String getList(Collection items, Map parameters, ContentProcessingOptions options, ArrayList fields,
            ArrayList fieldTitles, ArrayList fieldStyles, ArrayList fieldTitleStyles, String containerStyle,
            String innerContainerStyle) {
        String out = "";
        if (parameters.containsKey("title"))
            out += parameters.get("title");

        out += "\n<ul class=\"" + containerStyle + "\">";
        Iterator i = items.iterator();
        Object ctx = options.getExternalContext();
        PageContext context = null;
        if (ctx instanceof PageContext)
            context = (PageContext) ctx;

        while (i.hasNext()) {
            NewsItem item = (NewsItem) i.next();
            if (fields == null)
                out += "\n<li" + ((itemStyle != null) ? " class=\"" + itemStyle + "\"" : "") + "><a href=\""
                        + item.getBasePathKey() + item.getAccessPath() + "\">" + item.getTitle() + "</a></li>";
            else {
                Iterator f = fields.iterator();
                int nField = 0;
                out += "\n<li>\n<ul class=\"" + innerContainerStyle + "\">";
                while (f.hasNext()) {
                    try {
                        String tempOut = "";
                        String styleClass = "";
                        if (fieldStyles != null && nField < fieldStyles.size()) {
                            styleClass = "class=\"" + fieldStyles.get(nField) + "\"";
                        } else if (this.itemStyle != null) {
                            styleClass = "class=\"" + this.itemStyle + "\"";
                        }

                        tempOut += "\n<li " + styleClass + ">";
                        if (fieldTitles != null && nField < fieldTitles.size()) {
                            tempOut += "<div class=\"" + ((fieldTitleStyles != null && nField < fieldTitleStyles.size())
                                    ? ("class=\"" + fieldTitleStyles.get(nField).toString()) : "title") + ">";
                            tempOut += fieldTitles.get(nField).toString();
                            tempOut += "</div>";
                        }

                        tempOut += "\n<a href=\"" + item.getBasePathKey() + item.getAccessPath() + "\">";
                        // Object o =
                        // TagUtils.getFieldValueFromBean(context,item,f.next().toString());
                        String fieldName = f.next().toString();
                        if ("body".equals(fieldName) || "description".equals(fieldName))
                            tempOut += TagUtils.getProcessFieldContent(item, fieldName, null, context, "text/html",
                                    false, true, null, false, -1);
                        else
                            tempOut += TagUtils.getFieldValueFromBean(context, item, fieldName);

                        tempOut += "</a>&nbsp;";

                        if (fieldTitles != null)
                            tempOut += "</div>";
                        tempOut += "\n</li>\n";

                        out += tempOut;

                    } catch (Exception e) {
                        out += "\n<li>[" + e.getMessage() + "]</li>";
                    }
                    nField++;
                }
                out += "\n</ul>\n</li>";
            }
        }
        out += "\n</ul>\n";
        return out;
    }

    private String getTable(Collection items, Map parameters, ContentProcessingOptions options, ArrayList fields,
            ArrayList fieldTitles, ArrayList fieldStyles, ArrayList fieldTitleStyles, String containerStyle,
            String innerContainerStyle) {
        String out = "";
        boolean even = false;

        List widths = null;
        int widthSize = 0;
        if (parameters.containsKey("columnsWidth")) {
            widths = Util.listStringToArrayList(parameters.get("columnsWidth").toString());
            widthSize = widths.size();
        }

        Object ctx = options.getExternalContext();
        PageContext context = null;
        if (ctx instanceof PageContext)
            context = (PageContext) ctx;

        out += "\n<table class=\"" + containerStyle + "\">\n";
        if (parameters.containsKey("title"))
            out += "<caption>" + parameters.get("title") + "</caption>";

        if (fieldTitles != null) {
            out += "<tr class=\"fieldTitles\">";
            Iterator i = fieldTitles.iterator();
            while (i.hasNext()) {
                out += "<th>" + (i.next()) + "</th>";
            }
            out += "</tr>";
        }

        Iterator i = items.iterator();
        int nField = 0;
        while (i.hasNext()) {
            NewsItem item = (NewsItem) i.next();
            out += "<tr class=\"" + (even ? this.tdEven : this.tdOdd) + "\">";

            if (fields == null)
                out += getField(item, "title", context, null, itemStyle, nField);
            else {
                Iterator j = fields.iterator();
                int fieldCount = 0;
                while (j.hasNext()) {
                    out += getField(item, j.next().toString(), context,
                            (widths != null && widthSize > fieldCount) ? widths.get(fieldCount).toString() : null,
                            ((fieldStyles != null && nField < fieldStyles.size()) ? fieldStyles.get(nField).toString()
                                    : itemStyle),
                            nField);
                    fieldCount++;
                }

            }
            nField++;
            out += "</tr>";
            even = !even;
        }
        out += "</table>\n";
        return out;
    }

    private String getField(NewsItem item, String field, PageContext context, String width, String style, int nField) {
        try {
            return "<td " + (width != null ? ("width=\"" + width + "\"") : "") + " class=\"" + style + "\"><a href=\""
                    + item.getBasePathKey() + item.getAccessPath() + "\">"
                    + TagUtils.getFieldValueFromBean(context, item, field) + "</a></td>\n";
        } catch (ModelException e) {
            log.error("Problem getting value from field '" + field + "' of NewsItem id '" + item.getId() + "'", e);
            return "<td>!!</td>";
        }
    }

}