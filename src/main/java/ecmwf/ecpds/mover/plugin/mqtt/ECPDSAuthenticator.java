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

import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_MQTT_PERMISSION;

import java.io.Closeable;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hivemq.extension.sdk.api.auth.SimpleAuthenticator;
import com.hivemq.extension.sdk.api.auth.parameter.SimpleAuthInput;
import com.hivemq.extension.sdk.api.auth.parameter.SimpleAuthOutput;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import com.hivemq.extension.sdk.api.packets.auth.DefaultAuthorizationBehaviour;
import com.hivemq.extension.sdk.api.packets.connect.ConnackReasonCode;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.builder.Builders;

import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ecaccess.UserSession;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class ECPDSAuthenticator.
 */
public class ECPDSAuthenticator implements SimpleAuthenticator {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ECPDSAuthenticator.class);

    /** The Constant mover. */
    private static final MoverServer mover = StarterServer.getInstance(MoverServer.class);

    /** Registry mapping clientId to its active UserSession (for lifecycle tracking and admin close). */
    static final ConcurrentHashMap<String, UserSession> SESSION_REGISTRY = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * On connect.
     */
    @Override
    public void onConnect(final SimpleAuthInput simpleAuthInput, final SimpleAuthOutput simpleAuthOutput) {
        final ConnectPacket connectPacket = simpleAuthInput.getConnectPacket();
        final Optional<String> userNameOptional = connectPacket.getUserName();
        final Optional<ByteBuffer> passwordOptional = connectPacket.getPassword();
        final Optional<InetAddress> inetAddress = simpleAuthInput.getConnectionInformation().getInetAddress();
        // We want all fields
        if (userNameOptional.isEmpty() || passwordOptional.isEmpty() || inetAddress.isEmpty()) {
            simpleAuthOutput.failAuthentication(ConnackReasonCode.BAD_USER_NAME_OR_PASSWORD,
                    "Authentication failed because username or password are missing");
            return;
        }
        // Don't allow MQTT wildcard characters in the client id and username!
        final String clientId = simpleAuthInput.getClientInformation().getClientId();
        if (clientId.contains("#") || clientId.contains("+") || userNameOptional.get().contains("#")
                || userNameOptional.get().contains("+")) {
            simpleAuthOutput.failAuthentication(ConnackReasonCode.CLIENT_IDENTIFIER_NOT_VALID,
                    "Characters '#' and '+' are not allowed in the client identifier/username");
            return;
        }
        final var username = userNameOptional.get();
        final var password = new String(getBytesFromBuffer(passwordOptional.get()), StandardCharsets.UTF_8);
        final var remoteAddr = inetAddress.get().getHostAddress();
        final var from = "Using mqtts on DataMover=" + mover.getRoot() + " from " + username + "@" + remoteAddr;
        try {
            // Create a tracked session — this adds the connection to _dataSpaces so it
            // appears in the monitoring UI (/do/user/incoming/{user}) and can be closed by admins.
            final var session = NativeAuthenticationProvider.getInstance().getUserSession(remoteAddr, username,
                    password, "mqtts", (Closeable) () -> Services.clientService().disconnectClient(clientId));
            final var topicFilters = session.getECtransSetup().getStringList(USER_PORTAL_MQTT_PERMISSION);
            if (topicFilters.isEmpty()) {
                // No permissions configured — reject and discard the session immediately
                session.close(true);
                simpleAuthOutput.failAuthentication(ConnackReasonCode.NOT_AUTHORIZED,
                        "Authentication failed because of invalid credentials or permissions");
                return;
            }
            // Keep the session alive; onDisconnect will call close(true) to clean up
            SESSION_REGISTRY.put(clientId, session);
            // Build the permissions list
            simpleAuthOutput.getDefaultPermissions()
                    .addAll(topicFilters.stream().map(this::getTopicPermission).toList());
            simpleAuthOutput.getDefaultPermissions().setDefaultBehaviour(DefaultAuthorizationBehaviour.DENY);
            // Good to go!
            simpleAuthOutput.authenticateSuccessfully();
        } catch (final Exception e) {
            log.warn("{}: authentication failed", from, e);
            simpleAuthOutput.failAuthentication(ConnackReasonCode.NOT_AUTHORIZED,
                    "Authentication failed because of invalid credentials or permissions");
        }
    }

    /**
     * Gets the topic permission.
     *
     * @param topicFilter
     *            the topic filter
     *
     * @return the topic permission
     */
    private TopicPermission getTopicPermission(final String topicFilter) {
        return Builders.topicPermission().topicFilter(topicFilter).activity(TopicPermission.MqttActivity.SUBSCRIBE)
                .type(TopicPermission.PermissionType.ALLOW).retain(TopicPermission.Retain.ALL)
                .qos(TopicPermission.Qos.ALL).sharedSubscription(TopicPermission.SharedSubscription.ALL).build();
    }

    /**
     * Gets the bytes from buffer.
     *
     * @param byteBuffer
     *            the byte buffer
     *
     * @return the bytes from buffer
     */
    private static byte[] getBytesFromBuffer(final ByteBuffer byteBuffer) {
        final var bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return bytes;
    }
}
