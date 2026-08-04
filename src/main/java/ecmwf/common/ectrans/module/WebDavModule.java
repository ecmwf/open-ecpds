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

package ecmwf.common.ectrans.module;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SOCKET_STATISTICS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SO_MAX_PACING_RATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_CONGESTION_CONTROL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_KEEP_ALIVE_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_LINGER_ENABLE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_LINGER_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_MAX_SEGMENT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_NO_DELAY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_QUICK_ACK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_TIME_STAMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_USER_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_TCP_WINDOW_CLAMP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_CONNECT_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_LOCK_OWNER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_LOCK_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_MKDIRS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_PASSWORD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_PATH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_PROXY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_SCHEME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_SOCKET_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_SSL_VALIDATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_SUPPORTED_PROTOCOLS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_USERNAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_USE_LOCK;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.ClientSocketFactory;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.rmi.SSLClientSocketFactory;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;

/**
 * WebDAV transfer module.
 */
public final class WebDavModule extends TransferModule {
    /** The logger. */
    private static final Logger _log = LogManager.getLogger(WebDavModule.class);

    /** The propfind for size. */
    private static final String PROPFIND_SIZE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<D:propfind xmlns:D=\"DAV:\"><D:prop><D:getcontentlength/><D:resourcetype/>" + "</D:prop></D:propfind>";

    /** The propfind for list. */
    private static final String PROPFIND_LIST = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<D:propfind xmlns:D=\"DAV:\"><D:prop><D:href/><D:displayname/><D:resourcetype/>"
            + "</D:prop></D:propfind>";

    private CloseableHttpClient httpClient;
    private ClientSocketFactory socketFactory = null;
    private SSLClientSocketFactory sslSocketFactory = null;
    private String host;
    private String scheme;
    private int port;
    private String basePath;
    private boolean useLock;
    private boolean mkdirs;
    private long lockTimeout;
    private String lockOwner;
    private volatile boolean closed;

    @Override
    public void updateSocketStatistics() throws IOException {
        if (socketFactory != null) {
            socketFactory.updateStatistics();
        }
        if (sslSocketFactory != null) {
            sslSocketFactory.updateStatistics();
        }
    }

    @Override
    public void connect(final String location, final ECtransSetup setup) throws IOException {
        host = normalizeHost(location);
        scheme = setup.getString(HOST_WEBDAV_SCHEME);
        port = getPort(setup);
        basePath = normalizePath(setup.getString(HOST_WEBDAV_PATH));
        useLock = setup.getBoolean(HOST_WEBDAV_USE_LOCK);
        mkdirs = setup.getBoolean(HOST_WEBDAV_MKDIRS);
        lockTimeout = setup.getLong(HOST_WEBDAV_LOCK_TIMEOUT);
        lockOwner = setup.getString(HOST_WEBDAV_LOCK_OWNER);
        final var username = setup.getString(HOST_WEBDAV_USERNAME);
        final var password = setup.getString(HOST_WEBDAV_PASSWORD);
        final ClientSocketStatistics statistics;
        if (setup.getBoolean(HOST_ECTRANS_SOCKET_STATISTICS) && getAttribute("connectOptions") != null) {
            _log.debug("Activating Socket Statistics");
            statistics = new ClientSocketStatistics();
            setAttribute(statistics);
        } else {
            statistics = null;
        }
        final var socketConfig = new SocketConfig(statistics, "WebDavModule", getDebug());
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_NO_DELAY, socketConfig::setTcpNoDelay);
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE, socketConfig::setKeepAlive);
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_TIME_STAMP, socketConfig::setTCPTimeStamp);
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_QUICK_ACK, socketConfig::setTCPQuickAck);
        setup.setStringIfPresent(HOST_ECTRANS_TCP_CONGESTION_CONTROL, socketConfig::setTCPCongestion);
        setup.setByteSizeIfPresent(HOST_ECTRANS_SO_MAX_PACING_RATE, socketConfig::setSOMaxPacingRate);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_MAX_SEGMENT, socketConfig::setTCPMaxSegment);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_WINDOW_CLAMP, socketConfig::setTCPWindowClamp);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_TIME, socketConfig::setTCPKeepAliveTime);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL, socketConfig::setTCPKeepAliveInterval);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES, socketConfig::setTCPKeepAliveProbes);
        setup.setIntegerIfPresent(HOST_ECTRANS_TCP_USER_TIMEOUT, socketConfig::setTCPUserTimeout);
        setup.setBooleanIfPresent(HOST_ECTRANS_TCP_LINGER_ENABLE, enable -> setup
                .setIntegerIfPresent(HOST_ECTRANS_TCP_LINGER_TIME, time -> socketConfig.setTCPLinger(enable, time)));
        socketFactory = new ClientSocketFactory(socketConfig);
        try {
            sslSocketFactory = socketConfig.getSSLSocketFactory("TLS", setup.getBoolean(HOST_WEBDAV_SSL_VALIDATION));
        } catch (final Exception e) {
            throw new IOException("Unable to initialize WebDAV TLS support", e);
        }
        final var cm = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", new ConfigConnectionSocketFactory(socketFactory))
                .register("https",
                        new SSLConnectionSocketFactory(sslSocketFactory,
                                setup.getStringList(HOST_WEBDAV_SUPPORTED_PROTOCOLS).toArray(new String[0]), null,
                                setup.getBoolean(HOST_WEBDAV_SSL_VALIDATION) ? null : NoopHostnameVerifier.INSTANCE))
                .build());
        final var requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(setup.getDuration(HOST_WEBDAV_CONNECT_TIMEOUT).toMillis()))
                .setResponseTimeout(Timeout.ofMilliseconds(setup.getDuration(HOST_WEBDAV_SOCKET_TIMEOUT).toMillis()))
                .build();
        final var builder = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig);
        if (isNotEmpty(username)) {
            final var creds = new BasicCredentialsProvider();
            creds.setCredentials(new AuthScope(null, -1),
                    new UsernamePasswordCredentials(username, password.toCharArray()));
            builder.setDefaultCredentialsProvider(creds);
            builder.setDefaultHeaders(List.of(new org.apache.hc.core5.http.message.BasicHeader(
                    HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder()
                            .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)))));
        }
        final var proxy = setup.getString(HOST_WEBDAV_PROXY);
        if (isNotEmpty(proxy)) {
            final var index = proxy.lastIndexOf(':');
            if (index > 0) {
                builder.setProxy(
                        new HttpHost("http", proxy.substring(0, index), Integer.parseInt(proxy.substring(index + 1))));
            }
        }
        httpClient = builder.build();
        setAttribute("remote.hostName", host);
    }

    @Override
    public int getPort(final ECtransSetup setup) {
        return setup.getInteger(HOST_WEBDAV_PORT);
    }

    @Override
    public void del(final String name) throws IOException {
        executeAndConsume(new HttpDelete(buildUri(name)), HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        final var request = new HttpGet(buildUri(name));
        if (posn > 0) {
            request.setHeader(HttpHeaders.RANGE, "bytes=" + posn + "-");
        }
        final var response = execute(request, posn > 0 ? new int[] { HttpStatus.SC_OK, HttpStatus.SC_PARTIAL_CONTENT }
                : new int[] { HttpStatus.SC_OK });
        final var entity = response.getEntity();
        if (entity == null) {
            closeQuietly(response);
            throw new IOException("Empty response body for " + name);
        }
        final var in = entity.getContent();
        return new FilterInputStream(in) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    closeQuietly(response);
                }
            }
        };
    }

    @Override
    public boolean put(final InputStream in, final String name, final long posn, final long size) throws IOException {
        if (posn > 0) {
            throw new IOException("Append PUT not supported by the WebDAV module");
        }
        final var lockToken = useLock ? lock(name) : null;
        try {
            tryPut(name, in, size);
            setAttribute("remote.fileName", name);
            return true;
        } finally {
            if (lockToken != null) {
                unlock(name, lockToken);
            }
        }
    }

    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        if (posn > 0) {
            throw new IOException("Append PUT not supported by the WebDAV module");
        }
        final var output = new PipedOutputStream();
        final var input = new PipedInputStream(output, StreamPlugThread.DEFAULT_BUFF_SIZE);
        final var error = new AtomicReference<IOException>();
        final var done = new CountDownLatch(1);
        new ConfigurableRunnable() {
            @Override
            public void configurableRun() {
                try {
                    put(input, name, 0, size);
                } catch (final IOException e) {
                    error.set(e);
                } finally {
                    StreamPlugThread.closeQuietly(input);
                    done.countDown();
                }
            }
        }.execute();
        return new FilterOutputStream(output) {
            private boolean closedOnce;

            @Override
            public void close() throws IOException {
                if (closedOnce) {
                    return;
                }
                closedOnce = true;
                IOException failure = null;
                try {
                    super.close();
                } catch (final IOException e) {
                    failure = e;
                }
                try {
                    done.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (failure == null) {
                        failure = new IOException("Interrupted while waiting for WebDAV PUT", e);
                    }
                }
                if (failure == null) {
                    failure = error.get();
                }
                if (failure != null) {
                    throw failure;
                }
            }
        };
    }

    @Override
    public long size(final String name) throws IOException {
        final var request = new HttpPropfind(buildUri(name));
        request.setHeader("Depth", "0");
        request.setEntity(new StringEntity(PROPFIND_SIZE, ContentType.APPLICATION_XML));
        try (var response = execute(request, HttpStatus.SC_MULTI_STATUS)) {
            final var document = parseXml(EntityUtils.toString(response.getEntity()));
            final var lengths = document.getElementsByTagNameNS("DAV:", "getcontentlength");
            if (lengths.getLength() == 0) {
                throw new IOException("Missing DAV:getcontentlength for " + name);
            }
            return Long.parseLong(lengths.item(0).getTextContent().trim());
        } catch (final ParseException e) {
            throw new IOException("Unable to parse PROPFIND response", e);
        }
    }

    @Override
    public void mkdir(final String dir) throws IOException {
        executeAndConsume(new HttpMkcol(buildUri(dir)), HttpStatus.SC_CREATED, HttpStatus.SC_METHOD_NOT_ALLOWED,
                HttpStatus.SC_CONFLICT);
    }

    @Override
    public void rmdir(final String dir) throws IOException {
        del(dir);
    }

    @Override
    public void move(final String source, final String target) throws IOException {
        final var request = new HttpMove(buildUri(source));
        request.setHeader("Destination", buildUri(target).toString());
        request.setHeader("Overwrite", "T");
        executeAndConsume(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public void list(final String directory, final String pattern, final OutputStream out) throws IOException {
        for (final String line : listAsStringArray(directory, pattern)) {
            out.write(line.concat("\n").getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        final var request = new HttpPropfind(buildUri(directory));
        request.setHeader("Depth", "1");
        request.setEntity(new StringEntity(PROPFIND_LIST, ContentType.APPLICATION_XML));
        final var matcher = isNotEmpty(pattern) ? Pattern.compile(pattern) : null;
        try (var response = execute(request, HttpStatus.SC_MULTI_STATUS)) {
            final var document = parseXml(EntityUtils.toString(response.getEntity()));
            final var responses = document.getElementsByTagNameNS("DAV:", "response");
            final Set<String> items = new LinkedHashSet<>();
            final var requested = normalizePath(directory);
            for (int i = 0; i < responses.getLength(); i++) {
                final var responseElement = (Element) responses.item(i);
                final var href = getFirstChildText(responseElement, "href");
                if (href == null) {
                    continue;
                }
                final var resourcePath = normalizePath(extractPath(href));
                if (resourcePath.equals(requested)) {
                    continue;
                }
                final var name = decodeLastSegment(resourcePath);
                if (matcher == null || matcher.matcher(name).matches()) {
                    items.add(name);
                }
            }
            return items.toArray(new String[0]);
        } catch (final ParseException e) {
            throw new IOException("Unable to parse PROPFIND response", e);
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (httpClient != null) {
            httpClient.close();
            httpClient = null;
        }
    }

    private void tryPut(final String name, final InputStream input, final long size) throws IOException {
        IOException failure = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            final var request = new HttpPut(buildUri(name));
            request.setEntity(
                    new InputStreamEntity(input, size >= 0 ? size : -1, ContentType.APPLICATION_OCTET_STREAM));
            try (var _ = execute(request, HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT)) {
                return;
            } catch (final IOException e) {
                failure = e;
                if (attempt == 0 && mkdirs && e.getMessage() != null && e.getMessage().contains(" 409 ")) {
                    createParents(name);
                    continue;
                }
                throw e;
            }
        }
        throw failure != null ? failure : new IOException("PUT failed for " + name);
    }

    private String lock(final String name) throws IOException {
        final var request = new HttpLock(buildUri(name));
        request.setHeader("Timeout", lockTimeout <= 0 ? "Infinite" : "Second-" + lockTimeout);
        request.setHeader("Depth", "0");
        request.setEntity(new StringEntity(buildLockInfoXml(), ContentType.APPLICATION_XML));
        try (var response = execute(request, HttpStatus.SC_OK, HttpStatus.SC_CREATED)) {
            final var entity = response.getEntity();
            final var xml = entity != null ? EntityUtils.toString(entity) : "";
            final var token = parseLockToken(xml);
            if (token == null) {
                throw new IOException("Missing lock token for " + name);
            }
            return token;
        } catch (final ParseException e) {
            throw new IOException("Unable to parse LOCK response", e);
        }
    }

    private void unlock(final String name, final String token) throws IOException {
        final var request = new HttpUnlock(buildUri(name));
        final var coded = token.startsWith("<") ? token : "<" + token + ">";
        request.setHeader("Lock-Token", coded);
        request.setHeader("If", "(" + coded + ")");
        executeAndConsume(request, HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    }

    private void createParents(final String name) throws IOException {
        final var path = normalizePath(name);
        final var lastSlash = path.lastIndexOf('/');
        if (lastSlash <= 0) {
            return;
        }
        var current = "";
        for (final String segment : path.substring(1, lastSlash).split("/")) {
            if (segment.isEmpty()) {
                continue;
            }
            current = current + "/" + segment;
            final var request = new HttpMkcol(buildUri(current));
            executeAndConsume(request, HttpStatus.SC_CREATED, HttpStatus.SC_METHOD_NOT_ALLOWED, HttpStatus.SC_OK);
        }
    }

    private org.apache.hc.core5.http.ClassicHttpResponse execute(final HttpUriRequestBase request,
            final int... expected) throws IOException {
        ensureOpen();
        final var response = httpClient.executeOpen(null, request, null);
        final var code = response.getCode();
        for (final int status : expected) {
            if (status == code) {
                return response;
            }
        }
        try {
            final var body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";
            throw new IOException(request.getMethod() + " " + request.getRequestUri() + " failed with status " + code
                    + (isNotEmpty(body) ? ": " + body : ""));
        } catch (final ParseException e) {
            throw new IOException("Failed to parse HTTP response", e);
        } finally {
            closeQuietly(response);
        }
    }

    private void executeAndConsume(final HttpUriRequestBase request, final int... expected) throws IOException {
        final var response = execute(request, expected);
        try {
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            closeQuietly(response);
        }
    }

    private void ensureOpen() throws IOException {
        if (closed || httpClient == null) {
            throw new IOException("Module closed");
        }
    }

    private URI buildUri(final String name) throws IOException {
        try {
            return new URI(scheme, null, host, port, joinPaths(basePath, name), null, null);
        } catch (final URISyntaxException e) {
            throw new IOException("Invalid WebDAV URI for " + name, e);
        }
    }

    private static String normalizeHost(final String location) {
        var host = location == null ? "" : location.trim();
        final var slash = host.indexOf('/');
        if (slash >= 0) {
            host = host.substring(0, slash);
        }
        return host;
    }

    private static String normalizePath(final String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        var normalized = path.trim().replace('\\', '/');
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String joinPaths(final String first, final String second) {
        final var left = normalizePath(first);
        final var right = normalizePath(second);
        if ("/".equals(left)) {
            return right;
        }
        if ("/".equals(right)) {
            return left;
        }
        return normalizePath(left + "/" + right.substring(1));
    }

    private static String extractPath(final String href) {
        try {
            final var uri = URI.create(href);
            return uri.getPath() != null ? uri.getPath() : href;
        } catch (final IllegalArgumentException _) {
            return href;
        }
    }

    private static String decodeLastSegment(final String path) {
        final var normalized = normalizePath(path);
        final var slash = normalized.lastIndexOf('/');
        final var segment = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return java.net.URLDecoder.decode(segment, StandardCharsets.UTF_8);
    }

    private String buildLockInfoXml() throws IOException {
        try {
            final var document = newDocument();
            final var lockInfo = document.createElementNS("DAV:", "D:lockinfo");
            document.appendChild(lockInfo);
            final var lockScope = document.createElementNS("DAV:", "D:lockscope");
            lockScope.appendChild(document.createElementNS("DAV:", "D:exclusive"));
            lockInfo.appendChild(lockScope);
            final var lockType = document.createElementNS("DAV:", "D:locktype");
            lockType.appendChild(document.createElementNS("DAV:", "D:write"));
            lockInfo.appendChild(lockType);
            final var owner = document.createElementNS("DAV:", "D:owner");
            final var href = document.createElementNS("DAV:", "D:href");
            href.setTextContent(lockOwner);
            owner.appendChild(href);
            lockInfo.appendChild(owner);
            final var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            final var out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            return out.toString();
        } catch (final Exception e) {
            throw new IOException("Unable to build LOCK request", e);
        }
    }

    private static String parseLockToken(final String xml) throws IOException {
        if (!isNotEmpty(xml)) {
            return null;
        }
        final var document = parseXml(xml);
        final var nodes = document.getElementsByTagNameNS("DAV:", "locktoken");
        if (nodes.getLength() == 0) {
            return null;
        }
        final var hrefs = ((Element) nodes.item(0)).getElementsByTagNameNS("DAV:", "href");
        return hrefs.getLength() == 0 ? null : hrefs.item(0).getTextContent().trim();
    }

    private static String getFirstChildText(final Element parent, final String localName) {
        final NodeList list = parent.getElementsByTagNameNS("DAV:", localName);
        return list.getLength() == 0 ? null : list.item(0).getTextContent().trim();
    }

    private static Document parseXml(final String xml) throws IOException {
        try {
            final var builder = newDocumentBuilderFactory().newDocumentBuilder();
            return builder.parse(new org.xml.sax.InputSource(new StringReader(xml)));
        } catch (final Exception e) {
            throw new IOException("Unable to parse XML", e);
        }
    }

    private static Document newDocument() throws Exception {
        return newDocumentBuilderFactory().newDocumentBuilder().newDocument();
    }

    private static DocumentBuilderFactory newDocumentBuilderFactory() throws Exception {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory;
    }

    private static void closeQuietly(final Object response) {
        try {
            if (response instanceof AutoCloseable closable) {
                closable.close();
            }
        } catch (final Exception e) {
            _log.debug("Ignoring response close failure", e);
        }
    }

    /** Custom WebDAV PROPFIND request. */
    private static final class HttpPropfind extends HttpUriRequestBase {
        HttpPropfind(final URI uri) {
            super("PROPFIND", uri);
        }
    }

    /** Custom WebDAV MKCOL request. */
    private static final class HttpMkcol extends HttpUriRequestBase {
        HttpMkcol(final URI uri) {
            super("MKCOL", uri);
        }
    }

    /** Custom WebDAV MOVE request. */
    private static final class HttpMove extends HttpUriRequestBase {
        HttpMove(final URI uri) {
            super("MOVE", uri);
        }
    }

    /** Custom WebDAV LOCK request. */
    private static final class HttpLock extends HttpUriRequestBase {
        HttpLock(final URI uri) {
            super("LOCK", uri);
        }
    }

    /** Custom WebDAV UNLOCK request. */
    private static final class HttpUnlock extends HttpUriRequestBase {
        HttpUnlock(final URI uri) {
            super("UNLOCK", uri);
        }
    }

    /** Custom socket factory wrapper. */
    private static final class ConfigConnectionSocketFactory extends PlainConnectionSocketFactory {
        private final ClientSocketFactory socketFactory;

        ConfigConnectionSocketFactory(final ClientSocketFactory socketFactory) {
            this.socketFactory = socketFactory;
        }

        @Override
        public java.net.Socket createSocket(final org.apache.hc.core5.http.protocol.HttpContext context)
                throws IOException {
            return socketFactory.getConfiguredWrapper(super.createSocket(context));
        }
    }
}
