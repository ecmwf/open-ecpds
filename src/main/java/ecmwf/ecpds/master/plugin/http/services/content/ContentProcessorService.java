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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.ECMWFException;
import ecmwf.ecpds.master.plugin.http.model.users.Resource;
import ecmwf.ecpds.master.plugin.http.services.Util;
import ecmwf.ecpds.master.plugin.http.services.cache.CacheException;
import ecmwf.ecpds.master.plugin.http.services.cache.CacheService;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;

/**
 * <p>
 * The service which centralizes all the jobs regarding content transformation. It allows to get a field of a bean and
 * to convert it into another content type, applying some options. Also allows to check that a field with a given
 * content-type really has content valid for that content type.
 * </p>
 * <p>
 * 2004-03-04: ContentProcessors are now organized in a two level hierachy, areas and content types
 * </p>
 *
 * <p>
 * Created: Wed Apr 24 11:40:31 2002
 * </p>
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla </a>
 *
 * @version 2.0
 */

public class ContentProcessorService {

    private static Log log = LogFactory.getLog(ContentProcessorService.class);
    private static ContentProcessorService instance = new ContentProcessorService();

    private static final String DEFAULT_PROCESSOR = "default";
    private static final String CONTENT = Content.class.getName();

    private HashMap processors;

    /**
     * Creates a new <code>ContentProcessorService</code> instance. This method is invocated only once from the class
     * loading code. It then checks for the Properties called "content" in the ConfigService, which should have read it
     * from the "content.properties" file. This file should have the following format:
     *
     */

    public ContentProcessorService() {

        log.info("Initializing ContentProcessorService.");
        this.initializeContentProcessingContexts();

    }

    private void initializeContentProcessingContexts() {
        this.processors = new HashMap();
    }

    /*
     * private void initializeContentProcessingContexts() {
     *
     * try {
     *
     * this.contexts = new HashMap(); Properties props = ConfigService.getPropertySet("content");
     *
     * // First round for contexts. Iterator c = ConfigService.getCollectionParameter("content", "contexts").iterator();
     * while (c.hasNext()) { String context = c.next().toString(); HashMap processors = new HashMap();
     * contexts.put(context, processors);
     *
     * log.info("Loading processor context '" + context + "'.");
     *
     * // Handlers in the context. for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
     *
     * String propertyName = (String) e.nextElement(); String propertyValue = (String) props.get(propertyName); try { if
     * (propertyName.startsWith(context + ".") && propertyName.endsWith(".processor")) { String contentType =
     * propertyName.substring(context.length() + 1, propertyName.length() - 10); processors.put(contentType,
     * this.loadHandler(contentType, propertyValue, ContentProcessor.class)); } } catch (ContentProcessorException ex) {
     * log.warn("Processor not loaded DEFAULT will be used"); }
     *
     * }
     *
     * }
     *
     * } catch (ConfigException e) { log.error("Unable to configure ContentProcessorService: ", e); } }
     *
     */

    /**
     * Finds and returns the appropiate <code>ContentProcessor</code> for a given contentType.
     *
     * @param contentType
     *            The content type
     *
     * @return The <code>ContentProcessor</code>
     *
     * @throws ContentProcessorException
     *             If no processor if found for this content type.
     */
    /*
     * public static ContentProcessor getContentProcessor(String context, String contentType) throws
     * ContentProcessorException {
     *
     * HashMap processors = (HashMap) findInstance().contexts.get(context); if (processors == null) throw new
     * ContentProcessorException("Can't find a ContentProcessor CONTEXT '" + context +
     * "'. Please check file 'content.properties'."); else { ContentProcessor processor = (ContentProcessor)
     * processors.get(contentType); if (processor == null) throw new
     * ContentProcessorException("Can't find a ContentProcessor for type '" + contentType + "' in context '" + context +
     * "'. Please check file 'content.properties'."); else return processor; }
     *
     * }
     */
    public synchronized static ContentProcessor getContentProcessor(String context, String contentType)
            throws ContentProcessorException {

        String key = context + "." + contentType;
        Map processors = findInstance().processors;
        try {
            ContentProcessor processor = (ContentProcessor) processors.get(key);
            if (processor == null) {
                processor = (ContentProcessor) Util.loadAndConfigureObject(ConfigService.getPropertySet("content"),
                        key);
                synchronized (processors) {
                    processors.put(key, processor);
                }
            }
            return processor;
        } catch (ECMWFException e) {
            throw new ContentProcessorException(
                    "Can't find a ContentProcessor for type '" + key + "'. Please check file 'content.properties'.", e);
        }

    }

    /**
     * Create a <code>Content</code> object from a String. Interesting to comply with interfaces that expect a Content
     * object as result of a conversion.
     *
     * @param content
     *            The string content.
     *
     * @return The Content object
     */

    public static Content createContent(String content) {
        return new DefaultContent(content);
    }

    public static Content createContent(String content, String contentType) {
        return new DefaultContent(content, contentType);
    }

    public static Content createFileContent(File file, Resource resource, String contentType) {
        return new FileContent(file, resource, contentType);
    }

    /**
     * Create a <code>ContentProcessingOptions</code> object. Default (final) objects can be directly got from the
     * DEFAULT and DEFAULT_EMBEDDED static members of ContentProcessingOptions.
     *
     * @return The ContentProcessingOptions object
     */

    public static ContentProcessingOptions createProcessingOptions() {
        return new ContentProcessingOptions();
    }

    public static Content process(Content content, ContentProcessingOptions options) throws ContentProcessorException {

        try {
            Content c = null;
            String key = null;
            if (options.isCacheable()) {
                key = content.toString() + "=>" + options.getKey();
                c = (Content) CacheService.get(CONTENT, key);
            }
            if (c != null)
                return c;
            else {
                c = findInstance().getProcessorOrDefault(options.getProcessingContext(), content.getContentType())
                        .process(content, options);
                if (options.isCacheable())
                    CacheService.set(CONTENT, key, c);
            }
            return c;

        } catch (CacheException e) {
            throw new ContentProcessorException("ContentProcessor: Cache problem", e);
        } catch (ClassCastException e) {
            throw new ContentProcessorException("ContentProcessor: Class cast problem (Content expected)", e);
        }

    }

    public static Content process(Content content, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {

        try {
            Content c = null;
            String key = null;
            if (options.isCacheable()) {
                key = attribute + "@" + content.toString() + "=>" + options.getKey();
                c = (Content) CacheService.get(CONTENT, key);
            }
            if (c != null)
                return c;
            else {
                c = findInstance().getProcessorOrDefault(options.getProcessingContext(), content.getContentType())
                        .process(content, attribute, options);
                if (options.isCacheable())
                    CacheService.set(CONTENT, key, c);
            }
            return c;

        } catch (CacheException e) {
            throw new ContentProcessorException("Cache problem", e);
        } catch (ClassCastException e) {
            throw new ContentProcessorException("Class cast problem (Content expected)", e);
        }

    }

    public static Content getStructure(Content content, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        return getStructure(content, attribute, 1, options);
    }

    public static Content getStructure(Content content, String attribute, int startLevel,
            ContentProcessingOptions options) throws ContentProcessorException {

        try {
            Content c = null;
            String key = null;
            if (options.isCacheable()) {
                key = attribute + "@" + content.toString() + "@>" + options.getKey();
                c = (Content) CacheService.get(CONTENT, key);
            }
            if (c != null)
                return c;
            else {
                c = findInstance().getProcessorOrDefault(options.getProcessingContext(), content.getContentType())
                        .getStructure(content, attribute, startLevel, options);
                if (options.isCacheable())
                    CacheService.set(CONTENT, key, c);
            }
            return c;

        } catch (CacheException e) {
            throw new ContentProcessorException("Cache problem", e);
        } catch (ClassCastException e) {
            throw new ContentProcessorException("Class cast problem (Content expected)", e);
        }

    }

    public static void validate(Content content, ContentProcessingOptions options) throws ContentProcessorException {

        if (log.isDebugEnabled())
            log.debug("Validating content '" + content + "'  '" + content.getContentType() + "'.");
        findInstance().getProcessorOrDefault(options.getProcessingContext(), content.getContentType()).validate(content,
                options);

    }

    public static void validate(Content content, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {

        if (log.isDebugEnabled())
            log.debug("Validating content '" + content + "' attribute '" + attribute + "' with type '"
                    + content.getContentType() + "'.");
        findInstance().getProcessorOrDefault(options.getProcessingContext(), content.getContentType()).validate(content,
                attribute, options);

    }

    /**
     * This is a slightly related feature, which was stuck here due to lack of a better place. It will return the path
     * to the correct icon for a given URL, depending on its extension. TODO: This should be moved to the RESOURCES
     * file..
     *
     * @param url
     *            The URL
     *
     * @return The path of the corresponding icon, or the path of a default one, if none matches.
     */

    public static String getIconFromExtension(String url) {

        // if (log.isDebugEnabled())
        // log.debug("Getting an icon for URL '" + url + "'");

        int point;
        if (url != null && ((point = url.lastIndexOf(".")) > 0)) {
            String extension = url.substring(point, url.length());
            try {
                // if (log.isDebugEnabled())
                // log.debug("Getting an icon for and URL with extension '" + extension + "'");
                return ConfigService.getParameter("uploads", "icons" + extension);
            } catch (ConfigException e) {
                log.debug("No icon for extension '" + extension + "'", e);
            }
        }

        try {
            return ConfigService.getParameter("uploads", "icons.default");
        } catch (ConfigException e) {
            log.error("Impossible to get default icon: ", e);
            return "/assets/images/shim.gif";
        }

    }

    // ****************************************************************************************************************
    // Private
    // ************************************************************************

    private void setProcessorProperty(String context, String processorName, String name, String value)
            throws ContentProcessorException {

        ContentProcessor processor = getContentProcessor(context, processorName);
        processor.setProperty(name, value);

    }

    private Object loadHandler(String contentType, String className, Class expectedType)
            throws ContentProcessorException {

        log.info("Loading class '" + className + "' with key '" + contentType + "'");
        try {

            Object o = Class.forName(className).newInstance();
            if (expectedType.isAssignableFrom(o.getClass()))
                return o;
            else
                throw new ContentProcessorException("Loaded a class of type '" + o.getClass().getName()
                        + "' while expected '" + expectedType.getName() + "'");

        } catch (ClassNotFoundException ex) {
            String message = "Class not found while loading Processor '" + contentType + "'";
            log.error(message, ex);
            throw new ContentProcessorException(message, ex);

        } catch (InstantiationException ex) {
            String message = "Instantiation exception while loading Processor '" + contentType + "'";
            log.error(message, ex);
            throw new ContentProcessorException(message, ex);

        } catch (IllegalAccessException ex) {
            String message = "Illegal access exception while loading Processor '" + contentType + "'";
            log.error(message, ex);
            throw new ContentProcessorException(message, ex);
        }

    }

    private static ContentProcessor getProcessorOrDefault(String context, String contentType)
            throws ContentProcessorException {

        ContentProcessor processor = null;
        try {
            processor = getContentProcessor(context, contentType);
        } catch (ContentProcessorException e) {
            log.debug("Reverting to '" + DEFAULT_PROCESSOR + "' processor for content type '" + contentType + "'.", e);
            return getDefaultProcessor(context);
        }
        return processor;

    }

    public static ContentProcessor getDefaultProcessor(String context) throws ContentProcessorException {
        return getContentProcessor(context, DEFAULT_PROCESSOR);
    }

    private static ContentProcessorService findInstance() {
        // return
        // (ContentProcessorService)DiscoverSingleton.find(ContentProcessorService.class,ContentProcessorService.class.getName());
        return instance;
    }

} // ContentProcessorService
