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

package ecmwf.common.ectrans;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Immutable built-in and extension option catalogue. Duplicate definitions fail rather than override core options.
 */
public final class ECtransOptionRegistry {
    private ECtransOptionRegistry() {
    }

    private static final class Holder {
        private static final List<ECtransOptionDefinition> OPTIONS = load(ECtransOptionProvider.class.getClassLoader());
    }

    public static List<ECtransOptionDefinition> get(final ECtransGroups group) {
        return all().stream().filter(option -> option.getGroup() == group).toList();
    }

    public static List<ECtransOptionDefinition> all() {
        return Holder.OPTIONS;
    }

    public static List<ECtransOptionDefinition> load(final ClassLoader loader) {
        final var options = new LinkedHashMap<String, ECtransOptionDefinition>();
        Arrays.stream(ECtransOptions.values()).forEach(option -> add(options, option));
        for (final var provider : ServiceLoader.load(ECtransOptionProvider.class, loader)) {
            for (final var option : Objects.requireNonNull(provider.options(), "Provider options")) {
                add(options, option);
            }
        }
        return options.values().stream().sorted(Comparator.comparing(ECtransOptionDefinition::getParameter)
                .thenComparing(option -> option.getGroup().name())).toList();
    }

    private static void add(final LinkedHashMap<String, ECtransOptionDefinition> options,
            final ECtransOptionDefinition option) {
        Objects.requireNonNull(option, "Option");
        Objects.requireNonNull(option.getGroup(), "Option group");
        Objects.requireNonNull(option.getClazz(), "Option type");
        if (!option.getModule().matches("[a-z][a-z0-9]*") || !option.getName().matches("[A-Za-z][A-Za-z0-9]*")) {
            throw new IllegalArgumentException("Invalid option name: " + option.getParameter());
        }
        final var key = option.getGroup() + ":" + option.getParameter().toLowerCase(java.util.Locale.ROOT);
        if (options.putIfAbsent(key, option) != null) {
            throw new IllegalArgumentException("Duplicate option: " + key);
        }
    }
}
