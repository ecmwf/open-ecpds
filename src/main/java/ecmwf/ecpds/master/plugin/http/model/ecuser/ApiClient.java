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

package ecmwf.ecpds.master.plugin.http.model.ecuser;

import java.util.Collection;
import java.util.Date;

import ecmwf.web.model.ModelBean;
import ecmwf.web.model.users.User;

public interface ApiClient extends ModelBean {
    @Override
    String getId();

    @Override
    void setId(String id);

    String getComment();

    void setComment(String comment);

    boolean getActive();

    void setActive(boolean active);

    Date getCreated();

    Date getLastUsed();

    String getLastUsedHost();

    String getCountryIso();

    void setCountryIso(String countryIso);

    ecmwf.ecpds.master.plugin.http.model.transfer.Country getCountry()
            throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;

    Collection<ecmwf.common.database.ApiPermission> getPermissions();

    void insert(User u) throws ecmwf.ecpds.master.plugin.http.model.ecuser.ApiClientException;

    void delete(User u) throws ecmwf.ecpds.master.plugin.http.model.ecuser.ApiClientException;
}
