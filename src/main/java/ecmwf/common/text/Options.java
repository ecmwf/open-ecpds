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

package ecmwf.common.text;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class Options.
 */
public final class Options {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(Options.class);

    /** The content. */
    private final HashMap<String, String> content = new HashMap<>();

    /**
     * Instantiates a new Options with no parameters (empty).
     */
    public Options() {
        this(null, null);
    }

    /**
     * Instantiates a new Options. Parse options in the form:
     * filesize=">1234";fileage>=24h,dateformat=yyyyMMdd;datedelta="-1d". Default delimiters are ";", "," and new line.
     *
     * @param options
     *            the options
     */
    public Options(final String options) {
        this(options, null);
    }

    /**
     * Instantiates a new Options. Parse options in the form:
     * filesize=">1234";fileage>=24h,dateformat=yyyyMMdd;datedelta="-1d". Default delimiters are ";", "," and new line
     * if specified delimiters is empty or null.
     *
     * @param options
     *            the options
     * @param delimiters
     *            the delimiters
     */
    public Options(final String options, final String delimiters) {
        if (isNotEmpty(options)) {
            final var token = new StringTokenizer(options, isNotEmpty(delimiters) ? delimiters : ";,\n");
            while (token.hasMoreElements()) {
                addValue(token.nextToken());
            }
        }
        if (_log.isDebugEnabled() && get("debug", false)) {
            _log.debug("Options content: {}", content.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(";")));
        }
    }

    /**
     * Instantiates a new options. Called from ECtransSetup to use json objects.
     *
     * @param options
     *            the options
     */
    public Options(final Map<String, String> options) {
        if (options != null && !options.isEmpty()) {
            options.keySet().forEach(key -> {
                final var value = options.get(key);
                var operation = "=";
                for (final String op : new String[] { "=", ">", "<", "!=" }) {
                    if (value.startsWith(op)) {
                        operation = op;
                        break;
                    }
                }
                addValue(key + operation + value);
            });
        }
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return content.isEmpty();
    }

    /**
     * Sets the value.
     *
     * @param option
     *            the option
     */
    private void addValue(final String option) {
        // Don't process an empty option!
        if (!isNotEmpty(option)) {
            return;
        }
        StringIndexOutOfBoundsException t = null;
        try {
            // We have an option in the format name=value, name<=value, name>=value or
            // name!=value.
            for (String op : new String[] { "=", ">", "<", "!=" }) {
                var pos2 = option.indexOf(op);
                if (pos2 >= 0) {
                    // We might have an empty name!
                    final var name = option.substring(0, pos2);
                    if (op.length() == 1 && option.length() > pos2 + 1 && option.charAt(pos2 + 1) == '=') {
                        op += "=";
                        pos2++;
                    }
                    final var value = option.substring(pos2 + op.length());
                    // If '>', '<' or '=' then set '>>', '<<' or '==' to encode the operation on 2
                    // characters
                    if (op.length() == 1) {
                        op += op;
                    }
                    // If the value is between quotes then let's remove them!
                    content.put(name, op + (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1
                            ? value.substring(1, value.length() - 1) : value));
                    return;
                }
            }
        } catch (final StringIndexOutOfBoundsException e) {
            t = e;
        }
        // Parsing error?
        _log.warn("Error parsing option: {}", option, t);
    }

    /**
     * Inject (e.g. options.inject("$test", "myFileName")).
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void inject(final String name, final String value) {
        synchronized (content) {
            content.entrySet().forEach(entry -> {
                final var option = entry.getKey();
                final var metadata = entry.getValue();
                try {
                    content.put(option, Format.replaceAllExt(metadata, name, value));
                } catch (final Throwable t) {
                    _log.warn("Replacing {} in metadata '{}'", name, metadata, t);
                    content.put(option, metadata);
                }
            });
        }
    }

    /**
     * Inject provided options into current options. We have to order the list of keys by length otherwise a "time" and
     * "timestep" key might clash if not taken in the right order (e.g. if $ab=12 and $abcd=15 then the string
     * 'c=$abcd,$ab' would be translated into 'c=12cd,12' instead of 'c=15,12).
     *
     * @param options
     *            the options
     */
    public void inject(final Options options) {
        synchronized (options.content) {
            options.content.keySet().stream().sorted(Comparator.comparingInt(key -> -key.length()))
                    .forEach(key -> inject("$" + key, options.get(key, "")));
        }
    }

    /**
     * Replace parameters into provided target. We have to order the list of keys by length otherwise a "time" and
     * "timestep" key might clash if not taken in the right order (e.g. if $ab=12 and $abcd=15 then the string
     * 'c=$abcd,$ab' would be translated into 'c=12cd,12' instead of 'c=15,12).
     *
     * @param target
     *            the target
     *
     * @return the string builder
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public StringBuilder replace(final StringBuilder target) throws IOException {
        synchronized (content) {
            final var sortedList = content.keySet().stream().sorted(Comparator.comparingInt(key -> -key.length()))
                    .toList();
            for (final String key : sortedList) {
                Format.replaceAllExt(target, "$" + key, get(key, ""));
            }
            return target;
        }
    }

    /**
     * _compare.
     *
     * @param op
     *            the op
     * @param localValue
     *            the local value
     * @param remoteValue
     *            the remote value
     *
     * @return true, if successful
     */
    private boolean compare(final String op, final long localValue, final long remoteValue) {
        if ("==".equals(op)) {
            return remoteValue == localValue;
        }
        if (">=".equals(op)) {
            return remoteValue >= localValue;
        }
        if ("<=".equals(op)) {
            return remoteValue <= localValue;
        }
        if (">>".equals(op)) {
            return remoteValue > localValue;
        }
        if ("<<".equals(op)) {
            return remoteValue < localValue;
        }
        if ("!=".equals(op)) {
            return remoteValue != localValue;
        }
        // Operation not recognized
        return false;
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the string
     */
    public String get(final String option, final String defaultValue) {
        final var value = content.get(option);
        if (value != null && value.length() > 2) {
            // We found the option so let's remove the operator!
            return value.substring(2);
        }
        // Not found, so we return the default value!
        return defaultValue;
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the int
     */
    public int get(final String option, final int defaultValue) {
        final var value = get(option, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException t) {
            return defaultValue;
        }
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the long
     */
    public long get(final String option, final long defaultValue) {
        final var value = get(option, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException t) {
            return defaultValue;
        }
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return true, if successful
     */
    public boolean get(final String option, final boolean defaultValue) {
        final var value = get(option, null);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    /**
     * Gets the duration.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the duration
     */
    public Duration getDuration(final String option, final Duration defaultValue) {
        final var value = get(option, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Duration.ofMillis(Format.getDuration(value));
        } catch (final NumberFormatException t) {
            return defaultValue;
        }
    }

    /**
     * Gets the boolean.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the boolean
     */
    public Boolean getBoolean(final String option, final Boolean defaultValue) {
        final var value = get(option, null);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    /**
     * Gets the data as properties.
     *
     * @return the properties
     */
    public Properties getProperties() {
        final var properties = new Properties();
        synchronized (content) {
            for (final String key : content.keySet()) {
                properties.put(key, get(key, ""));
            }
        }
        return properties;
    }

    /**
     * Matches.
     *
     * @param option
     *            the option
     * @param value
     *            the value
     *
     * @return true, if successful
     */
    public boolean matches(final String option, final String value) {
        return matches(option, value, null);
    }

    /**
     * Matches.
     *
     * @param option
     *            the option
     * @param value
     *            the value
     * @param defaultRegex
     *            the default regex
     *
     * @return true, if successful
     */
    public boolean matches(final String option, final String value, final String defaultRegex) {
        final var regex = get(option, defaultRegex);
        try {
            return regex != null && value != null && value.matches(regex);
        } catch (final PatternSyntaxException e) {
            _log.warn("Pattern matching {} -> {}", value, regex, e);
            return false;
        }
    }

    /**
     * Check long.
     *
     * @param option
     *            the option
     * @param currentLong
     *            the current long
     *
     * @return true, if successful
     */
    public boolean checkLong(final String option, final long currentLong) {
        final var value = content.get(option);
        return value == null || value.length() < 3
                || compare(value.substring(0, 2), Long.parseLong(value.substring(2)), currentLong);
    }

    /**
     * Check date.
     *
     * @param option
     *            the option
     * @param currentDate
     *            the current date
     *
     * @return true, if successful
     */
    public boolean checkDate(final String option, final long currentDate) {
        final var value = content.get(option);
        return value == null || value.length() < 3
                || compare(value.substring(0, 2), Format.getDuration(value.substring(2)), currentDate);
    }
}
