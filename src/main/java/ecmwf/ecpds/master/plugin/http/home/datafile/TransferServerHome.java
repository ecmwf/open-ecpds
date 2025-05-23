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

package ecmwf.ecpds.master.plugin.http.home.datafile;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferGroup;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class TransferServerHome.
 */
public class TransferServerHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(TransferServerHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = TransferServer.class.getName();

    /**
     * Creates the.
     *
     * @return the transfer server
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final TransferServer create() throws DataFileException {
        try {
            return (TransferServer) DAOService.create(INTERFACE);
        } catch (final DAOException e) {
            log.error("Error creating object", e);
            throw new DataFileException("Error creating object", e);
        }
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the transfer server
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final TransferServer findByPrimaryKey(final String key) throws DataFileException {
        try {
            return (TransferServer) DAOService.findByPrimaryKey(INTERFACE, key);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new DataFileException("Error retrieving object by key", e);
        }
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<TransferServer> findAll() throws DataFileException {
        return find(getDefaultSearch(""));
    }

    /**
     * Find by data transfer.
     *
     * @param transfer
     *            the transfer
     *
     * @return the transfer server
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final TransferServer findByDataTransfer(final DataTransfer transfer) throws DataFileException {
        final var c = find(getDefaultSearch("dataTransfer=\"" + transfer.getId() + "\""));
        if (c.size() == 1) {
            return c.iterator().next();
        }
        throw new DataFileException("Result size for search not 1, but " + c.size());
    }

    /**
     * Find by transfer group.
     *
     * @param group
     *            the group
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<TransferServer> findByTransferGroup(final TransferGroup group)
            throws DataFileException {
        return find(getDefaultSearch("transferGroup=" + group.getId()));
    }

    /**
     * Find.
     *
     * @param search
     *            the search
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<TransferServer> find(final ModelSearch search) throws DataFileException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new DataFileException("Error retrieving objects", e);
        }
    }
}
