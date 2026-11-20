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

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** Compiles declared SQL value parameters without interpreting SQL literals or comments. */
final class SQLBindings {
    private static final Pattern DECLARATION = Pattern.compile("#bind \"([A-Za-z][A-Za-z0-9_]*);([A-Za-z]+)\"");

    enum Type {
        STRING, LONG, INTEGER, BOOLEAN, DECIMAL, TIMESTAMP, DATE, TIME;

        Object convert(final String name, final Object value) throws IOException {
            if (value == null) {
                return null;
            }
            try {
                final var text = value.toString();
                return switch (this) {
                case STRING -> text;
                case LONG -> Long.valueOf(text.trim());
                case INTEGER -> Integer.valueOf(text.trim());
                case BOOLEAN -> switch (text.trim().toLowerCase(Locale.ROOT)) {
                case "true", "1" -> true;
                case "false", "0" -> false;
                default -> throw new IllegalArgumentException();
                };
                case DECIMAL -> new BigDecimal(text.trim());
                case TIMESTAMP -> value instanceof Timestamp ? value : value instanceof Instant instant
                        ? Timestamp.from(instant)
                        : value instanceof LocalDateTime local ? Timestamp.valueOf(local) : Timestamp.valueOf(text);
                case DATE -> value instanceof Date ? value : Date.valueOf(text);
                case TIME -> value instanceof Time ? value : Time.valueOf(text);
                };
            } catch (final IllegalArgumentException failure) {
                throw new IOException("Invalid " + this + " SQL parameter: " + name);
            }
        }
    }

    record Query(String sql, List<String> names) {
        List<Object> arguments(final Map<String, Type> declarations, final Map<String, ?> values) throws IOException {
            final var arguments = new ArrayList<Object>(names.size());
            for (final var name : names) {
                if (!values.containsKey(name)) {
                    throw new IOException("Missing bound SQL parameter: " + name);
                }
                arguments.add(declarations.get(name).convert(name, values.get(name)));
            }
            return arguments;
        }
    }

    static Map<String, Type> declarations(final String script) throws IOException {
        final Map<String, Type> result = new LinkedHashMap<>();
        for (final var line : script.split("\\R")) {
            if (!line.startsWith("#bind")) {
                continue;
            }
            final var matcher = DECLARATION.matcher(line);
            if (!matcher.matches()) {
                throw new IOException("Malformed SQL binding declaration");
            }
            final Type type;
            try {
                type = Type.valueOf(matcher.group(2).toUpperCase(Locale.ROOT));
            } catch (final IllegalArgumentException failure) {
                throw new IOException("Unknown SQL binding type: " + matcher.group(2));
            }
            if (result.putIfAbsent(matcher.group(1), type) != null) {
                throw new IOException("Duplicate SQL binding: " + matcher.group(1));
            }
        }
        return result;
    }

    static Query compile(final String source, final Map<String, Type> declarations) throws IOException {
        final var sql = new StringBuilder(source.length());
        final var names = new ArrayList<String>();
        boolean terminated = false;
        for (int i = 0; i < source.length();) {
            final char c = source.charAt(i);
            final char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';
            if (Character.isWhitespace(c)) {
                sql.append(c);
                i++;
            } else if (c == '#' || c == '-' && next == '-'
                    && (i + 2 == source.length() || Character.isWhitespace(source.charAt(i + 2)))) {
                final int end = source.indexOf('\n', i);
                final int through = end < 0 ? source.length() : end;
                sql.append(source, i, through);
                i = through;
            } else if (c == '/' && next == '*') {
                final int end = source.indexOf("*/", i + 2);
                if (end < 0) {
                    throw new IOException("Unterminated SQL comment");
                }
                if (source.startsWith("/*!", i) || source.startsWith("/*M!", i)) {
                    throw new IOException("Executable comments are not supported in bound SQL");
                }
                sql.append(source, i, end + 2);
                i = end + 2;
            } else {
                if (terminated) {
                    throw new IOException("Bound SQL scripts must contain one statement");
                }
                if (c == '\'' || c == '"' || c == '`') {
                    final int start = i++;
                    boolean closed = false;
                    while (i < source.length()) {
                        final char current = source.charAt(i++);
                        if (current == '\\' && i < source.length()) {
                            i++;
                        } else if (current == c) {
                            if (i < source.length() && source.charAt(i) == c) {
                                i++;
                            } else {
                                closed = true;
                                break;
                            }
                        }
                    }
                    if (!closed) {
                        throw new IOException("Unterminated SQL quoted value");
                    }
                    sql.append(source, start, i);
                } else if (c == ':' && next == ':') {
                    sql.append("::");
                    i += 2;
                } else if (c == ':' && (Character.isLetter(next) || next == '_')) {
                    final int start = ++i;
                    while (i < source.length()
                            && (Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '_')) {
                        i++;
                    }
                    final var name = source.substring(start, i);
                    if (!declarations.containsKey(name)) {
                        throw new IOException("Undeclared SQL binding: " + name);
                    }
                    names.add(name);
                    sql.append('?');
                } else if (c == '?') {
                    throw new IOException("Use declared named parameters, not positional SQL markers");
                } else {
                    if (c == ';') {
                        terminated = true;
                    }
                    sql.append(c);
                    i++;
                }
            }
        }
        return new Query(sql.toString(), names);
    }

    private SQLBindings() {
    }
}
