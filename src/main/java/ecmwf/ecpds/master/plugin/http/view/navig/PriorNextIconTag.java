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

/**
 * Draws a link to the prior or next OrderedBean to the supplied one.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.9.0
 */

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.OrderedBean;
import ecmwf.ecpds.master.plugin.http.util.Util;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

public class PriorNextIconTag extends BodyTagSupport {

    private static Log log = LogFactory.getLog(PriorNextIconTag.class);
    private String name = null;
    private String property = null;
    private String iconKey = null;
    private String basePathKey = null;
    private String href = null;
    private boolean back = false;
    private String defaultValue = null;
    private String styleClass = null;
    private String order = null;

    public void setName(String value) {
        this.name = value;
    }

    public void setProperty(String value) {
        this.property = value;
    }

    public void setIconKey(String value) {
        this.iconKey = value;
    }

    public void setBasePathKey(String value) {
        this.basePathKey = value;
    }

    public void setHref(String value) {
        this.href = value;
    }

    public void setPrior(String value) {
        this.back = "true".equals(value);
    }

    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    public void setStyleClass(String value) {
        this.styleClass = value;
    }

    public void setOrder(String value) {
        this.order = value;
    }

    /**
     *
     *
     * @return What to do after... DO NOT eval the body
     */

    public int doStartTag() throws JspException {

        try {

            Exception ex;
            try {

                String fullName = ((this.name != null) ? this.name : "")
                        + ((this.property != null) ? ("." + this.property) : "");
                Object o = TagUtils.resolveQualifiedName(pageContext, fullName);
                if (o instanceof OrderedBean) {
                    OrderedBean bean = this.getSibling((OrderedBean) o);
                    pageContext.setAttribute((this.back) ? "prior" : "next", bean);
                    String href = TagUtils.getLink(pageContext, this.href, this.basePathKey);
                    pageContext.getOut().print(getLinkOpening(href));
                    pageContext.getOut().print(getLinkBody());
                    return EVAL_BODY_TAG;
                } else {
                    log.debug("The object point by 'name' and 'property' attribute is NOT and OrderedBean.");
                }

                return SKIP_BODY;

            } catch (ClassCastException e) {
                log.error("Expected a 'Content' instance", e);
                ex = e;
            } catch (Exception e) {
                log.error("Error getting data from bean", e);
                ex = e;
            }

            if (this.defaultValue != null)
                pageContext.getOut().println(this.defaultValue);
            else
                throw new JspException(ex.getMessage());

            return SKIP_BODY;

        } catch (IOException e) {
            log.error("IOError writing Converted Content to screen", e);
            return SKIP_BODY;
        }

    }

    public int doAfterBody() {

        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException e) {
            log.error("IOException writing page content", e);
        }
        return SKIP_BODY;

    }

    public int doEndTag() {

        try {
            pageContext.getOut().print(getLinkClosing());
        } catch (IOException e) {
            log.error("IOException writing page content", e);
        }
        return EVAL_PAGE;

    }

    // PRIVATE ************************************************************

    private OrderedBean getSibling(OrderedBean ob) throws ModelException {
        if (this.order != null) {
            ModelSearch search = ModelHomeBase.getDefaultSearch();
            search.setSortItemsFromStrings(Util.listStringToCollection(this.order));
            return (this.back) ? ob.getPriorWithinSort(search.getSortItems())
                    : ob.getNextWithinSort(search.getSortItems());
        } else
            return (this.back) ? ob.getPrior() : ob.getNext();
    }

    private String getLinkOpening(String href) {
        return "<a " + ((this.styleClass != null) ? "class=\"" + this.styleClass + "\"" : "") + " href=\"" + href
                + "\">";
    }

    private String getLinkClosing() {
        return "</a>";
    }

    private String getLinkBody() {
        return "<img src=\"" + TagUtils.getResource(pageContext, this.iconKey, "icon.unknown") + "\" border=\"0\"/>";
    }

}
