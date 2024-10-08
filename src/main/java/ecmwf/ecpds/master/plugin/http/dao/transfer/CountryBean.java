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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class CountryBean.
 */
public class CountryBean extends ModelBeanBase implements Country {

    /** The country. */
    private final ecmwf.common.database.Country country;

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return Country.class.getName();
    }

    /**
     * Instantiates a new country bean.
     *
     * @param c
     *            the c
     */
    protected CountryBean(final ecmwf.common.database.Country c) {
        this.country = c;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    public String getId() {
        return getIso();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the iso.
     */
    @Override
    public String getIso() {
        return country.getIso();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    public String getName() {
        return country.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof final CountryBean countryBean && equals(countryBean);
    }

    /**
     * Equals.
     *
     * @param u
     *            the u
     *
     * @return true, if successful
     */
    public boolean equals(final CountryBean u) {
        return getIso().equals(u.getIso());
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return getIso().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return getClass().getName() + " { " + country + " }";
    }
}
