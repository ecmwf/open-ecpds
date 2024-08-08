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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ecmwf.common.technical.ByteSize;

/**
 * The Class SQLParameterParser.
 */
public class SQLParameterParser {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SQLParameterParser.class);

    /** The Constant REGEX_BOOLEAN. */
    private static final String REGEX_BOOLEAN = "(?i)^(true|yes|false|no)$";

    /** The Constant REGEX_NUMBER. */
    private static final String REGEX_NUMBER = "-?\\d+(\\.\\d+)?";

    /** The Constant OPERATORS. */
    protected static final String[] OPERATORS = { "!=", "=", "<", ">", "<=", ">=" };

    /** The Constant TYPE_STRING. */
    protected static final String TYPE_STRING = "s"; // default type

    /** The Constant TYPE_DIGITS. */
    protected static final String TYPE_DIGITS = "d";

    /** The Constant TYPE_BYTES. */
    protected static final String TYPE_BYTES = "b";

    /** The Constant TYPE_BOOLEAN. */
    protected static final String TYPE_BOOLEAN = "?";

    /** The Constant CASE_SENSITIVE. */
    protected static final String CASE_SENSITIVE = "s"; // default case

    /** The Constant CASE_INSENSITIVE. */
    protected static final String CASE_INSENSITIVE = "i";

    /** The params. */
    private final Map<String, List<Map.Entry<String, String>>> params = new LinkedHashMap<>();

    /** The allowed options. */
    private final ArrayList<String> allowedOptions = new ArrayList<>();

    /** The option types. */
    private final ArrayList<String> optionTypes = new ArrayList<>();

    /** The is case sensitive. */
    private final boolean isCaseSensitive;

    /** The search. */
    private final String search;

    /**
     * Instantiates a new SQL parameter parser.
     *
     * @param search
     *            the search
     * @param defaultOption
     *            the default option
     * @param otherOptions
     *            the other options
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public SQLParameterParser(final String search, final String defaultOption, final String... otherOptions)
            throws IOException {
        add(defaultOption);
        for (final String otherOption : otherOptions) {
            add(otherOption);
        }
        this.search = search;
        var sensitive = true;
        if (search != null && !search.isEmpty()) {
            final var tokens = tokenizeInput(search);
            if (tokens.size() == 1 && extractKeyValue(tokens.get(0))[0].equalsIgnoreCase(search)) {
                final List<Map.Entry<String, String>> list = new ArrayList<>();
                list.add(new AbstractMap.SimpleEntry<>("=", getSQLString(defaultOption, search.trim(), getType(0))));
                params.put(defaultOption, list);
            } else {
                for (final String token : tokens) {
                    final var keyValue = extractKeyValue(token);
                    final var keyName = keyValue[0];
                    if ("case".equals(keyName)) {
                        sensitive = CASE_SENSITIVE.equalsIgnoreCase(keyValue[2]);
                    } else {
                        if (!isValid(allowedOptions.stream(), keyName)) {
                            throw new IOException("Rule '" + keyName + "' not known");
                        }
                        final var list = params.getOrDefault(keyName, new ArrayList<>());
                        list.add(new AbstractMap.SimpleEntry<>(keyValue[1],
                                getSQLString(keyName, keyValue[2], getType(keyName))));
                        params.put(keyName, list);
                    }
                }
            }
        }
        isCaseSensitive = sensitive;
    }

    /**
     * Gets the type.
     *
     * @param keyname
     *            the keyname
     *
     * @return the type
     */
    private String getType(final String keyname) {
        return getType(allowedOptions.indexOf(keyname));
    }

    /**
     * Gets the type.
     *
     * @param keyPosition
     *            the key position
     *
     * @return the type
     */
    private String getType(final int keyPosition) {
        return optionTypes.get(keyPosition);
    }

    /**
     * Adds the.
     *
     * @param option
     *            the option
     */
    private void add(final String option) {
        final var parts = option.split("=");
        allowedOptions.add(parts[0].toLowerCase());
        optionTypes.add(parts.length > 1 ? parts[1].toLowerCase() : TYPE_STRING);
    }

    /**
     * Gets the search.
     *
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * Checks if is case sensitive.
     *
     * @return true, if is case sensitive
     */
    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    /**
     * Tokenize input.
     *
     * @param input
     *            the input
     *
     * @return the list
     */
    private static List<String> tokenizeInput(final String input) {
        final List<String> tokens = new ArrayList<>();
        final var token = new StringBuilder();
        var inQuotes = false;
        var escaped = false;
        for (final char c : input.toCharArray()) {
            if (escaped) {
                token.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                tokens.add(token.toString());
                token.setLength(0);
            } else {
                token.append(c);
            }
        }
        if (token.length() > 0) {
            tokens.add(token.toString());
        }
        return tokens;
    }

    /**
     * Extract key value.
     *
     * @param statement
     *            the statement
     *
     * @return the string[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String[] extractKeyValue(final String statement) throws IOException {
        final var keyName = new StringBuilder();
        final var operator = new StringBuilder();
        final var value = new StringBuilder();
        var operatorFound = false;
        for (var i = 0; i < statement.length(); i++) {
            final var c = statement.charAt(i);
            if (!operatorFound) {
                if (c == '=' || c == '!' || c == '<' || c == '>') {
                    operator.append(c);
                    if (i + 1 < statement.length()
                            && (statement.charAt(i + 1) == '=' || statement.charAt(i + 1) == '>')) {
                        operator.append(statement.charAt(i + 1));
                        i++;
                    }
                    operatorFound = true;
                    if (!isValid(Stream.of(OPERATORS), operator)) {
                        throw new IOException("Operator '" + operator + "' not known for rule '" + keyName + "'");
                    }
                } else {
                    keyName.append(c);
                }
            } else {
                value.append(c);
            }
        }
        return new String[] { keyName.toString().toLowerCase(), operator.toString(), value.toString() };
    }

    /**
     * Checks if is valid.
     *
     * @param allowed
     *            the allowed
     * @param operator
     *            the operator
     *
     * @return true, if is valid
     */
    private static boolean isValid(final Stream<String> allowed, final CharSequence operator) {
        return allowed.anyMatch(op -> op.equals(operator.toString()));
    }

    /**
     * Gets the SQL string.
     *
     * @param keyName
     *            the key name
     * @param search
     *            the search
     * @param type
     *            the type
     *
     * @return the SQL string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static final String getSQLString(final String keyName, final String search, final String type)
            throws IOException {
        if (search == null || "".equals(search.trim())) {
            return "%";
        }
        final var result = new StringBuilder();
        var foundWildcards = false;
        for (final char c : search.toCharArray()) {
            switch (c) {
            case '%':
                result.append("\\%");
                break;
            case '_':
                result.append("\\_");
                break;
            case '*':
                result.append('%');
                foundWildcards = true;
                break;
            case '?':
                result.append('_');
                foundWildcards = true;
                break;
            default:
                result.append(c);
                break;
            }
        }
        if (!TYPE_STRING.equals(type) && foundWildcards) {
            throw new IOException("No wildcards allowed for rule '" + keyName + "'");
        }
        final var stringResult = result.toString();
        if (TYPE_BYTES.equals(type)) {
            try {
                return "" + ByteSize.parse(stringResult).size();
            } catch (final IllegalArgumentException e) {
                throw new IOException("Cannot parse number of bytes for rule '" + keyName + "' (e.g. 10MB or 1024B)");
            }
        }
        if (TYPE_BOOLEAN.equals(type)) {
            if (!stringResult.matches(REGEX_BOOLEAN)) {
                throw new IOException("Only 'yes|no' or 'true|false' allowed for rule '" + keyName + "'");
            }
            return stringResult.matches("(?i)^(true|yes)$") ? "1" : "0";
        } else {
            return stringResult;
        }
    }

    /**
     * Gets the case string.
     *
     * @param isCaseSensitive
     *            the is case sensitive
     *
     * @return the case string
     */
    private static String getCaseString(final boolean isCaseSensitive) {
        final var sensitive = isCaseSensitive ? CASE_SENSITIVE : CASE_INSENSITIVE;
        return "COLLATE latin1_general_c" + sensitive + " ";
    }

    /**
     * Gets the operator.
     *
     * @param operator
     *            the operator
     * @param isBooleanOrNumber
     *            the is boolean or number
     *
     * @return the operator
     */
    private String getOperator(final String operator, final boolean isBooleanOrNumber) {
        return switch (operator) {
        case "=" -> isBooleanOrNumber ? "=" : getCaseString(isCaseSensitive) + "LIKE";
        case "!=" -> isBooleanOrNumber ? "!=" : getCaseString(isCaseSensitive) + "NOT LIKE";
        default -> operator;
        };
    }

    /**
     * Gets the SQL statement.
     *
     * @param keyName
     *            the key name
     * @param operator
     *            the operator
     * @param value
     *            the value
     *
     * @return the SQL statement
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String getSQLStatement(final String keyName, final String operator, final String value) throws IOException {
        final var isNumber = value.matches(REGEX_NUMBER);
        if ((operator.startsWith("<") || operator.startsWith(">")) && !isNumber) {
            throw new IOException("Only '=' and '!=' allowed for the rule '" + keyName + "'");
        }
        return " " + getOperator(operator, value.matches(REGEX_BOOLEAN) || isNumber) + " '" + DataBase.escapeSql(value)
                + "'";
    }

    /**
     * Gets the position for key.
     *
     * @param keyName
     *            the key name
     *
     * @return the position for key
     */
    private int getPositionForKey(final String keyName) {
        return IntStream.range(0, params.size()).filter(i -> params.keySet().toArray()[i].equals(keyName)).findFirst()
                .orElse(-1);
    }

    /**
     * Removes the.
     *
     * @param keyPosition
     *            the key position
     *
     * @return the list
     */
    public List<Map.Entry<String, String>> remove(final int keyPosition) {
        return remove(allowedOptions.get(keyPosition));
    }

    /**
     * Removes the.
     *
     * @param keyName
     *            the key name
     *
     * @return the list
     */
    public List<Map.Entry<String, String>> remove(final String keyName) {
        final var lowerCaseKeyName = keyName.toLowerCase();
        final var position = getPositionForKey(lowerCaseKeyName);
        if (position >= 0) {
            final var result = params.remove(lowerCaseKeyName);
            allowedOptions.remove(position);
            optionTypes.remove(position);
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Checks for.
     *
     * @param keyPosition
     *            the key position
     *
     * @return true, if successful
     */
    public boolean has(final int keyPosition) {
        return has(allowedOptions.get(keyPosition));
    }

    /**
     * Checks for.
     *
     * @param key
     *            the key
     *
     * @return true, if successful
     */
    public boolean has(final String key) {
        return params.containsKey(key.toLowerCase());
    }

    /**
     * Gets the.
     *
     * @param keyPosition
     *            the key position
     * @param dataBaseFieldNames
     *            the data base field names
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String get(final int keyPosition, final String... dataBaseFieldNames) throws IOException {
        return get(allowedOptions.get(keyPosition), dataBaseFieldNames);
    }

    /**
     * Process field entry.
     *
     * @param field
     *            the field
     *
     * @return the string
     */
    private static String processFieldEntry(final String field) {
        return field.contains(" ") ? "(" + field + ")" : field;
    }

    /**
     * Gets the.
     *
     * @param keyName
     *            the key name
     * @param dataBaseFieldNames
     *            the data base field names
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String get(final String keyName, final String... dataBaseFieldNames) throws IOException {
        final var lowerCaseKeyName = keyName.toLowerCase();
        final var operations = params.get(lowerCaseKeyName);
        if ((operations == null) || operations.isEmpty()) {
            return "";
        }
        final var result = new StringBuilder();
        for (final String field : dataBaseFieldNames) {
            final var sb = new StringBuilder("(");
            for (final Map.Entry<String, String> pair : operations) {
                if (sb.length() > 1) {
                    sb.append(" AND ");
                }
                sb.append(processFieldEntry(field))
                        .append(getSQLStatement(lowerCaseKeyName, pair.getKey(), pair.getValue()));
            }
            sb.append(")");
            if (result.length() > 0) {
                result.append(" OR ");
            }
            result.append(sb);
        }
        if (dataBaseFieldNames.length > 1) {
            result.insert(0, "(").append(")");
        }
        return result.insert(0, "AND ").toString();
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void main(final String[] args) throws IOException {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);
        for (final String input : new String[] {
                "ts!=12 ts>100 ts<100 options=\"*mqtt=t?st*\" filename='*'.dat priority<=100 priority>10 filename!=\"*t?st\\\" *.dat\" target=*.bin country=u? case=i",
                "filename=*.dat size<=10MB", "EGS", "country=us", "priority>=10", "test=yes" }) {
            final var statements = new SQLParameterParser(input, "filename", "ts=d", "priority=d", "options", "toto",
                    "email", "target", "country", "size=b", "test=?");
            if (_log.isInfoEnabled()) {
                _log.info(
                        "Search:{}\nfilename={}\nts={}\npriority={}\noptions={}\ntoto={}\nemail={}\ncountry={}\nsize={}\ntest={}\n\n",
                        statements.getSearch(), statements.get(0, "DAT_TARGET"),
                        statements.get("ts", "DAT_TIME_STEP", "DAT_TEST_STEP"),
                        statements.get("priority", "DAT_PRIORITY"), statements.get("options", "HOS_DATA"),
                        statements.get(4, "HOS_TOTO"), statements.get("email", "HOS_EMAIL"),
                        statements.get("country", "COUNTRY_NAME", "COUNTRY_ISO"), statements.get("size", "FILE_SIZE"),
                        statements.get("test", "HOS_ACTIVE is NULL", "DAT_EXPIRY_TIME > UNIX_TIMESTAMP() * 1000"));
            }
        }
    }
}
