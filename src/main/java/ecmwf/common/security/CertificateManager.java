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
 * Utility class for centralised TLS certificate management. Supports:
 * <ul>
 *   <li>Auto-generating a unique self-signed PKCS#12 certificate at first startup</li>
 *   <li>Reading certificate metadata from an existing keystore</li>
 *   <li>Detecting whether the active certificate is self-signed</li>
 *   <li>Generating a Certificate Signing Request (CSR) in PEM format</li>
 *   <li>Importing PEM, PKCS#12 (PFX), or Java KeyStore (JKS) certificates</li>
 *   <li>Exporting the public certificate as PEM</li>
 * </ul>
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.EncryptedData;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.SafeBag;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * The Class CertificateManager.
 */
public final class CertificateManager {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(CertificateManager.class);

    /** Alias used for the single entry stored in every managed keystore. */
    public static final String ALIAS = "ecpds";

    /** Validity period for auto-generated self-signed certificates (10 years). */
    private static final int SELF_SIGNED_VALIDITY_DAYS = 3650;

    /** RSA key size for generated certificates. */
    private static final int KEY_SIZE = 2048;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private CertificateManager() {
        // utility class
    }

    // -------------------------------------------------------------------------
    // Public record for certificate metadata
    // -------------------------------------------------------------------------

    /**
     * Immutable snapshot of the metadata fields of an X.509 certificate that are displayed in the administration UI.
     */
    public record CertificateInfo(String subject, String issuer, String serialNumber, Date notBefore, Date notAfter,
            String fingerprintSha256, String keyAlgorithm, int keySize, boolean selfSigned, boolean expired,
            boolean expiringSoon, List<String> sans) {
    }

    // -------------------------------------------------------------------------
    // Auto-generate
    // -------------------------------------------------------------------------

    /**
     * Ensures a self-signed certificate exists at {@code keystorePath}. If the file is absent or empty the method
     * generates a new RSA-2048 / SHA-256 self-signed certificate and stores it as a PKCS#12 keystore at that path. An
     * existing PKCS#12 private-key keystore is normalized to the interoperable format without changing its identity.
     *
     * @param keystorePath
     *            path where the PKCS#12 keystore should be created
     * @param keystorePassword
     *            password to protect the keystore and private key
     * @param hostname
     *            the CN / SAN hostname to embed in the certificate
     *
     * @throws Exception
     *             if generation or writing fails
     */
    public static synchronized void ensureSelfSigned(final String keystorePath, final String keystorePassword,
            final String hostname) throws Exception {
        ensureSelfSigned(keystorePath, keystorePassword, hostname, "PKCS12");
    }

    /**
     * Ensures a certificate exists and normalizes an existing PKCS#12 private-key keystore so it can be consumed by
     * OpenSSL without legacy algorithms. JKS keystores are left unchanged.
     *
     * @param keystorePath
     *            path where the keystore should be created
     * @param keystorePassword
     *            password to protect the keystore and private key
     * @param hostname
     *            the CN / SAN hostname to embed when generating a new certificate
     * @param keystoreType
     *            configured type of the existing keystore
     *
     * @throws Exception
     *             if migration, generation, or writing fails
     */
    public static synchronized void ensureSelfSigned(final String keystorePath, final String keystorePassword,
            final String hostname, final String keystoreType) throws Exception {
        if (keystorePath == null || keystorePassword == null) {
            _log.warn("Cannot auto-generate certificate: keystorePath or keystorePassword is null");
            return;
        }
        final var ksFile = new File(keystorePath);
        if (ksFile.exists() && ksFile.length() > 0) {
            if ("PKCS12".equalsIgnoreCase(keystoreType)) {
                migratePkcs12(ksFile, keystorePassword);
            }
            _log.debug("Certificate already present at {}", keystorePath);
            return;
        }
        _log.info("No certificate found at {} – generating self-signed certificate for '{}'", keystorePath, hostname);
        generateSelfSigned(keystorePath, keystorePassword, hostname);
        _log.info("Self-signed certificate written to {}", keystorePath);
    }

    /**
     * Generates a new RSA-2048 / SHA-256 self-signed certificate and stores it as a PKCS#12 keystore at
     * {@code keystorePath}, overwriting any existing file.
     *
     * @param keystorePath
     *            destination path for the PKCS#12 keystore
     * @param keystorePassword
     *            password to protect the keystore and private key
     * @param hostname
     *            CN and DNS SAN entry for the certificate
     *
     * @throws Exception
     *             if generation or writing fails
     */
    public static void generateSelfSigned(final String keystorePath, final String keystorePassword,
            final String hostname) throws Exception {
        final var kpGen = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        kpGen.initialize(KEY_SIZE, new SecureRandom());
        final var keyPair = kpGen.generateKeyPair();

        final var subject = new X500Name("CN=" + hostname + ", O=OpenECPDS, OU=Auto-Generated");
        final var serial = BigInteger.valueOf(System.currentTimeMillis());
        final var notBefore = new Date();
        final var notAfter = new Date(notBefore.getTime() + (long) SELF_SIGNED_VALIDITY_DAYS * 86_400_000L);

        final var certBuilder = new JcaX509v3CertificateBuilder(subject, serial, notBefore, notAfter, subject,
                keyPair.getPublic());

        // Basic constraints – this is an end-entity certificate
        certBuilder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));

        // Subject Alternative Names: DNS hostname + localhost + loopback IPs for local access
        final var san = new GeneralNames(new GeneralName[] { new GeneralName(GeneralName.dNSName, hostname),
                new GeneralName(GeneralName.dNSName, "localhost"), new GeneralName(GeneralName.iPAddress, "127.0.0.1"),
                new GeneralName(GeneralName.iPAddress, "::1") });
        certBuilder.addExtension(Extension.subjectAlternativeName, false, san);

        final ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(keyPair.getPrivate());
        final X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certBuilder.build(signer));

        final var ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(ALIAS, keyPair.getPrivate(), keystorePassword.toCharArray(),
                new java.security.cert.Certificate[] { cert });
        writeKeystore(keystorePath, keystorePassword, ks);
    }

    // -------------------------------------------------------------------------
    // Certificate info
    // -------------------------------------------------------------------------

    /**
     * Reads the certificate stored under {@link #ALIAS} in the keystore at {@code keystorePath} and returns a
     * {@link CertificateInfo} snapshot.
     *
     * @param keystorePath
     *            path to the PKCS#12 (or JKS) keystore
     * @param keystorePassword
     *            password for the keystore
     * @param keystoreType
     *            {@code "PKCS12"} or {@code "JKS"}
     *
     * @return certificate metadata, or {@code null} if the file does not exist
     *
     * @throws Exception
     *             on any I/O or crypto error
     */
    public static CertificateInfo getCertificateInfo(final String keystorePath, final String keystorePassword,
            final String keystoreType) throws Exception {
        final var ksFile = new File(keystorePath);
        if (!ksFile.exists()) {
            return null;
        }
        final var cert = loadCertificate(ksFile, keystorePassword, keystoreType);
        if (cert == null) {
            return null;
        }
        return buildInfo(cert);
    }

    /**
     * Returns {@code true} when the certificate is self-signed, i.e. when its issuer DN equals its subject DN.
     *
     * @param cert
     *            the X.509 certificate to inspect
     *
     * @return {@code true} if self-signed
     */
    public static boolean isSelfSigned(final X509Certificate cert) {
        return cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());
    }

    // -------------------------------------------------------------------------
    // CSR generation
    // -------------------------------------------------------------------------

    /**
     * Generates a PKCS#10 Certificate Signing Request (CSR) for the private key stored in the keystore at
     * {@code keystorePath} and returns it as a PEM string.
     *
     * @param keystorePath
     *            path to the PKCS#12 keystore containing the private key
     * @param keystorePassword
     *            password for the keystore
     *
     * @return PEM-encoded CSR
     *
     * @throws Exception
     *             on any I/O or crypto error
     */
    public static String generateCsr(final String keystorePath, final String keystorePassword) throws Exception {
        return generateCsr(keystorePath, keystorePassword, null);
    }

    /**
     * Generates a PKCS#10 Certificate Signing Request (CSR). If {@code hostname} is provided it is used as the CN;
     * otherwise the subject of the existing certificate is reused.
     *
     * @param keystorePath
     *            path to the PKCS#12 keystore containing the private key
     * @param keystorePassword
     *            password for the keystore
     * @param hostname
     *            optional CN to embed; {@code null} to reuse the existing certificate subject
     *
     * @return PEM-encoded CSR
     *
     * @throws Exception
     *             on any I/O or crypto error
     */
    public static String generateCsr(final String keystorePath, final String keystorePassword, final String hostname)
            throws Exception {
        final var ksFile = new File(keystorePath);
        final var ks = openKeystore(ksFile, keystorePassword, "PKCS12");
        final var cert = (X509Certificate) ks.getCertificate(ALIAS);
        final var privateKey = (PrivateKey) ks.getKey(ALIAS, keystorePassword.toCharArray());
        if (cert == null || privateKey == null) {
            throw new IllegalStateException("No certificate/key found under alias '" + ALIAS + "'");
        }
        final var subject = (hostname != null && !hostname.isBlank())
                ? new X500Name("CN=" + hostname.trim() + ", O=OpenECPDS")
                : new X500Name(cert.getSubjectX500Principal().getName());
        final var csrBuilder = new JcaPKCS10CertificationRequestBuilder(subject, cert.getPublicKey());
        final ContentSigner csrSigner = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(privateKey);
        final PKCS10CertificationRequest csr = csrBuilder.build(csrSigner);

        final var sw = new StringWriter();
        try (final var pw = new JcaPEMWriter(sw)) {
            pw.writeObject(csr);
        }
        return sw.toString();
    }

    // -------------------------------------------------------------------------
    // Import
    // -------------------------------------------------------------------------

    /**
     * Imports a certificate (and optional private key) from raw bytes into the keystore at {@code targetKeystorePath},
     * replacing any existing entry. The input can be in PEM, PKCS#12, or JKS format; the method auto-detects the
     * format.
     *
     * @param targetKeystorePath
     *            path where the updated PKCS#12 keystore should be written
     * @param targetPassword
     *            password for the target keystore
     * @param inputBytes
     *            raw bytes of the certificate/keystore to import
     * @param inputPassword
     *            password for the input file (may be {@code null} for plain PEM)
     *
     * @throws Exception
     *             if the format is unsupported or import fails
     */
    public static void importCertificate(final String targetKeystorePath, final String targetPassword,
            final byte[] inputBytes, final String inputPassword) throws Exception {
        final KeyStore targetKs = KeyStore.getInstance("PKCS12");
        targetKs.load(null, null);

        if (isPem(inputBytes)) {
            importFromPem(targetKs, inputBytes, inputPassword, targetPassword);
        } else {
            importFromKeystore(targetKs, inputBytes, inputPassword, ALIAS, targetPassword);
        }

        writeKeystore(targetKeystorePath, targetPassword, targetKs);
    }

    // -------------------------------------------------------------------------
    // Export public certificate
    // -------------------------------------------------------------------------

    /**
     * Exports the public certificate from the keystore at {@code keystorePath} as a PEM-encoded string.
     *
     * @param keystorePath
     *            path to the PKCS#12 (or JKS) keystore
     * @param keystorePassword
     *            password for the keystore
     * @param keystoreType
     *            {@code "PKCS12"} or {@code "JKS"}
     *
     * @return PEM-encoded certificate
     *
     * @throws Exception
     *             on any I/O or crypto error
     */
    public static String exportPublicCertPem(final String keystorePath, final String keystorePassword,
            final String keystoreType) throws Exception {
        final var cert = loadCertificate(new File(keystorePath), keystorePassword, keystoreType);
        if (cert == null) {
            throw new IllegalStateException("No certificate found at " + keystorePath);
        }
        return toPem(cert);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void writeKeystore(final String path, final String password, final KeyStore keystore)
            throws Exception {
        final var chars = password.toCharArray();
        try {
            final var output = new ByteArrayOutputStream();
            keystore.store(output, chars);
            final var bytes = output.toByteArray();
            if (!isOpenSslCompatiblePkcs12(bytes)) {
                throw new IllegalStateException("PKCS12 is not OpenSSL-compatible before replacing " + path);
            }
            final var check = KeyStore.getInstance("PKCS12");
            check.load(new ByteArrayInputStream(bytes), chars);
            if (keystore.size() != check.size()) {
                throw new IllegalStateException("PKCS12 validation failed before replacing " + path);
            }
            final var aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                final var alias = aliases.nextElement();
                if (!check.containsAlias(alias) || keystore.isKeyEntry(alias) != check.isKeyEntry(alias)) {
                    throw new IllegalStateException("PKCS12 validation failed before replacing " + path);
                }
                if (keystore.isKeyEntry(alias)) {
                    final var key = keystore.getKey(alias, chars);
                    final var checkedKey = check.getKey(alias, chars);
                    if (key == null || checkedKey == null || !Arrays.equals(key.getEncoded(), checkedKey.getEncoded())
                            || !Arrays.equals(keystore.getCertificateChain(alias), check.getCertificateChain(alias))) {
                        throw new IllegalStateException("PKCS12 validation failed before replacing " + path);
                    }
                } else if (!java.util.Objects.equals(keystore.getCertificate(alias), check.getCertificate(alias))) {
                    throw new IllegalStateException("PKCS12 validation failed before replacing " + path);
                }
            }
            var target = new File(path).toPath().toAbsolutePath();
            if (Files.exists(target)) {
                target = target.toRealPath();
            }
            Files.createDirectories(target.getParent());
            final var posix = Files.getFileStore(target.getParent())
                    .supportsFileAttributeView(PosixFileAttributeView.class);
            final var temp = posix
                    ? Files.createTempFile(target.getParent(), ".ecpds-keystore-", ".tmp",
                            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")))
                    : Files.createTempFile(target.getParent(), ".ecpds-keystore-", ".tmp");
            try {
                Files.write(temp, bytes);
                if (posix && Files.exists(target)) {
                    final var attributes = Files.readAttributes(target,
                            java.nio.file.attribute.PosixFileAttributes.class);
                    final var view = Files.getFileAttributeView(temp, PosixFileAttributeView.class);
                    view.setOwner(attributes.owner());
                    view.setGroup(attributes.group());
                    view.setPermissions(attributes.permissions());
                }
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temp);
            }
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private static void migratePkcs12(final File file, final String password) throws Exception {
        try {
            if (isOpenSslCompatiblePkcs12(Files.readAllBytes(file.toPath()))
                    && isReadableByDefaultProvider(file, password)) {
                return;
            }
        } catch (final Exception e) {
            _log.debug("Could not inspect PKCS#12 algorithms before migration of {}: {}", file, e.getMessage());
        }
        final var chars = password.toCharArray();
        try {
            KeyStore replacement = null;
            Exception failure = null;
            var readSuccessfully = false;
            for (final var provider : new String[] { null, BouncyCastleProvider.PROVIDER_NAME }) {
                try {
                    final var source = provider == null ? KeyStore.getInstance("PKCS12")
                            : KeyStore.getInstance("PKCS12", provider);
                    try (final var input = new FileInputStream(file)) {
                        source.load(input, chars);
                    }
                    replacement = repackagePkcs12(source, chars);
                    readSuccessfully = true;
                    break;
                } catch (final Exception e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
            if (replacement == null) {
                if (!readSuccessfully && failure != null) {
                    throw new IllegalArgumentException("Cannot migrate PKCS#12 keystore: " + file, failure);
                }
                return;
            }
            writeKeystore(file.getPath(), password, replacement);
            _log.info("Migrated PKCS#12 keystore to OpenSSL-compatible format: {}", file);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private static boolean isReadableByDefaultProvider(final File file, final String password) throws Exception {
        final var chars = password.toCharArray();
        try {
            final var keystore = KeyStore.getInstance("PKCS12");
            try (final var input = new FileInputStream(file)) {
                keystore.load(input, chars);
            }
            final var aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                final var alias = aliases.nextElement();
                if (keystore.isKeyEntry(alias)) {
                    final var key = keystore.getKey(alias, chars);
                    final var chain = keystore.getCertificateChain(alias);
                    if (key instanceof PrivateKey && chain != null && chain.length > 0) {
                        return true;
                    }
                }
            }
            return false;
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private static boolean isOpenSslCompatiblePkcs12(final byte[] bytes) throws Exception {
        final var pfx = new PKCS12PfxPdu(bytes);
        if (!pfx.hasMac() || !NISTObjectIdentifiers.id_sha256.equals(pfx.getMacAlgorithmID().getAlgorithm())) {
            return false;
        }
        var hasEncryptedPrivateKey = false;
        for (final ContentInfo info : pfx.getContentInfos()) {
            if (PKCSObjectIdentifiers.encryptedData.equals(info.getContentType())) {
                final var encryptedData = EncryptedData.getInstance(info.getContent());
                if (!isModernPbe(encryptedData.getEncryptionAlgorithm())) {
                    return false;
                }
            } else if (PKCSObjectIdentifiers.data.equals(info.getContentType())) {
                final var safeContents = ASN1Sequence.getInstance(
                        ASN1Primitive.fromByteArray(ASN1OctetString.getInstance(info.getContent()).getOctets()));
                for (var i = 0; i < safeContents.size(); i++) {
                    final var bag = SafeBag.getInstance(safeContents.getObjectAt(i));
                    if (PKCSObjectIdentifiers.pkcs8ShroudedKeyBag.equals(bag.getBagId())) {
                        hasEncryptedPrivateKey = true;
                        final var encryptedKey = EncryptedPrivateKeyInfo.getInstance(bag.getBagValue());
                        if (!isModernPbe(encryptedKey.getEncryptionAlgorithm())) {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        }
        return hasEncryptedPrivateKey;
    }

    private static boolean isModernPbe(final AlgorithmIdentifier algorithm) {
        if (!PKCSObjectIdentifiers.id_PBES2.equals(algorithm.getAlgorithm())) {
            return false;
        }
        final var pbes2 = PBES2Parameters.getInstance(algorithm.getParameters());
        final var keyDerivation = pbes2.getKeyDerivationFunc();
        if (!PKCSObjectIdentifiers.id_PBKDF2.equals(keyDerivation.getAlgorithm())) {
            return false;
        }
        final var pbkdf2 = PBKDF2Params.getInstance(keyDerivation.getParameters());
        return NISTObjectIdentifiers.id_aes256_CBC.equals(pbes2.getEncryptionScheme().getAlgorithm())
                && PKCSObjectIdentifiers.id_hmacWithSHA256.equals(pbkdf2.getPrf().getAlgorithm());
    }

    private static KeyStore repackagePkcs12(final KeyStore source, final char[] password) throws Exception {
        final var target = KeyStore.getInstance("PKCS12");
        target.load(null, null);
        var hasPrivateKey = false;
        final var aliases = source.aliases();
        while (aliases.hasMoreElements()) {
            final var alias = aliases.nextElement();
            if (source.isKeyEntry(alias)) {
                final var key = source.getKey(alias, password);
                if (!(key instanceof PrivateKey) || source.getCertificateChain(alias) == null) {
                    throw new IllegalArgumentException("Unsupported non-private-key entry in PKCS#12 keystore");
                }
                target.setKeyEntry(alias, key, password, source.getCertificateChain(alias));
                hasPrivateKey = true;
            } else if (source.isCertificateEntry(alias)) {
                target.setCertificateEntry(alias, source.getCertificate(alias));
            }
        }
        return hasPrivateKey ? target : null;
    }

    private static X509Certificate loadCertificate(final File ksFile, final String password, final String type)
            throws Exception {
        final var ks = openKeystore(ksFile, password, type);
        // Try the standard alias first, then iterate
        var cert = (X509Certificate) ks.getCertificate(ALIAS);
        if (cert == null) {
            final var aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                final var a = aliases.nextElement();
                if (ks.isCertificateEntry(a) || ks.isKeyEntry(a)) {
                    cert = (X509Certificate) ks.getCertificate(a);
                    break;
                }
            }
        }
        return cert;
    }

    private static KeyStore openKeystore(final File ksFile, final String password, final String type) throws Exception {
        // Try the supplied type, fall back to PKCS12 then JKS
        for (final var t : new String[] { type, "PKCS12", "JKS" }) {
            if (t == null) {
                continue;
            }
            try {
                final var ks = KeyStore.getInstance(t);
                try (final var fis = new FileInputStream(ksFile)) {
                    ks.load(fis, password != null ? password.toCharArray() : null);
                }
                return ks;
            } catch (final Exception e) {
                _log.debug("Could not open keystore as {}: {}", t, e.getMessage());
            }
        }
        throw new IllegalArgumentException("Cannot open keystore: " + ksFile);
    }

    private static CertificateInfo buildInfo(final X509Certificate cert) throws Exception {
        final boolean selfSigned = isSelfSigned(cert);
        boolean expiredFlag = false;
        try {
            cert.checkValidity();
        } catch (final Exception e) {
            expiredFlag = true;
        }
        final boolean expired = expiredFlag;
        // Expiring within 30 days
        final boolean expiringSoon = !expired
                && cert.getNotAfter().getTime() - System.currentTimeMillis() < 30L * 86_400_000L;
        // Extract Subject Alternative Names
        final var sans = new ArrayList<String>();
        try {
            final var sanCollection = cert.getSubjectAlternativeNames();
            if (sanCollection != null) {
                for (final var sanEntry : sanCollection) {
                    final int type = (Integer) sanEntry.get(0);
                    final Object value = sanEntry.get(1);
                    if (type == 2) { // dNSName
                        sans.add("DNS:" + value);
                    } else if (type == 7) { // iPAddress
                        sans.add("IP:" + value);
                    }
                }
            }
        } catch (final Exception ignored) {
        }
        return new CertificateInfo(cert.getSubjectX500Principal().getName(), cert.getIssuerX500Principal().getName(),
                cert.getSerialNumber().toString(16).toUpperCase(), cert.getNotBefore(), cert.getNotAfter(),
                sha256Fingerprint(cert), cert.getPublicKey().getAlgorithm(), keySize(cert), selfSigned, expired,
                expiringSoon, java.util.Collections.unmodifiableList(sans));
    }

    private static String sha256Fingerprint(final X509Certificate cert) throws Exception {
        final var md = MessageDigest.getInstance("SHA-256");
        final var digest = md.digest(cert.getEncoded());
        final var sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }

    private static int keySize(final X509Certificate cert) {
        final var pk = cert.getPublicKey();
        if (pk instanceof java.security.interfaces.RSAPublicKey rsaKey) {
            return rsaKey.getModulus().bitLength();
        }
        if (pk instanceof java.security.interfaces.ECPublicKey ecKey) {
            return ecKey.getParams().getCurve().getField().getFieldSize();
        }
        return -1;
    }

    private static String toPem(final X509Certificate cert) throws CertificateEncodingException {
        final var encoded = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(cert.getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + encoded + "\n-----END CERTIFICATE-----\n";
    }

    private static boolean isPem(final byte[] bytes) {
        final var s = new String(bytes, 0, Math.min(30, bytes.length), StandardCharsets.US_ASCII);
        return s.startsWith("-----BEGIN");
    }

    private static void importFromPem(final KeyStore targetKs, final byte[] pemBytes, final String password,
            final String targetPassword) throws Exception {
        X509Certificate cert = null;
        PrivateKey privateKey = null;
        try (final var reader = new PEMParser(new InputStreamReader(new ByteArrayInputStream(pemBytes)))) {
            Object obj;
            while ((obj = reader.readObject()) != null) {
                if (obj instanceof X509CertificateHolder holder) {
                    cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .getCertificate(holder);
                } else if (obj instanceof org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo encPrivKey) {
                    final var decryptor = new org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .build(password != null ? password.toCharArray() : new char[0]);
                    privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .getPrivateKey(encPrivKey.decryptPrivateKeyInfo(decryptor));
                } else if (obj instanceof org.bouncycastle.openssl.PEMKeyPair pemKeyPair) {
                    privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .getPrivateKey(pemKeyPair.getPrivateKeyInfo());
                }
            }
        }
        if (cert == null) {
            throw new IllegalArgumentException("No X.509 certificate found in PEM input");
        }
        if (privateKey != null) {
            targetKs.setKeyEntry(ALIAS, privateKey, targetPassword.toCharArray(),
                    new java.security.cert.Certificate[] { cert });
        } else {
            targetKs.setCertificateEntry(ALIAS, cert);
        }
    }

    private static void importFromKeystore(final KeyStore targetKs, final byte[] bytes, final String password,
            final String alias, final String targetPassword) throws Exception {
        // Try PKCS12, then JKS
        for (final var type : new String[] { "PKCS12", "JKS" }) {
            try {
                final var srcKs = KeyStore.getInstance(type);
                srcKs.load(new ByteArrayInputStream(bytes), password != null ? password.toCharArray() : null);
                final var srcAliases = srcKs.aliases();
                while (srcAliases.hasMoreElements()) {
                    final var a = srcAliases.nextElement();
                    if (srcKs.isKeyEntry(a)) {
                        final var key = srcKs.getKey(a, password != null ? password.toCharArray() : null);
                        final var chain = srcKs.getCertificateChain(a);
                        targetKs.setKeyEntry(alias, key, targetPassword.toCharArray(), chain);
                        return;
                    }
                }
                // No key entry — copy first cert-only entry
                final var srcAliases2 = srcKs.aliases();
                if (srcAliases2.hasMoreElements()) {
                    final var a = srcAliases2.nextElement();
                    targetKs.setCertificateEntry(alias, srcKs.getCertificate(a));
                    return;
                }
            } catch (final Exception e) {
                _log.debug("Not a {} keystore: {}", type, e.getMessage());
            }
        }
        throw new IllegalArgumentException("Cannot parse input as PKCS#12 or JKS keystore");
    }
}
