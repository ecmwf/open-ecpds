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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Explicitly named, startup-only remote services. No discovery or reflective RPC dispatch. */
public final class RemoteServices implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(RemoteServices.class);
    private final Map<String, RemoteServiceProvider> providers = new LinkedHashMap<>();

    private RemoteServices() {
    }

    public static RemoteServices load(final String root, final Map<String, String> services,
            final Function<String, Map<String, String>> configuration) throws IOException {
        final var extensions = new RemoteServices();
        try {
            for (final var entry : new TreeMap<>(services).entrySet()) {
                final var id = entry.getKey();
                if (!id.matches("[A-Za-z][A-Za-z0-9_.-]{0,63}") || entry.getValue().isBlank()) {
                    throw new IOException("Invalid remote service registration");
                }
                final var context = new ExtensionContext(root, configuration.apply(entry.getValue().trim()));
                final var configured = context.option("class");
                if (configured == null || configured.isBlank()) {
                    throw new IOException("Missing remote service provider: " + id);
                }
                final var provider = Class.forName(configured.trim(), false, RemoteServices.class.getClassLoader())
                        .asSubclass(RemoteServiceProvider.class).getConstructor().newInstance();
                extensions.providers.put(id, provider);
                if (provider.apiVersion() != Extension.API_VERSION) {
                    throw new IOException("Incompatible remote service API: " + id);
                }
                provider.start(context);
                LOG.info("Started remote service {}: {}", id, provider.getClass().getName());
            }
            return extensions;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            extensions.close();
            throw new IOException("Cannot load remote service extension", e);
        } catch (IOException e) {
            extensions.close();
            throw e;
        }
    }

    public Remote open(final String service, final String target) throws IOException {
        final RemoteServiceProvider current;
        synchronized (this) {
            current = providers.get(service);
        }
        if (current == null) {
            throw new IOException("Remote service extension is not configured or is closed");
        }
        return current.open(target);
    }

    @Override
    public synchronized void close() {
        final var closing = new ArrayList<>(providers.values());
        providers.clear();
        for (var i = closing.size() - 1; i >= 0; i--) {
            final var provider = closing.get(i);
            try {
                provider.close();
            } catch (IOException | RuntimeException e) {
                LOG.warn("Could not close remote service extension {}", provider.getClass().getName());
            }
        }
    }
}
