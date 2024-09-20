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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class CatUrl.
 */
public class CatUrl extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4998875148534456402L;

    /** The cat id. */
    protected long CAT_ID;

    /** The url name. */
    protected String URL_NAME;

    /** The category. */
    protected Category category;

    /** The url. */
    protected Url url;

    /**
     * Instantiates a new cat url.
     */
    public CatUrl() {
    }

    /**
     * Instantiates a new cat url.
     *
     * @param categoryId
     *            the category id
     * @param urlName
     *            the url name
     */
    public CatUrl(final long categoryId, final String urlName) {
        setCategoryId(categoryId);
        setUrlName(urlName);
    }

    /**
     * Gets the category id.
     *
     * @return the category id
     */
    public long getCategoryId() {
        return CAT_ID;
    }

    /**
     * Sets the category id.
     *
     * @param param
     *            the new category id
     */
    public void setCategoryId(final long param) {
        CAT_ID = param;
    }

    /**
     * Sets the category id.
     *
     * @param param
     *            the new category id
     */
    public void setCategoryId(final String param) {
        CAT_ID = Long.parseLong(param);
    }

    /**
     * Gets the url name.
     *
     * @return the url name
     */
    public String getUrlName() {
        return URL_NAME;
    }

    /**
     * Sets the url name.
     *
     * @param param
     *            the new url name
     */
    public void setUrlName(final String param) {
        URL_NAME = param;
    }

    /**
     * Gets the category.
     *
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets the category.
     *
     * @param param
     *            the new category
     */
    public void setCategory(final Category param) {
        category = param;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public Url getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param param
     *            the new url
     */
    public void setUrl(final Url param) {
        url = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(CAT_ID, URL_NAME);
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (CatUrl) obj;
        return CAT_ID == other.CAT_ID && Objects.equals(URL_NAME, other.URL_NAME);
    }
}
