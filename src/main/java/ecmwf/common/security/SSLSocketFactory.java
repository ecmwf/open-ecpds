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
 * A factory for creating SSLSocket objects.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * A factory for creating SSLSocket objects.
 */
public class SSLSocketFactory {
    /** The Constant ssl version. */
    private static final String SSL_VERSION = Cnf.at("Security", "SSLVersion", "TLSv1.3");

    /** The Constant key store path. */
    private static final String KEY_STORE_PATH = Cnf.at("Security", "SSLKeyStore",
            System.getProperty("javax.net.ssl.keyStore"));

    /** The Constant key store pass. */
    private static final String KEY_STORE_PASSWORD = Cnf.at("Security", "SSLKeyStorePassword",
            System.getProperty("javax.net.ssl.keyStorePassword"));

    /** The Constant key store type. */
    private static final String KEY_STORE_TYPE = Cnf.at("Security", "SSLKeyStoreType",
            System.getProperty("javax.net.ssl.keyStoreType", "PKCS12"));

    /** The Constant _keyStoreAlgorithm. */
    private static final String KEY_STORE_ALGORITHM = Cnf.at("Security", "SSLKeyStoreAlgorithm",
            System.getProperty("sun.SSL.keymanager.type", "SunX509"));

    /** The Constant trust store path. */
    private static final String TRUST_STORE_PATH = Cnf.at("Security", "SSLTrustStore",
            System.getProperty("javax.net.ssl.trustStore", KEY_STORE_PATH));

    /** The Constant trust store pass. */
    private static final String TRUST_STORE_PASSWORD = Cnf.at("Security", "SSLTrustStorePassword",
            System.getProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD));

    /** The Constant trust store type. */
    private static final String TRUST_STORE_TYPE = Cnf.at("Security", "SSLTrustStoreType",
            System.getProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE));

    /** The Constant trust store algorithm. */
    private static final String TRUST_STORE_ALGORITHM = Cnf.at("Security", "SSLTrustStoreAlgorithm",
            System.getProperty("sun.SSL.trustmanager.type", KEY_STORE_ALGORITHM));

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SSLSocketFactory.class);

    /** The Constant _authLog. */
    private static final Logger _authLog = LogManager.getLogger("AuthLogs");

    /**
     * Instantiates a new SSL socket factory.
     */
    private SSLSocketFactory() {
        // To hide implicit constructor!
    }

    /**
     * Gets the SSL server socket factory.
     *
     * @return the SSL server socket factory
     */
    public static ServerSocketFactory getSSLServerSocketFactory() {
        try {
            return getSSLContext().getServerSocketFactory();
        } catch (final Exception e) {
            _log.warn("getSSLServerSocketFactory (use default)", e);
            return javax.net.ssl.SSLServerSocketFactory.getDefault();
        }
    }

    /**
     * Gets the SSL socket factory.
     *
     * @return the SSL socket factory
     */
    public static SocketFactory getSSLSocketFactory() {
        try {
            return getSSLContext().getSocketFactory();
        } catch (final Exception e) {
            _log.warn("getSSLSocketFactory (use default)", e);
            return javax.net.ssl.SSLSocketFactory.getDefault();
        }
    }

    /**
     * Gets the SSL context.
     *
     * @return the SSL context
     *
     * @throws java.security.KeyManagementException
     *             the key management exception
     * @throws java.security.NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    public static SSLContext getSSLContext() throws KeyManagementException, NoSuchAlgorithmException {
        final var keyManagers = getKeyManagers();
        final var trustManagers = getTrustManagers();
        return getSSLContext(trustManagers, keyManagers);
    }

    /**
     * Gets the SSL context.
     *
     * @param trustManagers
     *            the trust managers
     * @param keyManagers
     *            the key managers
     *
     * @return the SSL context
     *
     * @throws java.security.NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws java.security.KeyManagementException
     *             the key management exception
     */
    public static SSLContext getSSLContext(final TrustManager[] trustManagers, final KeyManager[] keyManagers)
            throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext ctx;
        ctx = SSLContext.getInstance(SSL_VERSION);
        for (var i = 0; i < trustManagers.length; i++) {
            trustManagers[0] = new ProxyTrustManager((X509TrustManager) trustManagers[0]);
        }
        ctx.init(keyManagers, trustManagers, new SecureRandom());
        return ctx;
    }

    /**
     * Gets the trust managers.
     *
     * @return the trust managers
     */
    private static TrustManager[] getTrustManagers() {
        return getTrustManagers(TRUST_STORE_PATH, TRUST_STORE_PASSWORD, TRUST_STORE_TYPE, TRUST_STORE_ALGORITHM);
    }

    /**
     * Gets the trust managers.
     *
     * @param trustStorePath
     *            the trust store path
     * @param trustStorePass
     *            the trust store pass
     * @param trustStoreType
     *            the trust store type
     * @param trustStoreAlgorithm
     *            the trust store algorithm
     *
     * @return the trust managers
     */
    public static TrustManager[] getTrustManagers(final String trustStorePath, final String trustStorePass,
            final String trustStoreType, final String trustStoreAlgorithm) {
        final var fname = new File(trustStorePath);
        final TrustManagerFactory tmf;
        _log.debug("Reading trustStore from {} (type={},algo={})", fname, trustStoreType, trustStoreAlgorithm);
        try {
            final var ks = KeyStore.getInstance(trustStoreType);
            try (final var fis = new FileInputStream(fname)) {
                ks.load(fis, trustStorePass.toCharArray());
            }
            tmf = TrustManagerFactory.getInstance(trustStoreAlgorithm);
            tmf.init(ks);
        } catch (final Exception e) {
            _log.error("Problem reading trustStore", e);
            return new TrustManager[] {};
        }
        _log.debug("TrustStore succcessfully read: {}", tmf.getAlgorithm());
        return tmf.getTrustManagers();
    }

    /**
     * Gets the key managers.
     *
     * @return the key managers
     */
    private static KeyManager[] getKeyManagers() {
        return getKeyManagers(KEY_STORE_PATH, KEY_STORE_PASSWORD, KEY_STORE_TYPE, KEY_STORE_ALGORITHM);
    }

    /**
     * Gets the key managers.
     *
     * @param keyStorePath
     *            the key store path
     * @param keyStorePass
     *            the key store pass
     * @param keyStoreType
     *            the key store type
     * @param keyStoreAlgorithm
     *            the key store algorithm
     *
     * @return the key managers
     */
    public static KeyManager[] getKeyManagers(final String keyStorePath, final String keyStorePass,
            final String keyStoreType, final String keyStoreAlgorithm) {
        final var fname = new File(keyStorePath);
        final var password = keyStorePass.toCharArray();
        final KeyManagerFactory kmf;
        _log.debug("Reading keyStore from {} (type={},algo={})", fname, keyStoreType, keyStoreAlgorithm);
        try {
            final var ks = KeyStore.getInstance(keyStoreType);
            try (final var fis = new FileInputStream(fname)) {
                ks.load(fis, password);
            }
            kmf = KeyManagerFactory.getInstance(keyStoreAlgorithm);
            kmf.init(ks, password);
        } catch (final Exception e) {
            _log.error("Problem reading keyStore", e);
            return new KeyManager[] {};
        }
        _log.debug("KeyStore succcessfully read: {}", kmf.getAlgorithm());
        return kmf.getKeyManagers();
    }

    /**
     * Sets the https url connection trust all certs.
     *
     * @throws java.security.NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws java.security.KeyManagementException
     *             the key management exception
     */
    public static void setHttpsURLConnectionTrustAllCerts() throws NoSuchAlgorithmException, KeyManagementException {
        final var trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                    throws CertificateException {
                _log.debug("Ignoring https server certificate check");
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                    throws CertificateException {
                _log.debug("Ignoring https client certificate check");
            }
        } };
        final var sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * The Class ProxyTrustManager.
     */
    static class ProxyTrustManager implements X509TrustManager {
        /** The _m. */
        private final X509TrustManager m;

        /**
         * Instantiates a new proxy trust manager.
         *
         * @param m
         *            the m
         */
        ProxyTrustManager(final X509TrustManager m) {
            this.m = m;
        }

        /**
         * Check client trusted.
         *
         * @param chain
         *            the chain
         * @param arg1
         *            the arg 1
         *
         * @throws CertificateException
         *             the certificate exception
         */
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String arg1) throws CertificateException {
            m.checkClientTrusted(chain, arg1);
        }

        /**
         * Check server trusted.
         *
         * @param chain
         *            the chain
         * @param arg1
         *            the arg 1
         *
         * @throws CertificateException
         *             the certificate exception
         */
        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String arg1) throws CertificateException {
            CertificateException ex = null;
            try {
                m.checkServerTrusted(chain, arg1);
            } catch (final CertificateException e) {
                ex = e;
            }
            if (ex != null) {
                // We have an error but do we have a certificate for the
                // ecproxy?
                for (final X509Certificate cert : chain) {
                    final var subject = cert.getSubjectX500Principal().toString();
                    if (subject.indexOf("O=ECMWF") != -1 && subject.indexOf("OU=NSS") != -1
                            && subject.indexOf("CN=ecproxy") != -1) {
                        // This is a connection to the ecproxy so whatever the
                        // certificate let's accept it!
                        if (_authLog.isInfoEnabled()) {
                            _authLog.info("Certificate trusted (invalid): {}", getlog(cert));
                        }
                        return;
                    }
                }
            }
            if (ex != null) {
                // We could not find a trusted certificate!
                if (_authLog.isInfoEnabled()) {
                    for (final X509Certificate cert : chain) {
                        _authLog.error("Certificate NOT trusted: {}", getlog(cert), ex);
                    }
                }
                // Should we stop on non-trusted certificate?
                if (Cnf.at("Security", "checkCertificates", true)) {
                    throw ex;
                }
            } else // Let's log the accepted certificates for accounting!
            if (_authLog.isInfoEnabled()) {
                for (final X509Certificate cert : chain) {
                    _authLog.info("Certificate trusted (valid): {}", getlog(cert));
                }
            }
        }

        /**
         * Gets the log.
         *
         * @param cert
         *            the cert
         *
         * @return the log
         */
        private static String getlog(final X509Certificate cert) {
            return "subject=[" + cert.getSubjectX500Principal() + "], issuer=[" + cert.getSubjectX500Principal()
                    + "], notAfter=[" + Format.formatTime("MMM dd yyyy HH:mm", cert.getNotAfter().getTime()) + "]";
        }

        /**
         * Gets the accepted issuers.
         *
         * @return the accepted issuers
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return m.getAcceptedIssuers();
        }
    }
}
