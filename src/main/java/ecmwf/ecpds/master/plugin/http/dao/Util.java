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
     * @throws ecmwf.web.model.ModelException
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
     * Gets the id.
     *
     * @param string
     *            the string
     *
     * @return the id
     */
    private static String getId(final String string) {
        final var result = new StringBuilder();
        for (final char c : string.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && (c != '-') && (c != '_') && (c != ':')) {
                break;
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Gets the href.
     *
     * @param name
     *            the name
     * @param ressource
     *            the ressource
     * @param user
     *            the user
     * @param line
     *            the line
     *
     * @return the href
     */
    private static String getHREF(final String name, final String ressource, final User user, String line) {
        // Let's search for the parameter name!
        final var pos = line.toLowerCase().indexOf(name.toLowerCase() + "=");
        if (pos >= 0) {
            // We found it so now we have to get its value!
            final var id = getId(line.substring(pos + name.length() + 1));
            if (id.length() > 0) {
                // A value is found!
                boolean authorized;
                try {
                    // Is the user authorized to access the link?
                    authorized = user != null && user.hasAccess(ressource);
                } catch (final UserException e) {
                    // Let's not authorize it!
                    authorized = false;
                }
                try {
                    // Build the line with the link if authorized or just the
                    // name if not authorized!
                    line = line.substring(0, pos)
                            + (authorized ? "<a href=\"" + ressource + "/" + id + "\">" + name + " " + id + "</a>"
                                    : name + " " + id)
                            + getHREF(name, ressource, user, line.substring(pos + name.length() + id.length() + 1));
                } catch (final Throwable t) {
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
    private static String getHREF(final String protocol, String line) {
        // Let's search for the parameter name!
        final var pos = line.toLowerCase().indexOf(protocol + "://");
        if (pos >= 0) {
            // We found it so now we have to get its value!
            var pos2 = line.substring(pos).indexOf(" ");
            if (pos2 == -1) {
                // This is the end of the line!
                pos2 = line.substring(pos).length();
            }
            if (pos2 > 0) {
                try {
                    final var url = line.substring(pos, pos + pos2);
                    // Build the line with the link!
                    line = line.substring(0, pos) + "<a href=\"" + url + "\">" + url + "</a>"
                            + getHREF(protocol, line.substring(pos + pos2));
                } catch (final Throwable t) {
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
    private static String getTime(String line) {
        // Let's search for the time parameter!
        final var pos = line.toLowerCase().indexOf("time=");
        if (pos >= 0) {
            // We found it so now we have to get its value!
            var pos2 = line.substring(pos).indexOf(" ");
            if (pos2 == -1) {
                // This is the end of the line!
                pos2 = line.substring(pos).length();
            }
            if (pos2 > 0) {
                try {
                    final var time = line.substring(pos + 5, pos + pos2);
                    // Build the line with the date formated!
                    line = line.substring(0, pos) + "<font color=\"black\">"
                            + Format.formatTime("MMM dd HH:mm:ss", Long.parseLong(time)) + "</font>"
                            + getTime(line.substring(pos + pos2));
                } catch (final Throwable t) {
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
    private static String getStart(String line) {
        // Let's search for the start parameter!
        final var pos = line.toLowerCase().indexOf("start=");
        if (pos >= 0) {
            // We found it so now we have to get its value!
            var pos2 = line.substring(pos).indexOf(" ");
            if (pos2 == -1) {
                // This is the end of the line!
                pos2 = line.substring(pos).length();
            }
            if (pos2 > 0) {
                try {
                    final var time = line.substring(pos + 6, pos + pos2);
                    // Build the line with the duration formated!
                    line = line.substring(0, pos)
                            + Format.formatDuration(Long.parseLong(time), System.currentTimeMillis())
                            + getStart(line.substring(pos + pos2));
                } catch (final Throwable t) {
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
        final var reader = new BufferedReader(new StringReader(beautify(Format.escapeHTML(output))));
        while (true) {
            String line = null;
            try {
                line = reader.readLine();
            } catch (final Throwable t) {
            }
            if (line == null) {
                break;
            }
            // Look for entries with "DataFileId=1387466"
            line = getHREF("DataFileId", "/do/datafile/datafile", user, line);
            // Look for entries with "DataTransferId=1387466"
            line = getHREF("DataTransferId", "/do/transfer/data", user, line);
            // Look for entries with "Destination=TST1"
            line = getHREF("Destination", "/do/transfer/destination", user, line);
            // Look for entries with "Host=123456"
            line = getHREF("Host", "/do/transfer/host", user, line);
            // Look for entries with "DataMover=ecpds-dm1"
            line = getHREF("DataMover", "/do/datafile/transferserver", user, line);
            // Look for entries with "TransferGroup=internet"
            line = getHREF("TransferGroup", "/do/datafile/transfergroup", user, line);
            // Look for entries with "TransferMethod=internet"
            line = getHREF("TransferMethod", "/do/transfer/method", user, line);
            // Look for entries with "WebUser=syi"
            line = getHREF("WebUser", "/do/user/user", user, line);
            // Look for entries with "DataUser=syi"
            line = getHREF("DataUser", "/do/user/incoming", user, line);
            // check if we have an "http://", "https://" or "ftp://" url?
            line = getHREF("http", line);
            line = getHREF("https", line);
            line = getHREF("ftp", line);
            // Do we have any "time" or "start" parameters to process?
            line = getTime(line);
            line = getStart(line);
            // Check if it is requested to use a different color?
            final var toLowerCase = line.toLowerCase();
            if (toLowerCase.startsWith("log:")) {
                result.append("<font color=\"green\">").append(line.substring(4)).append("</font>\n");
            } else if (toLowerCase.startsWith("err:")) {
                result.append("<font color=\"red\">").append(line.substring(4)).append("</font>\n");
            } else if (toLowerCase.startsWith("inf:")) {
                result.append("<font color=\"black\">").append(line.substring(4)).append("</font>\n");
            } else {
                result.append(line).append("\n");
            }
        }
        // Do we have any line separator?
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
        } catch (final Throwable e) {
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
