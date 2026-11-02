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
package ecmwf.common.telemetry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;

/** ECPDS-specific signal overrides, parsed with the SDK's resource attribute syntax. */
public final class TelemetryResources {
    private TelemetryResources() {
    }

    public static Resource overrides(final ConfigProperties properties, final String signal) {
        final var attributes = Attributes.builder();
        properties.getMap("ecpds.otel." + signal + ".resource.attributes").forEach(attributes::put);
        return Resource.create(attributes.build());
    }
}
