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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.MSUser;

/**
 * The Interface ECtransCallback.
 */
public interface ECtransCallback {
    /**
     * Gets the ectrans setup.
     *
     * @return the ectrans setup
     */
    ECtransSetup getECtransSetup();

    /**
     * Gets the unique name.
     *
     * @return the unique name
     */
    String getUniqueName();

    /**
     * Sets the MS user.
     *
     * @param msuser
     *            the new MS user
     */
    void setMSUser(MSUser msuser);

    /**
     * Retry.
     *
     * @param comment
     *            the comment
     */
    void retry(String comment);

    /**
     * Completed.
     *
     * @param module
     *            the module
     */
    void completed(TransferModule module);

    /**
     * Failed.
     *
     * @param module
     *            the module
     * @param comment
     *            the comment
     */
    void failed(TransferModule module, String comment);
}