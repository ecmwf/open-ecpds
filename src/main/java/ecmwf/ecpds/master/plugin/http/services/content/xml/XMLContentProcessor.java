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

/*
 * Created on Feb 23, 2004
 */
package ecmwf.ecpds.master.plugin.http.services.content.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorBase;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessor;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorService;

/**
 * Content processor for XML documents and the configured XSL transformers.
 */
public class XMLContentProcessor extends ContentProcessorBase implements ContentProcessor {
    private static final Log log = LogFactory.getLog(XMLContentProcessor.class);
    private final HashMap<String, Transformer> transformers = new HashMap<>();

    public XMLContentProcessor() {
        try {
            Properties properties = ConfigService.getPropertySet("xml");
            var keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                int dot = key.indexOf('.');
                String name = key.substring(0, dot);
                String property = key.substring(dot + 1);
                Transformer transformer = transformers.computeIfAbsent(name, ignored -> new Transformer());
                if ("xsl".equals(property))
                    transformer.xsl = properties.getProperty(key);
                else if ("publicId".equals(property))
                    transformer.publicId = properties.getProperty(key);
            }
        } catch (ConfigException e) {
            log.error("Problem loading XML transformers", e);
        }
    }

    @Override
    public Content process(Content original, ContentProcessingOptions options) throws ContentProcessorException {
        try {
            var reader = XMLReaderFactory.createXMLReader();
            var type = new TypeHandler();
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", type);
            reader.setEntityResolver(catalogResolver());
            reader.setContentHandler(type);
            reader.parse(new InputSource(original.getInputStream()));
            for (Transformer transformer : transformers.values()) {
                if (type.publicId.equals(transformer.publicId)) {
                    InputStream input = original.getInputStream();
                    try {
                        return transformer.transform(input, original);
                    } catch (Exception e) {
                        log.warn("Problem converting XML file", e);
                        return ContentProcessorService.getDefaultProcessor(options.getProcessingContext())
                                .process(original, options);
                    }
                }
            }
            return ContentProcessorService.getDefaultProcessor(options.getProcessingContext()).process(original,
                    options);
        } catch (FileNotFoundException e) {
            throw new ContentProcessorException("File not found converting XML file", e);
        } catch (SAXNotSupportedException e) {
            throw new ContentProcessorException("Not possible to get type information", e);
        } catch (SAXException e) {
            throw new ContentProcessorException("SAXException", e);
        } catch (IOException e) {
            throw new ContentProcessorException("IOException", e);
        }
    }

    @Override
    public void validate(Content content, ContentProcessingOptions options) throws ContentProcessorException {
        throw new ContentProcessorException("XMLContentProcessor.validate(content,options) not supported");
    }

    @Override
    public void validate(Content content, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        throw new ContentProcessorException("XMLContentProcessor.validate(content,attribute,options) not supported");
    }

    private static final class TypeHandler extends DefaultHandler2 {
        private String publicId;
        private boolean first = true;

        @Override
        public void startDTD(String name, String publicId, String systemId) {
            this.publicId = publicId;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (first) {
                first = false;
                publicId = uri;
            }
        }
    }

    private static EntityResolver catalogResolver() throws SAXException {
        try {
            return (EntityResolver) Class.forName("org.apache.xml.resolver.tools.CatalogResolver").getConstructor()
                    .newInstance();
        } catch (ClassNotFoundException e) {
            return jdkCatalogResolver();
        } catch (ReflectiveOperationException e) {
            throw new SAXException("Cannot create XML catalog resolver", e);
        }
    }

    private static EntityResolver jdkCatalogResolver() throws SAXException {
        try {
            Properties properties = new Properties();
            URL resource = XMLContentProcessor.class.getResource("/CatalogManager.properties");
            if (resource != null) {
                try (InputStream input = resource.openStream()) {
                    properties.load(input);
                }
            }
            String files = System.getProperty("xml.catalog.files");
            boolean fromProperties = files == null && properties.getProperty("catalogs") != null;
            if (fromProperties)
                files = properties.getProperty("catalogs");
            if (files == null)
                files = "./xcatalog";
            var catalogs = new ArrayList<URI>();
            for (String file : files.split(";")) {
                if (file.isEmpty())
                    continue;
                URI uri;
                if (file.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*"))
                    uri = new URL(file).toURI();
                else if (fromProperties
                        && !"true".equalsIgnoreCase(properties.getProperty("relative-catalogs", "true")))
                    uri = new URL(resource, file).toURI();
                else
                    uri = new File(file).toURI();
                if (!"file".equalsIgnoreCase(uri.getScheme()) || new File(uri).isFile())
                    catalogs.add(uri);
            }
            if (catalogs.isEmpty())
                return (publicId, systemId) -> null;
            String prefer = System.getProperty("xml.catalog.prefer", properties.getProperty("prefer", "public"));
            CatalogFeatures features = CatalogFeatures.builder()
                    .with(CatalogFeatures.Feature.PREFER, "system".equalsIgnoreCase(prefer) ? "system" : "public")
                    .with(CatalogFeatures.Feature.RESOLVE, "continue").build();
            return CatalogManager.catalogResolver(features, catalogs.toArray(new URI[0]));
        } catch (Exception e) {
            throw new SAXException("Cannot configure XML catalog resolver", e);
        }
    }

    private static final class Transformer {
        private String publicId;
        private String xsl;
        private final Semaphore permits = new Semaphore(10);
        private final ArrayBlockingQueue<Templates> templates = new ArrayBlockingQueue<>(10);

        private Content transform(InputStream input, Content original) throws Exception {
            permits.acquire();
            Templates compiled = null;
            try {
                compiled = templates.poll();
                TransformerFactory factory = TransformerFactory.newInstance();
                if (!factory.getFeature(SAXSource.FEATURE) || !factory.getFeature(SAXResult.FEATURE))
                    throw new IllegalStateException("TransformerFactory has insufficient SAX capabilities");
                if (compiled == null) {
                    String stylesheet = ConfigService.getBasePath() + "/WEB-INF/config/xsl/" + xsl;
                    compiled = factory.newTemplates(new StreamSource(stylesheet));
                }
                var handler = ((SAXTransformerFactory) factory).newTransformerHandler(compiled);
                var reader = XMLReaderFactory.createXMLReader();
                reader.setContentHandler(handler);
                reader.setEntityResolver(catalogResolver());
                var out = new ByteArrayOutputStream();
                handler.setResult(new StreamResult(out));
                reader.parse(new InputSource(input));
                return ContentProcessorService.createContent(out.toString(), original.getContentType());
            } finally {
                if (compiled != null)
                    templates.offer(compiled);
                permits.release();
            }
        }
    }
}
