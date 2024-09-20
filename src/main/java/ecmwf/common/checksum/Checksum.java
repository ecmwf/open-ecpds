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

package ecmwf.common.checksum;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 * The Class Checksum.
 */
public abstract class Checksum {

    /**
     * The Enum Algorithm.
     */
    public enum Algorithm {

        /** The adler32. */
        ADLER32("Adler32"),

        /** The crc32. */
        CRC32("CRC32"),

        /** The md5. */
        MD5("MD5"),

        /** The sha1. */
        SHA1("SHA-1"),

        /** The sha256. */
        SHA256("SHA-256");

        /** The name. */
        private final String name;

        /**
         * Instantiates a new algorithm.
         *
         * @param name
         *            the name
         */
        Algorithm(final String name) {
            this.name = name;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Gets the algorithm.
     *
     * @param name
     *            the name
     *
     * @return the algorithm
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    public static Algorithm getAlgorithm(final String name) throws NoSuchAlgorithmException {
        for (final Algorithm algorithm : Algorithm.values()) {
            if (algorithm.name.equalsIgnoreCase(name)) {
                return algorithm;
            }
        }
        throw new NoSuchAlgorithmException(name);
    }

    /**
     * Gets the checksum.
     *
     * @param algorithm
     *            the algorithm
     * @param in
     *            the in
     *
     * @return the checksum
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    public static Checksum getChecksum(final Algorithm algorithm, final InputStream in)
            throws NoSuchAlgorithmException {
        return getChecksum(null, algorithm, in);
    }

    /**
     * Gets the checksum.
     *
     * @param algorithm
     *            the algorithm
     * @param out
     *            the out
     *
     * @return the checksum
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    public static Checksum getChecksum(final Algorithm algorithm, final OutputStream out)
            throws NoSuchAlgorithmException {
        return getChecksum(null, algorithm, out);
    }

    /**
     * Gets the checksum.
     *
     * @param value
     *            the value
     * @param algorithm
     *            the algorithm
     * @param in
     *            the in
     *
     * @return the checksum
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    public static Checksum getChecksum(final String value, final Algorithm algorithm, final InputStream in)
            throws NoSuchAlgorithmException {
        return switch (algorithm) {
        case ADLER32 -> new Adler32(value, in);
        case CRC32 -> new CRC32(value, in);
        case MD5 -> new JDK(value, Algorithm.MD5.name, in);
        case SHA1 -> new JDK(value, Algorithm.SHA1.name, in);
        case SHA256 -> new JDK(value, Algorithm.SHA256.name, in);
        default -> throw new NoSuchAlgorithmException();
        };
    }

    /**
     * Gets the checksum.
     *
     * @param value
     *            the value
     * @param algorithm
     *            the algorithm
     * @param out
     *            the out
     *
     * @return the checksum
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    public static Checksum getChecksum(final String value, final Algorithm algorithm, final OutputStream out)
            throws NoSuchAlgorithmException {
        return switch (algorithm) {
        case ADLER32 -> new Adler32(value, out);
        case CRC32 -> new CRC32(value, out);
        case MD5 -> new JDK(value, Algorithm.MD5.name, out);
        case SHA1 -> new JDK(value, Algorithm.SHA1.name, out);
        case SHA256 -> new JDK(value, Algorithm.SHA256.name, out);
        default -> throw new NoSuchAlgorithmException();
        };
    }

    /**
     * Gets the native input stream.
     *
     * @return the native input stream
     */
    protected abstract InputStream getNativeInputStream();

    /**
     * Gets the input stream.
     *
     * @return the input stream
     */
    public abstract InputStream getInputStream();

    /**
     * Gets the native output stream.
     *
     * @return the native output stream
     */
    protected abstract OutputStream getNativeOutputStream();

    /**
     * Gets the output stream.
     *
     * @return the output stream
     */
    public abstract OutputStream getOutputStream();

    /**
     * Gets the value.
     *
     * @return the value
     */
    public abstract String getValue();
}
