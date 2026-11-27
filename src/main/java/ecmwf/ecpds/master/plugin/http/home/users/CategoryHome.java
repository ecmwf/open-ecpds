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

/**
 * Home interface for Category.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 1.0
 */

import java.util.Collection;

//import ecmwf.ecpds.master.plugin.http.services.log.LogService;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.users.Category;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;

public class CategoryHome extends ModelHomeBase {

    private static final String INTERFACE = "ecmwf.ecpds.master.plugin.http.model.users.Category";

    public static Category create() throws UserException {

        try {
            return (Category) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new UserException("(CategoryHome) Error creating object: ", e);
        }

    }

    public static Category findByPrimaryKey(String key) throws UserException {

        try {
            return (Category) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOException e) {
            throw new UserException("(CategoryHome) Error retrieving object set: ", e);
        }

    }

    public static Collection findAll() throws UserException {
        return find(getDefaultSearch(""));
    }

    public static Collection find(ModelSearch search) throws UserException {

        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new UserException("(CategoryHome) Error retrieving object set: ", e);
        }

    }

} // WebProfileHome
