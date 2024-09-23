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

package ecmwf.ecpds.master.plugin.http.controller.user.category;

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

import ecmwf.ecpds.master.plugin.http.model.ecuser.WebCategory;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.home.users.ResourceHome;
import ecmwf.web.model.users.Category;
import ecmwf.web.model.users.UserException;

/**
 * The Class CategoryActionForm.
 */
public class CategoryActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5005569274180306235L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(CategoryActionForm.class);

    /** The id. */
    private String id = "";

    /** The name. */
    private String name = "";

    /** The description. */
    private String description = "";

    /** The active. */
    private String active = "off";

    /** The category. */
    private Category category = null;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
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
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the category.
     *
     * @return the category
     */
    public Category getCategory() {
        return this.category;
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
     * Sets the name.
     *
     * @param string
     *            the new name
     */
    public void setName(final String string) {
        name = string;
    }

    /**
     * Sets the description.
     *
     * @param string
     *            the new description
     */
    public void setDescription(final String string) {
        description = string;
    }

    /**
     * Sets the category.
     *
     * @param cat
     *            the new category
     */
    public void setCategory(final Category cat) {
        this.category = cat;
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
     * Gets the resources.
     *
     * @return All the Resources currently associated to the Category
     */
    public Collection<?> getResources() {
        try {
            if (this.category != null) {
                return this.category.getAccessibleResources();
            }
        } catch (final UserException e) {
            log.error("Problem getting Resources", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Gets the resource options.
     *
     * @return All the Resources NOT currently associated to the Category
     */
    public Collection<?> getResourceOptions() {
        try {
            final Collection<?> all = ResourceHome.findAll();
            all.removeAll(getResources());
            return all;
        } catch (final UserException e) {
            log.error("Problem getting Resource options", e);
            return new ArrayList<>(0);
        }

    }

    /**
     * Populate category.
     *
     * @param category
     *            the category
     *
     * @throws ecmwf.web.controller.ECMWFActionFormException
     *             the ECMWF action form exception
     */
    protected void populateCategory(final Category category) throws ECMWFActionFormException {
        if (!(category instanceof final WebCategory webCategory)) {
            throw new ECMWFActionFormException(
                    "An Category implementation '" + category.getClass().getName() + "' can NOT be edited");
        }
        webCategory.setName(name);
        webCategory.setDescription(description);
        webCategory.setActive(true);
    }

    /**
     * Populate from category.
     *
     * @param category
     *            the category
     *
     * @throws ecmwf.web.controller.ECMWFActionFormException
     *             the ECMWF action form exception
     */
    protected void populateFromCategory(final Category category) throws ECMWFActionFormException {
        if (!(category instanceof final WebCategory webCategory)) {
            throw new ECMWFActionFormException(
                    "An Category implementation '" + category.getClass().getName() + "' can NOT be edited");
        }
        this.category = webCategory;
        this.setId(webCategory.getId());
        this.setName(webCategory.getName());
        this.setDescription(webCategory.getDescription());
        this.setActive(webCategory.getActive() ? "on" : "off");
    }
}
