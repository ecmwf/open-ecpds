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
 * Home interface for User
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 1.0
 */

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.users.User;
import ecmwf.ecpds.master.plugin.http.model.users.UserException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;

public class UserHome extends ModelHomeBase {

    private static final String INTERFACE = User.class.getName();
    public static final String ALL = "All";

    public static User create() throws UserException {

        try {
            return (User) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new UserException("(UserHome) Error creating object: ", e);
        }

    }
    /*
     * public static User createPublic() throws UserException {
     *
     * try { return (User) DAOService.create(INTERFACE); } catch (DAOException e) { throw new
     * UserException("(UserHome) Error creating object: ", e); }
     *
     * }
     */

    /**
     * Static primary key finder. As in all other beans.
     *
     * @param key
     *            The uid to search in the database.
     *
     * @throws WebUserException
     *             If any problem happens with the DB or a user with that id has not been found.
     */

    public static User findByPrimaryKey(String key) throws UserException {

        try {
            return (User) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOException e) {
            throw new UserException("Problem retrieving User with key '" + key + "'", e);
        }

    }

    public static User findByUidAndPass(String uid, String pass, String host, String userAgent, String comment)
            throws UserException {

        try {
            UserByUidAndPass search = new UserByUidAndPass(uid, pass, host, userAgent, comment);
            Collection c = DAOService.find(INTERFACE, search);
            if (c != null && c.size() == 1)
                return (User) c.iterator().next();
            else
                throw new DAOException("Expected ONE user as a result of a findByUidAndPass: " + c);
        } catch (DAOException e) {
            throw new UserException("Error retrieving object: ", e);
        }

    }

    /**
     * Static QUERY finder. As in all other beans.
     *
     * @param search
     *            The search object to use.
     *
     * @throws WebUserException
     *             If any problem happens with the DB. Empty results are valid.
     */

    public static Collection find(ModelSearch search) throws UserException {

        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new UserException("(UserHome) Error retrieving object set: ", e);
        }

    }

    public static Collection findAll() throws UserException {
        return find(getDefaultSearch(ALL));
    }

} // WebUserHome
