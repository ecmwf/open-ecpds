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
 * In applying this License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.mover;

import java.io.Serializable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Broker processing outcome, never an acknowledgement from a subscriber. */
public record MQTTPublishResult(String status, String reason) implements Serializable {
    /** Only an unsupported-method dispatch error is safe to fall back from without risking a duplicate publish. */
    public static boolean unsupportedMethod(final Throwable error) {
        Throwable current = error;
        for (int i = 0; current != null && i < 8; i++, current = current.getCause()) {
            if (current instanceof java.rmi.UnmarshalException && current.getMessage() != null
                    && current.getMessage().startsWith("unrecognized method hash:")) {
                return true;
            }
        }
        return false;
    }

    public static MQTTPublishResult await(final CompletionStage<Void> completion, final long timeoutMillis) {
        try {
            completion.toCompletableFuture().get(timeoutMillis, TimeUnit.MILLISECONDS);
            return new MQTTPublishResult("ACCEPTED", "");
        } catch (final TimeoutException e) {
            // Do not cancel or retry: the broker may still complete this publish.
            return new MQTTPublishResult("UNCONFIRMED", "Broker completion timed out; message may still be accepted");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return new MQTTPublishResult("UNCONFIRMED", "Interrupted while awaiting broker completion");
        } catch (final java.util.concurrent.CancellationException e) {
            return new MQTTPublishResult("UNCONFIRMED", "Broker completion was cancelled; delivery outcome is unknown");
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof UnsupportedOperationException) {
                return new MQTTPublishResult("UNCONFIRMED", "MQTT provider does not expose broker completion");
            }
            return new MQTTPublishResult("FAILED",
                    "Broker rejected publication: " + e.getCause().getClass().getSimpleName());
        }
    }
}
