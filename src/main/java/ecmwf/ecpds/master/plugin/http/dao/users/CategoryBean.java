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
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
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
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return ecmwf.web.model.users.Category.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return category;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return Long.toString(category.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the int id.
     */
    @Override
    public int getIntId() {
        return (int) category.getId();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the id.
     */
    @Override
    public void setId(final String id) {
        category.setId(Long.parseLong(id));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return category.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the description.
     */
    @Override
    public String getDescription() {
        return category.getDescription();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the description.
     */
    @Override
    public void setDescription(final String d) {
        category.setDescription(d);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the name.
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
     * {@inheritDoc}
     *
     * Checks for access.
     */
    @Override
    public boolean hasAccess(final Resource r) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the users with profile.
     */
    @Override
    public Collection<WebUser> getUsersWithProfile() throws UserException {
        return UserHome.find(ModelHomeBase.getDefaultSearch("category=\"" + getId() + "\""));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the accessible resources.
     */
    @Override
    public Collection<WebResource> getAccessibleResources() throws UserException {
        return ResourceHome.find(ModelHomeBase.getDefaultSearch("category=\"" + getId() + "\""));
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final ecmwf.web.model.users.Category category && equals(category);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final ecmwf.web.model.users.Category c) {
        return getName().equals(c.getName());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Dump.
     */
    @Override
    public String dump(final String format) {
        return category.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Delete resource.
     */
    @Override
    public void deleteResource(final Resource cat) {
        deletedResources.add(cat);
    }

    /**
     * {@inheritDoc}
     *
     * Adds the resource.
     */
    @Override
    public void addResource(final Resource res) {
        addedResources.add(res);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the active.
     */
    @Override
    public boolean getActive() {
        return category.getActive();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the active.
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
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getName();
    }
}
