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

package ecmwf.ecpds.master.plugin.http.home.transfer.searches;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DataTransfersByDataFile.
 */
public class DataTransfersByDataFile extends ModelSearchBase {

    /** The data file. */
    private final DataFile dataFile;

    /** The include deleted. */
    private final boolean includeDeleted;

    /**
     * Instantiates a new data transfers by data file.
     *
     * @param f
     *            the f
     * @param includeDeleted
     *            the include deleted
     */
    public DataTransfersByDataFile(final DataFile f, final boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
        this.dataFile = f;
    }

    /**
     * Gets the data file.
     *
     * @return the data file
     */
    public DataFile getDataFile() {
        return dataFile;
    }

    /**
     * Gets the include deleted.
     *
     * @return the include deleted
     */
    public boolean getIncludeDeleted() {
        return includeDeleted;
    }
}
