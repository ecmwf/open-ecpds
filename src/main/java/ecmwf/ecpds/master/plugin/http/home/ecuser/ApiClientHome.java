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

package ecmwf.ecpds.master.plugin.http.home.ecuser;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.model.ecuser.ApiClient;
import ecmwf.ecpds.master.plugin.http.model.ecuser.ApiClientException;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.model.ModelSearch;
import ecmwf.web.model.users.User;
import ecmwf.web.services.persistence.DAOException;
import ecmwf.web.services.persistence.DAOService;

public class ApiClientHome extends ModelHomeBase {

    private static final Logger log = LogManager.getLogger(ApiClientHome.class);
    private static final String INTERFACE = ApiClient.class.getName();

    public static ApiClient create() throws ApiClientException {
        try {
            return (ApiClient) DAOService.create(INTERFACE);
        } catch (final DAOException e) {
            log.error("Error creating ApiClient", e);
            throw new ApiClientException("Error creating ApiClient", e);
        }
    }

    public static ApiClient findByPrimaryKey(final String key) throws ApiClientException {
        try {
            return (ApiClient) DAOService.findByPrimaryKey(INTERFACE, key, false);
        } catch (final DAOException e) {
            log.error("Error finding ApiClient by key: " + key, e);
            throw new ApiClientException("Error finding ApiClient", e);
        }
    }

    public static Collection<ApiClient> findAll() throws ApiClientException {
        return find(getDefaultSearch(""));
    }

    public static Collection<ApiClient> find(final ModelSearch search) throws ApiClientException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (final DAOException e) {
            log.error("Error retrieving ApiClients", e);
            throw new ApiClientException("Error retrieving ApiClients", e);
        }
    }

    public static void insert(final ApiClient client, final User user) throws ApiClientException {
        try {
            DAOService.save(client, user);
        } catch (final DAOException e) {
            throw new ApiClientException("Error inserting ApiClient", e);
        }
    }

    public static void delete(final ApiClient client, final User user) throws ApiClientException {
        try {
            DAOService.delete(client, user);
        } catch (final DAOException e) {
            throw new ApiClientException("Error deleting ApiClient", e);
        }
    }

    public static ModelSearch getDefaultSearch(final String s) {
        final var search = ModelHomeBase.getDefaultSearch(s);
        search.setCacheable(false);
        return search;
    }
}
