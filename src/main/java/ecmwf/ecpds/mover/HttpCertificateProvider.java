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

package ecmwf.ecpds.mover;

/**
 * Implemented by plugins that expose an HTTPS server and can report on or replace the active TLS certificate. Keeping
 * this interface in the main mover package (rather than in the plugin sub-package) ensures it is loaded by the
 * application class loader and is therefore visible to RMI server threads, which cannot reach classes loaded by the
 * plugin's isolated class loader.
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
     * the server.
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
