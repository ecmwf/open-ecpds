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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.builder.Builders;
import com.hivemq.extension.sdk.api.services.builder.PublishBuilder;
import com.hivemq.extension.sdk.api.services.publish.PublishService;
import com.hivemq.extension.sdk.api.services.publish.RetainedMessageStore;
import com.hivemq.extension.sdk.api.services.session.ClientService;

import ecmwf.ecpds.mover.MQTTInterface;

/**
 * The Class ECPDSPublisher.
 */
public class ECPDSPublisher implements MQTTInterface {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ECPDSPublisher.class);

    /** The Constant publishService. */
    private static final PublishService publishService = Services.publishService();

    /** The Constant retainedMessageStore. */
    private static final RetainedMessageStore retainedMessageStore = Services.retainedMessageStore();

    /** The Constant clientService. */
    private static final ClientService clientService = Services.clientService();

    /**
     * Instantiates a new ECPDS publisher.
     */
    ECPDSPublisher() {
        // Hiding the implicit constructor
    }

    /**
     * Publish.
     *
     * @param topic
     *            the topic
     * @param qos
     *            the qos
     * @param expiryInterval
     *            the expiry interval
     * @param contentType
     *            the content type
     * @param clientId
     *            the client id
     * @param payload
     *            the payload
     * @param retain
     *            the retain
     */
    @Override
    public void publish(final String topic, final int qos, final long expiryInterval, final String contentType,
            final String clientId, final String payload, final boolean retain) {
        final Qos selectedQos = Qos.valueOf(qos >= 0 && qos <= 2 ? qos : 1);
        final PublishBuilder publish = Builders.publish().topic(topic).qos(selectedQos).retain(retain)
                .payload(ByteBuffer.wrap(payload.getBytes()));
        if (expiryInterval > 0) {
            publish.messageExpiryInterval(Duration.ofMillis(expiryInterval).toSeconds());
        }
        if (contentType != null && !contentType.isBlank()) {
            publish.contentType(contentType);
        }
        final var logMessage = getLogMessage(topic, selectedQos.name(), expiryInterval, contentType, payload, retain);
        if (clientId != null && !clientId.isBlank()) {
            publishService.publishToClient(publish.build(), clientId)
                    .whenComplete((aPublishToClientResult, throwable) -> {
                        if (throwable == null) {
                            log.debug("Publish sent successfully to {} ({})", clientId, logMessage);
                        } else {
                            log.warn("Publish not processed for {} ({})", clientId, logMessage, throwable);
                        }
                    });
        } else {
            publishService.publish(publish.build()).whenComplete((aVoid, throwable) -> {
                if (throwable == null) {
                    log.debug("Publish sent successfully ({})", logMessage);
                } else {
                    log.warn("Publish not processed ({})", logMessage, throwable);
                }
            });
        }
    }

    /**
     * Gets the log message.
     *
     * @param topic
     *            the topic
     * @param qos
     *            the qos
     * @param expiryInterval
     *            the expiry interval
     * @param contentType
     *            the content type
     * @param payload
     *            the payload
     * @param retain
     *            the retain
     *
     * @return the log message
     */
    private static String getLogMessage(final String topic, final String qos, final long expiryInterval,
            final String contentType, final String payload, final boolean retain) {
        return new StringBuilder("topic=").append(topic).append(",qos=").append(qos).append(",expiryInterval=")
                .append(expiryInterval).append(",contentType=").append(contentType).append(",payload:").append(payload)
                .append(",retain:").append(retain).toString();
    }

    /**
     * Removes the.
     *
     * @param topic
     *            the topic
     */
    @Override
    public void remove(final String topic) {
        retainedMessageStore.remove(topic).whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                log.debug("Failed to remove retained message for topic: {}", topic, throwable);
            } else {
                log.debug("Successfully removed retained message for topic: {}", topic);
            }
        });
    }

    /**
     * Clients count.
     *
     * @return the int
     */
    @Override
    public int clientsCount() {
        final var counter = new AtomicInteger();
        clientService.iterateAllClients((context, sessionInformation) -> {
            if (sessionInformation.isConnected()) {
                counter.incrementAndGet();
            }
        }).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                log.warn("", throwable);
            }
        });
        return counter.get();
    }
}
