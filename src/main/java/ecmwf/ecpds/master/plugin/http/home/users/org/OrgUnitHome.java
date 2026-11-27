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

package ecmwf.ecpds.master.plugin.http.home.users.org;

/**
 * @author sy8
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

import java.util.Collection;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgPersonException;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgUnit;

public class OrgUnitHome extends ModelHomeBase {

    public static OrgUnit create() throws OrgPersonException {

        try {
            return (OrgUnit) DAOService.create("ecmwf.ecpds.master.plugin.http.model.users.org.OrgUnit");
        } catch (DAOException e) {
            throw new OrgPersonException("(OrgUnitHome) Error creating object: ", e);
        }

    }

    public static OrgUnit findByPrimaryKey(String key) throws OrgPersonException {

        try {
            // Get the person Bean USING the cache.
            return (OrgUnit) DAOService.findByPrimaryKey("ecmwf.ecpds.master.plugin.http.model.users.org.OrgUnit", key,
                    false);
        } catch (DAOException e) {
            throw new OrgPersonException("(OrgUnitHome) Error retrieving object set: ", e);
        }

    }

    public static Collection find(ModelSearch search) throws OrgPersonException {

        try {
            search.setStoreSearchResults(false);
            return DAOService.find("ecmwf.ecpds.master.plugin.http.model.users.org.OrgUnit", search);
        } catch (DAOException e) {
            throw new OrgPersonException("(OrgUnitHome) Error retrieving object set: ", e);
        }

    }

}
