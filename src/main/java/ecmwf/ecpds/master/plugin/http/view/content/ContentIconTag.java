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
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

// import ecmwf.ecpds.master.plugin.http.services.log.LogService;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorService;

public class ContentIconTag extends TagSupport {

    private static Log log = LogFactory.getLog(ContentIconTag.class);
    private String src = null;
    private String name = null;
    private String property = null;
    private String key = null;
    private String altKey = null;
    private String titleKey = null;
    private String alt = null;
    private String title = null;
    private String defaultValue = null;
    private boolean writeFullTag = false;
    private String height = null;
    private String width = null;

    public void setSrc(String value) {
        this.src = value;
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setProperty(String value) {
        this.property = value;
    }

    public void setKey(String value) {
        this.key = value;
    }

    public void setAltKey(String value) {
        this.altKey = value;
    }

    public void setTitleKey(String value) {
        this.titleKey = value;
    }

    public void setAlt(String value) {
        this.alt = value;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    public void setWriteFullTag(String value) {
        this.writeFullTag = "true".equals(value);
    }

    public void setWidth(String value) {
        this.width = value;
    }

    public void setHeight(String value) {
        this.height = value;
    }

    /**
     *
     * @return What to do after... DO NOT eval the body
     */

    public int doStartTag() throws JspException {

        try {

            Exception ex;
            try {

                String image = "";

                if (this.src != null) {
                    image = TagUtils.parseMultiExpressionText(pageContext, this.src);
                } else if (this.name != null && this.property != null) {
                    Object o = TagUtils.resolveQualifiedName(pageContext, this.name);

                    String value = ModelUtils.getFieldValueFromBean(o, this.property).toString();
                    image = ContentProcessorService.getIconFromExtension(value);

                } else if (key != null) {
                    image = TagUtils.getResource(pageContext, TagUtils.parseMultiExpressionText(pageContext, this.key),
                            this.defaultValue != null ? this.defaultValue : "");
                } else {
                    throw new JspException(
                            "ContentIconTag: Expected a 'name' + 'property' or 'key' attribute combination.");
                }

                if (this.writeFullTag) {
                    image = "<img src=\"" + image + "\" border=\"0\"" + getAlt() + getTitle() + getHeight() + getWidth()
                            + " />";
                }

                pageContext.getOut().print(image);

                return SKIP_BODY;

            } catch (ClassCastException e) {
                log.error("ContentIconTag: Expected a 'Content' instance. Got : ", e);
                ex = e;
            } catch (Exception e) {
                log.error("ContentIconTag: Error getting data from bean : ", e);
                ex = e;
            }

            if (this.defaultValue != null)
                pageContext.getOut().println(this.defaultValue);
            else
                throw new JspException(ex.getMessage());

            return SKIP_BODY;

        } catch (IOException e) {
            log.error("ContentIconTag: IOError writing Converted Content to screen: ", e);
            return SKIP_BODY;
        }

    }

    /**
     * Hook method called by the taglib container at the end of the tag body, and after all of it has been evaluated. We
     * first write out the body content, and then call SKIP_BODY to avoid further body evaluations (as would be the
     * case, for example, of a loop tag).
     *
     * @return What to do after... SKIP_BODY
     */

    public int doAfterBody() throws JspTagException {

        return SKIP_BODY;

    }

    /* ******************************************************************************* */

    private String getAlt() {
        if (this.alt != null)
            return " alt=\"" + TagUtils.parseMultiExpressionText(pageContext, this.alt) + "\"";
        else if (this.altKey != null)
            return " alt=\"" + TagUtils.getResource(pageContext, this.altKey, "Alt '" + this.altKey + "' not Found")
                    + "\"";
        else
            return "";
    }

    private String getTitle() {
        if (this.title != null)
            return " title=\"" + TagUtils.parseMultiExpressionText(pageContext, this.title) + "\"";
        if (this.titleKey != null)
            return " title=\""
                    + TagUtils.getResource(pageContext, this.titleKey, "Title '" + this.titleKey + "' not Found")
                    + "\"";
        else
            return "";
    }

    private String getHeight() {
        if (this.height == null)
            return "";
        else
            return " height=\"" + this.height + "\"";
    }

    private String getWidth() {
        if (this.width == null)
            return "";
        else
            return " width=\"" + this.width + "\"";
    }

}
