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

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.database.Url;
import ecmwf.ecpds.master.plugin.http.dao.OjbImplementedBean;
import ecmwf.ecpds.master.plugin.http.model.ecuser.WebResource;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.home.ModelHomeBase;
import ecmwf.web.home.users.CategoryHome;
import ecmwf.web.model.users.ACL;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.Resource;
import ecmwf.web.model.users.UserException;

/**
 * The Class ResourceBean.
 */
public class ResourceBean extends ModelBeanBase implements WebResource, OjbImplementedBean {

    /** The Constant USE_INSTEAD_OF_SLASHES. */
    protected static final char USE_INSTEAD_OF_SLASHES = '-';

    /** The added categories. */
    private final Collection<Category> addedCategories = new ArrayList<>();

    /** The deleted categories. */
    private final Collection<Category> deletedCategories = new ArrayList<>();

    /** The url. */
    private final Url url;

    /**
     * Instantiates a new resource bean.
     *
     * @param url
     *            the url
     */
    protected ResourceBean(final Url url) {
        this.url = url;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return Resource.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the ojb implementation.
     */
    @Override
    public DataBaseObject getOjbImplementation() {
        return url;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return getPath().replace('/', USE_INSTEAD_OF_SLASHES);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the host.
     */
    @Override
    public String getHost() {
        return "";
    }

    /**
     * {@inheritDoc}
     *
     * Sets the host.
     */
    @Override
    public void setHost(final String v) {
    }

    /**
     * {@inheritDoc}
     *
     * Gets the path.
     */
    @Override
    public String getPath() {
        return url.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the path.
     */
    @Override
    public void setPath(final String v) {
        url.setName(v);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the categories.
     */
    @Override
    public Collection<Category> getCategories() throws UserException {
        return CategoryHome.find(ModelHomeBase.getDefaultSearch("resource=\"" + getId() + "\""));
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final Resource resource && equals(resource);
    }

    /**
     * Equals.
     *
     * @param r
     *            the r
     *
     * @return true, if successful
     */
    public boolean equals(final Resource r) {
        return getId().equals(r.getId());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
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
     * Gets the read ACL.
     */
    @Override
    public ACL getReadACL() throws UserException {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the write ACL.
     */
    @Override
    public ACL getWriteACL() throws UserException {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the read ACL.
     */
    @Override
    public void setReadACL(final ACL acl) throws IllegalAccessException {
    }
}
