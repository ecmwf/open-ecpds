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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.parameter.ExtensionInformation;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.intializer.InitializerRegistry;

/**
 * The Class ECPDSMain.
 */
public class ECPDSMain implements ExtensionMain {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ECPDSMain.class);

    /**
     * Extension start.
     *
     * @param extensionStartInput
     *            the extension start input
     * @param extensionStartOutput
     *            the extension start output
     */
    @Override
    public void extensionStart(final @NotNull ExtensionStartInput extensionStartInput,
            final @NotNull ExtensionStartOutput extensionStartOutput) {
        try {
            addClientLifecycleEventListener();
            addPublishModifier();
            addSecurityAuthenticator();
            final ExtensionInformation extensionInformation = extensionStartInput.getExtensionInformation();
            log.info("Started {}:{}", extensionInformation.getName(), extensionInformation.getVersion());
        } catch (final Exception e) {
            log.error("Exception thrown at extension start: ", e);
        }
    }

    /**
     * Extension stop.
     *
     * @param extensionStopInput
     *            the extension stop input
     * @param extensionStopOutput
     *            the extension stop output
     */
    @Override
    public void extensionStop(final @NotNull ExtensionStopInput extensionStopInput,
            final @NotNull ExtensionStopOutput extensionStopOutput) {
        final ExtensionInformation extensionInformation = extensionStopInput.getExtensionInformation();
        log.info("Started {}:{}", extensionInformation.getName(), extensionInformation.getVersion());
    }

    /**
     * Adds the client lifecycle event listener.
     */
    private void addClientLifecycleEventListener() {
        Services.eventRegistry().setClientLifecycleEventListener(input -> new ECPDSListener());
    }

    /**
     * Adds the publish modifier.
     */
    private void addPublishModifier() {
        final var interceptor = new ECPDSInterceptor();
        final InitializerRegistry registry = Services.initializerRegistry();
        registry.setClientInitializer(
                (initializerInput, clientContext) -> clientContext.addPublishInboundInterceptor(interceptor));
        registry.setClientInitializer(
                (initializerInput, clientContext) -> clientContext.addPublishOutboundInterceptor(interceptor));
    }

    /**
     * Adds the security authenticator.
     */
    private void addSecurityAuthenticator() {
        Services.securityRegistry().setAuthenticatorProvider(new ECPDSAuthenticatorProvider(new ECPDSAuthenticator()));
    }
}
