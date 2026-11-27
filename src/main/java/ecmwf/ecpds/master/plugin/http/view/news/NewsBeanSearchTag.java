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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.news.NewsChannelHome;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.util.Util;
import ecmwf.ecpds.master.plugin.http.view.general.ModelBeanSearchTag;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * NewsBeanSearchTag.java
 *
 * This class implements a JSP Tag whose main objective is to take a Collection of NewsItemBean and display them in a
 * table using some supplied parameters.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public class NewsBeanSearchTag extends ModelBeanSearchTag {

    private static Log log = LogFactory.getLog(NewsBeanSearchTag.class);
    private boolean useColorsFromPriority = false;
    private Collection channels;
    private boolean unrollItems = false;

    public NewsBeanSearchTag() {
        // Default values
        this.setTheClass(NewsItem.class.getName());
        this.setLink("/get/%{item.channel.name}/%{item.id}");
        this.setBasePathKey("news.basepath");
        this.setShowLink("true");
    }

    public void setChannels(String channels) {
        this.channels = Util.listStringToCollection(TagUtils.parseMultiExpressionText(pageContext, channels));
        if (log.isDebugEnabled())
            log.debug("NewsItemsBeanSearch: Channels set to: " + this.channels);
    }

    public Collection getResults() throws ModelException {
        if (this.channels != null) {
            this.setChannelFilter();
        }
        return super.getResults();

    }

    public void setUnrollItems(String value) {
        this.unrollItems = value.toLowerCase().trim().equals("true");
    }

    public void setUseColorsFromPriority(String value) {
        this.useColorsFromPriority = value.toLowerCase().trim().equals("true");
    }

    public void prePrintBeanHook(ModelBean bean) {

        try {
            NewsItem nib = (NewsItem) bean;
            if (this.useColorsFromPriority)
                this.setColorsFromPriority(nib.getPriority());
        } catch (Exception e) {
            log.error("Problem getting priority from a NewsItem", e);
        }
    }

    // ***** PRIVATE *************************************************************************************** */

    private void setColorsFromPriority(int priority) {

        /*
         * String foreground; String background;
         *
         * try { background = ConfigService.getParameter("news", "priority." + priority + ".color.background");
         * foreground = ConfigService.getParameter("news", "priority." + priority + ".color.text"); } catch
         * (ConfigException e) { log.warn( "Problem getting color for priority '" + priority + "': " ,e); background =
         * "#ffffff"; foreground = "#000000"; }
         *
         * //log.debug("Priority '"+priority+"' maps to '"+foreground+"','"+background+"'.");
         * this.setBackgroundColor(background); this.setForegroundColor(foreground);
         */
        this.setInnerTableClass("priority" + priority);

    }

    private void setChannelFilter() throws ModelException {

        String query = "";
        Iterator i = this.channels.iterator();
        while (i.hasNext()) {
            NewsChannel c = NewsChannelHome.findByName((String) i.next());
            query += "channel_id=" + c.getId() + " ";
            if (i.hasNext())
                query += " or ";
        }

        if (!this.unrollItems)
            query = "(" + query + ") and not isComment";

        this.setFilter(query);
        log.debug("NewsItemsBeanSearch: Calculated query is: " + query);

    }

}
