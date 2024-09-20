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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECTRANS;

import ecmwf.common.database.MSUser;
import ecmwf.common.text.Format;

/**
 * The Class DefaultCallback.
 */
public class DefaultCallback implements ECtransCallback {
    /** The _setup. */
    private final ECtransSetup _setup;

    /**
     * Instantiates a new default callback.
     *
     * @param setup
     *            the setup
     */
    public DefaultCallback(final ECtransSetup setup) {
        _setup = setup;
    }

    /**
     * Instantiates a new default callback.
     *
     * @param provider
     *            the provider
     * @param msuser
     *            the msuser
     */
    public DefaultCallback(final RemoteProvider provider, final MSUser msuser) {
        _setup = HOST_ECTRANS.getECtransSetup(msuser != null ? provider.decrypt(msuser.getData()) : "");
    }

    /**
     * Gets the unique name.
     *
     * @return the unique name
     */
    @Override
    public String getUniqueName() {
        return Format.formatLong(this.hashCode(), 10, true);
    }

    /**
     * Gets the ectrans setup.
     *
     * @return the ectrans setup
     */
    @Override
    public ECtransSetup getECtransSetup() {
        return _setup;
    }

    /**
     * Sets the MS user.
     *
     * @param msuser
     *            the new MS user
     */
    @Override
    public void setMSUser(final MSUser msuser) {
    }

    /**
     * Retry.
     *
     * @param comment
     *            the comment
     */
    @Override
    public void retry(final String comment) {
    }

    /**
     * Completed.
     *
     * @param module
     *            the module
     */
    @Override
    public void completed(final TransferModule module) {
    }

    /**
     * Failed.
     *
     * @param module
     *            the module
     * @param comment
     *            the comment
     */
    @Override
    public void failed(final TransferModule module, final String comment) {
    }
}
