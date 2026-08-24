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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import ecmwf.common.database.ApiPermission;
import ecmwf.common.database.DataBaseObject;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.home.transfer.CountryHome;
import ecmwf.ecpds.master.plugin.http.model.ecuser.ApiClient;
import ecmwf.ecpds.master.plugin.http.model.ecuser.ApiClientException;
import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.model.users.User;

public class ApiClientBean extends ModelBeanBase implements ApiClient, OjbImplementedBean {

    private final ecmwf.common.database.ApiClient apiClient;

    public ApiClientBean(final ecmwf.common.database.ApiClient c) {
        this.apiClient = c;
    }

    @Override
    public DataBaseObject getOjbImplementation() {
        return apiClient;
    }

    @Override
    public String getBeanInterfaceName() {
        return ApiClient.class.getName();
    }

    @Override
    public String getId() {
        return apiClient.getId();
    }

    @Override
    public void setId(final String id) {
        apiClient.setId(id);
    }

    @Override
    public String getComment() {
        return apiClient.getComment();
    }

    @Override
    public void setComment(final String comment) {
        apiClient.setComment(comment);
    }

    @Override
    public boolean getActive() {
        return apiClient.getActive();
    }

    @Override
    public void setActive(final boolean active) {
        apiClient.setActive(active);
    }

    @Override
    public Date getCreated() {
        return apiClient.getCreated();
    }

    @Override
    public Date getLastUsed() {
        return apiClient.getLastUsed();
    }

    @Override
    public String getLastUsedHost() {
        return apiClient.getLastUsedHost();
    }

    @Override
    public String getCountryIso() {
        return apiClient.getCountryIso();
    }

    @Override
    public void setCountryIso(final String countryIso) {
        apiClient.setCountryIso(countryIso);
    }

    @Override
    public Country getCountry() throws TransferException {
        final var iso = getCountryIso();
        if (iso == null || iso.isBlank()) {
            return null;
        }
        return CountryHome.findByPrimaryKey(iso);
    }

    @Override
    public Collection<ApiPermission> getPermissions() {
        try {
            final ApiPermission[] perms = MasterManager.getDB().getApiPermissionsForClient(getId());
            return Arrays.asList(perms);
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void insert(final User u) throws ApiClientException {
        try {
            ecmwf.ecpds.master.plugin.http.home.ecuser.ApiClientHome.insert(this, u);
        } catch (final Exception e) {
            throw new ApiClientException("Error inserting ApiClient", e);
        }
    }

    @Override
    public void delete(final User u) throws ApiClientException {
        try {
            ecmwf.ecpds.master.plugin.http.home.ecuser.ApiClientHome.delete(this, u);
        } catch (final Exception e) {
            throw new ApiClientException("Error deleting ApiClient", e);
        }
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof ApiClientBean b && getId().equals(b.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + " { " + apiClient + " }";
    }
}
