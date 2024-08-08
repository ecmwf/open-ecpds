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

package ecmwf.ecpds.master.plugin.http.controller.datafile.datafile;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.web.controller.ECMWFActionForm;

/**
 * The Class DataFileActionForm.
 */
public class DataFileActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 930804515079296057L;

    /** The id. */
    private String id = "";

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
     * @param v
     *            the new id
     */
    public void setId(final String v) {
        id = v;
    }

    /**
     * Populate data file.
     *
     * @param h
     *            the h
     */
    protected void populateDataFile(final DataFile h) {
    }

    /**
     * Populate from data file.
     *
     * @param h
     *            the h
     */
    protected void populateFromDataFile(final DataFile h) {
        this.setId(h.getId());
    }
}
