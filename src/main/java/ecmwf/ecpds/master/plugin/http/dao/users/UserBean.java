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

package ecmwf.ecpds.master.plugin.http.dao.users;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * PDS User implementation.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseObject;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.home.users.CategoryHome;
import ecmwf.web.home.users.ResourceHome;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.Resource;
import ecmwf.web.model.users.User;
import ecmwf.web.model.users.UserData;
import ecmwf.web.model.users.UserException;

/**
 * The Class UserBean.
 */
public class UserBean extends ModelBeanBase implements WebUser, OjbImplementedBean {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(UserBean.class);

    /** The user. */
    private final ecmwf.common.database.WebUser user;

    /** The categories. */
    private Collection<?> categories;

    /** The credentials. */
    private Object credentials = "";

    /** The added categories. */
    private final Collection<Category> addedCategories = new ArrayList<>();

    /** The deleted categories. */
    private final Collection<Category> deletedCategories = new ArrayList<>();

    /**
     * Instantiates a new user bean.
     *
     * @param user
     *            the user
     */
    protected UserBean(final ecmwf.common.database.WebUser user) {
        this.user = user;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return User.class.getName();
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return user;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return user.getId();
    }

    /**
     * Gets the uid.
     *
     * @return the uid
     */
    @Override
    public String getUid() {
        return user.getId();
    }

    /**
     * Gets the remote user.
     *
     * @return the remote user
     */
    @Override
    public String getRemoteUser() {
        return user.getId();
    }

    /**
     * Sets the remote user.
     *
     * @param v
     *            the new remote user
     */
    @Override
    public void setRemoteUser(final String v) {
    }

    /**
     * Gets the common name.
     *
     * @return the common name
     */
    @Override
    public String getCommonName() {
        return user.getName();
    }

    /**
     * Gets the friendly name.
     *
     * @return the friendly name
     */
    @Override
    public String getFriendlyName() {
        return getCommonName();
    }

    /**
     * Gets the category.
     *
     * @return the category
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public Category getCategory() throws UserException {
        throw new UserException("Use 'getCategories()' with this implementation");
    }

    /**
     * Gets the categories.
     *
     * @return the categories
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public Collection<?> getCategories() throws UserException {
        return categories != null ? categories
                : (categories = CategoryHome.find(ModelHomeBase.getDefaultSearch("user=\"" + getId() + "\"")));
    }

    /**
     * Gets the authentication method.
     *
     * @return the authentication method
     */
    @Override
    public String getAuthenticationMethod() {
        return "PASSWORD";
    }

    /**
     * Sets the authentication method.
     *
     * @param authenticationMethod
     *            the new authentication method
     */
    @Override
    public void setAuthenticationMethod(final int authenticationMethod) {

    }

    /**
     * Sets the user data.
     *
     * @param data
     *            the new user data
     */
    @Override
    public void setUserData(final UserData data) {
    }

    /**
     * Gets the user data.
     *
     * @return the user data
     */
    @Override
    public UserData getUserData() {
        if (user.getEnvironment() != null) {
            return new UserDataBean(user.getEnvironment());
        }
        return new UserDataBean("");
    }

    /**
     * Checks for access.
     *
     * @param path
     *            the path
     *
     * @return true, if successful
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public boolean hasAccess(final String path) throws UserException {
        return hasAccess(ResourceHome.findByURI(path));
    }

    /**
     * Checks for access.
     *
     * @param host
     *            the host
     * @param path
     *            the path
     *
     * @return true, if successful
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public boolean hasAccess(final String host, final String path) throws UserException {
        return hasAccess(path);
    }

    /**
     * Checks for access.
     *
     * @param r
     *            the r
     *
     * @return true, if successful
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public boolean hasAccess(final Resource r) throws UserException {
        if (log.isDebugEnabled()) {
            log.debug("Checking access for User '" + getUid() + "' to Resource '" + r.getPath() + "'");
        }
        final Collection<?> uCats = getCategories();
        final Collection<?> rCats = r.getCategories();
        for (Object uCat : uCats) {
            if (rCats.contains(uCat)) {
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("User '" + getUid() + "' (Categories: " + uCats + ") can't access Resource '" + r.getPath()
                    + "' (Categories: " + rCats + ")");
        }
        return false;
    }

    /**
     * Sets the user data.
     *
     * @param s
     *            the new user data
     */
    @Override
    public void setUserData(final String s) {
        user.setEnvironment(s);
    }

    /**
     * Gets the allowed packages.
     *
     * @return the allowed packages
     */
    public Collection<?> getAllowedPackages() {
        return new ArrayList<>(0);
    }

    /**
     * Gets the credentials.
     *
     * @return the credentials
     */
    @Override
    public Object getCredentials() {
        return credentials;
    }

    /**
     * Sets the credentials.
     *
     * @param o
     *            the new credentials
     */
    @Override
    public void setCredentials(final Object o) {
        credentials = o;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Sets the password.
     *
     * @param p
     *            the new password
     */
    @Override
    public void setPassword(final String p) {
        user.setPassword(p);
    }

    /**
     * Gets the last credential check.
     *
     * @return the last credential check
     */
    @Override
    public Date getLastCredentialCheck() {
        return user.getLastLogin();
    }

    /**
     * Sets the last credential check.
     *
     * @param d
     *            the new last credential check
     */
    @Override
    public void setLastCredentialCheck(final Date d) {
        user.setLastLogin(new Timestamp(d.getTime()));
    }

    /**
     * Compare to.
     *
     * @param o
     *            the o
     *
     * @return the int
     */
    @Override
    public int compareTo(final Object o) {
        return o instanceof final UserBean userBean ? user.getId().compareTo(userBean.user.getId()) : -1;
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final UserBean userBean && user.getId().equals(userBean.user.getId());
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return user.getId().hashCode();
    }

    /**
     * Gets the duplicate.
     *
     * @return the duplicate
     */
    @Override
    public User getDuplicate() {
        return null;
    }

    /**
     * Sets the as fallback public.
     */
    @Override
    public void setAsFallbackPublic() {
    }

    /**
     * Checks if is anonymous.
     *
     * @return true, if is anonymous
     */
    @Override
    public boolean isAnonymous() {
        return false;
    }

    /**
     * Dump.
     *
     * @param format
     *            the format
     *
     * @return the string
     */
    @Override
    public String dump(final String format) {
        return toString();
    }

    /**
     * Sets the common name.
     *
     * @param name
     *            the new common name
     */
    @Override
    public void setCommonName(final String name) {
        user.setName(name);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return user.getId();
    }

    /**
     * Sets the uid.
     *
     * @param uid
     *            the new uid
     */
    @Override
    public void setUid(final String uid) {
        user.setId(uid);
    }

    /**
     * Delete category.
     *
     * @param cat
     *            the cat
     */
    @Override
    public void deleteCategory(final Category cat) {
        deletedCategories.add(cat);
    }

    /**
     * Adds the category.
     *
     * @param cat
     *            the cat
     */
    @Override
    public void addCategory(final Category cat) {
        addedCategories.add(cat);
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    @Override
    public boolean getActive() {
        return user.getActive();
    }

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    @Override
    public void setActive(final boolean b) {
        user.setActive(b);
    }

    /**
     * Gets the added categories.
     *
     * @return the added categories
     */
    protected Collection<Category> getAddedCategories() {
        return addedCategories;
    }

    /**
     * Gets the deleted categories.
     *
     * @return the deleted categories
     */
    protected Collection<Category> getDeletedCategories() {
        return deletedCategories;
    }

    /**
     * Can read.
     *
     * @param r
     *            the r
     *
     * @return true, if successful
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public boolean canRead(final Resource r) throws UserException {
        return false;
    }

    /**
     * Can write.
     *
     * @param r
     *            the r
     *
     * @return true, if successful
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public boolean canWrite(final Resource r) throws UserException {
        return false;
    }

    /**
     * Gets the policies.
     *
     * @return the policies
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public Collection<?> getPolicies() throws UserException {
        return null;
    }

    /**
     * Checks for policy.
     *
     * @param name
     *            the name
     *
     * @return true, if successful
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public boolean hasPolicy(final String name) throws UserException {
        return false;
    }
}
