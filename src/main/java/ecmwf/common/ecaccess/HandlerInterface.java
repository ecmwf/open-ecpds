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

package ecmwf.common.ecaccess;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.rmi.RemoteException;

import ecmwf.common.plugin.PluginEvent;

/**
 * The Interface HandlerInterface.
 */
public interface HandlerInterface extends ClientInterface {
    /**
     * Handle a list of events.
     *
     * @param events
     *            the events
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void handle(PluginEvent<?>[] events) throws RemoteException;

    /**
     * Handle a single event.
     *
     * @param event
     *            the event
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void handle(PluginEvent<?> event) throws RemoteException;

    /**
     * Returns a JSON-encoded snapshot of the TLS certificate currently loaded by this handler's HTTPS server, or
     * {@code "{}"} if no certificate is available.
     *
     * <p>
     * <strong>Note:</strong> this is intentionally a {@code default} method returning {@code "{}"}. Java RMI's
     * {@code RemoteObjectInvocationHandler} (Java 9+) invokes default interface methods locally on the caller JVM
     * rather than forwarding the call to the remote object. The pre-compiled {@code HandlerServer_Stub} in
     * {@code ecaccess-stubs.jar} was generated before this method existed and has no entry for it.
     *
     * <p>
     * Monitors running as {@link ecmwf.ecpds.monitor.MonitorServer} instead of the legacy {@code HandlerServer}
     * implement {@link ecmwf.ecpds.monitor.MonitorInterface}, which re-declares this method as {@code abstract}. Since
     * {@code MonitorInterface} has no pre-compiled stub, RMI uses a dynamic proxy that properly forwards the call to
     * the remote {@code MonitorServer}. The {@code default} here preserves backward compatibility for legacy clients
     * that still present a {@code HandlerServer_Stub}.
     *
     * @return JSON string; never {@code null}
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    default String getHttpCertificateJson() throws RemoteException {
        return "{}";
    }

    /**
     * Deploys a new PKCS#12 keystore to this handler's HTTPS server and reloads the certificate.
     *
     * <p>
     * <strong>Note:</strong> this is intentionally a {@code default} no-op — see {@link #getHttpCertificateJson()} for
     * the rationale. Monitors running as {@link ecmwf.ecpds.monitor.MonitorServer} override this method via
     * {@link ecmwf.ecpds.monitor.MonitorInterface} and receive the call over the direct RMI channel.
     *
     * @param pkcs12Bytes
     *            the PKCS#12 keystore bytes
     * @param keystorePassword
     *            password for the keystore and private key
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    default void deployHttpCertificate(final byte[] pkcs12Bytes, final String keystorePassword) throws RemoteException {
        // no-op: only effective on MonitorServer (MonitorInterface overrides this as abstract)
    }

    /**
     * Hot-reloads the TLS certificate from the keystore file currently on disk. This is a {@code default} no-op; the
     * actual reload is performed on {@link ecmwf.ecpds.monitor.MonitorServer} via the abstract override in
     * {@link ecmwf.ecpds.monitor.MonitorInterface}.
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    default void reloadHttpCertificate() throws RemoteException {
        // no-op: only effective on MonitorServer (MonitorInterface overrides this as abstract)
    }
}
