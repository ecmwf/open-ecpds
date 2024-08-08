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

package ecmwf.ecpds.master.plugin.http.controller.user.resource;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.home.users.CategoryHome;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.Resource;
import ecmwf.web.model.users.UserException;

/**
 * The Class ResourceActionForm.
 */
public class ResourceActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2704190002700890860L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ResourceActionForm.class);

    /** The id. */
    private String id = "";

    /** The path. */
    private String path = "";

    /** The active. */
    private String active = "off";

    /** The resource. */
    private Resource resource;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path.
     *
     * @param string
     *            the new path
     */
    public void setPath(final String string) {
        path = string;
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
     * Sets the active.
     *
     * @param string
     *            the new active
     */
    public void setActive(final String string) {
        active = string;
    }

    /**
     * Gets the categories.
     *
     * @return All the Categories currently associated to the Resource
     */
    public Collection<Category> getCategories() {
        try {
            if (resource != null) {
                return resource.getCategories();
            }
        } catch (final UserException e) {
            log.error("Problem getting Categories", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Gets the category options.
     *
     * @return All the Categories NOT currently associated to the Resource
     */
    public Collection<Category> getCategoryOptions() {
        try {
            final Collection<Category> all = CategoryHome.findAll();
            all.removeAll(getCategories());
            return all;
        } catch (final UserException e) {
            log.error("Problem getting Category options", e);
            return new ArrayList<>(0);
        }

    }

    /**
     * Populate resource.
     *
     * @param r
     *            the r
     */
    protected void populateResource(final Resource r) {
        try {
            r.setPath(path);
        } catch (final IllegalAccessException e) {
            log.error(e);
        }
    }

    /**
     * Populate from resource.
     *
     * @param resource
     *            the resource
     */
    protected void populateFromResource(final Resource resource) {
        this.setId(resource.getId());
        this.resource = resource;
        this.setPath(resource.getPath());
    }
}
