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

package ecmwf.ecpds.master.plugin.http.view.content;

/**
 * Gets and draws HTML for an ICON. It may come from
 * <ul>
 * <li>A ModelBean attribute (defined by name and property), representing a file name.
 * The icon will be the one which represents the file type</li>
 * <li></li>
 *
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.9.0
 */

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.news.NewsItemHome;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.model.news.NewsObjectNotFoundException;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

public class ContentSelectorTag extends TagSupport {

    private static Log log = LogFactory.getLog(ContentSelectorTag.class);
    private Collection items = null;
    private NewsChannel currentChannel = null;
    private NewsItem currentItem = null;
    private String tableClass = "spareBox2";
    private String mask = "";
    private String inFormat = "";
    private String outFormat = "";
    private SimpleDateFormat inDf = null;
    private SimpleDateFormat outDf = null;
    private String title = "Default Title";
    private String type = "";
    private String linkBase = null;
    private int size = 5;

    public void setItems(Collection items) {
        this.items = items;
    }

    public void setItem(NewsItem item) {
        this.currentItem = item;
    }

    public void setChannel(NewsChannel c) {
        this.currentChannel = c;
    }

    public void setTableClass(String value) {
        this.tableClass = value;
    }

    public void setMask(String value) {
        this.mask = value;
    }

    public void setOutFormat(String outFormat) {
        this.outFormat = outFormat;
    }

    public void setInFormat(String inFormat) {
        this.inFormat = inFormat;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public void setType(String value) {
        this.type = value;
    }

    public void setSize(String value) {
        this.size = Integer.parseInt(value);
    }

    public int doStartTag() {

        try {
            if (this.items != null && this.items.size() > 0) {
                this.linkBase = TagUtils.getResource(pageContext, "news.basepath", "")
                        + this.currentChannel.getAccessPath();
                writeBox(this.currentItem, this.items, pageContext.getOut());
            }
            return SKIP_BODY;

        } catch (IOException e) {
            log.error("Error Writing Output", e);
            return SKIP_BODY;
        }

    }

    public int doAfterBody() throws JspTagException {
        return SKIP_BODY;
    }

    private void writeBox(NewsItem item, Collection items, Writer out) throws IOException {

        // HashMap values = new HashMap();
        ArrayList names = new ArrayList();
        Iterator i = items.iterator();
        String currVal = item != null ? (applyMask(item.getName(), mask, false)) : null;

        while (i.hasNext()) {
            NewsItem it = (NewsItem) i.next();
            String value = applyMask(it.getName(), mask, false);
            if (!names.contains(value)) {
                names.add(value);
            }
        }

        boolean select = (names.size() > this.size);

        out.write("<table class=\"" + this.tableClass + "\">");
        if (title != null)
            out.write("<tr><th>" + title + "</th></tr>");

        if (select)
            out.write(
                    "<tr><td><select onchange=\"if (this.value!='') { window.location.href=this.value} else {alert('Page not ready yet')}\" style=\"width:100%\">");

        i = names.iterator();
        NewsItem current = this.currentItem != null ? this.currentItem : (NewsItem) items.iterator().next();
        while (i.hasNext()) {
            String value = i.next().toString();
            String link = getLink(value, mask, current);
            String label = getLabel(value, this.type);

            if (currVal != null && currVal.equals(value))
                if (select)
                    out.write("<option value=\"" + link + "\" selected>" + label + "</option>");
                else
                    out.write("<tr><td>" + label + "</td></tr>");
            else if (select)
                out.write("<option value=\"" + link + "\">" + label + "</option>");
            else {
                if (link.equals(""))
                    link = "javascript:alert('Page not ready yet')";
                out.write("<tr><td><a href=\"" + link + "\">" + label + "</a></td></tr>");
            }
        }
        if (select)
            out.write("</select></td></tr>");
        out.write("</table>");

    }

    private static String applyMask(String value, String mask, boolean reverse) {

        String out = "";
        for (int i = 0; i < mask.length() && i < value.length(); i++) {
            if (!reverse && mask.charAt(i) != '#' || reverse && mask.charAt(i) == '#')
                out += value.charAt(i);
        }
        return out;

    }

    private String getLabel(String value, String type) {

        if ("date".equals(type)) {
            try {
                if (this.inDf == null)
                    this.inDf = new SimpleDateFormat(this.inFormat);
                if (this.outDf == null)
                    this.outDf = new SimpleDateFormat(this.outFormat);
                return this.outDf.format(inDf.parse(value));
            } catch (Exception e) {
                log.error("Problem formatting label", e);
                return value;
            }
        } else
            return value;

    }

    private static String fillUpMask(String mask, String masked, String unmasked) {

        try {
            int consumedFromMasked = 0, consumedFromUnmasked = 0;
            StringBuffer result = new StringBuffer(mask);
            for (int i = 0; i < result.length(); i++) {
                result.setCharAt(i, (result.charAt(i) == '#') ? masked.charAt(consumedFromMasked++)
                        : unmasked.charAt(consumedFromUnmasked++));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("Problem filling up mask '" + mask + "' with values masked '" + masked + "' and unmasked '"
                    + unmasked + "'", e);
            return mask;
        }

    }

    private String getLink(String value, String mask, NewsItem selectedItem) {
        String link = fillUpMask(mask, applyMask(selectedItem.getName(), mask, true), value);
        try {
            NewsItemHome.findByNameAndChannelId(link, this.currentChannel.getId());
        } catch (NewsException e) {
            // Maybe not ready yet!!
            if (log.isTraceEnabled())
                log.trace("Not Found", e);
            return "";
        }
        return this.linkBase + "/" + link;
    }

}
