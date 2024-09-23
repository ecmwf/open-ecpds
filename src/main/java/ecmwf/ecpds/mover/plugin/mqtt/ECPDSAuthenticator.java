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

import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_MQTT_PERMISSION;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.hivemq.extension.sdk.api.auth.SimpleAuthenticator;
import com.hivemq.extension.sdk.api.auth.parameter.SimpleAuthInput;
import com.hivemq.extension.sdk.api.auth.parameter.SimpleAuthOutput;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import com.hivemq.extension.sdk.api.packets.auth.DefaultAuthorizationBehaviour;
import com.hivemq.extension.sdk.api.packets.connect.ConnackReasonCode;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.services.builder.Builders;

import ecmwf.common.ecaccess.StarterServer;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class ECPDSAuthenticator.
 */
public class ECPDSAuthenticator implements SimpleAuthenticator {

    /** The Constant mover. */
    private static final MoverServer mover = StarterServer.getInstance(MoverServer.class);

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
        // Check the credentials and get the associated topic filters!
        final var topicFilters = validate(inetAddress.get(), userNameOptional.get(), passwordOptional.get());
        if (topicFilters.isEmpty()) { // No permissions on any topic
            simpleAuthOutput.failAuthentication(ConnackReasonCode.NOT_AUTHORIZED,
                    "Authentication failed because of invalid credentials or permissions");
            return;
        }
        // Build the permissions list
        simpleAuthOutput.getDefaultPermissions().addAll(topicFilters.stream().map(this::getTopicPermission).toList());
        simpleAuthOutput.getDefaultPermissions().setDefaultBehaviour(DefaultAuthorizationBehaviour.DENY);
        // Good to go!
        simpleAuthOutput.authenticateSuccessfully();
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
     * Validate.
     *
     * "+" matches a single MQTT topic level and can appear at any location in the topic pattern. e.g. foo/+/bar will
     * match foo/one/bar or foo/two/bar "#" matches multiple whole MQTT topic levels, but can only be used at the end of
     * the topic pattern. e.g. foo/# will match foo/bar and foo/bar/one and foo/bar/one/two.
     *
     * @param inetAddress
     *            the inet address
     * @param username
     *            the username
     * @param password
     *            the password
     *
     * @return the list
     */
    private static List<String> validate(final InetAddress inetAddress, final String username,
            final ByteBuffer password) {
        final var from = "Using MQTT on DataMover=" + mover.getRoot() + " from " + username + "@"
                + inetAddress.getHostAddress();
        try {
            final var profile = mover.getMasterProxy().getIncomingProfile(username,
                    new String(getBytesFromBuffer(password), StandardCharsets.UTF_8), from);
            // Now let's add all the permissions added through the user configuration
            return profile.getECtransSetup().getStringList(USER_PORTAL_MQTT_PERMISSION);
        } catch (final Exception e) {
            return List.of();
        }
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
