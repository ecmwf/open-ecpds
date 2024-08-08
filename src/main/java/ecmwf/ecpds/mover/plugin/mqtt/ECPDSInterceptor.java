/*
 * Copyright 2018-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ecmwf.ecpds.mover.plugin.mqtt;

/**
 * This is a very simple {@link PublishInboundInterceptor}, it changes the
 * payload of every incoming PUBLISH with the topic 'hello/world' to 'Hello
 * World!'.
 *
 * @author Yannick Weber
 * @since 4.3.1
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishOutboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundInput;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundOutput;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishOutboundInput;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishOutboundOutput;

/**
 * The Class ECPDSInterceptor.
 */
public class ECPDSInterceptor implements PublishInboundInterceptor, PublishOutboundInterceptor {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ECPDSInterceptor.class);

    /**
     * On inbound publish.
     *
     * @param publishInboundInput
     *            the publish inbound input
     * @param publishInboundOutput
     *            the publish inbound output
     */
    @Override
    public void onInboundPublish(final @NotNull PublishInboundInput publishInboundInput,
            final @NotNull PublishInboundOutput publishInboundOutput) {
        log.debug("Received inbound publish packet: {}", publishInboundOutput.getPublishPacket().getTopic());
    }

    /**
     * On outbound publish.
     *
     * @param publishOutboundInput
     *            the publish outbound input
     * @param publishOutboundOutput
     *            the publish outbound output
     */
    @Override
    public void onOutboundPublish(@NotNull final PublishOutboundInput publishOutboundInput,
            @NotNull final PublishOutboundOutput publishOutboundOutput) {
        log.debug("Received outbound publish packet: {}", publishOutboundOutput.getPublishPacket().getTopic());
    }

}