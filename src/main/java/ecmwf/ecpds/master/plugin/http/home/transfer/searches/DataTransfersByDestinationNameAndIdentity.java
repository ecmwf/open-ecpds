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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DataTransfersByDestinationNameAndIdentity.
 */
public class DataTransfersByDestinationNameAndIdentity extends ModelSearchBase {

    /** The destination name. */
    private final String destinationName;

    /** The identity. */
    private final String identity;

    /**
     * Instantiates a new data transfers by destination name and identity.
     *
     * @param dest
     *            the dest
     * @param id
     *            the id
     */
    public DataTransfersByDestinationNameAndIdentity(final String dest, final String id) {
        this.destinationName = dest;
        this.identity = id;
    }

    /**
     * Gets the identity.
     *
     * @return the identity
     */
    public String getIdentity() {
        return this.identity;
    }

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return destinationName;
    }
}
