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

package ecmwf.ecpds.master.plugin.http.view.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorService;

public class TagUtils {

    private static Log log = LogFactory.getLog(TagUtils.class);
    private static String bundle = Globals.MESSAGES_KEY;
    private static String localeKey = Globals.LOCALE_KEY;

    public static String getResource(PageContext pageContext, String name, String defaultValue) {

        String value = null;
        try {
            value = org.apache.struts.taglib.TagUtils.getInstance().message(pageContext, bundle, localeKey, name, null);
        } catch (JspException e) {
            log.error("JspException", e);
        }
        if (value == null) {
            log.warn("Resource '" + name
                    + "' not found in Struts messages. Check the file relevant for your LOCALE. Taken '" + defaultValue
                    + "' as default value.");
            value = defaultValue;
        }
        return value;

    }

    public static String parseMultiExpressionText(Object context, String expression) {

        String link = expression;
        int current = 0;
        int fieldPos;
        while ((fieldPos = link.indexOf("%{", current)) >= 0) {
            int fieldEnd = link.indexOf("}", fieldPos);
            if (fieldEnd > 0) {
                String fieldName = link.substring(fieldPos + 2, fieldEnd);
                try {
                    String value = resolveQualifiedName(context, fieldName).toString();
                    link = link.substring(0, fieldPos) + value + link.substring(fieldEnd + 1, link.length());
                    current = fieldPos + value.length();
                } catch (ModelException e) {
                    log.warn("The expression '" + expression + "' refers to an invalid field.", e);
                    current = fieldPos + fieldName.length();
                }
            } else {
                log.warn("The expression '" + expression + "' is not parseable. Please review it.");
                return "no close";
            }
        }

        return link;

    }

    public static Object resolveQualifiedName(Object context, String name) throws ModelException {
        return resolveQualifiedName(context, name, false);
    }

    public static Object resolveQualifiedName(Object context, String name, boolean ignoreNull) throws ModelException {

        int pos = name.indexOf('.');
        Object o = (pos < 0) ? lookupBean(context, name) : lookupBean(context, name.substring(0, pos));
        if (pos >= 0 && o != null)
            o = ModelUtils.getFieldValueFromBean(o, name.substring(pos + 1, name.length()));

        if (o == null && !ignoreNull) {
            if (log.isInfoEnabled())
                log.info("The name '" + name + "' refers to an NULL VALUE field.");
            throw new ModelException("The name '" + name + "' refers to an NULL VALUE field.");
        } else {
            return o;
        }

    }

    /**
     * Returns a link from a link definition (the <code>link</code> property). This link definition may have parameters
     * like %{propertyName} that will be substituted for appropriate values in the page context, request or session.
     *
     * @param pageContext
     *            The JSP page context
     * @param unparsedLink
     *            The original link code.
     * @param linkBaseKey
     *            The key to the resource that will be prepended as base path for the link
     * @param commandChar
     *            The character use to denote parameter (ie, the % in %{param})
     *
     * @return The link's final url after all substitutions.
     *
     *         TODO: See how the parameter interpretation thing matches JSTL
     */

    public static String getLink(PageContext pageContext, String unparsedLink, String linkBaseKey) {

        String link = (linkBaseKey != null) ? getResource(pageContext, linkBaseKey, "")
                : getCurrentBasePath(pageContext, unparsedLink);
        if (unparsedLink == null) {
            if (log.isWarnEnabled())
                log.warn("Link is null.");
            return "#";
        } else {
            return parseMultiExpressionText(pageContext, link + unparsedLink);
        }

    }

    public static Object lookupBean(Object context, String name) {

        if (context instanceof PageContext) {

            PageContext pageContext = (PageContext) context;

            Object o = null;
            if ((o = pageContext.getAttribute(name)) == null)
                if ((o = pageContext.getRequest().getAttribute(name)) == null)
                    if ((o = pageContext.getSession().getAttribute(name)) == null)
                        if ((o = pageContext.getServletContext().getAttribute(name)) == null)
                            if (log.isDebugEnabled())
                                log.debug("No object with name '" + name
                                        + "' in any of [page|request|session|context] scopes.");

            return o;

        } else if (context instanceof Map) {
            return ((Map) context).get(name);
        } else {
            log.warn("Received a context of class '" + context.getClass().getName()
                    + "' which is not a PageContext or a Map. Don't know how to process it");
            return null;
        }

    }

    /**
     *
     *
     * @param pageContext
     *            Well, the JSP implicit <code>PageContext</code>
     * @param link
     *            The unparsed link specified in the JSP tags.
     *
     * @return The path to be prepended (or a empty string) to the link once parsed.
     */

    public static String getCurrentBasePath(PageContext pageContext, String link) {

        // log.debug( "Taglibs.getCurrentBasePath.");
        ServletRequest request;
        if (!link.startsWith("/") && !link.startsWith("http://")
                && ((request = pageContext.getRequest()) instanceof HttpServletRequest)) {
            String basePath = ((HttpServletRequest) request).getRequestURI();
            // /log.debug( "Current base path is '" + basePath + "' .");
            int pos = basePath.lastIndexOf("/");
            return (pos > 0) ? basePath.substring(0, pos + 1) : basePath;
        } else
            return "";

    }

    /**
     * Get a formatted/converted value from a bean.
     *
     * This is a very important method regarding performance, since it is going to be executed very often.
     *
     * @param bean
     *            The bean to be shown
     * @param field
     *            The specific field of the bean to be shown. The name format will be "fieldName::outputContentType"
     *
     * @return The field formatted as an adequeate String for the outputContentType.
     *
     * @throws ModelException
     */

    public static String getFieldValueFromBean(PageContext context, ModelBean bean, String field)
            throws ModelException {

        int pos = -1;
        if ((bean instanceof Content) && ((pos = field.indexOf("::")) > 0)) {
            try {

                String theField = field.substring(0, pos);
                String contentType = field.substring(pos + 2, field.length());
                Object o = ModelUtils.getFieldValueFromBean(bean, theField);
                if (o instanceof Date) {
                    if (log.isDebugEnabled())
                        log.debug("Content from field '" + theField + "' is a Date. It will be formatted as '"
                                + contentType + "'.");
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(contentType.trim());
                        return sdf.format((Date) o);
                    } catch (Exception e) {
                        throw new ModelException("Error formatting date.", e);
                    }
                } else if (o instanceof Content) {
                    ContentProcessingOptions options = ContentProcessorService.createProcessingOptions();
                    options.setExternalContext(context);
                    options.setOutputContentType(contentType);
                    return ContentProcessorService.process((Content) bean, theField, options).getStringRepresentation();
                } else {
                    ContentProcessingOptions options = ContentProcessorService.createProcessingOptions();
                    options.setExternalContext(context);
                    options.setOutputContentType(contentType);
                    Content fieldContent = ContentProcessorService.createContent(o.toString(),
                            ((Content) bean).getContentType());
                    return ContentProcessorService.process(fieldContent, options).getStringRepresentation();
                }

            } catch (ContentProcessorException e) {
                log.error("Error processing content from field '" + field + "' of bean '" + bean + ".", e);
                return "!!";
            }
        } else {
            // log.debug( "ModelBeanSearch: Supplied Bean is a '"+bean.getClass().getName()+"'. Not a Content subclass
            // or no '::' in field named '"+field+"' (pos="+pos+").");
            Object o = ModelUtils.getFieldValueFromBean(bean, field);
            if (o != null)
                return o.toString();
            else
                return "null";
        }
    }

    public static String getFieldValueFromBean(PageContext context, ModelBean bean, String field, int maxLength,
            boolean addDots) throws ModelException {

        String value = getFieldValueFromBean(context, bean, field);
        if (maxLength > 0 && value.length() > maxLength)
            return value.substring(0, maxLength) + (addDots ? "..." : "");
        else
            return value;

    }

    public static String getProcessFieldContent(Object field, String property, String altProperty, PageContext context,
            String outputContentType, boolean thumbnail, boolean embedded, SimpleDateFormat dateFormat,
            boolean relativeTime, int maxLength) throws ContentProcessorException {

        String out = "";

        if (field instanceof Content) {

            Content original = (Content) field;
            ContentProcessingOptions options = ContentProcessorService.createProcessingOptions();
            options.setOutputContentType(outputContentType);
            options.setExternalContext(context);

            if (thumbnail)
                options.setSize(ContentProcessingOptions.SIZE_THUMBNAIL);
            else
                options.setSize(ContentProcessingOptions.SIZE_FULL);

            if (embedded)
                options.setDisposition(ContentProcessingOptions.SHOW_EMBEDDED);
            else
                options.setDisposition(ContentProcessingOptions.SHOW_ASLINK);

            Content converted = (property != null) ? ContentProcessorService.process(original, property, options)
                    : ContentProcessorService.process(original, options);

            if (altProperty != null && (converted == null || "".equals(converted.getStringRepresentation()))) {
                // If content is empty and there is an "alternative" property to be searched, go for it.
                if (altProperty.length() > 0)
                    options.setMaxLines(altProperty.length());
                converted = (property != null) ? ContentProcessorService.process(original, altProperty, options)
                        : ContentProcessorService.process(original, options);
                out = converted.getStringRepresentation();
            } else {
                // Else, just dump the content....
                out = trimContent(converted.getStringRepresentation(), maxLength);
            }

        } else if (field instanceof Date && dateFormat != null) {

            if (relativeTime) {
                Collection timeBits = TagUtils.getApproximateFormattedTimeDifference(new Date(), (Date) field);
                if (timeBits.size() > 0)
                    out = timeBits.iterator().next().toString() + " ago";
                else
                    out = "Now";
            } else
                out = dateFormat.format((Date) field);

        } else {
            out = trimContent(field.toString(), maxLength);
        }

        return out;

    }

    private static String trimContent(String input, int maxLength) {
        if (maxLength > 0 && input.length() > maxLength && maxLength > 3)
            return "<span title=\"" + input + "\">" + input.substring(0, maxLength - 3) + "...</span>";
        else
            return input;
    }

    public static Collection getApproximateFormattedTimeDifference(Date after, Date before) {
        long msecs = after.getTime() - before.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(msecs);
        Collection out = new ArrayList();

        int years = cal.get(Calendar.YEAR) - 1970;
        int months = cal.get(Calendar.MONTH) - 1;
        int days = cal.get(Calendar.DAY_OF_MONTH) - 1;
        int hours = cal.get(Calendar.HOUR_OF_DAY) - 1;
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);

        if (years > 0)
            out.add(years + " year" + ((years > 1) ? "s" : ""));
        if (months > 0)
            out.add(months + " month" + ((months > 1) ? "s" : ""));
        if (days > 0)
            out.add(days + " day" + ((days > 1) ? "s" : ""));
        if (hours > 0)
            out.add(hours + " hour" + ((hours > 1) ? "s" : ""));
        if (minutes > 0)
            out.add(minutes + " minute" + ((minutes > 1) ? "s" : ""));
        if (seconds > 0)
            out.add(seconds + " second" + ((seconds > 1) ? "s" : ""));

        return out;

    }

}