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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public interface MQTTInterface {

    /**
     * Publish a notification to the registered MQTT broker.
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
    void publish(final String topic, final int qos, final long expiryInterval, final String contentType,
            final String clientId, final String payload, final boolean retain);

    /**
     * Remove a retained notification from the MQTT broker.
     *
     * @param topic
     *            the topic
     */
    void remove(final String topic);

    /**
     * Get the number of client connected to the MQTT broker.
     *
     * @return number clients
     */
    int clientsCount();
}
