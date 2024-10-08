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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.text.Format;
import ecmwf.common.version.Version;

/**
 * The Class Cnf.
 */
public final class Cnf {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(Cnf.class);

    /** The Constant groupTable. */
    private static final Map<String, Map<String, String>> groupTable = new ConcurrentHashMap<>();

    /** The Constant properties. */
    private static final Properties properties = new Properties();

    /** The Constant reloads. */
    private static final List<String> reloads = new ArrayList<>();

    /** The managementThread. */
    private static final CnfManagementThread managementThread;

    /** The Constant FQCN. */
    private static final String FQCN = Cnf.class.getName() + ".";

    static {
        final var name = System.getProperty("ecmwf.properties");
        if (isNotEmpty(name)) {
            loadConfig(false, new File(name));
            if (!reloads.isEmpty()) {
                _log.debug("{} group(s) of parameter(s) to reload", reloads.size());
                for (final String group : reloads) {
                    _log.debug("Group {} tagged as reloaded", group);
                }
                managementThread = new CnfManagementThread(name);
                managementThread.setPriority(Thread.MIN_PRIORITY);
                managementThread.execute();
            } else {
                managementThread = null;
            }
            _log.debug("Configuration loaded (v{})", Version.getFullVersion());
        } else {
            _log.warn("Property 'ecmwf.properties' not defined (v{})", Version.getFullVersion());
            managementThread = null;
        }
    }

    /**
     * Utility classes should not have public constructors.
     */
    private Cnf() {
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return the long
     */
    public static long at(final String group, final String key, final long defaultValue) {
        try {
            final var at = at(group, key);
            return at == null ? defaultValue : Long.parseLong(removeDoubleQuote(at));
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Adds the.
     *
     * @param group
     *            the group
     */
    private static void add(final String group) {
        groupTable.put(group, new ConcurrentHashMap<>());
    }

    /**
     * Adds the.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param value
     *            the value
     */
    private static void add(final String group, final String key, final String value) {
        debug("Add " + group + " parameter: " + key + "=" + value);
        final Map<String, String> temp;
        if ((temp = at(group)) == null) {
            add(group);
            add(group, key, value);
            return;
        }
        temp.put(key, value);
    }

    /**
     * Checks for.
     *
     * @param group
     *            the group
     *
     * @return true, if successful
     */
    public static boolean has(final String group) {
        return groupTable.containsKey(group);
    }

    /**
     * At.
     *
     * @param group
     *            the group
     *
     * @return the hashtable
     */
    public static Map<String, String> at(final String group) {
        return groupTable.get(group);
    }

    /**
     * Values of.
     *
     * @param group
     *            the group
     *
     * @return the string
     */
    public static String valuesOf(final String group) {
        return valuesOf(at(group));
    }

    /**
     * Values of.
     *
     * @param values
     *            the values
     *
     * @return the string
     */
    public static String valuesOf(final Map<String, String> values) {
        return valuesOf(values, null);
    }

    /**
     * Values of.
     *
     * @param values
     *            the input values
     * @param processor
     *            the processor
     * @param privateFields
     *            the regex pattern of all private fields to process with the processor
     *
     * @return the result string
     */
    public static String valuesOf(final Map<String, String> values, final UnaryOperator<String> processor,
            final String... privateFields) {
        final List<String> toProcess = Arrays.asList(privateFields);
        final List<String> list = new ArrayList<>();
        if (values != null && !values.isEmpty()) {
            for (final Map.Entry<String, String> entry : values.entrySet()) {
                final var key = entry.getKey();
                final var value = toProcess.stream().anyMatch(str -> Pattern.matches(str, key))
                        ? processor.apply(entry.getValue()) : entry.getValue();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                    list.add(entry.getKey());
                } else {
                    list.add(entry.getKey() + " = \"" + value + "\"");
                }
            }
        }
        // Ignore the comments when sorting!
        final Comparator<String> stringComparator = Comparator.comparing(str -> str.replaceFirst("^#", ""));
        Collections.sort(list, stringComparator);
        final var data = new StringBuilder();
        for (final String element : list) {
            data.append(element).append("\n");
        }
        return data.toString();
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param defaultValue
     *            the default value
     *
     * @return the hashtable
     */
    public static Map<String, String> at(final String group, final Map<String, String> defaultValue) {
        final var result = at(group);
        return result == null ? defaultValue : result;
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     *
     * @return the string
     */
    public static String at(final String group, final String key) {
        final var temp = at(group);
        return temp != null ? temp.get(key) : null;
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return the string
     */
    public static String at(final String group, final String key, final String defaultValue) {
        final var value = at(group, key);
        return value == null ? defaultValue : value;
    }

    /**
     * File content at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultContent
     *            the default content
     *
     * @return the string
     */
    public static String fileContentAt(final String group, final String key, final String defaultContent) {
        final var fileName = at(group, key);
        if (isNotEmpty(fileName)) {
            try {
                return Files.readString(Path.of(fileName));
            } catch (final Exception e) {
                _log.warn("Could not read file {} (using default content)", fileName, e);
            }
        }
        return defaultContent;
    }

    /**
     * Values at.
     *
     * @param group
     *            the group
     *
     * @return the hashtable
     */
    public static List<String> keysAt(final String group) {
        final var temp = at(group);
        return temp != null ? temp.keySet().stream().toList() : new ArrayList<>();
    }

    /**
     * List at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     *
     * @return the vector
     */
    public static List<String> listAt(final String group, final String key) {
        final List<String> params = new ArrayList<>();
        final var values = at(group, key);
        if (values != null) {
            final var tokens = new StringTokenizer(values, ",");
            while (tokens.hasMoreElements()) {
                params.add(tokens.nextToken());
            }
        }
        return params;
    }

    /**
     * Get the list of periods of time.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     *
     * @return the vector
     */
    public static List<TimeRange> listOfTimeRangesAt(final String group, final String key) {
        final List<TimeRange> ranges = new ArrayList<>();
        for (final String timeRange : listAt(group, key)) {
            try {
                ranges.add(TimeRange.parse(timeRange));
            } catch (final DateTimeParseException e) {
                _log.warn("Could not parse time range {}", timeRange, e);
            }
        }
        return ranges;
    }

    /**
     * String list at. If not found return an empty string.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     *
     * @return the string[]
     */
    public static String[] stringListAt(final String group, final String key) {
        return listAt(group, key).toArray(new String[0]);
    }

    /**
     * Strings at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaults
     *            the defaults
     *
     * @return the string[]
     */
    public static String[] stringListAt(final String group, final String key, final String... defaults) {
        final var result = stringListAt(group, key);
        return result.length == 0 ? defaults : result;
    }

    /**
     * Adds the property.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private static void addProperty(final String name, final String value) {
        debug("Add property " + name + "=" + value);
        System.setProperty(name, value);
        properties.put(name, value);
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return true, if successful
     */
    public static boolean at(final String group, final String key, final boolean defaultValue) {
        return booleanAt(group, key, defaultValue);
    }

    /**
     * Boolean object at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return the boolean
     */
    public static Boolean booleanAt(final String group, final String key, final Boolean defaultValue) {
        try {
            final var at = at(group, key);
            if (at == null) {
                return defaultValue;
            }
            final var bool = removeDoubleQuote(at).toLowerCase();
            return "true".equals(bool) || "yes".equals(bool);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets the command.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param parameters
     *            the parameters
     *
     * @return the command
     */
    public static String[] getCommand(final String group, final String key, final String... parameters) {
        String command;
        if ((command = at(group, key)) == null) {
            return new String[0];
        }
        if (parameters != null) {
            for (var i = 1; i <= parameters.length; i++) {
                final var param = "$" + i;
                int ind;
                while ((ind = command.indexOf(param)) != -1) {
                    command = command.substring(0, ind) + parameters[i - 1] + command.substring(ind + param.length());
                }
            }
        }
        final var st = new StringTokenizer(command);
        final List<String> list = new ArrayList<>();
        while (st.hasMoreElements()) {
            list.add(st.nextToken());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Gets the added properties. Allow being compatible with JBoss. The signature should not be changed!
     *
     * @return the added properties
     */
    public static Properties getAddedProperties() {
        return new Properties(); // Empty properties as we don't want to send it across the network
    }

    /**
     * Gets the export. Allow being compatible with JBoss. The signature should not be changed!
     *
     * @return the export
     */
    public static Hashtable<String, Hashtable<String, String>> getExport() {
        return new Hashtable<>(); // Empty hashtable as we don't want to send it across the network
    }

    /**
     * Gets the system properties. Allow being compatible with JBoss. The signature should not be changed!
     *
     * @return the system properties
     */
    public static Properties getSystemProperties() {
        return new Properties(); // Empty properties as we don't want to send it across the network
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return the int
     */
    public static int at(final String group, final String key, final int defaultValue) {
        try {
            final var at = at(group, key);
            return at == null ? defaultValue : Integer.parseInt(removeDoubleQuote(at));
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Removes the double quote.
     *
     * @param value
     *            the value
     *
     * @return the string
     */
    private static String removeDoubleQuote(final String value) {
        final var result = value.trim();
        if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 1) {
            return result.substring(1, result.length() - 1);
        }
        return result;
    }

    /**
     * At.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return the short
     */
    public static short at(final String group, final String key, final short defaultValue) {
        try {
            final var at = at(group, key);
            return at == null ? defaultValue : Short.parseShort(removeDoubleQuote(at));
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get current date with the time specified.
     *
     * @param time
     *            the time
     * @param defaultValue
     *            the default value
     *
     * @return the date
     */
    public static Date getTime(final String time, final Date defaultValue) {
        try {
            if (time == null) {
                return defaultValue;
            }
            final var format0 = new SimpleDateFormat("yyyy-MM-dd ");
            final var format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final var pos = new ParsePosition(0);
            final var date = format1.parse(format0.format(new Date()) + removeDoubleQuote(time), pos);
            return date == null ? defaultValue : date;
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Time at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return the date
     */
    public static Date timeAt(final String group, final String key, final Date defaultValue) {
        return getTime(at(group, key), defaultValue);
    }

    /**
     * Duration at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return the long
     */
    public static long durationAt(final String group, final String key, final long defaultValue) {
        try {
            final var at = at(group, key);
            return at == null ? defaultValue : Format.getDuration(removeDoubleQuote(at));
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * _load config.
     *
     * @param reload
     *            the reload
     * @param file
     *            the file
     */
    private static void loadConfig(final boolean reload, final File file) {
        loadConfig(reload, file, null);
    }

    /**
     * _load config.
     *
     * @param reload
     *            the reload
     * @param file
     *            the file
     * @param groups
     *            the groups
     */
    private static void loadConfig(final boolean reload, final File file, final String groups) {
        _log.debug("{}oading configuration file {}", reload ? "Rel" : "L", file.getAbsolutePath());
        var exitOnException = System.getProperty(FQCN + "exitOnInitFailed", "false").matches("^(?i)(yes|true)$");
        var row = -1;
        final List<String> toExclude = new ArrayList<>();
        final List<String> toInclude = new ArrayList<>();
        if (groups != null) {
            final var tokens = new StringTokenizer(groups, ",");
            while (tokens.hasMoreElements()) {
                final var value = tokens.nextToken().trim();
                if (value.length() > 1 && value.startsWith("-")) {
                    toExclude.add(value.substring(1));
                } else if (value.length() > 1 && value.startsWith("+")) {
                    toInclude.add(value.substring(1));
                } else if (value.length() > 0) {
                    toInclude.add(value);
                }
            }
        }
        try {
            final var fis = new FileInputStream(file);
            final var dis = new BufferedReader(new InputStreamReader(fis));
            String group = null;
            int index;
            String line;
            row = 1;
            try {
                while ((line = dis.readLine()) != null) {
                    if (line.startsWith("#include \"") && line.length() > 11) {
                        var fileName = line.substring(10, line.lastIndexOf("\""));
                        String includes = null;
                        if ((index = fileName.indexOf("[")) > 0 && fileName.endsWith("]")) {
                            includes = fileName.substring(index + 1, fileName.length() - 1);
                            fileName = fileName.substring(0, index);
                        }
                        final var configFile = new File(getValue(fileName));
                        if (configFile.exists() && configFile.canRead()) {
                            loadConfig(reload, configFile, includes);
                        } else {
                            _log.warn("Ignoring import: {}", configFile.getAbsolutePath());

                        }
                    } else if (!reload && line.startsWith("#property ") && line.length() > 10) {
                        if ((index = line.indexOf('=')) != -1) {
                            addProperty(line.substring(10, index), getValue(line.substring(index + 1)));
                        }
                    } else if (!reload && line.startsWith("#reload \"") && line.length() > 10) {
                        reloads.add(line.substring(9, line.lastIndexOf("\"")));
                    } else if (line.length() != 0 && !line.startsWith("#")) {
                        if (line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') {
                            group = line.substring(1, line.length() - 1);
                            if (!(toExclude != null && !toExclude.isEmpty() && toExclude.contains(group)
                                    || toInclude != null && !toInclude.isEmpty() && !toInclude.contains(group))
                                    && (!reload || reload && reloads.contains(group))) {
                                if (reload) {
                                    debug("Reloading parameters group " + group);
                                    groupTable.remove(group);
                                }
                                if (!groupTable.containsKey(group)) {
                                    add(group);
                                }
                            } else {
                                group = null;
                            }
                        } else if (group != null && (index = line.indexOf('=')) != -1) {
                            add(group, line.substring(0, index), getValue(line.substring(index + 1)));
                        }
                    }
                    row++;
                }
            } finally {
                dis.close();
            }
            exitOnException = false;
        } catch (final FileNotFoundException e) {
            _log.warn("Could not load configuration (file {} not found)", file.getName());
        } catch (final Throwable t) {
            _log.warn("Could not load configuration{}", row != -1 ? " (error on line: " + row + ")" : "", t);
        } finally {
            if (exitOnException) {
                System.exit(0);
            }
        }
    }

    /**
     * Not empty string at.
     *
     * @param group
     *            the group
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     *
     * @return the string
     */
    public static String notEmptyStringAt(final String group, final String key, final String defaultValue) {
        final var at = at(group, key);
        if (at == null || at.length() == 0) {
            return defaultValue;
        }
        final var value = removeDoubleQuote(at);
        return isNotEmpty(value) ? value : defaultValue;
    }

    /**
     * _debug.
     *
     * @param message
     *            the message
     */
    private static void debug(final String message) {
        if (System.getProperty(FQCN + "debug", "false").matches("^(?i)(yes|true)$")) {
            _log.debug(message);
        }
    }

    /**
     * Gets the value.
     *
     * @param value
     *            the value
     *
     * @return the value
     */
    public static String getValue(final String value) {
        if (isNotEmpty(value)) {
            final int start;
            if ((start = value.indexOf("${")) != -1) {
                final var tag = value.substring(start + 1);
                int end;
                if ((end = tag.indexOf("}")) != -1) {
                    final var toResolv = getValue(tag.substring(0, end));
                    final String at;
                    final int index;
                    return value.substring(0, start)
                            + ((index = toResolv.indexOf("[")) == -1 || !toResolv.endsWith("]")
                                    ? System.getProperties().containsKey(toResolv.substring(1))
                                            ? System.getProperty(toResolv.substring(1))
                                            : toResolv.endsWith("()") && toResolv.length() > 2
                                                    ? callMethod(toResolv.substring(1, toResolv.length() - 2))
                                                    : "${" + toResolv.substring(1) + "}"
                                    : (at = at(toResolv.substring(1, index),
                                            toResolv.substring(index + 1, toResolv.length() - 1))) != null ? at : "")
                            + getValue(tag.substring(end + 1));
                }
            }
        }
        return value;
    }

    /**
     * Call method.
     *
     * @param name
     *            the name
     *
     * @return the string
     */
    private static String callMethod(final String name) {
        final var index = name.lastIndexOf(".");
        try {
            final Class<?> theClass = Class.forName(name.substring(0, index));
            return (String) theClass.getMethod(name.substring(index + 1))
                    .invoke(theClass.getDeclaredConstructor().newInstance());
        } catch (final Throwable e) {
            return name;
        }
    }

    /**
     * The Class CnfManagementThread.
     */
    static final class CnfManagementThread extends ConfigurableLoopRunnable {
        /** The _name. */
        private final String name;

        /** The _last modified. */
        private long lastModified;

        /**
         * Instantiates a new cnf management thread.
         *
         * @param name
         *            the name
         */
        CnfManagementThread(final String name) {
            setPause(30 * Timer.ONE_SECOND);
            lastModified = new File(name).lastModified();
            this.name = name;
        }

        /**
         * Configurable loop run.
         */
        @Override
        public void configurableLoopRun() {
            final var file = new File(name);
            final var timestamp = file.lastModified();
            if (timestamp != this.lastModified) {
                lastModified = timestamp;
                loadConfig(true, file);
            }
        }
    }
}
