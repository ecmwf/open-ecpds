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

package ecmwf.ecpds.master.plugin.http.dao.ecuser;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ecmwf.common.database.DataBaseException;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.model.ecuser.ApiClient;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOHandler;

public class ApiClientDAOHandler extends PDSDAOBase implements DAOHandler {

    @Override
    public ApiClient create() throws DAOException {
        return new ApiClientBean(new ecmwf.common.database.ApiClient());
    }

    @Override
    public ApiClient findByPrimaryKey(final String key) throws DAOException {
        try {
            return new ApiClientBean(MasterManager.getDB().getApiClient(key));
        } catch (final DataBaseException | RemoteException e) {
            throw new DAOException("Problem searching by key '" + key + "'", e);
        }
    }

    @Override
    public Collection<ApiClient> find(final ModelSearch search) throws DAOException {
        try {
            if ("".equals(search.getQuery())) {
                return convertArray(MasterManager.getDB().getApiClientArray());
            }
            throw new DAOException("Unsupported search: " + search.getKey());
        } catch (final DataBaseException | RemoteException e) {
            throw new DAOException("DataBase problem with search '" + search.getKey() + "'", e);
        }
    }

    @Override
    public void save(final ModelBean b, final Object context) throws DAOException {
        try {
            super.save(b, context);
        } catch (final Exception e) {
            throw new DAOException("Error saving ApiClient", e);
        }
    }

    @Override
    public void delete(final ModelBean b, final Object context) throws DAOException {
        final var bean = (ApiClientBean) b;
        try {
            final var db = MasterManager.getDB();
            final var session = Util.getECpdsSessionFromObject(context);
            db.remove(session, (ecmwf.common.database.ApiClient) bean.getOjbImplementation());
        } catch (final Exception e) {
            throw new DAOException("Problem deleting ApiClient '" + bean.getId() + "'", e);
        }
    }

    private static Collection<ApiClient> convertArray(final ecmwf.common.database.ApiClient[] arr) {
        if (arr == null) {
            return Collections.emptyList();
        }
        final List<ApiClient> results = new ArrayList<>(arr.length);
        for (final ecmwf.common.database.ApiClient a : arr) {
            if (a != null) {
                results.add(new ApiClientBean(a));
            }
        }
        return results;
    }
}
