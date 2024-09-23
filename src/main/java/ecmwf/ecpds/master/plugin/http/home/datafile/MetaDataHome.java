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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.home.datafile.searches.MetaDataValuesByAttributeName;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.MetaData;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

/**
 * The Class MetaDataHome.
 */
public class MetaDataHome extends ModelHomeBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(MetaDataHome.class);

    /** The Constant INTERFACE. */
    private static final String INTERFACE = MetaData.class.getName();

    /**
     * Creates the.
     *
     * @return the meta data
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final MetaData create() throws DataFileException {
        try {
            return (MetaData) DAOService.create(INTERFACE);
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
     * @return the meta data
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final MetaData findByPrimaryKey(final String key) throws DataFileException {
        try {
            return (MetaData) DAOService.findByPrimaryKey(INTERFACE, key);
        } catch (final DAOException e) {
            log.error("Error retrieving object by key", e);
            throw new DataFileException("Error retrieving object by key", e);
        }
    }

    /**
     * Find all meta data names.
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<MetaData> findAllMetaDataNames() throws DataFileException {
        return findAll();
    }

    /**
     * Find by data file.
     *
     * @param d
     *            the d
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<MetaData> findByDataFile(final DataFile d) throws DataFileException {
        return findByDataFileId(d.getId());
    }

    /**
     * Find by data file id.
     *
     * @param id
     *            the id
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<MetaData> findByDataFileId(final String id) throws DataFileException {
        return find(getDefaultSearch("datafile=\"" + id + "\""));
    }

    /**
     * Find by attribute name.
     *
     * @param attribute
     *            the attribute
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<MetaData> findByAttributeName(final String attribute) throws DataFileException {
        return find(new MetaDataValuesByAttributeName(attribute));
    }

    /**
     * Find all.
     *
     * @return the collection
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<MetaData> findAll() throws DataFileException {
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
     * @throws ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException
     *             the data file exception
     */
    public static final Collection<MetaData> find(final ModelSearch search) throws DataFileException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving objects", e);
            throw new DataFileException("Error retrieving objects", e);
        }
    }
}
