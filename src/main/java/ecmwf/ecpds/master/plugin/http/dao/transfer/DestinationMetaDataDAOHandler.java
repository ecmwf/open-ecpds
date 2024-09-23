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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ecmwf.common.ecaccess.FileListElement;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DestinationMetaData;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.web.dao.ReadOnlyDAOHandler;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.config.ConfigService;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class DestinationMetaDataDAOHandler.
 */
public class DestinationMetaDataDAOHandler extends ReadOnlyDAOHandler implements DAOHandler {

    /**
     * {@inheritDoc}
     *
     * Creates the.
     */
    @Override
    public ModelBean create() throws DAOException {
        throw new DAOException("Not Implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Find by primary key.
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        throw new DAOException("Not Implemented");
    }

    /**
     * {@inheritDoc}
     *
     * Find.
     */
    @Override
    public Collection<DestinationMetaData> find(final ModelSearch search) throws DAOException {
        try {
            final var id = search.getQuery();
            final var d = DestinationHome.findByPrimaryKey(id);
            final var ai = MasterManager.getAI();
            final var des = (ecmwf.common.database.Destination) ((ecmwf.common.database.Destination) ((OjbImplementedBean) d)
                    .getOjbImplementation()).clone();
            var key = des.getName();
            var files = ai.list(key, "");
            if (files.length == 0) { // Let's see if there is some MetaData for the type!
                key = DestinationOption.getLabel(des.getType());
                files = ai.list(key, ""); // Any files?
            }
            final List<DestinationMetaData> results = new ArrayList<>(files.length);
            for (final FileListElement file : files) {
                results.add(new DestinationMetaDataBean(key, file, getContentType(file)));
            }
            return results;
        } catch (final TransferException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        } catch (final RemoteException e) {
            throw new DAOException("Master problem with search '" + search.getKey() + "'", e);
        } catch (final Exception e) {
            throw new DAOException("Problem with search '" + search.getKey() + "'", e);
        }
    }

    /**
     * Gets the content type.
     *
     * @param f
     *            the f
     *
     * @return the content type
     */
    private static final String getContentType(final FileListElement f) {
        final var lastDot = f.getName().lastIndexOf(".");
        if (lastDot < 0) {
            return DestinationMetaDataBean.DEFAULT_CONTENT_TYPE;
        }
        final var extension = f.getName().substring(f.getName().lastIndexOf("."));
        return ConfigService.getOptionalParameter("uploads", "type" + extension,
                DestinationMetaDataBean.DEFAULT_CONTENT_TYPE);
    }
}
