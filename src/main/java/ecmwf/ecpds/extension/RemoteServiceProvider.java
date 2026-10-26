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

package ecmwf.ecpds.extension;

import java.io.IOException;
import java.rmi.Remote;

/**
 * An explicitly configured Master-side service for trusted remote peers. The extension owns its remote interface,
 * exported objects and their lifecycle. Implementations must be thread-safe and bound network waits.
 */
public interface RemoteServiceProvider extends Extension {
    /**
     * Return an exported service, or null if unavailable. The target is opaque to core; its interpretation and
     * validation belong to the provider.
     */
    Remote open(String target) throws IOException;
}
