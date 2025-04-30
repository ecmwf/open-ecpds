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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.ScriptManager;

/**
 * The Class ScriptFile.
 */
public abstract class ScriptFile {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ScriptFile.class);

    /** The allMenus. */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> allMenus = new ConcurrentHashMap<>();

    /** The allFiles. */
    private final ConcurrentHashMap<String, File> allFiles = new ConcurrentHashMap<>();

    /**
     * _eval string.
     *
     * @param target
     *            the target
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return the string
     */
    private static final String evalString(String target, final String name, final String value) {
        int ind;
        while ((ind = target.indexOf(name)) != -1) {
            target = target.substring(0, ind) + value + target.substring(ind + name.length());
        }
        return target;
    }

    /**
     * Evaluate the query according to the user choices.
     *
     * @param sql
     *            the sql
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static final String evalQuery(final String sql) throws IOException {
        final var result = new StringBuilder();
        final var dis = new BufferedReader(new StringReader(sql));
        try (dis) {
            var line = dis.readLine();
            final var thenStr = new StringBuilder();
            final var elseStr = new StringBuilder();
            while (line != null) {
                if (!line.startsWith("#")) {
                    result.append(line).append("\n");
                    line = dis.readLine();
                } else if (line.startsWith("#if") && line.length() > 3) {
                    final var condition = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
                    final var javascript = line.indexOf("$(") != -1;
                    while ((line = dis.readLine()) != null && !line.startsWith("#fi") && !line.startsWith("#if")) {
                        if (line != null && !line.startsWith("#")) {
                            thenStr.append(line).append("\n");
                        } else if (line.startsWith("#else")) {
                            elseStr.append(line.substring(5)).append(" ");
                            while ((line = dis.readLine()) != null && !line.startsWith("#")) {
                                elseStr.append(line).append("\n");
                            }
                        }
                    }
                    // Test the condition!
                    if (javascript) {
                        if (ScriptManager.exec(Boolean.class, ScriptManager.JS, condition)) {
                            result.append(thenStr);
                        } else if (elseStr != null) {
                            result.append(elseStr);
                        }
                    } else if (condition.indexOf("==") != -1) {
                        final var tokEql = new StringTokenizer(condition, "==");
                        final var regex = tokEql.nextToken().trim();
                        final var input = tokEql.nextToken().trim();
                        if (Pattern.compile(regex).matcher(input).find()) {
                            result.append(thenStr);
                        } else if (elseStr != null) {
                            result.append(elseStr);
                        }
                    } else if (condition.indexOf("!=") != -1) {
                        final var tokDif = new StringTokenizer(condition, "!=");
                        final var regex = tokDif.nextToken().trim();
                        final var input = tokDif.nextToken().trim();
                        if (!Pattern.compile(regex).matcher(input).find()) {
                            result.append(thenStr);
                        } else if (elseStr != null) {
                            result.append(elseStr);
                        }
                    } else if (condition.indexOf(".=") != -1) {
                        final var tokDif = new StringTokenizer(condition, "!=");
                        final var left = tokDif.nextToken().trim();
                        final var right = tokDif.nextToken().trim();
                        if (left.startsWith(right)) {
                            result.append(thenStr);
                        } else if (elseStr != null) {
                            result.append(elseStr);
                        }
                    } else if (condition.indexOf("=.") != -1) {
                        final var tokDif = new StringTokenizer(condition, "!=");
                        final var left = tokDif.nextToken().trim();
                        final var right = tokDif.nextToken().trim();
                        if (left.endsWith(right)) {
                            result.append(thenStr);
                        } else if (elseStr != null) {
                            result.append(elseStr);
                        }
                    } else {
                        _log.warn("Ignoring condition: {}", condition);
                    }
                    thenStr.setLength(0);
                    elseStr.setLength(0);
                } else if (line.startsWith("#fi")) {
                    line = dis.readLine();
                }
            }
        } catch (ScriptException | PatternSyntaxException e) {
            _log.warn("Evaluating: {}", sql, e);
            throw new IOException(e.getMessage());
        }
        return result.toString();
    }

    /**
     * Gets the value.
     *
     * @param values
     *            the values
     * @param name
     *            the name
     * @param comment
     *            the comment
     * @param possibleValues
     *            the possible values
     * @param defaultValue
     *            the default value
     *
     * @return the string
     */
    private final String getValue(final Map<String, String> values, final String name, final String comment,
            final String[] possibleValues, final String defaultValue) {
        final var value = getValue(name, comment, possibleValues, defaultValue);
        if (value == null && values != null) {
            return values.get(name);
        }
        return value;
    }

    /**
     * Gets the script content.
     *
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     *
     * @return the string
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    private final String getScriptContent(final String menu, final String group, final String name)
            throws FileNotFoundException {
        final var menus = allMenus.get(menu);
        String content = null;
        if (menus != null) {
            final var groups = menus.get(group);
            if (groups != null) {
                content = groups.get(name);
            }
        }
        if (content == null) {
            throw new FileNotFoundException(menu + "->" + group + "->" + name);
        }
        return content;
    }

    /**
     * Gets the script content.
     *
     * @param file
     *            the file
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static final String getScriptContent(final File file) throws IOException {
        final var fis = new FileInputStream(file);
        final var dis = new BufferedReader(new InputStreamReader(fis));
        final var content = new StringBuilder();
        String line;
        try {
            while ((line = dis.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (final IOException e) {
            _log.warn(e);
        } finally {
            dis.close();
        }
        return content.toString();
    }

    /**
     * Execute request.
     *
     * @param file
     *            the file
     *
     * @return the object
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public Object executeRequest(final File file) throws SQLException, IOException {
        executeUpdate(getScriptContent(file));
        return null;
    }

    /**
     * _execute request.
     *
     * @param values
     *            the values
     * @param request
     *            the request
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private final String executeRequest(Map<String, String> values, final String request) throws IOException {
        final Map<String, String> params = new HashMap<>();
        final List<String> keys = new ArrayList<>();
        final Map<String, String> resolved = new HashMap<>();
        final Map<String, String[]> options = new HashMap<>();
        final Map<String, String> contents = new HashMap<>();
        final var sql = new StringBuilder();
        final var help = new StringBuilder();
        String confirm = null;
        String name = null;
        String menu = null;
        // Introduce default parameters
        if (values == null) {
            values = new HashMap<>();
        }
        final var now = System.currentTimeMillis();
        final var currentTimeMillis = values.get("currentTimeMillis");
        if (currentTimeMillis == null) {
            values.put("currentTimeMillis", String.valueOf(now));
        }
        final var dis = new BufferedReader(new StringReader(request));
        final var date = DataBase.formatDate(now);
        final var time = DataBase.formatTime(now);
        String line;
        try {
            while ((line = dis.readLine()) != null) {
                if (line.length() > 0) {
                    if (line.startsWith("# ") && line.length() > 2) {
                        help.append(line.substring(2)).append("\n");
                    } else {
                        if (line.startsWith("#menu \"") && line.length() > 8) {
                            menu = line.substring(7, line.lastIndexOf("\""));
                            continue;
                        }
                        if (line.startsWith("#name \"") && line.length() > 8) {
                            name = line.substring(7, line.lastIndexOf("\""));
                            continue;
                        }
                        if (line.startsWith("#confirm \"") && line.length() > 11) {
                            confirm = line.substring(10, line.lastIndexOf("\""));
                            continue;
                        }
                        if (line.startsWith("#prompt \"") && line.length() > 10) {
                            final var param = line.substring(9, line.lastIndexOf("\""));
                            final var token = new StringTokenizer(param, ";");
                            if (token.countTokens() >= 2) {
                                final var parameter = token.nextToken();
                                final var question = token.nextToken();
                                params.put(parameter, evalString(evalString(question, "\\n", "\n"), "\\t", "\t"));
                                keys.add(parameter);
                                if (token.hasMoreElements()) {
                                    var value = token.nextToken();
                                    if (value.indexOf("|") != -1) {
                                        final var st = new StringTokenizer(value, "|");
                                        final var opts = new String[st.countTokens()];
                                        for (var i = 0; i < opts.length; i++) {
                                            opts[i] = st.nextToken();
                                        }
                                        options.put(parameter, opts);
                                    } else {
                                        final var key = value.toLowerCase();
                                        if ("today".equals(key) || "date".equals(key) || "sysdate".equals(key)) {
                                            value = date;
                                        }
                                        if ("now".equals(key) || "time".equals(key) || "systime".equals(key)) {
                                            value = time;
                                        }
                                        contents.put(parameter, value);
                                    }
                                }
                            }
                            continue;
                        }
                        // if - else - fi
                        if (line.startsWith("#if") || line.startsWith("#fi")
                                || line.startsWith("#else") && line.length() > 3) {
                            sql.append(line).append("\n");
                        }
                        if (!line.startsWith("#")) {
                            sql.append(line).append("\n");
                        }
                    }
                }
            }
        } finally {
            dis.close();
        }
        if (sql.length() > 0) {
            // Parameters to prompt?
            for (final String parameter : keys) {
                if (sql.indexOf("$" + parameter) == -1) {
                    continue;
                }
                var question = params.get(parameter);
                for (final String key : resolved.keySet()) {
                    final String value;
                    if ((value = resolved.get(key)) != null) {
                        question = evalString(question, "$" + key, value);
                    }
                }
                final Object value;
                if ((value = options.containsKey(parameter)
                        ? getValue(values, parameter, question, options.get(parameter), options.get(parameter)[0])
                        : getValue(values, parameter, question, null, contents.get(parameter))) == null) {
                    return null;
                }
                resolved.put(parameter, value.toString());
                final var eval = evalString(sql.toString(), "$" + parameter, value.toString());
                sql.setLength(0);
                sql.append(eval);
            }
            // process sql - #if - #else - #fi
            final var eval = evalQuery(sql.toString());
            sql.setLength(0);
            sql.append(eval);
            showHelp(sql.toString(), help.toString());
        }
        if (help.isEmpty()) {
            if (confirm != null && !getConfirmation(menu + " -> " + name, confirm)) {
                return null;
            }
            return sql.toString();
        }
        return null;
    }

    /**
     * Execute query.
     *
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     * @param values
     *            the values
     *
     * @return the DB result set
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public DBResultSet executeQuery(final String menu, final String group, final String name,
            final Map<String, String> values) throws SQLException, IOException {
        return executeQuery(executeRequest(values, getScriptContent(menu, group, name)));
    }

    /**
     * Execute query.
     *
     * @param <T>
     *            the generic type
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     * @param resultClass
     *            the result class
     * @param values
     *            the values
     *
     * @return the DB iterator
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public <T extends DataBaseObject> DBIterator<T> executeQuery(final String menu, final String group,
            final String name, final Class<T> resultClass, final Map<String, String> values)
            throws SQLException, IOException {
        return executeQuery(resultClass, executeRequest(values, getScriptContent(menu, group, name)));
    }

    /**
     * Execute query.
     *
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     *
     * @return the DB result set
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public DBResultSet executeQuery(final String menu, final String group, final String name)
            throws SQLException, IOException {
        return executeQuery(executeRequest(null, getScriptContent(menu, group, name)));
    }

    /**
     * Execute query.
     *
     * @param <T>
     *            the generic type
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     * @param resultClass
     *            the result class
     *
     * @return the DB iterator
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public <T extends DataBaseObject> DBIterator<T> executeQuery(final String menu, final String group,
            final String name, final Class<T> resultClass) throws SQLException, IOException {
        return executeQuery(resultClass, executeRequest(null, getScriptContent(menu, group, name)));
    }

    /**
     * Execute query.
     *
     * @param sql
     *            the sql
     *
     * @return the DB result set
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public abstract DBResultSet executeQuery(String sql) throws SQLException;

    /**
     * Execute query.
     *
     * @param <T>
     *            the generic type
     * @param resultClass
     *            the result class
     * @param sql
     *            the sql
     *
     * @return the DB iterator
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     */
    public abstract <T extends DataBaseObject> DBIterator<T> executeQuery(Class<T> resultClass, String sql)
            throws SQLException;

    /**
     * Execute update.
     *
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     * @param values
     *            the values
     *
     * @return the int
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public int executeUpdate(final String menu, final String group, final String name, final Map<String, String> values)
            throws SQLException, IOException {
        return executeUpdate(executeRequest(values, getScriptContent(menu, group, name)));
    }

    /**
     * Execute update.
     *
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     *
     * @return the int
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public int executeUpdate(final String menu, final String group, final String name)
            throws SQLException, IOException {
        return executeUpdate(menu, group, name, null);
    }

    /**
     * Execute update.
     *
     * @param sql
     *            the sql
     *
     * @return the int
     *
     * @throws java.sql.SQLException
     *             the SQL exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract int executeUpdate(String sql) throws SQLException, IOException;

    /**
     * Gets the value.
     *
     * @param name
     *            the name
     * @param comment
     *            the comment
     * @param possibleValues
     *            the possible values
     * @param defaultValue
     *            the default value
     *
     * @return the value
     */
    public String getValue(final String name, final String comment, final String[] possibleValues,
            final String defaultValue) {
        return null;
    }

    /**
     * Show help.
     *
     * @param sql
     *            the sql
     * @param message
     *            the message
     */
    public void showHelp(final String sql, final String message) {
    }

    /**
     * Gets the confirmation.
     *
     * @param title
     *            the title
     * @param message
     *            the message
     *
     * @return the confirmation
     */
    public boolean getConfirmation(final String title, final String message) {
        return true;
    }

    /**
     * Load scripts.
     *
     * @param filter
     *            the filter
     * @param repository
     *            the repository
     */
    public void loadScripts(final FilenameFilter filter, final String repository) {
        final List<Object[]> ordered = new ArrayList<>();
        final List<Object[]> others = new ArrayList<>();
        final var paths = new StringTokenizer(repository, File.pathSeparator);
        while (paths.hasMoreElements()) {
            final var dir = new File(paths.nextToken());
            final var scripts = dir.list(filter);
            for (var i = 0; scripts != null && i < scripts.length; i++) {
                try {
                    final var file = new File(dir, scripts[i]);
                    final Map<String, String> params = new HashMap<>();
                    final var fis = new FileInputStream(file);
                    final var dis = new BufferedReader(new InputStreamReader(fis));
                    String line;
                    String name = null;
                    String menu = null;
                    String group = null;
                    var id = -1;
                    try {
                        while ((line = dis.readLine()) != null) {
                            if (line.startsWith("#menu \"") && line.length() > 8) {
                                menu = line.substring(7, line.lastIndexOf("\""));
                                continue;
                            }
                            if (line.startsWith("#name \"") && line.length() > 8) {
                                name = line.substring(7, line.lastIndexOf("\""));
                                continue;
                            }
                            if (line.startsWith("#group \"") && line.length() > 9) {
                                group = line.substring(8, line.lastIndexOf("\""));
                                continue;
                            }
                            if (line.startsWith("#id \"") && line.length() > 6) {
                                try {
                                    id = Integer.parseInt(line.substring(5, line.lastIndexOf("\"")));
                                } catch (final NumberFormatException e) {
                                    id = -1;
                                }
                                continue;
                            }
                            if (line.startsWith("#param \"") && line.length() > 9) {
                                final var param = line.substring(8, line.lastIndexOf("\""));
                                final var index = param.indexOf("=");
                                if (index != -1) {
                                    params.put(param.substring(0, index), param.substring(index + 1));
                                }
                                continue;
                            }
                        }
                        if (name != null && menu != null) {
                            if (group == null) {
                                group = "common";
                            }
                            if (id == -1) {
                                others.add(new Object[] { group, menu, name, file, params });
                            } else {
                                var found = false;
                                int j;
                                for (j = 0; j < ordered.size(); j++) {
                                    final var value = ordered.get(j);
                                    if (id <= (Integer) value[0]) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (found) {
                                    ordered.add(j, new Object[] { id, group, menu, name, file, params });
                                } else {
                                    ordered.add(new Object[] { id, group, menu, name, file, params });
                                }
                            }
                        }
                    } finally {
                        dis.close();
                    }
                } catch (final Exception e) {
                    _log.debug(e);
                }
            }
        }
        final Map<String, String> group = new HashMap<>();
        var loaded = 0;
        for (final Object[] menu : ordered) {
            final var grp = group.get(menu[2]);
            try {
                if (addScriptContent((String) menu[2], (String) menu[1], (String) menu[3], (File) menu[4],
                        grp != null && !grp.equals(menu[1]))) {
                    loaded++;
                }
            } catch (final IOException e) {
                _log.debug(e);
            }
            group.put((String) menu[2], (String) menu[1]);
        }
        for (final Object[] menu : others) {
            final var grp = group.get(menu[1]);
            try {
                if (addScriptContent((String) menu[1], (String) menu[0], (String) menu[2], (File) menu[3],
                        grp != null && !grp.equals(menu[0]))) {
                    loaded++;
                }
            } catch (final IOException e) {
                _log.debug(e);
            }
            group.put((String) menu[1], (String) menu[0]);
        }
        _log.debug("{} script(s) loaded from {}", loaded, repository);
    }

    /**
     * Adds the script content.
     *
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     * @param file
     *            the file
     * @param separator
     *            the separator
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private boolean addScriptContent(final String menu, final String group, final String name, final File file,
            final boolean separator) throws IOException {
        final var menus = allMenus.computeIfAbsent(menu, k -> new ConcurrentHashMap<>());
        final var groups = menus.computeIfAbsent(group, k -> new ConcurrentHashMap<>());
        final var fileKey = menu + "." + group + "." + name;
        if (!groups.containsKey(name)) {
            groups.put(name, getScriptContent(file));
            allFiles.put(fileKey, file);
            _log.debug("Script {} loaded from {}", name, file.getAbsolutePath());
            return true;
        }
        // Let's not overwrite (select the first one in the repository path)
        final var original = allFiles.get(fileKey);
        _log.debug("Script {} NOT loaded from {} (already loaded{})", name, file.getAbsolutePath(),
                original == null ? "" : " from " + original.getAbsolutePath());
        return false;
    }

    /**
     * Update script content.
     *
     * @param menu
     *            the menu
     * @param group
     *            the group
     * @param name
     *            the name
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void updateScriptContent(final String menu, final String group, final String name) throws IOException {
        final var menus = allMenus.get(menu);
        if (menus == null) {
            throw new IOException("menu " + menu + ": not found");
        }
        final var groups = menus.get(group);
        if (groups == null) {
            throw new IOException("group " + group + ": not found");
        }
        final var file = allFiles.get(menu + "." + group + "." + name);
        groups.put(name, getScriptContent(file));
    }

    /**
     * Gets the sql parameters. Legacy method for the ECCMD daemon which is expecting some Hashtables.
     *
     * @return the sql parameters
     */
    public Hashtable<String, Hashtable<String, Hashtable<String, String>>> getSqlParameters() {
        final var scripts = new Hashtable<String, Hashtable<String, Hashtable<String, String>>>();
        for (final String menu : allMenus.keySet()) {
            final var groups = allMenus.get(menu);
            final var groupsResult = new Hashtable<String, Hashtable<String, String>>();
            scripts.put(menu, groupsResult);
            for (final String group : groups.keySet()) {
                final var options = groups.get(group);
                final var optionsResult = new Hashtable<String, String>();
                groupsResult.put(group, optionsResult);
                for (final String option : options.keySet()) {
                    optionsResult.put(option, options.get(option));
                }
            }
        }
        return scripts;
    }
}
