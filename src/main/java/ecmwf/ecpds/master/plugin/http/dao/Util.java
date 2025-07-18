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

package ecmwf.ecpds.master.plugin.http.dao;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * syi: added methods to allow processing links into comments.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.common.database.DataBaseObject;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.ECpdsSession;
import ecmwf.ecpds.master.plugin.http.model.CollectionSizeBean;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.web.model.ModelException;
import ecmwf.web.model.users.User;
import ecmwf.web.model.users.UserException;
import ecmwf.web.util.bean.Pair;

/**
 * The Class Util.
 */
public final class Util {

    /**
     * Gets the ecpds session from object.
     *
     * @param u
     *            the u
     *
     * @return the ecpds session from object
     *
     * @throws ModelException
     *             the model exception
     */
    public static ECpdsSession getECpdsSessionFromObject(final Object u) throws ModelException {
        if (!(u instanceof final User user)) {
            return null;
        }
        if (user.getCredentials() instanceof final ECpdsSession ecpdsSession) {
            return ecpdsSession;
        } else {
            throw new ModelException(user.getCredentials() != null
                    ? "Got a credential which is not an '" + ECpdsSession.class.getName() + "': "
                            + user.getCredentials().getClass().getName()
                    : "Null credential for user. Can't get ECpdsSession.");
        }
    }

    /**
     * Extracts the leading identifier from the input string, consisting only of letters, digits, '-', '_' or ':'
     * characters. Stops at the first invalid character.
     *
     * @param text
     *            the input string
     *
     * @return the extracted identifier, or an empty string if none is found
     */
    private static String getId(final String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        final var result = new StringBuilder(text.length());
        for (var i = 0; i < text.length(); i++) {
            final var c = text.charAt(i);
            if (isIdentifierChar(c)) {
                result.append(c);
            } else {
                break;
            }
        }
        return result.toString();
    }

    /** Determines whether a character is valid in an identifier. */
    private static boolean isIdentifierChar(final char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ':';
    }

    /**
     * Gets the href.
     *
     * @param name
     *            the name
     * @param resource
     *            the resource
     * @param user
     *            the user
     * @param line
     *            the line
     *
     * @return the href
     */
    private static String getHREF(final String name, final String resource, final User user, final String line) {
        final var lowerLine = line.toLowerCase();
        final var search = name.toLowerCase() + "=";
        final var paramIndex = lowerLine.indexOf(search);
        if (paramIndex >= 0) {
            final var valueStart = paramIndex + name.length() + 1;
            final var remainder = line.substring(valueStart);
            final var id = getId(remainder);
            if (!id.isEmpty()) {
                var authorized = false;
                try {
                    authorized = user != null && user.hasAccess(resource);
                } catch (final UserException _) {
                    // Access denied, fallback to plain text
                }
                try {
                    final var before = line.substring(0, paramIndex);
                    final var after = line.substring(valueStart + id.length());
                    final var replacement = authorized
                            ? "<a href=\"" + resource + "/" + id + "\">" + name + " " + id + "</a>" : name + " " + id;

                    return before + replacement + getHREF(name, resource, user, after);
                } catch (final IndexOutOfBoundsException _) {
                    // Defensive: malformed input
                }
            }
        }
        return line;
    }

    /**
     * Gets the href.
     *
     * @param protocol
     *            the protocol
     * @param line
     *            the line
     *
     * @return the href
     */
    private static String getHREF(final String protocol, final String line) {
        final var lowerLine = line.toLowerCase();
        final var prefix = protocol.toLowerCase() + "://";
        final var start = lowerLine.indexOf(prefix);
        if (start >= 0) {
            var end = line.indexOf(' ', start);
            if (end == -1) {
                end = line.length();
            }
            if (end > start) {
                try {
                    final var url = line.substring(start, end);
                    final var before = line.substring(0, start);
                    final var after = line.substring(end);
                    final var linked = "<a href=\"" + url + "\">" + url + "</a>";
                    return before + linked + getHREF(protocol, after);
                } catch (final IndexOutOfBoundsException _) {
                    // Malformed input, ignore and return original line
                }
            }
        }
        return line;
    }

    /**
     * Gets the time.
     *
     * @param line
     *            the line
     *
     * @return the time
     */
    private static String getTime(final String line) {
        final var lowerLine = line.toLowerCase();
        final var key = "time=";
        final var keyIndex = lowerLine.indexOf(key);
        if (keyIndex >= 0) {
            final var valueStart = keyIndex + key.length();
            final var spaceIndex = line.indexOf(' ', valueStart);
            final var valueEnd = (spaceIndex != -1) ? spaceIndex : line.length();
            if (valueEnd > valueStart) {
                try {
                    final var timestampStr = line.substring(valueStart, valueEnd);
                    final var timestamp = Long.parseLong(timestampStr);
                    final var formatted = Format.formatTime("MMM dd HH:mm:ss", timestamp);
                    final var before = line.substring(0, keyIndex);
                    final var after = line.substring(valueEnd);
                    return before + "<font color=\"black\">" + formatted + "</font>" + getTime(after);
                } catch (NumberFormatException | IndexOutOfBoundsException _) {
                    // Ignore malformed time value
                }
            }
        }
        return line;
    }

    /**
     * Gets the start.
     *
     * @param line
     *            the line
     *
     * @return the start
     */
    private static String getStart(final String line) {
        final var lowerLine = line.toLowerCase();
        final var key = "start=";
        final var keyIndex = lowerLine.indexOf(key);
        if (keyIndex >= 0) {
            final var valueStart = keyIndex + key.length();
            final var spaceIndex = line.indexOf(' ', valueStart);
            final var valueEnd = (spaceIndex != -1) ? spaceIndex : line.length();
            if (valueEnd > valueStart) {
                try {
                    final var timeStr = line.substring(valueStart, valueEnd);
                    final var startTime = Long.parseLong(timeStr);
                    final var formatted = Format.formatDuration(startTime, System.currentTimeMillis());
                    final var before = line.substring(0, keyIndex);
                    final var after = line.substring(valueEnd);
                    return before + formatted + getStart(after);
                } catch (NumberFormatException | IndexOutOfBoundsException _) {
                    // Ignore malformed start time
                }
            }
        }
        return line;
    }

    /**
     * Beautify.
     *
     * @param string
     *            the string
     *
     * @return the string
     */
    private static String beautify(final String string) {
        // Replace the sign ' <- ' by a left arrow
        return string.replace(" &#60;- ", " &#10229; ");
    }

    /**
     * Gets the formatted.
     *
     * @param user
     *            the user
     * @param output
     *            the output
     *
     * @return the formatted
     */
    public static String getFormatted(final User user, final String output) {
        if (output == null) {
            return null;
        }
        final var result = new StringBuilder();
        final var escaped = Format.escapeHTML(output);
        final var reader = new BufferedReader(new StringReader(beautify(escaped)));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // Add hyperlinks for different identifiers
                line = getHREF("DataFileId", "/do/datafile/datafile", user, line);
                line = getHREF("DataTransferId", "/do/transfer/data", user, line);
                line = getHREF("Destination", "/do/transfer/destination", user, line);
                line = getHREF("Host", "/do/transfer/host", user, line);
                line = getHREF("DataMover", "/do/datafile/transferserver", user, line);
                line = getHREF("TransferGroup", "/do/datafile/transfergroup", user, line);
                line = getHREF("TransferMethod", "/do/transfer/method", user, line);
                line = getHREF("WebUser", "/do/user/user", user, line);
                line = getHREF("DataUser", "/do/user/incoming", user, line);
                line = getHREF("MasterServer", null, null, line);
                // Linkify known protocols
                line = getHREF("http", line);
                line = getHREF("https", line);
                line = getHREF("ftp", line);
                // Format time and start fields
                line = getTime(line);
                line = getStart(line);
                // Color-coded log levels
                final var lower = line.toLowerCase();
                if (lower.startsWith("log:")) {
                    result.append("<font color=\"green\">").append(line.substring(4)).append("</font>\n");
                } else if (lower.startsWith("err:")) {
                    result.append("<font color=\"red\">").append(line.substring(4)).append("</font>\n");
                } else if (lower.startsWith("inf:")) {
                    result.append("<font color=\"black\">").append(line.substring(4)).append("</font>\n");
                } else {
                    result.append(line).append("\n");
                }
            }
        } catch (final IOException _) {
            // Unlikely with StringReader
        }
        // Replace separator lines with paragraph breaks
        Format.replaceAll(result, "------------------------------------------------------------------------", "<p>");
        return result.toString();
    }

    /**
     * The Interface DefaultValue.
     */
    public interface DefaultValue {

        /**
         * Gets the default value.
         *
         * @return the default value
         */
        String getDefaultValue();
    }

    /**
     * Gets the value.
     *
     * @param request
     *            the request
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the value
     */
    public static String getValue(final HttpServletRequest request, final String name, final String defaultValue) {
        return getValue(request, name, () -> defaultValue);
    }

    /**
     * Gets the value.
     *
     * @param request
     *            the request
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the value
     */
    public static String getValue(final HttpServletRequest request, final String name,
            final DefaultValue defaultValue) {
        final var session = request.getSession();
        final var value = request.getParameter(name);
        final var result = value != null ? value.trim() : (String) session.getAttribute(name);
        final var finalResult = result == null || result.isEmpty() ? defaultValue.getDefaultValue() : result;
        session.setAttribute(name, finalResult);
        return finalResult;
    }

    /**
     * Gets the data base cursor.
     *
     * @param displayId
     *            the display id
     * @param recordsPerPage
     *            the records per page
     * @param defaultSort
     *            the default sort
     * @param descending
     *            the descending
     * @param request
     *            the request
     *
     * @return the data base cursor
     */
    public static DataBaseCursor getDataBaseCursor(final String displayId, final int recordsPerPage,
            final int defaultSort, final boolean descending, final HttpServletRequest request) {
        request.setAttribute("recordsPerPage", recordsPerPage);
        final var encoder = new ParamEncoder(displayId);
        // Get the start and end indexes!
        final var paramPageNumber = encoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE);
        var start = 0;
        try {
            start = Integer.parseInt(request.getParameter(paramPageNumber));
        } catch (final Throwable _) {
            // Ignored
        }
        if (start > 0) {
            start = (start - 1) * recordsPerPage;
        }
        final var end = start + recordsPerPage;
        // Find the sort parameter!
        var sort = request.getParameter(encoder.encodeParameterName(TableTagParameters.PARAMETER_SORT));
        if (sort == null) {
            sort = String.valueOf(defaultSort);
        }
        // Find the order parameter!
        var order = request.getParameter(encoder.encodeParameterName(TableTagParameters.PARAMETER_ORDER));
        if (order == null) {
            order = descending ? "2" : "1"; // Descending is 2, Ascending is 1!
        }
        return new DataBaseCursor(sort, order, start, end);
    }

    /**
     * Gets the collection from.
     *
     * @param collection
     *            the collection
     *
     * @return the collection from
     */
    public static int getCollectionFrom(final Collection<? extends DataBaseObject> collection) {
        var collectionSize = 0;
        for (final DataBaseObject initialBean : collection) {
            collectionSize = initialBean.getCollectionSize();
            if (collectionSize != -1) {
                break;
            }
        }
        return collectionSize;
    }

    /**
     * Gets the collection size from.
     *
     * @param collection
     *            the collection
     *
     * @return the collection size from
     */
    public static int getCollectionSizeFrom(final Collection<? extends CollectionSizeBean> collection) {
        var collectionSize = 0;
        for (final CollectionSizeBean initialBean : collection) {
            collectionSize = initialBean.getCollectionSize();
            if (collectionSize != -1) {
                break;
            }
        }
        return collectionSize;
    }

    /**
     * Gets the destination pair list.
     *
     * @param list
     *            the list
     * @param toRemove
     *            the to remove
     *
     * @return the destination pair list
     */
    public static Collection<Pair> getDestinationPairList(final Collection<Pair> list,
            final Collection<Destination> toRemove) {
        final List<Pair> pairListToRemove = new ArrayList<>();
        for (final Destination destination : toRemove) {
            for (final Pair pair : list) {
                final var destinationName = destination.getName();
                if (pair.getName().equals(destinationName)) {
                    pairListToRemove.add(pair);
                }
            }
        }
        list.removeAll(pairListToRemove);
        return list.stream().sorted(
                (pair1, pair2) -> String.valueOf(pair1.getName()).compareToIgnoreCase(String.valueOf(pair2.getName())))
                .toList();
    }
}
