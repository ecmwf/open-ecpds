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
 * ECMWF Product Data Store (OpenECPDS) Project
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
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return User.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return user.getId();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the uid.
     */
    @Override
    public String getUid() {
        return user.getId();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the remote user.
     */
    @Override
    public String getRemoteUser() {
        return user.getId();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the remote user.
     */
    @Override
    public void setRemoteUser(final String v) {
    }

    /**
     * {@inheritDoc}
     *
     * Gets the common name.
     */
    @Override
    public String getCommonName() {
        return user.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the friendly name.
     */
    @Override
    public String getFriendlyName() {
        return getCommonName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the category.
     */
    @Override
    public Category getCategory() throws UserException {
        throw new UserException("Use 'getCategories()' with this implementation");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the categories.
     */
    @Override
    public Collection<?> getCategories() throws UserException {
        return categories != null ? categories
                : (categories = CategoryHome.find(ModelHomeBase.getDefaultSearch("user=\"" + getId() + "\"")));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the authentication method.
     */
    @Override
    public String getAuthenticationMethod() {
        return "PASSWORD";
    }

    /**
     * {@inheritDoc}
     *
     * Sets the authentication method.
     */
    @Override
    public void setAuthenticationMethod(final int authenticationMethod) {

    }

    /**
     * {@inheritDoc}
     *
     * Sets the user data.
     */
    @Override
    public void setUserData(final UserData data) {
    }

    /**
     * {@inheritDoc}
     *
     * Gets the user data.
     */
    @Override
    public UserData getUserData() {
        if (user.getEnvironment() != null) {
            return new UserDataBean(user.getEnvironment());
        }
        return new UserDataBean("");
    }

    /**
     * {@inheritDoc}
     *
     * Checks for access.
     */
    @Override
    public boolean hasAccess(final String path) throws UserException {
        return hasAccess(ResourceHome.findByURI(path));
    }

    /**
     * {@inheritDoc}
     *
     * Checks for access.
     */
    @Override
    public boolean hasAccess(final String host, final String path) throws UserException {
        return hasAccess(path);
    }

    /**
     * {@inheritDoc}
     *
     * Checks for access.
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
     * {@inheritDoc}
     *
     * Sets the user data.
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
     * {@inheritDoc}
     *
     * Gets the credentials.
     */
    @Override
    public Object getCredentials() {
        return credentials;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the credentials.
     */
    @Override
    public void setCredentials(final Object o) {
        credentials = o;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the password.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the password.
     */
    @Override
    public void setPassword(final String p) {
        user.setPassword(p);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the last credential check.
     */
    @Override
    public Date getLastCredentialCheck() {
        return user.getLastLogin();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the last credential check.
     */
    @Override
    public void setLastCredentialCheck(final Date d) {
        user.setLastLogin(new Timestamp(d.getTime()));
    }

    /**
     * {@inheritDoc}
     *
     * Compare to.
     */
    @Override
    public int compareTo(final Object o) {
        return o instanceof final UserBean userBean ? user.getId().compareTo(userBean.user.getId()) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final UserBean userBean && user.getId().equals(userBean.user.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return user.getId().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the duplicate.
     */
    @Override
    public User getDuplicate() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the as fallback public.
     */
    @Override
    public void setAsFallbackPublic() {
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is anonymous.
     */
    @Override
    public boolean isAnonymous() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Dump.
     */
    @Override
    public String dump(final String format) {
        return toString();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the common name.
     */
    @Override
    public void setCommonName(final String name) {
        user.setName(name);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return user.getId();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the uid.
     */
    @Override
    public void setUid(final String uid) {
        user.setId(uid);
    }

    /**
     * {@inheritDoc}
     *
     * Delete category.
     */
    @Override
    public void deleteCategory(final Category cat) {
        deletedCategories.add(cat);
    }

    /**
     * {@inheritDoc}
     *
     * Adds the category.
     */
    @Override
    public void addCategory(final Category cat) {
        addedCategories.add(cat);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the active.
     */
    @Override
    public boolean getActive() {
        return user.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
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
     * {@inheritDoc}
     *
     * Can read.
     */
    @Override
    public boolean canRead(final Resource r) throws UserException {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Can write.
     */
    @Override
    public boolean canWrite(final Resource r) throws UserException {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the policies.
     */
    @Override
    public Collection<?> getPolicies() throws UserException {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Checks for policy.
     */
    @Override
    public boolean hasPolicy(final String name) throws UserException {
        return false;
    }
}
