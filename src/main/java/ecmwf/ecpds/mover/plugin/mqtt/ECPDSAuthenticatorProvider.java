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

package ecmwf.ecpds.mover.plugin.mqtt;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import com.hivemq.extension.sdk.api.auth.Authenticator;
import com.hivemq.extension.sdk.api.auth.parameter.AuthenticatorProviderInput;
import com.hivemq.extension.sdk.api.services.auth.provider.AuthenticatorProvider;

/**
 * The Class ECPDSAuthenticatorProvider.
 */
public class ECPDSAuthenticatorProvider implements AuthenticatorProvider {

    /** The authenticator. */
    private final ECPDSAuthenticator authenticator;

    /**
     * Instantiates a new ECPDS authenticator provider.
     *
     * @param authenticator
     *            the authenticator
     */
    ECPDSAuthenticatorProvider(final ECPDSAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the authenticator.
     */
    @Override
    public Authenticator getAuthenticator(final AuthenticatorProviderInput authenticatorProviderInput) {
        // return a share-able authenticator which must be thread-safe / state-less
        return authenticator;
    }
}
