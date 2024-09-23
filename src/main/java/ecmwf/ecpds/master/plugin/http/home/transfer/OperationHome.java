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

import ecmwf.ecpds.master.plugin.http.home.transfer.searches.SearchByIncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.IncomingUser;
import ecmwf.ecpds.master.plugin.http.model.transfer.Operation;
import ecmwf.ecpds.master.plugin.http.model.transfer.OperationException;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class OperationHome.
 */
public class OperationHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(OperationHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = Operation.class.getName();

    /**
     * Creates the.
     *
     * @return the operation
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.OperationException
     *             the operation exception
     */
    public static final Operation create() throws OperationException {
        try {
            return (Operation) DAOService.create(INTERFACE);
        } catch (final DAOException e) {
            log.error("Error creating object", e);
            throw new OperationException("Error creating object", e);
        }
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the operation
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.OperationException
     *             the operation exception
     */
    public static final Operation findByPrimaryKey(final String key) throws OperationException {
        try {
            return (Operation) DAOService.findByPrimaryKey(INTERFACE, key, false);
        } catch (final DAOException e) {
            log.error("Error creating object", e);
            throw new OperationException("Error creating object", e);
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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.OperationException
     *             the operation exception
     */
    public static final Collection<Operation> findAssociatedToIncomingUser(final IncomingUser u)
            throws OperationException {
        return find(new SearchByIncomingUser(u.getId()));
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.OperationException
     *             the operation exception
     */
    public static final Collection<Operation> findAll() throws OperationException {
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
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.OperationException
     *             the operation exception
     */
    public static final Collection<Operation> find(final ModelSearch search) throws OperationException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new OperationException("Error retrieving objects", e);
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
