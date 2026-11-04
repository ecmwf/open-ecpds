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

package ecmwf.ecpds.extension.ecaccess;

import java.io.IOException;
import java.rmi.RemoteException;

import ecmwf.common.database.ECUser;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;
import ecmwf.ecpds.extension.ExtensionContext;
import ecmwf.ecpds.extension.UserDirectoryProvider;

/**
 * ECaccess user directory without credential authentication or the legacy Master implementation.
 */
public final class EcaccessIdentityProvider implements UserDirectoryProvider {
    private EccmdClient client;

    public EcaccessIdentityProvider() {
    }

    EcaccessIdentityProvider(final EccmdClient client) {
        this.client = client;
    }

    @Override
    public int apiVersion() {
        return API_VERSION;
    }

    @Override
    public void start(final ExtensionContext context) throws IOException {
        client = RmiEccmdClient.configured(context);
    }

    @Override
    public ECUser lookup(final String user) throws IOException {
        if (user == null || user.isBlank()) {
            throw new IOException("Missing ECaccess user name");
        }
        try {
            final var identity = client().lookup(user);
            if (identity != null) {
                validate(user, identity);
            }
            return identity;
        } catch (EccmdException | RemoteException e) {
            throw new IOException("ECaccess user directory lookup failed");
        }
    }

    private EccmdClient client() throws IOException {
        if (client == null) {
            throw new IOException("ECaccess identity provider is not started");
        }
        return client;
    }

    private static void validate(final String user, final ECUser identity) throws IOException {
        if (!user.equals(identity.getName())) {
            throw new IOException("ECaccess returned a different user");
        }
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
