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

package ecmwf.ecpds.master.plugin.http.controller.admin;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import org.apache.struts.upload.FormFile;

import ecmwf.web.controller.ECMWFActionForm;

/**
 * The Class CertificatesActionForm.
 *
 * Struts ActionForm for the certificate management page. Carries the uploaded certificate file and optional import
 * password for the {@code /do/admin/certificates?action=import} form submission.
 */
public class CertificatesActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The uploaded certificate file (PEM, PKCS#12, or JKS). */
    private FormFile certFile;

    /** The optional password for the uploaded keystore or encrypted PEM. */
    private String importPassword;

    /**
     * Gets the cert file.
     *
     * @return the cert file
     */
    public FormFile getCertFile() {
        return certFile;
    }

    /**
     * Sets the cert file.
     *
     * @param certFile
     *            the new cert file
     */
    public void setCertFile(final FormFile certFile) {
        this.certFile = certFile;
    }

    /**
     * Gets the import password.
     *
     * @return the import password
     */
    public String getImportPassword() {
        return importPassword;
    }

    /**
     * Sets the import password.
     *
     * @param importPassword
     *            the new import password
     */
    public void setImportPassword(final String importPassword) {
        this.importPassword = importPassword;
    }
}
