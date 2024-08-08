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
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import ecmwf.common.database.Category;
import ecmwf.common.database.DataBaseObject;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebCategory;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebResource;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.home.users.ResourceHome;
import ecmwf.web.home.users.UserHome;
import ecmwf.web.model.users.Resource;
import ecmwf.web.model.users.UserException;

/**
 * The Class CategoryBean.
 */
public class CategoryBean extends ModelBeanBase implements WebCategory, OjbImplementedBean {

    /** The added resources. */
    private final Collection<Resource> addedResources = new ArrayList<>();

    /** The deleted resources. */
    private final Collection<Resource> deletedResources = new ArrayList<>();

    /** The category. */
    private final Category category;

    /**
     * Instantiates a new category bean.
     *
     * @param category
     *            the category
     */
    public CategoryBean(final Category category) {
        this.category = category;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return ecmwf.web.model.users.Category.class.getName();
    }

    /**
     * Gets the ojb implementation.
     *
     * @return the ojb implementation
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return category;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return Long.toString(category.getId());
    }

    /**
     * Gets the int id.
     *
     * @return the int id
     */
    @Override
    public int getIntId() {
        return (int) category.getId();
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    @Override
    public void setId(final String id) {
        category.setId(Long.parseLong(id));
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return category.getName();
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return category.getDescription();
    }

    /**
     * Sets the description.
     *
     * @param d
     *            the new description
     */
    @Override
    public void setDescription(final String d) {
        category.setDescription(d);
    }

    /**
     * Sets the name.
     *
     * @param v
     *            the new name
     */
    @Override
    public void setName(final String v) {
        category.setName(v);
    }

    /**
     * Gets the realm.
     *
     * @return the realm
     */
    public String getRealm() {
        return category.getName();
    }

    /**
     * Checks for access.
     *
     * @param r
     *            the r
     *
     * @return true, if successful
     */
    @Override
    public boolean hasAccess(final Resource r) {
        return false;
    }

    /**
     * Gets the users with profile.
     *
     * @return the users with profile
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public Collection<WebUser> getUsersWithProfile() throws UserException {
        return UserHome.find(ModelHomeBase.getDefaultSearch("category=\"" + getId() + "\""));
    }

    /**
     * Gets the accessible resources.
     *
     * @return the accessible resources
     *
     * @throws UserException
     *             the user exception
     */
    @Override
    public Collection<WebResource> getAccessibleResources() throws UserException {
        return ResourceHome.find(ModelHomeBase.getDefaultSearch("category=\"" + getId() + "\""));
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
        return o instanceof final ecmwf.web.model.users.Category category && equals(category);
    }

    /**
     * Equals.
     *
     * @param c
     *            the c
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final ecmwf.web.model.users.Category c) {
        return getName().equals(c.getName());
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
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
        return category.getName();
    }

    /**
     * Delete resource.
     *
     * @param cat
     *            the cat
     */
    @Override
    public void deleteResource(final Resource cat) {
        deletedResources.add(cat);
    }

    /**
     * Adds the resource.
     *
     * @param res
     *            the res
     */
    @Override
    public void addResource(final Resource res) {
        addedResources.add(res);
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    @Override
    public boolean getActive() {
        return category.getActive();
    }

    /**
     * Sets the active.
     *
     * @param b
     *            the new active
     */
    @Override
    public void setActive(final boolean b) {
        category.setActive(b);
    }

    /**
     * Gets the added resources.
     *
     * @return the added resources
     */
    protected Collection<Resource> getAddedResources() {
        return addedResources;
    }

    /**
     * Gets the deleted resources.
     *
     * @return the deleted resources
     */
    protected Collection<Resource> getDeletedResources() {
        return deletedResources;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return getName();
    }
}
