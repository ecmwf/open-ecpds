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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * The Class Password.
 */
public final class Password {

    /**
     * Generate hash.
     *
     * @param password
     *            the password
     *
     * @return the string
     *
     * @throws java.security.NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws java.security.spec.InvalidKeySpecException
     *             the invalid key spec exception
     */
    public static String generateHash(final String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final var iterations = 1000;
        final var chars = password.toCharArray();
        final var salt = _getSalt();
        final var spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        final var skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final var hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + _toHex(salt) + ":" + _toHex(hash);
    }

    /**
     * Check.
     *
     * @param password
     *            the password
     * @param storedHash
     *            the stored hash
     *
     * @return true, if successful
     *
     * @throws java.security.NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws java.security.spec.InvalidKeySpecException
     *             the invalid key spec exception
     */
    public static boolean check(final String password, final String storedHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final var parts = storedHash.split(":");
        final var iterations = Integer.parseInt(parts[0]);
        final var salt = _fromHex(parts[1]);
        final var hash = _fromHex(parts[2]);
        final var spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
        final var skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final var testHash = skf.generateSecret(spec).getEncoded();
        var diff = hash.length ^ testHash.length;
        for (var i = 0; i < hash.length && i < testHash.length; i++) {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    /**
     * Gets the salt.
     *
     * @return the byte[]
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    private static byte[] _getSalt() throws NoSuchAlgorithmException {
        final var sr = SecureRandom.getInstance("SHA1PRNG");
        final var salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    /**
     * To hex.
     *
     * @param array
     *            the array
     *
     * @return the string
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    private static String _toHex(final byte[] array) throws NoSuchAlgorithmException {
        final var bi = new BigInteger(1, array);
        final var hex = bi.toString(16);
        final var paddingLength = array.length * 2 - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }
        return hex;
    }

    /**
     * From hex.
     *
     * @param hex
     *            the hex
     *
     * @return the byte[]
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    private static byte[] _fromHex(final String hex) throws NoSuchAlgorithmException {
        final var bytes = new byte[hex.length() / 2];
        for (var i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
