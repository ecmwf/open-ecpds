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

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 64;
    // Number of iterations for PBKDF2
    private static final int ITERATIONS = 10000;
    // Algorithm used for hashing
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";

    // Method to generate a new salt
    private static byte[] generateSalt() throws NoSuchAlgorithmException {
        var salt = new byte[SALT_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(salt);
        return salt;
    }

    // Method to hash the password using the provided salt
    private static byte[] hashPassword(final char[] password, final byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return SecretKeyFactory.getInstance(ALGORITHM)
                .generateSecret(new PBEKeySpec(password, salt, ITERATIONS, HASH_LENGTH * 8)).getEncoded();
    }

    // Method to create a combined string with salt and hashed password. The actual
    // length will always be 108 characters for the provided configuration of salt
    // and hash sizes.
    /**
     * <p>
     * generateSaltAndHash.
     * </p>
     *
     * @param password
     *            a {@link java.lang.String} object
     *
     * @return a {@link java.lang.String} object
     *
     * @throws java.security.NoSuchAlgorithmException
     *             if any.
     * @throws java.security.spec.InvalidKeySpecException
     *             if any.
     */
    public static String generateSaltAndHash(final String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Generate new salt
        var salt = generateSalt();
        // Hash the password with the salt
        var hash = hashPassword(password.toCharArray(), salt);
        // Combine salt and hash, and encode as Base64 for easier storage
        var saltAndHash = new byte[salt.length + hash.length];
        // Copy salt first
        System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
        // Copy hash after salt
        System.arraycopy(hash, 0, saltAndHash, salt.length, hash.length);
        // Encode as a single Base64 string (salt + hash)
        return Base64.getEncoder().encodeToString(saltAndHash);
    }

    // Method to verify the password
    /**
     * <p>
     * verifyPassword.
     * </p>
     *
     * @param password
     *            a {@link java.lang.String} object
     * @param storedSaltAndHash
     *            a {@link java.lang.String} object
     *
     * @return a boolean
     *
     * @throws java.security.NoSuchAlgorithmException
     *             if any.
     * @throws java.security.spec.InvalidKeySpecException
     *             if any.
     */
    public static boolean verifyPassword(final String password, final String storedSaltAndHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the stored salt+hash Base64 string
        var saltAndHash = Base64.getDecoder().decode(storedSaltAndHash);
        // Extract the salt (first part) and hash (second part)
        var salt = new byte[SALT_LENGTH];
        var storedHash = new byte[HASH_LENGTH];
        // Extract salt
        System.arraycopy(saltAndHash, 0, salt, 0, SALT_LENGTH);
        // Extract hash
        System.arraycopy(saltAndHash, SALT_LENGTH, storedHash, 0, HASH_LENGTH);
        // Hash the provided password using the extracted salt
        var hashOfInput = hashPassword(password.toCharArray(), salt);
        // Compare the newly generated hash with the stored hash
        return slowEquals(storedHash, hashOfInput);
    }

    // Method to prevent timing attacks during hash comparison
    private static boolean slowEquals(final byte[] a, final byte[] b) {
        int diff = a.length ^ b.length;
        for (var i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /**
     * <p>
     * main.
     * </p>
     *
     * @param args
     *            an array of {@link java.lang.String} objects
     *
     * @throws java.lang.Exception
     *             if any.
     */
    public static void main(String[] args) throws Exception {
        // Example usage
        String password = "SecurePassword123";
        // Generate the salt and hash and print it
        String saltAndHash = generateSaltAndHash(password);
        System.out.println("Salt and Hash: " + saltAndHash);
        // Verify the password
        boolean isPasswordValid = verifyPassword(password, saltAndHash);
        System.out.println("Password valid: " + isPasswordValid);
    }
}
