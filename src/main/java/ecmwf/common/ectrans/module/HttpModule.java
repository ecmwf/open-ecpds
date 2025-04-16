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
 * ECMWF Product Data Store (ECPDS) Project
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
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_ALLOW_CIRCULAR_REDIRECTS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_ALTERNATIVE_PATH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_ATTRIBUTE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_AUTHCACHE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_AUTHHEADER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_CREDENTIALS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_DODIR;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_ENABLE_CONTENT_COMPRESSION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_ENCODE_URL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_HAS_PARAMETERS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_FAIL_ON_EMPTY_SYMLINK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_FILENAME_ATTRIBUTE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_FTPGROUP;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_FTPUSER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_FTP_LIKE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_HEADERS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_IS_SYMLINK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_LIST_MAX_DIRS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_LIST_MAX_FILES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_LIST_MAX_THREADS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_LIST_MAX_WAITING;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_LIST_RECURSIVE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MAX_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_AWAIT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_CLEAN_START;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_CONNECTION_TIMEOUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_HREF;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_ALTERNATIVE_NAME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_TIME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_KEEP_ALIVE_INTERVAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_MAX_FILES;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_MODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_PERSISTENCE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_PERSISTENCE_DIRECTORY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_PERSISTENCE_MODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_QOS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_SCHEME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_SESSION_EXPIRY_INTERVAL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_SUBSCRIBER_ID;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MQTT_URL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_MULTIPART_MODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_PORT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_PROTOCOL;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_PROXY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_SCHEME;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_SELECT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_SSL_VALIDATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_STRICT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_SUPPORTED_PROTOCOLS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_UPLOAD_END_POINT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_URLDIR;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_USE_HEAD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_USE_MULTIPART;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_USE_POST;
import static ecmwf.common.text.Util.isEmpty;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicHeaderElementIterator;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.ClientSocketFactory;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SSLClientSocketFactory;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.ExecutorManager;
import ecmwf.common.technical.ExecutorRunnable;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;
import ecmwf.common.version.Version;

/**
 * The Class HttpModule.
 */
public final class HttpModule extends TransferModule {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(HttpModule.class);

    /** The currentStatus. */
    private String currentStatus = "INIT";

    /** The in. */
    private InputStream httpInput;

    /** The scheme. */
    private String scheme = "http";

    /** The host. */
    private String host = null;

    /** The username. */
    private String username = null;

    /** The password. */
    private String password = null;

    /** The httpClient. */
    private CloseableHttpClient httpClient = null;

    /** The currentSetup. */
    private ECtransSetup currentSetup = null;

    /** The targetHttpHost. */
    private HttpHost targetHttpHost = null;

    /** The authCache. */
    private AuthCache authCache = null;

    /** The poolManager. */
    private PoolingHttpClientConnectionManager poolManager = null;

    /** The headers list. */
    private final Map<String, String> headersList = new ConcurrentHashMap<>();

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** The socket factory. */
    private ClientSocketFactory socketFactory = null;

    /** The ssl socket factory. */
    private SSLClientSocketFactory sslSocketFactory = null;

    /** The mqtt subscriber. */
    private MqttClient mqttSubscriber = null;

    /** The executor manager. */
    private ExecutorManager<ListThread> manager = null;

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() {
        return currentStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the port.
     */
    @Override
    public int getPort(final ECtransSetup setup) {
        return setup.getInteger(HOST_HTTP_PORT);
    }

    /**
     * {@inheritDoc}
     *
     * Update socket statistics.
     */
    @Override
    public void updateSocketStatistics() throws IOException {
        if (socketFactory != null) {
            socketFactory.updateStatistics();
        }
        if (sslSocketFactory != null) {
            sslSocketFactory.updateStatistics();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Connect.
     */
    @Override
    public void connect(final String location, final ECtransSetup setup) throws IOException {
        // The location is: user:password@host/dir
        currentSetup = setup;
        setStatus("CONNECT");
        int pos;
        if ((pos = location.lastIndexOf("@")) == -1) {
            throw new IOException("Malformed URL ('@' not found)");
        }
        host = location.substring(pos + 1);
        username = location.substring(0, pos);
        if ((pos = username.indexOf(":")) == -1) {
            throw new IOException("Malformed URL (':' not found)");
        }
        password = username.substring(pos + 1);
        username = username.substring(0, pos);
        if ((pos = host.indexOf("/")) != -1) {
            // Ignore dir (not used)
            host = host.substring(0, pos);
        }
        // Add the minimum required headers!
        headersList.put("User-Agent", "ecpds/" + Version.getFullVersion());
        headersList.put("Accept", "*/*");
        // And the ones in the configuration (possibly overwriting User-Agent & Accept)
        final var headers = new BufferedReader(new StringReader(getSetup().getString(HOST_HTTP_HEADERS)));
        String line;
        while ((line = headers.readLine()) != null) {
            final var index = line.indexOf(":");
            if (index != -1) {
                headersList.put(line.substring(0, index).trim(), line.substring(index + 1).trim());
            }
        }
        final var port = getPort(getSetup());
        scheme = getSetup().getString(HOST_HTTP_SCHEME);
        _log.debug("Connection on {}://{}:{}{}", scheme, host, port, isNotEmpty(username) ? " (" + username + ")" : "");
        var connected = false;
        setAttribute("remote.hostName", host);
        targetHttpHost = new HttpHost(scheme, host, port);
        if (_log.isDebugEnabled()) {
            _log.debug("Target host URI: {}", targetHttpHost.toURI());
        }
        final var builder = HttpClients.custom();
        if (!getSetup().getBoolean(HOST_HTTP_ENABLE_CONTENT_COMPRESSION)) {
            _log.debug("Disable on-the-fly content compression");
            builder.disableContentCompression();
        }
        final var proxy = getSetup().getOptions(HOST_HTTP_PROXY);
        if (!proxy.isEmpty()) {
            final var proxyHost = proxy.get("host", null);
            if (proxyHost != null) {
                final var proxyProtocol = proxy.get("protocol", "http");
                final var proxyPort = proxy.get("port", 8080);
                final var host = new HttpHost(proxyProtocol, proxyHost, proxyPort);
                _log.debug("Using proxy: {}://{}:{}", proxyProtocol, proxyHost, proxyPort);
                builder.setProxy(host);
            }
        }
        if (getSetup().getBoolean(HOST_HTTP_ALLOW_CIRCULAR_REDIRECTS)) {
            _log.debug("Allow circular redirects");
            builder.setDefaultRequestConfig(RequestConfig.custom().setCircularRedirectsAllowed(true).build());
        }
        final ClientSocketStatistics statistics;
        if (setup.getBoolean(HOST_ECTRANS_SOCKET_STATISTICS) && getAttribute("connectOptions") != null) {
            _log.debug("Activating Socket Statistics");
            statistics = new ClientSocketStatistics();
            setAttribute(statistics);
        } else {
            statistics = null;
        }
        final var socketConfig = new SocketConfig(statistics, "HTTPSocketConfig", getDebug());
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
        try {
            _log.debug("Setting Pool Manager");
            socketFactory = new ClientSocketFactory(socketConfig);
            sslSocketFactory = socketConfig.getSSLSocketFactory(getSetup().getString(HOST_HTTP_PROTOCOL),
                    getSetup().getBoolean(HOST_HTTP_SSL_VALIDATION));
            poolManager = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory> create()
                    .register("http", new ConfigConnectionSocketFactory(socketFactory))
                    .register("https",
                            new SSLConnectionSocketFactory(sslSocketFactory,
                                    getSetup().getStringList(HOST_HTTP_SUPPORTED_PROTOCOLS).toArray(new String[0]),
                                    null, !getSetup().getBoolean(HOST_HTTP_STRICT) ? new NoopHostnameVerifier() : null))
                    .build());
            final int maxTotal = getSetup().getInteger(HOST_HTTP_LIST_MAX_THREADS);
            poolManager.setDefaultMaxPerRoute(maxTotal);
            poolManager.setMaxTotal(maxTotal);
            builder.setConnectionManager(poolManager);
            builder.setKeepAliveStrategy(_keepAliveStrategy);
            if (!username.isEmpty()) {
                if (getSetup().getBoolean(HOST_HTTP_AUTHHEADER)) {
                    final var auth = username + ":" + password;
                    final var encodedAuth = Base64.getEncoder()
                            .encodeToString(auth.getBytes(StandardCharsets.US_ASCII));
                    headersList.put(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
                } else // Doing authentication with user/password
                if (getSetup().getBoolean(HOST_HTTP_CREDENTIALS)) {
                    final var credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(new AuthScope(null, -1),
                            new UsernamePasswordCredentials(username, password.toCharArray()));
                    builder.setDefaultCredentialsProvider(credsProvider);
                    if (getSetup().getBoolean(HOST_HTTP_AUTHCACHE)) {
                        authCache = new BasicAuthCache();
                        final var basicAuth = new BasicScheme();
                        authCache.put(targetHttpHost, basicAuth);
                    }
                }
            }
            httpClient = builder.build();
            connected = true;
        } catch (final Exception e) {
            _log.error("Connection failed to host {}:{}", host, port, e);
            final var message = e.getMessage();
            throw new IOException(isNotEmpty(message) ? message : "connection failed to host " + host + ":" + port);
        } finally {
            if (!connected) {
                setStatus("ERROR");
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    } catch (final Throwable ignored) {
                        // Ignore exception!
                    }
                }
            }
        }
    }

    /**
     * A factory for creating ConfigConnectionSocket objects.
     */
    private static class ConfigConnectionSocketFactory extends PlainConnectionSocketFactory {

        /** The socket factory. */
        final ClientSocketFactory socketFactory;

        /**
         * Instantiates a new config connection socket factory.
         *
         * @param socketFactory
         *            the socket factory
         */
        ConfigConnectionSocketFactory(final ClientSocketFactory socketFactory) {
            this.socketFactory = socketFactory;
        }

        /**
         * Connect socket.
         *
         * @param connectTimeout
         *            the connect timeout
         * @param socket
         *            the socket
         * @param host
         *            the host
         * @param remoteAddress
         *            the remote address
         * @param localAddress
         *            the local address
         * @param context
         *            the context
         *
         * @return the socket
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public Socket connectSocket(final TimeValue connectTimeout, final Socket socket, final HttpHost host,
                final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpContext context)
                throws IOException {
            return socketFactory.getConfiguredWrapper(
                    super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context));
        }
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final String name) throws IOException {
        _log.debug("Del file {}", name);
        final var pr = new PrepareRequest(name);
        final var request = new HttpDelete(encodePath(getSetup().getBoolean(HOST_HTTP_ENCODE_URL),
                getSetup().getBoolean(HOST_HTTP_HAS_PARAMETERS) ? name : pr.getPath()));
        ClassicHttpResponse delResponse = null;
        try {
            delResponse = execute(pr.getHttpHost(), request, 200);
            final var entity = delResponse.getEntity();
            if (entity == null) {
                throw new IOException("Couldn't delete file: " + request.getRequestUri());
            }
            if (getDebug()) {
                _log.debug("Del: {}", request.getRequestUri());
            }
        } finally {
            closeResponse(delResponse);
        }
    }

    /**
     * Gets the http put or post entity. Utility for the put methods.
     *
     * @param input
     *            the input
     * @param filename
     *            the filename
     *
     * @return the http put or post entity
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private HttpEntity getHttpPutOrPostEntity(final InputStream input, final String filename) throws IOException {
        if (getSetup().getBoolean(HOST_HTTP_USE_MULTIPART)) {
            final var builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.valueOf(getSetup().getString(HOST_HTTP_MULTIPART_MODE).toUpperCase()));
            builder.addBinaryBody(getSetup().getString(HOST_HTTP_FILENAME_ATTRIBUTE), input.readAllBytes(),
                    ContentType.APPLICATION_OCTET_STREAM, filename);
            return builder.build();
        }
        return new InputStreamEntity(input, null);
    }

    /**
     * Process put or post. Utility for the put methods.
     *
     * @param input
     *            the input
     * @param filename
     *            the filename
     * @param posn
     *            the posn
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void processPutOrPost(final InputStream input, final String filename, final long posn) throws IOException {
        setStatus("PUT");
        if (posn > 0) {
            throw new IOException("Append PUT not supported by the " + getSetup().getModuleName() + " module");
        }
        final var endPoint = getSetup().getString(HOST_HTTP_UPLOAD_END_POINT);
        final var name = isEmpty(endPoint) ? filename : endPoint;
        final var pr = new PrepareRequest(name);
        final var uri = encodePath(getSetup().getBoolean(HOST_HTTP_ENCODE_URL),
                getSetup().getBoolean(HOST_HTTP_HAS_PARAMETERS) ? name : pr.getPath());
        final var request = getSetup().getBoolean(HOST_HTTP_USE_POST) ? new HttpPost(uri) : new HttpPut(uri);
        request.setEntity(getHttpPutOrPostEntity(input, filename));
        ClassicHttpResponse putResponse = null;
        try {
            putResponse = execute(pr.getHttpHost(), request, 200, 201);
        } finally {
            closeResponse(putResponse);
        }
        setAttribute("remote.fileName", filename);
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        _log.debug("Put file {} (posn={}) (size={})", name, posn, size);
        final var output = new PipedOutputStream();
        processPutOrPost(new PipedInputStream(output, StreamPlugThread.DEFAULT_BUFF_SIZE), name, posn);
        return output;
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public boolean put(final InputStream input, final String name, final long posn, final long size)
            throws IOException {
        _log.debug("Put file (inputstream) {} (posn={}) (size={})", name, posn, size);
        processPutOrPost(input, name, posn);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        _log.debug("Get file {}", name);
        setStatus("GET");
        if (posn > 0) {
            throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
        }
        final var pr = new PrepareRequest(name);
        final HttpUriRequestBase request = new HttpGet(encodePath(getSetup().getBoolean(HOST_HTTP_ENCODE_URL),
                getSetup().getBoolean(HOST_HTTP_HAS_PARAMETERS) ? name : pr.getPath()));
        ClassicHttpResponse response = null;
        try {
            response = execute(pr.getHttpHost(), request, 200);
            final var entity = response.getEntity();
            if (entity == null) {
                throw new IOException("Couldn't get file: " + request.getRequestUri());
            }
            if (getDebug()) {
                try {
                    _log.debug("Get: {}", request.getRequestUri());
                } catch (Throwable t) {
                    _log.warn("Logging request URI", t);
                }
            }
            httpInput = entity.getContent();
            setAttribute("remote.fileName", name);
            return httpInput;
        } catch (IOException | RuntimeException ex) {
            // Clean up response if something went wrong
            if (response != null) {
                try {
                    EntityUtils.consumeQuietly(response.getEntity());
                } catch (Exception cleanupEx) {
                    _log.warn("Error while cleaning up entity", cleanupEx);
                }
            }
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String name) throws IOException {
        _log.debug("Size {}", name);
        setStatus("SIZE");
        final var pr = new PrepareRequest(name);
        final var uri = encodePath(getSetup().getBoolean(HOST_HTTP_ENCODE_URL),
                getSetup().getBoolean(HOST_HTTP_HAS_PARAMETERS) ? name : pr.getPath());
        final var request = getSetup().getBoolean(HOST_HTTP_USE_HEAD) ? new HttpHead(uri) : new HttpGet(uri);
        ClassicHttpResponse sizeResponse = null;
        try {
            sizeResponse = execute(pr.getHttpHost(), request, 200);
            final var entity = sizeResponse.getEntity();
            final long size;
            if (entity == null) {
                final var contentLength = sizeResponse.getLastHeader("Content-Length");
                if (contentLength == null) {
                    // There is no size specified in the header, so this is probably a directory!
                    throw new IOException("Couldn't get size: " + request.getRequestUri());
                }
                size = Long.parseLong(contentLength.getValue());
            } else {
                size = entity.getContentLength();
            }
            if (getDebug()) {
                _log.debug("Size: {} => {}", request.getRequestUri(), size);
            }
            return size;
        } finally {
            closeResponse(sizeResponse);
        }
    }

    /**
     * Decode.
     *
     * @param url
     *            the url
     *
     * @return the string
     */
    private static String decode(final String url) {
        try {
            var prevURL = "";
            var decodeURL = url;
            while (!prevURL.equals(decodeURL)) {
                prevURL = decodeURL;
                decodeURL = URLDecoder.decode(decodeURL, "UTF-8");
            }
            return decodeURL;
        } catch (final UnsupportedEncodingException e) {
            _log.warn("Error decoding url: {}", url, e);
            return url;
        }
    }

    /**
     * Encode.
     *
     * @param url
     *            the url
     *
     * @return the string
     */
    private static String encode(final String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            _log.warn("Error encoding url: {}", url, e);
            return url;
        }
    }

    /**
     * Decode path.
     *
     * @param required
     *            the required
     * @param path
     *            the path
     *
     * @return the string
     */
    private static String decodePath(final boolean required, final String path) {
        if (!required) {
            return path;
        }
        final var sb = new StringBuilder();
        final var tokenizer = new StringTokenizer(path, "/");
        while (tokenizer.hasMoreTokens()) {
            final var token = tokenizer.nextToken();
            final var isUrl = sb.length() == 0 && ("http:".equalsIgnoreCase(token) || "https:".equalsIgnoreCase(token));
            sb.append(decode(isUrl ? token + "/" : token));
            if (tokenizer.hasMoreTokens()) {
                sb.append("/");
            }
        }
        return (path.startsWith("/") ? "/" : "") + sb.toString() + (path.endsWith("/") ? "/" : "");
    }

    /**
     * Encode path.
     *
     * @param required
     *            the required
     * @param path
     *            the path
     *
     * @return the string
     */
    private static String encodePath(final boolean required, final String path) {
        if (!required) {
            return path;
        }
        final var sb = new StringBuilder();
        final var tokenizer = new StringTokenizer(path, "/");
        while (tokenizer.hasMoreTokens()) {
            final var token = tokenizer.nextToken();
            if (".".equals(token)) { // We should replace "/./" by "/" always!
                continue;
            }
            sb.append(encode(token));
            if (tokenizer.hasMoreTokens()) {
                sb.append("/");
            }
        }
        return (path.startsWith("/") ? "/" : "") + sb.toString() + (path.endsWith("/") ? "/" : "");
    }

    /**
     * Gets the full name.
     *
     * @param directory
     *            the directory
     * @param name
     *            the name
     *
     * @return the string
     */
    private static String getFullName(final String directory, final String name) {
        return !directory.isEmpty() && directory.equals(name) ? name
                : directory + (!isURL(name) && !directory.endsWith("/") && !name.startsWith("/") ? "/" : "") + name;
    }

    /**
     * The Class FtpEntry.
     */
    private static final class FtpEntry {

        /** The permission. */
        final String permission;

        /** The user. */
        final String user;

        /** The group. */
        final String group;

        /** The size. */
        final String size;

        /** The time. */
        final long time;

        /** The name. */
        final String name;

        /** The full name. */
        final String fullName;

        /** The error. */
        final String error;

        /**
         * Instantiates a new ftp entry.
         *
         * @param directory
         *            the directory
         * @param permission
         *            the permission
         * @param user
         *            the user
         * @param group
         *            the group
         * @param size
         *            the size
         * @param time
         *            the time
         * @param name
         *            the name
         * @param error
         *            the error
         */
        FtpEntry(final String directory, final String permission, final String user, final String group,
                final String size, final long time, final String name, final String error) {
            this.permission = permission;
            this.user = user;
            this.group = group;
            this.size = size;
            this.time = time;
            this.name = name;
            this.fullName = getFullName(directory, name);
            this.error = error;
        }

        /**
         * Gets the ftp list.
         *
         * @param rootDirectory
         *            the root directory
         *
         * @return the ftp list
         */
        String getFtpList(final String rootDirectory) {
            return Format.getFtpList(permission, user, group, size, time,
                    fullName.startsWith(rootDirectory + (rootDirectory.endsWith("/") ? "" : "/"))
                            ? fullName.substring(rootDirectory.length()) : fullName);
        }

        /**
         * Checks if has error.
         *
         * @return true if has error
         */
        boolean hasError() {
            return error != null;
        }

        /**
         * Gets the error.
         *
         * @return the error
         */
        String getError() {
            return error;
        }

    }

    /**
     * Checks if is url. Check if the path is a url (starting with either http:// or https://)?
     *
     * @param url
     *            the url
     *
     * @return true, if is url
     */
    private static boolean isURL(final String url) {
        final var lowerCaseUrl = url.toLowerCase();
        return lowerCaseUrl.startsWith("http://") || lowerCaseUrl.startsWith("https://");
    }

    /**
     * The Class PrepareRequest. Prepare the request depending if it use the listing host or another host!
     */
    private final class PrepareRequest {

        /** The alternative host. */
        private boolean alternativeHost = false;

        /** The target host. */
        private HttpHost targetHost = targetHttpHost;

        /** The path. */
        private String path;

        /**
         * Instantiates a new prepare request.
         *
         * @param name
         *            the name
         */
        PrepareRequest(final String name) {
            if (isURL(path = name)) {
                // This is a full URL!
                if (getDebug()) {
                    _log.debug("URL detected: {}", name);
                }
                URL url = null;
                try {
                    url = new URI(name).toURL();
                } catch (final Exception e) {
                    _log.warn("Parsing URL {}", name, e);
                }
                if (url != null) {
                    final var port = url.getPort();
                    targetHost = new HttpHost(url.getProtocol(), url.getHost(),
                            port != -1 ? port : url.getDefaultPort());
                    path = url.getPath();
                    alternativeHost = true;
                    if (_log.isDebugEnabled() && getDebug()) {
                        _log.debug("Target host: {}", targetHost.toURI());
                    }
                }
            }
        }

        /**
         * Gets the http host.
         *
         * @return the http host
         */
        HttpHost getHttpHost() {
            return targetHost;
        }

        /**
         * Checks if is alternative host.
         *
         * @return true, if is alternative host
         */
        boolean isAlternativeHost() {
            return alternativeHost;
        }

        /**
         * Gets the path.
         *
         * @return the path
         */
        String getPath() {
            return path;
        }
    }

    /**
     * Gets the element.
     *
     * @param directory
     *            the directory
     * @param name
     *            the name
     *
     * @return the ftp entry
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private FtpEntry getElement(final String directory, final String name, final String ownerUser,
            final String ownerGroup) throws IOException {
        final var pr = new PrepareRequest(name);
        final var path = pr.getPath();
        final var fullName = encodePath(getSetup().getBoolean(HOST_HTTP_ENCODE_URL),
                getFullName(directory, getSetup().getBoolean(HOST_HTTP_HAS_PARAMETERS) ? name : pr.getPath()));
        final var request = getSetup().getBoolean(HOST_HTTP_USE_HEAD) ? new HttpHead(fullName) : new HttpGet(fullName);
        ClassicHttpResponse getResponse = null;
        try {
            getResponse = execute(pr.getHttpHost(), request, 200);
            final var entity = getResponse.getEntity();
            final long size;
            if (entity == null) {
                final var contentLength = getResponse.getLastHeader("Content-Length");
                if (contentLength != null) {
                    size = Long.parseLong(contentLength.getValue());
                } else {
                    // There is no size specified in the header, so this is probably a directory!
                    size = -1;
                }
            } else {
                size = entity.getContentLength();
            }
            if (getDebug()) {
                _log.debug("Size: {}", size);
            }
            final boolean isDirectory;
            boolean isSymlink;
            if (size == -1) {
                if (path.endsWith("/")) {
                    isDirectory = true;
                    isSymlink = false;
                } else {
                    isDirectory = false;
                    isSymlink = true;
                }
            } else {
                isDirectory = false;
                isSymlink = false;
            }
            var date = System.currentTimeMillis();
            final var lastModified = getResponse.getLastHeader("Last-Modified");
            if (lastModified != null) {
                final var value = lastModified.getValue();
                if (getDebug()) {
                    _log.debug("LastModified: {}", value);
                }
                try {
                    date = DateUtils.parseStandardDate(value).toEpochMilli();
                } catch (final Throwable t) {
                    if (getDebug()) {
                        _log.warn("Parsing {}", value, t);
                    }
                }
            }
            String filename = null;
            isSymlink = getSetup().get(HOST_HTTP_IS_SYMLINK, isSymlink);
            if (isSymlink) {
                // If it is not a directory and the size is -1 then we might have a filename in
                // the header?
                final var contentDisposition = getResponse.getLastHeader("Content-Disposition");
                if (contentDisposition != null) {
                    final var contentDispositionValue = contentDisposition.getValue();
                    String fileNameFound = null;
                    if (contentDispositionValue.contains("filename=")) {
                        // Extract the filename from the contentDisposition string
                        final var parts = contentDispositionValue.split("filename=");
                        if (parts.length > 1) {
                            fileNameFound = parts[1].replace("\"", "").trim();
                        }
                    } else {
                        fileNameFound = contentDispositionValue;
                    }
                    if (fileNameFound != null) {
                        filename = decode(fileNameFound).replace(" ", "+");
                    }
                }
                // If we have no filename found then what should we do?
                if (filename == null && getSetup().getBoolean(HOST_HTTP_FAIL_ON_EMPTY_SYMLINK)) {
                    throw new IOException("No Content-Disposition found in header");
                }
            }
            return new FtpEntry(directory, isSymlink ? "lrwxrwxrwx" : isDirectory ? "drw-r--r--" : "-rw-r--r--",
                    getSetup().get(HOST_HTTP_FTPUSER, ownerUser), getSetup().get(HOST_HTTP_FTPGROUP, ownerGroup),
                    String.valueOf(isSymlink ? 1 : isDirectory ? 1024 : size), date,
                    (pr.isAlternativeHost() ? name : path)
                            + (isSymlink && isNotEmpty(filename) ? " -> " + filename : ""),
                    null);
        } catch (final IOException e) {
            return new FtpEntry(directory, "?rw-r--r--", getSetup().get(HOST_HTTP_FTPUSER, ownerUser),
                    getSetup().get(HOST_HTTP_FTPGROUP, ownerGroup), "?", System.currentTimeMillis(),
                    pr.isAlternativeHost() ? name : path, e.getMessage());
        } finally {
            closeResponse(getResponse);
        }
    }

    /**
     * The Interface ProcessEntry.
     */
    private interface ProcessEntry {

        /**
         * Adds the.
         *
         * @param line
         *            the line
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void add(String line) throws IOException;

        /**
         * Adds the all.
         *
         * @param lines
         *            the lines
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void addAll(List<String> lines) throws IOException;

        /**
         * Contains.
         *
         * @param line
         *            the line
         *
         * @return true, if successful
         */
        boolean contains(final String line);

        /**
         * Size.
         *
         * @return the int
         */
        int size();

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        default void close() throws IOException {
        }
    }

    /**
     * The Class ProcessEntryAsList.
     */
    private class ProcessEntryAsList implements ProcessEntry {

        /** The result list. */
        final List<String> resultList = Collections.synchronizedList(new ArrayList<>());

        /**
         * Adds the.
         *
         * @param line
         *            the line
         */
        @Override
        public void add(final String line) {
            if (getDebug()) {
                _log.debug("Adding new line: {}", line);
            }
            resultList.add(line);
        }

        /**
         * Adds the all.
         *
         * @param lines
         *            the lines
         */
        @Override
        public void addAll(final List<String> lines) {
            resultList.addAll(lines);
        }

        /**
         * Contains.
         *
         * @param line
         *            the line
         *
         * @return true, if successful
         */
        @Override
        public boolean contains(final String line) {
            return resultList.contains(line);
        }

        /**
         * Size.
         *
         * @return the int
         */
        @Override
        public int size() {
            return resultList.size();
        }

        /**
         * Gets the list.
         *
         * @return the list
         */
        public List<String> getList() {
            return resultList;
        }
    }

    /**
     * The Class ProcessEntryAsOutput.
     */
    private class ProcessEntryAsOutput implements ProcessEntry, Closeable {

        /** The count. */
        final AtomicInteger count = new AtomicInteger(0);

        /** The out. */
        final OutputStream out;

        /**
         * Instantiates a new process entry as output.
         *
         * @param out
         *            the out
         */
        ProcessEntryAsOutput(final OutputStream out) {
            this.out = out;
        }

        /**
         * Adds the.
         *
         * @param line
         *            the line
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void add(final String line) throws IOException {
            if (getDebug()) {
                _log.debug("Writing new line: {}", line);
            }
            boolean successful = false;
            try {
                out.write((line + "\n").getBytes());
                out.flush();
                successful = true;
                count.addAndGet(1);
            } finally {
                if (!successful) {
                    _log.warn("Underlying output not healthy");
                    StreamPlugThread.closeQuietly(ProcessEntryAsOutput.this);
                    StreamPlugThread.closeQuietly(HttpModule.this);
                }
            }
        }

        /**
         * Adds the all.
         *
         * @param lines
         *            the lines
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void addAll(final List<String> lines) throws IOException {
            for (final String line : lines) {
                add(line);
            }
        }

        /**
         * Contains.
         *
         * @param line
         *            the line
         *
         * @return true, if successful
         */
        @Override
        public boolean contains(final String line) {
            return false;
        }

        /**
         * Size.
         *
         * @return the int
         */
        @Override
        public int size() {
            return count.get();
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            out.close();
        }
    }

    /**
     * {@inheritDoc}
     *
     * List.
     */
    @Override
    public void list(final String directory, final String pattern, final OutputStream out) throws IOException {
        final ProcessEntry resultList = new ProcessEntryAsOutput(out);
        _log.debug("List{}{}{}", !directory.isEmpty() ? " " + directory : "",
                isNotEmpty(pattern) ? " (" + pattern + ")" : "",
                getSetup().getBoolean(HOST_HTTP_MQTT_MODE) ? " (MQTT)" : "");
        setStatus("LIST");
        manager = getSetup().getBoolean(HOST_HTTP_FTP_LIKE)
                ? new ExecutorManager<>(getSetup().getInteger(HOST_HTTP_LIST_MAX_WAITING),
                        getSetup().getInteger(HOST_HTTP_LIST_MAX_THREADS))
                : null;
        // Let's start the listing!
        list(manager, resultList, directory, directory, pattern, 0);
        if (manager != null) {
            // We don't want to take more jobs!
            manager.stopRun();
            // And now we wait for all the Threads to complete!
            try {
                manager.join();
            } catch (final InterruptedException e) {
                _log.warn("Interrupted", e);
            }
        }
        // Make sure the result list is ready to be sent!
        _log.debug("List completed");
        resultList.close();
    }

    /**
     * {@inheritDoc}
     *
     * List as string array.
     */
    @Override
    public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
        final var resultList = new ProcessEntryAsList();
        if (_log.isDebugEnabled()) {
            _log.debug("List{}{}{}", !directory.isEmpty() ? " " + directory : "",
                    isNotEmpty(pattern) ? " (" + pattern + ")" : "",
                    getSetup().getBoolean(HOST_HTTP_MQTT_MODE) ? " (MQTT)" : "");
        }
        setStatus("LIST");
        manager = getSetup().getBoolean(HOST_HTTP_FTP_LIKE)
                ? new ExecutorManager<>(getSetup().getInteger(HOST_HTTP_LIST_MAX_WAITING),
                        getSetup().getInteger(HOST_HTTP_LIST_MAX_THREADS))
                : null;
        // Let's start the listing!
        list(manager, resultList, directory, directory, pattern, 0);
        if (manager != null) {
            // We don't want to take more jobs!
            manager.stopRun();
            // And now we wait for all the Threads to complete!
            try {
                manager.join();
            } catch (final InterruptedException e) {
                _log.warn("Interrupted", e);
            }
        }
        final var results = resultList.getList();
        if (getDebug()) {
            var i = 0;
            for (final String result : results) {
                _log.debug("Line[{}]: {}", i++, result);
            }
            _log.debug("Total: {}", resultList.size());
        }
        return results.toArray(new String[resultList.size()]);
    }

    /**
     * Close response.
     *
     * @param response
     *            the response
     */
    private void closeResponse(final ClassicHttpResponse response) {
        if (response != null) {
            final var entity = response.getEntity();
            if (entity != null) {
                try {
                    if (getDebug() && _log.isDebugEnabled()) {
                        _log.debug("HTTP response body: {}", EntityUtils.toString(entity));
                    } else {
                        EntityUtils.consume(entity);
                    }
                } catch (final Throwable t) {
                    _log.warn("Consuming entity", t);
                }
            }
        }
    }

    /**
     * Adds the entry.
     *
     * @param manager
     *            the manager
     * @param resultList
     *            the result list
     * @param rootDirectory
     *            the root directory
     * @param directory
     *            the directory
     * @param line
     *            the line
     * @param level
     *            the level
     * @param pattern
     *            the pattern
     * @param counter
     *            the counter
     * @param size
     *            the size
     * @param date
     *            the date
     */
    private void addEntry(final ExecutorManager<ListThread> manager, final ProcessEntry resultList,
            final String rootDirectory, final String directory, final String line, final int level,
            final String pattern, final AtomicInteger counter, final String alternativeName, final ByteSize size,
            final Long date) {
        try {
            if (isEmpty(pattern) || line.matches(pattern)) {
                // We don't want to have duplicated lines!
                if (!resultList.contains(line)) {
                    if (getDebug()) {
                        _log.debug("Adding [{}]: {}", counter.getAndAdd(1), line);
                    }
                    // Is it required to transform the listing to an ftp like listing? If yes then
                    // we need to check the properties for every single file in the list.
                    if (manager != null) {
                        manager.startIfNotStarted();
                        // Let's create a new list task and add it to the list manager queue!
                        try {
                            manager.put(new ListThread(manager, resultList, rootDirectory, directory, line, level,
                                    pattern, alternativeName, size, date));
                        } catch (final InterruptedException e) {
                        }
                    } else {
                        // No ftp processing so no recursion!
                        resultList.add(line);
                    }
                } else {
                    if (getDebug()) {
                        _log.debug("Discarding {} (duplicate)", line);
                    }
                }
            } else if (getDebug()) {
                _log.debug("Discarding {} (wrong-pattern)", line);
            }
        } catch (final Exception e) {
            _log.warn("Matching pattern", e);
        }
    }

    /**
     * Format an Element to plain-text
     *
     * @param element
     *            the root element to format
     *
     * @return formatted text
     */
    private static String getPlainText(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor.traverse(formatter, element); // walk the DOM, and call .head() and .tail() for each node
        return formatter.toString();
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private static class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if (name.equals("li"))
                append("\n * ");
            else if (name.equals("dt"))
                append("  ");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
                append("\n");
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n");
            else if (name.equals("a"))
                append(String.format(" <%s>", node.absUrl("href")));
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; // reset counter if starts with a newline. only from formats above, not in
                           // natural text
            if (text.equals(" ")
                    && (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String[] words = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                        word = word + " ";
                    if (word.length() + width > maxWidth) { // wrap and reset counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }

    /**
     * List.
     *
     * @param manager
     *            the manager
     * @param resultList
     *            the result list
     * @param rootDirectory
     *            the root directory
     * @param targetDirectory
     *            the target directory
     * @param pattern
     *            the pattern
     * @param level
     *            the level
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void list(final ExecutorManager<ListThread> manager, final ProcessEntry resultList,
            final String rootDirectory, final String targetDirectory, final String pattern, final int level)
            throws IOException {
        final var resultListSize = resultList.size();
        var directory = targetDirectory;
        var listSize = 0;
        final var counter = new AtomicInteger(0);
        if (getSetup().getBoolean(HOST_HTTP_MQTT_MODE)) {
            // This is an MQTT server so we are going to subscribe to get the list of data
            // file references!
            final var brokerUrl = getSetup().get(HOST_HTTP_MQTT_URL, getSetup().getString(HOST_HTTP_MQTT_SCHEME) + "://"
                    + host + ":" + getSetup().getInteger(HOST_HTTP_MQTT_PORT));
            var disconnected = false;
            try {
                final var connOpts = new MqttConnectionOptions();
                connOpts.setHttpsHostnameVerificationEnabled(getSetup().getBoolean(HOST_HTTP_STRICT));
                if (!getSetup().getBoolean(HOST_HTTP_SSL_VALIDATION)) {
                    connOpts.setSocketFactory(SocketConfig
                            .getBlindlyTrustingSSLContext(getSetup().getString(HOST_HTTP_PROTOCOL)).getSocketFactory());
                }
                connOpts.setAutomaticReconnect(true);
                // true: no states across restarts
                connOpts.setCleanStart(getSetup().getBoolean(HOST_HTTP_MQTT_CLEAN_START));
                // Session duration (in conjunction with CleanStart to false!
                connOpts.setSessionExpiryInterval(
                        getSetup().getDuration(HOST_HTTP_MQTT_SESSION_EXPIRY_INTERVAL).getSeconds());
                connOpts.setUserName(username);
                connOpts.setPassword(password.getBytes());
                connOpts.setConnectionTimeout(
                        (int) getSetup().getDuration(HOST_HTTP_MQTT_CONNECTION_TIMEOUT).getSeconds());
                connOpts.setKeepAliveInterval(
                        (int) getSetup().getDuration(HOST_HTTP_MQTT_KEEP_ALIVE_INTERVAL).getSeconds());
                final var receivedSignal = new CountDownLatch(getSetup().getInteger(HOST_HTTP_MQTT_MAX_FILES));
                final var persistenceMode = getSetup().getString(HOST_HTTP_MQTT_PERSISTENCE_MODE);
                mqttSubscriber = new MqttClient(brokerUrl,
                        getSetup().get(HOST_HTTP_MQTT_SUBSCRIBER_ID, UUID.randomUUID().toString()),
                        getSetup().getBoolean(HOST_HTTP_MQTT_PERSISTENCE) ? "file".equalsIgnoreCase(persistenceMode)
                                ? new MqttDefaultFilePersistence(getSetup().get(HOST_HTTP_MQTT_PERSISTENCE_DIRECTORY,
                                        System.getProperty("java.io.tmpdir")))
                                : "memory".equalsIgnoreCase(persistenceMode) ? new MemoryPersistence() : null : null);
                mqttSubscriber.setCallback(new MqttCallback() {
                    @Override
                    public void mqttErrorOccurred(final MqttException e) {
                        _log.warn("MQTT error", e);
                    }

                    @Override
                    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                        receivedSignal.countDown();
                        if (getDebug())
                            _log.debug("messageArrived: topic={} ; debugString={}", topic, message.toDebugString());
                        try {
                            final var bindings = new HashMap<>(Map.of("mqttPayload",
                                    new ObjectMapper().readValue(
                                            new String(message.getPayload(), StandardCharsets.UTF_8), Map.class),
                                    "mqttTopic", topic));
                            final var href = getSetup().getString(HOST_HTTP_MQTT_HREF, bindings);
                            _log.debug("payload: {} : bindings: {} ; href: {}", new String(message.getPayload()),
                                    bindings, href);
                            if (isNotEmpty(href)) {
                                final var alternativeName = getSetup().getString(HOST_HTTP_MQTT_ALTERNATIVE_NAME,
                                        bindings);
                                final var size = getSetup().getByteSize(HOST_HTTP_MQTT_SIZE, bindings);
                                final var time = getSetup().getLong(HOST_HTTP_MQTT_TIME, bindings);
                                _log.debug("{} : {} : {} : {} : {}", bindings, href, alternativeName, size, time);
                                addEntry(manager, resultList, rootDirectory, targetDirectory, href, level, pattern,
                                        counter, alternativeName, size, time);
                            } else {
                                _log.debug("Notification ignored (no href found): {}", topic);
                            }
                        } catch (Throwable t) {
                            _log.debug("Notification ignored (href resolution error): {}", topic, t);
                        }
                    }

                    @Override
                    public void disconnected(final MqttDisconnectResponse arg0) {
                        _log.debug("disconnected (returnCode={})", arg0.getReturnCode());
                    }

                    @Override
                    public void connectComplete(final boolean error, final String url) {
                        _log.debug("Connect complete: {} (error={})", url, error);
                    }

                    @Override
                    public void authPacketArrived(final int arg0, final MqttProperties arg1) {
                        if (getDebug()) {
                            _log.debug("Auth packet arrived: {} -> {}", arg0, arg1);
                        }
                    }

                    @Override
                    public void deliveryComplete(final IMqttToken arg0) {
                        if (getDebug()) {
                            _log.debug("Delivery complete: {}", arg0);
                        }
                    }
                });
                _log.debug("Connecting to broker {}", brokerUrl);
                mqttSubscriber.connect(connOpts);
                mqttSubscriber.subscribe(directory, getSetup().getInteger(HOST_HTTP_MQTT_QOS)); // QoS=1
                Thread.sleep(getSetup().getDuration(HOST_HTTP_MQTT_AWAIT).toMillis());
                mqttSubscriber.disconnect();
                disconnected = true;
                _log.debug("Disconnected from broker");
            } catch (final Throwable e) {
                _log.warn("Connecting to broker {}", brokerUrl, e);
                final var cause = e.getCause();
                throw new IOException((cause != null ? cause : e).getMessage());
            } finally {
                if (mqttSubscriber != null) {
                    try {
                        mqttSubscriber.close(!disconnected);
                    } catch (final Throwable t) {
                        _log.warn("Closing broker {}", brokerUrl, t);
                    }
                }
            }
        } else {
            // This is an HTTP/S server! Shall we have a / at the end of the URL? If
            // requested yes or if the directory does not have a ? and does not end with
            // .html/ or .txt/
            if (getSetup().getOptionalBoolean(HOST_HTTP_URLDIR).orElse(directory.indexOf("?") == -1
                    && !directory.endsWith(".html/") && !directory.endsWith(".htm/") && !directory.endsWith(".txt/"))) {
                // Make sure we have a / at the end of the URL
                if (!"".equals(directory) && !directory.endsWith("/")) {
                    directory += "/";
                }
            } else {
                // Make sure we don't have a / at then end of the URL
                while (directory.endsWith("/")) {
                    directory = directory.substring(0, directory.length() - 1);
                }
            }
            // At this stage the directory should have a correct format!
            _log.debug("List{}{}", !directory.isEmpty() ? " " + directory : "",
                    isNotEmpty(pattern) ? " (" + pattern + ")" : "");
            if (getSetup().getBoolean(HOST_HTTP_DODIR)) {
                // We have to do a listing of the files with a GET!
                final var maxSize = getSetup().getByteSize(HOST_HTTP_MAX_SIZE).size();
                final HttpUriRequestBase request = new HttpGet(
                        encodePath(getSetup().getBoolean(HOST_HTTP_ENCODE_URL), directory));
                ClassicHttpResponse getResponse = null;
                try {
                    getResponse = execute(targetHttpHost, request, 200);
                    final var entity = getResponse.getEntity();
                    if ((entity == null) || (entity.getContentLength() >= maxSize)) {
                        throw new IOException("Couldn't get list from: " + request.getRequestUri()
                                + (entity != null ? " (length is " + entity.getContentLength() + " bytes > "
                                        + Format.formatSize(maxSize) + ")" : ""));
                    }
                    final var select = getSetup().getString(HOST_HTTP_SELECT);
                    final String html;
                    try {
                        html = EntityUtils.toString(entity);
                    } catch (final ParseException e) {
                        throw new IOException(e.getMessage(), e.getCause());
                    }
                    final var doc = Jsoup.parse(html);
                    if (getDebug()) {
                        _log.debug("Content: {}", html);
                    }
                    if (select.isEmpty()) {
                        // We are just processing line by line (e.g. ftp view with file names only)
                        _log.debug("Parsing html and extracting {} tags", select);
                        BufferedReader br = null;
                        try {
                            final var text = getPlainText(doc);
                            br = new BufferedReader(new StringReader(text));
                            String line;
                            while ((line = br.readLine()) != null) {
                                final var filesCount = resultListSize + listSize;
                                if (filesCount >= getSetup().getInteger(HOST_HTTP_LIST_MAX_FILES)) {
                                    _log.debug("Processed maximum number of files: {}", filesCount);
                                    break;
                                } else {
                                    listSize++;
                                    addEntry(manager, resultList, rootDirectory, directory, line, level, pattern,
                                            counter, null, null, null);
                                }
                            }
                        } finally {
                            if (br != null) {
                                br.close();
                            }
                        }
                    } else {
                        // We are only extracting specified tags (e.g. a[href])
                        final var attribute = getSetup().getString(HOST_HTTP_ATTRIBUTE);
                        _log.debug("Parsing {} elements (using {})", select, attribute);
                        for (final Element element : doc.select(select)) {
                            final var filesCount = resultListSize + listSize;
                            if (filesCount >= getSetup().getInteger(HOST_HTTP_LIST_MAX_FILES)) {
                                _log.debug("Processed maximum number of files: {}", filesCount);
                                break;
                            }
                            final var line = getSetup().getString(HOST_HTTP_ALTERNATIVE_PATH)
                                    + (!attribute.isEmpty() ? element.attr(attribute) : element.text());
                            listSize++;
                            addEntry(manager, resultList, rootDirectory, directory, line, level, pattern, counter, null,
                                    null, null);
                        }
                    }
                } finally {
                    closeResponse(getResponse);
                }
            } else {
                // No listing required, the document we are interested in is the current URL!
                if (getDebug()) {
                    _log.debug("Adding directory: {}", directory);
                }
                addEntry(manager, resultList, rootDirectory, directory, directory, level, pattern, counter, null, null,
                        null);
            }
        }
        // Now we have the listing!
        _log.debug("{} line(s) selected", listSize);
    }

    /**
     * The Class ListThread. Listing meant to be used in multiple instances in parallel!
     */
    final class ListThread extends ExecutorRunnable {

        /** The manager. */
        final ExecutorManager<ListThread> manager;

        /** The result list. */
        final ProcessEntry resultList;

        /** The root directory. */
        final String rootDirectory;

        /** The directory. */
        final String currentDirectory;

        /** The name. */
        final String currentName;

        /** The level. */
        final int level;

        /** The pattern. */
        final String pattern;

        /** The alternativeName. */
        final String alternativeName;

        /** The size. */
        final ByteSize size;

        /** The date. */
        final Long date;

        /**
         * Instantiates a new list thread.
         *
         * @param manager
         *            the manager
         * @param resultList
         *            the result list
         * @param rootDirectory
         *            the root directory
         * @param currentDirectory
         *            the directory
         * @param currentName
         *            the name
         * @param level
         *            the level
         * @param pattern
         *            the pattern
         * @param alternativeName
         *            the alternativeName
         * @param size
         *            the size
         * @param date
         *            the date
         */
        ListThread(final ExecutorManager<ListThread> manager, final ProcessEntry resultList, final String rootDirectory,
                final String currentDirectory, final String currentName, final int level, final String pattern,
                final String alternativeName, final ByteSize size, final Long date) {
            super(manager);
            this.manager = manager;
            this.resultList = resultList;
            this.rootDirectory = rootDirectory;
            this.currentDirectory = currentDirectory;
            this.currentName = currentName;
            this.level = level;
            this.pattern = pattern;
            this.alternativeName = alternativeName;
            this.size = size;
            this.date = date;
        }

        /**
         * Process.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void process() throws IOException {
            final var lower = currentName.toLowerCase();
            final String path;
            final String filename;
            // Is it an absolute of a relative reference?
            if (getSetup().getBoolean(HOST_HTTP_MQTT_MODE) || lower.startsWith("http://")
                    || lower.startsWith("https://") || lower.startsWith("/")) {
                // This is an absolute path, so we don't need to keep the base directory!
                path = "";
            } else {
                // This is a relative path, so we keep the base directory!
                path = currentDirectory;
            }
            filename = currentName;
            if (getDebug()) {
                _log.debug("Listing path={}, name={}", path, currentName);
            }
            final FtpEntry entry;
            final var ownerUser = isNotEmpty(username) ? username : "nouser";
            final var ownerGroup = isNotEmpty(username) ? username : "nogroup";
            if (size != null && date != null) {
                // If the date and size is available, we already have the information required
                // to build the entry, no need to connect to retrieve the date and size.
                final var pr = new PrepareRequest(filename);
                final var symLink = isNotEmpty(alternativeName);
                entry = new FtpEntry(path, symLink ? "lrwxrwxrwx" : "-rw-r--r--",
                        getSetup().get(HOST_HTTP_FTPUSER, ownerUser), getSetup().get(HOST_HTTP_FTPGROUP, ownerGroup),
                        String.valueOf(size.size()), date,
                        (pr.isAlternativeHost() ? filename : pr.getPath()) + (symLink ? " -> " + alternativeName : ""),
                        null);
            } else {
                entry = getElement(path, filename, ownerUser, ownerGroup);
            }
            if (!entry.hasError() && !getSetup().getBoolean(HOST_HTTP_MQTT_MODE)
                    && getSetup().getBoolean(HOST_HTTP_DODIR)) {
                if (getSetup().getBoolean(HOST_HTTP_LIST_RECURSIVE) && entry.permission.startsWith("d")
                        && !entry.name.startsWith("/")) {
                    // Let's parse this directory!
                    if (level <= getSetup().getInteger(HOST_HTTP_LIST_MAX_DIRS)) {
                        list(manager, resultList, rootDirectory, currentDirectory + "/" + entry.name, pattern,
                                level + 1);
                    } else {
                        _log.warn("Discarding {} (max-directory): {}", entry.fullName, level);
                    }
                } else // This is a new entry!
                if (!rootDirectory.startsWith(entry.fullName)) {
                    processEntry(entry);
                } else {
                    if (getDebug()) {
                        _log.debug("Discarding {} (root-directory)", entry.fullName);
                    }
                }
            } else {
                processEntry(entry);
            }
        }

        /**
         * Process entry.
         *
         * @param entry
         *            the entry
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void processEntry(final FtpEntry entry) throws IOException {
            final var element = decodePath(getSetup().getBoolean(HOST_HTTP_ENCODE_URL),
                    entry.getFtpList(rootDirectory));
            if (isEmpty(pattern) || entry.name.matches(pattern)) {
                _log.debug("Adding ftp entry: {} => {}", currentName, element);
                final var hasError = entry.hasError();
                resultList.add((hasError ? "err:" : "") + element
                        + (hasError ? " (exception: " + entry.getError() + ")" : ""));
            } else if (getDebug()) {
                _log.debug("Discarding {} (wrong-pattern) - {} != {}", entry.fullName, entry.name, pattern);
            }
        }

    }

    /**
     * Execute the http request.
     *
     * @param targetHost
     *            the target host
     * @param httpRequest
     *            the http request
     * @param acceptedStatusCodes
     *            the accepted status codes
     *
     * @return the http entity
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private ClassicHttpResponse execute(final HttpHost targetHost, final HttpUriRequestBase httpRequest,
            final Integer... acceptedStatusCodes) throws IOException {
        try {
            httpRequest.setHeader("Host", targetHost.getHostName());
            for (final String key : headersList.keySet().toArray(new String[0])) {
                final var value = headersList.get(key);
                httpRequest.setHeader(key, value);
            }
            if (getDebug()) {
                _log.debug("Processing URI {}", httpRequest.getRequestUri());
                _log.debug("Method: {}", httpRequest.getMethod());
                _log.debug("Path: {}", httpRequest.getPath());
                final var entity = httpRequest.getEntity();
                if (entity != null) {
                    _log.debug("Content Type: {}", entity.getContentType());
                    _log.debug("Content Encoding: {}", entity.getContentEncoding());
                    _log.debug("Content Length: {}", entity.getContentLength());
                }
                for (final Header header : httpRequest.getHeaders()) {
                    _log.debug("Request Header: {}={}", header.getName(), header.getValue());
                }
            }
            final var context = HttpClientContext.create();
            if (authCache != null) { // Is it required?
                context.setAuthCache(authCache);
            }
            final ClassicHttpResponse httpResponse = httpClient.execute(targetHost, httpRequest, context);
            final var statusCode = httpResponse.getCode();
            final var statusMessage = statusCode + " " + httpResponse.getReasonPhrase() + " "
                    + httpResponse.getVersion().getProtocol();
            if (getDebug()) {
                _log.debug("Request status: {}", statusMessage);
                for (final Header header : httpResponse.getHeaders()) {
                    _log.debug("Response Header: {}={}", header.getName(), header.getValue());
                }
            }
            if (Arrays.stream(acceptedStatusCodes).noneMatch(acceptedStatusCode -> acceptedStatusCode == statusCode)) {
                throw new IOException("Error " + statusMessage);
            }
            return httpResponse;
        } catch (final Throwable t) {
            _log.warn("Processing {}", httpRequest.getRequestUri(), t);
            // Only retain the exception message as the MasterServer might not
            // have the Exception in its class path!
            throw new IOException(Format.getMessage(t));
        }
    }

    /**
     * Gets the setup. Utility call to get the ECtransSetup and check if the module is not closed!
     *
     * @return the setup
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private ECtransSetup getSetup() throws IOException {
        if (closed.get()) {
            throw new IOException("Module closed");
        }
        return currentSetup;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            _log.debug("Closing module");
            currentStatus = "CLOSE";
            if (manager != null) {
                manager.stopRun();
                manager = null;
            }
            StreamPlugThread.closeQuietly(httpInput);
            StreamPlugThread.closeQuietly(poolManager);
            StreamPlugThread.closeQuietly(httpClient);
            if (mqttSubscriber != null && mqttSubscriber.isConnected()) {
                try {
                    mqttSubscriber.disconnectForcibly();
                    _log.debug("MQTT subscriber disconnected forcibly");
                } catch (Throwable t) {
                    _log.debug("Closing MQTT subscriber", t);
                }
            }
            _log.debug("Close completed");
        } else {
            _log.debug("Already closed");
        }
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the status
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void setStatus(final String status) throws IOException {
        _log.debug("Status set to: {}", status);
        if (closed.get()) {
            throw new IOException("Module closed");
        }
        currentStatus = status;
    }

    /** The Constant _keepAliveStrategy. */
    private static final ConnectionKeepAliveStrategy _keepAliveStrategy = (httpResponse, context) -> {
        final var it = new BasicHeaderElementIterator(httpResponse.headerIterator("Keep-Alive"));
        while (it.hasNext()) {
            final var he = it.next();
            final var param = he.getName();
            final var value = he.getValue();
            if (value != null && "timeout".equalsIgnoreCase(param)) {
                return TimeValue.ofSeconds(Long.parseLong(value));
            }
        }
        return TimeValue.ofSeconds(5);
    };
}
