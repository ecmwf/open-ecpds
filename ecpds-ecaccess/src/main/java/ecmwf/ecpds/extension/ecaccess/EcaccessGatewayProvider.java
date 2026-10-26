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
import java.rmi.server.Unreferenced;
import java.util.HashSet;
import java.util.Set;

import ecmwf.common.callback.CallBackObject;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.MSUser;
import ecmwf.common.ectrans.ECtransException;
import ecmwf.common.technical.ProxySocket;
import ecmwf.ecbatch.eis.rmi.client.DataAccess;
import ecmwf.ecpds.extension.ExtensionContext;
import ecmwf.ecpds.extension.RemoteServiceProvider;

/** ECCMD gateway discovery and Master-side relay; no EJB or server implementation. */
public final class EcaccessGatewayProvider implements RemoteServiceProvider {
    private final Set<Access> exports = new HashSet<>();
    private RmiEccmdClient client;

    @Override
    public int apiVersion() {
        return API_VERSION;
    }

    @Override
    public synchronized void start(final ExtensionContext context) throws IOException {
        client = RmiEccmdClient.configured(context);
    }

    @Override
    public EcaccessGatewayAccess open(final String target) throws IOException {
        if (target == null || target.isBlank()) {
            throw new IOException("Missing ECaccess gateway");
        }
        final RmiEccmdClient current;
        synchronized (this) {
            current = client;
        }
        if (current == null) {
            throw new IOException("ECaccess gateway provider is not started or is closed");
        }
        final var remote = current.gateway(target);
        if (remote == null) {
            return null;
        }
        synchronized (this) {
            if (client != current) {
                throw new IOException("ECaccess gateway provider is closed");
            }
            final var access = new Access(remote, target);
            exports.add(access);
            return access;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (client != null) {
            client.close();
            client = null;
        }
        for (final var access : Set.copyOf(exports)) {
            access.close();
        }
    }

    private final class Access extends CallBackObject implements EcaccessGatewayAccess, Unreferenced {
        private static final long serialVersionUID = 1L;
        private final DataAccess remote;
        private final String target;

        private Access(final DataAccess remote, final String target) throws RemoteException {
            this.remote = remote;
            this.target = target;
        }

        @Override
        public boolean available() throws IOException {
            return remote.available(target);
        }

        @Override
        public void get(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie, final String name, final ProxySocket socket, final long offset)
                throws IOException {
            run(() -> remote.get(target, user, association, destination, cookie, name, socket, offset));
        }

        @Override
        public void put(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie, final String name, final ProxySocket socket, final long offset, final long size)
                throws IOException {
            run(() -> remote.put(target, user, association, destination, cookie, name, socket, offset, size));
        }

        @Override
        public void del(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie, final String name) throws IOException {
            run(() -> remote.del(target, user, association, destination, cookie, name));
        }

        @Override
        public void mkdir(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie, final String name) throws IOException {
            run(() -> remote.mkdir(target, user, association, destination, cookie, name));
        }

        @Override
        public void rmdir(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie, final String name) throws IOException {
            run(() -> remote.rmdir(target, user, association, destination, cookie, name));
        }

        @Override
        public long size(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie, final String name) throws IOException {
            try {
                return remote.size(target, user, association, destination, cookie, name);
            } catch (ECtransException e) {
                throw new IOException(e.getMessage());
            }
        }

        @Override
        public void move(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie, final String source, final String name) throws IOException {
            run(() -> remote.move(target, user, association, destination, cookie, source, name));
        }

        @Override
        public String[] list(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie, final String directory) throws IOException {
            try {
                return remote.list(target, user, association, destination, cookie, directory);
            } catch (ECtransException e) {
                throw new IOException(e.getMessage());
            }
        }

        @Override
        public void remove(final ECUser user, final MSUser association, final ECtransDestination destination,
                final String cookie) throws IOException {
            run(() -> remote.remove(target, user, association, destination, cookie));
        }

        @Override
        public void close() {
            synchronized (EcaccessGatewayProvider.this) {
                exports.remove(this);
                unexport();
            }
        }

        @Override
        public void unreferenced() {
            close();
        }

        private void run(final Operation operation) throws IOException {
            try {
                operation.run();
            } catch (ECtransException e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    @FunctionalInterface
    private interface Operation {
        void run() throws IOException, ECtransException;
    }
}
