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
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.HostChangeLog;
import ecmwf.ecpds.master.plugin.http.home.transfer.searches.HostsSearch;
import ecmwf.ecpds.master.plugin.http.model.transfer.ChangeLog;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMethod;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;
import ecmwf.web.util.bean.Pair;

/**
 * The Class HostHome.
 */
public class HostHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(HostHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = Host.class.getName();

    /**
     * Creates the.
     *
     * @return the host
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Host create() throws TransferException {
        try {
            return (Host) DAOService.create(INTERFACE);
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
     * @return the host
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Host findByPrimaryKey(final String key) throws TransferException {
        try {
            return (Host) DAOService.findByPrimaryKey(INTERFACE, key, false);
        } catch (final DAOException e) {
            log.error("Error getting object", e);
            throw new TransferException("Error getting object", e);
        }
    }

    /**
     * Find change log list.
     *
     * @param h
     *            the h
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<ChangeLog> findChangeLogList(final Host h) throws TransferException {
        return find(new HostChangeLog(h));
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Host> findAll() throws TransferException {
        return find(getDefaultSearch(""));
    }

    /**
     * Find by criteria.
     *
     * @param label
     *            the label
     * @param filter
     *            the filter
     * @param network
     *            the network
     * @param type
     *            the type
     * @param search
     *            the search
     * @param cursor
     *            the cursor
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Host> findByCriteria(final String label, final String filter, final String network,
            final String type, final String search, final DataBaseCursor cursor) throws TransferException {
        return find(new HostsSearch(label, filter, network, type, search, cursor));
    }

    /**
     * Find with priority by destination.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Pair> findWithPriorityByDestination(final Destination d) throws TransferException {
        return find(getDefaultSearch("destination=\"" + d.getId() + "\" and priority=\"*\""));
    }

    /**
     * Find by destination.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Host> findByDestination(final Destination d) throws TransferException {
        return find(getDefaultSearch("destination=\"" + d.getId() + "\""));
    }

    /**
     * Find by transfer method.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws TransferException
     *             the transfer exception
     */
    public static final Collection<Host> findByTransferMethod(final TransferMethod d) throws TransferException {
        return find(getDefaultSearch("transferMethod=" + d.getId()));
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
