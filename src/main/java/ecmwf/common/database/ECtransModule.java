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
 * The Class ECtransModule.
 */
public class ECtransModule extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3274730315941930033L;

    /** The ecm active. */
    protected boolean ECM_ACTIVE;

    /** The ecm archive. */
    protected String ECM_ARCHIVE;

    /** The ecm classe. */
    protected String ECM_CLASSE;

    /** The ecm name. */
    protected String ECM_NAME;

    /**
     * Instantiates a new ectrans module.
     */
    public ECtransModule() {
    }

    /**
     * Instantiates a new ectrans module.
     *
     * @param name
     *            the name
     */
    public ECtransModule(final String name) {
        setName(name);
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return ECM_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        ECM_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        ECM_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the archive.
     *
     * @return the archive
     */
    public String getArchive() {
        return ECM_ARCHIVE;
    }

    /**
     * Sets the archive.
     *
     * @param param
     *            the new archive
     */
    public void setArchive(final String param) {
        ECM_ARCHIVE = param;
    }

    /**
     * Gets the classe.
     *
     * @return the classe
     */
    public String getClasse() {
        return ECM_CLASSE;
    }

    /**
     * Sets the classe.
     *
     * @param param
     *            the new classe
     */
    public void setClasse(final String param) {
        ECM_CLASSE = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return ECM_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        ECM_NAME = param;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(ECM_NAME);
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
        final var other = (ECtransModule) obj;
        return Objects.equals(ECM_NAME, other.ECM_NAME);
    }
}
