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
 * Implements a user information tag. It takes its info from the WebUserBean instance stored in the session.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.9.0
 */

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

public class ContentProcessTag extends TagSupport {

    private static Log log = LogFactory.getLog(ContentProcessTag.class);
    private String name = "";
    private String property = null;
    private String altProperty = null;
    private int altPropertyLength = 0;
    private int maxLength = -1;
    private String inputContentType = null;
    private String outputContentType = "text/plain";
    private String defaultValue = null;
    private boolean thumbnail = false;
    private boolean embedded = false;
    private SimpleDateFormat sdf = null;
    private boolean relativeTime = false;
    private boolean ignoreNull = false;

    public void setName(String value) {
        this.name = value;
    }

    public void setProperty(String value) {
        this.property = value;
    }

    public void setAltProperty(String value) {
        this.altProperty = value;
    }

    public void setAltPropertyLength(String value) {
        try {
            this.altPropertyLength = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            this.altPropertyLength = 0;
            log.warn("Bad value for 'altPropertyLength' property.", e);
        }
    }

    public void setDateFormat(String value) {
        this.sdf = new SimpleDateFormat(value);
    }

    public void setDateFormatKey(String value) {
        this.sdf = new SimpleDateFormat(TagUtils.getResource(pageContext, value, "dd MMM yyyy"));
    }

    public void setRelativeTime(String value) {
        this.relativeTime = "yes".equals(value) || "true".equals(value);
    }

    public void setOutputContentType(String value) {
        this.outputContentType = value;
    }

    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    public void setThumbnail(String value) {
        this.thumbnail = "yes".equals(value) || "true".equals(value);
    }

    public void setEmbedded(String value) {
        this.embedded = "yes".equals(value) || "true".equals(value);
    }

    public void setIgnoreNull(String value) {
        this.ignoreNull = "yes".equals(value) || "true".equals(value);
    }

    public void setMaxLength(String value) {
        try {
            this.maxLength = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("Problem parsing number for property 'maxLength'", e);
        }
    }

    public int doStartTag() throws JspException {

        try {

            Exception ex;
            try {

                // Try to find the object with name "this.name", in all contexts.
                Object o = TagUtils.resolveQualifiedName(pageContext, this.name, this.ignoreNull);
                if (o == null)
                    return SKIP_BODY;

                pageContext.getOut()
                        .print(TagUtils.getProcessFieldContent(o, this.property, this.altProperty, pageContext,
                                this.outputContentType, this.thumbnail, this.embedded, this.sdf, this.relativeTime,
                                this.maxLength));
                return SKIP_BODY;

            } catch (Exception e) {
                log.error("Exception processing Content.", e);
                ex = e;
            }

            if (this.defaultValue != null) {
                if (log.isDebugEnabled())
                    log.debug("Error converting content (displaying default value '" + this.defaultValue + "')", ex);
                pageContext.getOut().println(this.defaultValue);
            } else {
                log.error("Error converting content.", ex);
                throw new JspException(ex.getMessage());
            }

            return SKIP_BODY;

        } catch (IOException e) {
            log.error("IOError writing Converted Content to screen: ", e);
            return SKIP_BODY;
        }

    }

    public int doAfterBody() throws JspTagException {
        return SKIP_BODY;
    }

}
