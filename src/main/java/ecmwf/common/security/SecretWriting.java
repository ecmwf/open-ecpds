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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.text.BASE64Coder;

/**
 * The Class SecretWriting.
 */
public class SecretWriting {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(SecretWriting.class);

    /** The key initialized. */
    private static final AtomicBoolean keyInitialized = new AtomicBoolean(false);

    /** The key. */
    private static Key key = null;

    /**
     * Utility classes should not have a public constructor.
     */
    private SecretWriting() {
    }

    /**
     * Inits the keys. If required to use a persistent keystore then first try to open it, if not found then create one.
     * If not required to use a persistent keystore then create an in-memory keystore.
     *
     * @throws KeyStoreException
     *             the key store exception
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws CertificateException
     *             the certificate exception
     * @throws UnrecoverableKeyException
     *             the unrecoverable key exception
     */
    private static void initKeys() throws KeyStoreException, NoSuchAlgorithmException, IOException,
            CertificateException, UnrecoverableKeyException {
        if (keyInitialized.compareAndSet(false, true)) {
            final var passwordString = Cnf.at("Security", "JCEKeyStorePassword");
            final var keystoreFileName = Cnf.at("Security", "JCEKeyStore");
            final var alias = Cnf.at("Security", "JCEAlias", SecretWriting.class.getName());
            final var keystore = KeyStore.getInstance(Cnf.at("Security", "JCEKeyStoreType", "JCEKS"));
            if (passwordString != null && keystoreFileName != null) {
                // We want to have a persistent keystore!
                final var password = passwordString.toCharArray();
                final var keystoreFile = new File(keystoreFileName);
                if (keystoreFile.exists()) {
                    _log.debug("Loading Keystore (type: {}, alias: {}): {}", keystore.getType(), alias,
                            keystoreFile.getCanonicalPath());
                    try {
                        try (final var fis = new FileInputStream(keystoreFile)) {
                            keystore.load(fis, password);
                            final var en = keystore.aliases();
                            if (_log.isDebugEnabled()) {
                                while (_log.isDebugEnabled() && en.hasMoreElements()) {
                                    _log.debug("Alias found: {}", en.nextElement());
                                }
                            }
                        }
                        key = keystore.getKey(alias, password);
                    } catch (final IOException e) {
                        _log.warn("KeyStore not valid: {}", keystoreFile.getCanonicalPath(), e);
                        throw e;
                    }
                } else {
                    _log.debug("Creating Keystore (type: {}, alias: {}): {}", keystore.getType(), alias,
                            keystoreFile.getCanonicalPath());
                    key = generateKey();
                    keystore.load(null, password);
                    keystore.setKeyEntry(alias, key, password, null);
                    keystoreFile.getParentFile().mkdirs();
                    try (final var fos = new FileOutputStream(keystoreFile)) {
                        keystore.store(fos, password);
                    }
                }
            } else {
                _log.debug("No Keystore required (generating temporary in-memory key)");
                key = generateKey();
            }
        }
    }

    /**
     * Gets the cipher. Get a new instance of a cipher (to be thread-safe).
     *
     * @return the cipher
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws NoSuchPaddingException
     *             the no such padding exception
     * @throws InvalidKeyException
     *             the invalid key exception
     */
    private static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        final var cipherAlgorithm = Cnf.at("Security", "JCECipherAlgorithm", "DES/ECB/PKCS5Padding");
        final var cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher;
    }

    /**
     * Generate key. Generating the key based on the local configuration.
     *
     * @return the key
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    private static Key generateKey() throws NoSuchAlgorithmException {
        final var keyAlgorithm = Cnf.at("Security", "JCEKeyStoreAlgorithm", "DES");
        final KeyGenerator generator;
        generator = KeyGenerator.getInstance(keyAlgorithm);
        generator.init(new SecureRandom());
        final var keySize = Cnf.at("Security", "JCEKeyStoreSize", -1);
        if (keySize != -1) {
            generator.init(keySize);
        }
        return generator.generateKey();
    }

    /**
     * Encrypt.
     *
     * @param data
     *            the data
     *
     * @return the string
     *
     * @throws UnrecoverableKeyException
     *             the unrecoverable key exception
     * @throws InvalidKeyException
     *             the invalid key exception
     * @throws KeyStoreException
     *             the key store exception
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws CertificateException
     *             the certificate exception
     * @throws NoSuchPaddingException
     *             the no such padding exception
     * @throws IllegalBlockSizeException
     *             the illegal block size exception
     * @throws BadPaddingException
     *             the bad padding exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String encrypt(final String data)
            throws UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
        return data == null ? null : encrypt(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Try to encrypt. If cannot encrypt then return the plain text!
     *
     * @param data
     *            the data
     * @param lineLength
     *            the line length
     *
     * @return the string
     */
    public static String tryEncrypt(final String data, final int lineLength) {
        try {
            return data == null ? null
                    : breakStringIntoLines(encrypt(data.getBytes(StandardCharsets.UTF_8)), lineLength);
        } catch (final Exception e) {
            _log.warn("Cannot encrypt", e);
            return data;
        }
    }

    /**
     * Break the input data into multiple lines.
     *
     * @param input
     *            the input
     * @param lineLength
     *            the line length
     *
     * @return the string
     */
    private static String breakStringIntoLines(final String input, final int lineLength) {
        final var length = input.length();
        final var numLines = (length + lineLength - 1) / lineLength; // Calculate the number of lines
        final var lines = new StringBuilder();
        for (var i = 0; i < numLines; i++) {
            if (numLines > 1) {
                lines.append("\n");
            }
            final var startIndex = i * lineLength;
            final var endIndex = Math.min(startIndex + lineLength, length);
            lines.append(input.substring(startIndex, endIndex));
        }
        if (numLines > 1) {
            lines.append("\n");
        }
        return lines.toString();
    }

    /**
     * Encrypt.
     *
     * @param data
     *            the data
     *
     * @return the string
     *
     * @throws UnrecoverableKeyException
     *             the unrecoverable key exception
     * @throws KeyStoreException
     *             the key store exception
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws CertificateException
     *             the certificate exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchPaddingException
     *             the no such padding exception
     * @throws InvalidKeyException
     *             the invalid key exception
     * @throws IllegalBlockSizeException
     *             the illegal block size exception
     * @throws BadPaddingException
     *             the bad padding exception
     */
    public static String encrypt(final byte[] data)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (data == null) {
            return null;
        }
        initKeys();
        return new String(BASE64Coder.encode(getCipher().doFinal(data)));
    }

    /**
     * Decrypt Base64 encoded data.
     *
     * @param data
     *            the data
     *
     * @return the string
     *
     * @throws UnrecoverableKeyException
     *             the unrecoverable key exception
     * @throws KeyStoreException
     *             the key store exception
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws CertificateException
     *             the certificate exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchPaddingException
     *             the no such padding exception
     * @throws InvalidKeyException
     *             the invalid key exception
     */
    public static String decrypt(final String data) throws UnrecoverableKeyException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException, NoSuchPaddingException, InvalidKeyException {
        if (data == null) {
            return null;
        }
        initKeys();
        try {
            final var raw = BASE64Coder.decode(data);
            final var stringBytes = getCipher().doFinal(raw);
            return new String(stringBytes, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            _log.warn("Cannot decrypt, trying old (unsecure) v201 mechanism", e);
            return decodeV201(data);
        }
    }

    /**
     * Decrypt a string which was previously encrypted with the tryEncrypt method.
     *
     * @param data
     *            the data
     *
     * @return the string
     */
    public static String tryDecrypt(final String data) {
        try {
            return data == null ? null : decrypt(data.replace("\n", ""));
        } catch (final Exception e) {
            _log.warn("Cannot decrypt", e);
            return data;
        }
    }

    /**
     * decodeV201 (old initial mechanism used on or before v201).
     *
     * @param data
     *            the data
     *
     * @return the string
     */
    private static String decodeV201(final String data) {
        final var sBuffer = data.substring(data.indexOf("123456789") + 9);
        final var sbu = new ByteArrayOutputStream();
        for (var i = 0; i < sBuffer.length(); i += 3) {
            sbu.write(500 - Integer.parseInt(sBuffer.substring(i, i + 3)));
        }
        return new String(sbu.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Decrypt.
     *
     * @param data
     *            the data
     *
     * @return the string
     *
     * @throws UnrecoverableKeyException
     *             the unrecoverable key exception
     * @throws InvalidKeyException
     *             the invalid key exception
     * @throws KeyStoreException
     *             the key store exception
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws CertificateException
     *             the certificate exception
     * @throws NoSuchPaddingException
     *             the no such padding exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String decrypt(final byte[] data) throws UnrecoverableKeyException, InvalidKeyException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchPaddingException, IOException {
        return data == null ? null : decrypt(new String(data, StandardCharsets.UTF_8));
    }
}
