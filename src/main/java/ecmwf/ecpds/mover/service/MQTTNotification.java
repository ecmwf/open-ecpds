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

package ecmwf.ecpds.mover.service;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * The Class RESTNotification. Allow notifying an MQTT broker that a file has
 * been transmitted successfully.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ectrans.NotificationInterface;
import ecmwf.common.technical.SessionCache;
import ecmwf.common.text.Options;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class MQTTNotification.
 */
public final class MQTTNotification implements NotificationInterface {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MQTTNotification.class);

    /** The Constant mover. */
    private static final MoverServer mover = StarterServer.getInstance(MoverServer.class);

    /** The Constant cache. */
    private static final MQTTCache cache = new MQTTCache();

    /** The current host. */
    private final StringBuilder currentHost = new StringBuilder();

    /** The previous host. */
    private final StringBuilder previousHost = new StringBuilder();

    /** The hosts list. */
    private final String[] hostsList;

    /** The name. */
    private final String name;

    /** The password. */
    private final String password;

    /** The key. */
    private final String key;

    /** The use ssl. */
    private final boolean useSsl;

    /** The client. */
    private MqttAsyncClient client;

    /**
     * Instantiates a new notification manager. The authentication is only performed once the file is transmitted (when
     * the notify method is called).
     *
     * @param useSsl
     *            the use ssl
     * @param hostsList
     *            the hosts (list of hosts, e.g. dm1:1883;dm2;dm3)
     * @param name
     *            the name
     * @param password
     *            the password
     */
    public MQTTNotification(final boolean useSsl, final String hostsList, final String name, final String password) {
        _log.debug("Initializing MQTT notification ({},{},{},{})", useSsl, hostsList, name, password);
        this.hostsList = hostsList.split("\\|");
        this.name = name;
        this.password = password;
        this.key = SessionCache.getKey(hostsList, -1, name, "useSsl=" + useSsl, "hash=" + password.hashCode());
        this.useSsl = useSsl;
    }

    /**
     * {@inheritDoc}
     *
     * Publish.
     */
    @Override
    public void notify(final String payload, final String topic, final String properties, final long lifetime)
            throws IOException, NumberFormatException {
        _log.debug("Publishing topic={} for payload={} with lifetime={} ({})", topic, payload, lifetime, properties);
        try {
            if ((client = cache.remove(key)) == null) {
                final var options = new MqttConnectionOptions();
                options.setAutomaticReconnect(true);
                options.setCleanStart(false);
                options.setUserName(name);
                options.setPassword(password.getBytes());
                options.setConnectionTimeout(10);
                client = new MqttAsyncClient((useSsl ? "ssl" : "tcp") + "://" + getInetSocketAddress(),
                        mover.getRoot() + "_" + UUID.randomUUID().toString(), new MemoryPersistence());
                client.connect(options).waitForCompletion();
            } else {
                _log.debug("Found cached mqtt connection ({})", key);
            }
            final var mqttProperties = new MqttProperties();
            final List<UserProperty> userProperties = new ArrayList<>();
            final var listOfProperties = new Options(properties, ",").getProperties();
            for (final Entry<Object, Object> property : listOfProperties.entrySet()) {
                userProperties
                        .add(new UserProperty(String.valueOf(property.getKey()), String.valueOf(property.getValue())));
            }
            mqttProperties.setUserProperties(userProperties);
            final var message = new MqttMessage(payload.getBytes());
            message.setProperties(mqttProperties);
            message.setQos(2);
            client.publish(topic, message).waitForCompletion();
        } catch (final Throwable e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        if (client != null) {
            if (key != null && client.isConnected()) {
                cache.put(key, client, 5 * Timer.ONE_MINUTE, -1);
            } else {
                try {
                    client.close();
                } catch (final MqttException e) {
                    throw new IOException(e.getMessage());
                }
            }
        }
    }

    /**
     * Get a random host from the list.
     *
     * @return the inet socket address
     *
     * @throws NumberFormatException
     *             the number format exception
     */
    private synchronized InetSocketAddress getInetSocketAddress() throws NumberFormatException {
        previousHost.setLength(0);
        previousHost.append(currentHost);
        currentHost.setLength(0);
        currentHost.append(hostsList[ThreadLocalRandom.current().nextInt(hostsList.length)]);
        final var host = currentHost.toString();
        final var index = host.lastIndexOf(":");
        if (index != -1) {
            // A port is defined
            return new InetSocketAddress(host.substring(0, index), Integer.parseInt(host.substring(index + 1)));
        }
        // Let's use the standard port
        return new InetSocketAddress(host, useSsl ? 8883 : 1883);
    }

    /**
     * The Class MQTTCache.
     */
    private static final class MQTTCache extends SessionCache<String, MqttAsyncClient> {

        /**
         * Disconnect.
         *
         * @param mqttClient
         *            the mqtt client
         */
        @Override
        public void disconnect(final MqttAsyncClient mqttClient) {
            try {
                mqttClient.close(true);
            } catch (final MqttException e) {
                // Ignored
            }
        }

        /**
         * Checks if is connected.
         *
         * @param mqttClient
         *            the mqtt client
         *
         * @return true, if is connected
         */
        @Override
        public boolean isConnected(final MqttAsyncClient mqttClient) {
            return mqttClient.isConnected();
        }

        /**
         * Update.
         *
         * @param mqttClient
         *            the mqtt client
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void update(final MqttAsyncClient mqttClient) throws IOException {
            // Nothing to do
        }
    }
}
