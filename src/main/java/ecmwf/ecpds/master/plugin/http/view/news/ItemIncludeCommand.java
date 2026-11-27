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
 * Created on 17-Dec-2004
 */
package ecmwf.ecpds.master.plugin.http.view.news;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;

import ecmwf.ecpds.master.plugin.http.services.content.CommandBase;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;
import ecmwf.ecpds.master.plugin.http.home.news.NewsItemHome;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.services.content.Command;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

/**
 * Includes a news item using its Monitor URL.
 */
public class ItemIncludeCommand extends CommandBase implements Command {
    private static final Log log = LogFactory.getLog(ItemIncludeCommand.class);

    @Override
    public String getOutput(Content container, Map parameters, String body, ContentProcessingOptions options)
            throws ContentProcessorException {
        try {
            Object name = parameters.get("name");
            if (name == null)
                return "[" + getName() + ": Needs a 'name' parameter]";
            String itemName = name.toString();
            NewsItem item;
            NewsChannel channel;
            if (itemName.indexOf(NewsChannel.CHANNEL_QUALIFIER) > 0) {
                item = NewsItemHome.findByQualifiedName(itemName);
                channel = item.getChannel();
            } else if (container instanceof NewsItem) {
                item = (NewsItem) container;
                channel = item.getChannel();
            } else if (container instanceof NewsChannel) {
                channel = (NewsChannel) container;
                item = NewsItemHome.findByNameAndChannelId(itemName, channel.getId());
            } else {
                return "[" + getName()
                        + ": Needs either a qualifed 'name' or to be called from a NewsItem or NewsChannel]";
            }

            String host = "localhost";
            int port = 80;
            String basepath = "/publications/cms/";
            if (options.getExternalContext() instanceof PageContext context) {
                host = context.getRequest().getServerName();
                port = context.getRequest().getServerPort();
                basepath = TagUtils.getResource(context, "news.basepath", "");
            } else {
                log.warn("Taken default values for host, port and basepath, since no PageContext was available");
            }
            String url = "http://" + host + ":" + port + basepath + "/get/" + channel.getName() + "/" + item.getId();
            validateUrl(url);
            var redirect = new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
                        throws ProtocolException {
                    return response.getCode() != 308 && super.isRedirected(request, response, context);
                }
            };
            try (var client = HttpClients.custom().setRedirectStrategy(redirect).build()) {
                int path = url.indexOf('/', "http://".length());
                int fragment = url.indexOf('#', path);
                var request = new HttpGet(url.substring(0, path) + "/");
                request.setPath(url.substring(path, fragment < 0 ? url.length() : fragment));
                return client.execute(request, response -> {
                    if (response.getCode() != 200)
                        return "[" + getName() + ": " + response.getCode() + " " + response.getReasonPhrase();
                    var entity = response.getEntity();
                    if (entity == null)
                        return null;
                    byte[] bytes = EntityUtils.toByteArray(entity);
                    Charset charset = StandardCharsets.ISO_8859_1;
                    var header = response.getFirstHeader("Content-Type");
                    if (header != null) {
                        try {
                            var type = org.apache.hc.core5.http.ContentType.parse(header.getValue());
                            if (type.getCharset() != null)
                                charset = type.getCharset();
                        } catch (java.nio.charset.UnsupportedCharsetException e) {
                            charset = Charset.defaultCharset();
                        } catch (IllegalArgumentException e) {
                            // HttpClient 3 defaults to ISO-8859-1 for an absent charset.
                        }
                    }
                    return new String(bytes, charset);
                });
            }
        } catch (Exception e) {
            throw new ContentProcessorException("Problem executing commmand", e);
        }
    }

    private static void validateUrl(String url) {
        int path = url.indexOf('/', "http://".length());
        if (path < 0)
            return;
        boolean query = false;
        for (int i = path; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '#')
                return;
            if (c == '?') {
                query = true;
                continue;
            }
            if (c == '%' && !query) {
                if (i + 2 >= url.length())
                    throw new IllegalArgumentException("Invalid uri '" + url + "': Invalid URL encoding: ");
                for (int j = 1; j <= 2; j++) {
                    char digit = url.charAt(i + j);
                    if (digit > 127)
                        throw invalidUrl(url, query);
                    if (Character.digit(digit, 16) < 0)
                        throw new IllegalArgumentException("Invalid uri '" + url
                                + "': Invalid URL encoding: not a valid digit (radix 16): " + (int) digit);
                }
                i += 2;
            } else if (c > 127 || c <= 32 || c == '[' || c == ']' || c == '|' || c == '\\' || c == '{' || c == '}'
                    || c == '^' || c == '`' || c == '"') {
                throw invalidUrl(url, query);
            }
        }
    }

    private static IllegalArgumentException invalidUrl(String url, boolean query) {
        return new IllegalArgumentException(
                "Invalid uri '" + url + "': " + (query ? "Invalid query" : "escaped absolute path not valid"));
    }
}
