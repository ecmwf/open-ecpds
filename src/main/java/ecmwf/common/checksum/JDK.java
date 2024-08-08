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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The Class JDK.
 */
final class JDK extends Checksum {

    /** The digest. */
    private final MessageDigest digest;

    /** The in. */
    private final DigestInputStream in;

    /** The out. */
    private final DigestOutputStream out;

    /** The value. */
    private final String value;

    /**
     * Instantiates a new jdk.
     *
     * @param value
     *            the value
     * @param algorithm
     *            the algorithm
     * @param in
     *            the in
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    JDK(final String value, final String algorithm, final InputStream in) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
        this.in = new DigestInputStream(in, digest);
        this.out = null;
        this.value = value;
    }

    /**
     * Instantiates a new jdk.
     *
     * @param value
     *            the value
     * @param algorithm
     *            the algorithm
     * @param out
     *            the out
     *
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    JDK(final String value, final String algorithm, final OutputStream out) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
        this.in = null;
        this.out = new DigestOutputStream(out, digest);
        this.value = value;
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     */
    @Override
    public InputStream getInputStream() {
        return value != null ? new ValidationInputStream(value, this) : in;
    }

    /**
     * Gets the native input stream.
     *
     * @return the native input stream
     */
    @Override
    protected InputStream getNativeInputStream() {
        return in;
    }

    /**
     * Gets the output stream.
     *
     * @return the output stream
     */
    @Override
    public OutputStream getOutputStream() {
        return value != null ? new ValidationOutputStream(value, this) : out;
    }

    /**
     * Gets the native output stream.
     *
     * @return the native output stream
     */
    @Override
    protected OutputStream getNativeOutputStream() {
        return out;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public String getValue() {
        return _printHexBinary(digest.digest()).toLowerCase();
    }

    /** The Constant hexCode. */
    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    /**
     * Prints the hex binary.
     *
     * @param data
     *            the data
     *
     * @return the string
     */
    private String _printHexBinary(final byte[] data) {
        final var r = new StringBuilder(data.length * 2);
        for (final byte b : data) {
            r.append(hexCode[b >> 4 & 0xF]);
            r.append(hexCode[b & 0xF]);
        }
        return r.toString();
    }
}
