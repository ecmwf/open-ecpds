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

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * This is an extention to the generic ecmwf.web.model.users.User interface to
 * enable setting of properties. The generic one doesn't provide it because its
 * instances are supposed to be handled only by an authentication external
 * method.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.User;

/**
 * The Interface WebUser.
 */
public interface WebUser extends User {

    /**
     * {@inheritDoc}
     *
     * Sets the id.
     */
    @Override
    void setId(String id);

    /**
     * Sets the uid.
     *
     * @param id
     *            the new uid
     */
    void setUid(String id);

    /**
     * Sets the password.
     *
     * @param password
     *            the new password
     */
    void setPassword(String password);

    /**
     * Sets the common name.
     *
     * @param id
     *            the new common name
     */
    void setCommonName(String id);

    /**
     * Sets the user data.
     *
     * @param s
     *            the new user data
     */
    void setUserData(String s);

    /**
     * Adds the category.
     *
     * @param c
     *            the c
     */
    void addCategory(Category c);

    /**
     * Delete category.
     *
     * @param c
     *            the c
     */
    void deleteCategory(Category c);

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    void setActive(boolean b);
}
