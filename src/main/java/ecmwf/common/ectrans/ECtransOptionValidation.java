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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.text.Format;

/** Declarative, non-executing dependency checks for the Properties editors and their server-side badges. */
public final class ECtransOptionValidation {
    private static final ObjectMapper JSON = new ObjectMapper();

    public enum Check {
        EQUALS, NON_BLANK, HTTPS_URL
    }

    public record Requirement(String when, String parameter, Check check, String value) {
        public Requirement {
            when = when == null ? "yes" : when;
            Objects.requireNonNull(parameter, "Required parameter");
            check = check == null ? Check.EQUALS : check;
            if (check == Check.EQUALS)
                Objects.requireNonNull(value, "Required value");
        }

        public String expected() {
            return switch (check) {
            case EQUALS -> "\"" + value + "\"";
            case NON_BLANK -> "a non-empty value";
            case HTTPS_URL -> "an HTTPS base URL ending in /, without credentials, query or fragment";
            };
        }

        private boolean accepts(final String actual, final ECtransOptionDefinition option) {
            if (actual == null)
                return false;
            return switch (check) {
            case EQUALS -> Objects.equals(normalize(option, actual), normalize(option, value));
            case NON_BLANK -> !actual.isBlank();
            case HTTPS_URL -> isHttpsUrl(actual);
            };
        }
    }

    public record Problem(String option, String parameter, String message, boolean deferred) {
    }

    private ECtransOptionValidation() {
    }

    static List<Requirement> parse(final String json) {
        if (json == null || json.isBlank())
            return List.of();
        try {
            return List.copyOf(JSON.readValue(json, new TypeReference<List<Requirement>>() {
            }));
        } catch (final IOException e) {
            throw new IllegalArgumentException("Invalid option requirements", e);
        }
    }

    public static ECtransOptionDefinition find(final ECtransGroups group, final String parameter) {
        return ECtransOptionRegistry.get(group).stream().filter(option -> option.getParameter().equals(parameter))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown required option: " + parameter));
    }

    /** Only dependency participants are exported, never unrelated global configuration or secured values. */
    public static String defaultValue(final ECtransOptionDefinition option) {
        return option.isSecured() ? null : value(new ECtransSetup(option.getModule()), option);
    }

    private static String value(final ECtransSetup setup, final ECtransOptionDefinition option) {
        final var value = setup.getUnresolved(option);
        return value != null && value.isBlank() && option.getClazz() != String.class ? option.getDefaultString()
                : value;
    }

    private static String normalize(final ECtransOptionDefinition option, final String value) {
        if (value != null && option.getClazz() == Boolean.class) {
            if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value))
                return "yes";
            if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value))
                return "no";
        }
        return value;
    }

    private static boolean isHttpsUrl(final String value) {
        try {
            final var uri = URI.create(value);
            return "https".equals(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null
                    && uri.getQuery() == null && uri.getFragment() == null && uri.getRawPath().endsWith("/")
                    && uri.getPort() <= 65535;
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean dynamic(final ECtransOptionDefinition option, final String script, final String value) {
        return ECtransSetup.isOptionDefined(option, script) || value != null && value.contains("$");
    }

    /**
     * Checks static properties and effective defaults without evaluating the Host script. Script/placeholder values are
     * reported as deferred diagnostics rather than configuration errors; runtime checks remain authoritative.
     */
    public static List<Problem> validate(final ECtransGroups group, final String data, final String module) {
        final var normalized = Format.windowsToUnix(data == null ? "" : data);
        final var setup = new ECtransSetup(module, normalized);
        final var separator = normalized.indexOf(ECtransSetup.SEPARATOR);
        final var script = separator < 0 ? "" : normalized.substring(separator + ECtransSetup.SEPARATOR.length());
        final var problems = new ArrayList<Problem>();
        for (final var source : ECtransOptionRegistry.get(group)) {
            final var requirements = source.getOption().getRequirements();
            if (requirements.isEmpty()
                    || module != null && !"ecaccess".equals(module) && !source.getModule().equals(module))
                continue;
            final var enabled = value(setup, source);
            if (dynamic(source, script, enabled)) {
                problems.add(
                        new Problem(source.getParameter(), source.getParameter(),
                                "Cannot verify requirements for " + source.getParameter()
                                        + ": its value is supplied by a script or placeholder. Checked at runtime.",
                                true));
                continue;
            }
            for (final var requirement : requirements) {
                if (!Objects.equals(normalize(source, enabled), normalize(source, requirement.when())))
                    continue;
                final var target = find(group, requirement.parameter());
                final var actual = value(setup, target);
                final var deferred = dynamic(target, script, actual);
                if (deferred || !requirement.accepts(actual, target))
                    problems.add(new Problem(
                            source.getParameter(), target.getParameter(), deferred
                                    ? "Cannot verify " + target.getParameter() + " required by " + source.getParameter()
                                            + ": its value is supplied by a script or placeholder. Checked at runtime."
                                    : source.getParameter() + "=\"" + requirement.when() + "\" requires "
                                            + target.getParameter() + " to be " + requirement.expected() + ".",
                            deferred));
            }
        }
        return List.copyOf(problems);
    }
}
