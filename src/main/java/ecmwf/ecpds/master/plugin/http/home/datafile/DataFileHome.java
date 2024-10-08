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
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.ecpds.master.plugin.http.home.datafile.searches.DataFilesByMetaDataAndDate;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class DataFileHome.
 */
public class DataFileHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DataFileHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = DataFile.class.getName();

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the data file
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final DataFile findByPrimaryKey(final String key) throws DataFileException {
        try {
            return (DataFile) DAOService.findByPrimaryKey(INTERFACE, key);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new DataFileException("Error retrieving object by key", e);
        }
    }

    /**
     * Find by meta data and date.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param date
     *            the date
     * @param cursor
     *            the cursor
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<DataFile> findByMetaDataAndDate(final String name, final String value,
            final Date date, final DataBaseCursor cursor) throws DataFileException {
        return find(new DataFilesByMetaDataAndDate(name, value, date, cursor));
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
    public static final Collection<DataFile> find(final ModelSearch search) throws DataFileException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new DataFileException("Error retrieving objects", e);
        }
    }
}
