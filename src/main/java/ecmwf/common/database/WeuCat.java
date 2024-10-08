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

import java.util.Objects;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public class WeuCat extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1610007381228128042L;

    /** The cat id. */
    protected long CAT_ID;

    /** The weu id. */
    protected String WEU_ID;

    /** The category. */
    protected Category category;

    /** The web user. */
    protected WebUser webUser;

    /**
     * Instantiates a new weu cat.
     */
    public WeuCat() {
    }

    /**
     * Instantiates a new weu cat.
     *
     * @param categoryId
     *            the category id
     * @param webuserId
     *            the webuser id
     */
    public WeuCat(final long categoryId, final String webuserId) {
        setCategoryId(categoryId);
        setWebUserId(webuserId);
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
     * Gets the web user id.
     *
     * @return the web user id
     */
    public String getWebUserId() {
        return WEU_ID;
    }

    /**
     * Sets the web user id.
     *
     * @param param
     *            the new web user id
     */
    public void setWebUserId(final String param) {
        WEU_ID = param;
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
     * Gets the web user.
     *
     * @return the web user
     */
    public WebUser getWebUser() {
        return webUser;
    }

    /**
     * Sets the web user.
     *
     * @param param
     *            the new web user
     */
    public void setWebUser(final WebUser param) {
        webUser = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(CAT_ID, WEU_ID);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (WeuCat) obj;
        return CAT_ID == other.CAT_ID && Objects.equals(WEU_ID, other.WEU_ID);
    }
}
