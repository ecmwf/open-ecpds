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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.MSUser;

/**
 * The Class ECtransProvider.
 */
public abstract class ECtransProvider extends RemoteProvider {
    /**
     * Deploy.
     */
    public void deploy() {
    }

    /**
     * Gets the MS user.
     *
     * @param ecuser
     *            the ecuser
     * @param msuser
     *            the msuser
     *
     * @return the MS user
     *
     * @throws ECtransException
     *             the ectrans exception
     */
    public abstract MSUser getMSUser(String ecuser, String msuser) throws ECtransException;

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     */
    public abstract ECUser getECUser(String name);

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     */
    public abstract ECtransModule getECtransModule(String name);

    /**
     * Checks if is granted.
     *
     * @param ecuser
     *            the ecuser
     * @param msuser
     *            the msuser
     *
     * @return true, if is granted
     */
    public boolean isGranted(final ECUser ecuser, final MSUser msuser) {
        return true;
    }

    /**
     * Checks if is granted.
     *
     * @param msuser
     *            the msuser
     * @param destination
     *            the destination
     *
     * @return true, if is granted
     */
    public boolean isGranted(final MSUser msuser, final ECtransDestination destination) {
        return true;
    }
}
