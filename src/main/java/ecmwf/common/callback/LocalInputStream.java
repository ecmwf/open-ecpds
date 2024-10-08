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

package ecmwf.common.callback;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;

/**
 * The Class LocalInputStream.
 */
public final class LocalInputStream extends InputStream {
    /** The _in. */
    private final RemoteInputStream _in;

    /**
     * Instantiates a new local input stream.
     *
     * @param in
     *            the in
     */
    public LocalInputStream(final RemoteInputStream in) {
        _in = in;
    }

    /**
     * {@inheritDoc}
     *
     * Available.
     */
    @Override
    public int available() throws IOException {
        return _in.available();
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public synchronized void close() throws IOException {
        _in.close();
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read() throws IOException {
        return _in.read();
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        final var data = _in.read(len);
        if ((len = data.getLen()) > 0) {
            System.arraycopy(data.getBytes(), 0, b, off, len);
        }
        return len;
    }

    /**
     * {@inheritDoc}
     *
     * Reset.
     */
    @Override
    public void reset() throws IOException {
        _in.reset();
    }

    /**
     * {@inheritDoc}
     *
     * Skip.
     */
    @Override
    public long skip(final long n) throws IOException {
        return _in.skip(n);
    }
}
