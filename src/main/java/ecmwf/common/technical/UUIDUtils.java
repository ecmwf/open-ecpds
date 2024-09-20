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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * The Class UUIDUtils.
 */
public class UUIDUtils {

    /**
     * Instantiates a new UUID utils.
     */
    private UUIDUtils() {
        // To hide the constructor!
    }

    /**
     * Gets the.
     *
     * @param firstLong
     *            the first long
     * @param secondLong
     *            the second long
     *
     * @return the string
     */
    public static String get(final long firstLong, final long secondLong) {
        return bytesToSeparatedHex(Arrays.copyOfRange(hashBytesSHA256(toBytes(firstLong, secondLong)), 0, 16), 4);
    }

    /**
     * Bytes to separated hex.
     *
     * @param bytes
     *            the bytes
     * @param blockSize
     *            the block size
     *
     * @return the string
     */
    private static String bytesToSeparatedHex(final byte[] bytes, final int blockSize) {
        final var hexString = new StringBuilder();
        var blockCount = 0;
        for (final byte b : bytes) {
            final var hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);

            blockCount++;
            if (blockCount == blockSize && blockSize != 0) {
                hexString.append("-");
                blockCount = 0;
            }
        }
        if (hexString.charAt(hexString.length() - 1) == '-') {
            hexString.deleteCharAt(hexString.length() - 1); // Remove trailing "-"
        }
        return hexString.toString();
    }

    /**
     * Combine two long values into a single byte array.
     *
     * @param firstLong
     *            the first long
     * @param secondLong
     *            the second long
     *
     * @return the byte[]
     */
    private static byte[] toBytes(final long firstLong, final long secondLong) {
        final var firstBytes = new byte[Long.BYTES];
        final var secondBytes = new byte[Long.BYTES];
        for (var i = 0; i < Long.BYTES; i++) {
            firstBytes[i] = (byte) (firstLong >> 8 * i);
            secondBytes[i] = (byte) (secondLong >> 8 * i);
        }
        final var combinedBytes = new byte[firstBytes.length + secondBytes.length];
        System.arraycopy(firstBytes, 0, combinedBytes, 0, firstBytes.length);
        System.arraycopy(secondBytes, 0, combinedBytes, firstBytes.length, secondBytes.length);
        return combinedBytes;
    }

    /**
     * Hash the byte array using SHA-256.
     *
     * @param bytes
     *            the bytes
     *
     * @return the byte[]
     */
    private static byte[] hashBytesSHA256(final byte[] bytes) {
        try {
            final var digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(bytes);
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
