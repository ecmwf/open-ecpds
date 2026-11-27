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

package ecmwf.ecpds.master.plugin.http.home.users;

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.users.Resource;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;

public class ResourceHome extends ModelHomeBase {

    private static final String INTERFACE = Resource.class.getName();

    public static Resource create() throws UserException {
        try {
            return (Resource) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new UserException("(ResourceHome) Error creating object: ", e);
        }
    }

    private static Resource findByPrimaryKey(String key) throws UserException {

        try {
            return (Resource) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOException e) {
            throw new UserException("Error finding resource '" + key + "'", e);
        }

    }

    public static Resource findByURI(String s) throws UserException {
        return findByPrimaryKey(s);
    }

    public static Collection find(ModelSearch search) throws UserException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new UserException("(ResourceHome) Error retrieving object set: ", e);
        }
    }

    public static Collection findAll() throws UserException {
        return find(getDefaultSearch(""));
    }

}
