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

package ecmwf.ecpds.extension.ecaccess;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOption;
import ecmwf.common.ectrans.ECtransOptionDefinition;
import ecmwf.common.ectrans.ECtransOptionProvider;

/**
 * ECaccess gateway options and their editor metadata, owned by the optional extension.
 */
public final class EcaccessOptions implements ECtransOptionProvider {
    private static final Properties METADATA = metadata();

    public static final ECtransOption<String> HOST_ECACCESS_LASTUSED = option("lastused", String.class, null,
            List.of());
    public static final ECtransOption<Boolean> HOST_ECACCESS_LOADBALANCING = option("loadbalancing", Boolean.class,
            false, List.of());
    public static final ECtransOption<String> HOST_ECACCESS_GATEWAY = option("gateway", String.class, "mover",
            List.of());
    public static final ECtransOption<String> HOST_ECACCESS_DESTINATION = option("destination", String.class,
            "genericFtp", List.of("genericFtp", "genericSftp", "genericExec", "genericFile"));

    private static <T> ECtransOption<T> option(final String name, final Class<T> type, final T defaultValue,
            final List<T> choices) {
        return new ECtransOption<>(ECtransGroups.HOST, "ecaccess", name, type, Arrays.asList(defaultValue), choices,
                List.of(), false, METADATA);
    }

    private static Properties metadata() {
        final var properties = new Properties();
        try (final var input = EcaccessOptions.class.getResourceAsStream("ecaccess-options.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing ECaccess option metadata");
            }
            properties.load(input);
        } catch (final IOException e) {
            throw new UncheckedIOException("Cannot load ECaccess option metadata", e);
        }
        return properties;
    }

    @Override
    public List<ECtransOptionDefinition> options() {
        return List.of(HOST_ECACCESS_LASTUSED, HOST_ECACCESS_LOADBALANCING, HOST_ECACCESS_GATEWAY,
                HOST_ECACCESS_DESTINATION);
    }
}
