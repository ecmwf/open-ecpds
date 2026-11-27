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

package ecmwf.ecpds.master.plugin.http.services.content.html;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Util;

import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessor;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorBase;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.util.bean.StringPair;

/**
 * HTMLContentProcessor.java
 *
 * Created: Mon Jan 13 13:27:18 2003
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 *
 */

public class HTMLContentProcessor extends ContentProcessorBase implements ContentProcessor {

    private static Log log = LogFactory.getLog(HTMLContentProcessor.class);
    protected static final String TOC_ANCHOR_PREFIX = "TOC_Position_";

    private Pattern patternProcessingInstructions = null;
    private Pattern patternCommandTag = null;
    private Pattern patternCommandTagWithBody = null;
    private Pattern patternHeaderExtractor = null;

    public HTMLContentProcessor() {

        super();
        try {
            PatternCompiler compiler = new Perl5Compiler();
            patternProcessingInstructions = compiler.compile(
                    ConfigService.getParameter("content", "html.processingInstruction.regexp"),
                    Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
            patternCommandTag = compiler.compile(ConfigService.getParameter("content", "html.command.regexp"),
                    Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
            patternCommandTagWithBody = compiler.compile(
                    ConfigService.getParameter("content", "html.commandWithBody.regexp"),
                    Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
            patternHeaderExtractor = compiler.compile(
                    ConfigService.getParameter("content", "html.headerExtractor.regexp"),
                    Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK);
        } catch (MalformedPatternException e) {
            log.error("Problem with parameter matching regexp", e);
        } catch (ConfigException e) {
            log.error("Problem configuring parameter matching regexp", e);
        }

    }

    public Content process(Content content, ContentProcessingOptions options) throws ContentProcessorException {
        return getIconContent(content, options);
    }

    public Content process(Content origin, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        try {
            return createContent(
                    parseHTML(origin, ModelUtils.getFieldValueFromBean(origin, attribute).toString(), options));
        } catch (ModelException e) {
            throw new ContentProcessorException("Error transforming content", e);
        }
    }

    public void validate(Content origin, ContentProcessingOptions options) throws ContentProcessorException {
        this.validate(origin.getStringRepresentation());
    }

    public void validate(Content origin, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        this.validate(origin.getStringRepresentation());
    }

    public Content getStructure(Content content, String attribute, int startLevel, ContentProcessingOptions options)
            throws ContentProcessorException {
        try {
            if ("text/html".equals(options.getOutputContentType())) {
                return createContent(
                        this.getTocInHTML(ModelUtils.getFieldValueFromBean(content, attribute).toString(), startLevel));
            } else {
                throw new ContentProcessorException("Getting an HTML TOC in '" + options.getOutputContentType()
                        + "' is not supported yet. Please contact the development team.");
            }
        } catch (ModelException e) {
            throw new ContentProcessorException("Error accessing content.", e);
        }

    }

    // ** Private *********************************************************

    private void validate(String content) throws ContentProcessorException {
        // The legacy parser is optional and is not included in OpenECPDS.
        try {
            Class<?> parserClass = Class.forName("com.quiotix.html.parser.HtmlParser");
            Object parser = parserClass.getConstructor(java.io.InputStream.class)
                    .newInstance(new ByteArrayInputStream(content.getBytes()));
            Object document = parserClass.getMethod("HtmlDocument").invoke(parser);
            var elements = document.getClass().getDeclaredField("elements");
            elements.setAccessible(true);
            validateSequence(elements.get(document));
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError("com/quiotix/html/parser/ParseException");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error)
                throw (Error) cause;
            if (cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            if (cause != null && cause.getClass().getName().equals("com.quiotix.html.parser.ParseException")) {
                throw new ContentProcessorException("HTML Content invalid: ",
                        new Exception("Problem parsing HTML content: ", cause));
            }
            throw new IllegalStateException("Cannot invoke HTML parser", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke HTML parser", e);
        }
    }

    private static void validateSequence(Object sequence) throws ReflectiveOperationException {
        // Preserve the legacy link visitor's handling of anchor and image attributes.
        Iterator<?> iterator = (Iterator<?>) sequence.getClass().getMethod("iterator").invoke(sequence);
        while (iterator.hasNext()) {
            Object element = iterator.next();
            String type = element.getClass().getName();
            if (type.equals("com.quiotix.html.parser.HtmlDocument$TagBlock")) {
                validateTag(element.getClass().getField("startTag").get(element));
                validateSequence(element.getClass().getField("body").get(element));
            } else if (type.equals("com.quiotix.html.parser.HtmlDocument$Tag")) {
                validateTag(element);
            }
        }
    }

    private static void validateTag(Object tag) throws ReflectiveOperationException {
        String tagName = (String) tag.getClass().getField("tagName").get(tag);
        String name = "a".equalsIgnoreCase(tagName) ? "href" : "img".equalsIgnoreCase(tagName) ? "src" : null;
        if (name == null)
            return;
        Object list = tag.getClass().getField("attributeList").get(tag);
        for (Object attribute : (Iterable<?>) list.getClass().getField("attributes").get(list)) {
            if (name.equalsIgnoreCase((String) attribute.getClass().getField("name").get(attribute)))
                deQuote((String) attribute.getClass().getField("value").get(attribute));
        }
    }

    private static String deQuote(String value) {
        if (value.startsWith("\"") && value.endsWith("\""))
            return value.substring(1, value.length() - 1);
        return value;
    }

    private String parseHTML(Content content, String text, ContentProcessingOptions options)
            throws ContentProcessorException {

        if (text == null || text.trim().equals(""))
            return text;

        int pos = -1;
        int lines = 0;
        // All this thang should use a StringBuffer, but in Java1.3 the buffer
        // doesn't have "indexOf".... Damn!! ;-)

        int current = 0;

        while (current < text.length()) {
            if ((pos = text.indexOf("<br>", current)) > 0 || (pos = text.indexOf("<br/>", current)) > 0) {
                lines++;
                if (lines > options.getMaxLines()) {
                    text = text.substring(0, pos + 4) + " ...";
                    current = text.length();
                } else {
                    current = pos + 4;
                }

            } else {
                current = text.length();
            }
        }

        // Resolve links to components

        current = 0;
        while (current < text.length()) {

            if ((pos = text.indexOf("\"file://", current)) > 0) {

                StringBuffer buffer = new StringBuffer(text);
                int endQuote = findInBuffer(buffer, pos + 7, '"');
                String name = buffer.substring(pos + 8, endQuote);
                htmlFile(pos, endQuote, buffer, content, name);
                current = pos + 8;
                text = buffer.toString();
            } else if ((pos = text.indexOf("\"cid:", current)) > 0) {

                StringBuffer buffer = new StringBuffer(text);
                int endQuote = findInBuffer(buffer, pos + 4, '"');
                String name = buffer.substring(pos + 5, endQuote);
                htmlFile(pos, endQuote, buffer, content, name);
                current = pos + 8;
                text = buffer.toString();

            } else {

                current = text.length();

            }

        }

        return processCommands(content, options, text);

    }

    private static void htmlFile(int pos, int endQuote, StringBuffer buffer, Content content, String name)
            throws ContentProcessorException {

        // file://fileName --> File included OR Linked, depending on its
        // associated ContentProcessor and options.
        // cid:part1.06070207.03010300@arrakis.es --> File which comes from a
        // multipart/related message.

        Content componentContent = null;
        try {
            if (name.contains("%"))
                name = URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Name '" + name + "' can not be decoded as an UTF-8 string. Used AS-IS", e);
        }
        name = name.replace("&amp;", "&");

        if (log.isDebugEnabled())
            log.debug("An HTML file references a Component named '" + name + "'");
        try {
            componentContent = content.getLinkByName(name);
        } catch (ContentProcessorException e) {
            log.warn("Referenced Component '" + name + "' not found. Using default Unknown content.");
            componentContent = getUnknownContent(name);
        }

        buffer.replace(pos + 1, endQuote, componentContent.getBasePathKey() + componentContent.getAccessPath());
    }

    private static int findInBuffer(StringBuffer buffer, int from, char what) throws ContentProcessorException {

        for (int i = from; i < buffer.length(); i++)
            if (buffer.charAt(i) == what)
                return i;
        throw new ContentProcessorException("Character '" + what + "' expected.");

    }

    private String processCommands(Content container, ContentProcessingOptions options, String body) {

        String tmp = body;

        Perl5Matcher matcher = new Perl5Matcher();
        // matcher.setMultiline(true);

        CommandSubstitution commandSubs = new CommandSubstitution(container, options);
        tmp = Util.substitute(matcher, this.patternCommandTagWithBody, commandSubs, tmp, Util.SUBSTITUTE_ALL);
        tmp = Util.substitute(matcher, this.patternCommandTag, commandSubs, tmp, Util.SUBSTITUTE_ALL);
        tmp = Util.substitute(matcher, this.patternProcessingInstructions, commandSubs, tmp, Util.SUBSTITUTE_ALL);

        HeaderSubstitution headerSubs = new HeaderSubstitution();
        tmp = Util.substitute(matcher, this.patternHeaderExtractor, headerSubs, tmp, Util.SUBSTITUTE_ALL);

        return tmp;

    }

    private String getTocInHTML(String content, int startLevel) {

        String out = "<ul class=\"toc\">";
        int tocNumber = 0;
        Iterator headers = this.getHeaders(content).iterator();
        int currentLevel = 1, headerLevel = 1;
        while (headers.hasNext()) {
            StringPair p = (StringPair) headers.next();
            try {
                headerLevel = Integer.parseInt(p.getFirst());
                for (int i = headerLevel; i < currentLevel; i++)
                    out += "</ul>\n";
                for (int i = currentLevel; i < headerLevel; i++)
                    out += "<ul>\n";
                out += "<li><a class=\"menuitem\" href=\"#" + TOC_ANCHOR_PREFIX + (tocNumber++) + "\">";
                out += p.getSecond() + "</a></li>\n";
                currentLevel = headerLevel;
            } catch (NumberFormatException e) {
                log.warn("Invalid header level '" + p.getFirst() + "' for header '" + p.getSecond()
                        + "'. Ignoring header for TOC ", e);
            }
        }

        for (int i = headerLevel; i >= 1; i--)
            out += "</ul>\n";

        return out + "</ul>\n";
    }

    private Collection getHeaders(String body) {

        ArrayList headers = new ArrayList(0);
        Perl5Matcher matcher = new Perl5Matcher();
        PatternMatcherInput pmi = new PatternMatcherInput(body);
        // matcher.setMultiline(true);
        while (matcher.contains(pmi, this.patternHeaderExtractor)) {
            MatchResult result = matcher.getMatch();
            String level = result.group(1);
            String text = result.group(2);
            // String endLevel = result.group(3);
            headers.add(new StringPair(level, text));
        }
        return headers;

    }

} // HTMLContentProcessor
