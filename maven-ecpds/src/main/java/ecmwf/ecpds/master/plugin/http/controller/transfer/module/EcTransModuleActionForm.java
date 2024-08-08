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

package ecmwf.ecpds.master.plugin.http.controller.transfer.module;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Form bean for Method.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.ecpds.master.plugin.http.model.transfer.EcTransModule;
import ecmwf.web.controller.ECMWFActionForm;

/**
 * The Class EcTransModuleActionForm.
 */
public class EcTransModuleActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2362215411434670959L;

    /** The id. */
    private String id = "";

    /** The name. */
    private String name = "";

    /** The classe. */
    private String classe = "";

    /** The archive. */
    private String archive = "";

    /** The active. */
    private String active = "";

    /**
     * Gets the active.
     *
     * @return the active
     */
    public String getActive() {
        return active;
    }

    /**
     * Gets the archive.
     *
     * @return the archive
     */
    public String getArchive() {
        return archive;
    }

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
     * Gets the classe.
     *
     * @return the classe
     */
    public String getClasse() {
        return classe;
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
     * Sets the archive.
     *
     * @param string
     *            the new archive
     */
    public void setArchive(final String string) {
        archive = string;
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
     * Sets the classe.
     *
     * @param c
     *            the new classe
     */
    public void setClasse(final String c) {
        classe = c;
    }

    /**
     * Populate ec trans module.
     *
     * @param h
     *            the h
     */
    protected void populateEcTransModule(final EcTransModule h) {
        h.setName(name);
        h.setArchive(archive);
        h.setClasse(classe);
        h.setActive(convertToBoolean(active));
    }

    /**
     * Populate from ec trans module.
     *
     * @param h
     *            the h
     */
    protected void populateFromEcTransModule(final EcTransModule h) {
        this.setId(h.getId());
        this.setName(h.getName());
        this.setClasse(h.getClasse());
        this.setArchive(h.getArchive());
        this.setActive(h.getActive() ? "on" : "off");
    }
}
