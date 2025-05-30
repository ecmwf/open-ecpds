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

import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DestinationAliases.
 */
public class DestinationAliases extends ModelSearchBase {

    /** The d. */
    private final Destination d;

    /** The alias list. */
    private final boolean aliasList;

    /**
     * Instantiates a new destination aliases.
     *
     * @param d
     *            the d
     * @param destinations
     *            the destinations
     */
    public DestinationAliases(final Destination d, final boolean destinations) {
        this.d = d;
        this.aliasList = destinations;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public Destination getDestination() {
        return d;
    }

    /**
     * Checks if is alias list.
     *
     * @return true, if is alias list
     */
    public boolean isAliasList() {
        return aliasList;
    }
}
