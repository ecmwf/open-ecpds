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

package ecmwf.ecpds.monitor;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.rmi.RemoteException;

import ecmwf.common.ecaccess.HandlerInterface;

/**
 * The Interface MonitorInterface.
 *
 * <p>
 * Extends {@link HandlerInterface} with direct RMI-callable certificate operations for ECpds Monitor daemons.
 *
 * <p>
 * {@link HandlerInterface#getHttpCertificateJson()} and {@link HandlerInterface#deployHttpCertificate} are declared
 * {@code default} (no-op) in {@code HandlerInterface} because the pre-compiled {@code HandlerServer_Stub} in the
 * parallel {@code ECaccess-J} project has no entry for those methods — Java 9+ RMI would invoke them locally on the
 * caller JVM rather than forwarding to the remote.
 *
 * <p>
 * {@code MonitorInterface} is defined inside open-ecpds, so no pre-compiled stub exists for it. Java RMI uses a dynamic
 * proxy ({@code RemoteObjectInvocationHandler}) for any remote object exported under this interface, and a dynamic
 * proxy forwards non-default abstract methods to the remote correctly. By re-declaring {@code getHttpCertificateJson()}
 * and {@code deployHttpCertificate()} as {@code abstract} here, the master can call them via an ordinary RMI round-trip
 * to the {@link MonitorServer} running on the monitor host.
 *
 * <p>
 * Monitors must be started with {@code -Decmwf.common.starter.name=ecmwf.ecpds.monitor.MonitorServer} instead of the
 * legacy {@code ecmwf.common.ecaccess.HandlerServer}.
 */
public interface MonitorInterface extends HandlerInterface {

    /**
     * Returns a JSON-encoded snapshot of the TLS certificate currently loaded by this monitor's HTTPS server.
     *
     * <p>
     * This method re-declares the {@code default} method from {@link HandlerInterface} as {@code abstract} so that Java
     * RMI's dynamic proxy forwards the call to the remote {@link MonitorServer} rather than executing it locally.
     *
     * @return JSON string; never {@code null}
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    @Override
    String getHttpCertificateJson() throws RemoteException;

    /**
     * Deploys a new PKCS#12 keystore to this monitor's HTTPS server and reloads the certificate.
     *
     * <p>
     * This method re-declares the {@code default} no-op from {@link HandlerInterface} as {@code abstract} so that Java
     * RMI's dynamic proxy forwards the call to the remote {@link MonitorServer}.
     *
     * @param pkcs12Bytes
     *            the PKCS#12 keystore bytes
     * @param keystorePassword
     *            password for the keystore and private key
     *
     * @throws java.rmi.RemoteException
     *             the remote exception
     */
    @Override
    void deployHttpCertificate(byte[] pkcs12Bytes, String keystorePassword) throws RemoteException;
}
