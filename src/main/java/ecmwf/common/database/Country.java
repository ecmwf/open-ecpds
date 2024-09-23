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
 * The Class Country.
 */
public class Country extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6609721102924722194L;

    /** The cou iso. */
    protected String COU_ISO;

    /** The cou name. */
    protected String COU_NAME;

    /**
     * Instantiates a new country.
     */
    public Country() {
    }

    /**
     * Instantiates a new country.
     *
     * @param iso
     *            the iso
     */
    public Country(final String iso) {
        setIso(iso);
    }

    /**
     * Gets the iso.
     *
     * @return the iso
     */
    public String getIso() {
        return COU_ISO;
    }

    /**
     * Sets the iso.
     *
     * @param param
     *            the new iso
     */
    public void setIso(final String param) {
        COU_ISO = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return COU_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        COU_NAME = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(COU_ISO);
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
        final var other = (Country) obj;
        return Objects.equals(COU_ISO, other.COU_ISO);
    }
}
