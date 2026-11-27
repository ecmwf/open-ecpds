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

package ecmwf.ecpds.master.plugin.http.services.persistence;

import java.util.Collection;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;

/**
 * <p>
 * This interface represents the "contract" between a ModelBean and the class in charge of handling its persistence. In
 * order to separate "bussiness logic" from "data persistence", this mechanism has been abstracted, so that ModelBeans
 * use a DAOService call in order to get the appropiate DAOHandler to call when executing "storage" operations.
 * </p>
 * <p>
 * The mapping between ModelBeans and DAOHandlers is held by the "dao.properties" file and loaded on startup.
 * </p>
 *
 * Created: Wed Apr 24 09:33:56 2002
 *
 * @author <a href="mailto:dvarela@emcwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public interface DAOHandler {

    /**
     * Returns a new ModelBean
     *
     * @param key
     *            The primary key for the bean.
     *
     * @return An instance of the ModelBean implementation for the Bean managed by this DAOHandler.
     *
     * @throws DAOException
     *             If there is any problem with the creation
     */

    public ModelBean create() throws DAOException;

    /**
     * Returns the ModelBean whose primary key is the one supplied.
     *
     * @param key
     *            The primary key for the bean.
     *
     * @return An instance of the ModelBean implementation for the Bean managed by this DAOHandler.
     *
     * @throws DAOException
     *             If the primary key doesn't exist or if there's problems accesing the data source.
     */

    public ModelBean findByPrimaryKey(String key) throws DAOException;

    /**
     * Returns the ModelBeans which comply with a filter.
     *
     * @param query
     *            The query string to use as a filter. Its format may vary, depending of the kind of data source used
     *            and id data is cached or not. Many DAOHandlers which cache data use the BooleanExpresionCreator class
     *            to interprete and then evaluate query strings.
     *
     * @return A Collection containing instances of the ModelBean implementation for the Bean managed by this
     *         DAOHandler. It can be empty but not null.
     *
     * @throws DAOException
     *             If the query is incorrect or if there's problems accesing the data source.
     */

    /**
     * Returns the ModelBeans which comply with a filter, taking in consideration some options.
     *
     * @param query
     *            The query string to use as a filter.
     * @param options
     *            The options to apply to this search.
     *
     * @return A Collection containing instances of the ModelBean implementation for the Bean managed by this
     *         DAOHandler. It can be empty but not null.
     *
     * @throws DAOException
     *             If the query is incorrect or if there's problems accesing the data source.
     */

    public Collection find(ModelSearch search) throws DAOException;

    /**
     * Saves in a persistent storage the supplied bean.
     *
     * @param bean
     *            The bean to save.
     * @param context
     *            Some information (ie, user) about the context of this bean.
     *
     * @throws DAOException
     *             If the bean doesn't exist already in the storage (is has to be "inserted" previously to be "saved")
     *             or if any data problem happens. If using a DB all "constraint" problems, etc, will also throw this
     *             exception.
     */

    public void save(ModelBean bean, Object context) throws DAOException;

    /**
     * Create into a persistent storage the supplied bean.
     *
     * @param bean
     *            The bean to insert.
     * @param context
     *            Some information (ie, user) about the context of this bean.
     *
     * @throws DAOException
     *             If the bean already exists in the storage (then it should be "saved", not "inserted") or if any data
     *             problem happens. If using a DB all "constraint" problems, etc, will also throw this exception.
     */

    public void insert(ModelBean bean, Object context) throws DAOException;

    /**
     * Deletes from a persistent storage the supplied bean.
     *
     * @param bean
     *            The bean to delete.
     * @param context
     *            Some information (ie, user) about the context of this bean.
     *
     * @throws DAOException
     *             If the bean doesn't exist in the storage or if any data problem happens. If using a DB all
     *             "constraint" problems, etc, will also throw this exception.
     */

    public void delete(ModelBean bean, Object context) throws DAOException;

} // DAOHandler
