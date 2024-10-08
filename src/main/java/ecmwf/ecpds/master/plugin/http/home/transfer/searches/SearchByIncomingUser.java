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
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class SearchByIncomingUser.
 */
public class SearchByIncomingUser extends ModelSearchBase {

    /** The incoming user id. */
    public final String incomingUserId;

    /**
     * Instantiates a new search by incoming user.
     *
     * @param incomingUserId
     *            the incoming user id
     */
    public SearchByIncomingUser(final String incomingUserId) {
        this.incomingUserId = incomingUserId;
    }

    /**
     * Gets the incoming user id.
     *
     * @return the incoming user id
     */
    public String getIncomingUserId() {
        return this.incomingUserId;
    }
}
