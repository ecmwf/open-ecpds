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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.news.NewsChannelHome;
import ecmwf.ecpds.master.plugin.http.home.news.NewsItemHome;
import ecmwf.ecpds.master.plugin.http.home.news.components.ComponentHome;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.model.news.components.Component;
import ecmwf.ecpds.master.plugin.http.model.news.components.ComponentException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorService;
import ecmwf.ecpds.master.plugin.http.util.Format;
import ecmwf.ecpds.master.plugin.http.util.Util;
import ecmwf.ecpds.master.plugin.http.util.bean.NewsItemUtil;
import ecmwf.ecpds.master.plugin.http.util.bean.Treble;
import ecmwf.ecpds.master.plugin.http.util.bean.StringPair;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * Show the listing of attachments for a given item
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla </a>
 *
 * @version 0.1.0
 */

public class ComponentsTag extends TagSupport {

    private static Log log = LogFactory.getLog(ComponentsTag.class);

    private String query = null;
    private int limit = Integer.MIN_VALUE;
    private Collection order = Util.listStringToCollection("modificationDate DESC");
    private String title = null;
    private String footer = null;

    private String titleKey = "news.component";
    private String uploadTitle = "[Upload a new document]";
    private String emptyTitle = "No documents uploaded";
    private String requestKey = "item";
    private String itemName = null;
    private String channelName = null;
    private Collection fields = null;
    private Collection fieldTitles = null;
    private Collection linkedFields = null;
    private ArrayList styles = null;
    private Collection channels = null;

    private boolean alwaysShowHeader = false;
    private boolean showUploadLink = false;

    private String tableStyle = null;
    private String rowEvenStyle = "even";
    private String rowOddStyle = "odd";

    private boolean allowEditing = true;
    private boolean showDetails = false;
    private boolean showFieldTitles = false;
    private boolean showTitle = true;
    private boolean showFooter = false;
    private boolean showSize = true;
    private boolean showName = true;
    private boolean showImage = true;
    private boolean linkToDetails = false;

    private String writeSearchIntoProperty = null;
    private int imageWidth = -1;
    private int imageHeight = -1;

    private static final int GRID_LAYOUT = 0;
    private static final int ROWS_LAYOUT = 1;
    private int layout = ROWS_LAYOUT;

    private int gridColumns = 5;

    public ComponentsTag() {
        this.tableStyle = ConfigService.getOptionalParameter("news", "componentsTable.defaultStyle", "componentsList");
    }

    /**
     * Item Name, fully (channel::item) or partly qualified. The files to list will be taken from this NewsItem.
     */

    public void setName(String s) {
        this.itemName = s;
    }

    /**
     * Channel Name. All files from all NewsItems of the channel will be listed.
     */

    public void setChannel(String s) {
        this.channelName = s;
    }

    /**
     * The CSS style to be applied to even number (2,4,6...) TR.
     */

    public void setRowEvenStyle(String rowEvenStyle) {
        this.rowEvenStyle = rowEvenStyle;
    }

    /**
     * The CSS style to be applied to odd number (1,3,5...) TR.
     */

    public void setRowOddStyle(String rowOddStyle) {
        this.rowOddStyle = rowOddStyle;
    }

    /**
     * The CSS style to be applied to the external table
     */

    public void setTableStyle(String tableStyle) {
        this.tableStyle = tableStyle;
    }

    /**
     * If edit buttons are to be showed
     */

    public void setAllowEditing(String allow) {
        this.allowEditing = "true".equals(allow);
    }

    /**
     * If the link to the file details is to be showed
     */

    public void setShowDetails(String allow) {
        this.showDetails = "true".equals(allow);
    }

    /**
     * If the link to "Upload a new file" is to be showed
     */

    public void setShowUploadLink(String allow) {
        this.showUploadLink = "true".equals(allow);
    }

    /**
     * Show header even if there are no files
     */

    public void setAlwaysShowHeader(String allow) {
        this.alwaysShowHeader = "true".equals(allow);
    }

    /**
     * Comma separated list of field names to be displayed, in addition to the default fields
     */

    public void setFields(String fields) {
        this.fields = Util.listStringToCollection(fields);
    }

    /**
     * Styles to apply to the fields. The first field will take the first style, the second field the seconds style and
     * so on.
     */

    public void setStyles(String styles) {
        this.styles = Util.listStringToArrayList(styles);
    }

    /**
     * Use this comma separated list of channels to get all components from all items belonging to any of the channels
     *
     * @see setChannel
     */

    public void setChannels(String channels) {
        this.channels = Util.listStringToCollection(channels);
    }

    /**
     * Comma separated list of field titles. If titles are not provided the field names will be used.
     */

    public void setFieldTitles(String fieldTitles) {
        this.fieldTitles = Util.listStringToCollection(fieldTitles);
    }

    /**
     * Comma separated list of fields that will be a link to the file
     */

    public void setLinkedFields(String fieldTitles) {
        this.linkedFields = Util.listStringToCollection(fieldTitles);
    }

    public void setShowFieldTitles(String show) {
        this.showFieldTitles = "true".equals(show);
    }

    public void setShowTitle(String show) {
        this.showTitle = "true".equals(show);
    }

    public void setShowFooter(String show) {
        this.showFooter = "true".equals(show);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public void setEmptyTitle(String title) {
        this.emptyTitle = title;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public void setRequestKey(String name) {
        this.requestKey = name;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setLimit(String limit) {
        try {
            this.limit = Integer.parseInt(limit);
        } catch (NumberFormatException e) {
            log.error("Bad number for 'limit' parameter: " + limit, e);
        }
    }

    public void setOrder(String order) {
        this.order = Util.listStringToCollection(order);
    }

    public void setImageWidth(String value) {
        try {
            this.imageWidth = Integer.parseInt(value);
        } catch (Exception e) {
            log.error("Bad int value", e);
        }
    }

    public void setImageHeight(String value) {
        try {
            this.imageHeight = Integer.parseInt(value);
        } catch (Exception e) {
            log.error("Bad int value", e);
        }
    }

    public void setGridColumns(String value) {
        try {
            this.gridColumns = Integer.parseInt(value);
        } catch (Exception e) {
            log.error("Bad int value", e);
        }
    }

    public void setLayout(String value) {
        this.layout = "grid".equals(value) ? GRID_LAYOUT : ROWS_LAYOUT;
    }

    public void setLinkToDetails(String linkToDetails) {
        this.linkToDetails = "true".equals(linkToDetails);
    }

    public void setShowImage(String showImage) {
        this.showImage = "true".equals(showImage);
    }

    public void setShowName(String showName) {
        this.showName = "true".equals(showName);
    }

    public void setShowSize(String showSize) {
        this.showSize = "true".equals(showSize);
    }

    public void setWriteSearchIntoProperty(String writeSearchIntoProperty) {
        this.writeSearchIntoProperty = writeSearchIntoProperty;
    }

    public int doStartTag() throws JspTagException {

        try {
            pageContext.getOut().println("\n<!-- Taglib generated HTML code.  -->");
            pageContext.getOut().println(this.getComponentsHTML(pageContext, null));
            pageContext.getOut().println("<!-- END of Taglib generated HTML code -->\n");
        } catch (IOException e) {
            log.error("Error writing bean data. ", e);
        }
        return SKIP_BODY;

    } // ***** PRIVATE

    // ***************************************************************************************
    // */

    public String getComponentsHTML(PageContext context, Content container) {

        // How to get the components
        // a) If there is a query, use it to get the components directly
        // b) If there is a "name" parameter, use it as qualified name to find the item
        // c) If there is a "channel" parameter, take all the components of all the items
        // d) If there is a container item, use it as the item
        // e) Try in the request, with the default key

        Treble data = null;
        try {

            data = this.getData(context, container);
            if (data == null)
                return "[ERROR, no data]";

            if (this.writeSearchIntoProperty != null) {
                // Just save results in page property
                pageContext.setAttribute(this.writeSearchIntoProperty, data.getFirst());
                return "";
            } else {
                return this.getComponents(context, (Collection) data.getFirst(), (NewsItem) data.getSecond(),
                        (NewsChannel) data.getThird());
            }

        } catch (ModelException e) {
            log.error("Error retrieving Bean Data. ", e);
        } catch (ContentProcessorException e) {
            log.error("Error processing bean data", e);
        } catch (ClassCastException e) {
            log.error("ClassCastException (data = " + data + ")", e);
        } catch (Exception e) {
            log.error("Other error", e);
        }
        return "[ERROR exception]";

    }

    /**
     * @return A treble of (Collection of Components, relevant NewsItem and relevant NewsChannel)
     */

    public Treble getData(PageContext context, Content container) throws ModelException {

        if (this.query != null) {

            ModelSearch search = ComponentHome.getDefaultSearch();
            search.setQuery(this.query);
            search.setSortItemsFromStrings(this.order);
            search.setSizeLimit(this.limit);
            return new Treble(ComponentHome.find(search), null, null);

        } else {

            NewsItem item = null;
            NewsChannel channel = null;
            if (container != null) {

                if (this.itemName == null && this.channelName == null && container instanceof NewsItem)
                    item = (NewsItem) container;
                else if (this.itemName != null)
                    item = NewsItemUtil.lookupItem(this.itemName, container);
                else if (this.channelName != null) {
                    channel = NewsChannelHome.findByName(this.channelName);
                } else
                    item = (NewsItem) TagUtils.lookupBean(context, this.requestKey);

            } else {

                if (this.itemName != null)
                    item = NewsItemHome.findByQualifiedName(this.itemName);
                else if (this.channelName != null) {
                    channel = NewsChannelHome.findByName(this.channelName);
                } else
                    item = (NewsItem) TagUtils.lookupBean(context, this.requestKey);

            }

            if (item == null && channel == null && this.channels == null) {

                log.warn("[Couldn't find an item to get the files from. Review the parameters]");

            } else if (item != null) {

                // return "\n<!-- Files belonging to item: " + item.getName() + "-->\n" + getComponents(context, item);
                channel = item.getChannel();
                ArrayList comps = new ArrayList(item.getComponents());
                if (this.order != null)
                    Collections.sort(comps, new ComponentsTagSort(this.order));

                if (this.limit > 0 && comps.size() > this.limit)
                    return new Treble(comps.subList(0, this.limit), item, channel);
                else
                    return new Treble(comps, item, channel);

            } else if (channel != null || this.channels != null) {

                // return "\n<!-- Files belonging to channel: " + channel.getName() + "-->\n" + getComponents(context,
                // channel);
                ArrayList components = new ArrayList();

                Collection items = null;
                if (channel != null)
                    items = channel.getNewsItems();
                else if (this.channels != null) {
                    Iterator i = this.channels.iterator();
                    items = new ArrayList();
                    while (i.hasNext()) {
                        NewsChannel c = NewsChannelHome.findByName(i.next().toString());
                        items.addAll(c.getNewsItems());
                    }
                } else {
                    // Where do we get the components from !!!!??
                }

                Iterator i = items.iterator();
                while (i.hasNext()) {
                    NewsItem it = (NewsItem) i.next();
                    components.addAll(it.getComponents());
                }

                if (this.order != null)
                    Collections.sort(components, new ComponentsTagSort(this.order));

                if (this.limit > 0 && components.size() > this.limit)
                    return new Treble(components.subList(0, this.limit), null, channel);
                else
                    return new Treble(components, null, channel);

            }

        }

        return null;

    }

    private String getComponents(PageContext context, Collection components, NewsItem componentsItem,
            NewsChannel componentsChannel) throws ContentProcessorException, NewsException, ComponentException {

        String channelName = (componentsChannel != null) ? componentsChannel.getName() : null;
        String itemId = (componentsItem != null) ? componentsItem.getId() : null;

        if (components.size() > 0 || this.alwaysShowHeader) {

            // String out = "\n<!-- Components for Item " + item.getId() +
            // "-->";

            String out = "";
            out += "\n<table class=\"" + this.tableStyle + "\">";

            // Number of fields
            int nFields = (this.showImage ? 1 : 0) + (this.showName ? 1 : 0) + (this.showSize ? 1 : 0)
                    + (this.fields == null ? 3 : this.fields.size()) + (this.showDetails ? 1 : 0);
            // Calculate columns for the header and footer
            int columns = nFields;
            if (layout == GRID_LAYOUT)
                columns = gridColumns * nFields;

            int columnsLeft = columns + 3;
            int columnsRight = 0;

            boolean canShowUploadLink = this.showUploadLink && channelName != null && itemId != null;
            if (canShowUploadLink) {
                columnsLeft = (columns / 2) + (columns % 2);
                columnsRight = (columns / 2);
            }

            if (this.showTitle)
                out += "\n<tr class=\"title\"><th colspan=\"" + columnsLeft + "\"> &nbsp;"
                        + this.getTitle(context, components.size()) + "</th>";

            if (canShowUploadLink)
                out += "<th style=\"text-align: right\" colspan=\"" + columnsRight
                        + "\"> <a href='#' onclick=window.open('" + TagUtils.getResource(context, "news.basepath", "")
                        + "/edit/" + channelName + "/insert_form/" + itemId
                        + "','component_edit','scrollbars=yes,toolbar=no,width=510,height=490,top=260,left=350')>"
                        + this.uploadTitle + "</a></th>";

            out += "</tr>";

            if (this.showFieldTitles && this.fieldTitles != null && components.size() > 0) {
                out += "<tr class=\"fieldTitles\">";
                Iterator i = this.fieldTitles.iterator();
                while (i.hasNext()) {
                    out += "<th>" + i.next() + "</th>";
                }
                out += "</tr>";
            }

            ContentProcessingOptions options = ContentProcessorService.createProcessingOptions();
            options.setOutputContentType("text/html");
            options.setDisposition(ContentProcessingOptions.SHOW_EMBEDDED);
            if (this.imageWidth > 0)
                options.setImageWidth(this.imageWidth);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            try {
                boolean even = false;
                Iterator i = components.iterator();
                int ii = 0;
                while (i.hasNext()) {

                    Component component = (Component) i.next();

                    String localChannelName = component.getChannelName();
                    String localItemId = component.getItemId();

                    String componentLink = TagUtils.getResource(context, "news.basepath", "") + "/get/"
                            + localChannelName + "/" + localItemId + "/" + component.getId();
                    String componentFileLink = componentLink + (this.linkToDetails ? "" : "?showfile=true");

                    String styleClass = (even) ? this.rowEvenStyle : this.rowOddStyle;

                    if (this.layout == ROWS_LAYOUT || (ii % gridColumns == 0))
                        out += "\n<tr class=\"" + styleClass + "\">";

                    if (this.showImage) {
                        out += "<td>";
                        out += "<a href=\"" + componentFileLink + "\">"
                                + ContentProcessorService.process(component, options).getStringRepresentation()
                                + "</a>";
                        out += "</td>";
                    }
                    if (this.showSize)
                        out += "<td>&nbsp;" + Format.formatSize(component.getFile().getPhysicalFileSize())
                                + "&nbsp;</td>";
                    if (this.showName)
                        out += "<td><a href=\"" + componentFileLink + "\">" + component.getName() + "</a></td>";

                    if (this.fields == null) {
                        out += "<td>" + component.getTitle() + "</td>";
                        String author = component.getModifiedBy();
                        if (author == null || "".equals(author))
                            author = component.getAuthor();
                        out += "<td>" + author + "</td>";
                        out += "<td>" + format.format(component.getModificationDate()) + "</td>";
                    } else {
                        int nField = 0;
                        Iterator f = this.fields.iterator();
                        while (f.hasNext()) {
                            String fieldName = f.next().toString();
                            String style = (this.styles != null && this.styles.size() > nField)
                                    ? (" class=\"" + this.styles.get(nField).toString() + "\"") : "";
                            boolean linked = this.linkedFields != null && this.linkedFields.contains(fieldName);
                            out += "<td " + style + ">" + (linked ? "<a href=\"" + componentFileLink + "\">" : "")
                                    + TagUtils.getFieldValueFromBean(context, component, fieldName)
                                    + (linked ? "</a>" : "") + "</td>";
                            nField++;
                        }
                    }

                    if (this.showDetails)
                        out += "<td><a href=\"" + componentLink + "\">[details]</a></td>";

                    if (this.layout == ROWS_LAYOUT || ((ii % gridColumns == gridColumns - 1)))
                        out += "</tr>";

                    even = !even;
                    ii++;

                }

                if (this.layout == GRID_LAYOUT && ((ii % gridColumns != 0)))
                    out += "</tr>";

                if (this.showFooter)
                    out += "\n<tr class=\"footer\"><th colspan=\"" + columnsLeft + "\"> &nbsp;"
                            + this.getFooter(context, components.size()) + "</th>";

            } catch (ModelException e) {
                log.error("Problem getting components", e);
            }

            out += "\n</table>";

            // out += "<!-- End of Components for Item " + item.getId() + "-->";
            return out + "\n";

        } else {
            return "<!-- No files attached -->";
        }

    }

    private String getTitle(PageContext context, int size) {

        if (this.title != null)
            return this.title;
        else if (this.titleKey != null)
            return TagUtils.getResource(context, this.titleKey, "");
        else
            return "No Title";

    }

    private String getFooter(PageContext context, int size) {

        if (this.footer != null)
            return this.footer;
        else
            return "Listed " + size + " files";

    }

    private class ComponentsTagSort implements Comparator {

        ArrayList order = null;

        private ComponentsTagSort(Collection o) {
            this.order = new ArrayList(o.size());
            Iterator i = o.iterator();
            while (i.hasNext()) {
                StringTokenizer tok = new StringTokenizer(i.next().toString(), " ");
                if (tok.countTokens() > 1)
                    this.order.add(new StringPair(tok.nextToken(), tok.nextToken().toUpperCase()));
                else
                    this.order.add(new StringPair(tok.nextToken(), "DESC"));
            }
        }

        public int compare(Object o1, Object o2) {

            if (o1 instanceof Component && o2 instanceof Component) {
                Component c1 = (Component) o1;
                Component c2 = (Component) o2;
                if (this.order == null || this.order.size() < 1)
                    return c2.getModificationDate().compareTo(c1.getModificationDate());
                else {
                    try {
                        // FIXME: Use ALL order elements, not just the first.
                        StringPair s = (StringPair) this.order.get(0);
                        String f1 = ModelUtils.getFieldValueFromBean(c1, s.getFirst()).toString();
                        String f2 = ModelUtils.getFieldValueFromBean(c2, s.getFirst()).toString();
                        if ("ASC".equals(s.getSecond()))
                            return f1.compareTo(f2);
                        else
                            return f2.compareTo(f1);

                    } catch (Exception e) {
                        log.warn("Problem sorting: " + this.order, e);
                        return 0;
                    }

                }

            } else
                return 0;

        }

    }

}