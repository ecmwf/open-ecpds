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
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingPolicy;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUserException;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class IncomingUserHome.
 */
public class IncomingUserHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(IncomingUserHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = IncomingUser.class.getName();

    /**
     * Creates the.
     *
     * @return the incoming user
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUserException
     *             the incoming user exception
     */
    public static final IncomingUser create() throws IncomingUserException {
        try {
            return (IncomingUser) DAOService.create(INTERFACE);
        } catch (final DAOException e) {
            log.error("Error creating object", e);
            throw new IncomingUserException("Error creating object", e);
        }
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the incoming user
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUserException
     *             the incoming user exception
     */
    public static final IncomingUser findByPrimaryKey(final String key) throws IncomingUserException {
        try {
            return (IncomingUser) DAOService.findByPrimaryKey(INTERFACE, key, false);
        } catch (final DAOException e) {
            log.error("Error creating object", e);
            throw new IncomingUserException("Error creating object", e);
        }
    }

    /**
     * Find associated to incoming policy.
     *
     * @param p
     *            the p
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUserException
     *             the incoming user exception
     */
    public static final Collection<IncomingUser> findAssociatedToIncomingPolicy(final IncomingPolicy p)
            throws IncomingUserException {
        return find(new SearchByIncomingPolicy(p.getId()));
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUserException
     *             the incoming user exception
     */
    public static final Collection<IncomingUser> findAll() throws IncomingUserException {
        return find(getDefaultSearch(""));
    }

    /**
     * Find.
     *
     * @param search
     *            the search
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUserException
     *             the incoming user exception
     */
    public static final Collection<IncomingUser> find(final ModelSearch search) throws IncomingUserException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new IncomingUserException("Error retrieving objects", e);
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
