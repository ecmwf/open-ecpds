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

package ecmwf.common.security;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Implemented by plugins (and remote-interface bridge objects) that expose an HTTPS server and can report on or replace
 * the active TLS certificate.
 *
 * <p>
 * Placing this interface in the {@code ecmwf.common.security} package (rather than in a plugin sub-package) ensures it
 * is loaded by the application class loader and is therefore visible to RMI server threads and other components that
 * cannot reach classes loaded by an isolated plugin class loader.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public interface HttpCertificateProvider {

    /**
     * Returns a JSON-encoded snapshot of the TLS certificate currently loaded by this plugin's HTTPS server, or
     * {@code "{}"} if no certificate is available.
     *
     * @return JSON string; never {@code null}
     */
    String buildCertificateJson();

    /**
     * Deploys a new PKCS#12 keystore to this plugin's HTTPS server and hot-reloads the certificate without restarting
     * the server (where supported).
     *
     * @param pkcs12Bytes
     *            the PKCS#12 keystore bytes
     * @param keystorePassword
     *            password for the keystore and private key
     *
     * @throws Exception
     *             if the deployment or reload fails
     */
    void deployCertificate(byte[] pkcs12Bytes, String keystorePassword) throws Exception;
}
