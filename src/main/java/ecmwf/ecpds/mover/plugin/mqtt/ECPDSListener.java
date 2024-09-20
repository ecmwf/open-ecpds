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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.events.client.ClientLifecycleEventListener;
import com.hivemq.extension.sdk.api.events.client.parameters.AuthenticationSuccessfulInput;
import com.hivemq.extension.sdk.api.events.client.parameters.ConnectionStartInput;
import com.hivemq.extension.sdk.api.events.client.parameters.DisconnectEventInput;
import com.hivemq.extension.sdk.api.packets.general.MqttVersion;

/**
 * The listener interface for receiving ECPDS events. The class that is interested in processing a ECPDS event
 * implements this interface, and the object created with that class is registered with a component using the
 * component's <code>addECPDSListener</code> method. When the ECPDS event occurs, that object's appropriate method is
 * invoked.
 *
 * @see ECPDSEvent
 */
public class ECPDSListener implements ClientLifecycleEventListener {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ECPDSListener.class);

    /**
     * On mqtt connection start.
     *
     * @param connectionStartInput
     *            the connection start input
     */
    @Override
    public void onMqttConnectionStart(final @NotNull ConnectionStartInput connectionStartInput) {
        final MqttVersion version = connectionStartInput.getConnectPacket().getMqttVersion();
        final String clientId = connectionStartInput.getClientInformation().getClientId();
        switch (version) {
        case V_5:
            log.debug("MQTT 5 client connected with id: {}", clientId);
            break;
        case V_3_1_1:
            log.debug("MQTT 3.1.1 client connected with id: {}", clientId);
            break;
        case V_3_1:
            log.debug("MQTT 3.1 client connected with id: {}", clientId);
            break;
        }
    }

    /**
     * On authentication successful.
     *
     * @param authenticationSuccessfulInput
     *            the authentication successful input
     */
    @Override
    public void onAuthenticationSuccessful(final @NotNull AuthenticationSuccessfulInput authenticationSuccessfulInput) {
        log.info("Client authenticated with id: {}",
                authenticationSuccessfulInput.getClientInformation().getClientId());
    }

    /**
     * On disconnect.
     *
     * @param disconnectEventInput
     *            the disconnect event input
     */
    @Override
    public void onDisconnect(final @NotNull DisconnectEventInput disconnectEventInput) {
        log.info("Client disconnected with id: {}", disconnectEventInput.getClientInformation().getClientId());
    }
}
