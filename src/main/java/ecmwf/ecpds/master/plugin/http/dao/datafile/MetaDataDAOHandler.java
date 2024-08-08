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

package ecmwf.ecpds.master.plugin.http.dao.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Persistence handler for DataTag.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.MetadataAttribute;
import ecmwf.common.database.MetadataValue;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.home.datafile.searches.MetaDataValuesByAttributeName;
import ecmwf.ecpds.master.plugin.http.model.datafile.MetaData;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;
import ecmwf.web.util.search.BooleanExpressionException;

/**
 * The Class MetaDataDAOHandler.
 */
public class MetaDataDAOHandler extends PDSDAOBase implements DAOHandler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(MetaDataDAOHandler.class);

    /**
     * Creates the.
     *
     * @return the meta data bean
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public MetaDataBean create() throws DAOException {
        throw new DAOException("Create metadata not implemented");
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the meta data
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public MetaData findByPrimaryKey(final String key) throws DAOException {
        try {
            return new MetaDataBean(MasterManager.getDB().getMetadataAttribute(key));
        } catch (DataBaseException | RemoteException e) {
            throw new DAOException("Error getting resource with key'" + key + "'", e);
        }
    }

    /**
     * Find.
     *
     * @param search
     *            the search
     *
     * @return the collection
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public Collection<MetaData> find(final ModelSearch search) throws DAOException {
        try {
            if (search instanceof final MetaDataValuesByAttributeName metaDataValuesByAttributeName) {
                return convertToModelBeanCollection(
                        MasterManager.getDB().getMetaDataByAttributeName(metaDataValuesByAttributeName.getName()));
            }
            if ("".equals(search.getQuery())) {
                return convertArrayToModelBeanCollection(MasterManager.getDB().getMetadataAttributeArray());
            } else {
                try {
                    final var clauses = getEqualityClauseLeavesFromExpression(search);
                    if (clauses.size() == 1) {
                        if (clauses.containsKey("datafile")) {
                            final var dataFileIdString = clauses.get("datafile");
                            long dataFileId;
                            try {
                                dataFileId = Long.parseLong(dataFileIdString);
                            } catch (final NumberFormatException e) {
                                throw new DAOException("Incorrect DataFileId: " + dataFileIdString);
                            }
                            return convertToModelBeanCollection(
                                    MasterManager.getDB().getMetaDataByDataFileId(dataFileId));
                        } else {
                            throw new DAOException("Search must have 'datafile' clause. '" + clauses + " 'invalid'");
                        }
                    } else {
                        throw new DAOException("Expecting a single search element");
                    }
                } catch (final BooleanExpressionException e) {
                    throw new DAOException("Bad find expression '" + search.getQuery() + "'", e);
                }
            }
        } catch (final Exception e) {
            log.warn("DataBase problem", e);
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * Convert array to model bean collection.
     *
     * @param attributes
     *            the attributes
     *
     * @return the collection
     */
    @SuppressWarnings("null")
    private static final Collection<MetaData> convertArrayToModelBeanCollection(final MetadataAttribute[] attributes) {
        final var length = attributes != null ? attributes.length : 0;
        final List<MetaData> results = new ArrayList<>(length);
        for (var i = 0; i < length; i++) {
            results.add(new MetaDataBean(attributes[i]));
        }
        return results;
    }

    /**
     * Convert to model bean collection.
     *
     * @param collection
     *            the collection
     *
     * @return the collection
     */
    private static final Collection<MetaData> convertToModelBeanCollection(final Collection<MetadataValue> collection) {
        final List<MetaData> results = new ArrayList<>(collection.size());
        for (final MetadataValue value : collection) {
            results.add(new MetaDataBean(value));
        }
        return results;
    }
}
