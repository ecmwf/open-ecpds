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

package ecmwf.common.ectrans.notification.wis2;

import java.io.IOException;
import java.time.DateTimeException;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.ectrans.notification.AcquisitionNotification;
import ecmwf.common.ectrans.notification.AcquisitionNotification.Integrity;
import ecmwf.common.ectrans.notification.NotificationProfile;

/** Opt-in WIS2 data acquisition, not a Global Cache certification or a replacement MQTT client. */
public final class Wis2NotificationProfile implements NotificationProfile {
    private static final ObjectMapper JSON = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    @Override
    public String name() {
        return "wis2";
    }

    @Override
    public Result decode(final String topic, final byte[] payload) throws IOException {
        try {
            if (topic == null || !topic.matches("(origin|cache)/a/wis2/[^/]+/[^/]+/data/(core|recommended)/.+"))
                throw new IllegalArgumentException("Invalid WIS2 data topic");
            if (payload.length > 1024 * 1024)
                throw new IllegalArgumentException("WIS2 notification exceeds 1 MiB");
            final var wnm = JSON.readTree(payload);
            if (wnm == null || !wnm.isObject())
                throw new IllegalArgumentException("WIS2 notification must be an object");
            final var properties = wnm.path("properties");
            final var identity = text(properties, "data_id");
            final var links = wnm.path("links");
            if (!links.isArray())
                throw new IllegalArgumentException("Missing WIS2 links");
            JsonNode canonical = null;
            JsonNode update = null;
            for (final var link : links) {
                if ("update".equals(link.path("rel").asText()) && update == null)
                    update = link;
                else if ("canonical".equals(link.path("rel").asText()) && canonical == null)
                    canonical = link;
            }
            final var link = update == null ? canonical : update;
            if (link == null)
                throw new IllegalArgumentException("No WIS2 canonical or update link");
            var href = link.path("href");
            if (href.isArray())
                href = href.path(0);
            if (!href.isTextual())
                throw new IllegalArgumentException("Invalid WIS2 retrieval link");
            final var length = link.path("length");
            if (!length.isMissingNode()
                    && (!length.isIntegralNumber() || !length.canConvertToLong() || length.longValue() < 0))
                throw new IllegalArgumentException("Invalid WIS2 link length");
            var size = length.isMissingNode() ? -1 : length.longValue();
            final var expected = link.has("integrity") ? link.path("integrity") : properties.path("integrity");
            final var integrity = expected.isMissingNode() ? null
                    : new Integrity(text(expected, "method"), text(expected, "value"));
            final var source = properties.path("global-cache");
            if (!source.isMissingNode() && !source.isTextual())
                throw new IllegalArgumentException("Invalid WIS2 Global Cache identity");
            if (topic.startsWith("cache/") && source.isMissingNode())
                throw new IllegalArgumentException("Missing WIS2 Global Cache identity");
            final var notification = new AcquisitionNotification(name(), text(wnm, "id"), identity,
                    text(properties, "pubtime"), topic, source.isMissingNode() ? "origin" : source.textValue(),
                    text(link, "rel"), integrity);
            String body = null;
            final var content = properties.path("content");
            if (!content.isMissingNode()) {
                if (!"base64".equalsIgnoreCase(text(content, "encoding")))
                    throw new IllegalArgumentException("Unsupported WIS2 inline encoding");
                final var value = content.path("value");
                if (!value.isTextual())
                    throw new IllegalArgumentException("Missing or invalid WIS2 inline value");
                final var encoded = value.textValue();
                if (encoded.length() > 21848)
                    throw new IllegalArgumentException("WIS2 inline content exceeds 16 KiB");
                final var bytes = Base64.getDecoder().decode(encoded);
                if (bytes.length > 16384 || size != -1 && bytes.length != size)
                    throw new IllegalArgumentException("Invalid WIS2 inline content length");
                if (integrity != null)
                    integrity.verifyContent(bytes);
                size = bytes.length;
                body = Base64.getEncoder().encodeToString(bytes);
            }
            return new Result(href.textValue(), identity.substring(identity.lastIndexOf('/') + 1), size, body,
                    notification);
        } catch (final JsonProcessingException e) {
            throw new IOException("Invalid WIS2 JSON", e);
        } catch (final DateTimeException | ArithmeticException e) {
            throw new IOException("Invalid WIS2 publication time", e);
        } catch (final IllegalArgumentException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private static String text(final JsonNode object, final String key) {
        final var value = object.path(key);
        if (!value.isTextual() || value.textValue().isBlank())
            throw new IllegalArgumentException("Missing or invalid WIS2 " + key);
        return value.textValue();
    }
}
