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

package ecmwf.ecpds.master.plugin.http.dao.users;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import ecmwf.web.dao.ReadOnlyDAOHandler;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

/**
 * The Class UserDataDAOHandler.
 */
public class UserDataDAOHandler extends ReadOnlyDAOHandler implements DAOHandler {

    /**
     * Creates the.
     *
     * @return the model bean
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public ModelBean create() throws DAOException {
        throw new DAOException("Not Implemented!");
    }

    /**
     * Find by primary key.
     *
     * @param key
     *            the key
     *
     * @return the model bean
     *
     * @throws DAOException
     *             the DAO exception
     */
    @Override
    public ModelBean findByPrimaryKey(final String key) throws DAOException {
        throw new DAOException("Not Implemented!");
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
    public Collection<?> find(final ModelSearch search) throws DAOException {
        throw new DAOException("Not Implemented!");
    }
}
