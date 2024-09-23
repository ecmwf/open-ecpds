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

package ecmwf.ecpds.master.plugin.http.controller.user.user;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.home.users.CategoryHome;
import ecmwf.web.model.users.User;
import ecmwf.web.model.users.UserException;

/**
 * The Class UserActionForm.
 */
public class UserActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1527949956717872885L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(UserActionForm.class);

    /** The id. */
    private String id = "";

    /** The uid. */
    private String uid = "";

    /** The name. */
    private String name = "";

    /** The user data. */
    private String userData = "";

    /** The active. */
    private String active = "off";

    /** The password. */
    private String password = "";

    /** The user. */
    private User user = null;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the uid.
     *
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public String getActive() {
        return active;
    }

    /**
     * Sets the id.
     *
     * @param string
     *            the new id
     */
    public void setId(final String string) {
        id = string;
    }

    /**
     * Sets the password.
     *
     * @param string
     *            the new password
     */
    public void setPassword(final String string) {
        password = string;
    }

    /**
     * Sets the name.
     *
     * @param string
     *            the new name
     */
    public void setName(final String string) {
        name = string;
    }

    /**
     * Sets the uid.
     *
     * @param string
     *            the new uid
     */
    public void setUid(final String string) {
        uid = string;
    }

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Sets the active.
     *
     * @param s
     *            the new active
     */
    public void setActive(final String s) {
        this.active = s;
    }

    /**
     * Sets the user data.
     *
     * @param s
     *            the new user data
     */
    public void setUserData(final String s) {
        this.userData = s;
    }

    /**
     * Gets the user data.
     *
     * @return the user data
     */
    public String getUserData() {
        return this.userData;
    }

    /**
     * Gets the categories.
     *
     * @return the categories
     */
    public Collection<?> getCategories() {
        try {
            if (user != null) {
                return user.getCategories();
            }
        } catch (final UserException e) {
            log.error("Problem getting Categories", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Gets the category options.
     *
     * @return the category options
     */
    public Collection<?> getCategoryOptions() {
        try {
            final Collection<?> all = CategoryHome.findAll();
            all.removeAll(getCategories());
            return all;
        } catch (final UserException e) {
            log.error("Problem getting Category options", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Populate user.
     *
     * @param u
     *            the u
     *
     * @throws ecmwf.web.controller.ECMWFActionFormException
     *             the ECMWF action form exception
     */
    protected void populateUser(final User u) throws ECMWFActionFormException {
        if (!(u instanceof final WebUser webUser)) {
            throw new ECMWFActionFormException(
                    "An User implementation '" + u.getClass().getName() + "' can NOT be edited");
        }
        webUser.setPassword(password);
        webUser.setId(id);
        webUser.setUid(uid);
        webUser.setCommonName(name);
        webUser.setUserData(this.userData);
        webUser.setActive("on".equalsIgnoreCase(active) || "true".equalsIgnoreCase(active));
    }

    /**
     * Populate from user.
     *
     * @param u
     *            the u
     */
    protected void populateFromUser(final User u) {
        this.user = u;
        this.setId(u.getId());
        this.setUid(u.getUid());
        this.setName(u.getCommonName());
        this.setUserData(u.getUserData().toString());
        this.setPassword(u.getPassword());
        if (u instanceof final WebUser webUser) {
            this.setActive(webUser.getActive() ? "on" : "off");
        }
    }
}
