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

/**
 * The Class CRC32.
 */
final class CRC32 extends Checksum {

    /** The crc 32. */
    private final java.util.zip.CRC32 crc32;

    /** The in. */
    private final ChecksumInputStream in;

    /** The out. */
    private final ChecksumOutputStream out;

    /** The value. */
    private final String value;

    /**
     * Instantiates a new crc32.
     *
     * @param value
     *            the value
     * @param in
     *            the in
     */
    CRC32(final String value, final InputStream in) {
        crc32 = new java.util.zip.CRC32();
        this.in = new ChecksumInputStream(crc32, in);
        this.out = null;
        this.value = value;
    }

    /**
     * Instantiates a new crc32.
     *
     * @param value
     *            the value
     * @param out
     *            the out
     */
    CRC32(final String value, final OutputStream out) {
        crc32 = new java.util.zip.CRC32();
        this.in = null;
        this.out = new ChecksumOutputStream(crc32, out);
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
        return Long.toHexString(crc32.getValue());
    }
}
