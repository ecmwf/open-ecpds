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
import java.util.Map;

import ecmwf.common.database.ECUser;

/** Startup-only directory loading shared by Masters independently of authentication policy. */
public final class UserDirectories implements AutoCloseable {
    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager
            .getLogger(UserDirectories.class);
    private volatile UserDirectoryProvider provider;

    private UserDirectories(final UserDirectoryProvider provider) {
        this.provider = provider;
    }

    public static UserDirectories load(final String root, final Map<String, String> options) throws IOException {
        final var context = new ExtensionContext(root, options);
        final var configured = context.option("class");
        if (configured == null || configured.isBlank()) {
            return new UserDirectories(null);
        }
        UserDirectoryProvider provider = null;
        try {
            provider = Class.forName(configured.trim(), false, UserDirectories.class.getClassLoader())
                    .asSubclass(UserDirectoryProvider.class).getConstructor().newInstance();
            if (provider.apiVersion() != Extension.API_VERSION) {
                throw new IOException("Incompatible user directory extension API");
            }
            provider.start(context);
            LOG.info("Started UserDirectoryProvider: {}", configured.trim());
            return new UserDirectories(provider);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError | IOException e) {
            new UserDirectories(provider).close();
            throw new IOException("Cannot load user directory extension: " + configured.trim(), e);
        }
    }

    public ECUser lookup(final String user) throws IOException {
        final var current = provider;
        final var result = current == null ? null : current.lookup(user);
        if (result != null && !user.equals(result.getName())) {
            throw new IOException("Directory extension returned a different user");
        }
        return result;
    }

    @Override
    public synchronized void close() {
        final var closing = provider;
        provider = null;
        if (closing != null) {
            try {
                closing.close();
            } catch (IOException | RuntimeException e) {
                LOG.warn("Could not close user directory extension {}", closing.getClass().getName());
            }
        }
    }
}
