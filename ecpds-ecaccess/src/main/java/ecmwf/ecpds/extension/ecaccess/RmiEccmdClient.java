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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ecmwf.common.database.ECUser;
import ecmwf.common.rmi.RMIClientSocketFactory;
import ecmwf.common.rmi.interruptible.InterruptibleRMIThread;
import ecmwf.ecbatch.eis.rmi.client.DataAccess;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;
import ecmwf.ecbatch.eis.rmi.client.EccmdInterface;
import ecmwf.ecpds.extension.ExtensionContext;

/**
 * On-demand connection to the existing ECCMD registry binding for directory and gateway lookup.
 */
final class RmiEccmdClient implements EccmdClient {
    private final List<String> hosts;
    private final int port;
    private final int requestTimeoutMillis;
    private final ThreadPoolExecutor requests = new ThreadPoolExecutor(0, 8, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>(), task -> {
                final var thread = new InterruptibleRMIThread(task);
                thread.setName("ECCMD-Request");
                thread.setDaemon(true);
                return thread;
            });
    private volatile EccmdInterface connection;
    private boolean closed;

    RmiEccmdClient(final List<String> hosts, final int port, final int requestTimeoutMillis) {
        this.hosts = List.copyOf(hosts);
        this.port = port;
        this.requestTimeoutMillis = requestTimeoutMillis;
    }

    static RmiEccmdClient configured(final ExtensionContext context) throws IOException {
        final var configured = context.option("host");
        if (configured == null || configured.isBlank()) {
            throw new IOException("Missing ECaccess host");
        }
        final var hosts = Arrays.stream(configured.split("[,;|]", -1)).map(String::trim).toList();
        if (hosts.stream().anyMatch(String::isBlank) || hosts.size() > 8) {
            throw new IOException("Invalid ECaccess hosts");
        }
        final int port;
        try {
            port = Integer.parseInt(context.option("port") == null ? "640" : context.option("port"));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid ECaccess port");
        }
        if (port < 1 || port > 65535) {
            throw new IOException("Invalid ECaccess port");
        }
        final int requestTimeoutMillis;
        try {
            requestTimeoutMillis = Integer.parseInt(
                    context.option("requestTimeoutMillis") == null ? "10000" : context.option("requestTimeoutMillis"));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid ECaccess requestTimeoutMillis");
        }
        if (requestTimeoutMillis < 1 || requestTimeoutMillis > 60000) {
            throw new IOException("ECaccess requestTimeoutMillis must be between 1 and 60000 ms");
        }
        return new RmiEccmdClient(hosts, port, requestTimeoutMillis);
    }

    @Override
    public DataAccess gateway(final String target) throws IOException {
        try {
            return call(remote -> remote.getDataAccess(target));
        } catch (EccmdException e) {
            throw new IOException("ECCMD gateway discovery failed");
        }
    }

    private EccmdInterface connection() throws RemoteException {
        synchronized (this) {
            if (closed || Thread.currentThread().isInterrupted()) {
                throw new RemoteException("ECCMD identity client is closed or interrupted");
            }
            if (connection != null) {
                return connection;
            }
        }
        for (final var host : hosts) {
            if (Thread.currentThread().isInterrupted()) {
                throw new RemoteException("ECCMD identity request interrupted");
            }
            try {
                final var registry = LocateRegistry.getRegistry(host, port, new RMIClientSocketFactory());
                final var remote = registry.lookup("EccmdEngine");
                if (remote instanceof EccmdInterface eccmd) {
                    synchronized (this) {
                        if (closed || Thread.currentThread().isInterrupted()) {
                            throw new RemoteException("ECCMD identity client is closed or interrupted");
                        }
                        if (connection == null) {
                            connection = eccmd;
                        }
                        return connection;
                    }
                }
            } catch (RemoteException | NotBoundException e) {
                // Try the next discovery endpoint.
            }
        }
        throw new RemoteException("ECCMD identity service is unavailable");
    }

    private <T> T call(final Operation<T> operation) throws EccmdException, RemoteException {
        final Future<T> request;
        try {
            request = requests.submit(() -> execute(operation));
        } catch (RejectedExecutionException e) {
            throw new RemoteException("ECCMD identity client is closed or busy");
        }
        try {
            return request.get(requestTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RemoteException("ECCMD identity request timed out after " + requestTimeoutMillis + " ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("ECCMD identity request interrupted");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof EccmdException failure) {
                throw failure;
            }
            if (e.getCause() instanceof RemoteException failure) {
                throw failure;
            }
            if (e.getCause() instanceof RuntimeException failure) {
                throw failure;
            }
            if (e.getCause() instanceof Error failure) {
                throw failure;
            }
            throw new RemoteException("ECCMD identity request failed");
        } finally {
            request.cancel(true);
        }
    }

    private <T> T execute(final Operation<T> operation) throws EccmdException, RemoteException {
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
    public ECUser lookup(final String user) throws EccmdException, RemoteException {
        return call(remote -> remote.getECUser(user));
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            closed = true;
            connection = null;
        }
        requests.shutdownNow();
    }

    @FunctionalInterface
    private interface Operation<T> {
        T call(EccmdInterface remote) throws EccmdException, RemoteException;
    }
}
