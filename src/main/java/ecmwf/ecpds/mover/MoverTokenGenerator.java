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

package ecmwf.ecpds.mover;

import java.io.IOException;

import ecmwf.common.ecaccess.ECauthToken;
import ecmwf.common.ecaccess.ECauthTokenManager;

/**
 * The Class MoverTokenGenerator.
 */
final class MoverTokenGenerator extends ECauthTokenManager {
    private final MoverServer _mover;

    MoverTokenGenerator(final MoverServer _mover) {
        this._mover = _mover;
        super();
    }

    /**
     * Request E cauth token.
     *
     * @param user
     *            the user
     *
     * @return the ecauth token
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public ECauthToken requestECauthToken(final String user) throws IOException {
        try {
            return _mover.getMasterProxy().getECauthToken(user);
        } catch (final Exception e) {
            MoverServer._log.warn("getECauthToken", e);
            throw new IOException("Getting ECauthToken for " + user, e);
        }
    }
}
