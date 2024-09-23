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

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class RandomInputStream.
 */
public class RandomInputStream extends InputStream {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RandomInputStream.class);

    /** The _read. */
    private long _read = 0;

    /** The _length. */
    private long _length = -1;

    /** The _closed. */
    private boolean _closed = false;

    /**
     * Instantiates a new random input stream.
     *
     * @param length
     *            the length
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public RandomInputStream(final long length) throws IOException {
        if (length < 0) {
            throw new IOException("Negative size");
        }
        _log.debug("Length: " + Format.formatSize(length));
        _length = length;
    }

    /**
     * {@inheritDoc}
     *
     * Available.
     */
    @Override
    public int available() throws IOException {
        final var remaining = _length - _read;
        if (remaining <= 0 || _closed) {
            return -1;
        }
        return (int) remaining;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read() throws IOException {
        final var remaining = _length - _read;
        if (remaining <= 0 || _closed) {
            return -1;
        }
        _read++;
        return _read > _length ? -1 : 0;
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte b[], final int off, final int len) throws IOException {
        final var remaining = _length - _read;
        if (remaining <= 0 || _closed) {
            return -1;
        }
        final var read = len < remaining ? len : (int) remaining;
        _read += read;
        return read;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        _closed = true;
    }
}
