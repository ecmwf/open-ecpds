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

package ecmwf.ecpds.master.plugin.http.home.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationAliasedFrom;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationAliases;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationChangeLog;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationTraffic;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationsByCountry;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationsByHost;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.DestinationsByUser;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingPolicy;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.Alias;
import ecmwf.ecpds.master.plugin.http.model.transfer.ChangeLog;
import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.Traffic;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.model.users.User;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;
import ecmwf.web.util.bean.Pair;

/**
 * The Class DestinationHome.
 */
public class DestinationHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DestinationHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = Destination.class.getName();

    /**
     * Creates the.
     *
     * @return the destination
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Destination create() throws TransferException {
        try {
            return (Destination) DAOService.create(INTERFACE);
        } catch (final DAOException e) {
            log.error("Error creating object", e);
            throw new TransferException("Error creating object", e);
        }
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the destination
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Destination findByPrimaryKey(final String key) throws TransferException {
        try {
            return (Destination) DAOService.findByPrimaryKey(INTERFACE, key, false);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new TransferException("Error retrieving object by key", e);
        }
    }

    /**
     * Find associated to incoming user.
     *
     * @param u
     *            the u
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findAssociatedToIncomingUser(final IncomingUser u)
            throws TransferException {
        return find(new SearchByIncomingUser(u.getId()));
    }

    /**
     * Find associated to incoming policy.
     *
     * @param p
     *            the p
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findAssociatedToIncomingPolicy(final IncomingPolicy p)
            throws TransferException {
        return find(new SearchByIncomingPolicy(p.getId()));
    }

    /**
     * Find by country.
     *
     * @param country
     *            the country
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findByCountry(final Country country) throws TransferException {
        return find(new DestinationsByCountry(country));
    }

    /**
     * Find by host.
     *
     * @param host
     *            the host
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findByHost(final Host host) throws TransferException {
        return find(new DestinationsByHost(host));
    }

    /**
     * Find by user.
     *
     * @param user
     *            the user
     * @param search
     *            the search
     * @param fromToAliases
     *            the from to aliases
     * @param asc
     *            the asc
     * @param status
     *            the status
     * @param type
     *            the type
     * @param filter
     *            the filter
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findByUser(final User user, final String search,
            final String fromToAliases, final boolean asc, final String status, final String type, final String filter)
            throws TransferException {
        return find(new DestinationsByUser(user, search, fromToAliases, asc, status, type, filter));
    }

    /**
     * Find alias list.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Alias> findAliasList(final Destination d) throws TransferException {
        return find(new DestinationAliases(d, true));
    }

    /**
     * Find aliases.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findAliases(final Destination d) throws TransferException {
        return find(new DestinationAliases(d, false));
    }

    /**
     * Find traffic list.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Traffic> findTrafficList(final Destination d) throws TransferException {
        return find(new DestinationTraffic(d));
    }

    /**
     * Find change log list.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<ChangeLog> findChangeLogList(final Destination d) throws TransferException {
        return find(new DestinationChangeLog(d));
    }

    /**
     * Find aliased from.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findAliasedFrom(final Destination d) throws TransferException {
        return find(new DestinationAliasedFrom(d));
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findAll() throws TransferException {
        return find(getDefaultSearch(""));
    }

    /**
     * Find all names and comments.
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Pair> findAllNamesAndComments() throws TransferException {
        return find(getDefaultSearch("NamesAndComments"));
    }

    /**
     * Find all showing in monitor.
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Destination> findAllShowingInMonitor() throws TransferException {
        return find(getDefaultSearch("ShowingInMonitor"));
    }

    /**
     * Find.
     *
     * @param search
     *            the search
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection find(final ModelSearch search) throws TransferException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new TransferException("Error retrieving objects", e);
        }
    }

    /**
     * Gets the default search.
     *
     * @param s
     *            the s
     *
     * @return the default search
     */
    public static final ModelSearch getDefaultSearch(final String s) {
        final var search = ModelHomeBase.getDefaultSearch(s);
        search.setCacheable(false);
        return search;
    }
}
