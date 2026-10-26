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
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;

import ecmwf.common.database.ECUser;
import ecmwf.common.rmi.RMIClientSocketFactory;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;
import ecmwf.ecbatch.eis.rmi.client.EccmdInterface;

/**
 * On-demand connection to the existing ECCMD registry binding. Never replay a credential RPC after failure.
 */
final class RmiEccmdClient implements EccmdClient {
    private final List<String> hosts;
    private final int port;
    private volatile EccmdInterface connection;
    private boolean closed;

    RmiEccmdClient(final List<String> hosts, final int port) {
        this.hosts = List.copyOf(hosts);
        this.port = port;
    }

    private synchronized EccmdInterface connection() throws RemoteException {
        if (closed) {
            throw new RemoteException("ECCMD identity client is closed");
        }
        if (connection != null) {
            return connection;
        }
        for (final var host : hosts) {
            try {
                final var registry = LocateRegistry.getRegistry(host, port, new RMIClientSocketFactory());
                final var remote = registry.lookup("EccmdEngine");
                if (remote instanceof EccmdInterface eccmd) {
                    return connection = eccmd;
                }
            } catch (RemoteException | NotBoundException e) {
                // Failover applies to discovery only, never to a possibly consumed passcode.
            }
        }
        throw new RemoteException("ECCMD identity service is unavailable");
    }

    private <T> T call(final Operation<T> operation) throws EccmdException, RemoteException {
        final var current = connection();
        try {
            return operation.call(current);
        } catch (RemoteException e) {
            synchronized (this) {
                if (connection == current) {
                    connection = null;
                }
            }
            throw new RemoteException("ECCMD identity request failed");
        }
    }

    @Override
    public ECUser password(final String user, final String credential) throws EccmdException, RemoteException {
        return call(remote -> remote.getECUser(user, credential));
    }

    @Override
    public void preauthenticate(final String user, final String credential, final String root)
            throws EccmdException, RemoteException {
        call(remote -> remote.computePreAuth(user, credential, root, "monitoring"));
    }

    @Override
    public ECUser lookup(final String user) throws EccmdException, RemoteException {
        return call(remote -> remote.getECUser(user));
    }

    @Override
    public synchronized void close() throws IOException {
        closed = true;
        connection = null;
    }

    @FunctionalInterface
    private interface Operation<T> {
        T call(EccmdInterface remote) throws EccmdException, RemoteException;
    }
}
