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

package ecmwf.ecpds.mover.plugin.mqtt;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hivemq.embedded.EmbeddedExtension;
import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;

import ecmwf.common.plugin.PluginThread;
import ecmwf.common.technical.Cnf;
import ecmwf.common.version.Version;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class MqttPlugin.
 */
public class MqttPlugin extends PluginThread {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MqttPlugin.class);

    /** The mover. */
    private static final MoverServer mover = getCaller(MoverServer.class);

    /** The Constant NAME. */
    private static final String NAME = "MqttPlugin";

    /** The Constant VERSION. */
    private static final String VERSION = Version.getFullVersion();

    /** The server. */
    private EmbeddedHiveMQ server;

    /**
     * Instantiates a new http plugin.
     *
     * @param ref
     *            the ref
     * @param params
     *            the params
     */
    public MqttPlugin(final String ref, final Map<String, String> params) {
        super(ref, params);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the plugin name.
     */
    @Override
    public String getPluginName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the version.
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * Start.
     */
    @Override
    public synchronized boolean start() {
        if (server != null) {
            return true;
        }
        final var thread = Thread.currentThread();
        final var loader = thread.getContextClassLoader();
        thread.setContextClassLoader(MqttPlugin.class.getClassLoader());
        try {
            final var mqttPort = Cnf.at("MqttPlugin", "mqtt", -1);
            final var mqttsPort = Cnf.at("MqttPlugin", "mqtts", -1);
            if (mqttsPort < 0 && mqttPort < 0) {
                throw new IOException("Invalid mqtt/s port specified: " + mqttPort + "/" + mqttsPort);
            }
            final var listenAddress = Cnf.at("MqttPlugin", "listenAddress", "0.0.0.0");
            final var sessionExpiryMaxInterval = Duration
                    .ofMillis(Cnf.durationAt("MqttPlugin", "sessionExpiryMaxInterval", Timer.ONE_HOUR)).toSeconds();
            final var messageExpiryMaxInterval = Duration
                    .ofMillis(Cnf.durationAt("MqttPlugin", "messageExpiryMaxInterval", Timer.ONE_WEEK)).toSeconds();
            _log.debug("SessionExpiryMaxInterval: {}s", sessionExpiryMaxInterval);
            _log.debug("MessageExpiryMaxInterval: {}s", messageExpiryMaxInterval);
            // Adding parameters used in the config.xml hivemq file!
            System.setProperty("listenAddress", listenAddress);
            System.setProperty("mqttPort", "" + mqttPort);
            System.setProperty("mqttsPort", "" + mqttsPort);
            System.setProperty("keyStore", Path.of(getConf("keyStore", "none")).toFile().getAbsolutePath());
            System.setProperty("keyStorePassword", getConf("keyStorePassword", "none"));
            System.setProperty("keyStorePasswordKey", getConf("keyStorePassword", "none"));
            System.setProperty("sessionExpiryMaxInterval", "" + sessionExpiryMaxInterval);
            System.setProperty("messageExpiryMaxInterval", "" + messageExpiryMaxInterval);
            // Create the embedded extension
            final EmbeddedExtension embeddedExtension = EmbeddedExtension.builder().withId("embedded-ecpds")
                    .withName("ECPDS Embedded Extension").withVersion(VERSION)
                    .withPriority(Cnf.at("MqttPlugin", "priority", 0))
                    .withStartPriority(Cnf.at("MqttPlugin", "startPriority", 1000))
                    .withAuthor("Laurent.Gougeon@ecmwf.com").withExtensionMain(new ECPDSMain()).build();
            // Create the server
            final var conf = Path.of(Cnf.at("MqttPlugin", "configurationFolder"));
            final var data = Path.of(Cnf.at("MqttPlugin", "dataFolder"));
            data.toFile().mkdirs(); // Make sure the path exists!
            final EmbeddedHiveMQBuilder embeddedHiveMQBuilder = EmbeddedHiveMQ.builder().withConfigurationFolder(conf)
                    .withDataFolder(data).withEmbeddedExtension(embeddedExtension);
            server = embeddedHiveMQBuilder.build();
            _log.info("Starting the mqtt/s server on {}:{}/{}", listenAddress, mqttPort, mqttsPort);
            final var started = new AtomicBoolean(true);
            server.start().thenAccept(result -> {
                _log.info("MQTT server started");
                mover.setMQTTInterface(new ECPDSPublisher());
            }).exceptionally(e -> {
                // Handle exceptions
                _log.error("MQTT server failed", e);
                started.set(false);
                return null;
            }).join();
            return started.get();
        } catch (final Exception e) {
            _log.error("Starting the plugin", e);
        } finally {
            thread.setContextClassLoader(loader);
        }
        return false;
    }

    /**
     * Gets the conf.
     *
     * @param keyName
     *            the key name
     * @param defaultValue
     *            the default value
     *
     * @return the conf
     */
    private static final String getConf(final String keyName, final String defaultValue) {
        return Cnf.at("Security", "SSL" + keyName.substring(0, 1).toUpperCase() + keyName.substring(1),
                System.getProperty("javax.net.ssl." + keyName, defaultValue));
    }

    /**
     * {@inheritDoc}
     *
     * Stop.
     */
    @Override
    public synchronized void stop() {
        if (server != null) {
            try {
                mover.setMQTTInterface(null);
                server.stop();
            } catch (final Exception e) {
                _log.warn(e);
            } finally {
                server = null;
            }
        }
    }
}
