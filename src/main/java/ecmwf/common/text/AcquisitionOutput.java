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

import java.util.regex.Pattern;

/** Resolves acquisition output for previews without initializing a server. */
public final class AcquisitionOutput {

    private AcquisitionOutput() {
    }

    /** Strips per-line options and filename filters and resolves date tokens. */
    public static String resolve(final String text) {
        if (text == null || text.isEmpty())
            return text;
        final var lines = text.split("\n", -1);
        final var out = new StringBuilder();
        for (var i = 0; i < lines.length; i++) {
            out.append(resolveLine(lines[i]));
            if (i < lines.length - 1)
                out.append('\n');
        }
        return out.toString();
    }

    private static String resolveLine(final String line) {
        var path = line.trim();
        String lineOpts = null;
        if (!path.isEmpty() && path.charAt(0) == '[') {
            var depth = 0;
            var closePos = -1;
            for (var i = 0; i < path.length(); i++) {
                final var c = path.charAt(i);
                if (c == '[')
                    depth++;
                else if (c == ']' && --depth == 0) {
                    closePos = i;
                    break;
                }
            }
            if (closePos != -1) {
                lineOpts = path.substring(1, closePos);
                path = path.substring(closePos + 1).trim();
            }
        }
        if (!path.isEmpty() && path.charAt(path.length() - 1) == '}') {
            final var openBrace = path.lastIndexOf('{');
            if (openBrace != -1)
                path = path.substring(0, openBrace).trim();
        }
        return replaceDateTokens(path, lineOpts);
    }

    private static String replaceDateTokens(final String text, final String lineOptions) {
        var lineFormat = "";
        var lineDelta = "";
        if (lineOptions != null && !lineOptions.isBlank()) {
            for (final var part : lineOptions.split("[,;]")) {
                final var eq = part.indexOf('=');
                if (eq > 0) {
                    final var key = part.substring(0, eq).trim().toLowerCase();
                    final var val = part.substring(eq + 1).trim().replaceAll("^\"|\"$", "");
                    if ("dateformat".equals(key)) {
                        lineFormat = val;
                    } else if ("datedelta".equals(key)) {
                        lineDelta = val;
                    }
                }
            }
        }
        final var pattern = Pattern.compile("\\$(date|dirdate)(?:\\[([^\\]]*)\\])?", Pattern.CASE_INSENSITIVE);
        final var matcher = pattern.matcher(text);
        final var result = new StringBuilder();
        while (matcher.find()) {
            final var params = matcher.group(2);
            var dateformat = lineFormat.isEmpty() ? "yyyyMMdd" : lineFormat;
            var datedelta = lineDelta;
            if (params != null && !params.isBlank()) {
                for (final var part : params.split("[,;]")) {
                    final var eq = part.indexOf('=');
                    if (eq > 0) {
                        final var key = part.substring(0, eq).trim().toLowerCase();
                        final var val = part.substring(eq + 1).trim().replaceAll("^\"|\"$", "");
                        if ("dateformat".equals(key)) {
                            dateformat = val;
                        } else if ("datedelta".equals(key)) {
                            datedelta = val;
                        }
                    }
                }
            }
            long delta = 0;
            try {
                if (!datedelta.isBlank()) {
                    delta = Format.getDuration(datedelta);
                }
            } catch (final Exception ignored) {
                // Preserve the default zero offset for invalid durations.
            }
            final var formatted = Format.formatTime(dateformat, System.currentTimeMillis() + delta);
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(formatted));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
