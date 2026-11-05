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
package ecmwf.ecpds.master;

import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;

/** Optional, bounded content read before entering the MQTT script's execution deadline. */
final class MQTTInlineContent {
    private MQTTInlineContent() {
    }

    static String readBase64(final long size, final int requestedLimit, final Callable<InputStream> source) {
        final var limit = Math.min(requestedLimit, 16 * 1024);
        if (limit <= 0 || size < 0 || size > limit) {
            return null;
        }
        try (final var input = source.call()) {
            final var bytes = input.readNBytes(limit + 1);
            // Never embed truncated content, or trust a stale metadata size to bound a read.
            return bytes.length == size ? Base64.getEncoder().encodeToString(bytes) : null;
        } catch (final Exception e) {
            LogManager.getLogger(MQTTInlineContent.class)
                    .warn("Optional MQTT inline content unavailable; publishing without inline content", e);
            return null;
        }
    }
}
