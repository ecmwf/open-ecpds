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

package ecmwf.common.ectrans.notification;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Stateless, thread-safe interpretation of incoming MQTT messages. Providers do not own connections or scheduling.
 */
public interface NotificationProfile {
    int API_VERSION = 1;

    default int apiVersion() {
        return API_VERSION;
    }

    String name();

    /** Return null to ignore a message; throw IOException for an invalid notification. */
    Result decode(String topic, byte[] payload) throws IOException;

    private static URI parseHref(final String href) {
        try {
            return URI.create(Objects.requireNonNull(href, "Notification href"));
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid notification retrieval URL");
        }
    }

    record Result(String href, String filename, long size, String body, AcquisitionNotification notification) {
        public Result {
            final var uri = parseHref(href);
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null)
                throw new IllegalArgumentException("Notification link must be an absolute HTTP/HTTPS URL");
            if (filename == null || filename.isBlank() || filename.equals(".") || filename.equals("..")
                    || filename.contains("/") || filename.contains("\\") || filename.contains(" -> ")
                    || filename.codePoints().anyMatch(Character::isISOControl))
                throw new IllegalArgumentException("Invalid notification filename");
            if (size < -1)
                throw new IllegalArgumentException("Invalid notification size");
            Objects.requireNonNull(notification, "Notification metadata");
        }
    }
}
