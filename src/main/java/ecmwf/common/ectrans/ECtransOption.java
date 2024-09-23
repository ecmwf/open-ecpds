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

import java.time.Duration;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.TimeRange;

/**
 * The Class ECtransOption.
 *
 * @param <T>
 *            the generic type
 *
 * @author root
 */
public class ECtransOption<T> {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECtransOption.class);

    /** The Constant properties. */
    private static final Properties properties = new Properties();

    /** The group. */
    private final ECtransGroups group;

    /** The module. */
    private final String module;

    /** The name. */
    private final String name;

    /** The tips. */
    private final String tips;

    /** The clazz. */
    private final Class<T> clazz;

    /** The default values. */
    private final List<T> defaultValues;

    /** The choices. */
    private final List<T> choices;

    /** The visible. */
    private final boolean visible;

    /** The secured. */
    private final boolean secured;

    static {
        try (final var inputStream = ECtransOption.class.getResourceAsStream("ectrans-options.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                _log.warn("Resource ectrans-options.properties not found");
            }
        } catch (final Throwable e) {
            _log.warn("Cannot load ectrans-properties", e);
        }
    }

    /**
     * Instantiates a new ectrans option.
     *
     * @param group
     *            the group
     * @param module
     *            the module
     * @param name
     *            the name
     * @param clazz
     *            the clazz
     * @param defaultValues
     *            the default values
     * @param choices
     *            the choices
     */
    protected ECtransOption(final ECtransGroups group, final String module, final String name, final Class<T> clazz,
            final List<T> defaultValues, final List<T> choices) {
        this.group = group;
        this.module = module;
        this.name = name;
        this.clazz = clazz;
        this.defaultValues = defaultValues;
        this.choices = choices;
        this.tips = wrapWords(new StringBuilder(getComment(true)).append(". ")
                .append(get(getParameter(), "tips").orElse("").replace("'", "\"")).toString().trim(), 80);
        this.visible = Boolean.parseBoolean(get(getParameter(), "visible").orElse("true"));
        this.secured = Boolean.parseBoolean(get(getParameter(), "secured").orElse("false"));
    }

    /**
     * Gets the.
     *
     * @param parameter
     *            the parameter
     * @param entry
     *            the entry
     *
     * @return the optional
     */
    private static Optional<String> get(final String parameter, final String entry) {
        return Optional.ofNullable(properties.getProperty(parameter + "." + entry));
    }

    /**
     * Gets the parameter.
     *
     * @return the parameter
     */
    protected String getParameter() {
        return module + "." + name;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    protected ECtransGroups getGroup() {
        return group;
    }

    /**
     * Gets the module.
     *
     * @return the module
     */
    protected String getModule() {
        return module;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    protected String getName() {
        return name;
    }

    /**
     * Gets the tips.
     *
     * @return the tips
     */
    protected String getTips() {
        return tips;
    }

    /**
     * Gets the clazz.
     *
     * @return the clazz
     */
    protected Class<T> getClazz() {
        return clazz;
    }

    /**
     * Gets the comment.
     *
     * @param detailed
     *            the detailed
     *
     * @return the comment
     */
    protected String getComment(final boolean detailed) {
        final var sb = new StringBuilder(detailed ? "Requires " : "requires ");
        final String format;
        if (clazz.equals(Integer.class)) {
            sb.append("an integer");
            format = "";
        } else if (clazz.equals(ByteSize.class)) {
            sb.append("a number of bytes");
            format = "e.g. \"10MB\" or \"1024B\"";
        } else if (clazz.equals(TimeRange.class)) {
            sb.append("a time range");
            format = "ISO-8601 extended local time format e.g. \"10:15-11:25:30\"";
        } else if (clazz.equals(Duration.class)) {
            sb.append("a duration");
            format = "ISO-8601 seconds based representation e.g. \"PT20.345S\", \"PT15M\" or \"PT48H\"";
        } else if (clazz.equals(Period.class)) {
            sb.append("a period");
            format = "ISO-8601 calendar system e.g. \"P2Y\", \"P3M\", \"P4W\", \"P5D\", \"P1Y2M3D\", or \"P1Y2M3W4D\"";
        } else if (clazz.equals(Boolean.class)) {
            sb.append("a boolean");
            format = "true if \"yes\" or \"true\"";
        } else {
            sb.append("a " + clazz.getSimpleName().toLowerCase());
            format = "";
        }
        final var defaultValueString = toString(defaultValues);
        if (detailed) {
            if ("none".equals(defaultValueString)) {
                sb.append(", and there is no default");
            } else {
                sb.append(", and the default is ").append(defaultValueString);
            }
            if (!format.isBlank()) {
                sb.append(" (").append(format).append(")");
            }
            if (!choices.isEmpty()) {
                sb.append(". The acceptable values are: ")
                        .append(choices.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
            }
        } else {
            sb.append(" (default: ").append(defaultValueString).append(")");
        }
        return sb.toString();
    }

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    protected T getDefaultValue() {
        return defaultValues.isEmpty() ? null : defaultValues.get(0);
    }

    /**
     * Gets the default values.
     *
     * @return the default values
     */
    protected List<T> getDefaultValues() {
        return defaultValues;
    }

    /**
     * Gets the choices.
     *
     * @return the choices
     */
    protected List<T> getChoices() {
        return choices;
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    protected boolean isVisible() {
        return visible;
    }

    /**
     * Checks if is secured.
     *
     * @return true, if is secured
     */
    protected boolean isSecured() {
        return secured;
    }

    /**
     * To string.
     *
     * @param object
     *            the object
     *
     * @return the string
     */
    private static String toString(final Object object) {
        return toString(object, false);
    }

    /**
     * To string.
     *
     * @param object
     *            the object
     * @param doubleQuotes
     *            the double quotes
     *
     * @return the string
     */
    private static String toString(final Object object, final boolean doubleQuotes) {
        if (object instanceof Period || object instanceof Duration || object instanceof TimeRange) {
            return object.toString();
        }
        if (object instanceof final Boolean value) {
            return Boolean.TRUE.equals(value) ? "yes" : "no";
        } else if (object instanceof final ByteSize value) {
            return value == ByteSize.MAX_VALUE ? "max-size" : value.toString();
        } else if (object instanceof final Integer value) {
            return value == Integer.MAX_VALUE ? "max-integer" : value.toString();
        } else if (object instanceof final Double value) {
            return value == Double.MAX_VALUE ? "max-double" : value.toString();
        } else if (object instanceof final Long value) {
            return value == Long.MAX_VALUE ? "max-long" : value.toString();
        } else if (object instanceof final String value) {
            if (value.isEmpty()) {
                return "empty";
            } else {
                return doubleQuotes ? "\"" + value + "\"" : value;
            }
        } else {
            return "none";
        }
    }

    /**
     * To string.
     *
     * @param object
     *            the object
     *
     * @return the string
     */
    private String toString(final List<T> object) {
        if (object != null) {
            return switch (object.size()) {
            case 0 -> "none";
            case 1 -> toString(object.get(0), true);
            default -> "\"" + object.stream().map(ECtransOption::toString).collect(Collectors.joining(",")) + "\"";
            };
        }
        return "none";
    }

    /**
     * Wrap words.
     *
     * @param text
     *            the text
     * @param maxLineLength
     *            the max line length
     *
     * @return the string
     */
    private static String wrapWords(final String text, final int maxLineLength) {
        final var lines = new StringBuilder();
        final var currentLine = new StringBuilder();
        final var words = text.split("\\s+");
        for (final String word : words) {
            if (currentLine.length() + word.length() + 1 <= maxLineLength) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
            } else {
                lines.append(currentLine.toString() + "\\n");
                currentLine.setLength(0);
            }
            currentLine.append(word);
        }
        if (currentLine.length() > 0) {
            lines.append(currentLine.toString());
        }
        return lines.toString();
    }
}
