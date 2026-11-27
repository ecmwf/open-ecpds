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

package ecmwf.ecpds.master.plugin.http.services.content.text;

import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessor;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorBase;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.util.WikiWordSubstitution;

/**
 * PlainTextContentProcessor.java
 *
 * Created: Mon Jan 13 13:27:18 2003
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla </a>
 *
 * @version 0.1.0
 *
 */

public class PlainTextContentProcessor extends ContentProcessorBase implements ContentProcessor {

    // private static Log log = LogFactory.getLog(PlainTextContentProcessor.class);
    private static final String HTML_CR = "<br />";
    private boolean detectWikiWords = true;

    public Content process(Content content, ContentProcessingOptions options) throws ContentProcessorException {
        return getIconContent(content, options);
    }

    public Content process(Content origin, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        try {
            String content = ModelUtils.getFieldValueFromBean(origin, attribute).toString();
            if (content == null)
                throw new ContentProcessorException("PlainTextContentProcessor: Can't process null field: '" + attribute
                        + "' from '" + origin + "'");
            return createContent(this.processText(origin, content, options));
        } catch (ModelException e) {
            throw new ContentProcessorException("HTMLContentProcessor: Error converting content: " + e.getMessage());
        }
    }

    public void validate(Content origin, ContentProcessingOptions options) throws ContentProcessorException {
        // throw new ContentProcessorException("validate(Content,
        // ContentProcessingOptions) hasn't been implemented by '" +
        // this.getClass().getName() + "'");
    }

    public void validate(Content origin, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        // throw new ContentProcessorException("validate(Content, String,
        // ContentProcessingOptions) hasn't been implemented by '" +
        // this.getClass().getName() + "'");
    }

    private String processText(Content origin, String content, ContentProcessingOptions options)
            throws ContentProcessorException {

        String outputContentType = options.getOutputContentType();
        if ("text/html".equals(outputContentType)) {
            StringBuffer buffer = new StringBuffer(content);
            parseText(buffer, options.getMaxLines());
            if (this.detectWikiWords)
                return WikiWordSubstitution.parseWikiWords(buffer.toString(), origin);
            else
                return buffer.toString();
        } else {
            return content;
        }
    }

    /*
     * private String getText(Content origin) throws ContentProcessorException {
     *
     * StringBuffer out = new StringBuffer(); try { InputStream is = origin.getInputStream(); byte[] buffer = new
     * byte[1024]; int read = 0; while ((read = is.read(buffer)) > 0) { out.append(new String(buffer, 0, read)); }
     * is.close(); return out.toString(); } catch (Exception e) { throw new ContentProcessorException("Problem
     * processing text file", e); } }
     */

    public void validate(String content) throws ContentProcessorException {
        return;
    }

    /**
     * Return a conversion TEXT->HTML, which basically consists in changing CR into " <br/>
     * ", while taking in consideration number of line limitations.
     *
     * @param buffer
     *            The original buffer, modified in place
     * @param maxLines
     *            The maximum number of lines we'll allow in the buffer.
     */

    private static void parseText(StringBuffer buffer, int maxLines) {

        char current;
        int lines = 0;

        for (int i = 0; i < buffer.length(); i++) {

            current = buffer.charAt(i);
            if (current == '\n') {
                lines++;
                htmlLineBreak(i, buffer);
                if (lines >= maxLines) {
                    buffer.delete(i + HTML_CR.length(), buffer.length());
                    buffer.append(" ...");
                    return;
                }
            }

        }

    }

    private static void htmlLineBreak(int i, StringBuffer buffer) {

        buffer.replace(i, i + 1, HTML_CR);

    }

} // PlainTextContentProcessor
