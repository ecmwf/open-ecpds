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
public class ECtransOption<T> implements ECtransOptionDefinition {

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

    /** The sub options (nested key=value entries expected inside this option's value, if any). */
    private final List<SubOption> subOptions;

    /**
     * Whether the nested key=value entries are free-form (arbitrary keys, e.g. HTTP headers) rather than a fixed/known
     * set described by {@link #subOptions}.
     */
    private final boolean freeFormSubOptions;

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
    public ECtransOption(final ECtransGroups group, final String module, final String name, final Class<T> clazz,
            final List<T> defaultValues, final List<T> choices) {
        this(group, module, name, clazz, defaultValues, choices, List.of(), false);
    }

    /**
     * Instantiates a new ectrans option, additionally describing the nested key=value entries expected inside its value
     * (as parsed by {@link ecmwf.common.text.Options}), if any.
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
     * @param subOptions
     *            the known nested key=value entries, or an empty list if not applicable
     * @param freeFormSubOptions
     *            true if the value holds arbitrary/free-form nested key=value entries (e.g. HTTP headers) rather than a
     *            fixed/known set
     */
    public ECtransOption(final ECtransGroups group, final String module, final String name, final Class<T> clazz,
            final List<T> defaultValues, final List<T> choices, final List<SubOption> subOptions,
            final boolean freeFormSubOptions) {
        this(group, module, name, clazz, defaultValues, choices, subOptions, freeFormSubOptions, properties);
    }

    /**
     * Builds an option using extension-owned help, visibility and redaction metadata. Metadata keys use the same
     * parameter.tips/visible/secured format as built-in options.
     */
    public ECtransOption(final ECtransGroups group, final String module, final String name, final Class<T> clazz,
            final List<T> defaultValues, final List<T> choices, final List<SubOption> subOptions,
            final boolean freeFormSubOptions, final Properties metadata) {
        this.group = group;
        this.module = module;
        this.name = name;
        this.clazz = clazz;
        this.defaultValues = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(defaultValues));
        this.choices = List.copyOf(choices);
        this.subOptions = List.copyOf(subOptions);
        this.freeFormSubOptions = freeFormSubOptions;
        this.tips = wrapWords(new StringBuilder(getComment(true)).append(". ")
                .append(metadata.getProperty(getParameter() + ".tips", "").replace("'", "\"")).toString().trim(), 80);
        this.visible = Boolean.parseBoolean(metadata.getProperty(getParameter() + ".visible", "true"));
        this.secured = Boolean.parseBoolean(metadata.getProperty(getParameter() + ".secured", "false"));
    }

    @Override
    public ECtransOption<?> getOption() {
        return this;
    }

    /**
     * Gets the parameter.
     *
     * @return the parameter
     */
    public String getParameter() {
        return module + "." + name;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public ECtransGroups getGroup() {
        return group;
    }

    /**
     * Gets the module.
     *
     * @return the module
     */
    public String getModule() {
        return module;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
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
    public Class<T> getClazz() {
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
            format = "e.g. \"10MiB\", \"10MB\" or \"1024B\"; KB/MB/GB/TB/PB/EB and KiB/MiB/GiB/TiB/PiB/EiB use powers of 1024";
        } else if (clazz.equals(TimeRange.class)) {
            sb.append("a time range");
            format = "ISO-8601 extended local time format e.g. \"10:15-11:25:30\"";
        } else if (clazz.equals(Duration.class)) {
            sb.append("a duration");
            format = "ISO-8601 seconds based representation e.g. \"PT20.345S\", \"PT15M\" or \"PT48H\", "
                    + "or a simplified format e.g. \"48h\", \"30m\" or \"10d\"";
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
     * Gets the known nested key=value entries expected inside this option's value, if any.
     *
     * @return the sub options, or an empty list if not applicable
     */
    public List<SubOption> getSubOptions() {
        return subOptions;
    }

    /**
     * Checks if this option's value holds arbitrary/free-form nested key=value entries (e.g. HTTP headers) rather than
     * a fixed/known set described by {@link #getSubOptions()}.
     *
     * @return true, if the nested entries are free-form
     */
    public boolean hasFreeFormSubOptions() {
        return freeFormSubOptions;
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Checks if is secured.
     *
     * @return true, if is secured
     */
    public boolean isSecured() {
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

    @SuppressWarnings("unchecked")
    static <T> T convertDefault(final Class<T> clazz, final Object object) {
        if (object == null) {
            return null; // Null object so no conversion
        }
        if (clazz.isInstance(object)) {
            return clazz.cast(object); // No conversion required
        } else {
            // We are required to make a conversion as the classes don't match!
            final var objectClassName = object.getClass().getName();
            if (clazz.equals(Integer.class) && object instanceof final ByteSize value) {
                return (T) Integer.valueOf((int) value.size()); // ByteSize to Integer
            } else if (clazz.equals(Long.class) && object instanceof final ByteSize value) {
                return (T) Long.valueOf(value.size()); // ByteSize to Long
            } else if (clazz.equals(Double.class) && object instanceof final ByteSize value) {
                return (T) Double.valueOf(value.size()); // ByteSize to Double
            } else if (clazz.equals(Integer.class) && object instanceof final Long value) {
                return (T) Integer.valueOf(value.intValue()); // Long to Integer
            } else if (clazz.equals(Long.class) && object instanceof final Integer value) {
                return (T) Long.valueOf(value); // Integer to Long
            } else if (clazz.equals(Boolean.class) && object instanceof final String value) {
                return (T) Boolean.valueOf("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value));
            } else if (clazz.equals(Boolean.class) && object instanceof final Double value) {
                return (T) Boolean.valueOf(value.doubleValue() != 0); // Double Boolean
            } else if (clazz.equals(Boolean.class) && object instanceof final Long value) {
                return (T) Boolean.valueOf(value.longValue() != 0); // Long to Boolean
            } else if (clazz.equals(Boolean.class) && object instanceof final Integer value) {
                return (T) Boolean.valueOf(value.intValue() != 0); // Integer to Boolean
            } else if (clazz.equals(String.class) && object instanceof final Boolean value) {
                return (T) (Boolean.TRUE.equals(value) ? "yes" : "no"); // Boolean to String
            } else if (clazz.equals(ByteSize.class) && object instanceof final String value) {
                return (T) ("max-size".equals(value) ? ByteSize.MAX_VALUE : ByteSize.parse(value));
            } else if (clazz.equals(Double.class) && object instanceof final String value) {
                final Double maxValue = Double.MAX_VALUE;
                return (T) ("max-double".equals(value) ? maxValue : Double.valueOf(value));
            } else if (clazz.equals(Long.class) && object instanceof final String value) {
                final Long maxValue = Long.MAX_VALUE;
                return (T) ("max-long".equals(value) ? maxValue : Long.valueOf(value));
            } else if (clazz.equals(Integer.class) && object instanceof final String value) {
                final Integer maxValue = Integer.MAX_VALUE;
                return (T) ("max-integer".equals(value) ? maxValue : Integer.valueOf(value));
            } else if (clazz.equals(String.class) // All other objects available to String
                    && (object instanceof Period || object instanceof Duration || object instanceof TimeRange
                            || object instanceof ByteSize || object instanceof Integer || object instanceof Long
                            || object instanceof Double)) {
                return (T) object.toString();
            } else {
                throw new ClassCastException("Cannot convert " + clazz.getName() + " to " + objectClassName);
            }
        }
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
                lines.append(currentLine.toString() + "\n");
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
