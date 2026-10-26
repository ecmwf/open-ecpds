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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.ECUser;

/**
 * Startup-only identity providers. No discovery, provider chaining or classpath-based activation.
 */
public final class IdentityExtensions implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(IdentityExtensions.class);

    private final List<Extension> providers = new ArrayList<>();
    private AuthenticationProvider authentication;
    private UserDirectoryProvider directory;
    private InteractiveLogins interactive;

    private IdentityExtensions() {
    }

    public static IdentityExtensions load(final String root, final Map<String, String> authenticationOptions,
            final Map<String, String> directoryOptions) throws IOException {
        return load(root, authenticationOptions, directoryOptions, Map.of(), Map.of());
    }

    public static IdentityExtensions load(final String root, final Map<String, String> authenticationOptions,
            final Map<String, String> directoryOptions, final Map<String, String> interactiveOptions,
            final Map<String, String> mappings) throws IOException {
        final var extensions = new IdentityExtensions();
        try {
            extensions.authentication = extensions.load(AuthenticationProvider.class, root, authenticationOptions);
            extensions.directory = extensions.load(UserDirectoryProvider.class, root, directoryOptions);
            final var provider = extensions.load(InteractiveLoginProvider.class, root, interactiveOptions);
            extensions.interactive = new InteractiveLogins(provider, provider == null ? Map.of() : mappings);
            return extensions;
        } catch (IOException | RuntimeException | LinkageError e) {
            extensions.close();
            throw e;
        }
    }

    private <T extends Extension> T load(final Class<T> type, final String root, final Map<String, String> options)
            throws IOException {
        final var context = new ExtensionContext(root, options);
        final var configured = context.option("class");
        if (configured == null || configured.isBlank()) {
            return null;
        }
        final var name = configured.trim();
        try {
            final var provider = Class.forName(name, false, IdentityExtensions.class.getClassLoader()).asSubclass(type)
                    .getConstructor().newInstance();
            providers.add(provider);
            if (provider.apiVersion() != Extension.API_VERSION) {
                throw new IOException("Incompatible extension API: " + name);
            }
            provider.start(context);
            LOG.info("Started {}: {}", type.getSimpleName(), name);
            return provider;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            throw new IOException("Cannot load " + type.getSimpleName() + ": " + name, e);
        }
    }

    public boolean hasAuthenticationProvider() {
        return authentication != null;
    }

    public InteractiveLogins interactive() {
        return interactive;
    }

    public AuthenticationProvider.Result authenticate(final String user, final String credentials, final String origin)
            throws IOException {
        if (authentication == null) {
            return AuthenticationProvider.Result.UNSUPPORTED;
        }
        final var result = authentication.authenticate(user, credentials, origin);
        if (result == null) {
            throw new IOException("Authentication extension returned no result");
        }
        return result;
    }

    public ECUser lookup(final String user) throws IOException {
        final var result = directory == null ? null : directory.lookup(user);
        if (result != null && !user.equals(result.getName())) {
            throw new IOException("Directory extension returned a different user");
        }
        return result;
    }

    @Override
    public synchronized void close() {
        if (interactive != null) {
            interactive.clear();
        }
        for (var i = providers.size() - 1; i >= 0; i--) {
            final var provider = providers.get(i);
            try {
                provider.close();
            } catch (IOException | RuntimeException e) {
                LOG.warn("Could not close identity extension {}", provider.getClass().getName());
            }
        }
        providers.clear();
    }
}
