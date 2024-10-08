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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

/**
 * The Class ChecksumOutputStream.
 */
final class ChecksumOutputStream extends FilterOutputStream {

    /** The checksum. */
    private final Checksum checksum;

    /**
     * Instantiates a new checksum output stream.
     *
     * @param checksum
     *            the checksum
     * @param out
     *            the out
     */
    ChecksumOutputStream(final Checksum checksum, final OutputStream out) {
        super(out);
        this.checksum = checksum;
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final int b) throws IOException {
        out.write(b);
        checksum.update((byte) b);
    }

    /**
     * {@inheritDoc}
     *
     * Write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        out.write(b, off, len);
        checksum.update(b, off, len);
    }
}
