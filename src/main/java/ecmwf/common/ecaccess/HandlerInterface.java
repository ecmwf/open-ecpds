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
     * <strong>Note:</strong> this method must NOT be a {@code default} method. Java RMI's
     * {@code RemoteObjectInvocationHandler} (Java 9+) invokes default interface methods locally on the caller JVM
     * rather than forwarding the call to the remote object, which would silently return {@code "{}"} instead of
     * querying the actual remote server.
     * </p>
     *
     * @return JSON string; never {@code null}
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    String getHttpCertificateJson() throws RemoteException;

    /**
     * Deploys a new PKCS#12 keystore to this handler's HTTPS server and reloads the certificate.
     *
     * <p>
     * <strong>Note:</strong> this method must NOT be a {@code default} method — see {@link #getHttpCertificateJson()}
     * for the rationale.
     * </p>
     *
     * @param pkcs12Bytes
     *            the PKCS#12 keystore bytes
     * @param keystorePassword
     *            password for the keystore and private key
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    void deployHttpCertificate(final byte[] pkcs12Bytes, final String keystorePassword) throws RemoteException;
}
