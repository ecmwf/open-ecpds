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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Checksum;

/**
 * The Class ChecksumInputStream.
 */
final class ChecksumInputStream extends FilterInputStream {

    /** The checksum. */
    private final Checksum checksum;

    /**
     * Instantiates a new checksum input stream.
     *
     * @param checksum
     *            the checksum
     * @param in
     *            the in
     */
    ChecksumInputStream(final Checksum checksum, final InputStream in) {
        super(in);
        this.checksum = checksum;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read() throws IOException {
        final var c = super.read();
        if (c >= 0) {
            checksum.update(c);
        }
        return c;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final var len2 = super.read(b, off, len);
        if (len2 > 0) {
            checksum.update(b, off, len2);
        }
        return len2;
    }
}
