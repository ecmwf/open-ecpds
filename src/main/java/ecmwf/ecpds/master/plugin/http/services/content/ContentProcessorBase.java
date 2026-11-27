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

package ecmwf.ecpds.master.plugin.http.services.content;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessor;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorOptions;

/**
 * Base implementation for content processors.
 *
 * @author Daniel Varela Santoalla
 */
public class ContentProcessorBase implements ContentProcessor {
    private static Log log = LogFactory.getLog(ContentProcessorBase.class);
    Properties props = null;

    public void configure(ContentProcessorOptions props) {
        this.props = props;
    }

    public void setProperty(String name, String value) {
        if (props == null)
            props = new Properties();
        this.props.put(name, value);
    }

    public static Content createContent(String content) {
        return new DefaultContent(content);
    }

    public static Content getUnknownContent(String name) {
        DefaultContent c = new DefaultContent(name, "application/unknown");
        return c;
    }

    public Content process(Content origin, ContentProcessingOptions options) throws ContentProcessorException {
        if (options.getOutputContentType().equals("text/html")) {
            String output = "";
            if (options.getDisposition() == ContentProcessingOptions.SHOW_EMBEDDED)
                output = "<a href=\"" + origin.getBasePathKey() + origin.getAccessPath() + "\">";
            output += origin.getStringRepresentation();
            if (options.getDisposition() == ContentProcessingOptions.SHOW_EMBEDDED)
                output += "</a>";
            return createContent(output);
        } else {
            return createContent(origin.getStringRepresentation());
        }
    }

    public Content getIconContent(Content content, ContentProcessingOptions options) {
        String output = "";
        String icon = "";
        String contentType = content.getContentType();
        if (contentType != null) {
            try {
                if (contentType != null && contentType.indexOf("/") > 0)
                    icon = ConfigService.getParameter("uploads",
                            "icons." + contentType.substring(contentType.indexOf("/") + 1, contentType.length()));
            } catch (ConfigException e) {
                log.debug("Icon for content type '" + content.getContentType()
                        + "' not found. Please edit content.properties and add it.", e);
                icon = ConfigService.getOptionalParameter("uploads", "icons.default", "NODEFAULT");
            }
            if (options.getDisposition() == ContentProcessingOptions.SHOW_EMBEDDED)
                output = "<a href=\"" + content.getBasePathKey() + content.getAccessPath() + "\">";
            output += "<img src=\"" + icon + "\" border=\"0\" title=\"" + content.getTitle() + "\" alt=\""
                    + content.getContentType() + " Icon\" >";
            if (options.getContextLabel() != null)
                output += options.getContextLabel();
            if (options.getDisposition() == ContentProcessingOptions.SHOW_EMBEDDED)
                output += "</a>";
        }
        return createContent(output);
    }

    public Content getStructure(Content origin, String attribute, int startLevel, ContentProcessingOptions options)
            throws ContentProcessorException {
        throw new ContentProcessorException(
                "getStructure(Content, attribute, startLevel, ContentProcessingOptions) hasn't been implemented by '"
                        + this.getClass().getName() + "'");
    }

    public Content process(Content origin, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        throw new ContentProcessorException(
                "process(Content, attribute, ContentProcessingOptions) hasn't been implemented by '"
                        + this.getClass().getName() + "'");
    }

    public void validate(Content origin, ContentProcessingOptions options) throws ContentProcessorException {
        throw new ContentProcessorException("validate(Content, ContentProcessingOptions) hasn't been implemented by '"
                + this.getClass().getName() + "'");
    }

    public void validate(Content origin, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        throw new ContentProcessorException(
                "validate(Content,String, ContentProcessingOptions) hasn't been implemented by '"
                        + this.getClass().getName() + "'");
    }
}
