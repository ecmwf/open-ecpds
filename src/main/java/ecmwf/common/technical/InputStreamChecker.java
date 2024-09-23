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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class InputStreamChecker.
 */
public abstract class InputStreamChecker extends FilterInputStream {

    /**
     * Catch exception.
     *
     * @param service
     *            the service
     * @param t
     *            the t
     */
    public abstract void catchException(String service, Throwable t);

    /**
     * Instantiates a new input stream checker.
     *
     * @param in
     *            the in
     */
    public InputStreamChecker(final InputStream in) {
        super(in);
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read() throws IOException {
        try {
            return in.read();
        } catch (final IOException e) {
            catchException("InputStream.read", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte b[]) throws IOException {
        try {
            return in.read(b);
        } catch (final IOException e) {
            catchException("InputStream.read", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Read.
     */
    @Override
    public int read(final byte b[], final int off, final int len) throws IOException {
        try {
            return in.read(b, off, len);
        } catch (final IOException e) {
            catchException("InputStream.read", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Skip.
     */
    @Override
    public long skip(final long n) throws IOException {
        try {
            return in.skip(n);
        } catch (final IOException e) {
            catchException("InputStream.skip", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Available.
     */
    @Override
    public int available() throws IOException {
        try {
            return in.available();
        } catch (final IOException e) {
            catchException("InputStream.available", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        try {
            in.close();
        } catch (final IOException e) {
            catchException("InputStream.close", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Reset.
     */
    @Override
    public synchronized void reset() throws IOException {
        try {
            in.reset();
        } catch (final IOException e) {
            catchException("InputStream.reset", e);
            throw e;
        }
    }
}
