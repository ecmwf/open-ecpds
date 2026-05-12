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

package ecmwf.ecpds.master.plugin.http.home.transfer.searches;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.web.dao.ModelSearchBase;
import ecmwf.web.model.users.User;

/**
 * The Class DestinationsByUser.
 */
public class DestinationsByUser extends ModelSearchBase {

    /** The user. */
    private final User user;

    /** The search. */
    private final String search;

    /** The DataTables column index to sort by. */
    private final int orderColumn;

    /** The ascending sort direction. */
    private final boolean ascending;

    /** The zero-based row offset for pagination (-1 means no limit). */
    private final int start;

    /** The maximum number of rows to return (-1 means no limit). */
    private final int length;

    /** The status. */
    private final String status;

    /** The type. */
    private final String type;

    /** The filter. */
    private final String filter;

    /** The from to aliases. */
    private final String fromToAliases;

    /**
     * Instantiates a new destinations by user (legacy — loads all rows, sorts by name).
     *
     * @param user
     *            the user
     * @param search
     *            the search
     * @param fromToAliases
     *            the from to aliases
     * @param asc
     *            the ascending sort direction for name column
     * @param status
     *            the status
     * @param type
     *            the type
     * @param filter
     *            the filter
     */
    public DestinationsByUser(final User user, final String search, final String fromToAliases, final boolean asc,
            final String status, final String type, final String filter) {
        this(user, search, fromToAliases, 1, asc, 0, -1, status, type, filter);
    }

    /**
     * Instantiates a new destinations by user with full pagination and sort control.
     *
     * @param user
     *            the user
     * @param search
     *            the search
     * @param fromToAliases
     *            the from to aliases
     * @param orderColumn
     *            the DataTables column index to sort by
     * @param ascending
     *            true for ascending sort
     * @param start
     *            the zero-based row offset (-1 for no limit)
     * @param length
     *            the maximum number of rows (-1 for no limit)
     * @param status
     *            the status
     * @param type
     *            the type
     * @param filter
     *            the filter
     */
    public DestinationsByUser(final User user, final String search, final String fromToAliases, final int orderColumn,
            final boolean ascending, final int start, final int length, final String status, final String type,
            final String filter) {
        this.user = user;
        this.search = search;
        this.fromToAliases = fromToAliases;
        this.orderColumn = orderColumn;
        this.ascending = ascending;
        this.start = start;
        this.length = length;
        this.status = status;
        this.type = type;
        this.filter = filter;
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
     * Gets the from to aliases.
     *
     * @return the from to aliases
     */
    public String getFromToAliases() {
        return fromToAliases;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Gets the order column index.
     *
     * @return the DataTables column index to sort by
     */
    public int getOrderColumn() {
        return orderColumn;
    }

    /**
     * Gets the ascending sort flag.
     *
     * @return true for ascending sort
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Gets the start offset.
     *
     * @return the zero-based row offset (-1 means no limit)
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets the length.
     *
     * @return the maximum number of rows (-1 means no limit)
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }
}
