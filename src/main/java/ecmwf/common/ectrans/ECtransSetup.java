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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_DEBUG;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransGroups.Module;
import ecmwf.common.technical.ByteSize;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.Pair;
import ecmwf.common.technical.ScriptManager;
import ecmwf.common.technical.TimeRange;
import ecmwf.common.text.Format;
import ecmwf.common.text.Options;

/**
 * The Class ECtransSetup.
 */
public final class ECtransSetup implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7032327024905125614L;

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECtransSetup.class);

    /** The Constant SEPARATOR. */
    public static final String SEPARATOR = "###### END-OF-PROPERTIES ######\n";

    /** The script content. */
    private final StringBuilder scriptContent = new StringBuilder();

    /** The current module name. */
    private String currentModuleName = null;

    /** The data content. */
    private final Map<String, String> dataContent = new ConcurrentHashMap<>();

    /** The parameter list. */
    private final List<Pair<?>> parameterList = Collections.synchronizedList(new ArrayList<>());

    /** The debug flag. */
    private boolean debug = false;

    /**
     * Instantiates a new ectrans setup.
     *
     * @param moduleName
     *            the module name
     */
    public ECtransSetup(final String moduleName) {
        this(moduleName, null);
    }

    /**
     * Instantiates a new ectrans setup.
     *
     * @param moduleName
     *            the module name
     * @param data
     *            the data
     * @param parameters
     *            the parameters
     */
    public ECtransSetup(final String moduleName, final String data, final Pair<?>... parameters) {
        if (parameters != null) {
            parameterList.addAll(Arrays.asList(parameters));
            // We have to order the list of keys by length otherwise a "time" and "timestep"
            // key might clash if not taken in the right order (e.g. if $ab=12 and $abcd=15
            // then the string 'c=$abcd,$ab' would be translated into 'c=12cd,12' instead of
            // 'c=15,12).
            Collections.sort(parameterList, (final Pair<?> p1, final Pair<?> p2) -> Integer
                    .compare(p2.getKey().length(), p1.getKey().length()));
        }
        currentModuleName = moduleName;
        setData(currentModuleName, data);
    }

    /**
     * Allow getting a new instance with the added parameters provided.
     *
     * @param parameters
     *            the parameters
     *
     * @return the ectrans setup
     */
    public ECtransSetup replace(final Pair<?>... parameters) {
        final List<Pair<?>> parametersList = new ArrayList<>(parameterList);
        if (parameters != null) {
            parametersList.addAll(Arrays.asList(parameters));
        }
        return new ECtransSetup(currentModuleName, getData(),
                parametersList.toArray(new Pair<?>[parametersList.size()]));
    }

    /**
     * Gets the value.
     *
     * @param value
     *            the value
     * @param nullable
     *            the nullable
     *
     * @return the string
     */
    private static String getValue(final String value, final boolean nullable) {
        if (value == null && nullable) {
            return null;
        }
        final var result = value == null ? "" : value;
        if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 1) {
            return result.substring(1, result.length() - 1);
        } else {
            return result;
        }
    }

    /**
     * Remove the white spaces in the property name in order to cope with something like: ectrans.debug = "yes" instead
     * of ectrans.debug="yes".
     *
     * @param sb
     *            the sb
     *
     * @return the string
     */
    private static StringBuilder removeSpacesInName(final StringBuilder sb) {
        var j = 0;
        int i;
        char c;
        for (i = 0; i < sb.length() && (c = sb.charAt(i)) != '"'; i++) {
            if (!Character.isWhitespace(c)) {
                sb.setCharAt(j++, sb.charAt(i));
            }
        }
        sb.delete(j, i);
        return sb;
    }

    /**
     * Extract the properties from the data string and the script if a separator is found. If a script is found then it
     * is stored.
     *
     * @param moduleName
     *            the module name
     * @param data
     *            the data
     */
    private void setData(final String moduleName, final String data) {
        this.currentModuleName = moduleName;
        dataContent.clear();
        scriptContent.setLength(0);
        if (isNotEmpty(data)) {
            final var sb = new StringBuilder(Format.windowsToUnix(data.trim()).concat("\n"));
            var indexStart = 0;
            var indexStop = 0;
            var index = -1;
            var foundSeparator = false;
            while (sb.length() > 0 && !(foundSeparator = sb.indexOf(SEPARATOR) == 0)
                    && (indexStart = removeSpacesInName(sb).indexOf("=\"")) != -1
                    && (indexStop = sb.substring(indexStart + 2).indexOf("\"\n")) != -1) {
                final var name = sb.substring(0, indexStart).trim();
                if ((index = name.indexOf(".")) != -1) {
                    set(name.substring(0, index), name.substring(index + 1),
                            sb.substring(indexStart + 2, indexStart + 2 + indexStop));
                }
                sb.delete(0, indexStart + 2 + indexStop + 2);
            }
            if (foundSeparator) {
                scriptContent.append(sb.substring(SEPARATOR.length()).trim());
            }
        }
        debug = getBoolean(HOST_ECTRANS_DEBUG);
    }

    /**
     * Sets the data.
     *
     * @param data
     *            the new data
     */
    public void setData(final String data) {
        setData(currentModuleName, data);
    }

    /**
     * Removes the.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     *
     * @return the string
     */
    private String remove(final String moduleName, final String name) {
        final var parameter = moduleName + "." + name;
        String value;
        if ((value = dataContent.remove(parameter)) == null) {
            value = getValue(Cnf.at("ECtrans", parameter), true);
            if (debug) {
                _log.debug("Value not found for {}{}", parameter, isNotEmpty(value) ? " (default: " + value + ")" : "");
            }
        } else if (debug) {
            _log.debug("Value found for {}: {}", parameter, value);
        }
        value = Cnf.getValue(value);
        if (debug) {
            _log.debug("Value resolved: {}", value);
        }
        return value;
    }

    /**
     * Removes the.
     *
     * @param name
     *            the name
     *
     * @return the string
     */
    public String remove(final String name) {
        return remove(currentModuleName, name);
    }

    /**
     * Removes the.
     *
     * @param option
     *            the option
     *
     * @return the string
     */
    public String remove(final ECtransOptions option) {
        return remove(option.getModule(), option.getName());
    }

    /**
     * Removes all parameters for the specified module name. This is used to clean a host.
     *
     * @param moduleName
     *            the module name
     */
    public void removeAll(final String moduleName) {
        final List<String> toRemove = new ArrayList<>();
        for (final String parameter : dataContent.keySet()) {
            if (parameter.startsWith(moduleName + ".")) {
                toRemove.add(parameter);
            }
        }
        for (final String key : toRemove) {
            dataContent.remove(key);
        }
    }

    /**
     * Removes all parameters for the specified module name. This is used to clean a host.
     *
     * @param module
     *            the module
     */
    public void removeAll(final Module module) {
        removeAll(module.getName());
    }

    /**
     * Replaces any references to parameters by their values.
     *
     * @param value
     *            the value
     *
     * @return the string
     */
    private String resolve(String value) {
        if (isNotEmpty(value)) { // Else nothing to do!
            // We have some parameters defined such as $destination or $timestamp. If the
            // parameter ends with a "[]" then we can expect some options like
            // $metadata[0..2]! Example of entries:
            // ["$destination[]","name"],["$timestamp",123456789000L]
            final var sb = new StringBuilder(Cnf.getValue(value));
            for (final Pair<?> parameter : parameterList) {
                final var key = parameter.getKey();
                final var content = String.valueOf(parameter.getValue());
                if (key.endsWith("[]")) {
                    try {
                        Format.replaceAllExt(sb, key.substring(0, key.length() - 2), content);
                    } catch (final IOException e) {
                        _log.warn("Replacing", e);
                    }
                } else {
                    Format.replaceAll(sb, key, content);
                }
            }
            value = sb.toString();
        }
        if (debug) {
            _log.debug("Value resolved: {}", value);
        }
        return value;
    }

    /**
     * Gets the.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the string
     */
    private String get(final String moduleName, final String name, final String defaultValue) {
        return get(moduleName, name, defaultValue, true);
    }

    /**
     * Gets the.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     * @param resolve
     *            resolve parameters with values in Cnf
     *
     * @return the string
     */
    private String get(final String moduleName, final String name, final String defaultValue, final boolean resolve) {
        final var value = getFromData(moduleName + "." + name, defaultValue);
        return resolve ? resolve(value) : value;
    }

    /**
     * Gets the value from the data *.
     *
     * @param parameter
     *            the parameter
     * @param defaultValue
     *            the default value
     *
     * @return the from data
     */
    private String getFromData(final String parameter, final String defaultValue) {
        final String value;
        if (!dataContent.containsKey(parameter)) {
            value = getValue(Cnf.at("ECtrans", parameter, defaultValue), true);
            if (debug) {
                _log.debug("Value not found for {}{}", parameter, isNotEmpty(value) ? " (default: " + value + ")" : "");
            }
        } else {
            value = dataContent.get(parameter);
            if (debug) {
                _log.debug("Value found for {} (data: {})", parameter, value);
            }
        }
        return value;
    }

    /**
     * Matches.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param defaultRegex
     *            the default regex
     *
     * @return true, if successful
     */
    public boolean matches(final String name, final String value, final String defaultRegex) {
        final var regex = get(name, defaultRegex);
        try {
            final boolean result = regex != null && value != null && value.matches(regex);
            if (debug) {
                _log.debug("Regex matching '{}' '{}': {}", value, regex, result);
            }
            return result;
        } catch (final PatternSyntaxException e) {
            _log.warn("Pattern matching {} -> {}", value, regex, e);
            return false;
        }
    }

    /**
     * Matches.
     *
     * @param options
     *            the options
     * @param value
     *            the value
     *
     * @return true, if successful
     */
    public boolean matches(final ECtransOptions options, final String value) {
        return matches(options, value, options.getDefaultString());
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
    public boolean matches(final ECtransOptions option, final String value, final String defaultRegex) {
        final var regex = get(option.getModule(), option.getName(), defaultRegex);
        try {
            final boolean result = regex != null && value != null && value.matches(regex);
            if (debug) {
                _log.debug("Regex matching '{}' '{}': {}", value, regex, result);
            }
            return result;
        } catch (final PatternSyntaxException e) {
            _log.warn("Pattern matching {} -> {}", value, regex, e);
            return false;
        }
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the string
     */
    public String get(final String name, final String defaultValue) {
        return get(currentModuleName, name, defaultValue);
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
    public String get(final ECtransOptions option, final String defaultValue) {
        return get(option.getModule(), option.getName(), defaultValue);
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     *
     * @return the duration
     */
    public Duration getDuration(final ECtransOptions option) {
        return getDuration(option.getModule(), option.getName(), option.getDefaultValue(Duration.class));
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the duration
     */
    public Duration get(final ECtransOptions option, final Duration defaultValue) {
        return getDuration(option.getModule(), option.getName(), defaultValue);
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     *
     * @return the period
     */
    public Period getPeriod(final ECtransOptions option) {
        return getPeriod(option.getModule(), option.getName(), option.getDefaultValue(Period.class));
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the period
     */
    public Period get(final ECtransOptions option, final Period defaultValue) {
        return getPeriod(option.getModule(), option.getName(), defaultValue);
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the byte size
     */
    public ByteSize get(final ECtransOptions option, final ByteSize defaultValue) {
        return getByteSize(option.getModule(), option.getName(), defaultValue);
    }

    /**
     * Gets the list.
     *
     * @param name
     *            the name
     * @param defaultValues
     *            the default values
     *
     * @return the list
     */
    public String[] getList(final String name, final String... defaultValues) {
        final String values;
        if ((values = get(name, null)) != null) {
            return values.split(",");
        }
        return defaultValues;
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     *
     * @return the options
     */
    public Options getOptions(final ECtransOptions option) {
        return getOptions(option, null);
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param delimiters
     *            the delimiters
     *
     * @return the options
     */
    public Options getOptions(final ECtransOptions option, final String delimiters) {
        return new Options(get(option.getModule(), option.getName(), null), delimiters);
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param key
     *            the key
     * @param delimiters
     *            the delimiters
     *
     * @return the options
     */
    public Options getOptions(final ECtransOptions option, final String key, final String delimiters) {
        final var options = get(option.getModule(), option.getName(), null);
        if (isNotEmpty(options)) {
            try {
                return new Options(Format.choose(key, options), delimiters);
            } catch (final IOException e) {
                _log.warn("Key '{}' not found in '{}'", key, options, e);
            }
        }
        // No valid options found!
        return new Options();
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param key
     *            the key
     * @param delimiters
     *            the delimiters
     *
     * @return the options
     */
    public Options getOptions(final String name, final String key, final String delimiters) {
        final var options = get(name, null);
        if (isNotEmpty(options)) {
            try {
                return new Options(Format.choose(key, options), delimiters);
            } catch (final IOException e) {
                _log.warn("Key '{}' not found in '{}'", key, options, e);
            }
        }
        // No valid options found!
        return new Options();
    }

    /**
     * Gets the.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the long
     */
    private long get(final String moduleName, final String name, final long defaultValue) {
        try {
            return Long.parseLong(get(moduleName, name, null));
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets duration.
     *
     * @param name
     *            the name
     *
     * @return the duration
     */
    public Duration getDuration(final String name) {
        return getDuration(currentModuleName, name, null);
    }

    /**
     * Gets duration.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the duration
     */
    public Duration getDuration(final String name, final Duration defaultValue) {
        return getDuration(currentModuleName, name, defaultValue);
    }

    /**
     * Gets the duration.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the duration
     */
    public Duration getDuration(final String moduleName, final String name, final Duration defaultValue) {
        final var value = get(moduleName, name, null);
        final var result = value == null || value.isBlank() ? defaultValue
                : Duration.ofMillis(Format.parseDurationWithDefaultInMillis(value));
        if (debug) {
            _log.debug("getDuration (parameter: {}.{}, value: {}, default: {}, result: {})", moduleName, name, value,
                    defaultValue, result);
        }
        return result;
    }

    /**
     * Gets the period.
     *
     * @param name
     *            the name
     *
     * @return the period
     */
    public Period getPeriod(final String name) {
        return getPeriod(currentModuleName, name, null);
    }

    /**
     * Gets the period.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the period
     */
    public Period getPeriod(final String name, final Period defaultValue) {
        return getPeriod(currentModuleName, name, defaultValue);
    }

    /**
     * Gets the period.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the period
     */
    public Period getPeriod(final String moduleName, final String name, final Period defaultValue) {
        final var value = get(moduleName, name, null);
        final var result = value == null || value.isBlank() ? defaultValue : Period.parse(value);
        if (debug) {
            _log.debug("getPeriod (parameter: {}.{}, value: {}, default: {}, result: {})", moduleName, name, value,
                    defaultValue, result);
        }
        return result;
    }

    /**
     * Gets byte size.
     *
     * @param name
     *            the name
     *
     * @return the long
     */
    public ByteSize getByteSize(final String name) {
        return getByteSize(currentModuleName, name, null);
    }

    /**
     * Gets byte size.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the long
     */
    public ByteSize getByteSize(final String name, final ByteSize defaultValue) {
        return getByteSize(currentModuleName, name, defaultValue);
    }

    /**
     * Gets byte size.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the long
     */
    public ByteSize getByteSize(final String moduleName, final String name, final ByteSize defaultValue) {
        final var value = get(moduleName, name, null);
        final var result = value == null || value.isBlank() ? defaultValue : ByteSize.parse(value);
        if (debug) {
            _log.debug("getByteSize (parameter: {}.{}, value: {}, default: {}, result: {})", moduleName, name, value,
                    defaultValue, result);
        }
        return result;
    }

    /**
     * Gets boolean.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the int
     */
    private int get(final String moduleName, final String name, final int defaultValue) {
        try {
            return Integer.parseInt(get(moduleName, name, null));
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets the list of booleans.
     *
     * @param option
     *            the option
     *
     * @return the list of booleans
     */
    public List<Boolean> getBooleanList(final ECtransOptions option) {
        return list(option, Boolean.class);
    }

    /**
     * Gets the boolean.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public boolean getBoolean(final ECtransOptions option) {
        return Boolean.TRUE.equals(get(option, Boolean.class));
    }

    /**
     * Gets the boolean.
     *
     * @param option
     *            the option
     * @param bindings
     *            the bindings
     *
     * @return the object of type class
     */
    public boolean getBoolean(final ECtransOptions option, final Map<String, Object> bindings) {
        return Boolean.TRUE.equals(get(option, Boolean.class, bindings));
    }

    /**
     * Gets the boolean object.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Boolean getBooleanObject(final ECtransOptions option) {
        return get(option, Boolean.class);
    }

    /**
     * Gets the boolean object.
     *
     * @param option
     *            the option
     * @param bindings
     *            the bindings
     *
     * @return the object of type class
     */
    public Boolean getBooleanObject(final ECtransOptions option, final Map<String, Object> bindings) {
        return get(option, Boolean.class, bindings);
    }

    /**
     * Gets the optional boolean.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<Boolean> getOptionalBoolean(final ECtransOptions option) {
        return getOptional(option, Boolean.class);
    }

    /**
     * Sets the optional boolean if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setBooleanIfPresent(final ECtransOptions option, final Consumer<? super Boolean> action) {
        getOptionalBoolean(option).ifPresent(action);
    }

    /**
     * Gets list of strings.
     *
     * @param option
     *            the option
     *
     * @return the list of strings
     */
    public List<String> getStringList(final ECtransOptions option) {
        return list(option, String.class);
    }

    /**
     * Gets the string.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public String getString(final ECtransOptions option) {
        return get(option, String.class);
    }

    /**
     * Gets the string.
     *
     * @param option
     *            the option
     * @param bindings
     *            the bindings
     *
     * @return the object of type class
     */
    public String getString(final ECtransOptions option, final Map<String, Object> bindings) {
        return get(option, String.class, bindings);
    }

    /**
     * Gets the optional string.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<String> getOptionalString(final ECtransOptions option) {
        return getOptional(option, String.class);
    }

    /**
     * Sets the optional string if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setStringIfPresent(final ECtransOptions option, final Consumer<? super String> action) {
        getOptionalString(option).ifPresent(action);
    }

    /**
     * Gets the lif of integers.
     *
     * @param option
     *            the option
     *
     * @return the list of integers
     */
    public List<Integer> getIntegerList(final ECtransOptions option) {
        return list(option, Integer.class);
    }

    /**
     * Gets the integer.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Integer getInteger(final ECtransOptions option) {
        return get(option, Integer.class);
    }

    /**
     * Gets the integer.
     *
     * @param option
     *            the option
     * @param bindings
     *            the bindings
     *
     * @return the object of type class
     */
    public Integer getInteger(final ECtransOptions option, final Map<String, Object> bindings) {
        return get(option, Integer.class, bindings);
    }

    /**
     * Gets the optional integer.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<Integer> getOptionalInteger(final ECtransOptions option) {
        return getOptional(option, Integer.class);
    }

    /**
     * Sets the optional integer if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setIntegerIfPresent(final ECtransOptions option, final Consumer<? super Integer> action) {
        getOptionalInteger(option).ifPresent(action);
    }

    /**
     * Gets the list of longs.
     *
     * @param option
     *            the option
     *
     * @return the list of longs
     */
    public List<Long> getLongList(final ECtransOptions option) {
        return list(option, Long.class);
    }

    /**
     * Gets the long.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Long getLong(final ECtransOptions option) {
        return get(option, Long.class);
    }

    /**
     * Gets the long.
     *
     * @param option
     *            the option
     * @param bindings
     *            the bindings
     *
     * @return the object of type class
     */
    public Long getLong(final ECtransOptions option, final Map<String, Object> bindings) {
        return get(option, Long.class, bindings);
    }

    /**
     * Gets the optional long.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<Long> getOptionalLong(final ECtransOptions option) {
        return getOptional(option, Long.class);
    }

    /**
     * Sets the optional long if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setLongIfPresent(final ECtransOptions option, final Consumer<? super Long> action) {
        getOptionalLong(option).ifPresent(action);
    }

    /**
     * Gets the list of doubles.
     *
     * @param option
     *            the option
     *
     * @return the list of doubles
     */
    public List<Double> getDoubleList(final ECtransOptions option) {
        return list(option, Double.class);
    }

    /**
     * Gets the double.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Double getDouble(final ECtransOptions option) {
        return get(option, Double.class);
    }

    /**
     * Gets the double.
     *
     * @param option
     *            the option
     * @param bindings
     *            the bindings
     *
     * @return the object of type class
     */
    public Double getDouble(final ECtransOptions option, final Map<String, Object> bindings) {
        return get(option, Double.class, bindings);
    }

    /**
     * Gets the optional double.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<Double> getOptionalDouble(final ECtransOptions option) {
        return getOptional(option, Double.class);
    }

    /**
     * Sets the optional double if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setDoubleIfPresent(final ECtransOptions option, final Consumer<? super Double> action) {
        getOptionalDouble(option).ifPresent(action);
    }

    /**
     * Gets the list of byte sizes.
     *
     * @param option
     *            the option
     *
     * @return the list of byte sizes
     */
    public List<ByteSize> getByteSizeList(final ECtransOptions option) {
        return list(option, ByteSize.class);
    }

    /**
     * Gets the byte size.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public ByteSize getByteSize(final ECtransOptions option) {
        return get(option, ByteSize.class);
    }

    /**
     * Gets the byte size.
     *
     * @param option
     *            the option
     * @param bindings
     *            the bindings
     *
     * @return the object of type class
     */
    public ByteSize getByteSize(final ECtransOptions option, final Map<String, Object> bindings) {
        return get(option, ByteSize.class, bindings);
    }

    /**
     * Gets the optional byte size.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<ByteSize> getOptionalByteSize(final ECtransOptions option) {
        return getOptional(option, ByteSize.class);
    }

    /**
     * Sets the optional byte size if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setByteSizeIfPresent(final ECtransOptions option, final Consumer<? super ByteSize> action) {
        getOptionalByteSize(option).ifPresent(action);
    }

    /**
     * Gets the optional duration.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<Duration> getOptionalDuration(final ECtransOptions option) {
        return getOptional(option, Duration.class);
    }

    /**
     * Sets the optional duration if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setDurationIfPresent(final ECtransOptions option, final Consumer<? super Duration> action) {
        getOptionalDuration(option).ifPresent(action);
    }

    /**
     * Gets the optional period.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<Period> getOptionalPeriod(final ECtransOptions option) {
        return getOptional(option, Period.class);
    }

    /**
     * Sets the optional period if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setPeriodIfPresent(final ECtransOptions option, final Consumer<? super Period> action) {
        getOptionalPeriod(option).ifPresent(action);
    }

    /**
     * Gets the list of time ranges.
     *
     * @param option
     *            the option
     *
     * @return the list of time ranges
     */
    public List<TimeRange> getTimeRangeList(final ECtransOptions option) {
        return list(option, TimeRange.class);
    }

    /**
     * Gets the time range.
     *
     * @param option
     *            the option
     *
     * @return the time range
     */
    public TimeRange getTimeRange(final ECtransOptions option) {
        return get(option, TimeRange.class);
    }

    /**
     * Gets the time range.
     *
     * @param option
     *            the option
     * @param bindings
     *            the bindings
     *
     * @return the time range
     */
    public TimeRange getTimeRange(final ECtransOptions option, final Map<String, Object> bindings) {
        return get(option, TimeRange.class, bindings);
    }

    /**
     * Gets the optional time range.
     *
     * @param option
     *            the option
     *
     * @return the object of type class
     */
    public Optional<TimeRange> getOptionalTimeRange(final ECtransOptions option) {
        return getOptional(option, TimeRange.class);
    }

    /**
     * Sets the optional time range if present.
     *
     * @param option
     *            the option
     * @param action
     *            the action
     */
    public void setTimeRangeIfPresent(final ECtransOptions option, final Consumer<? super TimeRange> action) {
        getOptionalTimeRange(option).ifPresent(action);
    }

    /**
     * Gets the option in the specified type. If the option is configured as a script, then invoke the function with the
     * given name if it exists. If the function does not exists or the script fails then switch back to the properties
     * to find the value.
     *
     * @param <T>
     *            the generic type
     * @param option
     *            the option
     * @param clazz
     *            the clazz
     *
     * @return the object of type class
     */
    private <T> T get(final ECtransOptions option, final Class<T> clazz) {
        return get(option, clazz, Map.of());
    }

    /**
     * Gets the option in the specified type. If the option is configured as a script, then invoke the function with the
     * given name if it exists. If the function does not exists or the script fails then switch back to the properties
     * to find the value. A set of arguments can also be provided, to be passed to the function.
     *
     * @param <T>
     *            the generic type
     * @param option
     *            the option
     * @param clazz
     *            the clazz
     * @param bindings
     *            the bindings
     *
     * @return the object of type class
     */
    private <T> T get(final ECtransOptions option, final Class<T> clazz, final Map<String, Object> bindings) {
        final var content = resolve(scriptContent.toString());
        if (!content.isBlank() && exists(option, content)) {
            try (final var manager = new ScriptManager(get("script", "limit", 0),
                    get("script", "language", ScriptManager.JS))) {
                manager.put(bindings);
                manager.eval(content);
                final var parameter = option.getModule() + "." + option.getName();
                if (manager.exists(parameter)) { // Checking the variable itself through the manager
                    return manager.get(clazz, parameter);
                }
            } catch (final ScriptException e) {
                _log.warn("Cannot execute script (will check properties)", e);
            }
        }
        // Switch back to the lookup in the properties
        return getFromProperties(option, clazz);
    }

    /**
     * Basic check to see if the option is declared in the script content. This is less costly than creating the script
     * manager directly, but does not guarantee it really exists. This will have to be checked at a later stage with the
     * script manager.
     *
     * @param option
     *            the option
     * @param content
     *            the content
     *
     * @return true, if successful
     */
    private static boolean exists(final ECtransOptions option, final String content) {
        return exists(option.getModule(), '=', content)
                && (exists(option.getName(), ':', content) || exists("\"" + option.getName() + "\"", ':', content));
    }

    /**
     * Exists.
     *
     * @param name
     *            the name
     * @param separator
     *            the separator
     * @param content
     *            the content
     *
     * @return true, if successful
     */
    public static boolean exists(final String name, final char separator, final String content) {
        return Pattern.compile("\\s*" + name + "\\s*" + separator + ".*").matcher(content).find();
    }

    /**
     * Gets the option in the specified type.
     *
     * @param <T>
     *            the generic type
     * @param option
     *            the option
     * @param clazz
     *            the clazz
     *
     * @return the object of type class
     */
    private <T> T getFromProperties(final ECtransOptions option, final Class<T> clazz) {
        final var found = get(option.getModule(), option.getName(), null);
        if (found == null || found.isBlank() && clazz != String.class) {
            // Nothing in the configuration or empty value, let's use the default!
            return option.getDefaultValue(clazz);
        } else {
            return cast(found, clazz);
        }
    }

    /**
     * Cast the string in the specified type.
     *
     * @param <T>
     *            the generic type
     * @param value
     *            the value
     * @param clazz
     *            the clazz
     *
     * @return the object of type class
     */
    @SuppressWarnings({ "unchecked" })
    private static <T> T cast(final String value, final Class<T> clazz) {
        if (clazz == String.class) {
            return (T) value;
        } else if (clazz == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (clazz == Double.class) {
            return (T) Double.valueOf(value);
        } else if (clazz == Long.class) {
            return (T) Long.valueOf(value);
        } else if (clazz == Boolean.class) {
            return (T) Boolean.valueOf("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value));
        } else if (clazz == ByteSize.class) {
            return (T) ByteSize.parse(value);
        } else if (clazz == Duration.class) {
            return (T) Duration.ofMillis(Format.parseDurationWithDefaultInMillis(value));
        } else if (clazz == Period.class) {
            return (T) Period.parse(value);
        } else if (clazz == TimeRange.class) {
            return (T) TimeRange.parse(value);
        } else {
            // No matching conversion is found, return null
            _log.warn("Unsuported option type: {}", clazz);
            return null;
        }
    }

    /**
     * Gets the list of options in the specified type.
     *
     * @param <T>
     *            the generic type
     * @param option
     *            the option
     * @param clazz
     *            the clazz
     *
     * @return the list of object of type class
     */
    private <T> List<T> list(final ECtransOptions option, final Class<T> clazz) {
        final var found = get(option.getModule(), option.getName(), null);
        if (found == null || found.isBlank() && clazz != String.class) {
            // Nothing in the configuration or empty value, let's use the default!
            return option.getDefaultValues(clazz);
        } else {
            return castList(found, clazz);
        }
    }

    /**
     * Gets the list of options in the specified type.
     *
     * @param <T>
     *            the generic type
     * @param value
     *            the value
     * @param clazz
     *            the clazz
     *
     * @return the list of object of type class
     */
    @SuppressWarnings({ "unchecked" })
    private static <T> List<T> castList(final String value, final Class<T> clazz) {
        if (clazz == String.class) {
            return (List<T>) Arrays.asList(value.split(","));
        } else if (clazz == Integer.class) {
            return (List<T>) Arrays.stream(value.split(",")).map(Integer::valueOf).toList();
        } else if (clazz == Double.class) {
            return (List<T>) Arrays.stream(value.split(",")).map(Double::valueOf).toList();
        } else if (clazz == Long.class) {
            return (List<T>) Arrays.stream(value.split(",")).map(Long::valueOf).toList();
        } else if (clazz == Boolean.class) {
            return (List<T>) Arrays.stream(value.split(","))
                    .map(entry -> "true".equalsIgnoreCase(entry) || "yes".equalsIgnoreCase(entry)).toList();
        } else if (clazz == ByteSize.class) {
            return (List<T>) Arrays.stream(value.split(",")).map(ByteSize::parse).toList();
        } else if (clazz == Duration.class) {
            return (List<T>) Arrays.stream(value.split(","))
                    .map(entry -> Duration.ofMillis(Format.parseDurationWithDefaultInMillis(entry))).toList();
        } else if (clazz == Period.class) {
            return (List<T>) Arrays.stream(value.split(",")).map(Period::parse).toList();
        } else if (clazz == TimeRange.class) {
            return (List<T>) Arrays.stream(value.split(",")).map(TimeRange::parse).toList();
        } else {
            // No matching conversion is found!
            _log.warn("Unsuported option type: {}", clazz);
            return Collections.emptyList();
        }
    }

    /**
     * Gets the option in the optional specified type.
     *
     * @param <T>
     *            the generic type
     * @param option
     *            the option
     * @param clazz
     *            the clazz
     *
     * @return the object of type class
     */
    private <T> Optional<T> getOptional(final ECtransOptions option, final Class<T> clazz) {
        return Optional.ofNullable(get(option, clazz));
    }

    /**
     * Gets the.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return true, if successful
     */
    private boolean get(final String moduleName, final String name, final boolean defaultValue) {
        try {
            final var result = get(moduleName, name, null);
            return result == null || result.isBlank() ? defaultValue
                    : "true".equalsIgnoreCase(result) || "yes".equalsIgnoreCase(result);
        } catch (final Exception e) {
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
     * @return the true, if successful
     */
    public boolean get(final ECtransOptions option, final boolean defaultValue) {
        return get(option.getModule(), option.getName(), defaultValue);
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the result
     */
    public long get(final ECtransOptions option, final long defaultValue) {
        return get(option.getModule(), option.getName(), defaultValue);
    }

    /**
     * Gets the.
     *
     * @param option
     *            the option
     * @param defaultValue
     *            the default value
     *
     * @return the result
     */
    public int get(final ECtransOptions option, final int defaultValue) {
        return get(option.getModule(), option.getName(), defaultValue);
    }

    /**
     * Check if the parameter exists in the specified module. If the parameter is related to a function then the
     * function is not executed.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     *
     * @return true, if exists
     */
    private boolean exists(final String moduleName, final String name) {
        return dataContent.containsKey(moduleName + "." + name);
    }

    /**
     * Check if the parameter exists in the current module. If the parameter is related to a function then the function
     * is not executed.
     *
     * @param name
     *            the name
     *
     * @return true, if exists
     */
    public boolean exists(final String name) {
        return exists(currentModuleName, name);
    }

    /**
     * Sets the.
     *
     * @param option
     *            the option
     * @param value
     *            the value
     */
    public void set(final ECtransOptions option, final String value) {
        set(option.getModule(), option.getName(), value);
    }

    /**
     * Sets the.
     *
     * @param option
     *            the option
     * @param value
     *            the value
     */
    public void set(final ECtransOptions option, final ByteSize value) {
        set(option.getModule(), option.getName(), value.toString());
    }

    /**
     * Sets the.
     *
     * @param option
     *            the option
     * @param value
     *            the value
     */
    public void set(final ECtransOptions option, final boolean value) {
        set(option.getModule(), option.getName(), value ? "yes" : "no");
    }

    /**
     * Sets the.
     *
     * @param option
     *            the option
     * @param value
     *            the value
     */
    public void set(final ECtransOptions option, final long value) {
        set(option.getModule(), option.getName(), value);
    }

    /**
     * Sets the.
     *
     * @param option
     *            the option
     * @param value
     *            the value
     */
    public void set(final ECtransOptions option, final int value) {
        set(option.getModule(), option.getName(), value);
    }

    /**
     * Sets the.
     *
     * @param option
     *            the option
     * @param value
     *            the value
     */
    public void set(final ECtransOptions option, final double value) {
        set(option.getModule(), option.getName(), value);
    }

    /**
     * Sets the.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private void set(final String moduleName, final String name, final String value) {
        dataContent.put(moduleName.trim() + "." + name.trim(), getValue(value, false));
    }

    /**
     * Allow setting the aliases.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void set(final String name, final String value) {
        set(currentModuleName, name, value);
    }

    /**
     * Sets the long.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private void set(final String moduleName, final String name, final long value) {
        set(moduleName, name, value == Long.MAX_VALUE ? "max-long" : String.valueOf(value));
    }

    /**
     * Sets the int.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void set(final String name, final int value) {
        set(currentModuleName, name, value);
    }

    /**
     * Sets the int.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private void set(final String moduleName, final String name, final int value) {
        set(moduleName, name, value == Integer.MAX_VALUE ? "max-int" : String.valueOf(value));
    }

    /**
     * Sets the double.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void set(final String name, final double value) {
        set(currentModuleName, name, value);
    }

    /**
     * Sets the double.
     *
     * @param moduleName
     *            the module name
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private void set(final String moduleName, final String name, final double value) {
        set(moduleName, name, value == Double.MAX_VALUE ? "max-double" : String.valueOf(value));
    }

    /**
     * Normalize the representation of the data and remove option with same value as default value if required.
     *
     * @param option
     *            the option
     * @param removeIfSameAsDefault
     *            remove if same as default value
     */
    public void standardize(final ECtransOptions option, final boolean removeIfSameAsDefault) {
        if (!exists(option.getModule(), option.getName())) {
            return; // Nothing to do!
        }
        final var value = get(option.getModule(), option.getName(), option.getDefaultString(), false);
        if (value != null) {
            final Class<?> clazz = option.getClazz();
            try {
                final var result = Arrays.stream(value.split(",")).map(entry -> {
                    if (clazz == Duration.class) {
                        return Duration.ofMillis(Format.parseDurationWithDefaultInMillis(entry)).toString();
                    }
                    if (clazz == Period.class) {
                        return Period.parse(entry).toString();
                    } else if (clazz == Boolean.class) {
                        return "true".equalsIgnoreCase(entry) || "yes".equalsIgnoreCase(entry) ? "yes" : "no";
                    } else if (clazz == ByteSize.class) {
                        return ByteSize.parse(entry).toString();
                    } else {
                        return entry;
                    }
                }).collect(Collectors.joining(","));
                if (result != null) {
                    if (removeIfSameAsDefault && result.equals(option.getDefaultString())) {
                        // If this is the same as the default value then we can remove it!
                        remove(option);
                    } else { // Save it with the right format!
                        set(option.getModule(), option.getName(), result);
                    }
                }
            } catch (final Exception ignore) {
                // No conversion!
            }
        }
    }

    /**
     * Gets the sorted properties.
     *
     * @param secured
     *            if true then the private fields are hidden
     *
     * @return the script
     */
    public String getProperties(final boolean secured) {
        return secured
                ? Cnf.valuesOf(dataContent, str -> "********",
                        ECtransOptions.getAll(true).stream().map(ECtransOptions::getParameter).toArray(String[]::new))
                : Cnf.valuesOf(dataContent);
    }

    /**
     * Gets the script if the engine exists.
     *
     * @return the script
     */
    public String getScript() {
        return scriptContent.toString();
    }

    /**
     * Gets the data (sorted properties and script if engine exists).
     *
     * @return the data
     */
    public String getData() {
        return getProperties(false) + SEPARATOR + getScript();
    }

    /**
     * Evaluate the script. The result is expected to be of the class clazz.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the object class expected
     * @param script
     *            the name of the function
     *
     * @return the result of the function execution
     *
     * @throws javax.script.ScriptException
     *             the script exception
     */
    public <T> T eval(final Class<T> clazz, final String script) throws ScriptException {
        try (final var manager = new ScriptManager(get("script", "limit", 0),
                get("script", "language", ScriptManager.JS))) {
            manager.eval(scriptContent.toString());
            return manager.eval(clazz, script);
        }
    }

    /**
     * Evaluate the script. No return is expected from the function (void).
     *
     * @param script
     *            the name of the function
     *
     * @throws javax.script.ScriptException
     *             the script exception
     */
    public void eval(final String script) throws ScriptException {
        eval(null, script);
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the int
     */
    public int get(final String name, final int defaultValue) {
        return get(currentModuleName, name, defaultValue);
    }

    /**
     * Gets the module name.
     *
     * @return the module name
     */
    public String getModuleName() {
        return currentModuleName;
    }
}
