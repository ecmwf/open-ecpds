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
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public class TransferModule extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6099499370549649496L;

    /** The trm active. */
    protected boolean TRM_ACTIVE;

    /** The trm archive. */
    protected String TRM_ARCHIVE;

    /** The trm classe. */
    protected String TRM_CLASSE;

    /** The trm name. */
    protected String TRM_NAME;

    /**
     * Instantiates a new transfer module.
     */
    public TransferModule() {
    }

    /**
     * Instantiates a new transfer module.
     *
     * @param name
     *            the name
     */
    public TransferModule(final String name) {
        setName(name);
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return TRM_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        TRM_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        TRM_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the archive.
     *
     * @return the archive
     */
    public String getArchive() {
        return TRM_ARCHIVE;
    }

    /**
     * Sets the archive.
     *
     * @param param
     *            the new archive
     */
    public void setArchive(final String param) {
        TRM_ARCHIVE = param;
    }

    /**
     * Gets the classe.
     *
     * @return the classe
     */
    public String getClasse() {
        return TRM_CLASSE;
    }

    /**
     * Sets the classe.
     *
     * @param param
     *            the new classe
     */
    public void setClasse(final String param) {
        TRM_CLASSE = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return TRM_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        TRM_NAME = param;
    }
}
